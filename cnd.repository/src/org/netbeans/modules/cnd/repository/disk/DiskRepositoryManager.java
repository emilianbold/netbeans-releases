/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.cnd.repository.disk;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.api.DatabaseTable;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.impl.BaseRepository;
import org.netbeans.modules.cnd.repository.queue.KeyValueQueue;
import org.netbeans.modules.cnd.repository.queue.RepositoryQueue;
import org.netbeans.modules.cnd.repository.queue.RepositoryThreadManager;
import org.netbeans.modules.cnd.repository.queue.RepositoryWriter;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 * @author Sergey Grinev
 */
public class DiskRepositoryManager extends BaseRepository implements RepositoryWriter {
    
    private static final Logger LOG = Logger.getLogger(DiskRepositoryManager.class.getName());
    private final Map<Integer, Unit> units;
    private final RepositoryQueue queue;
    private final RepositoryThreadManager threadManager;
    private final Persistent removedObject;
    private final ReadWriteLock queueLock;
    private final Map<Integer, Object> unitLocks = new HashMap<Integer, Object>();

    private static final class UnitLock {}
    private final Object mainUnitLock = new UnitLock();

    public DiskRepositoryManager(int id, CacheLocation cacheLocation) {
        super(id, cacheLocation);
        removedObject = new RemovedPersistent();
        queueLock = new ReentrantReadWriteLock(true);
        threadManager = new RepositoryThreadManager(this, queueLock);
        queue = threadManager.getQueue();
        units = new ConcurrentHashMap<Integer, Unit>();
    }

    @Override
    public DatabaseTable getDatabaseTable(Key unitKey, String tableID) {
        try {
            UnitImpl impl = (UnitImpl) getCreateUnit(unitKey);
            return impl.getDatabaseTable(tableID);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unitKey.getUnitId(), getUnitNameSafe(unitKey), new RepositoryException(ex));
        }
        return null;
    }

    private Object getUnitLock(int unitId) {
        synchronized (mainUnitLock) {
            Object lock = unitLocks.get(unitId);
            if (lock == null) {
                lock = new NamedLock("unitId=" + unitId); // NOI18N
                unitLocks.put(unitId, lock);
            }
            return lock;
        }
    }

    /** Never returns null - throws exceptions */
    private Unit getCreateUnit(Key key) throws IOException {
        assert key != null;
        Unit unit = units.get(key.getUnitId());
        if (unit != null) {
            return unit;
        }
        return getCreateUnit(key.getUnitId(), key.getUnit());
    }

    /** Never returns null - throws exceptions */
    private Unit getCreateUnit(int unitId, CharSequence unitName) throws IOException {
        assert unitName != null;

        Unit unit = units.get(unitId);

        if (unit == null) {
            unit = null;
            synchronized (getUnitLock(unitId)) {
                unit = units.get(unitId);
                if (unit == null) {
                    if (RepositoryListenersManager.getInstance().fireUnitOpenedEvent(unitId, unitName)) {
                        getTranslation().loadUnitIndex(unitName);
                        unit = new UnitImpl(unitId, unitName, this);
                        units.put(unitId, unit);
                    }
                }
            }
        }

        return unit;
    }

    @Override
    public void put(Key key, Persistent obj) {
        try {
            // to expencive assert
            //assert KeyPresentationFactorySupport.getDefaultFactory().create(key.getDataPresentation()).equals(key);
            getCreateUnit(key).putToCache(key, obj);
            queue.addLast(key, obj);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnitId(), getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

    @Override
    public void hang(Key key, Persistent obj) {
        try {
            getCreateUnit(key).hang(key, obj);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnitId(), getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

    @Override
    public void write(Key key, Persistent object) {
        try {
            Unit diskRep = getCreateUnit(key);
            if (object instanceof RemovedPersistent) {
                diskRep.removePhysically(key);
            } else {
                diskRep.putPhysically(key, object);
            }
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnitId(), getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

    @Override
    public Persistent get(Key key) {
        try {
            return getCreateUnit(key).get(key);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnitId(), getUnitNameSafe(key), new RepositoryException(ex));
        }
        return null;
    }

    @Override
    public Persistent tryGet(Key key) {
        try {
            return getCreateUnit(key).tryGet(key);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnitId(), getUnitNameSafe(key), new RepositoryException(ex));
        }
        return null;
    }

    @Override
    public void remove(Key key) {
        try {
            getCreateUnit(key).removeFromCache(key);
            queue.addLast(key, removedObject);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnitId(), getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

    @Override
    public void shutdown() {
        if (threadManager != null) {
            threadManager.shutdown();
        }
        List<Entry<Integer, Unit>> entries = new ArrayList<Entry<Integer, Unit>>(units.entrySet());
        for (Entry<Integer, Unit> entry : entries) {
            // iz #146241 IllegalStateException in the case revious session terminated with ^C in console
            // if there are projects that aren't yet closed => the data might be corrupted! => clean untit
            closeUnit(entry.getKey(), true, null);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.INFO, "Closing unit{0} done.", entry.getValue().getName());
            }
        }

        try {
            queueLock.writeLock().lock();
            cleanAndWriteQueue();
            if (!units.isEmpty()) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "Not empty unit list after closing: {0}", units.toString());
                }
            }
            units.clear();
        } finally {
            queueLock.writeLock().unlock();
        }
        getTranslation().shutdown();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.INFO, "Repository shutdown done.");
        }
    }

    @Override
    public boolean maintenance(long timeout) {
        if (units.isEmpty()) {
            return false;
        }

        Collection<Unit> values = units.values();
        Unit[] unitList = values.toArray(new Unit[values.size()]);
        Arrays.sort(unitList, new MaintenanceComparator());
        boolean needMoreTime = false;
        long start = System.currentTimeMillis();
        for (int i = 0; i < unitList.length; i++) {
            if (timeout <= 0) {
                needMoreTime = true;
                break;
            }

            try {
                if (unitList[i].maintenance(timeout)) {
                    needMoreTime = true;
                }
            } catch (IOException ex) {
                RepositoryListenersManager.getInstance().fireAnException(
                        unitList[i].getId(), unitList[i].getName(), new RepositoryException(ex));
            }
            timeout -= (System.currentTimeMillis() - start);
        }
        return needMoreTime;
    }

    @Override
    public void openUnit(int unitId, CharSequence unitName) {
        try {
            synchronized (getUnitLock(unitId)) {
                getCreateUnit(unitId, unitName);
            }
        } catch (Throwable exc) {
            RepositoryListenersManager.getInstance().fireAnException(unitId, unitName,
                    new RepositoryException(exc));
        }
    }

    @Override
    public void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits) {
        CharSequence unitName = RepositoryAccessor.getTranslator().getUnitName(unitId);
        Set<CharSequence> requiredUnitNames = null;
        if (requiredUnits != null) {
            requiredUnitNames = new LinkedHashSet<CharSequence>(requiredUnits.size());
            for (Integer integer : requiredUnits) {
                requiredUnitNames.add(getTranslation().getUnitName(unitId));
            }
        }
        synchronized (getUnitLock(unitId)) {
            closeUnit2(unitId, unitName, cleanRepository, requiredUnitNames);
        }
    }

    private void closeUnit2(final int unitId, final CharSequence unitName, final boolean cleanRepository, Set<CharSequence> requiredUnits) {

        try {
            queueLock.writeLock().lock();
            Collection<RepositoryQueue.Entry<Key, Persistent>> removedEntries = queue.clearQueue(new UnitFilter(unitId));
            if (!cleanRepository) {
                for (RepositoryQueue.Entry<Key, Persistent> entry : removedEntries) {
                    write(entry.getKey(), entry.getValue());
                }
            }
            
            Unit unit = units.remove(unitId);

            if (unit != null) {
                try {
                    unit.close();
                } catch (Throwable exc) {
                    RepositoryListenersManager.getInstance().fireAnException(unitId, unitName,
                            new RepositoryException(exc));
                }
            }

        } finally {
            queueLock.writeLock().unlock();
        }
        if (CndUtils.isDebugMode()) {
            Collection<KeyValueQueue.Entry<Key, Persistent>> clearQueue = queue.clearQueue(new UnitFilter(unitId));
            if (!clearQueue.isEmpty()) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "UNSAVED ENTRIES FOR {0}", unitName);
                    for (KeyValueQueue.Entry<Key, Persistent> entry : clearQueue) {
                        LOG.log(Level.INFO, "\t{0}\n\t{1}", new Object[]{entry.getKey(), entry.getValue()});
                    }
                }
            }
        }

        //clean the repository cach files here if it is necessary
        //
        StorageAllocator allocator = getStorageAllocator();
        if (cleanRepository) {
            allocator.deleteUnitFiles(unitName, true);
        }
        allocator.closeUnit(unitName);

        getTranslation().closeUnit(unitName, requiredUnits);
        RepositoryListenersManager.getInstance().fireUnitClosedEvent(unitId, unitName);
    }

    @Override
    public void removeUnit(int unitId) {
        CharSequence unitName = RepositoryAccessor.getTranslator().getUnitName(unitId);
        synchronized (getUnitLock(unitId)) {
            closeUnit2(unitId, unitName, true, Collections.<CharSequence>emptySet());
            getTranslation().removeUnit(unitName);
        }
    }

    @Override
    public void debugClear() {
        List<Entry<Integer, Unit>> entries = new ArrayList<Entry<Integer, Unit>>(units.entrySet());
        for (Entry<Integer, Unit> entry : entries) {
            entry.getValue().debugClear();
        }
        try {
            queueLock.writeLock().lock();
            cleanAndWriteQueue();
        } finally {
            queueLock.writeLock().unlock();
        }
    }

    private void cleanAndWriteQueue() {
        Collection<RepositoryQueue.Entry<Key, Persistent>> removedEntries = queue.clearQueue(new AllFilter());
        for (RepositoryQueue.Entry<Key, Persistent> entry : removedEntries) {
            write(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void cleanCaches() {
        getStorageAllocator().cleanRepositoryCaches();
    }

    @Override
    public void registerRepositoryListener(final RepositoryListener aListener) {
    }

    @Override
    public void unregisterRepositoryListener(final RepositoryListener aListener) {
    }

    @Override
    public void startup(int persistMechanismVersion) {
        threadManager.startup();
    }

    @Override
    public void debugDistribution() {
        for (Unit unit : units.values()){
            System.err.println("UNIT "+unit.getName());
            unit.debugDistribution();
        }
    }

    static private class RemovedPersistent implements Persistent {
    }

    private static class UnitFilter implements RepositoryQueue.Filter {

        private int unitId;

        public UnitFilter(int unitId) {
            this.unitId = unitId;
        }

        @Override
        public boolean accept(Key key, Persistent value) {
            return key.getUnitId() == unitId;
        }
    }

    private static class AllFilter implements RepositoryQueue.Filter {

        @Override
        public boolean accept(Key key, Persistent value) {
            return true;
        }
    }

    private static class MaintenanceComparator implements Comparator<Unit>, Serializable {

        private static final long serialVersionUID = 7249069246763182397L;

        @Override
        public int compare(Unit o1, Unit o2) {
            return getMaintenanceWeight(o2) - getMaintenanceWeight(o1);
        }
    }

    private static int getMaintenanceWeight(Unit unit) {
        try {
            return unit.getMaintenanceWeight();
        } catch (IOException ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unit.getId(), unit.getName(), new RepositoryException(ex));
        }
        return 0;
    }

    private CharSequence getUnitNameSafe(Key key) {
        return getTranslation().getUnitNameSafe(key.getUnitId());
    }

    private static final class NamedLock {
        private final String name;

        public NamedLock(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NamedLock other = (NamedLock) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }
}

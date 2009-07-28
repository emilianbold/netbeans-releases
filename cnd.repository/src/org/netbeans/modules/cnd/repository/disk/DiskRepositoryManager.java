/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.api.RepositoryTranslation;
import org.netbeans.modules.cnd.repository.queue.RepositoryQueue;
import org.netbeans.modules.cnd.repository.queue.RepositoryThreadManager;
import org.netbeans.modules.cnd.repository.queue.RepositoryWriter;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.repository.translator.RepositoryTranslatorImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Sergey Grinev
 */
public final class DiskRepositoryManager implements Repository, RepositoryWriter {

    private final Map<Integer, Unit> units;
    private final RepositoryQueue queue;
    private final RepositoryThreadManager threadManager;
    private final Persistent removedObject;
    private final ReadWriteLock queueLock;
    private final Map<Integer, Object> unitLocks = new HashMap<Integer, Object>();
    private static final class UnitLock {}
    private final Object mainUnitLock = new UnitLock();

    public DiskRepositoryManager() {
        removedObject = new RemovedPersistent();
        queueLock = new ReentrantReadWriteLock(true);
        threadManager = new RepositoryThreadManager(this, queueLock);
        queue = threadManager.startup();
        units = new ConcurrentHashMap<Integer, Unit>();
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
        return getCreateUnit(key.getUnitId(), key.getUnit().toString());
    }

    /** Never returns null - throws exceptions */
    private Unit getCreateUnit(int unitId, String unitName) throws IOException {
        assert unitName != null;

        Unit unit = units.get(unitId);

        if (unit == null) {
            unit = null;
            synchronized (getUnitLock(unitId)) {
                unit = units.get(unitId);
                if (unit == null) {
                    if (RepositoryListenersManager.getInstance().fireUnitOpenedEvent(unitName)) {
                        RepositoryTranslatorImpl.loadUnitIndex(unitName);
                        unit = new UnitImpl(unitName);
                        units.put(unitId, unit);
                    }
                }
            }
        }

        return unit;
    }

    public void put(Key key, Persistent obj) {
        try {
            getCreateUnit(key).putToCache(key, obj);
            queue.addLast(key, obj);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

    public void hang(Key key, Persistent obj) {
        try {
            getCreateUnit(key).hang(key, obj);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

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
                    getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

    public Persistent get(Key key) {
        try {
            return getCreateUnit(key).get(key);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    getUnitNameSafe(key), new RepositoryException(ex));
        }
        return null;
    }

    public Persistent tryGet(Key key) {
        try {
            return getCreateUnit(key).tryGet(key);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    getUnitNameSafe(key), new RepositoryException(ex));
        }
        return null;
    }

    public void remove(Key key) {
        try {
            getCreateUnit(key).removeFromCache(key);
            queue.addLast(key, removedObject);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    getUnitNameSafe(key), new RepositoryException(ex));
        }
    }

    public void shutdown() {
        if (threadManager != null) {
            threadManager.shutdown();
        }
        List<Entry<Integer, Unit>> entries = new ArrayList<Entry<Integer, Unit>>(units.entrySet());
        for (Entry<Integer, Unit> entry : entries) {
            // iz #146241 IllegalStateException in the case revious session terminated with ^C in console
            // if there are projects that aren't yet closed => the data might be corrupted! => clean untit
            closeUnit(RepositoryAccessor.getTranslator().getUnitName(entry.getKey()), true, null);
        }

        try {
            queueLock.writeLock().lock();
            cleanAndWriteQueue();
            units.clear();
        } finally {
            queueLock.writeLock().unlock();
        }
        RepositoryTranslatorImpl.shutdown();
    }

    public boolean maintenance(long timeout) {
        if (units.size() == 0) {
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
                        unitList[i].getName(), new RepositoryException(ex));
            }
            timeout -= (System.currentTimeMillis() - start);
        }
        return needMoreTime;
    }

    public void openUnit(int unitId, String unitName) {
        try {
            synchronized (getUnitLock(unitId)) {
                getCreateUnit(unitId, unitName);
            }
        } catch (Throwable exc) {
            RepositoryListenersManager.getInstance().fireAnException(unitName,
                    new RepositoryException(exc));
        }
    }

    public void closeUnit(String unitName, boolean cleanRepository, Set<String> requiredUnits) {
        int unitId = RepositoryAccessor.getTranslator().getUnitId(unitName);
        synchronized (getUnitLock(unitId)) {
            closeUnit2(unitName, cleanRepository, requiredUnits);
        }
    }

    private void closeUnit2(final String unitName, final boolean cleanRepository, Set<String> requiredUnits) {

        try {
            queueLock.writeLock().lock();

            Collection<RepositoryQueue.Entry<Key, Persistent>> removedEntries = queue.clearQueue(new UnitFilter(unitName));
            if (!cleanRepository) {
                for (RepositoryQueue.Entry<Key, Persistent> entry : removedEntries) {
                    write(entry.getKey(), entry.getValue());
                }
            }

            int unitId = RepositoryAccessor.getTranslator().getUnitId(unitName);
            Unit unit = units.remove(unitId);

            if (unit != null) {
                try {
                    unit.close();
                } catch (Throwable exc) {
                    RepositoryListenersManager.getInstance().fireAnException(unitName,
                            new RepositoryException(exc));
                }
            }

        } finally {
            queueLock.writeLock().unlock();
        }

        //clean the repository cach files here if it is necessary
        //
        StorageAllocator allocator = StorageAllocator.getInstance();
        if (cleanRepository) {
            allocator.deleteUnitFiles(unitName, true);
        }
        allocator.closeUnit(unitName);

        RepositoryTranslatorImpl.closeUnit(unitName, requiredUnits);
        RepositoryListenersManager.getInstance().fireUnitClosedEvent(unitName);
    }

    public void removeUnit(String unitName) {
        int unitId = RepositoryAccessor.getTranslator().getUnitId(unitName);
        synchronized (getUnitLock(unitId)) {
            closeUnit(unitName, true, Collections.<String>emptySet());
            RepositoryTranslatorImpl.removeUnit(unitName);
        }
    }

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

    public void cleanCaches() {
        StorageAllocator.getInstance().cleanRepositoryCaches();
    }

    public void registerRepositoryListener(final RepositoryListener aListener) {
    }

    public void unregisterRepositoryListener(final RepositoryListener aListener) {
    }

    public void startup(int persistMechanismVersion) {
    }

    public void debugDistribution() {
        for (Unit unit : units.values()){
            System.err.println("UNIT "+unit.getName());
            unit.debugDistribution();
        }
    }

    static private class RemovedPersistent implements Persistent {
    }

    private static class UnitFilter implements RepositoryQueue.Filter {

        private String unitName;

        public UnitFilter(String unitName) {
            this.unitName = unitName;
        }

        public boolean accept(Key key, Persistent value) {
            return key.getUnit().equals(unitName);
        }
    }

    private static class AllFilter implements RepositoryQueue.Filter {

        public boolean accept(Key key, Persistent value) {
            return true;
        }
    }

    private static class MaintenanceComparator implements Comparator<Unit>, Serializable {

        private static final long serialVersionUID = 7249069246763182397L;

        public int compare(Unit o1, Unit o2) {
            return getMaintenanceWeight(o2) - getMaintenanceWeight(o1);
        }
    }

    private static int getMaintenanceWeight(Unit unit) {
        try {
            return unit.getMaintenanceWeight();
        } catch (IOException ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unit.getName(), new RepositoryException(ex));
        }
        return 0;
    }

    private String getUnitNameSafe(Key key) {
        RepositoryTranslation translator = RepositoryAccessor.getTranslator();
        return translator.getUnitNameSafe(key.getUnitId());
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

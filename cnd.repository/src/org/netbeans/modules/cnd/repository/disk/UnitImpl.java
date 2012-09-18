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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.repository.api.DatabaseTable;
import org.netbeans.modules.cnd.repository.impl.BaseRepository;
import org.netbeans.modules.cnd.repository.sfs.FileStorage;
import org.netbeans.modules.cnd.repository.spi.DatabaseStorage;
import org.netbeans.modules.cnd.repository.spi.DatabaseStorage.Provider;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.util.Pair;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;

/**
 * Implements a repository unit
 * @author Nickolay Dalmatov
 * @author Vladimir Kvashin
 */
public final class UnitImpl implements Unit {
    
    private Storage    singleFileStorage;
    private Storage    multyFileStorage;
    private final Collection<DatabaseStorage> dbStorages = new ArrayList<DatabaseStorage>();
    private final CharSequence unitName;
    private final MemoryCache cache;
    private final BaseRepository repository;
    private final int id;
    
    public UnitImpl(int id, final CharSequence unitName, BaseRepository repository) throws IOException {
        this.id = id;
        assert unitName != null;
        this.unitName = unitName;
        assert repository != null;
        this.repository = repository;
        File homeDir = new File(repository.getStorageAllocator().getUnitStorageName(unitName));
        singleFileStorage = FileStorage.create(homeDir, repository);
        multyFileStorage = new MultyFileStorage(repository.getFilesAccessStrategy(), getName());
        Collection<? extends Provider> providers = Lookup.getDefault().lookupAll(DatabaseStorage.Provider.class);
        
        for (Provider provider : providers) {
            DatabaseStorage storage = provider.create(id, homeDir);
            if (storage != null) {
                dbStorages.add(storage);
            }
        }
        cache = new MemoryCache();
    }

    @Override
    public int getId() {
        return id;
    }

    private Storage getDiskStorage(Key key) {
        assert key != null;
        assert getName().equals(key.getUnit());
        if (key.getBehavior() == Key.Behavior.Default) {
            return singleFileStorage;
        } else {
            return multyFileStorage;
        }
    }

    DatabaseTable getDatabaseTable(String storageID) {
        for (DatabaseStorage dbStorage : dbStorages) {
            DatabaseTable table = dbStorage.getTable(storageID);
            if (table != null) {
                return table;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "UnitImpl{" + id + ' ' + unitName + '}'; // NOI18N
    }

    @Override
    public Persistent get(Key key) throws IOException {
        assert key != null;
        // I commented a next assertion because it is too expensive.
        // Use another way to control the assertion. For example by flag in unit tests.
        //assert getName().equals(key.getUnit().toString());
        Persistent data = cache.get(key);
        if (data == MemoryCache.REMOVED) {
            return null;
        }
        if (data == null) {
            data = getDiskStorage(key).read(key);
            if (data != null) {
                // no syncronization here!!!
                // the only possible collision here is lost of element, which is currently being deleted
                // by processQueue - it will be reread

                // we checked that nothing in cache above
                // but if someone already put object before us => reuse it
                Persistent prev = cache.putIfAbsent(key, data);
                if (prev != null) {
                    data = prev;
                }
            }
        }
        return data;
    }
    
    @Override
    public void putToCache(Key key, Persistent obj) {
        assert key != null;
        assert getName().equals(key.getUnit());
        cache.put(key, obj);
    }
    
    @Override
    public void hang(Key key, Persistent obj) {
        assert key != null;
        assert getName().equals(key.getUnit());
        cache.hang(key, obj);
    }
    
    @Override
    public Persistent tryGet(Key key) {
        assert key != null;
        assert getName().equals(key.getUnit());
        Persistent obj = cache.get(key);
        if (obj == MemoryCache.REMOVED) {
            obj = null;
        }
        return obj;
    }

    @Override
    public void removePhysically(Key key) throws IOException {
        assert key != null;
        assert getName().equals(key.getUnit());
        getDiskStorage(key).remove(key);
        cache.removePhysically(key);
    }
    
    @Override
    public void removeFromCache(Key key) {
        assert key != null;
        assert getName().equals(key.getUnit());
        cache.markRemoved(key);
    }

    @Override
    public void close() throws IOException {
        Collection<Pair<Key, Persistent>> hung = cache.clearHungObjects();
        for( Pair<Key, Persistent> pair : hung ) {
            putPhysically(pair.first, pair.second);
        }
        singleFileStorage.close();
        multyFileStorage.close();
        for (DatabaseStorage dbStorage : dbStorages) {
            dbStorage.close();
        }
    }

    @Override
    public void putPhysically(Key key, Persistent object) throws IOException {
        assert key != null;
        assert getName().equals(key.getUnit());
        assert object != null;
        getDiskStorage(key).write(key, object);
    }

    @Override
    public boolean maintenance(long timeout) throws IOException {
        return singleFileStorage.defragment(timeout);
    }

    @Override
    public int getMaintenanceWeight() throws IOException {
        return singleFileStorage.getFragmentationPercentage();
    }

    @Override
    public CharSequence getName() {
        return unitName;
    }

    @Override
    public void debugClear() {
        cache.clearSoftRefs();
    }

    @Override
    public void debugDistribution() {
        cache.printDistribution();
    }

    private static void traceKey(String msg, Key key) {
        if (key.getDepth() == 3 && 
                ("argc".contentEquals(key.getAt(2)) || // NOI18N
                "main".contentEquals(key.getAt(2)))) { // NOI18N
            System.err.println(msg + " at @" + System.identityHashCode(key) + " " + key); // NOI18N
        }
    }
    private static final boolean TRACE_ARGS = CndUtils.getBoolean("cnd.repository.trace.args", false); //NOI18N;
}

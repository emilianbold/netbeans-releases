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
package org.netbeans.modules.cnd.repository.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.api.DatabaseTable;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.disk.DiskRepositoryManager;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.translator.RepositoryTranslatorImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Utilities;

/**
 *
 * @author Vladimir Voskresensky
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.repository.api.Repository.class)
public final class DelegateRepository implements Repository {    

    /** guards cacheToDelegate and delegates *modification* */
    private final Object delegatesLock = new Object();    

    /** read access - without lock, write access guarded by delegatesLock */
    private final ArrayList<BaseRepository> delegates;
    
    /** guarded by delegatesLock */
    private final Map<CacheLocation, BaseRepository> cacheToDelegate = new HashMap<CacheLocation, BaseRepository>();
    
    private int persistMechanismVersion = -1;

    public DelegateRepository() {
        delegates = new ArrayList<BaseRepository>();        
        delegates.add(new DummyRepository(0));
    }

    @Override
    public void hang(Key key, Persistent obj) {
        getDelegate(key.getUnitId()).hang(key, obj);
    }

    @Override
    public void put(Key key, Persistent obj) {
        getDelegate(key.getUnitId()).put(key, obj);
    }

    @Override
    public Persistent get(Key key) {
        Repository delegate = getDelegate(key.getUnitId());
        Persistent result = delegate.get(key);
        if (result == null && Stats.useNullWorkaround) {
            String keyClassName = key.getClass().getName();
            // repository is often asked for projects when theis persistence just does not exist
            if (!keyClassName.endsWith(".ProjectKey") && !keyClassName.endsWith(".OffsetableDeclarationKey")) { // NOI18N
                System.err.printf("NULL returned for key %s on attempt 1\n", key);
                result = delegate.get(key);
                System.err.printf("%s value returned for key %s on attempt 2\n", (result == null) ? "NULL" : "NON-NULL", key);
            }
        }
        return result;
    }

    @Override
    public Persistent tryGet(Key key) {
        return getDelegate(key.getUnitId()).tryGet(key);
    }

    @Override
    public void remove(Key key) {
        getDelegate(key.getUnitId()).remove(key);
    }

    @Override
    public void debugClear() {
        synchronized (delegatesLock) {
            for (Repository delegate : getDelegates()) {
                delegate.debugClear();
            }
            //delegates.clear();
        }
    }

    @Override
    public void shutdown() {
        for (Repository aDelegate : getDelegates()) {
            aDelegate.shutdown();
        }
    }

    @Override
    public void openUnit(int unitId, CharSequence unitName) {
        getDelegate(unitId).openUnit(unitId, unitName);
    }

    @Override
    public void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits) {
        getDelegate(unitId).closeUnit(unitId, cleanRepository, requiredUnits);
    }

    @Override
    public void removeUnit(int unitId) {
        getDelegate(unitId).removeUnit(unitId);
    }

    @Override
    public void cleanCaches() {
        for (Repository delegate : getDelegates()) {
            delegate.cleanCaches();
        }
    }

    @Override
    public void registerRepositoryListener(RepositoryListener aListener) {
        RepositoryListenersManager.getInstance().registerListener(aListener);
    }

    @Override
    public void unregisterRepositoryListener(RepositoryListener aListener) {
        RepositoryListenersManager.getInstance().unregisterListener(aListener);
    }

    @Override
    public void startup(int persistMechanismVersion) {
        this.persistMechanismVersion = persistMechanismVersion;
        for (BaseRepository delegate : getDelegates()) {
            delegate.startup(persistMechanismVersion);
        }
    }

    private BaseRepository createRepository(int id, CacheLocation cacheLocation) {
        BaseRepository delegate;
        if (CndUtils.getBoolean("cnd.repository.validate.keys", false)) {
            Stats.log("Testing keys using KeyValidatorRepository."); // NOI18N
            delegate = new KeyValidatorRepository(id, cacheLocation);
        } else if (CndUtils.getBoolean("cnd.repository.hardrefs", false)) { // NOI18N
            Stats.log("Using HashMapRepository."); // NOI18N
            delegate = new HashMapRepository(id, cacheLocation);
        } else {
            Stats.log("by default using HybridRepository."); // NOI18N
            delegate = new DiskRepositoryManager(id, cacheLocation);
        }
        delegate.getTranslation().startup(persistMechanismVersion, delegate);
        delegate.startup(persistMechanismVersion);
        return delegate;
    }

    @Override
    public void debugDistribution() {
        for (Repository delegate : getDelegates()) {
            delegate.debugDistribution();
        }
    }

    @Override
    public DatabaseTable getDatabaseTable(Key unitKey, String tableID) {
        return getDelegate(unitKey.getUnitId()).getDatabaseTable(unitKey, tableID);
    }

    private BaseRepository getDelegate(int unitId) {
        int repoId = unitId / BaseRepository.REPO_DENOM;
        boolean assertions = false;
        assert (assertions = true);
        if (assertions && repoId >= delegates.size()) {
	    throw new IndexOutOfBoundsException("Index: "+repoId+", Size: "+delegates.size()); //NOI18N
        }
        return delegates.get(repoId);
    }

    public Iterable<BaseRepository> testGetDelegates() {
        return getDelegates();
    }

    private Iterable<BaseRepository> getDelegates() {
        synchronized (delegatesLock) {
            return new ArrayList<BaseRepository>(delegates);
        }
    }

    public RepositoryTranslatorImpl getTranslatorImpl(int unitId) {
        return getDelegate(unitId).getTranslation();
    }

    public int getUnitId(CharSequence unitName, CacheLocation cacheLocation) {
        CndUtils.assertNotNull(cacheLocation, "null cache location"); //NOI18N
        if (cacheLocation == null) {
            cacheLocation = CacheLocation.DEFAULT;
        }
        assert cacheLocation != null;
        synchronized (delegatesLock) {
            BaseRepository repo = cacheToDelegate.get(cacheLocation);
            if (repo == null) {
                int newId = delegates.size();
                repo = createRepository(newId, cacheLocation);
                cacheToDelegate.put(cacheLocation, repo);
                delegates.add(repo);
            }
            int unitId = repo.getTranslation().getUnitId(unitName);
            return unitId;
        }        
    }

    public CacheLocation getCacheLocation(int unitId) {
        return getDelegate(unitId).getCacheLocation();
    }
    
    private static class DummyRepository extends BaseRepository {

        private static final String exceptionText = "DummyRepository should never be accessed"; //NOI18N

        public DummyRepository(int id) {
            super(id, new CacheLocation(new File(Utilities.isWindows() ? "nul" : "/dev/null"))); //NOI18N;
        }

        @Override
        public void hang(Key key, Persistent obj) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public void put(Key key, Persistent obj) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public Persistent get(Key key) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public Persistent tryGet(Key key) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public void remove(Key key) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public void debugClear() {
        }

        @Override
        public void debugDistribution() {
        }

        @Override
        public void startup(int persistMechanismVersion) {
        }

        @Override
        public void shutdown() {
        }

        @Override
        public void openUnit(int unitId, CharSequence unitName) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public void removeUnit(int unitId) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public void cleanCaches() {
        }

        @Override
        public void registerRepositoryListener(RepositoryListener aListener) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public void unregisterRepositoryListener(RepositoryListener aListener) {
            throw new IllegalArgumentException(exceptionText);
        }

        @Override
        public DatabaseTable getDatabaseTable(Key unitKey, String tableID) {
            throw new IllegalArgumentException(exceptionText);
        }        
    }    
}

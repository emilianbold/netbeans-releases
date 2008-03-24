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

package org.netbeans.modules.cnd.repository.impl;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.disk.DiskRepositoryManager;
import org.netbeans.modules.cnd.repository.disk.MemoryCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.translator.RepositoryTranslatorImpl;
import org.netbeans.modules.cnd.repository.util.Filter;
import org.netbeans.modules.cnd.repository.util.Pair;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Nickolay Dalmatov
 */
public class HybridRepository implements Repository {

    MemoryCache cache;
    private final Repository diskRepository;
    
    /** Creates a new instance of HybridRepository */
    public HybridRepository() {
        cache = new MemoryCache();
        diskRepository = DiskRepositoryManager.getInstance();
    }

    public void hang(Key key, Persistent obj) {
        cache.hang(key, obj);
    }

    public void put(Key key, Persistent obj) {
        cache.put(key, obj, true);
        if (key.getPersistentFactory().canWrite(obj)) {
            diskRepository.put(key, obj);
        }
    }

    public final Persistent tryGet(Key key) {
        return cache.get(key);
    }

    public Persistent get(Key key) {
        Persistent data = cache.get(key);
        if (data == null) {
            data = diskRepository.get(key);
            if (data != null) {
                // no syncronization here!!!
                // the only possible collision here is lost of element, which is currently being deleted
                // by processQueue - it will be reread
                cache.put(key, data, false);
            }
        }
        return data;
    }

    public void remove(Key key) {
        cache.remove(key);
        diskRepository.remove(key);
    }

    public void debugClear() {
        cache.clearSoftRefs();
        diskRepository.debugClear();
    }

    public void shutdown() {
        diskRepository.shutdown();
        RepositoryTranslatorImpl.shutdown();
        if( Stats.memoryCacheHitStatistics ) {
            cache.printStatistics();
        }
    }
    

    private void cleanWriteHungObjects(final String unitName) {
        Collection<Pair<Key, Persistent>> hung = cache.clearHungObjects(new Filter<Key>() {
            public boolean accept(Key key) {
                return ((unitName == null) || ((unitName != null) && unitName.equals(key.getUnit())));
            }
            
        });
        for( Pair<Key, Persistent> pair : hung ) {
            Key key = pair.first;
            Persistent obj = pair.second;
            if (key.getPersistentFactory().canWrite(obj)) {
                diskRepository.put(key, obj);
            }
        }
    }

    public void openUnit(String unitName) {
        diskRepository.openUnit(unitName);
    }

    public void closeUnit(String unitName, boolean cleanRepository, Set<String> requiredUnits) {
        cleanWriteHungObjects(unitName);
        diskRepository.closeUnit(unitName, cleanRepository, null);
        RepositoryTranslatorImpl.closeUnit(unitName, requiredUnits);
        RepositoryListenersManager.getInstance().fireUnitClosedEvent(unitName);
    }

    public void removeUnit(String unitName) {
        RepositoryTranslatorImpl.removeUnit(unitName);
        diskRepository.removeUnit(unitName);
    }

    public void cleanCaches() {
        diskRepository.cleanCaches();
    }

    public void registerRepositoryListener(final RepositoryListener aListener) {
    }

    public void unregisterRepositoryListener(final RepositoryListener aListener) {
    }


    public void startup(int persistMechanismVersion) {
        RepositoryTranslatorImpl.startup(persistMechanismVersion);
    }
}
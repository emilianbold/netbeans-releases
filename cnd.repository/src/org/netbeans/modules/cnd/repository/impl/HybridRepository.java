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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.disk.DiskRepositoryManager;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.repository.translator.RepositoryTranslatorImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Nickolay Dalmatov
 */
public class HybridRepository implements Repository {

    private final Map<Key, Object> cache;
    private final Repository diskRepository;
    private Lock refQueueLock;
    private ReferenceQueue refQueue;

    private static final int DEFAULT_CACHE_CAPACITY = 77165;
    
    /** Creates a new instance of HybridRepository */
    public HybridRepository() {
        cache = new ConcurrentHashMap<Key, Object>(DEFAULT_CACHE_CAPACITY);
        diskRepository = DiskRepositoryManager.getInstance();
        refQueueLock = new ReentrantLock();
        refQueue = new ReferenceQueue();
    }

    public void hang(Key key, Persistent obj) {
        cache.put(key, obj);
    }

    public void put(Key key, Persistent obj) {
        cache.put(key, new SoftValue(obj, key, refQueue));

        if (key.getPersistentFactory().canWrite(obj)) {
            diskRepository.put(key, obj);
        }
    }

    public final Persistent tryGet(Key key) {
        Object value = cache.get(key);
        if (value instanceof Persistent) {
            return (Persistent) value;
        } else if (value instanceof SoftReference) {
            return ((SoftReference<Persistent>) value).get();
        }
        return null;
    }

    public Persistent get(Key key) {
        Persistent data = tryGet(key);
        if (data == null) {
            data = diskRepository.get(key);

            if (data != null) {
                processQueue();

                // no syncronization here!!!
                // the only possible collision here is lost of element, which is currently being deleted
                // by processQueue - it will be reread
                cache.put(key, new SoftValue(data, key, refQueue));
            }
        }

        return data;
    }

    public void remove(Key key) {
        cache.remove(key);
        diskRepository.remove(key);
    }

    public void debugClear() {
        //cleanWriteHungObjects(null, false);
        processQueue();
        Set<Key> keys = new HashSet<Key>(cache.keySet());
        for (Key key : keys) {
            Object value = cache.get(key);

            if (value != null && !(value instanceof Persistent)) {
                cache.remove(key);
            }
        }

        diskRepository.debugClear();
    }

    public void shutdown() {
        diskRepository.shutdown();
        RepositoryTranslatorImpl.shutdown();
    }

    private void cleanWriteHungObjects(String unitName, boolean clean) {
        processQueue();
        Set<Key> keys = new HashSet<Key>(cache.keySet());
        for (Key key : keys) {

            boolean fromUnit = ((unitName == null) || ((unitName != null) && unitName.equals(key.getUnit()))) ? true : false;

            if (fromUnit) {
                Object value = cache.remove(key);

                if (value != null) {
                    if (value instanceof Persistent && !clean) {
                        Persistent obj = (Persistent) value;
                        if (key.getPersistentFactory().canWrite(obj)) {
                            diskRepository.put(key, obj);
                        }
                    }
                }
            }
        }
    }

    public void openUnit(String unitName) {
        diskRepository.openUnit(unitName);
    }

    public void closeUnit(String unitName, boolean cleanRepository, Set<String> requiredUnits) {
        cleanWriteHungObjects(unitName, cleanRepository);
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

    private static class SoftValue extends SoftReference {

        private final Object key;

        private SoftValue(Object k, Object key, ReferenceQueue q) {
            super(k, q);
            this.key = key;
        }
    }

    private void processQueue() {
        if (refQueueLock.tryLock()) {
            try {
                SoftValue sv;
                while ((sv = (SoftValue) refQueue.poll()) != null) {
                    Object value = cache.get(sv.key);
                    // check if the object has already been added by another thread
                    // it is more efficient than blocking puts from the disk
                    if ((value != null) && (value instanceof SoftReference) && (((SoftReference) value).get() == null)) {
                        cache.remove(sv.key);
                    }
                }
            } finally {
                refQueueLock.unlock();
            }
        }
    }

    public void startup(int persistMechanismVersion) {
        RepositoryTranslatorImpl.startup(persistMechanismVersion);
    }
}
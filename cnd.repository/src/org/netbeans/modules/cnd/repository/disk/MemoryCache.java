/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.repository.disk;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.util.Pair;

/**
 * An in-memory cache for storing repository objects 
 * @author Nickolay Dalmatov
 * @author Vladimir Kvashin
 */
public final class MemoryCache {
    private static final boolean STATISTIC = false;
    
    private static class SoftValue extends SoftReference {
        private final Key key;
        private SoftValue(Object k, Key key, ReferenceQueue q) {
            super(k, q);
            this.key = key;
        }
    }
    
    private static final class Slice {
        private final Map<Key, Object> storage = new HashMap<Key, Object>(256);
        private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
        private final Lock w = cacheLock.writeLock();
        private final Lock r = cacheLock.readLock();
    }
    
    private static final int SLICE_SIZE = 19;
    private static final class SlicedMap {
        private final Slice slices[] = new Slice[SLICE_SIZE];
        private SlicedMap(){
            for(int i = 0; i < SLICE_SIZE; i++){
                slices[i] = new Slice();
            }
        }
        private Slice getSilce(Key key){
            int i = key.hashCode()%SLICE_SIZE;
            if (i < 0){
                i+=SLICE_SIZE;
            }
            return slices[i];
        }
        private Slice getSilce(int i){
            return slices[i];
        }
    }
    
    private final SlicedMap cache = new SlicedMap();
    private final Lock refQueueLock;
    private final ReferenceQueue refQueue;
    
    // Cache statistics
    private int readCnt = 0;
    private int readHitCnt = 0;
    
    public MemoryCache() {
        refQueueLock = new ReentrantLock();
        refQueue = new ReferenceQueue();
    }
    
    public void hang(Key key, Persistent obj) {
        Slice s = cache.getSilce(key);
        s.w.lock();
        try {
            s.storage.put(key, obj);
        } finally {
            s.w.unlock();
        }
    }
    
    public void put(Key key, Persistent obj, boolean primary) {
        Slice s = cache.getSilce(key);
        SoftValue value = new SoftValue(obj, key, refQueue);
        s.w.lock();
        try {
            s.storage.put(key, value);
        } finally {
            s.w.unlock();
        }
        if( ! primary ) {
            processQueue();
        }
    }
    
    public Persistent get(Key key) {
        if (STATISTIC) readCnt++;
        Slice s = cache.getSilce(key);
        Object value;
        s.r.lock();
        try{
            value = s.storage.get(key);
        } finally {
            s.r.unlock();
        }
        if (value instanceof Persistent) {
            if (STATISTIC) readHitCnt++;
            return (Persistent) value;
        } else if (value instanceof SoftReference) {
            Persistent result = ((SoftReference<Persistent>) value).get();
            if( STATISTIC && result != null ) {
                readHitCnt++;
            }
            return result;
        }
        return null;
    }
    
    public void remove(Key key) {
        Slice s = cache.getSilce(key);
        s.w.lock();
        try {
            s.storage.remove(key);
        } finally {
            s.w.unlock();
        }
    }
    
    public void clearSoftRefs() {
        //cleanWriteHungObjects(null, false);
        processQueue();
        Set<Key> keys;
        for(int i = 0; i < SLICE_SIZE; i++) {
            Slice s = cache.getSilce(i);
            s.r.lock();
            try{
                keys = new HashSet<Key>(s.storage.keySet());
            } finally {
                s.r.unlock();
            }
            for (Key key : keys) {
                Object value;
                s.w.lock();
                try{
                   value = s.storage.get(key);
                   if (value != null && !(value instanceof Persistent)) {
                       s.storage.remove(key);
                    }
                } finally {
                    s.w.unlock();
                }
            }
        }
    }

    private void processQueue() {
        if (refQueueLock.tryLock()) {
            try {
                SoftValue sv;
                while ((sv = (SoftValue) refQueue.poll()) != null) {
                    Object value;
                    Slice s = cache.getSilce(sv.key);
                    s.w.lock();
                    try{
                        value = s.storage.get(sv.key);
                        // check if the object has already been added by another thread
                        // it is more efficient than blocking puts from the disk
                        if ((value != null) && (value instanceof SoftReference) && (((SoftReference) value).get() == null)) {
                            Object removed = s.storage.remove(sv.key);
                            assert (value == removed);
                        }
                    } finally {
                        s.w.unlock();
                    }
                }
            } finally {
                refQueueLock.unlock();
            }
        }
    }
    
    public Collection<Pair<Key, Persistent>> clearHungObjects(/*Filter<Key> filter*/) {
        processQueue();
        Collection<Pair<Key, Persistent>> result = new ArrayList<Pair<Key, Persistent>>();
        Set<Key> keys;
        for(int i = 0; i < SLICE_SIZE; i++) {
            Slice s = cache.getSilce(i);
            s.r.lock();
            try{
                keys = new HashSet<Key>(s.storage.keySet());
            } finally {
                s.r.unlock();
            }
            for (Key key : keys) {
                Object value;
                s.r.lock();
                try{
                    value = s.storage.get(key);
                } finally {
                    s.r.unlock();
                }
                if (value instanceof Persistent ) {
                    result.add(new Pair(key, (Persistent) value));
                    s.w.lock();
                    try {
                        s.storage.remove(key);
                    } finally {
                        s.w.unlock();
                    }
                }
            }
        }
        return result;
    }
    
    private void printStatistics(String name) {
        int hitPercentage = (readCnt == 0) ? 0 : readHitCnt*100/readCnt;
        System.out.printf("\n\nMemory cache statistics %s: %d reads,  %d hits (%d%%)\n\n", // NOI18N
                name, readCnt, readHitCnt, hitPercentage);
    }
}

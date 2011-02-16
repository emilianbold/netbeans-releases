/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A cache that holds soft references to its values and
 * removes entries when values are collected
 * @author Vladimir Kvashin
 */
public class WeakCache<K, V> {

    private final ConcurrentMap<K, Ref<K, V>> map;
    private final ReferenceQueue<V> referenceQueue;
    private final Lock lock;

    private static class Ref<K, V> extends SoftReference<V> {
        private final K key;
        private Ref(K key, V value, ReferenceQueue<V> referenceQueue) {
            super(value, referenceQueue);
            this.key = key;
        }
    }

    public WeakCache() {
        map = new ConcurrentHashMap<K, Ref<K, V>>();
        referenceQueue = new ReferenceQueue<V>();
        lock = new ReentrantLock();
    }

    public V putIfAbsent(K key, V value) {
        Ref<K, V> newRef = new Ref<K, V>(key, value, referenceQueue);
        Ref<K, V> oldRef = map.putIfAbsent(key, newRef);
        if (oldRef == null) {
            return value;
        } else {
            V oldValue = oldRef.get();
            if (oldValue == null || (oldValue.getClass() != value.getClass())) {
                map.put(key, newRef);
                return value;
            } else {
                return oldValue;
            }
        }
    }

    public void put(K key, V value) {
        Ref<K, V> ref = new Ref<K, V>(key, value, referenceQueue);
        map.put(key, ref);
    }

    public V get(K key) {
        Ref<K, V> ref = map.get(key);
        V result = (ref == null) ? null : ref.get();
        return result;
    }

    public V remove(K key) {
        Ref<K, V> removed = map.remove(key);
        return (removed == null) ? null : removed.get();
    }

    public int size() {
        return map.size();
    }

    public void cleanDeadEntries() {        
        if (lock.tryLock()) {
            try {
                Ref<K, V> ref;
                while ( (ref = (Ref<K, V>) referenceQueue.poll()) != null) {
                    if (ref.key != null) {
                        map.remove(ref.key);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
}

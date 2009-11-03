/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * map for one entry set (only two fields to reduce memory)
 * @author Alexandr Simon
 * @author Vladimir Voskresensky
 */
public final class TinySingletonMap<K, V> implements Map<K, V> {

    private K key;
    private V value;

    public TinySingletonMap() {
    }

    public TinySingletonMap(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
    
    public int size() {
        if (key == null) {
            return 0;
        }
        return 1;
    }

    public boolean isEmpty() {
        if (key == null) {
            return true;
        }
        return false;
    }

    public boolean containsKey(Object aKey) {
        if (key == null) {
            return false;
        }
        return key.equals(aKey);
    }

    public boolean containsValue(Object aValue) {
        return value != null && value.equals(aValue);
    }

    public V get(Object aKey) {
        if (key != null && key.equals(aKey)) {
            return value;
        }
        return null;
    }

    public V put(K aKey, V aMacro) {
        V out = null;
        if (key != null && key.equals(aKey)) {
            out = value;
        }
        key = aKey;
        value = aMacro;
        return out;
    }

    public V remove(Object aKey) {
        if (key == null) {
            return null;
        }
        if (key.equals(aKey)) {
            V res = value;
            key = null;
            value = null;
            return res;
        }
        return null;
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void clear() {
        key = null;
        value = null;
    }

    public Set<K> keySet() {
        if (key == null) {
            return Collections.<K>emptySet();
        } else {
            return Collections.<K>singleton(key);
        }
    }

    public Collection<V> values() {
        if (key == null) {
            return Collections.<V>emptyList();
        } else {
            return Collections.<V>singletonList(value);
        }
    }

    public Set<Entry<K, V>> entrySet() {
        if (key == null) {
            return Collections.<Entry<K, V>>emptySet();
        } else {
            return new Set<Entry<K, V>>() {
                public int size() {
                    return 1;
                }
                public boolean isEmpty() {
                    return false;
                }
                public Iterator<Entry<K, V>> iterator() {
                    return new Iterator<Entry<K, V>>(){
                        private boolean last = false;
                        public boolean hasNext() {
                            return !last;
                        }
                        public Entry<K, V> next() {
                            if (!last) {
                                last = true;
                                return new Entry<K, V>(){
                                    public K getKey() {
                                        return key;
                                    }
                                    public V getValue() {
                                        return value;
                                    }
                                    public V setValue(V value) {
                                        V res = TinySingletonMap.this.value;
                                        TinySingletonMap.this.value = value;
                                        return res;
                                    }
                                };
                            }
                            return null;
                        }
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                        }
                    };
                }
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public boolean add(Entry<K, V> o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public boolean addAll(Collection<? extends Entry<K, V>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
            };
        }
    }
}

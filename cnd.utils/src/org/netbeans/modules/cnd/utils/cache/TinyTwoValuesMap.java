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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * map for one entry set (only four fields to reduce memory) - 24 bytes on 32 system.
 */
final class TinyTwoValuesMap<K, V> implements Map<K, V>, TinyMaps.CompactMap<K, V> {

    private K firstKey;
    private V firstValue;
    private K secondKey;
    private V secondValue;

    public TinyTwoValuesMap() {
    }

    TinyTwoValuesMap(TinySingletonMap<K, V> map) {
        this.firstKey = map.getKey();
        assert firstKey != null;
        this.firstValue = map.getValue();
    }

    public K getFirstKey() {
        return firstKey;
    }

    public V getFirstValue() {
        return firstValue;
    }
    
    public K getSecondKey() {
        return secondKey;
    }

    public V getSecondValue() {
        return secondValue;
    }

    @Override
    public int size() {
        if (firstKey == null) {
            return 0;
        } else if (secondKey == null) {
            return 1;
        }
        return 2;
    }

    @Override
    public boolean isEmpty() {
        if (firstKey == null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean containsKey(Object aKey) {
        if (firstKey == null) {
            return false;
        }
        if (firstKey.equals(aKey)) {
            return true;
        }

        if (secondKey == null) {
            return false;
        }
        return secondKey.equals(aKey);
    }

    @Override
    public boolean containsValue(Object aValue) {
        return (firstValue != null && firstValue.equals(aValue)) ||
               (secondValue != null && secondValue.equals(aValue)) ;
    }

    @Override
    public V get(Object aKey) {
        if (firstKey != null && firstKey.equals(aKey)) {
            return firstValue;
        } else if (secondKey != null && secondKey.equals(aKey)) {
            return secondValue;
        }
        return null;
    }

    @Override
    public V put(K aKey, V aValue) {
        assert aKey != null;
        if (firstKey == null) {
            assert secondKey == null : "first key is always filled the first";
            firstKey = aKey;
            firstValue = aValue;
            return null;
        }
        if (firstKey.equals(aKey)) {
            V out = firstValue;
            firstValue = aValue;
            return out;
        }
        if (secondKey == null) {
            secondKey = aKey;
            secondValue = aValue;
            return null;
        }
        if (secondKey.equals(aKey)) {
            V out = secondValue;
            secondValue = aValue;
            return out;
        }
        assert false : "this map can not contain more than two elements";
        return null;
    }

    @Override
    public V remove(Object aKey) {
        if (firstKey == null) {
            return null;
        }
        if (firstKey.equals(aKey)) {
            V res = firstValue;
            // shift the second element in the beginning
            firstKey = secondKey;
            firstValue = secondValue;
            secondKey = null;
            secondValue = null;
            return res;
        }
        if (secondKey == null) {
            return null;
        }
        if (secondKey.equals(aKey)) {
            V res = secondValue;
            // shift the second element in the beginning
            secondKey = null;
            secondValue = null;
            return res;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void clear() {
        firstKey = null;
        firstValue = null;
        secondKey = null;
        secondValue = null;
    }

    @Override
    public Set<K> keySet() {
        if (firstKey == null) {
            return Collections.<K>emptySet();
        } else if (secondKey == null) {
            return Collections.<K>singleton(firstKey);
        } else {
            return new Set<K>() {
                @Override
                public int size() {
                    return 2;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public boolean contains(Object o) {
                    return firstKey.equals(o) || secondKey.equals(o);
                }

                @Override
                public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        private byte index = 0;
                        @Override
                        public boolean hasNext() {
                            return index < 2;
                        }

                        @Override
                        public K next() {
                            if (index < 2) {
                                if (++index == 1) {
                                    return firstKey;
                                } else {
                                    return secondKey;
                                }
                            }
                            throw new NoSuchElementException();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                        }
                    };
                }

                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public boolean add(K e) {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public boolean addAll(Collection<? extends K> c) {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }

                @Override
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet.");// NOI18N
                }
            };
        }
    }

    @Override
    public Collection<V> values() {
        if (firstKey == null) {
            return Collections.<V>emptyList();
        } else if (secondKey == null) {
            return Collections.<V>singleton(firstValue);
        } else {
            @SuppressWarnings("unchecked")
            List<V> asList = Arrays.asList(firstValue, secondValue);
            return asList;
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (firstKey == null) {
            return Collections.<Entry<K, V>>emptySet();
        } else {
            return new Set<Entry<K, V>>() {
                final int size = TinyTwoValuesMap.this.size();
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return false;
                }
                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return new Iterator<Entry<K, V>>(){
                        private byte index = 0;
                        @Override
                        public boolean hasNext() {
                            return index < size;
                        }
                        @Override
                        public Entry<K, V> next() {
                            if (index < size) {
                                final int entryIndex = index;
                                index++;
                                return new Entry<K, V>(){
                                    @Override
                                    public K getKey() {
                                        return entryIndex == 0 ? firstKey : secondKey;
                                    }
                                    @Override
                                    public V getValue() {
                                        return entryIndex == 0 ? firstValue : secondValue;
                                    }
                                    @Override
                                    public V setValue(V value) {
                                        V res;
                                        if (entryIndex == 0) {
                                            res = TinyTwoValuesMap.this.firstValue;
                                            TinyTwoValuesMap.this.firstValue = value;
                                        } else {
                                            res = TinyTwoValuesMap.this.secondValue;
                                            TinyTwoValuesMap.this.secondValue = value;
                                        }
                                        return res;
                                    }
                                };
                            }
                            throw new NoSuchElementException();
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                        }
                    };
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean add(Entry<K, V> o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean addAll(Collection<? extends Entry<K, V>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
            };
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
        if (firstKey == null || firstKey.equals(newElem)) {
            return this;
        } else if (secondKey == null || secondKey.equals(newElem)) {
            return this;
        }
        return new TinyMaps.TinyMap4<K, V>(this);
    }
}

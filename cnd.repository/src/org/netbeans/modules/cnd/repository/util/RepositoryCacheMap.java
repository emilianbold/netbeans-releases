/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Nickolay Dalmatov
 */

public class RepositoryCacheMap<K,V> extends TreeMap<K,V> {
    
    public class CacheEntry<K,V> implements Map.Entry<K,V> {
        private K key;
        private V value;
        
        CacheEntry(K key, V value) {
            this.key   = key;
            this.value = value;
            
        }
        
        public K getKey() {
            return this.key;
        }
        
        public V getValue() {
            return this.value;
        }
        
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
    }
    
    
    
    public class RepositoryCacheValue<V>  implements Comparable{
        
        public AtomicInteger       frequency;
        public V                   value;
        
        RepositoryCacheValue(V value) {
            frequency = new AtomicInteger(1);
            this.value = value;
        }
        
        public int compareTo(Object o) {
            RepositoryCacheValue<V> elemToCompare = (RepositoryCacheValue<V>) o;
            int ownValue = frequency.intValue();
            int objValue = elemToCompare.frequency.intValue();
            
            if (ownValue < objValue)
                return -1;
            else if (ownValue == objValue)
                return 0;
            else
                return 1;
        }
        
    }
    
    private TreeMap<K, RepositoryCacheValue<V>>   storage;
    private AtomicInteger                         capacity;
    private ReentrantReadWriteLock                readWriteLock;
    static public int                             defaultCapacity  = 20;
    
    /**
     * Creates a new instance of RepositoryCacheMap
     */
    public RepositoryCacheMap(int capacity) {
        readWriteLock   = new ReentrantReadWriteLock(true);
        
        try {
            readWriteLock.writeLock().lock();
            storage         = new TreeMap<K, RepositoryCacheValue<V>>();
            this.capacity   = new AtomicInteger((capacity >0)?capacity:defaultCapacity);
            
        }  catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        
    }
    
    public int size() {
        int size = 0;
        
        try {
            readWriteLock.readLock().lock();
            size = storage.size();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return size;
    }
    
    public boolean isEmpty() {
        boolean isEmpty = true;
        
        try {
            readWriteLock.readLock().lock();
            isEmpty = storage.isEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return isEmpty;
    }
    
    public boolean containsKey(Object key) {
        boolean contKey = false;
        
        try {
            readWriteLock.readLock().lock();
            contKey = storage.containsKey(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return contKey;
    }
    
    public boolean containsValue(Object value) {
        return false;
    }
    
    public V get(Object key) {
        V retValue = null;
        
        try {
            readWriteLock.readLock().lock();
            
            RepositoryCacheValue<V> entry = (RepositoryCacheValue<V>)storage.get(key);
            
            if (entry != null) {
                entry.frequency.incrementAndGet();
                retValue= entry.value;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return retValue;
    }
    
    public V put(K key, V value) {
        V retValue = null;
        
        try {
            readWriteLock.writeLock().lock();
            RepositoryCacheValue<V> entry = new RepositoryCacheValue<V> (value);
            
            if (storage.size() < capacity.intValue()) {
                storage.put(key, entry);
            } else {
                Set<Map.Entry<K, RepositoryCacheValue<V>>> aSet = storage.entrySet();
                Iterator<Map.Entry<K, RepositoryCacheValue<V>>> iter = aSet.iterator();
                Map.Entry<K, RepositoryCacheValue<V>> elem = iter.next();
                
                int minFreq = elem.getValue().frequency.intValue();
                K   minKey    = elem.getKey();
                V   minValue  = elem.getValue().value;
                
                while (iter.hasNext()) {
                    
                    if (minFreq == 1)
                        break;
                    
                    elem = iter.next();
                    if (elem.getValue().frequency.intValue() < minFreq){
                        minFreq = elem.getValue().frequency.intValue();
                        minKey  = elem.getKey();
                        minValue  = elem.getValue().value;
                    }
                }
                
                storage.remove(minKey);
                storage.put(key, entry);
                retValue =  minValue;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        
        return retValue;
    }
    
    public V remove(Object key) {
        
        V retValue = null;
        try {
            
            readWriteLock.writeLock().lock();
            RepositoryCacheValue<V> entry = (RepositoryCacheValue<V> )storage.remove(key);
            
            if (entry != null)
                retValue = entry.value;
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        
        return retValue;
    }
    
    public void putAll(Map<? extends K, ? extends V> map) {
        // not supported
    }
    
    public void clear() {
        // storage.clear();
    }
    
    public Set<K> keySet() {
        Set<K> keySet = null;
        
        try {
            readWriteLock.readLock().lock();
            keySet = storage.keySet();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return keySet;
    }
    
    public Collection<V> values() {
        Collection<V>                       newCollection = new ArrayList<V>();
        Collection<RepositoryCacheValue<V>> origCollection;
        Iterator<RepositoryCacheValue<V>> iter;
        
        try {
            readWriteLock.readLock().lock();
            origCollection = storage.values();
            iter = origCollection.iterator();
            
            while ( iter.hasNext()) {
                newCollection.add(iter.next().value);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return newCollection;
    }
    
    public Set<Map.Entry<K,V>> entrySet() {
        TreeSet<Map.Entry<K,V>>                         resultSet = new TreeSet<Map.Entry<K,V>>();
        Set<Map.Entry<K, RepositoryCacheValue<V>>>      aSet;
        Iterator<Map.Entry<K, RepositoryCacheValue<V>>> iter;
        Map.Entry<K, RepositoryCacheValue<V>>           elem;
        
        try {
            readWriteLock.readLock().lock();
            aSet = storage.entrySet();
            iter = aSet.iterator();
            
            while (iter.hasNext()) {
                elem = iter.next();
                resultSet.add(new CacheEntry<K,V> (elem.getKey(), elem.getValue().value));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return resultSet;
    }
    
    public Set<V> adjustCapacity(int newCapacity) {
        
        Set<V>                                          retSet = new HashSet<V>();
        TreeSet<RepositoryCacheValue<V>>                sortedFreqFiles;
        Set<Map.Entry<K, RepositoryCacheValue<V>>>      aSet;
        Iterator<Map.Entry<K, RepositoryCacheValue<V>>> iter;
        int                                             numToRemove;
        Iterator<RepositoryCacheValue<V>>               sortedInter;
        
        newCapacity = (newCapacity >0)?newCapacity:defaultCapacity;
        
        try {
            readWriteLock.writeLock().lock();
            
            if (newCapacity >= capacity.intValue()) {
                capacity.set(newCapacity);
                
            } else if (newCapacity >= storage.size()) {
                capacity.set(newCapacity);
                
            } else {
                sortedFreqFiles = new TreeSet<RepositoryCacheValue<V>> ();
                aSet = storage.entrySet();
                iter = aSet.iterator();
                
                while (iter.hasNext()) {
                    sortedFreqFiles.add( iter.next().getValue() );
                }
                
                numToRemove = storage.size() - newCapacity;
                sortedInter = sortedFreqFiles.iterator();
                
                for (int i=0; i < numToRemove; i++) {
                    if (sortedInter.hasNext())
                        retSet.add(sortedInter.next().value);
                }
                
                capacity.set(newCapacity);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return retSet;
    }
}

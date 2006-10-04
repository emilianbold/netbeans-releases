/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Map implementation that allows to define the class that implements
 * the <code>Map.Entry</code>.
 * <br/>
 * The present implementation does not allow <code>null</code> to be used
 * as a key in the map. The client may use NULL_KEY masking on its side
 * if necessary (see <code>java.util.HashMap</code> impl).
 * <br/>
 * The load factor is fixed to <code>1.0</code>.
 * <br/>
 * The hashing function of the client should be good - there is no additional
 * hashing improvements like e.g. in HashMap.
 * <br/>
 * The iterators produced by this map are not fail-fast - they will continue iteration
 * and their behavior is generally undefined after the modification.
 * The caller should ensure that there will be no pending iterators during modification
 * of this map.
 * <br/>
 * When iterating inside through entries in a bucket the <code>Object.equals()</code>
 * is used for comparison.
 * <br/>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CompactMap<K,V> implements Map<K,V> {
    
    private static int EXPAND_THRESHOLD = 4;

    // Empty array would fail with present impl
    private MapEntry<K,V>[] table;
    
    private int size;

    public CompactMap() {
        table = allocateTableArray(1);
    }

    public CompactMap(int initialCapacity) {
        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }
        table = allocateTableArray(capacity);
    }
    
    public V get(Object key) {
        MapEntry<K,V> e = findEntry(key);
        return (e != null) ? e.getValue() : null;
    }

    public boolean containsKey(Object key) {
        MapEntry<K,V> e = findEntry(key);
        return (e != null);
    }
    
    public boolean containsValue(Object value) {
        for (int i = table.length - 1; i >= 0 ; i--) {
            for (MapEntry<K,V> e = table[i]; e != null; e = e.nextMapEntry()) {
                if ((value == null && e.getValue() == null)
                    || (value != null && value.equals(e.getValue()))
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Put the given entry into the map.
     * <br/>
     * The given entry should only be added to one compact map instance.
     * <br/>
     * Adding a single entry into multiple compact maps will break
     * internal consistency of all the intended maps!
     * <br/>
     * If there will be an existing entry with a key that equals to the key
     * in the entry parameter then the original entry will be replaced
     * by the given entry.
     */
    public MapEntry<K,V> putEntry(MapEntry<K,V> entry) {
        Object key = entry.getKey();
        int hash = key.hashCode();
        hash &= table.length - 1;
        MapEntry<K,V> e = table[hash];
        MapEntry<K,V> prevEntry = null;
        int entryCount = 0;
        while (e != null) {
            if (e == entry) { // Entry already added => do nothing
                return entry;
            }
            if (key.equals(e.getKey())) {
                // Found the entry -> replace it
                if (prevEntry == null) {
                    table[hash] = entry;
                } else {
                    prevEntry.setNextMapEntry(entry);
                }
                entry.setNextMapEntry(e.nextMapEntry());
                e.setNextMapEntry(null);
                return e;
            }
            prevEntry = e;
            e = e.nextMapEntry();
            entryCount++;
        }
        
        // Not found in present table => add the entry
        addEntry(entry, hash, entryCount);
        return null; // nothing replaced
    }

    public V put(K key, V value) {
        int hash = key.hashCode();
        hash &= table.length - 1;
        MapEntry<K,V> e = table[hash];
        int entryCount = 0;
        while (e != null) {
            if (key.equals(e.getKey())) {
                // Found the entry
                V oldValue = e.getValue();
                e.setValue(value);
                return oldValue;
            }
            e = e.nextMapEntry();
            entryCount++;
        }
        
        // Not found in present table => add the entry
        e = new DefaultMapEntry<K,V>(key);
        e.setValue(value);
        addEntry(e, hash, entryCount);
        return null;
    }

    public void putAll(Map<? extends K,? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public V remove(Object key) {
        MapEntry<K,V> e = removeEntryForKey(key);
        return (e != null) ? e.getValue() : null;
    }
    
    /**
     * Remove the given entry from the map.
     * <br/>
     * This method will search for the entry instance (not its key)
     * so the given entry must be physically used in the map
     * otherwise this method will not do anything.
     */
    public MapEntry<K,V> removeEntry(MapEntry<K,V> entry) {
        int hash = entry.getKey().hashCode();
        hash &= table.length - 1;
        MapEntry<K,V> e = table[hash];
        MapEntry<K,V> prev = null;
        while (e != null) {
            if (e == entry) {
                if (prev == null) {
                    table[hash] = e.nextMapEntry();
                } else {
                    prev.setNextMapEntry(e.nextMapEntry());
                }
                entry.setNextMapEntry(null);
                size--;
                return entry;
            }
            prev = entry;
            entry = entry.nextMapEntry();
        }
        return null;
    }

    public void clear() {
        // Retain present table array
        for (int i = table.length - 1; i >= 0; i--) {
            MapEntry<K,V> e = table[i];
            table[i] = null;
            // Unbind entries
            while (e != null) {
                MapEntry<K,V> next = e.nextMapEntry();
                e.setNextMapEntry(null);
                e = next;
            }

        }
    }

    public final int size() {
        return size;
    }

    public boolean isEmpty() {
        return (size() == 0);
    }

    public Set<Entry<K,V>> entrySet() {
        return new EntrySet();
    }
    
    public Collection<V> values() {
        throw new IllegalStateException("Not yet implemented");
    }

    public Set<K> keySet() {
        throw new IllegalStateException("Not yet implemented");
    }

    private MapEntry<K,V> findEntry(Object key) {
        int hash = key.hashCode();
        hash &= table.length - 1;
        MapEntry<K,V> e = table[hash];
        while (e != null) {
            if (key.equals(e.getKey())) {
                return e;
            }
            e = e.nextMapEntry();
        }
        return null;
    }
    
    private void addEntry(MapEntry<K,V> entry, int tableIndex, int entryCount) {
        entry.setNextMapEntry(table[tableIndex]);
        table[tableIndex] = entry;
        size++;
        if (size > table.length) { // Fill factor is 1.0
            MapEntry<K,V>[] newTable = allocateTableArray(Math.max(table.length << 1, 4));
            for (int i = table.length - 1; i >= 0; i--) {
                entry = table[i];
                while (entry != null) {
                    MapEntry<K,V> next = entry.nextMapEntry();
                    int newIndex = entry.getKey().hashCode() & (newTable.length - 1);
                    entry.setNextMapEntry(newTable[newIndex]);
                    newTable[newIndex] = entry;
                    entry = next;
                }
            }
            table = newTable;
        }
    }
    
    private MapEntry<K,V> removeEntryForKey(Object key) {
        int hash = key.hashCode();
        hash &= table.length - 1;
        MapEntry<K,V> e = table[hash];
        MapEntry<K,V> prev = null;
        while (e != null) {
            if (key.equals(e.getKey())) {
                if (prev == null) {
                    table[hash] = e.nextMapEntry();
                } else {
                    prev.setNextMapEntry(e.nextMapEntry());
                }
                e.setNextMapEntry(null);
                size--;
                return e;
            }
            prev = e;
            e = e.nextMapEntry();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private MapEntry<K,V>[] allocateTableArray(int capacity) {
        return new MapEntry[capacity];
    }
    
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("{");

	Iterator i = entrySet().iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
	    Map.Entry e = (Map.Entry)i.next();
	    Object key = e.getKey();
            Object value = e.getValue();
	    if (key == this)
		buf.append("(this Map)");
	    else
		buf.append(key);
	    buf.append("=");
	    if (value == this)
		buf.append("(this Map)");
	    else
		buf.append(value);
            hasNext = i.hasNext();
            if (hasNext)
                buf.append(", ");
        }

	buf.append("}");
	return buf.toString();
    }

    /*
     * Entry of the compact map.
     */
    public interface MapEntry<K,V> extends Map.Entry<K,V> {
        
        /**
         * Get next entry in the entry chain.
         */
        MapEntry<K,V> nextMapEntry();
        
        /**
         * Set the next entry in the entry chain.
         */
        void setNextMapEntry(MapEntry<K,V> nextMapEntry);
    
    }
    
    public static abstract class AbstractMapEntry<K,V> implements MapEntry<K,V> {
        
        private MapEntry<K,V> nextMapEntry; // 12 bytes
        
        public MapEntry<K,V> nextMapEntry() {
            return nextMapEntry;
        }
        
        public void setNextMapEntry(MapEntry<K,V> next) {
            this.nextMapEntry = next;
        }
        
        public final boolean equals(Object o) {
            return equals(this, o);
        }
        
        public final int hashCode() {
            return hashCode(this);
        }
    
        public static boolean equals(Map.Entry entry, Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            Object k1 = entry.getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = entry.getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) 
                    return true;
            }
            return false;
        }
    
        public static final int hashCode(Map.Entry entry) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            // Prevent stack overflow by using identityHashCode()
            return (key == null ? 0 : key.hashCode()) ^
                   (value == null ? 0 : (value != entry) ? value.hashCode() : System.identityHashCode(value));
        }
        
    }
    
    public static class DefaultMapEntry<K,V> extends AbstractMapEntry<K,V> {
        
        private K key; // 16 bytes
        
        private V value; // 20 bytes
        
        public DefaultMapEntry(K key) {
            this.key = key;
        }
        
        public K getKey() {
            return key;
        }
        
        public V getValue() {
            return value;
        }
        
        public V setValue(V value) {
            Object oldValue = this.value;
            this.value = value;
            return value;
        }
        
        public String toString() {
            return "key=" + getKey() + ", value=" + getValue(); // NOI18N
        }
        
    }

    private final class EntrySet extends AbstractSet<Entry<K,V>> {

        public Iterator<Entry<K,V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K,V> e = (Map.Entry<K,V>)o;
            MapEntry<K,V> candidate = findEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(Object o) {
            @SuppressWarnings("unchecked")
            MapEntry<K,V> e = (MapEntry<K,V>)o;
            return removeEntry(e) != null;
        }

        public int size() {
            return CompactMap.this.size();
        }

        public void clear() {
            CompactMap.this.clear();
        }

    }
    
    private abstract class HashIterator {

        MapEntry<K,V> next;       // next entry to return
        int index;                   // current slot
        MapEntry<K,V> current;    // current entry
        
        HashIterator() {
            MapEntry<K,V>[] t = table;
            int i = t.length;
            MapEntry<K,V> n = null;
            if (size != 0) { // advance to first entry
                while (i > 0 && (n = t[--i]) == null)
                    ;
            }
            next = n;
            index = i;
        }
        
        public boolean hasNext() {
            return next != null;
        }
        
        MapEntry<K,V> nextEntry() {
            MapEntry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            
            MapEntry<K,V> n = e.nextMapEntry();
            MapEntry<K,V>[] t = table;
            int i = index;
            while (n == null && i > 0)
                n = t[--i];
            index = i;
            next = n;
            return current = e;
        }
        
        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            Object k = current.getKey();
            current = null;
            removeEntryForKey(k);
        }
        
    }
    
    private final class ValueIterator extends HashIterator implements Iterator<V> {

        public V next() {
            return nextEntry().getValue();
        }
    }
    
    private final class KeyIterator extends HashIterator implements Iterator<K> {

        public K next() {
            return nextEntry().getKey();
        }

    }
    
    private final class EntryIterator extends HashIterator implements Iterator<Entry<K,V>> {

        public Entry<K,V> next() {
            return nextEntry();
        }

    }
    
}

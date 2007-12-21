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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.model.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A concrete class that implements ListMap
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
public class ArrayHashMap
    implements ListMap {

    private List mKeyList;
    private List mValueList;
    private Map mMap;

    /**
     * Constructs an empty <tt>ArrayHashMap</tt> with the default initial
     * capacity (16) and the default load factor (0.75).
     */
    public ArrayHashMap() {

        mKeyList = new ArrayList();
        mValueList = new ArrayList();
        mMap = new HashMap();
    }

    /**
     * Constructs an empty <tt>ArrayHashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor The load factor.
     */
    public ArrayHashMap(int initialCapacity, float loadFactor) {

        mKeyList = new ArrayList(initialCapacity);
        mValueList = new ArrayList(initialCapacity);
        mMap = new HashMap(initialCapacity, loadFactor);
    }

    /**
     * Constructs an <tt>ArrayHashMap</tt> with the elements from the specified
     * array No elements are added if a is null or zero length. If a's length
     * is an odd number then the last element is ignored.
     *
     * @param a The object array of form: key, value, ..., key, value.
     */
    public ArrayHashMap(Object[] a) {

        mKeyList = new ArrayList();
        mValueList = new ArrayList();
        mMap = new HashMap();

        if (a == null) {
            return;
        }

        int size = GenUtil.quotient(a.length, 2);

        for (int i = 0; i < size; i++) {
            Object k = a[2 * i];
            Object v = a[(2 * i) + 1];

            put(i, k, v);
        }
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    /**
     * Returns a list view of the keys contained in this map. Query operations
     * on the returned list "read through" to the key list, and attempts to
     * modify the returned list, whether direct or via its iterator, result in
     * an UnsupportedOperationException
     *
     * @return an unmodifiable list view of the keys contained in this map.
     */
    public List getKeyList() {
        return Collections.unmodifiableList(mKeyList);
    }

    /**
     * Returns a list view of the values contained in this map. Query
     * operations on the returned list "read through" to the key list, and
     * attempts to modify the returned list, whether direct or via its
     * iterator, result in an UnsupportedOperationException
     *
     * @return an unmodifiable list view of the values contained in this map.
     */
    public List getValueList() {
        return Collections.unmodifiableList(mValueList);
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {

        mMap.clear();
        mKeyList.clear();
        mValueList.clear();
    }

    /**
     * Returns a shallow copy of this <tt>ArrayHashMap</tt> instance: the keys
     * and values themselves are not cloned.
     *
     * @return a shallow copy of this map.
     */
    public Object clone() {

        ArrayHashMap result = null;

        try {
            result = (ArrayHashMap) super.clone();
        } catch (CloneNotSupportedException e) {

            // assert false;
        }

        result.mMap = (HashMap) ((HashMap) mMap).clone();
        result.mKeyList = (ArrayList) ((ArrayList) mKeyList).clone();
        result.mValueList = (ArrayList) ((ArrayList) mValueList).clone();

        return result;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key The key whose presence in this map is to be tested
     *
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key.
     */
    public boolean containsKey(Object key) {
        return mMap.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified
     * value.
     *
     * @param value value whose presence in this map is to be tested.
     *
     * @return <tt>true</tt> if this map maps one or more keys to the specified
     *         value.
     */
    public boolean containsValue(Object value) {
        return mMap.containsValue(value);
    }

    /**
     * Returns a collection view of the mappings contained in this map. Query
     * operations on the returned collection "read through" to the mappings
     * collection, and attempts to modify the returned collection, whether
     * direct or via its iterator, result in an UnsupportedOperationException.
     *
     * @return an unmodifiable collection view of the mappings contained in
     *         this map.
     *
     * @see Map.Entry
     */
    public Set entrySet() {
        return Collections.unmodifiableSet(mMap.entrySet());
    }

    /**
     * Returns the value to which the specified key is mapped in this identity
     * hash map, or <tt>null</tt> if the map contains no mapping for this key.
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
     * that the map contains no mapping for the key; it is also possible that
     * the map explicitly maps the key to <tt>null</tt> . The
     * <tt>containsKey</tt> method may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned.
     *
     * @return the value to which this map maps the specified key, or <tt>null
     *         </tt> if the map contains no mapping for this key.
     *
     * @see #put(Object, Object)
     */
    public Object get(Object key) {
        return mMap.get(key);
    }

    public Object get(int index) {
        return mValueList.get(index);
    }
    
    /**
     * Returns a set view of the keys contained in this map. Query operations
     * on the returned set "read through" to the key set, and attempts to
     * modify the returned set, whether direct or via its iterator, result in
     * an UnsupportedOperationException
     *
     * @return an unmodifiable set view of the keys contained in this map.
     */
    public Set keySet() {
        return Collections.unmodifiableSet(mMap.keySet());
    }

    /**
     * Associates the specified value with the specified key in this map, and
     * insert the key into the key list. If the map previously contained a
     * mapping for this key, the old value is replaced, and the position of
     * this key in the key list is changed to index.
     *
     * @param index the index
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     *
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the ArrayHashMap previously associated
     *         <tt>null</tt> with the specified key.
     */
    public Object put(int index, Object key, Object value) {

        if (!mMap.containsKey(key)) {
            mKeyList.add(index, key);
            mValueList.add(index, value);
            mMap.put(key, value);

            return null;
        }

        mKeyList.remove(index);
        mKeyList.add(index, key);
        mValueList.remove(index);
        mValueList.add(index, value);

        return mMap.put(key, value);
    }

    /**
     * Associates the specified value with the specified key in this map. If
     * the map previously contained a mapping for this key, the old value is
     * replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     *
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the ArrayHashMap previously associated
     *         <tt>null</tt> with the specified key.
     */
    public Object put(Object key, Object value) {

        if (!mMap.containsKey(key)) {
            mKeyList.add(key);
            mValueList.add(value);
            mMap.put(key, value);

            return null;
        }

        int index = mKeyList.indexOf(key);

        mKeyList.remove(index);
        mKeyList.add(index, key);
        mValueList.remove(index);
        mValueList.add(index, value);

        return mMap.put(key, value);
    }

    /**
     * Copies all of the mappings from the specified map to this map These
     * mappings will replace any mappings that this map had for any of the
     * keys currently in the specified map.
     *
     * @param t mappings to be stored in this map.
     */
    public void putAll(Map t) {

        for (Iterator i = t.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();

            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the key at the specified position in the key list. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Removes the mapping for the key from this map.
     *
     * @param index the index of the key to be removed.
     *
     * @return previous value associated with the key, or <tt>null</tt> if
     *         there was no mapping for key. A <tt>null</tt> return can also
     *         indicate that the map previously associated <tt>null</tt> with
     *         the specified key.
     */
    public Object remove(int index) {

        Object k = mKeyList.remove(index);

        if (k != null) {
            mValueList.remove(index);

            return mMap.remove(k);
        }

        return null;
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     *
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     */
    public Object remove(Object key) {

        Object v = mMap.remove(key);

        if (v != null) {
            int index = mKeyList.indexOf(key);

            mKeyList.remove(index);
            mValueList.remove(index);
        }

        return v;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return mMap.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @todo Document this method
     */
    public String toString() {
        return mMap.toString();
    }

    /**
     * Returns a collection view of the values contained in this map. Query
     * operations on the returned collection "read through" to the values
     * collection, and attempts to modify the returned collection, whether
     * direct or via its iterator, result in an UnsupportedOperationException.
     *
     * @return an unmodifiable collection view of the values contained in this
     *         map.
     */
    public Collection values() {
        return Collections.unmodifiableCollection(mMap.values());
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/

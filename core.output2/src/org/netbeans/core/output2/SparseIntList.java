/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * SparseIntList.java
 *
 * Created on August 15, 2004, 12:18 AM
 */

package org.netbeans.core.output2;

import java.util.Arrays;

/** 
 * A sparsely populated list of integers, internally implemented as two
 * arrays of integers - one containing sequential indices that have been entered,
 * and one the values associated with those integers.  Calls to get() for values
 * in between or greater than values that have really been added will return 
 * the value of the nearest lower added entry that has been added plus the interval
 * between that entry and the index requested.  So, if you have such a list, 
 * it works as follows:
 * <p>
 * Entries are added with an associated index.  If get() is called for a 
 * value > the last entered index, the value returned is the last entered 
 * index + the difference between the requested index and the last entered
 * index.  When a value is requested that is between recorded indices, it
 * will find the nearest lower recorded index.
 * <p>
 * So, if you call add (20, 10), then get(0)==0, get(9) == 9, get(10) == 10 (no
 * that is not a typo! After adding <i>at</i> 10, get(10) returns 10 - indices
 * <i>after</i> that are what's affected),
 * get(11) == 21, get(12) == 22, and so forth.  Elements must be added in
 * sequential order - that is, on any call to add, the index argument must
 * be > the index argument the last time it was called, and the same for the
 * value argument.
 * <p>
 * This is used to handle caching of logical line lengths in OutWriter - 
 * if we have a 400000 line file, most lines will typically not need to be 
 * word wrapped.  So we don't want to create a 400000 element int[] if most
 * of the time the number of wrapped lines will turn out to be 1 - instead,
 * only lines that are actually wrapped will have a line count added to a
 * SparseIntList; its get() behavior takes care of returning correct values
 * for the non-wrapped lines in between.
 *
 * @author  Tim Boudreau
 */
final class SparseIntList {
    private int[] keys;
    private int[] values;
    private int used = 0;
    private int lastAdded = Integer.MIN_VALUE;
    private int lastIndex = Integer.MIN_VALUE;
    
    /** Creates a new instance of IntMap */
    public SparseIntList(int capacity) {
        allocArrays (capacity);
    }
    
    /** Add an integer to the list.  The value must be > than the last value passed
     * to this methodd, and the index must be > than the last index passed to
     * this method.  Note that when you add <i>at</i> an index, you are really
     * adding numbers to the returned value <i>after</i> that index.  In other
     * words, if you call add(20, 11) as the first entry, get(11) will still return 11.
     * But get(12) will return 21.
     */
    public synchronized void add (int value, int idx) {
        if (value <= lastAdded) {
            throw new IllegalArgumentException ("Contents must be presorted - " + //NOI18N
                "added value " + value + " is less than preceding " + //NOI18N
                "value " + lastAdded); //NOI18N
        }
        if (idx <= lastIndex) {
            throw new IllegalArgumentException ("Contents must be presorted - " + //NOI18N
                "added index " + idx + " is less than preceding " + //NOI18N
                "index " + lastIndex); //NOI18N
        }
        if (used >= keys.length) {
            growArrays();
        }
        values[used] = value;
        keys[used++] = idx;
        lastAdded = value;
        lastIndex = idx;
    }
    
    int lastAdded() {
        return lastAdded;
    }
    
    int lastIndex() {
        return lastIndex;
    }
    
    private void allocArrays (int size) {
        keys = new int[size];
        values = new int[size];
        //Fill it with Integer.MAX_VALUE so binarySearch works properly (must
        //be sorted, cannot have 0's after the actual data
        Arrays.fill(keys, Integer.MAX_VALUE);
        Arrays.fill(values, -1);
    }
    
    /** Caches the last requested value.  Often we will be called repeatedly 
     * for the same value - since finding the value involves two binary searches,
     * cache it */
    private int lastGet = -1;
    /**
     * Caches the last requested result for the same reasons.
     */
    private int lastResult;
    /**
     * Get an entry in the list.  If the list is empty, it will simply return
     * the passed index value; if the index is lower than the first entry entered
     * by a call to add (index, value) in the list, it will do the same.
     * <p>
     * If the index is greater than an added value's index, the return result
     * will be the value of that index + the requested index minus the added
     * index.
     */
    public synchronized int get(int index) {
        if (index < 0) {
            return 0;
        }
        
        if ((used == 0) || (used > 0 && index < keys[0])) {
            return index;
        }
        
        if (index == lastGet) {
            return lastResult;
        } else {
            lastGet = index;
        }
        
        int result;
        //First, see if we have a real entry for this index - if add() was
        //called passing this exact index as a value
        int idx = Arrays.binarySearch(keys, index);
        
        if (idx < 0) {
            //Nope, not an exact match.  Divide and conquer the keys array to
            //find the nearest index to our value
            int nearest = findInRange(index, 0, used);
            
            //Make sure it's not bigger than the one we're looking for
            if (keys[nearest] > index) nearest--;
            if (nearest == -1) {
                result = index; 
            } else {
                //Result is the nearest value + the number of entries after it
                //that the passed index is
                result = values[nearest] + (index - keys[nearest]);
            }
        } else {
            //Just fetch the value and return it
            result = idx == 0 ? index : values[idx-1] + (index - keys[idx-1]);
        }
        lastResult = result;
        return result;
    }
    
    /** Recursive binary search - finds the index in the keys array of the
     * nearest value to the passed value.
     */
    private int findInRange (int val, int start, int end) {
        if (end - start <= 1) {
            return start;
        }
        int midPoint = start + ((end - start) / 2);
        int idxAtMidpoint = keys[midPoint];
        if (idxAtMidpoint > val) {
            return findInRange (val, start, start + ((end - start) / 2));
        } else {
            return findInRange (val, start + ((end - start) / 2), end);
        }
    }    
    
    /**
     * Grow the arrays we're using to store keys/values
     */
    private void growArrays() {
        int[] oldkeys = keys;
        int[] oldvals = values;
        allocArrays(Math.round(keys.length * 1.5f));
        System.arraycopy(oldkeys, 0, keys, 0, oldkeys.length);
        System.arraycopy(oldvals, 0, values, 0, oldvals.length);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer ("SparseIntList ["); //NOI18N
        result.append ("used="); //NOI18N
        result.append (used);
        result.append (" capacity="); //NOI18N
        result.append (keys.length);
        result.append (" keyValuePairs:"); //NOI18N
        for (int i=0; i < used; i++) {
            result.append (keys[i]);
            result.append (':'); //NOI18N
            result.append (values[i]);
            if (i != used-1) {
                result.append(','); //NOI18N
            }
        }
        result.append (']');
        return result.toString();
    }
    
}

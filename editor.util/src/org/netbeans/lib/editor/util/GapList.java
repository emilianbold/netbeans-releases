/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.RandomAccess;

/**
 * List implementation that stores items in an array
 * with a gap.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class GapList extends AbstractList
implements RandomAccess, Cloneable, java.io.Serializable {
    
    private static final Object[] EMPTY_ELEMENT_ARRAY = new Object[0];
    
    /**
     * The array buffer into which the elements are stored.
     * <br>
     * The elements are stored in the whole array except
     * the indexes starting at <code>gapStart</code>
     * till <code>gapStart + gapLength - 1</code>.
     */
    private transient Object elementData[];
    
    /**
     * The start of the gap in the elementData array.
     */
    private int gapStart;
    
    /**
     * Length of the gap in the elementData array starting at gapStart.
     */
    private int gapLength;
    
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list.
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    public GapList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " // NOI18N
                + initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
        this.gapLength = initialCapacity;
    }
    
    /**
     * Constructs an empty list.
     */
    public GapList() {
        elementData = EMPTY_ELEMENT_ARRAY;
    }
    
    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.  The <tt>GapList</tt> instance has an initial capacity of
     * 110% the size of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this list.
     * @throws NullPointerException if the specified collection is null.
     */
    public GapList(Collection c) {
        int size = c.size();
        // Allow 10% room for growth
        elementData = new Object[
        (int)Math.min((size*110L)/100,Integer.MAX_VALUE)];
        c.toArray(elementData);
        this.gapStart = size;
        this.gapLength = elementData.length - size;
    }
    
    /**
     * Trims the capacity of this <tt>GapList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>GapList</tt> instance.
     */
    public void trimToSize() {
        modCount++;
        if (gapLength > 0) {
            int newLength = elementData.length - gapLength;
            Object[] newElementData = new Object[newLength];
            copyAllData(newElementData);
            elementData = newElementData;
            // Leave gapStart as is
            gapLength = 0;
        }
    }
    
    /**
     * Increases the capacity of this <tt>GapList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
        modCount++; // expected to always increment modCount - see add() operations
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            int gapEnd = gapStart + gapLength;
            int afterGapLength = (oldCapacity - gapEnd);
            // Must ensure the gap will not be logically moved
            // (would have to call movedAbove/BeforeGapUpdate() methods)
            int newGapEnd = newCapacity - afterGapLength;
            Object[] newElementData = new Object[newCapacity];
            System.arraycopy(elementData, 0, newElementData, 0, gapStart);
            System.arraycopy(elementData, gapEnd, newElementData, newGapEnd, afterGapLength);
            elementData = newElementData;
            gapLength = newGapEnd - gapStart;
        }
    }
    
    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public int size() {
        return elementData.length - gapLength;
    }
    
    /**
     * Tests if this list has no elements.
     *
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
        return (elementData.length == gapLength);
    }
    
    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param elem element whose presence in this List is to be tested.
     * @return  <code>true</code> if the specified element is present;
     *		<code>false</code> otherwise.
     */
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }
    
    /**
     * Searches for the first occurence of the given argument, testing
     * for equality using the <tt>equals</tt> method.
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem) {
        if (elem == null) {
            int i = 0;
            while (i < gapStart) {
                if (elementData[i] == null) {
                    return i;
                }
                i++;
            }
            i += gapLength;
            int elementDataLength = elementData.length;
            while (i < elementDataLength) {
                if (elementData[i] == null) {
                    return i;
                }
                i++;
            }
            
        } else { // elem not null
            int i = 0;
            while (i < gapStart) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
                i++;
            }
            i += gapLength;
            int elementDataLength = elementData.length;
            while (i < elementDataLength) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
                i++;
            }
        }
        
        return -1;
    }
    
    /**
     * Returns the index of the last occurrence of the specified object in
     * this list.
     *
     * @param   elem   the desired element.
     * @return  the index of the last occurrence of the specified object in
     *          this list; returns -1 if the object is not found.
     */
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            int i = elementData.length - 1;
            int gapEnd = gapStart + gapLength;
            while (i >= gapEnd) {
                if (elementData[i] == null) {
                    return i;
                }
                i--;
            }
            i -= gapLength;
            while (i >= 0) {
                if (elementData[i] == null) {
                    return i;
                }
                i--;
            }
            
        } else { // elem not null
            int i = elementData.length - 1;
            int gapEnd = gapStart + gapLength;
            while (i >= gapEnd) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
                i--;
            }
            i -= gapLength;
            while (i >= 0) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
                i--;
            }
        }
        
        return -1;
    }
    
    /**
     * Returns a shallow copy of this <tt>GapList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>GapList</tt> instance.
     */
    public Object clone() {
        try {
            GapList clonedList = (GapList)super.clone();
            int size = size();
            Object[] clonedElementData = new Object[size];
            copyAllData(clonedElementData);
            clonedList.elementData = clonedElementData;
            // Will retain gapStart - would have to call moved*() otherwise
            clonedList.gapStart = size;
            clonedList.resetModCount();
            return clonedList;

        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
    
    public void copyItems(int srcStartIndex, int srcEndIndex,
    Object[] dest, int destIndex) {
        
        if (srcStartIndex < 0 || srcEndIndex < srcStartIndex || srcEndIndex > size()) {
            throw new IndexOutOfBoundsException("srcStartIndex=" + srcStartIndex // NOI18N
            + ", srcEndIndex=" + srcEndIndex + ", size()=" + size()); // NOI18N
        }
        
        if (srcEndIndex < gapStart) { // fully below gap
            System.arraycopy(elementData, srcStartIndex,
            dest, destIndex, srcEndIndex - srcStartIndex);
            
        } else { // above gap or spans the gap
            if (srcStartIndex >= gapStart) { // fully above gap
                System.arraycopy(elementData, srcStartIndex + gapLength, dest, destIndex,
                srcEndIndex - srcStartIndex);
                
            } else { // spans gap
                int beforeGap = gapStart - srcStartIndex;
                System.arraycopy(elementData, srcStartIndex, dest, destIndex, beforeGap);
                System.arraycopy(elementData, gapStart + gapLength, dest, destIndex + beforeGap,
                srcEndIndex - srcStartIndex - beforeGap);
            }
        }
    }
    
    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     * 	       in the correct order.
     */
    public Object[] toArray() {
        int size = size();
        Object[] result = new Object[size];
        copyAllData(result);
        return result;
    }
    
    /**
     * Returns an array containing all of the elements in this list in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     * @param a the array into which the elements of the list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     *         of the runtime type of every element in this list.
     */
    public Object[] toArray(Object a[]) {
        int size = size();
        if (a.length < size) {
            a = (Object[])java.lang.reflect.Array.newInstance(
                a.getClass().getComponentType(), size);
        }
        copyAllData(a);
        if (a.length > size)
            a[size] = null;
        
        return a;
    }
    
    // Positional Access Operations
    
    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object get(int index) {
        // rangeCheck(index) not necessary - would fail with AIOOBE anyway
        return elementData[(index < gapStart) ? index : (index + gapLength)];
    }
    
    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    public Object set(int index, Object element) {
        // rangeCheck(index) not necessary - would fail with AIOOBE anyway
        if (index >= gapStart) {
            index += gapLength;
        }
        Object oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }
    
    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(Object o) {
        add(size(), o);
        return true;
    }
    
    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws    IndexOutOfBoundsException if index is out of range
     *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
     */
    public void add(int index, Object element) {
        int size = size();
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size); // NOI18N
        }

        ensureCapacity(size + 1);  // Increments modCount!!
        moveGap(index);

        elementData[gapStart++] = element;
        gapLength--;
    }
    
    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the
     * specified Collection's Iterator.  The behavior of this operation is
     * undefined if the specified Collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified Collection is this list, and this
     * list is nonempty.)
     *
     * @param c the elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    NullPointerException if the specified collection is null.
     */
    public boolean addAll(Collection c) {
        return addAll(size(), c);
    }
    
    /**
     * Inserts all of the elements in the specified Collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified Collection's iterator.
     *
     * @param index index at which to insert first element
     *		    from the specified collection.
     * @param c elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     * @throws    NullPointerException if the specified Collection is null.
     */
    public boolean addAll(int index, Collection c) {
        return addArray(index, c.toArray());
    }

    /*
     * Inserts all elements from the given array into this list, starting
     * at the given index.
     *
     * @param index index at which to insert first element from the array.
     * @param elements array of elements to insert.
     */
    public boolean addArray(int index, Object[] elements) {
        return addArray(index, elements, 0, elements.length);
    }

    /**
     * Inserts elements from the given array into this list, starting
     * at the given index.
     *
     * @param index index at which to insert first element.
     * @param elements array of elements from which to insert elements.
     * @param off offset in the elements pointing to first element to copy.
     * @param len number of elements to copy from the elements array.
     */
    public boolean addArray(int index, Object[] elements, int off, int len) {
        int size = size();
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size); // NOI18N
        }
        
        ensureCapacity(size + len);  // Increments modCount
        
        moveGap(index);
        System.arraycopy(elements, off, elementData, gapStart, len);
        gapStart += len;
        gapLength -= len;

        return (len != 0);
    }
    
    
    
    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        removeRange(0, size());
    }
    
    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object remove(int index) {
        int size = size();
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException(
                "remove(): Index: " + index + ", Size: " + size); // NOI18N
        }

        modCount++;
        moveGap(index + 1); // if previous were adds() - this should be no-op
        Object oldValue = elementData[index];
        removeUpdate(index, elementData, index, index + 1);
        elementData[index] = null;
        gapStart--;
        gapLength++;
        
        return oldValue;
    }

    /**
     * Removes elements at the given index.
     *
     * @param index index of the first element to be removed.
     * @param count number of elements to remove.
     */
    public void remove(int index, int count) {
        int toIndex = index + count;
        if (index < 0 || toIndex < index || toIndex > size()) {
            throw new IndexOutOfBoundsException("index=" + index // NOI18N
            + ", count=" + count + ", size()=" + size()); // NOI18N
        }
        removeRange(index, toIndex);
    }
    
    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
     * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        if (fromIndex == toIndex) {
            return;
        }
        
        int removeCount = toIndex - fromIndex;
        if (fromIndex >= gapStart) { // completely over gap
            // Move gap to the start of the removed area
            // (this should be the minimum necessary count of elements moved)
            moveGap(fromIndex);
            
            // Allow GC of removed items
            fromIndex += gapLength; // begining of abandoned area
            toIndex += gapLength;
            removeUpdate(fromIndex - gapLength, elementData, fromIndex, toIndex);
            while (fromIndex < toIndex) {
                elementData[fromIndex] = null;
                fromIndex++;
            }
            
        } else { // completely below gap or spans the gap
            if (toIndex <= gapStart) {
                // Move gap to the end of the removed area
                // (this should be the minimum necessary count of elements moved)
                moveGap(toIndex);
                gapStart = fromIndex;
                // Call removeUpdate() for items that will be physically removed soon
                removeUpdate(fromIndex, elementData, fromIndex, toIndex);
                
            } else { // spans gap: gapStart > fromIndex but gapStart - fromIndex < removeCount
                removeUpdate(fromIndex, elementData, fromIndex, gapStart);
                // Allow GC of removed items
                for (int clearIndex = fromIndex; clearIndex < gapStart; clearIndex++) {
                    elementData[clearIndex] = null;
                }
                
                fromIndex = gapStart + gapLength; // part above the gap
                gapStart = toIndex - removeCount; // original value of fromIndex
                toIndex += gapLength;
                removeUpdate(gapStart, elementData, fromIndex, toIndex);
            }
            
            // Allow GC of removed items
            while (fromIndex < toIndex) {
                elementData[fromIndex++] = null;
            }
            
        }
        
        gapLength += removeCount;
    }
    
    /**
     * Called prior physical removing of the data from the list.
     * <br>
     * The implementation can possibly update the elements in the removed area.
     * <br>
     * After this method finishes the whole removed area will be
     * <code>null</code>-ed.
     *
     * @param index index in the list of the first item being removed.
     * @param data array of objects from which the data are being removed.
     *  The next two parameters define the indexes at which the elements
     *  can be updated.
     *  <br>
     *  Absolutely no changes should be done outside of
     *  <code>&lt;startOff, endOff)</code> area.
     * @param startOff offset in the data array of the first element that
     *  will be removed.
     * @param endOff offset in the data array following the last item that will
     *  be removed.
     */
    protected void removeUpdate(int index, Object[] data, int startOff, int endOff) {
    }

    /*
    protected void movedAboveGapUpdate(Object[] array, int index, int count) {
    }
    
    protected void movedBelowGapUpdate(Object[] array, int index, int count) {
    }
    */
    
    private void moveGap(int index) {
        if (index == gapStart) {
            return; // do nothing
        }

        if (index < gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(elementData, index, elementData,
            gapStart + gapLength - moveSize, moveSize);
            clearEmpty(index, Math.min(moveSize, gapLength));
            gapStart = index;
            // movedAboveGapUpdate(elementData, gapStart + gapLength, moveSize);
            
        } else { // above gap
            int gapEnd = gapStart + gapLength;
            int moveSize = index - gapStart;
            System.arraycopy(elementData, gapEnd, elementData, gapStart, moveSize);
            if (index < gapEnd) {
                clearEmpty(gapEnd, moveSize);
            } else {
                clearEmpty(index, gapLength);
            }
            // movedBelowGapUpdate(elementData, gapStart, moveSize);
            gapStart += moveSize;
        }
    }
    
    private void copyAllData(Object[] toArray) {
        if (gapLength != 0) {
            int gapEnd = gapStart + gapLength;
            System.arraycopy(elementData, 0, toArray, 0, gapStart);
            System.arraycopy(elementData, gapEnd, toArray, gapStart,
                elementData.length - gapEnd);
        } else { // no gap => single copy of everything
            System.arraycopy(elementData, 0, toArray, 0, elementData.length);
        }
    }
    
    private void clearEmpty(int index, int length) {
        while (--length >= 0) {
            elementData[index++] = null; // allow GC
        }
    }
    
    private void resetModCount() {
        modCount = 0;
    }
    
    /**
     * Save the state of the <tt>GapList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>GapList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException{
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();
        
        // Write out array length
        s.writeInt(elementData.length);
        
        // Write out all elements in the proper order.
        int i = 0;
        while (i < gapStart) {
            s.writeObject(elementData[i]);
            i++;
        }
        i += gapLength;
        int elementDataLength = elementData.length;
        while (i < elementDataLength) {
            s.writeObject(elementData[i]);
            i++;
        }
    }
    
    /**
     * Reconstitute the <tt>GapList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();
        
        // Read in array length and allocate array
        int arrayLength = s.readInt();
        elementData = new Object[arrayLength];
        
        // Read in all elements in the proper order.
        int i = 0;
        while (i < gapStart) {
            elementData[i] = s.readObject();
            i++;
        }
        i += gapLength;
        int elementDataLength = elementData.length;
        while (i < elementDataLength) {
            elementData[i] = s.readObject();
            i++;
        }
    }
    
    /**
     * Internal consistency check.
     */
    void consistencyCheck() {
        if (gapStart < 0 || gapLength < 0
            || gapStart + gapLength > elementData.length
        ) {
            consistencyError("Inconsistent gap"); // NOI18N
        }
        
        // Check whether the whole gap contains only nulls
        for (int i = gapStart + gapLength - 1; i >= gapStart; i--) {
            if (elementData[i] != null) {
                consistencyError("Non-null value at raw-index i"); // NOI18N
            }
        }
    }
    
    private void consistencyError(String s) {
        throw new IllegalStateException(s + ": " + toStringInternals()); // NOI18N
    }
    
    String toStringInternals() {
        return "elementData.length=" + elementData.length // NOI18N
            + ", gapStart=" + gapStart + ", gapLength=" + gapLength; // NOI18N
    }
    
}

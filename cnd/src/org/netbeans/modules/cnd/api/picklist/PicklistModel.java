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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.picklist;

import java.util.Collection;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * This interface defines the methods components for a picklist list (Picklist).
 */
public interface PicklistModel {

    /**
     * Adds the specified component to the beginning of this list,
     * increasing its size by one (if not already in the list).
     * @param   elem   the component to be added.
     */
    public void addElement(PicklistElement elem);

    /**
     * Adds the specified component to the beginning of this list, 
     * increasing its size by one (if not already inlist and check is true).
     * @param   elem   the component to be added.
     */
    public void addElement(PicklistElement elem, boolean check);

    /**
     * Tests if the specified element is a component in this list.
     *
     * @param   elem   an object.
     * @return  index of the specified element in the list if it exists
     * as determined by the <tt>equals</tt> method; <code>-1</code> otherwise.
     */
    public int contains(PicklistElement elem);

    /**
     * Returns the value at the specified index.  
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public PicklistElement getElementAt(int index);

    /** 
     * Returns all the elements in the list (possibly size 0 array)
     * @return all the elements in the list (possibly size 0 array).
     */
    public PicklistElement[] getElements();
    
    public String[] getElementsDisplayName();

    /** 
     * Returns the max length of the list.
     * @return the max length of the list
     */
    public int getMaxSize();

    /** 
     * Returns the top-most element in the list if not empty, othervise null.
     * @return the top-most element in the list
     */
    public PicklistElement getMostRecentUsedElement();

    /** 
     * Returns the length of the list.
     * @return the length of the list
     */
    public int getSize();

    /**
     * Removes all elements from this list and sets its size to zero.
     */
    public void removeAllElements();

    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this list.
     *
     * @param   elem   the element to be removed.
     * @return  the element removed if found, null othervise.
     */
    public PicklistElement removeElement(PicklistElement elem);

    /**
     * Removes the element at the specified index from the list. Each element in 
     * this list with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. The size of this list 
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the list. <p>
     *
     * @param      index   the index of the object to remove.
     * @return  the element removed if index valid, null othervise.
     */
    public PicklistElement removeElementAt(int index);

    /**
     * Replaces the element at the specified index with the one
     * specified.
     *
     * @param      index   the index of the object to remove.
     * @return  the element replaced if index valid, null othervise.
     */
    public PicklistElement replaceElementAt(PicklistElement elem, int index);

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     * @param l the <code>PicklistDataListener</code> to be added
     */  
    public void addPicklistDataListener(PicklistDataListener l);

    /**
     * Removes a listener from the list that's notified each time a 
     * change to the data model occurs.
     * @param l the <code>PicklistDataListener</code> to be removed
     */  
    public void removePicklistDataListener(PicklistDataListener l);

    /**
     * Clones a picklist
     */
    public PicklistModel clonePicklist();

    /**
     * First removes all elements, then copies all elements from 'picklist'.
     */
    public void copyPicklist(PicklistModel picklist);

    /**
     * Dumps all list elements to std err (for debugging...)
     */
    public void dumpElements();
}

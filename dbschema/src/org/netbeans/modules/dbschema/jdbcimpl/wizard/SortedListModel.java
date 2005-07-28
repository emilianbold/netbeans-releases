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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.AbstractListModel;


/**
 *
 */
public class SortedListModel extends AbstractListModel
{

    /**
     *
     */
    public static final Comparator DEFAULT_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            if (o1 == null)
                return -1;

            if (o2 == null)
                return 1;

            return o1.toString().compareTo(o2.toString());
        }

        public boolean equals(Object obj)
        {
            return obj == this;
        }
    };

    /**
     *
     */
    private List elements;

    /**
     *
     */
    private Comparator comp = DEFAULT_COMPARATOR;

    ///////////////////////////////////////////////////////////////////////////
    // construction
    ///////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    public SortedListModel()
    {
        elements = new ArrayList();
    }

    /**
     *
     */
    public SortedListModel(Collection c)
    {
        elements = new ArrayList(c);
        Collections.sort(elements, comp);
    }

    /**
     *
     */
    public SortedListModel(int initialCapacity)
    {
        elements = new ArrayList(initialCapacity);
    }

    /**
     *
     */
    public int getSize()
    {
        return elements.size();
    }

    /**
     *
     */
    public Object getElementAt(int index)
    {
        return elements.get(index);
    }

    /**
     * Returns the comparator used to sort the elements of this list model.
     *
     * @see #setComparator
     */
    public Comparator getComparator()
    {
        return comp;
    }

    /**
     *
     */
    public void setComparator(Comparator newComp)
    {
        if (comp == newComp)
            return;

        comp = newComp;
        Collections.sort(elements, comp);

        int last = elements.size() - 1;

        if (last >= 0)
            super.fireContentsChanged(this, 0, last);
    }

    /**
     * Returns <code>true</code> if this list model contains no elements.
     */
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }

    /**
     *
     */
    public boolean contains(Object o)
    {
        return Collections.binarySearch(elements, o, getComparator()) >= 0;
    }

    /**
     *
     */
    public Object[] toArray()
    {
        return elements.toArray();
    }

    /**
     *
     */
    public Object[] toArray(Object[] a)
    {
        return elements.toArray(a);
    }

    /**
     *
     */
    public int add(Object o)
    {
        int index = Collections.binarySearch(elements, o, getComparator());
        if (index < 0)
            index = -index - 1;

        elements.add(index, o);
        fireIntervalAdded(this, index, index);

        return index;
    }

    /**
     *
     */
    public int indexOf(Object o)
    {
        return Collections.binarySearch(elements, o, getComparator());
    }

    /**
     *
     */
    public int remove(Object o)
    {
        int index = Collections.binarySearch(elements, o, getComparator());
        if (index >= 0)
        {
            remove(index);
        }
        return index;
    }

    /**
     *
     */
    public boolean remove(int index)
    {
        elements.remove(index);
        fireIntervalRemoved(this, index, index);

        return true;
    }

    /**
     *
     */
    public void clear()
    {
        int last = elements.size() - 1;

        if (last >= 0)
        {
            elements.clear();
            fireIntervalRemoved(this, 0, last);
        }
    }

    /**
     * Returns a string that displays and identifies this
     * object's properties.
     *
     * @return a String representation of this object
     */
    public String toString()
    {
        return elements.toString();
    }
}

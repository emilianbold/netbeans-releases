/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author  Marian Petras
 */
class CompoundSearchIterator implements Iterator {
    
    /** */
    private final SearchInfo[] elements;
    /** */
    private int elementIndex;
    /** */
    private Iterator elementIterator;
    /** */
    private Object nextObject;
    /** */
    private boolean upToDate;
    
    /**
     * Creates a new instance of <code>CompoundSearchIterator</code>.
     *
     * @param  elements  elements of the compound iterator
     * @exception  java.lang.IllegalArgumentException
     *             if the argument is <code>null</code>
     */
    CompoundSearchIterator(SearchInfo[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }
        
        if (elements.length == 0) {
            this.elements = null;
            elementIndex = 0;
            upToDate = true;                //hasNext() returns always false
        } else {
            this.elements = elements;
            elementIterator = elements[elementIndex = 0].objectsToSearch();
            upToDate = false;
        }
    }

    /**
     */
    public boolean hasNext() {
        if (!upToDate) {
            update();
        }
        return elementIndex < elements.length;
    }

    /**
     */
    public Object next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        upToDate = false;
        return nextObject;
    }
    
    /**
     */
    private void update() {
        assert upToDate == false;
        
        while (!elementIterator.hasNext()) {
            elements[elementIndex] = null;
            
            if (++elementIndex == elements.length) {
                break;
            }
            
            elementIterator = elements[elementIndex].objectsToSearch();
        }
        
        if (elementIndex < elements.length) {
            nextObject = elementIterator.next();
        } else {
            elementIterator = null;
            nextObject = null;
        }
        
        upToDate = true;
    }

    /**
     * @exception  java.lang.UnsupportedOperationException
     *             always - this operation is not supported
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.lib;

import java.util.*;

/**
 * Let list of iterators behave as single iterator.
 * The implementation supports just usage where
 * hasNext() precedes next().
 * <pre>
 * Iterator it = .. // some instance
 * while (it.hasNext()) it.next();
 * </pre>
 */
public class IteratorIterator implements Iterator {

    private Vector iterators = new Vector();

    private Iterator current = null;  //current iterator;
    private Iterator it = null;       //iterators.iterator();
    private Object next = null;       //current element

    /**
     * New iterators can be added if hasNext() or prior its first call.
     */
    public void add(Iterator it) {
        iterators.add(it);
    }

    public void remove() {
        throw new UnsupportedOperationException(); 
    }

    public Object next() {
       if (next != null) {
           Object tmp = next;
           next = null;
           return tmp;
       } else {
           throw new NoSuchElementException();
       }
    }

    public boolean hasNext() {
        if (it == null) it = iterators.iterator();
        if (next != null) return true;

        while (it.hasNext()){
            if (current == null || current.hasNext() == false) {
                current = (Iterator) it.next();
            }
            if (current.hasNext()) {
                next = current.next(); 
                return true;
            }
        }
        next = null;
        return false;
    }  
}

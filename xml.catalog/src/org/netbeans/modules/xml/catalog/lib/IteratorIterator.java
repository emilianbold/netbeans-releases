/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.lib;

import java.util.*;

/**
 * Let a list of iterators behave as a single iterator.
 *
 * @author  Petr Kuzel
 */
public final class IteratorIterator implements Iterator {

    private Vector iterators = new Vector();

    private Iterator current = null;  //current iterator;
    private Iterator it = null;       //iterators.iterator();
    
    /*
     * It is set by hasNext() and cleared by next() call.
     */
    private Object next = null;       //current element

    /**
     * New iterators can be added while hasNext() or prior its first call.
     */
    public void add(Iterator it) {
        iterators.add(it);
    }

    /**
     * Unsupported operation.
     */
    public void remove() {
        throw new UnsupportedOperationException(); 
    }

    public Object next() {
       if (hasNext()) {
           Object tmp = next;
           next = null;
           return tmp;
       } else {
           throw new NoSuchElementException();
       }
    }

    public boolean hasNext() {
        if (next != null) return true;
        
        if (it == null) it = iterators.iterator();
        while (current == null) {
            if (it.hasNext()) {
                current = (Iterator) it.next();
            } else {
                return false;
            }
        }
        
        while (current.hasNext() || it.hasNext()) {
            
            // fetch next iterator if necessary
            if (current.hasNext() == false) {
                current = (Iterator) it.next();
                continue;
            } else {           
                next = current.next();
                return true;
            }
        }
        next = null;
        return false;
    }  
}

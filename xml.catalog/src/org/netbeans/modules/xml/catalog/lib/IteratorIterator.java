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
     * @param it iterator, never <codE>null</null>.
     */
    public void add(Iterator it) {
        assert it != null;
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

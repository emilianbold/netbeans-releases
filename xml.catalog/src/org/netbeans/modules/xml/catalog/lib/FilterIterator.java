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
 * Filter backend iterator by appling filter rule. 
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class FilterIterator implements Iterator {

    /** Creates new FilterIterator */
    public FilterIterator() {
    }

    private Iterator peer;
    private Filter filter;
    private Object next;

    public FilterIterator(Iterator it, Filter filter) {
        if (it == null || filter == null)
            throw new IllegalArgumentException("null not allowed"); // NOI18N
        peer = it; 
        this.filter = filter;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Object next() {
        if (next != null || hasNext()) {
           Object ret = next;
           next = null;
           return ret;
        } else {
           throw new NoSuchElementException();
        }
    }

    public boolean hasNext() {
        if (next != null) return true;

        while (peer.hasNext()) {
            next = peer.next();
            if (filter.accept(next)) return true;
        }
        next = null;
        return false;
    }
    
    public static interface Filter {
        public boolean accept(Object obj);
    }
}

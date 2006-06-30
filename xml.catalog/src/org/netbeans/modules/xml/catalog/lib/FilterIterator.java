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
 * Filter backend iterator by appling a filter rule.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class FilterIterator implements Iterator {

    private final Iterator peer;
    private final Filter filter;

    /*
     * Holds candidate for next() call. It is nulledt by the next() call.
     */
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

    /*
     * Retunr <code>next</code> element if available and reset it.
     */
    public Object next() {
        if (hasNext()) {
           Object ret = next;
           next = null;
           return ret;
        } else {
           throw new NoSuchElementException();
        }
    }

    /*
     * Determine if there is a next element. Put it in <code>next</code> field.
     */
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

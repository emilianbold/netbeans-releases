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

package org.netbeans.core.startup;

import java.util.*;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

/**
 * A special filtering and lazy iterator used by our XML factories
 *
 * @author Petr Nejedly
 */
class LazyIterator implements Iterator {
    Object first;
    Object step;
    Class template;
    Object skip;
    Iterator delegate;

    LazyIterator(Object first, Class template, Object skip) {
        assert first != null;

        this.first = first;
        this.template = template;
        this.skip = skip;
    }

    public boolean hasNext() {
        if (first != null) return true; 

        // lazily prepare delegate
        if (delegate == null) delegate = prepareDelegate();

        // check next step
        if (step != null) return true;

        // prepare next step
        while (delegate.hasNext() && step == null) {
            Object next = ((Lookup.Item)delegate.next()).getType();
            if (next != skip) step = next;
        }

        return step != null;
    }

    public java.lang.Object next() {
        if (first != null) {
            Object ret = first;
            first = null;
            return ret;
        }
        // lazily prepare delegate
        if (delegate == null) delegate = prepareDelegate();

        // check next step
        if (step != null) {
            Object ret = step;
            step = null;
            return ret;
        }

        // return directly next without storing
        while (delegate.hasNext()) {
            Object next = ((Lookup.Item)delegate.next()).getType();
            if (next != skip) return next;
        }

        throw new NoSuchElementException();
    }

    private Iterator prepareDelegate() {
        return Lookup.getDefault().lookup(new Lookup.Template(template)).allItems().iterator();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    } 
}

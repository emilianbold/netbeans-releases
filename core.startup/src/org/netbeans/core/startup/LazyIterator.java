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

package org.netbeans.core.startup;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.openide.util.Lookup;

/**
 * A special filtering and lazy iterator used by our XML factories
 *
 * @author Petr Nejedly
 */
class LazyIterator implements Iterator<Class> {
    Class first;
    Class step;
    Class<?> template;
    Object skip;
    Iterator delegate;

    LazyIterator(Class first, Class template, Class skip) {
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
            Class next = ((Lookup.Item)delegate.next()).getType();
            if (next != skip) step = next;
        }

        return step != null;
    }

    public Class next() {
        if (first != null) {
            Class ret = first;
            first = null;
            return ret;
        }
        // lazily prepare delegate
        if (delegate == null) delegate = prepareDelegate();

        // check next step
        if (step != null) {
            Class ret = step;
            step = null;
            return ret;
        }

        // return directly next without storing
        while (delegate.hasNext()) {
            Class next = ((Lookup.Item)delegate.next()).getType();
            if (next != skip) return next;
        }

        throw new NoSuchElementException();
    }

    private Iterator prepareDelegate() {
        return Lookup.getDefault().lookupResult(template).allItems().iterator();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    } 
}

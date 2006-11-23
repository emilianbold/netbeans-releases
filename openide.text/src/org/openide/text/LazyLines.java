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
package org.openide.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** Lazy List that delegates to another instance of itself.
 */
final class LazyLines extends Object implements List<Line> {
    private List<Line> delegate;
    private DocumentLine.Set set;

    public LazyLines(DocumentLine.Set set) {
        this.set = set;
    }

    /** Override this to create the delegate
     */
    private List<Line> createDelegate() {
        int cnt = set.listener.getOriginalLineCount();
        List<Line> l = new ArrayList<Line>(cnt);

        for (int i = 0; i < cnt; i++) {
            l.add(set.getOriginal(i));
        }

        return l;
    }

    private synchronized List<Line> getDelegate() {
        if (delegate == null) {
            delegate = createDelegate();
        }

        return delegate;
    }

    public int indexOf(Object o) {
        if (o instanceof DocumentLine) {
            Line find = set.findLine((DocumentLine) o);

            if (find != null) {
                int indx = set.listener.getOld(find.getLineNumber());

                if (set.getOriginal(indx).equals(o)) {
                    // just to verify that the index really exists
                    return indx;
                }
            }
        }

        return -1;
    }

    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    //
    // Pure delegate methods
    //
    public int hashCode() {
        return getDelegate().hashCode();
    }

    public boolean addAll(java.util.Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(java.util.Collection c) {
        throw new UnsupportedOperationException();
    }

    public ListIterator<Line> listIterator() {
        return getDelegate().listIterator();
    }

    public Object[] toArray() {
        return getDelegate().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return getDelegate().toArray(a);
    }

    public ListIterator<Line> listIterator(int index) {
        return getDelegate().listIterator(index);
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        return getDelegate().equals(obj);
    }

    public boolean contains(Object o) {
        return getDelegate().contains(o);
    }

    public void add(int index, Line element) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        getDelegate().clear();
    }

    public Line set(int index, Line element) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return getDelegate().size();
    }

    public Line get(int index) {
        return getDelegate().get(index);
    }

    public boolean containsAll(Collection<?> c) {
        return getDelegate().containsAll(c);
    }

    public boolean add(Line o) {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public List<Line> subList(int fromIndex, int toIndex) {
        return getDelegate().subList(fromIndex, toIndex);
    }

    public Line remove(int index) {
        return getDelegate().remove(index);
    }

    public Iterator<Line> iterator() {
        return getDelegate().iterator();
    }

    public boolean addAll(int index, java.util.Collection c) {
        throw new UnsupportedOperationException();
    }
}

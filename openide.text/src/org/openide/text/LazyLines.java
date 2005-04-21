/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.text;


/** Lazy List that delegates to another instance of itself.
 */
final class LazyLines extends Object implements java.util.List {
    private java.util.List delegate;
    private DocumentLine.Set set;

    public LazyLines(DocumentLine.Set set) {
        this.set = set;
    }

    /** Override this to create the delegate
     */
    private java.util.List createDelegate() {
        int cnt = set.listener.getOriginalLineCount();
        java.util.List l = new java.util.ArrayList(cnt);

        for (int i = 0; i < cnt; i++) {
            l.add(set.getOriginal(i));
        }

        return l;
    }

    private synchronized java.util.List getDelegate() {
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

    public java.util.ListIterator listIterator() {
        return getDelegate().listIterator();
    }

    public Object[] toArray() {
        return getDelegate().toArray();
    }

    public Object[] toArray(Object[] a) {
        return getDelegate().toArray(a);
    }

    public java.util.ListIterator listIterator(int index) {
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

    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        getDelegate().clear();
    }

    public Object set(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return getDelegate().size();
    }

    public Object get(int index) {
        return getDelegate().get(index);
    }

    public boolean containsAll(java.util.Collection c) {
        return getDelegate().containsAll(c);
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    public boolean retainAll(java.util.Collection c) {
        throw new UnsupportedOperationException();
    }

    public java.util.List subList(int fromIndex, int toIndex) {
        return getDelegate().subList(fromIndex, toIndex);
    }

    public Object remove(int index) {
        return getDelegate().remove(index);
    }

    public java.util.Iterator iterator() {
        return getDelegate().iterator();
    }

    public boolean addAll(int index, java.util.Collection c) {
        throw new UnsupportedOperationException();
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util.lookup;

import org.openide.util.Lookup;

import java.io.*;

import java.lang.ref.WeakReference;

import java.util.*;


/** Storages that can switch between another storages.
 * @author  Jaroslav Tulach
 */
final class DelegatingStorage extends Object implements Serializable, AbstractLookup.Storage {
    /** object to delegate to */
    private AbstractLookup.Storage delegate;

    /** thread just accessing the storage */
    private Thread owner;

    public DelegatingStorage(AbstractLookup.Storage d) {
        this.delegate = d;
        this.owner = Thread.currentThread();
    }

    /** Never serialize yourself, always put there the delegate */
    public Object writeReplace() {
        return this.delegate;
    }

    /** Method to check whether there is not multiple access from the same thread.
     */
    public void checkForTreeModification() {
        if (Thread.currentThread() == owner) {
            throw new AbstractLookup.ISE("You are trying to modify lookup from lookup query!"); // NOI18N
        }
    }

    /** Checks whether we have simple behaviour or complex.
     */
    public static boolean isSimple(AbstractLookup.Storage s) {
        if (s instanceof DelegatingStorage) {
            return ((DelegatingStorage) s).delegate instanceof ArrayStorage;
        } else {
            return s instanceof ArrayStorage;
        }
    }

    /** Exits from the owners ship of the storage.
     */
    public AbstractLookup.Storage exitDelegate() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("Onwer: " + owner + " caller: " + Thread.currentThread()); // NOI18N
        }

        AbstractLookup.Storage d = delegate;
        delegate = null;

        return d;
    }

    public boolean add(org.openide.util.lookup.AbstractLookup.Pair item, Object transaction) {
        return delegate.add(item, transaction);
    }

    public void remove(org.openide.util.lookup.AbstractLookup.Pair item, Object transaction) {
        delegate.remove(item, transaction);
    }

    public void retainAll(Map retain, Object transaction) {
        delegate.retainAll(retain, transaction);
    }

    public Object beginTransaction(int ensure) {
        try {
            return delegate.beginTransaction(ensure);
        } catch (UnsupportedOperationException ex) {
            // let's convert to InheritanceTree
            ArrayStorage arr = (ArrayStorage) delegate;
            delegate = new InheritanceTree();

            //
            // Copy content
            //
            Enumeration en = arr.lookup(Object.class);

            while (en.hasMoreElements()) {
                if (!delegate.add((AbstractLookup.Pair) en.nextElement(), new ArrayList())) {
                    throw new IllegalStateException("All objects have to be accepted"); // NOI18N
                }
            }

            //
            // Copy listeners
            //
            AbstractLookup.ReferenceToResult ref = arr.cleanUpResult(null);

            if (ref != null) {
                ref.cloneList(delegate);
            }

            // we have added the current content and now we can start transaction
            return delegate.beginTransaction(ensure);
        }
    }

    public org.openide.util.lookup.AbstractLookup.ReferenceToResult cleanUpResult(
        org.openide.util.Lookup.Template templ
    ) {
        return delegate.cleanUpResult(templ);
    }

    public void endTransaction(Object transaction, Set modified) {
        delegate.endTransaction(transaction, modified);
    }

    public Enumeration lookup(Class clazz) {
        return delegate.lookup(clazz);
    }

    public org.openide.util.lookup.AbstractLookup.ReferenceToResult registerReferenceToResult(
        org.openide.util.lookup.AbstractLookup.ReferenceToResult newRef
    ) {
        return delegate.registerReferenceToResult(newRef);
    }
}

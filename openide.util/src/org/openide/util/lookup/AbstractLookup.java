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
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.ref.*;
import java.lang.ref.ReferenceQueue;

import java.util.*;


/** Implementation of the lookup from OpenAPIs that is based on the
 * introduction of Item. This class should provide the default way
 * of how to store (Class, Object) pairs in the lookups. It offers
 * protected methods for subclasses to register the pairs.
 * <p>Serializable since 3.27.
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public class AbstractLookup extends Lookup implements Serializable {
    static final long serialVersionUID = 5L;

    /** lock for initialization of the maps of lookups */
    private static Object treeLock = new Object();

    /** This is a workaround for bug 35366 that probably has to be here until
     * we end producing separate openide-lookup.jar. The activeReferenceQueue
     * has to be obtained using reflection to prevent the openide-lookup.jar
     * to be as large as openide-util.jar.
     */
    private static Object activeQueue;

    /** the tree that registers all items (or Integer as a treshold size) */
    private Object tree;

    /** count of items in to lookup */
    private int count;

    /** Constructor to create this lookup and associate it with given
     * Content. The content than allows the creator to invoke protected
     * methods which are not accessible for any other user of the lookup.
     *
     * @param content the content to assciate with
     *
     * @since 1.25
     */
    public AbstractLookup(Content content) {
        content.attach(this);
    }

    /** Constructor for testing purposes that allows specification of storage
     * as mechanism as well.
     */
    AbstractLookup(Content content, Storage storage) {
        this(content);
        this.tree = storage;
        initialize();
    }

    /** Constructor for testing purposes that allows specification of storage
     * as mechanism as well.
     * @param trashhold number of Pair to "remain small"
     */
    AbstractLookup(Content content, Integer trashhold) {
        this(content);
        this.tree = trashhold;
    }

    /** Default constructor for subclasses that do not need to provide a content
     */
    protected AbstractLookup() {
    }

    public String toString() {
        if (tree instanceof Storage) {
            return "AbstractLookup" + lookup(new Lookup.Template(Object.class)).allItems(); // NOI18N
        } else {
            return super.toString();
        }
    }

    /** Entres the storage management system.
     */
    private AbstractLookup.Storage enterStorage() {
        for (;;) {
            synchronized (treeLock) {
                if (tree instanceof AbstractLookup.Storage) {
                    if (tree instanceof DelegatingStorage) {
                        // somebody is using the lookup right now
                        DelegatingStorage del = (DelegatingStorage) tree;

                        // check whether there is not access from the same 
                        // thread (can throw exception)
                        del.checkForTreeModification();

                        try {
                            treeLock.wait();
                        } catch (InterruptedException ex) {
                            // ignore and go on
                        }

                        continue;
                    } else {
                        // ok, tree is initialized and nobody is using it yet
                        tree = new DelegatingStorage((Storage) tree);

                        return (Storage) tree;
                    }
                }

                // first time initialization of the tree
                if (tree instanceof Integer) {
                    tree = new ArrayStorage((Integer) tree);
                } else {
                    tree = new ArrayStorage();
                }
            }

            // the tree has not yet been initilized, initialize and go on again
            initialize();
        }
    }

    /** Exists tree ownership.
     */
    private AbstractLookup.Storage exitStorage() {
        synchronized (treeLock) {
            AbstractLookup.Storage stor = ((DelegatingStorage) tree).exitDelegate();
            tree = stor;
            treeLock.notifyAll();

            return stor;
        }
    }

    /** Method for subclasses to initialize them selves.
     */
    protected void initialize() {
    }

    /** Notifies subclasses that a query is about to be processed.
     * @param template the template
     */
    protected void beforeLookup(Template template) {
    }

    /** The method to add instance to the lookup with.
     * @param pair class/instance pair
     */
    protected final void addPair(Pair pair) {
        HashSet toNotify = new HashSet();

        Object transaction = null;
        AbstractLookup.Storage t = enterStorage();

        try {
            transaction = t.beginTransaction(-2);

            if (t.add(pair, transaction)) {
                try {
                    pair.setIndex(t, count++);
                } catch (IllegalStateException ex) {
                    // remove the pair
                    t.remove(pair, transaction);

                    // rethrow the exception
                    throw ex;
                }

                // if the pair is newly added and was not there before
                t.endTransaction(transaction, toNotify);
            } else {
                // just finish the process by calling endTransaction
                t.endTransaction(transaction, new HashSet());
            }
        } finally {
            exitStorage();
        }

        notifyListeners(toNotify);
    }

    /** Remove instance.
     * @param pair class/instance pair
     */
    protected final void removePair(Pair pair) {
        HashSet toNotify = new HashSet();

        Object transaction = null;
        AbstractLookup.Storage t = enterStorage();

        try {
            transaction = t.beginTransaction(-1);
            t.remove(pair, transaction);
            t.endTransaction(transaction, toNotify);
        } finally {
            exitStorage();
        }

        notifyListeners(toNotify);
    }

    /** Changes all pairs in the lookup to new values.
     * @param collection the collection of (Pair) objects
     */
    protected final void setPairs(Collection collection) {
        notifyCollectedListeners(setPairsAndCollectListeners(collection));
    }

    /** Collects listeners without notification. Needed in MetaInfServicesLookup
     * right now, but maybe will become an API later.
     */
    final HashSet setPairsAndCollectListeners(Collection collection) {
        HashSet toNotify = new HashSet(27);

        Object transaction = null;
        AbstractLookup.Storage t = enterStorage();

        try {
            // map between the Items and their indexes (Integer)
            HashMap shouldBeThere = new HashMap(collection.size() * 2);

            count = 0;

            Iterator it = collection.iterator();
            transaction = t.beginTransaction(collection.size());

            while (it.hasNext()) {
                Pair item = (Pair) it.next();

                if (t.add(item, transaction)) {
                    // the item has not been there yet
                    //t.endTransaction(transaction, toNotify);
                }

                // remeber the item, because it should not be removed
                shouldBeThere.put(item, new Info(count++, transaction));

                //                    arr.clear ();
            }

            //            Object transaction = t.beginTransaction ();
            // deletes all objects that should not be there and
            t.retainAll(shouldBeThere, transaction);

            // collect listeners
            t.endTransaction(transaction, toNotify);

            /*
            // check consistency
            Enumeration en = t.lookup (java.lang.Object.class);
            boolean[] max = new boolean[count];
            int mistake = -1;
            while (en.hasMoreElements ()) {
                Pair item = (Pair)en.nextElement ();

                if (max[item.index]) {
                    mistake = item.index;
                }
                max[item.index] = true;
            }

            if (mistake != -1) {
                System.err.println ("Mistake at: " + mistake);
                tree.print (System.err, true);
            }
            */
        } finally {
            exitStorage();
        }

        return toNotify;
    }

    /** Notifies all collected listeners. Needed by MetaInfServicesLookup,
     * maybe it will be an API later.
     */
    final void notifyCollectedListeners(Object listeners) {
        notifyListeners((HashSet) listeners);
    }

    private final void writeObject(ObjectOutputStream oos)
    throws IOException {
        AbstractLookup.Storage s = enterStorage();

        try {
            // #36830: Serializing only InheritanceTree no ArrayStorage
            s.beginTransaction(Integer.MAX_VALUE);

            // #32040: don't write half-made changes
            oos.defaultWriteObject();
        } finally {
            exitStorage();
        }
    }

    /** Lookups an object of given interface. This is the simplest method
     * for the lookuping, if more registered objects implement the given
     * class any of them can be returned.
     *
     * @param clazz class of the object we are searching for
     * @return the object implementing given class or null if no such
     *    has been found
     */
    public final Object lookup(Class clazz) {
        Lookup.Item item = lookupItem(new Lookup.Template(clazz));

        return (item == null) ? null : item.getInstance();
    }

    /** Lookups just one item.
     * @param template a template for what to find
     * @return item or null
     */
    public final Lookup.Item lookupItem(Lookup.Template template) {
        AbstractLookup.this.beforeLookup(template);

        ArrayList list = null;
        AbstractLookup.Storage t = enterStorage();

        try {
            Enumeration en;

            try {
                en = t.lookup(template.getType());

                return findSmallest(en, template, false);
            } catch (AbstractLookup.ISE ex) {
                // not possible to enumerate the exception, ok, copy it 
                // to create new
                list = new ArrayList();
                en = t.lookup(null); // this should get all the items without any checks

                // the checks will be done out side of the storage
                while (en.hasMoreElements()) {
                    list.add(en.nextElement());
                }
            }
        } finally {
            exitStorage();
        }

        return findSmallest(Collections.enumeration(list), template, true);
    }

    private static Pair findSmallest(Enumeration en, Lookup.Template template, boolean deepCheck) {
        int smallest = InheritanceTree.unsorted(en) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        Pair res = null;

        while (en.hasMoreElements()) {
            Pair item = (Pair) en.nextElement();

            if (matches(template, item, deepCheck)) {
                if (smallest == Integer.MIN_VALUE) {
                    // ok, sorted enumeration the first that matches is fine
                    return item;
                } else {
                    // check for the smallest item
                    if (smallest > item.getIndex()) {
                        smallest = item.getIndex();
                        res = item;
                    }
                }
            }
        }

        return res;
    }

    /** The general lookup method.
     * @param template the template describing the services we are looking for
     * @return object containing the results
     */
    public final Lookup.Result lookup(Lookup.Template template) {
        for (;;) {
            AbstractLookup.ISE toRun = null;

            AbstractLookup.Storage t = enterStorage();

            try {
                R r = new R();
                ReferenceToResult newRef = new ReferenceToResult(r, this, template);
                newRef.next = t.registerReferenceToResult(newRef);

                return r;
            } catch (AbstractLookup.ISE ex) {
                toRun = ex;
            } finally {
                exitStorage();
            }

            toRun.recover(this);

            // and try again
        }
    }

    /** Notifies listeners.
     * @param allAffectedResults set of R
     */
    private static void notifyListeners(Set allAffectedResults) {
        if (allAffectedResults.isEmpty()) {
            return;
        }

        Iterator it = allAffectedResults.iterator();

        while (it.hasNext()) {
            AbstractLookup.R result = (AbstractLookup.R) it.next();
            result.fireStateChanged();
        }
    }

    /**
     * Call resultChanged on all listeners.
     * @param listeners array of listeners in the format used by
     *        javax.swing.EventListenerList. It means that there are Class
     *        objects on even positions and the listeners on odd positions
     * @param ev the event to fire
     */
    static void notifyListeners(final Object[] listeners, final LookupEvent ev) {
        for (int i = listeners.length - 1; i >= 0; i -= 2) {
            LookupListener ll = (LookupListener) listeners[i];

            try {
                ll.resultChanged(ev);
            } catch (RuntimeException e) {
                // Such as e.g. occurred in #32040. Do not halt other things.
                e.printStackTrace();
            }
        }
    }

    /** A method that defines matching between Item and Template.
     * @param t template providing the criteria
     * @param item the item to match
     * @param deepCheck true if type of the pair should be tested, false if it is already has been tested
     * @return true if item matches the template requirements, false if not
     */
    static boolean matches(Template t, Pair item, boolean deepCheck) {
        String id = t.getId();

        if ((id != null) && !item.getId().equals(id)) {
            return false;
        }

        Object instance = t.getInstance();

        if ((instance != null) && !item.creatorOf(instance)) {
            return false;
        }

        if (deepCheck) {
            return item.instanceOf(t.getType());
        } else {
            return true;
        }
    }

    /**
     * Compares the array elements for equality.
     * @return true if all elements in the arrays are equal
     *  (by calling equals(Object x) method)
     */
    private static boolean compareArrays(Object[] a, Object[] b) {
        // handle null values
        if (a == null) {
            return (b == null);
        } else {
            if (b == null) {
                return false;
            }
        }

        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            // handle null values for individual elements
            if (a[i] == null) {
                if (b[i] != null) {
                    return false;
                }

                // both are null --> ok, take next
                continue;
            } else {
                if (b[i] == null) {
                    return false;
                }
            }

            // perform the comparison
            if (!a[i].equals(b[i])) {
                return false;
            }
        }

        return true;
    }

    /** Method to be called when a result is cleared to signal that the list
     * of all result should be checked for clearing.
     * @param template the template the result was for
     * @return true if the hash map with all items has been cleared
     */
    boolean cleanUpResult(Lookup.Template template) {
        AbstractLookup.Storage t = enterStorage();

        try {
            return t.cleanUpResult(template) == null;
        } finally {
            exitStorage();
        }
    }

    /** Generic support for listeners, so it can be used in other results
     * as well.
     * @param add true to add it, false to modify
     * @param l listener to modify
     * @param ref the value of the reference to listener or listener list
     * @return new value to the reference to listener or list
     */
    static Object modifyListenerList(boolean add, LookupListener l, Object ref) {
        if (add) {
            if (ref == null) {
                return l;
            }

            if (ref instanceof LookupListener) {
                ArrayList arr = new ArrayList();
                arr.add(ref);
                ref = arr;
            }

            ((ArrayList) ref).add(l);

            return ref;
        } else {
            // remove
            if (ref == null) {
                return null;
            }

            if (ref == l) {
                return null;
            }

            ArrayList arr = (ArrayList) ref;
            arr.remove(l);

            if (arr.size() == 1) {
                return arr.iterator().next();
            } else {
                return arr;
            }
        }
    }

    private static ReferenceQueue activeQueue() {
        if (activeQueue == null) {
            try {
                Class c = Class.forName("org.openide.util.Utilities"); // NOI18N
                Class[] noArgs = new Class[0];
                java.lang.reflect.Method m = c.getDeclaredMethod("activeReferenceQueue", noArgs); // NOI18N
                activeQueue = m.invoke(null, noArgs);
            } catch (Exception ex) {
                activeQueue = ex;
            }
        }

        return (activeQueue instanceof ReferenceQueue) ? (ReferenceQueue) activeQueue : null;
    }

    /** Storage to keep the internal structure of Pairs and to answer
     * different queries.
     */
    interface Storage {
        /** Initializes a modification operation by creating an object
         * that will be passsed to all add, remove, retainAll methods
         * and should collect enough information about the change to
         * notify listeners about the transaction later
         *
         * @param ensure the amount of items that will appear in the storage
         *   after the modifications (-1 == remove one, -2 == add one, >= 0
         *   the amount of objects at the end
         * @return a token to identify the transaction
         */
        public Object beginTransaction(int ensure);

        /** Collects all affected results R that were modified in the
         * given transaction.
         *
         * @param modified place to add results R to
         * @param transaction the transaction indentification
         */
        public void endTransaction(Object transaction, Set modifiedResults);

        /** Adds an item into the storage.
        * @param item to add
        * @param transaction transaction token
        * @return true if the Item has been added for the first time or false if some other
        *    item equal to this one already existed in the lookup
        */
        public boolean add(AbstractLookup.Pair item, Object transaction);

        /** Removes an item.
        */
        public void remove(AbstractLookup.Pair item, Object transaction);

        /** Removes all items that are not present in the provided collection.
        * @param retain collection of Pairs to keep them in
        * @param transaction the transaction context
        */
        public void retainAll(Map retain, Object transaction);

        /** Queries for instances of given class.
        * @param clazz the class to check
        * @return enumeration of Item
        * @see #unsorted
        */
        public Enumeration lookup(Class clazz);

        /** Registers another reference to a result with the storage. This method
         * has also a special meaning.
         *
         * @param newRef the new reference to remember
         * @return the previous reference that was kept (null if newRef is the first one)
         *    the applications is expected to link from newRef to this returned
         *    value to form a linked list
         */
        public ReferenceToResult registerReferenceToResult(ReferenceToResult newRef);

        /** Given the provided template, Do cleanup the results.
         * @param templ template of a result(s) that should be checked
         * @return null if all references for this template were cleared or one of them
         */
        public ReferenceToResult cleanUpResult(Lookup.Template templ);
    }

    /** Extension to the default lookup item that offers additional information
     * for the data structures use in AbstractLookup
     */
    public static abstract class Pair extends Lookup.Item implements Serializable {
        private static final long serialVersionUID = 1L;

        /** possition of this item in the lookup, manipulated in addPair, removePair, setPairs methods */
        private int index = -1;

        /** For use by subclasses. */
        protected Pair() {
        }

        final int getIndex() {
            return index;
        }

        final void setIndex(AbstractLookup.Storage tree, int x) {
            if (tree == null) {
                this.index = x;

                return;
            }

            if (this.index == -1) {
                this.index = x;
            } else {
                throw new IllegalStateException("You cannot use " + this + " in more than one AbstractLookup"); // NOI18N
            }
        }

        /** Tests whether this item can produce object
        * of class c.
        */
        protected abstract boolean instanceOf(Class c);

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *   as obj.
         */
        protected abstract boolean creatorOf(Object obj);
    }

    /** Result based on one instance returned.
     */
    static final class R extends WaitableResult {
        /** reference our result is attached to (do not modify) */
        public ReferenceToResult reference;

        /** listeners on the results or pointer to one listener */
        private Object listeners;

        public R() {
        }

        /** Checks whether we have simple behaviour of complex.
         */
        private boolean isSimple() {
            Storage s = (Storage) reference.lookup.tree;

            return DelegatingStorage.isSimple(s);
        }

        //
        // Handling cache management for both cases, no caches
        // for simple (but mark that we needed them, so refresh can
        // be done in cloneList) and complex when all 3 types
        // of result are cached
        //
        private Object getFromCache(int indx) {
            if (isSimple()) {
                return null;
            }

            Object maybeArray = reference.caches;

            if (maybeArray instanceof Object[]) {
                return ((Object[]) maybeArray)[indx];
            }

            return null;
        }

        private Set getClassesCache() {
            return (Set) getFromCache(0);
        }

        private void setClassesCache(Set s) {
            if (isSimple()) {
                // mark it as being used
                reference.caches = reference;

                return;
            }

            if (!(reference.caches instanceof Object[])) {
                reference.caches = new Object[3];
            }

            ((Object[]) reference.caches)[0] = s;
        }

        private Collection getInstancesCache() {
            return (Collection) getFromCache(1);
        }

        private void setInstancesCache(Collection c) {
            if (isSimple()) {
                // mark it as being used
                reference.caches = reference;

                return;
            }

            if (!(reference.caches instanceof Object[])) {
                reference.caches = new Object[3];
            }

            ((Object[]) reference.caches)[1] = c;
        }

        private Object[] getItemsCache() {
            return (Object[]) getFromCache(2);
        }

        private void setItemsCache(Collection c) {
            if (isSimple()) {
                // mark it as being used
                reference.caches = reference;

                return;
            }

            if (!(reference.caches instanceof Object[])) {
                reference.caches = new Object[3];
            }

            ((Object[]) reference.caches)[2] = c.toArray();
        }

        private void clearCaches() {
            if (reference.caches instanceof Object[]) {
                reference.caches = new Object[3];
            }
        }

        /** Ok, register listeners to all classes and super classes.
         */
        public synchronized void addLookupListener(LookupListener l) {
            listeners = modifyListenerList(true, l, listeners);
        }

        /** Ok, register listeners to all classes and super classes.
         */
        public synchronized void removeLookupListener(LookupListener l) {
            listeners = modifyListenerList(false, l, listeners);
        }

        /** Delete all cached values, the template changed.
         */
        public void fireStateChanged() {
            Object[] previousItems = getItemsCache();

            clearCaches();

            if (previousItems != null) {
                Object[] newArray = allItemsWithoutBeforeLookup().toArray();

                if (compareArrays(previousItems, newArray)) {
                    // do not fire any change if nothing has been changed
                    return;
                }
            }

            LookupListener[] arr;

            synchronized (this) {
                if (listeners == null) {
                    return;
                }

                if (listeners instanceof LookupListener) {
                    arr = new LookupListener[] { (LookupListener) listeners };
                } else {
                    ArrayList l = (ArrayList) listeners;
                    arr = (LookupListener[]) l.toArray(new LookupListener[l.size()]);
                }
            }

            final LookupListener[] ll = arr;
            final LookupEvent ev = new LookupEvent(this);

            for (int i = 0; i < ll.length; i++) {
                ll[i].resultChanged(ev);
            }
        }

        public Collection allInstances() {
            reference.lookup.beforeLookup(reference.template);

            Collection s = getInstancesCache();

            if (s != null) {
                return s;
            }

            Collection items = allItemsWithoutBeforeLookup();
            s = new ArrayList(items.size());

            Iterator it = items.iterator();

            while (it.hasNext()) {
                Item item = (Item) it.next();
                Object obj = item.getInstance();

                if (obj != null) {
                    s.add(obj);
                }
            }

            setInstancesCache(s);

            return s;
        }

        /** Set of all classes.
         *
         */
        public Set allClasses() {
            reference.lookup.beforeLookup(reference.template);

            Set s = getClassesCache();

            if (s != null) {
                return s;
            }

            s = new HashSet();

            Iterator it = allItemsWithoutBeforeLookup().iterator();

            while (it.hasNext()) {
                Item item = (Item) it.next();
                Class clazz = item.getType();

                if (clazz != null) {
                    s.add(clazz);
                }
            }

            setClassesCache(s);

            return s;
        }

        /** Items are stored directly in the allItems.
         */
        public Collection allItems() {
            reference.lookup.beforeLookup(reference.template);

            return allItemsWithoutBeforeLookup();
        }

        /** Implements the search for allItems, but without asking for before lookup */
        private Collection allItemsWithoutBeforeLookup() {
            Object[] c = getItemsCache();

            if (c != null) {
                return Collections.unmodifiableList(Arrays.asList(c));
            }

            ArrayList saferCheck = null;
            AbstractLookup.Storage t = reference.lookup.enterStorage();

            try {
                try {
                    return Collections.unmodifiableCollection(initItems(t));
                } catch (AbstractLookup.ISE ex) {
                    // do less effective evaluation of items outside of the 
                    // locked storage
                    saferCheck = new ArrayList();

                    Enumeration en = t.lookup(null); // get all Pairs

                    while (en.hasMoreElements()) {
                        Pair i = (Pair) en.nextElement();
                        ;
                        saferCheck.add(i);
                    }
                }
            } finally {
                reference.lookup.exitStorage();
            }

            Iterator it = saferCheck.iterator();

            // InheritanceTree is comparator for AbstractLookup.Pairs
            TreeSet items = new TreeSet(ALPairComparator.DEFAULT);

            while (it.hasNext()) {
                Pair i = (Pair) it.next();

                if (matches(reference.template, i, false)) {
                    items.add(i);
                }
            }

            return Collections.unmodifiableCollection(items);
        }

        /** Initializes items.
         */
        private Collection initItems(Storage t) {
            // manipulation with the tree must be synchronized
            Enumeration en = t.lookup(reference.template.getType());

            // InheritanceTree is comparator for AbstractLookup.Pairs
            TreeSet items = new TreeSet(ALPairComparator.DEFAULT);

            while (en.hasMoreElements()) {
                Pair i = (Pair) en.nextElement();

                if (matches(reference.template, i, false)) {
                    items.add(i);
                }
            }

            // create a correctly sorted copy using the tree as the comparator
            setItemsCache(items);

            return items;
        }

        /** Used by proxy results to synchronize before lookup.
         */
        protected void beforeLookup(Lookup.Template t) {
            if (t.getType() == reference.template.getType()) {
                reference.lookup.beforeLookup(t);
            }
        }

        /* Do not need to implement it, the default way is ok.
        public boolean equals(java.lang.Object obj) {
            return obj == this;
        }
        */
        public String toString() {
            return super.toString() + " for " + reference.template;
        }
    }
     // end of R

    /** A class that can be used by the creator of the AbstractLookup to
     * control its content. It can be passed to AbstractLookup constructor
     * and used to add and remove pairs.
     *
     * @since 1.25
     */
    public static class Content extends Object implements Serializable {
        private static final long serialVersionUID = 1L;

        // one of them is always null (except attach stage)

        /** abstract lookup we are connected to */
        private AbstractLookup al = null;
        private transient ArrayList earlyPairs;

        /** A lookup attaches to this object.
         */
        final synchronized void attach(AbstractLookup al) {
            if (this.al == null) {
                this.al = al;

                if (earlyPairs != null) {
                    // we must just add no override!
                    Pair[] p = (Pair[]) earlyPairs.toArray(new Pair[earlyPairs.size()]);

                    for (int i = 0; i < p.length; i++) {
                        addPair(p[i]);
                    }
                }

                earlyPairs = null;
            } else {
                throw new IllegalStateException(
                    "Trying to use content for " + al + " but it is already used for " + this.al
                ); // NOI18N
            }
        }

        /** The method to add instance to the lookup with.
         * @param pair class/instance pair
         */
        public final void addPair(Pair pair) {
            AbstractLookup a = al;

            if (a != null) {
                a.addPair(pair);
            } else {
                if (earlyPairs == null) {
                    earlyPairs = new ArrayList(3);
                }

                earlyPairs.add(pair);
            }
        }

        /** Remove instance.
         * @param pair class/instance pair
         */
        public final void removePair(Pair pair) {
            AbstractLookup a = al;

            if (a != null) {
                a.removePair(pair);
            } else {
                if (earlyPairs == null) {
                    earlyPairs = new ArrayList(3);
                }

                earlyPairs.remove(pair);
            }
        }

        /** Changes all pairs in the lookup to new values.
         * @param c the collection of (Pair) objects
         */
        public final void setPairs(Collection c) {
            AbstractLookup a = al;

            if (a != null) {
                a.setPairs(c);
            } else {
                earlyPairs = new ArrayList(c);
            }
        }
    }
     // end of Content

    /** Just a holder for index & modified values.
     */
    final static class Info extends Object {
        public int index;
        public Object transaction;

        public Info(int i, Object t) {
            index = i;
            transaction = t;
        }
    }

    /** Reference to a result R
     */
    static final class ReferenceToResult extends WeakReference implements Runnable {
        /** next refernece in chain, modified only from AbstractLookup or this */
        private ReferenceToResult next;

        /** the template for the result */
        public final Template template;

        /** the lookup we are attached to */
        public final AbstractLookup lookup;

        /** caches for results */
        public Object caches;

        /** Creates a weak refernece to a new result R in context of lookup
         * for given template
         */
        private ReferenceToResult(R result, AbstractLookup lookup, Template template) {
            super(result, activeQueue());
            this.template = template;
            this.lookup = lookup;
            getResult().reference = this;
        }

        /** Returns the result or null
         */
        R getResult() {
            return (R) get();
        }

        /** Cleans the reference. Implements Runnable interface, do not call
         * directly.
         */
        public void run() {
            lookup.cleanUpResult(this.template);
        }

        /** Clones the reference list to given Storage.
         * @param storage storage to clone to
         */
        public void cloneList(AbstractLookup.Storage storage) {
            ReferenceIterator it = new ReferenceIterator(this);

            while (it.next()) {
                ReferenceToResult current = it.current();
                ReferenceToResult newRef = new ReferenceToResult(current.getResult(), current.lookup, current.template);
                newRef.next = storage.registerReferenceToResult(newRef);
                newRef.caches = current.caches;

                if (current.caches == current) {
                    current.getResult().initItems(storage);
                }
            }
        }
    }
     // end of ReferenceToResult

    /** Supporting class to iterate over linked list of ReferenceToResult
     * Use:
     * <PRE>
     *  ReferenceIterator it = new ReferenceIterator (this.ref);
     *  while (it.next ()) {
     *    it.current (): // do some work
     *  }
     *  this.ref = it.first (); // remember the first one
     */
    static final class ReferenceIterator extends Object {
        private ReferenceToResult first;
        private ReferenceToResult current;

        /** hard reference to current result, so it is not GCed meanwhile */
        private R currentResult;

        /** Initializes the iterator with first reference.
         */
        public ReferenceIterator(ReferenceToResult first) {
            this.first = first;
        }

        /** Moves the current to next possition */
        public boolean next() {
            ReferenceToResult prev;
            ReferenceToResult ref;

            if (current == null) {
                ref = first;
                prev = null;
            } else {
                prev = current;
                ref = current.next;
            }

            while (ref != null) {
                R result = (R) ref.get();

                if (result == null) {
                    if (prev == null) {
                        // move the head
                        first = ref.next;
                    } else {
                        // skip over this reference
                        prev.next = ref.next;
                    }

                    prev = ref;
                    ref = ref.next;
                } else {
                    // we have found next item
                    currentResult = result;
                    current = ref;

                    return true;
                }
            }

            currentResult = null;
            current = null;

            return false;
        }

        /** Access to current reference.
         */
        public ReferenceToResult current() {
            return current;
        }

        /** Access to reference that is supposed to be the first one.
         */
        public ReferenceToResult first() {
            return first;
        }
    }

    /** Signals that a lookup is being modified from a lookup query.
     *
     * @author  Jaroslav Tulach
     */
    static final class ISE extends IllegalStateException {
        /** list of jobs to execute. */
        private java.util.List jobs;

        /** @param msg message
         */
        public ISE(String msg) {
            super(msg);
        }

        /** Registers a job to be executed partially out and partially in
         * the lock over storage.
         */
        public void registerJob(Job job) {
            if (jobs == null) {
                jobs = new java.util.ArrayList();
            }

            jobs.add(job);
        }

        /** Executes the jobs outside, and then inside a locked session.
         */
        public void recover(AbstractLookup lookup) {
            if (jobs == null) {
                // no recovery plan, throw itself
                throw this;
            }

            for (java.util.Iterator it = jobs.iterator(); it.hasNext();) {
                Job j = (Job) it.next();
                j.before();
            }

            AbstractLookup.Storage s = lookup.enterStorage();

            try {
                for (java.util.Iterator it = jobs.iterator(); it.hasNext();) {
                    Job j = (Job) it.next();
                    j.inside();
                }
            } finally {
                lookup.exitStorage();
            }
        }

        /** A job to be executed partially outside and partially inside
         * the storage lock.
         */
        static interface Job {
            public void before();

            public void inside();
        }
    }
     // end of ISE
}

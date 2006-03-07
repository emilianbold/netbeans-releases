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

import java.lang.ref.WeakReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import java.lang.ref.Reference;

import java.util.*;

import javax.swing.event.EventListenerList;


/** Implementation of lookup that can delegate to others.
 *
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public class ProxyLookup extends Lookup {
    /** empty array of lookups for potential use */
    private static final Lookup[] EMPTY_ARR = new Lookup[0];
    
    /** lookups to delegate to (either Lookup or array of Lookups) */
    private Object lookups;

    /** map of templates to currently active results */
    private HashMap results;

    /** Create a proxy to some other lookups.
     * @param lookups the initial delegates
     */
    public ProxyLookup(Lookup[] lookups) {
        this.setLookupsNoFire(lookups);
    }

    /**
     * Create a lookup initially proxying to no others.
     * Permits serializable subclasses.
     * @since 3.27
     */
    protected ProxyLookup() {
        this(EMPTY_ARR);
    }

    public String toString() {
        return "ProxyLookup(class=" + getClass() + ")->" + Arrays.asList(getLookups(false)); // NOI18N
    }

    /** Getter for the delegates.
    * @return the array of lookups we delegate to
    * @since 1.19
    */
    protected final Lookup[] getLookups() {
        return getLookups(true);
    }

    /** getter for the delegates, that can but need not do a clone.
     * @param clone true if clone of internal array is requested
     */
    private final Lookup[] getLookups(boolean clone) {
        Object l = this.lookups;
        if (l instanceof Lookup) {
            return new Lookup[] { (Lookup)l };
        } else {
            Lookup[] arr = (Lookup[])l;
            if (clone) {
                arr = (Lookup[])arr.clone();
            }
            return arr;
        }
    }
    
    /** Called from setLookups and constructor. 
     * @param lookups the lookups to setup
     */
    private void setLookupsNoFire(Lookup[] lookups) {
        if (lookups.length == 1) {
            this.lookups = lookups[0];
            assert this.lookups != null : "Cannot assign null delegate";
        } else {
            if (lookups.length == 0) {
                this.lookups = EMPTY_ARR;
            } else {
                this.lookups = lookups.clone();
            }
        }
    }

    /** Change the delegates. To forbid anybody else then the creator
     * of the lookup to change the delegates, this method is protected.
     *
     * @param lookups the new lookups to delegate to
     * @since 1.19 protected
     */
    protected final void setLookups(Lookup[] lookups) {
        Reference[] arr;
        HashSet newL;
        HashSet current;
        Lookup[] old;

        synchronized (this) {
            old = getLookups(false);
            current = new HashSet(Arrays.asList(old));
            newL = new HashSet(Arrays.asList(lookups));

            setLookupsNoFire(lookups);
            
            if ((results == null) || results.isEmpty()) {
                // no affected results => exit
                return;
            }

            arr = (Reference[]) results.values().toArray(new Reference[0]);

            HashSet removed = new HashSet(current);
            removed.removeAll(newL); // current contains just those lookups that have disappeared
            newL.removeAll(current); // really new lookups

            if (removed.isEmpty() && newL.isEmpty()) {
                // no need to notify changes
                return;
            }

            for (int i = 0; i < arr.length; i++) {
                R r = (R) arr[i].get();

                if (r != null) {
                    r.lookupChange(newL, removed, old, lookups);
                }
            }
        }

        // this cannot be done from the synchronized block
        ArrayList evAndListeners = new ArrayList();
        for (int i = 0; i < arr.length; i++) {
            R r = (R) arr[i].get();

            if (r != null) {
                r.collectFires(evAndListeners);
            }
        }
        
        {
            Iterator it = evAndListeners.iterator();
            while (it.hasNext()) {
                LookupEvent ev = (LookupEvent)it.next();
                LookupListener l = (LookupListener)it.next();
                l.resultChanged(ev);
            }
        }
    }

    /** Notifies subclasses that a query is about to be processed.
     * Subclasses can update its state before the actual processing
     * begins. It is allowed to call <code>setLookups</code> method
     * to change/update the set of objects the proxy delegates to.
     *
     * @param template the template of the query
     * @since 1.31
     */
    protected void beforeLookup(Template template) {
    }

    public final Object lookup(Class clazz) {
        beforeLookup(new Template(clazz));

        Lookup[] lookups = this.getLookups(false);

        for (int i = 0; i < lookups.length; i++) {
            Object o = lookups[i].lookup(clazz);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    public final Item lookupItem(Template template) {
        beforeLookup(template);

        Lookup[] lookups = this.getLookups(false);

        for (int i = 0; i < lookups.length; i++) {
            Item o = lookups[i].lookupItem(template);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    public final synchronized Result lookup(Lookup.Template template) {
        R r;

        if (results != null) {
            Reference ref = (Reference) results.get(template);
            r = (ref == null) ? null : (R) ref.get();

            if (r != null) {
                return r;
            }
        } else {
            results = new HashMap();
        }

        r = new R(template);
        results.put(template, new java.lang.ref.SoftReference(r));

        return r;
    }

    /** Unregisters a template from the has map.
     */
    private final synchronized void unregisterTemplate(Template template) {
        if (results == null) {
            return;
        }

        Reference ref = (Reference) results.remove(template);

        if ((ref != null) && (ref.get() != null)) {
            // seems like there is a reference to a result for this template
            // thta is still alive
            results.put(template, ref);
        }
    }

    /** Result of a lookup request. Allows access to single object
     * that was found (not too useful) and also to all objects found
     * (more useful).
     */
    private final class R extends WaitableResult {
        /** list of listeners added */
        private javax.swing.event.EventListenerList listeners;

        /** template for this result */
        private Lookup.Template template;

        /** collection of Objects */
        private Collection[] cache;

        /** weak listener & result */
        private WeakResult weakL;

        /** Constructor.
         */
        public R(Lookup.Template t) {
            template = t;
            weakL = new WeakResult(this);
        }

        /** When garbage collected, remove the template from the has map.
         */
        protected void finalize() {
            unregisterTemplate(template);
        }

        /** initializes the results
         */
        private Result[] initResults() {
            synchronized (this) {
                if (weakL.results != null) {
                    return weakL.results;
                }
            }

            Lookup[] myLkps = getLookups(false);
            Result[] arr = new Result[myLkps.length];

            for (int i = 0; i < arr.length; i++) {
                arr[i] = myLkps[i].lookup(template);
            }

            synchronized (this) {
                // some other thread might compute the result mean while. 
                // if not finish the computation yourself
                if (weakL.results != null) {
                    return weakL.results;
                }

                for (int i = 0; i < arr.length; i++) {
                    arr[i].addLookupListener(weakL);
                }

                weakL.results = arr;

                return arr;
            }
        }

        /** Called when there is a change in the list of proxied lookups.
         * @param added set of added lookups
         * @param remove set of removed lookups
         * @param current array of current lookups
         */
        protected void lookupChange(Set added, Set removed, Lookup[] old, Lookup[] current) {
            synchronized (this) {
                if (weakL.results == null) {
                    // not computed yet, do not need to do anything
                    return;
                }

                // map (Lookup, Lookup.Result)
                HashMap map = new HashMap(old.length * 2);

                for (int i = 0; i < old.length; i++) {
                    if (removed.contains(old[i])) {
                        // removed lookup
                        weakL.results[i].removeLookupListener(weakL);
                    } else {
                        // remember the association
                        map.put(old[i], weakL.results[i]);
                    }
                }

                Lookup.Result[] arr = new Lookup.Result[current.length];

                for (int i = 0; i < current.length; i++) {
                    if (added.contains(current[i])) {
                        // new lookup
                        arr[i] = current[i].lookup(template);
                        arr[i].addLookupListener(weakL);
                    } else {
                        // old lookup
                        arr[i] = (Lookup.Result) map.get(current[i]);

                        if (arr[i] == null) {
                            // assert
                            throw new IllegalStateException();
                        }
                    }
                }

                // remember the new results
                weakL.results = arr;
            }
        }

        /** Just delegates.
         */
        public void addLookupListener(LookupListener l) {
            if (listeners == null) {
                synchronized (this) {
                    if (listeners == null) {
                        listeners = new EventListenerList();
                    }
                }
            }

            listeners.add(LookupListener.class, l);
        }

        /** Just delegates.
         */
        public void removeLookupListener(LookupListener l) {
            if (listeners != null) {
                listeners.remove(LookupListener.class, l);
            }
        }

        /** Access to all instances in the result.
         * @return collection of all instances
         */
        public java.util.Collection allInstances() {
            return computeResult(0);
        }

        /** Classes of all results. Set of the most concreate classes
         * that are registered in the system.
         * @return set of Class objects
         */
        public java.util.Set allClasses() {
            return (java.util.Set) computeResult(1);
        }

        /** All registered items. The collection of all pairs of
         * ii and their classes.
         * @return collection of Lookup.Item
         */
        public java.util.Collection allItems() {
            return computeResult(2);
        }

        /** Computes results from proxied lookups.
         * @param indexToCache 0 = allInstances, 1 = allClasses, 2 = allItems
         * @return the collection or set of the objects
         */
        private java.util.Collection computeResult(int indexToCache) {
            // results to use
            Lookup.Result[] arr = myBeforeLookup();

            // if the call to beforeLookup resulted in deletion of caches
            synchronized (this) {
                if (cache != null) {
                    Collection result = cache[indexToCache];
                    if (result != null) {
                        return result;
                    }
                }
            }

            // initialize the collection to hold result
            Collection compute;
            Collection ret;

            if (indexToCache == 1) {
                compute = new HashSet();
                ret = Collections.unmodifiableSet((Set)compute);
            } else {
                compute = new ArrayList(arr.length * 2);
                ret = Collections.unmodifiableList((List)compute);
            }

            // fill the collection
            for (int i = 0; i < arr.length; i++) {
                switch (indexToCache) {
                case 0:
                    compute.addAll(arr[i].allInstances());
                    break;
                case 1:
                    compute.addAll(arr[i].allClasses());
                    break;
                case 2:
                    compute.addAll(arr[i].allItems());
                    break;
                default:
                    assert false : "Wrong index: " + indexToCache;
                }
            }
            
            

            synchronized (this) {
                if (cache == null) {
                    // initialize the cache to indicate this result is in use
                    cache = new Collection[3];
                }
                
                if (arr == weakL.results) {
                    // updates the results, if the results have not been
                    // changed during the computation of allInstances
                    cache[indexToCache] = ret;
                }
            }

            return ret;
        }

        /** When the result changes, fire the event.
         */
        public void resultChanged(LookupEvent ev) {
            collectFires(null);
        }
        
        protected void collectFires(Collection evAndListeners) {
            // clear cached instances
            Collection oldItems;
            Collection oldInstances;
            synchronized (this) {
                if (cache == null) {
                    // nobody queried the result yet
                    return;
                }
                oldInstances = cache[0];
                oldItems = cache[2];
                

                if (listeners == null || listeners.getListenerCount() == 0) {
                    // clear the cache
                    cache = new Collection[3];
                    return;
                }
                
                // ignore events if they arrive as a result of call to allItems
                // or allInstances, bellow...
                cache = null;
            }

            boolean modified = true;

            if (oldItems != null) {
                Collection newItems = allItems();
                if (oldItems.equals(newItems)) {
                    modified = false;
                }
            } else {
                if (oldInstances != null) {
                    Collection newInstances = allInstances();
                    if (oldInstances.equals(newInstances)) {
                        modified = false;
                    }
                } else {
                    synchronized (this) {
                        if (cache == null) {
                            // we have to initialize the cache
                            // to show that the result has been initialized
                            cache = new Collection[3];
                        }
                    }
                }
            }
            
            assert cache != null;

            if (modified) {
                LookupEvent ev = new LookupEvent(this);
                AbstractLookup.notifyListeners(listeners.getListenerList(), ev, evAndListeners);
            }
        }

        /** Implementation of my before lookup.
         * @return results to work on.
         */
        private Lookup.Result[] myBeforeLookup() {
            ProxyLookup.this.beforeLookup(template);

            Lookup.Result[] arr = initResults();

            // invoke update on the results
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof WaitableResult) {
                    WaitableResult w = (WaitableResult) arr[i];
                    w.beforeLookup(template);
                }
            }

            return arr;
        }

        /** Used by proxy results to synchronize before lookup.
         */
        protected void beforeLookup(Lookup.Template t) {
            if (t.getType() == template.getType()) {
                myBeforeLookup();
            }
        }
    }
    private static final class WeakResult extends WaitableResult implements LookupListener {
        /** all results */
        private Lookup.Result[] results;

        private Reference result;
        
        public WeakResult(R r) {
            this.result = new WeakReference(r);
        }
        
        protected void beforeLookup(Lookup.Template t) {
            R r = (R)result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                removeListeners();
            }
        }

        private void removeListeners() {
            Lookup.Result[] arr = this.results;
            if (arr == null) {
                return;
            }

            for(int i = 0; i < arr.length; i++) {
                arr[i].removeLookupListener(this);
            }
        }

        protected void collectFires(Collection evAndListeners) {
            R r = (R)result.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            } else {
                removeListeners();
            }
        }

        public void addLookupListener(LookupListener l) {
            assert false;
        }

        public void removeLookupListener(LookupListener l) {
            assert false;
        }

        public Collection allInstances() {
            assert false;
            return null;
        }

        public void resultChanged(LookupEvent ev) {
            R r = (R)result.get();
            if (r != null) {
                r.resultChanged(ev);
            } else {
                removeListeners();
            }
        }

        public Collection allItems() {
            assert false;
            return null;
        }

        public Set allClasses() {
            assert false;
            return null;
        }
    } // end of WeakResult
}

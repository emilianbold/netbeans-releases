/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.util.lookup;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.EventListenerList;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/** Implementation of lookup that can delegate to others.
 *
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public class ProxyLookup extends Lookup {
    /** empty array of lookups for potential use */
    static final Lookup[] EMPTY_ARR = new Lookup[0];

    /** lookups to delegate to (either Lookup or array of Lookups) */
    private Object lookups;

    /** data representing the state of the lookup */
    private ImmutableInternalData data;

    /** Create a proxy to some other lookups.
     * @param lookups the initial delegates
     */
    public ProxyLookup(Lookup... lookups) {
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

    @Override
    public String toString() {
        return "ProxyLookup(class=" + getClass() + ")->" + Arrays.asList(getLookups(false)); // NOI18N
    }

    /** Getter for the delegates.
    * @return the array of lookups we delegate to
    * @since 1.19
    */
    protected final Lookup[] getLookups() {
        synchronized (ProxyLookup.this) {
            return getLookups(true);
        }
    }

    /** getter for the delegates, that can but need not do a clone.
     * @param clone true if clone of internal array is requested
     */
    private final Lookup[] getLookups(boolean clone) {
        assert Thread.holdsLock(ProxyLookup.this);
        Object l = this.lookups;
        if (l instanceof Lookup) {
            return new Lookup[] { (Lookup)l };
        } else {
            Lookup[] arr = (Lookup[])l;
            if (clone) {
                arr = arr.clone();
            }
            return arr;
        }
    }

    private Set<Lookup> identityHashSet(Collection<Lookup> current) {
        Map<Lookup,Void> map = new IdentityHashMap<Lookup, Void>();
        for (Lookup lookup : current) {
            map.put(lookup, null);
        }
        return map.keySet();
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

    /**
     * Changes the delegates.
     *
     * @param lookups the new lookups to delegate to
     * @since 1.19 protected
     */
    protected final void setLookups(Lookup... lookups) {
        Collection<Reference<R>> arr;
        Set<Lookup> newL;
        Set<Lookup> current;
        Lookup[] old;
        
        Map<Result,LookupListener> toRemove = new IdentityHashMap<Lookup.Result, LookupListener>();
        Map<Result,LookupListener> toAdd = new IdentityHashMap<Lookup.Result, LookupListener>();
        
        synchronized (ProxyLookup.this) {
            old = getLookups(false);
            current = identityHashSet(Arrays.asList(old));
            newL = identityHashSet(Arrays.asList(lookups));

            setLookupsNoFire(lookups);
            
            if (getData() == null) {
                // no affected results => exit
                return;
            }

            arr = getData().references();

            Set<Lookup> removed = identityHashSet(current);
            removed.removeAll(newL); // current contains just those lookups that have disappeared
            newL.removeAll(current); // really new lookups

            if (removed.isEmpty() && newL.isEmpty()) {
                // no need to notify changes
                return;
            }

            for (Reference<R> ref : arr) {
                R<?> r = ref.get();
                if (r != null) {
                    r.lookupChange(newL, removed, old, lookups, toAdd, toRemove);
                }
            }
        }
        
        // better to do this later than in synchronized block
        for (Map.Entry<Result, LookupListener> e : toRemove.entrySet()) {
            e.getKey().removeLookupListener(e.getValue());
        }
        for (Map.Entry<Result, LookupListener> e : toAdd.entrySet()) {
            e.getKey().addLookupListener(e.getValue());
        }


        // this cannot be done from the synchronized block
        ArrayList<Object> evAndListeners = new ArrayList<Object>();
        for (Reference<R> ref : arr) {
            R<?> r = ref.get();
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
    protected void beforeLookup(Template<?> template) {
    }

    public final <T> T lookup(Class<T> clazz) {
        beforeLookup(new Template<T>(clazz));

        Lookup[] tmpLkps;
        synchronized (ProxyLookup.this) {
            tmpLkps = this.getLookups(false);
        }

        for (int i = 0; i < tmpLkps.length; i++) {
            T o = tmpLkps[i].lookup(clazz);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    @Override
    public final <T> Item<T> lookupItem(Template<T> template) {
        beforeLookup(template);

        Lookup[] tmpLkps; 
        synchronized (ProxyLookup.this) {
            tmpLkps = this.getLookups(false);
        }

        for (int i = 0; i < tmpLkps.length; i++) {
            Item<T> o = tmpLkps[i].lookupItem(template);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> R<T> convertResult(R r) {
        return (R<T>)r;
    }

    public final <T> Result<T> lookup(Lookup.Template<T> template) {
        synchronized (ProxyLookup.this) {
            ImmutableInternalData[] res = { data };
            R<T> newR = ImmutableInternalData.findResult(this, res, template);
            setData(res[0]);
            return newR;
        }
    }

    /** Unregisters a template from the has map.
     */
    private final void unregisterTemplate(Template<?> template) {
        synchronized (ProxyLookup.this) {
            ImmutableInternalData id = getData();
            if (id == null) {
                return;
            }
            setData(id.removeTemplate(this, template));
        }
    }

    private ImmutableInternalData getData() {
        return data;
    }

    private void setData(ImmutableInternalData data) {
        assert Thread.holdsLock(ProxyLookup.this);
        this.data = data;
    }

    /** Result of a lookup request. Allows access to single object
     * that was found (not too useful) and also to all objects found
     * (more useful).
     */
    private final class R<T> extends WaitableResult<T> {
        /** list of listeners added */
        private javax.swing.event.EventListenerList listeners;

        /** template for this result */
        private final Lookup.Template<T> template;

        /** collection of Objects */
        private Collection[] cache;

        /** weak listener & result */
        private final WeakResult<T> weakL;

        /** Constructor.
         */
        public R(Lookup.Template<T> t) {
            template = t;
            weakL = new WeakResult<T>(this);
        }

        /** When garbage collected, remove the template from the has map.
         */
        @Override
        protected void finalize() {
            unregisterTemplate(template);
        }

        @SuppressWarnings("unchecked")
        private Result<T>[] newResults(int len) {
            return new Result[len];
        }

        /** initializes the results
         */
        private Result<T>[] initResults() {
            BIG_LOOP: for (;;) {
                Lookup[] myLkps;
                synchronized (ProxyLookup.this) {
                    if (weakL.getResults() != null) {
                        return weakL.getResults();
                    }
                    myLkps = getLookups(false);
                }

                Result<T>[] arr = newResults(myLkps.length);

                for (int i = 0; i < arr.length; i++) {
                    arr[i] = myLkps[i].lookup(template);
                }

                synchronized (ProxyLookup.this) {
                    Lookup[] currentLkps = getLookups(false);
                    if (currentLkps.length != myLkps.length) {
                        continue BIG_LOOP;
                    }
                    for (int i = 0; i < currentLkps.length; i++) {
                        if (currentLkps[i] != myLkps[i]) {
                            continue BIG_LOOP;
                        }
                    }
                    
                    // some other thread might compute the result mean while. 
                    // if not finish the computation yourself
                    if (weakL.getResults() != null) {
                        return weakL.getResults();
                    }

                    for (int i = 0; i < arr.length; i++) {
                        arr[i].addLookupListener(weakL);
                    }

                    weakL.setResults(arr);

                    return arr;
                }
            }
        }

        /** Called when there is a change in the list of proxied lookups.
         * @param added set of added lookups
         * @param remove set of removed lookups
         * @param current array of current lookups
         */
        protected void lookupChange(
            Set<Lookup> added, Set<Lookup> removed, Lookup[] old, Lookup[] current, 
            Map<Result,LookupListener> toAdd, Map<Result,LookupListener> toRemove
        ) {
            synchronized (ProxyLookup.this) {
                if (weakL.getResults() == null) {
                    // not computed yet, do not need to do anything
                    return;
                }

                // map (Lookup, Lookup.Result)
                Map<Lookup,Result<T>> map = new IdentityHashMap<Lookup,Result<T>>(old.length * 2);

                for (int i = 0; i < old.length; i++) {
                    if (removed.contains(old[i])) {
                        // removed lookup
                        toRemove.put(weakL.getResults()[i], weakL);
                    } else {
                        // remember the association
                        map.put(old[i], weakL.getResults()[i]);
                    }
                }

                Lookup.Result<T>[] arr = newResults(current.length);

                for (int i = 0; i < current.length; i++) {
                    if (added.contains(current[i])) {
                        // new lookup
                        arr[i] = current[i].lookup(template);
                        toAdd.put(arr[i], weakL);
                    } else {
                        // old lookup
                        arr[i] = map.get(current[i]);

                        if (arr[i] == null) {
                            // assert
                            throw new IllegalStateException();
                        }
                    }
                }

                // remember the new results
                weakL.setResults(arr);
            }
        }

        /** Just delegates.
         */
        public void addLookupListener(LookupListener l) {
            synchronized (ProxyLookup.this) {
                if (listeners == null) {
                    listeners = new EventListenerList();
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
        @SuppressWarnings("unchecked")
        public java.util.Collection<T> allInstances() {
            return computeResult(0);
        }

        /** Classes of all results. Set of the most concreate classes
         * that are registered in the system.
         * @return set of Class objects
         */
        @SuppressWarnings("unchecked")
        @Override
        public java.util.Set<Class<? extends T>> allClasses() {
            return (java.util.Set<Class<? extends T>>) computeResult(1);
        }

        /** All registered items. The collection of all pairs of
         * ii and their classes.
         * @return collection of Lookup.Item
         */
        @SuppressWarnings("unchecked")
        @Override
        public java.util.Collection<? extends Item<T>> allItems() {
            return computeResult(2);
        }

        /** Computes results from proxied lookups.
         * @param indexToCache 0 = allInstances, 1 = allClasses, 2 = allItems
         * @return the collection or set of the objects
         */
        private java.util.Collection computeResult(int indexToCache) {
            // results to use
            Lookup.Result<T>[] arr = myBeforeLookup();

            // if the call to beforeLookup resulted in deletion of caches
            synchronized (ProxyLookup.this) {
                if (getCache() != null) {
                    Collection result = getCache()[indexToCache];
                    if (result != null) {
                        return result;
                    }
                }
            }

            // initialize the collection to hold result
            Collection<Object> compute;
            Collection<Object> ret;

            if (indexToCache == 1) {
                HashSet<Object> s = new HashSet<Object>();
                compute = s;
                ret = Collections.unmodifiableSet(s);
            } else {
                List<Object> l = new ArrayList<Object>(arr.length * 2);
                compute = l;
                ret = Collections.unmodifiableList(l);
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
            
            

            synchronized (ProxyLookup.this) {
                if (getCache() == null) {
                    // initialize the cache to indicate this result is in use
                    setCache(new Collection[3]);
                }
                
                if (arr == weakL.getResults()) {
                    // updates the results, if the results have not been
                    // changed during the computation of allInstances
                    getCache()[indexToCache] = ret;
                }
            }

            return ret;
        }

        /** When the result changes, fire the event.
         */
        public void resultChanged(LookupEvent ev) {
            collectFires(null);
        }
        
        protected void collectFires(Collection<Object> evAndListeners) {
            // clear cached instances
            Collection oldItems;
            Collection oldInstances;
            synchronized (ProxyLookup.this) {
                if (getCache() == null) {
                    // nobody queried the result yet
                    return;
                }
                oldInstances = getCache()[0];
                oldItems = getCache()[2];
                

                if (listeners == null || listeners.getListenerCount() == 0) {
                    // clear the cache
                    setCache(new Collection[3]);
                    return;
                }
                
                // ignore events if they arrive as a result of call to allItems
                // or allInstances, bellow...
                setCache(null);
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
                    synchronized (ProxyLookup.this) {
                        if (getCache() == null) {
                            // we have to initialize the cache
                            // to show that the result has been initialized
                            setCache(new Collection[3]);
                        }
                    }
                }
            }
            
            if (modified) {
                LookupEvent ev = new LookupEvent(this);
                AbstractLookup.notifyListeners(listeners.getListenerList(), ev, evAndListeners);
            }
        }

        /** Implementation of my before lookup.
         * @return results to work on.
         */
        private Lookup.Result<T>[] myBeforeLookup() {
            ProxyLookup.this.beforeLookup(template);

            Lookup.Result<T>[] arr = initResults();

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

        private Collection[] getCache() {
            return cache;
        }

        private void setCache(Collection[] cache) {
            assert Thread.holdsLock(ProxyLookup.this);
            this.cache = cache;
        }
    }
    private static final class WeakResult<T> extends WaitableResult<T> implements LookupListener, Runnable {
        /** all results */
        private Lookup.Result<T>[] results;

        private final Reference<R> result;
        
        public WeakResult(R r) {
            this.result = new WeakReference<R>(r);//, Utilities.activeReferenceQueue());
        }
        
        protected void beforeLookup(Lookup.Template t) {
            R r = result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                removeListeners();
            }
        }

        private void removeListeners() {
            Lookup.Result<T>[] arr = this.getResults();
            if (arr == null) {
                return;
            }

            for(int i = 0; i < arr.length; i++) {
                arr[i].removeLookupListener(this);
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            R<?> r = result.get();
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

        public Collection<T> allInstances() {
            assert false;
            return null;
        }

        public void resultChanged(LookupEvent ev) {
            R r = result.get();
            if (r != null) {
                r.resultChanged(ev);
            } else {
                removeListeners();
            }
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            assert false;
            return null;
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            assert false;
            return null;
        }

        public void run() {
            removeListeners();
        }

        private Lookup.Result<T>[] getResults() {
            return results;
        }

        private void setResults(Lookup.Result<T>[] results) {
            this.results = results;
        }
    } // end of WeakResult
    private static final class ImmutableInternalData extends Object {
        /** map of templates to currently active results */
        private final HashMap<Template<?>,Reference<R>> results;

        public ImmutableInternalData(HashMap<Template<?>, Reference<ProxyLookup.R>> results) {
            this.results = results;
        }

        final Collection<Reference<R>> references() {
            return results.values();
        }
        
        final <T> ImmutableInternalData removeTemplate(ProxyLookup proxy, Template<T> template) {
            if (results.containsKey(template)) {
                HashMap<Template<?>,Reference<R>> c = new HashMap<Lookup.Template<?>, Reference<ProxyLookup.R>>(results);
                Reference<R> ref = c.remove(template);
                if (ref != null && ref.get() != null) {
                    // seems like there is a reference to a result for this template
                    // thta is still alive
                    return this;
                }
                return new ImmutableInternalData(c);
            } else {
                return this;
            }
        }


        static <T> R<T> findResult(ProxyLookup proxy, ImmutableInternalData[] oldAndNew, Template<T> template) {
            assert Thread.holdsLock(proxy);
            
            if (oldAndNew[0] != null) {
                Reference<R> ref = oldAndNew[0].results.get(template);
                R r = (ref == null) ? null : ref.get();

                if (r != null) {
                    return convertResult(r);
                }
            }
            
            HashMap<Template<?>, Reference<R>> res;
            if (oldAndNew[0] == null) {
                res = new HashMap<Template<?>, Reference<R>>();
            } else {
                res = new HashMap<Template<?>, Reference<R>>(oldAndNew[0].results);
            }
            
            R<T> newR = proxy.new R<T>(template);
            res.put(template, new java.lang.ref.SoftReference<R>(newR));
            oldAndNew[0] = new ImmutableInternalData(res);
            return newR;
        }
    }
}

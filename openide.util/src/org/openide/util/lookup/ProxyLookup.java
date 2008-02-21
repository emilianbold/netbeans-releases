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
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.event.EventListenerList;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Implementation of lookup that can delegate to others.
 *
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public class ProxyLookup extends Lookup {
    /** data representing the state of the lookup */
    private ImmutableInternalData data;

    /** Create a proxy to some other lookups.
     * @param lookups the initial delegates
     */
    public ProxyLookup(Lookup... lookups) {
        data = ImmutableInternalData.EMPTY.setLookupsNoFire(lookups, true);
    }

    /**
     * Create a lookup initially proxying to no others.
     * Permits serializable subclasses.
     * @since 3.27
     */
    protected ProxyLookup() {
        data = ImmutableInternalData.EMPTY;
    }

    @Override
    public synchronized String toString() {
        return "ProxyLookup(class=" + getClass() + ")->" + Arrays.asList(getData().getLookups(false)); // NOI18N
    }

    /** Getter for the delegates.
    * @return the array of lookups we delegate to
    * @since 1.19
    */
    protected final Lookup[] getLookups() {
        synchronized (ProxyLookup.this) {
            return getData().getLookups(true);
        }
    }

    private Set<Lookup> identityHashSet(Collection<Lookup> current) {
        Map<Lookup,Void> map = new IdentityHashMap<Lookup, Void>();
        for (Lookup lookup : current) {
            map.put(lookup, null);
        }
        return map.keySet();
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
        
        ImmutableInternalData orig;
        synchronized (ProxyLookup.this) {
            orig = getData();
            ImmutableInternalData newData = getData().setLookupsNoFire(lookups, false);
            if (newData == getData()) {
                return;
            }
            arr = setData(newData, lookups, toAdd, toRemove);
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
            tmpLkps = getData().getLookups(false);
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
            tmpLkps = getData().getLookups(false);
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
            ImmutableInternalData[] res = { null };
            R<T> newR = getData().findResult(this, res, template);
            setData(res[0], getData().getLookups(false), null, null);
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
            setData(id.removeTemplate(this, template), getData().getLookups(false), null, null);
        }
    }

    private ImmutableInternalData getData() {
        assert Thread.holdsLock(this);
        return data;
    }

    private Collection<Reference<R>> setData(
        ImmutableInternalData newData, Lookup[] current, 
        Map<Result,LookupListener> toAdd, Map<Result,LookupListener> toRemove
    ) {
        assert Thread.holdsLock(ProxyLookup.this);
        assert newData != null;
        
        ImmutableInternalData previous = this.getData();
        
        if (previous == newData) {
            return Collections.emptyList();
        }

        if (newData.isEmpty()) {
            this.setData(newData);
            // no affected results => exit
            return Collections.emptyList();
        }

        Collection<Reference<R>> arr = newData.references();

        Set<Lookup> removed = identityHashSet(previous.getLookupsList());
        Set<Lookup> currentSet = identityHashSet(Arrays.asList(current));
        Set<Lookup> newL = identityHashSet(currentSet);
        removed.removeAll(currentSet); // current contains just those lookups that have disappeared
        newL.removeAll(previous.getLookupsList()); // really new lookups

        for (Reference<R> ref : arr) {
            R<?> r = ref.get();
            if (r != null) {
                r.lookupChange(newData, current, previous, newL, removed, toAdd, toRemove);
                if (this.getData() != previous) {
                    // the data were changed by an re-entrant call
                    // skip any other change processing, as it is not needed
                    // anymore
                }
            }
        }
                for (Reference<R> ref : arr) {
            R<?> r = ref.get();
            if (r != null) {
                r.data = newData;
            }
        }
        this.setData(newData);
        return arr;
    }

    private void setData(ImmutableInternalData data) {
        this.data = data;
    }

    /** Result of a lookup request. Allows access to single object
     * that was found (not too useful) and also to all objects found
     * (more useful).
     */
    private static final class R<T> extends WaitableResult<T> {
        /** weak listener & result */
        private final WeakResult<T> weakL;
        
        /** list of listeners added */
        private javax.swing.event.EventListenerList listeners;

        /** collection of Objects */
        private Collection[] cache;

        
        /** associated lookup */
        private ImmutableInternalData data;

        /** Constructor.
         */
        public R(ProxyLookup proxy, Lookup.Template<T> t) {
            this.weakL = new WeakResult<T>(proxy, this, t);
        }
        
        private ProxyLookup proxy() {
            return weakL.result.proxy;
        }

        @SuppressWarnings("unchecked")
        private Result<T>[] newResults(int len) {
            return new Result[len];
        }
        
        @Override
        protected void finalize() {
            weakL.result.run();
        }

        /** initializes the results
         */
        private Result<T>[] initResults() {
            BIG_LOOP: for (;;) {
                Lookup[] myLkps;
                ImmutableInternalData current;
                synchronized (proxy()) {
                    if (weakL.getResults() != null) {
                        return weakL.getResults();
                    }
                    myLkps = data.getLookups(false);
                    current = data;
                }

                Result<T>[] arr = newResults(myLkps.length);

                for (int i = 0; i < arr.length; i++) {
                    arr[i] = myLkps[i].lookup(weakL.result.template);
                }

                synchronized (proxy()) {
                    if (current != data) {
                        continue;
                    }
                    
                    Lookup[] currentLkps = data.getLookups(false);
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
        final void lookupChange(
            ImmutableInternalData newData, Lookup[] current, ImmutableInternalData oldData,
            Set<Lookup> added, Set<Lookup> removed,
            Map<Result,LookupListener> toAdd, Map<Result,LookupListener> toRemove
        ) {
            if (weakL.getResults() == null) {
                // not computed yet, do not need to do anything
                return;
            }

            Lookup[] old = oldData.getLookups(false);

            // map (Lookup, Lookup.Result)
            Map<Lookup,Result<T>> map = new IdentityHashMap<Lookup,Result<T>>(old.length * 2);

            for (int i = 0; i < old.length; i++) {
                if (removed.contains(old[i])) {
                    // removed lookup
                    if (toRemove != null) {
                        toRemove.put(weakL.getResults()[i], weakL);
                    }
                } else {
                    // remember the association
                    map.put(old[i], weakL.getResults()[i]);
                }
            }

            Lookup.Result<T>[] arr = newResults(current.length);

            for (int i = 0; i < current.length; i++) {
                if (added.contains(current[i])) {
                    // new lookup
                    arr[i] = current[i].lookup(weakL.result.template);
                    if (toAdd != null) {
                        toAdd.put(arr[i], weakL);
                    }
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

        /** Just delegates.
         */
        public void addLookupListener(LookupListener l) {
            synchronized (proxy()) {
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
            synchronized (proxy()) {
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
            
            

            synchronized (proxy()) {
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
            synchronized (proxy()) {
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
                    synchronized (proxy()) {
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
            Template<T> template = weakL.result.template;
            
            proxy().beforeLookup(template);

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
            if (t.getType() == weakL.result.template.getType()) {
                myBeforeLookup();
            }
        }

        private Collection[] getCache() {
            return cache;
        }

        private void setCache(Collection[] cache) {
            assert Thread.holdsLock(proxy());
            this.cache = cache;
        }
    }
    private static final class WeakRef<T> extends WeakReference<R> implements Runnable {
        final WeakResult<T> result;
        final ProxyLookup proxy;
        final Template<T> template;
        
        public WeakRef(R r, WeakResult<T> result, ProxyLookup proxy, Template<T> template) {
            super(r);
            this.result = result;
            this.template = template;
            this.proxy = proxy;
        }

        public void run() {
            result.removeListeners();
            proxy.unregisterTemplate(template);
        }
    }
    
    
    private static final class WeakResult<T> extends WaitableResult<T> implements LookupListener, Runnable {
        /** all results */
        private Lookup.Result<T>[] results;
        private final WeakRef<T> result;
        
        public WeakResult(ProxyLookup proxy, R r, Template<T> t) {
            this.result = new WeakRef<T>(r, this, proxy, t);
        }
        
        final void removeListeners() {
            Lookup.Result<T>[] arr = this.getResults();
            if (arr == null) {
                return;
            }

            for(int i = 0; i < arr.length; i++) {
                arr[i].removeLookupListener(this);
            }
        }

        protected void beforeLookup(Lookup.Template t) {
            R r = result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                removeListeners();
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
    
    static abstract class ImmutableInternalData extends Object {
        static final ImmutableInternalData EMPTY = new EmptyInternalData();
        static final Lookup[] EMPTY_ARR = new Lookup[0];

        
        protected ImmutableInternalData() {
        }
        
        public static ImmutableInternalData create(Object lkp, Map<Template, Reference<R>> results) {
            if (results.size() == 0 && lkp == EMPTY_ARR) {
                return EMPTY;
            }
            if (results.size() == 1) {
                Entry<Template,Reference<R>> e = results.entrySet().iterator().next();
                return new SingleInternalData(lkp, e.getKey(), e.getValue());
            }
            
            return new RealInternalData(lkp, results);
        }

        protected abstract boolean isEmpty();
        protected abstract Map<Template, Reference<R>> getResults();
        protected abstract Object getRawLookups();

        final Collection<Reference<R>> references() {
            return getResults().values();
        }
        
        final <T> ImmutableInternalData removeTemplate(ProxyLookup proxy, Template<T> template) {
            if (getResults().containsKey(template)) {
                HashMap<Template,Reference<R>> c = new HashMap<Template, Reference<ProxyLookup.R>>(getResults());
                Reference<R> ref = c.remove(template);
                if (ref != null && ref.get() != null) {
                    // seems like there is a reference to a result for this template
                    // thta is still alive
                    return this;
                }
                return create(getRawLookups(), c);
            } else {
                return this;
            }
        }
        
        <T> R<T> findResult(ProxyLookup proxy, ImmutableInternalData[] newData, Template<T> template) {
            assert Thread.holdsLock(proxy);
            
            Map<Template,Reference<R>> map = getResults();
            
            Reference<R> ref = map.get(template);
            R r = (ref == null) ? null : ref.get();

            if (r != null) {
                newData[0] = this;
                return convertResult(r);
            }
            
            HashMap<Template, Reference<R>> res = new HashMap<Template, Reference<R>>(map);
            R<T> newR = new R<T>(proxy, template);
            res.put(template, new java.lang.ref.SoftReference<R>(newR));
            newR.data = newData[0] = create(getRawLookups(), res);
            return newR;
        }
        final ImmutableInternalData setLookupsNoFire(Lookup[] lookups, boolean skipCheck) {
            Object l;
            
            if (!skipCheck) {
                Lookup[] previous = getLookups(false);
                if (previous == lookups) {
                    return this;
                }
            
                if (previous.length == lookups.length) {
                    int same = 0;
                    for (int i = 0; i < previous.length; i++) {
                        if (lookups[i] != previous[i]) {
                            break;
                        }
                        same++;
                    }
                    if (same == previous.length) {
                        return this;
                    }
                }
            }
            
            if (lookups.length == 1) {
                l = lookups[0];
                assert l != null : "Cannot assign null delegate";
            } else {
                if (lookups.length == 0) {
                    l = EMPTY_ARR;
                } else {
                    l = lookups.clone();
                }
            }
            
            if (isEmpty() && l == EMPTY_ARR) {
                return this;
            }
            
            return create(l, getResults());
        }
        final Lookup[] getLookups(boolean clone) {
            Object l = this.getRawLookups();
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
        final List<Lookup> getLookupsList() {
            return Arrays.asList(getLookups(false));            
        }

    } // end of ImmutableInternalData
    
    private static final class SingleInternalData extends ImmutableInternalData {
        /** lookups to delegate to (either Lookup or array of Lookups) */
        private final Object lookups;
        private final Template template;
        private final Reference<ProxyLookup.R> result;
                
        public SingleInternalData(Object lookups, Template<?> template, Reference<ProxyLookup.R> result) {
            this.lookups = lookups;
            this.template = template;
            this.result = result;
        }

        protected final boolean isEmpty() {
            return false;
        }

        protected Map<Template, Reference<R>> getResults() {
            return Collections.singletonMap(template, result);
        }
        
        protected Object getRawLookups() {
            return lookups;
        }
    }
    private static final class RealInternalData extends ImmutableInternalData {
        /** lookups to delegate to (either Lookup or array of Lookups) */
        private final Object lookups;

        /** map of templates to currently active results */
        private final Map<Template,Reference<R>> results;

        public RealInternalData(Object lookups, Map<Template, Reference<ProxyLookup.R>> results) {
            this.results = results;
            this.lookups = lookups;
        }

        protected final boolean isEmpty() {
            return false;
        }

        protected Map<Template, Reference<R>> getResults() {
            boolean strict = false;
            assert strict = true;
            return strict ? Collections.unmodifiableMap(results) : results;
        }
        
        protected Object getRawLookups() {
            return lookups;
        }
    }
    
    private static final class EmptyInternalData extends ImmutableInternalData {
        EmptyInternalData() {
        }

        protected final boolean isEmpty() {
            return true;
        }

        protected Map<Template, Reference<R>> getResults() {
            return Collections.emptyMap();
        }

        @Override
        protected Object getRawLookups() {
            return EMPTY_ARR;
        }
    } // end of EmptyInternalData
}

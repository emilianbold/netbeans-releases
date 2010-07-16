/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import java.util.*;


/**
 * Simple proxy lookup. Keeps reference to a lookup it delegates to and
 * forwards all requests.
 *
 * @author Jaroslav Tulach
 */
final class SimpleProxyLookup extends org.openide.util.Lookup {
    /** the provider to check for the status */
    private Provider provider;

    /** the lookup we currently delegate to */
    private Lookup delegate;

    /** map of all templates to Reference (results) associated to this lookup */
    private WeakHashMap<Template<?>,Reference<ProxyResult<?>>> results;

    /**
     * @param provider provider to delegate to
     */
    SimpleProxyLookup(Provider provider) {
        this.provider = provider;
    }

    /** Checks whether we still delegate to the same lookup */
    private Lookup checkLookup() {
        Lookup l = provider.getLookup();

        // iterator over Reference (ProxyResult)
        Iterator<Reference<ProxyResult<?>>> toCheck = null;

        synchronized (this) {
            if (l != delegate) {
                this.delegate = l;

                if (results != null) {
                    toCheck = new ArrayList<Reference<ProxyResult<?>>>(results.values()).iterator();
                }
            }
        }

        if (toCheck != null) {
            // update
            ArrayList<Object> evAndListeners = new ArrayList<Object>();
            for (Iterator<Reference<ProxyResult<?>>> it = toCheck; it.hasNext(); ) {
                java.lang.ref.Reference<ProxyResult<?>> ref = it.next();
                if (ref == null) {
                    continue;
                }

                ProxyResult<?> p = ref.get();

                if (p != null && p.updateLookup(l)) {
                    p.collectFires(evAndListeners);
                }
            }
            
            for (Iterator it = evAndListeners.iterator(); it.hasNext(); ) {
                LookupEvent ev = (LookupEvent)it.next();
                LookupListener ll = (LookupListener)it.next();
                ll.resultChanged(ev);
            }
        }

        return delegate;
    }

    @SuppressWarnings("unchecked")
    private static <T> ProxyResult<T> cast(ProxyResult<?> p) {
        return (ProxyResult<T>)p;
    }

    public <T> Result<T> lookup(Template<T> template) {
        synchronized (this) {
            if (results == null) {
                results = new WeakHashMap<Template<?>,Reference<ProxyResult<?>>>();
            } else {
                Reference<ProxyResult<?>> ref = results.get(template);

                if (ref != null) {
                    ProxyResult<?> p = ref.get();

                    if (p != null) {
                        return cast(p);
                    }
                }
            }

            ProxyResult<T> p = new ProxyResult<T>(template);
            Reference<ProxyResult<?>> ref = new WeakReference<ProxyResult<?>>(p);
            results.put(template, ref);

            return p;
        }
    }

    public <T> T lookup(Class<T> clazz) {
        if (clazz == null) {
            checkLookup();
            return null;
        }
        return checkLookup().lookup(clazz);
    }

    public <T> Item<T> lookupItem(Template<T> template) {
        return checkLookup().lookupItem(template);
    }

    /**
     * Result used in SimpleLookup. It holds a reference to the collection
     * passed in constructor. As the contents of this lookup result never
     * changes the addLookupListener and removeLookupListener are empty.
     */
    private final class ProxyResult<T> extends WaitableResult<T> implements LookupListener {
        /** Template used for this result. It is never null.*/
        private Template<T> template;

        /** result to delegate to */
        private Lookup.Result<T> delegate;

        /** listeners set */
        private javax.swing.event.EventListenerList listeners;
        private LookupListener lastListener;

        /** Just remembers the supplied argument in variable template.*/
        ProxyResult(Template<T> template) {
            this.template = template;
        }

        /** Checks state of the result
         */
        private Result<T> checkResult() {
            updateLookup(checkLookup());

            return this.delegate;
        }

        /** Updates the state of the lookup.
         * @return true if the lookup really changed
         */
        public boolean updateLookup(Lookup l) {
            Collection<? extends Item<T>> oldPairs = (delegate != null) ? delegate.allItems() : null;

            LookupListener removedListener;

            synchronized (this) {
                if ((delegate != null) && (lastListener != null)) {
                    removedListener = lastListener;
                    delegate.removeLookupListener(lastListener);
                } else {
                    removedListener = null;
                }
            }

            // cannot call to foreign code 
            Lookup.Result<T> res = l.lookup(template);

            synchronized (this) {
                if (removedListener == lastListener) {
                    delegate = res;
                    lastListener = new WeakResult<T>(this, delegate);
                    delegate.addLookupListener(lastListener);
                }
            }

            if (oldPairs == null) {
                // nobody knows about a change
                return false;
            }

            Collection<? extends Item<T>> newPairs = delegate.allItems();

            // See #34961 for explanation.
            if (!(oldPairs instanceof List)) {
                if (oldPairs == Collections.EMPTY_SET) {
                    // avoid allocation
                    oldPairs = Collections.emptyList();
                } else {
                    oldPairs = new ArrayList<Item<T>>(oldPairs);
                }
            }

            if (!(newPairs instanceof List)) {
                newPairs = new ArrayList<Item<T>>(newPairs);
            }

            return !oldPairs.equals(newPairs);
        }

        public synchronized void addLookupListener(LookupListener l) {
            if (listeners == null) {
                listeners = new javax.swing.event.EventListenerList();
            }

            listeners.add(LookupListener.class, l);
        }

        public synchronized void removeLookupListener(LookupListener l) {
            if (listeners != null) {
                listeners.remove(LookupListener.class, l);
            }
        }

        public java.util.Collection<? extends T> allInstances() {
            return checkResult().allInstances();
        }

        public Set<Class<? extends T>> allClasses() {
            return checkResult().allClasses();
        }

        public Collection<? extends Item<T>> allItems() {
            return checkResult().allItems();
        }

        protected void beforeLookup(Lookup.Template t) {
            Lookup.Result r = checkResult();

            if (r instanceof WaitableResult) {
                ((WaitableResult) r).beforeLookup(t);
            }
        }

        /** A change in lookup occured.
         * @param ev event describing the change
         *
         */
        public void resultChanged(LookupEvent anEvent) {
            collectFires(null);
        } 
        
        protected void collectFires(Collection<Object> evAndListeners) {
            javax.swing.event.EventListenerList l = this.listeners;

            if (l == null) {
                return;
            }

            Object[] listeners = l.getListenerList();

            if (listeners.length == 0) {
                return;
            }

            LookupEvent ev = new LookupEvent(this);
            AbstractLookup.notifyListeners(listeners, ev, evAndListeners);
        }
    }
     // end of ProxyResult
    private final class WeakResult<T> extends WaitableResult<T> implements LookupListener {
        private Lookup.Result source;
        private Reference<ProxyResult<T>> result;
        
        public WeakResult(ProxyResult<T> r, Lookup.Result<T> s) {
            this.result = new WeakReference<ProxyResult<T>>(r);
            this.source = s;
        }
        
        protected void beforeLookup(Lookup.Template t) {
            ProxyResult r = (ProxyResult)result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                source.removeLookupListener(this);
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            ProxyResult<T> r = result.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            } else {
                source.removeLookupListener(this);
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
            ProxyResult r = (ProxyResult)result.get();
            if (r != null) {
                r.resultChanged(ev);
            } else {
                source.removeLookupListener(this);
            }
        }

        public Collection<? extends Item<T>> allItems() {
            assert false;
            return null;
        }

        public Set<Class<? extends T>> allClasses() {
            assert false;
            return null;
        }
    } // end of WeakResult
}

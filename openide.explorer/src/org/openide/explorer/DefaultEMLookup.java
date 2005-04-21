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
package org.openide.explorer;

import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.beans.*;

import java.util.*;


/**
 * Contents of the lookup for a ExplorerManager, is copied
 * from org.openide.windows.DefaultTopComponentLookup and shares
 * its test. If updating, please update both.
 * @author Jaroslav Tulach
 */
final class DefaultEMLookup extends ProxyLookup implements LookupListener, PropertyChangeListener {
    private static final Object PRESENT = new Object();

    /** component to work with */
    private ExplorerManager tc;

    /** lookup listener that is attached to all subnodes */
    private LookupListener listener;

    /** Map of (Node -> node Lookup.Result) the above lookup listener is attached to */
    private Map attachedTo;

    /** action map for the top component */
    private Lookup actionMap;

    /** Creates the lookup.
     * @param tc component to work on
     * @param map action map to add to the lookup
    */
    public DefaultEMLookup(ExplorerManager tc, javax.swing.ActionMap map) {
        super();

        this.tc = tc;
        this.listener = (LookupListener) WeakListeners.create(LookupListener.class, this, null);
        this.actionMap = Lookups.singleton(map);

        tc.addPropertyChangeListener(WeakListeners.propertyChange(this, tc));

        updateLookups(tc.getSelectedNodes());
    }

    /** Extracts activated nodes from a top component and
     * returns their lookups.
     */
    public void updateLookups(Node[] arr) {
        if (arr == null) {
            arr = new Node[0];
        }

        Lookup[] lookups = new Lookup[arr.length];

        Map copy;

        synchronized (this) {
            if (attachedTo == null) {
                copy = Collections.EMPTY_MAP;
            } else {
                copy = new HashMap(attachedTo);
            }
        }

        for (int i = 0; i < arr.length; i++) {
            lookups[i] = arr[i].getLookup();

            if (copy != null) {
                // node arr[i] remains there, so do not remove it
                copy.remove(arr[i]);
            }
        }

        for (Iterator it = copy.values().iterator(); it.hasNext();) {
            Lookup.Result res = (Lookup.Result) it.next();
            res.removeLookupListener(listener);
        }

        synchronized (this) {
            attachedTo = null;
        }

        setLookups(new Lookup[] { new NoNodeLookup(new ProxyLookup(lookups), arr), Lookups.fixed(arr), actionMap, });
    }

    /** Change in one of the lookups we delegate to */
    public void resultChanged(LookupEvent ev) {
        updateLookups(tc.getSelectedNodes());
    }

    /** Finds out whether a query for a class can be influenced
     * by a state of the "nodes" lookup and whether we should
     * initialize listening
     */
    private static boolean isNodeQuery(Class c) {
        return Node.class.isAssignableFrom(c) || c.isAssignableFrom(Node.class);
    }

    protected synchronized void beforeLookup(Template t) {
        if ((attachedTo == null) && isNodeQuery(t.getType())) {
            Lookup[] arr = getLookups();

            attachedTo = new WeakHashMap(arr.length * 2);

            for (int i = 0; i < (arr.length - 2); i++) {
                Lookup.Result res = arr[i].lookup(t);
                res.addLookupListener(listener);
                attachedTo.put(arr[i], res);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES == evt.getPropertyName()) {
            updateLookups((Node[]) evt.getNewValue());
        }
    }

    /**
     * A proxying Lookup impl which yields no results when queried for Node,
     * and will never return any of the listed objects.
     */
    private static final class NoNodeLookup extends Lookup {
        private final Lookup delegate;
        private final Map verboten;

        public NoNodeLookup(Lookup del, Object[] exclude) {
            delegate = del;
            verboten = new IdentityHashMap();

            for (int i = 0; i < exclude.length; verboten.put(exclude[i++], PRESENT))
                ;
        }

        public Object lookup(Class clazz) {
            if (clazz == Node.class) {
                return null;
            } else {
                Object o = delegate.lookup(clazz);

                if (verboten.containsKey(o)) {
                    // There might be another one of the same class.
                    Iterator it = lookup(new Lookup.Template(clazz)).allInstances().iterator();

                    while (it.hasNext()) {
                        Object o2 = it.next();

                        if (!verboten.containsKey(o2)) {
                            // OK, use this one.
                            return o2;
                        }
                    }

                    // All such instances were excluded.
                    return null;
                } else {
                    return o;
                }
            }
        }

        public Lookup.Result lookup(Lookup.Template template) {
            if (template.getType() == Node.class) {
                return Lookup.EMPTY.lookup(new Lookup.Template(Node.class));
            } else {
                return new ExclusionResult(delegate.lookup(template), verboten);
            }
        }

        /**
         * A lookup result excluding some instances.
         */
        private static final class ExclusionResult extends Lookup.Result implements LookupListener {
            private final Lookup.Result delegate;
            private final Map verboten;
            private final List listeners = new ArrayList(); // List<LookupListener>

            public ExclusionResult(Lookup.Result delegate, Map verboten) {
                this.delegate = delegate;
                this.verboten = verboten;
            }

            public Collection allInstances() {
                Collection c = delegate.allInstances();
                List ret = new ArrayList(c.size()); // upper bound

                for (Iterator it = c.iterator(); it.hasNext();) {
                    Object o = it.next();

                    if (!verboten.containsKey(o)) {
                        ret.add(o);
                    }
                }

                return ret;
            }

            public Set allClasses() {
                return delegate.allClasses(); // close enough
            }

            public Collection allItems() {
                Collection c = delegate.allItems();
                List ret = new ArrayList(c.size()); // upper bound

                for (Iterator it = c.iterator(); it.hasNext();) {
                    Lookup.Item i = (Lookup.Item) it.next();

                    if (!verboten.containsKey(i.getInstance())) {
                        ret.add(i);
                    }
                }

                return ret;
            }

            public void addLookupListener(LookupListener l) {
                synchronized (listeners) {
                    if (listeners.isEmpty()) {
                        delegate.addLookupListener(this);
                    }

                    listeners.add(l);
                }
            }

            public void removeLookupListener(LookupListener l) {
                synchronized (listeners) {
                    listeners.remove(l);

                    if (listeners.isEmpty()) {
                        delegate.removeLookupListener(this);
                    }
                }
            }

            public void resultChanged(LookupEvent ev) {
                LookupEvent ev2 = new LookupEvent(this);
                LookupListener[] ls;

                synchronized (listeners) {
                    ls = (LookupListener[]) listeners.toArray(new LookupListener[listeners.size()]);
                }

                for (int i = 0; i < ls.length; i++) {
                    ls[i].resultChanged(ev2);
                }
            }
        }
    }
     // end of NoNodeLookup
}

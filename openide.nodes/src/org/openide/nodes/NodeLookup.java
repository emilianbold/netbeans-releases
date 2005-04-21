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
package org.openide.nodes;

import org.openide.nodes.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/** A lookup that represents content of a Node.getCookie and the node itself.
 *
 *
 * @author  Jaroslav Tulach
 */
final class NodeLookup extends AbstractLookup {
    /** See #40734 and NodeLookupTest and CookieActionIsTooSlowTest.
     * When finding action state for FilterNode, the action might been
     * triggered way to many times, due to initialization in beforeLookup
     * that triggered LookupListener and PROP_COOKIE change.
     */
    static final ThreadLocal NO_COOKIE_CHANGE = new ThreadLocal();

    /** Set of Classes that we have already queried <type>Class</type> */
    private java.util.Collection queriedCookieClasses = new ArrayList();

    /** node we are associated with
     */
    private Node node;

    /** New flat lookup.
     */
    public NodeLookup(Node n) {
        super();

        this.node = n;
        addPair(new LookupItem(n));
    }

    /** Calls into Node to find out if it has a cookie of given class.
     * It does special tricks to make CookieSet.Entry work.
     *
     * @param node node to ask
     * @param c class to query
     * @param colleciton to put Pair into if found
     */
    private static void addCookie(Node node, Class c, Collection collection, java.util.Map fromPairToClass) {
        Object res;
        AbstractLookup.Pair pair;
        Object prev = CookieSet.entryQueryMode(c);

        try {
            res = node.getCookie(c);
        } finally {
            pair = CookieSet.exitQueryMode(prev);
        }

        if (pair == null) {
            if (res == null) {
                return;
            }

            pair = new LookupItem(res);
        }

        collection.add(pair);
        fromPairToClass.put(pair, c);
    }

    /** Notifies subclasses that a query is about to be processed.
     * @param template the template
     */
    protected final void beforeLookup(Template template) {
        Class type = template.getType();

        if (type == Object.class) {
            // ok, this is likely query for everything
            java.util.Set all;
            Object prev = null;

            try {
                prev = CookieSet.entryAllClassesMode();

                Object ignoreResult = node.getCookie(Node.Cookie.class);
            } finally {
                all = CookieSet.exitAllClassesMode(prev);
            }

            Iterator it = all.iterator();

            while (it.hasNext()) {
                Class c = (Class) it.next();
                updateLookupAsCookiesAreChanged(c);
            }

            // fallthru and update Node.Cookie if not yet
            type = Node.Cookie.class;
        }

        if (Node.Cookie.class.isAssignableFrom(type)) {
            if (!queriedCookieClasses.contains(type)) {
                updateLookupAsCookiesAreChanged(type);
            }
        }
    }

    public void updateLookupAsCookiesAreChanged(Class toAdd) {
        java.util.Collection instances;
        java.util.Map fromPairToQueryClass;

        // if it is cookie change, do the rescan, try to keep order
        synchronized (this) {
            if (toAdd != null) {
                if (queriedCookieClasses.contains(toAdd)) {
                    // if this class has already been added, go away
                    return;
                }

                queriedCookieClasses.add(toAdd);
            }

            instances = new java.util.LinkedHashSet(queriedCookieClasses.size());
            fromPairToQueryClass = new java.util.HashMap();

            java.util.Iterator it = queriedCookieClasses.iterator();
            LookupItem nodePair = new LookupItem(node);
            instances.add(nodePair);
            fromPairToQueryClass.put(nodePair, Node.class);

            while (it.hasNext()) {
                Class c = (Class) it.next();
                addCookie(node, c, instances, fromPairToQueryClass);
            }
        }

        final java.util.Map m = fromPairToQueryClass;

        class Cmp implements java.util.Comparator {
            public int compare(Object o1, Object o2) {
                Pair p1 = (Pair) o1;
                Pair p2 = (Pair) o2;
                Class c1 = (Class) m.get(p1);
                Class c2 = (Class) m.get(p2);

                if (c1.isAssignableFrom(c2)) {
                    return -1;
                }

                if (c2.isAssignableFrom(c1)) {
                    return 1;
                }

                if (c1.isAssignableFrom(p2.getType())) {
                    return -1;
                }

                if (c2.isAssignableFrom(p1.getType())) {
                    return 1;
                }

                return 0;
            }
        }

        java.util.ArrayList list = new java.util.ArrayList(instances);
        java.util.Collections.sort(list, new Cmp());

        if (toAdd == null) {
            setPairs(list);
        } else {
            Object prev = NO_COOKIE_CHANGE.get();

            try {
                NO_COOKIE_CHANGE.set(node);

                // doing the setPairs under entryQueryMode guarantees that 
                // FilterNode will ignore the change
                setPairs(list);
            } finally {
                NO_COOKIE_CHANGE.set(prev);
            }
        }
    }

    /** Simple Pair to hold cookies and nodes */
    private static class LookupItem extends AbstractLookup.Pair {
        private Object instance;

        public LookupItem(Object instance) {
            this.instance = instance;
        }

        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return instance.toString();
        }

        public Object getInstance() {
            return instance;
        }

        public Class getType() {
            return instance.getClass();
        }

        public boolean equals(Object object) {
            if (object instanceof LookupItem) {
                return instance == ((LookupItem) object).getInstance();
            }

            return false;
        }

        public int hashCode() {
            return instance.hashCode();
        }

        protected boolean creatorOf(Object obj) {
            return instance == obj;
        }

        protected boolean instanceOf(Class c) {
            return c.isInstance(instance);
        }
    }
     // End of LookupItem class
}

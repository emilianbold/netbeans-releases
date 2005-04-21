/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util.lookup;

import org.openide.util.Lookup;
import org.openide.util.LookupListener;

import java.util.*;


/** Allows exclusion of certain instances from lookup.
 *
 * @author Jaroslav Tulach
 */
final class ExcludingLookup extends org.openide.util.Lookup {
    /** the other lookup that we delegate to */
    private Lookup delegate;

    /** classes to exclude (Class[]) or just one class (Class) */
    private Object classes;

    /**
     * Creates new Result object with supplied instances parameter.
     * @param instances to be used to return from the lookup
     */
    ExcludingLookup(Lookup delegate, Class[] classes) {
        this.delegate = delegate;

        if (classes.length == 1) {
            this.classes = classes[0];
        } else {
            this.classes = classes;
        }
    }

    public String toString() {
        return "ExcludingLookup: " + delegate + " excludes: " + Arrays.asList(classes()); // NOI18N
    }

    public Result lookup(Template template) {
        if (template == null) {
            throw new NullPointerException();
        }

        if (areSubclassesOfThisClassAlwaysExcluded(template.getType())) {
            // empty result
            return Lookup.EMPTY.lookup(template);
        }

        return new R(template.getType(), delegate.lookup(template));
    }

    public Object lookup(Class clazz) {
        if (areSubclassesOfThisClassAlwaysExcluded(clazz)) {
            return null;
        }

        Object res = delegate.lookup(clazz);

        if (isObjectAccessible(clazz, res, 0)) {
            return res;
        } else {
            return null;
        }
    }

    public org.openide.util.Lookup.Item lookupItem(org.openide.util.Lookup.Template template) {
        if (areSubclassesOfThisClassAlwaysExcluded(template.getType())) {
            return null;
        }

        org.openide.util.Lookup.Item retValue = delegate.lookupItem(template);

        if (isObjectAccessible(template.getType(), retValue, 2)) {
            return retValue;
        } else {
            return null;
        }
    }

    /** @return true if the instance of class c shall never be returned from this lookup
     */
    private boolean areSubclassesOfThisClassAlwaysExcluded(Class c) {
        Class[] arr = classes();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isAssignableFrom(c)) {
                return true;
            }
        }

        return false;
    }

    /** Returns the array of classes this lookup filters.
     */
    final Class[] classes() {
        if (classes instanceof Class[]) {
            return (Class[]) classes;
        } else {
            return new Class[] { (Class) classes };
        }
    }

    /** Does a check whether two classes are accessible (in the super/sub class)
     * releation ship without walking thru any of the classes mentioned in the
     * barrier.
     */
    private static boolean isAccessible(Class[] barriers, Class from, Class to) {
        if ((to == null) || !from.isAssignableFrom(to)) {
            // no way to reach each other by walking up
            return false;
        }

        for (int i = 0; i < barriers.length; i++) {
            if (to == barriers[i]) {
                return false;
            }
        }

        if (from == to) {
            return true;
        }

        //
        // depth first search
        //
        if (isAccessible(barriers, from, to.getSuperclass())) {
            return true;
        }

        Class[] interfaces = to.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            if (isAccessible(barriers, from, interfaces[i])) {
                return true;
            }
        }

        return false;
    }

    /** based on type decides whether the class accepts or not anObject
     * @param from the base type of the query
     * @param to depending on value of type either Object, Class or Item
     * @param type 0,1,2 for Object, Class or Item
     * @return true if we can access the to from from by walking around the bariers
     */
    private final boolean isObjectAccessible(Class from, Object to, int type) {
        if (to == null) {
            return false;
        }

        return isObjectAccessible(classes(), from, to, type);
    }

    /** based on type decides whether the class accepts or not anObject
     * @param barriers classes to avoid when testing reachability
     * @param from the base type of the query
     * @param to depending on value of type either Object, Class or Item
     * @param type 0,1,2 for Object, Class or Item
     * @return true if we can access the to from from by walking around the bariers
     */
    static final boolean isObjectAccessible(Class[] barriers, Class from, Object to, int type) {
        if (to == null) {
            return false;
        }

        switch (type) {
        case 0:
            return isAccessible(barriers, from, to.getClass());

        case 1:
            return isAccessible(barriers, from, (Class) to);

        case 2: {
            Item item = (Item) to;

            return isAccessible(barriers, from, item.getType());
        }

        default:
            throw new IllegalStateException("Type: " + type);
        }
    }

    /** Filters collection accroding to set of given filters.
     */
    final java.util.Collection filter(Class[] arr, Class from, java.util.Collection c, int type) {
        java.util.Collection ret = null;


// optimistic strategy expecting we will not need to filter
TWICE: 
        for (;;) {
            Iterator it = c.iterator();
BIG: 
            while (it.hasNext()) {
                Object res = it.next();

                if (!isObjectAccessible(arr, from, res, type)) {
                    if (ret == null) {
                        // we need to restart the scanning again 
                        // as there is an active filter
                        if (type == 1) {
                            ret = new java.util.HashSet();
                        } else {
                            ret = new ArrayList(c.size());
                        }

                        continue TWICE;
                    }

                    continue BIG;
                }

                if (ret != null) {
                    // if we are running the second round from TWICE
                    ret.add(res);
                }
            }

            // ok, processed
            break TWICE;
        }

        return (ret != null) ? ret : c;
    }

    /** Delegating result that filters unwanted items and instances.
     */
    private final class R extends WaitableResult implements LookupListener {
        private Result result;
        private Object listeners;
        private Class from;

        R(Class from, Result delegate) {
            this.from = from;
            this.result = delegate;
        }

        protected void beforeLookup(Template t) {
            if (result instanceof WaitableResult) {
                ((WaitableResult) result).beforeLookup(t);
            }
        }

        public void addLookupListener(LookupListener l) {
            boolean add;

            synchronized (this) {
                listeners = AbstractLookup.modifyListenerList(true, l, listeners);
                add = listeners != null;
            }

            if (add) {
                result.addLookupListener(this);
            }
        }

        public void removeLookupListener(LookupListener l) {
            boolean remove;

            synchronized (this) {
                listeners = AbstractLookup.modifyListenerList(false, l, listeners);
                remove = listeners == null;
            }

            if (remove) {
                result.removeLookupListener(this);
            }
        }

        public java.util.Collection allInstances() {
            return filter(classes(), from, result.allInstances(), 0);
        }

        public Set allClasses() {
            return (Set) filter(classes(), from, result.allClasses(), 1);
        }

        public Collection allItems() {
            return filter(classes(), from, result.allItems(), 2);
        }

        public void resultChanged(org.openide.util.LookupEvent ev) {
            if (ev.getSource() == result) {
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
                final org.openide.util.LookupEvent newev = new org.openide.util.LookupEvent(this);

                for (int i = 0; i < ll.length; i++) {
                    ll[i].resultChanged(newev);
                }
            }
        }
    }
}

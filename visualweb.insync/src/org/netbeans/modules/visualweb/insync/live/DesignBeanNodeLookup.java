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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.insync.live;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import org.openide.nodes.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/** XXX Copied from NB openide/nodes, in order to get the lookup impl.
 * See also NB issue #81540.
 *
 * A lookup that represents content of a Node.getCookie and the node itself.
 *
 *
 * @author  Jaroslav Tulach
 */
final class DesignBeanNodeLookup extends AbstractLookup {
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
    public DesignBeanNodeLookup() {
        super();
    }

    public void setNode(Node n) {
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
//        Object prev = CookieSet.entryQueryMode(c);
        Object prev = cookieSetEntryQueryMode(c);

        try {
            res = node.getCookie(c);
        } finally {
//            pair = CookieSet.exitQueryMode(prev);
            pair = cookieSetExitQueryMode(prev);
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
//                prev = CookieSet.entryAllClassesMode();
                prev = cookieSetEntryAllClassesMode();

                Object ignoreResult = node.getCookie(Node.Cookie.class);
            } finally {
//                all = CookieSet.exitAllClassesMode(prev);
                all = cookieSetExitAllClassesMode(prev);
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

    private static Object cookieSetEntryQueryMode(Class c) {
        return invokeOnCookieSet("entryQueryMode", new Class[] {Class.class}, new Object[] {c});
    }

    private static Pair cookieSetExitQueryMode(Object prev) {
        Collection col = (Collection)invokeOnCookieSet("exitQueryMode", new Class[] {Object.class}, new Object[] {prev});
        return col != null ? (Pair)col.iterator().next() : null;
    }

    private Object cookieSetEntryAllClassesMode() {
        return invokeOnCookieSet("entryAllClassesMode", new Class[0], new Object[0]);
    }

    private Set cookieSetExitAllClassesMode(Object prev) {
        return (Set)invokeOnCookieSet("exitAllClassesMode", new Class[] {Object.class}, new Object[] {prev});
    }

    // XXX There seems to be no other way how to fake the same behaviour, so calling the methods via reflection.
    private static Object invokeOnCookieSet(String methodName, Class[] parameterTypes, Object[] parameters) {
        try {
            Method method = CookieSet.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            try {
                return method.invoke(null, parameters);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return null;
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

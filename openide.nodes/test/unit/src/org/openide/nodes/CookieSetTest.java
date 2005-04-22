/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import junit.framework.*;
import junit.textui.TestRunner;
import java.util.*;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.*;

import org.netbeans.junit.*;
import javax.swing.event.ChangeListener;

/** Tests behaviour of CookieSet.
 *
 * @author Jaroslav Tulach
 */
public class CookieSetTest extends NbTestCase {
    public CookieSetTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(CookieSetTest.class));
    }
    
    public void testAddRemove () throws Exception {
        CookieSet set = new CookieSet ();
        L l = new L ();
        set.addChangeListener(l);
        
        
        C1 a1 = new C1 ();
        C1 c1 = new C1 ();
        C2 c2 = new C2 ();
        
        set.add (c1);
        
        assertEquals ("One change expected", l.cnt (), 1);
        assertEquals ("Node.Cookie", c1, set.getCookie (Node.Cookie.class));
        
        
        // replacing the c1 with a1
        set.add (a1);
        
        assertEquals ("One change expected", l.cnt (), 1);
        assertEquals ("Node.Cookie", a1, set.getCookie (Node.Cookie.class));
        
        // removing the cookie a1 leaves the set empty
        set.remove (a1);
        assertEquals ("One change expected", l.cnt (), 1);
        assertNull ("Node.Cookie", set.getCookie (Node.Cookie.class));
        

        // adding c1 again
        set.add (c1);
        assertEquals ("One change expected", l.cnt (), 1);
        assertEquals ("Node.Cookie", c1, set.getCookie (Node.Cookie.class));
        
        // and c2
        set.add (c2);
        assertEquals ("One change expected", l.cnt (), 1);
        assertEquals ("C1 cookie", c1, set.getCookie (Node.Cookie.class));
        assertEquals ("C2 index", c2, set.getCookie (Index.class));
        
        // removing c2 leaves to null on Index cookie
        set.remove (c2);
        assertEquals ("One change expected", l.cnt (), 1);
        assertEquals ("C1 cookie", c1, set.getCookie (Node.Cookie.class));
        assertNull ("Null index", set.getCookie (Index.class));
    }
        
    /** Adding smaller and bigger and removing smaller.
     */
    public void testAddSBremoveS () {
        CookieSet set = new CookieSet ();
        C1 c1 = new C1 ();
        C2 c2 = new C2 ();
        
        // after adding c1
        set.add (c1);
        
        // adding c2 and removing c1
        set.add (c2);
        set.remove (c1);
        
        assertEquals ("C2 index", c2, set.getCookie (Index.class));
        assertEquals ("C2 cookie", c2, set.getCookie (Node.Cookie.class));
    }

    /** Adding bigger and smaller and removing bigger.
     */
    public void testAddBSremoveB () {
        CookieSet set = new CookieSet ();
        C1 c1 = new C1 ();
        C2 c2 = new C2 ();
        
        // after adding c1
        set.add (c2);
        assertEquals ("Bigger registered", c2, set.getCookie (Node.Cookie.class));
        
        // adding c2 and removing c1
        set.add (c1);
        
        assertEquals ("Smaller takes preceedence", c1, set.getCookie (Node.Cookie.class));
        
        set.remove (c2);
        
        assertEquals ("C1 cookie", c1, set.getCookie (Node.Cookie.class));
        assertEquals ("Null index", null, set.getCookie (Index.class));
    }
    
    /** Tests behaviour of modifications via factory.
     */
    public void testFactoryAddRemove () {
        L l = new L ();
        CookieSet set = new CookieSet ();
        set.addChangeListener(l);
        
        set.add (C1.class, l);
        
        assertEquals ("One change", l.cnt (), 1);
        Node.Cookie obj = set.getCookie (C1.class);
        if (! (obj instanceof C1)) {
            fail ("Instance created by factory is wrong");
        }
        
        if (obj != set.getCookie (C1.class)) {
            fail ("New cookie created even it should not");
        }
        
        if (obj != set.getCookie (Node.Cookie.class)) {
            fail ("C1 is not registered as cookie");
        }
        
        // replace
        set.add (C1.class, l);
        assertEquals ("One change", l.cnt (), 1);
        
        if (obj == set.getCookie (C1.class)) {
            fail ("Factory changed, but cookie remains");
        }
        
        obj = set.getCookie (C1.class);

        // remove
        set.remove (obj);
        assertNotNull("Factory cookies cannot directly be removed", set.getCookie (C1.class));
        
        // remove of a factory
        set.remove(C1.class, l);
        assertNull("Removed factory still returns a cookie", set.getCookie (C1.class));
    }

    /** Tests behaviour of modifications via factory.
     */
    public void testFactoryAddRemoveInherit () {
        L l = new L ();
        CookieSet set = new CookieSet ();
        set.addChangeListener(l);
        
        set.add (C1.class, l);
        set.add (Node.Cookie.class, l);

        assertNull ("Nobody registered as C2", set.getCookie (C2.class));

        {
            Node.Cookie cookie = set.getCookie (Node.Cookie.class);
            if (! (cookie instanceof C2)) {
                fail ("factory provides cookie C2 for Node.Cookie");
            } 
        }

        {
            Node.Cookie c1 = set.getCookie (C1.class);
            assertNotNull (c1);
            assertEquals ("Factory provides C1 for C1 class", c1.getClass (), C1.class);
        }
        
        assertNull ("Still nobody registered as C2", set.getCookie (C2.class));
        
    }
    
    public void testCookieSetThruLookupReturnsTheSame () throws Exception {
        doCookieSetTestsToSimulateIssue47411 (Node.Cookie.class, false);
    }

    public void testCookieSetThruLookupReturnsTheSameEvenWhenQueriedForLarger () throws Exception {
        doCookieSetTestsToSimulateIssue47411 (Index.class, false);
    }
    
    public void testCookieSetThruLookupReturnsTheSameWithFilter () throws Exception {
        doCookieSetTestsToSimulateIssue47411 (Node.Cookie.class, true);
    }

    public void testCookieSetThruLookupReturnsTheSameEvenWhenQueriedForLargerWithFilter () throws Exception {
        doCookieSetTestsToSimulateIssue47411 (Index.class, true);
    }
    
    private void doCookieSetTestsToSimulateIssue47411 (Class firstQuery, boolean filter) throws Exception {
        AbstractNode an = new AbstractNode (Children.LEAF);
        CookieSet set = new CookieSet ();
        an.setCookieSet (set);
        
        Node n = filter ? (Node)new FilterNode (an) : (Node)an;
        
        C1 c1 = new C1 ();
        C2 c2 = new C2 ();
        
        // after adding c1
        set.add (c2);
        assertEquals ("Bigger registered", c2, set.getCookie (firstQuery));
        assertEquals ("Bigger in lookup", c2, n.getLookup ().lookup (firstQuery));
        
        // adding c2 and removing c1
        set.add (c1);
        assertEquals ("Smaller takes preceedence", c1, set.getCookie (Node.Cookie.class));
        assertEquals ("Smaller even in lookup", c1, n.getLookup ().lookup (Node.Cookie.class));
    }
    
    public void testCookieSetThruLookupImprovedVersionIssue47411 () throws Exception {
        doCookieSetThruLookupImprovedVersionIssue47411 (false);
    }
        
    public void testCookieSetThruLookupImprovedVersionWithFitlerIssue47411 () throws Exception {
        doCookieSetThruLookupImprovedVersionIssue47411 (true);
    }
    
    private void doCookieSetThruLookupImprovedVersionIssue47411 (boolean filter) throws Exception {
        AbstractNode node = new AbstractNode (Children.LEAF);
        CookieSet set = new CookieSet ();
        node.setCookieSet (set);
        
        Node an = filter ? (Node)new FilterNode (node) : (Node)node;
        

        class X implements org.openide.cookies.OpenCookie, org.openide.cookies.EditCookie {
            public void open () {
            }
            public void edit () {
            }
        }
        X x = new X ();
        
        class A implements org.openide.cookies.OpenCookie {
            public void open () { }
        }
        A a = new A ();
        
        set.add (a);
        set.add (x);
        
        Object edit = an.getLookup ().lookup (org.openide.cookies.EditCookie.class);
        assertEquals ("X has edit", x, edit);
        
        Object open = an.getLookup ().lookup (org.openide.cookies.OpenCookie.class);
        
        assertEquals ("Just verify that CookieSet returns A", a, set.getCookie (org.openide.cookies.OpenCookie.class));
        assertEquals ("A has open", a, open);
        
        assertEquals (null, an.getLookup ().lookup (SaveCookie.class));
        assertEquals (a, an.getLookup ().lookup (OpenCookie.class));
        assertEquals (x, an.getLookup ().lookup (EditCookie.class));
    }
    
    /** Change listener.
     */
    private static final class L extends Object 
    implements ChangeListener, CookieSet.Factory {
        private int count;
        
        public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
            count++;
        }
        
        /** Gets and clears the count.
         */
        public int cnt () {
            int c = count;
            count = 0;
            return c;
        }
        
        /** Creates a Node.Cookie of given class. The method
         * may be called more than once.
         */
        public Node.Cookie createCookie(Class klass) {
            if (klass == C1.class) {
                return new C1 ();
            } 
            return new C2 ();
        }
        
    }
    
    /** A simple cookie.
     */
    private static class C1 implements Node.Cookie {
    }
    
    /** A complicated cookie.
     */
    private static final class C2 extends C1 implements Index {
        
        /** Get the index of a given node.
         * @param node node to find index of
         * @return index of the node, or <code>-1</code> if no such node was found
         */
        public int indexOf(Node node) {
            return 0;
        }
        
        /** Move an element up.
         * @param x index of element to move up
         * @exception IndexOutOfBoundsException if an index is out of bounds
         */
        public void moveUp(int x) {
        }
        
        /** Get the child nodes.
         * @return array of nodes that can be sorted by this index
         */
        public Node[] getNodes() {
            return null;
        }
        
        /** Move an element down.
         * @param x index of element to move down
         * @exception IndexOutOfBoundsException if an index is out of bounds
         */
        public void moveDown(int x) {
        }
        
        /** Invoke a dialog for reordering the children.
         */
        public void reorder() {
        }
        
        /** Exchange two elements.
         * @param x position of the first element
         * @param y position of the second element
         * @exception IndexOutOfBoundsException if an index is out of bounds
         */
        public void exchange(int x, int y) {
        }
        
        /** Remove a listener from the listener list.
         *
         * @param chl listener to remove
         */
        public void removeChangeListener(ChangeListener chl) {
        }
        
        /** Get the number of nodes.
         * @return the count
         */
        public int getNodesCount() {
            return 0;
        }
        
        /** Add a new listener to the listener list. The listener will be notified of
         * any change in the order of the nodes.
         *
         * @param chl new listener
         */
        public void addChangeListener(ChangeListener chl) {
        }
        
        /** Reorder all children with a given permutation.
         * @param perm permutation with the length of current nodes
         * @exception IllegalArgumentException if the permutation is not valid
         */
        public void reorder(int[] perm) {
        }
        
        /** Move the element at the <code>x</code>-th position to the <code>y</code>-th position. All
         * elements after the <code>y</code>-th position are moved down.
         *
         * @param x the position to remove the element from
         * @param y the position to insert the element to
         * @exception IndexOutOfBoundsException if an index is out of bounds
         */
        public void move(int x, int y) {
        }
    };
}

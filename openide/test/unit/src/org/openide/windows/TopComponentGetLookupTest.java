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

package org.openide.windows;

import java.util.ArrayList;
import javax.swing.ActionMap;

import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;



/** Check the behaviour of TopComponent's lookup.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public class TopComponentGetLookupTest extends NbTestCase {
    /** top component we work on */
    private TopComponent top;
    /** its lookup */
    private Lookup lookup;
    
    
    public TopComponentGetLookupTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TopComponentGetLookupTest.class);
        
        return suite;
    }
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        top = new TopComponent ();
        lookup = top.getLookup ();
    }

    
    
    /** Test to find nodes.
     */
    private void doTestNodes (org.openide.nodes.Node[] arr, Class c, int cnt) {
        if (arr != null) {
            top.setActivatedNodes(arr);
        }
        
        assertNotNull ("At least one node is registered", lookup.lookup (c));
        Lookup.Result res = lookup.lookup (new Lookup.Template (c));
        java.util.Collection coll = res.allItems ();
        assertEquals ("Two registered", cnt, coll.size ());
    }
    
    public void testNodes () {
        doTestNodes (new org.openide.nodes.Node[] {
            new N ("1"), new N ("2")
        }, N.class, 2);
        doTestNodes (new org.openide.nodes.Node[] {
            new N ("1"), new N ("2")
        }, java.beans.FeatureDescriptor.class, 2);
    }
    
    private void doTestNodesWithChangesInLookup (Class c) {
        InstanceContent ic = new InstanceContent();
        
        org.openide.nodes.Node[] arr = new org.openide.nodes.Node[] {
            new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF, new AbstractLookup (ic)), 
            new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF, Lookup.EMPTY) 
        };
        //doTestNodes (arr, org.openide.nodes.AbstractNode.class);
        doTestNodes (arr, c, 2);
        
        ic.add (arr[1]);
        
        assertEquals ("Now the [1] is in lookup of [0]", arr[1], lookup.lookup (c));

        doTestNodes (null, c, 2);
    }
    
    public void testNodesWhenTheyAreNotInTheirLookup () {
        doTestNodesWithChangesInLookup (org.openide.nodes.AbstractNode.class);
    }
    
    public void testNodesSuperclassesWhenTheyAreNotInTheirLookup () {
        doTestNodesWithChangesInLookup (java.beans.FeatureDescriptor.class);
    }
    
    
    /** Tests changes in cookies.
     */
    public void testCookies () {
        N[] arr = { new N ("1"), new N ("2"), new N ("3") };
        
        top.setActivatedNodes (arr);
        assertEquals ("Three nodes there", 3, top.getActivatedNodes ().length);
        
        L l = new L ();
        Lookup.Result res = lookup.lookup (new Lookup.Template (org.openide.cookies.OpenCookie.class));
        res.addLookupListener (l);
     
        assertEquals ("Empty now", res.allItems().size (), 0);
        
        arr[0].state (0x01); // enabled open cookie

        assertEquals ("One item", res.allItems ().size (), 1);
        l.check ("One change", 1);
        
        arr[2].state (0x02); // change of different cookie
        
        assertEquals ("Still one item", res.allItems ().size (), 1);
        l.check ("No change", 0);
        
        arr[2].state (0x03); // added also OpenCookie
        
        assertEquals ("Both items", res.allItems ().size (), 2);
        l.check ("One change again", 1);
        
        arr[0].state (0x00);
        
        assertEquals ("One still there", res.allItems ().size (), 1);
        assertEquals ("The second object", lookup.lookup (org.openide.cookies.OpenCookie.class), arr[2].getCookie (org.openide.cookies.OpenCookie.class));
        
        top.setActivatedNodes (new org.openide.nodes.Node[0]);
        assertNull ("No cookie now", lookup.lookup (org.openide.cookies.OpenCookie.class));
    }
    
    public void testNodesAreInTheLookupAndNothingIsFiredBeforeFirstQuery () {
        AbstractNode n1 = new AbstractNode (org.openide.nodes.Children.LEAF, Lookup.EMPTY);
        top.setActivatedNodes(new org.openide.nodes.Node[] { n1 });
        assertEquals ("One node there", 1, top.getActivatedNodes ().length);
        assertEquals ("Is the right now", n1, top.getActivatedNodes ()[0]);
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (org.openide.nodes.Node.class));
        L l = new L ();
        res.addLookupListener(l);
        
        l.check ("Nothing fired before first query", 0);
        res.allInstances ();
        l.check ("Nothing is fired on first query", 0);
        lookup.lookup (new Lookup.Template (org.openide.nodes.Node.class)).allInstances ();
        l.check ("And additional query does not change anything either", 0);
    }
   
    public void testNodesAreThereEvenIfTheyAreNotContainedInTheirOwnLookup () {
        Lookup.Result res = lookup.lookup (new Lookup.Template (org.openide.nodes.Node.class));
        
        AbstractNode n1 = new AbstractNode (org.openide.nodes.Children.LEAF, Lookup.EMPTY);
        
        InstanceContent content = new InstanceContent ();
        AbstractNode n2 = new AbstractNode (org.openide.nodes.Children.LEAF, new AbstractLookup (content));
        
        assertNull ("Not present in its lookup", n1.getLookup ().lookup (n1.getClass ()));
        assertNull ("Not present in its lookup", n2.getLookup ().lookup (n2.getClass ()));
        
        top.setActivatedNodes (new AbstractNode[] { n1 });
        assertEquals ("But node is in the lookup", n1, lookup.lookup (n1.getClass ()));
        
        assertEquals ("One item there", 1, res.allInstances ().size ());
        
        L listener = new L ();
        res.addLookupListener(listener);
        
        top.setActivatedNodes (new AbstractNode[] { n2 });
        assertEquals ("One node there", 1, top.getActivatedNodes ().length);
        assertEquals ("n2", n2, top.getActivatedNodes ()[0]);
        
        listener.check ("Node changed", 1);
        
        java.util.Collection addedByTCLookup = res.allInstances ();
        assertEquals ("One item still", 1, addedByTCLookup.size ());
        
        content.add (n2);
        assertEquals ("After the n2.getLookup starts to return itself, there is no change", 
            addedByTCLookup, res.allInstances ());
        
        // would be nice if there was no change, but is not right now
        // listener.check ("And nothing is fired", 0);
        
        content.remove (n2);
        assertEquals ("After the n2.getLookup stops to return itself, there is no change", 
            addedByTCLookup, res.allInstances ());
        
        // would be nice if there was no change, but is not right now
        // listener.check ("And nothing is fired", 0);
        
        content.add (n1);
        // would be nice if there was no change, but is not right now
        // listener.check ("Adding another node, fires change", 1);
        // so we check at least for one being fired
        listener.checkAtLeast ("Adding another node, fires change", 1);
        java.util.Collection two = res.allInstances ();
        assertEquals ("Really two nodes", 2, two.size ());
        java.util.Iterator it = two.iterator ();
        assertEquals ("First is the one from the node's lookup", n1, it.next ());
        assertEquals ("Second is the one added by the TC lookup", n2, it.next ());
    }
    
    public void testNoChangeWhenSomethingIsChangedOnNotActivatedNode () {
        doTestNoChangeWhenSomethingIsChangedOnNotActivatedNode (0);
    }
    
    public void testNoChangeWhenSomethingIsChangedOnNotActivatedNode2 () {
        doTestNoChangeWhenSomethingIsChangedOnNotActivatedNode (50);
    }
        
    private void doTestNoChangeWhenSomethingIsChangedOnNotActivatedNode (int initialSize) {
        Object obj = new org.openide.cookies.OpenCookie () { public void open () {} };
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (org.openide.cookies.OpenCookie.class));
        Lookup.Result nodeRes = lookup.lookup (new Lookup.Template (org.openide.nodes.Node.class));
        int mask = 0x01;
        
        InstanceContent ic = new InstanceContent ();
        CountingLookup cnt = new CountingLookup (ic);
        org.openide.nodes.AbstractNode ac = new org.openide.nodes.AbstractNode (
            org.openide.nodes.Children.LEAF, cnt
        );
        for (int i = 0; i < initialSize; i++) {
            ic.add (new Integer (i));
        }
        
        top.setActivatedNodes(new org.openide.nodes.Node[] { ac });
        assertEquals ("One node there", 1, top.getActivatedNodes ().length);
        assertEquals ("It is the ac one", ac, top.getActivatedNodes ()[0]);
        ic.add (obj);
        
        L listener = new L ();
        
        res.allItems();
        nodeRes.allItems ();
        res.addLookupListener (listener);
        
        java.util.Collection allListeners = cnt.listeners;
        
        assertEquals ("Has the cookie", 1, res.allItems ().size ());
        listener.check ("No changes yet", 0);

        ic.remove (obj);
        
        assertEquals ("Does not have the cookie", 0, res.allItems ().size ());
        listener.check ("One change", 1);
        
        top.setActivatedNodes (new N[0]);
        assertEquals("The nodes are empty", 0, top.getActivatedNodes ().length);
        listener.checkAtLeast ("There should be no change, but there is one now, improve if possible", 1);

        cnt.queries = 0;
        ic.add (obj);
        ic.add (ac);
        listener.check ("Removing the object or node from not active node does not send any event", 0);
        
        nodeRes.allItems ();
        listener.check ("Queriing for node does generate an event", 0);
        assertEquals ("No Queries to the not active node made", 0, cnt.queries);
        assertEquals ("No listeneners on cookies", allListeners, cnt.listeners);
    }
    
    public void testBug32470FilterNodeAndANodeImplementingACookie () {
        class NY extends AbstractNode implements org.openide.cookies.SaveCookie {
            public NY () {
                super (org.openide.nodes.Children.LEAF);
                getCookieSet ().add (this);
            }
            
            public void save () {
            }
        }
        
        Node ny = new NY ();
        Node node = new FilterNode (new FilterNode (ny, null, ny.getLookup ()));
        top.setActivatedNodes (new Node[] { node });
        
        Lookup.Template nodeTemplate = new Lookup.Template (Node.class);
        Lookup.Template saveTemplate = new Lookup.Template (org.openide.cookies.SaveCookie.class);
        java.util.Collection res;
        
        res = lookup.lookup (nodeTemplate).allInstances ();
        assertEquals ("FilterNode is the only node there", 
            java.util.Collections.singletonList (node), res
        );

        res = lookup.lookup (saveTemplate).allInstances ();
        assertEquals ("SaveCookie is there only once", 
            java.util.Collections.singletonList (ny), res
        );

        res = lookup.lookup (nodeTemplate).allInstances ();
        assertEquals ("FilterNode is still the only node there", 
            java.util.Collections.singletonList (node), res
        );
    }
    
    
    /** Listener to count number of changes.
     */
    private static final class L extends Object 
    implements org.openide.util.LookupListener {
        private int cnt;
        
        /** A change in lookup occured.
         * @param ev event describing the change
         */
        public void resultChanged(org.openide.util.LookupEvent ev) {
            cnt++;
        }
        
        /** Checks at least given number of changes.
         */
        public void checkAtLeast (String text, int num) {
            if (cnt < num) {
                fail (text + " expected at least " + num + " but was " + cnt);
            }
            cnt = 0;
        }
        
        /** Checks number of modifications.
         */
        public void check (String text, int num) {
            assertEquals (text, num, cnt);
            cnt = 0;
        }
    }
    

    /** Overides some methods so it is not necessary to use the data object.
     */
    private static final class N extends org.openide.nodes.AbstractNode {
        private org.openide.nodes.Node.Cookie[] cookies = {
            new org.openide.cookies.OpenCookie () { public void open () {} },
            new org.openide.cookies.EditCookie () { public void edit () {} },
            new org.openide.cookies.SaveCookie () { public void save () {} },
            new org.openide.cookies.CloseCookie () { public boolean  close () { return true; } }
        };
    
        private int s;
        
        public N (String name) {
            super (org.openide.nodes.Children.LEAF);
            setName (name);
        }

        public void state (int s) {
            this.s = s;
            fireCookieChange ();
        }
        
        public org.openide.nodes.Node.Cookie getCookie (Class c) {
            int mask = 0x01;
            
            for (int i = 0; i < cookies.length; i++) {
                if ((s & mask) != 0 && c.isInstance(cookies[i])) {
                    return cookies[i];
                }
                mask = mask << 1;

            }
            return null;
        }
    }
    
    private static final class CountingLookup extends Lookup {
        private Lookup delegate;
        public ArrayList listeners = new ArrayList ();
        public int queries;
        
        public CountingLookup (org.openide.util.lookup.InstanceContent ic) {
            delegate = new org.openide.util.lookup.AbstractLookup (ic);
            
        }
        
        public Object lookup(Class clazz) {
            return delegate.lookup (clazz);
        }
        
        public org.openide.util.Lookup.Result lookup(org.openide.util.Lookup.Template template) {
            if (
                !org.openide.nodes.Node.Cookie.class.isAssignableFrom(template.getType ()) &&
                !org.openide.nodes.Node.class.isAssignableFrom(template.getType ())
            ) {
                return delegate.lookup (template);
            }
            
            
            final Lookup.Result d = delegate.lookup (template);
            
            class Wrap extends Lookup.Result {
                public void addLookupListener (org.openide.util.LookupListener l) {
                    listeners.add (l);
                    d.addLookupListener (l);
                }
                
                public void removeLookupListener (org.openide.util.LookupListener l) {
                    listeners.remove (l);
                    d.removeLookupListener (l);
                }
                public java.util.Collection allInstances () {
                    queries++;
                    return d.allInstances ();
                }
                public java.util.Collection allItems () {
                    queries++;
                    return d.allItems ();
                }
                public java.util.Set allClasses () {
                    queries++;
                    return d.allClasses ();
                }
            }
            
            return new Wrap ();
        }
        
    }
}

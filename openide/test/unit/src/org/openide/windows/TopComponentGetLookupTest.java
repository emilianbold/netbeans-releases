/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.windows;

import java.util.ArrayList;
import javax.swing.ActionMap;

import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.nodes.AbstractNode;
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
    public void testNodes () {
        top.setActivatedNodes(new org.openide.nodes.Node[] {
            new N ("1"), new N ("2")
        });
        
        assertNotNull ("At least one node is registered", lookup.lookup (N.class));
        Lookup.Result res = lookup.lookup (new Lookup.Template (N.class));
        java.util.Collection c = res.allItems ();
        assertEquals ("Two registered", c.size (), 2);
    }
    
    /** Tests changes in cookies.
     */
    public void testCookies () {
        N[] arr = { new N ("1"), new N ("2"), new N ("3") };
        
        top.setActivatedNodes (arr);
        
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
}

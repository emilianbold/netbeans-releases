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
import org.openide.util.Lookup;



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
        assertEquals ("Two registered", lookup.lookup (new Lookup.Template (N.class)).allItems().size (), 2);
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

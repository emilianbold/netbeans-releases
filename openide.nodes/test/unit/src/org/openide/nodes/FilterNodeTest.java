/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** A test to check behavior of filter node.
 *
 * @author Jaroslav Tulach
 */
public class FilterNodeTest extends NbTestCase {

    public FilterNodeTest(String name) {
        super(name);
    }
    
    /** Demonstrates a bug in FilterNode.changeOriginal.
     */
    public void testChangeOriginalLeafToArray () {
        AbstractNode a = new AbstractNode (Children.LEAF);
        AbstractNode b = new AbstractNode (Children.LEAF);
        AbstractNode c = new AbstractNode (new Children.Array ());
    
        
        FN fn = new FN (a);
        fn.changeCh (b, true);
        fn.changeCh (c, true);
    }
    
    public void testYouCannotBeYourOwnOriginal () {
        doOriginalsMayNotFormCycle (0);
    }
    public void testYouCannotBeYourOwnOriginal1 () {
        doOriginalsMayNotFormCycle (1);
    }
    public void testYouCannotBeYourOwnOriginal2 () {
        doOriginalsMayNotFormCycle (2);
    }
    public void testYouCannotBeYourOwnOriginal3 () {
        doOriginalsMayNotFormCycle (3);
    }
    
    private void doOriginalsMayNotFormCycle (int length) {
        FilterNode first = new FilterNode (Node.EMPTY);
        FilterNode node = first;
        
        for (int i = 0; i < length; i++) {
            node = new FilterNode (node);
        }
        
        try {
            // this should not be allowed, because otherwise...
            first.changeOriginal (node, false);
            // ... these methods fail
            node.hashCode ();
            node.getName ();
        } catch (java.lang.IllegalArgumentException ex) {
            // iae is fine
        }
    }
    
    public void testHashCodeStackOverflowDetectionToFindOutTheProblemOfIssue46993 () {
        FilterNode node = new FilterNode (Node.EMPTY, null, org.openide.util.Lookup.EMPTY);
        FilterNode n2 = new FilterNode (node);
        FilterNode n3 = new FilterNode (n2);
        
        
        
        class FireL extends NodeAdapter {
            private boolean thrown;
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                thrown = true;
                throw new IllegalStateException ("my");
            }
        }
        FireL f = new FireL ();
        // trick for proper listener ordering
        n3.addNodeListener (f);
        n3.removeNodeListener (f);
        node.addNodeListener (f);
        

        try {
            node.changeOriginal (n3, false);
        } catch (IllegalStateException ex) {
            assertTrue ("Really thrown ex from my listener", f.thrown);
            assertEquals ("my", ex.getMessage ());
        } catch (IllegalArgumentException ex) {
            // this is fine since we added the check in 
            // checkIfIamAccessibleFromOriginal
            // if this fails, the rest of the test is not important, as
            // we prevent the tested issue before it could happen
            return;
        }
        
        try {
            n3.hashCode ();
            fail ("This has to throw an error");
        } catch (/*StackOverflow*/Error err) {
            if (! (err instanceof StackOverflowError)) {
                fail ("Wrong class: " + err.getClass ());
            }
            assertIdentityHashCode (err.getMessage (), node);
            assertIdentityHashCode (err.getMessage (), n2);
            assertIdentityHashCode (err.getMessage (), n3);
        }
    }
    
    private static void assertIdentityHashCode (String msg, Object obj) {
        String hex = Integer.toString (System.identityHashCode (obj), 16);
        int indx = msg != null ? msg.indexOf (hex) : -1;
        if (indx == -1) {
            fail ("Message <" + msg + "> should contain identityHashCode of " + obj + " which is " + hex + "\nAre you sure you are running tests with assertions enabled!?");
        }
    }
    
    
    /** Demonstates a bug in FilterNode.changeOriginal.
     */
    public void testChangeOriginalArrayToLeaf () {
        AbstractNode a = new AbstractNode (Children.LEAF);
        AbstractNode b = new AbstractNode (new Children.Array ());
        AbstractNode c = new AbstractNode (new Children.Array ());
    
        
        FN fn = new FN (c);
        fn.changeCh (b, true);
        fn.changeCh (a, true);
    }

    /** See issue #28198. */
    public void testEquals() {
        Node original = new AbstractNode(Children.LEAF);
        FilterNode filter = new FilterNode(original);

        // Both has to return same value.
        assertTrue("Equals of filter node and its original is not symmetric",
            (filter.equals(original)) == (original.equals(filter)));
    }
    
    public void testHashCodeAndEquals () {
        Node original = new AbstractNode(Children.LEAF);
        FilterNode filter = new FilterNode(original);
        
        assertTrue ("They are equal", filter.equals (original));
        assertTrue ("In both directions", original.equals (filter));
        assertEquals ("Have the same hashcode", original.hashCode(), filter.hashCode ());
    }
    
    /** Get actions are correctly propagated.
     */
    public void testGetActions () {
        final ArrayList contextActions = new ArrayList ();
        final ArrayList actions = new ArrayList ();
        
        class AA extends javax.swing.AbstractAction {
            public void actionPerformed (java.awt.event.ActionEvent ev) {
            }
        }
        
        contextActions.add (new AA ());
        contextActions.add (null);
        contextActions.add (new AA ());
        
        actions.add (new AA ());
        actions.add (new AA ());
        
        final javax.swing.Action pref = new AA ();
        
        AbstractNode n = new AbstractNode (Children.LEAF) {
            public javax.swing.Action[] getActions (boolean context) {
                ArrayList l = context ? contextActions : actions;
                return (javax.swing.Action[])l.toArray (
                    new javax.swing.Action[0]
                );
            }
            
            public javax.swing.Action getPreferredAction () {
                return pref;
            }
        };
        
        FilterNode fn = new FilterNode (n);
        
        
        assertEquals ("Same context actions", contextActions, Arrays.asList (fn.getActions(true)));
        assertEquals ("Same actions", actions, Arrays.asList (fn.getActions(false)));
        assertEquals ("Same preffered action", pref, fn.getPreferredAction());
        
        
        fn = new FilterNode (n) {
            public SystemAction getDefaultAction () {
                return SystemAction.get (OpenAction.class);
            }
            
            public SystemAction[] getActions () {
                return new SystemAction[] { getDefaultAction () };
            }
        };
        
        assertEquals ("Overriding getDefaultAction wins", fn.getDefaultAction (), fn.getPreferredAction());
        assertEquals ("Overriding getActions wins", Arrays.asList (fn.getActions()), Arrays.asList (fn.getActions(false)));
        assertEquals ("Same context actions", contextActions, Arrays.asList (fn.getActions(true)));
        
        
        fn = new FilterNode (n) {
            public SystemAction getDefaultAction () {
                return SystemAction.get (OpenAction.class);
            }
            
            public SystemAction[] getContextActions () {
                return new SystemAction[] { getDefaultAction () };
            }
        };
        
        assertEquals ("Overriding getDefaultAction wins", fn.getDefaultAction (), fn.getPreferredAction());
        assertEquals ("Same actions", actions, Arrays.asList (fn.getActions(false)));
        assertEquals ("Overriding getContextActions wins", Arrays.asList (fn.getContextActions()), Arrays.asList (fn.getActions (true)));
    }
    
    public void testUpdateLeaf () {
        AbstractNode a = new AbstractNode (Children.LEAF);
        FilterNode fn = new FilterNode (a);
        assertEquals ("Children is leaf", Children.LEAF, fn.getChildren ());
    
        a.setChildren(new Children.Array ());
        assertFalse("Children of FilterNode not updated", fn.isLeaf());
        assertTrue ("Children are not leaf", fn.getChildren () != Children.LEAF);
        
        class Counter extends NodeAdapter {
            public int cnt;
            
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                if (Node.PROP_LEAF.equals (ev.getPropertyName())) {
                    cnt++;
                }
            }
        }
        
        Counter counter = new Counter ();
        
        fn.addNodeListener(counter);
        a.setChildren (Children.LEAF);
      
        assertEquals ("Children is leaf", Children.LEAF, fn.getChildren ());
        assertTrue ("Now it is LEAF again", fn.isLeaf ());
        assertEquals ("One change", 1, counter.cnt);
        
        a.setChildren (new Children.Array ());
        assertFalse ("Again has children", fn.isLeaf ());
        assertEquals ("Another change", 2, counter.cnt);
        
    }

    
    public void testUpdateLeafWithProvidedChildren () {
        AbstractNode node = new AbstractNode (Children.LEAF);
        FilterNode fn = new FilterNode (node, new Children.Array ());
        
        assertFalse ("filter node is not leaf, it has Array children", fn.isLeaf ());
        
        
        node = new AbstractNode (new Children.Array ());
        fn = new FilterNode (node, Children.LEAF);
        
        assertTrue ("filter node is leaf as children were provided", fn.isLeaf ());
    }
    
    public void testIsLeafCanBeCalledWhenAnotherThreadHoldsALock () throws Exception {
        final FilterNode fn = new FilterNode (Node.EMPTY);
        final RequestProcessor rp = new RequestProcessor ("Will deadlock");
        
        assertTrue ("Is leaf", fn.isLeaf ());
        
        class BlockInReadAccess implements Runnable {
            int cnt;
            
            public synchronized void run () {
                if (cnt++ == 0) {
                    Children.MUTEX.readAccess(this);
                    return;
                }
                
                try {
                    notify ();
                    wait ();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                cnt = -1;
                notify ();
            }
        }
            
        BlockInReadAccess b = new BlockInReadAccess ();
        synchronized (b) {
            rp.post (b);
            b.wait ();
            // now the task is blocked in read access
        }
        
        assertTrue ("Is leaf can be called", fn.isLeaf ());
        
        synchronized (b) {
            b.notify ();
            b.wait ();
        }
        
        assertEquals ("B finished", -1, b.cnt);
    }

    public void testIsLeafDoesNotChangeWhileInReadAccess() throws Exception {
	AbstractNode a = new AbstractNode (Children.LEAF);
        final FilterNode fn = new FilterNode (a);
        
        assertTrue ("Is leaf", fn.isLeaf ());

	// change the original children so it have to be updated in fn
        a.setChildren(new Children.Array ());
        
        class ReadAccess implements Runnable {
            public void run () {
                assertTrue (
                    "It still claims that it is leaf because it cannot call setChildren to update, " +
                    " because that would upgrade the lock from read to write and that is not allowed", fn.isLeaf ());
            }
        }
        Children.MUTEX.readAccess (new ReadAccess ());
        
            
        assertFalse ("But as soon as the read access ends it is updated", fn.isLeaf ());
    }

    public void testGetSetValue() {
        AbstractNode node = new AbstractNode (Children.LEAF);

        FN fn_no = new FN (node);
        fn_no.disableDel(FN.DELEGATE_SET_VALUE | FN.DELEGATE_GET_VALUE);

        FN fn_get = new FN (node);
        fn_get.disableDel(FN.DELEGATE_SET_VALUE);

        FN fn_set = new FN (node);
        fn_set.disableDel(FN.DELEGATE_GET_VALUE);

        FilterNode fn_both = new FilterNode (node);
        
        node.setValue("val1", "item1");
        assertTrue("Should not delegate getValue", fn_no.getValue("val1") == null);
        assertEquals("Should delegate getValue", "item1", fn_get.getValue("val1"));
        assertTrue("Should not delegate getValue", fn_set.getValue("val1") == null);
        assertEquals("Should delegate getValue", "item1", fn_both.getValue("val1"));

        fn_no.setValue("val1", "xxx");
        assertEquals("Should have the value", "xxx", fn_no.getValue("val1"));
        assertEquals("Should not propagate setValue", "item1", node.getValue("val1"));

        fn_get.setValue("val1", "xxx");
        assertEquals("Should still detegate getValue", "item1", fn_get.getValue("val1"));
        assertEquals("Should not propagate setValue", "item1", node.getValue("val1"));

        fn_set.setValue("val1", "item2");
        assertTrue("Should not delegate getValue", fn_set.getValue("val1") == null);
        assertEquals("Should propagate setValue", "item2", node.getValue("val1"));

        fn_both.setValue("val1", "item3");
        assertTrue("Should still detegate getValue", fn_both.getValue("val3") == null);
        assertEquals("Should propagate setValue", "item3", node.getValue("val1"));
    }
    
    public void testChildrenFireCorrectEvents () throws Exception {
        doChildrenFireCorrectEvents (false);
    }
    
    public void testSubclassedChildrenFireCorrectEvents () throws Exception {
        doChildrenFireCorrectEvents (true);
    }
    
    private void doChildrenFireCorrectEvents (boolean subclassedChildren) throws Exception {
        ChildrenKeysTest.Keys k = new ChildrenKeysTest.Keys (new String[] { "1", "2", "3" });
        AbstractNode an = new AbstractNode (k);
        
        FilterNode fn;
        if (subclassedChildren) {
            class Sub extends FilterNode.Children {
                public Sub (Node n) {
                    super (n);
                }
            }
            fn = new FilterNode (an, new Sub (an));
        } else {
            fn = new FilterNode (an);
        }
        ChildrenKeysTest.Listener l = new ChildrenKeysTest.Listener ();
        fn.addNodeListener (l);
        
        assertEquals ("Three", 3, fn.getChildren ().getNodesCount ());

        l.assertNoEvents ("Well, we are asking for the first time");
        
        Node n1, n2;
        n1 = fn.getChildren ().getNodeAt (0);
        n2 = fn.getChildren ().getNodeAt (2);
        assertEquals ("Name is 1", "1", n1.getName ());
        assertEquals ("Name is 3", "3", n2.getName ());
        
        l.assertNoEvents ("No changes that would be observable from outside");
        
        k.keys (new String[] { "1", "3"});
        
        NodeMemberEvent ev = l.assertEvents (1);
        assertEquals ("Removal event type", NodeMemberEvent.class, ev.getClass ());
        assertFalse ("It is removal", ev.isAddEvent ());
        int[] removed = ev.getDeltaIndices ();
        assertEquals ("One node gone", 1, removed.length);
        assertEquals ("Middle one", 1, removed[0]);
    }
    
    public void testFilterNodeCanGCNodes () {
        class K extends Children.Keys {
            public int addNotify;
            public int removeNotify;
            public int optimal;
            public int nonoptimal;
            public java.lang.ref.Reference keyRef;
            
            protected void addNotify () {
                addNotify++;
                
                Integer key = new Integer (50);
                setKeys (Collections.singleton (key));
                keyRef = new java.lang.ref.WeakReference (key);
            }
            
            protected void removeNotify () {
                removeNotify++;
                setKeys (Collections.EMPTY_LIST);
            }
            
            
            public Node[] getNodes (boolean optimal) {
                if (optimal) {
                    assertEquals ("No addNotify yet", 0, addNotify);
                    this.optimal++;
                } else {
                    this.nonoptimal++;
                }
                Node[] ret = super.getNodes ();
                assertEquals ("addNotify done", 1, addNotify);
                return ret;
            }
            
            protected Node[] createNodes (Object obj) {
                return new Node[] { Node.EMPTY.cloneNode () };
            }
        }
        K k = new K ();
        AbstractNode n = new AbstractNode (k);
        FilterNode fn = new FilterNode (n);
        
        Node[] arr = fn.getChildren ().getNodes (true);
        assertEquals ("Add notify called", 1, k.addNotify);
        assertEquals ("optimal called", 1, k.optimal);
        assertEquals ("nonoptimal not called", 0, k.nonoptimal);
        assertEquals ("One node", 1, arr.length);
        
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (arr[0]);
        assertEquals ("No removeNotify", 0, k.removeNotify);
        arr = null;
        assertGC ("The node can go away", ref);
        assertGC ("Key can go away", k.keyRef);
        assertEquals ("One remove notify", 1, k.removeNotify);
        
        arr = fn.getChildren ().getNodes ();
        assertEquals ("Add notify called once more", 2, k.addNotify);
        assertEquals ("optimal stays as it was", 1, k.optimal);
        assertEquals ("nonoptimal stays", 0, k.nonoptimal);
        assertEquals ("still one remove", 1, k.removeNotify);
        assertEquals ("nonoptimal not called", 0, k.nonoptimal);
        assertEquals ("One node", 1, arr.length);
        
        k.setKeys (new Object[] { new Integer (10), new Integer (50), new Integer (70) });
        
        arr = fn.getChildren ().getNodes ();
        
        assertEquals ("Three", 3, arr.length);
        assertEquals ("Add notify called once more", 2, k.addNotify);
        assertEquals ("optimal stays as it was", 1, k.optimal);
        assertEquals ("nonoptimal stays", 0, k.nonoptimal);
        assertEquals ("still one remove", 1, k.removeNotify);
        assertEquals ("nonoptimal not called", 0, k.nonoptimal);
    }

    public void testChangesInKeysPropagatedCorrectlyIntoFilterNodeKeys () {
        class K extends Children.Keys {
            public int addNotify;
            public int removeNotify;
            public int optimal;
            
            protected void addNotify () {
                addNotify++;
            }
            
            protected void removeNotify () {
                removeNotify++;
                setKeys (Collections.EMPTY_LIST);
            }
            
            
            public Node[] getNodes (boolean optimal) {
                if (optimal) {
                    Integer key = new Integer (50);
                    setKeys (Collections.singleton (key));
                    this.optimal++;
                }
                Node[] ret = super.getNodes ();
                return ret;
            }
            
            protected Node[] createNodes (Object obj) {
                return new Node[] { Node.EMPTY.cloneNode () };
            }
        }
        K k = new K ();
        AbstractNode n = new AbstractNode (k);
        FilterNode fn = new FilterNode (n);
        
        Node[] arr = fn.getChildren ().getNodes ();
        assertEquals ("No nodes", 0, arr.length);
        
        arr = fn.getChildren ().getNodes (true);
        assertEquals ("Add notify called", 1, k.addNotify);
        assertEquals ("optimal called", 1, k.optimal);
        assertEquals ("One node", 1, arr.length);
        
        k.setKeys (new Object[] { new Integer (1), new Integer (50) });
        
        arr = fn.getChildren ().getNodes ();
        assertEquals ("Two nodes", 2, arr.length);
        
    }
    
    public void testFilterNodeChildrenThatOnceReturnedNullAreThenEmptyForTheRestOfNodesBug () {
        doFilterNodeChildrenThatOnceReturnedNullAreThenEmptyForTheRestOfNodesBug (false);
    }
    
    public void testFilterNodeChildrenThatOnceReturnedNullAreThenEmptyForTheRestOfNodesBugEvenWithBefore () {
        doFilterNodeChildrenThatOnceReturnedNullAreThenEmptyForTheRestOfNodesBug (true);
    }
    
    private void doFilterNodeChildrenThatOnceReturnedNullAreThenEmptyForTheRestOfNodesBug (boolean before) {
        final Children.Array ch = new Children.Array ();
        ch.add (new Node[] { 
            createNode ("1"),
            createNode ("2"), // this node should not be in the filter node, others should
            createNode ("3"),
        }); 
        final AbstractNode an = new AbstractNode (ch);
        
        
        class K extends FilterNode.Children {
            public boolean nodeFound;
            
            public K () {
               super (an);
            }
            
            @Override
            protected Node[] createNodes(Node o) {
                Node n = ch.getNodes()[1];
                if (o == n) {
                    nodeFound = true;
                    return null;
                }
                return super.createNodes (o);
            }
        }        
        K k = new K ();
        k.setBefore (before);
        FilterNode fn = new FilterNode (an, k);
        
        Node[] arr = fn.getChildren ().getNodes ();
        assertTrue ("The createNodes method was called for the right node", k.nodeFound);
        assertEquals ("There are two nodes", 2, arr.length);
        
    }
    
    private static AbstractNode createNode (String name) {
        AbstractNode an = new AbstractNode (Children.LEAF);
        an.setName (name);
        return an;
    }
    
    
    /** A class that allows access to protected methods.
     */
    private static final class FN extends FilterNode {
        public FN (Node orig) {
            super (orig);
        }
        
        public void changeCh (Node n, boolean children) {
            changeOriginal (n, children);
        }
        
        public void disableDel (int mask) {
            disableDelegation(mask);
        }

    }
    
    public void testLookupNode() {
        class NodeA extends AbstractNode {
            public NodeA() {
                super(Children.LEAF);
            }
        }
        
        class NodeB extends AbstractNode {
            public NodeB() {
                 this(Children.LEAF, new InstanceContent());
            }
            
            NodeB(Children ch, InstanceContent ic) {
                super(ch, new AbstractLookup(ic));
                ic.add(this);
            }
        }
        
        FilterNode n = new FilterNode(new NodeB());
        Object o = n.getLookup().lookup(NodeA.class);
        assertNull("There is no instance of NodeA in the lookup, we should get null here:" + o, o);

        Lookup.Item item = n.getLookup().lookupItem(new Lookup.Template(NodeA.class));
        assertNull("There is no instance of NodeA in the lookup, there shall be no item:" + item, item);
        
        Lookup.Result res = n.getLookup().lookupResult(NodeA.class);
        Collection c;
        c = res.allClasses();
        assertTrue("No classes:" + c, c.isEmpty());
        c = res.allItems();
        assertTrue("No items:" + c, c.isEmpty());
        c = res.allInstances();
        assertTrue("No instances:" + c, c.isEmpty());
    }
    
    public void testNoClassCast89329() throws Exception {
        InstanceContent ic = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(ic);
        AbstractNode a = new AbstractNode(Children.LEAF, lookup);
        FilterNode f = new FilterNode(a);
        
        ic.add("Kuk");
        
        Class what = String.class;
        assertNull("Indeed null, string is not a cookie", f.getCookie(what));
        assertEquals("Kuk", f.getLookup().lookup(String.class));
    }
    public void testNoClass2Cast89329() throws Exception {
        InstanceContent ic = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(ic);
        AbstractNode a = new AbstractNode(Children.LEAF, lookup);
        
        class F extends FilterNode implements OpenCookie {
            public F(Node n) {
                super(n);
            }
            public Node.Cookie getCookie(Class type) {
                if (OpenCookie.class.isAssignableFrom(type)) return this;
                else return super.getCookie(type);
            }

            public void open() {
            }
        }
        
        FilterNode f = new F(a);
        
        ic.add("Kuk");
        
        Class what = String.class;
        assertNull("Indeed null, string is not a cookie", f.getCookie(what));
        assertEquals("Kuk", f.getLookup().lookup(String.class));
    }
}


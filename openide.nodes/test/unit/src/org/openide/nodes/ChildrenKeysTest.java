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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

public class ChildrenKeysTest extends NbTestCase {
    private CharSequence err;
    private Logger LOG;
    
    public ChildrenKeysTest(java.lang.String testName) {
        super(testName);
    }
    
    protected Node createNode (Children ch) {
        return new AbstractNode (ch);
    }
    
    public static junit.framework.Test suite() {
        return new ChildrenKeysTest("testGetNodesFromTwoThreads57769");
        //return new NbTestSuite(ChildrenKeysTest.class);
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    

    protected void setUp () throws Exception {
        err = Log.enable("", Level.ALL);
        LOG = Logger.getLogger("test." + getName());
    }    

    public void testGetNodesFromTwoThreads57769() throws Exception {
        final Ticker t1 = new Ticker();
        final List who = new java.util.Vector();
        
        final int[] count = new int[1];
        Children children= new Children.Keys() {
            protected Node[] createNodes(Object key) {
                StringWriter msg = new StringWriter();
                msg.write("Creating: " + count[0] + " for key: " + key + "\n");
                new Exception().printStackTrace(new PrintWriter(msg));
                LOG.log(Level.INFO, msg.toString());
                count[0]++;
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                try {Thread.sleep(2000);}catch(InterruptedException e) {}
                return n == null ? new Node[]{} : new Node[]{n};
            }

            protected void addNotify() {
                setKeys(Arrays.asList(new Object[] {"1", "2"}));
            }            
        };
        final Node node = new AbstractNode(children);
        
        // Get optimal nodes from other thread
        Thread t = new Thread("THREAD") {
            Node[] keep;
            public void run() {
                t1.tick();
                keep = node.getChildren().getNodes(true);
            }
        };
        t.start();
        t1.waitOn();
        
        // and also from main thread
        Node[] remember = node.getChildren().getNodes();
        
        // wait for other thread
        t.join();
        
        if (2 != count[0]) {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            w.write("Just two nodes created: " + count[0] + " stacks:\n");
            Iterator it = who.iterator();
            while (it.hasNext()) {
                Exception e = (Exception)it.next();
                e.printStackTrace(pw);
            }
            pw.close();
            
            fail(w.toString());;
        }//fail("Ok");
    }

    /**
     * See #78519
     * T1 has write access and gets preempted just before call to
     * getNodes() by another thread callig getNodes.
     * Other thread
     */
    public void testGetNodesFromWriteAccess() throws Exception {
        final String[] keys = { "Nenik", "Tulach" };
        Keys o = new Keys (keys);
        Node orig = new AbstractNode(o);
        Node filter = new FilterNode(orig);
        final Children k = filter.getChildren();
        
        final Ticker t1 = new Ticker();
        final Ticker tick2 = new Ticker();
        final boolean[] done = new boolean[2];
        
        // Try to get nodes from writeAccess
        Thread t = new Thread("preempted") {
            public void run() {
                Children.PR.enterWriteAccess();
                try {
                    t1.tick(); // I do have the write access ...
                    tick2.waitOn(); // ... so wait till I'm preempted                    
                    k.getNodes();
                } finally {
                    Children.PR.exitWriteAccess();
                }
                done[0] = true;
            }
        };
        t.start();
        t1.waitOn();
        
        // and also from another thread
        Thread t2 = new Thread("other") {
            public void run() {
                k.getNodes(); // will block in getNodes
                done[1] = true;
            }
        };
        t2.start();
        
        Thread.sleep(2000); // give T2 some time ...
        tick2.tick(); // and unfuse T
        
        // wait for other thread
        t.join(2000);
        t2.join(2000);

        t.stop();
        t2.stop();

        assertTrue ("Preempted thread finished correctly", done[0]);
        assertTrue ("Other thread finished correctly", done[1]);
    }

    /**
     * See issue #76614
     */
    public void testNodesCreatedJustOnce() throws Exception {
        Counter children = new Counter(1);
        Node node = new AbstractNode(children);
        children.keys(Arrays.asList(new Object[] {"Add Children"}));
        Node[] nodes = node.getChildren().getNodes(true);
        
        assertEquals("One node returned", 1, nodes.length);
        assertEquals("One node created", 1, children.count);
    }        

    
    public void testDestroyIsCalledWhenANodeIsRemovedOrig () throws Exception {
        class K extends Keys {
            public Node[] arr;
            
            protected void destroyNodes (Node[] arr) {
                super.destroyNodes (arr);
                assertNull ("No destroy before", this.arr);
                this.arr = arr;
            }
        }
        
        K k = new K ();
        k.keys (new String[] { "A", "B", "C" });
        
        Node[] n = k.getNodes ();
        assertEquals ("3", 3, n.length);
        assertNull ("Still no destroy", k.arr);
        
        k.keys (new String[] { "A" });
        assertNotNull ("Some destroyed", k.arr);
        assertEquals ("2 destroyed", 2, k.arr.length);
        k.arr = null;
        n = k.getNodes ();
        assertEquals ("! left", 1, n.length);
        
        WeakReference ref = new WeakReference (n[0]);
        n = null;
        assertGC ("Node can be gced", ref);
        
        assertNull ("Garbage collected nodes are not notified", k.arr);
    }

    public void testDestroyIsCalledWhenANodeIsRemoved () throws Exception {
        class K extends Keys {
            public Node[] arr;
            
            protected void destroyNodes (Node[] arr) {
                super.destroyNodes (arr);
                assertNull ("No destroy before", this.arr);
                this.arr = arr;
            }
        }
        
        K k = new K ();
        k.keys (new String[] { "A", "B", "C" });
        Node node = createNode (k);
        
        Node[] n = node.getChildren ().getNodes ();
        assertEquals ("3", 3, n.length);
        assertNull ("Still no destroy", k.arr);
        
        k.keys (new String[] { "A" });
        assertNotNull ("Some destroyed", k.arr);
        assertEquals ("2 destroyed", 2, k.arr.length);
        k.arr = null;
        n = node.getChildren ().getNodes ();
        assertEquals ("! left", 1, n.length);
        
        WeakReference ref = new WeakReference (n[0]);
        n = null;
        assertGC ("Node can be gced", ref);
        
        assertNull ("Garbage collected nodes are not notified", k.arr);
    }

    public void testDestroyIsCalledWhenEntryIsRefreshed () throws Exception {
        class K extends Keys {
            public Node[] arr;
            public Node[] toReturn;
            
            protected void destroyNodes (Node[] arr) {
                super.destroyNodes (arr);
                assertNull ("No destroy before", this.arr);
                this.arr = arr;
            }
            
            protected Node[] createNodes (Object key) {
                if (toReturn != null) {
                    return toReturn;
                } else {
                    return super.createNodes (key);
                }
            }
        }
        
        K k = new K ();
        k.keys (new String[] { "A", "B", "C" });
        Node node = createNode (k);
        
        Node[] n = node.getChildren ().getNodes ();
        assertEquals ("3", 3, n.length);
        assertNull ("Still no destroy", k.arr);
        
        k.toReturn = new Node[0];
        k.refreshKey ("A");
        
        assertNotNull ("Some destroyed", k.arr);
        assertEquals ("1 destroyed", 1, k.arr.length);
        k.arr = null;
        n = node.getChildren ().getNodes ();
        assertEquals ("B C", 2, n.length);
        
        WeakReference ref = new WeakReference (n[0]);
        n = null;
        assertGC ("Node can be gced", ref);
        
        assertNull ("Garbage collected nodes are not notified", k.arr);
    }
    
    public void testRefreshKeyCanBeCalledFromReadAccess () throws Exception {
        final String[] keys = { "Hrebejk", "Tulach" };
        final Keys k = new Keys (keys);
        
        Keys.MUTEX.readAccess (new Runnable () {
            public void run () {
                k.refreshKey ("Hrebejk");
            }
        });

        if (err.toString ().indexOf ("readAccess") >= 0) {
            fail ("Should not contain messages about going from read to write access: " + err);
        }
    }


    public void testGCKeys () throws Exception {
        class K extends Children.Keys {
            int counterAdd = 0;
            int counterRem = 0;
            Object key;
            
            Reference createdNode;
            
            K(Object keyObject) {
                key = keyObject;
            }
            
            protected void addNotify() {
                counterAdd++;
                setKeys(Collections.singleton(key));
            }
            
            protected void removeNotify() {
                counterRem++;
                setKeys(Collections.EMPTY_LIST);
                key = null;
            }
            
            protected Node[] createNodes(Object k) {
                Node n = Node.EMPTY.cloneNode();
                assertNull ("Just one created node", createdNode);
                createdNode = new WeakReference (n);
                return new Node[] { n };
            }
        }
        
        Object myKey = new Object();
        K temp = new K(myKey);
        Node node = createNode (temp);
        
        assertEquals("not touched", 0, temp.counterAdd);
        assertEquals("not touched", 0, temp.counterRem);
        
        Node[] arr = node.getChildren ().getNodes();
        
        assertEquals("initialized", 1, temp.counterAdd);
        assertEquals("not touched", 0, temp.counterRem);
        assertEquals("one item", 1, arr.length);

        WeakReference ref = new WeakReference(arr[0]);
        arr = null;
        assertGC("node freed", ref);
        assertGC("and this one as well", temp.createdNode);
        
        assertEquals("initialized", 1, temp.counterAdd);
        assertEquals("removed", 1, temp.counterRem);
        
        ref = new WeakReference(myKey);
        myKey = null;
        assertGC("key freed", ref);
        
    }
    
    public void testIndexesAreCorrectWhenInsertingAnObject () {
        doIndexesAreCorrectWhenInsertingAnObject ("B", 3);
    }
    
    public void testIndexesAreCorrectWhenInsertingAnObjectNext () {
        doIndexesAreCorrectWhenInsertingAnObject ("1", 4);
    }
    
    private void doIndexesAreCorrectWhenInsertingAnObject (String add, int index) {
        Keys k = new Keys ();
        Node n = createNode (k);
        
        assertEquals ("Empty", 0, n.getChildren ().getNodesCount ());
        Node[] arr = n.getChildren ().getNodes ();
        
        Listener l = new Listener ();
        n.addNodeListener(l);
        
        
        ArrayList list = new ArrayList ();
        list.add ("A");
        list.add ("B");
        list.add ("1");
        list.add ("2");
        list.add ("3");
        k.setKeys(list);
        
        l.assertAddEvent ("Added 5", new int[] { 0, 1, 2, 3, 4 });
        Node[] newArr = n.getChildren ().getNodes ();
        
        list.add (2, "0");
        list.add (3, add);
        k.setKeys (list);
        
        l.assertAddEvent ("Added 2", new int[] { 2, index });
        l.assertNoEvents("And that is all");
    }
    
    public void testAddingALotOfItems () {
        Keys k = new Keys ();
        Node n = createNode (k);
        
        assertEquals ("Empty", 0, n.getChildren ().getNodesCount ());
        Node[] arr = n.getChildren ().getNodes ();
        
        Listener l = new Listener ();
        n.addNodeListener(l);
        
        
        ArrayList list = new ArrayList ();
        list.add ("A");
        list.add ("B");
        list.add ("1");
        list.add ("2");
        list.add ("3");
        k.setKeys(list);
        
        l.assertAddEvent ("Added 5", new int[] { 0, 1, 2, 3, 4 });
        Node[] newArr = n.getChildren ().getNodes ();
        
        list.add (2, "0.6");
        list.add (2, "0.5");
        list.add (2, "0.4");
        list.add (2, "0.3");
        list.add (2, "0.2");
        list.add (2, "0.1");
        k.setKeys (list);
        
        l.assertAddEvent ("Added 6", new int[] { 2, 3, 4, 5, 6, 7 });
        l.assertNoEvents("And that is all");
    }
    
    /** Check whether a nodes appears when keys are set.
    */
    public void testGetNodes () throws Exception {
        String[] arr = { "1", "2", "3", "4" };
        Children ch = new Keys (arr);
        checkNames (createNode (ch), arr);
    }
    
    public void testGetNodesOptimalOn0to1 () throws Exception {
        class K extends Keys {
            private boolean acceptOposite;
            
            public K () {
                super (0, 1);
            }
            
            protected Node[] createNodes (Object k) {
                boolean a = k instanceof String;
                if (acceptOposite) {
                    a = !a;
                }
                if (a) {
                    return super.createNodes (k);
                } else {
                    return null;
                }
            }
        }
        
        
        K k = new K ();
        Object[] keys = { "Ahoj", new Integer (3), "Kuk", new Integer (2) };
        k.setKeys (keys);
        Node[] arr = k.getNodes (true);
        assertEquals ("Just two", 2, arr.length);
        assertEquals ("Ahoj", arr[0].getName ());
        assertEquals ("Kuk", arr[1].getName ());
        
        try {
            Children.PR.enterWriteAccess ();
            k.acceptOposite = true;
            k.setKeys (new Object[0]);
            k.setKeys (keys);
        } finally {
            Children.PR.exitWriteAccess ();
        }
        
        arr = k.getNodes (true);
        assertEquals ("Just two", 2, arr.length);
        assertEquals ("3", arr[0].getName ());
        assertEquals ("2", arr[1].getName ());
    }
    
    public void testNoReorderEventJustAdd () {
        Keys k = new Keys (new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        Node[] toPreventGC = n.getChildren ().getNodes ();
        assertEquals (10, toPreventGC.length);
        
        k.keys (new String[] { "31", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
        
        l.assertAddEvent ("Adding one index", 1);
        l.assertNoEvents ("And that is all");
    }
    
    public void testComplexReorderAndAddAndRemoveEvent () {
        Keys k = new Keys (new String[] { "remove", "1", "0" });
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        Node[] toPreventGC = n.getChildren ().getNodes ();
        assertEquals (3, toPreventGC.length);
        
        k.keys (new String[] { "0", "1", "add" });

        l.assertRemoveEvent ("Removed index 0", 1);
        l.assertReorderEvent ("0->1 and 1->0", new int[] { 1, 0 });
        l.assertAddEvent ("Adding at index 2", 1);
        l.assertNoEvents ("And that is all");

        Node[] arr = n.getChildren ().getNodes ();
        assertEquals (3, arr.length);
        assertEquals ("0", arr[0].getName ());
        assertEquals ("1", arr[1].getName ());
        assertEquals ("add", arr[2].getName ());
    }
    
    /** Check refresh of nodes.
     */
    public void testResetOfNodes () throws Exception {
        String[] arr;
        Keys ch = new Keys ();
        arr = new String[] { "1", "2" };
        Node node = createNode (ch);
        
        ch.keys (arr);
        checkNames (node, arr);
        
        arr = new String[] { "X", "Y", "Z" };
        ch.keys (arr);
        checkNames (node, arr);
        
        Collections.reverse (Arrays.asList (arr));
        ch.keys (arr);
        checkNames (node, arr);
    }
    
    /** Tests whether nodes in children have the correct names on correct places
     * @param ch children
     * @param arr names
     */
    private void checkNames (Node ch, String[] arr) {
        Node[] nodes = ch.getChildren ().getNodes ();
        
        if (nodes.length != arr.length) {
            fail ("Keys: " + arr.length + " Nodes: " + nodes.length);
        }
        
        for (int i = 0; i < arr.length; i++) {
            if (!nodes[i].getName ().equals (arr[i])) {
                fail (i + "th: name: " + nodes[i].getName () + " key: " + arr[i]);
            }
        }
    }
    
    public void testGetAndRemove () {
        Keys k = new Keys (new String[] { "1", "2" });
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        
        Node d1 = n.getChildren ().getNodeAt (0);
        assertEquals ("Name is 1", "1", d1.getName ());
        
        k.keys (new String[] { "2" });
        
        l.assertRemoveEvent ("One node removed", 1);
        assertNull (d1.getParentNode ());
    }

    public void testSetBefore () {
        Keys k = new Keys (new String[] { "Ahoj" });
        k.add (new Node[] { Node.EMPTY.cloneNode () });
        
        Node node = createNode (k);
        
        Node[] arr;
        
        arr = node.getChildren ().getNodes ();
        assertEquals (2, arr.length);
        assertEquals ("First is ahoj", "Ahoj", arr[0].getName ());
        assertEquals ("2nd equlas to EMPTY", Node.EMPTY, arr[1]);
        
        k.setBefore (true);
        
        arr = node.getChildren ().getNodes ();
        assertEquals (2, arr.length);
        assertEquals ("2nd is ahoj", "Ahoj", arr[1].getName ());
        assertEquals ("First equals to EMPTY", Node.EMPTY, arr[0]);
        
        k.setBefore (false);
        
        arr = node.getChildren ().getNodes ();
        assertEquals (2, arr.length);
        assertEquals ("First is ahoj", "Ahoj", arr[0].getName ());
        assertEquals ("2nd equlas to EMPTY", Node.EMPTY, arr[1]);
        
    }
    
    public void testOperationsOnEqualNumberOfMinAndMax () throws Exception {
        Keys k = new Keys (1, 1);
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        
        assertEquals ("No nodes", 0, n.getChildren ().getNodesCount ());
        k.keys (new String[] { "Ahoj", "Kuk" });
        
        NodeMemberEvent mem = l.assertEvents (1);

        assertEquals ("Two nodes", 2, n.getChildren ().getNodesCount ());
    }

    public void testChildrenFireCorrectEvents () throws Exception {
        ChildrenKeysTest.Keys k = new ChildrenKeysTest.Keys (new String[] { "1", "2", "3" });
        Node fn = createNode (k);
        ChildrenKeysTest.Listener l = new ChildrenKeysTest.Listener ();
        fn.addNodeListener (l);
        
        assertEquals ("Three", 3, fn.getChildren ().getNodesCount ());
        
        Node n1, n2;
        n1 = fn.getChildren ().getNodeAt (0);
        n2 = fn.getChildren ().getNodeAt (2);
        assertEquals ("Name is 1", "1", n1.getName ());
        assertEquals ("Name is 3", "3", n2.getName ());
        
        k.keys (new String[] { "1", "3"});
        
        NodeMemberEvent ev = l.assertEvents (1);
        assertEquals ("Removal event type", NodeMemberEvent.class, ev.getClass ());
        int[] removed = ev.getDeltaIndices ();
        assertEquals ("One node gone", 1, removed.length);
        assertEquals ("Middle one", 1, removed[0]);
    }

    public void testRemovedNodesWillHaveParentRemoved () throws Exception {
        Keys k = new Keys (new String[] { "Ahoj", "Kuk" });
        Node n = createNode (k);
        
        Node[] arr = n.getChildren ().getNodes ();
        assertEquals ("Two", 2, arr.length);
        assertEquals ("Parent1", n, arr[0].getParentNode ());
        assertEquals ("Parent2", n, arr[1].getParentNode ());
        
        k.keys (new String[0]);
        
        Node[] newArr = n.getChildren ().getNodes();
        assertEquals ("Zero is current number of children", 0, newArr.length);
        assertNull ("Old node parent zeroed1", arr[0].getParentNode ());
        assertNull ("Old node parent zeroed2", arr[1].getParentNode ());
    }
    
    
    public void testRefreshClearsSizeWithoutLimits () throws Exception {
        doRefreshClearsSize (0, 0);
    }
    
    public void testRefreshClearsSizeOto1 () throws Exception {
        doRefreshClearsSize (0, 1);
    }
    
    private void doRefreshClearsSize (int min, int max) throws Exception {
        final String[] NULL = { "Null" };
        
        Keys k = new Keys (min, max) {
            protected Node[] createNodes (Object o) {
                if (o == NULL) {
                    if (NULL[0] == null) {
                        return null;
                    }
                    o = NULL[0];
                }
                return super.createNodes (o);
            }
        };
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        
        assertEquals ("No nodes", 0, n.getChildren ().getNodesCount ());
        k.setKeys (new Object[] { "Ahoj", NULL });
        assertEquals ("Two nodes", 2, n.getChildren ().getNodesCount ());
        NULL[0] = null;
        k.refreshKey (NULL);
        assertEquals ("Just one node", 1, n.getChildren ().getNodesCount ());
    }

    public void testGetNodesFromTwoThreads57769WhenBlockingAtRightPlaces() throws Exception {
        final Ticker t1 = new Ticker();
        final List who = new java.util.Vector();
        
        final int[] count = new int[1];
        class ChildrenKeys extends Children.Keys {
            protected Node[] createNodes(Object key) {
                who.add(new Exception("Creating: " + count[0] + " for key: " + key));
                count[0]++;
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                try {Thread.sleep(2000);}catch(InterruptedException e) {}
                return n == null ? new Node[]{} : new Node[]{n};
            }

            protected void addNotify() {
                setKeys(Arrays.asList(new Object[] {"1", "2"}));
            }            
        };
        
        // Get optimal nodes from other thread
        
        final String TOKEN = new String("TOKEN to wait on");
        final String THREAD_NAME = Thread.currentThread().getName();
        
        class Th extends Thread {
            public Th() {
                super("THREAD");
            }
            Node node;
            Node[] keep;
            Error err;
            
            public void run() {
                synchronized (TOKEN) {
                }
     
                try {
                    keep = node.getChildren().getNodes(true);
                } catch (Error err) {
                    this.err = err;
                }
            }
        }
        
        Th t = new Th();
        ChildrenKeys children = new ChildrenKeys();
        t.node = new AbstractNode(children);

        Node[] remember;
        synchronized (TOKEN) {
            t.start();

            // this call will result in "point X".notifyAll() when the 
            // main thread reaches "setEntries"
            remember = t.node.getChildren().getNodes();
            
            TOKEN.notifyAll();
        }
        
        // wait for other thread
        t.join();
        
        if (2 != count[0]) {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            w.write("Just two nodes created: " + count[0] + " stacks:\n");
            Iterator it = who.iterator();
            while (it.hasNext()) {
                Exception e = (Exception)it.next();
                e.printStackTrace(pw);
            }
            pw.close();
            
            fail(w.toString());;
        }
        
        if (t.err != null) {
            throw t.err;
        }
        
        //fail("Ok");
    }

    
    /** Sample keys.
    */
    public static class Keys extends Children.Keys {
        public Keys () {
        }
        
        public Keys (int minKeys, int maxKeys) {
            super(/*XXX what was this for? minKeys, maxKeys*/);
        }
        
        /** Constructor.
         */
        public Keys (String[] args) {
            setKeys (args);
        }
        
        /** Changes the keys.
         */
        public void keys (String[] args) {
            super.setKeys (args);
        }

        /** Changes the keys.
         */
        public void keys (Collection args) {
            super.setKeys (args);
        }
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no 
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode (Children.LEAF);
            an.setName (key.toString ());

            return new Node[] { an };
        }

    }
    
    public static class Counter extends Children.Keys {
        int limit;
        int count = 0;
        
        public Counter (int limit) {
            this.limit = limit;
        }
        
        
        /** Changes the keys.
         */
        public void keys (String[] args) {
            super.setKeys (args);
        }

        /** Changes the keys.
         */
        public void keys (Collection args) {
            super.setKeys (args);
        }
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no 
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            synchronized(this) {
                count++;
                assertTrue("# of created nodes", count <= limit);
            }
            AbstractNode an = new AbstractNode (Children.LEAF);
            an.setName (key.toString ());

            return new Node[] { an };
        }

    }

    static class Listener extends NodeAdapter {
        private LinkedList events = new LinkedList ();
        
        
        public void childrenRemoved (NodeMemberEvent ev) {
            events.add (ev);
        }

        public void childrenAdded (NodeMemberEvent ev) {
            events.add (ev);
        }

        public void childrenReordered (NodeReorderEvent ev) {
            events.add (ev);
        }
        
        public NodeMemberEvent assertEvents (int number) {
            if (events.size () != number) {
                fail ("There should be " + number + " event(s) but was :" + events.size () + ":\n" + events);
            }
            return (NodeMemberEvent)events.removeFirst ();
        }
        
        public void assertAddEvent (String msg, int cnt) {
            checkOneEvent (msg, cnt, null, true);
        }
        public void assertRemoveEvent (String msg, int cnt) {
            checkOneEvent (msg, cnt, null, false);
        }
        public void assertAddEvent (String msg, int[] indexes) {
            checkOneEvent (msg, indexes.length, indexes, true);
        }
        public void assertRemoveEvent (String msg, int[] indexes) {
            checkOneEvent (msg, indexes.length, indexes, false);
        }
        
        public void assertReorderEvent (String msg, int[] perm) {
            assertFalse (msg + " Cannot be empty", events.isEmpty ());
            Object o = events.removeFirst ();
            assertEquals (msg + " Reoder event", NodeReorderEvent.class, o.getClass ());
            NodeReorderEvent m = (NodeReorderEvent)o;
            
            int[] arr = m.getPermutation ();
            FAIL: if (arr.length == perm.length) {
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] != perm[i]) break FAIL;
                }
                return;
            }
            
            StringBuffer sb = new StringBuffer ();
            
            for (int i = 0; i < arr.length; i++) {
                sb.append ("at [" + i + "]: ");
                if (arr.length > i) {
                    sb.append (arr[i]);
                } else {
                    sb.append ("none");
                }
                sb.append (" = ");
                if (perm.length > i) {
                    sb.append (perm[i]);
                } else {
                    sb.append ("none");
                }
            }
            fail (sb.toString ());
        }
        
        private void checkOneEvent (String msg, int cnt, int[] indexes, boolean add) {
            assertFalse (msg + " Cannot be empty", events.isEmpty ());
            Object o = events.removeFirst ();
            assertEquals (msg + " Remove event", NodeMemberEvent.class, o.getClass ());
            NodeMemberEvent m = (NodeMemberEvent)o;
            if (add) {
                assertTrue (msg + " is add ", m.isAddEvent ());
            } else {
                assertFalse (msg + " Is remove", m.isAddEvent ());
            }
            assertEquals (msg + " Right count of removed nodes", cnt, m.getDelta ().length);
            assertEquals (msg + " Right count of removed indicies", cnt, m.getDeltaIndices ().length);
            
            if (indexes != null) {
                StringBuffer f = new StringBuffer ();
                boolean ok = true;
                for (int i = 0; i < cnt; i++) {
                    f.append ("[" + i + "]: " + indexes[i] + " = " + m.getDeltaIndices ()[i] + "\n");
                    ok = ok && indexes[i] == m.getDeltaIndices ()[i];
                }
                if (!ok) {
                    fail ("Indicies are not correct:\n" + f);
                }
            }
        }
        
        public void assertNoEvents (String msg) {
            assertEquals (msg, 0, events.size ());
        }
        
    } // end of Listener

    Ticker t1 = new Ticker();
        
    private static class Ticker {
        boolean state;
        
        public void waitOn() {
            synchronized(this) {
                while (!state) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new InternalError();
                    }
                }
                state = false; // reusable
            }
        }
        
        public void tick() {
            synchronized(this) {
                state = true;
                notifyAll();
            }
        }
    }

    
}

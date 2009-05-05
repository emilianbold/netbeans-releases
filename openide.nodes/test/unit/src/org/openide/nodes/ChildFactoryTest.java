/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.openide.nodes;

import java.awt.EventQueue;
import java.beans.*;
import java.util.*;
import junit.framework.TestCase;
import org.openide.nodes.ChildFactory.Detachable;
import org.openide.util.NbBundle;

/** Test for AsynchChildren, ChildFactory and SynchChildren.
 *
 * @author Tim Boudreau
 */
public class ChildFactoryTest extends TestCase {
    
    public ChildFactoryTest(String name) {
        super(name);
    }
    
    private ProviderImpl factory;
    
    private BatchProviderImpl factory2;
    private AsynchChildren kids2;
    private Node node2;
    private AsynchChildren kids;
    private Node node;
    protected @Override void setUp() throws Exception {
        factory = new ProviderImpl();
        kids = new AsynchChildren<String>(factory);
        node = new AbstractNode(kids);
        
        factory2 = new BatchProviderImpl();
        kids2 = new AsynchChildren<String>(factory2);
        node2 = new AbstractNode(kids2);
    }
    
    public void testChildrenCreate() {
        System.out.println("testChildrenCreate");
        ChildFactory<?> f = new ProviderImpl();
        Children kids = Children.create(f, true);
        assertTrue(kids instanceof AsynchChildren);
        
        ChildFactory<?> ff = new ProviderImpl();
        Children kids2 = Children.create(ff, false);
        assertFalse(kids2 instanceof AsynchChildren);
        assertTrue(kids2 instanceof SynchChildren);
        
        RuntimeException e = null;
        Children kids3 = null;
        try {
            kids3 = Children.create(ff, true);
        } catch (RuntimeException ex) {
            e = ex;
        }
        assertNull(kids3);
        assertNotNull("Exception should have been thrown creating two " +
                "Children objects over the same ChildFactory", e);
    }
    
    //A word of caution re adding tests:
    //Almost anything (getNodes(), justCreateNodes(), etc. can trigger a
    //fresh call to Children.addNotify().  Any test that expects a synchronous
    //change in the child nodes as a result of having triggered a call
    //to setKeys() is probably testing a race condition, not the behavior
    //of the children implementation
    
    public void testGetNodesWaits() throws Exception {
        System.out.println("testGetNodesWaits");
        factory.wait = false;
        kids.getNodes(false);
        synchronized (factory.lock) {
            factory.lock.wait(300);
        }
        Thread.yield();
        new NL(node);
        Node[] n = kids.getNodes(true);
        assertEquals(4, n.length);
    }

    public void testFindChildWaits() throws Exception {
        System.out.println("testFindChildWaits");
        factory.wait = false;
        Node n = kids.findChild("D");
        assertNotNull(n);
    }

    public void testGetNodesWaitsFirstTime() {
        System.out.println("testGetNodesWaits");
        factory.wait = false;
        Node[] n = kids.getNodes(true);
        assertEquals(4, n.length);
    }

    public void testInitialNodeIsWaitNode() throws Exception {
        System.out.println("testInitialNodeIsWaitNode");
        factory.wait = true;
        kids.addNotify();
        Node[] n = kids.getNodes(false);
        factory.wait = false;
        assertEquals(1, n.length);
        assertEquals(NbBundle.getMessage(AsynchChildren.class, "LBL_WAIT"),
                n[0].getDisplayName());
        factory.wait = false;
        synchronized (factory) {
            factory.wait(2000);
        }
        for (int i = 0; i < 5 && n.length != 4; i++) {
            n = kids.getNodes(true);
            Thread.yield();
        }
        assertEquals(4, n.length);
    }
    
    public void testBatch() throws Exception {
        System.out.println("testBatch");
        kids2.addNotify();
        Thread.yield();
        synchronized (factory2.lock) {
            factory2.lock.notifyAll();
        }
        new NL(node2);
        Node[] n = n = kids2.getNodes(true);
        assertEquals(4, n.length);
        assertEquals(2, factory2.callCount);
    }
    
    public void testSynchChildren() throws Exception {
        System.out.println("testSynchChildren");
        final SynchProviderImpl factory = new SynchProviderImpl();
        final Children ch = Children.create(factory, false);
        assertTrue(ch instanceof SynchChildren);
        factory.assertCreateKeysNotCalled();
        factory.assertCreateNodesForKeyNotCalled();
        final Node nd = new AbstractNode(ch);
        NodeAdapter adap = new NodeAdapter() {};
        nd.addNodeListener(adap);
        
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                ch.getNodes(true);
            }
        });
        ((SynchChildren) ch).active = true;
        synchronized (factory) {
            factory.wait(1000);
        }
        factory.assertCreateKeysCalled();
        factory.assertCreateNodesForKeyCalled();
        Node[] nodes = nd.getChildren().getNodes(true);
        assertEquals(SynchProviderImpl.CONTENTS1.size(), nodes.length);
        int ix = 0;
        for (String s : SynchProviderImpl.CONTENTS1) {
            assertEquals(s, nodes[ix].getName());
            ix++;
        }
        factory.switchChildren();
        nodes = nd.getChildren().getNodes(true);
        assertEquals(SynchProviderImpl.CONTENTS2.size(), nodes.length);
        ix = 0;
        for (String s : factory.CONTENTS2) {
            assertEquals(s, nodes[ix].getName());
            ix++;
        }
    }
    
    public void testCancel() throws Exception {
        System.out.println("testCancel");
        Thread.interrupted();
        factory.wait = true;
        kids.addNotify();
        Thread.yield();
        synchronized (factory.lock) {
            factory.lock.wait(500);
        }
        kids.removeNotify();
        factory.wait = false;
        synchronized (factory) {
            factory.wait(2000);
        }
        assertTrue(kids.cancelled);
        assertTrue(factory.cancelled);
    }

    public void testAddRemoveNotifySynch() throws Exception {
        DetachableImpl r = new DetachableImpl();
        Children ch = Children.create(r, false);
        new AbstractNode (ch);
        ch.addNotify();
        r.assertAdded();
        ch.removeNotify();
        r.assertRemoved();
        r = new DetachableImpl();
        ch = Children.create(r, false);
        Node[] n = ch.getNodes(true);
        assertEquals (2, n.length);
        assertEquals ("foo", n[0].getDisplayName());
        assertEquals ("bar", n[1].getDisplayName());
        ch.removeNotify();
        r.assertRemoved();
    }

    public void testAddRemoveNotifyAsynch() throws Exception {
        DetachableImpl r = new DetachableImpl();
        Children ch = Children.create(r, true);
        new AbstractNode (ch);
        ch.addNotify();
        synchronized(r) {
            r.wait(1000);
        }
        r.assertAdded();
        Node[] n = ch.getNodes(true);
        assertEquals (2, n.length);
        assertEquals ("foo", n[0].getDisplayName());
        assertEquals ("bar", n[1].getDisplayName());
        ch.removeNotify();
        synchronized(r) {
            r.wait(1000);
        }
        r.assertRemoved();
    }


    static final class ProviderImpl extends ChildFactory <String> {
        final Object lock = new Object();
        volatile boolean wait = false;
        
        public @Override Node[] createNodesForKey(String key) {
            AbstractNode nd = new AbstractNode(Children.LEAF);
            nd.setDisplayName(key);
            nd.setName(key);
            return new Node[] { nd };
        }
        
        boolean cancelled = false;
        public boolean createKeys(List <String> result) {
            try {
                while (wait) {
                    Thread.yield();
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
                if (Thread.interrupted()) {
                    cancelled = true;
                    return true;
                }
                result.add("A");
                result.add("B");
                result.add("C");
                result.add("D");
                if (Thread.interrupted()) {
                    cancelled = true;
                }
                return true;
            } finally {
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }
    
    static final class BatchProviderImpl extends ChildFactory <String> {
        boolean firstCycle = true;
        
        public @Override Node[] createNodesForKey(String key) {
            AbstractNode nd = new AbstractNode(Children.LEAF);
            nd.setDisplayName(key);
            return new Node[] { nd };
        }
        
        final Object lock = new Object();
        int callCount = 0;
        public boolean createKeys(List <String> result) {
            callCount++;
            synchronized (lock) {
                try {
                    lock.wait(500);
                } catch (InterruptedException ex) {
                    //re-interrupt
                    Thread.currentThread().interrupt();
                }
            }
            if (Thread.interrupted()) {
                return true;
            }
            boolean wasFirstCycle = firstCycle;
            if (wasFirstCycle) {
                result.add("A");
                result.add("B");
                firstCycle = false;
                return false;
            } else {
                result.add("C");
                result.add("D");
            }
            if (Thread.interrupted()) {
                return true;
            }
            synchronized (this) {
                notifyAll();
            }
            return true;
        }
    }

    public static void assertNodeAndEvent(final NodeEvent ev, final List<Node> snapshot) {
        Children.MUTEX.readAccess(new Runnable() {
            public void run() {
                int cnt = snapshot.size();
                assertEquals("Same number of nodes", ev.getNode().getChildren().getNodesCount(), cnt);
                for (int i = 0; i < cnt; i++) {
                    Node fromEv = snapshot.get(i);
                    if (fromEv instanceof EntrySupport.Lazy.DummyNode) {
                        continue;
                    }
                    Node fromCh = ev.getNode().getChildren().getNodeAt(i);
                    assertSame("The nodes are same at " + i, fromCh, fromEv);
                }
            }
        });
    }
    
    private static final class NL implements NodeListener {
        NL(Node n) {
            n.addNodeListener(this);
            try {
                waitFor();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throw new Error(e);
            }
        }
        
        NL() {
            
        }

        
        public void childrenAdded(NodeMemberEvent ev) {
            assertNodeAndEvent(ev, ev.getSnapshot());
            go();
        }
        
        public void childrenRemoved(NodeMemberEvent ev) {
            assertNodeAndEvent(ev, ev.getSnapshot());
            go();
        }
        
        public void childrenReordered(NodeReorderEvent ev) {
            assertNodeAndEvent(ev, ev.getSnapshot());
            go();
        }
        
        public void nodeDestroyed(NodeEvent ev) {
            assertNodeAndEvent(ev, Collections.<Node>emptyList());
        }
        
        public void propertyChange(PropertyChangeEvent arg0) {
        }
        
        private void go() {
            synchronized (this) {
                notifyAll();
            }
        }
        
        void waitFor() throws Exception {
            System.err.println("Enter waitfor");
            synchronized (this) {
                wait(1000);
            }
        }
    }
    
    private static final class SynchProviderImpl extends ChildFactory <String> {
        static List <String> CONTENTS1 = Arrays.<String>asList(new String[] {
            "One", "Two", "Three", "Four"
        });
        static List <String> CONTENTS2 = Arrays.<String>asList(new String[] {
            "Five", "Six", "Seven", "Eight", "Nine"
        });
        
        boolean createNodesForKeyCalled = false;
        public @Override Node[] createNodesForKey(String key) {
            createNodesForKeyCalled = true;
            Node result = new AbstractNode(Children.LEAF);
            result.setDisplayName(key);
            result.setName(key);
            return new Node[] { result };
        }
        
        boolean createKeysCalled = false;
        public boolean createKeys(List <String> toPopulate) {
            createKeysCalled = true;
            List <String> l = switched ? CONTENTS2 : CONTENTS1;
            toPopulate.addAll(l);
            return true;
        }
        
        void assertCreateNodesForKeyNotCalled() {
            assertFalse(createNodesForKeyCalled);
        }
        
        void assertCreateKeysNotCalled() {
            assertFalse(createKeysCalled);
        }
        
        boolean assertCreateNodesForKeyCalled() {
            boolean result = createNodesForKeyCalled;
            createNodesForKeyCalled = false;
            assertTrue(result);
            return result;
        }
        
        boolean assertCreateKeysCalled() {
            boolean result = createKeysCalled;
            createKeysCalled = false;
            assertTrue(result);
            return result;
        }
        
        volatile boolean switched = false;
        void switchChildren() {
            switched = !switched;
            refresh(true);
        }
    }

    private static final class DetachableImpl extends Detachable<String> {
        boolean removed;
        boolean added;

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            toPopulate.add("foo");
            toPopulate.add("bar");
            synchronized(this) {
                notifyAll();
            }
            return true;
        }

        @Override
        protected void removeNotify() {
            assertFalse (removed);
            synchronized(this) {
                notifyAll();
            }
            removed = true;
            added = false;
        }

        @Override
        protected Node createNodeForKey(String key) {
            AbstractNode nd = new AbstractNode(Children.LEAF);
            nd.setDisplayName(key);
            return nd;
        }

        @Override
        protected void addNotify() {
            assertFalse (added);
            added = true;
        }

        void assertAdded() {
            assertTrue (added);
        }

        void assertRemoved() {
            assertTrue (removed);
        }
    }
}

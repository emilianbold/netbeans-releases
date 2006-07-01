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

package org.openide.actions;

import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import java.util.Arrays;

/** Test behavior of MoveUpAction (also MoveDownAction and ReorderAction).
 * @author Jesse Glick
 */
public class MoveUpActionTest extends NbTestCase {

    static {
        // Get Lookup right to begin with.
        ActionsInfraHid.class.getName();
    }
    
    public MoveUpActionTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(MoveUpActionTest.class));
    }
    
    private Node n, n1, n2, n3;
    
    protected void setUp() throws Exception {
        n1 = new AbstractNode(Children.LEAF);
        n1.setName("n1");
        n2 = new AbstractNode(Children.LEAF);
        n2.setName("n2");
        n3 = new AbstractNode(Children.LEAF);
        n3.setName("n3");
        final Index.ArrayChildren c = new Index.ArrayChildren() {
            {
                add(new Node[] {n1, n2, n3});
            }
            public void reorder() {
                reorder(new int[] {1, 2, 0});
            }
        };
        n = new AbstractNode(c) {
            {
                getCookieSet().add(c);
            }
        };
        n.setName("n");
    }
    
    /**
     * in order to run in awt event queue
     * fix for #39789
     */
    protected boolean runInEQ()
    {
        return true;
    }
    
    public void testBasicUsage() throws Exception {
        SystemAction mua = SystemAction.get(MoveUpAction.class);
        SystemAction mda = SystemAction.get(MoveDownAction.class);
        SystemAction roa = SystemAction.get(ReorderAction.class);
        ActionsInfraHid.WaitPCL l = null;
        try {
            assertNull(ActionsInfraHid.UT.getCurrentNodes());
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            l = new ActionsInfraHid.WaitPCL(SystemAction.PROP_ENABLED);
            mua.addPropertyChangeListener(l);
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            ActionsInfraHid.UT.setCurrentNodes(new Node[] {n});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertTrue(roa.isEnabled());
            assertEquals(Arrays.asList(new Node[] {n1, n2, n3}), Arrays.asList(n.getChildren().getNodes()));
            roa.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n3, n1, n2}), Arrays.asList(n.getChildren().getNodes()));
            assertTrue(roa.isEnabled());
            ActionsInfraHid.UT.setCurrentNodes(new Node[] {n1, n2});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            ActionsInfraHid.UT.setCurrentNodes(new Node[] {n1});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue("MoveUp is enabled on a node in the middle of its parents", mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            mua.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n1, n3, n2}), Arrays.asList(n.getChildren().getNodes()));
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue("MoveUp is turned off after a node is moved to the very top", !mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            ActionsInfraHid.UT.setCurrentNodes(new Node[] {n2});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            ActionsInfraHid.UT.setCurrentNodes(new Node[] {n3});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            mda.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n1, n2, n3}), Arrays.asList(n.getChildren().getNodes()));
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
        } finally {
            if (l != null) {
                mua.removePropertyChangeListener(l);
                mda.removePropertyChangeListener(l);
                roa.removePropertyChangeListener(l);
            }
            ActionsInfraHid.UT.setCurrentNodes(new Node[0]);
            ActionsInfraHid.UT.setCurrentNodes(null);
        }
    }
    
}

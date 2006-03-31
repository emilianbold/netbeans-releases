/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.explorer;

import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import java.util.Collections;
import java.util.Arrays;
import java.awt.BorderLayout;
import org.openide.explorer.view.BeanTreeView;
import javax.swing.JLabel;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** Test finding help IDs on explorers.
 * @author Jesse Glick
 * @see "#14701"
 */
public class FindHelpTest extends NbTestCase {
    
    public FindHelpTest(String name) {
        super(name);
    }
    
    private static Node[] nodes;
    private static Node root;

    protected void setUp() throws Exception {
        Children kids = new Children.Array();
        nodes = new Node[] {
            new NoHelpNode(),
            new WithHelpNode("foo"),
            new WithHelpNode("bar"),
            new WithHelpNode("foo"),
        };
        kids.add(nodes);
        root = new AbstractNode(kids);
    }
    
    public void testFindHelpOnExplorer() throws Exception {
        ExplorerPanel p = new ExplorerPanel();
        ExplorerManager m = p.getExplorerManager();
        m.setRootContext(root);
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(m.getSelectedNodes()));
        HelpCtx base = new HelpCtx(ExplorerPanel.class);
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[0]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[2]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[3]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
        p = new WithHelpExplorer();
        m = p.getExplorerManager();
        m.setRootContext(root);
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(m.getSelectedNodes()));
        base = new HelpCtx("base");
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[0]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[2]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[3]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
    }
    
    public void testFindHelpHierarchically() throws Exception {
        ExplorerPanel p = new ExplorerPanel();
        p.setLayout(new BorderLayout());
        BeanTreeView b = new BeanTreeView();
        p.add(b, BorderLayout.CENTER);
        JLabel l1 = new JLabel("test1");
        HelpCtx.setHelpIDString(l1, "test");
        p.add(l1, BorderLayout.NORTH);
        JLabel l2 = new JLabel("test2");
        assertEquals(HelpCtx.DEFAULT_HELP, HelpCtx.findHelp(l2));
        p.add(l2, BorderLayout.SOUTH);
        ExplorerManager m = p.getExplorerManager();
        m.setRootContext(root);
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(m.getSelectedNodes()));
        HelpCtx base = new HelpCtx(ExplorerPanel.class);
        assertEquals(base, HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[0]});
        assertEquals(base, HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[1]});
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[1], nodes[2]});
        assertEquals(base, HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[1], nodes[3]});
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(b));
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(p));
        assertEquals(new HelpCtx("test"), HelpCtx.findHelp(l1));
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(l2));
    }
    
    private static final class NoHelpNode extends AbstractNode {
        public NoHelpNode() {
            super(Children.LEAF);
        }
    }
    
    private static final class WithHelpNode extends AbstractNode {
        private final String id;
        public WithHelpNode(String id) {
            super(Children.LEAF);
            this.id = id;
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(id);
        }
    }
    
    private static final class WithHelpExplorer extends ExplorerPanel {
        public HelpCtx getHelpCtx() {
            return getHelpCtx(getExplorerManager().getSelectedNodes(), new HelpCtx("base"));
        }
    }
    
}

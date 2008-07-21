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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.explorer.view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Marian Petras, Andrei Badea
 */
public final class TreeViewTest extends NbTestCase {
    
    private TestTreeView treeView;
    private JFrame testWindow;
    private volatile boolean isScrolledDown;
    private final Object semaphore = new Object();
    
    public TreeViewTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests whether <code>JTree</code>'s property <code>scrollsOnExpand</code>
     * is taken into account in
     * <code>TreeView.TreePropertyListener.treeExpanded(...)</code>.
     */
    public void testAutoscrollOnOff() throws InterruptedException {
        assert !EventQueue.isDispatchThread();
        
        testWindow = new ExplorerWindow();
        testWindow.getContentPane().add(treeView = new TestTreeView());
        
        class WindowDisplayer implements Runnable {
            public void run() {
                testWindow.pack();
                testWindow.setVisible(true);
            }
        }
        
        class Detector implements Runnable {
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(this);
                    return;
                }
                
                isScrolledDown = !treeView.isUp();
                
                synchronized (semaphore) {
                    semaphore.notify();
                }
            }
        }

        class Tester implements Runnable {
            private final boolean autoscroll;
            private final int part;
            Tester(boolean autoscroll, int part) {
                this.autoscroll = autoscroll;
                this.part = part;
            }
            public void run() {
                assert (part == 1) || (part == 2);
                if (part == 1) {
                    treeView.collapse();
                    treeView.scrollUp();
                    assert treeView.isUp();
                } else {
                    treeView.setAutoscroll(autoscroll);
                    treeView.expand(); //<-- posts a request to the RequestProcessor
                    RequestProcessor.getDefault().post(new Detector(), 1000 /*ms*/);
                }
            }
        }

        try {
            EventQueue.invokeAndWait(new WindowDisplayer());
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        
        //Wait for the AWT thread to actually display the dialog:
        Thread.sleep(5000);
        
        EventQueue.invokeLater(new Tester(true, 1));
        Thread.sleep(2000);      //wait for update of the screen
        EventQueue.invokeLater(new Tester(true, 2));
        synchronized (semaphore) {
            semaphore.wait();
        }
        assertTrue("Check the view has scrolled", isScrolledDown);

        EventQueue.invokeLater(new Tester(false, 1));
        Thread.sleep(2000);      //wait for update of the screen
        EventQueue.invokeLater(new Tester(false, 2));
        synchronized (semaphore) {
            semaphore.wait();
        }
        assertTrue("Check the view has not scrolled", !isScrolledDown);

        EventQueue.invokeLater(new Tester(true, 1));    //just collapse the tree
        Thread.sleep(2000);
    }
    
    
    private static final class TestTreeView extends BeanTreeView {
        
        private final Node rootNode;
        final JScrollBar vertScrollBar;
        private transient ExplorerManager explManager;
        
        TestTreeView() {
            super();
            tree.setAutoscrolls(true);

            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            vertScrollBar = getVerticalScrollBar();
            
            rootNode = new AbstractNode(new TreeChildren());
            rootNode.setDisplayName("Root node");
            
            tree.setRowHeight(20);
            
            Dimension prefSize = new Dimension(200, 6 * tree.getRowHeight() + 8);
            prefSize.width = (int) (prefSize.width * 1.25f)
                             + vertScrollBar.getWidth();
            setPreferredSize(prefSize);
        }
        
        @Override
        public void addNotify() {
            super.addNotify();
            explManager = ExplorerManager.find(this);
            explManager.setRootContext(rootNode);
            collapse();
        }
        
        void setAutoscroll(boolean autoscroll) {
            tree.setScrollsOnExpand(autoscroll);
        }
        
        void scrollUp() {
            vertScrollBar.setValue(vertScrollBar.getMinimum());
        }
        
        boolean isUp() {
            return vertScrollBar.getValue()
                   == vertScrollBar.getMinimum();
        }
        
        void expand() {
            tree.expandRow(4);
        }
        
        void collapse() {
            tree.collapseRow(4);
        }
        
        final static class TreeChildren extends Children.Array {
            
            private static final char[] letters
                    = new char[] {'A', 'B', 'C', 'D', 'E'};
            
            TreeChildren() {
                this(-1);
            }
            
            TreeChildren(final int first) {
                super();
                
                Node[] childNodes = new Node[5];
                int i;
                if (first == -1) {
                    for (i = 0; i < childNodes.length; i++) {
                        AbstractNode childNode = new AbstractNode(new TreeChildren(i));
                        childNode.setDisplayName("Child node " + i);
                        childNodes[i] = childNode;
                    }
                } else {
                    for (i = 0; i < childNodes.length; i++) {
                        AbstractNode childNode = new AbstractNode(Children.LEAF);
                        StringBuffer buf = new StringBuffer(3);
                        childNode.setDisplayName(buf.append(first)
                                                    .append('.')
                                                    .append(letters[i])
                                                    .toString());
                        childNodes[i] = childNode;
                    }
                }
                add(childNodes);
            }
            
        }
        
        
    }
    
    
    private static final class ExplorerWindow extends JFrame
                               implements ExplorerManager.Provider {
        
        private final ExplorerManager explManager = new ExplorerManager();
        
        ExplorerWindow() {
            super("TreeView test");                                     //NOI18N
        }
        
        public ExplorerManager getExplorerManager() {
            return explManager;
        }
        
    }
    
    /**
     * Used as the preferred actions by the nodes below
     */
    private static class MyAction extends NodeAction {

        public boolean enable(Node[] nodes) {
            return true;
        }

        public void performAction(Node[] nodes) {
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return "My Action";
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new MyDelegateAction(actionContext);
        }
    }
    
    /**
     * Returned by MyAction.createContextAwareInstance().
     */
    private static class MyDelegateAction extends AbstractAction {
        Lookup contextLookup;
        
        public MyDelegateAction(Lookup contextLookup) {
            this.contextLookup = contextLookup;
        }
        
        public void actionPerformed(ActionEvent e) {
        }
    }

    private static class NodeWhichHasItselfInLookup extends AbstractNode {
        public NodeWhichHasItselfInLookup() {
            super(Children.LEAF);
        }

        @Override
        public Action getPreferredAction() {
            return SystemAction.get(MyAction.class);
        }
    }

    private static class NodeWhichDoesntHaveItselfInLookup extends AbstractNode {
        public NodeWhichDoesntHaveItselfInLookup() {
            super(Children.LEAF, Lookup.EMPTY);
        }

        @Override
        public Action getPreferredAction() {
            return SystemAction.get(MyAction.class);
        }
    }
    
    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node contains itself in its lookup.
     */
    public void testTakeActionNodeInLookup() {
        doTestTakeAction(new NodeWhichHasItselfInLookup());        
    }

    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node doesn't contain itself in its lookup.
     */
    public void testTakeActionNodeNotInLookup() {
        doTestTakeAction(new NodeWhichDoesntHaveItselfInLookup());
    }
    
    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node contains itself in its lookup and is filtered by a FilterNode.
     */
    public void testTakeActionNodeInLookupAndFiltered() {
        doTestTakeAction(new FilterNode(new NodeWhichHasItselfInLookup()));        
    }

    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node doesn't contain itself in its lookup
     * and is filtered by a FilterNode.
     */
    public void testTakeActionNodeNotInLookupAndFiltered() {
        doTestTakeAction(new FilterNode(new NodeWhichDoesntHaveItselfInLookup()));
    }
    
    private void doTestTakeAction(Node node) {
        // if the preferred action instanceof ContextAwareAction
        // calls its createContextAwareInstance() method
        Action a = TreeView.takeAction(node.getPreferredAction(), node);
        int count = ((MyDelegateAction)a).contextLookup.lookup(new Lookup.Template(Node.class)).allInstances().size();
        assertEquals("The context lookup created by TreeView.takeAction() should contain the node only once.", 1, count);
    }
    
}

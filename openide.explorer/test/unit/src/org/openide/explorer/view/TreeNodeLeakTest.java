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

package org.openide.explorer.view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;

/**
 * A test covering JDK issue 6472844 and its NetBeans workaround
 * @author  Petr Nejedly
 */
public final class TreeNodeLeakTest extends NbTestCase {
    
    private TreeView treeView;
    private ExplorerWindow testWindow;
    private Node toSelect[] = new Node[6];
    
    public TreeNodeLeakTest(String testName) {
        super(testName);
    }
    

    private static Node createNode(String name, Node ... sub) {
        Children ch = Children.LEAF;
        if (sub != null) {
            ch = new Children.Array();
            ch.add(sub);
        }
        AbstractNode n = new AbstractNode(ch);
        n.setName(name);
        return n;
    }
    
    /**
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=84970
     */
    public void testNodesLeak() throws Exception {
        assert !EventQueue.isDispatchThread();
        final Node root = createNode("Root",
            toSelect[0] = createNode("ch1",
                toSelect[1] = createNode("A", (Node[])null),
                toSelect[2] = createNode("B", (Node[])null)),
            toSelect[3] = createNode("ch2",
                toSelect[4] = createNode("A", (Node[])null),
                toSelect[5] = createNode("B", (Node[])null)),
            createNode("ch3",
                createNode("A", (Node[])null),
                createNode("B", (Node[])null))
        );
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            treeView = new BeanTreeView();
            testWindow = new ExplorerWindow();
            testWindow.getContentPane().add(treeView);
            testWindow.pack();
            testWindow.setVisible(true);
            testWindow.getExplorerManager().setRootContext(root);
            try {
                testWindow.getExplorerManager().setSelectedNodes(toSelect);
            } catch (PropertyVetoException pve) {
                fail(pve.getMessage());
            }
            root.getChildren().remove( new Node[] {toSelect[0], toSelect[3]});
        }});
        EventQueue.invokeAndWait(new Runnable() { public void run() {}});

        WeakReference wr = new WeakReference(toSelect[0]);
        toSelect = null;
        assertGC("Node freed", wr);
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
}

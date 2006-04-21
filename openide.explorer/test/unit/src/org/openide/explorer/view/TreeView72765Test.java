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

/** Simulate a deadlock.
 *
 * @author Jaroslav Tulach
 */
public final class TreeView72765Test extends NbTestCase {
    private TreeView ttv;

    public TreeView72765Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        ttv = new BeanTreeView();
    }

    protected boolean runInEQ() {
        return true;
    }



    public void testRedrawWhenOtherThreadHasChildrenLock() throws InterruptedException {

        class Block implements Runnable {
            public synchronized void run() {
                if (!Children.MUTEX.isWriteAccess()) {
                    Children.MUTEX.writeAccess(this);
                    return;
                }

                notifyAll();
                try {
                    wait(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                synchronized (ttv.getTreeLock()) {
                }
            }
        }


        Block block = new Block();
        RequestProcessor.Task t;
        synchronized (block) {
            t = RequestProcessor.getDefault().post(block);
            block.wait();
        }

        ttv.addNotify();

        assertNotNull("Initialize peer", ttv.getPeer());

        ttv.invalidate();
        ttv.validate();

    }
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.test.ui.actions;

import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.compapp.test.ui.TestcaseNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

/**
 * Action to delete all results for an individual test case.
 *
 * @author Jun Qian
 */
public class TestCaseResultsDeleteAction extends CookieAction {
    
    protected Class[] cookieClasses() {
        return new Class[] {TestcaseCookie.class};
    }
    
    protected int mode() {
        return CookieAction.MODE_ALL;
    }
    
    // Similar to TestCaseDeleteAction except that we don't delete the test case
    // in the end.
    protected void performAction(Node[] activatedNodes) {
        
        // Remember the selection before deletion. There gotta be some easier
        // way to do this.
        JTree tree = null;
//        TreeSelectionModel treeSelectionModel = null;
//        TreePath[] selectionPaths = null;
        
        TopComponent topComponent = TopComponent.getRegistry().getActivated();
        if (topComponent.getComponent(0) instanceof TreeView) {
            TreeView treeView = (TreeView) topComponent.getComponent(0);
            
            if (treeView.getComponent(0) instanceof JViewport) {
                JViewport viewport = (JViewport)treeView.getComponent(0);
                if (viewport.getComponent(0) instanceof JTree) {
                    tree = (JTree) viewport.getComponent(0);
//                    treeSelectionModel = tree.getSelectionModel();
//                    selectionPaths = treeSelectionModel.getSelectionPaths();
                }
            }
        }
        
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(TestCaseResultsDeleteAction.class, "MSG_DeleteTestCaseResults"), // NOI18N
                NbBundle.getMessage(TestCaseResultsDeleteAction.class, "TTL_DeleteTestCaseResults"), // NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION);
        
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {            
            for (Node node : activatedNodes) {
                TestcaseCookie testCaseCookie =
                        ((TestcaseCookie) node.getCookie(TestcaseCookie.class));
                TestcaseNode testCaseNode = testCaseCookie.getTestcaseNode();
                testCaseNode.deleteResults();
            }
        }
        
        // Select the test case node after all results are deleted. (IZ 85064)
        if (tree != null /*&& treeSelectionModel != null && selectionPaths != null*/) {
            tree.requestFocus();
//            treeSelectionModel.clearSelection();
//            treeSelectionModel.setSelectionPaths(selectionPaths);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return NbBundle.getMessage(TestCaseResultsDeleteAction.class, "LBL_TestcaseDeleteResultsAction_Name");  // NOI18N
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        
        // If you will provide context help then use:
        // return new HelpCtx(AddTestcaseAction.class);
    }
}

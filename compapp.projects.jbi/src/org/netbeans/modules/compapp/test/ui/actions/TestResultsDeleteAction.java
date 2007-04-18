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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.compapp.test.ui.TestNode;
import org.netbeans.modules.compapp.test.ui.TestcaseNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

/**
 * Action to delete all results for all test cases.
 *
 * @author Jun Qian
 */
public class TestResultsDeleteAction extends CookieAction {
    
    protected Class[] cookieClasses() {
        return new Class[] {TestCookie.class};
    }
    
    protected int mode() {
        return CookieAction.MODE_ALL;
    }
    
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
                NbBundle.getMessage(TestResultsDeleteAction.class, "MSG_DeleteTestResults"), // NOI18N
                NbBundle.getMessage(TestResultsDeleteAction.class, "TTL_DeleteTestResults"), // NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION);
        
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {            
            for (Node node : activatedNodes) {
                TestCookie testCookie = ((TestCookie) node.getCookie(TestCookie.class));
                TestNode testNode = testCookie.getTestNode();
                
                Node[] children = testNode.getChildren().getNodes();
                
                for (Node child : children) {
                    TestcaseCookie testCaseCookie =
                            (TestcaseCookie) child.getCookie(TestcaseCookie.class);
                    if (testCaseCookie != null) {
                        TestcaseNode testCaseNode = testCaseCookie.getTestcaseNode();
                        testCaseNode.deleteResults();
                    }
                }
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
        return NbBundle.getMessage(TestResultsDeleteAction.class, "LBL_TestDeleteResultsAction_Name");  // NOI18N
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

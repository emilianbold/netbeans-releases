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

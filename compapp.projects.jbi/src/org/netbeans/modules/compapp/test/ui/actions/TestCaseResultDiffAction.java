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

import org.netbeans.modules.compapp.test.ui.TestcaseNode;
import org.netbeans.modules.compapp.test.ui.TestCaseResultNode;
import org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseConstants;
import java.util.List;

import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * DOCUMENT ME!
 *
 * @author Jun Qian
 */
public class TestCaseResultDiffAction extends NodeAction implements NewTestcaseConstants {
    private static final java.util.logging.Logger mLog =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.actions.TestCaseResultDiffAction"); // NOI18N
    
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        
        TestCaseResultNode testCaseResultNode = getTestCaseResultNode(activatedNodes[0]);
        if (testCaseResultNode == null) {
            return false;
        }
        
        return !testCaseResultNode.isSuccessful();
    }
    
    private TestCaseResultNode getTestCaseResultNode(Node node) {
        
        TestCaseResultNode testCaseResultNode = null;
        
        TestCaseResultCookie testCaseResultCookie =
                (TestCaseResultCookie) node.getCookie(TestCaseResultCookie.class);
        
        if (testCaseResultCookie != null) {
            testCaseResultNode = testCaseResultCookie.getTestCaseResultNode();
        }
        
        return testCaseResultNode;
    }    
    
    private TestcaseNode getTestCaseNode(Node node) {
        
        TestcaseNode testCaseNode = null;
        
        TestCaseResultNode testCaseResultNode = getTestCaseResultNode(node);
                
        if (testCaseResultNode != null) {
            TestcaseCookie testCaseCookie =
                    (TestcaseCookie) testCaseResultNode.getParentNode()/*.getParentNode()*/.getCookie(TestcaseCookie.class);
            if (testCaseCookie != null) {
                testCaseNode = testCaseCookie.getTestcaseNode();
            }
        }
        
        return testCaseNode;
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
     * @param activatedNodes DOCUMENT ME!
     */
    protected void performAction(Node[] activatedNodes) {
        TestCaseResultNode testCaseResultNode = getTestCaseResultNode(activatedNodes[0]);
        TestcaseNode testCaseNode = getTestCaseNode(activatedNodes[0]);
        if (testCaseResultNode != null && testCaseNode != null) {
            String nodeName = testCaseResultNode.getName();
//            System.out.println("name=" + testCaseResultNode.getName());
//            System.out.println("displayname=" + testCaseResultNode.getDisplayName());
            testCaseNode.showDiffTopComponentVisible(nodeName + ".xml"); // TMP  // NOI18N
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return NbBundle.getMessage(AddTestcaseAction.class, "LBL_TestcaseDiffAction_Name");  // NOI18N
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

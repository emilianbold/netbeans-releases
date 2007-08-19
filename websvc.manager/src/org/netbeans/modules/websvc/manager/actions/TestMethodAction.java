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

package org.netbeans.modules.websvc.manager.actions;

import org.netbeans.modules.websvc.manager.nodes.WebServiceMethodNode;
import org.netbeans.modules.websvc.manager.ui.TestWebServiceMethodDlg;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author qn145415
 */
public class TestMethodAction extends NodeAction {

    public TestMethodAction() {
        super();
    }

    
    protected boolean enable(Node[] activatedNodes) {
        if( activatedNodes != null && activatedNodes.length > 0 ) {
            WebServiceMethodNode node = null;
            if(activatedNodes[0] instanceof FilterNode){
                node = activatedNodes[0].getCookie(WebServiceMethodNode.class);
            }else if (activatedNodes[0] instanceof WebServiceMethodNode) {
                node = (WebServiceMethodNode)activatedNodes[0];
            }else {
                return false;
            }
            
            if (node != null) {
                return node.getWebServiceData().isJaxWsEnabled();
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/websvc/manager/resources/MyActionIcon.gif";
    }
    
    public String getName() {
        return NbBundle.getMessage(TestMethodAction.class, "TEST_METHOD");
    }
    
    protected void performAction(org.openide.nodes.Node[] nodes) {
        WebServiceMethodNode methodNode = null;
        
        if (nodes[0] instanceof FilterNode) {
            methodNode = nodes[0].getCookie(WebServiceMethodNode.class);
        }else if (nodes[0] instanceof WebServiceMethodNode) {
            methodNode = (WebServiceMethodNode)nodes[0];
        }else {
            return;
        }
        
        TestWebServiceMethodDlg testDialog = new TestWebServiceMethodDlg(
                methodNode.getWebServiceData(), 
                methodNode.getJavaMethod(),
                methodNode.getPort());
        
        testDialog.displayDialog();
        return;
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}

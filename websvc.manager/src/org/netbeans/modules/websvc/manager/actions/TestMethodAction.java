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

import com.sun.tools.ws.processor.model.java.JavaMethod;
import org.netbeans.modules.websvc.manager.ui.TestWebServiceMethodDlg;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
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
            WebServiceData wsdata = activatedNodes[0].getLookup().lookup(WebServiceData.class);
            JavaMethod javaMethod = activatedNodes[0].getLookup().lookup(JavaMethod.class);
            WsdlPort port = activatedNodes[0].getLookup().lookup(WsdlPort.class);            
            if (wsdata != null && javaMethod != null && port != null) {
                return wsdata.isJaxWsEnabled();
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
        return "org/netbeans/modules/visualweb/websvcmgr/resources/MyActionIcon.gif";
    }
    
    public String getName() {
        return NbBundle.getMessage(TestMethodAction.class, "TEST_METHOD");
    }
    
    protected void performAction(org.openide.nodes.Node[] nodes) {
        if( nodes != null && nodes.length > 0 ) {
            WebServiceData wsdata = nodes[0].getLookup().lookup(WebServiceData.class);
            JavaMethod javaMethod = nodes[0].getLookup().lookup(JavaMethod.class);
            WsdlPort port = nodes[0].getLookup().lookup(WsdlPort.class);
            if (wsdata != null && javaMethod != null && port != null) {
        TestWebServiceMethodDlg testDialog = new TestWebServiceMethodDlg(
                    wsdata, 
                    javaMethod,
                    port);
        
        testDialog.displayDialog();
            }
        }        
        return;
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}

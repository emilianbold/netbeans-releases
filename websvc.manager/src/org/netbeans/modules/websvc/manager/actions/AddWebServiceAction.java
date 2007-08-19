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

import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.nodes.WebServiceGroupNode;
import org.netbeans.modules.websvc.manager.nodes.WebServicesRootNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;

// import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.websvc.manager.ui.AddWebServiceDlg;

public class AddWebServiceAction extends NodeAction {
    
    
    WebServiceListModel wsListModel = WebServiceListModel.getInstance();
    
    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx(AddWebServiceAction.class);
    }
    
    public String getName() {
        return NbBundle.getMessage(AddWebServiceAction.class, "ADD_WEB_SERVICE_Action");
    }
    
    protected void performAction(Node[] nodes) {
        Node invokingNode = nodes[0];
        String groupId = null;
        
        if (invokingNode instanceof FilterNode) {
            Node root = invokingNode.getCookie(WebServicesRootNode.class);
            Node group = invokingNode.getCookie(WebServiceGroupNode.class);
            if(root != null){
                WebServicesRootNode rootNode = (WebServicesRootNode)root;
                groupId = rootNode.getWebServiceGroup().getId();
            }else if(group != null){
                WebServiceGroupNode groupNode = (WebServiceGroupNode)group;
                groupId = groupNode.getWebServiceGroup().getId();
            }
        }else {
            if(invokingNode instanceof WebServicesRootNode){
                WebServicesRootNode rootNode = (WebServicesRootNode)invokingNode;
                groupId = rootNode.getWebServiceGroup().getId();
            }else if(invokingNode instanceof WebServiceGroupNode){
                WebServiceGroupNode groupNode = (WebServiceGroupNode)invokingNode;
                groupId = groupNode.getWebServiceGroup().getId();
            }
        }
        
        if (groupId != null) {
            AddWebServiceDlg dlg = new AddWebServiceDlg(groupId);
            dlg.displayDialog();
        }else {
            AddWebServiceDlg dlg = new AddWebServiceDlg();
            dlg.displayDialog();
        }
                
        return;
        
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/websvc/manager/resources/webservice.png"; // NOI18N
    }
    
}

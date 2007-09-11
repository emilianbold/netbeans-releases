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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.actions;

import java.io.IOException;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.nodes.WebServiceNode;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that refreshes a web service from its original wsdl location.
 * 
 * @author quynguyen
 */
public class RefreshWebServiceAction extends NodeAction {
    protected boolean enable(Node[] nodes) {
        if(nodes != null &&
        nodes.length != 0) {
            for (int i = 0; i < nodes.length; i++) {
                WebServiceData data = nodes[i].getLookup().lookup(WebServiceData.class);
                if (data != null && !WebServiceManager.getInstance().isCompiling(data)) {
                    return true;
                }
            }
            
            return false;
        } else {
            return false;
        }
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/visualweb/websvcmgr/resources/MyActionIcon.gif"; // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(RefreshWebServiceAction.class, "REFRESH");
    }
    
    protected void performAction(Node[] nodes) {
        
        
        if(null != nodes && nodes.length > 0) {
            String msg = NbBundle.getMessage(RefreshWebServiceAction.class, "WS_REFRESH");
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            Object response = DialogDisplayer.getDefault().notify(d);
            if(null != response && response.equals(NotifyDescriptor.YES_OPTION)) {
                for(int ii = 0; ii < nodes.length; ii++) {
                    Node node = null;
                    if(nodes[ii] instanceof FilterNode){
                        node = (Node)(nodes[ii]).getCookie(WebServiceNode.class);
                    }else{
                        node = nodes[ii];
                    }

                    if(node instanceof WebServiceNode) {
                        final WebServiceNode currentNode = (WebServiceNode)node;
                        if(null == currentNode) continue;

                        Runnable refreshTask = new Runnable() {
                            public void run() {
                                try {
                                    if (!WebServiceManager.getInstance().isCompiling(currentNode.getWebServiceData())) {
                                        WebServiceManager.getInstance().refreshWebService(currentNode.getWebServiceData());
                                    }
                                } catch (IOException ioe) {
                                    ErrorManager.getDefault().notify(ioe);
                                }
                            }
                        };
                        
                        WebServiceManager.getInstance().getRequestProcessor().post(refreshTask);
                    }
                }
            }
        }
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
}

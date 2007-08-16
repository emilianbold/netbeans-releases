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

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.*;
import org.openide.ErrorManager;
import java.io.IOException;

import org.netbeans.modules.websvc.manager.nodes.*;

/**
 * This action will delete a web service group from the server navigator
 * @author  David Botterill
 */

public class DeleteWebServiceGroupAction extends NodeAction {
    
    protected boolean enable(Node[] nodes) {
        if(nodes != null &&
        nodes.length != 0) {
            Node node = (nodes[0] instanceof FilterNode) ? 
                (Node)(nodes[0]).getCookie(WebServiceGroupNode.class) : nodes[0];
            
            return node instanceof WebServiceGroupNode;
        } else {
            return false;
        }
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new AddToFormAction.class);
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/visualweb/websvcmgr/resources/MyActionIcon.gif"; // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(AddToFormAction.class, "DELETE");
    }
    
    protected void performAction(Node[] nodes) {
        WebServiceGroupNode currentNode = null;
        
        if(null != nodes && nodes.length > 0) {
            Node node = null;
            if(nodes[0] instanceof FilterNode){
                node = (Node)(nodes[0]).getCookie(WebServiceGroupNode.class);
            }else{
                node = nodes[0];
            }
            if(node instanceof WebServiceGroupNode){
                currentNode = (WebServiceGroupNode)node;
                String groupName = null;
                if(null != currentNode) {
                    groupName = currentNode.getWebServiceGroup().getName();
                }
                String msg = NbBundle.getMessage(this.getClass(), "WS_DELETE_GROUP") + " " + groupName;
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                Object response = DialogDisplayer.getDefault().notify(d);
                
                
                if(null != response && response.equals(NotifyDescriptor.YES_OPTION)) {
                    
                    try {
                        currentNode.destroy();
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }
                }
            }
        } else {
            return;
        }
        
        
        
    }
    
    protected boolean asynchronous() {
        return false;
    }
}

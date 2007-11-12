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
import java.util.Set;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;

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
        return "org/netbeans/modules/websvc/manager/resources/MyActionIcon.gif"; // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(DeleteWebServiceAction.class, "DELETE");
    }
    
    protected void performAction(Node[] nodes) {
        if(null != nodes && nodes.length > 0) {
            Node node = null;
            if(nodes[0] instanceof FilterNode){
                node = (Node)(nodes[0]).getCookie(WebServiceGroupNode.class);
            }else{
                node = nodes[0];
            }
            if(node instanceof WebServiceGroupNode){
                final WebServiceGroupNode currentNode = (WebServiceGroupNode)node;
                String groupName = null;
                if(null != currentNode) {
                    groupName = currentNode.getWebServiceGroup().getName();
                }
                String msg = NbBundle.getMessage(this.getClass(), "WS_DELETE_GROUP") + " " + groupName;
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                Object response = DialogDisplayer.getDefault().notify(d);
                
                if(null != response && response.equals(NotifyDescriptor.YES_OPTION)) {
                    Runnable deleteTask = new Runnable() {
                        public void run() {
                            try {
                                WebServiceGroup group = currentNode.getWebServiceGroup();
                                Set<String> ids = group.getWebServiceIds();
                                for (String id : ids) {
                                    WebServiceData wsData = WebServiceListModel.getInstance().getWebService(id);
                                    if (wsData != null) {
                                        if (wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILING)) {
                                            return;
                                        }
                                    }
                                }
                                
                                currentNode.destroy();
                            } catch (IOException ioe) {
                                ErrorManager.getDefault().notify(ioe);
                            }
                        }
                    };
                    WebServiceManager.getInstance().getRequestProcessor().post(deleteTask);
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

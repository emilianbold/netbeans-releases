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

package org.netbeans.modules.websvc.saas.ui.actions;

import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;
import org.netbeans.modules.websvc.saas.ui.wizards.AddWebServiceDlg;

public class AddServiceAction extends NodeAction {
    
    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx(AddServiceAction.class);
    }
    
    public String getName() {
        return NbBundle.getMessage(AddServiceAction.class, "ADD_WEB_SERVICE_Action");
    }
    
    protected void performAction(Node[] nodes) {
        Node invokingNode = nodes[0];
        String groupId = null;
        
        //TODO:nam integrate
        /*if (invokingNode instanceof FilterNode) {
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
        }*/
                
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

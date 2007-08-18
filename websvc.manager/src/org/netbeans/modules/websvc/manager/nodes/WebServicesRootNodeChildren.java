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

package org.netbeans.modules.websvc.manager.nodes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.manager.api.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceGroupListener;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.model.WebServiceListModelListener;

import org.netbeans.modules.websvc.manager.model.WebServiceGroupEvent;
import org.netbeans.modules.websvc.manager.model.WebServiceListModelEvent;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/** List of children of a containing node.
 * Remember to document what your permitted keys are!
 *
 * @author octav, Winston Prakash
 */
public class WebServicesRootNodeChildren extends Children.Keys<WebServiceGroup> implements WebServiceGroupListener, WebServiceListModelListener{
    private WebServiceListModel websvcListModel = WebServiceListModel.getInstance();

    public WebServicesRootNodeChildren() {
        websvcListModel.addWebServiceListModelListener(this);
        websvcListModel.addDefaultGroupListener(this);
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }
    
    private void updateKeys() {
        setKeys(websvcListModel.getWebServiceGroupSet());
    }
    
    @Override
    protected void removeNotify() {
        java.util.List<WebServiceGroup> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }
    
    protected Node[] createNodes(WebServiceGroup wsGroup) {            
        // Create the web service nodes at the top level instead of the
        // group folder if it is the default group
        if(wsGroup.getId().equals(WebServiceListModel.DEFAULT_GROUP)){
            Set<Node> nodes = new HashSet<Node>();
            for (String webServiceId : wsGroup.getWebServiceIds()) {            
                WebServiceData wsData = websvcListModel.getWebService(webServiceId);
                
                if (wsData != null && wsData.getWsdlService() == null) {
                    final WebServiceData data = wsData;
                    Runnable modelWsdl = new Runnable() {
                        public void run() {
                            try {
                                WebServiceManager.getInstance().addWebService(data);
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    };
                    WebServiceManager.getInstance().getRequestProcessor().post(modelWsdl);
                    AbstractNode waitNode = new AbstractNode(Children.LEAF);
                    waitNode.setName(NbBundle.getMessage(WebServiceGroupNodeChildren.class, "NODE_LOAD_MSG"));
                    waitNode.setIconBaseWithExtension("org/netbeans/modules/websvc/manager/resources/wait.gif"); // NOI18N
                    
                    nodes.add(waitNode);
                }else {
                    nodes.add(new WebServiceNode(wsData));
                }
            }
            return nodes.toArray(new Node[nodes.size()]);
        }else {
            return new Node[] { new WebServiceGroupNode(wsGroup) };
        }
    }
    
    public void webServiceGroupAdded(WebServiceListModelEvent modelEvent) {
        updateKeys();
    }
    
    public void webServiceGroupRemoved( WebServiceListModelEvent modelEvent) {
        updateKeys();
    }
    
    public void webServiceAdded( WebServiceGroupEvent groupEvent) {
        if (groupEvent.getGroupId().equals(WebServiceListModel.DEFAULT_GROUP)) {
            refreshKey(WebServiceListModel.getInstance().getWebServiceGroup(groupEvent.getGroupId()));
        }
    }
    
    public void webServiceRemoved(WebServiceGroupEvent groupEvent) {
        if (groupEvent.getGroupId().equals(WebServiceListModel.DEFAULT_GROUP)) {
            refreshKey(WebServiceListModel.getInstance().getWebServiceGroup(groupEvent.getGroupId()));
        }
    }
}

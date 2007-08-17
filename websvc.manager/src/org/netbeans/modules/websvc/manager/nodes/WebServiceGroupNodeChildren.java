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
import org.netbeans.modules.websvc.manager.api.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceGroupListener;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.model.WebServiceGroupEvent;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/** List of children of a webservices group node.
 *
 * @author Winston Prakash
 */
public class WebServiceGroupNodeChildren extends Children.Keys<String> implements WebServiceGroupListener {
    
    WebServiceGroup webserviceGroup;
    WebServiceListModel websvcListModel = WebServiceListModel.getInstance();
    
    public WebServiceGroupNodeChildren(WebServiceGroup websvcGroup) {
        webserviceGroup = websvcGroup;
        webserviceGroup.addWebServiceGroupListener(this);
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }
    
    private void updateKeys() {
        setKeys(webserviceGroup.getWebServiceIds());
    }
    
    @Override
    protected void removeNotify() {
        java.util.List<String> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }
    
    protected Node[] createNodes(String id) {
        WebServiceData wsData = websvcListModel.getWebService(id);
        if (wsData.getWsdlService() == null) {
            final WebServiceData data = wsData;
            Runnable modeller = new Runnable() {
                public void run() {
                    try {
                        WebServiceManager.getInstance().addWebService(data);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            };
            
            WebServiceManager.getInstance().getRequestProcessor().post(modeller);
            
            AbstractNode waitNode = new AbstractNode(Children.LEAF);
            waitNode.setName(NbBundle.getMessage(WebServiceGroupNodeChildren.class, "NODE_LOAD_MSG"));
            waitNode.setIconBaseWithExtension("org/netbeans/modules/visualweb/websvcmgr/resources/wait.gif"); // NOI18N
            return new Node[] { waitNode };
        }else {
            assert wsData.getWsdlService() != null;
            return new Node[] { new WebServiceNode(wsData) };
        }
    }
    
    public void webServiceAdded(WebServiceGroupEvent groupEvent) {
        updateKeys();
        refreshKey(groupEvent.getWebServiceId());
    }
    
    public void webServiceRemoved(WebServiceGroupEvent groupEvent) {
        updateKeys();
        refreshKey(groupEvent.getWebServiceId());
    }
    
}

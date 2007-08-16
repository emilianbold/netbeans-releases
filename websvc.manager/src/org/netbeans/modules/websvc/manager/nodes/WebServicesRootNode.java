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
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.websvc.manager.actions.*;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;

/**
 * The top level node representing Web Services in the Server Navigator
 * @author octav, Winston Prakash
 */
public class WebServicesRootNode extends AbstractNode implements Node.Cookie {
    
    public WebServicesRootNode() {
        super(new WebServicesRootNodeChildren());
        setName("default");
        setDisplayName(NbBundle.getMessage(WebServicesRootNode.class, "Web_Services"));
        setShortDescription(NbBundle.getMessage(WebServicesRootNode.class, "Web_Services"));
    }
    
    @Override
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/websvcmgr/resources/webservicegroup.png");
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/websvcmgr/resources/webservicegroup.png");
    }
    
    public WebServiceGroup getWebServiceGroup(){
        return WebServiceListModel.getInstance().getWebServiceGroup("default");
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(AddWebServiceAction.class),
            SystemAction.get(AddWebServiceGroupAction.class)
        };
    }

    @Override
    public Action getPreferredAction() {
        // set default action to bring up the add dialog
        return SystemAction.get(AddWebServiceAction.class);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_web_svcs_node");
    }
    
    protected WebServicesRootNodeChildren getWebRootNodeServicesChildren() {
        return (WebServicesRootNodeChildren)getChildren();
    }
    
    @Override
    protected void createPasteTypes(final Transferable t, List<PasteType> s) {
        // Paste type for the default group
        
        Node[] nodes = NodeTransfer.nodes(t, NodeTransfer.DND_COPY);
        for (int i = 0; nodes != null && i < nodes.length; i++) {
            if (nodes[i] instanceof WebServiceNode) {
                final WebServiceData wsData = ((WebServiceNode)nodes[i]).getWebServiceData();
                if (wsData.getGroupId().equals(WebServiceListModel.DEFAULT_GROUP)) {
                    continue;
                }
                
                s.add(new PasteType() {
                    public Transferable paste() throws IOException {
                        WebServiceListModel model = WebServiceListModel.getInstance();
                        WebServiceGroup originalGroup = model.getWebServiceGroup(wsData.getGroupId());
                        WebServiceGroup defaultGroup = model.getWebServiceGroup(WebServiceListModel.DEFAULT_GROUP);
                        
                        originalGroup.remove(wsData.getId());
                        defaultGroup.add(wsData.getId());
                        return t;
                    }
                });
            }
        }
    }
    
}

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

import java.awt.datatransfer.Transferable;
import java.util.List;
import org.netbeans.modules.websvc.manager.actions.AddWebServiceAction;
import org.netbeans.modules.websvc.manager.actions.DeleteWebServiceGroupAction;
import org.netbeans.modules.websvc.manager.actions.RenameWebServiceGroupAction;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction; 
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities; 
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;

/**
 * A second level node representing Group of Web Services
 * @author Winston Prakash
 */
public class WebServiceGroupNode extends AbstractNode implements Node.Cookie {
    private final WebServiceGroup websvcGroup;
    
    public WebServiceGroupNode(WebServiceGroup wsGroup) {
        super(new WebServiceGroupNodeChildren(wsGroup));
        websvcGroup = wsGroup;
        setName(websvcGroup.getName());
    }
    
    @Override
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/websvcmgr/resources/folder-closed.png");
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/websvcmgr/resources/folder-open.png");
    }
    
    public WebServiceGroup getWebServiceGroup(){
        return websvcGroup;
    }
    
    @Override
    public boolean canRename() {
        return true;
    }
    
    @Override
    public void setName(String name){
        websvcGroup.setName(name);
        super.setName(name);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(AddWebServiceAction.class),
            SystemAction.get(DeleteWebServiceGroupAction.class),
            SystemAction.get(RenameWebServiceGroupAction.class)
        };
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public void destroy() throws IOException{
        WebServiceListModel wsListModel = WebServiceListModel.getInstance();
        /**
         * Fix for Bug #:5039378
         * We simply need to remove the group from the list model and the list model
         * will take care of removing the children.
         */
        wsListModel.removeWebServiceGroup(websvcGroup.getId());
        super.destroy();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("websvcGroupNode");
    }

    @Override
    protected void createPasteTypes(final Transferable t, List<PasteType> s) {
        Node[] nodes = NodeTransfer.nodes(t, NodeTransfer.DND_COPY);
        for (int i = 0; nodes != null && i < nodes.length; i++) {
            if (nodes[i] instanceof WebServiceNode) {
                final WebServiceData wsData = ((WebServiceNode)nodes[i]).getWebServiceData();
                if (wsData.getGroupId().equals(websvcGroup.getId())) {
                    continue;
                }
                
                s.add(new PasteType() {
                    public Transferable paste() throws IOException {
                        WebServiceListModel model = WebServiceListModel.getInstance();
                        WebServiceGroup originalGroup = model.getWebServiceGroup(wsData.getGroupId());
                        
                        originalGroup.remove(wsData.getId());
                        websvcGroup.add(wsData.getId());
                        
                        return t;
                    }
                });
            }
        }
    }
    
    
}

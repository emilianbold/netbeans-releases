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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
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
        setShortDescription(NbBundle.getMessage(WebServicesRootNode.class, "Web_Services_Desc"));
    }
    
    @Override
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/webservicegroup.png");
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/webservicegroup.png");
    }
    
    public WebServiceGroup getWebServiceGroup(){
        return WebServiceListModel.getInstance().getWebServiceGroup("default");
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (WebServiceManagerExt ext : ManagerUtil.getExtensions()) {
            for (Action a : ext.getWebServicesRootActions(this)) {
                actions.add(a);
            }
        }
        actions.add(SystemAction.get(AddWebServiceAction.class));
        actions.add(SystemAction.get(AddWebServiceGroupAction.class));
        return actions.toArray(new Action[actions.size()]);
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

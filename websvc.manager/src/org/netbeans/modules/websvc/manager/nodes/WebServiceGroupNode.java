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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities; 
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
    public boolean canRename() {
        return websvcGroup.isUserDefined();
    }

    private Image getUserDirFolderImage(int type) {
        FileObject folder = FileUtil.toFileObject(new File(System.getProperty("netbeans.user"))); //NOI18N
        if (folder != null) {
            DataFolder df = DataFolder.findFolder(folder);
            if (df != null) {
                return df.getNodeDelegate().getIcon(type);
            }
        }
        return null;
    }

    @Override
    public Image getIcon(int type){
        Image standardFolderImage = getUserDirFolderImage(type);
        if (standardFolderImage != null) {
            return standardFolderImage;
        }
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/folder-closed.png");
    }
    
    @Override
    public Image getOpenedIcon(int type){
        Image standardFolderImage = getUserDirFolderImage(type);
        if (standardFolderImage != null) {
            return standardFolderImage;
        }
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/folder-open.png");
    }
    
    public WebServiceGroup getWebServiceGroup(){
        return websvcGroup;
    }
    
    @Override
    public void setName(String name){
        websvcGroup.setName(name);
        super.setName(name);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (WebServiceManagerExt ext : ManagerUtil.getExtensions()) {
            for (Action a : ext.getGroupActions(this)) {
                actions.add(a);
            }
        }
        actions.add(SystemAction.get(AddWebServiceAction.class));
        actions.add(SystemAction.get(DeleteWebServiceGroupAction.class));
        actions.add(SystemAction.get(RenameWebServiceGroupAction.class));
        return actions.toArray(new Action[actions.size()]);
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

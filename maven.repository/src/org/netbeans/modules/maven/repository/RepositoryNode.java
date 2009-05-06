/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.repository;

import java.awt.Image;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.repository.register.RepositoryRegisterUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Anuradha G
 */
public class RepositoryNode extends AbstractNode {

    private RepositoryInfo info;

    public RepositoryNode(RepositoryInfo info) {
        super(new GroupListChildren(info));
        this.info = info;
        setName(info.getId());
        setDisplayName(info.getName());
    }

    @Override
    public Image getIcon(int arg0) {
        if (info.isRemoteDownloadable()) {
            return ImageUtilities.loadImage("org/netbeans/modules/maven/repository/remoterepo.png", true); //NOI18N
        }
        return ImageUtilities.loadImage("org/netbeans/modules/maven/repository/localrepo.png", true); //NOI18N
    }

    @Override
    public Image getOpenedIcon(int arg0) {
        return getIcon(arg0);
    }

    @Override
    public String getShortDescription() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<html>");//NOI18N

        buffer.append(NbBundle.getMessage(RepositoryNode.class,
                "LBL_REPO_ID", info.getId()));//NOI18N

        buffer.append(NbBundle.getMessage(RepositoryNode.class,
                "LBL_REPO_Name", info.getName()));//NOI18N

        //show repo url if available
        if (info.getRepositoryUrl() != null) {
            buffer.append(NbBundle.getMessage(RepositoryNode.class,
                    "LBL_REPO_Url", info.getRepositoryUrl()));//NOI18N
        }
        //show index url if available
        if (info.getIndexUpdateUrl() != null) {
            buffer.append(NbBundle.getMessage(RepositoryNode.class,
                    "LBL_REPO_Index_Url", info.getIndexUpdateUrl()));//NOI18N
        }
        buffer.append("</html>");//NOI18N

        return buffer.toString();
    }

    @Override
    public void destroy() throws IOException {
        RepositoryPreferences.getInstance().removeRepositoryInfo(info);
        super.destroy();
    }

    @Override
    public boolean canDestroy() {
        return !info.isLocal();
    }

    @Override
    public Action[] getActions(boolean arg0) {
        return new Action[]{
            new RefreshIndexAction(),
            new EditAction(),
            DeleteAction.get(DeleteAction.class),
            null,
            PropertiesAction.get(PropertiesAction.class)
        };
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set basicProps = sheet.get(Sheet.PROPERTIES);
        try {
            PropertySupport.Reflection id = new PropertySupport.Reflection<String>(info, String.class, "getId", null); //NOI18N
            id.setName("Id"); //NOI18N
            id.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Id"));
            id.setShortDescription(""); //NOI18N
            PropertySupport.Reflection name = new PropertySupport.Reflection<String>(info, String.class, "getName", null); //NOI18N
            name.setName("name"); //NOI18N
            name.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Name"));
            name.setShortDescription(""); //NOI18N
            PropertySupport.Reflection type = new PropertySupport.Reflection<String>(info, String.class, "getType", null); //NOI18N
            type.setName("type"); //NOI18N
            type.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Repository_Type"));
            PropertySupport.Reflection local = new PropertySupport.Reflection<Boolean>(info, Boolean.TYPE, "isLocal", null); //NOI18N
            local.setName("local"); //NOI18N
            local.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Local"));
            local.setShortDescription("");
            PropertySupport.Reflection localRepoLocation = new PropertySupport.Reflection<String>(info, String.class, "getRepositoryPath", null); //NOI18N
            localRepoLocation.setName("repositoryPath"); //NOI18N
            localRepoLocation.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Local_repository_path"));
            PropertySupport.Reflection remoteDownloadable = new PropertySupport.Reflection<Boolean>(info, Boolean.TYPE, "isRemoteDownloadable", null); //NOI18N
            remoteDownloadable.setName("remoteDownloadable"); //NOI18N
            remoteDownloadable.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Remote_Index"));
            PropertySupport.Reflection repoURL = new PropertySupport.Reflection<String>(info, String.class, "getRepositoryUrl", null); //NOI18N
            repoURL.setName("repositoryUrl"); //NOI18N
            repoURL.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Remote_URL"));
            PropertySupport.Reflection indexURL = new PropertySupport.Reflection<String>(info, String.class, "getIndexUpdateUrl", null); //NOI18N
            indexURL.setName("indexUpdateUrl"); //NOI18N
            indexURL.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryNode.class, "LBL_Remote_Index_URL"));
            basicProps.put(new Node.Property[] {
                id, name, type, local, localRepoLocation, remoteDownloadable, repoURL, indexURL
            
            });
        } catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
        return sheet;
    }

    public class RefreshIndexAction extends AbstractAction {

        public RefreshIndexAction() {
            putValue(NAME, NbBundle.getMessage(RepositoryNode.class,
                    "LBL_REPO_Update_Index"));//NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    RepositoryIndexer.indexRepo(info);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            RefreshIndexAction.this.setEnabled(true);
                        }
                    });
                }
            });
        }
    }
    
    private class EditAction extends AbstractAction {
        public EditAction() {
            putValue(NAME, NbBundle.getMessage(RepositoryNode.class, "ACT_Edit..."));
        }

        public void actionPerformed(ActionEvent e) {
            final RepositoryRegisterUI rrui = new RepositoryRegisterUI();
            rrui.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RepositoryNode.class, "LBL_Add_Repo"));
            rrui.modify(RepositoryNode.this.info);
            DialogDescriptor dd = new DialogDescriptor(rrui, NbBundle.getMessage(RepositoryNode.class, "LBL_Add_Repo"));
            dd.setClosingOptions(new Object[]{
                        rrui.getButton(),
                        DialogDescriptor.CANCEL_OPTION
                    });
            dd.setOptions(new Object[]{
                        rrui.getButton(),
                        DialogDescriptor.CANCEL_OPTION
                    });
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (rrui.getButton() == ret) {
                RepositoryInfo info = rrui.getRepositoryInfo();
                RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(info);
                RepositoryNode.this.info = info;
                setDisplayName(info.getName());
                fireIconChange();
                fireOpenedIconChange();
                ((GroupListChildren)getChildren()).refreshGroups();
            }

        }
    }
}

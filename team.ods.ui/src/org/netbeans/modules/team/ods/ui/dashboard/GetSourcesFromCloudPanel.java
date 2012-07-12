/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * GetFromKenaiPanel.java
 *
 * Created on Feb 24, 2009, 3:36:03 PM
 */

package org.netbeans.modules.team.ods.ui.dashboard;

import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.domain.ScmType;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.c2c.api.ODSProject;
import org.netbeans.modules.team.c2c.client.api.CloudException;
import org.netbeans.modules.team.ui.common.DefaultDashboard;
import org.netbeans.modules.team.ui.common.AddInstanceAction;
import org.netbeans.modules.team.ui.spi.UIUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import static org.netbeans.modules.team.ods.ui.dashboard.Bundle.*;
import org.netbeans.modules.team.ods.ui.CloudServerProviderImpl;
import org.netbeans.modules.team.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ods.ui.dashboard.SourceAccessorImpl.ProjectAndRepository;
import org.netbeans.modules.team.ui.common.LoginHandleImpl;
import org.openide.ServiceType;
import org.openide.util.Exceptions;

/**
 *
 * @author Milan Kubec, Tomas Stupka
 */
public class GetSourcesFromCloudPanel extends javax.swing.JPanel {

    private SourceAccessorImpl.ProjectAndRepository prjAndRepository;
    private boolean localFolderPathEdited = false;

    private DashboardProviderImpl dashboardProvider;
    private DefaultComboBoxModel comboModel;
    private CloudUiServer server;
    private PropertyChangeListener listener;

    public GetSourcesFromCloudPanel(ProjectAndRepository prjFtr, DashboardProviderImpl dashboardProvider) {
        this.dashboardProvider = dashboardProvider;
        this.prjAndRepository = prjFtr;
        initComponents();
        if (prjAndRepository==null) {
            server = ((CloudUiServer) cloudCombo.getSelectedItem());
        } else {
            server = prjAndRepository.project.getTeamServer();
            cloudCombo.setSelectedItem(server);
        }

        refreshUsername();

        comboModel = new CloudRepositoriesComboModel();
        cloudRepoComboBox.setModel(comboModel);
        cloudRepoComboBox.setRenderer(new CloudServiceCellRenderer());

        updatePanelUI();
        updateRepoPath();
        listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (TeamServer.PROP_LOGIN.equals(evt.getPropertyName())) {
                    if (server.getPasswordAuthentication() != null) {
                        loginButton.setEnabled(false);
                    } else {
                        loginButton.setEnabled(true);
                    }
                }
            }
        };
        if (server!=null) {
            server.addPropertyChangeListener(WeakListeners.propertyChange(listener, server));
        }
    }

    public GetSourcesInfo getSelectedSourcesInfo() {
        ScmRepositoryListItem item = (ScmRepositoryListItem) cloudRepoComboBox.getSelectedItem();
        return (item != null) ? new GetSourcesInfo(item.projectHandle, item.repository) : null;
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loggedInLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        loginButton = new javax.swing.JButton();
        cloudRepoLabel = new javax.swing.JLabel();
        cloudRepoComboBox = new javax.swing.JComboBox();
        browseCloudButton = new javax.swing.JButton();
        projectPreviewLabel = new javax.swing.JLabel();
        cloudCombo = UIUtils.createTeamCombo(CloudServerProviderImpl.getDefault(), true);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 0, 12));
        setRequestFocusEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(loggedInLabel, org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loggedInLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetFromCloudPanel.notLoggedIn")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(loginButton, org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loginButton.text")); // NOI18N
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        cloudRepoLabel.setLabelFor(cloudRepoComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(cloudRepoLabel, org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoLabel.text")); // NOI18N

        cloudRepoComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloudRepoComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browseCloudButton, org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.browseCloudButton.text")); // NOI18N
        browseCloudButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCloudButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(projectPreviewLabel, org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.projectPreviewLabel.text")); // NOI18N

        cloudCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloudComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(loggedInLabel)
                        .addGap(33, 33, 33)
                        .addComponent(cloudCombo, 0, 318, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(usernameLabel)
                        .addGap(4, 4, 4)
                        .addComponent(loginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cloudRepoLabel)
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectPreviewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cloudRepoComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(4, 4, 4)
                        .addComponent(browseCloudButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(loggedInLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(usernameLabel))
                    .addComponent(loginButton)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(cloudCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(cloudRepoLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(cloudRepoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(browseCloudButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectPreviewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        loggedInLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loggedInLabel.AccessibleContext.accessibleDescription")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.usernameLabel.AccessibleContext.accessibleName")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.usernameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        loginButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loginButton.AccessibleContext.accessibleDescription")); // NOI18N
        cloudRepoLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        cloudRepoComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoComboBox.AccessibleContext.accessibleName")); // NOI18N
        cloudRepoComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        browseCloudButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.browseCloudButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        boolean loginSuccess = UIUtils.showLogin(server, false) != null;
        if (loginSuccess) {
            refreshUsername();
            UIUtils.activateTeamDashboard();
        } else {
            // login failed, do nothing
        }
}//GEN-LAST:event_loginButtonActionPerformed

    private void browseCloudButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseCloudButtonActionPerformed
        NotYetAction.notYet();
//        KenaiSearchPanel browsePanel = new KenaiSearchPanel(KenaiSearchPanel.PanelType.BROWSE, false, server);
//        String title = NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
//                "GetSourcesFromKenaiPanel.BrowseKenaiProjectsTitle"); // NOI18N
//        DialogDescriptor dialogDesc = new KenaiDialogDescriptor(browsePanel, title, true, null);
//
//        Object option = DialogDisplayer.getDefault().notify(dialogDesc);
//
//        if (NotifyDescriptor.OK_OPTION.equals(option)) {
//            KenaiProjectSearchInfo selProjectInfo = browsePanel.getSelectedProjectSearchInfo();
//            int modelSize = comboModel.getSize();
//            boolean inList = false;
//            ScmRepositoryListItem inListItem = null;
//            for (int i = 0; i < modelSize; i++) {
//                inListItem = (ScmRepositoryListItem) comboModel.getElementAt(i);
//                if (inListItem.project.getName().equals(selProjectInfo.kenaiProject.getName()) &&
//                    inListItem.service.getName().equals(selProjectInfo.kenaiFeature.getName())) {
//                    inList = true;
//                    break;
//                }
//            }
//            if (selProjectInfo != null && !inList) {
//                ScmRepositoryListItem item = new ScmRepositoryListItem(selProjectInfo.kenaiProject, selProjectInfo.kenaiFeature);
//                comboModel.addElement(item);
//                comboModel.setSelectedItem(item);
//            } else if (inList && inListItem != null) {
//                comboModel.setSelectedItem(inListItem);
//            }
//        }

}//GEN-LAST:event_browseCloudButtonActionPerformed

    private void cloudRepoComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cloudRepoComboBoxActionPerformed
        updatePanelUI();
        updateRepoPath();
    }//GEN-LAST:event_cloudRepoComboBoxActionPerformed

    @NbBundle.Messages("CTL_AddInstance=Add Cloud Server")
    private void cloudComboActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cloudComboActionPerformed
        final ActionEvent e = evt;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (cloudCombo.getSelectedItem()!=null && !(cloudCombo.getSelectedItem() instanceof CloudUiServer)) {
                    new AddInstanceAction(CloudServerProviderImpl.getDefault(), CTL_AddInstance()).actionPerformed(e);
                }
                server = ((CloudUiServer) cloudCombo.getSelectedItem());
                cloudRepoComboBox.setModel(new CloudRepositoriesComboModel());
                refreshUsername();
            }
        });
    }//GEN-LAST:event_cloudComboActionPerformed

    private class CloudRepositoriesComboModel extends DefaultComboBoxModel  {

        public CloudRepositoriesComboModel() {
            addOpenedProjects();
        }

        private void addOpenedProjects() {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    ProjectHandleImpl[] openedProjects = getOpenProjects();
                        for (final ProjectHandleImpl prjHandle : openedProjects) {
                            if(prjHandle == null) {
                                continue;
                            }
                            final ODSProject project = prjHandle.getTeamProject();
                            try {
                                Collection<ScmRepository> repositories = project.getRepositories();
                                if(repositories == null) {
                                    continue;
                                }
                                for (final ScmRepository repository : repositories) {
                                    if(repository.getType() == ScmType.GIT) {
                                        final ScmRepositoryListItem item = new ScmRepositoryListItem(prjHandle, repository);
                                        EventQueue.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                addElement(item);
                                                if (prjAndRepository != null &&
                                                    prjAndRepository.project.getId().equals(prjHandle.getId()) &&
                                                    prjAndRepository.repository.getUrl().equals(repository.getUrl())) 
                                                {
                                                    setSelectedItem(item);
                                                }
                                            }
                                        });
                                    }
                                }
                            } catch (CloudException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                    }
                }

                private ProjectHandleImpl[] getOpenProjects() {
                    if (server==null) {
                        return new ProjectHandleImpl[0];
                    }
                    String cloudName = server.getUrl().getHost();
                    // XXX define a different place for preferences. here as well as at all other places.
                    Preferences prefs = NbPreferences.forModule(DefaultDashboard.class).node(DefaultDashboard.PREF_ALL_PROJECTS + ("kenai.com".equals(cloudName) ? "" : "-" + cloudName)); //NOI18N
                    int count = prefs.getInt(DefaultDashboard.PREF_COUNT, 0); //NOI18N
                    ProjectHandleImpl[] handles = new ProjectHandleImpl[count];
                    ArrayList<String> ids = new ArrayList<String>(count);
                    for (int i = 0; i < count; i++) {
                        String id = prefs.get(DefaultDashboard.PREF_ID + i, null); //NOI18N
                        if (null != id && id.trim().length() > 0) {
                            ids.add(id.trim());
                        }
                    }

                    HashSet<ProjectHandleImpl> projects = new HashSet<ProjectHandleImpl>(ids.size());
                    ProjectAccessorImpl accessor = dashboardProvider.getProjectAccessor();
                    for (String id : ids) {
                        ProjectHandleImpl handle = accessor.getNonMemberProject(server, id, false);
                        if (handle != null) {
                            projects.add(handle);
                        } else {
                            //projects=null;
                        }
                    }
                    PasswordAuthentication pa = server.getPasswordAuthentication();
                    if (pa!=null) {
                        projects.addAll(accessor.getMemberProjectsImpls(server, new LoginHandleImpl(pa.getUserName()), false));
                    }
                    return projects.toArray(handles);
                }
            });
        }
    }

    public static class ScmRepositoryListItem {

        ProjectHandleImpl projectHandle;
        ScmRepository repository;

        public ScmRepositoryListItem(ProjectHandleImpl prj, ScmRepository repo) {
            projectHandle = prj;
            repository = repo;
        }

        @Override
        public String toString() {
            return repository.getUrl();
        }

    }

    public static class GetSourcesInfo {

        public ProjectHandleImpl projectHandle;
        public ScmRepository repository;

        public GetSourcesInfo(ProjectHandleImpl projectHandle, ScmRepository repo) {
            this.projectHandle = projectHandle;
            repository = repo;
        }

    }

    private void updatePanelUI() {
        ScmRepositoryListItem item = (ScmRepositoryListItem) cloudRepoComboBox.getSelectedItem();
        if (item != null) {
            String repositoryText = NbBundle.getMessage(GetSourcesFromCloudPanel.class,
                    "GetSourcesFromCloudPanel.RepositoryLabel"); // NOI18N
            if (item.repository.getType() == ScmType.GIT) {
                projectPreviewLabel.setText("(" + item.projectHandle.getDisplayName() + // NOI18N
                        "; Git " + repositoryText + ")"); // NOI18N
            } 
        }
    }

    private void updateRepoPath() {
        ScmRepositoryListItem selItem = (ScmRepositoryListItem) cloudRepoComboBox.getSelectedItem();
        if (!localFolderPathEdited && selItem != null) {
            String urlString = selItem.repository.getUrl();
            String repoName = urlString.substring(urlString.lastIndexOf("/") + 1); // NOI18N
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseCloudButton;
    private javax.swing.JComboBox cloudCombo;
    private javax.swing.JComboBox cloudRepoComboBox;
    private javax.swing.JLabel cloudRepoLabel;
    private javax.swing.JLabel loggedInLabel;
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel projectPreviewLabel;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables

    private void refreshUsername() {
        setChildrenEnabled(this, server!=null);
        PasswordAuthentication passwdAuth = server==null?null:server.getPasswordAuthentication();

        if (passwdAuth != null) {
            setUsername(passwdAuth.getUserName());
            loginButton.setEnabled(false);
        } else {
            setUsername(null);
            loginButton.setEnabled(true);
        }
    }

    private void setChildrenEnabled(Component root, boolean enabled) {
        root.setEnabled(enabled);
        if (root instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) root).getComponents()) {
                if (c != cloudCombo) {
                    setChildrenEnabled(c, enabled);
                }
            }
        }
    }


    private void setUsername(String uName) {
        if (uName != null) {
            usernameLabel.setText(uName);
            usernameLabel.setForeground(new Color(0, 102, 0));
            usernameLabel.setEnabled(true);
        } else {
            usernameLabel.setText(NbBundle.getMessage(GetSourcesFromCloudPanel.class,
                    "GetFromCloudPanel.notLoggedIn")); // NOI18N
            usernameLabel.setForeground(Color.BLACK);
            usernameLabel.setEnabled(false);
        }
    }

    private synchronized void setComboModel(DefaultComboBoxModel model) {
        comboModel = model;
    }

    private synchronized DefaultComboBoxModel getComboModel() {
        return comboModel;
    }

}

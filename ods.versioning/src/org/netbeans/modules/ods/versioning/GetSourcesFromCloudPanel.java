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

package org.netbeans.modules.ods.versioning;

import com.tasktop.c2c.server.scm.domain.ScmLocation;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.domain.ScmType;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.netbeans.modules.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.ods.versioning.SourceAccessorImpl.ProjectAndRepository;
import org.netbeans.modules.ods.ui.api.OdsUIUtil;
import org.netbeans.modules.team.ui.common.LoginHandleImpl;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.ods.versioning.Bundle.*;
import org.netbeans.modules.ods.versioning.spi.ApiProvider;
import org.netbeans.modules.team.ui.common.DefaultDashboard;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 *
 * @author Milan Kubec, Tomas Stupka
 */
public class GetSourcesFromCloudPanel extends javax.swing.JPanel {

    private SourceAccessorImpl.ProjectAndRepository prjAndRepository;
    private boolean localFolderPathEdited = false;

    private DefaultComboBoxModel comboModel;
    private CloudUiServer server;
    private PropertyChangeListener listener;
    private ArrayList<ApiProvider> providerList;

    public GetSourcesFromCloudPanel(ProjectAndRepository prjFtr) {
        this.prjAndRepository = prjFtr;
        initComponents();
        if (prjAndRepository==null) {
            server = ((CloudUiServer) cloudCombo.getSelectedItem());
        } else {
            server = CloudUiServer.forServer(prjAndRepository.project.getTeamProject().getServer());
            cloudCombo.setSelectedItem(server);
            cloudCombo.setEnabled(false);
        }

        refreshUsername();

        initializeProviders();
        
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
        return (item != null) ? new GetSourcesInfo(item.projectHandle, item.repository, item.getUrl()) : null;
    }

    ApiProvider getProvider () {
        return (ApiProvider) cmbProvider.getSelectedItem();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        loggedInLabel = new JLabel();
        usernameLabel = new JLabel();
        loginButton = new JButton();
        cloudRepoLabel = new JLabel();
        cloudRepoComboBox = new JComboBox();
        projectPreviewLabel = new JLabel();
        cloudCombo = OdsUIUtil.createTeamCombo();
        jLabel2 = new JLabel();
        lblError = new JLabel();

        Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.jLabel1.text")); // NOI18N

        setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        setMinimumSize(new Dimension(600, 100));
        setRequestFocusEnabled(false);

        Mnemonics.setLocalizedText(loggedInLabel, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loggedInLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetFromCloudPanel.notLoggedIn")); // NOI18N

        Mnemonics.setLocalizedText(loginButton, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loginButton.text")); // NOI18N
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        cloudRepoLabel.setLabelFor(cloudRepoComboBox);
        Mnemonics.setLocalizedText(cloudRepoLabel, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoLabel.text")); // NOI18N

        cloudRepoComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cloudRepoComboBoxActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(projectPreviewLabel, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.projectPreviewLabel.text")); // NOI18N

        cloudCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cloudComboActionPerformed(evt);
            }
        });

        cmbProvider.setToolTipText(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cmbProvider.toolTipText")); // NOI18N

        Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.jLabel2.text")); // NOI18N

        GroupLayout panelProviderLayout = new GroupLayout(panelProvider);
        panelProvider.setLayout(panelProviderLayout);
        panelProviderLayout.setHorizontalGroup(
            panelProviderLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(panelProviderLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel2)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(cmbProvider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelProviderLayout.setVerticalGroup(
            panelProviderLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(panelProviderLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelProviderLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbProvider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        lblError.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/ods/versioning/resources/error.png"))); // NOI18N
        Mnemonics.setLocalizedText(lblError, NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.lblError.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(cloudRepoLabel)
                    .addComponent(loggedInLabel))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cloudCombo, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(usernameLabel)
                        .addGap(4, 4, 4)
                        .addComponent(loginButton, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(projectPreviewLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(cloudRepoComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(panelProvider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblError))
                        .addGap(0, 135, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(loggedInLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(usernameLabel))
                    .addComponent(loginButton)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(cloudCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(cloudRepoLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(cloudRepoComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7)
                .addComponent(projectPreviewLabel, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(panelProvider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(lblError)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        loggedInLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loggedInLabel.AccessibleContext.accessibleDescription")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.usernameLabel.AccessibleContext.accessibleName")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.usernameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        loginButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.loginButton.AccessibleContext.accessibleDescription")); // NOI18N
        cloudRepoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        cloudRepoComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoComboBox.AccessibleContext.accessibleName")); // NOI18N
        cloudRepoComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.cloudRepoComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromCloudPanel.class, "GetSourcesFromCloudPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        boolean loginSuccess = TeamUIUtils.showLogin(server, false) != null;
        if (loginSuccess) {
            refreshUsername();
            TeamUIUtils.activateTeamDashboard();
            comboModel = new CloudRepositoriesComboModel();
            cloudRepoComboBox.setModel(comboModel);
            cloudRepoComboBox.setSelectedItem(server);
        } else {
            // login failed, do nothing
        }
}//GEN-LAST:event_loginButtonActionPerformed

    private void cloudRepoComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cloudRepoComboBoxActionPerformed
        updatePanelUI();
        updateRepoPath();
    }//GEN-LAST:event_cloudRepoComboBoxActionPerformed

    private void cloudComboActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cloudComboActionPerformed
        final ActionEvent e = evt;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object item = cloudCombo.getSelectedItem();
                if (item != null && !(item instanceof CloudUiServer)) {
                    OdsUIUtil.createAddInstanceAction().actionPerformed(e);
                }
                server = ((CloudUiServer) cloudCombo.getSelectedItem());
                cloudRepoComboBox.setModel(new CloudRepositoriesComboModel());
                refreshUsername();
            }
        });
    }//GEN-LAST:event_cloudComboActionPerformed

    private void initializeProviders () {
        cmbProvider.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof ApiProvider) {
                    value = ((ApiProvider) value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }

        });
        Collection<? extends ApiProvider> providers = Lookup.getDefault().lookupAll(ApiProvider.class);
        providerList = new ArrayList<ApiProvider>(providers);
        Collections.sort(providerList, new Comparator<ApiProvider>() {
            @Override
            public int compare (ApiProvider p1, ApiProvider p2) {
                return p1.getName().compareToIgnoreCase(p2.getName());
            }
        });
        lblError.setVisible(false);
    }

    private void updateProviders (ScmRepository repository, String url) {
        List<ApiProvider> providers = new ArrayList<ApiProvider>(providerList.size());
        ApiProvider preferredProvider = null, gitProvider = null;
        Preferences prefs = NbPreferences.forModule(GetSourcesFromCloudPanel.class);
        String className = prefs.get("repository.scm.provider." + url, ""); //NOI18N
        for (ApiProvider p : providerList) {
            if (repository.getScmLocation() != ScmLocation.CODE2CLOUD || p.accepts(repository.getType().name())) {
                providers.add(p);
                if (className.equals(p.getClass().getName())) {
                    preferredProvider = p;
                }
                if (p.accepts(ScmType.GIT.name())) {
                    gitProvider = p;
                }
            }
        }
        if (gitProvider != null) {
            preferredProvider = gitProvider;
        }
        
        cmbProvider.setModel(new DefaultComboBoxModel(providers.toArray(new ApiProvider[providers.size()])));
        lblError.setVisible(false);
        panelProvider.setVisible(false);
        if (providers.isEmpty()) {
            lblError.setVisible(true);
        } else if (providers.size() > 1) {
            if (preferredProvider != null) {
                cmbProvider.setSelectedItem(preferredProvider);
            }
            panelProvider.setVisible(true);
        }
    }

    private class CloudRepositoriesComboModel extends DefaultComboBoxModel  {

        public CloudRepositoriesComboModel() {
            addOpenedProjects();
        }

        private void addOpenedProjects() {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    ProjectHandle<ODSProject>[] openedProjects = getOpenProjects();
                    if (openedProjects != null) {
                        for (final ProjectHandle<ODSProject> prjHandle : openedProjects) {
                            if(prjHandle == null) {
                                continue;
                            }
                            final ODSProject project = prjHandle.getTeamProject();
                            try {
                                Collection<ScmRepository> repositories = project.getRepositories();
                                if(repositories == null) {
                                    continue;
                                }
                                final List<ScmRepositoryListItem> items = new ArrayList<ScmRepositoryListItem>(repositories.size() * 2);
                                for (final ScmRepository repository : repositories) {
                                    if (prjAndRepository == null || prjAndRepository.project.getId().equals(prjHandle.getId()) &&
                                                    prjAndRepository.repository.getUrl().equals(repository.getUrl())) {
                                        if (SourceAccessorImpl.isSupported(repository.getScmLocation() == ScmLocation.CODE2CLOUD
                                                ? repository.getType()
                                                : null)) {
                                            items.add(new ScmRepositoryListItem(prjHandle, repository, repository.getUrl()));
                                            if (repository.getAlternateUrl() != null && !repository.getAlternateUrl().isEmpty()
                                                    && !repository.getUrl().equals(repository.getAlternateUrl())) {
                                                items.add(new ScmRepositoryListItem(prjHandle, repository, repository.getAlternateUrl()));
                                            }
                                        }
                                    }
                                }
                                if (!items.isEmpty()) {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (ScmRepositoryListItem item : items) {
                                                addElement(item);
                                                if (prjAndRepository != null &&
                                                    prjAndRepository.project.getId().equals(prjHandle.getId()) &&
                                                    prjAndRepository.repository.getUrl().equals(item.getUrl())) 
                                                {
                                                    setSelectedItem(item);
                                                }
                                            }
                                        }
                                    });
                                }
                            } catch (ODSException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }

                private ProjectHandle<ODSProject>[] getOpenProjects() {
                    if (server == null) {
                        return new ProjectHandle[0];
                    }
                    String cloudName = server.getUrl().getHost();
                    
                    // XXX do we need this for ODS projects? >>>>>>>>>>>>>>>>>>>>>>>>>>
                    // XXX define a different place for preferences. here as well as at all other places.
                    Preferences prefs = NbPreferences.forModule(DefaultDashboard.class).node(DefaultDashboard.PREF_ALL_PROJECTS + ("kenai.com".equals(cloudName) ? "" : "-" + cloudName)); //NOI18N
                    int count = prefs.getInt(DefaultDashboard.PREF_COUNT, 0); //NOI18N
                    ProjectHandle[] handles = new ProjectHandle[count];
                    ArrayList<String> ids = new ArrayList<String>(count);
                    for (int i = 0; i < count; i++) {
                        String id = prefs.get(DefaultDashboard.PREF_ID + i, null); //NOI18N
                        if (null != id && id.trim().length() > 0) {
                            ids.add(id.trim());
                        }
                    }

                    HashSet<ProjectHandle> projects = new HashSet<ProjectHandle>(ids.size());
                    ProjectAccessor<CloudUiServer, ODSProject> projectAcccessor = server.getDashboard().getDashboardProvider().getProjectAccessor();
                    for (String id : ids) {
                        ProjectHandle handle = projectAcccessor.getNonMemberProject(server, id, false);
                        if (handle != null) {
                            projects.add(handle);
                        } else {
                            //projects=null;
                        }
                    }
                    
                    PasswordAuthentication pa = server.getPasswordAuthentication();
                    if (pa != null) {
                        projects.addAll(projectAcccessor.getMemberProjects(server, new LoginHandleImpl(pa.getUserName()), false));
                    }
                    return projects.toArray(handles);
                }
            });
        }
    }

    public static class ScmRepositoryListItem {

        final ProjectHandle<ODSProject> projectHandle;
        final ScmRepository repository;
        final String url;

        public ScmRepositoryListItem(ProjectHandle<ODSProject> prj, ScmRepository repo, String url) {
            projectHandle = prj;
            repository = repo;
            this.url = url;
        }

        @Override
        public String toString() {
            return getUrl();
        }

        public String getUrl () {
            return url;
        }

    }

    public static class GetSourcesInfo {

        public ProjectHandle<ODSProject> projectHandle;
        public ScmRepository repository;
        final String url;

        public GetSourcesInfo(ProjectHandle<ODSProject> projectHandle, ScmRepository repo, String url) {
            this.projectHandle = projectHandle;
            repository = repo;
            this.url = url;
        }

    }

    @Messages({"# {0} - project name", "# {1} - repository type",
        "LBL_GetSourceFromCloudPanel.repositoryLabel=({0}; {1} repository)",
        "LBL_GetSourceFromCloudPanel.repository.external=External",
        "LBL_GetSourceFromCloudPanel.repository.svn=Subversion",
        "LBL_GetSourceFromCloudPanel.repository.git=Git"})
    private void updatePanelUI() {
        ScmRepositoryListItem item = (ScmRepositoryListItem) cloudRepoComboBox.getSelectedItem();
        if (item != null) {
            String repositoryType = null;
            if (item.repository.getScmLocation() == ScmLocation.CODE2CLOUD) {
                if (item.repository.getType() == ScmType.GIT) {
                    repositoryType = LBL_GetSourceFromCloudPanel_repository_git();
                } else if (item.repository.getType() == ScmType.SVN) {
                    repositoryType = LBL_GetSourceFromCloudPanel_repository_svn();
                }
            }
            updateProviders(item.repository, item.getUrl());
            if (repositoryType == null) {
                repositoryType = LBL_GetSourceFromCloudPanel_repository_external();
            }
            projectPreviewLabel.setText(LBL_GetSourceFromCloudPanel_repositoryLabel(item.projectHandle.getDisplayName(), repositoryType));
        }
    }

    private void updateRepoPath() {
        ScmRepositoryListItem selItem = (ScmRepositoryListItem) cloudRepoComboBox.getSelectedItem();
        if (!localFolderPathEdited && selItem != null) {
            String urlString = selItem.getUrl();
            String repoName = urlString.substring(urlString.lastIndexOf("/") + 1); // NOI18N
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox cloudCombo;
    private JComboBox cloudRepoComboBox;
    private JLabel cloudRepoLabel;
    final JComboBox cmbProvider = new JComboBox();
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel lblError;
    private JLabel loggedInLabel;
    private JButton loginButton;
    final JPanel panelProvider = new JPanel();
    private JLabel projectPreviewLabel;
    private JLabel usernameLabel;
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

}

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

package org.netbeans.modules.kenai.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.KenaiSearchPanel.KenaiProjectSearchInfo;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl.ProjectAndFeature;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.api.TeamUIUtils;
import org.netbeans.modules.subversion.api.Subversion;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.util.Lookup;

/**
 *
 * @author Milan Kubec
 */
public class GetSourcesFromKenaiPanel extends javax.swing.JPanel {

    private ProjectAndFeature prjAndFeature;
    private boolean localFolderPathEdited = false;

    private DefaultComboBoxModel comboModel;
    private Kenai kenai;
    private final PropertyChangeListener listener;

    GetSourcesFromKenaiPanel(Kenai kenai) {
        this(kenai, null);
    }
    
    GetSourcesFromKenaiPanel(ProjectAndFeature prjFtr) {
        this(null, prjFtr);
    }
    
    private GetSourcesFromKenaiPanel(Kenai kenai, ProjectAndFeature prjFtr) {

        this.prjAndFeature = prjFtr;
        initComponents();
        if (prjAndFeature!=null) {
            this.kenai = prjAndFeature.kenaiProject.getKenai();
        } else if(kenai != null) {
            this.kenai = kenai;
        } 
        if(this.kenai != null) {
            serverLabel.setText(this.kenai.getName());
            serverLabel.setIcon(this.kenai.getIcon());
        }
        
        IDEServices ide = Lookup.getDefault().lookup(IDEServices.class);
        proxyConfigButton.setVisible(ide != null && ide.providesProxyConfiguration());
        
        refreshUsername();

        comboModel = new KenaiRepositoriesComboModel();
        kenaiRepoComboBox.setModel(comboModel);
        kenaiRepoComboBox.setRenderer(new KenaiFeatureCellRenderer());

        updatePanelUI();
        updateRepoPath();
        listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (TeamServer.PROP_LOGIN.equals(evt.getPropertyName())) {
                    if (GetSourcesFromKenaiPanel.this.kenai.getPasswordAuthentication() != null) {
                        loginButton.setEnabled(false);
                    } else {
                        loginButton.setEnabled(true);
                    }
                }
            }
        };
        if (kenai!=null)
            kenai.addPropertyChangeListener(WeakListeners.propertyChange(listener, kenai));
    }

    public GetSourcesInfo getSelectedSourcesInfo() {

        StringTokenizer stok = new StringTokenizer(repoFolderTextField.getText(), ","); // NOI18N
        ArrayList<String> repoFolders = new ArrayList<String>();
        while (stok.hasMoreTokens()) {
            repoFolders.add(stok.nextToken().trim());
        }
        String relPaths[] = repoFolders.isEmpty() ? new String[] { "" } : repoFolders.toArray(new String[repoFolders.size()]); // NOI18N
        KenaiFeatureListItem featureItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();

        return (featureItem != null) ? new GetSourcesInfo(featureItem.feature,
                localFolderTextField.getText(), relPaths) : null;
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        loggedInLabel = new JLabel();
        usernameLabel = new JLabel();
        loginButton = new JButton();
        kenaiRepoLabel = new JLabel();
        kenaiRepoComboBox = new JComboBox();
        browseKenaiButton = new JButton();
        projectPreviewLabel = new JLabel();
        repoFolderLabel = new JLabel();
        repoFolderTextField = new JTextField();
        browseRepoButton = new JButton();
        localFolderDescLabel = new JLabel();
        localFolderLabel = new JLabel();
        localFolderTextField = new JTextField();
        browseLocalButton = new JButton();
        proxyConfigButton = new JButton();
        emptySpace = new JPanel();
        serverLabel = new JLabel();

        setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        setPreferredSize(new Dimension(700, 250));
        setRequestFocusEnabled(false);
        setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(loggedInLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loggedInLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 12, 0);
        add(loggedInLabel, gridBagConstraints);
        loggedInLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loggedInLabel.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetFromKenaiPanel.notLoggedIn")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 12, 0);
        add(usernameLabel, gridBagConstraints);
        usernameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.usernameLabel.AccessibleContext.accessibleName")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.usernameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(loginButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loginButton.text")); // NOI18N
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 12, 0);
        add(loginButton, gridBagConstraints);
        loginButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loginButton.AccessibleContext.accessibleDescription")); // NOI18N

        kenaiRepoLabel.setLabelFor(kenaiRepoComboBox);
        Mnemonics.setLocalizedText(kenaiRepoLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(kenaiRepoLabel, gridBagConstraints);
        kenaiRepoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoLabel.AccessibleContext.accessibleDescription")); // NOI18N

        kenaiRepoComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                kenaiRepoComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(kenaiRepoComboBox, gridBagConstraints);
        kenaiRepoComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoComboBox.AccessibleContext.accessibleName")); // NOI18N
        kenaiRepoComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(browseKenaiButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseKenaiButton.text")); // NOI18N
        browseKenaiButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseKenaiButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(browseKenaiButton, gridBagConstraints);
        browseKenaiButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseKenaiButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(projectPreviewLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.projectPreviewLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 6, 16, 0);
        add(projectPreviewLabel, gridBagConstraints);

        repoFolderLabel.setLabelFor(repoFolderTextField);
        Mnemonics.setLocalizedText(repoFolderLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(repoFolderLabel, gridBagConstraints);
        repoFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N

        repoFolderTextField.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(repoFolderTextField, gridBagConstraints);
        repoFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        repoFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(browseRepoButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseRepoButton.text")); // NOI18N
        browseRepoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseRepoButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(browseRepoButton, gridBagConstraints);
        browseRepoButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseRepoButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(localFolderDescLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderDescLabel.svnText")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(24, 0, 6, 0);
        add(localFolderDescLabel, gridBagConstraints);
        localFolderDescLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderDescLabel.AccessibleContext.accessibleDescription")); // NOI18N

        localFolderLabel.setLabelFor(localFolderTextField);
        Mnemonics.setLocalizedText(localFolderLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(localFolderLabel, gridBagConstraints);
        localFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N

        localFolderTextField.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderTextField.text")); // NOI18N
        localFolderTextField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                localFolderTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(localFolderTextField, gridBagConstraints);
        localFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        localFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(browseLocalButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseLocalButton.text")); // NOI18N
        browseLocalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseLocalButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(browseLocalButton, gridBagConstraints);
        browseLocalButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseLocalButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(proxyConfigButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.proxyConfigButton.text")); // NOI18N
        proxyConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                proxyConfigButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        add(proxyConfigButton, gridBagConstraints);
        proxyConfigButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.proxyConfigButton.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(emptySpace, gridBagConstraints);

        Mnemonics.setLocalizedText(serverLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.serverLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 12, 0);
        add(serverLabel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        boolean loginSuccess = org.netbeans.modules.kenai.ui.api.KenaiUIUtils.showLogin(kenai);
        if (loginSuccess) {
            refreshUsername();
            TeamUIUtils.activateTeamDashboard();
        } else {
            // login failed, do nothing
        }
}//GEN-LAST:event_loginButtonActionPerformed

    private void proxyConfigButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_proxyConfigButtonActionPerformed
        IDEServices ide = Lookup.getDefault().lookup(IDEServices.class);
        if(ide != null && ide.providesProxyConfiguration()) {
            ide.openProxyConfiguration();
        }
}//GEN-LAST:event_proxyConfigButtonActionPerformed

    private void browseKenaiButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseKenaiButtonActionPerformed
        
        KenaiSearchPanel browsePanel = new KenaiSearchPanel(KenaiSearchPanel.PanelType.BROWSE, false, kenai);
        String title = NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                "GetSourcesFromKenaiPanel.BrowseKenaiProjectsTitle"); // NOI18N
        DialogDescriptor dialogDesc = new KenaiDialogDescriptor(browsePanel, title, true, null);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);

        if (NotifyDescriptor.OK_OPTION.equals(option)) {
            KenaiProjectSearchInfo selProjectInfo = browsePanel.getSelectedProjectSearchInfo();
            int modelSize = comboModel.getSize();
            boolean inList = false;
            KenaiFeatureListItem inListItem = null;
            for (int i = 0; i < modelSize; i++) {
                inListItem = (KenaiFeatureListItem) comboModel.getElementAt(i);
                if (inListItem.project.getName().equals(selProjectInfo.kenaiProject.getName()) &&
                    inListItem.feature.getName().equals(selProjectInfo.kenaiFeature.getName())) {
                    inList = true;
                    break;
                }
            }
            if (selProjectInfo != null && !inList) {
                KenaiFeatureListItem item = new KenaiFeatureListItem(selProjectInfo.kenaiProject, selProjectInfo.kenaiFeature);
                comboModel.addElement(item);
                comboModel.setSelectedItem(item);
            } else if (inList && inListItem != null) {
                comboModel.setSelectedItem(inListItem);
            }
        }

}//GEN-LAST:event_browseKenaiButtonActionPerformed

    private void browseRepoButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseRepoButtonActionPerformed
        if (Subversion.isClientAvailable(true)) {
            PasswordAuthentication passwdAuth = kenai.getPasswordAuthentication();

            KenaiFeatureListItem featureItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();
            String svnFolders[] = null;
            if (featureItem != null) {
                String title = NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                        "GetSourcesFromKenaiPanel.SelectRepositoryFolderTitle"); // NOI18N
                String repoUrl = featureItem.feature.getLocation();
                try {
                    if (passwdAuth != null) {
                        svnFolders = Subversion.selectRepositoryFolders(title, repoUrl,
                                passwdAuth.getUserName(), passwdAuth.getPassword());
                    } else {
                        svnFolders = Subversion.selectRepositoryFolders(title, repoUrl);
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException io) {
                    if (Subversion.CLIENT_UNAVAILABLE_ERROR_MESSAGE.equals(io.getMessage())) {
                        // DO SOMETHING, svn client is unavailable
                    } else {
                        Exceptions.printStackTrace(io);
                    }
                }
            }
            if (svnFolders != null) {
                repoFolderTextField.setText(svnFolders[0]);
            }
        }
    }//GEN-LAST:event_browseRepoButtonActionPerformed

    private void browseLocalButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseLocalButtonActionPerformed

        JFileChooser chooser = new JFileChooser();
        File uFile = new File(localFolderTextField.getText());
        if (uFile.exists()) {
            chooser.setCurrentDirectory(FileUtil.normalizeFile(uFile));
        }
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selFile = chooser.getSelectedFile();
            localFolderTextField.setText(selFile.getAbsolutePath());
        }
        
    }//GEN-LAST:event_browseLocalButtonActionPerformed

    private void kenaiRepoComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_kenaiRepoComboBoxActionPerformed
        updatePanelUI();
        updateRepoPath();
    }//GEN-LAST:event_kenaiRepoComboBoxActionPerformed

    private void localFolderTextFieldKeyTyped(KeyEvent evt) {//GEN-FIRST:event_localFolderTextFieldKeyTyped
        localFolderPathEdited = true;
    }//GEN-LAST:event_localFolderTextFieldKeyTyped

    private class KenaiRepositoriesComboModel extends DefaultComboBoxModel  {

        public KenaiRepositoriesComboModel() {
            addOpenedProjects();
        }

        private void addOpenedProjects() {
            Utilities.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    ProjectHandle[] openedProjects = getOpenProjects();
                        for (ProjectHandle<KenaiProject> prjHandle : openedProjects) {
                            KenaiProject kProject = null;
                            if (prjHandle != null) {
                                kProject = prjHandle.getTeamProject();
                            }
                            final KenaiProject project = kProject;
                            if (project != null) {
                            try {
                                KenaiFeature features[] = project.getFeatures(Type.SOURCE);
                                for (final KenaiFeature feature : features) {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (KenaiService.Names.MERCURIAL.equals(feature.getService()) ||
                                                KenaiService.Names.SUBVERSION.equals(feature.getService())) {
                                                KenaiFeatureListItem item = new KenaiFeatureListItem(project, feature);
                                                addElement(item);
                                                if (prjAndFeature != null &&
                                                    prjAndFeature.kenaiProject.getName().equals(project.getName()) &&
                                                    prjAndFeature.feature.equals(feature)) {
                                                    setSelectedItem(item);
                                                }
                                            }
                                        }
                                    });
                                }
                            } catch (KenaiException kenaiException) {
                                Exceptions.printStackTrace(kenaiException);
                            }
                        }
                    }
                }

                private ProjectHandle<KenaiProject>[] getOpenProjects() {
                    if (kenai==null) {
                        return new ProjectHandle[0];
                    }
                    String kenaiName = kenai.getUrl().getHost();
                    Preferences prefs = NbPreferences.forModule(DashboardSupport.class).node(DashboardSupport.PREF_ALL_PROJECTS + ("kenai.com".equals(kenaiName) ? "" : "-" + kenaiName)); //NOI18N
                    int count = prefs.getInt(DashboardSupport.PREF_COUNT, 0); //NOI18N
                    ProjectHandle[] handles = new ProjectHandle[count];
                    ArrayList<String> ids = new ArrayList<String>(count);
                    for (int i = 0; i < count; i++) {
                        String id = prefs.get(DashboardSupport.PREF_ID + i, null); //NOI18N
                        if (null != id && id.trim().length() > 0) {
                            ids.add(id.trim());
                        }
                    }

                    HashSet<ProjectHandle> projects = new HashSet<ProjectHandle>(ids.size());
                    ProjectAccessorImpl accessor = ProjectAccessorImpl.getDefault();
                    for (String id : ids) {
                        ProjectHandle handle = accessor.getNonMemberProject(KenaiServer.forKenai(kenai), id, false);
                        if (handle != null) {
                            projects.add(handle);
                        } else {
                            //projects=null;
                        }
                    }
                    PasswordAuthentication pa = kenai.getPasswordAuthentication();
                    if (pa!=null) {
                        projects.addAll(accessor.getMemberProjects(KenaiServer.forKenai(kenai), new LoginHandleImpl(pa.getUserName()), false));
                    }
                    return projects.toArray(handles);
                }
            });
        }
    }

    public static class KenaiFeatureListItem {

        KenaiProject project;
        KenaiFeature feature;

        public KenaiFeatureListItem(KenaiProject prj, KenaiFeature ftr) {
            project = prj;
            feature = ftr;
        }

        @Override
        public String toString() {
            return feature.getLocation();
        }

    }

    public static class GetSourcesInfo {

        public KenaiFeature feature;
        public String localFolderPath;
        public String relativePaths[];

        public GetSourcesInfo(KenaiFeature ftr, String lcl, String[] rel) {
            feature = ftr;
            localFolderPath = lcl;
            relativePaths = rel;
        }

    }

    private void updatePanelUI() {
        KenaiFeatureListItem featureItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();
        if (featureItem != null) {
            String serviceName = featureItem.feature.getService();
            String repositoryText = NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                    "GetSourcesFromKenaiPanel.RepositoryLabel"); // NOI18N
            if (KenaiService.Names.SUBVERSION.equals(serviceName)) {
                enableFolderToGetUI(true);
                localFolderDescLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                        "GetSourcesFromKenaiPanel.localFolderDescLabel.svnText")); // NOI18N
                projectPreviewLabel.setText("(" + featureItem.project.getDisplayName() + // NOI18N
                        "; Subversion " + repositoryText + ")"); // NOI18N
            } else if (KenaiService.Names.MERCURIAL.equals(serviceName)) {
                enableFolderToGetUI(false);
                localFolderDescLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                        "GetSourcesFromKenaiPanel.localFolderDescLabel.hgText")); // NOI18N
                projectPreviewLabel.setText("(" + featureItem.project.getDisplayName() + // NOI18N
                        "; Mercurial " + repositoryText + ")"); // NOI18N
            } else {
                enableFolderToGetUI(false);
            }
        }
    }

    private void updateRepoPath() {
        KenaiFeatureListItem selItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();
        if (!localFolderPathEdited && selItem != null) {
            String urlString = selItem.feature.getLocation();
            String repoName = urlString.substring(urlString.lastIndexOf("/") + 1); // NOI18N
            localFolderTextField.setText(TeamUIUtils.getDefaultRepoFolder().getPath() + File.separator + repoName);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseKenaiButton;
    private JButton browseLocalButton;
    private JButton browseRepoButton;
    private JPanel emptySpace;
    private JComboBox kenaiRepoComboBox;
    private JLabel kenaiRepoLabel;
    private JLabel localFolderDescLabel;
    private JLabel localFolderLabel;
    private JTextField localFolderTextField;
    private JLabel loggedInLabel;
    private JButton loginButton;
    private JLabel projectPreviewLabel;
    private JButton proxyConfigButton;
    private JLabel repoFolderLabel;
    private JTextField repoFolderTextField;
    private JLabel serverLabel;
    private JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables

    private void refreshUsername() {
        setChildrenEnabled(this, kenai!=null);
        PasswordAuthentication passwdAuth = kenai==null?null:kenai.getPasswordAuthentication();

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
                setChildrenEnabled(c, enabled);
            }
        }
    }


    private void setUsername(String uName) {
        if (uName != null) {
            usernameLabel.setText(uName);
            usernameLabel.setForeground(new Color(0, 102, 0));
            usernameLabel.setEnabled(true);
        } else {
            usernameLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                    "GetFromKenaiPanel.notLoggedIn")); // NOI18N
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

    private void enableFolderToGetUI(boolean enable) {
        repoFolderLabel.setEnabled(enable);
        repoFolderTextField.setEnabled(enable);
        browseRepoButton.setEnabled(enable);
    }

}

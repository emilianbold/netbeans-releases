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
 * NameAndLicenseWizardPanelGUI.java
 *
 * Created on Feb 6, 2009, 11:15:27 AM
 */
package org.netbeans.modules.ods.ui;

import com.tasktop.c2c.server.profile.domain.project.ProjectAccessibility;
import com.tasktop.c2c.server.profile.domain.project.WikiMarkupLanguage;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.common.AddInstanceAction;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import static org.netbeans.modules.ods.ui.Bundle.*;
import org.netbeans.modules.ods.ui.api.OdsUIUtil;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milan Kubec
 */
public class NameAndLicenseWizardPanelGUI extends JPanel {

    private RequestProcessor errorChecker = new RequestProcessor("Error Checker"); // NOI18N
    private WizardDescriptor settings;
    private NameAndLicenseWizardPanel panel;
    private PropertyChangeListener serverListener;
    private String prjNameCheckMessage = null;

    @NbBundle.Messages("CTL_AddInstance=Add Server")
    public NameAndLicenseWizardPanelGUI (NameAndLicenseWizardPanel pnl) {

        panel = pnl;
        initComponents();

        teamCombo.setSelectedItem(CloudUiServer.forServer(panel.getServer()));
        teamCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (final ActionEvent e) {
                if (teamCombo.getSelectedItem() instanceof CloudUiServer) {
                    panel.setServer(((CloudUiServer) teamCombo.getSelectedItem()).getServer());
                    refreshUsername();
                } else if (teamCombo.getSelectedItem() instanceof String) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            new AddInstanceAction(CloudServerProviderImpl.getDefault(), Bundle.CTL_AddInstance()).actionPerformed(e);
                            panel.setServer(((CloudUiServer) teamCombo.getSelectedItem()).getServer());
                            refreshUsername();
                        }
                    });
                }
                setupLicensesListModel();
            }
        });


        refreshUsername();

        DocumentListener firingDocListener = new DocumentListener() {
            @Override
            public void insertUpdate (DocumentEvent e) {
                panel.fireChangeEvent();
            }

            @Override
            public void removeUpdate (DocumentEvent e) {
                panel.fireChangeEvent();
            }

            @Override
            public void changedUpdate (DocumentEvent e) {
                panel.fireChangeEvent();
            }
        };

        projectNameTextField.getDocument().addDocumentListener(firingDocListener);
        projectDescTextField.getDocument().addDocumentListener(firingDocListener);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                projectNameTextField.requestFocus();
            }
        });


        setupLicensesListModel();
        setPreferredSize(new Dimension(Math.max(700, getPreferredSize().width), 450));

        serverListener = new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (TeamServer.PROP_LOGIN.equals(evt.getPropertyName())) {
                    if (panel.getServer().getPasswordAuthentication() != null) {
                        loginButton.setEnabled(false);
                    } else {
                        loginButton.setEnabled(true);
                    }
                }
            }
        };
        if (panel.getServer() != null) {
            panel.getServer().addPropertyChangeListener(WeakListeners.propertyChange(serverListener, panel.getServer()));
        }

    }

    private void setChildrenEnabled (Component root, boolean enabled) {
        root.setEnabled(enabled);
        if (root instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) root).getComponents()) {
                if (c != teamCombo) {
                    setChildrenEnabled(c, enabled);
                }
            }
        }
    }

    private void setupLicensesListModel () {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        projectWikiComboBox.setModel(model);
        for (WikiMarkupLanguage lang : WikiMarkupLanguage.values()) {
            model.addElement(lang);
        }
    }

    @Override
    public String getName () {
        return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                "NameAndLicenseWizardPanelGUI.panelName"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        btnGrpPrivacy = new ButtonGroup();
        loggedInLabel = new JLabel();
        usernameLabel = new JLabel();
        loginButton = new JButton();
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        projectDescLabel = new JLabel();
        projectWikiLabel = new JLabel();
        projectWikiComboBox = new JComboBox();
        proxyConfigButton = new JButton();
        teamCombo = TeamUIUtils.createTeamCombo(CloudServerProviderImpl.getDefault(), true);
        jScrollPane1 = new JScrollPane();
        projectDescTextField = new JTextArea();
        projectPrivacyLabel = new JLabel();
        btnPrivacyPrivate = new JRadioButton();
        btnPrivacyPublic = new JRadioButton();
        btnPrivacyOrganizationPrivate = new JRadioButton();

        setLayout(new GridBagLayout());

        loggedInLabel.setLabelFor(teamCombo);
        Mnemonics.setLocalizedText(loggedInLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loggedInLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(loggedInLabel, gridBagConstraints);
        loggedInLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loggedInLabel.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.notLoggedIn")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new Insets(0, 4, 5, 0);
        add(usernameLabel, gridBagConstraints);
        usernameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.usernameLabel.AccessibleContext.accessibleName")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.usernameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(loginButton, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loginButton.text")); // NOI18N
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 4, 5, 0);
        add(loginButton, gridBagConstraints);
        loginButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loginButton.AccessibleContext.accessibleDescription")); // NOI18N

        projectNameLabel.setLabelFor(projectNameTextField);
        Mnemonics.setLocalizedText(projectNameLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        add(projectNameLabel, gridBagConstraints);
        projectNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectNameTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameTextField.text")); // NOI18N
        projectNameTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent evt) {
                projectNameTextFieldFocusLost(evt);
            }
        });
        projectNameTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                projectNameTextFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(projectNameTextField, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameTextField.AccessibleContext.accessibleName")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameTextField.AccessibleContext.accessibleDescription")); // NOI18N

        projectDescLabel.setLabelFor(projectDescTextField);
        Mnemonics.setLocalizedText(projectDescLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(4, 0, 0, 4);
        add(projectDescLabel, gridBagConstraints);
        projectDescLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectWikiLabel.setLabelFor(projectWikiComboBox);
        Mnemonics.setLocalizedText(projectWikiLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectWikiLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 4);
        add(projectWikiLabel, gridBagConstraints);
        projectWikiLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectWikiLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectWikiComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectWikiComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(projectWikiComboBox, gridBagConstraints);
        projectWikiComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectWikiComboBox.AccessibleContext.accessibleName")); // NOI18N
        projectWikiComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectWikiComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(proxyConfigButton, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.proxyConfigButton.text")); // NOI18N
        proxyConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                proxyConfigButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(proxyConfigButton, gridBagConstraints);
        proxyConfigButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.proxyConfigButton.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(teamCombo, gridBagConstraints);

        projectDescTextField.setColumns(20);
        projectDescTextField.setRows(5);
        projectDescTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescTextField.text")); // NOI18N
        jScrollPane1.setViewportView(projectDescTextField);
        projectDescTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescTextField.AccessibleContext.accessibleName")); // NOI18N
        projectDescTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescTextField.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        Mnemonics.setLocalizedText(projectPrivacyLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectPrivacyLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(projectPrivacyLabel, gridBagConstraints);
        projectPrivacyLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectPrivacyLabel.AccessibleContext.accessibleDescription")); // NOI18N

        btnGrpPrivacy.add(btnPrivacyPrivate);
        Mnemonics.setLocalizedText(btnPrivacyPrivate, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.btnPrivacyPrivate.text")); // NOI18N
        btnPrivacyPrivate.setToolTipText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.btnPrivacyPrivate.TTtext")); // NOI18N
        btnPrivacyPrivate.setActionCommand(ProjectAccessibility.PRIVATE.name());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(btnPrivacyPrivate, gridBagConstraints);

        btnGrpPrivacy.add(btnPrivacyPublic);
        btnPrivacyPublic.setSelected(true);
        Mnemonics.setLocalizedText(btnPrivacyPublic, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.btnPrivacyPublic.text")); // NOI18N
        btnPrivacyPublic.setToolTipText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.btnPrivacyPublic.TTtext")); // NOI18N
        btnPrivacyPublic.setActionCommand(ProjectAccessibility.PUBLIC.name());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(btnPrivacyPublic, gridBagConstraints);

        btnGrpPrivacy.add(btnPrivacyOrganizationPrivate);
        Mnemonics.setLocalizedText(btnPrivacyOrganizationPrivate, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.btnPrivacyOrganizationPrivate.text")); // NOI18N
        btnPrivacyOrganizationPrivate.setToolTipText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.btnPrivacyOrganizationPrivate.TTtext")); // NOI18N
        btnPrivacyOrganizationPrivate.setActionCommand(ProjectAccessibility.ORGANIZATION_PRIVATE.name());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(btnPrivacyOrganizationPrivate, gridBagConstraints);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        boolean loginSuccess = OdsUIUtil.showLogin();
        if (loginSuccess) {
            panel.fireChangeEvent();
            refreshUsername();
        } else {
            // login failed, do nothing
        }
}//GEN-LAST:event_loginButtonActionPerformed

    private void proxyConfigButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_proxyConfigButtonActionPerformed
        OptionsDisplayer.getDefault().open("General"); //NOI18N
    }//GEN-LAST:event_proxyConfigButtonActionPerformed

    private void projectWikiComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectWikiComboBoxActionPerformed
        panel.fireChangeEvent();
    }//GEN-LAST:event_projectWikiComboBoxActionPerformed

    private void projectNameTextFieldKeyPressed(KeyEvent evt) {//GEN-FIRST:event_projectNameTextFieldKeyPressed
        if (prjNameCheckMessage != null) {
            prjNameCheckMessage = null;
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_projectNameTextFieldKeyPressed

    @Messages({
        "NameAndLicenseWizardPanelGUI.prjNameLengthErrMsg=Project Name length must be between 2 and 20 characters.",
        "NameAndLicenseWizardPanelGUI.prjNameExistsErrMsg=Project with the same name already exists."
    })
    private void projectNameTextFieldFocusLost(FocusEvent evt) {//GEN-FIRST:event_projectNameTextFieldFocusLost
        if (panel.getServer() == null)
            return;
        if (getProjectName().length() < 2) {
            prjNameCheckMessage = NameAndLicenseWizardPanelGUI_prjNameLengthErrMsg();
            panel.fireChangeEvent();
            return;
        }
        prjNameCheckMessage = null;
        panel.fireChangeEvent();
        errorChecker.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String projectName = getProjectName();
                    List<ODSProject> projects = panel.getServer().findProjects(projectName);
                    for (ODSProject p : projects) {
                        if (projectName.equals(p.getName())) {
                            prjNameCheckMessage = Bundle.NameAndLicenseWizardPanelGUI_prjNameExistsErrMsg();
                        }
                    }
                } catch (ODSException ex) {
                    Logger.getLogger(NameAndLicenseWizardPanelGUI.class.getName()).log(Level.INFO, null, ex);
                }
                panel.fireChangeEvent();
            }
        });
    }//GEN-LAST:event_projectNameTextFieldFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup btnGrpPrivacy;
    private JRadioButton btnPrivacyOrganizationPrivate;
    private JRadioButton btnPrivacyPrivate;
    private JRadioButton btnPrivacyPublic;
    private JScrollPane jScrollPane1;
    private JLabel loggedInLabel;
    private JButton loginButton;
    private JLabel projectDescLabel;
    private JTextArea projectDescTextField;
    private JLabel projectNameLabel;
    private JTextField projectNameTextField;
    private JLabel projectPrivacyLabel;
    private JComboBox projectWikiComboBox;
    private JLabel projectWikiLabel;
    private JButton proxyConfigButton;
    private JComboBox teamCombo;
    private JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify () {
        super.addNotify();
        panel.fireChangeEvent();
    }

    public boolean valid () {

        String message = checkForErrors();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }

        message = checkForWarnings();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);
        }

        message = checkForInfos();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        }

        return true;

    }

    public void validateWizard () throws WizardValidationException {
    }

    // - not all errors are checked!
    @Messages({"NameAndLicenseWizardPanelGUI.prjTitleLengthErrMsg=Project Title length must be between 2 and 40 characters.",
        "NameAndLicenseWizardPanelGUI.prjDescLengthErrMsg=Project Description must to be shorter than 500 characters."})
    private String checkForErrors () {
        String prjName = getProjectName();

        if (prjNameCheckMessage != null) {
            return prjNameCheckMessage;
        } else if (prjName.length() == 1 || prjName.length() > 40) {
            return NameAndLicenseWizardPanelGUI_prjTitleLengthErrMsg();
        } else if (getProjectDesc().length() > 500) {
            return NameAndLicenseWizardPanelGUI_prjDescLengthErrMsg();
        }
        return null;
    }

    @Messages({"NameAndLicenseWizardPanelGUI.needLogin=You need to login to Team Server to create a Team project and repository.", "NameAndLicenseWizardPanelGUI.prjNameRequired=Project name is required.", "NameAndLicenseWizardPanelGUI.prjDescRequired=Project description is required."})
    private String checkForInfos () {
        if (panel.getServer() == null || !panel.getServer().isLoggedIn()) {
            return NameAndLicenseWizardPanelGUI_needLogin();
        } else if (getProjectName().trim().isEmpty()) {
            return NameAndLicenseWizardPanelGUI_prjNameRequired();
        } else if (getProjectDesc().trim().isEmpty()) {
            return NameAndLicenseWizardPanelGUI_prjDescRequired();
        }
        return null;
    }

    @Messages({"# Default messages", "NameAndLicenseWizardPanelGUI.defaultPrjName=My Project", "NameAndLicenseWizardPanelGUI.defaultPrjDesc=No description yet."})
    public void read (WizardDescriptor settings) {
        this.settings = settings;
        String prjName = (String) this.settings.getProperty(NewProjectWizardIterator.PROP_PRJ_NAME);
        List<NewProjectWizardIterator.SharedItem> items = panel.getInitialItems();
        if (prjName == null || prjName.trim().isEmpty()) {
            if (items.size() == 1) {
                setProjectName(items.get(0).getRoot().getName().toLowerCase());
            } else {
                setProjectName(NameAndLicenseWizardPanelGUI_defaultPrjName());
            }
        } else {
            setProjectName(prjName);
        }
        String prjDesc = (String) this.settings.getProperty(NewProjectWizardIterator.PROP_PRJ_DESC);
        if (prjDesc == null || prjDesc.trim().isEmpty()) {
            setProjectDescription(NameAndLicenseWizardPanelGUI_defaultPrjDesc());
        } else {
            setProjectDescription(prjDesc);
        }
        String prjWikiStyle = (String) this.settings.getProperty(NewProjectWizardIterator.PROP_PRJ_WIKI);
        setProjectWiki(prjWikiStyle);
        String prjPrivacy = (String) this.settings.getProperty(NewProjectWizardIterator.PROP_PRJ_ACCESSIBILITY);
        setProjectAccessibility(prjWikiStyle);
    }

    public void store (WizardDescriptor settings) {
        settings.putProperty(NewProjectWizardIterator.PROP_PRJ_NAME, getProjectName());
        settings.putProperty(NewProjectWizardIterator.PROP_PRJ_DESC, getProjectDesc());
        settings.putProperty(NewProjectWizardIterator.PROP_PRJ_ACCESSIBILITY, getProjectAccessibility());
        settings.putProperty(NewProjectWizardIterator.PROP_PRJ_WIKI, getProjectWikiStyle());
    }

    // ----------
    private void refreshUsername () {
        setChildrenEnabled(this, panel.getServer() != null);
        PasswordAuthentication passwdAuth = panel.getServer() == null ? null : panel.getServer().getPasswordAuthentication();
        if (passwdAuth != null) {
            setUsername(passwdAuth.getUserName());
            loginButton.setEnabled(false);
        } else {
            setUsername(null);
            loginButton.setEnabled(true);
        }
    }

    private void setUsername (String uName) {
        if (uName != null) {
            usernameLabel.setText(uName);
            usernameLabel.setForeground(new Color(0, 102, 0));
            usernameLabel.setEnabled(true);
        } else {
            usernameLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.notLoggedIn")); //NOI18N
            usernameLabel.setForeground(Color.BLACK);
            usernameLabel.setEnabled(false);
        }
    }

    // ----------
    private void setProjectName (String prjName) {
        projectNameTextField.setText(prjName);
    }

    private String getProjectName () {
        return projectNameTextField.getText();
    }

    private void setProjectDescription (String prjDesc) {
        projectDescTextField.setText(prjDesc);
    }

    private String getProjectDesc () {
        return projectDescTextField.getText();
    }

    private String getProjectWikiStyle () {
        return ((WikiMarkupLanguage) projectWikiComboBox.getSelectedItem()).name();
    }

    private String getProjectAccessibility () {
        return btnGrpPrivacy.getSelection().getActionCommand();
    }

    private String checkForWarnings () {
        return null; // for future
    }

    private void setProjectWiki (String prjWikiStyle) {
        WikiMarkupLanguage lang = null;
        try {
            if (prjWikiStyle != null && !prjWikiStyle.isEmpty()) {
                lang = WikiMarkupLanguage.valueOf(prjWikiStyle);
            }
        } catch (IllegalArgumentException ex) {}
        if (lang != null) {
            projectWikiComboBox.setSelectedItem(lang);
        }
    }

    private void setProjectAccessibility (String acc) {
        for (JRadioButton btn : new JRadioButton[] { 
            btnPrivacyOrganizationPrivate,
            btnPrivacyPrivate,
            btnPrivacyPublic
        }) {
            if (btn.getActionCommand().equals(acc)) {
                btn.setSelected(true);
                break;
            }
        }
    }
}

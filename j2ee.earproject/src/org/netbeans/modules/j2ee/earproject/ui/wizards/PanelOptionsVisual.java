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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class PanelOptionsVisual extends JPanel implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    
    private final PanelConfigureProject panel;
    private final DefaultComboBoxModel serversModel = new DefaultComboBoxModel();
    private J2eeVersionWarningPanel warningPanel;
    private boolean valid;
    
    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual(PanelConfigureProject panel, boolean importStyle) {
        panel.getProjectTypeFlag();
        initComponents();
        this.panel = panel;
        setJ2eeVersionWarningPanel();
        initServers(UserProjectSettings.getDefault().getLastUsedServer());
        // preselect the first item in the j2ee spec combo
        if (j2eeSpecComboBox.getModel().getSize() > 0) {
            j2eeSpecComboBox.setSelectedIndex(0);
        }
        // if this panel is used during import there are lots of things we don't
        // need to ask about -- hide them from the user.
        if (importStyle) {
            createEjbCheckBox.setSelected(!importStyle);
            createWARCheckBox.setSelected(!importStyle);
            createCarCheckBox.setSelected(!importStyle);
            createEjbCheckBox.setVisible(!importStyle);
            createWARCheckBox.setVisible(!importStyle);
            createCarCheckBox.setVisible(!importStyle);
            jTextFieldEjbModuleName.setVisible(!importStyle);
            jTextFieldWebAppName.setVisible(!importStyle);
            jTextFieldCarName.setVisible(!importStyle);
            mainClassLabel.setVisible(!importStyle);
            mainClassTextField.setVisible(!importStyle);
        } else {
            DocumentListener subProjectNameListener = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    projectNameChanged();
                }
                public void removeUpdate(DocumentEvent e) {
                    projectNameChanged();
                }
                public void changedUpdate(DocumentEvent e) {
                    projectNameChanged();
                }
            };
            jTextFieldEjbModuleName.getDocument().addDocumentListener(subProjectNameListener);
            jTextFieldWebAppName.getDocument().addDocumentListener(subProjectNameListener);
            jTextFieldCarName.getDocument().addDocumentListener(subProjectNameListener);
            this.mainClassTextField.getDocument().addDocumentListener( new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    mainClassChanged();
                }
            
                public void removeUpdate(DocumentEvent e) {
                    mainClassChanged();
                }
            
                public void changedUpdate(DocumentEvent e) {
                    mainClassChanged();
                }
            
            });
        }
        //j2eeSpecComboBox.setVisible(!importStyle);
        //j2eeSpecLabel.setVisible(!importStyle);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setAsMainCheckBox = new javax.swing.JCheckBox();
        createEjbCheckBox = new javax.swing.JCheckBox();
        jTextFieldEjbModuleName = new javax.swing.JTextField();
        createWARCheckBox = new javax.swing.JCheckBox();
        jTextFieldWebAppName = new javax.swing.JTextField();
        warningPlaceHolderPanel = new javax.swing.JPanel();
        createCarCheckBox = new javax.swing.JCheckBox();
        jTextFieldCarName = new javax.swing.JTextField();
        mainClassLabel = new javax.swing.JLabel();
        mainClassTextField = new javax.swing.JTextField();
        serverAndVersionPanel = new javax.swing.JPanel();
        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();
        addServerButton = new javax.swing.JButton();
        j2eeSpecLabel = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        setAsMainCheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SetAsMain_CheckBoxMnemonic").charAt(0));
        setAsMainCheckBox.setSelected(true);
        setAsMainCheckBox.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SetAsMain_CheckBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(setAsMainCheckBox, gridBagConstraints);
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_SetAsMain_A11YDesc")); // NOI18N

        createEjbCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createEjbCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_NEAP_CreateEjbModule")); // NOI18N
        createEjbCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createEjbCheckBox_action(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 11);
        add(createEjbCheckBox, gridBagConstraints);
        createEjbCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_CreateEJBModule")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(jTextFieldEjbModuleName, gridBagConstraints);
        jTextFieldEjbModuleName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_EjbModuleName")); // NOI18N
        jTextFieldEjbModuleName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_EjbModuleName")); // NOI18N

        createWARCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createWARCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_NEAP_CreatWebAppModule")); // NOI18N
        createWARCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createWebAppCheckBox_action(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 11);
        add(createWARCheckBox, gridBagConstraints);
        createWARCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_CreateWebModule")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(jTextFieldWebAppName, gridBagConstraints);
        jTextFieldWebAppName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_WebAppName")); // NOI18N
        jTextFieldWebAppName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_WebAppName")); // NOI18N

        warningPlaceHolderPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(warningPlaceHolderPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createCarCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_NEAP_CreateCarModule")); // NOI18N
        createCarCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createCarCheckBox_action(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 11);
        add(createCarCheckBox, gridBagConstraints);
        createCarCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_CreateAppClientModule")); // NOI18N

        jTextFieldCarName.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(jTextFieldCarName, gridBagConstraints);
        jTextFieldCarName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_AppClientName")); // NOI18N
        jTextFieldCarName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_AppClientName")); // NOI18N

        mainClassLabel.setLabelFor(mainClassTextField);
        org.openide.awt.Mnemonics.setLocalizedText(mainClassLabel, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_NWP1_MainClass_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(mainClassLabel, gridBagConstraints);

        mainClassTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 5);
        add(mainClassTextField, gridBagConstraints);
        mainClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCN_mainClassTextFiled")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCD_mainClassTextFiled")); // NOI18N

        serverAndVersionPanel.setLayout(new java.awt.GridBagLayout());

        serverInstanceLabel.setLabelFor(serverInstanceComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(serverInstanceLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_Server_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 11);
        serverAndVersionPanel.add(serverInstanceLabel, gridBagConstraints);

        serverInstanceComboBox.setModel(serversModel);
        serverInstanceComboBox.setPrototypeDisplayValue("The Gr8est Marvelous Nr. 1 Server");
        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 6);
        serverAndVersionPanel.add(serverInstanceComboBox, gridBagConstraints);
        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_NWP1_Server_ComboBox_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addServerButton, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_AddServer")); // NOI18N
        addServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        serverAndVersionPanel.add(addServerButton, gridBagConstraints);
        addServerButton.getAccessibleContext().setAccessibleName("Add Server");
        addServerButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_ManageButton")); // NOI18N

        j2eeSpecLabel.setLabelFor(j2eeSpecComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(j2eeSpecLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_J2EESpecLevel_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 11, 11);
        serverAndVersionPanel.add(j2eeSpecLabel, gridBagConstraints);

        j2eeSpecComboBox.setPrototypeDisplayValue("MMMMMMMMM" /* "Java EE 5" */);
        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 5);
        serverAndVersionPanel.add(j2eeSpecComboBox, gridBagConstraints);
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NPW1_J2EESpecLevel_A11YDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(serverAndVersionPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ASCN_ManageServers")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ASCD_ManageServers")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void addServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerButtonActionPerformed
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        String selectedServerInstanceID = null;
        if (serverInstanceWrapper != null) {
            selectedServerInstanceID = serverInstanceWrapper.getServerInstanceID();
        }
        String lastSelectedJ2eeSpecLevel = (String) j2eeSpecComboBox.getSelectedItem();
        String newServerInstanceID = ServerManager.showAddServerInstanceWizard();
        if (newServerInstanceID != null) {
            selectedServerInstanceID = newServerInstanceID;
            // clear the spec level selection
            lastSelectedJ2eeSpecLevel = null;
            j2eeSpecComboBox.setSelectedItem(null);
        }
        // refresh the list of servers
        initServers(selectedServerInstanceID);
        if (lastSelectedJ2eeSpecLevel != null) {
            j2eeSpecComboBox.setSelectedItem(lastSelectedJ2eeSpecLevel);
        }
}//GEN-LAST:event_addServerButtonActionPerformed
    
    private void createCarCheckBox_action(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createCarCheckBox_action
        jTextFieldCarName.setEnabled(createCarCheckBox.isSelected());
        mainClassTextField.setEnabled(createCarCheckBox.isSelected());
        this.panel.fireChangeEvent();
    }//GEN-LAST:event_createCarCheckBox_action
    
    private void createWebAppCheckBox_action(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createWebAppCheckBox_action
        jTextFieldWebAppName.setEnabled(createWARCheckBox.isSelected());
        this.panel.fireChangeEvent();
    }//GEN-LAST:event_createWebAppCheckBox_action
    
    private void createEjbCheckBox_action(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createEjbCheckBox_action
        jTextFieldEjbModuleName.setEnabled(createEjbCheckBox.isSelected());
        this.panel.fireChangeEvent();
    }//GEN-LAST:event_createEjbCheckBox_action
    
    private void serverInstanceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInstanceComboBoxActionPerformed
        String prevSelectedItem = (String) j2eeSpecComboBox.getSelectedItem();
        // update the j2ee spec list according to the selected server
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceWrapper.getServerInstanceID());
            Set supportedVersions = j2eePlatform.getSupportedSpecVersions(J2eeModule.EAR);
            j2eeSpecComboBox.removeAllItems();
            if (supportedVersions.contains(J2eeModule.JAVA_EE_5)) {
                j2eeSpecComboBox.addItem(EarProjectProperties.JAVA_EE_SPEC_50_LABEL);
            }
            if (supportedVersions.contains(J2eeModule.J2EE_14)) {
                j2eeSpecComboBox.addItem(EarProjectProperties.J2EE_SPEC_14_LABEL);
            }
            if (prevSelectedItem != null) {
                j2eeSpecComboBox.setSelectedItem(prevSelectedItem);
            }
            boolean carSupported = j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.CLIENT);
            createCarCheckBox.setEnabled(carSupported);
            jTextFieldCarName.setEnabled(carSupported && createCarCheckBox.isSelected());
            mainClassLabel.setEnabled(carSupported);
            mainClassTextField.setEnabled(carSupported && createCarCheckBox.isSelected());
        } else {
            j2eeSpecComboBox.removeAllItems();
        }
        // revalidate the form
        panel.fireChangeEvent();
    }//GEN-LAST:event_serverInstanceComboBoxActionPerformed
    
    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        setJ2eeVersionWarningPanel();
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed

    public void propertyChange(PropertyChangeEvent event) {
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(event.getPropertyName())) {
            String newProjectName = (String) event.getNewValue();
            if (newProjectName.trim().equals("")) {
                return;
            }
            this.jTextFieldEjbModuleName.setText(MessageFormat.format(
                    NbBundle.getMessage(PanelOptionsVisual.class,"TXT_EJBProjectName"), new Object[] {newProjectName}));
            this.jTextFieldWebAppName.setText(MessageFormat.format(
                    NbBundle.getMessage(PanelOptionsVisual.class,"TXT_WebAppProjectName"), new Object[] {newProjectName}));
            this.jTextFieldCarName.setText(MessageFormat.format(
                    NbBundle.getMessage(PanelOptionsVisual.class,"TXT_AppClientProjectName"), new Object[] {newProjectName}));
            newProjectName = getPackageName(newProjectName);
            if (!Utilities.isJavaIdentifier(newProjectName)) {
                newProjectName = NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PackageNameSuffix", newProjectName);
            }
            this.mainClassTextField.setText(MessageFormat.format(
                    NbBundle.getMessage(PanelOptionsVisual.class,"TXT_ClassName"), new Object[] {newProjectName}));
        }
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (getSelectedServer() == null) {
            setErrorMessage("MSG_NoServer", wizardDescriptor); // NOI18N
            return false;
        }
        
        if (createWARCheckBox.isSelected()) {
            String warName = jTextFieldWebAppName.getText();
            if (warName.length() < 1) {
                setErrorMessage("MSG_NoWARName", wizardDescriptor); // NOI18N
                return false;
            }
            if (!warName.endsWith("-war")) { // NOI18N
                // this is really just a warning
                setErrorMessage("MSG_WARNameNotBlueprints", wizardDescriptor); // NOI18N
            }
        }
        
        if (createEjbCheckBox.isSelected()) {
            String jarName = jTextFieldEjbModuleName.getText();
            if (jarName.length() < 1) {
                setErrorMessage("MSG_NoJARName", wizardDescriptor); // NOI18N
                return false;
            }
            if (!jarName.endsWith("-ejb")) { //NOI18N
                // this is really just a warning
                setErrorMessage("MSG_JARNameNotBlueprints", wizardDescriptor); // NOI18N
            }
        }
        
        if (createCarCheckBox.isSelected()) {
            String jarName = jTextFieldCarName.getText();
            if (jarName.length() < 1) {
                setErrorMessage("MSG_NoCARName", wizardDescriptor); // NOI18N
                return false;
            }
            if (!valid) {
                setErrorMessage("ERROR_IllegalMainClassName", wizardDescriptor); // NOI18N
                return this.valid;
            }
            if (!jarName.endsWith("-app-client")) { //NOI18N
                // this is really just a warning
                setErrorMessage("MSG_CARNameNotBlueprints", wizardDescriptor); // NOI18N
            }
        }
        
        // check whether an application client is supported by the target server
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceWrapper.getServerInstanceID());
            if (!j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.CLIENT)) {
                // show warning/info
                setErrorMessage("MSG_CARIsNotSupported", wizardDescriptor);
            }
        }
        
        String specVer = getSelectedJ2eeSpec();
        if (null == specVer || specVer.equals(J2eeModule.J2EE_13)) {
            setErrorMessage("MSG_UnsupportedSpec", wizardDescriptor); // NOI18N
            return false;
        }
        return true;
    }
    
    private static void setErrorMessage(
            final String errMsgKey, final WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(PanelOptionsVisual.class, errMsgKey));
    }
    
    void store(WizardDescriptor d) {
        d.putProperty(WizardProperties.SET_AS_MAIN, setAsMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE );
        d.putProperty(WizardProperties.SERVER_INSTANCE_ID, getSelectedServer());
        d.putProperty(WizardProperties.J2EE_LEVEL, getSelectedJ2eeSpec());
        //        d.putProperty(WizardProperties.CONTEXT_PATH, jTextFieldContextPath.getText().trim());
        d.putProperty(WizardProperties.CREATE_WAR, createWARCheckBox.isSelected() ? Boolean.TRUE: Boolean.FALSE);
        d.putProperty(WizardProperties.CREATE_JAR, createEjbCheckBox.isSelected() ? Boolean.TRUE: Boolean.FALSE);
        d.putProperty(WizardProperties.CREATE_CAR, createCarCheckBox.isEnabled() && createCarCheckBox.isSelected() ? Boolean.TRUE: Boolean.FALSE);
        d.putProperty(WizardProperties.WAR_NAME,  jTextFieldWebAppName.getText());
        d.putProperty(WizardProperties.JAR_NAME, jTextFieldEjbModuleName.getText());
        d.putProperty(WizardProperties.CAR_NAME, jTextFieldCarName.getText());
        d.putProperty(WizardProperties.MAIN_CLASS, mainClassTextField.getText().trim()); // NOI18N
        if (warningPanel != null && warningPanel.getWarningType() != null && warningPanel.getDowngradeAllowed()) {
            d.putProperty(WizardProperties.JAVA_PLATFORM, warningPanel.getSuggestedJavaPlatformName());
            String j2ee = getSelectedJ2eeSpec();
            if (j2ee != null) {
                String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
                UserProjectSettings fls = UserProjectSettings.getDefault();
                String srcLevel = "1.6"; // NOI18N
                if ((warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_14)
                        || warningType.equals(J2eeVersionWarningPanel.WARN_SET_JDK_14))
                        && fls.isAgreedSetSourceLevel14()) {
                    srcLevel = "1.4"; // NOI18N
                } else if ((warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_15)
                        || warningType.equals(J2eeVersionWarningPanel.WARN_SET_JDK_15))
                        && fls.isAgreedSetSourceLevel15()) {
                    srcLevel = "1.5"; // NOI18N
                }
                d.putProperty(WizardProperties.SOURCE_LEVEL, srcLevel);
            }
        } else {
            d.putProperty(WizardProperties.SOURCE_LEVEL, null);
        }
    }
    
    void read(WizardDescriptor d) {
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
    private javax.swing.JCheckBox createCarCheckBox;
    private javax.swing.JCheckBox createEjbCheckBox;
    private javax.swing.JCheckBox createWARCheckBox;
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JLabel j2eeSpecLabel;
    private javax.swing.JTextField jTextFieldCarName;
    private javax.swing.JTextField jTextFieldEjbModuleName;
    private javax.swing.JTextField jTextFieldWebAppName;
    private javax.swing.JLabel mainClassLabel;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JPanel serverAndVersionPanel;
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JPanel warningPlaceHolderPanel;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Init servers model
     * @param selectedServerInstanceID preselected instance or null if non is preselected
     */
    private void initServers(String selectedServerInstanceID) {
        // init the list of server instances
        serversModel.removeAllElements();
        Set<ServerInstanceWrapper> servers = new TreeSet<ServerInstanceWrapper>();
        ServerInstanceWrapper selectedItem = null;
        boolean sjasFound = false;
        for (String serverInstanceID : Deployment.getDefault().getServerInstanceIDs()) {
            String displayName = Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID);
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            if (displayName != null && j2eePlatform != null && j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.EAR)) {
                ServerInstanceWrapper serverWrapper = new ServerInstanceWrapper(serverInstanceID, displayName);
                // decide whether this server should be preselected
                if (selectedItem == null || !sjasFound) {
                    if (selectedServerInstanceID != null) {
                        if (selectedServerInstanceID.equals(serverInstanceID)) {
                            selectedItem = serverWrapper;
                        }
                    } else {
                        // preselect the best server ;)
                        String shortName = Deployment.getDefault().getServerID(serverInstanceID);
                        if ("J2EE".equals(shortName)) { // NOI18N
                            selectedItem = serverWrapper;
                            sjasFound = true;
                        }
                        else
                        if ("JBoss4".equals(shortName)) { // NOI18N
                            selectedItem = serverWrapper;
                        }
                    }
                }
                servers.add(serverWrapper);
            }
        }
        for (ServerInstanceWrapper item : servers) {
            serversModel.addElement(item);
        }
        if (selectedItem != null) {
            // set the preselected item
            serversModel.setSelectedItem(selectedItem);
        } else if (serversModel.getSize() > 0) {
            // set the first item
            serversModel.setSelectedItem(serversModel.getElementAt(0));
        }
    }
    
    private String getSelectedJ2eeSpec() {
        Object item = j2eeSpecComboBox.getSelectedItem();
        return item == null ? null
                : item.equals(EarProjectProperties.JAVA_EE_SPEC_50_LABEL) ? J2eeModule.JAVA_EE_5 :
                    ( item.equals(EarProjectProperties.J2EE_SPEC_14_LABEL) ? J2eeModule.J2EE_14 : J2eeModule.J2EE_13);
    }
    
    private String getSelectedServer() {
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper == null) {
            return null;
        }
        return serverInstanceWrapper.getServerInstanceID();
    }
    
    private void setJ2eeVersionWarningPanel() {
        String j2ee = getSelectedJ2eeSpec();
        if (j2ee == null) {
            return;
        }
        String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
        if (warningType == null && warningPanel == null) {
            return;
        }
        if (warningPanel == null) {
            warningPanel = new J2eeVersionWarningPanel(warningType);
            warningPlaceHolderPanel.add(warningPanel, java.awt.BorderLayout.CENTER);
        }
        warningPanel.setWarningType(warningType);
    }
    
    private void mainClassChanged() {
        String mainClassName = this.mainClassTextField.getText().trim();
        StringTokenizer tk = new StringTokenizer(mainClassName, "."); //NOI18N
        boolean valid = tk.countTokens() > 0;
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
                valid = false;
                break;
            }
        }
        this.valid = valid;
        this.panel.fireChangeEvent();
    }
    
    private void projectNameChanged() {
        this.panel.fireChangeEvent();
    }
    
    private  String getPackageName(String displayName) {
        StringBuffer builder = new StringBuffer();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if ((!firstLetter && Character.isJavaIdentifierPart(c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                builder.append(c);
            }
        }
        return builder.length() == 0 ? NbBundle.getMessage(PanelOptionsVisual.class,"TXT_DefaultPackageName") : builder.toString();
    }
    
    /**
     * Server instance wrapper represents server instances in the servers combobox.
     * @author sherold
     */
    private static class ServerInstanceWrapper implements Comparable {

        private final String serverInstanceID;
        private final String displayName;

        ServerInstanceWrapper(String serverInstanceID, String displayName) {
            this.serverInstanceID = serverInstanceID;
            this.displayName = displayName;
        }

        public String getServerInstanceID() {
            return serverInstanceID;
        }

        public String toString() {
            return displayName;
        }

        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
        
    }
    
}


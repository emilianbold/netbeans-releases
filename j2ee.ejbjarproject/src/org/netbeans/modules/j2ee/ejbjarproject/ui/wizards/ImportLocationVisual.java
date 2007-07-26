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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProvider;
import org.netbeans.modules.j2ee.ejbjarproject.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author  pb97924, Martin Adamek
 */
public class ImportLocationVisual extends JPanel /*implements DocumentListener */{

    private static final String J2EE_SPEC_15_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_15");
    private static final String J2EE_SPEC_14_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_14");
    private static final String J2EE_SPEC_13_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_13");
    
    private final DefaultComboBoxModel serversModel = new DefaultComboBoxModel();
    private final ImportLocation panel;
    private final DocumentListener documentListener;
    private List earProjects;
    private BigDecimal ejbJarXmlVersion;
    private WizardDescriptor wizardDescriptor;
    private J2eeVersionWarningPanel warningPanel;
        
    /** Creates new form TestPanel */
    public ImportLocationVisual (ImportLocation panel) {
        this.panel = panel;
        initComponents ();
        setJ2eeVersionWarningPanel();
        initServers(FoldersListSettings.getDefault().getLastUsedServer());
        // preselect the first item in the j2ee spec combo
        if (j2eeSpecComboBox.getModel().getSize() > 0) {
            j2eeSpecComboBox.setSelectedIndex(0);
        }
        initEnterpriseApplications();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N
        setName(NbBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_IW_ImportTitle")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(ImportLocationVisual.class, "TXT_ImportEJBModule")); //NOI18N
        this.projectName.setText("");
        
        documentListener = new DocumentListener() {           
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        this.projectName.getDocument().addDocumentListener(documentListener);
        this.projectFolder.getDocument().addDocumentListener(documentListener);
        this.projectLocation.getDocument().addDocumentListener(documentListener);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelSrcLocationDesc = new javax.swing.JLabel();
        jLabelSrcLocation = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        browseProjectLocation = new javax.swing.JButton();
        jLabelPrjLocationDesc = new javax.swing.JLabel();
        jLabelPrjName = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        jLabelPrjLocation = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        browseProjectFolder = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();
        addToAppLabel = new javax.swing.JLabel();
        addToAppComboBox = new javax.swing.JComboBox();
        warningPlaceHolderPanel = new javax.swing.JPanel();
        addServerButton = new javax.swing.JButton();

        setNextFocusableComponent(projectLocation);
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrcDesc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelSrcLocationDesc, gridBagConstraints);

        jLabelSrcLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSrcLocation.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrc_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelSrcLocation, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle"); // NOI18N
        jLabelSrcLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_IW_Location_A11Desc")); // NOI18N

        projectLocation.setNextFocusableComponent(browseProjectLocation);
        projectLocation.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                projectLocationFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectLocation, gridBagConstraints);
        projectLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocation_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseProjectLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button_w")); // NOI18N
        browseProjectLocation.setNextFocusableComponent(projectName);
        browseProjectLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(browseProjectLocation, gridBagConstraints);
        browseProjectLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocationBrowse_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationPrjDesc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelPrjLocationDesc, gridBagConstraints);

        jLabelPrjName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjName.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjName, org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjName, gridBagConstraints);
        jLabelPrjName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_IW_ProjectName_A11Desc")); // NOI18N

        projectName.setNextFocusableComponent(projectFolder);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectName, gridBagConstraints);
        projectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc")); // NOI18N

        jLabelPrjLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjLocation.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjLocation, gridBagConstraints);
        jLabelPrjLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_IW_ProjectFolder_A11Desc")); // NOI18N

        projectFolder.setNextFocusableComponent(browseProjectFolder);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectFolder, gridBagConstraints);
        projectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseProjectFolder, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button")); // NOI18N
        browseProjectFolder.setNextFocusableComponent(addToAppComboBox);
        browseProjectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(browseProjectFolder, gridBagConstraints);
        browseProjectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jSeparator1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, bundle.getString("LBL_IW_SetAsMainProject_CheckBox")); // NOI18N
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(jCheckBox1, gridBagConstraints);
        jCheckBox1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_IW_SetAsMainProject_A11YDesc")); // NOI18N

        serverInstanceLabel.setLabelFor(serverInstanceComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(serverInstanceLabel, org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_Server")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(serverInstanceLabel, gridBagConstraints);

        serverInstanceComboBox.setModel(serversModel);
        serverInstanceComboBox.setNextFocusableComponent(addServerButton);
        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(serverInstanceComboBox, gridBagConstraints);
        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_IW_SelectServerInstance_A11YDesc")); // NOI18N

        jLabel7.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_SelectJ2EEVersion_LabelMnemonic").charAt(0));
        jLabel7.setLabelFor(j2eeSpecComboBox);
        jLabel7.setText(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_J2EESpecLevel_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(jLabel7, gridBagConstraints);

        j2eeSpecComboBox.setPrototypeDisplayValue("MMMMMMMMM" /* "Java EE 5" */);
        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(j2eeSpecComboBox, gridBagConstraints);
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_IW_SelectJ2EEVersion_A11YDesc")); // NOI18N

        addToAppLabel.setLabelFor(addToAppComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(addToAppLabel, bundle.getString("LBL_NWP1_AddToEApp_CheckBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(addToAppLabel, gridBagConstraints);

        addToAppComboBox.setNextFocusableComponent(serverInstanceComboBox);
        addToAppComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToAppComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanel1.add(addToAppComboBox, gridBagConstraints);

        warningPlaceHolderPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(warningPlaceHolderPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addServerButton, org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_AddServer")); // NOI18N
        addServerButton.setNextFocusableComponent(j2eeSpecComboBox);
        addServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        jPanel1.add(addServerButton, gridBagConstraints);
        addServerButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ASCN_AddServer")); // NOI18N
        addServerButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ASCD_AddServer")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        getAccessibleContext().setAccessibleName(bundle.getString("ACS_LBL_IW_ImportLocationVisual_A11Name")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_IW_ImportLocationVisual_A11Desc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void projectLocationFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_projectLocationFocusLost
        File f = new File(projectLocation.getText().trim());
        if (f.isDirectory()) {
            updateJ2EEVersion();
        }
    }//GEN-LAST:event_projectLocationFocusLost

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

    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        panel.stateChanged(null);
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed

    private void addToAppComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToAppComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addToAppComboBoxActionPerformed

    private void serverInstanceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInstanceComboBoxActionPerformed
        String prevSelectedItem = (String) j2eeSpecComboBox.getSelectedItem();
        // update the j2ee spec list according to the selected server
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceWrapper.getServerInstanceID());
            Set supportedVersions = j2eePlatform.getSupportedSpecVersions(J2eeModule.EJB);
            j2eeSpecComboBox.removeAllItems();
            if (supportedVersions.contains(J2eeModule.JAVA_EE_5)) {
                j2eeSpecComboBox.addItem(J2EE_SPEC_15_LABEL);
            }
            if (supportedVersions.contains(J2eeModule.J2EE_14)) {
                j2eeSpecComboBox.addItem(J2EE_SPEC_14_LABEL);
            }
            if (prevSelectedItem != null) {
                j2eeSpecComboBox.setSelectedItem(prevSelectedItem);
            }
        } else {
            j2eeSpecComboBox.removeAllItems();
        }
        // revalidate the form
        panel.fireChangeEvent();
    }//GEN-LAST:event_serverInstanceComboBoxActionPerformed

    private void browseProjectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectFolderActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (projectFolder.getText().length() > 0 && getProjectFolder().exists()) {
            chooser.setSelectedFile(getProjectFolder());
        } else if (projectLocation.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        chooser.setDialogTitle(NbBundle.getMessage(ImportLocationVisual.class, "LBL_SelectNewLocation")); // NOI18N
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            projectFolder.setText(projectDir.getAbsolutePath());
        }                    
    }//GEN-LAST:event_browseProjectFolderActionPerformed

    private void browseProjectLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (projectLocation.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            // honor the contract in issue 58987
            File currentDirectory = null;
            FileObject existingSourcesFO = Templates.getExistingSourcesFolder(wizardDescriptor);
            if (existingSourcesFO != null) {
                File existingSourcesFile = FileUtil.toFile(existingSourcesFO);
                if (existingSourcesFile != null && existingSourcesFile.isDirectory()) {
                    currentDirectory = existingSourcesFile;
                }
            }
            if (currentDirectory != null) {
                chooser.setCurrentDirectory(currentDirectory);
            } else {
                chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
            }
        }
        
        chooser.setDialogTitle(NbBundle.getMessage(ImportLocationVisual.class, "LBL_SelectExistingLocation")); // NOI18N
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            projectLocation.setText(FileUtil.normalizeFile(chooser.getSelectedFile()).getAbsolutePath());
            updateJ2EEVersion();
            updateProjectName();
            updateProjectFolder();
            panel.stateChanged(null);
        }
    }//GEN-LAST:event_browseProjectLocationActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
    private javax.swing.JComboBox addToAppComboBox;
    private javax.swing.JLabel addToAppLabel;
    private javax.swing.JButton browseProjectFolder;
    private javax.swing.JButton browseProjectLocation;
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelPrjLocation;
    private javax.swing.JLabel jLabelPrjLocationDesc;
    private javax.swing.JLabel jLabelPrjName;
    private javax.swing.JLabel jLabelSrcLocation;
    private javax.swing.JLabel jLabelSrcLocationDesc;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JTextField projectFolder;
    public javax.swing.JTextField projectLocation;
    public javax.swing.JTextField projectName;
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    private javax.swing.JPanel warningPlaceHolderPanel;
    // End of variables declaration//GEN-END:variables
    
    private void setJ2eeVersionWarning() {
        String errorMessage;
        String selectedItem = (String)j2eeSpecComboBox.getSelectedItem();
        
        if (J2EE_SPEC_14_LABEL.equals(selectedItem) && new BigDecimal(EjbJar.VERSION_2_0).equals(ejbJarXmlVersion)) {
            errorMessage = NbBundle.getMessage(ImportLocationVisual.class, "MSG_EjbJarXMLNotSupported");
        } else {
            errorMessage = null;
        }
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", errorMessage); //NOI18N
        }
        
        setJ2eeVersionWarningPanel();
    }
    
    private boolean ignoreEvent = false;
    
    private String lastComputedPrjName = null;
    private String computeProjectName() {
        String cPrjName = null;
        FileObject fo = FileUtil.toFileObject(getProjectLocation());
        if (fo != null) {
            cPrjName = fo.getName();
        }
        return cPrjName;
    }
    
    private String lastComputedPrjFolder = null;
    private String computeProjectFolder() {
        String cPrjFolder = null;
        if (isValidProjectLocation()) {
            cPrjFolder = getProjectLocation().getAbsolutePath();
        }
        return cPrjFolder;
    }
    
    private void update(DocumentEvent e) {
        if (ignoreEvent) {
            // side-effect of changes done in this handler
            return;
        }

        // start ignoring events
        ignoreEvent = true;

        if (projectLocation.getDocument() == e.getDocument()) {
            updateProjectName();
            updateProjectFolder();
        }
        
        // stop ignoring events
        ignoreEvent = false;
        
        panel.stateChanged(null);
    }
    
    private void updateProjectName() {
        String prjName = computeProjectName();
        if ((lastComputedPrjName != null) && (!lastComputedPrjName.equals(projectName.getText().trim()))) {
            return;
        }
        lastComputedPrjName = prjName;
        if (prjName != null) {
            projectName.setText(prjName);
        }
    }
    
    private void updateProjectFolder() {
        String prjFolder = computeProjectFolder();
        if ((lastComputedPrjFolder != null) && (!lastComputedPrjFolder.equals(projectFolder.getText().trim()))) {
            return;
        }
        lastComputedPrjFolder = prjFolder;
        if (prjFolder != null) {
            projectFolder.setText(prjFolder);
        } else {
            projectFolder.setText(""); // NOI18N
        }
    }
    
    private void updateJ2EEVersion() {
        File f = new File(projectLocation.getText().trim());
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
        if (fo != null) {
            FileObject configFilesPath = FileSearchUtility.guessConfigFilesPath(fo);
            if (configFilesPath != null) {
                FileObject ejbJarXML = configFilesPath.getFileObject(EjbJarProvider.FILE_DD);
                checkEjbJarXmlJ2eeVersion(ejbJarXML);
            } else {
                // suppose highest
                j2eeSpecComboBox.setSelectedItem(J2EE_SPEC_15_LABEL);
            }
        }
    }
    
    private boolean isValidProjectLocation() {
        return (getProjectLocation().exists() && getProjectLocation().isDirectory() &&
                projectLocation.getText().length() > 0 && (!projectLocation.getText().endsWith(":"))); // NOI18N
    }
    
    public boolean valid(WizardDescriptor wizardDescriptor) {
        File prjDir = new File(projectLocation.getText().trim());
//        File prjFolder = new File(projectFolder.getText().trim());
        String prjName = projectName.getText().trim();
        
        if (getSelectedServerInstanceID() == null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class,"MSG_NoServer")); //NOI18N
            return false;
        }
            
        if (!prjDir.isDirectory()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class,"MSG_ProvideExistingSourcesLocation")); //NOI18N
            return false; //Existing sources location not specified
        }
        
        //Do we need this check?
        //            if (!prjFolder.isDirectory()) {
        //                wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class,"MSG_ProjectFolderDoesNotExists")); //NOI18N
        //                return false; //Project folder not specified
        //            }
        
        // we will leave it up to user if he provides project folder with sources or not
        // if (FileSearchUtility.guessJavaRoots(FileUtil.toFileObject(FileUtil.normalizeFile(prjDir))) == null) {
        //     wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class,"MSG_NoEjbJarModule")); //NOI18N
        //     return false; // No java project location
        // }
        
        if (prjName == null || prjName.length() == 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class,"MSG_ProvideProjectName")); //NOI18N
            return false; //Project name not specified
        }
        
        String result = checkValidity (this.projectName.getText(), this.projectFolder.getText());
        if (result != null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", result); //NOI18N
            return false;
        }

        wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); //NOI18N
        
        setJ2eeVersionWarning();
        
        return true;
    }

    static String checkValidity (final String projectName, final String projectLocation) {
        File projLoc = new File (projectLocation).getAbsoluteFile();
        
        if (PanelProjectLocationVisual.getCanonicalFile(projLoc) == null) {
            return NbBundle.getMessage(ImportLocationVisual.class, "MSG_IllegalProjectLocation");
        }
        
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(ImportLocationVisual.class,"MSG_ProjectFolderReadOnly");
        }

        File destFolder = FileUtil.normalizeFile(new File( projectLocation ));
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i=0; i< kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_NetBeansProject");
                }
                else if ("build".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_BuildFolder");
                }
                else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_DistFolder");
                }
                else if ("build.xml".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_BuildXML");
                }
                else if ("manifest.mf".equals(childName)) { //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_Manifest");
                }
                if (file != null) {
                    String format = NbBundle.getMessage (ImportLocationVisual.class,"MSG_ProjectFolderInvalid");
                    return MessageFormat.format(format, new Object[] {file});
                }
            }
        }

        if (destFolder.isDirectory()) {
            FileObject destFO = FileUtil.toFileObject(destFolder);
            assert destFO != null : "No FileObject for " + destFolder;
            boolean clear;
            try {
                clear = ProjectManager.getDefault().findProject(destFO) == null;
            } catch (IOException e) {
                clear = false;
            }
            if (!clear) {
                return NbBundle.getMessage(ImportLocationVisual.class, "MSG_ProjectFolderHasDeletedProject");
            }
        }
        return null;
    }        

    void read (WizardDescriptor d) {
        wizardDescriptor = d;
    }
    
    void store( WizardDescriptor d ) {
        String name = projectName.getText().trim();
        String moduleLoc = projectLocation.getText().trim();

        if (name.equals("") || moduleLoc.equals("")) {
            return;
        }
        
        d.putProperty(WizardProperties.PROJECT_DIR, new File(projectFolder.getText().trim()));
        File moduleLocFile =  new File(moduleLoc);
        d.putProperty(WizardProperties.SOURCE_ROOT, moduleLocFile);
        d.putProperty(WizardProperties.NAME, name);
        d.putProperty(WizardProperties.JAVA_ROOT, FileSearchUtility.guessJavaRootsAsFiles(FileUtil.toFileObject(FileUtil.normalizeFile(moduleLocFile))));
        d.putProperty(WizardProperties.SERVER_INSTANCE_ID, getSelectedServerInstanceID());
        d.putProperty(WizardProperties.J2EE_LEVEL, getSelectedJ2eeSpec());
        d.putProperty(WizardProperties.EAR_APPLICATION, getSelectedEarApplication());
        if (warningPanel != null && warningPanel.getDowngradeAllowed()) {
            d.putProperty(WizardProperties.JAVA_PLATFORM, warningPanel.getSuggestedJavaPlatformName());
            
            String j2ee = getSelectedJ2eeSpec();
            if (j2ee != null) {
                String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
                FoldersListSettings fls = FoldersListSettings.getDefault();
                String srcLevel = "1.6"; //NOI18N
                if (warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_14) && fls.isAgreedSetSourceLevel14())
                    srcLevel = "1.4"; //NOI18N
                else if (warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_15) && fls.isAgreedSetSourceLevel15())
                    srcLevel = "1.5"; //NOI18N
                
                d.putProperty(WizardProperties.SOURCE_LEVEL, srcLevel);
            }            
        } else
            d.putProperty(WizardProperties.SOURCE_LEVEL, null);
        
        // TODO: ma154696: add also search for test roots
    }
    
    //extra finish dialog
    private Dialog dialog;
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        File dirF = new File(projectFolder.getText());
        JButton ok = new JButton(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_Buildfile_OK")); //NOI18N
        ok.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_IW_BuildFileDialog_OKButton_LabelMnemonic")); //NOI18N
        ok.setMnemonic(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_BuildFileDialog_OK_LabelMnemonic").charAt(0)); //NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_Buildfile_Cancel")); //NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_IW_BuildFileDialog_CancelButton_LabelMnemonic")); //NOI18N
        cancel.setMnemonic(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_BuildFileDialog_Cancel_LabelMnemonic").charAt(0)); //NOI18N
        
        final ImportBuildfile ibf = new ImportBuildfile(dirF.getAbsolutePath(), ok);
        if ((new File(dirF, GeneratedFilesHelper.BUILD_XML_PATH)).exists()) {
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Object src = event.getSource();
                    if (src instanceof JButton) {
                        String name = ((JButton) src).getText();
                        if (name.equals(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_Buildfile_OK"))) { //NOI18N
                            closeDialog();
                        } else if (name.equals(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_Buildfile_Cancel"))) { //NOI18N
                            NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_Buildfile_CancelConfirmation"), NotifyDescriptor.YES_NO_OPTION); //NOI18N
                            Object ret = DialogDisplayer.getDefault().notify(ndesc);
                            if (ret == NotifyDescriptor.YES_OPTION) {
                                closeDialog();
                            }
                        }
                    }
                }
            };
            
            DialogDescriptor descriptor = new DialogDescriptor(
                    ibf,
                    NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_BuildfileTitle"), //NOI18N
                    true,
                    new Object[] {ok, cancel},
                    DialogDescriptor.OK_OPTION,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            actionListener
                            );
                    
                    dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                    dialog.setVisible(true);
        } else {
            return;
        }
    }

    private void closeDialog() {
        dialog.dispose();
    }
    
    private boolean isParentOf(FileObject dir, FileObject[] fos) {
        boolean result = true;
        if (fos != null) {
            for (int i = 0; i < fos.length; i++) {
                result = FileUtil.isParentOf(dir, fos[i]);
                if (!result) {
                    return result;
                }
            }
        }
        return result;
    }
    
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
            if (displayName != null && j2eePlatform != null && j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
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
    
    public String getSelectedServerInstanceID() {
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper == null) {
            return null;
        }
        return serverInstanceWrapper.getServerInstanceID();
    }
    
    public String getSelectedJ2eeSpec() {
        Object item = j2eeSpecComboBox.getSelectedItem();
        return item == null ? null
                : item.equals(J2EE_SPEC_14_LABEL) ? J2eeModule.J2EE_14
                : item.equals(J2EE_SPEC_15_LABEL) ? J2eeModule.JAVA_EE_5 : J2eeModule.J2EE_13;
    }
    
    private File getAsFile(String filename) {
        return FileUtil.normalizeFile(new File(filename));
    }

    public File getProjectLocation() {
        return getAsFile(projectLocation.getText());
    }

    public File getProjectFolder() {
        return getAsFile(projectFolder.getText());
    }

    public String getProjectName() {
        return projectName.getText();
    }

    private Project getSelectedEarApplication() {
        int idx = addToAppComboBox.getSelectedIndex();
        return (idx <= 0) ? null : (Project) earProjects.get(idx - 1);
    }
    
    private void initEnterpriseApplications() {
        addToAppComboBox.addItem(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_AddToEApp_None"));
        addToAppComboBox.setSelectedIndex(0);
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        earProjects = new ArrayList();
        for (int i = 0; i < allProjects.length; i++) {
            J2eeApplicationProvider j2eeAppProvider = allProjects[i].getLookup().lookup(J2eeApplicationProvider.class);
	    if (j2eeAppProvider != null) {
		J2eeApplication j2eeApplication = (J2eeApplication) j2eeAppProvider.getJ2eeModule();
		ProjectInformation projectInfo = ProjectUtils.getInformation(allProjects[i]);
		if (j2eeApplication != null) {
		    earProjects.add(projectInfo.getProject());
		    addToAppComboBox.addItem(projectInfo.getDisplayName());
		}
	    }
        }
        if (earProjects.size() <= 0) {
            addToAppComboBox.setEnabled(false);
        }
    }
    
    private BigDecimal getEjbJarXmlVersion(FileObject ejbJarXml) throws IOException {
        if (ejbJarXml != null) {
            EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ejbJarXml);
            if (ejbJar != null) {
                return ejbJar.getVersion();
            }
        }
        return null;
    }

    private void checkEjbJarXmlJ2eeVersion(FileObject ejbJarXml) {
        try {
            BigDecimal version = getEjbJarXmlVersion(ejbJarXml);
            ejbJarXmlVersion = version;
            if (version == null) {
                return;
            }
            
            if(new BigDecimal(EjbJar.VERSION_2_0).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(J2EE_SPEC_13_LABEL);
            } else if(new BigDecimal(EjbJar.VERSION_2_1).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(J2EE_SPEC_14_LABEL);
            }
        } catch (IOException e) {
            String message = NbBundle.getMessage(ImportLocationVisual.class, "MSG_EjbJarXmlCorrupted"); // NOI18N
            Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(e, message));
        }
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
            warningPanel.setWarningType(warningType);
        } else {
            warningPanel.setWarningType(warningType);
        }
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

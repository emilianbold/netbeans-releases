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

package org.netbeans.modules.web.project.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class PanelOptionsVisual extends javax.swing.JPanel {
    
    private PanelConfigureProject panel;
    private String sourceStructure;
    private boolean contextModified = false;
    private final DefaultComboBoxModel serversModel = new DefaultComboBoxModel();
    private String currentLibrariesLocation;
    private String projectLocation;
    
    private J2eeVersionWarningPanel warningPanel;
    
    private static final String J2EE_SPEC_13_LABEL = NbBundle.getMessage(PanelOptionsVisual.class, "J2EESpecLevel_13"); //NOI18N
    private static final String J2EE_SPEC_14_LABEL = NbBundle.getMessage(PanelOptionsVisual.class, "J2EESpecLevel_14"); //NOI18N
    private static final String JAVA_EE_SPEC_50_LABEL = NbBundle.getMessage(PanelOptionsVisual.class, "JavaEESpecLevel_50"); //NOI18N

    private List earProjects;
    
    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual(PanelConfigureProject panel) {
        initComponents();
        setJ2eeVersionWarningPanel();
        this.panel = panel;
        currentLibrariesLocation = ".."+File.separatorChar+"libraries"; // NOI18N
        librariesLocation.setText(currentLibrariesLocation);
        initServers(FoldersListSettings.getDefault().getLastUsedServer());
        // preselect the first item in the j2ee spec combo
        if (j2eeSpecComboBox.getModel().getSize() > 0) {
            j2eeSpecComboBox.setSelectedIndex(0);
        }
        initEnterpriseApplications();
    }

    public void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }
    
    protected int computeHeight() {
        return serverInstanceComboBox.getFontMetrics(serverInstanceComboBox.getFont()).getHeight() * 8 + 100;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelEnterprise = new javax.swing.JLabel();
        jComboBoxEnterprise = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();
        j2eeSpecLabel = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();
        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        addServerButton = new javax.swing.JButton();
        warningPlaceHolderPanel = new javax.swing.JPanel();
        setAsMainCheckBox = new javax.swing.JCheckBox();
        sharableProject = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        librariesLocation = new javax.swing.JTextField();
        browseLibraries = new javax.swing.JButton();

        jLabelEnterprise.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_NWP1_AddToEnterprise_LabelMnemonic").charAt(0));
        jLabelEnterprise.setLabelFor(jComboBoxEnterprise);
        jLabelEnterprise.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_AddToEnterprise_Label")); // NOI18N

        serverInstanceLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_Server_LabelMnemonic").charAt(0));
        serverInstanceLabel.setLabelFor(serverInstanceComboBox);
        serverInstanceLabel.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_Server")); // NOI18N

        serverInstanceComboBox.setModel(serversModel);
        serverInstanceComboBox.setPrototypeDisplayValue("The Gr8est Marvelous Nr. 1 Server");
        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });

        j2eeSpecLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_J2EESpecLevel_CheckBoxMnemonic").charAt(0));
        j2eeSpecLabel.setLabelFor(j2eeSpecComboBox);
        j2eeSpecLabel.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_J2EESpecLevel_Label")); // NOI18N

        j2eeSpecComboBox.setPrototypeDisplayValue("MMMMMMMMM" /* "Java EE 5" */);
        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });

        jLabelContextPath.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_ContextPath_CheckBoxMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_ContextPath_Label")); // NOI18N

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addServerButton, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_AddServer")); // NOI18N
        addServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(serverInstanceLabel)
                .add(55, 55, 55)
                .add(serverInstanceComboBox, 0, 273, Short.MAX_VALUE)
                .add(6, 6, 6)
                .add(addServerButton))
            .add(jPanel1Layout.createSequentialGroup()
                .add(j2eeSpecLabel)
                .add(11, 11, 11)
                .add(j2eeSpecComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabelContextPath)
                .add(23, 23, 23)
                .add(jTextFieldContextPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(serverInstanceLabel))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(serverInstanceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(addServerButton))
                .add(5, 5, 5)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(j2eeSpecLabel))
                    .add(j2eeSpecComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(jLabelContextPath))
                    .add(jTextFieldContextPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_NWP1_Server_ComboBox_A11YDesc")); // NOI18N
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NPW1_J2EESpecLevel_A11YDesc")); // NOI18N
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_ContextPath_A11YDesc")); // NOI18N
        addServerButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ASCN_AddServer")); // NOI18N
        addServerButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ASCD_AddServer")); // NOI18N

        warningPlaceHolderPanel.setLayout(new java.awt.BorderLayout());

        setAsMainCheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SetAsMain_CheckBoxMnemonic").charAt(0));
        setAsMainCheckBox.setSelected(true);
        setAsMainCheckBox.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SetAsMain_CheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        sharableProject.setMnemonic('P');
        sharableProject.setSelected(true);
        sharableProject.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_SharableProject_Checkbox")); // NOI18N
        sharableProject.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sharableProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sharableProjectActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Location_Label")); // NOI18N

        librariesLocation.setEditable(false);

        browseLibraries.setMnemonic('B');
        browseLibraries.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Browse_Button")); // NOI18N
        browseLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLibrariesActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(sharableProject)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(librariesLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseLibraries)))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(setAsMainCheckBox)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(jLabelEnterprise)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxEnterprise, 0, 286, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(warningPlaceHolderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelEnterprise)
                    .add(jComboBoxEnterprise, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(warningPlaceHolderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setAsMainCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sharableProject)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(browseLibraries)
                    .add(librariesLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jLabelEnterprise.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_AddToEnterpriseComboBox_A11YDesc")); // NOI18N
        jComboBoxEnterprise.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_AddToEnterpriseComboBox_A11YDesc")); // NOI18N
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_SetAsMain_A11YDesc")); // NOI18N
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

    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        setJ2eeVersionWarningPanel();
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed

    private void serverInstanceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInstanceComboBoxActionPerformed
        String prevSelectedItem = (String) j2eeSpecComboBox.getSelectedItem();
        // update the j2ee spec list according to the selected server
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceWrapper.getServerInstanceID());
            Set supportedVersions = j2eePlatform.getSupportedSpecVersions(J2eeModule.WAR);
            j2eeSpecComboBox.removeAllItems();
            if (supportedVersions.contains(J2eeModule.JAVA_EE_5)) {
                j2eeSpecComboBox.addItem(JAVA_EE_SPEC_50_LABEL);
            }
            if (supportedVersions.contains(J2eeModule.J2EE_14)) {
                j2eeSpecComboBox.addItem(J2EE_SPEC_14_LABEL);
            }
            if (supportedVersions.contains(J2eeModule.J2EE_13)) {
                j2eeSpecComboBox.addItem(J2EE_SPEC_13_LABEL);
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

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        contextModified = true;
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased

    private void sharableProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sharableProjectActionPerformed
        librariesLocation.setEnabled(sharableProject.isSelected());
        browseLibraries.setEnabled(sharableProject.isSelected());
        if (sharableProject.isSelected()) {
            librariesLocation.setText(currentLibrariesLocation);
        } else {
            librariesLocation.setText("");
        }
    }//GEN-LAST:event_sharableProjectActionPerformed

    private void browseLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLibrariesActionPerformed
        // below folder is used just for relativization:
        File f = FileUtil.normalizeFile(new File(projectLocation +
                File.separatorChar + "project_folder")); // NOI18N
        String curr = SharableLibrariesUtils.browseForLibraryLocation(librariesLocation.getText().trim(), this, f);
        if (curr != null) {
            currentLibrariesLocation = curr;
            if (sharableProject.isSelected()) {
                librariesLocation.setText(currentLibrariesLocation);
            }
        }
    }//GEN-LAST:event_browseLibrariesActionPerformed
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (getSelectedServer() == null) {
            String errMsg = NbBundle.getMessage(PanelOptionsVisual.class, "MSG_NoServer");
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", errMsg); // NOI18N
            return false;
        }
        return true;
    }

    void store(WizardDescriptor d) {
        d.putProperty(WizardProperties.SET_AS_MAIN, setAsMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE );
        d.putProperty(WizardProperties.SERVER_INSTANCE_ID, getSelectedServer());
        d.putProperty(WizardProperties.SOURCE_STRUCTURE, sourceStructure);
        d.putProperty(WizardProperties.J2EE_LEVEL, getSelectedJ2eeSpec());
        d.putProperty(WizardProperties.CONTEXT_PATH, jTextFieldContextPath.getText().trim());
        d.putProperty(WizardProperties.EAR_APPLICATION, getSelectedEarApplication());
        d.putProperty(WizardProperties.SHARED_LIBRARIES, sharableProject.isSelected() ? librariesLocation.getText() : null ); // NOI18N
        // TODO: for Java EE 5.0, warningpanel is null, 
        // but we need some check for Java SE 5.0 and higher
        if (warningPanel != null && warningPanel.getDowngradeAllowed()) {
            d.putProperty(WizardProperties.JAVA_PLATFORM, warningPanel.getSuggestedJavaPlatformName());
            
            String j2ee = getSelectedJ2eeSpec();
            if (j2ee != null) {
                String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
                FoldersListSettings fls = FoldersListSettings.getDefault();
                String srcLevel = "1.6"; //NOI18N
                if ((warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_14) && fls.isAgreedSetSourceLevel14()) ||
                        (warningType.equals(J2eeVersionWarningPanel.WARN_SET_JDK_14) && fls.isAgreedSetJdk14()))
                    srcLevel = "1.4"; //NOI18N
                else if ((warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_15) && fls.isAgreedSetSourceLevel15()) ||
                        (warningType.equals(J2eeVersionWarningPanel.WARN_SET_JDK_15) && fls.isAgreedSetJdk15()))
                    srcLevel = "1.5"; //NOI18N
                
                d.putProperty(WizardProperties.SOURCE_LEVEL, srcLevel);
            }            
        } else
            d.putProperty(WizardProperties.SOURCE_LEVEL, null);
    }
    
    void read(WizardDescriptor d) {
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
    private javax.swing.JButton browseLibraries;
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JLabel j2eeSpecLabel;
    private javax.swing.JComboBox jComboBoxEnterprise;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelEnterprise;
    private javax.swing.JPanel jPanel1;
    protected javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JTextField librariesLocation;
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JCheckBox sharableProject;
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
            if (displayName != null && j2eePlatform != null && j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.WAR)) {
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
                            : item.equals(JAVA_EE_SPEC_50_LABEL) ? J2eeModule.JAVA_EE_5 : 
                                ( item.equals(J2EE_SPEC_14_LABEL) ? J2eeModule.J2EE_14 : J2eeModule.J2EE_13);
    }
    
    private String getSelectedServer() {
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper == null) {
            return null;
        }
        return serverInstanceWrapper.getServerInstanceID();
    }
    
    protected boolean isContextModified() {
         return contextModified;
    }

    private Project getSelectedEarApplication() {
        int idx = jComboBoxEnterprise.getSelectedIndex();
        return (idx <= 0) ? null : (Project) earProjects.get(idx - 1);
    }
    
    private void initEnterpriseApplications() {
        jComboBoxEnterprise.addItem(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_AddToEnterprise_None")); // TODO: AB: add to bundle
        jComboBoxEnterprise.setSelectedIndex(0);
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        earProjects = new ArrayList();
        for (int i = 0; i < allProjects.length; i++) {
            J2eeApplicationProvider j2eeAppProvider = allProjects[i].getLookup().lookup(J2eeApplicationProvider.class);
            if (j2eeAppProvider == null) {
                continue;
            }
            J2eeApplication j2eeApplication = (J2eeApplication) j2eeAppProvider.getJ2eeModule();
            ProjectInformation projectInfo = ProjectUtils.getInformation(allProjects[i]);
            if (j2eeApplication != null) {
                earProjects.add(projectInfo.getProject());
                jComboBoxEnterprise.addItem(projectInfo.getDisplayName());
            }
        }
        if (earProjects.size() <= 0) {
            jComboBoxEnterprise.setEnabled(false);
        }
    }
    
    private void setJ2eeVersionWarningPanel() {
        String j2ee = getSelectedJ2eeSpec();
        if (j2ee == null)
            return;
        String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
        if (warningType == null && warningPanel == null)
            return;
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

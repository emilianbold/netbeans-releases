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

package org.netbeans.modules.j2ee.common.project.ui;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.common.FileSearchUtility;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

final class ProjectServerPanel extends javax.swing.JPanel implements DocumentListener {

    private ProjectServerWizardPanel wizard;
    private boolean contextModified = false;
    private final DefaultComboBoxModel serversModel = new DefaultComboBoxModel();
    
    private J2eeVersionWarningPanel warningPanel;
    private boolean sharableProject;

    private List<Project> earProjects;
    private final J2eeModule.Type j2eeModuleType;
    private File projectLocation;
    
    private BigDecimal xmlVersion;

    @Deprecated
    public ProjectServerPanel(Object j2eeModuleType, String name, String title,
            ProjectServerWizardPanel wizard, boolean showAddToEar,
            boolean mainAppClientClass, boolean showContextPath, boolean createProjects) {

        this(J2eeModule.Type.fromJsrType(j2eeModuleType), name, title, wizard, showAddToEar, mainAppClientClass, showContextPath, createProjects);
    }

    /** Creates new form ProjectServerPanel */
    public ProjectServerPanel(J2eeModule.Type j2eeModuleType, String name, String title,
            ProjectServerWizardPanel wizard, boolean showAddToEar, 
            boolean mainAppClientClass, boolean showContextPath, boolean createProjects) {
        initComponents();
        setJ2eeVersionWarningPanel();
        this.wizard = wizard;
        this.j2eeModuleType = j2eeModuleType;
        initServers(UserProjectSettings.getDefault().getLastUsedServer());
        // preselect the first item in the j2ee spec combo
        if (j2eeSpecComboBox.getModel().getSize() > 0) {
            j2eeSpecComboBox.setSelectedIndex(0);
        }
        initEnterpriseApplications();
        
        // Provide a name in the title bar.
        setName(name);
        putClientProperty ("NewProjectWizard_Title", title);
        
        jLabelEnterprise.setVisible(showAddToEar);
        jComboBoxEnterprise.setVisible(showAddToEar);
        jLabelContextPath.setVisible(showContextPath);
        jTextFieldContextPath.setVisible(showContextPath);
        mainClassLabel.setVisible(mainAppClientClass);
        mainClassTextField.setVisible(mainAppClientClass);
        createCarCheckBox.setVisible(createProjects);
        createEjbCheckBox.setVisible(createProjects);
        createWARCheckBox.setVisible(createProjects);
        jTextFieldCarName.setVisible(createProjects);
        jTextFieldEjbModuleName.setVisible(createProjects);
        jTextFieldWebAppName.setVisible(createProjects);
        mainClassLabel1.setVisible(createProjects);
        mainClassTextFieldWithinEar.setVisible(createProjects);
        
        jTextFieldCarName.getDocument().addDocumentListener( this );
        jTextFieldEjbModuleName.getDocument().addDocumentListener( this );
        jTextFieldWebAppName.getDocument().addDocumentListener( this );
        mainClassTextFieldWithinEar.getDocument().addDocumentListener( this );
        mainClassTextField.getDocument().addDocumentListener( this );
        jTextFieldContextPath.getDocument().addDocumentListener( this );
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
        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();
        addServerButton = new javax.swing.JButton();
        j2eeSpecLabel = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();
        jLabelContextPath = new javax.swing.JLabel();
        warningPlaceHolderPanel = new javax.swing.JPanel();
        jTextFieldContextPath = new javax.swing.JTextField();
        mainClassLabel = new javax.swing.JLabel();
        mainClassTextField = new javax.swing.JTextField();
        createEjbCheckBox = new javax.swing.JCheckBox();
        jTextFieldEjbModuleName = new javax.swing.JTextField();
        createWARCheckBox = new javax.swing.JCheckBox();
        jTextFieldWebAppName = new javax.swing.JTextField();
        createCarCheckBox = new javax.swing.JCheckBox();
        jTextFieldCarName = new javax.swing.JTextField();
        mainClassLabel1 = new javax.swing.JLabel();
        mainClassTextFieldWithinEar = new javax.swing.JTextField();
        serverLibraryCheckbox = new javax.swing.JCheckBox();

        jLabelEnterprise.setLabelFor(jComboBoxEnterprise);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelEnterprise, NbBundle.getMessage(ProjectServerPanel.class, "LBL_NWP1_AddToEnterprise_Label")); // NOI18N

        serverInstanceLabel.setLabelFor(serverInstanceComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(serverInstanceLabel, NbBundle.getMessage(ProjectServerPanel.class, "LBL_NWP1_Server")); // NOI18N

        serverInstanceComboBox.setModel(serversModel);
        serverInstanceComboBox.setPrototypeDisplayValue("The Gr8est Marvelous Nr. 1 Server");
        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addServerButton, org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "LBL_AddServer")); // NOI18N
        addServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerButtonActionPerformed(evt);
            }
        });

        j2eeSpecLabel.setLabelFor(j2eeSpecComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(j2eeSpecLabel, NbBundle.getMessage(ProjectServerPanel.class, "LBL_NWP1_J2EESpecLevel_Label")); // NOI18N

        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });

        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPath, NbBundle.getMessage(ProjectServerPanel.class, "LBL_NWP1_ContextPath_Label")); // NOI18N

        warningPlaceHolderPanel.setBackground(new java.awt.Color(0, 153, 102));
        warningPlaceHolderPanel.setLayout(new java.awt.BorderLayout());

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });

        mainClassLabel.setLabelFor(mainClassTextField);
        org.openide.awt.Mnemonics.setLocalizedText(mainClassLabel, org.openide.util.NbBundle.getBundle(ProjectServerPanel.class).getString("LBL_NWP1_MainClass_Label")); // NOI18N
        mainClassLabel.setAlignmentX(0.5F);

        mainClassTextField.setText("com.myapp.Main");

        createEjbCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createEjbCheckBox, org.openide.util.NbBundle.getBundle(ProjectServerPanel.class).getString("LBL_NEAP_CreateEjbModule")); // NOI18N
        createEjbCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createEjbCheckBox_action(evt);
            }
        });

        createWARCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createWARCheckBox, org.openide.util.NbBundle.getBundle(ProjectServerPanel.class).getString("LBL_NEAP_CreatWebAppModule")); // NOI18N
        createWARCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createWARCheckBoxcreateWebAppCheckBox_action(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(createCarCheckBox, org.openide.util.NbBundle.getBundle(ProjectServerPanel.class).getString("LBL_NEAP_CreateCarModule")); // NOI18N
        createCarCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createCarCheckBox_action(evt);
            }
        });

        jTextFieldCarName.setEnabled(false);

        mainClassLabel1.setLabelFor(mainClassTextFieldWithinEar);
        org.openide.awt.Mnemonics.setLocalizedText(mainClassLabel1, org.openide.util.NbBundle.getBundle(ProjectServerPanel.class).getString("LBL_NWP1_MainClass_Label")); // NOI18N

        mainClassTextFieldWithinEar.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(serverLibraryCheckbox, org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "PanelSharabilityVisual.serverLibraryCheckbox.text")); // NOI18N
        serverLibraryCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        serverLibraryCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverLibraryCheckboxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelContextPath)
                    .add(serverInstanceLabel)
                    .add(j2eeSpecLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(mainClassTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                        .add(74, 74, 74))
                    .add(layout.createSequentialGroup()
                        .add(jTextFieldContextPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                        .add(74, 74, 74))
                    .add(layout.createSequentialGroup()
                        .add(serverInstanceComboBox, 0, 350, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addServerButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(serverLibraryCheckbox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                        .add(74, 74, 74))
                    .add(layout.createSequentialGroup()
                        .add(j2eeSpecComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .add(layout.createSequentialGroup()
                .add(mainClassLabel)
                .addContainerGap(463, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(createCarCheckBox)
                        .add(createEjbCheckBox)
                        .add(createWARCheckBox))
                    .add(mainClassLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldEjbModuleName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .add(jTextFieldWebAppName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .add(jTextFieldCarName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .add(mainClassTextFieldWithinEar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE))
                .add(74, 74, 74))
            .add(warningPlaceHolderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(jLabelEnterprise)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxEnterprise, 0, 342, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelEnterprise)
                    .add(jComboBoxEnterprise, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverInstanceLabel)
                    .add(addServerButton)
                    .add(serverInstanceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serverLibraryCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(j2eeSpecLabel)
                    .add(j2eeSpecComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(warningPlaceHolderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelContextPath)
                    .add(jTextFieldContextPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mainClassLabel)
                    .add(mainClassTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldEjbModuleName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createEjbCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldWebAppName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createWARCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createCarCheckBox)
                    .add(jTextFieldCarName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mainClassLabel1)
                    .add(mainClassTextFieldWithinEar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(67, 67, 67))
        );

        jLabelEnterprise.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        jComboBoxEnterprise.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        serverInstanceLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        addServerButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ASCN_AddServer")); // NOI18N
        addServerButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        j2eeSpecLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        jLabelContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        warningPlaceHolderPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        warningPlaceHolderPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        mainClassLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        createEjbCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        jTextFieldEjbModuleName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "LBL_NEAP_CreateEjbModule")); // NOI18N
        jTextFieldEjbModuleName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        createWARCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        jTextFieldWebAppName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "LBL_NEAP_CreatWebAppModule")); // NOI18N
        jTextFieldWebAppName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        createCarCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        jTextFieldCarName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "LBL_NEAP_CreateCarModule")); // NOI18N
        jTextFieldCarName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        mainClassLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        mainClassTextFieldWithinEar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        serverLibraryCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectServerPanel.class, "ACSD_ProjectServerPanel_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void addServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerButtonActionPerformed
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        String selectedServerInstanceID = null;
        if (serverInstanceWrapper != null) {
            selectedServerInstanceID = serverInstanceWrapper.getServerInstanceID();
        }
        ProfileItem lastSelectedJ2eeProfile = (ProfileItem) j2eeSpecComboBox.getSelectedItem();
        String newServerInstanceID = ServerManager.showAddServerInstanceWizard();
        if (newServerInstanceID != null) {
            selectedServerInstanceID = newServerInstanceID;
            // clear the spec level selection
            lastSelectedJ2eeProfile = null;
            j2eeSpecComboBox.setSelectedItem(null);
        }
        // refresh the list of servers
        initServers(selectedServerInstanceID);
        if (lastSelectedJ2eeProfile != null) {
            j2eeSpecComboBox.setSelectedItem(lastSelectedJ2eeProfile);
        }
}//GEN-LAST:event_addServerButtonActionPerformed

    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        setJ2eeVersionWarningPanel();
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed

    private void serverInstanceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInstanceComboBoxActionPerformed
        ProfileItem prevSelectedItem = (ProfileItem) j2eeSpecComboBox.getSelectedItem();
        // update the j2ee spec list according to the selected server
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        j2eeSpecComboBox.removeAllItems();
        if (serverInstanceWrapper != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceWrapper.getServerInstanceID());
            Set<Profile> profiles = new TreeSet<Profile>(Profile.UI_COMPARATOR);
            profiles.addAll(j2eePlatform.getSupportedProfiles(j2eeModuleType));
            for (Profile profile : profiles) {
                // j2ee 1.3 is not supported anymore
                if (Profile.J2EE_13.equals(profile)) {
                    continue;
                }
                if (j2eeModuleType ==J2eeModule.Type.WAR) {
                    if (Profile.JAVA_EE_6_FULL.equals(profile)) {
                        // for web apps always offer only JAVA_EE_6_WEB profile and skip full one
                        continue;
                    }
                } else {
                    if (Profile.JAVA_EE_6_WEB.equals(profile)) {
                        // for EE apps always skip web profile
                        continue;
                    }
                }

                j2eeSpecComboBox.addItem(new ProfileItem(profile));
            }
            if (prevSelectedItem != null) {
                j2eeSpecComboBox.setSelectedItem(prevSelectedItem);
            }
        }
        // revalidate the form
        wizard.fireChangeEvent();
    }//GEN-LAST:event_serverInstanceComboBoxActionPerformed

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        contextModified = true;
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased

private void createEjbCheckBox_action(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createEjbCheckBox_action
    jTextFieldEjbModuleName.setEnabled(createEjbCheckBox.isSelected());
    wizard.fireChangeEvent();
}//GEN-LAST:event_createEjbCheckBox_action

private void createWARCheckBoxcreateWebAppCheckBox_action(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createWARCheckBoxcreateWebAppCheckBox_action
    jTextFieldWebAppName.setEnabled(createWARCheckBox.isSelected());
    wizard.fireChangeEvent();
}//GEN-LAST:event_createWARCheckBoxcreateWebAppCheckBox_action

private void createCarCheckBox_action(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createCarCheckBox_action
    jTextFieldCarName.setEnabled(createCarCheckBox.isSelected());
    mainClassTextFieldWithinEar.setEnabled(createCarCheckBox.isSelected());
    wizard.fireChangeEvent();
}//GEN-LAST:event_createCarCheckBox_action

private void serverLibraryCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverLibraryCheckboxActionPerformed
    wizard.fireChangeEvent();
}//GEN-LAST:event_serverLibraryCheckboxActionPerformed
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty(ProjectLocationPanel.PROP_ERROR_MESSAGE, null);
        if (getSelectedServer() == null) {
            String errMsg = NbBundle.getMessage(ProjectServerPanel.class, "MSG_NoServer");
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, errMsg); // NOI18N
            return false;
        }
        if (isSharableProject() && serverLibraryCheckbox.isEnabled()
            && serverLibraryCheckbox.isSelected()) {
                wizardDescriptor.putProperty(ProjectLocationPanel.PROP_ERROR_MESSAGE, ProjectLocationPanel.decorateMessage(
                    NbBundle.getMessage(ProjectServerPanel.class, "PanelSharability.licenseWarning.text")));
        }
        if (J2eeModule.Type.EJB.equals(j2eeModuleType)) {
            setJ2eeVersionWarning(wizardDescriptor);
        }
        
        if (J2eeModule.Type.CAR.equals(j2eeModuleType)) {
            if (!isMainClassValid(mainClassTextField.getText())) {
                setErrorMessage("ERROR_IllegalMainClassName", wizardDescriptor); // NOI18N
                return false;
            }
        }

        if (J2eeModule.Type.EAR.equals(j2eeModuleType)) {
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
                if (!isMainClassValid(mainClassTextFieldWithinEar.getText())) {
                    setErrorMessage("ERROR_IllegalMainClassName", wizardDescriptor); // NOI18N
                    return false;
                }
                if (!jarName.endsWith("-app-client")) { //NOI18N
                    // this is really just a warning
                    setErrorMessage("MSG_CARNameNotBlueprints", wizardDescriptor); // NOI18N
                }
            }
        }

        return true;
    }

    private boolean isMainClassValid(String mainClassName) {
        StringTokenizer tk = new StringTokenizer(mainClassName, "."); //NOI18N
        boolean valid = tk.countTokens() > 0;
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
                valid = false;
                break;
            }
        }
        return valid;
    }
    
    private static void setErrorMessage(
            final String errMsgKey, final WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                NbBundle.getMessage(ProjectServerPanel.class, errMsgKey));
    }
    
    private boolean isSharableProject() {
        return sharableProject;
    }
    
    void store(WizardDescriptor d) {
        d.putProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID, getSelectedServer());
        d.putProperty(ProjectServerWizardPanel.J2EE_LEVEL, getSelectedJ2eeProfile());
        d.putProperty(ProjectServerWizardPanel.CONTEXT_PATH, jTextFieldContextPath.getText().trim());
        d.putProperty(ProjectServerWizardPanel.EAR_APPLICATION, getSelectedEarApplication());
        d.putProperty(ProjectServerWizardPanel.WAR_NAME,  jTextFieldWebAppName.getText());
        d.putProperty(ProjectServerWizardPanel.JAR_NAME, jTextFieldEjbModuleName.getText());
        d.putProperty(ProjectServerWizardPanel.CAR_NAME, jTextFieldCarName.getText());
        d.putProperty(ProjectServerWizardPanel.MAIN_CLASS, J2eeModule.Type.CAR.equals(j2eeModuleType) ? mainClassTextField.getText().trim() : mainClassTextFieldWithinEar.getText().trim()); // NOI18N
        d.putProperty(ProjectServerWizardPanel.CREATE_WAR, Boolean.valueOf(createWARCheckBox.isVisible() ? createWARCheckBox.isSelected() : false));
        d.putProperty(ProjectServerWizardPanel.CREATE_JAR, Boolean.valueOf(createEjbCheckBox.isVisible() ? createEjbCheckBox.isSelected() : false));
        d.putProperty(ProjectServerWizardPanel.CREATE_CAR, Boolean.valueOf(createCarCheckBox.isVisible() ? createCarCheckBox.isSelected() : false));
    
        // #119052
        String sourceLevel = "1.5"; // NOI18N
        if (warningPanel != null && warningPanel.getDowngradeAllowed()) {
            d.putProperty(ProjectServerWizardPanel.JAVA_PLATFORM, warningPanel.getSuggestedJavaPlatformName());
            
            Profile j2ee = getSelectedJ2eeProfile();
            if (j2ee != null) {
                String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
                UserProjectSettings fls = UserProjectSettings.getDefault();
                if (warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_14) && fls.isAgreedSetSourceLevel14()) {
                    sourceLevel = "1.4"; //NOI18N
                }
            }
        }
        d.putProperty(ProjectServerWizardPanel.SOURCE_LEVEL, sourceLevel);
        
        d.putProperty(ProjectServerWizardPanel.WIZARD_SERVER_LIBRARY, getServerLibraryName());
    }
    
    private String getServerLibraryName() {
        if (!serverLibraryCheckbox.isSelected() || !serverLibraryCheckbox.isEnabled()) {
            return null;
        }
        Deployment deployment = Deployment.getDefault();
        String name = deployment.getServerDisplayName(deployment.getServerID(getSelectedServer()));
        // null can occur only if the server was removed somehow
        return (name == null) ? "" : PropertyUtils.getUsablePropertyName(name); // NOI18N
    }

    
    void read(WizardDescriptor d) {
        if (!isContextModified()) {
            jTextFieldContextPath.setText(createDefaultContext((String)d.getProperty(ProjectLocationWizardPanel.NAME)));
        }
        sharableProject = d.getProperty(ProjectLocationWizardPanel.SHARED_LIBRARIES) != null;
        serverLibraryCheckbox.setEnabled(isSharableProject());
        if (!serverLibraryCheckbox.isEnabled()) {
            serverLibraryCheckbox.setSelected(false);
        }
        projectLocation = (File)d.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (J2eeModule.Type.EJB.equals(j2eeModuleType)) {
            updateJ2EEVersion("ejb-jar.xml");
        }
        if (J2eeModule.Type.CAR.equals(j2eeModuleType)) {
            initClientAppMainClass((String)d.getProperty(ProjectLocationWizardPanel.NAME));
            updateJ2EEVersion("application-client.xml");
        }
        if (J2eeModule.Type.EAR.equals(j2eeModuleType)) {
            String newProjectName = (String)d.getProperty(ProjectLocationWizardPanel.NAME);
            initClientAppMainClass(newProjectName);
            this.jTextFieldEjbModuleName.setText(MessageFormat.format(
                NbBundle.getMessage(ProjectServerPanel.class,"TXT_EJBProjectName"), new Object[] {newProjectName}));
            this.jTextFieldWebAppName.setText(MessageFormat.format(
                NbBundle.getMessage(ProjectServerPanel.class,"TXT_WebAppProjectName"), new Object[] {newProjectName}));
            this.jTextFieldCarName.setText(MessageFormat.format(
                NbBundle.getMessage(ProjectServerPanel.class,"TXT_AppClientProjectName"), new Object[] {newProjectName}));
        }
    }
    
    /** Create a valid default for context path from project name.
     */
    private static String createDefaultContext(String projectName) {
        return "/" + PropertyUtils.getUsablePropertyName(projectName);
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
    private javax.swing.JCheckBox createCarCheckBox;
    private javax.swing.JCheckBox createEjbCheckBox;
    private javax.swing.JCheckBox createWARCheckBox;
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JLabel j2eeSpecLabel;
    private javax.swing.JComboBox jComboBoxEnterprise;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelEnterprise;
    private javax.swing.JTextField jTextFieldCarName;
    protected javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JTextField jTextFieldEjbModuleName;
    private javax.swing.JTextField jTextFieldWebAppName;
    private javax.swing.JLabel mainClassLabel;
    private javax.swing.JLabel mainClassLabel1;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JTextField mainClassTextFieldWithinEar;
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    private javax.swing.JCheckBox serverLibraryCheckbox;
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
        boolean gfv3Found = false;
        boolean gfv3ee6Found = false;
        for (String serverInstanceID : Deployment.getDefault().getServerInstanceIDs()) {
            String displayName = Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID);
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            if (displayName != null && j2eePlatform != null && j2eePlatform.getSupportedTypes().contains(j2eeModuleType)) {
                ServerInstanceWrapper serverWrapper = new ServerInstanceWrapper(serverInstanceID, displayName);
                // decide whether this server should be preselected
                if (selectedItem == null || !gfv3ee6Found) {
                    if (selectedServerInstanceID != null) {
                        if (selectedServerInstanceID.equals(serverInstanceID)) {
                            selectedItem = serverWrapper;
                        }
                    } else {
                        // preselect the best server ;)
                        // FIXME replace with PriorityQueue mechanism
                        String shortName = Deployment.getDefault().getServerID(serverInstanceID);
                        if ("gfv3ee6".equals(shortName)) { // NOI18N
                            selectedItem = serverWrapper;
                            gfv3ee6Found = true;
                        } else if ("gfv3".equals(shortName) && !gfv3ee6Found) { // NOI18N
                            selectedItem = serverWrapper;
                            gfv3Found = true;
                        } else if ("J2EE".equals(shortName) && !(gfv3ee6Found || gfv3Found)) { // NOI18N
                            selectedItem = serverWrapper;
                            sjasFound = true;
                        } else if ("JBoss4".equals(shortName) && !(gfv3ee6Found || gfv3Found || sjasFound)) { // NOI18N
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
    
    private Profile getSelectedJ2eeProfile() {
        ProfileItem item = (ProfileItem) j2eeSpecComboBox.getSelectedItem();
        return item == null ? null : item.getProfile();
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
        return (idx <= 0) ? null : earProjects.get(idx - 1);
    }
    
    private void initEnterpriseApplications() {
        jComboBoxEnterprise.addItem(NbBundle.getMessage(ProjectServerPanel.class, "LBL_NWP1_AddToEnterprise_None")); // TODO: AB: add to bundle
        jComboBoxEnterprise.setSelectedIndex(0);
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        earProjects = new ArrayList<Project>();
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
        Profile j2ee = getSelectedJ2eeProfile();
        if (j2ee == null) {
            warningPlaceHolderPanel.setVisible(false);
            return;
        }
        String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
        if (warningType == null && warningPanel == null) {
            warningPlaceHolderPanel.setVisible(false);
            return;
        }
        if (warningPanel == null) {
            warningPanel = new J2eeVersionWarningPanel(warningType);
            warningPlaceHolderPanel.add(warningPanel, java.awt.BorderLayout.CENTER);
            warningPanel.setWarningType(warningType);
        } else {
            warningPanel.setWarningType(warningType);
        }
        warningPlaceHolderPanel.setVisible(true);
        this.revalidate();
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
    
    private void updateJ2EEVersion(String configFileName) {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projectLocation));
        if (fo != null) {
            FileObject configFilesPath = FileSearchUtility.guessConfigFilesPath(fo, configFileName);
            if (configFilesPath != null) {
                FileObject configFile = configFilesPath.getFileObject(configFileName); // NOI18N
                if (J2eeModule.Type.EJB.equals(j2eeModuleType)) {
                    checkEjbJarXmlJ2eeVersion(configFile);
                } else if (J2eeModule.Type.CAR.equals(j2eeModuleType)) {
                    checkACXmlJ2eeVersion(configFile);
                }
            } else {
                // suppose highest
                j2eeSpecComboBox.setSelectedItem(new ProfileItem(Profile.JAVA_EE_5));
            }
        }
    }
    
    private BigDecimal getEjbJarXmlVersion(FileObject ejbJarXml) throws IOException {
        if (ejbJarXml != null) {
            org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = 
                    org.netbeans.modules.j2ee.dd.api.ejb.DDProvider.getDefault().getDDRoot(ejbJarXml);
            if (ejbJar != null) {
                return ejbJar.getVersion();
            }
        }
        return null;
    }

    private void checkEjbJarXmlJ2eeVersion(FileObject ejbJarXml) {
        try {
            BigDecimal version = getEjbJarXmlVersion(ejbJarXml);
            xmlVersion = version;
            if (version == null) {
                return;
            }
            
            if(new BigDecimal(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.VERSION_2_1).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(new ProfileItem(Profile.J2EE_14));
            }
        } catch (IOException e) {
            String message = NbBundle.getMessage(ProjectServerPanel.class, "MSG_EjbJarXmlCorrupted"); // NOI18N
            Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(e, message));
        }
    }

    private BigDecimal getACXmlVersion(FileObject appClientXML) throws IOException {
        if (appClientXML != null) {
            // TODO: possible NPE (will getEjbJar return something)?
            return org.netbeans.modules.j2ee.dd.api.client.DDProvider.getDefault().getDDRoot(appClientXML).getVersion();
        } else {
            return null;
        }
    }

    private void checkACXmlJ2eeVersion(FileObject appClientXML) {
        try {
            BigDecimal version = getACXmlVersion(appClientXML);
            if (version == null) {
                return;
            }
            
            if(new BigDecimal(org.netbeans.modules.j2ee.dd.api.client.AppClient.VERSION_1_4).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(new ProfileItem(Profile.J2EE_14));
            } else if(new BigDecimal(org.netbeans.modules.j2ee.dd.api.client.AppClient.VERSION_5_0).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(new ProfileItem(Profile.JAVA_EE_5));
            } else if(new BigDecimal(org.netbeans.modules.j2ee.dd.api.client.AppClient.VERSION_6_0).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(new ProfileItem(Profile.JAVA_EE_6_FULL));
            }
        } catch (IOException e) {
            String message = NbBundle.getMessage(ProjectServerPanel.class, "MSG_AppClientXmlCorrupted"); // NOI18N
            Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(e, message));
        }
    }
    
    private void setJ2eeVersionWarning(WizardDescriptor d) {
        String errorMessage = null;
        ProfileItem selectedItem = (ProfileItem) j2eeSpecComboBox.getSelectedItem();
        
        boolean oldXml = xmlVersion == null ? true :
            new BigDecimal(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.VERSION_2_1).compareTo(xmlVersion) > 0;
        if (Profile.J2EE_14 == selectedItem.getProfile() && oldXml) {
            errorMessage = NbBundle.getMessage(ProjectServerPanel.class, "MSG_EjbJarXMLNotSupported");
        }

        if (d != null) {
            d.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage);
        }
        
        setJ2eeVersionWarningPanel();
    }
    
    private void initClientAppMainClass(String newProjectName) {
        newProjectName = getPackageName(newProjectName);
        if (!Utilities.isJavaIdentifier(newProjectName)) {
            newProjectName = NbBundle.getMessage(ProjectServerPanel.class, "TXT_PackageNameSuffix", newProjectName);
        }
        if (J2eeModule.Type.CAR.equals(j2eeModuleType)) {
            mainClassTextField.setText(MessageFormat.format(
                    NbBundle.getMessage(ProjectServerPanel.class,"TXT_ClassName"), new Object[] {newProjectName}
            ));
        } else {
            mainClassTextFieldWithinEar.setText(MessageFormat.format(
                    NbBundle.getMessage(ProjectServerPanel.class,"TXT_ClassName"), new Object[] {newProjectName}
            ));
        }
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
        return builder.length() == 0 ? NbBundle.getMessage(ProjectServerPanel.class,"TXT_DefaultPackageName") : builder.toString();
    }
    
    public void changedUpdate( DocumentEvent e ) {
        wizard.fireChangeEvent();
    }
    
    public void insertUpdate( DocumentEvent e ) {
        wizard.fireChangeEvent();
    }
    
    public void removeUpdate( DocumentEvent e ) {
        wizard.fireChangeEvent();
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ProjectImportLocationPanel.generateHelpID(ProjectServerPanel.class, j2eeModuleType));
    }

    private static class ProfileItem {

        private final Profile profile;

        public ProfileItem(Profile profile) {
            this.profile = profile;
        }

        public Profile getProfile() {
            return profile;
        }

        @Override
        public String toString() {
            return profile.getDisplayName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProfileItem other = (ProfileItem) obj;
            if (this.profile != other.profile && (this.profile == null || !this.profile.equals(other.profile))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.profile != null ? this.profile.hashCode() : 0);
            return hash;
        }

    }
}

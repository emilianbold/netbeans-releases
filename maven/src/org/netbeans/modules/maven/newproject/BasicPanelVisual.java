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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.modules.maven.api.MavenValidators;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.netbeans.modules.maven.options.MavenOptionController;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class BasicPanelVisual extends JPanel implements DocumentListener, WindowFocusListener, Runnable {
    
    public static final String PROP_PROJECT_NAME = "projectName"; //NOI18N
    
    private static final String ERROR_MSG = "WizardPanel_errorMessage"; //NOI18N

    private static final ArtifactVersion BORDER_VERSION = new DefaultArtifactVersion("2.0.7"); //NOI18N

    private boolean askedForVersion;

    private ArtifactVersion mavenVersion;

    private static final Object MAVEN_VERSION_LOCK = new Object();

    private BasicWizardPanel panel;
    private final Archetype arch;

    private String lastProjectName = ""; //NOI18N
    private final ValidationGroup vg;

    private boolean changedPackage = false;
    
    private static final RequestProcessor RPgetver = new RequestProcessor("BasicPanelVisual-getCommandLineMavenVersion"); //NOI18N
    private static final RequestProcessor RPprep = new RequestProcessor("BasicPanelVisual-prepareAdditionalProperties"); //NOI18N
    
    /** Creates new form PanelProjectLocationVisual */
    @SuppressWarnings("unchecked")
    @Messages({
        "VAL_projectLocationTextField=Project Location",
        "VAL_projectNameTextField=Project Name",
        "VAL_ArtifactId=ArtifactId",
        "VAL_Version=Version",
        "VAL_GroupId=GroupId",
        "VAL_Package=Package",
        "ERR_Project_Folder_cannot_be_created=Project Folder cannot be created.",
        "ERR_Project_Folder_is_not_valid_path=Project Folder is not a valid path.",
        "ERR_Project_Folder_is_UNC=Project Folder cannot be located on UNC path.",
        "ERR_old_maven=Maven {0} is too old, version 2.0.7 or newer is needed.",
        "ERR_Project_Folder_exists=Project Folder already exists and is not empty."
    })
    BasicPanelVisual(BasicWizardPanel panel, Archetype arch) {
        this.panel = panel;
        this.arch = arch;

        initComponents();

        projectLocationTextField.putClientProperty(ValidationListener.CLIENT_PROP_NAME, VAL_projectLocationTextField());
        projectNameTextField.putClientProperty(ValidationListener.CLIENT_PROP_NAME, VAL_projectNameTextField());
        txtArtifactId.putClientProperty(ValidationListener.CLIENT_PROP_NAME, VAL_ArtifactId());
        txtVersion.putClientProperty(ValidationListener.CLIENT_PROP_NAME, VAL_Version());
        txtGroupId.putClientProperty(ValidationListener.CLIENT_PROP_NAME, VAL_GroupId());
        txtPackage.putClientProperty(ValidationListener.CLIENT_PROP_NAME, VAL_Package());

        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        txtArtifactId.getDocument().addDocumentListener(this);
        txtGroupId.getDocument().addDocumentListener(this);
        txtVersion.getDocument().addDocumentListener(this);
        txtPackage.getDocument().addDocumentListener(this);
        tblAdditionalProps.setVisible(false);
        lblAdditionalProps.setVisible(false);
        jScrollPane1.setVisible(false);

        if (panel.getArchetypes() == null) {
            lblEEVersion.setVisible(false);
            comboEEVersion.setVisible(false);
        }

        btnSetupNewer.setVisible(false);

        getAccessibleContext().setAccessibleDescription(LBL_CreateProjectStep2());

        txtGroupId.setText(MavenSettings.getDefault().getLastArchetypeGroupId());
        vg = ValidationGroup.create();
        vg.add(txtGroupId, MavenValidators.createGroupIdValidators());
        vg.add(txtArtifactId, MavenValidators.createArtifactIdValidators());
        vg.add(txtVersion, MavenValidators.createVersionValidators());
        vg.add(txtPackage, Validators.JAVA_PACKAGE_NAME);
        vg.add(projectNameTextField, Validators.merge(true,
                MavenValidators.createArtifactIdValidators(),
                Validators.REQUIRE_VALID_FILENAME
              ));

        vg.add(projectLocationTextField, Validators.merge(true,
//                        Validators.FILE_MUST_BE_DIRECTORY,
                new Validator<String>() {
                    public boolean validate(Problems problems, String compName, String model) {
                        File fil = FileUtil.normalizeFile(new File(model));
                        File projLoc = fil;
                        while (projLoc != null && !projLoc.exists()) {
                            projLoc = projLoc.getParentFile();
                        }
                        if (projLoc == null || !projLoc.canWrite()) {
                            problems.add(ERR_Project_Folder_cannot_be_created());
                            return false;
                        }
                        if (FileUtil.toFileObject(projLoc) == null) {
                            problems.add(ERR_Project_Folder_is_not_valid_path());
                            return false;
                        }
                        //#167136
                        if (Utilities.isWindows() && fil.getAbsolutePath().startsWith("\\\\")) {
                            problems.add(ERR_Project_Folder_is_UNC());
                            return false;
                        }
                        return true;
                    }
                }));

        vg.add(new ValidationListener() {
            @Override
            protected boolean validate(Problems problems) {
                boolean tooOld = isMavenTooOld();
                btnSetupNewer.setVisible(tooOld);
                if (tooOld) {
                    problems.add(ERR_old_maven(getCommandLineMavenVersion()));
                    return false;
                }
                File destFolder = FileUtil.normalizeFile(new File(new File(projectLocationTextField.getText().trim()), projectNameTextField.getText().trim()).getAbsoluteFile());
                File[] kids = destFolder.listFiles();
                if (destFolder.exists() && kids != null && kids.length > 0) {
                    // Folder exists and is not empty
                    problems.add(ERR_Project_Folder_exists());
                    return false;
                }

                return true;
            }
        });

    }
    
    
    public String getProjectName() {
        return this.projectNameTextField.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblPackage = new javax.swing.JLabel();
        txtPackage = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        pnlAdditionals = new javax.swing.JPanel();
        lblAdditionalProps = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAdditionalProps = new javax.swing.JTable();
        btnSetupNewer = new javax.swing.JButton();
        lblEEVersion = new javax.swing.JLabel();
        comboEEVersion = new javax.swing.JComboBox();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ProjectName")); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ProjectLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BTN_Browse")); // NOI18N
        browseButton.setActionCommand("BROWSE");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ProjectFolder")); // NOI18N

        createdFolderTextField.setEditable(false);
        createdFolderTextField.setEnabled(false);

        lblArtifactId.setLabelFor(txtArtifactId);
        org.openide.awt.Mnemonics.setLocalizedText(lblArtifactId, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_ArtifactId")); // NOI18N

        txtArtifactId.setEditable(false);
        txtArtifactId.setEnabled(false);

        lblGroupId.setLabelFor(txtGroupId);
        org.openide.awt.Mnemonics.setLocalizedText(lblGroupId, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_GroupId")); // NOI18N

        lblVersion.setLabelFor(txtVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblVersion, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_Version")); // NOI18N

        txtVersion.setText("1.0-SNAPSHOT");

        lblPackage.setLabelFor(txtPackage);
        org.openide.awt.Mnemonics.setLocalizedText(lblPackage, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_Package")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_Optional")); // NOI18N

        lblAdditionalProps.setLabelFor(tblAdditionalProps);
        org.openide.awt.Mnemonics.setLocalizedText(lblAdditionalProps, "jLabel2");

        tblAdditionalProps.setModel(createPropModel());
        tblAdditionalProps.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(tblAdditionalProps);
        tblAdditionalProps.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblAdditionalProps.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.tblAdditionalProps.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnSetupNewer, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BTN_SetupNewer.text")); // NOI18N
        btnSetupNewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetupNewerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAdditionalsLayout = new javax.swing.GroupLayout(pnlAdditionals);
        pnlAdditionals.setLayout(pnlAdditionalsLayout);
        pnlAdditionalsLayout.setHorizontalGroup(
            pnlAdditionalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdditionalsLayout.createSequentialGroup()
                .addComponent(lblAdditionalProps)
                .addContainerGap(511, Short.MAX_VALUE))
            .addGroup(pnlAdditionalsLayout.createSequentialGroup()
                .addComponent(btnSetupNewer)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
        );
        pnlAdditionalsLayout.setVerticalGroup(
            pnlAdditionalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdditionalsLayout.createSequentialGroup()
                .addComponent(lblAdditionalProps)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSetupNewer))
        );

        btnSetupNewer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.btnSetupNewer.AccessibleContext.accessibleDescription")); // NOI18N

        lblEEVersion.setLabelFor(comboEEVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblEEVersion, org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "LBL_JavaEE")); // NOI18N

        comboEEVersion.setModel(new DefaultComboBoxModel(panel.getEELevels()));
        comboEEVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboEEVersionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPackage)
                    .addComponent(lblVersion)
                    .addComponent(lblGroupId)
                    .addComponent(lblArtifactId)
                    .addComponent(createdFolderLabel)
                    .addComponent(projectLocationLabel)
                    .addComponent(projectNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(txtPackage, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(txtVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(browseButton)
                    .addComponent(jLabel1)))
            .addComponent(pnlAdditionals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblEEVersion)
                .addGap(18, 18, 18)
                .addComponent(comboEEVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(253, 253, 253))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(projectLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdFolderLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEEVersion)
                    .addComponent(comboEEVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblArtifactId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGroupId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPackage)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAdditionals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.projectNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.projectLocationTextField.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.createdFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        txtArtifactId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtArtifactId.AccessibleContext.accessibleDescription")); // NOI18N
        txtGroupId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtGroupId.AccessibleContext.accessibleDescription")); // NOI18N
        txtVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtVersion.AccessibleContext.accessibleDescription")); // NOI18N
        txtPackage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.txtPackage.AccessibleContext.accessibleDescription")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicPanelVisual.class, "BasicPanelVisual.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    @Messages("TIT_Select_Project_Location=Select Project Location")
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) { //NOI18N
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(TIT_Select_Project_Location());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            panel.fireChangeEvent();
        }
        
    }//GEN-LAST:event_browseButtonActionPerformed

    private void comboEEVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboEEVersionActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_comboEEVersionActionPerformed

    private void btnSetupNewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetupNewerActionPerformed
        OptionsDisplayer.getDefault().open(OptionsDisplayer.ADVANCED + "/" + MavenOptionController.OPTIONS_SUBPATH); //NOI18N
        panel.getValidationGroup().validateAll();
    }//GEN-LAST:event_btnSetupNewerActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton btnSetupNewer;
    private javax.swing.JComboBox comboEEVersion;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAdditionalProps;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblEEVersion;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblPackage;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel pnlAdditionals;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JTable tblAdditionalProps;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtPackage;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
        tblAdditionalProps.setVisible(false);
        lblAdditionalProps.setVisible(false);
        jScrollPane1.setVisible(false);
        // for maven version checking
        SwingUtilities.getWindowAncestor(this).addWindowFocusListener(this);
    }

    @Messages("ERR_multibyte=Multibyte chars not permitted in project name or Maven coordinates.")
    static boolean containsMultiByte (String text, WizardDescriptor wd) {
        char[] textChars = text.toCharArray();
        for (int i = 0; i < textChars.length; i++) {
            if ((int)textChars[i] > 255) {
                wd.putProperty(ERROR_MSG, ERR_multibyte());
                return true;
            }

        }
        return false;
    }

    private boolean isMavenTooOld () {
        ArtifactVersion version = getCommandLineMavenVersion();
        return version != null ? BORDER_VERSION.compareTo(version) > 0 : false;
    }

    private ArtifactVersion getCommandLineMavenVersion () {
        synchronized (MAVEN_VERSION_LOCK) {
            if (!askedForVersion) {
                askedForVersion = true;
                // obtain version asynchronously, as it takes some time
                RPgetver.post(this);
            }
            return mavenVersion;
        }
    }
    
    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        
        d.putProperty("projdir", new File(folder)); //NOI18N
        d.putProperty("name", name); //NOI18N
        d.putProperty("artifactId", txtArtifactId.getText().trim()); //NOI18N
        d.putProperty("groupId", txtGroupId.getText().trim()); //NOI18N
        MavenSettings.getDefault().setLastArchetypeGroupId(txtGroupId.getText().trim());
        d.putProperty("version", txtVersion.getText().trim()); //NOI18N
        d.putProperty("package", txtPackage.getText().trim()); //NOI18N
        if (tblAdditionalProps.isVisible()) {
            TableModel mdl = tblAdditionalProps.getModel();
            HashMap<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < mdl.getRowCount(); i++) {
                map.put((String)mdl.getValueAt(i, 0), (String)mdl.getValueAt(i, 1));
            }
            d.putProperty(ArchetypeWizardUtils.ADDITIONAL_PROPS, map);
        }
        if (panel.getArchetypes() != null) {
            d.putProperty(ChooseArchetypePanel.PROP_ARCHETYPE, getArchetype(d));
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                panel.getValidationGroup().removeValidationGroup(vg);
            }
        });
    }
    
    @Messages({
        "TXT_MavenProjectName=mavenproject{0}",
        "TXT_Checking1=Checking additional creation properties..."
    })
    void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty("projdir"); //NOI18N
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String) settings.getProperty("name"); //NOI18N

        if(projectName == null) {
            int baseCount = 1;
            while ((projectName = validFreeProjectName(projectLocation, TXT_MavenProjectName(baseCount))) == null) {
                baseCount++;                
            }
        }
        
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
        // skip additional properties if direct known archetypes without additional props used
        if (panel.areAdditional()) {
            final Archetype arch = getArchetype(settings);
            lblAdditionalProps.setText(TXT_Checking1());
            lblAdditionalProps.setVisible(true);
            tblAdditionalProps.setVisible(false);
            jScrollPane1.setVisible(false);
            RPprep.post(new Runnable() {
                public void run() {
                    prepareAdditionalProperties(arch);
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                panel.getValidationGroup().addValidationGroup(vg, true);
            }
        });
    }

    @Messages({
        "COL_Key=Key",
        "COL_Value=Value",
        "TXT_Checking2=A&dditional Creation Properties:"
    })
    private void prepareAdditionalProperties(Archetype arch) {
        final DefaultTableModel dtm = new DefaultTableModel();
        dtm.addColumn(COL_Key());
        dtm.addColumn(COL_Value());
        try {
            Artifact art = downloadArchetype(arch);
            File fil = art.getFile();
            if (fil.exists()) {
                Map<String, String> props = ArchetypeWizardUtils.getAdditionalProperties(art);
                for (String key : props.keySet()) {
                    String defVal = props.get(key);
                    dtm.addRow(new Object[] {key, defVal == null ? "" : defVal });
                }
            }
        } catch (ArtifactResolutionException ex) {
            //#143026
            Logger.getLogger( BasicPanelVisual.class.getName()).log( Level.FINE, "Cannot download archetype", ex);
        } catch (ArtifactNotFoundException ex) {
            //#143026
            Logger.getLogger( BasicPanelVisual.class.getName()).log( Level.FINE, "Cannot download archetype", ex);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (dtm.getRowCount() > 0) {
                    Mnemonics.setLocalizedText(lblAdditionalProps, TXT_Checking2());
                    lblAdditionalProps.setVisible(true);
                    jScrollPane1.setVisible(true);
                    tblAdditionalProps.setModel(dtm);
                    tblAdditionalProps.setVisible(true);
                } else {
                    tblAdditionalProps.setVisible(false);
                    lblAdditionalProps.setVisible(false);
                    jScrollPane1.setVisible(false);
                }
            }
        });
    }

    private Archetype getArchetype (WizardDescriptor settings) {
        if (arch != null) {
            return arch;
        }
        Archetype[] archs = panel.getArchetypes();
        if (archs == null) {
            return (Archetype)settings.getProperty(ChooseArchetypePanel.PROP_ARCHETYPE);
        } else {
            return archs[Math.max(0, comboEEVersion.getSelectedIndex())];
        }
    }

    @Messages("Handle_Download=Downloading Archetype")
    private Artifact downloadArchetype(Archetype arch) throws ArtifactResolutionException, ArtifactNotFoundException {
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        Artifact art = online.createArtifact(
                arch.getGroupId(), 
                arch.getArtifactId(), 
                arch.getVersion(), 
                "jar", //NOI18N
                "maven-archetype"); //NOI18N
        Artifact pom = online.createArtifact(
                arch.getGroupId(), 
                arch.getArtifactId(), 
                arch.getVersion(), 
                "pom", //NOI18N
                "pom"); //NOI18N
        
        //hack to get the right extension for the right packaging without the plugin.
        art.setArtifactHandler(new ArtifactHandler() {
            public String getExtension() {
                return "jar"; //NOI18N
            }
            public String getDirectory() {
                return null;
            }
            public String getClassifier() {
                return null;
            }
            public String getPackaging() {
                return "maven-archetype"; //NOI18N
            }
            public boolean isIncludesDependencies() {
                return false;
            }
            public String getLanguage() {
                return "java"; //NOI18N
            }
            public boolean isAddedToClasspath() {
                return false;
            }
        });
        List<ArtifactRepository> repos;
        if (arch.getRepository() == null) {
            repos = Collections.<ArtifactRepository>singletonList(EmbedderFactory.createRemoteRepository(online, RepositoryPreferences.REPO_CENTRAL, "central"));//NOI18N
        } else {
            
            repos = Collections.<ArtifactRepository>singletonList(EmbedderFactory.createRemoteRepository(online, arch.getRepository(), "custom-repo"));//NOI18N
        }
        AggregateProgressHandle hndl = AggregateProgressFactory.createHandle(Handle_Download(),
                new ProgressContributor[] {
                    AggregateProgressFactory.createProgressContributor("zaloha") },  //NOI18N
                ProgressTransferListener.cancellable(), null);
        ProgressTransferListener.setAggregateHandle(hndl);
        try {
            hndl.start();
//TODO how to rewrite to track progress?
//            try {
//                WagonManager wagon = online.getPlexusContainer().lookup(WagonManager.class);
//                wagon.setDownloadMonitor(new ProgressTransferListener());
//            } catch (ComponentLookupException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            online.resolve(pom, repos, online.getLocalRepository());
            online.resolve(art, repos, online.getLocalRepository());
        } catch (ThreadDeath d) { // download interrupted
        } finally {
            hndl.finish();
            ProgressTransferListener.clearAggregateHandle();
        }
        //#154913
        RepositoryInfo info = RepositoryPreferences.getInstance().getRepositoryInfoById(RepositoryPreferences.LOCAL_REPO_ID);
        if (info != null) {
            RepositoryIndexer.updateIndexWithArtifacts(info, Collections.singletonList(art));
        }
        return art;
    }
    
    private TableModel createPropModel() {
        return new DefaultTableModel();
    }
    
    
    // Implementation of DocumentListener --------------------------------------
    
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    /** Handles changes in the Project name and project directory, */
    private void updateTexts(DocumentEvent e) {
        
        Document doc = e.getDocument();
        
        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name
            
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            
            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
            //}
            
        }
        
        if (projectNameTextField.getDocument() == doc) {
            String projName = projectNameTextField.getText().trim();
            txtArtifactId.setText(projName.replaceAll(" ", ""));
        }
        
        if (!changedPackage && (projectNameTextField.getDocument() == doc || txtGroupId.getDocument() == doc)) {
            txtPackage.getDocument().removeDocumentListener(this);
            txtPackage.setText(getPackageName(txtGroupId.getText() + "." + projectNameTextField.getText())); //NOI18N
            txtPackage.getDocument().addDocumentListener(this);
        }
        
        if (txtPackage.getDocument() == doc) {
            changedPackage = txtPackage.getText().trim().length() != 0;
        }
        if (vg != null) {
            vg.validateAll(); // Notify that the panel changed
        }
    }
    
    private String validFreeProjectName(File parentFolder, String name) {
        File file = new File (parentFolder, name);
        return file.exists() ? null : name;
    }
    


    static String getPackageName (String displayName) {
        StringBuilder builder = new StringBuilder ();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if ((!firstLetter && Character.isJavaIdentifierPart (c))
                    || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                builder.append(c);
                continue;
            }
            if (!firstLetter && c == '.') {
                firstLetter = true;
                builder.append(c);
                continue;
            }
        }
        String toRet =  builder.length() == 0 ? "pkg" : builder.toString(); //NOI18N
        return toRet;
    }

    /*** Implementation of WindowFocusListener ***/

    public void windowGainedFocus(WindowEvent e) {
        // trigger re-check of maven version
        askedForVersion = false;
        getCommandLineMavenVersion();
    }

    public void windowLostFocus(WindowEvent e) {
    }

    /*** Implementation of Runnable, checks Maven version ***/

    public void run() {
        if (!EventQueue.isDispatchThread()) {
            // phase one, outside EQ thread
            String version = MavenSettings.getCommandLineMavenVersion();
            mavenVersion = version != null ? new DefaultArtifactVersion(version.trim()) : null;
            // trigger revalidation -> phase two
            SwingUtilities.invokeLater(this);
        } else {
            // phase two, inside EQ thread
            vg.validateAll();
        }
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class RunDialogPanel extends javax.swing.JPanel implements PropertyChangeListener {
    private DocumentListener modifiedValidateDocumentListener = null;
    private Project[] projectChoices = null;
    private JButton actionButton;
    private final boolean isRun;
    
    private static String lastSelectedExecutable = null;
    private static Project lastSelectedProject = null;
    
    private static DefaultPicklistModel picklist = null;
    private static String picklistHomeDir = null;
    private static final String picklistName = "executables"; // NOI18N
    private boolean isValidating = false;
    
    private void initAccessibility() {
        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("RUN_DIALOG_PANEL_AD"));
        executableTextField.getAccessibleContext().setAccessibleDescription(getString("EXECUTABLE_AD"));
        executableBrowseButton.getAccessibleContext().setAccessibleDescription(getString("BROWSE_BUTTON_AD"));
        projectComboBox.getAccessibleContext().setAccessibleDescription(getString("ASSOCIATED_PROJECT_AD"));
        runDirectoryTextField.getAccessibleContext().setAccessibleDescription(getString("RUN_DIRECTORY_LABEL_AD"));
        runDirectoryBrowseButton.getAccessibleContext().setAccessibleDescription(getString("RUN_DIRECTORY_BUTTON_AD"));
        argumentTextField.getAccessibleContext().setAccessibleDescription(getString("ARGUMENTS_LABEL_AD"));
        environmentTextField.getAccessibleContext().setAccessibleDescription(getString("ENVIRONMENT_LABEL_AD"));
    }
    
    public RunDialogPanel(String exePath, JButton actionButton, boolean isRun) {
        this.actionButton = actionButton;
        this.isRun = isRun;
        initialize(exePath);
        errorLabel.setText(""); //NOI18N
        initAccessibility();
    }
    
    private void initialize(String exePath) {
        initComponents();
        errorLabel.setForeground(javax.swing.UIManager.getColor("nb.errorForeground")); // NOI18N
        modifiedValidateDocumentListener = new ModifiedValidateDocumentListener();
        //modifiedRunDirectoryListener = new ModifiedRunDirectoryListener();
        if (exePath != null) {
            executableTextField.setText(exePath);
        }
        if (isRun) {
            guidanceTextarea.setText(getString("DIALOG_GUIDANCETEXT"));
        } else {
            guidanceTextarea.setText(getString("DIALOG_GUIDANCETEXT_CREATE"));
        }
        String[] savedExePaths = getExecutablePicklist().getElementsDisplayName();
        String feed = null;
        if (exePath != null) {
            feed = exePath;
        } else if (savedExePaths.length > 0) {
            feed = savedExePaths[0];
        } else {
            feed = ""; // NOI18N
        }
        
        executableTextField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        runDirectoryTextField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        projectNameField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        projectLocationField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        initGui();
        
        guidanceTextarea.setBackground(getBackground());
        setPreferredSize(new java.awt.Dimension(700, (int)getPreferredSize().getHeight()));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        guidanceTextarea = new javax.swing.JTextArea();
        executableLabel1 = new javax.swing.JLabel();
        executableBrowseButton = new javax.swing.JButton();
        executableTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();
        errorLabel = new javax.swing.JLabel();
        runDirectoryLabel = new javax.swing.JLabel();
        runDirectoryTextField = new javax.swing.JTextField();
        runDirectoryBrowseButton = new javax.swing.JButton();
        argumentLabel = new javax.swing.JLabel();
        argumentTextField = new javax.swing.JTextField();
        environmentLabel = new javax.swing.JLabel();
        environmentTextField = new javax.swing.JTextField();
        projectkindLabel = new javax.swing.JLabel();
        projectKind = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        projectNameLabel = new javax.swing.JLabel();
        projectLocationLabel = new javax.swing.JLabel();
        projectFolderLabel = new javax.swing.JLabel();
        projectNameField = new javax.swing.JTextField();
        projectLocationField = new javax.swing.JTextField();
        projectFolderField = new javax.swing.JTextField();
        projectLocationButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        guidanceTextarea.setEditable(false);
        guidanceTextarea.setLineWrap(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle"); // NOI18N
        guidanceTextarea.setText(bundle.getString("DIALOG_GUIDANCETEXT")); // NOI18N
        guidanceTextarea.setWrapStyleWord(true);
        guidanceTextarea.setMinimumSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(guidanceTextarea, gridBagConstraints);

        executableLabel1.setLabelFor(executableTextField);
        org.openide.awt.Mnemonics.setLocalizedText(executableLabel1, bundle.getString("EXECUTABLE_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(executableLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(executableBrowseButton, bundle.getString("BROWSE_BUTTON_TXT")); // NOI18N
        executableBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executableBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 12);
        add(executableBrowseButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(executableTextField, gridBagConstraints);

        projectLabel.setLabelFor(projectComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, bundle.getString("ASSOCIATED_PROJECT_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        add(projectLabel, gridBagConstraints);

        projectComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
        add(projectComboBox, gridBagConstraints);

        errorLabel.setText(bundle.getString("ERROR_NOTAEXEFILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 12, 0, 12);
        add(errorLabel, gridBagConstraints);

        runDirectoryLabel.setLabelFor(runDirectoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(runDirectoryLabel, bundle.getString("RUN_DIRECTORY_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(runDirectoryLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(runDirectoryTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(runDirectoryBrowseButton, bundle.getString("RUN_DIRECTORY_BUTTON_TXT")); // NOI18N
        runDirectoryBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runDirectoryBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(runDirectoryBrowseButton, gridBagConstraints);

        argumentLabel.setLabelFor(argumentTextField);
        org.openide.awt.Mnemonics.setLocalizedText(argumentLabel, bundle.getString("ARGUMENTS_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(argumentLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(argumentTextField, gridBagConstraints);

        environmentLabel.setLabelFor(environmentTextField);
        org.openide.awt.Mnemonics.setLocalizedText(environmentLabel, bundle.getString("ENVIRONMENT_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(environmentLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(environmentTextField, gridBagConstraints);

        projectkindLabel.setLabelFor(projectKind);
        org.openide.awt.Mnemonics.setLocalizedText(projectkindLabel, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "ProjectKindName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectkindLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectKind, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 12);
        add(jSeparator1, gridBagConstraints);

        projectNameLabel.setLabelFor(projectNameField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.name.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectNameLabel, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.location.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectLocationLabel, gridBagConstraints);

        projectFolderLabel.setLabelFor(projectFolderField);
        projectFolderLabel.setText(org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.folder.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(projectFolderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectNameField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(projectLocationField, gridBagConstraints);

        projectFolderField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectFolderField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(projectLocationButton, org.openide.util.NbBundle.getMessage(RunDialogPanel.class, "RunDialogPanel.project.location.button")); // NOI18N
        projectLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectLocationButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(projectLocationButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void runDirectoryBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runDirectoryBrowseButtonActionPerformed
        String seed;
        if (runDirectoryTextField.getText().length() > 0) {
            seed = runDirectoryTextField.getText();
        }
        else {
            seed = getExecutablePath();
        }
        // Show the file chooser
        FileChooser fileChooser = new FileChooser(
                getString("SelectWorkingDir"),
                getString("SelectLabel"),
                FileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                true
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == FileChooser.CANCEL_OPTION) {
            return;
        }
        runDirectoryTextField.setText(fileChooser.getSelectedFile().getPath());
    }//GEN-LAST:event_runDirectoryBrowseButtonActionPerformed
    
    private void projectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectComboBoxActionPerformed
        int selectedIndex = projectComboBox.getSelectedIndex();
        if (selectedIndex == 0) {
            if (new File(executableTextField.getText()).getParentFile() != null) {
                runDirectoryTextField.setText(new File(executableTextField.getText()).getParentFile().getPath());
            } else {
                if (!isValidating) {
                    executableTextField.setText(""); // NOI18N
                }
            }
            argumentTextField.setText(""); // NOI18N
            environmentTextField.setText(""); // NOI18N
            projectKind.setEnabled(true);
            projectNameField.setEnabled(true);
            projectLocationField.setEnabled(true);
            projectLocationButton.setEnabled(true);
            projectFolderField.setEnabled(true);
            projectLocationField.setText(ProjectGenerator.getDefaultProjectFolder());
            projectNameField.setText(ProjectGenerator.getValidProjectName(projectLocationField.getText(), new File(getExecutablePath()).getName()));
        }
        else {
            projectKind.setEnabled(false);
            projectNameField.setEnabled(false);
            projectLocationField.setEnabled(false);
            projectLocationButton.setEnabled(false);
            projectFolderField.setEnabled(false);
            Project project = projectChoices[projectComboBox.getSelectedIndex()-1];
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp == null) {
                return;
            }
            MakeConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
            MakeConfiguration conf = projectDescriptor.getActiveConfiguration();
            RunProfile runProfile = conf.getProfile();
            runDirectoryTextField.setText(runProfile.getRunDirectory());
            argumentTextField.setText(runProfile.getArgsFlat());
            environmentTextField.setText(runProfile.getEnvironment().toString());
        }
    }//GEN-LAST:event_projectComboBoxActionPerformed
    
    private void executableBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executableBrowseButtonActionPerformed
        String seed = getExecutablePath();
        if (seed.length() == 0 && FileChooser.getCurrentChooserFile() != null) {
            seed = FileChooser.getCurrentChooserFile().getPath();
        }
        if (seed.length() == 0) {
            seed = System.getProperty("user.home");
        } // NOI18N
        
        FileFilter[] filter;
        if (Utilities.isWindows()){
            filter = new FileFilter[] {FileFilterFactory.getPeExecutableFileFilter()};
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            filter = new FileFilter[] {FileFilterFactory.getMacOSXExecutableFileFilter()};
        } else {
            filter = new FileFilter[] {FileFilterFactory.getElfExecutableFileFilter()};
        }
        // Show the file chooser
        FileChooser fileChooser = new FileChooser(
                getString("SelectExecutable"),
                getString("SelectLabel"),
                FileChooser.FILES_ONLY,
                filter,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == FileChooser.CANCEL_OPTION) {
            return;
        }
        executableTextField.setText(fileChooser.getSelectedFile().getPath());
    }//GEN-LAST:event_executableBrowseButtonActionPerformed

    private void projectLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectLocationButtonActionPerformed
        String path = this.projectLocationField.getText();
        FileChooser chooser = new FileChooser(
                getString("RunDialogPanel.Title_SelectProjectLocation"),
                null, JFileChooser.DIRECTORIES_ONLY, null, path, true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            projectLocationField.setText(projectDir.getAbsolutePath());
        }
    }//GEN-LAST:event_projectLocationButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel argumentLabel;
    private javax.swing.JTextField argumentTextField;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JTextField environmentTextField;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton executableBrowseButton;
    private javax.swing.JLabel executableLabel1;
    private javax.swing.JTextField executableTextField;
    private javax.swing.JTextArea guidanceTextarea;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox projectComboBox;
    private javax.swing.JTextField projectFolderField;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JComboBox projectKind;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JButton projectLocationButton;
    private javax.swing.JTextField projectLocationField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectNameField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JLabel projectkindLabel;
    private javax.swing.JButton runDirectoryBrowseButton;
    private javax.swing.JLabel runDirectoryLabel;
    private javax.swing.JTextField runDirectoryTextField;
    // End of variables declaration//GEN-END:variables
    
    private Project[] getOpenedProjects() {
        List<Project> res = new ArrayList<Project>();
        for(Project p :OpenProjects.getDefault().getOpenProjects()) {
            if (p.getLookup().lookup(ConfigurationDescriptorProvider.class) != null) {
                res.add(p);
            }
        }
        return res.toArray(new Project[res.size()]);
    }

    private void initGui() {
        ActionListener projectComboBoxActionListener = projectComboBox.getActionListeners()[0];
        projectComboBox.removeActionListener(projectComboBoxActionListener);
        projectComboBox.removeAllItems();
        projectComboBox.addItem(getString("NO_PROJECT")); // always first
        int index = 0;
        projectComboBox.setVisible(isRun);
        projectLabel.setVisible(isRun);
        if (isRun) {
            projectChoices = getOpenedProjects();
            for (int i = 0; i < projectChoices.length; i++) {
                projectComboBox.addItem(ProjectUtils.getInformation(projectChoices[i]).getName());
            }

            // preselect project ???
            if (lastSelectedExecutable != null && getExecutablePath().equals(lastSelectedExecutable) && lastSelectedProject != null) {
                for (int i = 0; i < projectChoices.length; i++) {
                    if (projectChoices[i] == lastSelectedProject) {
                        index = i+1;
                        break;
                    }
                }
            }
        }
        projectComboBox.setSelectedIndex(index);
        projectComboBox.addActionListener(projectComboBoxActionListener);
        projectComboBoxActionPerformed(null);
        //validateRunDirectory();
        projectKind.removeAllItems();
        projectKind.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.Minimal));
        projectKind.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.IncludeDependencies));
        projectKind.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.CreateDependencies));
        projectKind.setSelectedIndex(1);

    }
    
    private boolean validateExecutable() {
        String exePath = getExecutablePath();
        File exeFile = new File(exePath);
        if (!exeFile.exists()) {
            setError("ERROR_DONTEXIST", true); // NOI18N
            return false;
        }
        if (exeFile.isDirectory()) {
            setError("ERROR_NOTAEXEFILE", true); // NOI18N
            return false;
        }
        return true;
    }
    
    private boolean validateProjectLocation() {
        if (!PanelProjectLocationVisual.isValidProjectName(projectNameField.getText())) {
            setError("RunDialogPanel.MSG_IllegalProjectName", false); // NOI18N
            return false;
        }
        if (!CndPathUtilitities.isPathAbsolute(projectLocationField.getText())) {
            setError("RunDialogPanel.MSG_IllegalProjectLocation", false); // NOI18N
            return false;
        }
        File f = CndFileUtils.createLocalFile(projectLocationField.getText()).getAbsoluteFile();
        if (PanelProjectLocationVisual.getCanonicalFile(f) == null) {
            setError("RunDialogPanel.MSG_IllegalProjectLocation", false); // NOI18N
            return false;
        }
        final File destFolder = PanelProjectLocationVisual.getCanonicalFile(CndFileUtils.createLocalFile(projectFolderField.getText()).getAbsoluteFile()); // project folder always local
        if (destFolder == null) {
            setError("RunDialogPanel.MSG_IllegalProjectName", false); // NOI18N
            return false;
        }
        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            setError("RunDialogPanel.MSG_ProjectFolderReadOnly", false); // NOI18N
            return false;
        }
        if (destFolder.exists()) {
            if (destFolder.isFile()) {
                setError("RunDialogPanel.MSG_NotAFolder", false); // NOI18N
                return false;
            }
            if (CndFileUtils.isValidLocalFile(destFolder, MakeConfiguration.NBPROJECT_FOLDER)) {
                setError("RunDialogPanel.MSG_ProjectfolderNotEmpty", false, MakeConfiguration.NBPROJECT_FOLDER); // NOI18N
                return false;
            }
        }
        return true;
    }
    
    private void setError(String errorMsg, boolean disable, String ... args) {
        setErrorMsg(getString(errorMsg, args));
        if (disable) {
            runDirectoryBrowseButton.setEnabled(false);
            runDirectoryLabel.setEnabled(false);
            runDirectoryTextField.setEnabled(false);
            argumentLabel.setEnabled(false);
            argumentTextField.setEnabled(false);
            environmentLabel.setEnabled(false);
            environmentTextField.setEnabled(false);
            projectComboBox.setEnabled(false);
            projectKind.setEnabled(false);
            projectNameField.setEnabled(false);
            projectLocationField.setEnabled(false);
            projectLocationButton.setEnabled(false);
            projectFolderField.setEnabled(false);
        }
        actionButton.setEnabled(false);
    }
    
    private void clearError() {
        setErrorMsg(" "); // NOI18N
        
        runDirectoryBrowseButton.setEnabled(true);
        runDirectoryLabel.setEnabled(true);
        runDirectoryTextField.setEnabled(true);
        argumentLabel.setEnabled(true);
        argumentTextField.setEnabled(true);
        environmentLabel.setEnabled(true);
        environmentTextField.setEnabled(true);
        projectComboBox.setEnabled(true);
        if (projectComboBox.getSelectedIndex() == 0) {
            projectKind.setEnabled(true);
            projectNameField.setEnabled(true);
            projectLocationField.setEnabled(true);
            projectLocationButton.setEnabled(true);
            projectFolderField.setEnabled(true);
        } else {
            projectKind.setEnabled(false);
            projectNameField.setEnabled(false);
            projectLocationField.setEnabled(false);
            projectLocationButton.setEnabled(false);
            projectFolderField.setEnabled(false);
        }
        
        actionButton.setEnabled(true);
    }
    
    private void validateFields(javax.swing.event.DocumentEvent documentEvent) {
        isValidating = true;
        try {
            clearError();
            if (documentEvent.getDocument() == executableTextField.getDocument()) {
                projectComboBox.setSelectedIndex(0);
                if (!validateExecutable()) {
                    return;
                }
                runDirectoryTextField.setText(new File(executableTextField.getText()).getParentFile().getPath());
            } else if (documentEvent.getDocument() == projectNameField.getDocument() ||
                       documentEvent.getDocument() == projectLocationField.getDocument()) {
                String projectName = projectNameField.getText().trim();
                String projectFolder = projectLocationField.getText().trim();
                while (projectFolder.endsWith("/")) { // NOI18N
                    projectFolder = projectFolder.substring(0, projectFolder.length() - 1);
                }
                projectFolderField.setText(projectFolder + File.separatorChar + projectName);
                if (!validateProjectLocation()) {
                    return;
                }
            }
        } finally {
            isValidating = false;
        }
    }

    // ModifiedDocumentListener
    private final class ModifiedValidateDocumentListener implements DocumentListener {
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
        
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
    }
    
    public Project getSelectedProject() {
        Project project;
        lastSelectedExecutable = getExecutablePath();
        if (projectComboBox.getSelectedIndex() > 0) {
            lastSelectedProject = projectChoices[projectComboBox.getSelectedIndex()-1];
            project = lastSelectedProject;
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
            MakeConfiguration conf = projectDescriptor.getActiveConfiguration();
            updateRunProfile(conf.getBaseDir(), conf.getProfile());
        } else {
            try {
                ProgressHandle progress = ProgressHandleFactory.createHandle(getString("CREATING_PROJECT_PROGRESS")); // NOI18N
                progress.start();
                try {
                    String projectParentFolder = projectLocationField.getText().trim();
                    String projectName = projectNameField.getText().trim();
                    String baseDir = projectFolderField.getText().trim();
                    MakeConfiguration conf = new MakeConfiguration(baseDir, "Default", MakeConfiguration.TYPE_MAKEFILE, // NOI18N
                            ExecutionEnvironmentFactory.getLocal().getHost());
                    // Working dir
                    String wd = new File(getExecutablePath()).getParentFile().getPath();
                    wd = CndPathUtilitities.toRelativePath(baseDir, wd);
                    wd = CndPathUtilitities.normalizeSlashes(wd);
                    conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(wd);
                    // Executable
                    String exe = getExecutablePath();
                    exe = CndPathUtilitities.toRelativePath(baseDir, exe);
                    exe = CndPathUtilitities.normalizeSlashes(exe);
                    conf.getMakefileConfiguration().getOutput().setValue(exe);
                    updateRunProfile(baseDir, conf.getProfile());
                    ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectParentFolder);
                    prjParams.setOpenFlag(false)
                             .setConfiguration(conf)
                             .setImportantFiles(Collections.<String>singletonList(exe).iterator())
                             .setMakefileName(""); //NOI18N
                    project = ProjectGenerator.createBlankProject(prjParams);
                    lastSelectedProject = project;
                    OpenProjects.getDefault().addPropertyChangeListener(this);
                    OpenProjects.getDefault().open(new Project[]{project}, false);
                    OpenProjects.getDefault().setMainProject(project);
                } finally {
                    progress.finish();
                }
            } catch (Exception e) {
                project = null;
            }
            lastSelectedProject = project;
        }
        return project;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                OpenProjects.getDefault().removePropertyChangeListener(this);
                if (lastSelectedProject == null) {
                    return;
                }
                IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                if (extension != null) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("DW:buildResult", getExecutablePath()); // NOI18N
                    map.put("DW:consolidationLevel", "file"); // NOI18N
                    map.put("DW:rootFolder", lastSelectedProject.getProjectDirectory().getPath()); // NOI18N
                    IteratorExtension.ProjectKind kind = ((ProjectKindItem)projectKind.getSelectedItem()).kind;
                    extension.discoverProject(map, lastSelectedProject, kind); // NOI18N
                }
            }
        }
    }

    private void updateRunProfile(String baseDir, RunProfile runProfile) {
        // Arguments
        runProfile.setArgs(argumentTextField.getText());
        // Working dir
        String wd = runDirectoryTextField.getText();
        wd = CndPathUtilitities.toRelativePath(baseDir, wd);
        wd = CndPathUtilitities.normalizeSlashes(wd);
        runProfile.setRunDirectory(wd);
        // Environment
        Env env = runProfile.getEnvironment();
	env.removeAll();
        env.decode(environmentTextField.getText());
    }
    
    public String getExecutablePath() {
        return executableTextField.getText();
    }
    
    private void setErrorMsg(String msg) {
        errorLabel.setText(msg);
    }
    
    private static DefaultPicklistModel getExecutablePicklist() {
        if (picklist == null) {
            picklistHomeDir = System.getProperty("netbeans.user") + File.separator + "var" + File.separator + "picklists"; // NOI18N
            picklist = (DefaultPicklistModel)DefaultPicklistModel.restorePicklist(picklistHomeDir, picklistName);
            if (picklist == null) {
                picklist = new DefaultPicklistModel(16);
            }
        }
        return picklist;
    }
    
    public static void addElementToExecutablePicklist(String exePath) {
        getExecutablePicklist().addElement(exePath);
        getExecutablePicklist().savePicklist(picklistHomeDir, picklistName);
    }
    
    /** Look up i18n strings here */
    private String getString(String s, String ... args) {
        return NbBundle.getMessage(RunDialogPanel.class, s, args);
    }
    
    public boolean asynchronous() {
        return false;
    }

    private final class ProjectKindItem {
        private final IteratorExtension.ProjectKind kind;
        ProjectKindItem(IteratorExtension.ProjectKind kind) {
            this.kind = kind;
        }

        @Override
        public String toString() {
            return RunDialogPanel.this.getString("ProjectItemKind_"+kind);
        }
    }
}

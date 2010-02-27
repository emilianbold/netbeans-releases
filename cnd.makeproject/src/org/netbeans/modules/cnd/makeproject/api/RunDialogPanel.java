/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class RunDialogPanel extends javax.swing.JPanel {
    private DocumentListener modifiedValidateDocumentListener = null;
    private Project[] projectChoices = null;
    private boolean executableReadOnly = true;
    private JButton actionButton;
    
    private static String lastSelectedExecutable = null;
    private static Project lastSelectedProject = null;
    
    private static DefaultPicklistModel picklist = null;
    private static String picklistHomeDir = null;
    private static final String picklistName = "executables"; // NOI18N
    private boolean isValidating = false;
    
    public RunDialogPanel() {
        initialize(null, false);
        errorLabel.setText(""); //NOI18N
        initAccessibility();
    }
    
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
    
    public RunDialogPanel(String exePath, boolean executableReadOnly, JButton actionButton) {
        this.actionButton = actionButton;
        initialize(exePath, executableReadOnly);
        errorLabel.setText(""); //NOI18N
        initAccessibility();
    }
    
    private void initialize(String exePath, boolean executableReadOnly) {
        initComponents();
        errorLabel.setForeground(javax.swing.UIManager.getColor("nb.errorForeground")); // NOI18N
        this.executableReadOnly = executableReadOnly;
        modifiedValidateDocumentListener = new ModifiedValidateDocumentListener();
        //modifiedRunDirectoryListener = new ModifiedRunDirectoryListener();
        if (executableReadOnly) {
//            executableTextField.setEditable(false);
//            executableBrowseButton.setEnabled(false);
        }
        if (exePath != null) {
            executableTextField.setText(exePath);
        }
        guidanceTextarea.setText(getString("DIALOG_GUIDANCETEXT"));
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
        initGui();
        runDirectoryTextField.getDocument().addDocumentListener(modifiedValidateDocumentListener);
        
        guidanceTextarea.setBackground(getBackground());
        setPreferredSize(new java.awt.Dimension(700, (int)getPreferredSize().getHeight()));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

        setLayout(new java.awt.GridBagLayout());

        guidanceTextarea.setEditable(false);
        guidanceTextarea.setLineWrap(true);
        guidanceTextarea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("DIALOG_GUIDANCETEXT"));
        guidanceTextarea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(guidanceTextarea, gridBagConstraints);

        executableLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("EXECUTABLE_MN").charAt(0));
        executableLabel1.setLabelFor(executableTextField);
        executableLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("EXECUTABLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 0);
        add(executableLabel1, gridBagConstraints);

        executableBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("BROWSE_BUTTON_MN").charAt(0));
        executableBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("BROWSE_BUTTON_TXT"));
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
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
        add(executableBrowseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(executableTextField, gridBagConstraints);

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ASSOCIATED_PROJECT_MN").charAt(0));
        projectLabel.setLabelFor(projectComboBox);
        projectLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ASSOCIATED_PROJECT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
        add(projectComboBox, gridBagConstraints);

        errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ERROR_NOTAEXEFILE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 12, 0, 12);
        add(errorLabel, gridBagConstraints);

        runDirectoryLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("RUN_DIRECTORY_LABEL_MN").charAt(0));
        runDirectoryLabel.setLabelFor(runDirectoryTextField);
        runDirectoryLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("RUN_DIRECTORY_LABEL_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
        add(runDirectoryLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 4, 0, 0);
        add(runDirectoryTextField, gridBagConstraints);

        runDirectoryBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("RUN_DIRECTORY_BUTTON_MN").charAt(0));
        runDirectoryBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("RUN_DIRECTORY_BUTTON_TXT"));
        runDirectoryBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runDirectoryBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 4, 0, 12);
        add(runDirectoryBrowseButton, gridBagConstraints);

        argumentLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ARGUMENTS_LABEL_MN").charAt(0));
        argumentLabel.setLabelFor(argumentTextField);
        argumentLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ARGUMENTS_LABEL_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(argumentLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(argumentTextField, gridBagConstraints);

        environmentLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ENVIRONMENT_LABEL_MN").charAt(0));
        environmentLabel.setLabelFor(environmentTextField);
        environmentLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ENVIRONMENT_LABEL_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(environmentLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(environmentTextField, gridBagConstraints);

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
        }
        else {
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
        if (seed.length() == 0 && FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
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
    private javax.swing.JComboBox projectComboBox;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JButton runDirectoryBrowseButton;
    private javax.swing.JLabel runDirectoryLabel;
    private javax.swing.JTextField runDirectoryTextField;
    // End of variables declaration//GEN-END:variables
    
    private void initGui() {
        projectChoices = OpenProjects.getDefault().getOpenProjects();
        ActionListener projectComboBoxActionListener = projectComboBox.getActionListeners()[0];
        projectComboBox.removeActionListener(projectComboBoxActionListener);
        projectComboBox.removeAllItems();
        projectComboBox.addItem(getString("NO_PROJECT")); // always first
        for (int i = 0; i < projectChoices.length; i++) {
            projectComboBox.addItem(ProjectUtils.getInformation(projectChoices[i]).getName());
        }
        
        int index = 0;
        // preselect project ???
        if (lastSelectedExecutable != null && getExecutablePath().equals(lastSelectedExecutable) && lastSelectedProject != null) {
            for (int i = 0; i < projectChoices.length; i++) {
                if (projectChoices[i] == lastSelectedProject) {
                    index = i+1;
                    break;
                }
            }
        }
        projectComboBox.setSelectedIndex(index);
        projectComboBox.addActionListener(projectComboBoxActionListener);
        projectComboBoxActionPerformed(null);
        //validateRunDirectory();
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
    
    private boolean validateRunDirectory() {
        String runDirectory = runDirectoryTextField.getText();
        
        File runDirectoryFile = new File(runDirectoryTextField.getText());
     
        if (!runDirectoryFile.exists()) {
            setError("ERROR_RUNDIR_DONTEXIST", false); // NOI18N
            return false;
        }
        if (!runDirectoryFile.isDirectory()) {
            setError("ERROR_RUNDIR_INVALID", false); // NOI18N
            return false;
        }
        return true;
    }
    
    private void setError(String errorMsg, boolean disable) {
        setErrorMsg(getString(errorMsg));
        if (disable) {
            runDirectoryBrowseButton.setEnabled(false);
            runDirectoryLabel.setEnabled(false);
            runDirectoryTextField.setEnabled(false);
            argumentLabel.setEnabled(false);
            argumentTextField.setEnabled(false);
            environmentLabel.setEnabled(false);
            environmentTextField.setEnabled(false);
            projectComboBox.setEnabled(false);
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
            }
            validateRunDirectory();
        } finally {
            isValidating = false;
        }
    }
    
    // ModifiedDocumentListener
    public class ModifiedValidateDocumentListener implements DocumentListener {
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
                String projectFolder = ProjectGenerator.getDefaultProjectFolder();
                String projectName = ProjectGenerator.getValidProjectName(projectFolder, new File(getExecutablePath()).getName());
                String baseDir = projectFolder + File.separator + projectName;
                MakeConfiguration conf = new MakeConfiguration(baseDir, "Default", MakeConfiguration.TYPE_MAKEFILE);  // NOI18N
                // Working dir
                String wd = new File(getExecutablePath()).getParentFile().getPath();
                wd = CndPathUtilitities.toRelativePath(baseDir, wd);
                wd = CndPathUtilitities.normalize(wd);
                conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(wd);
                // Executable
                String exe = getExecutablePath();
                exe = CndPathUtilitities.toRelativePath(baseDir, exe);
                exe = CndPathUtilitities.normalize(exe);
                conf.getMakefileConfiguration().getOutput().setValue(exe);
                
                updateRunProfile(baseDir, conf.getProfile());
                
                project = MakeProjectGenerator.createBlankProject(projectName, projectFolder, new MakeConfiguration[] {conf}, true);
            } catch (Exception e) {
                project = null;
            }
            lastSelectedProject = project;
        }
        return project;
    }
    
    private void updateRunProfile(String baseDir, RunProfile runProfile) {
        // Arguments
        runProfile.setArgs(argumentTextField.getText());
        // Working dir
        String wd = runDirectoryTextField.getText();
        wd = CndPathUtilitities.toRelativePath(baseDir, wd);
        wd = CndPathUtilitities.normalize(wd);
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
    private ResourceBundle bundle;
    private String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(RunDialogPanel.class);
        }
        return bundle.getString(s);
    }
    
    public boolean asynchronous() {
        return false;
    }
}

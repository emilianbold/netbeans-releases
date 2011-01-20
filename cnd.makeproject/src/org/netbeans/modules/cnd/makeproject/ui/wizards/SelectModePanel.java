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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual.DevHostsInitializer;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual.ToolCollectionItem;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.DocumentAdapter;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class SelectModePanel extends javax.swing.JPanel {

    private final SelectModeDescriptorPanel controller;
    private volatile boolean initialized = false;

    /** Creates new form SelectModePanel */
    public SelectModePanel(SelectModeDescriptorPanel controller) {
        this.controller = controller;
        initComponents();
        sourceFolderLabel.setVisible(controller.isFullRemote());
        sourceFolder.setVisible(controller.isFullRemote());
        sourceBrowseButton.setVisible(controller.isFullRemote());
        if (!controller.isFullRemote()) {
            // the same dir is ised for both project metadata and existing sources;
            // but "existing sources" in more clear title
            projectFolderLabel.setText(sourceFolderLabel.getText());
        }
        instructions.setEditorKit(new HTMLEditorKit());
        instructions.setBackground(instructionPanel.getBackground());
        disableHostSensitiveComponents();
        addListeners();
    }
    
    private void addListeners(){
        projectFolder.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                String path = projectFolder.getText();
                controller.getWizardStorage().setProjectPath(path);
                if (!controller.isFullRemote()) {
                    if (!path.isEmpty()) {
                        String normalizedPath = CndFileUtils.normalizeAbsolutePath(path);
                        controller.getWizardStorage().setSourcesFileObject(CndFileUtils.toFileObject(normalizedPath));
                    }
                }
                updateInstruction();
            }
        });
        sourceFolder.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                String path = sourceFolder.getText();
                FileObject fileObject;
                String projectName = null;
                if (path.isEmpty()) {
                    fileObject = null;
                } else {
                    //fileObject = RemoteFileUtil.getFileObject(path, env,
                    ExecutionEnvironment env = getSelectedExecutionEnvironment();
                    fileObject = RemoteFileUtil.getFileObject(path, env, RemoteProject.Mode.REMOTE_SOURCES);
                    projectName = (fileObject == null) ? null : fileObject.getNameExt();
                }
                controller.getWizardStorage().setSourcesFileObject(fileObject);
                if (projectFolder.getText().isEmpty()) {
                    if (projectName != null && ! projectName.isEmpty() ) {
                        File projectLocation = ProjectChooser.getProjectsFolder();
                        File projectFile = CndFileUtils.createLocalFile(projectLocation, projectName);
                        projectFolder.setText(projectFile.getAbsolutePath());
                    }
                }
                updateInstruction();
            }
        });
        simpleMode.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.getWizardStorage().setMode(simpleMode.isSelected());
                updateInstruction();
            }
        });
        advancedMode.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.getWizardStorage().setMode(simpleMode.isSelected());
                updateInstruction();
            }
        });
        updateInstruction();
    }
    
    private void updateInstruction(){
        if (simpleMode.isSelected()){
            String tool = "Makefile"; // NOI18N
            String toolsInfo = NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionExtraText_Make"); // NOI18N
            if (controller.getWizardStorage() != null) {
                String configure = controller.getWizardStorage().getConfigure();
                if (configure != null) {
                    toolsInfo = NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionExtraText_Configure"); // NOI18N
                    tool = configure;
                    String normalizedPath = CndFileUtils.normalizeAbsolutePath(configure);
                    FileObject fo = CndFileUtils.toFileObject(normalizedPath);
                    if (fo != null && fo.isValid()) {
                        String mimeType = fo.getMIMEType();
                        if (MIMENames.CMAKE_MIME_TYPE.equals(mimeType)) {
                            toolsInfo = NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionExtraText_CMake"); // NOI18N
                            tool = "cmake"; // NOI18N
                        } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mimeType)) {
                            toolsInfo = NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionExtraText_QMake"); // NOI18N
                            tool = "qmake"; // NOI18N
                        }
                    }
                } else {
                    String makefile = controller.getWizardStorage().getMake();
                    if (makefile != null) {
                        tool = makefile;
                    }
                }
            }
            String modeInfo = NbBundle.getMessage(SelectModePanel.class, "SimpleModeButtonText", tool); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(simpleMode, modeInfo);
            instructions.setText(NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionText", toolsInfo));
        } else {
            instructions.setText(NbBundle.getMessage(SelectModePanel.class, "SelectModeAdvancedInstructionText")); // NOI18N
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        instructionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        instructions = new javax.swing.JTextPane();
        simpleMode = new javax.swing.JRadioButton();
        advancedMode = new javax.swing.JRadioButton();
        modeLabel = new javax.swing.JLabel();
        projectFolderLabel = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        projectBrowseButton = new javax.swing.JButton();
        toolchainComboBox = new javax.swing.JComboBox();
        toolchainLabel = new javax.swing.JLabel();
        hostComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        sourceFolderLabel = new javax.swing.JLabel();
        sourceFolder = new javax.swing.JTextField();
        sourceBrowseButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        instructionPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 200));

        instructions.setBorder(null);
        instructions.setEditable(false);
        instructions.setFocusable(false);
        instructions.setOpaque(false);
        jScrollPane1.setViewportView(instructions);

        instructionPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        buttonGroup1.add(simpleMode);
        simpleMode.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(simpleMode, bundle.getString("SimpleModeButtonText")); // NOI18N
        simpleMode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        simpleMode.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(simpleMode, gridBagConstraints);

        buttonGroup1.add(advancedMode);
        org.openide.awt.Mnemonics.setLocalizedText(advancedMode, bundle.getString("AdvancedModeButtonText")); // NOI18N
        advancedMode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        advancedMode.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(advancedMode, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(modeLabel, bundle.getString("SelectModeLabelText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(modeLabel, gridBagConstraints);

        projectFolderLabel.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_PROJECT_FOLDER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(projectFolderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(projectFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(projectBrowseButton, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_BROWSE_PROJECT_FOLDER")); // NOI18N
        projectBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(projectBrowseButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(toolchainComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(toolchainLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_TOOLCHAIN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 8, 0);
        add(toolchainLabel, gridBagConstraints);

        hostComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hostComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(hostComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 8, 0);
        add(hostLabel, gridBagConstraints);

        sourceFolderLabel.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(sourceFolderLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_SOURCES_FOLDER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceFolderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(sourceFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(sourceBrowseButton, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_BROWSE_PROJECT_FOLDER")); // NOI18N
        sourceBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(sourceBrowseButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void projectBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectBrowseButtonActionPerformed
        String seed = projectFolder.getText();
        JFileChooser fileChooser;
        String approveButtonText = NbBundle.getMessage(SelectModePanel.class, "PROJECT_DIR_BUTTON_TXT"); // NOI18N
        fileChooser = new FileChooser( // Sic! - project is always local
                NbBundle.getMessage(SelectModePanel.class, "PROJECT_DIR_CHOOSER_TITLE_TXT"), // NOI18N
                approveButtonText,
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) { // seems paranoidal, but once I've seen NPE otherwise 8-()
            String path = selectedFile.getPath();
            projectFolder.setText(path);
        }
}//GEN-LAST:event_projectBrowseButtonActionPerformed

    private void hostComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hostComboBoxItemStateChanged
        if (!initialized) {
            return;
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            ServerRecord newItem = (ServerRecord) evt.getItem();
            PanelProjectLocationVisual.updateToolchains(toolchainComboBox, newItem);
            this.controller.fireChangeEvent(); // Notify that the panel changed
        }
}//GEN-LAST:event_hostComboBoxItemStateChanged

    private void sourceBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceBrowseButtonActionPerformed
        CndUtils.assertTrue(controller.isFullRemote());
        String seed = sourceFolder.getText();
        String approveButtonText = NbBundle.getMessage(SelectModePanel.class, "SOURCES_DIR_BUTTON_TXT"); // NOI18N
        String title = NbBundle.getMessage(SelectModePanel.class, "SOURCES_DIR_CHOOSER_TITLE_TXT"); //NOI18N
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                title,
                approveButtonText,
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) { // seems paranoidal, but once I've seen NPE otherwise 8-()
            String path = selectedFile.getPath();
            sourceFolder.setText(path);
        }
    }//GEN-LAST:event_sourceBrowseButtonActionPerformed
    
    void read(WizardDescriptor wizardDescriptor) {
        initialized = false;
        updateControls();
        String hostUID = (String) wizardDescriptor.getProperty(WizardConstants.PROPERTY_HOST_UID);
        CompilerSet cs = (CompilerSet) wizardDescriptor.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        RequestProcessor.getDefault().post(new DevHostsInitializer(hostUID, cs, false,
                (ToolsCacheManager) wizardDescriptor.getProperty(WizardConstants.PROPERTY_TOOLS_CACHE_MANAGER)) { // NOI18N
            @Override
            public void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean enabled) {
                boolean enableHost = enabled;
                if (controller.isFullRemote()) {
                    enableHost = false;
                }
                enableHostSensitiveComponents(records, srToSelect, csToSelect, enableHost, enabled);
            }
        });
    }
    
    void updateControls() {
        updateInstruction();
    }

    void enableControls(boolean enable){
        advancedMode.setEnabled(enable);
        simpleMode.setEnabled(enable);
    }

    private ExecutionEnvironment getSelectedExecutionEnvironment() {
        Object obj = hostComboBox.getSelectedItem();
        if (obj != null && obj instanceof ServerRecord) {
            ServerRecord sr = (ServerRecord) obj;
            return sr.getExecutionEnvironment();
        }
        return ServerList.getDefaultRecord().getExecutionEnvironment();
    }


    void store(WizardDescriptor wizardDescriptor) {
        if (simpleMode.isSelected()) {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE, Boolean.TRUE);
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_SET_AS_MAIN,  Boolean.TRUE);
        } else {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE, Boolean.FALSE);
        }
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER, projectFolder.getText().trim());
        String folderPath = projectFolder.getText().trim();
        if (CndPathUtilitities.isPathAbsolute(folderPath)) {
            File file = CndFileUtils.createLocalFile(folderPath);
            file = FileUtil.normalizeFile(file);
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_PROJECT_FOLDER, file);
        }
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.TRUE);

        ExecutionEnvironment ee = getSelectedExecutionEnvironment();
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.toUniqueID(ee));
        controller.getWizardStorage().setExecutionEnvironment(ee);

        Object tc = toolchainComboBox.getSelectedItem();
        if (tc != null && tc instanceof ToolCollectionItem) {
            ToolCollectionItem item = (ToolCollectionItem) tc;
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_TOOLCHAIN, item.getCompilerSet());
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT, item.isDefaultCompilerSet());
            controller.getWizardStorage().setCompilerSet(item.getCompilerSet());
            controller.getWizardStorage().setDefaultCompilerSet(item.isDefaultCompilerSet());
        }
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_NATIVE_PROJ_FO, controller.getWizardStorage().getSourcesFileObject()); 
        FileObject fo = controller.getWizardStorage().getSourcesFileObject();
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_NATIVE_PROJ_DIR, (fo == null) ? null : fo.getPath()); 
        initialized = false;
    }

    private static final byte noMessage = 0;
    private static final byte notFolder = 1;
    private static final byte cannotReadFolder = 2;
    private static final byte cannotWriteFolder = 3;
    private static final byte alreadyNbPoject = 4;
    private static final byte notFoundMakeAndConfigure = 5;
    private static final byte notRoot = 6;
    private byte messageKind = noMessage;

    boolean valid() {
        messageKind = noMessage;
        String path = projectFolder.getText().trim();
        try {
            if (path.length() == 0) {
                return false;
            }
            if (!CndPathUtilitities.isPathAbsolute(path)) {
                messageKind = notFolder;
                return false;
            }
            File projectDirFile = FileUtil.normalizeFile(CndFileUtils.createLocalFile(path)); // it's project folder - always local
            File projectDirParent = projectDirFile.getParentFile();
            // in the cse of full remote the directory should not necessarily exist, but its parent should
            File fileToCheck = controller.isFullRemote() ? projectDirParent : projectDirFile;
            if (fileToCheck == null || !fileToCheck.isDirectory() || !fileToCheck.canRead()) {
                if (fileToCheck == null) {
                    messageKind = notRoot;
                }
                else if(fileToCheck.isDirectory()) {
                    messageKind = cannotReadFolder;
                } else {
                    messageKind = notFolder;
                }
                path = (fileToCheck == null) ? "" : fileToCheck.getAbsolutePath(); //NOI18N
                return false;
            }

            if (simpleMode.isSelected()) {
                if (!fileToCheck.canWrite()) {
                    messageKind = cannotWriteFolder;
                    path = fileToCheck.getAbsolutePath();
                    return false;
                }
                File nbFile = CndFileUtils.createLocalFile(
                        CndFileUtils.createLocalFile(path, MakeConfiguration.NBPROJECT_FOLDER),
                        MakeConfiguration.PROJECT_XML);
                if (nbFile.exists()) {
                    messageKind = alreadyNbPoject;
                    return false;
                }
                if (projectDirFile.isDirectory()) {
                    FileObject fo = CndFileUtils.toFileObject(projectDirFile);
                    if (fo != null && fo.isValid()) {
                        try {
                            if (ProjectManager.getDefault().findProject(fo) != null) {
                                messageKind = alreadyNbPoject;
                                return false;
                            }
                        } catch (IOException ex) {
                        } catch (IllegalArgumentException ex) {
                        }
                    }
                }
            }
            if (ConfigureUtils.findConfigureScript(controller.getWizardStorage().getSourcesFileObject()) != null) {
                return true;
            }
            FileObject makeFO = ConfigureUtils.findMakefile(controller.getWizardStorage().getSourcesFileObject());
            if (makeFO != null){
                controller.getWizardStorage().setMake(makeFO);
                return true;
            }
            if (simpleMode.isSelected()) {
                messageKind = notFoundMakeAndConfigure;
                return false;
            }
            return true;
        } finally {
            if (messageKind > 0) {
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                        NbBundle.getMessage(SelectModePanel.class, "SelectModeError"+messageKind,path)); // NOI18N
            } else {
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                if (simpleMode.isSelected()) {
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                            NbBundle.getMessage(SelectModePanel.class, "CleanInfoMessageSimpleMode")); // NOI18N
                } else {
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);
                }
            }
        }
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton advancedMode;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextPane instructions;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JButton projectBrowseButton;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JRadioButton simpleMode;
    private javax.swing.JButton sourceBrowseButton;
    private javax.swing.JTextField sourceFolder;
    private javax.swing.JLabel sourceFolderLabel;
    private javax.swing.JComboBox toolchainComboBox;
    private javax.swing.JLabel toolchainLabel;
    // End of variables declaration//GEN-END:variables

    private void disableHostSensitiveComponents() {
        PanelProjectLocationVisual.disableHostsInfo(this.hostComboBox, this.toolchainComboBox);
        this.advancedMode.setEnabled(false);
        this.simpleMode.setEnabled(false);
    }

    private void enableHostSensitiveComponents(Collection<ServerRecord> records, 
            ServerRecord srToSelect, CompilerSet csToSelect, boolean enableHost, boolean enableToolchain) {
        PanelProjectLocationVisual.updateToolchainsComponents(SelectModePanel.this.hostComboBox, SelectModePanel.this.toolchainComboBox, 
                records, srToSelect, csToSelect, enableHost, enableToolchain);
        this.advancedMode.setEnabled(true);
        this.simpleMode.setEnabled(true);
        updateInstruction();
        initialized = true;
        SelectModePanel.this.controller.fireChangeEvent(); // Notify that the panel changed
    }

}

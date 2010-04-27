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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual.DevHostsInitializer;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
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
    public SelectModePanel(SelectModeDescriptorPanel wizard) {
        this.controller = wizard;
        initComponents();
        instructions.setEditorKit(new HTMLEditorKit());
        instructions.setBackground(instructionPanel.getBackground());
        disableHostSensitiveComponents();
        addListeners();
    }
    
    private void addListeners(){
        projectFolder.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                controller.getWizardStorage().setPath(projectFolder.getText());
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
            String toolsInfo = getString("SelectModeSimpleInstructionExtraText_Make");
            if (controller.getWizardStorage() != null && controller.getWizardStorage().getMake() == null) {
                String configure = controller.getWizardStorage().getConfigure();
                if (configure != null) {
                    toolsInfo = getString("SelectModeSimpleInstructionExtraText_Configure");
                    File confFile = FileUtil.normalizeFile(new File(configure));
                    FileObject fo = FileUtil.toFileObject(confFile);
                    if (fo != null) {
                        String mimeType = fo.getMIMEType();
                        if (MIMENames.CMAKE_MIME_TYPE.equals(mimeType)) {
                            toolsInfo = getString("SelectModeSimpleInstructionExtraText_CMake");
                        } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mimeType)) {
                            toolsInfo = getString("SelectModeSimpleInstructionExtraText_QMake");
                        }
                    }
                }
            }
            instructions.setText(getString("SelectModeSimpleInstructionText", toolsInfo)); // NOI18N
        } else {
            instructions.setText(getString("SelectModeAdvancedInstructionText")); // NOI18N
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
        jLabel1 = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        toolchainComboBox = new javax.swing.JComboBox();
        toolchainLabel = new javax.swing.JLabel();
        hostComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        instructionPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 200));

        instructions.setBorder(null);
        instructions.setEditable(false);
        instructions.setFocusable(false);
        jScrollPane1.setViewportView(instructions);

        instructionPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
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
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(advancedMode, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(modeLabel, bundle.getString("SelectModeLabelText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(modeLabel, gridBagConstraints);

        jLabel1.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_PROJECT_FOLDER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(projectFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_BROWSE_PROJECT_FOLDER")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(browseButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(toolchainComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(toolchainLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_TOOLCHAIN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
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
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(hostComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 8, 0);
        add(hostLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String seed = projectFolder.getText();
        JFileChooser fileChooser = new FileChooser(
                getString("PROJECT_DIR_CHOOSER_TITLE_TXT"), // NOI18N
                getString("PROJECT_DIR_BUTTON_TXT"), // NOI18N
                JFileChooser.DIRECTORIES_ONLY, 
                null,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        projectFolder.setText(path);
}//GEN-LAST:event_browseButtonActionPerformed

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
    
    void read(WizardDescriptor wizardDescriptor) {
        initialized = false;
        updateControls();
        String hostUID = (String) wizardDescriptor.getProperty("hostUID");
        CompilerSet cs = (CompilerSet) wizardDescriptor.getProperty("toolchain");
        RequestProcessor.getDefault().post(new DevHostsInitializer(hostUID, cs, false) {
            @Override
            public void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean enabled) {
                enableHostSensitiveComponents(records, srToSelect, csToSelect, enabled);
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

    void store(WizardDescriptor wizardDescriptor) {
        if (simpleMode.isSelected()) {
            wizardDescriptor.putProperty("simpleMode", Boolean.TRUE); // NOI18N
            wizardDescriptor.putProperty("setAsMain",  Boolean.TRUE); // NOI18N
        } else {
            wizardDescriptor.putProperty("simpleMode", Boolean.FALSE); // NOI18N
        }
        wizardDescriptor.putProperty("simpleModeFolder", projectFolder.getText().trim()); // NOI18N
        wizardDescriptor.putProperty("readOnlyToolchain", Boolean.TRUE); // NOI18N
        Object obj = hostComboBox.getSelectedItem();
        if (obj != null && obj instanceof ServerRecord) {
            ServerRecord sr = (ServerRecord) obj;
            ExecutionEnvironment ee = sr.getExecutionEnvironment();
            wizardDescriptor.putProperty("hostUID", ExecutionEnvironmentFactory.toUniqueID(ee)); // NOI18N
            controller.getWizardStorage().setExecutionEnvironment(ee);
        }
        Object tc = toolchainComboBox.getSelectedItem();
        if (tc != null && tc instanceof CompilerSet) {
            wizardDescriptor.putProperty("toolchain", tc); // NOI18N
            controller.getWizardStorage().setCompilerSet((CompilerSet) tc);
        }
        initialized = false;
    }

    private static final byte noMessage = 0;
    private static final byte notFolder = 1;
    private static final byte cannotReadFolder = 2;
    private static final byte cannotWriteFolder = 3;
    private static final byte alreadyNbPoject = 4;
    private static final byte notFoundMakeAndConfigure = 5;
    private byte messageKind = noMessage;

    boolean valid() {
        messageKind = noMessage;
        String path = projectFolder.getText().trim();
        try {
            if (path.length() == 0) {
                return false;
            }
            File file = new File(path);
            if (!(file.isDirectory() && file.canRead())) {
                if (file.isDirectory()) {
                    messageKind = cannotReadFolder;
                } else {
                    messageKind = notFolder;
                }
                return false;
            }
            if (simpleMode.isSelected()) {
                if (!file.canWrite()) {
                    messageKind = cannotWriteFolder;
                    return false;
                }
                File nbFile = new File(new File(path, MakeConfiguration.NBPROJECT_FOLDER), MakeConfiguration.PROJECT_XML); // NOI18N
                if (nbFile.exists()) {
                    messageKind = alreadyNbPoject;
                    return false;
                }
                if (file.isDirectory()) {
                    FileObject fo = FileUtil.toFileObject(file);
                    if (fo != null) {
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
            if (ConfigureUtils.findMakefile(path) != null){
                return true;
            }
            if (ConfigureUtils.findConfigureScript(path) != null){
                return true;
            }
            if (simpleMode.isSelected()) {
                messageKind = notFoundMakeAndConfigure;
                return false;
            }
            return true;
        } finally {
            if (messageKind > 0) {
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("SelectModeError"+messageKind,path)); // NOI18N
            } else {
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
            }
        }
    }
    
    private String getString(String key, String ... params){
        return NbBundle.getMessage(SelectModePanel.class, key, params);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton advancedMode;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextPane instructions;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JRadioButton simpleMode;
    private javax.swing.JComboBox toolchainComboBox;
    private javax.swing.JLabel toolchainLabel;
    // End of variables declaration//GEN-END:variables

    private void disableHostSensitiveComponents() {
        PanelProjectLocationVisual.disableHostsInfo(this.hostComboBox, this.toolchainComboBox);
        this.advancedMode.setEnabled(false);
        this.simpleMode.setEnabled(false);
    }

    private void enableHostSensitiveComponents(Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean enabled) {
        PanelProjectLocationVisual.updateToolchainsComponents(SelectModePanel.this.hostComboBox, SelectModePanel.this.toolchainComboBox, records, srToSelect, csToSelect, enabled);
        this.advancedMode.setEnabled(true);
        this.simpleMode.setEnabled(true);
        updateInstruction();
        initialized = true;
        SelectModePanel.this.controller.fireChangeEvent(); // Notify that the panel changed
    }

}

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
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual.DevHostsInitializer;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual.ToolCollectionItem;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.EditableComboBox;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class SelectModePanel extends javax.swing.JPanel {
    private static final int VERIFY_DELAY = 300;

    private final SelectModeDescriptorPanel controller;
    private volatile boolean initialized = false;
    private static final String SOURCES_FILE_KEY = "sourcesField"; // NOI18N
    private ExecutionEnvironment env;
    private FileSystem fileSystem;
    private static final RequestProcessor RP = new RequestProcessor("SelectModePanel", 1); // NOI18N
    private static final RequestProcessor RP2 = new RequestProcessor("SelectRoot", 1); // NOI18N
    private final RequestProcessor.Task refreshSourceFolderTask;
    private final RefreshRunnable refreshRunnable;

    /** Creates new form SelectModePanel */
    public SelectModePanel(SelectModeDescriptorPanel controller) {
        this.controller = controller;
        initComponents();
        instructions.setEditorKit(new HTMLEditorKit());
        instructions.setBackground(instructionPanel.getBackground());
        disableHostSensitiveComponents();
        refreshRunnable = new RefreshRunnable();
        refreshSourceFolderTask = RP2.create(refreshRunnable);
        addListeners();
    }
    
    private void refreshSourceFolder() {
        String path = ((EditableComboBox)sourceFolder).getText();
        FileObject fileObject;
        if (path.isEmpty()) {
            fileObject = null;
        } else {
            fileObject = fileSystem.findResource(path);
        }
        controller.getWizardStorage().setSourcesFileObject(fileObject);
    }
    
    private void addListeners(){
        ((EditableComboBox)sourceFolder).addChangeListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshInstruction(true);
            }
        });
        simpleMode.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshInstruction(false);
            }
        });
        advancedMode.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshInstruction(false);
            }
        });
        //refreshInstruction();
    }
    
    private void refreshInstruction(boolean refreshRoot) {
        controller.invalidate();
        refreshSourceFolderTask.cancel();
        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(SelectModePanel.class, "SelectModeError0")); // NOI18N
        refreshRunnable.setRefreshRoot(refreshRoot);
        refreshSourceFolderTask.schedule(VERIFY_DELAY);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        instructionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        instructions = new javax.swing.JTextPane();
        simpleMode = new javax.swing.JRadioButton();
        advancedMode = new javax.swing.JRadioButton();
        modeLabel = new javax.swing.JLabel();
        toolchainComboBox = new javax.swing.JComboBox();
        toolchainLabel = new javax.swing.JLabel();
        hostComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        sourceFolderLabel = new javax.swing.JLabel();
        sourceBrowseButton = new javax.swing.JButton();
        sourceFolder = new EditableComboBox();

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
        gridBagConstraints.gridy = 7;
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
        gridBagConstraints.gridy = 5;
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
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(advancedMode, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(modeLabel, bundle.getString("SelectModeLabelText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(modeLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(toolchainComboBox, gridBagConstraints);

        toolchainLabel.setLabelFor(toolchainComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(toolchainLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_TOOLCHAIN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(hostComboBox, gridBagConstraints);

        hostLabel.setLabelFor(hostComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 8, 0);
        add(hostLabel, gridBagConstraints);

        sourceFolderLabel.setLabelFor(sourceFolder);
        org.openide.awt.Mnemonics.setLocalizedText(sourceFolderLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_SOURCES_FOLDER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceFolderLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(sourceBrowseButton, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_BROWSE_PROJECT_FOLDER")); // NOI18N
        sourceBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(sourceBrowseButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(sourceFolder, gridBagConstraints);
    }// </editor-fold>                        

    private void hostComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {                                              
        if (!initialized) {
            return;
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            ServerRecord newItem = (ServerRecord) evt.getItem();
            PanelProjectLocationVisual.updateToolchains(toolchainComboBox, newItem);
            this.controller.fireChangeEvent(); // Notify that the panel changed
        }
}                                             

    private void sourceBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        String seed = ((EditableComboBox)sourceFolder).getText();
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
            ((EditableComboBox)sourceFolder).setText(path);
        }
    }                                                  
    
    void read(WizardDescriptor wizardDescriptor) {
        initialized = false;
        env = (ExecutionEnvironment) wizardDescriptor.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV);
        if (env != null) {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.toUniqueID(env));
        } else {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        fileSystem = FileSystemProvider.getFileSystem(env);
        ((EditableComboBox)sourceFolder).setStorage(SOURCES_FILE_KEY, NbPreferences.forModule(SelectModePanel.class));
        ((EditableComboBox)sourceFolder).read("");
        refreshInstruction(false);
        
        String hostUID = (String) wizardDescriptor.getProperty(WizardConstants.PROPERTY_HOST_UID);
        CompilerSet cs = (CompilerSet) wizardDescriptor.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        boolean isDefaultCompilerSet = Boolean.TRUE.equals(wizardDescriptor.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
        RP.post(new DevHostsInitializer(hostUID, cs, isDefaultCompilerSet, false,
                (ToolsCacheManager) wizardDescriptor.getProperty(WizardConstants.PROPERTY_TOOLS_CACHE_MANAGER)) { // NOI18N
            @Override
            public void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enabled) {
                boolean enableHost = enabled;
                if (controller.isFullRemote()) {
                    enableHost = false;
                }
                enableHostSensitiveComponents(records, srToSelect, csToSelect, isDefaultCompilerSet, enableHost, enabled);
            }
        });
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
        } else {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE, Boolean.FALSE);
        }
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER, ((EditableComboBox)sourceFolder).getText().trim());
        ((EditableComboBox)sourceFolder).setStorage(SOURCES_FILE_KEY, NbPreferences.forModule(SelectModePanel.class));
        ((EditableComboBox)sourceFolder).store();
        String folderPath = ((EditableComboBox)sourceFolder).getText().trim();
        if (WizardDescriptor.CLOSED_OPTION.equals(wizardDescriptor.getValue()) || WizardDescriptor.CANCEL_OPTION.equals(wizardDescriptor.getValue()) ) {
            return;
        }
        if (CndPathUtilities.isPathAbsolute(folderPath)) {
            String normalizeAbsolutePath = RemoteFileUtil.normalizeAbsolutePath(folderPath, env);
            FSPath path = new FSPath(fileSystem, normalizeAbsolutePath);
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_PROJECT_FOLDER, path);
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
        FileObject fo = controller.getWizardStorage().getSourcesFileObject();
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_NATIVE_PROJ_FO, fo);
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
        String path = ((EditableComboBox)sourceFolder).getText().trim();
        try {
            if (path.length() == 0) {
                return false;
            }
            if (!CndPathUtilities.isPathAbsolute(path)) {
                messageKind = notFolder;
                return false;
            }
            FileObject projectDirFO = fileSystem.findResource(path); // can be null
            if (projectDirFO == null || !projectDirFO.isValid()) {
                messageKind = notFolder;
                return false;
            }
            if (!projectDirFO.isFolder()) {
                messageKind = notFolder;
                return false;
            }
            if (!projectDirFO.canRead()) {
                messageKind = cannotReadFolder;
                return false;
            }
            
            if (simpleMode.isSelected()) {
                if (!projectDirFO.canWrite()) {
                    messageKind = cannotWriteFolder;
                    return false;
                }
                FileObject nbProjFO = projectDirFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
                if (nbProjFO != null && nbProjFO.isValid()) {
                    messageKind = alreadyNbPoject;
                    return false;
                }
                try {
                    if (ProjectManager.getDefault().findProject(projectDirFO) != null) {
                        messageKind = alreadyNbPoject;
                        return false;
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
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
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            messageKind = cannotReadFolder;
            return false;
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
        
    // Variables declaration - do not modify                     
    private javax.swing.JRadioButton advancedMode;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextPane instructions;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JRadioButton simpleMode;
    private javax.swing.JButton sourceBrowseButton;
    private javax.swing.JComboBox sourceFolder;
    private javax.swing.JLabel sourceFolderLabel;
    private javax.swing.JComboBox toolchainComboBox;
    private javax.swing.JLabel toolchainLabel;
    // End of variables declaration                   

    private void disableHostSensitiveComponents() {
        PanelProjectLocationVisual.disableHostsInfo(this.hostComboBox, this.toolchainComboBox);
        this.advancedMode.setEnabled(false);
        this.simpleMode.setEnabled(false);
    }

    private void enableHostSensitiveComponents(Collection<ServerRecord> records, 
            ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enableHost, boolean enableToolchain) {
        PanelProjectLocationVisual.updateToolchainsComponents(SelectModePanel.this.hostComboBox, SelectModePanel.this.toolchainComboBox, 
                records, srToSelect, csToSelect, isDefaultCompilerSet, enableHost, enableToolchain);
        this.advancedMode.setEnabled(true);
        this.simpleMode.setEnabled(true);
        refreshInstruction(false);
        initialized = true;
        SelectModePanel.this.controller.fireChangeEvent(); // Notify that the panel changed
    }


    private class RefreshRunnable implements Runnable {
        private boolean refreshRoot = false;
        private int generation = 0;
        private final Object lock = new Object();

        public RefreshRunnable() {
        }
        
        private void setRefreshRoot(boolean refreshRoot) {
            synchronized(lock) {
                if (refreshRoot && ! this.refreshRoot) {
                    this.refreshRoot = true;
                    generation++;
                }
            }
        }

        @Override
        public void run() {
            int startCount;
            boolean refresh;
            synchronized(lock) {
                refresh = refreshRoot;
                refreshRoot = false;
                startCount = generation;
            }
            boolean simple = simpleMode.isSelected();
            SelectModePanel.this.controller.getWizardStorage().setMode(simple);            
            if (refresh) {
                refreshSourceFolder();
            }
            synchronized(lock) {
                if (startCount < generation) {
                    return;
                }
            }
            if (simple) {
                String tool = "Makefile"; // NOI18N
                String toolsInfo = NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionExtraText_Make"); // NOI18N
                if (SelectModePanel.this.controller.getWizardStorage() != null) {
                    String configure = SelectModePanel.this.controller.getWizardStorage().getConfigure();
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
                        String makefile = SelectModePanel.this.controller.getWizardStorage().getMake();
                        if (makefile != null) {
                            tool = makefile;
                        }
                    }
                }
                synchronized(lock) {
                    if (startCount <generation) {
                        return;
                    }
                }
                String modeInfo = NbBundle.getMessage(SelectModePanel.class, "SimpleModeButtonText", tool); // NOI18N
                final String message1 = modeInfo;
                final String message2 = NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionText", toolsInfo);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        org.openide.awt.Mnemonics.setLocalizedText(simpleMode, message1);
                        instructions.setText(message2);
                    }
                });
            } else {
                final String message = NbBundle.getMessage(SelectModePanel.class, "SelectModeAdvancedInstructionText"); // NOI18N
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        instructions.setText(message);
                    }
                });
            }
        }
    }

}

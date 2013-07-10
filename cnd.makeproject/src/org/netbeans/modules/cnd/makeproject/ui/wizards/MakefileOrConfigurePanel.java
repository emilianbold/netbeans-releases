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

import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class MakefileOrConfigurePanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final int VERIFY_DELAY = 300;
    private final DocumentListener documentListener;
    private final MakefileOrConfigureDescriptorPanel controller;
    private final RefreshRunnable refreshRunnable;
    private static final RequestProcessor RP = new RequestProcessor("MakefileOrConfigure Validator", 1); // NOI18N
    private final RequestProcessor.Task refreshSourceFolderTask;
    private WizardDescriptor settings;

    MakefileOrConfigurePanel(MakefileOrConfigureDescriptorPanel buildActionsDescriptorPanel) {
        initComponents();
        configureRunFolderTextField.setText("build"); // default build subfolder //NOI18N
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        this.controller = buildActionsDescriptorPanel;
        documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };

        // Button group
        buttonGroup.add(makefileRadioButton);
        buttonGroup.add(configureRadioButton);

        // init focus
        makefileNameTextField.requestFocus();

        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("MakefileOrConfigureName_AD"));
        makefileNameTextField.getAccessibleContext().setAccessibleDescription(getString("MAKEFILE_NAME_AD"));
        makefileBrowseButton.getAccessibleContext().setAccessibleDescription(getString("MAKEFILE_BROWSE_BUTTON_AD"));
        addDocumentLiseners();
        refreshRunnable = new RefreshRunnable();
        refreshSourceFolderTask = RP.create(refreshRunnable);

    }

    private void addDocumentLiseners() {
        // Add change listeners
        makefileNameTextField.getDocument().addDocumentListener(documentListener);
        configureNameTextField.getDocument().addDocumentListener(documentListener);
        configureRunFolderTextField.getDocument().addDocumentListener(documentListener);
    }

    private void removeDocumentLiseners() {
        makefileNameTextField.getDocument().removeDocumentListener(documentListener);
        configureNameTextField.getDocument().removeDocumentListener(documentListener);
        configureRunFolderTextField.getDocument().removeDocumentListener(documentListener);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewMakeWizardP11"); // NOI18N
    }

    private void update(DocumentEvent e) {
        refreshSourceFolderTask.cancel();
        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(MakefileOrConfigurePanel.class, "SelectModeError0")); // NOI18N
        controller.stateChanged(null);
        refreshRunnable.start(settings);
        refreshSourceFolderTask.schedule(VERIFY_DELAY);
    }

    void read(WizardDescriptor wizardDescriptor) {
        settings = wizardDescriptor;
        String hostUID = (String) wizardDescriptor.getProperty(WizardConstants.PROPERTY_HOST_UID);
        ExecutionEnvironment ee = null;
        if (hostUID != null) {
            ee = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
        }
        CompilerSet cs = null;
        if (ee != null) {
            cs = (CompilerSet) wizardDescriptor.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        }
        try {
            removeDocumentLiseners();
            String path = (String) wizardDescriptor.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER); // NOI18N
            if (path != null) {
                boolean selected = false;
                FileObject makeFileFO = ConfigureUtils.findMakefile((FileObject) wizardDescriptor.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_FO));
                String makeFile = (makeFileFO == null) ? null : makeFileFO.getPath();
                if (makeFile != null) {
                    makefileNameTextField.setText(makeFile);
                    makefileRadioButton.setSelected(true);
                    selected = true;
                }
                String configureScript;
                ExecutionEnvironment env = (ExecutionEnvironment) wizardDescriptor.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV);
                if (env != null) {
                    FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                    configureScript = ConfigureUtils.findConfigureScript(fileSystem.findResource(path));
                } else {
                    configureScript = ConfigureUtils.findConfigureScript(path);
                }
                if (configureScript != null) {
                    if (!selected) {
                        configureRadioButton.setSelected(true);
                        runConfigureCheckBox.setSelected(true);
                    }
                    configureNameTextField.setText(configureScript);
                    configureArgumentsTextField.setText(ConfigureUtils.getConfigureArguments(ee, cs, configureScript, "")); // NOI18N
                }
            }
            enableControls();
        } finally {
            addDocumentLiseners();
        }
        update((DocumentEvent)null);
    }

    void store(WizardDescriptor wizardDescriptor) {
        if (makefileRadioButton.isSelected()) {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH, makefileNameTextField.getText());
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH, ""); // NOI18N
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS, ""); // NOI18N
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER, ""); // NOI18N
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_RUN_REBUILD, makeCheckBox.isSelected() ? "true" : "false"); // NOI18N
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_RUN_CONFIGURE, ""); // NOI18N
        } else {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH, configureMakefileNameTextField.getText());
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH, configureNameTextField.getText());
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS, configureArgumentsTextField.getText());
            if (runSubfolderCheckBox.isSelected()) {
                wizardDescriptor.putProperty(WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER, configureRunFolderTextField.getText());
            } else {
                wizardDescriptor.putProperty(WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER, ""); // NOI18N
            }
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_RUN_CONFIGURE, runConfigureCheckBox.isSelected() ? "true" : "false"); // NOI18N
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_RUN_REBUILD, ""); // NOI18N
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        infoLabel = new javax.swing.JLabel();
        makefileRadioButton = new javax.swing.JRadioButton();
        makefileNameLabel = new javax.swing.JLabel();
        makefileNameTextField = new javax.swing.JTextField();
        makefileBrowseButton = new javax.swing.JButton();
        configureRadioButton = new javax.swing.JRadioButton();
        configureNameLabel = new javax.swing.JLabel();
        configureNameTextField = new javax.swing.JTextField();
        configureBrowseButton = new javax.swing.JButton();
        configureArgumentsLabel = new javax.swing.JLabel();
        configureArgumentsTextField = new javax.swing.JTextField();
        configureMakefileNameLabel = new javax.swing.JLabel();
        configureMakefileNameTextField = new javax.swing.JTextField();
        runConfigureCheckBox = new javax.swing.JCheckBox();
        makeCheckBox = new javax.swing.JCheckBox();
        runSubfolderCheckBox = new javax.swing.JCheckBox();
        configureRunFolderTextField = new javax.swing.JTextField();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        infoLabel.setText(bundle.getString("INTRO_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(infoLabel, gridBagConstraints);

        makefileRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(makefileRadioButton, bundle.getString("MAKEFILE_RADIO_BUTTON_TXT")); // NOI18N
        makefileRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        makefileRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makefileRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(makefileRadioButton, gridBagConstraints);
        makefileRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "MAKEFILE_RADIO_BUTTON_AD")); // NOI18N

        makefileNameLabel.setLabelFor(makefileNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(makefileNameLabel, bundle.getString("MAKEFILE_NAME_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(makefileNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(makefileNameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(makefileBrowseButton, bundle.getString("MAKEFILE_BROWSE_BUTTON")); // NOI18N
        makefileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makefileBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(makefileBrowseButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configureRadioButton, bundle.getString("CONFIGURE_RADIO_BUTTON_TXT")); // NOI18N
        configureRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        configureRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(configureRadioButton, gridBagConstraints);
        configureRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "CONFIGURE_RADIO_BUTTON_AD")); // NOI18N

        configureNameLabel.setLabelFor(configureNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(configureNameLabel, bundle.getString("CONFIGURE_NAME_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(configureNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(configureNameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configureBrowseButton, bundle.getString("CONFIGURE_BROWSE_BUTTON")); // NOI18N
        configureBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(configureBrowseButton, gridBagConstraints);

        configureArgumentsLabel.setLabelFor(configureArgumentsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(configureArgumentsLabel, bundle.getString("CONFIGURE_ARGUMENT_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(configureArgumentsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(configureArgumentsTextField, gridBagConstraints);

        configureMakefileNameLabel.setLabelFor(configureMakefileNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(configureMakefileNameLabel, bundle.getString("CONFIGURE_MAKEFILE_NAME_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(configureMakefileNameLabel, gridBagConstraints);

        configureMakefileNameTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(configureMakefileNameTextField, gridBagConstraints);

        runConfigureCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(runConfigureCheckBox, bundle.getString("RUN_CONFIGURE_CHECKBOX")); // NOI18N
        runConfigureCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(runConfigureCheckBox, gridBagConstraints);

        makeCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(makeCheckBox, bundle.getString("CLEAN_BUILD_CHECKBOX")); // NOI18N
        makeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(makeCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(runSubfolderCheckBox, org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "run.configure.subfolder")); // NOI18N
        runSubfolderCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runSubfolderCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(runSubfolderCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(configureRunFolderTextField, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(bundle.getString("MakefileOrConfigureInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);
        instructionsTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "CONFIGURE_HELP")); // NOI18N
        instructionsTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "CONFIGURE_HELP_AD")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void configureRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureRadioButtonActionPerformed
        //controller.stateChanged(null);
        update((DocumentEvent )null);
    }//GEN-LAST:event_configureRadioButtonActionPerformed

    private void makefileRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makefileRadioButtonActionPerformed
        //controller.stateChanged(null);
        update((DocumentEvent )null);
    }//GEN-LAST:event_makefileRadioButtonActionPerformed

    private void configureBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureBrowseButtonActionPerformed
        String seed;
        String root = (String) controller.getWizardDescriptor().getProperty(WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER);
        if (makefileNameTextField.getText().length() > 0) {
            seed = makefileNameTextField.getText();
        } else if (root != null && !root.isEmpty()) {
            seed = root;
        } else if (FileChooser.getCurrentChooserFile() != null) {
            seed = FileChooser.getCurrentChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                getString("CONFIGURE_CHOOSER_TITLE_TXT"),
                getString("MAKEFILE_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                new FileFilter[]{FileFilterFactory.getConfigureFileFilter()},
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilities.normalizeSlashes(path);
        configureNameTextField.setText(path);
        update((DocumentEvent )null);        
    }//GEN-LAST:event_configureBrowseButtonActionPerformed

    private void makefileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makefileBrowseButtonActionPerformed
        String seed;
        String root = (String) controller.getWizardDescriptor().getProperty(WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER);
        if (makefileNameTextField.getText().length() > 0) {
            seed = makefileNameTextField.getText();
        } else if (root != null && !root.isEmpty()) {
            seed = root;
        } else if (FileChooser.getCurrentChooserFile() != null) {
            seed = FileChooser.getCurrentChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                getString("MAKEFILE_CHOOSER_TITLE_TXT"),
                getString("MAKEFILE_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                new FileFilter[]{FileFilterFactory.getMakefileFileFilter()},
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilities.normalizeSlashes(path);
        makefileNameTextField.setText(path);
        update((DocumentEvent )null);       
    }//GEN-LAST:event_makefileBrowseButtonActionPerformed

    private void runSubfolderCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runSubfolderCheckBoxActionPerformed
        if (runSubfolderCheckBox.isSelected()) {
            configureRunFolderTextField.setEnabled(true);
        } else {
            configureRunFolderTextField.setEnabled(false);
        }
        update((DocumentEvent )null);
    }//GEN-LAST:event_runSubfolderCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel configureArgumentsLabel;
    private javax.swing.JTextField configureArgumentsTextField;
    private javax.swing.JButton configureBrowseButton;
    private javax.swing.JLabel configureMakefileNameLabel;
    private javax.swing.JTextField configureMakefileNameTextField;
    private javax.swing.JLabel configureNameLabel;
    private javax.swing.JTextField configureNameTextField;
    private javax.swing.JRadioButton configureRadioButton;
    private javax.swing.JTextField configureRunFolderTextField;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JCheckBox makeCheckBox;
    private javax.swing.JButton makefileBrowseButton;
    private javax.swing.JLabel makefileNameLabel;
    private javax.swing.JTextField makefileNameTextField;
    private javax.swing.JRadioButton makefileRadioButton;
    private javax.swing.JCheckBox runConfigureCheckBox;
    private javax.swing.JCheckBox runSubfolderCheckBox;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(BuildActionsPanel.class, s);
    }

    private void enableControls() {
        if (makefileRadioButton.isSelected()) {
            makefileNameLabel.setEnabled(true);
            makefileNameTextField.setEnabled(true);
            makefileBrowseButton.setEnabled(true);
            makeCheckBox.setEnabled(true);

            configureNameLabel.setEnabled(false);
            configureNameTextField.setEnabled(false);
            configureArgumentsLabel.setEnabled(false);
            configureBrowseButton.setEnabled(false);
            configureArgumentsTextField.setEnabled(false);
            runSubfolderCheckBox.setEnabled(false);
            configureRunFolderTextField.setEnabled(false);
            configureMakefileNameTextField.setEnabled(false);
            configureMakefileNameLabel.setEnabled(false);
            runConfigureCheckBox.setEnabled(false);
        } else {
            makefileNameLabel.setEnabled(false);
            makefileNameTextField.setEnabled(false);
            makefileBrowseButton.setEnabled(false);
            makeCheckBox.setEnabled(false);

            configureNameLabel.setEnabled(true);
            configureNameTextField.setEnabled(true);
            configureArgumentsLabel.setEnabled(true);
            configureBrowseButton.setEnabled(true);
            configureArgumentsTextField.setEnabled(true);
            runSubfolderCheckBox.setEnabled(true);
            configureRunFolderTextField.setEnabled(runSubfolderCheckBox.isSelected());
            configureMakefileNameTextField.setEnabled(true);
            configureMakefileNameLabel.setEnabled(true);
            runConfigureCheckBox.setEnabled(true);
        }
    }

    private class RefreshRunnable implements Runnable {

        private AtomicInteger generation = new AtomicInteger(0);
        private final Object lock = new Object();
        private WizardDescriptor settings;

        public RefreshRunnable() {
        }

        private void start(WizardDescriptor settings) {
            this.settings = settings;
            generation.incrementAndGet();
        }

        @Override
        public void run() {
            final int startCount = generation.get();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    enableControls();
                }

            });
            // Validate fields
            String newConfigureNameTextField = null;
            String newConfigureMakefileNameTextField = null;
            String newMakefileNameTextField = null;
            if (makefileRadioButton.isSelected()) {
                if (makefileNameTextField.getText().isEmpty()) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOMAKEFILE"); // NOI18N
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                    return;//false;
                }

                if (!CndPathUtilities.isPathAbsolute(makefileNameTextField.getText())
                        || !NewProjectWizardUtils.fileExists(makefileNameTextField.getText(), controller.getWizardDescriptor())
                        || NewProjectWizardUtils.isDirectory(makefileNameTextField.getText(), controller.getWizardDescriptor())) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "MAKEFILEDOESNOTEXIST"); // NOI18N
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                    return;//false;
                }

                String mn = makefileNameTextField.getText();
                int i = mn.replace('\\', '/').lastIndexOf('/');
                if (i > 0) {// && !configureNameTextField.getText().isEmpty()) {
                    String cn;
                    ExecutionEnvironment env = (ExecutionEnvironment) settings.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV);
                    if (env != null) {
                        FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                        cn = ConfigureUtils.findConfigureScript(fileSystem.findResource(mn.substring(0, i)));
                    } else {
                        cn = ConfigureUtils.findConfigureScript(mn.substring(0, i));
                    }
                    if (cn != null && NewProjectWizardUtils.fileExists(cn, controller.getWizardDescriptor())) {
                        newConfigureNameTextField = cn;
                    }
                }
            } else {
                newConfigureMakefileNameTextField = ""; // NOI18N
                if (configureNameTextField.getText().isEmpty()) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOCONFIGUREFILE"); // NOI18N
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                    return;//false;
                }
                if (!CndPathUtilities.isPathAbsolute(configureNameTextField.getText())) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "CONFIGUREFILEDOESNOTEXIST"); // NOI18N
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                    return;//false;
                }
                FileObject file = NewProjectWizardUtils.getFileObject(configureNameTextField.getText(), controller.getWizardDescriptor());
                if (file == null || !file.isValid() || file.isFolder()) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "CONFIGUREFILEDOESNOTEXIST"); // NOI18N
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                    return;//false;
                } else if (!ConfigureUtils.isRunnable(file)) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "CONFIGUREFILEISNOTEXECUTABLE"); // NOI18N
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                    return;//false;
                }

                int i = configureNameTextField.getText().replace('\\', '/').lastIndexOf('/');  // NOI18N
                if (i > 0) {
                    String mn = configureNameTextField.getText().substring(0, i + 1);
                    if (runSubfolderCheckBox.isSelected()) {
                        String subfolder = configureRunFolderTextField.getText().trim();
                        if (!subfolder.isEmpty()) {
                            mn += subfolder + "/";  // NOI18N
                        }
                    }
                    mn += "Makefile";  // NOI18N
                    newConfigureMakefileNameTextField = mn;
                    if (NewProjectWizardUtils.fileExists(mn, controller.getWizardDescriptor())) {
                        newMakefileNameTextField = mn;
                    }
                }
            }
            if (startCount < generation.get()) {
                return;
            }                

            final String finalConfigureNameTextField = newConfigureNameTextField;
            final String finalConfigureMakefileNameTextField = newConfigureMakefileNameTextField;
            final String finalMakefileNameTextField = newMakefileNameTextField;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (startCount < generation.get()) {
                        return;
                    }                
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
                    try {
                        removeDocumentLiseners();
                        if (finalConfigureNameTextField != null && !configureNameTextField.getText().equals(finalConfigureNameTextField)) {
                            configureNameTextField.setText(finalConfigureNameTextField);
                        }
                        if (finalConfigureMakefileNameTextField != null && !configureMakefileNameTextField.getText().equals(finalConfigureMakefileNameTextField)) {
                            configureMakefileNameTextField.setText(finalConfigureMakefileNameTextField);
                        }
                        if (finalMakefileNameTextField != null && !makefileNameTextField.getText().equals(finalMakefileNameTextField)) {
                            makefileNameTextField.setText(finalMakefileNameTextField);
                        }
                    } finally {
                        addDocumentLiseners();
                    }
                    controller.stateChanged(null);
                }
            });
        }
    }
}

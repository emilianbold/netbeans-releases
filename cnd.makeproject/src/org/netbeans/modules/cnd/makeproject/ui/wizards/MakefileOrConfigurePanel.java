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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class MakefileOrConfigurePanel extends javax.swing.JPanel implements HelpCtx.Provider{
    
    private DocumentListener documentListener;
    private MakefileOrConfigureDescriptorPanel descriptorPanel;
    
    
    MakefileOrConfigurePanel(MakefileOrConfigureDescriptorPanel buildActionsDescriptorPanel) {
        initComponents();
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        this.descriptorPanel = buildActionsDescriptorPanel;
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
    }
    
    private void addDocumentLiseners(){
        // Add change listeners
        makefileNameTextField.getDocument().addDocumentListener(documentListener);
        configureNameTextField.getDocument().addDocumentListener(documentListener);
    }

    private void removeDocumentLiseners(){
        makefileNameTextField.getDocument().removeDocumentListener(documentListener);
        configureNameTextField.getDocument().removeDocumentListener(documentListener);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewMakeWizardP11"); // NOI18N
    }
    
    private void update(DocumentEvent e) {
        descriptorPanel.stateChanged(null);
    }
    
    void read(WizardDescriptor wizardDescriptor) {
        try {
            removeDocumentLiseners();
            String path = (String) wizardDescriptor.getProperty("simpleModeFolder"); // NOI18N
            if (path != null) {
                boolean selected = false;
                String makeFile = ConfigureUtils.findMakefile(path);
                if (makeFile != null) {
                    makefileNameTextField.setText(makeFile);
                    makefileRadioButton.setSelected(true);
                    selected = true;
                }
                String configureScript = ConfigureUtils.findConfigureScript(path);
                if (configureScript != null) {
                    if (!selected) {
                        configureRadioButton.setSelected(true);
                        runConfigureCheckBox.setSelected(true);
                    }
                    configureNameTextField.setText(configureScript);
                    configureArgumentsTextField.setText(ConfigureUtils.getConfigureArguments(configureScript,"")); // NOI18N
                }
            }
        } finally {
            addDocumentLiseners();
        }
    }
    
    void store(WizardDescriptor wizardDescriptor) {
        if (makefileRadioButton.isSelected()) {
            wizardDescriptor.putProperty("makefileName", makefileNameTextField.getText()); // NOI18N
            wizardDescriptor.putProperty("configureName", ""); // NOI18N
            wizardDescriptor.putProperty("configureArguments", ""); // NOI18N
            wizardDescriptor.putProperty("makeProject", makeCheckBox.isSelected() ? "true" : "false"); // NOI18N
            wizardDescriptor.putProperty("runConfigure", ""); // NOI18N
        } else {
            wizardDescriptor.putProperty("makefileName", configureMakefileNameTextField.getText()); // NOI18N
            wizardDescriptor.putProperty("configureName", configureNameTextField.getText()); // NOI18N
            wizardDescriptor.putProperty("configureArguments", configureArgumentsTextField.getText()); // NOI18N
            wizardDescriptor.putProperty("runConfigure", runConfigureCheckBox.isSelected() ? "true" : "false"); // NOI18N
            wizardDescriptor.putProperty("makeProject", ""); // NOI18N
        }
    }
    
    boolean valid(WizardDescriptor settings) {
        // Enable/disable components
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
            configureMakefileNameTextField.setEnabled(true);
            configureMakefileNameLabel.setEnabled(true);
            runConfigureCheckBox.setEnabled(true);
        }
        // Validate fields
        try {
            removeDocumentLiseners();
            if (makefileRadioButton.isSelected()) {
                if (makefileNameTextField.getText().isEmpty()) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOMAKEFILE"); // NOI18N
                    descriptorPanel.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); // NOI18N
                    return false;
                }

                if (!CndPathUtilitities.isPathAbsolute(makefileNameTextField.getText()) || !new File(makefileNameTextField.getText()).exists() || new File(makefileNameTextField.getText()).isDirectory()) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "MAKEFILEDOESNOTEXIST"); // NOI18N
                    descriptorPanel.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); // NOI18N
                    return false;
                }

                String mn = makefileNameTextField.getText();
                int i = mn.replace('\\', '/').lastIndexOf('/');
                if (i > 0) {// && !configureNameTextField.getText().isEmpty()) {
                    String cn = ConfigureUtils.findConfigureScript(mn.substring(0,i));
                    if (cn != null && new File(cn).exists()) {
                        configureNameTextField.setText(cn);
                    }
                }
            } else {
                configureMakefileNameTextField.setText(""); // NOI18N
                if (configureNameTextField.getText().isEmpty()) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOCONFIGUREFILE"); // NOI18N
                    descriptorPanel.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); // NOI18N
                    return false;
                }
                File file = new File(configureNameTextField.getText());
                if (!CndPathUtilitities.isPathAbsolute(configureNameTextField.getText()) ||
                    !file.exists() || file.isDirectory()) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "CONFIGUREFILEDOESNOTEXIST"); // NOI18N
                    descriptorPanel.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); // NOI18N
                    return false;
                } else if (!ConfigureUtils.isRunnable(file)) {
                    String msg = NbBundle.getMessage(BuildActionsPanel.class, "CONFIGUREFILEISNOTEXECUTABLE"); // NOI18N
                    descriptorPanel.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); // NOI18N
                    return false;
                }

                int i = configureNameTextField.getText().replace('\\', '/').lastIndexOf('/');  // NOI18N
                if (i > 0) {
                    String mn = configureNameTextField.getText().substring(0, i+1) + "Makefile";  // NOI18N
                    configureMakefileNameTextField.setText(mn); // NOI18N
                    if (new File(mn).exists()) {
                        makefileNameTextField.setText(mn);
                    }
                }
            }
        } finally {
            addDocumentLiseners();
        }
        
        return true;
    }
    
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        configureMakefileNameLabel = new javax.swing.JLabel();
        configureMakefileNameTextField = new javax.swing.JTextField();
        runConfigureCheckBox = new javax.swing.JCheckBox();
        makeCheckBox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(323, 223));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        infoLabel.setText(bundle.getString("INTRO_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(infoLabel, gridBagConstraints);

        makefileRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("MAKEFILE_RADIO_BUTTON_MN").charAt(0));
        makefileRadioButton.setSelected(true);
        makefileRadioButton.setText(bundle.getString("MAKEFILE_RADIO_BUTTON_TXT")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(makefileRadioButton, gridBagConstraints);
        makefileRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "MAKEFILE_RADIO_BUTTON_AD")); // NOI18N

        makefileNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("MAKEFILE_NAME_MN").charAt(0));
        makefileNameLabel.setLabelFor(makefileNameTextField);
        makefileNameLabel.setText(bundle.getString("MAKEFILE_NAME_LBL")); // NOI18N
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

        makefileBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("MAKEFILE_BROWSE_BUTTON_MN").charAt(0));
        makefileBrowseButton.setText(bundle.getString("MAKEFILE_BROWSE_BUTTON")); // NOI18N
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

        configureRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("CONFIGURE_RADIO_BUTTON_MN").charAt(0));
        configureRadioButton.setText(bundle.getString("CONFIGURE_RADIO_BUTTON_TXT")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(configureRadioButton, gridBagConstraints);
        configureRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "CONFIGURE_RADIO_BUTTON_AD")); // NOI18N

        configureNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("CONFIGURE_NAME_MN").charAt(0));
        configureNameLabel.setLabelFor(configureNameTextField);
        configureNameLabel.setText(bundle.getString("CONFIGURE_NAME_LBL")); // NOI18N
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

        configureBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("CONFIGURE_BROWSE_BUTTON_MN").charAt(0));
        configureBrowseButton.setText(bundle.getString("CONFIGURE_BROWSE_BUTTON")); // NOI18N
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

        configureArgumentsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("CONFIGURE_ARGUMENT_LABEL_MN").charAt(0));
        configureArgumentsLabel.setLabelFor(configureArgumentsTextField);
        configureArgumentsLabel.setText(bundle.getString("CONFIGURE_ARGUMENT_LABEL_TXT")); // NOI18N
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

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(bundle.getString("MakefileOrConfigureInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);
        instructionsTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "CONFIGURE_HELP")); // NOI18N
        instructionsTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakefileOrConfigurePanel.class, "CONFIGURE_HELP_AD")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        configureMakefileNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("CONFIGURE_MAKEFILE_NAME_MN").charAt(0));
        configureMakefileNameLabel.setLabelFor(configureMakefileNameTextField);
        configureMakefileNameLabel.setText(bundle.getString("CONFIGURE_MAKEFILE_NAME_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(configureMakefileNameLabel, gridBagConstraints);

        configureMakefileNameTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(configureMakefileNameTextField, gridBagConstraints);

        runConfigureCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("RUN_CONFIGURE_CHECKBOX").charAt(0));
        runConfigureCheckBox.setSelected(true);
        runConfigureCheckBox.setText(bundle.getString("RUN_CONFIGURE_CHECKBOX")); // NOI18N
        runConfigureCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 16, 0, 0);
        add(runConfigureCheckBox, gridBagConstraints);

        makeCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(makeCheckBox, bundle.getString("CLEAN_BUILD_CHECKBOX")); // NOI18N
        makeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 16, 0, 0);
        add(makeCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void configureRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureRadioButtonActionPerformed
        descriptorPanel.stateChanged(null);
    }//GEN-LAST:event_configureRadioButtonActionPerformed
    
    private void makefileRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makefileRadioButtonActionPerformed
        descriptorPanel.stateChanged(null);
    }//GEN-LAST:event_makefileRadioButtonActionPerformed
    
    private void configureBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureBrowseButtonActionPerformed
        String seed = null;
        if (makefileNameTextField.getText().length() > 0) {
            seed = makefileNameTextField.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        JFileChooser fileChooser = new FileChooser(
                getString("CONFIGURE_CHOOSER_TITLE_TXT"),
                getString("MAKEFILE_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                new FileFilter[] {FileFilterFactory.getConfigureFileFilter()},
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilitities.normalize(path);
        configureNameTextField.setText(path);
    }//GEN-LAST:event_configureBrowseButtonActionPerformed
    
    private void makefileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makefileBrowseButtonActionPerformed
        String seed = null;
        if (makefileNameTextField.getText().length() > 0) {
            seed = makefileNameTextField.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        JFileChooser fileChooser = new FileChooser(
                getString("MAKEFILE_CHOOSER_TITLE_TXT"),
                getString("MAKEFILE_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                new FileFilter[] {FileFilterFactory.getMakefileFileFilter()},
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilitities.normalize(path);
        makefileNameTextField.setText(path);
    }//GEN-LAST:event_makefileBrowseButtonActionPerformed
    
    
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
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JCheckBox makeCheckBox;
    private javax.swing.JButton makefileBrowseButton;
    private javax.swing.JLabel makefileNameLabel;
    private javax.swing.JTextField makefileNameTextField;
    private javax.swing.JRadioButton makefileRadioButton;
    private javax.swing.JCheckBox runConfigureCheckBox;
    // End of variables declaration//GEN-END:variables
    
    private static String getString(String s) {
        return NbBundle.getBundle(BuildActionsPanel.class).getString(s);
    }
}

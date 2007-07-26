/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.utils.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.api.utils.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.PeExecutableFileFilter;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class BuildActionsPanel extends javax.swing.JPanel implements HelpCtx.Provider{
    
    private DocumentListener documentListener;
    private boolean valid = false;
    private BuildActionsDescriptorPanel buildActionsDescriptorPanel;
    private String makefileName = null;
    
    private static String DEF_WORKING_DIR = ""; // NOI18N
    private static String DEF_BUILD_COMMAND = "make"; // NOI18N
    private static String DEF_CLEAN_COMMAND = "make clean"; // NOI18N
    private static String DEF_BUILD_COMMAND_FMT = "{0} -f {1}"; // NOI18N
    private static String DEF_CLEAN_COMMAND_FMT = "{0} -f {1} clean"; // NOI18N
    
    public BuildActionsPanel(BuildActionsDescriptorPanel buildActionsDescriptorPanel) {
        initComponents();
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        this.buildActionsDescriptorPanel = buildActionsDescriptorPanel;
        documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        
        
        // Add change listeners
        buildCommandTextField.getDocument().addDocumentListener(documentListener);
        buildCommandWorkingDirTextField.getDocument().addDocumentListener(documentListener);
        cleanCommandTextField.getDocument().addDocumentListener(documentListener);
        outputTextField.getDocument().addDocumentListener(documentListener);
        
        // init focus
        buildCommandWorkingDirTextField.selectAll();
        buildCommandWorkingDirTextField.requestFocus();
        
        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("BUILD_ACTIONS_PANEL_AD"));
        buildCommandTextField.getAccessibleContext().setAccessibleDescription(getString("BUILD_COMMAND_AD"));
        buildCommandWorkingDirTextField.getAccessibleContext().setAccessibleDescription(getString("WORKING_DIR_AD"));
        cleanCommandTextField.getAccessibleContext().setAccessibleDescription(getString("CLEAN_COMMAND_AD"));
        outputTextField.getAccessibleContext().setAccessibleDescription(getString("OUTPUT_AD"));
        buildCommandWorkingDirBrowseButton.getAccessibleContext().setAccessibleDescription(getString("WORKING_DIR_BROWSE_BUTTON_AD"));
        outputBrowseButton.getAccessibleContext().setAccessibleDescription(getString("OUTPUT_BROWSE_BUTTON_AD"));
    }
    
    class MakefileDocumentListener implements DocumentListener {
        public void changedUpdate( DocumentEvent e ) {
            makefileFieldChanged();
        }
        
        public void insertUpdate( DocumentEvent e ) {
            makefileFieldChanged();
        }
        
        public void removeUpdate( DocumentEvent e ) {
            makefileFieldChanged();
        }
    }
    
    private void makefileFieldChanged() {
        File makefile = new File(makefileName);
        if (makefile.getParent() != null) {
            buildCommandWorkingDirTextField.setText(FilePathAdaptor.normalize(makefile.getParent()));
            String buildCommand = MessageFormat.format(DEF_BUILD_COMMAND_FMT, new Object[]{DEF_BUILD_COMMAND, makefile.getName()});
            String cleanCommand = MessageFormat.format(DEF_CLEAN_COMMAND_FMT, new Object[]{DEF_BUILD_COMMAND, makefile.getName()});
            buildCommandTextField.setText(buildCommand);
            cleanCommandTextField.setText(cleanCommand);
        }
    }
    
    private void initFields() {
        // Set default values
        buildCommandWorkingDirTextField.setText(DEF_WORKING_DIR);
        buildCommandTextField.setText(DEF_BUILD_COMMAND);
        cleanCommandTextField.setText(DEF_CLEAN_COMMAND);
        outputTextField.setText(""); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(BuildActionsPanel.class);
    }
    
    private void update(DocumentEvent e) {
        buildActionsDescriptorPanel.stateChanged(null);
    }
    
    void read(WizardDescriptor wizardDescriptor) {
        String mn = (String)wizardDescriptor.getProperty("makefileName"); // NOI18N
        if (makefileName == null || !makefileName.equals(mn)) {
            initFields();
            makefileName = mn;
            makefileFieldChanged();
        }
    }
    
    void store(WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty("buildCommandWorkingDirTextField", buildCommandWorkingDirTextField.getText()); // NOI18N
        wizardDescriptor.putProperty("buildCommandTextField", buildCommandTextField.getText()); // NOI18N
        wizardDescriptor.putProperty("cleanCommandTextField", cleanCommandTextField.getText()); // NOI18N
        wizardDescriptor.putProperty("outputTextField", outputTextField.getText()); // NOI18N
    }
    
    boolean valid(WizardDescriptor settings) {
        if (buildCommandWorkingDirTextField.getText().length() == 0) {
            String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOWORKINGDIR"); // NOI18N
            buildActionsDescriptorPanel.getWizardDescriptor().putProperty("WizardPanel_errorMessage", msg); // NOI18N
            return false;
        }
        if (buildCommandWorkingDirTextField.getText().length() > 0) {
            if (!IpeUtils.isPathAbsolute(buildCommandWorkingDirTextField.getText()) || !new File(buildCommandWorkingDirTextField.getText()).exists()) {
                String msg = NbBundle.getMessage(BuildActionsPanel.class, "WORKINGDIRDOESNOTEXIST"); // NOI18N
                buildActionsDescriptorPanel.getWizardDescriptor().putProperty("WizardPanel_errorMessage", msg); // NOI18N
                return false;
            }
        }
        if (outputTextField.getText().length() > 0) {
            if (!IpeUtils.isPathAbsolute(outputTextField.getText())) {
                String msg = NbBundle.getMessage(BuildActionsPanel.class, "BUILDRESULTNOTABSOLUTE"); // NOI18N
                buildActionsDescriptorPanel.getWizardDescriptor().putProperty("WizardPanel_errorMessage", msg); // NOI18N
                return false;
            }
        }
        return true;
    }
    
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buildCommandWorkingDirLabel = new javax.swing.JLabel();
        buildCommandWorkingDirTextField = new javax.swing.JTextField();
        buildCommandWorkingDirBrowseButton = new javax.swing.JButton();
        buildCommandLabel = new javax.swing.JLabel();
        buildCommandTextField = new javax.swing.JTextField();
        cleanCommandLabel = new javax.swing.JLabel();
        cleanCommandTextField = new javax.swing.JTextField();
        outputLabel = new javax.swing.JLabel();
        outputTextField = new javax.swing.JTextField();
        outputBrowseButton = new javax.swing.JButton();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        group2Label = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(323, 223));
        buildCommandWorkingDirLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("WORKING_DIR_MN").charAt(0));
        buildCommandWorkingDirLabel.setLabelFor(buildCommandWorkingDirTextField);
        buildCommandWorkingDirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("WORKING_DIR_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(buildCommandWorkingDirLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        add(buildCommandWorkingDirTextField, gridBagConstraints);

        buildCommandWorkingDirBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("WORKING_DIR_BROWSE_BUTTON_MN").charAt(0));
        buildCommandWorkingDirBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("WORKING_DIR_BROWSE_BUTTON_TXT"));
        buildCommandWorkingDirBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildCommandWorkingDirBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        add(buildCommandWorkingDirBrowseButton, gridBagConstraints);

        buildCommandLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("BUILD_COMMAND_MN").charAt(0));
        buildCommandLabel.setLabelFor(buildCommandTextField);
        buildCommandLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("BUILD_COMMAND_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(buildCommandLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(buildCommandTextField, gridBagConstraints);

        cleanCommandLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("CLEAN_COMMAND_MN").charAt(0));
        cleanCommandLabel.setLabelFor(cleanCommandTextField);
        cleanCommandLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("CLEAN_COMMAND_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cleanCommandLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(cleanCommandTextField, gridBagConstraints);

        outputLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("OUTPUT_MN").charAt(0));
        outputLabel.setLabelFor(outputTextField);
        outputLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("OUTPUT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(outputLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        add(outputTextField, gridBagConstraints);

        outputBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("OUTPUT_BROWSE_BUTTON_MN").charAt(0));
        outputBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("OUTPUT_BROWSE_BUTTON_TXT"));
        outputBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        add(outputBrowseButton, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("BuildActionsInstructions"));
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        group2Label.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("GROUP2_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(group2Label, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void outputBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputBrowseButtonActionPerformed
        String seed = null;
        if (outputTextField.getText().length() > 0) {
            seed = outputTextField.getText();
        } else if (buildCommandWorkingDirTextField.getText().length() > 0) {
            seed = buildCommandWorkingDirTextField.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        FileFilter[] filters;
        if (Utilities.isWindows()){
            filters = new FileFilter[] {PeExecutableFileFilter.getInstance(),
            ElfStaticLibraryFileFilter.getInstance(),
            PeDynamicLibraryFileFilter.getInstance()};
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            filters = new FileFilter[] {MacOSXExecutableFileFilter.getInstance(),
            ElfStaticLibraryFileFilter.getInstance(),
            MacOSXDynamicLibraryFileFilter.getInstance()};
        } else {
            filters = new FileFilter[] {ElfExecutableFileFilter.getInstance(),
            ElfStaticLibraryFileFilter.getInstance(),
            ElfDynamicLibraryFileFilter.getInstance()};
        }
        JFileChooser fileChooser = new FileChooser(
                getString("OUTPUT_CHOOSER_TITLE_TXT"),
                getString("OUTPUT_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                filters,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION)
            return;
        //String path = IpeUtils.toRelativePath(buildCommandWorkingDirTextField.getText(), fileChooser.getSelectedFile().getPath()); // FIXUP: not always relative path
        String path = FilePathAdaptor.normalize(fileChooser.getSelectedFile().getPath());
        outputTextField.setText(path);
    }//GEN-LAST:event_outputBrowseButtonActionPerformed
    
    private void buildCommandWorkingDirBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildCommandWorkingDirBrowseButtonActionPerformed
        String seed = null;
        if (buildCommandWorkingDirTextField.getText().length() > 0) {
            seed = buildCommandWorkingDirTextField.getText();
//        } else if (makefileNameTextField.getText().length() > 0) {
//            seed = makefileNameTextField.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        
        JFileChooser fileChooser = new FileChooser(
                getString("WORKING_DIR_CHOOSER_TITLE_TXT"),
                getString("WORKING_DIR_BUTTON_TXT"),
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION)
            return;
        String path = fileChooser.getSelectedFile().getPath();
        path = FilePathAdaptor.normalize(path);
        buildCommandWorkingDirTextField.setText(path);
    }//GEN-LAST:event_buildCommandWorkingDirBrowseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel buildCommandLabel;
    private javax.swing.JTextField buildCommandTextField;
    private javax.swing.JButton buildCommandWorkingDirBrowseButton;
    private javax.swing.JLabel buildCommandWorkingDirLabel;
    private javax.swing.JTextField buildCommandWorkingDirTextField;
    private javax.swing.JLabel cleanCommandLabel;
    private javax.swing.JTextField cleanCommandTextField;
    private javax.swing.JLabel group2Label;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JButton outputBrowseButton;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JTextField outputTextField;
    // End of variables declaration//GEN-END:variables
    
    private static String getString(String s) {
        return NbBundle.getBundle(BuildActionsPanel.class).getString(s);
    }
}

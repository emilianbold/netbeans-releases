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

import java.io.File;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.ui.DocumentAdapter;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class BuildActionsPanel extends javax.swing.JPanel implements HelpCtx.Provider{
    
    private final DocumentListener documentListener;
    private boolean valid = false;
    private final BuildActionsDescriptorPanel controller;
    private String makefileName = null;
    
    private static String DEF_WORKING_DIR = ""; // NOI18N
    private static String DEF_BUILD_COMMAND = "${MAKE}"; // NOI18N
    private static String DEF_CLEAN_COMMAND = "${MAKE} clean"; // NOI18N
    private static String DEF_BUILD_COMMAND_FMT = "{0} -f {1}"; // NOI18N
    private static String DEF_CLEAN_COMMAND_FMT = "{0} -f {1} clean"; // NOI18N
    
    /*package-local*/ BuildActionsPanel(BuildActionsDescriptorPanel buildActionsDescriptorPanel) {
        initComponents();
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        this.controller = buildActionsDescriptorPanel;
        documentListener = new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                BuildActionsPanel.this.update();
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
    
    private void makefileFieldChanged() {
        File makefile = new File(makefileName);
        if (makefile.getParent() != null) {
            buildCommandWorkingDirTextField.setText(CndPathUtilitities.normalizeSlashes(makefile.getParent()));
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
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(BuildActionsPanel.class);
    }
    
    private void update() {
        controller.stateChanged(null);
    }
    
    void read(WizardDescriptor wizardDescriptor) {
        String mn = (String)wizardDescriptor.getProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH);
        if (makefileName == null || !makefileName.equals(mn)) {
            initFields();
            makefileName = mn;
            makefileFieldChanged();
        }
    }
    
    void store(WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_WORKING_DIR, buildCommandWorkingDirTextField.getText()); // NOI18N
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_BUILD_COMMAND, buildCommandTextField.getText()); // NOI18N
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_CLEAN_COMMAND, cleanCommandTextField.getText()); // NOI18N
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_BUILD_RESULT, outputTextField.getText()); // NOI18N
    }
    
    boolean valid(WizardDescriptor settings) {
        if (buildCommandWorkingDirTextField.getText().length() == 0) {
            String msg = NbBundle.getMessage(BuildActionsPanel.class, "NOWORKINGDIR"); // NOI18N
            controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
            //return false;
        }
        if (buildCommandWorkingDirTextField.getText().length() > 0) {
            if (!CndPathUtilitities.isPathAbsolute(buildCommandWorkingDirTextField.getText()) 
                    || !NewProjectWizardUtils.fileExists(buildCommandWorkingDirTextField.getText(), controller.getWizardDescriptor())) {
                String msg = NbBundle.getMessage(BuildActionsPanel.class, "WORKINGDIRDOESNOTEXIST"); // NOI18N
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                //return false;
            }
        }
        if (outputTextField.getText().length() > 0) {
            if (!CndPathUtilitities.isPathAbsolute(outputTextField.getText())) {
                String msg = NbBundle.getMessage(BuildActionsPanel.class, "BUILDRESULTNOTABSOLUTE"); // NOI18N
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
                //return false;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        buildCommandWorkingDirLabel.setLabelFor(buildCommandWorkingDirTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildCommandWorkingDirLabel, bundle.getString("WORKING_DIR_LBL")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(buildCommandWorkingDirBrowseButton, bundle.getString("WORKING_DIR_BROWSE_BUTTON_TXT")); // NOI18N
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

        buildCommandLabel.setLabelFor(buildCommandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(buildCommandLabel, bundle.getString("BUILD_COMMAND_LBL")); // NOI18N
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

        cleanCommandLabel.setLabelFor(cleanCommandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(cleanCommandLabel, bundle.getString("CLEAN_COMMAND_LBL")); // NOI18N
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

        outputLabel.setLabelFor(outputTextField);
        org.openide.awt.Mnemonics.setLocalizedText(outputLabel, bundle.getString("OUTPUT_LBL")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(outputBrowseButton, bundle.getString("OUTPUT_BROWSE_BUTTON_TXT")); // NOI18N
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
        instructionsTextArea.setText(bundle.getString("BuildActionsInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
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

        group2Label.setText(bundle.getString("GROUP2_LBL")); // NOI18N
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
        } else if (FileChooser.getCurrentChooserFile() != null) {
            seed = FileChooser.getCurrentChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        FileFilter[] filters = FileFilterFactory.getBinaryFilters();
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                getString("OUTPUT_CHOOSER_TITLE_TXT"),
                getString("OUTPUT_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                filters,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        //String path = CndPathUtilitities.toRelativePath(buildCommandWorkingDirTextField.getText(), fileChooser.getSelectedFile().getPath()); // FIXUP: not always relative path
        String path = CndPathUtilitities.normalizeSlashes(fileChooser.getSelectedFile().getPath());
        outputTextField.setText(path);
    }//GEN-LAST:event_outputBrowseButtonActionPerformed
    
    private void buildCommandWorkingDirBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildCommandWorkingDirBrowseButtonActionPerformed
        String seed = null;
        if (buildCommandWorkingDirTextField.getText().length() > 0) {
            seed = buildCommandWorkingDirTextField.getText();
//        } else if (makefileNameTextField.getText().length() > 0) {
//            seed = makefileNameTextField.getText();
        } else if (FileChooser.getCurrentChooserFile() != null) {
            seed = FileChooser.getCurrentChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                getString("WORKING_DIR_CHOOSER_TITLE_TXT"),
                getString("WORKING_DIR_BUTTON_TXT"),
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        path = CndPathUtilitities.normalizeSlashes(path);
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public final class ImportProjectPanel extends JPanel {

    private final ImportProjectDescriptorPanel controller;

    /** Creates new form ImportProjectVisualPanel1 */
    public ImportProjectPanel(ImportProjectDescriptorPanel controller) {
        initComponents();

        jLabel1.setVisible(false);
        projectFolder.setVisible(false);
        browseButton.setVisible(false);

        jLabel2.setVisible(false);
        configureFlags.setVisible(false);
        buildProjectCheckBox.setVisible(false);

        todoPane.setBackground(jPanel1.getBackground());
        this.controller = controller;
        projectFolder.setText(controller.getWizardStorage().getPath());
        configureFlags.setText(controller.getWizardStorage().getFlags());
        setMainProjectCheckBox.setSelected(controller.getWizardStorage().isSetMain());
        buildProjectCheckBox.setSelected(controller.getWizardStorage().isBuildProject());
        addListeners();
    }

    private void addListeners() {
        projectFolder.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                controller.getWizardStorage().setPath(projectFolder.getText());
                checkFolderAndFlags();
            }

            public void removeUpdate(DocumentEvent e) {
                controller.getWizardStorage().setPath(projectFolder.getText());
                checkFolderAndFlags();
            }

            public void changedUpdate(DocumentEvent e) {
                controller.getWizardStorage().setPath(projectFolder.getText());
                checkFolderAndFlags();
            }
        });
        configureFlags.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                controller.getWizardStorage().setFlags(configureFlags.getText());
                checkFolderAndFlags();
            }

            public void removeUpdate(DocumentEvent e) {
                controller.getWizardStorage().setFlags(configureFlags.getText());
                checkFolderAndFlags();
            }

            public void changedUpdate(DocumentEvent e) {
                controller.getWizardStorage().setFlags(configureFlags.getText());
                checkFolderAndFlags();
            }
        });
        setMainProjectCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                controller.getWizardStorage().setSetMain(setMainProjectCheckBox.isSelected());
            }
        });
        buildProjectCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                controller.getWizardStorage().setBuildProject(buildProjectCheckBox.isSelected());
            }
        });
    }

    void read(WizardDescriptor settings){
        projectFolder.setText((String) settings.getProperty("simpleModeFolder")); // NOI18N
    }

    private void checkFolderAndFlags(){
        boolean isProject = controller.getWizardStorage().isNbProjectFolder();
        String configure = null;
        String make = null;
        if (!isProject)  {
            configure = controller.getWizardStorage().getConfigure();
            make = controller.getWizardStorage().getMake();
        }
        if (configure != null && make == null){
            jLabel2.setVisible(true);
            configureFlags.setVisible(true);
        } else {
            jLabel2.setVisible(false);
            configureFlags.setVisible(false);
        }

        StringBuilder buf = new StringBuilder();
        if (configure == null) {
            if (make == null) {
                // no action
                if (isProject) {
                    buf.append(getString("Prompt_AlreadyProject")); // NOI18N
                } else {
                    buf.append(getString("Prompt_NoConfigure_NoMake")); // NOI18N
                }
            } else {
                // rebuild project
                buf.append(getString("Prompt_Intention")); // NOI18N
                buf.append("\n- "); // NOI18N
                buf.append(getString("Prompt_CreateProject", controller.getWizardStorage().getPath())); // NOI18N
                buf.append("\n- "); // NOI18N
                buf.append(getString("Prompt_RebuildProject", "make clean", "make")); // NOI18N
            }
        } else {
            if (make == null) {
                // configure and build project
                buf.append(getString("Prompt_Intention")); // NOI18N
                buf.append("\n- "); // NOI18N
                buf.append(getString("Prompt_CreateProject", controller.getWizardStorage().getPath())); // NOI18N
                buf.append("\n- "); // NOI18N
                buf.append(getString("Prompt_ConfigureProject", "configure "+controller.getWizardStorage().getRealFlags())); // NOI18N
                buf.append("\n- "); // NOI18N
                buf.append(getString("Prompt_BuildProject", "make")); // NOI18N
            } else {
                // rebuild project
                buf.append(getString("Prompt_Intention")); // NOI18N
                buf.append("\n- "); // NOI18N
                buf.append(getString("Prompt_CreateProject", controller.getWizardStorage().getPath())); // NOI18N
                buf.append("\n- "); // NOI18N
                buf.append(getString("Prompt_RebuildProject", "make clean", "make")); // NOI18N
            }
        }
        todoPane.setText(buf.toString());
    }

    private String getString(String key, String ... params){
        return NbBundle.getMessage(ImportProjectPanel.class, key, params);
    }

    @Override
    public String getName() {
        return getString("Step1"); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        configureFlags = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        todoPane = new javax.swing.JTextPane();
        setMainProjectCheckBox = new javax.swing.JCheckBox();
        buildProjectCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ImportProjectPanel.class, "ImportProjectPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(projectFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ImportProjectPanel.class, "ImportProjectPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(browseButton, gridBagConstraints);

        jLabel2.setLabelFor(configureFlags);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ImportProjectPanel.class, "ImportProjectPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(configureFlags, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);

        todoPane.setBorder(null);
        todoPane.setEditable(false);
        todoPane.setFocusable(false);
        jScrollPane1.setViewportView(todoPane);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jPanel1, gridBagConstraints);

        setMainProjectCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setMainProjectCheckBox, org.openide.util.NbBundle.getMessage(ImportProjectPanel.class, "ImportProjectPanel.setMainProjectCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(setMainProjectCheckBox, gridBagConstraints);

        buildProjectCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(buildProjectCheckBox, org.openide.util.NbBundle.getMessage(ImportProjectPanel.class, "ImportProjectPanel.buildProjectCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(buildProjectCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String seed = null;
        if (projectFolder.getText().length() > 0) {
            seed = projectFolder.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox buildProjectCheckBox;
    private javax.swing.JTextField configureFlags;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JCheckBox setMainProjectCheckBox;
    private javax.swing.JTextPane todoPane;
    // End of variables declaration//GEN-END:variables
}


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project.ui.wizards;

import java.io.File;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.avatar_js.project.AvatarJSProject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author martin
 */
class PanelServerFileComponent extends javax.swing.JPanel {
    
    private final PanelServerFile panel;
    private FileObject projectRoot;

    /**
     * Creates new form PanelServerFileComponent
     */
    public PanelServerFileComponent(PanelServerFile panel) {
        this.panel = panel;
        initComponents();
        FieldDocumentListener dl = new FieldDocumentListener();
        fileNameTextField.getDocument().addDocumentListener(dl);
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        String errorMsg = "";   // NOI18N
        String file = createdFileTextField.getText();
        String portStr = portTextField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException nfex) {
            port = 0;
            errorMsg = nfex.getLocalizedMessage();
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMsg);
        return errorMsg.isEmpty();
    }
    
    void readSettings(WizardDescriptor wizardDescriptor) {
        Project project = Templates.getProject(wizardDescriptor);
        projectRoot = project.getProjectDirectory();
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        String name = Templates.getTargetName(wizardDescriptor);
        fileNameTextField.setText(name);
        folderTextField.setText("");
        String portStr = (String) wizardDescriptor.getProperty(WizardSettings.PROP_SERVER_FILE_PORT);
        if (portStr == null) {
            portStr = "";   // NOI18N
        }
        portTextField.setText(portStr);
    }
    
    void storeSettings(WizardDescriptor wizardDescriptor) {
        Templates.setTargetFolder(wizardDescriptor, getTargetFolderFO());
        Templates.setTargetName(wizardDescriptor, fileNameTextField.getText().trim());
        wizardDescriptor.putProperty(WizardSettings.PROP_SERVER_FILE_PORT, portTextField.getText().trim());
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // Nothing to validate
    }
    
    private FileObject getTargetFolderFO() {
        FileObject fo = projectRoot;
        String folderName = folderTextField.getText().trim();
        FileObject folder;
        if (folderName.isEmpty()) {
            folder = fo;
        } else {
            folderName = folderName.replace(File.separatorChar, '/');
            folder = fo.getFileObject(folderName);
            if (folder == null) {
                try {
                    folder = FileUtil.createFolder(fo, folderName);
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
        return folder;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        folderBrowseButton = new javax.swing.JButton();
        createdFileLabel = new javax.swing.JLabel();
        createdFileTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(PanelServerFileComponent.class, "PanelServerFileComponent.fileNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(PanelServerFileComponent.class, "PanelServerFileComponent.projectLabel.text")); // NOI18N

        projectTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(PanelServerFileComponent.class, "PanelServerFileComponent.folderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(folderBrowseButton, org.openide.util.NbBundle.getMessage(PanelServerFileComponent.class, "PanelServerFileComponent.folderBrowseButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(createdFileLabel, org.openide.util.NbBundle.getMessage(PanelServerFileComponent.class, "PanelServerFileComponent.createdFileLabel.text")); // NOI18N

        createdFileTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(PanelServerFileComponent.class, "PanelServerFileComponent.portLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileNameLabel)
                    .addComponent(projectLabel)
                    .addComponent(folderLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(folderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(folderBrowseButton))
                    .addComponent(fileNameTextField)
                    .addComponent(projectTextField)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(createdFileLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createdFileTextField))
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addComponent(portLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portTextField))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileNameLabel)
                    .addComponent(fileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(folderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(folderBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFileLabel)
                    .addComponent(createdFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(portLabel)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFileLabel;
    private javax.swing.JTextField createdFileTextField;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JButton folderBrowseButton;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    // End of variables declaration//GEN-END:variables

    private class FieldDocumentListener implements DocumentListener {
        
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
        
        private void update(DocumentEvent e) {
            updateTexts(e);
            Document doc = e.getDocument();
            if (doc == fileNameTextField.getDocument()) {
                firePropertyChange("fileName", null, fileNameTextField.getText());
            }
            if (doc == folderTextField.getDocument()) {
                firePropertyChange("folder", null, folderTextField.getText());
            }
        }
        
        private void updateTexts(DocumentEvent e) {
            Document doc = e.getDocument();
            if ( doc == fileNameTextField.getDocument() || doc == folderTextField.getDocument() ) {
                // Change in the project name
                String fileName = fileNameTextField.getText().trim();
                String folder = folderTextField.getText().trim();
                String prjFile = FileUtil.getFileDisplayName(projectRoot);
                String newFileName = prjFile;
                if (!folder.isEmpty()) {
                    newFileName += File.separator + folder;
                }
                if (!fileName.isEmpty()) {
                    boolean hasExt = fileName.indexOf('.') > 0;
                    if (!hasExt) {
                        fileName += AvatarJSProject.JS_FILE_EXT;
                    }
                    newFileName += File.separator + fileName;
                }
                createdFileTextField.setText(newFileName);
            }                
            panel.fireChangeEvent(); // Notify that the panel changed
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
public class ConfigureFXMLControllerPanelVisual extends JPanel implements DocumentListener {
    private static final String JAVA_FILE_EXTENSION = ".java"; // NOI18N
    
    private Panel observer;
    private File rootFolder;
    private String fxmlName;

    private ConfigureFXMLControllerPanelVisual(Panel observer) {
        this.observer = observer;
        initComponents(); // Matisse
        initComponents2(); // My own
    }

    private void fireChange() {
        this.observer.fireChangeEvent();
    }

    private void initComponents2() {
        createdNameTextField.getDocument().addDocumentListener(this);
        existingNameTextField.getDocument().addDocumentListener(this);
    }

    public void initValues(FileObject template, String fxmlName, File rootFolder) {
        if (template == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Template_Error")); // NOI18N
        }

        String displayName;
        try {
            DataObject templateDo = DataObject.find(template);
            displayName = templateDo.getNodeDelegate().getDisplayName();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }
        putClientProperty("NewFileWizard_Title", displayName); // NOI18N

        this.fxmlName = fxmlName;
        this.rootFolder = rootFolder;
        updateText();
        updateResult();
    }
    
    boolean isControllerEnabled() {
        return controllerCheckBox.isSelected();
    }

    String getNewControllerName() {
        String text = createdNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    String getExistingControllerName() {
        String text = existingNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    private void radioButtonsStateChanged() {
        if (!controllerCheckBox.isSelected()) {
            return;
        }
        createdNameLabel.setEnabled(createNewRadioButton.isSelected());
        createdNameTextField.setEnabled(createNewRadioButton.isSelected());
        existingNameLabel.setEnabled(!createNewRadioButton.isSelected());
        existingNameTextField.setEnabled(!createNewRadioButton.isSelected());
        chooseButton.setEnabled(!createNewRadioButton.isSelected());
        updateResult();
        fireChange();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        controllerCheckBox = new javax.swing.JCheckBox();
        createdNameLabel = new javax.swing.JLabel();
        createdNameTextField = new javax.swing.JTextField();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        createNewRadioButton = new javax.swing.JRadioButton();
        useExistingRadioButton = new javax.swing.JRadioButton();
        existingNameLabel = new javax.swing.JLabel();
        existingNameTextField = new javax.swing.JTextField();
        chooseButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(500, 340));

        org.openide.awt.Mnemonics.setLocalizedText(controllerCheckBox, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.controllerCheckBox.text")); // NOI18N
        controllerCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                controllerCheckBoxItemStateChanged(evt);
            }
        });

        createdNameLabel.setLabelFor(createdNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createdNameLabel.text")); // NOI18N
        createdNameLabel.setEnabled(false);

        createdNameTextField.setEnabled(false);

        fileLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.resultLabel.text")); // NOI18N
        fileLabel.setEnabled(false);

        fileTextField.setEnabled(false);

        buttonGroup1.add(createNewRadioButton);
        createNewRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createNewRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createNewRadioButton.text")); // NOI18N
        createNewRadioButton.setEnabled(false);
        createNewRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createNewRadioButtonItemStateChanged(evt);
            }
        });

        buttonGroup1.add(useExistingRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(useExistingRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.useExistingRadioButton.text")); // NOI18N
        useExistingRadioButton.setEnabled(false);
        useExistingRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                useExistingRadioButtonItemStateChanged(evt);
            }
        });

        existingNameLabel.setLabelFor(existingNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(existingNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.existingNameLabel.text")); // NOI18N
        existingNameLabel.setEnabled(false);

        existingNameTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(chooseButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.chooseButton.text")); // NOI18N
        chooseButton.setEnabled(false);
        chooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controllerCheckBox)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(existingNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(existingNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                            .addComponent(useExistingRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chooseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(createdNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createdNameTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(createNewRadioButton)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileLabel)
                        .addGap(50, 50, 50)
                        .addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controllerCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createNewRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdNameLabel)
                    .addComponent(createdNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useExistingRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(existingNameLabel)
                    .addComponent(existingNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(179, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void controllerCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_controllerCheckBoxItemStateChanged
        createNewRadioButton.setEnabled(controllerCheckBox.isSelected());
        if (createNewRadioButton.isSelected()) {
            createdNameLabel.setEnabled(controllerCheckBox.isSelected());
            createdNameTextField.setEnabled(controllerCheckBox.isSelected());
        }
        useExistingRadioButton.setEnabled(controllerCheckBox.isSelected());
        if (useExistingRadioButton.isSelected()) {
            existingNameLabel.setEnabled(controllerCheckBox.isSelected());
            existingNameTextField.setEnabled(controllerCheckBox.isSelected());
            chooseButton.setEnabled(controllerCheckBox.isSelected());
        }
        fileLabel.setEnabled(controllerCheckBox.isSelected());
        updateResult();
        fireChange();
    }//GEN-LAST:event_controllerCheckBoxItemStateChanged

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "LBL_ConfigureFXMLPanel_FileChooser_Select_Controller")); // NOI18N
        chooser.setFileFilter(new JavaFileFilter());
        String existingPath = existingNameTextField.getText();
        if (existingPath.length() > 0) {
            File f = new File(existingPath);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        } else {
            chooser.setCurrentDirectory(rootFolder);
        }
        
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            String controllerClass = FileUtil.normalizeFile(chooser.getSelectedFile()).getPath();
//            final String srcPath = FileUtil.normalizeFile(rootFolder).getPath();
//            final String path = FileUtil.normalizeFile(controllerClass).getPath();
//            final String relativePath = path.substring(srcPath.length() + 1);
//            final String relativePathWithoutExt = relativePath.substring(0, relativePath.indexOf(JAVA_FILE_EXTENSION));
//            existingNameTextField.setText(relativePathWithoutExt.replace(File.separatorChar, '.')); // NOI18N
            existingNameTextField.setText(controllerClass.replace(JAVA_FILE_EXTENSION, "")); // NOI18N
        }
    }//GEN-LAST:event_chooseButtonActionPerformed

    private void createNewRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createNewRadioButtonItemStateChanged
        radioButtonsStateChanged();
    }//GEN-LAST:event_createNewRadioButtonItemStateChanged

    private void useExistingRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_useExistingRadioButtonItemStateChanged
        radioButtonsStateChanged();
    }//GEN-LAST:event_useExistingRadioButtonItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton chooseButton;
    private javax.swing.JCheckBox controllerCheckBox;
    private javax.swing.JRadioButton createNewRadioButton;
    private javax.swing.JLabel createdNameLabel;
    private javax.swing.JTextField createdNameTextField;
    private javax.swing.JLabel existingNameLabel;
    private javax.swing.JTextField existingNameTextField;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JRadioButton useExistingRadioButton;
    // End of variables declaration//GEN-END:variables

    // DocumentListener implementation -----------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateResult();
        fireChange();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    // Private methods ---------------------------------------------------------
    private void updateText() {
        String controllerName = getNewControllerName();
        if (controllerName == null) {
            controllerName = fxmlName;
            String firstChar = String.valueOf(controllerName.charAt(0)).toUpperCase();
            String otherChars = controllerName.substring(1);
            controllerName = firstChar + otherChars + "Controller"; // NOI18N
            createdNameTextField.setText(controllerName);
        }
    }
    
    private void updateResult() {
        String controllerName = shouldCreateController() ? getNewControllerName() : getExistingControllerName();
        if (controllerName == null) {
            fileTextField.setText(null);
            return;
        }

        if (shouldCreateController()) {
            String file = FileUtil.getFileDisplayName(FileUtil.toFileObject(rootFolder)) + File.separator + controllerName + JAVA_FILE_EXTENSION;
            fileTextField.setText(file);
        } else {
            String file = FileUtil.getFileDisplayName(FileUtil.toFileObject(new File(controllerName + JAVA_FILE_EXTENSION)));
            fileTextField.setText(file);
        }
    }

    String isControllerValid() {
        if (createNewRadioButton.isSelected()) {
            if (!Utilities.isJavaIdentifier(getNewControllerName())) {
                return "WARN_ConfigureFXMLPanel_Provide_Java_Name"; // NOI18N
            }
            return FXMLTemplateWizardIterator.canUseFileName(rootFolder, getNewControllerName());
        }
        
        if (existingNameTextField.getText().isEmpty()) {
            return "WARN_ConfigureFXMLPanel_Provide_Java_Name"; // NOI18N
        }
        return FXMLTemplateWizardIterator.fileExist(existingNameTextField.getText() + JAVA_FILE_EXTENSION);
    }

    boolean shouldCreateController() {
        return controllerCheckBox.isSelected() && createNewRadioButton.isSelected();
    }

    // Private innerclasses ----------------------------------------------------
    private static class JavaFileFilter extends FileFilter {
        
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            return ("." + FileUtil.getExtension(f.getName())).equals(JAVA_FILE_EXTENSION); // NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "LBL_ConfigureFXMLPanel_FileChooser_Description"); // NOI18N
        }
    }
    
    static class Panel implements WizardDescriptor.Panel<WizardDescriptor> {
        
        private ConfigureFXMLControllerPanelVisual component;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private WizardDescriptor settings;

        public Panel() {
            component = new ConfigureFXMLControllerPanelVisual(this);
        }

        @Override
        public Component getComponent() {
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx(ConfigureFXMLPanelVisual.class);
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            this.settings = settings;
            String fxmlName = Templates.getTargetName(settings);
            File rootFolder = (File) settings.getProperty(FXMLTemplateWizardIterator.ROOT_FOLDER);
            component.initValues(Templates.getTemplate(settings), fxmlName, rootFolder);

            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewFileWizard to modify the title
            Object substitute = component.getClientProperty("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                settings.putProperty("NewFileWizard_Title", substitute); // NOI18N
            }
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            Object value = settings.getValue();
            if (WizardDescriptor.PREVIOUS_OPTION.equals(value)
                    || WizardDescriptor.CANCEL_OPTION.equals(value)
                    || WizardDescriptor.CLOSED_OPTION.equals(value)) {
                return;
            }
            if (isValid()) {
                settings.putProperty(FXMLTemplateWizardIterator.JAVA_CONTROLLER_CREATE, component.shouldCreateController());
                settings.putProperty(FXMLTemplateWizardIterator.JAVA_CONTROLLER_NAME_PROPERTY, 
                    component.shouldCreateController() ? component.getNewControllerName() : component.getExistingControllerName());
            }
            settings.putProperty("NewFileWizard_Title", null); // NOI18N
        }

        @Override
        public boolean isValid() {
            if (component.isControllerEnabled()) {
                String msg = component.isControllerValid();
                FXMLTemplateWizardIterator.setErrorMessage(msg, settings);
                return msg == null;
            }
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        private void fireChangeEvent() {
            changeSupport.fireChange();
        }
    }
}

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
// Used JavaTargetChooserPanelGUI
public class ConfigureFXMLPanelVisual extends JPanel implements ActionListener, DocumentListener {
    private static final String FXML_FILE_EXTENSION = ".fxml"; // NOI18N
    private static final String JAVA_FILE_EXTENSION = ".java"; // NOI18N
    
    private final List<ChangeListener> listeners;
    private Project project;
    private SourceGroup groups[];

    private boolean ignoreRootCombo;
    private RequestProcessor.Task updatePackagesTask;
    private static final ComboBoxModel WAIT_MODEL;
    
    static {
        WAIT_MODEL = new DefaultComboBoxModel(
                new String[]{
                    NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                        "LBL_ConfigureFXMLPanel_PackageName_PleaseWait") // NOI18N
                });
    }

    public ConfigureFXMLPanelVisual(Project project, SourceGroup[] groups) {
        this.project = project;
        if (project == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Project_Error")); // NOI18N
        }

        this.groups = groups;
        for (SourceGroup sourceGroup : groups) {
            if (sourceGroup == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_SG_Error")); // NOI18N
            }
        }
        
        listeners = new ArrayList<ChangeListener>();

        initComponents(); // Matisse
        initComponents2(); // My own
    }

    private File getSrcFolder() {
        return FileUtil.toFile(groups[0].getRootFolder());
    }

    private void initComponents2() {
        fxmlNameTextField.getDocument().addDocumentListener(this);
        createdControllerNameTextField.getDocument().addDocumentListener(this);
        existingControllerNameTextField.getDocument().addDocumentListener(this);
        cssNameTextField.getDocument().addDocumentListener(this);
        
        packageComboBox.getEditor().addActionListener(this);
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if (packageEditor instanceof JTextField) {
            ((JTextField) packageEditor).getDocument().addDocumentListener(this);
        }

        locationComboBox.setRenderer(new GroupListCellRenderer());
        packageComboBox.setRenderer(PackageView.listRenderer());
        locationComboBox.addActionListener(this);

        setName(name());
    }

    public void initValues(FileObject template, FileObject preselectedFolder) {
        if (template == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Template_Error")); // NOI18N
        }

        // Show name of the project
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        String displayName;
        try {
            DataObject templateDo = DataObject.find(template);
            displayName = templateDo.getNodeDelegate().getDisplayName();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }
        putClientProperty("NewFileWizard_Title", displayName); // NOI18N        

        // Setup comboboxes 
        locationComboBox.setModel(new DefaultComboBoxModel(groups));
        SourceGroup preselectedGroup = getPreselectedGroup(preselectedFolder);
        ignoreRootCombo = true;
        locationComboBox.setSelectedItem(preselectedGroup);
        ignoreRootCombo = false;
        Object preselectedPackage = getPreselectedPackage(preselectedGroup, preselectedFolder, packageComboBox.getModel());
        if (preselectedPackage != null) {
            packageComboBox.getEditor().setItem(preselectedPackage);
        }
        if (template != null) {
            if (fxmlNameTextField.getText().trim().length() == 0) { // To preserve the fxml name on back in the wiazard
                final String baseName = template.getName();
                String activeName = baseName;
                if (preselectedFolder != null) {
                    int index = 0;
                    while (true) {
                        FileObject fo = preselectedFolder.getFileObject(activeName, template.getExt()); // NOI18N
                        if (fo == null) {
                            break;
                        }
                        activeName = baseName + ++index;
                    }
                }
                fxmlNameTextField.setText(activeName);
                fxmlNameTextField.selectAll();
            }
        }
        
        updatePackages();
        updateText();
    }
    
    public String name() {
        return NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "LBL_ConfigureFXMLPanel_Name"); // NOI18N
    }

    public FileObject getLocationFolder() {
        final Object selectedItem  = locationComboBox.getSelectedItem();
        return (selectedItem instanceof SourceGroup) ? ((SourceGroup)selectedItem).getRootFolder() : null;
    }

    public String getPackageFileName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName.replace('.', '/'); // NOI18N
    }

    /**
     * Name of selected package, or "" for default package.
     */
    String getPackageName() {
        return packageComboBox.getEditor().getItem().toString();
    }

    public String getFXMLName() {
        String text = fxmlNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    boolean isControllerEnabled() {
        return controllerCheckBox.isSelected();
    }

    String getNewControllerName() {
        String text = createdControllerNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    String getExistingControllerName() {
        String text = existingControllerNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    boolean isCSSEnabled() {
        return cssCheckBox.isSelected();
    }

    String getCSSName() {
        String text = cssNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }
    
    private void radioButtonsStateChanged() {
        createdControllerNameLabel.setEnabled(createNewRadioButton.isSelected());
        createdControllerNameTextField.setEnabled(createNewRadioButton.isSelected());
        existingControllerNameLabel.setEnabled(!createNewRadioButton.isSelected());
        existingControllerNameTextField.setEnabled(!createNewRadioButton.isSelected());
        chooseControllerButton.setEnabled(!createNewRadioButton.isSelected());
        updateText();
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
        fxmlNameLabel = new javax.swing.JLabel();
        fxmlNameTextField = new javax.swing.JTextField();
        controllerCheckBox = new javax.swing.JCheckBox();
        createdControllerNameLabel = new javax.swing.JLabel();
        createdControllerNameTextField = new javax.swing.JTextField();
        cssCheckBox = new javax.swing.JCheckBox();
        cssNameLabel = new javax.swing.JLabel();
        cssNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        locationLabel = new javax.swing.JLabel();
        packageLabel = new javax.swing.JLabel();
        resultLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationComboBox = new javax.swing.JComboBox();
        resultTextField = new javax.swing.JTextField();
        packageComboBox = new javax.swing.JComboBox();
        createNewRadioButton = new javax.swing.JRadioButton();
        useExistingRadioButton = new javax.swing.JRadioButton();
        existingControllerNameLabel = new javax.swing.JLabel();
        existingControllerNameTextField = new javax.swing.JTextField();
        chooseControllerButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(500, 340));

        fxmlNameLabel.setLabelFor(fxmlNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fxmlNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.fxmlNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerCheckBox, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.controllerCheckBox.text")); // NOI18N
        controllerCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                controllerCheckBoxItemStateChanged(evt);
            }
        });

        createdControllerNameLabel.setLabelFor(createdControllerNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdControllerNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.createdControllerNameLabel.text")); // NOI18N
        createdControllerNameLabel.setEnabled(false);

        createdControllerNameTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(cssCheckBox, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.cssCheckBox.text")); // NOI18N
        cssCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cssCheckBoxItemStateChanged(evt);
            }
        });

        cssNameLabel.setLabelFor(cssNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(cssNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.cssNameLabel.text")); // NOI18N
        cssNameLabel.setEnabled(false);

        cssNameTextField.setEnabled(false);

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.projectLabel.text")); // NOI18N

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.locationLabel.text")); // NOI18N

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.packageLabel.text")); // NOI18N

        resultLabel.setLabelFor(resultTextField);
        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.resultLabel.text")); // NOI18N

        projectTextField.setEditable(false);
        projectTextField.setEnabled(false);

        resultTextField.setEditable(false);
        resultTextField.setEnabled(false);

        packageComboBox.setEditable(true);

        buttonGroup1.add(createNewRadioButton);
        createNewRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createNewRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.createNewRadioButton.text")); // NOI18N
        createNewRadioButton.setEnabled(false);
        createNewRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createNewRadioButtonItemStateChanged(evt);
            }
        });

        buttonGroup1.add(useExistingRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(useExistingRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.useExistingRadioButton.text")); // NOI18N
        useExistingRadioButton.setEnabled(false);
        useExistingRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                useExistingRadioButtonItemStateChanged(evt);
            }
        });

        existingControllerNameLabel.setLabelFor(existingControllerNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(existingControllerNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.existingControllerNameLabel.text")); // NOI18N
        existingControllerNameLabel.setEnabled(false);

        existingControllerNameTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(chooseControllerButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.chooseControllerButton.text")); // NOI18N
        chooseControllerButton.setEnabled(false);
        chooseControllerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseControllerButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(fxmlNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fxmlNameTextField))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(cssNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(cssCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cssNameTextField))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(projectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(packageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resultLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectTextField)
                    .addComponent(locationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resultTextField)
                    .addComponent(packageComboBox, 0, 424, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(controllerCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(useExistingRadioButton)
                                    .addComponent(createNewRadioButton))
                                .addContainerGap(390, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(createdControllerNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                    .addComponent(existingControllerNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(14, 14, 14)
                                .addComponent(existingControllerNameTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseControllerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))))
            .addGroup(layout.createSequentialGroup()
                .addGap(180, 180, 180)
                .addComponent(createdControllerNameTextField))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fxmlNameLabel)
                    .addComponent(fxmlNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controllerCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createNewRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdControllerNameLabel)
                    .addComponent(createdControllerNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useExistingRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(existingControllerNameLabel)
                    .addComponent(existingControllerNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseControllerButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cssCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cssNameLabel)
                    .addComponent(cssNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageLabel)
                    .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resultLabel)
                    .addComponent(resultTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(44, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void controllerCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_controllerCheckBoxItemStateChanged
        createNewRadioButton.setEnabled(controllerCheckBox.isSelected());
        useExistingRadioButton.setEnabled(controllerCheckBox.isSelected());
        radioButtonsStateChanged();
    }//GEN-LAST:event_controllerCheckBoxItemStateChanged

    private void cssCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cssCheckBoxItemStateChanged
        cssNameLabel.setEnabled(cssCheckBox.isSelected());
        cssNameTextField.setEnabled(cssCheckBox.isSelected());
        updateText();
        fireChange();
    }//GEN-LAST:event_cssCheckBoxItemStateChanged

    private void chooseControllerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseControllerButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "LBL_ConfigureFXMLPanel_FileChooser_Select_Controller")); // NOI18N
        chooser.setFileFilter(new JavaFileFilter());
        String existingPath = existingControllerNameTextField.getText();
        if (existingPath.length() > 0) {
            File f = new File(existingPath);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        } else {
            chooser.setCurrentDirectory(getSrcFolder());
        }
        
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File controllerClass = chooser.getSelectedFile();
            final String srcPath = FileUtil.normalizeFile(getSrcFolder()).getPath();
            final String path = FileUtil.normalizeFile(controllerClass).getPath();
            final String relativePath = path.substring(srcPath.length() + 1);
            final String relativePathWithoutExt = relativePath.substring(0, relativePath.indexOf(JAVA_FILE_EXTENSION));
            existingControllerNameTextField.setText(relativePathWithoutExt.replace(File.separatorChar, '.')); // NOI18N
        }
    }//GEN-LAST:event_chooseControllerButtonActionPerformed

    private void createNewRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createNewRadioButtonItemStateChanged
        radioButtonsStateChanged();
    }//GEN-LAST:event_createNewRadioButtonItemStateChanged

    private void useExistingRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_useExistingRadioButtonItemStateChanged
        radioButtonsStateChanged();
    }//GEN-LAST:event_useExistingRadioButtonItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton chooseControllerButton;
    private javax.swing.JCheckBox controllerCheckBox;
    private javax.swing.JRadioButton createNewRadioButton;
    private javax.swing.JLabel createdControllerNameLabel;
    private javax.swing.JTextField createdControllerNameTextField;
    private javax.swing.JCheckBox cssCheckBox;
    private javax.swing.JLabel cssNameLabel;
    private javax.swing.JTextField cssNameTextField;
    private javax.swing.JLabel existingControllerNameLabel;
    private javax.swing.JTextField existingControllerNameTextField;
    private javax.swing.JLabel fxmlNameLabel;
    private javax.swing.JTextField fxmlNameTextField;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JTextField resultTextField;
    private javax.swing.JRadioButton useExistingRadioButton;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (locationComboBox == e.getSource()) {
            if (!ignoreRootCombo) {
                updatePackages();
            }
            updateText();
            fireChange();
        } else if (packageComboBox == e.getSource()) {
            updateText();
            fireChange();
        } else if (packageComboBox.getEditor() == e.getSource()) {
            updateText();
            fireChange();
        }
    }

    // DocumentListener implementation -----------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        final Document doc = e.getDocument();
        if (doc != createdControllerNameTextField.getDocument() &&
                doc != cssNameTextField.getDocument() &&
                doc != existingControllerNameTextField.getDocument()) {
            updateText();
        }
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
    private void updatePackages() {
        final Object item = locationComboBox.getSelectedItem();
        if (!(item instanceof SourceGroup)) {
            return;
        }
        WAIT_MODEL.setSelectedItem(packageComboBox.getEditor().getItem());
        packageComboBox.setModel(WAIT_MODEL);

        if (updatePackagesTask != null) {
            updatePackagesTask.cancel();
        }

        updatePackagesTask = new RequestProcessor("ComboUpdatePackages").post(new Runnable() { // NOI18N
            @Override
            public void run() {
                final ComboBoxModel model = PackageView.createListView((SourceGroup) item);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        model.setSelectedItem(packageComboBox.getEditor().getItem());
                        packageComboBox.setModel(model);
                    }
                });
            }
        });
    }

    private void updateText() {
        final Object selectedItem = locationComboBox.getSelectedItem();
        String createdFileName;
        if (selectedItem instanceof SourceGroup) {
            SourceGroup g = (SourceGroup) selectedItem;
            FileObject rootFolder = g.getRootFolder();
            String packageName = getPackageFileName();
            String fxmlName = getFXMLName();
            if (fxmlName != null && fxmlName.length() > 0) {
                fxmlName = fxmlName + FXML_FILE_EXTENSION;
            }
            createdFileName = FileUtil.getFileDisplayName(rootFolder)
                    + (packageName.startsWith("/") || packageName.startsWith(File.separator) ? "" : "/") + // NOI18N
                    packageName
                    + (packageName.endsWith("/") || packageName.endsWith(File.separator) || packageName.length() == 0 ? "" : "/") + // NOI18N
                    fxmlName;
        } else {
            //May be null if nothing selected
            createdFileName = "";   //NOI18N
        }
        resultTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N
        
        if (controllerCheckBox.isSelected()) {
            String controllerName = getNewControllerName();
            if (controllerName == null) {
                controllerName = getFXMLName();
                String firstChar = String.valueOf(controllerName.charAt(0)).toUpperCase();
                String otherChars = controllerName.substring(1);
                controllerName = firstChar + otherChars + "Controller"; // NOI18N
            }
            createdControllerNameTextField.setText(controllerName);
        }
        
        if (cssCheckBox.isSelected()) {
            String cssName = getCSSName();
            if (cssName == null) {
                cssName = getFXMLName().toLowerCase();
            }
            cssNameTextField.setText(cssName);
        }
    }

    private SourceGroup getPreselectedGroup(FileObject folder) {
        for(int i = 0; folder != null && i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (root.equals(folder) || FileUtil.isParentOf(root, folder)) {
                return groups[i];
            }
        }
        return groups[0];
    }
    
    /**
     * Get a package combo model item for the package the user selected before
     * opening the wizard. May return null if it cannot find it; or a String
     * instance if there is a well-defined package but it is not listed among
     * the packages shown in the list model.
     */
    private Object getPreselectedPackage(SourceGroup group, FileObject folder, ListModel model) {
        if (folder == null) {
            return null;
        }

        FileObject root = group.getRootFolder();
        String relPath = FileUtil.getRelativePath(root, folder);
        if (relPath == null) {
            // Group Root folder is not a parent of the preselected folder
            // No package should be selected
            return null;
        } else {
            // Find the right item.            
            String name = relPath.replace('/', '.'); // NOI18N
            return name;
        }
    }

    boolean isControllerValid() {
        if (createNewRadioButton.isSelected()) {
            return Utilities.isJavaIdentifier(getNewControllerName());
        }
        
        if (existingControllerNameTextField.getText().isEmpty()) {
            return false;
        }
        
        final String path = existingControllerNameTextField.getText().replace('.', File.separatorChar); // NOI18N
        final File file = new File(getSrcFolder().getPath() + File.separatorChar + path + JAVA_FILE_EXTENSION);
        return file.exists();
    }

    boolean shouldCreateController() {
        return controllerCheckBox.isSelected() && createNewRadioButton.isSelected();
    }

    // Private innerclasses ----------------------------------------------------

    /**
     * Displays a {@link SourceGroup} in {@link #rootComboBox}.
     */
    private static final class GroupListCellRenderer extends DefaultListCellRenderer {

        public GroupListCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            Icon icon;
            if (value == null) {
                name = ""; //NOI18N
                icon = null;
            } else {
                assert value instanceof SourceGroup;
                SourceGroup g = (SourceGroup) value;
                name = g.getDisplayName();
                icon = g.getIcon(false);
            }
            super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            setIcon(icon);
            
            return this;
        }
    }
    
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
            return NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "LBL_ConfigureFXMLPanel_FileChooser_Description"); // NOI18N
        }
    
    }
}

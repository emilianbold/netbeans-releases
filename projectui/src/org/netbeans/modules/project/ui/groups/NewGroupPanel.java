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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.project.ui.groups;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Panel permitting user to create a new project group.
 * Applicable in advanced mode.
 * @author Jesse Glick
 */
public class NewGroupPanel extends JPanel {

    public static final String PROP_READY = "ready"; // NOI18N

    public NewGroupPanel() {
        initComponents();
        DocumentListener l = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                firePropertyChange(PROP_READY, null, null);
            }
            public void removeUpdate(DocumentEvent e) {
                firePropertyChange(PROP_READY, null, null);
            }
            public void changedUpdate(DocumentEvent e) {}
        };
        directoryField.getDocument().addDocumentListener(l);
        nameField.getDocument().addDocumentListener(l);
        updateNameField();
    }

    public boolean isReady() {
        if (adHocKindRadio.isSelected()) {
            return nameField.getText() != null && nameField.getText().trim().length() > 0;
        } else if (subprojectsKindRadio.isSelected()) {
            String s = masterProjectField.getText();
            if (s != null && s.length() > 0) {
                File f = new File(s);
                FileObject fo = FileUtil.toFileObject(f);
                if (fo != null && fo.isFolder()) {
                    try {
                        return ProjectManager.getDefault().findProject(fo) != null;
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
            return false;
        } else {
            assert directoryKindRadio.isSelected();
            if (nameField.getText() == null || nameField.getText().trim().length() == 0) {
                return false;
            }
            String s = directoryField.getText();
            if (s != null) {
                return new File(s.trim()).isDirectory();
            } else {
                return false;
            }
        }
    }

    private void updateNameField() {
        if (adHocKindRadio.isSelected() && useOpenCheckbox.isSelected()) {
            Project p = OpenProjects.getDefault().getMainProject();
            if (p != null && nameField.getText().length() == 0) {
                nameField.setText(ProjectUtils.getInformation(p).getDisplayName());
            }
        } else if (subprojectsKindRadio.isSelected()) {
            String s = masterProjectField.getText();
            if (s != null && s.length() > 0) {
                File f = new File(s);
                FileObject fo = FileUtil.toFileObject(f);
                if (fo != null && fo.isFolder()) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(fo);
                        if (p != null && nameField.getText().length() == 0) {
                            nameField.setText(ProjectUtils.getInformation(p).getDisplayName());
                        }
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
        } else if (directoryKindRadio.isSelected()) {
            String s = directoryField.getText();
            if (s != null && s.length() > 0 && nameField.getText().length() == 0) {
                File f = new File(s);
                nameField.setText(f.getName());
            }
        }
    }

    public Group create() {
        assert isReady();
        if (adHocKindRadio.isSelected()) {
            AdHocGroup g = AdHocGroup.create(nameField.getText().trim(), autoSynchCheckbox.isSelected());
            if (useOpenCheckbox.isSelected()) {
                g.setProjects(new HashSet<Project>(Arrays.asList(OpenProjects.getDefault().getOpenProjects())));
                g.setMainProject(OpenProjects.getDefault().getMainProject());
            }
            return g;
        } else if (subprojectsKindRadio.isSelected()) {
            FileObject fo = FileUtil.toFileObject(new File(masterProjectField.getText()));
            try {
                return SubprojectsGroup.create(nameField.getText().trim(), ProjectManager.getDefault().findProject(fo));
            } catch (IOException x) {
                throw new AssertionError(x);
            }
        } else {
            assert directoryKindRadio.isSelected();
            FileObject f = FileUtil.toFileObject(FileUtil.normalizeFile(new File(directoryField.getText().trim())));
            try {
                return DirectoryGroup.create(nameField.getText().trim(), f);
            } catch (FileStateInvalidException x) {
                throw new AssertionError(x);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        kindButtonGroup = new javax.swing.ButtonGroup();
        adHocKindRadio = new javax.swing.JRadioButton();
        adHocKindLabel = new javax.swing.JLabel();
        useOpenCheckbox = new javax.swing.JCheckBox();
        autoSynchCheckbox = new javax.swing.JCheckBox();
        subprojectsKindRadio = new javax.swing.JRadioButton();
        subprojectsKindLabel = new javax.swing.JLabel();
        masterProjectLabel = new javax.swing.JLabel();
        masterProjectField = new javax.swing.JTextField();
        masterProjectButton = new javax.swing.JButton();
        directoryKindRadio = new javax.swing.JRadioButton();
        directoryKindLabel = new javax.swing.JLabel();
        directoryLabel = new javax.swing.JLabel();
        directoryField = new javax.swing.JTextField();
        directoryButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();

        kindButtonGroup.add(adHocKindRadio);
        adHocKindRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(adHocKindRadio, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindRadio.text")); // NOI18N
        adHocKindRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        adHocKindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adHocKindRadioActionPerformed(evt);
            }
        });

        adHocKindLabel.setLabelFor(adHocKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(adHocKindLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindLabel.text")); // NOI18N

        useOpenCheckbox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(useOpenCheckbox, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.useOpenCheckbox.text")); // NOI18N

        autoSynchCheckbox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoSynchCheckbox, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.autoSynchCheckbox.text")); // NOI18N

        kindButtonGroup.add(subprojectsKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(subprojectsKindRadio, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindRadio.text")); // NOI18N
        subprojectsKindRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        subprojectsKindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subprojectsKindRadioActionPerformed(evt);
            }
        });

        subprojectsKindLabel.setLabelFor(subprojectsKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(subprojectsKindLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindLabel.text")); // NOI18N
        subprojectsKindLabel.setEnabled(false);

        masterProjectLabel.setLabelFor(masterProjectField);
        org.openide.awt.Mnemonics.setLocalizedText(masterProjectLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectLabel.text")); // NOI18N
        masterProjectLabel.setEnabled(false);

        masterProjectField.setEditable(false);
        masterProjectField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(masterProjectButton, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectButton.text")); // NOI18N
        masterProjectButton.setEnabled(false);
        masterProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterProjectButtonActionPerformed(evt);
            }
        });

        kindButtonGroup.add(directoryKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(directoryKindRadio, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindRadio.text")); // NOI18N
        directoryKindRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryKindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryKindRadioActionPerformed(evt);
            }
        });

        directoryKindLabel.setLabelFor(directoryKindRadio);
        org.openide.awt.Mnemonics.setLocalizedText(directoryKindLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindLabel.text")); // NOI18N
        directoryKindLabel.setEnabled(false);

        directoryLabel.setLabelFor(directoryField);
        org.openide.awt.Mnemonics.setLocalizedText(directoryLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryLabel.text")); // NOI18N
        directoryLabel.setEnabled(false);

        directoryField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(directoryButton, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryButton.text")); // NOI18N
        directoryButton.setEnabled(false);
        directoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryButtonActionPerformed(evt);
            }
        });

        nameLabel.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE))
                    .addComponent(directoryKindRadio)
                    .addComponent(adHocKindRadio)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(directoryLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(directoryField, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(directoryButton))
                            .addComponent(directoryKindLabel)))
                    .addComponent(subprojectsKindRadio)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(masterProjectLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(masterProjectField, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE))
                            .addComponent(subprojectsKindLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(masterProjectButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(adHocKindLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(autoSynchCheckbox)
                            .addComponent(useOpenCheckbox))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(adHocKindRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(adHocKindLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useOpenCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoSynchCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subprojectsKindRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subprojectsKindLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(masterProjectLabel)
                    .addComponent(masterProjectButton)
                    .addComponent(masterProjectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(directoryKindRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(directoryKindLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryLabel)
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(directoryButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        adHocKindRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindRadio.AccessibleContext.accessibleDescription")); // NOI18N
        adHocKindLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.adHocKindLabel.AccessibleContext.accessibleDescription")); // NOI18N
        useOpenCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.useOpenCheckbox.AccessibleContext.accessibleDescription")); // NOI18N
        autoSynchCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.autoSynchCheckbox.AccessibleContext.accessibleDescription")); // NOI18N
        subprojectsKindRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindRadio.AccessibleContext.accessibleDescription")); // NOI18N
        subprojectsKindLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.subprojectsKindLabel.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectLabel.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectField.AccessibleContext.accessibleName")); // NOI18N
        masterProjectField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectField.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.masterProjectButton.AccessibleContext.accessibleDescription")); // NOI18N
        directoryKindRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindRadio.AccessibleContext.accessibleDescription")); // NOI18N
        directoryKindLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryKindLabel.AccessibleContext.accessibleDescription")); // NOI18N
        directoryLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryLabel.AccessibleContext.accessibleDescription")); // NOI18N
        directoryField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryField.AccessibleContext.accessibleName")); // NOI18N
        directoryField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryField.AccessibleContext.accessibleDescription")); // NOI18N
        directoryButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.directoryButton.AccessibleContext.accessibleDescription")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        nameField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameField.AccessibleContext.accessibleName")); // NOI18N
        nameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.nameField.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void directoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        File start = ProjectChooser.getProjectsFolder();
        if (directoryField.getText() != null && directoryField.getText().trim().length() > 0) {
            start = new File(directoryField.getText().trim());
        }
        FileUtil.preventFileChooserSymlinkTraversal(chooser, start);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                directoryField.setText(f.getAbsolutePath());
                updateNameField();
            }
        }
    }//GEN-LAST:event_directoryButtonActionPerformed

    private void masterProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterProjectButtonActionPerformed
        JFileChooser chooser = ProjectChooser.projectChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                masterProjectField.setText(f.getAbsolutePath());
                updateNameField();
                firePropertyChange(PROP_READY, null, null);
            }
        }
    }//GEN-LAST:event_masterProjectButtonActionPerformed

    private void directoryKindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryKindRadioActionPerformed
        adHocKindLabel.setEnabled(false);
        useOpenCheckbox.setEnabled(false);
        autoSynchCheckbox.setEnabled(false);
        subprojectsKindLabel.setEnabled(false);
        masterProjectLabel.setEnabled(false);
        masterProjectField.setEnabled(false);
        masterProjectButton.setEnabled(false);
        directoryKindLabel.setEnabled(true);
        directoryLabel.setEnabled(true);
        directoryField.setEnabled(true);
        directoryButton.setEnabled(true);
        updateNameField();
        firePropertyChange(PROP_READY, null, null);
    }//GEN-LAST:event_directoryKindRadioActionPerformed

    private void subprojectsKindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subprojectsKindRadioActionPerformed
        adHocKindLabel.setEnabled(false);
        useOpenCheckbox.setEnabled(false);
        autoSynchCheckbox.setEnabled(false);
        subprojectsKindLabel.setEnabled(true);
        masterProjectLabel.setEnabled(true);
        masterProjectField.setEnabled(true);
        masterProjectButton.setEnabled(true);
        directoryKindLabel.setEnabled(false);
        directoryLabel.setEnabled(false);
        directoryField.setEnabled(false);
        directoryButton.setEnabled(false);
        updateNameField();
        firePropertyChange(PROP_READY, null, null);
    }//GEN-LAST:event_subprojectsKindRadioActionPerformed

    private void adHocKindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adHocKindRadioActionPerformed
        adHocKindLabel.setEnabled(true);
        useOpenCheckbox.setEnabled(true);
        autoSynchCheckbox.setEnabled(true);
        subprojectsKindLabel.setEnabled(false);
        masterProjectLabel.setEnabled(false);
        masterProjectField.setEnabled(false);
        masterProjectButton.setEnabled(false);
        directoryKindLabel.setEnabled(false);
        directoryLabel.setEnabled(false);
        directoryField.setEnabled(false);
        directoryButton.setEnabled(false);
        updateNameField();
        firePropertyChange(PROP_READY, null, null);
    }//GEN-LAST:event_adHocKindRadioActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adHocKindLabel;
    private javax.swing.JRadioButton adHocKindRadio;
    private javax.swing.JCheckBox autoSynchCheckbox;
    private javax.swing.JButton directoryButton;
    private javax.swing.JTextField directoryField;
    private javax.swing.JLabel directoryKindLabel;
    private javax.swing.JRadioButton directoryKindRadio;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.ButtonGroup kindButtonGroup;
    private javax.swing.JButton masterProjectButton;
    private javax.swing.JTextField masterProjectField;
    private javax.swing.JLabel masterProjectLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel subprojectsKindLabel;
    private javax.swing.JRadioButton subprojectsKindRadio;
    private javax.swing.JCheckBox useOpenCheckbox;
    // End of variables declaration//GEN-END:variables
    
}

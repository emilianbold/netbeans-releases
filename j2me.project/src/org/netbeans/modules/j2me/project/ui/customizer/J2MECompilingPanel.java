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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.Dialog;
import java.awt.Dimension;
import javax.lang.model.SourceVersion;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class J2MECompilingPanel extends JPanel implements HelpCtx.Provider {

    private static final String HELP_ID = "org.netbeans.modules.j2me.project.ui.customizer.J2MECompilingPanel"; //NOI18N

    public J2MECompilingPanel(J2MEProjectProperties uiProperties) {
        initComponents();

        int nextExtensionYPos = 0;
        addPanelFiller(nextExtensionYPos);

        uiProperties.JAVAC_DEPRECATION_MODEL.setMnemonic(deprecationCheckBox.getMnemonic());
        deprecationCheckBox.setModel(uiProperties.JAVAC_DEPRECATION_MODEL);

        uiProperties.JAVAC_DEBUG_MODEL.setMnemonic(debugInfoCheckBox.getMnemonic());
        debugInfoCheckBox.setModel(uiProperties.JAVAC_DEBUG_MODEL);

        uiProperties.DO_DEPEND_MODEL.setMnemonic(doDependCheckBox.getMnemonic());
        doDependCheckBox.setModel(uiProperties.DO_DEPEND_MODEL);

        uiProperties.ENABLE_ANNOTATION_PROCESSING_MODEL.setMnemonic(enableAPTCheckBox.getMnemonic());
        enableAPTCheckBox.setModel(uiProperties.ENABLE_ANNOTATION_PROCESSING_MODEL);

        uiProperties.ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL.setMnemonic(enableAPTEditorCheckBox.getMnemonic());
        enableAPTEditorCheckBox.setModel(uiProperties.ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL);

        annotationProcessorsList.setModel(uiProperties.ANNOTATION_PROCESSORS_MODEL);

        processorOptionsTable.setModel(uiProperties.PROCESSOR_OPTIONS_MODEL);

        additionalJavacParamsField.setDocument(uiProperties.JAVAC_COMPILER_ARG_MODEL);

        enableAPTCheckBoxActionPerformed(null);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        debugInfoCheckBox = new javax.swing.JCheckBox();
        deprecationCheckBox = new javax.swing.JCheckBox();
        doDependCheckBox = new javax.swing.JCheckBox();
        enableAPTCheckBox = new javax.swing.JCheckBox();
        enableAPTEditorCheckBox = new javax.swing.JCheckBox();
        annotationProcessorsLabel = new javax.swing.JLabel();
        AnnotationProcessorsScrollPane = new javax.swing.JScrollPane();
        annotationProcessorsList = new javax.swing.JList();
        addProcessorButton = new javax.swing.JButton();
        removeProcessorButton = new javax.swing.JButton();
        processorOptionsLabel = new javax.swing.JLabel();
        processorOptionsScrollPane = new javax.swing.JScrollPane();
        processorOptionsTable = new javax.swing.JTable();
        addOptionButton = new javax.swing.JButton();
        removeOptionButton = new javax.swing.JButton();
        additionalJavacParamsLabel = new javax.swing.JLabel();
        additionalJavacParamsField = new javax.swing.JTextField();
        additionalJavacParamsExample = new javax.swing.JLabel();
        extPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(debugInfoCheckBox, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Compiler_DebugInfo_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deprecationCheckBox, org.openide.util.NbBundle.getBundle(J2MECompilingPanel.class).getString("LBL_CustomizeCompile_Compiler_Deprecation_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doDependCheckBox, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "CustomizerCompile.doDependCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(enableAPTCheckBox, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Enable_Annotation_Processing")); // NOI18N
        enableAPTCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAPTCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(enableAPTEditorCheckBox, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Enable_Editor_Annotation_Processing")); // NOI18N

        annotationProcessorsLabel.setLabelFor(annotationProcessorsList);
        org.openide.awt.Mnemonics.setLocalizedText(annotationProcessorsLabel, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Annotation_Processors")); // NOI18N

        annotationProcessorsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        annotationProcessorsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                annotationProcessorsListValueChanged(evt);
            }
        });
        AnnotationProcessorsScrollPane.setViewportView(annotationProcessorsList);

        org.openide.awt.Mnemonics.setLocalizedText(addProcessorButton, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Add_Annotation_Processor")); // NOI18N
        addProcessorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProcessorButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeProcessorButton, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Remove_Annotation_Processors")); // NOI18N
        removeProcessorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProcessorButtonActionPerformed(evt);
            }
        });

        processorOptionsLabel.setLabelFor(processorOptionsTable);
        org.openide.awt.Mnemonics.setLocalizedText(processorOptionsLabel, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Processor_Options")); // NOI18N

        processorOptionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        processorOptionsTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        processorOptionsTable.getTableHeader().setReorderingAllowed(false);
        processorOptionsScrollPane.setViewportView(processorOptionsTable);
        processorOptionsTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                processorOptionsListSelectionChanged(evt);
            }
        });
        processorOptionsTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "TBL_ACSN_AnnotationProcesserOptions")); // NOI18N
        processorOptionsTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "TBL_ACSD_AnnotationProcesserOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addOptionButton, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Add_Annotation_ProcessorOption")); // NOI18N
        addOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOptionButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeOptionButton, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Remove_Processor_Option")); // NOI18N
        removeOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeOptionButtonActionPerformed(evt);
            }
        });

        additionalJavacParamsLabel.setLabelFor(additionalJavacParamsField);
        org.openide.awt.Mnemonics.setLocalizedText(additionalJavacParamsLabel, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_AdditionalCompilerOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(additionalJavacParamsExample, org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "LBL_AdditionalCompilerOptionsExample")); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(additionalJavacParamsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(additionalJavacParamsExample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(additionalJavacParamsField)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(debugInfoCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deprecationCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(doDependCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enableAPTCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(annotationProcessorsLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(enableAPTEditorCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(processorOptionsLabel)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(processorOptionsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                                                .addComponent(AnnotationProcessorsScrollPane))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(removeOptionButton)
                                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(addOptionButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(addProcessorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(removeProcessorButton))))))))
                        .addGap(2, 2, 2)))
                .addGap(14, 14, 14))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(debugInfoCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deprecationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doDependCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enableAPTCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enableAPTEditorCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(annotationProcessorsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(addProcessorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeProcessorButton))
                    .addComponent(AnnotationProcessorsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processorOptionsLabel)
                .addGap(6, 6, 6)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(processorOptionsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(addOptionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeOptionButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(additionalJavacParamsLabel)
                    .addComponent(additionalJavacParamsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalJavacParamsExample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        debugInfoCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "ACSD_CustomizerCompile_jCheckBoxDebugInfo")); // NOI18N
        deprecationCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "ACSD_CustomizerCompile_jCheckBoxDeprecation")); // NOI18N
        doDependCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "ACSD_doDependCheckBox")); // NOI18N
        enableAPTCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "AD_CustomizeCompile_Enable_Annotation_Processing")); // NOI18N
        enableAPTEditorCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "AD_CustomizeCompile_Enable_Editor_Annotation_Processing")); // NOI18N
        annotationProcessorsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "AD_CustomizeCompile_Annotation_Processors")); // NOI18N
        addProcessorButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSN_AddProcessor")); // NOI18N
        addProcessorButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSD_AddProcessor")); // NOI18N
        removeProcessorButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSN_RemoveProcessor")); // NOI18N
        removeProcessorButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSD_RemoveProcessor")); // NOI18N
        addOptionButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSN_AddProcessorOption")); // NOI18N
        addOptionButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSD_AddProcessorOption")); // NOI18N
        removeOptionButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSD_RemoveProcessorOption")); // NOI18N
        removeOptionButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MECompilingPanel.class, "BTN_ACSD_RemoveProcessorOption")); // NOI18N
        additionalJavacParamsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage (J2MECompilingPanel.class,"AD_AdditionalCompilerOptions"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(mainPanel, gridBagConstraints);

        extPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(extPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addProcessorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProcessorButtonActionPerformed
        final AddAnnotationProcessor panel = new AddAnnotationProcessor();
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(J2MECompilingPanel.class, "LBL_AddAnnotationProcessor_Title")); //NOI18N
        desc.setValid(false);
        panel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String fqn = panel.getProcessorFQN();
                desc.setValid(fqn.length() > 0);
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            ((DefaultListModel) annotationProcessorsList.getModel()).addElement(panel.getProcessorFQN());
        }
        dlg.dispose();
    }//GEN-LAST:event_addProcessorButtonActionPerformed

    private void removeProcessorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeProcessorButtonActionPerformed
        DefaultListModel model = (DefaultListModel) annotationProcessorsList.getModel();
        int[] indices = annotationProcessorsList.getSelectedIndices();
        for (int i = indices.length - 1; i >= 0; i--) {
            model.remove(indices[i]);
        }
        if (!model.isEmpty()) {
            // Select reasonable item
            int selectedIndex = indices[indices.length - 1] - indices.length + 1;
            if (selectedIndex > model.size() - 1) {
                selectedIndex = model.size() - 1;
            }
            annotationProcessorsList.setSelectedIndex(selectedIndex);
        }
    }//GEN-LAST:event_removeProcessorButtonActionPerformed

    private void enableAPTCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAPTCheckBoxActionPerformed
        boolean b = enableAPTCheckBox.isSelected();
        enableAPTEditorCheckBox.setEnabled(b);
        annotationProcessorsLabel.setEnabled(b);
        annotationProcessorsList.setEnabled(b);
        addProcessorButton.setEnabled(b);
        int[] indices = annotationProcessorsList.getSelectedIndices();
        removeProcessorButton.setEnabled(b && indices != null && indices.length > 0);
        processorOptionsLabel.setEnabled(b);
        processorOptionsTable.setEnabled(b);
        addOptionButton.setEnabled(b);
        int[] rows = processorOptionsTable.getSelectedRows();
        removeOptionButton.setEnabled(b && rows != null && rows.length > 0);
    }//GEN-LAST:event_enableAPTCheckBoxActionPerformed

    private void annotationProcessorsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_annotationProcessorsListValueChanged
        int[] indices = annotationProcessorsList.getSelectedIndices();
        removeProcessorButton.setEnabled(enableAPTCheckBox.isSelected() && indices != null && indices.length > 0);
    }//GEN-LAST:event_annotationProcessorsListValueChanged

    private void addOptionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOptionButtonActionPerformed
        final AddProcessorOption panel = new AddProcessorOption();
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(J2MECompilingPanel.class, "LBL_AddProcessorOption_Title")); //NOI18N
        desc.setValid(false);
        panel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String key = panel.getOptionKey();
                for (String s : key.split("\\.", -1)) { //NOI18N
                    if (!SourceVersion.isIdentifier(s)) {
                        desc.setValid(false);
                        return;
                    }
                }
                desc.setValid(true);
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            ((DefaultTableModel) processorOptionsTable.getModel()).addRow(new String[]{panel.getOptionKey(), panel.getOptionValue()});
        }
        dlg.dispose();
    }//GEN-LAST:event_addOptionButtonActionPerformed

    private void removeOptionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeOptionButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) processorOptionsTable.getModel();
        int[] rows = processorOptionsTable.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            model.removeRow(rows[i]);
        }
        if (model.getRowCount() > 0) {
            // Select reasonable row
            int selectedIndex = rows[rows.length - 1] - rows.length + 1;
            if (selectedIndex > model.getRowCount() - 1) {
                selectedIndex = model.getRowCount() - 1;
            }
            processorOptionsTable.setRowSelectionInterval(selectedIndex, selectedIndex);
        }
    }//GEN-LAST:event_removeOptionButtonActionPerformed

    private void processorOptionsListSelectionChanged(javax.swing.event.ListSelectionEvent evt) {
        int[] rows = processorOptionsTable.getSelectedRows();
        removeOptionButton.setEnabled(enableAPTCheckBox.isSelected() && rows != null && rows.length > 0);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane AnnotationProcessorsScrollPane;
    private javax.swing.JButton addOptionButton;
    private javax.swing.JButton addProcessorButton;
    private javax.swing.JLabel additionalJavacParamsExample;
    private javax.swing.JTextField additionalJavacParamsField;
    private javax.swing.JLabel additionalJavacParamsLabel;
    private javax.swing.JLabel annotationProcessorsLabel;
    private javax.swing.JList annotationProcessorsList;
    private javax.swing.JCheckBox debugInfoCheckBox;
    private javax.swing.JCheckBox deprecationCheckBox;
    private javax.swing.JCheckBox doDependCheckBox;
    private javax.swing.JCheckBox enableAPTCheckBox;
    private javax.swing.JCheckBox enableAPTEditorCheckBox;
    private javax.swing.JPanel extPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel processorOptionsLabel;
    private javax.swing.JScrollPane processorOptionsScrollPane;
    private javax.swing.JTable processorOptionsTable;
    private javax.swing.JButton removeOptionButton;
    private javax.swing.JButton removeProcessorButton;
    // End of variables declaration//GEN-END:variables

    private void addPanelFiller(int gridY) {
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        constraints.gridx = 0;
        constraints.gridy = gridY;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        extPanel.add(new Box.Filler(
                new Dimension(),
                new Dimension(),
                new Dimension(10000, 10000)),
                constraints);
    }
}

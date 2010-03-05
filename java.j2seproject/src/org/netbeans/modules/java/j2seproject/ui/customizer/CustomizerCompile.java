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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.Dialog;
import javax.lang.model.SourceVersion;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CustomizerCompile extends JPanel implements HelpCtx.Provider {

    public CustomizerCompile( J2SEProjectProperties uiProperties ) {
        initComponents();

        uiProperties.COMPILE_ON_SAVE_MODEL.setMnemonic(compileOnSave.getMnemonic());
        compileOnSave.setModel(new UnselectedWhenDisabledButtonModel(uiProperties.COMPILE_ON_SAVE_MODEL,
                                                                     J2SEProjectUtil.isCompileOnSaveSupported(uiProperties.getProject())));
        
        uiProperties.JAVAC_DEPRECATION_MODEL.setMnemonic( deprecationCheckBox.getMnemonic() );
        deprecationCheckBox.setModel( uiProperties.JAVAC_DEPRECATION_MODEL );

        uiProperties.JAVAC_DEBUG_MODEL.setMnemonic( debugInfoCheckBox.getMnemonic() );
        debugInfoCheckBox.setModel( uiProperties.JAVAC_DEBUG_MODEL );

        uiProperties.DO_DEPEND_MODEL.setMnemonic(doDependCheckBox.getMnemonic());
        doDependCheckBox.setModel(uiProperties.DO_DEPEND_MODEL);

        uiProperties.ENABLE_ANNOTATION_PROCESSING_MODEL.setMnemonic(enableAPTCheckBox.getMnemonic());
        enableAPTCheckBox.setModel(uiProperties.ENABLE_ANNOTATION_PROCESSING_MODEL);

        uiProperties.ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL.setMnemonic(enableAPTEditorCheckBox.getMnemonic());
        enableAPTEditorCheckBox.setModel(uiProperties.ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL);

        annotationProcessorsList.setModel(uiProperties.ANNOTATION_PROCESSORS_MODEL);
        
        processorOptionsTable.setModel(uiProperties.PROCESSOR_OPTIONS_MODEL);

        additionalJavacParamsField.setDocument( uiProperties.JAVAC_COMPILER_ARG_MODEL );

        enableAPTCheckBoxActionPerformed(null);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerCompile.class );
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        compileOnSave = new javax.swing.JCheckBox();
        compileOnSaveDescription = new javax.swing.JLabel();
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

        org.openide.awt.Mnemonics.setLocalizedText(compileOnSave, org.openide.util.NbBundle.getBundle(CustomizerCompile.class).getString("CustomizerCompile.CompileOnSave")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(compileOnSaveDescription, org.openide.util.NbBundle.getBundle(CustomizerCompile.class).getString("LBL_CompileOnSaveDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(debugInfoCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Compiler_DebugInfo_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deprecationCheckBox, org.openide.util.NbBundle.getBundle(CustomizerCompile.class).getString("LBL_CustomizeCompile_Compiler_Deprecation_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doDependCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "CustomizerCompile.doDependCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(enableAPTCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Enable_Annotation_Processing")); // NOI18N
        enableAPTCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAPTCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(enableAPTEditorCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Enable_Editor_Annotation_Processing")); // NOI18N

        annotationProcessorsLabel.setLabelFor(annotationProcessorsList);
        org.openide.awt.Mnemonics.setLocalizedText(annotationProcessorsLabel, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Annotation_Processors")); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(addProcessorButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Add_Annotation_Processor")); // NOI18N
        addProcessorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProcessorButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeProcessorButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Remove_Annotation_Processors")); // NOI18N
        removeProcessorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProcessorButtonActionPerformed(evt);
            }
        });

        processorOptionsLabel.setLabelFor(processorOptionsTable);
        org.openide.awt.Mnemonics.setLocalizedText(processorOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Processor_Options")); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(addOptionButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Add_Processor_Option")); // NOI18N
        addOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOptionButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeOptionButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Remove_Processor_Option")); // NOI18N
        removeOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeOptionButtonActionPerformed(evt);
            }
        });

        additionalJavacParamsLabel.setLabelFor(additionalJavacParamsField);
        org.openide.awt.Mnemonics.setLocalizedText(additionalJavacParamsLabel, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_AdditionalCompilerOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(additionalJavacParamsExample, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_AdditionalCompilerOptionsExample")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(compileOnSave)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(compileOnSaveDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(debugInfoCheckBox)
                    .add(deprecationCheckBox)
                    .add(doDependCheckBox)
                    .add(enableAPTCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(annotationProcessorsLabel)
                            .add(enableAPTEditorCheckBox)))
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(processorOptionsLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, processorOptionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                                    .add(AnnotationProcessorsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(removeOptionButton)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(addOptionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(addProcessorButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(removeProcessorButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))))
                .add(12, 12, 12))
            .add(layout.createSequentialGroup()
                .add(additionalJavacParamsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(additionalJavacParamsExample)
                    .add(additionalJavacParamsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(compileOnSave)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(compileOnSaveDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(debugInfoCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(deprecationCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(doDependCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(enableAPTCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(enableAPTEditorCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(annotationProcessorsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addProcessorButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeProcessorButton))
                    .add(AnnotationProcessorsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(processorOptionsLabel)
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(processorOptionsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(addOptionButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeOptionButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(additionalJavacParamsLabel)
                    .add(additionalJavacParamsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(additionalJavacParamsExample)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        compileOnSave.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizerCompile.CompileOnSave")); // NOI18N
        debugInfoCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustomizerCompile_jCheckBoxDebugInfo")); // NOI18N
        deprecationCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustomizerCompile_jCheckBoxDeprecation")); // NOI18N
        doDependCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACSD_doDependCheckBox")); // NOI18N
        enableAPTCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Enable_Annotation_Processing")); // NOI18N
        enableAPTEditorCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Enable_Editor_Annotation_Processing")); // NOI18N
        annotationProcessorsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Annotation_Processors")); // NOI18N
        addProcessorButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Add_Annotation_Processor")); // NOI18N
        removeProcessorButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Remove_Annotation_Processors")); // NOI18N
        additionalJavacParamsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage (CustomizerCompile.class,"AD_AdditionalCompilerOptions"));
    }// </editor-fold>//GEN-END:initComponents

    private void addProcessorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProcessorButtonActionPerformed
        final AddAnnotationProcessor panel = new AddAnnotationProcessor();
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage (CustomizerCompile.class, "LBL_AddAnnotationProcessor_Title")); //NOI18N
        desc.setValid(false);
        panel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String fqn = panel.getProcessorFQN();
                desc.setValid(fqn.length() > 0);
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible (true);
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            ((DefaultListModel)annotationProcessorsList.getModel()).addElement(panel.getProcessorFQN());
        }
        dlg.dispose();
    }//GEN-LAST:event_addProcessorButtonActionPerformed

    private void removeProcessorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeProcessorButtonActionPerformed
        DefaultListModel model = (DefaultListModel) annotationProcessorsList.getModel();
        int[] indices = annotationProcessorsList.getSelectedIndices();
        for (int i = indices.length - 1 ; i >= 0 ; i--) {
            model.remove(indices[i]);
        }
        if (!model.isEmpty()) {
            // Select reasonable item
            int selectedIndex = indices[indices.length - 1] - indices.length  + 1; 
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
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage (CustomizerCompile.class, "LBL_AddProcessorOption_Title")); //NOI18N
        desc.setValid(false);
        panel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String key = panel.getOptionKey();
                for(String s : key.split("\\.", -1)) { //NOI18N
                    if (!SourceVersion.isIdentifier(s)) {
                        desc.setValid(false);
                        return;
                    }
                }
                desc.setValid(true);
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible (true);
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            ((DefaultTableModel)processorOptionsTable.getModel()).addRow(new String[] {panel.getOptionKey(), panel.getOptionValue()});
        }
        dlg.dispose();
    }//GEN-LAST:event_addOptionButtonActionPerformed

    private void removeOptionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeOptionButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) processorOptionsTable.getModel();
        int[] rows = processorOptionsTable.getSelectedRows();
        for(int i = rows.length - 1 ; i >= 0 ; i--) {
            model.removeRow(rows[i]);
        }
        if (model.getRowCount() > 0) {
            // Select reasonable row
            int selectedIndex = rows[rows.length - 1] - rows.length  + 1;
            if ( selectedIndex > model.getRowCount() - 1) {
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
    private javax.swing.JCheckBox compileOnSave;
    private javax.swing.JLabel compileOnSaveDescription;
    private javax.swing.JCheckBox debugInfoCheckBox;
    private javax.swing.JCheckBox deprecationCheckBox;
    private javax.swing.JCheckBox doDependCheckBox;
    private javax.swing.JCheckBox enableAPTCheckBox;
    private javax.swing.JCheckBox enableAPTEditorCheckBox;
    private javax.swing.JLabel processorOptionsLabel;
    private javax.swing.JScrollPane processorOptionsScrollPane;
    private javax.swing.JTable processorOptionsTable;
    private javax.swing.JButton removeOptionButton;
    private javax.swing.JButton removeProcessorButton;
    // End of variables declaration//GEN-END:variables

    private static final class UnselectedWhenDisabledButtonModel extends JToggleButton.ToggleButtonModel {
        private final ButtonModel delegate;

        public UnselectedWhenDisabledButtonModel(ButtonModel delegate, boolean enabled) {
            this.delegate = delegate;
            setEnabled(enabled);
        }

        @Override
        public boolean isSelected() {
            return isEnabled() && delegate.isSelected();
        }

        @Override
        public void setSelected(boolean b) {
            delegate.setSelected(b);
        }

    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.ui.wizard.options;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.options.NewOptionsIterator.DataModel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
final class OptionsPanel0 extends BasicWizardIterator.Panel {
    private NewOptionsIterator.DataModel data;
    private DocumentListener fieldsDL;
    
    public OptionsPanel0(final WizardDescriptor setting, final NewOptionsIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title",// NOI18N
                NbBundle.getMessage(OptionsPanel0.class,"LBL_OptionsWizardTitle")); // NOI18N
        
    }
    
    /** Returns array of IDs of primary panels (categories) from project's layer. 
     * Advanced (Miscellaneous) is added as first item.
     * @return array of IDs
     */
    private String[] getPrimaryIdsFromLayer() {
        ArrayList<String> primaryIds = new ArrayList<String>();
        FileSystem layerFS = LayerUtils.layerForProject(this.data.getProject()).layer(false);
        if(layerFS != null) {
            FileObject optionsDialogFO = layerFS.findResource("OptionsDialog"); //NOI18N
            if(optionsDialogFO != null) {
                FileObject[] children = optionsDialogFO.getChildren();
                for (int i = 0; i < children.length; i++) {
                    FileObject child = children[i];
                    if(!child.isFolder()) {
                        primaryIds.add(child.getName());
                    }
                }
                Collections.sort(primaryIds);
            }
        }
        primaryIds.add(0, DataModel.MISCELLANEOUS_LABEL);
        return primaryIds.toArray(new String[primaryIds.size()]);
    }
    
    private void addListeners() {
        if (fieldsDL == null) {
            fieldsDL = new UIUtil.DocumentAdapter() {
                public void insertUpdate(DocumentEvent e) { updateData(); }
            };
            
            categoryNameField.getDocument().addDocumentListener(fieldsDL);
            displayNameField1.getDocument().addDocumentListener(fieldsDL);
            iconField.getDocument().addDocumentListener(fieldsDL);
            titleField.getDocument().addDocumentListener(fieldsDL);
            tooltipField1.getDocument().addDocumentListener(fieldsDL);
            if(primaryPanelCombo.getEditor().getEditorComponent() instanceof JTextField) {
                ((JTextField)primaryPanelCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(fieldsDL);
            }
        }
    }
    
    private void removeListeners() {
        if (fieldsDL != null) {        
            categoryNameField.getDocument().removeDocumentListener(fieldsDL);
            displayNameField1.getDocument().removeDocumentListener(fieldsDL);
            iconField.getDocument().removeDocumentListener(fieldsDL);
            titleField.getDocument().removeDocumentListener(fieldsDL);
            tooltipField1.getDocument().removeDocumentListener(fieldsDL);
            fieldsDL = null;
        }
    }
    
    
    protected void storeToDataModel() {
        removeListeners();
        updateData();
    }
    protected void readFromDataModel() {
        addListeners();
    }
    
    private void updateData() {
        int retCode = 0;
        if (advancedButton.isSelected()) {
            assert !optionsCategoryButton.isSelected();
            retCode = data.setDataForAdvanced(primaryPanelCombo.getEditor().getItem().toString(), displayNameField1.getText(), tooltipField1.getText());
        } else {
            assert optionsCategoryButton.isSelected();
            retCode = data.setDataForOptionCategory(titleField.getText(),
                    categoryNameField.getText(), iconField.getText(), allowSecondaryPanelsCheckBox.isSelected());
        }
        if (DataModel.isSuccessCode(retCode)) {
            markValid();
        } else if (DataModel.isErrorCode(retCode)) {
            setError(data.getErrorMessage(retCode));
        }  else if (DataModel.isWarningCode(retCode)) {
            setWarning(data.getWarningMessage(retCode));
        } else {
            assert false : retCode;
        }
    }
    
    protected String getPanelName() {
        return NbBundle.getMessage(OptionsPanel0.class,"LBL_OptionsPanel0_Title"); // NOI18N
    }
    
    
    protected HelpCtx getHelp() {
        return new HelpCtx(OptionsPanel0.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(OptionsPanel0.class, key);
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_OptionsPanel0"));
        advancedButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_Advanced"));
        optionsCategoryButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_OptionsCategory"));
        titleField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Title"));
        tooltipField1.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Tooltip"));
        displayNameField1.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_DisplayName"));
        categoryNameField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CategoryName"));
        iconField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_IconPath"));
        iconButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_IconButton"));
        allowSecondaryPanelsCheckBox.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_AllowSecondaryPanels"));
        primaryPanelCombo.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_PrimaryPanelCombo"));
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        addListeners();
        updateData();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        removeListeners();
    }
    
    private void enableDisable() {
        boolean advancedEnabled = advancedButton.isSelected();
        assert advancedEnabled != optionsCategoryButton.isSelected();
        
        categoryNameField.setEnabled(!advancedEnabled);
        categoryNameLbl.setEnabled(!advancedEnabled);
        iconButton.setEnabled(!advancedEnabled);
        iconField.setEnabled(!advancedEnabled);
        iconLbl.setEnabled(!advancedEnabled);
        titleField.setEnabled(!advancedEnabled);
        titleLbl.setEnabled(!advancedEnabled);
        allowSecondaryPanelsCheckBox.setEnabled(!advancedEnabled);
    
        primaryPanelComboLbl.setEnabled(advancedEnabled);
        primaryPanelCombo.setEnabled(advancedEnabled);
        displayNameField1.setEnabled(advancedEnabled);
        displayNameLbl1.setEnabled(advancedEnabled);
        tooltipField1.setEnabled(advancedEnabled);
        tooltipLbl1.setEnabled(advancedEnabled);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        advancedButton = new javax.swing.JRadioButton();
        optionsCategoryButton = new javax.swing.JRadioButton();
        dummyPanel = new javax.swing.JPanel();
        categoryNameLbl = new javax.swing.JLabel();
        categoryNameField = new javax.swing.JTextField();
        displayNameLbl1 = new javax.swing.JLabel();
        displayNameField1 = new javax.swing.JTextField();
        tooltipLbl1 = new javax.swing.JLabel();
        tooltipField1 = new javax.swing.JTextField();
        titleLbl = new javax.swing.JLabel();
        titleField = new javax.swing.JTextField();
        iconLbl = new javax.swing.JLabel();
        iconField = new javax.swing.JTextField();
        iconButton = new javax.swing.JButton();
        allowSecondaryPanelsCheckBox = new javax.swing.JCheckBox();
        primaryPanelComboLbl = new javax.swing.JLabel();
        primaryPanelCombo = new javax.swing.JComboBox();

        buttonGroup1.add(advancedButton);
        advancedButton.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(advancedButton, bundle.getString("LBL_Advanced")); // NOI18N
        advancedButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        advancedButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        advancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(optionsCategoryButton);
        org.openide.awt.Mnemonics.setLocalizedText(optionsCategoryButton, bundle.getString("LBL_OptionsCategory")); // NOI18N
        optionsCategoryButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optionsCategoryButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        optionsCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsCategoryButtonActionPerformed(evt);
            }
        });

        categoryNameLbl.setLabelFor(categoryNameField);
        org.openide.awt.Mnemonics.setLocalizedText(categoryNameLbl, bundle.getString("LBL_CategoryName")); // NOI18N
        categoryNameLbl.setEnabled(false);

        categoryNameField.setEnabled(false);

        displayNameLbl1.setLabelFor(displayNameField1);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLbl1, bundle.getString("LBL_DisplaName")); // NOI18N

        tooltipLbl1.setLabelFor(tooltipField1);
        org.openide.awt.Mnemonics.setLocalizedText(tooltipLbl1, bundle.getString("LBL_Tooltip")); // NOI18N

        titleLbl.setLabelFor(titleField);
        org.openide.awt.Mnemonics.setLocalizedText(titleLbl, bundle.getString("LBL_Title")); // NOI18N
        titleLbl.setEnabled(false);

        titleField.setEnabled(false);

        iconLbl.setLabelFor(iconField);
        org.openide.awt.Mnemonics.setLocalizedText(iconLbl, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Icon")); // NOI18N
        iconLbl.setEnabled(false);

        iconField.setEditable(false);
        iconField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "CTL_None")); // NOI18N
        iconField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(iconButton, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Icon_Browse")); // NOI18N
        iconButton.setEnabled(false);
        iconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(allowSecondaryPanelsCheckBox, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_AllowSecondaryPanels")); // NOI18N
        allowSecondaryPanelsCheckBox.setEnabled(false);
        allowSecondaryPanelsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allowSecondaryPanelsCheckBoxActionPerformed(evt);
            }
        });

        primaryPanelComboLbl.setLabelFor(primaryPanelCombo);
        org.openide.awt.Mnemonics.setLocalizedText(primaryPanelComboLbl, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_PrimaryPanelCombo")); // NOI18N

        primaryPanelCombo.setEditable(true);
        primaryPanelCombo.setModel(new DefaultComboBoxModel(getPrimaryIdsFromLayer()));
        primaryPanelCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primaryPanelComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(advancedButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(98, 98, 98))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(allowSecondaryPanelsCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(277, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(18, 18, 18)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(tooltipLbl1)
                                .add(displayNameLbl1)
                                .add(primaryPanelComboLbl))
                            .add(22, 22, 22)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(primaryPanelCombo, 0, 379, Short.MAX_VALUE)
                                .add(displayNameField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                                .add(tooltipField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
                            .add(10, 10, 10))
                        .add(layout.createSequentialGroup()
                            .add(13, 13, 13)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(categoryNameLbl)
                                .add(iconLbl))
                            .add(19, 19, 19)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(layout.createSequentialGroup()
                                    .add(iconField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                    .add(iconButton))
                                .add(categoryNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                                .add(titleField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
                            .add(10, 10, 10))
                        .add(layout.createSequentialGroup()
                            .add(235, 235, 235)
                            .add(dummyPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(layout.createSequentialGroup()
                            .add(13, 13, 13)
                            .add(titleLbl)
                            .add(373, 373, 373)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, optionsCategoryButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(advancedButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(primaryPanelComboLbl)
                    .add(primaryPanelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameLbl1)
                    .add(displayNameField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tooltipLbl1)
                    .add(tooltipField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(12, 12, 12)
                .add(optionsCategoryButton)
                .add(9, 9, 9)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(titleLbl)
                    .add(titleField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(categoryNameLbl)
                    .add(categoryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(iconLbl)
                    .add(iconButton)
                    .add(iconField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(allowSecondaryPanelsCheckBox)
                .add(28, 28, 28)
                .add(dummyPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(56, 56, 56))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void optionsCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsCategoryButtonActionPerformed
        enableDisable();
        updateData();
    }//GEN-LAST:event_optionsCategoryButtonActionPerformed
    
    private void advancedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedButtonActionPerformed
        enableDisable();
        updateData();
    }//GEN-LAST:event_advancedButtonActionPerformed
    
    private void iconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconButtonActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser(iconField.getText());
        int ret = chooser.showDialog(this, getMessage("LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File iconFile =  chooser.getSelectedFile();
            iconField.setText(iconFile.getAbsolutePath());
            //updateData();
        }
    }//GEN-LAST:event_iconButtonActionPerformed

private void allowSecondaryPanelsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allowSecondaryPanelsCheckBoxActionPerformed
    updateData();
}//GEN-LAST:event_allowSecondaryPanelsCheckBoxActionPerformed

private void primaryPanelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primaryPanelComboActionPerformed
    updateData();
}//GEN-LAST:event_primaryPanelComboActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton advancedButton;
    private javax.swing.JCheckBox allowSecondaryPanelsCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField categoryNameField;
    private javax.swing.JLabel categoryNameLbl;
    private javax.swing.JTextField displayNameField1;
    private javax.swing.JLabel displayNameLbl1;
    private javax.swing.JPanel dummyPanel;
    private javax.swing.JButton iconButton;
    private javax.swing.JTextField iconField;
    private javax.swing.JLabel iconLbl;
    private javax.swing.JRadioButton optionsCategoryButton;
    private javax.swing.JComboBox primaryPanelCombo;
    private javax.swing.JLabel primaryPanelComboLbl;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleLbl;
    private javax.swing.JTextField tooltipField1;
    private javax.swing.JLabel tooltipLbl1;
    // End of variables declaration//GEN-END:variables
    
}

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
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
            secondaryPanelTitle.getDocument().addDocumentListener(fieldsDL);
            iconField.getDocument().addDocumentListener(fieldsDL);
            primaryPanelTitle.getDocument().addDocumentListener(fieldsDL);
            tooltipField1.getDocument().addDocumentListener(fieldsDL);
            primaryKwField.getDocument().addDocumentListener(fieldsDL);
            secondaryKwField.getDocument().addDocumentListener(fieldsDL);
            if(primaryPanelCombo.getEditor().getEditorComponent() instanceof JTextField) {
                ((JTextField)primaryPanelCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(fieldsDL);
            }
        }
    }
    
    private void removeListeners() {
        if (fieldsDL != null) {        
            categoryNameField.getDocument().removeDocumentListener(fieldsDL);
            secondaryPanelTitle.getDocument().removeDocumentListener(fieldsDL);
            iconField.getDocument().removeDocumentListener(fieldsDL);
            primaryPanelTitle.getDocument().removeDocumentListener(fieldsDL);
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

    private boolean smallerThan110(String version) {
        String[] ver = version.split("\\.");
        if (Integer.parseInt(ver[0]) <= 1 && Integer.parseInt(ver[1]) < 10) {
            return true;
        } else {
            return false;
        }
    }
    
    private void updateData() {
        NbPlatform platform = LayerUtils.getPlatformForProject(data.getProject());

        //do not allow platforms older then 6.5
        if (smallerThan110(platform.getModule("org.netbeans.modules.options.api").getSpecificationVersion())) { // NOI18N
            setError(NbBundle.getMessage(OptionsPanel0.class, "MSG_INVALID_PLATFORM")); // NOI18N
            return;
        }

        int retCode = 0;
        if (advancedButton.isSelected()) {
            assert !optionsCategoryButton.isSelected();
            retCode = data.setDataForSecondaryPanel(
                    primaryPanelCombo.getEditor().getItem().toString(),
                    secondaryPanelTitle.getText(),
                    tooltipField1.getText(),
                    secondaryKwField.getText());
        } else {
            assert optionsCategoryButton.isSelected();
            retCode = data.setDataForPrimaryPanel(
                    primaryPanelTitle.getText(),
                    categoryNameField.getText(),
                    iconField.getText(),
                    allowSecondaryPanelsCheckBox.isSelected(),
                    primaryKwField.getText());
        }
        
        String msg = data.getMessage(retCode);
        if (DataModel.isSuccessCode(retCode)) {
            markValid();
        } else if (DataModel.isErrorCode(retCode)) {
            setError(msg);
        }  else if (DataModel.isWarningCode(retCode)) {
            setWarning(msg);
        } else if (DataModel.isInfoCode(retCode)) {
            setInfo(msg, false);
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
        primaryPanelTitle.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Title"));
        tooltipField1.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Tooltip"));
        secondaryPanelTitle.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_DisplayName"));
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
        primaryPanelTitle.setEnabled(!advancedEnabled);
        primaryKwField.setEnabled(!advancedEnabled);
        primKeywordsLabel.setEnabled(!advancedEnabled);
        titleLbl.setEnabled(!advancedEnabled);
        allowSecondaryPanelsCheckBox.setEnabled(!advancedEnabled);
    
        primaryPanelComboLbl.setEnabled(advancedEnabled);
        primaryPanelCombo.setEnabled(advancedEnabled);
        secondaryPanelTitle.setEnabled(advancedEnabled);
        secondaryKwField.setEditable(advancedEnabled);
        keywordsLabel.setEnabled(advancedEnabled);
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
        secondaryPanelTitle = new javax.swing.JTextField();
        tooltipLbl1 = new javax.swing.JLabel();
        tooltipField1 = new javax.swing.JTextField();
        titleLbl = new javax.swing.JLabel();
        primaryPanelTitle = new javax.swing.JTextField();
        iconLbl = new javax.swing.JLabel();
        iconField = new javax.swing.JTextField();
        iconButton = new javax.swing.JButton();
        allowSecondaryPanelsCheckBox = new javax.swing.JCheckBox();
        primaryPanelComboLbl = new javax.swing.JLabel();
        primaryPanelCombo = new javax.swing.JComboBox();
        keywordsLabel = new javax.swing.JLabel();
        primKeywordsLabel = new javax.swing.JLabel();
        secondaryKwField = new javax.swing.JTextField();
        primaryKwField = new javax.swing.JTextField();

        buttonGroup1.add(advancedButton);
        advancedButton.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(advancedButton, bundle.getString("LBL_Advanced")); // NOI18N
        advancedButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        advancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(optionsCategoryButton);
        org.openide.awt.Mnemonics.setLocalizedText(optionsCategoryButton, bundle.getString("LBL_OptionsCategory")); // NOI18N
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

        displayNameLbl1.setLabelFor(secondaryPanelTitle);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLbl1, bundle.getString("LBL_DisplaName")); // NOI18N

        tooltipLbl1.setLabelFor(tooltipField1);
        org.openide.awt.Mnemonics.setLocalizedText(tooltipLbl1, bundle.getString("LBL_Tooltip")); // NOI18N

        titleLbl.setLabelFor(primaryPanelTitle);
        org.openide.awt.Mnemonics.setLocalizedText(titleLbl, bundle.getString("LBL_Title")); // NOI18N
        titleLbl.setEnabled(false);

        primaryPanelTitle.setEnabled(false);

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

        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Keywords")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(primKeywordsLabel, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Keywords")); // NOI18N
        primKeywordsLabel.setEnabled(false);

        secondaryKwField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "OptionsPanel0.secondaryKwField.text")); // NOI18N

        primaryKwField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "OptionsPanel0.primaryKwField.text")); // NOI18N
        primaryKwField.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(advancedButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(98, 98, 98))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tooltipLbl1)
                            .add(displayNameLbl1)
                            .add(primaryPanelComboLbl)
                            .add(keywordsLabel))
                        .add(22, 22, 22)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(secondaryKwField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                            .add(primaryPanelCombo, 0, 403, Short.MAX_VALUE)
                            .add(secondaryPanelTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                            .add(tooltipField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                        .add(10, 10, 10))
                    .add(layout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(categoryNameLbl)
                            .add(iconLbl)
                            .add(primKeywordsLabel))
                        .add(19, 19, 19)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(primaryKwField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(iconField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(iconButton))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, categoryNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, primaryPanelTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE))
                        .add(10, 10, 10))
                    .add(layout.createSequentialGroup()
                        .add(235, 235, 235)
                        .add(dummyPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(titleLbl)
                        .add(373, 373, 373)))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(optionsCategoryButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(allowSecondaryPanelsCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(320, Short.MAX_VALUE))
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
                    .add(secondaryPanelTitle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tooltipLbl1)
                    .add(tooltipField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keywordsLabel)
                    .add(secondaryKwField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(22, 22, 22)
                .add(optionsCategoryButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(titleLbl)
                    .add(primaryPanelTitle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(categoryNameLbl)
                    .add(categoryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(iconLbl)
                    .add(iconButton)
                    .add(iconField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(primKeywordsLabel)
                    .add(primaryKwField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allowSecondaryPanelsCheckBox)
                .add(22, 22, 22)
                .add(dummyPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(56, 56, 56))
        );

        advancedButton.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.advancedButton.AccessibleContext.accessibleDescription")); // NOI18N
        optionsCategoryButton.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.optionsCategoryButton.AccessibleContext.accessibleDescription")); // NOI18N
        dummyPanel.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.dummyPanel.AccessibleContext.accessibleName")); // NOI18N
        dummyPanel.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.dummyPanel.AccessibleContext.accessibleDescription")); // NOI18N
        categoryNameLbl.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.categoryNameLbl.AccessibleContext.accessibleDescription")); // NOI18N
        categoryNameField.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.categoryNameField.AccessibleContext.accessibleName")); // NOI18N
        categoryNameField.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.categoryNameField.AccessibleContext.accessibleDescription")); // NOI18N
        displayNameLbl1.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.displayNameLbl1.AccessibleContext.accessibleDescription")); // NOI18N
        secondaryPanelTitle.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.displayNameField1.AccessibleContext.accessibleName")); // NOI18N
        secondaryPanelTitle.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.displayNameField1.AccessibleContext.accessibleDescription")); // NOI18N
        tooltipLbl1.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.tooltipLbl1.AccessibleContext.accessibleDescription")); // NOI18N
        tooltipField1.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.tooltipField1.AccessibleContext.accessibleName")); // NOI18N
        tooltipField1.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.tooltipField1.AccessibleContext.accessibleDescription")); // NOI18N
        titleLbl.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.titleLbl.AccessibleContext.accessibleDescription")); // NOI18N
        primaryPanelTitle.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.titleField.AccessibleContext.accessibleName")); // NOI18N
        primaryPanelTitle.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.titleField.AccessibleContext.accessibleDescription")); // NOI18N
        iconLbl.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.iconLbl.AccessibleContext.accessibleDescription")); // NOI18N
        iconField.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.iconField.AccessibleContext.accessibleName")); // NOI18N
        iconField.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.iconField.AccessibleContext.accessibleDescription")); // NOI18N
        iconButton.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.iconButton.AccessibleContext.accessibleDescription")); // NOI18N
        allowSecondaryPanelsCheckBox.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.allowSecondaryPanelsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        primaryPanelComboLbl.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.primaryPanelComboLbl.AccessibleContext.accessibleDescription")); // NOI18N
        primaryPanelCombo.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.primaryPanelCombo.AccessibleContext.accessibleName")); // NOI18N
        primaryPanelCombo.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.primaryPanelCombo.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.AccessibleContext.accessibleDescription")); // NOI18N
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
            String iconPath = iconFile.getAbsolutePath();
            String srcFolder = this.data.getProject().getProjectDirectory() + "/src"; //NOI18N
            if (!iconPath.contains(srcFolder)) { //seleced icon is not placed within project 'src' folder
                //seleced icon is not placed within project 'src' folder
                String iconFileName = iconFile.getName();
                String packageName = data.getPackageName().replace('.', '/');
                File target = new File(srcFolder + "/" + packageName, iconFileName);
                try {
                    copyFile(iconFile, target);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                iconField.setText(packageName + "/" + iconFileName); //NOI18N
            } else {
                iconField.setText(iconPath.substring(srcFolder.length() + 1));
            }
            //updateData();
        }
    }//GEN-LAST:event_iconButtonActionPerformed

    private void copyFile(File source, File target) throws IOException {
        FileChannel ic = new FileInputStream(source).getChannel();
        FileChannel oc = new FileOutputStream(target).getChannel();
        ic.transferTo(0, ic.size(), oc);
        ic.close();
        oc.close();
    }

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
    private javax.swing.JLabel displayNameLbl1;
    private javax.swing.JPanel dummyPanel;
    private javax.swing.JButton iconButton;
    private javax.swing.JTextField iconField;
    private javax.swing.JLabel iconLbl;
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JRadioButton optionsCategoryButton;
    private javax.swing.JLabel primKeywordsLabel;
    private javax.swing.JTextField primaryKwField;
    private javax.swing.JComboBox primaryPanelCombo;
    private javax.swing.JLabel primaryPanelComboLbl;
    private javax.swing.JTextField primaryPanelTitle;
    private javax.swing.JTextField secondaryKwField;
    private javax.swing.JTextField secondaryPanelTitle;
    private javax.swing.JLabel titleLbl;
    private javax.swing.JTextField tooltipField1;
    private javax.swing.JLabel tooltipLbl1;
    // End of variables declaration//GEN-END:variables
    
}

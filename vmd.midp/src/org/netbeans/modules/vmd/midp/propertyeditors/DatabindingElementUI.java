/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * DataSetDatabindingElement.java
 *
 * Created on June 3, 2008, 4:20 PM
 */
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.categories.DatabindingCategoryCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetAbstractCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetConnectorCD;
import org.netbeans.modules.vmd.midp.components.databinding.IndexableDataAbstractSetCD;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class DatabindingElementUI extends javax.swing.JPanel implements CleanUp {

    private static String NOT_DEFINED = NbBundle.getMessage(DatabindingElementUI.class, "LBL_NOT_DEFINED"); //TODO Localized
    private DesignPropertyEditor propertyEditor;
    private JRadioButton radioButton;
    private static String INDEXABLE = NbBundle.getMessage(DatabindingElementUI.class, "LBL_INDEXABLE"); //NOI18N
    private static String DATASET = NbBundle.getMessage(DatabindingElementUI.class, "LBL_DATASET"); //NOI18N
    private static String CREATE_INDEX = NbBundle.getMessage(DatabindingElementUI.class, "LBL_CREATE_INDEX"); //NOI18N

    /** Creates new form DataSetDatabindingElement */
    DatabindingElementUI(DesignPropertyEditor propertyEditor, final JRadioButton radioButton) {
        this.propertyEditor = propertyEditor;
        this.radioButton = radioButton;
        initComponents();
        jComboBoxDatasets.addActionListener(new UpdateUIListener());
        updateDataSetRelatedUI();
        ComponentFocusAdapter focusListener = new ComponentFocusAdapter();
        jTextFieldExpressionRead.addFocusListener(focusListener);
        jComboBoxCommandUpdate.addFocusListener(focusListener);
        jComboBoxDatasets.addFocusListener(focusListener);
        jComboBoxIndexNames.addFocusListener(focusListener);
        jComboBoxIndexNames.addActionListener(new UpdateUIListener());

        jComboBoxCommandsIndexablePrevious.addFocusListener(focusListener);
        jComboBoxIndexableNext.addFocusListener(focusListener);
        jCheckBox1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (jCheckBox1.isSelected()) {
                    jTextFieldExpressionWrite.setEnabled(false);
                    jTextFieldExpressionWrite.setText(jTextFieldExpressionRead.getText());
                } else {
                    jTextFieldExpressionWrite.setEnabled(true);
                    jTextFieldExpressionWrite.setText(null);
                }
            }
        });

        jTextFieldExpressionRead.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                updateWarning();
                if (jCheckBox1.isSelected()) {
                    jTextFieldExpressionWrite.setText(jTextFieldExpressionRead.getText());
                }
            }
        });
        TextFieldFocusListener fl = new TextFieldFocusListener();
        jTextFieldExpressionRead.addFocusListener(fl);
        jTextFieldExpressionWrite.addFocusListener(fl);


        radioButton.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (!radioButton.isSelected()) {
                    jLabelWarning.setText(null);
                    //jLabelWarning1.setText(null);
                    jLabelWarning2.setText(null);
                } else {
                    updateWarning();
                }
            }
        });
        updateIndexableUIComponents();


    }

    private boolean updateWarning() {

        if (radioButton.isSelected()) {
            StringBuffer warning = new StringBuffer();
            if (jComboBoxDatasets.getSelectedItem() == null || jComboBoxDatasets.getSelectedItem() == NOT_DEFINED) {
                warning.append(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_Dataset_not_Selected"));
            }
            if (jTextFieldExpressionRead.getText() == null || jTextFieldExpressionRead.getText().length() == 0) {
                if (warning.length() != 0) {
                    warning.append(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNING_and_"));
                }
                warning.append(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNING_Empty_expression"));
            }
//            if (jComboBoxIndexNames.getSelectedItem() == null || jComboBoxIndexNames.getSelectedItem() == NULL) {
//                if (jComboBoxDatasets.getSelectedItem() != null && ((String) jComboBoxDatasets.getSelectedItem()).contains(INDEXABLE)) {
//                    if (warning.length() != 0) {
//                        warning.append(" and ");
//                    }
//                    warning.append("No index set for Indexable DataSet");
//                }
//            }
            if (warning.length() != 0) {
                jLabelWarning.setText(warning.toString());
                jLabelWarning1.setText(warning.toString());
                jLabelWarning2.setText(warning.toString());
                return false;
            } else {
                jLabelWarning.setText(null);
                jLabelWarning1.setText(null);
                jLabelWarning2.setText(null);
            }
        } else {
            jLabelWarning.setText(null);
            jLabelWarning1.setText(null);
            jLabelWarning2.setText(null);
        }
        return true;
    }

   

    private void updateDataSetRelatedUI() {
        if (jComboBoxDatasets.getSelectedItem() != null && jComboBoxDatasets.getSelectedItem() != NOT_DEFINED) {
            jTextFieldExpressionRead.setEnabled(true);

            if (jTextFieldExpressionRead.getText().equals(jTextFieldExpressionWrite.getText())) {
                jCheckBox1.setSelected(true);
                jTextFieldExpressionWrite.setEnabled(false);
            } else {
                jCheckBox1.setSelected(false);
                jTextFieldExpressionWrite.setEnabled(true);
            }
            jComboBoxCommandUpdate.setEnabled(true);
        } else {
            jCheckBox1.setEnabled(true);
            jCheckBox1.setSelected(true);
            jTextFieldExpressionWrite.setEnabled(false);
            jTextFieldExpressionRead.setEnabled(false);
            jComboBoxCommandUpdate.setEnabled(false);
        }
        if (!(jComboBoxDatasets.getModel() instanceof Model)) {
            return;
        }
        Model model = (Model) jComboBoxDatasets.getModel();
        if (model.isSelectedDataSetReadOnly() != null) {
            if (model.isSelectedDataSetReadOnly()) {
                jLabelReadOnly.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNINGRead_Only"));
            } else {
                jLabelReadOnly.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNING_Read/Write"));
            }
        } else {
            jLabelReadOnly.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNING_Not_Defined"));
        }
        if (jComboBoxIndexNames.getSelectedItem() == null || jComboBoxIndexNames.getSelectedItem() == NOT_DEFINED) {
            jComboBoxCommandsIndexablePrevious.setEnabled(false);
            jComboBoxIndexableNext.setEnabled(false);
        } else {
            jComboBoxCommandsIndexablePrevious.setEnabled(true);
            jComboBoxIndexableNext.setEnabled(true);
        }
    }

     public void clean(DesignComponent component) {
        propertyEditor = null;
        radioButton = null;
        jCheckBox1 = null;
        jComboBoxCommandUpdate = null;
        jComboBoxCommandsIndexablePrevious = null;
        jComboBoxDatasets = null;
        jComboBoxIndexNames = null;
        jComboBoxIndexableNext = null;
        jLabel1 = null;
        jLabel2 = null;
        jLabel3 = null;
        jLabel4 = null;
        jLabel5 = null;
        jLabel6 = null;
        jLabel7 = null;
        jLabel8 = null;
        jLabel9 = null;
        jLabelReadOnly = null;
        jLabelWarning = null;
        jLabelWarning1 = null;
        jLabelWarning2 = null;
        jPanel1 = null;
        jPanel2 = null;
        jPanel3 = null;
        jPanel4 = null;
        jPanel5 = null;
        jTabbedPane1 = null;
        jTextFieldExpressionRead = null;
        jTextFieldExpressionWrite = null;
        this.removeAll();
    }

    private void updateIndexableUIComponents() {
        String name = (String) jComboBoxDatasets.getSelectedItem();
        if (name != null && name.contains(INDEXABLE)) {
            jComboBoxCommandsIndexablePrevious.setEnabled(true);
            jComboBoxIndexableNext.setEnabled(true);
            jComboBoxIndexNames.setEnabled(true);
        } else if (name != null && name.contains(DATASET) || name != null && name.equals(NOT_DEFINED)) {
            jComboBoxCommandsIndexablePrevious.setEnabled(false);
            jComboBoxCommandsIndexablePrevious.setSelectedItem(NOT_DEFINED);
            jComboBoxIndexableNext.setEnabled(false);
            jComboBoxIndexableNext.setSelectedItem(NOT_DEFINED);
            jComboBoxIndexNames.setEnabled(false);
            jComboBoxIndexNames.setSelectedItem(NOT_DEFINED);
        }
        if (jComboBoxIndexNames.getSelectedItem() == null || jComboBoxIndexNames.getSelectedItem() == NOT_DEFINED) {
            jComboBoxCommandsIndexablePrevious.setEnabled(false);
            jComboBoxIndexableNext.setEnabled(false);
        } else {
            jComboBoxCommandsIndexablePrevious.setEnabled(true);
            jComboBoxIndexableNext.setEnabled(true);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxDatasets = new javax.swing.JComboBox();
        jTextFieldExpressionRead = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabelReadOnly = new javax.swing.JLabel();
        jLabelWarning = new javax.swing.JLabel();
        jComboBoxIndexNames = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxCommandUpdate = new javax.swing.JComboBox();
        jTextFieldExpressionWrite = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabelWarning2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxIndexableNext = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxCommandsIndexablePrevious = new javax.swing.JComboBox();
        jLabelWarning1 = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(0, 0));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(355, 225));

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel1.border.title"))); // NOI18N
        jPanel1.setFocusable(false);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel1.text")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(68, 16));

        jLabel2.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel2.text")); // NOI18N

        jTextFieldExpressionRead.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jTextFieldExpressionRead.text")); // NOI18N
        jTextFieldExpressionRead.setEnabled(false);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel4.text_1")); // NOI18N

        jLabelReadOnly.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabelReadOnly.text")); // NOI18N

        jLabelWarning.setForeground(new java.awt.Color(255, 0, 0));
        jLabelWarning.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabelWarning.text_1")); // NOI18N
        jLabelWarning.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jComboBoxIndexNames.setEnabled(false);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabelWarning, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelReadOnly, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                        .addGap(170, 170, 170))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxDatasets, 0, 246, Short.MAX_VALUE)
                            .addComponent(jComboBoxIndexNames, 0, 246, Short.MAX_VALUE)
                            .addComponent(jTextFieldExpressionRead, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                        .addGap(14, 14, 14))))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel3});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxDatasets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxIndexNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldExpressionRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabelReadOnly))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addComponent(jLabelWarning))
        );

        jComboBoxDatasets.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingDataSets")); // NOI18N
        jComboBoxDatasets.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingDataSets")); // NOI18N
        jTextFieldExpressionRead.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingELRead")); // NOI18N
        jTextFieldExpressionRead.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingELRead")); // NOI18N
        jComboBoxIndexNames.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingIndex")); // NOI18N
        jComboBoxIndexNames.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingIndex")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel2.border.title"))); // NOI18N
        jPanel2.setFocusable(false);

        jLabel8.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel8.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel5.text")); // NOI18N

        jTextFieldExpressionWrite.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jTextFieldExpressionWrite.text")); // NOI18N
        jTextFieldExpressionWrite.setEnabled(false);

        jLabel9.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel9.text")); // NOI18N

        jCheckBox1.setSelected(true);
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jCheckBox1.text")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabelWarning2.setForeground(new java.awt.Color(255, 0, 0));
        jLabelWarning2.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabelWarning2.text")); // NOI18N
        jLabelWarning2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 69, Short.MAX_VALUE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(262, 262, 262)
                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 9, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                            .addComponent(jTextFieldExpressionWrite, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                            .addComponent(jComboBoxCommandUpdate, 0, 247, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabelWarning2, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel5, jLabel9});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBoxCommandUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextFieldExpressionWrite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                .addComponent(jLabelWarning2))
        );

        jComboBoxCommandUpdate.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingCommand")); // NOI18N
        jComboBoxCommandUpdate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingCommand")); // NOI18N
        jTextFieldExpressionWrite.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingELWrite")); // NOI18N
        jTextFieldExpressionWrite.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingELWrite")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel4.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel3.border.title"))); // NOI18N
        jPanel3.setFocusable(false);
        jPanel3.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel6.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel6.text")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(68, 16));

        jComboBoxIndexableNext.setEnabled(false);

        jLabel7.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel7.text")); // NOI18N

        jComboBoxCommandsIndexablePrevious.setEnabled(false);

        jLabelWarning1.setForeground(new java.awt.Color(255, 0, 0));
        jLabelWarning1.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabelWarning1.text")); // NOI18N
        jLabelWarning1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelWarning1, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxCommandsIndexablePrevious, 0, 248, Short.MAX_VALUE)
                            .addComponent(jComboBoxIndexableNext, 0, 248, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel6, jLabel7});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxIndexableNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBoxCommandsIndexablePrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                .addComponent(jLabelWarning1))
        );

        jComboBoxIndexableNext.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingNext")); // NOI18N
        jComboBoxIndexableNext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingNext")); // NOI18N
        jComboBoxCommandsIndexablePrevious.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingPrevious")); // NOI18N
        jComboBoxCommandsIndexablePrevious.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingPrevious")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCN_DatabindingPane")); // NOI18N
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "ASCD_DatabindingPane")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
// TODO add your handling code here://GEN-LAST:event_jCheckBox1ActionPerformed
    }

    public void updateComponent(final DesignComponent component) {
        if (component == null) {
            return;
        }
        final DesignDocument document = component.getDocument();
        document.getTransactionManager().readAccess(new Runnable() {

            public void run() {
                jComboBoxDatasets.setModel(new Model(component, DatabindingCategoryCD.TYPEID));
                jComboBoxCommandUpdate.setModel(new Model(component, CommandsCategoryCD.TYPEID));
                jComboBoxCommandsIndexablePrevious.setModel(new Model(component, CommandsCategoryCD.TYPEID));
                jComboBoxIndexableNext.setModel(new Model(component, CommandsCategoryCD.TYPEID));
                Model indexNamesModel = new Model(component);
                jComboBoxIndexNames.setModel(indexNamesModel);
                indexNamesModel.addListDataListener(new Listener());

                DesignComponent connector = MidpDatabindingSupport.getConnector(component, propertyEditor.getPropertyNames().get(0));
                if (connector != null) {
                    radioButton.setSelected(true);
                    String dataSetName = createDataSetName((String) connector.getParentComponent().readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue(), connector.getParentComponent());
                    jComboBoxDatasets.setSelectedItem(dataSetName);
                    String readExpression = (String) connector.readProperty(DataSetConnectorCD.PROP_EXPRESSION_READ).getPrimitiveValue();
                    String writeExpression = (String) connector.readProperty(DataSetConnectorCD.PROP_EXPRESSION_WRITE).getPrimitiveValue();
                    jTextFieldExpressionRead.setText(readExpression);
                    jTextFieldExpressionWrite.setText(writeExpression);
                    jComboBoxIndexNames.setSelectedItem(MidpDatabindingSupport.getIndexName(connector));

                    setCommandComboBox(connector, jComboBoxCommandUpdate, DataSetConnectorCD.PROP_UPDATE_COMMAND);
                    setCommandComboBox(connector, jComboBoxIndexableNext, DataSetConnectorCD.PROP_NEXT_COMMAND);
                    setCommandComboBox(connector, jComboBoxCommandsIndexablePrevious, DataSetConnectorCD.PROP_PREVIOUS_COMMAND);
                    setReadOnlyLabel(connector);
                } else {
                    jComboBoxCommandUpdate.setSelectedItem(NOT_DEFINED);
                    jComboBoxCommandsIndexablePrevious.setSelectedItem(NOT_DEFINED);
                    jComboBoxDatasets.setSelectedItem(NOT_DEFINED);
                    jComboBoxIndexNames.setSelectedItem(NOT_DEFINED);
                    jComboBoxIndexableNext.setSelectedItem(NOT_DEFINED);
                }
            }
        });
        updateDataSetRelatedUI();
        updateIndexableUIComponents();
        updateWarning();
    }

    private void setReadOnlyLabel(DesignComponent connector) {
        PropertyValue value = connector.getParentComponent().readProperty(DataSetAbstractCD.PROP_READ_ONLY);
        if (value != PropertyValue.createNull() && value.getPrimitiveValue() == Boolean.TRUE) {
            jLabelReadOnly.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNING_Read_Only"));
        } else {
            jLabelReadOnly.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNING_Read/Write"));
        }
    }

    private void setCommandComboBox(DesignComponent connector, JComboBox comboBox, String propertyName) {
        DesignComponent command = connector.readProperty(propertyName).getComponent();
        if (command != null) {
            String commandName = (String) command.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
            comboBox.setSelectedItem(commandName);
        }

    }

    public void saveToModel(final DesignComponent component) {
        final DesignDocument document = component.getDocument();
        if (!updateWarning()) {
            return;
        }
        document.getTransactionManager().writeAccess(new Runnable() {

            public void run() {

                String selectedDataSet = cleanUpDataSetName((String) jComboBoxDatasets.getSelectedItem());
                String selectedUpdateCommand = (String) jComboBoxCommandUpdate.getSelectedItem();
                String selectedNextCommand = (String) jComboBoxIndexableNext.getSelectedItem();
                String selectedPreviousCommand = (String) jComboBoxCommandsIndexablePrevious.getSelectedItem();
                Collection<DesignComponent> dataSets = MidpDocumentSupport.getCategoryComponent(document, DatabindingCategoryCD.TYPEID).getComponents();
                MidpDatabindingSupport.removeUnusedConnector(component, propertyEditor.getPropertyNames().get(0));
                for (DesignComponent dataSet : dataSets) {
                    if (dataSet.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue().equals(selectedDataSet)) {
                        DesignComponent connector = MidpDatabindingSupport.getConnector(component, propertyEditor.getPropertyNames().get(0));
                        if (connector == null) {
                            connector = document.createComponent(DataSetConnectorCD.TYPEID);
                            connector.writeProperty(DataSetConnectorCD.PROP_BINDED_PROPERTY, MidpTypes.createStringValue(propertyEditor.getPropertyNames().get(0)));
                            dataSet.addComponent(connector);
                        }
                        if (jComboBoxIndexNames.getSelectedItem() != null && !jComboBoxIndexNames.getSelectedItem().equals(NOT_DEFINED)) {
                            if (MidpDatabindingSupport.isIndexableDataSet(document, dataSet.getType())) {
                                String indexName = (String) jComboBoxIndexNames.getSelectedItem();
                                DesignComponent index = MidpDatabindingSupport.getIndex(dataSet, indexName);
                                if (index == null) {
                                    index = MidpDatabindingSupport.createIndex(dataSet, indexName);
                                }
                                connector.writeProperty(DataSetConnectorCD.PROP_INDEX, PropertyValue.createComponentReference(index));
                            }
                        } else {
                            connector.writeProperty(DataSetConnectorCD.PROP_INDEX, PropertyValue.createNull());
                        }

                        connector.writeProperty(DataSetConnectorCD.PROP_COMPONENT_ID, MidpTypes.createLongValue(component.getComponentID()));
                        connector.writeProperty(DataSetConnectorCD.PROP_EXPRESSION_READ, MidpTypes.createStringValue(jTextFieldExpressionRead.getText().trim()));
                        connector.writeProperty(DataSetConnectorCD.PROP_EXPRESSION_WRITE, MidpTypes.createStringValue(jTextFieldExpressionWrite.getText().trim()));

                        saveCommands(document, connector, selectedUpdateCommand, DataSetConnectorCD.PROP_UPDATE_COMMAND);
                        saveCommands(document, connector, selectedNextCommand, DataSetConnectorCD.PROP_NEXT_COMMAND);
                        saveCommands(document, connector, selectedPreviousCommand, DataSetConnectorCD.PROP_PREVIOUS_COMMAND);
                        MidpDatabindingSupport.removerUnusedIndexes(document);
                        component.resetToDefault(propertyEditor.getPropertyNames().get(0));
                        break;
                    }
                }

            }
        });
    }

    private void saveCommands(DesignDocument document, DesignComponent connector, String commandName, String propertyName) {
        assert document != null || connector != null || propertyName != null;
        Collection<DesignComponent> commands = MidpDocumentSupport.getCategoryComponent(document, CommandsCategoryCD.TYPEID).getComponents();
        for (DesignComponent command : commands) {
            PropertyValue value = command.readProperty(ClassCD.PROP_INSTANCE_NAME);
            if (value != PropertyValue.createNull() && value.getPrimitiveValue().equals(commandName)) {
                connector.writeProperty(propertyName, PropertyValue.createComponentReference(command));
                break;
            }
        }
    }

    public void resetValuesInModel(final DesignComponent component) {
        component.getDocument().getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                MidpDatabindingSupport.removeUnusedConnector(component, propertyEditor.getPropertyNames().get(0));
            }
        });
        jComboBoxCommandUpdate.setSelectedItem(null);
        jComboBoxDatasets.setSelectedItem(null);
        jComboBoxCommandsIndexablePrevious.setSelectedItem(null);
        jComboBoxIndexableNext.setSelectedItem(null);
        jTextFieldExpressionRead.setText(null);
        jTextFieldExpressionRead.setEnabled(false);
        jTextFieldExpressionWrite.setText(null);
        jTextFieldExpressionWrite.setEnabled(false);
        jCheckBox1.setSelected(true);
        jCheckBox1.setEnabled(false);
        jLabelReadOnly.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/propertyeditors/Bundle").getString("LBL_WARNING_Not_defined"));
        jComboBoxIndexNames.setSelectedItem(null);
        updateWarning();
        updateIndexableUIComponents();
    }

    private static String createDataSetName(String name, DesignComponent c) {
        DescriptorRegistry registry = c.getDocument().getDescriptorRegistry();
        if (registry.isInHierarchy(IndexableDataAbstractSetCD.TYPEID, c.getType())) {
            name = name + " " + INDEXABLE; //NOI18N
        } else if (registry.isInHierarchy(DataSetAbstractCD.TYPEID, c.getType())) {
            name = name + " " + DATASET; //NOI18N
        }
        return name;
    }

    private static String cleanUpDataSetName(String name) {
        if (name != null) {
            name = name.replace(INDEXABLE, "").replace(DATASET, "").trim(); //NOI18N
        }
        return name;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBoxCommandUpdate;
    private javax.swing.JComboBox jComboBoxCommandsIndexablePrevious;
    private javax.swing.JComboBox jComboBoxDatasets;
    private javax.swing.JComboBox jComboBoxIndexNames;
    private javax.swing.JComboBox jComboBoxIndexableNext;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelReadOnly;
    private javax.swing.JLabel jLabelWarning;
    private javax.swing.JLabel jLabelWarning1;
    private javax.swing.JLabel jLabelWarning2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldExpressionRead;
    private javax.swing.JTextField jTextFieldExpressionWrite;
    // End of variables declaration//GEN-END:variables

    private class Model implements ComboBoxModel {

        private final List<String> names;
        private String selectedItem;
        private TypeID categoryType;
        private ListDataListener lisener;
        private Map<String, Boolean> dataSetWriteCapability;

        Model(DesignComponent component) {
            Collection<DesignComponent> connectors = MidpDatabindingSupport.getAllConnectors(component.getDocument());
            names = new ArrayList<String>();
            for (DesignComponent connector : connectors) {
                DesignComponent index = connector.readProperty(DataSetConnectorCD.PROP_INDEX).getComponent();
                String indexName = MidpDatabindingSupport.getIndexName(connector);
                if (index != null && !names.contains(indexName)) {
                    names.add(indexName);
                }

            }
            names.add(NOT_DEFINED);
            names.add(CREATE_INDEX); //NOI18N
        }

        Model(List<String> items) {
            names = items;
        }

        Model(DesignComponent component, TypeID categoryType) {
            this.categoryType = categoryType;
            this.names = new ArrayList<String>();
            this.names.add(NOT_DEFINED);
            Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(component.getDocument(), categoryType).getComponents();
            for (DesignComponent component_ : components) {
                String name = (String) component_.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
                if (name != null && !name.trim().equals("")) { //NOI18N
                    DescriptorRegistry registry = component_.getDocument().getDescriptorRegistry();
                    name = createDataSetName(name, component_);
                    if (registry.isInHierarchy(IndexableDataAbstractSetCD.TYPEID, component_.getType())) {
                        if (dataSetWriteCapability == null) {
                            dataSetWriteCapability = new HashMap<String, Boolean>();
                        }
                        PropertyValue value = component_.readProperty(DataSetAbstractCD.PROP_READ_ONLY);
                        if (value != PropertyValue.createNull() && value.getPrimitiveValue().equals(Boolean.TRUE)) {
                            dataSetWriteCapability.put(name, Boolean.TRUE);
                        } else {
                            dataSetWriteCapability.put(name, Boolean.FALSE);
                        }
                    }

                    if (categoryType == CommandsCategoryCD.TYPEID) {
                        Collection<String> activeCommands = new HashSet<String>();
                        Collection<DesignComponent> eventSources = DocumentSupport.gatherAllComponentsOfTypeID(component.getDocument(), CommandEventSourceCD.TYPEID);
                        for (DesignComponent event : eventSources) {
                            if (event.readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent() == component_) {
                                activeCommands.add((String) component_.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue());
                            }
                        }
                        names.addAll(activeCommands);
                    } else {
                        names.add(name);
                    }
                }
            }
        }

        public void setSelectedItem(final Object item) {
            if (item == null) {
                this.selectedItem = NOT_DEFINED;
            } else if (item.equals(CREATE_INDEX)) {
                this.lisener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
            } else {
                this.selectedItem = (String) item;
            }
        }

        public Boolean isSelectedDataSetReadOnly() {
            if (dataSetWriteCapability == null) {
                return null;
            }
            Boolean readOnly = dataSetWriteCapability.get(selectedItem);
            if (readOnly != null) {
                return readOnly;
            }
            return null;

        }

        public Object getSelectedItem() {
            return this.selectedItem;

        }

        public int getSize() {
            return names.size();
        }

        public Object getElementAt(int index) {
            return names.get(index);
        }

        public void addListDataListener(ListDataListener listener) {
            if (listener instanceof Listener) {
                this.lisener = listener;
            }
        }

        public void removeListDataListener(ListDataListener listener) {
            this.lisener = null;
        }

        public List<String> getItems() {
            return names;
        }
    }

    private class ComponentFocusAdapter extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e) {
            radioButton.setSelected(true);
        }
    }

    private class Listener implements ListDataListener {

        public void intervalAdded(ListDataEvent e) {
        }

        public void intervalRemoved(ListDataEvent e) {
        }

        public void contentsChanged(ListDataEvent e) {
            //String nameDataSet = cleanUpDataSetName((String) jComboBoxDatasets.getSelectedItem());
            //nameDataSet = nameDataSet.substring(0,1).toUpperCase() + nameDataSet.substring(1);
            String name = "index";//NOI18N + nameDataSet;
            List<String> names = ((Model) jComboBoxIndexNames.getModel()).getItems();
            names.remove(CREATE_INDEX);
            names.remove(NOT_DEFINED);
            int i = 0;
            while (names.contains(name)) {
                name = name + i++;
            }
            names.add(name);
            names.add(NOT_DEFINED);
            names.add(CREATE_INDEX);
            Model indexNamesModel = new Model(names);
            jComboBoxIndexNames.setModel(indexNamesModel);
            indexNamesModel.addListDataListener(new Listener());
            jComboBoxIndexNames.setSelectedItem(name);

        }
    }

    private class UpdateUIListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            updateDataSetRelatedUI();
            updateWarning();
            updateIndexableUIComponents();

        }
    }

    private class TextFieldFocusListener extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            jTextFieldExpressionRead.setText(jTextFieldExpressionRead.getText().trim());
            jTextFieldExpressionWrite.setText(jTextFieldExpressionWrite.getText().trim());
        }

        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);
            jTextFieldExpressionRead.setText(jTextFieldExpressionRead.getText().trim());
            jTextFieldExpressionWrite.setText(jTextFieldExpressionWrite.getText().trim());
        }
    }
}
    
    

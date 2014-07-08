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

package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class J2MEAttributesPanel extends javax.swing.JPanel {
    
    private final JTable table;
    private final StorableTableModel tableModel;
    private final Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> configs;

    private final J2MEProjectProperties uiProperties;
    private final ListSelectionListener listSelectionListener;
    private final ActionListener actionListener;

    /**
     * Creates new form J2MEAttributesPanel
     */
    public J2MEAttributesPanel(J2MEProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        initComponents();
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(J2MEAttributesPanel.class, "ACSN_Jad"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MEAttributesPanel.class, "ACSD_Jad"));
        tableModel = this.uiProperties.ATTRIBUTES_TABLE_MODEL;
        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                final int row = table.getSelectedRow();
                final boolean enabled = table.isEnabled() && row >= 0;
                bEdit.setEnabled(enabled);
                bRemove.setEnabled(enabled && !tableModel.containsInMandatory((String) tableModel.getValueAt(row, 1)));
            }
        };
        table.getSelectionModel().addListSelectionListener(listSelectionListener);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("synthetic-access")
            @Override
			public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2  &&  e.getButton() == MouseEvent.BUTTON1)
                    bEditActionPerformed(null);
            }
        });
        actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jTextFieldURL.setEditable(jCheckBoxOverride.isSelected());
            }
        };
        jCheckBoxOverride.addActionListener(actionListener);

        configs = uiProperties.RUN_CONFIGS;
        configChanged(uiProperties.activeConfig);
        configCombo.setRenderer(new ConfigListCellRenderer());
        configCombo.setModel(uiProperties.CONFIGS_MODEL);
        postInitComponents();
    }
    
    private void postInitComponents() {
        uiProperties.DEPLOYMENT_OVERRIDE_JARURL_MODEL.setMnemonic(jCheckBoxOverride.getMnemonic());
        jCheckBoxOverride.setModel(uiProperties.DEPLOYMENT_OVERRIDE_JARURL_MODEL);
        jTextFieldURL.setDocument(uiProperties.DEPLOYMENT_JARURL_MODEL);
        String liblet = uiProperties.getProject().evaluator().getProperty(J2MEProjectProperties.MANIFEST_IS_LIBLET);
        jRadioButtonSuite.setSelected(liblet == null || (liblet != null && liblet.equals(jRadioButtonSuite.getActionCommand())));
        jRadioButtonLIBlet.setSelected(liblet != null && liblet.equals(jRadioButtonLIBlet.getActionCommand()));        
        tableModel.initManifestModel(jRadioButtonLIBlet.isSelected());
        tableModel.setDataDelegates(loadPropertyValues());
        table.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background")); //NOI18N
        tableModel.switchManifestModel(jRadioButtonLIBlet.isSelected());
        listSelectionListener.valueChanged(null);
        actionListener.actionPerformed(null);
    }

    private HashMap<String, String[]> loadPropertyValues() {
        String[] propertyNames = uiProperties.ATTRIBUTES_PROPERTY_NAMES;

        HashMap<String, String[]> values2configs = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : uiProperties.RUN_CONFIGS.entrySet()) {
            String configName = entry.getKey();
            Map<String, String> configValues = entry.getValue();
            if (configValues == null) {
                continue;
            }
            String values[] = new String[propertyNames.length];
            values2configs.put(configName, values);
            for (int i = 0; i < values.length; i++) {
                values[i] = configValues.get(propertyNames[i]);
                if (i == 0 && configName != null && (values[i] == null || values[i].isEmpty())) {
                    // same in this config as in default config
                    // default values only for properties that are both in manifest and JAD, therefore i == 0
                    values[i] = values2configs.get(null)[i];
                }
            }
        }
        return values2configs;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupModel = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabelModel = new javax.swing.JLabel();
        jRadioButtonSuite = new javax.swing.JRadioButton();
        jRadioButtonLIBlet = new javax.swing.JRadioButton();
        scrollPane = new javax.swing.JScrollPane();
        bRemove = new javax.swing.JButton();
        bEdit = new javax.swing.JButton();
        lTable = new javax.swing.JLabel();
        bAdd = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabelExpl = new javax.swing.JLabel();
        jTextFieldURL = new javax.swing.JTextField();
        jCheckBoxOverride = new javax.swing.JCheckBox();
        configPanel = new javax.swing.JPanel();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        configNew = new javax.swing.JButton();
        configDel = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelModel, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.jLabelModel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabelModel, gridBagConstraints);

        buttonGroupModel.add(jRadioButtonSuite);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonSuite, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.jRadioButtonSuite.text")); // NOI18N
        jRadioButtonSuite.setActionCommand(org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.jRadioButtonSuite.actionCommand")); // NOI18N
        jRadioButtonSuite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSuiteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jRadioButtonSuite, gridBagConstraints);

        buttonGroupModel.add(jRadioButtonLIBlet);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonLIBlet, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.jRadioButtonLIBlet.text")); // NOI18N
        jRadioButtonLIBlet.setActionCommand(org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.jRadioButtonLIBlet.actionCommand")); // NOI18N
        jRadioButtonLIBlet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLIBletActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jRadioButtonLIBlet, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 0);
        add(scrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bRemove, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.bRemove.text")); // NOI18N
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 8);
        add(bRemove, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bEdit, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.bEdit.text")); // NOI18N
        bEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 8);
        add(bEdit, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lTable, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.lTable.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 8, 0, 4);
        add(lTable, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bAdd, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.bAdd.text")); // NOI18N
        bAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 11, 8);
        add(bAdd, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelExpl, NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.jLabelExpl.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(jLabelExpl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        jPanel2.add(jTextFieldURL, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxOverride, NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.jCheckBoxOverride.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        jPanel2.add(jCheckBoxOverride, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
        add(jPanel2, gridBagConstraints);

        configPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.configLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        configPanel.add(configLabel, gridBagConstraints);

        configCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default>" }));
        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.configNew.text")); // NOI18N
        configNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configNew, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(J2MEAttributesPanel.class, "J2MEAttributesPanel.configDel.text")); // NOI18N
        configDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configDel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 8);
        add(configPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(jSeparator2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void bRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        final int i = table.getSelectedRow();
        if (i < 0)
        return;
        tableModel.removeRow(i);
        final int max = tableModel.getRowCount();
        if (max <= 0)
        table.getSelectionModel().clearSelection();
        else if (i < max)
        table.getSelectionModel().setSelectionInterval(i, i);
        else
        table.getSelectionModel().setSelectionInterval(max - 1, max - 1);
    }//GEN-LAST:event_bRemoveActionPerformed

    private void bEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEditActionPerformed
        final int row = table.getSelectedRow();
        if (row < 0)
        return;
        final String key = (String) tableModel.getValueAt(row, 1);
        final String value = (String) tableModel.getValueAt(row, 2);
        final AddAttributePanel add = new AddAttributePanel();
        add.init(true, tableModel, tableModel.getKeys(), key, value);
        final DialogDescriptor dd = new DialogDescriptor(
            add, NbBundle.getMessage(J2MEAttributesPanel.class, "TITLE_EditAttribute"), //NOI18N
            true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("org.netbeans.modules.j2me.project.ui.customizer.J2MEAttributesPanel"),
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                        if (key == null  ||  ! key.equals(add.getKey())) {
                            if (key != null)
                            tableModel.removeKey(key);
                            int newrow = tableModel.addRow(add.getKey(), add.getValue(), add.getPlacement());
                            table.getSelectionModel().setSelectionInterval(newrow, newrow);
                        } else {
                            tableModel.editRow(add.getKey(), add.getValue(), add.getPlacement());
                            table.getSelectionModel().setSelectionInterval(row, row);
                        }
                    }
                }
            }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bEditActionPerformed

    private void jRadioButtonSuiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSuiteActionPerformed
        tableModel.switchManifestModel(jRadioButtonLIBlet.isSelected());
        uiProperties.putAdditionalProperty(J2MEProjectProperties.MANIFEST_IS_LIBLET, Boolean.toString(jRadioButtonLIBlet.isSelected()));
    }//GEN-LAST:event_jRadioButtonSuiteActionPerformed

    private void jRadioButtonLIBletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLIBletActionPerformed
        tableModel.switchManifestModel(jRadioButtonLIBlet.isSelected());
        uiProperties.putAdditionalProperty(J2MEProjectProperties.MANIFEST_IS_LIBLET, Boolean.toString(jRadioButtonLIBlet.isSelected()));
    }//GEN-LAST:event_jRadioButtonLIBletActionPerformed

    private void bAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        final AddAttributePanel add = new AddAttributePanel();
        add.init(false, tableModel, tableModel.getKeys(), null, null);
        final DialogDescriptor dd = new DialogDescriptor(
            add, NbBundle.getMessage(J2MEAttributesPanel.class, "TITLE_AddAttribute"), //NOI18N
            true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("org.netbeans.modules.j2me.project.ui.customizer.AddAttributePanel"),
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                        int row = tableModel.addRow(add.getKey(), add.getValue(), add.getPlacement());
                        table.getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bAddActionPerformed

    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        String config = (String) configCombo.getSelectedItem();
        if (config.length() == 0) {
            config = null;
        }
        configChanged(config);
        uiProperties.activeConfig = config;
    }//GEN-LAST:event_configComboActionPerformed

    private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
        createNewConfiguration();
    }//GEN-LAST:event_configNewActionPerformed

    private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
        String config = (String) configCombo.getSelectedItem();
        assert config != null;
        configs.put(config, null);
        configChanged(null);
        uiProperties.activeConfig = null;
    }//GEN-LAST:event_configDelActionPerformed

    private void createNewConfiguration() {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(J2MEAttributesPanel.class, "J2MERunPanel.input.prompt"), // NOI18N
                NbBundle.getMessage(J2MEAttributesPanel.class, "J2MERunPanel.input.title")); // NOI18N
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String name = d.getInputText();
        String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
        if (config.trim().length() == 0) {
            //#143764
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2MEAttributesPanel.class, "J2MERunPanel.input.empty", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;

        }
        if (configs.get(config) != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2MEAttributesPanel.class, "J2MERunPanel.input.duplicate", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        Map<String, String> m = new HashMap<>();
        if (!name.equals(config)) {
            m.put("$label", name); // NOI18N
        }
        configs.put(config, m);
        configChanged(config);
        uiProperties.activeConfig = config;
    }

    private void configChanged(String activeConfig) {
        uiProperties.CONFIGS_MODEL = new DefaultComboBoxModel();
        uiProperties.CONFIGS_MODEL.addElement("");
        SortedSet<String> alphaConfigs = new TreeSet<>(new Comparator<String>() {
            Collator coll = Collator.getInstance();

            @Override
            public int compare(String s1, String s2) {
                return coll.compare(label(s1), label(s2));
            }

            private String label(String c) {
                Map<String, String> m = configs.get(c);
                String label = m.get("$label"); // NOI18N
                return label != null ? label : c;
            }
        });
        for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config != null && entry.getValue() != null) {
                alphaConfigs.add(config);
            }
        }
        for (String c : alphaConfigs) {
            uiProperties.CONFIGS_MODEL.addElement(c);
        }
        configCombo.setModel(uiProperties.CONFIGS_MODEL);
        configCombo.setSelectedItem(activeConfig != null ? activeConfig : "");
        Map<String, String> m = configs.get(activeConfig);
        if (m != null) {
            uiProperties.storeAttributesToRunConfigs();
            tableModel.CONFIG = activeConfig;
            tableModel.setDataDelegates(loadPropertyValues());
        }
        configDel.setEnabled(activeConfig != null);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bEdit;
    private javax.swing.JButton bRemove;
    private javax.swing.ButtonGroup buttonGroupModel;
    private javax.swing.JComboBox configCombo;
    private javax.swing.JButton configDel;
    private javax.swing.JLabel configLabel;
    private javax.swing.JButton configNew;
    private javax.swing.JPanel configPanel;
    private javax.swing.JCheckBox jCheckBoxOverride;
    private javax.swing.JLabel jLabelExpl;
    private javax.swing.JLabel jLabelModel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButtonLIBlet;
    private javax.swing.JRadioButton jRadioButtonSuite;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextFieldURL;
    private javax.swing.JLabel lTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    static class StorableTableModel extends AbstractTableModel {
        public String CONFIG;
        private final HashMap<String, HashMap<String,String>> othersMap, jadMap, manifestMap;
        final private ArrayList<String> items = new ArrayList<>();
        private boolean isLIBlet;

        private String[] additionalAttributes = null;

        final private static String NAME="MIDlet-Name"; //NOI18N
        final private static String VENDOR="MIDlet-Vendor"; //NOI18N
        final private static String VERSION="MIDlet-Version"; //NOI18N
        final private static String ICON="MIDlet-Icon"; //NOI18N
        final private static String DESCRIPTION="MIDlet-Description"; //NOI18N
        final private static String INFOURL="MIDlet-Info-URL"; //NOI18N
        final private static String DATASIZE="MIDlet-Data-Size"; //NOI18N
        final private static String MECONFIG="MicroEdition-Configuration"; //NOI18N
        final private static String MEPROFILE="MicroEdition-Profile"; //NOI18N
        final private static String INSTALL="MIDlet-Install-Notify"; //NOI18N
        final private static String DELETE="MIDlet-Delete-Notify"; //NOI18N
        final private static String CONFIRM="MIDlet-Delete-Confirm"; //NOI18N

        final private static String FONT="MIDlet-Font"; //NOI18N
        final private static String MINCANVAS="MIDlet-Minimum-Canvas-Size"; //NOI18N
        final private static String MAXCANVAS="MIDlet-Maximum-Canvas-Size"; //NOI18N
        final private static String SPLASH="MIDlet-Splash-Screen-Image"; //NOI18N
        final private static String USERDENIED="MIDlet-UserDenied"; //NOI18N
        final private static String UPDATEURL="MIDlet-Update-URL"; //NOI18N
        final private static String IPVERSION="MIDlet-Required-IP-Version"; //NOI18N
        final private static String SCICON="MIDlet-Scalable-Icon"; //NOI18N

        final private static String LNAME="LIBlet-Name"; //NOI18N
        final private static String LVENDOR="LIBlet-Vendor"; //NOI18N
        final private static String LVERSION="LIBlet-Version"; //NOI18N

        final private static String LDESCRIPTION="LIBlet-Description"; //NOI18N
        final private static String LNONSHAREDDATA="LIBlet-NonShared-Data-Size"; //NOI18N
        final private static String LSHAREDDATA="LIBlet-Shared-Data-Size"; //NOI18N
        final private static String LINSTALL="LIBlet-Install-Notify"; //NOI18N
        final private static String LICON="LIBlet-Icon"; //NOI18N
        final private static String LDELETE="LIBlet-Delete-Notify"; //NOI18N
        final private static String LFONT="LIBlet-Font"; //NOI18N

        private static final String[][] mandatoryProperties = {
            new String[] {
                LNAME, LVENDOR, LVERSION
            },
            new String[] {
                NAME, VENDOR, VERSION
            }
        };
        
        private static final String[][] nonmandatoryProperties = {
            new String [] {
                LDESCRIPTION, LNONSHAREDDATA, LSHAREDDATA,
                LINSTALL, LICON, LDELETE, LFONT,
            },
            new String[] {
                ICON, DESCRIPTION, INFOURL, 
                DATASIZE, 
                INSTALL, DELETE, CONFIRM, 
                MECONFIG, MEPROFILE, 
            },
            new String[] {
                ICON, DESCRIPTION, INFOURL, 
                DATASIZE, 
                INSTALL, DELETE, CONFIRM, 
                MECONFIG, MEPROFILE, 
            },
            new String[] {
                ICON, DESCRIPTION, INFOURL,
                DATASIZE,
                INSTALL, DELETE, CONFIRM,
                FONT, MINCANVAS, MAXCANVAS, SPLASH,
                USERDENIED, UPDATEURL, IPVERSION, SCICON,
                MECONFIG, MEPROFILE,
            },
        };
        
        private static final String[] riskyProperties = { MECONFIG, MEPROFILE, };
        
        private static final long serialVersionUID = -2195421895353167160L;

        private final J2MEProjectProperties uiProperties;
        private boolean dataDelegatesWereSet = false;

        public StorableTableModel(J2MEProjectProperties uiProperties) {
            this.uiProperties = uiProperties;
            this.othersMap = new HashMap<>();
            this.jadMap = new HashMap<>();
            this.manifestMap = new HashMap<>();
            CONFIG = uiProperties.activeConfig;
        }

        public void initManifestModel(boolean isLIBlet) {
            this.isLIBlet = isLIBlet;
        }

        public HashSet<String> getKeys() {
            return new HashSet<>(items);
        }
        
        @Override
        public int getRowCount() {
            return items.size();
        }
        
        @Override
        public int getColumnCount() {
            return 3;
        }
        
        @Override
        public boolean isCellEditable(@SuppressWarnings("unused")
		final int rowIndex, @SuppressWarnings("unused")
		final int columnIndex) {
            return false;
        }
        
        @Override
        public String getColumnName(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return NbBundle.getMessage(J2MEAttributesPanel.class, "LBL_Jad_Column_Type"); //NOI18N
                case 1:
                    return NbBundle.getMessage(J2MEAttributesPanel.class, "LBL_Jad_Column_Key"); //NOI18N
                case 2:
                    return NbBundle.getMessage(J2MEAttributesPanel.class, "LBL_Jad_Column_Value"); //NOI18N
                default:
                    return null;
            }
        }

        public void switchManifestModel(boolean toLIBlet) {
            isLIBlet = toLIBlet;
            uiProperties.LIBLET_PACKAGING = isLIBlet;
            final String FROM = toLIBlet ? "MIDlet-" : "LIBlet-"; //NOI18N
            final String TO = toLIBlet ? "LIBlet-" : "MIDlet-"; //NOI18N
            for (int i = 0; i < items.size(); i++) {
                String key = items.get(i);
                if (key.startsWith(FROM)) {
                    String newKey = TO + key.substring(FROM.length());
                    if (contains(getMandatory(), newKey) || contains(getNonMandatory(), newKey)) {
                        items.set(i, newKey);
                        fireTableRowsUpdated(i, i);
                    }
                }
            }
            for (String configName : manifestMap.keySet()) {
                Set<String> allKeys = new HashSet<>(manifestMap.get(configName).keySet());
                allKeys.addAll(othersMap.get(configName).keySet());
                allKeys.addAll(jadMap.get(configName).keySet());
                for (String key : allKeys) {
                    if (key.startsWith(FROM)) {
                        String newKey = TO + key.substring(FROM.length());
                        if (contains(getMandatory(), newKey) || contains(getNonMandatory(), newKey)) {
                            String v = othersMap.get(configName).remove(key);
                            if (v != null) {
                                othersMap.get(configName).put(newKey, v);
                            }
                            v = jadMap.get(configName).remove(key);
                            if (v != null) {
                                jadMap.get(configName).put(newKey, v);
                            }
                            v = manifestMap.get(configName).remove(key);
                            if (v != null) {
                                manifestMap.get(configName).put(newKey, v);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                default:
                    return null;
            }
        }
        
        public synchronized HashMap[] getDataDelegates() {
            if (!dataDelegatesWereSet) {
                String[] propertyNames = uiProperties.ATTRIBUTES_PROPERTY_NAMES;
                HashMap<String, String[]> values2configs = new HashMap<>();
                for (Map.Entry<String, Map<String, String>> entry : uiProperties.RUN_CONFIGS.entrySet()) {
                    String configName = entry.getKey();
                    Map<String, String> configValues = entry.getValue();
                    String values[] = new String[propertyNames.length];
                    values2configs.put(configName, values);
                    for (int i = 0; i < values.length; i++) {
                        values[i] = configValues.get(propertyNames[i]);
                        if (configName != null && (values[i] == null || values[i].isEmpty())) {
                            // same in this config as in default config
                            values[i] = values2configs.get(null)[i];
                        }
                    }
                }
                setDataDelegates(values2configs);
            }
            updateMapsFromItems();
            return new HashMap[]{othersMap, jadMap, manifestMap};
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            assert rowIndex < items.size();
            switch (columnIndex) {
                case 0: {
                    final String item = items.get(rowIndex);
                    String value;
                    if (containsInMandatory(item))
                        value = NbBundle.getMessage(J2MEAttributesPanel.class, "LBL_CustomJad_Required"); //NOI18N
                    else if (containsInRisky(item))
                        value = NbBundle.getMessage(J2MEAttributesPanel.class, "LBL_CustomJad_Risky"); //NOI18N
                    else if (containsInNonMandatory(item))
                        value = NbBundle.getMessage(J2MEAttributesPanel.class, "LBL_CustomJad_Optional"); //NOI18N
                    else {
                        final Boolean b = getPlacement(item);
                        value = NbBundle.getMessage(J2MEAttributesPanel.class, "LBL_CustomJad_Custom" + (b == null ? "" : (b.booleanValue() ? "_J" : "_M"))); //NOI18N
                    }
                    return value;
                }
                case 1:
                    return items.get(rowIndex);
                case 2: {
                    String value = othersMap.get(CONFIG).get(items.get(rowIndex));
                    if (value == null) {
                        value = jadMap.get(CONFIG).get(items.get(rowIndex));
                    }
                    if (value == null) {
                        value = manifestMap.get(CONFIG).get(items.get(rowIndex));
                    }
                    if (value == null) {
                        value = ""; //NOI18N
                    }
                    return value;
                }
                default:
                    return null;
            }
        }
        
        public synchronized void setDataDelegates(final HashMap<String, String[]> values2configs) {
            assert values2configs != null;
            for (Map.Entry<String, String[]> entry : values2configs.entrySet()) {
                String configName = entry.getKey();
                String[] data = entry.getValue();
                othersMap.put(configName, data[0] == null ? new HashMap<String, String>() : (HashMap<String, String>) uiProperties.decode(data[0]));
                jadMap.put(configName, data[1] == null ? new HashMap<String, String>() : (HashMap<String, String>) uiProperties.decode(data[1]));
                manifestMap.put(configName, data[2] == null ? new HashMap<String, String>() : (HashMap<String, String>) uiProperties.decode(data[2]));
            }

            updateItemsFromMaps();
            fireTableDataChanged();
            dataDelegatesWereSet = true;
        }
        
        public synchronized void updateItemsFromMaps() {
            String liblet = uiProperties.getProject().evaluator().getProperty(J2MEProjectProperties.MANIFEST_IS_LIBLET);
            if (liblet != null && Boolean.valueOf(liblet)) {
                switchManifestModel(true);
            }
            items.clear();
            final ArrayList<String> keys = new ArrayList<>(othersMap.get(CONFIG).keySet());
            keys.addAll(jadMap.get(CONFIG).keySet());
            keys.addAll(manifestMap.get(CONFIG).keySet());
            for (String mandatory : getMandatory()) {
                items.add(mandatory);
                keys.remove(mandatory);
            }
            final String[] strKeys = keys.toArray(new String[keys.size()]);
            Arrays.sort(strKeys);
            items.addAll(Arrays.asList(strKeys));
        }
        
        public synchronized void updateMapsFromItems() {
            final HashMap<String,String> res1 = new HashMap<>(), 
            		res2 = new HashMap<>(),
            		res3 = new HashMap<>();
            for (int a = 0; a < items.size(); a ++) {
                final String key = items.get(a);
                String value = manifestMap.get(CONFIG).get(key);
                if (value != null) {
                    res3.put(key, value);
                } else {
                    value = jadMap.get(CONFIG).get(key);
                    if (value != null) {
                        res2.put(key, value);
                    } else {
                        value = othersMap.get(CONFIG).get(key);
                        res1.put(key, value == null ? "" : value); //NOI18N
                    }
                }
            }
            othersMap.put(CONFIG, res1);
            jadMap.put(CONFIG, res2);
            manifestMap.put(CONFIG, res3);
        }
        
        private static boolean contains(final String[] array, final String item) {
            for (String array1 : array) {
                if (array1.equals(item)) {
                    return true;
                }
            }
            return false;
        }
        
        public String[] getMandatory() {
            return mandatoryProperties[isLIBlet ? 0 : 1];
        }
        
        public String[] getNonMandatory() {
            if (additionalAttributes == null){
                additionalAttributes = loadAdditionalAttributes();
            }
            return mergeAttributes(nonmandatoryProperties[isLIBlet ? 0 : 1], additionalAttributes);
        }
        
        public String[] getAllAttrs() {
            return mergeAttributes(getMandatory(), getNonMandatory());//allMIDletProperties[isMIDP20 ? 1 : 0];
        }
        
        public boolean containsInMandatory(final String item) {
            return contains(getMandatory(), item);
        }
        
        public boolean containsInRisky(final String item) {
            return contains(riskyProperties, item);
        }
        
        public boolean containsInNonMandatory(final String item) {
            return contains(getNonMandatory(), item);
        }
        
        public boolean containsInAllAttrs(final String item) {
            return contains(getAllAttrs(), item);
        }
        
        public boolean isAcceptable(final String key) {
            if (key == null)
                return false;
            if (! key.startsWith("MIDlet-") && ! key.startsWith("LIBlet-")) //NOI18N
                return true;
            if (containsInAllAttrs(key))
                return true;
            return false;
        }
        
        public int addRow(final String key, final String value, final Boolean placement) {
            if (key == null || items.contains(key)) {
                return -1;
            }
            final int row = items.size();
            othersMap.get(CONFIG).remove(key);
            jadMap.get(CONFIG).remove(key);
            manifestMap.get(CONFIG).remove(key);
            if (placement == null) {
                othersMap.get(CONFIG).put(key, value);
            } else if (placement) {
                jadMap.get(CONFIG).put(key, value);
            } else {
                manifestMap.get(CONFIG).put(key, value);
            }
            items.add(key);
            fireTableRowsInserted(row, row);
            return row;
        }
        
        public void editRow(final String key, final String value, final Boolean placement) {
            final int row = items.indexOf(key);
            if (row < 0) {
                return;
            }
            othersMap.get(CONFIG).remove(key);
            jadMap.get(CONFIG).remove(key);
            manifestMap.get(CONFIG).remove(key);
            if (placement == null) {
                othersMap.get(CONFIG).put(key, value);
            } else if (placement) {
                jadMap.get(CONFIG).put(key, value);
            } else {
                manifestMap.get(CONFIG).put(key, value);
            }
            fireTableRowsUpdated(row, row);
        }
        
        public Boolean getPlacement(final String key) {
            if (jadMap.containsKey(key)) return Boolean.TRUE;
            if (manifestMap.containsKey(key)) return Boolean.FALSE;
            return null;
        }
        
        public void removeRow(final int row) {
            assert row < items.size();
            if (containsInMandatory(items.get(row)))
                return;
            items.remove(row);
            fireTableRowsDeleted(row, items.size() + 1);
        }
        
        public void removeKey(final String key) {
            final int row = items.indexOf(key);
            if (row < 0)
                return;
            removeRow(row);
        }

        private String[] loadAdditionalAttributes(){
            final List<String> attrs = new ArrayList<>();

            final FileObject xml = FileUtil.getConfigFile("Buildsystem/ApplicationDescriptor/Attributes"); // NOI18N
            if (xml == null){
                return new String[0];
            }

            FileObject[] entries = xml.getChildren();            
            for (FileObject fileObject : Arrays.asList(entries)) {
                InputStream is = null;
                try {
                    is = fileObject.getInputStream();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    try {
                        String s;
                        while ((s = reader.readLine()) != null){
                            s = s.trim();
                            if (!attrs.contains(s) && s.length() != 0){
                                attrs.add(s);
                            }
                        }
                    } finally {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return attrs.toArray(new String[attrs.size()]);
        }

        private String[] mergeAttributes(String[] a, String[] b){
            String[] s = new String[a.length + b.length];
            System.arraycopy(a, 0, s, 0, a.length);
            System.arraycopy(b, 0, s, a.length, b.length);
            return s;
        }
    }

    private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer, UIResource {

        public ConfigListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            String config = (String) value;
            String label;
            if (config == null) {
                // uninitialized?
                label = null;
            } else if (config.length() > 0) {
                Map<String, String> m = configs.get(config);
                label = m != null ? m.get("$label") : /* temporary? */ null; // NOI18N
                if (label == null) {
                    label = config;
                }
            } else {
                label = NbBundle.getBundle("org.netbeans.modules.java.j2seproject.Bundle").getString("J2SEConfigurationProvider.default.label"); // NOI18N
            }
            setText(label);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
}

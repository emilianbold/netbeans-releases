/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.editors2.JTableHeaderEditor;
import org.netbeans.modules.form.editors2.TableColumnModelEditor;
import org.netbeans.modules.form.editors2.TableModelEditor;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.util.NbBundle;

/**
 * Customizer for JTable.
 *
 * @author Jan Stola
 */
public class TableCustomizer extends JPanel implements Customizer, FormAwareEditor {
    private JTable table;
    
    /** Property editor for model from component section. */
    private RADConnectionPropertyEditor modelFromComponentEd;
    /** Property editor for custom code model section. */
    private RADConnectionPropertyEditor modelCustomEd;

    /** Customizer for model from component. */
    private Component modelFromComponentCustomizer;
    /** Customizer for custom code model. */
    private Component modelCustomCustomizer;
    /** Customizer for bound model. */
    private BindingCustomizer modelBoundCustomizer;

    /** Elements binding property. */
    private BindingProperty bindingProperty;   
    /** Model property. */
    private FormProperty modelProperty;
    /** TableHeader property. */
    private FormProperty headerProperty;
    /** ColumnModel property. */
    private FormProperty columnModelProperty;
    /** ColumnSelectionAllowed property. */
    private FormProperty columnSelectionAllowedProperty;

    /** Information about columns. */
    private List<ColumnInfo> columns;
    /** Table model for table with column information. */
    private CustomizerTableModel columnTableModel;
    /** Table model for table with row information. */
    private TableModelEditor.NbTableModel rowTableModel;

    /**
     * Creates new <code>TableCustomizer</code>.
     */
    public TableCustomizer() {
        init();
    }

    private void init() {
        initComponents();
        initColumnTypeCombo();
        initSelectionTypeCombo();
        initWidthCombos();
        modelFromComponentEd = new RADConnectionPropertyEditor(TableModel.class, RADConnectionPropertyEditor.Type.FormConnection);
        modelCustomEd = new RADConnectionPropertyEditor(TableModel.class, RADConnectionPropertyEditor.Type.CustomCode);
        columnTableModel = new CustomizerTableModel();
        columnsTable.setModel(columnTableModel);
        columnsTable.getSelectionModel().addListSelectionListener(new ColumnSelectionListener());
        rowsTable.getSelectionModel().addListSelectionListener(new RowSelectionListener());
        titleListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (lastSelectedColumn != -1) {
                    columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn); 
                }
            }
        };
    }

    /**
     * Initializes column type combo box. 
     */
    private void initColumnTypeCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Object")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_String")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Boolean")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Integer")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Byte")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Short")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Long")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Float")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Double")); // NOI18N
        columnTypeCombo.setModel(model);
        DefaultComboBoxModel editorModel = new DefaultComboBoxModel();
        for (int i=0; i<model.getSize(); i++) {
            editorModel.addElement(model.getElementAt(i));
        }
        typeCellEditor.setModel(editorModel);
    }

    /**
     * Initializes column selection type combo box. 
     */
    private void initSelectionTypeCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_None")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_Single")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_Contiguous")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_Discontiguous")); // NOI18N
        selectionModelCombo.setModel(model);
    }

    /**
     * Initializes width combo boxes.
     */
    private void initWidthCombos() {
        String defaultWidth = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Width_Default"); // NOI18N
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(defaultWidth);
        widthMinCombo.setModel(model);
        model =  new DefaultComboBoxModel();
        model.addElement(defaultWidth);
        widthPrefCombo.setModel(model);
        model =  new DefaultComboBoxModel();
        model.addElement(defaultWidth);
        widthMaxCombo.setModel(model);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        modelButtonGroup = new javax.swing.ButtonGroup();
        columnExpressionLabel = new javax.swing.JLabel();
        typeCellEditor = new javax.swing.JComboBox();
        tabbedPane = new javax.swing.JTabbedPane();
        modelTab = new javax.swing.JPanel();
        modelHardcodedChoice = new javax.swing.JRadioButton();
        modelBoundChoice = new javax.swing.JRadioButton();
        modelBoundPanel = new javax.swing.JPanel();
        modelFromComponentChoice = new javax.swing.JRadioButton();
        modelFromComponentPanel = new javax.swing.JPanel();
        modelCustomChoice = new javax.swing.JRadioButton();
        modelCustomPanel = new javax.swing.JPanel();
        columnsTab = new javax.swing.JPanel();
        columnsScrollPane = new javax.swing.JScrollPane();
        columnsTable = new javax.swing.JTable();
        columnCountLabel = new javax.swing.JLabel();
        insertColumnButton = new javax.swing.JButton();
        columnCountSpinner = new javax.swing.JSpinner();
        deleteColumnButton = new javax.swing.JButton();
        moveUpColumnButton = new javax.swing.JButton();
        moveDownColumnButton = new javax.swing.JButton();
        columnTitleLabel = new javax.swing.JLabel();
        columnTypeLabel = new javax.swing.JLabel();
        columnEditorLabel = new javax.swing.JLabel();
        columnRendererLabel = new javax.swing.JLabel();
        columnTitlePanel = new org.openide.explorer.propertysheet.PropertyPanel();
        columnTypeCombo = new javax.swing.JComboBox();
        columnEditorPanel = new org.openide.explorer.propertysheet.PropertyPanel();
        columnRendererPanel = new org.openide.explorer.propertysheet.PropertyPanel();
        separator = new javax.swing.JSeparator();
        selectionModelLabel = new javax.swing.JLabel();
        selectionModelCombo = new javax.swing.JComboBox();
        reorderingAllowedChoice = new javax.swing.JCheckBox();
        resizableColumnChoice = new javax.swing.JCheckBox();
        editableColumnChoice = new javax.swing.JCheckBox();
        widthPrefLabel = new javax.swing.JLabel();
        widthMinLabel = new javax.swing.JLabel();
        widthMaxLabel = new javax.swing.JLabel();
        widthPrefCombo = new javax.swing.JComboBox();
        widthMinCombo = new javax.swing.JComboBox();
        widthMaxCombo = new javax.swing.JComboBox();
        placeHolder1 = new javax.swing.JLabel();
        placeHolder1.setVisible(false);
        placeHolder2 = new javax.swing.JLabel();
        placeHolder2.setVisible(false);
        rowsTab = new javax.swing.JPanel();
        rowsScrollPane = new javax.swing.JScrollPane();
        rowsTable = new javax.swing.JTable();
        rowCountLabel = new javax.swing.JLabel();
        rowCountSpinner = new javax.swing.JSpinner();
        insertRowButton = new javax.swing.JButton();
        deleteRowButton = new javax.swing.JButton();
        moveUpRowButton = new javax.swing.JButton();
        moveDownRowButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        org.openide.awt.Mnemonics.setLocalizedText(columnExpressionLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Expression")); // NOI18N

        tabbedPane.addChangeListener(formListener);

        modelButtonGroup.add(modelHardcodedChoice);
        modelHardcodedChoice.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(modelHardcodedChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Hardcoded")); // NOI18N
        modelHardcodedChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelHardcodedChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelHardcodedChoice.addActionListener(formListener);

        modelButtonGroup.add(modelBoundChoice);
        org.openide.awt.Mnemonics.setLocalizedText(modelBoundChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Bound")); // NOI18N
        modelBoundChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelBoundChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelBoundChoice.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout modelBoundPanelLayout = new org.jdesktop.layout.GroupLayout(modelBoundPanel);
        modelBoundPanel.setLayout(modelBoundPanelLayout);
        modelBoundPanelLayout.setHorizontalGroup(
            modelBoundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 535, Short.MAX_VALUE)
        );
        modelBoundPanelLayout.setVerticalGroup(
            modelBoundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        modelButtonGroup.add(modelFromComponentChoice);
        org.openide.awt.Mnemonics.setLocalizedText(modelFromComponentChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_FromComponent")); // NOI18N
        modelFromComponentChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelFromComponentChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelFromComponentChoice.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout modelFromComponentPanelLayout = new org.jdesktop.layout.GroupLayout(modelFromComponentPanel);
        modelFromComponentPanel.setLayout(modelFromComponentPanelLayout);
        modelFromComponentPanelLayout.setHorizontalGroup(
            modelFromComponentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 535, Short.MAX_VALUE)
        );
        modelFromComponentPanelLayout.setVerticalGroup(
            modelFromComponentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        modelButtonGroup.add(modelCustomChoice);
        org.openide.awt.Mnemonics.setLocalizedText(modelCustomChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Custom")); // NOI18N
        modelCustomChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelCustomChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelCustomChoice.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout modelCustomPanelLayout = new org.jdesktop.layout.GroupLayout(modelCustomPanel);
        modelCustomPanel.setLayout(modelCustomPanelLayout);
        modelCustomPanelLayout.setHorizontalGroup(
            modelCustomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 535, Short.MAX_VALUE)
        );
        modelCustomPanelLayout.setVerticalGroup(
            modelCustomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout modelTabLayout = new org.jdesktop.layout.GroupLayout(modelTab);
        modelTab.setLayout(modelTabLayout);
        modelTabLayout.setHorizontalGroup(
            modelTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(modelTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(modelBoundPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(modelHardcodedChoice)
                    .add(modelBoundChoice)
                    .add(modelFromComponentChoice)
                    .add(modelCustomChoice)
                    .add(modelFromComponentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(modelCustomPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        modelTabLayout.setVerticalGroup(
            modelTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(modelHardcodedChoice)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelBoundChoice)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelBoundPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelFromComponentChoice)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelFromComponentPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelCustomChoice)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelCustomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        modelHardcodedChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Hardcoded_ACSD")); // NOI18N
        modelBoundChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Bound_ACSD")); // NOI18N
        modelFromComponentChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_FromComponent_ACSD")); // NOI18N
        modelCustomChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Custom_ACSD")); // NOI18N

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ModelTab"), modelTab); // NOI18N

        columnsScrollPane.setViewportView(columnsTable);

        columnCountLabel.setLabelFor(columnCountSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(columnCountLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Count")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(insertColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Insert")); // NOI18N
        insertColumnButton.addActionListener(formListener);

        columnCountSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        columnCountSpinner.addChangeListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(deleteColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Delete")); // NOI18N
        deleteColumnButton.setEnabled(false);
        deleteColumnButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveUp")); // NOI18N
        moveUpColumnButton.setEnabled(false);
        moveUpColumnButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveDownColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveDown")); // NOI18N
        moveDownColumnButton.setEnabled(false);
        moveDownColumnButton.addActionListener(formListener);

        columnTitleLabel.setLabelFor(columnTitlePanel);
        org.openide.awt.Mnemonics.setLocalizedText(columnTitleLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Title")); // NOI18N

        columnTypeLabel.setLabelFor(columnTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(columnTypeLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Type")); // NOI18N

        columnEditorLabel.setLabelFor(columnEditorPanel);
        org.openide.awt.Mnemonics.setLocalizedText(columnEditorLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Editor")); // NOI18N

        columnRendererLabel.setLabelFor(columnRendererPanel);
        org.openide.awt.Mnemonics.setLocalizedText(columnRendererLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Renderer")); // NOI18N

        columnTitlePanel.setEnabled(false);

        org.jdesktop.layout.GroupLayout columnTitlePanelLayout = new org.jdesktop.layout.GroupLayout(columnTitlePanel);
        columnTitlePanel.setLayout(columnTitlePanelLayout);
        columnTitlePanelLayout.setHorizontalGroup(
            columnTitlePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 230, Short.MAX_VALUE)
        );
        columnTitlePanelLayout.setVerticalGroup(
            columnTitlePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 24, Short.MAX_VALUE)
        );

        columnTypeCombo.setEnabled(false);
        columnTypeCombo.addActionListener(formListener);

        columnEditorPanel.setEnabled(false);

        org.jdesktop.layout.GroupLayout columnEditorPanelLayout = new org.jdesktop.layout.GroupLayout(columnEditorPanel);
        columnEditorPanel.setLayout(columnEditorPanelLayout);
        columnEditorPanelLayout.setHorizontalGroup(
            columnEditorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 230, Short.MAX_VALUE)
        );
        columnEditorPanelLayout.setVerticalGroup(
            columnEditorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 24, Short.MAX_VALUE)
        );

        columnRendererPanel.setEnabled(false);

        org.jdesktop.layout.GroupLayout columnRendererPanelLayout = new org.jdesktop.layout.GroupLayout(columnRendererPanel);
        columnRendererPanel.setLayout(columnRendererPanelLayout);
        columnRendererPanelLayout.setHorizontalGroup(
            columnRendererPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 230, Short.MAX_VALUE)
        );
        columnRendererPanelLayout.setVerticalGroup(
            columnRendererPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 24, Short.MAX_VALUE)
        );

        selectionModelLabel.setLabelFor(selectionModelCombo);
        org.openide.awt.Mnemonics.setLocalizedText(selectionModelLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_SelectionModel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reorderingAllowedChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ReorderingAllowed")); // NOI18N
        reorderingAllowedChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reorderingAllowedChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(resizableColumnChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Resizable")); // NOI18N
        resizableColumnChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        resizableColumnChoice.setEnabled(false);
        resizableColumnChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        resizableColumnChoice.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(editableColumnChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Editable")); // NOI18N
        editableColumnChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        editableColumnChoice.setEnabled(false);
        editableColumnChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        editableColumnChoice.addActionListener(formListener);

        widthPrefLabel.setLabelFor(widthPrefCombo);
        org.openide.awt.Mnemonics.setLocalizedText(widthPrefLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Pref")); // NOI18N

        widthMinLabel.setLabelFor(widthMinCombo);
        org.openide.awt.Mnemonics.setLocalizedText(widthMinLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Min")); // NOI18N

        widthMaxLabel.setLabelFor(widthMaxCombo);
        org.openide.awt.Mnemonics.setLocalizedText(widthMaxLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Max")); // NOI18N

        widthPrefCombo.setEditable(true);
        widthPrefCombo.setEnabled(false);

        widthMinCombo.setEditable(true);
        widthMinCombo.setEnabled(false);

        widthMaxCombo.setEditable(true);
        widthMaxCombo.setEnabled(false);

        org.jdesktop.layout.GroupLayout columnsTabLayout = new org.jdesktop.layout.GroupLayout(columnsTab);
        columnsTab.setLayout(columnsTabLayout);
        columnsTabLayout.setHorizontalGroup(
            columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(columnsTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(separator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, columnsTabLayout.createSequentialGroup()
                        .add(columnsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(insertColumnButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, columnsTabLayout.createSequentialGroup()
                                .add(columnCountLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(columnCountSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
                            .add(deleteColumnButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(moveUpColumnButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(moveDownColumnButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(columnsTabLayout.createSequentialGroup()
                        .add(selectionModelLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(selectionModelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(reorderingAllowedChoice)
                    .add(columnsTabLayout.createSequentialGroup()
                        .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(columnTypeLabel)
                            .add(columnEditorLabel)
                            .add(columnRendererLabel)
                            .add(columnTitleLabel)
                            .add(placeHolder1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(placeHolder2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(columnTitlePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(columnRendererPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(columnEditorPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(columnTypeCombo, 0, 230, Short.MAX_VALUE))
                        .add(18, 18, 18)
                        .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(columnsTabLayout.createSequentialGroup()
                                .add(resizableColumnChoice)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(editableColumnChoice))
                            .add(columnsTabLayout.createSequentialGroup()
                                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(widthPrefLabel)
                                    .add(widthMinLabel)
                                    .add(widthMaxLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(widthPrefCombo, 0, 79, Short.MAX_VALUE)
                                    .add(widthMinCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(widthMaxCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .add(45, 45, 45)))
                .addContainerGap())
        );
        columnsTabLayout.setVerticalGroup(
            columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(columnsTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(columnsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(columnsTabLayout.createSequentialGroup()
                        .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(columnCountLabel)
                            .add(columnCountSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(insertColumnButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteColumnButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveUpColumnButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveDownColumnButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(columnTitlePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(resizableColumnChoice)
                        .add(editableColumnChoice)
                        .add(columnTitleLabel)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(columnTypeLabel)
                    .add(widthPrefLabel)
                    .add(widthPrefCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(columnTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(columnEditorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(widthMinLabel)
                        .add(columnEditorLabel)
                        .add(widthMinCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(4, 4, 4)
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(columnRendererPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(widthMaxLabel)
                        .add(columnRendererLabel)
                        .add(widthMaxCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(placeHolder1)
                    .add(placeHolder2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(separator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(columnsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectionModelLabel)
                    .add(selectionModelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reorderingAllowedChoice)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        insertColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Insert_ACSD")); // NOI18N
        columnCountSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Count_ACSD")); // NOI18N
        deleteColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Delete_ACSD")); // NOI18N
        moveUpColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveUp_ACSD")); // NOI18N
        moveDownColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveDown_ACSD")); // NOI18N
        columnTitlePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Title_ACSD")); // NOI18N
        columnTypeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Type_ACSD")); // NOI18N
        columnEditorPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Editor_ACSD")); // NOI18N
        columnRendererPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Renderer_ACSD")); // NOI18N
        selectionModelCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_SelectionModel_ACSD")); // NOI18N
        reorderingAllowedChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ReorderingAllowed_ACSD")); // NOI18N
        resizableColumnChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Resizable_ACSD")); // NOI18N
        editableColumnChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Editable_ACSD")); // NOI18N
        widthPrefCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Min_ACSD")); // NOI18N
        widthMinCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Pref_ACSD")); // NOI18N
        widthMaxCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Max_ACSD")); // NOI18N

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ColumnsTab"), columnsTab); // NOI18N

        rowsScrollPane.setViewportView(rowsTable);

        rowCountLabel.setLabelFor(rowCountSpinner);
        rowCountLabel.setText(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Count")); // NOI18N

        rowCountSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        rowCountSpinner.addChangeListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(insertRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Insert")); // NOI18N
        insertRowButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(deleteRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Delete")); // NOI18N
        deleteRowButton.setEnabled(false);
        deleteRowButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveUp")); // NOI18N
        moveUpRowButton.setEnabled(false);
        moveUpRowButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveDownRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveDown")); // NOI18N
        moveDownRowButton.setEnabled(false);
        moveDownRowButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout rowsTabLayout = new org.jdesktop.layout.GroupLayout(rowsTab);
        rowsTab.setLayout(rowsTabLayout);
        rowsTabLayout.setHorizontalGroup(
            rowsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(rowsTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(rowsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rowsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rowsTabLayout.createSequentialGroup()
                        .add(rowCountLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rowCountSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                    .add(insertRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .add(deleteRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .add(moveUpRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .add(moveDownRowButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, Short.MAX_VALUE))
                .addContainerGap())
        );
        rowsTabLayout.setVerticalGroup(
            rowsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(rowsTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(rowsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rowsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                    .add(rowsTabLayout.createSequentialGroup()
                        .add(rowsTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(rowCountLabel)
                            .add(rowCountSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(insertRowButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteRowButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveUpRowButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveDownRowButton)))
                .addContainerGap())
        );

        rowCountSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Count_ACSD")); // NOI18N
        insertRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Insert_ACSD")); // NOI18N
        deleteRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Delete_ACSD")); // NOI18N
        moveUpRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveUp_ACSD")); // NOI18N
        moveDownRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveDown_ACSD")); // NOI18N

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_RowsTab"), rowsTab); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPane)
        );

        tabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ACSD")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, javax.swing.event.ChangeListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == modelHardcodedChoice) {
                TableCustomizer.this.modelHardcodedChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == modelBoundChoice) {
                TableCustomizer.this.modelBoundChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == modelFromComponentChoice) {
                TableCustomizer.this.modelFromComponentChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == modelCustomChoice) {
                TableCustomizer.this.modelCustomChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == insertColumnButton) {
                TableCustomizer.this.insertColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == deleteColumnButton) {
                TableCustomizer.this.deleteColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveUpColumnButton) {
                TableCustomizer.this.moveUpColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveDownColumnButton) {
                TableCustomizer.this.moveDownColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == columnTypeCombo) {
                TableCustomizer.this.columnTypeComboActionPerformed(evt);
            }
            else if (evt.getSource() == resizableColumnChoice) {
                TableCustomizer.this.resizableColumnChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == editableColumnChoice) {
                TableCustomizer.this.editableColumnChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == insertRowButton) {
                TableCustomizer.this.insertRowButtonActionPerformed(evt);
            }
            else if (evt.getSource() == deleteRowButton) {
                TableCustomizer.this.deleteRowButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveUpRowButton) {
                TableCustomizer.this.moveUpRowButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveDownRowButton) {
                TableCustomizer.this.moveDownRowButtonActionPerformed(evt);
            }
        }

        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            if (evt.getSource() == tabbedPane) {
                TableCustomizer.this.tabbedPaneStateChanged(evt);
            }
            else if (evt.getSource() == columnCountSpinner) {
                TableCustomizer.this.columnCountSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == rowCountSpinner) {
                TableCustomizer.this.rowCountSpinnerStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void columnTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_columnTypeComboActionPerformed
        ColumnInfo info = columns.get(lastSelectedColumn);
        info.setType(columnTypeCombo.getSelectedIndex());
        columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
    }//GEN-LAST:event_columnTypeComboActionPerformed

    private void editableColumnChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editableColumnChoiceActionPerformed
        ColumnInfo info = columns.get(lastSelectedColumn);
        info.setEditable(editableColumnChoice.isSelected());
        columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
    }//GEN-LAST:event_editableColumnChoiceActionPerformed

    private void resizableColumnChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizableColumnChoiceActionPerformed
        ColumnInfo info = columns.get(lastSelectedColumn);
        info.getColumn().setResizable(resizableColumnChoice.isSelected());
        columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
        updateWidthCombos();
    }//GEN-LAST:event_resizableColumnChoiceActionPerformed

    private void moveDownRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownRowButtonActionPerformed
        int index = rowsTable.getSelectedRow();
        rowTableModel.moveRow(index, index+1);
        rowsTable.getSelectionModel().setSelectionInterval(index+1, index+1);
    }//GEN-LAST:event_moveDownRowButtonActionPerformed

    private void moveUpRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpRowButtonActionPerformed
        int index = rowsTable.getSelectedRow();
        rowTableModel.moveRow(index, index-1);
        rowsTable.getSelectionModel().setSelectionInterval(index-1, index-1);
    }//GEN-LAST:event_moveUpRowButtonActionPerformed

    private void deleteRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteRowButtonActionPerformed
        int[] index = rowsTable.getSelectedRows();
        for (int i=index.length-1; i>=0; i--) {
            rowTableModel.removeRow(index[i]);
        }
        rowCountSpinner.setValue(rowTableModel.getRowCount());
    }//GEN-LAST:event_deleteRowButtonActionPerformed

    private void insertRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertRowButtonActionPerformed
        rowTableModel.addRow(rowTableModel.getRowCount());
        rowCountSpinner.setValue(rowTableModel.getRowCount());
    }//GEN-LAST:event_insertRowButtonActionPerformed

    private void rowCountSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rowCountSpinnerStateChanged
        int rowNo = ((Integer)rowCountSpinner.getValue()).intValue();
        ensureRowCount(rowNo);
    }//GEN-LAST:event_rowCountSpinnerStateChanged

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        if (tabbedPane.getSelectedIndex() == 2) {
            updateColumnSection();
        }
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void columnCountSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_columnCountSpinnerStateChanged
        int columnNo = ((Integer)columnCountSpinner.getValue()).intValue();
        ensureColumnCount(columnNo);
    }//GEN-LAST:event_columnCountSpinnerStateChanged

    private void moveDownColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownColumnButtonActionPerformed
        int index = columnsTable.getSelectedRow();
        columnsTable.clearSelection();
        ColumnInfo column = columns.remove(index);
        columns.add(index+1, column);
        if (modelHardcodedChoice.isSelected()) {
            rowTableModel.moveColumn(index, index+1);
        }
        columnsTable.getSelectionModel().setSelectionInterval(index+1, index+1);
    }//GEN-LAST:event_moveDownColumnButtonActionPerformed

    private void moveUpColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpColumnButtonActionPerformed
        int index = columnsTable.getSelectedRow();
        columnsTable.clearSelection();
        ColumnInfo column = columns.remove(index);
        columns.add(index-1, column);
        if (modelHardcodedChoice.isSelected()) {
            rowTableModel.moveColumn(index, index-1);
        }
        columnsTable.getSelectionModel().setSelectionInterval(index-1, index-1);
    }//GEN-LAST:event_moveUpColumnButtonActionPerformed

    private void deleteColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteColumnButtonActionPerformed
        boolean hardcoded = modelHardcodedChoice.isSelected();
        int[] index = columnsTable.getSelectedRows();
        for (int i=index.length-1; i>=0; i--) {
            lastSelectedColumn = -1;
            columns.remove(index[i]);
            if (hardcoded) {
                rowTableModel.removeColumn(index[i]);
            }
            columnTableModel.fireTableRowsDeleted(index[i], index[i]);
        }
        columnCountSpinner.setValue(columns.size());
    }//GEN-LAST:event_deleteColumnButtonActionPerformed

    private void insertColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertColumnButtonActionPerformed
        columns.add(new ColumnInfo(columnModelProperty));
        if (modelHardcodedChoice.isSelected()) {
            rowTableModel.addColumn(rowTableModel.getColumnCount());
        }
        int size = columns.size();
        columnTableModel.fireTableRowsInserted(size-1, size-1);
        columnCountSpinner.setValue(columns.size());
    }//GEN-LAST:event_insertColumnButtonActionPerformed

    private void modelCustomChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelCustomChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelCustomChoiceActionPerformed

    private void modelFromComponentChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelFromComponentChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelFromComponentChoiceActionPerformed

    private void modelBoundChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelBoundChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelBoundChoiceActionPerformed

    private void modelHardcodedChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelHardcodedChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelHardcodedChoiceActionPerformed

    private int lastSelectedCustomizer = -1;
    private void updateModelCustomizers() {
        boolean userCode = modelCustomChoice.isSelected();
        boolean fromComponent = modelFromComponentChoice.isSelected();
        boolean hardcoded = modelHardcodedChoice.isSelected();
        boolean bound = modelBoundChoice.isSelected();
        modelBoundCustomizer.getBindingPanel().setVisible(bound);
        modelFromComponentCustomizer.setVisible(fromComponent);
        modelCustomCustomizer.setVisible(userCode);
        tabbedPane.setEnabledAt(2, hardcoded);
        if (bound) {
            checkBindingType();
        } else {
            tabbedPane.setEnabledAt(1, true);
        }
        if (fromComponent) {
            checkModelFromComponent();
            if (columns.size() == 0) userCode = true; // Hide content of columns section
        }
        columnsScrollPane.setVisible(!userCode);
        columnCountLabel.setVisible(!userCode && !fromComponent);
        columnCountSpinner.setVisible(!userCode && !fromComponent);
        insertColumnButton.setVisible(!userCode && !fromComponent);
        deleteColumnButton.setVisible(!userCode  && !fromComponent);
        moveUpColumnButton.setVisible(!userCode && !fromComponent);
        moveDownColumnButton.setVisible(!userCode && !fromComponent);
        columnTitleLabel.setVisible(!userCode);
        columnTypeLabel.setVisible(hardcoded || bound);
        columnEditorLabel.setVisible(!userCode);
        columnRendererLabel.setVisible(!userCode);
        columnTitlePanel.setVisible(!userCode);
        columnEditorPanel.setVisible(!userCode);
        columnRendererPanel.setVisible(!userCode);
        columnTypeCombo.setVisible(hardcoded || bound);
        columnTypeCombo.setEditable(bound);
        resizableColumnChoice.setVisible(!userCode);
        editableColumnChoice.setVisible(hardcoded || bound);
        separator.setVisible(!userCode);
        widthMinLabel.setVisible(!userCode);
        widthMinCombo.setVisible(!userCode);
        widthPrefLabel.setVisible(!userCode);
        widthPrefCombo.setVisible(!userCode);
        widthMaxLabel.setVisible(!userCode);
        widthMaxCombo.setVisible(!userCode);
        expressionCombo.setVisible(bound);
        columnExpressionLabel.setVisible(bound);
        boolean switch1 = bound != (columnExpressionLabel.getParent() != null);
        boolean switch2 = fromComponent != (dummyLabel1.getParent() != null);
        if (switch1) {
            if (switch2) {
                if (bound) {
                    switchHelper2();
                    switchHelper1();
                } else {
                    switchHelper1();
                    switchHelper2();
                }
            } else {
                switchHelper1();
            }
        } else if (switch2) {
            switchHelper2();
        }
        if ((lastSelectedCustomizer != -1)
            && (((lastSelectedCustomizer != 0) && hardcoded)
                || ((lastSelectedCustomizer != 1) && bound))) {
            ensureColumnCount(0);
            ensureRowCount(0);
        }
        columnCountSpinner.setValue(columns.size());
        lastSelectedCustomizer = (hardcoded ? 0 : (bound ? 1 : (fromComponent ? 2 : 3)));
        columnTableModel.setModelType(lastSelectedCustomizer);
    }

    private void switchHelper1() {
        GroupLayout layout = (GroupLayout)columnsTab.getLayout();
        if (modelBoundChoice.isSelected()) {
            layout.replace(columnTypeLabel, columnExpressionLabel);
            layout.replace(columnTypeCombo, expressionCombo);
            layout.replace(placeHolder1, columnTypeLabel);
            layout.replace(placeHolder2, columnTypeCombo);
        } else {
            layout.replace(columnTypeCombo, placeHolder2);
            layout.replace(columnTypeLabel, placeHolder1);
            layout.replace(columnExpressionLabel, columnTypeLabel);
            layout.replace(expressionCombo, columnTypeCombo);                
        }        
    }

    private void switchHelper2() {
        GroupLayout layout = (GroupLayout)columnsTab.getLayout();
        if (modelFromComponentChoice.isSelected()) {
            layout.replace(columnRendererLabel, dummyLabel1);
            layout.replace(columnRendererPanel, dummyLabel2);
            layout.replace(columnEditorLabel, columnRendererLabel);
            layout.replace(columnEditorPanel, columnRendererPanel);
            layout.replace(columnTypeLabel, columnEditorLabel);
            layout.replace(columnTypeCombo, columnEditorPanel);
        } else {
            layout.replace(columnEditorPanel, columnTypeCombo);
            layout.replace(columnEditorLabel, columnTypeLabel);
            layout.replace(columnRendererPanel, columnEditorPanel);
            layout.replace(columnRendererLabel, columnEditorLabel);
            layout.replace(dummyLabel2, columnRendererPanel);
            layout.replace(dummyLabel1, columnRendererLabel);
        }
    }

    private void updateModel(PropertyEditor propEd) {
        try {
            Object value = propEd.getValue();
            if (value == null) {
                modelProperty.restoreDefaultValue();
            } else {
                modelProperty.setValue(new FormProperty.ValueWithEditor(value, propEd));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ComboBoxWithTree expressionCombo;
    private JLabel dummyLabel1 = new JLabel();
    private JLabel dummyLabel2 = new JLabel();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel columnCountLabel;
    private javax.swing.JSpinner columnCountSpinner;
    private javax.swing.JLabel columnEditorLabel;
    private org.openide.explorer.propertysheet.PropertyPanel columnEditorPanel;
    private javax.swing.JLabel columnExpressionLabel;
    private javax.swing.JLabel columnRendererLabel;
    private org.openide.explorer.propertysheet.PropertyPanel columnRendererPanel;
    private javax.swing.JLabel columnTitleLabel;
    private org.openide.explorer.propertysheet.PropertyPanel columnTitlePanel;
    private javax.swing.JComboBox columnTypeCombo;
    private javax.swing.JLabel columnTypeLabel;
    private javax.swing.JScrollPane columnsScrollPane;
    private javax.swing.JPanel columnsTab;
    private javax.swing.JTable columnsTable;
    private javax.swing.JButton deleteColumnButton;
    private javax.swing.JButton deleteRowButton;
    private javax.swing.JCheckBox editableColumnChoice;
    private javax.swing.JButton insertColumnButton;
    private javax.swing.JButton insertRowButton;
    private javax.swing.JRadioButton modelBoundChoice;
    private javax.swing.JPanel modelBoundPanel;
    private javax.swing.ButtonGroup modelButtonGroup;
    private javax.swing.JRadioButton modelCustomChoice;
    private javax.swing.JPanel modelCustomPanel;
    private javax.swing.JRadioButton modelFromComponentChoice;
    private javax.swing.JPanel modelFromComponentPanel;
    private javax.swing.JRadioButton modelHardcodedChoice;
    private javax.swing.JPanel modelTab;
    private javax.swing.JButton moveDownColumnButton;
    private javax.swing.JButton moveDownRowButton;
    private javax.swing.JButton moveUpColumnButton;
    private javax.swing.JButton moveUpRowButton;
    private javax.swing.JLabel placeHolder1;
    private javax.swing.JLabel placeHolder2;
    private javax.swing.JCheckBox reorderingAllowedChoice;
    private javax.swing.JCheckBox resizableColumnChoice;
    private javax.swing.JLabel rowCountLabel;
    private javax.swing.JSpinner rowCountSpinner;
    private javax.swing.JScrollPane rowsScrollPane;
    private javax.swing.JPanel rowsTab;
    private javax.swing.JTable rowsTable;
    private javax.swing.JComboBox selectionModelCombo;
    private javax.swing.JLabel selectionModelLabel;
    private javax.swing.JSeparator separator;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JComboBox typeCellEditor;
    private javax.swing.JComboBox widthMaxCombo;
    private javax.swing.JLabel widthMaxLabel;
    private javax.swing.JComboBox widthMinCombo;
    private javax.swing.JLabel widthMinLabel;
    private javax.swing.JComboBox widthPrefCombo;
    private javax.swing.JLabel widthPrefLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    updateFromUI();
                }
            });
        }
    }

    private void updateFromUI() {
        updateColumnSection();
        if (!modelBoundChoice.isSelected() && !bindingProperty.isDefaultValue()) {
            bindingProperty.restoreDefaultValue();
        }
        if (modelFromComponentChoice.isSelected()) {
            updateModel(modelFromComponentEd);
        } else if (modelCustomChoice.isSelected()) {
            updateModel(modelCustomEd);
        } else if (modelBoundChoice.isSelected()) {
            if (modelProperty.isChanged()) {
                try {
                    modelProperty.restoreDefaultValue();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            modelBoundCustomizer.getBindingFromUI();
            MetaBinding binding = modelBoundCustomizer.getBinding();
            if (binding != null) {
                if (binding.hasSubBindings()) {
                    binding.clearSubBindings();
                }
                int count = 0;
                for (ColumnInfo info : columns) {
                    String expression = info.getExpression();
                    String title = expression;
                    if (BindingDesignSupport.isSimpleExpression(title)) {
                        title = BindingDesignSupport.unwrapSimpleExpression(title);
                        title = capitalize(title);
                    }
                    FormProperty titleProp = info.getColumn().getTitle();
                    if (titleProp.isChanged()) {
                        try {
                            Object value = titleProp.getValue();
                            if ((value instanceof String) && value.equals(title)) {
                                titleProp.restoreDefaultValue();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    MetaBinding subBinding = binding.addSubBinding(expression, null);
                    subBinding.setParameter(MetaBinding.TABLE_COLUMN_PARAMETER, Integer.toString(count));
                    String clazz = info.getClazz();
                    if ((clazz != null) && (!clazz.equals("Object"))) { // NOI18N
                        subBinding.setParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER, clazz + ".class"); // NOI18N
                    }
                    if (!info.isEditable()) {
                        subBinding.setParameter(MetaBinding.EDITABLE_PARAMETER, "false"); // NOI18N
                    }
                    count++;
                }
            }
            bindingProperty.setValue(binding);
        } else if (modelHardcodedChoice.isSelected()) {
            int count = columns.size();
            for (int i=0; i<count; i++) {
                ColumnInfo info = columns.get(i);
                FormProperty prop = info.getColumn().getTitle();
                Object value = null;
                try {
                    value = prop.getValue();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (value instanceof String) {
                    try {
                        prop.restoreDefaultValue();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            try {
                modelProperty.setValue(rowTableModel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // columnModel
        TableColumnModelEditor.FormTableColumnModel model = new TableColumnModelEditor.FormTableColumnModel(columnModelProperty);
        for (ColumnInfo column : columns) {
            model.getColumns().add(column.getColumn());
        }
        int selectionModel = selectionModelCombo.getSelectedIndex();
        model.setSelectionModel(selectionModel);
        try {
            columnModelProperty.setValue(model);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            columnSelectionAllowedProperty.setValue(selectionModel != 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // tableHeader
        Object value = null;
        try {
            value = headerProperty.getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        boolean resizingAllowed = true;
        if (value instanceof JTableHeaderEditor.FormTableHeader) {
            JTableHeaderEditor.FormTableHeader header = (JTableHeaderEditor.FormTableHeader)value;
            resizingAllowed = header.isResizingAllowed();
        }
        try {
            headerProperty.setValue(new JTableHeaderEditor.FormTableHeader(headerProperty, resizingAllowed, reorderingAllowedChoice.isSelected()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void setObject(Object table) {
        assert (table instanceof JTable);
        this.table = (JTable)table;
    }

    public void setContext(FormModel formModel, FormProperty property) {
        assert (property instanceof RADProperty);
        
        // Obtain relevant properties
        RADProperty prop = (RADProperty)property;
        RADComponent comp = prop.getRADComponent();
        modelProperty = (FormProperty)comp.getPropertyByName("model"); // NOI18N        
        modelFromComponentEd.setContext(formModel, modelProperty);
        modelCustomEd.setContext(formModel, modelProperty);
        headerProperty = (FormProperty)comp.getPropertyByName("tableHeader"); // NOI18N
        columnModelProperty = (FormProperty)comp.getPropertyByName("columnModel"); // NOI18N
        columnSelectionAllowedProperty = (FormProperty)comp.getPropertyByName("columnSelectionAllowed"); // NOI18N

        // Determine type of model
        try {
            Object value = modelProperty.getValue();
            PropertyEditor propEd = modelProperty.getCurrentEditor();
            if (propEd instanceof RADConnectionPropertyEditor) {
                RADConnectionPropertyEditor.Type type = ((RADConnectionPropertyEditor)propEd).getEditorType();
                if (type == RADConnectionPropertyEditor.Type.CustomCode) {
                    modelCustomEd.setValue(value);
                    modelCustomChoice.setSelected(true);
                } else {
                    modelFromComponentEd.setValue(value);
                    modelFromComponentChoice.setSelected(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        bindingProperty = comp.getBindingProperty("elements"); // NOI18N
        MetaBinding binding = (MetaBinding)bindingProperty.getValue();
        modelBoundCustomizer = new BindingCustomizer(bindingProperty);
        modelBoundCustomizer.setBinding(binding);
        if (binding != null) {
            modelBoundChoice.setSelected(true);
        }
        expressionCombo = modelBoundCustomizer.getSubExpressionCombo();
        expressionCombo.setEnabled(false);
        expressionCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ColumnInfo info = columns.get(lastSelectedColumn);
                Object expression = expressionCombo.getSelectedItem();
                info.setExpression((expression == null) ? null : expression.toString());
                String clazz = "Object"; // NOI18N
                TreePath treePath = expressionCombo.getSelectedTreePath();
                if (treePath != null) {
                    Object pComp = treePath.getLastPathComponent();
                    if (pComp instanceof BindingCustomizer.ExpressionNode) {
                        clazz = ((BindingCustomizer.ExpressionNode)pComp).getTypeName();
                        clazz = FormUtils.autobox(clazz);
                        if (clazz.startsWith("java.lang.")) { // NOI18N
                            clazz = clazz.substring(10);
                        }
                    }
                }
                columnTypeCombo.setSelectedItem(clazz);
                columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
            }
        });
        modelBoundCustomizer.addTypeChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (modelBoundChoice.isSelected()) {
                    checkBindingType();
                }
            }            
        });
        modelFromComponentEd.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateModelCustomizers();
            }
        });

        // Replace dummy panels by customizers
        GroupLayout layout = (GroupLayout)modelTab.getLayout();
        modelFromComponentCustomizer = modelFromComponentEd.getCustomEditor();
        layout.replace(modelFromComponentPanel, modelFromComponentCustomizer);
        modelCustomCustomizer = modelCustomEd.getCustomEditor();
        layout.replace(modelCustomPanel, modelCustomCustomizer);
        layout.replace(modelBoundPanel, modelBoundCustomizer.getBindingPanel());

        columns = new LinkedList<ColumnInfo>();
        Object value = null;
        try {
            value = columnModelProperty.getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (value instanceof TableColumnModelEditor.FormTableColumnModel) {
            TableColumnModelEditor.FormTableColumnModel columnModel = (TableColumnModelEditor.FormTableColumnModel)value;
            for (TableColumnModelEditor.FormTableColumn column : columnModel.getColumns()) {
                columns.add(new ColumnInfo(column));
            }
            selectionModelCombo.setSelectedIndex(columnModel.getSelectionModel());
        } else {
            value = false;
            try {
                value = columnSelectionAllowedProperty.getRealValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (Boolean.TRUE.equals(value)) {
                selectionModelCombo.setSelectedIndex(2); // multiple interval selection
            } else {
                selectionModelCombo.setSelectedIndex(0); // not allowed
            }
        }
        
        try {
            value = headerProperty.getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        boolean reorderingAllowed = true;
        if (value instanceof JTableHeaderEditor.FormTableHeader) {
            reorderingAllowed = ((JTableHeaderEditor.FormTableHeader)value).isReorderingAllowed();            
        }
        reorderingAllowedChoice.setSelected(reorderingAllowed);
        setColumnsToUI();
        updateModelCustomizers();
    }

    /**
     * Called when bound model is selected to enable/disable
     * the columns tab. 
     */
    private void checkBindingType() {
        FormUtils.TypeHelper type = modelBoundCustomizer.getSelectedType();
        boolean collection = (type != null) && Collection.class.isAssignableFrom(FormUtils.typeToClass(type));
        tabbedPane.setEnabledAt(1, collection);
    }

    /**
     * Called when model from existing component is selected
     * to update columns tab. 
     */
    private void checkModelFromComponent() {
        try {
            Object value = modelFromComponentEd.getValue();
            if (value instanceof FormDesignValue) {
                value = ((FormDesignValue)value).getDesignValue();
            }
            if (value instanceof TableModel) {
                TableModel model = (TableModel)value;
                ensureColumnCount(model.getColumnCount());
                for (int i=0; i<model.getColumnCount(); i++) {
                    FormProperty title = columns.get(i).getColumn().getTitle();
                    if (!title.isChanged()) {
                        title.setValue(model.getColumnName(i));
                    }
                }
            } else {
                ensureColumnCount(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setColumnsToUI() {
        rowTableModel = new TableModelEditor.NbTableModel(new String[0], new Class[0], new boolean[0], 0);
        if (modelHardcodedChoice.isSelected()) {
            try {
                Object value = modelProperty.getValue();
                if (value instanceof TableModelEditor.NbTableModel) {
                    TableModelEditor.NbTableModel model = (TableModelEditor.NbTableModel)value;
                    int rowCount = model.getRowCount();
                    ensureRowCount(rowCount);
                    int columnCount = model.getColumnCount();
                    ensureColumnCount(columnCount);
                    String[] titles = new String[columnCount];
                    Class[] types = new Class[columnCount];
                    boolean[] editable = new boolean[columnCount];
                    for (int i=0; i<columnCount; i++) {
                        ColumnInfo info = columns.get(i);
                        info.setEditable(model.isColumnEditable(i));
                        FormProperty title = info.getColumn().getTitle();
                        if (!title.isChanged()) {
                            title.setValue(model.getColumnName(i));
                        }
                        info.setType(typeToIndex(model.getColumnClass(i).getName()));
                        titles[i] = model.getColumnName(i);
                        types[i] = model.getColumnClass(i);
                        editable[i] = model.isColumnEditable(i);
                    }
                    rowTableModel = new TableModelEditor.NbTableModel(titles, types, editable, rowCount);
                    for (int i=0; i<columnCount; i++) {                    
                        for (int j=0; j<rowCount; j++) {
                            rowTableModel.setValueAt(model.getValueAt(j,i),j,i);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }            
        } else if (modelBoundChoice.isSelected()) {
            MetaBinding binding = (MetaBinding)bindingProperty.getValue();
            if (binding != null) {
                TableModel model = table.getModel();
                int columnCount = 0;
                if (binding.hasSubBindings()) {
                    for (MetaBinding subBinding : binding.getSubBindings()) {
                        String tableColumn = subBinding.getParameter(MetaBinding.TABLE_COLUMN_PARAMETER);
                        if (tableColumn != null) columnCount++;
                    }
                }
                ensureColumnCount(columnCount);
                int index = 0;
                if (binding.hasSubBindings()) {
                    for (MetaBinding subBinding : binding.getSubBindings()) {
                        String tableColumn = subBinding.getParameter(MetaBinding.TABLE_COLUMN_PARAMETER);
                        if (tableColumn != null) {
                            ColumnInfo info = columns.get(index);
                            String columnClass = subBinding.getParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER);
                            if ((columnClass != null) && columnClass.trim().endsWith(".class")) { // NOI18N
                                columnClass = columnClass.trim();
                                columnClass = columnClass.substring(0, columnClass.length()-6);
                            }
                            info.setClazz(columnClass);
                            info.setExpression(subBinding.getSourcePath());
                            FormProperty title = info.getColumn().getTitle();
                            if (!title.isChanged() && (model != null) && (model.getColumnCount() > index)) {
                                try {
                                    title.setValue(model.getColumnName(index));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        String editableColumn = subBinding.getParameter(MetaBinding.EDITABLE_PARAMETER);
                        if (editableColumn != null) {
                            ColumnInfo info = columns.get(index);
                            info.setEditable(!"false".equals(editableColumn)); // NOI18N
                        }
                        index++;
                    }
                }
            }
        }
        rowsTable.setModel(rowTableModel);
        rowCountSpinner.setValue(rowTableModel.getRowCount());
    }

    /**
     * Converts column type to index of column type combo. 
     * 
     * @param type column type.
     * @return index of the given column type in column type combo.
     */
    private static int typeToIndex(String type) {
        if (type.indexOf('.') == -1) {
            type = "java.lang." + type; // NOI18N
        }
        int index = 0;
        if (Object.class.getName().equals(type)) {
            index = 0;
        } else if (String.class.getName().equals(type)) {
            index = 1;
        } else if (Boolean.class.getName().equals(type)) {
            index = 2;
        } else if (Integer.class.getName().equals(type)) {
            index = 3;
        } else if (Byte.class.getName().equals(type)) {
            index = 4;
        } else if (Short.class.getName().equals(type)) {
            index = 5;
        } else if (Long.class.getName().equals(type)) {
            index = 6;
        } else if (Float.class.getName().equals(type)) {
            index = 7;
        } else if (Double.class.getName().equals(type)) {
            index = 8;
        }
        return index;
    }

    /**
     * Returns column type that corresponds to the given index in column type combo.
     * 
     * @param index index of column type in column type combo.
     * @return column type that corresponds to the given index in column type combo.
     */
    private static Class indexToType(int index) {
        Class type;
        switch (index) {
            case 1: type = String.class; break;
            case 2: type = Boolean.class; break;
            case 3: type = Integer.class; break;
            case 4: type = Byte.class; break;
            case 5: type = Short.class; break;
            case 6: type = Long.class; break;
            case 7: type = Float.class; break;
            case 8: type = Double.class; break;
            default: type = Object.class; break;
        }
        return type;
    }

    /**
     * Ensures that there is correct number of rows in column info table 
     * and correct number of columns in row info table.
     * 
     * @param columnCount number of columns.
     */
    private void ensureColumnCount(int columnCount) {
        boolean hardcoded = modelHardcodedChoice.isSelected();
        for (int i=columns.size(); i<columnCount; i++) {
            columns.add(new ColumnInfo(columnModelProperty));
            if (hardcoded) {
                rowTableModel.addColumn(i);
            }
        }
        for (int i=columns.size()-1; i>=columnCount; i--) {
            if (lastSelectedColumn == i) {
                lastSelectedColumn = -1;
            }
            columns.remove(i);
            if (hardcoded) {
                rowTableModel.removeColumn(i);
            }
        }
        columnTableModel.fireTableDataChanged();
    }

    /**
     * Ensures that there is correct number of rows in row info table.
     */
    private void ensureRowCount(int rowCount) {
        for (int i=rowTableModel.getRowCount(); i<rowCount; i++) {
            rowTableModel.addRow(i);
        }
        for (int i=rowTableModel.getRowCount()-1; i>=rowCount; i--) {
            rowTableModel.removeRow(i);
        }
    }

    /**
     * Returns title of the specified column.
     * 
     * @param columnNo column index. 
     * @return title of the specified column.
     */
    private String getTitle(int columnNo) {
        String title = "null"; // NOI18N
        ColumnInfo info = columns.get(columnNo);
        FormProperty titleProp = info.getColumn().getTitle();
        if (titleProp.isChanged()) {
            try {
                Object value = titleProp.getRealValue();
                if (value != null) {
                    title = value.toString();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return title;
    }

    private void updateWidthCombos() {
        boolean resizable = resizableColumnChoice.isSelected();
        widthMinCombo.setEnabled(resizable);
        widthMaxCombo.setEnabled(resizable);
        if (!resizable) {
            widthMinCombo.setSelectedIndex(0);
            widthMaxCombo.setSelectedIndex(0);
        }
    }

    private int lastSelectedColumn = -1;
    private void updateColumnSection() {
        if (lastSelectedColumn != -1) {
            ColumnInfo info = columns.get(lastSelectedColumn);
            info.setEditable(editableColumnChoice.isSelected());
            info.setType(columnTypeCombo.getSelectedIndex());
            if (columnTypeCombo.getEditor().getEditorComponent().hasFocus()) {
                info.setClazz(columnTypeCombo.getEditor().getItem().toString());
            } else {
                info.setClazz(columnTypeCombo.getSelectedItem().toString());
            }
            Object expression = expressionCombo.getSelectedItem();
            info.setExpression((expression == null) ? "null" : expression.toString()); // NOI18N
            if (modelHardcodedChoice.isSelected()) {
                Class oldClass = rowTableModel.getColumnClass(lastSelectedColumn);
                Class newClass = indexToType(info.getType());
                if (newClass != oldClass) {
                    rowTableModel.setColumnClass(lastSelectedColumn, newClass);
                    for (int i=0; i<rowTableModel.getRowCount(); i++) {
                        rowTableModel.setValueAt(null, i, lastSelectedColumn);
                    }
                }
                rowTableModel.setColumnName(lastSelectedColumn, getTitle(lastSelectedColumn));
                rowTableModel.setColumnEditable(lastSelectedColumn, info.isEditable());
                rowTableModel.fireTableStructureChanged();
            }
            TableColumnModelEditor.FormTableColumn column = info.getColumn();
            column.setResizable(resizableColumnChoice.isSelected());
            int width = -1;
            try {
                width = Integer.parseInt(widthMinCombo.getSelectedItem().toString());
            } catch (NumberFormatException nfex) {}
            column.setMinWidth(width);
            width = -1;
            try {
                width = Integer.parseInt(widthPrefCombo.getSelectedItem().toString());
            } catch (NumberFormatException nfex) {}
            column.setPrefWidth(width);
            width = -1;
            try {
                width = Integer.parseInt(widthMaxCombo.getSelectedItem().toString());
            } catch (NumberFormatException nfex) {}
            column.setMaxWidth(width);
            info.getColumn().getTitle().removePropertyChangeListener(titleListener);
        }
        int[] index = columnsTable.getSelectedRows();
        boolean single = (index.length == 1);
        columnTitlePanel.setEnabled(single);
        columnEditorPanel.setEnabled(single);
        columnRendererPanel.setEnabled(single);
        columnTypeCombo.setEnabled(single);
        resizableColumnChoice.setEnabled(single);
        editableColumnChoice.setEnabled(single);
        widthMinCombo.setEnabled(single);
        widthPrefCombo.setEnabled(single);
        widthMaxCombo.setEnabled(single);
        expressionCombo.setEnabled(single);
        if (single) {
            lastSelectedColumn = index[0];
            ColumnInfo info = columns.get(index[0]);
            editableColumnChoice.setSelected(info.isEditable());
            expressionCombo.setSelectedItem(info.getExpression());
            if (modelBoundChoice.isSelected()) {
                columnTypeCombo.setSelectedItem(info.getClazz());
                if (columnTypeCombo.getEditor().getEditorComponent().hasFocus()) {
                    columnTypeCombo.getEditor().setItem(info.getClazz());
                }
            } else {
                columnTypeCombo.setSelectedIndex(info.getType());
            }
            TableColumnModelEditor.FormTableColumn selectedColumn = info.getColumn();
            columnTitlePanel.setProperty(selectedColumn.getTitle());
            columnEditorPanel.setProperty(selectedColumn.getEditor());
            columnRendererPanel.setProperty(selectedColumn.getRenderer());
            resizableColumnChoice.setSelected(selectedColumn.isResizable());
            if (selectedColumn.getMinWidth() == -1) {
                widthMinCombo.setSelectedIndex(0);
            } else {
                widthMinCombo.setSelectedItem(selectedColumn.getMinWidth());
            }
            if (selectedColumn.getPrefWidth() == -1) {
                widthPrefCombo.setSelectedIndex(0);
            } else {
                widthPrefCombo.setSelectedItem(selectedColumn.getPrefWidth());
            }
            if (selectedColumn.getMaxWidth() == -1) {
                widthMaxCombo.setSelectedIndex(0);
            } else {
                widthMaxCombo.setSelectedItem(selectedColumn.getMaxWidth());
            }
            info.getColumn().getTitle().addPropertyChangeListener(titleListener);
        } else {
            lastSelectedColumn = -1;
        }
        updateWidthCombos();
    }
    private PropertyChangeListener titleListener;

    private static String capitalize(String title) {
        StringBuilder builder = new StringBuilder(title);
        boolean lastWasUpper = false;
        for (int i = 0; i < builder.length(); i++) {
            char aChar = builder.charAt(i);
            if (i == 0) {
                builder.setCharAt(i, Character.toUpperCase(aChar));
                lastWasUpper = true;
            } else if (Character.isUpperCase(aChar)) {
                if (!lastWasUpper) {
                    builder.insert(i, ' ');
                }
                lastWasUpper = true;
                i++;
            } else {
                lastWasUpper = false;
            }
        }
        return builder.toString();
    }

    static class ColumnInfo {
        private TableColumnModelEditor.FormTableColumn column;
        private int type = 0;
        private boolean editable = true;
        private String expression = "null"; // NOI18N
        private String clazz;

        ColumnInfo(TableColumnModelEditor.FormTableColumn column) {
            this.column = column;
        }

        ColumnInfo(FormProperty columnModelProperty) {
            this(new TableColumnModelEditor.FormTableColumn(columnModelProperty));
        }

        public TableColumnModelEditor.FormTableColumn getColumn() {
            return column;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getClazz() {
            return (clazz == null) ? NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Type_Object") : clazz; // NOI18N
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }
    }

    /**
     * Selection listener for the columnsTable. 
     */
    class ColumnSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            int[] index = columnsTable.getSelectedRows();
            boolean empty = (index.length == 0);
            boolean multi = (index.length > 1);
            deleteColumnButton.setEnabled(!empty);
            moveUpColumnButton.setEnabled(!empty && !multi && (index[0] > 0));
            moveDownColumnButton.setEnabled(!empty && !multi && (index[0] < columns.size()-1));
            updateColumnSection();
        }
        
    }

    /**
     * Selection listener for the rowsTable. 
     */
    class RowSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            int[] index = rowsTable.getSelectedRows();
            boolean empty = (index.length == 0);
            boolean multi = (index.length > 1);
            deleteRowButton.setEnabled(!empty);
            moveUpRowButton.setEnabled(!empty && !multi && (index[0] > 0));
            moveDownRowButton.setEnabled(!empty && !multi && (index[0] < rowTableModel.getRowCount()-1));
        }

    }

    /**
     * Table model for the columnsTable. 
     */
    class CustomizerTableModel extends AbstractTableModel {
        private int modelType;

        public void setModelType(int modelType) {
            this.modelType = modelType;
            fireTableStructureChanged();
            if (modelType == 0) {
                columnsTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCellEditor));
            }
        }

        public int getRowCount() {
            return columns.size();
        }

        public int getColumnCount() {
            int columnCount;
            switch (modelType) {
                case 0: columnCount = 4; break;
                case 1: columnCount = 4; break;
                case 2: columnCount = 2; break;
                default: columnCount = 0;
            }
            return columnCount;
        }

        @Override
        public String getColumnName(int column) {
            String name = null;
            switch (column) {
                case 0: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Title"); break; // NOI18N
                case 1: 
                    switch (modelType) {
                        case 0: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Type"); break; // NOI18N
                        case 1: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Expression"); break; // NOI18N
                        case 2: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_ResizableH"); break; // NOI18N
                    }
                    break;
                case 2: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_ResizableH"); break; // NOI18N
                case 3: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_EditableH"); break; // NOI18N
            }
            return name;
        }

        @Override
        public Class getColumnClass(int column) {
            if ((column > 1) || ((column == 1) && (modelType == 2))) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value = null;
            ColumnInfo info = columns.get(rowIndex);
            switch (columnIndex) {
                case 0: value = getTitle(rowIndex); break;
                case 1:
                    switch (modelType) {
                        case 0: value = indexToType(info.getType()).getName().substring(10); break;
                        case 1: value = info.getExpression(); break;
                        case 2: value = info.getColumn().isResizable();
                    }
                    break;
                case 2: value = info.getColumn().isResizable(); break;
                case 3: value = info.isEditable(); break;
            }
            return value;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            ColumnInfo info = columns.get(rowIndex);
            switch (columnIndex) {
                case 0: 
                    try {
                        info.getColumn().getTitle().setValue(value);
                        columnTitlePanel.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 1:
                    switch (modelType) {
                        case 0:
                            info.setType(typeToIndex((String)value));
                            columnTypeCombo.setSelectedIndex(info.getType());
                            break;
                        case 1:
                            info.setExpression((String)value);
                            expressionCombo.setSelectedItem(value);
                            break;
                        case 2:
                            info.getColumn().setResizable((Boolean)value);
                            resizableColumnChoice.setSelected((Boolean)value);
                            break;
                    }
                    break;
                case 2:
                    info.getColumn().setResizable((Boolean)value);
                    resizableColumnChoice.setSelected((Boolean)value);
                    updateWidthCombos();
                    break;
                case 3:
                    info.setEditable((Boolean)value);
                    editableColumnChoice.setSelected((Boolean)value);
                    break;
            }
        }

    }

}

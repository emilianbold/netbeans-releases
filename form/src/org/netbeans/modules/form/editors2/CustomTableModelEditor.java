/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ResourceBundle;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.NbBundle;


/** A custom property editor for TableModel.
* @author  Jan Jancura, Ian Formanek
* @version 1.00, 06 Oct 1998
*/
public class CustomTableModelEditor extends JPanel implements EnhancedCustomPropertyEditor {

    private boolean isCreated = false;
    private boolean isChangingTableModel = false;

    private ChangeListener changeListener = new TabChangeListener();
    private ListSelectionListener selectionListener = new SelectionListener();

    private JTabbedPane tabbedPane;
    private JTable settingsTable;
    private JTable defaultValuesTable;
    private JTextField rowsField;
    private JTextField columnsField;
    private TableModelEditor.NbTableModel model; // holds default table values
    private TableModelEditor.NbTableModel titleModel; // holds metadata

    private int stSelectedRow;
    private int stSelectedColumn;
    private int defSelectedRow;
    private int defSelectedColumn;

    // buttons
    private JButton insertSColBtn;
    private JButton deleteSColBtn;
    private JButton moveSColUpBtn;
    private JButton moveSColDownBtn;

    private JButton insertColBtn;
    private JButton deleteColBtn;
    private JButton moveColLeftBtn;
    private JButton moveColRightBtn;
    private JButton insertRowBtn;
    private JButton deleteRowBtn;
    private JButton moveRowUpBtn;
    private JButton moveRowDownBtn;

    private JButton addRowBtn;
    private JButton removeRowBtn;
    private JButton addColBtn;
    private JButton removeColBtn;

    static final int SETTINGS_TAB = 0;
    static final int DEFAULT_TAB = 1;

    static final long serialVersionUID =8002510111948803668L;

    public CustomTableModelEditor(TableModelEditor editor) {
        ResourceBundle bundle = NbBundle.getBundle(CustomTableModelEditor.class);

        model = new TableModelEditor.NbTableModel((TableModel)editor.getValue());

        setLayout(new BorderLayout(0, 2));
        setBorder(new EmptyBorder(6, 6, 6, 6));
        
        tabbedPane = new JTabbedPane();

        JLabel titleLabel = new JLabel(bundle.getString("CTL_TableModelTitle"));
        titleLabel.setDisplayedMnemonic(bundle.getString("CTL_TableModelTitle_Mnemonic").charAt(0));
        titleLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_TableModelTitle"));
        titleLabel.setLabelFor(tabbedPane);
        add(titleLabel, BorderLayout.NORTH);

        // first tab (table settings)
        JPanel tab = new JPanel();
        tab.setLayout(new BorderLayout(6, 6));
        tab.setBorder(new EmptyBorder(6, 2, 0, 2));
        
        settingsTable = new CustomJTable();

        JLabel titleLabel1 = new JLabel(bundle.getString("CTL_Title1"));
        titleLabel1.setDisplayedMnemonic(bundle.getString("CTL_Title1_Mnemonic").charAt(0));
        titleLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Title1"));
        titleLabel1.setLabelFor(settingsTable);
        tab.add(titleLabel1,BorderLayout.NORTH); // NOI18N

        titleModel = new TableModelEditor.NbTableModel(
                         new String[] {
                             bundle.getString("CTL_Column"),
                             bundle.getString("CTL_Title"),
                             bundle.getString("CTL_Type"),
                             bundle.getString("CTL_Editable")
                         },
                         new Class[] {
                             String.class, String.class, String.class, Boolean.class
                         },
                         new boolean[] {
                             false, true, true, true
                         },
                         model.getColumnCount()
                     );
        settingsTable.setModel(titleModel);

        JComboBox comboBox = new JComboBox();
        comboBox.addItem("Object"); // NOI18N
        comboBox.addItem("String"); // NOI18N
        comboBox.addItem("Boolean"); // NOI18N
        comboBox.addItem("Integer"); // NOI18N
        comboBox.addItem("Byte"); // NOI18N
        comboBox.addItem("Short"); // NOI18N
        comboBox.addItem("Long"); // NOI18N
        comboBox.addItem("Float"); // NOI18N
        comboBox.addItem("Double"); // NOI18N
//        comboBox.addItem("Character"); // NOI18N
        comboBox.setMaximumRowCount(9);
        comboBox.setSelectedIndex(0);

        TableColumn typeColumn = settingsTable.getColumn(bundle.getString("CTL_Type"));
        typeColumn.setCellEditor(new DefaultCellEditor(comboBox));

        JScrollPane jscrollpane = new JScrollPane(settingsTable);
        settingsTable.setSelectionMode(0);
        settingsTable.setCellSelectionEnabled(true);
        settingsTable.setRowSelectionAllowed(true);
        settingsTable.setColumnSelectionAllowed(true);
        settingsTable.setPreferredScrollableViewportSize(new Dimension(450, 200));
        tab.add(jscrollpane, BorderLayout.CENTER);

        // panel with buttons on the right (for settings table)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 0, 0, 3));

        insertSColBtn = new JButton(bundle.getString("CTL_InsertColumn"));
        insertSColBtn.setMnemonic(bundle.getString("CTL_InsertColumn_Mnemonic").charAt(0));
        insertSColBtn.setToolTipText(bundle.getString("CTL_HINT_InsertColumn"));
        //insertSColBtn.setMargin(new Insets(0,2,0,2));
        buttonsPanel.add(insertSColBtn);
        insertSColBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertColumn();
            }
        });

        deleteSColBtn = new JButton(bundle.getString("CTL_DeleteColumn"));
        deleteSColBtn.setMnemonic(bundle.getString("CTL_DeleteColumn_Mnemonic").charAt(0));
        deleteSColBtn.setToolTipText(bundle.getString("CTL_HINT_DeleteColumn"));
        //deleteSColBtn.setMargin(new Insets(0,2,0,2));
        buttonsPanel.add(deleteSColBtn);
        deleteSColBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeColumn();
            }
        });

        moveSColUpBtn = new JButton(bundle.getString("CTL_MoveRowUp"));
        moveSColUpBtn.setMnemonic(bundle.getString("CTL_MoveRowUp_Mnemonic").charAt(0));
        moveSColUpBtn.setToolTipText(bundle.getString("CTL_HINT_MoveColumnUp"));
        //moveSColUpBtn.setMargin(new Insets(0,2,0,2));
        buttonsPanel.add(moveSColUpBtn);
        moveSColUpBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveColumnLeft();
            }
        });

        moveSColDownBtn = new JButton(bundle.getString("CTL_MoveRowDown"));
        moveSColDownBtn.setMnemonic(bundle.getString("CTL_MoveRowDown_Mnemonic").charAt(0));
        moveSColDownBtn.setToolTipText(bundle.getString("CTL_HINT_MoveColumnDown"));
        //moveSColDownBtn.setMargin(new Insets(0,2,0,2));
        buttonsPanel.add(moveSColDownBtn);
        moveSColDownBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveColumnRight();
            }
        });

        JPanel padding = new JPanel(new BorderLayout());
        padding.add(buttonsPanel, BorderLayout.NORTH);

        tab.add(padding, BorderLayout.EAST);

        tabbedPane.addTab(bundle.getString("CTL_Title2"), tab);

        // second tab (default-values table)
        tab = new JPanel();
        tab.setLayout(new BorderLayout(6, 6));
        tab.setBorder(new EmptyBorder(6, 2, 0, 2));

        defaultValuesTable = new CustomJTable();

        JLabel titleLabel2 = new JLabel(bundle.getString("CTL_DefaultTableValues"));
        titleLabel2.setDisplayedMnemonic(bundle.getString("CTL_DefaultTableValues_Mnemonic").charAt(0));
        titleLabel2.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_DefaultTableValues"));
        titleLabel2.setLabelFor(defaultValuesTable);
        tab.add(titleLabel2,BorderLayout.NORTH);

        defaultValuesTable.setModel(model);
        model.alwaysEditable = true;
        jscrollpane = new JScrollPane(defaultValuesTable);
        defaultValuesTable.setSelectionMode(0);
        defaultValuesTable.setCellSelectionEnabled(true);
        defaultValuesTable.setRowSelectionAllowed(true);
        defaultValuesTable.setColumnSelectionAllowed(true);
        defaultValuesTable.setPreferredScrollableViewportSize(new Dimension(450, 80));
        defaultValuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tab.add(jscrollpane,BorderLayout.CENTER); // NOI18N

        tabbedPane.addTab(bundle.getString("CTL_DefaultValues"), tab);

        // panel with buttons on the right (for default-values table)
        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());

        JPanel colButtonsPanel = new JPanel(new GridLayout(4, 0, 0, 3));
        JLabel colLabel = new JLabel(bundle.getString("CTL_Columns"));
        colLabel.setLabelFor(colButtonsPanel);
        java.awt.GridBagConstraints gridBagConstraints1;
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 2, 0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.gridx = 0;
        buttonsPanel.add(colLabel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 12, 5, 0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.gridx = 0;
        buttonsPanel.add(colButtonsPanel, gridBagConstraints1);
        
        insertColBtn = new JButton(bundle.getString("CTL_InsertColumn"));
        insertColBtn.setMnemonic(bundle.getString("CTL_InsertColumn_Mnemonic").charAt(0));
        insertColBtn.setToolTipText(bundle.getString("CTL_HINT_InsertColumn"));
        //insertColBtn.setMargin(new Insets(0,2,0,2));
        colButtonsPanel.add(insertColBtn);
        insertColBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertColumn();
            }
        });

        deleteColBtn = new JButton(bundle.getString("CTL_DeleteColumn"));
        deleteColBtn.setMnemonic(bundle.getString("CTL_DeleteColumn_Mnemonic").charAt(0));
        deleteColBtn.setToolTipText(bundle.getString("CTL_HINT_DeleteColumn"));
        //deleteColBtn.setMargin(new Insets(0,2,0,2));
        colButtonsPanel.add(deleteColBtn);
        deleteColBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeColumn();
            }
        });

        moveColLeftBtn = new JButton(bundle.getString("CTL_MoveColumnLeft"));
        moveColLeftBtn.setMnemonic(bundle.getString("CTL_MoveColumnLeft_Mnemonic").charAt(0));
        moveColLeftBtn.setToolTipText(bundle.getString("CTL_HINT_MoveColumnLeft"));
        //moveColLeftBtn.setMargin(new Insets(0,2,0,2));
        colButtonsPanel.add(moveColLeftBtn);
        moveColLeftBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveColumnLeft();
            }
        });

        moveColRightBtn = new JButton(bundle.getString("CTL_MoveColumnRight"));
        moveColRightBtn.setMnemonic(bundle.getString("CTL_MoveColumnRight_Mnemonic").charAt(0));
        moveColRightBtn.setToolTipText(bundle.getString("CTL_HINT_MoveColumnRight"));
        //moveColRightBtn.setMargin(new Insets(0,2,0,2));
        colButtonsPanel.add(moveColRightBtn);
        moveColRightBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveColumnRight();
            }
        });

        JPanel rowButtonsPanel = new JPanel(new GridLayout(4, 0, 0, 3));
        JLabel rowLabel = new JLabel(bundle.getString("CTL_Rows"));
        rowLabel.setLabelFor(rowButtonsPanel);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 2, 0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.gridx = 0;
        buttonsPanel.add(rowLabel, gridBagConstraints1);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 12, 0, 0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.gridx = 0;
        buttonsPanel.add(rowButtonsPanel, gridBagConstraints1);

        insertRowBtn = new JButton(bundle.getString("CTL_InsertRow"));
        insertRowBtn.setMnemonic(bundle.getString("CTL_InsertRow_Mnemonic").charAt(0));
        insertRowBtn.setToolTipText(bundle.getString("CTL_HINT_InsertRow"));
        //insertRowBtn.setMargin(new Insets(0,2,0,2));
        rowButtonsPanel.add(insertRowBtn);
        insertRowBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertRow();
            }
        });

        deleteRowBtn = new JButton(bundle.getString("CTL_DeleteRow"));
        deleteRowBtn.setMnemonic(bundle.getString("CTL_DeleteRow_Mnemonic").charAt(0));
        deleteRowBtn.setToolTipText(bundle.getString("CTL_HINT_DeleteRow"));
        //deleteRowBtn.setMargin(new Insets(0,2,0,2));
        rowButtonsPanel.add(deleteRowBtn);
        deleteRowBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeRow();
            }
        });

        moveRowUpBtn = new JButton(bundle.getString("CTL_MoveRowUp"));
        moveRowUpBtn.setMnemonic(bundle.getString("CTL_MoveRowUp_Mnemonic").charAt(0));
        moveRowUpBtn.setToolTipText(bundle.getString("CTL_HINT_MoveRowUp"));
        //moveRowUpBtn.setMargin(new Insets(0,2,0,2));
        rowButtonsPanel.add(moveRowUpBtn);
        moveRowUpBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveRowUp();
            }
        });

        moveRowDownBtn = new JButton(bundle.getString("CTL_MoveRowDown"));
        moveRowDownBtn.setMnemonic(bundle.getString("CTL_MoveRowDown_Mnemonic").charAt(0));
        moveRowDownBtn.setToolTipText(bundle.getString("CTL_HINT_MoveRowDown"));
        //moveRowDownBtn.setMargin(new Insets(0,2,0,2));
        rowButtonsPanel.add(moveRowDownBtn);
        moveRowDownBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveRowDown();
            }
        });

        padding = new JPanel();
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.weighty = 1.0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.VERTICAL;
        buttonsPanel.add(padding, gridBagConstraints1);

        tab.add(buttonsPanel, BorderLayout.EAST);

        add(tabbedPane, BorderLayout.CENTER);

        // lower panel with size info & buttons
        JPanel sizePanel = new JPanel();
        sizePanel.setBorder(new EmptyBorder(8,2,0,2));
        sizePanel.setLayout(new java.awt.GridBagLayout());

        rowsField = new JTextField(3);

        JLabel rowsLabel = new JLabel();
        rowsLabel.setText(bundle.getString("CTL_Rows"));
        rowsLabel.setDisplayedMnemonic(bundle.getString("CTL_Rows_Mnemonic").charAt(0));
        rowsLabel.setLabelFor(rowsField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        sizePanel.add(rowsLabel, gridBagConstraints1);

        rowsField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Rows"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 4, 0, 0);
        sizePanel.add(rowsField, gridBagConstraints1);

        addRowBtn = new JButton(" "+bundle.getString("CTL_CountPlus")+" ");
        addRowBtn.setMnemonic(bundle.getString("CTL_CountPlusRow_Mnemonic").charAt(0));
        addRowBtn.setToolTipText(bundle.getString("CTL_HINT_AddRow"));
        addRowBtn.setMargin(new Insets(-1,1,-2,0));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 4, 0, 0);
        sizePanel.add(addRowBtn, gridBagConstraints1);
        addRowBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addRow();
            }
        });
        
        removeRowBtn = new JButton(" "+bundle.getString("CTL_CountMinus")+" ");
        removeRowBtn.setMnemonic(bundle.getString("CTL_CountMinusRow_Mnemonic").charAt(0));
        removeRowBtn.setToolTipText(bundle.getString("CTL_HINT_RemoveRow"));
        removeRowBtn.setMargin(new Insets(-2,1,-1,1));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        sizePanel.add(removeRowBtn, gridBagConstraints1);
        removeRowBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeLastRow();
            }
        });
        
        columnsField = new JTextField(3);

        JLabel columnsLabel = new JLabel();
        columnsLabel.setText(bundle.getString("CTL_Columns"));
        columnsLabel.setDisplayedMnemonic(bundle.getString("CTL_Columns_Mnemonic").charAt(0));
        columnsLabel.setLabelFor(columnsField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(0, 20, 0, 0);
        sizePanel.add(columnsLabel, gridBagConstraints1);

        columnsField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Columns"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 4, 0, 0);
        sizePanel.add(columnsField, gridBagConstraints1);

        addColBtn = new JButton(" "+bundle.getString("CTL_CountPlus")+" ");
        addColBtn.setMnemonic(bundle.getString("CTL_CountPlusColumn_Mnemonic").charAt(0));
        addColBtn.setToolTipText(bundle.getString("CTL_HINT_AddColumn"));
        addColBtn.setMargin(new Insets(-1,1,-2,0));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 4, 0, 0);
        sizePanel.add(addColBtn, gridBagConstraints1);
        addColBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addColumn();
            }
        });
        
        removeColBtn = new JButton(" "+bundle.getString("CTL_CountMinus")+" ");
        removeColBtn.setMnemonic(bundle.getString("CTL_CountMinusColumn_Mnemonic").charAt(0));
        removeColBtn.setToolTipText(bundle.getString("CTL_HINT_RemoveColumn"));
        removeColBtn.setMargin(new Insets(-2,1,-1,1));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        sizePanel.add(removeColBtn, gridBagConstraints1);
        removeColBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeLastColumn();
            }
        });
        
        padding = new JPanel();

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0;
        sizePanel.add(padding, gridBagConstraints1);

        add(sizePanel, BorderLayout.SOUTH);


        // set textfields values and listeners
        rowsField.setText(String.valueOf(model.getRowCount()));
        columnsField.setText(String.valueOf(model.getColumnCount()));
        rowsField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent evt) {
                updateRows(rowsField.getText());
            }
        });
        rowsField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateRows(rowsField.getText());
            }
        });
        columnsField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent evt) {
                updateColumns(columnsField.getText());
            }
        });
        columnsField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateColumns(columnsField.getText());
            }
        });


        updateSettingsTable();
        enableButtons();
        isCreated = true;

        // listen to changes in the settings table - update the default-values table
        titleModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                if (evt.getType() == TableModelEvent.UPDATE) {
                    int selRow = defaultValuesTable.getSelectedRow(),
                        selCol = defaultValuesTable.getSelectedColumn();
                    updateDefaultTable();
                    refreshSelection(defaultValuesTable, selRow, selCol);
                }
                else
                    enableButtons();
            }
        });

        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                if (evt.getType() != TableModelEvent.UPDATE)
                    enableButtons();
            }
        });

        // listen to default table's ColumnModel and change column order when user does it visually
        defaultValuesTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            public void columnAdded(TableColumnModelEvent e) {
            }
            public void columnRemoved(TableColumnModelEvent e) {
            }
            public void columnMoved(TableColumnModelEvent e) {
                int fromI = e.getFromIndex(),
                    toI = e.getToIndex();
                if (fromI != toI)
                    moveColumn(fromI, toI);
                else {
                    defSelectedRow = defaultValuesTable.getSelectedRow();
                    defSelectedColumn = defaultValuesTable.getSelectedColumn();
                }
            }
            public void columnMarginChanged(ChangeEvent e) {
            }
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

        // enable/disable buttons on any selection change
        tabbedPane.addChangeListener(changeListener);
        settingsTable.getSelectionModel().addListSelectionListener(selectionListener);
        settingsTable.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
        defaultValuesTable.getSelectionModel().addListSelectionListener(selectionListener);
        defaultValuesTable.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TableCustomEditor"));
    }

    // enables/disables control buttons according to selected rows and columns
    private void enableButtons() {
        int rowCount = model.getRowCount();
        boolean anyRow = rowCount > 0;

        int columnCount = model.getColumnCount();
        boolean anyColumn = columnCount > 0;

        int selectedColumn = -1;
        if (anyColumn)
            if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
                selectedColumn = defaultValuesTable.getSelectedColumn();
            else if (tabbedPane.getSelectedIndex() == SETTINGS_TAB)
                selectedColumn = settingsTable.getSelectedRow();
        boolean columnSelected = selectedColumn >= 0 && selectedColumn < columnCount;

        int selectedRow = -1;
        if (anyRow && tabbedPane.getSelectedIndex() == DEFAULT_TAB) {
            selectedRow = defaultValuesTable.getSelectedRow();
            columnSelected &= selectedRow >= 0 && selectedRow < rowCount;
        }
        boolean rowSelected = selectedRow >= 0 && selectedRow < rowCount;
        rowSelected &= columnSelected;

        insertSColBtn.setEnabled(columnSelected);
        deleteSColBtn.setEnabled(columnSelected);
        moveSColUpBtn.setEnabled(selectedColumn > 0 && selectedColumn < columnCount);
        moveSColDownBtn.setEnabled(columnSelected && selectedColumn < columnCount-1);

        insertColBtn.setEnabled(columnSelected);
        deleteColBtn.setEnabled(columnSelected);
        moveColLeftBtn.setEnabled(selectedColumn > 0 && selectedColumn < columnCount && rowSelected);
        moveColRightBtn.setEnabled(columnSelected && selectedColumn < columnCount-1);
        insertRowBtn.setEnabled(rowSelected);
        deleteRowBtn.setEnabled(rowSelected);
        moveRowUpBtn.setEnabled(selectedRow > 0 && selectedRow < rowCount && columnSelected);
        moveRowDownBtn.setEnabled(rowSelected && selectedRow < rowCount-1);

        addRowBtn.setEnabled(true);
        removeRowBtn.setEnabled(anyRow);
        addColBtn.setEnabled(true);
        removeColBtn.setEnabled(anyColumn);
    }

    // adds one row to the default-values table
    private void addRow() {
        model.setRowCount(model.getRowCount()+1);
        rowsField.setText(Integer.toString(model.getRowCount()));
        defaultValuesTable.clearSelection();
    }

    // removes one row from the default-values table
    private void removeLastRow() {
        int n = model.getRowCount();
        if (n > 0) {
            model.setRowCount(n-1);
            rowsField.setText(Integer.toString(model.getRowCount()));
            defaultValuesTable.clearSelection();
        }
    }

    // inserts one row to the default-values table (at current position)
    private void insertRow() {
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB) {
            examineSelections();
            if (defSelectedRow >= 0) {
                model.addRow(defSelectedRow);
                rowsField.setText(Integer.toString(model.getRowCount()));
                updateSelections(true);
            }
            else if (model.getRowCount() == 0)
                addRow();
        }
    }

    // removes one row from the default-values table (from current position)
    private void removeRow() {
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB && model.getRowCount() > 0) {
            examineSelections();
            if (defSelectedRow >= 0) {
                model.removeRow(defSelectedRow);
                rowsField.setText(Integer.toString(model.getRowCount()));

                if (defSelectedRow == model.getRowCount() && defSelectedRow > 0)
                    defSelectedRow--;
                updateSelections(true);
            }
        }
    }

    // sets number of rows for the default-values table from given text
    private void updateRows(String text) {
        int n = 0;
        try {
            n = Integer.parseInt(text);
        } catch(NumberFormatException e) {
            return;
        }
        if (n >= 0) {
            model.setRowCount(n);
            //defaultValuesTable.clearSelection();
        }
    }

    // moves selected row up one row
    private void moveRowUp() {
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB && model.getRowCount() > 0) {
            examineSelections();
            if (defSelectedRow > 0) {
                model.moveRow(defSelectedRow, defSelectedRow-1);
                defSelectedRow--;
                updateSelections(true);
            }
        }
    }

    // moves selected row down one row
    private void moveRowDown() {
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB && model.getRowCount() > 0) {
            examineSelections();
            if (defSelectedRow >= 0 && defSelectedRow < model.getRowCount()-1) {
                model.moveRow(defSelectedRow, defSelectedRow+1);
                defSelectedRow++;
                updateSelections(true);
            }
        }
    }

    // adds one column to the default-values table (a row to the settings table)
    private void addColumn() {
        int n = model.getColumnCount();
        model.setColumnCount(n+1);
        defaultValuesTable.clearSelection();
        updateSettingsTable();
        settingsTable.clearSelection();
        columnsField.setText(Integer.toString(model.getColumnCount()));
    }

    // removes last column from the default-values table (last row from the settings table)
    private void removeLastColumn() {
        int n = model.getColumnCount();
        if (n > 0) {
            model.setColumnCount(n-1);
            defaultValuesTable.clearSelection();
            updateSettingsTable();
            settingsTable.clearSelection();
            columnsField.setText(Integer.toString(model.getColumnCount()));
        }
    }

    // inserts one column to the default-values table at the current position (row in settings table)
    private void insertColumn() {
        int ci;
        examineSelections();
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
            ci = defSelectedColumn;
        else if (tabbedPane.getSelectedIndex() == SETTINGS_TAB)
            ci = stSelectedRow;
        else ci = -1;

        if (ci >= 0) {
            model.addColumn(ci);
            updateSettingsTable();
            columnsField.setText(Integer.toString(model.getColumnCount()));
            updateSelections(true);
        }
        else if (model.getColumnCount() == 0)
            addColumn();
    }

    // removes currently selected column from the default-values table (row from the settings table)
    private void removeColumn() {
        if (model.getColumnCount() > 0) {
            int ci;
            examineSelections();
            if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
                ci = defSelectedColumn;
            else if (tabbedPane.getSelectedIndex() == SETTINGS_TAB)
                ci = stSelectedRow;
            else ci = -1;

            if (ci >= 0) {
                model.removeColumn(ci);
                updateSettingsTable();
                columnsField.setText(Integer.toString(model.getColumnCount()));

                if (ci == model.getColumnCount() && ci > 0)  ci--;
                if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
                    defSelectedColumn = ci;
                else stSelectedRow = ci;
                updateSelections(true);
            }
        }
    }

    // sets number of columns from given text
    private void updateColumns(String text) {
        int n = 0;
        try {
            n = Integer.parseInt(text);
        } catch(NumberFormatException e) {
            return;
        }
        if (n >= 0) {
            model.setColumnCount(n);
            //defaultValuesTable.clearSelection();
            updateSettingsTable();
            //settingsTable.clearSelection();
        }
    }

    private void moveColumnLeft() {
        int ci;
        examineSelections();
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
            ci = defSelectedColumn;
        else if (tabbedPane.getSelectedIndex() == SETTINGS_TAB)
            ci = stSelectedRow;
        else ci = -1;

        if (ci > 0) {
            model.moveColumn(ci, ci-1);
            updateSettingsTable();

            if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
                defSelectedColumn = ci-1;
            else stSelectedRow = ci-1;
            updateSelections(true);
        }
    }

    private void moveColumnRight() {
        int ci;
        examineSelections();
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
            ci = defSelectedColumn;
        else if (tabbedPane.getSelectedIndex() == SETTINGS_TAB)
            ci = stSelectedRow;
        else ci = -1;

        if (ci >= 0 && ci < model.getColumnCount()-1) {
            model.moveColumn(ci, ci+1);
            updateSettingsTable();

            if (tabbedPane.getSelectedIndex() == DEFAULT_TAB)
                defSelectedColumn = ci+1;
            else stSelectedRow = ci+1;
            updateSelections(true);
        }
    }

    // called when a column is moved in the default-values table (by user)
    private void moveColumn(int fromIndex, int toIndex) {
        model.moveColumn(fromIndex, toIndex);
        updateSettingsTable();

        int selCol;
        if (defSelectedRow >= 0) {
            if (defSelectedColumn == fromIndex)
                selCol = toIndex;
            else if (defSelectedColumn == toIndex)
                selCol = fromIndex;
            else
                selCol = defSelectedColumn;
        }
        else selCol = -1;

        if (selCol >= 0) {
            defaultValuesTable.setRowSelectionInterval(defSelectedRow,defSelectedRow);
            defaultValuesTable.setColumnSelectionInterval(selCol, selCol);
        }
        else {
            defaultValuesTable.clearSelection();
            defaultValuesTable.getParent().requestFocus();
        }

        if (defaultValuesTable.getTableHeader().getDraggedColumn() != null)
            defaultValuesTable.getTableHeader().setDraggedColumn(
                defaultValuesTable.getColumnModel().getColumn(toIndex));
    }

    // helper methods ------------------
    private void examineSelections() {
        stSelectedRow = settingsTable.getSelectedRow();
        stSelectedColumn = settingsTable.getSelectedColumn();
        defSelectedRow = defaultValuesTable.getSelectedRow();
        defSelectedColumn = defaultValuesTable.getSelectedColumn();
    }

    // updates cell selection in tables (settings and default-values)
    private void updateSelections(boolean focusTable) {
        if (tabbedPane.getSelectedIndex() == DEFAULT_TAB) {
            refreshSelection(settingsTable, defSelectedColumn, stSelectedColumn);
            if (focusTable && refreshSelection(defaultValuesTable, defSelectedRow, defSelectedColumn))
                defaultValuesTable.requestFocus();
        }
        else if (tabbedPane.getSelectedIndex() == SETTINGS_TAB) {
            refreshSelection(defaultValuesTable, defSelectedRow, stSelectedRow);
            if (focusTable && refreshSelection(settingsTable, stSelectedRow, stSelectedColumn))
                settingsTable.requestFocus();
        }
    }

    private static boolean refreshSelection(JTable table, int row, int column) {
        table.clearSelection();
        if (row >= 0 && row < table.getRowCount() && column >= 0 && column < table.getColumnCount()) {
            table.setRowSelectionInterval(row, row);
            table.setColumnSelectionInterval(column, column);
            return true;
        }
        return false;
    }
    // ------------------

    // updates the default-values-table ('model') according to settings-table ('titleModel')
    void updateDefaultTable() {
        if (model == null || isChangingTableModel) return; //at component creation

        int cols = model.getColumnCount(),
            rows = model.getRowCount();
        boolean typeChanged = false;

        for (int i=0; i < cols; i++) {
            model.setColumnName(i, (String) titleModel.getValueAt(i,1));
            Class type;
            try {
                type = Class.forName("java.lang."+((String) titleModel.getValueAt(i,2))); // NOI18N
            } catch(Exception e) {
                type = Object.class;
            }
            if (!type.equals(model.getColumnClass(i))) {
                typeChanged = true;
                model.setColumnClass(i, type);
                for (int j=0; j < rows; j++)
                    model.setValueAt(null, j,i);
            }
            model.setColumnEditable(i, ((Boolean) titleModel.getValueAt(i,3)).booleanValue());
        }

        if (typeChanged)
            defaultValuesTable.createDefaultColumnsFromModel();

        model.fireTableStructureChanged();
    }

    // updates the settings table ('titleModel') according to default-values-table ('model')
    void updateSettingsTable() {
        isChangingTableModel = true;
        int cols = model.getColumnCount();
        if (cols != titleModel.getRowCount()) {
            isCreated = false;
            titleModel.setRowCount(cols);
            isCreated = true;
        }

        for (int i=0; i < cols; i++) {
            titleModel.setValueAt(Integer.toString(i+1), i,0);
            titleModel.setValueAt(model.getColumnName(i), i,1);
            String type = model.getColumnClass(i).getName();
            int lastDot = type.lastIndexOf('.');
            if (lastDot >= 0)
                type = type.substring(lastDot+1, type.length());
            titleModel.setValueAt(type, i,2);
            titleModel.setValueAt(model.isColumnEditable(i) ? Boolean.TRUE : Boolean.FALSE, i,3);
        }

        isChangingTableModel = false;
    }

    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue() throws IllegalStateException {
        updateDefaultTable();
        return new TableModelEditor.NbTableModel(model);
    }

    //
    // -------------------------------------------------------------------------
    
    private class SelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            enableButtons();
        }
    }

    private class TabChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            enableButtons();
        }
    }

    // a custom JTable class solving some accessibility issues of JTable
    private static class CustomJTable extends JTable {
        Component edComp;

        protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                            int condition, boolean pressed)
        {
            if (e != null && e.getID() == KeyEvent.KEY_PRESSED
                && (e.getModifiers() & (InputEvent.CTRL_MASK|InputEvent.ALT_MASK)) != 0)
            {
                return false;
            }
            else return super.processKeyBinding(ks, e, condition, pressed);
        }

        protected void processKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (!isEditing())
                        return;
                }
                else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    int anchorRow = getSelectionModel().getAnchorSelectionIndex();
                    int anchorColumn = getColumnModel().getSelectionModel().
                                       getAnchorSelectionIndex();
                    if (anchorRow != -1 && anchorColumn != -1 && !isEditing()) {
                        super.processKeyEvent(e);
                        e.consume();
                        if (edComp != null)
                            edComp.requestFocus();
                        return;
                    }
                }
            }
            super.processKeyEvent(e);
        }

        public Component prepareEditor(TableCellEditor editor, int row, int column) {
            edComp = super.prepareEditor(editor, row, column);
            return edComp;
        }
    }
}

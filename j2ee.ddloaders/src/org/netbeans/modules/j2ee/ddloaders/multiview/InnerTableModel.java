/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.*;

/**
 * @author pfiala
 */
public abstract class InnerTableModel extends AbstractTableModel {

    private XmlMultiViewDataObject dataObject;
    protected final String[] columnNames;
    private int[] columnWidths;
    private int rowCount = -1;

    public InnerTableModel(XmlMultiViewDataObject dataObject, String[] columnNames, int[] columnWidths) {
        this.dataObject = dataObject;
        this.columnNames = columnNames;
        this.columnWidths = columnWidths;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public TableCellEditor getCellEditor(int columnIndex) {
        return null;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int[] getColumnWidths() {
        return columnWidths;
    }

    public abstract int addRow();

    public abstract void removeRow(int selectedRow);

    public int getDefaultColumnWidth(int i) {
        return columnWidths[i];
    }

    public void refreshView() {
        fireTableDataChanged();
    }

    protected void tableChanged() {
        if (!checkRowCount()) {
            fireTableDataChanged();
        }
    }

    private boolean checkRowCount() {
        int n = getRowCount();
        if (rowCount == -1) {
            rowCount = n;
        }
        if (n != rowCount) {
            while (rowCount < n) {
                rowCount++;
                fireTableRowsInserted(0, 0);
            }
            while (rowCount > n) {
                rowCount--;
                fireTableRowsDeleted(0, 0);
            }
            return true;
        } else {
            return false;
        }
    }

    protected void modelUpdatedFromUI() {
        if (dataObject!= null) {
            dataObject.modelUpdatedFromUI();
        }
    }

    public TableCellEditor getTableCellEditor(int column) {
        return null;
    }

    protected TableCellEditor createComboBoxCellEditor(Object[] items) {
        return createComboBoxCellEditor(items, false);
    }

    private static TableCellEditor createComboBoxCellEditor(Object[] items, final boolean editable) {
        final JComboBox comboBox = new JComboBox(items);
        comboBox.setEditable(editable);
        return new DefaultCellEditor(comboBox);
    }
}

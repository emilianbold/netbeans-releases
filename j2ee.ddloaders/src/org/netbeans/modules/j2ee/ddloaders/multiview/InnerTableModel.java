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

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
public abstract class InnerTableModel extends AbstractTableModel {

    protected final String[] columnNames;
    private int[] columnWidths;

    public InnerTableModel(String[] columnNames, int[] columnWidths) {
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

    public void dataFileChanged() {
        fireTableDataChanged();
    }
}

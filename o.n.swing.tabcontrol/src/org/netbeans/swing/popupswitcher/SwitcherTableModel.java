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

package org.netbeans.swing.popupswitcher;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.openide.util.Utilities;

/**
 * <code>TableModel</code> for <code>SwitcherTable</class>.
 *
 * @see SwitcherTable
 *
 * @author mkrauskopf
 */
class SwitcherTableModel extends AbstractTableModel {
    
    /**
     * Used to estimate number of cells fitting to given space Event object for
     * this TableModel.
     */
    private TableModelEvent event;
    
    /** Number of rows */
    private int rows;
    
    /** Number of columns */
    private int cols;
    
    /** Items */
    private SwitcherTableItem[] items;
    
    /**
     * Use whole screen for table height during number of columns/row
     * computing.
     */
    SwitcherTableModel(SwitcherTableItem[] items, int rowHeight) {
        this(items, rowHeight, Utilities.getUsableScreenBounds().height);
    }
    
    /** Use specified table height during number of columns/row computing. */
    SwitcherTableModel(SwitcherTableItem[] items, int rowHeight, int tableHeight) {
        super();
        this.items = items;
        computeRowsAndCols(rowHeight, tableHeight);
    }
    
    private void computeRowsAndCols(int rowHeight, int tableHeight) {
        // Default algorithm - use whole screen for SwitcherTable
        int nOfItems = items.length;
        if (nOfItems > 0) { // avoid div by 0
            // Compute number of rows in one column
            int maxRowsPerCol = tableHeight / rowHeight;
            int nOfColumns = (nOfItems / maxRowsPerCol);
            if (nOfItems % maxRowsPerCol > 0) {
                nOfColumns++;
            }
            int nOfRows = nOfItems / nOfColumns;
            if (nOfItems % nOfColumns > 0) {
                nOfRows++;
            }
            setRowsAndColumns(nOfRows, nOfColumns);
        } else {
            setRowsAndColumns(0, 0);
        }
    }
    
    private void setRowsAndColumns(int rows, int cols) {
        if ((this.rows != rows) || (this.cols != cols)) {
            this.rows = rows;
            this.cols = cols;
            if (event == null) {
                event = new TableModelEvent(this);
            }
            fireTableChanged(event);
        }
    }
    
    public Class getColumnClass(int columnIndex) {
        return SwitcherTableItem.class;
    }
    
    public String getColumnName(int columnIndex) {
        return "";
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex == -1) || (columnIndex == -1)) {
            return null;
        }
        int docIdx = (columnIndex * getRowCount()) + rowIndex;
        return (docIdx < items.length ? items[docIdx] : null);
    }
    
    public int getRowCount() {
        return rows;
    }
    
    public int getColumnCount() {
        return cols;
    }
}

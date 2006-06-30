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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.tablemodel;

import org.netbeans.modules.jmx.mbeanwizard.listener.TableRemoveListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;


/**
 * Abstract class defining a generic table model
 *
 */
public abstract class AbstractJMXTableModel extends AbstractTableModel {
    
    protected static final int NBROW_MIN = 0;
    protected ArrayList data;
    protected transient ResourceBundle bundle;
    protected String[]  columnNames;
    
    /**
     * Constructor
     */
    public AbstractJMXTableModel() {
    }
    
    /**
     * Adds a row to the current table model
     */
    public abstract void addRow();
    
    /**
     * Method returning the size of the model i.e the number of lines
     * @return int the number of lines
     */
    public int size() {
        
        return getRowCount();
    }
    
    /**
     * Method returning the number of rows
     * @return int the number of rows
     */
    public int getRowCount() {
        return data.size();
    }
    
    /**
     * Method returning the number of columns
     * @return int the number of columns
     */
    public int getColumnCount() {
        return columnNames.length;
    }
    
    /**
     * Method returning a value of the model
     * @return Object the value contained in the model at (row,column)
     * @param row the row of the value to get
     * @param col the column of the value to get
     */
    public Object getValueAt(int row, int col) {
        return ((ArrayList)data.get(row)).get(col);
    }
    
    /**
     * Sets a value in the model
     * @param aValue the value to be set at (row,column)
     * @param rowIndex the row
     * @param columnIndex the column
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size())
            ((ArrayList)data.get(rowIndex)).set(columnIndex,aValue);
    }
    
    /**
     * Method returning the header of the column
     * @param column the column number
     * @return String the column header
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    /**
     * Method returning wheter the cell (r,c) is editable or not
     * @param r the row of the cell
     * @param c the column of the cell
     * @return boolean true if the cell is editable
     */
    public boolean isCellEditable(int r, int c) {
        return true;
    }
    
    /**
     * Removes a row in the model
     * @param selectedRow the row number to be removed
     * @param table the table to remove the row in
     */
    public void remRow(int selectedRow, JTable table) {
        int nbRow = getRowCount();
        
        //if there is one or more elements in the model
        //remove the selected row
        if(nbRow > NBROW_MIN) {
            data.remove(selectedRow);
            
            // fire an event of removal
            fireTableRemoveEvent(selectedRow, table);
            
            if(table.isEditing())
                table.getCellEditor().stopCellEditing();
        }
        this.fireTableDataChanged();
    }
    
    /**
     * Method selecting the next row after removal
     * @param selectedRow the selected row
     * @param table the given table
     */
    public void selectNextRow(int selectedRow,JTable table) {
        
        final JTable t = table;
        final int sel = selectedRow;
        
        //first, empty the event queue before firethe event
        //workaround swing event model
        if(((AbstractJMXTableModel)t.getModel()).size() > 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(sel == 0) {
                        t.setRowSelectionInterval(0,0);
                    } else {
                        if(sel == ((AbstractJMXTableModel)t.getModel()).size()){
                            t.setRowSelectionInterval(sel -1, sel -1);
                        } else {
                            t.setRowSelectionInterval(sel, sel);
                        }
                    }
                }
            });
        }
    }
    
    //=========================================
    // Manual event gestion
    //=========================================
    private final Set listeners = new HashSet(1); // Set<ChangeListener>
    public final void addTableRemoveListener(TableRemoveListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeTableRemoveListener(TableRemoveListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireTableRemoveEvent(int selectedRow, JTable table) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        
        TableModelEvent ev = new TableModelEvent(this,selectedRow,selectedRow,
                table.getSelectedColumn(), TableModelEvent.DELETE);
        while (it.hasNext()) {
            ((TableRemoveListener) it.next()).tableStateChanged(ev);
        }
    }
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.ui.deployInfo;

import org.openide.ErrorManager;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;


/**
 * JBI deployInfo component Table Model
 *
 * @author Tientien Li
 */
public class ComponentTableModel extends AbstractTableModel {
    /** Column labels used */
    private Vector mColumnNames = null;

    /** Data for this model */
    private Vector mData = null;

    /**
     * Constructor for the DependencyModel object
     *
     * @param data row data to populate table model with
     * @param labels column titles to populate table model with
     */
    public ComponentTableModel(Vector data, Vector labels) {
        mData = data;
        mColumnNames = labels;
    }

    /**
     * get column count
     *
     * @return int
     */
    public int getColumnCount() {
        return mColumnNames.size();
    }

    /**
     * get row count
     *
     * @return int
     */
    public int getRowCount() {
        return mData.size();
    }

    /**
     * return name of column given column index
     *
     * @param col int
     *
     * @return String
     */
    public String getColumnName(int col) {
        return (String) mColumnNames.elementAt(col);
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     * Gets the cellEditable attribute of the Dependency Model object
     *
     * @param row row position
     * @param col column position
     *
     * @return The cellEditable value
     */
    public boolean isCellEditable(int row, int col) {
        boolean isEditable = false;

        if (col == 0) {
            isEditable = true;

            /*
               ComponentObject dataEntry = (ComponentObject) mData.elementAt(row);
               isEditable = dataEntry.getEnabled();
             */
        }

        return isEditable;
    }

    /**
     * Get the valueAt attribute of the model object
     *
     * @param row row position
     * @param col column position
     *
     * @return The valueAt value
     */
    public Object getValueAt(int row, int col) {
        Object obj = null;

        try {
            if ((getRowCount() > 0) && ((row != -1) && (col != -1))) {
                ComponentObject tableEntry = (ComponentObject) mData.elementAt(row);

                if (tableEntry != null) { //

                    // set the right data for each column
                    if (col == 0) {
                        obj = new Boolean(tableEntry.getEnabled());
                    } else if (col == 1) {
                        obj = tableEntry.getType();
                    } else if (col == 2) {
                        obj = tableEntry.getName();
                    //} else if (col == 3) {
                    //    obj = tableEntry.getId();
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return obj;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change. Column 0 is the only one that is changing
     */
    public void setValueAt(Object value, int row, int col) {
        ComponentObject dataEntry = (ComponentObject) mData.elementAt(row);

        // the boolean value is always the 1st entry on the model
        if (value instanceof Boolean) {
            dataEntry.setEnabled(((Boolean) value).booleanValue());
        }

        fireTableCellUpdated(row, col);
    }

    /**
     * reset the data and column labels
     *
     * @param data row data to set
     * @param labels column labels to set
     */
    public void setDataVector(Vector data, Vector labels) {
        mData = data;
        mColumnNames = labels;
    }

    /**
     * Returns true if row is editable
     *
     * @param row row position
     *
     * @return The edit mode value
     */
    public boolean isRowEditable(int row) {
        boolean isEditable = false;

        // only column 0 is editable in this model
        try {
            ComponentObject dataEntry = (ComponentObject) mData.elementAt(row);
            isEditable = dataEntry.getEnabled();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return isEditable;
    }

    /**
     * The main program for the DependencyModel class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
    }
}

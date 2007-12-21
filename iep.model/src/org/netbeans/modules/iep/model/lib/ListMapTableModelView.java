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


package org.netbeans.modules.iep.model.lib;

import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * ListMapTableModelView provides a filterable TableModel view of a ListMap
 *
 * @author Bing Lu
 *
 * @since May 8, 2002
 */
public class ListMapTableModelView
    implements TableModel {

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ListMapTableModelView.class.getName());

    // default table model to delegate event firing
    private DefaultTableModel mDelegateTableModel = null;
    private ListMap mListMap = null;

    /**
     * Constructor for the ListMapTableModelView object
     *
     * @param listMap The data to be represented in a JTable
     * @param viewKeys The keys specifying data to be represented
     */
    public ListMapTableModelView(ListMap listMap, List viewKeys) {

        super();

        Vector colVector = new Vector();

        colVector.add("Name");
        colVector.add("Value");

        // convert ListMap to Vector of Vectors to delegate
        // everything to superclass
        List keys = listMap.getKeyList();
        List vals = listMap.getValueList();
        Vector dataVector = new Vector();

        for (int i = 0, sz = keys.size(); i < sz; i++) {
            if (viewKeys.contains(keys.get(i))) {
                Vector row = new Vector();

                row.add(keys.get(i));
                row.add(vals.get(i));
                dataVector.add(row);
            }
        }

        mDelegateTableModel = new DefaultTableModel();

        mDelegateTableModel.setDataVector(dataVector, colVector);

        mListMap = listMap;
    }

    /**
     * Returns true for columns > 0.
     *
     * @param rowIndex the row being queried
     * @param columnIndex the column being queried
     *
     * @return false
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (!(columnIndex == 0));
        //if (columnIndex == 0) {

            // first column is not editable
        //    return false;
        //} else {
        //    return true;
        //}
    }

    /**
     * Gets the class type at the given column index. If no value is found at
     * given index, class type Object.class is returned to be consistent with
     * super class AbstractTableModel.getColumnClass(int)
     *
     * @param columnIndex Column index value to look up
     *
     * @return The class type at the given index
     */
    public Class getColumnClass(int columnIndex) {

        // default value as in super class
        Class c = Object.class;
        Vector dataVector = mDelegateTableModel.getDataVector();

        if (dataVector.size() != 0) {

            // get first row
            Vector rowVector = (Vector) dataVector.elementAt(0);

            // get value at given column
            Object obj = rowVector.elementAt(columnIndex);

            if (obj != null) {

                // and lookup class type
                c = obj.getClass();
            }
        }

        return c;
    }

    /**
     * Returns the number of columns in this data table.
     *
     * @return the number of columns in the model
     */
    public int getColumnCount() {
        return mDelegateTableModel.getColumnCount();
    }

    /**
     * Replaces the column identifiers in the model. If columnIdentifiers is
     * null or does not contain two elements, no change is made.
     *
     * @param columnIdentifiers The new columnIdentifiers value
     */
    public void setColumnIdentifiers(Vector columnIdentifiers) {

        if ((columnIdentifiers != null) && (columnIdentifiers.size() == 2)) {
            mDelegateTableModel.setColumnIdentifiers(columnIdentifiers);
        }
    }

    /**
     * Replaces the column identifiers in the model. If columnIdentifiers is
     * null or does not contain two elements, no change is made.
     *
     * @param columnIdentifiers The new columnIdentifiers value
     *
     * @see #setNumRows
     */
    public void setColumnIdentifiers(Object[] columnIdentifiers) {

        if ((columnIdentifiers != null) && (columnIdentifiers.length == 2)) {
            mDelegateTableModel.setColumnIdentifiers(columnIdentifiers);
        }
    }

    /**
     * Gets the column name at the given index. If column cannot be found,
     * returns an empty string Overrides String
     * AbstractTableModel.getColumnName(int)
     *
     * @param column Requested column index
     *
     * @return The column name value
     */
    public String getColumnName(int column) {

        try {
            return (String) mDelegateTableModel.getColumnName(column);
        } catch (ArrayIndexOutOfBoundsException e) {
            mLog.warning("Invalid column index: " + column
                      + ". Returned empty String column name");

            // resturn an empty string if column cannot be found
            // This is the recovery action documented in AbstractTablemodel's
            // javadoc
            return "";
        }
    }

    /**
     * Override DefaultTableModel to prevent internal data from being changed
     *
     * @param dataVector the new data vector
     * @param columnIdentifiers the names of the columns
     *
     * @see #getDataVector
     */
    public void setDataVector(Vector dataVector, Vector columnIdentifiers) {
    }

    /**
     * Override DefaultTableModel to prevent internal data from being changed
     *
     * @param dataVector the new data vector
     * @param columnIdentifiers the names of the columns
     *
     * @see #setDataVector(Vector, Vector)
     */
    public void setDataVector(Object[][] dataVector,
                              Object[] columnIdentifiers) {
    }

    /**
     * Returns the number of rows in this data table.
     *
     * @return the number of rows in the model
     */
    public int getRowCount() {
        return mDelegateTableModel.getRowCount();
    }

    /**
     * Override ListMapDataModel's method to prevent properties from being
     * changed Sets the valueAt attribute of the PropertyTableModel object
     *
     * @param aValue The new valueAt value
     * @param row The new valueAt value
     * @param column The new valueAt value
     *
     */
    public void setValueAt(Object aValue, int row, int column) {

        if (column == 0) {

            // first column contains keys which are intended to be immutable
            return;
        }

        try {
            mDelegateTableModel.setValueAt(aValue, row, column);
        } catch (ArrayIndexOutOfBoundsException e) {

            // re-throw with better message
            throw new ArrayIndexOutOfBoundsException("Could not set value \""
                                                     + aValue.toString()
                                                     + "\" at row=" + row
                                                     + ",col=" + column
                                                     + ": out of bound.");
        }

        // the call to delegate didn't throw exception so this is valid
        // data, go ahead and get the correct row
        Vector dataVector = mDelegateTableModel.getDataVector();
        Vector rowVector = (Vector) dataVector.elementAt(row);

        // now get key at given column on the retrieved row
        Object key = rowVector.elementAt(0);

        // update internal data using the found key
        this.mListMap.put(key, aValue);
    }

    /**
     * Returns an attribute value for the cell at <code>row</code> and
     * <code>column</code> .
     *
     * @param row the row whose value is to be queried
     * @param column the column whose value is to be queried
     *
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int row, int column) {
        return mDelegateTableModel.getValueAt(row, column);
    }

    /**
     * Adds a listener to the list that is notified each time a change to the
     * data model occurs.
     *
     * @param l the TableModelListener
     */
    public void addTableModelListener(TableModelListener l) {
        mDelegateTableModel.addTableModelListener(l);
    }

    /**
     * Returns a column given its name. Overrides int
     * AbstractTableModel.findColumnName(String)
     *
     * @param columnName string containing name of column to be located
     *
     * @return the column index where columnName resides, or -1 if not found
     */
    public int findColumn(String columnName) {
        return mDelegateTableModel.findColumn(columnName);
    }

    /**
     * Removes a listener from the list that is notified each time a change to
     * the data model occurs.
     *
     * @param l the TableModelListener
     */
    public void removeTableModelListener(TableModelListener l) {
        mDelegateTableModel.removeTableModelListener(l);
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/

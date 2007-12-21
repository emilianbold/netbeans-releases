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
 * ListMapTableModel provides a TableModel view of a ListMap
 *
 * @author Bing Lu
 *
 * @since May 8, 2002
 */
public class ListMapTableModel implements TableModel {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ListMapTableModel.class.getName());

    // default table model to delegate event firing
    private DefaultTableModel mDelegateTableModel = null;
    private ListMap mListMap = null;

    /**
     * Constructor for the ListMapTableModel object
     *
     * @param listMap The data to be represented in a JTable
     */
    public ListMapTableModel(ListMap listMap) {

        super();

        Vector colVector = new Vector();

        colVector.add("Name");
        colVector.add("Value");

        // convert ListMap to Vector of Vectors to delegate
        // everything to superclass
        List keys = listMap.getKeyList();
        List vals = listMap.getValueList();
        Vector dataVector = new Vector();

        for (int i = 0; i < vals.size(); i++) {
            Vector row = new Vector();

            row.add(keys.get(i));
            row.add(vals.get(i));
            dataVector.add(row);
        }

        mDelegateTableModel = new DefaultTableModel();

        mDelegateTableModel.setDataVector(dataVector, colVector);

        mListMap = listMap;
    }

    /**
     * Test method
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {

        // move test
        ListMap toMove = new ArrayHashMap();

        toMove.put("key0", "a");
        toMove.put("key1", "B");
        toMove.put("key2", "C");
        toMove.put("key3", "D");
        toMove.put("key4", "e");
        toMove.put("key5", "f");
        toMove.put("key6", "g");
        toMove.put("key7", "h");
        toMove.put("key8", "i");
        toMove.put("key9", "j");

        List keys = toMove.getKeyList();

        for (int i = 0; i < keys.size(); i++) {
            mLog.info(keys.get(i) + " = " + toMove.get(keys.get(i)));
        }

        ListMapTableModel tblModel = new ListMapTableModel(toMove);

        tblModel.moveRow(1, 3, 5);

        keys = toMove.getKeyList();

        for (int i = 0; i < keys.size(); i++) {
            mLog.info(keys.get(i) + " = " + toMove.get(keys.get(i)));
        }

        // display test
        ListMap listMap = new ArrayHashMap();

        for (int i = 0; i < 40; i++) {
            listMap.put("key" + i, "val" + i);
        }

        javax.swing.JFrame frame = new javax.swing.JFrame("ListMapTableModel");
        javax.swing.JTable table =
            new javax.swing.JTable(new ListMapTableModel(listMap));

        frame.getContentPane().add(new javax.swing.JScrollPane(table));
        frame.pack();
        frame.setVisible(true);
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
     * Sets the object value for the cell at <code>column</code> and
     * <code>row</code> . <code>aValue</code> is the new value. No change if
     * column == 0 as first column is immutable in this perticular
     * implementation. This method will generate a <code>tableChanged</code>
     * notification.
     *
     * @param aValue the new value; this can be null
     * @param row the row whose value is to be changed
     * @param column the column whose value is to be changed
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
     * Adds a row to the end of the model. The new row will contain null values
     * unless rowData is specified. Notification of the row being added will
     * be generated.
     *
     * @param rowData optional data of the row being added
     */
    public void addRow(Vector rowData) {

        if ((rowData != null) && (rowData.size() == 2)) {
            Object key = rowData.get(0);
            Object val = rowData.get(1);

            if (mListMap.containsKey(key)) {
                int idx = mListMap.getKeyList().indexOf(key);

                mDelegateTableModel.removeRow(idx);
                mListMap.remove(key);
            }

            mListMap.put(key, val);
            mDelegateTableModel.addRow(rowData);
        }
    }

    /**
     * Adds a row to the end of the model. The new row will contain null values
     * unless rowData is specified. Notification of the row being added will
     * be generated.
     *
     * @param rowData optional data of the row being added
     */
    public void addRow(Object[] rowData) {

        if ((rowData != null) && (rowData.length == 2)) {
            Object key = rowData[0];
            Object val = rowData[1];

            if (mListMap.containsKey(key)) {
                int idx = mListMap.getKeyList().indexOf(key);

                mDelegateTableModel.removeRow(idx);
                mListMap.remove(key);
            }

            mListMap.put(key, val);
            mDelegateTableModel.addRow(rowData);
        }
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
     * Inserts a row at row in the model. The new row will contain null values
     * unless rowData is specified. Notification of the row being added will
     * be generated.
     *
     * @param idx the row index of the row to be inserted
     * @param rowData optional data of the row being added
     *
     * @exception ArrayIndexOutOfBoundsException Description of the Exception
     */
    public void insertRow(int idx, Vector rowData)
        throws ArrayIndexOutOfBoundsException {

        if ((rowData != null) && (rowData.size() == 2)) {
            Object key = rowData.get(0);
            Object val = rowData.get(1);

            if (mListMap.containsKey(key)) {
                mListMap.remove(key);

                if (idx > mListMap.size()) {

                    // add to the end
                    mListMap.put(key, val);
                } else {
                    mListMap.put(idx, key, val);
                }
            } else {
                mListMap.put(key, val);
            }

            mDelegateTableModel.insertRow(idx, rowData);
        }
    }

    /**
     * Inserts a row at row in the model. The new row will contain null values
     * unless rowData is specified. Notification of the row being added will
     * be generated.
     *
     * @param idx the row index of the row to be inserted
     * @param rowData optional data of the row being added
     *
     * @exception ArrayIndexOutOfBoundsException Description of the Exception
     */
    public void insertRow(int idx, Object[] rowData)
        throws ArrayIndexOutOfBoundsException {

        Vector v = new Vector();

        for (int i = 0; i < rowData.length; i++) {
            v.add(rowData[i]);
        }

        this.insertRow(idx, v);
    }

    /**
     * Moves one or more rows from the inclusive range <code>start</code> to
     * <code>end</code> to the <code>to</code> position in the model. After
     * the move, the row that was at index <code>start</code> will be at index
     * <code>to</code>. This method will send a <code>tableChanged</code>
     * notification message to all the listeners.
     *
     * <p>
     * <pre>
     *  Examples of moves:
     *  <p>
     *
     * 1. moveRow(1,3,5); a|B|C|D|e|f|g|h|i|j|k - before a|e|f|g|h|B|C|D|i|j|k -
     * after <p>
     *
     * 2. moveRow(6,7,1); a|b|c|d|e|f|G|H|i|j|k - before a|G|H|b|c|d|e|f|i|j|k -
     * after <p>
     *
     * </pre>
     * </p>
     *
     * @param start the starting row index to be moved
     * @param end the ending row index to be moved
     * @param to the destination of the rows to be moved
     *
     * @exception ArrayIndexOutOfBoundsException if any of the elements would
     *            be moved out of the table's range
     */
    public void moveRow(int start, int end, int to)
        throws ArrayIndexOutOfBoundsException {

        int shift = to - start;
        int first;
        int last;

        if (shift < 0) {
            first = to;
            last = end;
        } else {
            first = start;
            last = (to + end) - start;
        }

        rotate(mListMap, first, last + 1, shift);
        mDelegateTableModel.moveRow(start, end, to);
    }

    /**
     * Removes the row at <code>row</code> from the model. Notification of the
     * row being removed will be sent to all the listeners.
     *
     * @param row the row index of the row to be removed
     *
     */
    public void removeRow(int row) {

        try {
            mDelegateTableModel.removeRow(row);
        } catch (ArrayIndexOutOfBoundsException e) {

            // re-throw with better message
            throw new ArrayIndexOutOfBoundsException("Could not remove row "
                                                     + row + ": out of bound.");
        }

        mListMap.remove(row);
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

    private static int gcd(int i, int j) {

        return (j == 0)
               ? i
               : gcd(j, i % j);
    }

    private static void rotate(ListMap v, int a, int b, int shift) {

        int size = b - a;
        int r = size - shift;
        int g = gcd(size, r);
        List keys = v.getKeyList();

        for (int i = 0; i < g; i++) {
            int to = i;
            Object tmpKey = keys.get(a + to);
            Object tmpVal = v.get(tmpKey);

            for (int from = (to + r) % size; from != i;
                    from = (to + r) % size) {
                Object key = keys.get(a + from);
                Object val = v.get(key);

                v.put(a + to, key, val);

                to = from;
            }

            v.put(a + to, tmpKey, tmpVal);
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/

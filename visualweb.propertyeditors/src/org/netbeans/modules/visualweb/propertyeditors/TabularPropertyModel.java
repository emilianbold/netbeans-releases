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
package org.netbeans.modules.visualweb.propertyeditors;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A model that defines methods for manipulating a tabular property value. A
 * tabular property is typically a matrix of primitive types (e.g. a list of
 * lists of strings, or a list of objects with any number of properties).
 *
 * @author gjmurphy
 */
public interface TabularPropertyModel extends TableModel {

    /**
     * Return the value of the table contents, as an object of type suitable
     * for the property being edited.
     */
    public Object getValue();

    /**
     * Set the initial value of the table contents.
     */
    public void setValue(Object value);

    /**
     * Adds a listener to the model, which is notified each time a change to the
     * model occurs.
     */
    public void addTableModelListener(TableModelListener listener);

    /**
     * Removes the listener specified.
     */
    public void removeTableModelListener(TableModelListener listener);

    /**
     * Returns the number of columns in the model. This number must not
     * change over the lifetime of the editor.
     *
     * @return The number of columns in the model.
     */
    public int getColumnCount();

    /**
     * Returns the display name of the column with the index specified.
     *
     * @param columnIndex
     * @return The display name of the column with the index specified.
     */
    public String getColumnName(int columnIndex);

    /**
     * Returns the type of the column with the index specified.
     *
     * @param columnIndex
     * @return The type of the column with the index specified.
     */
    public Class getColumnClass(int columnIndex);

    /**
     * Returns the current number of rows. The number may change over the
     * lifetime of the editor.
     *
     * @return The current number of rows.
     */
    public int getRowCount();

    /**
     * Returns the value of the cell at the row and column specified.
     *
     * @param rowIndex
     * @param columnIndex
     * @return The value of the cell at the row and column specified.
     */
    public Object getValueAt(int rowIndex, int columnIndex);

    /**
     * Returns <code>true</code> if the cell specified is editable.
     *
     * @param rowIndex
     * @param columnIndex
     * @return <code>true</code> if the cell specified is editable.
     */
    public boolean isCellEditable(int rowIndex, int columnIndex);

    /**
     * Change the value of the cell specified.
     *
     * @param newValue
     * @param rowIndex
     * @param columnIndex
     * @return <code>true</code> if the cell specified was modified.
     */
    public void setValueAt(Object newValue, int rowIndex, int columnIndex);

    /**
     * Returns true if rows may be added to this table model.
     *
     * @return <code>true</code> if rows may be added to this table model.
     */
    public boolean canAddRow();

    /**
     * Add a row to this model.
     *
     * @return <code>true</code> if a row was added to this table model.
     */
    public boolean addRow();

    /**
     * Returns <code>true</code> if the row specified may be moved to the
     * new location specified. Rows should be shift down one to make room
     * for the new row.
     */
    public boolean canMoveRow(int indexFrom, int indexTo);

    public boolean moveRow(int indexFrom, int indexTo);

    /**
     * Returns <code>true</code> if the row specified may be removed. Remaining
     * rows should be shifted up one.
     */
    public boolean canRemoveRow(int index);

    public boolean removeRow(int index);

    public boolean removeAllRows();
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.soa.mapper.common.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.table.AbstractTableModel;

/**
 * version 2002.08.02
 * author Copyright 2002 by UltiMeth Systems.  Permission for
 * use is granted if this copyright notice is preserved.
 *
 * TableRowModel implements a TableModel in terms of TableRows.
 *
 * @author    htung
 * @created   October 4, 2002
 */
public class TableRowModel
     extends AbstractTableModel {

    private AbstractList mTableRows = new ArrayList();
    private int mColumns;

    /**
     * TableRowModel constructor.
     *
     * @param columns  the number of columns.
     */
    public TableRowModel(int columns) {
        mColumns = columns;
    }

    /**
     * Gets a TableRow.
     *
     * @param row  the subscript of the row.
     * @return     TableRow - the TableRow.
     */
    public synchronized TableRow getRow(int row) {
        return (TableRow) mTableRows.get(row);
    }

    /**
     * Adds a TableRow.
     *
     * @param tableRow  the TableRow object.
     */
    public void addRow(TableRow tableRow) {
        insertRow(getRowCount(), tableRow);
    }

    /**
     * Inserts a TableRow.
     *
     * @param row       the subscript of the row.
     * @param tableRow  Description of the Parameter
     */
    public synchronized void insertRow(int row, TableRow tableRow) {
        mTableRows.add(row, tableRow);
        fireTableRowsInserted(row, row);
    }

    /**
     * Removes a TableRow.
     *
     * @param row  the subscript of the row.
     */
    public synchronized void removeRow(int row) {
        mTableRows.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * Removes all TableRows.
     */
    public synchronized void clear() {
        int rows = getRowCount();
        if (rows > 0) {
            mTableRows.clear();
            fireTableRowsDeleted(0, rows - 1);
        }
    }

    /**
     * Gets the number of TableRows (AbstractTableModel override).
     *
     * @return   int - the number of TableRows.
     */
    public synchronized int getRowCount() {
        return mTableRows.size();
    }

    /**
     * Gets the number of columns (AbstractTableModel override).
     *
     * @return   int - the number of columns.
     */
    public int getColumnCount() {
        return mColumns;
    }

    /**
     * Sets a TableRow column Object (AbstractTableModel override).
     *
     * @param row     the subscript of the Object's row.
     * @param column  the subscript of the Object's column.
     * @param obj     The new valueAt value
     */
    public void setValueAt(Object obj, int row, int column) {
        TableRow tableRow = (TableRow) mTableRows.get(row);
        tableRow.setValueAt(obj, column);
        fireTableCellUpdated(row, column);
    }

    /**
     * Gets a TableRow column Object (AbstractTableModel override).
     *
     * @param row     the subscript of the Object's row.
     * @param column  the subscript of the Object's column.
     * @return        Object - the selected Object.
     */
    public Object getValueAt(int row, int column) {
        return getRow(row).getValueAt(column);
    }

    /**
     * Sort an array of TableRows.
     *
     * @param column     the subscript of the column object to sort on.
     * @param ascending  true to sort ascending.
     */
    public synchronized void sortRows(final int column,
        final boolean ascending) {
        if (getRowCount() > 1) {
            TableRow[] rows = new TableRow[getRowCount()];
            // Comparator implementation
            Arrays.sort(mTableRows.toArray(rows),
                new Comparator() {
                    public int compare(Object row1, Object row2) {
                        int result = ((TableRow) row1).getSortableAt(column)
                            .compareTo(((TableRow) row2).getSortableAt(column));
                        return ascending ? result : -result;
                    }
                });
            mTableRows.clear();
            mTableRows.addAll(Arrays.asList(rows));
            fireTableRowsUpdated(0, rows.length - 1);
        }
    }
}

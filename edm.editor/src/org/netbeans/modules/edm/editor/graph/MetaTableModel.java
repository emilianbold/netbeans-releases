/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.edm.editor.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.netbeans.modules.edm.model.DBColumn;
import org.netbeans.modules.edm.editor.utils.NativeColumnOrderComparator;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.SQLDBColumn;


/**
 * This class provide a model for JMetaTableModel
 *
 * @author radval
 * @version $Revision$
 */
public class MetaTableModel extends AbstractTableModel {

    /**
     * Description of the column names This should come from some config file
     */
    private ArrayList disableRowsList = new ArrayList();

    private String[] columnNames = { "Column Name"};

    private ArrayList columns;

    //private DBTable table;

    private ArrayList cdws;

    /**
     * Creates a new instance of MetaTableModel
     */
    public MetaTableModel() {
    }

    /**
     * Creates a new instance of MetaTableModel, associating the given DBTable object with
     * it.
     *
     * @param table DBTable object to associate with this instance
     */
    public MetaTableModel(DBTable table, int tType) {
        List columnList = table.getColumnList();
        Collections.sort(columnList, NativeColumnOrderComparator.getInstance());
        columns = new ArrayList();

        Iterator it = columnList.iterator();
        int i = 0;
        cdws = new ArrayList();
        while (it.hasNext()) {
            SQLDBColumn columnData = (SQLDBColumn) it.next();
            if (columnData.isVisible()) {
                columns.add(columnData);
                ColumnDataWrapper cdw = new ColumnDataWrapper((DBColumn) columns.get(i));
                cdw.setFilter(cdw.hasFilter);
                cdws.add(cdw);
                i++;
            }
        }
    }

    /**
     * Gets the columnClass attribute of the ColumnTableModel object
     *
     * @param columnIndex Description of the Parameter
     * @return The columnClass value
     */
    public Class getColumnClass(int columnIndex) {
        Class klass = String.class;
        if (columnIndex == 3) {
            klass = Boolean.class;
        }
        return klass;
    }

    /**
     * Gets the columnCount attribute of the ColumnTableModel object
     *
     * @return The columnCount value
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Gets the columnName attribute of the ColumnTableModel object
     *
     * @param columnIndex Description of the Parameter
     * @return The columnName value
     */
    public String getColumnName(int columnIndex) {
        String ret = null;

        if (columnIndex >= 0 && columnIndex < columnNames.length) {
            ret = columnNames[columnIndex];
        }

        return ret;
    }

    /**
     * get the SQLColumnMetadata for a table row
     *
     * @param idx index of column whose metadata will be retrieved
     * @return SQLColumnMetadata instance associated with column
     */
    public DBColumn getSQLColumn(int idx) {
        return (DBColumn) columns.get(idx);
    }

    /**
     * get the SQLColumnMetadata for a table row
     *
     * @param idx index of column whose metadata will be retrieved
     * @return SQLColumnMetadata instance associated with column
     */
    public DBColumn getColumn(int idx) {
        return (DBColumn) columns.get(idx);
    }

    public void addColumn(DBColumn column) {
        columns.add(column);
        Collections.sort(columns, NativeColumnOrderComparator.getInstance());

        int pos = columns.indexOf(column);
        if (pos != -1) {
            this.fireTableRowsInserted(pos, pos);
        }
    }

    public boolean containsColumn(DBColumn column) {
        return this.columns.contains(column);
    }

    public void removeColumn(DBColumn column) {
        int row = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (column.equals(columns.get(i))) {
                row = i;
                break;
            }
        }
        //FIXME here order of deletion first notifys listener
        //then actually deletes the column, This is to make sure
        //if deleted column is linked to somewhere then that link is deleted
        //This is a ok fix and should find a better way to handle it
        if (row != -1) {
            this.fireTableRowsDeleted(row, row);
            columns.remove(row);
        }
    }

    public void updateColumn(DBColumn column) {
        int row = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (column.equals(columns.get(i))) {
                row = i;
                break;
            }
        }

        if (row != -1) {
            this.fireTableRowsUpdated(row, row);
        }
    }
   public void updateColumn(String newColName, SQLDBColumn newCol) {
        int row = -1;
        for (int i = 0; i < columns.size(); i++) {
            SQLDBColumn oldCol = (SQLDBColumn)columns.get(i);
            if (oldCol.getName().equals((newColName))) {
                row = i;
                break;
            }
        }

        if (row != -1) {
            columns.set(row, newCol);
            this.fireTableRowsUpdated(row, row);
        }
    }

    /**
     * Gets the rowCount attribute of the ColumnTableModel object
     *
     * @return The rowCount value
     */
    public int getRowCount() {
        return (columns == null) ? 0 : columns.size();
    }

    /**
     * Gets the valueAt attribute of the ColumnTableModel object
     *
     * @param rowIndex Description of the Parameter
     * @param columnIndex Description of the Parameter
     * @return The valueAt value
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        DBColumn column = (DBColumn) columns.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return column;
        }

        return String.valueOf(columnIndex + "?");

    }

    /**
     * Gets the cellEditable attribute of the ColumnTableModel object
     *
     * @param rowIndex Description of the Parameter
     * @param columnIndex Description of the Parameter
     * @return The cellEditable value
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        /*
         * if ((!disableRowsList.contains(new Integer(rowIndex))) && columnIndex == 3) {
         * return true; }
         */
        return false;
    }

    /**
     * Sets the valueAt attribute of the ColumnTableModel object
     *
     * @param aValue The new valueAt value
     * @param rowIndex The new valueAt value
     * @param columnIndex The new valueAt value
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        /*
         * if (columnIndex == 3) { /index of include column ColumnDescriptor cd =
         * (ColumnDescriptor) rows.get(rowIndex); this.fireTableDataChanged(); }
         */
    }

    /**
     * Description of the Method
     *
     * @param rowIndex Description of the Parameter
     */
    public void delistDisableRow(int rowIndex) {
        if (disableRowsList.contains(new Integer(rowIndex))) {
            disableRowsList.remove(new Integer(rowIndex));
        }
    }

    /**
     * Description of the Method
     *
     * @param rowIndex Description of the Parameter
     */
    public void disableRows(int rowIndex) {
        disableRowsList.add(new Integer(rowIndex));
    }

    /**
     * Description of the Method
     */
    public void resetDisabledRowsList() {
        disableRowsList.clear();
    }

    /**
     * Sets the filter for the column
     *
     * @param name - column name
     * @param filter - boolean flag
     */
    public void setFilter(String name, boolean filter) {
        int rowCount = this.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            DBColumn rowValue = (DBColumn) getValueAt(i, 0);
            if (rowValue != null && (rowValue.getName().trim()).equals(name.trim())) {
                ((ColumnDataWrapper) cdws.get(i)).setFilter(filter);
            }
        }
    }

    /**
     * Resets the filter flags in all columns, each time we click ok.
     */
    public void resetFilters() {
        int rowCount =  0;
        if (cdws != null) {
            rowCount = cdws.size();
        }

        for (int i = 0; i < rowCount; i++) {
            ((ColumnDataWrapper) cdws.get(i)).setFilter(false);
        }
    }

    /**
     * Sets validation flag for the column name passed.
     *
     * @param name
     * @param filter
     */
    public void setValidationFlag(String name, boolean flag) {
        int rowCount = this.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            DBColumn rowValue = (DBColumn) getValueAt(i, 0);
            if (rowValue != null && (rowValue.getName().trim()).equals(name.trim())) {
                ((ColumnDataWrapper) cdws.get(i)).setValidationPresent(flag);
            }
        }
    }

    /**
     * Resets the validation flag.
     */
    public void resetValidationFlag() {
        int rowCount = 0;
        if (cdws != null) {
            rowCount = cdws.size();
        }

        for (int i = 0; i < rowCount; i++) {
            ((ColumnDataWrapper) cdws.get(i)).setValidationPresent(false);
        }
    }

    /**
     * Tells whether the filter has been applied to the particular column.
     *
     * @param row - row index
     * @return bool
     */
    public boolean isFiltered(int row) {
        if ((cdws != null) && (cdws.size() > row )){
            ColumnDataWrapper colMetaData = ((ColumnDataWrapper) cdws.get(row));
            return colMetaData.hasFilter();
        }else {
            return false;
        }

    }

    /**
     * Whether perticular column is part of Validation condition.
     *
     * @param row - row index
     * @return bool
     */
    public boolean isValidationPresent(int row) {
        if ((cdws != null) && (cdws.size() > row )){
            ColumnDataWrapper colMetaData = ((ColumnDataWrapper) cdws.get(row));
            return colMetaData.isValidationPresent();
        }else {
            return false;
        }
    }

    class ColumnDataWrapper {
        private DBColumn colData;
        private boolean hasFilter = false;
        private boolean validationPresent = false;

        /**
         * Constructor
         *
         * @param colData - DBColumn to be wrapped.
         */
        public ColumnDataWrapper(DBColumn colData) {
            this.colData = colData;
        }

        /**
         * Gets name of wrapped column.
         *
         * @return column name
         */
        public String getColumnName() {
            return colData.getName();
        }

        /**
         * Gets String representation of SQL type of wrapped column.
         *
         * @return SQL type string
         */
        public String getTypeName() {
            return colData.getJdbcTypeString();
        }

        /**
         * Gets precision of wrapped column.
         *
         * @return precision
         */
        public int getPrecision() {
            return colData.getPrecision();
        }

        /**
         * Gets scale of wrapped column.
         *
         * @return scale
         */
        public int getScale() {
            return colData.getScale();
        }

        /**
         * Gets JDBC SQL type.
         *
         * @return int value representing JDBC SQL type
         * @see java.sql.Types
         */
        public int getJdbcSQLType() {
            return colData.getJdbcType();
        }

        /**
         * Sets whether this column has a filter
         *
         * @param filter true if filtered, false otherwise
         */
        public void setFilter(boolean filter) {
            hasFilter = filter;
        }

        /**
         * Indicates whether the column has filter.
         *
         * @return true if filtered, false otherwise
         */
        public boolean hasFilter() {
            return hasFilter;
        }

        /**
         * @return Returns the validationPresent.
         */
        public boolean isValidationPresent() {
            return validationPresent;
        }

        /**
         * @param validationPresent The validationPresent to set.
         */
        public void setValidationPresent(boolean validationPresent) {
            this.validationPresent = validationPresent;
        }
    }
}


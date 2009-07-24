/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is virtual table description. This table description is
 * used on all levels of communication.
 * <p>
 * <ul>
 * <li> It should be used to describe data collected, see {@link org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration}
 * <li> It is used in storage 
 * <li> It is used by Visualizer as table description to be visualized
 * </ul>
 * </p>
 */
public final class DataTableMetadata {

    private final String name;
    private final List<Column> columns;
    private final List<Column> indexedColumns;
    private final List<String> columnNames;
    private final List<DataTableMetadata> sourceTables;
    private final String statement;

    /**
     * Creates new table description with the name <code>name</code> and using <code>columns</code> as table column descriptions
     * @param name  table name
     * @param columns  table columns
     * @param indexedColumns  columns to create indices for
     */
    public DataTableMetadata(String name, List<Column> columns, List<Column> indexedColumns) {
        this(name, columns, indexedColumns, null, null);
    }

    /**
     * Creates new VIEW description with the name <code>name</code>,using <code>columns</code> as table column descriptions,
     *  <code>statement</code> string is info about gettig data and <code>sourceTables</code> is the list of tables
     * this VIEW is built from
     * @param name view name
     * @param columns columns description
     * @param statement string which represents infor about getting data for this VIEW
     * @param sourceTables tables this VIEW is built on the base of
     */
    public DataTableMetadata(String name, List<Column> columns, String statement, List<DataTableMetadata> sourceTables) {
        this(name, columns, null, statement, sourceTables);
    }

    private DataTableMetadata(String name, List<Column> columns, List<Column> indexedColumns, String statement, List<DataTableMetadata> sourceTables) {
        this.name = name;
        this.columns = columns;
        this.indexedColumns = indexedColumns == null? Collections.<Column>emptyList() : indexedColumns;
        this.statement = statement;
        this.sourceTables = sourceTables;
        columnNames = new ArrayList<String>(columns.size());
        for (Column c : columns) {
            columnNames.add(c.getColumnName());
        }
    }

    /**
     * Return column names
     * @return column names
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Return source tables this table is based on the top of, <code>null</code> if no such tables
     * @return source tables this table is based on the top of, <code>null</code> if no such tables
     */
    public List<DataTableMetadata> getSourceTables() {
        return sourceTables;
    }

    /**
     * Return table name
     * @return table name
     */
    public String getName() {
        return name;
    }

    /**
     * Return columns list
     * @return columns list
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Return indexed columns list
     * @return indexed columns list, never <code>null</code>
     */
    public List<Column> getIndexedColumns() {
        return indexedColumns;
    }

    /**
     * Return view statement if any
     * @return view statement if any
     */
    public String getViewStatement() {
        return statement;
    }

    /**
     * Return column description by the column name, <code>null</code> if there is no column with the name <code>columnName</code>
     * @param columnName column name to get Column for
     * @return column if exists, <code>null</code> if not
     */
    public Column getColumnByName(String columnName) {
        for (Column c : columns) {
            if (c.getColumnName().equals(columnName)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns column count
     * @return column count
     */
    public int getColumnsCount() {
        return columns.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(' ');
        if (columns != null) {
            for (Column column : columns) {
                sb.append("\n\t"); //NOI18N
                sb.append(column);
            }
        }
        return sb.toString();
    }

    /**
     * Column description 
     */
    public static final class Column {

        final String name;
        final Class columnClass;
        final String shortUName;
        final String longUName;;
        final String expression;

        /**
         * Creates new column
         * @param name column name
         * @param columnClass class of this column
         */
        public Column(String name, Class columnClass) {
            this(name, columnClass, name, null);
        }

        /**
         * Creates new column
         * @param name column name
         * @param columnClass column class
         * @param shortName displayed name
         * @param expression expression which is used to calculate column value, for examle there can be "column2*column3"
         */
        public Column(String name, Class columnClass, String shortName, String expression) {
            this(name, columnClass, shortName, shortName, expression);

        }

        /**
         * Creates new column
         * @param name column name
         * @param columnClass column class
         * @param shortName displayed name
         * @param longName long  name, can be used as a tooltip
         * @param expression expression which is used to calculate column value, for examle there can be "column2*column3"
         */
        public Column(String name, Class columnClass, String shortName, String longName, String expression) {
            this.name = name;
            this.columnClass = columnClass;
            this.shortUName = shortName;
            this.longUName = longName;
            this.expression = expression;
        }

        /**
         * Return column class
         * @return column class
         */
        public Class<?> getColumnClass() {
            return columnClass;
        }

        /**
         * Return column name
         * @return column name
         */
        public String getColumnName() {
            return name;
        }

        /**
         * Long column displayed name, in terms of UI this is a tooltip
         * @return  long column displayed name
         */
        public String getColumnLongUName(){
            return longUName;
        }

        /**
         * Column displayed (user) name
         * @return column name
         */
        public String getColumnUName() {
            return shortUName;
        }

        /**
         * Return expression used to calculate column value if exists, <code>null</code> otherwise
         * @return expression
         */
        public String getExpression() {
            return expression;
        }

        @Override
        public String toString() {
            return name + " : (" + getColumnClass().getName() + ")"; //NOI18N
        }
    }

}


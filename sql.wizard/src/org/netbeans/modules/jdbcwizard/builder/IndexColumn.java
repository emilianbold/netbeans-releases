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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * Captures database index metadata associated with a specific database table column.
 * 
 * @author
 */
public class IndexColumn implements Comparable {
    private static final String RS_INDEX_NAME = "INDEX_NAME"; // NOI18N

    private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N

    private static final String RS_NON_UNIQUE = "NON_UNIQUE"; // NOI18N

    private static final String RS_TYPE = "TYPE"; // NOI18N

    private static final String RS_ORDINAL = "ORDINAL_POSITION"; // NOI18N

    private static final String RS_ASC_OR_DESC = "ASC_OR_DESC"; // NOI18N

    private static final String RS_CARDINALITY = "CARDINALITY"; // NOI18N

    /* Name of index */
    private String name;

    /* Name of column associated with this index */
    private String columnName;

    /* Index type: tableIndexClustered, tableIndexHashed, tableIndexOther */
    private int type;

    /* Column sequence number within a composite index */
    private int ordinalPosition;

    /* Indicates whether index is unique */
    private boolean unique = false;

    /* Indicates sort order, if any, of index */
    private String sortSequence;

    /* Indicates number of unique values in index */
    private int cardinality;

    /**
     * Creates a List of IndexColumn instances from the given ResultSet.
     * 
     * @param rs ResultSet containing index metadata as obtained from DatabaseMetaData
     * @return List of IndexColumn instances based from metadata in rs'
     * @throws SQLException if SQL error occurs while reading in data from given ResultSet
     */
    public static List createIndexList(final ResultSet rs) throws SQLException {
        List indices = Collections.EMPTY_LIST;

        if (rs != null && rs.next()) {
            indices = new ArrayList();
            do {
                final IndexColumn newIndex = new IndexColumn(rs);

                // Ignore statistics indexes as they are relevant only to the
                // DB which sourced this metadata.
                if (newIndex.getType() != DatabaseMetaData.tableIndexStatistic) {
                    indices.add(newIndex);
                }
            } while (rs.next());
        }

        return indices;
    }

    IndexColumn(final ResultSet rs) throws SQLException {
        if (rs == null) {
            // Locale locale = Locale.getDefault();
            // ResourceBundle cMessages =
            // ResourceBundle.getBundle("com/sun/jbi/ui/devtool/jdbc/builder/Bundle", locale); // NO
            // i18n
            final ResourceBundle cMessages = NbBundle.getBundle(IndexColumn.class);// NO i18n
            throw new IllegalArgumentException(cMessages.getString("ERROR_VALID_RS") + "(ERROR_VALID_RS)"); // NOI18N
        }

        this.name = rs.getString(IndexColumn.RS_INDEX_NAME);
        this.columnName = rs.getString(IndexColumn.RS_COLUMN_NAME);

        this.unique = !rs.getBoolean(IndexColumn.RS_NON_UNIQUE);
        this.type = rs.getShort(IndexColumn.RS_TYPE);

        this.ordinalPosition = rs.getShort(IndexColumn.RS_ORDINAL);
        this.sortSequence = rs.getString(IndexColumn.RS_ASC_OR_DESC);
        this.cardinality = rs.getInt(IndexColumn.RS_CARDINALITY);
    }

    /**
     * Creates a new instance of IndexColumn using the given values.
     * 
     * @param colName name of column
     * @param indexName name of index
     * @param indexType index type, as defined in DatabaseMetaData; note that tableIndexStatistic is
     *            not a valid type for IndexColumn
     * @param colOrder sequence of this column within the (composite) index; should be 1 for
     *            single-column indexes
     * @param isUnique true if index enforces unique values, false otherwise
     * @param sortOrder "A" for ascending order, "D" for descending order, null for no defined sort
     *            order
     * @param indexCardinality count of unique values contained in the index
     */
    public IndexColumn(final String colName, final String indexName, final short indexType, final short colOrder, final boolean isUnique,
            final String sortOrder, final int indexCardinality) {

        final ResourceBundle cMessages = NbBundle.getBundle(IndexColumn.class);// NO i18n
        if (colName == null) {
            throw new IllegalArgumentException(cMessages.getString("ERROR_PARAMETER_NAME") + "(ERROR_PARAMETER_NAME)"); // NOI18N
        }

        if (indexType != DatabaseMetaData.tableIndexClustered && indexType != DatabaseMetaData.tableIndexHashed
                && indexType != DatabaseMetaData.tableIndexOther) {
            throw new IllegalArgumentException(MessageFormat.format(cMessages.getString("ERROR_INDEX_TYPE"),
                    new Object[] { new Integer(this.type) })
                    + "(ERROR_INDEX_TYPE)"); // NOI18N
        }

        this.name = indexName;
        this.columnName = colName;

        this.type = indexType;
        this.ordinalPosition = colOrder;
        this.unique = isUnique;

        this.sortSequence = sortOrder;
        this.cardinality = indexCardinality;
    }

    public IndexColumn(final IndexColumn iCol) {

        final ResourceBundle cMessages = NbBundle.getBundle(IndexColumn.class);// NO i18n
        if (iCol.getColumnName() == null) {
            throw new IllegalArgumentException(cMessages.getString("ERROR_PARAMETER_NAME") + "(ERROR_PARAMETER_NAME)"); // NOI18N
        }

        if (iCol.getType() != DatabaseMetaData.tableIndexClustered
                && iCol.getType() != DatabaseMetaData.tableIndexHashed
                && iCol.getType() != DatabaseMetaData.tableIndexOther) {
            throw new IllegalArgumentException(MessageFormat.format(cMessages.getString("ERROR_INDEX_TYPE"),
                    new Object[] { new Integer(this.type) })
                    + "(ERROR_INDEX_TYPE)"); // NOI18N
        }

        this.name = iCol.getIndexName();
        this.columnName = iCol.getColumnName();

        this.type = iCol.getType();
        this.ordinalPosition = iCol.getOrdinalPosition();
        this.unique = iCol.isUnique();

        this.sortSequence = iCol.getSortOrder();
        this.cardinality = iCol.getCardinality();
    }

    private IndexColumn() {
    }

    /**
     * Gets index name.
     * 
     * @return index name
     */
    public String getIndexName() {
        return this.name;
    }

    /**
     * Gets column name.
     * 
     * @return name of column
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Gets index type.
     * 
     * @return index type, one of DatabaseMetaData.tableIndexClustered,
     *         DatabaseMetaData.tableIndexHashed, or DatabaseMetaData.tableIndexOther
     * @see java.sql.DatabaseMetaData.tableIndexClustered
     * @see java.sql.DatabaseMetaData.tableIndexHashed
     * @see java.sql.DatabaseMetaData.tableIndexOther
     */
    public int getType() {
        return this.type;
    }

    /**
     * Indicates whether the index associated with this column is unique.
     * 
     * @return true if index is unique, false otherwise
     */
    public boolean isUnique() {
        return this.unique;
    }

    /**
     * Gets ordinal position of this column within a (composite) index. For an index associated with
     * a single column, this should be 1.
     * 
     * @return ordinal position of column within the composite index; 1 if
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    /**
     * Gets sort order, if any, associated with this index.
     * 
     * @return "A" for ascending, "D" for descending, or null if no sort order is defined
     */
    public String getSortOrder() {
        return this.sortSequence;
    }

    /**
     * Gets index cardinality. It represents the number of unique values associated with the index
     * (for clustered, hashed, or other index types).
     * 
     * @return count of unique values in the associated index
     */
    public int getCardinality() {
        return this.cardinality;
    }

    /**
     * Compares this instance against the given object with respect to some "natural" ordering.
     * Useful for sorting IndexColumn instances for UI display. Note: this class has a natural
     * ordering that is inconsistent with equals.
     * 
     * @param o Object against which this will be compared
     * @return -1 if this is lesser than o; 0 if this is equal to o; 1 if this is greater than o
     */
    public int compareTo(final Object o) {
        if (o == this) {
            return 0;
        }

        final IndexColumn idx = (IndexColumn) o;
        if (idx != null) {
            if (this.name != null) {
                return idx.name != null ? this.name.compareTo(idx.name) : -1;
            } else if (this.columnName != null) {
                return idx.columnName != null ? this.columnName.compareTo(idx.columnName) : -1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }
}

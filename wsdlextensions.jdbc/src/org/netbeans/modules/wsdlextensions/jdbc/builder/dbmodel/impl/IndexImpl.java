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
package org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl;

import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBColumn;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.Index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * Implements Index interface.
 * 
 * @author
 */
public class IndexImpl implements Index, Cloneable {

    /* Name of this index */
    private String name;

    /* Type of index, as enumerated in DatabaseMetaData */
    private int type;

    /* DBTable to which this Index belongs */
    private DBTable parent;

    /* List of column names in key sequence order. */
    private List columnNames;

    /* Indicates whether index is unique */
    private boolean unique = false;

    /* Indicates sort order, if any, of index */
    private String sortSequence;

    /* Indicates number of unique values in index */
    private int cardinality;

    private IndexImpl() {
        this.name = null;
        this.columnNames = new ArrayList();
    }

    /**
     * Creates a new instance of Index with the given key name and attributes.
     * 
     * @param indexName name of this Index, must be non-empty
     * @param indexType type of Index, as enumerated in java.sql.DatabaseMetaData; one of
     *            tableIndexClustered, tableIndexHashed, or tableIndexOther
     * @param isUnique true if index enforces uniqueness, false otherwise
     * @param sortOrder 'A' for ascending, 'D' for descending, null if undefined
     * @param indexCardinality cardinality of this index
     * @see java.sql.DatabaseMetaData#tableIndexClustered
     * @see java.sql.DatabaseMetaData#tableIndexHashed
     * @see java.sql.DatabaseMetaData#tableIndexOther
     */
    public IndexImpl(final String indexName, final int indexType, final boolean isUnique, final String sortOrder, final int indexCardinality) {
        this();

        if (indexName == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(IndexImpl.class);

            throw new IllegalArgumentException(cMessages.getString("ERROR_INDEX_NAME") + "(ERROR_INDEX_NAME)");// NO
            // i18n
        }

        this.name = indexName;
        this.type = indexType;
        this.unique = isUnique;
        this.sortSequence = sortOrder;
        this.cardinality = indexCardinality;
    }

    /**
     * Creates a new instance of Index with the given key name and attributes, and referencing the
     * column names in the given List.
     * 
     * @param indexName name of this Index, must be non-empty
     * @param indexType type of Index, as enumerated in java.sql.DatabaseMetaData; one of
     *            tableIndexClustered, tableIndexHashed, or tableIndexOther
     * @param isUnique true if index enforces uniqueness, false otherwise
     * @param sortOrder 'A' for ascending, 'D' for descending, null if undefined
     * @param indexCardinality cardinality of this index
     * @param indexColumnNames List of Column objects, or column names in sequential order,
     *            depending on state of isStringList
     * @param isStringList true if indexColumnName contains column names in sequential order, false
     *            if it contains Column objects which need to be sorted in sequential order.
     * @see java.sql.DatabaseMetaData#tableIndexClustered
     * @see java.sql.DatabaseMetaData#tableIndexHashed
     * @see java.sql.DatabaseMetaData#tableIndexOther
     */
    public IndexImpl(final String indexName, final int indexType, final boolean isUnique, final String sortOrder, final int indexCardinality,
            final List indexColumnNames, final boolean isStringList) {
        this(indexName, indexType, isUnique, sortOrder, indexCardinality);
        this.setColumnNames(indexColumnNames, isStringList);
    }

    /**
     * Creates a new instance of Index, cloning the contents of the given Index implementation
     * instance.
     * 
     * @param src Index instance to be cloned
     */
    public IndexImpl(final Index src) {
        this();
        this.copyFrom(src);
    }

    /*
     * IMPLEMENTATION OF com.stc.model.database.Index
     */

    /**
     * @see com.stc.model.database.Index#getName
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.stc.model.database.Index#getParent
     */
    public DBTable getParent() {
        return this.parent;
    }

    /**
     * @see com.stc.model.database.Index#getType
     */
    public int getType() {
        return this.type;
    }

    /**
     * @see com.stc.model.database.Index#contains(String)
     */
    public boolean isUnique() {
        return this.unique;
    }

    /**
     * @see com.stc.model.database.Index#getSortSequence
     */
    public String getSortSequence() {
        return this.sortSequence;
    }

    /**
     * @see com.stc.model.database.Index#getCardinality
     */
    public int getCardinality() {
        return this.cardinality;
    }

    /**
     * @see com.stc.model.database.Index#getColumnNames
     */
    public List getColumnNames() {
        return Collections.unmodifiableList(this.columnNames);
    }

    /**
     * @see com.stc.model.database.Index#getColumnCount
     */
    public int getColumnCount() {
        return this.columnNames.size();
    }

    /**
     * @see com.stc.model.database.Index#getSequence(DBColumn)
     */
    public int getSequence(final DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return this.getSequence(col.getName().trim());
    }

    /**
     * @see com.stc.model.database.Index#getColumnName
     */
    public String getColumnName(final int iColumn) {
        return (String) this.columnNames.get(iColumn);
    }

    /**
     * @see com.stc.model.database.Index#contains(String)
     */
    public boolean contains(final String columnName) {
        return this.columnNames.contains(columnName);
    }

    /**
     * @see com.stc.model.database.Index#contains(DBColumn)
     */
    public boolean contains(final DBColumn col) {
        return col != null ? this.contains(col.getName()) : false;
    }

    /*
     * Setter and non-API helper methods
     */

    /**
     * Sets reference to JDBCTable that owns this primary key.
     * 
     * @param newParent new parent of this primary key.
     */
    public void setParent(final DBTable newParent) {
        this.parent = newParent;
    }

    public void setColumnNames(final List indexColumnNames, final boolean isStringList) {
        if (isStringList) {
            this.columnNames.addAll(indexColumnNames);
        } else {
            Collections.sort(indexColumnNames);
            final Iterator iter = indexColumnNames.iterator();
            while (iter.hasNext()) {
                final IndexImpl.Column col = (IndexImpl.Column) iter.next();
                this.columnNames.add(col.getName());
            }
        }
    }

    /**
     * Gets the ordinal position of the column, if any, associated with the given columnName.
     * 
     * @param columnName name of column whose position is desired
     * @return (zero-based) position of given column, or -1 if no column by the given columnName
     *         could be located
     */
    public int getSequence(final String columnName) {
        return this.columnNames.indexOf(columnName);
    }

    /**
     * Create a clone of this PrimaryKeyImpl.
     * 
     * @return cloned copy of DBColumn.
     */
    public Object clone() {
        try {
            final IndexImpl impl = (IndexImpl) super.clone();
            impl.columnNames = new ArrayList(this.columnNames);

            return impl;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    public boolean equals(final Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof IndexImpl)) {
            return false;
        }

        final IndexImpl ref = (IndexImpl) refObj;

        boolean result = this.name != null ? this.name.equals(ref.name) : ref.name == null;

        result &= this.type == ref.type && this.cardinality == ref.cardinality && this.unique == ref.unique;

        result &= this.sortSequence != null ? this.sortSequence.equals(ref.sortSequence) : ref.sortSequence == null;

        result &= this.columnNames != null ? this.columnNames.equals(ref.columnNames) : ref.columnNames == null;

        return result;
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used in equals()
     * for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = this.name != null ? this.name.hashCode() : 0;
        myHash += this.columnNames != null ? this.columnNames.hashCode() : 0;

        myHash += this.type + this.cardinality + (this.unique ? 1 : 0);
        myHash += this.sortSequence != null ? this.sortSequence.hashCode() : 0;

        return myHash;
    }

    /**
     * Replaces the current List of column names with the contents of the given String array.
     * 
     * @param newColNames array of names to supplant current list of column names
     */
    public void setColumnNames(final String[] newColNames) {
        if (newColNames == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(IndexImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_COL_NAMES") + "(ERROR_COL_NAMES)");// NO
            // i18n
        }

        this.columnNames.clear();
        for (int i = 0; i < newColNames.length; i++) {
            this.columnNames.add(newColNames[i]);
        }
    }

    private void copyFrom(final Index src) {
        this.name = src.getName();
        this.parent = src.getParent();

        this.columnNames.clear();
        this.columnNames.addAll(src.getColumnNames());

        this.type = src.getType();
        this.unique = src.isUnique();
        this.sortSequence = src.getSortSequence();
        this.cardinality = src.getCardinality();
    }

    /**
     * Intermediate container class to hold metadata of columns involved in a particular Index.
     */
    public static class Column implements Comparable {
        private String name;

        private int sequence;

        /**
         * Creates a new instance of Index.Column with the given name and sequence.
         * 
         * @param colName name of new column
         * @param colSequence sequence of new column w.r.t. other columns
         */
        public Column(final String colName, final int colSequence) {
            final ResourceBundle cMessages = NbBundle.getBundle(IndexImpl.class);
            if (colName == null || colName.trim().length() == 0) {
                throw new IllegalArgumentException(cMessages.getString("ERROR_COL_NAME") + "(ERROR_COL_NAME)");// NO
                // i18n
            }

            if (colSequence <= 0) {
                throw new IllegalArgumentException(cMessages.getString("ERROR_COL_SEQ") + "(ERROR_COL_SEQ)");// NO
                // i18n
            }

            this.name = colName;
            this.sequence = colSequence;
        }

        /**
         * Creates a new instance of Index.Column with the given DBColumn and sequence.
         * 
         * @param col new DBColumn
         * @param colSequence sequence of new column w.r.t. other columns
         */
        public Column(final DBColumn col, final int colSequence) {
            this(col.getName(), colSequence);
        }

        /**
         * Gets name of this column.
         * 
         * @return column name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Gets sequence of this column with respect to others in the associated index.
         * 
         * @return column index
         */
        public int getSequence() {
            return this.sequence;
        }

        /**
         * Compares this object with the specified object for order. Returns a negative integer,
         * zero, or a positive integer as this object is less than, equal to, or greater than the
         * specified object.
         * <p>
         * Note: this class has a natural ordering that is inconsistent with equals.
         * 
         * @param o the Object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less than,
         *         equal to, or greater than the specified object.
         */
        public int compareTo(final Object o) {
            return this.sequence - ((Column) o).sequence;
        }
    }
}

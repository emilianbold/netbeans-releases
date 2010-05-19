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
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.PrimaryKey;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.ForeignKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/**
 * Implements ForeignKey interface.
 * 
 * @author
 */
public class ForeignKeyImpl implements ForeignKey, Cloneable {


    /* Name of this key; may be null */
    private String fkName;

    /* Name of corresponding primary key; may be null */
    private String pkName;

    /* List of column names for this foreign key in key sequence order. */
    private List fkColumnNames = new ArrayList();

    /*
     * List of column names of corresponding primary key columns, in key sequence order.
     */
    private List pkColumnNames = new ArrayList();

    private String pkTable;

    private String pkSchema;

    private String pkCatalog;

    private int updateRule;

    private int deleteRule;

    private int deferrability;

    /* DBTable to which this PK belongs */
    private DBTable parent;

    /**
     * Creates a new instance of ForeignKey with the given key name and referencing the column names
     * in the given List.
     * 
     * @param fkTable DBTable that owns this FK instance
     * @param foreignKeyName name, if any, of this ForeignKeyImpl
     * @param primaryKeyName name, if any, of PK associated with this ForeignKeyImpl
     * @param primaryKeyTable table owning associated PK
     * @param primaryKeySchema schema containing table which owns associated PK; may be null
     * @param primaryKeyCatalog catalog containing table which owns associated PK; may be null
     * @param updateFlag update cascade rule
     * @param deleteFlag delete cascade rule
     * @param deferFlag flag indicating deferrability of application of cascade rules
     */
    public ForeignKeyImpl(final DBTable fkTable, final String foreignKeyName, final String primaryKeyName, final String primaryKeyTable,
            final String primaryKeySchema, final String primaryKeyCatalog, final int updateFlag, final int deleteFlag, final int deferFlag) {
        this.parent = fkTable;
        this.fkName = foreignKeyName;
        this.pkName = primaryKeyName;

        this.pkTable = primaryKeyTable;
        this.pkSchema = primaryKeySchema;
        this.pkCatalog = primaryKeyCatalog;

        this.updateRule = updateFlag;
        this.deleteRule = deleteFlag;
        this.deferrability = deferFlag;
    }

    /**
     * Creates a new instance of ForeignKeyImpl, cloning the contents of the given ForeignKey
     * implementation instance.
     * 
     * @param src ForeignKey to be cloned
     */
    public ForeignKeyImpl(final ForeignKey src) {
        if (src == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(ForeignKeyImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_FK") + "ERROR_NULL_FK");// NO
            // i18n
        }

        this.copyFrom(src);
    }

    /**
     * @see com.stc.model.database.ForeignKey#getName
     */
    public String getName() {
        return this.fkName;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getPKName
     */
    public String getPKName() {
        return this.pkName;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getColumnNames
     */
    public List getColumnNames() {
        return Collections.unmodifiableList(this.fkColumnNames);
    }

    public void setColumnNames(final ForeignKeyImpl.Column[] columns) {
        this.fkColumnNames.clear();
        this.pkColumnNames.clear();

        if (columns == null) {
            return;
        }

        for (int i = 0; i < columns.length; i++) {
            final Column col = columns[i];
            this.fkColumnNames.add(col.getName());
            this.pkColumnNames.add(col.getPKColumnName());
        }
    }

    public void setColumnNames(final List columns) {
        this.fkColumnNames.clear();
        this.pkColumnNames.clear();

        if (columns == null) {
            return;
        }

        for (final Iterator it = columns.iterator(); it.hasNext();) {
            final ForeignKeyImpl.Column col = (ForeignKeyImpl.Column) it.next();
            this.fkColumnNames.add(col.getName());
            this.pkColumnNames.add(col.getPKColumnName());
        }
    }

    public void setColumnNames(final List fkColumns, final List pkColumns) {
        this.fkColumnNames.clear();
        this.pkColumnNames.clear();

        if (fkColumns == null && pkColumns == null) {
            return;
        }

        if (fkColumns.size() != pkColumns.size()) {
            final ResourceBundle cMessages = NbBundle.getBundle(ForeignKeyImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_SIZE_PK_FK") + "ERROR_SIZE_PK_FK");// NO
            // i18n
        }

        for (final ListIterator it = fkColumns.listIterator(); it.hasNext();) {
            final String fkName = (String) it.next();
            final String pkName = (String) pkColumns.get(it.previousIndex());

            if (fkName != null && pkName != null) {
                this.fkColumnNames.add(fkName);
                this.pkColumnNames.add(pkName);
            }
        }
    }

    /**
     * @see com.stc.model.database.ForeignKey#getPKColumnNames
     */
    public List getPKColumnNames() {
        return Collections.unmodifiableList(this.pkColumnNames);
    }

    /**
     * @see com.stc.model.database.ForeignKey#getMatchingPKColumn
     */
    public String getMatchingPKColumn(final String fkColumnName) {
        final ListIterator it = this.fkColumnNames.listIterator();
        while (it.hasNext()) {
            final String colName = (String) it.next();
            if (colName.equals(fkColumnName.trim())) {
                return (String) this.pkColumnNames.get(it.previousIndex());
            }
        }

        return null;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getPKTable
     */
    public String getPKTable() {
        return this.pkTable;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getPKSchema
     */
    public String getPKSchema() {
        return this.pkSchema;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getPKCatalog
     */
    public String getPKCatalog() {
        return this.pkCatalog;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getUpdateRule
     */
    public int getUpdateRule() {
        return this.updateRule;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getDeleteRule
     */
    public int getDeleteRule() {
        return this.deleteRule;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getDeferrability
     */
    public int getDeferrability() {
        return this.deferrability;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getParent
     */
    public DBTable getParent() {
        return this.parent;
    }

    public void setParent(final DBTable newParent) {
        this.parent = newParent;
    }

    /**
     * @see com.stc.model.database.ForeignKey#contains(String)
     */
    public boolean contains(final String fkColumnName) {
        return this.fkColumnNames.contains(fkColumnName);
    }

    /**
     * @see com.stc.model.database.ForeignKey#contains(DbColumn)
     */
    public boolean contains(final DBColumn fkCol) {
        return this.contains(fkCol.getName());
    }

    /**
     * @see com.stc.model.database.ForeignKey#references
     */
    public boolean references(final PrimaryKey pk) {
        if (pk == null) {
            return false;
        }

        final List targetColNames = pk.getColumnNames();
        final DBTable targetTable = pk.getParent();

        return this.references(targetTable) && targetColNames.containsAll(this.pkColumnNames)
                && this.pkColumnNames.containsAll(targetColNames);
    }

    /**
     * @see com.stc.model.database.ForeignKey#references(DBTable)
     */
    public boolean references(final DBTable pkTable) {
        return pkTable != null ? this.references(pkTable.getName(), pkTable.getSchema(), pkTable.getCatalog()) : false;
    }

    /**
     * @see com.stc.model.database.ForeignKey#references(String, String, String)
     */
    public boolean references(final String pkTableName, final String pkSchemaName, final String pkCatalogName) {
        final boolean tableMatches = pkTableName != null ? pkTableName.equals(this.pkTable) : this.pkTable == null;

        final boolean schemaMatches = pkSchemaName != null ? pkSchemaName.equals(this.pkSchema) : this.pkSchema == null;

        final boolean catalogMatches = pkCatalogName != null ? pkCatalogName.equals(this.pkCatalog) : this.pkCatalog == null;

        return tableMatches && schemaMatches && catalogMatches;
    }

    /**
     * Create a clone of this PrimaryKeyImpl.
     * 
     * @return cloned copy of DBColumn.
     */
    public Object clone() {
        try {
            final ForeignKeyImpl impl = (ForeignKeyImpl) super.clone();
            impl.pkColumnNames = new ArrayList(this.pkColumnNames);
            impl.fkColumnNames = new ArrayList(this.fkColumnNames);

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

        if (!(refObj instanceof ForeignKeyImpl)) {
            return false;
        }

        final ForeignKeyImpl ref = (ForeignKeyImpl) refObj;

        boolean result = this.fkName != null ? this.fkName.equals(ref.fkName) : ref.fkName == null;

        result &= this.pkName != null ? this.pkName.equals(ref.pkName) : ref.pkName == null;

        result &= this.pkTable != null ? this.pkTable.equals(ref.pkTable) : ref.pkTable == null;

        result &= this.pkSchema != null ? this.pkSchema.equals(ref.pkSchema) : ref.pkSchema == null;

        result &= this.pkCatalog != null ? this.pkCatalog.equals(ref.pkCatalog) : ref.pkCatalog == null;

        result &= this.updateRule == ref.updateRule && this.deleteRule == ref.deleteRule
                && this.deferrability == ref.deferrability;

        result &= this.pkColumnNames != null ? this.pkColumnNames.equals(ref.pkColumnNames) : ref.pkColumnNames != null;

        result &= this.fkColumnNames != null ? this.fkColumnNames.equals(ref.fkColumnNames) : ref.fkColumnNames != null;

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
        int myHash = this.fkName != null ? this.fkName.hashCode() : 0;

        myHash += this.pkName != null ? this.pkName.hashCode() : 0;
        myHash += this.pkTable != null ? this.pkTable.hashCode() : 0;
        myHash += this.pkSchema != null ? this.pkSchema.hashCode() : 0;
        myHash += this.pkCatalog != null ? this.pkCatalog.hashCode() : 0;

        myHash += this.updateRule + this.deleteRule + this.deferrability;

        myHash += this.fkColumnNames != null ? this.fkColumnNames.hashCode() : 0;
        myHash += this.pkColumnNames != null ? this.pkColumnNames.hashCode() : 0;

        return myHash;
    }

    /**
     * @see com.stc.model.database.ForeignKey#getColumnCount
     */
    public int getColumnCount() {
        return this.fkColumnNames.size();
    }

    /**
     * @see com.stc.model.database.ForeignKey#getColumnName
     */
    public String getColumnName(final int iColumn) {
        return (String) this.fkColumnNames.get(iColumn);
    }

    /**
     * @see com.stc.model.database.ForeignKey#getSequence
     */
    public int getSequence(final DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return this.fkColumnNames.indexOf(col.getName().trim());
    }

    private void copyFrom(final ForeignKey src) {
        this.parent = src.getParent();

        this.fkName = src.getName();
        this.fkColumnNames.clear();
        this.fkColumnNames.addAll(src.getColumnNames());

        this.pkName = src.getPKName();
        this.pkCatalog = src.getPKCatalog();
        this.pkSchema = src.getPKSchema();
        this.pkTable = src.getPKTable();
        this.pkColumnNames.clear();
        this.pkColumnNames.addAll(src.getPKColumnNames());

        // Set cascade attributes
        this.updateRule = src.getUpdateRule();
        this.deleteRule = src.getDeleteRule();
        this.deferrability = src.getDeferrability();
    }

    public static class Column implements Comparable {
        private String name;

        private int sequence;

        private String pkColumnName;

        public Column(final String colName, final int colSequence, final String pkColName) {
            final ResourceBundle cMessages = NbBundle.getBundle(ForeignKeyImpl.class);

            if (colName == null || colName.trim().length() == 0) {
                throw new IllegalArgumentException(cMessages.getString("ERROR_COL_NAME") + "ERROR_COL_NAME");// NO
                // i18n
            }

            if (pkColName == null || pkColName.trim().length() == 0) {
                throw new IllegalArgumentException(cMessages.getString("ERROR_PK_COLNAME") + "ERROR_PK_COLNAME");// NO
                // i18n
            }

            if (colSequence <= 0) {
                throw new IllegalArgumentException(cMessages.getString("ERROR_COL_SEQ") + "ERROR_COL_SEQ");// NO
                // i18n
            }

            this.name = colName.trim();
            this.sequence = colSequence;

            this.pkColumnName = pkColName.trim();
        }

        public String getName() {
            return this.name;
        }

        public int getSequence() {
            return this.sequence;
        }

        public String getPKColumnName() {
            return this.pkColumnName;
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

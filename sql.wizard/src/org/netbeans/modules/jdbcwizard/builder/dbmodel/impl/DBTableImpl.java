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
package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DatabaseModel;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.PrimaryKey;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.ForeignKey;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/**
 * Reference implementation for interface com.stc.model.database.DBTable
 * 
 * @author
 */
public class DBTableImpl implements DBTable, Cloneable, Comparable {

    /* Log4J category string */
    private static final String LOG_CATEGORY = DBTableImpl.class.getName();

    /** table name. */
    protected String name;

    /** user-defined description */
    protected String description;

    /** schema to which this table belongs. */
    protected String schema;

    /** catalog to which this table belongs. */
    protected String catalog;

    /** Type of this table, these are "TABLE" or "VIEW" */
    protected String type = null;

    /** DatabaseModelImpl instance that "owns" this table. */
    protected DatabaseModel parent;

    /** Map of column metadata. */
    protected Map columns;

    /**
     * Map of columns in the order the driver returns them. Order preserving to keep both JCE and
     * BPEL editors show the otd in the same way.
     */
    protected ArrayList columnsInTableOrder;

    /** PrimaryKey for this table; may be null. */
    protected PrimaryKeyImpl primaryKey;

    /** Map of names to ForeignKey instances for this table; may be empty. */
    protected Map foreignKeys;

    /** Map of names to Index instances for this table; may be empty. */
    protected Map indexes;

    /**
     * Java name of this table; not in DatabaseModel but supplied as courtesy for WSDL
     */
    protected String javaName;

    private final HashMap jdbcTypeMap = new HashMap();

    private final HashMap sqlTypeMap = new HashMap();

    /** editable */
    protected boolean editable = true;

    /** selected */
    protected boolean selected = false;

    /* No-arg constructor; initializes Collections-related member variables. */
    private DBTableImpl() {
        this.columns = new HashMap();
        this.foreignKeys = new HashMap();
        this.indexes = new HashMap();
        this.columnsInTableOrder = new ArrayList();
        this.initJDBCTypeMap();
    }

    /**
     * Creates a new instance of DBTableImpl with the given name.
     * 
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     */
    public DBTableImpl(final String aName, final String aSchema, final String aCatalog) {
        this();

        this.name = aName != null ? aName.trim() : null;
        this.schema = aSchema != null ? aSchema.trim() : null;
        this.catalog = aCatalog != null ? aCatalog.trim() : null;
    }

    /**
     * Creates a new instance of DBTableImpl with the given name.
     * 
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     * @param aType type of new DBTable instance;
     */
    public DBTableImpl(final String aName, final String aSchema, final String aCatalog, final String aType) {
        this();

        this.name = aName != null ? aName.trim() : null;
        this.schema = aSchema != null ? aSchema.trim() : null;
        this.catalog = aCatalog != null ? aCatalog.trim() : null;
        this.type = aType != null ? aType.trim() : null;
    }

    /**
     * Creates a new instance of DBTableImpl, cloning the contents of the given DBTable
     * implementation instance.
     * 
     * @param src DBTable instance to be cloned
     */
    public DBTableImpl(final DBTable src) {
        this();

        if (src == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(DBTableImpl.class);

            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_DBTABLE") + "ERROR_NULL_DBTABLE");// NO
            // i18n
        }

        this.copyFrom(src);
    }

    /*
     * Implementation of DBTable interface.
     */

    /**
     * @see com.stc.model.database.DBTable#getName
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.stc.model.database.DBTable#getDescription
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @see com.stc.model.database.DBTable#getSchema
     */
    public String getSchema() {
        return this.schema;
    }

    /**
     * @see com.stc.model.database.DBTable#getCatalog
     */
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * @see com.stc.model.database.DBTable#getType
     */
    public String getType() {
        return this.type;
    }

    /**
     * @see com.stc.model.database.DBTable#getColumns
     */
    public Map getColumns() {
        return this.columns;
    }

    /**
     * @see com.stc.model.database.DBTable#getColumnList
     */
    public List getColumnList() {
        final List list = new ArrayList();
        list.addAll(this.columns.values());
        return list;
    }

    /**
     * Map of columns in the order the driver returns them. Order preserving to keep both JCE and
     * BPEL editors show the otd in the same way.
     */
    public List getColumnListInTablelOrder() {
        return this.columnsInTableOrder;
    }

    /**
     * @see com.stc.model.database.DBTable#getParent
     */
    public DatabaseModel getParent() {
        return this.parent;
    }

    /**
     * @see com.stc.model.database.DBTable#getPrimaryKey
     */
    public PrimaryKey getPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * @see com.stc.model.database.DBTable#getForeignKeys
     */
    public List getForeignKeys() {
        return new ArrayList(this.foreignKeys.values());
    }

    /**
     * @see com.stc.model.database.DBTable#getForeignKey(String)
     */
    public ForeignKey getForeignKey(final String fkName) {
        return (ForeignKey) this.foreignKeys.get(fkName);
    }

    /**
     * @see com.stc.model.database.DBTable#getReferencedTables
     */
    public Set getReferencedTables() {
        Set tables = Collections.EMPTY_SET;
        final List keys = this.getForeignKeys();

        if (keys.size() != 0) {
            tables = new HashSet(keys.size());
            final Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                final ForeignKeyImpl fk = (ForeignKeyImpl) iter.next();
                final DBTable pkTable = this.parent.getTable(fk.getPKTable(), fk.getPKSchema(), fk.getPKCatalog());
                if (pkTable != null && fk.references(pkTable.getPrimaryKey())) {
                    tables.add(pkTable);
                }
            }

            if (tables.size() == 0) {
                tables.clear();
                tables = Collections.EMPTY_SET;
            }
        }

        return tables;
    }

    /**
     * @see com.stc.model.database.DBTable#getIndexes
     */
    public List getIndexes() {
        return new ArrayList(this.indexes.values());
    }

    /**
     * @see com.stc.model.database.DBTable#getIndex
     */
    public Index getIndex(final String indexName) {
        return (Index) this.indexes.get(indexName);
    }

    /**
     * @see com.stc.model.database.DBTable#references
     */
    public boolean references(final DBTable pkTarget) {
        return this.getReferenceFor(pkTarget) != null;
    }

    /**
     * @see com.stc.model.database.DBTable#getReferenceFor
     */
    public ForeignKey getReferenceFor(final DBTable target) {
        if (target == null) {
            return null;
        }

        final PrimaryKey targetPK = target.getPrimaryKey();
        if (targetPK == null) {
            return null;
        }

        final Iterator iter = this.foreignKeys.values().iterator();
        while (iter.hasNext()) {
            final ForeignKey myFK = (ForeignKey) iter.next();
            if (myFK.references(targetPK)) {
                return myFK;
            }
        }

        return null;
    }

    /*
     * Setters and non-API helper methods for this implementation.
     */
    /**
     * Sets table name to new value.
     * 
     * @param newSchema new value for schema name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Sets schema name to new value.
     * 
     * @param newSchema new value for schema name
     */
    public void setSchema(final String newSchema) {
        this.schema = newSchema;
    }

    /**
     * Sets catalog name to new value.
     * 
     * @param newCatalog new value for catalog name
     */
    public void setCatalog(final String newCatalog) {
        this.catalog = newCatalog;
    }

    /**
     * Sets type name to new value.
     * 
     * @param newType new value for type name
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * Sets PrimaryKey instance for this DBTable to the given instance.
     * 
     * @param newPk new PrimaryKey instance to be associated
     * @return true if association succeeded, false otherwise
     */
    public boolean setPrimaryKey(final PrimaryKeyImpl newPk) {
        if (newPk != null) {
            newPk.setParent(this);
        }

        this.primaryKey = newPk;
        return true;
    }

    /**
     * Adds the given ForeignKeyImpl, associating it with this DBTableImpl instance.
     * 
     * @param newFk new ForeignKeyImpl instance to be added
     * @return return true if addition succeeded, false otherwise
     */
    public boolean addForeignKey(final ForeignKeyImpl newFk) {
        if (newFk != null) {
            newFk.setParent(this);
            this.foreignKeys.put(newFk.getName(), newFk);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Dissociates the given ForeignKeyImpl from this DBTableImpl instance, removing it from its
     * internal FK collection.
     * 
     * @param newFk new ForeignKeyImpl instance to be removed
     * @return return true if removal succeeded, false otherwise
     */
    public boolean removeForeignKey(final ForeignKeyImpl oldKey) {
        if (oldKey != null) {
            return this.foreignKeys.remove(oldKey.getName()) != null;
        }

        return false;
    }

    /**
     * Clears list of foreign keys.
     */
    public void clearForeignKeys() {
        this.foreignKeys.clear();
    }

    /**
     * Adds the given IndexImpl, associating it with this DBTableImpl instance.
     * 
     * @param newIndex new IndexImpl instance to be added
     * @return return true if addition succeeded, false otherwise
     */
    public boolean addIndex(final IndexImpl newIndex) {
        if (newIndex != null) {
            newIndex.setParent(this);
            this.indexes.put(newIndex.getName(), newIndex);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Clears list of indexes.
     */
    public void clearIndexes() {
        this.indexes.clear();
    }

    /**
     * Compares DBTable with another object for lexicographical ordering. Null objects and those
     * DBTables with null names are placed at the end of any ordered collection using this method.
     * 
     * @param refObj Object to be compared.
     * @return -1 if the column name is less than obj to be compared. 0 if the column name is the
     *         same. 1 if the column name is greater than obj to be compared.
     */
    public int compareTo(final Object refObj) {
        if (refObj == null) {
            return -1;
        }

        if (refObj == this) {
            return 0;
        }

        final String refName = this.parent != null ? this.parent.getFullyQualifiedTableName((DBTable) refObj)
                : ((DBTable) refObj).getName();

        final String myName = this.parent != null ? this.parent.getFullyQualifiedTableName(this) : this.name;

        return myName != null ? myName.compareTo(refName) : refName != null ? 1 : -1;
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param obj Object against which we compare this instance
     * @return true if obj is functionally identical to this JDBCTable instance; false otherwise
     */
    public boolean equals(final Object obj) {
        boolean result = false;

        // Check for reflexivity first.
        if (this == obj) {
            return true;
        }

        // Check for castability (also deals with null obj)
        if (obj instanceof DBTable) {
            final DBTable aTable = (DBTable) obj;
            final String aTableName = aTable.getName();
            final DatabaseModel aTableParent = aTable.getParent();
            final Map aTableColumns = aTable.getColumns();
            final PrimaryKey aTablePK = aTable.getPrimaryKey();
            final List aTableFKs = aTable.getForeignKeys();
            final List aTableIdxs = aTable.getForeignKeys();

            result = aTableName != null && this.name != null && this.name.equals(aTableName)
                    && this.parent != null && aTableParent != null && this.parent.equals(aTableParent);

            if (this.columns != null && aTableColumns != null) {
                final Set objCols = aTableColumns.keySet();
                final Set myCols = this.columns.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                result &= myCols.containsAll(objCols) && objCols.containsAll(myCols);
            } else if (!(this.columns == null && aTableColumns == null)) {
                result = false;
            }

            result &= this.primaryKey != null ? this.primaryKey.equals(aTablePK) : aTablePK == null;

            if (this.foreignKeys != null && aTableFKs != null) {
                final Collection myFKs = this.foreignKeys.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myFKs.containsAll(aTableFKs) && aTableFKs.containsAll(myFKs);
            } else if (!(this.foreignKeys == null && aTableFKs == null)) {
                result = false;
            }

            if (this.indexes != null && aTableIdxs != null) {
                final Collection myIdxs = this.indexes.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myIdxs.containsAll(aTableIdxs) && aTableIdxs.containsAll(myIdxs);
            } else if (!(this.indexes == null && aTableIdxs == null)) {
                result = false;
            }
        }

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
        myHash += this.parent != null ? this.parent.hashCode() : 0;
        myHash += this.schema != null ? this.schema.hashCode() : 0;
        myHash += this.catalog != null ? this.catalog.hashCode() : 0;

        // Include hashCodes of all column names.
        if (this.columns != null) {
            myHash += this.columns.keySet().hashCode();
        }

        if (this.primaryKey != null) {
            myHash += this.primaryKey.hashCode();
        }

        if (this.foreignKeys != null) {
            myHash += this.foreignKeys.keySet().hashCode();
        }

        if (this.indexes != null) {
            myHash += this.indexes.keySet().hashCode();
        }

        return myHash;
    }

    /**
     * Clone a deep copy of DBTable.
     * 
     * @return a copy of DBTable.
     */
    public Object clone() {
        try {
            final DBTableImpl table = (DBTableImpl) super.clone();

            table.columns = new HashMap();
            table.deepCopyReferences(this);

            return table;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Performs deep copy of contents of given DBTable. We deep copy (that is, the method clones all
     * child objects such as columns) because columns have a parent-child relationship that must be
     * preserved internally.
     * 
     * @param source JDBCTable providing contents to be copied.
     */
    public void copyFrom(final DBTable source) {
        if (source == null) {
            throw new IllegalArgumentException("Must supply non-null ref for source");
        } else if (source == this) {
            return;
        }

        this.name = source.getName();
        this.description = source.getDescription();
        this.schema = source.getSchema();
        this.catalog = source.getCatalog();

        this.parent = source.getParent();
        this.deepCopyReferences(source);
    }

    /*
     * Perform deep copy of columns. @param source JDBCTable whose columns are to be copied.
     */
    private void deepCopyReferences(final DBTable source) {
        if (source != null && source != this) {
            this.primaryKey = null;
            final PrimaryKey srcPk = source.getPrimaryKey();
            if (srcPk != null) {
                this.primaryKey = new PrimaryKeyImpl(source.getPrimaryKey());
            }

            this.foreignKeys.clear();
            Iterator iter = source.getForeignKeys().iterator();
            while (iter.hasNext()) {
                final ForeignKeyImpl impl = new ForeignKeyImpl((ForeignKey) iter.next());
                impl.setParent(this);
                this.foreignKeys.put(impl.getName(), impl);
            }

            this.indexes.clear();
            iter = source.getIndexes().iterator();
            while (iter.hasNext()) {
                final IndexImpl impl = new IndexImpl((Index) iter.next());
                impl.setParent(this);
                this.indexes.put(impl.getName(), impl);
            }

            this.columns.clear();
            iter = source.getColumnList().iterator();

            // Must do deep copy to ensure correct parent-child relationship.
            while (iter.hasNext()) {
                final DBColumnImpl dbColImpl = new DBColumnImpl();
                dbColImpl.copyFrom((DBColumn) iter.next());
                dbColImpl.setParent(this);
                this.columns.put(dbColImpl.getName(), dbColImpl);
            }
        }
    }

    /**
     * Adds a DBColumn instance to this table.
     * 
     * @param theColumn column to be added.
     * @return true if successful. false if failed.
     */
    public boolean addColumn(final DBColumn theColumn) {
        if (theColumn != null) {
            // theColumn.setParent(this);
            this.columns.put(theColumn.getName(), theColumn);
            return true;
        }

        return false;
    }

    /**
     * Convenience class to create DBColumnImpl instance (with the given column name, data source
     * name, JDBC type, scale, precision, and nullable), and add it to this DBTableImpl instance.
     * 
     * @param columnName Column name
     * @param jdbcType JDBC type defined in SQL.Types
     * @param scale Scale
     * @param precision Precision
     * @param nullable Nullable
     * @return new DBColumnImpl instance
     */
    public DBColumnImpl createColumn(final String columnName,
                                     final int jdbcType,
                                     final int scale,
                                     final int precision,
                                     final boolean isPK,
                                     final boolean isFK,
                                     final boolean isIndexed,
                                     final boolean nullable) {
        final DBColumnImpl impl = new DBColumnImpl(columnName, jdbcType, scale, precision, isPK, isFK, isIndexed, nullable);
        // impl.setParent(this);
        this.columns.put(columnName, impl);
        this.columnsInTableOrder.add(impl);

        return impl;
    }

    /**
     * Deletes all columns associated with this table.
     * 
     * @return true if all columns were deleted successfully, false otherwise.
     */
    public boolean deleteAllColumns() {
        this.columns.clear();
        return false;
    }

    /**
     * Deletes DBColumn, if any, associated with the given name from this table.
     * 
     * @param columnName column name to be removed.
     * @return true if successful. false if failed.
     */
    public boolean deleteColumn(final String columnName) {
        if (columnName != null && columnName.trim().length() != 0) {
            return this.columns.remove(columnName) != null;
        }
        return false;
    }

    /**
     * Get editable
     * 
     * @return true/false
     */
    public boolean isEditable() {
        return this.editable;
    }

    /**
     * @return
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * @param select
     */
    public void setSelected(final boolean select) {
        this.selected = select;
    }

    /**
     * Set editable
     * 
     * @param edit - editable
     */
    public void setEditable(final boolean edit) {
        this.editable = edit;
    }

    /**
     * Gets the DBColumn, if any, associated with the given name
     * 
     * @param columnName column name
     * @return DBColumn associated with columnName, or null if none exists
     */
    public DBColumn getColumn(final String columnName) {
        return (DBColumn) this.columns.get(columnName);
    }

    /**
     * Clones contents of the given Map to this table's internal column map, overwriting any
     * previous mappings.
     * 
     * @param theColumns Map of columns to be substituted
     * @return true if successful. false if failed.
     */
    public boolean setAllColumns(final Map theColumns) {
        this.columns.clear();
        if (theColumns != null) {
            this.columns.putAll(theColumns);
        }
        return true;
    }

    /**
     * Sets description text for this instance.
     * 
     * @param newDesc new descriptive text
     */
    public void setDescription(final String newDesc) {
        this.description = newDesc;
    }

    /**
     * Sets parent DatabaseModel to the given reference.
     * 
     * @param newParent new DatabaseModel parent
     */
    public void setParent(final DatabaseModelImpl newParent) {
        this.parent = newParent;
    }

    /**
     * Gets Java name for this table.
     * 
     * @return normalized Java name for this table
     */
    public String getJavaName() {
        return this.javaName != null ? this.javaName : this.name;
    }

    /**
     * Sets Java name for this table.
     * 
     * @param newName new normalized Java name for this table; null if plain name is to be used.
     */
    public void setJavaName(final String newName) {
        this.javaName = newName;
    }

    private void initJDBCTypeMap() {
        this.jdbcTypeMap.put("array", new Integer(java.sql.Types.ARRAY));
        this.jdbcTypeMap.put("bigint", new Integer(java.sql.Types.BIGINT));
        this.jdbcTypeMap.put("binary", new Integer(java.sql.Types.BINARY));
        this.jdbcTypeMap.put("bit", new Integer(java.sql.Types.BIT));
        this.jdbcTypeMap.put("blob", new Integer(java.sql.Types.BLOB));
        this.jdbcTypeMap.put("char", new Integer(java.sql.Types.CHAR));
        this.jdbcTypeMap.put("clob", new Integer(java.sql.Types.CLOB));
        this.jdbcTypeMap.put("date", new Integer(java.sql.Types.DATE));
        this.jdbcTypeMap.put("decimal", new Integer(java.sql.Types.DECIMAL));
        this.jdbcTypeMap.put("distinct", new Integer(java.sql.Types.DISTINCT));
        this.jdbcTypeMap.put("double", new Integer(java.sql.Types.DOUBLE));
        this.jdbcTypeMap.put("float", new Integer(java.sql.Types.FLOAT));
        this.jdbcTypeMap.put("integer", new Integer(java.sql.Types.INTEGER));
        this.jdbcTypeMap.put("longvarbinary", new Integer(java.sql.Types.LONGVARBINARY));
        this.jdbcTypeMap.put("longvarchar", new Integer(java.sql.Types.LONGVARCHAR));
        this.jdbcTypeMap.put("numeric", new Integer(java.sql.Types.NUMERIC));
        this.jdbcTypeMap.put("real", new Integer(java.sql.Types.REAL));
        this.jdbcTypeMap.put("smallint", new Integer(java.sql.Types.SMALLINT));
        this.jdbcTypeMap.put("time", new Integer(java.sql.Types.TIME));
        this.jdbcTypeMap.put("timestamp", new Integer(java.sql.Types.TIMESTAMP));
        this.jdbcTypeMap.put("tinyint", new Integer(java.sql.Types.TINYINT));
        this.jdbcTypeMap.put("varchar", new Integer(java.sql.Types.VARCHAR));
        this.jdbcTypeMap.put("varbinary", new Integer(java.sql.Types.VARBINARY));

        this.sqlTypeMap.put(new Integer(java.sql.Types.ARRAY), "array");
        this.sqlTypeMap.put(new Integer(java.sql.Types.BIGINT), "bigint");
        this.sqlTypeMap.put(new Integer(java.sql.Types.BINARY), "binary");
        this.sqlTypeMap.put(new Integer(java.sql.Types.BIT), "bit");
        this.sqlTypeMap.put(new Integer(java.sql.Types.BLOB), "blob");
        this.sqlTypeMap.put(new Integer(java.sql.Types.CHAR), "char");
        this.sqlTypeMap.put(new Integer(java.sql.Types.CLOB), "clob");
        this.sqlTypeMap.put(new Integer(java.sql.Types.DATE), "date");
        this.sqlTypeMap.put(new Integer(java.sql.Types.DECIMAL), "decimal");
        this.sqlTypeMap.put(new Integer(java.sql.Types.DISTINCT), "distinct");
        this.sqlTypeMap.put(new Integer(java.sql.Types.DOUBLE), "double");
        this.sqlTypeMap.put(new Integer(java.sql.Types.FLOAT), "float");
        this.sqlTypeMap.put(new Integer(java.sql.Types.INTEGER), "integer");
        this.sqlTypeMap.put(new Integer(java.sql.Types.LONGVARBINARY), "longvarbinary");
        this.sqlTypeMap.put(new Integer(java.sql.Types.LONGVARCHAR), "longvarchar");
        this.sqlTypeMap.put(new Integer(java.sql.Types.NUMERIC), "numeric");
        this.sqlTypeMap.put(new Integer(java.sql.Types.REAL), "real");
        this.sqlTypeMap.put(new Integer(java.sql.Types.SMALLINT), "smallint");
        this.sqlTypeMap.put(new Integer(java.sql.Types.TIME), "time");
        this.sqlTypeMap.put(new Integer(java.sql.Types.TIMESTAMP), "timestamp");
        this.sqlTypeMap.put(new Integer(java.sql.Types.TINYINT), "tinyint");
        this.sqlTypeMap.put(new Integer(java.sql.Types.VARCHAR), "varchar");
        this.sqlTypeMap.put(new Integer(java.sql.Types.VARBINARY), "varbinary");
    }

    /**
     * Gets the JDBC Type for a given oracle8/oracle9 type
     * 
     * @param dbType for which jdbctype is returned
     * @return java.sql.Types.* for given string jdbcType
     */
    public int getJDBCType(final String dbType) {
        final Integer value = (Integer) this.jdbcTypeMap.get(dbType);
        if (value != null) {
            return value.intValue();
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Gets the SQLType
     * 
     * @param type for which sql type is returned
     * @return String for given jdbc type
     */
    public String getSQLType(final int type) {
        final Integer intType = new Integer(type);
        return (String) this.sqlTypeMap.get(intType);
    }

    static class StringComparator implements Comparator {
        public int compare(final Object o1, final Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                return ((String) o1).compareTo((String) o2);
            } else {
                final ResourceBundle cMessages = NbBundle.getBundle(DBTableImpl.class);
                throw new ClassCastException(cMessages.getString("ERROR_STRING_COMPARATOR")
                        + "ERROR_STRING_COMPARATOR");// NO i18n
            }
        }
    }

    public boolean isSelectedforAnOperation() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSelectedforAllOperations(final boolean setAll) {
        // TODO Auto-generated method stub

    }

}

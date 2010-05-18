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

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DatabaseModel;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBConnectionDefinition;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * This class implements DBQueryModel
 * 
 * @author
 */
public class DatabaseModelImpl implements DatabaseModel, Cloneable {

    /* Initial buffer size for StringBuffer used in marshalling OTDs to XML */
    private static final String LOG_CATEGORY = DatabaseModelImpl.class.getName();

    /*
     * String used to separate name, schema, and/or catalog Strings in a fully-qualified table name.
     */
    private static final String FQ_TBL_NAME_SEPARATOR = ".";

    /** User-supplied name */
    protected volatile String name;

    /** User-supplied description */
    protected volatile String description;

    /** Map of DBTable instances */
    protected SortedMap tables;

    /** Connection name */
    protected volatile String connectionName;

    // protected RepositoryObject source;

    /* Connection definition used to retrieve metadata */
    protected DBConnectionDefinition connectionDefinition;

    private DatabaseModelImpl() {
        this.tables = new TreeMap();
    }

    /**
     * Constructs a new instance of DatabaseModelImpl using the given name and
     * DBConnectionDefinition.
     * 
     * @param dName name of new DBQueryModel
     * @param connDef DBConnectionInfo for this Data Source
     */
    public DatabaseModelImpl(final String modelName, final DBConnectionDefinition connDef) {
        this();
        final ResourceBundle cMessages = NbBundle.getBundle(DatabaseModelImpl.class);
        if (connDef == null) {
            throw new IllegalArgumentException("connDef must be non-null");
        }

        final String connName = connDef.getName();
        if (connName == null || connName.trim().length() == 0) {
            throw new IllegalArgumentException(cMessages.getString("ERROR_NAME_CONNDEF") + "(ERROR_NAME_CONNDEF)");
        }

        if (modelName == null || modelName.trim().length() == 0) {
            throw new IllegalArgumentException(cMessages.getString("ERROR_NAME_MODEL") + "(ERROR_NAME_MODEL)");
        }

        this.name = modelName;
        this.connectionDefinition = connDef;
    }

    /**
     * Creates a new instance of DatabaseModelImpl, cloning the contents of the given DBQueryModel
     * implementation instance.
     * 
     * @param src DBQueryModel instance to be cloned
     */
    /*
     * public DatabaseModelImpl(DBQueryModel src) { this(); ResourceBundle cMessages =
     * NbBundle.getBundle(DatabaseModelImpl.class); if (src == null) { throw new
     * IllegalArgumentException( cMessages.getString("ERROR_PARAM")+"(ERROR_PARAM)"); }
     * copyFrom(src); }
     */

    /**
     * @see com.stc.model.database.DBQueryModel#getModelDescription
     */
    public String getModelDescription() {
        return this.description;
    }

    /**
     * Sets the description string of this DBQueryModel
     * 
     * @param newDesc new description string
     */
    public void setDescription(final String newDesc) {
        this.description = newDesc;
    }

    /**
     * Adds new DBTable to the model.
     * 
     * @param table new DBTable to add
     * @return true if add succeeded, false otherwise
     */
    public boolean addTable(final DBTable table) {
        if (table != null) {
            this.tables.put(this.getFullyQualifiedTableName(table), table);
            return true;
        }
        return false;
    }

    /**
     * Copies member values to those contained in the given DBQueryModel instance.
     * 
     * @param src DBQueryModel whose contents are to be copied into this instance
     */
    public void copyFrom(final DatabaseModel src) {
        if (src != null) {
            this.name = src.getModelName();
            this.description = src.getModelDescription();

            final DBConnectionDefinition def = src.getConnectionDefinition();
            if (def instanceof DBConnectionDefinitionImpl) {
                this.connectionDefinition = def;
            } else {
                this.connectionDefinition = new DBConnectionDefinitionImpl(def);
            }

            this.tables.clear();
            final List srcTables = src.getTables();
            if (srcTables != null) {
                final Iterator iter = srcTables.iterator();
                while (iter.hasNext()) {
                    final DBTable tbl = (DBTable) iter.next();
                    this.addTable(new DBTableImpl(tbl));
                }
            }
        }
    }

    /**
     * Create DBTable with the given table name, schema name and catalog name.
     * 
     * @param tableName table name.
     * @param schemaName schema name; may be null
     * @param catalogName catalog name; may be null
     * @return an instance of DBTable if successfull, null if failed.
     */
    public DBTable createTable(final String tableName, final String schemaName, final String catalogName) {
        DBTableImpl table = null;
        final ResourceBundle cMessages = NbBundle.getBundle(DatabaseModelImpl.class);
        if (tableName == null || tableName.length() == 0) {
            throw new IllegalArgumentException(cMessages.getString("ERROR_TABLE_NAME") + "(ERROR_TABLE_NAME)");
        }

        table = new DBTableImpl(tableName, schemaName, catalogName);
        this.addTable(table);
        return table;
    }

    /**
     * Deletes all tables associated with this data source.
     * 
     * @return true if all tables were deleted successfully, false otherwise.
     */
    public boolean deleteAllTables() {
        this.tables.clear();
        return true;
    }

    /**
     * Delete table from the DatabaseModelImpl
     * 
     * @param fqTableName fully qualified name of table to be deleted.
     * @return true if successful. false if failed.
     * @see com.stc.model.database.DBTable#getFullyQualifiedTableName
     */
    public boolean deleteTable(final String fqTableName) {
        if (fqTableName != null && fqTableName.trim().length() != 0) {
            this.tables.remove(fqTableName);
            return true;
        }
        return false;
    }

    /**
     * @see com.stc.model.database.DBQueryModel#getConnectionName
     */
    public String getConnectionName() {
        return this.connectionName;

    }

    /**
     * @see com.stc.model.database.DBQueryModel#getModelName
     */
    public String getModelName() {
        return this.name;
    }

    /**
     * @see com.stc.model.database.DBQueryModel#getTable(String, String, String)
     */
    public DBTable getTable(final String tableName, final String schemaName, final String catalogName) {
        return this.getTable(this.getFullyQualifiedTableName(tableName, schemaName, catalogName));
    }

    /**
     * @see com.stc.model.database.DBQueryModel#getTable(String)
     */
    public DBTable getTable(final String fqTableName) {
        return (DBTable) this.tables.get(fqTableName);
    }

    /**
     * @see com.stc.model.database.DBQueryModel#getTables
     */
    public List getTables() {
        List list = Collections.EMPTY_LIST;
        final Collection tableColl = this.tables.values();

        if (tableColl.size() != 0) {
            list = new ArrayList(tableColl.size());
            list.addAll(tableColl);
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Gets a read-only Map of table names to available DBTable instances in this model.
     * 
     * @return readonly Map of table names to DBTable instances
     */
    public Map getTableMap() {
        return Collections.unmodifiableMap(this.tables);
    }

    /**
     * Sets the Connection Name associated with connection name
     * 
     * @param theConName associated with this DataSource
     */
    public void setConnectionName(final String theConName) {
        this.connectionName = theConName;
    }

    /**
     * @see java.lang.Object#equals
     */
    public boolean equals(final Object refObj) {
        // Check for reflexivity.
        if (this == refObj) {
            return true;
        }

        boolean result = false;

        // Ensure castability (also checks for null refObj)
        if (refObj instanceof DatabaseModelImpl) {
            final DatabaseModelImpl aSrc = (DatabaseModelImpl) refObj;

            result = aSrc.name != null ? aSrc.name.equals(this.name) : this.name == null;

            boolean connCheck = aSrc.connectionName != null ? aSrc.connectionName.equals(this.connectionName)
                    : this.connectionName == null;
            result &= connCheck;

            connCheck = aSrc.connectionDefinition != null ? aSrc.connectionDefinition.equals(this.connectionDefinition)
                    : this.connectionDefinition == null;
            result &= connCheck;

            if (this.tables != null && aSrc.tables != null) {
                final Set objTbls = aSrc.tables.keySet();
                final Set myTbls = this.tables.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                final boolean tblCheck = myTbls.containsAll(objTbls) && objTbls.containsAll(myTbls);
                result &= tblCheck;
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

        myHash += this.connectionName != null ? this.connectionName.hashCode() : 0;
        myHash += this.connectionDefinition != null ? this.connectionDefinition.hashCode() : 0;

        if (this.tables != null) {
            myHash += this.tables.keySet().hashCode();
        }

        return myHash;
    }

    /**
     * Overrides default implementation to return name of this DBQueryModel.
     * 
     * @return model name.
     */
    public String toString() {
        return this.getModelName();
    }

    /**
     * Clones this object.
     * 
     * @return shallow copy of this DatabaseModelImpl
     */
    public Object clone() {
        try {
            final DatabaseModelImpl myClone = (DatabaseModelImpl) super.clone();

            myClone.name = this.name;
            myClone.description = this.description;
            myClone.connectionName = myClone.connectionName;
            myClone.tables = new TreeMap();
            this.tables.putAll(this.tables);
            myClone.connectionDefinition = new DBConnectionDefinitionImpl(this.connectionDefinition);

            return myClone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Gets DBConnectionDefinition of the DatabaseModelImpl object
     * 
     * @return ConnectionDefinition of the DatabaseModelImpl object
     */
    public DBConnectionDefinition getConnectionDefinition() {
        return this.connectionDefinition;
    }

    /**
     * @see com.stc.model.database.DBQueryModel#getFullyQualifiedTableName(DBTable)
     */
    public String getFullyQualifiedTableName(final DBTable tbl) {
        return tbl != null ? this.getFullyQualifiedTableName(tbl.getName(), tbl.getSchema(), tbl.getCatalog()) : "";
    }

    /**
     * @see com.stc.model.database.DBQueryModel#getFullyQualifiedTableName(String, String, String)
     */
    public String getFullyQualifiedTableName(final String tblName, final String schName, final String catName) {
        if (tblName == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(DatabaseModelImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_TABLE_NAME") + "ERROR_NULL_TABLE_NAME");
        }

        final StringBuffer buf = new StringBuffer(50);

        /**
         * TODO:RE:Mar11/2004 Per rex we need to provide option in the wizard to use catalog name or
         * not. Disabling for now.
         */

        /*
         * if (catName != null && catName.trim().length() != 0) { buf.append(catName.trim());
         * buf.append(FQ_TBL_NAME_SEPARATOR); }
         */
        if (schName != null && schName.trim().length() != 0) {
            buf.append(schName.trim());
            buf.append(DatabaseModelImpl.FQ_TBL_NAME_SEPARATOR);
        }

        buf.append(tblName.trim());

        return buf.toString();
    }

    /**
     * Gets repository object, if any, providing underlying data for this DBQueryModel.
     * 
     * @return RepositoryObject hosting this object's metadata, or null if data are not held by a
     *         RepositoryObject.
     */
    /*
     * public RepositoryObject getSource() { return source; } public void setSource(RepositoryObject
     * newSource) { source = newSource; }
     */

}

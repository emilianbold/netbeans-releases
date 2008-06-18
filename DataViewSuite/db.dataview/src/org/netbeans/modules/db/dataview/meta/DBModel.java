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
package org.netbeans.modules.db.dataview.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Container object for database tables.
 *
 * @author Ahimanikya Satapathy
 */
public final class DBModel extends DBObject<Object> {

    private static final String FQ_TBL_NAME_SEPARATOR = ".";
    protected volatile String description;
    protected volatile String name;
    protected Map<String, DBTable> tables;

    public DBModel() {
        tables = new HashMap<String, DBTable>();
    }

    /**
     * Adds table to this instance.
     *
     * @param table
     *            new table to add
     * @throws IllegalStateException
     *             if unable to add table
     */
    public void addTable(DBTable table) throws IllegalStateException {
        if (table != null) {

            // if table already exists then we should throw exception
            String fqName = getFullyQualifiedTableName(table);
            if (this.getTable(fqName) != null) {
                //throw new IllegalStateException("Cannot add table " + fqName + ", it already exist!");
            }

            table.setParent(this);
            tables.put(fqName, table);
        }
    }

    /**
     * check if a table exists This will check if a table is in database model,
     */
    public boolean containsTable(DBTable table) {
        if (this.getTable(this.getFullyQualifiedTableName(table)) != null) {
            return true;
        }

        return false;
    }

    /**
     * Create DBTable instance with the given table, schema, and catalog names.
     *
     * @param tableName
     *            table name of new table
     * @param schemaName
     *            schema name of new table
     * @param catalogName
     *            catalog name of new table
     * @return an instance of SQLTable if successful, null if failed.
     */
    public DBTable createTable(String tableName, String schemaName, String catalogName) {
        DBTable table = null;

        if (tableName == null || tableName.length() == 0) {
            throw new IllegalArgumentException("tableName cannot be null");
        }

        table = new DBTable(tableName, schemaName, catalogName);
        addTable(table);
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
     * Delete table from the SQLDataSource
     *
     * @param fqTableName
     *            fully qualified name of table to be deleted.
     * @return true if successful. false if failed.
     */
    public boolean deleteTable(String fqTableName) {
        if (fqTableName != null && fqTableName.trim().length() != 0) {
            this.tables.remove(fqTableName);
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object refObj) {
        // Check for reflexivity.
        if (this == refObj) {
            return true;
        }

        boolean result = false;

        // Ensure castability (also checks for null refObj)
        if (refObj instanceof DBModel) {
            DBModel aSrc = (DBModel) refObj;

            result = ((aSrc.name != null) ? aSrc.name.equals(name) : (name == null));

            if (tables != null && aSrc.tables != null) {
                Set<String> objTbls = aSrc.tables.keySet();
                Set<String> myTbls = tables.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                boolean tblCheck = myTbls.containsAll(objTbls) && objTbls.containsAll(myTbls);
                result &= tblCheck;
            }
        }

        return result;
    }

    /**
     * Gets the allTables attribute of the SQLDataSource object
     *
     * @return The allTables value
     */
    public synchronized Map getAllDBTables() {
        return tables;
    }

    /**
     * get a list of tables based on table name, schema name and catalog name
     * since we allow duplicate tables this will return a list of tables
     */
    public List getAllTables(String tableName, String schemaName, String catalogName) {
        List<DBTable> tbls = new ArrayList<DBTable>();
        for (DBTable table : tbls) {
            String tName = table.getName();
            String tSchemaName = table.getSchema();
            String tCatalogName = table.getCatalog();

            boolean found = true;
            found = tName != null ? tName.equals(tableName) : tableName == null;
            found &= tSchemaName != null ? tSchemaName.equals(schemaName) : schemaName == null;
            found &= tCatalogName != null ? tCatalogName.equals(catalogName)
                    : (catalogName == null || catalogName.trim().equals(""));

            if (found) {
                tbls.add(table);
            }
        }

        return tbls;
    }

    /**
     * Gets List of child SQLObjects belonging to this instance.
     *
     * @return List of child SQLObjects
     */
    @Override
    public List<DBTable> getChildDBObjects() {
        return this.getTables();
    }

    public String getFullyQualifiedTableName(DBTable tbl) {

        if (tbl != null) {
            String tblName = tbl.getName();
            String schName = tbl.getSchema();
            String catName = tbl.getCatalog();

            if (tblName == null) {
                throw new IllegalArgumentException(
                        "Cannot construct fully qualified table name, table name is null.");
            }

            StringBuilder buf = new StringBuilder(50);

            if (catName != null && catName.trim().length() != 0) {
                buf.append(catName.trim());
                buf.append(FQ_TBL_NAME_SEPARATOR);
            }

            if (schName != null && schName.trim().length() != 0) {
                buf.append(schName.trim());
                buf.append(FQ_TBL_NAME_SEPARATOR);
            }

            buf.append(tblName.trim());

            return buf.toString();
        }

        return null;
    }

    public String getModelDescription() {
        return description;
    }

    public String getModelName() {
        return this.name;
    }

    public DBTable getTable(String fqTableName) {
        return (DBTable) this.tables.get(fqTableName);
    }

    /**
     * NOTE: This method will return first matching table, since now we allow
     * duplicate tables, so if you want to get specific table use
     * getFullyQualifiedTableName(DBTable tbl) to generate a qualified name
     * which includes object id then call getTable(fqName)
     *
     * @see org.netbeans.modules.model.database.DatabaseModel#getTable(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public DBTable getTable(String tableName, String schemaName, String catalogName) {
        Iterator it = this.tables.values().iterator();
        while (it.hasNext()) {
            DBTable table = (DBTable) it.next();
            String tName = table.getName();
            String tSchemaName = table.getSchema();
            String tCatalogName = table.getCatalog();

            boolean found = true;
            found = tName != null ? tName.equals(tableName) : tableName == null;
            found &= tSchemaName != null ? tSchemaName.equals(schemaName) : schemaName == null;
            found &= tCatalogName != null ? tCatalogName.equals(catalogName)
                    : (catalogName == null || catalogName.trim().equals(""));

            if (found) {
                return table;
            }
        }

        return null;
    }

    /**
     * Gets a read-only Map of table names to available DBTable instances in
     * this model.
     *
     * @return readonly Map of table names to DBTable instances
     */
    public Map getTableMap() {
        return Collections.unmodifiableMap(tables);
    }

    public List<DBTable> getTables() {
        List<DBTable> list = Collections.emptyList();
        Collection<DBTable> tableColl = tables.values();

        if (tableColl.size() != 0) {
            list = new ArrayList<DBTable>(tableColl.size());
            list.addAll(tableColl);
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Overrides default implementation to compute hashCode value for those
     * members used in equals() for comparison.
     *
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;

        if (tables != null) {
            myHash += tables.keySet().hashCode();
        }

        return myHash;
    }

    /**
     * Sets the description string of this DatabaseModel
     *
     * @param newDesc
     *            new description string
     */
    public void setDescription(String newDesc) {
        this.description = newDesc;
    }

    /**
     * Overrides default implementation to return name of this DatabaseModel.
     *
     * @return model name.
     */
    @Override
    public String toString() {
        return this.getModelName();
    }

    public void setModelName(String theName) {
        name = theName;
    }
}

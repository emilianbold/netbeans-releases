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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

/**
 * Represent Database Table
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBTable extends DBObject<DBModel> {

    public static class StringComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                return ((String) o1).compareTo((String) o2);
            }
            throw new ClassCastException("StringComparator cannot compare non-String objects.");
        }
    }
    private static final String FQ_TBL_NAME_SEPARATOR = ".";
    protected String alias;
    protected String catalog;
    protected Map<String, DBColumn> columns;
    protected String description;
    protected boolean editable = true;
    protected Map<String, DBForeignKey> foreignKeys;
    protected Map<String, DBIndex> indexes;
    protected String name;
    protected DBModel parentDBModel;
    protected DBPrimaryKey primaryKey;
    protected String schema;
    private String escapeString;

    /** No-arg constructor; initializes Collections-related member variables. */
    protected DBTable() {
        columns = new LinkedHashMap<String, DBColumn>();
        foreignKeys = new HashMap<String, DBForeignKey>();
        indexes = new HashMap<String, DBIndex>();
    }

    /**
     * Creates a new instance of DBTable with the given name.
     * 
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     */
    public DBTable(String aName, String aSchema, String aCatalog) {
        this();

        name = (aName != null) ? aName.trim() : null;
        schema = (aSchema != null) ? aSchema.trim() : null;
        catalog = (aCatalog != null) ? aCatalog.trim() : null;
    }

    /**
     * Adds an AbstractDBColumn instance to this table.
     * 
     * @param theColumn column to be added.
     * @return true if successful. false if failed.
     */
    public boolean addColumn(DBColumn theColumn) throws DBException {
        if (theColumn != null) {
            theColumn.setParent(this);
            columns.put(theColumn.getName(), theColumn);
            return true;
        }

        return false;
    }

    /**
     * Adds the given ForeignKeyImpl, associating it with this DBTable instance.
     * 
     * @param newFk new ForeignKeyImpl instance to be added
     * @return return true if addition succeeded, false otherwise
     */
    public boolean addForeignKey(DBForeignKey newFk) throws DBException {
        if (newFk != null) {
            newFk.setParentObject(this);
            foreignKeys.put(newFk.getName(), newFk);
            return true;
        }
        return false;
    }

    /**
     * Adds the given IndexImpl, associating it with this DBTable instance.
     * 
     * @param newIndex new IndexImpl instance to be added
     * @return return true if addition succeeded, false otherwise
     */
    public boolean addIndex(DBIndex newIndex) throws DBException {
        if (newIndex != null) {
            newIndex.setParentObject(this);
            indexes.put(newIndex.getName(), newIndex);

            return true;
        }
        return false;
    }

    /**
     * Clears list of foreign keys.
     */
    public void clearForeignKeys() {
        foreignKeys.clear();
    }

    /**
     * Clears list of indexes.
     */
    public void clearIndexes() {
        indexes.clear();
    }

    /**
     * Compares DBTable with another object for lexicographical ordering. Null objects and
     * those DBTables with null names are placed at the end of any ordered collection
     * using this method.
     * 
     * @param refObj Object to be compared.
     * @return -1 if the column name is less than obj to be compared. 0 if the column name
     *         is the same. 1 if the column name is greater than obj to be compared.
     */
    public int compareTo(Object refObj) {
        if (refObj == null) {
            return -1;
        }

        if (refObj == this) {
            return 0;
        }

        String refName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName((DBTable) refObj) : ((DBTable) refObj).getName();

        String myName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName(this) : name;

        return (myName != null) ? myName.compareTo(refName) : (refName != null) ? 1 : -1;
    }

    /**
     * Deletes all columns associated with this table.
     * 
     * @return true if all columns were deleted successfully, false otherwise.
     */
    public boolean deleteAllColumns() {
        columns.clear();
        return false;
    }

    /**
     * Deletes DBColumn, if any, associated with the given name from this table.
     * 
     * @param columnName column name to be removed.
     * @return true if successful. false if failed.
     */
    public boolean deleteColumn(String columnName) {
        if (columnName != null && columnName.trim().length() != 0) {
            return (columns.remove(columnName) != null);
        }
        return false;
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param obj Object against which we compare this instance
     * @return true if obj is functionally identical to this SQLTable instance; false
     *         otherwise
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        // Check for reflexivity first.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DBTable)) {
            return false;
        }

        result = super.equals(obj);

        if (!result) {
            return result;
        }

        // Check for castability (also deals with null obj)
        if (obj instanceof DBTable) {
            DBTable aTable = (DBTable) obj;
            String aTableName = aTable.getName();
            DBModel aTableParent = aTable.getParent();
            Map<String, DBColumn> aTableColumns = aTable.getColumns();
            DBPrimaryKey aTablePK = aTable.getPrimaryKey();
            List<DBForeignKey> aTableFKs = aTable.getForeignKeys();
            List<DBIndex> aTableIdxs = aTable.getIndexes();

            result &= (aTableName != null && name != null && name.equals(aTableName)) && (parentDBModel != null && aTableParent != null && parentDBModel.equals(aTableParent));

            if (columns != null && aTableColumns != null) {
                Set<String> objCols = aTableColumns.keySet();
                Set<String> myCols = columns.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                result &= myCols.containsAll(objCols) && objCols.containsAll(myCols);
            } else if (!(columns == null && aTableColumns == null)) {
                result = false;
            }

            result &= (primaryKey != null) ? primaryKey.equals(aTablePK) : aTablePK == null;

            if (foreignKeys != null && aTableFKs != null) {
                Collection<DBForeignKey> myFKs = foreignKeys.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myFKs.containsAll(aTableFKs) && aTableFKs.containsAll(myFKs);
            } else if (!(foreignKeys == null && aTableFKs == null)) {
                result = false;
            }

            if (indexes != null && aTableIdxs != null) {
                Collection<DBIndex> myIdxs = indexes.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myIdxs.containsAll(aTableIdxs) && aTableIdxs.containsAll(myIdxs);
            } else if (!(indexes == null && aTableIdxs == null)) {
                result = false;
            }
        }
        return result;
    }

    public String getAliasName() {
        return alias;
    }

    public String getCatalog() {
        return catalog;
    }
    
    public String getEscapeString() {
        return escapeString;
    }

    public void setEscapeString(String escapeString) {
        this.escapeString = escapeString;
    }    

    /**
     * Gets List of child SQLObjects belonging to this instance.
     * 
     * @return List of child SQLObjects
     */
    @Override
    public List<DBColumn> getChildDBObjects() {
        return this.getColumnList();
    }

    /**
     * Gets the DBColumn, if any, associated with the given name
     * 
     * @param columnName column name
     * @return DBColumn associated with columnName, or null if none exists
     */
    public DBColumn getColumn(String columnName) {
        return columns.get(columnName);
    }

    public List<DBColumn> getColumnList() {
        List<DBColumn> list = new ArrayList<DBColumn>();
        list.addAll(columns.values());
        Collections.sort(list, new ColumnOrderComparator());
        return list;
    }

    public Map<String, DBColumn> getColumns() {
        return columns;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get display name
     * 
     * @return display name
     */
    @Override
    public String getDisplayName() {
        return this.getQualifiedName();
    }

    public DBForeignKey getForeignKey(String fkName) {
        return foreignKeys.get(fkName);
    }

    public List<DBForeignKey> getForeignKeys() {
        return new ArrayList<DBForeignKey>(foreignKeys.values());
    }

    /**
     * get table fully qualified name including schema , catalog info
     * 
     * @return fully qualified table name prefixed with alias
     */
    public String getFullyQualifiedName() {

        String tblName = getName();
        String schName = getSchema();
        String catName = getCatalog();

        if (tblName == null) {
            throw new IllegalArgumentException("can not construct fully qualified table name, table name is null.");
        }

        StringBuilder buf = new StringBuilder(50);

        if (catName != null && catName.trim().length() != 0) {
            buf.append(escapeString).append(catName.trim()).append(escapeString);
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }

        if (schName != null && schName.trim().length() != 0) {
            buf.append(escapeString).append(schName.trim()).append(escapeString);
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }

        buf.append(escapeString).append(tblName.trim()).append(escapeString);

        return buf.toString();
    }

    public DBIndex getIndex(String indexName) {
        return indexes.get(indexName);
    }

    public List<DBIndex> getIndexes() {
        return new ArrayList<DBIndex>(indexes.values());
    }

    public synchronized String getName() {
        return name;
    }

    public DBModel getParent() {
        return parentDBModel;
    }

    public DBPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * get table qualified name
     * 
     * @return qualified table name prefixed with alias
     */
    public String getQualifiedName() {
        StringBuilder buf = new StringBuilder(50);
        String aName = this.getAliasName();
        if (aName != null && !aName.trim().equals("")) {
            buf.append("(");
            buf.append(aName);
            buf.append(") ");
            buf.append(this.getName());
        } else {
            buf.append(this.getFullyQualifiedName());
        }

        return buf.toString();
    }

    public Set getReferencedTables() {
        List keys = getForeignKeys();
        Set<DBTable> tables = new HashSet<DBTable>(keys.size());

        if (keys.size() != 0) {
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                DBForeignKey fk = (DBForeignKey) iter.next();
                DBTable pkTable = parentDBModel.getTable(fk.getPKTable(), fk.getPKSchema(), fk.getPKCatalog());
                if (pkTable != null && fk.references(pkTable.getPrimaryKey())) {
                    tables.add(pkTable);
                }
            }

            if (tables.size() == 0) {
                tables.clear();
                tables = Collections.emptySet();
            }
        }

        return tables;
    }

    public DBForeignKey getReferenceFor(DBTable target) {
        if (target == null) {
            return null;
        }

        DBPrimaryKey targetPK = target.getPrimaryKey();
        if (targetPK == null) {
            return null;
        }

        Iterator iter = foreignKeys.values().iterator();
        while (iter.hasNext()) {
            DBForeignKey myFK = (DBForeignKey) iter.next();
            if (myFK.references(targetPK)) {
                return myFK;
            }
        }

        return null;
    }

    public String getSchema() {
        return schema;
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        int myHash = super.hashCode();
        myHash = (name != null) ? name.hashCode() : 0;
        myHash += (parentDBModel != null) ? parentDBModel.hashCode() : 0;
        myHash += (schema != null) ? schema.hashCode() : 0;
        myHash += (catalog != null) ? catalog.hashCode() : 0;

        // Include hashCodes of all column names.
        if (columns != null) {
            myHash += columns.keySet().hashCode();
        }

        if (primaryKey != null) {
            myHash += primaryKey.hashCode();
        }

        if (foreignKeys != null) {
            myHash += foreignKeys.keySet().hashCode();
        }

        if (indexes != null) {
            myHash += indexes.keySet().hashCode();
        }

        myHash += (displayName != null) ? displayName.hashCode() : 0;

        return myHash;
    }

    /**
     * Get editable
     * 
     * @return true/false
     */
    public boolean isEditable() {
        return this.editable;
    }

    public boolean references(DBTable pkTarget) {
        return (getReferenceFor(pkTarget) != null);
    }

    /**
     * Dissociates the given ForeignKeyImpl from this DBTable instance, removing
     * it from its internal FK collection.
     * 
     * @param oldKey new ForeignKeyImpl instance to be removed
     * @return return true if removal succeeded, false otherwise
     */
    public boolean removeForeignKey(DBForeignKey oldKey) {
        if (oldKey != null) {
            return (foreignKeys.remove(oldKey.getName()) != null);
        }

        return false;
    }

    /**
     * set the alias name for this table
     * 
     * @param aName alias name
     */
    public void setAliasName(String aName) {
        this.alias = aName;
    }

    /**
     * Clones contents of the given Map to this table's internal column map, overwriting
     * any previous mappings.
     * 
     * @param theColumns Map of columns to be substituted
     * @return true if successful. false if failed.
     */
    public boolean setAllColumns(Map<String, DBColumn> theColumns) {
        columns.clear();
        if (theColumns != null) {
            columns.putAll(theColumns);
        }
        return true;
    }

    /**
     * Sets catalog name to new value.
     * 
     * @param newCatalog new value for catalog name
     */
    public void setCatalog(String newCatalog) {
        catalog = newCatalog;
    }

    /**
     * Sets description text for this instance.
     * 
     * @param newDesc new descriptive text
     */
    public void setDescription(String newDesc) {
        description = newDesc;
    }

    /**
     * Set editable
     * 
     * @param edit - editable
     */
    public void setEditable(boolean edit) {
        this.editable = edit;
    }

    /**
     * Sets table name to new value.
     * 
     * @param newName new value for table name
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Sets parentDBModel DatabaseModel to the given reference.
     * 
     * @param newParent new DatabaseModel parentDBModel
     */
    public void setParent(DBModel newParent) {
        parentDBModel = newParent;
        try {
            setParentObject(newParent);
        } catch (DBException ex) {
            // do nothing
        }
    }

    /**
     * Sets PrimaryKey instance for this DBTable to the given instance.
     * 
     * @param newPk new PrimaryKey instance to be associated
     * @return true if association succeeded, false otherwise
     */
    public boolean setPrimaryKey(DBPrimaryKey newPk) {
        if (newPk != null) {
            newPk.setParent(this);
        }

        primaryKey = newPk;
        return true;
    }

    public void setForeignKeyMap(Map<String, DBForeignKey> fkMap) {
        foreignKeys = fkMap;
    }

    /**
     * Sets schema name to new value.
     * 
     * @param newSchema new value for schema name
     */
    public void setSchema(String newSchema) {
        schema = newSchema;
    }

    /**
     * Overrides default implementation to return appropriate display name of this DBTable
     * 
     * @return qualified table name.
     */
    @Override
    public String toString() {
        return getQualifiedName();
    }

    /**
     * ColumnOrderComparator
     *
     */
    final class ColumnOrderComparator implements Comparator<DBColumn> {

        /*
         * Private constructor - to get an instance, use static method getInstance().
         *
         */
        public ColumnOrderComparator() {
        }

        public int compare(DBColumn col1, DBColumn col2) {
            return col1.getOrdinalPosition() - col2.getOrdinalPosition();
        }
    }
}


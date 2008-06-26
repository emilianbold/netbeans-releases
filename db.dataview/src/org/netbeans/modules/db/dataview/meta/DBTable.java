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
    private String catalog;
    private Map<String, DBColumn> columns;
    private Map<String, DBForeignKey> foreignKeys;
    private String name;
    private DBPrimaryKey primaryKey;
    private String schema;
    private String escapeString;

    /**
     * Creates a new instance of DBTable with the given name.
     * 
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     */
    public DBTable(String aName, String aSchema, String aCatalog) {
        columns = new LinkedHashMap<String, DBColumn>();
        foreignKeys = new HashMap<String, DBForeignKey>();

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
    public synchronized boolean addColumn(DBColumn theColumn) throws DBException {
        if (theColumn != null) {
            theColumn.setParent(this);
            columns.put(theColumn.getName(), theColumn);
            return true;
        }

        return false;
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

        DBModel parentDBModel = getParentObject();
        String refName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName((DBTable) refObj) : ((DBTable) refObj).getName();
        String myName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName(this) : name;
        return (myName != null) ? myName.compareTo(refName) : (refName != null) ? 1 : -1;
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
            Map<String, DBColumn> aTableColumns = aTable.getColumns();
            DBPrimaryKey aTablePK = aTable.primaryKey;
            List<DBForeignKey> aTableFKs = aTable.getForeignKeys();

            result &= (aTableName != null && name != null && name.equals(aTableName));

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
        }
        return result;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getEscapeString() {
        return escapeString;
    }

    void setEscapeString(String escapeString) {
        this.escapeString = escapeString;
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

    /**
     * Get display name
     * 
     * @return display name
     */
    @Override
    public String getDisplayName() {
        return this.getFullyQualifiedName();
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

    public String getName() {
        return name;
    }

    public DBPrimaryKey getPrimaryKey() {
        return primaryKey;
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

        myHash += (displayName != null) ? displayName.hashCode() : 0;

        return myHash;
    }

    /**
     * Sets PrimaryKey instance for this DBTable to the given instance.
     * 
     * @param newPk new PrimaryKey instance to be associated
     * @return true if association succeeded, false otherwise
     */
    boolean setPrimaryKey(DBPrimaryKey newPk) {
        if (newPk != null) {
            newPk.setParentObject(this);
        }

        primaryKey = newPk;
        return true;
    }

    void setForeignKeyMap(Map<String, DBForeignKey> fkMap) {
        foreignKeys = fkMap;
    }

    /**
     * Overrides default implementation to return appropriate display name of this DBTable
     * 
     * @return qualified table name.
     */
    @Override
    public String toString() {
        return getFullyQualifiedName();
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


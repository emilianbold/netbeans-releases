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
package org.netbeans.modules.sql.framework.model.impl;

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

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.NativeColumnOrderComparator;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.StringUtil;
import java.util.LinkedHashMap;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;
import org.netbeans.modules.sql.framework.model.ForeignKey;
import org.netbeans.modules.sql.framework.model.Index;
import org.netbeans.modules.sql.framework.model.PrimaryKey;

/**
 * Abstract implementation for org.netbeans.modules.model.database.DBTable and SQLObject interfaces.
 * 
 * @author Sudhendra Seshachala, Jonathan Giron
 * @version $Revision$
 */
public abstract class AbstractDBTable extends AbstractSQLObject implements SQLDBTable {

    static class StringComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                return ((String) o1).compareTo((String) o2);
            }
            throw new ClassCastException("StringComparator cannot compare non-String objects.");
        }
    }

    /** Attribute name for commit batch size. */
    protected static final String ATTR_COMMIT_BATCH_SIZE = "commitBatchSize";

    /** String constant for table catalog name attribute. */
    protected static final String CATALOG_NAME_ATTR = "catalog"; // NOI18N

    /** String constants for dbTableRef tag. */
    protected static final String DB_TABLE_REF = "dbTableRef"; // NOI18N

    /** String constant for table name attribute. */
    protected static final String DISPLAY_NAME_ATTR = "displayName"; // NOI18N

    /** String constant for table ID attribute. */
    protected static final String ID_ATTR = "id"; // NOI18N

    /** String to use in prefixing each line of a generated XML document */
    protected static final String INDENT = "\t";

    /** Initial buffer size for StringBuilder used in marshalling SQLTable to XML */
    protected static final int INIT_XMLBUF_SIZE = 500;

    /** Constant for column model name tag. */
    protected static final String MODEL_NAME_TAG = "dbModelName"; // NOI18N

    /** String onstant for table schema attribute. */
    protected static final String SCHEMA_NAME_ATTR = "schema"; // NOI18N

    /** String constant for table name attribute. */
    protected static final String TABLE_NAME_ATTR = "name"; // NOI18N

    private static final String ATTR_ALIAS_NAME = "aliasName";

    private static final String ATTR_FLATFILE_LOCATION_RUNTIME_INPUT_NAME = "flatFileLocationRuntimeInputName";

    private static final String ATTR_TABLE_PREFIX = "tablePrefix";

    private static final String ATTR_USERDEFINED_CATALOG_NAME = "userDefinedCatalogName";

    private static final String ATTR_USERDEFINED_SCHEMA_NAME = "userDefinedSchemaName";

    private static final String ATTR_USERDEFINED_TABLE_NAME = "userDefinedTableName";

    private static final String ATTR_USING_FULLYQUALIFIED_NAME = "usingFullyQualifiedName";

    private static final int DEFAULT_COMMIT_BATCH_SIZE = 5000;

    private static final String FQ_TBL_NAME_SEPARATOR = ".";

    // RFE-102428
    private static final String ATTR_STAGING_TABLE_NAME = "stagingTableName";
    
    /** use alias is required : transient variable */
    protected boolean aliasUsed = false;

    /** catalog to which this table belongs. */
    protected String catalog;

    /** Map of column metadata. */
    protected Map<String, DBColumn> columns;

    /** User-defined description. */
    protected String description;
    /** editable */
    protected boolean editable = true;

    /** Map of names to ForeignKey instances for this table; may be empty. */
    protected Map<String, ForeignKey> foreignKeys;

    /** Contains UI state information */
    protected GUIInfo guiInfo;

    /** Map of names to Index instances for this table; may be empty. */
    protected Map<String, Index> indexes;

    /** Table name as supplied by data source. */
    protected String name;

    protected boolean overrideCatalogName = false;

    protected String overridenCatalogName = null;

    protected String overridenSchemaName = null;
    protected boolean overrideSchemaName = false;

    /** Model instance that "owns" this table */
    protected DatabaseModel parentDBModel;
    /** PrimaryKey for this table; may be null. */
    protected PrimaryKeyImpl primaryKey;
    /** schema to which this table belongs. */
    protected String schema;
    /** selected */
    protected boolean selected;

    /** No-arg constructor; initializes Collections-related member variables. */
    protected AbstractDBTable() {
        columns = new LinkedHashMap<String, DBColumn>();
        foreignKeys = new HashMap<String, ForeignKey>();
        indexes = new HashMap<String, Index>();
        guiInfo = new GUIInfo();
        setDefaultAttributes();
    }

    /**
     * Creates a new instance of AbstractDBTable, cloning the contents of the given
     * DBTable implementation instance.
     * 
     * @param src DBTable instance to be 43d
     */
    protected AbstractDBTable(DBTable src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBTable instance for src param.");
        }
        copyFrom(src);
    }

    /**
     * Creates a new instance of AbstractDBTable with the given name.
     * 
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     */
    protected AbstractDBTable(String aName, String aSchema, String aCatalog) {
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
    public boolean addColumn(SQLDBColumn theColumn) {
        if (theColumn != null) {
            theColumn.setParent(this);
            columns.put(theColumn.getName(), theColumn);
            return true;
        }

        return false;
    }

    /**
     * Adds the given ForeignKeyImpl, associating it with this AbstractDBTable instance.
     * 
     * @param newFk new ForeignKeyImpl instance to be added
     * @return return true if addition succeeded, false otherwise
     */
    public boolean addForeignKey(ForeignKeyImpl newFk) {
        if (newFk != null) {
            newFk.setParent(this);
            foreignKeys.put(newFk.getName(), newFk);
            return true;
        }
        return false;
    }

    /**
     * Adds the given IndexImpl, associating it with this AbstractDBTable instance.
     * 
     * @param newIndex new IndexImpl instance to be added
     * @return return true if addition succeeded, false otherwise
     */
    public boolean addIndex(IndexImpl newIndex) {
        if (newIndex != null) {
            newIndex.setParent(this);
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

    public void clearOverride(boolean clearCatalogOverride, boolean clearSchemaOverride) {
        if (clearCatalogOverride) {
            this.overrideCatalogName = false;
            this.overridenCatalogName = null;
        }

        if (clearSchemaOverride) {
            this.overrideSchemaName = false;
            this.overridenSchemaName = null;
        }
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
     * Sets the various member variables and collections using the given DBTable instance
     * as a source object. Concrete implementations should override this method, call
     * super.copyFrom(DBColumn) to pick up member variables defined in this class and then
     * implement its own logic for copying member variables defined within itself.
     * 
     * @param source DBTable from which to obtain values for member variables and
     *        collections
     */
    public void copyFrom(DBTable source) {
        if (source == null) {
            throw new IllegalArgumentException("Must supply non-null ref for source");
        } else if (source == this) {
            return;
        }

        name = source.getName();
        description = source.getDescription();
        schema = source.getSchema();
        catalog = source.getCatalog();

        parentDBModel = source.getParent();

        if (source instanceof SQLDBTable) {
            SQLDBTable abstractTbl = (SQLDBTable) source;
            super.copyFromSource(abstractTbl);
            displayName = abstractTbl.getDisplayName();
            guiInfo = abstractTbl.getGUIInfo();
            aliasUsed = abstractTbl.isAliasUsed();
        }

        deepCopyReferences(source);
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
        if (!(obj instanceof SQLDBTable)) {
            return false;
        }

        result = super.equals(obj);

        if (!result) {
            return result;
        }

        SQLDBTable target = (SQLDBTable) obj;

        // since now we allow duplicate source tables we need to check the id and if id
        // is not equal then table is not equal
        result &= target.getId() != null ? target.getId().equals(this.getId()) : this.getId() == null;

        // Check for castability (also deals with null obj)
        if (obj instanceof DBTable) {
            DBTable aTable = (DBTable) obj;
            String aTableName = aTable.getName();
            DatabaseModel aTableParent = aTable.getParent();
            Map<String, DBColumn> aTableColumns = aTable.getColumns();
            PrimaryKey aTablePK = aTable.getPrimaryKey();
            List<ForeignKey> aTableFKs = aTable.getForeignKeys();
            List<Index> aTableIdxs = aTable.getIndexes();

            result &= (aTableName != null && name != null && name.equals(aTableName))
                && (parentDBModel != null && aTableParent != null && parentDBModel.equals(aTableParent));

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
                Collection<ForeignKey> myFKs = foreignKeys.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myFKs.containsAll(aTableFKs) && aTableFKs.containsAll(myFKs);
            } else if (!(foreignKeys == null && aTableFKs == null)) {
                result = false;
            }

            if (indexes != null && aTableIdxs != null) {
                Collection<Index> myIdxs = indexes.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myIdxs.containsAll(aTableIdxs) && aTableIdxs.containsAll(myIdxs);
            } else if (!(indexes == null && aTableIdxs == null)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#getAliasName()
     */
    public String getAliasName() {
        return (String) this.getAttributeObject(ATTR_ALIAS_NAME);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#getBatchSize()
     */
    public int getBatchSize() {
        Integer batchSize = (Integer) this.getAttributeObject(ATTR_COMMIT_BATCH_SIZE);
        return (batchSize != null) ? batchSize.intValue() : DEFAULT_COMMIT_BATCH_SIZE;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getCatalog
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Gets List of child SQLObjects belonging to this instance.
     * 
     * @return List of child SQLObjects
     */
    @Override
    public List<DBColumn> getChildSQLObjects() {
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

    /**
     * @see org.netbeans.modules.model.database.DBTable#getColumnList
     */
    public List<DBColumn> getColumnList() {
        List<DBColumn> list = new ArrayList<DBColumn>();
        list.addAll(columns.values());
        Collections.sort(list, NativeColumnOrderComparator.getInstance());

        return list;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getColumns
     */
    public Map<String, DBColumn> getColumns() {
        return columns;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getDescription
     */
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

    /**
     * Gets the flat file location runtime input name which is generate when a flat file
     * table is added to collaboration. use this name at runtime for file location passed
     * by eInsight
     * 
     * @return String representing flatfile location runtime input name
     */
    public String getFlatFileLocationRuntimeInputName() {
        return (String) this.getAttributeObject(ATTR_FLATFILE_LOCATION_RUNTIME_INPUT_NAME);
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getForeignKey(java.lang.String)
     */
    public ForeignKey getForeignKey(String fkName) {
        return foreignKeys.get(fkName);
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getForeignKeys
     */
    public List<ForeignKey> getForeignKeys() {
        return new ArrayList<ForeignKey>(foreignKeys.values());
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

    /**
     * @see SQLCanvasObject#getGUIInfo
     */
    public GUIInfo getGUIInfo() {
        return guiInfo;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getIndex
     */
    public Index getIndex(String indexName) {
        return indexes.get(indexName);
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getIndexes
     */
    public List<Index> getIndexes() {
        return new ArrayList<Index>(indexes.values());
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getName
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * Get specified SQL object
     * 
     * @param objectId - object ID
     * @return SQLObject
     */
    public SQLObject getObject(String objectId) {
        List list = this.getColumnList();
        Iterator it = list.iterator();

        while (it.hasNext()) {
            SQLDBColumn dbColumn = (SQLDBColumn) it.next();
            // if looking for table then return table
            if (objectId.equals(dbColumn.getId())) {
                return dbColumn;
            }
        }
        return null;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getParent
     */
    public DatabaseModel getParent() {
        return parentDBModel;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getPrimaryKey
     */
    public PrimaryKey getPrimaryKey() {
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

    /**
     * @see org.netbeans.modules.model.database.DBTable#getReferencedTables
     */
    public Set getReferencedTables() {
        List keys = getForeignKeys();
        Set<DBTable> tables = new HashSet<DBTable>(keys.size());

        if (keys.size() != 0) {
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                ForeignKeyImpl fk = (ForeignKeyImpl) iter.next();
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

    /**
     * @see org.netbeans.modules.model.database.DBTable#getReferenceFor
     */
    public ForeignKey getReferenceFor(DBTable target) {
        if (target == null) {
            return null;
        }

        PrimaryKey targetPK = target.getPrimaryKey();
        if (targetPK == null) {
            return null;
        }

        Iterator iter = foreignKeys.values().iterator();
        while (iter.hasNext()) {
            ForeignKey myFK = (ForeignKey) iter.next();
            if (myFK.references(targetPK)) {
                return myFK;
            }
        }

        return null;
    }

    public String getRuntimeArgumentName() {
        return this.getFlatFileLocationRuntimeInputName();
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getSchema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#getTablePrefix()
     */
    public String getTablePrefix() {
        return (String) this.getAttributeObject(ATTR_TABLE_PREFIX);
    }
    
    //RFE-102428
    /**
     * Gets the staging table name.
     * 
     * @return staging table name
     */
    public String getStagingTableName() {
    	return (String) this.getAttributeObject(ATTR_STAGING_TABLE_NAME);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#getUniqueTableName()
     */
    public String getUniqueTableName() {
        // Use alias name + given name to make this name consistent with the existing
        // name formats used in 5.0.x for flatfile runtime arguments.
        return this.getAliasName() + "_" + this.getName();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#getUserDefinedCatalogName()
     */
    public String getUserDefinedCatalogName() {
        if (overrideCatalogName) {
            return overridenCatalogName;
        } else {
            return (String) this.getAttributeObject(ATTR_USERDEFINED_CATALOG_NAME);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#getUserDefinedSchemaName()
     */
    public String getUserDefinedSchemaName() {
        if (overrideSchemaName) {
            return overridenSchemaName;
        } else { 
            return (String) this.getAttributeObject(ATTR_USERDEFINED_SCHEMA_NAME);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#getUserDefinedTableName()
     */
    public String getUserDefinedTableName() {
        return (String) this.getAttributeObject(ATTR_USERDEFINED_TABLE_NAME);
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
     * @return Returns the aliasUsed.
     */
    public boolean isAliasUsed() {
        return aliasUsed;
    }

    /**
     * Get editable
     * 
     * @return true/false
     */
    public boolean isEditable() {
        return this.editable;
    }

    public boolean isInputStatic(String inputName) {
        return false;
    }

    /**
     * Get selected
     * 
     * @return selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#isUsingFullyQualifiedName()
     */
    public boolean isUsingFullyQualifiedName() {
        Boolean isUsing = (Boolean) getAttributeObject(ATTR_USING_FULLYQUALIFIED_NAME);
        return (isUsing != null) ? isUsing.booleanValue() : true;
    }

    public void overrideCatalogName(String nName) {
        this.overrideCatalogName = true;
        this.overridenCatalogName = nName;
    }

    public void overrideSchemaName(String nName) {
        this.overrideSchemaName = true;
        this.overridenSchemaName = nName;
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     * 
     * @param tableElement DOM element containing XML marshalled version of a
     * @exception BaseException thrown while parsing XML, or if member variable element is
     *            null
     */
    @Override
    public void parseXML(Element tableElement) throws BaseException {
        if (tableElement == null) {
            throw new BaseException("Null ref for tableElement.");
        }

        if (!(tableElement.getNodeName().equals(getElementTagName()))) {
            throw new BaseException("No <" + getElementTagName() + "> element found.");
        }

        super.parseXML(tableElement);

        name = tableElement.getAttribute(TABLE_NAME_ATTR);
        schema = tableElement.getAttribute(SCHEMA_NAME_ATTR);
        catalog = tableElement.getAttribute(CATALOG_NAME_ATTR);

        NodeList childNodeList = tableElement.getChildNodes();
        parseChildren(childNodeList);
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#references
     */
    public boolean references(DBTable pkTarget) {
        return (getReferenceFor(pkTarget) != null);
    }

    /**
     * Dissociates the given ForeignKeyImpl from this AbstractDBTable instance, removing
     * it from its internal FK collection.
     * 
     * @param oldKey new ForeignKeyImpl instance to be removed
     * @return return true if removal succeeded, false otherwise
     */
    public boolean removeForeignKey(ForeignKeyImpl oldKey) {
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
        this.setAttribute(ATTR_ALIAS_NAME, aName);
    }

    /**
     * @param aliasUsed The aliasUsed to set.
     */
    public void setAliasUsed(boolean aliasUsed) {
        this.aliasUsed = aliasUsed;
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

    public void setBatchSize(int newSize) {
        if (newSize < 0) {
            newSize = DEFAULT_COMMIT_BATCH_SIZE;
        }

        this.setAttribute(ATTR_COMMIT_BATCH_SIZE, new Integer(newSize));
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
     * set flat file location runtime input name which is generate when a flat file table
     * is added to collaboration
     * 
     * @param runtimeArgName name of runtime input argument for flat file location
     */
    public void setFlatFileLocationRuntimeInputName(String runtimeArgName) {
        this.setAttribute(ATTR_FLATFILE_LOCATION_RUNTIME_INPUT_NAME, runtimeArgName);
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
    public void setParent(SQLDBModel newParent) {
        parentDBModel = newParent;
        try {
            setParentObject(newParent);
        } catch (BaseException ex) {
            // do nothing
        }
    }

    /**
     * Sets PrimaryKey instance for this DBTable to the given instance.
     * 
     * @param newPk new PrimaryKey instance to be associated
     * @return true if association succeeded, false otherwise
     */
    public boolean setPrimaryKey(PrimaryKeyImpl newPk) {
        if (newPk != null) {
            newPk.setParent(this);
        }

        primaryKey = newPk;
        return true;
    }
    
    public void setForeignKeyMap(Map<String, ForeignKey> fkMap){
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
     * Set selected
     * 
     * @param sel - selected
     */
    public void setSelected(boolean sel) {
        this.selected = sel;
    }

    public void setTablePrefix(String tPrefix) {
        this.setAttribute(ATTR_TABLE_PREFIX, tPrefix);
    }

    //RFE-102428
    /**
     * Sets the staging table name.
     * 
     * @param stName staging table name
     */
    public void setStagingTableName(String stName) {
    	this.setAttribute(ATTR_STAGING_TABLE_NAME, stName);
    }
    
    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#setUserDefinedCatalogName(java.lang.String)
     */
    public void setUserDefinedCatalogName(String newName) {
        this.setAttribute(ATTR_USERDEFINED_CATALOG_NAME, newName);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#setUserDefinedSchemaName(java.lang.String)
     */
    public void setUserDefinedSchemaName(String newName) {
        this.setAttribute(ATTR_USERDEFINED_SCHEMA_NAME, newName);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#setUserDefinedTableName(java.lang.String)
     */
    public void setUserDefinedTableName(String newName) {
        this.setAttribute(ATTR_USERDEFINED_TABLE_NAME, newName);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDBTable#setUsingFullyQualifiedName(boolean)
     */
    public void setUsingFullyQualifiedName(boolean usesFullName) {
        this.setAttribute(ATTR_USING_FULLYQUALIFIED_NAME, (usesFullName ? Boolean.TRUE : Boolean.FALSE));
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
     * @see SQLObject#toXMLString
     */
    @Override
    public String toXMLString(String prefix) throws BaseException {
        return toXMLString(prefix, false);
    }

    /**
     * Returns XML representation of table metadata.
     * 
     * @param prefix prefix for the xml.
     * @param tableOnly flag for generating table only metadata.
     * @return XML representation of the table metadata.
     * @exception BaseException - exception
     */
     public String toXMLString(String prefix, boolean tableOnly) throws BaseException {
            throw new UnsupportedOperationException("Not supported yet.");
     }

    /**
     * Perform deep copy of columns.
     * 
     * @param source SQLTable whose columns are to be copied.
     */
    protected void deepCopyReferences(DBTable source) {
        if (source != null && source != this) {
            primaryKey = null;
            PrimaryKey srcPk = source.getPrimaryKey();
            if (srcPk != null) {
                primaryKey = new PrimaryKeyImpl(source.getPrimaryKey());
                primaryKey.setParent(this);
            }

            foreignKeys.clear();
            Iterator iter = source.getForeignKeys().iterator();
            while (iter.hasNext()) {
                ForeignKeyImpl impl = new ForeignKeyImpl((ForeignKey) iter.next());
                impl.setParent(this);
                foreignKeys.put(impl.getName(), impl);
            }

            indexes.clear();
            iter = source.getIndexes().iterator();

            while (iter.hasNext()) {
                IndexImpl impl = new IndexImpl((Index) iter.next());
                impl.setParent(this);
                indexes.put(impl.getName(), impl);
            }

            columns.clear();
            iter = source.getColumnList().iterator();
            while (iter.hasNext()) {
                try {
                    SQLDBColumn column = (SQLDBColumn) iter.next();
                    SQLDBColumn clonedColumn = (SQLDBColumn) column.cloneSQLObject();
                    columns.put(clonedColumn.getName(), clonedColumn);
                } catch (Exception ex) {
                    // TODO Log this exception
                }
            }
        }
    }

    /**
     * Gets String representing tag name for this table class.
     * 
     * @return String representing element tag for this class
     */
     protected String getElementTagName() {
        throw new UnsupportedOperationException("Not supported yet.");
     }

    /**
     * Parses node elements to extract child components to various collections (columns,
     * PK, FK, indexes).
     * 
     * @param childNodeList Nodes to be unmarshalled
     * @throws BaseException if error occurs while parsing
     */
     protected void parseChildren(NodeList childNodeList) throws BaseException {
            throw new UnsupportedOperationException("Not supported yet.");
     }

         /**
     * Sets default values for attributes defined in this abstract class.
     */
    protected void setDefaultAttributes() {
        setUserDefinedTableName("");
        setUserDefinedSchemaName("");
        setUserDefinedCatalogName("");
        setTablePrefix("");
        setAliasName("");
        setUsingFullyQualifiedName(true);
        setStagingTableName("");
    }
    
    public static String getResolvedCatalogName(SQLDBTable t) {
        // Ensure order of precedence for catalog name is followed.
        String resolvedCatalogName = t.getUserDefinedCatalogName();
        if (StringUtil.isNullString(resolvedCatalogName)) {
            resolvedCatalogName = t.getCatalog();
        }
        return resolvedCatalogName;
    }
        
    public static String getResolvedSchemaName(SQLDBTable t) {
        // Ensure order of precedence for schema name is followed.
        String resolvedSchemaName = t.getUserDefinedSchemaName();
        if (StringUtil.isNullString(resolvedSchemaName)) {
            resolvedSchemaName = t.getSchema();
        }
        return resolvedSchemaName;
    }
    
    public static String getResolvedTableName(SQLDBTable t) {
        // Ensure order of precedence for schema name is followed.
        String resolvedTableName = t.getUserDefinedTableName();
        if (StringUtil.isNullString(resolvedTableName)) {
            resolvedTableName = t.getName();
        }
        return resolvedTableName;
    }
}


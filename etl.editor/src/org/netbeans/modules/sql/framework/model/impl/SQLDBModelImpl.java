/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.netbeans.modules.etl.model.ETLObject;
import org.netbeans.modules.mashup.db.common.Property;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLFrameworkParentObject;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConnectionParameters;
import java.io.File;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.openide.awt.StatusDisplayer;

/**
 * SQLBuilder-specific concrete implementation of DatabaseModel interface.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SQLDBModelImpl extends AbstractSQLObject implements Cloneable, SQLDBModel {

    private static transient final Logger mLogger = Logger.getLogger(SQLDBModelImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SQLDBModelImpl.class.getName());
    /** Initial buffer size for StringBuilder used in marshaling Databases to XML */
    protected static final int INIT_XMLBUF_SIZE = 1000;
    /*
     * String used to separate name, schema, and/or catalog Strings in a
     * fully-qualified table name.
     */
    private static final String FQ_TBL_NAME_SEPARATOR = ".";
    /* String to use in prefixing each line of a generated XML document */
    private static final String INDENT = "\t";
    /* Initial buffer size for StringBuilder used in marshaling Databases to XML */
    private static final String LOG_CATEGORY = SQLDBModelImpl.class.getName();
    /** Connection definition used to retrieve metadata */
    protected DBConnectionDefinition connectionDefinition;
    /** User-supplied description */
    protected volatile String description;
    /** User-supplied name */
    protected volatile String name;
    /** Map of DBTable instances */
    protected Map tables;
    private transient SQLFrameworkParentObject mParent;
    /* Database that supplied metadata for this DatabaseModel instance. */
    protected transient ETLObject source;

    /** Constructs a new default instance of SQLDBModelImpl. */
    public SQLDBModelImpl() {
        tables = new HashMap();
        type = SQLConstants.SOURCE_DBMODEL;
        setRefKey("{" + UUID.randomUUID().toString() + "}");
    }

    /**
     * @type SQLConstants.SOURCE_DBMODEL or SQLConstants.TARGET_DBMODEL
     */
    public SQLDBModelImpl(int type) {
        this();
        this.type = type;
    }

    /**
     * Creates a new instance of SQLDBModelImpl, cloning the contents of the
     * given DatabaseModel implementation instance.
     *
     * @param src
     *            DatabaseModel instance to be cloned
     * @param modelType
     *            model type, either SOURCE_DBMODEL or TARGET_DBMODEL
     * @see SQLConstants#SOURCE_DBMODEL
     * @see SQLConstants#TARGET_DBMODEL
     */
    public SQLDBModelImpl(DatabaseModel src, int modelType, SQLFrameworkParentObject sqlParent) {
        this();

        if (src == null) {
            throw new IllegalArgumentException(
                    "Must supply non-null DatabseModel instance for src param.");
        }

        mParent = sqlParent;
        copyFrom(src, modelType);

        if (src instanceof ETLObject) {
            setSource((ETLObject) src);
        }
    }

    /**
     * Adds table to this instance.
     *
     * @param table
     *            new table to add
     * @throws IllegalStateException
     *             if unable to add table
     */
    public void addTable(SQLDBTable table) throws IllegalStateException {
        if (table != null) {

            if (type == SQLConstants.TARGET_DBMODEL && table.getObjectType() != SQLConstants.TARGET_TABLE) {
                throw new IllegalStateException(
                        "Cannot add TargetTable to a non-target DatabaseModel!");
            }

            if (type == SQLConstants.SOURCE_DBMODEL && table.getObjectType() != SQLConstants.SOURCE_TABLE) {
                throw new IllegalStateException(
                        "Cannot add TargetTable to a non-target DatabaseModel!");
            }

            // if table already exists then we should throw exception
            String fqName = getFullyQualifiedTableName(table);
            if (this.getTable(fqName) != null) {
                //throw new IllegalStateException("Cannot add table " + fqName + ", it already exist!");
            }

            table.setParent(this);
            tables.put(fqName, table);
        }
    }

    public void clearOverride(boolean clearCatalogOverride, boolean clearSchemaOverride) {
        List tbls = getTables();
        Iterator itr = tbls.iterator();
        SQLDBTable table = null;
        while (itr.hasNext()) {
            table = (SQLDBTable) itr.next();
            table.clearOverride(clearCatalogOverride, clearSchemaOverride);
        }
    }

    /**
     * Clones this object.
     *
     * @return shallow copy of this SQLDataSource
     */
    @Override
    public Object clone() {
        try {
            SQLDBModelImpl myClone = (SQLDBModelImpl) super.clone();

            myClone.attributes = new HashMap(attributes);
            myClone.name = name;
            myClone.id = id;
            myClone.displayName = displayName;
            myClone.description = description;

            myClone.tables = new HashMap();
            tables.putAll(tables);

            myClone.connectionDefinition = new SQLDBConnectionDefinitionImpl(
                    getConnectionDefinition());

            return myClone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * check if a table exists This will check if a table is in database model,
     */
    public boolean containsTable(SQLDBTable table) {
        if (this.getTable(this.getFullyQualifiedTableName(table)) != null) {
            return true;
        }

        return false;
    }

    /**
     * Copies member values to those contained in the given DatabaseModel
     * instance.
     *
     * @param src
     *            DatabaseModel whose contents are to be copied into this
     *            instance
     */
    public void copyFrom(DatabaseModel src) {
        if (src instanceof SQLDBModel) {
            copyFrom(src, ((SQLDBModel) src).getObjectType());
        } else {
            copyFrom(src, SQLConstants.SOURCE_DBMODEL);
        }
    }

    /**
     * Copies member values to those contained in the given DatabaseModel
     * instance, using the given value for object type.
     *
     * @param src
     *            DatabaseModel whose contents are to be copied into this
     *            instance
     * @param objType
     *            type of object (SOURCE_DBMODEL or TARGET_DBMODEL)
     */
    public void copyFrom(DatabaseModel src, int objType) {
        if (src != null) {
            name = src.getModelName();
            description = src.getModelDescription();
            type = objType;

            // Defer creation of connection info - lazy load only when
            // getConnectionDefinition() is called.
            connectionDefinition = new SQLDBConnectionDefinitionImpl(src.getConnectionDefinition());

            tables.clear();
            List srcTables = src.getTables();
            if (srcTables != null) {
                Iterator iter = srcTables.iterator();
                while (iter.hasNext()) {
                    DBTable tbl = (DBTable) iter.next();
                    SQLDBTable localTable = null;

                    switch (type) {
                        case SQLConstants.SOURCE_DBMODEL:
                            localTable = SQLModelObjectFactory.getInstance().createSourceTable(tbl);
                            addTable(localTable);
                            break;

                        case SQLConstants.TARGET_DBMODEL:
                            localTable = SQLModelObjectFactory.getInstance().createTargetTable(tbl);
                            addTable(localTable);
                            break;
                    }
                }
            }

            if (src instanceof SQLDBModel) {
                SQLDBModel object = (SQLDBModel) src;

                id = object.getId();
                displayName = object.getDisplayName();
                parentObject = object.getParentObject();
            }

            // if (src instanceof JDBCConnectionProvider) {
            // try {
            // String dBPath = ProjectUtil.getProjectPath((ProjectElement) src,
            // true);
            // connectionDefinition.setDbPathName(dBPath);
            //
            // if (src instanceof ProjectElement){
            // displayName = ((ProjectElement) src).getName();
            // }
            // } catch (Exception ex) {
            // // Log the exception.
            // }
            // } else {
            // ETLObject repObj = src.getSource();
            // if (repObj instanceof JDBCConnectionProvider) {
            // try {
            // String dBPath = ProjectUtil.getProjectPath((ProjectElement)
            // repObj, true);
            // connectionDefinition.setDBPathName(dBPath);
            // } catch (Exception ex) {
            // // Log the exception.
            // }
            // }
            // }

            setSource(src.getSource());
        }
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
        SQLDBTable table = null;

        if (tableName == null || tableName.length() == 0) {
            throw new IllegalArgumentException("tableName cannot be null");
        }

        switch (type) {
            case SQLConstants.SOURCE_DBMODEL:
                table = SQLModelObjectFactory.getInstance().createSourceTable(tableName, schemaName,
                        catalogName);
                addTable(table);
                break;

            case SQLConstants.TARGET_DBMODEL:
                table = SQLModelObjectFactory.getInstance().createTargetTable(tableName, schemaName,
                        catalogName);
                addTable(table);
                break;
        }

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

    /**
     * @see java.lang.Object#equals
     */
    @Override
    public boolean equals(Object refObj) {
        // Check for reflexivity.
        if (this == refObj) {
            return true;
        }

        boolean result = false;

        // Ensure castability (also checks for null refObj)
        if (refObj instanceof SQLDBModelImpl) {
            SQLDBModelImpl aSrc = (SQLDBModelImpl) refObj;

            result = ((aSrc.name != null) ? aSrc.name.equals(name) : (name == null));

            DBConnectionDefinition myConnDef = this.getConnectionDefinition();
            DBConnectionDefinition srcConnDef = aSrc.getConnectionDefinition();
            boolean connCheck = ((srcConnDef != null) ? srcConnDef.equals(myConnDef)
                    : (myConnDef == null));
            result &= connCheck;

            boolean typeCheck = (aSrc.type == type);
            result &= typeCheck;

            if (tables != null && aSrc.tables != null) {
                Set objTbls = aSrc.tables.keySet();
                Set myTbls = tables.keySet();

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
    public synchronized Map getAllSQLTables() {
        return tables;
    }

    /**
     * get a list of tables based on table name, schema name and catalog name
     * since we allow duplicate tables this will return a list of tables
     */
    public List getAllTables(String tableName, String schemaName, String catalogName) {

        ArrayList tbls = new ArrayList();

        Iterator it = this.tables.values().iterator();
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
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
    public List getChildSQLObjects() {
        return this.getTables();
    }

    /**
     * Gets SQLDBConnectionDefinition of the SQLDataSource object
     *
     * @return ConnectionDefinition of the SQLDataSource object
     */
    public DBConnectionDefinition getConnectionDefinition() {
        try {
            return getETLDBConnectionDefinition();
        } catch (BaseException e) {
            throw new IllegalStateException("Could not obtain reference to DBConnectionDefinition");
        }
    }

    /**
     * Gets SQLDBConnectionDefinition of the SQLDataSource object
     *
     * @return ConnectionDefinition of the SQLDataSource object
     */
    public DBConnectionDefinition getETLDBConnectionDefinition() throws BaseException {
        if (connectionDefinition == null) {
            mLogger.infoNoloc(mLoc.t("EDIT114: Lazy loading connection definition for DB model{0}", getDisplayName()));
            connectionDefinition = createETLDBConnectionDefinition();
        }
        return connectionDefinition;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractSQLObject#getFooter
     */
    @Override
    public String getFooter() {
        return "";
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getFullyQualifiedTableName(DBTable)
     */
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

            // since now we allow duplicate tables we need to make sure map
            // entries are
            // unique so we will add id also to fully qualified map if it is
            // available.
            if (tbl instanceof SQLDBTable) {
                SQLDBTable table = (SQLDBTable) tbl;

                String id1 = table.getId();
                String alias = table.getAliasName();
                if (id1 != null && id1.trim().length() != 0) {
                    buf.append(id1.trim());
                    buf.append(FQ_TBL_NAME_SEPARATOR);
                } else if (alias != null && alias.trim().length() != 0) {
                    buf.append(alias.trim());
                    buf.append(FQ_TBL_NAME_SEPARATOR);
                }
            }

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

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getFullyQualifiedTableName(
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public String getFullyQualifiedTableName(String tblName, String schName, String catName) {
        throw new IllegalAccessError(
                "This method is not supported, use getFullyQualifiedTableName(DBTable) instead.");
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractSQLObject#getHeader
     */
    @Override
    public String getHeader() {
        return "";
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getModelDescription
     */
    public String getModelDescription() {
        return description;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getModelName
     */
    public String getModelName() {
        return this.name;
    }

    /**
     * Gets SQLObject, if any, having the given object ID.
     *
     * @param objectId
     *            ID of SQLObject being sought
     * @return SQLObject associated with objectID, or null if no such object
     *         exists.
     */
    public SQLObject getObject(String objectId) {
        List list = this.getTables();
        Iterator it = list.iterator();

        while (it.hasNext()) {
            SQLDBTable dbTable = (SQLDBTable) it.next();
            // if looking for table then return table
            if (objectId.equals(dbTable.getId())) {
                return dbTable;
            }

            // check tables child list and see if a column is found
            SQLObject columnObj = dbTable.getObject(objectId);
            if (columnObj != null) {
                return columnObj;
            }
        }

        return null;
    }

    public String getRefKey() {
        return (String) getAttributeObject(REFKEY);
    }

    /**
     * Gets repository object, if any, providing underlying data for this
     * DatabaseModel implementation.
     *
     * @return ETLObject hosting this object's metadata, or null if data are not
     *         held by a ETLObject.
     */
    public ETLObject getSource() {
        return this.source;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getTable(java.lang.String)
     *      fully qualified name should be catalog.schema.table.id
     */
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
            SQLDBTable table = (SQLDBTable) it.next();
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

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getTables
     */
    public List getTables() {
        List list = Collections.EMPTY_LIST;
        Collection tableColl = tables.values();

        if (tableColl.size() != 0) {
            list = new ArrayList(tableColl.size());
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
        myHash += (connectionDefinition != null) ? connectionDefinition.hashCode() : 0;
        myHash += type;

        if (tables != null) {
            myHash += tables.keySet().hashCode();
        }

        return myHash;
    }

    public void overrideCatalogNames(Map catalogOverride) {
        List tbls = getTables();
        Iterator itr = tbls.iterator();
        SQLDBTable table = null;
        String origName = null;
        String newName = null;

        while (itr.hasNext()) {
            table = (SQLDBTable) itr.next();
            origName = table.getCatalog();
            if (origName != null) {
                newName = (String) catalogOverride.get(origName);
                if (newName != null) {
                    table.overrideCatalogName(newName);
                }
            }
        }
    }

    public void overrideSchemaNames(Map schemaOverride) {
        List tbls = getTables();
        Iterator itr = tbls.iterator();
        SQLDBTable table = null;
        String origName = null;
        String newName = null;

        while (itr.hasNext()) {
            table = (SQLDBTable) itr.next();
            origName = table.getSchema();
            if (origName != null) {
                newName = (String) schemaOverride.get(origName);
                if (newName != null) {
                    table.overrideSchemaName(newName);
                }
            }
        }
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     *
     * @param dbElement
     *            DOM element containing XML marshalled version of a
     * @exception BaseException
     *                thrown while parsing XML, or if member variable element is
     *                null
     */
    @Override
    public void parseXML(Element dbElement) throws BaseException {
        if (dbElement == null) {
            throw new BaseException(
                    "Must supply non-null org.w3c.dom.Element ref for element. No <" + MODEL_TAG + "> element found.");
        }

        if (!MODEL_TAG.equals(dbElement.getNodeName())) {
            throw new BaseException("Invalid root element; expected " + MODEL_TAG + ", got " + dbElement.getNodeName());
        }

        super.parseXML(dbElement);

        name = dbElement.getAttribute(NAME);

        String typeStr = dbElement.getAttribute(TYPE);

        NodeList childNodeList = null;

        if (STRTYPE_TARGET.equals(typeStr)) {
            type = SQLConstants.TARGET_DBMODEL;
            childNodeList = dbElement.getElementsByTagName(SQLDBTable.TABLE_TAG);
            parseTargetTables(childNodeList);
        } else if (STRTYPE_SOURCE.equals(typeStr)) {
            type = SQLConstants.SOURCE_DBMODEL;
            childNodeList = dbElement.getElementsByTagName(SQLDBTable.TABLE_TAG);
            parseSourceTables(childNodeList);
        } else {
            throw new BaseException("Missing or invalid modelType attribute: " + typeStr);
        }

        childNodeList = dbElement.getElementsByTagName(DBConnectionParameters.CONNECTION_DEFINITION_TAG);
        int length = childNodeList.getLength();

        for (int i = 0; i < length; i++) {
            if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element tmpElement = (Element) (childNodeList.item(i));

                this.connectionDefinition = new SQLDBConnectionDefinitionImpl();
                ((DBConnectionParameters) this.connectionDefinition).parseXML(tmpElement);

            }
        }

        String connNamePrefix = type == SQLConstants.SOURCE_DBMODEL?"SourceConnection":"TargetConnection";
        if (!name.startsWith(connNamePrefix)) {
            String modelName = generateDBModelName(type == SQLConstants.SOURCE_DBMODEL);
            name = modelName;
            ((SQLDBConnectionDefinitionImpl) connectionDefinition).setName(name);
                DataObjectProvider.getProvider().getActiveDataObject().getETLEditorSupport().setUpdatedDuringLoad(true);        
        }
    }
    
        
     private String generateDBModelName(boolean isSource) {
        int cnt = 1;
        String connNamePrefix = isSource ? "SourceConnection" : "TargetConnection";
        String aName = connNamePrefix + cnt;
        while (isDBModelNameExist(isSource, aName)) {
            cnt++;
            aName = connNamePrefix + cnt;
        }

        return aName;
    }
    
     private boolean isDBModelNameExist(boolean isSource, String aName) {
        Iterator<SQLDBModel> it  = ((SQLDefinition) this.getParentObject()).getAllDatabases().iterator();
        while (it.hasNext()) {
            SQLDBModel dbModel = it.next();
            String dbName = dbModel.getModelName();
            if (dbName != null && dbName.equals(aName)) {
                return true;
            }
        }
        return false;
    }


    public void setConnectionDefinition(DBConnectionDefinition dbConnectionDef) {
        this.connectionDefinition = new SQLDBConnectionDefinitionImpl(dbConnectionDef);
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
     * @see org.netbeans.modules.model.database.DatabaseModel#getModelName
     */
    public void setModelName(String name) {
        if (name.startsWith(DBExplorerUtil.AXION_URL_PREFIX) && name.contains(ETLEditorSupport.PRJ_PATH)) {
            String[] urlParts = DBExplorerUtil.parseConnUrl(name);
            this.name = urlParts[0];
        } else {
            if (name.length() > 60) {
                this.name = name.substring(name.length() - 60);
            } else {
                this.name = name;
            }
        }
    }

    public void setRefKey(String aKey) {
        setAttribute(REFKEY, aKey);
    }

    /**
     * Sets repository object, if any, providing underlying data for this
     * DatabaseModel implementation.
     *
     * @param obj
     *            Object hosting this object's metadata, or null if data are not
     *            held by a ETLObject.
     */
    public void setSource(ETLObject obj) {
        source = obj;
    }

    @SuppressWarnings("unchecked")
    public void setSQLFrameworkParentObject(SQLFrameworkParentObject aParent) {
        if (mParent == null) {
            mParent = aParent;
        }

        if (getRefKey() == null) {
            this.setSource(source);
        }
    }

    private boolean validateConnDefinition(DBConnectionDefinition connDef) {
        String dbUrl = connDef.getConnectionURL();
        Pattern pattern = Pattern.compile("jdbc:axiondb:");
        java.util.regex.Matcher matcher = pattern.matcher(dbUrl);
        dbUrl = matcher.replaceAll("");
        StringTokenizer tokenizer = new StringTokenizer(dbUrl, ":");
        String conPath = null;
        ArrayList tmpList = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            tmpList.add(tokenizer.nextToken());
        }
        String[] recordSeps = (String[]) tmpList.toArray(new String[0]);
        //Check if conPath is a valid file/directory
        StringBuffer sb = new StringBuffer();
        if (recordSeps[1] != null && recordSeps[1].length() != 0) {
            sb.append(recordSeps[1]);
        }
        if (recordSeps[2] != null && recordSeps[2].length() != 0) {
            sb.append(":").append(recordSeps[2]);
        }
        File f = new File(sb.toString());
        if (!f.exists()) {
            StatusDisplayer.getDefault().setStatusText("Check ConnectionDefinition." + conPath);
            return false;
        }
        return true;
    }

    /**
     * @param condef
     * @param element
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, Property> getTableMetaData(DBConnectionDefinition conndef, SQLDBTable element) {
        HashMap<String, Property> map = new HashMap<String, Property>();
        final String prefix = "ORGPROP_";
        Collection<String> attrNames = (Collection<String>) element.getAttributeNames();
        for (String attrName : attrNames) {
            Object attrValue = element.getAttributeObject(attrName);
            if (attrName.startsWith(prefix)) {
                attrName = attrName.substring(attrName.indexOf(prefix) + prefix.length());
                Property prop = new Property();
                if (attrValue instanceof java.lang.Boolean) {
                    prop = new Property(attrName, Boolean.class, true);
                    prop.setValue((Boolean) attrValue);
                    map.put(attrName, prop);
                } else if (attrValue instanceof java.lang.Integer) {
                    prop = new Property(attrName, Integer.class, true);
                    prop.setValue((Integer) attrValue);
                    map.put(attrName, prop);
                } else {
                    prop = new Property(attrName, String.class, true);
                    prop.setValue((String) attrValue);
                    map.put(attrName, prop);
                }
            }
        }
        return map;
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

    /**
     * Gets xml representation of this DatabaseModel instance.
     *
     * @param prefix
     *            for this xml.
     * @return Return the xml representation of data source metadata.
     * @exception BaseException -
     *                exception
     */
    @Override
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder xml = new StringBuilder(INIT_XMLBUF_SIZE);
        if (prefix == null) {
            prefix = "";
        }

        xml.append(prefix).append("<").append(MODEL_TAG).append(" ").append(NAME).append("=\"").append(name.trim()).append("\"");

        if (id != null && id.trim().length() != 0) {
            xml.append(" ").append(ID).append("=\"").append(id.trim()).append("\"");
        }

        if (displayName != null && displayName.trim().length() != 0) {
            xml.append(" ").append(DISPLAY_NAME).append("=\"").append(displayName.trim()).append(
                    "\"");
        }

        switch (type) {
            case SQLConstants.SOURCE_DBMODEL:
                xml.append(" " + TYPE + "=\"").append(STRTYPE_SOURCE).append("\"");
                break;

            case SQLConstants.TARGET_DBMODEL:
                xml.append(" " + TYPE + "=\"").append(STRTYPE_TARGET).append("\"");
                break;

            default:
                break;
        }

        xml.append(">\n");

        xml.append(super.toXMLAttributeTags(prefix));

        // write out tables
        writeTables(prefix, xml);

        // write connection defs
        xml.append(getXMLConnectionDefition());

        xml.append(prefix).append("</").append(MODEL_TAG).append(">\n");
        return xml.toString();
    }

    /**
     * Extracts SourceTable instances from the given NodeList.
     *
     * @param tableNodeList
     *            Nodes to be unmarshaled
     * @throws BaseException
     *             if error occurs while parsing
     */
    protected void parseSourceTables(NodeList tableNodeList) throws BaseException {
        for (int i = 0; i < tableNodeList.getLength(); i++) {
            if (tableNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element tableElement = (Element) tableNodeList.item(i);

                SourceTable table = new SourceTableImpl();
                table.setParentObject(this);

                table.parseXML(tableElement);
                addTable(table);
            }
        }
    }

    /**
     * Extracts TargetTable instances from the given NodeList.
     *
     * @param tableNodeList
     *            Nodes to be unmarshaled
     * @throws BaseException
     *             if error occurs while parsing
     */
    protected void parseTargetTables(NodeList tableNodeList) throws BaseException {
        for (int i = 0; i < tableNodeList.getLength(); i++) {
            if (tableNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element tableElement = (Element) tableNodeList.item(i);

                TargetTable table = new TargetTableImpl();
                table.setParentObject(this);

                table.parseXML(tableElement);
                this.addTable(table);
            }
        }
    }

    /**
     * Write table
     *
     * @param prefix -
     *            prefix
     * @param xml -
     *            StringBuilder
     * @throws BaseException -
     *             exception
     */
    protected void writeTables(String prefix, StringBuilder xml) throws BaseException {
        // Ensure tables are written out in ascending name order.
        List tblList = new ArrayList(tables.keySet());
        Collections.sort(tblList, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    return ((String) o1).compareTo((String) o2);
                }
                throw new ClassCastException("Cannot compare objects from different classes");
            }
        });

        Iterator iter = tblList.listIterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            SQLDBTable table = (SQLDBTable) tables.get(key);
            xml.append(table.toXMLString(prefix + INDENT));
        }
    }

    private String getXMLConnectionDefition() {
        if (connectionDefinition != null && connectionDefinition instanceof SQLDBConnectionDefinition) {
            return ((SQLDBConnectionDefinition) connectionDefinition).toXMLString();
        } else {
            return "";
        }
    }

    private SQLDBConnectionDefinition createETLDBConnectionDefinition() {
        DatabaseModel dbModel = (DatabaseModel) this.getSource();
        return createETLDBConnectionDefinition(dbModel);
    }

    private SQLDBConnectionDefinition createETLDBConnectionDefinition(DatabaseModel dbModel) {
        SQLDBConnectionDefinition etlConnDef = null;
        if (dbModel != null) {
            etlConnDef = new SQLDBConnectionDefinitionImpl(dbModel.getConnectionDefinition());
            try {
                etlConnDef.setName(this.getModelName());
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT107: Exception{0}", LOG_CATEGORY), ex);
            }
        } else {
            etlConnDef = new SQLDBConnectionDefinitionImpl();
        }

        return etlConnDef;
    }
}

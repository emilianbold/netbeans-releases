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

import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.DatabaseModel;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLFilter;
import org.netbeans.modules.sql.framework.model.SQLFrameworkParentObject;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObjectListener;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SQLWhen;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLOTDSynchronizationValidationVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLOperatorInfoVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Attribute;
import com.sun.sql.framework.utils.Logger;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Implements SQLDefinition.
 */

public class SQLDefinitionImpl implements SQLDefinition, Serializable {

    private static final String STAGING = "Staging";
    private static final String BEST_FIT = "Best Fit";
    private static final String PIPELINE = "Pipeline";
    
    private static final String WEB_ROWSET = "WebRowset";
    private static final String RELATIONALMAP = "RelationalMap";
    private static final String JSON = "Json";

    class SecondParseObjectInfo {
        private Element mElm;
        private SQLObject mObj;

        SecondParseObjectInfo(SQLObject obj, Element elm) {
            this.mObj = obj;
            this.mElm = elm;
        }

        public boolean equals(Object obj) {
            SecondParseObjectInfo other = (SecondParseObjectInfo) obj;

            return (other.mObj == this.mObj && other.mElm.equals(this.mElm));
        }

        public Element getElement() {
            return mElm;
        }

        public SQLObject getSQLObject() {
            return mObj;
        }

        public int hashCode() {
            int hashCode = (mObj != null) ? mObj.hashCode() : 0;
            hashCode += (mElm != null) ? mElm.hashCode() : 0;
            return hashCode;
        }
    }

    /** TAG_DEFINITION is the tag for an SQL definition */
    protected static final String TAG_DEFINITION = "sqlDefinition";

    /* Log4J category string */
    private static final String LOG_CATEGORY = SQLDefinitionImpl.class.getName();

    private static String VERSION = "5.1.0.1";

    /**
     * Map of attributes; used by concrete implementations to store class-specific fields
     * without hard coding them as member variables
     */
    protected Map attributes = new LinkedHashMap();

    /* Display name */
    private String displayName;

    private ArrayList listeners = new ArrayList();

    private transient SQLFrameworkParentObject mParent;

    private Map objectMap = new LinkedHashMap();

    /* Parent ETLObject. */
    private Object parent;

    private transient Set secondPassList = new HashSet();

    /**
     * Creates a new default instance of SQLDefinitionImpl.
     */
    public SQLDefinitionImpl() {
        this.init();
    }

    /**
     * Creates a new instance of SQLDefinitionImpl, parsing the given DOM Element to
     * retrieve its contents.
     * 
     * @param xmlElement DOM element containing content information
     * @exception BaseException if error occurs while parsing
     */
    public SQLDefinitionImpl(Element xmlElement) throws BaseException {
        this();
        parseXML(xmlElement);
    }

    /**
     * Creates a new instance of SQLDefinitionImpl, parsing the given DOM Element to
     * retrieve its contents.
     * 
     * @param xmlElement DOM element containing content information
     * @exception BaseException if error occurs while parsing
     */
    public SQLDefinitionImpl(Element xmlElement, SQLFrameworkParentObject parent) throws BaseException {
        this();

        mParent = parent;
        parseXML(xmlElement);
    }

    /**
     * Creates a new instance of SQLDefinitionImpl with the given display name.
     * 
     * @param aDisplayName for this
     */
    public SQLDefinitionImpl(String aDisplayName) {
        this();
        this.displayName = aDisplayName;
    }

    /**
     * Adds given SQLObject instance to this SQLDefinition.
     * 
     * @param newObject new instance to add
     * @throws BaseException if add fails or instance implements an unrecognized object
     *         type.
     */
    public void addObject(SQLObject newObject) throws BaseException {
        // check if object already exist
        if (objectMap.get(newObject.getId()) != null) {
//            throw new BaseException("Object " + newObject.getDisplayName() + "already exists.");
        }

        // always set the id first.
        if (newObject.getId() == null) {
            newObject.setId(generateId());
        }

        // set the object properties
        setSQLObjectProperties(newObject);

        // special handling for tables and columns
        // we need to generate unique ids for them
        switch (newObject.getObjectType()) {
            // Tables are not added directly to object map, but rather through
            // its parent database model.
            case SQLConstants.SOURCE_TABLE:
            case SQLConstants.TARGET_TABLE:
            case SQLConstants.RUNTIME_INPUT:
            case SQLConstants.RUNTIME_OUTPUT:
                addTable((SQLDBTable) newObject);
                break;

            case SQLConstants.SOURCE_DBMODEL:
            case SQLConstants.TARGET_DBMODEL:
            case SQLConstants.RUNTIME_DBMODEL:

                objectMap.put(newObject.getId(), newObject);
                SQLDBModel dbModel = (SQLDBModel) newObject;
                setTableId(dbModel.getTables());
                newObject.setParentObject(this);
                break;

            default:
                add(newObject);
                break;
        }
    }

    /**
     * Add SecondPass SQLObject to list
     * 
     * @param sqlObj to be added
     * @param element xmlElement of SQLObject
     */
    public void addSecondPassSQLObject(SQLObject sqlObj, Element element) {
        // this is a fix for problem with equal and hashcode method of SQL object
        // equal method usually use some object which are not yet resolved when
        // this method is called during parsing. so different object become
        // equal breaking the code because of hash map get and put method
        // may return a different object or overwrite existing object

        secondPassList.add(new SecondParseObjectInfo(sqlObj, element));
    }

    /**
     * add an SQL object listener
     * 
     * @param listener SQL object listener
     */
    public synchronized void addSQLObjectListener(SQLObjectListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void clearOverride(boolean clearCatalogOverride, boolean clearSchemaOverride) {
        List dbModels = this.getAllOTDs();
        Iterator itr = dbModels.iterator();
        SQLDBModel dbModel = null;

        while (itr.hasNext()) {
            dbModel = (SQLDBModel) itr.next();
            dbModel.clearOverride(clearCatalogOverride, clearSchemaOverride);
        }
    }

    /**
     * all sql objects are cloneable
     */
    public Object cloneSQLObject() throws CloneNotSupportedException {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Creates a new SQLObject instance of the given type. Does not add the the vended
     * SQLObject to this SQLDefinition, although it does set its parent reference to this.
     * To correctly associate the returned SQLObject instance with this instance, the
     * calling method should call addSQLObject(SQLObject).
     * 
     * @param objTag objTag of object to create
     * @return new SQLObject instance
     * @throws BaseException if error occurs during creation
     * @see #addObject(SQLObject)
     */
    public SQLObject createObject(String objTag) throws BaseException {
        return SQLObjectFactory.createObjectForTag(objTag);
    }

    /**
     * Creates a new SQLObject instance of the given type. Does not add the the vended
     * SQLObject to this SQLDefinition, although it does set its parent reference to this.
     * To correctly associate the returned SQLObject instance with this instance, the
     * calling method should call addSQLObject(SQLObject).
     * 
     * @param className className of object to create
     * @return new SQLObject instance
     * @throws BaseException if error occurs during creation
     * @see #addObject(SQLObject)
     */
    public SQLObject createSQLObject(String className) throws BaseException {
        return SQLObjectFactory.createSQLObject(className);
    }

    /**
     * Overrides default implementation to determine value based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @param o Object to be compared
     * @return true if o is equivalent to this; false otherwise
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        }

        boolean response = false;

        if (o instanceof SQLDefinitionImpl) {
            SQLDefinitionImpl target = (SQLDefinitionImpl) o;

            response = (objectMap != null) ? objectMap.equals(target.objectMap) : (target.objectMap == null);
            response &= (displayName != null) ? displayName.equals(target.displayName) : (target.displayName == null);
        }

        return response;
    }

    public String generateId() {
        int cnt = 0;

        String id = "sqlObject" + "_" + cnt;
        while (isIdExists(id)) {
            cnt++;
            id = "sqlObject" + "_" + cnt;
        }

        return id;
    }

    /**
     * Gets Collection of all SQLObjects in this model.
     * 
     * @return Collection, possibly empty, of all SQLObjects
     */
    public Collection getAllObjects() {
        return objectMap.values();
    }

    /**
     * Gets List of all OTDs associated with this model.
     * 
     * @return List of DatabaseModels representing participating OTDs
     */
    public List getAllOTDs() {
        ArrayList list = new ArrayList();

        list.addAll(getObjectsOfType(SQLConstants.SOURCE_DBMODEL));
        list.addAll(getObjectsOfType(SQLConstants.TARGET_DBMODEL));

        return list;
    }

    /**
     * Gets an attribute based on its name
     * 
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute getAttribute(String attrName) {
        return (Attribute) attributes.get(attrName);
    }

    /**
     * @see SQLObject#getAttributeNames
     */
    public Collection getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * @see SQLObject#getAttributeObject
     */
    public Object getAttributeValue(String attrName) {
        Attribute attr = getAttribute(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }

    /**
     * Gets display name.
     * 
     * @return current display name
     */
    public String getDisplayName() {
        return displayName;
    }

    public Integer getExecutionStrategyCode() {
        return (Integer) this.getAttributeValue(ATTR_EXECUTION_STRATEGY_CODE);
    }
    
    public Integer getExtractionTypeCode() {
        return (Integer)this.getAttributeValue(ATTR_EXTRACTION_TYPE_CODE);
    }

    public String getExecutionStrategyStr() {
        int code = getExecutionStrategyCode().intValue();
        switch (code) {
            case SQLDefinition.EXECUTION_STRATEGY_PIPELINE:
                return SQLDefinitionImpl.PIPELINE;

            case SQLDefinition.EXECUTION_STRATEGY_STAGING:
                return SQLDefinitionImpl.STAGING;

            case SQLDefinition.EXECUTION_STRATEGY_BEST_FIT:
            default:
                return SQLDefinitionImpl.BEST_FIT;
        }
    }

    public List getJoinSources() {
        ArrayList joinSources = new ArrayList();

        List sTables = this.getSourceTables();
        Iterator it = sTables.iterator();

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            if (sTable.isUsedInJoin()) {
                continue;
            }

            joinSources.add(sTable);
        }
        // add any join views also
        return joinSources;
    }

    /**
     * Gets associated SQLObject instance, if any, with the given object ID.
     * 
     * @param objectId ID of SQLObject instance to be retrieved
     * @param type type of object to retrieve
     * @return associated SQLObject instance, or null if no such instance exists
     */
    public SQLObject getObject(String objectId, int type) {
        SQLObject sqlObj;

        switch (type) {
            // for source table and source column we need to look in each db model
            // if that id exists
            case SQLConstants.SOURCE_TABLE:
            case SQLConstants.SOURCE_COLUMN:
                sqlObj = getObjectFromDBModel(objectId, SQLConstants.SOURCE_DBMODEL);
                // if sqlObj is null then we should check in runtime model
                // as it also contains SOURCE_COLUMN
                if (sqlObj == null) {
                    sqlObj = getObjectFromDBModel(objectId, SQLConstants.RUNTIME_DBMODEL);
                }
                break;

            case SQLConstants.TARGET_TABLE:
            case SQLConstants.TARGET_COLUMN:
                sqlObj = getObjectFromDBModel(objectId, SQLConstants.TARGET_DBMODEL);
                // if sqlObj is null then we should check in runtime model
                // as it also contains TARGET_COLUMN
                if (sqlObj == null) {
                    sqlObj = getObjectFromDBModel(objectId, SQLConstants.RUNTIME_DBMODEL);
                }
                break;

            case SQLConstants.RUNTIME_INPUT:
            case SQLConstants.RUNTIME_OUTPUT:
                sqlObj = getObjectFromDBModel(objectId, SQLConstants.RUNTIME_DBMODEL);
                break;

            case SQLConstants.LITERAL:
                sqlObj = (SQLObject) objectMap.get(objectId);
                break;
            default:
                sqlObj = (SQLObject) objectMap.get(objectId);
        }

        return sqlObj;
    }

    /**
     * Gets Collection of SQLObjects matching the given object type.
     * 
     * @param type type of objects to retrieve
     * @return Collection, possibly empty, of SQLObjects with matching type
     */
    public Collection getObjectsOfType(int type) {
        ArrayList list = new ArrayList();

        switch (type) {
            case SQLConstants.SOURCE_TABLE:
                return getSourceTables();
            case SQLConstants.SOURCE_COLUMN:
                return getSourceColumns();
            case SQLConstants.TARGET_TABLE:
                return getTargetTables();
            case SQLConstants.TARGET_COLUMN:
                return getTargetColumns();
        }

        Iterator it = objectMap.values().iterator();

        while (it.hasNext()) {
            SQLObject sqlObject = (SQLObject) it.next();
            if (sqlObject.getObjectType() == type) {
                list.add(sqlObject);
            }
        }

        return list;
    }

    /**
     * @see SQLDefinition#getParent
     */
    public Object getParent() {
        return parent;
    }

    /**
     * Gets the Root SQLJoinOperator Object in a given List
     * 
     * @param sourceTables List of Source Table SQLObjects
     * @return SQLObject Root Join from List
     * @throws BaseException while getting the Root
     */
    public SQLObject getRootJoin(List sourceTables) throws BaseException {
        if (sourceTables == null || sourceTables.size() == 0) {
            throw new BaseException("Source Table List is null or Empty");
        }

        List tables = new ArrayList();
        // Get the root Joins
        Iterator it1 = this.getRootJoins(SQLConstants.JOIN).iterator();
        while (it1.hasNext()) {
            SQLObject joinObject = (SQLObject) it1.next();
            discoverSourceTables(joinObject, tables);
            if (tables.containsAll(sourceTables)) {
                return joinObject;
            }
        }

        return null;
    }

    /**
     * Gets Collection of SQLJoinOperators representing "root" joins for this model.
     * 
     * @param type ???
     * @return Collection, possibly empty, of root SQLJoinOperators
     */
    public Collection getRootJoins(int type) {
        List list = new ArrayList();
        Iterator it = objectMap.values().iterator();
        while (it.hasNext()) {
            SQLObject sqlObject = (SQLObject) it.next();
            if (sqlObject.getObjectType() == type) {
                if (((SQLJoinOperator) sqlObject).isRoot()) {
                    list.add(sqlObject);
                }
            }
        }
        return list;
    }

    public RuntimeDatabaseModel getRuntimeDbModel() {
        Collection runtimeDbC = getObjectsOfType(SQLConstants.RUNTIME_DBMODEL);
        if (runtimeDbC.size() == 0) {
            RuntimeDatabaseModelImpl runtimeDbModel = new RuntimeDatabaseModelImpl();
            try {
                this.addObject(runtimeDbModel);
            } catch (BaseException ex) {
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "getRuntimeDbModel", "can not add runtime database model to definition", ex);
                runtimeDbModel = null;
            }

            return runtimeDbModel;
        }

        return (RuntimeDatabaseModel) runtimeDbC.iterator().next();
    }

    /**
     * Gets List of source columns associated with target tables of this model.
     * 
     * @return List of SourceColumn instances
     */
    public List getSourceColumns() {
        Collection sTables = getSourceTables();
        ArrayList sColumns = new ArrayList();

        Iterator it = sTables.iterator();
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            sColumns.addAll(table.getColumnList());
        }

        return sColumns;
    }

    /**
     * Gets List of source DatabaseModels associated with this model.
     * 
     * @return List of DatabaseModels containing source tables
     */
    public List getSourceDatabaseModels() {
        ArrayList list = new ArrayList();
        list.addAll(getObjectsOfType(SQLConstants.SOURCE_DBMODEL));
        return list;
    }

    /**
     * Gets List of source tables participating in this model.
     * 
     * @return List of instances
     */
    public List getSourceTables() {
        Collection sDBModel = getObjectsOfType(SQLConstants.SOURCE_DBMODEL);
        ArrayList sTables = new ArrayList();

        Iterator it = sDBModel.iterator();
        while (it.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            sTables.addAll(dbModel.getTables());
        }

        return sTables;
    }

    /**
     * Given a column find out the filters when the given column is used in left or right
     * of it.
     * 
     * @param sColumn sourceColumn
     * @return list of filters which have reference to these columns
     */
    public List getSQLFilterFor(SourceColumn sColumn) {
        ArrayList filterList = new ArrayList();
        Collection filters = getObjectsOfType(SQLConstants.FILTER);
        Iterator it = filters.iterator();
        while (it.hasNext()) {
            SQLFilter filter = (SQLFilter) it.next();
            SQLObject leftObj = filter.getSQLObject(SQLFilter.LEFT);
            SQLObject rightObj = filter.getSQLObject(SQLFilter.RIGHT);

            if (leftObj != null && leftObj.equals(sColumn) || rightObj != null && rightObj.equals(sColumn)) {
                filterList.add(filter);
            }
        }

        return filterList;
    }

    public SQLFrameworkParentObject getSQLFrameworkParentObject() {
        return mParent;
    }

    /**
     * get the tag name for this SQLDefinition override at subclass level to return a
     * different tag name
     * 
     * @return tag name to be used in xml representation of this object
     */
    public String getTagName() {
        return SQLDefinitionImpl.TAG_DEFINITION;
    }

    /**
     * Gets List of target columns associated with target tables of this model.
     * 
     * @return List of TargetColumn instances
     */
    public List getTargetColumns() {
        Collection tTables = getSourceTables();
        ArrayList tColumns = new ArrayList();

        Iterator it = tTables.iterator();
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            tColumns.addAll(table.getColumnList());
        }

        return tColumns;
    }

    /**
     * Gets List of target DatabaseModels associated with this model.
     * 
     * @return List of DatabaseModels containing target tables
     */
    public List getTargetDatabaseModels() {
        ArrayList list = new ArrayList();
        list.addAll(getObjectsOfType(SQLConstants.TARGET_DBMODEL));
        return list;
    }

    /**
     * Gets List of target tables participating in this model.
     * 
     * @return List of TargetTable instances
     */
    public List getTargetTables() {
        Collection tDBModel = getObjectsOfType(SQLConstants.TARGET_DBMODEL);
        ArrayList tTables = new ArrayList();

        Iterator it = tDBModel.iterator();
        while (it.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            tTables.addAll(dbModel.getTables());
        }

        return tTables;
    }

    public String getVersion() {
        return (String) this.getAttributeValue(ATTR_VERSION);
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hashCode = (displayName != null) ? displayName.hashCode() : 0;
        hashCode += objectMap.hashCode();

        return hashCode;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDefinition#hasValidationConditions()
     */
    public boolean hasValidationConditions() {
        Iterator it = this.getSourceTables().iterator();
        while (it.hasNext()) {
            SourceTable table = (SourceTable) it.next();
            SQLCondition vCondition = table.getDataValidationCondition();
            if (vCondition != null && vCondition.isConditionDefined() && vCondition.getRootPredicate() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a java operator is used in the model.
     * 
     * @return true if a java operator is used.
     */
    public boolean isContainsJavaOperators() {
        Boolean containsJavaOperators = (Boolean) this.getAttributeValue(ATTR_CONTAINS_JAVA_OPERATORS);
        if (containsJavaOperators != null) {
            return containsJavaOperators.booleanValue();
        }
        return false;
    }

    /**
     * Check if a table already exists in this definition
     * 
     * @param table - table
     * @return the existing object
     * @throws BaseException - exception
     */
    public Object isTableExists(DBTable table) throws BaseException {
        SQLDBModel dbModel = (SQLDBModel) getExistingDatabaseModelFor((SQLDBTable) table);
        if (dbModel != null) {
            // Check for duplication
            DBTable existing = dbModel.getTable(dbModel.getFullyQualifiedTableName(table));
            return existing;
        }
        return null;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLDefinition#migrateFromOlderVersions()
     */
    public void migrateFromOlderVersions() throws BaseException {
        String currentVersion = getVersion();

        // do join migration from version 5.0.1 or any old version (for old version,
        // version number was null) here
        if (currentVersion == null || getVersionInt(currentVersion) <= getVersionInt("5.0.1")) {
            Collection joins = this.getObjectsOfType(SQLConstants.JOIN);
            // ArrayList joinsToIgnore = new ArrayList();
            Iterator it = joins.iterator();
            while (it.hasNext()) {
                SQLJoinOperator join = (SQLJoinOperator) it.next();
                if (!join.isRoot()) {
                    continue;
                }
                SQLJoinView joinView = SQLModelObjectFactory.getInstance().createSQLJoinView();
                migrateJoin(join, joinView);
                this.addObject(joinView);
            }
        }

        // Set source and target table alias for old tables of version 5.0.3
        if (currentVersion == null || getVersionInt(currentVersion) <= getVersionInt("5.0.3")) {
            // add runtime argument for flatfile if it does not exist
            List sourceTables = this.getSourceTables();
            addRuntimeArgumentsForFlatFileTables(sourceTables);
            List targetTables = this.getTargetTables();
            addRuntimeArgumentsForFlatFileTables(targetTables);

            // set alias names
            setAliasNameForJoinViews();
            setAliasNameForSourceTables();
            setAliasNameForTargetTables();
        }

        // Convert literal data types as necessary to appropriate supported datatypes.
        if (currentVersion == null || getVersionInt(currentVersion) < getVersionInt("5.1.0")) {
            Collection literals = this.getObjectsOfType(SQLConstants.LITERAL);
            Iterator it = literals.iterator();
            while (it.hasNext()) {
                SQLLiteral lit = (SQLLiteral) it.next();
                migrateLiteral_510(lit);
            }
        }

        // Convert predicates associated with case-when operator to a part-of its
        // associated when for pre-5.1.0.1 versions.
        if (currentVersion == null || getVersionInt(currentVersion) < getVersionInt("5.1.0.1")) {
            Collection caseWhens = this.getObjectsOfType(SQLConstants.CASE);
            Iterator it = caseWhens.iterator();
            while (it.hasNext()) {
                SQLCaseOperator caseOp = (SQLCaseOperator) it.next();
                migrateCaseWhenCondition(caseOp);
            }

            Collection predicates = this.getObjectsOfType(SQLConstants.VISIBLE_PREDICATE);
            this.removeObjects(predicates);
        }

        // Upgrade version string to current value
        this.setAttribute(ATTR_VERSION, VERSION);
    }

    public void overrideCatalogNamesForOtd(Map overrideMapMap) {
        if (overrideMapMap != null) {
            List dbModels = this.getAllOTDs();
            Iterator itr = dbModels.iterator();
            SQLDBModel dbModel = null;
            Map catalogOverride = null;
            StringBuilder sb = null;
            while (itr.hasNext()) {
                dbModel = (SQLDBModel) itr.next();
//                sb = new StringBuilder(dbModel.getSource().getOID());
                sb = new StringBuilder();
                if (dbModel.getObjectType() == SQLConstants.SOURCE_DBMODEL) {
                    sb.append(SQLConstants.SOURCE_DB_MODEL_NAME_SUFFIX);
                } else {
                    sb.append(SQLConstants.TARGET_DB_MODEL_NAME_SUFFIX);
                }

                catalogOverride = (Map) overrideMapMap.get(sb.toString());
                if (catalogOverride != null) {
                    dbModel.overrideCatalogNames(catalogOverride);
                }
            }
        }
    }

    public void overrideSchemaNamesForOtd(Map overrideMapMap) {
        if (overrideMapMap != null) {
            List dbModels = this.getAllOTDs();
            Iterator itr = dbModels.iterator();
            SQLDBModel dbModel = null;
            Map catalogOverride = null;
            StringBuilder sb = null;
            while (itr.hasNext()) {
                dbModel = (SQLDBModel) itr.next();
                sb = new StringBuilder();
                if (dbModel.getObjectType() == SQLConstants.SOURCE_DBMODEL) {
                    sb.append(SQLConstants.SOURCE_DB_MODEL_NAME_SUFFIX);
                } else {
                    sb.append(SQLConstants.TARGET_DB_MODEL_NAME_SUFFIX);
                }

                catalogOverride = (Map) overrideMapMap.get(sb.toString());
                if (catalogOverride != null) {
                    dbModel.overrideSchemaNames(catalogOverride);
                }
            }
        }
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     * 
     * @param xmlElement DOM element containing XML marshalled version of a SQLDefinition
     *        instance
     * @exception BaseException thrown while parsing XML, or if xmlElement is null
     */
    public void parseXML(Element xmlElement) throws BaseException {
        NodeList list;

        if (xmlElement == null) {
            throw new BaseException("xmlElement is null");
        }

        // displayName - String
        this.displayName = xmlElement.getAttribute(SQLDefinition.ATTR_DISPLAYNAME);

        list = xmlElement.getChildNodes();
        attributes.putAll(parseAttributeList(list));

        list = xmlElement.getElementsByTagName(SQLDBModel.MODEL_TAG);
        parseDatabaseModels(list);

        list = xmlElement.getElementsByTagName(RuntimeDatabaseModel.RUNTIME_MODEL_TAG);
        parseRuntimeDatabaseModels(list);

        list = xmlElement.getChildNodes();
        parseXML(list);
        doSecondPassParse();
    }

    /**
     * Remove all objects from this container
     */
    public void removeAllObjects() {
        this.objectMap.clear();
    }

    /**
     * Removes given SQLObject instance from this SQLDefinition.
     * 
     * @param sqlObj instance to remove
     * @throws BaseException if error occurs during removal
     */
    public void removeObject(SQLObject sqlObj) throws BaseException {
        // check if it is a table object
        if (sqlObj == null) {
            throw new BaseException("Can not delete null object");
        }

        switch (sqlObj.getObjectType()) {
            case SQLConstants.SOURCE_TABLE:
            case SQLConstants.TARGET_TABLE:
            case SQLConstants.RUNTIME_INPUT:
            case SQLConstants.RUNTIME_OUTPUT:
                deleteTable((SQLDBTable) sqlObj);
                break;
            case SQLConstants.JOIN_VIEW:
                // remove tables used in join view
                SQLJoinView joinView = (SQLJoinView) sqlObj;
                Iterator sTables = joinView.getSourceTables().iterator();
                while (sTables.hasNext()) {
                    deleteTable((SQLDBTable) sTables.next());
                }
                // now remove join view
                objectMap.remove(sqlObj.getId());
                break;
            default:
                objectMap.remove(sqlObj.getId());
        }
    }

    /**
     * Removes given SQLObjects from SQLDefinition collection.
     * 
     * @param sqlObjects to be removed
     * @throws BaseException while removing
     */
    public void removeObjects(Collection sqlObjs) throws BaseException {
        if (sqlObjs == null) {
            throw new BaseException("Can not delete null object");
        }

        Iterator itr = sqlObjs.iterator();
        while (itr.hasNext()) {
            this.removeObject((SQLObject) itr.next());
        }
    }

    /**
     * remove SQL object listener
     * 
     * @param listener SQL object listener
     */
    public synchronized void removeSQLObjectListener(SQLObjectListener listener) {
        listeners.remove(listener);
    }

    /**
     * check if we have to use axion database if definition contains a java operator or
     * there is a validation condition on one of source tables.
     * 
     * @return
     */
    // TODO: Find java operator using visitor
    public boolean requiresPipelineProcess() {
        if (SQLDefinition.EXECUTION_STRATEGY_PIPELINE == getExecutionStrategyCode().intValue()) {
            return true;
        }

        if (isContainsJavaOperators()) {
            return true;
        }

        SQLOperatorInfoVisitor oInfoVisitor = new SQLOperatorInfoVisitor();
        oInfoVisitor.visit(this);
        if ((oInfoVisitor.isJavaOperatorFound()) || (oInfoVisitor.isValidationConditionFound())) {
            return true;
        }

        return false;
    }

    /**
     * @see SQLObject#setAttribute
     */
    public void setAttribute(String attrName, Object val) {
        Attribute attr = getAttribute(attrName);
        if (attr != null) {
            attr.setAttributeValue(val);
        } else {
            attr = new Attribute(attrName, val);
            attributes.put(attrName, attr);
        }
    }

    /**
     * set it to true if a java operator is used in the model
     * 
     * @param javaOp true if there is a java operator
     */
    public void setContainsJavaOperators(boolean javaOp) {
        this.setAttribute(ATTR_CONTAINS_JAVA_OPERATORS, Boolean.valueOf(javaOp));
    }

    /**
     * Sets display name to given value.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        displayName = newName;
    }

    public void setExecutionStrategyCode(Integer code) {
        this.setAttribute(ATTR_EXECUTION_STRATEGY_CODE, code);
    }
    
    public void setExtractionTypeCode(Integer code) {
       this.setAttribute(ATTR_EXTRACTION_TYPE_CODE, code);
    }

    /**
     * @see SQLDefinition#setParent
     */
    public void setParent(Object newParent) {
        if (newParent == null) {
            throw new IllegalArgumentException("Must supply non-null reference for Parent Object.");
        }
        this.parent = newParent;
    }

    public void setSQLFrameworkParentObject(SQLFrameworkParentObject aParent) {
        mParent = aParent;
    }

    public void setVersion(String newVersion) {
        if (getVersionInt(getVersion()) < getVersionInt(newVersion)) {
            this.setAttribute(ATTR_VERSION, newVersion);
        }
    }

    /**
     * Gets the XML representation of this SQLDefinition.
     * 
     * @return Returns the XML representation of this SQLDefinition.
     */
    public String toXMLString() throws BaseException {
        return toXMLString("");
    }

    /**
     * Gets the XML representation of this SQLDefinition, using the given String as a
     * prefix for individual XML elements.
     * 
     * @param prefix indent string to prefix each element in the xml document.
     * @return the XML representation of this SQLDefinition.
     */
    public String toXMLString(String prefix) throws BaseException {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder xml = new StringBuilder(500);
        xml.append(prefix).append("<").append(getTagName());

        xml.append(" " + ATTR_DISPLAYNAME + "=\"");
        if (displayName != null) {
            xml.append(displayName.trim());
        }
        xml.append("\" >\n");

        // write out attributes
        xml.append(toXMLAttributeTags(prefix));

        xml.append(toXMLString(prefix, objectMap.values()));
        xml.append(toXMLExtra(prefix + "\t"));
        xml.append(prefix).append("</").append(getTagName()).append(">\n");

        XmlUtil.dumpXMLString("Definition_" + getDisplayName() + ".xml", xml.toString());
        return xml.toString();
    }

    /**
     * validate the definition starting from the target tables.
     * 
     * @return Map of invalid input object as keys and reason as value
     */
    public List validate() {
        // Validate OTD Synchronization
//RIT This we may not need since there is no OTD        
//        List valInfo = validateOtdSynchronization();
        List valInfo = new ArrayList();
        // General eTL Collaboration validation
        SQLValidationVisitor vVisitor = new SQLValidationVisitor();
        vVisitor.visit(this);
        valInfo.addAll(vVisitor.getValidationInfoList());
        // Operator usage validation.
        SQLOperatorInfoVisitor opInfo = new SQLOperatorInfoVisitor(true);
        opInfo.visit(this);
        valInfo.addAll(opInfo.getValidationInfoList());
        valInfo = ConditionBuilderUtil.filterValidations(valInfo);
        return valInfo;
    }

    /**
     * Validate OTD synchronization. Identify any eTL Collaboration element which has been
     * deleted or modified in OTD.
     * 
     * @return Map of invalid object as keys and reason as value
     */
    public List validateOtdSynchronization() {
        SQLOTDSynchronizationValidationVisitor vVisitor = new SQLOTDSynchronizationValidationVisitor();
        vVisitor.visit(this);
        return vVisitor.getValidationInfoList();
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    boolean isIdExists(String id) {
        if (id == null) {
            return false;
        }

        SQLObject existingObj = getObjectFromDBModel(id, SQLConstants.SOURCE_DBMODEL);
        if (existingObj == null) {
            existingObj = getObjectFromDBModel(id, SQLConstants.TARGET_DBMODEL);
        }

        // check if object is in runtime model
        if (existingObj == null) {
            existingObj = getObjectFromDBModel(id, SQLConstants.RUNTIME_DBMODEL);
        }

        if (existingObj != null) {
            return true;
        }

        Collection ids = objectMap.keySet();

        if (ids.contains(id)) {
            return true;
        }

        return false;
    }

    protected void init() {
        this.setAttribute(ATTR_VERSION, VERSION);

        if (this.getAttributeValue(ATTR_EXECUTION_STRATEGY_CODE) == null) {
            setExecutionStrategyCode(new Integer(EXECUTION_STRATEGY_DEFAULT));
        }
    }

    protected void setSQLObjectProperties(SQLObject obj) {
        if (obj instanceof SourceTable) {
            SourceTable sTable = (SourceTable) obj;
            sTable.setTemporaryTableName(SQLObjectUtil.generateTemporaryTableName(sTable.getName()));
        } else if (obj instanceof SQLJoinView) {
            SQLJoinView joinView = (SQLJoinView) obj;
            joinView.setAliasName(generateJoinViewAliasName());
        }
    }

    /**
     * Generates XML elements representing this object's associated attributes.
     * 
     * @param prefix Prefix string to be prepended to each element
     * @return String containing XML representation of attributes
     */
    protected String toXMLAttributeTags(String prefix) {
        StringBuilder buf = new StringBuilder(100);

        Iterator iter = attributes.values().iterator();
        while (iter.hasNext()) {
            Attribute attr = (Attribute) iter.next();
            if (attr.getAttributeValue() != null) {
                buf.append(attr.toXMLString(prefix + "\t"));
            }
        }
        return buf.toString();
    }

    /**
     * Provides a way for child classes to write out their own XML elements.
     * 
     * @param prefix - prefix
     * @return a string
     */
    protected String toXMLExtra(String prefix) {
        return "";
    }

    private void add(SQLObject newObject) throws BaseException {
        // Make sure an object added has unique id: first check if id exists if yes
        // generate a unique id then add the object
        if (newObject.getId() == null) {
            newObject.setId(generateId());
        }

        objectMap.put(newObject.getId(), newObject);
        newObject.setParentObject(this);
    }

    private void addRuntimeArgumentsForFlatFileTables(List tables) throws BaseException {
        Iterator it = tables.iterator();
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            // if this is a table from flat file then we need to set runtime argument
            // for flat file if it is null for older version than 5.0.4
            String fArg = table.getFlatFileLocationRuntimeInputName();

            if (SQLObjectUtil.getFlatfileDBTable(table) != null && (fArg == null || fArg.trim().equals(""))) {
                // add a runtime argument for this
                SourceColumn runtimeArg = SQLObjectUtil.createRuntimeInput(table, this);
                if (runtimeArg != null) {
                    RuntimeInput runtimeInput = (RuntimeInput) runtimeArg.getParent();
                    if (runtimeInput != null) {
                        // if runtime input is not in SQL definition then add it
                        if ((isTableExists(runtimeInput)) == null) {
                            addObject(runtimeInput);
                        }
                    }
                }
            }
        }
    }

    private void addTable(SQLDBTable table) throws BaseException {
        // before adding a table we should create alias name and set it to table
        if(table.getAliasName() == null){
            if (table.getObjectType() == SQLConstants.SOURCE_TABLE) {
                table.setAliasName(this.generateSourceTableAliasName());
            } else if (table.getObjectType() == SQLConstants.TARGET_TABLE) {
                table.setAliasName(this.generateTargetTableAliasName());
            }
        }

        boolean createDBModel = false;

        SQLDBModel dbModel = (SQLDBModel) getExistingDatabaseModelFor(table);
        if (dbModel != null) {
            // Check for duplication; don't add if it's already there.
            DBTable existing = dbModel.getTable(dbModel.getFullyQualifiedTableName(table));
            if (existing != null) {
                if (table.getObjectType() == SQLConstants.TARGET_TABLE) {
                    throw new BaseException("Table '" + dbModel.getFullyQualifiedTableName(table) + "' already exists on the canvas.");
                }
                createDBModel = true;
            } else {
                dbModel.addTable(table);
                // Add table first, then call setTableId to ensure IDs are added
                // correctly for all elements.
                List list = new ArrayList();
                list.add(table);
                setTableId(list);

                return;
            }
        } else {
            createDBModel = true;
        }

        if (createDBModel) {
            DatabaseModel dbModelParent = null;

            switch (table.getObjectType()) {
                case SQLConstants.SOURCE_TABLE:
                    dbModelParent = table.getParent();
                    dbModel = SQLModelObjectFactory.getInstance().createDBModel(dbModelParent, SQLConstants.SOURCE_DBMODEL, mParent);
                    break;

                case SQLConstants.TARGET_TABLE:
                    dbModelParent = table.getParent();
                    dbModel = SQLModelObjectFactory.getInstance().createDBModel(table.getParent(), SQLConstants.TARGET_DBMODEL, mParent);
                    break;

                case SQLConstants.RUNTIME_INPUT:
                case SQLConstants.RUNTIME_OUTPUT:
                    dbModel = SQLModelObjectFactory.getInstance().createRuntimeDatabaseModel();
                    break;
                default:
                    throw new BaseException("Unknown table type (" + table.getObjectType() + ")");
            }

            // No existing database model found...add the table's parent, after
            // clearing all cloned tables then adding back the table in question.
            dbModel.setParentObject(this);
            dbModel.deleteAllTables();
            dbModel.addTable(table);
            addObject(dbModel);
        }
    }

    /**
     * Makes a deep copy of the given SQLConnectableObject and adds it to the given
     * SQLCondition.
     * 
     * @param object SQLConnectableObject to be deep-copied
     * @param condition SQLCondition in which to add <code>object</code>
     * @return copy of <code>object</code>
     */
    private SQLObject copyExpressionObject(SQLConnectableObject object, SQLCondition condition) throws BaseException {
        int objectType = object.getObjectType();
        switch (objectType) {
            case SQLConstants.VISIBLE_PREDICATE: {
                VisibleSQLPredicate visiblePredCopy = null;

                if (object instanceof VisibleMatchesPredicateImpl) {
                    visiblePredCopy = new VisibleMatchesPredicateImpl((VisibleMatchesPredicateImpl) object);
                } else {
                    visiblePredCopy = new VisibleSQLPredicateImpl();
                    ((VisibleSQLPredicateImpl) visiblePredCopy).copyFrom((VisibleSQLPredicate) object);
                }

                copyNonStaticInputs(visiblePredCopy, condition, visiblePredCopy.getOperatorXmlInfo());
                return visiblePredCopy;
            }

            case SQLConstants.PREDICATE: {
                SQLPredicateImpl predCopy = new VisibleSQLPredicateImpl();
                predCopy.copyFrom((VisibleSQLPredicate) object);

                copyNonStaticInputs(predCopy, condition, predCopy.getOperatorXmlInfo());
                return predCopy;
            }

            case SQLConstants.GENERIC_OPERATOR:
                try {
                    if (object instanceof SQLNormalizeOperatorImpl) {
                        SQLNormalizeOperatorImpl newOp = new SQLNormalizeOperatorImpl((SQLNormalizeOperatorImpl) object);

                        copyNonStaticInputs(newOp, condition, newOp.getOperatorXmlInfo());
                        return newOp;
                    } else if (object instanceof SQLStandardizeOperatorImpl) {
                        SQLStandardizeOperatorImpl newOp = new SQLStandardizeOperatorImpl((SQLStandardizeOperatorImpl) object);

                        copyNonStaticInputs(newOp, condition, newOp.getOperatorXmlInfo());
                        return newOp;
                    }
                } catch (CloneNotSupportedException e) {
                    throw new BaseException(e);
                }

                SQLGenericOperatorImpl newOp = new SQLGenericOperatorImpl((SQLGenericOperator) object);
                copyNonStaticInputs(newOp, condition, newOp.getOperatorXmlInfo());
                return newOp;

            case SQLConstants.CUSTOM_OPERATOR:
            	SQLCustomOperatorImpl newCustopr = new SQLCustomOperatorImpl((SQLCustomOperatorImpl) object);
                copyNonStaticInputs(newCustopr, condition, newCustopr.getOperatorXmlInfo());
                return newCustopr;
                
            default:
                String typeStr = TagParserUtility.getDisplayStringFor(objectType);
                throw new BaseException("Copying of this expression object type (" + typeStr + ") is not currently supported.");

        }
    }

    /**
     * Copies all non-static inputs of the given SQLConnectableObject into the given
     * SQLCondition, depending on the argument information contained in the given
     * IOperatorXmlInfo instance.
     * 
     * @param expObj SQLConnectableObject whose inputs are to be evaluated and copied as
     *        necessary
     * @param condition SQLCondition in which non-static inputs of <code>expObj</code>
     *        will be added
     * @param operatorInfo IOperatorXmlInfo containing info on the static or non-static
     *        nature of input arguments in <code>expObj</code>
     * @throws BaseException if error occurs while resolving or copying input objects
     */
    private void copyNonStaticInputs(SQLConnectableObject expObj, SQLCondition condition, IOperatorXmlInfo operatorInfo) throws BaseException {
        Map inputObjects = expObj.getInputObjectMap();
        Iterator iter = inputObjects.values().iterator();
        while (iter.hasNext()) {
            SQLInputObject inputObj = (SQLInputObject) iter.next();
            String argName = inputObj.getArgName();
            IOperatorField fieldInfo = operatorInfo.getInputField(argName);
            if (fieldInfo != null && !fieldInfo.isStatic()) {
                Object input = resolveInputObject(inputObj, condition);
                if (expObj instanceof SQLPredicate && input instanceof SQLPredicate) {
                    ((SQLPredicate) input).setRoot((SQLPredicate) expObj);
                }
            }
        }
    }

    private void deleteTable(SQLDBTable table) throws BaseException {
        SQLDBModel dbModel = (SQLDBModel) table.getParent();
        dbModel.deleteTable(dbModel.getFullyQualifiedTableName(table));

        if (dbModel.getTables().size() == 0) {
            removeObject(dbModel);
        }
    }

    private void discoverSourceTables(SQLObject sqlObj, List tables) {
        SQLJoinOperator joinObject = (SQLJoinOperator) sqlObj;
        SQLObject left = joinObject.getSQLObject(SQLJoinOperator.LEFT);
        SQLObject right = joinObject.getSQLObject(SQLJoinOperator.RIGHT);
        if (left.getObjectType() == SQLConstants.SOURCE_TABLE) {
            tables.add(left);
        } else {
            discoverSourceTables(left, tables);
        }
        if (right.getObjectType() == SQLConstants.SOURCE_TABLE) {
            tables.add(right);
        } else {
            discoverSourceTables(right, tables);
        }
    }

    private void doSecondPassParse() throws BaseException {
        Iterator it = secondPassList.iterator();
        while (it.hasNext()) {
            SecondParseObjectInfo objInfo = (SecondParseObjectInfo) it.next();
            objInfo.getSQLObject().secondPassParse(objInfo.getElement());
        }

        secondPassList.clear();
    }

    private String generateJoinViewAliasName() {
        int cnt = 1;
        String aliasPrefix = "J";
        String aName = aliasPrefix + cnt;
        while (isJoinViewAliasNameExist(aName)) {
            cnt++;
            aName = aliasPrefix + cnt;
        }

        return aName;
    }

    private String generateSourceTableAliasName() {
        int cnt = 1;
        String aliasPrefix = "S";
        String aName = aliasPrefix + cnt;
        while (isSourceTableAliasNameExist(aName)) {
            cnt++;
            aName = aliasPrefix + cnt;
        }

        return aName;
    }

    private String generateTargetTableAliasName() {
        int cnt = 1;
        String aliasPrefix = "T";
        String aName = aliasPrefix + cnt;
        while (isTargetTableAliasNameExist(aName)) {
            cnt++;
            aName = aliasPrefix + cnt;
        }

        return aName;
    }

    private DatabaseModel getExistingDatabaseModelFor(SQLDBTable table) throws BaseException {

        // if table is RuntimeInput or RuntimeOutput then we want to return runtime db
        // model
        if (table.getObjectType() == SQLConstants.RUNTIME_INPUT || table.getObjectType() == SQLConstants.RUNTIME_OUTPUT) {
            return this.getRuntimeDbModel();
        }

        // for other tables we need to check if they have a parent db model
        DatabaseModel dbModel = table.getParent();
        if (dbModel == null) {
            return null;
        }

        List dbModels;
        switch (table.getObjectType()) {
            case SQLConstants.SOURCE_TABLE:
                dbModels = getSourceDatabaseModels();
                break;

            case SQLConstants.TARGET_TABLE:
                dbModels = getTargetDatabaseModels();
                break;

            default:
                throw new BaseException("Unknown table type (" + table.getObjectType() + ")");
        }

        Iterator it = dbModels.iterator();
        while (it.hasNext()) {
            DatabaseModel existingModel = (DatabaseModel) it.next();
            if (existingModel.getModelName().equals(dbModel.getModelName())) {
                return (SQLDBModel) existingModel;
            }
        }

        return null;
    }

    private SQLObject getObjectFromDBModel(String objectId, int modelType) {
        Collection dbModelC = getObjectsOfType(modelType);
        Iterator it = dbModelC.iterator();

        while (it.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            SQLObject sqlObj = dbModel.getObject(objectId);
            if (sqlObj != null) {
                return sqlObj;
            }
        }

        return null;
    }

    private int getVersionInt(String version) {
        // starting with 5.0.0.0
        int majorVersion = 10000000;
        int minorVersion = 100000; // maximum value of minor version number: 99
        int pointRelease = 1000; // maximum value of point release number: 99
        int internalVersion = 1; // maximum value of internal build number: 999

        List versions = StringUtil.createStringListFrom(version, '.');
        switch (versions.size()) {
            case 4:
                internalVersion *= (StringUtil.getInt((String) versions.get(3)) + 1);
            case 3:
                pointRelease *= (StringUtil.getInt((String) versions.get(2)) + 1);
            case 2:
                minorVersion *= (StringUtil.getInt((String) versions.get(1)) + 1);
            case 1:
                majorVersion *= (StringUtil.getInt((String) versions.get(0)) + 1);
        }

        return majorVersion + minorVersion + pointRelease + internalVersion;
    }

    private boolean isJoinViewAliasNameExist(String aName) {
        Collection joinViews = this.getObjectsOfType(SQLConstants.JOIN_VIEW);
        Iterator it = joinViews.iterator();

        while (it.hasNext()) {
            SQLJoinView joinView = (SQLJoinView) it.next();
            String sAlias = joinView.getAliasName();
            if (sAlias != null && sAlias.equals(aName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSourceTableAliasNameExist(String aName) {
        List sTables = this.getSourceTables();
        Iterator it = sTables.iterator();

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            String sAlias = sTable.getAliasName();
            if (sAlias != null && sAlias.equals(aName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTargetTableAliasNameExist(String aName) {
        List sTables = this.getTargetTables();
        Iterator it = sTables.iterator();

        while (it.hasNext()) {
            TargetTable tTable = (TargetTable) it.next();
            String tAlias = tTable.getAliasName();
            if (tAlias != null && tAlias.equals(aName)) {
                return true;
            }
        }

        return false;
    }

    private void migrateCaseWhenCondition(SQLCaseOperator caseOper) throws BaseException {
        List whens = caseOper.getWhenList();
        Iterator it = whens.iterator();
        while (it.hasNext()) {
            SQLWhen when = (SQLWhen) it.next();

            SQLInputObject whenCond = when.getInput(SQLWhen.CONDITION);
            SQLPredicate predicate = (SQLPredicate) whenCond.getSQLObject();
            if (predicate == null) {
                continue;
            }
            SQLCondition condition = when.getCondition();

            // Add copy of predicate to SQLCondition using copy method - this ensures that
            // special cases (e.g., SQLLiterals representing the contents of static
            // fields) are handled correctly.
            SQLObject newPredicate = copyExpressionObject(predicate, condition);
            condition.addObject(newPredicate);

            // Force construction of condition text.
            condition.setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
            condition.getConditionText(true);

            // dissociate condition input from parent
            whenCond.setSQLObject(null);
            this.removeObject(predicate);

            when.getInputObjectMap().remove(SQLWhen.CONDITION);
        }
    }

    private List migrateJoin(SQLJoinOperator join, SQLJoinView joinView) throws BaseException {
        ArrayList joinsSoFar = new ArrayList();
        joinsSoFar.add(join);

        this.removeObject(join);

        // reset join id and parent object
        join.reset();
        SQLInputObject leftIn = join.getInput(SQLJoinOperator.LEFT);
        SQLInputObject rightIn = join.getInput(SQLJoinOperator.RIGHT);

        SQLObject leftObj = leftIn.getSQLObject();
        SQLObject rightObj = rightIn.getSQLObject();

        if (leftObj.getObjectType() == SQLConstants.JOIN) {
            joinsSoFar.addAll(migrateJoin((SQLJoinOperator) leftObj, joinView));
        } else {
            SourceTable sTable = (SourceTable) leftObj;
            sTable.setUsedInJoin(true);

            SQLJoinTable leftTable = SQLModelObjectFactory.getInstance().createSQLJoinTable(sTable);
            leftIn.setSQLObject(leftTable);
            joinView.addObject(leftTable);
        }

        if (rightObj.getObjectType() == SQLConstants.JOIN) {
            joinsSoFar.addAll(migrateJoin((SQLJoinOperator) rightObj, joinView));
        } else {
            SourceTable sTable = (SourceTable) rightObj;
            sTable.setUsedInJoin(true);

            SQLJoinTable rightTable = SQLModelObjectFactory.getInstance().createSQLJoinTable(sTable);
            rightIn.setSQLObject(rightTable);
            joinView.addObject(rightTable);
        }

        // migrate join condition
        migrateJoinCondition(join);

        // add join
        joinView.addObject(join);

        return joinsSoFar;
    }

    private void migrateJoinCondition(SQLJoinOperator join) throws BaseException {
        SQLInputObject joinInputCond = join.getInput(SQLJoinOperator.CONDITION);
        SQLPredicate predicate = (SQLPredicate) joinInputCond.getSQLObject();
        this.removeObject(predicate); // first remove the predicate

        // set join condition type to user modified
        join.setJoinConditionType(SQLJoinOperator.USER_DEFINED_CONDITION);
        SQLCondition condition = join.getJoinCondition();
        SQLObjectUtil.migrateJoinCondition(predicate, condition);
        condition.addObject(predicate); // add predicate to SQLCondition

        // dissociate condition input from parent
        joinInputCond.setSQLObject(null);
    }

    private void migrateLiteral_510(SQLLiteral lit) {
        int oldJdbcType = lit.getJdbcType();

        switch (oldJdbcType) {
            case Types.FLOAT:
            case Types.DOUBLE:
                lit.setJdbcType(Types.NUMERIC);
                break;

            default:
                break;
        }
    }

    private Map parseAttributeList(NodeList list) throws BaseException {
        Map attrMap = new HashMap();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) list.item(i);
                if (elem.getNodeName().equals(Attribute.TAG_ATTR)) {
                    Attribute attr = new Attribute();
                    attr.parseXMLString(elem);
                    attrMap.put(attr.getAttributeName(), attr);
                }
            }
        }
        return attrMap;
    }

    private void parseDatabaseModels(NodeList list) throws BaseException {
        if (list == null) {
            throw new BaseException("Must supply non-null NodeList.");
        }

        for (int i = 0; i < list.getLength(); i++) {
            Element elem = (Element) list.item(i);
            SQLDBModel dbModel = (SQLDBModel) createSQLObject(SQLDBModelImpl.class.getName());

            dbModel.setParentObject(this);
            //dbModel.setSQLFrameworkParentObject(mParent);

            dbModel.parseXML(elem);
            dbModel.setSQLFrameworkParentObject(mParent);
            this.addObject(dbModel);
        }
    }

    private void parseRuntimeDatabaseModels(NodeList list) throws BaseException {
        if (list == null) {
            throw new BaseException("Must supply non-null NodeList.");
        }

        for (int i = 0; i < list.getLength(); i++) {
            Element elem = (Element) list.item(i);
            RuntimeDatabaseModel dbModel = (RuntimeDatabaseModel) createSQLObject(RuntimeDatabaseModelImpl.class.getName());

            dbModel.setParentObject(this);
            dbModel.parseXML(elem);
            this.addObject(dbModel);
        }
    }

    private void parseXML(NodeList list) throws BaseException {
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (!node.getNodeName().equals(SQLObject.TAG_SQLOBJECT)) {
                continue;
            }

            Element opeElem = (Element) node;
            SQLObject sqlObj = SQLObjectFactory.createSQLObjectForElement(this, opeElem);
            if (sqlObj != null) {
                this.addObject(sqlObj);
            } else {
                throw new BaseException("Failed to parse " + opeElem);
            }
        }
    }

    /**
     * Resolves the given input object, cloning it and adding it to the given SQLCondition
     * if necessary.
     * 
     * @param inputObject SQLInputObject to be resolved
     * @param condition SQLCondition in which to add the input referenced by

     *        <code>inputObject</code>
     * @return input object that was resolved and added to condition; possibly null
     */
    private Object resolveInputObject(SQLInputObject inputObject, SQLCondition condition) throws BaseException {
        SQLObject sqlObject = inputObject.getSQLObject();
        if (sqlObject != null) {
            if (sqlObject.getObjectType() == SQLConstants.SOURCE_COLUMN) {
                ColumnRef columnRef = SQLModelObjectFactory.getInstance().createColumnRef((SQLDBColumn) sqlObject);
                condition.addObject(columnRef);
                inputObject.setSQLObject(columnRef);
            } else {
                if (sqlObject instanceof SQLConnectableObject) {
                    sqlObject = copyExpressionObject((SQLConnectableObject) sqlObject, condition);
                    inputObject.setSQLObject(sqlObject);
                } else if (sqlObject instanceof VisibleSQLLiteral) {
                    // Must handle visible flavor before standard one
                    // Copy VisibleSQLLiteral object into the condition.
                    VisibleSQLLiteral oldLiteral = (VisibleSQLLiteral) sqlObject;

                    VisibleSQLLiteral newLiteral = new VisibleSQLLiteralImpl();
                    newLiteral.setDisplayName(oldLiteral.getDisplayName());
                    newLiteral.setJdbcType(oldLiteral.getJdbcType());
                    newLiteral.setValue(oldLiteral.getValue());
                    inputObject.setSQLObject(newLiteral);

                    sqlObject = newLiteral;
                } else if (sqlObject instanceof SQLLiteral) {
                    // Copy SQLLiteral object into the condition.
                    SQLLiteral oldLiteral = (SQLLiteral) sqlObject;

                    SQLLiteral newLiteral = new SQLLiteralImpl();
                    newLiteral.setDisplayName(oldLiteral.getDisplayName());
                    newLiteral.setJdbcType(oldLiteral.getJdbcType());
                    newLiteral.setValue(oldLiteral.getValue());
                    inputObject.setSQLObject(newLiteral);

                    sqlObject = newLiteral;
                }

                condition.addObject(sqlObject);
            }
        }

        return sqlObject;
    }

    private void setAliasNameForJoinViews() {
        Collection joinViews = this.getObjectsOfType(SQLConstants.JOIN_VIEW);
        Iterator it = joinViews.iterator();

        while (it.hasNext()) {
            SQLJoinView joinView = (SQLJoinView) it.next();
            String sAlias = joinView.getAliasName();

            if (sAlias == null || sAlias.trim().equals("")) {
                joinView.setAliasName(this.generateJoinViewAliasName());
            }
        }
    }

    private void setAliasNameForSourceTables() {
        List sTables = this.getSourceTables();
        Iterator it = sTables.iterator();

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            String sAlias = sTable.getAliasName();

            if (sAlias == null || sAlias.trim().equals("")) {
                sTable.setAliasName(this.generateSourceTableAliasName());
            }
        }
    }

    private void setAliasNameForTargetTables() {
        List tTables = this.getTargetTables();
        Iterator it = tTables.iterator();

        while (it.hasNext()) {
            TargetTable tTable = (TargetTable) it.next();
            String tAlias = tTable.getAliasName();

            if (tAlias == null || tAlias.trim().equals("")) {
                tTable.setAliasName(this.generateTargetTableAliasName());
            }
        }
    }

    private void setSQLObjectId(List sqlObjects) throws BaseException {
        Iterator it = sqlObjects.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            sqlObj.setId(generateId());
        }
    }

    private void setTableId(List tables) throws BaseException {
        Iterator it = tables.iterator();
        while (it.hasNext()) {
            SQLObject table = (SQLObject) it.next();
            if (table.getId() == null) {
                table.setId(generateId());
            }
            setSQLObjectId(((DBTable) table).getColumnList());
        }
    }

    private String toXMLString(String prefix, Collection sqlObjects) throws BaseException {
        Iterator it = sqlObjects.iterator();
        StringBuilder xml = new StringBuilder(" ");

        int i = 0;
        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            if (obj != null) {
                // Add newline between each element, starting after first one.
                if (i++ != 0) {
                    xml.append("\n");
                }
                xml.append(obj.toXMLString(prefix + "\t"));
            }
        }

        return xml.toString();
    }

    public void setExecutionStrategyStr(String text) {
    }
    
     public void setResponseTypeStr(String text) {
     }
     
     public String getResponseTypeStr() {
        int code = getExecutionStrategyCode().intValue();
        switch (code) {
            case SQLDefinition.WEB_ROWSET:
                return SQLDefinitionImpl.WEB_ROWSET;
            case SQLDefinition.RELATIONAL_MAP:
                return SQLDefinitionImpl.RELATIONALMAP;
            case SQLDefinition.JSON:
                return SQLDefinitionImpl.JSON;
                
            default:
                return SQLDefinitionImpl.WEB_ROWSET;
        }
    }

}

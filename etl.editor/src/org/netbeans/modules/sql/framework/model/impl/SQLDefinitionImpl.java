/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
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
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObjectListener;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLDBSynchronizationValidationVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLOperatorInfoVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.Attribute;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.StringUtil;
import java.io.File;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.visitors.SQLDBDriverValidationVisitor;

/**
 * Implements SQLDefinition.
 *
 * @author Ahimanikya Satapathy
 */
public class SQLDefinitionImpl implements SQLDefinition, Serializable {

    private static final String J = "J";
    private static final String S = "S";
    private static final String T = "T";
    private static final String STAGING = "Staging";
    private static final String BEST_FIT = "Best Fit";
    private static final String PIPELINE = "Pipeline";
    private static transient final Logger mLogger = Logger.getLogger(SQLDefinitionImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    class SecondParseObjectInfo {

        private Element mElm;
        private SQLObject mObj;

        SecondParseObjectInfo(SQLObject obj, Element elm) {
            this.mObj = obj;
            this.mElm = elm;
        }

        @Override
        public boolean equals(Object obj) {
            SecondParseObjectInfo other = (SecondParseObjectInfo) obj;
            return other.mObj == this.mObj && other.mElm.equals(this.mElm);
        }

        public Element getElement() {
            return mElm;
        }

        public SQLObject getSQLObject() {
            return mObj;
        }

        @Override
        public int hashCode() {
            int hashCode = (mObj != null) ? mObj.hashCode() : 0;
            hashCode += (mElm != null) ? mElm.hashCode() : 0;
            return hashCode;
        }
    }
    /** TAG_DEFINITION is the tag for an SQL definition */
    protected static final String TAG_DEFINITION = "sqlDefinition";
    /* Log category string */
    private static final String LOG_CATEGORY = SQLDefinitionImpl.class.getName();
    private static String VERSION = "6.0";
    /**
     * Map of attributes; used by concrete implementations to store class-specific fields
     * without hard coding them as member variables
     */
    protected Map<String, Attribute> attributes = new LinkedHashMap<String, Attribute>();
    private String displayName;
    private List<SQLObjectListener> listeners = new ArrayList<SQLObjectListener>();
    private transient SQLFrameworkParentObject mParent;
    private Map<String, SQLObject> objectMap = new LinkedHashMap<String, SQLObject>();
    private Object parent; // Parent ETLObject.
    private transient Set<SecondParseObjectInfo> secondPassList = new HashSet<SecondParseObjectInfo>();

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
        // check if object already exist -- Do we still need this check ?? -- Ahi
        if (objectMap.get(newObject.getId()) != null) {
            //throw new BaseException("Object " + newObject.getDisplayName() + "already exists.");
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
        List<SQLDBModel> dbModels = this.getAllDatabases();
        SQLDBModel dbModel = null;
        for (Iterator<SQLDBModel> itr = dbModels.iterator(); itr.hasNext();) {
            dbModel = itr.next();
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
     * Creates a new SQLObject instance of the given type. Does not add the vended
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
     * Creates a new SQLObject instance of the given type. Does not add the vended
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
    @Override
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
    public Collection<SQLObject> getAllObjects() {
        return objectMap.values();
    }

    /**
     * Gets List of all Databases associated with this model.
     *
     * @return List of DatabaseModels representing participating Databases
     */
    public List<SQLDBModel> getAllDatabases() {
        List<SQLDBModel> list = new ArrayList<SQLDBModel>();
        Iterator it = getObjectsOfType(SQLConstants.SOURCE_DBMODEL).iterator();
        while (it.hasNext()) {
            list.add((SQLDBModel) it.next());
        }

        it = getObjectsOfType(SQLConstants.TARGET_DBMODEL).iterator();
        while (it.hasNext()) {
            list.add((SQLDBModel) it.next());
        }
        return list;
    }

    /**
     * Gets an attribute based on its name
     *
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute getAttribute(String attrName) {
        return attributes.get(attrName);
    }

    /**
     * @see SQLObject#getAttributeNames
     */
    public Collection<String> getAttributeNames() {
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

    public String getAxiondbWorkingDirectory() {
        String fs = File.separator;
        String workingFolder = (String) this.getAttributeValue(AXION_DB_WORKING_DIR);
        workingFolder = (workingFolder == null || "".equalsIgnoreCase(workingFolder)) ? System.getProperty("netbeans.user") : workingFolder;
        return workingFolder;
    }

    public String getAxiondbDataDirectory() {
        String dbName = (String) this.getAttributeValue(AXION_DB_DATA_DIR);
        dbName = (dbName == null) ? ETLEditorSupport.PRJ_PATH + File.separator + "data" + File.separator :  dbName;
        return dbName;
    }
    
    public boolean isDynamicFlatFile() {
        Boolean dynamicFlatFile = (Boolean) this.getAttributeValue(DYNAMIC_FLAT_FILE);
        boolean flag = false;
        if(dynamicFlatFile != null) {
            flag = dynamicFlatFile.booleanValue();
        }
        return flag;
    }
    
    public void setDynamicFlatFile(boolean flag) {
        this.setAttribute(DYNAMIC_FLAT_FILE, Boolean.valueOf(flag));
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

    public List<DBTable> getJoinSources() {
        List<DBTable> joinSources = new ArrayList<DBTable>();
        List<DBTable> sTables = this.getSourceTables();
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
                sqlObj = objectMap.get(objectId);
                break;
            default:
                sqlObj = objectMap.get(objectId);
        }

        return sqlObj;
    }

    /**
     * Gets Collection of SQLObjects matching the given object type.
     *
     * @param type type of objects to retrieve
     * @return Collection, possibly empty, of SQLObjects with matching type
     */
    @SuppressWarnings(value = "unchecked")
    public Collection getObjectsOfType(int type) {
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

        List<SQLObject> list = new ArrayList<SQLObject>();
        Iterator<SQLObject> it = objectMap.values().iterator();

        while (it.hasNext()) {
            SQLObject sqlObject = it.next();
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
    public SQLObject getRootJoin(List<DBTable> sourceTables) throws BaseException {
        if (sourceTables == null || sourceTables.size() == 0) {
            throw new BaseException("Source Table List is null or Empty");
        }

        List<DBTable> tables = new ArrayList<DBTable>();
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
    public Collection<SQLObject> getRootJoins(int type) {
        List<SQLObject> list = new ArrayList<SQLObject>();
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
        Collection runtimeModels = getObjectsOfType(SQLConstants.RUNTIME_DBMODEL);
        if (runtimeModels.size() == 0) {
            RuntimeDatabaseModelImpl runtimeDbModel = new RuntimeDatabaseModelImpl();
            try {
                this.addObject(runtimeDbModel);
            } catch (BaseException ex) {
                mLogger.errorNoloc(mLoc.t("EDIT116: can not add runtime database model to definition{0}", LOG_CATEGORY), ex);
                runtimeDbModel = null;
            }
            return runtimeDbModel;
        }
        return (RuntimeDatabaseModel) runtimeModels.iterator().next();
    }

    /**
     * Gets List of source columns associated with source tables of this model.
     *
     * @return List of SourceColumn instances
     */
    public List<DBColumn> getSourceColumns() {
        List<DBTable> sTables = getSourceTables();
        List<DBColumn> sColumns = new ArrayList<DBColumn>();
        for (Iterator<DBTable> it = sTables.iterator(); it.hasNext();) {
            sColumns.addAll(it.next().getColumnList());
        }
        return sColumns;
    }

    /**
     * Gets List of source DatabaseModels associated with this model.
     *
     * @return List of DatabaseModels containing source tables
     */
    public List<SQLDBModel> getSourceDatabaseModels() {
        List<SQLDBModel> sourceModels = new ArrayList<SQLDBModel>();
        Iterator it = getObjectsOfType(SQLConstants.SOURCE_DBMODEL).iterator();
        while (it.hasNext()) {
            sourceModels.add((SQLDBModel) it.next());
        }
        return sourceModels;
    }

    /**
     * Gets List of source tables participating in this model.
     *
     * @return List of instances
     */
    public List<DBTable> getSourceTables() {
        Collection sDBModel = getObjectsOfType(SQLConstants.SOURCE_DBMODEL);
        List<DBTable> sTables = new ArrayList<DBTable>();
        for (Iterator it = sDBModel.iterator(); it.hasNext();) {
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
    public List<SQLFilter> getSQLFilterFor(SourceColumn sColumn) {
        List<SQLFilter> filterList = new ArrayList<SQLFilter>();
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
    public List<DBColumn> getTargetColumns() {
        Collection<DBTable> tTables = getTargetTables();
        List<DBColumn> tColumns = new ArrayList<DBColumn>();
        for (Iterator<DBTable> it = tTables.iterator(); it.hasNext();) {
            DBTable table = it.next();
            tColumns.addAll(table.getColumnList());
        }
        return tColumns;
    }

    /**
     * Gets List of target DatabaseModels associated with this model.
     *
     * @return List of DatabaseModels containing target tables
     */
    public List<SQLDBModel> getTargetDatabaseModels() {
        List<SQLDBModel> targetModels = new ArrayList<SQLDBModel>();
        Iterator it = getObjectsOfType(SQLConstants.TARGET_DBMODEL).iterator();
        while (it.hasNext()) {
            targetModels.add((SQLDBModel) it.next());
        }
        return targetModels;
    }

    /**
     * Gets List of target tables participating in this model.
     *
     * @return List of TargetTable instances
     */
    public List<DBTable> getTargetTables() {
        Collection tDBModel = getObjectsOfType(SQLConstants.TARGET_DBMODEL);
        List<DBTable> tTables = new ArrayList<DBTable>();
        for (Iterator it = tDBModel.iterator(); it.hasNext();) {
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
    @Override
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
        SQLDBModel dbModel = getExistingDatabaseModelFor((SQLDBTable) table);
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
        // Upgrade version string to current value
        this.setAttribute(ATTR_VERSION, VERSION);
    }

    public void overrideCatalogNamesForDb(Map overrideMapMap) {
        if (overrideMapMap != null) {
            List dbModels = this.getAllDatabases();
            Iterator itr = dbModels.iterator();
            SQLDBModel dbModel = null;
            Map catalogOverride = null;
            StringBuilder sb = null;
            while (itr.hasNext()) {
                dbModel = (SQLDBModel) itr.next();
                // sb = new StringBuilder(dbModel.getSource().getOID());
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

    public void overrideSchemaNamesForDb(Map overrideMapMap) {
        if (overrideMapMap != null) {
            List dbModels = this.getAllDatabases();
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

    /**
     * sets the axion database working folder
     * @param appDataRoot
     */
    public void setAxiondbWorkingDirectory(String appDataRoot) {
        this.setAttribute(AXION_DB_WORKING_DIR, appDataRoot);
    }

    /**
     * sets the axion database instance name
     * @param dbInstanceName
     */
    public void setAxiondbDataDirectory(String dbInstanceName) {
        this.setAttribute(AXION_DB_DATA_DIR, dbInstanceName);
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
    public List<ValidationInfo> validate() {
        // TODO: Need to validate the drivers, file location used for data file
        List<ValidationInfo> valInfo = validateDbDrivers();
        if (valInfo.size() == 0) { // Found driver errors. don't proceed with other validation.
            valInfo.addAll(validateDbSynchronization()); // Validate Database Synchronization
        }

        // General eTL Collaboration validation
        SQLValidationVisitor vVisitor = new SQLValidationVisitor();
        vVisitor.visit(this);
        valInfo.addAll(vVisitor.getValidationInfoList());

        // Operator usage validation.
        SQLOperatorInfoVisitor opInfo = new SQLOperatorInfoVisitor(true);
        opInfo.visit(this);
        valInfo.addAll(opInfo.getValidationInfoList());

        // Filter condition validation
        valInfo = ConditionBuilderUtil.filterValidations(valInfo);
        return valInfo;
    }

    public List<ValidationInfo> validateModel() {
        
        // General eTL Collaboration validation
        SQLValidationVisitor vVisitor = new SQLValidationVisitor();
        vVisitor.visit(this);
        List<ValidationInfo> valInfo = vVisitor.getValidationInfoList();

        // Operator usage validation.
        SQLOperatorInfoVisitor opInfo = new SQLOperatorInfoVisitor(true);
        opInfo.visit(this);
        valInfo.addAll(opInfo.getValidationInfoList());

        // Filter condition validation
        valInfo = ConditionBuilderUtil.filterValidations(valInfo);
        return valInfo;
    }
    /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> badgeValidate() {
        // TODO: Need to validate the drivers, file location used for data file
        List<ValidationInfo> valInfo = validateDbDrivers();

        // General eTL Collaboration validation
        SQLValidationVisitor vVisitor = new SQLValidationVisitor();
        vVisitor.visit(this);
        valInfo.addAll(vVisitor.getValidationInfoList());

        // Operator usage validation.
        SQLOperatorInfoVisitor opInfo = new SQLOperatorInfoVisitor(true);
        opInfo.visit(this);
        valInfo.addAll(opInfo.getValidationInfoList());

        // Filter condition validation
        valInfo = ConditionBuilderUtil.filterValidations(valInfo);
        return valInfo;
    }

    /**
     * Validates if the Database drivers required for this SQLDefinition are 
     * already installed in Database Explorer. 
     *
     * @return Map of invalid object as keys and reason as value
     */
    public List<ValidationInfo> validateDbDrivers() {
        SQLDBDriverValidationVisitor vVisitor = new SQLDBDriverValidationVisitor();
        vVisitor.visit(this);
        return vVisitor.getValidationInfoList();
    }

    /**
     * Validate Database synchronization. Identify any eTL Collaboration element which has been
     * deleted or modified in Database.
     *
     * @return Map of invalid object as keys and reason as value
     */
    public List<ValidationInfo> validateDbSynchronization() {
        SQLDBSynchronizationValidationVisitor vVisitor = new SQLDBSynchronizationValidationVisitor();
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
        for (Iterator iter = attributes.values().iterator(); iter.hasNext();) {
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

    private void addTable(SQLDBTable table) throws BaseException {
        // before adding a table we should create alias name and set it to table
        if (table.getAliasName() == null) {
            if (table.getObjectType() == SQLConstants.SOURCE_TABLE) {
                table.setAliasName(this.generateSourceTableAliasName());
            } else if (table.getObjectType() == SQLConstants.TARGET_TABLE) {
                table.setAliasName(this.generateTargetTableAliasName());
            }
        }

        boolean createDBModel = false;
        SQLDBModel dbModel = getExistingDatabaseModelFor(table);
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
                List<SQLDBTable> list = new ArrayList<SQLDBTable>();
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
        for (Iterator iter = inputObjects.values().iterator(); iter.hasNext();) {
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

    private void discoverSourceTables(SQLObject sqlObj, List<DBTable> tables) {
        SQLJoinOperator joinObject = (SQLJoinOperator) sqlObj;
        SQLObject left = joinObject.getSQLObject(SQLJoinOperator.LEFT);
        SQLObject right = joinObject.getSQLObject(SQLJoinOperator.RIGHT);

        if (left.getObjectType() == SQLConstants.SOURCE_TABLE) {
            tables.add((DBTable) left);
        } else {
            discoverSourceTables(left, tables);
        }

        if (right.getObjectType() == SQLConstants.SOURCE_TABLE) {
            tables.add((DBTable) right);
        } else {
            discoverSourceTables(right, tables);
        }
    }

    private void doSecondPassParse() throws BaseException {
        for (Iterator<SecondParseObjectInfo> it = secondPassList.iterator(); it.hasNext();) {
            SecondParseObjectInfo objInfo = it.next();
            objInfo.getSQLObject().secondPassParse(objInfo.getElement());
        }
        secondPassList.clear();
    }

    private String generateJoinViewAliasName() {
        String aName = J + 0;
        for (int cnt = 1; isJoinViewAliasNameExist(aName); cnt++) {
            aName = J + cnt;
        }
        return aName;
    }

    private String generateSourceTableAliasName() {
        String aName = S + 0;
        for (int cnt = 1; isSourceTableAliasNameExist(aName); cnt++) {
            aName = S + cnt;
        }
        return aName;
    }

    private String generateTargetTableAliasName() {
        String aName = T + 0;
        for (int cnt = 1; isTargetTableAliasNameExist(aName); cnt++) {
            aName = T + cnt;
        }
        return aName;
    }

    private SQLDBModel getExistingDatabaseModelFor(SQLDBTable table) throws BaseException {
        // if table is RuntimeInput or RuntimeOutput then we want to return runtime db model
        if (table.getObjectType() == SQLConstants.RUNTIME_INPUT || table.getObjectType() == SQLConstants.RUNTIME_OUTPUT) {
            return this.getRuntimeDbModel();
        }

        // for other tables we need to check if they have a parent db model
        DatabaseModel dbModel = table.getParent();
        if (dbModel == null) {
            return null;
        }

        List<SQLDBModel> dbModels;
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

        for (Iterator<SQLDBModel> it = dbModels.iterator(); it.hasNext();) {
            SQLDBModel existingModel = it.next();
            if (existingModel.getModelName().equals(dbModel.getModelName())) {
                return existingModel;
            }
        }
        return null;
    }

    private SQLObject getObjectFromDBModel(String objectId, int modelType) {
        Collection dbModels = getObjectsOfType(modelType);
        for (Iterator it = dbModels.iterator(); it.hasNext();) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            SQLObject sqlObj = dbModel.getObject(objectId);
            if (sqlObj != null) {
                return sqlObj;
            }
        }
        return null;
    }

    @SuppressWarnings(value = "fallthrough")
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
        for (Iterator it = joinViews.iterator(); it.hasNext();) {
            SQLJoinView joinView = (SQLJoinView) it.next();
            String sAlias = joinView.getAliasName();
            if (sAlias != null && sAlias.equals(aName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSourceTableAliasNameExist(String aName) {
        for (Iterator it = getSourceTables().iterator(); it.hasNext();) {
            SourceTable sTable = (SourceTable) it.next();
            String sAlias = sTable.getAliasName();
            if (sAlias != null && sAlias.equals(aName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetTableAliasNameExist(String aName) {
        for (Iterator it = getTargetTables().iterator(); it.hasNext();) {
            TargetTable tTable = (TargetTable) it.next();
            String tAlias = tTable.getAliasName();
            if (tAlias != null && tAlias.equals(aName)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Attribute> parseAttributeList(NodeList list) throws BaseException {
        Map<String, Attribute> attrMap = new LinkedHashMap<String, Attribute>();
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

    private void setSQLObjectId(List sqlObjects) throws BaseException {
        for (Iterator it = sqlObjects.iterator(); it.hasNext();) {
            SQLObject sqlObj = (SQLObject) it.next();
            sqlObj.setId(generateId());
        }
    }

    private void setTableId(List tables) throws BaseException {
        for (Iterator it = tables.iterator(); it.hasNext();) {
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

        for (int i = 0; it.hasNext();) {
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
}

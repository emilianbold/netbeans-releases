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
package org.netbeans.modules.sql.framework.model.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLContainerObject;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.Attribute;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.StringUtil;
import java.util.HashMap;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.ForeignKey;
import org.netbeans.modules.sql.framework.model.PrimaryKey;
import org.openide.awt.StatusDisplayer;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class SQLObjectUtil {

    public static final String FILE_LOC = "FILE_LOC";
    /* Log4J category string */
    private static final String LOG_CATEGORY = SQLObjectUtil.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLObjectUtil.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public static Map<String, String> getTableMetaData(SQLDBTable table) throws SQLException, BaseException {
        SQLDBModel dbModel = (SQLDBModel) table.getParent();
        HashMap<String, String> map = new HashMap<String, String>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            if (dbModel.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                dbModel.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                DBConnectionDefinition conndef = dbModel.getETLDBConnectionDefinition();
                conn = DBExplorerUtil.createConnection(conndef.getDriverClass(), conndef.getConnectionURL(), conndef.getUserName(), conndef.getPassword());

                stmt = conn.createStatement();
                String query = "select PROPERTY_NAME, PROPERTY_VALUE from AXION_TABLE_PROPERTIES " + "where TABLE_NAME = '" + table.getName() + "' ORDER BY PROPERTY_NAME";
                stmt.execute(query);
                rs = stmt.getResultSet();
                while (rs.next()) {
                    rs.getMetaData().getColumnCount();
                    String propKey = rs.getString(1);
                    String propVal = rs.getString(2);
                    map.put(propKey, StringUtil.escapeControlChars(propVal));
                }
            }
        } finally {

            try {
                rs.close();
            } catch (Exception ex) {
                //ignore
            }

            try {
                stmt.close();
            } catch (Exception ex) {
                //ignore
            }

            try {
                conn.close();
            } catch (Exception ex) {
                //ignore
            }
        }
        return map;
    }

    public static void setOrgProperties(SQLDBTable sTable) throws BaseException {
        try {
            Map<String, String> propsMap = SQLObjectUtil.getTableMetaData(sTable);
            for (String key : propsMap.keySet()) {
                sTable.setAttribute("ORGPROP_" + key, propsMap.get(key));
            }
        } catch (SQLException ex) {
            throw new BaseException(ex);
        }
    }

    public static void createMissingModelTablesInDB(ETLDataObject dataObj) {
        // This is the method that gets called when opening the eTL collab
        // for the first time.
        ETLCollaborationModel etlCollabModel = dataObj.getModel();
        List<SQLDBModel> models = etlCollabModel.getSourceDatabaseModels();
        List<SQLDBModel> targetModels = etlCollabModel.getTargetDatabaseModels();
        models.addAll(targetModels);
        // Get the database connection, query all the tables
        SQLDBModel aModel = null;
        for (int li = 0; li < models.size(); li++) {
            aModel = models.get(li);
            if (aModel == null) {
                //Fix me.
                continue;
            }
            try {
                //Check if all the tables from this model are present in the database
                checkAndCreateTables(aModel);
            } catch (BaseException be) {
                be.printStackTrace();
            } catch (SQLException sqe) {
                StatusDisplayer.getDefault().setStatusText("SQLObjectUtil.createMissingModelTablesInDB(): " + sqe.getMessage());
            }
        } // end for
    }

    private static String[][] getTablesForDBModel(SQLDBModel model) throws BaseException, SQLException {
        String[][] tableList = null;
        Connection conn = null;
        DBConnectionDefinition conndef = model.getETLDBConnectionDefinition();

        DBMetaDataFactory dbMeta = new DBMetaDataFactory();
        try {
            conn = DBExplorerUtil.createConnection(conndef.getDriverClass(), conndef.getConnectionURL(), conndef.getUserName(), conndef.getPassword());
            String catalog = null;
            String[] types = {"TABLE"};

            catalog = (conn.getCatalog() == null) ? "" : conn.getCatalog();
            dbMeta.connectDB(conn);
            tableList = dbMeta.getTables(catalog, "", "", types);

        } catch (Exception ex) {
            StatusDisplayer.getDefault().setStatusText("Failed to get tables for model: " + model.getDisplayName() +
                    "Reason: " + ex.getMessage());
            return null;
        } finally {
            dbMeta.disconnectDB();
        }
        return tableList;
    }

    private static void checkAndCreateTables(SQLDBModel aModel) throws BaseException, SQLException {
        //String aString = aModel.getETLDBConnectionDefinition().toString();
        //From the connection get all the tables
        if (aModel.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
            aModel.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {

            String[][] tableList = getTablesForDBModel(aModel);
            List<DBTable> tables = aModel.getTables();
            if (tables == null) {
                throw new BaseException("No tables in DB Model. Chceck.");
            }
            if (tableList == null) {
                //Recreate all tables from the model.
                for (DBTable table : tables) {
                    createTable((SQLDBTable) table, aModel);
                }
            } else {
                //Create only missing tables
                SQLDBTable aTable = null;
                for (int i = 0; i < tables.size(); i++) {
                    aTable = (SQLDBTable) tables.get(i);
                    String[] currTable;
                    boolean tableFound = false;
                    for (int ii = 0; ii < tableList.length; ii++) {
                        currTable = tableList[ii];
                        if (aTable.getName().equals(currTable[DBMetaDataFactory.NAME])) {
                            tableFound = true;
                            break;
                        }
                    }
                    if (!tableFound) {
                        createTable(aTable, aModel);
                    }
                }
            }
        }
    }

    public static void dropTable(SQLDBTable table, SQLDBModel model) throws BaseException, SQLException {
//        Connection conn = null;
//        Statement stmt = null;
//        DBConnectionDefinition conndef = model.getETLDBConnectionDefinition();
//        try {
//            conn = DBExplorerUtil.createConnection(conndef.getDriverClass(), conndef.getConnectionURL(), conndef.getUserName(), conndef.getPassword());
//            stmt = conn.createStatement();
//            stmt.execute(VirtualDBTable.getDropStatementSQL(table));
//        } catch (Exception ex) {
//            StatusDisplayer.getDefault().setStatusText("Failed to drop table: " + table.getName() +
//                    "Reason: " + ex.getMessage());
//        } finally {
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//            }
//        }
    }

    public static void createTable(SQLDBTable aTable, SQLDBModel model) throws BaseException, SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            String prefix = "ORGPROP_";
            //FIXME
            //VirtualDBTable fftbl = new VirtualDBTable(aTable);
            VirtualDBTable fftbl = new VirtualDBTable();
            Attribute attr = aTable.getAttribute("ORGPROP_LOADTYPE");

            if (attr != null) {
                DBConnectionDefinition conndef = model.getETLDBConnectionDefinition();
                conn = DBExplorerUtil.createConnection(conndef.getDriverClass(), conndef.getConnectionURL(), conndef.getUserName(), conndef.getPassword());
                stmt = conn.createStatement();

                String parseType = attr.getAttributeValue().toString();
                fftbl.setParseType(parseType);
                String createSQL = fftbl.getCreateStatementSQL(fftbl);

                StringBuffer sb = new StringBuffer(createSQL);
                sb.append("  ORGANIZATION( ");

                Collection<String> attrNames = (Collection<String>) aTable.getAttributeNames();
                for (String attrName : attrNames) {
                    if (attrName.startsWith(prefix)) {
                        String keyName = attrName.substring(attrName.indexOf(prefix) + prefix.length());
                        sb.append(keyName + "='");
                        String propValue = (String) aTable.getAttribute(attrName).getAttributeValue();
                        sb.append(StringUtil.escapeControlChars(propValue) + "' ");
                    }
                }
                sb.append(")");

                stmt.execute(sb.toString().trim());
                stmt.execute("shutdown");
            }

        } catch (Exception ex) {
            StatusDisplayer.getDefault().setStatusText("Failed to create table: " + aTable.getName() +
                    "Reason: " + ex.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    public static SourceColumn createRuntimeInput(SQLDBTable sTable, SQLDefinition sqlDefn) throws BaseException {
        SQLDBModel dbModel = (SQLDBModel) sTable.getParent();
        if (dbModel.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
            dbModel.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
            // set the flatfile location name for this table
            sTable.setFlatFileLocationRuntimeInputName(generateFFRuntimeInputName(FILE_LOC, sTable));
            RuntimeDatabaseModel rtDBModel = getOrCreateRuntimeModel(sqlDefn);
            // now use the same name as runtime input argument for file location
            String argName = sTable.getFlatFileLocationRuntimeInputName();
            RuntimeInput rtInput = rtDBModel.getRuntimeInput();

            if (rtInput == null) {
                rtInput = getOrCreateRuntimeInput(rtDBModel);
            }
            // if runtime input arg does not exist then only add it
            if (rtInput.getColumn(argName) == null) {
                SourceColumn arg = createRuntimeInputArg(sTable, argName, dbModel.getETLDBConnectionDefinition());
                rtInput.addColumn(arg);
                return arg;
            }
        } else {
            // Fix for CR#6771984
            RuntimeDatabaseModel rtDBModel = getOrCreateRuntimeModel(sqlDefn);
            RuntimeInput rtInput = rtDBModel.getRuntimeInput();
            String argName = "arg_0";
            if (rtInput == null) {
                rtInput = getOrCreateRuntimeInput(rtDBModel);
            }
            // if runtime input arg does not exist then only add it
            if (rtInput.getColumnList().isEmpty() && rtInput.getColumn(argName) == null) {
                SourceColumn arg = SQLModelObjectFactory.getInstance().createSourceColumn(
                        argName, java.sql.Types.VARCHAR, 0, 0, true);
                arg.setVisible(false); // the column is not visible in canvas
                rtInput.addColumn(arg);
                return arg;
            }    
        }
        
        return null;
    }

    public static SourceColumn createRuntimeInputArg(SQLDBTable sTable, String ffArgName) {
        SourceColumn srcColumn = SQLModelObjectFactory.getInstance().createSourceColumn(
                ffArgName, java.sql.Types.VARCHAR, 0, 0, true);
        srcColumn.setEditable(false); // the name is not editable
        srcColumn.setVisible(false); // the column is not visible in canvas

        // set default value
        VirtualDBTable flatfileTable = (VirtualDBTable) (sTable);
        if (flatfileTable != null) {
            srcColumn.setDefaultValue(flatfileTable.getFileName());
        }
        return srcColumn;
    }

    public static SourceColumn createRuntimeInputArg(SQLDBTable sTable,
            String ffArgName, DBConnectionDefinition connDef) {
        SourceColumn srcColumn = SQLModelObjectFactory.getInstance().createSourceColumn(
                ffArgName, java.sql.Types.VARCHAR, 0, 0, true);
        srcColumn.setEditable(false); // the name is not editable
        srcColumn.setVisible(false); // the column is not visible in canvas

        // set default value
        srcColumn.setDefaultValue(getFileNameFromDB(sTable, connDef));
        return srcColumn;
    }

    private static String getFileNameFromDB(SQLDBTable sTable, DBConnectionDefinition conDef) {
        String fileName = "";
        Connection conn = null;
        try {
            conn = DBExplorerUtil.createConnection(conDef.getConnectionProperties());
            Statement stmt = conn.createStatement();
            String query = "select PROPERTY_NAME, PROPERTY_VALUE from AXION_TABLE_PROPERTIES " + "where TABLE_NAME = '" + sTable.getName() + "'";
            stmt.execute(query);
            
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                if (rs.getString(1).equals("FILENAME")) {
                    fileName = fileName + rs.getString(2);
                    break;
                }
            }
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT124: Error while retrieving file name{0}", SQLDefinition.class.getName()), ex);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                conn = null;
            }
        }
        return fileName;
    }

    public static String generateFFRuntimeInputName(String prefix, SQLDBTable table) {
        String genName = prefix + "_" + table.getUniqueTableName();
        mLogger.infoNoloc(mLoc.t("EDIT125: table name{0} for table", genName, table.getName()));
        return genName;
    }

    public static String generateTemporaryTableName(String tableName) {
        // converting time to hex to take less chars
        String sysName = "RAW_" + Long.toHexString((long) (java.lang.Math.random() * 100000000)) + "_" + tableName;

        // DB2 cannot accept table names > 18 char
        if (sysName.length() > 18) {
            sysName = sysName.substring(0, 18);
        }

        mLogger.infoNoloc(mLoc.t("EDIT126: temp table name{0} for table", sysName, tableName));
        return sysName.toUpperCase();
    }

    public static String generateTemporaryTableName(String prefix, String tableName) {
        String sysName = prefix + "_" + Long.toHexString((long) (java.lang.Math.random() * 100000000)) + "_" + tableName;

        // DB2 cannot accept table names > 18 char
        if (sysName.length() > 18) {
            sysName = sysName.substring(0, 18);
        }

        mLogger.infoNoloc(mLoc.t("EDIT126: temp table name{0} for table", sysName, tableName));
        return sysName;
    }

    public static List getAllExpressionObjects(Collection objs) {
        ArrayList expObjects = new ArrayList();

        Iterator it = objs.iterator();

        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            if (obj instanceof SQLConnectableObject) {
                expObjects.add(obj);
            }
        }
        return expObjects;
    }

    /**
     * Gets ancestral SQLDefinition instance for the given SQLExpresionObject.
     *
     * @param sqlObj SQLConnectableObject whose ancestor SQLDefinition is sought
     * @return SQLDefinition instance
     */
    public static SQLDefinition getAncestralSQLDefinition(SQLObject sqlObj) {
        SQLDefinition defn = null;
        SQLObject loopObj = sqlObj;
        Object parentObject = loopObj.getParentObject();

        do {
            if (parentObject instanceof SQLDefinition) {
                defn = (SQLDefinition) parentObject;
                break;
            } else if (parentObject instanceof SQLContainerObject) {
                parentObject = ((SQLContainerObject) parentObject).getParent();
            } else if (parentObject == null) {
                return null;
            } else if (parentObject instanceof SQLObject) {
                parentObject = ((SQLObject) parentObject).getParentObject();
            }
        } while (true);

        return defn;
    }

    /**
     * Creates a List of automatically generated join objects (if any) between the given
     * and existing s in the model. Any new instances will need to be added to this
     * SQLDefinition, as well as their contained SQLPredicate conditions.
     *
     * @param right SourceTable against which we check for join relationships with
     *        existing SourceTables
     * @lookupCollection collection where to lookup the join
     * @return List of Join objects, empty if no relationships were detected.
     * @throws BaseException for any internal (unspecific) error.
     */
    public static List getAutoJoins(SQLJoinTable right, Collection lookupCollection) throws BaseException {
        if (right == null) {
            throw new BaseException("Must supply non-null ref for param 'right'.");
        }

        List joinList = Collections.EMPTY_LIST;
        Iterator iter = lookupCollection.iterator();
        if (iter.hasNext()) {
            joinList = new ArrayList();
            do {
                SQLJoinTable left = (SQLJoinTable) iter.next();
                if (left == right) {
                    continue;
                }

                SQLJoinOperator newJoin = createAutoJoin(left, right);
                if (newJoin != null) {
                    joinList.add(newJoin);
                }
            } while (iter.hasNext());
        }

        return joinList;
    }

    public static SQLConnectableObject getExpressionObject(SQLObject sqlObject, Collection allObjects) {
        Iterator it = allObjects.iterator();
        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            if (obj instanceof SQLConnectableObject) {
                SQLConnectableObject expObj = (SQLConnectableObject) obj;
                Iterator eIt = expObj.getInputObjectMap().values().iterator();
                while (eIt.hasNext()) {
                    SQLInputObject inObj = (SQLInputObject) eIt.next();
                    SQLObject sqlObj = inObj.getSQLObject();

                    if (sqlObj != null && sqlObj.equals(sqlObject)) {
                        return expObj;
                    }
                }
            }
        }
        return null;
    }

    /**
     * get source table connected to this sql object
     */
    public static SourceTable getInputSourceTable(SQLObject obj) {
        if (obj == null) {
            return null;
        }

        if (obj.getObjectType() == SQLConstants.SOURCE_TABLE) {
            return (SourceTable) obj;
        }

        if (obj.getObjectType() == SQLConstants.SOURCE_COLUMN) {
            SourceColumn column = (SourceColumn) obj;
            return getInputSourceTable((SourceTable) column.getParent());
        }

        if (obj instanceof SQLConnectableObject) {
            SQLConnectableObject expObj = (SQLConnectableObject) obj;

            Iterator it = expObj.getInputObjectMap().values().iterator();
            while (it.hasNext()) {
                SQLInputObject inObj = (SQLInputObject) it.next();
                SQLObject sqlObj = inObj.getSQLObject();
                SourceTable table = getInputSourceTable(sqlObj);
                if (table != null) {
                    return table;
                }
            }

            List children = expObj.getChildSQLObjects();
            Iterator cIt = children.iterator();
            while (cIt.hasNext()) {
                SQLObject chObj = (SQLObject) cIt.next();

                SourceTable table = getInputSourceTable(chObj);
                if (table != null) {
                    return table;
                }
            }
        }
        return null;
    }

    /**
     * get mapped target table for a source table
     */
    public static SourceTable getInputSourceTable(SQLObject obj, Collection allObjects) {
        SQLConnectableObject topExpObj = getTopExpressionObject(obj, allObjects);
        if (topExpObj != null) {
            return getInputSourceTable(topExpObj);
        }
        return getInputSourceTable(obj);
    }

    /**
     * get mapped target table for a source table
     */
    public static TargetTable getMappedTargetTable(SQLJoinView jv, Collection targetTables) {
        if (jv == null) {
            return null;
        }

        Iterator it = targetTables.iterator();
        while (it.hasNext()) {
            TargetTable tt = (TargetTable) it.next();
            if (jv.equals(tt.getJoinView())) {
                return tt;
            }
        }
        return null;
    }

    /**
     * get mapped target table for a source table
     */
    public static TargetTable getMappedTargetTable(SQLObject sqlObj, Collection targetTables) {
        if (sqlObj.getObjectType() == SQLConstants.TARGET_TABLE) {
            return (TargetTable) sqlObj;
        }

        Iterator it = targetTables.iterator();

        while (it.hasNext()) {
            TargetTable tt = (TargetTable) it.next();
            if (isSQLObjectMappedToTarget(sqlObj, tt)) {
                return tt;
            }
        }
        return null;
    }

    /**
     * Gets RuntimeInput, if any, associated with the parent SQLDefinition of the given
     * SQLDBTable.
     *
     * @param table SQLDBTable whose parent's RuntimeInput is to be obtained
     * @return RuntimeInput associated with the parent of <i>table </i>, or null if no
     *         such instance exists.
     */
    public static RuntimeInput getRuntimeInput(SQLDBTable table) {
        SQLDefinition sqlDefinition = getAncestralSQLDefinition(table);
        if (sqlDefinition != null) {
            RuntimeDatabaseModel runModel = sqlDefinition.getRuntimeDbModel();
            if (runModel != null) {
                RuntimeInput rInput = runModel.getRuntimeInput();
                return rInput;
            }
        }
        return null;
    }

    /**
     * Generates runtime output name to be used in referencing the count of rows inserted
     * or updated for the given TargetTable.
     *
     * @param targetTable TargetTable whose count of rows will be referenced by the
     *        generated runtime output
     * @return String representing runtime output name for count of rows processed in
     *         <code>targetTable</code>
     */
    public static String getTargetTableCountRuntimeOutput(TargetTable targetTable) {
        return "Count_" + targetTable.getUniqueTableName(); // NOI18N
    }

    /*
     * get the expression object whose input is given sql object
     */
    // since currently we do not keep the reference to output object to which
    // a given input object is connected we need to do this logic to do that.
    public static SQLConnectableObject getTopExpressionObject(SQLObject sqlObject, Collection allObjects) {
        SQLConnectableObject topExpObj = null;
        SQLConnectableObject tmpObj = null;
        topExpObj = getExpressionObject(sqlObject, allObjects);
        tmpObj = topExpObj;

        while (tmpObj != null) {
            tmpObj = getExpressionObject(tmpObj, allObjects);
            if (tmpObj != null) {
                topExpObj = tmpObj;
            }
        }

        return topExpObj;
    }

    public static SQLCanvasObject getTopSQLCanvasObject(SQLObject sqlObj) {
        if (sqlObj instanceof SQLCanvasObject) {
            return (SQLCanvasObject) sqlObj;
        }

        Object parentObj = sqlObj.getParentObject();
        while (parentObj != null && parentObj instanceof SQLObject && !(parentObj instanceof SQLCanvasObject)) {
            parentObj = ((SQLObject) parentObj).getParentObject();
        }

        if (parentObj instanceof SQLCanvasObject) {
            return (SQLCanvasObject) parentObj;
        }

        return null;
    }

    /**
     * get source table connected to this sql object
     */
    public static boolean isAggregateFunctionMapped(SQLObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof SQLGenericOperator) {
            return ((SQLGenericOperator) obj).isAggregateFunction();
        }

        if (obj instanceof SQLConnectableObject) {
            SQLConnectableObject expObj = (SQLConnectableObject) obj;

            Iterator it = expObj.getInputObjectMap().values().iterator();
            while (it.hasNext()) {
                SQLInputObject inObj = (SQLInputObject) it.next();
                SQLObject sqlObj = inObj.getSQLObject();
                if (isAggregateFunctionMapped(sqlObj)) {
                    return true;
                }
            }

            List children = expObj.getChildSQLObjects();
            Iterator cIt = children.iterator();
            while (cIt.hasNext()) {
                SQLObject chObj = (SQLObject) cIt.next();
                if (isAggregateFunctionMapped(chObj)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isObjectMappedToExpression(SQLObject obj, SQLConnectableObject expObj) {
        boolean response = false;

        Iterator it = expObj.getInputObjectMap().values().iterator();
        while (it.hasNext()) {
            SQLInputObject inObj = (SQLInputObject) it.next();
            SQLObject sqlObj = inObj.getSQLObject();

            if (sqlObj != null && sqlObj.equals(obj)) {
                response = true;
                break;
            }
        }

        return response;
    }

    public static void migrateJoinCondition(SQLPredicate predicate, SQLCondition condition) throws BaseException {
        migrateConnectableObject(predicate, condition);
        condition.addObject(predicate); // add predicate to SQLCondition
    }

    public static SourceColumn removeRuntimeInput(SQLDBTable sTable, CollabSQLUIModel sqlModel) throws BaseException {
        SQLDefinition sqlDefn = sqlModel.getSQLDefinition();
        // get the file location arg name
        String fArgName = sTable.getFlatFileLocationRuntimeInputName();
        RuntimeDatabaseModel rtDBModel = getOrCreateRuntimeModel(sqlDefn);

        RuntimeInput rtInput = rtDBModel.getRuntimeInput();
        if (rtInput != null && fArgName != null) {
            SourceColumn arg = (SourceColumn) rtInput.getColumn(fArgName);
            if (arg != null) {
                rtInput.deleteColumn(fArgName);
                return arg;
            }
        }

        return null;
    }

    private static SQLJoinOperator createAutoJoin(SQLJoinTable one, SQLJoinTable two) {
        return discoverJoinPredicate(one, two);
    }

    private static SQLJoinOperator discoverJoinPredicate(SQLJoinTable one, SQLJoinTable two) {
        SourceTable oneS = one.getSourceTable();
        SourceTable twoS = two.getSourceTable();
        String name = "Join-" + oneS.getName() + "-" + twoS.getName();

        PrimaryKey pk = null;
        SQLJoinTable left = null;
        SourceColumn leftColumn;

        ForeignKey fk = null;
        SQLJoinTable right = null;
        SourceColumn rightColumn;

        SQLJoinOperator jmd = null;

        if (oneS.references(twoS)) {
            pk = twoS.getPrimaryKey();
            fk = oneS.getReferenceFor(twoS);
            left = two;
            right = one;
        } else if (twoS.references(oneS)) {
            pk = oneS.getPrimaryKey();
            fk = twoS.getReferenceFor(oneS);
            left = one;
            right = two;

        }

        if (left != null && right != null && pk != null && fk != null) {
            // TODO support composite keys
            // Just get first column for now - joins and predicates don't yet.
            leftColumn = (SourceColumn) left.getSourceTable().getColumn((String) pk.getColumnNames().get(0));
            rightColumn = (SourceColumn) right.getSourceTable().getColumn((String) fk.getColumnNames().get(0));

            try {
                jmd = (SQLJoinOperator) SQLObjectFactory.createObjectForTag(SQLConstants.STR_JOIN_OPERATOR);
                jmd.setDisplayName(name);
                jmd.setJoinType(SQLConstants.INNER_JOIN);
                jmd.setJoinConditionType(SQLJoinOperator.SYSTEM_DEFINED_CONDITION);

                SQLCondition condition = SQLModelObjectFactory.getInstance().createSQLCondition(SQLJoinOperator.JOIN_CONDITION);

                VisibleSQLPredicate joinPredicate = (VisibleSQLPredicate) SQLObjectFactory.createObjectForTag(SQLConstants.STR_VISIBLE_PREDICATE);
                joinPredicate.setDisplayName(oneS.getName() + "-" + twoS.getName());
                joinPredicate.setOperatorType("=");

                ColumnRef leftColumnRef = SQLModelObjectFactory.getInstance().createColumnRef(leftColumn);
                condition.addObject(leftColumnRef);

                ColumnRef rightColumnRef = SQLModelObjectFactory.getInstance().createColumnRef(rightColumn);
                condition.addObject(rightColumnRef);

                joinPredicate.addInput(SQLPredicate.LEFT, leftColumnRef);
                joinPredicate.addInput(SQLPredicate.RIGHT, rightColumnRef);
                joinPredicate.setRoot(null);

                condition.addObject(joinPredicate);

                // Make sure condition object state is stable.
                condition.setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
                condition.getRootPredicate();

                jmd.setJoinCondition(condition);

                jmd.addInput(SQLJoinOperator.LEFT, left);
                jmd.addInput(SQLJoinOperator.RIGHT, right);
            } catch (BaseException sqlEx) {
                mLogger.errorNoloc(mLoc.t("EDIT128: Failed to create auto-join{0}", LOG_CATEGORY), sqlEx);

                jmd = null;
            }
        }

        return jmd;
    }

    private static RuntimeInput getOrCreateRuntimeInput(RuntimeDatabaseModel rtDBModel) throws BaseException {
        RuntimeInput rtInput = rtDBModel.getRuntimeInput();
        // do nothing, just return
        if (rtInput != null) {
            return rtInput;
        }

        rtInput = SQLModelObjectFactory.getInstance().createRuntimeInput();
        rtInput.setName("RuntimeInput"); // NOI18N

        return rtInput;
    }

    private static RuntimeDatabaseModel getOrCreateRuntimeModel(SQLDefinition sqlDefn) throws BaseException {
        if (sqlDefn != null) {
            RuntimeDatabaseModel rtDBModel = sqlDefn.getRuntimeDbModel();
            // If RuntimeDBModel exists
            if (rtDBModel != null) {
                return rtDBModel;
            }
            rtDBModel = SQLModelObjectFactory.getInstance().createRuntimeDatabaseModel();
            sqlDefn.addObject(rtDBModel);
            return rtDBModel;
        }

        return null;
    }

    private static boolean isSQLObjectMappedToTarget(SQLObject sqlObject, SQLConnectableObject expObj) {
        Iterator it = expObj.getInputObjectMap().values().iterator();
        while (it.hasNext()) {
            SQLInputObject inObj = (SQLInputObject) it.next();
            SQLObject sqlObj = inObj.getSQLObject();

            if (sqlObj != null) {
                if (sqlObj.equals(sqlObject)) {
                    return true;
                } else if (sqlObject.getObjectType() == SQLConstants.SOURCE_TABLE && sqlObj.getObjectType() == SQLConstants.SOURCE_COLUMN) {
                    SourceColumn sColumn = (SourceColumn) sqlObj;
                    if (sqlObject.equals(sColumn.getParent())) {
                        return true;
                    }
                } else if (sqlObj instanceof SQLConnectableObject) {
                    SQLConnectableObject linedExpObj = (SQLConnectableObject) sqlObj;
                    if (isSQLObjectMappedToTarget(sqlObject, linedExpObj)) {
                        return true;
                    }
                }
            }
        }

        List children = expObj.getChildSQLObjects();
        Iterator cIt = children.iterator();
        while (cIt.hasNext()) {
            SQLObject chObj = (SQLObject) cIt.next();
            if (chObj != null) {
                if (chObj instanceof SQLConnectableObject) {
                    if (isSQLObjectMappedToTarget(sqlObject, (SQLConnectableObject) chObj)) {
                        return true;
                    }
                } else if (sqlObject.getObjectType() == SQLConstants.SOURCE_TABLE && chObj.getObjectType() == SQLConstants.SOURCE_COLUMN) {
                    SourceColumn sColumn = (SourceColumn) chObj;
                    if (sqlObject.equals(sColumn.getParent())) {
                        return true;
                    }
                } else if (chObj.equals(sqlObject)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void migrateConnectableObject(SQLConnectableObject expObj, SQLCondition condition) throws BaseException {
        expObj.reset(); // reset the predicate id and add it to jon view

        Map inputs = expObj.getInputObjectMap();
        Iterator it = inputs.values().iterator();
        while (it.hasNext()) {
            SQLInputObject objIn = (SQLInputObject) it.next();
            SQLObject obj = objIn.getSQLObject();
            if (obj != null) {
                if (obj.getObjectType() == SQLConstants.SOURCE_COLUMN) {
                    ColumnRef columnRef = SQLModelObjectFactory.getInstance().createColumnRef((SQLDBColumn) obj);
                    condition.addObject(columnRef);
                    objIn.setSQLObject(columnRef);
                } else {
                    obj.reset();
                    if (obj instanceof SQLConnectableObject) {
                        migrateConnectableObject((SQLConnectableObject) obj, condition);
                    }
                    condition.addObject(obj);
                }
            }
        }
    }
}


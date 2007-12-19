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
package org.netbeans.modules.etl.codegen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.netbeans.modules.etl.codegen.ETLCodegenUtil;
import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilder;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.codegen.PatternFinder;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.axion.AxionDB;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConnectionFactory;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.AttributeMap;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * Base class for all ETLStrategyBuilder classes.
 * 
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public abstract class BaseETLStrategyBuilder implements ETLStrategyBuilder {

    protected static final MessageManager MSG_MGR = MessageManager.getManager("org.netbeans.modules.etl.codegen.impl");
    private static final String RUNTIME_INPUTS_MAP = "runtimeInputsMap";

    public static Properties getConnectionPropertiesFrom(DBConnectionDefinition def) {
        Properties props = null;
        if (def instanceof SQLDBConnectionDefinition) {
            props = ((SQLDBConnectionDefinition) def).getConnectionProperties();
        } else {
            props = new Properties();
            props.put(DBConnectionFactory.PROP_DBTYPE, def.getDBType());
            props.put(DBConnectionFactory.PROP_URL, def.getConnectionURL());
            props.put(DBConnectionFactory.PROP_USERNAME, def.getUserName());
            props.put(DBConnectionFactory.PROP_PASSWORD, def.getPassword());
        }
        return props;
    }
    protected ETLScriptBuilderModel builderModel = null;
    protected DBConnectionFactory connFactory = DBConnectionFactory.getInstance();
    protected DBConnectionDefinition targtConDef = null;

    public BaseETLStrategyBuilder(ETLScriptBuilderModel model) throws BaseException {
        this.builderModel = model;
    }

    public void setETLScriptBuilderModel(ETLScriptBuilderModel model) {
        this.builderModel = model;
    }

    protected void addCreateIfNotExistsSummaryTableStatement(ETLTaskNode initTask) throws BaseException {
        DB db = DBFactory.getInstance().getDatabase(DB.AXIONDB);
        Statements stmts = db.getStatements();

        SQLPart createSummaryTablePart = stmts.getCreateLogSummaryTableStatement(builderModel.isMemoryMonitorDB());
        createSummaryTablePart.setType(SQLPart.STMT_CREATELOGSUMMARYTABLE);
        createSummaryTablePart.setConnectionPoolName(ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);

        initTask.addStatement(createSummaryTablePart);
    }

    /**
     * @see org.netbeans.modules.etl.codegen.ETLStrategyBuilder#addInsertNewExecutionRecordStatement(org.netbeans.modules.etl.engine.ETLTaskNode)
     */
    protected void addInsertNewExecutionRecordStatement(ETLTaskNode taskNode, TargetTable tt) throws BaseException {
        DB db = DBFactory.getInstance().getDatabase(DB.AXIONDB);

        StatementContext context = new StatementContext();
        context.setUsingUniqueTableName(true);

        Statements stmts = db.getStatements();
        SQLPart insertStartDateIntoSummaryTablePart = stmts.getInsertStartDateIntoSummaryTableStatement(tt, context);
        insertStartDateIntoSummaryTablePart.setConnectionPoolName(ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);

        context.setUsingFullyQualifiedTablePrefix(false);
        insertStartDateIntoSummaryTablePart.setTableName(db.getUnescapedName(db.getGeneratorFactory().generate(tt, context)));

        taskNode.addStatement(insertStartDateIntoSummaryTablePart);
    }

    protected void addSelectExecutionIdForNewExecutionRecordStatement(ETLTaskNode pipeline, TargetTable tt) throws BaseException {
        DB db = DBFactory.getInstance().getDatabase(DB.AXIONDB);

        StatementContext context = new StatementContext();
        context.setUsingUniqueTableName(true);
        context.setUsingFullyQualifiedTablePrefix(false);

        Statements stmts = db.getStatements();
        SQLPart selectExecutionIdPart = stmts.getSelectExecutionIdFromSummaryTableStatement(tt, context);
        selectExecutionIdPart.setConnectionPoolName(ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);

        selectExecutionIdPart.setTableName(db.getUnescapedName(db.getGeneratorFactory().generate(tt, context)));

        pipeline.addStatement(selectExecutionIdPart);
    }

    /**
     * @see org.netbeans.modules.etl.codegen.ETLStrategyBuilder#addUpdateExecutionRecordPreparedStatement(org.netbeans.modules.etl.engine.ETLTaskNode,
     *      TargetTable)
     */
    protected void addUpdateExecutionRecordPreparedStatement(ETLTaskNode updateStats, TargetTable tt) throws BaseException {
        DB db = DBFactory.getInstance().getDatabase(DB.AXIONDB);

        StatementContext context = new StatementContext();
        context.setUsingFullyQualifiedTablePrefix(false);
        context.setUsingUniqueTableName(true);

        Statements stmts = db.getStatements();
        SQLPart updateEndDatePart = stmts.getUpdateEndDateInSummaryTableStatement(tt, context);
        updateEndDatePart.setConnectionPoolName(ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);

        String tableName = db.getUnescapedName(db.getGeneratorFactory().generate(tt, context));
        updateEndDatePart.setTableName(tableName);
        updateStats.addTableSpecificStatement(tableName, updateEndDatePart);
    }

    protected boolean areAllAssociatedTablesInternal(TargetTable tt) throws BaseException {
        Collection participatingTables = tt.getSourceTableList();
        return PatternFinder.isInternalDBTable(tt) && PatternFinder.allDBTablesAreInternal(participatingTables.iterator());
    }

    protected void checkTargetConnectionDefinition(ETLStrategyBuilderContext context) throws BaseException {
        targtConDef = context.getModel().getConnectionDefinition(context.getTargetTable());

        if (targtConDef == null) {
            throw new BaseException("Target table connection definition is null.");
        }
    }

    protected SQLPart createSQLPart(String sqlString, String key, String conDefnName) {
        SQLPart sqlPart = new SQLPart(sqlString, key, conDefnName);
        return sqlPart;
    }

    protected void createTargetTableIfNotExists(TargetTable table, ETLTaskNode taskNode, String trgtConnName, DB targetDB, StatementContext context) throws BaseException {
        if (table.isCreateTargetTable() && (!(targetDB instanceof AxionDB))) {
            StringBuilder sqlBuffer = new StringBuilder(200);

            Statements stmts = targetDB.getStatements();

            context.clearAllUsingTempTableName();

            SQLPart ifExists = stmts.getTableExistsStatement(table, context);
            SQLPart doCreate = stmts.getCreateStatement(table, context);

            sqlBuffer.append(ifExists.getSQL()).append(SQLPart.STATEMENT_SEPARATOR);
            sqlBuffer.append(doCreate.getSQL());

            SQLPart createIfExists = new SQLPart(sqlBuffer.toString(), SQLPart.STMT_CREATEBEFOREPROCESS, trgtConnName);
            taskNode.addStatement(createIfExists);
        }
    }

    protected void createTransformerSQLPart(TargetTable tt, boolean useSourceFilter, ETLTaskNode transformerTask, String trgtConnName,
            ETLTaskNode cleanupTask, DB targetDB, DB statsDB) throws BaseException {

        StatementContext context = new StatementContext();
        context.setUsingFullyQualifiedTablePrefix(false);
        context.setUsingUniqueTableName(true);
        final String statsTableName = statsDB.getUnescapedName(statsDB.getGeneratorFactory().generate(tt, context));
        transformerTask.setTableName(statsTableName);

        int statementType = tt.getStatementType();

        SQLPart insertSelectPart = null;
        SQLPart defragPartStatement = null;

        List srcTblList = tt.getSourceTableList();

        insertSelectPart = generateTransformerSQLPart(tt, useSourceFilter, targetDB, statementType, srcTblList);
        insertSelectPart.setTableName(statsTableName);
        insertSelectPart.setConnectionPoolName(trgtConnName);
        transformerTask.addStatement(insertSelectPart);

        // defrag if required.
        defragPartStatement = generateDefragPart(tt, targetDB, statementType);
        if (defragPartStatement != null) {
            defragPartStatement.setConnectionPoolName(trgtConnName);
            cleanupTask.addStatement(defragPartStatement);
        }

        StatementContext tgtContext = new StatementContext();
        if ((this.builderModel.isConnectionDefinitionOverridesApplied()) && (PatternFinder.isInternalDBTable(tt))) {
            tgtContext.setUsingUniqueTableName(tt, true);
        }
        createTargetTableIfNotExists(tt, transformerTask, trgtConnName, targetDB, tgtContext);
        truncateTargetTableIfExists(tt, transformerTask, trgtConnName, targetDB.getStatements(), tgtContext);

        // Insert new execution record for target table, tt.
        addInsertNewExecutionRecordStatement(transformerTask, tt);

        // Add query to obtain execution id assigned to new execution record.
        addSelectExecutionIdForNewExecutionRecordStatement(transformerTask, tt);
    }

    protected void createCorrelatedUpdateSQLParts(TargetTable tt,
            ETLTaskNode correlatedQueryExecutor,
            String trgtConnName,
            DB targetDB,
            DB statsDB,
            boolean genInsertSelect) throws BaseException {
        SQLPart insertSelect = null;
        SQLPart select = null;
        SQLPart update = null;

        StatementContext context = new StatementContext();
        context.setUsingFullyQualifiedTablePrefix(false);
        context.setUsingUniqueTableName(true);
        final String statsTableName = statsDB.getUnescapedName(statsDB.getGeneratorFactory().generate(tt, context));
        correlatedQueryExecutor.setTableName(statsTableName);

        Map map = getCorrelatedUpdateQueries(tt, targetDB, genInsertSelect);
        insertSelect = (SQLPart) map.get(SQLPart.STMT_INSERTSELECT);
        select = (SQLPart) map.get(SQLPart.STMT_CORRELATED_SELECT);
        update = (SQLPart) map.get(SQLPart.STMT_CORRELATED_UPDATE);

        select.setTableName(statsTableName);
        select.setConnectionPoolName(trgtConnName);

        update.setTableName(statsTableName);
        update.setConnectionPoolName(trgtConnName);

        correlatedQueryExecutor.addStatement(select);
        correlatedQueryExecutor.addStatement(update);

        if (genInsertSelect) {
            insertSelect.setTableName(statsTableName);
            insertSelect.setConnectionPoolName(trgtConnName);
            correlatedQueryExecutor.addStatement(insertSelect);
        }

        StatementContext tgtContext = new StatementContext();
        createTargetTableIfNotExists(tt, correlatedQueryExecutor, trgtConnName, targetDB, tgtContext);
        truncateTargetTableIfExists(tt, correlatedQueryExecutor, trgtConnName, targetDB.getStatements(), tgtContext);

        // Insert new execution record for target table, tt.
        addInsertNewExecutionRecordStatement(correlatedQueryExecutor, tt);

        // Add query to obtain execution id assigned to new execution record.
        addSelectExecutionIdForNewExecutionRecordStatement(correlatedQueryExecutor, tt);

        AttributeMap attrMap = new AttributeMap();
        attrMap.put("batchSize", tt.getBatchSize() + ""); // NOI18N
        correlatedQueryExecutor.setAttributeMap(attrMap);
    }

    protected Map getCorrelatedUpdateQueries(TargetTable tt, DB targetDB, boolean genInsertSelect) throws BaseException {
        Map ret = new HashMap();
        StatementContext context;
        List srcTblList = tt.getSourceTableList();

        Statements stmts = targetDB.getStatements();
        context = new StatementContext();

        Iterator iter = srcTblList.iterator();
        while (iter.hasNext()) {
            SourceTable srcTable = (SourceTable) iter.next();
            if (isExtractionRequired(srcTable, tt)) {
                context.setUsingTempTableName(srcTable, true);
            } else {
                useUniqueNameIfRequired(srcTable, context);
            }
        }
        context.putClientProperty(RUNTIME_INPUTS_MAP, this.builderModel.getEngine().getInputAttrMap());
        ret = stmts.getCorrelatedUpdateStatement(tt, context);

        if (genInsertSelect) {
            context.putClientProperty("useWhere", Boolean.TRUE);
            SQLPart insertSelectPart = stmts.getInsertSelectStatement(tt, context);
            ret.put(SQLPart.STMT_INSERTSELECT, insertSelectPart);
        }
        return ret;
    }

    // Generate defrag when required.
    protected SQLPart generateDefragPart(TargetTable tt, DB targetDB, int statementType) throws BaseException {
        SQLPart defragPartStatement = null;
        StatementContext localContext = new StatementContext();
        localContext.setUsingFullyQualifiedTablePrefix(false);
        localContext.setUsingUniqueTableName(true);

        switch (statementType) {
            case SQLConstants.UPDATE_STATEMENT:
            case SQLConstants.INSERT_UPDATE_STATEMENT:
            case SQLConstants.DELETE_STATEMENT:
                defragPartStatement = targetDB.getStatements().getDefragStatement(tt, localContext);
                break;

            default:
                break;
        }
        return defragPartStatement;
    }

    protected final void useUniqueNameIfRequired(SQLDBTable table, StatementContext sc) throws BaseException {
        if ((this.builderModel.isConnectionDefinitionOverridesApplied()) && (PatternFinder.isInternalDBTable(table))) {
            sc.setUsingUniqueTableName(table, true);
        }
    }

    // Generate Insert Select Part
    protected SQLPart generateTransformerSQLPart(TargetTable tt, boolean useSourceFilter, DB targetDB, int statementType, List srcTblList)
            throws BaseException {
        Statements stmts = targetDB.getStatements();
        StatementContext context = new StatementContext();

        Iterator iter = srcTblList.iterator();
        while (iter.hasNext()) {
            SourceTable srcTable = (SourceTable) iter.next();
            if (isExtractionRequired(srcTable, tt)) {
                context.setUsingTempTableName(srcTable, true);
            } else {
                useUniqueNameIfRequired(srcTable, context);
            }
        }

        useUniqueNameIfRequired(tt, context);

        SQLPart insertSelectPart;
        switch (statementType) {
            case SQLConstants.UPDATE_STATEMENT:
                insertSelectPart = stmts.getUpdateStatement(tt, context);
                break;

            case SQLConstants.INSERT_UPDATE_STATEMENT:
                // Generate merge-select statement for transformation.
                insertSelectPart = stmts.getMergeStatement(tt, context);
                break;

            case SQLConstants.DELETE_STATEMENT:
                context.putClientProperty("useWhere", Boolean.TRUE);
                insertSelectPart = stmts.getDeleteStatement(tt, context);
                break;

            default:
                if (srcTblList == null || srcTblList.size() == 0) {
                    insertSelectPart = stmts.getStaticInsertStatement(tt, context);
                } else {
                    // Generate insert select statement for transformation.
                    context.putClientProperty("useWhere", Boolean.valueOf(useSourceFilter));
                    insertSelectPart = stmts.getInsertSelectStatement(tt, context);
                }
                break;
        }
        return insertSelectPart;
    }

    protected SQLPart generateSQLPart(FlatfileDBTable flatfileDBTable, String tableName, String staticDirectory, String stmtType, String connPoolName,
            String flatfileRuntimeFilePath, boolean isDynamicPath, boolean createDataFileIfNotExist) {

        SQLPart sqlPart = null;

        // NOTE: getCreateStatementSQL() and getDropStatementSQL() were modified not to
        // have the side effect of assigning tableName as flatfileDBTable's new name. If
        // this is required, then cast FlatfileDBTable to FlatfileDBTableImpl and call
        // setName().
        if (stmtType.equals("DROP")) {
            sqlPart = createSQLPart(FlatfileDBTableImpl.getDropStatementSQL(tableName), stmtType, connPoolName);
        } else if (stmtType.equals("CREATE")) {
            sqlPart = createSQLPart(flatfileDBTable.getCreateStatementSQL(staticDirectory, tableName, flatfileRuntimeFilePath, isDynamicPath,
                    createDataFileIfNotExist), stmtType, connPoolName);
        }

        // Provide a fully-qualified filename, appending staticDirectory as the path, as
        // default value if dynamic path is true; otherwise, use just the sample filename
        // as supplied from the Database.
        String defaultValue = (isDynamicPath ? staticDirectory + "/" : "") + flatfileDBTable.getFileName();
        sqlPart.setDefaultValue(defaultValue);

        sqlPart.setTableName(tableName);
        sqlPart.setConnectionPoolName(connPoolName);

        return sqlPart;
    }

    /**
     * Generate Transformer comment line for a given target table
     * 
     * @param targetTable
     * @return Comment String
     */
    protected String getCommentForTransformer(TargetTable targetTable) throws BaseException {
        return MSG_MGR.getString("DISPLAY_INSERT_TARGET", targetTable.getParent().getModelName(), this.builderModel.getConnectionDefinition(
                targetTable).getDBType());
    }

    /**
     * @param srcType
     * @return
     * @throws BaseException
     */
    protected DB getDBFor(DBConnectionDefinition srcConDefn) throws BaseException {
        int srcType = connFactory.getDatabaseVersion(getConnectionPropertiesFrom(srcConDefn));
        return DBFactory.getInstance().getDatabase(srcType);
    }

    protected ETLEngine getEngine() {
        return builderModel.getEngine();
    }

    protected Statements getStatementsForTableDB(DBTable table) throws BaseException {
        DBConnectionDefinition cd = table.getParent().getConnectionDefinition();
        DB db = getDBFor(cd);
        return db.getStatements();
    }

    protected Statements getStatementsForTableDB(DBTable table, ETLStrategyBuilderContext context) throws BaseException {
        DBConnectionDefinition cd = context.getModel().getConnectionDefinition(table);
        DB db = getDBFor(cd);
        return db.getStatements();
    }

    protected Statements getStatementsForTargetTableDB(ETLStrategyBuilderContext context) throws BaseException {
        DBTable table = context.getTargetTable();
        DBConnectionDefinition cd = context.getModel().getConnectionDefinition(table);
        DB db = getDBFor(cd);
        return db.getStatements();
    }

    protected void getFlatfileInitSQLParts(FlatfileDefinition ffdb, InternalDBMetadata ffMetadata, boolean isAllDBTypeInternal,
            ETLTaskNode initTask, ETLTaskNode cleanupTask, String connPoolName, SQLDBTable flatfileRuntime, boolean createDataFileIfNotExist) {
        String staticDirectory = ffMetadata.getStaticDirectory();
        boolean isDynamicFilePath = ffMetadata.isDynamicFilePath();

        String tableName = null;
        String flatfileRuntimeFilePath = flatfileRuntime.getRuntimeArgumentName();
        String oId = flatfileRuntime.getUniqueTableName();
        FlatfileDBTable flatfileTable = (FlatfileDBTable) ffdb.getTable(flatfileRuntime.getName());

//        if (isAllDBTypeInternal) {
        tableName = oId;
//        } else {
//            tableName = flatfileTable.getName();
//        }

        if (flatfileTable != null) {
            SQLPart dropPart = generateSQLPart(flatfileTable, tableName, staticDirectory, "DROP", connPoolName, flatfileRuntimeFilePath, isDynamicFilePath,
                    createDataFileIfNotExist);
            SQLPart createPart = generateSQLPart(flatfileTable, tableName, staticDirectory, "CREATE", connPoolName, flatfileRuntimeFilePath,
                    isDynamicFilePath, createDataFileIfNotExist);

            initTask.addOptionalTask(dropPart);
            initTask.addOptionalTask(createPart);
            cleanupTask.addOptionalTask(dropPart);
        }
    }

    protected void getFlatfileInitSQLParts(FlatfileDefinition ffdb, InternalDBMetadata internalMetadata, ETLTaskNode initTask,
            ETLTaskNode cleanupTask, SQLDBTable flatfileRuntime, boolean createDataFileIfNotExist) {
        String staticDirectory = internalMetadata.getStaticDirectory();
        boolean isDynamicFilePath = internalMetadata.isDynamicFilePath();

        FlatfileDBTable flatfileTable = (FlatfileDBTable) ffdb.getTable(flatfileRuntime.getName());
        String tableName = flatfileRuntime.getUniqueTableName();
        String flatfileRuntimeFilePath = flatfileRuntime.getRuntimeArgumentName();

        if (flatfileTable != null) {
            SQLPart dropPart = generateSQLPart(flatfileTable, tableName, staticDirectory, "DROP", ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME,
                    flatfileRuntimeFilePath, isDynamicFilePath, createDataFileIfNotExist);
            SQLPart createPart = generateSQLPart(flatfileTable, tableName, staticDirectory, "CREATE",
                    ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME, flatfileRuntimeFilePath, isDynamicFilePath, createDataFileIfNotExist);

            initTask.addOptionalTask(dropPart);
            initTask.addOptionalTask(createPart);
            cleanupTask.addOptionalTask(dropPart);
        }
    }

    protected String getTargetConnName() {

        if (targtConDef.getDriverClass().equals("org.axiondb.jdbc.AxionDriver") &&
                builderModel.isConnectionDefinitionOverridesApplied()) {
            return ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME;
        }
        return targtConDef.getName();
    }

    protected Statements getTargetStatements() throws BaseException {
        DB aDb = getDBFor(targtConDef);
        return aDb.getStatements();
    }

    protected String getTransformerSQL(TargetTable targetTable, DB targetDB, List srcTblList, boolean useSourceFilter) throws BaseException {
        String sql = null;
        int statementType = targetTable.getStatementType();
        if ((targetDB.getDBType() == DB.JDBCDB) && (targetTable.getSourceTableList().size() != 0) && ((statementType == SQLConstants.UPDATE_STATEMENT) || (statementType == SQLConstants.INSERT_UPDATE_STATEMENT))) {
            Map map = this.getCorrelatedUpdateQueries(targetTable, targetDB, (statementType == SQLConstants.INSERT_UPDATE_STATEMENT));
            SQLPart select = (SQLPart) map.get(SQLPart.STMT_CORRELATED_SELECT);
            SQLPart update = (SQLPart) map.get(SQLPart.STMT_CORRELATED_UPDATE);
            sql = select.getSQL();
            sql = sql + "\n\n\n" + update.getSQL();
        } else {
            SQLPart insertSelectPart = generateTransformerSQLPart(targetTable, useSourceFilter, targetDB, statementType, srcTblList);
            sql = insertSelectPart.getSQL();
        }


        return sql;
    }

    protected boolean isExtractionRequired(SourceTable sourceTable, TargetTable targetTable) throws BaseException {
        return (!PatternFinder.isFromSameDB(sourceTable, targetTable, this.builderModel));
    }

    protected List makeUniqueTableNames(ETLStrategyBuilderContext context) throws BaseException {
        Iterator iter = context.getTargetTable().getSourceTableList().iterator();
        List oldUserDefinedList = new ArrayList();
        while (iter.hasNext()) {
            SQLDBTable dbTable = (SQLDBTable) iter.next();
            oldUserDefinedList.add(dbTable.getUserDefinedTableName());
            makeTableNameUnique(dbTable);
        }

        SQLDBTable tt = context.getTargetTable();
        oldUserDefinedList.add(tt.getUserDefinedTableName());
        makeTableNameUnique(tt);
        return oldUserDefinedList;
    }

    protected void populateInitTask(ETLTaskNode initTask, ETLTaskNode cleanupTask, TargetTable tTable) throws BaseException {
        DBConnectionDefinition dbCondefn = builderModel.getConnectionDefinition(tTable);
        String tgtConnPoolName = dbCondefn.getName();

        InternalDBMetadata tgtInternalMetadata = builderModel.getInternalMetadata(tTable);
        if (tgtInternalMetadata == null) {
            // Placeholder instance.
            tgtInternalMetadata = new InternalDBMetadata(null, false, null);
        }

        int targetDBType = connFactory.getDatabaseVersion(getConnectionPropertiesFrom(dbCondefn));
        String poolName = (targetDBType == DB.AXIONDB) ? ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME: tgtConnPoolName;
        populateFlatfileMetadata(tTable, poolName , tgtInternalMetadata, initTask, cleanupTask);


        DB db = DBFactory.getInstance().getDatabase(targetDBType);
        StatementContext context = new StatementContext();
        SQLPart initStatements = db.getStatements().getInitializationStatements(context);

        if (initStatements != null) {
            initStatements.setConnectionPoolName(poolName);
            initTask.addStatement(initStatements);
        }
    }

    protected void populateFlatfileMetadata(TargetTable tTable, String tgtConnPoolName, InternalDBMetadata tgtInternalMetadata, ETLTaskNode initTask,
            ETLTaskNode cleanupTask) throws BaseException {
        Collection participatingTables = tTable.getSourceTableList();
        boolean isAllDBTypeInternal = PatternFinder.isInternalDBTable(tTable) && PatternFinder.allDBTablesAreInternal(participatingTables.iterator());

        // For Target Table
        FlatfileDefinition tgtDB = ETLCodegenUtil.getStcdbObjectTypeDefinition(tTable);
        if (tgtDB != null && tgtInternalMetadata != null && tgtInternalMetadata.getStaticDirectory() != null) {
            getFlatfileInitSQLParts(tgtDB, tgtInternalMetadata, isAllDBTypeInternal, initTask, cleanupTask, tgtConnPoolName, tTable, true);
        }

        // For all SourceTables associated with Target table
        for (Iterator it = participatingTables.iterator(); it.hasNext();) {
            SQLDBTable dbTable = (SQLDBTable) it.next();
            FlatfileDefinition srcDB = ETLCodegenUtil.getFFDefinition(dbTable);
            if (srcDB != null) {
                DBConnectionDefinition srcCondefn = builderModel.getConnectionDefinition(dbTable);
                InternalDBMetadata srcInternalMetadata = builderModel.getInternalMetadata(dbTable);
                String conDefnName = getConDefnName(srcCondefn);

                if (srcInternalMetadata != null) {
                    if (isAllDBTypeInternal) {
                        conDefnName = tgtConnPoolName;
                    }
                    getFlatfileInitSQLParts(srcDB, srcInternalMetadata, isAllDBTypeInternal, initTask, cleanupTask, conDefnName, dbTable, false);
                }
            }
        }
    }

    protected String getConDefnName(DBConnectionDefinition srcCondefn) {
        if (srcCondefn.getDriverClass().equals("org.axiondb.jdbc.AxionDriver") &&
                builderModel.isConnectionDefinitionOverridesApplied()) {
            return ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME;
        }
        return srcCondefn.getName();
    }

    protected void truncateTargetTableIfExists(TargetTable tt, ETLTaskNode taskNode, String trgtConnName, Statements stmts, StatementContext sc) throws BaseException {
        if (tt.isTruncateBeforeLoad()) {
            truncateTableIfExists((SQLDBTable) tt, taskNode, trgtConnName, stmts, sc);
        }
    }

    protected void truncateTableIfExists(SQLDBTable dbt, ETLTaskNode taskNode, String trgtConnName, Statements stmts, StatementContext sc) throws BaseException {
        SQLPart ifExists = stmts.getTableExistsStatement(dbt, sc);
        String existsSql = (ifExists != null) ? ifExists.getSQL().trim() : null;
        StringBuilder sqlBuffer = StringUtil.isNullString(existsSql) ? new StringBuilder(200) : new StringBuilder(existsSql);

        SQLPart doTruncate = stmts.getTruncateStatement(dbt, sc);
        String truncateSql = doTruncate.getSQL().trim();
        if (!StringUtil.isNullString(truncateSql)) {
            if (sqlBuffer.length() != 0) {
                sqlBuffer.append(SQLPart.STATEMENT_SEPARATOR);
            }

            sqlBuffer.append(truncateSql);
            SQLPart truncateIfExists = new SQLPart(sqlBuffer.toString(), SQLPart.STMT_TRUNCATEBEFOREPROCESS, trgtConnName);
            taskNode.addStatement(truncateIfExists);
        } else {
            truncateSql = "";
        }

        StatementContext context = new StatementContext();
        context.putClientProperty("useWhere", Boolean.FALSE);
        SQLPart doDelete = stmts.getDeleteStatement(dbt, context);
        String deleteSql = doDelete.getSQL().trim();

        // Only add the fallback delete statement if it is not identical to the
        // truncate statement. The delete and truncate statements are identical for
        // those DBs which do not have a separate 'truncate' command.
        if (!StringUtil.isNullString(deleteSql) && !deleteSql.equalsIgnoreCase(truncateSql)) {
            sqlBuffer = StringUtil.isNullString(existsSql) ? new StringBuilder(200) : new StringBuilder(existsSql);
            if (sqlBuffer.length() != 0) {
                sqlBuffer.append(SQLPart.STATEMENT_SEPARATOR);
            }

            sqlBuffer.append(deleteSql);

            SQLPart deleteIfExists = new SQLPart(sqlBuffer.toString(), SQLPart.STMT_DELETEBEFOREPROCESS, trgtConnName);
            taskNode.addStatement(deleteIfExists);
        }
    }

    private void makeTableNameUnique(SQLDBTable dbTable) {
        String name = dbTable.getUniqueTableName();
        dbTable.setUserDefinedTableName(name);
    }
}

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.etl.codegen.ETLCodegenUtil;
import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.axion.AxionDB;
import org.netbeans.modules.sql.framework.codegen.axion.AxionPipelineStatements;
import org.netbeans.modules.sql.framework.codegen.axion.AxionStatements;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConstants;
import com.sun.sql.framework.jdbc.SQLPart;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

/**
 * @author Jonathan Giron
 * @author Ritesh Adval
 * @version $Revision$
 */
public class PipelinedStrategyBuilderImpl extends BaseETLStrategyBuilder {

    private static final String LOG_CATEGORY = PipelinedStrategyBuilderImpl.class.getName();
    private static final MessageManager msgMgr = MessageManager.getManager(ETLTaskNode.class);
    private static transient final Logger mLogger = Logger.getLogger(PipelinedStrategyBuilderImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    protected AxionDB db;
    protected Map linkTableMap;
    protected AxionPipelineStatements pipelineStmts;
    protected List remoteTables;
    protected AxionStatements stmts;

    public PipelinedStrategyBuilderImpl(ETLScriptBuilderModel model) throws BaseException {
        super(model);

        db = (AxionDB) DBFactory.getInstance().getDatabase(DB.AXIONDB);
        stmts = (AxionStatements) db.getStatements();
        pipelineStmts = db.getAxionPipelineStatements();
        linkTableMap = new HashMap();
        remoteTables = new ArrayList(3);
    }

    public void addSelectRejectedRowCountStatement(ETLTaskNode updateStats, TargetTable tt) throws BaseException {
        // Empty to be overridden by sub class
    }

    /**
     * Builds ETLTaskNode to hold and execute statements associated with execution cleanup
     * for the given List of TargetTables.
     *
     * @param pfGen PipelinedFlowGenerator to use in building the cleanup task node
     * @param targetTables List of TargetTables whose cleanup statements are to be
     *        generated
     * @return ETLTaskNode containing cleanup statements to be executed
     * @throws BaseException if error occurs during statement generation
     */
    public ETLTaskNode buildCleanupTask(List targetTables) throws BaseException {
        ETLTaskNode cleanupTask = this.getEngine().createETLTaskNode(ETLEngine.CLEANUP);
        buildCleanupStatements(cleanupTask, targetTables);

        // Add statement to shut down the pipelining database.
        SQLPart shutdownInternalDB = new SQLPart("SHUTDOWN", SQLPart.STMT_DEFRAG, ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
        cleanupTask.addStatement(shutdownInternalDB);

        if (this.builderModel.isShutdownMonitorDB()) {
            shutdownInternalDB = new SQLPart("SHUTDOWN", SQLPart.STMT_DEFRAG, ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);
            cleanupTask.addOptionalTask(shutdownInternalDB);
        }

        return cleanupTask;
    }

    public ETLTaskNode buildInitTask(List targetTables) throws BaseException {
        ETLEngine engine = this.getEngine();
        ETLTaskNode initTask = engine.createETLTaskNode(ETLEngine.INIT);
        Map connDefToLinkName = new HashMap();

        linkTableMap.clear();

        // Create set of all tables participating in the collaboration.
        Set tables = new HashSet(10);
        tables.addAll(targetTables);
        Iterator ttIter = targetTables.iterator();
        while (ttIter.hasNext()) {
            TargetTable tt = (TargetTable) ttIter.next();
            tables.addAll(tt.getSourceTableList());
        }

        // Create execution summary table if it does not exist.
        addCreateIfNotExistsSummaryTableStatement(initTask);

        // Now build all required initialization statements for each table.
        Iterator tblIter = tables.iterator();
        while (tblIter.hasNext()) {
            SQLDBTable table = (SQLDBTable) tblIter.next();
            buildInitializationStatements(table, initTask, connDefToLinkName);
        }

        return initTask;
    }

    /**
     * Before calling apply appropriate applyConnections
     */
    public void generateScriptForTable(ETLStrategyBuilderContext context) throws BaseException {
        mLogger.infoNoloc(mLoc.t("EDIT004: Looping through target tables:{0}", LOG_CATEGORY));
        checkTargetConnectionDefinition(context);

        TargetTable tt = context.getTargetTable();

        buildFlatfileSQLParts(context.getInitTask(), context.getGlobalCleanUpTask(), context.getTargetTable(), context.getModel());

        ETLTaskNode pipelineTask = buildPipelineTransformTask(context, tt);

        context.getInitTask().addNextETLTaskNode(ETLTask.SUCCESS, pipelineTask.getId());
        context.setLastPipelinedTask(pipelineTask);
        pipelineTask.addNextETLTaskNode(ETLTask.SUCCESS, context.getNextTaskOnSuccess().getId());
        pipelineTask.addNextETLTaskNode(ETLTask.EXCEPTION, context.getNextTaskOnException().getId());

        // Add query to obtain execution id assigned to new execution record.
        addSelectExecutionIdForNewExecutionRecordStatement(pipelineTask, tt);

        // Add queries to get count of rejected rows and update the execution record
        // in update statistics task
        addSelectRejectedRowCountStatement(context.getStatsUpdateTask(), tt);
        addUpdateExecutionRecordPreparedStatement(context.getStatsUpdateTask(), tt);
    }

    public String getScriptToDisplay(ETLStrategyBuilderContext context) throws BaseException {
        super.checkTargetConnectionDefinition(context);

        StringBuilder buffer = new StringBuilder();
        TargetTable targetTable = context.getTargetTable();

        StatementContext stmtContext = new StatementContext();
        stmtContext.setUsingFullyQualifiedTablePrefix(false);

        String transformSQL = createTransformStatement(targetTable, stmtContext).getSQL();
        buffer.append(getCommentForTransformer(targetTable)).append("\n");
        buffer.append(transformSQL);

        return buffer.toString();

    }

    void buildFlatfileSQLParts(ETLTaskNode initTask, ETLTaskNode cleanupTask, TargetTable tTable, ETLScriptBuilderModel model) throws BaseException {
        Collection participatingTables = tTable.getSourceTableList();
        InternalDBMetadata tgtInternalMetadata = model.getInternalMetadata(tTable);

        //For Target Table
        FlatfileDefinition trgFFDef = ETLCodegenUtil.getStcdbObjectTypeDefinition(tTable);
        if (trgFFDef instanceof FlatfileDefinition && tgtInternalMetadata != null) {
            getFlatfileInitSQLParts(trgFFDef, tgtInternalMetadata, initTask, cleanupTask, tTable, true);
        }

        //For all SourceTables associated with Target table
        for (Iterator it = participatingTables.iterator(); it.hasNext();) {
            SQLDBTable dbTable = (SQLDBTable) it.next();
            FlatfileDefinition srcRepObj = ETLCodegenUtil.getStcdbObjectTypeDefinition(dbTable);
            if (srcRepObj != null) {
                FlatfileDefinition srcFFDef = srcRepObj;
                InternalDBMetadata srcInternalMetadata = model.getInternalMetadata(dbTable);
                if (srcInternalMetadata != null) {
                    getFlatfileInitSQLParts(srcFFDef, srcInternalMetadata, initTask, cleanupTask, dbTable, false);
                }
            }
        }
    }

    ETLTaskNode buildPipelineTransformTask(ETLStrategyBuilderContext context, TargetTable tt) throws BaseException {
        String displayName = msgMgr.getString("TEMPLATE_dn", getTaskType(), tt.getName());
        ETLTaskNode pipeline = context.getModel().getEngine().createETLTaskNode(getTaskType());
        pipeline.setDisplayName(displayName);

        StatementContext sc = new StatementContext();
        sc.setUsingUniqueTableName(true);
        sc.putClientProperty(StatementContext.USE_FULLY_QUALIFIED_TABLE, Boolean.FALSE);

        truncateTargetTableIfExists(tt, pipeline, ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME, db.getAxionPipelineStatements(), sc);
        SQLPart insertSelectPart = createTransformStatement(tt, sc);

        insertSelectPart.setConnectionPoolName(ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
        pipeline.addStatement(insertSelectPart);

        // Use unique table name to generate table name associated with
        // pipeline/validation task.
        sc.setUseTargetTableAliasName(false);
        pipeline.setTableName(db.getUnescapedName(db.getGeneratorFactory().generate(tt, sc)));

        // Insert new execution record for target table, tt.
        super.addInsertNewExecutionRecordStatement(pipeline, tt);

        return pipeline;
    }

    /**
     * @param cleanupTask
     * @param pfGen
     * @param targetTables
     */
    protected void buildCleanupStatements(ETLTaskNode cleanupTask, List targetTables) throws BaseException {
        StatementContext context = new StatementContext();
        context.setUsingFullyQualifiedTablePrefix(false);

        Iterator iter = targetTables.iterator();
        while (iter.hasNext()) {
            TargetTable tt = (TargetTable) iter.next();
            if (!remoteTables.contains(tt)) { // Generate defrag only for non-remote
                // tables
                SQLPart defragPartStatement = generateDefragPart(tt, db, tt.getStatementType());
                if (defragPartStatement != null) {
                    defragPartStatement.setConnectionPoolName(ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
                    cleanupTask.addOptionalTask(defragPartStatement);
                }
            }
        }
    }

    protected void buildInitializationStatements(SQLDBTable table, ETLTaskNode initTask, Map connDefToLinkName) throws BaseException {
        DBConnectionDefinition connDef = this.builderModel.getConnectionDefinition(table);

        if (requiresRemoteAccess(table)) {
            // Generate a unique name for the DB link, ensuring that the link name is a
            // legal SQL identifier, then generate SQL statement(s).
            String linkName = StringUtil.createSQLIdentifier(connDef.getName());

            if (!connDefToLinkName.containsValue(linkName)) {
                // Create an eTL-specific connection definition that references the
                // correct driver class, among other things. This is used solely to
                // generate organization parameters for "CREATE EXTERNALDB TABLE"
                // statements.
                SQLDBConnectionDefinition etlConnDef = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(connDef);
                etlConnDef.setDriverClass(connDef.getDriverClass());
                SQLPart initPart = new SQLPart(getCreateDBLinkSQL(etlConnDef, linkName), SQLPart.STMT_INITIALIZESTATEMENTS,
                        ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);

                initTask.addOptionalTask(initPart);
                connDefToLinkName.put(connDef, linkName);
            }

            // Generate a unique name for each and add to init task SQL part
            Set linkTables = (Set) linkTableMap.get(linkName);
            if (linkTables == null) {
                linkTables = new HashSet();
                linkTableMap.put(linkName, linkTables);
            }
            linkTables.add(table);
            remoteTables.add(table);

            StatementContext context = new StatementContext();
            context.setUsingUniqueTableName(table, true);
            context.setUsingFullyQualifiedTablePrefix(false);
            String localName = db.getUnescapedName(db.getGeneratorFactory().generate(table, context));

            SQLPart initPart = new SQLPart(getCreateRemoteTableSQL(table, localName, linkName), SQLPart.STMT_INITIALIZESTATEMENTS,
                    ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
            initTask.addOptionalTask(initPart);

            // FIXME: create remount statement in Axion statement Generator
            // FIXME: Do we need remount here ?
            SQLPart remountPart = new SQLPart("REMOUNT EXTERNAL TABLE " + (StringUtil.isNullString(localName) ? table.getName() : localName),
                    SQLPart.STMT_INITIALIZESTATEMENTS, ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
            initTask.addOptionalTask(remountPart);
        }
    }

    protected void createTargetTableIfNotExists(TargetTable table, ETLTaskNode taskNode, String trgtConnName, DB targetDB) throws BaseException {
        if (table.isCreateTargetTable()) {
            StringBuilder sqlBuffer = new StringBuilder(200);

            Statements tStmts = targetDB.getStatements();
            StatementContext context = new StatementContext();

            context.clearAllUsingTempTableName();

            SQLPart ifExists = tStmts.getTableExistsStatement(table, context);
            SQLPart doCreate = tStmts.getCreateStatement(table, context);

            sqlBuffer.append(ifExists.getSQL()).append(SQLPart.STATEMENT_SEPARATOR);
            sqlBuffer.append(doCreate.getSQL());

            SQLPart createIfExists = new SQLPart(sqlBuffer.toString(), SQLPart.STMT_CREATEBEFOREPROCESS, trgtConnName);
            taskNode.addStatement(createIfExists);
        }
    }

    /**
     * Creates SQLPart containing appropriate transformation statement for the given run
     * mode and TargetTable instance, using the given StatementContext to assist in
     * statement generation.
     *
     * @param runMode ETLProcessFlowGenerator.DESIGN_TIME if statement will be executed in
     *        design time, ETLProcessFlowGenerator.RUN_TIME if statement will be executed
     *        in runtime on an app server
     * @param tt TargetTable whose transformation statement is to be generated
     * @param context StatementContext to use in generating transformation statement
     * @return SQLPart containing transformation statement
     * @throws BaseException if error occurs during statement generation
     */
    protected SQLPart createTransformStatement(TargetTable tt, StatementContext context) throws BaseException {
        SQLPart insertSelectPart;

        Statements statements = this.pipelineStmts;

        int statementType = tt.getStatementType();
        switch (statementType) {
            case SQLConstants.UPDATE_STATEMENT:
                insertSelectPart = statements.getUpdateStatement(tt, context);
                break;

            case SQLConstants.INSERT_UPDATE_STATEMENT:
                // Generate merge-select statement for transformation.
                insertSelectPart = statements.getMergeStatement(tt, context);
                break;

            case SQLConstants.DELETE_STATEMENT:
                context.putClientProperty("useWhere", Boolean.TRUE);
                insertSelectPart = statements.getDeleteStatement(tt, context);
                break;

            default:
                List srcTblList = tt.getSourceTableList();
                if (srcTblList == null || srcTblList.size() == 0) {
                    insertSelectPart = statements.getStaticInsertStatement(tt, context);
                } else {
                    // Generate insert select statement for transformation.
                    context.putClientProperty("useWhere", Boolean.TRUE);
                    insertSelectPart = statements.getInsertSelectStatement(tt, context);
                }

                break;
        }

        context.setUseTargetTableAliasName(false);
        insertSelectPart.setTableName(db.getUnescapedName(db.getGeneratorFactory().generate(tt, context)));
        return insertSelectPart;
    }

    /**
     * Generate Transformer comment line for a given target table
     *
     * @param targetTable
     * @return Comment String
     */
    @Override
    protected String getCommentForTransformer(TargetTable targetTable) throws BaseException {
        return MSG_MGR.getString("DISPLAY_INSERT_TARGET_PIPELINE", targetTable.getParent().getModelName(), this.builderModel.getConnectionDefinition(
                targetTable).getDBType());
    }

    protected String getCreateDBLinkSQL(DBConnectionDefinition connDef, String linkName) throws BaseException {
        StringBuilder stmtBuf = new StringBuilder(50);

        // Generate check for link existence + drop statement if necessary
        // Generate a "create DB link" statement using connection parameters in the
        // connection definition
        stmtBuf.append(stmts.getCreateDBLinkStatement(connDef, linkName).getSQL());

        return stmtBuf.toString();
    }

    /**
     * Generates drop external statement for the given SQLDBTable if appropriate.
     *
     * @param table SQLDBTable for which to generate a drop external statement
     * @param localName local name of table as used in the Axion database; may be
     *        different from the table name contained in <code>table</code>
     * @param ifExists true if statement should include an "IF EXISTS" qualifier
     * @param context StatementContext to use in generating statement
     * @return SQL statement representing drop external statement for SQLDBTable.
     * @throws BaseException if error occurs during statement generation
     */
    protected String getDropExternalTableSQL(SQLDBTable table, String localName, boolean ifExists, StatementContext context) throws BaseException {
        return stmts.getDropExternalTableStatement(table, localName, ifExists, context).getSQL();
    }

    protected String getTaskType() {
        return ETLEngine.PIPELINE;
    }

    protected boolean requiresRemoteAccess(SQLDBTable table) {
        if (this.builderModel.isConnectionDefinitionOverridesApplied()) {
            // If table is not an Axion table, create an external remote table if it
            // doesn't
            // already exist. We handle Axion flatfiles in buildFlatfileSQLParts(), though
            // we will create a log table for target tables, regardless of DB type.
            DBConnectionDefinition connDef = table.getParent().getConnectionDefinition();
            return (SQLUtils.getSupportedDBType(connDef.getDBType()) != DBConstants.AXION);
        } else {
            return true;
        }
    }

    private String getCreateRemoteTableSQL(SQLDBTable table, String localName, String linkName) throws BaseException {
        StringBuilder stmtBuf = new StringBuilder(50);
        if (StringUtil.isNullString(localName)) {
            localName = table.getName();
        }

        // Generate a "create external table" statement that references its DB link
        stmtBuf.append(stmts.getCreateRemoteTableStatement(table, localName, linkName).getSQL());
        return stmtBuf.toString();
    }
}

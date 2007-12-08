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

import java.util.List;
import java.util.Map;

import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.utils.MonitorUtil;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLPart;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

/**
 * Extends base pipelining strategy builder to override methods which affect conduct and
 * execution of a validating collaboration.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ValidatingStrategyBuilderImpl extends PipelinedStrategyBuilderImpl {
    public ValidatingStrategyBuilderImpl(ETLScriptBuilderModel model) throws BaseException {
        super(model);
    }

    /**
     * Adds statement, if appropriate, to the given ETLTaskNode in order to obtain count
     * of rejected rows from rejection details table.
     *
     * @param updateStats ETLTaskNode to receive new statement
     * @param tt TargetTable instance from which rejected row table information is derived
     * @throws BaseException if error occurs during statement generation
     */
    @Override
    public void addSelectRejectedRowCountStatement(ETLTaskNode updateStats, TargetTable tt) throws BaseException {
        StatementContext context = new StatementContext();
        context.setUsingFullyQualifiedTablePrefix(false);
        context.setUsingUniqueTableName(true);

        // Add SelectRejectedRowsCountFromDetailsTableStatement
        SQLPart selectRejectedCountPart = pipelineStmts.getSelectRejectedRowsCountFromDetailsTableStatement(tt);
        selectRejectedCountPart.setConnectionPoolName(ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);

        String tableName = db.getUnescapedName(db.getGeneratorFactory().generate(tt, context));
        selectRejectedCountPart.setTableName(tableName);
        updateStats.addTableSpecificStatement(tableName, selectRejectedCountPart);
    }

    @Override
    public String getScriptToDisplay(ETLStrategyBuilderContext context) throws BaseException {
        super.checkTargetConnectionDefinition(context);

        StringBuilder buffer = new StringBuilder();
        TargetTable targetTable = context.getTargetTable();

        StatementContext stmtContext = new StatementContext();
        stmtContext.setUsingFullyQualifiedTablePrefix(false);

        DBConnectionDefinition tgtConnDef = context.getModel().getConnectionDefinition(targetTable);
        buffer.append(MSG_MGR.getString("DISPLAY_TARGET_LOG_VALIDATING", targetTable.getParent().getModelName(), tgtConnDef.getDBType()));
        buffer.append("\n");

        String targetLogSQL = getCreateLogDetailsTableSQL(targetTable);
        buffer.append(targetLogSQL).append("\n\n");

        String transformSQL = createTransformStatement(targetTable, stmtContext).getSQL();
        buffer.append(getCommentForTransformer(targetTable)).append("\n");
        buffer.append(transformSQL);

        return buffer.toString();
    }

    @Override
    protected void buildCleanupStatements(ETLTaskNode cleanupTask, List targetTables) throws BaseException {
        super.buildCleanupStatements(cleanupTask, targetTables);

        // Add statement to shut down the pipelining database.
        SQLPart shutdownPipeline = new SQLPart("SHUTDOWN", SQLPart.STMT_DEFRAG, ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
        cleanupTask.addStatement(shutdownPipeline);
    }

    @Override
    protected void buildInitializationStatements(SQLDBTable table, ETLTaskNode initTask, Map connDefToLinkName) throws BaseException {
        super.buildInitializationStatements(table, initTask, connDefToLinkName);
        if (table instanceof TargetTable) {
            doBuildInitializationStatements((TargetTable) table, initTask, connDefToLinkName);
        }
    }

    /**
     * Implements construction of appropriate initialization statements for a validating
     * engine task.
     *
     * @param table TargetTable for which to construct initialization statements
     * @param initTask ETLTaskNode representing initialization node to hold generated
     *        statements
     * @param connDefToLinkName Map of DBConnectionDefinition instances to DB link names
     * @param pfGen PipelinedFlowGenerator instance to use in generating statements
     * @throws BaseException if error occurs during statement generation
     */
    protected void doBuildInitializationStatements(TargetTable table, ETLTaskNode initTask, Map connDefToLinkName) throws BaseException {
        StatementContext localContext = new StatementContext();
        localContext.setUsingFullyQualifiedTablePrefix(false);
        localContext.setUsingUniqueTableName(true);

        TargetTable detailsTable = getLogTableFor(table);

        if (!this.builderModel.isConnectionDefinitionOverridesApplied()) {
            SQLPart dropLogPart = new SQLPart(getDropExternalTableSQL(detailsTable, "", true, localContext), SQLPart.STMT_INITIALIZESTATEMENTS,
                ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);
            initTask.addOptionalTask(dropLogPart);
        }

        SQLPart createLogPart = new SQLPart(getCreateLogDetailsTableSQL(table), SQLPart.STMT_INITIALIZESTATEMENTS,
            ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);
        initTask.addOptionalTask(createLogPart);

        if (!this.builderModel.isConnectionDefinitionOverridesApplied()) {
            SQLPart truncateLogPart = stmts.getTruncateStatement(detailsTable, localContext);
            truncateLogPart.setConnectionPoolName(ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);
            truncateLogPart.setType(SQLPart.STMT_INITIALIZESTATEMENTS);
            initTask.addOptionalTask(truncateLogPart);
        }

        // Create DBLink for monitoring DB
        DBConnectionDefinition monitorDef = builderModel.getConnectionDefinition(ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);
        String linkName = monitorDef.getName();

        SQLDBConnectionDefinition linkConnDef = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(monitorDef);
        linkConnDef.setDriverClass(monitorDef.getDriverClass());
        SQLPart createMonitorDbLink = new SQLPart(super.getCreateDBLinkSQL(linkConnDef, linkName), SQLPart.STMT_INITIALIZESTATEMENTS,
            ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
        initTask.addOptionalTask(createMonitorDbLink);

        // Add remote reference to table in monitoring DB from pipeline DB
        SQLPart createRemoteRefPart = new SQLPart(pipelineStmts.getCreateRemoteLogDetailsTableStatement(table, linkName).getSQL(),
            SQLPart.STMT_INITIALIZESTATEMENTS, ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
        initTask.addOptionalTask(createRemoteRefPart);

        SQLPart remountPart = new SQLPart(pipelineStmts.getRemountRemoteLogDetailsStatement(detailsTable).getSQL(),
            SQLPart.STMT_INITIALIZESTATEMENTS, ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
        initTask.addOptionalTask(remountPart);
    }

    /**
     * Overrides parent implementation to indicate that design-time form of the create
     * statement should be generated.
     *
     * @see org.netbeans.modules.etl.codegen.impl.ValidatingStrategyBuilderImpl#getCreateLogDetailsTableSQL(org.netbeans.modules.sql.framework.model.TargetTable)
     */
    protected String getCreateLogDetailsTableSQL(TargetTable table) throws BaseException {
        return pipelineStmts.getCreateLogDetailsTableStatement(table, this.builderModel.isMemoryMonitorDB()).getSQL();
    }

    protected TargetTable getLogTableFor(SQLDBTable table) {
        TargetTable clone = SQLModelObjectFactory.getInstance().createTargetTable(table);
        clone.setAliasName(table.getAliasName());
        clone.setTablePrefix(MonitorUtil.LOG_DETAILS_TABLE_PREFIX);
        return clone;
    }

    @Override
    protected String getTaskType() {
        return ETLEngine.VALIDATING;
    }

}

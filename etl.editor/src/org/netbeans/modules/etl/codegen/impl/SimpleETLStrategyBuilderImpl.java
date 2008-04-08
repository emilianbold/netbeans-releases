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

import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Strategy: If extractionRequired is true we will extract all the source table to target
 * database and then do the transformation. If extractionRequired is false we assume
 * source tables and target table are from same database so we directly jump to
 * transformation.
 * <p>
 * Note: In some cases where the source tables and target table are flat file we will also
 * jump directly transformation, but the caller has to make sure the table names are
 * unique since there might be situation where two Database was created and both have same
 * table name, in that case we just prefix the table name with database name
 * <p>
 * 
 * @author Ahimanikya Satapathy
 */
public class SimpleETLStrategyBuilderImpl extends BaseETLStrategyBuilder {

    private static final String LOG_CATEGORY = SimpleETLStrategyBuilderImpl.class.getName();
    private static final String SQL_INDENT = "";
    private static transient final Logger mLogger = Logger.getLogger(SimpleETLStrategyBuilderImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public SimpleETLStrategyBuilderImpl(ETLScriptBuilderModel model) throws BaseException {
        super(model);
    }

    /**
     * Before calling apply appropriate applyConnections
     */
    public void generateScriptForTable(ETLStrategyBuilderContext context) throws BaseException {
        mLogger.infoNoloc(mLoc.t("EDIT004: Looping through target tables:{0}", LOG_CATEGORY));
        populateInitTask(context.getInitTask(), context.getGlobalCleanUpTask(), context.getTargetTable());

        checkTargetConnectionDefinition(context);

        createTransformerTask(context);

        //RIT commented summary table and statistics related code for now       
        // Add statements to create execution summary table if it does not exist, and
        // update the assocaited execution record upon successful execution
        addCreateIfNotExistsSummaryTableStatement(context.getInitTask());
        addUpdateExecutionRecordPreparedStatement(context.getStatsUpdateTask(), context.getTargetTable());
    }

    public String getScriptToDisplay(ETLStrategyBuilderContext context) throws BaseException {
        super.checkTargetConnectionDefinition(context);
        StringBuilder buffer = new StringBuilder();
        TargetTable targetTable = context.getTargetTable();
        DB targetDB = getDBFor(context.getModel().getConnectionDefinition(targetTable));

        List sourceTables = context.getTargetTable().getSourceTableList();

        String transformSQL = getTransformerSQL(targetTable, targetDB, sourceTables, true);
        buffer.append(SQL_INDENT).append(getCommentForTransformer(targetTable)).append("\n");
        buffer.append(transformSQL);

        return buffer.toString();
    }

    protected void createTransformerTask(ETLStrategyBuilderContext context) throws BaseException {
        TargetTable targetTable = context.getTargetTable();
        DB targetDB = getDBFor(this.builderModel.getConnectionDefinition(targetTable));
        DB statsDB = DBFactory.getInstance().getDatabase(DB.AXIONDB);
        int statementType = targetTable.getStatementType();
        ETLTaskNode transformerTask = null;

        MessageManager msgMgr = MessageManager.getManager(ETLTaskNode.class);
        String displayName = msgMgr.getString("TEMPLATE_dn", msgMgr.getString("LBL_dn_transformer"), context.getTargetTable().getName());

        // TODO Need to refactor/redesign interfaces between sql-codegen and etl-codgen framework
        // such that we will avoid code like "IF ELSE" like below.
        if ((targetDB.getDBType() == DB.JDBCDB) && (targetTable.getSourceTableList().size() != 0) && ((statementType == SQLConstants.UPDATE_STATEMENT) || (statementType == SQLConstants.INSERT_UPDATE_STATEMENT))) {
            transformerTask = builderModel.getEngine().createETLTaskNode(ETLEngine.CORRELATED_QUERY_EXECUTOR);
            transformerTask.addNextETLTaskNode(ETLTask.SUCCESS, context.getNextTaskOnSuccess().getId());
            transformerTask.addNextETLTaskNode(ETLTask.EXCEPTION, context.getNextTaskOnException().getId());

            transformerTask.setDisplayName(displayName);

            if (context.getDependentTasksForNextTask().length() > 0) {
                context.getDependentTasksForNextTask().append(",");
            }
            context.getDependentTasksForNextTask().append(transformerTask.getId());

            createCorrelatedUpdateSQLParts(targetTable, transformerTask, getTargetConnName(), targetDB,
                    statsDB, (statementType == SQLConstants.INSERT_UPDATE_STATEMENT));
        } else {
            // for each target table create a transformer task node
            transformerTask = builderModel.getEngine().createETLTaskNode(ETLEngine.TRANSFORMER);
            transformerTask.addNextETLTaskNode(ETLTask.SUCCESS, context.getNextTaskOnSuccess().getId());
            transformerTask.addNextETLTaskNode(ETLTask.EXCEPTION, context.getNextTaskOnException().getId());


            transformerTask.setDisplayName(displayName);

            if (context.getDependentTasksForNextTask().length() > 0) {
                context.getDependentTasksForNextTask().append(",");
            }
            context.getDependentTasksForNextTask().append(transformerTask.getId());

            createTransformerSQLPart(targetTable, true, transformerTask, getTargetConnName(), context.getNextTaskOnException(), targetDB, statsDB);
        }

        // Reference to collector task node that immediately precedes the set
        // of transform nodes; ordinarily the extractor wait node, but could
        // be START if no extractor nodes exist.
        ETLTaskNode xformPredecessor = context.getPredecessorTask();

        // Set dependent list for predecessor to transform nodes
        xformPredecessor.addNextETLTaskNode(ETLTask.SUCCESS, transformerTask.getId());

    }

    protected StatementContext getTargetTableBaseContext() {
        StatementContext sc = new StatementContext();
        if (ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME.equals(getTargetConnName())) {
            sc.setUsingUniqueTableName(true);
        }
        return sc;
    }

    @Override
    protected boolean isExtractionRequired(SourceTable sourceTable, TargetTable targetTable) throws BaseException {
        return false;
    }
}

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
package org.netbeans.modules.etl.codegen.impl;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.codegen.PatternFinder;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.evaluators.database.DB;
import org.netbeans.modules.sql.framework.evaluators.database.DBFactory;
import org.netbeans.modules.sql.framework.evaluators.database.StatementContext;
import org.netbeans.modules.sql.framework.evaluators.database.Statements;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.AttributeMap;
import com.sun.sql.framework.utils.Logger;
import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Girish Patil
 * @version $Revision$
 */
public class StagingStrategyBuilder extends BaseETLStrategyBuilder {
    private static final String LOG_CATEGORY = StagingStrategyBuilder.class.getName();
    private static final String SQL_INDENT = "";
    private boolean forceStaging = false;

    public StagingStrategyBuilder(ETLScriptBuilderModel model) throws BaseException {
        super(model);
    }

    private String getTargetTableUserDefinedSchema(TargetTable tt)
    {
    	String schemaName = null;
    	// If User has defined Schema name use it.
        String uSchema = tt.getUserDefinedSchemaName();
        if (StringUtil.isNullString(uSchema)){
            if (!StringUtil.isNullString(tt.getSchema())){
                schemaName = tt.getSchema().toUpperCase();
            }
        }else{
            schemaName = uSchema.toUpperCase();
        }
        
        return schemaName;
    }
    /**
     * Before calling apply appropriate applyConnections
     */
    public void generateScriptForTable(ETLStrategyBuilderContext context) throws BaseException {
        Logger.print(Logger.DEBUG, LOG_CATEGORY, "In generateScriptForTable:");
        super.checkTargetConnectionDefinition(context);

        populateInitTask(context.getInitTask(), context.getGlobalCleanUpTask(), context.getTargetTable());
        MessageManager msgMgr = MessageManager.getManager(ETLTaskNode.class);
        TargetTable targetTable = context.getTargetTable();
        int statementType = targetTable.getStatementType();
        DB targetDB = getDBFor(this.builderModel.getConnectionDefinition(targetTable));
        DB statsDB = DBFactory.getInstance().getDatabase(DB.AXIONDB);

        // For each target table create a transformer task node
        ETLTaskNode transformerTask = null;
        String displayName = msgMgr.getString("TEMPLATE_dn", msgMgr.getString("LBL_dn_transformer"), context.getTargetTable().getName());

        // TODO Need to refactor/redesign interfaces between evaluators and codgen framework
        // such that we will avoid code like "IF ELSE" like below.
        if ((targetDB.getDBType() == DB.JDBCDB)
                && (targetTable.getSourceTableList().size() != 0)
                && ( (statementType == SQLConstants.UPDATE_STATEMENT)
                        || (statementType == SQLConstants.INSERT_UPDATE_STATEMENT))){
            transformerTask = builderModel.getEngine().createETLTaskNode(ETLEngine.CORRELATED_QUERY_EXECUTOR);
            transformerTask.addNextETLTaskNode(ETLTask.SUCCESS, context.getNextTaskOnSuccess().getId());
            transformerTask.addNextETLTaskNode(ETLTask.EXCEPTION, context.getNextTaskOnException().getId());
            transformerTask.setDisplayName(displayName);
            if (context.getDependentTasksForNextTask().length() > 0) {
                context.getDependentTasksForNextTask().append(",");
            }

            context.getDependentTasksForNextTask().append(transformerTask.getId());

            createCorrelatedUpdateSQLParts(targetTable,
                    transformerTask,
                    getTargetConnName(),
                    targetDB,
                    statsDB,
                    (statementType == SQLConstants.INSERT_UPDATE_STATEMENT));
        } else {
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


        ETLTaskNode xformPredecessor = null;
        List sourceTables = context.getTargetTable().getSourceTableList();
        String dropSQLStr = "";
        String truncateSql = "";

        // Loop Thru the source tables to generate
        if (sourceTables == null || sourceTables.isEmpty()) {
            // No extraction: link transformer nodes to init wait task.
            xformPredecessor = context.getPredecessorTask();
        } else {
            Iterator srcIter = sourceTables.iterator();
            // Create a wait task node for each extraction chain, so
            // that transformation will start only when all the participated tables
            // has been extracted to the temp table in the workspace database.
            ETLTaskNode extractorWait = this.builderModel.getEngine().createETLTaskNode(ETLEngine.WAIT);
            extractorWait.addNextETLTaskNode(ETLTask.SUCCESS, transformerTask.getId());
            extractorWait.addNextETLTaskNode(ETLTask.EXCEPTION, context.getNextTaskOnException().getId());
            String waitDisplayName = msgMgr.getString("TEMPLATE_dn", msgMgr.getString("LBL_dn_extractorwait"), context.getTargetTable().getName());
            extractorWait.setDisplayName(waitDisplayName);

            StringBuilder dependentTasks = new StringBuilder();
            String waitTaskId = extractorWait.getId();
            xformPredecessor = extractorWait;

            while (srcIter.hasNext()) {
                SourceTable sourceTable = (SourceTable) srcIter.next();

                // Don't create an extractor node for a source table that is in the
                // same DB as the target table.
                if (isExtractionRequired(sourceTable, targetTable)) {
                    ETLTaskNode extractorTask = createExtractorNode(sourceTable, targetDB, waitTaskId, waitTaskId, getTargetConnName(),
                    		getTargetTableUserDefinedSchema(targetTable));

                    // Add staging table to the drop list only if user specifies
                    // that it is deleteable - otherwise preserve the contents of the
                    // staging table.
                    if (sourceTable.isDropStagingTable()) {
                        StatementContext dropContext = new StatementContext();
                        dropContext.setUsingTempTableName(sourceTable, true);
                        dropContext.putClientProperty(StatementContext.IF_EXISTS, Boolean.TRUE);
                        SQLPart dropSQLPartTemp = getTargetStatements().getDropStatement(sourceTable, dropContext);
                        if (dropSQLStr.length() > 0) {
                            dropSQLStr += SQLPart.STATEMENT_SEPARATOR;
                        }
                        dropSQLStr += dropSQLPartTemp.getSQL();
                    }
                    if(sourceTable.isTruncateStagingTable()) {
                       // User has specified the "Staging Table Name" property. Use it and truncate the data.
                        StatementContext trcontext = new StatementContext();
                        trcontext.setUsingTempTableName(sourceTable, true);
                        trcontext.putClientProperty(StatementContext.IF_EXISTS, Boolean.TRUE);
                        SQLPart doTruncate = getTargetStatements().getTruncateStatement(sourceTable, trcontext);
                        if(truncateSql.length() > 0 ) {
                            truncateSql += SQLPart.STATEMENT_SEPARATOR;
                        }
                        truncateSql += doTruncate.getSQL().trim();
                        
                     }

                    context.getPredecessorTask().addNextETLTaskNode(ETLTask.SUCCESS, extractorTask.getId());
                    if (dependentTasks.length() > 0) {
                        dependentTasks.append(",");
                    }
                    dependentTasks.append(extractorTask.getId());

                    StatementContext stmtContext = new StatementContext();
                    stmtContext.setUsingFullyQualifiedTablePrefix(false);
                    stmtContext.setUsingUniqueTableName(true);
                    final String statsTableName = statsDB.getUnescapedName(statsDB.getEvaluatorFactory().evaluate(targetTable, stmtContext));
                    extractorTask.setTableName(statsTableName);
                }
            } // end extractor Loop

            // set dependent List for Level1 wait node
            extractorWait.setDependsOn(dependentTasks.toString());
        }

        // Add drop statements for temp tables, if any, to cleanup task.
        if (dropSQLStr != null && dropSQLStr.trim().length() != 0) {
            // Ensure we use same connection in dropping staging tables as we used
            // in creating them.
            SQLPart dropSQLPart = new SQLPart(dropSQLStr, SQLPart.STMT_DROP, getTargetConnName()); // NOI18N
            SQLPart etlSQLPart = dropSQLPart;
            context.getNextTaskOnException().addStatement(etlSQLPart);
        }
        
        if(truncateSql != null && truncateSql.trim().length() != 0 ) {
            SQLPart truncateSQLPart = new SQLPart(truncateSql, SQLPart.STMT_TRUNCATEBEFOREPROCESS, getTargetConnName()); // NOI18N
            context.getNextTaskOnException().addStatement(truncateSQLPart);
        }

        // Set dependent list for predecessor to transform nodes
        xformPredecessor.addNextETLTaskNode(ETLTask.SUCCESS, transformerTask.getId());

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
        if (sourceTables != null && !sourceTables.isEmpty()) {
            Iterator srcIter = sourceTables.iterator();
            while (srcIter.hasNext()) {
                SourceTable sourceTable = (SourceTable) srcIter.next();
                if (isExtractionRequired(sourceTable, targetTable)) {
                    buffer.append(getExtractorSQL(sourceTable, targetDB, context.getTargetTable()));
                    buffer.append("\n");
                }
            }
        }

        String transformSQL = getTransformerSQL(targetTable, targetDB, sourceTables, true);
        buffer.append(SQL_INDENT).append(getCommentForTransformer(targetTable)).append("\n");
        buffer.append(transformSQL);

        return buffer.toString();
    }

    public boolean isForceStaging() {
        return this.forceStaging;
    }

    public void setForceStaging(boolean forceStaging) {
        this.forceStaging = forceStaging;
    }

    protected boolean isExtractionRequired(SourceTable sourceTable, TargetTable targetTable) throws BaseException {
        if (forceStaging) {
            return true;
        } else {
            return (!PatternFinder.isFromSameDB(sourceTable, targetTable, this.builderModel));
        }
    }

    private ETLTaskNode createExtractorNode(SourceTable srcTable, DB targetDB, String waitTaskId, String cleanupTaskId, String trgtConnName,
            String trgtSchema) throws BaseException {
        DBConnectionDefinition srcConDef = this.builderModel.getConnectionDefinition(srcTable);
        String srcConnName = srcConDef.getName();

        // reset the evaluator for the source table dbType
        DB sourceDB = getDBFor(srcConDef);
        final Statements sourceStmts = sourceDB.getStatements();
        final Statements targetStmts = targetDB.getStatements();

        // for each source table create a extractor task node
        ETLTaskNode extractorTask = this.builderModel.getEngine().createETLTaskNode(ETLEngine.EXTRACTOR);
        extractorTask.addNextETLTaskNode(ETLTask.SUCCESS, waitTaskId);
        extractorTask.addNextETLTaskNode(ETLTask.EXCEPTION, cleanupTaskId);

        MessageManager msgMgr = MessageManager.getManager(ETLTaskNode.class);
        String displayName = msgMgr.getString("TEMPLATE_dn", msgMgr.getString("LBL_dn_extractor"), srcTable.getName());
        extractorTask.setDisplayName(displayName);
        
        // RFE-102428
        String stgTableName = srcTable.getStagingTableName();
        if(stgTableName == null || stgTableName.trim().length() == 0) {
        	// User has not specified the "Staging Table Name", proceed with default logic
        	if (srcTable.isDropStagingTable()) {
            StatementContext context = new StatementContext();
            context.setUsingTempTableName(srcTable, true);
            context.putClientProperty("targetSchema", trgtSchema);

            SQLPart tableExistsPart = targetStmts.getTableExistsStatement(srcTable, context);
            tableExistsPart.setConnectionPoolName(trgtConnName);
            extractorTask.addStatement(tableExistsPart);

            // Drop if exists statement for temp table
            context.setUsingTempTableName(srcTable, true);
            context.putClientProperty("ifExists", Boolean.TRUE);

            SQLPart dropSQLPart = targetStmts.getDropStatement(srcTable, context);
            dropSQLPart.setConnectionPoolName(trgtConnName);
            extractorTask.addStatement(dropSQLPart);
        }

        // Create temp table in target database
        StatementContext context = new StatementContext();
        context.setUsingTempTableName(srcTable, true);
        SQLPart createSQLPart = targetStmts.getCreateStatement(srcTable, context);
        createSQLPart.setConnectionPoolName(trgtConnName);
        extractorTask.addStatement(createSQLPart);
        }
        
        if(srcTable.isTruncateStagingTable()){
        	// User has specified the "Staging Table Name" property. Use it and truncate the data.
        	StatementContext trcontext = new StatementContext();
        	trcontext.setUsingTempTableName(srcTable, true);
        	trcontext.putClientProperty("targetSchema", trgtSchema);
        	truncateTableIfExists(srcTable, extractorTask, trgtConnName, targetStmts, trcontext);
        }

        // Select extraction set from source
        StatementContext context = new StatementContext();
        context.setUsingTempTableName(srcTable, false);        
        this.useUniqueNameIfRequired(srcTable, context);        
        SQLPart selectSQLPart = sourceStmts.getSelectStatement(srcTable, context);
        selectSQLPart.setConnectionPoolName(srcConnName);
        extractorTask.addStatement(selectSQLPart);

        // Create insert prepared statement.
        context.setUsingTempTableName(srcTable, true);
        context.setUsingUniqueTableName(srcTable, false);        
        SQLPart insertSQLPart = targetStmts.getPreparedInsertStatement(srcTable, context);
        insertSQLPart.setConnectionPoolName(trgtConnName);
        extractorTask.addStatement(insertSQLPart);

        AttributeMap attrMap = new AttributeMap();
        attrMap.put("batchSize", srcTable.getBatchSize() + ""); // NOI18N
        extractorTask.setAttributeMap(attrMap);

        return extractorTask;
    }

    private String getExtractorSQL(SourceTable srcTable, DB targetDB, TargetTable tt) throws BaseException {
        StringBuilder buffer = new StringBuilder(50);

        // Get the evaluator
        DBConnectionDefinition srcConDefn = this.builderModel.getConnectionDefinition(srcTable);
        DB sourceDB = getDBFor(srcConDefn);

        String modelName = srcTable.getParent().getModelName();
        String msg = "";

        // Select statement for source table
        msg = MSG_MGR.getString("DISPLAY_SELECT", srcTable.getName(), modelName, srcConDefn.getDBType());
        buffer.append(SQL_INDENT).append(msg).append("\n");

        StatementContext context = new StatementContext();
        SQLPart selectSQLPart = sourceDB.getStatements().getSelectStatement(srcTable, context);

        buffer.append(selectSQLPart.getSQL());
        buffer.append("\n\n");

        // Create statement for temp table
        msg = MSG_MGR.getString("DISPLAY_CREATE", modelName, this.builderModel.getConnectionDefinition(tt).getDBType());
        buffer.append(SQL_INDENT).append(msg).append("\n");

        context = new StatementContext();
        context.setUsingTempTableName(srcTable, true);
        SQLPart createSQLPart = targetDB.getStatements().getCreateStatement(srcTable, context);
        buffer.append(createSQLPart.getSQL());
        buffer.append("\n\n");

        // insert statement to store extracted rows.
        buffer.append(SQL_INDENT).append(MSG_MGR.getString("DISPLAY_INSERT_TEMP")).append("\n");
        SQLPart insertSQLPart = targetDB.getStatements().getPreparedInsertStatement(srcTable, context);
        buffer.append(insertSQLPart.getSQL());
        buffer.append("\n\n");

        return buffer.toString();
    }

}

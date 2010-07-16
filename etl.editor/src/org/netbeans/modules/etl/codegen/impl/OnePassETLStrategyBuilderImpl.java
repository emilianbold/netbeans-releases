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
import org.netbeans.modules.etl.codegen.PatternFinder;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.etl.engine.impl.Extractor;
import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.DBConstants;
import com.sun.etl.jdbc.SQLPart;
import com.sun.etl.utils.AttributeMap;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

/**
 * If all source table are from same DB and statement type is Insert
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class OnePassETLStrategyBuilderImpl extends BaseETLStrategyBuilder {

    private static final String LOG_CATEGORY = OnePassETLStrategyBuilderImpl.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(OnePassETLStrategyBuilderImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public OnePassETLStrategyBuilderImpl(ETLScriptBuilderModel model) throws BaseException {
        super(model);
    }

    public void createTransformerTask(ETLStrategyBuilderContext context) throws BaseException {
        DBConnectionDefinition targetConnDef = context.getModel().getConnectionDefinition(context.getTargetTable());
        DB targetDB = this.getDBFor(targetConnDef);
        MessageManager msgMgr = MessageManager.getManager(ETLTaskNode.class);
        ETLTaskNode onePassETLNode = context.getModel().getEngine().createETLTaskNode(ETLEngine.EXTRACTOR);
        String displayName = msgMgr.getString("TEMPLATE_dn", msgMgr.getString("LBL_dn_onepass"), context.getTargetTable().getName());
        onePassETLNode.setDisplayName(displayName);

        context.getPredecessorTask().addNextETLTaskNode(ETLTask.SUCCESS, onePassETLNode.getId());

        TargetTable tgtTable = context.getTargetTable();
        StatementContext tgtStmtContext = new StatementContext();
        if ((this.builderModel.isConnectionDefinitionOverridesApplied()) && (PatternFinder.isInternalDBTable(tgtTable))) {
            tgtStmtContext.setUsingUniqueTableName(tgtTable, true);
        }
        createTargetTableIfNotExists(context.getTargetTable(), onePassETLNode, targetConnDef.getName(), targetDB, tgtStmtContext);
        truncateTargetTableIfExists(context.getTargetTable(), onePassETLNode, targetConnDef.getName(), targetDB.getStatements(), tgtStmtContext);


        List sourceTables = context.getTargetTable().getSourceTableList();
        if (sourceTables != null && !sourceTables.isEmpty()) {
            StatementContext stmtContext = new StatementContext();
            SourceTable srcTable = (SourceTable) sourceTables.get(0);
            DBConnectionDefinition srcConDef = context.getModel().getConnectionDefinition(srcTable);
            String sourceConnName = getConDefnName(srcConDef);
            int dbType = connFactory.getDatabaseVersion(BaseETLStrategyBuilder.getConnectionPropertiesFrom(srcConDef));

            DB db = DBFactory.getInstance().getDatabase(dbType);

            if ((this.builderModel.isConnectionDefinitionOverridesApplied()) && (dbType == DBConstants.AXION)) {
                stmtContext.setUsingUniqueTableName(true);
            }

            SQLPart selectPart = db.getStatements().getOnePassSelectStatement(tgtTable, stmtContext);
            stmtContext.setUsingUniqueTableName(false);
            selectPart.setConnectionPoolName(sourceConnName);
            onePassETLNode.addStatement(selectPart);

            // Insert new execution record for target table, tt, and add query to obtain
            // execution id assigned to new execution record.
            super.addInsertNewExecutionRecordStatement(onePassETLNode, tgtTable);
            super.addSelectExecutionIdForNewExecutionRecordStatement(onePassETLNode, tgtTable);

            DB statsDb = DBFactory.getInstance().getDatabase(DB.AXIONDB);
            stmtContext.setUsingFullyQualifiedTablePrefix(false);
            stmtContext.setUsingUniqueTableName(true);
            String statsTable = statsDb.getUnescapedName(statsDb.getGeneratorFactory().generate(tgtTable, stmtContext));
            onePassETLNode.setTableName(statsTable); //NOI18N
        } else {
            throw new BaseException("Must have source table for One-Pass eTL Strategy.");
        }

        SQLPart insertPart = targetDB.getStatements().getPreparedInsertStatement(tgtTable, tgtStmtContext);
        insertPart.setConnectionPoolName(getTargetConnName());
        onePassETLNode.addStatement(insertPart);

        onePassETLNode.addNextETLTaskNode(ETLTask.SUCCESS, context.getNextTaskOnSuccess().getId());
        onePassETLNode.addNextETLTaskNode(ETLTask.EXCEPTION, context.getNextTaskOnException().getId());

        if (context.getDependentTasksForNextTask().length() > 0) {
            context.getDependentTasksForNextTask().append(",");
        }
        context.getDependentTasksForNextTask().append(onePassETLNode.getId());

        AttributeMap attrMap = new AttributeMap();
        attrMap.put("batchSize", context.getTargetTable().getBatchSize() + ""); //NOI18N

        // Set flag in extractor indicating it is operating in one-pass mode.
        attrMap.put(Extractor.KEY_ISONEPASS, Boolean.TRUE);

        onePassETLNode.setAttributeMap(attrMap);
    }

    /**
     * Before calling apply appropriate applyConnections
     */
    public void generateScriptForTable(ETLStrategyBuilderContext context) throws BaseException {
        mLogger.infoNoloc(mLoc.t("EDIT004: Looping through target tables:{0}", LOG_CATEGORY));
        populateInitTask(context.getInitTask(), context.getGlobalCleanUpTask(), context.getTargetTable());

        // Create cleanup task for this execution thread.
        ETLTaskNode threadCleanupTask = context.getNextTaskOnException();

        // Create commit node to collect transformer connections and commit/close
        // them.
        ETLTaskNode commitTask = context.getNextTaskOnSuccess();

        checkTargetConnectionDefinition(context);

        createTransformerTask(context);

        // Add statements to create execution summary table if it does not exist, and
        // update the associated execution record upon successful execution
        addCreateIfNotExistsSummaryTableStatement(context.getInitTask());
        addUpdateExecutionRecordPreparedStatement(context.getStatsUpdateTask(), context.getTargetTable());

        // --------------------------------------------------------------------
        commitTask.addNextETLTaskNode(ETLTask.SUCCESS, threadCleanupTask.getId());
        commitTask.addNextETLTaskNode(ETLTask.EXCEPTION, threadCleanupTask.getId());
    }

    public String getScriptToDisplay(ETLStrategyBuilderContext context) throws BaseException {
        super.checkTargetConnectionDefinition(context);
        StringBuilder buffer = new StringBuilder();

        TargetTable tgtTable = context.getTargetTable();
        List sourceTables = tgtTable.getSourceTableList();

        // generate one-pass select part.
        if (sourceTables != null && !sourceTables.isEmpty()) {
            SourceTable srcTable = (SourceTable) sourceTables.get(0);
            DBConnectionDefinition srcConDefn = this.builderModel.getConnectionDefinition(srcTable);

            String modelName = srcTable.getParent().getModelName();
            buffer.append(MSG_MGR.getString("DISPLAY_SELECT", srcTable.getName(), modelName, srcConDefn.getDBType())).append("\n");

            int dbType = connFactory.getDatabaseVersion(BaseETLStrategyBuilder.getConnectionPropertiesFrom(srcConDefn));
            DB db = DBFactory.getInstance().getDatabase(dbType);
            StatementContext stmtContext = new StatementContext();
            SQLPart selectPart = db.getStatements().getOnePassSelectStatement(tgtTable, stmtContext);

            buffer.append(selectPart.getSQL());
            buffer.append("\n\n");
        }

        // generate one-pass insert/load part.
        StatementContext stmtContext = new StatementContext();
        SQLPart insertPart = getStatementsForTargetTableDB(context).getPreparedInsertStatement(tgtTable, stmtContext);

        if (insertPart != null) {
            buffer.append(getCommentForTransformer(tgtTable)).append("\n");
            buffer.append(insertPart.getSQL());
            buffer.append("\n\n");
        }
        return buffer.toString();
    }
}

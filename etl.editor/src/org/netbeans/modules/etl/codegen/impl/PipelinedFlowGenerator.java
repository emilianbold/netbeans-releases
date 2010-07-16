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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilder;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.DBConstants;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

/**
 * Builds ETL Process Flow and delegates to appropriate ETLStrategyBuilder implementation
 * as required
 *
 * @author Ahimanikya Satapathy
 * @author Jonathan Giron
 * @version $Revision$
 */
public class PipelinedFlowGenerator extends BaseFlowGenerator {

    private static final String LOG_CATEGORY = PipelinedFlowGenerator.class.getName();
    private static final MessageManager MSG_MGR = MessageManager.getManager(ETLTaskNode.class);
    protected PipelinedStrategyBuilderImpl pipelinedBuilder;
    private static transient final Logger mLogger = Logger.getLogger(PipelinedFlowGenerator.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public PipelinedFlowGenerator(SQLDefinition sqlD) throws BaseException {
        super(sqlD);
        this.builderModel.setUseInstanceDB(true);
    }

    @Override
    public void applyConnectionDefinitions() throws BaseException {
        super.applyConnectionDefinitions();
    }

    @Override
    public void applyConnectionDefinitions(Map name2connectionDefMap, Map connDefNameMap, Map intDbConfigParams) throws BaseException {
        this.builderModel.setUseInstanceDB(true);
        this.builderModel.setShutdownMonitorDB(true);
        super.applyConnectionDefinitions(name2connectionDefMap, connDefNameMap, intDbConfigParams);
    }

    public ETLEngine getScript() throws BaseException {
        mLogger.infoNoloc(mLoc.t("EDIT003: In getScript(){0}", LOG_CATEGORY));
        generateScript();
        return builderModel.getEngine();
    }

    protected void createWarapperTask() throws BaseException {
        final MessageManager dnLabelMgr = MessageManager.getManager(ETLTaskNode.class);

        if (pipelinedBuilder != null) {
            List targetTables = this.builderModel.getSqlDefinition().getTargetTables();
            this.initTask = pipelinedBuilder.buildInitTask(targetTables);
            this.initTask.setDisplayName(dnLabelMgr.getString("LBL_dn_init"));
            this.globalCleanupTask = pipelinedBuilder.buildCleanupTask(targetTables);
            this.globalCleanupTask.setDisplayName(dnLabelMgr.getString("LBL_dn_cleanup"));
        }

        this.threadCollectorWaitNode = this.builderModel.getEngine().createETLTaskNode(ETLEngine.WAIT);

        this.statsUpdateTask = this.builderModel.getEngine().createETLTaskNode(ETLEngine.UPDATE_STATS);
        this.statsUpdateTask.setDisplayName(MSG_MGR.getString("LBL_dn_updatestats"));
    }

    protected void generateScript() throws BaseException {
        pipelinedBuilder = getBuilder();
        List dependencies = new ArrayList();
        createWarapperTask();

        // get target table List
        List targetTables = builderModel.getSqlDefinition().getTargetTables();
        if (targetTables == null || targetTables.size() == 0) {
            throw new BaseException("Invalid eTL Collaboration: No target table defined.");
        }

        ETLStrategyBuilderContext context = new ETLStrategyBuilderContext(initTask, globalCleanupTask, this.statsUpdateTask, this.builderModel);

        ETLTaskNode pipelineTask = null;
        // Iterate through the target tables to generate pipeline tasks.
        Iterator it = targetTables.iterator();
        while (it.hasNext()) {
            mLogger.infoNoloc(mLoc.t("EDIT004: Looping through target tables:{0}", LOG_CATEGORY));
            TargetTable tt = (TargetTable) it.next();

            context.setTargetTable(tt);
            context.setPredecessorTask(initTask);
            context.setNextTaskOnSucess(threadCollectorWaitNode);
            context.setNextTaskOnException(statsUpdateTask);

            pipelinedBuilder.generateScriptForTable(context);
            pipelineTask = context.getLastPipelinedTask();
            dependencies.add(pipelineTask.getId());
        } // end transformer Loop

        // Create commit node to collect transformer connections and
        // commit/close them.
        ETLTaskNode commitTask = this.builderModel.getEngine().createETLTaskNode(ETLEngine.COMMIT);
        commitTask.setDisplayName(MSG_MGR.getString("LBL_dn_commit"));

        // set dependent list for wait node
        this.threadCollectorWaitNode.setDependsOn(StringUtil.createDelimitedStringFrom(dependencies));

        // Complete task net by linking nodes.
        this.startTask.addNextETLTaskNode(ETLTask.SUCCESS, this.initTask.getId());
        this.initTask.addNextETLTaskNode(ETLTask.EXCEPTION, this.globalCleanupTask.getId());

        //
        // Commit data first, then update statistics.
        //
        this.threadCollectorWaitNode.addNextETLTaskNode(ETLTask.SUCCESS, commitTask.getId());

        commitTask.addNextETLTaskNode(ETLTask.SUCCESS, this.statsUpdateTask.getId());
        commitTask.addNextETLTaskNode(ETLTask.EXCEPTION, this.statsUpdateTask.getId());

        statsUpdateTask.addNextETLTaskNode(ETLTask.SUCCESS, globalCleanupTask.getId());
        statsUpdateTask.addNextETLTaskNode(ETLTask.EXCEPTION, globalCleanupTask.getId());

        globalCleanupTask.addNextETLTaskNode(ETLTask.SUCCESS, endTask.getId());
        globalCleanupTask.addNextETLTaskNode(ETLTask.EXCEPTION, endTask.getId());

        // this to generate the engine xml and save for debugging use
        this.builderModel.getEngine().toXMLString();
    }

    protected ETLStrategyBuilder getTargetTableScriptBuilder() throws BaseException {
        return ETLProcessFlowGeneratorFactory.getPipelinedTargetTableScriptBuilder(builderModel);
    }

    /**
     * Indicates whether the given table must be accessed by the pipeline database via a
     * dblink/remote table combination.
     *
     * @param table SQLDBTable instance to test
     * @return true if <code>table</code> should be accessed via remote table, false
     *         otherwise
     */
    protected boolean requiresRemoteAccess(SQLDBTable table) {
        // If table is not an Axion table, create an external remote table if it doesn't
        // already exist. We handle Axion flatfiles in buildFlatfileSQLParts(), though
        // we will create a log table for target tables, regardless of DB type.
        boolean ret = true;

        if (this.builderModel.isConnectionDefinitionOverridesApplied()) {
            DBConnectionDefinition connDef = table.getParent().getConnectionDefinition();
            ret = SQLUtils.getSupportedDBType(connDef.getDBType()) != DBConstants.AXION;
        }

        return ret;

    }

    private PipelinedStrategyBuilderImpl getBuilder() throws BaseException {
        if (this.builderModel.getSqlDefinition().hasValidationConditions()) {
            return ETLProcessFlowGeneratorFactory.getValidatingTargetTableScriptBuilder(builderModel);
        } else {
            return ETLProcessFlowGeneratorFactory.getPipelinedTargetTableScriptBuilder(builderModel);
        }
    }
}

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

import org.netbeans.modules.etl.codegen.ETLStrategyBuilder;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.codegen.PatternFinder;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;
import net.java.hulp.i18n.Logger;
import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.etl.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Builds ETLProcess Flow and delegate to appropriate ETLStrategy Builder as required
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class DefaultProcessFlowGenerator extends BaseFlowGenerator {

    private static final String LOG_CATEGORY = DefaultProcessFlowGenerator.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(DefaultProcessFlowGenerator.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private StringBuilder dependentsForThreadCollector = new StringBuilder();

    public DefaultProcessFlowGenerator(SQLDefinition sqlD) throws BaseException {
        super(sqlD);
        this.builderModel.setUseInstanceDB(false);
    }

    /**
     * Called during Test run codegen.
     */
    public ETLEngine getScript() throws BaseException {
        mLogger.infoNoloc(mLoc.t("EDIT003: In getScript(){0}", LOG_CATEGORY));
        generateScript();
        return builderModel.getEngine();
    }

    private void generateScript() throws BaseException {
        final MessageManager dnLabelMgr = MessageManager.getManager(ETLTaskNode.class);
        createInitTask();

        // get target table List
        List targetTables = builderModel.getSqlDefinition().getTargetTables();
        if (targetTables == null || targetTables.size() == 0) {
            throw new BaseException("Invalid eTL Collaboration: No target table defined.");
        }

        // Loop Thru the target tables to generate:
        ETLTaskNode commitTask = null;
        ETLTaskNode threadCleanupTask = null;
        List cleanupNodes = new ArrayList();
        Iterator it = targetTables.iterator();

        ETLStrategyBuilderContext context = new ETLStrategyBuilderContext(initTask, globalCleanupTask, this.statsUpdateTask, this.builderModel);

        while (it.hasNext()) {
            mLogger.infoNoloc(mLoc.t("EDIT004: Looping through target tables:{0}", LOG_CATEGORY));
            TargetTable tt = (TargetTable) it.next();
            // Create commit node to collect transformer connections and commit/close
            // them.

            commitTask = this.builderModel.getEngine().createETLTaskNode(ETLEngine.COMMIT);
            commitTask.setDisplayName(dnLabelMgr.getString("TEMPLATE_dn", dnLabelMgr.getString("LBL_dn_commit"), tt.getName()));

            threadCleanupTask = this.builderModel.getEngine().createETLTaskNode(ETLEngine.CLEANUP);
            threadCleanupTask.setDisplayName(dnLabelMgr.getString("TEMPLATE_dn", dnLabelMgr.getString("LBL_dn_cleanup"), tt.getName()));
            cleanupNodes.add(threadCleanupTask);

            ETLStrategyBuilder tableScriptBuilder = PatternFinder.createETLStrategyBuilder(tt, builderModel);
            context.setTargetTable(tt);
            context.setPredecessorTask(initTask);
            context.setNextTaskOnSucess(commitTask);
            context.setNextTaskOnException(threadCleanupTask);

            commitTask.addNextETLTaskNode(ETLTask.SUCCESS, threadCleanupTask.getId());
            commitTask.addNextETLTaskNode(ETLTask.EXCEPTION, threadCleanupTask.getId());

            if (dependentsForThreadCollector.length() != 0) {
                dependentsForThreadCollector.append(",");
            }
            dependentsForThreadCollector.append(threadCleanupTask.getId());

            tableScriptBuilder.generateScriptForTable(context);

        } // end transformer Loop

        // set dependent list for collector wait node
        Iterator iter = cleanupNodes.iterator();
        while (iter.hasNext()) {
            threadCleanupTask = (ETLTaskNode) iter.next();
            threadCleanupTask.addNextETLTaskNode(ETLTask.SUCCESS, threadCollectorWaitNode.getId());
            threadCleanupTask.addNextETLTaskNode(ETLTask.EXCEPTION, threadCollectorWaitNode.getId());
        }

        threadCollectorWaitNode.setDependsOn(dependentsForThreadCollector.toString());
        threadCollectorWaitNode.addNextETLTaskNode(ETLTask.SUCCESS, statsUpdateTask.getId());
        threadCollectorWaitNode.addNextETLTaskNode(ETLTask.EXCEPTION, statsUpdateTask.getId());
        String waitDisplayName = dnLabelMgr.getString("LBL_dn_transformerwait");
        threadCollectorWaitNode.setDisplayName(waitDisplayName);

        statsUpdateTask.addNextETLTaskNode(ETLTask.SUCCESS, globalCleanupTask.getId());
        statsUpdateTask.addNextETLTaskNode(ETLTask.EXCEPTION, globalCleanupTask.getId());

        globalCleanupTask.addNextETLTaskNode(ETLTask.SUCCESS, this.endTask.getId());
        globalCleanupTask.addNextETLTaskNode(ETLTask.EXCEPTION, this.endTask.getId());

        // this to generate the engine xml and save for debugging use
        this.builderModel.getEngine().toXMLString();
    }
}

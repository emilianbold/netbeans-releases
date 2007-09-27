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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.etl.codegen.ETLStrategyBuilder;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.codegen.PatternFinder;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * Builds ETLProcess Flow and delegate to appropriate ETLStrategy Builder as required
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class DefaultProcessFlowGenerator extends BaseFlowGenerator {
    private static final String LOG_CATEGORY = DefaultProcessFlowGenerator.class.getName();

    private StringBuilder dependentsForThreadCollector = new StringBuilder();

    public DefaultProcessFlowGenerator(SQLDefinition sqlD) throws BaseException {
        super(sqlD);
        this.builderModel.setUseInstanceDB(false);
    }

    /**
     * Called during Test run codegen.
     */
    public ETLEngine getScript() throws BaseException {
        Logger.print(Logger.DEBUG, LOG_CATEGORY, "In getScript()");
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
            Logger.print(Logger.DEBUG, LOG_CATEGORY, "Looping through target tables: ");
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

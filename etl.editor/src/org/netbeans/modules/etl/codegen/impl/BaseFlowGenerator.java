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

import java.util.Map;

import org.netbeans.modules.etl.codegen.ETLProcessFlowGenerator;
import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.sql.framework.model.SQLDefinition;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLEngineContext;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.Logger;

/**
 * @author Girish Patil
 * @version $Revision$
 */
public abstract class BaseFlowGenerator implements ETLProcessFlowGenerator {
    private static final String LOG_CATEGORY = BaseFlowGenerator.class.getName();
    protected ETLScriptBuilderModel builderModel = new ETLScriptBuilderModel();
    protected ETLTaskNode endTask = null;
    protected ETLTaskNode globalCleanupTask = null;
    protected ETLTaskNode initTask = null;
    protected ETLTaskNode startTask = null;
    protected ETLTaskNode statsUpdateTask = null;
    protected ETLTaskNode threadCollectorWaitNode = null;

    public BaseFlowGenerator(SQLDefinition sqlD) throws BaseException {
        init(sqlD);
    }

    public void applyConnectionDefinitions() throws BaseException {
        this.builderModel.applyConnectionDefinitions();
        this.builderModel.buildRuntimeDatabaseModel();
    }

    public void applyConnectionDefinitions(Map name2connectionDefMap, Map otdOid2ConnDefNameMap, Map intDbConfigParams) throws BaseException {
        if ((intDbConfigParams != null) && (intDbConfigParams.size() > 0)) {
            this.builderModel.setUseInstanceDB(true);
        }
        this.builderModel.setMemoryMonitorDB(false);
        //this.builderModel.setShutdownMonitorDB(true); //103130
        this.builderModel.applyConnectionDefinitions(name2connectionDefMap, otdOid2ConnDefNameMap, intDbConfigParams);
        this.builderModel.buildRuntimeDatabaseModel();
    }

    public String getInstanceDBFolder() {
        return this.builderModel.getInstanceDBFolder();
    }

    public String getInstanceDBName() {
        return this.builderModel.getInstanceDBName();
    }

    public String getMonitorDBFolder() {
        return this.builderModel.getMonitorDBFolder();
    }

    public String getMonitorDBName() {
        return this.builderModel.getMonitorDBName();
    }

    public String getWorkingFolder() {
        return this.builderModel.getWorkingFolder();
    }

    public void setInstanceDBFolder(String instanceDBFolder) {
        this.builderModel.setInstanceDBFolder(instanceDBFolder);
    }

    public void setInstanceDBName(String instanceDBName) {
        this.builderModel.setInstanceDBName(instanceDBName);
    }

    public void setMonitorDBFolder(String monitorDBFolder) {
        this.builderModel.setMonitorDBFolder(monitorDBFolder);
    }

    public void setMonitorDBName(String monitorDBName) {
        this.builderModel.setMonitorDBName(monitorDBName);
    }

    public void setWorkingFolder(String workingFolder) {
        this.builderModel.setWorkingFolder(workingFolder);
    }

    protected void createInitTask() throws BaseException {
        final MessageManager dnLabelMgr = MessageManager.getManager(ETLTaskNode.class);
        Logger.print(Logger.DEBUG, LOG_CATEGORY, "createInitTask(): ");
        // START task
        startTask = builderModel.getEngine().getStartETLTaskNode();

        // INIT task
        initTask = builderModel.getEngine().createETLTaskNode(ETLEngine.INIT);
        initTask.setDisplayName(dnLabelMgr.getString("LBL_dn_init"));

        startTask.addNextETLTaskNode(ETLTask.SUCCESS, initTask.getId());

        // Global CLEANUP task
        globalCleanupTask = builderModel.getEngine().createETLTaskNode(ETLEngine.CLEANUP);
        globalCleanupTask.setDisplayName(dnLabelMgr.getString("LBL_dn_cleanup"));

        // WT #67938: Ensure cleanup is called if init task chokes.
        initTask.addNextETLTaskNode(ETLTask.EXCEPTION, globalCleanupTask.getId());

        SQLPart shutdownDB = null;

        // Always shut down instance DB
        if (this.builderModel.isUseInstanceDB()) {
            shutdownDB = new SQLPart("SHUTDOWN", SQLPart.STMT_DEFRAG, ETLScriptBuilderModel.ETL_INSTANCE_DB_CONN_DEF_NAME);
            globalCleanupTask.addStatement(shutdownDB);
        }

		/**  HotFix #103130 : 
		//  Commented to resolve problem that occur while mulitple threads attempt to modify Summary table in mutlithread environment.
		//   if (this.builderModel.isShutdownMonitorDB()) {
		//       shutdownDB = new SQLPart("SHUTDOWN", SQLPart.STMT_DEFRAG, ETLScriptBuilderModel.ETL_MONITOR_DB_CONN_DEF_NAME);
		//       globalCleanupTask.addOptionalTask(shutdownDB);
		//   }
		*/

        // Final WAIT task
        // Create a wait task node to collect thread of each transformer chain.
        threadCollectorWaitNode = builderModel.getEngine().createETLTaskNode(ETLEngine.WAIT);

        // UPDATE_STATS Task
        // Create update statistics node.
        statsUpdateTask = builderModel.getEngine().createETLTaskNode(ETLEngine.UPDATE_STATS);
        statsUpdateTask.setDisplayName(dnLabelMgr.getString("LBL_dn_updatestats"));
    }

    protected void init(SQLDefinition sqlD) throws BaseException {
        if (sqlD == null) {
            throw new BaseException("SQLDefinition is null");
        }
        builderModel.setSqlDefinition(sqlD);
        initEngine();
    }

    protected void initEngine() {
        Logger.print(Logger.DEBUG, LOG_CATEGORY, "initEngine(): ");
        // Create an empty engine.
        ETLEngineContext engineContext = new ETLEngineContext();
        ETLEngine engine = this.builderModel.getEngine();
        engine.setContext(engineContext);
        engine.createStartETLTaskNode();
        engine.createEndETLTaskNode();
        engine.setDisplayName(builderModel.getSqlDefinition().getDisplayName());

        startTask = engine.getStartETLTaskNode();
        endTask = engine.getEndETLTaskNode();

        MessageManager dnLabelMgr = MessageManager.getManager(ETLTaskNode.class);
        startTask.setDisplayName(dnLabelMgr.getString("LBL_dn_start"));
        endTask.setDisplayName(dnLabelMgr.getString("LBL_dn_end"));
    }
    public void applyConnectionDefinitions(boolean isMemoryDb) throws BaseException {
    	this.builderModel.setMemoryMonitorDB(false);
    	applyConnectionDefinitions();
    }

}

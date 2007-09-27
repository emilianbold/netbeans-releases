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
package org.netbeans.modules.etl.codegen;

import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;

/**
 * This will be use as context to be passed from ETLProcessFlowGenerator to various
 * ETLStrategyBuilder.
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class ETLStrategyBuilderContext {

    private StringBuilder dependentTasksForNextTask = new StringBuilder();
    private ETLTaskNode globalCleanUpTask;

    private ETLTaskNode initTask;
    private ETLTaskNode lastPipelinedTask;

    private ETLScriptBuilderModel model;

    private ETLTaskNode nextTaskOnException;
    private ETLTaskNode nextTaskOnSucess;
    private ETLTaskNode predecessorTask;
    private ETLTaskNode statsUpdateTask;

    private TargetTable targetTable;

    /**
     * @param initTask
     * @param globalCleanupTask
     * @param statsUpdateTask
     * @param model
     */
    public ETLStrategyBuilderContext(ETLTaskNode initTask, ETLTaskNode globalCleanupTask, ETLTaskNode statsUpdateTask, ETLScriptBuilderModel model) {
        this.initTask = initTask;
        this.globalCleanUpTask = globalCleanupTask;
        this.statsUpdateTask = statsUpdateTask;
        this.model = model;
    }

    /**
     * Used to generate SQL to be shown in ShowSQL.
     *
     * @param sqlDefinition
     * @param targetTable
     * @param model
     * @throws BaseException
     */
    public ETLStrategyBuilderContext(SQLDefinition sqlDefinition, TargetTable targetTable) throws BaseException {
        this.model = new ETLScriptBuilderModel();
        this.model.setSqlDefinition(sqlDefinition);
        this.model.applyConnectionDefinitions();
        this.model.setConnectionDefinitionOverridesApplied(true);
        this.targetTable = targetTable;
    }

    /**
     * @return Returns the dependentTasksForNextTask.
     */
    public StringBuilder getDependentTasksForNextTask() {
        return dependentTasksForNextTask;
    }

    public ETLTaskNode getGlobalCleanUpTask() {
        return this.globalCleanUpTask;
    }

    public ETLTaskNode getInitTask() {
        return this.initTask;
    }

    public ETLTaskNode getLastPipelinedTask() {
        return this.lastPipelinedTask;
    }

    /**
     * @return Returns the generator.
     */
    public ETLScriptBuilderModel getModel() {
        return model;
    }

    /**
     * @return Returns the nextTaskOnException.
     */
    public ETLTaskNode getNextTaskOnException() {
        return nextTaskOnException;
    }

    /**
     * @return Returns the nextTaskOnSucess.
     */
    public ETLTaskNode getNextTaskOnSuccess() {
        return nextTaskOnSucess;
    }

    public ETLTaskNode getNextTaskOnSucess() {
        return this.nextTaskOnSucess;
    }

    /**
     * @return Returns the predecessorTask.
     */
    public ETLTaskNode getPredecessorTask() {
        return predecessorTask;
    }

    public ETLTaskNode getStatsUpdateTask() {
        return this.statsUpdateTask;
    }

    public TargetTable getTargetTable() {
        return targetTable;
    }

    public void setDependentTasksForNextTask(StringBuilder dependentTasksForNextTask) {
        this.dependentTasksForNextTask = dependentTasksForNextTask;
    }

    public void setGlobalCleanUpTask(ETLTaskNode globalCleanUpTask) {
        this.globalCleanUpTask = globalCleanUpTask;
    }

    public void setInitTask(ETLTaskNode initTask) {
        this.initTask = initTask;
    }

    public void setLastPipelinedTask(ETLTaskNode lastPipelinedTask) {
        this.lastPipelinedTask = lastPipelinedTask;
    }

    public void setNextTaskOnException(ETLTaskNode nextTaskOnException) {
        this.nextTaskOnException = nextTaskOnException;
    }

    public void setNextTaskOnSucess(ETLTaskNode nextTaskOnSucess) {
        this.nextTaskOnSucess = nextTaskOnSucess;
    }

    public void setPredecessorTask(ETLTaskNode predecessorTask) {
        this.predecessorTask = predecessorTask;
    }

    public void setStatsUpdateTask(ETLTaskNode statsUpdateTask) {
        this.statsUpdateTask = statsUpdateTask;
    }

    public void setTargetTable(TargetTable tt) {
        this.targetTable = tt;
    }
}

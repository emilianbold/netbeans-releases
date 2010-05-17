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
package org.netbeans.modules.etl.codegen;

import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.etl.exception.BaseException;

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

    public void setTargetTable(TargetTable targetTable) {
        this.targetTable = targetTable;
    }
}
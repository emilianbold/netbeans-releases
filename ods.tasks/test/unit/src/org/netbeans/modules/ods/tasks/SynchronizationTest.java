/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.tasks;

import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaBuilder;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.RepositoryExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger;
import org.eclipse.mylyn.internal.tasks.core.data.TaskAttributeDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore;
import org.eclipse.mylyn.internal.tasks.core.externalization.ExternalizationManager;
import org.eclipse.mylyn.internal.tasks.core.externalization.IExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeQueriesJob;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ods.tasks.query.QueryParameters;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.openide.modules.Places;

/**
 *
 * @author tomas
 */
public class SynchronizationTest extends AbstractC2CTestCase {

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()  
                .addTest(SynchronizationTest.class)
                .gui(false)
                .suite();
    }
    private ExternalizationManager externalizationManager;
    private TaskList taskList;
    private RepositoryModel repositoryModel;
    private TaskListExternalizer taskListExternalizer;
    private TaskListExternalizationParticipant taskListExternalizationParticipant;
    private TaskActivityManager taskActivityManager;
    private SynchronizationManger synchronizationManger;
    private TaskDataManager taskDataManager;
//    private TaskJobFactory taskJobFactory;
    
    public SynchronizationTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        
        // TasksUIPlugin
        
        new File(Places.getUserDirectory() + "/var/bugtracking").mkdirs();
        
        // create data model
        externalizationManager = new ExternalizationManager(Places.getUserDirectory() + "/var/bugtracking");

        IExternalizationParticipant repositoryParticipant = new RepositoryExternalizationParticipant(
                        externalizationManager, trm);
        externalizationManager.addParticipant(repositoryParticipant);

        taskList = new TaskList();
        repositoryModel = new RepositoryModel(taskList, trm);
        taskListExternalizer = new TaskListExternalizer(repositoryModel, trm);

        taskListExternalizationParticipant = new TaskListExternalizationParticipant(repositoryModel, taskList,
                        taskListExternalizer, externalizationManager, trm);
        //externalizationManager.load(taskListSaveParticipant);
        externalizationManager.addParticipant(taskListExternalizationParticipant);
        taskList.addChangeListener(taskListExternalizationParticipant);

        taskActivityManager = new TaskActivityManager(trm, taskList);

        taskActivityManager.addActivationListener(taskListExternalizationParticipant);
        
        
        // instantiate taskDataManager
        TaskDataStore taskDataStore = new TaskDataStore(trm);
        synchronizationManger = new SynchronizationManger(repositoryModel);
        taskDataManager = new TaskDataManager(taskDataStore, trm, taskList, taskActivityManager,
                        synchronizationManger);
        
//        taskJobFactory = new TaskJobFactory(taskList, taskDataManager, trm, repositoryModel);

        taskDataManager.setDataPath(Places.getUserDirectory() + "/var/bugtracking");
        externalizationManager.setRootFolderPath(Places.getUserDirectory() + "/var/bugtracking");
//        getContextStore().setDirectory(new File(Places.getUserDirectory() + "/var/bugtracking", "tasks")); //$NON-NLS-1$

        externalizationManager.load();
        
        // externalizationManager.load() clears the repoMap in trm, so add repos one more time
        // NOTE: should
        trm.addRepositoryConnector(rc);
        trm.addRepository(taskRepository);
    }
    
    public void testSynch() throws CoreException, MalformedURLException {
        String summary = "testsynch" + System.currentTimeMillis();
        TaskData td = createIssue(summary);
        
        RepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), "testSynch"); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.SUMMARY.toString(), Criteria.Operator.EQUALS, summary);
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        taskList.deleteQuery(query); // cleanup from previous run
        taskList.addQuery(query);
        taskList.addChangeListener(new ITaskListChangeListener() {
            @Override
            public void containersChanged(Set<TaskContainerDelta> set) {
                for (TaskContainerDelta d : set) {
//                    IRepositoryElement element = d.getElement();
//                    System.out.println(" TL delta : " + d.toString());
//                    System.out.println("   " + element.getClass());
                    
//                    if(element instanceof RepositoryQuery) {
//                        // 
//                    }
//                    if(element instanceof TaskTask) {
//                        boolean changed = ((TaskTask)element).isChanged();
//                        if(changed) {
//                            System.out.println(" !!! CHANGED !!! ");
//                        }
//                    }
                    
                    
//                    System.out.println(" delta : " + 
//                                            d.getKind() + " " + 
//                                            d.getParent() + " " + 
//                                            d.getElement().getHandleIdentifier() + " " +
//                                            d.getElement().getSummary());
                }
            }
        });
        
        UnmatchedTaskContainer utc = new UnmatchedTaskContainer(taskRepository.getConnectorKind(), taskRepository.getUrl());
        taskList.addUnmatchedContainer(utc);
        //        SynchronizationJob sqj = taskJobFactory.createSynchronizeQueriesJob(rc, taskRepository, Collections.singleton(query));
        SynchronizeQueriesJob sqj = new SynchronizeQueriesJob(taskList, taskDataManager, repositoryModel, rc, taskRepository, Collections.singleton(query));
        
        taskDataManager.addListener(new ITaskDataManagerListener() {

            @Override
            public void taskDataUpdated(TaskDataManagerEvent tdme) {
                System.out.println(" DM delta : " + tdme.getTask().getTaskId());
                System.out.println(" DM delta : " + tdme.getTaskChanged());
                System.out.println(" DM delta : " + tdme.getTaskDataChanged());
                System.out.println("   " + tdme.getData());
                System.out.println("   " + tdme.getToken());
            }

            @Override
            public void editsDiscarded(TaskDataManagerEvent tdme) {
                
            }
        });
        
        int c = 0;
        synch(sqj, ++c);
        
        AbstractTask task = synch(sqj, ++c);
        
        externalizationManager.save(false);
        taskDataManager.setTaskRead(task, true);
        
//        TaskData taskData = rc.getTaskData(taskRepository, taskId, nullProgressMonitor);
        
        ITaskDataWorkingCopy taskDataState = taskDataManager.getWorkingCopy(task);
        TaskDataModel taskDataModel = new TaskDataModel(taskRepository, taskList.getTask(taskRepository.getUrl(), task.getTaskId()), taskDataState);
                
        // change
        TaskData taskData = taskDataModel.getTaskData();
        TaskAttribute rta = taskData.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(ta.getValue() + ".2");
        taskDataModel.attributeChanged(ta);
        
         // A severity is required., A status value is required., A priority value is required., A release value is required., A component is required., A product is required.
         
//        SubmitTaskJob stj = new SubmitTaskJob(taskDataManager,
//                                                rc,
//                                                taskDataModel.getTaskRepository(), 
//                                                task, 
//                                                taskDataModel.getTaskData(),
//                                                taskDataModel.getChangedOldAttributes());
        
//        RepositoryResponse rr = C2CUtil.postTaskData(rc, taskRepository, taskData, taskDataModel.getChangedOldAttributes());
        RepositoryResponse rr = rc.getTaskDataHandler().postTaskData(taskRepository, taskData, taskDataModel.getChangedOldAttributes(), new NullProgressMonitor());
        assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());
        
        synch(sqj, ++c);
        synch(sqj, ++c);
    }
    
    public TaskData createIssue(String summary) throws CoreException, MalformedURLException {
        return createTaskData(summary, "testing synching", "bug");
    }

    private String getSumary(TaskData td) {
        if (td != null) {
            TaskAttribute ta = td.getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
            if(ta != null) {
                return ta.getValue();
            }
            return "summary N/A";
        } 
        return "taskData N/A";
    }

    private AbstractTask synch(SynchronizeQueriesJob sqj, int counter) throws CoreException {
        System.out.println(" ======= run " + counter + " =======");
        
        IStatus status = sqj.run(nullProgressMonitor);
        assertTrue(status.isOK());
        Collection<AbstractTask> tasks = taskList.getAllTasks();
        assertFalse(tasks.isEmpty());
        
        AbstractTask task = tasks.iterator().next();
        ITaskDataWorkingCopy taskDataState = taskDataManager.getWorkingCopy(task);
        
        
        System.out.println(" sumaryLastRead : " + getSumary(taskDataState.getLastReadData()));
        System.out.println(" summaryRepository : " + getSumary(taskDataState.getRepositoryData()));
        System.out.println(" summaryEdit : " + getSumary(taskDataState.getEditsData()));
        
        TaskDataDiff diff = synchronizationManger.createDiff(taskDataState.getRepositoryData(), taskDataState.getLastReadData(), nullProgressMonitor);
        
        Collection<ITaskAttributeDiff> attrs = diff.getChangedAttributes();
        if(attrs != null && !attrs.isEmpty()) {
            for (ITaskAttributeDiff ta : attrs) {
                if(ta instanceof TaskAttributeDiff) {
                    TaskAttributeDiff tad = (TaskAttributeDiff) ta;
                    System.out.println(" tad : " + tad.toString());
                }
            }
        }
        return task;
    }
    
}

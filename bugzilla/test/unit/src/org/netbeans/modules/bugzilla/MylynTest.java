/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugzilla;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataState;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.internal.tasks.core.sync.SubmitTaskJob;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeQueriesJob;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskJobListener;
import org.eclipse.mylyn.tasks.core.TaskMigrationEvent;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_PASSWD;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_URL;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_USER;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.FileUtils;
import org.netbeans.modules.mylyn.util.SubmitCommand;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ondrej Vrabec
 */
public class MylynTest extends NbTestCase {
    private static final String DEFAULT_SUMMARY = "Default summary";
    private static final String QUERY_NAME = "My new query";
    private BugzillaRepository br;
    private TaskDataManager tdm;
    private TaskList taskList;
    private RepositoryModel repositoryModel;
    private SynchronizationManger synchronizationManger;
    private TaskRepositoryManager trm;
    private TaskListExternalizer taskListWriter;
    private String dataPath;
    private TaskRepository ltr;
    private File tasklistStorageFile;
    private TaskRepository btr;
    private BugzillaRepositoryConnector brc;
    
    private static final String PRODUCT = "mylyn"; //NOI18N
    private static final String COMPONENT = "default"; //NOI18N
    
    public MylynTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", new File(getDataDir(), "userdir").getAbsolutePath());
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector
        
        br = TestUtil.getRepository("testbugzilla", REPO_URL, REPO_USER, REPO_PASSWD);
        btr = br.getTaskRepository();
        trm = new TaskRepositoryManager();
        trm.addRepositoryConnector(brc = Bugzilla.getInstance().getRepositoryConnector());
        trm.addRepository(br.getTaskRepository());
        trm.addRepositoryConnector(new LocalRepositoryConnector());
        trm.addRepository(ltr = new TaskRepository(LocalRepositoryConnector.CONNECTOR_KIND,
                LocalRepositoryConnector.REPOSITORY_URL));
        
        TaskDataStore taskDataStore = new TaskDataStore(trm);
        taskList = new TaskList();
        repositoryModel = new RepositoryModel(taskList, trm);
        synchronizationManger = new SynchronizationManger(repositoryModel) {

            @Override
            public synchronized boolean hasParticipants (String connectorKind) {
                return false;
            }
            
        };
        tdm = new TaskDataManager(taskDataStore, trm, taskList, new TaskActivityManager(trm, taskList), synchronizationManger);
        dataPath = System.getProperty("netbeans.user") + "/bugtracking/mylyn";
        // reinit for queries
        if ("testCreateQuery".equals(getName())) {
            FileUtils.deleteRecursively(new File(dataPath));
        }
        tdm.setDataPath(dataPath);
        tasklistStorageFile = new File(dataPath, ITasksCoreConstants.DEFAULT_TASK_LIST_FILE);
        taskListWriter = new TaskListExternalizer(repositoryModel, trm);
        try {
            taskListWriter.readTaskList(taskList, tasklistStorageFile);
        } catch (CoreException ex) {
            if (!ex.getMessage().contains("Task list file not found")) {
                throw ex;
            }
        }
    }

    @Override
    protected void tearDown () throws Exception {
        // persist for next round
        tasklistStorageFile.getParentFile().mkdirs();
        taskListWriter.writeTaskList(taskList, tasklistStorageFile);
        super.tearDown();
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite();
        // creates an offline temporary task
        suite.addTest(new MylynTest("testCreateUnsubmittedTask"));
        // submit the temporary task to the server and turn it into a full remote task
        suite.addTest(new MylynTest("testSubmitTemporaryTask"));
        // create and submit task, no local
        suite.addTest(new MylynTest("testEditAndSubmitTask"));
        // external changes
        suite.addTest(new MylynTest("testIncomingChanges"));
        // conflicts in incoming and outgoing
        suite.addTest(new MylynTest("testConflicts"));
        
        // create and init query
        suite.addTest(new MylynTest("testCreateQuery"));
        // restart IDE and get offline results
        suite.addTest(new MylynTest("testGetQueryOfflineResults"));
        // synchronize and get external changes
        suite.addTest(new MylynTest("testSynchronizeQuery"));
        // remove from query internal
        suite.addTest(new MylynTest("testTaskRemovedFromQueryInt"));
        // remove from query externally
        suite.addTest(new MylynTest("testTaskRemovedFromQueryExt"));
        return suite;
    }
    
    public void testCreateUnsubmittedTask () throws Exception {
        // create taskdata
        TaskData taskData = TestUtil.createTaskData(brc, btr, DEFAULT_SUMMARY, "", "");
        
        // create local task
        AbstractTask task = new LocalTask(String.valueOf(taskList.getNextLocalTaskId()), "");
        task.setAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND, btr.getConnectorKind());
        task.setAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL, btr.getUrl());
        task.setSynchronizationState(ITask.SynchronizationState.OUTGOING_NEW);
        
        // initialize task from taskdata
        ITaskMapping mapping = brc.getTaskMapping(taskData);
        String taskKind = mapping.getTaskKind();
        if (taskKind != null && taskKind.length() > 0) {
            task.setTaskKind(taskKind);
        }
        ITaskDataWorkingCopy workingCopy = tdm.createWorkingCopy(task, taskData);
        workingCopy.save(null, null);
        TaskRepository taskRepository = trm.getRepository(taskData.getConnectorKind(), taskData.getRepositoryUrl());
        brc.updateNewTaskFromTaskData(taskRepository, task, taskData);
        String summary = mapping.getSummary();
        if (summary != null && summary.length() > 0) {
            task.setSummary(summary);
        }
        
        // sort into tasklist
        taskList.addTask(task, taskList.getUnsubmittedContainer(task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL)));
        
        /*************** TEST *******************/
        // is it really in the tasklist
        assertSame(task, taskList.getTask(task.getHandleIdentifier()));
        assertSame(task, taskList.getTask(task.getRepositoryUrl(), task.getTaskId()));
        assertNull(taskList.getTask(btr.getUrl(), task.getTaskId()));
        
        // now simulate IDE restart
        // after restart and another save, data for disabled connectors should not be removed from the tasklist
        // and available again when the connector is reenabled
        // A) persist
        taskListWriter.writeTaskList(taskList, tasklistStorageFile);
        
        // B) restart with some connectors disabled
        // disable connectors
        trm.removeRepository(ltr);
        trm.removeRepositoryConnector(LocalRepositoryConnector.CONNECTOR_KIND);
        taskList.reset();
        taskListWriter.readTaskList(taskList, tasklistStorageFile);
        // now there is no such task
        assertNull(taskList.getTask(task.getHandleIdentifier()));
        // add some new data
        taskList.addCategory(new TaskCategory("CAT1", "DESC"));
        // C) persist again
        // tasklist is empty and the old content should not be overwritten
        taskListWriter.writeTaskList(taskList, tasklistStorageFile);
        
        // D) restart with connectors enabled egain
        // disable connectors
        trm.addRepository(ltr);
        trm.addRepositoryConnector(new LocalRepositoryConnector());
        taskList.reset();
        assertEquals(0, taskList.getLastLocalTaskId());
        // empty
        assertNull(taskList.getContainerForHandle("CAT1"));
        taskListWriter.readTaskList(taskList, tasklistStorageFile);
        // is it really in the tasklist
        assertEquals(1, taskList.getLastLocalTaskId());
        assertEquals(task, taskList.getTask(task.getHandleIdentifier()));
        assertEquals(task, taskList.getTask(task.getRepositoryUrl(), task.getTaskId()));
        assertNull(taskList.getTask(btr.getUrl(), task.getTaskId()));
        assertNotNull(taskList.getContainerForHandle("CAT1"));
    }
    
    public void testSubmitTemporaryTask () throws Exception {
        ITask task = taskList.getTask(LocalRepositoryConnector.REPOSITORY_URL, "1");
        assertNotNull(task);
        
        // edit the task
        ITaskDataWorkingCopy workingCopy = tdm.getWorkingCopy(task);
        TaskDataModel model = new TaskDataModel(btr, task, workingCopy);
        
        // model.getTaskData returns our local data
        TaskAttribute rta = model.getTaskData().getRoot();
        assertFalse(model.isDirty());
        assertEquals(DEFAULT_SUMMARY, task.getSummary());
        assertEquals(DEFAULT_SUMMARY, rta.getMappedAttribute(TaskAttribute.SUMMARY).getValue());
        // now edit summary, product and component
        String newSummary = "New task summary";
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(newSummary);
        model.attributeChanged(ta);
        ta = rta.getMappedAttribute(TaskAttribute.PRODUCT);
        ta.setValue(PRODUCT);
        model.attributeChanged(ta);
        ta = rta.getMappedAttribute(TaskAttribute.COMPONENT);
        ta.setValue(COMPONENT);
        model.attributeChanged(ta);
        
        // now we have unsaved changes, the task is dirty
        assertTrue(model.isDirty());
        // not yet saved
        assertEquals(DEFAULT_SUMMARY, task.getSummary());
        // save
        model.save(new NullProgressMonitor());
        // all saved?
        assertFalse(model.isDirty());
        // well, not exactly, for new unsubmitted task we need to manually refresh task's attributes
        assertEquals(DEFAULT_SUMMARY, task.getSummary());
        if (task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
            task.setSummary(newSummary);
        }
        assertEquals(newSummary, task.getSummary());
        
        // let's submit finally
        final CountDownLatch cdl = new CountDownLatch(1);
        SubmitJob job = new SubmitTaskJob(tdm,
                trm.getRepositoryConnector(task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND)),
                trm.getRepository(task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL)),
                task, 
                model.getTaskData(), model.getChangedOldAttributes() /*??? no idea what's this good for*/,
                Collections.<TaskJobListener>emptyList());
        // this needs to turn local task into a full repository task
        final ITask oldTask = task;
        job.addSubmitJobListener(new SubmitJobListener() {

            @Override
            public void taskSubmitted (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // nothing for new task
            }

            @Override
            public void taskSynchronized (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // ???
            }

            @Override
            public void done (SubmitJobEvent event) {
                // turn into full task
                SubmitJob job = event.getJob();
                ITask newTask = job.getTask();
                if (newTask != null && newTask != oldTask) {
                    // copy anything you want
                    taskList.addTask(newTask);
                    taskList.deleteTask(oldTask);
                    brc.migrateTask(new TaskMigrationEvent(oldTask, newTask));
                    try {
                        tdm.deleteTaskData(oldTask);
                    } catch (CoreException ex) {
                        log(ex.toString());
                    }
                }
                cdl.countDown();
            }
        });
        job.schedule();
        cdl.await();
        task = job.getTask();
        
        assertNull(taskList.getTask(LocalRepositoryConnector.REPOSITORY_URL, "1"));
        assertEquals(1, taskList.getLastLocalTaskId());
        assertSame(task, taskList.getTask(task.getHandleIdentifier()));
        assertSame(task, taskList.getTask(btr.getUrl(), task.getTaskId()));
        
        assertEquals(newSummary, task.getSummary());
        workingCopy = tdm.getWorkingCopy(task);
        model = new TaskDataModel(btr, task, workingCopy);
        assertFalse(model.isDirty());
        assertTrue(model.hasBeenRead());
        assertTrue(model.getChangedAttributes().isEmpty());
        assertTrue(model.getChangedOldAttributes().isEmpty());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
    }
    
    public void testEditAndSubmitTask () throws Exception {
        assertEquals(1, taskList.getTasks(br.getUrl()).size());
        ITask task = taskList.getTasks(br.getUrl()).iterator().next();
        assertNotNull(task);
        
        // edit the task
        ITaskDataWorkingCopy workingCopy = tdm.getWorkingCopy(task);
        final TaskDataModel model = new TaskDataModel(btr, task, workingCopy);
        
        // model.getTaskData returns our local data
        TaskAttribute rta = model.getTaskData().getRoot();
        assertFalse(model.isDirty());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        // now edit summary, add some comments
        String oldSummary = task.getSummary();
        String newSummary = getName();
        TaskAttribute summaryAttr = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        summaryAttr.setValue(newSummary);
        model.attributeChanged(summaryAttr);
        String comment = "Comment, testing " + getName();
        TaskAttribute commentAttr = rta.getMappedAttribute(TaskAttribute.COMMENT_NEW);
        commentAttr.setValue(comment);
        model.attributeChanged(commentAttr);
        
        // now we have unsaved changes, the task is dirty
        assertTrue(model.isDirty());
        assertEquals(2, model.getChangedAttributes().size());
        for (TaskAttribute attr : model.getChangedAttributes()) {
            if (summaryAttr.getId().equals(attr.getId())) {
                assertEquals(newSummary, attr.getValue());
            } else if (commentAttr.getId().equals(attr.getId())) {
                assertEquals(comment, attr.getValue());
            } else {
                fail(attr.getId());
            }
        }
        // not yet saved
        assertEquals(oldSummary, task.getSummary());
        // save
        model.save(new NullProgressMonitor());
        // all saved?
        assertFalse(model.isDirty());
        // task's attributes are modified only whn the task is submitted
        assertEquals(oldSummary, task.getSummary());
        
        // now what are outgoing changes???
        assertEquals(ITask.SynchronizationState.OUTGOING, task.getSynchronizationState());
        for (TaskAttribute attr : new TaskAttribute[] { summaryAttr, commentAttr }) {
            assertTrue(model.hasOutgoingChanges(attr));
        }
        
        // submit
        final CountDownLatch cdl = new CountDownLatch(1);
        SubmitJob job = new SubmitTaskJob(tdm, brc, btr, task, 
                model.getTaskData(), model.getChangedOldAttributes() /*??? no idea what's this good for*/,
                Collections.<TaskJobListener>emptyList());
        job.addSubmitJobListener(new SubmitJobListener() {

            @Override
            public void taskSubmitted (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // 
            }

            @Override
            public void taskSynchronized (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // ???
            }

            @Override
            public void done (SubmitJobEvent event) {
                try {
                    model.refresh(new NullProgressMonitor());
                } catch (CoreException ex) {
                    log(ex.toString());
                }
                cdl.countDown();
            }
        });
        job.schedule();
        cdl.await();
        task = job.getTask();
        
        assertSame(task, taskList.getTask(task.getHandleIdentifier()));
        assertSame(task, taskList.getTask(btr.getUrl(), task.getTaskId()));
        assertFalse(model.isDirty());
        assertTrue(model.getChangedAttributes().isEmpty());
        assertTrue(model.getChangedOldAttributes().isEmpty());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(newSummary, task.getSummary());
    }
    
    public void testIncomingChanges () throws Exception {
        assertEquals(1, taskList.getTasks(br.getUrl()).size());
        ITask task = taskList.getTasks(br.getUrl()).iterator().next();
        assertNotNull(task);
        
        // edit the task
        ITaskDataWorkingCopy workingCopy = tdm.getWorkingCopy(task);
        final TaskDataModel model = new TaskDataModel(btr, task, workingCopy);
        
        TaskAttribute rta = model.getTaskData().getRoot();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        // now make an external change in summary
        TaskData external = new TaskData(model.getTaskData().getAttributeMapper(),
                model.getTaskData().getConnectorKind(),
                model.getTaskData().getRepositoryUrl(),
                model.getTaskData().getTaskId());
        external.setVersion(model.getTaskData().getVersion());
        for (TaskAttribute child : rta.getAttributes().values()) {
            external.getRoot().deepAddCopy(child);
        }
        String oldSummary = task.getSummary();
        String newSummary = getName();
        external.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue(newSummary);
        SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
        br.getExecutor().execute(submitCmd);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, task.getSummary());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        
        final TaskDataManagerEvent[] evHolder = new TaskDataManagerEvent[1];
        ITaskDataManagerListener tdmListener = new ITaskDataManagerListener() {
            @Override
            public void taskDataUpdated (TaskDataManagerEvent event) {
                evHolder[0] = event;
                
            }
            @Override
            public void editsDiscarded (TaskDataManagerEvent event) {
            }
        };
        tdm.addListener(tdmListener);
        // sync with server
        SynchronizeTasksJob job = new SynchronizeTasksJob(taskList,
                tdm,
                repositoryModel,
                brc,
                btr,
                Collections.<ITask>singleton(task));
        job.run(new NullProgressMonitor());
        
        assertFalse(model.isDirty());
        assertEquals(ITask.SynchronizationState.INCOMING, task.getSynchronizationState());
        assertTrue(model.getChangedAttributes().isEmpty());
        assertTrue(model.getChangedOldAttributes().isEmpty());
        assertEquals(newSummary, task.getSummary());
        // need changed event
        TaskDataManagerEvent ev = evHolder[0];
        assertNotNull(ev);
        assertTrue(ev.getTaskChanged());
        assertTrue(ev.getTaskDataChanged());
        assertTrue(ev.getTaskDataUpdated());
        
        // need deltas
        workingCopy.refresh(null);
        TaskDataDiff diff = new TaskDataDiff(repositoryModel, workingCopy.getRepositoryData(), workingCopy.getLastReadData());
        for (ITaskAttributeDiff diffAttr : diff.getChangedAttributes()) {
            // summary changed
            assertEquals(diff.getNewTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getId(),
                    diffAttr.getAttributeId());
        }
        // diff between local and remote
        diff = new TaskDataDiff(repositoryModel, workingCopy.getRepositoryData(), workingCopy.getLocalData());
        for (ITaskAttributeDiff diffAttr : diff.getChangedAttributes()) {
            // summary changed
            assertEquals(diff.getNewTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getId(),
                    diffAttr.getAttributeId());
        }
        
        tdm.removeListener(tdmListener);
    }
    
    public void testConflicts () throws Exception {
        assertEquals(1, taskList.getTasks(br.getUrl()).size());
        ITask task = taskList.getTasks(br.getUrl()).iterator().next();
        assertNotNull(task);
        
        // edit the task
        ITaskDataWorkingCopy workingCopy = tdm.getWorkingCopy(task);
        final TaskDataModel model = new TaskDataModel(btr, task, workingCopy);
        
        TaskAttribute rta = model.getTaskData().getRoot();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        
        // make an external change in summary
        TaskData external = new TaskData(model.getTaskData().getAttributeMapper(),
                model.getTaskData().getConnectorKind(),
                model.getTaskData().getRepositoryUrl(),
                model.getTaskData().getTaskId());
        external.setVersion(model.getTaskData().getVersion());
        for (TaskAttribute child : rta.getAttributes().values()) {
            external.getRoot().deepAddCopy(child);
        }
        String oldSummary = task.getSummary();
        String newSummary = getName();
        external.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue(newSummary);
        external.getRoot().getMappedAttribute(TaskAttribute.PRIORITY).setValue("P5");
        SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
        br.getExecutor().execute(submitCmd);
        
        // make local changes
        rta = model.getTaskData().getRoot();
        String newLocalSummary = getName() + "_local";
        TaskAttribute summaryAttr = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        summaryAttr.setValue(newLocalSummary);
        model.attributeChanged(summaryAttr);
        
        assertTrue(model.isDirty());
        // save
        model.save(new NullProgressMonitor());
        assertFalse(model.isDirty());
        
        // still no change, need to do a sync job
        assertEquals(ITask.SynchronizationState.OUTGOING, task.getSynchronizationState());
        
        final TaskDataManagerEvent[] evHolder = new TaskDataManagerEvent[1];
        ITaskDataManagerListener tdmListener = new ITaskDataManagerListener() {
            @Override
            public void taskDataUpdated (TaskDataManagerEvent event) {
                evHolder[0] = event;
            }
            @Override
            public void editsDiscarded (TaskDataManagerEvent event) {
                fail("");
            }
        };
        tdm.addListener(tdmListener);
        // sync with server
        SynchronizeTasksJob job = new SynchronizeTasksJob(taskList,
                tdm,
                repositoryModel,
                brc,
                btr,
                Collections.<ITask>singleton(task));
        job.run(new NullProgressMonitor());
        
        assertEquals(ITask.SynchronizationState.CONFLICT, task.getSynchronizationState());
        assertFalse(model.getChangedAttributes().isEmpty());
        assertFalse(model.getChangedOldAttributes().isEmpty());
        assertEquals(newSummary, task.getSummary());
        
        TaskDataManagerEvent ev = evHolder[0];
        assertNotNull(ev);
        assertTrue(ev.getTaskChanged());
        assertTrue(ev.getTaskDataChanged());
        assertTrue(ev.getTaskDataUpdated());
        
        // need deltas
        // use getTaskDataState and not getWorkingCopy to get incoming changes
        // getWorkingCopy should be called only from the editor page, it clears tasks conflicted state
        workingCopy = tdm.getTaskDataState(task);
        // diff between local and remote
        TaskDataDiff diff = new TaskDataDiff(repositoryModel, workingCopy.getRepositoryData(), workingCopy.getLastReadData());
        assertEquals(2, diff.getChangedAttributes().size());
        for (ITaskAttributeDiff diffAttr : diff.getChangedAttributes()) {
            // summary and priority changed
            assertTrue(diff.getNewTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getId().equals(diffAttr.getAttributeId())
                    || diff.getNewTaskData().getRoot().getMappedAttribute(TaskAttribute.PRIORITY).getId().equals(diffAttr.getAttributeId()));
        }
        // diff between incoming
        workingCopy.revert(); // as a side-effect loads working copy data
        diff = new TaskDataDiff(repositoryModel, workingCopy.getRepositoryData(), workingCopy.getLocalData());
        assertEquals(1, diff.getChangedAttributes().size());
        for (ITaskAttributeDiff diffAttr : diff.getChangedAttributes()) {
            // summary changed
            assertTrue(diff.getNewTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getId().equals(diffAttr.getAttributeId()));
        }
        tdm.removeListener(tdmListener);
        
        assertEquals(ITask.SynchronizationState.CONFLICT, task.getSynchronizationState());
        // open
        // model.refresh refreshes task's CONFLICTED status
        model.refresh(new NullProgressMonitor());
        assertEquals(ITask.SynchronizationState.OUTGOING, task.getSynchronizationState());
        assertEquals(1, model.getChangedAttributes().size());
        assertEquals(1, model.getChangedOldAttributes().size());
        // discard edits
        tdm.addListener(tdmListener = new ITaskDataManagerListener() {
            @Override
            public void taskDataUpdated (TaskDataManagerEvent event) {
                fail("");
            }

            @Override
            public void editsDiscarded (TaskDataManagerEvent event) {
                try {
                    model.refresh(null);
                } catch (CoreException ex) {
                    log(ex.toString());
                }
            }
        });
        tdm.discardEdits(task);
        tdm.removeListener(tdmListener);
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(0, model.getChangedAttributes().size());
        assertEquals(0, model.getChangedOldAttributes().size());
    }
    
    public void testCreateQuery () throws Exception {
        // clear tasklist and reinit
        taskList.reset();
        tasklistStorageFile.delete();
        
        assertEquals(0, taskList.getTasks(btr.getRepositoryUrl()).size());
        // query list is empty
        assertEquals(0, taskList.getRepositoryQueries(btr.getRepositoryUrl()).size());
        final RepositoryQuery q = (RepositoryQuery) repositoryModel.createRepositoryQuery(btr);
        q.setSummary(QUERY_NAME);
        q.setUrl("/buglist.cgi?query_format=advanced&product=" + PRODUCT + "&component=" + COMPONENT + "&bug_status=NEW" + "&bug_status=REOPENED");
        taskList.addQuery(q);
        assertEquals(1, taskList.getRepositoryQueries(btr.getRepositoryUrl()).size());
        
        // synchronize
        assertEquals(0, q.getChildren().size());
        final List<ITask> eventsPartial = new ArrayList<ITask>();
        final List<ITask> events = new ArrayList<ITask>();
        ITaskDataManagerListener tdmListener = new ITaskDataManagerListener() {
            @Override
            public void taskDataUpdated (TaskDataManagerEvent event) {
                if (event.getTaskData().isPartial()) {
                    eventsPartial.add(event.getTask());
                } else {
                    events.add(event.getTask());
                }
            }
            @Override
            public void editsDiscarded (TaskDataManagerEvent event) {
                fail("");
            }
        };
        tdm.addListener(tdmListener);
        final List<ITask> addedTasks = new ArrayList<ITask>();
        // listener on tasklist, notified when content of container changes (e.g. new task in a query is added)
        ITaskListChangeListener tlList = new ITaskListChangeListener() {
            @Override
            public void containersChanged (Set<TaskContainerDelta> containers) {
                for (TaskContainerDelta delta : containers) {
                    if (delta.getKind() == TaskContainerDelta.Kind.ADDED && delta.getParent() == q
                            && delta.getElement() instanceof ITask) {
                        addedTasks.add(((ITask) delta.getElement()));
                        // the task should be already in the query
                        assertEquals(addedTasks.size(), q.getChildren());
                    }
                }
            }
        };
        taskList.addChangeListener(tlList);
        SynchronizeQueriesJob job = new SynchronizeQueriesJob(taskList, tdm, repositoryModel, brc, btr,
                Collections.<RepositoryQuery>singleton(q));
        job.run(new NullProgressMonitor());
        
        assertFalse(q.getChildren().isEmpty());
        assertEquals(q.getChildren().size(), addedTasks.size());
        assertEquals(q.getChildren().size(), eventsPartial.size());
        assertEquals(q.getChildren().size(), events.size());
        
        tdm.removeListener(tdmListener);
        taskList.removeChangeListener(tlList);
        
        // all tasks are NEW
        for (ITask task : taskList.getTasks(btr.getUrl())) {
            assertEquals(ITask.SynchronizationState.INCOMING_NEW, task.getSynchronizationState());
            assertTrue(addedTasks.contains(task));
            assertTrue(events.contains(task));
            assertTrue(eventsPartial.contains(task));
        }
    }
    
    public void testGetQueryOfflineResults () throws Exception {
        // query list is empty
        assertEquals(1, taskList.getRepositoryQueries(btr.getRepositoryUrl()).size());
        final RepositoryQuery q = getQueryFromTasklist(taskList, btr, QUERY_NAME);
        
        // get details
        assertFalse(q.getChildren().isEmpty());
        // all tasks are still NEW
        for (ITask task : q.getChildren()) {
            assertEquals(ITask.SynchronizationState.INCOMING_NEW, task.getSynchronizationState());
            // mark read - open
            ITaskDataWorkingCopy workingCopy = tdm.getWorkingCopy(task);
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        }
    }
    
    public void testSynchronizeQuery () throws Exception {
        RepositoryQuery q = getQueryFromTasklist(taskList, btr, QUERY_NAME);
        
        // get details
        assertFalse(q.getChildren().isEmpty());
        // make external changes in summaries
        Map<ITask, String> newSummaries = new HashMap<ITask, String>(q.getChildren().size());
        for (ITask task : q.getChildren()) {
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
            String newSummary = getName() + System.currentTimeMillis();
            newSummaries.put(task, newSummary);
            TaskData external = tdm.getTaskData(task);
            external.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue(newSummary);
            SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
            br.getExecutor().execute(submitCmd);
        }
        
        // no change yet
        for (ITask task : q.getChildren()) {
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        }
        
        // sync
        final List<ITask> updatedTasks = new ArrayList<ITask>(newSummaries.size());
        ITaskDataManagerListener tdmListener = new ITaskDataManagerListener() {
            @Override
            public void taskDataUpdated (TaskDataManagerEvent event) {
                assertTrue(event.getTaskChanged());
                if (!event.getTaskData().isPartial()) {
                    updatedTasks.add(event.getTask());
                }
            }
            @Override
            public void editsDiscarded (TaskDataManagerEvent event) {
                fail("");
            }
        };
        tdm.addListener(tdmListener);
        SynchronizeQueriesJob job = new SynchronizeQueriesJob(taskList, tdm, repositoryModel, brc, btr,
                Collections.<RepositoryQuery>singleton(q));
        job.run(new NullProgressMonitor());
        
        assertEquals(newSummaries.size(), q.getChildren().size());
        assertEquals(newSummaries.size(), updatedTasks.size());
        
        tdm.removeListener(tdmListener);
        
        // all tasks have incoming changes
        for (ITask task : taskList.getTasks(btr.getUrl())) {
            assertEquals(ITask.SynchronizationState.INCOMING, task.getSynchronizationState());
            assertEquals(newSummaries.get(task), task.getSummary());
            
            // what changes?
            TaskDataState repositoryState = tdm.getTaskDataState(task);
            // diff between local and remote
            TaskDataDiff diff = new TaskDataDiff(repositoryModel, repositoryState.getRepositoryData(), repositoryState.getLastReadData());
            assertEquals(1, diff.getChangedAttributes().size());
            // summary changed
            assertEquals(diff.getNewTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getId(),
                    diff.getChangedAttributes().iterator().next().getAttributeId());
            assertEquals(ITask.SynchronizationState.INCOMING, task.getSynchronizationState());
            // open and see changes
            TaskDataModel model = new TaskDataModel(btr, task, tdm.getWorkingCopy(task));
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
            // changes ?
            assertTrue(model.hasIncomingChanges(model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY)));
            // mark read
            model.refresh(null);
            assertFalse(model.hasIncomingChanges(model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY)));
        }
    }
    
    public void testTaskRemovedFromQueryInt () throws Exception {
        final RepositoryQuery q = getQueryFromTasklist(taskList, btr, QUERY_NAME);
        assertFalse(q.getChildren().isEmpty());
        // get a task to close
        ITask task = q.getChildren().iterator().next();
        TaskDataModel model = new TaskDataModel(btr, task, tdm.getWorkingCopy(task));
        TaskOperation taskOperation = null;
        TaskAttribute opAttr = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.OPERATION);
        for (TaskOperation op : model.getTaskData().getAttributeMapper().getTaskOperations(opAttr)) {
            if (BugzillaOperation.resolve.getLabel().equals(op.getLabel())) {
                taskOperation = op;
                break;
            }
        }
        assertNotNull(taskOperation);
        assertFalse(task.isCompleted());
        model.getTaskData().getAttributeMapper().setTaskOperation(opAttr, taskOperation);
        model.attributeChanged(opAttr);
        TaskAttribute resolutionAttr = model.getTaskData().getRoot().getMappedAttribute(BugzillaOperation.resolve.getInputId());
        resolutionAttr.setValue("WONTFIX");
        model.attributeChanged(resolutionAttr);
        
        assertEquals(2, model.getChangedOldAttributes().size());
        assertTrue(model.isDirty());
        
        model.save(null);
        assertEquals(2, model.getChangedOldAttributes().size());
        assertFalse(model.isDirty());
        assertFalse(task.isCompleted());
        
        // submit
        final List<ITask> addedTasks = new ArrayList<ITask>();
        final List<ITask> removedTasks = new ArrayList<ITask>();
        ITaskListChangeListener tlList = new ITaskListChangeListener() {
            @Override
            public void containersChanged (Set<TaskContainerDelta> containers) {
                for (TaskContainerDelta delta : containers) {
                    if (!delta.isTransient() && delta.getParent() == q && delta.getElement() instanceof ITask) {
                        if (delta.getKind() == TaskContainerDelta.Kind.ADDED) {
                            addedTasks.add((ITask) delta.getElement());
                        } else if (delta.getKind() == TaskContainerDelta.Kind.REMOVED) {
                            removedTasks.add((ITask) delta.getElement());
                        }
                    }
                }
            }
        };
        taskList.addChangeListener(tlList);
        final CountDownLatch cdl = new CountDownLatch(1);
        SubmitJob job = new SubmitTaskJob(tdm,
                brc,
                btr,
                task, 
                model.getTaskData(), model.getChangedOldAttributes(),
                Collections.<TaskJobListener>emptyList());
        // this needs to turn local task into a full repository task
        job.addSubmitJobListener(new SubmitJobListener() {

            @Override
            public void taskSubmitted (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // nothing for new task
            }

            @Override
            public void taskSynchronized (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // ???
            }

            @Override
            public void done (SubmitJobEvent event) {
                // refresh all queries
                // probably should be in background
                SynchronizeQueriesJob syncJob = new SynchronizeQueriesJob(taskList, tdm, repositoryModel, brc, btr,
                        taskList.getRepositoryQueries(btr.getUrl()));
                syncJob.addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void done (IJobChangeEvent event) {
                        cdl.countDown();
                    }
                });
                syncJob.schedule();
            }
        });
        job.schedule();
        cdl.await();
        assertTrue(task.isCompleted());
        
        // the task should be gone from the query
        assertFalse(q.getChildren().contains(task));
        // check tasklist event - task removed from query
        assertEquals(0, addedTasks.size());
        assertEquals(1, removedTasks.size());
        assertTrue(removedTasks.contains(task));
        // task's data
        model.refresh(null);
        assertEquals(0, model.getChangedOldAttributes().size());
        assertEquals("WONTFIX", tdm.getWorkingCopy(task).getRepositoryData().getRoot().getMappedAttribute(TaskAttribute.RESOLUTION).getValue());
        
        /*******************************************************/
        
        // reopen and add to query again
        opAttr = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.OPERATION);
        taskOperation = null;
        for (TaskOperation op : model.getTaskData().getAttributeMapper().getTaskOperations(opAttr)) {
            if (BugzillaOperation.reopen.getLabel().equals(op.getLabel())) {
                taskOperation = op;
                break;
            }
        }
        assertNotNull(taskOperation);
        assertTrue(task.isCompleted());
        model.getTaskData().getAttributeMapper().setTaskOperation(opAttr, taskOperation);
        model.attributeChanged(opAttr);
        assertEquals(1, model.getChangedOldAttributes().size());
        assertTrue(model.isDirty());
        
        model.save(null);
        assertEquals(1, model.getChangedOldAttributes().size());
        assertFalse(model.isDirty());
        assertTrue(task.isCompleted());
        
        // submit
        addedTasks.clear();
        removedTasks.clear();
        final CountDownLatch cdl2 = new CountDownLatch(1);
        job = new SubmitTaskJob(tdm,
                brc,
                btr,
                task, 
                model.getTaskData(), model.getChangedOldAttributes(),
                Collections.<TaskJobListener>emptyList());
        // this needs to turn local task into a full repository task
        job.addSubmitJobListener(new SubmitJobListener() {

            @Override
            public void taskSubmitted (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // nothing for new task
            }

            @Override
            public void taskSynchronized (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
                // ???
            }

            @Override
            public void done (SubmitJobEvent event) {
                // refresh all queries
                // probably should be in background
                SynchronizeQueriesJob syncJob = new SynchronizeQueriesJob(taskList, tdm, repositoryModel, brc, btr,
                        taskList.getRepositoryQueries(btr.getUrl()));
                syncJob.addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void done (IJobChangeEvent event) {
                        cdl2.countDown();
                    }
                });
                syncJob.schedule();
            }
        });
        job.schedule();
        cdl2.await();
        assertFalse(task.isCompleted());
        
        // the task should be again int the query
        assertTrue(q.getChildren().contains(task));
        // check tasklist event - task added to the query
        assertEquals(1, addedTasks.size());
        assertEquals(0, removedTasks.size());
        assertTrue(addedTasks.contains(task));
        // task's data
        model.refresh(null);
        assertEquals(0, model.getChangedOldAttributes().size());
        assertEquals("REOPENED", tdm.getWorkingCopy(task).getRepositoryData().getRoot().getMappedAttribute(TaskAttribute.STATUS).getValue());
        
        taskList.removeChangeListener(tlList);
    }
    
    public void testTaskRemovedFromQueryExt () throws Exception {
        final RepositoryQuery q = getQueryFromTasklist(taskList, btr, QUERY_NAME);
        List<ITask> tasksToClose = new ArrayList<ITask>(q.getChildren());
        assertFalse(tasksToClose.isEmpty());
        
        // make external changes - close all tasks
        for (ITask task : tasksToClose) {
            assertFalse(task.isCompleted());
            TaskData external = tdm.getTaskData(task);
            TaskAttribute opAttr = external.getRoot().getMappedAttribute(TaskAttribute.OPERATION);
            TaskOperation taskOperation = null;
            for (TaskOperation op : external.getAttributeMapper().getTaskOperations(opAttr)) {
                if (BugzillaOperation.resolve.getLabel().equals(op.getLabel())) {
                    taskOperation = op;
                    break;
                }
            }
            assertNotNull(taskOperation);
            external.getAttributeMapper().setTaskOperation(opAttr, taskOperation);
            TaskAttribute resolutionAttr = external.getRoot().getMappedAttribute(BugzillaOperation.resolve.getInputId());
            resolutionAttr.setValue("WONTFIX");
            SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
            br.getExecutor().execute(submitCmd);
        }
        
        // refresh query
        final List<ITask> addedTasks = new ArrayList<ITask>();
        final List<ITask> removedTasks = new ArrayList<ITask>();
        ITaskListChangeListener tlList = new ITaskListChangeListener() {
            @Override
            public void containersChanged (Set<TaskContainerDelta> containers) {
                for (TaskContainerDelta delta : containers) {
                    if (!delta.isTransient() && delta.getParent() == q && delta.getElement() instanceof ITask) {
                        if (delta.getKind() == TaskContainerDelta.Kind.ADDED) {
                            addedTasks.add((ITask) delta.getElement());
                        } else if (delta.getKind() == TaskContainerDelta.Kind.REMOVED) {
                            removedTasks.add((ITask) delta.getElement());
                        }
                    }
                }
            }
        };
        taskList.addChangeListener(tlList);
        SynchronizeQueriesJob syncJob = new SynchronizeQueriesJob(taskList, tdm, repositoryModel, brc, btr,
            taskList.getRepositoryQueries(btr.getUrl()));
        syncJob.run(new NullProgressMonitor());
        
        assertEquals(0, q.getChildren().size());
        assertEquals(tasksToClose.size(), removedTasks.size());
        assertEquals(0, addedTasks.size());
        for (ITask task : tasksToClose) {
            assertTrue(task.isCompleted());
            // check tasklist event - task removed from query
            assertTrue(removedTasks.contains(task));
            // task's data
            assertEquals("WONTFIX", tdm.getWorkingCopy(task).getRepositoryData().getRoot().getMappedAttribute(TaskAttribute.RESOLUTION).getValue());
        }
    }

    private RepositoryQuery getQueryFromTasklist (TaskList taskList, TaskRepository repository, String queryName) {
        for (RepositoryQuery q : taskList.getRepositoryQueries(repository.getUrl())) {
            if (queryName.equals(q.getSummary())) {
                return q;
            }
        }
        fail("No query with name " + queryName);
        return null;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.rest;

import org.netbeans.junit.NbTestCase;

/**
 *
 * 
 */
public class JiraIssueTest extends NbTestCase {

    public JiraIssueTest(String name) {
        super(name);
    }
    
//    public JiraIssueTest(String arg0) {
//        super(arg0);
//    }
//    
//    @Override
//    protected void setUp() throws Exception { 
//        JiraTestUtil.initClient(getWorkDir());
//        BugtrackingManager.getInstance();
//        // need this to initialize cache -> server defined status values & co
//        getClient().getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
//        JiraTestUtil.cleanProject(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()));
//    }
//
//    @Override
//    protected void tearDown() throws Exception {
//    }
//
////    XXX not proprly supported in mylyn
////    public void testQuickSearch() throws Throwable {
////        try {
////
////            // create
////            RepositoryResponse rr = JiraTestUtil.createIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt", "Alles Kaputt!", "Bug");
////            assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
////            assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()));
////            TaskData data1 = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId());
////
////            rr = JiraTestUtil.createIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt", "Alles Kaputt!", "Bug");
////            assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
////            assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()));
////            TaskData data2 = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId());
////
////            TextFilter tf = new TextFilter("Kaputt");
////            JiraTestUtil.TestTaskDataCollector tdc = JiraTestUtil.list(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), tf);
////
////            assertEquals(2, tdc.data.size());
////            TaskData data = null;
////            for (TaskData d : tdc.data) {
////                if(d.getTaskId().equals(data1.getTaskId())) {
////                    data = d;
////                    break;
////                }
////            }
////            assertNotNull(data);
////
////
////        } catch (Exception exception) {
////            JiraTestUtil.handleException(exception);
////        }
////    }
//
//    public void testJiraTaskData() throws Throwable {
//        try {
//
//            // create
//            RepositoryResponse rr = JiraTestUtil.createIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt", "Alles Kaputt!", "Bug");
//            assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
//            assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()));
//
//            String taskId = rr.getTaskId();
//
//            // update
//            updateTaskData(taskId);
//
//            // subtask
//            createSubtask(taskId);
//
//            // list
//            list(taskId);
//
//            // add attachement
//            addAttachement(taskId, "crap");
//
//            // delete attachement
//            readAttachement(taskId, "crap");
//
//            // close issue
//            closeIssue(taskId);
//
//        } catch (Exception exception) {
//            JiraTestUtil.handleException(exception);
//        }
//    }
//
//    public void testJiraIssue() throws Throwable {
//        try {
//
//            // create
//            JiraIssue issue = JiraTestUtil.createJiraIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(),
//                    JiraTestUtil.TEST_PROJECT, "Kaputt", "Alles Kaputt!", "Bug");
//            assertNotNull(issue);
//            assertNotNull(issue.getId());
//            assertNotNull(issue.getKey());
//
//            // update
//            updateJiraIssue(issue);
//
//            // subtask
//            createJiraSubtask(issue);
//
//            // list
//            listJiraIssues(issue);
//
//            // add attachement
//            addAttachement(issue, "crap");
//
//            // delete attachement
//            readAttachement(issue, "crap");
//
//            // close issue
//            closeIssue(issue);
//
//        } catch (Exception exception) {
//            JiraTestUtil.handleException(exception);
//        }
//    }
//
//    public void testJiraIssueWorklog() throws Throwable {
//        try {
//
//            // create
//            JiraIssue issue = JiraTestUtil.createJiraIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(),
//                  JiraTestUtil.TEST_PROJECT, "Kaputt", "Alles Kaputt!", "Bug");
////            JiraIssue issue = getClient().getIssueByKey("TESTPROJECTWL-1", JiraTestUtil.nullProgressMonitor);
//            assertNotNull(issue);
//            assertNotNull(issue.getId());
//            assertNotNull(issue.getKey());
//
//
//            // update
//            updateJiraIssue(issue);
//            addJiraWorkLog(issue);
//            //updateJiraWorkLog(issue);
//
//            // close issue
//            closeIssue(issue);
//        } catch (Exception exception) {
//            JiraTestUtil.handleException(exception);
//        }
//    }
//
//    public void testJiraWorklogTaskData() throws Throwable {
//        try {
////            TaskData td = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), "TESTPROJECTWL-1");
//            RepositoryResponse rr = JiraTestUtil.createIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt", "Alles Kaputt!", "Bug");
//            assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
//            assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()));
//
//            String taskId = rr.getTaskId();
//            // update
//            updateTaskData(taskId);
//            addWorkLogTaskData(taskId);
//
//            // close issue
//            closeIssue(taskId);
//        } catch (Exception exception) {
//            JiraTestUtil.handleException(exception);
//        }
//    }
//
//    private void addAttachement(String taskId, String comment) throws CoreException, JiraException, Exception {
//        String key = getClient().getKeyFromId(taskId, JiraTestUtil.nullProgressMonitor);
//        Task task = new Task(JiraTestUtil.getTaskRepository().getRepositoryUrl(), JiraTestUtil.getTaskRepository().getConnectorKind(), key, taskId, "");
//        File f = getAttachmentFile(comment);
//
//        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
//        attachmentSource.setContentType("text/plain");
//
//        getRepositoryConnector().getTaskAttachmentHandler().postContent(JiraTestUtil.getTaskRepository(), task, attachmentSource, "attaching " + comment, null, JiraTestUtil.nullProgressMonitor);
//    }
//
//    private String addAttachement(JiraIssue issue, String comment) throws CoreException, JiraException, Exception {
//        File f = getAttachmentFile(comment);
//        getClient().addAttachment(issue, comment, f.getName(), comment.getBytes(), JiraTestUtil.nullProgressMonitor);
//        return f.getName();
//    }
//
//    private void createSubtask(String taskId) throws JiraException, CoreException {
//        TaskData parent = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
//        RepositoryResponse rr = JiraTestUtil.createSubtask(parent, getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt", "Alles Kaputt!");
//        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
//        assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()));
//        TaskData subtask = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId());
//        assertNotNull(subtask.getRoot().getMappedAttribute(JiraAttribute.PARENT_ID.id()));
//    }
//
//    private void readAttachement(String taskId, String content) throws CoreException, JiraException, IOException {
//        String key = getClient().getKeyFromId(taskId, JiraTestUtil.nullProgressMonitor);
//        Task task = new Task(JiraTestUtil.getTaskRepository().getRepositoryUrl(), JiraTestUtil.getTaskRepository().getConnectorKind(), key, taskId, "");
//
//        final TaskData taskData = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
//		List<TaskAttribute> attributes = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_ATTACHMENT);
//        TaskAttribute attribute = attributes.get(0);
//        InputStream attachement = getRepositoryConnector().getTaskAttachmentHandler().getContent(JiraTestUtil.getTaskRepository(), task, attribute, JiraTestUtil.nullProgressMonitor);
//        try {
//			byte[] data = new byte[4];
//			attachement.read(data);
//			assertEquals(content, new String(data));
//        } finally {
//			if(attachement != null) attachement.close();
//		}
//    }
//
//    private void readAttachement(JiraIssue issue, String content) throws CoreException, JiraException, IOException {
//        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        Attachment[] attachments = issue.getAttachments();
//        assertNotNull(attachments);
//        assertEquals(1, attachments.length);
//        Attachment attachment = attachments[0];
//        assertNotNull(attachment);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        getClient().getAttachment(issue, attachment, os, JiraTestUtil.nullProgressMonitor);
//        assertEquals(content, new String(os.toByteArray()));
//    }
//
//    private File getAttachmentFile(String content) throws Exception {
//        FileWriter fw = null;
//        try {
//            File f = new File("/tmp/attachement");
//            try {
//                f.createNewFile();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                // ignore
//            }
//            f.deleteOnExit();
//            fw = new FileWriter(f);
//            fw.write(content);
//            fw.flush();
//            return f;
//        } catch (IOException ioe) {
//            throw ioe;
//        } finally {
//            try { if (fw != null) fw.close(); } catch (IOException iOException) { }
//        }
//    }
//
//    private void updateTaskData(String taskId) throws Throwable {
//        TaskData data = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
//        TaskAttribute rta = data.getRoot();
//        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
//
//        String val = ta.getValue();
//        ta.setValue(val + " updated");
//
//        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
//        attrs.add(ta);
//        // edit estimated time
//        ta = rta.getAttribute(JiraAttribute.ESTIMATE.id());
//        long estimated = Long.parseLong(ta.getValue()) + 60 * 10;
//        ta.setValue(Long.toString(estimated));
//
//        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(JiraTestUtil.getTaskRepository(),data, attrs, JiraTestUtil.nullProgressMonitor);
//        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);
//
//        rta = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()).getRoot();
//        ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
//        assertEquals(val + " updated", ta.getValue());
//        ta = rta.getAttribute(JiraAttribute.ESTIMATE.id());
//        assertEquals(estimated, Long.parseLong(ta.getValue()));
//    }
//
//    private void updateJiraIssue(JiraIssue issue) throws Throwable {
//        Date updated1 = issue.getUpdated();
//
//        String oldSumary = issue.getSummary();
//        issue.setSummary(issue.getSummary() + " updated");
//        long estimatedTime = 60 * 10; // 10 minutes ETA
//        issue.setEstimate(estimatedTime);
//        getClient().updateIssue(issue, "changed summary", JiraTestUtil.nullProgressMonitor);
//
//        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        assertEquals(oldSumary + " updated", issue.getSummary());
//        Date updated2 = issue.getUpdated();
//        assertNotSame(updated1, updated2);
//        assertEquals(estimatedTime, issue.getEstimate());
//    }
//
//    private void closeIssue(String taskId) throws CoreException {
//        TaskData data = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
//        TaskAttribute rta = data.getRoot();
//
//        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);        
//        Resolution resolution = getResolutionByName("Fixed");
//        ta.setValue(resolution.getId());
//
//        ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
//        String key = getClient().getKeyFromId(taskId, JiraTestUtil.nullProgressMonitor);
//        JiraAction[] actions = getClient().getAvailableActions(key, JiraTestUtil.nullProgressMonitor);
//        JiraAction action = null;
//        for (JiraAction a : actions) {
//            if(a.getName().equals("Resolve Issue")) {
//                action = a;
//                break;
//            }
//        }
//        ta.setValue(action.getId());
//
//        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
//        attrs.add(ta);
//        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(JiraTestUtil.getTaskRepository(),data, attrs, JiraTestUtil.nullProgressMonitor);
//        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);
//
//        rta = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()).getRoot();
//        ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
//        assertEquals(resolution.getId(), ta.getValue());
//    }
//
//    private void closeIssue(JiraIssue issue) throws CoreException {
//        Resolution resolution = getResolutionByName("Fixed");
//        
//        issue.setResolution(resolution);
////		issue.setFixVersions(new Version[0]); // XXX set version
//        String resolveOperationId = getResolveOperation(issue.getKey());
//        assertNotNull(resolveOperationId);
//		getClient().advanceIssueWorkflow(issue, resolveOperationId, "shutup", JiraTestUtil.nullProgressMonitor);
//
//        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        assertEquals(resolution.getId(), issue.getResolution().getId());
//    }
//
//    public void list(String taskId) throws Throwable {
//        JiraClientFactory.getDefault().getJiraClient(JiraTestUtil.getTaskRepository()).getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
//
//        FilterDefinition fd = new FilterDefinition();
//        fd.setProjectFilter(new ProjectFilter(JiraTestUtil.getProject(getClient())));
//        fd.setStatusFilter(new StatusFilter(new JiraStatus[] {getStatusByName(getClient(), "Open")}));
//        fd.setIssueTypeFilter(new IssueTypeFilter(new IssueType[] {getIssueTypeByName("Bug")}));
//        JiraTestUtil.TestTaskDataCollector tdc = JiraTestUtil.list(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), fd);
//
//        assertTrue(tdc.data.size() > 0);
//        TaskData data = null;
//        for (TaskData d : tdc.data) {
//            if(d.getTaskId().equals(taskId)) {
//                data = d;
//                break;
//            }
//        }
//        assertNotNull(data);
//    }
//
//    public List<TaskData> list(ISynchronizationSession session) throws Throwable {
//        JiraClientFactory.getDefault().getJiraClient(JiraTestUtil.getTaskRepository()).getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
//
//        FilterDefinition fd = new FilterDefinition();
//        fd.setProjectFilter(new ProjectFilter(JiraTestUtil.getProject(getClient())));
//        fd.setStatusFilter(new StatusFilter(new JiraStatus[] {getStatusByName(getClient(), "Open")}));
//        fd.setIssueTypeFilter(new IssueTypeFilter(new IssueType[] {getIssueTypeByName("Bug")}));
//        JiraTestUtil.TestTaskDataCollector tdc = JiraTestUtil.list(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), fd, session);
//
//        assertTrue(tdc.data.size() > 0);
//        return tdc.data;
//    }
//
//    public void listJiraIssues(JiraIssue issue) throws Throwable {
//        JiraClientFactory.getDefault().getJiraClient(JiraTestUtil.getTaskRepository()).getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
//
//        FilterDefinition fd = new FilterDefinition();
//        fd.setProjectFilter(new ProjectFilter(JiraTestUtil.getProject(getClient())));
//        fd.setStatusFilter(new StatusFilter(new JiraStatus[] {getStatusByName(getClient(), "Open")}));
//        fd.setIssueTypeFilter(new IssueTypeFilter(new IssueType[] {getIssueTypeByName("Bug")}));
//        JiraCollector jc = new JiraCollector();
//        JiraTestUtil.getClient().findIssues(fd, jc, JiraTestUtil.nullProgressMonitor);
//
//        assertTrue(jc.issues.size() > 0);
//        for (JiraIssue i : jc.issues) {
//            if(i.getId().equals(issue.getId())) {
//                return;
//            }
//        }
//        fail("issue with id " + issue.getId() + " not found");
//    }
//
//    private JiraClient getClient() {
//        return JiraTestUtil.getClient();
//    }
//
//    private JiraRepositoryConnector getRepositoryConnector() {
//        return JiraTestUtil.getRepositoryConnector();
//    }
//
//    private IssueType getIssueTypeByName(String name) {
//        IssueType[] types = getClient().getCache().getIssueTypes();
//        for (IssueType type : types) {
//            if(type.getName().equals(name)) return type;
//        }
//        throw new IllegalStateException("Unknown type: " + name);
//    }
//
//    private Resolution getResolutionByName(String name) {
//        Resolution[] resolutions = getClient().getCache().getResolutions();
//        for (Resolution r : resolutions) {
//            if(r.getName().equals(name)) return r;
//        }
//        throw new IllegalStateException("Unknown type: " + name);
//    }
//
//    private JiraStatus getStatusByName(JiraClient client, String name) {
//        JiraStatus[] statuses = getClient().getCache().getStatuses();
//        for (JiraStatus status : statuses) {
//            if(status.getName().equals(name)) return status;
//        }
//        throw new IllegalStateException("Unknown status: " + name);
//    }
//
//    private void createJiraSubtask(JiraIssue parent) throws JiraException, CoreException {
//        assertNull(parent.getParentId());
//        JiraIssue issue = JiraTestUtil.createJiraSubtask(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(),
//                    JiraTestUtil.TEST_PROJECT, "Kaputt subtask", "Alles subKaputt!");
//        issue.setParentId(parent.getId());
//        JiraIssue subTask = getClient().createSubTask(issue, JiraTestUtil.nullProgressMonitor);
//        assertNotNull(subTask);
//        assertNotNull(subTask.getId());
//        assertNotNull(subTask.getKey());
//        assertNotNull(subTask.getParentId());
//    }
//
//    private void addJiraWorkLog(JiraIssue issue) throws JiraException {
//        long est = issue.getEstimate();
//        long actual = issue.getActual();
//        String workLogId;
//
//        JiraWorkLog workLog = new JiraWorkLog();
//        workLog.setStartDate(new Date());
//        workLog.setTimeSpent(5 * 60); // 5 minutes
//        workLog.setComment("Some work done");
//        workLog = getClient().addWorkLog(issue.getKey(), workLog, JiraTestUtil.nullProgressMonitor);
//        workLogId = workLog.getId();
//
//        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        assertEquals(actual = actual + workLog.getTimeSpent(), issue.getActual());
//        assertEquals(est < workLog.getTimeSpent() ? 0 : est - workLog.getTimeSpent(), issue.getEstimate());
//
//        JiraWorkLog[] workLogs = getClient().getWorklogs(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        assertEquals(workLogId, workLogs[workLogs.length - 1].getId());
//    }
//
//    private void addWorkLogTaskData(String taskId) throws CoreException {
//        TaskData data = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
//        TaskAttribute rta = data.getRoot();
//        TaskAttribute ta = rta.createMappedAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
//
//        TaskAttributeMapper mapper = data.getAttributeMapper();
//        final Long timeSpent = new Long(60 * 5);
//        mapper.setLongValue(ta.createMappedAttribute(WorkLogConverter.TIME_SPENT.key()), timeSpent);
//        final Date dateStarted = new Date();
//        mapper.setDateValue(ta.createMappedAttribute(WorkLogConverter.START_DATE.key()), dateStarted);
//        String comment = "Work log done";
//        mapper.setValue(ta.createMappedAttribute(WorkLogConverter.COMMENT.key()), comment);
//        mapper.setValue(ta.createMappedAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG), "true"); // NOI18N        
//        
//        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
//        attrs.add(ta);
//        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(JiraTestUtil.getTaskRepository(),data, attrs, JiraTestUtil.nullProgressMonitor);
//        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);
//
//        data = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId());
//        List<TaskAttribute> workLogs = data.getAttributeMapper().getAttributesByType(data, WorkLogConverter.TYPE_WORKLOG);
//        TaskAttribute workLog = workLogs.get(workLogs.size() - 1);
//        assertEquals(timeSpent, workLog.getTaskData().getAttributeMapper().getLongValue(workLog.getAttribute(WorkLogConverter.TIME_SPENT.key())));
//        assertEquals(dateStarted, workLog.getTaskData().getAttributeMapper().getDateValue(workLog.getAttribute(WorkLogConverter.START_DATE.key())));
//        assertEquals(comment, workLog.getTaskData().getAttributeMapper().getValue(workLog.getAttribute(WorkLogConverter.COMMENT.key())));
//    }
//
//    /*private void updateJiraWorkLog(JiraIssue issue) throws JiraException {
//        long est = issue.getEstimate();
//        long actual = issue.getActual();
//        JiraWorkLog[] workLogs = getClient().getWorklogs(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        assertNotSame(0, workLogs.length);
//        JiraWorkLog workLog = workLogs[workLogs.length - 1];
//        workLog.setComment(workLog.getComment() + " - updated");
//        workLog.setTimeSpent(workLog.getTimeSpent() + 60 * 5); // another 5 minutes
//        getClient().addWorkLog(issue.getKey(), workLog, JiraTestUtil.nullProgressMonitor);
//
//        workLogs = getClient().getWorklogs(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        assertNotSame(0, workLogs.length);
//        assertEquals(workLog.getId(), workLogs[workLogs.length - 1].getId());
//        assertEquals(workLog.getComment(), workLogs[workLogs.length - 1].getComment());
//        assertEquals(workLog.getTimeSpent(), workLogs[workLogs.length - 1].getTimeSpent());
//
//        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
//        assertEquals(actual = actual + workLog.getTimeSpent(), issue.getActual());
//        assertEquals(est < workLog.getTimeSpent() ? 0 : est - workLog.getTimeSpent(), issue.getEstimate());
//    }*/
//
//    private class JiraCollector implements IssueCollector {
//        private List<JiraIssue> issues = new ArrayList<JiraIssue>();
//        public void start() { }
//        public void done() { }
//        public void collectIssue(JiraIssue issue) {
//            issues.add(issue);
//        }
//        public boolean isCancelled() {
//            return false; // XXX
//        }
//        public int getMaxHits() {
//            return -1; // XXX
//        }
//    }
//
//    // XXX how to get task!!!
//    // XXX TaskTask isn't working - returns taskId instead of taskKey
//    private class Task extends AbstractTask {
//        private String key;
//
//        public Task(String repositoryUrl, String connectorKind, String key, String taskId, String summary) {
//            super(repositoryUrl, taskId, summary);
//            this.key = key;
//        }
//
//        @Override
//        public boolean isLocal() {
//            return true;
//        }
//
//        @Override
//        public String getConnectorKind() {
//            return JiraTestUtil.getTaskRepository().getRepositoryUrl();
//        }
//
//        @Override
//        public String getTaskKey() {
//            return key;
//        }
//    }
//
//    public String getResolveOperation(String issueKey) throws JiraException {
//        JiraAction[] operations = getClient().getAvailableActions(issueKey, null);
//        for (JiraAction action : operations) {
//                if (action.getName().toLowerCase().startsWith("resolve")) {
//                        return action.getId();
//                }
//        }
//        return null;
//    }
}

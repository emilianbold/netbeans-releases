/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.jira;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraAction;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author tomas
 */
public class JiraIssueTest extends NbTestCase {

    public JiraIssueTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        JiraCorePlugin jcp = new JiraCorePlugin();
        try {
            jcp.start(null);
        } catch (Exception ex) {
            throw ex;
        }
        // need this to initialize cache -> server defined status values & co
//        getClient().getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
        JiraTestUtil.cleanProject(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()));
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testJiraTaskData() throws Throwable {
        try {

            // create
            RepositoryResponse rr = JiraTestUtil.createIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt", "Alles Kaputt!", "Bug");
            assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
            assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()));

            String taskId = rr.getTaskId();

            // update
            updateTaskData(taskId);

            // subtask
            createSubtask(taskId);

            // list
            list(taskId);

            // add attachement
            addAttachement(taskId, "crap");

            // delete attachement
            readAttachement(taskId, "crap");

            // close issue
            closeIssue(taskId);

        } catch (Exception exception) {
            JiraTestUtil.handleException(exception);
        }
    }

    public void testJiraIssue() throws Throwable {
        try {

            // create
            JiraIssue issue = JiraTestUtil.createJiraIssue(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(),
                    JiraTestUtil.TEST_PROJECT, "Kaputt", "Alles Kaputt!", "Bug");
            assertNotNull(issue);
            assertNotNull(issue.getId());
            assertNotNull(issue.getKey());

            // update
            updateJiraIssue(issue);

            // subtask
            createJiraSubtask(issue);

            // list
            listJiraIssues(issue);

            // add attachement
            addAttachement(issue, "crap");

            // delete attachement
            readAttachement(issue, "crap");

            // close issue
            closeIssue(issue);

        } catch (Exception exception) {
            JiraTestUtil.handleException(exception);
        }
    }

    private void addAttachement(String taskId, String comment) throws CoreException, JiraException, Exception {
        String key = getClient().getKeyFromId(taskId, JiraTestUtil.nullProgressMonitor);
        Task task = new Task(JiraTestUtil.getTaskRepository().getRepositoryUrl(), JiraTestUtil.getTaskRepository().getConnectorKind(), key, taskId, "");
        File f = getAttachmentFile(comment);

        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
        attachmentSource.setContentType("text/plain");

        getRepositoryConnector().getTaskAttachmentHandler().postContent(JiraTestUtil.getTaskRepository(), task, attachmentSource, "attaching " + comment, null, JiraTestUtil.nullProgressMonitor);
    }

    private String addAttachement(JiraIssue issue, String comment) throws CoreException, JiraException, Exception {
        File f = getAttachmentFile(comment);
        getClient().addAttachment(issue, comment, f.getName(), f, "text/plain", JiraTestUtil.nullProgressMonitor);
        return f.getName();
    }

    private void createSubtask(String taskId) throws JiraException, CoreException {
        TaskData parent = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
        RepositoryResponse rr = JiraTestUtil.createSubtask(parent, getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(), JiraTestUtil.getProject(getClient()), "Kaputt", "Alles Kaputt!");
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
        assertNotNull(JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()));
        TaskData subtask = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId());
        assertNotNull(subtask.getRoot().getMappedAttribute(JiraAttribute.PARENT_ID.id()));
    }

    private void readAttachement(String taskId, String content) throws CoreException, JiraException, IOException {
        String key = getClient().getKeyFromId(taskId, JiraTestUtil.nullProgressMonitor);
        Task task = new Task(JiraTestUtil.getTaskRepository().getRepositoryUrl(), JiraTestUtil.getTaskRepository().getConnectorKind(), key, taskId, "");

        final TaskData taskData = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
		List<TaskAttribute> attributes = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_ATTACHMENT);
        TaskAttribute attribute = attributes.get(0);
        InputStream attachement = getRepositoryConnector().getTaskAttachmentHandler().getContent(JiraTestUtil.getTaskRepository(), task, attribute, JiraTestUtil.nullProgressMonitor);
        try {
			byte[] data = new byte[4];
			attachement.read(data);
			assertEquals(content, new String(data));
        } finally {
			if(attachement != null) attachement.close();
		}
    }

    private void readAttachement(JiraIssue issue, String content) throws CoreException, JiraException, IOException {
        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
        Attachment[] attachments = issue.getAttachments();
        assertNotNull(attachments);
        assertEquals(1, attachments.length);
        Attachment attachment = attachments[0];
        assertNotNull(attachment);
        byte[] bytes = getClient().getAttachment(issue, attachment, JiraTestUtil.nullProgressMonitor);
        assertEquals(content, new String(bytes));
    }

    private File getAttachmentFile(String content) throws Exception {
        FileWriter fw = null;
        try {
            File f = new File("/tmp/attachement");
            try {
                f.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // ignore
            }
            f.deleteOnExit();
            fw = new FileWriter(f);
            fw.write(content);
            fw.flush();
            return f;
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            try { if (fw != null) fw.close(); } catch (IOException iOException) { }
        }
    }

    private void updateTaskData(String taskId) throws Throwable {
        TaskData data = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);

        String val = ta.getValue();
        ta.setValue(val + " updated");

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        attrs.add(ta);
        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(JiraTestUtil.getTaskRepository(),data, attrs, JiraTestUtil.nullProgressMonitor);
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        rta = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()).getRoot();
        ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        assertEquals(val + " updated", ta.getValue());
    }

    private void updateJiraIssue(JiraIssue issue) throws Throwable {
        Date updated1 = issue.getUpdated();

        String oldSumary = issue.getSummary();
        issue.setSummary(issue.getSummary() + " updated");
        getClient().updateIssue(issue, "changed summary", JiraTestUtil.nullProgressMonitor);

        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
        assertEquals(oldSumary + " updated", issue.getSummary());
        Date updated2 = issue.getUpdated();
        assertNotSame(updated1, updated2);
    }

    private void closeIssue(String taskId) throws CoreException, JiraException {
        TaskData data = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), taskId);
        TaskAttribute rta = data.getRoot();

        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);        
        Resolution resolution = getResolutionByName("Fixed");
        ta.setValue(resolution.getId());

        ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        String key = getClient().getKeyFromId(taskId, JiraTestUtil.nullProgressMonitor);
        JiraAction[] actions = getClient().getAvailableActions(key, JiraTestUtil.nullProgressMonitor);
        JiraAction action = null;
        for (JiraAction a : actions) {
            if(a.getName().equals("Resolve Issue")) {
                action = a;
                break;
            }
        }
        ta.setValue(action.getId());

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        attrs.add(ta);
        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(JiraTestUtil.getTaskRepository(),data, attrs, JiraTestUtil.nullProgressMonitor);
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        rta = JiraTestUtil.getTaskData(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), rr.getTaskId()).getRoot();
        ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
        assertEquals(resolution.getId(), ta.getValue());
    }

    private void closeIssue(JiraIssue issue) throws CoreException, JiraException {
        Resolution resolution = getResolutionByName("Fixed");
        
        issue.setResolution(resolution);
//		issue.setFixVersions(new Version[0]); // XXX set version
        String resolveOperationId = getResolveOperation(issue.getKey());
        assertNotNull(resolveOperationId);
		getClient().advanceIssueWorkflow(issue, resolveOperationId, "shutup", JiraTestUtil.nullProgressMonitor);

        issue = getClient().getIssueByKey(issue.getKey(), JiraTestUtil.nullProgressMonitor);
        assertEquals(resolution.getId(), issue.getResolution().getId());
    }

    public void list(String taskId) throws Throwable {
        JiraClientFactory.getDefault().getJiraClient(JiraTestUtil.getTaskRepository()).getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);

        FilterDefinition fd = new FilterDefinition();
        fd.setProjectFilter(new ProjectFilter(JiraTestUtil.getProject(getClient())));
        fd.setStatusFilter(new StatusFilter(new JiraStatus[] {getStatusByName(getClient(), "Open")}));
        fd.setIssueTypeFilter(new IssueTypeFilter(new IssueType[] {getIssueTypeByName("Bug")}));
        JiraTestUtil.TestTaskDataCollector tdc = JiraTestUtil.list(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), fd);

        assertTrue(tdc.data.size() > 0);
        TaskData data = null;
        for (TaskData d : tdc.data) {
            if(d.getTaskId().equals(taskId)) {
                data = d;
                break;
            }
        }
        assertNotNull(data);
    }

    public void listJiraIssues(JiraIssue issue) throws Throwable {
        JiraClientFactory.getDefault().getJiraClient(JiraTestUtil.getTaskRepository()).getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);

        FilterDefinition fd = new FilterDefinition();
        fd.setProjectFilter(new ProjectFilter(JiraTestUtil.getProject(getClient())));
        fd.setStatusFilter(new StatusFilter(new JiraStatus[] {getStatusByName(getClient(), "Open")}));
        fd.setIssueTypeFilter(new IssueTypeFilter(new IssueType[] {getIssueTypeByName("Bug")}));
        JiraCollector jc = new JiraCollector();
        JiraTestUtil.getClient().findIssues(fd, jc, JiraTestUtil.nullProgressMonitor);

        assertTrue(jc.issues.size() > 0);
        for (JiraIssue i : jc.issues) {
            if(i.getId().equals(issue.getId())) {
                return;
            }
        }
        fail("issue with id " + issue.getId() + " not found");
    }

    private JiraClient getClient() {
        return JiraTestUtil.getClient();
    }

    private JiraRepositoryConnector getRepositoryConnector() {
        return JiraTestUtil.getRepositoryConnector();
    }

    private IssueType getIssueTypeByName(String name) {
        IssueType[] types = getClient().getCache().getIssueTypes();
        for (IssueType type : types) {
            if(type.getName().equals(name)) return type;
        }
        throw new IllegalStateException("Unknown type: " + name);
    }

    private Resolution getResolutionByName(String name) {
        Resolution[] resolutions = getClient().getCache().getResolutions();
        for (Resolution r : resolutions) {
            if(r.getName().equals(name)) return r;
        }
        throw new IllegalStateException("Unknown type: " + name);
    }

    private JiraStatus getStatusByName(JiraClient client, String name) {
        JiraStatus[] statuses = getClient().getCache().getStatuses();
        for (JiraStatus status : statuses) {
            if(status.getName().equals(name)) return status;
        }
        throw new IllegalStateException("Unknown status: " + name);
    }

    private void createJiraSubtask(JiraIssue parent) throws JiraException, CoreException {
        assertNull(parent.getParentId());
        JiraIssue issue = JiraTestUtil.createJiraSubtask(getRepositoryConnector(), JiraTestUtil.getTaskRepository(), getClient(),
                    JiraTestUtil.TEST_PROJECT, "Kaputt subtask", "Alles subKaputt!");
        issue.setParentId(parent.getId());
        JiraIssue subTask = getClient().createSubTask(issue, JiraTestUtil.nullProgressMonitor);
        assertNotNull(subTask);
        assertNotNull(subTask.getId());
        assertNotNull(subTask.getKey());
        assertNotNull(subTask.getParentId());
    }

    private class JiraCollector implements IssueCollector {
        private List<org.eclipse.mylyn.internal.jira.core.model.JiraIssue> issues = new ArrayList<org.eclipse.mylyn.internal.jira.core.model.JiraIssue>();
        public void start() { }
        public void done() { }
        public void collectIssue(org.eclipse.mylyn.internal.jira.core.model.JiraIssue issue) {
            issues.add(issue);
        }
        public boolean isCancelled() {
            return false; // XXX
        }
        public int getMaxHits() {
            return -1; // XXX
        }
    }

    // XXX how to get task!!!
    // XXX TaskTask isn't working - returns taskId instead of taskKey
    private class Task extends AbstractTask {
        private String key;

        public Task(String repositoryUrl, String connectorKind, String key, String taskId, String summary) {
            super(repositoryUrl, taskId, summary);
            this.key = key;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public String getConnectorKind() {
            return JiraTestUtil.getTaskRepository().getRepositoryUrl();
        }

        @Override
        public String getTaskKey() {
            return key;
        }
    }

    public String getResolveOperation(String issueKey) throws JiraException {
        JiraAction[] operations = getClient().getAvailableActions(issueKey, null);
        for (JiraAction action : operations) {
                if (action.getName().toLowerCase().startsWith("resolve")) {
                        return action.getId();
                }
        }
        return null;
    }
}

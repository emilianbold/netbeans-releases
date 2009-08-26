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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.jira.core.IJiraConstants;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.netbeans.modules.jira.repository.JiraRepository;

/**
 *
 * @author tomas
 */
public class JiraTestUtil {

    public static final String TEST_PROJECT = "TESTPROJECT";
    public static final String REPO_PASSWD  = "dilino";
    public static final String REPO_URL     = "http://localhost:8888";
    public static final String REPO_USER    = "tomas";

    public static NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    private static JiraClient client;
    private static JiraRepositoryConnector jrc;
    private static TaskRepository taskRepository;
    private static JiraRepository repository;

    public static RepositoryResponse createIssue(String summary, String desc, String typeName) throws CoreException, JiraException {
        return createIssue(getRepositoryConnector(), getRepository().getTaskRepository(), getClient(), getProject(getClient()), summary, desc, typeName);
    }

    public static RepositoryResponse createIssue(JiraRepositoryConnector rc, TaskRepository repository, JiraClient client, Project project, String summary, String desc, String typeName) throws CoreException, JiraException {
        // project
        project.setVersions(new Version[0]); // XXX HACK
        project.setComponents(new Component[0]);

        final TaskAttributeMapper attributeMapper = rc.getTaskDataHandler().getAttributeMapper(repository);
        final TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
        rc.getTaskDataHandler().initializeTaskData(data, client, project);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);
        ta = rta.createMappedAttribute(JiraAttribute.TYPE.id());
        ta.setValue(getIssueTypeByName(client, typeName).getId());
        ta = rta.createMappedAttribute(JiraAttribute.INITIAL_ESTIMATE.id());
        ta.setValue("600");

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return rc.getTaskDataHandler().postTaskData(repository, data, attrs, nullProgressMonitor);
    }

    public static RepositoryResponse createSubtask(TaskData parent, JiraRepositoryConnector rc, TaskRepository repository, JiraClient client, Project project, String summary, String desc) throws CoreException, JiraException {
        // project
        project.setVersions(new Version[0]); // XXX HACK
        project.setComponents(new Component[0]);

        final TaskAttributeMapper attributeMapper = rc.getTaskDataHandler().getAttributeMapper(repository);
        final TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
        rc.getTaskDataHandler().initializeTaskData(data, client, project);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);
        ta = rta.createMappedAttribute(JiraAttribute.TYPE.id());
        ta.getMetaData().putValue(IJiraConstants.META_SUB_TASK_TYPE, Boolean.toString(true));
        ta.setValue(getIssueTypeByName(client, "Sub-task").getId());
        ta = rta.createMappedAttribute(JiraAttribute.PARENT_ID.id());
        ta.setValue(parent.getTaskId());

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return rc.getTaskDataHandler().postTaskData(repository, data, attrs, nullProgressMonitor);
    }

    public static JiraIssue createJiraIssue(JiraRepositoryConnector rc, TaskRepository repository, JiraClient client, String projectID, String summary, String desc, String typeName) throws CoreException, JiraException {
        JiraIssue issue = new JiraIssue();
        issue.setReporter(REPO_USER);
        issue.setSummary(summary);
        issue.setDescription(desc);
        issue.setType(getIssueTypeByName(client, typeName));
        issue.setProject(getProject(client));
        issue.setPriority(getPriorityByName(client, "Blocker"));
        issue.setReporter(JiraTestUtil.REPO_USER);
        issue.setInitialEstimate(60 * 10);

        return client.createIssue(issue, nullProgressMonitor);
    }

    public static JiraIssue createJiraSubtask(JiraRepositoryConnector rc, TaskRepository repository, JiraClient client, String projectID, String summary, String desc) throws CoreException, JiraException {
        JiraIssue issue = new JiraIssue();
        issue.setReporter(REPO_USER);
        issue.setSummary(summary);
        issue.setDescription(desc);
        issue.setType(getIssueTypeByName(client, "Sub-task"));
        issue.setProject(getProject(client));
		issue.setPriority(getPriorityByName(client, "Blocker"));
        issue.setReporter(JiraTestUtil.REPO_USER);

        return issue;
    }

    public static TaskData getTaskData(JiraRepositoryConnector rc, TaskRepository repository, String taskId) throws CoreException {
        return rc.getTaskDataHandler().getTaskData(repository, taskId, JiraTestUtil.nullProgressMonitor);
    }

    public static TestTaskDataCollector list(JiraRepositoryConnector rc, TaskRepository repository, JiraFilter jf) {
        return list(rc, repository, jf, null);
    }

    public static TestTaskDataCollector list(JiraRepositoryConnector rc, TaskRepository repository, JiraFilter jf, ISynchronizationSession session) {
        final RepositoryQuery repositoryQuery = new RepositoryQuery(rc.getConnectorKind(), "query");
        JiraUtil.setQuery(repository, repositoryQuery, jf);
        TestTaskDataCollector tdc = new TestTaskDataCollector();
        rc.performQuery(repository, repositoryQuery, tdc, session, JiraTestUtil.nullProgressMonitor);
        return tdc;
    }


    public static void cleanProject(JiraRepositoryConnector rc, TaskRepository repository, JiraClient client, Project project) throws JiraException, CoreException {
        FilterDefinition fd = new FilterDefinition();
        fd.setProjectFilter(new ProjectFilter(project));
        TestTaskDataCollector tdc = list(rc, repository, fd);
        for (TaskData d : tdc.data) {
            delete(client, d.getTaskId());
        }
    }

    private static IssueType getIssueTypeByName(JiraClient client, String name) {
        IssueType[] types = client.getCache().getIssueTypes();
        for (IssueType issueType : types) {
            if(issueType.getName().equals(name)) return issueType;
        }
        return null;
    }

    private static Project getProjectByID(JiraClient client, String name) {
        return client.getCache().getProjectById(name);
//        getProjects();
//        for (Project p : projetcts) {
//            if(p.getId().equals(name)) return p;
//        }
//        return null;
    }

    private static Priority getPriorityByName(JiraClient client, String name) {
        Priority[] prios = client.getCache().getPriorities();
        for (Priority p : prios) {
            if(p.getName().equals(name)) return p;
        }
        return null;
    }

    private static void delete(JiraClient client, String taskId) throws JiraException, CoreException {
        String key = client.getKeyFromId(taskId, JiraTestUtil.nullProgressMonitor);
        JiraIssue issue = client.getIssueByKey(key, JiraTestUtil.nullProgressMonitor);
        client.deleteIssue(issue, JiraTestUtil.nullProgressMonitor);
    }

    public static class TestTaskDataCollector extends TaskDataCollector {
        List<TaskData> data = new ArrayList<TaskData>();
        @Override
        public void accept(TaskData taskdata) {
            this.data.add(taskdata);
        }
    }

    public static void handleException(Exception exception) throws Throwable {
        if (exception instanceof CoreException) {
            CoreException e = (CoreException) exception;
            IStatus status = e.getStatus();
            if (status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                throw new Exception(rs.getHtmlMessage());
            }
            if (e.getStatus().getException() != null) {
                throw e.getStatus().getException();
            }
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        throw exception;
    }

    public static Project getProject(JiraClient client) throws JiraException {
        // XXX query ???
        Project[] projects = client.getProjects(JiraTestUtil.nullProgressMonitor);
        for (Project p : projects) {
            if (p.getKey().equals(JiraTestUtil.TEST_PROJECT)) {
                return p;
            }
        }
        return null;
    }

    public static JiraClient getClient() {
        if(client == null) {
            client = JiraClientFactory.getDefault().getJiraClient(getTaskRepository());
        }
        return client;
    }

    public static JiraRepositoryConnector getRepositoryConnector() {
        if(jrc == null) {
            jrc = new JiraRepositoryConnector();
        }
        return jrc;
    }

    public static TaskRepository getTaskRepository() {
        if(taskRepository == null) {
            taskRepository = new TaskRepository("jira", JiraTestUtil.REPO_URL);
            AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD);
            taskRepository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
            taskRepository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
        }
        return taskRepository;
    }

    public static JiraRepository getRepository() {
        if(repository == null) {
            repository = new JiraRepository("jira", "jira", JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD, null, null);
        }
        return repository;
    }
}

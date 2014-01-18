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

package org.netbeans.modules.jira;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.jira.client.spi.Component;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider.JiraClient;
import static org.netbeans.modules.jira.client.spi.JiraConnectorProvider.Type.XMLRPC;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraConstants;
import org.netbeans.modules.jira.client.spi.JiraFilter;
import org.netbeans.modules.jira.client.spi.Priority;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.Version;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.issue.NbJiraIssueTest;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;

/**
 *
 * @author tomas
 */
public class JiraTestUtil {

    public static final String TEST_PROJECT = "TESTPROJECT";
    public static final String REPO_PASSWD  = "unittest";
    public static final String REPO_URL     = "http://bugtracking-test.cz.oracle.com:8090";
    public static final String REPO_USER    = "unittest";

    public static NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    private static JiraClient client;
    private static TaskRepository taskRepository;
    private static JiraRepository repository;

    public static NbJiraIssue createIssue(String summary, String desc, String typeName) {
        return createIssue(getProject(), summary, desc, typeName);
    }

    public static NbJiraIssue createIssue(String summary, String desc, String typeName, String estimate) {
        return createIssue(getProject(), summary, desc, typeName, estimate);
    }

    public static NbJiraIssue createIssue(Project p, String summary, String desc, String typeName) {
        return createIssue(p, summary, desc, typeName, null);
    }

    public static NbJiraIssue createIssue(String projectId, String summary, String desc, String typeName, String estimate) {
        return createIssue(getProject(projectId), summary, desc, typeName, estimate);
    }

    public static NbJiraIssue createIssue(Project p, String summary, String desc, String typeName, String estimate) {
        NbJiraIssue issue = (NbJiraIssue) getRepository().createIssue();
        issue.loadModel();
        issue.setFieldValue(NbJiraIssue.IssueField.PROJECT, p.getId());
        if (summary == null) {
            summary = "Summary " + System.currentTimeMillis();
        }
        if(desc == null) {
            desc = "Description for " + summary;
        }
        issue.setFieldValue(NbJiraIssue.IssueField.SUMMARY, summary);
        issue.setFieldValue(NbJiraIssue.IssueField.DESCRIPTION, desc);
        if(typeName != null) {
            issue.setFieldValue(NbJiraIssue.IssueField.TYPE, getIssueTypeByName(client, typeName).getId());
        }        
        if(estimate != null) {
            issue.setFieldValue(NbJiraIssue.IssueField.ESTIMATE, estimate);
        }
        
        assertTrue(issue.submitAndRefresh());
        
        String id = issue.getKey();
        assertNotNull(id);
        assertFalse(id.trim().equals(""));
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(summary, issue.getSummary());
        assertEquals(desc, issue.getDescription());
        assertEquals(NbJiraIssueTest.JiraIssueStatus.OPEN.statusName, issue.getJiraStatus().getName());
        return issue;
    }
    
//    public static RepositoryResponse createIssue(String summary, String desc, String typeName) throws CoreException {
//        return createIssue(getRepositoryConnector(), getRepository().getTaskRepository(), getClient(), getProject(getClient()), summary, desc, typeName);
//    }
//
//    public static RepositoryResponse createIssue(AbstractRepositoryConnector rc, TaskRepository repository, JiraClient client, Project project, String summary, String desc, String typeName) throws CoreException {
//        // project
//        project.setVersions(new Version[0]); // XXX HACK
//        project.setComponents(new Component[0]);
//
//        TaskAttributeMapper attributeMapper = rc.getTaskDataHandler().getAttributeMapper(repository);
//        TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
//        ITaskMapping mapping = rc.getTaskMapping(data);
//        rc.getTaskDataHandler().initializeTaskData(repository, data, mapping, new NullProgressMonitor());
//        TaskAttribute rta = data.getRoot();
//        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
//        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
//        ta.setValue(summary);
//        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
//        ta.setValue(desc);
//        ta = rta.createMappedAttribute(getJiraConstants().getJiraAttribute_TYPE_id());
//        ta.setValue(getIssueTypeByName(client, typeName).getId());
//        ta = rta.createMappedAttribute(getJiraConstants().getJiraAttribute_INITIAL_ESTIMATE_id());
//        ta.setValue("600");
//
//        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
//        return rc.getTaskDataHandler().postTaskData(repository, data, attrs, nullProgressMonitor);
//    }

    public static RepositoryResponse createSubtask(TaskData parent, AbstractRepositoryConnector rc, TaskRepository repository, JiraClient client, Project project, String summary, String desc) throws CoreException {
        // project
        project.setVersions(new Version[0]); // XXX HACK
        project.setComponents(new Component[0]);

        final TaskAttributeMapper attributeMapper = rc.getTaskDataHandler().getAttributeMapper(repository);
        final TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
        ITaskMapping mapping = rc.getTaskMapping(data);
        rc.getTaskDataHandler().initializeTaskData(repository, data, mapping, new NullProgressMonitor());
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);
        ta = rta.createMappedAttribute(getJiraConstants().getJiraAttribute_TYPE_id());
        ta.getMetaData().putValue(getJiraConstants().getMETA_SUB_TASK_TYPE(), Boolean.toString(true));
        ta.setValue(getIssueTypeByName(client, "Sub-task").getId());
        ta = rta.createMappedAttribute(getJiraConstants().getPARENT_ID_id());
        ta.setValue(parent.getTaskId());

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return rc.getTaskDataHandler().postTaskData(repository, data, attrs, nullProgressMonitor);
    }

//    public static JiraIssue createJiraIssue(AbstractRepositoryConnector rc, TaskRepository repository, JiraClient client, String projectID, String summary, String desc, String typeName) throws CoreException, JiraException {
//        JiraIssue issue = new JiraIssue();
//        issue.setReporter(REPO_USER);
//        issue.setSummary(summary);
//        issue.setDescription(desc);
//        issue.setType(getIssueTypeByName(client, typeName));
//        issue.setProject(getProject(client));
//        issue.setPriority(getPriorityByName(client, "Blocker"));
//        issue.setReporter(JiraTestUtil.REPO_USER);
//        issue.setInitialEstimate(60 * 10);
//
//        return client.createIssue(issue, nullProgressMonitor);
//    }

//    public static JiraIssue createJiraSubtask(AbstractRepositoryConnector rc, TaskRepository repository, JiraClient client, String projectID, String summary, String desc) throws CoreException, JiraException {
//        JiraIssue issue = new JiraIssue();
//        issue.setReporter(REPO_USER);
//        issue.setSummary(summary);
//        issue.setDescription(desc);
//        issue.setType(getIssueTypeByName(client, "Sub-task"));
//        issue.setProject(getProject(client));
//		issue.setPriority(getPriorityByName(client, "Blocker"));
//        issue.setReporter(JiraTestUtil.REPO_USER);
//
//        return issue;
//    }

    public static TaskData getTaskData(AbstractRepositoryConnector rc, TaskRepository repository, String taskId) throws CoreException {
        return rc.getTaskData(repository, taskId, JiraTestUtil.nullProgressMonitor);
    }

    private static TestTaskDataCollector list(JiraFilter jf) {
        return list(jf, null);
    }

    private static TestTaskDataCollector list(JiraFilter jf, ISynchronizationSession session) {
        final RepositoryQuery repositoryQuery = new RepositoryQuery(getRepositoryConnector().getConnectorKind(), "query");
        JiraConnectorSupport.getInstance().getConnector().setQuery(getTaskRepository(), repositoryQuery, jf);
        TestTaskDataCollector tdc = new TestTaskDataCollector();
        getRepositoryConnector().performQuery(getTaskRepository(), repositoryQuery, tdc, session, JiraTestUtil.nullProgressMonitor);
        return tdc;
    }

    public static void cleanProject(Project project) throws CoreException, IOException {
        JiraConnectorProvider cp = JiraConnectorSupport.getInstance().getConnector();
        FilterDefinition fd = cp.createFilterDefinition();
        fd.setProjectFilter(cp.createProjectFilter(project));
        TestTaskDataCollector tdc = list(fd);
        for (TaskData d : tdc.data) {
            getClient().delete(d.getTaskId());
        }
    }

    private static IssueType getIssueTypeByName(JiraClient client, String name) {
        IssueType[] types = client.getIssueTypes();
        for (IssueType issueType : types) {
            if(issueType.getName().equals(name)) return issueType;
        }
        return null;
    }

    private static Project getProjectByID(JiraClient client, String name) {
        return client.getProjectById(name);
//        getProjects();
//        for (Project p : projetcts) {
//            if(p.getId().equals(name)) return p;
//        }
//        return null;
    }

    private static Priority getPriorityByName(JiraClient client, String name) {
        Priority[] prios = client.getPriorities();
        for (Priority p : prios) {
            if(p.getName().equals(name)) return p;
        }
        return null;
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

    public static Project getProject() {
        return getProject(null);
            }
    public static Project getProject(String projectId) {
        if(projectId == null) {
            projectId = TEST_PROJECT;
        }
        // XXX query ???
        return getClient().getProjectByKey(projectId);
//        Project[] projects = getClient().getProjects();
//        for (Project p : projects) {
//            if (p.getKey().equals(projectName)) {
//                return p;
//            }
//        }
//        return null;
    }

    public static void initClient(File workDir) {
        if(client == null) {
            try {
                client = Jira.getInstance().getClient(getTaskRepository());
                if(!client.hasDetails()) {
                    Jira.getInstance().getRepositoryConnector().updateRepositoryConfiguration(getTaskRepository(), new NullProgressMonitor());
                }
            } catch (Throwable t) {
//                try {
//                    // lests asume it's not initialized yet
//                    JiraCorePlugin.initialize(new File(workDir, "jiraservercache"));
//                } catch (Exception e) {
//                    System.out.println("");
//                }
//                client = JiraClientFactory.getDefault().getJiraClient(getTaskRepository());
                }
            }            
        }
    
    public static JiraClient getClient() {
        assert client != null : "invoke init client first";
        return client;
    }

    public static AbstractRepositoryConnector getRepositoryConnector() {
        return Jira.getInstance().getRepositoryConnector();
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
            RepositoryInfo info = new RepositoryInfo("jira", JiraConnector.ID, JiraTestUtil.REPO_URL, "jira", "jira", JiraTestUtil.REPO_USER, null, JiraTestUtil.REPO_PASSWD.toCharArray() , null);
            repository = new JiraRepository(info);
        }
        return repository;
    }
    
    public static Query getQuery(JiraQuery jiraQuery) {
        return getQuery(JiraUtils.createRepository(jiraQuery.getRepository()), jiraQuery);
    }        
    
    private static Query getQuery(Repository repository, JiraQuery q) {
        RepositoryImpl repositoryImpl = APIAccessor.IMPL.getImpl(repository);
        QueryImpl impl = repositoryImpl.getQuery(q);
        if(impl == null) {
            return null;
        }
        return impl.getQuery();
    }        
    
    public static JiraConstants getJiraConstants() {
        return JiraConnectorSupport.getInstance().getConnector().getJiraConstants();
}
}

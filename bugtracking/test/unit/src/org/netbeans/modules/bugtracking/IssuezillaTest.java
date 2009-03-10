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

package org.netbeans.modules.bugtracking;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.junit.NbTestCase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author tomas
 * // XXX this is crap. 
 */
public class IssuezillaTest extends NbTestCase {
    public static final String TEST_PROJECT = "TESTPROJECT";

    private static final String REPO_PASSWD = "passwd";
    private static final String REPO_URL = "http://www.netbeans.org/nonav/issues/"; //http://www.netbeans.org/issues/";
    private static final String REPO_USER = "dilino";

    private NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    private TaskRepository repository;
//    private IssuezillaRepositoryConnector irc;
//    private IssuezillaClient client;

    public IssuezillaTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
//        startPlugin();
    }

    @Override
    protected void tearDown() throws Exception {        
    }

//    public void testJira() throws Throwable {
////        try {
//
//        TestTaskDataCollector tdc = new TestTaskDataCollector();
//        String qurl = "http://www.netbeans.org/nonav/issues/xml.cgi?issue_type=DEFECT&component=subversion&issue_status=NEW&issue_status=STARTED&issue_status=REOPENED&priority=P1&priority=P2&priority=P3&email1=&emailtype1=exact&emailassigned_to1=1&email2=&emailtype2=exact&emailreporter2=1&issueidtype=include&issue_id=&changedin=&votes=&chfieldfrom=&chfieldto=Now&chfieldvalue=&short_desc=&short_desc_type=substring&long_desc=&long_desc_type=substring&issue_file_loc=&issue_file_loc_type=substring&status_whiteboard=&status_whiteboard_type=substring&keywords=INCOMPLETE&keywords_type=notokens&field0-0-0=noop&type0-0-0=noop&value0-0-0=&cmdtype=doit&namedcmd=subversion-DEFECT&newqueryname=&order=Reuse+same+sort+as+last+time&Submit+query=Submit+query";
//       // String qurl = "http://www.netbeans.org/issues/xml.cgi?id=141964";
//        IssuezillaRepositoryQuery query = new IssuezillaRepositoryQuery("Q", qurl, REPO_URL, true);
//        IStatus r = getRepositoryConnector().performQuery(getRepository(), query, tdc, null, nullProgressMonitor);
//
//        if(r != null && r.getCode() != r.OK) {
//            Throwable t = r.getException();
//            if(r.getException() != null) {
//                throw t;
//            }
//            throw new Exception(r.getMessage());
//        }
//
//        assertTrue(tdc.data.size() > 0);
//        TaskData data = null;
//        for (TaskData d : tdc.data) {
//            if(d.getTaskId().equals("")) {
//                data = d;
//                break;
//            }
//        }
//        assertNotNull(data);
////
////
////        } catch (Exception exception) {
////            handleException(exception);
////        }
//    }
//
////    private void addAttachement(String taskId, String comment) throws CoreException, JiraException, Exception {
////        String key = getClient().getKeyFromId(taskId, nullProgressMonitor);
////        Task task = new Task(getRepository().getRepositoryUrl(), getRepository().getConnectorKind(), key, taskId, "");
////        File f = getAttachmentFile(comment);
////
////        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
////        attachmentSource.setContentType("text/plain");
////
////        getRepositoryConnector().getTaskAttachmentHandler().postContent(getRepository(), task, attachmentSource, "attaching " + comment, null, nullProgressMonitor);
////    }
////
////    private void readAttachement(String taskId) throws CoreException, JiraException, IOException {
////        String key = getClient().getKeyFromId(taskId, nullProgressMonitor);
////        Task task = new Task(getRepository().getRepositoryUrl(), getRepository().getConnectorKind(), key, taskId, "");
////
////        final TaskData taskData = getTaskData(taskId);
////		List<TaskAttribute> attributes = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_ATTACHMENT);
////        TaskAttribute attribute = attributes.get(0);
////        InputStream attachement = getRepositoryConnector().getTaskAttachmentHandler().getContent(getRepository(), task, attribute, nullProgressMonitor);
////        try {
////			byte[] data = new byte[4];
////			attachement.read(data);
////			assertEquals("crap", new String(data));
////        } finally {
////			if(attachement != null) attachement.close();
////		}
////    }
////
////    private void cleanProject(Project project) throws JiraException, CoreException {
////        FilterDefinition fd = new FilterDefinition();
////        fd.setProjectFilter(new ProjectFilter(project));
////        TestTaskDataCollector tdc = list(fd);
////        for (TaskData d : tdc.data) {
////            delete(d.getTaskId());
////        }
////    }
////
////    private void delete(String taskId) throws JiraException, CoreException {
////        String key = getClient().getKeyFromId(taskId, nullProgressMonitor);
////        JiraIssue issue = getClient().getIssueByKey(key, nullProgressMonitor);
////        getClient().deleteIssue(issue, nullProgressMonitor);
////    }
////
////    private File getAttachmentFile(String content) throws Exception {
////        FileWriter fw = null;
////        try {
////            File f = new File("/tmp/attachement");
////            try {
////                f.createNewFile();
////            } catch (IOException ex) {
////                ex.printStackTrace();
////                // ignore
////            }
////            f.deleteOnExit();
////            fw = new FileWriter(f);
////            fw.write(content);
////            fw.flush();
////            return f;
////        } catch (IOException ieo) {
////            throw ioe;
////        } finally {
////            try { if (fw != null) fw.close(); } catch (IOException iOException) { }
////        }
////    }
//
////    private TestTaskDataCollector list(FilterDefinition fd) {
////        final RepositoryQuery repositoryQuery = new RepositoryQuery(getRepositoryConnector().getConnectorKind(), "query");
////        JiraUtil.setQuery(getRepository(), repositoryQuery, fd);
////        TestTaskDataCollector tdc = new TestTaskDataCollector();
////        getRepositoryConnector().performQuery(getRepository(), repositoryQuery, tdc, null, nullProgressMonitor);
////        return tdc;
////    }
//
////    private void updateIssue(String taskId) throws Throwable {
////        TaskData data = getTaskData(taskId);
////        TaskAttribute rta = data.getRoot();
////        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
////
////        String val = ta.getValue();
////        ta.setValue(val + " updated");
////
////        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
////        attrs.add(ta);
////        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(getRepository(),data, attrs, nullProgressMonitor);
////        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);
////
////        rta = getTaskData(rr.getTaskId()).getRoot();
////        ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
////        assertEquals(val + " updated", ta.getValue());
////    }
////
////    private void closeIssue(String taskId) throws CoreException, JiraException {
////        TaskData data = getTaskData(taskId);
////        TaskAttribute rta = data.getRoot();
////
////        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
////        Resolution[] res = getClient().getResolutions(nullProgressMonitor);
////        Resolution resolution = null;
////        for (Resolution r : res) {
////            if(r.getName().equals("Fixed")) {
////                resolution = r;
////                break;
////            }
////        }
////        ta.setValue(resolution.getId());
////
////        ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
////        String key = getClient().getKeyFromId(taskId, nullProgressMonitor);
////        JiraAction[] actions = getClient().getAvailableActions(key, nullProgressMonitor);
////        JiraAction action = null;
////        for (JiraAction a : actions) {
////            if(a.getName().equals("Resolve Issue")) {
////                action = a;
////                break;
////            }
////        }
////        ta.setValue(action.getId());
////
////        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
////        attrs.add(ta);
////        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(getRepository(),data, attrs, nullProgressMonitor);
////        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);
////
////        rta = getTaskData(rr.getTaskId()).getRoot();
////        ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
////        assertEquals(resolution.getId(), ta.getValue());
////    }
////
////    public void list(String taskId) throws Throwable {
////        JiraClientFactory.getDefault().getJiraClient(getRepository()).getCache().refreshDetails(nullProgressMonitor);
////
////        FilterDefinition fd = new FilterDefinition();
////        fd.setProjectFilter(new ProjectFilter(getProject()));
////        fd.setStatusFilter(new StatusFilter(new JiraStatus[] {getStatusByName(getClient(), "Open")}));
////        fd.setIssueTypeFilter(new IssueTypeFilter(new IssueType[] {getIssueTypeByName("Bug")}));
////        TestTaskDataCollector tdc = list(fd);
////
////        assertTrue(tdc.data.size() > 0);
////        TaskData data = null;
////        for (TaskData d : tdc.data) {
////            if(d.getTaskId().equals(taskId)) {
////                data = d;
////                break;
////            }
////        }
////        assertNotNull(data);
////    }
//
////    private String createIssue() throws CoreException, JiraException {
////        // project
////        Project project = getProject();
////        project.setVersions(new Version[0]); // XXX HACK
////        project.setComponents(new Component[0]);
////
////        final TaskAttributeMapper attributeMapper = getRepositoryConnector().getTaskDataHandler().getAttributeMapper(getRepository());
////        final TaskData data = new TaskData(attributeMapper, getRepository().getConnectorKind(), getRepository().getRepositoryUrl(), "");
////        getRepositoryConnector().getTaskDataHandler().initializeTaskData(data, getClient(), project);
////        TaskAttribute rta = data.getRoot();
////        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
////        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
////        ta.setValue("Kaputt");
////        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
////        ta.setValue("Alles Kaputt!");
////        ta = rta.createMappedAttribute(JiraAttribute.TYPE.getName());
////        ta.setValue("Bug");
////
////        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
////        RepositoryResponse rr = getRepositoryConnector().getTaskDataHandler().postTaskData(getRepository(), data, attrs, nullProgressMonitor);
////
////        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
////        assertNotNull(getTaskData(rr.getTaskId()));
////        return rr.getTaskId();
////    }
//
//    private TaskData getTaskData(String taskId) throws CoreException {
//        return irc.getTaskDataHandler().getTaskData(getRepository(), taskId, nullProgressMonitor);
//    }
//
//    private IssuezillaRepositoryConnector getRepositoryConnector() {
//        if(irc == null) {
//            irc = new IssuezillaRepositoryConnector();
//        }
//        return irc;
//    }
//
////    private Project getProject() {
////        // XXX query ???
////        Project[] projects = getClient().get
////        for (Project p : projects) {
////            if (p.getKey().equals(TEST_PROJECT)) {
////                return p;
////            }
////        }
////        return null;
////    }
//
//    private IssuezillaClient getClient() {
//        if(client == null) {
//            client = new IssuezillaClientManager().getClient(getRepository());
//        }
//        return client;
//    }
//
//    private TaskRepository getRepository() {
//        if(repository == null) {
//            repository = new TaskRepository("isuezilla", REPO_URL);
//            AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(REPO_USER, REPO_PASSWD);
//            repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
//            repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
//        }
//        return repository;
//    }
//
//    private void handleException(Exception exception) throws Throwable, Exception {
//        if (exception instanceof CoreException) {
//            CoreException e = (CoreException) exception;
//            IStatus status = e.getStatus();
//            if (status instanceof RepositoryStatus) {
//                RepositoryStatus rs = (RepositoryStatus) status;
//                log(rs.getMessage());
//                log(rs.getHtmlMessage());
//                throw new Exception(rs.getHtmlMessage());
//            }
//            if (e.getStatus().getException() != null) {
//                throw e.getStatus().getException();
//            }
//            if (e.getCause() != null) {
//                throw e.getCause();
//            }
//            throw e;
//        }
//        throw exception;
//    }
//
//    private Bundle b = new Bundle() {
//        public Dictionary getHeaders() {
//            return null;
//        }
//        public BundleContext getBundleContext() {
//            return bc;
//        }
//    };
//    private BundleContext bc = new BundleContext() {
//        public Bundle getBundle() {
//            return b;
//        }
//    };
//    private void startPlugin() throws Exception {
//        IssuezillaCorePlugin icp = new IssuezillaCorePlugin();
//        try {
//            icp.start(bc);
//        } catch (Exception ex) {
//            throw ex;
//        }
//    }
//
//    private class TestTaskDataCollector extends TaskDataCollector {
//        List<TaskData> data = new ArrayList<TaskData>();
//        @Override
//        public void accept(TaskData taskdata) {
//            this.data.add(taskdata);
//        }
//    }
//
////    private IssueType getIssueTypeByName(String name) {
////        IssueType[] types = getClient().getCache().getIssueTypes();
////        for (IssueType type : types) {
////            if(type.getName().equals(name)) return type;
////        }
////        throw new IllegalStateException("Unknown type: " + name);
////    }
////
////    private JiraStatus getStatusByName(JiraClient client, String name) {
////        JiraStatus[] statuses = getClient().getCache().getStatuses();
////        for (JiraStatus status : statuses) {
////            if(status.getName().equals(name)) return status;
////        }
////        throw new IllegalStateException("Unknown status: " + name);
////    }
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
//            return getRepository().getRepositoryUrl();
//        }
//
//        @Override
//        public String getTaskKey() {
//            return key;
//        }
//    }
}

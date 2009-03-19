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

package org.netbeans.modules.bugzilla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTaskAttachmentHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;

/**
 *
 * @author tomas
 */
public class BugzillaQueryTest extends NbTestCase implements TestConstants {

    private TaskRepository repository;
    private BugzillaRepositoryConnector brc;
    private TaskRepositoryManager trm;

    public BugzillaQueryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        repository = new TaskRepository("bugzilla", REPO_URL);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(REPO_USER, REPO_PASSWD);
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);

        trm = new TaskRepositoryManager();
        brc = new BugzillaRepositoryConnector();

        trm.addRepository(repository);
        trm.addRepositoryConnector(brc);

        WebUtil.init();
    }

    public void testBugzilla() throws Throwable {


    }

    public static TaskData createIssue(BugzillaRepositoryConnector brc, TaskRepository repository, String summary, String desc, String typeName) throws CoreException, MalformedURLException {
        TaskAttributeMapper attributeMapper = brc.getTaskDataHandler().getAttributeMapper(repository);
        TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);

        BugzillaClient client = brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR);
        String os = client.getRepositoryConfiguration().getOSs().get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.OP_SYS.getKey());
        ta.setValue(os);

        String product = client.getRepositoryConfiguration().getProducts().get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.PRODUCT.getKey());
        ta.setValue(product);

        String platform = client.getRepositoryConfiguration().getPlatforms().get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.REP_PLATFORM.getKey());
        ta.setValue(platform);

        String version = client.getRepositoryConfiguration().getVersions(product).get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.VERSION.getKey());
        ta.setValue(version);

        String component = client.getRepositoryConfiguration().getComponents(product).get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.COMPONENT.getKey());
        ta.setValue(component);

//        ta = rta.createMappedAttribute(BugzillaAttribute.TYPE.getType());
//        ta.setValue(typeName);

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        // XXX what is this for
        RepositoryResponse rr = brc.getTaskDataHandler().postTaskData(repository, data, attrs, NULL_PROGRESS_MONITOR);

        String taskId = rr.getTaskId();
        data = brc.getTaskData(repository, taskId, NULL_PROGRESS_MONITOR);
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
        assertNotNull(data);    

        return data;
    }

    public static void handleException(Exception exception) throws Throwable {
        if (exception instanceof CoreException) {
            CoreException e = (CoreException) exception;
            IStatus status = e.getStatus();
            if (status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();
                if(html != null && !html.trim().equals("")) {
//                    HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
//                    if (displayer != null) {
//                        displayer.showURL (url);
//                    } else {
//                        //LOG.info("No URLDisplayer found.");
//                    }

                    final HtmlPanel p = new HtmlPanel();
                    p.setHtml(html);
                    BugzillaUtil.show(p, "html", "ok");
                }
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
        exception.printStackTrace();
        throw exception;
    }

    private void addComment(TaskRepository repository, TaskData data, String comment) throws CoreException {
        TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
        ta.setValue(comment);

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        attrs.add(ta);
        RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
        assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());
    }

    private void assertChanged(Task task, TaskData data, boolean changed) {
        boolean hasChanged = brc.hasTaskChanged(repository, task, data);
        assertEquals(changed, hasChanged);
    }


    private void listIssues(BugzillaRepositoryConnector brc, TaskRepository repository, TaskData data) throws UnsupportedEncodingException {
        String dateString = repository.getSynchronizationTimeStamp();
        if (dateString == null) {
            dateString = "";
        }
//
//        IRepositoryQuery query = new RepositoryQuery(repository.getConnectorKind(), "");
//        String urlQueryBase = repository.getRepositoryUrl() + CHANGED_BUGS_CGI_QUERY
//					+ URLEncoder.encode(dateString, repository.getCharacterEncoding()) + CHANGED_BUGS_CGI_ENDDATE;

		String url = "/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr";
        IRepositoryQuery query = new RepositoryQuery(repository.getConnectorKind(), "");
		query.setUrl(url);
        final List<TaskData> collectedData = new ArrayList<TaskData>();
        TaskDataCollector collector = new TaskDataCollector() {
			public void accept(TaskData taskData) {
				collectedData.add(taskData);
			}
		};
		brc.performQuery(repository, query, collector, null, NULL_PROGRESS_MONITOR);
        assertTrue(collectedData.size() > 0);
        String taskId = data.getTaskId();
        data = null;
        for (TaskData taskData : collectedData) {
            if(taskData.getTaskId().equals(taskId)) {
                data = taskData;
                break;
            }
        }
        assertNotNull(data);
    }

    private void readComment(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository, String comment) throws CoreException {
        // refresh
        data = brc.getTaskData(repository, data.getTaskId(), NULL_PROGRESS_MONITOR);

        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
        assertNotNull(attributes);
        
        boolean fail = true;
        for (TaskAttribute ta : attributes) {
            if(ta.getMappedAttribute(TaskAttribute.COMMENT_TEXT).getValue().equals(comment)) {
                fail = false;
                break;
            }
        }
        if(fail) {
            fail("Couldn't find comment text [" + comment + "] for taskdata [" + data.getTaskId() + "]");
        }
    }

    private void updateTaskData(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository) throws CoreException {
        data = brc.getTaskData(repository, data.getTaskId(), NULL_PROGRESS_MONITOR);
		TaskAttribute attrModification1 = data.getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);

        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        String val = ta.getValue();
        ta.setValue(val + " updated");
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        attrs.add(ta);
        RepositoryResponse rr = brc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        data = brc.getTaskData(repository, data.getTaskId(), NULL_PROGRESS_MONITOR);
        rta = data.getRoot();
        ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        assertEquals(val + " updated", ta.getValue());

        TaskAttribute attrModification2 = data.getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);
        assertNotSame(attrModification1, attrModification2);

    }

    private TaskData addAttachement(TaskData data,  BugzillaRepositoryConnector brc, TaskRepository repository, String comment, String desc, String content) throws Exception {
//        Task task = new Task(getRepository().getRepositoryUrl(), getRepository().getConnectorKind(), key, taskId, "");
        File f = getAttachmentFile(content);

        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
        attachmentSource.setContentType("text/plain");
        BugzillaTaskAttachmentHandler.AttachmentPartSource source = new BugzillaTaskAttachmentHandler.AttachmentPartSource(attachmentSource);

        String bugId = Integer.toString(BugzillaRepositoryConnector.getBugId(data.getTaskId()));
        brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR).postAttachment(bugId, comment, desc, attachmentSource.getContentType(), false, source, NULL_PROGRESS_MONITOR);

        data = brc.getTaskData(repository, data.getTaskId(), NULL_PROGRESS_MONITOR);
        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        assertTrue(attributes.size() > 0);
        return data;
    }

    private void readAttachement(TaskData data,  BugzillaRepositoryConnector brc, TaskRepository repository, String content) throws Exception {
        // refresh
        data = brc.getTaskData(repository, data.getTaskId(), NULL_PROGRESS_MONITOR);

        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        TaskAttribute attribute = attributes.get(0);
        TaskAttachmentMapper attachment = TaskAttachmentMapper.createFrom(attribute);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR).getAttachmentData(attachment.getAttachmentId(), os, NULL_PROGRESS_MONITOR);

        try {
//			byte[] d = new byte[4];
//			os.read(d);
			assertEquals(content, os.toString());
        } finally {
			if(os != null) os.close();
		}
    }

    private File getAttachmentFile(String content) throws Exception {
        FileWriter fw = null;
        File f = null;
        try {
            f = File.createTempFile("bugzillatest", null);
            f.deleteOnExit();
            try {
                f.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // ignore
            }
            fw = new FileWriter(f);
            fw.write(content);
            fw.flush();
            return f;
        } finally {
            try { if (fw != null) fw.close(); } catch (IOException iOException) { }
        }
    }

    private void closeIssue(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository) throws Exception {
        //RepositoryConfiguration rc = brc.getClientManager().getClient(repository, nullProgressMonitor).getRepositoryConfiguration();

        // refresh
        data = brc.getTaskData(repository, data.getTaskId(), NULL_PROGRESS_MONITOR);

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        TaskAttribute rta = data.getRoot();

        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue("resolve");
        attrs.add(ta);

        ta = rta.getMappedAttribute(TaskAttribute.STATUS);
        ta.setValue("FIXED");
        attrs.add(ta);

        RepositoryResponse rr = brc.getTaskDataHandler().postTaskData(repository, data, attrs, NULL_PROGRESS_MONITOR);
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        data = brc.getTaskData(repository, rr.getTaskId(), NULL_PROGRESS_MONITOR);
        rta = data.getRoot();
        ta = rta.getMappedAttribute(TaskAttribute.STATUS);
        assertEquals("RESOLVED", ta.getValue());
    }


    // XXX how to get task!!!
    // XXX TaskTask isn't working - returns taskId instead of taskKey
    private class Task extends AbstractTask {
        private String key;

        public Task(String repositoryUrl, String key, String taskId, String summary) {
            super(repositoryUrl, taskId, summary);
            this.key = key;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public String getConnectorKind() {
            return repository.getConnectorKind();
        }

        @Override
        public String getTaskKey() {
            return key;
        }
    }
}

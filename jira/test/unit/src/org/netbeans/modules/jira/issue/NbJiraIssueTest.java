/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jira.issue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.jira.*;
import java.util.logging.Level;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondra Vrabec
 */
public class NbJiraIssueTest extends NbTestCase {

    private static JiraRepository repository;
    protected static final String TEST_PROJECT = "NBJIRAISSUEDEV";
    protected static final String REPO_PASSWD = "unittest";
    protected static final String REPO_URL = "http://kenai-test.czech.sun.com:8090";
    protected static final String REPO_USER = "unittest";
    private JiraConfiguration config;
    private TaskRepository taskRepository;
    private static final String ATTACHMENTS_FOLDER = "attachments";     //NOI18N

    public enum JiraIssueResolutionStatus {

        FIXED("Fixed"),
        WONTFIX("Won't Fix"),
        DUPLICATE("Duplicate"),
        INCOMPLETE("Incomplete"),
        CANNOTREPRODUCE("Cannot Reproduce");
        public final String statusName;

        private JiraIssueResolutionStatus(String name) {
            this.statusName = name;
        }
    }

    public enum JiraIssueStatus {

        RESOLVED("Resolved"),
        OPEN("Open"),
        REOPENED("Reopened");
        public final String statusName;

        private JiraIssueStatus(String name) {
            this.statusName = name;
        }
    }

    public NbJiraIssueTest(String arg0) {
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
        //getClient().getCache().refreshDetails(JiraTestUtil.nullProgressMonitor);
        config = getRepository().getConfiguration();
        config.refreshDetails(new NullProgressMonitor());
        JiraTestUtil.cleanProject(getRepositoryConnector(), getRepository().getTaskRepository(), getClient(), config.getProjectByKey(TEST_PROJECT));
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testIssueCreate() throws JiraException, CoreException {
        assertNotNull(createIssue());
    }

    /**
     * <ul>
     * <li>Create issue</li>
     * <li>Resolve issue - fixed</li>
     * <li>Reopen issue</li>
     * <li>Resolve issue - wontfix</li>
     * <li>Reopen issue</li>
     * <li>Resolve issue - incomplete</li>
     * <li>Reopen issue</li>
     * <li>Resolve issue - cannot reproduce</li>
     * </ul>
     */
    public void testIssueCloseAndReopen() throws CoreException, JiraException {
        // TODO incomplete
        NbJiraIssue issue = createIssue();
        assertNotNull(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.FIXED);
        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.WONTFIX);
    }
    
    public void testAddComments() throws CoreException {
        int commentCount = 0;

        NbJiraIssue issue = createIssue();
        assertNotNull(issue);
        NbJiraIssue.Comment[] comments = issue.getComments();
        assertNotNull(comments);
        assertEquals(commentCount++, comments.length);

        addComment(issue);
        comments = issue.getComments();
        assertNotNull(comments);
        assertEquals(commentCount++, comments.length);

        addComment(issue);
        comments = issue.getComments();
        assertNotNull(comments);
        assertEquals(commentCount++, comments.length);

        addComment(issue);
        comments = issue.getComments();
        assertNotNull(comments);
        assertEquals(commentCount++, comments.length);
    }

    public void testAddAttachments () throws IOException, CoreException {
        NbJiraIssue issue = createIssue();
        issue.submitAndRefresh();
        
        File attachmentsDir = new File(getDataDir(), ATTACHMENTS_FOLDER);
        File[] attachmentFiles = attachmentsDir.listFiles();
        int attachNumber = 1;
        for (File attachmentFile : attachmentFiles) {
            addAttachment(issue, attachmentFile, attachNumber++);
        }
    }

    private NbJiraIssue createIssue() throws CoreException {
        NbJiraIssue issue = (NbJiraIssue) getRepository().createIssue();

        JiraRepositoryConnector rc = Jira.getInstance().getRepositoryConnector();
        Project p = config.getProjectByKey(TEST_PROJECT);

        String summary = "Summary " + System.currentTimeMillis();
        String description = "Description for " + summary;

        final TaskData data = issue.getTaskData();
        issue.setFieldValue(NbJiraIssue.IssueField.PROJECT, p.getKey());
        assertTrue(rc.getTaskDataHandler().initializeTaskData(getRepository().getTaskRepository(), data, rc.getTaskMapping(data), new NullProgressMonitor()));
        issue.setFieldValue(NbJiraIssue.IssueField.TYPE, "1");
        issue.setFieldValue(NbJiraIssue.IssueField.SUMMARY, summary);
        issue.setFieldValue(NbJiraIssue.IssueField.DESCRIPTION, description);

        submit(issue);

        String id = issue.getID();
        assertNotNull(id);
        assertFalse(id.trim().equals(""));

        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(summary, issue.getSummary());
        assertEquals(description, issue.getDescription());
        assertEquals(JiraIssueStatus.OPEN.statusName, issue.getStatus().getName());

        return issue;
    }

    private void addAttachment (NbJiraIssue issue, File attachmentFile, int attachmentCount) throws IOException {
        String key = issue.getID();
        String comment = "Comment for the attachment: " + attachmentFile.getName();
        String description = "Description for the attachment: " + attachmentFile.getName();
        String author = getRepository().getUsername();
        FileObject fo = FileUtil.toFileObject(attachmentFile);
        String contentType = fo.getMIMEType();
        long size = fo.getSize();
        issue.addAttachment(attachmentFile, comment, description, null, attachmentFile.getName().endsWith(".patch"));

        issue = (NbJiraIssue) repository.getIssue(key);
        assertNotNull(issue);
        NbJiraIssue.Attachment[] attachments = issue.getAttachments();
        assertNotNull(attachments);
        assertEquals(attachmentCount, attachments.length);

        NbJiraIssue.Attachment attachment = attachments[0];
        for (NbJiraIssue.Attachment att : attachments) {
            if (Long.parseLong(att.getId()) > Long.parseLong(attachment.getId())) {
                attachment = att;
            }
        }
        assertEquals(config.getUser(author).getFullName(), attachment.getAuthor());
        assertEquals(size, Integer.parseInt(attachment.getSize()));
        assertEquals(fo.getNameExt(), attachment.getDesc());
        assertNotNull(attachment.getDate());
        assertEquals(fo.getNameExt(), attachment.getFilename());
        assertNotNull(attachment.getUrl());
        assertTrue(attachment.getUrl().startsWith(repository.getUrl()));

        ByteArrayOutputStream bos = new ByteArrayOutputStream((int)size);
        attachment.getAttachementData(bos);
        assertEquals((int)size, bos.size());
        compare(attachmentFile, bos);
    }

    private static void compare (File file, ByteArrayOutputStream baos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream originalBaos = new ByteArrayOutputStream(baos.size());
        int len;
        byte[] buff = new byte[1024];
        while ((len = fis.read(buff)) != -1) {
            originalBaos.write(buff, 0, len);
        }
        int size = originalBaos.size();
        assertEquals(size, baos.size());
        byte[] originalCont = originalBaos.toByteArray();
        byte[] cont = baos.toByteArray();
        for (int i = 0; i < originalCont.length; ++i) {
            assertEquals(originalCont[i], cont[i]);
        }
    }

    private void resolveIssue(NbJiraIssue issue, JiraIssueResolutionStatus resolution) throws JiraException {
        String description = "resolving issue";
        issue.resolve(getResolutionByName(resolution.statusName), description);
        submit(issue);
        String id = issue.getID();
        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(JiraIssueStatus.RESOLVED.statusName, issue.getStatus().getName());
        assertEquals(resolution.statusName, issue.getResolution().getName());
    }

    private void reopenIssue(NbJiraIssue issue) throws JiraException {
        String description = "reopening issue";
        issue.reopen(description);
        submit(issue);
        String id = issue.getID();
        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(JiraIssueStatus.REOPENED.statusName, issue.getStatus().getName());
        assertNull(issue.getResolution());
    }

    private void addComment(NbJiraIssue issue) {
        String comment = "Comment for " + issue.getID() + " - " + System.currentTimeMillis();
        String id = issue.getID();
        String who = getRepository().getUsername();
        int commentsCount = issue.getComments().length;

        issue.addComment(comment);
        issue.submitAndRefresh();

        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        NbJiraIssue.Comment[] comments = issue.getComments();
        assertNotNull(comments);
        assertTrue(comments.length > 0);
        assertEquals(comment, comments[comments.length - 1].getText());
        assertEquals(config.getUser(who).getFullName(), comments[comments.length - 1].getWho());
        assertEquals(commentsCount + 1, comments[comments.length - 1].getNumber().intValue());
    }

    private JiraClient getClient() {
        return JiraTestUtil.getClient();
    }

    private JiraRepositoryConnector getRepositoryConnector() {
        return JiraTestUtil.getRepositoryConnector();
    }

    private void submit(NbJiraIssue issue) {
        assertTrue(issue.submitAndRefresh());
    }

    private JiraRepository getRepository() {
        if (repository == null) {
            repository = new JiraRepository("jira", REPO_URL, REPO_USER, REPO_PASSWD, null, null);
        }
        return repository;
    }

    private TaskRepository getTaskRepository() {
        if (taskRepository == null) {
            taskRepository = new TaskRepository("jira", JiraTestUtil.REPO_URL);
            AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD);
            taskRepository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
            taskRepository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
        }
        return taskRepository;
    }

    private Resolution getResolutionByName(String name) {
        Resolution[] resolutions = config.getResolutions();
        for (Resolution r : resolutions) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        throw new IllegalStateException("Unknown type: " + name);
    }
}

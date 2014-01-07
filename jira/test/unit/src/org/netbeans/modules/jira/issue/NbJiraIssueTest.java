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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jira.issue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.jira.*;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.jira.client.spi.Component;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider.JiraClient;
import org.netbeans.modules.jira.client.spi.Priority;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.Resolution;
import org.netbeans.modules.jira.client.spi.Version;
import org.netbeans.modules.jira.issue.NbJiraIssue.CustomField;
import org.netbeans.modules.jira.issue.NbJiraIssue.WorkLog;
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
    protected static final String REPO_URL = "http://bugtracking-test.cz.oracle.com:8090";
    protected static final String REPO_USER = "unittest";
//    protected static final String TEST_PROJECT = "OVRABEC_JIRA";
//    protected static final String REPO_PASSWD = "*****";
//    protected static final String REPO_URL = "http://testkenai.com/jira/";
//    protected static final String REPO_USER = "ovrabec";
    private JiraConfiguration config;
    private static final String ATTACHMENTS_FOLDER = "attachments";     //NOI18N
    private static final String TAG_FIELD = "Tags:";                    //NOI18N
    private static final String USER2 = "tester2";                      //NOI18N

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
        REOPENED("Reopened"),
        CLOSED("Closed"),
        INPROGRESS("In Progress");
        public final String statusName;

        private JiraIssueStatus(String name) {
            this.statusName = name;
        }
    }

    public NbJiraIssueTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        System.setProperty("netbeans.user", new File(getWorkDir(), "userdir").getAbsolutePath());
        System.setProperty("netbeans.t9y.jira.nb.config.path", System.getProperty("netbeans.user"));
        JiraTestUtil.initClient(getWorkDir());
        if (config == null) {
            config = getRepository().getConfiguration();
        }
        JiraTestUtil.cleanProject(config.getProjectByKey(TEST_PROJECT));
    }

    public static Test suite () {
        return NbModuleSuite.createConfiguration(NbJiraIssueTest.class).gui(false).suite();
    }

    public void testIssueCreate() throws CoreException {
        assertNotNull(createIssue());
    }

    /**
     * <ul>
     * <li>Create issue</li>
     * <li>Resolve issue - fixed</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - fixed</li>
     * <li>Close issue - fixed</li>
     *
     * <li>Reopen issue</li>
     * <li>Close issue - fixed</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - wontfix</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - wontfix</li>
     * <li>Close issue - wontfix</li>
     *
     * <li>Reopen issue</li>
     * <li>Close issue - wontfix</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - duplicate</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - duplicate</li>
     * <li>Close issue - duplicate</li>
     *
     * <li>Reopen issue</li>
     * <li>Close issue - duplicate</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - incomplete</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - incomplete</li>
     * <li>Close issue - incomplete</li>
     *
     * <li>Reopen issue</li>
     * <li>Close issue - incomplete</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - cannot reproduce</li>
     *
     * <li>Reopen issue</li>
     * <li>Resolve issue - cannot reproduce</li>
     * <li>Close issue - cannot reproduce</li>
     *
     * <li>Reopen issue</li>
     * <li>Close issue - cannot reproduce</li>
     *
     * <li>Reopen issue</li>
     * <li>Start progress</li>
     * <li>Stop progress</li>
     * <li>Close issue - fixed</li>
     * </ul>
     * @throws org.eclipse.core.runtime.CoreException
     */
    public void testIssueWorkflow() throws CoreException {
        NbJiraIssue issue = createIssue();
        assertNotNull(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.FIXED);

        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.FIXED);
        closeIssue(issue, JiraIssueResolutionStatus.FIXED);

        reopenIssue(issue);
        closeIssue(issue, JiraIssueResolutionStatus.FIXED);

        /********************/
        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.WONTFIX);

        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.WONTFIX);
        closeIssue(issue, JiraIssueResolutionStatus.WONTFIX);

        reopenIssue(issue);
        closeIssue(issue, JiraIssueResolutionStatus.WONTFIX);

        /********************/
        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.INCOMPLETE);

        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.INCOMPLETE);
        closeIssue(issue, JiraIssueResolutionStatus.INCOMPLETE);

        reopenIssue(issue);
        closeIssue(issue, JiraIssueResolutionStatus.INCOMPLETE);

        /********************/
        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.CANNOTREPRODUCE);

        reopenIssue(issue);
        resolveIssue(issue, JiraIssueResolutionStatus.CANNOTREPRODUCE);
        closeIssue(issue, JiraIssueResolutionStatus.CANNOTREPRODUCE);

        reopenIssue(issue);
        closeIssue(issue, JiraIssueResolutionStatus.CANNOTREPRODUCE);

        /*******************/
        reopenIssue(issue);
        assignTo(issue, USER2);
        assignToMe(issue);
        startIssueProgress(issue);
        stopIssueProgress(issue);
        closeIssue(issue, JiraIssueResolutionStatus.FIXED);
    }

    public void testIsFinished() throws CoreException {
        NbJiraIssue issue = createIssue();
        assertFalse(issue.isFinished());
        issue.resolve(getResolutionByName(JiraIssueResolutionStatus.FIXED.statusName), "fixed");
        issue.submitAndRefresh();
        assertTrue(issue.isFinished());
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

    public void testTagsFields () throws CoreException {
        NbJiraIssue issue = createIssue();
        NbJiraIssue.CustomField[] customFields = issue.getCustomFields();
        assertNotNull(customFields);
        assertTrue(customFields.length > 0);
        NbJiraIssue.CustomField customField = getTagField(customFields);
        assertNotNull(customField);
        assertFalse(customField.isReadOnly());
        List<String> values = customField.getValues();
        assertEquals(1, values.size());
        assertEquals("", values.get(0));

        String newValue = "sometag1 sometag2";                         //NOI18N
        values = new LinkedList<String>();
        values.add(newValue);
        customField.setValues(values);
        issue.setCustomField(customField);
        issue.submitAndRefresh();

        issue = (NbJiraIssue) getRepository().getIssue(issue.getKey());
        customFields = issue.getCustomFields();
        assertNotNull(customFields);
        assertTrue(customFields.length > 0);
        customField = getTagField(customFields);
        assertNotNull(customField);
        values = customField.getValues();
        assertEquals(1, values.size());
        assertEquals(newValue, values.get(0));

        newValue += " sometag3";                                        //NOI18N
        values = new LinkedList<String>();
        values.add(newValue);
        customField.setValues(values);
        issue.setCustomField(customField);
        issue.submitAndRefresh();

        issue = (NbJiraIssue) getRepository().getIssue(issue.getKey());
        customFields = issue.getCustomFields();
        assertNotNull(customFields);
        assertTrue(customFields.length > 0);
        customField = getTagField(customFields);
        assertNotNull(customField);
        values = customField.getValues();
        assertEquals(1, values.size());
        assertEquals(newValue, values.get(0));
    }

    public void testFields () throws CoreException {
        NbJiraIssue issue = createIssue();

        long estimate = Long.parseLong(issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE));
        String priority = issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY);
        String type = issue.getFieldValue(NbJiraIssue.IssueField.TYPE);
        List<String> affectedVersions = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
        List<String> fixedVersions = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
        List<String> components = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);

        long newEstimate = estimate + 10 * 60; // +10 minutes
        String newPriority = getAnotherPriority(priority);
        String newType = getAnotherType(type);
        affectedVersions = setVersions(affectedVersions);
        fixedVersions = setVersions(fixedVersions);
        Collections.sort(fixedVersions);
        components = setComponents(components);
        issue.setFieldValue(NbJiraIssue.IssueField.ESTIMATE, Long.toString(newEstimate));
        issue.setFieldValue(NbJiraIssue.IssueField.PRIORITY, newPriority);
        issue.setFieldValue(NbJiraIssue.IssueField.TYPE, newType);
        issue.setFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS, affectedVersions);
        issue.setFieldValues(NbJiraIssue.IssueField.FIXVERSIONS, fixedVersions);
        issue.setFieldValues(NbJiraIssue.IssueField.COMPONENT, components);

        submit(issue);

//        String id = issue.getKey();
//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);

        assertEquals(newType, issue.getFieldValue(NbJiraIssue.IssueField.TYPE));
        assertEquals(newPriority, issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY));
        assertEquals(newEstimate, Long.parseLong(issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE)));
        List<String> newAffectedVersions = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
        assertEquals(new HashSet<>(affectedVersions), new HashSet<>(newAffectedVersions));
        List<String> newFixedVersions = new ArrayList<>(issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS));
        Collections.sort(newFixedVersions);
        assertEquals(fixedVersions, newFixedVersions);
        List<String> newComponents = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
        assertEquals(components, newComponents);

        affectedVersions = setVersions(affectedVersions);
        fixedVersions = setVersions(fixedVersions);
        issue.setFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS, affectedVersions);
        issue.setFieldValues(NbJiraIssue.IssueField.FIXVERSIONS, fixedVersions);
        resolveIssue(issue, JiraIssueResolutionStatus.FIXED);

//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        newAffectedVersions = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
        assertEquals(affectedVersions, newAffectedVersions);
        newFixedVersions = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
        assertEquals(fixedVersions, newFixedVersions);
    }

    public void testWorkLogs () throws CoreException {
        NbJiraIssue issue = createIssue();
        for (int i = 1; i < 4; ++i) {
            addWorkLog(issue, i);
        }
    }

    private CustomField getTagField(CustomField[] customFields) {
        CustomField customField = null;
        for (NbJiraIssue.CustomField cf : customFields) {
            if (cf.getLabel().equals(TAG_FIELD)) {                           //NOI18N
                customField = cf;
                break;
            }
        }
        return customField;
    }

    private NbJiraIssue createIssue() throws CoreException {
        NbJiraIssue issue = JiraTestUtil.createIssue(TEST_PROJECT, (String) null, (String) null, "Bug", "600");
        return issue;
    }

    private void addAttachment (NbJiraIssue issue, File attachmentFile, int attachmentCount) throws IOException {
        String key = issue.getKey();
        String comment = "Comment for the attachment: " + attachmentFile.getName();
        String author = getRepository().getUsername();
        FileObject fo = FileUtil.toFileObject(attachmentFile);
        long size = fo.getSize();
        issue.addAttachment(attachmentFile, comment, null);

        issue = repository.getIssue(key);
        assertNotNull(issue);
        issue.loadModel();
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

    private void resolveIssue(NbJiraIssue issue, JiraIssueResolutionStatus resolution)  {
        String description = "resolving issue";
        issue.resolve(getResolutionByName(resolution.statusName), description);
        submit(issue);
//        String id = issue.getKey();
//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(JiraIssueStatus.RESOLVED.statusName, issue.getJiraStatus().getName());
        assertEquals(resolution.statusName, issue.getResolution().getName());
    }

    private void closeIssue(NbJiraIssue issue, JiraIssueResolutionStatus resolution) {
        String description = "closing issue";
        issue.close(getResolutionByName(resolution.statusName), description);
        submit(issue);
//        String id = issue.getKey();
//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(JiraIssueStatus.CLOSED.statusName, issue.getJiraStatus().getName());
        assertEquals(resolution.statusName, issue.getResolution().getName());
    }

    private void startIssueProgress(NbJiraIssue issue) {
        issue.startProgress();
        submit(issue);
//        String id = issue.getKey();
//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(JiraIssueStatus.INPROGRESS.statusName, issue.getJiraStatus().getName());
    }

    private void stopIssueProgress(NbJiraIssue issue) {
        issue.stopProgress();
        submit(issue);
//        String id = issue.getKey();
//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(JiraIssueStatus.OPEN.statusName, issue.getJiraStatus().getName());
    }

    private void assignTo (NbJiraIssue issue, String user) {
        issue.setFieldValue(NbJiraIssue.IssueField.ASSIGNEE, user);
        submit(issue);
//        String id = issue.getKey();
//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertEquals(user, issue.getFieldValue(NbJiraIssue.IssueField.ASSIGNEE));
    }

    private void assignToMe (NbJiraIssue issue) {
        assignTo(issue, getRepository().getUsername());
    }

    private List<String> setVersions (List<String> versions) {
        LinkedList<String> newVersions = new LinkedList<String>();
        Version[] allVersions = config.getVersions(config.getProjectByKey(TEST_PROJECT));
        for (Version v : allVersions) {
            if (!versions.contains(v.getId())) {
                newVersions.add(v.getId());
            }
        }
        if (newVersions.size() == allVersions.length) {
            newVersions.remove((int)Math.round((Math.random() * (allVersions.length - 1))));
        }
        return newVersions;
    }

    private List<String> setComponents (List<String> components) {
        LinkedList<String> newComponents = new LinkedList<String>();
        Component[] allComponents = config.getComponents(config.getProjectByKey(TEST_PROJECT));
        for (Component c : allComponents) {
            if (!components.contains(c.getId())) {
                newComponents.add(c.getId());
            }
        }
        return newComponents;
    }

    private void reopenIssue(NbJiraIssue issue) {
        String description = "reopening issue";
        issue.reopen(description);
        submit(issue);
//        String id = issue.getKey();
//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        assertEquals(JiraIssueStatus.REOPENED.statusName, issue.getJiraStatus().getName());
        assertNull(issue.getResolution());
    }

    private void addComment(NbJiraIssue issue) {
        String comment = "Comment for " + issue.getKey() + " - " + System.currentTimeMillis();
//        String id = issue.getKey();
        String who = getRepository().getUsername();
        int commentsCount = issue.getComments().length;

        issue.addComment(comment);
        issue.submitAndRefresh();

//        issue = (NbJiraIssue) getRepository().getIssueCache().getIssue(id);
        assertNotNull(issue.getKey());
        assertFalse("".equals(issue.getKey()));
        NbJiraIssue.Comment[] comments = issue.getComments();
        assertNotNull(comments);
        assertTrue(comments.length > 0);
        assertEquals(comment, comments[comments.length - 1].getText());
        assertEquals(config.getUser(who).getFullName(), comments[comments.length - 1].getWho());
        assertEquals(commentsCount + 1, comments[comments.length - 1].getNumber().intValue());
    }

    private void addWorkLog (NbJiraIssue issue, int worklogNumber) {
        WorkLog[] workLogs = issue.getWorkLogs();
        assertNotNull(workLogs);
        assertEquals(workLogs.length, worklogNumber - 1);
        String comment = "Worklog number " + worklogNumber;
        Date startDate = new Date();
        long timeSpent = 10 * 60; // 10 minutes
        NbJiraIssue.NewWorkLog log = new NbJiraIssue.NewWorkLog();
        log.setStartDate(startDate);
        log.setTimeSpent(timeSpent);
        log.setComment(comment);
        log.setAutoAdjust(true);
        log.setToSubmit(true);
        issue.addWorkLog(log);
        issue.submitAndRefresh();

        workLogs = issue.getWorkLogs();
        assertNotNull(workLogs);
        assertEquals(workLogs.length, worklogNumber);
        // our worklog should be the last???
        WorkLog workLog = workLogs[worklogNumber - 1];
        assertEquals(getRepository().getUsername(), workLog.getAuthor());
        assertEquals(timeSpent, workLog.getTimeSpent());
        assertEquals(comment, workLog.getComment());
        assertEquals(startDate.toString(), workLog.getStartDate().toString());
    }

    private void submit(NbJiraIssue issue) {
        assertTrue(issue.submitAndRefresh());
    }

    private JiraRepository getRepository() {
        if (repository == null) {
            RepositoryInfo info = new RepositoryInfo("jira", JiraConnector.ID, JiraTestUtil.REPO_URL, "jira", "jira", JiraTestUtil.REPO_USER, null, JiraTestUtil.REPO_PASSWD.toCharArray() , null);
            repository = new JiraRepository(info);
        }
        return repository;
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

    private String getAnotherPriority(String priority) {
        Priority[] priorities = config.getPriorities();
        for (Priority p : priorities) {
            if (!p.getId().equals(priority)) {
                return p.getId();
            }
        }

        return priority;
    }

    private String getAnotherType(String type) {
        IssueType[] types = config.getIssueTypes();
        for (IssueType t : types) {
            if (!t.getId().equals(type)) {
                return t.getId();
            }
        }

        return type;
    }
}

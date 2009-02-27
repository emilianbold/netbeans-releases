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

package org.netbeans.modules.bugzilla.issue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Attachment;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Comment;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.IssueField;

/**
 *
 * @author tomas
 */
public class IssueTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";

    public IssueTest(String arg0) {
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
    }

//    public void testSetFields() throws Throwable {
//        // WARNING: the test assumes that there are more than one value
//        // for atributes like platform, versions etc.
//
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//
//        String keyword = getKeyword(issue);
//        String milestone = getMilestone(issue);
//        String platform = getPlatform(issue);
//        String priority = getPriority(issue);
//        String resolution = getResolution(issue);
//        String version = getVersion(issue);
//        String assignee = "dil@dil.com";
//        String qaContact = "dil@dil.com";
//        String assigneeName = "dilino";
//        String qaContactName = "dilino";
//        String blocks = "1";
//        String depends = "2";
//        String cc = "dil@dil.com";
//        String url = "http://new.ulr";
//        String component = getComponent(issue);
//        String severity = getSeverity(issue);
//
//        // CC handled in separate test
//        // resolve handled in separate test
//        issue.setFieldValue(IssueField.ASSIGEND_TO,assignee);
//        issue.setFieldValue(IssueField.ASSIGNED_TO_NAME, assigneeName);
//        issue.setFieldValue(IssueField.BLOCKS, blocks);
//        issue.setFieldValue(IssueField.COMPONENT, component);
//        issue.setFieldValue(IssueField.DEPENDS_ON, depends);
//        issue.setFieldValue(IssueField.KEYWORDS, keyword);
//        issue.setFieldValue(IssueField.MILESTONE,milestone);
//        issue.setFieldValue(IssueField.PLATFORM,platform);
//        issue.setFieldValue(IssueField.PRIORITY, priority);
//        issue.setFieldValue(IssueField.QA_CONTACT, qaContact);
////        issue.setFieldValue(IssueField.QA_CONTACT_NAME, qaContactName);
////        issue.setFieldValue(IssueField.REPORTER, "dilino");
////        issue.setFieldValue(IssueField.RESOLUTION, resolution);
//        issue.setFieldValue(IssueField.SEVERITY, getSeverity(issue));
//        //issue.setFieldValue(IssueField.STATUS, getSeverity());
//        issue.setFieldValue(IssueField.SUMMARY, summary + ".new");
//        issue.setFieldValue(IssueField.URL, url);
//        issue.setFieldValue(IssueField.VERSION, version);
//
//        try {
//            issue.submit();
//        } catch (CoreException ex) {
//            TestUtil.handleException(ex);
//        }
//
//        issue.refresh();
//
//        assertEquals(assignee, issue.getFieldValue(IssueField.ASSIGEND_TO));
//        assertEquals(assigneeName, issue.getFieldValue(IssueField.ASSIGNED_TO_NAME));
//        assertEquals(blocks, issue.getFieldValue(IssueField.BLOCKS));
////        assertEquals(cc, issue.getFieldValue(IssueField.CC));
//        assertEquals(component, issue.getFieldValue(IssueField.COMPONENT));
//        assertEquals(depends, issue.getFieldValue(IssueField.DEPENDS_ON));
//        assertEquals(keyword, issue.getFieldValue(IssueField.KEYWORDS));
//        assertEquals(milestone, issue.getFieldValue(IssueField.MILESTONE));
//        assertEquals(platform, issue.getFieldValue(IssueField.PLATFORM));
//        assertEquals(priority, issue.getFieldValue(IssueField.PRIORITY));
//        assertEquals(qaContact, issue.getFieldValue(IssueField.QA_CONTACT));
////        assertEquals(qaContactName, issue.getFieldValue(IssueField.QA_CONTACT_NAME));
////        assertEquals(resolution, issue.getFieldValue(IssueField.RESOLUTION));
//        assertEquals(severity, issue.getFieldValue(IssueField.SEVERITY));
//        assertEquals(summary + ".new", issue.getFieldValue(IssueField.SUMMARY));
//        assertEquals(url, issue.getFieldValue(IssueField.URL));
//        assertEquals(version, issue.getFieldValue(IssueField.VERSION));
//
////        XXX changing a product might also imply the change of other fields!!!
////
////        String product = getProduct(issue);
////        issue.setFieldValue(IssueField.PRODUCT, product);
////        try {
////            issue.submit();
////        } catch (CoreException ex) {
////            TestUtil.handleException(ex);
////        }
////
////        issue.refresh();
////        assertEquals(product, issue.getFieldValue(IssueField.PRODUCT));
//
//    }
//
//    public void testCC() throws Throwable {
//        // WARNING: the test assumes that there are more than one value
//        // for atributes like platform, versions etc.
//
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//
//        // add a cc
//        issue.setFieldValue(IssueField.NEWCC, "dil@dil.com");
//        submit(issue);
//        issue.refresh();
//        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.CC));
//
//        // add new cc
//        issue.setFieldValue(IssueField.NEWCC, "dil2@dil.com");
//        submit(issue);
//        issue.refresh();
//        List<String> ccs = issue.getFieldValues(IssueField.CC);
//        assertEquals(2, ccs.size());
//        assertTrue(ccs.contains("dil@dil.com"));
//        assertTrue(ccs.contains("dil2@dil.com"));
//
//        // add two cc-s at once
//        issue.setFieldValue(IssueField.NEWCC, "dil3@dil.com, dil4@dil.com");
//        submit(issue);
//        issue.refresh();
//        ccs = issue.getFieldValues(IssueField.CC);
//        assertEquals(4, ccs.size());
//        assertTrue(ccs.contains("dil@dil.com"));
//        assertTrue(ccs.contains("dil2@dil.com"));
//        assertTrue(ccs.contains("dil3@dil.com"));
//        assertTrue(ccs.contains("dil4@dil.com"));
//
//        // remove a cc
//        ccs = new ArrayList<String>();
//        ccs.add("dil4@dil.com");
//        ccs.add("dil@dil.com");
//        issue.setFieldValues(IssueField.REMOVECC, ccs);
//        submit(issue);
//        issue.refresh();
//        ccs = issue.getFieldValues(IssueField.CC);
//        assertEquals(2, ccs.size());
//        assertTrue(ccs.contains("dil2@dil.com"));
//        assertTrue(ccs.contains("dil3@dil.com"));
//    }
//
//    public void testResolveFixedVerifiedClosedReopen() throws Throwable {
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));
//
//        issue.resolve("FIXED");
//        submit(issue);
//        issue.refresh();
//        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));
//
//        issue.verify();
//        submit(issue);
//        issue.refresh();
//        assertEquals("VERIFIED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));
//
//        issue.close();
//        submit(issue);
//        issue.refresh();
//        assertEquals("CLOSED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));
//
//        issue.reopen();
//        submit(issue);
//        issue.refresh();
//        assertEquals("REOPENED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));
//    }
//
//    public void testResolveDuplicateReopen() throws Throwable {
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
//
//        issue.duplicate("1");
//        submit(issue);
//        issue.refresh();
//
//        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("DUPLICATE", issue.getFieldValue(IssueField.RESOLUTION));
//
//        issue.reopen();
//        submit(issue);
//        issue.refresh();
//        assertEquals("REOPENED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));
//
//        // XXX get dupl ID
//
//    }
//
//    public void testReassign() throws Throwable {
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGEND_TO));
//
//        issue.reassigne("dil2@dil.com");
//        submit(issue);
//        issue.refresh();
//
//        assertEquals("dil2@dil.com", issue.getFieldValue(IssueField.ASSIGEND_TO));
//    }
//
//    public void testAddComment() throws Throwable {
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGEND_TO));
//
//        String comment = "koment";
//        issue.addComment(comment);
//        submit(issue);
//        issue.refresh();
//
//        Comment[] comments = issue.getComments();
//        assertEquals(1, comments.length);
//        assertEquals(comment, issue.getComments()[0].getText());
//    }
//
//    public void testAddCommentClose() throws Throwable {
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGEND_TO));
//
//        String comment = "koment";
//        issue.addComment(comment, true);
//        issue.refresh();
//
//        Comment[] comments = issue.getComments();
//        assertEquals(1, comments.length);
//        assertEquals(comment, issue.getComments()[0].getText());
//
//        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));
//    }


    public void testAddAttachement() throws Throwable {
        try {
            long ts = System.currentTimeMillis();
            String summary = "somary" + ts;
            String id = TestUtil.createIssue(getRepository(), summary);
            BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
            assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
            assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
            assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGEND_TO));

            String atttext = "my first attchement";
            String attcomment = "my first attchement";
            String attdesc = "file containing text";
            File f = getAttachmentFile(atttext);
            issue.addAttachment(f, attcomment, attdesc, "text/plain");
            issue.refresh();
            Attachment[] atts = issue.getAttachments();
            assertEquals(1, atts.length);
            assertEquals(attdesc, atts[0].getDesc());

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            atts[0].getAttachementData(os);
            String fileConttents = os.toString();
            assertEquals(atttext, fileConttents);
            
        } catch (Exception e) {
            TestUtil.handleException(e);
        }
    }

    private BugzillaRepository getRepository() {
        return TestUtil.getRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
    }

    private String getKeyword(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getKeywords(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.KEYWORDS));
    }

    private String getMilestone(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getTargetMilestones(getRepository(), TEST_PROJECT);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.MILESTONE));
    }

    private String getPlatform(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getPlatforms(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PLATFORM));
    }

    private String getProduct(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getProducts(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PRODUCT));
    }

    private String getPriority(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getPriorities(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PRIORITY));
    }

    private String getVersion(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getVersions(getRepository(), TEST_PROJECT);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.VERSION));
    }

    private String getSeverity(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getSeverities(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.SEVERITY));
    }

    private String getResolution(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getResolutions(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.RESOLUTION));
    }

    private String getComponent(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getComponents(getRepository(), TEST_PROJECT);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.RESOLUTION));
    }

    private String getDifferentServerValue(List<String> l, String v) {
        if(v != null) {
            for (String s : l) {
                if(!s.equals(v)) {
                    return s;
                }
            }
            fail("there is no different value then [" + v + "] on the server.");
        }
        return l.get(0);
    }

    private void submit(BugzillaIssue issue) throws Throwable {
        try {
            issue.submit();
        } catch (CoreException ex) {
            TestUtil.handleException(ex);
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
}

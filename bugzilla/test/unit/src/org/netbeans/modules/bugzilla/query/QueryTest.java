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

package org.netbeans.modules.bugzilla.query;

import org.netbeans.modules.bugzilla.*;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;

/**
 *
 * @author tomas
 */
public class QueryTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";
    private static String QUERY_NAME = "Hilarious";
    private static String PARAMETERS_FORMAT =
        "&short_desc_type=allwordssubstr&short_desc={0}" +
        "&long_desc_type=substring&long_desc=&bug_file_loc_type=allwordssubstr" +
        "&bug_file_loc=&status_whiteboard_type=allwordssubstr&status_whiteboard=" +
        "&keywords_type=allwords&keywords=&deadlinefrom=&deadlineto=&bug_status=NEW" +
        "&bug_status=ASSIGNED&bug_status=REOPENED&emailassigned_to1=1&emailtype1=substring" +
        "&email1=&emailassigned_to2=1&emailreporter2=1&emailqa_contact2=1&emailcc2=1" +
        "&emailtype2=substring&email2=&bugidtype=include&bug_id=&votes=&chfieldfrom=" +
        "&chfieldto=Now&chfieldvalue=&cmdtype=doit&order=Reuse+same+sort+as+last+time" + "" +
        "&field0-0-0=noop&type0-0-0=noop&value0-0-0=";

    public QueryTest(String arg0) {
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

    public void testRefresh() throws MalformedURLException, CoreException {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id1 = TestUtil.createIssue(getRepository(), summary);

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME, getRepository(), p, ts);
        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);

        nl.reset();
        q.refresh();
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(Query.ISSUE_STATUS_NOT_OBSOLETE).size());
        Issue i = nl.issues.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());

        nl.reset();
        q.refresh(p);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(Query.ISSUE_STATUS_NOT_OBSOLETE).size());
        i = nl.issues.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());
    }

    public void testGetIssues() throws MalformedURLException, CoreException {
        long ts = System.currentTimeMillis();
        String summary1 = "somary1" + ts;
        String id1 = TestUtil.createIssue(getRepository(), summary1);
        String summary2 = "somary2" + ts;
        String id2 = TestUtil.createIssue(getRepository(), summary2);

        // query for issue1
        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary1);
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME, getRepository(), p, ts);
        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);

        Issue[] issues = q.getIssues();
        assertEquals(0, nl.issues.size());

        nl.reset();
        q.refresh();
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(Query.ISSUE_STATUS_NOT_OBSOLETE).size());
        assertEquals(1, q.getIssues().length);
        Issue i = q.getIssues()[0];
        assertEquals(summary1, i.getSummary());
        assertEquals(id1, i.getID());

        nl.reset();
        q.refresh(p);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(Query.ISSUE_STATUS_NOT_OBSOLETE).size());
        assertEquals(1, q.getIssues().length);
        i = q.getIssues()[0];
        assertEquals(summary1, i.getSummary());
        assertEquals(id1, i.getID());

        // query for issue1 & issue2
        p =  MessageFormat.format(PARAMETERS_FORMAT, ts);
        nl.reset();
        q.refresh(p);
        issues = q.getIssues();
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(2, nl.getIssues(Query.ISSUE_STATUS_NOT_OBSOLETE).size());
        assertEquals(2, issues.length);
        List<String> summaries = new ArrayList<String>();
        List<String> ids = new ArrayList<String>();
        for(Issue issue : issues) {
            summaries.add(issue.getSummary());
            ids.add(issue.getID());
        }
        assertTrue(summaries.contains(summary1));
        assertTrue(summaries.contains(summary2));
        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));

        issues = q.getIssues("" + ts); // shoud return both issues
        assertEquals(2, issues.length);
        summaries = new ArrayList<String>();
        ids = new ArrayList<String>();
        for(Issue issue : issues) {
            summaries.add(issue.getSummary());
            ids.add(issue.getID());
        }
        assertTrue(summaries.contains(summary1));
        assertTrue(summaries.contains(summary2));
        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));

        issues = q.getIssues(summary1); // shoud return 1st issue
        assertEquals(1, issues.length);
        assertEquals(id1, issues[0].getID());
        assertEquals(summary1, issues[0].getSummary());
    }

    // XXX test obsolete status

    // XXX shoud be on the spi
    public void testLastRefresh() {
        BugzillaQuery q = new BugzillaQuery(getRepository());
        long lastRefresh = q.getLastRefresh();
        assertEquals(-1, lastRefresh);

        long ts = System.currentTimeMillis();

        ts = System.currentTimeMillis();
        q.refresh("whatver");
        assertTrue(q.getLastRefresh() >= ts);

        ts = System.currentTimeMillis();
        q.refresh();
        assertTrue(q.getLastRefresh() >= ts);

    }
    
    private BugzillaRepository getRepository() {
        return TestUtil.getRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
    }

}

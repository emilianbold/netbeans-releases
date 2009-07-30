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

package org.netbeans.modules.bugzilla.repository;

import org.netbeans.modules.bugzilla.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class RepositoryTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";
    private static String QUERY_NAME = "Hilarious";

    public RepositoryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testController() throws Throwable {
        BugzillaConnector bc = getConnector();
        BugzillaRepository repo = (BugzillaRepository) bc.createRepository();
        assertNotNull(repo);
        BugtrackingController c = repo.getController();
        assertNotNull(c);
        assertFalse(c.isValid());

        // populate
        // only name
        populate(c, REPO_NAME, "", "", "");
        assertFalse(c.isValid());

        // only url
        populate(c, "", REPO_URL, "", "");
        assertFalse(c.isValid());

        // only user
        populate(c, "", "", REPO_USER, "");
        assertFalse(c.isValid());

        // only passwd
        populate(c, "", "", "", REPO_PASSWD);
        assertFalse(c.isValid());

        // only user & passwd
        populate(c, "", "", REPO_USER, REPO_PASSWD);
        assertFalse(c.isValid());

        // name & url
        populate(c, REPO_NAME, REPO_URL, "", "");
        assertTrue(c.isValid());

        // full house
        populate(c, REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
        assertTrue(c.isValid());

        // no crap, its valid!
        c.applyChanges();
        try {
            Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(repo.getTaskRepository(), NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
        } catch (Exception ex) {
            TestUtil.handleException(ex);
        }
    }

    public void testRepo() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
        BugzillaRepository repo = new BugzillaRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD, null, null);

        // test queries
        Query[] queries = repo.getQueries();
        assertEquals(0, queries.length);

        Query q = repo.createQuery();
        queries = repo.getQueries();
        assertEquals(0, queries.length); // returns only saved queries

        // save query
        long lastRefresh = System.currentTimeMillis();
        String parameters = "&product=zaibatsu";
        BugzillaQuery bq = new BugzillaQuery(QUERY_NAME, repo, parameters, lastRefresh, false);
        repo.saveQuery(bq);
        queries = repo.getQueries();
        assertEquals(1, queries.length); // returns only saved queries

        // remove query
        repo.removeQuery(bq);
        queries = repo.getQueries();
        assertEquals(0, queries.length);

        // XXX repo.createIssue();

        // get issue
        String id = TestUtil.createIssue(repo, "somari");
        Issue i = repo.getIssue(id);
        assertNotNull(i);
        assertEquals(id, i.getID());
        assertEquals("somari", i.getSummary());
    }

    public void testSimpleSearch() throws MalformedURLException, CoreException {
        long ts = System.currentTimeMillis();
        String summary1 = "somary" + ts;
        String summary2 = "mary" + ts;
        BugzillaRepository repo = new BugzillaRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD, null, null);

        String id1 = TestUtil.createIssue(repo, summary1);
        String id2 = TestUtil.createIssue(repo, summary2);

        Issue[] issues = repo.simpleSearch(summary1);
        assertEquals(1, issues.length);
        assertEquals(summary1, issues[0].getSummary());

        issues = repo.simpleSearch(id1);
        // at least one as id might be also contained
        // in another issues summary
        assertTrue(issues.length > 0);
        Issue i = null;
        for(Issue issue : issues) {
            if(issue.getID().equals(id1)) {
                i = issue;
                break;
            }
        }
        assertNotNull(i);

        issues = repo.simpleSearch(summary2);
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
    }

    private BugzillaConnector getConnector() {
        Collection<BugtrackingConnector> c = (Collection<BugtrackingConnector>) Lookup.getDefault().lookupAll(BugtrackingConnector.class);
        BugzillaConnector bc = null;
        for (BugtrackingConnector bugtrackingConnector : c) {
            if(bugtrackingConnector instanceof BugzillaConnector) {
                bc = (BugzillaConnector) bugtrackingConnector;
                break;
            }
        }
        assertNotNull(bc);
        return bc;
    }

    private RepositoryPanel getRepositoryPanel(BugtrackingController c) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = c.getClass().getDeclaredField("panel");
        f.setAccessible(true);
        return (RepositoryPanel) f.get(c);
    }

    private void populate(BugtrackingController c, String name, String url, String user, String psswd) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        RepositoryPanel panel = getRepositoryPanel(c);
        resetPanel(panel);
        panel.nameField.setText(name);
        panel.urlField.setText(url);
        panel.userField.setText(user);
        panel.psswdField.setText(psswd);
    }

    private void resetPanel(RepositoryPanel panel) {
        panel.nameField.setText("");
        panel.urlField.setText("");
        panel.userField.setText("");
        panel.psswdField.setText("");
    }
}

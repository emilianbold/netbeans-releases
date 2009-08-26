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

package org.netbeans.modules.jira.repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientData;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.JiraTestUtil;
import org.netbeans.modules.jira.LogHandler;
import org.netbeans.modules.jira.query.JiraQuery;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class RepositoryTest extends NbTestCase {

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
        JiraCorePlugin bcp = new JiraCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JiraTestUtil.cleanProject(JiraTestUtil.getRepositoryConnector(), JiraTestUtil.getTaskRepository(), JiraTestUtil.getClient(), JiraTestUtil.getProject(JiraTestUtil.getClient()));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JiraConfig.getInstance().removeRepository(REPO_NAME);
    }


    public void testController() throws Throwable, NoSuchFieldException, NoSuchFieldException, NoSuchFieldException, NoSuchFieldException, NoSuchFieldException {
        RepositoryController c = getController();

        // populate
        // only name
        populate(c, REPO_NAME, "", "", "");
        assertFalse(c.isValid());

        // only url
        populate(c, "", JiraTestUtil.REPO_URL, "", "");
        assertFalse(c.isValid());

        // only user
        populate(c, "", "", JiraTestUtil.REPO_USER, "");
        assertFalse(c.isValid());

        // only passwd
        populate(c, "", "", "", JiraTestUtil.REPO_PASSWD);
        assertFalse(c.isValid());

        // only user & passwd
        populate(c, "", "", JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD);
        assertFalse(c.isValid());

        // wrong url format
        populate(c, REPO_NAME, "", JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD);
        assertFalse(c.isValid());

        populate(c, REPO_NAME, "crap", JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD);
        assertFalse(c.isValid());

        populate(c, REPO_NAME, "crap://crap", JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD);
        assertFalse(c.isValid());

        // name & url
        populate(c, REPO_NAME, JiraTestUtil.REPO_URL, "", "");
        assertTrue(c.isValid());

        // full house
        populate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD);
        assertTrue(c.isValid());
    }

    public void testControllerOnValidate() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
        RepositoryController c = getController();

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, null, JiraTestUtil.REPO_PASSWD, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, "", JiraTestUtil.REPO_PASSWD, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, "xxx", JiraTestUtil.REPO_PASSWD, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, null, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, "", false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, "xxx", false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD, true);
    }

    private void checkOnValidate(RepositoryController c, String repoName, String repoUrl, String user, String psswd, boolean assertWorked) throws Throwable {

        populate(c, repoName, repoUrl, user, psswd); //
        assertTrue(c.isValid());

        try {
            LogHandler lh = new LogHandler("validate for", LogHandler.Compare.STARTS_WITH);
            LogHandler lhAutoupdate = new LogHandler("JiraAutoupdate.checkAndNotify start", LogHandler.Compare.STARTS_WITH);
            onValidate(c);
            lh.waitUntilDone();
            assertFalse(lhAutoupdate.isDone());
            lhAutoupdate.reset();
            String msg = lh.getInterceptedMessage();
            boolean worked = msg.indexOf("worked") > -1;
            if(assertWorked) {
                assertTrue(worked);
            } else {
                assertFalse(worked);
            }
        } catch (Exception ex) {
            JiraTestUtil.handleException(ex);
        }
    }
//    public void testRepo() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
//        JiraRepository repo = new JiraRepository(REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD, null, null);
//
//        // test queries
//        Query[] queries = getLocalQueries(repo);
//        assertEquals(0, queries.length);
//
//        Query q = repo.createQuery();
//        queries = getLocalQueries(repo);
//        assertEquals(0, queries.length); // returns only saved queries
//
//        // save query
//        long lastRefresh = System.currentTimeMillis();
//        FilterDefinition fd = new FilterDefinition();
//        LogHandler lh = new LogHandler("Finnished populate query controller", LogHandler.Compare.STARTS_WITH, 60);
//        JiraQuery jq = new JiraQuery(QUERY_NAME, repo, fd, lastRefresh);
//        lh.waitUntilDone();
//        System.out.println("DONE2 !!!");
//        repo.saveQuery(jq);
//
//        queries = getLocalQueries(repo);
//        assertEquals(1, queries.length); // returns only saved queries
//
//        // remove query
//        repo.removeQuery(jq);
//        queries = getLocalQueries(repo);
//        assertEquals(0, queries.length);
//
//        // XXX repo.createIssue();
//
//        // get issue
//        RepositoryResponse rr = JiraTestUtil.createIssue("somari", "Trobleu", "Bug");
//        String id = rr.getTaskId();
//        Issue i = repo.getIssue(id);
//        assertNotNull(i);
//        assertEquals("somari", i.getSummary());
//    }

    public void testConfigurationData () throws NoSuchFieldException, NoSuchMethodException {
        Class configurationDataClass = JiraConfiguration.ConfigurationData.class;
        Field[] declaredFields = JiraClientData.class.getDeclaredFields();
        for (Field f : declaredFields) {
            if (Modifier.isPrivate(f.getModifiers())) {
                continue;
            }
            Field confDataField = configurationDataClass.getDeclaredField(f.getName());
            assertNotNull(confDataField);
            compareModifiers(f.getModifiers(), confDataField.getModifiers());
        }

        Method[] declaredMethods = JiraClientData.class.getDeclaredMethods();
        for (Method m : declaredMethods) {
            if (Modifier.isPrivate(m.getModifiers())) {
                continue;
            }
            Method confDataMethod = configurationDataClass.getDeclaredMethod(m.getName());
            assertNotNull(confDataMethod);
            compareModifiers(m.getModifiers(), confDataMethod.getModifiers());
        }
    }

    public void testSimpleSearch() throws MalformedURLException, CoreException, JiraException {
        long ts = System.currentTimeMillis();
        String summary1 = "somary";
        String summary2 = "somar";
        JiraRepository repo = new JiraRepository(REPO_NAME, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD, null, null);

        RepositoryResponse rr = JiraTestUtil.createIssue(JiraTestUtil.getRepositoryConnector(), JiraTestUtil.getTaskRepository(), JiraTestUtil.getClient(), JiraTestUtil.getProject(JiraTestUtil.getClient()), summary1, "Alles Kaputt!", "Bug");
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
        String id1 = rr.getTaskId();

        JiraTestUtil.createIssue(JiraTestUtil.getRepositoryConnector(), JiraTestUtil.getTaskRepository(), JiraTestUtil.getClient(), JiraTestUtil.getProject(JiraTestUtil.getClient()), summary2, "Alles Kaputt!", "Bug");
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
        String id2 = rr.getTaskId();

        Issue[] issues = repo.simpleSearch(summary1);
        assertEquals(1, issues.length);
        assertEquals(summary1, issues[0].getSummary());

        String key1 = getKey(repo, id1);
        String key2 = getKey(repo, id2);

        issues = repo.simpleSearch(key1);
        // at least one as id might be also contained
        // in another issues summary
        assertTrue(issues.length > 0);
        Issue i = null;
        for(Issue issue : issues) {
            if(issue.getID().equals(key1)) {
                i = issue;
                break;
            }
        }
        assertNotNull(i);

        issues = repo.simpleSearch(summary2.substring(0, summary2.length() - 2));
        assertEquals(2, issues.length);
        List<String> summaries = new ArrayList<String>();
        List<String> ids = new ArrayList<String>();
        for(Issue issue : issues) {
            summaries.add(issue.getSummary());
            ids.add(issue.getID());
        }
        assertTrue(summaries.contains(summary1));
        assertTrue(summaries.contains(summary2));
        assertTrue(ids.contains(key1));
        assertTrue(ids.contains(key2));
    }

    private JiraConnector getConnector() {
        Collection<BugtrackingConnector> c = (Collection<BugtrackingConnector>) Lookup.getDefault().lookupAll(BugtrackingConnector.class);
        JiraConnector bc = null;
        for (BugtrackingConnector bugtrackingConnector : c) {
            if(bugtrackingConnector instanceof JiraConnector) {
                bc = (JiraConnector) bugtrackingConnector;
                break;
            }
        }
        assertNotNull(bc);
        return bc;
    }

    private RepositoryController getController() {
        JiraConnector bc = getConnector();
        JiraRepository repo = (JiraRepository) bc.createRepository();
        assertNotNull(repo);
        RepositoryController c = (RepositoryController) repo.getController();
        assertNotNull(c);
        assertFalse(c.isValid());
        return c;
    }

    private Query[] getLocalQueries(JiraRepository repo) {
        Query[] queries = repo.getQueries();
        List<Query> ret = new ArrayList<Query>();
        for (Query query : queries) {
            JiraQuery jq = (JiraQuery) query;
            if(jq.getFilterDefinition() instanceof FilterDefinition) {
                ret.add(jq);
            }
        }
        return ret.toArray(new Query[ret.size()]);
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

    private void compareModifiers (int expected, int actual) {
        // public => public
        assertTrue(!Modifier.isPublic(expected) || Modifier.isPublic(actual));
        // protected => protected | public
        assertTrue(!Modifier.isProtected(expected) || Modifier.isPublic(actual) || Modifier.isProtected(actual));
        // package-private => public | package-private
        assertFalse(!(Modifier.isPrivate(expected) || Modifier.isProtected(expected) || Modifier.isPublic(expected))
                && (Modifier.isPrivate(actual) || Modifier.isProtected(actual)));
        // static must remain static and vice-versa
        assertEquals(Modifier.isStatic(expected), Modifier.isStatic(actual));
    }

    private String getKey(JiraRepository repo, String id1) {
        Issue i = repo.getIssue(id1);
        return i.getID();
    }

    private void onValidate(RepositoryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("onValidate");
        m.setAccessible(true);
        m.invoke(c);
    }
}

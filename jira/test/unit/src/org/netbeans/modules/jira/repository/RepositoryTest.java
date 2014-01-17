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

package org.netbeans.modules.jira.repository;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JTextField;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.JiraTestUtil;
import org.netbeans.modules.jira.LogHandler;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class RepositoryTest extends NbTestCase {

    private static String REPO_NAME;
    private static String QUERY_NAME = "Hilarious";

    public RepositoryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir(), "userdir").getAbsolutePath());
        super.setUp();
        REPO_NAME = "Beautiful-" + System.currentTimeMillis();
        JiraTestUtil.initClient(getWorkDir());
        JiraTestUtil.cleanProject(JiraTestUtil.getProject());
    }

    public static Test suite () {
        return NbModuleSuite.createConfiguration(RepositoryTest.class).gui(false).suite();
    }


    public void testController() throws Throwable, NoSuchFieldException, NoSuchFieldException, NoSuchFieldException, NoSuchFieldException, NoSuchFieldException {
        JiraRepositoryController c = getController();

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
        JiraRepositoryController c = getController();

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, null, JiraTestUtil.REPO_PASSWD, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, "", JiraTestUtil.REPO_PASSWD, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, "xxx", JiraTestUtil.REPO_PASSWD, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, null, false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, "", false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, "xxx", false);

        checkOnValidate(c, REPO_NAME, JiraTestUtil.REPO_URL, JiraTestUtil.REPO_USER, JiraTestUtil.REPO_PASSWD, true);
    }

    private void checkOnValidate(JiraRepositoryController c, String repoName, String repoUrl, String user, String psswd, boolean assertWorked) throws Throwable {

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
//        QueryProvider[] queries = getLocalQueries(repo);
//        assertEquals(0, queries.length);
//
//        QueryProvider q = repo.createQuery();
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
//        IssueProvider i = repo.getIssue(id);
//        assertNotNull(i);
//        assertEquals("somari", i.getSummary());
//    }

    public void testSimpleSearch() throws MalformedURLException, CoreException {
        String summary1 = "somary";
        String summary2 = "somar";
        RepositoryInfo info = new RepositoryInfo(REPO_NAME, JiraConnector.ID, JiraTestUtil.REPO_URL, REPO_NAME, REPO_NAME, JiraTestUtil.REPO_USER, null, JiraTestUtil.REPO_PASSWD.toCharArray() , null);
        JiraRepository repo = new JiraRepository(info);

        NbJiraIssue issue = JiraTestUtil.createIssue(summary1, "Alles Kaputt!", "Bug");
        String id1 = issue.getID();

        issue = JiraTestUtil.createIssue(summary2, "Alles Kaputt!", "Bug");
        String id2 = issue.getID();
        
        Collection<NbJiraIssue> issues = repo.simpleSearch(summary1);
        assertEquals(1, issues.size());
        assertEquals(summary1, issues.iterator().next().getSummary());

        String key1 = getKey(repo, id1);
        String key2 = getKey(repo, id2);

        issues = repo.simpleSearch(key1);
        // at least one as id might be also contained
        // in another issues summary
        assertTrue(issues.size() > 0);
        NbJiraIssue i = null;
        for(NbJiraIssue is : issues) {
            if(is.getKey().equals(key1)) {
                i = is;
                break;
            }
        }
        assertNotNull(i);

        issues = repo.simpleSearch(summary2.substring(0, summary2.length() - 1));
        assertEquals(2, issues.size());
        List<String> summaries = new ArrayList<String>();
        List<String> ids = new ArrayList<String>();
        for(NbJiraIssue is : issues) {
            summaries.add(is.getSummary());
            ids.add(is.getKey());
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

    private JiraRepositoryController getController() {
        JiraRepository repo = new JiraRepository();
        assertNotNull(repo);
        JiraRepositoryController c = (JiraRepositoryController) repo.getController();
        assertNotNull(c);
        assertFalse(c.isValid());
        return c;
    }

    private QueryProvider[] getLocalQueries(JiraRepository repo) {
        Collection<JiraQuery> queries = repo.getQueries();
        List<JiraQuery> ret = new ArrayList<JiraQuery>();
        for (JiraQuery query : queries) {
            if(query.getFilterDefinition() instanceof FilterDefinition) {
                ret.add(query);
            }
        }
        return ret.toArray(new QueryProvider[ret.size()]);
    }

    private RepositoryPanel getRepositoryPanel(RepositoryController c) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = c.getClass().getDeclaredField("panel");
        f.setAccessible(true);
        return (RepositoryPanel) f.get(c);
    }

    private void populate(RepositoryController c, String name, String url, String user, String psswd) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        RepositoryPanel panel = getRepositoryPanel(c);
        resetPanel(panel);
//        panel.nameField.setText(name);
        setText(panel, "nameField", name);
//        panel.urlField.setText(url);
        setText(panel, "urlField", url);
//        panel.userField.setText(user);
        setText(panel, "userField", user);
//        panel.psswdField.setText(psswd);
        setText(panel, "psswdField", psswd);
        Field f = JiraRepositoryController.class.getDeclaredField("populated");
        f.setAccessible(true);
        f.set(c, true);
    }

    private void resetPanel(RepositoryPanel panel) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//        panel.nameField.setText("");
        setText(panel, "nameField", "");
//        panel.urlField.setText("");
        setText(panel, "urlField", "");
//        panel.userField.setText("");
        setText(panel, "userField", "");
//        panel.psswdField.setText("");
        setText(panel, "psswdField", "");
    }
    
    private void setText (RepositoryPanel panel, String field, String value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = RepositoryPanel.class.getDeclaredField(field);
        f.setAccessible(true);
        ((JTextField) f.get(panel)).setText(value);
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
        NbJiraIssue i = repo.getIssue(id1);
        return i.getKey();
    }

    private void onValidate(JiraRepositoryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("onValidate");
        m.setAccessible(true);
        m.invoke(c);
    }
}

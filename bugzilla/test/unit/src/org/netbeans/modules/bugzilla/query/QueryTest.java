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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;

/**
 *
 * @author tomas
 */
public class QueryTest extends NbTestCase implements TestConstants, QueryConstants {

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
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cleanupStoredIssues();
    }

    public void testRefresh() throws MalformedURLException, CoreException, InterruptedException {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id1 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary);

        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME, QueryTestUtil.getRepository(), p, true, false, true);
        ts = System.currentTimeMillis();
        h.waitUntilDone();

        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);

        nl.reset();
        q.refresh();
        Issue[] is = q.getIssues();
        assertEquals(1, is.length);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        List<Issue> il = nl.getIssues(IssueCache.ISSUE_STATUS_ALL);
        assertEquals(1, il.size());
        Issue i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());

        nl.reset();
        q.refresh(p, false);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        il = nl.getIssues(IssueCache.ISSUE_STATUS_ALL);
        assertEquals(1, il.size());
        i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());
        is = q.getIssues();
        assertEquals(1, is.length);
    }

    public void testGetIssues() throws MalformedURLException, CoreException {
        long ts = System.currentTimeMillis();
        String summary1 = "somary1" + ts;
        String id1 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary1);
        String summary2 = "somary2" + ts;
        String id2 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary2);

        // query for issue1
        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary1);
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME, QueryTestUtil.getRepository(), p, true, false, true);
        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);

        Issue[] issues = q.getIssues();
        assertEquals(0, nl.issues.size());

        nl.reset();
        q.refresh();
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(IssueCache.ISSUE_STATUS_ALL).size());
        assertEquals(1, q.getIssues().length);
        Issue i = q.getIssues()[0];
        assertEquals(summary1, i.getSummary());
        assertEquals(id1, i.getID());

        nl.reset();
        q.refresh(p, false);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(IssueCache.ISSUE_STATUS_ALL).size());
        assertEquals(1, q.getIssues().length);
        i = q.getIssues()[0];
        assertEquals(summary1, i.getSummary());
        assertEquals(id1, i.getID());

        // query for issue1 & issue2
        p =  MessageFormat.format(PARAMETERS_FORMAT, ts);
        nl.reset();
        q.refresh(p, false);
        issues = q.getIssues();
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(2, nl.getIssues(IssueCache.ISSUE_STATUS_ALL).size());
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
        BugzillaQuery q = new BugzillaQuery(QueryTestUtil.getRepository());
        long lastRefresh = q.getLastRefresh();
        assertEquals(0, lastRefresh);

        long ts = System.currentTimeMillis();

        ts = System.currentTimeMillis();
        q.refresh("query_format=advanced&" +
                  "short_desc_type=allwordssubstr&" +
                  "short_desc=whatever112233445566778899&" +
                  "product=TestProduct", false);
        assertTrue(q.getLastRefresh() >= ts);

        ts = System.currentTimeMillis();
        q.refresh();
        assertTrue(q.getLastRefresh() >= ts);

    }

    public void testSaveAterSearch() throws MalformedURLException, CoreException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        BugzillaRepository repository = QueryTestUtil.getRepository();
        String id1 = TestUtil.createIssue( repository, summary);

        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);

        // create query
        BugzillaQuery q = new BugzillaQuery(repository);

        // get controler and wait until populated with default values
        QueryController c = q.getController();
        ts = System.currentTimeMillis();
        h.waitUntilDone();

        // populate with parameters - summary
        populate(c, summary);

        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);

        // search
        nl.reset();
        h = new LogHandler("refresh finish", LogHandler.Compare.STARTS_WITH);
        search(c); // search button and wait until done
        ts = System.currentTimeMillis();
        h.waitUntilDone();

        assertTrue(nl.started);
        assertTrue(nl.finished);
        List<Issue> il = nl.getIssues(IssueCache.ISSUE_STATUS_ALL);
        assertEquals(1, il.size());
        Issue i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());

        // save
        nl.reset();
        String name = QUERY_NAME + ts;
        h = new LogHandler(" saved", LogHandler.Compare.ENDS_WITH);
        save(c, name); // save button
        h.waitUntilDone();
        assertTrue(q.isSaved());
        // create a new repo instance and check if our query is between them
        repository = QueryTestUtil.getRepository();
        Query[] queries = repository.getQueries();
        boolean bl = false;
        for (Query query : queries) {
            bl = query.getDisplayName().equals(name);
            if(bl) break;
        }
        assertTrue(bl);
    }

    public void testSaveBeforeSearch() throws MalformedURLException, CoreException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id1 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary);

        LogHandler h = new LogHandler("Finnished populate ", LogHandler.Compare.STARTS_WITH);

        // create query
        BugzillaQuery q = new BugzillaQuery(QueryTestUtil.getRepository());

        // get controler and wait until populated with default values
        QueryController c = q.getController();
        h.waitUntilDone();

        // populate with parameters - summary
        populate(c, summary);

        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);
        nl.reset();
        QueryListener ql = new QueryListener();
        q.addPropertyChangeListener(ql);

        h = new LogHandler("refresh finish", LogHandler.Compare.STARTS_WITH); // we wan't to check
                                                                              // if the refresh is made after save
        save(c, QUERY_NAME + ts); // save button
        h.waitUntilDone();
        assertEquals(1, ql.saved);

        assertTrue(nl.started);
        assertTrue(nl.finished);
        List<Issue> il = nl.getIssues(IssueCache.ISSUE_STATUS_ALL);
        assertEquals(1, il.size());
        Issue i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());
    }

    public void testSaveRemove() throws MalformedURLException, CoreException, InterruptedException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        long ts = System.currentTimeMillis();

        // create query
        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);
        BugzillaQuery q = new BugzillaQuery(QueryTestUtil.getRepository());

        // get controler and wait until populated with default values
        QueryController c = q.getController();
        h.waitUntilDone();

        Query[] qs = QueryTestUtil.getRepository().getQueries();
        int queriesCount = qs.length;

        QueryListener ql = new QueryListener();
        q.addPropertyChangeListener(ql);
        // save
        h = new LogHandler(" saved", LogHandler.Compare.ENDS_WITH);
        save(c, QUERY_NAME + ts);
        h.waitUntilDone();
        assertEquals(1, ql.saved);

        qs = QueryTestUtil.getRepository().getQueries();
        assertEquals(queriesCount + 1, qs.length);

        // remove
        remove(c);
        assertEquals(1, ql.removed);
        qs = QueryTestUtil.getRepository().getQueries();
        assertEquals(queriesCount, qs.length);
    }

    private void save(QueryController c, String name) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("save", String.class);
        m.setAccessible(true);
        m.invoke(c, name);
    }

    private void remove(QueryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("remove");
        m.setAccessible(true);
        m.invoke(c);
    }

    private void search(QueryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("onRefresh");
        m.setAccessible(true);
        m.invoke(c);
    }

    private void populate(QueryController c, String summary) {
        QueryPanel p = (QueryPanel) c.getComponent();
        p.summaryTextField.setText(summary);
        p.productList.getSelectionModel().clearSelection(); // no product
    }

    private void cleanupStoredIssues() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        QueryTestUtil.getRepository().getIssueCache().storeArchivedQueryIssues(QUERY_NAME, new String[0]);
        QueryTestUtil.getRepository().getIssueCache().storeQueryIssues(QUERY_NAME, new String[0]);
    }

    private class QueryListener implements PropertyChangeListener {
        int saved = 0;
        int removed = 0;
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(Query.EVENT_QUERY_REMOVED)) {
                removed++;
            }
            if(evt.getPropertyName().equals(Query.EVENT_QUERY_SAVED)) {
                saved++;
            }
        }
        void reset() {
            saved = 0;
            removed = 0;
        }

    }
}

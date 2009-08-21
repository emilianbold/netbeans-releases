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

package org.netbeans.modules.jira.query;

import javax.swing.ListModel;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.netbeans.modules.jira.*;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.kenai.KenaiQuery;
import org.netbeans.modules.jira.repository.JiraRepository;

/**
 *
 * @author tomas
 */
public class QueryRefreshTest extends NbTestCase {

    public QueryRefreshTest(String arg0) {
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
        BugtrackingManager.getInstance();
        // need this to initialize cache -> server defined status values & co
        JiraTestUtil.cleanProject(JiraTestUtil.getRepositoryConnector(), JiraTestUtil.getTaskRepository(), JiraTestUtil.getClient(), JiraTestUtil.getProject(JiraTestUtil.getClient()));        
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testQueryOpenNoRefresh() throws Throwable {
        final String summary = "summary" + System.currentTimeMillis();
        final String queryName = "refreshtest";
        RepositoryResponse rr = JiraTestUtil.createIssue(summary, "desc", "Bug");
        assertNotNull(rr.getTaskId());

        JiraRepository repo = JiraTestUtil.getRepository();
        FilterDefinition fd = new FilterDefinition();
        fd.setContentFilter(new ContentFilter(summary, true, true, true, true));
        final JiraQuery jq = new JiraQuery( queryName, repo, fd, true, true);
        assertEquals(0,jq.getIssues().length);

        LogHandler lh = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH);
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                // init columndescriptors before opening query to prevent some "do not call in awt asserts"
                NbJiraIssue.getColumnDescriptors(JiraTestUtil.getRepository());
                QueryAction.openQuery(jq);
            }
        }).waitFinished();
        assertFalse(lh.isDone());    // but this one wasn't yet
    }

    public void testQueryOpenAndRefresh() throws Throwable {
        final String summary = "summary" + System.currentTimeMillis();
        final String queryName = "refreshtest";

        JiraRepository repo = JiraTestUtil.getRepository();
        FilterDefinition fd = new FilterDefinition();
        fd.setContentFilter(new ContentFilter(summary, true, false, false, false));
        final JiraQuery jq = new JiraQuery( queryName, repo, fd, true, true);
//        selectTestProject(jq);
        assertEquals(0, jq.getIssues().length);
        jq.refresh(); // refresh the query - so it won't be refreshed via first time open

        Issue[] issues = jq.getIssues();
        assertEquals(0, issues.length);

        RepositoryResponse rr = JiraTestUtil.createIssue(summary, "desc", "Bug");
        assertNotNull(rr.getTaskId());

        JiraConfig.getInstance().setQueryRefreshInterval(1); // 1 minute
        JiraConfig.getInstance().setQueryAutoRefresh(queryName, true);

        LogHandler refreshHandler = new LogHandler("refresh finish -", LogHandler.Compare.STARTS_WITH, 120);
        LogHandler schedulingHandler = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH, 120);
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                // init columndescriptors before opening query to prevent some "do not call in awt asserts"
                NbJiraIssue.getColumnDescriptors(JiraTestUtil.getRepository());
                QueryAction.openQuery(jq);
            }
        }).waitFinished();
        schedulingHandler.waitUntilDone();
        refreshHandler.waitUntilDone();

        assertTrue(schedulingHandler.isDone());
        assertTrue(refreshHandler.isDone());

        issues = jq.getIssues();
        assertEquals(1, issues.length);
    }

    public void testKenaiQueryNoAutoRefresh() throws Throwable {
        final String summary = "summary" + System.currentTimeMillis();
        final String queryName = "refreshtest";
        JiraConfig.getInstance().setQueryRefreshInterval(0); // would mean refresh imediately
        JiraConfig.getInstance().setQueryAutoRefresh(queryName, false);

        LogHandler schedulingHandler = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH, 120);

        // create query
        JiraRepository repo = JiraTestUtil.getRepository();
        FilterDefinition fd = new FilterDefinition();
        fd.setContentFilter(new ContentFilter(summary, true, false, false, false));
        final JiraQuery jq = new KenaiQuery(queryName, repo, fd, JiraTestUtil.TEST_PROJECT, true, false);

        // query was created yet it wasn't refreshed
        assertFalse(schedulingHandler.isDone());

    }

    // XXX can't get this running
//    public void testKenaiQueryAutoRefresh() throws Throwable {
//        final String summary = "summary" + System.currentTimeMillis();
//        final String queryName = "refreshtest";
//
//        JiraConfig.getInstance().setQueryRefreshInterval(1); // 1 minute
//        JiraConfig.getInstance().setQueryAutoRefresh(queryName, true);
//
//        // create issue
//        RepositoryResponse rr = JiraTestUtil.createIssue(summary, "desc", "Bug");
//        assertNotNull(rr.getTaskId());
//
//        // create query
//        JiraRepository repo = JiraTestUtil.getRepository();
//        FilterDefinition fd = new FilterDefinition();
//        fd.setContentFilter(new ContentFilter(summary, true, false, false, false));
//        LogHandler populateHandler = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);
//        LogHandler refreshHandler = new LogHandler("refresh finish -", LogHandler.Compare.STARTS_WITH, 120);
//        LogHandler schedulingHandler = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH, 120);
//        final JiraQuery jq = new KenaiQuery(queryName, repo, fd, JiraTestUtil.TEST_PROJECT, true, false);
//        populateHandler.waitUntilDone();
//        selectTestProject(jq);
//        Issue[] issues = jq.getIssues();
//        assertEquals(0, issues.length);
//
//        // kenai queries are auto refreshed no matter if they are open or not, so
//        // we don't have to do anythink with the query - just wait until it gets refreshed.
//
//        schedulingHandler.waitUntilDone();
//        refreshHandler.waitUntilDone();
//
//        assertTrue(schedulingHandler.isDone());
//        assertTrue(refreshHandler.isDone());
//
//        issues = jq.getIssues();
//        assertEquals(1, issues.length);
//    }

    private void selectTestProject(final JiraQuery jq) {
        QueryPanel panel = (QueryPanel) jq.getController().getComponent();
        ListModel model = panel.projectList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Project project = (Project) model.getElementAt(i);
            if (project.getKey().equals(JiraTestUtil.TEST_PROJECT)) {
                panel.projectList.setSelectedIndex(i);
                break;
            }
        }
    }

}

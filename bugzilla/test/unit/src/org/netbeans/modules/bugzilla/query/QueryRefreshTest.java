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

package org.netbeans.modules.bugzilla.query;

import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.LogHandler;
import org.netbeans.modules.bugzilla.TestConstants;
import org.netbeans.modules.bugzilla.TestUtil;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.issue.BugzillaIssueProvider;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author tomas
 */
public class QueryRefreshTest extends NbTestCase implements TestConstants, QueryConstants {

    public QueryRefreshTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        // bypass bugtracking owner logic
        System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
        
        // refresh faster
        System.setProperty("netbeans.t9y.bugzilla.force.refresh.schedule", "60000");
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testQueryOpenNoRefresh() throws Throwable {
        long ts = System.currentTimeMillis();
        final String summary = "summary" + System.currentTimeMillis();

        final BugzillaRepository repo = QueryTestUtil.getRepository();
        String id = TestUtil.createIssue(repo, summary);
        assertNotNull(id);

        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        final BugzillaQuery q = new BugzillaQuery(QUERY_NAME, repo, p, false, false, true);
        ts = System.currentTimeMillis();
        h.waitUntilDone();


        LogHandler lh = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH);
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                // init columndescriptors before opening query to prevent some "do not call in awt asserts"
                BugzillaIssue.getColumnDescriptors(repo);
                QueryAction.openQuery(q, null);
            }
        }).waitFinished();
        assertFalse(lh.isDone());    // but this one wasn't yet
    }

    @RandomlyFails
    public void testQueryOpenAndRefresh() throws Throwable {
        long ts = System.currentTimeMillis();
        final String summary = "summary" + System.currentTimeMillis();

        final BugzillaRepository repo = QueryTestUtil.getRepository();
        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        final BugzillaQuery q = new BugzillaQuery(QUERY_NAME, repo, p, true, false, true);
        ts = System.currentTimeMillis();
        h.waitUntilDone();
        QueryTestUtil.selectTestProject(q);
        assertEquals(0, q.getIssues().length);
        q.refresh(); // refresh the query - so it won't be refreshed via first time open

        Issue[] issues = q.getIssues();
        assertEquals(0, issues.length);

        String id = TestUtil.createIssue(repo, summary);
        assertNotNull(id);

        BugzillaConfig.getInstance().setQueryAutoRefresh(QUERY_NAME, true);
        
        LogHandler refreshHandler = new LogHandler("refresh finish -", LogHandler.Compare.STARTS_WITH, 120);
        LogHandler schedulingHandler = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH, 120);
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                // init columndescriptors before opening query to prevent some "do not call in awt asserts"
                BugzillaIssue.getColumnDescriptors(repo);
                QueryAction.openQuery(q, null);
            }
        }).waitFinished();
        schedulingHandler.waitUntilDone();
        refreshHandler.waitUntilDone();

        assertTrue(schedulingHandler.isDone());
        assertTrue(refreshHandler.isDone());
        
        issues = q.getIssues();
        assertEquals(1, issues.length);
    }

    public static final class TestLookup extends AbstractLookup {
        public TestLookup() {
            this(new InstanceContent());
        }
        private TestLookup(InstanceContent ic) {
            super(ic);
            ic.add(new DummyBugtrackingOwnerSupport());
            ic.add(new BugzillaIssueProvider());
        }
    }
    
    public static class DummyBugtrackingOwnerSupport extends BugtrackingOwnerSupport {
        @Override
        protected Repository getRepository(DataObject dataObj) {
            return null;
        }
        @Override
        public Repository getRepository(Project project, boolean askIfUnknown) {
            return null;
        }
        @Override
        public Repository getRepository(File file, String issueId, boolean askIfUnknown) {
            return null;
        }
        @Override
        protected Repository getRepositoryForContext(File context, String issueId, boolean askIfUnknown) {
            return null;
        }
        @Override
        public void setFirmAssociation(File file, Repository repository) {
            // do nothing
        }
        @Override
        public void setFirmAssociations(File[] files, Repository repository) {
            // do nothing
        }
        @Override
        public void setLooseAssociation(ContextType contextType, Repository repository) {
            // do nothing
        }
        @Override
        public void setLooseAssociation(File file, Repository repository) {
            // do nothing
        }        
    }

}

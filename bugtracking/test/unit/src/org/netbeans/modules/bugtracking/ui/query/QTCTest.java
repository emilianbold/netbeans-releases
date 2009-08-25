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

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Image;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.LogHandler;
import org.netbeans.modules.bugtracking.kenai.QueryAccessorImpl;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueCache;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.kenai.api.Kenai;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author tomas
 */
public class QTCTest extends NbTestCase {

    public QTCTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testOpenNewQuery() throws Throwable {
        MyRepository repo = MyConnector.repo;

        LogHandler openedHandler = new LogHandler("QueryAction.openQuery finnish", LogHandler.Compare.STARTS_WITH);
        LogHandler savedQueriesHandler = new LogHandler("saved queries.", LogHandler.Compare.ENDS_WITH);

        repo.queries.add(new MyQuery(repo));

        QueryAction.openQuery(null, repo, true);
        openedHandler.waitUntilDone();

        QueryTopComponent qtc1 = getQueryTC();
        JComboBox combo = (JComboBox) getField(qtc1, "repositoryComboBox");
        assertFalse(combo.isEnabled());
        assertTrue(combo.isVisible());
        JButton button = (JButton) getField(qtc1, "newButton");
        assertFalse(button.isEnabled());
        assertTrue(button.isVisible());

        openedHandler.reset();
        savedQueriesHandler.reset();
        QueryAction.openQuery(null, repo, false);
        openedHandler.waitUntilDone();
        savedQueriesHandler.waitUntilDone();

        QueryTopComponent qtc2 = getQueryTC(qtc1);
        combo = (JComboBox) getField(qtc2, "repositoryComboBox");
        assertTrue(combo.isEnabled());
        assertTrue(combo.isVisible());
        button = (JButton) getField(qtc2, "newButton");
        assertTrue(button.isEnabled());
        assertTrue(button.isVisible());

        JPanel queriesPanel = (JPanel) getField(qtc2, "queriesPanel");
        assertTrue(queriesPanel.isVisible());
        Query[] savedQueries = (Query[]) getField(qtc2, "savedQueries");
        assertEquals(1, savedQueries.length);
    }

    public void testSaveQuery() throws Throwable {
        MyRepository repo = MyConnector.repo;

        LogHandler openedHandler = new LogHandler("opened", LogHandler.Compare.ENDS_WITH);

        repo.queries.add(new MyQuery(repo));

        QueryAction.openQuery(null, repo, true);
        openedHandler.waitUntilDone();

        QueryTopComponent qtc1 = getQueryTC();
        JPanel repoPanel = (JPanel) getField(qtc1, "repoPanel");
        assertTrue(repoPanel.isVisible());

        repo.newquery.setSaved(true);
        repoPanel = (JPanel) getField(qtc1, "repoPanel");
        assertFalse(repoPanel.isVisible());
    }

    public void testOpenQuery() throws Throwable {
        MyRepository repo = MyConnector.repo;

        LogHandler openedHandler = new LogHandler("opened", LogHandler.Compare.ENDS_WITH);

        repo.queries.add(new MyQuery(repo));
        MyQuery query = new MyQuery(repo);
        QueryAction.openQuery(query);
        openedHandler.waitUntilDone();

        QueryTopComponent qtc1 = getQueryTC();
        JPanel repoPanel = (JPanel) getField(qtc1, "repoPanel");
        assertFalse(repoPanel.isVisible());

        openedHandler.reset();
        query = new MyQuery(repo); // new query
        QueryAction.openQuery(query);
        openedHandler.waitUntilDone();

        QueryTopComponent qtc2 = getQueryTC(qtc1);
        assertNotSame(qtc1, qtc2);

        repoPanel = (JPanel) getField(qtc2, "repoPanel");
        assertFalse(repoPanel.isVisible());
    }

    private QueryTopComponent getQueryTC(QueryTopComponent... ignore) {
        QueryTopComponent qtc = null;
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : tcs) {
            if (tc instanceof QueryTopComponent) {
                boolean found = false;
                for (TopComponent i : ignore) {
                    if(tc == i) {
                        found = true;
                        break;
                    }
                }
                if(found) continue;
                qtc = (QueryTopComponent) tc;
                break;
            }
        }
        assertNotNull(qtc);
        return qtc;
    }

    private Query[] getSavedQueries(QueryTopComponent tc) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return (Query[]) getField(tc, "savedQueries");
    }

    private Object getField(Object o, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static class MyRepository extends Repository {
        List<Query> queries = new ArrayList<Query>();
        MyQuery newquery;
        private static int c = 0;
        private final int i;
        public MyRepository() {
            this.newquery = new MyQuery(this);
            this.i = c++;
        }

        @Override
        public String getID() {
            return "repoid";
        }

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "repo"+i;
        }

        @Override
        public String getTooltip() {
            return "repo"+i;
        }

        @Override
        public String getUrl() {
            return "http://repo";
        }

        @Override
        public Issue getIssue(String id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BugtrackingController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Query createQuery() {
            return newquery;
        }

        @Override
        public Issue createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Query[] getQueries() {
            return queries.toArray(new Query[queries.size()]);
        }

        @Override
        public Issue[] simpleSearch(String criteria) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected IssueCache getIssueCache() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class MyQuery extends Query {
        private Repository repository;
        private static int c = 0;
        private final int i;

        private BugtrackingController controler = new BugtrackingController() {
            private JPanel panel = new JPanel();
            @Override
            public JComponent getComponent() {
                return panel;
            }
            @Override
            public HelpCtx getHelpCtx() {
                return null;
            }
            @Override
            public boolean isValid() {
                return true;
            }
            @Override
            public void applyChanges() throws IOException {}
        };

        public MyQuery(Repository repository) {
            this.repository = repository;
            i = c++;
        }

        @Override
        public String getDisplayName() {
            return "query"+i;
        }
        @Override
        public String getTooltip() {
            return "query"+i;
        }
        @Override
        public BugtrackingController getController() {
            return controler;
        }
        @Override
        public Repository getRepository() {
            return repository;
        }
        @Override
        public boolean refresh() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Issue[] getIssues(int includeStatus) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public boolean contains(Issue issue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getIssueStatus(Issue issue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSaved(boolean saved) {
            super.setSaved(saved);
        }

    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bugtracking.spi.BugtrackingConnector.class)
    public static class MyConnector extends BugtrackingConnector {
        static String ID = "QTCconector";
        private static MyRepository repo = new MyRepository();

        public MyConnector() {
        }

        @Override
        public String getDisplayName() {
            return ID;
        }

        @Override
        public String getTooltip() {
            return ID;
        }

        @Override
        public Repository createRepository() {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Repository[] getRepositories() {
            return new Repository[] {repo};
        }

        public Lookup getLookup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}

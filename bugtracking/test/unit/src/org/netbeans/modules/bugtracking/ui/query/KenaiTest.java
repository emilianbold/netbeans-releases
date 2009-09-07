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
import java.util.Collection;
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
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
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
public class KenaiTest extends NbTestCase {

    private String username;
    private String password;

    public KenaiTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
        username = br.readLine();
        password = br.readLine();
        br.close();
    }

    public void testQueryTopComponent() throws Throwable {
        KenaiRepository repo = KenaiConnector.repo;

        LogHandler openedHandler = new LogHandler("QueryAction.openQuery finnish", LogHandler.Compare.STARTS_WITH);

        QueryAction.openQuery(null, repo, true);
        openedHandler.waitUntilDone();

        QueryTopComponent qtc = getQueryTC();
        JComboBox combo = (JComboBox) getField(qtc, "repositoryComboBox");
        assertFalse(combo.isEnabled());
        JButton button = (JButton) getField(qtc, "newButton");
        assertFalse(button.isEnabled());

        Kenai.getDefault().login(username, password.toCharArray());
        Kenai.getDefault().logout();
        Kenai.getDefault().login(username, password.toCharArray());

        combo = (JComboBox) getField(qtc, "repositoryComboBox");
        assertFalse(combo.isEnabled());
        button = (JButton) getField(qtc, "newButton");
        assertFalse(button.isEnabled());
    }

    public void testRefreshQueriesInQueryTopComponent() throws Throwable {
        QueryAccessorImpl qa = new QueryAccessorImpl(); // need the instace to listen on kenai

        Kenai.getDefault().login(username, password.toCharArray());

        KenaiRepository repo = KenaiConnector.repo;
        repo.queries.add(new KenaiQuery(repo));

        LogHandler openedHandler = new LogHandler("QueryAction.openQuery finnish", LogHandler.Compare.STARTS_WITH);
        LogHandler savedHandler = new LogHandler("saved queries.", LogHandler.Compare.ENDS_WITH);

        QueryAction.openQuery(null, repo, true);
        openedHandler.waitUntilDone();

        QueryTopComponent qtc = getQueryTC();

        savedHandler.waitUntilDone();
        Query[] savedQueries = getSavedQueries(qtc);
        assertNotNull(savedQueries);

        assertEquals(repo.queries.size(), savedQueries.length);

        repo.queries.add(new KenaiQuery(repo));
        savedHandler.reset();
        Kenai.getDefault().logout();
        savedHandler.waitUntilDone();

        savedQueries = getSavedQueries(qtc);
        assertEquals(repo.queries.size(), savedQueries.length);

        repo.queries.clear();
        LogHandler noQueriesHandler = new LogHandler("No queries.", LogHandler.Compare.ENDS_WITH);
        Kenai.getDefault().login(username, password.toCharArray());
        noQueriesHandler.waitUntilDone();

        savedQueries = getSavedQueries(qtc);
        assertEquals(repo.queries.size(), savedQueries.length);
    }

    private QueryTopComponent getQueryTC() {
        QueryTopComponent qtc = null;
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : tcs) {
            if (tc instanceof QueryTopComponent) {
                qtc = (QueryTopComponent) tc;
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

    private static class KenaiRepository extends Repository {
        List<Query> queries = new ArrayList<Query>();
        Query newquery;

        public KenaiRepository() {
            this.newquery = new KenaiQuery(this);
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
            return "kenai repo";
        }

        @Override
        public String getTooltip() {
            return "kenai repo";
        }

        @Override
        public String getUrl() {
            return "http://kenai.repo";
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

        protected IssueCache getIssueCache() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Lookup getLookup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<RepositoryUser> getUsers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class KenaiQuery extends Query {
        private Repository repository;
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

        public KenaiQuery(Repository repository) {
            this.repository = repository;
        }

        @Override
        public String getDisplayName() {
            return "kenai query";
        }
        @Override
        public String getTooltip() {
            return "kenai query";
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
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bugtracking.spi.BugtrackingConnector.class)
    public static class KenaiConnector extends BugtrackingConnector {
        static String ID = "KenaiCconector";
        private static KenaiRepository repo = new KenaiRepository();

        public KenaiConnector() {
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

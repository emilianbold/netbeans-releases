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

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author tomas
 */
public class QTCTestHidden extends NbTestCase {

    public QTCTestHidden(String arg0) {
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

//    XXX failing on hudson SHOULD BE FIXED
//    public void testOpenNewQuery() throws Throwable {
//        MyRepository repo = MyConnector.repo;
//
//        LogHandler openedHandler = new LogHandler("QueryAction.openQuery finnish", LogHandler.Compare.STARTS_WITH);
//        LogHandler savedQueriesHandler = new LogHandler("saved queries.", LogHandler.Compare.ENDS_WITH);
//
//        repo.queries.add(new MyQuery(repo));
//
//        QueryAction.openQuery(null, repo, true);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc1 = getQueryTC();
//        JComboBox combo = (JComboBox) getField(qtc1, "repositoryComboBox");
//        assertFalse(combo.isEnabled());
//        assertTrue(combo.isVisible());
//        JButton button = (JButton) getField(qtc1, "newButton");
//        assertFalse(button.isEnabled());
//        assertTrue(button.isVisible());
//
//        openedHandler.reset();
//        savedQueriesHandler.reset();
//        QueryAction.openQuery(null, repo, false);
//        openedHandler.waitUntilDone();
//        savedQueriesHandler.waitUntilDone();
//
//        QueryTopComponent qtc2 = getQueryTC(qtc1);
//        combo = (JComboBox) getField(qtc2, "repositoryComboBox");
//        assertTrue(combo.isEnabled());
//        assertTrue(combo.isVisible());
//        button = (JButton) getField(qtc2, "newButton");
//        assertTrue(button.isEnabled());
//        assertTrue(button.isVisible());
//
//        JPanel queriesPanel = (JPanel) getField(qtc2, "queriesPanel");
//        assertTrue(queriesPanel.isVisible());
//        QueryProvider[] savedQueries = (QueryProvider[]) getField(qtc2, "savedQueries");
//        assertEquals(1, savedQueries.length);
//    }

//    XXX failing on hudson SHOULD BE FIXED
//    public void testSaveQuery() throws Throwable {
//        MyRepository repo = MyConnector.repo;
//
//        LogHandler openedHandler = new LogHandler("opened", LogHandler.Compare.ENDS_WITH);
//
//        repo.queries.add(new MyQuery(repo));
//
//        QueryAction.openQuery(null, repo, true);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc1 = getQueryTC();
//        JPanel repoPanel = (JPanel) getField(qtc1, "repoPanel");
//        assertTrue(repoPanel.isVisible());
//
//        repo.newquery.setSaved(true);
//        repoPanel = (JPanel) getField(qtc1, "repoPanel");
//        assertFalse(repoPanel.isVisible());
//    }

//    XXX failing on hudson SHOULD BE FIXED
//    public void testOpenQuery() throws Throwable {
//        MyRepository repo = MyConnector.repo;
//
//        LogHandler openedHandler = new LogHandler("opened", LogHandler.Compare.ENDS_WITH);
//
//        repo.queries.add(new MyQuery(repo));
//        MyQuery query = new MyQuery(repo);
//        QueryAction.openQuery(query);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc1 = getQueryTC();
//        JPanel repoPanel = (JPanel) getField(qtc1, "repoPanel");
//        assertFalse(repoPanel.isVisible());
//
//        openedHandler.reset();
//        query = new MyQuery(repo); // new query
//        QueryAction.openQuery(query);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc2 = getQueryTC(qtc1);
//        assertNotSame(qtc1, qtc2);
//
//        repoPanel = (JPanel) getField(qtc2, "repoPanel");
//        assertFalse(repoPanel.isVisible());
//    }

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

    private QueryProvider[] getSavedQueries(QueryTopComponent tc) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return (QueryProvider[]) getField(tc, "savedQueries");
    }

    private Object getField(Object o, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static class MyRepository extends RepositoryProvider {
        List<QueryProvider> queries = new ArrayList<QueryProvider>();
        MyQuery newquery;
        private static int c = 0;
        private final int i;
        private RepositoryInfo info;
        public MyRepository() {
            this.newquery = new MyQuery(this);
            this.i = c++;
            String name = "repoid" + i;
            info = new RepositoryInfo(name, name, "http://repo", name, name, null, null, null, null);
        }

        @Override
        public RepositoryInfo getInfo() {
            return info;
        }

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public IssueProvider getIssue(String id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RepositoryController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public QueryProvider createQuery() {
            return newquery;
        }

        @Override
        public IssueProvider createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public QueryProvider[] getQueries() {
            return queries.toArray(new QueryProvider[queries.size()]);
        }

        @Override
        public IssueProvider[] simpleSearch(String criteria) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected IssueCache getIssueCache() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Lookup getLookup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class MyQuery extends QueryProvider {
        private RepositoryProvider repository;
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
        private boolean saved;

        public MyQuery(RepositoryProvider repository) {
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
        public RepositoryProvider getRepository() {
            return repository;
        }        
        @Override
        public IssueProvider[] getIssues(int includeStatus) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public boolean contains(IssueProvider issue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getIssueStatus(IssueProvider issue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setSaved(boolean saved) {
            this.saved = saved;
        }

        @Override
        public boolean isSaved() {
            return saved;
        }
        
        @Override
        public void setContext(Node[] nodes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    
    @BugtrackingConnector.Registration (
        id=MyConnector.ID,
        displayName="Dummy bugtracking connector",
        tooltip="bugtracking connector created for testing purposes"
    )
    public static class MyConnector extends BugtrackingConnector {
        final static String ID = "QTCconector";
        private static MyRepository repo = new MyRepository();

        public MyConnector() {
        }

        @Override
        public RepositoryProvider createRepository() {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public RepositoryProvider createRepository(RepositoryInfo info) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}

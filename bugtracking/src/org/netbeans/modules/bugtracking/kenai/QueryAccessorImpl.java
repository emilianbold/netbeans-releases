/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.FakeJiraSupport.FakeJiraQueryHandle;
import org.netbeans.modules.bugtracking.kenai.FakeJiraSupport.FakeJiraQueryResultHandle;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.kenai.ui.spi.QueryAccessor.class)
public class QueryAccessorImpl extends QueryAccessor implements PropertyChangeListener {

    final private Map<String, ProjectHandleListener> projectListeners = new HashMap<String, ProjectHandleListener>();
    final private Map<String, KenaiRepositoryListener> kenaiRepoListeners = new HashMap<String, KenaiRepositoryListener>();

    final private Map<String, Map<String, QueryHandle>> queryHandles = new HashMap<String, Map<String, QueryHandle>>();

    public QueryAccessorImpl() {
        Kenai.getDefault().addPropertyChangeListener(this);
        Dashboard.getDefault().addPropertyChangeListener(this);
    }

    @Override
    public List<QueryHandle> getQueries(ProjectHandle project) {
        Repository repo = KenaiRepositories.getInstance().getRepository(project);
        if(repo == null) {
            FakeJiraSupport jira = FakeJiraSupport.get(project);
            if(jira != null) {
                return jira.getQueries();
            }
            // XXX log this inconvenience
            return Collections.emptyList();
        }
        KenaiRepositoryListener krl = null;
        synchronized(kenaiRepoListeners) {
            krl = kenaiRepoListeners.get(repo.getID());
            if(krl == null) {
                krl = new KenaiRepositoryListener(repo, project);
                repo.addPropertyChangeListener(krl);
                kenaiRepoListeners.put(repo.getID(), krl);
            } 
        }
        
        List<QueryHandle> queries = getQueryHandles(repo, project, true);

        ProjectHandleListener pl;
        synchronized(projectListeners) {
            pl = projectListeners.get(project.getId());
        }
        if(pl != null) {
            project.removePropertyChangeListener(pl);
        }
        pl = new ProjectHandleListener(project, queries);
        project.addPropertyChangeListener(pl);
        synchronized(projectListeners) {
            projectListeners.put(project.getId(), pl);
        }
        
        return Collections.unmodifiableList(queries);
    }

    private List<QueryHandle> getQueryHandles(Repository repo, ProjectHandle project, boolean newQueriesNeedRefresh) {
        Query[] queries = repo.getQueries();
        if(queries == null || queries.length == 0) {
            // XXX is this possible - at least preset queries
            return Collections.emptyList();
        }

        Map<String, QueryHandle> m;
        synchronized(queryHandles) {
            m = queryHandles.get(project.getId());
            if(m == null) {
                m = new HashMap<String, QueryHandle>();
                queryHandles.put(project.getId(), m);
            } else {
                // remove all which aren't in the returned query list
                List<String> l = new ArrayList<String>();
                for (Query q : queries) {
                    l.add(q.getDisplayName());
                }
                m.keySet().retainAll(l);
            }
        }

        List<QueryHandle> ret = new ArrayList<QueryHandle>();
        for (Query q : queries) {

            QueryHandle qh = m.get(q.getDisplayName());
            if(qh == null) {
                qh = new QueryHandleImpl(q, newQueriesNeedRefresh);
                m.put(q.getDisplayName(), qh);
            }
            ret.add(qh);
        }
        if(!KenaiUtil.isLoggedIn()) {
            QueryHandle myIssuesFake = new QueryHandle() {
                @Override
                public String getDisplayName() {
                    return NbBundle.getMessage(QueryAccessorImpl.class, "LBL_MyIssuesNotLoggedIn");
                }
                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {}
                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {}
            };
            ret.add(myIssuesFake);
        }
        return ret;
    }

    @Override
    public List<QueryResultHandle> getQueryResults(QueryHandle query) {
        if(query instanceof QueryHandleImpl) {
            QueryHandleImpl qh = (QueryHandleImpl) query;
            qh.refreshIfNeeded();
            return Collections.unmodifiableList(qh.getQueryResults());
        } else if(query instanceof FakeJiraQueryHandle) {
            FakeJiraQueryHandle jqh = (FakeJiraQueryHandle) query;
            return jqh.getQueryResults();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Action getFindIssueAction(ProjectHandle project) {
        final Repository repo = KenaiRepositories.getInstance().getRepository(project);
        if(repo == null) {
            // XXX dummy jira impl to open the jira page in a browser
            FakeJiraSupport jira = FakeJiraSupport.get(project);
            if(jira != null) {
                return new ActionWrapper(jira.getOpenProjectListener());
            }
            return null;
        }
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() { // XXX add post method to BM
                    public void run() {
                        QueryAction.openQuery(null, repo, true);
                    }
                });          
            }
        };
    }

    @Override
    public Action getCreateIssueAction(ProjectHandle project) {
        final Repository repo = KenaiRepositories.getInstance().getRepository(project);
        if(repo == null) {
            // XXX dummy jira impl to open the jira page in a browser
            FakeJiraSupport jira = FakeJiraSupport.get(project);
            if(jira != null) {
                return new ActionWrapper(jira.getCreateIssueListener());
            }
            return null;
        }
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() { // XXX add post method to BM
                    public void run() {
                        IssueAction.openIssue(repo);
                    }
                });
            }
        };
    }

    @Override
    public Action getOpenQueryResultAction(QueryResultHandle result) {
        if(result instanceof QueryResultHandleImpl ||
           result instanceof FakeJiraQueryResultHandle)
        {
            return new ActionWrapper((ActionListener) result);
        } else {
            return null;
        }
    }

    @Override
    public Action getDefaultAction(QueryHandle query) {
        if(query instanceof QueryHandleImpl ||
           query instanceof FakeJiraQueryHandle)
        {
            return new ActionWrapper((ActionListener) query);
        } else {
            return null;
        }
    }

    void fireQueriesChanged(ProjectHandle project, List<QueryHandle> newQueryList) {
        fireQueryListChanged(project, newQueryList);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Dashboard.PROP_REFRESH_REQUEST)) {
            synchronized(projectListeners) {
                projectListeners.clear();
            }
            synchronized(kenaiRepoListeners) {
                kenaiRepoListeners.clear();
            }
            synchronized(queryHandles) {
                queryHandles.clear();
            }
        } else if(evt.getPropertyName().equals(Kenai.PROP_LOGIN)) {
            if(evt.getNewValue() == null) {
                ProjectHandleListener[] pls;
                synchronized(projectListeners) {
                    pls = projectListeners.values().toArray(new ProjectHandleListener[projectListeners.values().size()]);
                }
                for (ProjectHandleListener pl : pls) {
                    pl.closeQueries();
                }
            }
            refreshKenaiQueries();
        }
    }

    private void refreshKenaiQueries() {
        Set<QueryTopComponent> tcs = QueryTopComponent.getOpenQueries(); // XXX updates also non kenai TC
        for (QueryTopComponent tc : tcs) {
            tc.updateSavedQueries();
        }
    }
    
    private class ProjectHandleListener implements PropertyChangeListener {
        private List<QueryHandle> queries;
        private ProjectHandle ph;
        public ProjectHandleListener(ProjectHandle ph, List<QueryHandle> queries) {
            this.queries = queries;
            this.ph = ph;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(ProjectHandle.PROP_CLOSE)) {
                closeQueries();
            }
        }
        public void closeQueries() {
            for (QueryHandle qh : queries) {
                if(qh instanceof QueryHandleImpl) {
                    QueryAction.closeQuery(((QueryHandleImpl) qh).getQuery());
                }
            }
            synchronized (projectListeners) {
                ph.removePropertyChangeListener(this);
                projectListeners.remove(ph.getId());
            }
        }
    }

    private class KenaiRepositoryListener implements PropertyChangeListener {
        private final ProjectHandle ph;
        private Repository repo;

        public KenaiRepositoryListener(Repository repo, ProjectHandle ph) {
            this.ph = ph;
            this.repo = repo;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(Repository.EVENT_QUERY_LIST_CHANGED)) {
                fireQueriesChanged(ph, getQueryHandles(repo, ph, false));
            }
        }
    }

    private static class ActionWrapper extends AbstractAction {
        private final ActionListener al;

        public ActionWrapper( ActionListener al ) {
            this.al = al;
        }
        public void actionPerformed(ActionEvent e) {
           al.actionPerformed(e);
        }
    }
}

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.jira.FakeJiraSupport;
import org.netbeans.modules.bugtracking.jira.FakeJiraSupport.FakeJiraQueryHandle;
import org.netbeans.modules.bugtracking.jira.FakeJiraSupport.FakeJiraQueryResultHandle;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Issue;
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
    public QueryHandle getAllIssuesQuery(ProjectHandle project) {
        Repository repo = KenaiRepositoryUtils.getInstance().getRepository(project);
        if(repo == null) {
            FakeJiraSupport jira = FakeJiraSupport.get(project);
            if (jira != null) {
                return jira.getAllIssuesQuery();
            }
            // XXX log this inconvenience
            return null;
        }

        KenaiSupport support = repo.getLookup().lookup(KenaiSupport.class);
        if(support == null) {
            return null;
        }

        registerRepository(repo, project);

        Query allIssuesQuery = support.getAllIssuesQuery(repo);
        if(allIssuesQuery == null) {
            return null;
        }
        List<QueryHandle> queries = getQueryHandles(project, allIssuesQuery);
        assert queries.size() == 1;

        registerProject(project, queries);

        return queries.get(0);
    }

    @Override
    public List<QueryHandle> getQueries(ProjectHandle project) {
        Repository repo = KenaiRepositoryUtils.getInstance().getRepository(project);
        if(repo == null) {
            return getQueriesForNoRepo(project);
        }

        registerRepository(repo, project);
        
        List<QueryHandle> queries = getQueryHandles(repo, project);

        registerProject(project, queries);

        return Collections.unmodifiableList(queries);
    }

    private List<QueryHandle> getQueriesForNoRepo(ProjectHandle project) {
        FakeJiraSupport jira = FakeJiraSupport.get(project);
        if (jira != null) {
            return jira.getQueries();
        }
        // XXX log this inconvenience
        return Collections.emptyList();
    }

    private List<QueryHandle> getQueryHandles(ProjectHandle project, Query... queries) {
        List<QueryHandle> ret = new ArrayList<QueryHandle>();
        synchronized (queryHandles) {
            Map<String, QueryHandle> m = queryHandles.get(project.getId());
            if (m == null) {
                m = new HashMap<String, QueryHandle>();
                queryHandles.put(project.getId(), m);
            } else {
                List<String> l = new ArrayList<String>();
                for (Query q : queries) {
                    if (q != null) {
                        String qName = q.getDisplayName();
                        l.add(qName);
                    }
                }
                m.keySet().retainAll(l);
            }
            for (Query q : queries) {
                String qName = q.getDisplayName();
                QueryHandle qh = m.get(qName);
                if (qh == null) {
                    Issue[] issues = q.getIssues();
                    // XXX HACK - totaly new queries should be refreshed.
                    //            unfortunatelly, an already refreshed query with
                    //            will be unnecessarilly refreshed one more time
                    //            as needed.
                    if(issues != null && issues.length > 0) {
                        qh = new QueryHandleImpl(q, false);
                    } else {
                        qh = new QueryHandleImpl(q, true);
                    }
                    m.put(qName, qh);
                }
                ret.add(qh);
            }
        }
        return ret;
    }

    private void registerProject(ProjectHandle project, List<QueryHandle> queries) {
        ProjectHandleListener pl;
        synchronized (projectListeners) {
            pl = projectListeners.get(project.getId());
        }
        if (pl != null) {
            project.removePropertyChangeListener(pl);
        }
        pl = new ProjectHandleListener(project, queries);
        project.addPropertyChangeListener(pl);
        synchronized (projectListeners) {
            projectListeners.put(project.getId(), pl);
        }
    }

    private void registerRepository(Repository repo, ProjectHandle project) {
        KenaiRepositoryListener krl = null;
        synchronized (kenaiRepoListeners) {
            krl = kenaiRepoListeners.get(repo.getID());
            if (krl == null) {
                krl = new KenaiRepositoryListener(repo, project);
                repo.addPropertyChangeListener(krl);
                kenaiRepoListeners.put(repo.getID(), krl);
            }
        }
    }

    private List<QueryHandle> getQueryHandles(Repository repo, ProjectHandle projectHandle) {
        Query[] queries = repo.getQueries();
        if(queries == null) {
            // XXX is this possible - at least preset queries
            return Collections.emptyList();
        }        
        List<QueryHandle> ret;
        if(KenaiUtil.isLoggedIn()) {
            ret = getQueryHandles(projectHandle, queries);
        } else {
            KenaiSupport.BugtrackingType type = getBugtrackingType(repo);
            assert type != null;
            
            if(KenaiSupport.BugtrackingType.JIRA == type) {
                // JIRA always returns zero issues if not logged in. Showing zero
                // in the resut would nbe missleading
                ret = new LinkedList<QueryHandle>();
                for (Query q : queries) {
                    ret.add(new NotLoggedInQueryHandle(q.getDisplayName()));
                }
            } else if(KenaiSupport.BugtrackingType.BUGZILLA == type) {
                // not logged in so my issues makes no sense
                // as we don't know who 'me' actually is. Other queries
                // contains resonable results so keep them
                ret = getQueryHandles(projectHandle, queries);
                
                Query myIssuesQuery = null;
                KenaiSupport support = repo.getLookup().lookup(KenaiSupport.class);
                if(support != null) {
                    myIssuesQuery = support.getMyIssuesQuery(repo);
                }
                for (QueryHandle qh : ret) {
                    if(qh instanceof QueryHandleImpl) {
                        if(((QueryHandleImpl)qh).getQuery() == myIssuesQuery) {
                            ret.remove(qh);
                            ret.add(new NotLoggedInQueryHandle(qh.getDisplayName()));
                            break;
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Unknown bugtracking type : " + getBugtrackingType(repo));
            }
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
        final Repository repo = KenaiRepositoryUtils.getInstance().getRepository(project);
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
                if(!KenaiUtil.isLoggedIn() &&
                    KenaiSupport.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !KenaiUtil.showLogin())
                {
                    return;
                }
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
        final Repository repo = KenaiRepositoryUtils.getInstance().getRepository(project);
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
                if(!KenaiUtil.isLoggedIn() &&
                    KenaiSupport.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !KenaiUtil.showLogin())
                {
                    return;
                }
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

    private KenaiSupport.BugtrackingType getBugtrackingType(Repository repo) {
        KenaiSupport support = repo.getLookup().lookup(KenaiSupport.class);
        if(support != null) {
            return support.getType();
        } else {
            assert false : "no KenaiSupport available for repository [" + repo.getDisplayName() + "]";  // NOI18N
        }
        return null;
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
                fireQueriesChanged(ph, getQueryHandles(repo, ph));
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

    private class NotLoggedInQueryHandle extends QueryHandle {
        private String notLoggedIn = NbBundle.getMessage(QueryAccessorImpl.class, "LBL_NotLoggedIn"); // NOI18N
        private String label;
        public NotLoggedInQueryHandle(String label) {
            this.label = label;
        }
        @Override
        public String getDisplayName() {
            return label + " " + notLoggedIn;                                   // NOI18N
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }

}

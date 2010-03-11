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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiNotification;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
class KenaiHandler {


    private final QueryAccessorImpl qaImpl;
    private final Kenai kenai;

    final private Map<String, ProjectListener> projectListeners = new HashMap<String, ProjectListener>();
    final private Map<String, KenaiRepositoryListener> kenaiRepoListeners = new HashMap<String, KenaiRepositoryListener>();
    final private Map<String, Map<String, QueryHandle>> queryHandles = new HashMap<String, Map<String, QueryHandle>>();

    private String lastLoggedUser = null;

    public KenaiHandler(QueryAccessorImpl qaImpl, Kenai kenai) {
        this.qaImpl = qaImpl;
        this.kenai = kenai;
        this.kenai.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Kenai.PROP_LOGIN)) {
                    if(evt.getNewValue() == null) { // means logged out
                        ProjectListener[] pls;
                        synchronized(projectListeners) {
                            pls = projectListeners.values().toArray(new ProjectListener[projectListeners.values().size()]);
                        }
                        for (ProjectListener pl : pls) {
                            pl.closeQueries();
                        }
                    } else {
                        // logged in
                        String user = getKenaiUser();
                        if(!user.equals(lastLoggedUser)) {
                            for(Map<String, QueryHandle> m : queryHandles.values()) {
                                for(QueryHandle qh : m.values()) {
                                    if(qh instanceof LoginAwareQueryHandle) {
                                        ((LoginAwareQueryHandle)qh).needsRefresh();
                                    }
                                }
                            }
                        }
                        user = lastLoggedUser;
                    }
                    refreshKenaiQueries();
                }
            }
        });
        Dashboard.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Dashboard.PROP_REFRESH_REQUEST)) {
                    if(KenaiHandler.this.kenai.equals(((Dashboard)evt.getSource())/* XXX .getKenai()*/)) {
                        clear();
                    }
                }
            }
        });
        lastLoggedUser = getKenaiUser();
    }

    List<QueryHandle> getQueryHandles(ProjectHandle project, Query... queries) {
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
                        qh = createQueryHandle(q, false);
                    } else {
                        qh = createQueryHandle(q, true); // true -> needs refresh
                    }
                    m.put(qName, qh);
                }
                ret.add(qh);
            }
        }
        sortQueries(ret);
        return ret;
    }

    private QueryHandleImpl createQueryHandle(Query q, boolean needsRefresh) {
        Repository repo = q.getRepository();
        KenaiSupport support = repo.getLookup().lookup(KenaiSupport.class);
        boolean predefined = false;
        if(support != null) {
            boolean needsLogin = support.needsLogin(q);
            predefined = support.getAllIssuesQuery(repo) == q || support.getMyIssuesQuery(repo) == q;
            if(needsLogin) {
                return new LoginAwareQueryHandle(q, needsRefresh, predefined);
            }
        }
        return new QueryHandleImpl(q, needsRefresh, predefined);
    }

    List<QueryHandle> getQueryHandles(Repository repo, ProjectHandle projectHandle) {
        Query[] queries = repo.getQueries();
        if(queries == null) {
            // XXX is this possible - at least preset queries
            return Collections.emptyList();
        }
        return getQueryHandles(projectHandle, queries);
    }

    private void sortQueries(List<QueryHandle> queryHandles) {
        Collections.sort(queryHandles, new Comparator<QueryHandle>() {
            @Override
            public int compare(QueryHandle qh1, QueryHandle qh2) {
                if(qh1 == null && qh1 == null) {
                    return 0;
                }
                if(qh2 == null) {
                    return 1;
                }
                if(qh1 == null) {
                    return -1;
                }
                boolean predefined1 = false;
                boolean predefined2 = false;
                if(qh1 instanceof QueryDescriptor && ((QueryDescriptor) qh1).isPredefined()) predefined1 = true;
                if(qh2 instanceof QueryDescriptor && ((QueryDescriptor) qh2).isPredefined()) predefined2 = true;
                if(predefined1 && !predefined2) {
                    return -1;
                } else if(predefined1 && !predefined2) {
                    return 1;
                }
                return qh1.getDisplayName().compareTo(qh2.getDisplayName());
            }
        });
    }

    void registerProject(ProjectHandle project, List<QueryHandle> queries) {
        ProjectListener pl;
        synchronized (projectListeners) {
            pl = projectListeners.get(project.getId());
        }
        if (pl != null) {
            project.removePropertyChangeListener(pl);
            project.getKenaiProject().removePropertyChangeListener(pl);
        }
        pl = new ProjectListener(project, queries);
        project.addPropertyChangeListener(pl);
        project.getKenaiProject().addPropertyChangeListener(pl);
        synchronized (projectListeners) {
            projectListeners.put(project.getId(), pl);
        }
    }

    void registerRepository(Repository repo, ProjectHandle project) {
        KenaiRepositoryListener krl = null;
        synchronized (kenaiRepoListeners) {
            String url = project.getKenaiProject().getKenai().getUrl().toString();
            krl = kenaiRepoListeners.get(repo.getID());
            if (krl == null) {
                krl = new KenaiRepositoryListener(repo, project);
                repo.addPropertyChangeListener(krl);
                kenaiRepoListeners.put(repo.getID(), krl);
            }
        }
    }

    private String getKenaiUser() {
        PasswordAuthentication pa = KenaiAccessorImpl.getPasswordAuthentication(kenai, false);
        return pa != null ? pa.getUserName() : null;
    }

    private void refreshKenaiQueries() {
        KenaiUtil.refreshOpenedQueries();
    }

    void clear() {
        synchronized(projectListeners) {
            projectListeners.clear();
        }
        synchronized(kenaiRepoListeners) {
            kenaiRepoListeners.clear();
        }
        synchronized(queryHandles) {
            queryHandles.clear();
        }
    }

    Action getFindIssuesAction(final Repository repo) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!KenaiAccessorImpl.isLoggedIn(kenai) &&
                    KenaiSupport.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !KenaiAccessorImpl.showLoginIntern())
                {
                    return;
                }
                Support.getInstance().post(new Runnable() { // XXX add post method to BM
                    @Override
                    public void run() {
                        BugtrackingUtil.openQuery(null, repo, true);
                    }
                });
            }
        };
    }

    Action getCreateIssueAction(final Repository repo) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!KenaiAccessorImpl.isLoggedIn(kenai) &&
                    KenaiSupport.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !KenaiAccessorImpl.showLoginIntern())
                {
                    return;
                }
                Support.getInstance().post(new Runnable() { // XXX add post method to BM
                    @Override
                    public void run() {
                        BugtrackingUtil.createIssue(repo);
                    }
                });
            }
        };
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

    private class ProjectListener implements PropertyChangeListener {
        private List<QueryHandle> queries;
        private ProjectHandle ph;
        public ProjectListener(ProjectHandle ph, List<QueryHandle> queries) {
            this.queries = queries;
            this.ph = ph;
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(ProjectHandle.PROP_CLOSE)) {
                closeQueries();
            } else if(evt.getPropertyName().equals(KenaiProject.PROP_PROJECT_NOTIFICATION)) {
                Object newValue = evt.getNewValue();
                if (newValue instanceof KenaiNotification) {
                    KenaiNotification kn = (KenaiNotification) newValue;
                    if (kn.getType() != KenaiService.Type.ISSUES) {
                        return;
                    }
                    for (QueryHandle qh : queries) {
                        if(qh instanceof QueryHandleImpl) {
                            Query query = ((QueryHandleImpl)qh).getQuery();
                            KenaiSupport ks = query.getRepository().getLookup().lookup(KenaiSupport.class);
                            assert ks != null;
                            if(ks != null) {
                                ks.refresh(query, false);
                            }              
                        }
                    }
                }

            }
        }
        public void closeQueries() {
            for (QueryHandle qh : queries) {
                if(qh instanceof QueryHandleImpl) {
                    BugtrackingUtil.closeQuery(((QueryHandleImpl) qh).getQuery());
                }
            }
            synchronized (projectListeners) {
                ph.removePropertyChangeListener(this);
                ph.getKenaiProject().removePropertyChangeListener(this);
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

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(Repository.EVENT_QUERY_LIST_CHANGED)) {
                List<QueryHandle> queryHandles = getQueryHandles(repo, ph);
                for (QueryHandle queryHandle : queryHandles) {
                    if(queryHandle instanceof QueryHandleImpl) {
                        QueryHandleImpl impl = (QueryHandleImpl) queryHandle;
                        if(impl.getQuery().isSaved()) {
                            // at this point every saved query should already be refreshed.
                            // it's just for the one which was eventually saved right now
                            // that it's needsRefresh flag haven't been set yet.
                            impl.needsRefresh = false;
                        }
                    }
                }
                qaImpl.fireQueriesChanged(ph, queryHandles);
            }
        }
    }

    private class LoginAwareQueryHandle extends QueryHandleImpl {
        private String notLoggedIn = NbBundle.getMessage(QueryAccessorImpl.class, "LBL_NotLoggedIn"); // NOI18N
        public LoginAwareQueryHandle(Query query, boolean needsRefresh, boolean predefined) {
            super(query, needsRefresh, predefined);
        }
        @Override
        public String getDisplayName() {
            return super.getDisplayName() + (KenaiAccessorImpl.isLoggedIn(kenai) ? "" : " " + notLoggedIn);        // NOI18N
        }
        @Override
        List<QueryResultHandle> getQueryResults() {
            return KenaiAccessorImpl.isLoggedIn(kenai) ? super.getQueryResults() : Collections.EMPTY_LIST;
        }
        @Override
        void refreshIfNeeded() {
            if(!KenaiAccessorImpl.isLoggedIn(kenai)) {
                return;
            }
            super.refreshIfNeeded();
        }
        void needsRefresh() {
            super.needsRefresh = true;
        }
    }
}

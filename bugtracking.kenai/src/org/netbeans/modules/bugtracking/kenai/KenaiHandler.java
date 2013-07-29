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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.team.spi.TeamBugtrackingConnector;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiNotification;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.api.KenaiUIUtils;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.QueryHandle;
import org.netbeans.modules.team.server.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

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
                if(evt.getPropertyName().equals(TeamServer.PROP_LOGIN)) {
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
                }
            }
        });
        KenaiUIUtils.addDashboardListener(kenai, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(DashboardSupport.PROP_REFRESH_REQUEST)) {
                    if(KenaiHandler.this.kenai.getUrl().equals(((DashboardSupport)evt.getSource()).getServer().getUrl())) {
                        clear();
                    }
                }
            }
        });
        lastLoggedUser = getKenaiUser();
    }

    List<QueryHandle> getQueryHandles(ProjectHandle<KenaiProject> project, Query... queries) {
        return getQueryHandles(project.getTeamProject().getName(), queries);
    }

    List<QueryHandle> getQueryHandles(String projectId, Query... queries) {
        List<QueryHandle> ret = new ArrayList<QueryHandle>();
        synchronized (queryHandles) {
            Map<String, QueryHandle> m = queryHandles.get(projectId);
            if (m == null) {
                m = new HashMap<String, QueryHandle>();
                queryHandles.put(projectId, m);
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
                    Collection<Issue> issues = q.getIssues();
                    // XXX HACK - totaly new queries should be refreshed.
                    //            unfortunatelly, an already refreshed query with
                    //            will be unnecessarilly refreshed one more time
                    //            as needed.
                    if(issues != null && !issues.isEmpty()) {
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
        boolean predefined = false;
        if(TeamUtil.isFromTeamServer(repo)) {
            boolean needsLogin = TeamUtil.needsLogin(q);
            predefined = TeamUtil.getAllIssuesQuery(repo) == q || TeamUtil.getMyIssuesQuery(repo) == q;
            if(needsLogin) {
                return new LoginAwareQueryHandle(q, needsRefresh, predefined);
            }
        }
        return new QueryHandleImpl(q, needsRefresh, predefined);
    }

    List<QueryHandle> getQueryHandles(Repository repo, ProjectHandle projectHandle) {
        Collection<Query> queries = repo.getQueries();
        if(queries == null) {
            // XXX is this possible - at least preset queries
            return Collections.emptyList();
        }
        return getQueryHandles(projectHandle, queries.toArray(new Query[queries.size()]));
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
                if(qh1 instanceof QueryDescriptor && ((QueryDescriptor) qh1).isPredefined()) {
                    predefined1 = true;
                }
                if(qh2 instanceof QueryDescriptor && ((QueryDescriptor) qh2).isPredefined()) {
                    predefined2 = true;
                }
                if(predefined1 && !predefined2) {
                    return -1;
                } else if(predefined1 && !predefined2) {
                    return 1;
                }
                return qh1.getDisplayName().compareTo(qh2.getDisplayName());
            }
        });
    }

    void registerProject(ProjectHandle<KenaiProject> project, List<QueryHandle> queries) {
        ProjectListener pl;
        synchronized (projectListeners) {
            pl = projectListeners.get(project.getId());
        }
        if (pl != null) {
            project.removePropertyChangeListener(pl);
            project.getTeamProject().removePropertyChangeListener(pl);
        }
        pl = new ProjectListener(project, queries);
        project.addPropertyChangeListener(pl);
        project.getTeamProject().addPropertyChangeListener(pl);
        synchronized (projectListeners) {
            projectListeners.put(project.getId(), pl);
        }
    }

    void registerRepository(Repository repo, ProjectHandle<KenaiProject> project) {
        synchronized (kenaiRepoListeners) {
            KenaiRepositoryListener krl;
            String url = project.getTeamProject().getKenai().getUrl().toString();
            krl = kenaiRepoListeners.get(repo.getId());
            if (krl == null) {
                krl = new KenaiRepositoryListener(repo, project);
                repo.addPropertyChangeListener(WeakListeners.propertyChange(krl, repo));
                kenaiRepoListeners.put(repo.getId(), krl);
            }
            krl.attachProject(project);
        }
    }

    private String getKenaiUser() {
        PasswordAuthentication pa = TeamAccessorImpl.getPasswordAuthentication(kenai, false);
        return pa != null ? pa.getUserName() : null;
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
                if(!TeamAccessorImpl.isLoggedIn(kenai) &&
                    TeamBugtrackingConnector.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !TeamAccessorImpl.showLoginIntern())
                {
                    return;
                }
                Support.getInstance().post(new Runnable() { // XXX add post method to BM
                    @Override
                    public void run() {
                        TeamUtil.openNewQuery(repo, true);
                    }
                });
            }
        };
    }

    Action getCreateIssueAction(final Repository repo) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!TeamAccessorImpl.isLoggedIn(kenai) &&
                    TeamBugtrackingConnector.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !TeamAccessorImpl.showLoginIntern())
                {
                    return;
                }
                Support.getInstance().post(new Runnable() { // XXX add post method to BM
                    @Override
                    public void run() {
                        TeamUtil.createIssue(repo);
                    }
                });
            }
        };
    }

    private TeamBugtrackingConnector.BugtrackingType getBugtrackingType(Repository repo) {
        return TeamUtil.getType(repo);
    }

    private class ProjectListener implements PropertyChangeListener {
        private List<QueryHandle> queries;
        private ProjectHandle<KenaiProject> ph;
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
                            final Query query = ((QueryHandleImpl)qh).getQuery();
                            Support.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    query.refresh();
                                }
                            });
                        }
                    }
                }
            }
        }
        public void closeQueries() {
            for (QueryHandle qh : queries) {
                if(qh instanceof QueryHandleImpl) {
                    TeamUtil.closeQuery(((QueryHandleImpl) qh).getQuery());
                }
            }
            synchronized (projectListeners) {
                ph.removePropertyChangeListener(this);
                ph.getTeamProject().removePropertyChangeListener(this);
                projectListeners.remove(ph.getId());
            }
        }
    }

    private class KenaiRepositoryListener implements PropertyChangeListener {
        private final ProjectHandle ph;
        private final Map<ProjectHandle, ProjectHandle> attachedProjects;
        private Repository repo;

        public KenaiRepositoryListener(Repository repo, ProjectHandle ph) {
            this.ph = ph;
            attachedProjects = new IdentityHashMap<ProjectHandle, ProjectHandle>(5);
            attachedProjects.put(ph, ph);
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
                ProjectHandle[] projectHandles;
                synchronized (attachedProjects) {
                    projectHandles = attachedProjects.keySet().toArray(new ProjectHandle[attachedProjects.size()]);
                }
                for (ProjectHandle projectHandle : projectHandles) {
                    qaImpl.fireQueriesChanged(projectHandle, queryHandles);
                }
            }
        }

        private void attachProject (ProjectHandle<KenaiProject> project) {
            synchronized (attachedProjects) {
                attachedProjects.put(project, project);
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
            return super.getDisplayName() + (TeamAccessorImpl.isLoggedIn(kenai) ? "" : " " + notLoggedIn);        // NOI18N
        }
        @Override
        List<QueryResultHandle> getQueryResults() {
            return TeamAccessorImpl.isLoggedIn(kenai) ? super.getQueryResults() : Collections.EMPTY_LIST;
        }
        @Override
        void refreshIfNeeded() {
            if(!TeamAccessorImpl.isLoggedIn(kenai)) {
                return;
            }
            super.refreshIfNeeded();
        }
        void needsRefresh() {
            super.needsRefresh = true;
        }
    }
}

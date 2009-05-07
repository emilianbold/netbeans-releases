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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;

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
    }

    @Override
    public List<QueryHandle> getQueries(ProjectHandle project) {
        Repository repo = KenaiRepositories.getInstance().getRepository(project);
        if(repo == null) {
            // XXX log this inconvenience
            return Collections.emptyList();
        }
        KenaiRepositoryListener krl = null;
        synchronized(kenaiRepoListeners) {
            krl = kenaiRepoListeners.get(repo.getDisplayName());
            if(krl == null) {
                krl = new KenaiRepositoryListener(repo, project);
                repo.addPropertyChangeListener(krl);
                kenaiRepoListeners.put(repo.getDisplayName(), krl);
            } 
        }
        
        List<QueryHandle> queries = getQueryHandles(repo);

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

    List<QueryHandle> getQueryHandles(Repository repo) {
        Query[] queries = repo.getQueries();
        if(queries == null || queries.length == 0) {
            // XXX is this possible - at least preset queries
            return Collections.emptyList();
        }
        
        Map<String, QueryHandle> m = queryHandles.get(repo.getUrl());
        if(m == null) {
            m = new HashMap<String, QueryHandle>();
            queryHandles.put(repo.getUrl(), m);
        } else {
            // remove all which aren't in the returned query list
            List<String> l = new ArrayList<String>();
            for (Query q : queries) {
                l.add(q.getDisplayName());
            }
            m.keySet().retainAll(l);
        }

        List<QueryHandle> ret = new ArrayList<QueryHandle>();
        for (Query q : queries) {

            QueryHandle qh = m.get(q.getDisplayName());
            if(qh == null) {
                qh = new QueryHandleImpl(q);
                m.put(q.getDisplayName(), qh);
            }
            ret.add(qh);
        }
        return ret;
    }

    @Override
    public List<QueryResultHandle> getQueryResults(QueryHandle query) {
        if(query instanceof QueryHandleImpl) {
            QueryHandleImpl qh = (QueryHandleImpl) query;
            qh.refreshIfFirstTime();
            return Collections.unmodifiableList(qh.getQueryResults());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public ActionListener getFindIssueAction(ProjectHandle project) {
        final Repository repo = KenaiRepositories.getInstance().getRepository(project);
        if(repo == null) {
            // XXX dummy jira impl to open the jira page in a browser
            FakeJiraSupport jira = FakeJiraSupport.create(project);
            if(jira != null) {
                return jira.getOpenProjectListener();
            }
            return null;
        }
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() { // XXX add post method to BM
                    public void run() {
                        QueryAction.openKenaiQuery(null, repo);
                    }
                });          
            }
        };
    }

    @Override
    public ActionListener getCreateIssueAction(ProjectHandle project) {
        final Repository repo = KenaiRepositories.getInstance().getRepository(project);
        if(repo == null) {
            // XXX dummy jira impl to open the jira page in a browser
            FakeJiraSupport jira = FakeJiraSupport.create(project);
            if(jira != null) {
                return jira.getCreateIssueListener();
            }
            return null;
        }
        return new ActionListener() {
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
    public ActionListener getOpenQueryResultAction(QueryResultHandle result) {
        if(result instanceof QueryResultHandle) {
            return (ActionListener) result;
        } else {
            return null;
        }
    }

    @Override
    public ActionListener getDefaultAction(QueryHandle query) {
        if(query instanceof QueryHandleImpl) {
            return (ActionListener) query;
        } else {
            return null;
        }
    }

    void fireQueriesChanged(ProjectHandle project, List<QueryHandle> newQueryList) {
        fireQueryListChanged(project, newQueryList);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Kenai.PROP_LOGIN) && evt.getNewValue() == null) {
            ProjectHandleListener[] pls;
            synchronized(projectListeners) {
                pls = projectListeners.values().toArray(new ProjectHandleListener[projectListeners.values().size()]);
            }
            for (ProjectHandleListener pl : pls) {
                pl.closeQueries();
            }
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
                QueryAction.closeQuery(((QueryHandleImpl) qh).getQuery());
            }
            synchronized (projectListeners) {
                ph.removePropertyChangeListener(this);
                projectListeners.remove(ph.getId());
            }
        }
    }

    private static class FakeJiraSupport {
        private static final String JIRA_SUBSTRING ="kenai.com/jira/"; // NOI18N
        private String projectUrl;
        private String createIssueUrl;

        private FakeJiraSupport(String projectUrl, String createIssueUrl) {
            this.projectUrl = projectUrl;
            this.createIssueUrl = createIssueUrl;
        }

        static FakeJiraSupport create(ProjectHandle handle) {
            KenaiProject project = KenaiRepositories.getKenaiProject(handle);
            if(project == null) {
                return null;
            }
            String url = null;
            String issueUrl = null;
            try {
                KenaiFeature[] features = project.getFeatures(KenaiService.Type.ISSUES);
                url = null;
                issueUrl = null;
                for (KenaiFeature f : features) {
                    if (!KenaiService.Names.JIRA.equals(f.getService())) { 
                        return null;
                    }
                    url = f.getLocation();
                    break;
                }
            } catch (KenaiException kenaiException) {
                Exceptions.printStackTrace(kenaiException);
            }
            if(url == null) {
                return null;
            }
            int idx = url.indexOf(JIRA_SUBSTRING);
            if(idx > -1) {
                issueUrl =
                        url.substring(0, idx + JIRA_SUBSTRING.length()) +
                        "secure/CreateIssue!default.jspa?pname=" + // NOI18N
                        project.getName();

            }
            return new FakeJiraSupport(url, issueUrl);
        }

        ActionListener getCreateIssueListener() {
            return getJiraListener(createIssueUrl);
        }

        ActionListener getOpenProjectListener() {
            return getJiraListener(projectUrl);
        }

        private ActionListener getJiraListener(String urlString) {
            final URL url;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
                return null;
            }
            return new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
                        public void run() {
                            HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                            if (displayer != null) {
                                displayer.showURL (url);
                            } else {
                                // XXX nice error message?
                                BugtrackingManager.LOG.warning("No URLDisplayer found.");             // NOI18N
                            }
                        }
                    });
                }
            };
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
                fireQueriesChanged(ph, getQueryHandles(repo));
            }
        }
    }

}

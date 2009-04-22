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

package org.netbeans.modules.jira.repository;

import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import java.awt.Image;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.util.IssueCache;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class JiraRepository extends Repository {

    private String name;
    private TaskRepository taskRepository;
    private RepositoryController controller;
    private Set<Query> queries = null;
    private IssueCache cache;

    private final Set<String> issuesToRefresh = new HashSet<String>(5);
    private final Set<JiraQuery> queriesToRefresh = new HashSet<JiraQuery>(3);
    private Task refreshIssuesTask;
    private Task refreshQueryTask;
    private RequestProcessor refreshProcessor;

    public JiraRepository() {
    }

    public JiraRepository(String repoName, String url, String user, String password, String httpUser, String httpPassword) {
        this();
        name = repoName;
        if(user == null) {
            user = "";                                                          // NOI18N
        }
        if(password == null) {
            password = "";                                                      // NOI18N
        }
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
    }


    JiraRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Query createQuery() {
        return new JiraQuery(this);
    }

    public Issue createIssue() {
        throw new UnsupportedOperationException();
    }

    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " : " + taskRepository.getCredentials(AuthenticationType.REPOSITORY).getUserName() + "@" + taskRepository.getUrl(); // NOI18N
    }
    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    @Override
    public Issue getIssue(String id) {
        JiraIssue issue = null;
        try {
            issue = Jira.getInstance().getClient(taskRepository).getIssueByKey(id, new NullProgressMonitor());
            if(issue != null) {
                return new NbJiraIssue(issue, this);
            }
        } catch (JiraException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public BugtrackingController getController() {
        if(controller == null) {
            controller = new RepositoryController(this);
        }
        return controller;
    }

    @Override
    public Image getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    @Override
    public void remove() {
        Query[] qs = getQueries();
        for (Query q : qs) {
            removeQuery((JiraQuery) q);
        }
        resetRepository();
        Jira.getInstance().removeRepository(this);
    }

    @Override
    public void fireQueryListChanged() {
        super.fireQueryListChanged();
    }

    public void removeQuery(JiraQuery query) {
        JiraConfig.getInstance().removeQuery(this, query);
        getIssueCache().removeQuery(name);
        getQueriesIntern().remove(query);
        stopRefreshing(query);
    }

    public void saveQuery(JiraQuery query) {
        assert name != null;
        JiraConfig.getInstance().putQuery(this, query); // XXX display name ????
        getQueriesIntern().add(query);
    }

    private Set<Query> getQueriesIntern() {
        if(queries == null) {
            queries = new HashSet<Query>(10);
            String[] qs = JiraConfig.getInstance().getQueries(name);
            for (String queryName : qs) {
                JiraQuery q = JiraConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Jira.LOG.warning("Couldn't find query with stored name " + queryName); // NOI18N
                }
            }
        }
        return queries;
    }

    @Override
    public Query[] getQueries() {
        Set<Query> l = getQueriesIntern();
        return l.toArray(new Query[l.size()]);
    }

    @Override
    public Issue[] simpleSearch(String criteria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IssueCache getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    void setName(String newName) {
        name = newName;
    }

    protected void setTaskRepository(String name, String url, String user, String password, String httpUser, String httpPassword) {
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
        Jira.getInstance().addRepository(this);
        resetRepository(); // only on url, user or passwd change        
    }

    static TaskRepository createTaskRepository(String name, String url, String user, String password, String httpUser, String httpPassword) {
        TaskRepository repository = new TaskRepository(name, url);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(user, password);
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);

        if(httpUser != null || httpPassword != null) {
            httpUser = httpUser != null ? httpUser : "";                        // NOI18N
            httpPassword = httpPassword != null ? httpPassword : "";            // NOI18N
            authenticationCredentials = new AuthenticationCredentials(httpUser, httpPassword);
            repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
        }

        // XXX need proxy settings from the IDE

        return repository;
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public String getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getPassword() : ""; // NOI18N
    }

    public String getHttpUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public String getHttpPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getPassword() : ""; // NOI18N
    }
    
    synchronized void resetRepository() {
        // XXX
//        bc = null;
//        if(getTaskRepository() != null) {
//            Jira.getInstance()
//                    .getRepositoryConnector()
//                    .getClientManager()
//                    .repositoryRemoved(getTaskRepository());
//        }
    }

    // XXX spi
    private void setupIssueRefreshTask() {
        if(refreshIssuesTask == null) {
            refreshIssuesTask = getRefreshProcessor().create(new Runnable() {
                public void run() {
                    Set<String> ids;
                    synchronized(issuesToRefresh) {
                        ids = new HashSet<String>(issuesToRefresh);
                    }
                    if(ids.size() == 0) {
                        Jira.LOG.log(Level.FINE, "no issues to refresh {0}", new Object[] {name}); // NOI18N
                        return;
                    }
                    Jira.LOG.log(Level.FINER, "preparing to refresh {0} - {1}", new Object[] {name, ids}); // NOI18N

                    // XXX
//                    GetMultiTaskDataCommand cmd = new GetMultiTaskDataCommand(JiraRepository.this, ids, new IssuesCollector());
//                    getExecutor().execute(cmd, false);
                    scheduleIssueRefresh();
                }
            });
            scheduleIssueRefresh();
        }
    }

    private void setupQueryRefreshTask() {
        if(refreshQueryTask == null) {
            refreshQueryTask = getRefreshProcessor().create(new Runnable() {
                public void run() {
                    Set<JiraQuery> queries;
                    synchronized(refreshQueryTask) {
                        queries = new HashSet<JiraQuery>(queriesToRefresh);
                    }
                    if(queries.size() == 0) {
                        Jira.LOG.log(Level.FINE, "no queries to refresh {0}", new Object[] {name}); // NOI18N
                        return;
                    }
                    for (JiraQuery q : queries) {
                        Jira.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), name}); // NOI18N
                        // XXX
//                        QueryController qc = q.getController();
//                        qc.autoRefresh();
                    }

                    scheduleQueryRefresh();
                }
            });
            scheduleQueryRefresh();
        }
    }

    private void scheduleIssueRefresh() {
        int delay = JiraConfig.getInstance().getIssueRefreshInterval();
        Jira.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)", new Object[] {name, delay}); // NOI18N
        refreshIssuesTask.schedule(delay * 60 * 1000); // given in minutes
    }

    private void scheduleQueryRefresh() {
        int delay = JiraConfig.getInstance().getQueryRefreshInterval();
        Jira.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)", new Object[] {name, delay}); // NOI18N
        refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
    }

    public void scheduleForRefresh(String id) {
        Jira.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}", new Object[] {id, name}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.add(id);
        }
        setupIssueRefreshTask();
    }

    public void stopRefreshing(String id) {
        Jira.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}", new Object[] {id, name}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.remove(id);
        }
    }

    public void scheduleForRefresh(JiraQuery query) {
        Jira.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}", new Object[] {query.getDisplayName(), name}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.add(query);
        }
        setupQueryRefreshTask();
    }

    public void stopRefreshing(JiraQuery query) {
        Jira.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}", new Object[] {query.getDisplayName(), name}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.remove(query);
        }
    }

    public void refreshAllQueries() {
        Query[] qs = getQueries();
        for (Query q : qs) {
            Jira.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), name}); // NOI18N
            // XXX
//            QueryController qc = ((JiraQuery) q).getController();
//            qc.onRefresh();
        }
    }

    private RequestProcessor getRefreshProcessor() {
        if(refreshProcessor == null) {
            refreshProcessor = new RequestProcessor("Bugzilla refresh - " + name); // NOI18N
        }
        return refreshProcessor;
    }

    private class Cache extends IssueCache {
        Cache() {
            super(JiraRepository.this.getUrl());
        }
        protected Issue createIssue(TaskData taskData) {
            // XXX
            return null; //new NbJiraIssue(taskData, JiraRepository.this);
        }
        protected void setTaskData(Issue issue, TaskData taskData) {
            // XXX
            //((NbJiraIssue)issue).setTaskData(taskData);
        }
    }
}

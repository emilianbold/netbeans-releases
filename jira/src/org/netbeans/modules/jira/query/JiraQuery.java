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

package org.netbeans.modules.jira.query;

import java.util.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider.Status;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraFilter;
import org.netbeans.modules.jira.client.spi.NamedFilter;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class JiraQuery {

    public static final String JIRA_ADHOC_QUERY_PREFIX = "jira ad-hoc query nr. ";
    
    private String name;
    private final JiraRepository repository;
    protected QueryController controller;
    private final Set<String> issues = new HashSet<>();

    protected JiraFilter jiraFilter;
    private boolean firstRun = true;
    private boolean saved;
    protected long lastRefresh;
    private SynchronizeQueryCommand queryCmd;
    private IRepositoryQuery iquery;
    private final Object ISSUES_LOCK = new Object();

    public JiraQuery(JiraRepository repository) {
        this(null, repository, null, false, true);
    }

    public JiraQuery(String name, JiraRepository repository, JiraFilter jiraFilter) {
        this(name, repository, jiraFilter, true, true);
    }

    public JiraQuery(String name, JiraRepository repository, JiraFilter jiraFilter, boolean saved, boolean initControler) {
        this.repository = repository;
        this.saved = saved;
        this.name = name;
        this.jiraFilter = jiraFilter;
        this.lastRefresh = JiraConfig.getInstance().getLastQueryRefresh(repository, getStoredQueryName());
        
        if(initControler) {
            // enforce controller creation
            getController();
        }
    }

    public String getDisplayName() {
        return name;
    }

    public boolean canRename() {
        return jiraFilter != null && !(isModifiable(jiraFilter));
    }
    
    public String getTooltip() {
        return name + " - " + repository.getDisplayName(); // NOI18N
    }

    public synchronized QueryController getController() {
        if (controller == null) {
            controller = createControler(repository, this, jiraFilter);
        }
        return controller;
    }

    public JiraRepository getRepository() {
        return repository;
    }

    protected QueryController createControler(JiraRepository r, JiraQuery q, JiraFilter jiraFilter) {
        if(jiraFilter == null || jiraFilter instanceof FilterDefinition) {
            return new QueryController(r, q, (FilterDefinition) jiraFilter);
        } else if(isModifiable(jiraFilter)) {
            return new QueryController(r, q, jiraFilter, false);
        }
        throw new IllegalStateException("wrong filter type : " + jiraFilter.getClass().getName());
    }

    private static boolean isModifiable(JiraFilter jiraFilter) {
        return jiraFilter instanceof NamedFilter;
    }

    public ColumnDescriptor[] getColumnDescriptors() {
        return NbJiraIssue.getColumnDescriptors(repository);
    }

    public void refresh() { // XXX what if already running! - cancel task
        refreshIntern();
    }

    boolean refreshIntern() { // XXX what if already running! - cancel task

        assert jiraFilter != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean ret[] = new boolean[1];
        executeQuery(new Runnable() {
            @Override
            public void run() {
                Jira.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new String[] {name /* XXX , filterDefinition*/ }); // NOI18N
                MylynSupport supp = MylynSupport.getInstance();
                IRepositoryQuery runningQuery = iquery;
                try {

                    // keeps all issues we will retrieve from the server
                    // - those matching the query criteria
                    // - and the archived
                    issues.clear();
                    if(isSaved()) {
                        if(!wasRun() && !issues.isEmpty()) {
                                Jira.LOG.log(Level.WARNING, "query {0} supposed to be run for the first time yet already contains issues.", getDisplayName()); // NOI18N
                                assert false;
                        }
                    }
                    firstRun = false;

                    try {
                        if (iquery == null) {
                            String qName = getStoredQueryName();
                            if (qName == null || name == null) {
                                qName = JIRA_ADHOC_QUERY_PREFIX + System.currentTimeMillis(); //NOI18N
                            }
                            iquery = MylynSupport.getInstance().getRepositoryQuery(repository.getTaskRepository(), qName);
                            if (iquery == null) {
                                iquery = MylynSupport.getInstance().createNewQuery(repository.getTaskRepository(), qName);
                                MylynSupport.getInstance().addQuery(repository.getTaskRepository(), iquery);
                            }
                        }
                        JiraConnectorSupport.getInstance().getConnector().setQuery(repository.getTaskRepository(), iquery, jiraFilter);
                        queryCmd = MylynSupport.getInstance().getCommandFactory()
                                .createSynchronizeQueriesCommand(repository.getTaskRepository(), iquery);
                        QueryProgressListener list = new QueryProgressListener();
                        queryCmd.addCommandProgressListener(list);
                        repository.getExecutor().execute(queryCmd);
                        ret[0] = queryCmd.hasFailed();
                        if (ret[0]) {
                            if (isSaved()) {
                                for (NbTask t : MylynSupport.getInstance().getTasks(iquery)) {
                                    // as a side effect creates a NbJiraIssue instance
                                    getRepository().getIssueForTask(t);
                                    issues.add(t.getTaskKey());
                                }
                            }
                            return;
                        }

                        list.notifyIssues(issues);

                        // but what about the archived issues?
                        // they should be refreshed as well, but do we really care about them ?
                    } catch (CoreException ex) {
                        Jira.LOG.log(Level.INFO, null, ex);
                        ret[0] = true;
                    }
                } finally {
                    queryCmd = null;
                    JiraConfig.getInstance().putLastQueryRefresh(repository, getStoredQueryName(), System.currentTimeMillis());
                    logQueryEvent(issues.size());
                    Jira.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new String[] {name /* XXX , filterDefinition*/}); // NOI18N
                }
            }
        });
        return ret[0];
    }

    public String getStoredQueryName() {
        return getDisplayName();
    }

    protected void logQueryEvent(int count) {
        LogUtils.logQueryEvent(
            JiraConnector.getConnectorName(),
            name,
            count,
            false,
            false);
    }

    void refresh(JiraFilter jiraFilter) {
        assert jiraFilter != null;
        this.jiraFilter = jiraFilter;
        refreshIntern();
    }

    public boolean canRemove() {
        return isModifiable(jiraFilter);
    }
    
    void delete() {
        if(iquery != null) {
            MylynSupport.getInstance().deleteQuery(iquery);
        }
    }
        
    public void remove() {
        if(QueryController.isNamedFilter(jiraFilter)) {
            // Serverside filter. Can't remove.
            JiraUtils.notifyErrorMessage(NbBundle.getMessage(JiraQuery.class, "MSG_CANNOT_REMOVE_SERVER_FILTER"));
            return;
        }
        repository.removeQuery(this);
    }

    public Status getIssueStatus(String key) {
        return repository.getIssue(key).getStatus();
    }

    int getSize() {
        return issues.size();
    }
    
    public void setName(String name) {
        this.name = name;
        if (iquery != null) {
            iquery.setSummary(name);
        }
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }
    
    public void setFilter(Filter filter) {
        getController().selectFilter(filter);
    }

    public Collection<NbJiraIssue> getIssues() {
        if (issues == null) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        synchronized (issues) {
            ids.addAll(issues);
        }
        
        List<NbJiraIssue> ret = new ArrayList<>();
        for (String id : ids) {
            ret.add(repository.getIssueCache().getIssue(id));
        }
        return ret;
    }

    /**
     * Returns the filter
     * @return an instance of FilterDefinition set in UI
     */
    public FilterDefinition getFilterDefinition () {
        return (FilterDefinition) getController().getJiraFilter();
    }
    
    boolean wasRun() {
        return !firstRun;
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    private class QueryProgressListener implements SynchronizeQueryCommand.CommandProgressListener {
        
        private final Set<String> addedIds = new HashSet<>();
        private final Set<String> ids = new HashSet<>();
        
        @Override
        public void queryRefreshStarted (Collection<NbTask> tasks) {
            for (NbTask task : tasks) {
                taskAdded(task);
            }
        }

        @Override
        public void tasksRefreshStarted (Collection<NbTask> tasks) {
            getController().switchToDeterminateProgress(tasks.size());
        }

        @Override
        public void taskAdded (NbTask task) {
            synchronized(ISSUES_LOCK) {
                // using taskId here, task key not yet available for fresh incoming tasks
                ids.add(task.getTaskId());
            }
            // when issue table or task dashboard is able to handle deltas
            // fire an event from here
        }

        @Override
        public void taskRemoved (NbTask task) {
            synchronized(ISSUES_LOCK) {
                // using taskId here, task key not yet available for fresh incoming tasks
                ids.remove(task.getTaskId());
            }
            NbJiraIssue issue = repository.getIssueForTask(task);
            if (issue != null) {
                fireNotifyDataRemoved(issue);
            }            
        }

        @Override
        public void taskSynchronized (NbTask task) {
            // using taskId here, task key not yet available for fresh incoming tasks
            if (ids.contains(task.getTaskId()) && addedIds.add(task.getTaskId())) {
                getController().progress(task.getSummary());
                NbJiraIssue issue = repository.getIssueForTask(task);
                if (issue != null) {
                    issues.add(task.getTaskKey());
                    fireNotifyDataAdded(issue); // XXX - !!! triggers getIssues()
                }
            }
        }
        
        private void notifyIssues (Set<String> issues) {
            // this is due to the archived issues
            MylynSupport supp = MylynSupport.getInstance();
            try {
                for (String taskId : issues) {
                    NbTask task = supp.getTask(repository.getUrl(), taskId);
                    if (task != null) {
                        NbJiraIssue issue = repository.getIssueForTask(task);
                        if (issue != null) {
                            // using taskId here, task key not yet available for fresh incoming tasks
                            if (addedIds.add(task.getTaskId())) {
                                fireNotifyDataAdded(issue); // XXX - !!! triggers getIssues()
                            }
                        }
                    }
                }
            } catch (CoreException ex) {
                Jira.LOG.log(Level.INFO, null, ex);
            }
        }

    };
    
    public void addNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized(list) {
            list.add(l);
        }
    }

    public void removeNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized(list) {
            list.remove(l);
        }
    }

    protected void fireNotifyDataAdded(NbJiraIssue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyDataAdded(issue);
        }
    }
    
    protected void fireNotifyDataRemoved(NbJiraIssue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyDataRemoved(issue);
        }
    }

    protected void fireStarted() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.started();
        }
    }

    protected void fireFinished() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.finished();
        }
    }

    // XXX move to API
    protected void executeQuery (Runnable r) {
        fireStarted();
        try {
            r.run();
        } finally {
            fireFinished();
            lastRefresh = System.currentTimeMillis();
        }
    }

    void cancel () {
        SynchronizeQueryCommand cmd = queryCmd;
        if (cmd != null) {
            cmd.cancel();
        }
    }
    
    private QueryNotifyListener[] getListeners() {
        List<QueryNotifyListener> list = getNotifyListeners();
        QueryNotifyListener[] listeners;
        synchronized (list) {
            listeners = list.toArray(new QueryNotifyListener[list.size()]);
        }
        return listeners;
    }

    private List<QueryNotifyListener> notifyListeners;
    private List<QueryNotifyListener> getNotifyListeners() {
        if(notifyListeners == null) {
            notifyListeners = new ArrayList<>();
        }
        return notifyListeners;
    }     
}

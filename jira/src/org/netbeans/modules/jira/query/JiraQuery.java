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

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.util.LogUtils;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.commands.PerformQueryCommand;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class JiraQuery {

    private String name;
    private final JiraRepository repository;
    protected QueryController controller;
    private final Set<String> issues = new HashSet<String>();
    private Set<String> archivedIssues = new HashSet<String>();

    protected JiraFilter jiraFilter;
    private boolean firstRun = true;
    private boolean saved;
    protected long lastRefresh;
    private final PropertyChangeSupport support;

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
        this.lastRefresh = repository.getIssueCache().getQueryTimestamp(getStoredQueryName());
        this.support = new PropertyChangeSupport(this);
        
        if(initControler) {
            // enforce controller creation
            getController();
        }
        if(repository instanceof KenaiRepository) {
            boolean autoRefresh = JiraConfig.getInstance().getQueryAutoRefresh(getDisplayName());
            if(autoRefresh) {
                getRepository().scheduleForRefresh(this);
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // XXX does this has to be protected
    public void fireQuerySaved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_SAVED, null, null);
    }

    protected void fireQueryRemoved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_REMOVED, null, null);
    }

    protected void fireQueryIssuesChanged() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_ISSUES_CHANGED, null, null);
    }  
    
    public String getDisplayName() {
        return name;
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
        } else if(jiraFilter instanceof NamedFilter) {
            return new QueryController(r, q, jiraFilter, false);
        }
        throw new IllegalStateException("wrong fileter type : " + jiraFilter.getClass().getName());
    }

    public ColumnDescriptor[] getColumnDescriptors() {
        return NbJiraIssue.getColumnDescriptors(repository);
    }

    public void refresh() { // XXX what if already running! - cancel task
        refreshIntern(false);
    }

    boolean refreshIntern(final boolean autoRefresh) { // XXX what if already running! - cancel task

        assert jiraFilter != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean ret[] = new boolean[1];
        executeQuery(new Runnable() {
            public void run() {
                Jira.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new String[] {name /* XXX , filterDefinition*/ }); // NOI18N
                try {

                    // keeps all issues we will retrieve from the server
                    // - those matching the query criteria
                    // - and the archived
                    issues.clear();
                    archivedIssues.clear();
                    if(isSaved()) {
                        if(!wasRun() && issues.size() != 0) {
                                Jira.LOG.warning("query " + getDisplayName() + " supposed to be run for the first time yet already contains issues."); // NOI18N
                                assert false;
                        }
                        // read the stored state ...
                        archivedIssues.addAll(repository.getIssueCache().readQueryIssues(getStoredQueryName()));
                        archivedIssues.addAll(repository.getIssueCache().readArchivedQueryIssues(getStoredQueryName()));
                    }
                    firstRun = false;

                    // run query to know what matches the criteria
                    // IssuesIdCollector will populate the issues set
                    PerformQueryCommand queryCmd = new PerformQueryCommand(repository, jiraFilter, new IssuesCollector());
                    repository.getExecutor().execute(queryCmd, !autoRefresh);
                    ret[0] = !queryCmd.hasFailed();
                    if(!ret[0]) {
                       return;
                    }

                    // only issues not returned by the query are archived
                    archivedIssues.removeAll(issues);
                    if(isSaved()) {
                        // ... and store the actuall state
                        repository.getIssueCache().storeQueryIssues(getStoredQueryName(), issues.toArray(new String[issues.size()]));
                        repository.getIssueCache().storeArchivedQueryIssues(getStoredQueryName(), archivedIssues.toArray(new String[archivedIssues.size()]));
                    }
                } finally {
                    logQueryEvent(issues.size(), autoRefresh);
                    Jira.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new String[] {name /* XXX , filterDefinition*/}); // NOI18N
                }
            }
        });
        return ret[0];
    }

    public String getStoredQueryName() {
        return getDisplayName();
    }

    protected void logQueryEvent(int count, boolean autoRefresh) {
        LogUtils.logQueryEvent(
            JiraConnector.getConnectorName(),
            name,
            count,
            false,
            autoRefresh);
    }

    void refresh(JiraFilter jiraFilter, boolean autoReresh) {
        assert jiraFilter != null;
        this.jiraFilter = jiraFilter;
        refreshIntern(autoReresh);
    }

    public void remove() {
        if(QueryController.isNamedFilter(jiraFilter)) {
            // Serverside filter. Can't remove.
            JiraUtils.notifyErrorMessage(NbBundle.getMessage(JiraQuery.class, "MSG_CANNOT_REMOVE_SERVER_FILTER"));
            return;
        }
        repository.removeQuery(this);
        fireQueryRemoved();
    }

    public boolean contains(String id) {
        return issues.contains(id);
    }

    public int getIssueStatus(String id) {
        return repository.getIssueCache().getStatus(id);
    }

    int getSize() {
        return issues.size();
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
        fireQuerySaved();
    }

    public boolean isSaved() {
        return saved;
    }
    
    public void setFilter(Filter filter) {
        getController().selectFilter(filter);
    }

    public Collection<NbJiraIssue> getIssues() {
        return getIssues(~0);
    }
    
    public Collection<NbJiraIssue> getIssues(int includeStatus) {
        if (issues == null) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<String>();
        synchronized (issues) {
            ids.addAll(issues);
        }
        
        IssueCache<NbJiraIssue, TaskData> cache = repository.getIssueCache();
        List<NbJiraIssue> ret = new ArrayList<NbJiraIssue>();
        for (String id : ids) {
            int status = getIssueStatus(id);
            if((status & includeStatus) != 0) {
                ret.add(cache.getIssue(id));
            }
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

    private class IssuesCollector extends TaskDataCollector {
        public IssuesCollector() {}
        public void accept(TaskData taskData) {
            String id = NbJiraIssue.getID(taskData);
            NbJiraIssue issue;
            try {
                getController().progress(NbJiraIssue.getDisplayName(taskData));
                IssueCache<NbJiraIssue, TaskData> cache = repository.getIssueCache();
                issue = (NbJiraIssue) cache.setIssueData(id, taskData);
                issues.add(issue.getID());
            } catch (IOException ex) {
                Jira.LOG.log(Level.SEVERE, null, ex);
                return;
            }
            fireNotifyData(issue); // XXX - !!! triggers getIssues()
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

    protected void fireNotifyData(NbJiraIssue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyData(issue);
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
            fireQueryIssuesChanged();
            lastRefresh = System.currentTimeMillis();
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
            notifyListeners = new ArrayList<QueryNotifyListener>();
        }
        return notifyListeners;
    }     
}

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

package org.netbeans.modules.bugzilla.query;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.util.*;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.cache.IssueCache;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.util.LogUtils;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaQuery {

    private String name;
    private final BugzillaRepository repository;
    protected QueryController controller;
    private final Set<String> issues = new HashSet<String>();
    private Set<String> archivedIssues = new HashSet<String>();

    // XXX its not clear how the urlParam is used between query and controller
    protected String urlParameters;
    private boolean initialUrlDef;

    private boolean firstRun = true;
    private ColumnDescriptor[] columnDescriptors;
    private OwnerInfo info;
    private boolean saved;
    protected long lastRefresh;
    private final PropertyChangeSupport support;
    private IRepositoryQuery iquery;
        
    public BugzillaQuery(BugzillaRepository repository) {
        this(null, null, repository, null, false, false, true);
    }

    public BugzillaQuery (String name, BugzillaRepository repository, String urlParameters, boolean saved, boolean urlDef, boolean initControler) {
        this(name, null, repository, urlParameters, saved, urlDef, initControler);
    }
    
    public BugzillaQuery (String name, IRepositoryQuery query, BugzillaRepository repository, String urlParameters, boolean saved, boolean urlDef, boolean initControler) {
        this.repository = repository;
        this.saved = saved;
        this.name = name;
        this.iquery = query;
        this.urlParameters = urlParameters;
        this.initialUrlDef = urlDef;
        this.lastRefresh = repository.getIssueCache().getQueryTimestamp(getStoredQueryName());
        this.support = new PropertyChangeSupport(this);
        
        if(initControler) {
            controller = createControler(repository, this, urlParameters);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireQuerySaved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_SAVED, null, null);
    }

    private void fireQueryRemoved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_REMOVED, null, null);
    }

    private void fireQueryIssuesChanged() {
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
            controller = createControler(repository, this, urlParameters);
        }
        return controller;
    }

    public BugzillaRepository getRepository() {
        return repository;
    }

    protected QueryController createControler(BugzillaRepository r, BugzillaQuery q, String parameters) {
        return new QueryController(r, q, parameters, initialUrlDef);
    }

    public ColumnDescriptor[] getColumnDescriptors() {
        if(columnDescriptors == null) {
            columnDescriptors = BugzillaIssue.getColumnDescriptors(repository);
        }
        return columnDescriptors;
    }

    boolean refreshIntern(final boolean autoRefresh) { // XXX what if already running! - cancel task

        assert urlParameters != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean ret[] = new boolean[1];
        executeQuery(new Runnable() {
            @Override
            public void run() {
                Bugzilla.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new String[] {name, urlParameters}); // NOI18N
                IRepositoryQuery runningQuery = iquery;
                try {

                    // keeps all issues we will retrieve from the server
                    // - those matching the query criteria
                    // - and the obsolete ones
                    Set<String> queryIssues = new HashSet<String>();

                    issues.clear();
                    archivedIssues.clear();
                    if(isSaved()) {
                        if(!wasRun() && !issues.isEmpty()) {
                            Bugzilla.LOG.log(Level.WARNING, "query {0} supposed to be run for the first time yet already contains issues.", getDisplayName()); // NOI18N
                            assert false;
                        }
                        // read the stored state ...
                        queryIssues.addAll(repository.getIssueCache().readQueryIssues(getStoredQueryName()));
                        queryIssues.addAll(repository.getIssueCache().readArchivedQueryIssues(getStoredQueryName()));
                        // ... and they might be rendered obsolete if not returned by the query
                        archivedIssues.addAll(queryIssues);
                    }
                    firstRun = false;

                    // run query to know what matches the criteria
                    StringBuilder url = new StringBuilder();
                    url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST);
                    url.append(urlParameters); // XXX encode url?
                    // IssuesIdCollector will populate the issues set
                    try {
                        if (runningQuery == null) {
                            String qName = getStoredQueryName();
                            if (qName == null || name == null) {
                                qName = "bugzilla ad-hoc query nr. " + System.currentTimeMillis(); //NOI18N
                            }
                            runningQuery = MylynSupport.getInstance().getRepositoryQuery(repository.getTaskRepository(), qName);
                            if (runningQuery == null) {
                                runningQuery = MylynSupport.getInstance().createNewQuery(repository.getTaskRepository(), qName);
                                MylynSupport.getInstance().addQuery(repository.getTaskRepository(), runningQuery);
                            }
                            if (isSaved()) {
                                iquery = runningQuery;
                            }
                        }
                        String queryUrl = url.toString();
                        runningQuery.setUrl(queryUrl);
                        SynchronizeQueryCommand queryCmd = MylynSupport.getInstance().getCommandFactory()
                                .createSynchronizeQueriesCommand(repository.getTaskRepository(), runningQuery);
                        QueryProgressListener list = new QueryProgressListener();
                        queryCmd.addCommandProgressListener(list);
                        repository.getExecutor().execute(queryCmd, !autoRefresh);
                        ret[0] = queryCmd.hasFailed();
                        if (ret[0]) {
                            if (isSaved()) {
                                for (NbTask t : MylynSupport.getInstance().getTasks(runningQuery)) {
                                    issues.add(t.getTaskId());
                                }
                            }
                            return;
                        }

                        // only issues not returned by the query are obsolete
                        archivedIssues.removeAll(issues);
                        if(isSaved()) {
                            // ... and store all issues you got
                            repository.getIssueCache().storeQueryIssues(getStoredQueryName(), issues.toArray(new String[issues.size()]));
                            repository.getIssueCache().storeArchivedQueryIssues(getStoredQueryName(), archivedIssues.toArray(new String[archivedIssues.size()]));
                        }
                        list.notifyIssues(issues);
                        list.notifyIssues(archivedIssues);

                        // but what about the archived issues?
                        // they should be refreshed as well, but do we really care about them ?
                    } catch (CoreException ex) {
                        Bugzilla.LOG.log(Level.INFO, null, ex);
                        ret[0] = true;
                    }
                } finally {
                    if (iquery == null && runningQuery != null) {
                        // ad-hoc queries cannot be saved in tasklist
                        MylynSupport.getInstance().deleteQuery(runningQuery);
                    }
                    logQueryEvent(issues.size(), autoRefresh);
                    Bugzilla.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new String[] {name, urlParameters}); // NOI18N
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
            BugzillaConnector.getConnectorName(),
            name,
            count,
            false,
            autoRefresh);
    }

    void refresh(String urlParameters, boolean autoReresh) {
        assert urlParameters != null;
        this.urlParameters = urlParameters;
        refreshIntern(autoReresh);
    }

    public void remove() {
        repository.removeQuery(this);
        fireQueryRemoved();
    }

    public boolean contains(String id) {
        return issues.contains(id);
    }

    public void setOwnerInfo(OwnerInfo info) {
        this.info = info;
    }

    public OwnerInfo getOwnerInfo() {
        return info;
    }

    public IssueCache.Status getIssueStatus(String id) {
        return repository.getIssueCache().getStatus(id);
    }

    int getSize() {
        return issues.size();
    }

    public String getUrlParameters() {
        return getController().getUrlParameters(false);
    }

    public boolean isUrlDefined() {
        return getController().isUrlDefined();
    }

    public void setName(String name) {
        this.name = name;
        if (iquery != null) {
            iquery.setSummary(name);
        }
    }

    public void setSaved(boolean saved) {
        if(saved) {
            info = null;
        }
        this.saved = saved;
        fireQuerySaved();
    }

    public boolean isSaved() {
        return saved;
    }
    
    public Collection<BugzillaIssue> getIssues() {
        if (issues == null) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<String>();
        synchronized (issues) {
            ids.addAll(issues);
        }

        IssueCache<BugzillaIssue> cache = repository.getIssueCache();
        List<BugzillaIssue> ret = new ArrayList<BugzillaIssue>();
        for (String id : ids) {
            BugzillaIssue issue = cache.getIssue(id);
            if (issue != null) {
                ret.add(issue);
            }
        }
        return ret;
    }

    boolean wasRun() {
        return !firstRun;
    }

    long getLastRefresh() {
        return lastRefresh;
    }

    private class QueryProgressListener implements SynchronizeQueryCommand.CommandProgressListener {
        
        private final Set<String> addedIds = new HashSet<String>();
        
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
            issues.add(task.getTaskId());
            // when issue table or task dashboard is able to handle deltas
            // fire an event from here
        }

        @Override
        public void taskRemoved (NbTask task) {
            issues.remove(task.getTaskId());
            // when issue table or task dashboard is able to handle removals
            // fire an event from here
        }

        @Override
        public void taskSynchronized (NbTask task) {
            getController().addProgressUnit(BugzillaIssue.getDisplayName(task));
        }

        private void notifyIssues (Set<String> issues) {
            // this is due to the archived issues
            MylynSupport supp = MylynSupport.getInstance();
            try {
                for (String taskId : issues) {
                    NbTask task = supp.getTask(repository.getUrl(), taskId);
                    if (task != null) {
                        BugzillaIssue issue = repository.getIssueForTask(task);
                        if (issue != null) {
                            if (addedIds.add(task.getTaskId())) {
                                fireNotifyDataAdded(issue); // XXX - !!! triggers getIssues()
                            }
                        }
                    }
                }
            } catch (CoreException ex) {
                Bugzilla.LOG.log(Level.INFO, null, ex);
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

    protected void fireNotifyDataAdded (BugzillaIssue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyDataAdded(issue);
        }
    }

    protected void fireNotifyDataRemoved (BugzillaIssue issue) {
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.tasks.query;

import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.commons.LogUtils;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.client.api.ODCSFactory;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.ODCSConnector;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Stupka
 */
public abstract class ODCSQuery {

    private final ODCSRepository repository;
    private ODCSQueryController controller;

    private final List<QueryNotifyListener> notifyListeners = new ArrayList<QueryNotifyListener>();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);;
    private String name;
    private long lastRefresh;
    
    private boolean saved;

    private boolean firstRun = true;
    private ColumnDescriptor[] columnDescriptors;
    
    private final Object ISSUES_LOCK = new Object();
    private final Set<String> issues = new HashSet<String>();
    private SynchronizeQueryCommand queryCmd;
    
    public static ODCSQuery createNew(ODCSRepository repository) {
        return new CustomQuery(repository);
    }
    
    public static ODCSQuery createNew(ODCSRepository repository, Criteria criteria) {
        return new CustomQuery(repository, criteria);
    }
    
    public static ODCSQuery createSaved(ODCSRepository repository, SavedTaskQuery stq) {
        return new CustomQuery(repository, stq);
    }
    
    public static ODCSQuery createPredefined(ODCSRepository repository, String name, IRepositoryQuery predefinedQuery) {
        return new PredefinedQuery(repository, name, predefinedQuery);
    }
        
    protected abstract void refresh(boolean autoRefresh);
    protected abstract Criteria getCriteria();
    protected abstract IRepositoryQuery getRepositoryQuery();
    protected abstract boolean isModifiable();
    protected abstract boolean save(String name);
    public abstract void remove();
    
    protected ODCSQuery(ODCSRepository repository, String name) {
        this.name = name;
        this.repository = repository;
        this.saved = name != null;
    }
    
    public ODCSRepository getRepository() {
        return repository;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    int getSize() {
        synchronized(ISSUES_LOCK) {
            return issues.size();
        }
    }

    boolean wasRun() {
        return !firstRun;
    }

    public String getDisplayName() {
        return name;
    }

    protected void setSaved(String name) {
        this.name = name;
        this.saved = true;
    }

    long getLastRefresh() {
        return lastRefresh;
    }

    public Collection<ODCSIssue> getIssues() {
        List<String> ids;
        synchronized(ISSUES_LOCK) {
            if (issues == null) {
                return Collections.emptyList();
            }
            ids = new ArrayList<String>(issues.size());
            ids.addAll(issues);
        }
        
        ODCSRepository.Cache cache = repository.getIssueCache();
        List<ODCSIssue> ret = new ArrayList<ODCSIssue>();
        for (String id : ids) {
            ret.add(cache.getIssue(id));
        }
        return ret;
    }

    public ColumnDescriptor[] getColumnDescriptors() {
        if(columnDescriptors == null) {
            columnDescriptors = ODCSIssue.getColumnDescriptors(repository);
        }
        return columnDescriptors;
    }
    
    public void addNotifyListener(QueryNotifyListener l) {
        synchronized(notifyListeners) {
            notifyListeners.add(l);
        }
    }

    public void removeNotifyListener(QueryNotifyListener l) {
        synchronized(notifyListeners) {
            notifyListeners.remove(l);
        }
    }

    protected void fireNotifyData(ODCSIssue issue) {
        QueryNotifyListener[] list;
        synchronized(notifyListeners) {
            list = notifyListeners.toArray(new QueryNotifyListener[notifyListeners.size()]);
        }
        for (QueryNotifyListener l : list) {
            l.notifyData(issue);
        }
    }

    protected void fireStarted() {
        QueryNotifyListener[] list;
        synchronized(notifyListeners) {
            list = notifyListeners.toArray(new QueryNotifyListener[notifyListeners.size()]);
        }        
        for (QueryNotifyListener l : list) {
            l.started();
        }
    }

    protected void fireFinished() {
        QueryNotifyListener[] list;
        synchronized(notifyListeners) {
            list = notifyListeners.toArray(new QueryNotifyListener[notifyListeners.size()]);
        }        
        for (QueryNotifyListener l : list) {
            l.finished();
        }
    }

    public String getTooltip() {
        return name + " - " + repository.getDisplayName(); // NOI18N
    }

    public final synchronized ODCSQueryController getController () {
        if(controller == null) {
            controller = new ODCSQueryController(repository, this, getCriteria(), isModifiable());
        }
        return controller;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireQueryIssuesChanged() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_REFRESHED, null, null);
    }  

    public void refresh() {
        refreshIntern(false);
    }
    
    public void refreshIntern(final boolean autoRefresh) {
        
//        assert if query was provided with parameters from controller
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
          
        executeQuery(new Runnable() {
            @Override
            public void run() {
                ODCS.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new Object[] {name, getRepositoryQuery().getAttribute(CloudDevConstants.QUERY_CRITERIA)}); // NOI18N
                MylynSupport supp = MylynSupport.getInstance();
                IRepositoryQuery runningQuery = null;
                try {
                    
                    synchronized(ISSUES_LOCK) {
                        issues.clear();
                    }
                    
                    firstRun = false;

                    runningQuery = getRepositoryQuery();
                    if (supp.getRepositoryQuery(getRepository().getTaskRepository(),
                            runningQuery.getSummary()) == null) {
                        supp.addQuery(getRepository().getTaskRepository(), runningQuery);
                    }
                    queryCmd = MylynSupport.getInstance().getCommandFactory()
                                .createSynchronizeQueriesCommand(repository.getTaskRepository(), runningQuery);
                    QueryProgressListener list = new QueryProgressListener();
                    queryCmd.addCommandProgressListener(list);
                    repository.getExecutor().execute(queryCmd, true, !autoRefresh);
                    
                    if(queryCmd.hasFailed()) {
                        return;
                    }

                } catch (CoreException ex) {
                    ODCS.LOG.log(Level.INFO, null, ex);
                } finally {
                    queryCmd = null;
                    if (!isSaved() && runningQuery != null) {
                        // ad-hoc queries cannot be saved in tasklist
                        MylynSupport.getInstance().deleteQuery(runningQuery);
                    }
                    synchronized(ISSUES_LOCK) {
                        logQueryEvent(issues.size(), autoRefresh);
                    }
                    if(ODCS.LOG.isLoggable(Level.FINE)) {
                        ODCS.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new Object[] {name, getRepositoryQuery().getAttribute(CloudDevConstants.QUERY_CRITERIA)}); // NOI18N
                    }
                }
            }
        });
    }

    protected void logQueryEvent(int count, boolean autoRefresh) {
        LogUtils.logQueryEvent(
            ODCSConnector.ID,
            name,
            count,
            false,
            autoRefresh);
    }
    
    private void executeQuery (Runnable r) {
        fireStarted();
        try {
            r.run();
        } finally {
            fireFinished();
            fireQueryIssuesChanged();
            lastRefresh = System.currentTimeMillis();
        }
    }

    void cancel () {
        SynchronizeQueryCommand cmd = queryCmd;
        if (cmd != null) {
            cmd.cancel();
        }
    }

    public boolean canRemove() {
        return isModifiable();
    }

    private class QueryProgressListener implements SynchronizeQueryCommand.CommandProgressListener {
        
        private final Set<String> addedIds = new HashSet<String>();
        private final Set<String> ids = new HashSet<String>();
        
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
                ids.add(task.getTaskId());
            }
            // when issue table or task dashboard is able to handle deltas
            // fire an event from here
        }

        @Override
        public void taskRemoved (NbTask task) {
            synchronized(ISSUES_LOCK) {
                ids.remove(task.getTaskId());
            }
            // when issue table or task dashboard is able to handle removals
            // fire an event from here
        }

        @Override
        public void taskSynchronized (NbTask task) {
            if (ids.contains(task.getTaskId()) && addedIds.add(task.getTaskId())) {
                getController().addProgressUnit(task.getSummary());
                ODCSIssue issue = repository.getIssueForTask(task);
                if (issue != null) {
                    issues.add(task.getTaskId());
                    fireNotifyData(issue); // XXX - !!! triggers getIssues()
                }
            }
        }

    };
    
    private static class CustomQuery extends ODCSQuery {

        private IRepositoryQuery repositoryQuery;
        private SavedTaskQuery savedQuery;
        private Criteria criteria;
                
        public CustomQuery(ODCSRepository repository) {
            this(repository, (SavedTaskQuery) null);
        }
        
        public CustomQuery(ODCSRepository repository, SavedTaskQuery savedQuery) {
            super(repository, savedQuery != null ? savedQuery.getName() : null);
            this.savedQuery = savedQuery;
            this.criteria = null;
        }
        
        public CustomQuery(ODCSRepository repository, Criteria criteria) {
            super(repository, null);
            this.savedQuery = null;
            this.criteria = criteria;
        }

        @Override
        protected IRepositoryQuery getRepositoryQuery() {
            // XXX synchronize
            if(repositoryQuery == null) {
                MylynSupport supp = MylynSupport.getInstance();
                String name = getDisplayName();
                if (name == null) {
                    name = "ODCS ad hoc query - " + System.currentTimeMillis();
                }
                try {
                    repositoryQuery = supp.getRepositoryQuery(getRepository().getTaskRepository(), name);
                    if (repositoryQuery == null) {
                        repositoryQuery = supp.createNewQuery(getRepository().getTaskRepository(), name);
                        repositoryQuery.setUrl(CloudDevConstants.CRITERIA_QUERY);                
                        repositoryQuery.setAttribute(CloudDevConstants.QUERY_NAME, name);
                    }
                } catch (CoreException ex) {
                    ODCS.LOG.log(Level.INFO, null, ex);
                }
            }
            return repositoryQuery;
        }

        @Override
        protected Criteria getCriteria() { 
            return savedQuery != null ? savedQuery.getQueryCriteria() : criteria;
        }

        @Override
        protected void refresh(boolean autoRefresh) {
            String queryString = getController().getQueryString();
            if(queryString == null) {
                queryString = ""; // NOI18N
            }
            if(savedQuery != null) {
                savedQuery.setQueryString(queryString);
            } 
            getRepositoryQuery().setAttribute(CloudDevConstants.QUERY_CRITERIA, queryString);      
            refreshIntern(autoRefresh);
        }

        @Override
        protected boolean isModifiable() {
            return true;
        }
        
        @Override
        public synchronized boolean save(String name) { // XXX sync me properly !!!
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
            
            ProjectAndClient pac = getProjectAndClient();
            if(pac == null) {
                ODCS.LOG.log(Level.WARNING, "couldn''t save query : {0}", name); // NOI18N
                return false;
            }
            
            if(savedQuery == null) {
                savedQuery = new SavedTaskQuery();
                savedQuery.setDefaultSort(new SortInfo("taskId")); // XXX constant  ???
                savedQuery.setName(name);
                savedQuery.setQueryString(getController().getQueryString());
                try {
                    savedQuery = pac.client.createQuery(pac.projectId, savedQuery);
                } catch (ODCSException ex) {
                    ODCS.LOG.log(Level.WARNING, "exception while creating query : " + name, ex); // NOI18N
                    return false;
                }
            } else {
                savedQuery.setName(name);
                savedQuery.setQueryString(getController().getQueryString());
                try {
                    savedQuery = pac.client.updateQuery(pac.projectId, savedQuery);
                } catch (ODCSException ex) {
                    ODCS.LOG.log(Level.WARNING, "exception while creating query : " + name, ex); // NOI18N
                    return false;
                }
            }

            if (repositoryQuery != null) {
                repositoryQuery.setSummary(name);
            }
            setSaved(name); 
            getRepository().saveQuery(this);
            return true;
        }        

        @Override
        public synchronized void remove() {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
            
            ProjectAndClient pac = getProjectAndClient();
            if(pac == null) {
                ODCS.LOG.log(Level.WARNING, "couldn''t save query : {0}", getDisplayName()); // NOI18N
                return;
            }
            
            try {
                pac.client.deleteQuery(pac.projectId, savedQuery.getId());
            } catch (ODCSException ex) {
                ODCS.LOG.log(Level.WARNING, "exception while removing query : " + getDisplayName(), ex); // NOI18N
                return;
            }
            getRepository().removeQuery(this);
            if (repositoryQuery != null) {
                MylynSupport.getInstance().deleteQuery(repositoryQuery);
            }
        }
        
        private class ProjectAndClient {
            String projectId;
            ODCSClient client;

            public ProjectAndClient(String projectId, ODCSClient client) {
                this.projectId = projectId;
                this.client = client;
            }
        }

        private ProjectAndClient getProjectAndClient() {
            TeamProject kp = getRepository().getTeamProject();
            assert kp != null; // all odcs repositories should come from team support
            if (kp == null) {
                ODCS.LOG.log(Level.WARNING, "  no project available for query"); // NOI18N
                return null;
            }
            String url = kp.getFeatureLocation();
            if(url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            url = url.substring(0, url.length() - "/tasks".length()); // NOI18N
            String projectId = url.substring(url.lastIndexOf("/") + 1, url.length()); // NOI18N
            url = url.substring(0, url.length() - ("/s/" + projectId).length()); // NOI18N
            AuthenticationCredentials c = getRepository().getTaskRepository().getCredentials(AuthenticationType.REPOSITORY); // XXX repository info doesn't contain creds
            ODCSClient client = ODCSFactory.getInstance().createClient(url, new PasswordAuthentication(c.getUserName(), c.getPassword().toCharArray()));        

            return new ProjectAndClient(projectId, client);
        }
    }

    private static class PredefinedQuery extends ODCSQuery {
        private final IRepositoryQuery repositoryQuery;

        public PredefinedQuery(ODCSRepository repository, String name, IRepositoryQuery predefinedQuery) {
            super(repository, name);
            repositoryQuery = predefinedQuery;
        }
        
        @Override
        protected IRepositoryQuery getRepositoryQuery() {
            return repositoryQuery;
        }

        @Override
        protected Criteria getCriteria() {
            return null;
        }

        @Override
        protected void refresh(boolean autoRefresh) {
            refreshIntern(autoRefresh);
        }

        @Override
        protected boolean isModifiable() {
            return false;
        }

        @Override
        protected boolean save(String name) {
            throw new UnsupportedOperationException("Can't save a predefined query."); // NOI18N
        }

        @Override
        public void remove() {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                NbBundle.getMessage(ODCSQueryController.class, "MSG_CantRemoveQuery"), // NOI18N
                NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}

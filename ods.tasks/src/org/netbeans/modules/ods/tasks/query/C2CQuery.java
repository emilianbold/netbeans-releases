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
package org.netbeans.modules.ods.tasks.query;

import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiProject;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.LogUtils;
import org.netbeans.modules.mylyn.util.PerformQueryCommand;
import org.netbeans.modules.ods.client.api.ODSClient;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.client.api.ODSFactory;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.C2CConnector;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.netbeans.modules.ods.tasks.spi.C2CData;


/**
 *
 * @author Tomas Stupka
 */
public abstract class C2CQuery {

    private final C2CRepository repository;
    private C2CQueryController controller;

    private final List<QueryNotifyListener> notifyListeners = new ArrayList<QueryNotifyListener>();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);;
    private String name;
    private long lastRefresh;
    
    private boolean saved;

    private boolean firstRun = true;
    private ColumnDescriptor[] columnDescriptors;
    private OwnerInfo info;
    
    public static C2CQuery createNew(C2CRepository repository) {
        return new CustomQuery(repository, null);
    }
    
    public static C2CQuery createSaved(C2CRepository repository, SavedTaskQuery stq) {
        return new CustomQuery(repository, stq);
    }
    
    public static C2CQuery createPredefined(C2CRepository repository, String name, IRepositoryQuery predefinedQuery) {
        return new PredefinedQuery(repository, name, predefinedQuery);
    }
        
    protected abstract void refresh(boolean autoRefresh);
    protected abstract Criteria getCriteria();
    protected abstract IRepositoryQuery getRepositoryQuery();
    protected abstract boolean isModifiable();
    protected abstract boolean save(String name);
    public abstract void remove();
    
    protected C2CQuery(C2CRepository repository, String name) {
        this.name = name;
        this.repository = repository;
        this.saved = name != null;
    }
    
    public C2CRepository getRepository() {
        return repository;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public void setOwnerInfo (OwnerInfo info) {
        this.info = info;
    }

    public OwnerInfo getOwnerInfo () {
        return info;
    }

    int getSize() {
        return issues.size();
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

    public boolean contains(String id) {
        return issues.contains(id);
    }

    public Collection<C2CIssue> getIssues() {
        if (issues == null) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<String>();
        synchronized (issues) {
            ids.addAll(issues);
        }

        IssueCache<C2CIssue, TaskData> cache = repository.getIssueCache();
        List<C2CIssue> ret = new ArrayList<C2CIssue>();
        for (String id : ids) {
            ret.add(cache.getIssue(id));
        }
        return ret;
    }

    public ColumnDescriptor[] getColumnDescriptors() {
        if(columnDescriptors == null) {
            columnDescriptors = C2CIssue.getColumnDescriptors(repository);
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

    protected void fireNotifyData(C2CIssue issue) {
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

    public final C2CQueryController getController () {
        if(controller == null) {
            controller = new C2CQueryController(repository, this, getCriteria(), isModifiable());
        }
        return controller;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void fireQuerySaved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_SAVED, null, null);
    }

    protected void fireQueryRemoved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_REMOVED, null, null);
    }

    private void fireQueryIssuesChanged() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_ISSUES_CHANGED, null, null);
    }  

    public void refresh() {
        refreshIntern(false);
    }
    
    private final Set<String> issues = new HashSet<String>();
    private Set<String> archivedIssues = new HashSet<String>();
    public void refreshIntern(final boolean autoRefresh) {
        
//        assert if query was provided with parameters from controller
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
          
        executeQuery(new Runnable() {
            @Override
            public void run() {
                C2C.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new Object[] {name, getRepositoryQuery().getAttribute(C2CData.ATTR_QUERY_CRITERIA)}); // NOI18N
                try {
                    
                    // keeps all issues we will retrieve from the server
                    // - those matching the query criteria
                    // - and the obsolete ones
                    Set<String> queryIssues = new HashSet<String>();

                    issues.clear();
                    archivedIssues.clear();
                    if(isSaved()) {
                        if(!wasRun() && !issues.isEmpty()) {
                            C2C.LOG.log(Level.WARNING, "query {0} supposed to be run for the first time yet already contains issues.", getDisplayName()); // NOI18N
                            assert false;
                        }
                        // read the stored state ...
                        queryIssues.addAll(repository.getIssueCache().readQueryIssues(getDisplayName()));
                        queryIssues.addAll(repository.getIssueCache().readArchivedQueryIssues(getDisplayName()));
                        // ... and they might be rendered obsolete if not returned by the query
                        archivedIssues.addAll(queryIssues);
                    }
                    firstRun = false;

                    // run query to know what matches the criteria
                    IssuesCollector ic = new IssuesCollector();
                    PerformQueryCommand queryCmd = 
                        new PerformQueryCommand(
                            C2C.getInstance().getRepositoryConnector(),
                            repository.getTaskRepository(), 
                            ic,
                            getRepositoryQuery());
                    repository.getExecutor().execute(queryCmd, true, !autoRefresh);
                    if(queryCmd.hasFailed()) {
                        return;
                    }

                    // only issues not returned by the query are obsolete
                    archivedIssues.removeAll(issues);
                    if(isSaved()) {
                        // ... and store all issues you got
                        repository.getIssueCache().storeQueryIssues(getDisplayName(), issues.toArray(new String[issues.size()]));
                        repository.getIssueCache().storeArchivedQueryIssues(getDisplayName(), archivedIssues.toArray(new String[archivedIssues.size()]));
                    }

                    // now get the task data for
                    // - all issue returned by the query
                    // - and issues which were returned by some previous run and are archived now
                    queryIssues.addAll(issues);

                    //XXX opened issues must have complete task data
                    //is there another way?
                    if (!ic.openedIssues.isEmpty()) {
                        getController().switchToDeterminateProgress(ic.openedIssues.size());
                        for (C2CIssue issue : ic.openedIssues) {
                            getController().addProgressUnit(issue.getDisplayName());
                            repository.getIssue(issue.getID());
                        }
                    }                    
                } finally {
                    logQueryEvent(issues.size(), autoRefresh);
                    if(C2C.LOG.isLoggable(Level.FINE)) {
                        C2C.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new Object[] {name, getRepositoryQuery().getAttribute(C2CData.ATTR_QUERY_CRITERIA)}); // NOI18N
                    }
                }
            }
        });
    }

    protected void logQueryEvent(int count, boolean autoRefresh) {
        LogUtils.logQueryEvent(
            C2CConnector.ID,
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

    private class IssuesIdCollector extends TaskDataCollector {
        public IssuesIdCollector() {}
        @Override
        public void accept(TaskData taskData) {
            String id = C2CIssue.getID(taskData);
            issues.add(id);
        }
    };
    private class IssuesCollector extends TaskDataCollector {
        List<C2CIssue> openedIssues = new LinkedList<C2CIssue>();
        
        public IssuesCollector() {}
        @Override
        public void accept(TaskData taskData) {
            String id = C2CIssue.getID(taskData);
            issues.add(id);
            C2CIssue issue;
            try {
                IssueCache<C2CIssue, TaskData> cache = repository.getIssueCache();
                issue = cache.setIssueData(id, taskData);
                if (!issue.isNew() && issue.isOpened()) {
                    openedIssues.add(issue);
                }
            } catch (IOException ex) {
                C2C.LOG.log(Level.SEVERE, null, ex);
                return;
            }
            fireNotifyData(issue); // XXX - !!! triggers getIssues()
        }
    };    
    
    private static class CustomQuery extends C2CQuery {

        private IRepositoryQuery repositoryQuery;
        private SavedTaskQuery savedQuery;
                
        public CustomQuery(C2CRepository repository, SavedTaskQuery savedQuery) {
            super(repository, savedQuery != null ? savedQuery.getName() : null);
            this.savedQuery = savedQuery;
        }

        @Override
        protected IRepositoryQuery getRepositoryQuery() {
            // XXX synchronize
            if(repositoryQuery == null) {
                repositoryQuery = new RepositoryQuery(C2C.getInstance().getRepositoryConnector().getConnectorKind(), "ODS query -" + getDisplayName()); // NOI18N
            }
            return repositoryQuery;
        }

        @Override
        protected Criteria getCriteria() { 
            return savedQuery != null ? savedQuery.getQueryCriteria() : null;
        }

        @Override
        protected void refresh(boolean autoRefresh) {
            String queryString = getController().getQueryString();
            // XXX what if queryString == null
            if(savedQuery != null) {
                savedQuery.setQueryString(queryString);
            } 
            getRepositoryQuery().setAttribute(C2CData.ATTR_QUERY_CRITERIA, queryString);
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
                C2C.LOG.log(Level.WARNING, "couldn''t save query : {0}", name); // NOI18N
                return false;
            }
            
            if(savedQuery == null) {
                savedQuery = new SavedTaskQuery();
                savedQuery.setDefaultSort(new SortInfo("taskId")); // XXX constant  ???
                savedQuery.setName(name);
                savedQuery.setQueryString(getController().getQueryString());
                try {
                    savedQuery = pac.client.createQuery(pac.projectId, savedQuery);
                } catch (ODSException ex) {
                    C2C.LOG.log(Level.WARNING, "exception while creating query : " + name, ex); // NOI18N
                    return false;
                }
            } else {
                savedQuery.setName(name);
                savedQuery.setQueryString(getController().getQueryString());
                try {
                    savedQuery = pac.client.updateQuery(pac.projectId, savedQuery);
                } catch (ODSException ex) {
                    C2C.LOG.log(Level.WARNING, "exception while creating query : " + name, ex); // NOI18N
                    return false;
                }
            }

            setSaved(name); 
            getRepository().saveQuery(this);
            fireQuerySaved();            
            return true;
        }        

        @Override
        public synchronized void remove() {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
            
            ProjectAndClient pac = getProjectAndClient();
            if(pac == null) {
                C2C.LOG.log(Level.WARNING, "couldn''t save query : {0}", getDisplayName()); // NOI18N
                return;
            }
            
            try {
                pac.client.deleteQuery(pac.projectId, savedQuery.getId());
            } catch (ODSException ex) {
                C2C.LOG.log(Level.WARNING, "exception while removing query : " + getDisplayName(), ex); // NOI18N
                return;
            }
            getRepository().removeQuery(this);
            fireQueryRemoved();
        }
        
        private class ProjectAndClient {
            String projectId;
            ODSClient client;

            public ProjectAndClient(String projectId, ODSClient client) {
                this.projectId = projectId;
                this.client = client;
            }
        }

        private ProjectAndClient getProjectAndClient() {
            KenaiProject kp = getRepository().getLookup().lookup(KenaiProject.class);
            assert kp != null; // all c2c repositories should come from team support
            if (kp == null) {
                C2C.LOG.log(Level.WARNING, "  no project available for query"); // NOI18N
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
            ODSClient client = ODSFactory.getInstance().createClient(url, new PasswordAuthentication(c.getUserName(), c.getPassword().toCharArray()));        

            return new ProjectAndClient(projectId, client);
        }
    }

    
    private static class PredefinedQuery extends C2CQuery {
        private final IRepositoryQuery repositoryQuery;

        public PredefinedQuery(C2CRepository repository, String name, IRepositoryQuery predefinedQuery) {
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
            // XXX this is called from API, need a proper error msg
            throw new UnsupportedOperationException("Can't remove a predefined query."); // NOI18N
        }
    }
}

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
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.LogUtils;
import org.netbeans.modules.mylyn.util.PerformQueryCommand;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.client.api.ODCSFactory;
import org.netbeans.modules.odcs.tasks.C2C;
import org.netbeans.modules.odcs.tasks.C2CConnector;
import org.netbeans.modules.odcs.tasks.issue.C2CIssue;
import org.netbeans.modules.odcs.tasks.repository.C2CRepository;
import org.netbeans.modules.odcs.tasks.spi.C2CData;
import org.netbeans.modules.odcs.tasks.util.C2CUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


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
    
    private final Object ISSUES_LOCK = new Object();
    private final Set<String> issues = new HashSet<String>();
    
    public static C2CQuery createNew(C2CRepository repository) {
        return new CustomQuery(repository);
    }
    
    public static C2CQuery createNew(C2CRepository repository, Criteria criteria) {
        return new CustomQuery(repository, criteria);
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

    public boolean contains(String id) {
        synchronized(ISSUES_LOCK) {
            return issues.contains(id);
        }
    }

    public Collection<C2CIssue> getIssues() {
        List<String> ids;
        synchronized(ISSUES_LOCK) {
            if (issues == null) {
                return Collections.emptyList();
            }
            ids = new ArrayList<String>(issues.size());
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
    
    public void refreshIntern(final boolean autoRefresh) {
        
//        assert if query was provided with parameters from controller
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
          
        executeQuery(new Runnable() {
            @Override
            public void run() {
                C2C.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new Object[] {name, getRepositoryQuery().getAttribute(C2CData.ATTR_QUERY_CRITERIA)}); // NOI18N
                IssuesCollector ic = new IssuesCollector();
                try {
                    
                    synchronized(ISSUES_LOCK) {
                        issues.clear();
                    }
                    
                    firstRun = false;

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

                    if(isSaved()) {
                        synchronized(ISSUES_LOCK) {
                            // store all issues you got
                            repository.getIssueCache().storeQueryIssues(getDisplayName(), issues.toArray(new String[issues.size()]));
                        }
                    }

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
                    synchronized(ISSUES_LOCK) {
                        logQueryEvent(issues.size(), autoRefresh);
                    }
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

    private class IssuesCollector extends TaskDataCollector {
        List<C2CIssue> openedIssues = new LinkedList<C2CIssue>();
        
        public IssuesCollector() {}
        @Override
        public void accept(TaskData taskData) {
            String id = C2CIssue.getID(taskData);
            synchronized(ISSUES_LOCK) {
                issues.add(id);
            }
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
        private Criteria criteria;
                
        public CustomQuery(C2CRepository repository) {
            this(repository, (SavedTaskQuery) null);
        }
        
        public CustomQuery(C2CRepository repository, SavedTaskQuery savedQuery) {
            super(repository, savedQuery != null ? savedQuery.getName() : null);
            this.savedQuery = savedQuery;
            this.criteria = null;
        }
        
        public CustomQuery(C2CRepository repository, Criteria criteria) {
            super(repository, null);
            this.savedQuery = null;
            this.criteria = criteria;
        }

        @Override
        protected IRepositoryQuery getRepositoryQuery() {
            // XXX synchronize
            if(repositoryQuery == null) {
                repositoryQuery = new RepositoryQuery(C2C.getInstance().getRepositoryConnector().getConnectorKind(), "ODCS query -" + getDisplayName()); // NOI18N
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
                } catch (ODCSException ex) {
                    C2C.LOG.log(Level.WARNING, "exception while creating query : " + name, ex); // NOI18N
                    return false;
                }
            } else {
                savedQuery.setName(name);
                savedQuery.setQueryString(getController().getQueryString());
                try {
                    savedQuery = pac.client.updateQuery(pac.projectId, savedQuery);
                } catch (ODCSException ex) {
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
            } catch (ODCSException ex) {
                C2C.LOG.log(Level.WARNING, "exception while removing query : " + getDisplayName(), ex); // NOI18N
                return;
            }
            getRepository().removeQuery(this);
            fireQueryRemoved();
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
            ODCSClient client = ODCSFactory.getInstance().createClient(url, new PasswordAuthentication(c.getUserName(), c.getPassword().toCharArray()));        

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
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                NbBundle.getMessage(C2CQueryController.class, "MSG_CantRemoveQuery"), // NOI18N
                NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}

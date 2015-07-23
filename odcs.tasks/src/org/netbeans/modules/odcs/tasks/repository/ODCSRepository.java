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
package org.netbeans.modules.odcs.tasks.repository;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevClient;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.team.spi.TeamAccessor;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.MylynUtils;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.ODCSConnector;
import org.netbeans.modules.odcs.tasks.ODCSExecutor;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.query.ODCSQuery;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.netbeans.modules.mylyn.util.UnsubmittedTasksContainer;
import org.netbeans.modules.mylyn.util.commands.SimpleQueryCommand;
import org.netbeans.modules.odcs.tasks.query.QueryParameters;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSRepository implements PropertyChangeListener {

    private final Object INFO_LOCK = new Object();
    private final Object QUERIES_LOCK = new Object();
    private RepositoryInfo info;
    private ODCSRepositoryController controller;
    private TaskRepository taskRepository;
    private Cache cache;
    private ODCSExecutor executor;
    private Map<PredefinedTaskQuery, ODCSQuery> predefinedQueries;
    private Collection<ODCSQuery> queries;
    private static final String ICON_PATH = "org/netbeans/modules/odcs/tasks/resources/repository.png"; //NOI18N
    private final Image icon;
    
    private PropertyChangeSupport support;
    
    private TeamProject project;
    private UnsubmittedTasksContainer unsubmittedTasksContainer;
    private PropertyChangeListener unsubmittedTasksListener;
    private final Object CACHE_LOCK = new Object();
    
    public ODCSRepository (TeamProject project) {
        this(createInfo(project.getDisplayName(), project.getFeatureLocation(), project)); // use name as id - can'npe be changed anyway
        assert project != null;
        this.project = project;

        String projectHost = project.getHost();
        if (projectHost != null) {
            TeamAccessor teamAccessor = TeamAccessorUtils.getTeamAccessor(projectHost);
            if (teamAccessor != null) {
                teamAccessor.addPropertyChangeListener(this, projectHost);
            } else {
                ODCS.LOG.log(Level.WARNING, "No TeamAccessor available for {0} project from {1}.", new Object[] {project.getName(), project.getWebLocation()}); // NOI18N
                assert false : "Missing server entry"; // NOI18N
            }
        } else {
            ODCS.LOG.log(Level.WARNING, "Project {0} from unknown host.", project.getName()); // NOI18N
            assert false : "Project with unknown host"; // NOI18N
        }
    }

    public ODCSRepository() {
        this.icon = ImageUtilities.loadImage(ICON_PATH, true);
        this.support = new PropertyChangeSupport(this);
    }
    
    private ODCSRepository(RepositoryInfo info) {
        this();
        this.info = info;
        
        String name = info.getDisplayName();
        String url = info.getUrl();
        taskRepository = setupTaskRepository(name, null, url, "", new char[0], "", new char[0]);
    }

    @NbBundle.Messages({"# {0} - repository name", "# {1} - url", "LBL_RepositoryTooltipNoUser={0} : {1}"})
    private static RepositoryInfo createInfo (String repoName, String url, TeamProject project) {
        String id = getRepositoryId(repoName, url);
        String tooltip = Bundle.LBL_RepositoryTooltipNoUser(repoName, url);
        RepositoryInfo i = new RepositoryInfo(id, ODCSConnector.ID, url, repoName, tooltip);
        i.putValue(TeamBugtrackingConnector.TEAM_PROJECT_NAME, project.getName());
        return i;
    }
    
    private static String getRepositoryId (String name, String url) {
        return TextUtils.encodeURL(url) + ":" + name; //NOI18N
    }
    
    public TeamProject getKenaiProject () {
        return project;
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TeamAccessor.PROP_LOGIN)) {
            new RequestProcessor("ODCS Tasks - team logout", 1).post(new Runnable() {
                @Override
                public void run() {
                    String user;
                    char[] psswd;
                    PasswordAuthentication pa = getAuthentication(); 
                    if (pa != null) {
                        user = pa.getUserName();
                        psswd = pa.getPassword();
                    } else {
                        user = ""; //NOI18N
                        psswd = new char[0];

                        cancelQueries();
                    }

                    setCredentials(user, psswd, null, null);
                }
            });
        }
    }
            
    private void cancelQueries() {
        final List<ODCSQuery> tmpQueries = new LinkedList<>();
        synchronized (QUERIES_LOCK) {
            if(queries == null) {
                return;
            }
            
            tmpQueries.addAll(queries);
            queries = null;
            predefinedQueries = null;
        }
        
        for (ODCSQuery q : tmpQueries) {
            ODCS.LOG.log(Level.FINE, "Cancelling query: {0}", q.getDisplayName());
            q.getController().cancel();
        }
    }

    public boolean isLoggedIn() {
        return getAuthentication() != null;
    }
    
    private PasswordAuthentication getAuthentication() {
        return TeamAccessorUtils.getPasswordAuthentication(project.getWebLocation().toString(), false); // do not force login
    }

    public void ensureCredentials() {
        PasswordAuthentication pa = TeamAccessorUtils.getPasswordAuthentication(project.getWebLocation().toString(), false);
        if(pa == null) {
            return;
        }
        
        String user = pa.getUserName();
        char[] password = pa.getPassword();

        setCredentials(user, password, null, null);
    }
    
    public synchronized void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {
        if (taskRepository == null) {
            return;
        }
        String oldUser = taskRepository.getUserName();
        if (oldUser == null) {
            oldUser = ""; //NOI18N
        }
        setupProperties(taskRepository, taskRepository.getRepositoryLabel(), user, password, httpUser, httpPassword);
    }

    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        String id = info != null ? info.getID() : name + System.currentTimeMillis();
        info = new RepositoryInfo(id, ODCSConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
        info.putValue(TeamBugtrackingConnector.TEAM_PROJECT_NAME, project.getName());
        setTaskRepository(name, url, user, password, httpUser, httpPassword);
    }
    
    private void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        taskRepository = setupTaskRepository(name, oldUrl.equals(url) ? null : oldUrl,
                url, user, password, httpUser, httpPassword);
    }    
    
    /**
     * If oldUrl is not null, gets the repository for the oldUrl and rewrites it
     * to the new url.
     */
    private static TaskRepository setupTaskRepository (String name, String oldUrl, String url, String user,
            char[] password, String httpUser, char[] httpPassword) {
        TaskRepository repository;
        if (oldUrl == null) {
            repository = MylynSupport.getInstance().getTaskRepository(ODCS.getInstance().getRepositoryConnector(), url);
        } else {
            repository = MylynSupport.getInstance().getTaskRepository(ODCS.getInstance().getRepositoryConnector(), oldUrl);
            try {
                MylynSupport.getInstance().setRepositoryUrl(repository, url);
            } catch (CoreException ex) {
                ODCS.LOG.log(Level.WARNING, null, ex);
            }
        }
        setupProperties(repository, name, user, password, httpUser, httpPassword); 
        return repository;
    }
    
    private static void setupProperties (TaskRepository repository, String displayName,
            String user, char[] password, String httpUser, char[] httpPassword) {
        repository.setRepositoryLabel(displayName);
        
        // set u/p as well as http u/p which is being used by the odcs http client
        if(httpUser == null || httpUser.trim().equals("")) {
            httpUser = user;
        }
        if(httpPassword == null || new String(httpPassword).trim().equals("")) {
            httpPassword = password;
        }
        
        MylynUtils.setCredentials(repository, user, password, httpUser, httpPassword);
    }
    
    public TaskRepository getTaskRepository() {
        return taskRepository;
    }
            
    public RepositoryInfo getInfo() {
        synchronized(INFO_LOCK) {
            return info;
        }
    }

    public String getDisplayName() {
        return info.getDisplayName();
    }
    
    public String getUrl() {
        return info.getUrl();
    }
    
    public Image getIcon() {
        return icon;
    }

    public void remove() {
        cancelQueries();
    }

    public ODCSIssue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        ODCSIssue issue = findUnsubmitted(id);
        if (issue == null) {
            try {
                NbTask task = MylynSupport.getInstance().getTask(getUrl(), id);
                if (task != null) {
                    issue = getIssueForTask(task);
                }
            } catch (CoreException ex) {
                ODCS.LOG.log(Level.WARNING, null, ex);
            }
        }
        if (issue == null) {
            issue = getIssueForTask(ODCSUtil.getRepositoryTask(this, id, true));
        }
        return issue;
    }

    
    public RepositoryController getControler() {
        if(controller == null) {
            controller = new ODCSRepositoryController(this);
        }
        return controller;
    }

    public Collection<ODCSIssue> simpleSearch(String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<ODCSIssue> issues = new ArrayList<>();
        
        if(keywords.length == 1 && isInteger(keywords[0])) {
            ODCSIssue issue = getIssueForTask(ODCSUtil.getRepositoryTask(this, keywords[0], false));
            if (issue != null) {
                issues.add(issue);
            }
        }

        try {
            criteria = URLEncoder.encode(criteria, getTaskRepository().getCharacterEncoding());
        } catch (UnsupportedEncodingException ueex) {
            ODCS.LOG.log(Level.INFO, null, ueex);
            try {
                criteria = URLEncoder.encode(criteria, "UTF-8"); // NOI18N
            } catch (UnsupportedEncodingException ex) {
                // should not happen
            }
        }

        try {
            // XXX shouldn'npe be only a perfect match 
            IRepositoryQuery iquery = MylynSupport.getInstance().createNewQuery(taskRepository, "ODCS simple task search"); //NOI18N
            iquery.setUrl(CloudDevConstants.CRITERIA_QUERY);
            iquery.setAttribute(
                        CloudDevConstants.QUERY_CRITERIA, 
                        new ColumnCriteria(
                            QueryParameters.Column.SUMMARY.getColumnName(), 
                            Criteria.Operator.EQUALS, 
                            criteria).toQueryString()); 
            SimpleQueryCommand cmd = MylynSupport.getInstance().getCommandFactory().createSimpleQueryCommand(taskRepository, iquery);
            getExecutor().execute(cmd, true, false);
            for (NbTask task : cmd.getTasks()) {
                ODCSIssue issue = getIssueForTask(task);
                if (issue != null) {
                    issues.add(issue);
                }
            }
        } catch (CoreException ex) {
            // should not happen
            ODCS.LOG.log(Level.WARNING, null, ex);
        }
        return issues;
    }
    
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }    

    public Collection<ODCSQuery> getQueries() {
        List<ODCSQuery> ret = new ArrayList<>();
        synchronized (QUERIES_LOCK) {
            
            if (queries == null && isLoggedIn()) {
                queries = new HashSet<>();
                
                // predefined
                initializePredefinedQueries();
                queries.addAll(predefinedQueries.values());

                if(ODCS.LOG.isLoggable(Level.FINER)) {
                    for (ODCSQuery q : queries) {
                        ODCS.LOG.log(Level.FINER, "added predefined query {0} to repository {1}", new Object[]{q.getDisplayName(), getDisplayName()});
                    }
                }
                
                // remote
                List<ODCSQuery> remoteQueries = new ArrayList<>();
                ensureCredentials();
                RepositoryConfiguration conf = getRepositoryConfiguration(false);
                if (conf != null) {
                    List<SavedTaskQuery> savedQueries = conf.getSavedTaskQueries();
                    for (SavedTaskQuery sq : savedQueries) {
                        ODCSQuery q = ODCSQuery.createSaved(this, sq);
                        remoteQueries.add(q);

                        ODCS.LOG.log(Level.FINER, "added remote query {0} to repository {1}", new Object[]{sq.getName(), getDisplayName()});
                    }
                }
        
                if(queries != null && !remoteQueries.isEmpty() && isLoggedIn()) {
                    queries.addAll(remoteQueries);
                }
            } 
            
            if(queries != null) {
                ret.addAll(queries);
            }
        }
        
        return ret;
    }
    
    private void initializePredefinedQueries () {
        if (predefinedQueries == null) {
            Map<PredefinedTaskQuery, IRepositoryQuery> queries = new EnumMap<>(PredefinedTaskQuery.class);
            if(!Boolean.getBoolean("odcs.tasks.noPredefinedQueries")) { // NOI18N
                Collection<String> queriesToSkip = getQueriesToSkip();
                for (PredefinedTaskQuery ptq : PredefinedTaskQuery.values()) {
                    if(ptq == PredefinedTaskQuery.RECENT || queriesToSkip.contains(ptq.toString())) {
                        continue;
                    }
                    try {
                        MylynSupport supp = MylynSupport.getInstance();
                        IRepositoryQuery query = supp.getRepositoryQuery(getTaskRepository(), ODCSUtil.getPredefinedQueryName(ptq));
                        if (query == null) {
                            query = supp.createNewQuery(taskRepository, ODCSUtil.getPredefinedQueryName(ptq));
                            query.setUrl(CloudDevConstants.PREDEFINED_QUERY);
                            query.setAttribute(CloudDevConstants.QUERY_NAME, ptq.toString());
                            supp.addQuery(taskRepository, query);
                        }
                        queries.put(ptq, query);
                    } catch (CoreException ex) {
                        ODCS.LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
            synchronized(QUERIES_LOCK) {
                predefinedQueries = new EnumMap<>(PredefinedTaskQuery.class);
                for (Map.Entry<PredefinedTaskQuery, IRepositoryQuery> e : queries.entrySet()) {
                    predefinedQueries.put(e.getKey(), ODCSQuery.createPredefined(ODCSRepository.this, e.getValue().getSummary(), e.getValue()));
                    
                    ODCS.LOG.log(Level.FINER, "added predefined query {0} to repository {1}", new Object[]{e.getKey().name(), getDisplayName()});
                }
            }
        }
    }

    private static Collection<String> getQueriesToSkip() {
        try {
            String prop = System.getProperty("odcs.tasks.queriesToSkip"); // NOI18N
            if(prop == null || prop.trim().isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            StringTokenizer tk = new StringTokenizer(prop, ",");
            Set<String> ret = new HashSet<>(PredefinedTaskQuery.values().length);
            while(tk.hasMoreTokens()) {
                String t = tk.nextToken().trim();
                if(!t.isEmpty()) {
                    ret.add(t);
                }
            }
            return ret;
        } catch (Throwable t) {
            t.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }
    
    public final ODCSQuery getPredefinedQuery (PredefinedTaskQuery ptq) {
        getQueries();
        synchronized (QUERIES_LOCK) {
            initializePredefinedQueries();
            return predefinedQueries.get(ptq);
        }
    }

    public ODCSIssue createIssue() {
        NbTask task;
        try {
            task = MylynSupport.getInstance().createTask(taskRepository, new TaskMapping());
            return getIssueForTask(task);
        } catch (OperationCanceledException ex) {
            // creation of new task may be immediately canceled
            // happens when more repositories are available and
            // the RepoComboSupport immediately switches to another repo
            ODCS.LOG.log(Level.FINE, null, ex);
            return null;
        } catch (CoreException ex) {
            ODCS.LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    public ODCSQuery createQuery() {
        return ODCSQuery.createNew(this);
    }

    public List<ODCSIssue> getIssues(String[] ids) {
        if (ids.length == 0) {
            return Collections.emptyList();
        } else {
            //TODO is there a bulk command?
            List<ODCSIssue> issues = new ArrayList<>(ids.length);
            for (String id : ids) {
                ODCSIssue i = getIssue(id);
                if (i != null) {
                    issues.add(i);
                }
            }
            return issues;
        }
    }
    
    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(ODCSRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    public String getID() {
        return info.getID();
    }

    public Cache getIssueCache() {
        synchronized (CACHE_LOCK) {
            if(cache == null) {
                cache = new Cache();
            }
            return cache;
        }
    }

    public ODCSExecutor getExecutor() {
        if(executor == null) {
            executor = new ODCSExecutor(this);
        }
        return executor;
    }

    public void removeQuery(ODCSQuery query) {
        synchronized (QUERIES_LOCK) {
            queries.remove(query);
        }
        fireQueryListChanged();
    }

    public void saveQuery(ODCSQuery query) {
        synchronized (QUERIES_LOCK) {
            queries.add(query);
        }
        fireQueryListChanged();
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireQueryListChanged() {
        support.firePropertyChange(RepositoryProvider.EVENT_QUERY_LIST_CHANGED, null, null);
    }
    
    private void fireUnsubmittedIssuesChanged() {
        ODCS.LOG.log(Level.FINER, "firing unsubmitted issues changed for repository {0}", new Object[] { getDisplayName() }); //NOI18N
        support.firePropertyChange(RepositoryProvider.EVENT_UNSUBMITTED_ISSUES_CHANGED, null, null);
    }

    public void refreshConfiguration() {
        getRepositoryConfiguration(true);
    }

    public synchronized RepositoryConfiguration getRepositoryConfiguration(boolean forceRefresh) {
        CloudDevClient client = ODCS.getInstance().getCloudDevClient(taskRepository);
        RepositoryConfiguration configuration = null;
        try {
            configuration = client.getRepositoryConfiguration(forceRefresh, new NullProgressMonitor());
        } catch (CoreException | RuntimeException ex) {
            Throwable t = ex.getCause();
            String msg = t != null ? t.getMessage() : ex.getMessage();
            Level level;
            if(msg != null) {
                level = Level.INFO;
                ODCSExecutor.notifyErrorMessage(msg);
            } else {
                level = Level.WARNING;
            }
            ODCS.LOG.log(level, "Trying to access " + getUrl() + " resulted in an exception:", ex);
            
        }
        return configuration;
    }
    
    public ODCSIssue getIssueForTask (NbTask task) {
        ODCSIssue issue = null;
        if (task != null) {
            synchronized (CACHE_LOCK) {
                String taskId = ODCSIssue.getID(task);
                Cache issueCache = getIssueCache();
                issue = issueCache.getIssue(taskId);
                if (issue == null) {
                    issue = issueCache.setIssue(taskId, new ODCSIssue(task, this));
                }
            }
        }
        return issue;
    }

    private ODCSIssue findUnsubmitted (String id) {
        try {
            for (NbTask task : getUnsubmittedTasksContainer().getTasks()) {
                if (id.equals("-" + task.getTaskId())) {
                    return getIssueForTask(task);
                }
            }
        } catch (CoreException ex) {
            ODCS.LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    private UnsubmittedTasksContainer getUnsubmittedTasksContainer () throws CoreException {
        synchronized (this) {
            if (unsubmittedTasksContainer == null) {
                unsubmittedTasksContainer = MylynSupport.getInstance().getUnsubmittedTasksContainer(getTaskRepository());
                unsubmittedTasksContainer.addPropertyChangeListener(WeakListeners.propertyChange(unsubmittedTasksListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (UnsubmittedTasksContainer.EVENT_ISSUES_CHANGED.equals(evt.getPropertyName())) {
                            fireUnsubmittedIssuesChanged();
                        }
                    }
                }, unsubmittedTasksContainer));
            }
            return unsubmittedTasksContainer;
        }
    }

    public void taskDeleted (String taskId) {
        getIssueCache().removeIssue(taskId);
    }

    public Collection<ODCSIssue> getUnsubmittedIssues () {
        try {
            UnsubmittedTasksContainer cont = getUnsubmittedTasksContainer();
            List<NbTask> unsubmittedTasks = cont.getTasks();
            List<ODCSIssue> unsubmittedIssues = new ArrayList<>(unsubmittedTasks.size());
            for (NbTask task : unsubmittedTasks) {
                ODCSIssue issue = getIssueForTask(task);
                if (issue != null) {
                    unsubmittedIssues.add(issue);
                }
            }
            return unsubmittedIssues;
        } catch (CoreException ex) {
            ODCS.LOG.log(Level.INFO, null, ex);
            return Collections.<ODCSIssue>emptyList();
        }
    }

    public TeamProject getTeamProject() {
        return project;
    }

    public boolean needsAndHasNoLogin(ODCSQuery q) {
        return (q != getPredefinedQuery(PredefinedTaskQuery.ALL)
               || q != getPredefinedQuery(PredefinedTaskQuery.RECENT))
            && !isLoggedIn();
    }

    public class Cache {
        private final Map<String, Reference<ODCSIssue>> issues = new HashMap<>();
        
        Cache() { }

        public ODCSIssue getIssue (String id) {
            synchronized (CACHE_LOCK) {
                Reference<ODCSIssue> issueRef = issues.get(id);
                return issueRef == null ? null : issueRef.get();
            }
        }

        public ODCSIssue setIssue (String id, ODCSIssue issue) {
            synchronized (CACHE_LOCK) {
                issues.put(id, new SoftReference<>(issue));
            }
            return issue;
        }

        private void removeIssue (String id) {
            synchronized (CACHE_LOCK) {
                issues.remove(id);
            }
        }
    }

}

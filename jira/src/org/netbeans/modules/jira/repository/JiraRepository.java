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

package org.netbeans.modules.jira.repository;

import org.netbeans.modules.team.spi.RepositoryUser;
import java.util.Map;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.client.spi.ContentFilter;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider.JiraClient;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.NamedFilter;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.ProjectFilter;
import org.netbeans.modules.jira.client.spi.User;
import org.netbeans.modules.jira.commands.JiraExecutor;
import org.netbeans.modules.jira.commands.NamedFiltersCommand;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.query.QueryController;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.MylynUtils;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.UnsubmittedTasksContainer;
import org.netbeans.modules.mylyn.util.commands.SimpleQueryCommand;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class JiraRepository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png"; // NOI18N

    private TaskRepository taskRepository;
    private JiraRepositoryController controller;
    private Set<JiraQuery> queries = null;
    private Set<JiraQuery> remoteFilters = null;
    private Cache cache;
    private Image icon;

    private RequestProcessor refreshProcessor;
    private JiraExecutor executor;
    private JiraConfiguration configuration;

    private final Object CONFIGURATION_LOCK = new Object();
    private final Object QUERIES_LOCK = new Object();

    private RepositoryInfo info;
    
    private PropertyChangeSupport support;
    private UnsubmittedTasksContainer unsubmittedTasksContainer;
    private PropertyChangeListener unsubmittedTasksListener;
    private final Object CACHE_LOCK = new Object();
    private boolean queryCleanedup;
    
    public JiraRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        support = new PropertyChangeSupport(this);
    }

    public JiraRepository(RepositoryInfo info) {
        this();
        this.info = info;
        String name = info.getDisplayName();
        String url = info.getUrl();

        taskRepository = setupTaskRepository(name, null, url, "", new char[0], "", new char[0]);
    }
    
    public RepositoryInfo getInfo() {
        return info;
    }
    
    public String getID() {
        return info.getID();
    }

    public JiraQuery createQuery() {
        if(getConfiguration() == null) {
            // invalid connection data?
            return null;
        }
        return new JiraQuery(this);
    }

    public NbJiraIssue createIssue() {
        return createIssue(null);
    }
    
    protected final NbJiraIssue createIssue (String productName) {
        JiraConfiguration config = getConfiguration();
        if (config == null) {
            // invalid connection data?
            return null;
        }
        Project project = null;
        for (Project proj : config.getProjects()) {
            if (productName == null || productName.equals(proj.getName())
                    || productName.equals(proj.getKey())) {
                project = proj;
                break;
            }
        }
        if (project == null) {
            return null;
        }
        final String projectKey = project.getKey();
        NbTask task;
        try {
            task = MylynSupport.getInstance().createTask(taskRepository, new TaskMapping() {
                @Override
                public String getProduct () {
                    return projectKey;
                }
            });
            return getIssueForTask(task);
        } catch (OperationCanceledException ex) {
            // creation of new task may be immediately canceled
            // happens when more repositories are available and
            // the RepoComboSupport immediately switches to another repo
            Jira.LOG.log(Level.FINE, null, ex);
            return null;
        } catch (CoreException ex) {
            Jira.LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        String id = info != null ? info.getID() : name + System.currentTimeMillis();
        info = createInfo(id, url, name, user, httpUser, password, httpPassword);
        setTaskRepository(name, url, user, password, httpUser, httpPassword);
    }

    protected RepositoryInfo createInfo(String id, String url, String name, String user, String httpUser, char[] password, char[] httpPassword) {
        return new RepositoryInfo(id, JiraConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
    }
        
    public String getDisplayName() {
        return info.getDisplayName();
    }

    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(JiraRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    public Image getIcon() {
        return icon;
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public List<NbJiraIssue> getIssues(String[] keys) {
        final List<NbJiraIssue> ret = new LinkedList<NbJiraIssue>();
        // can't use GetMultiTaskDataCommand as it isn't implemented in the JIRA connector
        // see also issue ##212090 
        for (String key : keys) {
            NbJiraIssue issue = getIssue(key);
            if(issue != null) {
                ret.add(issue);
            }
        }
        return ret;
    }
    
    public NbJiraIssue getIssue(String key) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        NbJiraIssue issue = findUnsubmitted(key);
        if (issue == null) {
            try {
                NbTask task = MylynSupport.getInstance().getTask(getUrl(), key);
                if (task != null) {
                    issue = getIssueForTask(task);
                }
            } catch (CoreException ex) {
                Jira.LOG.log(Level.WARNING, null, ex);
            }
        }
        if (issue == null) {
            issue = getIssueForTask(JiraUtils.getRepositoryTask(this, key, true));
        }
        return issue;
    }
    
    public RepositoryController getController() {
        if(controller == null) {
            controller = new JiraRepositoryController(this);
        }
        return controller;
    }

    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    public void remove() {
        synchronized(QUERIES_LOCK) {
            Set<JiraQuery> qs = getQueriesIntern();
            JiraQuery[] toRemove = qs.toArray(new JiraQuery[qs.size()]);
            for (JiraQuery q : toRemove) {
                removeQuery(q);
            }
        }
        resetRepository(true);
    }

    public void removeQuery(JiraQuery query) {
        Jira.getInstance().getStorageManager().removeQuery(this, query.getDisplayName(), query.getStoredQueryName());
        synchronized(QUERIES_LOCK) {
            getQueriesIntern().remove(query);
        }
        fireQueryListChanged();
    }

    public void saveQuery(JiraQuery query) {
        assert info != null;
        Jira.getInstance().getStorageManager().putQueryData(this, query.getDisplayName(), query.getLastRefresh(), query.getFilterDefinition());
        synchronized (QUERIES_LOCK) {
            getQueriesIntern().add(query);
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
        Jira.LOG.log(Level.FINER, "firing unsubmitted issues changed for repository {0}", new Object[] { getDisplayName() }); //NOI18N
        support.firePropertyChange(RepositoryProvider.EVENT_UNSUBMITTED_ISSUES_CHANGED, null, null);
    }
    
    private Set<JiraQuery> getQueriesIntern() {
        synchronized (QUERIES_LOCK) {
            if(queries == null) {
                if(!queryCleanedup) {
                    // clean up. we are deleting adhoc queries when closing the query dialog
                    // an eventual crash could have left some stored in MylynSupport
                    queryCleanedup = true;
                    try {
                        Set<IRepositoryQuery> iqs = MylynSupport.getInstance().getRepositoryQueries(taskRepository);
                        for (IRepositoryQuery q : iqs) {
                            if(q.getSummary().startsWith(JiraQuery.JIRA_ADHOC_QUERY_PREFIX)) {
                                MylynSupport.getInstance().deleteQuery(q);
                            }
                        }
                    } catch (CoreException ex) { 
                        Jira.LOG.log(Level.INFO, null, ex);
                    }
                }

                JiraStorageManager manager = Jira.getInstance().getStorageManager();
                queries = manager.getQueries(this);
            }
            return queries;
        }
    }

    public Collection<JiraQuery> getQueries() {
        List<JiraQuery> ret = new ArrayList<JiraQuery>();
        synchronized (QUERIES_LOCK) {            
            Set<JiraQuery> l = getQueriesIntern();
            ret.addAll(l);
            if(remoteFilters == null) {
                Jira.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        getRemoteFilters();
                    }
                });
            } else {
                ret.addAll(remoteFilters);
            }
        }
        return ret;
    }

    protected void getRemoteFilters() {
        List<JiraQuery> ret = new ArrayList<JiraQuery>();
        NamedFiltersCommand cmd = new NamedFiltersCommand(this);
        getExecutor().execute(cmd);
        if(!cmd.hasFailed()) {
            NamedFilter[] filters = cmd.getNamedFilters();
            if(filters != null) {
                for (NamedFilter nf : filters) {
                    JiraQuery q = new JiraQuery(nf.getName(), this, nf);
                    ret.add(q);
                    q.getController().fireChanged();
                }
            }
        }
        synchronized (QUERIES_LOCK) {
            remoteFilters = new HashSet<JiraQuery>();
            remoteFilters.addAll(ret);
        }
        if (!ret.isEmpty()) {
            fireQueryListChanged();
        }
    }

    public Collection<NbJiraIssue> simpleSearch(String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<NbJiraIssue> issues = new ArrayList<NbJiraIssue>();
        if(keywords.length == 1) {
            // only one search criteria -> might be we are looking for the bug with id=keywords[0]
            keywords[0] = repairKeyIfNeeded(keywords[0]);
            if (keywords[0] != null) {
                NbJiraIssue issue = getIssueForTask(JiraUtils.getRepositoryTask(this, keywords[0], false));
                if (issue != null) {
                    issues.add(issue);
                }
            }
        }

        // XXX escape special characters
        // + - && || ! ( ) { } [ ] ^ " ~ * ? \

        final JiraConnectorProvider connectorProvider = JiraConnectorSupport.getInstance().getConnector();
        FilterDefinition fd = connectorProvider.createFilterDefinition();
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(criteria, " \t");  // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            sb.append(token);
            sb.append(' ');                                         // NOI18N
            sb.append(token);
            sb.append('*');                                         // NOI18N
            sb.append(' ');                                         // NOI18N
        }

        final ContentFilter cf = connectorProvider.createContentFilter(sb.toString(), true, false, false, false);
        fd.setContentFilter(cf);
        fd.setProjectFilter(getProjectFilter());
        
        try {
            // XXX shouldn't be only a perfect match 
            IRepositoryQuery iquery = MylynSupport.getInstance().createNewQuery(taskRepository, "Jira simple task search"); //NOI18N
            JiraConnectorSupport.getInstance().getConnector().setQuery(getTaskRepository(), iquery, fd);
            SimpleQueryCommand cmd = MylynSupport.getInstance().getCommandFactory().createSimpleQueryCommand(taskRepository, iquery);
            getExecutor().execute(cmd);
            for (NbTask task : cmd.getTasks()) {
                NbJiraIssue issue = getIssueForTask(task);
                if (issue != null) {
                    issues.add(issue);
                }
            }
        } catch (CoreException ex) {
            // should not happen
            Jira.LOG.log(Level.WARNING, null, ex);
        }
        return issues;
    }

    public Cache getIssueCache () {
        synchronized (CACHE_LOCK) {
            if(cache == null) {
                cache = new Cache();
            }
            return cache;
        }
    }
    
    private void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        AuthenticationCredentials c = taskRepository != null ? taskRepository.getCredentials(AuthenticationType.REPOSITORY) : null;
        String oldUser = c != null ? c.getUserName() : "";

        taskRepository = setupTaskRepository(name, oldUrl.equals(url) ? null : oldUrl,
                url, user, password, httpUser, httpPassword);
        resetRepository(oldUrl.equals(url) && oldUser.equals(user));
    }

    public void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {                       
        MylynUtils.setCredentials(taskRepository, user, password, httpUser, httpPassword);
    }
    
    /**
     * If oldUrl is not null, gets the repository for the oldUrl and rewrites it
     * to the new url.
     */
    private static TaskRepository setupTaskRepository (String name, String oldUrl, String url, String user,
            char[] password, String httpUser, char[] httpPassword) {
        TaskRepository repository;
        if (oldUrl == null) {
            repository = MylynSupport.getInstance().getTaskRepository(Jira.getInstance().getRepositoryConnector(), url);
        } else {
            repository = MylynSupport.getInstance().getTaskRepository(Jira.getInstance().getRepositoryConnector(), oldUrl);
            try {
                MylynSupport.getInstance().setRepositoryUrl(repository, url);
            } catch (CoreException ex) {
                Jira.LOG.log(Level.WARNING, null, ex);
            }
        }
        setupProperties(repository, name, user, password, httpUser, httpPassword); 
        return repository;
    }

    static TaskRepository createTemporaryTaskRepository (String name, String url, String user,
            char[] password, String httpUser, char[] httpPassword) {
        TaskRepository taskRepository = new TaskRepository(
                Jira.getInstance().getRepositoryConnector().getConnectorKind(), url);
        setupProperties(taskRepository, name, user, password, httpUser, httpPassword);
        return taskRepository;
    }
    
    private static void setupProperties (TaskRepository repository, String displayName,
            String user, char[] password, String httpUser, char[] httpPassword) {
        repository.setRepositoryLabel(displayName);
        MylynUtils.setCredentials(repository, user, password, httpUser, httpPassword);
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public char[] getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getPassword().toCharArray() : new char[0]; 
    }

    public String getHttpUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public char[] getHttpPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getPassword().toCharArray() : new char[0]; 
    }

    void resetRepository(boolean keepConfiguration) {
        // XXX synchronization
        if(!keepConfiguration) {
            configuration = null;
        }
    }

    /**
     * Returns the jira configuration or null if not available
     *
     * @return
     */
    public JiraConfiguration getConfiguration() {
        synchronized (CONFIGURATION_LOCK) {
            if (configuration == null) {
                configuration = getConfigurationIntern(false);
            }
            return configuration;
        }
    }

    public void refreshConfiguration() {
        JiraConfiguration c = getConfigurationIntern(true);
        synchronized (CONFIGURATION_LOCK) {
            configuration = c;
        }
    }

    protected JiraConfiguration createConfiguration(JiraClient client) throws CoreException {
        return new JiraConfiguration(client, JiraRepository.this);
    }

    private JiraConfiguration getConfigurationIntern(final boolean forceRefresh) {
        // XXX need logging incl. consumed time

        class ConfigurationCommand extends BugtrackingCommand {
            JiraConfiguration configuration;
            @Override
            public void execute() throws CoreException {
                final JiraClient client = Jira.getInstance().getClient(getTaskRepository());

                boolean needRefresh = !client.hasDetails();
                Jira.LOG.log(Level.FINE, "configuration refresh {0} : needRefresh = {1} forceRefresh={2}", new Object[]{getUrl(), needRefresh, forceRefresh});
                if(forceRefresh || needRefresh) {
                    Jira.getInstance().getRepositoryConnector().updateRepositoryConfiguration(getTaskRepository(), new NullProgressMonitor());
                }
                configuration = createConfiguration(client);
            }
        }
        ConfigurationCommand cmd = new ConfigurationCommand();

        getExecutor().execute(cmd, true, false, false);
        if(!cmd.hasFailed()) {
            return cmd.configuration;
        }
        return null;
    }

    public void refreshAllQueries() {
        Collection<JiraQuery> qs = getQueries();
        for (JiraQuery q : qs) {
            Jira.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
            QueryController qc = ((JiraQuery) q).getController();
            qc.onRefresh();
        }
    }

    public void ensureCredentials() {
        setCredentials(info.getUsername(), info.getPassword(), info.getHttpUsername(), info.getHttpPassword());
    }
    
    public boolean authenticate(String errroMsg) {
        return Jira.getInstance().getBugtrackingFactory().editRepository(this, errroMsg);
    }

    public Collection<RepositoryUser> getUsers() {
        Collection<User> users = getConfiguration().getUsers();
        List<RepositoryUser> members = new ArrayList<RepositoryUser>();
        for (User user : users) {
            members.add(new RepositoryUser(user.getName(), user.getFullName()));
        }
        return members;
    }

    public JiraExecutor getExecutor() {
        if(executor == null) {
            executor = new JiraExecutor(this);
        }
        return executor;
    }

    /**
     * Returns null if key is not a valid Jira issue key
     * @param key
     * @return
     */
    protected String repairKeyIfNeeded (String key) {
        String retval = null;
        try {
            Long.parseLong(key);
            // problem
            // mylyn will interpret this key as an ID
        } catch (NumberFormatException ex) {
            // this is good, no InsufficientRightsException will be thrown in mylyn
            retval = key;
        }
        return retval;
    }

    /**
     * Returns <code>null</code> for a general repository.
     * Override this to provide a valid project filter for a repository which is limited to a subset of all projects (e.g. kenai).
     * @return a project filter - <code>null</code> for this implementation.
     */
    protected ProjectFilter getProjectFilter () {
        return null;
    }
    
    public NbJiraIssue getIssueForTask (NbTask task) {
        NbJiraIssue issue = null;
        if (task != null) {
            synchronized (CACHE_LOCK) {
                String taskKey = NbJiraIssue.getKey(task);
                Cache issueCache = getIssueCache();
                issue = issueCache.getIssue(taskKey);
                if (issue == null) {
                    issue = issueCache.setIssue(taskKey, new NbJiraIssue(task, this));
                }
            }
        }
        return issue;
    }

    private NbJiraIssue findUnsubmitted (String key) {
        try {
            for (NbTask task : getUnsubmittedTasksContainer().getTasks()) {
                if (key.equals("-" + task.getTaskId())) {
                    return getIssueForTask(task);
                }
            }
        } catch (CoreException ex) {
            Jira.LOG.log(Level.INFO, null, ex);
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
    
    /**
     * Do not call this method other than from BugzillaConfig.
     * To be overridden by KenaiRepository
     */
    public JiraQuery createPersistentQuery (String queryName, FilterDefinition filterDefinition) {
        return new JiraQuery(queryName, this, filterDefinition);
    }

    public void taskDeleted (String taskId) {
        getIssueCache().removeIssue(taskId);
    }

    public Collection<NbJiraIssue> getUnsubmittedIssues () {
        try {
            UnsubmittedTasksContainer cont = getUnsubmittedTasksContainer();
            List<NbTask> unsubmittedTasks = cont.getTasks();
            List<NbJiraIssue> unsubmittedIssues = new ArrayList<>(unsubmittedTasks.size());
            for (NbTask task : unsubmittedTasks) {
                NbJiraIssue issue = getIssueForTask(task);
                if (issue != null) {
                    unsubmittedIssues.add(issue);
                }
            }
            return unsubmittedIssues;
        } catch (CoreException ex) {
            Jira.LOG.log(Level.INFO, null, ex);
            return Collections.<NbJiraIssue>emptyList();
        }
    }
    
    public class Cache  {
        private final Map<String, Reference<NbJiraIssue>> issues = new HashMap<>();
        
        Cache() { }

        public NbJiraIssue getIssue (String key) {
            synchronized (CACHE_LOCK) {
                Reference<NbJiraIssue> issueRef = issues.get(key);
                return issueRef == null ? null : issueRef.get();
            }
        }

        public NbJiraIssue setIssue (String key, NbJiraIssue issue) {
            synchronized (CACHE_LOCK) {
                issues.put(key, new SoftReference<>(issue));
            }
            return issue;
        }

        private void removeIssue (String key) {
            synchronized (CACHE_LOCK) {
                issues.remove(key);
            }
        }
    }
    
}

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
 * single choiceget of license, a recipient has the option to distribute
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

package org.netbeans.modules.bugzilla.repository;

import java.awt.EventQueue;
import org.netbeans.modules.bugzilla.*;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugtracking.kenai.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugzilla.commands.BugzillaExecutor;
import org.netbeans.modules.mylyn.util.GetMultiTaskDataCommand;
import org.netbeans.modules.mylyn.util.PerformQueryCommand;
import org.netbeans.modules.bugzilla.issue.BugzillaTaskListProvider;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.netbeans.modules.bugzilla.query.QueryParameter;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.mylyn.util.MylynUtils;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class BugzillaRepository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png"; // NOI18N

    private RepositoryInfo info;
    private TaskRepository taskRepository;
    private BugzillaRepositoryController controller;
    private Set<BugzillaQuery> queries = null;
    private IssueCache<BugzillaIssue, TaskData> cache;
    private BugzillaExecutor executor;
    private Image icon;
    private BugzillaConfiguration bc;
    private RequestProcessor refreshProcessor;

    private final Set<String> issuesToRefresh = new HashSet<String>(5);
    private final Set<BugzillaQuery> queriesToRefresh = new HashSet<BugzillaQuery>(3);
    private Task refreshIssuesTask;
    private Task refreshQueryTask;

    private PropertyChangeSupport support;
    
    private Lookup lookup;

    public BugzillaRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        support = new PropertyChangeSupport(this);
    }

    public BugzillaRepository(RepositoryInfo info) {
        this();
        this.info = info;
        String name = info.getDisplayName();
        String user = info.getUsername();
        if(user == null) {
            user = ""; // NOI18N
        }
        char[] password = info.getPassword();
        if(password == null) {
            password = new char[0]; 
        }
        String httpUser = info.getHttpUsername();
        if(httpUser == null) {
            httpUser = ""; // NOI18N
        }
        char[] httpPassword = info.getHttpPassword();
        if(httpPassword == null) {
            httpPassword = new char[0]; 
        }
        String url = info.getUrl();
        boolean shortLoginEnabled = Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword, shortLoginEnabled);
        
        BugzillaTaskListProvider.getInstance().notifyRepositoryCreated(this);
    }

    public RepositoryInfo getInfo() {
        return info;
    }

    public String getID() {
        return info.getId();
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public BugzillaQuery createQuery() {
        BugzillaConfiguration conf = getConfiguration();
        if(conf == null || !conf.isValid()) {
            // invalid connection data?
            return null;
        }
        BugzillaQuery q = new BugzillaQuery(this);        
        return q;
    }

    public BugzillaIssue createIssue() {
        BugzillaConfiguration conf = getConfiguration();
        if(conf == null || !conf.isValid()) {
            // invalid connection data?
            return null;
        }
        TaskAttributeMapper attributeMapper =
                Bugzilla.getInstance()
                    .getRepositoryConnector()
                    .getTaskDataHandler()
                    .getAttributeMapper(taskRepository);
        TaskData data =
                new TaskData(
                    attributeMapper,
                    taskRepository.getConnectorKind(),
                    taskRepository.getRepositoryUrl(),
                    ""); // NOI18N
        return new BugzillaIssue(data, this);
    }

    public void remove() {
        Collection<BugzillaQuery> qs = getQueries();
        BugzillaQuery[] toRemove = qs.toArray(new BugzillaQuery[qs.size()]);
        for (BugzillaQuery q : toRemove) {
            removeQuery(q);
        }
        resetRepository(true);
        BugzillaTaskListProvider.getInstance().notifyRepositoryRemoved(this);
    }

    public Lookup getLookup() {
        if(lookup == null) {
            lookup = Lookups.fixed(getLookupObjects());
        }
        return lookup;
    }

    protected Object[] getLookupObjects() {
        return new Object[] { getIssueCache() };
    }

    synchronized void resetRepository(boolean keepConfiguration) {
        if(!keepConfiguration) {
            bc = null;
        }
        if(getTaskRepository() != null) {
            Bugzilla.getInstance()
                    .getRepositoryConnector()
                    .getClientManager()
                    .repositoryRemoved(getTaskRepository());
        }
    }

    public String getDisplayName() {
        return info.getDisplayName();
    }

    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(BugzillaRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    public Image getIcon() {
        return icon;
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

    public BugzillaIssue[] getIssues(final String... ids) {
        final List<BugzillaIssue> ret = new LinkedList<BugzillaIssue>();
        TaskDataCollector collector = new TaskDataCollector() {
            @Override
            public void accept(TaskData taskData) {
                String id = BugzillaIssue.getID(taskData);
                try {
                    BugzillaIssue issue = (BugzillaIssue) getIssueCache().setIssueData(id, taskData);
                    if(issue != null) {
                        ret.add(issue);
                    }
                } catch (IOException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                }
            }
        };
        GetMultiTaskDataCommand dataCmd = 
                new GetMultiTaskDataCommand(
                    Bugzilla.getInstance().getRepositoryConnector(), 
                    getTaskRepository(), 
                    collector,
                    new HashSet<String>(Arrays.asList(ids)));
        getExecutor().execute(dataCmd, true);
        return ret.toArray(new BugzillaIssue[ret.size()]);
    }
    
    public BugzillaIssue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        TaskData taskData = BugzillaUtil.getTaskData(BugzillaRepository.this, id);
        if(taskData == null) {
            return null;
        }
        try {
            BugzillaIssue issue = (BugzillaIssue) getIssueCache().setIssueData(id, taskData);
            ensureConfigurationUptodate(issue);
            return issue;
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    // XXX create repo wih product if kenai project and use in queries
    public Collection<BugzillaIssue> simpleSearch(final String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<BugzillaIssue> issues = new ArrayList<BugzillaIssue>();
        TaskDataCollector collector = new TaskDataCollector() {
            @Override
            public void accept(TaskData taskData) {
                BugzillaIssue issue = new BugzillaIssue(taskData, BugzillaRepository.this);
                issues.add(issue); // we don't cache this issues
                                   // - the retured taskdata are partial
                                   // - and we need an as fast return as possible at this place

            }
        };

        if(keywords.length == 1 && isInteger(keywords[0])) {
            // only one search criteria -> might be we are looking for the bug with id=keywords[0]
            TaskData taskData = BugzillaUtil.getTaskData(this, keywords[0], false);
            if(taskData != null) {
                BugzillaIssue issue = new BugzillaIssue(taskData, BugzillaRepository.this);
                issues.add(issue); // we don't cache this issues
                                   // - the retured taskdata are partial
                                   // - and we need an as fast return as possible at this place
            }
        }

        StringBuilder url = new StringBuilder();
        url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST + "&short_desc_type=allwordssubstr&short_desc="); // NOI18N
        for (int i = 0; i < keywords.length; i++) {
            String val = keywords[i].trim();
            if(val.equals("")) {
                continue;
            }                                        // NOI18N
            try {
                val = URLEncoder.encode(val, getTaskRepository().getCharacterEncoding());
            } catch (UnsupportedEncodingException ueex) {
                Bugzilla.LOG.log(Level.INFO, null, ueex);
                try {
                    val = URLEncoder.encode(val, "UTF-8"); // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    // should not happen
                }
            }
            url.append(val);
            if(i < keywords.length - 1) {
                url.append("+");                                                // NOI18N
            }
        }
        QueryParameter[] additionalParams = getSimpleSearchParameters();
        for (QueryParameter qp : additionalParams) {
            url.append(qp.get(true));
        }
        
        IRepositoryQuery iquery = new RepositoryQuery(taskRepository.getConnectorKind(), "bugzilla simple search query");            // NOI18N
        iquery.setUrl(url.toString());
        PerformQueryCommand queryCmd = 
            new PerformQueryCommand(
                Bugzilla.getInstance().getRepositoryConnector(),
                getTaskRepository(), 
                collector,
                iquery);
        getExecutor().execute(queryCmd);
        if(queryCmd.hasFailed()) {
            return Collections.emptyList();
        }
        return issues;
    }

    public RepositoryController getController() {
        if(controller == null) {
            controller = new BugzillaRepositoryController(this);
        }
        return controller;
    }

    public Collection<BugzillaQuery> getQueries() {
        return getQueriesIntern();
    }

    public IssueCache<BugzillaIssue, TaskData> getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public void removeQuery(BugzillaQuery query) {
        BugzillaConfig.getInstance().removeQuery(this, query);
        getIssueCache().removeQuery(query.getStoredQueryName());
        getQueriesIntern().remove(query);
        stopRefreshing(query);
        fireQueryListChanged();
    }

    public void saveQuery(BugzillaQuery query) {
        assert info != null;
        BugzillaConfig.getInstance().putQuery(this, query); 
        getQueriesIntern().add(query);
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
    
    private Set<BugzillaQuery> getQueriesIntern() {
        if(queries == null) {
            queries = new HashSet<BugzillaQuery>(10);
            String[] qs = BugzillaConfig.getInstance().getQueries(getID());
            for (String queryName : qs) {
                BugzillaQuery q = BugzillaConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Bugzilla.LOG.log(Level.WARNING, "Couldn''t find query with stored name {0}", queryName); // NOI18N
                }
            }
        }
        return queries;
    }

    public synchronized void setInfoValues(String user, char[] password, String httpUser, char[] httpPassword) {
        setTaskRepository(info.getDisplayName(), info.getUrl(), user, password, httpUser, httpPassword, Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)));
        info = new RepositoryInfo(
                        info.getId(), info.getConnectorId(), 
                        info.getUrl(), info.getDisplayName(), info.getTooltip(), 
                        user, httpUser, 
                        password, httpPassword);
    }
    
    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean localUserEnabled) {
        setTaskRepository(name, url, user, password, httpUser, httpPassword, localUserEnabled);
        String id = info != null ? info.getId() : name + System.currentTimeMillis();
        info = new RepositoryInfo(id, BugzillaConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
    }
    
    public void ensureCredentials() {
        setCredentials(info.getUsername(), info.getPassword(), info.getHttpUsername(), info.getHttpPassword(), true);
    }
    
    public void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {
        setCredentials(user, password, httpUser, httpPassword, false);
    }
    
    private synchronized void setCredentials(String user, char[] password, String httpUser, char[] httpPassword, boolean keepConfiguration) {
        MylynUtils.setCredentials(taskRepository, user, password, httpUser, httpPassword);
        resetRepository(keepConfiguration);
    }

    protected void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean shortLoginEnabled) {

        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        AuthenticationCredentials c = taskRepository != null ? taskRepository.getCredentials(AuthenticationType.REPOSITORY) : null;
        String oldUser = c != null ? c.getUserName() : "";
        String oldPassword = c != null ? c.getPassword() : "";

        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword, shortLoginEnabled);
        resetRepository(oldUrl.equals(url) && oldUser.equals(user) && oldPassword.equals(new String(password))); // XXX reset the configuration only if the host changed
                                                                                                     //     on psswd and user change reset only taskrepository
    }

    static TaskRepository createTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean shortLoginEnabled) {
        TaskRepository repository = MylynUtils.createTaskRepository(
                Bugzilla.getInstance().getRepositoryConnector().getConnectorKind(),
                name,
                url,
                user, password,
                httpUser, httpPassword);
        repository.setProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN, shortLoginEnabled ? "true" : "false"); //NOI18N
        return repository;
    }

    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public BugzillaExecutor getExecutor() {
        if(executor == null) {
            executor = new BugzillaExecutor(this);
        }
        return executor;
    }

    public boolean authenticate(String errroMsg) {
        return BugtrackingUtil.editRepository(BugzillaUtil.getRepository(this), errroMsg);
    }

    /**
     *
     * @return true if the repository accepts usernames in a short form (without domain specification).
     */
    public boolean isShortUsernamesEnabled() {
        return taskRepository != null && "true".equals(taskRepository.getProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
    }

    public Collection<RepositoryUser> getUsers() {
        return Collections.emptyList();
    }

    public OwnerInfo getOwnerInfo(Node[] nodes) {
        if(nodes == null || nodes.length == 0) {
            return null;
        }
        if(BugzillaUtil.isNbRepository(this)) {
            if(nodes != null && nodes.length > 0) {
                OwnerInfo ownerInfo = KenaiUtil.getOwnerInfo(nodes[0]);
                if(ownerInfo != null /*&& ownerInfo.getOwner().equals(product)*/ ) {
                    return ownerInfo;
                }
            }
        }
        return null;
    }

    /**
     * Returns the bugzilla configuration or null if not available
     * 
     * @return
     */
    public synchronized BugzillaConfiguration getConfiguration() {
        if(bc == null) {
            bc = createConfiguration(false);
        } else if(!bc.isValid()) {
            // there was already an attempt to get the configuration
            // yet it happend to be invalid, so try one more time as it 
            // might have been just a networking glitch  
            bc = createConfiguration(false);
        }
        return bc;
    }

    public synchronized void refreshConfiguration() {
        BugzillaConfiguration conf = createConfiguration(true);
        if(conf.isValid()) {
            bc = conf;
        } else {
            // Hard to say at this point why the attempt to refresh the 
            // configuration failed - could be just a temporary networking issue.
            // This is called only from ensureConfigurationUptodate(), so even if
            // the metadata might not be uptodate anymore, they still may be 
            // sufficient for what the user plans to do. So let's cross the 
            // fingers and keep bc the way it is.
        }
    }

    protected BugzillaConfiguration createConfiguration(boolean forceRefresh) {
        BugzillaConfiguration conf = new BugzillaConfiguration();
        conf.initialize(this, forceRefresh);
        return conf;
    }

    private void setupIssueRefreshTask() {
        if(refreshIssuesTask == null) {
            refreshIssuesTask = getRefreshProcessor().create(new Runnable() {
                @Override
                public void run() {
                    Set<String> ids;
                    synchronized(issuesToRefresh) {
                        ids = new HashSet<String>(issuesToRefresh);
                    }
                    if(ids.isEmpty()) {
                        Bugzilla.LOG.log(Level.FINE, "no issues to refresh {0}", new Object[] {getDisplayName()}); // NOI18N
                        return;
                    }
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh issue {0} - {1}", new Object[] {getDisplayName(), ids}); // NOI18N
                    GetMultiTaskDataCommand cmd = 
                        new GetMultiTaskDataCommand(
                            Bugzilla.getInstance().getRepositoryConnector(),
                            getTaskRepository(), 
                            new IssuesCollector(), 
                            ids);
                    getExecutor().execute(cmd, false);
                    scheduleIssueRefresh();
                }
            });
            scheduleIssueRefresh();
        }
    }

    private void setupQueryRefreshTask() {
        if(refreshQueryTask == null) {
            refreshQueryTask = getRefreshProcessor().create(new Runnable() {
                @Override
                public void run() {
                    try {
                        Set<BugzillaQuery> queries;
                        synchronized(refreshQueryTask) {
                            queries = new HashSet<BugzillaQuery>(queriesToRefresh);
                        }
                        if(queries.isEmpty()) {
                            Bugzilla.LOG.log(Level.FINE, "no queries to refresh {0}", new Object[] {getDisplayName()}); // NOI18N
                            return;
                        }
                        for (BugzillaQuery q : queries) {
                            Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
                            QueryController qc = q.getController();
                            qc.autoRefresh();
                        }
                    } finally {
                        scheduleQueryRefresh();
                    }
                }
            });
            scheduleQueryRefresh();
        }
    }

    private void scheduleIssueRefresh() {
        int delay = BugzillaConfig.getInstance().getIssueRefreshInterval();
        Bugzilla.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)", new Object[] {getDisplayName(), delay}); // NOI18N
        if(delay < 5 && System.getProperty("netbeans.t9y.bugzilla.force.refresh.delay") == null) {
            Bugzilla.LOG.log(Level.WARNING, " wrong issue refresh delay {0}. Falling back to default {0}", new Object[] {delay, BugzillaConfig.DEFAULT_ISSUE_REFRESH}); // NOI18N
            delay = BugzillaConfig.DEFAULT_ISSUE_REFRESH;
        }
        refreshIssuesTask.schedule(delay * 60 * 1000); // given in minutes
    }

    private void scheduleQueryRefresh() {
        String schedule = System.getProperty("netbeans.t9y.bugzilla.force.refresh.schedule", "");
        if(!schedule.isEmpty()) {
            int delay = Integer.parseInt(schedule);
            refreshQueryTask.schedule(delay); 
            return;
        }
        
        int delay = BugzillaConfig.getInstance().getQueryRefreshInterval();
        Bugzilla.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)", new Object[] {getDisplayName(), delay}); // NOI18N
        if(delay < 5) {
            Bugzilla.LOG.log(Level.WARNING, " wrong query refresh delay {0}. Falling back to default {0}", new Object[] {delay, BugzillaConfig.DEFAULT_QUERY_REFRESH}); // NOI18N
            delay = BugzillaConfig.DEFAULT_QUERY_REFRESH;
        }
        refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
    }

    public void scheduleForRefresh(String id) {
        Bugzilla.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}", new Object[] {id, getDisplayName()}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.add(id);
        }
        setupIssueRefreshTask();
    }

    public void stopRefreshing(String id) {
        Bugzilla.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}", new Object[] {id, getDisplayName()}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.remove(id);
        }
    }

    public void scheduleForRefresh(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}", new Object[] {query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.add(query);
        }
        setupQueryRefreshTask();
    }

    public void stopRefreshing(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}", new Object[] {query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.remove(query);
        }
    }

    public void refreshAllQueries() {
        refreshAllQueries(true);
    }

    protected void refreshAllQueries(final boolean onlyOpened) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Collection<BugzillaQuery> qs = getQueries();
                for (BugzillaQuery q : qs) {
                    if(!onlyOpened || !Bugzilla.getInstance().getBugtrackingFactory().isOpen(BugzillaUtil.getRepository(BugzillaRepository.this), q)) {
                        continue;
                    }
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
                    QueryController qc = ((BugzillaQuery) q).getController();
                    qc.onRefresh();
                }
            }
        });
    }

    public void ensureConfigurationUptodate(BugzillaIssue issue) {
        BugzillaConfiguration conf = getConfiguration();

        String product = issue.getFieldValue(IssueField.PRODUCT);
        String resolution = issue.getFieldValue(IssueField.RESOLUTION);
        String severity = issue.getFieldValue(IssueField.SEVERITY);
        String milestone = issue.getFieldValue(IssueField.MILESTONE);
        String version = issue.getFieldValue(IssueField.VERSION);
        String priority = issue.getFieldValue(IssueField.PRIORITY);
        String platform = issue.getFieldValue(IssueField.PLATFORM);
        String status = issue.getFieldValue(IssueField.STATUS);
        String os = issue.getFieldValue(IssueField.OS);
        String component = issue.getFieldValue(IssueField.COMPONENT);

        if(!component.isEmpty() && !conf.getComponents(product).contains(component) ||
           !os.isEmpty() && !conf.getOSs().contains(os) ||
           !status.isEmpty() && !conf.getStatusValues().contains(status) ||
           !platform.isEmpty() && !conf.getPlatforms().contains(platform) ||
           !priority.isEmpty() && !conf.getPriorities().contains(priority) ||
           !product.isEmpty() && !conf.getProducts().contains(product) ||
           !resolution.isEmpty() && !conf.getResolutions().contains(resolution) ||
           !severity.isEmpty() && !conf.getSeverities().contains(severity) ||
           !milestone.isEmpty() && !conf.getTargetMilestones(product).contains(milestone) ||
           !version.isEmpty() && !conf.getVersions(product).contains(version))
        {
            refreshConfiguration();
        }
    }

    private class IssuesCollector extends TaskDataCollector {
        @Override
        public void accept(TaskData taskData) {
            String id = BugzillaIssue.getID(taskData);
            Bugzilla.LOG.log(Level.FINE, "refreshed issue {0} - {1}", new Object[] {getDisplayName(), id}); // NOI18N
            try {
                getIssueCache().setIssueData(id, taskData);
            } catch (IOException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
        }
    };

    private RequestProcessor getRefreshProcessor() {
        if(refreshProcessor == null) {
            refreshProcessor = new RequestProcessor("Bugzilla refresh - " + getDisplayName()); // NOI18N
        }
        return refreshProcessor;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + getDisplayName() + ')';        //NOI18N
    }

    protected QueryParameter[] getSimpleSearchParameters () {
        return new QueryParameter[] {};
    }

    private class Cache extends IssueCache<BugzillaIssue, TaskData> {
        Cache() {
            super(
                BugzillaRepository.this.getUrl(), 
                new IssueAccessorImpl(), 
                Bugzilla.getInstance().getIssueProvider(), 
                BugzillaUtil.getRepository(BugzillaRepository.this));
        }
    }

    private class IssueAccessorImpl implements IssueCache.IssueAccessor<BugzillaIssue, TaskData> {
        @Override
        public BugzillaIssue createIssue(TaskData taskData) {
            BugzillaIssue issue = new BugzillaIssue(taskData, BugzillaRepository.this);
            org.netbeans.modules.bugzilla.issue.BugzillaTaskListProvider.getInstance().notifyIssueCreated(issue);
            return issue;
        }
        @Override
        public void setIssueData(BugzillaIssue issue, TaskData taskData) {
            assert issue != null && taskData != null;
            ((BugzillaIssue)issue).setTaskData(taskData);
        }
        @Override
        public String getRecentChanges(BugzillaIssue issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getRecentChanges();
        }
        @Override
        public long getLastModified(BugzillaIssue issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getLastModify();
        }
        @Override
        public long getCreated(BugzillaIssue issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getCreated();
        }
        @Override
        public String getID(TaskData issueData) {
            assert issueData != null;
            return BugzillaIssue.getID(issueData);
        }
        @Override
        public Map<String, String> getAttributes(BugzillaIssue issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getAttributes();
        }
    }

}

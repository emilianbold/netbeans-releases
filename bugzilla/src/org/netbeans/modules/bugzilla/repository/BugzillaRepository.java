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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.kenai.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugzilla.commands.BugzillaExecutor;
import org.netbeans.modules.bugzilla.commands.GetMultiTaskDataCommand;
import org.netbeans.modules.bugzilla.commands.PerformQueryCommand;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.netbeans.modules.bugzilla.query.QueryParameter;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.util.MylynUtils;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class BugzillaRepository extends RepositoryProvider {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png"; // NOI18N

    private String name;
    private TaskRepository taskRepository;
    private BugzillaRepositoryController controller;
    private Set<QueryProvider> queries = null;
    private IssueCache<TaskData> cache;
    private BugzillaExecutor executor;
    private Image icon;
    private BugzillaConfiguration bc;
    private RequestProcessor refreshProcessor;

    private final Set<String> issuesToRefresh = new HashSet<String>(5);
    private final Set<BugzillaQuery> queriesToRefresh = new HashSet<BugzillaQuery>(3);
    private Task refreshIssuesTask;
    private Task refreshQueryTask;
    private String id;

    public static final String ATTRIBUTE_URL = "bugzilla.repository.attribute.url"; //NOI18N
    public static final String ATTRIBUTE_DISPLAY_NAME = "bugzilla.repository.attribute.displayName"; //NOI18N
    private Lookup lookup;
    private final PropertyChangeSupport support;

    public BugzillaRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        this.support = new PropertyChangeSupport(this);
    }

    public BugzillaRepository(RepositoryInfo info) {
        this(info.getId(), 
            info.getDisplayName(), 
            info.getUrl(), 
            info.getUsername(), 
            info.getPassword(), 
            info.getHttpUsername(), 
            info.getHttpPassword(), 
            Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)));
    }
    
    public BugzillaRepository(String id, String repoName, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        this(id, repoName, url, user, password, httpUser, httpPassword, false);
    }
    
    public BugzillaRepository(String id, String repoName, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean shortLoginEnabled) {
        this();
        this.id = id;
        name = repoName;
        if(user == null) {
            user = ""; // NOI18N
        }
        if(password == null) {
            password = new char[0]; 
        }
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword, shortLoginEnabled);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Notify listeners on this repository that a query was either removed or saved
     * XXX make use of new/old value
     */
    public void fireQueryListChanged() {
        support.firePropertyChange(EVENT_QUERY_LIST_CHANGED, null, null);
    }

    /**
     * Notify listeners on this repository that some of repository's attributes have changed.
     * @param oldValue map of old attributes
     * @param newValue map of new attributes
     */
    protected void fireAttributesChanged (java.util.Map<String, Object> oldAttributes, java.util.Map<String, Object> newAttributes) {
        LinkedList<String> equalAttributes = new LinkedList<String>();
        // find unchanged values
        for (Map.Entry<String, Object> e : newAttributes.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            Object oldValue = oldAttributes.get(key);
            if ((value == null && oldValue == null) || (value != null && value.equals(oldValue))) {
                equalAttributes.add(key);
            }
        }
        // remove unchanged values
        for (String equalAttribute : equalAttributes) {
            if (oldAttributes != null) {
                oldAttributes.remove(equalAttribute);
            }
            newAttributes.remove(equalAttribute);
        }
        if (!newAttributes.isEmpty()) {
            support.firePropertyChange(new java.beans.PropertyChangeEvent(this, EVENT_ATTRIBUTES_CHANGED, oldAttributes, newAttributes));
        }        
    }
    
    @Override
    public RepositoryInfo getInfo() {
        RepositoryInfo info = new RepositoryInfo(id, BugzillaConnector.ID, getUrl(), getDisplayName(), getTooltip(), getUsername(), getHttpUsername(), getPassword(), getHttpPassword());
        info.putValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN, taskRepository.getProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)); 
        return info;
    }

    
    public String getID() {
        if(id == null) {
            id = name + System.currentTimeMillis();
        }
        return id;
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    @Override
    public QueryProvider createQuery() {
        BugzillaConfiguration conf = getConfiguration();
        if(conf == null || !conf.isValid()) {
            // invalid connection data?
            return null;
        }
        BugzillaQuery q = new BugzillaQuery(this);        
        return q;
    }

    @Override
    public IssueProvider createIssue() {
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

    @Override
    public void remove() {
        QueryProvider[] qs = getQueries();
        for (QueryProvider q : qs) {
            removeQuery((BugzillaQuery) q);
        }
        resetRepository(true);
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

    void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return name;
    }

    public String getTooltip() {
        return name + " : " + taskRepository.getCredentials(AuthenticationType.REPOSITORY).getUserName() + "@" + taskRepository.getUrl(); // NOI18N
    }

    @Override
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

    public IssueProvider getIssue(final String id) {
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

    @Override
    // XXX create repo wih product if kenai project and use in queries
    public IssueProvider[] simpleSearch(final String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<IssueProvider> issues = new ArrayList<IssueProvider>();
        TaskDataCollector collector = new TaskDataCollector() {
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

        StringBuffer url = new StringBuffer();
        url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST + "&short_desc_type=allwordssubstr&short_desc="); // NOI18N
        for (int i = 0; i < keywords.length; i++) {
            String val = keywords[i].trim();
            if(val.equals("")) continue;                                        // NOI18N
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
            url.append(qp.get());
        }
        PerformQueryCommand queryCmd = new PerformQueryCommand(this, url.toString(), collector);
        getExecutor().execute(queryCmd);
        if(queryCmd.hasFailed()) {
            return new IssueProvider[0];
        }
        return issues.toArray(new BugzillaIssue[issues.size()]);
    }

    @Override
    public RepositoryController getController() {
        if(controller == null) {
            controller = new BugzillaRepositoryController(this);
        }
        return controller;
    }

    @Override
    public QueryProvider[] getQueries() {
        Set<QueryProvider> l = getQueriesIntern();
        return l.toArray(new QueryProvider[l.size()]);
    }

    public IssueCache<TaskData> getIssueCache() {
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
    }

    public void saveQuery(BugzillaQuery query) {
        assert id != null;
        BugzillaConfig.getInstance().putQuery(this, query); 
        getQueriesIntern().add(query);
    }

    private Set<QueryProvider> getQueriesIntern() {
        if(queries == null) {
            queries = new HashSet<QueryProvider>(10);
            String[] qs = BugzillaConfig.getInstance().getQueries(id);
            for (String queryName : qs) {
                BugzillaQuery q = BugzillaConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Bugzilla.LOG.warning("Couldn't find query with stored name " + queryName); // NOI18N
                }
            }
        }
        return queries;
    }

    public void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {
        MylynUtils.setCredentials(taskRepository, user, password, httpUser, httpPassword);
        resetRepository(false);
    }

    protected void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean shortLoginEnabled) {
        HashMap<String, Object> oldAttributes = createAttributesMap();

        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        AuthenticationCredentials c = taskRepository != null ? taskRepository.getCredentials(AuthenticationType.REPOSITORY) : null;
        String oldUser = c != null ? c.getUserName() : "";
        String oldPassword = c != null ? c.getPassword() : "";

        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword, shortLoginEnabled);
        resetRepository(oldUrl.equals(url) && oldUser.equals(user) && oldPassword.equals(password)); // XXX reset the configuration only if the host changed
                                                                                                     //     on psswd and user change reset only taskrepository
        HashMap<String, Object> newAttributes = createAttributesMap();
        fireAttributesChanged(oldAttributes, newAttributes);
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
        return BugtrackingUtil.editRepository(this, errroMsg);
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
                public void run() {
                    Set<String> ids;
                    synchronized(issuesToRefresh) {
                        ids = new HashSet<String>(issuesToRefresh);
                    }
                    if(ids.size() == 0) {
                        Bugzilla.LOG.log(Level.FINE, "no issues to refresh {0}", new Object[] {name}); // NOI18N
                        return;
                    }
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh issue {0} - {1}", new Object[] {name, ids}); // NOI18N
                    GetMultiTaskDataCommand cmd = new GetMultiTaskDataCommand(BugzillaRepository.this, ids, new IssuesCollector());
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
                public void run() {
                    try {
                        Set<BugzillaQuery> queries;
                        synchronized(refreshQueryTask) {
                            queries = new HashSet<BugzillaQuery>(queriesToRefresh);
                        }
                        if(queries.size() == 0) {
                            Bugzilla.LOG.log(Level.FINE, "no queries to refresh {0}", new Object[] {name}); // NOI18N
                            return;
                        }
                        for (BugzillaQuery q : queries) {
                            Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), name}); // NOI18N
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
        Bugzilla.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)", new Object[] {name, delay}); // NOI18N
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
        Bugzilla.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)", new Object[] {name, delay}); // NOI18N
        if(delay < 5) {
            Bugzilla.LOG.log(Level.WARNING, " wrong query refresh delay {0}. Falling back to default {0}", new Object[] {delay, BugzillaConfig.DEFAULT_QUERY_REFRESH}); // NOI18N
            delay = BugzillaConfig.DEFAULT_QUERY_REFRESH;
        }
        refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
    }

    public void scheduleForRefresh(String id) {
        Bugzilla.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}", new Object[] {id, name}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.add(id);
        }
        setupIssueRefreshTask();
    }

    public void stopRefreshing(String id) {
        Bugzilla.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}", new Object[] {id, name}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.remove(id);
        }
    }

    public void scheduleForRefresh(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}", new Object[] {query.getDisplayName(), name}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.add(query);
        }
        setupQueryRefreshTask();
    }

    public void stopRefreshing(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}", new Object[] {query.getDisplayName(), name}); // NOI18N
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
                QueryProvider[] qs = getQueries();
                for (QueryProvider q : qs) {
                    if(!onlyOpened || !BugtrackingUtil.isOpened(q)) {
                        continue;
                    }
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), name}); // NOI18N
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
        public void accept(TaskData taskData) {
            String id = BugzillaIssue.getID(taskData);
            Bugzilla.LOG.log(Level.FINE, "refreshed issue {0} - {1}", new Object[] {name, id}); // NOI18N
            try {
                getIssueCache().setIssueData(id, taskData);
            } catch (IOException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
                return;
            }
        }
    };

    private RequestProcessor getRefreshProcessor() {
        if(refreshProcessor == null) {
            refreshProcessor = new RequestProcessor("Bugzilla refresh - " + name); // NOI18N
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

    private HashMap<String, Object> createAttributesMap () {
        HashMap<String, Object> attributes = new HashMap<String, Object>(2);
        // XXX add more if requested
        attributes.put(ATTRIBUTE_DISPLAY_NAME, getDisplayName());
        attributes.put(ATTRIBUTE_URL, getUrl());
        return attributes;
    }

    private class Cache extends IssueCache<TaskData> {
        Cache() {
            super(BugzillaRepository.this.getUrl(), new IssueAccessorImpl());
        }
    }

    private class IssueAccessorImpl implements IssueCache.IssueAccessor<TaskData> {
        public IssueProvider createIssue(TaskData taskData) {
            BugzillaIssue issue = new BugzillaIssue(taskData, BugzillaRepository.this);
            org.netbeans.modules.bugzilla.issue.BugzillaIssueProvider.getInstance().notifyIssueCreated(issue);
            return issue;
        }
        public void setIssueData(IssueProvider issue, TaskData taskData) {
            assert issue != null && taskData != null;
            ((BugzillaIssue)issue).setTaskData(taskData);
        }
        public String getRecentChanges(IssueProvider issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getRecentChanges();
        }
        public long getLastModified(IssueProvider issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getLastModify();
        }
        public long getCreated(IssueProvider issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getCreated();
        }
        public String getID(TaskData issueData) {
            assert issueData != null;
            return BugzillaIssue.getID(issueData);
        }
        public Map<String, String> getAttributes(IssueProvider issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getAttributes();
        }
    }

}

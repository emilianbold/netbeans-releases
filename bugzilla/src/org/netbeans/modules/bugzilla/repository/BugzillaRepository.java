/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.bugzilla.repository;

import org.netbeans.modules.bugzilla.*;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.IssueCache;
import org.netbeans.modules.bugzilla.commands.BugzillaExecutor;
import org.netbeans.modules.bugzilla.commands.PerformQueryCommand;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaRepository extends Repository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png";

    private String name;
    private TaskRepository taskRepository;
    private RepositoryController controller;
    private Set<Query> queries = null;
    private IssueCache cache;
    private BugzillaExecutor executor;
    private Image icon;
    private BugzillaConfiguration bc;
    
    public BugzillaRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
    }

    public BugzillaRepository(String repoName, String url, String user, String password) {
        this();
        name = repoName;
        taskRepository = createTaskRepository(name, url, user, password);
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    @Override
    public Query createQuery() {
        BugzillaQuery q = new BugzillaQuery(this);        
        return q;
    }

    @Override
    public Issue createIssue() {
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
                    "");
        return new BugzillaIssue(data, this);
    }

    @Override
    public void remove() {
        BugzillaConfig.getInstance().removeRepository(this.getDisplayName());
        Query[] qs = getQueries();
        for (Query q : qs) {
            removeQuery((BugzillaQuery) q);
        }
        resetRepository();
    }

    synchronized void resetRepository() {
        bc = null;
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
    
    @Override
    public void fireQueryListChanged() {
        super.fireQueryListChanged();
    }

    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " : " + taskRepository.getCredentials(AuthenticationType.REPOSITORY).getUserName() + "@" + taskRepository.getUrl();
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c.getUserName();
    }

    public String getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c.getPassword();
    }

    public Issue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";

        TaskData taskData = BugzillaUtil.getTaskData(BugzillaRepository.this, id);
        if(taskData == null) {
            return null;
        }
        try {
            return getIssueCache().setIssueData(id, taskData);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    // XXX create repo wih product if kenai project and use in queries
    public Issue[] simpleSearch(final String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<Issue> issues = new ArrayList<Issue>();
        TaskDataCollector collector = new TaskDataCollector() {
            public void accept(TaskData taskData) {
                try {
                    Issue issue = getIssueCache().setIssueData(BugzillaIssue.getID(taskData), taskData);
                    issues.add(issue); // XXX we don't cache this issues - why?
                } catch (IOException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX handle errors
                }
            }
        };

        StringBuffer url = new StringBuffer();
        if(keywords.length == 1 && isNumber(keywords[0])) {
            // only one search criteria -> might be we are looking for the bug with id=values[0]
            url.append(IBugzillaConstants.URL_GET_SHOW_BUG);
            url.append("="); // XXX ???                                         // NOI18N
            url.append(keywords[0]);

            PerformQueryCommand queryCmd = new PerformQueryCommand(this, url.toString(), collector);
            getExecutor().execute(queryCmd);
            if(queryCmd.hasFailed()) {
                return new Issue[0];
            }
        }

        url = new StringBuffer();
        url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST + "&short_desc_type=allwordssubstr&short_desc="); // NOI18N
        for (int i = 0; i < keywords.length; i++) {
            String val = keywords[i].trim();
            if(val.equals("")) continue;                                        // NOI18N
            url.append(val);
            if(i < keywords.length - 1) {
                url.append("+");                                                // NOI18N
            }
        }
        PerformQueryCommand queryCmd = new PerformQueryCommand(this, url.toString(), collector);
        getExecutor().execute(queryCmd);
        if(queryCmd.hasFailed()) {
            return new Issue[0];
        }
        return issues.toArray(new BugzillaIssue[issues.size()]);
    }

    @Override
    public BugtrackingController getController() {
        if(controller == null) {
            controller = new RepositoryController(this);
        }
        return controller;
    }

    @Override
    public Query[] getQueries() {
        return getQueriesIntern().toArray(new Query[queries.size()]);
    }

    public IssueCache getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public void removeQuery(BugzillaQuery query) {
        BugzillaConfig.getInstance().removeQuery(this, query);
        getQueriesIntern().remove(query);
    }

    public void saveQuery(BugzillaQuery query) {
        assert name != null;
        BugzillaConfig.getInstance().putQuery(this, query); // XXX display name ????
        getQueriesIntern().add(query);
    }

    private Set<Query> getQueriesIntern() {
        if(queries == null) {
            queries = new HashSet<Query>(10);
            String[] qs = BugzillaConfig.getInstance().getQueries(name);
            for (String queryName : qs) {
                BugzillaQuery q = BugzillaConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Bugzilla.LOG.warning("Couldn't find query with stored name " + queryName);
                }
            }
        }
        return queries;
    }

    void setTaskRepository(String name, String url, String user, String password) {
        taskRepository = createTaskRepository(name, url, user, password);
    }

    static TaskRepository createTaskRepository(String name, String url, String user, String password) {
        TaskRepository repository = new TaskRepository(name, url);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(user, password);

        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        return repository;
    }

    @Override
    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    private boolean isNumber(String str) {
        for (int i = 0; i < str.length() -1; i++) {
            if(!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    public BugzillaExecutor getExecutor() {
        if(executor == null) {
            executor = new BugzillaExecutor(this);
        }
        return executor;
    }

    private class Cache extends IssueCache {
        Cache() {
            super(BugzillaRepository.this.getUrl());
        }
        protected Issue createIssue(TaskData taskData) {
            return new BugzillaIssue(taskData, BugzillaRepository.this);
        }
        protected void setTaskData(Issue issue, TaskData taskData) {
            ((BugzillaIssue)issue).setTaskData(taskData); 
        }
    }

    /**
     * Returns the bugzilla configuration or null if not available
     * 
     * @return
     */
    public synchronized BugzillaConfiguration getConfiguration() {
        if(bc == null) {
            bc = BugzillaConfiguration.create(this);
        }
        return bc;
    }

}

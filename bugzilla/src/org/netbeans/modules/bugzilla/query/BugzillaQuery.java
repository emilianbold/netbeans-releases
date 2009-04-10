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

package org.netbeans.modules.bugzilla.query;

import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.io.IOException;
import org.netbeans.modules.bugzilla.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.IssueCache;
import org.netbeans.modules.bugzilla.commands.GetMultiTaskDataCommand;
import org.netbeans.modules.bugzilla.commands.PerformQueryCommand;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaQuery extends Query {

    private String name;
    private final BugzillaRepository repository;
    protected QueryController controller;
    private final Set<String> issues = new HashSet<String>();
    private Set<String> obsoleteIssues = new HashSet<String>();

    protected String urlParameters;
    private boolean firstRun = true;

    public BugzillaQuery(BugzillaRepository repository) {
        this(null, repository, null, false, -1);
    }

    protected BugzillaQuery(String name, BugzillaRepository repository, String urlParameters, boolean saved) {
        super();
        this.name = name;
        this.repository = repository;
        this.urlParameters = urlParameters;
        this.saved = saved;
        // let the subclass create the controller
    }

    public BugzillaQuery(String name, BugzillaRepository repository, String urlParameters, long lastRefresh) {
        this(name, repository, urlParameters, true, lastRefresh);
    }

    private BugzillaQuery(String name, BugzillaRepository repository, String urlParameters, boolean saved, long lastRefresh) {
        this.repository = repository;
        this.saved = saved;
        this.name = name;
        this.urlParameters = urlParameters;
        this.setLastRefresh(lastRefresh);
        controller = createControler(repository, this, urlParameters);
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " - " + repository.getDisplayName(); // NOI18N
    }

    @Override
    public synchronized QueryController getController() {
        if (controller == null) {
            controller = createControler(repository, this, urlParameters);
        }
        return controller;
    }

    @Override
    public BugzillaRepository getRepository() {
        return repository;
    }

    protected QueryController createControler(BugzillaRepository r, BugzillaQuery q, String parameters) {
        return new QueryController(r, q, parameters);
    }

    @Override
    public ColumnDescriptor[] getColumnDescriptors() {
        return BugzillaIssue.getColumnDescriptors();
    }

    @Override
    public boolean refresh() { // XXX sync???

        assert urlParameters != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean ret[] = new boolean[1];
        executeQuery(new Runnable() {
            public void run() {
                Bugzilla.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new String[] {name, urlParameters}); // NOI18N
                try {
                    
                    // keeps all issues we will retrieve from the server
                    // - those matching the query criteria
                    // - and the obsolete ones
                    Set<String> queryIssues = new HashSet<String>();
                    
                    if(isSaved()) {
                        if(!wasRun()) {
                            if(issues.size() != 0) {
                                Bugzilla.LOG.warning("query " + getDisplayName() + " supposed to be run for the first time yet already contains issues."); // NOI18N
                                assert false;
                            }
                            // read the stored state if query wasn't run yet ...
                            // we have to query them ...
                            queryIssues.addAll(repository.getIssueCache().readQuery(BugzillaQuery.this.getDisplayName()));
                            // ... and they might be rendered obsolete if not returned by the query
                            obsoleteIssues.addAll(queryIssues);
                        } else {
                            // all previously queried issues are candidates to become obsolete
                            obsoleteIssues.addAll(issues);
                            queryIssues.addAll(obsoleteIssues);
                        }
                        // ... and store all you got
                        repository.getIssueCache().storeQuery(BugzillaQuery.this.getDisplayName(), queryIssues.toArray(new String[queryIssues.size()]));
                    }
                    issues.clear();
                    firstRun = false;

                    // run query to know what matches the criteria
                    StringBuffer url = new StringBuffer();
                    url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST);
                    url.append(urlParameters); // XXX encode url?
                    // IssuesIdCollector will populate the issues set
                    PerformQueryCommand queryCmd = new PerformQueryCommand(repository, url.toString(), new IssuesIdCollector());
                    repository.getExecutor().execute(queryCmd);
                    ret[0] = queryCmd.hasFailed();
                    if(ret[0]) {
                        return;
                    }

                    // only issues not returned by the query are obsolete
                    obsoleteIssues.removeAll(issues);

                    // now get the task data for
                    // - all issue returned by the query
                    // - and issues which were returned by some previous run
                    queryIssues.addAll(issues);

                    GetMultiTaskDataCommand dataCmd = new GetMultiTaskDataCommand(repository, queryIssues, new IssuesCollector());
                    repository.getExecutor().execute(dataCmd);
                    ret[0] = dataCmd.hasFailed();
                } finally {
                    logQueryEvent(issues.size());
                    Bugzilla.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new String[] {name, urlParameters}); // NOI18N
                }
            }
        });
        return ret[0];
    }

    protected void logQueryEvent(int count) {
        BugtrackingUtil.logQueryEvent(
            BugzillaConnector.getConnectorName(),
            name,
            count,
            false,
            getController().isAutoRefresh());
    }

    public void refresh(String urlParameters) {
        assert urlParameters != null;
        this.urlParameters = urlParameters;
        refresh();
    }

    void remove() {
        repository.removeQuery(this);        
        fireQueryRemoved();
    }

    @Override
    public int getIssueStatus(Issue issue) {
        String id = issue.getID();
        return getIssueStatus(id);
    }

    @Override
    public boolean contains(Issue issue) {
        return issues.contains(issue.getID());
    }

    public int getIssueStatus(String id) {
        return repository.getIssueCache().getStatus(id);
    }

    int getSize() {
        return issues.size();
    }

    public String getUrlParameters() {
        return getController().getUrlParameters();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setSaved(boolean saved) {
        super.setSaved(saved);
    }

    @Override
    public void setFilter(Filter filter) {
        getController().selectFilter(filter);
        super.setFilter(filter);
    }

    @Override
    public void fireQuerySaved() {
        super.fireQuerySaved();
        repository.fireQueryListChanged();
    }

    @Override
    public void fireQueryRemoved() {
        super.fireQueryRemoved();
        repository.fireQueryListChanged();
    }

    @Override
    public Issue[] getIssues(int includeStatus) {
        if (issues == null) {
            return new Issue[0];
        }
        List<String> ids = new ArrayList<String>();
        synchronized (issues) {
            ids.addAll(issues);
        }

        IssueCache cache = repository.getIssueCache();
        List<Issue> ret = new ArrayList<Issue>();
        for (String id : ids) {
            int status = getIssueStatus(id);
            if((status & includeStatus) != 0) {
                ret.add(cache.getIssue(id));
            }
        }
        return ret.toArray(new Issue[ret.size()]);
    }

    boolean wasRun() {
        return !firstRun;
    }

    private class IssuesIdCollector extends TaskDataCollector {
        public IssuesIdCollector() {}
        public void accept(TaskData taskData) {
            String id = BugzillaIssue.getID(taskData);
            issues.add(id);
        }
    };
    private class IssuesCollector extends TaskDataCollector {
        public IssuesCollector() {}
        public void accept(TaskData taskData) {
            String id = BugzillaIssue.getID(taskData);
            BugzillaIssue issue;
            try {
                IssueCache cache = repository.getIssueCache();
                issue = (BugzillaIssue) cache.setIssueData(id, taskData);
            } catch (IOException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
                return;
            }
            fireNotifyData(issue); // XXX - !!! triggers getIssues()
        }
    };
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.tasklist;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.IssueProvider.LazyIssue;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Provider of issues/tasks for the tasklist. Keeps issues to display in the tasklist 
 * and acts as a bridge between the issuetracking and the tasklist.
 *
 * @author Ondra Vrabec
 */
public final class TaskListProvider extends PushTaskScanner {
    
    private static TaskListProvider instance; //this class is a singleton

    // tasklist oriented fields
    private Callback callback;
    private TaskScanningScope scope;
    private WeakReference<TaskScanningScope> lastScope = new WeakReference<TaskScanningScope>(null);
    private static final Object SCOPE_LOCK = new Object();
    /**
     * true flag signalizes the tasklist should be opened after a refresh
     */
    private boolean openTaskList;
    private static final String TASK_GROUP_NAME = "nb-tasklist-issue";  //NOI18N
    private static final String TC_TASKLIST_ID = "TaskListTopComponent"; //NOI18N

    // issue-task mapping oriented fields
    /**
     * all cached tasks, a subset is selected and displayed in the tasklist when it's scope changes
     */
    private final WeakHashMap<IssueProvider.LazyIssue, Task> cachedTasks;
    private final WeakHashMap<IssueProvider, Set<IssueProvider.LazyIssue> > cachedIssues;
    private final WeakSet<IssueProvider.LazyIssue> validatedIssues;
    /**
     * Set of providers whose issues need to be validated. Validate means test if they belong to the current tasklist scope.
     */
    private final HashSet<IssueProvider> providersToValidate;
    
    private boolean providersInitialized;
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.tasklist"); //NOI18N

    private final RequestProcessor.Task refreshTask = new RequestProcessor("IssuesToTaskListProvider", 1, true).create(new RefreshTask()); //NOI18N

    private TaskListProvider () {
        super(NbBundle.getMessage(TaskListProvider.class, "LBL_TaskListProvider_DisplayName"), NbBundle.getMessage(TaskListProvider.class, "LBL_TaskListProvider_Description"), null); //NOI18N
        cachedTasks = new WeakHashMap<IssueProvider.LazyIssue, Task>(10);
        cachedIssues = new WeakHashMap<IssueProvider, Set<IssueProvider.LazyIssue>>(5);
        providersToValidate = new HashSet<IssueProvider>(5);
        validatedIssues = new WeakSet<LazyIssue>(10);
    }

    public static synchronized TaskListProvider getInstance() {
        if (instance == null) {
            instance = new TaskListProvider();
            instance.refreshTasks(true);
        }
        return instance;
    }

    @Override
    public void setScope(TaskScanningScope scope, Callback callback) {
        synchronized (SCOPE_LOCK) {
            if (!providersInitialized) {
                // tasklist initialization
                for (IssueProvider provider : Lookup.getDefault().lookupAll(IssueProvider.class)) {
                    LOG.finer("TaskListProvider.setScope: waking up " + provider.getClass().getName()); //NOI18N
                }
                providersInitialized = true;
            }
            this.callback = callback;
            this.scope = scope;
            this.lastScope.clear();
        }
        refreshTasks(true);
    }

    /**
     * Schedules given issues for addition to the tasklist.
     * @param provider issuetracking provider
     * @param openTaskList if set to true, the tasklist top component will be opened
     * @param issuesToAdd issues to be added
     */
    public void add (IssueProvider provider, boolean openTaskList, IssueProvider.LazyIssue... issuesToAdd) {
        if (provider == null || issuesToAdd == null || issuesToAdd.length == 0) {
            LOG.fine("TaskListProvider.add: provider: " + provider + ", issuesToAdd: " + issuesToAdd); //NOI18N
            return;
        }
        LOG.fine("TaskListProvider.add: adding " + issuesToAdd.length + " for " + provider + ", request to open: " + openTaskList); //NOI18N
        synchronized (cachedIssues) {
            this.openTaskList = this.openTaskList || openTaskList;
            Set<IssueProvider.LazyIssue> issues = cachedIssues.get(provider);
            if (issues == null) {
                issues = new HashSet<IssueProvider.LazyIssue>(5);
            }
            for (IssueProvider.LazyIssue issue : issuesToAdd) {
                issues.add(issue);
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.fine("TaskListProvider.add: issues for " + provider + ": " + issues); //NOI18N
            }
            cachedIssues.put(provider, issues);
            // also schedule a validation of the provider's issues
            providersToValidate.add(provider);
        }
        refreshTasks(false);
    }

    /**
     * Schedules given issues for removal
     * @param provider issuetracking provider
     * @param issuesToRemove issues to be removed from the tasklist
     */
    public void remove (IssueProvider provider, IssueProvider.LazyIssue... issuesToRemove) {
        if (provider == null || issuesToRemove == null || issuesToRemove.length == 0) {
            LOG.fine("TaskListProvider.remove: provider: " + provider + ", issuesToRemove: " + issuesToRemove); //NOI18N
            return;
        }
        LOG.fine("TaskListProvider.remove: removing " + issuesToRemove.length + " for " + provider); //NOI18N
        synchronized (cachedIssues) {
            Set<IssueProvider.LazyIssue> issues = cachedIssues.get(provider);
            if (issues != null) {
                for (IssueProvider.LazyIssue issue : issuesToRemove) {
                    issues.remove(issue);
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("TaskListProvider.remove: issues for " + provider + ": " + (issues == null ? "empty" : issues)); //NOI18N
            }
        }
        removeCachedTasks(issuesToRemove);
        refreshTasks(false);
    }

    /**
     * Removes all issues for the given provider
     * @param provider
     */
    public void removeAll (IssueProvider provider) {
        if (provider == null) {
            LOG.fine("TaskListProvider.removeAll: provider is null");   //NOI18N
            return;
        }
        LOG.fine("TaskListProvider.removeAll: provider is null");       //NOI18N
        Set<LazyIssue> issues;
        synchronized (cachedIssues) {
            // clear the issues
            issues = cachedIssues.remove(provider);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("TaskListProvider.removeAll: issues for " + provider + ": " + (cachedIssues.get(provider) == null ? "empty" : cachedIssues.get(provider))); //NOI18N
            }
        }
        if (issues != null) {
            removeCachedTasks(issues.toArray(new LazyIssue[issues.size()]));
        }
        refreshTasks(false);
    }

    private void removeCachedTasks (LazyIssue... issues) {
        synchronized (cachedTasks) {
            for (LazyIssue issue : issues) {
                cachedTasks.remove(issue);
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("TaskListProvider.removeCachedTasks: cached tasks: " + cachedTasks); //NOI18N
            }
        }
    }

    private Task createTask(final LazyIssue lazyIssue, final WeakReference<IssueProvider> providerRef) {
        List<Action> actions = new LinkedList<Action>();
        // open action: a default action for the first action added by tasklist and for a dbl-click on a task
        ActionListener openIssueAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        Issue issue = lazyIssue.getIssue();
                        if (issue != null) {
                            LOG.log(Level.FINER, "TaskListProvider: openning issue {0}", lazyIssue.getName()); //NOI18N
                            // openning the real issue in it's top component
                            issue.open();
                        } else {
                            LOG.log(Level.FINE, "null issue returned for {0}", lazyIssue.getName()); //NOI18N
                        }
                    }
                });
            }
        };
        // remove action: always added
        actions.add(new AbstractAction(NbBundle.getMessage(TaskListProvider.class, "TaskListProvider.Action.remove.name")) { //NOI18N
            public void actionPerformed(ActionEvent e) {
                IssueProvider provider = providerRef.get();
                if (provider != null) {
                    LOG.log(Level.FINE, "TaskListProvider: removing issue {0}", lazyIssue.getName()); //NOI18N
                    TaskListProvider.this.remove(provider, lazyIssue);
                    provider.removed(lazyIssue);
                }
            }
        });
        List<? extends Action> additionalActions = lazyIssue.getActions();
        if (additionalActions != null) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "TaskListProvider.createTask: provided actions {0}", additionalActions); //NOI18N
            }
            for (Action a : additionalActions) {
                if (a != null) {
                    actions.add(a);
                }
            }
        }
        Task task = Task.create(lazyIssue.getUrl(), TASK_GROUP_NAME, lazyIssue.getName(), openIssueAL, actions.toArray(new AbstractAction[actions.size()]));
        cachedTasks.put(lazyIssue, task);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("TaskListProvider.createTasks: cached tasks: " + cachedTasks); //NOI18N
        }
        // setting valid to true disables task re-creation in a next tasklist refresh
        lazyIssue.setValid(true);
        return task;
    }

    /**
     *
     * @param cancelRunningRefresh set to true only when the scope changes, otherwise the validation inside the refreshtask can return unexpected results
     */
    private void refreshTasks (boolean cancelRunningRefresh) {
        LOG.finer("TaskListProvider.refreshTasks: cancel=" + cancelRunningRefresh); //NOI18N
        if (cancelRunningRefresh) {
            refreshTask.cancel();
        }
        refreshTask.schedule(200);
    }

    /**
     * Refreshes issue tasks in the tasklist.
     */
    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            if (Thread.interrupted()) {
                return;
            }
            TaskScanningScope scope;
            Callback callback;
            boolean scopeChanged;
            synchronized (SCOPE_LOCK) {
                // get current scope set by the tasklist
                scope = TaskListProvider.this.scope;
                callback = TaskListProvider.this.callback;
                scopeChanged = scope != lastScope.get();
                if (scopeChanged) {
                    LOG.finer("RefreshTask.run: scope has changed, running the full scan."); //NOI18N
                }
            }
            if (callback == null || scope == null) {
                // this might happen, e.g. after startup
                return;
            }
            // issues that can be directly included and don't need to be tested for belonging under the current scope
            HashMap<IssueProvider.LazyIssue, IssueProvider> issuesToInclude = new HashMap<LazyIssue, IssueProvider>();
            // issues that can't be directly included and need to be tested for belonging under the current scope
            HashMap<IssueProvider.LazyIssue, IssueProvider> issuesToValidate = new HashMap<LazyIssue, IssueProvider>();
            synchronized(cachedIssues) {
                if (scopeChanged) {
                    // when a scope changes, all issues need to be re-validated
                    providersToValidate.addAll(cachedIssues.keySet());
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "RefreshTask.run: providers to validate: {0}", providersToValidate); //NOI18N
                }
                for (Map.Entry<IssueProvider, Set<IssueProvider.LazyIssue>> e : cachedIssues.entrySet()) {
                    if (providersToValidate.contains(e.getKey())) {
                        for (LazyIssue issue : e.getValue()) {
                            issuesToValidate.put(issue, e.getKey());
                        }
                    } else {
                        for (LazyIssue issue : e.getValue()) {
                            if (validatedIssues.contains(issue) && !issuesToValidate.containsKey(issue)) {
                                issuesToInclude.put(issue, e.getKey());
                            } else {
                                issuesToValidate.put(issue, e.getKey());
                            }
                        }
                    }
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "RefreshTask.run: issues to validate: {0}", issuesToValidate); //NOI18N
                }
            }
            List<String> repositoryUrls = getRepositoriesFor(scope);
            // if the issue's repository is not among allAssociatedRepositories, then it is probably not associated yet
            // and the issue would never be displayed. So show unassociated issues/repositories by default.
            Collection<String> allAssociatedRepositories = BugtrackingOwnerSupport.getInstance().getAllAssociatedUrls();
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "RefreshTask.run: all associated repositories: {0}", allAssociatedRepositories); //NOI18N
            }
            if (repositoryUrls == null) {
                return;
            }
            // validate issues
            for (Map.Entry<LazyIssue, IssueProvider> e : issuesToValidate.entrySet()) {
                if (isIssueFromRepository(e.getKey(), repositoryUrls, allAssociatedRepositories)) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "RefreshTask.run: issue {0} is valid under current scope", e.getKey().getName()); //NOI18N
                    }
                    issuesToInclude.put(e.getKey(), e.getValue());
                } else if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "RefreshTask.run: issue {0} is invalid under current scope", e.getKey().getName()); //NOI18N
                }
            }
            // list of tasks eventually inserted into the tasklist
            LinkedList<Task> tasks = new LinkedList<Task>();
            for (Map.Entry<LazyIssue, IssueProvider> e : issuesToInclude.entrySet()) {
                LazyIssue issue = e.getKey();
                Task t;
                synchronized (cachedTasks) {
                    if ((t = cachedTasks.get(issue)) == null    // task is not yet created
                            || !issue.isValid()) {              // the issue is planned to be refreshed
                        t = createTask(issue, new WeakReference<IssueProvider>(e.getValue()));
                    }
                }
                tasks.add(t);
            }
            if (Thread.interrupted()) {
                return;
            }
            boolean openTaskList;
            synchronized(cachedIssues) {
                openTaskList = TaskListProvider.this.openTaskList;
                TaskListProvider.this.openTaskList = false;
                TaskListProvider.this.providersToValidate.clear();
                validatedIssues.clear();
                validatedIssues.addAll(issuesToInclude.keySet());
            }
            callback.setTasks(tasks);
            lastScope = new WeakReference<TaskScanningScope>(scope);
            if (openTaskList) {
                // openning the tasklist
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        TopComponent tc = WindowManager.getDefault().findTopComponent(TC_TASKLIST_ID); //NOI18N
                        tc.open();
                        tc.requestVisible();
                    }
                });
            }
        }
    }

    private List<String> getRepositoriesFor (TaskScanningScope scope) {
        Collection<? extends Project> projects = scope.getLookup().lookupAll(Project.class);
        LinkedList<String> repositoryUrls = new LinkedList<String>();
        if (!projects.isEmpty()) {
            for (Project p : projects) {
                if (Thread.interrupted()) {
                    return null;
                }
                long startTime = 0;
                if (LOG.isLoggable(Level.FINER)) {
                    startTime = System.currentTimeMillis();
                }
                // lookup a repository registered with current projects
                Repository repository = BugtrackingOwnerSupport.getInstance().getRepository(p, false);
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "getRepositoriesFor: repository: {0} for {1} after {2}", new Object[] {repository, p, (System.currentTimeMillis() - startTime)});
                }
                if (repository != null) {
                    repositoryUrls.add(repository.getUrl());
                }
            }
        }
        // XXX find somehow repositories not registered to any project
        return repositoryUrls;
    }

    /**
     * Issues are validated based on their repository's URLs.
     * @param issue validated issue
     * @param repositoryUrls collection of valid repository URLs in the current scope.
     * @return
     */
    private boolean isIssueFromRepository (IssueProvider.LazyIssue issue, Collection<String> repositoryUrls, Collection<String> allAssociatedRepositories) {
        String repositoryUrl = issue.getRepositoryUrl();
        boolean returnValue = false;
        if (repositoryUrl != null) {
            for (String url : repositoryUrls) {
                if (repositoryUrl.equals(url)) {
                    LOG.log(Level.FINE, "isIssueFromRepository: issue {0} under an allowed repository", issue);
                    returnValue = true;
                    break;
                }
            }
            // XXX kenai repositories, which have not yet been associated??
            if (!returnValue && !allAssociatedRepositories.contains(repositoryUrl)) {
                LOG.log(Level.FINE, "isIssueFromRepository: issue {0} under a unassociated repository", issue);
                returnValue = true;
            }
        }
        return returnValue;
    }
}

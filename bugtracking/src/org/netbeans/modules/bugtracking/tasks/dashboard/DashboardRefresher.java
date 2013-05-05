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
package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.settings.DashboardSettings;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author jpeska
 */
public class DashboardRefresher {

    private static final RequestProcessor RP = new RequestProcessor(DashboardRefresher.class.getName());
    private final Task refreshDashboard;
    private static DashboardRefresher instance;
    private boolean refreshEnabled;
    private boolean dashboardBusy = false;
    private boolean refreshWaiting = false;

    private DashboardRefresher() {
        refreshDashboard = RP.create(new Runnable() {
            @Override
            public void run() {
                if (!refreshEnabled) {
                    return;
                }
                if (dashboardBusy) {
                    refreshWaiting = true;
                    return;
                }
                try {
                    refresh();
                } finally {
                    setupDashboardRefresh();
                }
            }
        });
    }

    public static DashboardRefresher getInstance() {
        if (instance == null) {
            instance = new DashboardRefresher();
        }
        return instance;
    }

    public void setupDashboardRefresh() {
        final DashboardSettings settings = DashboardSettings.getInstance();
        if (!settings.isAutoSync() || !refreshEnabled) {
            return;
        }
        refreshDashboard.cancel();
        scheduleDashboardRefresh();
    }

    private void scheduleDashboardRefresh() {
        final DashboardSettings settings = DashboardSettings.getInstance();
        int delay = settings.getAutoSyncValue();
        refreshDashboard.schedule(delay * 60 * 1000); // given in minutes
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    public void setDashboardBusy(boolean dashboardBusy) {
        this.dashboardBusy = dashboardBusy;
        if (!dashboardBusy && refreshWaiting) {
            refreshWaiting = false;
            refreshDashboard.run();
        }
    }

    private void refresh() {
        List<Repository> repositories = DashboardViewer.getInstance().getRepositories(false);
        List<Category> categories = DashboardViewer.getInstance().getCategories(false);
        List<Issue> changedTasks = new ArrayList<Issue>();
        for (Repository repository : repositories) {
            for (Query query : repository.getQueries()) {
                Collection<TaskContainer> oldTasks = getTaskContainers(query.getIssues());
                query.refresh();
                Collection<Issue> newTasks = query.getIssues();
                changedTasks.addAll(getChangedTasks(oldTasks, newTasks));
            }
        }

        for (Category category : categories) {
            Collection<TaskContainer> oldTasks = getTaskContainers(category.getTasks());
            category.refresh();
            List<Issue> newTasks = category.getTasks();
            changedTasks.addAll(getChangedTasks(oldTasks, newTasks));
        }

    }

    private List<Issue> getChangedTasks(Collection<TaskContainer> oldTasks, Collection<Issue> newTasks) {
        List<Issue> changedTask = new ArrayList<Issue>();
        for (Issue newTask : newTasks) {
            boolean isChanged = true;
            for (TaskContainer oldTask : oldTasks) {
                if (newTask.getID().equals(oldTask.id) && newTask.getRepository().getId().equals(oldTask.idRepository)) {
                    if (newTask.getStatus().equals(oldTask.status)) {
                        isChanged = false;
                    }
                    break;
                }
            }
            if (isChanged) {
                changedTask.add(newTask);
            }
        }

        return changedTask;
    }
    
    private List<TaskContainer> getTaskContainers(Collection<Issue> tasks) {
        List<TaskContainer> containers = new ArrayList<TaskContainer>(tasks.size());
        for (Issue task : tasks) {
            containers.add(new TaskContainer(task));
        }
        return containers;
    }

    private static class TaskContainer {
        private final String id;
        private final String idRepository;
        private final Issue.Status status;

        public TaskContainer(Issue issue) {
            this.id = issue.getID();
            this.idRepository = issue.getRepository().getId();
            this.status = issue.getStatus();
        }
    }
}

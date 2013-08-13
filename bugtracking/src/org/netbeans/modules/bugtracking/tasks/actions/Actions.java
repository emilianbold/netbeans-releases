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
package org.netbeans.modules.bugtracking.tasks.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.tasks.dashboard.CategoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.netbeans.modules.bugtracking.tasks.dashboard.QueryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.RepositoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.bugtracking.tasks.dashboard.Refreshable;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskContainerNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.Submitable;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jpeska
 */
public class Actions {

    public static final KeyStroke REFRESH_KEY = KeyStroke.getKeyStroke("F5"); //NOI18N
    public static final KeyStroke DELETE_KEY = KeyStroke.getKeyStroke("DELETE"); //NOI18N

    private static final RequestProcessor RP = new RequestProcessor(Actions.class.getName(), 10);

    public static List<Action> getDefaultActions(TreeListNode... nodes) {
        List<Action> actions = new ArrayList<Action>();

        Action markSeen = MarkSeenAction.createAction(nodes);
        if (markSeen != null) {
            actions.add(markSeen);
        }

        Action refresh = RefreshAction.createAction(nodes);
        if (refresh != null) {
            actions.add(refresh);
        }
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="default actions">
    public static class RefreshAction extends AbstractAction {

        private final List<Refreshable> nodes;

        private RefreshAction(List<Refreshable> nodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh"));
            putValue(ACCELERATOR_KEY, REFRESH_KEY);
            this.nodes = nodes;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (Refreshable refreshableNode : nodes) {
                refreshableNode.refreshContent();
            }
        }

        public static RefreshAction createAction(TreeListNode... nodes) {
            List<Refreshable> refreshables = new ArrayList<Refreshable>();
            for (TreeListNode n : nodes) {
                if (n instanceof Refreshable) {
                    refreshables.add((Refreshable) n);
                } else {
                    return null;
                }
            }
            return new RefreshAction(refreshables);
        }
    }

    public static class MarkSeenAction extends AbstractAction {

        private final boolean setAsSeen;
        private final List<IssueImpl> tasks;
        private boolean canceled = false;

        private MarkSeenAction(boolean setAsSeen, List<IssueImpl> tasks) {
            super(setAsSeen ? NbBundle.getMessage(Actions.class, "CTL_MarkSeen") : NbBundle.getMessage(Actions.class, "CTL_MarkUnseen"));
            this.setAsSeen = setAsSeen;
            this.tasks = tasks;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    ProgressHandle markProgress = getProgress();
                    markProgress.start(tasks.size());
                    int workunits = 0;
                    for (IssueImpl task : tasks) {
                        if (canceled) {
                            break;
                        }
                        markProgress.progress(NbBundle.getMessage(Actions.class, "LBL_MarkTaskProgress", task.getDisplayName()));
                        task.setSeen(setAsSeen);
                        workunits++;
                        markProgress.progress(workunits);
                    }
                    markProgress.finish();
                }
            });
        }

        static MarkSeenAction createAction(TreeListNode... nodes) {
            List<IssueImpl> tasks = new ArrayList<IssueImpl>();
            for (TreeListNode n : nodes) {
                if (n instanceof TaskContainerNode) {
                    tasks.addAll(((TaskContainerNode) n).getTasks(true));
                } else {
                    return null;
                }
            }
            boolean setAsSeen = false;
            for (IssueImpl issue : tasks) {
                if (!issue.getStatus().equals(IssueStatusProvider.Status.SEEN)) {
                    setAsSeen = true;
                }
            }
            return new MarkSeenAction(setAsSeen, tasks);
        }

        private ProgressHandle getProgress() {
            return ProgressHandleFactory.createHandle(NbBundle.getMessage(Actions.class, setAsSeen ? "LBL_MarkSeenAllProgress" : "LBL_MarkUnseenAllProgress"), new Cancellable() {
                @Override
                public boolean cancel() {
                    canceled = true;
                    return canceled;
                }
            });
        }
    }
    //</editor-fold>

    public static List<Action> getSubmitablePopupActions(TreeListNode... nodes) {
        List<Action> actions = new ArrayList<Action>();
        Action submitAction = SubmitAction.createAction(nodes);
        if (submitAction != null) {
            actions.add(submitAction);
        }
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="submitable actions">
    public static class SubmitAction extends AbstractAction {

        private final List<Submitable> nodes;
        private boolean canceled = false;

        private SubmitAction(List<Submitable> nodes, String name) {
            super(name);
            this.nodes = nodes;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Map<String, IssueImpl> tasksMap = new HashMap<String, IssueImpl>();
                    for (Submitable submitable : nodes) {
                        for (IssueImpl task : submitable.getTasksToSubmit()) {
                            if (!tasksMap.containsKey(getTaskKey(task))) {
                                tasksMap.put(getTaskKey(task), task);
                            }
                        }
                    }
                    ProgressHandle submitProgress = getProgress();
                    submitProgress.start(tasksMap.values().size());
                    int workunits = 0;
                    for (IssueImpl task : tasksMap.values()) {
                        if (canceled) {
                            break;
                        }
                        submitProgress.progress(NbBundle.getMessage(Actions.class, "LBL_SubmitTaskProgress", task.getDisplayName()));
                        task.submit();
                        workunits++;
                        submitProgress.progress(workunits);
                    }
                    submitProgress.finish();
                }

            });
        }

        private ProgressHandle getProgress() {
            return ProgressHandleFactory.createHandle(NbBundle.getMessage(Actions.class, "LBL_SubmitAllProgress"), new Cancellable() {
                @Override
                public boolean cancel() {
                    canceled = true;
                    return canceled;
                }
            });
        }

        private String getTaskKey(IssueImpl task) {
            return task.getRepositoryImpl().getId() + ";;" + task.getID();
        }

        public static SubmitAction createAction(TreeListNode... nodes) {
            List<Submitable> submitables = new ArrayList<Submitable>();
            for (TreeListNode n : nodes) {
                if (n instanceof Submitable && ((Submitable) n).isUnsubmitted()) {
                    submitables.add((Submitable) n);
                } else {
                    return null;
                }
            }
            String name = NbBundle.getMessage(Actions.class, "CTL_SubmitAll");
            if (nodes.length == 1 && nodes[0] instanceof TaskContainerNode) {
                TaskContainerNode n = (TaskContainerNode) nodes[0];
                if (n.getTasks(true).size() == 1 && (n instanceof TaskNode)) {
                    name = NbBundle.getMessage(Actions.class, "CTL_Submit");
                }
            }
            return new SubmitAction(submitables, name);
        }
    }
//</editor-fold>

    public static List<Action> getTaskPopupActions(TaskNode... taskNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenTaskAction(taskNodes));
        if (taskNodes.length == 1) {
            AbstractAction action = DashboardViewer.getInstance().isTaskNodeActive(taskNodes[0]) ? new DeactivateTaskAction() : new ActivateTaskAction(taskNodes[0]);
            action.setEnabled(false);
            actions.add(action);
        }
        boolean enableSetCategory = true;
        boolean showRemoveTask = true;
        for (TaskNode taskNode : taskNodes) {
            if (!taskNode.isCategorized()) {
                showRemoveTask = false;
            }
            if (taskNode.getTask().isNew()) {
                enableSetCategory = false;
            }
        }
        if (showRemoveTask) {
            actions.add(new RemoveTaskAction(taskNodes));
        }
        SetCategoryAction setCategoryAction = new SetCategoryAction(taskNodes);
        actions.add(setCategoryAction);
        if (!enableSetCategory) {
            setCategoryAction.setEnabled(false);
        }
        //actions.add(new ScheduleTaskAction(taskNodes));
        //actions.add(new NotificationTaskAction(taskNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="task actions">
    public static class RemoveTaskAction extends TaskAction {

        public RemoveTaskAction(TaskNode... taskNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_RemoveFromCat"), taskNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (TaskNode taskNode : getTaskNodes()) {
                DashboardViewer.getInstance().removeTask(taskNode);
            }
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private static class ScheduleTaskAction extends TaskAction {

        public ScheduleTaskAction(TaskNode... taskNodes) {
            super("Schedule", taskNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    private static class SetCategoryAction extends TaskAction {

        public SetCategoryAction(TaskNode... taskNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_SetCat"), taskNode); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().addTask(getTaskNodes());
        }
    }

    private static class NotificationTaskAction extends TaskAction {

        public NotificationTaskAction(TaskNode... taskNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification"), taskNode); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static class ActivateTaskAction extends TaskAction {

        public ActivateTaskAction(TaskNode taskNode) {
            super(NbBundle.getMessage(ActivateTaskAction.class, "CTL_ActivateTask"), taskNode);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().activateTask(getTaskNodes()[0]);
        }
    }

    public static class CreateTaskAction extends RepositoryAction {

        public CreateTaskAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_CreateTask"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                IssueAction.createIssue(repositoryNode.getRepository());
            }
        }
    }

    public static class DeactivateTaskAction extends AbstractAction {

        public DeactivateTaskAction() {
            super(NbBundle.getMessage(DeactivateTaskAction.class, "CTL_DeactivateTask")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().deactivateTask();
        }
    }

    public static class OpenTaskAction extends TaskAction {

        public OpenTaskAction(TaskNode... taskNodes) {
            super(NbBundle.getMessage(OpenTaskAction.class, "CTL_OpenNode"), taskNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (TaskNode taskNode : getTaskNodes()) {
                IssueAction.openIssue(taskNode.getTask().getRepositoryImpl(), taskNode.getTask().getID());
            }
        }
    }
    //</editor-fold>

    public static List<Action> getCategoryPopupActions(CategoryNode... categoryNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new RenameCategoryAction(categoryNodes));
        actions.add(new DeleteCategoryAction(categoryNodes));
        //actions.add(new NotificationCategoryAction(categoryNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="category actions">
    public static class DeleteCategoryAction extends CategoryAction {

        public DeleteCategoryAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete"), categoryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().deleteCategory(getCategoryNodes());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private static class NotificationCategoryAction extends CategoryAction {

        public NotificationCategoryAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    private static class RenameCategoryAction extends CategoryAction {

        public RenameCategoryAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Rename"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().renameCategory(getCategoryNodes()[0].getCategory());
        }

        @Override
        public boolean isEnabled() {
            boolean parent = super.isEnabled();
            boolean singleNode = getCategoryNodes().length == 1;
            return parent && singleNode;
        }
    }

    public static class CloseCategoryNodeAction extends CategoryAction {

        public CloseCategoryNodeAction(CategoryNode... categoryNodes) {
            super(org.openide.util.NbBundle.getMessage(CloseCategoryNodeAction.class, "CTL_CloseNode"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (CategoryNode categoryNode : getCategoryNodes()) {
                DashboardViewer.getInstance().setCategoryOpened(categoryNode, false);
            }
        }
    }

    public static class CreateCategoryAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().createCategory();
        }
    }

    public static class OpenCategoryNodeAction extends CategoryAction {

        public OpenCategoryNodeAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(OpenCategoryNodeAction.class, "CTL_OpenNode"), categoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (CategoryNode categoryNode : getCategoryNodes()) {
                DashboardViewer.getInstance().setCategoryOpened(categoryNode, true);
            }
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public static class ClearCategoriesAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().clearCategories();
        }
    }
    //</editor-fold>

    public static List<Action> getRepositoryPopupActions(RepositoryNode... repositoryNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new CreateTaskAction(repositoryNodes));
        actions.add(new CreateQueryAction(repositoryNodes));
        actions.add(new QuickSearchAction(repositoryNodes));

        actions.add(null);
        actions.add(new RemoveRepositoryAction(repositoryNodes));
        actions.add(new PropertiesRepositoryAction(repositoryNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="repository actions">
    public static class RemoveRepositoryAction extends RepositoryAction {

        public RemoveRepositoryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Remove"), repositoryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().removeRepository(getRepositoryNodes());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private static class PropertiesRepositoryAction extends RepositoryAction {

        public PropertiesRepositoryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Properties"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BugtrackingUtil.editRepository(getRepositoryNodes()[0].getRepository(), null);
        }

        @Override
        public boolean isEnabled() {
            boolean parent = super.isEnabled();
            boolean singleNode = getRepositoryNodes().length == 1;
            boolean allMutable = true;
            for (RepositoryNode n : getRepositoryNodes()) {
                allMutable = n.getRepository().isMutable();
                if (!allMutable) {
                    break;
                }
            }
            return parent && singleNode && allMutable;
        }
    }

    public static class CloseRepositoryNodeAction extends AbstractAction {

        private final RepositoryNode repositoryNode;

        public CloseRepositoryNodeAction(RepositoryNode repositoryNode) {
            super(org.openide.util.NbBundle.getMessage(CloseRepositoryNodeAction.class, "CTL_CloseNode")); //NOI18N
            this.repositoryNode = repositoryNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().setRepositoryOpened(repositoryNode, false);
        }
    }

    public static class CreateRepositoryAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            BugtrackingUtil.createRepository(false);
        }
    }

    public static class OpenRepositoryNodeAction extends AbstractAction {

        private final RepositoryNode repositoryNode;

        public OpenRepositoryNodeAction(RepositoryNode repositoryNode) {
            super(org.openide.util.NbBundle.getMessage(OpenCategoryNodeAction.class, "CTL_OpenNode")); //NOI18N
            this.repositoryNode = repositoryNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().setRepositoryOpened(repositoryNode, true);
        }
    }

    public static class CreateQueryAction extends RepositoryAction {

        public CreateQueryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_CreateQuery"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                org.netbeans.modules.bugtracking.ui.query.QueryAction.openQuery(null, repositoryNode.getRepository());
            }
        }
    }

    public static class QuickSearchAction extends RepositoryAction {

        public QuickSearchAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Search"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RepositoryNode repositoryNode = getRepositoryNodes()[0];
            DashboardUtils.quickSearchTask(repositoryNode.getRepository());
        }

        @Override
        public boolean isEnabled() {
            boolean parentEnabled = super.isEnabled();
            return parentEnabled && getRepositoryNodes().length == 1;
        }
    }
    //</editor-fold>

    public static List<Action> getQueryPopupActions(QueryNode... queryNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new EditQueryAction(queryNodes));  
        actions.add(new OpenQueryAction(queryNodes));
        actions.add(new DeleteQueryAction(queryNodes));
        //actions.add(new NotificationQueryAction(queryNodes));
        return actions;
    }

    //<editor-fold defaultstate="collapsed" desc="query actions">
    public static class DeleteQueryAction extends QueryAction {

        public DeleteQueryAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete"), queryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, DELETE_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    DashboardViewer.getInstance().deleteQuery(getQueryNodes());
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private static class NotificationQueryAction extends QueryAction {

        public NotificationQueryAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification"), queryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static class OpenQueryAction extends QueryAction {

        private QueryController.QueryMode mode;

        public OpenQueryAction(QueryNode... queryNodes) {
            this(QueryController.QueryMode.SHOW_ALL, queryNodes);
        }

        public OpenQueryAction(QueryController.QueryMode mode, QueryNode... queryNodes) {
            super(NbBundle.getMessage(OpenQueryAction.class, "CTL_OpenNode"), queryNodes); //NOI18N
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (QueryNode queryNode : getQueryNodes()) {
                queryNode.getQuery().open(false, mode);
            }
        }
    }
    public static class EditQueryAction extends QueryAction {

        private QueryController.QueryMode mode;

        public EditQueryAction(QueryNode... queryNodes) {
            this(QueryController.QueryMode.EDIT, queryNodes);
        }

        public EditQueryAction(QueryController.QueryMode mode, QueryNode... queryNodes) {
            super(NbBundle.getMessage(OpenQueryAction.class, "CTL_Edit"), queryNodes); //NOI18N
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (QueryNode queryNode : getQueryNodes()) {
                queryNode.getQuery().open(false, mode);
            }
        }
    }
    //</editor-fold>

    public static class UniversalDeleteAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
            Map<String, List<TreeListNode>> map = new HashMap<String, List<TreeListNode>>();
            for (TreeListNode treeListNode : selectedNodes) {
                List<TreeListNode> list = map.get(treeListNode.getClass().getName());
                if (list == null) {
                    list = new ArrayList<TreeListNode>();
                }
                list.add(treeListNode);
                map.put(treeListNode.getClass().getName(), list);
            }

            for (String key : map.keySet()) {
                List<TreeListNode> value = map.get(key);
                Action action = null;
                if (key.equals(RepositoryNode.class.getName())) {
                    action = new Actions.RemoveRepositoryAction(value.toArray(new RepositoryNode[value.size()]));
                } else if (key.equals(CategoryNode.class.getName())) {
                    action = new Actions.DeleteCategoryAction(value.toArray(new CategoryNode[value.size()]));
                } else if (key.equals(QueryNode.class.getName())) {
                    action = new Actions.DeleteQueryAction(value.toArray(new QueryNode[value.size()]));
                } else {
                    action = new Actions.RemoveTaskAction(value.toArray(new TaskNode[value.size()]));
                }
                action.actionPerformed(e);
            }
        }

        @Override
        public boolean isEnabled() {
            List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
            for (TreeListNode treeListNodes : selectedNodes) {
                if (treeListNodes instanceof RepositoryNode || treeListNodes instanceof CategoryNode || treeListNodes instanceof QueryNode || treeListNodes instanceof TaskNode) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class UniversalRefreshAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
            RefreshAction refresh = RefreshAction.createAction(selectedNodes.toArray(new TreeListNode[selectedNodes.size()]));
            if (refresh != null) {
                refresh.actionPerformed(e);
            }
        }
    }
}

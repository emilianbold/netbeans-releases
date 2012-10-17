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
package org.netbeans.modules.tasks.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Query.QueryMode;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.tasks.ui.DashboardTopComponent;
import org.netbeans.modules.tasks.ui.dashboard.CategoryNode;
import org.netbeans.modules.tasks.ui.dashboard.DashboardViewer;
import org.netbeans.modules.tasks.ui.dashboard.QueryNode;
import org.netbeans.modules.tasks.ui.dashboard.RepositoryNode;
import org.netbeans.modules.tasks.ui.dashboard.TaskNode;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jpeska
 */
public class Actions {

    public static final KeyStroke REFRESH_KEY = KeyStroke.getKeyStroke("F5"); //NOI18N
    public static final KeyStroke DELETE_KEY = KeyStroke.getKeyStroke("DELETE"); //NOI18N

    public static List<Action> getTaskPopupActions(TaskNode... taskNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenTaskAction(taskNodes));
        if (taskNodes.length == 1) {
            AbstractAction action = DashboardViewer.getInstance().isTaskNodeActive(taskNodes[0]) ? new DeactivateTaskAction() : new ActivateTaskAction(taskNodes[0]);
            action.setEnabled(false);
            actions.add(action);
        }
        boolean showRemoveTask = true;
        for (TaskNode taskNode : taskNodes) {
            if (!taskNode.isCategorized()) {
                showRemoveTask = false;
            }
        }
        if (showRemoveTask) {
            actions.add(new RemoveTaskAction(taskNodes));
        }
        actions.add(new SetCategoryAction(taskNodes));
        actions.add(new ScheduleTaskAction(taskNodes));
        actions.add(new NotificationTaskAction(taskNodes));
        actions.add(new RefreshTaskAction(taskNodes));
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

    public static class RefreshTaskAction extends TaskAction {

        public RefreshTaskAction(TaskNode... taskNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh"), taskNodes); //NOI18N
            putValue(ACCELERATOR_KEY, REFRESH_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    for (TaskNode taskNode : getTaskNodes()) {
                        taskNode.getTask().refresh();
                    }
                }
            });
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
                Util.createNewIssue(repositoryNode.getRepository());
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
            super(NbBundle.getMessage(OpenTaskAction.class, "CTL_Open"), taskNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (TaskNode taskNode : getTaskNodes()) {
                Util.openIssue(taskNode.getTask().getRepository(), taskNode.getTask().getID());
            }
        }
    }
    //</editor-fold>

    public static List<Action> getCategoryPopupActions(CategoryNode... categoryNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new DeleteCategoryAction(categoryNodes));
        actions.add(new RenameCategoryAction(categoryNodes));
        actions.add(new NotificationCategoryAction(categoryNodes));
        actions.add(new RefreshCategoryAction(categoryNodes));
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

    public static class RefreshCategoryAction extends CategoryAction {

        public RefreshCategoryAction(CategoryNode... categoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh"), categoryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, REFRESH_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (CategoryNode categoryNode : getCategoryNodes()) {
                categoryNode.refreshContent();
            }
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
        actions.add(new RemoveRepositoryAction(repositoryNodes));
        actions.add(new RefreshRepositoryAction(repositoryNodes));
        actions.add(new PropertiesRepositoryAction(repositoryNodes));

        actions.add(null);
        actions.add(new CreateTaskAction(repositoryNodes));
        actions.add(new SearchRepositoryAction(repositoryNodes));
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

    public static class RefreshRepositoryAction extends RepositoryAction {

        public RefreshRepositoryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh"), repositoryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, REFRESH_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                repositoryNode.refreshContent();
            }
        }
    }

    private static class PropertiesRepositoryAction extends RepositoryAction {

        public PropertiesRepositoryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Properties"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Repository repository = getRepositoryNodes()[0].getRepository();
            repository.edit();
        }

        @Override
        public boolean isEnabled() {
            boolean parent = super.isEnabled();
            boolean singleNode = getRepositoryNodes().length == 1;
            boolean allMutable = true;
            for(RepositoryNode n : getRepositoryNodes()) {
                allMutable = n.getRepository().isMutable();
                if(!allMutable) {
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
            Repository repository = RepositoryManager.getInstance().createRepository();
            // TODO replace this with listener in TC
            if (repository != null) {
                DashboardViewer.getInstance().addRepository(repository);
            }
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

    public static class SearchRepositoryAction extends RepositoryAction {

        public SearchRepositoryAction(RepositoryNode... repositoryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Search"), repositoryNodes); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (RepositoryNode repositoryNode : getRepositoryNodes()) {
                Util.createNewQuery(repositoryNode.getRepository());
            }
        }
    }
    //</editor-fold>

    public static List<Action> getQueryPopupActions(QueryNode... queryNodes) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenQueryAction(queryNodes));
        actions.add(new DeleteQueryAction(queryNodes));
        actions.add(new NotificationQueryAction(queryNodes));
        actions.add(new RefreshQueryAction(queryNodes));
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
            RequestProcessor.getDefault().post(new Runnable() {
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

    public static class RefreshQueryAction extends QueryAction {

        public RefreshQueryAction(QueryNode... queryNodes) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh"), queryNodes); //NOI18N
            putValue(ACCELERATOR_KEY, REFRESH_KEY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (QueryNode queryNode : getQueryNodes()) {
                queryNode.refreshContent();
            }
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

        private QueryMode mode;

        public OpenQueryAction(QueryNode... queryNodes) {
            this(Query.QueryMode.SHOW_ALL, queryNodes);
        }

        public OpenQueryAction(QueryMode mode, QueryNode... queryNodes) {
            super(NbBundle.getMessage(OpenQueryAction.class, "CTL_Open"), queryNodes); //NOI18N
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (QueryNode queryNode : getQueryNodes()) {
                queryNode.getQuery().open(mode);
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
                } else if (key.equals(TaskNode.class.getName())) {
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
            for (TreeListNode treeListNode : selectedNodes) {
                if (treeListNode instanceof RepositoryNode) {
                    new Actions.RefreshRepositoryAction((RepositoryNode) treeListNode).actionPerformed(e);
                } else if (treeListNode instanceof CategoryNode) {
                    new Actions.RefreshCategoryAction((CategoryNode) treeListNode).actionPerformed(e);
                } else if (treeListNode instanceof QueryNode) {
                    new Actions.RefreshQueryAction((QueryNode) treeListNode).actionPerformed(e);
                } else if (treeListNode instanceof TaskNode) {
                    new Actions.RefreshTaskAction(((TaskNode) treeListNode)).actionPerformed(e);
                }
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
}

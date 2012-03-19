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
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.tasks.ui.DashboardTopComponent;
import org.netbeans.modules.tasks.ui.dashboard.AbstractRepositoryNode;
import org.netbeans.modules.tasks.ui.dashboard.DashboardViewer;
import org.netbeans.modules.tasks.ui.dashboard.QueryNode;
import org.netbeans.modules.tasks.ui.dashboard.TaskNode;
import org.netbeans.modules.tasks.ui.model.Category;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class Actions {

    public static List<Action> getTaskPopupActions(TaskNode taskNode) {
        Issue task = taskNode.getTask();
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenTaskAction(task));
        actions.add(DashboardViewer.getInstance().isTaskNodeActive(taskNode) ? new DeactivateTaskAction() : new ActivateTaskAction(taskNode));
        if (taskNode.isCategorized()) {
            actions.add(new RemoveTaskAction(taskNode));
        }
        actions.add(new MoveTaskAction(taskNode));
        actions.add(new ScheduleTaskAction(task));
        actions.add(new RefreshTaskAction(task));
        actions.add(new NotificationTaskAction(task));
        return actions;
    }

    private static class RemoveTaskAction extends AbstractAction {

        private TaskNode taskNode;

        public RemoveTaskAction(TaskNode taskNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_RemoveFromCat")); //NOI18N
            this.taskNode = taskNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().removeTask(taskNode);
        }
    }

    private static class ScheduleTaskAction extends AbstractAction {

        public ScheduleTaskAction(Issue task) {
            super("Schedule"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class RefreshTaskAction extends AbstractAction {

        public RefreshTaskAction(Issue task) {
            super("Refresh"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class MoveTaskAction extends AbstractAction {

        private TaskNode taskNode;

        public MoveTaskAction(TaskNode taskNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_MoveTask")); //NOI18N
            this.taskNode = taskNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().addTask(taskNode);
        }
    }

    private static class NotificationTaskAction extends AbstractAction {

        public NotificationTaskAction(Issue task) {
            super("Notification"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    public static List<Action> getCategoryPopupActions(Category category) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new RenameCategoryAction(category));
        actions.add(new DeleteCategoryAction(category));
        actions.add(new RefreshCategoryAction(category));
        actions.add(new NotificationCategoryAction(category));
        return actions;
    }

    private static class DeleteCategoryAction extends AbstractAction {

        private Category category;

        public DeleteCategoryAction(Category category) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete")); //NOI18N
            this.category = category;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().deleteCategory(category);
        }
    }

    private static class NotificationCategoryAction extends AbstractAction {

        public NotificationCategoryAction(Category category) {
            super("Notification"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class RefreshCategoryAction extends AbstractAction {

        public RefreshCategoryAction(Category category) {
            super("Refresh"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class RenameCategoryAction extends AbstractAction {

        private Category category;

        public RenameCategoryAction(Category category) {
            super(NbBundle.getMessage(Actions.class, "CTL_Rename")); //NOI18N
            this.category = category;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().renameCategory(category);
        }
    }

    public static List<Action> getRepositoryPopupActions(AbstractRepositoryNode repositoryNode) {
        Repository repository = repositoryNode.getRepository();
        List<Action> actions = new ArrayList<Action>();
        actions.add(new EditRepositoryAction(repository));
        actions.add(new RemoveRepositoryAction(repositoryNode));
        actions.add(new CreateTaskAction(repository));
        actions.add(new SearchRepositoryAction(repository));
        actions.add(new RefreshRepositoryAction(repositoryNode));
        return actions;
    }

    private static class RefreshRepositoryAction extends AbstractAction {

        public RefreshRepositoryAction(AbstractRepositoryNode repositoryNode) {
            super("Refresh"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class EditRepositoryAction extends AbstractAction {

        public EditRepositoryAction(Repository repository) {
            super("Edit"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    public static List<Action> getQueryPopupActions(QueryNode queryNode) {
        Query query = queryNode.getQuery();
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenQueryAction(query));
        actions.add(new EditQueryAction(query));
        actions.add(new DeleteQueryAction(query));
        actions.add(new RefreshQueryAction(queryNode));
        actions.add(new NotificationQueryAction(query));
        return actions;
    }

    private static class DeleteQueryAction extends AbstractAction {

        public DeleteQueryAction(Query query) {
            super("Delete"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class EditQueryAction extends AbstractAction {

        public EditQueryAction(Query query) {
            super("Edit"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class RefreshQueryAction extends AbstractAction {

        private QueryNode queryNode;

        public RefreshQueryAction(QueryNode queryNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh")); //NOI18N
            this.queryNode = queryNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            queryNode.refreshContent();
        }
    }

    private static class NotificationQueryAction extends AbstractAction {

        public NotificationQueryAction(Query query) {
            super("Notification"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }
}

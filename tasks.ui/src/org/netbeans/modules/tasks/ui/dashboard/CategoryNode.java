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
package org.netbeans.modules.tasks.ui.dashboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.tasks.ui.LinkButton;
import org.netbeans.modules.tasks.ui.actions.Actions;
import org.netbeans.modules.tasks.ui.actions.CloseCategoryNodeAction;
import org.netbeans.modules.tasks.ui.actions.DummyAction;
import org.netbeans.modules.tasks.ui.actions.OpenCategoryNodeAction;
import org.netbeans.modules.tasks.ui.actions.OpenQueryAction;
import org.netbeans.modules.tasks.ui.model.Category;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.netbeans.modules.tasks.ui.utils.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class CategoryNode extends TaskContainerNode implements Comparable<CategoryNode> {

    private final Category category;
    private JPanel panel;
    private TreeLabel lblName;
    private LinkButton btnRefresh;
    private List<JLabel> labels;
    private CloseCategoryNodeAction closeCategoryAction;
    private OpenCategoryNodeAction openCategoryAction;
    private boolean opened;
    private TreeLabel lblTotal;
    private TreeLabel lblChanged;

    public CategoryNode(Category category) {
        this(category, true);
    }

    public CategoryNode(Category category, boolean opened) {
        super(true, null);
        this.category = category;
        this.opened = opened;
        labels = new ArrayList<JLabel>();
        updateNodes();
    }

    @Override
    protected List<TreeListNode> createChildren() {
        if (isRefresh()) {
            refreshTasks();
            updateNodes();
            setRefresh(false);
        }
        List<TaskNode> children = getFilteredTaskNodes();
        Collections.sort(children);
        return new ArrayList<TreeListNode>(children);
    }

    @Override
    void updateContent() {
        updateNodes();
        refreshChildren();
    }

    @Override
    List<Issue> getTasks() {
        return category.getTasks();
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
        taskNode.setCategory(category);
    }

    @Override
    void updateCounts() {
        synchronized (UI_LOCK) {
            lblTotal.setText(getTotalString());
            lblChanged.setText(getChangedString());
        }
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        synchronized (UI_LOCK) {
            if (panel == null) {
                panel = new JPanel(new GridBagLayout());
                panel.setOpaque(false);
                final JLabel iconLabel = new JLabel(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/category.png", true)); //NOI18N
                panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

                lblName = new TreeLabel(Utils.getCategoryDisplayText(this));
                panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
                labels.add(lblName);

                TreeLabel lbl = new TreeLabel("("); //NOI18N
                labels.add(lbl);
                addTotalCountComp(lbl);
                panel.add(lbl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));

                lblTotal = new TreeLabel(getTotalString());
                panel.add(lblTotal, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                labels.add(lblTotal);
                addTotalCountComp(lblTotal);

                lbl = new TreeLabel("|"); //NOI18N
                panel.add(lbl, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
                labels.add(lbl);
                addChangedCountComp(lbl);

                lblChanged = new TreeLabel(getChangedString());
                panel.add(lblChanged, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                labels.add(lblChanged);
                addChangedCountComp(lblChanged);

                lbl = new TreeLabel(")"); //NOI18N
                labels.add(lbl);
                addTotalCountComp(lbl);
                panel.add(lbl, new GridBagConstraints(6, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

                ProgressLabel lblProgress = getLblProgress();
                panel.add(lblProgress, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
                lblProgress.setVisible(false);
                labels.add(lblProgress);
                panel.add(new JLabel(), new GridBagConstraints(7, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

                btnRefresh = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/refresh.png", true), new DummyAction()); //NOI18N
                btnRefresh.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_Refresh")); //NOI18N
                panel.add(btnRefresh, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
            }
            lblName.setText(Utils.getCategoryDisplayText(this));
            for (JLabel jLabel : labels) {
                jLabel.setForeground(foreground);
            }
            return panel;
        }
    }

    private Action getCategoryAction() {
        if (opened) {
            if (closeCategoryAction == null) {
                closeCategoryAction = new CloseCategoryNodeAction(this);
            }
            return closeCategoryAction;
        } else {
            if (openCategoryAction == null) {
                openCategoryAction = new OpenCategoryNodeAction(this);
            }
            return openCategoryAction;
        }
    }

    public final Category getCategory() {
        return category;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Override
    public final Action[] getPopupActions() {
        List<Action> actions = new ArrayList<Action>();
        actions.add(getCategoryAction());
        actions.addAll(Actions.getCategoryPopupActions(this));
        return actions.toArray(new Action[actions.size()]);
    }

    public boolean addTaskNode(TaskNode taskNode, boolean isInFilter) {
        if (getTaskNodes().contains(taskNode)) {
            return false;
        }
        getTaskNodes().add(taskNode);
        category.addTask(taskNode.getTask());
        if (isInFilter) {
            getFilteredTaskNodes().add(taskNode);
        }
        return true;
    }

    public void removeTaskNode(TaskNode taskNode) {
        getTaskNodes().remove(taskNode);
        category.removeTask(taskNode.getTask());
        getFilteredTaskNodes().remove(taskNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CategoryNode other = (CategoryNode) obj;
        return category.equals(other.category);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.category != null ? this.category.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(CategoryNode toCompare) {
        return category.getName().compareToIgnoreCase(toCompare.getCategory().getName());
    }

    @Override
    public String toString() {
        return category.getName();
    }

    int indexOf(Issue task) {
        for (int i = 0; i < getTaskNodes().size(); i++) {
            TaskNode taskNode = getTaskNodes().get(i);
            if (taskNode.getTask().equals(task)) {
                return i;
            }
        }
        return -1;
    }

    private void refreshTasks() {
        Map<Repository, List<String>> map = getTasksToRepository(category.getTasks());
        Set<Repository> repositoryKeys = map.keySet();
        for (Repository repository : repositoryKeys) {
            List<String> ids = map.get(repository);
            repository.getIssues(ids.toArray(new String[ids.size()]));
        }
    }

    private Map<Repository, List<String>> getTasksToRepository(List<Issue> tasks) {
        Map<Repository, List<String>> map = new HashMap<Repository, List<String>>();
        for (Issue issue : tasks) {
            Repository repositoryKey = issue.getRepository();
            if (map.containsKey(repositoryKey)) {
                map.get(repositoryKey).add(issue.getID());
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(issue.getID());
                map.put(repositoryKey, list);
            }
        }
        return map;
    }
}
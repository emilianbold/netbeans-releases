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

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.tasks.ui.actions.Actions;
import org.netbeans.modules.tasks.ui.filter.AppliedFilters;
import org.netbeans.modules.tasks.ui.model.Category;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;

/**
 *
 * @author jpeska
 */
public abstract class AbstractCategoryNode extends TreeListNode implements Comparable<AbstractCategoryNode> {

    private final Category category;
    private List<TaskNode> taskNodes;
    private List<TaskNode> filteredTaskNodes;

    public AbstractCategoryNode(boolean expandable, Category category) {
        super(expandable, null);
        this.category = category;
        updateNodes();
    }

    final void updateNodes() {
        AppliedFilters appliedFilters = DashboardViewer.getInstance().getAppliedFilters();
        List<Issue> tasks = category.getTasks();
        taskNodes = new ArrayList<TaskNode>(tasks.size());
        filteredTaskNodes = new ArrayList<TaskNode>(tasks.size());
        for (Issue issue : tasks) {
            TaskNode taskNode = new TaskNode(issue, this);
            taskNode.setCategory(category);
            taskNodes.add(taskNode);
            if (appliedFilters.isInFilter(issue)) {
                filteredTaskNodes.add(taskNode);
            }
        }
    }

    public final Category getCategory() {
        return category;
    }

    @Override
    public final Action[] getPopupActions() {
        List<Action> actions = new ArrayList<Action>();
        actions.add(getCategoryAction());
        actions.addAll(Actions.getCategoryPopupActions(category));
        return actions.toArray(new Action[actions.size()]);
    }

    protected abstract Action getCategoryAction();

    abstract void updateContent();

    public List<TaskNode> getTaskNodes() {
        return new ArrayList<TaskNode>(taskNodes);
    }

    public int getTotalCount() {
        return filteredTaskNodes.size();
    }

    public int getModifiedCount() {
        int modifiedCount = 0;
        for (TaskNode taskNode : getFilteredTaskNodes()) {
            if (taskNode.getTask().getStatus() != Issue.Status.UPTODATE) {
                modifiedCount++;
            }
        }
        return modifiedCount;
    }

    public List<TaskNode> getFilteredTaskNodes() {
        return filteredTaskNodes;
    }

    public void setFilteredTaskNodes(List<TaskNode> filteredTaskNodes) {
        this.filteredTaskNodes = filteredTaskNodes;
    }

    public boolean addTaskNode(TaskNode taskNode, boolean isInFilter) {
        if (taskNodes.contains(taskNode)) {
            return false;
        }
        taskNodes.add(taskNode);
        category.addTask(taskNode.getTask());
        if (isInFilter) {
            filteredTaskNodes.add(taskNode);
        }
        return true;
    }

    public void removeTaskNode(TaskNode taskNode) {
        taskNodes.remove(taskNode);
        category.removeTask(taskNode.getTask());
        filteredTaskNodes.remove(taskNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractCategoryNode other = (AbstractCategoryNode) obj;
        return category.equals(other.category);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.category != null ? this.category.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(AbstractCategoryNode toCompare) {
        return category.getName().compareToIgnoreCase(toCompare.getCategory().getName());
    }

    @Override
    public String toString() {
        return category.getName();
    }

    int indexOf(Issue task) {
        for (int i = 0; i < taskNodes.size(); i++) {
            TaskNode taskNode = taskNodes.get(i);
            if (taskNode.getTask().equals(task)) {
                return i;
            }
        }
        return -1;
    }
}

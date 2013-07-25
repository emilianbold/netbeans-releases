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

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.OpenTaskAction;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;

/**
 *
 * @author jpeska
 */
public class TaskNode extends TaskContainerNode implements Comparable<TaskNode>, Submitable {

    private final IssueImpl task;
    private JPanel panel;
    private TreeLabel lblName;
    private Category category;
    private final TaskListener taskListener;
    private final Object LOCK = new Object();
    private boolean unsubmitted = false;

    public TaskNode(IssueImpl task, TreeListNode parent) {
        // TODO subtasks, it is not in bugtracking API
        //super(task.hasSubtasks(), parent);
        super(false, false, parent, DashboardUtils.getTaskAnotatedText(task));
        this.task = task;
        taskListener = new TaskListener();
    }

    @Override
    protected void attach() {
        super.attach();
        this.task.addPropertyChangeListener(taskListener);
    }

    @Override
    protected void dispose() {
        super.dispose();
        this.task.removePropertyChangeListener(taskListener);
    }

    @Override
    public List<IssueImpl> getTasks(boolean includingNodeItself) {
        if (includingNodeItself) {
            List<IssueImpl> l = new ArrayList<IssueImpl>(1);
            l.add(task);
            return l;
        }
        return Collections.emptyList();
    }

    @Override
    void updateCounts() {
    }

    @Override
    boolean isTaskLimited() {
        return false;
    }

    @Override
    void refreshTaskContainer() {
        task.refresh();
    }

    @Override
    protected JComponent createComponent(List<IssueImpl> data) {
        if (isError()) {
            setError(false);
            return null;
        }
        updateNodes(data);
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        synchronized (LOCK) {
            labels.clear();
            buttons.clear();

            lblName = new TreeLabel();
            panel.add(lblName, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 3), 0, 0));
            lblName.setToolTipText(task.getTooltip());
            labels.add(lblName);
        }
        return panel;
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        super.configure(component, foreground, background, isSelected, hasFocus, rowWidth);
        lblName.setText(DashboardUtils.getTaskDisplayText(task, lblName, rowWidth, DashboardViewer.getInstance().isTaskNodeActive(this), isSelected || hasFocus));
    }

    @Override
    protected List<TreeListNode> createChildren() {
//        if(!task.hasSubtasks()){
//            return Collections.emptyList();
//        }
//        List<TaskNode> children = new ArrayList<TaskNode>();
//        List<Issue> tasks = task.getSubtasks();
//        AppliedFilters filters = DashboardViewer.getInstance().getAppliedFilters();
//        for (Issue t : tasks) {
//            if (filters.isInFilter(t)) {
//                children.add(new TaskNode(t, this));
//            }
//        }
//        Collections.sort(children);
//        return new ArrayList<TreeListNode>(children);
        return Collections.emptyList();
    }

    @Override
    protected Action getDefaultAction() {
        return new OpenTaskAction(this);
    }

    @Override
    public Action[] getPopupActions() {
        List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
        TaskNode[] taskNodes = new TaskNode[selectedNodes.size()];
        boolean justTasks = true;
        boolean containsNewTask = false;
        for (int i = 0; i < selectedNodes.size(); i++) {
            TreeListNode treeListNode = selectedNodes.get(i);
            if (treeListNode instanceof TaskNode) {
                taskNodes[i] = (TaskNode) treeListNode;
                if (!containsNewTask &&((TaskNode) treeListNode).getTask().isNew()) {
                    containsNewTask = true;
                }
            } else {
                justTasks = false;
                break;
            }
        }
        List<Action> actions = new ArrayList<Action>();
        if (justTasks) {
            actions.addAll(Actions.getTaskPopupActions(taskNodes));
        }
        actions.addAll(Actions.getSubmitablePopupActions(selectedNodes.toArray(new TreeListNode[selectedNodes.size()])));
        List<Action> defaultActions = Actions.getDefaultActions(selectedNodes.toArray(new TreeListNode[selectedNodes.size()]));
        if (containsNewTask) {
            for (Action action : defaultActions) {
                action.setEnabled(false);
            }
        }
        actions.addAll(defaultActions);
        return actions.toArray(new Action[actions.size()]);
    }

    public IssueImpl getTask() {
        return task;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isCategorized() {
        return category != null && category.persist();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskNode other = (TaskNode) obj;
        if (isCategorized() && other.isCategorized()) {
            return taskEquals(other.getTask());
        } else {
            return getParent().equals(other.getParent()) && taskEquals(other.getTask());
        }
    }

    private boolean taskEquals(IssueImpl other) {
        // TODO complete task equals method
        if (task.getStatus() != other.getStatus()) {
            return false;
        }
        if (!task.getRepositoryImpl().getId().equalsIgnoreCase(other.getRepositoryImpl().getId())) {
            return false;
        }
        if (!task.getID().equalsIgnoreCase(other.getID())) {
            return false;
        }
        if (!task.getDisplayName().equalsIgnoreCase(other.getDisplayName())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.task != null ? this.task.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(TaskNode toCompare) {
        //compare status
        int statusCompare = task.getStatus().compareTo(toCompare.task.getStatus());
        if (statusCompare != 0) {
            return statusCompare;
        }

        //compare ID
        int id = 0;
        boolean isIdNumeric = true;
        try {
            id = Integer.parseInt(task.getID());
        } catch (NumberFormatException numberFormatException) {
            isIdNumeric = false;
        }
        int idOther = 0;
        boolean isIdOtherNumberic = true;
        try {
            idOther = Integer.parseInt(toCompare.task.getID());
        } catch (NumberFormatException numberFormatException) {
            isIdOtherNumberic = false;
        }
        if (isIdNumeric && isIdOtherNumberic) {
            return compareNumericId(id, idOther);
        } else if (isIdNumeric) {
            return -1;
        } else if (isIdOtherNumberic) {
            return 1;
        } else {
            return compareComplexId(task.getID(), toCompare.task.getID());
        }

    }

    @Override
    public String toString() {
        return task.getDisplayName();
    }

    private int compareNumericId(int id, int idOther) {
        if (id < idOther) {
            return 1;
        } else if (id > idOther) {
            return -1;
        } else {
            return 0;
        }
    }

    private int compareComplexId(String id1, String id2) {
        int dividerIndex1 = id1.lastIndexOf("-"); //NOI18
        int dividerIndex2 = id2.lastIndexOf("-"); //NOI18
        if (dividerIndex1 == -1 || dividerIndex2 == -1) {
            DashboardViewer.LOG.log(Level.WARNING, "Unsupported ID format");
            return 0;
        }
        String prefix1 = id1.subSequence(0, dividerIndex1).toString();
        String suffix1 = id1.substring(dividerIndex1 + 1);

        String prefix2 = id2.subSequence(0, dividerIndex2).toString();
        String suffix2 = id2.substring(dividerIndex2 + 1);

        //compare prefix, alphabetically
        int comparePrefix = prefix1.compareTo(prefix2);
        if (comparePrefix != 0) {
            return comparePrefix;
        }
        //compare number suffix
        return compareNumericId(Integer.parseInt(suffix1), Integer.parseInt(suffix2));
    }

    @Override
    public boolean isUnsubmitted() {
        if (getParent() instanceof Submitable) {
            Submitable s = (Submitable) getParent();
            return s.isUnsubmitted();
        }
        return false;
    }

    public void setUnsubmitted(boolean unsubmitted) {
        this.unsubmitted = unsubmitted;
    }

    @Override
    public List<IssueImpl> getTasksToSubmit() {
        return getTasks(true);
    }

    private class TaskListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(IssueImpl.EVENT_ISSUE_REFRESHED)) {
                fireContentChanged();
            }
        }
    }
}

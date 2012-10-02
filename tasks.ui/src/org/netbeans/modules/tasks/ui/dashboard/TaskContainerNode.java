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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.tasks.ui.LinkButton;
import org.netbeans.modules.tasks.ui.filter.AppliedFilters;
import org.netbeans.modules.tasks.ui.settings.DashboardSettings;
import org.netbeans.modules.tasks.ui.treelist.AsynchronousNode;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public abstract class TaskContainerNode extends AsynchronousNode<List<Issue>> {

    private List<TaskNode> taskNodes;
    private List<TaskNode> filteredTaskNodes;
    private TaskListener taskListener;
    private boolean refresh;
    private final Object LOCK = new Object();
    private Collection<Issue> toSelect;
    protected List<TreeLabel> labels;
    protected List<LinkButton> buttons;
    private int pageSize;
    private int pageCountShown;

    public TaskContainerNode(boolean expandable, TreeListNode parent, String title) {
        this(false, expandable, parent, title);
    }

    public TaskContainerNode(boolean refresh, boolean expandable, TreeListNode parent, String title) {
        super(expandable, parent, title);
        this.refresh = refresh;
        labels = new ArrayList<TreeLabel>();
        buttons = new ArrayList<LinkButton>();
        initPaging();
    }

    abstract List<Issue> getTasks();

    abstract void adjustTaskNode(TaskNode taskNode);

    abstract void updateCounts();

    abstract boolean isTaskLimited();

    @Override
    protected void childrenLoadingFinished() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (toSelect != null) {
                    DashboardViewer.getInstance().setSelection(toSelect);
                    toSelect = null;
                }
            }
        });
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        for (JLabel lbl : labels) {
            lbl.setForeground(foreground);
        }
        for (LinkButton lb : buttons) {
            lb.setForeground(foreground, isSelected);
        }
    }

    @Override
    protected void dispose() {
        super.dispose();
        removeTaskListeners();
    }

    void updateContent() {
        updateContentAndSelect(null);
    }

    void updateContentAndSelect(Collection<Issue> toSelect) {
        this.toSelect = toSelect;
        final boolean empty = getChildren().isEmpty();
        boolean expand = toSelect != null && !toSelect.isEmpty() && !isExpanded();
        updateNodes();
        updateCounts();
        // expand node if needed
        if (expand) {
            setExpanded(true);
        }

        // if getChildren().isEmpty() is true, refresh was already performed in setExpanded
        if (!empty || !expand) {
            refreshChildren();
        }
    }

    public final void refreshContent() {
        refresh = true;
        initPaging();
        refresh();
    }

    public final List<TaskNode> getFilteredTaskNodes() {
        return filteredTaskNodes;
    }

    public final List<TaskNode> getTaskNodes() {
        return taskNodes;
    }

    public final boolean isRefresh() {
        return refresh;
    }

    public final void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public final int getChangedTaskCount() {
        synchronized (LOCK) {
            int count = 0;
            for (TaskNode taskNode : filteredTaskNodes) {
                if (taskNode.getTask().getStatus() != Issue.Status.UPTODATE) {
                    count++;
                }
            }
            return count;
        }
    }

    public final int getTotalTaskCount() {
        synchronized (LOCK) {
            return filteredTaskNodes.size();
        }
    }
    
    final void updateNodes() {
        synchronized (LOCK) {
            DashboardViewer dashboard = DashboardViewer.getInstance();
            AppliedFilters appliedFilters = dashboard.getAppliedTaskFilters();
            List<Issue> issues = getTasks();
            disposeTaskNodes();
            removeTaskListeners();
            if (taskListener == null) {
                taskListener = new TaskListener();
            }
            taskNodes = new ArrayList<TaskNode>(issues.size());
            filteredTaskNodes = new ArrayList<TaskNode>(issues.size());
            for (Issue issue : issues) {
                issue.addPropertyChangeListener(taskListener);
                TaskNode taskNode = new TaskNode(issue, this);
                adjustTaskNode(taskNode);
                taskNodes.add(taskNode);
                if (appliedFilters.isInFilter(issue)) {
                    dashboard.addTaskMapEntry(issue, taskNode);
                    filteredTaskNodes.add(taskNode);
                }
            }
        }
    }

    final String getTotalString() {
        String bundleName = DashboardViewer.getInstance().expandNodes() ? "LBL_Matches" : "LBL_Total"; //NOI18N
        return getTotalTaskCount() + " " + NbBundle.getMessage(TaskContainerNode.class, bundleName);
    }

    final String getChangedString() {
        return getChangedTaskCount() + " " + NbBundle.getMessage(TaskContainerNode.class, "LBL_Changed");//NOI18N
    }

    final void removeTaskListeners() {
        synchronized (LOCK) {
            if (taskListener != null) {
                for (TaskNode taskNode : taskNodes) {
                    taskNode.getTask().removePropertyChangeListener(taskListener);
                }
            }
        }
    }

    final void addTaskListeners() {
        synchronized (LOCK) {
            if (taskListener == null) {
                taskListener = new TaskListener();
            }
            for (TaskNode taskNode : filteredTaskNodes) {
                taskNode.getTask().addPropertyChangeListener(taskListener);
            }
        }
    }

    final void showAdditionalPage(){
        pageCountShown++;
        updateContent();
    }

    @Override
    protected List<TreeListNode> createChildren() {
        List<TaskNode> filteredNodes = getFilteredTaskNodes();
        Collections.sort(filteredNodes);
        List<TaskNode> taskNodesToShow;
        boolean addShowNext = false;
        int taskCountToShow = getTaskCountToShow();
        if (!isTaskLimited() || filteredNodes.size() <= taskCountToShow) {
            taskNodesToShow = filteredNodes;
        } else {
            taskNodesToShow = new ArrayList<TaskNode>(filteredNodes.subList(0, taskCountToShow));
            addShowNext = true;
        }
        ArrayList<TreeListNode> children = new ArrayList<TreeListNode>(taskNodesToShow);
        if (addShowNext) {
            children.add(new ShowNextNode(this, Math.min(filteredNodes.size() - children.size(), pageSize)));
        }
        return children;
    }

    private int getTaskCountToShow(){
        return pageSize * pageCountShown;
    }

    final void initPaging() {
        pageSize = DashboardSettings.getInstance().isTasksLimit() ? DashboardSettings.getInstance().getTasksLimitValue() : Integer.MAX_VALUE;
        pageCountShown = 1;
    }

    private void disposeTaskNodes() {
        synchronized(LOCK) {
            if (taskNodes != null) {
                for (TaskNode taskNode : taskNodes) {
                    taskNode.dispose();
                }
            }
        }
    }

    private class TaskListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Issue.EVENT_ISSUE_REFRESHED)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateNodes();
                        updateCounts();
                    }
                });
            }
        }
    }
}

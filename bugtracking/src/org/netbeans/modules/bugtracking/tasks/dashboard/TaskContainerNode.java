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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.bugtracking.tasks.filter.AppliedFilters;
import org.netbeans.modules.bugtracking.tasks.settings.DashboardSettings;
import org.netbeans.modules.team.commons.treelist.AsynchronousNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jpeska
 */
public abstract class TaskContainerNode extends AsynchronousNode<List<IssueImpl>> implements Refreshable{

    private List<TaskNode> taskNodes;
    private List<TaskNode> filteredTaskNodes;
    private TaskListener taskListener;
    private boolean refresh;
    private final Object LOCK = new Object();
    private Collection<TaskNode> toSelect;
    protected List<TreeLabel> labels;
    protected List<LinkButton> buttons;
    private int pageSize;
    private int pageCountShown;
    private boolean error;

    private RequestProcessor rp = new RequestProcessor("Tasks Dashboard - TaskContainerNode", 10); // NOI18N
    
    public TaskContainerNode(boolean refresh, boolean expandable, TreeListNode parent, String title) {
        super(expandable, parent, title);
        this.refresh = refresh;
        labels = new ArrayList<TreeLabel>();
        buttons = new ArrayList<LinkButton>();
        initPaging();
    }

    public abstract List<IssueImpl> getTasks(boolean includingNodeItself);

    abstract void updateCounts();

    abstract boolean isTaskLimited();

    abstract void refreshTaskContainer();

    //override if you need to adjust node during updateNodes method
    void adjustTaskNode(TaskNode taskNode) {
    }

    @Override
    protected List<IssueImpl> load() {
        if (refresh) {
            refreshTaskContainer();
            refresh = false;
        }
        return getTasks(false);
    }

    @Override
    protected void childrenLoadingFinished() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (toSelect != null) {
                    DashboardViewer.getInstance().select(toSelect);
                    toSelect = null;
                }
            }
        });
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        for (JLabel lbl : labels) {
            lbl.setForeground(foreground);
        }
        for (LinkButton lb : buttons) {
            lb.setForeground(foreground, isSelected);
        }
    }

    @Override
    protected void attach() {
        super.attach();
        addTaskListeners();
    }

    @Override
    protected void dispose() {
        super.dispose();
        removeTaskListeners();
    }

    void updateContent() {
        updateContentAndSelect(null);
    }

    void updateContentAndSelect(Collection<TaskNode> toSelect) {
        this.toSelect = toSelect;
        final boolean empty = getChildren().isEmpty();
        boolean expand = toSelect != null && !toSelect.isEmpty() && !isExpanded();
        updateNodes();
        updateCounts();
        fireContentChanged();
        // expand node if needed
        if (expand) {
            setExpanded(true);
        }

        // if getChildren().isEmpty() is true, refresh was already performed in setExpanded
        if (!empty || !expand) {
            refreshChildren();
        }
    }

    @Override
    public final void refreshContent() {
        refresh = true;
        initPaging();
        refresh();
    }

    final List<TaskNode> getFilteredTaskNodes() {
        return filteredTaskNodes;
    }

    final List<TaskNode> getTaskNodes() {
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
                if (taskNode.getTask().getStatus() != IssueStatusProvider.Status.SEEN) {
                    count++;
                }
            } 
            return count;
        }
    }

    public final int getFilteredTaskCount() {
        synchronized (LOCK) {
            return filteredTaskNodes != null ? filteredTaskNodes.size() : 0;
        }
    }

    final void updateNodes() {
        updateNodes(getTasks(false));
    }

    final void updateNodes(List<IssueImpl> tasks) {
        
        synchronized (LOCK) {
            DashboardViewer dashboard = DashboardViewer.getInstance();
            AppliedFilters appliedFilters = dashboard.getAppliedTaskFilters();
            removeTaskListeners();
            if (taskListener == null) {
                taskListener = new TaskListener();
            }
            
            if(taskNodes == null) {
                taskNodes = new ArrayList<TaskNode>(tasks.size());
            }
            
            // remove obsolete
            Set<String> set = new HashSet<String>(tasks.size());
            for (IssueImpl task : tasks) {
                set.add(task.getID());
            }
            Iterator<TaskNode> it = taskNodes.iterator();
            while(it.hasNext()) {
                TaskNode n = it.next();
                if(!set.contains(n.getTask().getID())) {
                    it.remove();
                }
            }
            
            // add new ones
            set = new HashSet<String>(taskNodes.size());
            for (TaskNode n : taskNodes) {
                set.add(n.getTask().getID());
            }
            
            for (IssueImpl task : tasks) {
                if(!set.contains(task.getID())) {
                    TaskNode taskNode = new TaskNode(task, this);
                    adjustTaskNode(taskNode);
                    taskNodes.add(taskNode);
                }
            }
            addTaskListeners();

            filteredTaskNodes = new ArrayList<TaskNode>(tasks.size());
            for (TaskNode taskNode : taskNodes) {
                if (appliedFilters.isInFilter(taskNode.getTask())) {
                    filteredTaskNodes.add(taskNode);
                }
            }
        }
    }

    final String getTotalString() {
        String bundleName = DashboardViewer.getInstance().expandNodes() ? "LBL_Matches" : "LBL_Total"; //NOI18N
        return getFilteredTaskCount() + " " + NbBundle.getMessage(TaskContainerNode.class, bundleName);
    }

    final String getChangedString(int count) {
        return count + " " + NbBundle.getMessage(TaskContainerNode.class, "LBL_Changed");//NOI18N
    }

    private void removeTaskListeners() {
        synchronized (LOCK) {
            if (taskListener != null && taskNodes != null) {
                for (TaskNode taskNode : taskNodes) {
                    taskNode.getTask().removePropertyChangeListener(taskListener);
                }
            }
        }
    }

    private void addTaskListeners() {
        synchronized (LOCK) {
            if (taskListener != null && taskNodes != null) {
                for (TaskNode taskNode : taskNodes) {
                    taskNode.getTask().addPropertyChangeListener(taskListener);
                }
            }
        }
    }

    final void showAdditionalPage() {
        Collection<TaskNode> list = new ArrayList<TaskNode>(1);
        list.add(filteredTaskNodes.get(getTaskCountToShow() - 1));
        pageCountShown++;
        updateContentAndSelect(list);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        List<TaskNode> filteredNodes = filteredTaskNodes;
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

    private int getTaskCountToShow() {
        return pageSize * pageCountShown;
    }

    final void initPaging() {
        pageSize = DashboardSettings.getInstance().isTasksLimit() ? DashboardSettings.getInstance().getTasksLimitValue() : Integer.MAX_VALUE;
        pageCountShown = 1;
    }

    final void handleError(Throwable throwable) {
        setRefresh(true);
        setError(true);
        DashboardViewer.LOG.log(Level.WARNING, "Tasks loading failed due to: {0}", throwable); //NOI18N
    }

    boolean isError() {
        return error;
    }

    void setError(boolean error) {
        this.error = error;
    }

    private void refilterTaskNodes() {
        DashboardViewer dashboard = DashboardViewer.getInstance();
        AppliedFilters appliedFilters = dashboard.getAppliedTaskFilters();
        filteredTaskNodes.clear();
        for (TaskNode taskNode : taskNodes) {
            if (appliedFilters.isInFilter(taskNode.getTask())) {
                filteredTaskNodes.add(taskNode);
            }
        }
    }

    private final RequestProcessor.Task updateTask = rp.create(new Runnable() {
        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refilterTaskNodes();
                    updateCounts();
                    fireContentChanged();
                }
            });
        }
    });
    
    private class TaskListener implements PropertyChangeListener {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(IssueImpl.EVENT_ISSUE_REFRESHED)) {
                updateTask.schedule(1000);
            }
        }
    }
}

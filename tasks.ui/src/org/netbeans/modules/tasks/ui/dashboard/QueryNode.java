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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.tasks.ui.LinkButton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.tasks.ui.actions.Actions;
import org.netbeans.modules.tasks.ui.actions.OpenQueryAction;
import org.netbeans.modules.tasks.ui.filter.AppliedFilters;
import org.netbeans.modules.tasks.ui.treelist.AsynchronousNode;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class QueryNode extends AsynchronousNode<List<Issue>> implements Comparable<QueryNode>, PropertyChangeListener {

    private final Query query;
    private JPanel panel;
    private List<TreeLabel> labels;
    private List<LinkButton> buttons;
    private TreeLabel lblName;
    private List<TaskNode> taskNodes;
    private List<TaskNode> filteredTaskNodes;
    private final Object LOCK = new Object();
    private boolean refresh;
    private LinkButton btnChanged;
    private LinkButton btnTotal;

    public QueryNode(Query query, TreeListNode parent, boolean refresh) {
        super(true, parent, query.getDisplayName());
        this.query = query;
        labels = new ArrayList<TreeLabel>();
        buttons = new ArrayList<LinkButton>();
        updateNodes();
        query.addPropertyChangeListener(this);
        this.refresh = refresh;
    }

    private void updateNodes() {
        AppliedFilters appliedFilters = DashboardViewer.getInstance().getAppliedFilters();
        Collection<Issue> issues = query.getIssues();
        taskNodes = new ArrayList<TaskNode>(issues.size());
        filteredTaskNodes = new ArrayList<TaskNode>(issues.size());
        for (Issue issue : issues) {
            TaskNode taskNode = new TaskNode(issue, this);
            taskNodes.add(taskNode);
            if (appliedFilters.isInFilter(issue)) {
                filteredTaskNodes.add(taskNode);
            }
        }
    }

    @Override
    protected void dispose() {
        super.dispose();
        query.removePropertyChangeListener(this);
        for (TaskNode taskNode : getFilteredTaskNodes()) {
            taskNode.getTask().addPropertyChangeListener(this);
        }
    }

    @Override
    protected List<Issue> load() {
        if (refresh) {
            query.refresh(true);
            refresh = false;
        }
        return new ArrayList<Issue>(query.getIssues());
    }

    @Override
    protected List<TreeListNode> createChildren() {
        List<TaskNode> children = getFilteredTaskNodes();
        Collections.sort(children);
        for (TaskNode taskNode : children) {
            taskNode.getTask().addPropertyChangeListener(this);
        }
        return new ArrayList<TreeListNode>(children);
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        synchronized (LOCK) {
            for (JLabel lbl : labels) {
                lbl.setForeground(foreground);
            }
            for (LinkButton lb : buttons) {
                lb.setForeground(foreground, isSelected);
            }
        }
    }

    @Override
    protected JComponent createComponent(List<Issue> data) {
        updateNodes();
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        synchronized (LOCK) {
            labels.clear();
            buttons.clear();
            int col = 0;
            lblName = new TreeLabel(query.getDisplayName());
            panel.add(lblName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            labels.add(lblName);

            TreeLabel lbl;
            if (getTotalTaskCount() > 0) {
                lbl = new TreeLabel("("); //NOI18N
                labels.add(lbl);
                panel.add(lbl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));

                btnTotal = new LinkButton(getTotalString(), new OpenQueryAction(query));
                panel.add(btnTotal, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                buttons.add(btnTotal);

                if (getChangedTaskCount() > 0) {
                    lbl = new TreeLabel("|"); //NOI18N
                    panel.add(lbl, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
                    labels.add(lbl);
                    btnChanged = new LinkButton(getChangedString(), new OpenQueryAction(query)); //NOI18N
                    panel.add(btnChanged, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    buttons.add(btnChanged);
                }

                lbl = new TreeLabel(")"); //NOI18N
                labels.add(lbl);
                panel.add(lbl, new GridBagConstraints(6, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            }
            panel.add(new JLabel(), new GridBagConstraints(8, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            if (DashboardViewer.getInstance().containsActiveTask(this)) {
                lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
            } else {
                lblName.setFont(lblName.getFont().deriveFont(Font.PLAIN));
            }
        }
        return panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Query.EVENT_QUERY_ISSUES_CHANGED)) {
            updateContent(true);
        } else if (evt.getPropertyName().equals(Issue.EVENT_ISSUE_REFRESHED)){
            updateContent(false);
        }
    }

    private void updateContent(boolean refreshChildren) {
        updateCounts();
        fireContentChanged();
        if (refreshChildren) {
            refreshChildren();
        }
    }

    private void updateCounts() {
        updateNodes();
        if (btnTotal != null) {
            btnTotal.setText(getTotalString());
        }
        if (btnChanged != null) {
            btnChanged.setText(getChangedString());
        }
    }

    public void refreshContent() {
        refresh = true;
        refresh();
    }

    public List<TaskNode> getFilteredTaskNodes() {
        return filteredTaskNodes;
    }

    public void setFilteredTaskNodes(List<TaskNode> filteredTaskNodes) {
        this.filteredTaskNodes = filteredTaskNodes;
    }

    public List<TaskNode> getTaskNodes() {
        return taskNodes;
    }

    public int getChangedTaskCount() {
        int count = 0;
        for (TaskNode taskNode : filteredTaskNodes) {
            if (taskNode.getTask().getStatus() != Issue.Status.UPTODATE) {
                count++;
            }
        }
        return count;
    }

    public int getTotalTaskCount() {
        return filteredTaskNodes.size();
    }

    @Override
    protected Action getDefaultAction() {
        return new OpenQueryAction(query);
    }

    @Override
    public Action[] getPopupActions() {
        List<Action> actions = Actions.getQueryPopupActions(this);
        return actions.toArray(new Action[actions.size()]);
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryNode other = (QueryNode) obj;
        //TODO complete query equals method
        return query.getDisplayName().equalsIgnoreCase(other.query.getDisplayName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.query != null ? this.query.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(QueryNode toCompare) {
        return query.getDisplayName().compareToIgnoreCase(toCompare.query.getDisplayName());
    }

    @Override
    public String toString() {
        return this.query.getDisplayName();
    }

    private String getTotalString() {
        String bundleName = DashboardViewer.getInstance().expandNodes() ? "LBL_Matches" : "LBL_Total"; //NOI18N
        return getTotalTaskCount() + " " + NbBundle.getMessage(QueryNode.class, bundleName);
    }

    private String getChangedString() {
        return getChangedTaskCount() + " " + NbBundle.getMessage(QueryNode.class, "LBL_Changed");
    }
}

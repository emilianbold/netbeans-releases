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
import java.util.Map;
import javax.swing.*;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.tasks.ui.actions.*;
import org.netbeans.modules.tasks.ui.actions.Actions.CloseRepositoryNodeAction;
import org.netbeans.modules.tasks.ui.actions.Actions.CreateTaskAction;
import org.netbeans.modules.tasks.ui.actions.Actions.OpenRepositoryNodeAction;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.netbeans.modules.tasks.ui.utils.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class RepositoryNode extends TreeListNode implements PropertyChangeListener, Comparable<RepositoryNode> {

    private final Repository repository;
    private List<QueryNode> queryNodes;
    private List<QueryNode> filteredQueryNodes;
    private boolean loaded;
    private boolean refresh;
    private JPanel panel;
    private TreeLabel lblName;
    private final Object LOCK = new Object();
    private LinkButton btnRefresh;
    private LinkButton btnSearch;
    private LinkButton btnCreateTask;
    private CloseRepositoryNodeAction closeRepositoryAction;
    private OpenRepositoryNodeAction openRepositoryAction;
    private LinkButton btnEmtpyContent;

    public RepositoryNode(Repository repository, boolean loaded) {
        this(repository, loaded, true);
    }

    public RepositoryNode(Repository repository, boolean loaded, boolean opened) {
        super(opened, null);
        this.repository = repository;
        this.loaded = loaded;
        this.refresh = false;
        updateNodes();
        repository.addPropertyChangeListener(this);
    }

    @Override
    protected void dispose() {
        super.dispose();
        getRepository().removePropertyChangeListener(this);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        if (refresh) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (QueryNode queryNode : queryNodes) {
                        queryNode.refreshContent();
                    }
                    refresh = false;
                }
            });
        }
        loaded = true;
        if (!filteredQueryNodes.isEmpty()) {
            List<QueryNode> children = filteredQueryNodes;
            boolean expand = DashboardViewer.getInstance().expandNodes();
            for (QueryNode queryNode : children) {
                queryNode.setExpanded(expand);
            }
            Collections.sort(children);
            return new ArrayList<TreeListNode>(children);
        } else {
            List<TreeListNode> children = new ArrayList<TreeListNode>();
            children.add(new EmptyContentNode(this, getEmptyContentLink()));
            return children;
        }
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        synchronized (LOCK) {
            if (panel == null) {
                panel = new JPanel(new GridBagLayout());
                panel.setOpaque(false);
                final JLabel iconLabel = new JLabel(getIcon()); //NOI18N
                if (!isOpened()) {
                    iconLabel.setEnabled(false);
                }
                panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

                lblName = new TreeLabel(getRepository().getDisplayName());
                panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
                panel.add(new JLabel(), new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                if (isOpened()) {
                    btnRefresh = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/refresh.png", true), new Actions.RefreshRepositoryAction(this)); //NOI18N
                    btnRefresh.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_Refresh")); //NOI18N
                    panel.add(btnRefresh, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));

                    btnSearch = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/search_repo.png", true), new SearchRepositoryAction(this)); //NOI18N
                    btnSearch.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_SearchInRepo")); //NOI18N
                    panel.add(btnSearch, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));

                    btnCreateTask = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/add_task.png", true), new CreateTaskAction(this)); //NOI18N
                    btnCreateTask.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_CreateTask")); //NOI18N
                    panel.add(btnCreateTask, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
                }

            }
            lblName.setText(Utils.getRepositoryDisplayText(this));
            lblName.setForeground(foreground);
            return panel;
        }
    }

    final void updateNodes() {
        queryNodes = new ArrayList<QueryNode>();
        filteredQueryNodes = new ArrayList<QueryNode>();
        Collection<Query> queries = getQueries();
        for (Query query : queries) {
            QueryNode queryNode = new QueryNode(query, this, !loaded);
            queryNodes.add(queryNode);
            if ((DashboardViewer.getInstance().expandNodes() && !queryNode.getFilteredTaskNodes().isEmpty()) || !DashboardViewer.getInstance().expandNodes()) {
                filteredQueryNodes.add(queryNode);
            }
        }
    }

    public final Repository getRepository() {
        return repository;
    }

    public boolean isOpened() {
        return true;
    }

    @Override
    public final Action[] getPopupActions() {
        List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
        RepositoryNode[] repositoryNodes = new RepositoryNode[selectedNodes.size()];
        for (int i = 0; i < selectedNodes.size(); i++) {
            TreeListNode treeListNode = selectedNodes.get(i);
            if (treeListNode instanceof RepositoryNode) {
                repositoryNodes[i] = (RepositoryNode) treeListNode;
            } else {
                return null;
            }
        }
        List<Action> actions = new ArrayList<Action>();
        Action repositoryAction = getRepositoryAction(repositoryNodes);
        if (repositoryAction != null) {
            actions.add(repositoryAction);
        }
        actions.addAll(Actions.getRepositoryPopupActions(repositoryNodes));
        return actions.toArray(new Action[actions.size()]);
    }

    private Action getRepositoryAction(RepositoryNode... repositoryNodes) {
        boolean allOpened = true;
        boolean allClosed = true;
        for (RepositoryNode repositoryNode : repositoryNodes) {
            if (repositoryNode.isOpened()) {
                allClosed = false;
            } else {
                allOpened = false;
            }
        }
        if (allOpened) {
            if (closeRepositoryAction == null) {
                closeRepositoryAction = new CloseRepositoryNodeAction(this);
            }
            return closeRepositoryAction;
        } else if (allClosed) {
            if (openRepositoryAction == null) {
                openRepositoryAction = new OpenRepositoryNodeAction(this);
            }
            return openRepositoryAction;
        }
        return null;
    }

    public List<QueryNode> getQueryNodes() {
        return queryNodes;
    }

    public List<QueryNode> getFilteredQueryNodes() {
        return filteredQueryNodes;
    }

    public void setFilteredQueryNodes(List<QueryNode> filteredQueryNodes) {
        this.filteredQueryNodes = filteredQueryNodes;
    }

    public int getFilterHits() {
        int hits = 0;
        for (QueryNode queryNode : filteredQueryNodes) {
            hits += queryNode.getTotalTaskCount();
        }
        return hits;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RepositoryNode other = (RepositoryNode) obj;
        return repository.getDisplayName().equalsIgnoreCase(other.repository.getDisplayName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.repository != null ? this.repository.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(RepositoryNode toCompare) {
        if (this.isOpened() != toCompare.isOpened()) {
            return this.isOpened() ? -1 : 1;
        } else {
            return repository.getDisplayName().compareToIgnoreCase(toCompare.repository.getDisplayName());
        }
    }

    @Override
    public String toString() {
        return repository.getDisplayName();
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Repository.EVENT_QUERY_LIST_CHANGED)) {
            updateContent();
        } else if (evt.getPropertyName().equals(Repository.EVENT_ATTRIBUTES_CHANGED)) {
            if (evt.getNewValue() instanceof Map) {
                Map<String, String> attributes = (Map<String, String>) evt.getNewValue();
                String displayName = attributes.get(Repository.ATTRIBUTE_DISPLAY_NAME);
                if (displayName != null && !displayName.isEmpty()) {
                    if (lblName != null) {
                        lblName.setText(displayName);
                        fireContentChanged();
                    }
                }
            }
        }
    }

    void updateContent() {
        updateNodes();
        refreshChildren();
    }

    Collection<Query> getQueries() {
        return repository.getQueries();
    }

    ImageIcon getIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/remote_repo.png", true);
    }

    public void refreshContent() {
        refresh = true;
        if (!isExpanded()) {
            setExpanded(true);
        }
        updateContent();
    }

    private LinkButton getEmptyContentLink() {
        if (btnEmtpyContent == null) {
            btnEmtpyContent = new LinkButton(NbBundle.getMessage(RepositoryNode.class, "LBL_EmptyRepositoryContent"), //NOI18N
                    ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/search_repo.png", true), //NOI18N
                    new SearchRepositoryAction(this)); //NOI18N
            btnEmtpyContent.setToolTipText(NbBundle.getMessage(RepositoryNode.class, "LBL_SearchInRepo")); //NOI18N
        }
        return btnEmtpyContent;
    }
}

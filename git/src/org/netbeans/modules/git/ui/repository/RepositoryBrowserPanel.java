/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.repository;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.modules.git.GitRepositories;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
public class RepositoryBrowserPanel extends JPanel implements Provider, PropertyChangeListener {

    AbstractNode root;
    private static final RequestProcessor RP = new RequestProcessor("RepositoryPanel", 1); //NOI18N
    private static final Logger LOG = Logger.getLogger(RepositoryBrowserPanel.class.getName());
    private final ExplorerManager manager;
    private final EnumSet<Option> options;
    private String currRevision = null;
    public static final String PROP_REVISION_CHANGED = "RepositoryBrowserPanel.revision"; //NOI18N

    public static enum Option {
        DISPLAY_ALL_REPOSITORIES,
        DISPLAY_BRANCHES_LOCAL,
        DISPLAY_BRANCHES_REMOTE,
        DISPLAY_TAGS,
        DISPLAY_TOOLBAR,
        ENABLE_POPUP
    }

    public static final EnumSet<Option> OPTIONS_INSIDE_PANEL = EnumSet.of(Option.DISPLAY_BRANCHES_LOCAL,
            Option.DISPLAY_BRANCHES_REMOTE,
            Option.DISPLAY_TAGS);

    public RepositoryBrowserPanel () {
        this(EnumSet.allOf(Option.class), null, null);
    }

    public RepositoryBrowserPanel (EnumSet<Option> options, File repository, RepositoryInfo info) {
        this.root = options.contains(Option.DISPLAY_ALL_REPOSITORIES) ? new AbstractNode(new RepositoriesChildren()) : new RepositoryNode(repository, info);
        this.manager = new ExplorerManager();
        this.options = options;
        initComponents();
        if (!options.contains(Option.DISPLAY_TOOLBAR)) {
            toolbar.setVisible(false);
        }
        tree.setRootVisible(false);
        tree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }

    @Override
    public ExplorerManager getExplorerManager () {
        return manager;
    }

    @Override
    public void addNotify () {
        super.addNotify();
        getExplorerManager().setRootContext(root);
        getExplorerManager().addPropertyChangeListener(this);
        if (toolbar.isVisible()) {
            attachToolbarListeners();
        }
    }

    @Override
    public void removeNotify() {
        getExplorerManager().removePropertyChangeListener(this);
        if (toolbar.isVisible()) {
            detachToolbarListeners();
        }
        super.removeNotify();
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == ExplorerManager.PROP_SELECTED_NODES) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc != null) {
                tc.setActivatedNodes(getExplorerManager().getSelectedNodes());
            }
            
            String oldRevision = currRevision;
            currRevision = null;
            if (getExplorerManager().getSelectedNodes().length == 1) {
                Node selectedNode = getExplorerManager().getSelectedNodes()[0];
                Revision rev = selectedNode.getLookup().lookup(Revision.class);
                if (rev != null) {
                    currRevision = rev.getRevision();
                }
            }
            if (currRevision != null || oldRevision != null) {
                firePropertyChange(PROP_REVISION_CHANGED, oldRevision, currRevision);
            }
        }
    }

    public void selectRepository (File repository) {
        Node[] nodes = root.getChildren().getNodes();
        for (Node node : nodes) {
            if (node instanceof RepositoryNode && repository.equals(node.getLookup().lookup(File.class))) {
                tree.expandNode(node);
                try {
                    getExplorerManager().setSelectedNodes(new Node[] { node });
                } catch (PropertyVetoException ex) {
                }
                break;
            }
        }
    }

    private void attachToolbarListeners () {

    }

    private void detachToolbarListeners () {

    }

    private abstract class RepositoryBrowserNode extends AbstractNode {

        protected RepositoryBrowserNode (Children children) {
            this(children, null);
        }

        protected RepositoryBrowserNode (Children children, Lookup lookup) {
            super(children, lookup);
        }

        @Override
        public final Action[] getActions (boolean context) {
            return options.contains(Option.ENABLE_POPUP) ? getPopupActions(context) : RepositoryBrowserNode.this.getPopupActions(context);
        }

        protected Action[] getPopupActions (boolean context) {
            return new Action[0];
        }

    }

    private class RepositoriesChildren extends Children.SortedMap<File> {
        private final PropertyChangeListener list;
        private boolean initialized = false;

        public RepositoriesChildren () {
            setComparator(new Comparator<Node>() {
                @Override
                public int compare (Node o1, Node o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            GitRepositories.getInstance().addPropertyChangeListener(WeakListeners.propertyChange(list = new PropertyChangeListener() {
                @Override
                @SuppressWarnings("unchecked")
                public void propertyChange (PropertyChangeEvent evt) {
                    final Set<File> oldValues = (Set<File>) evt.getOldValue();
                    final Set<File> newValues = (Set<File>) evt.getNewValue();
                    if (oldValues.size() > newValues.size()) {
                        oldValues.removeAll(newValues);
                        removeAll(oldValues);
                    } else if (oldValues.size() < newValues.size()) {
                        newValues.removeAll(oldValues);
                        RP.post(new Runnable () {
                            @Override
                            public void run () {
                                java.util.Map<File, RepositoryNode> nodes = new HashMap<File, RepositoryNode>();
                                for (File r : newValues) {
                                    nodes.put(r, new RepositoryNode(r, RepositoryInfo.getInstance(r)));
                                }
                                putAll(nodes);
                            }
                        });
                    }
                }
            }, GitRepositories.getInstance()));
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            if (!initialized) {
                // initialize keys
                initialized = true;
                list.propertyChange(new PropertyChangeEvent(GitRepositories.getInstance(), GitRepositories.PROP_REPOSITORIES, Collections.<File>emptySet(), GitRepositories.getInstance().getKnownRepositories()));
            }
        }
    }

    private class RepositoryNode extends RepositoryBrowserNode implements PropertyChangeListener {
        private PropertyChangeListener list;

        public RepositoryNode (final File file, RepositoryInfo info) {
            super(new RepositoryChildren(), Lookups.fixed(file));

            if (info == null) {
                setDisplayName(file.getName());
                RP.post(new Runnable () {
                    @Override
                    public void run () {
                        RepositoryInfo info = RepositoryInfo.getInstance(file);
                        setName(info);
                        info.addPropertyChangeListener(list = WeakListeners.propertyChange(RepositoryNode.this, info));
                    }
                });
            } else {
                setName(info);
                info.addPropertyChangeListener(list = WeakListeners.propertyChange(this, info));
            }
        }

        private void setName (RepositoryInfo info) {
            String annotation;
            String branchLabel = ""; //NOI18N
            GitBranch branch = info.getActiveBranch();
            if (branch != null) {
                branchLabel = branch.getName();
                if (branchLabel == GitBranch.NO_BRANCH) { // do not use equals
                    // not on a branch, show also commit id
                    branchLabel += " " + branch.getId(); // NOI18N
                }
            }
            GitRepositoryState repositoryState = info.getRepositoryState();
            if (repositoryState != GitRepositoryState.SAFE) {
                annotation = repositoryState.toString() + " - " + branchLabel; //NOI18N
            } else {
                annotation = branchLabel;
            }
            setDisplayName(info.getName() + " [" + annotation + "]");
        }

        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            if (evt.getSource() instanceof RepositoryInfo) {
                setName((RepositoryInfo) evt.getSource());
            }
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    private class RepositoryChildren extends Children.Keys<AbstractNode> {

        boolean initialized = false;

        @Override
        protected void addNotify () {
            super.addNotify();
            if (!initialized) {
                initialized = true;
                List<AbstractNode> keys = new LinkedList<AbstractNode>();
                if (options.contains(Option.DISPLAY_BRANCHES_LOCAL) || options.contains(Option.DISPLAY_BRANCHES_REMOTE)) {
                    keys.add(new BranchesTopNode(getNode().getLookup().lookup(File.class)));
                }
                if (options.contains(Option.DISPLAY_TAGS)) {
                    keys.add(new TagsNode(getNode().getLookup().lookup(File.class)));
                }
                setKeys(keys);
            }
        }

        @Override
        protected void removeNotify () {
            setKeys(Collections.<AbstractNode>emptySet());
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes (AbstractNode key) {
            return new Node[] { key };
        }

    }

    //<editor-fold defaultstate="collapsed" desc="branches">
    private class BranchesTopNode extends RepositoryBrowserNode {

        public BranchesTopNode (File repository) {
            super(new BranchesTopChildren(repository));
        }

        @Override
        public String getDisplayName () {
            return getName();
        }

        @Override
        public String getName () {
            return NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_RepositoryPanel.BranchesNode.name"); //NOI18N
        }

        @Override
        public Action[] getPopupActions (boolean context) {
            return new Action[] {
                new AbstractAction(NbBundle.getMessage(BranchesTopNode.class, "LBL_RepositoryPanel.RefreshBranchesAction.name")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        ((BranchesTopChildren) getChildren()).refreshBranches();
                    }
                }
            };
        }
    }

    private static enum BranchNodeType {
        LOCAL {
            @Override
            public String toString () {
                return NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_RepositoryPanel.BranchesChildren.LocalNode.name"); //NOI18N
            }
        },
        REMOTE {
            @Override
            public String toString () {
                return NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_RepositoryPanel.BranchesChildren.RemoteNode.name"); //NOI18N
            }
        }
    }

    private class BranchesTopChildren extends Children.Keys<BranchNodeType> {
        private final File repository;
        private java.util.Map<String, GitBranch> branches = new TreeMap<String, GitBranch>();
        private BranchesNode local, remote;

        private BranchesTopChildren (File repository) {
            this.repository = repository;
        }

        @Override
        protected void addNotify () {
            super.addNotify();
            List<BranchNodeType> keys = new LinkedList<BranchNodeType>();
            if (options.contains(Option.DISPLAY_BRANCHES_LOCAL)) {
                keys.add(BranchNodeType.LOCAL);
            }
            if (options.contains(Option.DISPLAY_BRANCHES_REMOTE)) {
                keys.add(BranchNodeType.REMOTE);
            }
            setKeys(keys);
            refreshBranches();
        }

        @Override
        protected void removeNotify () {
            setKeys(Collections.<BranchNodeType>emptySet());
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes (BranchNodeType key) {
            BranchesNode node;
            switch (key) {
                case LOCAL:
                    node = local = new BranchesNode(key, branches);
                    break;
                case REMOTE:
                    node = remote = new BranchesNode(key, branches);
                    break;
                default:
                    throw new IllegalStateException();
            }
            return new Node[] { node };
        }

        private void refreshBranches () {
            new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        final java.util.Map<String, GitBranch> branches = client.getBranches(true, this);
                        if (!isCanceled()) {
                            if (branches.isEmpty()) {
                                BranchesTopChildren.this.branches.clear();
                            } else {
                                BranchesTopChildren.this.branches.keySet().retainAll(branches.keySet());
                                branches.keySet().removeAll(BranchesTopChildren.this.branches.keySet());
                                BranchesTopChildren.this.branches.putAll(branches);
                            }
                            if (local != null) {
                                local.refresh();
                            }
                            if (remote != null) {
                                remote.refresh();
                            }
                        }
                    } catch (GitException ex) {
                        LOG.log(Level.INFO, "refreshBranches()", ex); //NOI18N
                    }
                }
            }.start(RP, repository, NbBundle.getMessage(BranchesTopChildren.class, "MSG_RepositoryPanel.refreshingBranches")); //NOI18N
        }

    }

    private class BranchesNode extends RepositoryBrowserNode {
        private final BranchNodeType type;

        private BranchesNode (BranchNodeType type, Map<String, GitBranch> branches) {
            super(new BranchesChildren(type, branches));
            this.type = type;
        }

        private void refresh () {
            ((BranchesChildren) getChildren()).refreshKeys();
        }

        @Override
        public String getName () {
            return type.toString();
        }

        @Override
        public String getDisplayName () {
            return getName();
        }
    }

    private class BranchesChildren extends Children.Keys<GitBranch> {
        private final BranchNodeType type;
        private final java.util.Map<String, GitBranch> branches;

        private BranchesChildren (BranchNodeType type, java.util.Map<String, GitBranch> branches) {
            this.type = type;
            this.branches = branches;
        }

        @Override
        protected void addNotify () {
            super.addNotify();
            RP.post(new Runnable () {
                @Override
                public void run () {
                    refreshKeys();
                }
            });
        }

        @Override
        protected Node[] createNodes (GitBranch key) {
            return new Node[] { new BranchNode(key) };
        }

        private void refreshKeys () {
            List<GitBranch> keys = new LinkedList<GitBranch>();
            for (java.util.Map.Entry<String, GitBranch> e : branches.entrySet()) {
                GitBranch branch = e.getValue();
                if (type == BranchNodeType.REMOTE && branch.isRemote() || type == BranchNodeType.LOCAL && !branch.isRemote()) {
                    keys.add(branch);
                }
            }
            setKeys(keys);
        }
    }

    private class BranchNode extends RepositoryBrowserNode {
        private final GitBranch branch;

        public BranchNode (GitBranch branch) {
            super(Children.LEAF, Lookups.fixed(new Revision(branch.getId())));
            this.branch = branch;
        }

        @Override
        public String getDisplayName () {
            return getName();
        }

        @Override
        public String getName () {
            return branch.getName() + " - " + branch.getId();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="tags">
    private class TagsNode extends RepositoryBrowserNode {

        public TagsNode (File repository) {
            super(Children.LEAF);
            assert repository != null;
        }

        @Override
        public String getDisplayName () {
            return getName();
        }

        @Override
        public String getName () {
            return NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_RepositoryPanel.TagsNode.name"); //NOI18N
        }
    }
    //</editor-fold>

    private static class Revision {
        final String revision;

        public Revision (String revision) {
            this.revision = revision;
        }

        public String getRevision () {
            return revision;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
        add(toolbar, java.awt.BorderLayout.PAGE_START);

        tree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        add(tree, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final org.netbeans.modules.git.ui.repository.ControlToolbar toolbar = new org.netbeans.modules.git.ui.repository.ControlToolbar();
    private final org.openide.explorer.view.BeanTreeView tree = new org.openide.explorer.view.BeanTreeView();
    // End of variables declaration//GEN-END:variables

}

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
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
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitRepositories;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.branch.CreateBranchAction;
import org.netbeans.modules.git.ui.branch.DeleteBranchAction;
import org.netbeans.modules.git.ui.checkout.CheckoutRevisionAction;
import org.netbeans.modules.git.ui.fetch.FetchAction;
import org.netbeans.modules.git.ui.merge.MergeRevisionAction;
import org.netbeans.modules.git.ui.repository.remote.RemoveRemoteConfig;
import org.netbeans.modules.git.ui.tag.CreateTagAction;
import org.netbeans.modules.git.ui.tag.ManageTagsAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
public class RepositoryBrowserPanel extends JPanel implements Provider, PropertyChangeListener, ListSelectionListener,
        MouseListener {

    AbstractNode root;
    private static final RequestProcessor RP = new RequestProcessor("RepositoryPanel", 1); //NOI18N
    private static final Logger LOG = Logger.getLogger(RepositoryBrowserPanel.class.getName());
    private final ExplorerManager manager;
    private final EnumSet<Option> options;
    private Revision currRevision;
    private File currRepository;
    public static final String PROP_REVISION_CHANGED = "RepositoryBrowserPanel.revision"; //NOI18N
    /**
     * Fired when user dbl-clicks or accepts a revision in any other way.
     * A controller displaying the panel may answer accordingly and close the dialog.
     * {@link PropertyChangeEvent#getNewValue()} contains the value of the accepted revision.
     */
    public static final String PROP_REVISION_ACCEPTED = "RepositoryBrowserPanel.acceptedRevision"; //NOI18N
    private final File[] roots;
    private String branchMergeWith;

    public static enum Option {
        DISPLAY_ALL_REPOSITORIES,
        DISPLAY_BRANCHES_LOCAL,
        DISPLAY_BRANCHES_REMOTE,
        DISPLAY_COMMIT_IDS,
        DISPLAY_REMOTES,
        DISPLAY_REVISIONS,
        DISPLAY_TAGS,
        DISPLAY_TOOLBAR,
        ENABLE_POPUP
    }

    public static final EnumSet<Option> OPTIONS_INSIDE_PANEL = EnumSet.of(Option.DISPLAY_BRANCHES_LOCAL,
            Option.DISPLAY_BRANCHES_REMOTE,
            Option.DISPLAY_REVISIONS,
            Option.DISPLAY_TAGS);

    public RepositoryBrowserPanel () {
        this(EnumSet.complementOf(EnumSet.of(Option.DISPLAY_REVISIONS)), null, new File[0], null);
    }

    public RepositoryBrowserPanel (EnumSet<Option> options, File repository, File[] roots, RepositoryInfo info) {
        Parameters.notNull("roots", roots);
        this.currRepository = repository;
        this.root = options.contains(Option.DISPLAY_ALL_REPOSITORIES) ? new AbstractNode(new RepositoriesChildren()) : new RepositoryNode(repository, info);
        this.manager = new ExplorerManager();
        this.options = options;
        this.roots = roots;
        initComponents();
        if (!options.contains(Option.DISPLAY_TOOLBAR)) {
            toolbar.setVisible(false);
        }
        tree.setRootVisible(false);
        tree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        if (!options.contains(Option.DISPLAY_REVISIONS)) {
            remove(jSplitPane1);
            add(tree, BorderLayout.CENTER);
        }
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
        revisionsPanel1.lstRevisions.addListSelectionListener(this);
        revisionsPanel1.lstRevisions.addMouseListener(this);
        if (options.contains(Option.DISPLAY_REVISIONS)) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
            revisionsPanel1.updateHistory(currRepository, roots, currRevision);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int width = revisionsPanel1.getPreferredSize().width;
                    int leftPanelWidth = jSplitPane1.getPreferredSize().width - width;
                    jSplitPane1.setDividerLocation(Math.min(200, leftPanelWidth));
                }
            });
        }
    }

    @Override
    public void removeNotify() {
        if (options.contains(Option.DISPLAY_REVISIONS)) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(this);
        }
        revisionsPanel1.lstRevisions.removeListSelectionListener(this);
        getExplorerManager().removePropertyChangeListener(this);
        if (toolbar.isVisible()) {
            detachToolbarListeners();
        }
        super.removeNotify();
    }

    @Override
    public void mouseClicked (MouseEvent e) {
        if (e.getSource() == revisionsPanel1.lstRevisions) {
            if (e.getClickCount() == 2 && currRevision != null) {
                e.consume();
                firePropertyChange(PROP_REVISION_ACCEPTED, null, currRevision);
            }
        }
    }

    @Override
    public void mousePressed (MouseEvent e) {
    }

    @Override
    public void mouseReleased (MouseEvent e) {
    }

    @Override
    public void mouseEntered (MouseEvent e) {
    }

    @Override
    public void mouseExited (MouseEvent e) {
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == ExplorerManager.PROP_SELECTED_NODES) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc != null) {
                tc.setActivatedNodes(getExplorerManager().getSelectedNodes());
            }
            
            currRepository = null;
            Revision oldRevision = currRevision;
            currRevision = null;
            if (getExplorerManager().getSelectedNodes().length == 1) {
                Node selectedNode = getExplorerManager().getSelectedNodes()[0];
                currRevision = selectedNode.getLookup().lookup(Revision.class);
                currRepository = lookupRepository(selectedNode);
            }
            if ((currRevision != null || oldRevision != null) && !(currRevision != null && oldRevision != null 
                    && currRevision.equals(oldRevision))) {
                firePropertyChange(PROP_REVISION_CHANGED, oldRevision, currRevision);
            }
            if (options.contains(Option.DISPLAY_REVISIONS) && currRevision != null) {
                revisionsPanel1.updateHistory(currRepository, roots, currRevision);
            }
        } else if (options.contains(Option.DISPLAY_REVISIONS) && "focusOwner".equals(evt.getPropertyName())) {
            Component compNew = (Component) evt.getNewValue();
            if (compNew != null) {
                if (SwingUtilities.getAncestorOfClass(tree.getClass(), compNew) != null) {
                    if (getExplorerManager().getSelectedNodes().length == 1) {
                        propertyChange(new PropertyChangeEvent(tree, ExplorerManager.PROP_SELECTED_NODES, getExplorerManager().getSelectedNodes(), getExplorerManager().getSelectedNodes()));
                    }
                } else if (revisionsPanel1.lstRevisions == compNew) {
                    int selection = revisionsPanel1.lstRevisions.getSelectedIndex();
                    if (selection != -1) {
                        valueChanged(new ListSelectionEvent(revisionsPanel1.lstRevisions, selection, selection, false));
                    }
                }
            }
        }
    }

    private File lookupRepository (Node selectedNode) {
        // there should ALWAYS be a repository node somewhere in the root
        while (!(selectedNode instanceof RepositoryNode) && selectedNode != null) {
            selectedNode = selectedNode.getParentNode();
        }
        return selectedNode == null ? null : ((RepositoryNode) selectedNode).getRepository();
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

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && revisionsPanel1.lstRevisions.isFocusOwner()) {
            GitRevisionInfo selectedRevision = revisionsPanel1.getSelectedRevision();
            Revision oldRevision = currRevision;
            if (selectedRevision == null && currRevision != null) {
                currRevision = null;
                firePropertyChange(PROP_REVISION_CHANGED, oldRevision, currRevision);
            } else if (selectedRevision != null) {
                Revision newRev = new Revision(selectedRevision.getRevision(), selectedRevision.getRevision(),
                        selectedRevision.getShortMessage());
                if (!newRev.equals(oldRevision)) {
                    currRevision = newRev;
                    firePropertyChange(PROP_REVISION_CHANGED, oldRevision, currRevision);
                }
            }
        }
    }

    void displayBrancheMergedStatus (String revision) {
        this.branchMergeWith = revision;
    }

    private static final HashMap<String, Image> cachedIcons = new HashMap<String, Image>(2);
    private abstract class RepositoryBrowserNode extends AbstractNode {
        
        protected RepositoryBrowserNode (Children children, File repository) {
            this(children, repository, null);
        }

        protected RepositoryBrowserNode (Children children, File repository, Lookup lookup) {
            super(children, lookup == null ? Lookups.singleton(repository) : new ProxyLookup(Lookups.singleton(repository), lookup));
        }

        @Override
        public final Action[] getActions (boolean context) {
            return options.contains(Option.ENABLE_POPUP) ? getPopupActions(context) : RepositoryBrowserNode.this.getPopupActions(context);
        }

        protected Action[] getPopupActions (boolean context) {
            return new Action[0];
        }
        
        protected Image getFolderIcon (int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                img = findIcon("Nb.Explorer.Folder.icon", "Tree.closedIcon"); //NOI18N
            }
            if (img == null) {
                img = super.getIcon(type);
            }
            return img;
        }

        protected Image getOpenedFolderIcon (int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                img = findIcon("Nb.Explorer.Folder.openedIcon", "Tree.openIcon"); //NOI18N
            }
            if (img == null) {
                img = super.getOpenedIcon(type);
            }
            return img;
        }

        private Image findIcon (String key1, String key2) {
            Image img = cachedIcons.containsKey(key1) ? cachedIcons.get(key1) : null;
            if (img == null) {
                img = findIcon(key1);
                if (img == null) {
                    img = findIcon(key2);
                }
                cachedIcons.put(key1, img);
            }
            return img;
        }

        private Image findIcon (String key) {
            Object obj = UIManager.get(key);
            if (obj instanceof Image) {
                return (Image)obj;
            }

            if (obj instanceof Icon) {
                Icon icon = (Icon)obj;
                return ImageUtilities.icon2Image(icon);
            }

            return null;
        }

    }

    private class RepositoriesChildren extends Children.SortedMap<File> {
        private final PropertyChangeListener list;
        private boolean initialized = false;

        public RepositoriesChildren () {
            setComparator(new Comparator<Node>() {
                @Override
                public int compare (Node o1, Node o2) {
                    int result;
                    if (o1 instanceof RepositoryNode && o2 instanceof RepositoryNode) {
                        File repo1 = ((RepositoryNode) o1).getRepository();
                        File repo2 = ((RepositoryNode) o2).getRepository();
                        result = repo1.getName().compareTo(repo2.getName());
                        if (result == 0 && !repo1.equals(repo2)) {
                            result = repo1.getAbsolutePath().compareTo(repo2.getAbsolutePath());
                        }
                    } else {
                        result = o1.toString().compareTo(o2.toString());
                    }
                    return result;
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
                                    RepositoryInfo info = RepositoryInfo.getInstance(r);
                                    if (info == null) {
                                        LOG.log(Level.INFO, "RepositoriesChildren.propertyChange() : Null info for {0}", r); //NOI18N
                                    } else {
                                        nodes.put(r, new RepositoryNode(r, info));
                                    }
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
        private final File repository;

        public RepositoryNode (final File repository, RepositoryInfo info) {
            super(new RepositoryChildren(), repository);
            this.repository = repository;
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/repository.png"); //NOI18N
            if (info == null) {
                setDisplayName(repository.getName());
                RP.post(new Runnable () {
                    @Override
                    public void run () {
                        RepositoryInfo info = RepositoryInfo.getInstance(repository);
                        if (info == null) {
                            LOG.log(Level.INFO, "RepositoryNode() : Null info for {0}", repository); //NOI18N
                        } else {
                            setName(info);
                            info.addPropertyChangeListener(list = WeakListeners.propertyChange(RepositoryNode.this, info));
                        }
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
                    Map<String, GitTag> tags = info.getTags();
                    StringBuilder tagLabel = new StringBuilder(); //NOI18N
                    for (GitTag tag : tags.values()) {
                        if (tag.getTaggedObjectId().equals(branch.getId())) {
                            tagLabel.append(",").append(tag.getTagName());
                        }
                    }
                    if (tagLabel.length() <= 1) {
                        // not on a branch or tag, show at least part of commit id
                        branchLabel = branch.getId();
                        if (branchLabel.length() > 7) {
                            branchLabel = branchLabel.substring(0, 7) + "..."; //NOI18N
                        }
                    } else {
                        tagLabel.delete(0, 1);
                        branchLabel = tagLabel.toString();
                    }
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

        public File getRepository() {
            return repository;
        }

        @Override
        protected Action[] getPopupActions (boolean context) {
            return new Action[] {
                SystemAction.get(FetchAction.class)
            };
        }

        @Override
        public String getShortDescription () {
            return repository.getAbsolutePath();
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
                    keys.add(new BranchesTopNode(((RepositoryNode) getNode()).getRepository()));
                }
                if (options.contains(Option.DISPLAY_TAGS)) {
                    keys.add(new TagsNode(((RepositoryNode) getNode()).getRepository()));
                }
                if (options.contains(Option.DISPLAY_REMOTES)) {
                    keys.add(new RemotesNode(((RepositoryNode) getNode()).getRepository()));
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
            super(new BranchesTopChildren(repository), repository);
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/branches.png"); //NOI18N
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

    private static class GitBranchInfo {
        private final GitBranch branch;
        private final Boolean mergedStatus;

        public GitBranchInfo (GitBranch branch, Boolean mergedStatus) {
            this.branch = branch;
            this.mergedStatus = mergedStatus;
        }
    }
    
    private class BranchesTopChildren extends Children.Keys<BranchNodeType> implements PropertyChangeListener {
        private final File repository;
        private java.util.Map<String, GitBranchInfo> branches = new TreeMap<String, GitBranchInfo>();
        private BranchesNode local, remote;

        private BranchesTopChildren (File repository) {
            this.repository = repository;
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            if (info == null) {
                LOG.log(Level.INFO, "BranchesTopChildren() : Null info for {0}", repository); //NOI18N
            } else {
                info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
            }
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
                    node = local = new BranchesNode(repository, key, branches);
                    break;
                case REMOTE:
                    node = remote = new BranchesNode(repository, key, branches);
                    break;
                default:
                    throw new IllegalStateException();
            }
            return new Node[] { node };
        }

        private void refreshBranches () {
            new GitProgressSupport.NoOutputLogging() {
                @Override
                protected void perform () {
                    RepositoryInfo info = RepositoryInfo.getInstance(repository);
                    info.refresh();
                    java.util.Map<String, GitBranch> branches = info.getBranches();
                    if (!isCanceled()) {
                        refreshBranches(branches);
                    }
                }
            }.start(RP, repository, NbBundle.getMessage(BranchesTopChildren.class, "MSG_RepositoryPanel.refreshingBranches")); //NOI18N
        }
        
        private void refreshBranches (java.util.Map<String, GitBranch> branches) {
            assert !EventQueue.isDispatchThread();
            if (branches.isEmpty()) {
                BranchesTopChildren.this.branches.clear();
            } else {
                branches = new java.util.HashMap<String, GitBranch>(branches);
                BranchesTopChildren.this.branches.keySet().retainAll(branches.keySet());
                for (java.util.Map.Entry<String, GitBranchInfo> e : BranchesTopChildren.this.branches.entrySet()) {
                    GitBranch newBranchInfo = branches.get(e.getKey());
                    // do not refresh branches that don't change their active state or head id
                    if (newBranchInfo != null && (newBranchInfo.getId().equals(e.getValue().branch.getId()) 
                            && newBranchInfo.isActive() == e.getValue().branch.isActive())) {
                        branches.remove(e.getKey());
                    }
                }
                GitClient client = null;
                try {
                    if (branchMergeWith != null) {
                        client = Git.getInstance().getClient(repository);
                    }
                    for (java.util.Map.Entry<String, GitBranch> e : branches.entrySet()) {
                        Boolean mergedStatus = null;
                        if (branchMergeWith != null) {
                            GitRevisionInfo commonAncestor = client.getCommonAncestor(new String[] { branchMergeWith, e.getValue().getId()}, GitUtils.NULL_PROGRESS_MONITOR);
                            mergedStatus = commonAncestor.getRevision().equals(e.getValue().getId());
                        }
                        BranchesTopChildren.this.branches.put(e.getKey(), new GitBranchInfo(e.getValue(), mergedStatus));
                    }
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                } finally {
                    if (client != null) {
                        client.release();
                    }
                }
            }
            if (local != null) {
                local.refresh();
            }
            if (remote != null) {
                remote.refresh();
            }
        }

        @Override
        public void propertyChange (final PropertyChangeEvent evt) {
            if (RepositoryInfo.PROPERTY_BRANCHES.equals(evt.getPropertyName())) {
                RP.post(new Runnable() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void run () {
                        refreshBranches((java.util.Map<String, GitBranch>) evt.getNewValue());
                    }
                });
            }
        }
    }

    private class BranchesNode extends RepositoryBrowserNode {
        private final BranchNodeType type;

        private BranchesNode (File repository, BranchNodeType type, Map<String, GitBranchInfo> branches) {
            super(new BranchesChildren(type, branches), repository);
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

        @Override
        public Image getIcon (int type) {
            return getFolderIcon(type);
        }

        @Override
        public Image getOpenedIcon (int type) {
            return getOpenedFolderIcon(type);
        }
    }

    private class BranchesChildren extends Children.Keys<GitBranchInfo> {
        private final BranchNodeType type;
        private final java.util.Map<String, GitBranchInfo> branches;

        private BranchesChildren (BranchNodeType type, java.util.Map<String, GitBranchInfo> branches) {
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
        protected Node[] createNodes (GitBranchInfo key) {
            Node node = getNode();
            while (!(node instanceof RepositoryNode)) {
                node = node.getParentNode();
            }
            File repository = ((RepositoryNode) node).getRepository();
            return new Node[] { new BranchNode(repository, key) };
        }

        private void refreshKeys () {
            List<GitBranchInfo> keys = new LinkedList<GitBranchInfo>();
            for (java.util.Map.Entry<String, GitBranchInfo> e : branches.entrySet()) {
                GitBranchInfo branchInfo = e.getValue();
                if (type == BranchNodeType.REMOTE && branchInfo.branch.isRemote() 
                        || type == BranchNodeType.LOCAL && !branchInfo.branch.isRemote()) {
                    keys.add(branchInfo);
                }
            }
            if (branchMergeWith != null) {
                keys = new ArrayList<GitBranchInfo>(keys);
                Collections.sort(keys, new Comparator<GitBranchInfo>() {
                    @Override
                    public int compare (GitBranchInfo i1, GitBranchInfo i2) {
                        assert i1.mergedStatus != null;
                        assert i2.mergedStatus != null;
                        int res = i1.mergedStatus.compareTo(i2.mergedStatus);
                        if (res == 0) {
                            res = i1.branch.getName().compareToIgnoreCase(i2.branch.getName());
                        }
                        return res;
                    }
                });
            }
            setKeys(keys);
        }
    }

    private class BranchNode extends RepositoryBrowserNode {
        private PropertyChangeListener list;
        private boolean active;
        private final String branchName;
        private String branchId;
        private final GitBranch trackedBranch;
        private String lastTrackingMyId;
        private String lastTrackingOtherId;
        private final Boolean mergeStatus;

        public BranchNode (File repository, GitBranchInfo branchInfo) {
            super(Children.LEAF, repository, Lookups.singleton(new Revision.BranchReference(branchInfo.branch)));
            GitBranch branch = branchInfo.branch;
            branchName = branch.getName();
            mergeStatus = branchInfo.mergedStatus;
            branchId = branch.getId();
            trackedBranch = branch.getTrackedBranch();
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/branch.png"); //NOI18N
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            if (info == null) {
                LOG.log(Level.INFO, "BranchNode() : Null info for {0}", repository); //NOI18N
            } else {
                info.addPropertyChangeListener(WeakListeners.propertyChange(list = new PropertyChangeListener() {
                    @Override
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (RepositoryInfo.PROPERTY_ACTIVE_BRANCH.equals(evt.getPropertyName()) || RepositoryInfo.PROPERTY_HEAD.equals(evt.getPropertyName())) {
                            refreshActiveBranch((GitBranch) evt.getNewValue());
                        }
                    }
                }, info));
                refreshActiveBranch(info.getActiveBranch());
            }
            refreshTracking(branch.getTrackedBranch(), repository);
        }

        @Override
        public String getDisplayName () {
            return getName(false);
        }

        @Override
        public String getHtmlDisplayName() {
            return getName(true);
        }

        @Override
        public String getName() {
            return getName(false);
        }
        
        public String getName (boolean html) {
            StringBuilder sb = new StringBuilder();
            if (active && html) {
                sb.append("<html><strong>").append(branchName).append("</strong>"); //NOI18N
            } else {
                sb.append(branchName).append(getMergeStatus(mergeStatus));
            }
            if (options.contains(Option.DISPLAY_COMMIT_IDS)) {
                if (trackedBranch != null) {
                    sb.append(NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_BranchNode.basedOn", trackedBranch.getName())); //NOI18N
                }
                sb.append(" - ").append(branchId); //NOI18N
            }
            return sb.toString();
        }

        private void refreshActiveBranch (GitBranch activeBranch) {
            String oldHtmlName = getHtmlDisplayName();
            boolean oldActive = active;
            if (activeBranch.getName().equals(branchName)) {
                active = true;
                this.branchId = activeBranch.getId();
                refreshTracking(activeBranch.getTrackedBranch(), lookupRepository(this));
            } else {
                active = false;
            }
            if (active != oldActive) {
                setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/" + (active ? "active_branch" : "branch") + ".png"); //NOI18N
            }
            String newHtmlName = getHtmlDisplayName();
            if (!oldHtmlName.equals(newHtmlName)) {
                fireDisplayNameChange(null, null);
            }
        }

        @Override
        protected Action[] getPopupActions (boolean context) {
            List<Action> actions = new LinkedList<Action>();
            if (currRepository != null && branchName != null) {
                final File repo = currRepository;
                final String branch = branchName;
                actions.add(new AbstractAction(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Utils.postParallel(new Runnable () {
                            @Override
                            public void run() {
                                CheckoutRevisionAction action = SystemAction.get(CheckoutRevisionAction.class);
                                action.checkoutRevision(repo, branch);
                            }
                        }, 0);
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(CreateBranchAction.class, "LBL_CreateBranchAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Utils.postParallel(new Runnable () {
                            @Override
                            public void run() {
                                CreateBranchAction action = SystemAction.get(CreateBranchAction.class);
                                action.createBranch(repo, branch);
                            }
                        }, 0);
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(CreateTagAction.class, "LBL_CreateTagAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        CreateTagAction action = SystemAction.get(CreateTagAction.class);
                        action.createTag(repo, branch);
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Utils.postParallel(new Runnable () {
                            @Override
                            public void run() {
                                MergeRevisionAction action = SystemAction.get(MergeRevisionAction.class);
                                action.mergeRevision(repo, branch);
                            }
                        }, 0);
                    }

                    @Override
                    public boolean isEnabled() {
                        return !active;
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        DeleteBranchAction action = SystemAction.get(DeleteBranchAction.class);
                        action.deleteBranch(currRepository, branchName);
                    }

                    @Override
                    public boolean isEnabled() {
                        return !active;
                    }
                });
            }
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public Action getPreferredAction () {
            if (currRevision != null) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        RepositoryBrowserPanel.this.firePropertyChange(PROP_REVISION_ACCEPTED, null, currRevision);
                    }
                };
            }
            return null;
        }

        private void refreshTracking (final GitBranch trackedBranch, final File repository) {
            if (trackedBranch != null && repository != null && options.contains(Option.DISPLAY_COMMIT_IDS)
                    && (!branchId.equals(lastTrackingMyId) || !trackedBranch.getId().equals(lastTrackingOtherId))) {
                lastTrackingMyId = branchId;
                lastTrackingOtherId = trackedBranch.getId();
                if (trackedBranch.getId().equals(branchId)) {
                    setShortDescription(NbBundle.getMessage(RepositoryBrowserPanel.class, "MSG_BranchNode.tracking.inSync", trackedBranch.getName())); //NOI18N
                } else {
                    final String id = branchId;
                    RP.post(new Runnable() {
                        @Override
                        public void run () {
                            String tt = null;
                            GitClient client = null;
                            try {
                                client = Git.getInstance().getClient(repository);
                                GitRevisionInfo info = client.getCommonAncestor(new String[] { id, trackedBranch.getId() }, GitUtils.NULL_PROGRESS_MONITOR);
                                if (info == null || !(info.getRevision().equals(id) || info.getRevision().equals(trackedBranch.getId()))) {
                                    tt = NbBundle.getMessage(RepositoryBrowserPanel.class, "MSG_BranchNode.tracking.mergeNeeded", trackedBranch.getName()); //NOI18N
                                } else {
                                    SearchCriteria crit = new SearchCriteria();
                                    if (info.getRevision().equals(trackedBranch.getId())) {
                                        crit.setRevisionFrom(trackedBranch.getId());
                                        crit.setRevisionTo(id);
                                    } else if (info.getRevision().equals(id)) {
                                        crit.setRevisionFrom(id);
                                        crit.setRevisionTo(trackedBranch.getId());
                                    }
                                    GitRevisionInfo[] revs = client.log(crit, GitUtils.NULL_PROGRESS_MONITOR);
                                    int diff = (revs.length - 1);
                                    if (info.getRevision().equals(trackedBranch.getId())) {
                                        tt = NbBundle.getMessage(RepositoryBrowserPanel.class, diff == 1 
                                                ? "MSG_BranchNode.tracking.ahead.commit" : "MSG_BranchNode.tracking.ahead.commits", //NOI18N
                                                trackedBranch.getName(), diff);
                                    } else if (info.getRevision().equals(id)) {
                                        tt = NbBundle.getMessage(RepositoryBrowserPanel.class, diff == 1 
                                                ? "MSG_BranchNode.tracking.behind.commit" : "MSG_BranchNode.tracking.behind.commits", //NOI18N
                                                trackedBranch.getName(), diff);
                                    }
                                }
                            } catch (GitException ex) {
                                LOG.log(Level.INFO, null, ex);
                            } finally {
                                if (client != null) {
                                    client.release();
                                }
                            }
                            final String toolTip = tt;
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run () {
                                    setShortDescription(toolTip);
                                }
                            });
                        }
                    });
                }
            }
        }

        @NbBundle.Messages("MSG_BranchMergeStatus.merged= [merged]")
        private String getMergeStatus (Boolean mergedStatus) {
            if (Boolean.TRUE.equals(mergedStatus)) {
                return Bundle.MSG_BranchMergeStatus_merged();
            } else {
                return "";
            }
        }
        
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="tags">
    private class TagsNode extends RepositoryBrowserNode {

        public TagsNode (File repository) {
            super(new TagChildren(repository), repository);
            assert repository != null;
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/tags.png"); //NOI18N
        }

        @Override
        public String getDisplayName () {
            return getName();
        }

        @Override
        public String getName () {
            return NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_RepositoryPanel.TagsNode.name"); //NOI18N
        }

        @Override
        public Action[] getPopupActions (boolean context) {
            return new Action[] {
                new AbstractAction(NbBundle.getMessage(BranchesTopNode.class, "LBL_RepositoryPanel.RefreshTagsAction.name")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        ((TagChildren) getChildren()).refreshTags();
                    }
                }
            };
        }
    }

    private class TagChildren extends Children.Keys<GitTag> implements PropertyChangeListener {
        private final java.util.Map<String, GitTag> tags = new TreeMap<String, GitTag>();
        private final File repository;

        private TagChildren (File repository) {
            this.repository = repository;
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            if (info == null) {
                LOG.log(Level.INFO, "TagChildren() : Null info for {0}", repository); //NOI18N
            } else {
                info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
            }
        }

        
        @Override
        protected void addNotify () {
            super.addNotify();
            refreshTags();
        }

        @Override
        protected void removeNotify () {
            setKeys(Collections.<GitTag>emptySet());
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes (GitTag key) {
            return new Node[] { new TagNode(repository, key) };
        }

        private void refreshTags () {
            new GitProgressSupport.NoOutputLogging() {
                @Override
                protected void perform () {
                    RepositoryInfo info = RepositoryInfo.getInstance(repository);
                    info.refresh();
                    java.util.Map<String, GitTag> tags = info.getTags();
                    if (!isCanceled()) {
                        refreshTags(tags);
                    }
                }
            }.start(RP, repository, NbBundle.getMessage(BranchesTopChildren.class, "MSG_RepositoryPanel.refreshingTags")); //NOI18N
        }
        
        private void refreshTags (java.util.Map<String, GitTag> tags) {
            if (tags.isEmpty()) {
                this.tags.clear();
            } else {
                tags = new java.util.HashMap<String, GitTag>(tags);
                this.tags.keySet().retainAll(tags.keySet());
                for (java.util.Map.Entry<String, GitTag> e : this.tags.entrySet()) {
                    GitTag newTagInfo = tags.get(e.getKey());
                    // do not refresh tags they keep the same
                    if (newTagInfo != null && (newTagInfo.getTaggedObjectId().equals(e.getValue().getTaggedObjectId()) 
                            && newTagInfo.getMessage().equals(e.getValue().getMessage()))) {
                        tags.remove(e.getKey());
                    }
                }
                this.tags.putAll(tags);
            }
            setKeys(this.tags.values().toArray(new GitTag[this.tags.values().size()]));
        }

        @Override
        public void propertyChange (final PropertyChangeEvent evt) {
            if (RepositoryInfo.PROPERTY_TAGS.equals(evt.getPropertyName())) {
                RP.post(new Runnable() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void run () {
                        refreshTags((java.util.Map<String, GitTag>) evt.getNewValue());
                    }
                });
            }
        }
    }

    private class TagNode extends RepositoryBrowserNode {
        private boolean active;
        private final String tagName;
        private String revisionId;
        private final String message;
        private final PropertyChangeListener list;

        public TagNode (File repository, GitTag tag) {
            super(Children.LEAF, repository, Lookups.singleton(new Revision(tag.getTaggedObjectId(), tag.getTagName())));
            tagName = tag.getTagName();
            message = tag.getMessage();
            revisionId = tag.getTaggedObjectId();
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/tag.png"); //NOI18N
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            if (info == null) {
                LOG.log(Level.INFO, "TagNode() : Null info for {0}", repository); //NOI18N
                list = null;
            } else {
                info.addPropertyChangeListener(WeakListeners.propertyChange(list = new PropertyChangeListener() {
                    @Override
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (RepositoryInfo.PROPERTY_ACTIVE_BRANCH.equals(evt.getPropertyName()) || RepositoryInfo.PROPERTY_HEAD.equals(evt.getPropertyName())) {
                            refreshActiveBranch((GitBranch) evt.getNewValue());
                        }
                    }
                }, info));
                refreshActiveBranch(info.getActiveBranch());
            }
        }

        @Override
        public String getDisplayName () {
            return getName(false);
        }

        @Override
        public String getHtmlDisplayName() {
            return getName(true);
        }

        @Override
        public String getName() {
            return getName(false);
        }
        
        public String getName (boolean html) {
            StringBuilder sb = new StringBuilder();
            if (active && html) {
                sb.append("<html><strong>").append(tagName).append("</strong>"); //NOI18N
            } else {
                sb.append(tagName);
            }
            if (options.contains(Option.DISPLAY_COMMIT_IDS)) {
                sb.append(" - ").append(revisionId); //NOI18N
            }
            return sb.toString();
        }

        private void refreshActiveBranch (GitBranch activeBranch) {
            String oldHtmlName = getHtmlDisplayName();
            if (activeBranch.getId().equals(revisionId)) {
                active = true;
            } else {
                active = false;
            }
            String newHtmlName = getHtmlDisplayName();
            if (!oldHtmlName.equals(newHtmlName)) {
                fireDisplayNameChange(null, null);
            }
        }

        @Override
        public String getShortDescription () {
            return message;
        }

        @Override
        protected Action[] getPopupActions (boolean context) {
            List<Action> actions = new LinkedList<Action>();
            if (currRepository != null && tagName != null) {
                final File repo = currRepository;
                final String tag = tagName;
                actions.add(new AbstractAction(NbBundle.getMessage(RepositoryBrowserAction.class, "LBL_RepositoryBrowser.tagNode.showDetails")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Utils.postParallel(new Runnable () {
                            @Override
                            public void run() {
                                ManageTagsAction action = SystemAction.get(ManageTagsAction.class);
                                action.showTagManager(repo, tag);
                            }
                        }, 0);
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Utils.postParallel(new Runnable () {
                            @Override
                            public void run() {
                                CheckoutRevisionAction action = SystemAction.get(CheckoutRevisionAction.class);
                                action.checkoutRevision(repo, tag);
                            }
                        }, 0);
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(CreateBranchAction.class, "LBL_CreateBranchAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Utils.postParallel(new Runnable () {
                            @Override
                            public void run() {
                                CreateBranchAction action = SystemAction.get(CreateBranchAction.class);
                                action.createBranch(repo, tag);
                            }
                        }, 0);
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Utils.postParallel(new Runnable () {
                            @Override
                            public void run() {
                                MergeRevisionAction action = SystemAction.get(MergeRevisionAction.class);
                                action.mergeRevision(repo, tag);
                            }
                        }, 0);
                    }

                    @Override
                    public boolean isEnabled() {
                        return !active;
                    }
                });
            }
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public Action getPreferredAction () {
            if (currRevision != null) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        RepositoryBrowserPanel.this.firePropertyChange(PROP_REVISION_ACCEPTED, null, currRevision);
                    }
                };
            }
            return null;
        }
        
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="remotes">
    private class RemotesNode extends RepositoryBrowserNode {

        public RemotesNode (File repository) {
            super(new AllRemotesChildren(repository), repository);
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/repository.png"); //NOI18N
        }

        @Override
        public String getDisplayName () {
            return getName();
        }

        @Override
        public String getName () {
            return NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_RepositoryPanel.RemotesNode.name"); //NOI18N
        }
    }

    private class AllRemotesChildren extends Children.Keys<GitRemoteConfig> implements PropertyChangeListener {
        private final File repository;
        private boolean refreshing;

        private AllRemotesChildren (File repository) {
            this.repository = repository;
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            if (info == null) {
                LOG.log(Level.INFO, "AllRemotesChildren() : Null info for {0}", repository); //NOI18N
            } else {
                info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
            }
        }

        @Override
        protected void addNotify () {
            super.addNotify();
            refreshRemotes();
        }

        private void refreshRemotes () {
            new GitProgressSupport.NoOutputLogging() {
                @Override
                protected void perform () {
                    RepositoryInfo info = RepositoryInfo.getInstance(repository);
                    refreshing = true;
                    try {
                        info.refreshRemotes();
                        java.util.Map<String, GitRemoteConfig> remotes = info.getRemotes();
                        if (!isCanceled()) {
                            refreshRemotes(remotes);
                        }
                    } catch (GitException ex) {
                        LOG.log(Level.INFO, null, ex);
                    } finally {
                        refreshing = false;
                    }
                }
            }.start(RP, repository, NbBundle.getMessage(BranchesTopChildren.class, "MSG_RepositoryPanel.refreshingRemotes")); //NOI18N
        }
        
        private void refreshRemotes (java.util.Map<String, GitRemoteConfig> remotes) {
            setKeys(remotes.values());
        }

        @Override
        public void propertyChange (final PropertyChangeEvent evt) {
            if (!refreshing && RepositoryInfo.PROPERTY_REMOTES.equals(evt.getPropertyName())) {
                RP.post(new Runnable() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void run () {
                        refreshRemotes((java.util.Map<String, GitRemoteConfig>) evt.getNewValue());
                    }
                });
            }
        }

        @Override
        protected Node[] createNodes (GitRemoteConfig key) {
            return new Node[] { new RemoteNode(repository, key) };
        }
    }

    private class RemoteNode extends RepositoryBrowserNode {
        private final String remoteName;
        private final File repository;

        public RemoteNode (File repository, GitRemoteConfig remote) {
            super(new RemoteChildren(remote), repository, Lookups.fixed(remote));
            this.repository = repository;
            this.remoteName = remote.getRemoteName();
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/remote.png"); //NOI18N
        }

        @Override
        public String getName () {
            return remoteName;
        }
        
        @Override
        protected Action[] getPopupActions (boolean context) {
            List<Action> actions = new LinkedList<Action>();
            actions.add(new AbstractAction(NbBundle.getMessage(FetchAction.class, "LBL_FetchAction_PopupName")) { //NOI18N
                @Override
                public void actionPerformed (ActionEvent e) {
                    FetchAction action = SystemAction.get(FetchAction.class);
                    action.fetch(repository, getLookup().lookup(GitRemoteConfig.class));
                }
            });
            actions.add(new AbstractAction(NbBundle.getMessage(RepositoryBrowserPanel.class, "LBL_RepositoryPanel.RemoteNode.remove")) { //NOI18N
                @Override
                public void actionPerformed (ActionEvent e) {
                    new RemoveRemoteConfig().removeRemote(repository, remoteName);
                }
            });
            return actions.toArray(new Action[actions.size()]);
        }
    }

    private class RemoteUri {
        final String uri;
        final boolean push;

        public RemoteUri (String url, boolean push) {
            this.uri = url;
            this.push = push;
        }
    }
    
    private class RemoteChildren extends Children.Keys<RemoteUri> {
        private final GitRemoteConfig remote;
        
        public RemoteChildren (GitRemoteConfig remote) {
            this.remote = remote;
        }

        @Override
        protected void addNotify () {
            super.addNotify();
            ArrayList<RemoteUri> urls = new ArrayList<RemoteUri>(remote.getPushUris().size() + remote.getUris().size());
            for (String s : remote.getUris()) {
                urls.add(new RemoteUri(s, false));
            }
            if (remote.getPushUris().isEmpty() && !remote.getUris().isEmpty()) {
                urls.add(new RemoteUri(remote.getUris().get(0), true));
            } else {
                for (String s : remote.getPushUris()) {
                    urls.add(new RemoteUri(s, true));
                }
            }
            Collections.sort(urls, new Comparator<RemoteUri>() {
                @Override
                public int compare (RemoteUri o1, RemoteUri o2) {
                    return o1.uri.compareTo(o2.uri);
                }
            });
            setKeys(urls);
        }
        
        @Override
        protected Node[] createNodes (RemoteUri key) {
            return new Node[] { new RemoteUriNode(lookupRepository(getNode()), key, remote) };
        }
    }
    
    private class RemoteUriNode extends RepositoryBrowserNode {
        private final RemoteUri uri;
        private final GitRemoteConfig remote;

        public RemoteUriNode (File repository, RemoteUri uri, GitRemoteConfig remote) {
            super(Children.LEAF, repository);
            this.uri = uri;
            this.remote = remote;
            setIconBaseWithExtension("org/netbeans/modules/git/resources/icons/" + (uri.push ? "push" : "fetch") + ".png"); //NOI18N
        }

        @Override
        public String getName () {
            return uri.uri;
        }

        @Override
        protected Action[] getPopupActions (boolean context) {
            List<Action> actions = new LinkedList<Action>();
            if (uri.push) {
                // push action
            } else {
                actions.add(new AbstractAction(NbBundle.getMessage(FetchAction.class, "LBL_FetchAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        FetchAction action = SystemAction.get(FetchAction.class);
                        action.fetch(currRepository, uri.uri, remote.getFetchRefSpecs(), null);
                    }
                });
            }
            return actions.toArray(new Action[actions.size()]);
        }
    }
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();

        setLayout(new java.awt.BorderLayout());
        add(toolbar, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setResizeWeight(0.5);

        tree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jSplitPane1.setLeftComponent(tree);
        jSplitPane1.setRightComponent(revisionsPanel1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    final org.netbeans.modules.git.ui.repository.RevisionListPanel revisionsPanel1 = new org.netbeans.modules.git.ui.repository.RevisionListPanel();
    private final org.netbeans.modules.git.ui.repository.ControlToolbar toolbar = new org.netbeans.modules.git.ui.repository.ControlToolbar();
    private final org.openide.explorer.view.BeanTreeView tree = new org.openide.explorer.view.BeanTreeView();
    // End of variables declaration//GEN-END:variables

}

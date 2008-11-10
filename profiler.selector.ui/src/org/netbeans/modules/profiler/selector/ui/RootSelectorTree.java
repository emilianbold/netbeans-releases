/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.profiler.selector.ui;

import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.ui.SwingWorker;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JCheckTree;
import org.netbeans.lib.profiler.ui.components.JCheckTree.CheckTreeListener;
import org.netbeans.lib.profiler.ui.components.tree.CheckTreeNode;
import org.netbeans.modules.profiler.selector.spi.SelectionTreeBuilder;
import org.netbeans.modules.profiler.selector.spi.nodes.ContainerNode;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;
import org.netbeans.modules.profiler.utilities.trees.TreeDecimator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 *
 * @author Jaroslav Bachorik
 */
public class RootSelectorTree extends JCheckTree {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class CancellableController implements ProgressDisplayer.ProgressController {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private volatile boolean cancelled = false;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public boolean isCancelled() {
            return cancelled;
        }

        public boolean cancel() {
            cancelled = true;

            return true;
        }
    }

    private class SelectionTreeViewWrapper implements SelectionTreeView {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private SelectionTreeBuilder wrappedBuilder;
        private boolean enabled;
        private boolean preferred;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public SelectionTreeViewWrapper(SelectionTreeBuilder builder, boolean preferredFlag, boolean enabledFlag) {
            wrappedBuilder = builder;
            preferred = preferredFlag;
            enabled = enabledFlag;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public SelectionTreeBuilder getBuilder() {
            return wrappedBuilder;
        }

        public String getDisplayName() {
            return wrappedBuilder.getDisplayName();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setPreferred(boolean value) {
            preferred = value;
        }

        public boolean isPreferred() {
            return preferred;
        }

        public String toString() {
            return wrappedBuilder.toString();
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static TreeDecimator.NodeFilter<RootSelectorNode> DEFAULT_FILTER = new TreeDecimator.NodeFilter<RootSelectorNode>() {
        public boolean match(RootSelectorNode node) {
            return true;
        }

        public boolean maymatch(RootSelectorNode node) {
            return true;
        }
    };

    private static TreeDecimator.NodeFilter<SelectorNode> DEFAULT_FILTER_INNER = new TreeDecimator.NodeFilter<SelectorNode>() {
        public boolean match(SelectorNode node) {
            return true;
        }

        public boolean maymatch(SelectorNode node) {
            return true;
        }
    };

    // -----
    // I18N String constants
    private static final String EMPTY_STRING = NbBundle.getMessage(RootSelectorTree.class, "RootSelectorTree_EmptyString"); // NOI18N
    private static final String LOADING_STRING = NbBundle.getMessage(RootSelectorTree.class, "RootSelectorTree_LoadingString"); // NOI18N
    private static final String ROOT_STRING = NbBundle.getMessage(RootSelectorTree.class, "RootSelectorTree_RootString"); // NOI18N
    private static final String NO_PROJECT_STRING = NbBundle.getMessage(RootSelectorTree.class, "RootSelectorTree_NoProjectString"); // NOI18N
                                                                                                                                     // -----
    private static final TreeModel DEFAULTMODEL = new DefaultTreeModel(new DefaultMutableTreeNode(EMPTY_STRING));
    public static final String SELECTION_TREE_VIEW_LIST_PROPERTY = "SELECTION_TREE_VIEW_LIST"; // NO18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Set<ClientUtils.SourceCodeSelection> currentSelectionSet = new HashSet<ClientUtils.SourceCodeSelection>();
    private TreeDecimator.NodeFilter<SelectorNode> nodeFilter = DEFAULT_FILTER_INNER;
    private ProgressDisplayer progress = ProgressDisplayer.DEFAULT;
    private SelectionTreeBuilder currentTreeBuilder = SelectionTreeBuilder.NULL;
    private TreeDecimator<SelectorNode> treeDecimator = new TreeDecimator<SelectorNode>() {
        @Override
        protected List<SelectorNode> getChildren(SelectorNode aNode) {
            aNode.getChildCount(true);

            Enumeration<SelectorNode> children = aNode.children();
            List<SelectorNode> newChildren = new ArrayList<SelectorNode>();

            while (children.hasMoreElements()) {
                newChildren.add(children.nextElement());
            }

            return newChildren;
        }

        @Override
        protected void attachChildren(SelectorNode aNode, List<SelectorNode> children) {
            for (SelectorNode child : children) {
                child.setParent(aNode);
            }
        }

        @Override
        protected void detachChild(SelectorNode aNode, SelectorNode child) {
            child.setParent(null);
        }

        @Override
        protected void detachChildren(SelectorNode aNode) {
            aNode.removeAllChildren();
        }
    };

    private Project[] relevantProjects;
    private boolean showInheritedMethods = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public RootSelectorTree() {
        init();
    }

    public RootSelectorTree(ProgressDisplayer pd) {
        progress = pd;
        init();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setNodeFilter(final TreeDecimator.NodeFilter<RootSelectorNode> customizer) {
        nodeFilter = new TreeDecimator.NodeFilter<SelectorNode>() {
                public boolean match(SelectorNode node) {
                    return customizer.match(new RootSelectorNode(node));
                }

                public boolean maymatch(SelectorNode node) {
                    return customizer.maymatch(new RootSelectorNode(node));
                }
            };
    }

    public void setProjects(Project[] projects) {
        relevantProjects = projects;
        firePropertyChange(SELECTION_TREE_VIEW_LIST_PROPERTY, null, null);
    }

    public void setSelection(final ClientUtils.SourceCodeSelection[] selection) {
        new SwingWorker(false) {
                protected void doInBackground() {
                    applySelection(selection);
                }

                protected void done() {
                    treeDidChange();
                }
            }.execute();
    }

    public ClientUtils.SourceCodeSelection[] getSelection() {
        return currentSelectionSet.toArray(new ClientUtils.SourceCodeSelection[currentSelectionSet.size()]);
    }

    public void setSelectionTreeView(SelectionTreeView view) {
        if (view instanceof SelectionTreeViewWrapper) {
            currentTreeBuilder = ((SelectionTreeViewWrapper) view).getBuilder();

            // notify the tree to rebuild itself
            refreshTree();
        }
    }

    public List<SelectionTreeView> getSelectionTreeViewList() {
        List<SelectionTreeView> treeViews = new ArrayList<SelectionTreeView>();

        Map<SelectionTreeBuilder, Integer> frequencyMap = calculateBuilderUsageFrequency();
        SelectionTreeViewWrapper defaultView = null;
        int maxCount = 0;

        for (Map.Entry<SelectionTreeBuilder, Integer> entry : frequencyMap.entrySet()) {
            SelectionTreeViewWrapper view = new SelectionTreeViewWrapper(entry.getKey(), false, entry.getValue() > 0);

            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                defaultView = view;
            }

            treeViews.add(view);
        }

        if (defaultView != null) {
            defaultView.setPreferred(true);
        }

        return treeViews;
    }

    public void setShowingInheritedMethods(boolean value) {
        showInheritedMethods = value;
    }

    /**
     * Override this method to filter showing inherited methods
     */
    public boolean isShowingInheritedMethods() {
        return showInheritedMethods;
    }

    public static boolean canBeShown() {
        return Lookup.getDefault().lookup(SelectionTreeBuilder.class) != null;
    }

    /**
     * Resets the selector tree
     * Clears the list of selected root methods + sets the default tree model
     * Should be called right before trying to show the selector tree
     */
    public void reset() {
        setModel(DEFAULTMODEL);
        currentSelectionSet.clear();
        relevantProjects = new Project[0];
    }

    public void setup(Project[] projects, final ClientUtils.SourceCodeSelection[] selection) {
        if (this.currentSelectionSet != null) {
            this.currentSelectionSet.clear();
        }

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setProjects(projects);
        applySelection(selection);
    }

    protected SelectionTreeBuilder getBuilder() {
        return currentTreeBuilder;
    }

    private static void addRequiredPackages(Collection<ClientUtils.SourceCodeSelection> selection,
                                            Collection<ClientUtils.SourceCodeSelection> toAdd) {
        for (ClientUtils.SourceCodeSelection signature : selection) {
            boolean appendSignature = true;

            for (ClientUtils.SourceCodeSelection addSignature : toAdd) {
                appendSignature = appendSignature && !signature.contains(addSignature);
            }

            if (appendSignature) {
                toAdd.add(signature);
            }
        }
    }

    private Map<SelectionTreeBuilder, Integer> calculateBuilderUsageFrequency() {
        Collection<?extends SelectionTreeBuilder> allBuilders = Lookup.getDefault().lookupAll(SelectionTreeBuilder.class);
        Map<SelectionTreeBuilder, Integer> frequencyMap = new HashMap<SelectionTreeBuilder, Integer>();
        SelectionTreeBuilder defaultBuilder = SelectionTreeBuilder.NULL;

        for (Project currentProject : relevantProjects) {
            boolean defaultBuilderUsed = false;

            for (SelectionTreeBuilder builder : allBuilders) {
                if (builder.isDefault()) {
                    defaultBuilderUsed = true;
                    defaultBuilder = builder;

                    continue;
                }

                if (builder.supports(currentProject) && builder.isPreferred(currentProject)) {
                    if (!frequencyMap.containsKey(builder)) {
                        frequencyMap.put(builder, 2); // starting at 2 to push a possible default bulder to the end
                    } else {
                        frequencyMap.put(builder, frequencyMap.get(builder) + 1);
                    }
                }
            }

            if (defaultBuilderUsed) {
                if (!frequencyMap.containsKey(defaultBuilder)) {
                    frequencyMap.put(defaultBuilder, 1);
                } else {
                    frequencyMap.put(defaultBuilder, frequencyMap.get(defaultBuilder) + 1);
                }
            }
        }

        return frequencyMap;
    }

    private static void calculateInflatedSelection(SelectorNode node, SelectorNode root,
                                                   Collection<ClientUtils.SourceCodeSelection> selection,
                                                   Collection<ClientUtils.SourceCodeSelection> toRemove) {
        if ((node == null) || (root == null) || (selection == null) || (toRemove == null)) {
            return; // don't process an invalid data
        }

        if (root.isFullyChecked() || root.isPartiallyChecked()) {
            if ((root.getSignature() != null) && root.isFullyChecked() && !toRemove.contains(root.getSignature())) {
                selection.add(root.getSignature());
            }

            if ((root.getSignature() == null) || (node.getSignature() == null)
                    || root.getSignature().contains(node.getSignature())) {
                int childrenCount = root.getChildCount();

                for (int i = 0; i < childrenCount; i++) {
                    SelectorNode childNode = (SelectorNode) root.getChildAt(i);
                    calculateInflatedSelection(node, childNode, selection, toRemove);
                }
            }
        }
    }

    private static void checkNodeChildren(final DefaultMutableTreeNode myNode, boolean recurse) {
        checkNodeChildren(myNode, recurse, null);
    }

    private static void checkNodeChildren(final DefaultMutableTreeNode myNode, boolean recurse, CancellableController controller) {
        if ((controller != null) && controller.isCancelled()) {
            return;
        }

        Enumeration children = myNode.children();

        if (myNode instanceof CheckTreeNode) {
            if (((CheckTreeNode) myNode).isFullyChecked()) {
                while (children.hasMoreElements()) {
                    TreeNode child = (TreeNode) children.nextElement();

                    if (child instanceof CheckTreeNode) {
                        ((CheckTreeNode) child).setChecked(true);

                        if (recurse) {
                            checkNodeChildren((CheckTreeNode) child, recurse, controller);
                        }
                    }
                }
            }
        }
    }

    private boolean isSingleSelection() {
        return (relevantProjects != null) && (relevantProjects.length < 2);
    }

    private DefaultMutableTreeNode getTreeRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(ROOT_STRING);

        if (relevantProjects != null) {
            for (Project project : relevantProjects) {
                if (getBuilder().supports(project)) {
                    for (SelectorNode node : getBuilder().buildSelectionTree(project, isSingleSelection())) {
                        if (node.isValid()) {
                            node = customizeTree(node);

                            if (node != null) {
                                root.add(node);
                            }
                        }
                    }
                }
            }
        } else {
            root.add(new DefaultMutableTreeNode(NO_PROJECT_STRING));
        }

        return root;
    }

    private void applyCurrentSelection() {
        TreeNode root = (TreeNode) this.getModel().getRoot();
        Enumeration childrenEnum = root.children();

        while (childrenEnum.hasMoreElements()) {
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                for (ClientUtils.SourceCodeSelection selection : currentSelectionSet) {
                    applySelection((SelectorNode) child, selection);
                }
            }
        }
    }

    private void applySelection(ClientUtils.SourceCodeSelection[] selections) {
        TreeNode root = (TreeNode) this.getModel().getRoot();
        Enumeration childrenEnum = root.children();

        while (childrenEnum.hasMoreElements()) {
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                for (ClientUtils.SourceCodeSelection selection : selections) {
                    applySelection((SelectorNode) child, selection);
                }
            }
        }

        currentSelectionSet.addAll(Arrays.asList(selections));
    }

    private void applySelection(SelectorNode node, ClientUtils.SourceCodeSelection selection) {
        ClientUtils.SourceCodeSelection signature = node.getSignature();

        if (signature != null) {
            if (signature.equals(selection) || selection.contains(signature)) {
                node.setChecked(true);

                return;
            }

            if (!signature.contains(selection)) {
                return;
            }
        }

        Enumeration childrenEnum = node.children();

        while (childrenEnum.hasMoreElements()) {
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                applySelection((SelectorNode) child, selection);
            }
        }
    }

    private SelectorNode customizeTree(SelectorNode root) {
        if (root == null) {
            return null;
        }

        return treeDecimator.decimate(root, nodeFilter);
    }

    private void init() {
        UIUtils.makeTreeAutoExpandable(this, true);
        this.addCheckTreeListener(new CheckTreeListener() {
                public void checkTreeChanged(Collection<CheckTreeNode> nodes) {
                }

                public void checkNodeToggled(final TreePath treePath, boolean before) {
                    if (!before) { // only after the node check-mark has been changed

                        SelectorNode selectedNode = (SelectorNode) treePath.getLastPathComponent();
                        Collection<ClientUtils.SourceCodeSelection> signatures = selectedNode.getRootMethods(true);

                        if (selectedNode.isFullyChecked()) { // new root method selected

                            Collection<ClientUtils.SourceCodeSelection> toRemove = new ArrayList<ClientUtils.SourceCodeSelection>();

                            // replace with this root method as much as possible from the previously selected root methods (eg. wildcard replacing single root methods within a package etc.)
                            // basically remove all root methods of the selected node's subtree
                            for (ClientUtils.SourceCodeSelection signature : signatures) {
                                for (ClientUtils.SourceCodeSelection rootMethod : currentSelectionSet) {
                                    if (signature.contains(rootMethod)) {
                                        toRemove.add(rootMethod);
                                    }
                                }
                            }

                            removeSelection(toRemove.toArray(new ClientUtils.SourceCodeSelection[toRemove.size()]));
                            applySelection(signatures.toArray(new ClientUtils.SourceCodeSelection[signatures.size()]));
                        } else {
                            // removing a previously selected root method
                            ContainerNode parent = selectedNode.getParent();

                            Collection<ClientUtils.SourceCodeSelection> toAdd = new ArrayList<ClientUtils.SourceCodeSelection>();

                            if (parent != null) {
                                Enumeration siblings = parent.children();

                                // might be changing full-check to partial-check for the selected node parent; in that case replace the parent's wildcarded root method with its children root methods
                                while (siblings.hasMoreElements()) {
                                    SelectorNode siblingNode = (SelectorNode) siblings.nextElement();

                                    if ((siblingNode != selectedNode) && siblingNode.isFullyChecked()) {
                                        toAdd.addAll(siblingNode.getRootMethods(true));
                                    }
                                }
                                toAdd.removeAll(signatures);
                            }

                            Collection<ClientUtils.SourceCodeSelection> toRemove = new ArrayList<ClientUtils.SourceCodeSelection>();

                            for (ClientUtils.SourceCodeSelection signature : signatures) {
                                for (ClientUtils.SourceCodeSelection rootMethod : currentSelectionSet) {
                                    if (rootMethod.contains(signature) || signature.contains(rootMethod)) {
                                        toRemove.add(rootMethod);
                                    }
                                }
                            }

                            toRemove.addAll(signatures);

                            TreeNode root = (TreeNode) getModel().getRoot();
                            Collection<ClientUtils.SourceCodeSelection> selection = new ArrayList<ClientUtils.SourceCodeSelection>();
                            int firstLevelCnt = root.getChildCount();

                            for (int i = 0; i < firstLevelCnt; i++) {
                                calculateInflatedSelection(parent, (SelectorNode) root.getChildAt(i), selection, toRemove);
                            }

                            addRequiredPackages(selection, toAdd);

                            removeSelection(toRemove.toArray(new ClientUtils.SourceCodeSelection[toRemove.size()]));

                            applySelection(toAdd.toArray(new ClientUtils.SourceCodeSelection[toAdd.size()]));
                        }
                    }
                }
            });
        this.addTreeWillExpandListener(new TreeWillExpandListener() {
                private volatile boolean openingSubtree = false;

                public void treeWillCollapse(TreeExpansionEvent event)
                                      throws ExpandVetoException {
                }

                public void treeWillExpand(final TreeExpansionEvent event)
                                    throws ExpandVetoException {
                    TreeNode node = (TreeNode) event.getPath().getLastPathComponent();

                    if (!(node instanceof DefaultMutableTreeNode)) {
                        return;
                    }

                    final DefaultMutableTreeNode myNode = (DefaultMutableTreeNode) node;

                    if (myNode.getChildCount() == -1) {
                        if (openingSubtree) {
                            throw new ExpandVetoException(event);
                        }

                        openingSubtree = true;

                        new SwingWorker() {
                                protected void doInBackground() {
                                    checkNodeChildren(myNode, false);
                                }

                                protected void nonResponding() {
                                    progress.showProgress(NbBundle.getMessage(this.getClass(), "NodeLoadingMessage")); // NOI18N
                                }

                                protected void done() {
                                    progress.close();

                                    expandPath(event.getPath());
                                    doLayout();
                                    openingSubtree = false;
                                }
                            }.execute();
                        throw new ExpandVetoException(event);
                    } else {
                        checkNodeChildren(myNode, false);
                    }
                }
            });

        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.setModel(DEFAULTMODEL);
    }

    private void refreshTree() {
        setModel(new DefaultTreeModel(new DefaultMutableTreeNode(LOADING_STRING)));
        setRootVisible(true);
        setShowsRootHandles(false);

        setRootVisible(false);
        setShowsRootHandles(true);
        setModel(new DefaultTreeModel(getTreeRoot()));
        applyCurrentSelection();
        treeDidChange();
    }

    private void removeSelection(ClientUtils.SourceCodeSelection[] selections) {
        TreeNode root = (TreeNode) this.getModel().getRoot();
        Enumeration childrenEnum = root.children();

        while (childrenEnum.hasMoreElements()) {
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                for (ClientUtils.SourceCodeSelection selection : selections) {
                    removeSelection((SelectorNode) child, selection);
                }
            }
        }

        currentSelectionSet.removeAll(Arrays.asList(selections));
    }

    private void removeSelection(SelectorNode node, ClientUtils.SourceCodeSelection selection) {
        ClientUtils.SourceCodeSelection signature = node.getSignature();

        if (signature != null) {
            if (signature.equals(selection)) {
                node.setChecked(false);

                return;
            }

            if (!signature.contains(selection)) {
                return;
            }
        }

        Enumeration childrenEnum = node.children();

        while (childrenEnum.hasMoreElements()) {
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                removeSelection((SelectorNode) child, selection);
            }
        }
    }
}

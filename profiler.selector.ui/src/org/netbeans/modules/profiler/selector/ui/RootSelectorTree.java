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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.selector.ui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import org.netbeans.modules.profiler.api.ProgressDisplayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.lib.profiler.ui.SwingWorker;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.CellTipManager;
import org.netbeans.lib.profiler.ui.components.JCheckTree;
import org.netbeans.lib.profiler.ui.components.tree.CheckTreeNode;
import org.netbeans.lib.profiler.utils.formatting.DefaultMethodNameFormatter;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory;
import org.netbeans.modules.profiler.api.GestureSubmitter;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.netbeans.modules.profiler.selector.api.nodes.*;
import org.netbeans.modules.profiler.selector.spi.SelectionTreeBuilder;
import org.netbeans.modules.profiler.selector.api.SelectionTreeBuilderType;
import org.netbeans.modules.profiler.utilities.trees.NodeFilter;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "RootMethodsAccessMessage=Retrieving Root Methods...",
    "RootSelectorTree_EmptyString=Empty",
    "RootSelectorTree_LoadingString=Loading...",
    "RootSelectorTree_RootString=root",
    "RootSelectorTree_NoProjectString=No projects selected",
    "MSG_ApplyingSelection=Applying Selection...",
    "NodeLoadingMessage=Loading..."
})
public class RootSelectorTree extends JPanel {
    final private static MethodNameFormatterFactory methodFormatterFactory = MethodNameFormatterFactory.getDefault(new DefaultMethodNameFormatter(DefaultMethodNameFormatter.VERBOSITY_FULLCLASSMETHOD));
    final private static Comparator<SourceCodeSelection> containmentComparator = new Comparator<SourceCodeSelection>() {
        @Override
        public int compare(SourceCodeSelection o1, SourceCodeSelection o2) {
            if (o1 == null && o2 != null) return 1;
            if (o1 != null && o2 == null) return -1;
            if (o1 == null && o2 == null) return 0;

            if (o1.equals(o2)) return 0;
            if (o1.contains(o2)) return -1;
            if (o2.contains(o1)) return 1;

            return o1.toFlattened().compareTo(o2.toFlattened());
        }
    };
    
    private static class TypeEntry {
        SelectionTreeBuilderType type;
        int frequency;

        public TypeEntry(SelectionTreeBuilderType type) {
            this.type = type;
            frequency = 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypeEntry other = (TypeEntry) obj;
            if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
            return hash;
        }
    }
    
    private JCheckTree tree = new JCheckTree() {
        @Override
        public String getToolTipText(MouseEvent event) {
            TreePath tp = tree.getPathForLocation(event.getX(), event.getY());
                
            if (tp != null && tp.getPathCount() > 1) {
                while (tp != null) {
                    TreeNode n = (TreeNode)tp.getLastPathComponent();

                    if (n instanceof SelectorNode) {
                        if (tp.getPathCount() == 2) {
                            return ((SelectorNode)n).getDisplayName();
                        }

                        String txt = null;
                        if (n instanceof PackageNode) {
                            txt = ((PackageNode)n).getPackageInfo().getBinaryName();
                        } else if (n instanceof ClassNode) {
                            txt = ((ClassNode)n).getClassInfo().getQualifiedName();
                        } else  if (n instanceof MethodNode) {
                            SourceMethodInfo mi = ((MethodNode)n).getMethodInfo();
                            txt = methodFormatterFactory.getFormatter().formatMethodName(mi.getClassName(), mi.getName(), mi.getSignature()).toFormatted();
                        } else if (n instanceof ConstructorNode) {
                            SourceMethodInfo mi = ((ConstructorNode)n).getMethodInfo();
                            txt = methodFormatterFactory.getFormatter().formatMethodName(mi.getClassName(), mi.getName(), mi.getSignature()).toFormatted();
                        }

                        if (txt != null) {
                            if (txt.isEmpty()) {
                                txt = PackageNode.DEFAULT_NAME;
                            }
                            return txt;
                        }
                    }
                    tp = tp.getParentPath();
                }
            }
            return super.getToolTipText(event);
        }
    };
    
    final private static NodeFilter<SelectorNode> DEFAULT_FILTER_INNER = new NodeFilter<SelectorNode>() {

        @Override
        public boolean match(SelectorNode node) {
            return true;
        }

        @Override
        public boolean maymatch(SelectorNode node) {
            return true;
        }
    };
    public static NodeFilter<SelectorNode> DEFAULT_FILTER = DEFAULT_FILTER_INNER;
    
    private static final TreeModel DEFAULTMODEL = new DefaultTreeModel(new DefaultMutableTreeNode(Bundle.RootSelectorTree_EmptyString()));
    public static final String SELECTION_TREE_VIEW_LIST_PROPERTY = "SELECTION_TREE_VIEW_LIST"; // NO18N
    private final Set<SourceCodeSelection> currentSelectionSet = new HashSet<SourceCodeSelection>();
    private ProgressDisplayer progress = ProgressDisplayer.DEFAULT;
//    private NodeFilter<SelectorNode> nodeFilter = DEFAULT_FILTER_INNER;
    private Lookup context = Lookup.EMPTY;
    private SelectionTreeBuilderType builderType = null;
    private SearchPanel searchPanel = null;
    final private TreePathSearch.ClassIndex ci;
    private Cancellable cancellHandler;

    public RootSelectorTree(ProgressDisplayer pd, TreePathSearch.ClassIndex ci) {
        this.progress = pd;
        this.ci = ci;
//        this.nodeFilter = filter;
        init();
    }

    public void setContext(Lookup context) {
        this.context = context;

        firePropertyChange(SELECTION_TREE_VIEW_LIST_PROPERTY, null, null);
    }
    
    public void setCancelHandler(Cancellable cancellable) {
        this.cancellHandler = cancellable;
    }

    final private AtomicBoolean isActive = new AtomicBoolean(true);
    final private Semaphore semaphore = new Semaphore(1);
    
    public void setSelection(final SourceCodeSelection[] selection, Lookup context) {
        setSelection(selection);
        setContext(context);
    }
    
    private static interface SelectionGetter {
        SourceCodeSelection[] getSelection();
    }
    
    final private class SelectionSetter extends SwingWorker {
        volatile private ProgressDisplayer pd = null;
        final private SelectionGetter getter;
        
        private SelectionSetter(SelectionGetter selection) {
            super(semaphore);
            this.getter = selection;
        }
        
        private SelectionSetter(final SourceCodeSelection[] newSelection) {
            this(new SelectionGetter() {

                @Override
                public SourceCodeSelection[] getSelection() {
                    return newSelection;
                }
            });
        }
        
        protected void doInBackground() {
            isActive.set(true);
            SourceCodeSelection[] newSelection = getter.getSelection(); // just in case if the getter decides to return the current selection which gets cleaned on the next line
            removeSelection(getSelection());
            applySelection(newSelection);
        }

        @Override
        protected void nonResponding() {
            final CountDownLatch cl = new CountDownLatch(1);
            final SwingWorker worker = this;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    RootSelectorTree.this.setEnabled(false);
                    pd = progress.showProgress(Bundle.MSG_ApplyingSelection(), new ProgressDisplayer.ProgressController() {
                        @Override
                        public boolean cancel() {
                            worker.cancel();
                            return true;
                        }
                    });
                    cl.countDown();
                }
            });

            try {
                cl.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        protected void done() {
            closeProgress();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    RootSelectorTree.this.setEnabled(true);
                }
            });
            tree.treeDidChange();
        }

        private void closeProgress() {
            if (pd != null) {
                pd.close();
                pd = null;
            }                
        }

        @Override
        protected int getWarmup() {
            return 50;
        }

        @Override
        protected void cancelled() {
            isActive.set(false);
            closeProgress();
            if (cancellHandler != null) {
                cancellHandler.cancel();
            }
        }
    }
    
    private void setSelection(final SourceCodeSelection[] selection) {
        new SelectionSetter(selection).execute();
    }

    public SourceCodeSelection[] getSelection() {
        synchronized(currentSelectionSet) {
            if (currentSelectionSet.isEmpty()) return new SourceCodeSelection[0];

            List<SourceCodeSelection> selectionList = new ArrayList<SourceCodeSelection>(currentSelectionSet);

            Collections.sort(selectionList, containmentComparator);

            currentSelectionSet.clear();
            SourceCodeSelection parentSel = null;
            for(SourceCodeSelection scs : selectionList) {
                if (parentSel == null || !parentSel.contains(scs)) {
                    parentSel = scs;
                    currentSelectionSet.add(scs);
                }
            }        
            return currentSelectionSet.toArray(new SourceCodeSelection[currentSelectionSet.size()]);
        }
    }

    public List<SelectionTreeBuilderType> getBuilderTypes() {
//      **** useful for testing *******
//      return Collections.EMPTY_LIST;
//      *******************************

        List<TypeEntry> entries = new ArrayList<TypeEntry>();

        for (SelectionTreeBuilder builder : context.lookupAll(SelectionTreeBuilder.class)) {
            if (builder.estimatedNodeCount() == -1) continue; // builder can't build the tree for some reason
            SelectionTreeBuilderType type = builder.getType();
            TypeEntry te = new TypeEntry(type);
            if (entries.contains(te)) {
                int index = entries.indexOf(te);
                te = entries.get(index);
                te.frequency += builder.isPreferred() ? 2 : 1;
            } else {
                te.frequency = builder.isPreferred() ? 2 : 1;
                entries.add(te);
            }
        }

        Collections.sort(entries, new Comparator<TypeEntry>() {

            @Override
            public int compare(TypeEntry o1, TypeEntry o2) {
                if (o1.frequency < o2.frequency) {
                    return 1;
                } else if (o1.frequency > o2.frequency) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        List<SelectionTreeBuilderType> types = new ArrayList<SelectionTreeBuilderType>(entries.size());
        for (TypeEntry entry : entries) {
            types.add(entry.type);
        }

        return types;
    }

    public void setBuilderType(SelectionTreeBuilderType type) {
        builderType = type;
        refreshTree();
    }

    /**
     * Resets the selector tree
     * Clears the list of selected root methods + sets the default tree model
     * Should be called right before trying to show the selector tree
     */
    public void reset() {
        isActive.set(true);
        tree.setModel(DEFAULTMODEL);
        context = Lookup.EMPTY;
        sCont = null;
        if (searchPanel != null) searchPanel.reset();
        synchronized(currentSelectionSet) {
            currentSelectionSet.clear();
        }
    }

    public void setRowHeight(int rowHeight) {
        tree.setRowHeight(rowHeight);
    }

    public static boolean canBeShown(Lookup ctx) {
        return ctx.lookup(SelectionTreeBuilder.class) != null;
    }

    private void init() {
        /*** Disable celltips, enable tooltips ***/
        CellTipManager.sharedInstance().unregisterComponent(tree);
        ToolTipManager.sharedInstance().registerComponent(tree);
        /*****************************************/
        
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        UIUtils.makeTreeAutoExpandable(tree, true);
        
        setupTreeNodeToggleLogic();
        addTreeLazyOpening();

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setModel(DEFAULTMODEL);
        
        JScrollPane scrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        if (!Boolean.getBoolean("profiler.roots.hide.libraries")) { // NOI18N
            searchPanel = new SearchPanel(tree) {

                @Override
                protected void performFind() {
                    tree.requestFocus();
                    findNode(getSearchText());
                }

                @Override
                protected void performNext() {
                    tree.requestFocus();
                    find(false);
                }

                @Override
                protected void performPrevious() {
                    tree.requestFocus();
                    find(true);
                }

                @Override
                protected void onCancel() {
                    tree.requestFocus();
                }

                @Override
                protected void onClose() {
                    searchPanel.setPermanent(false);
                }
            };
        
            add(searchPanel, BorderLayout.SOUTH);
        }
        
        scrollPane.setPreferredSize(tree.getPreferredSize());
        
        invalidate();
        revalidate();
        repaint();
    }

    final private Semaphore lazyOpeningSemaphore = new Semaphore(1);
    private void addTreeLazyOpening() {
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {

            private volatile boolean openingSubtree = false;

            @Override
            public void treeWillCollapse(TreeExpansionEvent event)
                    throws ExpandVetoException {
            }

            @Override
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

                    new SwingWorker(lazyOpeningSemaphore) {

                        @Override
                        protected void doInBackground() {
                            checkNodeChildren(myNode, false);
                        }

                        @Override
                        protected void nonResponding() {
                            progress.showProgress(Bundle.NodeLoadingMessage());
                        }

                        @Override
                        protected void done() {
                            progress.close();

                            tree.expandPath(event.getPath());
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
    }

    private void setupTreeNodeToggleLogic() {
        tree.addCheckTreeListener(new JCheckTree.CheckTreeListener() {
            @Override
            public void checkTreeChanged(Collection<CheckTreeNode> nodes) {
            }

            @Override
            public void checkNodeToggled(final TreePath treePath, boolean before) {
                if (!before) { // only after the node check-mark has been changed

                    SelectorNode selectedNode = (SelectorNode) treePath.getLastPathComponent();
                    Collection<SourceCodeSelection> signatures = selectedNode.getRootMethods(true);

                    if (selectedNode.isFullyChecked()) { // new root method selected

                        Collection<SourceCodeSelection> toRemove = new ArrayList<SourceCodeSelection>();

                        // replace with this root method as much as possible from the previously selected root methods (eg. wildcard replacing single root methods within a package etc.)
                        // basically remove all root methods of the selected node's subtree
                        synchronized(currentSelectionSet) {
                            for (SourceCodeSelection signature : signatures) {
                                for (SourceCodeSelection rootMethod : currentSelectionSet) {
                                    if (signature.contains(rootMethod)) {
                                        toRemove.add(rootMethod);
                                    }
                                }
                            }
                        }

                        removeSelection(toRemove.toArray(new SourceCodeSelection[toRemove.size()]));
                        applySelection(signatures.toArray(new SourceCodeSelection[signatures.size()]));
                    } else {
                        // removing a previously selected root method
                        ContainerNode parent = selectedNode.getParent();

                        Collection<SourceCodeSelection> toAdd = new ArrayList<SourceCodeSelection>();

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

                        Collection<SourceCodeSelection> toRemove = new ArrayList<SourceCodeSelection>();

                        for (SourceCodeSelection signature : signatures) {
                            for (SourceCodeSelection rootMethod : currentSelectionSet) {
                                if (rootMethod.contains(signature) || signature.contains(rootMethod)) {
                                    toRemove.add(rootMethod);
                                }
                            }
                        }

                        toRemove.addAll(signatures);

                        TreeNode root = (TreeNode) tree.getModel().getRoot();
                        Collection<SourceCodeSelection> selection = new ArrayList<SourceCodeSelection>();
                        int firstLevelCnt = root.getChildCount();

                        for (int i = 0; i < firstLevelCnt; i++) {
                            calculateInflatedSelection(parent, (SelectorNode) root.getChildAt(i), selection, toRemove);
                        }

                        addRequiredPackages(selection, toAdd);

                        removeSelection(toRemove.toArray(new SourceCodeSelection[toRemove.size()]));

                        applySelection(toAdd.toArray(new SourceCodeSelection[toAdd.size()]));
                    }
                }
            }
        });
    }

    private TreePathSearch sCont;
    private AtomicBoolean searchInProgress = new AtomicBoolean(false);
    
    private void findNode(final String searchText) {
        GestureSubmitter.logRMSSearch(searchText);
        
        sCont = new TreePathSearch((TreeNode)tree.getModel().getRoot(), searchText, ci);
        find(false);
    }
    
    @NbBundle.Messages({
        "MSG_SEARCHING=Searching...",
        "CAP_SEARCHRSLT=Search Results",
        "MSG_NOMATCH=No results available"
    })
    private void find(final boolean backward) {
        if (sCont == null) {
            // todo display warning
            return;
        }
        if (searchInProgress.compareAndSet(false, true)) {
            new SwingWorker() {
                volatile private TreePath rsltPath;
                volatile private ProgressDisplayer pd;
                @Override
                protected void doInBackground() {
                    rsltPath = backward ? sCont.back() : sCont.forward();
                }

                @Override
                protected void nonResponding() {
                    pd = progress.showProgress(Bundle.MSG_SEARCHING(), new ProgressDisplayer.ProgressController() {

                        @Override
                        public boolean cancel() {
                            if (sCont != null) {
                                sCont.cancel();
                            }
                            return true;
                        }
                    });
                }

                @Override
                protected void done() {
                    try {
                        if (pd != null) {
                            pd.close();
                        }
                        if (rsltPath != null) {
                            tree.makeVisible(rsltPath);
                            tree.setSelectionPath(rsltPath);
                            tree.scrollPathToVisible(rsltPath);
                        } else {
                            ProfilerDialogs.displayWarning(Bundle.MSG_NOMATCH(), Bundle.CAP_SEARCHRSLT(), null);
                        }
                    } finally {
                        searchInProgress.set(false);
                    }
                }
            }.execute();
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
    
    private void applySelection(SourceCodeSelection[] selections) {
        if (!isActive.get()) return;
        
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        Enumeration childrenEnum = root.children();

        while (childrenEnum.hasMoreElements()) {
            if (!isActive.get()) return;
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                for (SourceCodeSelection selection : selections) {
                    applySelection((SelectorNode) child, selection);
                }
            }
        }

        synchronized(currentSelectionSet) {
            currentSelectionSet.addAll(Arrays.asList(selections));
        }
    }

    private void applySelection(SelectorNode node, SourceCodeSelection selection) {
        if (!isActive.get()) return;
        SourceCodeSelection signature = node.getSignature();

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
            if (!isActive.get()) return;
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                applySelection((SelectorNode) child, selection);
            }
        }
    }

    private void removeSelection(SourceCodeSelection[] selections) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        Enumeration childrenEnum = root.children();

        while (childrenEnum.hasMoreElements()) {
            Object child = childrenEnum.nextElement();

            if (child instanceof SelectorNode) {
                for (SourceCodeSelection selection : selections) {
                    removeSelection((SelectorNode) child, selection);
                }
            }
        }

        currentSelectionSet.removeAll(Arrays.asList(selections));
    }

    private void removeSelection(SelectorNode node, SourceCodeSelection selection) {
        SourceCodeSelection signature = node.getSignature();

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

    private static void calculateInflatedSelection(SelectorNode node, SelectorNode root,
            Collection<SourceCodeSelection> selection,
            Collection<SourceCodeSelection> toRemove) {
        if ((node == null) || (root == null) || (selection == null) || (toRemove == null)) {
            return; // don't process an invalid data
        }

        if (root.isFullyChecked() || root.isPartiallyChecked()) {
            if ((root.getSignature() != null) && root.isFullyChecked() && !toRemove.contains(root.getSignature())) {
                selection.add(root.getSignature());
            }

            if ((root.getSignature() == null) || (node.getSignature() == null) || root.getSignature().contains(node.getSignature())) {
                int childrenCount = root.getChildCount();

                for (int i = 0; i < childrenCount; i++) {
                    SelectorNode childNode = (SelectorNode) root.getChildAt(i);
                    calculateInflatedSelection(node, childNode, selection, toRemove);
                }
            }
        }
    }

    private static void addRequiredPackages(Collection<SourceCodeSelection> selection,
            Collection<SourceCodeSelection> toAdd) {
        for (SourceCodeSelection signature : selection) {
            boolean appendSignature = true;

            for (SourceCodeSelection addSignature : toAdd) {
                appendSignature = appendSignature && !signature.contains(addSignature);
            }

            if (appendSignature) {
                toAdd.add(signature);
            }
        }
    }

    private void refreshTree() {
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(Bundle.RootSelectorTree_LoadingString())));
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setModel(new DefaultTreeModel(getTreeRoot()));
        tree.treeDidChange();
        applyCurrentSelection();
    }

    private DefaultMutableTreeNode getTreeRoot() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(Bundle.RootSelectorTree_RootString());

        if (builderType != null) {
            for (SelectionTreeBuilder builder : context.lookupAll(SelectionTreeBuilder.class)) {
                if (builder.getType().equals(builderType)) {
                    for (SelectorNode node : builder.buildSelectionTree()) {
                        rootNode.add(node);
                    }
                }
            }
        }

        return rootNode;
    }

    private void applyCurrentSelection() {
        new SelectionSetter(new SelectionGetter() {

            @Override
            public SourceCodeSelection[] getSelection() {
                return RootSelectorTree.this.getSelection();
            }
        }).execute();
    }
}

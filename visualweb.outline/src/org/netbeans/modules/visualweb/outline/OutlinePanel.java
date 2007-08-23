/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.outline;


import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.event.DesignProjectListener;
import com.sun.rave.designtime.faces.FacesDesignProject;
//import com.sun.rave.designtime.event.DesignProjectListener;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;


/**
 * Component showing the outline of the specified <code>DesignBean</code>
 * It works the way, it looks for the <code>DesignBean</code> instance
 * in the provided <code>Lookup</code>, and then it finds its root bean
 * and corresponding <code>DesignContext</code>, which is used to retrieve
 * the relevant beans and their <code>Node</code> represenations.
 *
 * @author Peter Zavadsky
 */
class OutlinePanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider, HelpCtx.Provider {

    /** Debugging flag. */
    private static final boolean DEBUG = ErrorManager.getDefault()
            .getInstance(OutlinePanel.class.getName()).isLoggable(ErrorManager.INFORMATIONAL);

    private static final OutlinePanel instance = new OutlinePanel();

    private final ExplorerManager manager = new ExplorerManager();
    private final Lookup lookup;

    private final OutlineTreeView treeView = new OutlineTreeView(this);

    private final PropertyChangeListener outlineManagerListener = new OutlineManagerListener();

//    private DesignProjectListener designProjectListener;

    private final OutlineRootChildren outlineRootChildren = new OutlineRootChildren(this);


    /** Creates a new instance of OutlinePanel */
    private OutlinePanel() {
        // same as before...
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false // NOI18N

		// Do not use the InputMap for Cut, Copy, Paste and Delete. Instead let the key bindings
		// for global Cut, Copy, Paste and Delete.
        // ...but add e.g.:
        //InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        //keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction); // NOI18N
        //keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction); // NOI18N
        //keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction); // NOI18N
        //keys.put(KeyStroke.getKeyStroke("DELETE"), "delete"); // NOI18N
        
        // >>>Debugging helper
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "rave-outline-dump-nodes"); // NOI18N
        map.put("rave-outline-dump-nodes", // NOI18N
            new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    dumpActivatedNodes();
                }
            }
        );
        // <<<Debugging helper
        

        // ...and initialization of lookup variable
        lookup = ExplorerUtils.createLookup(manager, map);
        
        treeView.setRootVisible(false);
        
        setLayout(new BorderLayout());
        add(treeView, BorderLayout.CENTER);
        
        manager.addPropertyChangeListener(outlineManagerListener);
        
//        manager.setRootContext(new OutlineRootNode());
        Node rootNode = new AbstractNode(outlineRootChildren);
        rootNode.setName("Hidden Outline Root"); // NOI18N
        manager.setRootContext(rootNode);
    }

    
    public static OutlinePanel getDefault() {
        return instance;
    }
    
    
    // XXX #6486267 Delegating to the treeView.
    public void requestFocus() {
        treeView.requestFocus();
    }

    // XXX #6486267 Delegating to the treeView.
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }
    
    // ...method as before and getLookup
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public Lookup getLookup() {
        return lookup;
    }
    // ...methods as before, but replace componentActivated and
    // componentDeactivated with e.g.:
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(manager, true);
    }
    public void removeNotify() {
        ExplorerUtils.activateActions(manager, false);
        super.removeNotify();
    }    
    
    
    public void setActiveBeans(DesignBean[] designBeans) {
        if (DEBUG) {
            debugLog("designBeans=" + Arrays.asList(designBeans)); // NOI18N
        }
        
        if (designBeans.length == 0) {
//            ((OutlineRootNode)manager.getRootContext()).setDesignBean(null);
            outlineRootChildren.setDesignBean(null);
//            updateDesignProjectListening(null);
            
            selectNodesForDesignBeans(new DesignBean[0]);
        } else {
            // XXX First only?
//            ((OutlineRootNode)manager.getRootContext()).setDesignBean(designBeans[0]);
            outlineRootChildren.setDesignBean(designBeans[0]);

            selectNodesForDesignBeans(designBeans);
//            updateDesignProjectListening(rootBean.getDesignContext().getProject());
        }
    }

    private void selectNodesForDesignBeans(final DesignBean[] designBeans) {
        if (isShowing()) {
            doSelectNodesForDesignBeans(designBeans);
        } else {
            // XXX Neccessary in order to give a chance the component to open.
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // XXX The nodes are not inited when the component is not on the screen
                    // strange impl (that was supposed to be a model).
//                    if (OutlinePanel.this.isShowing()) {
                        doSelectNodesForDesignBeans(designBeans);
//                    }
                }
            });
        }
    }
    
    private void doSelectNodesForDesignBeans(DesignBean[] designBeans) {
        Node[] nodes = getNodesForBeans(designBeans);
        initializeParentNodes(nodes);
        
        if (!areNodesUnderRoot(nodes, manager.getRootContext())) {
            return;
        }
        
        expandNodes(nodes);
        scrollToNodes(nodes);
        
        // XXX Don't listen on these programatic changes.
        manager.removePropertyChangeListener(outlineManagerListener);
        try {
            manager.setSelectedNodes(nodes);
        } catch (PropertyVetoException pve) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, pve);
        } finally {
            manager.addPropertyChangeListener(outlineManagerListener);
        }
    }
    
    private static Node[] getNodesForBeans(DesignBean[] designBeans) {
        List<Node> nodes = new ArrayList<Node>();
        for (DesignBean designBean : designBeans) {
            Node node = OutlineUtilities.getNodeForDesignBean(designBean);
            if (node != null) {
                nodes.add(node);
            }
        }

        return nodes.toArray(new Node[nodes.size()]);
    }
    
    private void expandNodes(Node[] nodes) {
        treeView.expandNodes(nodes);
    }
    private void scrollToNodes(Node[] nodes) {
        treeView.scrollToNodes(nodes);
    }

    /** See the {@link #initializeParentNodes(Node) method. */
    private static void initializeParentNodes(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            initializeParentNodes(nodes[i]);
        }
    }
    /** XXX Assures the parents of this node are initialized.
     * The node structure parent vs. children might not be initialized.
     * @see org.openide.nodes.Children#getNodes(boolean) */
    private static void initializeParentNodes(Node node) {
        while (true) {
            DesignBean designBean = (DesignBean)node.getLookup().lookup(DesignBean.class);
            if (designBean == null) {
                return;
            }

            DesignBean parentBean = designBean.getBeanParent();
            if (parentBean == null) {
                return;
            }

            node = OutlineUtilities.getNodeForDesignBean(parentBean);
            // XXX This inits the children.
            node.getChildren().getNodes(true);
        }
    }
    
    private static boolean areNodesUnderRoot(Node[] nodes, Node root) {
        if (root == null || nodes == null || nodes.length == 0) {
            return false;
        }
        
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (root != NodeOp.findRoot(node)) {
                // XXX This happens in the sanity, couldn't reproduce. Therefore under DEBUG.
                // FIXME It looks like there is another not created for the page1 comp. Needs to be checked.
                if (DEBUG) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                            new IllegalStateException("Node=" + node + " is not under root=" + root // NOI18N
                            + "\n\n" + dumpNodeWithChildren(root, 0) // NOI18N
                            + "\n\n" + dumpNodeWithChildren(NodeOp.findRoot(node), 0))); // NOI18N
                }
                return false;
            }
        }
        
        return true;
    }
    
    private void dumpActivatedNodes() {
        log("Activated nodes:"); // NOI18N
        Node[] nodes = getExplorerManager().getSelectedNodes();
        if (nodes == null) {
            return;
        }
        for (Node node : nodes) {
            log(dumpNodeWithChildren(node, 0));
        }
    }
    
    private static String dumpNodeWithChildren(Node node, int tabs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + getTabs(tabs) + node); // NOI18N
        Node[] children = node.getChildren().getNodes(true);
        for (int i = 0; i < children.length; i++) {
            sb.append(dumpNodeWithChildren(children[i], tabs + 1));
        }
        return sb.toString();
    }
    
    private static String getTabs(int tabs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            sb.append('\t');
        }
        
        return sb.toString();
    }
    
    /** Logs debug message. Use only after checking <code>DEBUG</code> flag. */
    private static void debugLog(String message) {
        ErrorManager.getDefault().getInstance(OutlinePanel.class.getName()).log(message);
    }

    boolean isSelectionValid() {
        return areNodesUnderRoot(manager.getSelectedNodes(), manager.getRootContext());
    }

//    private void updateDesignProjectListening(DesignProject designProject) {
//        if (designProject == null) {
//            designProjectListener = null;
//            return;
//        }
//        
//        designProjectListener = new OutlineDesignProjectListener(this);
//        designProject.addDesignProjectListener(
//                (DesignProjectListener)WeakListeners.create(DesignProjectListener.class, designProjectListener, designProject));
//    }
    
    public HelpCtx getHelpCtx() {
        return ExplorerUtils.getHelpCtx(
                getExplorerManager().getSelectedNodes(),
                new HelpCtx("projrave_ui_elements_project_nav_about_app_outline")); // NOI18N
    }
    
    private static class OutlineTreeView extends BeanTreeView {
        
        private final OutlinePanel outlinePanel;
        public OutlineTreeView(OutlinePanel outlinePanel) {
            this.outlinePanel = outlinePanel;
        }

        @Override
        public String toString() {
            ExplorerManager manager = outlinePanel.manager;
            if (manager == null) {
                return super.toString();
            }
            Node[] nodes = manager.getSelectedNodes();
            return super.toString() + ", manager=" + manager + ", selectedNodes=" + (nodes == null ? null : Arrays.asList(nodes));
        }
        
        public void expandNodes(Node[] nodes) {
            TreePath[] treePaths = getTreePathsForNodes(nodes);
            for (int i = 0; i < treePaths.length; i++) {
                // XXX Silly Swing impl, if the last element is leaf,
                // the path is not expanded!
                TreePath expandPath = getTreePathToExpand(treePaths[i]);
                if (expandPath != null) {
                    tree.expandPath(expandPath);
                }
            }
        }
        
        public void scrollToNodes(Node[] nodes) {
            TreePath[] treePaths = getTreePathsForNodes(nodes);
            Rectangle rect = new Rectangle();
            
            if (areTreePathsVisible(treePaths)) {
                return;
            }
            
            for (int i = 0; i < treePaths.length; i++) {
                Rectangle bounds = tree.getPathBounds(treePaths[i]);
                if (bounds == null) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new NullPointerException("No bounds for treePath=" + treePaths[i])); // NOI18N
                } else {
                    rect.add(bounds);
                }
            }
            
            if (!rect.isEmpty()) {
                tree.scrollRectToVisible(rect);
            }
        }
        
        private boolean areTreePathsVisible(TreePath[] treePaths) {
            for (int i = 0; i < treePaths.length; i++) {
                if (!tree.isVisible(treePaths[i])) {
                    return false;
                }
            }
            return true;
        }
        
        private static TreePath getTreePathToExpand(TreePath original) {
            Object[] elements = original.getPath();
            if (elements.length < 2) {
                return null;
            } else {
                List<Object> newElements = new ArrayList<Object>(Arrays.asList(elements));
                newElements.remove(elements.length - 1);
                return new TreePath(newElements.toArray());
            }
        }
        
        private TreePath[] getTreePathsForNodes(Node[] nodes) {
            List<TreePath> treePaths = new ArrayList<TreePath>();
            for (Node node : nodes) {
                TreePath tp = getTreePathForNode(node);
                if (tp == null) {
                    continue;
                }
                treePaths.add(tp);
            }
            return treePaths.toArray(new TreePath[treePaths.size()]);
        }
        
        private TreePath getTreePathForNode(Node node) {
            TreeNode tn = Visualizer.findVisualizer(node);
            if (tn == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new NullPointerException("TreeNode not found for node=" + node)); // NOI18N
                return null;
            }

            TreeModel model = tree.getModel();
            if (!(model instanceof DefaultTreeModel)) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new NullPointerException("DefaultTreeModel not found, model=" + model)); // NOI18N
                return null;
            }

            return new TreePath(((DefaultTreeModel)model).getPathToRoot(tn));
	}
    } // End of OutlineTreeView.
    
    
//    private static class OutlineDesignProjectListener implements DesignProjectListener {
//        private final OutlinePanel outlinePanel;
//        
//        public OutlineDesignProjectListener(OutlinePanel outlinePanel) {
//            this.outlinePanel = outlinePanel;
//        }
//        
//        public void contextOpened(DesignContext designContext) {
//        }
//
//        public void contextClosed(DesignContext designContext) {
//            DesignBean designBean = ((OutlineRootNode)outlinePanel.getExplorerManager().getRootContext()).getDesignBean();
//            
//            if (designBean == null) {
//                return;
//            }
//            
//            if (designBean.getDesignContext() == designContext) {
//                // Our DesignContext was closed, clean the outline panel.
//                outlinePanel.setActiveBeans(new DesignBean[0]);
//            }
//        }
//    } // End of OutlineDesignProjectListener.
    
    
    private static class OutlineRootChildren extends Children.Keys<DesignBean> {

        private final DesignProjectListener designProjectListener = new OutlineDesignProjectListener(this);
        
        private DesignContextListener designContextListener;
        
        private final OutlinePanel outlinePanel;
        
        
        /** Current root (Page) design bean. */
        private DesignBean rootDesignBean;
        
        
        public OutlineRootChildren(OutlinePanel outlinePanel) {
            this.outlinePanel = outlinePanel;
        }

//        protected void addNotify() {
//            super.addNotify();
//            setKeys(Collections.EMPTY_SET);
//        }
//        
//        protected void removeNotify() {
//            setKeys(Collections.EMPTY_SET);
//            super.removeNotify();
//        }
        
        protected Node[] createNodes(DesignBean key) {
            if (DEBUG) {
                debugLog("creatingNode for key=" + key); // NOI18N
            }
            return key == null ? new Node[0] : new Node[] {OutlineUtilities.getNodeForDesignBean(key)};
        }

        private void setDesignBean(DesignBean designBean) {
            setRootDesignBean(designBean == null ? null : designBean.getDesignContext().getRootContainer());
        }
        
        private void setRootDesignBean(DesignBean rootDesignBean) {
            if (this.rootDesignBean == rootDesignBean) {
                return;
            }

            removeDesignProjectListening(this.rootDesignBean);
            
            this.rootDesignBean = rootDesignBean;
            updateKeys();
            
            addDesignProjectListening(this.rootDesignBean);
        }
        
        private void updateKeys() {
            Collection<DesignBean> keys = getAllBeansCollection(rootDesignBean);
            setKeys(keys);

            // #6463783.
            updateDesignContextListening(keys);

//            // XXX Bean nodes may be changed! After refresh, 
//            // the original nodes are destroyed.
//            for (Iterator it = keys.iterator(); it.hasNext(); ) {
//                refreshKey(it.next());
//            }
        }

        private void addDesignProjectListening(DesignBean designBean) {
            if (designBean == null) {
                return;
            }

            DesignContext designContext = designBean.getDesignContext();
            if (designContext == null) {
                // XXX Invalid already
                return;
            }
            DesignProject designProject = designContext.getProject();
            if (designProject == null) {
                // XXX Invalid already.
                return;
            }
            designProject.addDesignProjectListener(designProjectListener);
        }
        
        private void removeDesignProjectListening(DesignBean designBean) {
            if (designBean == null) {
                return;
            }
            
            DesignContext designContext = designBean.getDesignContext();
            if (designContext == null) {
                // XXX Invalid already
                return;
            }
            DesignProject designProject = designContext.getProject();
            if (designProject == null) {
                // XXX Invalid already.
                return;
            }
            designProject.removeDesignProjectListener(designProjectListener);
        }
        
        private void updateDesignContextListening(Collection<DesignBean> designBeans) {
            designContextListener = null;
            
            Set<DesignContext> designContexts = new HashSet<DesignContext>();
            for (DesignBean designBean : designBeans) {
                designContexts.add(designBean.getDesignContext());
            }

            if (!designContexts.isEmpty()) {
                designContextListener = new OutlineDesignContextListener(outlinePanel);
            }
            for (DesignContext designContext : designContexts) {
                if (rootDesignBean != null && designContext == rootDesignBean.getDesignContext()) {
                    // XXX Skipping the context representing the Page.
                    continue;
                }
                designContext.addDesignContextListener(
                        (DesignContextListener)WeakListeners.create(DesignContextListener.class, designContextListener, designContext));
            }
        }
        
        // For performance improvement. No need to get all the contexts in the project
        private static DesignContext[] getDesignContexts(DesignContext context){
            DesignProject designProject = context.getProject();
            DesignContext[] contexts;
            if (designProject instanceof FacesDesignProject) {
                contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                    "request",
                    "session",
                    "application"
                });
            } else {
                contexts = new DesignContext[0];
            }
            DesignContext[] designContexts = new DesignContext[contexts.length + 1];
            designContexts[0] = context;
            System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
            return designContexts;
        }

        
        private static Collection<DesignBean> getAllBeansCollection(DesignBean designBean) {
            if (designBean == null) {
                return Collections.emptyList();
            }

            List<DesignBean> beans = new ArrayList<DesignBean>();
            beans.add(designBean);

            // TODO Also provide children for Application, Session, Request beans.
            DesignProject project = designBean.getDesignContext().getProject();

            if (project == null) {
                return Collections.emptyList();
            }
            
            //DesignContext[] contexts = project.getDesignContexts();
            // XXX This is supposed to get the design context except the page bean ones, better name needed?.
            DesignContext[] contexts;
            DesignContext designContext = designBean.getDesignContext();
            if (designContext == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Design bean returns null design context, designBean=" + designBean)); // NOI18N
                contexts = new DesignContext[0];
            } else {
                contexts = getDesignContexts(designContext);
            }

            List<DesignContext> auxiliaryContexts = new ArrayList<DesignContext>();
//            for (DesignContext context : contexts) {
//                Object scope = context.getContextData(Constants.ContextData.SCOPE);
//                // XXX Missing designtime API, do not use just strings!
//                if (/*"request".equals(scope) ||*/ "application".equals(scope) // NOI18N
//                || "session".equals(scope)  || "none".equals(scope)) { // NOI18N
//                    auxiliaryContexts.add(context);
//                } else if ("request".equals(scope)) { // NOI18N
//                    if (!InsyncAccessor.isPageRootContainerDesignBean(context.getRootContainer())) {
//                        auxiliaryContexts.add(context);
//                    }
//                }
//            }
            auxiliaryContexts.addAll(Arrays.asList(contexts));

            Collections.sort(auxiliaryContexts, new AuxiliaryContextComparator());

            for (DesignContext context : auxiliaryContexts) {
                DesignBean rootBean = context.getRootContainer();
                // XXX There cannot be the same keys twice.
                if (beans.contains(rootBean)) {
                    continue;
                }
                beans.add(rootBean);
            }

            if (DEBUG) {
                debugLog("beans=" + beans);
            }

            return new ArrayList<DesignBean>(beans);
        }
        
        
        private static class OutlineDesignProjectListener implements DesignProjectListener {
            private final OutlineRootChildren outlineRootChildren;
            
            public OutlineDesignProjectListener(OutlineRootChildren outlineRootChildren) {
                this.outlineRootChildren = outlineRootChildren;
            }
            
            public void contextOpened(DesignContext designContext) {
                updateChildren();
            }

            public void contextClosed(DesignContext designContext) {
                updateChildren();
            }
            
            private void updateChildren() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        outlineRootChildren.updateKeys();
                    }
                });
            }
        } // End of OutlineDesignProjectListener.

        
        private static class OutlineDesignContextListener implements DesignContextListener {
            private final OutlinePanel outlinePanel;
            
            public OutlineDesignContextListener(OutlinePanel outlinePanel) {
                this.outlinePanel = outlinePanel;
            }
            
            public void contextActivated(DesignContext designContext) {
            }

            public void contextDeactivated(DesignContext designContext) {
            }

            public void contextChanged(DesignContext designContext) {
            }

            public void beanCreated(DesignBean designBean) {
                expandNodeForDesignBean(designBean);
            }

            public void beanDeleted(DesignBean designBean) {
            }

            public void beanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
            }

            public void beanContextActivated(DesignBean designBean) {
            }

            public void beanContextDeactivated(DesignBean designBean) {
            }

            public void instanceNameChanged(DesignBean designBean, String string) {
            }

            public void beanChanged(DesignBean designBean) {
            }

            public void propertyChanged(DesignProperty designProperty, Object object) {
            }

            public void eventChanged(DesignEvent designEvent) {
            }
            
            private void expandNodeForDesignBean(final DesignBean designBean) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Node[] nodes = getNodesForBeans(new DesignBean[] {designBean});
                        initializeParentNodes(nodes);
                        if (areNodesUnderRoot(nodes, outlinePanel.getExplorerManager().getRootContext())) {
                            outlinePanel.expandNodes(nodes);
                        }
                    }
                });
            }
        } // End of OutlineDesignContextListener.
        
        
        /** Compares the auxiliary <code>DesignContext</code>s to order according
         * request, session, application and none scope. */
        private static class AuxiliaryContextComparator implements Comparator<DesignContext> {

            public int compare(DesignContext dc1, DesignContext dc2) {
                int scopeWeight1 = getScopeWeight(dc1);
                int scopeWeight2 = getScopeWeight(dc2);

                return scopeWeight2 - scopeWeight1;
            }

            private static int getScopeWeight(DesignContext designContext) {
                Object scope = designContext.getContextData(Constants.ContextData.SCOPE);
                
                // XXX Missing designtime API, there shouldn't be used strings!
                if ("request".equals(scope)) { // NOI18N
                    return 4;
                } else if ("session".equals(scope)) { // NOI18N
                    return 3;
                } else if ("application".equals(scope)) { // NOI18N
                    return 2;
                } else if ("none".equals(scope)) { // NOI18N
                    return 1;
                } else {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new IllegalStateException("The design context doesn't provide valid scope, designContext=" + designContext
                            + ", scope=" + scope)); // NOI18N
                    return 0;
                }

            }
        } // End of AuxiliaryContextComparator.
        
        
    } // End of OutlineRootChildren.


    private static void log(String message) {
        Logger logger = getLogger();
        logger.log(Level.INFO, message);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(OutlinePanel.class.getName());
    }
}

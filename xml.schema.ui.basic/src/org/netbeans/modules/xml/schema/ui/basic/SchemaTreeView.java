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

package org.netbeans.modules.xml.schema.ui.basic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.nodes.DefaultExpandedCookie;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.StructuralSchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;

/**
 * Represents the schema model using a tree view.
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Nathan Fiedler
 * @author  Jeri Lockhart
 */
public class SchemaTreeView extends JPanel
        implements ExplorerManager.Provider, Lookup.Provider,
        PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    private ExplorerManager explorerManager;
    private Lookup lookup;
    private TreeView treeView;

    public static enum ViewType {
        /** Use the categorized node factory */
        CATEGORIZED,
        /** Use the structural node factory */
        STRUCTURAL;
    }

    /**
     * Creates a new instance of SchemaTreeView.
     *
     * @param  model     schema model.
     * @param  viewType  type of view (categorized, structural).
     * @param  lookup    the Lookup for this view.
     */
    public SchemaTreeView(SchemaModel model, ViewType viewType, Lookup lookup) {
        super(new BorderLayout());
        treeView = new BeanTreeView();
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SchemaTreeCategory.class,
                "LBL_SchemaCategory_Tree"));
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SchemaTreeCategory.class,
                "HINT_SchemaCategory_Tree"));
        treeView.setRootVisible(true);
        treeView.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        treeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(treeView, BorderLayout.CENTER);

        SchemaNodeFactory factory;
        switch (viewType) {
            case CATEGORIZED:
                factory = new CategorizedSchemaNodeFactory(model, lookup);
                break;
            case STRUCTURAL:
                factory = new StructuralSchemaNodeFactory(model, lookup);
                break;
            default:
                factory = null;
                break;
        }
        Node rootNode = factory.createRootNode();
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(rootNode);
        // Listen to changes in the selection
        explorerManager.addPropertyChangeListener(this);

        // Set up the map of standard actions.
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction,
                ExplorerUtils.actionCopy(explorerManager));
        map.put(DefaultEditorKit.cutAction,
                ExplorerUtils.actionCut(explorerManager));
        map.put(DefaultEditorKit.pasteAction,
                ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, false));
        // This lookup is sufficient for us, no need to include the lookup
        // coming from the argument list.
        this.lookup = ExplorerUtils.createLookup(explorerManager, map);
        // Do _not_ define the keyboard shortcuts for the actions, as
        // they are in the lookup of our containing TopComponent, and
        // those are activated by the standard keyboard bindings. If we
        // define our own here, we get exceptions in OwnPaste (IZ#80500).

        // Expand the default nodes
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                expandDefaultNodes();
            }
        });
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

    /**
     * Expand the nodes which should be expanded by default.
     */
    private void expandDefaultNodes() {
        Node rootNode = getExplorerManager().getRootContext();
        // Need to prevent looping on malformed trees, so avoid going too
        // deep when expanding the children of nodes with only one child.
        int depth = 0;
        do {
            Node[] children = rootNode.getChildren().getNodes();
            if (children.length == 1) {
                // Expand all nodes that have only a single child.
                treeView.expandNode(children[0]);
                rootNode = children[0];
                depth++;
            } else {
                // Expand all first-level children that are meant to be shown
                // expanded by default.
                for (Node child : children) {
                    DefaultExpandedCookie cookie = (DefaultExpandedCookie)
                    child.getCookie(DefaultExpandedCookie.class);
                    if (cookie != null && cookie.isDefaultExpanded()) {
                        treeView.expandNode(child);
                    }
                }
                rootNode = null;
            }
        } while (rootNode != null && depth < 5);

        // The following code addresses two issues:
        //
        // 1. When viewing large schemas, expanding the default set of nodes
        //    generally means that the contents of the view are so long that
        //    copious amounts of scrolling are necessary to see it all. This is
        //    not desirable for the user's first experience with the document.
        //
        // 2. Because BasicTreeUI essentially ignores the scrollsOnExpand
        //    setting (or at least it does not work as documented), the tree
        //    is left scrolled to some random position.
        //
        // So, if scrolling is necessary, then collapse root's children.
        JTree tree = (JTree) treeView.getViewport().getView();
        if (tree.getRowCount() > tree.getVisibleRowCount()) {
            rootNode = getExplorerManager().getRootContext();
            Enumeration kids = rootNode.getChildren().nodes();
            while (kids.hasMoreElements()) {
                Node kid = (Node) kids.nextElement();
                treeView.collapseNode(kid);
            }
        }
    }

    /**
     * Finds the TopComponent that contains us.
     *
     * @return  the parent TopComponent.
     */
    private TopComponent findParentTopComponent() {
        Component parent = getParent();
        while (parent != null) {
            if (parent instanceof TopComponent) {
                return (TopComponent) parent;
            } else {
                parent = parent.getParent();
            }
        }
        return null;
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(
                event.getPropertyName())) {
            Node[] filteredNodes = (Node[]) event.getNewValue();
            if (filteredNodes != null && filteredNodes.length >= 1) {
                // Set the active nodes for the parent TopComponent.
                TopComponent tc = findParentTopComponent();
                if (tc != null) {
                    tc.setActivatedNodes(filteredNodes);
                }
            }
        }
    }

    public void showComponent(SchemaComponent sc) {
        List<Node> path = UIUtilities.findPathFromRoot(
                getExplorerManager().getRootContext(), sc);
        if (path == null || path.isEmpty()) {
            return;
        }
        Node node = path.get(path.size() - 1);
        // If using the explorer manager to show selection does not work,
        // use the following code instead.
//        JTree tree = (JTree) treeView.getViewport().getView();
//        NodeTreeModel model = (NodeTreeModel) tree.getModel();
//        TreeNode tn = Visualizer.findVisualizer(node);
//        TreeNode[] tnp = model.getPathToRoot(tn);
//        TreePath treePath = new TreePath(tnp);
//        tree.setSelectionPath(treePath);
//        tree.scrollPathToVisible(treePath);
        try {
            getExplorerManager().setSelectedNodes(new Node[] { node });
        } catch (PropertyVetoException pve) {
        }
    }
}

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

package org.netbeans.modules.xml.wsdl.ui.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLModelCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Navigator component containing a tree of WSDL components.
 *
 * @author  Nathan Fiedler
 */
public class WSDLNavigatorContent extends JPanel
        implements ExplorerManager.Provider, Runnable, PropertyChangeListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
//    /** The lookup for our component tree. */
//    private static Lookup lookup;
    /** Explorer manager for the tree view. */
    private ExplorerManager explorerManager;
    /** Our component node tree view. */
    private TreeView treeView;
    /** Root node of the tree. */
    private Node rootNode;
    
    private final JLabel notAvailableLabel = new JLabel(
            NbBundle.getMessage(WSDLNavigatorContent.class, "MSG_NotAvailable"));

//    static {
//        // Present a read-only view of the components.
//        lookup = Lookups.singleton(new ReadOnlyCookie(true));
//    }
    
    private static WSDLNavigatorContent content;
    

    /**
     * Creates a new instance of WSDLNavigatorContent.
     */
    private WSDLNavigatorContent() {
        setLayout(new BorderLayout());
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        notAvailableLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailableLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        notAvailableLabel.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailableLabel.setOpaque(true);
    }
    
    public static WSDLNavigatorContent getDefault() {
        if (content == null) {
            synchronized (WSDLNavigatorContent.class) {
                if (content == null) {
                    content = new WSDLNavigatorContent();
                }
            }
        }
        return content;
    }

//    /**
//     * Expand the nodes which should be expanded by default.
//     */
//    protected void expandDefaultNodes() {
//        Node rootNode = getExplorerManager().getRootContext();
//        // Need to prevent looping on malformed trees, so avoid going too
//        // deep when expanding the children of nodes with only one child.
//        int depth = 0;
//        do {
//            Node[] children = rootNode.getChildren().getNodes();
//            if (children.length == 1) {
//                // Expand all nodes that have only a single child.
//                treeView.expandNode(children[0]);
//                rootNode = children[0];
//                depth++;
//            } else {
//                // Expand all first-level children that are meant to be shown
//                // expanded by default.
//                for (Node child : children) {
//                    DefaultExpandedCookie cookie = (DefaultExpandedCookie)
//                    child.getCookie(DefaultExpandedCookie.class);
//                    if (cookie != null && cookie.isDefaultExpanded()) {
//                        treeView.expandNode(child);
//                    }
//                }
//                rootNode = null;
//            }
//        } while (rootNode != null && depth < 5);
//
//        // The following code addresses two issues:
//        //
//        // 1. When viewing large files, expanding the default set of nodes
//        //    generally means that the contents of the column are so long that
//        //    copious amounts of scrolling are necessary to see it all. This is
//        //    not desirable for the user's first experience with the document.
//        //
//        // 2. Because BasicTreeUI essentially ignores the scrollsOnExpand
//        //    setting (or at least it does not work as documented), the tree
//        //    is left scrolled to some random position.
//        //
//        // So, if scrolling is necessary, then collapse root's children.
//        JTree tree = (JTree) treeView.getViewport().getView();
//        if (tree.getRowCount() > tree.getVisibleRowCount()) {
//            rootNode = getExplorerManager().getRootContext();
//            Enumeration kids = rootNode.getChildren().nodes();
//            while (kids.hasMoreElements()) {
//                Node kid = (Node) kids.nextElement();
//                treeView.collapseNode(kid);
//            }
//        }
//    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     * Show the data object in the navigator.
     *
     * @param  dobj  data object to show.
     */
    public void navigate(DataObject dobj) {
        WSDLModel model = null;
        try {
            WSDLModelCookie modelCookie = dobj.getCookie(WSDLModelCookie.class);
            if (modelCookie != null) {
                model = modelCookie.getModel();
                if (model != null) {
                    model.removePropertyChangeListener(this);
                    model.addPropertyChangeListener(this);
                }
            }
        } catch (IOException ioe) {
            // Show a blank page if there is an error.
        }
        if (model == null || model.getState() != WSDLModel.State.VALID) {
            showError();
        } else {
            show(model);
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

    public void run() {
        // Initially expand root node and the folder nodes below it.
        treeView.expandNode(rootNode);
        Utility.expandNodes(treeView, 1, rootNode);
        selectActivatedNodes();
        revalidate();
        repaint();
    }

    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getPropertyName();
        if (WSDLModel.STATE_PROPERTY.equals(property)) {
            State newState = (State) event.getNewValue();
            if (newState == WSDLModel.State.VALID) {
                WSDLModel model = (WSDLModel) event.getSource();
                show(model);
            } else {
                showError();
            }
            return;
        }
        TopComponent tc = (TopComponent) SwingUtilities.
                getAncestorOfClass(TopComponent.class, this);
        if (tc != null) {
            boolean isActivatedTC = (tc == TopComponent.getRegistry().getActivated());
            if (ExplorerManager.PROP_SELECTED_NODES.equals(property) &&
                    isActivatedTC) {
                Node[] filteredNodes = (Node[])event.getNewValue();
                if (filteredNodes != null && filteredNodes.length >= 1) {
                    // Set the active nodes for the parent TopComponent.
                    tc.setActivatedNodes(filteredNodes);
                    repaint();
                }
            } else if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(property) &&
                    !isActivatedTC) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        selectActivatedNodes();
                        repaint();
                    }
                });
            } else if (TopComponent.Registry.PROP_ACTIVATED.equals(property) &&
                    isActivatedTC) {
                tc.setActivatedNodes(getExplorerManager().getSelectedNodes());
                repaint();
            }
        }
    }

    private void selectActivatedNodes() {
        Node[] activated = TopComponent.getRegistry().getActivatedNodes();
        List<Node> selNodes = new ArrayList<Node>();
        for (Node n : activated) {
            WSDLComponent wc = n.getLookup().lookup(WSDLComponent.class);
            if (wc != null) {
                List<Node> path = UIUtilities.findPathFromRoot(
                        getExplorerManager().getRootContext(), wc);
                if (path != null && !path.isEmpty()) {
                    selNodes.add(path.get(path.size() - 1));
                }
            }
        }
        try {
            getExplorerManager().setSelectedNodes(
                    selNodes.toArray(new Node[0]));
        } catch (PropertyVetoException pve) {
        }
    }

    /**
     * Display the "not available" message in place of the tree view.
     */
    private void showError() {
        if (notAvailableLabel.isShowing()) {
            return;
        }
        if (treeView != null && treeView.isShowing()) remove(treeView);
        add(notAvailableLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Show the tree view for the given model.
     *
     * @param  model  component model to display.
     */
    private void show(WSDLModel model) {
        remove(notAvailableLabel);
        treeView = new BeanTreeView();
        add(treeView, BorderLayout.CENTER);
        NodesFactory factory = NodesFactory.getInstance();
        rootNode = factory.create(model.getDefinitions());
        getExplorerManager().setRootContext(rootNode);
        EventQueue.invokeLater(this);
    }

    public void release() {
        //cleanup all the elements in the navigator.
        removeAll();

        Node dummyNode = new AbstractNode(Children.LEAF);
        getExplorerManager().setRootContext(dummyNode);
        getExplorerManager().setExploredContext(dummyNode);
        
        rootNode = null;
        treeView = null;
    }
}

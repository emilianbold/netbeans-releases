/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;

/**
 * Panel that displays DOM tree of the inspected page.
 *
 * @author Jan Stola
 */
public class DomPanel extends JPanel implements ExplorerManager.Provider {
    /** Request processor used for interaction with {@code PageModel}. */
    private static final RequestProcessor RP = new RequestProcessor(DomPanel.class.getName(), 5);
    /** Explorer manager provided by this panel. */
    private ExplorerManager manager = new ExplorerManager();
    /** Tree view that displays the DOM tree. */
    private BeanTreeView treeView;
    /** Label used when no DOM tree is available. */
    private JLabel noDomLabel;
    /** Page model used by this panel. */
    private PageModel pageModel;
    /** Context actions (actions shown when the panel/view is right-clicked) of this panel. */
    private Action[] contextActions;
    /** Determines whether we are just updating view from the model. */
    private boolean updatingView = false;

    /**
     * Creates a new {@code DomPanel}.
     * 
     * @param pageModel page model for the panel (can be {@code null}).
     */
    public DomPanel(PageModel pageModel) {
        this.pageModel = pageModel;
        setLayout(new BorderLayout());
        initTreeView();
        initNoDOMLabel();
        add(noDomLabel);
        if (pageModel != null) {
            pageModel.addPropertyChangeListener(createModelListener());
            manager.addPropertyChangeListener(createSelectedNodesListener());
            update(true);
            updateHighlight();
        }
    }

    /**
     * Initializes the tree view.
     */
    private void initTreeView() {
        treeView = new BeanTreeView() {
            {
                MouseAdapter listener = createTreeMouseListener();
                tree.addMouseListener(listener);
                tree.addMouseMotionListener(listener);
                tree.setCellRenderer(createTreeCellRenderer(tree.getCellRenderer()));
            }
        };
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
    }

    /**
     * Creates a mouse listener for the DOM tree.
     * 
     * @return mouse listener for the DOM tree.
     */
    private MouseAdapter createTreeMouseListener() {
        return new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                processEvent(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                processEvent(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                processEvent(null);
                // Make sure that lastHover != <any potential value>
                // i.e., make sure that change in hover is triggered when
                // mouse returns into this component
                lastHover = new Object();
            }

            // The last node we were hovering over.
            private Object lastHover = null;
            
            /**
             * Processes the specified mouse event.
             * 
             * @param e mouse event to process.
             */
            private void processEvent(MouseEvent e) {
                Object hover = null;
                if (e != null) {
                    JTree tree = (JTree)e.getSource();
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if  (path != null) {
                        hover = path.getLastPathComponent();
                    }
                }
                if (hover != lastHover) {
                    lastHover = hover;
                    final List<? extends Node> highlight;
                    if (hover != null) {
                        Node node = Visualizer.findNode(hover);
                        highlight = Arrays.asList(node);
                    } else {
                        highlight = Collections.EMPTY_LIST;
                    }
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            pageModel.setHighlightedNodes(highlight);
                        }
                    });
                }
            }
            
        };
    }

    /**
     * Highlighted (visualizer) nodes.
     * This collection is for rendering purposes only.
     */
    final List highlightedTreeNodes = new ArrayList();

    /**
     * Forces update of the set of highlighted nodes.
     */
    final void updateHighlight() {
        synchronized (highlightedTreeNodes) {
            highlightedTreeNodes.clear();
            for (Node node : pageModel.getHighlightedNodes()) {
                TreeNode visualizer = Visualizer.findVisualizer(node);
                highlightedTreeNodes.add(visualizer);
            }
            treeView.repaint();
        }
    }

    /**
     * Determines whether the given (visualizer) node is highlighted.
     * 
     * @param treeNode (visualizer) node to check.
     * @return {@code true} when the specified node should be highlighted,
     * returns {@code false} otherwise.
     */
    boolean isHighlighted(Object treeNode) {
        synchronized (highlightedTreeNodes) {
            return highlightedTreeNodes.contains(treeNode);
        }
    } 

    /**
     * Creates a cell renderer for the DOM tree.
     * 
     * @param delegate delegating/original tree renderer.
     * @return call renderer for the DOM tree.
     */
    private TreeCellRenderer createTreeCellRenderer(final TreeCellRenderer delegate) {
        return new DefaultTreeCellRenderer() {
            // Color used for hovering highlight
            private Color hoverColor = UIManager.getColor("Tree.selectionBackground").brighter().brighter(); // NOI18N

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel component;
                if (!selected && highlightedTreeNodes.contains(value)) {
                    component = (JLabel)delegate.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, hasFocus);
                    component.setBackground(hoverColor);
                    component.setOpaque(true);
                } else {
                    component = (JLabel)delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                }
                return component;
            }
        };
    }

    /**
     * Initializes the "no DOM available" label.
     */
    private void initNoDOMLabel() {
        noDomLabel = new JLabel();
        noDomLabel.setText(NbBundle.getMessage(DomPanel.class, "DomPanel.noDomLabel.text")); // NOI18N
        noDomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noDomLabel.setVerticalAlignment(SwingConstants.CENTER);
        noDomLabel.setEnabled(false);
        noDomLabel.setBackground(treeView.getViewport().getView().getBackground());
        noDomLabel.setOpaque(true);
    }

    /**
     * Updates the content of the panel. It fetches the current data
     * from the model and updates the view accordingly.
     * 
     * @param rebuild determines if the view should be rebuilt completely
     * or whether it should attempt to keep the current expand/collapse state
     * of the nodes displayed currently.
     */
    private void update(final boolean rebuild) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                // Make sure that the document node is created
                pageModel.getDocumentNode();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        update(rebuild, null);
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
//                                updateSelectionFromModel(false);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Updates the content of the panel.
     * 
     * @param rebuild determines if the view should be rebuilt completely
     * or whether it should attempt to keep the current expand/collapse state
     * of the nodes displayed currently.
     * @param document new DOM that should be shown in the panel.
     */
    private void update(boolean rebuild, Document document) {
        updatingView  = true;
        try {
//            if (document == null) {
//                Node root = new AbstractNode(Children.LEAF);
//                replace(treeView, noDomLabel);
//                manager.setRootContext(root);
//            } else {
                manager.setRootContext(pageModel.getDocumentNode());
//                Element documentElement = document.getDocumentElement();
//                if (rebuild) {
//                    ElementNode root = new ElementNode();
//                    root.setElement(documentElement);
//                    manager.setRootContext(new FakeRootNode(root, getContextActions()));
//                } else {
//                    // Trying to keep the selected nodes and expanded branches in the tree view
//                    FakeRootNode fakeRoot = (FakeRootNode)manager.getRootContext();
//                    ElementNode oldRoot = (ElementNode)fakeRoot.getRealRoot();
//                    oldRoot.setElement(documentElement);
//                }
                replace(noDomLabel, treeView);
//            }
        } finally {
            updatingView = false;
        }
    }

    /**
     * Helper method that replaces one component by another (if it is necessary).
     * 
     * @param componentToHide component that should be hidden.
     * @param componentToShow component that should be shown.
     */
    private void replace(Component componentToHide, Component componentToShow) {
        if (componentToHide.getParent() != null) {
            remove(componentToHide);
            add(componentToShow);
            revalidate();
            repaint();
        }
    }

    /**
     * Returns context actions, i.e., the actions that are shown when
     * the tree view is right-clicked.
     * 
     * @return context actions.
     */
    private Action[] getContextActions() {
        if (contextActions == null) {
            contextActions = new Action[] {
                createRefreshAction()
            };
        }
        return contextActions;
    }

    /**
     * Creates an action that refreshes the content of this panel.
     * 
     * @return action that refreshes the content of this panel.
     */
    private Action createRefreshAction() {
        String name = NbBundle.getMessage(DomPanel.class, "DomPanel.refreshAction"); // NOI18N
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                update(false);
            }
        };
    }

    /**
     * Creates {@code PageModel} listener.
     * 
     * @return {@code PageModel} listener.
     */
    private PropertyChangeListener createModelListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (PageModel.PROP_SELECTED_NODES.equals(propName)) {
                    List<? extends Node> nodes = pageModel.getSelectedNodes();
                    final Node[] selection = nodes.toArray(new Node[nodes.size()]);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updatingView = true;
                            try {
                                manager.setSelectedNodes(selection);
                            } catch (PropertyVetoException pvex) {
                                Logger.getLogger(DomPanel.class.getName()).log(Level.INFO, null, pvex);
                            } finally {
                                updatingView = false;
                            }
                        }
                    });
                } else if (PageModel.PROP_HIGHLIGHTED_NODES.equals(propName)) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateHighlight();
                        }
                    });
                } else if (PageModel.PROP_DOCUMENT.equals(propName)) {
                    update(true);
                }
            }
        };
    }

//    /**
//     * Updates selection in this view from the model.
//     * 
//     * @param triggeredByModel determines whether this selection update
//     * was triggered by selection change in the model.
//     */
//    private void updateSelectionFromModel(boolean triggeredByModel) {
//        Collection<ElementHandle> selection = pageModel.getSelectedElements();
//        final List<Node> nodeSelection = new ArrayList<Node>();
//        for (ElementHandle handle : selection) {
//            Node root = manager.getRootContext();
//            if (root instanceof FakeRootNode) {
//                root = ((FakeRootNode)root).getRealRoot();
//            }
//            if (root instanceof ElementNode) {
//                ElementNode node = ((ElementNode)root).locate(handle);
//                if (node == null) {
//                    if (triggeredByModel) {
//                        // Selected node not found => try to refresh the view
//                        // from the model to get the missing node
//                        update(false);
//                        // No need to continue, selection will be updated
//                        // as a result of DOM tree update
//                        return;
//                    }
//                } else {
//                    nodeSelection.add(node);
//                }
//            }
//        }
//        EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                updatingView = true;
//                try {
//                    manager.setSelectedNodes(nodeSelection.toArray(new Node[nodeSelection.size()]));
//                } catch (PropertyVetoException pvex) {
//                    Logger.getLogger(DomPanel.class.getName()).log(Level.INFO, null, pvex);
//                } finally {
//                    updatingView = false;
//                }
//            }
//        });
//    }

    /**
     * Creates a listener for selected nodes.
     * 
     * @return listener for selected nodes.
     */
    private PropertyChangeListener createSelectedNodesListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(propName)) {
                    if (updatingView) {
                        // This change was triggered by update from the model
                        // => no need to synchronize back into the model.
                        return;
                    }
                    final Node[] nodes = manager.getSelectedNodes();
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            pageModel.setSelectedNodes(Arrays.asList(nodes));
                        }
                    });
                }
            }
        };
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
}

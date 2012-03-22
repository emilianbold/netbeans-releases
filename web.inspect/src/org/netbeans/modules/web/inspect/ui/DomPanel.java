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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.modules.web.inspect.ElementHandle;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Panel that displays DOM Tree of the inspected page.
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
    /** Determines whether we are just updating selection from the model. */
    private boolean updatingSelection = false;

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
        }
    }

    /**
     * Initializes the tree view.
     */
    private void initTreeView() {
        treeView = new BeanTreeView();
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
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
                final Document document = pageModel.getDocument();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        update(rebuild, document);
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                updateSelectionFromModel();
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
        if (document == null) {
            Node root = new AbstractNode(Children.LEAF);
            replace(treeView, noDomLabel);
            manager.setRootContext(root);
        } else {
            Element documentElement = document.getDocumentElement();
            if (rebuild) {
                ElementNode root = new ElementNode();
                root.setElement(documentElement);
                manager.setRootContext(new FakeRootNode(root, getContextActions()));
            } else {
                // Trying to keep the selected nodes and expanded branches in the tree view
                FakeRootNode fakeRoot = (FakeRootNode)manager.getRootContext();
                ElementNode oldRoot = (ElementNode)fakeRoot.getRealRoot();
                oldRoot.setElement(documentElement);
            }
            replace(noDomLabel, treeView);
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
                if (PageModel.PROP_SELECTED_ELEMENTS.equals(propName)) {
                    updateSelectionFromModel();
                }
            }
        };
    }

    /**
     * Updates selection in this view from the model.
     */
    private void updateSelectionFromModel() {
        Collection<ElementHandle> selection = pageModel.getSelectedElements();
        final List<Node> nodeSelection = new ArrayList<Node>();
        for (ElementHandle handle : selection) {
            Node root = manager.getRootContext();
            if (root instanceof FakeRootNode) {
                root = ((FakeRootNode)root).getRealRoot();
            }
            if (root instanceof ElementNode) {
                ElementNode node = ((ElementNode)root).locate(handle);
                if (node != null) {
                    nodeSelection.add(node);
                }
            }
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                updatingSelection = true;
                try {
                    manager.setSelectedNodes(nodeSelection.toArray(new Node[nodeSelection.size()]));
                } catch (PropertyVetoException pvex) {
                    Logger.getLogger(DomPanel.class.getName()).log(Level.INFO, null, pvex);
                } finally {
                    updatingSelection = false;
                }
            }
        });
    }

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
                    if (updatingSelection) {
                        // This change was triggered by update from the model
                        // => no need to synchronize back into the model.
                        // In fact, synchronization back into the model could be
                        // harmful when some part of the selection is not found among
                        // the displayed nodes (when the view is not 100% up to date).
                        return;
                    }
                    Node[] nodes = manager.getSelectedNodes();
                    final List<ElementHandle> elements = new ArrayList<ElementHandle>(nodes.length);
                    for (Node node : nodes) {
                        ElementHandle handle = node.getLookup().lookup(ElementHandle.class);
                        if (handle != null) {
                            elements.add(handle);
                        }
                    }
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            pageModel.setSelectedElements(elements);
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

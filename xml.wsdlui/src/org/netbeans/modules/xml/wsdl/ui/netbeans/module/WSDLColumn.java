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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.DefinitionsNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.DummyDefinitionsNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.FolderNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.netbeans.modules.xml.xam.ui.column.Column;
import org.netbeans.modules.xml.xam.ui.column.ColumnListCellRenderer;
import org.netbeans.modules.xml.xam.ui.column.ColumnListView;
import org.netbeans.modules.xml.xam.ui.column.ColumnProvider;
import org.openide.explorer.view.ListView;
import org.openide.nodes.Children;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.NbBundle;

/**
 * Represents a column in the WSDLColumnsView, displaying the details of
 * a WSDLElementNode.
 * 
 * @author Todd Fast, todd.fast@sun.com
 * @author Nathan Fiedler
 * @author Jeri Lockhart
 */
public class WSDLColumn extends JPanel
        implements ExplorerManager.Provider, Lookup.Provider, NodeListener,
        PropertyChangeListener, Column, FocusListener {
    private static final long serialVersionUID = 1L;
    private WSDLColumnsView columnView;
    private ExplorerManager explorerManager;
    private transient Lookup lookup;
    private ListView nodeView;
    public static final String WSDL_COLUMN_NODE_SELECTED = "wsdl-column-node-selected"; //NOI18N
    private JLabel noChildrenLabel;

    /**
     * Creates a new instance of WSDLColumn, initialized for the given
     * root node.
     *
     * @param  columnView  our parent.
     * @param  rootNode    Node to be displayed here.
     * @param  showRoot    true to show root node.
     */
    public WSDLColumn(WSDLColumnsView columnView, Node rootNode,
            boolean showRoot) {
        super();
        this.columnView = columnView;
        explorerManager = new ExplorerManager();
        // Listen to changes in the selection
        explorerManager.addPropertyChangeListener(this);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 200));
        nodeView = new ColumnListView(explorerManager);
        nodeView.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Traversal is a bad thing for our columns view, and allows the
        // user to cause all of the columns to be completely blank.
        nodeView.setTraversalAllowed(false);
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(nodeView, BorderLayout.CENTER);
        JList list = (JList) nodeView.getViewport().getView();
        list.addFocusListener(this);
        list.setCellRenderer(new ColumnListCellRenderer());
        if (rootNode != null) {
            if (rootNode.getCookie(DummyDefinitionsNode.class) == null &&
                    rootNode.getCookie(DefinitionsNode.class) != null) {
                //IZ 142123 : Call getNodes initially.
                if (rootNode.getChildren() != null) 
                    rootNode.getChildren().getNodes();
                rootNode = new DummyDefinitionsNode(rootNode);
            }
            getExplorerManager().setRootContext(rootNode);
            rootNode.addNodeListener(this);
            // nochildren label initialization
            noChildrenLabel = new JLabel(NbBundle.getMessage(WSDLColumn.class,
                    "LBL_NoChildren"));
            noChildrenLabel.setBackground(getBackground());
            noChildrenLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            noChildrenLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noChildrenLabel.setVisible(false);
            noChildrenLabel.setEnabled(false);
            if (rootNode.getCookie(FolderNode.class) != null) {
                noChildrenLabel.setText(NbBundle.getMessage(WSDLColumn.class,
                        "LBL_NoCateroryChildren", rootNode.getDisplayName()));
            }
        }

        // Set up the map of standard actions.
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction,
                ExplorerUtils.actionCopy(explorerManager));
        map.put(DefaultEditorKit.cutAction,
                ExplorerUtils.actionCut(explorerManager));
        map.put(DefaultEditorKit.pasteAction,
                ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, false));
        lookup = ExplorerUtils.createLookup(explorerManager, map);
        // Do _not_ define the keyboard shortcuts for the actions, as
        // they are in the lookup of our containing TopComponent, and
        // those are activated by the standard keyboard bindings. If we
        // define our own here, we get exceptions in OwnPaste (IZ#80500).
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        nodeView.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return nodeView.requestFocusInWindow();
    }

    /**
     * Finds the TopComponent that contains us.
     *
     * @return  the parent TopComponent.
     */
    protected TopComponent findParentTopComponent() {
        return (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, this);
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        Node node = getExplorerManager().getRootContext();
        Column col = columnView.getFirstColumn();
        if (col == this && (node.getCookie(DummyDefinitionsNode.class) != null ||
                node.getCookie(DefinitionsNode.class) != null)) {
            return NbBundle.getMessage(WSDLColumn.class,
                    "LBL_DefinitionsNode_Title");
        }
        return node.getDisplayName();
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        Node node = getExplorerManager().getRootContext();
        sb.append(node.getDisplayName());
        node = node.getParentNode();
        while (node != null) {
            sb.insert(0, '/');
            sb.insert(0, node.getDisplayName());
            node = node.getParentNode();
        }
        return sb.toString();
    }

    public void propertyChange(PropertyChangeEvent event) {
        String pname = event.getPropertyName();
        Object src = event.getSource();
        if (ExplorerManager.PROP_SELECTED_NODES.equals(pname)) {
            Node[] filteredNodes = (Node[])event.getNewValue();
            if (filteredNodes != null && filteredNodes.length >= 1) {
                // Set the active nodes for the parent TopComponent.
                TopComponent tc = findParentTopComponent();
                if (tc != null) {
                    tc.setActivatedNodes(filteredNodes);
                }
                addDetailColumn(filteredNodes[0]);
                EventListener listener = getColumnView().getColumnListener();
                if (listener != null) {
                    firePropertyChange(WSDLColumn.WSDL_COLUMN_NODE_SELECTED,
                            null, filteredNodes[0]);
                }
            }
        } else if (Node.PROP_DISPLAY_NAME.equals(pname) && src instanceof Node) {
            // The root node display name has changed, need to inform
            // our listeners that our title has effectively changed.
            firePropertyChange(PROP_TITLE, null, null);
        }
    }

    /**
     * Adds a column to the right of this column, showing the details
     * of the given node.
     *
     * @param  node  a Node instance.
     */
    protected void addDetailColumn(final Node node) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Column column = null;
                // Give the node a chance to return its own column component
                ColumnProvider provider = (ColumnProvider) node.getLookup().lookup(
                        ColumnProvider.class);
                WSDLColumnsView view = getColumnView();
                if (provider != null) {
                    column = provider.getColumn();
                    if (column instanceof WSDLColumn) {
                        WSDLColumn sc = (WSDLColumn) column;
                        if (sc.getColumnView() == null) {
                            sc.setColumnView(view);
                        }
                    }
                }
                // Create the default column if one wasn't specified by the node.
                if (column == null && node != getExplorerManager().getRootContext() &&
                        node.getChildren() != Children.LEAF) {
                    column = view.createColumnComponent(node, false);
                }
                // Check if this column is in the view and remove columns after.
                if (view.getColumnIndex(WSDLColumn.this) != -1) {
                    view.removeColumnsAfter(WSDLColumn.this);
                }
                if (column != null) {
                    view.appendColumn(column);
                }
                requestFocusInWindow();
            }
        });
    }

    public void focusLost(FocusEvent e) {
    }

    public void focusGained(FocusEvent e) {
        TopComponent tc = findParentTopComponent();
        if (tc != null) {
            // Find the selected node in this column and activate it.
            JList list = (JList) nodeView.getViewport().getView();
            Object comp = list.getSelectedValue();
            if (comp != null) {
                Node node = Visualizer.findNode(comp);
                if (node != null) {
                    tc.setActivatedNodes(new Node[] { node });
                }
            }
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        getColumnView().addColumnListener(this);
        // hide list view and show nochildren label if needed
        Node rootNode = getExplorerManager().getRootContext();
        if (rootNode != null && rootNode.getChildren().getNodesCount() == 0) {
            remove(nodeView);
            noChildrenLabel.setVisible(true);
            add(noChildrenLabel,BorderLayout.CENTER);
        }
    }

    public void childrenAdded(NodeMemberEvent event) {
        // show list view and hide nochildren label if needed
        Node node = event.getNode();
        if (node != null && node == getExplorerManager().getRootContext() &&
                noChildrenLabel.isVisible()) {
            noChildrenLabel.setVisible(false);
            remove(noChildrenLabel);
            add(nodeView, BorderLayout.CENTER);
        }
    }

    public void childrenRemoved(NodeMemberEvent event) {
        // hide list view and show nochildren label if needed
        final Node node = event.getNode();
        if (node != null && node == getExplorerManager().getRootContext() &&
                !noChildrenLabel.isVisible()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if (node.getChildren().getNodesCount() == 0) {
                        noChildrenLabel.setVisible(true);
                        remove(nodeView);
                        add(noChildrenLabel, BorderLayout.CENTER);
                    }
                }
            });
        }
    }

    public void childrenReordered(NodeReorderEvent event) {
    }

    public void nodeDestroyed(NodeEvent event) {
        // delete this column and columns after this.
        Node node = event.getNode();
        if (node != null && node == getExplorerManager().getRootContext()) {
            final WSDLColumnsView view = getColumnView();
            int idx = view.getColumnIndex(this);
            if (idx <= 0) {
                return;
            }
            Column c = view.getFirstColumn();
            for (int ii = 1; ii < idx; ii++) {
                c = view.getNextColumn(c);
            }
            final Column currentColumn = c;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    view.removeColumnsAfter(currentColumn);
                }
            });
        }
    }

    protected WSDLColumnsView getColumnView() {
        return columnView;
    }

    protected void setColumnView(WSDLColumnsView columnView) {
        this.columnView = columnView;
    }
}

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

package org.netbeans.modules.xml.schema.ui.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
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
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategoryNode;
import org.netbeans.modules.xml.xam.ui.column.ColumnListCellRenderer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.DummySchemaNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.ReadOnlySchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.schema.SchemaNode;
import org.netbeans.modules.xml.xam.ui.column.Column;
import org.netbeans.modules.xml.xam.ui.column.ColumnListView;
import org.netbeans.modules.xml.xam.ui.column.ColumnProvider;
import org.openide.explorer.view.ListView;
import org.openide.nodes.NodeListener;
import org.openide.util.NbBundle;

/**
 * Represents a column in the SchemaColumnsView, displaying the details of
 * a SchemaComponentNode.
 * 
 * @author Todd Fast, todd.fast@sun.com
 * @author Nathan Fiedler
 * @author Jeri Lockhart
 */
public class SchemaColumn extends JPanel
	implements ExplorerManager.Provider, Lookup.Provider, 
		NodeListener, Column, FocusListener {
    private static final long serialVersionUID = 1L;
    private SchemaColumnsView columnView;
    private ExplorerManager explorerManager;
    private transient Lookup lookup;
    private ListView nodeView;
    private JLabel noChildrenLabel;            
    public static final String SCHEMA_COLUMN_NODE_SELECTED = "schema-column-node-selected";	//NOI18N

    /**
     * Creates a new instance of SchemaColumn, initialized for the given
     * root node.
     *
     * @param  columnView  the containing column view (may be null).
     * @param  rootNode    Node to be displayed here (may be null).
     * @param  showRoot    true to show root node.
     */
    public SchemaColumn(SchemaColumnsView columnView, Node rootNode,
            boolean showRoot) {
        super();
        this.columnView = columnView;
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        setBackground(usualWindowBkg != null ? usualWindowBkg :Color.white);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 200));
        nodeView = new ColumnListView(explorerManager);
        nodeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SchemaColumn.class,
                "LBL_SchemaCategory_Categorized"));
        nodeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SchemaColumn.class,
                "HINT_SchemaCategory_Categorized"));
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
            if (rootNode.getCookie(DummySchemaNode.class) == null &&
                    rootNode.getCookie(SchemaNode.class) != null) {
                //issue 140605: call getChildren.getNodes() once
                if(rootNode.getChildren() != null)
                    rootNode.getChildren().getNodes();
                rootNode = new DummySchemaNode(rootNode);
            }
            getExplorerManager().setRootContext(rootNode);
            rootNode.addNodeListener(this);
            // nochildren label initialization
            noChildrenLabel = new JLabel(NbBundle.getMessage(SchemaColumn.class,
                    "LBL_NoChildren"));
            noChildrenLabel.setBackground(getBackground());
            noChildrenLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            noChildrenLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            noChildrenLabel.setVisible(false);
            noChildrenLabel.setEnabled(false);
            if (rootNode.getCookie(CategoryNode.class) != null) {
                noChildrenLabel.setText(NbBundle.getMessage(SchemaColumn.class,
                        "LBL_NoCateroryChildren",rootNode.getDisplayName()));
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
        return (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class,this);
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        Node node = getExplorerManager().getRootContext();
        Column col = columnView.getFirstColumn();
        if (col == this && (node.getCookie(DummySchemaNode.class) != null ||
                node.getCookie(SchemaNode.class) != null)) {
            return NbBundle.getMessage(SchemaColumn.class,
                    "LBL_SchemaNode_Title");
        } else {
            return getDefaultDisplayName(node);
        }
    }

     private String getDefaultDisplayName(Node n) {
	String displayName = null;
	ReadOnlySchemaComponentNode roNode = 
                n.getLookup().lookup(ReadOnlySchemaComponentNode.class);
	    
	if (roNode != null) {
	    displayName = roNode.getDefaultDisplayName();
	} else {
	    SchemaComponentNode scn = 
                    n.getLookup().lookup(SchemaComponentNode.class);
	    if (scn != null) {
		displayName = scn.getDefaultDisplayName();
	    } else {
		displayName = n.getDisplayName();
	    }
	}
	
	return displayName;
    }
    
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        Node node = getExplorerManager().getRootContext();        
        sb.append(getDefaultDisplayName(node));
        node = node.getParentNode();
        while (node != null) {
            sb.insert(0, '/');
            sb.insert(0, getDefaultDisplayName(node));
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
                    firePropertyChange(SchemaColumn.SCHEMA_COLUMN_NODE_SELECTED,
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
     * of the given schema node.
     *
     * @param  node  a SchemaNode instance.
     */
    protected void addDetailColumn(final Node node) {
        Column column = null;
        // Give the node a chance to return its own column component
        ColumnProvider provider = node.getLookup().lookup(
                ColumnProvider.class);
        final SchemaColumnsView view = getColumnView();
        if (provider != null) {
            column = provider.getColumn();
            if (column instanceof SchemaColumn) {
                SchemaColumn sc = (SchemaColumn) column;
                if (sc.getColumnView() == null) {
                    sc.setColumnView(view);
                }
            }
        }
        // Create the default column if one wasn't specified by the node
        // check for right clicks
        if (column == null && (node != getExplorerManager().getRootContext()) &&
                node.getChildren() != Children.LEAF) {
            column = getColumnView().createColumnComponent(node, false);
        }
        // check to see if this column is in columns view and then remove columns after
        if (view.getColumnIndex(this) != -1) {
            view.removeColumnsAfter(this);
        }
        if (column != null) {
            view.appendColumn(column);
        }
        requestFocusInWindow();
    }

    public void focusLost(FocusEvent e) {
    }

    public void focusGained(FocusEvent e) {
        TopComponent tc = findParentTopComponent();
        if (tc != null) {
            // Find the selected node in this column and activate it.
            JList list = (JList) nodeView.getViewport().getView();
            Object[] items = list.getSelectedValues();
            if (items != null && items.length > 0) {
                Node[] nodes = new Node[items.length];
                for(int i=0; i<items.length; i++) {
                    Node n = Visualizer.findNode(items[i]);
                    if(n != null) {
                        nodes[i] = n;
                    }
                }
                tc.setActivatedNodes(nodes);
            }
            getColumnView().scrollToColumn(this,true);
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
    
    protected SchemaColumnsView getColumnView() {
        return columnView;
    }

    protected void setColumnView(SchemaColumnsView columnView) {
        this.columnView = columnView;
    }

    public void childrenAdded(NodeMemberEvent event) {
        // show list view and hide nochildren label if needed
        final Node node = event.getNode();
        // Avoid deadlock by running this on the EDT (see issue 83708).
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (noChildrenLabel.isVisible() && node != null &&
                        node == getExplorerManager().getRootContext()) {
                    noChildrenLabel.setVisible(false);
                    remove(noChildrenLabel);
                    add(nodeView,BorderLayout.CENTER);
                }
            }
        });
    }

    public void childrenRemoved(NodeMemberEvent event) {
        // hide list view and show nochildren label if needed
        final Node node = event.getNode();
        // Avoid deadlock by running this on the EDT (see issue 83708).
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (!noChildrenLabel.isVisible() && node != null &&
                        node == getExplorerManager().getRootContext() &&
                        node.getChildren().getNodesCount() == 0) {
                    noChildrenLabel.setVisible(true);
                    remove(nodeView);
                    add(noChildrenLabel,BorderLayout.CENTER);
                    repaint();
                }
            }
        });
    }

    public void childrenReordered(NodeReorderEvent event) {
    }

    public void nodeDestroyed(NodeEvent event)
    {
        // delete this column and columns after this. 
        final Node node = event.getNode();
        if(node!=null && node==getExplorerManager().getRootContext())
        {
            final SchemaColumnsView view = getColumnView();
            int idx = view.getColumnIndex(this);
            if(idx<=0) return;
            Column c = view.getFirstColumn();
            for(int i=1;i<idx;i++)
            {
                c = view.getNextColumn(c);
            }
            final Column currentColumn = c;
            EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    view.removeColumnsAfter(currentColumn);
                }
            });
        }
    }
}

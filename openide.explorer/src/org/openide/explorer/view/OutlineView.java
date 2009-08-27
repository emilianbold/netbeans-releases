/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.openide.explorer.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.TableColumnSelector;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RowModel;
import org.netbeans.swing.outline.TreePathSupport;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * <p>Explorer view displaying nodes in a tree table.</p>
 * 
 * <p>Related documentation:
 * <ul>
 * <li><a href="http://weblogs.java.net/blog/timboudreau/archive/2008/06/egads_an_actual.html">Egads! An actual Swing Tree-Table!</a>
 * <li><a href="http://blogs.sun.com/geertjan/entry/swing_outline_component">Swing Outline Component</a>
 * </ul>
 * 
 * <p><b>Note:</b> This API is still under development and may change even in
 * incompatible way during its stabilization phase. The API will be finalized in
 * NetBeans version 6.5.</p>
 * 
 * @author David Strupl
 */
public class OutlineView extends JScrollPane {

    /** The table */
    private Outline outline;
    /** Explorer manager, valid when this view is showing */
    ExplorerManager manager;
    /** not null if popup menu enabled */
    private PopupAdapter popupListener;
    /** the most important listener (on four types of events */
    private TableSelectionListener managerListener = null;
    /** weak variation of the listener for property change on the explorer manager */
    private PropertyChangeListener wlpc;
    /** weak variation of the listener for vetoable change on the explorer manager */
    private VetoableChangeListener wlvc;
    
    private OutlineModel model;
    private NodeTreeModel treeModel;
    private PropertiesRowModel rowModel;
    /** */
    private NodePopupFactory popupFactory;
    
    /** true if drag support is active */
    private transient boolean dragActive = true;

    /** true if drop support is active */
    private transient boolean dropActive = true;

    /** Drag support */
    transient OutlineViewDragSupport dragSupport;

    /** Drop support */
    transient OutlineViewDropSupport dropSupport;
    transient boolean dropTargetPopupAllowed = true;

    // default DnD actions
    transient private int allowedDragActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    transient private int allowedDropActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;

    /** Listener on keystroke to invoke default action */
    private ActionListener defaultTreeActionListener;
    private final String nodesColumnLabel;

    /** Creates a new instance of TableView */
    public OutlineView() {
        this(null);
    }    
    
    /** Creates a new instance of TableView */
    public OutlineView(String nodesColumnLabel) {
        treeModel = new NodeTreeModel();
        rowModel = new PropertiesRowModel();
        model = createOutlineModel(treeModel, rowModel, nodesColumnLabel);
        this.nodesColumnLabel = nodesColumnLabel;
        outline = new OutlineViewOutline(model, rowModel);
        rowModel.setOutline(outline);
        outline.setRenderDataProvider(new NodeRenderDataProvider(outline));
        SheetCell tableCell = new SheetCell.OutlineSheetCell(outline);
        outline.setDefaultRenderer(Node.Property.class, tableCell);
        outline.setDefaultEditor(Node.Property.class, tableCell);
        setViewportView(outline);
        setPopupAllowed(true);
        // do not care about focus
        setRequestFocusEnabled (false);
        outline.setRequestFocusEnabled(true);
        java.awt.Color c = javax.swing.UIManager.getColor("Table.background1");
        if (c == null) {
            c = javax.swing.UIManager.getColor("Table.background");
        }
        if (c != null) {
            getViewport().setBackground(c);
        }
        getActionMap().put("org.openide.actions.PopupAction", new PopupAction());
        popupFactory = new OutlinePopupFactory();
        // activation of drop target
        setDropTarget( DragDropUtilities.dragAndDropEnabled );
        defaultTreeActionListener = new DefaultTreeAction (outline);
        outline.registerKeyboardAction(
            defaultTreeActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED
        );

        final Color focusSelectionBackground = outline.getSelectionBackground();
        final Color focusSelectionForeground = outline.getSelectionForeground();
        outline.addFocusListener(new java.awt.event.FocusListener(){
            public void focusGained(java.awt.event.FocusEvent ev) {
                outline.setSelectionBackground(focusSelectionBackground);
                outline.setSelectionForeground(focusSelectionForeground);
            }

            public void focusLost(java.awt.event.FocusEvent ev) {
                outline.setSelectionBackground(SheetCell.getNoFocusSelectionBackground());
                outline.setSelectionForeground(SheetCell.getNoFocusSelectionForeground());
            }

        });
        TableColumnSelector tcs = Lookup.getDefault ().lookup (TableColumnSelector.class);
        if (tcs != null) {
            outline.setColumnSelector(tcs);
        }

        if (UIManager.getColor("control") != null) { // NOI18N
            getOutline().setGridColor(UIManager.getColor("control")); // NOI18N
        }
        
        if (DragDropUtilities.dragAndDropEnabled ) {//&& dragActive) {
            setDragSource(true);
        }
        
        setBorder( BorderFactory.createEmptyBorder() );
    }

    /**
     * This method allows plugging own OutlineModel to the OutlineView.
     * You can override it and create different model in the subclass.
     */
    protected OutlineModel createOutlineModel(NodeTreeModel treeModel, RowModel rowModel, String label) {
        if (label == null) {
            label = NbBundle.getMessage(OutlineView.class, "NodeOutlineModel_NodesColumnLabel"); // NOI18N
        }
        return new NodeOutlineModel(treeModel, rowModel, false, label);
    }
    
    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public void requestFocus () {
        outline.requestFocus();
    }
    
    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow () {
        return outline.requestFocusInWindow();
    }
    
    /**
     * Getter for the embeded table component.
     */
    public Outline getOutline() {
        return outline;
    }
    
    /** Is it permitted to display a popup menu?
     * @return <code>true</code> if so
     */
    public boolean isPopupAllowed () {
        return popupListener != null;
    }

    public void setProperties(Node.Property[] newProperties) {
        rowModel.setProperties(newProperties);
        outline.tableChanged(null);
    }
    
    /** Enable/disable displaying popup menus on tree view items.
    * Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed (boolean value) {
        if (popupListener == null && value) {
            // on
            popupListener = new PopupAdapter ();
            outline.addMouseListener (popupListener);
            addMouseListener(popupListener);
            return;
        }
        if (popupListener != null && !value) {
            // off
            outline.removeMouseListener (popupListener);
            removeMouseListener (popupListener);
            popupListener = null;
            return;
        }
    }
    
    /** Initializes the component and lookup explorer manager.
     */
    @Override
    public void addNotify () {
        super.addNotify ();
        lookupExplorerManager ();
        ViewUtil.adjustBackground(outline);
        ViewUtil.adjustBackground(getViewport());
    }
    
    /**
     * Method allowing to read stored values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void readSettings(Properties p, String propertyPrefix) {
        outline.readSettings(p, propertyPrefix);
    }

    /**
     * Method allowing to store customization values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void writeSettings(Properties p, String propertyPrefix) {
        outline.writeSettings(p, propertyPrefix);
    }

    /**
     * Allows customization of the popup menus.
     */
    public void setNodePopupFactory(NodePopupFactory newFactory) {
        popupFactory = newFactory;
    }
    
    /**
     * Getter for the current popup customizer factory.
     */
    public NodePopupFactory getNodePopupFactory() {
        return popupFactory;
    }
    
    /** Registers in the tree of components.
     */
    private void lookupExplorerManager () {
    // Enter key in the tree

        if (managerListener == null) {
            managerListener = new TableSelectionListener();
        }
        
        ExplorerManager newManager = ExplorerManager.find(this);
        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener (wlvc);
                manager.removePropertyChangeListener (wlpc);
            }

            manager = newManager;

            manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(managerListener, manager));
        }
        
        synchronizeRootContext();
        synchronizeSelectedNodes(true);
        
        // Sometimes the listener is registered twice and we get the 
        // selection events twice. Removing the listener before adding it
        // should be a safe fix.
        outline.getSelectionModel().removeListSelectionListener(managerListener);
        outline.getSelectionModel().addListSelectionListener(managerListener);
    }
    
    /** Synchronize the root context from the manager of this Explorer.
    */
    final void synchronizeRootContext() {
        if( null != treeModel ) {
            treeModel.setNode(manager.getRootContext());
        }
    }

    /** Synchronize the selected nodes from the manager of this Explorer.
     */
    final void synchronizeSelectedNodes(boolean scroll, Node... nodes) {
        if (! needToSynchronize ()) {
            return ;
        }
        expandSelection();
        outline.invalidate();
        invalidate();
        validate();
        Node[] arr = manager.getSelectedNodes ();
        outline.getSelectionModel().clearSelection();
        int size = outline.getRowCount();
        int firstSelection = -1;
        for (int i = 0; i < size; i++) {
            Node n = getNodeFromRow(i);
            for (int j = 0; j < arr.length; j++) {
                if ((n != null) && (n.equals(arr[j]))) {
                    outline.getSelectionModel().addSelectionInterval(i, i);
                    if (firstSelection == -1) {
                        firstSelection = i;
                    }
                }
            }
        }
        if (scroll && (firstSelection >= 0)) {
            JViewport v = getViewport();
            if (v != null) {
                Rectangle rect = outline.getCellRect(firstSelection, 0, true);
                if (v.getExtentSize().height > rect.height) {
                    rect.height = v.getExtentSize().height;
                }
                int ho = outline.getSize().height;
                if (ho > 0) {
                    if (rect.y + rect.height > ho) {
                        rect.height = ho - rect.y;
                        if (rect.height <= 0) {
                            rect.height = 40;
                        }
                    }
                }
                v.setViewPosition(new Point()); // strange line - but without
                                                // it the next one is wrong
                outline.scrollRectToVisible(rect);
            }
        }
    }

    private boolean needToSynchronize () {
        boolean doSync = false;
        Node [] arr = manager.getSelectedNodes ();
        if (outline.getSelectedRows ().length != arr.length) {
            doSync = true;
        } else if (arr.length > 0) {
            List<Node> nodes = Arrays.asList (arr);
            for (int idx : outline.getSelectedRows ()) {
                Node n = getNodeFromRow (idx);
                if (n == null || ! nodes.contains (n)) {
                    doSync = true;
                    break;
                }
            }
        }
        return doSync;
    }

    /**
     * Tries to expand nodes selected in the explorer manager.
     */
    private void expandSelection() {
        Node[] arr = manager.getSelectedNodes ();
        for (int i = 0; i < arr.length; i++) {
            if ( (arr[i].getParentNode() == null) && (! outline.isRootVisible())) {
                // don't try to show root if it is invisible
                continue;
            }
            TreeNode tn = Visualizer.findVisualizer(arr[i]);
            if (tn != null) {
                ArrayList<TreeNode> al = new ArrayList<TreeNode> ();
                while (tn != null) {
                    al.add(tn);
                    tn = tn.getParent();
                }
                Collections.reverse(al);
                TreePath tp = new TreePath(al.toArray());
                while ((tp != null) && (tp.getPathCount() > 0)) {
                    tp = tp.getParentPath();
                    if (tp != null) {
                        outline.expandPath(tp);
                    }
                }
            }
        }
    }
    
    /**
     * Deinitializes listeners.
     */
    @Override
    public void removeNotify () {
        super.removeNotify ();
        outline.getSelectionModel().clearSelection();
        outline.getSelectionModel().removeListSelectionListener(managerListener);
        manager.removePropertyChangeListener (wlpc);
        manager.removeVetoableChangeListener (wlvc);
        manager = null;
    }

    /**
     * Shows popup menu invoked on the table.
     */
    void showPopup(int xpos, int ypos, final JPopupMenu popup) {
        if ((popup != null) && (popup.getSubElements().length > 0)) {
            final PopupMenuListener p = new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    
                }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popup.removePopupMenuListener(this);
                    outline.requestFocus();
                }
                public void popupMenuCanceled(PopupMenuEvent e) {
                    
                }
            };
            popup.addPopupMenuListener(p);
            popup.show(this, xpos, ypos);
        }
    }    
    
    /**
     * Find relevant actions and call the factory to create a popup.
     */
    private JPopupMenu createPopup(Point p) {
        int[] selRows = outline.getSelectedRows();
        ArrayList<Node> al = new ArrayList<Node> (selRows.length);
        for (int i = 0; i < selRows.length; i++) {
            Node n = getNodeFromRow(selRows[i]);
            if (n != null) {
                al.add(n);
            }
        }
        Node[] arr = al.toArray (new Node[al.size ()]);
        if (arr.length == 0) {
            // hack to show something even when no rows are selected
            arr = new Node[] { manager.getRootContext() };
        }
        p = SwingUtilities.convertPoint(this, p, outline);
        int column = outline.columnAtPoint(p);
        int row = outline.rowAtPoint(p);
        return popupFactory.createPopupMenu(row, column, arr, outline);
    }
    
    /**
     * 
     */
    Node getNodeFromRow(int rowIndex) {
        int row = outline.convertRowIndexToModel(rowIndex);
        TreePath tp = outline.getLayoutCache().getPathForRow(row);
        if (tp == null) {
            return null;
        }
        return Visualizer.findNode(tp.getLastPathComponent());
    }
    
    /** Returns the point at which the popup menu is to be shown. May return null.
     * @return the point or null
     */    
    private Point getPositionForPopup () {
        int i = outline.getSelectionModel().getLeadSelectionIndex();
        if (i < 0) return null;
        int j = outline.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        if (j < 0) {
            j = 0;
        }

        Rectangle rect = outline.getCellRect(i, j, true);
        if (rect == null) return null;

        Point p = new Point(rect.x + rect.width / 3,
                rect.y + rect.height / 2);
        
        // bugfix #36984, convert point by TableView.this
        p =  SwingUtilities.convertPoint (outline, p, OutlineView.this);

        return p;
    }

    /**
     * Action registered in the component's action map.
     */
    private class PopupAction extends javax.swing.AbstractAction implements Runnable {
        public void actionPerformed(ActionEvent evt) {
            SwingUtilities.invokeLater(this);
        }
        public void run() {
            Point p = getPositionForPopup ();
            if (p == null) {
                return ;
            }
            if (isPopupAllowed()) {
                JPopupMenu pop = createPopup(p);
                showPopup(p.x, p.y, pop);
            }
        }
    };
    
    /**
     * Mouse listener that invokes popup.
     */
    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

	PopupAdapter() {}
	
        protected void showPopup (MouseEvent e) {
            int selRow = outline.rowAtPoint(e.getPoint());

            if (selRow != -1) {
                if (! outline.getSelectionModel().isSelectedIndex(selRow)) {
                    outline.getSelectionModel().clearSelection();
                    outline.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
            } else {
                outline.getSelectionModel().clearSelection();
            }
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), OutlineView.this);
            if (isPopupAllowed()) {
                JPopupMenu pop = createPopup(p);
                OutlineView.this.showPopup(p.x, p.y, pop);
                e.consume();
            }
        }

        @Override
        public void mouseClicked (MouseEvent e) {
            int selRow = outline.rowAtPoint (e.getPoint ());

            if ((selRow != -1) && SwingUtilities.isLeftMouseButton (e) && MouseUtils.isDoubleClick (e)) {
                // Default action.
                if (outline.getSelectedColumn () == 0) {
                    TreePath selPath = outline.getClosestPathForLocation (e.getX (), e.getY ());
                    Node node = Visualizer.findNode (selPath.getLastPathComponent ());
                    if (node.isLeaf ()) {
                        Action a = TreeView.takeAction (node.getPreferredAction (), node);

                        if (a != null) {
                            if (a.isEnabled ()) {
                                a.actionPerformed (new ActionEvent (node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                            } else {
                                Logger.getLogger (OutlineView.class.getName ()).info ("Action " + a + " on node " + node + " is disabled");
                            }

                            e.consume ();
                            return;
                        }
                    }
                }
            }
            super.mouseClicked (e);
        }
    }

    /**
     * Called when selection in tree is changed.
     */
    final private void callSelectionChanged (Node[] nodes) {
        manager.removePropertyChangeListener (wlpc);
        manager.removeVetoableChangeListener (wlvc);
        try {
            manager.setSelectedNodes(nodes);
        } catch (PropertyVetoException e) {
            synchronizeSelectedNodes(false);
        } finally {
            // to be sure not to add them twice!
            manager.removePropertyChangeListener (wlpc);
            manager.removeVetoableChangeListener (wlvc);
            manager.addPropertyChangeListener (wlpc);
            manager.addVetoableChangeListener (wlvc);
        }
    }
    
    /** 
     * Check if selection of the nodes could break
     * the selection mode set in the ListSelectionModel.
     * @param nodes the nodes for selection
     * @return true if the selection mode is broken
     */
    private boolean isSelectionModeBroken(Node[] nodes) {
        
        // if nodes are empty or single then everthing is ok
        // or if discontiguous selection then everthing ok
        if (nodes.length <= 1 || outline.getSelectionModel().getSelectionMode() == 
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
            return false;
        }

        // if many nodes
        
        // breaks single selection mode
        if (outline.getSelectionModel().getSelectionMode() == 
            ListSelectionModel.SINGLE_SELECTION) {
            return true;
        }
        
        // check the contiguous selection mode

        // check selection's rows
        
        // all is ok
        return false;
    }

    /********** Support for the Drag & Drop operations *********/
    /** Drag support is enabled by default.
    * @return true if dragging from the view is enabled, false
    * otherwise.
    */
    public boolean isDragSource() {
        return dragActive;
    }

    /** Enables/disables dragging support.
    * @param state true enables dragging support, false disables it.
    */
    public void setDragSource(boolean state) {
        // create drag support if needed
        if (state && (dragSupport == null)) {
            dragSupport = new OutlineViewDragSupport(this, getOutline());
        }

        // activate / deactivate support according to the state
        dragActive = state;

        if (dragSupport != null) {
            dragSupport.activate(dragActive);
        }
    }

    /** Drop support is enabled by default.
    * @return true if dropping to the view is enabled, false
    * otherwise<br>
    */
    public boolean isDropTarget() {
        return dropActive;
    }

    /** Enables/disables dropping support.
    * @param state true means drops into view are allowed,
    * false forbids any drops into this view.
    */
    public void setDropTarget(boolean state) {
        // create drop support if needed
        if (dropActive && (dropSupport == null)) {
            dropSupport = new OutlineViewDropSupport(this, outline, dropTargetPopupAllowed);
        }

        // activate / deactivate support according to the state
        dropActive = state;

        if (dropSupport != null) {
            dropSupport.activate(dropActive);
        }
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions (copy, move, link) are allowed by default.
    * @return int representing set of actions which are allowed when dragging from
    * asociated component.
     */
    public int getAllowedDragActions() {
        return allowedDragActions;
    }

    /** Sets allowed actions for dragging
    * @param actions new drag actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDragActions(int actions) {
        // PENDING: check parameters
        allowedDragActions = actions;
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions are allowed by default.
    * @return int representing set of actions which are allowed when dropping
    * into the asociated component.
    */
    public int getAllowedDropActions() {
        return allowedDropActions;
    }

    /** Sets allowed actions for dropping.
    * @param actions new allowed drop actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDropActions(int actions) {
        // PENDING: check parameters
        allowedDropActions = actions;
    }
    
    public void addTreeExpansionListener( TreeExpansionListener l ) {
        TreePathSupport tps = getOutline().getOutlineModel().getTreePathSupport();
        if( tps != null )
            tps.addTreeExpansionListener(l);
    }
    
    public void removeTreeExpansionListener( TreeExpansionListener l ) {
        TreePathSupport tps = getOutline().getOutlineModel().getTreePathSupport();
        if( tps != null )
            tps.removeTreeExpansionListener(l);
    }

    /** Collapses the tree under given node.
    *
    * @param n node to collapse
    */
    public void collapseNode(Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));
        getOutline().collapsePath(treePath);
    }

    /** Expandes the node in the tree.
    *
    * @param n node
    */
    public void expandNode(Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        lookupExplorerManager();

        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));

        getOutline().expandPath(treePath);
    }

    /** Test whether a node is expanded in the tree or not
    * @param n the node to test
    * @return true if the node is expanded
    */
    public boolean isExpanded(Node n) {
        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));
        return getOutline().isExpanded(treePath);
    }
    
    /**
     * Listener attached to the explorer manager and also to the
     * changes in the table selection.
     */
    private class TableSelectionListener implements VetoableChangeListener, ListSelectionListener, PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (manager == null) return; // the tree view has been removed before the event got delivered
            if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
                synchronizeRootContext();
                if (nodesColumnLabel == null && model instanceof NodeOutlineModel) {
                    String oldLabel = ((NodeOutlineModel) model).getColumnName(0);
                    String newLabel = manager.getRootContext().getDisplayName();
                    if (oldLabel != null && ! oldLabel.equals(newLabel)) {
                        ((NodeOutlineModel) model).setNodesColumnLabel(newLabel);
                    }
                }
            }
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                synchronizeSelectedNodes(true);
            }
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
            int selectedRows[] = outline.getSelectedRows();
            ArrayList<Node> selectedNodes = new ArrayList<Node> (selectedRows.length);
            for (int i = 0; i < selectedRows.length;i++) {
                Node n = getNodeFromRow(selectedRows[i]);
                if (n != null) {
                    selectedNodes.add(n);
                }
            }
            callSelectionChanged(selectedNodes.toArray (new Node[selectedNodes.size ()]));
        }

        public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                // issue 11928 check if selecetion mode will be broken
                Node[] nodes = (Node[])evt.getNewValue();
                if (isSelectionModeBroken(nodes)) {
                    throw new PropertyVetoException("selection mode " +  " broken by " + Arrays.asList(nodes), evt); // NOI18N
                }
            }
        }
    }


    /** Invokes default action.
     */
    private class DefaultTreeAction implements ActionListener {

        private Outline outline;

        DefaultTreeAction (Outline outline) {
            this.outline = outline;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed (ActionEvent e) {
            if (outline.getSelectedColumn () != 0) {
                return;
            }

            Node[] nodes = manager.getSelectedNodes ();

            if (nodes.length == 1) {
                Action a = nodes[0].getPreferredAction ();

                if (a != null) {
                    if (a.isEnabled ()) {
                        a.actionPerformed (new ActionEvent (nodes[0], ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    } else {
                        Logger.getLogger (OutlineView.class.getName ()).info ("Action " + a + " on node " + nodes [0] + " is disabled");
                    }
                }
            }
        }
    }

    /**
     * Extension of the ETable that allows adding a special comparator
     * for sorting the rows.
     */
    private static class OutlineViewOutline extends Outline {
        private final PropertiesRowModel rowModel;
        private static final String COLUMNS_SELECTOR_HINT = "ColumnsSelectorHint"; // NOI18N
        public OutlineViewOutline(OutlineModel mdl, PropertiesRowModel rowModel) {
            super(mdl);
            this.rowModel = rowModel;
            setSelectVisibleColumnsLabel(NbBundle.getMessage(OutlineView.class, "CTL_ColumnsSelector")); //NOI18N
        }
        
        @Override
        public Object transformValue(Object value) {
            if (value instanceof OutlineViewOutlineColumn) {
                OutlineViewOutlineColumn c = (OutlineViewOutlineColumn) value;
                String dn = c.getRawColumnName ();
                if (dn == null) {
                    dn = c.getHeaderValue ().toString ();
                }
                String desc = c.getShortDescription (null);
                if (desc == null) {
                    return dn;
                }
                return NbBundle.getMessage (OutlineView.class, "OutlineViewOutline_NameAndDesc", dn, desc); // NOI18N
            } else if (COLUMNS_SELECTOR_HINT.equals (value)) {
                return NbBundle.getMessage (OutlineView.class, COLUMNS_SELECTOR_HINT);
            } else if (value instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) value;
                Mnemonics.setLocalizedText (b, b.getText ());
                return b;
            } else if (value instanceof VisualizerNode) {
                return Visualizer.findNode (value);
            }
            return PropertiesRowModel.getValueFromProperty(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean editCellAt(int row, int column, EventObject e) {
            Object o = getValueAt(row, column);
            if (o instanceof Node.Property) { // && (e == null || e instanceof KeyEvent)) {
                Node.Property p = (Node.Property)o;
                if (p.getValueType() == Boolean.class || p.getValueType() == Boolean.TYPE) {
                    PropertiesRowModel.toggleBooleanProperty(p);
                    Rectangle r = getCellRect(row, column, true);
                    repaint (r.x, r.y, r.width, r.height);
                    return false;
                }
            }

            boolean res = super.editCellAt(row, column, e);
            if( !res && e instanceof MouseEvent ) {
                //try invoking custom editor on disabled cell
            
                final Rectangle r = getCellRect(row, column, true);
                boolean isTreeColumn = convertColumnIndexToModel( column ) == 0;
                if( ((MouseEvent) e).getX() > ((r.x + r.width) - 24) && ((MouseEvent) e).getX() < (r.x + r.width) 
                    && o instanceof Node.Property
                    && !isTreeColumn ) {
                    
                    Node.Property p = (Node.Property)o;
                    if( !Boolean.TRUE.equals(p.getValue("suppressCustomEditor") ) ) { //NOI18N
                        PropertyPanel panel = new PropertyPanel(p);
                        @SuppressWarnings("deprecation")
                        PropertyEditor ed = panel.getPropertyEditor();

                        if ((ed != null) && ed.supportsCustomEditor()) {
                            Action act = panel.getActionMap().get("invokeCustomEditor"); //NOI18N

                            if (act != null) {
                                SwingUtilities.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            r.x = 0;
                                            r.width = getWidth();
                                            OutlineViewOutline.this.repaint(r);
                                        }
                                    }
                                );
                                act.actionPerformed(null);

                                return false;
                            }
                        }
                    }
                }
            }
            return res;
        }
        
        @Override
        protected TableColumn createColumn(int modelIndex) {
            return new OutlineViewOutlineColumn(modelIndex);
        }

        /**
         * Extension of ETableColumn using TableViewRowComparator as
         * comparator.
         */
        private class OutlineViewOutlineColumn extends OutlineColumn {
            private String tooltip;
            private final Comparator originalNodeComparator = new NodeNestedComparator ();

            public OutlineViewOutlineColumn(int index) {
                super(index);
            }
            @Override
            public boolean isSortingAllowed() {
                boolean res = super.isSortingAllowed();
                TableModel model = getModel();
                if (model.getRowCount() <= 0) {
                    return res;
                }
                Object sampleValue = model.getValueAt(0, getModelIndex());
                if (sampleValue instanceof Node.Property) {
                    Node.Property prop = (Node.Property)sampleValue;
                    Object sortableColumnProperty = prop.getValue("SortableColumn");
                    if (sortableColumnProperty instanceof Boolean) {
                        return ((Boolean)sortableColumnProperty).booleanValue();
                    }
                }
                return res;
            }

            @Override
            public Comparator getNestedComparator () {
                // it it's the tree column
                if (getModelIndex () == 0 && super.getNestedComparator () == null) {
                    return originalNodeComparator;
                }
                return super.getNestedComparator ();
            }

            @Override
            protected TableCellRenderer createDefaultHeaderRenderer() {
                TableCellRenderer orig = super.createDefaultHeaderRenderer();
                OutlineViewOutlineHeaderRenderer ovohr = new OutlineViewOutlineHeaderRenderer(orig);
                return ovohr;
            }

            public String getShortDescription (String defaultValue) {
                TableModel model = getModel();
                if (model.getRowCount() <= 0) {
                    return null;
                }
                if (getModelIndex () == 0) {
                    // 1st column
                    return defaultValue;
                }
                return rowModel.getShortDescription (getModelIndex () - 1);
            }

            public String getRawColumnName () {
                TableModel model = getModel();
                if (model.getRowCount() <= 0) {
                    return null;
                }
                if (getModelIndex () == 0) {
                    return null;
                }
                return rowModel.getRawColumnName (getModelIndex () - 1);
            }

            /** This is here to compute and set the header tooltip. */
            class OutlineViewOutlineHeaderRenderer implements TableCellRenderer {
                private TableCellRenderer orig;
                public OutlineViewOutlineHeaderRenderer(TableCellRenderer delegate) {
                    orig = delegate;
                }
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component oc = orig.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (tooltip == null) {
                        tooltip = getShortDescription (value.toString ());
                    }
                    if ((tooltip != null) && (oc instanceof JComponent)) {
                        JComponent jc = (JComponent)oc;
                        jc.setToolTipText(tooltip);
                    }
                    return oc;
                }
            }

            private class NodeNestedComparator implements
                    Comparator {
                public int compare (Object o1, Object o2) {
                    assert o1 instanceof Node : o1 + " is instanceof Node";
                    assert o2 instanceof Node : o2 + " is instanceof Node";
                    return ((Node) o1).getDisplayName ().compareTo (((Node) o2).getDisplayName ());
                }
            }

        }
    }
    
    private static class OutlinePopupFactory extends NodePopupFactory {
        public OutlinePopupFactory() {
        }

        @Override
        public JPopupMenu createPopupMenu(int row, int column, Node[] selectedNodes, Component component) {
            if (component instanceof ETable) {
                ETable et = (ETable)component;
                int modelRowIndex = et.convertColumnIndexToModel(column);
                setShowQuickFilter(modelRowIndex != 0);
            }
            return super.createPopupMenu(row, column, selectedNodes, component);
        }
    }
    
    private static class NodeOutlineModel extends DefaultOutlineModel {

        public NodeOutlineModel(NodeTreeModel treeModel, RowModel rowModel, boolean largeModel, String nodesColumnLabel) {
            super( treeModel, rowModel, largeModel, nodesColumnLabel );
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                Node treeNode = getNodeAt(rowIndex);
                return null != treeNode && treeNode.canRename();
            }
            return super.isCellEditable(rowIndex, columnIndex);
        }

        @Override
        protected void setTreeValueAt(Object aValue, int rowIndex) {
            Node n = getNodeAt(rowIndex);
            if( null != n ) {
                n.setName(aValue == null ? "" : aValue.toString());
            }
        }

        protected final Node getNodeAt( int rowIndex ) {
            Node result = null;
            TreePath path = getLayout().getPathForRow(rowIndex);
            if (path != null) {
                result = Visualizer.findNode(path.getLastPathComponent());
            }
            return result;
        }
    }

}

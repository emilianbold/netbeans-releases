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

/*
 * DefaultOutlineModel.java
 *
 * Created on January 27, 2004, 6:58 PM
 */

package org.netbeans.swing.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.FixedHeightLayoutCache;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.VariableHeightLayoutCache;

/** Proxies a standard TreeModel and TableModel, translating events between
 * the two.  Note that the constructor is not public;  the TableModel that is
 * proxied is the OutlineModel's own.  To make use of this class, implement
 * RowModel - that is a mini-table model in which the TreeModel is responsible
 * for defining the set of rows; it is passed an object from the tree, which
 * it may use to generate values for the other columns.  Pass that and the
 * TreeModel you want to use to <code>createOutlineModel</code>.
 * <p>
 * A note on TableModelEvents produced by this model:  There is a slight 
 * impedance mismatch between TableModelEvent and TreeModelEvent.  When the
 * tree changes, it is necessary to fire TableModelEvents to update the display.
 * However, TreeModelEvents support changes to discontiguous segments of the
 * model (i.e. &quot;child nodes 3, 4 and 9 were deleted&quot;).  TableModelEvents
 * have no such concept - they operate on contiguous ranges of rows.  Therefore,
 * one incoming TreeModelEvent may result in more than one TableModelEvent being
 * fired.  Discontiguous TreeModelEvents will be broken into their contiguous
 * segments, which will be fired sequentially (in the case of removals, in
 * reverse order).  So, the example above would generate two TableModelEvents,
 * the first indicating that row 9 was removed, and the second indicating that
 * rows 3 and 4 were removed.
 * <p>
 * Clients which need to know whether the TableModelEvent they have just 
 * received is one of a group (perhaps they update some data structure, and
 * should not do so until the table's state is fully synchronized with that
 * of the tree model) may call <code>areMoreEventsPending()</code>.
 * <p>
 * In the case of TreeModelEvents which add items to an unexpanded tree node,
 * a simple value change TableModelEvent will be fired for the row in question
 * on the tree column index.
 * <p>
 * Note also that if the model is large-model, removal events may only indicate
 * those indices which were visible at the time of removal, because less data
 * is retained about the position of nodes which are not displayed.  In this
 * case, the only issue is the accuracy of the scrollbar in the model; in
 * practice this is a non-issue, since it is based on the Outline's row count,
 * which will be accurate.
 * <p>
 * A note to subclassers, if we even leave this class non-final:  If you do
 * not use ProxyTableModel and RowMapper (which probably means you are doing
 * something wrong), <strong>do not fire structural changes from the TableModel</strong>.
 * This class is designed such that the TreeModel is entirely in control of the
 * count and contents of the rows of the table.  It and only it may fire 
 * structural changes.
 * <p>
 * Note that this class enforces access only on the event dispatch thread
 * with assertions.  All events fired by the underlying table and tree model
 * must be fired on the event dispatch thread.
 *
 * @author  Tim Boudreau
 */
public class DefaultOutlineModel implements OutlineModel {
    private TreeModel treeModel;
    private TableModel tableModel;
    private AbstractLayoutCache layout;
    private TreePathSupport treePathSupport;
    private EventBroadcaster broadcaster;
    //Some constants we use to have a single method handle all translated
    //event firing
    private static final int NODES_CHANGED = 0;
    private static final int NODES_INSERTED = 1;
    private static final int NODES_REMOVED = 2;
    private static final int STRUCTURE_CHANGED = 3;
    
    /**
     * 4/19/2004 - Added ability to set the node column name.
     * David Botterill
     */
    
    private String nodeColumnName;
    
    //XXX deleteme - string version of the avoid constants debug output:
    private static final String[] types = new String[] {
        "nodesChanged", "nodesInserted", "nodesRemoved", "structureChanged"
    };
    
    /** Create a small model OutlineModel using the supplied tree model and row model 
     * @param treeModel The tree model that is the data model for the expandable
     *  tree column of an Outline
     * @param rowModel The row model which will supply values for each row based
     *  on the tree node in that row in the tree model
     */
    public static OutlineModel createOutlineModel(TreeModel treeModel, RowModel rowModel) {
        return createOutlineModel (treeModel, rowModel, false);
    }

    /** Create an OutlineModel using the supplied tree model and row model,
     * specifying if it is a large-model tree */
    public static OutlineModel createOutlineModel(TreeModel treeModel, RowModel rowModel, boolean isLargeModel) {
        TableModel tableModel = new ProxyTableModel(rowModel);
        return new DefaultOutlineModel (treeModel, tableModel, isLargeModel);
    }
    
    /** Creates a new instance of DefaultOutlineModel.  <strong><b>Note</b> 
     * Do not fire table structure changes from the wrapped TableModel (value
     * changes are okay).  Changes that affect the number of rows must come
     * from the TreeModel.   */
    protected DefaultOutlineModel(TreeModel treeModel, TableModel tableModel, boolean largeModel) {
        this.treeModel = treeModel;
        this.tableModel = tableModel;
        
        layout = largeModel ? (AbstractLayoutCache) new FixedHeightLayoutCache() 
            : (AbstractLayoutCache) new VariableHeightLayoutCache();
            
        broadcaster = new EventBroadcaster (this);
        
        layout.setRootVisible(true);
        layout.setModel(this);
        treePathSupport = new TreePathSupport(this, layout);
        treePathSupport.addTreeExpansionListener(broadcaster);
        treePathSupport.addTreeWillExpandListener(broadcaster);
        treeModel.addTreeModelListener(broadcaster);
        tableModel.addTableModelListener(broadcaster);
        if (tableModel instanceof ProxyTableModel) {
            ((ProxyTableModel) tableModel).setOutlineModel(this);
        }
    }
    
    public final TreePathSupport getTreePathSupport() {
        return treePathSupport;
    }    
    
    public final AbstractLayoutCache getLayout() {
        return layout;
    }
    
    public boolean areMoreEventsPending() {
        return broadcaster.areMoreEventsPending();
    }
    
    /** Accessor for EventBroadcaster */
    TreeModel getTreeModel() {
        return treeModel;
    }
    
    /** Accessor for EventBroadcaster */
    TableModel getTableModel() {
        return tableModel;
    }
    
    public final Object getChild(Object parent, int index) {
        return treeModel.getChild (parent, index);
    }
    
    public final int getChildCount(Object parent) {
        return treeModel.getChildCount (parent);
    }
    
    /** Delegates to the RowMapper for > 0 columns; column 0 always
     * returns Object.class */
    public final Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Object.class;
        } else {
            return tableModel.getColumnClass(columnIndex-1);
        }
    }
    
    public final int getColumnCount() {
        return tableModel.getColumnCount()+1;
    }
    /**
     * Added 4/19/2004 David Botterill
     */
    public void setNodeColumnName(String inName) {
        nodeColumnName = inName;
    }
    public String getColumnName(int columnIndex) {
        /**
         * Changed 4/19/2004 to allow the node column to be named.
         * - David Botterill
         */
        if (columnIndex == 0) {
            return null == nodeColumnName ? "Nodes": nodeColumnName; //XXX
        } else {
            return tableModel.getColumnName(columnIndex-1);
        }
    }
    
    public final int getIndexOfChild(Object parent, Object child) {
        return treeModel.getIndexOfChild(parent, child);
    }
    
    public final Object getRoot() {
        return treeModel.getRoot();
    }
    
    public final int getRowCount() {
        return layout.getRowCount();
    }
    
    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object result;
        if (columnIndex == 0) { //XXX need a column ID - columnIndex = 0 depends on the column model
            TreePath path = getLayout().getPathForRow(rowIndex);
            if (path != null) {
                result = path.getLastPathComponent();
            } else {
                result = null;
            }
        } else {
            result = (tableModel.getValueAt(rowIndex, columnIndex -1));
        }
        return result;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return false; //XXX support editing of node names
        } else {
            return tableModel.isCellEditable(rowIndex, columnIndex-1);
        }
    }
    
    public final boolean isLeaf(Object node) {
        /**
         * Changed 1/13/2005 - David Botterill
         * We need to check for a null node here since the DefaultTreeModel does 
         * not and will cause NPE if it's null.  This situation occurs when the
         * DefaultOutlineCellRenderer gets called from the accessibility context.
         */
        if(null == node) return true;
        return treeModel.isLeaf(node);
    }

    /** Delegates to the EventBroadcaster for this model */
    public final synchronized void addTableModelListener(TableModelListener l) {
        broadcaster.addTableModelListener (l);
    }
    
    /** Delegates to the EventBroadcaster for this model */
    public final synchronized void addTreeModelListener(TreeModelListener l) {
        broadcaster.addTreeModelListener (l);
    }    
    
    /** Delegates to the EventBroadcaster for this model */
    public final synchronized void removeTableModelListener(TableModelListener l) {
        broadcaster.removeTableModelListener(l);
    }
    
    /** Delegates to the EventBroadcaster for this model */
    public final synchronized void removeTreeModelListener(TreeModelListener l) {
        broadcaster.removeTreeModelListener(l);
    }
    
    /** Delegates to the RowModel (or TableModel) for non-0 columns */
    public final void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 0) {
            tableModel.setValueAt (aValue, rowIndex, columnIndex-1);
        } else {
            //XXX do something
        }
    }
    
    public final void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
        //if the model is correctly implemented, this will trigger a change
        //event
        treeModel.valueForPathChanged(path, newValue);
    }

    public boolean isLargeModel() {
        return layout instanceof FixedHeightLayoutCache;
    }
    
    public NodeRowModel getRowNodeModel() {
        return (ProxyTableModel)tableModel;
    }    

    
}

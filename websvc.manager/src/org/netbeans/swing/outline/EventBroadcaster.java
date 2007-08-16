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


package org.netbeans.swing.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/** Responsible for handling tree model events from the user-supplied treemodel
 * portion of a DefaultOutlineModel, translating them into appropriate 
 * TableModelEvents and refiring these events to listeners on the table model.
 * <p>
 * This class could be (and originally was) incorporated directly into 
 * DefaultOutlineModel, but is separated for better readability and separation
 * of concerns.
 *
 * @author  Tim Boudreau
 */
final class EventBroadcaster implements TableModelListener, TreeModelListener, ExtTreeWillExpandListener, TreeExpansionListener {
    
    /** Debugging constant for whether logging should be enabled */
    static boolean log = false;
    
    /** Debugging message counter to differentiate log entries */
    private int logcount = 0;
    
    /** The model we will proxy */
    private DefaultOutlineModel model;
    
    /** The last event sent to treeWillExpand/Collapse, used to compare against the
     * next value sent to treeExpanded/Collapse */
    private TreeExpansionEvent inProgressEvent = null;
    
    /** A TableModelEvent generated in treeWillExpand/Collapse (so, generated when
     * data about the rows/columns in the tree model is still in sync with the
     * TableModel), which will be fired from treeExpanded/Collapsed if the
     * expansion event is not vetoed */
    private TableModelEvent pendingExpansionEvent = null;

    /** Are we in the middle of firing multiple TableModelEvents for a single
     * TreeModelEvent. */
    private boolean inMultiEvent = false;
    
    //Some constants we use to have a single method handle all translated
    //event firing
    private static final int NODES_CHANGED = 0;
    private static final int NODES_INSERTED = 1;
    private static final int NODES_REMOVED = 2;
    private static final int STRUCTURE_CHANGED = 3;
    
    //XXX deleteme - string version of the avoid constants debug output:
    private static final String[] types = new String[] {
        "nodesChanged", "nodesInserted", "nodesRemoved", "structureChanged"
    }; //NOI18N

    /** List of table model listeners */
    private List tableListeners = new ArrayList();
    
    /** List of tree model listeners */
    private List treeListeners = new ArrayList();
    
    
    /** Creates a new instance of EventBroadcaster which will
     * produce events for the passed DefaultOutlineModel model.  */
    public EventBroadcaster(DefaultOutlineModel model) {
        setModel (model);
    }
    
    /** Debug logging */
    private void log (String method, Object o) {
        if (log) {
            if (o instanceof TableModelEvent) {
                //TableModelEvents just give their hash code in toString()
                o = tableModelEventToString ((TableModelEvent) o);
            }
            System.err.println("EB-" + (logcount++) + " " + method + ":" + 
                (o instanceof String ? 
                (String) o : o.toString()));
        }
    }
    
    
//***************** Bean properties/convenience getters & setters ************    
    /** Flag which is set to true while multiple TableModelEvents generated
     * from a single TreeModelEvent are being fired, so clients can avoid
     * any model queries until all pending changes have been fired.  The
     * main thing to avoid is any mid-process repaints, which can only happen
     * if the response to an event will be to call paintImmediately(). 
     * <p>
     * This value is guaranteed to be true for the first of a group of
     * related events, and false if tested in response to the final event.
     */
    public boolean areMoreEventsPending() {
        return inMultiEvent;
    }
    
    /** Get the outline model for which this broadcaster will proxy events*/
    private DefaultOutlineModel getModel() {
        return model;
    }
    
    /** Set the outline model this broadcaster will proxy events for */
    private void setModel(DefaultOutlineModel model) {
        this.model = model;
    }
    
    /** Convenience getter for the proxied model's layout cache */
    private AbstractLayoutCache getLayout() {
        return getModel().getLayout();
    }
    
    /** Convenience getter for the proxied model's TreePathSupport */
    private TreePathSupport getTreePathSupport() {
        return getModel().getTreePathSupport();
    }
    
    /** Convenience getter for the proxied model's user-supplied TreeModel */
    private TreeModel getTreeModel() {
        return getModel().getTreeModel();
    }
    
    /** Convenience getter for the proxied model's user-supplied TableModel (in
     * practice, an instance of ProxyTableModel driven by the tree model and a
     * RowModel) */
    private TableModel getTableModel() {
        return getModel().getTableModel();
    }
   
    
    
//******************* Event source implementation **************************
    
    /** Add a table model listener.  All events fired by this EventBroadcaster
     * will have the OutlineModel as the event source */
    public synchronized void addTableModelListener(TableModelListener l) {
        tableListeners.add (l);
    }
    
    /** Add a tree model listener.  All events fired by this EventBroadcaster
     * will have the OutlineModel as the event source */
    public synchronized void addTreeModelListener(TreeModelListener l) {
        treeListeners.add (l);
    }    
    
    /** Remove a table model listener.  */
    public synchronized void removeTableModelListener(TableModelListener l) {
        tableListeners.remove(l);
    }
    
    /** Remove a tree model listener.  */
    public synchronized void removeTreeModelListener(TreeModelListener l) {
        treeListeners.remove(l);
    }
    
    /** Fire a table change to the list of listeners supplied. The event should
     * already have its source set to be the OutlineModel we're proxying for. */
    private void fireTableChange (TableModelEvent e, TableModelListener[] listeners) {
        //Event may be null for offscreen info, etc.
        if (e == null) {
            return;
        }
        
        assert (e.getSource() == getModel());
        
        log ("fireTableChange", e);
        
        for (int i=0; i < listeners.length; i++) {
            listeners[i].tableChanged(e);
        }
    }
    
    /** Convenience method to fire a single table change to all listeners */
    private void fireTableChange (TableModelEvent e) {
        //Event may be null for offscreen info, etc.
        if (e == null) {
            return;
        }
        inMultiEvent = false;
        TableModelListener[] listeners = getTableModelListeners();
        fireTableChange(e, getTableModelListeners());
    }
    
    /** Fires multiple table model events, setting the inMultiEvent flag
     * as appropriate. */
    private void fireTableChange (TableModelEvent[] e) {
        //Event may be null for offscreen info, etc.
        if (e == null || e.length==0) {
            return;
        }
        
        TableModelListener[] listeners = getTableModelListeners();
        inMultiEvent = e.length > 1;
        try {
            for (int i=0; i < e.length; i++) {
                fireTableChange (e[i], listeners);
                if (i == e.length-1) {
                    inMultiEvent = false;
                }
            }
        } finally {
            inMultiEvent = false;
        }
    }
    
    /** Fetch an array of the currently registered table model listeners */
    private TableModelListener[] getTableModelListeners() {
        TableModelListener[] listeners = null;
        synchronized (this) {
            listeners = new TableModelListener[
                tableListeners.size()];
            
            listeners = (TableModelListener[]) 
                tableListeners.toArray(listeners);
        }
        return listeners;
    }
    
    /** Fire the passed TreeModelEvent of the specified type to all
     * registered TreeModelListeners.  The passed event should already have
     * its source set to be the model. */
    private synchronized void fireTreeChange (TreeModelEvent e, int type) {
        //Event may be null for offscreen info, etc.
        if (e == null) {
            return;
        }
        assert (e.getSource() == getModel());
        
        TreeModelListener[] listeners = null;
        synchronized (this) {
            listeners = new TreeModelListener[treeListeners.size()];
            listeners = (TreeModelListener[]) treeListeners.toArray(listeners);
        }
        
        log ("fireTreeChange-" + types[type], e);
        
        //Now refire it to any listeners
        for (int i=0; i < listeners.length; i++) {
            switch (type) {
                case NODES_CHANGED :
                    listeners[i].treeNodesChanged(e);
                    break;
                case NODES_INSERTED :
                    listeners[i].treeNodesInserted(e);
                    break;
                case NODES_REMOVED :
                    listeners[i].treeNodesRemoved(e);
                    break;
                case STRUCTURE_CHANGED :
                    listeners[i].treeStructureChanged(e);
                    break;
                default :
                    assert false;
            }
        }
    }    
    
//******************* Event listener implementations ************************    
    
    /** Process a change event from the user-supplied tree model.  This
     * method will throw an assertion failure if it receives any event type
     * other than TableModelEvent.UPDATE - the ProxyTableModel should never,
     * ever fire structural changes - only the tree model is allowed to do
     * that. */
    public void tableChanged(TableModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        //The *ONLY* time we should see events here is due to user
        //data entry.  The ProxyTableModel should never change out
        //from under us - all structural changes happen through the
        //table model.
        assert (e.getType() == e.UPDATE) : "Table model should only fire " +
            "updates, never structural changes";
        
        fireTableChange (translateEvent(e));
    }
    
    /** Process a change event from the user-supplied tree model.
     * Order of operations: 
     * <ol><li>Refire the same tree event with the OutlineModel we're
     *   proxying as the source</li>
     * <li>Create one or more table model events (more than one if the
     * incoming event affects discontiguous rows) reflecting the effect
     * of the tree change</li>
     * <li>Call the method with the same signature as this one on the
     * layout cache, so it will update its state appropriately</li>
     * <li>Fire the generated TableModelEvent(s)</li></ol>
     */
    public void treeNodesChanged(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        fireTreeChange (translateEvent(e), NODES_CHANGED);
        
        TableModelEvent[] events = translateEvent(e, NODES_CHANGED);
        getLayout().treeNodesChanged(e);
        fireTableChange(events);
    }
    
    /** Process a node insertion event from the user-supplied tree model 
     * Order of operations: 
     * <ol><li>Refire the same tree event with the OutlineModel we're
     *   proxying as the source</li>
     * <li>Create one or more table model events (more than one if the
     * incoming event affects discontiguous rows) reflecting the effect
     * of the tree change</li>
     * <li>Call the method with the same signature as this one on the
     * layout cache, so it will update its state appropriately</li>
     * <li>Fire the generated TableModelEvent(s)</li></ol>
     */
    public void treeNodesInserted(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        fireTreeChange (translateEvent(e), NODES_INSERTED);
        
        TableModelEvent[] events = translateEvent(e, NODES_INSERTED);
        getLayout().treeNodesInserted(e);
        fireTableChange(events);
    }
    
    /** Process a node removal event from the user-supplied tree model 
     * Order of operations: 
     * <ol><li>Refire the same tree event with the OutlineModel we're
     *   proxying as the source</li>
     * <li>Create one or more table model events (more than one if the
     * incoming event affects discontiguous rows) reflecting the effect
     * of the tree change</li>
     * <li>Call the method with the same signature as this one on the
     * layout cache, so it will update its state appropriately</li>
     * <li>Fire the generated TableModelEvent(s)</li></ol>
     */
    public void treeNodesRemoved(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        fireTreeChange (e, NODES_REMOVED);
        
        TableModelEvent[] events = translateEvent(e, NODES_REMOVED);
        getLayout().treeNodesRemoved(e);
        fireTableChange(events);
    }
    
    /** Process a structural change event from the user-supplied tree model.
     * This will result in a generic &quot;something changed&quot; 
     * TableModelEvent being fired.  */
    public void treeStructureChanged(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        getLayout().treeStructureChanged(e);
        fireTreeChange (e, STRUCTURE_CHANGED);
        
        //If it's a structural change, we need to dump all our info about the
        //existing tree structure - it can be bogus now.  Similar to JTree,
        //this will have the effect of collapsing all expanded paths.  The
        //TreePathSupport takes care of dumping the layout cache's copy of
        //such data
        getTreePathSupport().clear();
        
        //We will just fire a "Something happened. Go figure out what." event.
        fireTableChange (new TableModelEvent (getModel()));
    }
    
    /** Receives a TreeWillCollapse event and constructs a TableModelEvent
     * based on the pending changes while the model still reflects the unchanged
     * state */
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        assert SwingUtilities.isEventDispatchThread();
        
        log ("treeWillCollapse", event);
        
        //Construct the TableModelEvent here, before data structures have
        //changed.  We will fire it from TreeCollapsed if the change is 
        //not vetoed.
        pendingExpansionEvent = translateEvent (event, false);
        log ("treeWillCollapse generated ", pendingExpansionEvent);
        inProgressEvent = event;
    }
    
    /** Receives a TreeWillExpand event and constructs a TableModelEvent
     * based on the pending changes while the model still reflects the unchanged
     * state */
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        assert SwingUtilities.isEventDispatchThread();

        log ("treeWillExpand", event);
        
        //Construct the TableModelEvent here, before data structures have
        //changed.  We will fire it from TreeExpanded if the change is not
        //vetoed
        pendingExpansionEvent = translateEvent (event, true);
        
        log ("treeWillExpand generated", pendingExpansionEvent);
        inProgressEvent = event;
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        assert SwingUtilities.isEventDispatchThread();

        log ("treeExpanded", event);
        
        //FixedHeightLayoutCache tests if the event is null.
        //Don't know how it could be, but there's probably a reason...
        if(event != null) {
            TreePath path = event.getPath();

            //Tell the layout about the change
            if(path != null && getTreePathSupport().isVisible(path)) {
                getLayout().setExpandedState(path, false);
            }
        }

        
        log ("about to fire", pendingExpansionEvent);
        
        //Now fire a change on the owning row so its display is updated (it
        //may have just become an expandable node)
        TreePath path = event.getPath();
        int row = getLayout().getRowForPath(path);
        TableModelEvent evt = new TableModelEvent (getModel(), row, row, 0,
            TableModelEvent.UPDATE);
        fireTableChange(new TableModelEvent[] {evt, pendingExpansionEvent});
        
        pendingExpansionEvent = null;
        inProgressEvent = null;
    }
    
    /** Updates the layout to mark the descendants of the events path as also
     * expanded if they were the last it was expanded, then fires a table change. */
    public void treeExpanded(TreeExpansionEvent event) {
        assert SwingUtilities.isEventDispatchThread();
        
        log ("treeExpanded", event);
        
        //Mysterious how the event could be null, but JTree tests it
        //so we will too.
        if(event != null) {
            updateExpandedDescendants(event.getPath());
        }

        log ("about to fire", pendingExpansionEvent);
        
        //Now fire a change on the owning row so its display is updated (it
        //may have just become an expandable node)
        TreePath path = event.getPath();
        int row = getLayout().getRowForPath(path);
        TableModelEvent evt = new TableModelEvent (getModel(), row, row, 0,
            TableModelEvent.UPDATE);
        fireTableChange(new TableModelEvent[] {evt, pendingExpansionEvent});
        
        pendingExpansionEvent = null;
        inProgressEvent = null;
    }
    
    /** Messaged if the tree expansion event (for which we will have already
     * constructed a TableModelEvent) was vetoed;  disposes of the constructed
     * TableModelEvent in that circumstance. */
    public void treeExpansionVetoed(TreeExpansionEvent event, ExpandVetoException exception) {
        assert SwingUtilities.isEventDispatchThread();
        
        log ("treeExpansionVetoed", exception);
        
        //Make sure the event that was vetoed is the one we're interested in
        if (event == inProgressEvent) {
            //If so, delete the expansion event we thought we were going
            //to use in treeExpanded/treeCollapsed, so that it doesn't
            //stick around forever holding references to objects from the
            //model
            pendingExpansionEvent = null;
            inProgressEvent = null;
        }
    }
    
//******************* Support routines for handling events ******************
    //do I date myself by using the word "routines"? :-)

    /** Re&euml;expand descendants of a newly expanded path which were
     * expanded the last time their parent was expanded */
    private void updateExpandedDescendants(TreePath path) {
        getLayout().setExpandedState(path, true);

        TreePath[] descendants = 
            getTreePathSupport().getExpandedDescendants(path);

        if(descendants.length > 0) {
            for (int i=0; i < descendants.length; i++) {
                getLayout().setExpandedState(descendants[i], true);
            }
        }
    }    

    
//******************* Event translation routines ****************************
    
    /** Creates a TableModelEvent identical to the original except that the
     * column index has been shifted by +1.  This is used to refire events
     * from the ProxyTableModel (generated by RowModel.setValueFor()) as 
     * change events on the OutlineModel. */
    private TableModelEvent translateEvent (TableModelEvent e) {
        TableModelEvent nue = new TableModelEvent (getModel(),
            e.getFirstRow(), e.getLastRow(), e.getColumn()+1, e.getType());
        return nue;
    }
    
    /** Creates an identical TreeModelEvent with the model we are proxying
     * as the event source */
    private TreeModelEvent translateEvent (TreeModelEvent e) {
        //Create a new TreeModelEvent with us as the source
        TreeModelEvent nue = new TreeModelEvent (getModel(), e.getPath(), 
            e.getChildIndices(), e.getChildren());
        return nue;
    }
    
    /** Tranlates a TreeModelEvent into one or more contiguous TableModelEvents 
     */
    private TableModelEvent[] translateEvent (TreeModelEvent e, int type) {

        TreePath path = e.getTreePath();
        int row = getLayout().getRowForPath(path);
        
        //If the node is not expanded, we simply fire a change
        //event for the parent
        boolean inClosedNode = !getLayout().isExpanded(path);
        if (inClosedNode) {
            //If the node is closed, no expensive checks are needed - just
            //fire a change on the parent node in case it needs to update
            //its display
            if (row != -1) {
                switch (type) {
                    case NODES_CHANGED :
                    case NODES_INSERTED :
                    case NODES_REMOVED :
                        return new TableModelEvent[] {
                            new TableModelEvent (getModel(), row, row,
                              0, TableModelEvent.UPDATE)
                        };
                    default: 
                        assert false : "Unknown event type " + type;
                }
            }
            //In a closed node that is not visible, no event needed
            return new TableModelEvent[0];
        }
        
        boolean discontiguous = isDiscontiguous(e);
        
        Object[] blocks;
        if (discontiguous) {
            blocks = getContiguousIndexBlocks(e, type == NODES_REMOVED);
            log ("discontiguous " + types[type] + " event", blocks.length + " blocks");
        } else {
            blocks = new Object[] {e.getChildIndices()};
        }
        
        
        TableModelEvent[] result = new TableModelEvent[blocks.length];
        for (int i=0; i < blocks.length; i++) {
            
            int[] currBlock = (int[]) blocks[i];
            switch (type) {
                case NODES_CHANGED :
                    result[i] = createTableChangeEvent (e, currBlock);
                    break;
                case NODES_INSERTED :
                    result[i] = createTableInsertionEvent (e, currBlock);
                    break;
                case NODES_REMOVED :
                    result[i] = createTableDeletionEvent (e, currBlock);
                    break;
                default :
                    assert false : "Unknown event type: " + type;
            }            
        }
        log ("translateEvent", e);
        log ("generated table events", new Integer(result.length));
        if (log) {
            for (int i=0; i < result.length; i++) {
                log ("  Event " + i, result[i]);
            }
        }
        return result;
    }
    
    /** Translates tree expansion event into an appropriate TableModelEvent
     * indicating the number of rows added/removed at the appropriate index */
    private TableModelEvent translateEvent (TreeExpansionEvent e, boolean expand) {
        //PENDING:  This code should be profiled - the descendent paths search
        //is not cheap, and it might be less expensive (at least if the table
        //does not have expensive painting logic) to simply fire a generic
        //"something changed" table model event and be done with it.
        
        TreePath path = e.getPath();
        
        //Add one because it is a child of the row.
        int firstRow = getLayout().getRowForPath(path) + 1;
        if (firstRow == -1) {
            //This does not mean nothing happened, it may just be that we are
            //a large model tree, and the FixedHeightLayoutCache says the
            //change happened in a row that is not showing.
            
            //TODO:  Just to make the table scrollbar adjust itself appropriately,
            //we may want to look up the number of children in the model and
            //fire an event that says that that many rows were added.  Waiting
            //to see if anybody actually will use this (i.e. fires changes in
            //offscreen nodes as a normal part of usage
            return null;
        }
        
        //Get all the expanded descendants of the path that was expanded/collapsed
        TreePath[] paths = getTreePathSupport().getExpandedDescendants(path);
        
        //Start with the number of children of whatever was expanded/collapsed
        int count = getTreeModel().getChildCount(path.getLastPathComponent());
        
        //Iterate any of the expanded children, adding in their child counts
        for (int i=0; i < paths.length; i++) {
            count += getTreeModel().getChildCount(paths[i].getLastPathComponent());
        }
        
        //Now we can calculate the last row affected for real
        int lastRow = firstRow + count -1;
        
        //Construct a table model event reflecting this data
        TableModelEvent result = new TableModelEvent (getModel(), firstRow, lastRow, 
            TableModelEvent.ALL_COLUMNS, expand ? TableModelEvent.INSERT : 
            TableModelEvent.DELETE);
            
        return result;
    }

    /** Create a change TableModelEvent for the passed TreeModelEvent and the 
     * contiguous subrange of the TreeModelEvent's getChildIndices() value */
    private TableModelEvent createTableChangeEvent (TreeModelEvent e, int[] indices) {
        TableModelEvent result = null;
        TreePath path = e.getTreePath();
        int row = getLayout().getRowForPath(path);
        
        int first = indices[0];
        int last = indices[indices.length-1];
        
        //TODO - does not need to be ALL_COLUMNS, but we need a way to determine
        //which column index is the tree
        result = new TableModelEvent (getModel(), first, last, 
            TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
        
        return result;
    }
    
    /** Create an insertion TableModelEvent for the passed TreeModelEvent and the 
     * contiguous subrange of the TreeModelEvent's getChildIndices() value */
    private TableModelEvent createTableInsertionEvent (TreeModelEvent e, int[] indices) {
        TableModelEvent result = null;

        log ("createTableInsertionEvent", e);
        
        TreePath path = e.getTreePath();
        int row = getLayout().getRowForPath(path);
        
        boolean realInsert = getLayout().isExpanded(path);

        if (realInsert) {
            if (indices.length == 1) {
                //Only one index to change, fire a simple event.  It
                //will be the first index in the array + the row +
                //1 because the 0th child of a node is 1 greater than
                //its row index
                int affectedRow = row + indices[0] + 1;
                result = new TableModelEvent (getModel(), affectedRow, affectedRow, 
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            } else {
                //Find the first and last indices.  Add one since it is at 
                //minimum the first index after the affected row, since it
                //is a child of it.
                int lowest = indices[0] + 1;
                int highest = indices[indices.length-1] + 1;
                result = new TableModelEvent (getModel(), row + lowest, row + highest,
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            }
        } else {
            //Nodes were inserted in an unexpanded parent.  Just fire
            //a change for that row and column so that it gets repainted
            //in case the node there changed from leaf to non-leaf
            result = new TableModelEvent (getModel(), row, row, 
                TableModelEvent.ALL_COLUMNS); //TODO - specify only the tree column
        }        
        return result;
    }
    
    
    /** Create a deletion TableModelEvent for the passed TreeModelEvent and the 
     * contiguous subrange of the TreeModelEvent's getChildIndices() value */
    private TableModelEvent createTableDeletionEvent (TreeModelEvent e, int[] indices) {
        TableModelEvent result = null;
        
        log ("createTableDeletionEvent " + Arrays.asList(toArrayOfInteger(indices)), e);
        
        TreePath path = e.getTreePath();
        int row = getLayout().getRowForPath(path);
        if (row == -1) {
            //XXX could calculate based on last visible row?
            return null;
        }
        
        int countRemoved = indices.length;
        
        //Get the subset of the children in the event that correspond
        //to the passed indices
        Object[] children = getChildrenForIndices(e, indices);
        
        for (int i=0; i < children.length; i++) {
            TreePath childPath = path.pathByAddingChild(children[i]);
            if (getTreePathSupport().isExpanded(childPath)) {
                
                int visibleChildren = 
                    getLayout().getVisibleChildCount(childPath);
                
                if (log) {
                    log (childPath + " has ", new Integer(visibleChildren));
                }
                
                countRemoved += visibleChildren;
            }
            getTreePathSupport().removePath(path);
        }

        //Add in the first index, and add one to it since the 0th
        //will have the row index of its parent + 1
        int firstRow = row + indices[0] + 1;
        
        log ("firstRow", new Integer(firstRow));
        /*
        if (countRemoved == 1) {
            System.err.println("Only one removed: " + (row + indices[0] + 1));
            result = new TableModelEvent (getModel(), firstRow, firstRow, 
                TableModelEvent.ALL_COLUMNS, 
                TableModelEvent.DELETE);
        } else {
         */
            System.err.println("Count removed is " + countRemoved);

            int lastRow = firstRow + (countRemoved - 1);

            System.err.println("TableModelEvent: fromRow: " + firstRow + " toRow: " + lastRow);

            result = new TableModelEvent (getModel(), firstRow, lastRow,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);        
        //}
        
        /* //old code
        
            //Okay, one or more nodes was removed.  The event's tree path
            //will be the parent.  Now we need to find out about any children
            //that were also removed so we can create a TreeModelEvent with
            //the right number of removed rows.
            
            //Note there is a slight impedance mismatch between TreeModel and
            //TableModel here - if we're using a large model layout cache,
            //we don't actually know what was offscreen - the data is already
            //gone from the model, so even if we know it was expanded, we
            //can't find out how many children it had.
            
            //The only thing this really affects is the scrollbar, and in
            //fact, the standard JTable UIs will update it correctly, since
            //the scrollbar will read getRowCount() to calculate its position.
            //In theory, this could break on a hyper-efficient TableUI that
            //attempted to manage scrollbar position *only* based on the
            //content of table model events.  That's pretty unlikely; but if
            //it happens, the solution is for Outline.getPreferredSize() to
            //proxy the preferred size from the layout cache
            
            TreePath path = e.getTreePath();
            boolean lastRemoveWasExpanded = getTreePathSupport().isExpanded(path);
            int countRemoved = 1;
            
            //See if it's expanded - if it wasn't we're just going to blow
            //away one row anyway
            if (lastRemoveWasExpanded) {
                Object[] kids = e.getChildren();
                
                //TranslateEvent uses countRemoved to set the TableModelEvent
                countRemoved = kids.length;
                
                //Iterate the removed children
                for (int i=0; i < kids.length; i++) {
                    //Get the child's path
                    TreePath childPath = path.pathByAddingChild(kids[i]);
                    
                    //If it's not expanded, we don't care
                    if (getTreePathSupport().isExpanded(childPath)) {
                        //Find the number of *visible* children.  This may not
                        //be all the children, but it's the best information we have.
                        int visibleChildren = 
                            getLayout().getVisibleChildCount(childPath);

                        //add in the number of visible children
                        countRemoved += visibleChildren;
                    }
                    //Kill any references to the dead path to avoid memory leaks
                    getTreePathSupport().removePath(childPath);
                }
            }
            
            //Tell the layout what happened, now that we've mined it for data
            //about the visible children of the removed paths
            getLayout().treeNodesRemoved(e);        
            
                boolean realRemove = lastRemoveWasExpanded;//getLayout().isExpanded(path);
                if (realRemove) {
                    System.err.println("Nodes removed from open countainer");
                    int[] indices = e.getChildIndices();
                    
                    //Comments in FixedHeightLayoutCache suggest we cannot
                    //assume array is sorted, though it should be
                    Arrays.sort(indices);
                    if (indices.length == 0) {
                        //well, that's a little weird
                        return null;
                    } else if (countRemoved == 1) {
                        System.err.println("Only one removed: " + (row + indices[0] + 1));
                        return new TableModelEvent (this, row + indices[0] + 1,
                            row + indices[0] + 1, TableModelEvent.ALL_COLUMNS, 
                            TableModelEvent.DELETE);
                    }
                    System.err.println("Count removed is " + countRemoved);
                    
                    //Add in the first index, and add one to it since the 0th
                    //will have the row index of its parent + 1
                    int firstRow = row + indices[0] + 1;
                    int lastRow = firstRow + (countRemoved - 1);
                    
                    System.err.println("TableModelEvent: fromRow: " + firstRow + " toRow: " + lastRow);
                     
                    return new TableModelEvent (this, firstRow, lastRow,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
                } else {
                    System.err.println("Nodes removed from a closed container. Change for row " + row);
                    //Nodes were removed in an unexpanded parent.  Just fire
                    //a change for that row and column so that it gets repainted
                    //in case the node there changed from leaf to non-leaf
                    TableModelEvent evt = new TableModelEvent (this, row, row, 0); //XXX 0 may not be tree column
                    System.err.println(" Returning " + evt);
                    return evt;
                }        
         */
        
        return result;
    }


//**************** Static utility routines *****************************    

    /** Determine if the indices referred to by a TreeModelEvent are
     * contiguous.  If they are not, we will need to generate multiple
     * TableModelEvents for each contiguous block */
    private static boolean isDiscontiguous (TreeModelEvent e) {
        int[] indices = e.getChildIndices();
        if (indices.length == 1) {
            return false;
        }
        Arrays.sort(indices);
        int lastVal = indices[0];
        for (int i=1; i < indices.length; i++) {
            if (indices[i] != lastVal + 1) {
                return true;
            } else {
                lastVal++;
            }
        }
        return false;
    }
    
    /** Returns an array of int[]s each one representing a contiguous set of 
     * indices in the tree model events child indices - each of which can be
     * fired as a single TableModelEvent.  The length of the return value is
     * the number of TableModelEvents required to represent this TreeModelEvent.
     * If reverseOrder is true (needed for remove events, where the last indices
     * must be removed first or the indices of later removals will be changed),
     * the returned int[]s will be sorted in reverse order, and the order in
     * which they are returned will also be from highest to lowest. */
    private static Object[] getContiguousIndexBlocks (TreeModelEvent e, boolean reverseOrder) {
        int[] indices = e.getChildIndices();
        
        //Quick check if there's only one index
        if (indices.length == 1) {
            return new Object[] {indices};
        }
        
        //The array of int[]s we'll return
        ArrayList al = new ArrayList();
        
        //Sort the indices as requested
        if (reverseOrder) {
            inverseSort (indices);
        } else {
            Arrays.sort (indices);
        }


        //The starting block
        ArrayList currBlock = new ArrayList(indices.length / 2);
        al.add(currBlock);
        
        //The value we'll check against the previous one to detect the
        //end of contiguous segment
        int lastVal = -1;
        
        //Iterate the indices
        for (int i=0; i < indices.length; i++) {
            if (i != 0) {
                //See if we've hit a discontinuity
                boolean newBlock = reverseOrder ? indices[i] != lastVal - 1 :
                    indices[i] != lastVal + 1;
                    
                if (newBlock) {
                    currBlock = new ArrayList(indices.length - 1);
                    al.add(currBlock);
                }
            }
            currBlock.add (new Integer(indices[i]));
            lastVal = indices[i];
        }
        
        for (int i=0; i < al.size(); i++) {
            ArrayList curr = (ArrayList) al.get(i);
            Integer[] ints = (Integer[]) curr.toArray(new Integer[0]);
            
            al.set(i, toArrayOfInt(ints));
        }
        
        return al.toArray();
    }
    
    /** Get the children from a TreeModelEvent associated with the set of
     * indices passed. */
    private Object[] getChildrenForIndices (TreeModelEvent e, int[] indices) {
        //XXX performance - better way to do this may be to have
        //getContinguousIndexBlocks instead construct sub-treemodelevents - 
        //that would save having to do these iterations later to extract the
        //children.
        
        //At the same time, discontiguous child removals are relatively rare
        //events - optimizing them heavily may not be a good use of time.
        Object[] children = e.getChildren();
        int[] allIndices = e.getChildIndices();
        
        ArrayList al = new ArrayList();
        
        for (int i=0; i < indices.length; i++) {
            int pos = Arrays.binarySearch (allIndices, indices[i]);
            if (pos > -1) {
                al.add (children[pos]);
            }
            if (al.size() == indices.length) {
                break;
            }
        }
        return al.toArray();
    }
    
    
    /** Converts an Integer[] to an int[] */
    private static int[] toArrayOfInt (Integer[] ints) {
        int[] result = new int[ints.length];
        for (int i=0; i < ints.length; i++) {
            result[i] = ints[i].intValue();
        }
        return result;
    }
    
    /** Converts an Integer[] to an int[] */
    //XXX deleteme - used for debug logging only
    private static Integer[] toArrayOfInteger (int[] ints) {
        Integer[] result = new Integer[ints.length];
        for (int i=0; i < ints.length; i++) {
            result[i] = new Integer(ints[i]);
        }
        return result;
    }
    
    
    /** Sort an array of ints from highest to lowest */
    private static void inverseSort (int[] array) {
        //XXX replace with a proper sort algorithm at some point -
        //this is brute force
        for (int i=0; i < array.length; i++) {
            array[i] *= -1;
        }
        Arrays.sort(array);
        for (int i=0; i < array.length; i++) {
            array[i] *= -1;
        }
    }
    
    private static String tableModelEventToString (TableModelEvent e) {
        StringBuffer sb = new StringBuffer();
        sb.append ("TableModelEvent ");
        switch (e.getType()) {
            case TableModelEvent.INSERT : sb.append ("insert ");
                 break;
            case TableModelEvent.DELETE : sb.append ("delete ");
                 break;
            case TableModelEvent.UPDATE : sb.append ("update ");
                 break;
            default : sb.append ("Unknown type " + e.getType());
        }
        sb.append ("from ");
        switch (e.getFirstRow()) {
            case TableModelEvent.HEADER_ROW : sb.append ("header row ");
                break;
            default : sb.append (e.getFirstRow());
                      sb.append (' ');
        }
        sb.append ("to ");
        sb.append (e.getLastRow());
        sb.append (" column ");
        switch (e.getColumn()) {
            case TableModelEvent.ALL_COLUMNS :
                sb.append ("ALL_COLUMNS");
                break;
            default : sb.append (e.getColumn());
        }
        return sb.toString();
    }
}

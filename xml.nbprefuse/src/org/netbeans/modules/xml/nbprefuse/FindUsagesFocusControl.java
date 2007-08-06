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

package org.netbeans.modules.xml.nbprefuse;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.xml.nbprefuse.util.GraphUtilities;
import org.openide.filesystems.FileObject;
import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class FindUsagesFocusControl extends NbFocusControl{
    
    protected SelectionMode selectionMode = SelectionMode.SINGLE;
    protected PropertyChangeSupport pcSupport;
    
    public enum SelectionMode {SINGLE, MULTIPLE};
    
    
    /**
     * Creates a new FocusControl that changes the focus to another item
     * when that item is clicked once.
     */
    public FindUsagesFocusControl() {
        super();
        initialize();
        
    }
    
    /**
     * Creates a new FocusControl that changes the focus to another item
     * when that item is clicked once.
     * @param focusGroup the name of the focus group to use
     */
    public FindUsagesFocusControl(String focusGroup) {
        super(focusGroup);
        initialize();
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param clicks the number of clicks needed to switch the focus.
     */
    public FindUsagesFocusControl(int clicks) {
        super(clicks);
        initialize();
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param focusGroup the name of the focus group to use
     * @param clicks the number of clicks needed to switch the focus.
     */
    public FindUsagesFocusControl(String focusGroup, int clicks) {
        super(focusGroup, clicks);
        initialize();
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param clicks the number of clicks needed to switch the focus.
     * @param act an action run to upon focus change
     */
    public FindUsagesFocusControl(int clicks, String act) {
        super(clicks,  act);
        initialize();
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param focusGroup the name of the focus group to use
     * @param clicks the number of clicks needed to switch the focus.
     * @param act an action run to upon focus change
     */
    public FindUsagesFocusControl(String focusGroup, int clicks, String act) {
        super(focusGroup, clicks, act);
        initialize();
    }
    
    public void itemPressed(VisualItem item, MouseEvent e) {
	super.itemPressed(item, e);
	if (e.isPopupTrigger()) {
	    // select node before popup shows
	    selectNode(item,e);
	}
    }
    
    public void itemReleased(VisualItem item, MouseEvent e) {
	super.itemReleased(item, e);
	if (e.isPopupTrigger()) {
	    // select node before popup shows
	    selectNode(item,e);
	}
    }
    
    private void selectNode(VisualItem item, MouseEvent e) {
	// Only handle nodes (not edges)
	Visualization vis = item.getVisualization();
	NodeItem nodeItem = null;
	if (item instanceof NodeItem){
	    nodeItem = NodeItem.class.cast(item);
	} else {
	    return;
	}
	selectNode(e, nodeItem, vis);
	runActivity(vis);
    }
    
    /**
     * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemClicked(VisualItem item, MouseEvent e) {
        if ( UILib.isButtonPressed(e, button) &&
                e.getClickCount() == ccount) {
            Visualization vis = item.getVisualization();
            if (ccount == 1){
		selectNode(item,e);
            }   // end if ccount == 1
            else if (ccount == 2){
                // expand or collapse Schema File node or the AggregateItem
                //  that contains the File Node
                int aggregateItemFileGroup = -1;
                if (item.canGetBoolean(AnalysisConstants.IS_FILE_GROUP_AGGREGATE) &&
                        item.getBoolean(AnalysisConstants.IS_FILE_GROUP_AGGREGATE) &&
                        item.canGetInt(AnalysisConstants.ID)){
                    aggregateItemFileGroup = item.getInt(AnalysisConstants.ID);
                }
                if ((item.canGetBoolean(AnalysisConstants.IS_FILE_NODE)
                && item.getBoolean(AnalysisConstants.IS_FILE_NODE) )
                ||
                        aggregateItemFileGroup > -1) {
                    // If the item is the AggregateItem containing the File Node
                    //   find the FileNode
                    NodeItem fileNode = null;
                    if (item instanceof AggregateItem){
                        fileNode = findFileNode(aggregateItemFileGroup, item);
                    } else {
                        fileNode =  (NodeItem)item;
                    }
                    GraphUtilities.expandCollapseFileNode(fileNode);
                }   // end if FileNode or AggregateItem containing the FileNode
                
                // Double click on a Component node:
                // Perform the default action on the UIHelper org.openide.nodes.Node
                // that is stored in the NodeItem.
                //  (For a SchemaComponent, the default action is to open the
                //   schema view with the component highlighted
                // TODO run Default Action on double click
                
                // Use Object instead of Component in order
                //  to remove dependency on XAM module
              /*  else if (item.canGet(AnalysisConstants.XAM_COMPONENT, Object.class)) {
                    Object comp = item.get(AnalysisConstants.XAM_COMPONENT);
                    assert item.canGet(
                            AnalysisConstants.OPENIDE_NODE, org.openide.nodes.Node.class):
                                "NodeItem is missing UIHelper display org.openide.nodes.Node instance.";
                    org.openide.nodes.Node helperNode =
                            (org.openide.nodes.Node)item.get(AnalysisConstants.OPENIDE_NODE);
                    assert helperNode != null:"UIHelper node should not be null";
                    Action preferredAction = helperNode.getPreferredAction();
                    if (!(preferredAction == null && comp == null)){
                        final ActionEvent event = new ActionEvent(
                                comp,
                                0,
                                "");    //NOI18N
                        preferredAction.actionPerformed(event);
                    }
                }*/
                
                else if (item.canGet(AnalysisConstants.REFACTORING_ELEMENT, RefactoringElement.class)) {
                    RefactoringElement comp = (RefactoringElement) item.get(AnalysisConstants.REFACTORING_ELEMENT);
                    
                    if(comp != null)
                        comp.openInEditor();
                }
  
            if (item instanceof NodeItem){
                selectNode(e, NodeItem.class.cast(item), vis);
            }
            runActivity(vis);
            
            
        }// end if ccount ==2
    }
}

/**
 * Finds the schema File Node in the AggregateItem
 * The AggregateItem contains the schema file node and
 *  the SchemaComponent nodes for the file
 *
 */
private NodeItem findFileNode(final int aggregateItemFileGroup, final VisualItem item) {
    NodeItem fileNode = null;
    AggregateItem agIt = AggregateItem.class.cast(item);
    Iterator agItems = agIt.items();
    while(agItems.hasNext()){
        VisualItem agItem = (VisualItem)agItems.next();
        int fileGroup = -1;
        if (agItem.canGetInt(AnalysisConstants.FILE_NODE_FILE_GROUP)) {
            fileGroup = agItem.getInt(AnalysisConstants.FILE_NODE_FILE_GROUP);
            if (fileGroup == aggregateItemFileGroup &&
                    agItem.canGetBoolean(AnalysisConstants.IS_EXPANDED)){
                fileNode =  NodeItem.class.cast(agItem);
                break;
            }
        }
    }
    return fileNode;
}

private void selectNode(final MouseEvent e, final NodeItem nodeItem, final Visualization vis) {
    TupleSet ts = vis.getFocusGroup(Visualization.FOCUS_ITEMS);
    NodeItem curSelected = null;
    List<NodeItem> selected = getNodeItemList(ts);
    if (!selected.isEmpty()){
        // assuming SINGLE selection mode
        curSelected = selected.get(0);
//            curSelected.setStroke(AnalysisConstants.UNSELECTED_STROKE);
    }
    // get graph node object,
    //   which is either a SchemaComponent or a FileObject
    Object curComp = getGraphNodeUserObject(curSelected);
    Object newComp = getGraphNodeUserObject(nodeItem);
    
//    HighlightManager hm = HighlightManager.getDefault();
//    Lookup hmLookup = Lookups.singleton(hm);
    
    
    boolean ctrl = e.isControlDown();
    if ( !ctrl) {
        pcSupport.firePropertyChange(
                AnalysisViewer.PROP_GRAPH_NODE_SELECTION_CHANGE,
                curComp, newComp);    // select
        curFocus = nodeItem;
        ts.setTuple(nodeItem);
//        highlightSchemaComponent(nodeItem, hmLookup);
//            nodeItem.setStroke(AnalysisConstants.SELECTED_STROKE);
        
    } else if ( ts.containsTuple(nodeItem) ) {
        pcSupport.firePropertyChange(
                AnalysisViewer.PROP_GRAPH_NODE_SELECTION_CHANGE,
                newComp, null);    // unselect
        ts.removeTuple(nodeItem);
        // MouseoverActionControl sets MOUSEOVER to true
        //  Set MOUSEOVER to false so the node is
        //  rendered without MOUSEOVER color
        //  This mimics Windows selection behaviour for trees and
        //     lists.
        if (nodeItem instanceof NodeItem &&
                nodeItem.canSetBoolean(AnalysisConstants.MOUSEOVER)){
            nodeItem.setBoolean(AnalysisConstants.MOUSEOVER, false);
        }
        
//            nodeItem.setStroke(AnalysisConstants.UNSELECTED_STROKE);
//        HighlightProvider.hideResults(hmLookup);
        
    } else {
        if (selectionMode == SelectionMode.MULTIPLE){
            // This shouldn't happen --
            // Find Usages node selection is always SINGLE
            ts.addTuple(nodeItem);
        } else {
            // AnalysisViewer is listening to this event
            //  AnalysisViewer will notify RefactoringPanel
            //   RefactoringPanel will select the corresponding node
            //      in the RefactoringPanel explorer JTree
            pcSupport.firePropertyChange(
                    AnalysisViewer.PROP_GRAPH_NODE_SELECTION_CHANGE,
                    curComp, newComp);    // select
            curFocus = nodeItem;
            ts.setTuple(nodeItem);
//            highlightSchemaComponent(nodeItem, hmLookup);
        }
    }
}

//private void highlightSchemaComponent(final NodeItem nodeItem, final Lookup hmLookup) {
//    HighlightProvider.hideResults(hmLookup);
//    if (nodeItem.canGet(AnalysisConstants.XAM_COMPONENT, Component.class)){
//        Component c = (Component)nodeItem.get(AnalysisConstants.XAM_COMPONENT);
//        if (c instanceof SchemaComponent){
//            HighlightProvider.showResults(
//                    Collections.singleton(SchemaComponent.class.cast(c)),
//                    hmLookup);
//        }
//    }
//}

// ***************************************************************************
//  Extensions
// ***************************************************************************



private void initialize() {
    pcSupport = new PropertyChangeSupport(this);
}

/**
 *  WhereUsedView.showView() adds the AnalysisViewer instance as a listener
 *    AnalysisViewer then notifies RefactoringPanel
 *     so that RefactoringPanel can select the corresponding
 *      RefactoringPanel JTreenode
 *
 */
public void addGraphNodeSelectionChangeListener(PropertyChangeListener l){
    pcSupport.addPropertyChangeListener(
            AnalysisViewer.PROP_GRAPH_NODE_SELECTION_CHANGE, l);
}



/**
 * @param ts should be a set of NodeItems
 *
 */
private List<NodeItem> getNodeItemList(TupleSet ts){
    ArrayList<NodeItem> list = null;
    int count = ts.getTupleCount();
    if (count  > 0){
        list = new ArrayList<NodeItem>(count);
    } else {
        return Collections.emptyList();
    }
    Iterator it = ts.tuples();
    while (it.hasNext()){
        // should only be one (Single select mode)
        list.add((NodeItem)it.next());
    }
    return list;
}


/**
 * the graph nodes can represent XAM Components or
 * FileObjects
 *
 */
private Object getGraphNodeUserObject(NodeItem item ){
    if (item == null){
        return null;
    }

    FileObject fo = (FileObject) item.get(AnalysisConstants.FILE_OBJECT);
    if (fo != null) {
        return fo;
    }
    return item.get(AnalysisConstants.USER_OBJECT);
}
}

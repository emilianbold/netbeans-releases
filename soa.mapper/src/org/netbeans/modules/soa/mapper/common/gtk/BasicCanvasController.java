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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>
 *
 * Title: BasicCanvasController </p> <p>
 *
 * Description: Controller for the canvas </p> <p>
 *
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public class BasicCanvasController
     implements ICanvasController {

    /**
     * Loger used for loging messages
     */
    private static final Logger mTheLogger =
            Logger.getLogger(BasicCanvasController.class.getName());

    /**
     * Canvas
     */
    protected ICanvas mCanvas;

    /**
     * Constructor
     */
    public BasicCanvasController() { }

    /**
     * Constructor
     *
     * @param canvas - the canvas
     */
    public BasicCanvasController(ICanvas canvas) {
        mCanvas = canvas;
    }

    /**
     * Set the canvas.
     *
     * @param canvas  the canvas this controller is controlling.
     */
    public void setCanvas(ICanvas canvas) {
        mCanvas = canvas;
    }

    /**
     * Sets the data model
     *
     * @param model - the data model
     */
    public void setDataModel(Object model) { }

    /**
     * Sets the view manager
     *
     * @param viewManager - the view manager
     */
    public void setViewManager(Object viewManager) { }

    /**
     * Sets the view manager
     *
     * @return             The viewManager value
     */
    public Object getViewManager() {
        return null;
    }

    /**
     * Retrieves the data model
     *
     * @return Object - maybe null
     */
    public Object getDataModel() {
        return null;
    }

    /**
     * Handles mouse pressed
     *
     * @param data - canvas mouse data
     * @return boolean
     */
    public boolean handleMouseDown(ICanvasMouseData data) {
        mTheLogger.finest("handleMouseDown(): " + data.toString());
        mCanvas.doDefaultMouseDown(data);
        return false;
    }

    /**
     * Handles when mouse us up
     *
     * @param data - canvas mouse data
     * @return boolean
     */
    public boolean handleMouseUp(ICanvasMouseData data) {
        mTheLogger.finest("handleMouseUp(): " + data.toString());
        mCanvas.doDefaultMouseUp(data);
        return false;
    }

    /**
     * Handles when mouse is double clicked
     *
     * @param data - canvas mouse data
     * @return boolean
     */
    public boolean handleMouseDblClick(ICanvasMouseData data) {
        mTheLogger.finest("handleMouseDblClick(): " + data.toString());
        mCanvas.doDefaultMouseDblClick(data);
        return false;
    }

    /**
     * Handles when mouse is moved
     *
     * @param data - canvas mouse data
     * @return boolean
     */
    public boolean handleMouseMove(ICanvasMouseData data) {
        mCanvas.doDefaultMouseMove(data);
        return false;
    }

    /**
     * Handles when mouse clicks
     *
     * @param data - canvas mouse data
     * @return boolean
     */
    public boolean handleMouseClick(ICanvasMouseData data) {
        mTheLogger.finest("handleMouseClick(): " + data.toString());
        mCanvas.doDefaultMouseClick(data);
        return false;
    }

    /**
     * Handles delete selection
     *
     * @return   Description of the Return Value
     */
    public boolean handleDeleteSelection() {
        return mCanvas.doDefaultDeleteSelection();
    }

    /**
     * Deletes all nodes that are not group nodes.
     *
     */
    public void handleDeleteNonGroupNodes() {
        Collection nodes = mCanvas.getSelectedNodes();
        if (nodes != null) {
            Collection list = new ArrayList();
            Iterator iter = nodes.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (!(obj instanceof ICanvasGroupNode)) {
                    list.add(obj);
                }
            }
            mCanvas.removeNodes(list);
        }
    }

    /**
     * Overrides the dragOver method to drag and drop the selected
     * icon to the canvas
     *
     * @param event - the drag source drop event
     * @return boolean
     */
    public boolean handleDragOver(DropTargetDragEvent event) {
        return false;
    }

    public void handleDragEnter(DropTargetDragEvent dtde) {
    }
    
    public void handleDragExit(DropTargetEvent dte) {
    }
    
    public void handleDragGestureRecognized(DragGestureEvent dge) {
    }
    
    /**
     * Overrides the drop method to drop the selected icon to the canvas
     *
     * @param event - Drop target drop event
     * @return       Description of the Return Value
     */
    public boolean handleDrop(DropTargetDropEvent event) {
        return false;
    }

    /**
     * Overrides the dragDropEnd method to drag and drop the selected
     * icon to the canvas
     *
     * @param event - drag source drop event
     * @return boolean
     */
    public boolean handleDragDropEnd(DragSourceDropEvent event) {
        return false;
    }

    /**
     * Handles canvas updates
     *
     * @param id - integer representation of the id
     * @param dataList - list of data
     * @return  boolean
     */
    public boolean handleCanvasUpdates(int id, List dataList) {
        switch (id) {
        case ICanvasController.NODE_NAME_CHANGE:
            handleNodeNameChange(dataList);
            break;
        default:
            break;
        }
        return true;
    }

    /**
     * Handles add link
     *
     * @param from - from canvas node
     * @param to - to canvas node
     * @return boolean
     */
    public boolean handleAddLink(ICanvasNode from, ICanvasNode to) {
        return false;
    }

    /**
     * Handles add link
     *
     * @param from - from canvas node
     * @param location - point to add the node
     * @return boolean
     */
    public boolean handleAddLink(
    ICanvasNode fromNode, 
    Point modelLocation,
    Point viewLocation,
    int dropAction) {
        return false;
    }
    
    /**
     * Handles add link
     *
     * @param from - from canvas node
     * @param to - to canvas node
     * @param isComponentNode  Description of the Parameter
     * @param isWithBinding    Description of the Parameter
     * @return  boolean
     */
    public boolean handleAddLink(ICanvasNode from, ICanvasNode to,
        boolean isComponentNode, boolean isWithBinding) {
        return false;
    }

    /**
     * Handles update link
     *
     * @param from - from canvas node
     * @param to - to canvas node
     * @param isComponentNode  Description of the Parameter
     * @param isWithBinding    Description of the Parameter
     * @return  boolean
     */
    public boolean updateLink(ICanvasNode from, ICanvasNode to,
        ICanvasNode mSourceNode, boolean isWithBinding) {
        return false;
    }

    /**
     * Handles node name change
     *
     * @param dataList - the data list
     */
    protected void handleNodeNameChange(List dataList) { }

}

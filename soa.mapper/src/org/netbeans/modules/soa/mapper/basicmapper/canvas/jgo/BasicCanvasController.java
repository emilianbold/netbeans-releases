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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;
import org.netbeans.modules.soa.mapper.basicmapper.MapperLink;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicAccumulatingMethoidNode;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicMethoidNode;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IBasicDragController;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperLinkFromLinkRequest;
import org.netbeans.modules.soa.mapper.common.IMapperLinkFromNodeRequest;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseData;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;

/**
 * <p>
 *
 * Title: </p>BasicCanvasController <p>
 *
 * Description: </p>BasicCanvasController provides the implemenation of the mapper canvas
 * controller. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class BasicCanvasController
     extends AbstractCanvasController {
    /**
     * The drop data flavor for the drop event from palette.
     */
    private static final DataFlavor DROP_DATAFLAVOR =
        MapperUtilities.getJVMLocalObjectDataFlavor();

    /**
     * the log instance of this class
     */
    private Logger LOGGER = Logger.getLogger(BasicCanvasController.class.getName());
        
    private Cursor mOriginalDragCursor;
    

    /**
     * Creates a new BasicCanvasController object.
     */
    public BasicCanvasController() { }

    /**
     * Return true if a new link creates and contains the specified from and to
     * nodes, false otherwise.
     *
     * @param from  the from canvas node
     * @param to    the to canvas node
     * @return      true if a new link creates and contains the specified from
     *      and to nodes, false otherwise.
     */
    public boolean handleAddLink(ICanvasNode from, ICanvasNode to) {
        if (!(from instanceof ICanvasFieldNode) || !(to instanceof ICanvasFieldNode)) {
            return false;
        }

        MapperLink newLink = new MapperLink(
            ((ICanvasFieldNode) from).getFieldNode(),
            ((ICanvasFieldNode) to).getFieldNode());

        this.requestNewLink(newLink);

        return true;
    }

    /**
     * Handles creation of a new link at the specified location.
     * The link may or may not actually be created... always returns true.
     *
     * @param node  the canvas node
     * @param to    the target location
     * @return      true
     */
    private boolean handleAddLink(
    final IMapperLink link, 
    final Point modelLocation,
    final Point viewLocation,
    final int dropAction) {
        IMapperLinkFromLinkRequest linkRequest = new IMapperLinkFromLinkRequest() {
            public IMapperLink getSourceLink() {
                return link;
            }
            public Point getModelTargetLocation() {
                return modelLocation;
            }
            public Point getViewTargetLocation() {
                return viewLocation;
            }
            public int getDropAction() {
                return dropAction;
            }
        };
        this.requestNewLink(linkRequest);
        return true;
    }
    
    /**
     * Handles creation of a new link at the specified location.
     * The link may or may not actually be created... always returns true.
     *
     * @param node  the canvas node
     * @param to    the target location
     * @return      true
     */
    public boolean handleAddLink(
    final ICanvasNode node, 
    final Point modelLocation,
    final Point viewLocation,
    final int dropAction) {
        IMapperLinkFromNodeRequest linkRequest = new IMapperLinkFromNodeRequest() {
            public IMapperNode getSourceNode() {
                return ((ICanvasFieldNode) node).getFieldNode();
            }
            public Point getModelTargetLocation() {
                return modelLocation;
            }
            public Point getViewTargetLocation() {
                return viewLocation;
            }
            public int getDropAction() {
                return dropAction;
            }
        };
        this.requestNewLink(linkRequest);
        return true;
    }
    
    /**
     * Return true if a new link creates and contains the specified from and to
     * nodes, false otherwise. This method calls
     * handleAddLink(ICanvasNode,ICanvasNode).
     *
     * @param fromNode         the from canvas node
     * @param toNode           the to canvas node
     * @param isComponentNode  flag indicates component node
     * @param isWithBinding    flag indicates with binding
     * @return                 true if a new link creates and contains the
     *      specified from and to nodes, false otherwise.
     */
    public boolean handleAddLink(ICanvasNode fromNode, ICanvasNode toNode, boolean isComponentNode,
        boolean isWithBinding) {
        return handleAddLink(fromNode, toNode);
    }

    /**
     * Deletes all nodes that are not group nodes.
     */
    public void handleDeleteNonGroupNodes() {
        Collection nodes = getCanvas().getSelectedNodes();

        if (nodes != null) {
            Collection list = new ArrayList();
            Iterator iter = nodes.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();

                if (!(obj instanceof ICanvasGroupNode)) {
                    list.add(obj);
                }
            }
            getCanvas().removeNodes(list);
        }
    }

    /**
     * Return true if delete selection is successful, false otherwise.
     *
     * @return   true if delete selection is successful, false otherwise.
     */
    public boolean handleDeleteSelection() {
        if (!((Component) getCanvas()).isEnabled()
            || !((Component) getCanvas()).isVisible()) {
            return false;
        }

        Collection selectedNodes = this.getCanvas().getSelectedNodes();
        Collection selectedLinks = this.getCanvas().getSelectedLinks();

        Iterator iter = selectedLinks.iterator();

        while (iter.hasNext()) {
            handleDeleteLink((ICanvasLink) iter.next());
        }

        iter = selectedNodes.iterator();

        while (iter.hasNext()) {
            handleDeleteNode((ICanvasNode) iter.next());
        }

        return true;
    }

    /**
     * Return true if the drag drop end is handle successfully, false otherwise.
     * This method is not applicable, it always return false.
     *
     * @param event  the DragSourceDropEvent
     * @return       always false;
     */
    public boolean handleDragDropEnd(DragSourceDropEvent event) {
        return false;
    }

    /**
     * Return true if the drop is handled successfully, false otherwise. This
     * method checks if drop object is java local object, and returns
     * handleDropObject(DropTargetDropEvent, Object). If not, it return false.
     *
     * @param event  DropTargetDropEvent event
     * @return       true if the drop is handled successfully, false otherwise.
     */
    public boolean handleDrop(DropTargetDropEvent event) {
        if (getView() instanceof IBasicMapperView
            && !((IBasicMapperView) getView()).isMapable()) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return true;
        }
        LOGGER.finest("CanvasController handleDrop(): " + event);

        Transferable transferable = event.getTransferable();

        try {
            if (transferable.isDataFlavorSupported(DROP_DATAFLAVOR)) {
                return handleDropObject(
                    event,
                    transferable.getTransferData(DROP_DATAFLAVOR));
            }
        } catch (java.io.IOException io) {
            io.printStackTrace(System.err);
        } catch (java.awt.datatransfer.UnsupportedFlavorException u) {
            u.printStackTrace(System.err);
        }

        return false;
    }

    /**
     * Return true if handles mouse clicks successfully, false otherwise. This
     * method checks if the click is to expend or collese the group node. If
     * yes, it calls the CanvasMethoidNode expend or collapse, otherwise, it
     * delgates the drop event back to canvas by calling
     * doDefaultMouseClick(ICanvasMouseData).
     *
     * @param data  the mouse data of the canvas
     * @return      Description of the Return Value
     */
    public boolean handleMouseClick(ICanvasMouseData data) {
        if (!((Component) getCanvas()).isEnabled()
            || !((Component) getCanvas()).isVisible()) {
            return false;
        }

        Point clickPoint = data.getModelLocation();
        JGoObject clickObj = ((JGoView) getCanvas()).pickDocObject(clickPoint, true);

        if ((clickObj != null)
            && clickObj instanceof BasicCanvasMethoidNode
            && ((BasicCanvasMethoidNode) clickObj).isInButton(clickPoint)) {
            BasicCanvasMethoidNode methoidNode = (BasicCanvasMethoidNode) clickObj;

            if (methoidNode.isExpanded()) {
                methoidNode.collapse();
            } else {
                methoidNode.expand();
            }

            return true;
        }

        getCanvas().doDefaultMouseClick(data);

        return false;
    }

    /**
     * Return true if handles mouse double clicked successfully, false,
     * otherwise. This method delgates the double clicks event back to canvas by
     * calling doDefaultMouseDblClick(ICanvasMouseData) and return false.
     *
     * @param data  the mouse event data.
     * @return      true if handles mouse double clicked successfully, false,
     *      otherwise.
     */
    public boolean handleMouseDblClick(ICanvasMouseData data) {
        if (!((Component) getCanvas()).isEnabled()
            || !((Component) getCanvas()).isVisible()) {
            return false;
        }
        getCanvas().doDefaultMouseDblClick(data);
        return false;
    }

    /**
     * Return true if handles mouse button pressed successfully, false,
     * otherwise. This method delgates the button pressed event back to canvas
     * by calling doDefaultMouseDown(ICanvasMouseData) and return false.
     *
     * @param data  the mouse event data.
     * @return      true if handles mouse button pressed successfully, false,
     *      otherwise.
     */
    public boolean handleMouseDown(ICanvasMouseData data) {
        if (
            !((Component) getCanvas()).isEnabled()
            || !((Component) getCanvas()).isVisible()) {
            return false;
        }
        getCanvas().doDefaultMouseDown(data);
        return false;
    }

    /**
     * Return true if handles mouse move successfully, false, otherwise. This
     * method delgates the mouse move event back to canvas by calling
     * doDefaultMouseMove(ICanvasMouseData) and return false.
     *
     * @param data  the mouse event data.
     * @return      true if handles mouse move successfully, false, otherwise.
     */
    public boolean handleMouseMove(ICanvasMouseData data) {
        if (
            !((Component) getCanvas()).isEnabled()
            || !((Component) getCanvas()).isVisible()) {
            return false;
        }
        getCanvas().doDefaultMouseMove(data);
        return false;
    }

    /**
     * Return true if handles mouse button released successfully, false,
     * otherwise. This method delgates the mouse button released event back to
     * canvas by calling doDefaultMouseUp(ICanvasMouseData) and return false.
     *
     * @param data  the mouse event data.
     * @return      true if handles mouse button released successfully, false,
     *      otherwise.
     */
    public boolean handleMouseUp(ICanvasMouseData data) {
        if (!((Component) getCanvas()).isEnabled()
            || !((Component) getCanvas()).isVisible()) {
            return false;
        }
        getCanvas().doDefaultMouseUp(data);

        return false;
    }

    /**
     * Handles delete a canvas link.
     *
     * @param link  the link to be deleted.
     */
    protected void handleDeleteLink(ICanvasLink link) {
        if (link instanceof ICanvasMapperLink) {
            requestRemoveLink(((ICanvasMapperLink) link).getMapperLink());
        }
    }

    /**
     * Handles delete a canvas node.
     *
     * @param node  the canvas node to be deleted.
     */
    protected void handleDeleteNode(ICanvasNode node) {
        if (node instanceof ICanvasMethoidNode) {
            requestRemoveNode(
                ((ICanvasMethoidNode) node).getMethoidNode());
        }
    }

    /**
     * Return true if the drop object is handled successfully, false otherwise.
     * The method checks if the object is IMapperLink, or IMethoid, then it
     * handleNewLinkDrop(DropTargetDropEvent, IMapperLink) and
     * handleNewMethoidDrop (DropTargetDropEvent, IMethoid) accordingly.
     *
     * @param event         the DropTargetDropEvent
     * @param transferData  the transfable object in the DropTargetDropEvent
     * @return              true if the drop object is handled successfully,
     *      false otherwise.
     */
    protected boolean handleDropObject(
        DropTargetDropEvent event,
        Object transferData) {
        LOGGER.finest("CanvasController transferData.class="
                     + transferData.getClass().getName());

        if (transferData instanceof IMapperLink) {
            return handleNewLinkDrop(event, (IMapperLink) transferData);
        } else if (transferData instanceof IMethoid) {
            return handleNewMethoidDrop(event, (IMethoid) transferData);
        }

        return false;
    }

    /**
     * Return true if the new methoid is handled successfully, false otherwise.
     *
     * @param event    the DropTargetDropEvent
     * @param methoid  the methoid object
     * @return         true if the new methoid is handled successfully, false
     *      otherwise.
     */
    protected boolean handleNewMethoidDrop(DropTargetDropEvent event, IMethoid methoid) {
        BasicMethoidNode newMethoidNode = null;
        if (methoid.isAccumulative()) {
            newMethoidNode = new BasicAccumulatingMethoidNode(methoid);
        } else {
            newMethoidNode = new BasicMethoidNode(methoid);
        }
        Point nodePt = event.getLocation();
        ((JGoView) getCanvas()).convertViewToDoc(nodePt);
        newMethoidNode.setX(nodePt.x);
        newMethoidNode.setY(nodePt.y);
        this.requestNewNode(newMethoidNode);

        return true;
    }

    /**
     * Return true if the new mapper link is handled successfully, false
     * otherwise.
     *
     * @param event  the DropTargetDropEvent
     * @param link   the mapper link object.
     * @return       true if the new mapper link is handled successfully, false
     *      otherwise.
     */
    protected boolean handleNewLinkDrop(DropTargetDropEvent event, IMapperLink link) {
        JGoView view = (JGoView) getCanvas();
        Point viewLocation = event.getLocation();
        Point modelLocation = new Point(viewLocation);
        view.convertViewToDoc(modelLocation);

        BasicCanvasView canvasView = (BasicCanvasView) getCanvas();
        if (canvasView.getPortObjectInModel(modelLocation, false) == null) {
            handleAddLink(link, modelLocation, viewLocation, event.getDropAction());
            return false;
        }
        return canvasView.connectLinkByPoint(modelLocation, link);
    }
    
    
    public void handleDragEnter(DropTargetDragEvent dtde) {
        IBasicDragController dragController = getMapperDragController();
        Object transferObject = dragController.getTransferObject();
        if (transferObject instanceof IMapperLink) {
            // DRAG FROM TREE: When dragging a link around from one of the trees,
            // we need to make it so the tree draws the horizontal lines
            // when the user drags out of the tree and into the canvas.
            // Basically this handles the case of drawing the line on the
            // tree containing the source node (where the link was dragged FROM)
            // only when the dragging occurs INSIDE the canvas.
            // We do this by grabbing the tree node from the link and
            // setting a link on that node and then adding it to the view model.
            // This node needs to be removed when done dragging.
            IMapperLink link = (IMapperLink) transferObject;
            IMapperNode node = null;
            if (link.getStartNode() != null) {
                node = link.getStartNode();
            }
            if (link.getEndNode() != null) {
                node = link.getEndNode();
            }
            if (node != null && node instanceof IMapperTreeNode) {
                IMapperLink newLink = new MapperLink();
                // Prefer to work with our own instance of a node
                // so we don't end up modifying nodes that are supposed
                // to stay in the tree on a non-temporary basis.
                IMapperNode newNode = (IMapperNode) node.clone();
                newLink.setStartNode(link.getStartNode() != null ? newNode : null);
                newLink.setEndNode  (link.getEndNode()   != null ? newNode : null);
                if (!MapperUtilities.isLinkAlreadyConnected(link, node)) {
                    newNode.addLink(newLink);
                    getViewModel().addNode(newNode);
                    dragController.setOriginatingDragNode(newNode);
                }
            }
            
            // Change the cursor to the hand cursor which is the drag cursor for the canvas.
            DragSourceContext dragSourceContext = dragController.getLinkDragSourceContext();
            if (dragSourceContext != null) {
                mOriginalDragCursor = dragSourceContext.getCursor();
                dragSourceContext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }
    
    public void handleDragExit(DropTargetEvent dte) {
        IBasicDragController dragController = getMapperDragController();
        if (dragController.getTransferObject() instanceof IMapperLink) {
            // DRAG FROM TREE: When dragging a link around from one of the trees,
            // we must remove the node we created when we first dragged into
            // the canvas, as we're only drawing the tree lines when we're
            // in the canvas. The tree controller takes care of all other
            // situations, including drawing the line on the opposite tree
            // when dragging in a tree.
            IMapperNode node = dragController.getOriginatingDragNode();
            if (node != null) {
                getViewModel().removeNode(node);
            }
            // The link that shows up in the canvas should be reset so that
            // it no longer appears (we set the start and end points to be the same).
            dragController.resetDragLinkEndLocation();
            // Restore the drag cursor.
            dragController.getLinkDragSourceContext().setCursor(mOriginalDragCursor);
            mOriginalDragCursor = null;
        }
    }
    
    public boolean handleDragOver(DropTargetDragEvent dtde) {
        IBasicDragController dragController = getMapperDragController();
        if (dragController.getTransferObject() instanceof IMapperLink) {
            // DRAG FROM TREE: When dragging a link around from one of the trees,
            // we need to draw a link from the edge of the tree to
            // our current drag location.
            dragController.setDragLinkEndLocation(dtde.getLocation());
        }
        return true;
    }
    
    public void handleDragGestureRecognized(DragGestureEvent dge) {
        // DRAG FROM CANVAS: When dragging a link around from the canvas,
        // set ourselves as the transfer object.
        getMapperDragController().setTransferObject(this);
    }
}

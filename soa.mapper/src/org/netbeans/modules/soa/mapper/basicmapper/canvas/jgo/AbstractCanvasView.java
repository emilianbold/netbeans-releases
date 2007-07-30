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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.Icon;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoLayer;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;
import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IBasicDragController;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDHandler;
import org.netbeans.modules.soa.mapper.common.gtk.CanvasModelUpdateEvent;
import org.netbeans.modules.soa.mapper.common.gtk.DefaultCanvasPalette;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasController;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasModel;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;

/**
 * <p>
 *
 * Title: </p> AbstractCanvasView<p>
 *
 * Description: </p> AbstractCanvasView provides an implementation of
 * ICanvasView back by an JGoView. Since JGoView also is the controller for
 * JGoObjects. This class provides a defautl DnDHanlder that warps all the
 * JGoView DnD methods for the IBasicViewController. <p>
 *
 * @author    Un Seng Leong
 * @created   December 31, 2002
 */
public abstract class AbstractCanvasView
     extends JGoView
     implements ICanvasView, IDnDHandler {

    /**
     * The location of the drag starts. This point can locate the drag object.
     */
    private Point mDnDDragOrginPoint;

    /**
     * the node border color
     */
    private Color mNodeBorderColor = Color.lightGray;

    /**
     * the mapper view contains this canvas
     */
    private IMapperCanvasView mMapperView;

    /**
     * Zoom factor controls the zoom of this view. Does not user in this
     * application.
     */
    private double mZoomFactor;

    /**
     * the DragGestureListener for default IDnDHandler
     */
    private DragGestureListener mDragGestureListener;

    /**
     * the DragSourceListener for default IDnDHandler
     */
    private DragSourceListener mDragSourceListener;

    /**
     * the DropTargetListener for default IDnDHandler
     */
    private DropTargetListener mDropTargetListener;

    /**
     * the dnd handler to use (current dnd handler)
     */
    private IDnDHandler mHandler;

    /**
     * The controller of this canvas. This view will forward most of the
     * controller handle to this controller.
     */
    private ICanvasController mController;

    //  cuurent object under mouse
    private static JGoObject currentObj = null;
    
    /**
     * Whether an entire chain of connected links/node elements will
     * appear highlighted upon selection of any element in the chain.
     */
    private boolean mIsPathHighlightingEnabled;
    
    
    /**
     * Constructor an AbstractCanvasView with no visiual appearing.
     *
     * @param mapperView  Description of the Parameter
     */
    public AbstractCanvasView(IMapperCanvasView mapperView) {
        mMapperView = mapperView;
        mDragGestureListener = new DragGestureListenerWapper();
        mDragSourceListener = new DragSourceListenerWapper();
        mDropTargetListener = new DropTargetListenerWapper();
        setDnDHandler(getDefaultDnDHandler());
        mIsPathHighlightingEnabled = false;
        configureFocusTravers();
    }

    /**
     * Return the canvas mapper view that contains this canvas.
     *
     * @return   the canvas mapper view that contains this canvas.
     */
    public IMapperCanvasView getParentView() {
        return mMapperView;
    }

    /**
     * Gets the defaultDnDHandler attribute of the AbstractCanvasView object
     *
     * @return   The defaultDnDHandler value
     */
    public IDnDHandler getDefaultDnDHandler() {
        return this;
    }

    /**
     * Sets the dnDHanlder attribute of the AbstractCanvasView object
     *
     * @param handler  The new dnDHanlder value
     */
    public void setDnDHandler(IDnDHandler handler) {
        mHandler = handler;
    }

    /**
     * Gets the dnDHanlder attribute of the AbstractCanvasView object
     *
     * @return   The dnDHanlder value
     */
    public IDnDHandler getDnDHanlder() {
        return mHandler;
    }

    /**
     * Close this handler and release all the system resource.
     */
    public void releaseHandler() {
        // unless is not using the JGo default DnD Handler. Otherwise,
        // JGo should release its own DnD objects.
        if (mHandler != this) {
            mHandler.releaseHandler();
        }
    }

    /**
     * Return the canvas controller of this canvas.
     *
     * @return   the canvas controller of this canvas.
     */
    public ICanvasController getCanvasController() {
        return mController;
    }

    /**
     * Set the canvas controller to this view receive events.
     *
     * @param controller  the canvas controller.
     */
    public void setCanvasController(ICanvasController controller) {
        mController = controller;
    }

    /**
     * Return the default canvas palette of this canvas. This method is not
     * applicable to this canvas. It always returns null.
     *
     * @return   always null.
     */
    public DefaultCanvasPalette getCanvasPalette() {
        return null;
    }

    /**
     * Return the model of the canvas. This method is not applicable to this
     * canvas. It always return null.
     *
     * @return   always null
     */
    public ICanvasModel getModel() {
        return null;
    }

    /**
     * Get the canvas node border color.
     *
     * @return   the canvas node border color.
     */
    public Color getNodeBorderColor() {
        return mNodeBorderColor;
    }

    /**
     * Gets the dnDDragOrginPoint attribute of the AbstractCanvasView object
     *
     * @return   The dnDDragOrginPoint value
     */
    public Point getDnDDragOrginPoint() {
        return mDnDDragOrginPoint;
    }

    /**
     * Return true if link label is visible, false otherwise. This method is not
     * applicable to this canvas. It always return false.
     *
     * @return   false
     */
    public boolean isLinkLabelVisible() {
        return false;
    }

    /**
     * Retrieves whether this canvas pass mouse event to the controller. This
     * method is not applicable to this Cavnas. It always returns true.
     *
     * @return   true
     */
    public boolean isPassMouseEventToController() {
        return true;
    }

    /**
     * Set the defautl canvas palette of this canvas. This method is not
     * applicable to this canvas. It returns immediately.
     *
     * @param palette  the palette of this canvas.
     */
    public void setCanvasPalette(DefaultCanvasPalette palette) { }

    /**
     * Sets the link's label visiblity to the given value. This method is not
     * applicable to this canvas. It returns immediately.
     *
     * @param val  the value
     */
    public void setLinkLabelVisible(boolean val) { }

    /**
     * This method is not applicable to this view. It returns immediately.
     *
     * @param model  the model
     */
    public void setModel(ICanvasModel model) { }

    /**
     * Set the canvas node border color.
     *
     * @param color  the canvas node border color.
     */
    public void setNodeBorderColor(Color color) {
        mNodeBorderColor = color;
    }

    /**
     * Sets whether to pass mouse event to controller. This method is not
     * applicable to this Cavnas. It returns immediately.
     *
     * @param val  the value
     */
    public void setPassMouseEventToController(boolean val) { }

    /**
     * This method is not applicable to this view. It always return null.
     *
     * @param location   the loc of the node.
     * @param size       the size of the node.
     * @param icon       the icon of the node.
     * @param label      the label of the node.
     * @param labelIcon  the icon of the label
     * @return           null
     */
    public ICanvasGroupNode createCanvasGroupNode(
        Point location,
        Dimension size,
        Icon icon,
        String label,
        Icon labelIcon) {
        return null;
    }

    /**
     * This method is not applicable to this view. It always return null.
     *
     * @param src   source node
     * @param dest  dest node
     * @return      always null
     */
    public ICanvasLink createCanvasLink(
        ICanvasNode src,
        ICanvasNode dest) {
        return null;
    }

    /**
     * This method is not applicable to this view. It always return null.
     *
     * @param src              source node
     * @param dest             dest node
     * @param isComponentNode  is component node
     * @param isWithBinding    is with binding
     * @return                 always null
     */
    public ICanvasLink createCanvasLink(
        ICanvasNode src,
        ICanvasNode dest,
        boolean isComponentNode,
        boolean isWithBinding) {
        return null;
    }

    /**
     * Create a Canvas node. This method is not applicable to this view. It
     * always return null.
     *
     * @param location  the loc of the node.
     * @param size      the size of the node
     * @param label     the label of the node.
     * @return          always null
     */
    public ICanvasNode createCanvasNode(
        Point location,
        Dimension size,
        String label) {
        return null;
    }

    /**
     * This method is not applicable to this view. It always return null.
     *
     * @param location  the loc of the node.
     * @param size      the size of the node.
     * @param icon      the icon of the node.
     * @param label     the label of the node.
     * @return          always null
     */
    public ICanvasNode createCanvasNode(
        Point location,
        Dimension size,
        Icon icon,
        String label) {
        return null;
    }

    /**
     * Listens on the model event, execute when group node is loaded from the
     * model. This method is not applicable to this canvas. It returns
     * immediately.
     *
     * @param event  CanvasModelUpdateEvent.
     */
    public void groupNodeLoaded(CanvasModelUpdateEvent event) { }

    /**
     * Listens on the model event, execute when link is loaded from the model.
     * This method is not applicable to this canvas. It returns immediately.
     *
     * @param event  the CanvasModelUpdateEvent event
     */
    public void linkLoaded(CanvasModelUpdateEvent event) { }

    /**
     * Listens on the model event, execute when node is loaded from the model.
     * This method is not applicable to this canvas. It returns immediately.
     *
     * @param event  CanvasModelUpdateEvent
     */
    public void nodeLoaded(CanvasModelUpdateEvent event) { }

    /**
     * Finds and updates parent node, this method is not applicable to this implemenation.
     * This method returns immediately.
     *
     * @param node   - the node whose parent node will be updated
     * @param nodes  -- the list of potentional parents node to be set
     */
    public void updatesParentNode(Object node, Object nodes[]) { }

    /**
     * Toggle to another link mode. This method is not applicable to this
     * canvas. It returns immediately.
     *
     * @param val  the value
     */
    public void toggleLinkMode(boolean val) { }

    /**
     * Return the zoom factor of ths view.
     *
     * @return   the zoom factor of ths view.
     */
    public double getZoomFactor() {
        return mZoomFactor;
    }

    /**
     * Set the zoon factor of this view.
     *
     * @param factor  the zoom factor of this view
     */
    public void setZoomFactor(double factor) {
        mZoomFactor = factor;
    }

    /**
     * Gets the dragGestureListener attribute of the IDnDHandler object
     *
     * @return   The dragGestureListener value
     */
    public DragGestureListener getDragGestureListener() {
        return mDragGestureListener;
    }

    /**
     * Gets the dragSourceListener attribute of the IDnDHandler object
     *
     * @return   The dragSourceListener value
     */
    public DragSourceListener getDragSourceListener() {
        return mDragSourceListener;
    }

    /**
     * Gets the dropTargetListener attribute of the IDnDHandler object
     *
     * @return   The dropTargetListener value
     */
    public DropTargetListener getDropTargetListener() {
        return mDropTargetListener;
    }

    /**
     * Return an int representing the type of action used in this Drag
     * operation.
     *
     * @return   an int representing the type of action used in this Drag
     *      operation. See <code>java.awt.dnd.DnDConstants</code> for a list of
     *      available drag actions.
     */
    public int getDragAction() {
        return java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE;
    }

    /**
     * Return the cursor to use when start draging on the component of this
     * handler.
     *
     * @return   the cursor to use when start draging. See <code>java.awt.dnd.DragSource</code>
     *      for a list of avaiable drag cursor.
     */
    public Cursor getDragCursor() {
        return java.awt.dnd.DragSource.DefaultCopyDrop;
    }

    /**
     * Overrides java.awt.dnd.DragGestureListener.dragGestureRecognized, this
     * method delegates to IDnDHandler.getDragGestureListener.dragGestureRecognized.
     *
     * @param e  the DragGestureEvent
     */
    public void dragGestureRecognized(DragGestureEvent e) {
        if (mHandler != null && mHandler.getDragGestureListener() != null) {
            mHandler.getDragGestureListener().dragGestureRecognized(e);
            getCanvasController().handleDragGestureRecognized(e);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dragEnter, this method
     * delegates to IDnDHandler.getDropTargetListener.dragEnter.
     *
     * @param dtde  the DropTargetDragEvent
     */
    public void dragEnter(DropTargetDragEvent dtde) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dragEnter(dtde);
            getCanvasController().handleDragEnter(dtde);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dragExit, this method delegates
     * to IDnDHandler.getDropTargetListener.dragExit.
     *
     * @param dte  the DropTargetEvent
     */
    public void dragExit(DropTargetEvent dte) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dragExit(dte);
            getCanvasController().handleDragExit(dte);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dragOver, this method delegates
     * to IDnDHandler.getDropTargetListener.dragOver.
     *
     * @param dtde  the DropTargetDragEvent
     */
    public void dragOver(DropTargetDragEvent dtde) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dragOver(dtde);
            getCanvasController().handleDragOver(dtde);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.drop, this method delegates to
     * IDnDHandler.getDropTargetListener.drop.
     *
     * @param event  the DropTargetDropEvent
     */
    public void drop(DropTargetDropEvent event) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().drop(event);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dropActionChanged, this method
     * delegates to IDnDHandler.getDropTargetListener.dropActionChanged.
     *
     * @param dtde  the DropTargetDragEvent
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dropActionChanged(dtde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragDropEnd, this method
     * delegates to IDnDHandler.getDragSourceListener.dragDropEnd.
     *
     * @param dsde  Description of the Parameter
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragDropEnd(dsde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragEnter, this method
     * delegates to IDnDHandler.getDragSourceListener.dragEnter.
     *
     * @param dsde  Description of the Parameter
     */
    public void dragEnter(DragSourceDragEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragEnter(dsde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragExit, this method delegates
     * to IDnDHandler.getDragSourceListener.dragExit.
     *
     * @param dse  Description of the Parameter
     */
    public void dragExit(DragSourceEvent dse) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragExit(dse);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragOver, this method delegates
     * to IDnDHandler.getDragSourceListener.dragOver.
     *
     * @param dsde  Description of the Parameter
     */
    public void dragOver(DragSourceDragEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragOver(dsde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dropActionChanged, this method
     * delegates to IDnDHandler.getDragSourceListener.dropActionChanged.
     *
     * @param dsde  Description of the Parameter
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dropActionChanged(dsde);
        }
    }

    /**
     * This method delegates to super.dragEnter.
     *
     * @param dtde  the DropTargetDragEvent
     */
    private void superDragEnter(DropTargetDragEvent dtde) {
        super.dragEnter(dtde);
    }

    /**
     * This method delegates to super.dragExit.
     *
     * @param dte  the DropTargetEvent
     */
    private void superDragExit(DropTargetEvent dte) {
        super.dragExit(dte);
    }

    /**
     * This method delegates to super.dragGestureRecognized.
     *
     * @param e  the DragGestureEvent
     */
    private void superDragGestureRecognized(DragGestureEvent e) {
        super.dragGestureRecognized(e);
    }

    /**
     * This method delegates to super.dragOver.
     *
     * @param dtde  the DropTargetDragEvent
     */
    private void superDragOver(DropTargetDragEvent dtde) {
        super.dragOver(dtde);
    }

    /**
     * This method delegates to super.drop.
     *
     * @param event  the DropTargetDropEvent
     */
    private void superDrop(DropTargetDropEvent event) {
        super.drop(event);
    }

    /**
     * This method delegates to super.dropActionChanged.
     *
     * @param dtde  the DropTargetDragEvent
     */
    private void superDropActionChanged(DropTargetDragEvent dtde) {
        super.dropActionChanged(dtde);
    }

    /**
     * This method delegates to super.dragDropEnd.
     *
     * @param dsde  Description of the Parameter
     */
    private void superDragDropEnd(DragSourceDropEvent dsde) {
        super.dragDropEnd(dsde);
    }

    /**
     * This method delegates to super.dragEnter.
     *
     * @param dsde  Description of the Parameter
     */
    private void superDragEnter(DragSourceDragEvent dsde) {
        super.dragEnter(dsde);
    }

    /**
     * This method delegates to super.dragExit.
     *
     * @param dse  Description of the Parameter
     */
    private void superDragExit(DragSourceEvent dse) {
        super.dragExit(dse);
    }

    /**
     * This method delegates to super.dragOver.
     *
     * @param dsde  Description of the Parameter
     */
    private void superDragOver(DragSourceDragEvent dsde) {
        super.dragOver(dsde);
    }

    /**
     * This method delegates to super.dropActionChanged.
     *
     * @param dsde  Description of the Parameter
     */
    private void superDropActionChanged(DragSourceDragEvent dsde) {
        super.dropActionChanged(dsde);
    }

    /**
     * DragGestureListener of default IDnDHanlder. Delagates all coorsponding
     * methods to default JGoView methods.
     *
     * @author    sleong
     * @created   December 31, 2002
     */
    private class DragGestureListenerWapper implements DragGestureListener {
        /**
         * Overrides java.awt.dnd.DragGestureListener.dragGestureRecognized,
         * this method delegates to superDragGestureRecognized. It also set the
         * drag orgin point for the drag event to provide a way to search for
         * dragged object in JGoView.
         *
         * @param e  the DragGestureEvent
         */
        public void dragGestureRecognized(DragGestureEvent e) {
            if (!AbstractCanvasView.this.isEnabled() || !AbstractCanvasView.this.isVisible()) {
                return;
            }

            mDnDDragOrginPoint = new Point(e.getDragOrigin());
            superDragGestureRecognized(e);
        }
    }

    /**
     * DragSourceListener of default IDnDHanlder. Delagates all coorsponding
     * methods to default JGoView methods.
     *
     * @author    sleong
     * @created   December 31, 2002
     */
    private class DragSourceListenerWapper implements DragSourceListener {
        /**
         * Overrides java.awt.dnd.DragSourceListener.dragDropEnd, this method
         * delegates to getCanvasController().handleDragDropEnd(DragSourceDropEvent).
         * If controller is null, superDragDropEnd is called.
         *
         * @param dsde  Description of the Parameter
         */
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (getCanvasController() != null) {
                getCanvasController().handleDragDropEnd(dsde);
            } else {
                superDragDropEnd(dsde);
            }
        }

        /**
         * Overrides java.awt.dnd.DragSourceListener.dragEnter, this method
         * delegates to superDragEnter.
         *
         * @param dsde  Description of the Parameter
         */
        public void dragEnter(DragSourceDragEvent dsde) {
            superDragEnter(dsde);
        }

        /**
         * Overrides java.awt.dnd.DragSourceListener.dragExit, this method
         * delegates to superDragExit.
         *
         * @param dse  Description of the Parameter
         */
        public void dragExit(DragSourceEvent dse) {
            superDragExit(dse);
        }

        /**
         * Overrides java.awt.dnd.DragSourceListener.dragOver, this method
         * delegates to superDragOver.
         *
         * @param dsde  Description of the Parameter
         */
        public void dragOver(DragSourceDragEvent dsde) {
            superDragOver(dsde);
        }

        /**
         * Overrides java.awt.dnd.DragSourceListener.dropActionChanged, this
         * method delegates to superDropActionChanged.
         *
         * @param dsde  Description of the Parameter
         */
        public void dropActionChanged(DragSourceDragEvent dsde) {
            superDropActionChanged(dsde);
        }
    }

    /**
     * DropTargetListener of default IDnDHanlder. Delagates all coorsponding
     * methods to default JGoView methods.
     *
     * @author    sleong
     * @created   December 31, 2002
     */
    private class DropTargetListenerWapper implements DropTargetListener {

        /**
         * Overrides java.awt.dnd.DropTargetListener.dragEnter, this method
         * delegates to superDragEnter.
         *
         * @param dtde  the DropTargetDragEvent
         */
        public void dragEnter(DropTargetDragEvent dtde) {
            superDragEnter(dtde);
        }

        /**
         * Overrides java.awt.dnd.DropTargetListener.dragExit, this method
         * delegates to superDragExit.
         *
         * @param dte  the DropTargetEvent
         */
        public void dragExit(DropTargetEvent dte) {
            superDragExit(dte);
        }

        /**
         * Overrides java.awt.dnd.DropTargetListener.dragOver, this method is
         * empty because the super method always rejects drag.
         *
         * @param dtde  the DropTargetDragEvent
         */
        public void dragOver(DropTargetDragEvent dtde) {}

        /**
         * Handles drop action - actual work is delegated to the controller
         *
         * @param event  Description of the Parameter
         */

        /**
         * Overrides java.awt.dnd.DropTargetListener.drop, this method delegates
         * to getCanvasController.handleDrop(DropTargetDropEvent).
         *
         * @param event  the DropTargetDropEvent
         */
        public void drop(DropTargetDropEvent event) {
            if (getCanvasController() != null) {
                if (getCanvasController().handleDrop(event)) {
                    event.dropComplete(true);

                    // Canvas will have focus so that the mouse over Cursor icons
                    // will function
                    AbstractCanvasView.this.grabFocus();
                } else {
                    event.dropComplete(false);
                }
            }
        }

        /**
         * Overrides java.awt.dnd.DropTargetListener.dropActionChanged, this
         * method delegates to superDropActionChanged.
         *
         * @param dtde  the DropTargetDragEvent
         */
        public void dropActionChanged(DropTargetDragEvent dtde) {
            superDropActionChanged(dtde);
        }
    }

    /**
     * @see com.nwoods.jgo.JGoView#isDropFlavorAcceptable
     */
    public boolean isDropFlavorAcceptable(DropTargetDragEvent e) {
        return true;
    }
    
    public void addRawLink(ICanvasLink link) {
        addObjectAtTail((JGoObject) link);
    }
    
    public void removeRawLink(ICanvasLink link) {
        removeObject((JGoObject) link);
    }
    
    protected JGoPort pickPort(Point dc) {
        return pickNearestPort(dc);
    }

    // Delegate to our custom port picking method.
    public JGoPort pickNearestPort(Point dc) {
        JGoObject obj = pickAnyNearestPort(dc);
        if (obj instanceof JGoPort) {
            return (JGoPort) obj;
        }
        return null;
    }
    
    private JGoPort pickAnyNearestPort(Point point)
    {
        JGoPort bestPort = null;
        double bestDistance = getPortGravity();
        bestDistance *= bestDistance;
        for (JGoLayer jgolayer = getFirstLayer(); jgolayer != null; jgolayer = getNextLayer(jgolayer))
        {
            if (jgolayer.isVisible())
            {
                JGoListPosition jgolistposition = jgolayer.getFirstObjectPos();
                Point linkPoint = new Point(0, 0);
                while (jgolistposition != null) 
                {
                    JGoObject jgoobject = jgolayer.getObjectAtPos(jgolistposition);
                    jgolistposition = jgolayer.getNextObjectPos(jgolistposition);
                    if (jgoobject instanceof JGoPort)
                    {
                        JGoPort iterPort = (JGoPort) jgoobject;
                        if (iterPort.isPointInObj(point)) {
                            if (iterPort.getParent() instanceof ICanvasGroupNode) {
                                ICanvasGroupNode groupNode = (ICanvasGroupNode) iterPort.getParent();
                                if (groupNode.isExpanded()) {
                                    // If our ports are shown (expanded), then 
                                    // if the point is directly on a port
                                    // it's obviously the best one.
                                    return iterPort;
                                }
                            }
                        } else {
                            int position = JGoObject.Center;
                            if (iterPort instanceof BasicCanvasPort) {
                                // BasicCanvasPorts span the entire width of
                                // the field node, so the gravity should
                                // react to the left or right sides of
                                // the ports (i.e. not the center).
                                position = ((BasicCanvasPort) iterPort).getLinkPosition();
                            }
                            linkPoint = iterPort.getLinkPoint(position, linkPoint);
                        }
                        double dx = point.x - linkPoint.x;
                        double dy = point.y - linkPoint.y;
                        double distance = dx * dx + dy * dy;
                        if (distance <= bestDistance)
                        {
                            // the port with the smallest distance wins
                            bestPort = iterPort;
                            bestDistance = distance;
                        }
                    }
                }
            }
        }

        return bestPort;
    }
    
    public JGoLink createTemporaryLinkForNewLink(JGoPort from,
                                                 JGoPort to) {
        BasicCanvasSimpleLink link = 
            new BasicCanvasSimpleLink(from, to);
        link.setPen(JGoPen.makeStockPen(ICanvasMapperLink.DEFAULT_LINK_COLOR));
        link.setBrush(JGoBrush.makeStockBrush(ICanvasMapperLink.DEFAULT_LINK_COLOR));
        
        link.setArrowHeads(from.isValidDestination() && !from.isValidSource(),
                           to.isValidDestination() && !to.isValidSource());
        AbstractCanvasLink.initializeArrowHeads(link);
        IBasicDragController dragController = 
            ((BasicCanvasController)mController).getMapperDragController();
        dragController.setDragLink(link);
        return link;
    }
    
    /**
     * Whether an entire chain of connected links/node elements will
     * appear highlighted upon selection of any element in the chain.
     */
    public boolean isPathHighlightingEnabled() {
        return mIsPathHighlightingEnabled;
    }
    
    /**
     * Sets whether chains of linked nodes are highlighted.

     */
    public void setPathHighlightingEnabled(boolean isEnabled) {
        mIsPathHighlightingEnabled = isEnabled;
    }
    
    public boolean doMouseMove(int modifiers, Point dc, Point vc) {
        boolean result = super.doMouseMove(modifiers, dc, vc);
        
         //handle mouse event and delegate to child object under mouse point
        if (  getState() != MouseStateCreateLinkFrom
           && getState() != MouseStateCreateLink 
           && getState() != MouseStateDragBoxSelection)
            doMouseHandling(modifiers, dc, vc);
        
        return result;
    }
    
    private boolean doMouseHandling(int modifiers, Point dc, Point vc) {
        boolean returnStatus = false;

        // if we're over a port, start drawing a new link
        JGoObject obj = pickPort(dc);

        if (obj == null)
            obj = pickDocObject(dc, false);

        while (obj != null) {
            if (obj instanceof BasicCanvasPort
                    && ((BasicCanvasPort) obj).doMouseEntered(modifiers, dc, vc, this)) {
                returnStatus = true;
                break;
            } else {
                obj = obj.getParent();
            }
        }

        //fire mouseExisted event
        if (currentObj instanceof BasicCanvasPort && currentObj != obj) {
            ((BasicCanvasPort) currentObj).doMouseExited(modifiers, dc, vc, this);
        }
        //this is the curent obj where mouse has entered
        currentObj = obj;

        return returnStatus;
    }
    
    
    private void configureFocusTravers() {
        KeyStroke tabForward = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        KeyStroke tabBackward = KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                KeyEvent.SHIFT_DOWN_MASK);
        
        Set<AWTKeyStroke> oldForwardTK = getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> oldBackwardTK = getFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        
        Set<AWTKeyStroke> newForwardTK = new HashSet<AWTKeyStroke>(
                oldForwardTK);
        Set<AWTKeyStroke> newBackwardTK = new HashSet<AWTKeyStroke>(
                oldBackwardTK);
        
        newForwardTK.remove(tabForward);
        newBackwardTK.remove(tabBackward);
        
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                newForwardTK);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                newBackwardTK);
        
        getInputMap().put(tabForward, "tabNextAction"); // NOI18N
        getInputMap().put(tabBackward, "tabPrevAction"); // NOI18N
        
        getActionMap().put("tabNextAction", new TabNextAction());
        getActionMap().put("tabPrevAction", new TabPrevAction());
    }
    
    
    public AbstractCanvasMethoidNode getNextNode() {
        AbstractCanvasMethoidNode firstSelected = null;
        int selectedCount = 0;
        
        Collection<?> selection = getSelectedNodes();
        
        if (selection != null) {
            for (Object o : selection) {
                if (o instanceof AbstractCanvasMethoidNode) {
                    if (firstSelected == null) {
                        firstSelected = (AbstractCanvasMethoidNode) o;
                    }
                    selectedCount++;
                }
            }
        }
        
        if (selectedCount > 1) {
            return firstSelected;
        }
        
        List<?> nodes = getNodes();
        int size = (nodes == null) ? 0 : nodes.size();
        
        if (size != 0) {
            if (firstSelected == null) {
                for (int i = 0; i < size; i++) {
                    Object o = nodes.get(i);
                    if (o instanceof AbstractCanvasMethoidNode) {
                        return (AbstractCanvasMethoidNode) o;
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    if (nodes.get(i) == firstSelected) {
                        for (int j = i + 1; j < size; j++) {
                            Object o = nodes.get(j);
                            if (o instanceof AbstractCanvasMethoidNode) {
                                return (AbstractCanvasMethoidNode) o;
                            }
                        }
                        
                        return null;
                    }
                }
            }
        }
        
        return null;
    }
    
    
    public AbstractCanvasMethoidNode getPrevNode() {
        AbstractCanvasMethoidNode lastSelected = null;
        int selectedCount = 0;
        
        Collection<?> selection = getSelectedNodes();
        
        if (selection != null) {
            for (Object o : selection) {
                if (o instanceof AbstractCanvasMethoidNode) {
                    lastSelected = (AbstractCanvasMethoidNode) o;
                    selectedCount++;
                }
            }
        }
        
        if (selectedCount > 1) {
            return lastSelected;
        }
        
        List<?> nodes = getNodes();
        int size = (nodes == null) ? 0 : nodes.size();
        
        if (size != 0) {
            if (lastSelected == null) {
                for (int i = size - 1; i >= 0; i--) {
                    Object o = nodes.get(i);
                    if (o instanceof AbstractCanvasMethoidNode) {
                        return (AbstractCanvasMethoidNode) o;
                    }
                }
            } else {
                for (int i = size - 1; i >= 0; i--) {
                    if (nodes.get(i) == lastSelected) {
                        for (int j = i - 1; j >= 0; j--) {
                            Object o = nodes.get(j);
                            if (o instanceof AbstractCanvasMethoidNode) {
                                return (AbstractCanvasMethoidNode) o;
                            }
                        }
                        
                        return null;
                    }
                }
            }
        }
        
        return null;
    }
    
    
    private void scrollNodeToBeVisible(AbstractCanvasMethoidNode node) {
        Rectangle bounds = node.getBoundingRect();
        if (bounds != null) {
            scrollRectToVisible(bounds);
        }
    }
    
    
    private class TabNextAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            AbstractCanvasMethoidNode nextNode = getNextNode();
            if (nextNode != null) {
                selectObject(nextNode);
                scrollNodeToBeVisible(nextNode);
            } else {
                transferFocus();
            }
        }
    }


    private class TabPrevAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            AbstractCanvasMethoidNode prevNode = getPrevNode();
            if (prevNode != null) {
                selectObject(prevNode);
                scrollNodeToBeVisible(prevNode);
            } else {
                transferFocusBackward();
            }
        }
    }
}

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

package org.netbeans.modules.soa.mapper.basicmapper.tree;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.netbeans.modules.soa.mapper.basicmapper.BasicViewController;
import org.netbeans.modules.soa.mapper.basicmapper.MapperLink;
import org.netbeans.modules.soa.mapper.basicmapper.dnd.ComponentDnDHandler;
import org.netbeans.modules.soa.mapper.basicmapper.util.LocalObjectTransferable;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperView;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IBasicDragController;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDHandler;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IBasicTreeViewSelectionPathController;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperView;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasController;

/**
 * <p>
 *
 * Title: </p> AbstractTreeViewController <p>
 *
 * Description: </p> Generic tree view controller handles the basic drag and
 * drop object behavior for a tree. The actural drag and drop object operations
 * should be provided by subclasses. <p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 */
public abstract class AbstractTreeViewController
    extends BasicViewController
    implements IDnDHandler, IBasicTreeViewSelectionPathController {

    private static final Logger LOGGER = Logger.getLogger(AbstractTreeViewController.class.getName());

    /**
     * the dnd handler warpper
     */
    private ComponentDnDHandler mDndHandler;

    /**
     * the transferable object for drag operations
     */
    private LocalObjectTransferable mTransferable;

    /**
     * the DragGestureListener of this IDnDHandler
     */
    private final DragGestureListener mDragGestureListener =
        new GestureListener();

    /**
     * the DragSourceListener of this IDnDHandler
     */
    private final DragSourceListener mDragSourceListener = new DragListener();

    /**
     * the DropTargetListener of this IDnDHandler
     */
    private final DropTargetListener mDropTargetListener = new DropListener();

    private IMapperTreeNode mOriginatingNode;
    
    private TreePath mDragLinkSourcePath;
    
    private Cursor mOriginalDragCursor;
    
    private MapperTreeNodeLinkDragPair mDragPair;
    

    /**
     * Creates a new TreeViewController object.
     */
    public AbstractTreeViewController() {
        mTransferable = new LocalObjectTransferable();
        mDndHandler = new ComponentDnDHandler();
        mDndHandler.setHandler(this);
        setDnDHandler(this);
    }

    /**
     * Set the view that this controller is handling.
     *
     * @param view  the view that this controller is handling.
     */
    public void setView(IMapperView view) {
        if (!(view instanceof IMapperTreeView)) {
            throw new java.lang.IllegalArgumentException(
                "TreeViewController.setView expecting IMapperTreeView. Got: "
                    + view.getClass().getName());
        }
        super.setView(view);
        mDndHandler.setComponent(((IMapperTreeView) view).getTree());
    }

    /**
     * Gets the dragGestureListener attribute of the IDnDHandler object.
     * Overrides IDnDHandler.getDragGestureListener.
     *
     * @return   The dragGestureListener value
     */
    public DragGestureListener getDragGestureListener() {
        return mDragGestureListener;
    }

    /**
     * Gets the dragSourceListener attribute of the IDnDHandler object.
     * Overrides IDnDHandler.getDragGestureListener.
     *
     * @return   The dragSourceListener value
     */
    public DragSourceListener getDragSourceListener() {
        return mDragSourceListener;
    }

    /**
     * Gets the dropTargetListener attribute of the IDnDHandler object.
     * Overrides IDnDHandler.getDragGestureListener.
     *
     * @return   The dropTargetListener value
     */
    public DropTargetListener getDropTargetListener() {
        return mDropTargetListener;
    }

    /**
     * Return DnDConstants.ACTION_COPY_OR_MOVE. Overrides
     * IDnDHandler.getDragAction.
     *
     * @return   DnDConstants.ACTION_COPY_OR_MOVE.
     */
    public int getDragAction() {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    /**
     * Return DragSource.DefaultCopyDrop cursor, Overrides
     * IDnDHandler.getDragCursor.
     *
     * @return   DragSource.DefaultCopyDrop cursor
     */
    public Cursor getDragCursor() {
        return DragSource.DefaultCopyDrop;
    }

    /**
     * Close this handler and release any system resource.
     */
    public void releaseHandler() {
        mDndHandler.setComponent(null);
    }

    public Point getSelectionPathPoint() {
        IMapperTreeView treeView = (IMapperTreeView) getView();
        JTree tree = treeView.getTree();
        TreePath path = tree.getSelectionPath();
        if (mDragLinkSourcePath == null) {
            mDragLinkSourcePath = tree.getSelectionPath();
        }
        Rectangle pathRect = treeView.getShowingPathRectBound(mDragLinkSourcePath);
        if (pathRect != null) {
            Point nodePoint = treeView.getTreeNodePoint(pathRect);
            if (nodePoint != null) {
                return new Point(getDragPortOriginX(), nodePoint.y + 2 - treeView.getViewOffset().y);
            }
        }
        return null;
    }
    
    private MapperTreeNodeLinkDragPair getDragPair() {
        if (mDragPair == null) {
            mDragPair = new MapperTreeNodeLinkDragPair();
        }
        return mDragPair;
    }
    
    private void cleanupDragAndDrop() {
        // The drag is over, clear out all temporary node settings,
        // whether we were dragging from the tree or canvas.
        if (mOriginatingNode != null) {
            mOriginatingNode.setHighlightLink(false);
            getViewModel().removeNode(mOriginatingNode);
            mOriginatingNode = null;
        }
        if (getDragPair() != null) {
            if (getDragPair().getTreeNode() != null) {
                getDragPair().getTreeNode().setHighlightLink(false);
            }
            getViewModel().removeNode(getDragPair().getTreeNode());
            mDragPair = null;
        }
        IBasicDragController dragController = getMapperDragController();
        dragController.setOriginatingDragNode(null);
        dragController.setTransferObject(null);
        dragController.clearDragLink();
        mDragLinkSourcePath = null;
        dragController.setLinkDragSourceContext(null);
    }
    
    
    /**
     * Return true if the method handles JGoObject Drop to this tree
     * successfully, false otherwise. since the JGo data in transferable is
     * always JGoSelection which never tells if it is a JGoPort connection or
     * Moveing Objects around. Therefore, the CanvasView is responesible to tell
     * what the dnd operation is.
     *
     * @param event  the DropTargetDropEvent event that supports JGo DataFlavor
     *      in its transferable.
     * @return       true if the method handles JGoObject Drop to this tree
     *      successfully, false otherwise.
     */
    protected abstract boolean handleJGoObjectDrop(DropTargetDropEvent event);

    /**
     * Return true if the method handles the Java Local Object dropped to this
     * tree successfully, false otherwise.
     *
     * @param event     the DropTargetDropEvent event that supports Java Local
     *      Object DataFlavor in its transferable.
     * @param localObj  the object of from the transferable of the Java Local
     *      Object DataFlavor.
     * @return          true if the method handles the Java Local Object dropped
     *      to this tree successfully, false otherwise.
     */
    protected abstract boolean handleJLocalObjectDrop(
        DropTargetDropEvent event,
        Object localObj);

    /**
     * Return the object to be transfered in a drag operations
     *
     * @param event  the DragGestureEvent
     * @return       the object to be transfered in a drag operations
     */
    protected abstract Object getDragObject(DragGestureEvent event);

    /**
     * Sets the specified node as the end or start node on the given link,
     * according to the direction that points to this tree.
     */
    protected abstract void setLinkOnNode(IMapperLink link, IMapperTreeNode node);

    /**
     * Sets the specified node as the end or start node on the given link,
     * according to the direction that points to the opposite tree.
     */
    protected abstract void setOppositeLinkOnNode(IMapperLink link, IMapperTreeNode node);

    /**
     * Returns the tree node linked to the opposite tree.
     */
    protected abstract IMapperTreeNode getOriginatingTreeNodeFromLink(IMapperLink link);

    /**
     * Determines if any of the links on the node link to the node with the
     * direction of the link pointing to this tree.
     */
    protected abstract boolean isNodeAlreadyLinked(IMapperNode node);

    /**
     * Returns the x location of the edge of the tree.
     */
    protected abstract int getDragPortOriginX();


    /**
     * Implementation of DropTargetListener to handle tree drop events.
     *
     * @author    sleong
     * @created   December 26, 2002
     */
    protected class DropListener
        extends DropTargetAdapter /*implements java.awt.dnd.Autoscroll */ {

        private static final int EXPAND_WAIT_INTERVAL   = 1000;
        private static final int COLLAPSE_WAIT_INTERVAL = 2000;
        private Thread thread;
        private TreePath hoverPath;

        // The following two mouse listeners are just for the
        // safe side, fall back kind of mechanism in the event of an
        // error or something happens and the thread is never interrupted
        // as expected. This would ensure the thread stopping.
        private final MouseListener MSE_LISTENER = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                interrupt();
                hoverPath = null;
            }
            public void mousePressed(MouseEvent e) {
                interrupt();
                hoverPath = null;
            }
            public void mouseReleased(MouseEvent e) {
                interrupt();
                hoverPath = null;
            }
            public void mouseEntered(MouseEvent e) {
                interrupt();
                hoverPath = null;
            }
            public void mouseExited(MouseEvent e) {
                interrupt();
                hoverPath = null;
            }
        };
        // listening on mouse events just to ensure that the thread is stopped
        // if not appropriately stopped in the event of an unexpected error
        private final MouseMotionListener MSE_MOTION_LISTENER = new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent e) {
                interrupt();
                hoverPath = null;
            }
            public void mouseMoved(MouseEvent e) {
                interrupt();
                hoverPath = null;
            }
        };
        
        /**
          * Select the tree node when dragging over.
          *
          * @param event  the DropTargetDragEvent object
          */
        public void dragOver(DropTargetDragEvent dtde) {
            Point dragPoint = dtde.getLocation();
            JTree tree = ((IMapperTreeView) getView()).getTree();
            int row = tree.getRowForLocation(dragPoint.x, dragPoint.y);
            boolean isInterrupt = false;
            if (row >= 0) {
                tree.setSelectionRow(row);
                TreePath tPath = tree.getPathForLocation(dragPoint.x, dragPoint.y);
                if (tPath != null && !tPath.equals(hoverPath)) {
                    hoverPath = tPath;
                    isInterrupt = true;
                } else {
                    hoverPath = tPath;
                }
            } else {
                hoverPath = null;
            }

            // We must draw the horizontal line on the tree we're currently dragging
            // over. This horizontal line should only be drawn if we're hovering over
            // a node, and if the node is not already linked. We must also update
            // the canvas drag link end location as we move the mouse.
            IBasicDragController dragController = getMapperDragController();
            Object transferObject = dragController.getTransferObject();
            if (transferObject instanceof IMapperLink) {
                if (getOriginatingTreeNodeFromLink((IMapperLink) transferObject) != null) {
                    handleDragOverTreeLink(getDragPair());
                    handleDragOverCanvasLink(dtde, dragController);
                }
            } else if (transferObject instanceof ICanvasController) {
                handleDragOverTreeLink(getDragPair());
                handleDragOverCanvasLink(dtde, dragController);
            }

            if (isInterrupt) {
                interrupt();
                addMouseListeners(tree);
                thread = new ExpandThread(hoverPath);
                thread.start();
            }
        }

        /**
         * This method draws the temporary horizontal line on a tree.
         * It does this by added and removing tree nodes and managing their
         * links, which in turn is used by the tree view to determine whether
         * to draw a line.
         */
        private void handleDragOverTreeLink(MapperTreeNodeLinkDragPair dragPair) {
            if (hoverPath == null && dragPair.getTreeNode() != null) {
                // We aren't dragging over anything, remove the line.
                dragPair.removeDragNode();
            }

            if (hoverPath != null) {
                // getMapperTreeNode(...) never returns null
                IMapperTreeNode node = ((IMapperTreeView) getView()).getMapperTreeNode(hoverPath);
                boolean isSameNode = node.equals(dragPair.getTreeNode());
                if (!isSameNode && !isNodeAlreadyLinked(node)) {
                    // If the node hasn't already been linked (i.e. from a drag
                    // operation that completed a while ago, or from reverse engineering)
                    // then set it as our current drag node and draw the line.
                    IMapperTreeNode newNode = (IMapperTreeNode) node.clone();
                    dragPair.removeDragNode();
                    dragPair.setDragNode(newNode);
                } else if (!isSameNode) {
                    dragPair.removeDragNode();
                }
            }
        }
        
        /**
         * This method repositions the end link of the temporary canvas link
         * while the user mouses around on the tree. Basically, we want to
         * make sure the canvas drag link connects to our tree edge at the location
         * of our current mouse y location (or node mid-point if we're on a node).
         */
        private void handleDragOverCanvasLink(DropTargetDragEvent dtde, IBasicDragController dragController) {
            Point dragLinkEndLocation = null;
            IMapperTreeView treeView = (IMapperTreeView) getView();
            if (hoverPath != null) {
                IMapperTreeNode node = treeView.getMapperTreeNode(hoverPath);
                TreePath path = node.getPath();
                Rectangle pathRect = treeView.getShowingPathRectBound(path);
                Point nodePoint = treeView.getTreeNodePoint(pathRect);
                if (nodePoint != null) {
                    dragLinkEndLocation = new Point(getDragPortOriginX(), nodePoint.y + 2 - treeView.getViewOffset().y);
                }
            
            } else {
                dragLinkEndLocation = new Point(getDragPortOriginX(), dtde.getLocation().y - treeView.getViewOffset().y);
            }
            
            if (dragLinkEndLocation != null) {
                dragController.setDragLinkEndLocation(dragLinkEndLocation);
            }
        }

        public void dragExit(DropTargetEvent dte) {
            if (getDragPair() != null) {
                getDragPair().removeDragNode();
                mDragPair = null;
            }
            
            Object transferObject = getMapperDragController().getTransferObject();
            if (transferObject instanceof IMapperLink) {
                // DRAG FROM TREE: When dragging a link around from one of the trees,
                // remove the horizontal line on the opposite tree (at the node
                // where the drag originated from).
                if (mOriginatingNode != null) {
                    // The node that resides on the tree where the drag started FROM.
                    // This would result from a simple tree to tree node drag.
                    getViewModel().removeNode(mOriginatingNode);
                    mOriginatingNode = null;
                }
                getMapperDragController().resetDragLinkEndLocation();
            } else if (transferObject instanceof ICanvasController) {
                // DRAG FROM CANVAS: When dragging a link around from the canvas,
                // restore the drag cursor.
                getMapperDragController().getLinkDragSourceContext().setCursor(mOriginalDragCursor);
                mOriginalDragCursor = null;
            }
            
            interrupt();
            hoverPath = null;
        }

        public void dragEnter(DropTargetDragEvent dte) {
            Object transferObject = getMapperDragController().getTransferObject();
            if (transferObject instanceof IMapperLink) {
                // DRAG FROM TREE: When dragging a link around from one of the trees,
                // we must draw the horizontal line on the opposite tree (at the node
                // where the drag originated from).
                IMapperLink link = (IMapperLink) transferObject;
                IMapperTreeNode originatingNode = getOriginatingTreeNodeFromLink(link);
                if (originatingNode != null) {
                    IMapperNode node = null;
                    if (link.getStartNode() != null) {
                        node = link.getStartNode();
                    } else if (link.getEndNode() != null) {
                        node = link.getEndNode();
                    }
                    if (!MapperUtilities.isLinkAlreadyConnected(link, node)) {
                        IMapperLink newLink = new MapperLink();
                        mOriginatingNode = (IMapperTreeNode) originatingNode.clone();
                        setOppositeLinkOnNode(newLink, mOriginatingNode);
                        mOriginatingNode.addLink(newLink);
                        getViewModel().addNode(mOriginatingNode);
                    }
                }
            }
            
            else if (transferObject instanceof ICanvasController) {
                // DRAG FROM CANVAS: When dragging a link around from the canvas,
                // we need to set the drag cursor to be that of the standard
                // tree drag and drop cursor.
                DragSourceContext dragSourceContext = getMapperDragController().getLinkDragSourceContext();
                if (dragSourceContext != null) {
                    mOriginalDragCursor = dragSourceContext.getCursor();
                    dragSourceContext.setCursor(DragSource.DefaultCopyDrop);
                }
            }

            interrupt();
            hoverPath = null;
        }

        /**
          * Check if the transferable supports JGo DataFlavor, then calls
          * handleJGoObjectDrop and pass the result back to dropComplete. Then
          * check if the transferable supports JavaLocalObjectDataFlavor, calls
          * handleJLocalObjectDrop and pass the result back to dropComplete.
          *
          * @param event  the DropTargetDropEvent
          */
        public void drop(DropTargetDropEvent event) {
            
            cleanupDragAndDrop();
            
            if (getView() instanceof IBasicMapperView
                && !((IBasicMapperView) getView()).isMapable()) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                event.rejectDrop();
                return;
            }

            Point dropPoint = event.getLocation();
            JTree tree = ((IMapperTreeView) getView()).getTree();
            TreePath treePath = tree.getPathForLocation(dropPoint.x, dropPoint.y);
            if (treePath != null) {
                if (getDnDCustomizer() != null) {
                    // Determine whether the path is mappable.
                    if (!getDnDCustomizer().isMappable(treePath)) {
                        event.rejectDrop();
                        return;
                    }
                }
            }

            if (event.getTransferable().isDataFlavorSupported(
            MapperUtilities.getJGoSelectionDataFlavor())) {
                event.dropComplete(handleJGoObjectDrop(event));
            }
            else if (event.getTransferable().isDataFlavorSupported(
            MapperUtilities.getJVMLocalObjectDataFlavor())) {
                try {
                    event.dropComplete(handleJLocalObjectDrop(
                            event,
                            event.getTransferable().getTransferData(
                                MapperUtilities.getJVMLocalObjectDataFlavor())));
                } catch (java.io.IOException io) {
                    io.printStackTrace(System.err);
                    event.rejectDrop();
                } catch (java.awt.datatransfer.UnsupportedFlavorException u) {
                    u.printStackTrace(System.err);
                    event.rejectDrop();
                }
            } else {
                event.rejectDrop();
            }
        }

        private void interrupt() {
            try {
                if (thread != null) {
                    thread.interrupt();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Tree drag interrupt exception", e);
            }
        }

        class ExpandThread extends Thread {
            private TreePath mHoverTreePath;
            ExpandThread(TreePath tPath) {
                mHoverTreePath = tPath;
            }
            public void run() {
                try {
                    while (true) {
                        final JTree tree = ((IMapperTreeView) getView()).getTree();
                        final boolean isExpanded = tree.isExpanded(mHoverTreePath);
                        if (isExpanded) {
                            // collapse wait
                            sleep(COLLAPSE_WAIT_INTERVAL);
                        } else {
                            // expand wait
                            sleep(EXPAND_WAIT_INTERVAL);
                        }
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                if (isExpanded) {
                                    tree.collapsePath(mHoverTreePath);
                                } else {
                                    tree.expandPath(mHoverTreePath);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    // do nothing.
                } finally {
                    JTree tree = ((IMapperTreeView) getView()).getTree();
                    removeMouseListeners(tree);
                }
            }
        }

        private void addMouseListeners(JTree tree) {
            tree.addMouseListener(MSE_LISTENER);
            tree.addMouseMotionListener(MSE_MOTION_LISTENER);
        }

        private void removeMouseListeners(JTree tree) {
            tree.removeMouseListener(MSE_LISTENER);
            tree.removeMouseMotionListener(MSE_MOTION_LISTENER);
        }
    }

    /**
     * Implementation of DragSourceListener to handle tree drag events.
     *
     * @author    sleong
     * @created   December 26, 2002
     */
    protected class DragListener extends DragSourceAdapter {
        
        public void dragDropEnd(DragSourceDropEvent dsde) {
            cleanupDragAndDrop();
        }
        
        public void dragEnter(DragSourceDragEvent dsde) {
            // Save a copy of the DragSourceContext object so that our drag
            // events can set and access the drag cursor.
            getMapperDragController().setLinkDragSourceContext(dsde.getDragSourceContext());
        }
    }
    
    /**
     * Implementation of DragGestureListener to handle tree start draging
     * events.
     *
     * @author    sleong
     * @created   December 26, 2002
     */
    protected class GestureListener implements DragGestureListener {
        /**
         * This method check if the component is enable and visible and start
         * the draging event using the DragGestureEvent. Overrides
         * java.awt.dnd.DragGestureListener.dragGestureRecognized.
         *
         * @param e  the DragGestureEvent
         */
        public void dragGestureRecognized(DragGestureEvent e) {
            IMapperTreeView treeView = (IMapperTreeView) getView();
            JTree tree = treeView.getTree();
            if (
                    e.getComponent() != tree ||
                    !tree.isEnabled()        ||
                    !tree.isVisible()        ||
                    !treeView.isMapable()) {
                return;
            }
            
            // DRAG FROM TREE: When dragging a link around from one of the trees,
            // set the transfer object as the IMapperLink that is created for 
            // this operation. One one end of the link is the IMapperTreeNode
            // representing the source tree node that was dragged from.
            Object transferObject = getDragObject(e);
            if (transferObject != null) {
                Point selectionPathPoint = getSelectionPathPoint();
                if (selectionPathPoint != null) {
                    getMapperDragController().setTransferObject(transferObject);
                    // Create a link at the edge of the tree in the canvas
                    // that will be used show where we are linking to when we
                    // drag around in the canvas. Subsequent canvas drag events
                    // update the end location of this link.
                    getMapperDragController().setDragLink(
                        selectionPathPoint, 
                        AbstractTreeViewController.this);
                    mTransferable.setTransferData(transferObject);
                    e.startDrag(getDnDHandler().getDragCursor(), mTransferable);
                }
            }
        }
    }
    
    /**
     * Handles record-keeping for the current tree drag node and
     * link. These together allow a horizontal line to be drawn
     * on the tree while dragging.
     */
    private class MapperTreeNodeLinkDragPair {
        private IMapperTreeNode mTreeNode;
        private IMapperLink mLink;
        public IMapperTreeNode getTreeNode() {
            return mTreeNode;
        }
        public void removeDragNode() {
            if (mLink != null) {
                setLinkOnNode(mLink, null);
                mTreeNode.removeLink(mLink);
                mTreeNode.setHighlightLink(false);
            }
            getViewModel().removeNode(mTreeNode);
            mTreeNode = null;
            mLink = null;
        }
        public void setDragNode(IMapperTreeNode newNode) {
            mTreeNode = newNode;
            if (mLink == null) {
                mLink = new MapperLink();
            }
            mTreeNode.setHighlightLink(true);
            setLinkOnNode(mLink, mTreeNode);
            mTreeNode.addLink(mLink);
            getViewModel().addNode(mTreeNode);
        }
    }
}

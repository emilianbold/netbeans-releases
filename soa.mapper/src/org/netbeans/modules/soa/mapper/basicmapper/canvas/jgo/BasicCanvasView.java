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

import com.nwoods.jgo.JGoBrush;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoLayer;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoSelection;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DropTargetDragEvent;
import org.netbeans.modules.soa.mapper.basicmapper.MapperLink;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.DrawPort;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.PortSelection;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasNodeToNodeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasNodeToTreeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasObjectFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasTreeToNodeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasTreeToTreeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperViewController;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseData;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseListener;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;




/**
 * <p>
 *
 * Title: </p> BasicCanvasView <p>
 *
 * Description: </p> BasicCanvasView provides an implementation of a mapper
 * canvas view.<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class BasicCanvasView extends AbstractCanvasView {
    
    final static int MOUSE_CLICK = 1;
    final static int MOUSE_DBLCLICK = 2;
    final static int MOUSE_PRESS = 3;
    final static int MOUSE_RELEASE = 4;
    final static int MOUSE_MOVE = 5;
    
    /**
     * the log instance of this class
     */
    private Logger mLogger = Logger.getLogger(BasicCanvasView.class.getName());

    /**
     * Listener listens on the change of the mapper view model.
     */
    private PropertyChangeListener mViewModelListener =
        new ViewModelChangeListener();

    /**
     * Contains the link with at least one side connected to a tree.
     */
    private List mFilterTreeNodeLink = new Vector();

    /**
     * Contains the link with node to node links
     */
    private List mFilterNodeLinks = new Vector();

    /**
     * Caching the jgo layer and the mapper view model.
     */
    private Map mLayerModelMap;

    /**
     * Listener listens on the add and remove link in each of the node in the
     * view model and group node.
     */
    private PropertyChangeListener mNodeLinkListener;

    /**
     * Listener listens on the add and remove node in a group node.
     */
    private PropertyChangeListener mGroupNodeListener;

    /**
     * Canvas object factory to produce canvas object.
     */
    private ICanvasObjectFactory mObjectFactory;

    /**
     * the unique layer to be display.
     */
    private JGoLayer mSelectedLayer;

    /**
     * the default cursor
     */
    private Cursor mDefaultCursor = Cursor.getDefaultCursor();

    /**
     * the linking cursor
     */
    //private Cursor mLinkCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    private Cursor mLinkCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    /**
     * the moving node cursor
     */
    private Cursor mMoveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

    /**
     * the link in this list is waiting for another end of the node to be added
     * to the canvas, link cannot be display without either end not in the canvas.
     */
    private List mNewLinkList;

    private HoverPort mCurrentTargetHoverPort;
    private HoverPort mCurrentSourceHoverPort;
    private Point mDropLocation;
    private int mDropAction;
    private Set mMouseListeners = new HashSet();

    
    /**
     * Construct a new Canvas View. This canvas contains no vertical bar, but
     * horizontal bar.
     *
     * @param view  the parent mapper view contains this canvas
     */
    public BasicCanvasView(IMapperCanvasView view) {
        super(view);
        this.setHidingDisabledScrollbars(true);

        setDocument(new JGoDocument());

        addComponentListener(new CanvasSizeListener());
        mNodeLinkListener = new NodeLinkListener(this);
        mGroupNodeListener = new GroupNodeListener();
        mLayerModelMap = new Hashtable();
        setDefaultPrimarySelectionColor(ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR);

        mObjectFactory = new BasicCanvasObjectFactory();
        mObjectFactory.setMapperCanvas(this);
        mNewLinkList = new Vector();
        setDefaultPortGravity(20);
    }

    
    /**
     * Return a canvas link that contains the mapper link, or null if no such
     * mapper link.
     *
     * @param dataObject  the mapper link
     * @return            a canvas link that contains the mapper link, or null
     *      if no such mapper link.
     */
    public ICanvasLink getCanvasLinkByDataObject(Object dataObject) {
        if (!(dataObject instanceof IMapperLink)) {
            return null;
        }

        IMapperLink matchLink = (IMapperLink) dataObject;

        JGoDocument model = this.getDocument();
        JGoListPosition pos = model.getFirstObjectPos();
        JGoObject obj = null;

        for (; pos != null; pos = model.getNextObjectPos(pos)) {
            obj = model.getObjectAtPos(pos);

            if (obj instanceof ICanvasMapperLink
                && (((ICanvasMapperLink) obj).getMapperLink() == matchLink)) {
                return (ICanvasLink) obj;
            }
        }

        return null;
    }

    /**
     * Return the canvas node repersetns the data object. Currently, the data
     * object should be IMethoidFieldNode or IMethoidNode, or return null if
     * data object is either of the them, or no each object in this canvas.
     *
     * @param dataObject  the data object to match
     * @return            the canvas node repersents the data object.
     */
    public ICanvasNode getCanvasNodeByDataObject(Object dataObject) {
        if (dataObject instanceof IFieldNode) {
            return findCanvasFieldNode((IFieldNode) dataObject);
        }

        if (dataObject instanceof IMethoidNode) {
            return findCanvasMethoidNode((IMethoidNode) dataObject);
        }

        return null;
    }

    /**
     * Return the canvas object factory for this canvas.
     *
     * @return   the canvas object factory.
     */
    public ICanvasObjectFactory getCanvasObjectFactory() {
        return mObjectFactory;
    }

    /**
     * Return all the nodes in the currect layer.
     *
     * @return   all the nodes in the currect layer.
     */
    public List getNodes() {
        List nodes = new ArrayList();

        if (mSelectedLayer == null) {
            return nodes;
        }

        for (JGoListPosition pos = mSelectedLayer.getFirstObjectPos();
            pos != null;
            pos = mSelectedLayer.getNextObjectPos(pos)) {
            nodes.add(mSelectedLayer.getObjectAtPos(pos));
        }

        return nodes;
    }

    /**
     * Retrieves model object at the given location
     *
     * @param modelCor  Description of the Parameter
     * @param flag      Description of the Parameter
     * @return          The objectInModel value
     */
    public Object getObjectInModel(Point modelCor, boolean flag) {
        return mSelectedLayer.pickObject(modelCor, flag);
    }

    public Object getPortObjectInModel(Point modelCor, boolean flag) {
        JGoPort object = pickNearestPort(modelCor);
        if (object != null) {
            return object;
        }
        return mSelectedLayer.pickObject(modelCor, flag);
    }

    /**
     * Retrieves a collection of selected links
     *
     * @return   The selectedLinks value
     */
    public Collection getSelectedLinks() {
        List list = new ArrayList();
        JGoSelection selection = this.getSelection();

        if (selection == null) {
            return list;
        }

        JGoObject obj = null;
        JGoListPosition pos = selection.getFirstObjectPos();

        while (pos != null) {
            obj = selection.getObjectAtPos(pos);

            if (obj instanceof ICanvasLink) {
                list.add(obj);
            }

            pos = selection.getNextObjectPos(pos);
        }

        return list;
    }

    /**
     * Retrieves a list of selected nodes
     *
     * @return   List
     */
    public Collection getSelectedNodes() {
        List list = new ArrayList();
        JGoSelection selection = this.getSelection();

        if (selection == null) {
            return list;
        }

        JGoObject obj = null;
        JGoListPosition pos = selection.getFirstObjectPos();

        while (pos != null) {
            obj = selection.getObjectAtPos(pos);

            if (obj instanceof ICanvasNode) {
                list.add(obj);
            }

            pos = selection.getNextObjectPos(pos);
        }

        return list;
    }

    /**
     * Return the Look and Feel of this canvas. The method return "this".
     *
     * @return   the compoenent repersents this canvas look and feel.
     */
    public Component getUIComponent() {
        return this;
    }

    /**
     * Return true if the canvas node is existed in the canvas model.
     *
     * @param node  the specified node to be searched.
     * @return      The nodeExisit value
     */
    public boolean isNodeExisit(ICanvasNode node) {
        JGoListPosition result = getDocument()
            .findObject((JGoObject) node);

        if (result == null) {
            return false;
        }

        if (!allContainersExpanded(node)) {
            return false;
        }

        return ((JGoObject) node).isVisible();
    }

    /**
     * Set the canvas object factory for this canvas.
     *
     * @param factory  the canvas object factory
     */
    public void setCanvasObjectFactory(ICanvasObjectFactory factory) {
        mObjectFactory = factory;
    }

    /**
     * Set the mapper view model for this canvas.
     *
     * @param viewModel  the mapper view model for this canvas.
     */
    public void setViewModel(IMapperViewModel viewModel) {
        IMapperViewModel oldModel = getParentView().getViewModel();
        this.clearSelection();

        if (oldModel != null) {
            oldModel.removePropertyChangeListener(mViewModelListener);
        }

        if (viewModel != null) {
            viewModel.addPropertyChangeListener(mViewModelListener);

            // create a JGo Layer for each view so that we don't have to
            // create all the JGo objects for each view model
            if (mLayerModelMap.containsKey(viewModel)) {
                mSelectedLayer.setVisible(false);
                mSelectedLayer = (JGoLayer) mLayerModelMap.get(viewModel);
                mSelectedLayer.setVisible(true);
                getDocument().setDefaultLayer(mSelectedLayer);
            } else {
                if (mSelectedLayer == null) {
                    mSelectedLayer = this.getDocument().getDefaultLayer();
                } else {
                    mSelectedLayer.setVisible(false);
                    mSelectedLayer = getDocument().addLayerBefore(mSelectedLayer);
                }

                getDocument().setDefaultLayer(mSelectedLayer);
                mSelectedLayer.setVisible(true);
                mLayerModelMap.put(viewModel, mSelectedLayer);
                initializeCanvasNode(viewModel);
            }
        }
    }

    /**
     * Add a link to this canvas to display
     *
     * @param link  the specified link to be added.
     */
    public void addLink(ICanvasLink link) {
        if (mSelectedLayer == null) {
            mSelectedLayer = this.getDocument().getDefaultLayer();
        }

        mSelectedLayer.addObjectAtHead((JGoObject) link);

        if (link instanceof ICanvasNodeToTreeLink
             || link instanceof ICanvasTreeToNodeLink
             || link instanceof ICanvasTreeToTreeLink) {
            mFilterTreeNodeLink.add(link);
        }

        if (link instanceof ICanvasNodeToNodeLink) {
            mFilterNodeLinks.add(link);
        }
    }

    /**
     * Add a node a this canvas to display.
     *
     * @param node  the specified node to be added.
     */
    public void addNode(ICanvasNode node) {
        if (mSelectedLayer == null) {
            mSelectedLayer = this.getDocument().getDefaultLayer();
        }

        // Need to clear selection for new object, otherwise, the selection
        // box may appear on top of the new object
        clearSelection();
        mSelectedLayer.addObjectAtTail((JGoObject) node);

    }

    /**
     * Retrun true if the container expended, false otherwise.
     *
     * @param node  the container node.
     * @return      Retrun true if the container expended, false otherwise.
     */
    public boolean allContainersExpanded(ICanvasNode node) {
        ICanvasGroupNode container = node.getContainer();

        if (container == null) {
            return true;
        } else {
            return container.isExpanded() && allContainersExpanded(container);
        }
    }

    /**
     * Clear the selection. No object will be selected.
     */
    public void clearSelection() {
        this.getSelection().clearSelection();
    }

    /**
     * Collapse all Methoid Node in the current layer.
     */
    public void collapseAllNode() {
        for (
            JGoListPosition pos = mSelectedLayer.getFirstObjectPos();
            pos != null;
            pos = mSelectedLayer.getNextObjectPos(pos)) {
            JGoObject jgoObj = mSelectedLayer.getObjectAtPos(pos);

            if (jgoObj instanceof BasicCanvasMethoidNode) {
                ((BasicCanvasMethoidNode) jgoObj).collapse();
            }
        }
    }

    /**
     * Create link from dragging on this canvas to other view. This method
     * provide the way for user to drag a JGoPort (connection object of
     * ICanvasFieldNode) to other view to create a link.
     *
     * @param link  the link to be created, should has only 1 end (start or end
     *      node) null.
     * @return      Return true if the link is create successfull, false
     *      otherwise.
     */
    public boolean connectLinkByDrag(IMapperLink link) {
        Point convertedPoint = getDnDDragOrginPoint();
        convertViewToDoc(convertedPoint);

        return connectLinkByPoint(convertedPoint, link);
    }
    
    /**
     * Return true if the specified mapper link can be connected by a mapper
     * node that can be found by the specified point, false otherwise.
     *
     * @param docPoint  the point where the mapper node to be found, in the
     *      document point system.
     * @param link      the link that should has 1 end does not connected to any
     *      nodes.
     * @return          true if a canvas link contains the specified mapper link
     *      can be created, false otherwise.
     */
    public boolean connectLinkByPoint(Point docPoint, IMapperLink link) {
        JGoLayer layer = mSelectedLayer;
        JGoObject dropObject = (JGoObject) getPortObjectInModel(docPoint, false);
        
        ICanvasFieldNode dropFieldNode = null;
        if (dropObject instanceof JGoPort) {
            dropFieldNode = 
                findCanvasFieldNodeByConnectionObject(layer, dropObject);
            if (dropFieldNode == null) {
                return false;
            }
        } else {
            return false;
        }
        IFieldNode dragEndNode = dropFieldNode.getFieldNode();
        IMapperNode dragStartNode = link.getStartNode() == null ? 
            link.getEndNode() : link.getStartNode();
        boolean requiresInput = false;
        if (dragStartNode instanceof IMapperTreeNode) {
            requiresInput = 
                ((IMapperTreeNode)dragStartNode).isSourceTreeNode();
        } else if (dragStartNode instanceof IFieldNode) {
            requiresInput = ((IFieldNode)dragStartNode).isOutput();
        } else {
            throw new Error("This shouldn't have happened...");
        }
        if (requiresInput) {
            if (!dragEndNode.isInput()) {
                return false;
            }
        } else {
            if (!dragEndNode.isOutput()) {
                return false;
            }
        }
        if (link.getStartNode() == null) {
            ((MapperLink) link).setStartNode(dragEndNode);
        } else {
            ((MapperLink) link).setEndNode(dragEndNode);
        }
        ((IMapperViewController) this.getCanvasController()).requestNewLink(link);
        return true;
    }

    /**
     * Return the canvas field node contains the specified connection object in
     * a given layer, or null if no filed node contains the connection object.
     *
     * @param layer       the layer to be search on
     * @param connObject  the object to be match
     * @return            the canvas field node contains the specified
     *      connection object in a given layer, or null if no filed node
     *      contains the connection object.
     */
    private ICanvasFieldNode findCanvasFieldNodeByConnectionObject(JGoLayer layer, Object connObject) {
        ICanvasFieldNode canvasFieldNode = null;
        Collection canvasFieldNodes = null;
        ICanvasMethoidNode dragMethoidNode = null;
        ICanvasFieldNode dragFieldNode = null;

        for (JGoListPosition pos = layer.getFirstObjectPos(); pos != null; pos = layer.getNextObjectPos(pos)) {
            JGoObject jgoObj = layer.getObjectAtPos(pos);

            if (!(jgoObj instanceof ICanvasMethoidNode)) {
                continue;
            }

            dragMethoidNode = (ICanvasMethoidNode) jgoObj;

            if ((dragFieldNode =
                dragMethoidNode.getFieldNodeByConnectPointObject(connObject)) != null) {
                break;
            }
        }
        return dragFieldNode;
    }

    /**
     * Handles delete the selected object. Actural work is delegated to
     * controller.
     */
    public void deleteSelection() {
        if (getCanvasController() != null) {
            getCanvasController().handleDeleteSelection();
        } else {
            doDefaultDeleteSelection();
        }
    }

    /**
     * Wrapper method for the default delection selection, always return true.
     *
     * @return   always true
     */
    public boolean doDefaultDeleteSelection() {
        super.deleteSelection();
        return true;
    }

    /**
     * Sets the Cursor icon on mouse over (when no other mouse events occur)
     *
     * @param modifiers  modifiers
     * @param dc         dc
     * @param vc         vc
     */
    public void doUncapturedMouseMove(int modifiers, Point dc, Point vc) {
        Object obj = pickDocObject(dc, true);
        Cursor cursor = mDefaultCursor;
        
        markCurrentHoverPort(dc, false);
        
        if (obj instanceof BasicCanvasMethoidNode) {
            BasicCanvasMethoidNode methoidNode = (BasicCanvasMethoidNode) obj;
            if (!methoidNode.isExpanded()) {
                if (!methoidNode.isInButton(vc)) {
                    cursor = mMoveCursor;
                }
            } else {
                for (JGoListPosition pos = methoidNode.getFirstObjectPos();
                     pos != null;
                     pos = methoidNode.getNextObjectPos(pos)) {
                    
                    JGoObject jgoObj = methoidNode.getObjectAtPos(pos);
                    if (jgoObj.getBoundingRect().contains(dc)) {
                        if (jgoObj instanceof BasicTitleBarUI) {
                            if (!((BasicTitleBarUI) jgoObj).isInButton(vc)) {
                                cursor = mMoveCursor;
                            }
                            break;
                        } else if (jgoObj instanceof JGoPort) {
                            cursor = mLinkCursor;
                            break;
                        }
                    }
                }
            }
        } 
        setCursor(cursor);
    }
    
    // Override to set up the source hover port.
    // The source hover port is only created if the source of the drag
    // was a port of a methoid.
    public JGoLink createTemporaryLinkForNewLink(JGoPort from, JGoPort to) {
        JGoLink link = super.createTemporaryLinkForNewLink(from, to);
        
        link.setPen(JGoPen.make(
                JGoPen.SOLID,
                1,
                ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR));
        link.setBrush(JGoBrush.makeStockBrush(ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR));
        

        if (from instanceof BasicCanvasPort) {
            mCurrentSourceHoverPort = new HoverPort(((BasicCanvasPort) from).getDrawPort());
            mCurrentSourceHoverPort.set(true);
        } else if (to instanceof BasicCanvasPort) {
            mCurrentSourceHoverPort = new HoverPort(((BasicCanvasPort) to).getDrawPort());
            mCurrentSourceHoverPort.set(true);
        }
        return link;
    }
    
    // When dragging is done, this method is called.
    // We simply clean up our source hover port.
    public void removeRawLink(ICanvasLink link) {
        super.removeRawLink(link);
        if (mCurrentSourceHoverPort != null) {
            mCurrentSourceHoverPort.unset();
            mCurrentSourceHoverPort = null;
        }
    }
    
    public void dragOver(DropTargetDragEvent dtde) {
        super.dragOver(dtde);
        markCurrentHoverPort(dtde.getLocation(), true);
    }
    
    public void dragOver(DragSourceDragEvent dsde) {
        super.dragOver(dsde);
        Point point = dsde.getLocation();
        Point screenReferencePoint = this.getLocationOnScreen();
        point.translate(-screenReferencePoint.x, - screenReferencePoint.y);
        markCurrentHoverPort(point, true);
    }
    
    // Manages the source and target hover ports during mouse moving/dragging.
    private void markCurrentHoverPort(Point point, boolean isActivated) {
        if (mCurrentTargetHoverPort != null) {
            mCurrentTargetHoverPort.unset();
        }
        
        if (mCurrentSourceHoverPort != null) {
            mCurrentSourceHoverPort.set(true);
        }
        
        Object obj = pickDocObject(point, true);
        if (obj instanceof BasicCanvasMethoidNode)
        {
            BasicCanvasMethoidNode methoidNode = (BasicCanvasMethoidNode) obj;
            if (methoidNode.isExpanded()) {
                for (JGoListPosition pos = methoidNode.getFirstObjectPos();
                     pos != null;
                     pos = methoidNode.getNextObjectPos(pos))
                {
                    JGoObject jgoObj = methoidNode.getObjectAtPos(pos);
                    if (jgoObj.getBoundingRect().contains(point)) {
                        if (jgoObj instanceof BasicCanvasFieldNode) {
                            BasicCanvasFieldNode field = (BasicCanvasFieldNode) jgoObj;
                            if (field.getDrawPort() != null) {
                                mCurrentTargetHoverPort = new HoverPort(field);
                                mCurrentTargetHoverPort.set(isActivated);
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        JGoPort port = pickNearestPort(point);
        if (port instanceof BasicCanvasPort) {
            BasicCanvasPort basicCanvasPort = (BasicCanvasPort) port;
            if (basicCanvasPort.getDrawPort() != null) {
                mCurrentTargetHoverPort = new HoverPort(basicCanvasPort.getDrawPort());
                mCurrentTargetHoverPort.set(isActivated);
            }
        }
    }
    
    /**
     * Handles default mouse click behavior
     *
     * @param data  the mouse data of the double click event
     * @return      true if default mouse click is processed successfully, false
     *      otherwise.
     */
    public boolean doDefaultMouseClick(ICanvasMouseData data) {
        if (fireMouseEvent(MOUSE_CLICK, data)) {
            return true;
        }
        return doDefaultMouseClick(
            data.getMouseModifier(),
            data.getModelLocation(),
            data.getViewLocation());
    }

    /**
     * Wrapper method for the default mouse click
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse click is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseClick(
                                       int modifier,
                                       Point docLocation,
                                       Point viewLocation) {
        // just call jgo
        return super.doMouseClick(
                                  modifier,
                                  docLocation,
                                  viewLocation);
    }

    /**
     * Handles default mouse double click behavior
     *
     * @param data  the mouse data of event
     * @return      true if default mouse double click is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseDblClick(ICanvasMouseData data) {
        // called from controller
        if (fireMouseEvent(MOUSE_DBLCLICK, data)) {
            return true;
        }
        return doDefaultMouseDblClick(
                                      data.getMouseModifier(),
                                      data.getModelLocation(),
                                      data.getViewLocation());
    }

    /**
     * Wrapper method for the default mouse double click
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse double click is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseDblClick(
                                          int modifier,
                                          Point docLocation,
                                          Point viewLocation) {
        // call jgo
        return super.doMouseDblClick(modifier, docLocation, viewLocation);
    }

    /**
     * Handles default mouse down behavior
     *
     * @param data  the mouse data of event
     * @return      true if default mouse button pressed is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseDown(ICanvasMouseData data) {
        if (fireMouseEvent(MOUSE_PRESS, data)) {
            return true;
        }
        return doDefaultMouseDown(
                           data.getMouseModifier(),
                           data.getModelLocation(),
                           data.getViewLocation());
    }

    /**
     * The wrapper method for the default mouse down
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse button pressed is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseDown(
                                      int modifier,
                                      Point docLocation,
                                      Point viewLocation) {
        // just call jgo..
        return super.doMouseDown(modifier, docLocation, viewLocation);
    }
    
    /**
     * Handles default mouse move behavior
     *
     * @param data  the mouse data of event
     * @return      true if default mouse moved is processed successfully, false
     *      otherwise.
     */
    public boolean doDefaultMouseMove(ICanvasMouseData data) {
        if (fireMouseEvent(MOUSE_MOVE, data)) {
            return true;
        }
        return doDefaultMouseMove(data.getMouseModifier(),
                                  data.getModelLocation(),
                                  data.getViewLocation());
    }

    /**
     * Wrapper method for the default mouse move
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse moved is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseMove(
                                      int modifier,
                                      Point docLocation,
                                      Point viewLocation) {
        // just call jgo
        return super.doMouseMove(modifier, docLocation, viewLocation);
    }
    
    /**
     * Handles default mouse up behavior
     *
     * @param data  the mouse data of event
     * @return      true if default mouse button released is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseUp(ICanvasMouseData data) {
        // called by controller
        if (fireMouseEvent(MOUSE_RELEASE, data)) {
            return true;
        }
        
        boolean result = doDefaultMouseUp(
                data.getMouseModifier(),
                data.getModelLocation(),
                data.getViewLocation());
                                
        // hack to get the popup menu to show upon a right-click on empty canvas.
        // getState() returns 0 if the mouse was pressed on empty canvas.
        int mods = data.getMouseModifier();
        if (getState() == 0 && (mods & InputEvent.BUTTON1_MASK) == 0) {
            doDefaultMouseClick(data);
        }
        
        return result;
    }
    
    /**
     * Wrapper method for the default mouse up
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse button released is processed
     *      successfully, false otherwise.
     */
    public boolean doDefaultMouseUp(
        int modifier,
        Point docLocation,
        Point viewLocation)
    {
        if (mCurrentSourceHoverPort != null) {
            mCurrentSourceHoverPort.unset();
            mCurrentSourceHoverPort = null;
        }
        return super.doMouseUp(modifier, docLocation, viewLocation);
    }

    /**
     * Handles mouse click - actual work is delegated to the controller
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse button clicked is processed
     *      successfully, false otherwise.
     */
    public boolean doMouseClick(int modifier, Point docLocation, Point viewLocation) {
        // called by jgo
        if (getCanvasController() != null) {
            BasicCanvasMouseData data = new BasicCanvasMouseData(
                    this,
                    modifier,
                    docLocation,
                    viewLocation);
            return getCanvasController().handleMouseClick(data);
        }
        return doDefaultMouseClick(modifier, docLocation, viewLocation);
    }

    /**
     * Handles mouse double click - actual work is delegated to the controller
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse button double clicked is
     *      processed successfully, false otherwise.
     */
    public boolean doMouseDblClick(int modifier, Point docLocation, Point viewLocation) {
        if (getCanvasController() != null) {
            BasicCanvasMouseData data = new BasicCanvasMouseData(
                    this,
                    modifier,
                    docLocation,
                    viewLocation);
            return getCanvasController().handleMouseDblClick(data);
        }
        return doDefaultMouseDblClick(modifier, docLocation, viewLocation);
    }
    
    /**
     * Handles mouse up - actual work is delegated to the controller
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse button up is
     *      processed successfully, false otherwise.
     */
    public boolean doMouseUp(int modifier, Point docLocation, Point viewLocation) {
        // called by jgo
        if (getCanvasController() != null) {
            BasicCanvasMouseData data = new BasicCanvasMouseData(
                    this,
                    modifier,
                    docLocation,
                    viewLocation);
            return getCanvasController().handleMouseUp(data);
        }
        return doDefaultMouseUp(modifier, docLocation, viewLocation);
    }
    
    /**
     * Handles mouse down - actual work is delegated to the controller
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse button down is
     *      processed successfully, false otherwise.
     */
    public boolean doMouseDown(int modifier, Point docLocation, Point viewLocation) {
        // called by jgo
        if (getCanvasController() != null) {
            BasicCanvasMouseData data = new BasicCanvasMouseData(
                    this,
                    modifier,
                    docLocation,
                    viewLocation);
            return getCanvasController().handleMouseDown(data);
        }
        return doDefaultMouseDown(modifier, docLocation, viewLocation);
    }
    
    /**
     * Handles mouse move - actual work is delegated to the controller
     *
     * @param modifier      the modifier
     * @param docLocation   the documentation location
     * @param viewLocation  the view location
     * @return              true if default mouse button move is
     *      processed successfully, false otherwise.
     */
    public boolean doMouseMove(int modifier, Point docLocation, Point viewLocation) {
        // called by jgo
        if (getCanvasController() != null) {
            BasicCanvasMouseData data = new BasicCanvasMouseData(
                    this,
                    modifier,
                    docLocation,
                    viewLocation);
            return getCanvasController().handleMouseMove(data);
        }
        return doDefaultMouseMove(modifier, docLocation, viewLocation);
    }
    
    /**
     * Expend all Methoid Node in the current layer.
     */
    public void expendAllNode() {
        for (
            JGoListPosition pos = mSelectedLayer.getFirstObjectPos();
            pos != null;
            pos = mSelectedLayer.getNextObjectPos(pos)) {
            JGoObject jgoObj = mSelectedLayer.getObjectAtPos(pos);

            if (jgoObj instanceof BasicCanvasMethoidNode) {
                ((BasicCanvasMethoidNode) jgoObj).expand();
            }
        }
    }

    /**
     * Return a default location for the specified new node that will possible 
     * add to this canvas view. Currently, the new node is not use.
     * 
     * @param newNode  the node to be use to calculate the new location. 
     * @return a point where the defualt location of the new node.
     */
    public Point getDefaultLocationForNewNode (IMethoidNode newNode) {
        Point defaultPoint = new Point (getViewPosition());
        JGoObject findNode = null;
        int shift = 1;
        do {
            defaultPoint.x += 10;
            defaultPoint.y += 10;
            if (defaultPoint.y >= getExtentSize().height) {
                defaultPoint.x = getViewPosition().x + (shift++ * 40);
                defaultPoint.y = getViewPosition().y + 10;
            }
            findNode = mSelectedLayer.pickObject(defaultPoint, false);
        }
        while (findNode != null 
                && findNode.getLocation().equals(defaultPoint));

        return defaultPoint;
    }

    /**
     * Retrun the canvas field node that repersents the specifeid mapper field
     * node. Or null if the specified field node cannot be found.
     *
     * @param fieldNode  the mapper field node
     * @return           the canvas node that warps the mapper field node.
     */
    public ICanvasFieldNode findCanvasFieldNode(IFieldNode fieldNode) {
        JGoDocument jgoModel = this.getDocument();
        JGoListPosition pos = null;
        JGoObject jgoObj = null;
        ICanvasFieldNode canvasFieldNode = null;
        Collection canvasFieldNodes = null;

        for (
            pos = jgoModel.getFirstObjectPos();
            pos != null;
            pos = jgoModel.getNextObjectPos(pos)) {
            jgoObj = jgoModel.getObjectAtPos(pos);

            if (!(jgoObj instanceof ICanvasMethoidNode)) {
                continue;
            }
            ICanvasMethoidNode methoidNode = (ICanvasMethoidNode) jgoObj;

            canvasFieldNodes = methoidNode.getNodes();

            Iterator iterator = canvasFieldNodes.iterator();

            while (iterator.hasNext()) {
                canvasFieldNode = (ICanvasFieldNode) iterator.next();

                if (canvasFieldNode.getFieldNode() == fieldNode) {
                    return canvasFieldNode;
                }
            }
        }

        return null;
    }

    /**
     * Search and return the canvas methoid node that repersents the specified
     * mapper methoid node.
     *
     * @param methoidNode  Description of the Parameter
     * @return             Description of the Return Value
     */
    public ICanvasMethoidNode findCanvasMethoidNode(
        IMethoidNode methoidNode) {
        JGoDocument jgoModel = this.getDocument();
        JGoListPosition pos = null;
        JGoObject jgoObj = null;
        ICanvasMethoidNode canvasMethoidNode = null;

        for (
            pos = jgoModel.getFirstObjectPos();
            pos != null;
            pos = jgoModel.getNextObjectPos(pos)) {
            jgoObj = jgoModel.getObjectAtPos(pos);

            if (!(jgoObj instanceof ICanvasMethoidNode)) {
                continue;
            }
            ICanvasMethoidNode methoidCanvasNode =
                (ICanvasMethoidNode) jgoObj;

            if (methoidCanvasNode.getMethoidNode() == methoidNode) {
                return methoidCanvasNode;
            }
        }

        return null;
    }

    /**
     * Return a canvas methoid node that contains the specified point in its
     * bounding.
     *
     * @param point  the point to be matched
     * @return       a canvas methoid node that contains the specified point in
     *      its bounding
     */
    public ICanvasMethoidNode getCanvasMethoidNodeByPoint(Point point) {
        Point docPt = new Point (point);
        this.convertViewToDoc(docPt);
        for (JGoListPosition pos = mSelectedLayer.getFirstObjectPos();
             pos != null; pos = mSelectedLayer.getNextObjectPos(pos)) {
            JGoObject obj = mSelectedLayer.getObjectAtPos(pos);
            if (obj instanceof ICanvasMethoidNode
                && obj.getBoundingRect().contains(docPt)) {
                return (ICanvasMethoidNode) obj;
            }
        }
        return null;
    }

    /**
     * Return a canvas field node that contains the specified point in its
     * bounding.
     *
     * @param point  the point to be matched
     * @return       a canvas field node that contains the specified point in
     *      its bounding
     */
    public ICanvasFieldNode getCanvasFieldNodeByPoint(Point point) {
        Point docPt = new Point (point);
        this.convertViewToDoc(docPt);
        ICanvasMethoidNode methoidNode = getCanvasMethoidNodeByPoint(point);
        if (methoidNode != null) {
            return methoidNode.getFieldNodeByPoint(docPt);
        }
        return null;
    }
    
    /**
     * Request the specified from port to a to port to produce a new link. This
     * method is executed when user drag a port to another port on the canvas.
     * It calls noNewLink whenever the link is not able to be create. If from
     * and to share the same parent, no link will be created. Finally, it
     * delagated the creation of the new to controller.
     *
     * @param from  a from port
     * @param to    a to port
     */
    public void newLink(JGoPort from, JGoPort to) {
        if (getCanvasController() == null) {
            super.newLink(from, to);
        }

        if (from.getParent() == to.getParent()) {
            noNewLink(from, to);
            return;
        }

        if (!(from.getParent() instanceof ICanvasMethoidNode)
            || !(to.getParent() instanceof ICanvasMethoidNode)) {
            noNewLink(from, to);
            return;
        }

        ICanvasFieldNode fromFieldNode =
            ((ICanvasMethoidNode) from.getParent())
            .getFieldNodeByConnectPointObject(from);

        ICanvasFieldNode toFieldNode =
            ((ICanvasMethoidNode) to.getParent())
            .getFieldNodeByConnectPointObject(to);

        if ((fromFieldNode == null) || (toFieldNode == null)) {
            noNewLink(from, to);
            return;
        }

        if (!(getCanvasController().handleAddLink(fromFieldNode, toFieldNode))) {
            noNewLink(from, to);
        }
    }

    /**
     * Handles the case where normally no new link would be created.
     * We override this method to check for the case where we may want to
     * pop up a dialog to allow the user to create a node.
     *
     * @param srcPort  Source port
     * @param destPort  Destination port
     */
    protected void noNewLink(JGoPort from, JGoPort to)
    {
        if (mDropLocation == null) {
            return;
        }
        
        Point viewLocation = mDropLocation;
        Point modelLocation = new Point(viewLocation);
        mDropLocation = null;
        this.convertViewToDoc(modelLocation);
        
        super.noNewLink(from, to);
        
        if (modelLocation == null) {
            return;
        }
        
        if (getCanvasController() == null) {
            return;
        }

        JGoPort sourcePort = (from != null) ? from : to;
        if (sourcePort == null) {
            return;
        }
        
        ICanvasFieldNode sourceFieldNode =
            ((ICanvasMethoidNode) sourcePort.getParent())
            .getFieldNodeByConnectPointObject(sourcePort);

        if (sourceFieldNode == null) {
            return;
        }

        getCanvasController().handleAddLink(sourceFieldNode, modelLocation, viewLocation, mDropAction);
    }

    public void doCancelMouse() {
        // Bug fix: prevents the new link dialog from popping up 
        // sometimes if the drop location is outside of the canvas.
        mDropLocation = null;
        super.doCancelMouse();
    }
    
    public void onDrop(DropTargetDropEvent droptargetdropevent)
    {
        mDropLocation = droptargetdropevent.getLocation();
        mDropAction = droptargetdropevent.getDropAction();
        super.onDrop(droptargetdropevent);
    }
    
    
    /**
     * Handles key event
     *
     * @param evt  Description of the Parameter
     */
    public void onKeyEvent(KeyEvent evt) {
        int t = evt.getKeyCode();

        if (t == KeyEvent.VK_DELETE) {
            if (getDocument().isModifiable()) {
                deleteSelection();
            }
        }
    }

    /**
     * Removes the nodes from the canvas
     *
     * @param nodes  Description of the Parameter
     */
    public void removeNodes(Collection nodes) {
        final Iterator iter = nodes.iterator();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                while (iter.hasNext()) {
                    JGoObject obj = (JGoObject) iter.next();
                    if (obj instanceof ICanvasNodeToNodeLink) {
                        mFilterNodeLinks.remove(obj);
                    } else if (obj instanceof ICanvasNodeToTreeLink
                        || obj instanceof ICanvasTreeToNodeLink
                        || obj instanceof ICanvasTreeToTreeLink) {
                        mFilterTreeNodeLink.remove(obj);
                     }

                    getDocument().removeObject(obj);
                }
            }
        });
    }

    /**
     * Selectes the node that repersent by the specified data object.
     *
     * @param dataObject  the data object of the node.
     */
    public void selectNode(Object dataObject) {
        JGoDocument doc = getDocument();
        JGoListPosition pos = doc.getFirstObjectPos();

        while (pos != null) {
            JGoObject obj = doc.getObjectAtPos(pos);
            pos = doc.getNextObjectPosAtTop(pos);

            if (obj instanceof ICanvasNode) {
                ICanvasNode node = (ICanvasNode) obj;

                if (node.getDataObject() == dataObject) {
                    this.selectObject(obj);
                    // Ensure we have focus because otherwise key events don't get heard.
                    return;
                }
            }
        }
    }

    public void onDragGestureRecognized(java.awt.dnd.DragGestureEvent e) {
        super.onDragGestureRecognized(e);
    }

    /**
     * Create the canvas node with the new group node added to this mapper.
     *
     * @param groupNode  the new mapper group node.
     */
    protected void handleNewGroupNode(IMapperGroupNode groupNode) {
        if (!(groupNode instanceof IMethoidNode)) {
            return;
        }

        IMethoidNode methoidNode = (IMethoidNode) groupNode;
        ICanvasMethoidNode canvasMethoidNode = 
            getCanvasObjectFactory().createMethoidNode(methoidNode);
        addNode(canvasMethoidNode);

        IMapperNode node = methoidNode.getFirstNode();

        while (node != null) {
            handleNewNode(node);
            node = methoidNode.getNextNode(node);
        }
        groupNode.addPropertyChangeListener(mGroupNodeListener);
    }

    /**
     * Create the canvas node with the new node added to this mapper.
     *
     * @param node  the new mapper node.
     */
    protected void handleNewNode(IMapperNode node) {
        if (node instanceof IMapperGroupNode) {
            handleNewGroupNode((IMapperGroupNode) node);

            return;
        }

        List links = node.getLinks();
        synchronized (mNewLinkList) {
            for (int i = 0; i < links.size(); i++) {
                if (!mNewLinkList.contains(links.get(i))) {
                    mNewLinkList.add(links.get(i));
                }
            }
        }

        node.addPropertyChangeListener(mNodeLinkListener);
    }

    /**
     * Description of the Method
     *
     * @param node  Description of the Parameter
     */
    protected void handleRemoveNode(IMapperNode node) {
        if (node instanceof IMethoidNode) {
            handleRemoveMethoidNode((IMethoidNode) node);
            return;
        } else {
            node.removePropertyChangeListener(mNodeLinkListener);
        }
    }

    /**
     * Description of the Method
     *
     * @param methoidNode  Description of the Parameter
     */
    protected void handleRemoveMethoidNode(IMethoidNode methoidNode) {
        if (methoidNode instanceof IMethoidNode) {
            methoidNode.removePropertyChangeListener(mGroupNodeListener);

            for (IMapperNode childNode = methoidNode.getFirstNode();
                childNode != null;
                childNode = methoidNode.getNextNode(childNode)) {
                handleRemoveNode(childNode);
            }
        }

        ICanvasMethoidNode canvasMethoidNode =
            findCanvasMethoidNode(methoidNode);

        if (canvasMethoidNode != null) {
            List removeNode = new ArrayList(1);
            removeNode.add(canvasMethoidNode);
            removeNodes(removeNode);
        }
    }

    /**
     * Reallocate the tree to tree link ports when scrolling.
     * Overrides JGoView.onScrollEvent.
     *
     * @param evt  the scrolling event
     */
    protected void onScrollEvent(java.awt.event.AdjustmentEvent evt) {

        if (mFilterTreeNodeLink == null) {
            return;
        }

        int oldX = this.getViewPosition().x;
        int oldY = this.getViewPosition().y;

        // set the new view point
        super.onScrollEvent(evt);

        if (oldX != this.getViewPosition().x) {
            adjustTreeLinkPortsX();
        }

        if (oldY != this.getViewPosition().y) {
            adjustTreeLinkPortsY();
        }

        updateNodeToNodeLinks();
    }

    /**
     * Initialize canvas node with the specified view model. This method is call
     * whenever a new view model is loaded.
     *
     * @param viewModel  the mapper view model to be read from.
     */
    protected void initializeCanvasNode(IMapperViewModel viewModel) {
        Collection nodes = viewModel.getNodes();
        Iterator iterator = nodes.iterator();
        Object node = null;

        synchronized (nodes) {
            while (iterator.hasNext()) {
                node = iterator.next();

                if (node instanceof IMapperNode) {
                    handleNewNode((IMapperNode) node);
                }
            }
        }
        initializeNewLinks();
    }

    private void initializeNewLinks () {
        if (SwingUtilities.isEventDispatchThread()) {
            initializeNewLinksInST();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    initializeNewLinksInST();
                }
            });
        }
    }

    private void initializeNewLinksInST () {
        IMapperViewModel model = getParentView().getViewModel();
        synchronized (mNewLinkList) {
            for (int i = 0; i < mNewLinkList.size(); i++) {
                final IMapperLink link = (IMapperLink) mNewLinkList.get(i);

                IMapperNode startTopLevelNode = findTopLevelNode(link.getStartNode());
                IMapperNode endToLevelNode = findTopLevelNode(link.getEndNode());

                if (startTopLevelNode != null
                    && endToLevelNode != null
                    && model.containsNode(startTopLevelNode)
                    && model.containsNode(endToLevelNode)) {

                    ICanvasLink canvasLink =
                        getCanvasObjectFactory().createLink(link);
                    if (canvasLink != null) {
                        addLink(canvasLink);
                        mNewLinkList.remove(i);
                        i--;
                    } else {
                        mLogger.log(Level.SEVERE, "Factory cannot create Canvas Link from mapper link: " +
                                     link.toString());
                    }
                }
            }
        }
    }


    private IMapperNode findTopLevelNode (IMapperNode node) {
        while ( node != null && node.getGroupNode() != null ) {
            node = node.getGroupNode();
        }
        return node;
    }

    private void updateNodeToNodeLinks () {
        // this method could be called before
        // this class is instanated
        // some how JGoView constructor call onScrollEvent and call this method.
        if (mFilterNodeLinks == null) {
            return;
        }
        synchronized (mFilterNodeLinks) {
            for (int i = 0; i < mFilterNodeLinks.size(); i++) {
                AbstractCanvasLink link = (AbstractCanvasLink) mFilterNodeLinks.get(i);
                if (link.isDisplaying(getViewRect())) {
                    link.calculateStroke();
                }
            }
        }
    }

    /**
     * Return the x coordination of a tree link from port.
     *
     * @return   the x coordination of a tree link from port.
     */
    int getTreeLinkFromPortX() {
        return this.getViewPosition().x;
    }

    /**
     * Return the y coordination of the link ports in the specified node.
     *
     * @param node  the node of all the links
     * @return      the y coordination of the link port in the specified node.
     */
    int getTreeLinkPortY(IMapperNode node) {
        return node.getY() + AbstractCanvasLink.NodeYChangeListener.Y_AXIS_DIFF
            + getViewPosition().y;
    }

    /**
     * Return the x coordination of a tree link to port.
     *
     * @return   the x coordination of a tree link to port.
     */
    int getTreeLinkToPortX() {
        return getTreeLinkFromPortX() + this.getExtentSize().width;
    }

    /**
     * Adjust tree links in this canvas when size or view size change.
     */
    private void adjustTreeLinkPortsX() {
        final int fromPortX = getTreeLinkFromPortX();
        final int toPortX = getTreeLinkToPortX();


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int i = 0;
                Object filterLink = null;
                while (i < mFilterTreeNodeLink.size()) {
                    synchronized (mFilterTreeNodeLink) {
                        if (mFilterTreeNodeLink.size() <= i) {
                            break;
                        }
                        filterLink = mFilterTreeNodeLink.get(i++);
                    }

                    if (filterLink instanceof AbstractCanvasLink) {
                        final AbstractCanvasLink canvasLink =
                            (AbstractCanvasLink) filterLink;

                        if (canvasLink.getMapperLink().getStartNode()
                            instanceof IMapperTreeNode) {

                            JGoPort fromPort = canvasLink.getFromPort();
                            fromPort.setLeft(fromPortX);
                        }

                        if (canvasLink.getMapperLink().getEndNode()
                            instanceof IMapperTreeNode) {

                            JGoPort toPort = canvasLink.getToPort();
                            toPort.setLeft(toPortX);
                        }
                    }
                }
            }
        });
    }

    /**
     * Adjust tree links in this canvas when size or view size change.
     */
    private void adjustTreeLinkPortsY() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                int i = 0;
                Object linkObj = null;

                while (i < mFilterTreeNodeLink.size()) {

                    synchronized (mFilterTreeNodeLink) {
                        if (i >= mFilterTreeNodeLink.size()) {
                            break;
                        }
                        linkObj = mFilterTreeNodeLink.get(i++);
                    }

                    if (linkObj instanceof AbstractCanvasLink) {
                        final AbstractCanvasLink canvasLink =
                            (AbstractCanvasLink) linkObj;

                        if (canvasLink.getMapperLink().getStartNode()instanceof IMapperTreeNode) {
                            JGoPort fromPort = canvasLink.getFromPort();
                            fromPort.setTop(
                                getTreeLinkPortY(
                                canvasLink.getMapperLink().getStartNode()));
                        }

                        if (canvasLink.getMapperLink().getEndNode()instanceof IMapperTreeNode) {
                            JGoPort toPort = canvasLink.getToPort();
                            toPort.setTop(
                                getTreeLinkPortY(
                                canvasLink.getMapperLink().getEndNode()));
                        }
                    }
                }
            }
        });
    }

    /**
     * Print out the JGoDocument debugging information.
     */
    private void debugDoc() {
        JGoListPosition pos = this.getDocument().getFirstObjectPos();

        for (; pos != null; pos = this.getDocument().getNextObjectPos(pos)) {
            JGoObject obj = this.getDocument().getObjectAtPos(pos);
        }
    }

    /**
     * We need to determine what object exists on the entire canvas
     * at the specified mouse location. If we find a methoid node, then
     * we need to ask it for the tooltip text within itself (which iterates
     * through its contained field nodes).
     */
    public String getToolTipText(MouseEvent mouseevent)
    {
        if(!isMouseEnabled())
            return null;
        Point point = mouseevent.getPoint();
        convertViewToDoc(point);
        for(Object obj = pickDocObject(point, false); obj != null; obj = ((JGoObject) (obj)).getParent())
        {
            String s = null;
            if (obj instanceof BasicCanvasMethoidNode) {
                s = ((BasicCanvasMethoidNode) obj).getToolTipText(point);
            } else {
                s = ((JGoObject) obj).getToolTipText();
            }
            if(s != null)
                return s;
        }

        return null;
    }
    
    public void addCanvasMouseListener(ICanvasMouseListener listener) {
        synchronized (mMouseListeners) {
            mMouseListeners.add(listener);
        }
    }

    public void removeCanvasMouseListener(ICanvasMouseListener listener) {
        synchronized (mMouseListeners) {
            mMouseListeners.remove(listener);
        }
    }
    
    public boolean fireMouseEvent(int event, ICanvasMouseData data) {
        boolean result = false;
        List list = new LinkedList();
        synchronized (mMouseListeners) {
            list.addAll(mMouseListeners);
        }
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            ICanvasMouseListener l = (ICanvasMouseListener)iter.next();
            switch (event) {
            case MOUSE_CLICK:
                if (l.doMouseClick(data)) {
                    result = true;
                }
                break;
            case MOUSE_DBLCLICK:
                if (l.doMouseDblClick(data)) {
                    result = true;
                }
                break;
            case MOUSE_PRESS:
                if (l.doMouseDown(data)) {
                    result = true;
                }
                break;
            case MOUSE_RELEASE:
                if (l.doMouseUp(data)) {
                    result = true;
                }
                break;
            case MOUSE_MOVE:
                if (l.doMouseMove(data)) {
                    result = true;
                }
                break;
            }
        }
        return result;
    }


    public JGoSelection createDefaultSelection() {
        return new PortSelection(this) {
            public JGoObject selectObject(JGoObject obj) {
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            requestFocus();
                        }
                    });

                getDocument().bringObjectToFront(obj);  
                return super.selectObject(obj);
            }
        };
    }

    public void autoscroll(Point location) {
        // do nothing... we don't want the canvas to autoscroll
    }

    
    
    private static class HoverPort {
        private BasicCanvasFieldNode mFieldNode;
        private DrawPort mDrawPort;
        public HoverPort(BasicCanvasFieldNode field) {
            mFieldNode = field;
            mDrawPort = field.getDrawPort();
        }
        public HoverPort(DrawPort drawPort) {
            mFieldNode = drawPort.getFieldNode();
            mDrawPort = drawPort;
        }
        public void set(boolean isActivated) {
            mDrawPort.setIsActivated(isActivated);
            mDrawPort.setIsHovering(true);
            mFieldNode.setIsHovering(true);
            mFieldNode.layoutPorts();
        }
        public void unset() {
            mDrawPort.setIsActivated(false);
            mDrawPort.setIsHovering(false);
            mFieldNode.setIsHovering(false);
            mFieldNode.layoutPorts();
        }
    }

    /**
     * This class listens on the change of the size of this canvas to relocate
     * the ports that connect to tree node.
     *
     * @author    sleong
     * @created   December 4, 2002
     */
    private class CanvasSizeListener
         extends ComponentAdapter {
        /**
         * Re-adjust tree link ports x coordination when this canvas resize.
         *
         * @param e  the ComponentEvent
         */
        public void componentResized(ComponentEvent e) {
            adjustTreeLinkPortsX();
            updateNodeToNodeLinks();
        }
    }

    /**
     * This class listens on the change of each mapper node of the view model
     * and the node inside the group node for link changes.
     *
     * @author    sleong
     * @created   December 4, 2002
     */
    private class NodeLinkListener
         implements PropertyChangeListener {
        /**
         * the canvas of the node it is listenning on.
         */
        private ICanvasView mCanvasView;

        /**
         * Construct a NodeLinkListener with the specified canvas.
         *
         * @param canvasView  the canvas
         */
        private NodeLinkListener(ICanvasView canvasView) {
            mCanvasView = canvasView;
        }

        /**
         * Execute when a change of a mapper node. This method handles the add
         * and remove link event of a node.
         *
         * @param e  the PropertyChangeEvent event.
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IMapperNode.LINK_ADDED)) {
                if (e.getNewValue() instanceof IMapperLink
                    && (e.getSource() == ((IMapperLink) e.getNewValue()).getStartNode())) {
                    mNewLinkList.add((IMapperLink) e.getNewValue());
                    initializeNewLinks();
                }
            } else if (e.getPropertyName().equals(IMapperNode.LINK_REMOVED)) {
                if (e.getOldValue() instanceof IMapperLink) {
                    IMapperLink link = (IMapperLink) e.getOldValue();

                    ICanvasLink canvasLink = getCanvasLinkByDataObject(link);

                    if (canvasLink != null) {
                        List removeLink = new ArrayList(1);
                        removeLink.add(canvasLink);
                        mCanvasView.removeNodes(removeLink);
                    }
                }
            }
        }
    }

    /**
     * This class listen on the change of view model.
     *
     * @author    sleong
     * @created   December 4, 2002
     */
    private class ViewModelChangeListener
         implements PropertyChangeListener {
        /**
         * Add/Remove canvas objects when there is a link changed on the link
         * model.
         *
         * @param e  the PropertyChangeEvent
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IMapperViewModel.NODE_ADDED)) {
                handleNewNode((IMapperNode) e.getNewValue());
                initializeNewLinks();
            } else if (e.getPropertyName()
                .equals(IMapperViewModel.NODE_REMOVED)) {
                handleRemoveNode((IMapperNode) e.getOldValue());
            }
        }
    }

    /**
     * Provides grouop node add / remove nodes listener functions.
     *
     * @author    sleong
     * @created   January 29, 2003
     */
    private class GroupNodeListener
         implements PropertyChangeListener {

        /**
         * If new node added to the group node, add listener for new link event.
         * If node removed from the group node, remove new link listener.
         *
         * @param e  Description of the Parameter
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IMapperGroupNode.NODE_INSERTED)) {
                handleNewNode((IMapperNode) e.getNewValue());
                initializeNewLinks();
            } else if (e.getPropertyName().equals(IMapperGroupNode.NODE_REMOVED)) {
                handleRemoveNode((IMapperNode) e.getOldValue());
            }
        }
    }
}

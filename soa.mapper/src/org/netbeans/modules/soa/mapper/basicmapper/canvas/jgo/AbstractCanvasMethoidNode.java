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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import com.nwoods.jgo.JGoObject;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.AccessibleArea;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvas;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasAction;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasComponentNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasControlNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNodePopupBox;

/**
 * <p>
 *
 * Title: </p> AbstractCanvasMethoidNode<p>
 *
 * Description: </p> AbstractCanvasMethoidNode provides the basic implemenation
 * of a ICanvasMethoidNode providing the basic view of a canvas methoid node.
 * <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public abstract class AbstractCanvasMethoidNode
     extends AccessibleArea
     implements ICanvasMethoidNode {

    /**
     * the canvas of this node
     */
    private ICanvas mCanvas;

    /**
     * the dest nodes storage
     */
    private List mDestNodeList;

    /**
     * the canvas field node storage
     */
    private List mFieldNodeList;

    /**
     * the methoid node of this canvas node
     */
    protected IMethoidNode mMethoidNode;

    /**
     * the group node of this group node
     */
    private ICanvasGroupNode mGroup;

    /**
     * flag indicates if group node is expanded.
     */
    private boolean mIsExpanded;

    /**
     * the canvas of this node.
     */
    private ICanvasView mMapperCanvas;

    /**
     * the name of this node
     */
    private String mName;

    /**
     * the source node list
     */
    private List mSourceNodeList;

    /**
     * Creates a new AbstractCanvasMethoidNode object, with specified the
     * methoid node.
     *
     * @param methoidNode  the methoid node this canvas node repersents.
     */
    public AbstractCanvasMethoidNode(IMethoidNode methoidNode) {
        super();
        mMethoidNode = methoidNode;
        mFieldNodeList = new Vector();
        setName(mMethoidNode.getMethoidName());
    }

    /**
     * Return the bounding rectangle of this node.
     *
     * @return the bounding rectangle of this node.
     */
    public Rectangle getBounding() {
        return getBoundingRect();
    }

    /**
     * Retrieves the action
     *
     * @return   always null
     */
    public ICanvasAction getAction() {
        return null;
    }

    /**
     * Return the addable nodes.
     *
     * @return   always null
     */
    public List getAddableNodes() {
        return null;
    }

    /**
     * Retrieves the aux popup
     *
     * @return   always null
     */
    public ICanvasNodePopupBox getAuxPopup() {
        return null;
    }

    /**
     * Retrieves the cavnas
     *
     * @return   The canvas value
     */
    public ICanvas getCanvas() {
        return mMapperCanvas;
    }

    /**
     * Return the compoeent node.
     *
     * @return   always null
     */
    public ICanvasComponentNode getComponentNode() {
        return null;
    }

    /**
     * Retrieves the container
     *
     * @return   The container value
     */
    public ICanvasGroupNode getContainer() {
        return mGroup;
    }

    /**
     * Retrieves the control node associated with the canvas node. Return null
     * is nothing associated.
     *
     * @return   always null
     */
    public ICanvasControlNode getControlNode() {
        return null;
    }

    /**
     * Retrieves the data object
     *
     * @return   - underlying data object represented by this node
     */
    public Object getDataObject() {
        return getMethoidNode();
    }

    /**
     * Retrieves a list of nodes that this node is pointing to
     *
     * @return   The destinationNodes value
     */
    public List getDestinationNodes() {
        return Collections.unmodifiableList(mDestNodeList);
    }

    /**
     * Return a canvas field node that contains the specified connection object,
     * or null if the field node cannot be found.
     *
     * @param pointObj  the connection object to be matched.
     * @return          a canvas field node that contains the specified
     *      connection object, or null if the field node cannot be found.
     */
    public ICanvasFieldNode getFieldNodeByConnectPointObject(Object pointObj) {

        synchronized (mFieldNodeList) {
            int i = mFieldNodeList.size() - 1;
            Object node = null;

            for (; i >= 0; i--) {
                node = mFieldNodeList.get(i);

                if (
                        node instanceof ICanvasFieldNode &&
                        ((ICanvasFieldNode) node).getConnectPointObject().equals(pointObj))
                {
                    return (ICanvasFieldNode) node;
                }
            }
        }

        return null;
    }

    /**
     * Return a canvas field node that contains the specified point in its
     * bounding.
     *
     * @param point  the point to match
     * @return       a canvas field node that contains the specified point in
     *      its bounding.
     */
    public ICanvasFieldNode getFieldNodeByPoint(Point point) {
        synchronized (mFieldNodeList) {
            int i = mFieldNodeList.size() - 1;
            Object node = null;

            for (; i >= 0; i--) {
                node = mFieldNodeList.get(i);

                if (node instanceof ICanvasFieldNode
                    && node instanceof JGoObject
                    && ((JGoObject) node).getBoundingRect().contains(point)) {
                    return (ICanvasFieldNode) node;
                }
            }
        }

        return null;
    }

    /**
     * Return the methoid node that repersents by this canvas node.
     *
     * @return   the methoid node that repersents by this canvas node.
     */
    public IMethoidNode getMethoidNode() {
        return mMethoidNode;
    }

    /**
     * Gets the Icon which representing this node
     *
     * @return   always null
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Retrun the mapper canvas that displays this node.
     *
     * @return   the canvas displays this node.
     */
    public ICanvasView getMapperCanvas() {
        return mMapperCanvas;
    }

    /**
     * Retrieves the name of the node
     *
     * @return   - name
     */
    public String getName() {
        return mName;
    }

    /**
     * Retrieves the location
     *
     * @return   The nodeLocation value
     */
    public Point getNodeLocation() {
        return this.getLocation();
    }

    /**
     * DOCUMENT ME!
     *
     * @return   DOCUMENT ME!
     */
    public Collection getNodes() {
        return Collections.unmodifiableCollection(mFieldNodeList);
    }

    /**
     * Retrieves the popup
     *
     * @return   always null
     */
    public ICanvasNodePopupBox getPopup() {
        return null;
    }

    /**
     * Retrieves a list of nodes that have links connecting TO this node
     *
     * @return   The sourceNodes value
     */
    public List getSourceNodes() {
        return Collections.unmodifiableList(mSourceNodeList);
    }

    /**
     * Retrieves the underlying ui component
     *
     * @return   The uIComponent value
     */
    public Object getUIComponent() {
        return this;
    }

    /**
     * Return true if this node is expended, false otherwise.
     *
     * @return   true if this node is expended, false otherwise.
     */
    public boolean isExpanded() {
        return mIsExpanded;
    }

    /**
     * Sets the action
     *
     * @param action  The new action value
     */
    public void setAction(ICanvasAction action) { }

    /**
     * Set the addable nodes.
     *
     * @param nodes  the addable node list
     */
    public void setAddableNodes(List nodes) { }

    /**
     * Sets the aux popup this node is associated with
     *
     * @param popup  The new auxPopup value
     */
    public void setAuxPopup(ICanvasNodePopupBox popup) { }

    /**
     * Sets the canvas this node belongs to
     *
     * @param canvas  The new canvas value
     */
    public void setCanvas(ICanvas canvas) { }

    /**
     * Sets the component node associated with this canvas node
     *
     * @param node  The new componentNode value
     */
    public void setComponentNode(ICanvasComponentNode node) { }

    /**
     * Sets the container this node belongs to
     *
     * @param group  The new container value
     */
    public void setContainer(ICanvasGroupNode group) {
        mGroup = group;
    }

    /**
     * Sets the control node associated with this canvas node
     *
     * @param node  The new controlNode value
     */
    public void setControlNode(ICanvasControlNode node) { }

    /**
     * sets the data object
     *
     * @param obj  - then object to be represented by this node
     */
    public void setDataObject(Object obj) { }

    /**
     * Set the group folder object
     *
     * @param folder  the group folder object
     */
    public void setGroupFolder(Object folder) { }

    /**
     * Retrun the group folder object
     *
     * @return   the group folder object
     */
    public Object getGroupFolder() {
        return null;
    }

    /**
     * Sets the icon associated with this node
     *
     * @param icon  The new icon value
     */
    public void setIcon(Icon icon) { }

    /**
     * Set the mapper canvas that displays this node.
     *
     * @param canvas  the canvas displays this node.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        mMapperCanvas = canvas;
    }

    /**
     * Sets the name of the name
     *
     * @param name  - new name
     */
    public void setName(String name) {
        mName = name;
        updateLabel(mName);
    }

    /**
     * Sets the location
     *
     * @param p  - location
     */
    public void setNodeLocation(Point p) {
        this.setLocation(p);
    }

    /**
     * Sets the abstract canvas node popup
     *
     * @param popup  The new popup value
     */
    public void setPopup(ICanvasNodePopupBox popup) { }

    /**
     * Adds a new destination node
     *
     * @param node  The feature to be added to the DestinationNode attribute
     */
    public void addDestinationNode(ICanvasNode node) {
        mDestNodeList.add(node);
    }

    /**
     * add a canvas node to this node.
     *
     * @param node  the canvas of this node
     */
    public void addNode(ICanvasNode node) {
        synchronized (mFieldNodeList) {
            mFieldNodeList.add(node);
            node.setContainer(this);
        }
    }

    /**
     * Insert a chid node to this container with the specified index
     *
     * @param node   the node to be added
     * @param index  the index to add the node
     */
    public void insertNode(ICanvasNode node, int index) {
        synchronized (mFieldNodeList) {
            mFieldNodeList.add(index, node);
            node.setContainer(this);
        }
    }

    /**
     * Adds a new source node
     *
     * @param node  The feature to be added to the SourceNode attribute
     */
    public void addSourceNode(ICanvasNode node) {
        mSourceNodeList.add(node);
    }

    /**
     * Retrun the canvas node that is repersenting the collapse canvas node.
     *
     * @return   the canvas node that is repersenting the collapse canvas node.
     */
    public ICanvasNode collapse() {
        mIsExpanded = false;
        return this;
    }

    /**
     * Retrun the canvas node that is repersenting the expand canvas node.
     *
     * @return   the canvas node that is repersenting the expand canvas node.
     */
    public ICanvasNode expand() {
        mIsExpanded = true;

        return this;
    }

    /**
     * Refresh the node image.
     *
     * @param icon  the icon of this node.
     */
    public void refreshNodeImage(javax.swing.Icon icon) { }

    /**
     * Removes a destination node
     *
     * @param node  Description of the Parameter
     */
    public void removeDestinationNode(ICanvasNode node) {
        mDestNodeList.remove(node);
    }

    /**
     * Remove a canvas node
     *
     * @param node  the canvas node to be removed
     */
    public void removeNode(ICanvasNode node) {
        mFieldNodeList.remove(node);
        node.setContainer(null);
    }

    /**
     * Removes a source node
     *
     * @param node  the source node to be removed
     */
    public void removeSourceNode(ICanvasNode node) {
        mSourceNodeList.remove(node);
    }

    /**
     * update the text label
     *
     * @param text  update the text label
     */
    public void updateLabel(String text) { }

    /**
     * Description of the Method
     *
     * @param docPoint  Description of the Parameter
     * @return          Description of the Return Value
     */
    public ICanvasNode findNodeByPoint(Point docPoint) {
        synchronized (mFieldNodeList) {
            for (int i = 0; i < mFieldNodeList.size(); i++) {
                if (((JGoObject) mFieldNodeList.get(i)).isPointInObj(docPoint)) {
                    return (ICanvasNode) mFieldNodeList.get(i);
                }
            }
        }
        return null;
    }

    /**
     * Return the modifiable node list
     *
     * @return   the modifiable node list
     */
    protected List getModifiableNodeList() {
        return mFieldNodeList;
    }

    public void setTitle(String title) { }
    
    public String getTitle() {
        return mName;
    }
}

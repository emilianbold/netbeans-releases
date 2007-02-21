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

package org.netbeans.modules.soa.mapper.basicmapper;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: </p> MapperNode <p>
 *
 * Description: </p> MapperNode Provides common functionalities of IMapperNode
 * interface.<p>
 *
 * @author    Un Seng Leong
 * @created   December 19, 2002
 */
public class MapperNode
     implements IMapperNode, Cloneable {

    /**
     * Group node contains this node
     */
    private IMapperGroupNode mGroupNode;

    /**
     * Links connected to this node.
     */
    private List mLinks;

    /**
     * The user object
     */
    private Object mNodeObject;

    /**
     * Property change listener list
     */
    private List mPropertyListeners;

    /**
     * the x coordination of this node
     */
    private int mX;

    /**
     * the y coordination of this node
     */
    private int mY;

    /**
     * Storage for the nodes that connect from this node.
     */
    private List mNextNodes;

    /**
     * Storage for the nodes that connect to this node.
     */
    private List mPreviousNodes;

    /**
     * Creates a new MapperNode object with 0,0 coordination.
     */
    public MapperNode() {
        this(0, 0);
    }

    /**
     * Creates a new MapperNode object with specified x and y coordination.
     *
     * @param x  the x coordination of this node
     * @param y  the y coordination of this node
     */
    public MapperNode(
        int x,
        int y) {
        this.mX = x;
        this.mY = y;
        mPropertyListeners = new Vector();
        mNextNodes = new Vector();
        mPreviousNodes = new Vector();
        mLinks = new Vector();
    }


    /**
     * Return the group node associate with this node.
     *
     * @return   the group node associate with this node.
     */
    public IMapperGroupNode getGroupNode() {
        return mGroupNode;
    }


    /**
     * Retrun the number of links connected to this node.
     *
     * @return   the number of links connected to this node.
     */
    public int getLinkCount() {
        return mLinks.size();
    }


    /**
     * Return a list of links that connects to this node.
     *
     * @return   a list of link that connects to this node.
     */
    public List getLinks() {
        return new ArrayList(mLinks);
    }


    /**
     * Return the design object of this node in object repersentation.
     *
     * @return   an object repsersenting this node in another form of object.
     */
    public Object getNodeObject() {
        return mNodeObject;
    }


    /**
     * Return the x coordination of this node in a 2 dimension base view.
     *
     * @return   an integer of x of this node.
     */
    public int getX() {
        return mX;
    }


    /**
     * Return the y coordination of this node in a 2 dimension base view.
     *
     * @return   an integer of y of this node.
     */
    public int getY() {
        return mY;
    }


    /**
     * Set the group node that contains this node.
     *
     * @param groupNode  the group node contains this node
     */
    public void setGroupNode(IMapperGroupNode groupNode) {
        mGroupNode = groupNode;
    }


    /**
     * Set the object of this node in another object repersentation.
     *
     * @param nodeObject  an object repsersenting this node in another form.
     */
    public void setNodeObject(Object nodeObject) {
        mNodeObject = nodeObject;
    }


    /**
     * Sets the x coordination of this node in a 2 dimension base view.
     *
     * @param x  an integer of x of this node.
     */
    public void setX(int x) {
        if (mX == x) {
            return;
        }

        int oldX = mX;
        this.mX = x;

        firePropertyChange(
            IMapperNode.X_CHANGE,
            new Integer(x),
            new Integer(oldX));
    }


    /**
     * Sets the y coordination of this node in a 2 dimension base view.
     *
     * @param y  an integer of y of this node.
     */
    public void setY(int y) {
        if (mY == y) {
            return;
        }

        int oldY = mY;
        this.mY = y;
        firePropertyChange(
            IMapperNode.Y_CHANGE,
            new Integer(mY),
            new Integer(oldY));
    }


    /**
     * Add a link that is connected to this node. Link is added only if the link
     * is not already existed.
     *
     * @param link  the is connected to this node.
     */
    public void addLink(IMapperLink link) {
        if (!mLinks.contains(link)) {
            mLinks.add(link);
            if (link.getStartNode() == this) {
                mNextNodes.add(link.getEndNode());
            } else {
                mPreviousNodes.add(link.getStartNode());
            }
            firePropertyChange(
                IMapperNode.LINK_ADDED,
                link,
                null);
        }
    }


    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mPropertyListeners.add(listener);
    }


    /**
     * Return true if the specified link is connected to this node, false
     * otherwise.
     *
     * @param link  the specified to check
     * @return      true if specified link is one of the link connected to this
     *      node.
     */
    public boolean containsLink(IMapperLink link) {
        return mLinks.contains(link);
    }


    /**
     * Remove a link that is no longer connected to this node. Link is remove
     * only if the link existes.
     *
     * @param link  the is no longer connected to this node.
     */
    public void removeLink(IMapperLink link) {
        if (mLinks.contains(link)) {
            mLinks.remove(link);
            if (link.getStartNode() == this) {
                mNextNodes.remove(link.getEndNode());
            } else {
                mPreviousNodes.remove(link.getStartNode());
            }
            firePropertyChange(
                IMapperNode.LINK_REMOVED,
                null,
                link);
        }
    }

    /**
     * Return a list of nodes that connects from this node.
     *
     * @return   a list of nodes that connects from this node. The element of
     *      the list is IMapperNode.
     */
    public List getNextNodes() {
        return new ArrayList(mNextNodes);
    }

    /**
     * Return a list of nodes that connects to this node.
     *
     * @return   a list of nodes that connects from this node. The element of
     *      the list is IMapperNode.
     */
    public List getPreviousNodes() {
        return new ArrayList(mPreviousNodes);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPropertyListeners.remove(listener);
    }


    /**
     * Fire a specified property change event of this node.
     *
     * @param propertyName  the name of this property has changed
     * @param newValue      the new value of the property
     * @param oldValue      the old value of the property
     */
    protected void firePropertyChange(
        String propertyName,
        Object newValue,
        Object oldValue) {

        if (mPropertyListeners.size() > 0) {
            MapperUtilities.firePropertyChanged(
                (PropertyChangeListener[]) mPropertyListeners.toArray(
                new PropertyChangeListener[mPropertyListeners.size()]), this, propertyName, newValue, oldValue);
        }
    }

    /**
     * Return true if the specified object is equal to this node, false
     * otherwise. This method simply use "reference equal".
     *
     * @param obj  the object to be matched
     * @return     true if the specified object is equal to this node, false
     *      otherwise.
     */
    public boolean equals(Object obj) {
        return this == obj;
    }

    /**
     * Return a string repersentation of this node.
     *
     * @return   a string repersentation of this node.
     */
    public String getParamString() {
        return super.toString() + " [X=" + mX + ", Y=" + mY
            + ", links=" + mLinks + ", nodeObj=" + mNodeObject + ", groupNode=" + mGroupNode + "]";
    }

    /**
     * Clone a new mapper node. This method only copies the x and y
     * coordinations of this node. Other property of this node will set to null.
     * For links and property listeners, are assigned to new list.
     *
     * @return   return a new mapper node repersents this node.
     */
    public Object clone() {
        MapperNode cloneNode = null;
        try {
            cloneNode = (MapperNode) super.clone();
        } catch (CloneNotSupportedException e) {
            cloneNode = new MapperNode();
        }

        cloneNode.mX = mX;
        cloneNode.mY = mY;

        cloneNode.mNodeObject = null;
        cloneNode.mGroupNode = null;
        cloneNode.mLinks = new Vector();
        cloneNode.mPropertyListeners = new Vector();

        return cloneNode;
    }
}

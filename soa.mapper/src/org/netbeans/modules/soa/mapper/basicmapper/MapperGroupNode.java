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

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: </p> MapperGroupNode<p>
 *
 * Description: </p> MapperGroupNode extends from MapperNode to provide simple
 * implemenation of IMapperGroupNode. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 19, 2002
 * @version   1.0
 */
public class MapperGroupNode
     extends MapperNode
     implements IMapperGroupNode {

    /**
     * The children node list
     */
    private List mNodeList;

    /**
     * Creates a new MapperGroupNode object with empty child.
     */
    public MapperGroupNode() {
        this(null);
    }

    /**
     * Creates a new MapperGroupNode object with specified children.
     *
     * @param nodeList  the children of this MapperGroupNode
     */
    public MapperGroupNode(Collection nodeList) {
        super();

        if (nodeList != null) {
            mNodeList = new Vector(nodeList);
        } else {
            mNodeList = new Vector();
        }
    }

    /**
     * Return the first position node of this group node.
     *
     * @return   the first position node of this group node.
     */
    public IMapperNode getFirstNode() {
        synchronized (mNodeList) {
            int nodeNum = getNodeCount();
            if (nodeNum == 0) {
                return null;
            }
            return (IMapperNode) mNodeList.get(0);
        }
    }

    /**
     * Return the last position node of this group node.
     *
     * @return   the last position node of this group node.
     */
    public IMapperNode getLastNode() {
        synchronized (mNodeList) {
            int nodeNum = getNodeCount();

            if (nodeNum == 0) {
                return null;
            }

            return (IMapperNode) mNodeList.get(nodeNum - 1);
        }
    }

    /**
     * Return the end node that contains by the specifed link and also is one of
     * the node in this group node.
     *
     * @param link  the specified link
     * @return      the node in this group node that connected end point to the
     *      of the specified link.
     */
    public IMapperNode getLinkEndNode(IMapperLink link) {
        IMapperNode endNode = link.getEndNode();

        if (mNodeList.contains(endNode)) {
            return endNode;
        }

        return null;
    }

    /**
     * Return the start node that contains by the specifed link and also is one
     * of the node in this group node.
     *
     * @param link  the specified link
     * @return      the node in this group node that connected to the start
     *      point of the specified link.
     */
    public IMapperNode getLinkStartNode(IMapperLink link) {
        IMapperNode startNode = link.getStartNode();

        if (mNodeList.contains(startNode)) {
            return startNode;
        }

        return null;
    }

    /**
     * Return the next node of the specified node.
     *
     * @param node  the start node to search for.
     * @return      the next node of the specified start node
     */
    public IMapperNode getNextNode(IMapperNode node) {
        synchronized (mNodeList) {
            int nodeIndex = mNodeList.lastIndexOf(node);

            if ((nodeIndex < 0) || (nodeIndex == (mNodeList.size() - 1))) {
                return null;
            }

            return (IMapperNode) mNodeList.get(nodeIndex + 1);
        }
    }

    /**
     * Return the number of nodes in this group node.
     *
     * @return   the number of nodes in this group node.
     */
    public int getNodeCount() {
        return mNodeList.size();
    }

    /**
     * Return the previous node of the specified node.
     *
     * @param node  the previous node to search for.
     * @return      the previous node of the specified start node
     */
    public IMapperNode getPreviousNode(IMapperNode node) {
        synchronized (mNodeList) {
            int nodeIndex = mNodeList.lastIndexOf(node);

            if (nodeIndex <= 0) {
                return null;
            }

            return (IMapperNode) mNodeList.get(nodeIndex - 1);
        }
    }

    /**
     * Return true if the specifed node is in this group node, false otherwise.
     *
     * @param node  the specifed node to find.
     * @return      true if the specifed node is in this group node, false
     *      otherwise.
     */
    public boolean containsNode(IMapperNode node) {
        return mNodeList.contains(node);
    }

    /**
     * Adds a node to be the first child of the IMapperGroupNode object
     *
     * @param node  the first child of the IMapperGroupNode object
     */
    public void addToFirst(IMapperNode node) {
        synchronized (mNodeList) {
            mNodeList.add(0, node);
            node.setGroupNode(this);
        }
        firePropertyChange(NODE_INSERTED, node, null);
    }

    /**
     * Adds a node to be the Last child of the IMapperGroupNode object
     *
     * @param node  be the Last child of the IMapperGroupNode object
     */
    public void addToLast(IMapperNode node) {
        Object prevNode = null;
        synchronized (mNodeList) {
            prevNode = getLastNode();
            mNodeList.add(node);
            node.setGroupNode(this);
        }
        firePropertyChange(NODE_INSERTED, node, prevNode);
    }

    /**
     * Add a node to be the next one of the specified child node.
     *
     * @param childNode  the childNode to be match
     * @param newNode    the new node to be added next to child
     */
    public void addNextNode(IMapperNode childNode, IMapperNode newNode) {
        synchronized (mNodeList) {
            int index = mNodeList.indexOf(childNode);
            if (index < 0) {
                return;
            }
            mNodeList.add(index + 1, newNode);
            newNode.setGroupNode(this);
        }
        firePropertyChange(NODE_INSERTED, newNode, childNode);
    }

    /**
     * Add a node to be the previous one of the specified child node.
     *
     * @param childNode  the childNode to be match
     * @param newNode    the new node to be added previous to child
     */
    public void addPrevNode(IMapperNode childNode, IMapperNode newNode) {
        Object prevNewNode = null;
        synchronized (mNodeList) {
            int index = mNodeList.indexOf(childNode);
            if (index < 0) {
                return;
            }

            if (index > 0) {
                prevNewNode = mNodeList.get(index - 1);
            }

            mNodeList.add(index, newNode);
            newNode.setGroupNode(this);
        }
        firePropertyChange(NODE_INSERTED, newNode, prevNewNode);
    }

    /**
     * Remove the sepecified from this group node.
     *
     * @param node  the node to be removed.
     */
    public void removeNode(IMapperNode node) {
        mNodeList.remove(node);
        node.setGroupNode(null);
        firePropertyChange(NODE_REMOVED, null, node);
    }

    /**
     * Find and return the mapper node that contains the specified node object,
     * or null if the node not found.
     *
     * @param nodeObj  the specifed node object to be matched.
     * @return         the mapper node that contains the specified node object,
     *      or null if the node not found.
     */
    public IMapperNode findNodeByNodeObject(Object nodeObj) {
        for (int i = 0; i < mNodeList.size(); i++) {
            IMapperNode node =
                (IMapperNode) mNodeList.get(i);

            if (node.getNodeObject().equals(nodeObj)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Return the node list contains the children node.
     *
     * @return   the node list contains the children node.
     */
    protected List getNodeList() {
        return mNodeList;
    }

    /**
     * Return a string repersentation of this node.
     *
     * @return   a string repersentation of this node.
     */
    public String getParamString() {
        return super.getParamString() + "[node list=" + mNodeList + "]";
    }

    /**
     * Clone a new group mapper node. This method will not copy the node object,
     * the links and the property listeners of this node to the return object.
     * Each child is called clone to added to the new group mapper node.
     *
     * @return   return a new mapper node repersents this node.
     */
    public Object clone() {
        MapperGroupNode newNode = (MapperGroupNode) super.clone();
        newNode.mNodeList = new Vector();
        for (int i = 0; i < getNodeList().size(); i++) {
            IMapperNode newChild = (IMapperNode)
                ((IMapperNode) getNodeList().get(i)).clone();
            newChild.setGroupNode(newNode);
            newNode.mNodeList.add(newChild);
        }
        return newNode;
    }
}

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

import com.nwoods.jgo.JGoRectangle;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoSelection;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.AccessibleArea;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.GradientRectangle;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;

/**
 * <p>
 *
 * Title: </p> BasicCanvasMethoidNode<p>
 *
 * Description: </p> BasicCanvasMethoidNode provides a basic implementation of a
 * methoid canvas node. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class BasicCanvasMethoidNode
        extends AbstractCanvasMethoidNode {
    
    /**
     * the log instance
     */
    private Logger mLogger = Logger.getLogger(BasicCanvasMethoidNode.class.getName());
    
    /**
     * the border rectange
     */
    private JGoRectangle mBorderRect;
    
    /**
     * the height of this canvas node
     */
    private int mFixedHeight;
    
    /**
     * the width of this canvas node
     */
    private int mFixedWidth;
    
    /**
     * the title bar ui component of this node.
     */
    private BasicTitleBarUI mTitleBar;
    
    
    /**
     * Creates a new BasicCanvasMethoidNode object.
     *
     * @param node  the method node
     */
    public BasicCanvasMethoidNode(IMethoidNode node) {
        super(node);
        mTitleBar = new BasicTitleBarUI(
                node.getMethoidName(),
                ((ImageIcon) node.getIcon()).getImage());
        mTitleBar.setSelectable(false);
        mTitleBar.setDraggable(false);
        mTitleBar.setResizable(false);
        mTitleBar.setSelectable(false);
        mTitleBar.resizeToMinimum();
        
        this.addObjectAtTail(mTitleBar);
        this.setSelectable(true);
        this.setDraggable(true);
        this.setResizable(false);
        this.setSelectable(true);
        this.setPickableBackground(true);
        
        mBorderRect = new GradientRectangle(this);
        mBorderRect.setSelectable(false);
        mBorderRect.setDraggable(false);
        mBorderRect.setResizable(false);
        
        this.addObjectAtHead(mBorderRect);
        
        super.expand();
        
        node.addPropertyChangeListener(new MethoidPropertyListener());
    }
    
    /**
     * Return ture if the point is within the expand or collapse button, false
     * otherwise.
     *
     * @param point  the point to check
     * @return       ture if the point is within the expand or collapse button,
     *      false otherwise.
     */
    public boolean isInButton(Point point) {
        return this.mTitleBar.isInButton(point);
    }
    
    /**
     * Add a canvas node
     *
     * @param node  the node to be added
     */
    public void addNode(ICanvasNode node) {
        super.addNode(node);
        configNewChild(node);
    }
    
    /**
     * Insert a chid node to this container with the specified index
     *
     * @param node   the node to be added
     * @param index  the index to add the node
     */
    public void insertNode(ICanvasNode node, int index) {
        super.insertNode(node, index);
        configNewChild(node);
    }
    
    /**
     * Return the collapse canvas node of this node.
     *
     * @return   the collapse canvas node of this node.
     */
    public ICanvasNode collapse() {
        super.collapse();
        layoutChildren();
        changeAreaSize();
        mTitleBar.collapse();
        return this;
    }
    
    /**
     * Return the expand canvas node of this node.
     *
     * @return   the expand canvas node of this node.
     */
    public ICanvasNode expand() {
        super.expand();
        layoutChildren();
        changeAreaSize();
        mTitleBar.expand();
        return this;
    }
    
    /**
     * Remove a node from this methoid.
     *
     * @param node  a node from this methoid
     */
    public void removeNode(ICanvasNode node) {
        super.removeNode(node);
        
        if (node instanceof JGoObject) {
            this.removeObject((JGoObject) node);
        }
        
        if (node instanceof ICanvasFieldNode) {
            ICanvasFieldNode canvasFieldNode = (ICanvasFieldNode) node;
            canvasFieldNode.getFieldNode().setGroupNode(null);
            
            Object port = canvasFieldNode.getConnectPointObject();
            if (port instanceof JGoObject) {
                this.removeObject((JGoObject) port);
            }
        }
    }
    
    /**
     * Invoks when size or location change of this canvas.
     *
     * @param prevRect  the pervious rectangle.
     */
    protected void geometryChange(java.awt.Rectangle prevRect) {
        // see if this is just a move and not a scale
        if ((prevRect.width == getWidth()) && (prevRect.height == getHeight())) {
            // let the default JGoArea implementation do the work
            super.geometryChange(prevRect);
        } else {
            ensureBounding();
        }
        
        updatePortBounds();
    }
    
    // Update the port positions, which will in-turn change
    // the bounding rect of the methoid itself.
    private void updatePortBounds() {
        JGoListPosition pos = getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = getObjectAtPos(pos);
            pos = getNextObjectPos(pos);
            
            if (obj instanceof AccessibleArea) {
                if (((AccessibleArea) obj).isBoundingRectInvalid()) {
                    continue;
                }
            }
            
            if (obj instanceof BasicCanvasFieldNode) {
                ((BasicCanvasFieldNode) obj).layoutPorts();
            }
        }
    }
    
    /**
     * layout the children of this canvas node.
     */
    protected void layoutChildren() {
        List nodeList = this.getModifiableNodeList();
        int i = 0;
        JGoObject prevNode = mTitleBar;
        JGoObject node = null;
        
        synchronized (nodeList) {
            for (; i < nodeList.size(); i++) {
                node = (JGoObject) nodeList.get(i);
                
                if (isExpanded()) {
                    node.setSpotLocation(
                            JGoObject.TopLeft,
                            prevNode,
                            JGoObject.BottomLeft);
                    node.setLocation(node.getLeft(), node.getTop() + 1);
                    node.setVisible(true);
                } else {
                    node.setSpotLocation(
                            JGoObject.TopLeft,
                            prevNode,
                            JGoObject.TopLeft);
                    node.setVisible(false);
                }
                
                if (node instanceof BasicCanvasFieldNode) {
                    BasicCanvasFieldNode fieldNode = (BasicCanvasFieldNode) node;
                    JGoPort port = (JGoPort) fieldNode.getConnectPointObject();
                    port.setSpotLocation(JGoObject.TopLeft, node, JGoObject.TopLeft);
                }
                
                prevNode = node;
            }
        }
    }
    
    /**
     * Invokes when the size of this area change.
     */
    public void changeAreaSize() {
        
        if (isExpanded()) {
            setBoundingRect(getLeft(), getTop(),
                    mFixedWidth,
                    mFixedHeight * (getModifiableNodeList().size() + 1));
        } else {
            setBoundingRect(getLeft(), getTop(),
                    mFixedWidth, mFixedHeight);
        }
        
        Rectangle rect = computeBoundingRectWithoutPorts();
        mBorderRect.setBoundingRect(rect.x, rect.y, rect.width, rect.height);
    }
    
    // A version of computeBoundingRect that does not include the ports.
    // This is useful for when we want to obtain a bounding rect that
    // represents only the visible methoid itself - i.e. this is used
    // when creating the selection rect for a methoid.
    public Rectangle computeBoundingRectWithoutPorts() {
        Rectangle rect = null;
        JGoListPosition pos = getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = getObjectAtPos(pos);
            pos = getNextObjectPos(pos);
            
            if (obj instanceof AccessibleArea) {
                if (((AccessibleArea) obj).isBoundingRectInvalid()) {
                    continue;
                }
                if (rect == null) {
                    Rectangle b = obj.getBoundingRect();
                    rect = new Rectangle(b.x, b.y, b.width, b.height);
                } else {
                    // add the object's bounding rect to this one
                    rect.add(obj.getBoundingRect());
                }
            }
        }
        return rect;
    }
    
    /**
     * Ensure the children are within this canvas node and the canvas node is
     * big enough to holds all the children.
     */
    public void ensureBounding() {
        List nodeList = this.getModifiableNodeList();
        int i = 0;
        
        int fixedWidth = mTitleBar.getWidth();
        int fixedHeight = mTitleBar.getHeight();
        
        JGoObject node = null;
        
        synchronized (nodeList) {
            for (; i < nodeList.size(); i++) {
                node = (JGoObject) nodeList.get(i);
                
                fixedWidth = Math.max(fixedWidth, node.getWidth());
                fixedHeight = Math.max(fixedHeight, node.getHeight());
            }
        }
        boolean isFixedSizeChanged = fixedWidth != mFixedWidth || fixedHeight != mFixedHeight;
        if (isFixedSizeChanged) {
            mFixedWidth = fixedWidth;
            mFixedHeight = fixedHeight;
            mTitleBar.setSize(mFixedWidth, mFixedHeight);
            
            for (i = 0; i < nodeList.size(); i++) {
                node = (JGoObject) nodeList.get(i);
                node.setSize(mFixedWidth, mFixedHeight);
            }
            
            layoutChildren();
            changeAreaSize();
        }
    }
    
    /**
     * Description of the Method
     *
     * @param node  Description of the Parameter
     */
    private void configNewChild(ICanvasNode node) {
        if (node instanceof JGoObject) {
            
            JGoObject jgoNode = (JGoObject) node;
            jgoNode.setSelectable(false);
            jgoNode.setDraggable(false);
            jgoNode.setResizable(false);
            jgoNode.setVisible(false);
            
            if (jgoNode instanceof JGoArea) {
                ((JGoArea) jgoNode).setSelectable(false);
            }
            
            this.addObjectAtTail(jgoNode);
            
            JGoPort port = null;
            if (node instanceof ICanvasFieldNode) {
                ICanvasFieldNode canvasFieldNode = (ICanvasFieldNode) node;
                canvasFieldNode.setContainer(this);
                port = (JGoPort) canvasFieldNode.getConnectPointObject();
                port.setVisible(false);
                bringObjectToFront(port);
            }
            
            if (jgoNode.getWidth() > mFixedWidth || jgoNode.getHeight() > mFixedHeight) {
                ensureBounding();
            } else {
                jgoNode.setBoundingRect(mTitleBar.getBoundingRect());
                layoutChildren();
                changeAreaSize();
            }
        }
    }
    
    /**
     * Provides listener listens on the node change on the methoid node that
     * this class repersents.
     *
     * @author    sleong
     * @created   January 27, 2003
     */
    private class MethoidPropertyListener
            implements PropertyChangeListener {
        /**
         * Add or remove the canvas node coorsponding to the new / remove node
         * of the methoid node that this canvas node repersents.
         *
         * @param e  the PropertyChangeEvent value
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IMapperGroupNode.NODE_INSERTED)) {
                int i = 0;
                for (IMapperNode findNode = getMethoidNode().getFirstNode();
                findNode != null;
                findNode = getMethoidNode().getNextNode(findNode)) {
                    
                    if (findNode == e.getNewValue()) {
                        ICanvasFieldNode newChild = ((BasicCanvasView)
                        BasicCanvasMethoidNode.this.getCanvas())
                        .getCanvasObjectFactory().createFieldNode(
                                (IFieldNode) e.getNewValue());
                        insertNode(newChild, i);
                        break;
                    }
                    i++;
                }
            } else if (e.getPropertyName().equals(IMapperGroupNode.NODE_REMOVED)) {
                List nodeList = getModifiableNodeList();
                synchronized (nodeList) {
                    for (int i = 0; i < nodeList.size(); i++) {
                        ICanvasFieldNode node = (ICanvasFieldNode) nodeList.get(i);
                        if (node.getFieldNode() == e.getOldValue()) {
                            removeNode(node);
                            break;
                        }
                    }
                }
            } else if (e.getPropertyName().equals(IMethoidNode.NAME_CHANGED)) {
                BasicCanvasMethoidNode.this.setName(mMethoidNode.getMethoidName());
            } else if (e.getPropertyName().equals(IMethoidNode.TOOLTIP_CHANGED)) {
                
            } else if (e.getPropertyName().equals(IMethoidNode.ICON_CHANGED)) {
                setIcon(mMethoidNode.getIcon());
            } else if (e.getPropertyName().equals(IMethoidNode.METHOID_OBJECT_CHANGED)) {
                setName(mMethoidNode.getMethoidName());
                setIcon(mMethoidNode.getIcon());
                setTitle(mMethoidNode.getMethoidName());
            }
        }
    }
    
    /**
     * The methoid node itself does not have a tooltip per se.
     * We instead look through our field nodes to determine if
     * the field node at the given point has a tooltip.
     */
    public String getToolTipText(Point p) {
        JGoObject fieldNode = (JGoObject) getFieldNodeByPoint(p);
        if (fieldNode != null) {
            return fieldNode.getToolTipText();
        } else if (mTitleBar.getBoundingRect().contains(p)) {
            return getMethoidNode().getToolTipText();
        }
        return null;
    }
    
    public void setTitle(String title) {
        mTitleBar.setTitle(title);
        ensureBounding();
    }
    
    public String getTitle() {
        return mTitleBar.getTitle();
    }
    
    public void setIcon(Icon icon) {
        mTitleBar.setTitleIcon(((ImageIcon)icon).getImage());
        ensureBounding();
    }
    
    public void setTitleBarColor(Color color) {
        mTitleBar.setTitleBarBackground(color);
    }
    
    public void resetTitleBarColor() {
        mTitleBar.resetTitleBarBackground();
    }
    
    public Color getTitleBarColor() {
        return mTitleBar.getTitleBarBackground();
    }
    
    protected void gainedSelection(JGoSelection jgoselection) {
        super.gainedSelection(jgoselection);
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            new NetworkHighlightTraverser(true).visit(this);
        }
    }
    
    protected void lostSelection(JGoSelection jgoselection) {
        super.lostSelection(jgoselection);
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            new NetworkHighlightTraverser(false).visit(this);
        }
    }
}

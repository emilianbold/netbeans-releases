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
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import java.awt.Point;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.DrawPort;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;

/**
 * <p>
 *
 * Title: </p> BasicCanvasFieldNode <p>
 *
 * Description: </p> BasicCanvasFieldNode provides an implemenation of canvas
 * field node. The port circles are explictly handled by this class and not
 * added to this JGoArea due to its bounding exceeds this bounding area. They
 * are added and removed directly to the document. <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public class BasicCanvasFieldNode
        extends AbstractCanvasFieldNode {
    
    private static final Color  TEXT_COLOR_RESULT     = Color.WHITE;
    private static final Color  TEXT_COLOR_INPUT      = Color.BLACK;
    private static final Color  TEXT_COLOR_LITERAL    = new Color( 30,  70, 230); // navy
    
    private static final JGoPen BORDER_INPUT_REGULAR  =
            JGoPen.makeStockPen(new Color(186, 205, 240)); // baby blue
    private static final JGoPen BORDER_OUTPUT_REGULAR =
            JGoPen.makeStockPen(new Color(116, 140, 192)); // purple
    private static final JGoPen BORDER_HOVER =
            JGoPen.make(JGoPen.SOLID, 1, new Color(0, 24, 127));
    
    private static final JGoBrush BRUSH_LITERAL    =
            JGoBrush.makeStockBrush(Color.WHITE);
    private static final JGoBrush BRUSH_REGULAR    =
            JGoBrush.makeStockBrush(new Color(255, 255, 255, 0)); // transparent
    private static final JGoBrush BRUSH_RESULT     =
            JGoBrush.makeStockBrush(new Color(116, 140, 192));    // purple
    
    
    private DrawPort mInPort;
    private DrawPort mOutPort;
    private BasicCanvasPort mPort;
    private String mCurrentTextStyle = "";
    private boolean mIsResultField;
    private boolean mIsLiteral;
    
    
    /**
     * Creates a new BasicCanvasFieldNode object.
     *
     * @param node  the field node
     */
    public BasicCanvasFieldNode(IFieldNode node) {
        super(node);
        node.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String propName = e.getPropertyName();
                if (IFieldNode.NAME_CHANGED.equals(propName)) {
                    updateLabel((String) e.getNewValue());
                } else if (IFieldNode.FIELD_OBJECT_CHANGED.equals(propName)) {
                    updateLabel(mFieldNode.getName());
                } else if (IFieldNode.STYLE_CHANGED.equals(propName)) {
                    updateLabelStyle((String) e.getNewValue());
                } else if (IMapperNode.LINK_ADDED.equals(propName)) {
                    setPortStyleVacant(false);
                } else if (IMapperNode.LINK_REMOVED.equals(propName)) {
                    setPortStyleVacant(true);
                }
            }
        });
        mPort = new BasicCanvasPort();
        mPort.setStyle(JGoPort.StyleObject);
        mPort.setValidDestination(false);
        mPort.setValidSource(false);
        mPort.setSelectable(false);
        mPort.setDraggable(true);
        
        mIsResultField = node.isOutput();
        IMapperGroupNode groupNode = node.getGroupNode();
        if (groupNode instanceof IMethoidNode) {
            IMethoidNode methoidNode = (IMethoidNode) groupNode;
            if (methoidNode.getMethoidObject() instanceof IMethoid) {
                IMethoid methoid = (IMethoid) methoidNode.getMethoidObject();
                if (methoid.isLiteral()) {
                    mIsResultField = false;
                    mIsLiteral = true;
                }
            }
        }
        
        if (node.isInput()) {
            mInPort = makePort();
            mPort.setValidDestination(true);
        }
        
        if (node.isOutput()) {
            mOutPort = makePort();
            mPort.setValidSource(true);
        }
        
        mPort.setPortObject(getDrawPort());
        layoutPorts();
        
        setHorizonalAligment(LEFT);
        
        if (mIsResultField) {
            setBrush(BRUSH_RESULT);
        } else if (mIsLiteral) {
            setBrush(BRUSH_LITERAL);
        } else {
            setBrush(BRUSH_REGULAR);
        }
        
        mCurrentTextStyle = IFieldNode.STYLE_TYPE_NORMAL;
        setTextColorFromStyle(mCurrentTextStyle);
    }
    
    private DrawPort makePort() {
        DrawPort ob = new DrawPort(this);
        ob.setDraggable(false);
        ob.setSelectable(false);
        ob.setResizable(false);
        return ob;
    }
    
    /**
     * Return the connection point object of this node.
     *
     * @return   the connection point object of this node.
     */
    public Object getConnectPointObject() {
        return mPort;
    }
    
    public DrawPort getDrawPort() {
        if (mInPort != null) {
            return mInPort;
        } else if (mOutPort != null) {
            return mOutPort;
        }
        return null;
    }
    
    /**
     * Set the mapper canvas that displays this node.
     *
     * @param canvas  the canvas displays this node.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        super.setMapperCanvas(canvas);
        
        if (mInPort != null) {
            // white top/bottom border around each input field
            setPen(BORDER_INPUT_REGULAR);
        }
        
        if (mOutPort != null) {
            setPen(BORDER_OUTPUT_REGULAR);
        }
    }
    
    public void setIsHovering(boolean isHovering) {
        if (mInPort != null) {
            // input ports show special hover animation: dark blue rectangle
            setPen(isHovering ? BORDER_HOVER : BORDER_INPUT_REGULAR);
        }
    }
    
    /**
     * Defines the location of the drawing region (port object) of the
     * DrawPort as well as the bounding rect of the JGoPort itself.
     */
    public void layoutPorts() {
        Point point = null;
        if (mOutPort != null) {
            point = getSpotLocation(JGoObject.RightCenter);
            Dimension drawSize = mPort.getDrawSize();
            int offset = 3;
            mOutPort.setDrawLocation(
                    point.x + offset, 
                    point.y - drawSize.height / 2);
            mPort.setBoundingRect(
                    getLeft(),
                    getTop(),
                    getWidth() + drawSize.width + offset,
                    getHeight());
        }
        
        if (mInPort != null) {
            point = getSpotLocation(JGoObject.LeftCenter);
            Dimension drawSize = mPort.getDrawSize();
            int offset = drawSize.width + 2;
            mInPort.setDrawLocation(
                    point.x - offset, 
                    point.y - drawSize.height / 2);
            mPort.setBoundingRect(
                    getLeft() - offset,
                    getTop(),
                    getWidth() + offset,
                    getHeight());
        }
    }
    
    /**
     * Sets the visible attribute of the BasicCanvasFieldNode object
     *
     * @param visible  The new visible value
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        mPort.setVisible(visible);
    }
    
    private void updateLabelStyle(String newStyle) {
        if (mCurrentTextStyle.equals(newStyle)) {
            return;
        }
        if (
                !IFieldNode.STYLE_TYPE_NORMAL.equals(newStyle) &&
                !IFieldNode.STYLE_TYPE_LITERAL.equals(newStyle)) {
            throw new IllegalArgumentException("Style " + newStyle + " not supported.");
        }
        mCurrentTextStyle = newStyle;
        setTextColorFromStyle(newStyle);
        setPortStyleVacant(IFieldNode.STYLE_TYPE_NORMAL.equals(newStyle));
        ensureContainerSize();
    }
    
    /**
     * update the text of this node.
     *
     * @param text  update the text of this node.
     */
    public void updateLabel(String text) {
        super.updateLabel(text);
        
        ensureContainerSize();
    }
    
    private void ensureContainerSize() {
        // ensure methoid canvas node size is updated
        ICanvasGroupNode container = getContainer();
        if (container != null) {
            if (container instanceof BasicCanvasMethoidNode) {
                ((BasicCanvasMethoidNode) container).ensureBounding();
            }
        }
    }
    
    public void setHighlight(boolean isHighlighted) {
        DrawPort port = getDrawPort();
        if (port != null) {
            port.setIsActivated(isHighlighted);
        }
    }
    
    private void setTextColorFromStyle(String newStyle) {
        if        (mIsResultField) {
            setTextColor(TEXT_COLOR_RESULT);
        } else if (newStyle.equals(IFieldNode.STYLE_TYPE_NORMAL)) {
            setTextColor(TEXT_COLOR_INPUT);
        } else if (newStyle.equals(IFieldNode.STYLE_TYPE_LITERAL)) {
            setTextColor(TEXT_COLOR_LITERAL);
        }
    }
    
    private void setPortStyleVacant(boolean isVacant) {
        DrawPort port = getDrawPort();
        if (port != null) {
            port.setIsConnected(!isVacant);
        }
    }
}

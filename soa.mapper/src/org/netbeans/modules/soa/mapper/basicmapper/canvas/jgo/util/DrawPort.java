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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util;

import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;

/**
 *
 * @author jsandusky
 */
public class DrawPort extends JGoRectangle {
    
    public static final Color COLOR_NORMAL    = new Color(0, 24, 127);   // navy
    public static final Color COLOR_ACTIVATED = ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR;
    public static final Dimension SIZE_NORMAL = new Dimension(8, 8);
    public static final Dimension SIZE_HOVER  = new Dimension(10, 10);
    
    private BasicCanvasFieldNode mFieldNode;
    private boolean mIsHovering;
    private boolean mIsActivated;
    private boolean mIsConnected;
    private int mSideLength;
    private Dimension mDrawSize;
    private Point mDrawLocation = new Point(0, 0);
    
    
    public DrawPort(BasicCanvasFieldNode fieldNode) {
        mFieldNode = fieldNode;
        setIsHovering(false);
    }
    
    
    public BasicCanvasFieldNode getFieldNode() {
        return mFieldNode;
    }
    
    public void setIsHovering(boolean isHovering) {
        mIsHovering = isHovering;
        mDrawSize = isHovering ? SIZE_HOVER : SIZE_NORMAL;
        mSideLength = mDrawSize.width;
    }
    
    public boolean isHovering() {
        return mIsHovering;
    }
    
    public void setIsActivated(boolean isActivated) {
        mIsActivated = isActivated;
    }
    
    public boolean isActivated() {
        return mIsActivated;
    }
    
    public void setIsConnected(boolean isConnected) {
        mIsConnected = isConnected;
    }
    
    public boolean isConnected() {
        return mIsConnected;
    }
    
    public void setDrawLocation(int x, int y) {
        mDrawLocation = new Point(x, y);
    }
    
    public Dimension getDrawSize() {
        return mDrawSize;
    }
    
    protected static void setAntiAliasing(Graphics2D graphics2d, boolean isEnable) {
        graphics2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                isEnable ?
                    RenderingHints.VALUE_ANTIALIAS_ON :
                    RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    protected static void drawDashedSquare(
            Graphics2D graphics2d, 
            Point loc, 
            int sideLength, 
            int dash)
    {
        int solid = (sideLength - dash) / 2;
        int end1 = solid - 1;
        int end2 = sideLength - solid;
        int lastX = loc.x + (sideLength - 1);
        int lastY = loc.y + (sideLength - 1);
        
        // horizontal lines
        graphics2d.drawLine(loc.x, loc.y, loc.x + end1, loc.y);
        graphics2d.drawLine(loc.x + end2, loc.y, lastX, loc.y);
        graphics2d.drawLine(loc.x, lastY, loc.x + end1, lastY);
        graphics2d.drawLine(loc.x + end2, lastY, lastX, lastY);
        
        // vertical lines
        graphics2d.drawLine(loc.x, loc.y, loc.x, loc.y + end1);
        graphics2d.drawLine(loc.x, loc.y + end2, loc.x, lastY);
        graphics2d.drawLine(lastX, loc.y, lastX, loc.y + end1);
        graphics2d.drawLine(lastX, loc.y + end2, lastX, lastY);
    }
    
    
    public void paint(Graphics2D graphics2d, JGoView jgoview) {
        
        Point loc = mDrawLocation;
        graphics2d.setColor(mIsActivated ? COLOR_ACTIVATED : COLOR_NORMAL);
        
        if (mIsConnected) {
            // Connected.
            setAntiAliasing(graphics2d, false);
            if (mIsHovering) {
                graphics2d.fillRect(loc.x + 3, loc.y + 3, 4, 4);
                drawDashedSquare(graphics2d, loc, mSideLength, 4);
            } else {
                graphics2d.fillRect(loc.x + 2, loc.y + 2, 4, 4);
                drawDashedSquare(graphics2d, loc, mSideLength, 2);
            }
            setAntiAliasing(graphics2d, true);
        } else {
            // Disconnected.
            if (mIsHovering) {
                setAntiAliasing(graphics2d, false);
                drawDashedSquare(graphics2d, loc, mSideLength, 4);
                setAntiAliasing(graphics2d, true);
            } else {
                // normal disconnected ports are not visible
            }
        }
    }
}

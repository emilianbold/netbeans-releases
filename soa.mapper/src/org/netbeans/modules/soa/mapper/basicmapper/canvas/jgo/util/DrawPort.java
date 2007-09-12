/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

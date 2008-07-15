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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 */package org.netbeans.modules.mobility.svgcore.composer;

import com.sun.perseus.j2d.Transform;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Pavel Benes
 */
public final class SVGObjectOutline {
    private static final int   ROUND_CORNER_SIZE   = 3;
    private static final int   DIAMOND_CORNER_SIZE = 3;
    public static final int    SELECTOR_OVERLAP    = ROUND_CORNER_SIZE + 2;
    
    private static final float HANDLE_DIST         = 30;
    public static final Color SELECTOR_BODY       = Color.RED;
    private static final Color SELECTOR_OUTLINE    = Color.WHITE;
    private static final Color COLOR_HIGHLIGHT     = new Color( 0,0,0, 64);
    private static final int   RESIZE_NW_CORNER_INDEX = 0;
    private static final int   ROTATE_CORNER_INDEX = 1;
    private static final int   RESIZE_SE_CORNER_INDEX = 2;
    private static final int   SKEW_CORNER_INDEX   = 3;
    
    private final SVGObject m_svgObject;
    private final float[][] m_coords = new float[4][2];
    private       short     m_tickerCopy;
        
    public SVGObjectOutline(SVGObject svgObject) {
        assert svgObject != null : "The SVGObject reference cannot be null";
        m_svgObject  = svgObject;
        setDirty();
    }
    
    public float[][] getCoords() {
        checkObject();
        return m_coords;
    }
    
    public void setDirty() {
        m_tickerCopy = -1;
    }
    
    public static void drawOutline( Graphics g, int xOff, int yOff, float [][] coords) {
        for (int i = 0; i < 4; i++) {
            int j = (i+1) % 4;
            g.drawLine(Math.round(coords[i][0]) + xOff, Math.round(coords[i][1]) + yOff,
                       Math.round(coords[j][0]) + xOff, Math.round(coords[j][1]) + yOff);
        }
    }
    
    public void draw(Graphics g, int xOff, int yOff, Color color, boolean drawCorners) {
        checkObject();

        g.setColor(color);        

        drawOutline( g, xOff, yOff, m_coords);

        if (drawCorners) {
            for (int i = 0; i < 4; i++) {
                if ( i == ROTATE_CORNER_INDEX) {
                    GraphicUtils.drawRoundSelectorCorner(g, SELECTOR_OUTLINE, SELECTOR_BODY,
                            (int)m_coords[i][0] + xOff,(int)m_coords[i][1] + yOff,
                            ROUND_CORNER_SIZE);
                } else if ( i == SKEW_CORNER_INDEX) {
                    GraphicUtils.drawDiamondSelectorCorner(g, SELECTOR_OUTLINE, SELECTOR_BODY,
                            (int)m_coords[i][0] + xOff,(int)m_coords[i][1] + yOff,
                            DIAMOND_CORNER_SIZE);
                } else {
                    drawRectSelectorCorner(g, (int)m_coords[i][0] + xOff,(int)m_coords[i][1] + yOff);
                }
            }
        }
    }

    public void highlight(Graphics g, int xOff, int yOff, Color color) {
        checkObject();
        
        g.setColor(color);
        int [] xPoints = new int[4];
        int [] yPoints = new int[4];
        
        for (int i = 0; i < 4; i++) {
            xPoints[i] = Math.round(xOff + m_coords[i][0]);
            yPoints[i] = Math.round(yOff + m_coords[i][1]);
        }
        g.fillPolygon( xPoints, yPoints, 4);        
    }
    
    public void highlight(Graphics g, int xOff, int yOff) {
        highlight(g, xOff, yOff, COLOR_HIGHLIGHT);
    }
    
    public static Rectangle getShapeBoundingBox(float [][] points) {
        float min_x, max_x, min_y, max_y;
        min_x = max_x = points[0][0]; 
        min_y = max_y = points[0][1];
        
        for (int i = 1; i < points.length; i++) {
            float x = points[i][0];
            float y = points[i][1];
            
            if ( x < min_x) {
                min_x = x;
            } else if ( x > max_x) {
                max_x = x;
            }
            if ( y < min_y) {
                min_y = y;
            } else if ( y > max_y) {
                max_y = y;
            }
        }
        return new Rectangle( Math.round(min_x), Math.round(min_y),
                              Math.round(max_x - min_x), Math.round(max_y - min_y));
    }
    
    public Rectangle getScreenBoundingBox() {
        checkObject();
        return getShapeBoundingBox(m_coords);
    }

    public float[] getRotatePivotPoint() {
        return getCenter();
    }
    
    public float[] getScalePivotPoint() {
        return getCenter();
    }
    
    public float [] getCenter() {
        checkObject();
        float sx = 0, sy = 0;
        
        for (int i = 0; i < m_coords.length; i++) {
            sx += m_coords[i][0];
            sy += m_coords[i][1];
        }
        return new float[] { sx/4, sy/4 };
    }
    
    public boolean isAtRotateHandlePoint(float x, float y) {
        checkObject();
        float [] pt = m_coords[ROTATE_CORNER_INDEX];

        if (GraphicUtils.areNear(x, y, pt[0], pt[1], HANDLE_DIST)) {
            return true;
        }
        return false;
    }
    
    /*
    public boolean isAtScaleHandlePoint(float x, float y) {
        checkObject();
        for (int i = 0; i < 4; i++) {
            if ( i != ROTATE_CORNER_INDEX && i != SKEW_CORNER_INDEX) {
                if (GraphicUtils.areNear(x, y, m_coords[i][0], m_coords[i][1],HANDLE_DIST)) {
                    return true;
                }
            }
        }
        
        return false;        
    }
     */

    public boolean isAtScaleNWHandlePoint(float x, float y) {
        checkObject();
        float [] pt = m_coords[RESIZE_NW_CORNER_INDEX];
        if (GraphicUtils.areNear(x, y, pt[0], pt[1], HANDLE_DIST)) {
            return true;
        }
        return false;        
    }

    public boolean isAtScaleSEHandlePoint(float x, float y) {
        checkObject();
        float [] pt = m_coords[RESIZE_SE_CORNER_INDEX];
        if (GraphicUtils.areNear(x, y, pt[0], pt[1], HANDLE_DIST)) {
            return true;
        }
        return false;        
    }

    /*
    public boolean isAtScaleXHandlePoint(float x, float y) {
        checkObject();
        if (GraphicUtils.isNearLine(x, y, m_coords[1][0], m_coords[1][1], m_coords[2][0], m_coords[2][1], HANDLE_DIST)
                || GraphicUtils.isNearLine(x, y, m_coords[3][0], m_coords[3][1], m_coords[0][0], m_coords[0][1], HANDLE_DIST))
        {
            return true;
        }
        
        return false;        
    }
    
    public boolean isAtScaleYHandlePoint(float x, float y) {
        checkObject();
        if (GraphicUtils.isNearLine(x, y, m_coords[0][0], m_coords[0][1], m_coords[1][0], m_coords[1][1], HANDLE_DIST)
                || GraphicUtils.isNearLine(x, y, m_coords[2][0], m_coords[2][1], m_coords[3][0], m_coords[3][1], HANDLE_DIST))
        {
            return true;
        }
        
        return false;        
    }
     */
    
    public boolean isAtScaleEHandlePoint(float x, float y) {
        checkObject();
        if (GraphicUtils.isNearLine(x, y, 
                m_coords[1][0], m_coords[1][1], 
                m_coords[2][0], m_coords[2][1], HANDLE_DIST))
        {
            return true;
        }
        
        return false;        
    }
    
    public boolean isAtScaleWHandlePoint(float x, float y) {
        checkObject();
        if (GraphicUtils.isNearLine(x, y, 
                m_coords[3][0], m_coords[3][1], 
                m_coords[0][0], m_coords[0][1], HANDLE_DIST))
        {
            return true;
        }
        
        return false;        
    }
    
    public boolean isAtScaleNHandlePoint(float x, float y) {
        checkObject();
        if (GraphicUtils.isNearLine(x, y, 
                m_coords[0][0], m_coords[0][1], 
                m_coords[1][0], m_coords[1][1], HANDLE_DIST))
        {
            return true;
        }
        
        return false;        
    }

    public boolean isAtScaleSHandlePoint(float x, float y) {
        checkObject();
        if (GraphicUtils.isNearLine(x, y, 
                m_coords[2][0], m_coords[2][1], 
                m_coords[3][0], m_coords[3][1], HANDLE_DIST))
        {
            return true;
        }
        
        return false;        
    }
    
    public boolean isAtSkewHandlePoint(float x, float y) {
        checkObject();
        float [] pt = m_coords[SKEW_CORNER_INDEX];

        if (GraphicUtils.areNear(x, y, pt[0], pt[1], HANDLE_DIST)) {
            return true;
        }
        
        return false;        
    }
    
    private static void drawRectSelectorCorner(Graphics g, int x, int y) {
        g.setColor(SELECTOR_BODY);
        g.fillRect(x-2, y-2, 5, 5);
        g.setColor(SELECTOR_OUTLINE);
        g.drawRect(x-3, y-3, 6, 6);
    }
    
    private synchronized void checkObject() {
        assert m_svgObject != null : "SVGObject reference cannot be null";
        short ticker = m_svgObject.getScreenManager().getChangeTicker();
        
        if (ticker != m_tickerCopy) {
            transformRectangle(m_svgObject.getSafeBBox(),
                    (Transform) m_svgObject.getSVGElement().getScreenCTM(), m_coords);
            m_tickerCopy = ticker;
        }        
    }
    
    public static float [][] transformRectangle( SVGRect rect, Transform txf, float [][] coords) {
        float x = rect.getX(),
              y = rect.getY(),
              w = rect.getWidth(),
              h = rect.getHeight();

        float [][] points = new float[][] {
            {x, y}, {x+w, y}, {x+w, y+h}, {x, y+h}
        };
        assert coords.length == 4;
        for (int i = 0; i < 4; i++) {
            txf.transformPoint(points[i], coords[i]);
        }
        return coords;
    }    
}

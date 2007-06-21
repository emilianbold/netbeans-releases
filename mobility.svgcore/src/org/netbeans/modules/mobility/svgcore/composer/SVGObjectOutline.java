/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */
package org.netbeans.modules.mobility.svgcore.composer;

import com.sun.perseus.j2d.Transform;
import java.awt.Color;
import java.awt.Graphics;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Pavel Benes
 */
public class SVGObjectOutline {
    private static final int   ROUND_CORNER_SIZE   = 3;
    public static final int    SELECTOR_OVERLAP    = ROUND_CORNER_SIZE + 2;
    
    private static final float HANDLE_DIST         = 30;
    private static final Color SELECTOR_BODY       = Color.RED;
    private static final Color SELECTOR_OUTLINE    = Color.WHITE;
    private static final Color COLOR_HIGHLIGHT     = new Color( 0,0,0, 64);
    private static final int   ROTATE_CORNER_INDEX = 1;
    
    private final SVGObject m_svgObject;
    private final float[][] m_coords = new float[4][2];
    private final SVGRect   m_screenBBox;
    private       short     m_tickerCopy;
        
    public SVGObjectOutline(SVGObject svgObject) {
        assert svgObject != null : "The SVGObject reference cannot be null";
        m_svgObject  = svgObject;
        m_screenBBox = m_svgObject.getInitialScreenBBox();
        setDirty();
    }
    
    public float[][] getCoords() {
        checkObject();
        return m_coords;
    }
    
    public void setDirty() {
        m_tickerCopy = -1;
    }
    
    public void draw(Graphics g, int xOff, int yOff) {
        checkObject();
        
        g.setColor(SELECTOR_BODY);
        
        for (int i = 0; i < 4; i++) {
            int j = (i+1) % 4;
            g.drawLine((int)m_coords[i][0] + xOff, (int)m_coords[i][1] + yOff,
                       (int)m_coords[j][0] + xOff, (int)m_coords[j][1] + yOff);
        }

        for (int i = 0; i < 4; i++) {
            if ( i != ROTATE_CORNER_INDEX) {
                drawRectSelectorCorner(g, (int)m_coords[i][0] + xOff,(int)m_coords[i][1] + yOff);
            } else {
                GraphicUtils.drawRoundSelectorCorner(g, SELECTOR_OUTLINE, SELECTOR_BODY,
                        (int)m_coords[i][0] + xOff,(int)m_coords[i][1] + yOff,
                        ROUND_CORNER_SIZE);
            }
        }
    }
    
    public void highlight(Graphics g, int xOff, int yOff) {
        checkObject();
        
        g.setColor(COLOR_HIGHLIGHT);
        int [] xPoints = new int[4];
        int [] yPoints = new int[4];
        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) (xOff + m_coords[i][0]);
            yPoints[i] = (int) (yOff + m_coords[i][1]);
        }
        g.fillPolygon( xPoints, yPoints, 4);        
    }

    public float[] getRotatePivotPoint() {
        return getCenter();
    }
    
    public float[] getScalePivotPoint() {
        return getCenter();
    }
    
    public float [] getCenter() {
        checkObject();
        float zoomRatio = getZoomRatio();
        
        float [] pt = new float[2];
        pt[0] = zoomRatio * (m_svgObject.getCurrentTranslateX() + m_screenBBox.getX() + m_screenBBox.getWidth() / 2);
        pt[1] = zoomRatio * (m_svgObject.getCurrentTranslateY() + m_screenBBox.getY() + m_screenBBox.getHeight() / 2);
        System.out.println("Center: {" + pt[0] + "," + pt[1] + "}");
        return pt;
    }
    
    public boolean isAtRotateHandlePoint(float x, float y) {
        checkObject();
        float [] pt = m_coords[ROTATE_CORNER_INDEX];

        if (GraphicUtils.areNear(x, y, pt[0], pt[1], HANDLE_DIST)) {
            return true;
        }
        return false;
    }
    
    public boolean isAtScaleHandlePoint(float x, float y) {
        checkObject();
        for (int i = 0; i < 4; i++) {
            if ( i != ROTATE_CORNER_INDEX) {
                if (GraphicUtils.areNear(x, y, m_coords[i][0], m_coords[i][1],HANDLE_DIST)) {
                    return true;
                }
            }
        }
        
        return false;        
    }
    
    private static void drawRectSelectorCorner(Graphics g, int x, int y) {
        g.setColor(SELECTOR_BODY);
        g.fillRect(x-2, y-2, 5, 5);
        g.setColor(SELECTOR_OUTLINE);
        g.drawRect(x-3, y-3, 6, 6);
    }
    
    private float getZoomRatio() {
        return m_svgObject.getSceneManager().getScreenManager().getZoomRatio();
    }
    
    private synchronized void checkObject() {
        assert m_svgObject != null : "SVGObject reference cannot be null";
        short ticker = m_svgObject.getScreenManager().getChangeTicker();
        
        if (ticker != m_tickerCopy) {
            float zoomRatio = getZoomRatio();
            //TODO use actual rotate pivot
            
            SVGRect bBox = GraphicUtils.scale(m_screenBBox, zoomRatio);
            float px = bBox.getX() + bBox.getWidth()  / 2;
            float py = bBox.getY() + bBox.getHeight() / 2;

            float scale = m_svgObject.getCurrentScale();
            Transform txf = new Transform( scale, 0, 0, scale, 0, 0);
            txf.mRotate(m_svgObject.getCurrentRotate());

            float [] point = new float[2];

            point[0] = (bBox.getX() - px);
            point[1] = (bBox.getY() - py);
            txf.transformPoint(point, m_coords[0]);

            point[0] += bBox.getWidth();
            txf.transformPoint(point, m_coords[1]);

            point[1] += bBox.getHeight();
            txf.transformPoint(point, m_coords[2]);

            point[0] -= bBox.getWidth();
            txf.transformPoint(point, m_coords[3]);

            px = (px + m_svgObject.getCurrentTranslateX() * zoomRatio) ;
            py = (py + m_svgObject.getCurrentTranslateY() * zoomRatio) ;

            for (int i = 0; i < 4; i++) {
                m_coords[i][0] += px;
                m_coords[i][1] += py;
            }          
            bBox = m_svgObject.getSVGScreenBBox();

            m_tickerCopy = ticker;
        }        
    }    
}

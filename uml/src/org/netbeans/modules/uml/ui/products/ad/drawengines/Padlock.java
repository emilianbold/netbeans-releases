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

package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.SwingConstants;

/**
 * Responsible for drawing a padlock.
 * It allows to specify location, size scale, rotation and mirroring by Y axis.
 * The transparency is 0.4 but can be changed in code. 
 *
 * @author nk160297
 */
public class Padlock {
    
    private double centerX  = 0d;
    private double centerY  = 0d;
    private double scale    = 1d;
    private double rotation = 0d;
    private boolean mirrorY = true;
    private int originalPointAnchor = SwingConstants.CENTER; 
    private Point2D originalPoint;
    
    
    //===================================================================
    
    public Padlock() {
    }

    public Padlock(double centerX, double centerY, double scale, double rotation) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.scale = scale;
        this.rotation = rotation;
    }

    //===================================================================
    
    public void setLocation(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public double getLocationX() {
        return this.centerX;
    }

    public double getLocationY() {
        return this.centerY;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getScale() {
        return this.scale;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return this.rotation;
    }

    public boolean getMirrorY() {
        return mirrorY;
    }
    
    public void setMirrorY(boolean mirrorY) {
        this.mirrorY = mirrorY;
    }
    
    /**
     * The padlock can has the special point which defines the zero coordinates. 
     * It's called the Original Point. By default it's located at the geometric center of the padlock.
     */
    public void setOriginalPoint(int newPoint) {
        if (newPoint == this.originalPointAnchor) return; // nothing to do
        //
        switch (newPoint) {
            case SwingConstants.CENTER: 
                originalPointAnchor = newPoint;
                originalPoint = null;
                break;
            case SwingConstants.NORTH: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(0f, -PadlockDrawEngin.totalHeightHalf);
                break;
            case SwingConstants.NORTH_EAST: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(PadlockDrawEngin.totalWidthHalf, -PadlockDrawEngin.totalHeightHalf);
                break;
            case SwingConstants.EAST: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(PadlockDrawEngin.totalWidthHalf, 0);
                break;
            case SwingConstants.SOUTH_EAST: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(PadlockDrawEngin.totalWidthHalf, PadlockDrawEngin.totalHeightHalf);
                break;
            case SwingConstants.SOUTH: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(0f, PadlockDrawEngin.totalHeightHalf);
                break;
            case SwingConstants.SOUTH_WEST: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(-PadlockDrawEngin.totalWidthHalf, PadlockDrawEngin.totalHeightHalf);
                break;
            case SwingConstants.WEST: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(-PadlockDrawEngin.totalWidthHalf, 0);
                break;
            case SwingConstants.NORTH_WEST: 
                originalPointAnchor = newPoint;
                originalPoint = new Point2D.Float(-PadlockDrawEngin.totalWidthHalf, -PadlockDrawEngin.totalHeightHalf);
                break;
            default: 
                throw new IllegalArgumentException();
        }
    }
    
    //===================================================================
    
    public void paint(Graphics2D g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.translate(centerX, centerY);
        g2.scale(scale, (mirrorY ? -scale : scale));
        g2.rotate(rotation);
        if (originalPointAnchor != SwingConstants.CENTER) {
            g2.translate((mirrorY ? -originalPoint.getX() : originalPoint.getX()), -originalPoint.getY());
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        PadlockDrawEngin.paint(g2);
    }

    //===================================================================
    
    /**
     * Performs drawing work. It's implied the standard Java2D coordinate system (Y axis from top to bottom). 
     */
    private static class PadlockDrawEngin {
        private static final float lockBodyHWidth  = 6.5f;
        private static final float lockBodyHeight  = 10f;
        private static final float keyHoleYShift   = 1f;
        private static final float keyHoleHeight   = 5f;
        private static final float keyHoleHWidth   = 1.3f; // half of width
        private static final float keyHoleRadius   = 1.5f;
        private static final float claspHeight     = 8f;
        private static final float claspHWidth     = 3f;  // half of width
        private static final float claspThickness  = 2f;
        //
        public static final float totalHeightHalf = (lockBodyHeight + claspHeight) / 2f;
        public static final float totalWidthHalf = lockBodyHWidth;
        //
        private static GeneralPath lockBodyWithHole = null;
        private static GeneralPath clasp = null;
        
        private static void initLockTemplate() {
            if (lockBodyWithHole == null || clasp == null) {
                synchronized(PadlockDrawEngin.class) {
                    //
                    if (lockBodyWithHole != null && clasp != null) return;
                    //
                    RoundRectangle2D.Float lockBody = new RoundRectangle2D.Float(
                            -lockBodyHWidth,
                            -totalHeightHalf + claspHeight,
                            lockBodyHWidth * 2f,
                            lockBodyHeight,
                            lockBodyHWidth / 2.5f,
                            lockBodyHeight / 5f);
                    //
                    // Round
                    Ellipse2D.Double keyHoleCenter = new Ellipse2D.Double(
                            -keyHoleRadius, keyHoleYShift,
                            keyHoleRadius * 2f, keyHoleRadius * 2f);
                    //
                    // Triangle
                    GeneralPath keyHole = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                    keyHole.moveTo(0f, keyHoleYShift);
                    keyHole.lineTo(-keyHoleHWidth, keyHoleHeight + keyHoleYShift);
                    keyHole.lineTo(keyHoleHWidth, keyHoleHeight + keyHoleYShift);
                    keyHole.lineTo(0f, keyHoleYShift);
                    //
                    Area keyHoleArea = new Area(keyHole);
                    keyHoleArea.add(new Area(keyHoleCenter));
                    //
                    lockBodyWithHole = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                    lockBodyWithHole.append(lockBody, false);
                    lockBodyWithHole.append(keyHoleArea, false);
                    //
                    clasp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                    {
                        float tempX1 = -claspHWidth;
                        float tempY1 = -totalHeightHalf + claspHeight - claspThickness / 2f;
                        clasp.moveTo(tempX1, tempY1);
                        float tempY2 = tempY1 - claspHeight * 0.5f;
                        clasp.lineTo(tempX1, tempY2);
                        float tempX2 = claspHWidth;
                        clasp.curveTo(
                                tempX1, (-totalHeightHalf),
                                tempX2, (-totalHeightHalf),
                                tempX2, tempY2);
                        clasp.lineTo((float)tempX2, (float)tempY1);
                    }
                }
            } 
        }
        
        public static void paint(Graphics2D g2) {
            //
            initLockTemplate();
            //
            // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //
            BasicStroke wideStroke = new BasicStroke((float)claspThickness);
            g2.setStroke(wideStroke);
            g2.setColor(Color.black);
            //
            g2.draw(clasp);
            g2.fill(lockBodyWithHole);
        }
    }

}

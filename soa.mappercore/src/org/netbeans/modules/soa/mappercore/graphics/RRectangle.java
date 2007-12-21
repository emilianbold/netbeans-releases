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

package org.netbeans.modules.soa.mappercore.graphics;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author anjeleevich
 */
public class RRectangle implements Shape {
    
    private double x;
    private double y;
    private double width;
    private double height;
    private double radiusTopLeft;
    private double radiusTopRight;
    private double radiusBottomLeft;
    private double radiusBottomRight;
    
    
    /** Creates a new instance of RoundRectangle */
    public RRectangle(double x, double y, double width, double height,
            double radiusTopLeft, 
            double radiusTopRight, 
            double radiusBottomLeft, 
            double radiusBottomRight) 
    {
        setBounds(x, y, width, height);
        setRadiuses(radiusTopLeft, radiusTopRight, radiusBottomLeft, 
                radiusBottomRight);
    }
    
    
    public void setBounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    
    public void setRadiuses(double radiusTopLeft, double radiusTopRight, 
            double radiusBottomLeft, double radiusBottomRight) 
    {
        this.radiusTopLeft = Math.max(0.0, radiusTopLeft);
        this.radiusTopRight = Math.max(0.0, radiusTopRight);
        this.radiusBottomLeft = Math.max(0.0, radiusBottomLeft);
        this.radiusBottomRight = Math.max(0.0, radiusBottomRight);
    }
    
    
    public double getMaxRadius() {
        return Math.min(width, height) / 2.0;
    }
    
    
    public double getRadiusTopLeft() {
        return Math.min(getMaxRadius(), radiusTopLeft);
    }


    public double getRadiusTopRight() {
        return Math.min(getMaxRadius(), radiusTopRight);
    }

    
    public double getRadiusBottomLeft() {
        return Math.min(getMaxRadius(), radiusBottomLeft);
    }


    public double getRadiusBottomRight() {
        return Math.min(getMaxRadius(), radiusBottomRight);
    }

    
    public Rectangle getBounds() {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        
        return new Rectangle(x0, y0, 
                (int) Math.ceil(x + width) - x0, 
                (int) Math.ceil(y + height) - y0);
    }
    

    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(x, y, width, height);
    }
    

    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    
    public boolean contains(double x, double y) {
        x -= this.x;
        y -= this.y;
        
        if (x < 0.0 || x > width || y < 0.0 || y > height) return false;
        
        double maxRadius = getMaxRadius();
        
        double radius = Math.min(maxRadius, radiusTopLeft);
        double px = x - radius;
        double py = y - radius;
        if (px < 0.0 && py < 0.0) return px * px + py * py < radius * radius;
        
        radius = Math.min(maxRadius, radiusTopRight);
        px = width - x - radius;
        py = y - radius;
        if (px < 0.0 && py < 0.0) return px * px + py * py < radius * radius;
        
        radius = Math.min(maxRadius, radiusBottomLeft);
        px = x - radius;
        py = height - y - radius;
        if (px < 0.0 && py < 0.0) return px * px + py * py < radius * radius;

        radius = Math.min(maxRadius, radiusBottomRight);
        px = width - x - radius;
        py = height - y - radius;
        if (px < 0.0 && py < 0.0) return px * px + py * py < radius * radius;
        
        return true;
    }
    
    
    public boolean intersects(double x, double y, double w, double h) {
        double x1 = x - this.x;
        double y1 = y - this.y;
        
        double x2 = x1 + w;
        double y2 = y1 + h;
        
        return !(x2 < 0.0 || x1 > width || y2 < 0.0 || y1 > height);
    }
    

    public boolean contains(double x, double y, double w, double h) {
        double x2 = x + w;
        double y2 = y + h;
        return contains(x, y) && contains(x2, y2) 
                && contains(x2, y) && contains(x, y2);
    }
    


    public PathIterator getPathIterator(AffineTransform at) {
        return new RRectanglePathIterator(x, y, width, height, radiusTopLeft, 
                radiusTopRight, radiusBottomLeft, radiusBottomRight, at);
    }

    
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
    
    
    private static class RRectanglePathIterator implements PathIterator {
        double[] coords;

        int index = 0;
        
        RRectanglePathIterator(double x1, double y1, double width, double height,
                double radiusTopLeft,
                double radiusTopRight,
                double radiusBottomLeft,
                double radiusBottomRight,
                AffineTransform transform)
        {
            double maxRadius = Math.min(width, height) / 2.0;
            
            radiusTopLeft = Math.min(maxRadius, radiusTopLeft);
            radiusTopRight = Math.min(maxRadius, radiusTopRight);
            radiusBottomLeft = Math.min(maxRadius, radiusBottomLeft);
            radiusBottomRight = Math.min(maxRadius, radiusBottomRight);
            
            double radiusTopLeft2 = radiusTopLeft * K2;
            double radiusTopRight2 = radiusTopRight * K2;
            double radiusBottomLeft2 = radiusBottomLeft * K2;
            double radiusBottomRight2 = radiusBottomRight * K2;
            
            double x2 = x1 + width;
            double y2 = y1 + height;
            
            coords = new double[] {
                    // [0] moveTo
                    x1,                      y1 + radiusTopLeft,   
                    // [2] curveTo (top-left arc)
                    x1,                      y1 + radiusTopLeft2,  
                    x1 + radiusTopLeft2,     y1,
                    x1 + radiusTopLeft,      y1,
                    // [8] lineTo (top line)
                    x2 - radiusTopRight,     y1,
                    // [10] curveTo (top-right arc)
                    x2 - radiusTopRight2,    y1,
                    x2,                      y1 + radiusTopRight2,
                    x2,                      y1 + radiusTopRight,
                    // [16] lineTo (right line)
                    x2,                      y2 - radiusBottomRight,
                    // [18] curveTo (bottom-right arc)
                    x2,                      y2 - radiusBottomRight2,
                    x2 - radiusBottomRight2, y2,
                    x2 - radiusBottomRight,  y2,
                    // [24] lineTo (bottom line)
                    x1 + radiusBottomLeft,   y2,
                    // [26] curveTo (bottom-left arc)
                    x1 + radiusBottomLeft2,  y2,
                    x1,                      y2 - radiusBottomLeft2,
                    x1,                      y2 - radiusBottomLeft,
                    // [32 ]lineTo (left line)
                    x1,                      y1 + radiusTopLeft
            };
            
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, 17);
            }
        }

        
        public int getWindingRule() {
            return WIND_NON_ZERO;
        }

        
        public boolean isDone() {
            return index > 34;
        }

        
        public void next() {
            index = getNextIndex(index);
        }
        
        
        private int getNextIndex(int currentIndex) {
            if (currentIndex == 32) return 34;
            if (currentIndex == 0) return 2;
            if ((currentIndex & 7) == 0) return currentIndex + 2;
            return currentIndex + 6;
        }

        
        public int currentSegment(float[] coords) {
            double[] theCoords = this.coords;
            
            if (index >= 34) {
                return SEG_CLOSE;
            } 
            
            if (index == 0) {
                coords[0] = (float) theCoords[0];
                coords[1] = (float) theCoords[1];
                return SEG_MOVETO;
            } 
            
            if ((index & 7) == 0) { // line
                coords[0] = (float) theCoords[index];
                coords[1] = (float) theCoords[index + 1];
                return SEG_LINETO;
            }
            
            coords[0] = (float) theCoords[index];
            coords[1] = (float) theCoords[index + 1];
            coords[2] = (float) theCoords[index + 2];
            coords[3] = (float) theCoords[index + 3];
            coords[4] = (float) theCoords[index + 4];
            coords[5] = (float) theCoords[index + 5];
            return SEG_CUBICTO;
        }

        
        public int currentSegment(double[] coords) {
            double[] theCoords = this.coords;
            
            if (index >= 34) {
                return SEG_CLOSE;
            } 
            
            if (index == 0) {
                coords[0] = theCoords[0];
                coords[1] = theCoords[1];
                return SEG_MOVETO;
            } 
            
            if ((index & 7) == 0) { // line
                coords[0] = theCoords[index];
                coords[1] = theCoords[index + 1];
                return SEG_LINETO;
            }
            
            coords[0] = theCoords[index];
            coords[1] = theCoords[index + 1];
            coords[2] = theCoords[index + 2];
            coords[3] = theCoords[index + 3];
            coords[4] = theCoords[index + 4];
            coords[5] = theCoords[index + 5];
            return SEG_CUBICTO;
        }
        
        
        static final double K1 = 4.0 * (Math.sqrt(2.0) - 1.0) / 3.0;
        static final double K2 = 1.0 - K1;
    }
}

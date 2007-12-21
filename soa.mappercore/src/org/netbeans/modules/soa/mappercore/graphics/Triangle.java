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
public class Triangle implements Shape {

    private double x1;
    private double y1;
    
    private double x2;
    private double y2;
    
    private double x3;
    private double y3;

    
    public Triangle(double x1, double y1, 
            double x2, double y2, 
            double x3, double y3) 
    {
        this.x1 = x1;
        this.y1 = y1;
        
        this.x2 = x2;
        this.y2 = y2;
        
        this.x3 = x3;
        this.y3 = y3;
    }
    

    public Rectangle getBounds() {
        int x = (int) Math.floor(getMinX());
        int y = (int) Math.floor(getMinY());

        return new Rectangle(x, y, 
                (int) Math.ceil(getMaxX()) - x,
                (int) Math.ceil(getMaxX()) - y);
    }

    
    public Rectangle2D getBounds2D() {
        double x = getMinX();
        double y = getMinY();
        return new Rectangle2D.Double(x, y, getMaxX() - x, getMaxY() - y);
    }
    
    
    public double getMinX() { return Math.min(x1, Math.min(x2, x3)); }
    public double getMinY() { return Math.min(y1, Math.min(y2, y3)); }

    public double getMaxX() { return Math.max(x1, Math.max(x2, x3)); }
    public double getMaxY() { return Math.max(y1, Math.max(y2, y3)); }
    
    
    public boolean contains(double x, double y) {
        x -= x1;
        y -= y1;
        
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        
        double dx2 = x3 - x1;
        double dy2 = x3 - y1;
        
        if (x < Math.min(dx1, dx2)) return false;
        if (y < Math.min(dy1, dy2)) return false;
        
        if (x > Math.max(dx1, dx2)) return false;
        if (y > Math.max(dy1, dy2)) return false;
        
        // alfa * dx1 + beta * dx2 = x
        // alfa * dy1 + beta * dy2 = y
        
        double det = dx1 * dy2 - dx2 * dy1;
        double det1 = dx2 * y - dy2 * x;
        double det2 = dx1 * y - dy1 * x;
        
        double alfa = -det1 / det;
        double beta = det2 / det;
        
        return 0.0 <= alfa && 0.0 <= beta && alfa + beta <= 1.0;
    }
    

    public boolean intersects(double x, double y, double w, double h) {
        if (getMaxX() < x) return false;
        if (getMaxY() < y) return false;
        if (x + w < getMinX()) return false;
        if (y + h < getMinY()) return false;
        
        return true;
    }
    

    public boolean contains(double x, double y, double w, double h) {
        double x2 = x + w;
        double y2 = y + h;
        return contains(x, y) && contains(x2, y2) 
                && contains(x2, y) && contains(x, y2);
    }
    

    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    
    public PathIterator getPathIterator(AffineTransform at) {
        return new TriangePathIterator(x1, y1, x2, y2, x3, y3, at);
    }

    
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
    
    
    private static class TriangePathIterator implements PathIterator {
        
        private double[] coords;
        private int index = 0;
        
        
        public TriangePathIterator(double x1, double y1, 
                double x2, double y2, 
                double x3, double y3,
                AffineTransform at) 
        {
            coords = new double[] { x1, y1, x2, y2, x3, y3 };
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 3);
            }
        }
        
        
        public int getWindingRule() {
            return WIND_NON_ZERO;
        }

        
        public boolean isDone() {
            return index > 6;
        }

        
        public void next() {
            index += 2;
        }

        
        public int currentSegment(float[] coords) {
            if (index < 6) {
                coords[0] = (float) this.coords[index];
                coords[1] = (float) this.coords[index + 1];
                return (index == 0) ? SEG_MOVETO : SEG_LINETO;
            }
            return SEG_CLOSE;
        }
        

        public int currentSegment(double[] coords) {
            if (index < 6) {
                coords[0] = this.coords[index];
                coords[1] = this.coords[index + 1];
                return (index == 0) ? SEG_MOVETO : SEG_LINETO;
            }
            return SEG_CLOSE;
        }
    }
}

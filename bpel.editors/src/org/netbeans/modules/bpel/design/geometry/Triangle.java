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


package org.netbeans.modules.bpel.design.geometry;


import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author anjeleevich
 */
public class Triangle implements Shape {

    private double[] coords = new double[8];
    
    private double minX = Double.NaN;
    private double minY = 0.0;
    
    private double maxX = 0.0;
    private double maxY = 0.0;
    
    
    public Triangle(double x1, double y1, double x2, double y2, 
            double x3, double y3) 
    {
        coords[0] = x1;
        coords[1] = y1;
        coords[2] = x2;
        coords[3] = y2;
        coords[4] = x3;
        coords[5] = y3;
        coords[6] = x1;
        coords[7] = y1;
    }

    
    private final void calculateBounds() {
        if (minX != minX) {
            maxX = minX = coords[0];
            maxY = minY = coords[1];
            
            for (int i = 2; i < 6; i += 2) {
                double x = coords[i];
                double y = coords[i + 1];
                
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                
                if (maxX < x) maxX = x;
                if (maxY < y) maxY = y;
            }
        }
    }

    
    public Rectangle getBounds() {
        calculateBounds();
        int x = (int) Math.floor(minX);
        int y = (int) Math.floor(minY);
        return new Rectangle(x, y, (int) Math.ceil(maxX) - x, 
                (int) Math.ceil(maxY) - y);
    }
    

    public Rectangle2D getBounds2D() {
        calculateBounds();
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
    

    public boolean contains(double px, double py) {
        calculateBounds();
        
        if (px < minX || maxX < px) return false;
        if (py < minY || maxY < py) return false;
        
        double x0 = coords[0];
        double y0 = coords[1];
        
        px -= x0;
        py -= y0;
        
        double x1 = coords[2] - x0;
        double y1 = coords[3] - y0;
        
        double x2 = coords[4] - x0;
        double y2 = coords[5] - y0;
        
        double lenSq1 = x1 * x1 + y1 * y1;
        double lenSq2 = x2 * x2 + y1 * y2;
        
        double dotPr12 = x2 * x1 + y2 * y1;
        double dotPr1  = px * x1 + py * y1;
        double dotPr2  = px * x2 + py * y2;
        
        double det = lenSq1 * lenSq2 - dotPr12 * dotPr12;
        double a1 = -(dotPr12 * dotPr2 - lenSq2 * dotPr1) / det;
        double a2 = (lenSq1 * dotPr2 - dotPr12 * dotPr1) / det;
        
        double sum = a1 + a2;
        
        return (0.0 <= a1 && a1 <= 1.0) && (0.0 <= a2 && a2 <= 1.0) 
                && (sum <= 1.0);
    }
    

    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }
    

    public boolean intersects(double x, double y, double w, double h) {
        calculateBounds();
        
        double x1 = Math.min(x, x + w);
        double y1 = Math.min(y, y + h);
        
        double x2 = x1 + Math.abs(w);
        double y2 = y1 + Math.abs(h);
        
        return (Math.max(x1, minX) <= Math.min(x2, maxX)) 
                && (Math.max(y1, minY) <= Math.min(y2, maxY));
    }
    

    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    
    public boolean contains(double x, double y, double w, double h) {
        return contains(x, y) && contains(x + w, y) && contains(y, y + h) 
                && contains(x + w, y + h);
    } 

    
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    

    public PathIterator getPathIterator(AffineTransform at) {
        return new TrianglePathIterator(at);
    }

    
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new TrianglePathIterator(at);
    }
    
    
    private class TrianglePathIterator implements PathIterator {
        
        private int index = 0;
        private AffineTransform at;
        
        
        public TrianglePathIterator(AffineTransform at) {
            this.at = at;
        }
        
        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }

        
        public boolean isDone() {
            return (index > 8);
        }

        
        public void next() {
            index += 2;
        }

        
        public int currentSegment(float[] coords) {
            if (index >= 8) {
                return PathIterator.SEG_CLOSE;
            }
                
            coords[0] = (float) Triangle.this.coords[index];
            coords[1] = (float) Triangle.this.coords[index + 1];
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return (index == 0) 
                    ? PathIterator.SEG_MOVETO 
                    : PathIterator.SEG_LINETO; 
        }

        
        public int currentSegment(double[] coords) {
            if (index >= 8) {
                return PathIterator.SEG_CLOSE;
            }
                
            coords[0] = Triangle.this.coords[index];
            coords[1] = Triangle.this.coords[index + 1];
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return (index == 0) 
                    ? PathIterator.SEG_MOVETO 
                    : PathIterator.SEG_LINETO; 
        }
    }
}

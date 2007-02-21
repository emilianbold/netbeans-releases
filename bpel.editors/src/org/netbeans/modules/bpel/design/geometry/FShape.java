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


import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


public abstract class FShape extends FBounds implements Shape {
    

    
    public FShape(double width, double height) {
        super(width, height);
    }
    
    
    public FShape(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    
    public FShape rebound(double x, double y, double w, double h) {
        return reshape(x, y, w, h);
    }
    
    
    public abstract FShape enlarge(double v);
    

    public abstract FShape reshape(double x, double y, double w, double h);
    
    
    public FShape reshapeCentered(double cx, double cy, double w, double h) {
        return reshape(cx - w / 2.0, cy - h / 2.0, w, h);
    }
    
    
    public FShape move(double x, double y) {
        return reshape(x, y, width, height);
    }
    
    
    public FShape moveCenter(double cx, double cy) {
        return reshape(cx - width / 2.0, cy - height / 2.0, width, height);
    }
    
    public FShape resize(double w, double h) {
        return reshape(x, y, w, h);
    }
    
    
    public FShape resizeCentered(double w, double h) {
        return reshapeCentered(getCenterX(), getCenterY(), w, h);
    }
    
    public FShape translate(double tx, double ty) {
        return reshape(x + tx, y + ty, width, height);
    }
    
     
    public abstract boolean intersect(FIntersector intersector);   
    
   
    public Area createArea() {
        return new Area(this);
    }
    
    
    public abstract PathIterator getPathIterator(AffineTransform at);
    
    
    public PathIterator getPathIterator(AffineTransform at, 
            double flatness) 
    {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
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

    
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Float(x, y, width, height);
    }

    
    public Rectangle getBounds() {
        int x1 = (int) Math.floor(x);
        int y1 = (int) Math.floor(y);
        
        int x2 = (int) Math.ceil(x + width);
        int y2 = (int) Math.ceil(y + height);
        
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }


    public boolean intersects(double x, double y, double w, double h) {
        double x1, x2, y1, y2;
        
        if (w < 0.0) {
            x1 = x + w;
            x2 = x;
        } else {
            x1 = x;
            x2 = x + w;
        }
        
        if (h < 0.0) {
            y1 = y + h;
            y2 = y;
        } else {
            y1 = y;
            y2 = y + h;
        }
        
        if (this.x + this.width < x1) return false;
        if (this.y + this.height < y1) return false;
        if (x2 < this.x) return false;
        if (y2 < this.y) return false;
        
        return true;
    }


    public boolean contains(double x, double y, double w, double h) {
        return contains(x, y) && contains(x + w, y) && contains(x, y + h) 
                && contains(x + w, y + h);
    }

    
    
    public FPoint getNormalizedCenter(Graphics2D g2) {
        return getNormalizedCenter(g2.getTransform());
    }
    
    
    public FPoint getNormalizedCenter(AffineTransform at) {
        if (at == null) {
            return new FPoint(
                    0.5 * ((int) x + (int) (x + width)),
                    0.5 * ((int) y + (int) (y + height)));
        }

        float[] coords = { x, y, x + width, y + height };

        at.transform(coords, 0, coords, 0, 2);

        double[] center = {
                0.5 * ((int) coords[0] + (int) coords[2]) + 0.5,
                0.5 * ((int) coords[1] + (int) coords[3]) + 0.5
        };

        try {
            at.inverseTransform(center, 0, center, 0, 1);
        } catch (NoninvertibleTransformException exception) {
            return getCenter();
        }

        return new FPoint(center[0], center[1]);
    }
    
    
    protected class LinePathIterator implements PathIterator {
        
        private AffineTransform at;
        private int index = 0;
        
        public LinePathIterator(AffineTransform at) {
            this.at = at;
        }
        

        public int currentSegment(double[] coords) {
            int type = 0;
            
            if (index == 0) {
                coords[0] = x;
                coords[1] = y;
                type = PathIterator.SEG_MOVETO;
            } else {
                coords[0] = x + width;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            }
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return type;
        }
        

        public int currentSegment(float[] coords) {
            int type = 0;
            
            if (index == 0) {
                coords[0] = x;
                coords[1] = y;
                type = PathIterator.SEG_MOVETO;
            } else {
                coords[0] = x + width;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            }
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return type;
        }

        
        public void next() { 
            index++; 
        }

        
        public boolean isDone() {
            return index > 1;
        }

        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }
    }
    
    
    protected class EmptyPathIterator implements PathIterator {
        public int currentSegment(double[] coords) {
            return PathIterator.SEG_CLOSE;
        }

        public int currentSegment(float[] coords) {
            return PathIterator.SEG_CLOSE;
        }

        
        public void next() {}

        
        public boolean isDone() {
            return true;
        }

        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }
    }
}

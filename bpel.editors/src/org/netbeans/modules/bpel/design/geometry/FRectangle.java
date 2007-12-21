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

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 *
 * @author anjeleevich
 */
public class FRectangle extends FShape {

    public final float radius;


    public FRectangle(double width, double height) {
        this(0.0, 0.0, width, height, 00);
    }
    

    public FRectangle(double width, double height, double radius) {
        this(0.0, 0.0, width, height, radius);
    }
    
    
    public FRectangle(double x, double y, double width, double height) {
        this(x, y, width, height, 0.0);
    }
    

    public FRectangle(double x, double y, double width, double height, 
            double radius) 
    {
        super(x, y, width, height);
        this.radius = (float) radius;
    }
    
    
    public FRectangle enlarge(double v) {
        double v2 = v * 2;
        return new FRectangle(x - v, y - v, width + v2, height + v2, radius + v);
    }
    
    
    public FRectangle reshape(double x, double y, double w, double h) {
        return new FRectangle((float) x, (float) y, (float) w, (float) h, 
                radius);
    }
    
    
    public double getRadius() {
        double r = this.radius;
        
        if (r < 0.0) return 0.0;
        
        double w = (double) this.width / 2.0;
        double h = (double) this.height / 2.0;
        
        return (h < w) ? ((r < h) ? r : h) : ((r < w) ? r : w);
    }
    
    
    public FRectangle radius(double r) {
        return new FRectangle(x, y, width, height, r);
    }

    
    public boolean intersect(FIntersector intersector) {
        intersector.intersectByRoundRectangle(x, y, width, height, radius);
        return intersector.ok();
    }    
    
    
    public PathIterator getPathIterator(AffineTransform at) {
        if (width == 0.0f) {
            return (height == 0.0f) 
                    ? new EmptyPathIterator() 
                    : new LinePathIterator(at);
        } else if (height == 0.0f) {
            return new LinePathIterator(at);
        }

        if (getRadius() == 0.0f) {
            return new RectanglePathIterator(at);
        }
        
        return new RoundRectanglePathItarator(at);
    }
    

    public boolean contains(double px, double py) {
        if (!super.contains(px, py)) return false;
        
        double radius = getRadius();
        
        if (radius == 0.0) return true;
        
        double radiusSq = (double) radius * (double) radius;
        
        double x1 = x + radius - px;
        double x2 = x + width - radius - px;
        
        double y1 = y + radius - py;
        double y2 = y + height - radius - py;

        if (x1 <= 0.0 && 0.0 <= x2) return true;
        if (y1 <= 0.0 && 0.0 <= y2) return true;
        
        double xSq1 = x1 * x1;
        double ySq1 = y1 * y1;
        
        double xSq2 = x2 * x2;
        double ySq2 = y2 * y2;
        
        if (xSq1 + ySq1 <= radiusSq) return true;
        if (xSq1 + ySq2 <= radiusSq) return true;
        if (xSq2 + ySq1 <= radiusSq) return true;
        if (xSq2 + ySq2 <= radiusSq) return true;
        
        return false;
    }
    
    
    private class RoundRectanglePathItarator implements PathIterator {
        
        private AffineTransform at;
        private int index = 0;
        private float radius;
        
        private boolean skipHorizontalLines;
        private boolean skipVerticalLines;
        
        
        public RoundRectanglePathItarator(AffineTransform at) {
            double radius = getRadius();
            
            this.radius = (float) radius;
            this.at = at;
            
            skipHorizontalLines = radius >= (double) width / 2.0f;
            skipVerticalLines = radius >= (double) height / 2.0f;
        }


        public int currentSegment(double[] coords) {
            if (index >= TYPE.length) {
                return PathIterator.SEG_CLOSE;
            }
            
            int type = TYPE[index];
            double[] k = K[index];
            
            coords[0] = x + k[0] * width  + k[2] * radius;
            coords[1] = y + k[1] * height + k[3] * radius;
            
            if (type == PathIterator.SEG_CUBICTO)  {
                coords[2] = x + k[4] * width  + k[6] * radius;
                coords[3] = y + k[5] * height + k[7] * radius;
                
                coords[4] = x + k[8] * width  + k[10] * radius;
                coords[5] = y + k[9] * height + k[11] * radius;
                
                if (at != null) {
                    at.transform(coords, 0, coords, 0, 3);
                }
            } else if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return type;
        }

        
        public int currentSegment(float[] coords) {
            if (index >= TYPE.length) {
                return PathIterator.SEG_CLOSE;
            }
            
            int type = TYPE[index];
            double[] k = K[index];
            
            coords[0] = (float) (x + k[0] * width  + k[2] * radius);
            coords[1] = (float) (y + k[1] * height + k[3] * radius);
            
            if (type == PathIterator.SEG_CUBICTO)  {
                coords[2] = (float) (x + k[4] * width  + k[6] * radius);
                coords[3] = (float) (y + k[5] * height + k[7] * radius);
                
                coords[4] = (float) (x + k[8] * width  + k[10] * radius);
                coords[5] = (float) (y + k[9] * height + k[11] * radius);
                
                if (at != null) {
                    at.transform(coords, 0, coords, 0, 3);
                }
            } else if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return type;
        }

        
        public void next() {
            if (index == 0 || index == 4) {
                if (skipHorizontalLines) index += 2;
                else index++;
                return;
            }
            
            if (index == 2 || index == 6) {
                if (skipVerticalLines) index += 2;
                else index++;
                return;
            }

            index++;
        }

        
        public boolean isDone() {
            return index > TYPE.length;
        }

        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }
    }
    
    
    private class RectanglePathIterator implements PathIterator {
        
        private AffineTransform at;
        private int index = 0;
        
        public RectanglePathIterator(AffineTransform at) {
            this.at = at;
        }
        
        
        public int currentSegment(double[] coords) {
            int type;
            
            if (index == 0) {
                coords[0] = x;
                coords[1] = y;
                type = PathIterator.SEG_MOVETO;
            } else if (index == 1) {
                coords[0] = x + width;
                coords[1] = y;
                type = PathIterator.SEG_LINETO;
            } else if (index == 2) {
                coords[0] = x + width;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            } else if (index == 3) {
                coords[0] = x;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            } else if (index == 4) {
                coords[0] = x;
                coords[1] = y;
                type = PathIterator.SEG_LINETO;
            } else {
                return PathIterator.SEG_CLOSE;
            }

            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
        
            return type;
        }

        
        public int currentSegment(float[] coords) {
            int type;
            
            if (index == 0) {
                coords[0] = x;
                coords[1] = y;
                type = PathIterator.SEG_MOVETO;
            } else if (index == 1) {
                coords[0] = x + width;
                coords[1] = y;
                type = PathIterator.SEG_LINETO;
            } else if (index == 2) {
                coords[0] = x + width;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            } else if (index == 3) {
                coords[0] = x;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            } else if (index == 4) {
                coords[0] = x;
                coords[1] = y;
                type = PathIterator.SEG_LINETO;
            } else {
                return PathIterator.SEG_CLOSE;
            }

            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
        
            return type;
        }

        
        public boolean isDone() {
            return (index >= 6);
        }

        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }
        

        public void next() {
            index++;
        }
    }
    

    private static final double V = 1.0 - 4.0 * (Math.sqrt(2.0) - 1.0) / 3.0;

 
    private static final int TYPE[] = {
        PathIterator.SEG_MOVETO,
        PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
        PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
        PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
        PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
    };
    
    
    private static final double K[][] = {
        { 0.0, 0.0, 1.0, 0.0 },
        { 1.0, 0.0, -1.0, 0.0 },
        { 1.0, 0.0, -V, 0.0, 1.0, 0.0, 0.0, V, 1.0, 0.0, 0.0, 1.0 },
        { 1.0, 1.0, 0.0, -1.0 },
        { 1.0, 1.0, 0.0, -V, 1.0, 1.0, -V, 0.0, 1.0, 1.0, -1.0, 0.0 },
        { 0.0, 1.0, 1.0, 0.0 },
        { 0.0, 1.0, V, 0.0, 0.0, 1.0, 0.0, -V, 0.0, 1.0, 0.0, -1.0 },
        { 0.0, 0.0, 0.0, 1.0 },
        { 0.0, 0.0, 0.0, V, 0.0, 0.0, V, 0.0, 0.0, 0.0, 1.0, 0.0 }
    };
}

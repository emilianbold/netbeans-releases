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
import java.util.Collection;

/**
 *
 * @author anjeleevich
 */
public class FPath implements Shape {

    private FPoint[] points;
    private int[] types;
    
    private int pointOffset;
    private int pointCount;
    
    private float minX;
    private float minY;
    
    private float maxX;
    private float maxY;
    
    
    public FPath(double x1, double y1, double x2, double y2) {
        points = new FPoint[] { new FPoint(x1, y1), new FPoint(x2, y2) };
        types = new int[] { PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO };
        pointOffset = 0;
        pointCount = 2;
        ok();
    }
    

    public FPath(double x1, double y1, 
            double x2, double y2, 
            double x3, double y3) 
    {
        points = new FPoint[] { 
                new FPoint(x1, y1), 
                new FPoint(x2, y2),
                new FPoint(x3, y3) 
        };
        
        types = new int[] { 
                PathIterator.SEG_MOVETO, 
                PathIterator.SEG_LINETO,
                PathIterator.SEG_LINETO
        };
        
        pointOffset = 0;
        pointCount = 3;
        ok();
    }    

    
    public FPath(double x1, double y1, 
            double x2, double y2, 
            double x3, double y3,
            double x4, double y4) 
    {
        points = new FPoint[] { 
                new FPoint(x1, y1), 
                new FPoint(x2, y2),
                new FPoint(x3, y3), 
                new FPoint(x4, y4) 
        };
        
        types = new int[] { 
                PathIterator.SEG_MOVETO, 
                PathIterator.SEG_LINETO,
                PathIterator.SEG_LINETO,
                PathIterator.SEG_LINETO
        };
        
        pointOffset = 0;
        pointCount = 4;
        ok();
    }    
    
    
    private FPath() {
        points = new FPoint[INITIAL_SIZE];
        types = new int[INITIAL_SIZE];
        pointOffset = 0;
        pointCount = 0;
    };
    
    
    public FPath(Collection<FPoint> points) {
        pointOffset = 0;
        pointCount = points.size();
        this.points = points.toArray(new FPoint[pointCount]);
        this.types = new int[pointCount];
        
        if (pointCount > 0) {
            types[0] = PathIterator.SEG_MOVETO;
            for (int i = pointCount - 1; i >= 1; i--) {
                types[i] = PathIterator.SEG_LINETO;
            }
        }
    }
    
    
    public FPath(FPoint[] points) {
        pointOffset = 0;
        pointCount = points.length;
        
        this.points = new FPoint[pointCount];
        this.types = new int[pointCount];
        
        if (pointCount > 0) {
            System.arraycopy(points, 0, this.points, 0, pointCount);
            types[0] = PathIterator.SEG_MOVETO;
            for (int i = pointCount - 1; i >= 1; i--) {
                types[i] = PathIterator.SEG_LINETO;
            }
        }
    }
    
    
    private FPath(FPoint[] points, int[] types, int offset, int count) {
        this.types = types;
        this.points = points;
        pointOffset = offset;
        pointCount = count;
        ok();
    }
    
    
    
    public FPath translate(double tx, double ty) {
        if (((float) tx == 0.0f) && ((float) ty == 0.0f)) return this;
        
        FPoint[] newPoints = new FPoint[pointCount];
        int[] newTypes = new int[pointCount];
        
        for (int i = 0; i < pointCount; i++) {
            int j = pointOffset + i;
            newPoints[i] = points[j].translate(tx, ty);
            newTypes[i] = types[j];
        }
        
        return new FPath(newPoints, newTypes, 0, pointCount);
    }
    
    
    public FPath move(double px, double py) {
        if (pointCount == 0) {
            return this;
        }
        
        FPoint p = points[pointOffset];
        
        return translate(px - p.x, py - p.y);
    }


    public FPath subtract(FShape shape) {
        if (pointCount == 0) { return this; }
        
        int i1 = pointOffset;
        int i2 = i1 + pointCount;
        
        FIntersector intersector = null;
        
        FPoint point1 = points[i1];
        int type1 = types[i1];
        boolean inside1 = shape.contains(point1);
        
        FPath result = new FPath();
        
        for (int i = i1 + 1; i < i2; i++) {
            FPoint point2 = points[i];
            int type2 = types[i];
            boolean inside2 = shape.contains(point2);
            
            if (type2 == PathIterator.SEG_MOVETO) {
                result.addSpace(point1, point2);
            } else if (inside1 && inside2) {
                result.addSpace(point1, point2);
            } else {
                if (intersector == null) {
                    intersector = new FIntersector();
                }
                
                intersector.setLine(point1, point2);
                
                if (shape.intersect(intersector)) {
                    FPoint ip1 = point1.point(intersector.getT1(), point2);
                    FPoint ip2 = point1.point(intersector.getT2(), point2);
                    
                    if (!ip1.equals(point1)) {
                        result.addLine(point1, ip1);
                    } 
                    
                    result.addSpace(ip1, ip2);
                    
                    if (!ip2.equals(point2)) {
                        result.addLine(ip2, point2);
                    }
                } else {
                    result.addLine(point1, point2);
                }
            }
            
            inside1 = inside2;
            type1 = type2;
            point1 = point2;
        }
        
        result.ok();
        
        return result;
    }
    
    
    public FPath intersect(FShape shape) {
        if (pointCount == 0) { return this; }
        
        int i1 = pointOffset;
        int i2 = i1 + pointCount;
        
        FIntersector intersector = null;
        
        FPoint point1 = points[i1];
        int type1 = types[i1];
        boolean inside1 = shape.contains(point1);
        
        FPath result = new FPath();;
        
        for (int i = i1 + 1; i < i2; i++) {
            FPoint point2 = points[i];
            int type2 = types[i];
            boolean inside2 = shape.contains(point2);
            
            if (type2 == PathIterator.SEG_MOVETO) {
                result.addSpace(point1, point2);
            } else if (inside1 && inside2) {
                result.addLine(point1, point2);
            } else {
                if (intersector == null) {
                    intersector = new FIntersector();
                }
                
                intersector.setLine(point1, point2);
                
                if (shape.intersect(intersector)) {
                    FPoint ip1 = point1.point(intersector.getT1(), point2);
                    FPoint ip2 = point1.point(intersector.getT2(), point2);
                    
                    if (!ip1.equals(point1)) {
                        result.addSpace(point1, ip1);
                    } 
                    
                    result.addLine(ip1, ip2);
                    
                    if (!ip2.equals(point2)) {
                        result.addSpace(ip2, point2);
                    }
                } else {
                    result.addSpace(point1, point2);
                }
            }
            
            inside1 = inside2;
            type1 = type2;
            point1 = point2;
        }
        
        result.ok();
        
        return result;
    }
    
    
    public FPath round(double radius) {
        if (radius <= 0.0) return this;
        if (pointCount <= 2) return this;
        
        FPath result = new FPath();
        
        int i1 = pointOffset;
        int i2 = i1 + pointCount;
        
        FPoint p = points[i1];
        
        for (int i = i1 + 1; i < i2; i++) {
            FPoint p2 = points[i];
            int t2 = types[i];
            
            if (t2 == PathIterator.SEG_MOVETO) {
                result.addSpace(p, p2);
                p = p2;
                continue;
            } 
            
            
            if (i + 1 == i2) {
                result.addLine(p, p2);
                p = p2;
                continue;
            } 
              
            int t3 = types[i + 1];
                
            if (t3 == PathIterator.SEG_MOVETO) {
                result.addLine(p, p2);
                p = p2;
            } else {
                FPoint p1 = points[i - 1];
                FPoint p3 = points[i + 1];

                double len1 = p1.distance(p2);
                double len2 = p2.distance(p3);

                if (((float) len1 != 0.0f) && ((float) len2 != 0.0f)) {
                    double k1 = (len1 - radius) / len1;

                    if (k1 < 0.5) { 
                        k1 = 0.5;
                    }

                    double k2 = radius / len2;

                    if (k2 > 0.5) {
                        k2 = 0.5;
                    }

                    FPoint kp1 = p1.point(k1, p2);
                    FPoint kp2 = p2.point(k2, p3);

                    result.addLine(p, kp1);
                    result.addLine(kp1, kp2);

                    p = kp2;
                } else {
                    result.addLine(p, p2);
                    p = p2;
                }
            }
        }
        
        result.ok();
        
        return result;
    }
    

    private void addSpace(FPoint p1, FPoint p2) {
        if (pointCount == 0) {
            ensureCapacity(1);
            points[0] = p2;
            types[0] = PathIterator.SEG_MOVETO;
            pointCount = 1;
        } else if (types[pointCount - 1] == PathIterator.SEG_MOVETO) {
            points[pointCount - 1] = p2;
        } else {
            ensureCapacity(pointCount + 1);
            points[pointCount] = p2;
            types[pointCount] = PathIterator.SEG_MOVETO;
            pointCount++;
        }
    }

    
    private void addLine(FPoint p1, FPoint p2) {
        if (p1.equals(p2)) return;
        
        if (pointCount == 0) {
            ensureCapacity(2);
            
            points[0] = p1;
            points[1] = p2;

            types[0] = PathIterator.SEG_MOVETO;
            types[1] = PathIterator.SEG_LINETO;
            
            pointCount = 2;
        } else {
            ensureCapacity(pointCount + 1);
            
            points[pointCount] = p2;
            types[pointCount] = PathIterator.SEG_LINETO;
            pointCount++;
        }
    }
    
    
    private void ensureCapacity(int size) {
        if (size > points.length) {
            int newSize = points.length * 3 / 2 + 1;
            int maxNewSize = points.length + 10;
            
            if (newSize > maxNewSize) {
                newSize = maxNewSize;
            }
            
            if (newSize < size) {
                newSize = size;
            }
            
            FPoint[] newPoints = new FPoint[newSize];
            int[] newTypes = new int[newSize];
            
            System.arraycopy(points, 0, newPoints, 0, pointCount);
            System.arraycopy(types, 0, newTypes, 0, pointCount);
            
            points = newPoints;
            types = newTypes;
        }
    }
    
    
    private void ok() {
        int i1 = pointOffset;
        int i2 = i1 + pointCount - 1;
        
        for (int i = i2; i >= i1; i--) {
            if (types[i] == PathIterator.SEG_LINETO) break;
            
            points[i] = null;
            pointCount--;
        }
            
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;

        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        
        i2 = i1 + pointCount;
        
        for (int i = i1; i < i2; i++) {
            FPoint p = points[i];
            
            if (p.x < minX) { minX = p.x; }
            if (p.x > maxX) { maxX = p.x; }
            
            if (p.y < minY) { minY = p.y; }
            if (p.y > maxY) { maxY = p.y; }
        }
        
        this.minX = minX;
        this.minY = minY;
        
        this.maxX = maxX;
        this.maxY = maxY;
    }
    
    
    public boolean isConnected() {
        if (pointCount == 0) return true;
        
        for (int i = 2; i < pointCount; i++) {
            if (types[i] == PathIterator.SEG_MOVETO) return false;
        }
        
        return true;
    }
    
    
    public double length() {
        if (pointCount == 0) return 0.0;
        
        double length = 0.0;
        
        int i1 = pointOffset;
        int i2 = i1 + pointCount;
        
        FPoint p1 = points[i1];
        
        for (int i = i1 + 1; i < i2; i++) {
            FPoint p2 = points[i];
            
            if (!p1.equals(p2) && types[i] == PathIterator.SEG_LINETO) {
                length += p1.distance(p2);
            }
            
            p1 = p2;
        }
        
        return length;
    }
    
    
    public FCoords coords(double t) {
        t = parameterToLength(t);

        int i1 = pointOffset;
        int i2 = i1 + pointCount;
        
        double length = 0;

        FPoint p1 = points[i1];
        
        for (int i = i1 + 1; i < i2; i++) {
            FPoint p2 = points[i];
            
            if (!p1.equals(p2) && (types[i] == PathIterator.SEG_LINETO)) {
                double segLength = p1.distance(p2);
                double nextLength = length + segLength;
                
                if (length <= t && t <= nextLength) {
                    t = (t - length) / segLength;
                    
                    double x0;
                    double y0;
                    
                    double dx = p2.x - p1.x;
                    double dy = p2.y - p1.y;
                    
                    if (t < 0.0) {
                        x0 = p1.x;
                        y0 = p1.y;
                    } else if (t > 1.0) {
                        x0 = p2.x;
                        y0 = p2.y;
                    } else {
                        x0 = p1.x + t * dx;
                        y0 = p1.y + t * dy;
                    }
                    
                    return new FCoords(x0, y0, dx / segLength, dy / segLength);
                }
                
                length = nextLength;
            }
            
            p1 = p2;
        }
        
        return new FCoords(p1.x, p1.y);
    }
    
    
    
    public FPoint point(double t) {
        t = parameterToLength(t);

        int i1 = pointOffset;
        int i2 = i1 + pointCount;
        
        FPoint p1 = points[i1];
        
        for (int i = i1 + 1; i < i2; i++) {
            FPoint p2 = points[i];
            
            if (!p1.equals(p2) && (types[i] == PathIterator.SEG_LINETO)) {
                double segLength = p1.distance(p2);
                
                if (t <= segLength) {
                    return (t == 0) ? p1 : p1.point(t / segLength, p2);
                }
                
                t -= segLength;
            }
            
            p1 = p2;
        }
        
        return p1;
    }
    
    
    public FPoint tangent(double t) {
        t = parameterToLength(t);

        int i1 = pointOffset;
        int i2 = i1 + pointCount;
        
        double length = 0;
        
        FPoint p1 = points[i1];
        
        for (int i = i1 + 1; i < i2; i++) {
            FPoint p2 = points[i];
            
            if (!p1.equals(p2) && (types[i] == PathIterator.SEG_LINETO)) {
                double segLength = p1.distance(p2);
                double nextLength = length + segLength;
                
                if (length <= t && t <= nextLength) {
                    return new FPoint(
                            (p2.x - p1.x) / segLength, 
                            (p2.y - p1.y) / segLength);
                }
                
                length = nextLength;
            }
            
            p1 = p2;
        }
        
        return new FPoint(0, 0);
    }
    
    
    public FPoint normal(double t) {
        return tangent(t).rotate90();
    }
    
    
    private double parameterToLength(double t) {
        if (t < 0.0) return 0.0;
        if (t >= 1.0) return length();
        return t * length();
   }
    
    
    
    // java.awt.Shape interface implementation below
    
    public boolean contains(Point2D p) { return false; }
    
    public boolean contains(Rectangle2D r) { return false; }

    public boolean contains(double x, double y, double w, double h) {
        return false; 
    }
    
    public boolean contains(double x, double y) { return false; }
    
    
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    
    public PathIterator getPathIterator(AffineTransform at) {
        return new PathPathIterator(at);
    }

    
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }

    
    public boolean intersects(double x, double y, double w, double h) {
        if (pointCount == 0) return false;
        
        if (w < 0.0) {
            x += w;
            w = -w;
        }
        
        if (h < 0.0) {
            y += h;
            h = -h;
        }
        
        if (maxX < x) return false;
        if (maxY < y) return false;
        
        if (x + w < minX) return false;
        if (y + h < minY) return false;
        
        return true;
    }

    
    public Rectangle2D getBounds2D() {
        if (pointCount == 0) return new Rectangle2D.Float();

        return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
    }

    
    public Rectangle getBounds() {
        if (pointCount == 0) return new Rectangle();

        int x = (int) Math.floor(minX);
        int y = (int) Math.floor(minY);
        
        return new Rectangle(x, y, 
                (int) Math.ceil(maxX) - x, 
                (int) Math.ceil(maxY) - y);
    }


    private class PathPathIterator implements PathIterator {
        private int index = 0;
        private AffineTransform at;
        
        public PathPathIterator(AffineTransform at) {
            this.at = at;
        }
        
        
        public int currentSegment(double[] coords) {
            FPoint p = points[index];
            
            coords[0] = p.x;
            coords[1] = p.y;
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return types[index];
        }

        
        public int currentSegment(float[] coords) {
            FPoint p = points[index];
            
            coords[0] = p.x;
            coords[1] = p.y;
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return types[index];
        }

        
        public void next() {
            index++;
        }

        
        public boolean isDone() {
            return (index >= pointCount);
        }

        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }
    }

    
    private static final int MAX_GROW = 10;
    private static final int INITIAL_SIZE = 10;

    
}

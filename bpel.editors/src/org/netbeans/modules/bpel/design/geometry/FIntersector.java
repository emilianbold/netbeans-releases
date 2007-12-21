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

/**
 *
 * @author anjeleevich
 */
public class FIntersector {
    

    private double x1;
    private double y1;
    
    private double x2;
    private double y2;
    
    private double dx;
    private double dy;
    
    private double t1;
    private double t2;
    
    
    public FIntersector() {}
    
    
    public FIntersector(double x1, double y1, double x2, double y2) {
        setLine(x1, y1, x2, y2);
    }

    
    public void setLine(FPoint p1, FPoint p2) {
        this.x1 = p1.x;
        this.y1 = p1.y;
        
        this.x2 = p2.x;
        this.y2 = p2.y;
        
        this.dx = x2 - x1;
        this.dy = y2 - y1;
        
        t1 = Double.POSITIVE_INFINITY;
        t2 = Double.NEGATIVE_INFINITY;
    }
    
    
    public void setLine(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        
        this.x2 = x2;
        this.y2 = y2;
        
        this.dx = x2 - x1;
        this.dy = y2 - y1;
        
        t1 = Double.POSITIVE_INFINITY; // 1.0
        t2 = Double.NEGATIVE_INFINITY; // 0.0
    }
    
    
    public void intersectByRoundRectangle(double x, double y, 
            double w, double h, double r) 
    {
        double rx = w / 2.0;
        double ry = h / 2.0;
        
        double cx = x + rx;
        double cy = y + ry;
        
        if (r >= rx) {
            if (r >= ry) {
                intersectByCircle(x + rx, y + ry, (rx < ry) ? rx : ry);
            } else {
                r = rx;
                
                double y1 = y + r;
                double y2 = y + h - r;
                
                intersectByRectangle(cx, cy, rx, (y2 - y1) / 2.0);
                
                cx = x + r;
                
                intersectByCircle(cx, y1, r);
                intersectByCircle(cx, y2, r);
            }
        } else if (r >= ry) {
            r = ry;
            double x1 = x + r;
            double x2 = x + w - r;
            
            intersectByRectangle(cx, cy, (x2 - x1) / 2.0, ry);
            
            cy = y + r;
            
            intersectByCircle(x1, cy, r);
            intersectByCircle(x2, cy, r);
        } else {
            double x1 = x + r;
            double x2 = x + w - r;
            
            double y1 = y + r;
            double y2 = y + h - r;
            
            intersectByRectangle(cx, cy, (x2 - x1) / 2.0, ry);
            intersectByRectangle(cx, cy, rx, (y2 - y1) / 2.0);
            intersectByCircle(x1, y1, r);
            intersectByCircle(x1, y2, r);
            intersectByCircle(x2, y1, r);
            intersectByCircle(x2, y2, r);
        }
    }
    
    
    public void intersectByCircle(double cx, double cy, double r) {
        double x = this.x1 - cx;
        double y = this.y1 - cy;
        
        double dx = this.dx;
        double dy = this.dy;
        
        double rSq = r * r;
        
        double a = (dx * dx + dy * dy) / rSq;
        double b = (x * dx + y * dy) / rSq;
        double c = (x * x + y * y) / rSq - 1.0;
        
        if ((float) a == 0.0f) {
            storeT(-c / b);
            return;
        }
        
        double d = b * b - a * c;
        
        if ((float) d == 0.0f) {
            storeT(-b / a);
        } else if (d > 0.0) {
            d = Math.sqrt(d);
            storeT((-b - d) / a);
            storeT((-b + d) / a);
        }
    }
    
    
    public void intersectByEllipse(double cx, double cy, double rx, double ry) 
    {
        if ((float) (rx - ry) == 0.0f) {
            intersectByCircle(cx, cy, (rx + ry) / 2.0);
            return;
        }
        
        double x = this.x1 - cx;
        double y = this.y1 - cy;
        
        double dx = this.dx;
        double dy = this.dy;
        
        double rxSq = rx * rx;
        double rySq = ry * ry;
        
        double a = dx * dx / rxSq + dy * dy / rySq;
        double b = dx * x / rxSq + dy * y / rySq;
        double c = x * x / rxSq + y * y / rySq - 1.0;
        
        if ((float) a == 0.0f) {
            storeT(-c / b);
            return;
        }
        
        double d = b * b - a * c;
        
        if ((float) d == 0.0f) {
            storeT(-b / a);
        } else if (d > 0.0) {
            d = Math.sqrt(d);
            storeT((-b - d) / a);
            storeT((-b + d) / a);
        }
    }
    
    
    public void intersectByRoumb(double cx, double cy, double rx, double ry) {
        double x = this.x1 - cx;
        double y = this.y1 - cy;
        
        double dx = this.x2 - this.x1;
        double dy = this.y2 - this.y1;

        double c = rx * ry;
        
        double t = (c - (x * ry + y * rx)) / (dx * ry + dy * rx);
        if (x + t * dx >= 0.0 && y + t * dy >= 0.0) {
            storeT(t);
        }
        
        t = (c - (-x * ry + y * rx)) / (-dx * ry + dy * rx);
        if (x + t * dx <= 0.0 && y + t * dy >= 0.0) {
            storeT(t);
        }

        t = (c - (x * ry - y * rx)) / (dx * ry - dy * rx);
        if (x + t * dx >= 0.0 && y + t * dy <= 0.0) {
            storeT(t);
        }
        
        t = (c - (-x * ry - y * rx)) / (-dx * ry - dy * rx);
        if (x + t * dx <= 0.0 && y + t * dy <= 0.0) {
            storeT(t);
        }
    }
    
    
    public void intersectByRectangle(double cx, double cy, 
            double rx, double ry) 
    {
        double x = this.x1 - cx;
        double y = this.y1 - cy;
        
        double dx = this.x2 - this.x1;
        double dy = this.y2 - this.y1;

        if ((float) dx != 0.0f) {
            double t = (-rx - x) / dx;
            
            if (betweenIn(-ry, y + t * dy, ry)) {
                storeT(t);
            }

            t = (rx - x) / dx;
            if (betweenIn(-ry, y + t * dy, ry)) {
                storeT(t);
            }
        }
    
        if ((float) dy != 0.0f) {
            double t = (-ry - y) / dy;
            if (betweenIn(-rx, x + t * dx, rx)) {
                storeT(t);
            }

            t = (ry - y) / dy;
            if (betweenIn(-rx, x + t * dx, rx)) {
                storeT(t);
            }
        }
    }
    

    public boolean ok() {
        if (t1 < 0.0) { t1 = 0.0; }
        if (t2 > 1.0) { t2 = 1.0; }
        return (t1 <= t2);
    }
    
    
    public double getT1() {
        return t1;
    }
    
    
    public double getT2() {
        return t2;
    }
    
    
    private void storeT(double t) {
        if (t < t1) { t1 = t; }
        if (t > t2) { t2 = t; }
    }
    
    
    private static boolean betweenEx(double v1, double v, double v2) {
        return (v1 <= v2) ? ((v1 < v) && (v < v2)) : ((v2 < v) && (v < v1));
    }
    
    
    private static boolean betweenIn(double v1, double v, double v2) {
        return (v1 <= v2) ? ((v1 <= v) && (v <= v2)) : ((v2 <= v) && (v <= v1));
    }

}

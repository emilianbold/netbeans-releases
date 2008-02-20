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
public class FPoint {
    
    public final float x;
    public final float y;
    
    public FPoint(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }
    
    
    public double distance(FPoint point) {
        if (equals(point)) return 0.0;
        return distance(point.x, point.y);
    } 
    
    
    
    public FPoint move(double px, double py) {
        return ((px == x) && (py == y)) ? this : new FPoint(x, y);
    }
    
    
    public FPoint translate(double tx, double ty) {
        return new FPoint(tx + x, ty + y);
    }
    
    
    public double distance(double px, double py) {
        px -= x;
        py -= y;
        
        return Math.sqrt(px * px + py * py);
    }

    
    
    public FPoint point(double t, FPoint p) {
        if (((float) t == 0.0f) || equals(p)) { 
            return this;
        }
        
        if ((float) t == 1.0f) {
            return p;
        }
        
        return point(t, p.x, p.y);
    }
    
    
    public FPoint rotate90() {
        return new FPoint(-y, x);
    }
    
    
    public FPoint point(double t, double px, double py) {
        return new FPoint(
                (double) x + t * (px - (double) x),
                (double) y + t * (py - (double) y));
    }

    
    public boolean equals(FPoint point) {
        return (point == this) || ((point.x == this.x) && (point.y == this.y));
    }
}

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

import java.util.Collection;

/**
 *
 * @author anjeleevich
 */
public class FBounds {
    
    
    public final float x;
    public final float y;
    public final float width;
    public final float height;
    

    public FBounds(double width, double height) {
        this(0.0, 0.0, width, height);
    }
    
    
    public FBounds(double x, double y, double width, double height) {
        if (width < 0.0) {
            this.x = (float) (x + width);
            this.width = (float) -width;
        } else {
            this.x = (float) x;
            this.width = (float) width;
        }
        
        if (height < 0.0) {
            this.y = (float) (y + height);
            this.height = (float) -height;
        } else {
            this.y = (float) y;
            this.height = (float) height;
        }
    }

    
    public FBounds(Collection<FBounds> boundsCollection) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        
        for (FBounds bounds : boundsCollection) {
            float x1 = bounds.x;
            float y1 = bounds.y;
            
            float x2 = x1 + bounds.width;
            float y2 = y1 + bounds.height;
            
            if (x1 < minX) minX = x1;
            if (y1 < minY) minY = y1;
            if (maxX < x2) maxX = x2;
            if (maxY < y2) maxY = y2;
        }
    
        if ((minX <= maxX) && (minY <= maxY)) {
            x = minX;
            y = minY;
            width = maxX - minX;
            height = maxY - minY;
        } else {
            x = 0;
            y = 0;
            width = 0;
            height = 0;
        }
    }
    
    
    public boolean contains(double px, double py) {
        px -= x;
        py -= y;
        return (0.0 <= px) && (px <= width) && (0.0 <= py) && (py <= height);
    }
    
    
    public boolean contains(FPoint point) {
        return contains(point.x, point.y);
    }
    
    
    public FPoint getPoint(double kx, double ky) {
        return new FPoint(x + kx * width, y + ky * height);
    }
    
    
    public double getX() { 
        return x; 
    }
    
    
    public double getY() { 
        return y; 
    }

    
    public double getWidth() { 
        return width; 
    }
    
    
    public double getHeight() { 
        return height; 
    }

    
    public FDimension getSize() { 
        return new FDimension(width, height); 
    }
    
    
    public double getMinX() { 
        return x; 
    }
    
    
    public double getMinY() { 
        return y; 
    }
    
    
    public double getMaxX() { 
        return x + width; 
    }
    
    
    public double getMaxY() { 
        return y + height; 
    }
    
    
    public double getCenterX() { 
        return x + width / 2.0; 
    }
    
    
    public double getCenterY() { 
        return y + height / 2.0; 
    }
    
    
    public FPoint getCenter() { 
        return new FPoint(x + width / 2.0, y + height / 2.0);
    }
    
    
    public FPoint getTopLeft() { 
        return new FPoint(x, y); 
    }
    
    
    public FPoint getTopCenter() { 
        return new FPoint(x + width / 2.0, y); 
    }
    
    
    public FPoint getTopRight() { 
        return new FPoint(x + width, y); 
    }
    
    
    public FPoint getCenterLeft() { 
        return new FPoint(x, y + height / 2.0); 
    }
    
    
    public FPoint getCenterRight() { 
        return new FPoint(x + width, y + height / 2.0); 
    }
    
    
    public FPoint getBottomLeft() { 
        return new FPoint(x, y + height); 
    }
    
    
    public FPoint getBottomCenter() { 
        return new FPoint(x + width / 2.0, y + height); 
    }
    
    
    public FPoint getBottomRight() { 
        return new FPoint(x + width, y + height); 
    }
    
    
    public boolean isIntersects(FBounds bounds) {
        if (x + width < bounds.x) return false;
        if (y + height < bounds.y) return false;
        
        if (bounds.x + bounds.width < x) return false;
        if (bounds.y + bounds.height < y) return false;
        
        return true;
    }   
    
    
    public FBounds translate(double tx, double ty) {
        return new FBounds(tx + x, ty + y, width, height);
    }
    
    
    public FEllipse createEllipse() { 
        return new FEllipse(x, y, width, height); 
    }
    
    
    public FRectangle createRectangle() { 
        return new FRectangle(x, y, width, height); 
    }
    

    public FRectangle createRectangle(double radius) { 
        return new FRectangle(x, y, width, height, radius); 
    }
    

    public FRoumb createRoumb() {
        return new FRoumb(x, y, width, height);
    }
}

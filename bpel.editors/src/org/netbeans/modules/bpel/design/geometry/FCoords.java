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
public class FCoords extends FPoint {
    
    public final float tx;
    public final float ty;

    public final float nx;
    public final float ny;
    
    public FCoords(double x0, double y0, double tx, double ty, 
            double nx, double ny) 
    {
        super(x0, y0);
        
        this.tx = (float) tx;
        this.ty = (float) ty;
        
        this.nx = (float) nx;
        this.ny = (float) ny;
    }


    public FCoords(double x0, double y0, double tx, double ty) {
        this(x0, y0, tx, ty, -ty, tx);
    }

            
    public FCoords(double x0, double y0) {
        this(x0, y0, 0.0, 0.0, 0.0, 0.0);
    }
            
    
    public FPoint getPoint(double x, double y) {
        return new FPoint(this.x + x * tx + y * nx, this.y + x * ty + y * ny);
    }
    
    
    public double getX(double x, double y) {
        return this.x + x * tx + y * nx;
    }
    
    
    public double getY(double x, double y) {
        return this.y + x * ty + y * ny;
    }
}

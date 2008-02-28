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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;


public final class FStroke {

    public final double width;
    public final double dash;
    public final double space;
    
    public final boolean isSolid;

    
    public FStroke(double width) {
        this(width, 0.0, 0.0);
    }
    
    
    public FStroke(double width, double dash) {
        this(width, dash, 0.0);
    }

    
    public FStroke(double width, double dash, double space) {
        this.width = (width < 0.0) ? -width : width;
        
        if (dash <= 0.0) {
            this.isSolid = true;
            this.dash = 0.0;
            this.space = 0.0;
        } else {
//            this.isSolid = false;
//            this.dash = dash;
//            this.space = (space <= 0.0) ? (dash + width) : space;
              this.isSolid = true;
              this.dash = 0.0;
              this.space = 0.0;
        }
    }


    public final Stroke createSolidStroke(Graphics2D g2) {
        return createSolidStroke(g2.getTransform());
    }
    
    
    public final Stroke createStroke(Graphics2D g2) {
        return createStroke(g2.getTransform());
    }
    
    
    public final Stroke createSolidStroke(AffineTransform at) {
        double scale;
        
        if (at == null) { 
            scale = 1.0;
        } else {
            double a = at.getScaleX();
            double b = at.getShearY();
            scale = Math.sqrt(a * a + b * b);
            if (scale <= 0.0) scale = 1.0;
        }

        double k = scale * width;
        
        if (k < 1.0) {
            return new BasicStroke((float) (width / k), 
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        }
        
        return new BasicStroke((float) width, 
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    }
    
    
    public final Stroke createStroke(AffineTransform at) {
        double scale;
        
        if (at == null) { 
            scale = 1.0;
        } else {
            double a = at.getScaleX();
            double b = at.getShearY();
            scale = Math.sqrt(a * a + b * b);
            if (scale <= 0.0) scale = 1.0;
        }

        double k = scale * width;
        
        if (k < 1.0) {
            return (isSolid) 
                    ? new BasicStroke((float) (width / k), 
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
                    : new BasicStroke((float) (width / k), 
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, 
                        new float[] { (float) (dash / k), (float) (space / k) }, 
                        0);
        }
        
        return (isSolid) 
                ? new BasicStroke((float) width, 
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
                : new BasicStroke((float) width, 
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, 
                    new float[] { (float) dash, (float) space }, 0);
    }
}

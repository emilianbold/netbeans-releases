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

package org.netbeans.modules.soa.mappercore.icons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import org.netbeans.modules.soa.mappercore.model.Vertex;

/**
 *
 * @author anjeleevich
 */
public class PlusIcon2D implements Icon2D {
    
    private double radius;
    private Color color;
    
    public PlusIcon2D(Color color, double radius) {
        this.radius = radius;
        this.color = color;
    }

    
    public void paintIcon(Vertex vertex, Graphics2D g2, int step) {
        Paint oldPaint = g2.getPaint();
        
        double scale = radius * (step - 2);
        
        g2.setPaint(color);
        g2.scale(scale, scale);
        g2.fill(SHAPE);
        g2.scale(1.0 / scale, 1.0 / scale);
        g2.setPaint(oldPaint);
    }
    
    
    private static Shape SHAPE;
    
    
    static {
        GeneralPath gp = new GeneralPath();
        
        float v1 = -1;
        float v2 = v1 / 5;
        float v3 = -v2;
        float v4 = -v1;
        
        gp.moveTo(v1, v2);
        gp.lineTo(v2, v2);
        gp.lineTo(v2, v1);
        gp.lineTo(v3, v1);
        gp.lineTo(v3, v2);
        gp.lineTo(v4, v2);
        gp.lineTo(v4, v3);
        gp.lineTo(v3, v3);
        gp.lineTo(v3, v4);
        gp.lineTo(v2, v4);
        gp.lineTo(v2, v3);
        gp.lineTo(v1, v3);
        gp.closePath();
                
        SHAPE = gp;
    }
}

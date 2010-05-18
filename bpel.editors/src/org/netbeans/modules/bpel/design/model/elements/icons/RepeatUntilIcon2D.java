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


package org.netbeans.modules.bpel.design.model.elements.icons;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import org.netbeans.modules.bpel.design.geometry.Triangle;

/**
 *
 * @author anjeleevich
 */
public class RepeatUntilIcon2D extends Icon2D {
    
    private RepeatUntilIcon2D() {}
    
    
    public void paint(Graphics2D g2) {
        g2.setStroke(STROKE);
        g2.setPaint(COLOR);
        g2.draw(SHAPE_1);
        g2.fill(SHAPE_2);
    }
    
    
    public static final Icon2D INSTANCE = new RepeatUntilIcon2D();
    
    
    private static final Shape SHAPE_1;
    private static final Shape SHAPE_2;
    
    
    static {
        final double r = 5;
        final double kt = 5.4;
        final double kn = 2.8;
        
        final double start = -45 + 180;
        final double delta = 270;
        
        SHAPE_1 = new Arc2D.Double(-r, -r, 2 * r, 2 * r, start, delta, 
                Arc2D.OPEN);
        
        double phi = Math.PI * start / 180;
        
        float x = (float) (r * Math.cos(phi));
        float y = (float) (-r * Math.sin(phi));
         
        float tx = (float) (-y / r);
        float ty = (float) (x / r);
        
        float nx = -ty;
        float ny = tx;
        
        tx *= kt;
        ty *= kt;
        nx *= kn;
        ny *= kn;
        
        SHAPE_2 = new Triangle(x + tx, y + ty, x - nx, y - ny, x + nx, y + ny);
    }       
}

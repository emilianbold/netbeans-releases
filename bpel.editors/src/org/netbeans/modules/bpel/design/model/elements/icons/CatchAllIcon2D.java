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
import java.awt.geom.GeneralPath;

/**
 *
 * @author anjeleevich
 */
public class CatchAllIcon2D extends Icon2D {
    
    private CatchAllIcon2D() {}
    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.fill(SHAPE);
    }
    
    public static final Icon2D INSTANCE = new CatchAllIcon2D();
    
    
    private static final Shape SHAPE;
    
    
    static {
        float R = 10;
        float r = 4;
            
        GeneralPath gp = new GeneralPath();
                
        gp.moveTo(0, -R);
        
        for (int i = 1; i < 10; i++) {
            double phi = Math.toRadians(-90 + 36 * i);
            float cos = (float) Math.cos(phi);
            float sin = (float) Math.sin(phi);
            
            if (i % 2 == 1) {
                gp.lineTo(r * cos, r * sin);
            } else {
                gp.lineTo(R * cos, R * sin);
            }
        }
        
        gp.lineTo(0, -R);
        gp.closePath();
        
        SHAPE = gp;
    }    
}

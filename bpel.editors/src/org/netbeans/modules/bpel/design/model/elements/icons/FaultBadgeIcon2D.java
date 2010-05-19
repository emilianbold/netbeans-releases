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
public class FaultBadgeIcon2D extends Icon2D {
    
    private FaultBadgeIcon2D() {}
    
    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.fill(SHAPE);
    }
    
    
    public static final Icon2D INSTANCE = new FaultBadgeIcon2D();
    
    
    private static final Shape SHAPE;
    
    
    static {
        float w = 8f / 2;
        float h = 10f / 2;
        
        float x1 = -w;
        float x2 = -w * 0.67f;
        float x3 = -w * 0.33f;
        float x4 = w * 0.33f;
        float x5 = w * 0.67f;
        float x6 = w;
        
        float y1 = -h;
        float y2 = -h * 0.75f;
        float y3 = 0;
        float y4 = h * 0.75f;
        float y5 = h;
    
        GeneralPath gp = new GeneralPath();

        gp.moveTo(x1, y5);
        gp.lineTo(x2, y2);
        gp.lineTo(x4, y3);
        gp.lineTo(x6, y1);
        gp.lineTo(x5, y4);
        gp.lineTo(x3, y3);
        gp.closePath();
        
        SHAPE = gp;
    }    
}

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
public class XORSmallIcon2D extends Icon2D {
    
    private XORSmallIcon2D() {}

    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.fill(SHAPE);
    }
    
    
    public static final Icon2D INSTANCE = new XORSmallIcon2D();
    
    
    private static final Shape SHAPE;

    
    static {
        final float w = 20f / 4 - 1;
        final float h = 20f / 4 - 1;
        
        final float s = 3.0f;
        
        final float x1 = -w;
        final float x2 = -w + s;
        final float x3 = w - s;
        final float x4 = w;
        
        final float y1 = -h;
        final float y2 = h;
        
        final float rx = s / 2;
        final float ry = h * rx / (w - rx);
        
        GeneralPath gp = new GeneralPath();
        
        gp.moveTo(x1, y1);
        gp.lineTo(x2, y1);
        gp.lineTo(0, -ry);
        gp.lineTo(x3, y1);
        gp.lineTo(x4, y1);
        gp.lineTo(rx, 0);
        gp.lineTo(x4, y2);
        gp.lineTo(x3, y2);
        gp.lineTo(0, ry);
        gp.lineTo(x2, y2);
        gp.lineTo(x1, y2);
        gp.lineTo(-rx, 0);
        gp.closePath();
        
        SHAPE = gp;
    }    
}

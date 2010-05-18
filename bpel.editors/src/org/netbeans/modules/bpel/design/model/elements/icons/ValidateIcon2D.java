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
import java.awt.geom.Rectangle2D;

public class ValidateIcon2D extends Icon2D {
    
    private ValidateIcon2D() {}
    
    public void paint(Graphics2D g2) {
        g2.setStroke(STROKE);
        g2.setPaint(COLOR);
        g2.fill(SHAPE_2);
    }

    public static final Icon2D INSTANCE = new ValidateIcon2D();

    private static final Shape SHAPE_1;
    private static final Shape SHAPE_2;

    static {
        final double phi = Math.toRadians(45);
        final float r = 7.6f;
        final float cos = (float) Math.cos(phi);
        final float sin = (float) Math.sin(phi);
        
        GeneralPath generalPath = new GeneralPath();
        generalPath.moveTo(-r * cos, -r * sin);
        generalPath.lineTo(0, r);
        generalPath.lineTo(r * cos, r * sin);
        SHAPE_1 = generalPath;

        generalPath = new GeneralPath();
        generalPath.moveTo(-r * cos, -r * sin);
        generalPath.lineTo(0, r);
        generalPath.lineTo(r * cos, -r * sin);
        generalPath.lineTo(0, r / 5);
        generalPath.closePath();
        SHAPE_2 = generalPath;
    }
}

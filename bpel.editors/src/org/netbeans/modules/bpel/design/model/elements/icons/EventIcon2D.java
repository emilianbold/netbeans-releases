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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 *
 * @author anjeleevich
 */
public class EventIcon2D extends Icon2D {
    
    
    private EventIcon2D() {}

    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.setStroke(STROKE);
        g2.draw(SHAPE_1);
        g2.draw(SHAPE_2);
        g2.fill(SHAPE_3);
    }
    
    
    public static final Icon2D INSTANCE = new EventIcon2D();
    
    
    private static final Shape SHAPE_1 = new Ellipse2D.Float(-8, -8, 16, 16);
    private static final Shape SHAPE_2 = new Ellipse2D.Float(-6, -6, 12, 12);
    
    private static final Shape SHAPE_3;
    
    static {
        float r1 = 5.0f;
        float r2 = 2.5f;
        
        float cos30 = (float) Math.cos(Math.toRadians(30));
        float sin30 = (float) Math.sin(Math.toRadians(30));
        
        GeneralPath gp;
        
        gp = new GeneralPath();
        gp.moveTo(0, -r1);
        gp.lineTo(r2 * cos30, -r2 * sin30);
        gp.lineTo(r1 * cos30, r1 * sin30);
        gp.lineTo(0, +r2);
        gp.lineTo(-r1 * cos30, r1 * sin30);
        gp.lineTo(-r2 * cos30, -r2 * sin30);
        gp.closePath();

        Area area = new Area(gp);
        
        gp.reset();
        gp.moveTo(0, r1);
        gp.lineTo(-r2 * cos30, r2 * sin30);
        gp.lineTo(-r1 * cos30, -r1 * sin30);
        gp.lineTo(0, -r2);
        gp.lineTo(r1 * cos30, -r1 * sin30);
        gp.lineTo(r2 * cos30, r2 * sin30);
        gp.closePath();
        
        area.add(new Area(gp));
        
        SHAPE_3 = new GeneralPath(area);
    }
}

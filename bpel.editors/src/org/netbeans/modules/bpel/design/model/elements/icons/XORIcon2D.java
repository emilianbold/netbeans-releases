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
import java.awt.geom.GeneralPath;

/**
 *
 * @author anjeleevich
 */
public class XORIcon2D extends Icon2D {
    
    private XORIcon2D() {}
    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.fill(SHAPE);
    }
    
    
    public static final Icon2D INSTANCE = new XORIcon2D();
    
    private static final Shape SHAPE;
    
    static {
        float  x1 = -6;
        float  x2 = 6;
        
        float  y1 = -6.5f;
        float  y2 = +6.5f;
        
        float w = 3.5f;
        
        GeneralPath gp;
        
        gp = new GeneralPath();
        gp.moveTo(x1, y1);
        gp.lineTo(x1 + w, y1);
        gp.lineTo(x2, y2);
        gp.lineTo(x2 - w, y2);
        gp.closePath();
        
        Area a = new Area(gp);

        gp.reset();
        gp.moveTo(x2 - w, y1);
        gp.lineTo(x2, y1);
        gp.lineTo(x1 + w, y2);
        gp.lineTo(x1, y2);
        gp.closePath();
        
        a.add(new Area(gp));
        
        SHAPE = new GeneralPath(a);
    }    
}

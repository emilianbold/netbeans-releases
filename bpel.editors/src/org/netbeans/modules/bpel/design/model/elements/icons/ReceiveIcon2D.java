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

/**
 *
 * @author anjeleevich
 */
public class ReceiveIcon2D extends Icon2D {
    
    private ReceiveIcon2D() {}

    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.setStroke(STROKE);
        g2.draw(SHAPE);
    }

    
    public static final Icon2D INSTANCE = new ReceiveIcon2D();
    
    private static final Shape SHAPE;
    
    static {
        GeneralPath p = new GeneralPath(new Rectangle2D.Float(-7, -4, 14, 8));
        p.moveTo(-6.5f, -3.5f);
        p.lineTo(0, 1);
        p.lineTo(6.5f, -3.5f);
        
        SHAPE = p;
    }
}

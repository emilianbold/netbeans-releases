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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.netbeans.modules.bpel.design.geometry.FStroke;

/**
 *
 * @author anjeleevich
 */
public class PlaceholderIcon2D extends Icon2D {
    

    private PlaceholderIcon2D() {}
    
    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR_1_1);
        g2.setStroke(STROKE_1.createStroke(g2));
        g2.draw(SHAPE_1);
        
        g2.setStroke(STROKE_2.createStroke(g2));
        g2.draw(SHAPE_2);
    }
    
    
    public static final Icon2D INSTANCE = new PlaceholderIcon2D();
    
    
    private static final Shape SHAPE_1 = new Ellipse2D.Float(-10, -10, 20, 20);
    private static final Shape SHAPE_2 = new Ellipse2D.Float(-5, -5, 10, 10);

    private static final FStroke STROKE_1 = new FStroke(1, 3);
    private static final FStroke STROKE_2 = new FStroke(1);
    
    private static final Paint COLOR_1_1 = new Color(0xDCDCDC);
}

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
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.geometry.Triangle;

/**
 *
 * @author anjeleevich
 */
public class CompensateIcon2D extends Icon2D {
    
    private CompensateIcon2D() {}

    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.translate(-1, 0);
        g2.fill(SHAPE_1);
        g2.fill(SHAPE_2);
        g2.translate(1, 0);
    }
    
    public static final Icon2D INSTANCE = new CompensateIcon2D();
    
    private static final Shape SHAPE_1 
            = new Triangle(-6.7, 0, -0.7, 6, -0.7, -6);
    private static final Shape SHAPE_2 = new Triangle(0.3, 0, 6.3, 6, 6.3, -6);
}

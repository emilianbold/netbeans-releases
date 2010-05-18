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
import org.netbeans.modules.bpel.design.model.elements.ContentElement;

/**
 *
 * @author anjeleevich
 */
public class EndEventIcon2D extends Icon2D {
    
    private EndEventIcon2D() {}

    
    public void paint(Graphics2D g2) {
        g2.setPaint(ContentElement.STROKE_COLOR);
        g2.fill(SHAPE);
    }
    
    
    public static final Icon2D INSTANCE = new EndEventIcon2D();

    
    private static final Shape SHAPE;
    
    
    static {
        Area a = new Area(new Ellipse2D.Float(-12, -12, 24, 24));
        a.subtract(new Area(new Ellipse2D.Float(-9.5f, -9.5f, 19, 19)));
        SHAPE = new GeneralPath(a);
    }
}

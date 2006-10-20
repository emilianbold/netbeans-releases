/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.design.model.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.geom.FEllipse;
import org.netbeans.modules.bpel.design.geom.FShape;


public class PlaceHolderElement extends ContentElement {

    public PlaceHolderElement() {
        super(SHAPE);
    }


    public void paint(Graphics2D g2) {
        Shape s = GUtils.convert(getShape());

        GUtils.setPaint(g2, new Color(0xDCDCDC));
        GUtils.setDashedStroke(g2, 1, 3);
        GUtils.draw(g2, s, true);
        
        Point2D center = GUtils.getNormalizedCenter(g2, s);
        
        g2.translate(center.getX(), center.getY());

        GUtils.setSolidStroke(g2, 1);
        GUtils.draw(g2, ELEMENT, false);
        
        g2.translate(-center.getX(), -center.getY());
    }
    
    
    private static final FShape SHAPE = new FEllipse(20);
    
    private static final Shape ELEMENT = new Ellipse2D.Float(-5, -5, 10, 10);
}

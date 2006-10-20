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
import java.awt.TexturePaint;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.ViewProperties;
import org.netbeans.modules.bpel.design.geom.FInsets;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.geom.FShape;


public class ProcessBorder extends BorderElement {


    public ProcessBorder() {
        super(SHAPE, INSETS);
    }
    
    
//    public void updateLabelPosition() {
//        label.setAnchor(TextProperty.Anchor.MIDDLE);
//        label.setY(16 + BASELINE);
//        label.setX(width / 2);
//        
//        float labelWidth = getStringWidth(getLabelText());
//        float labelHeight = getStringHeight();
//        
//        labelMouseCatcher.setX(width / 2 - labelWidth / 2);
//        labelMouseCatcher.setY(16 + BASELINE - getStringBaseLine());
//        labelMouseCatcher.setWidth(labelWidth);
//        labelMouseCatcher.setHeight(labelHeight);        
//    }
    
    public void paint(Graphics2D g2) {
        FRectangle r = (FRectangle) getShape();

        Shape shape = GUtils.convert(r);
        
        GUtils.setPaint(g2, BACKGROUND_COLOR);
        GUtils.fill(g2, shape);

        Rectangle2D header = new Rectangle2D.Float(r.x, r.y, r.width, 32);
        
        GUtils.setPaint(g2, new TexturePaint(ContentElement.GRADIENT_TEXTURE, 
                header));
        GUtils.fill(g2, header);
        
        GUtils.setPaint(g2, STROKE_COLOR);
        GUtils.setSolidStroke(g2, 1);
        GUtils.draw(g2, shape, true);
        GUtils.draw(g2, new Line2D.Float(r.x, r.y + 32, 
                r.x + r.width, r.y + 32), true);
        
        if (isPaintText()) {
            GUtils.setPaint(g2, getTextColor());
            setTextBounds(GUtils.drawCenteredString(g2, getText(),
                    getCenterX(), getY() + 16, getWidth() - 8)); 
        }
    }
    
    
    // Shape constants
    private static final FInsets INSETS = new FInsets(48, 16, 16, 16);
    private static final FShape SHAPE = new FRectangle();
    
    // Rendering constants
    private static final Color STROKE_COLOR = new Color(0xA7A2A7);
    private static final Color BACKGROUND_COLOR = new Color(0xFFFFFF);
}

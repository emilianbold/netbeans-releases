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


package org.netbeans.modules.bpel.design.model.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import org.netbeans.modules.bpel.design.geometry.FInsets;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.geometry.FStroke;

/**
 *
 * @author anjeleevich
 */
public class GroupBorder extends BorderElement {
    
    public GroupBorder() {
        super(SHAPE, INSETS);
    }
    

    public void paint(Graphics2D g2) {
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setPaint(STROKE_COLOR);
        
        g2.setStroke(STROKE.createStroke(g2));
        g2.draw(getShape());
        
        if (isPaintText()) {
            g2.setPaint(getTextColor());
            drawString(g2, getText(), getX() + 6, getY() + 1, getWidth() - 12);
        }
    }


    public void paintThumbnail(Graphics2D g2) {
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setPaint(STROKE_COLOR);
        
        g2.setStroke(STROKE.createSolidStroke(g2));
        g2.draw(getShape());
    }
    
    
    public static final FShape SHAPE = new FRectangle(32, 32, 10);
    public static final FInsets INSETS = new FInsets(16, 12, 16, 12);
    
    private static final Paint STROKE_COLOR = new Color(0xD0D0D0);
    private static FStroke STROKE = new FStroke(1, 3);
}

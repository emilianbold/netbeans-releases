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
import java.awt.TexturePaint;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.geometry.FInsets;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.geometry.FStroke;
import org.netbeans.modules.bpel.design.model.elements.icons.Icon2D;

/**
 *
 * @author anjeleevich
 */
public class ProcessBorder extends BorderElement {
    
    public ProcessBorder() {
        super(SHAPE, INSETS);
    }
    

    public void paint(Graphics2D g2) {
        FShape shape = this.shape;
        
        // draw background
        Rectangle2D headerRectangle = new Rectangle2D.Float(shape.x, shape.y, 
                shape.width, HEADER_HEIGHT);
        g2.setPaint(BACKGROUND);
        g2.fill(shape);
        g2.setPaint(new TexturePaint(GRADIENT_TEXTURE, headerRectangle));
        g2.fill(headerRectangle);
        
        // draw border;
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setPaint(ContentElement.STROKE_COLOR);
        g2.setStroke(STROKE.createStroke(g2));
        g2.draw(shape);
        float x = shape.x;
        float y = shape.y + HEADER_HEIGHT;
        g2.draw(new Line2D.Float(x, y, x + shape.width, y));
        
        if (isPaintText()) {
            g2.setPaint(getTextColor());
            drawCenteredString(g2, getText(), getCenterX(), 
                    getY() + HEADER_HEIGHT / 2, getWidth() - 8); 
        }
    }
    

    public void paintThumbnail(Graphics2D g2) {
        FShape shape = this.shape;
        Rectangle2D headerRectangle = new Rectangle2D.Float(shape.x, shape.y, 
                shape.width, HEADER_HEIGHT);

        g2.setPaint(BACKGROUND);
        g2.fill(shape);
        g2.setPaint(GRADIENT_TEXTURE_COLOR);
        g2.fill(headerRectangle);

        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setPaint(ContentElement.STROKE_COLOR);
        g2.setStroke(STROKE.createStroke(g2));
        
        g2.draw(shape);
        float x = shape.x;
        float y = shape.y + HEADER_HEIGHT;
        g2.draw(new Line2D.Float(x, y, x + shape.width, y));
    }
    
    
    public static final int HEADER_HEIGHT = 24;
    public static final FShape SHAPE = new FRectangle(40 + 12 + 12, HEADER_HEIGHT + 16 + 16);
    public static final FInsets INSETS = new FInsets(16 + HEADER_HEIGHT, 12, 16, 12);

    public static Paint BACKGROUND = new Color(0xFFFFFF);
    private static FStroke STROKE = new FStroke(1);

}

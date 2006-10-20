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
import java.awt.image.BufferedImage;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.ViewProperties;
import org.netbeans.modules.bpel.design.geom.FShape;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;


public abstract class ContentElement extends VisualElement 
        implements Selectable 
{
    

    private boolean selected = false;
    

    public ContentElement(FShape shape) {
        super(shape);
    }
    
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    
    public boolean isSelected() {
        return selected;
    }
    
    
    protected final Shape beforePaint(Graphics2D g2) {
        Shape shape = GUtils.convert(getShape());

        GUtils.setPaint(g2, new TexturePaint(GRADIENT_TEXTURE, shape.getBounds2D()));
        GUtils.fill(g2, shape);

        if (isPaintText()) {
            BorderElement border = null;
            for (Pattern p = getPattern(); p != null; p = p.getParent()) {
                if (p instanceof CompositePattern) {
                    border = ((CompositePattern) p).getBorder();
                    if (border != null) break;
                }
            }
            
            if (border != null) {
                float cx = getCenterX();
                float x1 = border.getX();
                float x2 = x1 + border.getWidth();
                
                float hw = Math.min(Math.abs(x2 - cx), Math.abs(x1 - cx)) - 4;
                
                if (hw > 0) {
                    GUtils.setPaint(g2, getTextColor());
                    setTextBounds(GUtils.drawXCenteredString(g2, getText(), 
                            cx, getY() + getHeight(), hw * 2));
                }
            }
        }
        
        GUtils.setPaint(g2, ICON_COLOR);
        
        return shape;
    }
    
    
    protected final void afterPaint(Graphics2D g2, Shape shape) {
        GUtils.setSolidStroke(g2, 1);
        GUtils.setPaint(g2, STROKE_COLOR);
        GUtils.draw(g2, shape, true);
    }
    
    
    public abstract void paint(Graphics2D g2);
    
    
    public static final Color STROKE_COLOR = new Color(0xA7A2A7);
    public static final Color ICON_COLOR = new Color(0x5668CA);
    
    public static final double[] GRADIENT_YS = { 0, 0.0843, 
            0.1798, 0.7416, 0.9045, 1.0674 };
    
    public static final int[] GRADIENT_COLORS = { 0xA9CDE8, 0xDDEBF6,
            0xFFFFFF, 0xDCE3EF, 0xE7F1F9, 0xDCE3EF };
    
    
    public static final BufferedImage GRADIENT_TEXTURE = GUtils
            .createVerticalGradient(GRADIENT_YS, GRADIENT_COLORS);
    
}

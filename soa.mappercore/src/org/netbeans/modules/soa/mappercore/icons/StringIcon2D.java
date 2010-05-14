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

package org.netbeans.modules.soa.mappercore.icons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import org.netbeans.modules.soa.mappercore.MapperStyle;
import org.netbeans.modules.soa.mappercore.model.Vertex;

/**
 *
 * @author anjeleevich
 */
public class StringIcon2D implements Icon2D {
    
    private double radius;
    private Color color;
    private Shape shape;
    
    /**
     * Simple constructor with only one parameter. 
     * The color and radius are initiated with a default values.
     * @param string
     */
    public StringIcon2D(String string) {
        this(string, MapperStyle.ICON_COLOR, 0.8d);
    }
    
    public StringIcon2D(String string, Color color, double radius) {
        this.radius = radius;
        this.color = color;
        
        Font font = new Font("SansSerif", Font.PLAIN, 20);
        Area area = new Area(font.createGlyphVector(new FontRenderContext(null, 
                true, true), string).getOutline());
        Rectangle2D bounds = area.getBounds2D();
        double k = 2.0 / Math.max(bounds.getWidth(), bounds.getHeight());
        
        shape = new GeneralPath(area.createTransformedArea(AffineTransform
                .getTranslateInstance(-bounds.getCenterX(), -bounds
                .getCenterY())).createTransformedArea(AffineTransform
                .getScaleInstance(k, k)));
    }

    
    public void paintIcon(Vertex vertex, Graphics2D g2, int step) {
        Paint oldPaint = g2.getPaint();
        
        double scale = radius * (step - 2);
        
        g2.setPaint(color);
        g2.scale(scale, scale);
        g2.fill(shape);
        g2.scale(1.0 / scale, 1.0 / scale);
        g2.setPaint(oldPaint);
    }
    
    
    private static Shape SHAPE;
    
    
    static {
    }

}

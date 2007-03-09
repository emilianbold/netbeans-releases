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

package org.netbeans.modules.compapp.casaeditor.graph.awt;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Paints a rectangle, with various border options.
 * @author Josh Sandusky
 */
public class BorderedRectangularPainter extends RectangularChainPainter {
    
    public static final float BORDER_PERIMETER_WIDTH = 3.0f;
    
    
    public BorderedRectangularPainter(RectangularPaintProvider provider, Painter nextPainter) {
        super(provider, nextPainter);
    }
    
    
    public void chainPaint(Graphics2D graphics)
    {
        BorderedRectangularProvider provider = (BorderedRectangularProvider) getProvider();
        Rectangle bounds = provider.getClipRect();
        
        Shape shape = null;
        if (provider.isRounded()) {
            shape = new RoundRectangle2D.Float(
                    bounds.x + 0.5f, 
                    bounds.y + 0.5f, 
                    bounds.width - 1, 
                    bounds.height - 1, 
                    RectangularChainPainter.ARC_WIDTH, 
                    RectangularChainPainter.ARC_HEIGHT);
        } else {
            shape = new Rectangle2D.Float(
                    bounds.x, 
                    bounds.y, 
                    bounds.width, 
                    bounds.height);
        }
        
        if (provider.getBackgroundColor() != null) {
            graphics.setColor(provider.getBackgroundColor());
            graphics.fill(shape);
        }
        
        Rectangle headerRect = provider.getHeaderRect();
        if (headerRect != null) {
            graphics.setColor(provider.getHeaderColor());
            graphics.fill(headerRect);
        }
        
        Stroke originalStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(BORDER_PERIMETER_WIDTH));
        graphics.setColor(provider.getBorderColor());
        graphics.draw(shape);
        graphics.setStroke(originalStroke);
    }
}

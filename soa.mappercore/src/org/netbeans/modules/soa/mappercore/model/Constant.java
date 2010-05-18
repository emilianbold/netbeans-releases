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

package org.netbeans.modules.soa.mappercore.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.CanvasRendererContext;
import org.netbeans.modules.soa.mappercore.MapperStyle;
import org.netbeans.modules.soa.mappercore.graphics.RRectangle;
import org.netbeans.modules.soa.mappercore.graphics.Triangle;

/**
 *
 * @author anjeleevich
 */
public class Constant extends Vertex {

    
    public Constant(Icon icon) {
        this(null, icon);
    }
    
    
    public Constant(Object dataObject, Icon icon) {
        super(dataObject, icon);
    }

    
    public RRectangle createShape(int step) {
        return new RRectangle(0, 0, getWidth() * step, getHeight() * step,
                step, 0, step, 0);
    }
    
    
    public void paint(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY) {
        boolean selected = rendererContext.isSelected(treePath, this);
        final int step = rendererContext.getStep();
        
        int x0 = rendererContext.getGraphX() + getX() * step;
        int y0 = graphY + getY() * step;
        int width = getWidth() * step;
        int height = getHeight() * step;

        double tx = x0 + 0.5;
        double ty = y0 + 0.5;
        
        Rectangle bounds = new Rectangle(width, height);
        
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g2.translate(tx, ty);
        
        RRectangle rrect = new RRectangle(0, 0, step * 3, height, step, 0, step, 0);
        
        g2.setPaint(Color.WHITE);
        
        int x = step * 3;
        int w = width - x;
        
        g2.fillRect(x, 0, w, height);
        g2.setPaint(new TexturePaint(MapperStyle.GRADIENT_TEXTURE, bounds));
        g2.fill(rrect);

        Icon icon = getIcon();
        if (icon != null) {
            double tx2 = step - 0.5;
            double ty2 = Math.round(0.5 * height) - 0.5 
                    - icon.getIconHeight() / 2 + 1;
            
            g2.translate(tx2, ty2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_NORMALIZE);
            icon.paintIcon(rendererContext.getCanvas(), g2, 0, 0);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.translate(-tx2, -ty2);
        }
        
        g2.translate(-0.5, -0.5);
        
        g2.setPaint(MapperStyle.VERTEX_BORDER_COLOR);
        g2.translate(0.5, 0.5);
        for (int y = 2 * step; y < height; y += 2 * step) {
            g2.drawLine(x, y, x + w, y);
        }
        g2.drawLine(x, 0, x, height - 1);
        
        rrect.setBounds(0, 0, width, height);
        
        if (selected) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(MapperStyle.SELECTION_STROKE);
            g2.setPaint(MapperStyle.SELECTION_COLOR);
            g2.draw(rrect);
            g2.setStroke(oldStroke);
        } else {
            g2.setPaint(MapperStyle.VERTEX_BORDER_COLOR);
            g2.draw(rrect);
        }
        
        g2.fill(new Triangle(width - step, height, 
                width, height - step, width, height));
        
        g2.translate(-tx, -ty);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);

        for (int i = getItemCount() - 1; i >= 0; i--) {
            VertexItem item = getItem(i);
            item.paint(g2, treePath, rendererContext, graphY);
        }
        
        paintSourcePin(g2, treePath, rendererContext, graphY);
    }
    
    
    @Override
    public void layout() {
        int itemWidth = getWidth() - 3;
        int itemCount = getItemCount();
        int y = 0;
        
        for (int i = 0; i < itemCount; i++) {
            VertexItem item = getItem(i);
            int itemHeight = (item.isHairline()) ? 0 : 2;
            item.setBounds(3, y, itemWidth, itemHeight);
            y += itemHeight;
        }
        
        setHeight(Math.max(y, 2));
        super.layout();        
    }

    @Override
    public int getMaximumWidth() {
        return 30;
    }
}

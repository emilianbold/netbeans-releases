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

/**
 *
 * @author anjeleevich
 */
public class Operation extends Vertex {

    
    public Operation(Icon icon) {
        this(null, icon);
    }
    

    public Operation(Object dataObject, Icon icon) {
        super(dataObject, icon);
    }
    
    
    @Override
    public int getMinimumWidth() { return 4; }
    @Override
    public int getMaximumWidth() { return 4; }    
    
    
    public RRectangle createShape(int step) {
        return new RRectangle(0, 0, getWidth() * step, getHeight() * step,
                step, step, step, step);
    }
    
    
    public void paint(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY) 
    {
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        
        boolean selected = rendererContext.isSelected(treePath, this);
        
        int step = rendererContext.getStep();
        
        int x0 = rendererContext.getGraphX() + getX() * step;
        int y0 = graphY + getY() * step;
        int w = getWidth() * step;
        int h = getHeight() * step;
        
        double tx = x0 + 0.5;
        double ty = y0 + 0.5;
        
        g2.translate(tx, ty);
        
        Rectangle bounds = new Rectangle(w, h);
        RRectangle rrect = new RRectangle(0, 0, w, h, step, step, step, step);
        g2.setPaint(new TexturePaint(MapperStyle.GRADIENT_TEXTURE, bounds));
        
        g2.fill(rrect);

        Icon icon = getIcon();
        if (icon != null) {
            double tx2 = Math.round(0.5 * w) - 0.5 - icon.getIconWidth() / 2 + 1;
            double ty2 = Math.round(0.5 * h) - 0.5 - icon.getIconHeight() / 2 + 1;
            g2.translate(tx2, ty2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_NORMALIZE);
            icon.paintIcon(rendererContext.getCanvas(), g2, 0, 0);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.translate(-tx2, -ty2);
        }
        
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
        
        g2.translate(-tx, -ty);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        
        for (int i = getItemCount() - 1; i >= 0; i--) {
            getItem(i).paintTargetPin(g2, treePath, rendererContext, graphY);
        }
        
        paintSourcePin(g2, treePath, rendererContext, graphY);
    }
    
    
    @Override
    public void layout() {
        int width = getWidth();
        int itemCount = getItemCount();
        int y = 0;
        
        for (int i = 0; i < itemCount; i++) {
            VertexItem item = getItem(i);
            int itemHeight = (item.isHairline()) ? 0 : 2;
            item.setBounds(0, y, width, itemHeight);
            y += itemHeight;
        }
        
        setHeight(Math.max(2, y));
        super.layout();
    }
}

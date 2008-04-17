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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.CanvasRendererContext;
import org.netbeans.modules.soa.mappercore.MapperStyle;
import org.netbeans.modules.soa.mappercore.graphics.RRectangle;
import org.netbeans.modules.soa.mappercore.graphics.Triangle;
import org.netbeans.modules.soa.mappercore.icons.Icon2D;

/**
 *
 * @author anjeleevich
 */
public class Function extends Vertex {

    
    public Function(Icon icon, String name, String resultText) {
        this(null, icon, name, resultText);
    }
    
    
    public Function(Object dataObject, Icon icon, String name, 
            String resultText) 
    {
        super(dataObject, icon, name, resultText);
    }


    public int getPinY() { return getHeight() - 1; }
    
    
    public RRectangle createShape(int step) {
        return new RRectangle(0, 0, getWidth() * step, 
                getHeight() * step, step, step, 0.0, 0.0);
    }
    
    
    public void paint(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY) 
    {
        boolean selected = rendererContext.isSelected(treePath, this);
        
        int step = rendererContext.getStep();
        
        int x0 = rendererContext.getGraphX() + getX() * step;
        int y0 = graphY + getY() * step;
        int width = getWidth() * step;
        int height = getHeight() * step;

        double tx = x0 + 0.5;
        double ty = y0 + 0.5;
        
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g2.translate(tx, ty);
        
        RRectangle rrect = new RRectangle(0, 0, width, step * 2, 
                step, step, 0.0, 0.0);
        
        g2.setPaint(Color.WHITE);
        
        int y = step * 2;
        int h = height - y;
        
        g2.fillRect(0, y, width, h);
        g2.setPaint(new TexturePaint(MapperStyle.GRADIENT_TEXTURE, 
                new Rectangle(0, 0, width, step * 2)));
        g2.fill(rrect);

        Icon icon = getIcon();
        
        int textX = step;
        
        if (icon != null) {
            double tx2 = step - 0.5;
            double ty2 = step - 0.5 - icon.getIconHeight() / 2 + 1;
            g2.translate(tx2, ty2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_NORMALIZE);
            icon.paintIcon(rendererContext.getCanvas(), g2, 0, 0);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.translate(-tx2, -ty2);
            
            textX += icon.getIconWidth() + 4;
        }
        g2.translate(-tx, -ty);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        
        String name = getName();
        if (name != null) {
            int labelWidth = width - textX - step - 1;
            if (labelWidth > 0) {
                int labelX = x0 + textX;
                int labelY = y0 + 1;
                int labelHeight = step * 2 - 1;
                
                JLabel label = rendererContext.getTextRenderer();
                label.setBounds(0, 0, labelWidth, labelHeight);
                label.setForeground(MapperStyle.ICON_COLOR);
                label.setText(name);
                label.setHorizontalAlignment(JLabel.LEFT);
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
                
                g2.translate(labelX, labelY);
                label.paint(g2);
                g2.translate(-labelX, -labelY);
            }
        }
        
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g2.translate(tx, ty);
        
        Stroke oldStroke = g2.getStroke();
        g2.setPaint(MapperStyle.VERTEX_BORDER_COLOR);
        for (int i = 2; i < getHeight(); i += 2) {
            y = i * step;
            g2.drawLine(0, y, width - 1, y);
        }
        
        rrect.setBounds(0, 0, width, height);
        
        if (selected) {
            g2.setStroke(MapperStyle.SELECTION_STROKE);
            g2.setPaint(MapperStyle.SELECTION_COLOR);
            g2.draw(rrect);
        } else {
            g2.setPaint(MapperStyle.VERTEX_BORDER_COLOR);
            g2.draw(rrect);
        }
        
        g2.fill(new Triangle(width - step, height, 
        width, height - step, width, height));
        
        g2.setStroke(oldStroke);
        g2.translate(-tx, -ty);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        
        for (int i = getItemCount() - 1; i >= 0; i--) {
            VertexItem item = getItem(i);
            item.paintTargetPin(g2, treePath, rendererContext, graphY);
            if (!item.isHairline()) {
                item.paint(g2, treePath, rendererContext, graphY);
            }
        }
        
        paintSourcePin(g2, treePath, rendererContext, graphY);
        
        String resultText = getResultText();
        if (resultText != null) {
            JLabel label = rendererContext.getTextRenderer();
            
            int labelWidth = width - 5 - step;
            
            if (labelWidth > 0) {
                int labelHeight = 2 * step - 1;
                int labelX = x0 + 3;
                int labelY = y0 + height - labelHeight;
                
                label.setHorizontalAlignment(JLabel.RIGHT);
                label.setText(resultText);
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
                label.setForeground(MapperStyle.FUNCTION_RESULT_TEXT_COLOR);
                label.setBounds(0, 0, labelWidth, labelHeight);
                
                g2.translate(labelX, labelY);
                label.paint(g2);
                g2.translate(-labelX, -labelY);
            }
        }
    }
    
    
    public void layout() {
        int y = 2;
        
        int width = getWidth();
        
        int itemCount = getItemCount();
        
        for (int i = 0; i < itemCount; i++) {
            VertexItem item = getItem(i);
            int itemHeight = (item.isHairline()) ? 0 : 2;
            item.setBounds(0, y, width, itemHeight);
            y += itemHeight;
        }
            
        setHeight(y + 2);
        super.layout();
    }
}

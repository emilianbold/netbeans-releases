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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.design.view.widget;

import org.netbeans.api.visual.border.Border;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

/**
 * @author David Kaspar
 */
public class RoundedBorder3D implements Border, MouseActions {
    
    private int radius;
    private int insetWidth;
    private int insetHeight;
    private Color drawColor;
    private int depth = 3;
    
    /**
     *
     * @param radius
     * @param depth
     * @param insetWidth
     * @param insetHeight
     * @param drawColor
     */
    public RoundedBorder3D(int radius, int depth, int insetWidth, int insetHeight, Color drawColor) {
        this.radius = radius;
        this.depth = depth;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.drawColor = drawColor;
    }
    
    public Insets getInsets() {
        return new Insets(insetHeight, insetWidth, insetHeight+depth, insetWidth+depth);
    }
    
    public void paint(Graphics2D gr, Rectangle bounds) {
        if (drawColor != null) {
            Paint oldPaint = gr.getPaint();
            gr.setPaint(drawColor);
            RoundRectangle2D rect = new RoundRectangle2D.Double(bounds.x+0.5f,
                    bounds.y + 0.5f, bounds.width - depth - 1, 
                    bounds.height - depth - 1, radius, radius);
            RoundRectangle2D outerRect = new RoundRectangle2D.Double(
                    bounds.x + depth + 0.5f, bounds.y + depth + 0.5f,
                    bounds.width - depth - 1, bounds.height - depth - 1, radius, radius);
            Area raisedArea = new Area(outerRect);
            raisedArea.subtract(new Area(rect));
            gr.fill(raisedArea);
            gr.draw(rect);
            gr.setPaint(oldPaint);
        }
    }
    
    public boolean isOpaque() {
        return true;
    }

    public void mouseClicked() {
    }

    public void mousePressed() {
    }

    public void mouseReleased() {
    }

    public void mouseEntered() {
    }

    public void mouseExited() {
    }
    
}

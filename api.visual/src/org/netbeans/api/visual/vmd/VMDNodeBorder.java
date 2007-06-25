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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author David Kaspar
 */
class VMDNodeBorder implements Border {

    private Color colorBorder;
    private Insets insets;
    private Stroke stroke;
    private Color color1;
    private Color color2;
    private Color color3;
    private Color color4;
    private Color color5;

    VMDNodeBorder (Color colorBorder, int thickness, Color color1, Color color2, Color color3, Color color4, Color color5) {
        this.colorBorder = colorBorder;
        this.insets = new Insets (thickness, thickness, thickness, thickness);
        this.stroke = new BasicStroke (thickness);
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.color4 = color4;
        this.color5 = color5;
    }

    public Insets getInsets () {
        return insets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        Shape previousClip = gr.getClip ();
        gr.clip (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, 4, 4));

        drawGradient (gr, bounds, color1, color2, 0f, 0.3f);
        drawGradient (gr, bounds, color2, color3, 0.3f, 0.764f);
        drawGradient (gr, bounds, color3, color4, 0.764f, 0.927f);
        drawGradient (gr, bounds, color4, color5, 0.927f, 1f);

        gr.setColor (colorBorder);
        Stroke previousStroke = gr.getStroke ();
        gr.setStroke (stroke);
        gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, 4, 4));
        gr.setStroke (previousStroke);

        gr.setClip (previousClip);
    }

    private void drawGradient (Graphics2D gr, Rectangle bounds, Color color1, Color color2, float y1, float y2) {
        y1 = bounds.y + y1 * bounds.height;
        y2 = bounds.y + y2 * bounds.height;
        gr.setPaint (new GradientPaint (bounds.x, y1, color1, bounds.x, y2, color2));
        gr.fill (new Rectangle.Float (bounds.x, y1, bounds.x + bounds.width, y2));
    }

    public boolean isOpaque () {
        return true;
    }

}

/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.widgets;

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author David Kaspar
 */
class EDMNodeBorder implements Border {

    static final Color COLOR_BORDER = new Color (0x8BA6FD);
    static final Color COLOR_CONNECTION = new Color (0x8BA6FD);
    private static final Insets INSETS = new Insets (1, 1, 1, 1);

    private static final Color COLOR1 = new Color (221, 235, 246);
    private static final Color COLOR2 = new Color (255, 255, 255);
    private static final Color COLOR3 = new Color (214, 235, 255);
    private static final Color COLOR4 = new Color (241, 249, 253);
    private static final Color COLOR5 = new Color (255, 255, 255);
    
    EDMNodeBorder () {
    }

    public Insets getInsets () {
        return INSETS;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        Shape previousClip = gr.getClip ();
        gr.clip (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, 4, 4));

        drawGradient (gr, bounds, COLOR1, COLOR2, 0f, 0.3f);
        drawGradient (gr, bounds, COLOR2, COLOR3, 0.3f, 0.764f);
        drawGradient (gr, bounds, COLOR3, COLOR4, 0.764f, 0.927f);
        drawGradient (gr, bounds, COLOR4, COLOR5, 0.927f, 1f);

        gr.setColor (COLOR_CONNECTION);
        gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, 4, 4));

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

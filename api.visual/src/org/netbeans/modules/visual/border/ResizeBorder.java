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
package org.netbeans.modules.visual.border;

import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author David Kaspar
 */
public final class ResizeBorder implements Border {

    private static final BasicStroke STROKE = new BasicStroke (1.0f, BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[] { 6.0f, 3.0f }, 0.0f);

    private int thickness;
    private Color color;
    private boolean outer;

    public ResizeBorder (int thickness, Color color, boolean outer) {
        this.thickness = thickness;
        this.color = color;
        this.outer = outer;
    }

    public Insets getInsets () {
        return new Insets (thickness, thickness, thickness, thickness);
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);

        Stroke stroke = gr.getStroke ();
        gr.setStroke (STROKE);
        if (outer)
            gr.draw (new Rectangle2D.Double (bounds.x + 0.5, bounds.y + 0.5, bounds.width - 1.0, bounds.height - 1.0));
        else
            gr.draw (new Rectangle2D.Double (bounds.x + thickness + 0.5, bounds.y + thickness + 0.5, bounds.width - thickness - thickness - 1.0, bounds.height - thickness - thickness - 1.0));
        gr.setStroke (stroke);

        gr.fillRect (bounds.x, bounds.y, thickness, thickness);
        gr.fillRect (bounds.x + bounds.width - thickness, bounds.y, thickness, thickness);
        gr.fillRect (bounds.x, bounds.y + bounds.height - thickness, thickness, thickness);
        gr.fillRect (bounds.x + bounds.width - thickness, bounds.y + bounds.height - thickness, thickness, thickness);

        Point center = GeomUtil.center (bounds);
        if (bounds.width >= thickness * 5) {
            gr.fillRect (center.x - thickness / 2, bounds.y, thickness, thickness);
            gr.fillRect (center.x - thickness / 2, bounds.y + bounds.height - thickness, thickness, thickness);
        }
        if (bounds.height >= thickness * 5) {
            gr.fillRect (bounds.x, center.y - thickness / 2, thickness, thickness);
            gr.fillRect (bounds.x + bounds.width - thickness, center.y - thickness / 2, thickness, thickness);
        }
    }

    public boolean isOpaque () {
        return outer;
    }

}

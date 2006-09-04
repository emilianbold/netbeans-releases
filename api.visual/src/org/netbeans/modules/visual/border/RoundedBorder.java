/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author David Kaspar
 */
public final class RoundedBorder implements Border {

    private int arcWidth;
    private int arcHeight;
    private int insetWidth;
    private int insetHeight;
    private Color fillColor;
    private Color drawColor;

    public RoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Color fillColor, Color drawColor) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.fillColor = fillColor;
        this.drawColor = drawColor;
    }

    public Insets getInsets () {
        return new Insets (insetHeight, insetWidth, insetHeight, insetWidth);
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        if (fillColor != null) {
            gr.setColor (fillColor);
            gr.fill (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, arcWidth, arcHeight));
        }
        if (drawColor != null) {
            gr.setColor (drawColor);
            gr.draw (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, arcWidth, arcHeight));
        }
    }

    public boolean isOpaque () {
        return false;
    }

}

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
            gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, arcWidth, arcHeight));
        }
    }

    public boolean isOpaque () {
        return false;
    }

}

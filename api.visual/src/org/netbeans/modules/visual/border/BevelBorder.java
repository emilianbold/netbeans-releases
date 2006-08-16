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

/**
 * @author David Kaspar
 */
public final class BevelBorder implements Border {

    private boolean raised;
    private Color color;

    public BevelBorder (boolean raised, Color color) {
        this.raised = raised;
        this.color = color;
    }

    public Insets getInsets () {
        return new Insets (2, 2, 2, 2);
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);
        int h = bounds.height;
        int w = bounds.width;

        gr.translate (bounds.x, bounds.y);

        gr.setColor (raised ? color.brighter ().brighter () : color.darker ().darker ());
        gr.drawLine (0, 0, 0, h - 2);
        gr.drawLine (1, 0, w - 2, 0);

        gr.setColor (raised ? color.brighter () : color.darker ());
        gr.drawLine (1, 1, 1, h - 3);
        gr.drawLine (2, 1, w - 3, 1);

        gr.setColor (raised ? color.darker ().darker () : color.brighter ().brighter ());
        gr.drawLine (0, h - 1, w - 1, h - 1);
        gr.drawLine (w - 1, 0, w - 1, h - 2);

        gr.setColor (raised ? color.darker () : color.brighter ());
        gr.drawLine (1, h - 2, w - 2, h - 2);
        gr.drawLine (w - 2, 1, w - 2, h - 3);

        gr.translate (- bounds.x, - bounds.y);
    }

    public boolean isOpaque () {
        return true;
    }

}

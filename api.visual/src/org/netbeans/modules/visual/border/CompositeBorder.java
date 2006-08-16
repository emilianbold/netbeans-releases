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
public final class CompositeBorder implements Border {

    private Border[] borders;
    private Insets insets;

    public CompositeBorder (Border... borders) {
        this.borders = borders;

        Insets result = new Insets (0, 0, 0, 0);
        for (Border border : borders) {
            Insets insets = border.getInsets ();
            result.top += insets.top;
            result.left += insets.left;
            result.bottom += insets.bottom;
            result.right += insets.right;
        }
        this.insets = result;
    }

    public Insets getInsets () {
        return insets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        for (Border border : borders) {
            border.paint (gr, new Rectangle (bounds));
            Insets insets = border.getInsets ();
            bounds.x += insets.left;
            bounds.width -= insets.left + insets.right;
            bounds.y += insets.top;
            bounds.height -= insets.top + insets.bottom;
        }
    }

    public boolean isOpaque () {
        for (Border border : borders) {
            if (border.isOpaque ())
                return true;
        }
        return false;
    }

}

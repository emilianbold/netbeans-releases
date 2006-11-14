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

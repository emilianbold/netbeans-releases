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
package org.netbeans.modules.visual.laf;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.laf.LookFeel;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class DefaultLookFeel extends LookFeel {

    private static final Color COLOR_SELECTED = new Color (0x447BCD);
    private static final Color COLOR_HIGHLIGHTED = COLOR_SELECTED.darker ();
    private static final Color COLOR_HOVERED = COLOR_SELECTED.brighter ();
    private static final int MARGIN = 3;
    private static final int ARC = 10;
    private static final int MINI_THICKNESS = 1;

    private static final Border BORDER_NORMAL = BorderFactory.createEmptyBorder (MARGIN, MARGIN);
    private static final Border BORDER_HOVERED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border BORDER_SELECTED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_SELECTED, COLOR_SELECTED.darker ());

    private static final Border MINI_BORDER_NORMAL = BorderFactory.createEmptyBorder (MINI_THICKNESS);
    private static final Border MINI_BORDER_HOVERED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border MINI_BORDER_SELECTED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_SELECTED, COLOR_SELECTED.darker ());

    public Paint getBackground () {
        return Color.WHITE;
    }

    public Color getForeground () {
        return Color.BLACK;
    }

    public Border getBorder (ObjectState state) {
        if (state.isHovered ())
            return BORDER_HOVERED;
        if (state.isSelected ())
            return BORDER_SELECTED;
        if (state.isFocused ())
            return BORDER_HOVERED;
        return BORDER_NORMAL;
    }

    public Border getMiniBorder (ObjectState state) {
        if (state.isHovered ())
            return MINI_BORDER_HOVERED;
        if (state.isSelected ())
            return MINI_BORDER_SELECTED;
        if (state.isFocused ())
            return MINI_BORDER_HOVERED;
        return MINI_BORDER_NORMAL;
    }

    public boolean getOpaque (ObjectState state) {
        return state.isHovered ()  ||  state.isSelected ();
    }

    public Color getLineColor (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return Color.BLACK;
    }

    public Paint getBackground (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return Color.WHITE;
    }

    public Color getForeground (ObjectState state) {
        return state.isSelected () ? Color.WHITE : Color.BLACK;
    }

    public int getMargin () {
        return MARGIN;
    }

}

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
package org.netbeans.api.visual.laf;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

import java.awt.*;

/**
 * This class is defining a reusable LookAndFeel fragments e.g. colors.
 * <p>
 * WARNING: This class is not in final shape and be changed later.
 *
 * @author David Kaspar
 */
public abstract class LookFeel {

    private static final LookFeel DEFAULT = new DefaultLookFeel ();

    /**
     * Creates a default look and feel. The instance can be shared by multiple scenes.
     * @return the default look and feel
     */
    public static LookFeel createDefaultLookFeel () {
        return DEFAULT;
    }

    /**
     * Returns a default scene background
     * @return the default background
     */
    public abstract Paint getBackground ();

    /**
     * Returns a default scene foreground
     * @return the default foreground
     */
    public abstract Color getForeground ();

    /**
     * Returns a border for a specific state.
     * @param state the state
     * @return the border
     */
    public abstract Border getBorder (ObjectState state);

    /**
     * Returns a minimalistic version of border for a specific state.
     * @param state the state
     * @return the mini-border
     */
    public abstract Border getMiniBorder (ObjectState state);

    /**
     * Returns a opacity value for a specific state.
     * @param state the state
     * @return the opacity value
     */
    public abstract boolean getOpaque (ObjectState state);

    /**
     * Returns a line color for a specific state.
     * @param state the state
     * @return the line color
     */
    public abstract Color getLineColor (ObjectState state);

    /**
     * Returns a background for a specific state.
     * @param state the state
     * @return the background
     */
    public abstract Paint getBackground (ObjectState state);

    /**
     * Returns a foreground for a specific state.
     * @param state the state
     * @return the foreground
     */
    public abstract Color getForeground (ObjectState state);

    /**
     * Returns a margin for a specific state. It is used with borders - usually the value is a inset value of a border.
     * @return the margin
     */
    // TODO - is naming correct?
    public abstract int getMargin ();
/*
    public void updateWidget (Widget widget, WidgetState state) {
        widget.setBorder (getBorder (state));
        widget.setOpaque (getOpaque (state));
        widget.setBackground (getBackground (state));
        widget.setForeground (getForeground (state));
    }
*/
}

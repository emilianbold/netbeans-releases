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
package org.netbeans.api.visual.widget;

import java.awt.*;

/**
 * This is a separator widget. Renders a rectangle that is usually expand across the width or height of the parent widget
 * based on an orientation.
 *
 * @author David Kaspar
 */
public class SeparatorWidget extends Widget {

    /**
     * The separator orientation
     */
    public static enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private Orientation orientation;
    private int thickness;

    /**
     * Creates a separator widget.
     * @param scene the scene
     * @param orientation the separator orientation
     */
    public SeparatorWidget (Scene scene, Orientation orientation) {
        super (scene);
        assert orientation != null;
        this.orientation = orientation;
        thickness = 1;
    }

    /**
     * Returns a separator orientation
     * @return the separator orientation
     */
    public Orientation getOrientation () {
        return orientation;
    }

    /**
     * Sets a separator orientation
     * @param orientation the separator orientation
     */
    public void setOrientation (Orientation orientation) {
        assert orientation != null;
        this.orientation = orientation;
        revalidate();
    }

    /**
     * Returns a thickness of the separator.
     * @return the thickness
     */
    public int getThickness () {
        return thickness;
    }

    /**
     * Sets a thickness of the seperator.
     * @param thickness the thickness
     */
    public void setThickness (int thickness) {
        assert thickness >= 0;
        this.thickness = thickness;
        revalidate();
    }

    /**
     * Calculates a client area of the separator widget.
     * @return the calculated client area
     */
    protected Rectangle calculateClientArea () {
        if (orientation == Orientation.HORIZONTAL)
            return new Rectangle (0, 0, 0, thickness);
        else
            return new Rectangle (0, 0, thickness, 0);
    }

    /**
     * Paints the separator widget.
     */
    protected void paintWidget() {
        Graphics2D gr = getGraphics();
        gr.setColor (getForeground());
        Rectangle bounds = getBounds ();
        Insets insets = getBorder ().getInsets ();
        if (orientation == Orientation.HORIZONTAL)
            gr.fillRect (0, 0, bounds.width - insets.left - insets.right, thickness);
        else
            gr.fillRect (0, 0, thickness, bounds.height - insets.top - insets.bottom);
    }
    
}

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
 * @author David Kaspar
 */
public class SeparatorWidget extends Widget {

    public static enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private Orientation orientation;
    private int thickness;

    public SeparatorWidget (Scene scene, Orientation orientation) {
        super (scene);
        assert orientation != null;
        this.orientation = orientation;
        thickness = 1;
    }
    
    public Orientation getOrientation () {
        return orientation;
    }
    
    public void setOrientation (Orientation orientation) {
        assert orientation != null;
        this.orientation = orientation;
        revalidate();
    }
    
    public int getThickness () {
        return thickness;
    }

    public void setThickness (int thickness) {
        assert thickness >= 0;
        this.thickness = thickness;
        revalidate();
    }

    protected Rectangle calculateClientArea () {
        if (orientation == Orientation.HORIZONTAL)
            return new Rectangle (0, 0, 0, thickness);
        else
            return new Rectangle (0, 0, thickness, 0);
    }
    
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

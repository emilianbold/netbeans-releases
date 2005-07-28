/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ThinBevelBorder extends AbstractBorder {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static int WIDTH = 1;
    public static final int RAISED = 0;
    public static final int LOWERED = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private int bevelType;
    private Color highlightColor;
    private Color shadowColor;

    /**
     *
     *
     */
    public ThinBevelBorder(int type) {
        super();
        this.bevelType = type;
    }

    /**
     *
     *
     */
    public ThinBevelBorder(int type, Color highlight, Color shadow) {
        this(type);
        highlightColor = highlight;
        shadowColor = shadow;
    }

    /**
     *
     *
     */
    public int getBevelType() {
        return bevelType;
    }

    /**
     *
     *
     */
    public void setBevelType(int value) {
        bevelType = value;
    }

    /**
     *
     *
     */
    public Insets getBorderInsets(Component component) {
        return new Insets(WIDTH, WIDTH, WIDTH, WIDTH);
    }

    /**
     *
     *
     */
    public Insets getBorderInsets(Component component, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = WIDTH;

        return insets;
    }

    /**
     * Returns the outer highlight color of the bevel border.
     * Will return null if no highlight color was specified
     * at instantiation.
     */
    public Color getHighlightColor() {
        return highlightColor;
    }

    /**
     * Returns the inner shadow color of the bevel border.
     * Will return null if no shadow color was specified
     * at instantiation.
     */
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * Returns the outer highlight color of the bevel border
     * when rendered on the specified component.  If no highlight
     * color was specified at instantiation, the highlight color
     * is derived from the specified component's background color.
     *
     * @param        component
     *                        The component for which the highlight may be derived
     */
    public Color getHighlightColor(Component component) {
        Color highlight = getHighlightColor();

        return (highlight != null) ? highlight : component.getBackground().brighter().brighter();
    }

    /**
     * Returns the outer shadow color of the bevel border
     * when rendered on the specified component.  If no shadow
     * color was specified at instantiation, the shadow color
     * is derived from the specified component's background color.
     *
     * @param        component
     *                        The component for which the shadow may be derived
     */
    public Color getShadowColor(Component component) {
        Color shadow = getShadowColor();

        return (shadow != null) ? shadow : component.getBackground().darker().darker();
    }

    /**
     *
     *
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (getBevelType() == LOWERED) {
            paintLoweredBevel(c, g, x, y, width, height);
        } else {
            paintRaisedBevel(c, g, x, y, width, height);
        }
    }

    /**
     *
     *
     */
    protected void paintRaisedBevel(Component component, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.translate(x, y);

        g.setColor(getHighlightColor(component));

        // Left
        g.drawLine(0, 0, 0, height - WIDTH);

        // Top
        g.drawLine(WIDTH, 0, width - WIDTH, 0);

        g.setColor(getShadowColor(component));

        // Bottom
        g.drawLine(0, height - WIDTH, width - WIDTH, height - WIDTH);

        // Right
        g.drawLine(width - WIDTH, 0, width - WIDTH, height - WIDTH);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    /**
     *
     *
     */
    protected void paintLoweredBevel(Component component, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.translate(x, y);

        g.setColor(getShadowColor(component));

        // Left
        g.drawLine(0, 0, 0, height - WIDTH);

        // Top
        g.drawLine(WIDTH, 0, width - WIDTH, 0);

        g.setColor(getHighlightColor(component));

        // Bottom
        g.drawLine(0, height - WIDTH, width - WIDTH, height - WIDTH);

        // Right
        g.drawLine(width - WIDTH, 0, width - WIDTH, height - WIDTH);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }
}

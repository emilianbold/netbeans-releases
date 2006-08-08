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
package org.netbeans.api.visual.border;

import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - check insets values
public final class BorderFactory {

    private static final Border BORDER_EMPTY = new EmptyBorder (0, 0, 0, 0);
    private static final Border BORDER_LINE = createLineBorder (1);

    private BorderFactory () {
    }

    public static Border createEmptyBorder () {
        return BORDER_EMPTY;
    }

    public static Border createEmptyBorder (int thickness) {
        return thickness > 0 ? createEmptyBorder (thickness, thickness, thickness, thickness) : BORDER_EMPTY;
    }

    public static Border createEmptyBorder (int horizontal, int vertical) {
        return createEmptyBorder (vertical, horizontal, vertical, horizontal);
    }

    public static Border createEmptyBorder (int top, int left, int bottom, int right) {
        return new EmptyBorder (top, left, bottom, right);
    }

    public static Border createCompositeBorder (Border... borders) {
        return new CompositeBorder (borders);
    }

    public static Border createSwingBorder (Scene scene, javax.swing.border.Border border) {
        assert scene != null && scene.getComponent () != null && border != null;
        return new SwingBorder (scene, border);
    }

    public static Border createLineBorder () {
        return BORDER_LINE;
    }

    public static Border createLineBorder (int thickness) {
        return createLineBorder (thickness, null);
    }

    public static Border createLineBorder (int thickness, Color color) {
        return new LineBorder (thickness, color != null ? color : Color.BLACK);
    }

    public static Border createBevelBorder (boolean raised) {
        return createBevelBorder (raised, null);
    }

    public static Border createBevelBorder (boolean raised, Color color) {
        return new BevelBorder (raised, color != null ? color : Color.GRAY);
    }

    public static Border createImageBorder (Insets insets, Image image) {
        return createImageBorder (insets, insets, image);
    }

    public static Border createImageBorder (Insets borderInsets, Insets imageInsets, Image image) {
        assert borderInsets != null  &&  imageInsets != null  &&  image != null;
        return new ImageBorder (borderInsets, imageInsets, image);
    }

    public static Border createRoundedBorder (int arcWidth, int arcHeight, Color fillColor, Color drawColor) {
        return createRoundedBorder (arcWidth, arcHeight, arcWidth, arcHeight, fillColor, drawColor);
    }

    public static Border createRoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Color fillColor, Color drawColor) {
        return new RoundedBorder (arcWidth, arcHeight, insetWidth, insetHeight, fillColor, drawColor);
    }

    public static Border createResizeBorder (int thickness) {
        return createResizeBorder (thickness, null, false);
    }

    public static Border createResizeBorder (int thickness, Color color, boolean outer) {
        return new ResizeBorder (thickness, color != null ? color : Color.BLACK, outer);
    }

    public static Border createDashedBorder (Color color, int width, int height) {
        return new DashedBorder (color != null ? color : Color.BLACK, width, height);
    }

    public static Border createFancyDashedBorder (Color color, int width, int height) {
        return new FancyDashedBorder (color != null ? color : Color.BLACK, width, height);
    }

}

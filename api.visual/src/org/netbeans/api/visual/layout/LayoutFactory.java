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
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.layout.*;

/**
 * This class is a factory of all built-in layouts.
 *
 * @author David Kaspar
 */
public final class LayoutFactory {

    private static final AbsoluteLayout LAYOUT_ABSOLUTE = new AbsoluteLayout ();
    private static final FillLayout LAYOUT_FILL = new FillLayout ();

    /**
     * Alignment of children widgets within a calculated widget used by SerialLayout (vertical and horizontal box layout).
     */
    public static enum SerialAlignment {

        LEFT_TOP, CENTER, RIGHT_BOTTOM, JUSTIFY
    }

    /**
     * Alignment of children widgets within a calculated connection widgets used by default layout used in a connection widget.
     */
    public enum ConnectionWidgetLayoutAlignment {

        NONE, CENTER, TOP_CENTER, BOTTOM_CENTER, CENTER_LEFT, CENTER_RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private LayoutFactory () {
    }

    /**
     * Creates an absolute layout where widgets are located at placed defined by their preferredLocation.
     * @return the absolute layout
     */
    public static Layout createAbsoluteLayout () {
        return LAYOUT_ABSOLUTE;
    }

    /**
     * Creates a vertical box layout with default style where widgets are placed vertically one to the bottom from another.
     * @return the vertical box layout
     */
    public static Layout createVerticalLayout () {
        return createVerticalLayout (null, 0);
    }

    /**
     * Creates a vertical box layout with a specific style where widgets are placed vertically one to the bottom from another.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the vertical box layout
     */
    public static Layout createVerticalLayout (SerialAlignment alignment, int gap) {
        return new SerialLayout (true, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    /**
     * Creates a horizontal box layout with default style where widgets are placed horizontally one to the right from another.
     * @return the horizontal box layout
     */
    public static Layout createHorizontalLayout () {
        return createHorizontalLayout (null, 0);
    }

    /**
     * Creates a horizontal box layout with a specific style where widgets are placed horizontally one to the right from another.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the horizontal box layout
     */
    public static Layout createHorizontalLayout (SerialAlignment alignment, int gap) {
        return new SerialLayout (false, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    /**
     * Creates a card layout where all children widgets except the active one are hidden. The active one is the only shown.
     * The active widget could be managed using LayoutFactory.getActiveCard and LayoutFactory.setActiveCard methods.
     * @param cardLayoutWidget the widget where the card layout is going to be used
     * @return the card layout
     */
    public static Layout createCardLayout (Widget cardLayoutWidget) {
        assert cardLayoutWidget != null;
        return new CardLayout (cardLayoutWidget);
    }

    /**
     * Returns active card of a specified widget where a card layout is used.
     * @param cardLayoutWidget the widget with card layout
     * @return the active widget
     */
    public static Widget getActiveCard (Widget cardLayoutWidget) {
        Layout layout = cardLayoutWidget.getLayout ();
        return layout instanceof CardLayout ? ((CardLayout) layout).getActiveChildWidget () : null;
    }

    /**
     * Sets active card of a specified widget where a card layout is used.
     * @param widget the widget with card layout
     * @param activeChildWidget the new active widget
     */
    public static void setActiveCard (Widget widget, Widget activeChildWidget) {
        Layout layout = widget.getLayout ();
        if (layout instanceof CardLayout)
            ((CardLayout) layout).setActiveChildWidget (activeChildWidget);
    }

    /**
     * Returns a fill layout where all children widgets has the boundary at the biggest one of them or
     * they are expanded to the parent widget boundaries during justification.
     * @return the fill layout
     */
    public static Layout createFillLayout () {
        return LAYOUT_FILL;
    }

    /**
     * Returns a scene layout which performs one-time layout using specified devolve-layout.
     * @param widget the
     * @param devolveLayout the layout that is going to be used for one-time layout
     * @param animate if true, then setting preferredLocation is gone animated
     * @return the scene layout
     */
    public static SceneLayout createDevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
        return new DevolveWidgetLayout (widget, devolveLayout, animate);
    }

}

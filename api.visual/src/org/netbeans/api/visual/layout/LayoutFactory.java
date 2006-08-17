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
import org.netbeans.modules.visual.layout.AbsoluteLayout;
import org.netbeans.modules.visual.layout.CardLayout;
import org.netbeans.modules.visual.layout.SerialLayout;
import org.netbeans.modules.visual.layout.FillLayout;

/**
 * @author David Kaspar
 */
public final class LayoutFactory {

    private static final AbsoluteLayout LAYOUT_ABSOLUTE = new AbsoluteLayout ();
    private static final FillLayout LAYOUT_FILL = new FillLayout ();

    public static enum SerialAlignment {

        LEFT_TOP, CENTER, RIGHT_BOTTOM, JUSTIFY
    }

    public enum ConnectionWidgetLayoutAlignment {

        NONE, CENTER, TOP_CENTER, BOTTOM_CENTER, CENTER_LEFT, CENTER_RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private LayoutFactory () {
    }

    public static Layout createAbsoluteLayout () {
        return LAYOUT_ABSOLUTE;
    }

    public static Layout createVerticalLayout () {
        return createVerticalLayout (null, 0);
    }

    public static Layout createVerticalLayout (SerialAlignment alignment, int gap) {
        return new SerialLayout (true, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    public static Layout createHorizontalLayout () {
        return createHorizontalLayout (null, 0);
    }

    public static Layout createHorizontalLayout (SerialAlignment alignment, int gap) {
        return new SerialLayout (false, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    public static Layout createCardLayout (Widget cardLayoutWidget) {
        assert cardLayoutWidget != null;
        return new CardLayout (cardLayoutWidget);
    }

    public static Widget getActiveCard (Widget cardLayoutWidget) {
        Layout layout = cardLayoutWidget.getLayout ();
        return layout instanceof CardLayout ? ((CardLayout) layout).getActiveChildWidget () : null;
    }

    public static void setActiveCard (Widget widget, Widget activeChildWidget) {
        Layout layout = widget.getLayout ();
        if (layout instanceof CardLayout)
            ((CardLayout) layout).setActiveChildWidget (activeChildWidget);
    }

    public static Layout createFillLayout () {
        return LAYOUT_FILL;
    }

}

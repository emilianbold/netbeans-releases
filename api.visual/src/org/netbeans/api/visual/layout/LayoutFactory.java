/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.ConnectionWidget;

/**
 * @author David Kaspar
 */
public final class LayoutFactory {

    private static final AbsoluteLayout LAYOUT_ABSOLUTE = new AbsoluteLayout ();

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
        return new SerialLayout (SerialLayout.Orientation.VERTICAL, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    public static Layout createHorizontalLayout () {
        return createHorizontalLayout (null, 0);
    }

    public static Layout createHorizontalLayout (SerialAlignment alignment, int gap) {
        return new SerialLayout (SerialLayout.Orientation.HORIZONTAL, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
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

    @Deprecated
    public static Layout createConnectionWidgetLayout (ConnectionWidget connectionWidget) {
        assert connectionWidget != null;
        return new ConnectionWidgetLayout (connectionWidget);
    }

    @Deprecated
    public static void setConstraint (ConnectionWidget connectionWidget, Widget childWidget, ConnectionWidgetLayoutAlignment alignment, float placementInPercentage) {
        Layout layout = connectionWidget.getLayout ();
        if (layout instanceof ConnectionWidgetLayout)
            ((ConnectionWidgetLayout) layout).setConstraint (childWidget, alignment, placementInPercentage);
    }

    @Deprecated
    public static void setConstraint (ConnectionWidget connectionWidget, Widget childWidget, ConnectionWidgetLayoutAlignment alignment, int placementAtDistance) {
        Layout layout = connectionWidget.getLayout ();
        if (layout instanceof ConnectionWidgetLayout)
            ((ConnectionWidgetLayout) layout).setConstraint (childWidget, alignment, placementAtDistance);

    }

    @Deprecated
    public static void removeConstraint (ConnectionWidget connectionWidget, Widget childWidget) {
        Layout layout = connectionWidget.getLayout ();
        if (layout instanceof ConnectionWidgetLayout)
            ((ConnectionWidgetLayout) layout).removeConstraint (childWidget);
    }

}

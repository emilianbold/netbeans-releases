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
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GraphLayout;
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
     * The instance can be shared by multiple widgets.
     * @return the absolute layout
     */
    public static Layout createAbsoluteLayout () {
        return LAYOUT_ABSOLUTE;
    }

    /**
     * Creates a vertical box layout with default style where widgets are placed vertically one to the bottom from another.
     * The instance can be shared by multiple widgets.
     * @return the vertical box layout
     */
    public static Layout createVerticalLayout () {
        return createVerticalLayout (null, 0);
    }

    /**
     * Creates a vertical box layout with a specific style where widgets are placed vertically one to the bottom from another.
     * The instance can be shared by multiple widgets.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the vertical box layout
     */
    public static Layout createVerticalLayout (SerialAlignment alignment, int gap) {
        return new SerialLayout (true, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    /**
     * Creates a horizontal box layout with default style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * @return the horizontal box layout
     */
    public static Layout createHorizontalLayout () {
        return createHorizontalLayout (null, 0);
    }

    /**
     * Creates a horizontal box layout with a specific style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
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
     * The instance cannot be shared.
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
     * The instance can be shared by multiple widgets.
     * @return the fill layout
     */
    public static Layout createFillLayout () {
        return LAYOUT_FILL;
    }

    /**
     * Returns a scene layout which performs one-time layout using specified devolve-layout.
     * The instance cannot be shared.
     * @param widget the
     * @param devolveLayout the layout that is going to be used for one-time layout
     * @param animate if true, then setting preferredLocation is gone animated
     * @return the scene layout
     */
    public static SceneLayout createDevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
        return new DevolveWidgetLayout (widget, devolveLayout, animate);
    }

    /**
     * Creates a scene layout which performs a specified graph-oriented layout on a specified GraphScene.
     * @param graphScene the graph scene
     * @param graphLayout the graph layout
     * @return the scene layout
     */
    public static <N,E> SceneLayout createSceneGraphLayout (final GraphScene<N,E> graphScene, final GraphLayout<N,E> graphLayout) {
        assert graphScene != null  &&  graphLayout != null;
        return new SceneLayout(graphScene) {
            protected void performLayout () {
                graphLayout.layoutGraph (graphScene);
            }
        };
    }

    /**
     * Creates a scene layout which performs a specified graph-oriented layout on a specified GraphPinScene.
     * @param graphPinScene the graph pin scene
     * @param graphLayout the graph layout
     * @return the scene layout
     */
    public static <N,E> SceneLayout createSceneGraphLayout (final GraphPinScene<N,?,E> graphPinScene, final GraphLayout<N,E> graphLayout) {
        assert graphPinScene != null && graphLayout != null;
        return new SceneLayout(graphPinScene) {
            protected void performLayout () {
                graphLayout.layoutGraph (graphPinScene);
            }
        };
    }

}

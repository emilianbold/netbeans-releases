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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.layout.Layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;

/**
 * This is a scroll widget similar to JScrollPane. In comparison with the ScrollWidget class, this class is using JScrollBar
 * for vertical and horizontal scroll bars.
 * @author David Kaspar
 */
public class SwingScrollWidget extends Widget {

    private static final int BAR_VERTICAL_SIZE = 16;
    private static final int BAR_HORIZONTAL_SIZE = 16;

    private Widget viewport;
    private Widget view;

    private ComponentWidget verticalWidget;
    private ComponentWidget horizontalWidget;

    private JScrollBar verticalScroll;
    private JScrollBar horizontalScroll;

    private AdjustmentListener verticalListener = new MyAdjustmentListener (true);
    private AdjustmentListener horizontalListener = new MyAdjustmentListener (false);

    /**
     * Creates a scroll widget.
     * @param scene
     */
    public SwingScrollWidget (Scene scene) {
        super (scene);

        setLayout (new SwingScrollWidget.ScrollLayout ());
        setCheckClipping (true);

        viewport = new Widget (scene);
        viewport.setCheckClipping (true);
        addChild (viewport);

        verticalScroll = new JScrollBar (JScrollBar.VERTICAL);
        verticalScroll.setUnitIncrement (16);
        verticalScroll.setBlockIncrement (64);
        verticalWidget = new ComponentWidget (scene, verticalScroll);

        horizontalScroll = new JScrollBar (JScrollBar.HORIZONTAL);
        horizontalScroll.setUnitIncrement (16);
        horizontalScroll.setBlockIncrement (64);
        horizontalWidget = new ComponentWidget (scene, horizontalScroll);
    }

    /**
     * Creates a scroll widget.
     * @param scene the scene
     * @param view  the scrolled view
     */
    public SwingScrollWidget (Scene scene, Widget view) {
        this (scene);
        setView (view);
    }

    /**
     * Returns an inner widget.
     * @return the inner widget
     */
    public final Widget getView () {
        return view;
    }

    /**
     * Sets an scrolled widget.
     * @param view the scrolled widget
     */
    public final void setView (Widget view) {
        if (this.view != null)
            viewport.removeChild (this.view);
        this.view = view;
        if (this.view != null)
            viewport.addChild (this.view);
    }

    /**
     * Calculates a client area as from the scroll widget preferred bounds.
     * @return the calculated client area
     */
    protected Rectangle calculateClientArea () {
        return new Rectangle (calculateSize ());
    }

    private Dimension calculateSize () {
        if (isPreferredBoundsSet ()) {
            Rectangle preferredBounds = getPreferredBounds ();
            Insets insets = getBorder ().getInsets ();
            return new Dimension (preferredBounds.width - insets.left - insets.right, preferredBounds.height - insets.top - insets.bottom);
        } else {
            Dimension size = view.getBounds ().getSize ();

            Dimension minimumSize = getMinimumSize ();
            if (minimumSize != null) {
                if (size.width < minimumSize.width)
                    size.width = minimumSize.width;
                if (size.height < minimumSize.height)
                    size.height = minimumSize.height;
            }

            Dimension maximumSize = getMaximumSize ();
            if (maximumSize != null) {
                if (size.width > maximumSize.width)
                    size.width = maximumSize.width;
                if (size.height > maximumSize.height)
                    size.height = maximumSize.height;
            }

            return size;
        }
    }

    private final class ScrollLayout implements Layout {

        public void layout (Widget widget) {
            Point scrollWidgetClientAreaLocation;
            if (isPreferredBoundsSet ()) {
                scrollWidgetClientAreaLocation = getPreferredBounds ().getLocation ();
                Insets insets = getBorder ().getInsets ();
                scrollWidgetClientAreaLocation.translate (insets.left, insets.top);
            } else
                scrollWidgetClientAreaLocation = new Point ();

            Rectangle viewBounds = view != null ? view.getPreferredBounds () : new Rectangle ();
            Rectangle viewportBounds = view != null ? new Rectangle (view.getLocation (), calculateSize ()) : new Rectangle ();

            boolean showVertical = checkVertical (viewBounds, viewportBounds);
            boolean showHorizontal = checkHorizontal (viewBounds, viewportBounds);
            if (showVertical) {
                viewportBounds.width -= SwingScrollWidget.BAR_HORIZONTAL_SIZE;
                showHorizontal = checkHorizontal (viewBounds, viewportBounds);
            }
            if (showHorizontal) {
                viewportBounds.height -= SwingScrollWidget.BAR_VERTICAL_SIZE;
                if (! showVertical) {
                    showVertical = checkVertical (viewBounds, viewportBounds);
                    if (showVertical)
                        viewportBounds.width -= SwingScrollWidget.BAR_HORIZONTAL_SIZE;
                }
            }

            viewport.resolveBounds (scrollWidgetClientAreaLocation, new Rectangle (viewportBounds.getSize ()));

            int x1 = scrollWidgetClientAreaLocation.x;
            int x2 = scrollWidgetClientAreaLocation.x + viewportBounds.width;
            int y1 = scrollWidgetClientAreaLocation.y;
            int y2 = scrollWidgetClientAreaLocation.y + viewportBounds.height;

            if (showVertical) {
                if (verticalWidget.getParentWidget () == null)
                    addChild (verticalWidget);
                verticalWidget.resolveBounds (new Point (x2, y1), new Rectangle (SwingScrollWidget.BAR_HORIZONTAL_SIZE, viewportBounds.height));
            } else {
                if (verticalWidget.getParentWidget () != null)
                    removeChild (verticalWidget);
            }

            if (showHorizontal) {
                if (horizontalWidget.getParentWidget () == null)
                    addChild (horizontalWidget);
                horizontalWidget.resolveBounds (new Point (x1, y2), new Rectangle (viewportBounds.width, SwingScrollWidget.BAR_VERTICAL_SIZE));
            } else {
                if (horizontalWidget.getParentWidget () != null)
                    removeChild (horizontalWidget);
            }

            verticalScroll.removeAdjustmentListener (verticalListener);
            verticalScroll.setValues (- viewportBounds.y, viewportBounds.height, viewBounds.y, viewBounds.y + viewBounds.height);
            verticalScroll.addAdjustmentListener (verticalListener);

            horizontalScroll.removeAdjustmentListener (horizontalListener);
            horizontalScroll.setValues (- viewportBounds.x, viewportBounds.width, viewBounds.x, viewBounds.x + viewBounds.width);
            horizontalScroll.addAdjustmentListener (horizontalListener);
        }

        public boolean requiresJustification (Widget widget) {
            return false;
        }

        public void justify (Widget widget) {
        }

        private boolean checkHorizontal (Rectangle viewBounds, Rectangle viewportBounds) {
            return (viewBounds.x < viewportBounds.x  ||  viewBounds.x + viewBounds.width > viewportBounds.x + viewportBounds.width)  &&  viewportBounds.width > 3 * SwingScrollWidget.BAR_HORIZONTAL_SIZE;
        }

        private boolean checkVertical (Rectangle viewBounds, Rectangle viewportBounds) {
            return (viewBounds.y < viewportBounds.y  ||  viewBounds.y + viewBounds.height > viewportBounds.y + viewportBounds.height)  &&  viewportBounds.height > 3 * SwingScrollWidget.BAR_VERTICAL_SIZE;
        }

    }

    private class MyAdjustmentListener implements AdjustmentListener {

        private boolean vertical;

        private MyAdjustmentListener (boolean vertical) {
            this.vertical = vertical;
        }

        public void adjustmentValueChanged (AdjustmentEvent e) {
            Point location = view.getLocation ();
            if (vertical)
                location.y = - verticalScroll.getValue ();
            else
                location.x = - horizontalScroll.getValue ();
            view.setPreferredLocation (location);
            getScene ().validate ();
        }
    }

}

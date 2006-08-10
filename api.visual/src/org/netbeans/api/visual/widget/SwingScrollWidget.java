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

import org.netbeans.api.visual.layout.Layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;

/**
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

    private Dimension minimalSize, maximalSize;

    private AdjustmentListener verticalListener = new MyAdjustmentListener (true);
    private AdjustmentListener horizontalListener = new MyAdjustmentListener (false);

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

    public final Widget getView () {
        return view;
    }

    public final void setView (Widget view) {
        if (this.view != null)
            viewport.removeChild (this.view);
        this.view = view;
        if (this.view != null)
            viewport.addChild (this.view);
    }

    public Dimension getMinimalSize () {
        return minimalSize;
    }

    public void setMinimalSize (Dimension minimalSize) {
        this.minimalSize = minimalSize;
        revalidate ();
    }

    public Dimension getMaximalSize () {
        return maximalSize;
    }

    public void setMaximalSize (Dimension maximalSize) {
        this.maximalSize = maximalSize;
        revalidate ();
    }

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

            Dimension minimalSize = getMinimalSize ();
            if (minimalSize != null) {
                if (size.width < minimalSize.width)
                    size.width = minimalSize.width;
                if (size.height < minimalSize.height)
                    size.height = minimalSize.height;
            }

            Dimension maximalSize = getMaximalSize ();
            if (maximalSize != null) {
                if (size.width > maximalSize.width)
                    size.width = maximalSize.width;
                if (size.height > maximalSize.height)
                    size.height = maximalSize.height;
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

        private boolean checkHorizontal (Rectangle viewBounds, Rectangle viewportBounds) {
            return (viewBounds.x < viewportBounds.x  ||  viewBounds.x + viewBounds.width > viewportBounds.x + viewportBounds.width)  &&  viewportBounds.width > 3 * SwingScrollWidget.BAR_HORIZONTAL_SIZE;
        }

        private boolean checkVertical (Rectangle viewBounds, Rectangle viewportBounds) {
            return (viewBounds.y < viewportBounds.y  ||  viewBounds.y + viewBounds.height > viewportBounds.y + viewportBounds.height)  &&  viewportBounds.height > 3 * SwingScrollWidget.BAR_VERTICAL_SIZE;
        }

    }

    private class MyAdjustmentListener implements AdjustmentListener {

        private boolean vertical;

        public MyAdjustmentListener (boolean vertical) {
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

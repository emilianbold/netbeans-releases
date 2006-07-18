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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BevelBorder;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.Layout;
import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
// TODO - does not cooperate with action-tools - actions from null-tool should be working all the time
// TODO - invalid calculation/checking showVertical and showHorizontal when view.preferredLocation has negative values
public class ScrollWidget extends Widget {

    private static final Image IMAGE_UP = Utilities.loadImage ("org/netbeans/modules/visual/resources/arrow-up.png"); // NOI18N
    private static final Image IMAGE_DOWN = Utilities.loadImage ("org/netbeans/modules/visual/resources/arrow-down.png"); // NOI18N
    private static final Image IMAGE_LEFT = Utilities.loadImage ("org/netbeans/modules/visual/resources/arrow-left.png"); // NOI18N
    private static final Image IMAGE_RIGHT = Utilities.loadImage ("org/netbeans/modules/visual/resources/arrow-right.png"); // NOI18N

    private static final int BAR_VERTICAL_SIZE = 16;
    private static final int BAR_HORIZONTAL_SIZE = 16;

    private static final Point POINT_EMPTY = new Point ();
    private static final Rectangle RECTANGLE_EMPTY = new Rectangle ();

    private static final Border BORDER = new BevelBorder (true);

    private Widget viewport;
    private Widget view;

    private SliderWidget verticalSlider;
    private SliderWidget horizontalSlider;
    private Widget upArrow;
    private Widget downArrow;
    private Widget leftArrow;
    private Widget rightArrow;

    private Dimension minimalSize, maximalSize;

    public ScrollWidget (Scene scene) {
        super (scene);

        setLayout (new ScrollLayout ());
        setCheckClipping (true);

        viewport = new Widget (scene);
        viewport.setCheckClipping (true);
        addChild (viewport);

        addChild (upArrow = createUpArrow ());
        addChild (verticalSlider = createVerticalSlider ());
        addChild (downArrow = createDownArrow ());

        addChild (leftArrow = createLeftArrow ());
        addChild (horizontalSlider = createHorizontalSlider ());
        addChild (rightArrow = createRightArrow ());

        upArrow.getActions ().addAction (new UnitScrollAction (0, -16));
        downArrow.getActions ().addAction (new UnitScrollAction (0, 16));
        leftArrow.getActions ().addAction (new UnitScrollAction (-16, 0));
        rightArrow.getActions ().addAction (new UnitScrollAction (16, 0));

        horizontalSlider.getActions ().addAction (new BlockScrollAction (horizontalSlider, - 64, 0));
        verticalSlider.getActions ().addAction (new BlockScrollAction (verticalSlider, 0, - 64));
        horizontalSlider.getActions ().addAction (new SliderAction (horizontalSlider));
        verticalSlider.getActions ().addAction (new SliderAction (verticalSlider));
    }

    private SliderWidget createVerticalSlider () {
        SliderWidget slider = new SliderWidget (getScene (), true);
        slider.setOpaque (true);
        slider.setBackground (Color.LIGHT_GRAY);
        return slider;
    }

    private SliderWidget createHorizontalSlider () {
        SliderWidget slider = new SliderWidget (getScene (), false);
        slider.setOpaque (true);
        slider.setBackground (Color.LIGHT_GRAY);
        return slider;
    }

    protected Widget createUpArrow () {
        ImageWidget arrow = new ImageWidget (getScene (), IMAGE_UP);
        arrow.setBorder (BORDER);
        arrow.setOpaque (true);
        arrow.setBackground (Color.LIGHT_GRAY);
        return arrow;
    }

    protected Widget createDownArrow () {
        ImageWidget arrow = new ImageWidget (getScene (), IMAGE_DOWN);
        arrow.setBorder (BORDER);
        arrow.setOpaque (true);
        arrow.setBackground (Color.LIGHT_GRAY);
        return arrow;
    }

    protected Widget createLeftArrow () {
        ImageWidget arrow = new ImageWidget (getScene (), IMAGE_LEFT);
        arrow.setBorder (BORDER);
        arrow.setOpaque (true);
        arrow.setBackground (Color.LIGHT_GRAY);
        return arrow;
    }

    protected Widget createRightArrow () {
        ImageWidget arrow = new ImageWidget (getScene (), IMAGE_RIGHT);
        arrow.setBorder (BORDER);
        arrow.setOpaque (true);
        arrow.setBackground (Color.LIGHT_GRAY);
        return arrow;
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
                viewportBounds.width -= BAR_HORIZONTAL_SIZE;
                showHorizontal = checkHorizontal (viewBounds, viewportBounds);
            }
            if (showHorizontal) {
                viewportBounds.height -= BAR_VERTICAL_SIZE;
                if (! showVertical) {
                    showVertical = checkVertical (viewBounds, viewportBounds);
                    if (showVertical)
                        viewportBounds.width -= BAR_HORIZONTAL_SIZE;
                }
            }

            viewport.resolveBounds (scrollWidgetClientAreaLocation, new Rectangle (viewportBounds.getSize ()));

            int x1 = scrollWidgetClientAreaLocation.x;
            int x2 = scrollWidgetClientAreaLocation.x + viewportBounds.width;
            int y1 = scrollWidgetClientAreaLocation.y;
            int y2 = scrollWidgetClientAreaLocation.y + viewportBounds.height;

            if (showVertical) {
                upArrow.resolveBounds (new Point (x2, y1), new Rectangle (BAR_HORIZONTAL_SIZE, BAR_VERTICAL_SIZE));
                downArrow.resolveBounds (new Point (x2, y2 - BAR_VERTICAL_SIZE), new Rectangle (BAR_HORIZONTAL_SIZE, BAR_VERTICAL_SIZE));
                verticalSlider.resolveBounds (new Point (x2, y1 + BAR_VERTICAL_SIZE), new Rectangle (BAR_HORIZONTAL_SIZE, viewportBounds.height - BAR_VERTICAL_SIZE - BAR_VERTICAL_SIZE));
            } else {
                upArrow.resolveBounds (POINT_EMPTY, RECTANGLE_EMPTY);
                downArrow.resolveBounds (POINT_EMPTY, RECTANGLE_EMPTY);
                verticalSlider.resolveBounds (POINT_EMPTY, RECTANGLE_EMPTY);
            }

            if (showHorizontal) {
                leftArrow.resolveBounds (new Point (x1, y2), new Rectangle (BAR_HORIZONTAL_SIZE, BAR_VERTICAL_SIZE));
                rightArrow.resolveBounds (new Point (x2 - BAR_HORIZONTAL_SIZE, y2), new Rectangle (BAR_HORIZONTAL_SIZE, BAR_VERTICAL_SIZE));
                horizontalSlider.resolveBounds (new Point (x1 + BAR_HORIZONTAL_SIZE, y2), new Rectangle (viewportBounds.width - BAR_HORIZONTAL_SIZE - BAR_HORIZONTAL_SIZE, BAR_VERTICAL_SIZE));
            } else {
                leftArrow.resolveBounds (POINT_EMPTY, RECTANGLE_EMPTY);
                rightArrow.resolveBounds (POINT_EMPTY, RECTANGLE_EMPTY);
                horizontalSlider.resolveBounds (POINT_EMPTY, RECTANGLE_EMPTY);
            }

            verticalSlider.setValues (viewBounds.y + viewportBounds.y, viewBounds.y + viewportBounds.y + viewBounds.height, 0, viewportBounds.height);
            horizontalSlider.setValues (viewBounds.x + viewportBounds.x, viewBounds.x + viewportBounds.x + viewBounds.width, 0, viewportBounds.width);
        }

        private boolean checkHorizontal (Rectangle viewBounds, Rectangle viewportBounds) {
            return (viewBounds.x < viewportBounds.x  ||  viewBounds.x + viewBounds.width > viewportBounds.x + viewportBounds.width)  &&  viewportBounds.width > 3 * BAR_HORIZONTAL_SIZE;
        }

        private boolean checkVertical (Rectangle viewBounds, Rectangle viewportBounds) {
            return (viewBounds.y < viewportBounds.y  ||  viewBounds.y + viewBounds.height > viewportBounds.y + viewportBounds.height)  &&  viewportBounds.height > 3 * BAR_VERTICAL_SIZE;
        }

    }

    private static class SliderWidget extends Widget {

        public enum Part {
            OUTSIDE, BEFORE, SLIDER, AFTER
        }

        private static final Color COLOR = new Color (0x5B87CE);
        private static final BevelBorder BORDER = new BevelBorder (true, COLOR);

        private boolean vertical;
        private long minimumValue = 0;
        private long maximumValue = 100;
        private long startValue = 0;
        private long endValue = 100;

        public SliderWidget (Scene scene, boolean vertical) {
            super (scene);
            this.vertical = vertical;
        }

        public boolean isVertical () {
            return vertical;
        }

        public long getMinimumValue () {
            return minimumValue;
        }

        public void setValues (long minimumValue, long maximumValue, long startValue, long endValue) {
            this.minimumValue = minimumValue;
            this.maximumValue = maximumValue;
            this.startValue = startValue;
            this.endValue = endValue;
            repaint ();
        }

        public void setMinimumValue (long minimumValue) {
            this.minimumValue = minimumValue;
            repaint ();
        }

        public long getMaximumValue () {
            return maximumValue;
        }

        public void setMaximumValue (long maximumValue) {
            this.maximumValue = maximumValue;
            repaint ();
        }

        public long getStartValue () {
            return startValue;
        }

        public void setStartValue (long startValue) {
            this.startValue = startValue;
            repaint ();
        }

        public long getEndValue () {
            return endValue;
        }

        public void setEndValue (long endValue) {
            this.endValue = endValue;
            repaint ();
        }

        public Part getPartHitAt (Point localLocation) {
            if (! isHitAt (localLocation))
                return Part.OUTSIDE;

            Rectangle clientArea = getClientArea ();
            long area;
            int point;

            if (vertical) {
                area = clientArea.height;
                point = localLocation.y - clientArea.y;
            } else {
                area = clientArea.width;
                point = localLocation.x - clientArea.x;
            }

            long s = Math.min (Math.max (startValue, minimumValue), maximumValue);
            long e = Math.min (Math.max (endValue, minimumValue), maximumValue);

            int start = (int) ((float) area * (float) (s - minimumValue) / (float) (maximumValue - minimumValue));
            int end = (int) ((float) area * (float) (e - minimumValue) / (float) (maximumValue - minimumValue));

            if (point < start)
                return Part.BEFORE;
            else if (point >= end)
                return Part.AFTER;
            else
                return Part.SLIDER;
        }

        public float getPixelIncrement () {
            return (float) (vertical ? getClientArea ().height : getClientArea ().width) / (float) (maximumValue - minimumValue);
        }

        protected void paintWidget () {
            Graphics2D gr = getGraphics ();

            Rectangle clientArea = getClientArea ();
            long area = vertical ? clientArea.height : clientArea.width;
            if (minimumValue < maximumValue  &&  startValue < endValue) {
                long s = Math.min (Math.max (startValue, minimumValue), maximumValue);
                int start = (int) ((float) area * (float) (s - minimumValue) / (float) (maximumValue - minimumValue));
                long e = Math.min (Math.max (endValue, minimumValue), maximumValue);
                int end = (int) ((float) area * (float) (e - minimumValue) / (float) (maximumValue - minimumValue));

                gr.setColor (COLOR);
                if (start + 4 < end) {
                    if (vertical) {
                        gr.fillRect (clientArea.x + 2, clientArea.y + start + 2, clientArea.width - 4, end - start - 4);
                        BORDER.paint (gr, new Rectangle (clientArea.x, clientArea.y + start, clientArea.width, end - start));
                    } else {
                        gr.fillRect (clientArea.x + start + 2, clientArea.y + 2, end - start - 4, clientArea.height - 4);
                        BORDER.paint (gr, new Rectangle (clientArea.x + start, clientArea.y, end - start, clientArea.height));
                    }
                } else {
                    if (vertical)
                        gr.fillRect (clientArea.x, clientArea.y + start, clientArea.width, end - start);
                    else
                        gr.fillRect (clientArea.x + start, clientArea.y, end - start, clientArea.height);
                }
            }

        }

    }

    private void translateView (int dx, int dy) {
        Widget v = getView ();
        if (v == null)
            return;
        Point location = view.getLocation ();
        location.translate (- dx, - dy);
        checkViewLocationInBounds (location, view, dx, dy);
        v.setPreferredLocation (location);
    }

    private void checkViewLocationInBounds (Point location, Widget v, int dx, int dy) {
        Rectangle view = v.getBounds ();
        Rectangle viewport = ScrollWidget.this.viewport.getBounds ();

        if (dx < 0 && location.x > view.x)
            location.x = view.x;
        if (dy < 0 && location.y > view.y)
            location.y = view.y;

        if (dx > 0 && location.x + view.width < viewport.x + viewport.width)
            location.x = viewport.x + viewport.width - view.width;
        if (dy > 0 && location.y + view.height < viewport.y + viewport.height)
            location.y = viewport.y + viewport.height - view.height;
    }

    private class UnitScrollAction extends WidgetAction.Adapter {

        private int dx;
        private int dy;

        public UnitScrollAction (int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            if (event.getButton () == MouseEvent.BUTTON1) {
                translateView (dx, dy);
                return State.CHAIN_ONLY;
            }
            return State.REJECTED;
        }

    }

    private class BlockScrollAction extends WidgetAction.Adapter {

        private SliderWidget slider;
        private int dx;
        private int dy;

        public BlockScrollAction (SliderWidget slider, int dx, int dy) {
            this.slider = slider;
            this.dx = dx;
            this.dy = dy;
        }

        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            if (event.getButton () == MouseEvent.BUTTON1) {
                assert slider == widget;
                SliderWidget.Part part = slider.getPartHitAt (event.getPoint ());
                switch (part) {
                    case AFTER:
                        translateView (- dx, - dy);
                        return State.CHAIN_ONLY;
                    case BEFORE:
                        translateView (dx, dy);
                        return State.CHAIN_ONLY;
                }
            }
            return State.REJECTED;
        }

    }

    private class SliderAction extends WidgetAction.Adapter {

        private SliderWidget slider;

        private Widget movingWidget = null;
        private Point dragSceneLocation = null;
        private Point originalSceneLocation = null;

        public SliderAction (SliderWidget slider) {
            this.slider = slider;
        }

        protected boolean isLocked () {
            return movingWidget != null;
        }

        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 1) {
                Widget view = getView ();
                if (view != null  &&  slider.getPartHitAt (event.getPoint ()) == SliderWidget.Part.SLIDER) {
                    movingWidget = widget;
                    originalSceneLocation = view.getPreferredLocation ();
                    if (originalSceneLocation == null)
                        originalSceneLocation = new Point ();
                    dragSceneLocation = widget.convertLocalToScene (event.getPoint ());
                    return State.createLocked (widget, this);
                }
            }
            return State.REJECTED;
        }

        public State mouseReleased (Widget widget, WidgetMouseEvent event) {
            boolean state = move (widget, event.getPoint ());
            if (state)
                movingWidget = null;
            return state ? State.CONSUMED : State.REJECTED;
        }

        public State mouseDragged (Widget widget, WidgetMouseEvent event) {
            return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
        }

        private boolean move (Widget widget, Point newLocation) {
            Widget view = getView ();
            if (movingWidget != widget  ||  view == null)
                return false;
            newLocation = widget.convertLocalToScene (newLocation);

            int dx = 0, dy = 0;
            if (slider.isVertical ())
                dy = - (int) ((newLocation.y - dragSceneLocation.y) / slider.getPixelIncrement ());
            else
                dx = - (int) ((newLocation.x - dragSceneLocation.x) / slider.getPixelIncrement ());

            newLocation.x = originalSceneLocation.x + dx;
            newLocation.y = originalSceneLocation.y + dy;

            checkViewLocationInBounds (newLocation, view, - dx, - dy);
            view.setPreferredLocation (newLocation);
            return true;
        }

    }


}

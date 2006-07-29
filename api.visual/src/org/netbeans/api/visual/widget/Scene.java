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

import org.netbeans.api.visual.action.MouseHoverAction;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.laf.DefaultLookFeel;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.util.GeomUtil;
import org.openide.util.WeakSet;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

/**
 * @author David Kaspar
 */
// TODO - take SceneComponent dimension and correct Scene.resolveBounds
// TODO - remove SuppressWarnings
public class Scene extends Widget {

    private double zoomFactor = 1.0;
    private SceneAnimator sceneAnimator;

    private JComponent component;
    private Graphics2D graphics;
    private WeakSet satelites;

    private Font defaultFont;
    private Rectangle repaintRegion = null;
    private HashSet<Widget> repaintWidgets = new HashSet<Widget> ();
    private LookFeel lookFeel = new DefaultLookFeel ();
    private String activeTool;

    private final HashSet validateListeners = new HashSet ();

    private WidgetAction widgetHoverAction;

    public Scene () {
        super (null);
        setScene (this);
        defaultFont = Font.decode (null);
        resolveBounds (new Point (), new Rectangle ());
        setOpaque(true);
        setFont (defaultFont);
        setBackground (lookFeel.getBackground ());
        setForeground (lookFeel.getForeground ());
        sceneAnimator = new SceneAnimator (this);
        satelites = new WeakSet ();
    }

    public JComponent createView () {
        assert component == null;
        component = new SceneComponent (this);
        component.addAncestorListener (new AncestorListener() {
            public void ancestorAdded (AncestorEvent event) {
                repaintSatelite ();
            }
            public void ancestorRemoved (AncestorEvent event) {
                repaintSatelite ();
            }
            public void ancestorMoved (AncestorEvent event) {
                repaintSatelite ();
            }
        });
        return component;
    }

    public JComponent getComponent () {
        return component;
    }

    @SuppressWarnings("unchecked")
    public JComponent createSateliteView () {
        SateliteComponent sateliteComponent = new SateliteComponent (this);
        satelites.add (sateliteComponent);
        return sateliteComponent;
    }

    public final Graphics2D getGraphics () {
        return graphics;
    }

    final void setGraphics (Graphics2D graphics) {
        this.graphics = graphics;
    }

    public Font getDefaultFont () {
        return defaultFont;
    }

    protected boolean isRepaintRequiredForRevalidating () {
        return false;
    }

    // TODO - maybe it could improve the perfomance, if bounds != null then do nothing
    // WARNING - you have to asure that there will be no component/widget will not change its location/bounds between this and validate method calls
    public final void revalidateWidget (Widget widget) {
        Rectangle widgetBounds = widget.getBounds ();
        if (widgetBounds != null) {
            Rectangle sceneBounds = widget.convertLocalToScene (widgetBounds);
            if (repaintRegion == null)
                repaintRegion = sceneBounds;
            else
                repaintRegion.add (sceneBounds);
        }
        repaintWidgets.add (widget);
    }

    // TODO - requires optimalization while changing preferred size and calling revalidate/repaint
    private void layoutScene () {
        layout (false);

        Rectangle rect = null;
        for (Widget widget : getChildren ()) {
            Point location = widget.getLocation ();
            Rectangle bounds = widget.getBounds ();
            bounds.translate (location.x, location.y);
            if (rect == null)
                rect = bounds;
            else
                rect.add (bounds);
        }
        if (rect != null) {
            Insets insets = getBorder ().getInsets ();
            rect.x -= insets.left;
            rect.y -= insets.top;
            rect.width += insets.left + insets.right;
            rect.height += insets.top + insets.bottom;
        }

        Point preLocation = getLocation ();
        Rectangle preBounds = getBounds ();
        resolveBounds (rect != null ? new Point (- rect.x, - rect.y) : new Point (), rect);

        Dimension preferredSize = rect != null ? rect.getSize () : new Dimension ();
        preferredSize = new Dimension ((int) (preferredSize.width * zoomFactor), (int) (preferredSize.height * zoomFactor));
        if (! preferredSize.equals (component.getPreferredSize ())) {
            component.setPreferredSize (preferredSize);
            component.revalidate ();
//            repaintSatelite ();
        }

        Dimension componentSize = component.getSize ();
        componentSize.width = (int) (componentSize.width / zoomFactor);
        componentSize.height = (int) (componentSize.height / zoomFactor);
        Rectangle bounds = getBounds ();

        boolean sceneResized = false;
        if (bounds.width < componentSize.width) {
            bounds.width = componentSize.width;
            sceneResized = true;
        }
        if (bounds.height < componentSize.height) {
            bounds.height = componentSize.height;
            sceneResized = true;
        }
        if (sceneResized)
            resolveBounds (getLocation (), bounds);

        if (! getLocation ().equals (preLocation)  ||  ! bounds.equals (preBounds)) {
            Rectangle rectangle = convertLocalToScene (getBounds ());
            if (repaintRegion == null)
                repaintRegion = rectangle;
            else
                repaintRegion.add (rectangle);
        }
    }

    @SuppressWarnings("unchecked")
    public final void validate () {
        if (graphics == null)
            return;
        ValidateListener[] ls = (ValidateListener[]) validateListeners.toArray (new ValidateListener[validateListeners.size ()]);

        for (ValidateListener listener : ls)
            if (listener != null)
                listener.sceneValidating ();

        layoutScene ();

        for (Widget widget : repaintWidgets) {
            Rectangle repaintBounds = calculateRepaintBounds (widget);
            if (repaintBounds == null)
                continue;
            if (repaintRegion != null)
                repaintRegion.add (repaintBounds);
            else
                repaintRegion = repaintBounds;
        }
        repaintWidgets.clear ();
//        System.out.println ("r = " + r);

        // NOTE - maybe improves performance when component.repaint will be called for all widgets/rectangles separately
        if (repaintRegion != null) {
            component.repaint (convertSceneToView (repaintRegion));
            repaintSatelite ();
            repaintRegion = null;
        }
//        System.out.println ("time: " + System.currentTimeMillis ());

        for (ValidateListener listener : ls)
            if (listener != null)
                listener.sceneValidated ();
    }

    public final Rectangle calculateRepaintBounds (Widget widget) {
        Rectangle bounds = widget.getBounds ();
        if (bounds == null)
            return null;
        Rectangle sceneRectangle = new Rectangle (bounds);
        for (;;) {
            if (widget == null)
                return null;
            Point location = widget.getLocation ();
            sceneRectangle.x += location.x;
            sceneRectangle.y += location.y;
            if (widget == this)
                return sceneRectangle;
            widget = widget.getParentWidget ();
        }
    }

    private void repaintSatelite () {
        for (Object view : satelites)
            ((JComponent) view).repaint ();
    }

    public final double getZoomFactor () {
        return zoomFactor;
    }

    public final void setZoomFactor (double zoomFactor) {
        this.zoomFactor = zoomFactor;
        revalidate ();
    }

    public SceneAnimator getSceneAnimator () {
        return sceneAnimator;
    }

    public LookFeel getLookFeel () {
        return lookFeel;
    }

    public void setLookFeel (LookFeel lookFeel) {
        this.lookFeel = lookFeel;
    }

    public String getActiveTool () {
        return activeTool;
    }

    public void setActiveTool (String activeTool) {
        this.activeTool = activeTool;
    }

    public final void addValidateListener (ValidateListener listener) {
        synchronized (validateListeners) {
            validateListeners.add (listener);
        }
    }

    public final void removeValidateListener (ValidateListener listener) {
        synchronized (validateListeners) {
            validateListeners.remove (listener);
        }
    }

    public final Point convertSceneToView (Point sceneLocation) {
        return new Point ((int) (zoomFactor * sceneLocation.x), (int) (zoomFactor * sceneLocation.y));
    }

    public final Rectangle convertSceneToView (Rectangle sceneRectangle) {
        return GeomUtil.roundRectangle (new Rectangle2D.Double (
                (double) sceneRectangle.x * zoomFactor,
                (double) sceneRectangle.y * zoomFactor,
                (double) sceneRectangle.width * zoomFactor,
                (double) sceneRectangle.height * zoomFactor));
    }

    public Point convertViewToScene (Point viewLocation) {
        return new Point ((int) ((double) viewLocation.x / zoomFactor), (int) ((double) viewLocation.y / zoomFactor));
    }

    public WidgetAction createWidgetHoverAction () {
        if (widgetHoverAction == null) {
            widgetHoverAction = new WidgetHoverAction ();
            getActions ().addAction (widgetHoverAction);
        }
        return widgetHoverAction;
    }

    private class WidgetHoverAction extends MouseHoverAction.TwoStated {

        protected void unsetHovering (Widget widget) {
            widget.setState (widget.getState ().deriveWidgetHovered (false));
        }

        protected void setHovering (Widget widget) {
            widget.setState (widget.getState ().deriveWidgetHovered (true));
        }

    }

    public interface ValidateListener {

        void sceneValidating ();

        void sceneValidated ();

    }

}

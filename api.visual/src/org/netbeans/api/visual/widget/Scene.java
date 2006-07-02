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

import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.laf.DefaultLookFeel;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.util.GeomUtil;
import org.openide.util.WeakSet;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
// TODO - revalidateWidget does not work for ConnectionWidget correct - probably the old bounds are not taken in account
// TODO - take SceneComponent dimension and correct Scene.resolveBounds
public class Scene extends Widget {

    private double zoomFactor = 1.0;
    private SceneAnimator sceneAnimator;

    private JComponent component;
    private Graphics2D graphics;
    private WeakSet<JComponent> satelites;

    private Font defaultFont;
    private HashMap<Widget,Rectangle> repaints = new HashMap<Widget, Rectangle> ();
    private LookFeel lookFeel = new DefaultLookFeel ();

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
        satelites = new WeakSet<JComponent> ();
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

    // TODO - maybe it could improve the perfomance, if bounds != null then do nothing
    // WARNING - you have to asure that there will be no component/widget will not change its location/bounds between this and validate method calls
    public final void revalidateWidget (Widget widget) {
        Rectangle bounds = repaints.get (widget);
        Rectangle absoluteBounds = widget.getBounds ();
        if (absoluteBounds == null) {
            if (bounds == null)
                repaints.remove (widget);
            return;
        }
        Point absoluteLocation = widget.convertLocalToScene (new Point ());
        absoluteBounds.translate (absoluteLocation.x, absoluteLocation.y);
        if (bounds == null)
            repaints.put (widget, absoluteBounds);
        else
            bounds.add (absoluteBounds);
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
            component.repaint ();
//            repaintSatelite ();
        }
    }

    public final void validate () {
        if (graphics == null)
            return;
        notifyValidating ();

        Rectangle r = null;
        for (Rectangle bounds : repaints.values ()) {
            if (bounds == null)
                continue;
            if (r != null)
                r.add (bounds);
            else
                r = new Rectangle (bounds);
        }

        layoutScene ();

        for (Widget widget : repaints.keySet ()) {
            Rectangle absoluteBounds = widget.convertLocalToScene (widget.getBounds ());
            if (r != null)
                r.add (absoluteBounds);
            else
                r = absoluteBounds;
        }

        repaints.clear ();

//        System.out.println ("r = " + r);

        // TODO - count with zoom factor while repainting
        // TODO - maybe improves performance when component.repaint will be called for all widgets/rectangles separately
        if (r != null) {
            component.repaint (convertSceneToView (r));
            repaintSatelite ();
        }
//        System.out.println ("time: " + System.currentTimeMillis ());

        notifyValidated ();
    }

    protected void notifyValidating () {
    }

    protected void notifyValidated () {
    }

    private void repaintSatelite () {
        for (JComponent view : satelites) {
            view.repaint ();
        }
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

}

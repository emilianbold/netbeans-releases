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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.router.DirectRouter;
import org.netbeans.api.visual.router.Router;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - control points can be modified by accessing: getControlPoints ().get (0).x or y
public class ConnectionWidget extends Widget implements Widget.Dependency {

    private Anchor sourceAnchor;
    private Anchor targetAnchor;
    private AnchorShape sourceAnchorShape;
    private AnchorShape targetAnchorShape;
    private Router router;
    private List<Point> controlPoints;
    private List<Point> controlPointsUm;

    public ConnectionWidget (Scene scene) {
        super (scene);
        sourceAnchorShape = AnchorShape.NONE;
        targetAnchorShape = AnchorShape.NONE;
        router = DirectRouter.DEFAULT;
    }

    public final Anchor getSourceAnchor () {
        return sourceAnchor;
    }

    public final void setSourceAnchor (Anchor sourceAnchor) {
        if (this.sourceAnchor != null)
            this.sourceAnchor.removeDependency (this);
        this.sourceAnchor = sourceAnchor;
        if (this.sourceAnchor != null)
            sourceAnchor.addDependency (this);
        revalidate ();
    }

    public final Anchor getTargetAnchor () {
        return targetAnchor;
    }

    public final void setTargetAnchor (Anchor targetAnchor) {
        if (this.targetAnchor != null)
            this.targetAnchor.removeDependency (this);
        this.targetAnchor = targetAnchor;
        if (targetAnchor != null)
            targetAnchor.addDependency (this);
        revalidate ();
    }

    public AnchorShape getSourceAnchorShape () {
        return sourceAnchorShape;
    }

    public void setSourceAnchorShape (AnchorShape sourceAnchorShape) {
        assert sourceAnchorShape != null;
        this.sourceAnchorShape = sourceAnchorShape;
        revalidate ();
    }

    public AnchorShape getTargetAnchorShape () {
        return targetAnchorShape;
    }

    public void setTargetAnchorShape (AnchorShape targetAnchorShape) {
        assert targetAnchorShape != null;
        this.targetAnchorShape = targetAnchorShape;
        revalidate ();
    }

    public final Router getRouter () {
        return router;
    }

    public final void setRouter (Router router) {
        this.router = router;
        revalidate ();
    }

    public List<Point> getControlPoints () {
        return controlPointsUm;
    }

    public void setControlPoints (Collection<Point> controlPoints, boolean sceneLocations) {
        if (sceneLocations) {
            Point translation = this.convertLocalToScene (new Point ());
            ArrayList<Point> list = new ArrayList<Point> ();
            for (Point point : controlPoints)
                list.add (new Point (point.x - translation.x, point.y - translation.y));
            this.controlPoints = list;
        } else
            this.controlPoints = new ArrayList<Point> (controlPoints);
        this.controlPointsUm = Collections.unmodifiableList (this.controlPoints);
        revalidate ();
    }

    protected Rectangle calculateClientArea () {
        if (router != null)
            setControlPoints (router.routeConnection (this), true);
        Rectangle rect = null;
        for (Point point : controlPoints) {
            if (rect == null)
                rect = new Rectangle (point);
            else
                rect.add (point);
        }

        Point firstPoint = getFirstControlPoint ();
        if (firstPoint != null) {
            int radius = sourceAnchorShape.getRadius ();
            if (rect == null)
                rect = new Rectangle (firstPoint.x - radius / 2, firstPoint.y - radius / 2, radius, radius);
            else
                rect.add (new Rectangle (firstPoint.x - radius / 2, firstPoint.y - radius / 2, radius, radius));
        }

        Point lastPoint = getLastControlPoint ();
        if (lastPoint != null) {
            int radius = targetAnchorShape.getRadius ();
            if (rect == null)
                rect = new Rectangle (lastPoint.x - radius / 2, lastPoint.y - radius / 2, radius, radius);
            else
                rect.add (new Rectangle (lastPoint.x - radius / 2, lastPoint.y - radius / 2, radius, radius));
        }

        return rect != null ? rect : new Rectangle ();
    }

    private Point getFirstControlPoint () {
        if (controlPoints.size () <= 0)
            return null;
        return new Point (controlPoints.get (0));
    }

    private Point getLastControlPoint () {
        int size = controlPoints.size ();
        if (size <= 0)
            return null;
        return new Point (controlPoints.get (size - 1));
    }

    private double getSourceAnchorShapeRotation () {
        if (controlPoints.size () <= 1)
            return 0.0;
        Point point1 = controlPoints.get (0);
        Point point2 = controlPoints.get (1);
        return Math.atan2 (point2.y - point1.y, point2.x - point1.x);
    }

    public double getTargetAnchorShapeRotation () {
        int size = controlPoints.size ();
        if (size <= 1)
            return 0.0;
        Point point1 = controlPoints.get (size - 1);
        Point point2 = controlPoints.get (size - 2);
        return Math.atan2 (point2.y - point1.y, point2.x - point1.x);
    }

    public boolean isHitAt (Point localLocation) {
        return super.isHitAt (localLocation); // TODO - do checking with line segments
    }
    
    protected void paintWidget () {
        Graphics2D gr = getGraphics ();
        gr.setColor (getForeground ());
        GeneralPath path = null;
        for (Point point : controlPoints) {
            if (path == null) {
                path = new GeneralPath ();
                path.moveTo (point.x + 0.5f, point.y + 0.5f);
            } else {
                path.lineTo (point.x + 0.5f, point.y + 0.5f);
            }
        }
        if (path != null)
            gr.draw (path);

        AffineTransform previousTransform;
        Point controlPoint;

        controlPoint = getFirstControlPoint ();
        if (controlPoint != null) {
            previousTransform = gr.getTransform ();
            gr.translate (controlPoint.x, controlPoint.y);
            if (sourceAnchorShape.isLineOriented ())
                gr.rotate (getSourceAnchorShapeRotation ());
            sourceAnchorShape.paint (gr, true);
            gr.setTransform (previousTransform);
        }

        controlPoint = getLastControlPoint ();
        if (controlPoint != null) {
            previousTransform = gr.getTransform ();
            gr.translate (controlPoint.x, controlPoint.y);
            if (targetAnchorShape.isLineOriented ())
                gr.rotate (getTargetAnchorShapeRotation ());
            targetAnchorShape.paint (gr, false);
            gr.setTransform (previousTransform);
        }
    }

}

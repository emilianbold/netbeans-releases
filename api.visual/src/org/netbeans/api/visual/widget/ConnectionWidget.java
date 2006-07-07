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
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.DirectRouter;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.layout.ConnectionWidgetLayout;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - control points can be modified by accessing: getControlPoints ().get (0).x or y
public class ConnectionWidget extends Widget implements Widget.Dependency {

    private static final double HIT_DISTANCE_SQUARE = 16.0;

    private Anchor sourceAnchor;
    private Anchor targetAnchor;
    private AnchorShape sourceAnchorShape;
    private AnchorShape targetAnchorShape;
    private PointShape controlPointShape;
    private PointShape endPointShape;
    private Router router;
    private boolean routingRequired;
    private List<Point> controlPoints;
    private List<Point> controlPointsUm;
    private ConnectionWidgetLayout connectionWidgetLayout;

    public ConnectionWidget (Scene scene) {
        super (scene);
        sourceAnchorShape = AnchorShape.NONE;
        targetAnchorShape = AnchorShape.NONE;
        controlPointShape = PointShape.NONE;
        endPointShape = PointShape.NONE;
        router = DirectRouter.DEFAULT;
        routingRequired = true;
        connectionWidgetLayout = new ConnectionWidgetLayout (this);
        setLayout (connectionWidgetLayout);
    }

    public void notifyStateChanged (ObjectState state) {
        setForeground (getScene ().getLookFeel ().getLineColor (state));
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
        reroute ();
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
        reroute ();
    }

    public AnchorShape getSourceAnchorShape () {
        return sourceAnchorShape;
    }

    public void setSourceAnchorShape (AnchorShape sourceAnchorShape) {
        assert sourceAnchorShape != null;
        boolean repaint = this.sourceAnchorShape.getRadius () == sourceAnchorShape.getRadius ();
        this.sourceAnchorShape = sourceAnchorShape;
        if (repaint)
            repaint ();
        else
            revalidate ();
    }

    public AnchorShape getTargetAnchorShape () {
        return targetAnchorShape;
    }

    public void setTargetAnchorShape (AnchorShape targetAnchorShape) {
        assert targetAnchorShape != null;
        boolean repaint = this.targetAnchorShape.getRadius () == targetAnchorShape.getRadius ();
        this.targetAnchorShape = targetAnchorShape;
        if (repaint)
            repaint ();
        else
            revalidate ();
    }

    public PointShape getControlPointShape () {
        return controlPointShape;
    }

    public void setControlPointShape (PointShape controlPointShape) {
        assert controlPointShape != null;
        boolean repaint = this.controlPointShape.getRadius () == controlPointShape.getRadius ();
        this.controlPointShape = controlPointShape;
        if (repaint)
            repaint ();
        else
            revalidate ();
    }

    public PointShape getEndPointShape () {
        return endPointShape;
    }

    public void setEndPointShape (PointShape endPointShape) {
        assert endPointShape != null;
        boolean repaint = this.endPointShape.getRadius () == endPointShape.getRadius ();
        this.endPointShape = endPointShape;
        if (repaint)
            repaint ();
        else
            revalidate ();
    }

    public final Router getRouter () {
        return router;
    }

    public final void setRouter (Router router) {
        this.router = router;
        reroute ();
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
        routingRequired = false;
        revalidate ();
    }

    public void setConstraint (Widget childWidget, ConnectionWidgetLayout.Alignment alignment, float placementInPercentage) {
        connectionWidgetLayout.setConstraint (childWidget, alignment, placementInPercentage);
    }

    public void setConstraint (Widget childWidget, ConnectionWidgetLayout.Alignment alignment, int placementAtDistance) {
        connectionWidgetLayout.setConstraint (childWidget, alignment, placementAtDistance);
    }

    public void removeConstraint (Widget childWidget) {
        connectionWidgetLayout.removeConstraint (childWidget);
    }

    public void revalidateDependency () {
        reroute ();
    }

    public void calculateRouting () {
        if (routingRequired)
            setControlPoints (router.routeConnection (this), true);
    }

    protected Rectangle calculateClientArea () {
        calculateRouting ();
        int controlPointShapeRadius = controlPointShape.getRadius ();
        int controlPointShapeRadius2 = controlPointShapeRadius + controlPointShapeRadius;
        int endPointShapeRadius = endPointShape.getRadius ();

        Rectangle rect = null;
        for (Point point : controlPoints) {
            Rectangle addRect = new Rectangle (point.x - controlPointShapeRadius, point.y - controlPointShapeRadius, controlPointShapeRadius2, controlPointShapeRadius2);
            if (rect == null)
                rect = addRect;
            else
                rect.add (addRect);
        }

        Point firstPoint = getFirstControlPoint ();
        if (firstPoint != null) {
            int radius = Math.max (sourceAnchorShape.getRadius (), endPointShapeRadius);
            int radius2 = radius + radius;
            if (rect == null)
                rect = new Rectangle (firstPoint.x - radius, firstPoint.y - radius, radius2, radius2);
            else
                rect.add (new Rectangle (firstPoint.x - radius, firstPoint.y - radius, radius2, radius2));
        }

        Point lastPoint = getLastControlPoint ();
        if (lastPoint != null) {
            int radius = Math.max (targetAnchorShape.getRadius (), endPointShapeRadius);
            int radius2 = radius + radius;
            if (rect == null)
                rect = new Rectangle (lastPoint.x - radius, lastPoint.y - radius, radius2, radius2);
            else
                rect.add (new Rectangle (lastPoint.x - radius, lastPoint.y - radius, radius2, radius2));
        }

        if (rect != null)
            rect.grow (2, 2);

        return rect != null ? rect : new Rectangle ();
    }

    public boolean isValidated () {
        return super.isValidated ()  &&  isRouted ();
    }

    public final boolean isRouted () {
        return ! routingRequired;
    }

    public final void reroute () {
        routingRequired = true;
        revalidate ();
    }

    public final Point getFirstControlPoint () {
        if (controlPoints.size () <= 0)
            return null;
        return new Point (controlPoints.get (0));
    }

    public final Point getLastControlPoint () {
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
        if (! super.isHitAt (localLocation))
                return false;

        List<Point> controlPoints = getControlPoints ();
        for (int i = 0; i < controlPoints.size () - 1; i++) {
            Point point1 = controlPoints.get (i);
            Point point2 = controlPoints.get (i + 1);
            double dist = Line2D.ptSegDistSq (point1.x, point1.y, point2.x, point2.y, localLocation.x, localLocation.y);
            if (dist < HIT_DISTANCE_SQUARE)
                return true;
        }

        return getControlPointHitAt (localLocation) >= 0;
    }

    public final boolean isFirstControlPointHitAt (Point localLocation) {
        int endRadius = endPointShape.getRadius ();
        endRadius *= endRadius;
        Point firstPoint = getFirstControlPoint ();
        if (firstPoint != null)
            if (Point2D.distanceSq (firstPoint.x, firstPoint.y, localLocation.x, localLocation.y) <= endRadius)
                return true;
        return false;
    }

    public final boolean isLastControlPointHitAt (Point localLocation) {
        int endRadius = endPointShape.getRadius ();
        endRadius *= endRadius;
        Point lastPoint = getLastControlPoint ();
        if (lastPoint != null)
            if (Point2D.distanceSq (lastPoint.x, lastPoint.y, localLocation.x, localLocation.y) <= endRadius)
                return true;
        return false;
    }

    public final int getControlPointHitAt (Point localLocation) {
        int controlRadius = controlPointShape.getRadius ();
        controlRadius *= controlRadius;

        if (isFirstControlPointHitAt (localLocation))
            return 0;

        if (isLastControlPointHitAt (localLocation))
            return controlPoints.size () - 1;

        for (int i = 0; i < controlPoints.size (); i ++) {
            Point point = controlPoints.get (i);
            if (Point2D.distanceSq (point.x, point.y, localLocation.x, localLocation.y) <= controlRadius)
                return i;
        }

        return -1;
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

        ObjectState state = getState ();
        if (state.isSelected ()  ||  state.isFocused ()) {
            int last = controlPoints.size () - 1;
            for (int index = 0; index <= last; index ++) {
                Point point = controlPoints.get (index);
                previousTransform = gr.getTransform ();
                gr.translate (point.x, point.y);
                if (index == 0  ||  index == last)
                    endPointShape.paint (gr);
                else
                    controlPointShape.paint (gr);
                gr.setTransform (previousTransform);
            }
        }
    }

}

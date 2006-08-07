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
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.DirectRouter;
import org.netbeans.api.visual.router.Router;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - control points can be modified by accessing: getControlPoints ().get (0).x or y
public class ConnectionWidget extends Widget {

    private static final double HIT_DISTANCE_SQUARE = 16.0;
    private static final Stroke STROKE_DEFAULT = new BasicStroke (1.0f);

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
    private Layout connectionWidgetLayout;
    private Stroke stroke;
    private boolean paintControlPoints;

    private ConnectionEntry sourceEntry;
    private ConnectionEntry targetEntry;

    public ConnectionWidget (Scene scene) {
        super (scene);
        sourceAnchorShape = AnchorShape.NONE;
        targetAnchorShape = AnchorShape.NONE;
        controlPointShape = PointShape.NONE;
        endPointShape = PointShape.NONE;
        router = DirectRouter.DEFAULT;
        routingRequired = true;
        connectionWidgetLayout = LayoutFactory.createConnectionWidgetLayout (this);
        setLayout (connectionWidgetLayout);
        stroke = STROKE_DEFAULT;
        paintControlPoints = false;
        sourceEntry = new ConnectionEntry (true);
        targetEntry = new ConnectionEntry (false);
    }

    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        setForeground (getScene ().getLookFeel ().getLineColor (state));
        setPaintControlPoints (state.isSelected ());
    }

    public Stroke getStroke () {
        return stroke;
    }

    public void setStroke (Stroke stroke) {
        this.stroke = stroke;
        repaint ();
    }

    public boolean isPaintControlPoints () {
        return paintControlPoints;
    }

    public void setPaintControlPoints (boolean paintControlPoints) {
        this.paintControlPoints = paintControlPoints;
        repaint ();
    }

    public final Anchor getSourceAnchor () {
        return sourceAnchor;
    }

    public final void setSourceAnchor (Anchor sourceAnchor) {
        if (this.sourceAnchor != null)
            this.sourceAnchor.removeEntry (sourceEntry);
        this.sourceAnchor = sourceAnchor;
        if (this.sourceAnchor != null)
            sourceAnchor.addEntry (sourceEntry);
        reroute ();
    }

    public final Anchor getTargetAnchor () {
        return targetAnchor;
    }

    public final void setTargetAnchor (Anchor targetAnchor) {
        if (this.targetAnchor != null)
            this.targetAnchor.removeEntry (targetEntry);
        this.targetAnchor = targetAnchor;
        if (targetAnchor != null)
            targetAnchor.addEntry (targetEntry);
        reroute ();
    }

    public ConnectionEntry getSourceAnchorEntry () {
        return sourceEntry;
    }

    public ConnectionEntry getTargetAnchorEntry () {
        return targetEntry;
    }

    public AnchorShape getSourceAnchorShape () {
        return sourceAnchorShape;
    }

    public void setSourceAnchorShape (AnchorShape sourceAnchorShape) {
        assert sourceAnchorShape != null;
        boolean repaintOnly = this.sourceAnchorShape.getRadius () == sourceAnchorShape.getRadius ();
        this.sourceAnchorShape = sourceAnchorShape;
        revalidate (repaintOnly);
    }

    public AnchorShape getTargetAnchorShape () {
        return targetAnchorShape;
    }

    public void setTargetAnchorShape (AnchorShape targetAnchorShape) {
        assert targetAnchorShape != null;
        boolean repaintOnly = this.targetAnchorShape.getRadius () == targetAnchorShape.getRadius ();
        this.targetAnchorShape = targetAnchorShape;
        revalidate (repaintOnly);
    }

    public PointShape getControlPointShape () {
        return controlPointShape;
    }

    public void setControlPointShape (PointShape controlPointShape) {
        assert controlPointShape != null;
        boolean repaintOnly = this.controlPointShape.getRadius () == controlPointShape.getRadius ();
        this.controlPointShape = controlPointShape;
        revalidate (repaintOnly);
    }

    public PointShape getEndPointShape () {
        return endPointShape;
    }

    public void setEndPointShape (PointShape endPointShape) {
        assert endPointShape != null;
        boolean repaintOnly = this.endPointShape.getRadius () == endPointShape.getRadius ();
        this.endPointShape = endPointShape;
        revalidate (repaintOnly);
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

    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, float placementInPercentage) {
        LayoutFactory.setConstraint (this, childWidget, alignment, placementInPercentage);
    }

    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, int placementAtDistance) {
        LayoutFactory.setConstraint (this, childWidget, alignment, placementAtDistance);
    }

    public void removeConstraint (Widget childWidget) {
        LayoutFactory.removeConstraint (this, childWidget);
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
            rect.grow (2, 2); // TODO - improve line width calculation

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
        if (path != null) {
            Stroke previousStroke = gr.getStroke ();
            gr.setPaint (getForeground ());
            gr.setStroke (getStroke ());
            gr.draw (path);
            gr.setStroke (previousStroke);
        }


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

        if (paintControlPoints) {
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

    private class ConnectionEntry implements Anchor.Entry {

        private boolean source;

        public ConnectionEntry (boolean source) {
            this.source = source;
        }

        public void revalidateEntry () {
            ConnectionWidget.this.reroute ();
        }

        public ConnectionWidget getAttachedConnectionWidget () {
            return ConnectionWidget.this;
        }

        public boolean isAttachedToConnectionSource () {
            return source;
        }

        public Anchor getAttachedAnchor () {
            return source ? ConnectionWidget.this.getSourceAnchor () : ConnectionWidget.this.getTargetAnchor ();
        }

        public Anchor getOppositeAnchor () {
            return source ? ConnectionWidget.this.getTargetAnchor () : ConnectionWidget.this.getSourceAnchor ();
        }

    }

}

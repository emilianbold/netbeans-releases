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
package org.netbeans.modules.visual.layout;

import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;

import java.awt.*;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
public final class ConnectionWidgetLayout implements Layout {

    private ConnectionWidget connectionWidget;
    private HashMap<Widget, LayoutFactory.ConnectionWidgetLayoutAlignment> alignments;
    private HashMap<Widget, Float> percentagePlacements;
    private HashMap<Widget, Integer> distancePlacements;

    public ConnectionWidgetLayout (ConnectionWidget connectionWidget) {
        this.connectionWidget = connectionWidget;
    }

    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, float placementInPercentage) {
        assert childWidget != null;
        assert alignment != null;

        if (alignments == null)
            alignments = new HashMap<Widget, LayoutFactory.ConnectionWidgetLayoutAlignment> ();
        alignments.put (childWidget, alignment);

        if (percentagePlacements == null)
            percentagePlacements = new HashMap<Widget, Float> ();
        percentagePlacements.put (childWidget, placementInPercentage);

        if (distancePlacements != null)
            distancePlacements.remove (childWidget);
    }

    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, int placementAtDistance) {
        assert childWidget != null;
        assert alignment != null;

        if (alignments == null)
            alignments = new HashMap<Widget, LayoutFactory.ConnectionWidgetLayoutAlignment> ();
        alignments.put (childWidget, alignment);

        if (percentagePlacements != null)
            percentagePlacements.remove (childWidget);

        if (distancePlacements == null)
            distancePlacements = new HashMap<Widget, Integer> ();
        distancePlacements.put (childWidget, placementAtDistance);
    }

    public void removeConstraint (Widget childWidget) {
        assert childWidget != null;

        if (alignments != null)
            alignments.remove (childWidget);
        if (percentagePlacements != null)
            percentagePlacements.remove (childWidget);
        if (distancePlacements != null)
            distancePlacements.remove (childWidget);
    }

    public void layout (Widget widget) {
        assert connectionWidget == widget;

        connectionWidget.calculateRouting ();
        java.util.List<Point> controlPoints = connectionWidget.getControlPoints ();
        boolean empty = controlPoints == null  ||  controlPoints.size () <= 0;

        double totalDistance = 0.0;
        double[] distances = new double[empty ? 0 : controlPoints.size () - 1];
        for (int i = 0; i < distances.length; i ++)
            distances[i] = totalDistance += GeomUtil.distanceSq (controlPoints.get (i), controlPoints.get (i + 1));

        for (Widget child : widget.getChildren ()) {
            Float percentage = percentagePlacements != null ? percentagePlacements.get (child) : null;
            Integer distance = distancePlacements != null ? distancePlacements.get (child) : null;

            if (empty)
                layoutChildAt (child, new Point ());
            else if (percentage != null) {
                if (percentage <= 0.0)
                    layoutChildAt (child, connectionWidget.getFirstControlPoint ());
                else if (percentage >= 1.0)
                    layoutChildAt (child, connectionWidget.getLastControlPoint ());
                else
                    layoutChildAtDistance (distances, (int) (percentage * totalDistance), child, controlPoints);
            } else if (distance != null) {
                if (distance < 0)
                    layoutChildAtDistance (distances, distance + (int) totalDistance, child, controlPoints);
                else
                    layoutChildAtDistance (distances, distance, child, controlPoints);
            } else
                layoutChildAt (child, new Point ());
        }
    }

    public boolean requiresJustification (Widget widget) {
        return false;
    }

    public void justify (Widget widget) {
    }

    private void layoutChildAtDistance (double[] distances, int lineDistance, Widget child, java.util.List<Point> controlPoints) {
        int index = distances.length - 1;
        for (int i = 0; i < distances.length; i ++) {
            if (lineDistance < distances[i]) {
                index = i;
                break;
            }
        }

        double segmentStartDistance = index > 0 ? distances[index - 1] : 0;
        double segmentLength = distances[index] - segmentStartDistance;
        double segmentDistance = lineDistance - segmentStartDistance;

        if (segmentLength == 0.0) {
            layoutChildAt (child, controlPoints.get (index));
            return;
        }

        Point p1 = controlPoints.get (index);
        Point p2 = controlPoints.get (index + 1);

        double segmentFactor = segmentDistance / segmentLength;

        layoutChildAt (child, new Point ((int) (p1.x + (p2.x - p1.x) * segmentFactor), (int) (p1.y + (p2.y - p1.y) * segmentFactor)));
    }

    private void layoutChildAt (Widget childWidget, Point linePoint) {
        Rectangle preferredBounds = childWidget.getPreferredBounds ();
        Point referencePoint = getReferencePoint (alignments.get (childWidget), preferredBounds);
        Point location = childWidget.getPreferredLocation ();
        if (location != null)
            referencePoint.translate (- location.x, - location.y);
        childWidget.resolveBounds (new Point (linePoint.x - referencePoint.x, linePoint.y - referencePoint.y), preferredBounds);
    }

    private static Point getReferencePoint (LayoutFactory.ConnectionWidgetLayoutAlignment alignment, Rectangle rectangle) {
        switch (alignment) {
            case BOTTOM_CENTER:
                return new Point (GeomUtil.centerX (rectangle), rectangle.y - 1);
            case BOTTOM_LEFT:
                return new Point (rectangle.x + rectangle.width, rectangle.y - 1);
            case BOTTOM_RIGHT:
                return new Point (rectangle.x - 1, rectangle.y - 1);
            case CENTER:
                return GeomUtil.center (rectangle);
            case CENTER_LEFT:
                return new Point (rectangle.x + rectangle.width, GeomUtil.centerY (rectangle));
            case CENTER_RIGHT:
                return new Point (rectangle.x - 1, GeomUtil.centerY (rectangle));
            case NONE:
                return new Point ();
            case TOP_CENTER:
                return new Point (GeomUtil.centerX (rectangle), rectangle.y + rectangle.height);
            case TOP_LEFT:
                return new Point (rectangle.x + rectangle.width, rectangle.y + rectangle.height);
            case TOP_RIGHT:
                return new Point (rectangle.x - 1, rectangle.y + rectangle.height);
            default:
                return new Point ();
        }
    }

}

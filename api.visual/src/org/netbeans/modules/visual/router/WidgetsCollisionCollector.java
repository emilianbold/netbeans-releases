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
package org.netbeans.modules.visual.router;

import org.netbeans.api.visual.router.CollisionsCollector;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author David Kaspar
 */
public class WidgetsCollisionCollector implements CollisionsCollector {

    private LayerWidget[] layers;

    public WidgetsCollisionCollector (LayerWidget... layers) {
        this.layers = layers;
    }

    public void collectCollisions (java.util.List<Rectangle> verticalCollisions, java.util.List<Rectangle> horizontalCollisions) {
        for (Widget widget : getWidgets ()) {
            if (! widget.isValidated ())
                continue;
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget conn = (ConnectionWidget) widget;
                if (! conn.isRouted ())
                    continue;
                java.util.List<Point> controlPoints = conn.getControlPoints ();
                int last = controlPoints.size () - 1;
                for (int i = 0; i < last; i ++) {
                    Point point1 = controlPoints.get (i);
                    Point point2 = controlPoints.get (i + 1);
                    if (point1.x == point2.x) {
                        Rectangle rectangle = new Rectangle (point1.x, Math.min (point1.y, point2.y), 0, Math.abs (point2.y - point1.y));
                        rectangle.grow (OrthogonalSearchRouter.SPACING, OrthogonalSearchRouter.SPACING);
                        verticalCollisions.add (rectangle);
                    } else if (point1.y == point2.y) {
                        Rectangle rectangle = new Rectangle (Math.min (point1.x, point2.x), point1.y, Math.abs (point2.x - point1.x), 0);
                        rectangle.grow (OrthogonalSearchRouter.SPACING, OrthogonalSearchRouter.SPACING);
                        horizontalCollisions.add (rectangle);
                    }
                }
            } else {
                Rectangle bounds = widget.getBounds ();
                Rectangle rectangle = widget.convertLocalToScene (bounds);
                rectangle.grow (OrthogonalSearchRouter.SPACING, OrthogonalSearchRouter.SPACING);
                verticalCollisions.add (rectangle);
                horizontalCollisions.add (rectangle);
            }
        }
    }

    protected Collection<Widget> getWidgets () {
        ArrayList<Widget> list = new ArrayList<Widget> ();
        for (LayerWidget layer : layers)
            list.addAll (layer.getChildren ());
        return list;
    }

}

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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * @author Alex
 */
public final class FreeRectangularAnchor extends Anchor {

    private boolean includeBorders;

    public FreeRectangularAnchor(Widget widget, boolean includeBorders) {
        super(widget);
        this.includeBorders = includeBorders;
    }

    public Result compute(Entry entry) {
        assert entry.getAttachedConnectionWidget()instanceof ConnectionWidget;
        Point relatedLocation = getRelatedSceneLocation();

        Widget widget = getRelatedWidget();
        ConnectionWidget fcw=entry.getAttachedConnectionWidget();
        List<Point> fcwControlPoints = fcw.getControlPoints ();
        Point oppositeLocation;
        if (fcwControlPoints.size () < 2)
            oppositeLocation = getOppositeSceneLocation (entry);
        else if (entry.isAttachedToConnectionSource ())
            oppositeLocation = fcwControlPoints.get (1);
        else
            oppositeLocation = fcwControlPoints.get (fcwControlPoints.size () - 2);

        Rectangle bounds = widget.getBounds();
        if (! includeBorders) {
            Insets insets = widget.getBorder().getInsets();
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        bounds = widget.convertLocalToScene(bounds);

        if (bounds.isEmpty()  || relatedLocation.equals(oppositeLocation))
            return new Anchor.Result(relatedLocation, Anchor.DIRECTION_ANY);

        float dx = oppositeLocation.x - relatedLocation.x;
        float dy = oppositeLocation.y - relatedLocation.y;

        float ddx = Math.abs(dx) / (float) bounds.width;
        float ddy = Math.abs(dy) / (float) bounds.height;

        Anchor.Direction direction;

        if (ddx >= ddy) {
            direction = dx >= 0.0f ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = dy >= 0.0f ? Direction.BOTTOM : Direction.TOP;
        }

        float scale = 0.5f / Math.max(ddx, ddy);

        Point point = new Point(Math.round(relatedLocation.x + scale * dx), Math.round(relatedLocation.y + scale * dy));
        return new Anchor.Result(point, direction);
    }

}

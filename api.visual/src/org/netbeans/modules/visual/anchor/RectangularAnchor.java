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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - scene component location is not 100% attach to the bounding rectangle when the line goes far to the bottom-left-bottom direction
public final class RectangularAnchor extends Anchor {

    private boolean includeBorders;

    public RectangularAnchor (Widget widget, boolean includeBorders) {
        super (widget);
//        assert widget != null;
        this.includeBorders = includeBorders;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        Widget widget = getRelatedWidget ();
        Rectangle bounds = widget.getBounds ();
        if (! includeBorders) {
            Insets insets = widget.getBorder ().getInsets ();
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        bounds = widget.convertLocalToScene (bounds);

        if (bounds.isEmpty ()  || relatedLocation.equals (oppositeLocation))
            return new Anchor.Result (relatedLocation, Anchor.DIRECTION_ANY);

        float dx = oppositeLocation.x - relatedLocation.x;
        float dy = oppositeLocation.y - relatedLocation.y;

        float ddx = Math.abs (dx) / (float) bounds.width;
        float ddy = Math.abs (dy) / (float) bounds.height;

        Anchor.Direction direction;

        if (ddx >= ddy) {
            direction = dx >= 0.0f ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = dy >= 0.0f ? Direction.BOTTOM : Direction.TOP;
        }

        float scale = 0.5f / Math.max (ddx, ddy);

        Point point = new Point (Math.round (relatedLocation.x + scale * dx), Math.round (relatedLocation.y + scale * dy));
        return new Anchor.Result (point, direction);
    }

}

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
public final class CircularAnchor extends Anchor {

    private int radius;

    public CircularAnchor (Widget widget, int radius) {
        super (widget);
//        assert widget != null;
        this.radius = radius;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        double angle = Math.atan2 (oppositeLocation.y - relatedLocation.y, oppositeLocation.x - relatedLocation.x);

        Point location = new Point (relatedLocation.x + (int) (radius * Math.cos (angle)), relatedLocation.y + (int) (radius * Math.sin (angle)));
        return new Anchor.Result (location, Anchor.DIRECTION_ANY); // TODO - resolve direction
    }

}

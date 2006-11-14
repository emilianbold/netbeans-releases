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

import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class DirectionalAnchor extends Anchor {

    private AnchorFactory.DirectionalAnchorKind kind;
    private int gap;

    public DirectionalAnchor (Widget widget, AnchorFactory.DirectionalAnchorKind kind, int gap) {
        super (widget);
//        assert widget != null;
        this.kind = kind;
        this.gap = gap;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        Widget widget = getRelatedWidget ();
        Rectangle bounds = widget.convertLocalToScene (widget.getBounds ());
        Point center = GeomUtil.center (bounds);

        switch (kind) {
            case HORIZONTAL:
                if (relatedLocation.x >= oppositeLocation.x)
                    return new Anchor.Result (new Point (bounds.x - gap, center.y), Direction.LEFT);
                else
                    return new Anchor.Result (new Point (bounds.x + bounds.width + gap, center.y), Direction.RIGHT);
            case VERTICAL:
                if (relatedLocation.y >= oppositeLocation.y)
                    return new Anchor.Result (new Point (center.x, bounds.y - gap), Direction.TOP);
                else
                    return new Anchor.Result (new Point (center.x, bounds.y + bounds.height + gap), Direction.BOTTOM);
        }
        return null;
    }

}

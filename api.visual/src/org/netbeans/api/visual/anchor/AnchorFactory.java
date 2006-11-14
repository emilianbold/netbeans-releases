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
package org.netbeans.api.visual.anchor;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.modules.visual.anchor.*;

import java.awt.*;

/**
 * This is a factory of all built-in anchor implementations. Anchors are designed to be shared by multiple instances of proxy anchors and connection widgets.
 * @author David Kaspar
 */
public final class AnchorFactory {

    private AnchorFactory () {
    }

    /**
     * Represents possible orthogonal directions used by directional anchor.
     */
    public enum DirectionalAnchorKind {
        HORIZONTAL, VERTICAL//, DIRECTION_4, DIRECTION_8
    }

    /**
     * Creates a anchor with fixed scene location.
     * @param location the scene location
     * @return the anchor
     */
    public static Anchor createFixedAnchor (Point location) {
        return new FixedAnchor (location);
    }

    /**
     * Creates a proxy anchor with delegates the computation one of specified anchors based on state in a model.
     * @param model the model with state
     * @param anchors the slave anchors
     * @return the anchor
     */
    public static Anchor createProxyAnchor (StateModel model, Anchor... anchors) {
        return model != null  &&  model.getMaxStates () == anchors.length ? new ProxyAnchor (model, anchors) : null;
    }

    /**
     * Creates an anchor with always computes a point in the center of specified widget.
     * @param widget the widget
     * @return the anchor
     */
    public static Anchor createCenterAnchor (Widget widget) {
        return widget != null ? new CenterAnchor (widget) : null;
    }

    /**
     * Creates an anchor which computes a point as the one on a circle around specified widget.
     * The point is the closest one to location of opposite anchor.
     * @param widget the widget
     * @param radius the radius of the circle
     * @return the anchor
     */
    public static Anchor createCircularAnchor (Widget widget, int radius) {
        return widget != null  &&  radius >= 0 ? new CircularAnchor (widget, radius) : null;
    }

    /**
     * Creates an anchor which computes a point as the one on the boundary of spacified widget.
     * The point is the closest one to location of opposite anchor.
     * @param widget the widget
     * @return the anchor
     */
    public static Anchor createRectangularAnchor (Widget widget) {
        return createRectangularAnchor (widget, true);
    }

    /**
     * Creates an anchor which computes a point as the one on the boundary of spacified widget.
     * The point is the closest one to location of opposite anchor.
     * @param widget the widget
     * @param includeBorders if true, then the boundary is widget bounds;
     *         if null then the boundary is widget client-area (bounds without borders)
     * @return the anchor
     */
    public static Anchor createRectangularAnchor (Widget widget, boolean includeBorders) {
        return widget != null ? new RectangularAnchor (widget, includeBorders) : null;
    }

    /**
     * Creates a directional anchor with computes a point as the one in the middle of the boundary side of specified widget.
     * The side is the closest one to the opposite anchor.
     * @param widget the widget
     * @param kind the kind of directional anchor
     * @return the anchor
     */
    public static Anchor createDirectionalAnchor (Widget widget, DirectionalAnchorKind kind) {
        return createDirectionalAnchor (widget, kind, 0);
    }

    /**
     * Creates a directional anchor with computes a point as the one in the middle of the boundary side of specified widget.
     * The side is the closest one to the opposite anchor.
     * @param widget the widget
     * @param kind the kind of directional anchor
     * @param gap the gap between the widget and the anchor location
     * @return the anchor
     */
    public static Anchor createDirectionalAnchor (Widget widget, DirectionalAnchorKind kind, int gap) {
        return widget != null && kind != null ? new DirectionalAnchor (widget, kind, gap) : null;
    }

    /**
     * Creates a free rectangular anchor. IT is similar to rectangular anchor but it is designed to be used together with FreeRouter.
     * @param widget the widget
     * @param includeBorders true if borders has to be included in the boundary
     * @return the anchor
     */
    public static Anchor createFreeRectangularAnchor (Widget widget, boolean includeBorders) {
        return widget != null ? new FreeRectangularAnchor (widget, includeBorders) : null;
    }
}

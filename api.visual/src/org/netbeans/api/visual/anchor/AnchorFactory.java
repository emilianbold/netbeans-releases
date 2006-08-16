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
package org.netbeans.api.visual.anchor;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.modules.visual.anchor.*;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class AnchorFactory {

    private AnchorFactory () {
    }

    public enum DirectionalAnchorKind {
        HORIZONTAL, VERTICAL//, DIRECTION_4, DIRECTION_8
    }

    public static Anchor createFixedAnchor (Point location) {
        return new FixedAnchor (location);
    }

    public static Anchor createProxyAnchor (StateModel model, Anchor... anchors) {
        return model != null  &&  model.getMaxStates () == anchors.length ? new ProxyAnchor (model, anchors) : null;
    }

    public static Anchor createCenterAnchor (Widget widget) {
        return widget != null ? new CenterAnchor (widget) : null;
    }

    public static Anchor createCircularAnchor (Widget widget, int radius) {
        return widget != null  &&  radius >= 0 ? new CircularAnchor (widget, radius) : null;
    }

    public static Anchor createRectangularAnchor (Widget widget) {
        return createRectangularAnchor (widget, true);
    }

    public static Anchor createRectangularAnchor (Widget widget, boolean includeBorders) {
        return widget != null ? new RectangularAnchor (widget, includeBorders) : null;
    }

    public static Anchor createDirectionalAnchor (Widget widget, DirectionalAnchorKind kind) {
        return widget != null  &&  kind != null ? new DirectionalAnchor (widget, kind) : null;
    }

}

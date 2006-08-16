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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class CardLayout implements Layout {

    private static final Point POINT_EMPTY = new Point ();
    private static final Rectangle RECTANGLE_EMPTY = new Rectangle ();

    private Widget cardLayoutWidget;
    private Widget activeChildWidget;

    public CardLayout (Widget cardLayoutWidget) {
        assert cardLayoutWidget != null;
        this.cardLayoutWidget = cardLayoutWidget;
        cardLayoutWidget.setCheckClipping (true);
    }

    public Widget getActiveChildWidget () {
        return activeChildWidget;
    }

    public void setActiveChildWidget (Widget activeChildWidget) {
        this.activeChildWidget = activeChildWidget;
        cardLayoutWidget.revalidate ();
    }

    public void layout (Widget widget) {
        assert widget == cardLayoutWidget;

        Point preferredLocation = null;
        Rectangle preferredBounds = null;

        if (activeChildWidget != null)
            for (Widget child : cardLayoutWidget.getChildren ())
                if (child == activeChildWidget) {
                    preferredLocation = child.getPreferredLocation ();
                    preferredBounds = child.getPreferredBounds ();
                    break;
                }

        if (preferredLocation == null)
            preferredLocation = POINT_EMPTY;
        if (preferredBounds == null)
            preferredBounds = RECTANGLE_EMPTY;
        Rectangle otherBounds = new Rectangle (preferredBounds.x, preferredBounds.y, 0, 0);

        for (Widget child : cardLayoutWidget.getChildren ())
            child.resolveBounds (preferredLocation, child == activeChildWidget ? preferredBounds : otherBounds);
    }

}

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

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget widget) {
        assert widget == cardLayoutWidget;

        if (activeChildWidget != null)
            for (Widget child : cardLayoutWidget.getChildren ())
                if (child == activeChildWidget) {
                    Rectangle bounds = widget.getClientArea ();
                    Point location = child.getLocation ();
                    Rectangle childBounds = child.getBounds ();

                    bounds.translate (- location.x, - location.y);
                    childBounds.add (bounds);

                    child.resolveBounds (location, bounds);
                    return;
                }
    }

}

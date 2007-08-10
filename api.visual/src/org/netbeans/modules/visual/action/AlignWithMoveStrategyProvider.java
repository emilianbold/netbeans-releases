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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class AlignWithMoveStrategyProvider extends AlignWithSupport implements MoveStrategy, MoveProvider {

    private boolean outerBounds;

    public AlignWithMoveStrategyProvider (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator, boolean outerBounds) {
        super (collector, interractionLayer, decorator);
        this.outerBounds = outerBounds;
    }

    public Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation) {
        Point widgetLocation = widget.getLocation ();
        Rectangle widgetBounds = outerBounds ? widget.getBounds () : widget.getClientArea ();
        Rectangle bounds = widget.convertLocalToScene (widgetBounds);
        bounds.translate (suggestedLocation.x - widgetLocation.x, suggestedLocation.y - widgetLocation.y);
        Insets insets = widget.getBorder ().getInsets ();
        if (! outerBounds) {
            suggestedLocation.x += insets.left;
            suggestedLocation.y += insets.top;
        }
        Point point = super.locationSuggested (widget, bounds, suggestedLocation, true, true, true, true);
        if (! outerBounds) {
            point.x -= insets.left;
            point.y -= insets.top;
        }
        return widget.getParentWidget ().convertSceneToLocal (point);
    }

    public void movementStarted (Widget widget) {
        show ();
    }

    public void movementFinished (Widget widget) {
        hide ();
    }

    public Point getOriginalLocation (Widget widget) {
        return ActionFactory.createDefaultMoveProvider ().getOriginalLocation (widget);
    }

    public void setNewLocation (Widget widget, Point location) {
        ActionFactory.createDefaultMoveProvider ().setNewLocation (widget, location);
    }

}

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

import org.netbeans.api.visual.action.MoveControlPointProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class MoveControlPointAction extends WidgetAction.LockedAdapter {

    private MoveControlPointProvider provider;
    private ConnectionWidget.RoutingPolicy routingPolicy;

    private ConnectionWidget movingWidget = null;
    private Point controlPointLocation;
    private int controlPointIndex;
    private Point lastLocation = null;

    public MoveControlPointAction (MoveControlPointProvider provider, ConnectionWidget.RoutingPolicy routingPolicy) {
        this.provider = provider;
        this.routingPolicy = routingPolicy;
    }

    protected boolean isLocked () {
        return movingWidget != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget conn = (ConnectionWidget) widget;
                controlPointIndex = conn.getControlPointHitAt (event.getPoint ());
                if (controlPointIndex >= 0) {
                    movingWidget = conn;
                    controlPointLocation = new Point (conn.getControlPoints (). get (controlPointIndex));
                    lastLocation = new Point (event.getPoint ());
                    return State.createLocked (widget, this);
                } else {
                    movingWidget = null;
                }
            }
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        State state = move (widget, event.getPoint ());
        if (state.isConsumed ())
            movingWidget = null;
        return state;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return move (widget, event.getPoint ());
    }

    private State move (Widget widget, Point newLocation) {
        if (movingWidget != widget)
            return State.REJECTED;

        java.util.List<Point> controlPoints = movingWidget.getControlPoints ();
        if (controlPointIndex < 0  ||  controlPointIndex >= controlPoints.size ())
            return State.REJECTED;

        Point location = new Point (controlPointLocation);
        location.translate (newLocation.x - lastLocation.x, newLocation.y - lastLocation.y);

        controlPoints = provider.locationSuggested (movingWidget, controlPointIndex, location);
        if (controlPoints == null)
            return State.REJECTED;

        if (routingPolicy != null)
            movingWidget.setRoutingPolicy (routingPolicy);
        movingWidget.setControlPoints (controlPoints, false);
        return State.createLocked (widget, this);
    }

}

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

    private ConnectionWidget movingWidget = null;
    private Point controlPointLocation;
    private int controlPointIndex;
    private Point lastLocation = null;

    public MoveControlPointAction (MoveControlPointProvider provider) {
        this.provider = provider;
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

        movingWidget.setControlPoints (controlPoints, false);
        return State.createLocked (widget, this);
    }

}

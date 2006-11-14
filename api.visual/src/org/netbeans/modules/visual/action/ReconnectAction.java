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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Override at least isReplacementWidget and setConnectionAnchor methods. reconnectingFinished is always called before setConnectionAnchor.
 *
 * @author David Kaspar
 */
public final class ReconnectAction extends WidgetAction.LockedAdapter {

    private static final int MIN_DIFFERENCE = 5;

    private ReconnectDecorator decorator;
    private ReconnectProvider provider;

    private ConnectionWidget connectionWidget = null;
    private boolean reconnectingSource = false;
    private Point floatPoint = null;
    private Widget replacementWidget = null;
    private Anchor originalAnchor = null;

    public ReconnectAction (ReconnectDecorator decorator, ReconnectProvider provider) {
        this.decorator = decorator;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return connectionWidget != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget conn = (ConnectionWidget) widget;
                int index = conn.getControlPointHitAt (event.getPoint ());
                List<Point> controlPoints = conn.getControlPoints ();
                if (index == 0  &&  provider.isSourceReconnectable (conn)) {
                    reconnectingSource = true;
                } else if (controlPoints != null  &&  index == controlPoints.size () - 1  && provider.isTargetReconnectable (conn)) {
                    reconnectingSource = false;
                } else {
                    return State.REJECTED;
                }

                floatPoint = new Point (event.getPoint ());
                replacementWidget = null;
                connectionWidget = conn;
                provider.reconnectingStarted (conn, reconnectingSource);
                if (reconnectingSource)
                    originalAnchor = connectionWidget.getSourceAnchor ();
                else
                    originalAnchor = connectionWidget.getTargetAnchor ();
                return State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        Point point = event.getPoint ();
        boolean state = move (widget, point);
        if (state) {
            if (reconnectingSource)
                connectionWidget.setSourceAnchor (originalAnchor);
            else
                connectionWidget.setTargetAnchor (originalAnchor);
            provider.reconnectingFinished (connectionWidget, reconnectingSource);
            if (Math.abs (floatPoint.x - point.x) >= ReconnectAction.MIN_DIFFERENCE  ||  Math.abs (floatPoint.y - point.y) >= ReconnectAction.MIN_DIFFERENCE)
                provider.reconnect (connectionWidget, replacementWidget, reconnectingSource);
            replacementWidget = null;
            floatPoint = null;
            connectionWidget = null;
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point point) {
        if (connectionWidget != widget)
            return false;

        Point replacementSceneLocation = widget.convertLocalToScene (point);
        replacementWidget = resolveReplacementWidgetCore (connectionWidget.getScene (), replacementSceneLocation);
        Anchor replacementAnchor = null;
        if (replacementWidget != null)
            replacementAnchor = decorator.createReplacementWidgetAnchor (replacementWidget);
        if (replacementAnchor == null)
            replacementAnchor = decorator.createFloatAnchor (replacementSceneLocation);

        if (reconnectingSource)
            connectionWidget.setSourceAnchor (replacementAnchor);
        else
            connectionWidget.setTargetAnchor (replacementAnchor);

        return true;
    }

    protected Widget resolveReplacementWidgetCore (Scene scene, Point sceneLocation) {
        if (provider != null)
            if (provider.hasCustomReplacementWidgetResolver (scene))
                return provider.resolveReplacementWidget (scene, sceneLocation);
        Point sceneOrigin = scene.getLocation ();
        sceneLocation = new Point (sceneLocation.x + sceneOrigin.x, sceneLocation.y + sceneOrigin.y);
        Widget[] result = new Widget[]{null};
        resolveReplacementWidgetCoreDive (result, scene, sceneLocation);
        return result[0];
    }

    private boolean resolveReplacementWidgetCoreDive (Widget[] result, Widget widget, Point parentLocation) {
        if (widget == connectionWidget)
            return false;

        Point widgetLocation = widget.getLocation ();
        Point location = new Point (parentLocation.x - widgetLocation.x, parentLocation.y - widgetLocation.y);

        if (! widget.getBounds ().contains (location))
            return false;

        java.util.List<Widget> children = widget.getChildren ();
        for (int i = children.size () - 1; i >= 0; i --) {
            if (resolveReplacementWidgetCoreDive (result, children.get (i), location))
                return true;
        }

        if (! widget.isHitAt (location))
            return false;

        ConnectorState state = provider.isReplacementWidget (connectionWidget, widget, reconnectingSource);
        if (state == ConnectorState.REJECT)
            return false;
        if (state == ConnectorState.ACCEPT)
            result[0] = widget;
        return true;
    }

}

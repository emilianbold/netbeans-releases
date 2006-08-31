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

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.TwoStateHoverProvider;

/**
 * @author David Kaspar
 */
public final class TwoStatedMouseHoverAction extends WidgetAction.Adapter {

    private long eventID = Integer.MIN_VALUE;
    private TwoStateHoverProvider provider;
    private Widget lastWidget;

    public TwoStatedMouseHoverAction (TwoStateHoverProvider provider) {
        this.provider = provider;
    }

    public State mouseMoved (Widget widget, WidgetMouseEvent event) {
        long id = event.getEventID ();
        if (id != eventID) {
            eventID = id;
            widgetHovered (widget);
        }
        return State.REJECTED;
    }

    public State mouseExited (Widget widget, WidgetMouseEvent event) {
        long id = event.getEventID ();
        if (id != eventID) {
            eventID = id;
            widgetHovered (null);
        }
        return State.REJECTED;
    }

    public void widgetHovered (Widget widget) {
        if (widget instanceof Scene)
            widget = null;

        if (lastWidget == widget)
            return;

        if (lastWidget != null)
            provider.unsetHovering (lastWidget);

        lastWidget = widget;

        if (widget != null)
            provider.setHovering (widget);
    }

}

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

import org.netbeans.api.visual.action.HoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
// TODO - this action has to be calculated even if the mouse is not hovering any widget
public final class MouseHoverAction extends WidgetAction.Adapter {

    private long eventID = Integer.MIN_VALUE;
    private HoverProvider provider;

    public MouseHoverAction (HoverProvider provider) {
        this.provider = provider;
    }

    public State mouseMoved (Widget widget, WidgetMouseEvent event) {
        long id = event.getEventID ();
        if (id != eventID) {
            eventID = id;
            provider.widgetHovered (widget);
        }
        return State.REJECTED;
    }

    public State mouseExited (Widget widget, WidgetMouseEvent event) {
        long id = event.getEventID ();
        if (id != eventID) {
            eventID = id;
            provider.widgetHovered (null);
        }
        return State.REJECTED;
    }

}

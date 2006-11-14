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

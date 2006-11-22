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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public final class ForwardKeyEventsAction extends WidgetAction.Adapter {

    private Widget forwardToWidget;
    private String forwardedToTool;

    public ForwardKeyEventsAction (Widget forwardToWidget, String forwardedToTool) {
        this.forwardToWidget = forwardToWidget;
        this.forwardedToTool = forwardedToTool;
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        WidgetAction.Chain actions = forwardedToTool != null ? widget.getActions (forwardedToTool) : widget.getActions ();
        return actions != null ? actions.keyTyped (forwardToWidget, event) : State.REJECTED;
    }

    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        WidgetAction.Chain actions = forwardedToTool != null ? widget.getActions (forwardedToTool) : widget.getActions ();
        return actions != null ? actions.keyPressed (forwardToWidget, event) : State.REJECTED;
    }

    public State keyReleased (Widget widget, WidgetKeyEvent event) {
        WidgetAction.Chain actions = forwardedToTool != null ? widget.getActions (forwardedToTool) : widget.getActions ();
        return actions != null ? actions.keyReleased (forwardToWidget, event) : State.REJECTED;
    }

}

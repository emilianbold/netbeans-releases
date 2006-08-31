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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.EditProvider;

import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class EditAction extends WidgetAction.Adapter {

    private EditProvider provider;

    public EditAction (EditProvider provider) {
        this.provider = provider;
    }

    public State mouseClicked (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 2) {
            provider.edit (widget);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}

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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.CycleFocusProvider;
import org.netbeans.api.visual.widget.Widget;

import java.awt.event.KeyEvent;

/**
 * @author David Kaspar
 */
public class CycleFocusAction extends WidgetAction.Adapter {

    private CycleFocusProvider provider;

    public CycleFocusAction (CycleFocusProvider provider) {
        this.provider = provider;
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        boolean state = false;
        if (event.getKeyChar () == KeyEvent.VK_TAB) {
            if ((event.getModifiersEx () & KeyEvent.CTRL_DOWN_MASK) != 0)
                state = provider.switchPreviousFocus (widget);
            else
                state = provider.switchNextFocus (widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

}

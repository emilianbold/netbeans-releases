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
            if ((event.getModifiers () & KeyEvent.SHIFT_MASK) == KeyEvent.SHIFT_MASK)
                state = provider.switchPreviousFocus (widget);
            else
                state = provider.switchNextFocus (widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

}

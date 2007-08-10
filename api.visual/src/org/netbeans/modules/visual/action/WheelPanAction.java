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

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class WheelPanAction extends WidgetAction.Adapter {

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        JComponent view = widget.getScene ().getView ();
        Rectangle visibleRect = view.getVisibleRect ();
        int amount = event.getWheelRotation () * 64;

        switch (event.getModifiers () & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK)) {
            case InputEvent.SHIFT_MASK:
                visibleRect.x += amount;
                break;
            case 0:
                visibleRect.y += amount;
                break;
            default:
                return State.REJECTED;
        }

        view.scrollRectToVisible (visibleRect);
        return State.CONSUMED;
    }

}

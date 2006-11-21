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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.EditProvider;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

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

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (event.getKeyChar () == KeyEvent.VK_ENTER) {
            provider.edit (widget);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}

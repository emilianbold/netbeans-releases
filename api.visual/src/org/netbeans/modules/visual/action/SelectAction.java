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
import org.netbeans.api.visual.action.SelectProvider;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class SelectAction extends WidgetAction.LockedAdapter {

    private boolean aiming = false;
    private Widget aimedWidget = null;
    private boolean invertSelection;
    private SelectProvider provider;

    public SelectAction (SelectProvider provider) {
        this.provider = provider;
    }

    protected boolean isLocked () {
        return aiming;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (event.getButton ()  ==  MouseEvent.BUTTON1  ||  event.getButton () == MouseEvent.BUTTON2) {
            invertSelection = (event.getModifiersEx () & MouseEvent.CTRL_DOWN_MASK) != 0;
            Point localLocation = event.getPoint ();
            if (provider.isSelectionAllowed (widget, localLocation, invertSelection)) {
                aiming = provider.isAimingAllowed (widget, localLocation, invertSelection);
                if (aiming) {
                    updateState (widget, localLocation);
                    return State.createLocked (widget, this);
                } else {
                    provider.select (widget, localLocation, invertSelection);
                    return State.CHAIN_ONLY;
                }
            }
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (aiming) {
            Point point = event.getPoint ();
            updateState (widget, point);
            if (aimedWidget != null)
                provider.select (widget, point, invertSelection);
            updateState (null, null);
            aiming = false;
            return State.CONSUMED;
        }
        return super.mouseReleased (widget, event);
    }

    private void updateState (Widget widget, Point localLocation) {
        if (widget != null  &&  ! widget.isHitAt (localLocation))
            widget = null;
        if (widget == aimedWidget)
            return;
        if (aimedWidget != null)
            aimedWidget.setState (aimedWidget.getState ().deriveWidgetAimed (false));
        aimedWidget = widget;
        if (aimedWidget != null)
            aimedWidget.setState (aimedWidget.getState ().deriveWidgetAimed (true));
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (! aiming  &&  event.getKeyChar () == KeyEvent.VK_SPACE) {
            provider.select (widget, null, (event.getModifiersEx () & MouseEvent.CTRL_DOWN_MASK) != 0);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}

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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class MouseCenteredZoomAction extends WidgetAction.Adapter {

    private double zoomMultiplier;

    public MouseCenteredZoomAction (double zoomMultiplier) {
        this.zoomMultiplier = zoomMultiplier;
    }

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        Scene scene = widget.getScene ();

        int modifiers = scene.getInputBindings ().getZoomActionModifiers ();
        if ((event.getModifiers () & modifiers) != modifiers)
            return State.REJECTED;

        int amount = event.getWheelRotation ();

        double scale = 1.0;
        while (amount > 0) {
            scale /= zoomMultiplier;
            amount --;
        }
        while (amount < 0) {
            scale *= zoomMultiplier;
            amount ++;
        }

        JComponent view = scene.getView ();
        if (view != null) {
            Rectangle viewBounds = view.getVisibleRect ();

            Point center = widget.convertLocalToScene (event.getPoint ());
            Point mouseLocation = scene.convertSceneToView (center);

            scene.setZoomFactor (scale * scene.getZoomFactor ());
            scene.validate (); // HINT - forcing to change preferred size of the JComponent view

            center = scene.convertSceneToView (center);

            view.scrollRectToVisible (new Rectangle (
                    center.x - (mouseLocation.x - viewBounds.x),
                    center.y - (mouseLocation.y - viewBounds.y),
                    viewBounds.width,
                    viewBounds.height
            ));
        } else
            scene.setZoomFactor (scale * scene.getZoomFactor ());

        return State.CONSUMED;
    }

}

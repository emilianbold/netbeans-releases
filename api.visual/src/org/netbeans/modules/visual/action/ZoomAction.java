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

import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;

/**
 * @author David Kaspar
 */
public final class ZoomAction extends WidgetAction.Adapter {

    private double zoomMultiplier;
    private boolean useAnimator;

    public ZoomAction (double zoomMultiplier, boolean useAnimator) {
        this.zoomMultiplier = zoomMultiplier;
        this.useAnimator = useAnimator;
    }

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        Scene scene = widget.getScene ();
        int amount = event.getWheelRotation ();

        if (useAnimator) {
            SceneAnimator sceneAnimator = scene.getSceneAnimator ();
            synchronized (sceneAnimator) {
                double zoom = sceneAnimator.isAnimatingZoomFactor () ? sceneAnimator.getTargetZoomFactor () : scene.getZoomFactor ();
                while (amount > 0) {
                    zoom /= zoomMultiplier;
                    amount --;
                }
                while (amount < 0) {
                    zoom *= zoomMultiplier;
                    amount ++;
                }
                sceneAnimator.animateZoomFactor (zoom);
            }
        } else {
            double zoom = scene.getZoomFactor ();
            while (amount > 0) {
                zoom /= zoomMultiplier;
                amount --;
            }
            while (amount < 0) {
                zoom *= zoomMultiplier;
                amount ++;
            }
            scene.setZoomFactor (zoom);
        }

        return WidgetAction.State.CONSUMED;
    }

}

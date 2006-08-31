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

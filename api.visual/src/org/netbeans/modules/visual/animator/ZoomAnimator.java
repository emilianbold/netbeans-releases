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
package org.netbeans.modules.visual.animator;

import org.netbeans.api.visual.animator.Animator;
import org.netbeans.api.visual.animator.SceneAnimator;

/**
 * @author David Kaspar
 */
public final class ZoomAnimator extends Animator {

    private volatile double sourceZoom;
    private volatile double targetZoom;

    public ZoomAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setZoomFactor (double zoomFactor) {
        sourceZoom = getScene ().getZoomFactor ();
        targetZoom = zoomFactor;
        start ();
    }

    public double getTargetZoom () {
        return targetZoom;
    }

    public void tick (double progress) {
        getScene ().setZoomFactor (progress >= 1.0 ? targetZoom : (sourceZoom + progress * (targetZoom - sourceZoom)));
    }

}

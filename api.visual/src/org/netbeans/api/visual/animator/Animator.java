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
package org.netbeans.api.visual.animator;

import org.netbeans.api.visual.widget.Scene;

/**
 * @author David Kaspar
 */
public abstract class Animator {

    private SceneAnimator sceneAnimator;
    private boolean reset;

    protected Animator (SceneAnimator sceneAnimator) {
        this.sceneAnimator = sceneAnimator;
    }

    protected Scene getScene () {
        return sceneAnimator.getScene ();
    }

    protected void start () {
        sceneAnimator.start (this);
    }

    public boolean isRunning () {
        return sceneAnimator.isRunning (this);
    }

    void reset () {
        reset = true;
    }

    void performTick (double progress) {
        if (reset) {
            reset = false;
            return;
        }
        tick (progress);
    }

    public abstract void tick (double progress);

}

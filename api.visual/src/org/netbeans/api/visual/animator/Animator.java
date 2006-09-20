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
 * Represents an animator. An animator is registed to a scene animator and could be started.
 * From that moment the scene animator automatically calls Animator.tick method for a solid period of time set by the scene animator.
 * In the tick method the animation has to implemented. The animation should be independent on time-duration.
 * @author David Kaspar
 */
public abstract class Animator {

    private SceneAnimator sceneAnimator;
    private boolean reset;

    /**
     * Creates an animator and assigns a scene animator.
     * @param sceneAnimator
     */
    protected Animator (SceneAnimator sceneAnimator) {
        assert sceneAnimator != null;
        this.sceneAnimator = sceneAnimator;
    }

    /**
     * Returns a scene that is related to the scene animator.
     * @return the scene
     */
    protected final Scene getScene () {
        return sceneAnimator.getScene ();
    }

    /**
     * Registers and starts the animation.
     */
    protected final void start () {
        sceneAnimator.start (this);
    }

    /**
     * Returns whether the animation is running.
     * @return true if still running
     */
    public final boolean isRunning () {
        return sceneAnimator.isRunning (this);
    }

    final void reset () {
        reset = true;
    }

    final void performTick (double progress) {
        if (reset) {
            reset = false;
            return;
        }
        tick (progress);
    }

    /**
     * Called for performing the animation based on a progress value. The value is a double number in interval from 0.0 to 1.0 (including).
     * The 0.0 value represents beginning, the 1.0 value represents the end.
     * @param progress the progress
     */
    protected abstract void tick (double progress);

}

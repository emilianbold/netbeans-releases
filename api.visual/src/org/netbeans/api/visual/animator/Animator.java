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
package org.netbeans.api.visual.animator;

import org.netbeans.api.visual.widget.Scene;

/**
 * Represents an animator. An animator is registed to a scene animator and could be started.
 * From that moment the scene animator automatically calls Animator.tick method for a solid period of time set by the scene animator.
 * In the tick method the animation has to implemented. The animation should be independent on time-duration.
 *
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

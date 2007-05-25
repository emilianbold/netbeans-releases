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

/**
 * This is an animator event which is used by <code>AnimatorListener</code>.
 * It contains a reference to the animator and animation progress value which is can be used only in case of
 * <code>AnimatorListener.animatorPreTick</code> and <code>AnimatorListener.animatorPostTick</code> methods.
 *
 * @author David Kaspar
 */
public final class AnimatorEvent {

    private Animator animator;
    private double progress;

    AnimatorEvent (Animator animator) {
        this (animator, Double.NaN);
    }

    AnimatorEvent (Animator animator, double progress) {
        this.animator = animator;
        this.progress = progress;
    }

    /**
     * Returns the related animator instance.
     * @return the animator
     */
    public Animator getAnimator () {
        return animator;
    }

    /**
     * The animation progress value. Contains valid value only when the event is received as an argument of
     * <code>AnimatorListener.animatorPreTick</code> and <code>AnimatorListener.animatorPostTick</code> methods.
     * @return the progress value; valid range is from 0.0 to 1.0 where 0.0 represents animator-start and 1.0 represents animator-end;
     *     Double.NaN if the progress value is not available
     */
    public double getProgress () {
        return progress;
    }

}

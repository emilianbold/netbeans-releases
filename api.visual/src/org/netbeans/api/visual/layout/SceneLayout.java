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
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Scene;

/**
 * This is used for a one-time operations that had to be invoked after the scene is initialized and/or validated.
 * This is usually used for applying graph-oriented layouts where the layout requires to calculate boundaries
 * of widgets before the layout is invokes.
 * <p>
 * The SceneLayout can be invoked by SceneLayout.invokeLayout method. This method just schedules the scene layout
 * to be performed after the scene validation is done.
 *
 * @author David Kaspar
 */
public abstract class SceneLayout {

    private Scene.SceneListener listener = new LayoutSceneListener ();
    private Scene scene;
    private volatile boolean attached;

    /**
     * Creates a scene layout that is related to a specific scene.
     * @param scene the related scene
     */
    protected SceneLayout (Scene scene) {
        assert scene != null;
        this.scene = scene;
    }

    private void attach () {
        synchronized (this) {
            if (attached)
                return;
            attached = true;
        }
        scene.addSceneListener (listener);
    }

    private void deatach () {
        synchronized (this) {
            if (! attached)
                return;
            attached = false;
        }
        scene.removeSceneListener (listener);
    }

    /**
     * Schedules the performing of this scene layout just immediately after the scene validation.
     * It also calls scene revalidation. The Scene.validate method has to be manually called after.
     */
    public final void invokeLayout () {
        attach ();
        scene.revalidate ();
    }

    /**
     * Schedules the performing of this scene layout just immediately after the scene validation.
     * It also calls scene revalidation. The Scene.validate method is called automatically at the end.
     */
    public final void invokeLayoutImmediately () {
        attach ();
        scene.revalidate ();
        scene.validate ();
    }

    /**
     * Called immediately after the scene validation and is responsible for performing the logic e.g. graph-oriented layout.
     */
    protected abstract void performLayout ();

    private final class LayoutSceneListener implements Scene.SceneListener {

        public void sceneRepaint () {
        }

        public void sceneValidating () {
        }

        public void sceneValidated () {
            deatach ();
            performLayout ();
        }
    }

}

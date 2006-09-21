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
     * Schedule the performing of this scene layout just immediately after the scene validation.
     * It also calls scene revalidation.
     */
    public final void invokeLayout () {
        attach ();
        scene.revalidate ();
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

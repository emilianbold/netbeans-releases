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
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public abstract class SceneLayout {

    private Scene.SceneListener listener = new LayoutSceneListener ();
    private Scene scene;
    private volatile boolean attached;

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

    public void invokeLayout () {
        attach ();
        scene.revalidate ();
    }

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

    @Deprecated
    public static final class DevolveWidgetLayout extends SceneLayout {

        private Widget widget;
        private Layout devolveLayout;
        private boolean animate;

        @Deprecated
        public DevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
            super (widget.getScene ());
            assert devolveLayout != null;
            this.widget = widget;
            this.devolveLayout = devolveLayout;
            this.animate = animate;
        }

        protected void performLayout () {
            devolveLayout.layout (widget);
            for (Widget child : widget.getChildren ()) {
                if (animate)
                    widget.getScene ().getSceneAnimator ().animatePreferredLocation (child, child.getLocation ());
                else
                    child.setPreferredLocation (child.getLocation ());
            }
        }
    }

}

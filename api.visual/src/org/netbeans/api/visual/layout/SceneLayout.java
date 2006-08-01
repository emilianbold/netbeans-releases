/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public abstract class SceneLayout {

    private Scene.ValidateListener listener = new LayoutValidateListener ();
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
        scene.addValidateListener (listener);
    }

    private void deatach () {
        synchronized (this) {
            if (! attached)
                return;
            attached = false;
        }
        scene.removeValidateListener (listener);
    }

    public void invokeLayout () {
        attach ();
        scene.revalidate ();
    }

    protected abstract void performLayout ();

    private final class LayoutValidateListener implements Scene.ValidateListener {

        public void sceneValidating () {
        }

        public void sceneValidated () {
            deatach ();
            performLayout ();
        }
    }

    public static final class DevolveWidgetLayout extends SceneLayout {

        private Widget widget;
        private Layout devolveLayout;
        private boolean animate;

        public DevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
            super (widget.getScene ());
            assert devolveLayout != null;
            this.widget = widget;
            this.devolveLayout = devolveLayout;
            this.animate = animate;
        }

        protected void performLayout () {
            Layout oldLayout = widget.getLayout ();
            try {
                widget.setLayout (devolveLayout);
                Scene scene = widget.getScene ();
                scene.validate ();
                SceneAnimator sceneAnimator = scene.getSceneAnimator ();
                for (Widget child : widget.getChildren ()) {
                    if (animate)
                        sceneAnimator.animatePreferredLocation (child, child.getLocation ());
                    else
                        child.setPreferredLocation (child.getLocation ());
                }
            } finally {
                widget.setLayout (oldLayout);
            }
        }
    }

}

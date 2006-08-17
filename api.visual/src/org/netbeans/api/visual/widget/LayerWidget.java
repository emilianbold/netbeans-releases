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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.layout.Layout;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - reevaluateLayout
public class LayerWidget extends Widget {

    private Layout devolveLayout;

    public LayerWidget (Scene scene) {
        super (scene);
    }

    public boolean isHitAt (Point localLocation) {
        return false;
    }

    protected boolean isRepaintRequiredForRevalidating () {
        return false;
    }

    void layout (boolean fullValidation) {
        super.layout (fullValidation);
        rejustify ();
    }

    @Deprecated
    public Layout getDevolveLayout () {
        return devolveLayout;
    }

    @Deprecated
    public void setDevolveLayout (Layout devolveLayout) {
        this.devolveLayout = devolveLayout;
    }

    @Deprecated
    public void reevaluateLayout (boolean animate) {
        reevaluateLayout (getDevolveLayout (), animate);
    }

    /**
     * Reevaluates layout of the layer widget using a specified layout.
     * <p>
     * Note: This method does not work corectly when the scene does not have Graphics2D instance assigned - means Scene is not shown on a display. This happens when a layout algorithm calculates with children boundaries only.
     * <p>
     * Note: Calling this method has side-effects.
     * Best if this method is called alone (without any other library method) during a single scene validation - means during the initialization or AWT event processing.
     * The layer widget will temporarily set different layout.
     * Scene.validate method is called.
     * Changes preferredLocation of children Widget.
     * At the end the layer widget is marked for revalidation.
     *
     * @param layout the layout using for reevaluating
     * @param animate
     * @deprecated Because of many side-effects, this method could be redesigned later.
     */
    @Deprecated
    public void reevaluateLayout (Layout layout, boolean animate) {
        if (layout == null)
            return;

        Layout oldLayout = getLayout ();
        try {
            setLayout (layout);
            getScene ().validate ();
            // The child Widgets have now had their 'location' set... Now, we need to set their preferredLocation.
            SceneAnimator sceneAnimator = getScene ().getSceneAnimator ();
            for (Widget child : getChildren ()) {
                if (animate)
                    sceneAnimator.animatePreferredLocation (child, child.getLocation ());
                else
                    child.setPreferredLocation (child.getLocation ());
            }
        } finally {
            setLayout (oldLayout);
        }
    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.visual.widget;

import org.netbeans.modules.visual.widget.BirdViewWindow;

import java.awt.*;

/**
 * This class controls a bird view created for a specific scene. The bird is tracking mouse-cursor over the main scene view.
 * You can specify a separate zoom-factor and you can enable and disable it by calling <code>show</code> and <code>hide</code> methods.
 * <p>
 * When a bird view is enabled then it consumes all events of a main scene view therefore you cannot do anything except
 * watch the scene with bird view.
 *
 * @since 2.7
 * @author David Kaspar
 */
public final class BirdViewController {

    private BirdViewWindow birdView;

    BirdViewController (Scene scene) {
        birdView = new BirdViewWindow (scene);
    }

    /**
     * Sets a zoom factor of the bird view.
     * @param zoomFactor the zoom factor
     * @since 2.7
     */
    public void setZoomFactor (double zoomFactor) {
        birdView.setZoomFactor (zoomFactor);
    }

    /**
     * Sets a size of the bird view window.
     * @param size the window size
     * @since 2.7
     */
    public void setWindowSize (Dimension size) {
        birdView.setWindowSize (size);
    }

    /**
     * Enables the bird view. It means that the bird view window will be visible while a mouse cursor is over the visible
     * area of the main scene view.
     * <p>
     * Note: Has to be invoked after <code>Scene.createView</code> method.
     * <p>
     * Note: An user has to initially move cursor over the visible area of the main scene view
     * to show the window up for the first time after the method call.
     * @since 2.7
     */
    public void show () {
        birdView.invokeShow ();
    }

    /**
     * Disables the bird view. It means the bird view window is hidden and the main scene view is not blocked for events.
     * @since 2.7
     */
    public void hide () {
        birdView.invokeHide ();
    }

}

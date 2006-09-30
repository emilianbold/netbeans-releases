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

import java.awt.*;

/**
 * The layer widget represents a transparent widget which functionality is similar to JGlassPane.
 * The layer widget is used for speed optimalization too since it is not repainted when the widget is re-layout.
 * <p>
 * It can be used widgets organization. A scene usually has layer widgets directly underneath.
 * E.g. each layer widget is used different purpose:
 * background for widgets on background,
 * main layer for node widgets,
 * connection layer for edge widgets,
 * interraction layer for temporary widgets created/used by actions.
 *
 * @author David Kaspar
 */
public class LayerWidget extends Widget {

    /**
     * Creates a layer widget.
     * @param scene the scene
     */
    public LayerWidget (Scene scene) {
        super (scene);
    }

    /**
     * Returns whether a specified local location is part of the layer widget.
     * @param localLocation the local location
     * @return always false
     */
    public boolean isHitAt (Point localLocation) {
        return false;
    }

    /**
     * Returns whether the layer widget requires to repainted after revalidation.
     * @return always false
     */
    protected boolean isRepaintRequiredForRevalidating () {
        return false;
    }

    void layout (boolean fullValidation) {
        super.layout (fullValidation);
        justify ();
    }

}

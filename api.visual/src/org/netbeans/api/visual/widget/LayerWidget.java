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

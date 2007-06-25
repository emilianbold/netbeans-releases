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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * This class represents a connection widget in the VMD visualization style. Can be combined with any other widget.
 *
 * @author David Kaspar
 */
public class VMDConnectionWidget extends ConnectionWidget {

    private VMDColorScheme scheme;

    /**
     * Creates a connection widget with a specific router.
     * @param scene the scene
     * @param router the router
     */
    public VMDConnectionWidget (Scene scene, Router router) {
        this (scene, VMDFactory.getOriginalScheme ());
        if (router != null)
            setRouter (router);
    }

    /**
     * Creates a connection widget with a specific color scheme.
     * @param scene the scene
     * @param scheme the color scheme
     */
    public VMDConnectionWidget (Scene scene, VMDColorScheme scheme) {
        super (scene);
        assert scheme != null;
        this.scheme = scheme;
        scheme.installUI (this);
        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        scheme.updateUI (this, previousState, state);
    }

}

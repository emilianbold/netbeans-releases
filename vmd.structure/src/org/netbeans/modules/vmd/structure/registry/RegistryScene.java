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
package org.netbeans.modules.vmd.structure.registry;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;

import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public class RegistryScene extends ObjectScene {

    public RegistryScene () {
        setOpaque (false);
        setLayout (LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.LEFT_TOP, 10));
        getActions ().addAction (ActionFactory.createZoomAction ());
        getActions ().addAction (ActionFactory.createPanAction ());
    }

    public void clear () {
        ArrayList<Object> objects = new ArrayList<Object> (getObjects ());
        for (Object object : objects) {
            findWidget (object).removeFromParent ();
            removeObject (object);
        }
    }

    public void addRootNode (String id, RegistryWidget node) {
        addChild (node);
        addObject (id, node);
    }

}

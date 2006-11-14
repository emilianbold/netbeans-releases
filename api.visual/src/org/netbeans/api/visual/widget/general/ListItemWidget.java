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
package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * This class represents a general list item widget. Right now it presented as a label.
 * <p>
 * WARNING: This class is meant to be redesigned later.

 * @author David Kaspar
 */
public class ListItemWidget extends LabelWidget {

    /**
     * Creates a list item widget.
     * @param scene the scene
     */
    public ListItemWidget (Scene scene) {
        super (scene);

        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        setBorder (BorderFactory.createCompositeBorder (BorderFactory.createEmptyBorder (8, 2), lookFeel.getMiniBorder (state)));
        setForeground (lookFeel.getForeground (state));
    }

}

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

import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.anchor.PointShapeFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.Utilities;

import java.awt.*;

/**
 * This class represents a connection widget in the VMD visualization style. Can be combined with any other widget.
 *
 * @author David Kaspar
 */
public class VMDConnectionWidget extends ConnectionWidget {

    private static final PointShape POINT_SHAPE_IMAGE = PointShapeFactory.createImagePointShape (Utilities.loadImage ("org/netbeans/modules/visual/resources/vmd-pin.png")); // NOI18N

    private static final Color COLOR_NORMAL = VMDNodeBorder.COLOR_BORDER;
    private static final Color COLOR_HOVERED = Color.BLACK;
    private static final Color COLOR_HIGHLIGHTED = new Color (49, 106, 197);

    /**
     * Creates a connection widget.
     * @param scene the scene
     * @param router
     */
    public VMDConnectionWidget (Scene scene, Router router) {
        super (scene);
        setRouter (router);
        setSourceAnchorShape (AnchorShape.NONE);
        setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        setPaintControlPoints (true);
        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (state.isHovered ())
            setForeground (COLOR_HOVERED);
        else if (state.isSelected ())
            setForeground (VMDNodeWidget.COLOR_SELECTED);
        else if (state.isHighlighted ())
            setForeground (COLOR_HIGHLIGHTED);
        else if (state.isFocused ())
            setForeground (COLOR_HOVERED);
        else
            setForeground (COLOR_NORMAL);

        if (state.isSelected ()) {
            setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
            setEndPointShape (PointShape.SQUARE_FILLED_BIG);
        } else {
            setControlPointShape (PointShape.NONE);
            setEndPointShape (POINT_SHAPE_IMAGE);
        }
    }

}

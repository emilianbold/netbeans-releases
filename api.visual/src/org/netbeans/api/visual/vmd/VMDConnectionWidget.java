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
 * This class represents a connection widget in the VMD plug-in.
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

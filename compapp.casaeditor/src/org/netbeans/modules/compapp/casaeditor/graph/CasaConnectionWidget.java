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

package org.netbeans.modules.compapp.casaeditor.graph;

import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;
import org.netbeans.api.visual.widget.ConnectionWidget;


public class CasaConnectionWidget extends ConnectionWidget {

    private static final Stroke STROKE_DEFAULT  = new BasicStroke (1.0f);
    private static final Stroke STROKE_HOVERED  = new BasicStroke (1.5f);
    private static final Stroke STROKE_SELECTED = new BasicStroke (2.0f);
    
    
    public CasaConnectionWidget (Scene scene) {
        super (scene);
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
        if (state.isSelected () || state.isFocused()) {
            bringToFront();
            setStroke(STROKE_SELECTED);
            setForeground (CasaFactory.getCasaCustomizer().getCOLOR_SELECTION());
        } else if (state.isHovered () || state.isHighlighted()) {
            bringToFront();
            setStroke(STROKE_HOVERED);
            setForeground (CasaFactory.getCasaCustomizer().getCOLOR_HOVERED_EDGE());
        } else {
            setStroke(STROKE_DEFAULT);
            setForeground (CasaFactory.getCasaCustomizer().getCOLOR_CONNECTION_NORMAL());
        }
    }
    
    public void setForegroundColor(Color color) {
        setForeground(color);
    }
}

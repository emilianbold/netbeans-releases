/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.edm.editor.widgets;

import java.awt.Color;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.anchor.PointShapeFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.Utilities;



/**
 * This class represents a connection widget in the VMD visualization style. Can be combined with any other widget.
 *
 * @author David Kaspar
 */
public class EDMConnectionWidget extends ConnectionWidget {

    private static final PointShape POINT_SHAPE_IMAGE =
            PointShapeFactory.createImagePointShape(
            Utilities.loadImage("org/netbeans/modules/edm/editor/resources/edm-pin.png")); // NOI18N
    private static final Color COLOR_NORMAL = EDMNodeBorder.COLOR_CONNECTION;
    private static final Color COLOR_HOVERED = new Color(85, 140, 85);
    private static final Color COLOR_HIGHLIGHTED = new Color(49, 106, 197);
    private static final Color COLOR_SELECTED = new Color(150, 200, 150);

    /**
     * Creates a connection widget.
     * @param scene the scene
     * @param router
     */
    public EDMConnectionWidget(Scene scene, Router router) {
        super(scene);
        setRouter(router);
        setSourceAnchorShape(AnchorShape.NONE);
        setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        setPaintControlPoints(true);
        setState(ObjectState.createNormal());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (state.isHovered()) {
            setForeground(COLOR_HOVERED);
        } else if (state.isSelected()) {
            setForeground(COLOR_SELECTED);
        } else if (state.isHighlighted()) {
            setForeground(COLOR_HIGHLIGHTED);
        } else if (state.isFocused()) {
            setForeground(COLOR_HOVERED);
        } else {
            setForeground(COLOR_NORMAL);
        }
        
        if (state.isSelected()) {
            setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
            setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        } else {
            setControlPointShape(PointShape.NONE);
            setEndPointShape(POINT_SHAPE_IMAGE);
        }
    }
}

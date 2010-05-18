/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.compapp.casaeditor.graph.layout;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;

/**
 * Modified to only register collisions from:
 * - connection widgets with no shared endpoints with another connection widget
 * - CasaNodeWidget widgets
 *
 * @author Josh Sandusky
 */
public class CasaCollisionCollector implements ConnectionWidgetCollisionsCollector {

    public static final int MAX_ORTHOGONAL_CONNECTIONS = 15;
    public static final int MAX_ORTHOGONAL_NODES       = 15;
    
    private static final int SPACING_EDGE = 4;
    private static final int SPACING_NODE = 6;
    private LayerWidget[] layers;

    
    public CasaCollisionCollector (LayerWidget... layers) {
        this.layers = layers;
    }

    
    public void collectCollisions (
            ConnectionWidget connectionWidget, 
            List<Rectangle> verticalCollisions, 
            List<Rectangle> horizontalCollisions)
    {
        CasaModelGraphScene scene = (CasaModelGraphScene) connectionWidget.getScene();
        CasaComponent component = (CasaComponent) scene.findObject(connectionWidget);
        if (component == null || !component.isInDocumentModel()) {
            return;
        }
        
        CasaComponent source = scene.getEdgeSource(component);
        CasaComponent target = scene.getEdgeTarget(component);
        
        for (Widget widget : getWidgets ()) {
            
            if (!widget.isValidated ()) {
                continue;
            }
            
            if (widget == connectionWidget) {
                continue;
            }
            
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget iterConnection = (ConnectionWidget) widget;
                if (!iterConnection.isRouted ()) {
                    continue;
                }
                
                CasaComponent iterComponent = (CasaComponent) scene.findObject(iterConnection);
                if (iterComponent == null || !iterComponent.isInDocumentModel()) {
                    return;
                }
                
                // If there are any shared endpoints, then do not register the connection
                // as colliding with the given iterConnection.
                if (source != null) {
                    CasaComponent iterSource = scene.getEdgeSource(iterComponent);
                    if (source == iterSource) {
                        continue;
                    }
                }
                if (target != null) {
                    CasaComponent iterTarget = scene.getEdgeTarget(iterComponent);
                    if (target == iterTarget) {
                        continue;
                    }
                }
                
                List<Point> controlPoints = iterConnection.getControlPoints ();
                int last = controlPoints.size () - 1;
                for (int i = 0; i < last; i ++) {
                    Point point1 = controlPoints.get (i);
                    Point point2 = controlPoints.get (i + 1);
                    if (point1.x == point2.x) {
                        Rectangle rectangle = new Rectangle (point1.x, Math.min (point1.y, point2.y), 0, Math.abs (point2.y - point1.y));
                        rectangle.grow (SPACING_EDGE, SPACING_EDGE);
                        verticalCollisions.add (rectangle);
                    } else if (point1.y == point2.y) {
                        Rectangle rectangle = new Rectangle (Math.min (point1.x, point2.x), point1.y, Math.abs (point2.x - point1.x), 0);
                        rectangle.grow (SPACING_EDGE, SPACING_EDGE);
                        horizontalCollisions.add (rectangle);
                    }
                }
                
            // Check that the widget is a node widget.
            // This allows lines to go through other widgets, such as region labels.
            } else if (widget instanceof CasaNodeWidget) {
                Rectangle bounds = widget.getBounds ();
                Rectangle rectangle = widget.convertLocalToScene (bounds);
                rectangle.grow (SPACING_NODE, SPACING_NODE);
                verticalCollisions.add (rectangle);
                horizontalCollisions.add (rectangle);
            }
        }
    }

    protected Collection<Widget> getWidgets () {
        ArrayList<Widget> list = new ArrayList<Widget> ();
        for (LayerWidget layer : layers)
            list.addAll (layer.getChildren ());
        return list;
    }
}

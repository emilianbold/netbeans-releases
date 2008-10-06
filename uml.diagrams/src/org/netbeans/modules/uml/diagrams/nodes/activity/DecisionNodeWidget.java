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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ResizeToContentMarker;

/**
 *
 * @author thuy
 */
public class DecisionNodeWidget extends ControlNodeWidget implements ResizeToContentMarker
{
    public static final int MIN_NODE_WIDTH =  20;
    public static final int MIN_NODE_HEIGHT = 30;
    
    public DecisionNodeWidget(Scene scene, String path)
    {
        super(scene, path); 
    }
    
    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if (presentation != null)
        {
            Scene scene = getScene();
            
            //create main view 
            PolygonWidget polygonWidget = new PolygonWidget(scene,
                                                 getResourcePath(),
                                                 bundle.getString("LBL_body"));
             polygonWidget.setMinimumSize(new Dimension(
                                      MIN_NODE_WIDTH, MIN_NODE_HEIGHT));
            polygonWidget.setUseGradient(useGradient);
//            polygonWidget.setCustomizableResourceTypes(
//                    new ResourceType[]{ResourceType.BACKGROUND});
            polygonWidget.setOpaque(true);
            setCurrentView(polygonWidget);
        }
        super.initializeNode(presentation);
    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.DECISIONNODEWIDGET.toString();
    }
    
    private class PolygonWidget extends CustomizableWidget
    {
        public PolygonWidget(Scene scene, String propID, String propDisplayName)
        {
            super(scene, propID, propDisplayName);
        }

        @Override
        protected void paintWidget()
        {
            // draw the outline of the polygon
            Graphics2D graphics = getGraphics();
            Color currentColor = graphics.getColor();
            graphics.setColor(Color.BLACK);
            graphics.drawPolygon(createPolygon());
            
            //reset to previous color
            graphics.setColor(currentColor);
        }
        
        @Override
        public void paintBackground()
        {
            Rectangle bounds = getBounds();
            Paint bgColor = getBackground();
            Color bg = (Color) bgColor;
            if (isGradient())
            {
                bgColor = new GradientPaint(
                        0, 0, Color.WHITE,
                        0, bounds.height, bg);
            }
            
           Graphics2D graphics = getGraphics();
           Paint currentPaint = graphics.getPaint();
           graphics.setPaint(bgColor);
           graphics.fillPolygon(createPolygon());
           
           //reset to previous paint
           graphics.setPaint(currentPaint);
        }
        
       private Polygon createPolygon ()
       {
            Rectangle bounds = getBounds();
            int cx = (int)bounds.getCenterX();
            int cy = (int)bounds.getCenterY();
            
            Polygon polygon = new Polygon();
            // create 4 vertices of  the polygon
            polygon.addPoint(bounds.x, cy);   //center left
            polygon.addPoint(cx, bounds.y);   // center top
            polygon.addPoint(bounds.x+bounds.width, cy );  //center right
            polygon.addPoint(cx, bounds.y+bounds.height);  // center bottom
            return polygon;
       }
    }
}

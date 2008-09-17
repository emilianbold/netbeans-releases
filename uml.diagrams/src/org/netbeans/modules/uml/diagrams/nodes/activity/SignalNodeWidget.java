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
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.ISignalNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.MultilineEditableCompartmentWidget;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;

/**
 *
 * @author thuy
 */
public class SignalNodeWidget extends ActivityNodeWidget
{
    public static final int MIN_NODE_WIDTH =  50;
    public static final int MIN_NODE_HEIGHT = 40;
    
    public SignalNodeWidget(Scene scene)
    {
        super(scene, true, false);
        setMinimumSize(new Dimension(MIN_NODE_WIDTH, MIN_NODE_HEIGHT));
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if (presentation != null)
        {
            ISignalNode element = (ISignalNode) presentation.getFirstSubject();
            Scene scene = getScene();
            
            //create main view 
            MainViewWidget mainView = new MainViewWidget(scene,
                                                         getResourcePath(),
                                                         bundle.getString("LBL_body"));    
            mainView.setLayout(
                    LayoutFactory.createVerticalFlowLayout(
                    LayoutFactory.SerialAlignment.JUSTIFY, 2));
            
            mainView.setUseGradient(useGradient);
            mainView.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
            mainView.setCustomizableResourceTypes(
                    new ResourceType[]{ResourceType.BACKGROUND});
            mainView.setOpaque(true);

            // stereotype widget
            mainView.addChild(this.createStereoTypeWidget());
            enableStereoTypeWidget(element);
       
            // create multiline editable widget
            nameWidget = new MultilineEditableCompartmentWidget(scene, "", null,
                                                                 mainView,
                                                                 getWidgetID()+".name",
                                                                 bundle.getString("LBL_text"));
            nameWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
            String labelStr = element.getNameWithAlias();
            nameWidget.setLabel(labelStr != null && labelStr.trim().length() > 0 ? labelStr : "");
            
            mainView.addChild(nameWidget, 1);
            
            //taggedvalue widget
            mainView.addChild(createTaggedValueWidget());
            enableTaggedValueWidget(element);
            
            setCurrentView(mainView);
        }
        super.initializeNode(presentation);
    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.SIGNALNODEWIDGET.toString();
    }

    @Override
    public Dimension getDefaultMinimumSize()
    {
        return new Dimension(MIN_NODE_WIDTH, MIN_NODE_HEIGHT);
    }
    
    private class MainViewWidget extends CustomizableWidget
    {
        public MainViewWidget(Scene scene, String propID, String propDisplayName)
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
            //    |----------\   
            //    |             \ 
            //    |              \
            //    |              /
            //    |             /
            //    |----------/
            //
            Rectangle bounds = getBounds();
            int width = bounds.width;
            int height = bounds.height;
            
            // make the point inset either 15 or width/5, whichever is smaller
            int pointInset = Math.min(15, width/5);
            Polygon polygon = new Polygon();
            // create 5 vertices of  the polygon
            polygon.addPoint(bounds.x, bounds.y);                                          //top left 
            polygon.addPoint(bounds.x+(width-pointInset), bounds.y );             //top right
            polygon.addPoint(bounds.x+width, ((int)bounds.getCenterY()) );       //midle right 
            polygon.addPoint(bounds.x+(width-pointInset), bounds.y+height );  //bottom right
            polygon.addPoint(bounds.x, bounds.y+height );                              //bottom left
            
            return polygon;
       }
    }
}

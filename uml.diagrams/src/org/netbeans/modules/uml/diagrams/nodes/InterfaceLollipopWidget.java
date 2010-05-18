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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import org.netbeans.modules.uml.drawingarea.view.CircleShape;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.uml.drawingarea.view.ContextViewWidget;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author treyspiva
 */
public class InterfaceLollipopWidget extends Widget 
        implements ContextViewWidget, PropertyChangeListener
{
    
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    
    public InterfaceLollipopWidget(Scene scene)
    {
        super(scene);
        
        setLayout(LayoutFactory.createVerticalFlowLayout());
        
        CircleWidget circleWidget = new CircleWidget(scene, 10);
        addChild(circleWidget);
        
        
        UMLNameWidget nameWidget = new UMLNameWidget(scene, UMLWidgetIDString.UMLCLASSWIDGET.toString());
        nameWidget.setShowIcon(false);
        nameWidget.hideStereotype("interface");
        addChild(nameWidget);
        
        lookupContent.add(new CircleShape(circleWidget, 10));
        
        // Inherit the parents color.
        setBackground(null);
        setFont(null);
        setForeground(null);
    }

    @Override
    public Lookup getLookup()
    {
        return lookup;
    }

    
    public void showingView(IPresentationElement element)
    {
        UMLNameWidget widget = (UMLNameWidget)getChildren().get(1);
        widget.initialize(element.getFirstSubject());
        widget.setNameFont(getFont().deriveFont(Font.BOLD, getFont().getSize() + 4));        
    }
    
    public void removingView()
    {
        
    }
    
    public Rectangle getShape()
    {
        Widget widget = getChildren().get(0);
        return widget.getBounds();
    }
    
    protected class CircleWidget extends Widget
    {
        int radius = 10;
        
        public CircleWidget(Scene scene, int r)
        {
            super(scene);
            radius = r;
            setOpaque(true);
            
            // Inherit the parents color.
            setBackground(null);
            setFont(null);
            setForeground(null);
        }
        
        @Override
        protected Rectangle calculateClientArea()
        {
            int r = (int) Math.ceil(radius);
            return new Rectangle(-r, -r, 2 * r + 1, 2 * r + 1);
        }

        @Override
        protected void paintBackground()
        {
            Rectangle bounds = getBounds();
            
            int r = (int) Math.ceil(radius);
            Graphics2D g = getGraphics();
            g.setColor(getForeground());
            
            int midX = bounds.x + (int)(bounds.width / 2);
            int midY = bounds.y + (int)(bounds.height / 2);
            
            Paint bg = getBackground();
            if((bg instanceof Color) && (UMLNodeWidget.useGradient() == true))
            {
                Color bgColor = (Color)bg;
                
                int startY = midY - r;
                GradientPaint paint = new GradientPaint(midX, startY, Color.WHITE,
                                                        midX, startY + (r * 2), 
                                                        bgColor);
                g.setPaint(paint);
            }
            else
            {
                g.setPaint(bg);
            }
            
            g.fillOval(midX - r, midY - r, 2 * r, 2 * r);
        }
        
        @Override
        protected void paintWidget()
        {
            Rectangle bounds = getBounds();
            
            int r = (int) Math.ceil(radius);
            Graphics2D g = getGraphics();
            g.setColor(getForeground());
            
            int midX = bounds.x + (int)(bounds.width / 2);
            int midY = bounds.y + (int)(bounds.height / 2);
            
            g.drawOval(midX - r, midY - r, 2 * r, 2 * r);
        }
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        UMLNameWidget widget = (UMLNameWidget)getChildren().get(1);
        widget.propertyChange(event);
    }
}

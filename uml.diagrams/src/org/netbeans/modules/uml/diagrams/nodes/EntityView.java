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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.CircleShape;
import org.netbeans.modules.uml.drawingarea.view.ContextViewWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author treyspiva
 */
public class EntityView extends Widget 
        implements ContextViewWidget, PropertyChangeListener
{
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    
    public EntityView(Scene scene)
    {
        super(scene);
        
        setLayout(LayoutFactory.createVerticalFlowLayout());
        
        EntityWidget entityWidget = new EntityWidget(scene, 20);
        addChild(entityWidget);
        
        UMLNameWidget name = new UMLNameWidget(scene, UMLWidgetIDString.UMLCLASSWIDGET.toString());
        name.setShowIcon(false);
        name.hideStereotype("entity");
        addChild(name);
        
        lookupContent.add(new CircleShape(entityWidget, 20));
        
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected class EntityWidget extends Widget
    {
        int radius = 10;
        
        public EntityWidget(Scene scene, int r)
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
            return new Rectangle(0, 0, 2 * r + 1, 2 * r + 7);
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
            g.setColor(Color.BLACK);
            
            int midX = bounds.x + (int)(bounds.width / 2);
            int midY = bounds.y + (int)(bounds.height / 2);
            int halfArm = r / 3;
            
            int ovalTop = midY - r - 2;
            g.drawOval(midX - r, ovalTop, 2 * r, 2 * r);
            g.drawLine(midX - r + halfArm, midY + r + 4, midX + r - halfArm, midY + r + 4);
            g.drawLine(midX, midY  + 3 + r, midX, midY + r - 1);
        }
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        UMLNameWidget widget = (UMLNameWidget)getChildren().get(1);
        widget.propertyChange(event);
    }
}

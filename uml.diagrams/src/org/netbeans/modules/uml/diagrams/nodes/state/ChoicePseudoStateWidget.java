/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.nodes.state;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author Sheryl Su
 */
public class ChoicePseudoStateWidget extends CustomizableWidget
{
    public static final int width = 20;
    
    public ChoicePseudoStateWidget(Scene scene, String propID, String propDisplayName)
    {
        super(scene, propID, propDisplayName);
        setOpaque(true);
    }
    
    @Override
    protected Rectangle calculateClientArea()
    {
        if (getBounds()==null || !isPreferredBoundsSet())
        {
            return new Rectangle(0, 0, width, width);
        }
       return  super.calculateClientArea();
    }

    @Override
    protected void paintWidget()
    {
        Graphics2D graphics = getGraphics();
        Color currentColor = graphics.getColor();
        graphics.setColor(getForeground());
        
        graphics.drawPolygon(getPolygon());
        graphics.setColor(currentColor);
    }

    protected void paintBackground()
    {
        Paint bgColor = getBackground();
            Color bg = (Color) bgColor;
            if (UMLNodeWidget.useGradient())
            {
                bgColor = new GradientPaint(
                        0, 0, Color.WHITE,
                        0, getBounds().height, bg);
            }
            
           Graphics2D graphics = getGraphics();
           Paint currentPaint = graphics.getPaint();
           graphics.setPaint(bgColor);
           graphics.fillPolygon(getPolygon());
           
           //reset to previous paint
           graphics.setPaint(currentPaint);
        
    }
    
    private Polygon getPolygon()
    {
        Rectangle rec = getBounds();
    
        return new Polygon(new int[]{rec.x + rec.width/2, rec.x, rec.x + rec.width/2, rec.x + rec.width},
                           new int[]{rec.y, rec.y + rec.height/2, rec.y + rec.height, rec.y + rec.height/2},
                           4);
    }
}

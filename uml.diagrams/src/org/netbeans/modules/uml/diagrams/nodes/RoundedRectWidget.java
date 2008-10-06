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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;

/**
 * @author thuy
 */
public class RoundedRectWidget extends CustomizableWidget
{

    public static final BasicStroke DEFAULT_DASH = 
            new BasicStroke(1,BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND,
            BasicStroke.JOIN_MITER,
            new float[]{10,10 }, 0);
    
    private ArcDim arcDim = new ArcDim(20, 20);
    private int insetWidth = 1;
    private int insetHeight = 1;
    private Paint fillColor;
    private Paint drawColor = Color.BLACK;
    private Stroke stroke;

    /**
     * Creates a rounded rectangle widget with default values of arc width, arc height, insets, draw color and stroke
     * @param scene 
     * @param propId the resource property id which is used to lookup for the fill color in the resource table
     * @param propDisplayName the displayed name of the property.
     */
    public RoundedRectWidget(Scene scene, String propId, String propDisplayName)
    {
        super(scene, propId, propDisplayName);
        
        // Always get the parents foreground.
        setForeground(null);
    }

   /**
     * Creates a rounded rectangle widget with default values of arc width, arc height, insets and draw color
     * @param scene 
     * @param propId the resource property id which is used to lookup for the fill color in the resource table
     * @param propDisplayName the displayed name of the property.
     * @param stroke the stroke used to draw the shape of the widget
     */
    public RoundedRectWidget(Scene scene, String propId, String propDisplayName,
                              Stroke stroke)
    {
        this(scene, propId, propDisplayName);
        this.stroke = stroke;
    }

    /**
     * Creates a rounded rectangle border with specified attributes
     * @param scene 
     * @param propId the resource property id which is used to lookup for the fill color in the resource table
     * @param propDisplayName the displayed name of the property.
     * @param arcDim the width and the hieght of the arc of this rounded rectangle.
     * @param insetWidth
     * @param insetHeight
     * @param fillColor the color to fill the object; if null, the object is not filled.
     * @param drawColor the color to draw the border.  If not set, Black color is used by default.
     * @param stroke the stroke to draw the shape of the widget.  If null, the current stroke is used.
     */
    public RoundedRectWidget(Scene scene,
                              String propId, String propDisplayName,
                              ArcDim arcDim, int insetWidth, int insetHeight,
                              Paint fillColor, Paint drawColor, Stroke stroke)
    {
        this(scene, propId, propDisplayName);
        this.arcDim = arcDim;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.fillColor = fillColor;
        this.drawColor = drawColor != null ? drawColor : Color.BLACK;
        this.stroke = stroke;
    }

    public Insets getInsets()
    {
        return new Insets(insetHeight, insetWidth, insetHeight, insetWidth);
    }

    public void setFillColor(Paint val)
    {
        this.fillColor = val;
    }

    public Paint getFillColor()
    {
        return this.fillColor;
    }

    public void setArc(int width, int height)
    {
        arcDim.setArchWidth(width);
        arcDim.setArchHeight(height);
    }

    public void setArc(ArcDim arcDimension)
    {
        arcDim = arcDimension;
    }

    public ArcDim getArc()
    {
        return arcDim;
    }

    public void setStroke(Stroke val)
    {
        this.stroke = val;
    }

    public Stroke getStroke()
    {
        return this.stroke;
    }

    @Override
    public void paintBackground()
    {
        Graphics2D graphics = getGraphics();
        Rectangle bounds = super.getBounds();
        assert bounds != null : "Error: bounds of RoundedRectangleWidget is null!";   // NOI18N
        Paint previousPaint = graphics.getPaint();

        Insets insets = this.getInsets();
        Paint bgColor = getBackground();
        
        if (isGradient())
        {
            Color bg = (Color) bgColor;
            bgColor = new GradientPaint(
                    0, 0, Color.WHITE,
                    0, bounds.height, bg);
        } 
        graphics.setPaint(bgColor);
        graphics.fill(new RoundRectangle2D.Float(
                      bounds.x + insets.left,
                      bounds.y + insets.top,
                      bounds.width - insets.left - insets.right,
                      bounds.height - insets.top - insets.bottom,
                      arcDim.getArchWidth(), arcDim.getArchHeight()));

        if (previousPaint != graphics.getPaint())
        {
            graphics.setPaint(previousPaint);
        }

    }

    @Override
    protected void paintWidget()
    {
        Graphics2D graphics = getGraphics();
        Rectangle bounds = super.getBounds();
        assert bounds != null : "Error: bounds of RoundedRectangleWidget is null!";   // NOI18N
        Stroke previousStroke = graphics.getStroke();
        Paint previousPaint = graphics.getPaint();

        graphics.setPaint(drawColor);
        if (stroke != null)
        {
            graphics.setStroke(stroke);
        }
        graphics.draw(new RoundRectangle2D.Float(
                      bounds.x + 0.5f, bounds.y + 0.5f,
                      bounds.width - 1, bounds.height - 1,
                      arcDim.getArchWidth(), arcDim.getArchHeight()));

        // reset to the previous paint and stroke
        if (previousStroke != graphics.getStroke())
        {
            graphics.setStroke(previousStroke);
        }

        if (previousPaint != graphics.getPaint())
        {
            graphics.setPaint(previousPaint);
        }
    }

    public class ArcDim
    {

        private int arcWidth = 20;
        private int arcHeight = 20;

        public ArcDim()
        {
        }

        public ArcDim(int width, int height)
        {
            arcWidth = width;
            arcHeight = height;
        }

        int getArchWidth()
        {
            return arcWidth;
        }

        void setArchWidth(int width)
        {
            arcWidth = width;
        }

        int getArchHeight()
        {
            return arcHeight;
        }

        void setArchHeight(int height)
        {
            arcHeight = height;
        }
    }
}

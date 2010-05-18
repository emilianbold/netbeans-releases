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

package org.netbeans.modules.uml.drawingarea.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.border.Border;

/**
 *
 * @author sp153251
 */
public class ResizeBorder implements Border {
    private static final BasicStroke STROKE = new BasicStroke (1.0f, BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[] { 6.0f, 3.0f }, 0.0f);

    private int thickness;
    private Color color;
    private boolean outer;
    private ArrayList<ResizeProvider.ControlPoint> points;

    
    /*
     * @param thickness the thickness of the border
     * @param color the border color
     * @param points points which will be filled
     * @param outer if true, then the rectangle encapsulate the squares too; if false, then the rectangle encapsulates the widget client area only
     */
    public ResizeBorder (int thickness, Color color,ResizeProvider.ControlPoint[] points, boolean outer) {
        this.thickness = thickness;
        this.color = color;
        this.outer = outer;
        this.points=new ArrayList<ResizeProvider.ControlPoint>();
        if(points!=null)for(int i=0;i<points.length;i++)this.points.add(points[i]);
    }
    /*
     * @param thickness the thickness of the border
     * @param color the border color
     * @param points points which will be filled
     */
    public ResizeBorder (int thickness, Color color,ResizeProvider.ControlPoint[] points) {
        this(thickness,color,points,false);
    }
    /*
     * @param thickness the thickness of the border
     * @param color the border color
     */
    public ResizeBorder (int thickness, Color color) {
        this(thickness,color,new ResizeProvider.ControlPoint[]{ResizeProvider.ControlPoint.TOP_LEFT,ResizeProvider.ControlPoint.TOP_CENTER,ResizeProvider.ControlPoint.TOP_RIGHT,ResizeProvider.ControlPoint.CENTER_LEFT,ResizeProvider.ControlPoint.BOTTOM_LEFT,ResizeProvider.ControlPoint.BOTTOM_CENTER,ResizeProvider.ControlPoint.BOTTOM_RIGHT,ResizeProvider.ControlPoint.CENTER_RIGHT});
    }
    /*
     * @param thickness the thickness of the border
     */
    public ResizeBorder (int thickness) {
        this(thickness,Color.BLACK);
    }

    public Insets getInsets () {
        return new Insets (thickness, thickness, thickness, thickness);
    }

    public boolean isOuter () {
        return outer;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);

        Stroke stroke = gr.getStroke ();
        gr.setStroke (STROKE);
        if (outer)
            gr.draw (new Rectangle2D.Double (bounds.x + 0.5, bounds.y + 0.5, bounds.width - 1.0, bounds.height - 1.0));
        else
            gr.draw (new Rectangle2D.Double (bounds.x + thickness + 0.5, bounds.y + thickness + 0.5, bounds.width - thickness - thickness - 1.0, bounds.height - thickness - thickness - 1.0));
        gr.setStroke (stroke);

        
        //TBD, may it have sense for perfomance to have 8 boolean vars
        if(points.contains(ResizeProvider.ControlPoint.TOP_LEFT))gr.fillRect (bounds.x, bounds.y, thickness, thickness);
        else gr.drawRect (bounds.x, bounds.y, thickness, thickness);
        if(points.contains(ResizeProvider.ControlPoint.TOP_RIGHT))gr.fillRect (bounds.x + bounds.width - thickness, bounds.y, thickness, thickness);
        else gr.drawRect (bounds.x + bounds.width - thickness, bounds.y, thickness, thickness);
        if(points.contains(ResizeProvider.ControlPoint.BOTTOM_LEFT))gr.fillRect (bounds.x, bounds.y + bounds.height - thickness, thickness, thickness);
        else gr.drawRect (bounds.x, bounds.y + bounds.height - thickness, thickness, thickness);
        if(points.contains(ResizeProvider.ControlPoint.BOTTOM_RIGHT))gr.fillRect (bounds.x + bounds.width - thickness, bounds.y + bounds.height - thickness, thickness, thickness);
        else gr.drawRect (bounds.x + bounds.width - thickness, bounds.y + bounds.height - thickness, thickness, thickness);

        Point center = new Point(bounds.x+bounds.width/2,bounds.y+bounds.height/2);
        if (bounds.width >= thickness * 5) {
            if(points.contains(ResizeProvider.ControlPoint.TOP_CENTER))gr.fillRect (center.x - thickness / 2, bounds.y, thickness, thickness);
            else gr.drawRect (center.x - thickness / 2, bounds.y, thickness, thickness);
            if(points.contains(ResizeProvider.ControlPoint.BOTTOM_CENTER))gr.fillRect (center.x - thickness / 2, bounds.y + bounds.height - thickness, thickness, thickness);
            else gr.drawRect (center.x - thickness / 2, bounds.y + bounds.height - thickness, thickness, thickness);
        }
        if (bounds.height >= thickness * 5) {
            if(points.contains(ResizeProvider.ControlPoint.CENTER_LEFT))gr.fillRect (bounds.x, center.y - thickness / 2, thickness, thickness);
            else gr.drawRect (bounds.x, center.y - thickness / 2, thickness, thickness);
            if(points.contains(ResizeProvider.ControlPoint.CENTER_RIGHT))gr.fillRect (bounds.x + bounds.width - thickness, center.y - thickness / 2, thickness, thickness);
            else gr.drawRect (bounds.x + bounds.width - thickness, center.y - thickness / 2, thickness, thickness);
        }
    }

    public boolean isOpaque () {
        return outer;
    }

}

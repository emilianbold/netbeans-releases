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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;

/**
 *
 * @author jsandusky
 */
public class GradientRectangle extends JGoRectangle {
    
    private static final JGoPen PEN_BORDER = 
            JGoPen.makeStockPen(new Color(127, 127, 127));
    
    // The gradient colors and boundaries are defined as follows:
    //
    //    .-----------------.    COLOR_5, 100% of span   (top of node)
    //    |                 |       |
    //    |                 |    COLOR_4,  95% of span
    //    |                 |       |
    //    |  title field &  |    COLOR_3,  70% of span
    //    |  input fields   |       |
    //    |                 |    COLOR_2,  23.6% of span
    //    |                 |       |
    //    |                 |    COLOR_1,   7.3% of span
    //    |                 |       |
    //    |_________________|    COLOR_0,  start of span (top of return field)
    //    |  return field   |
    //    |_________________|
    
    private static final Color COLOR_0 = Color.WHITE;
    private static final Color COLOR_1 = new Color(231, 241, 249);
    private static final Color COLOR_2 = new Color(184, 215, 255);
    private static final Color COLOR_3 = new Color(255, 255, 255);
    private static final Color COLOR_4 = new Color(221, 235, 246);
    private static final Color COLOR_5 = new Color(169, 197, 235);

    private Map mColorMap = new LinkedHashMap();
    private ICanvasGroupNode mGroupNode;
    
    
    public GradientRectangle(ICanvasGroupNode groupNode) {
        super();
        mGroupNode = groupNode;
        setPen(PEN_BORDER);
    }
    
    
    private void calculateColorMap() {
        mColorMap.clear();
        float spanLength = getSpanLength();
        float midX   = getLeft() + getWidth() / 2;
        int position = getTop()  + (int) spanLength;
        // From the bottom of the methoid to the top, we define a set of 
        // gradients that start from COLOR_0 and span to COLOR_5.
        // Thus, COLOR_0 is the color at the bottom, and COLOR_5 is at the top.
        position = addColorPoint(0.073f, position, COLOR_0, COLOR_1, spanLength, midX);
        position = addColorPoint(0.236f, position, COLOR_1, COLOR_2, spanLength, midX);
        position = addColorPoint(0.700f, position, COLOR_2, COLOR_3, spanLength, midX);
        position = addColorPoint(0.950f, position, COLOR_3, COLOR_4, spanLength, midX);
        position = addColorPoint(1.000f, position, COLOR_4, COLOR_5, spanLength, midX);
    }
    
    private float getSpanLength() {
        float spanLength = getHeight();
        
        if (mGroupNode.isExpanded()) {
            // If a result node exists, then start the span length from
            // the top of the result node.
            if (mGroupNode.getNodes().size() > 0) {
                ICanvasFieldNode resultNode = null;
                Collection nodes = mGroupNode.getNodes();
                for (Iterator iter=nodes.iterator(); iter.hasNext();) {
                    ICanvasFieldNode iterNode = (ICanvasFieldNode) iter.next();
                    if (iterNode.getFieldNode().isOutput()) {
                        resultNode = iterNode;
                        break;
                    }
                }
                if (resultNode != null && resultNode instanceof JGoArea) {
                    spanLength -= ((JGoArea) resultNode).getHeight();
                }
            }
        }
        
        return spanLength;
    }
    
    private int addColorPoint(
            float spanFactor, 
            int spanPosition, 
            Color one, 
            Color two, 
            float spanLength, 
            float midX)
    {
        // We work from the bottom of the methoid towards the top.
        // Each new span position that we calculate has a smaller y
        // coordinate than the previous. The first color is below
        // the second color.
        int newSpanPosition = getTop() + Math.round(spanLength - spanLength * spanFactor);
        Rectangle rect = new Rectangle(
                getLeft(), 
                newSpanPosition, 
                getWidth(), 
                (int) spanPosition - newSpanPosition);
        GradientPaint paint = new GradientPaint(
                midX, 
                newSpanPosition, 
                two, 
                midX, 
                spanPosition, 
                one);
        mColorMap.put(rect, paint);
        return newSpanPosition;
    }
    
    public void setBoundingRect(int left, int top, int width, int height) {
        super.setBoundingRect(left, top, width, height);
        calculateColorMap();
    }
    
    public void paint(Graphics2D graphics2d, JGoView jgoview) {
        Rectangle rectangle = getBoundingRect();
        drawGradientRect(
                graphics2d, 
                getPen(), 
                getBrush(), 
                rectangle.x, 
                rectangle.y, 
                rectangle.width, 
                rectangle.height);
    }
    
    public void drawGradientRect(
            Graphics2D graphics2d, 
            JGoPen jgopen, 
            JGoBrush jgobrush, 
            int x, 
            int y, 
            int w, 
            int h)
    {
        for (Iterator iter=mColorMap.keySet().iterator(); iter.hasNext();) {
            Rectangle rect = (Rectangle) iter.next();
            GradientPaint paint = (GradientPaint) mColorMap.get(rect);
            graphics2d.setPaint(paint);
            graphics2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
        
        if (jgopen != null) {
            java.awt.Stroke stroke = jgopen.getStroke();
            if (stroke != null) {
                graphics2d.setStroke(stroke);
                graphics2d.setColor(jgopen.getColor());
                graphics2d.drawRect(x, y, w, h);
            }
        }
    }
}

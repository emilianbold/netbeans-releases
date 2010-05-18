/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.nodes.state;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author Sheryl Su
 */
public class TabWidget extends CustomizableWidget
{
    private static final int arcHeight = 20;
    private static final int arcWidth = 20;

    public TabWidget(Scene scene, String id, String name)
    {
        super(scene, id, name);
        setOpaque(true);
        Border border = new NameTagBorder(Color.BLACK);
        setBorder(border);
    }

    protected void paintBackground()
    {
        Graphics2D gr = getGraphics();
        Rectangle bounds = getBounds();
        Paint bgColor = getBackground();

        if (UMLNodeWidget.useGradient())
        {
            Color bg = (Color) getBackground();
            int x1, y1;

            x1 = bounds.x;
            y1 = bounds.y;
            bgColor = new GradientPaint(x1, y1, Color.WHITE, x1, y1 + bounds.height, bg);
        }
        gr.setPaint(bgColor);

        gr.fillRect(bounds.x, bounds.y + arcHeight / 2, bounds.width, bounds.height);
        gr.fillRect(bounds.x + arcWidth / 2, bounds.y, bounds.width - arcWidth, arcHeight);
        gr.fillArc(bounds.x, bounds.y, arcWidth, arcHeight, 90, 90);
        gr.fillArc(bounds.x - arcWidth + bounds.width, bounds.y, arcWidth, arcHeight, 0, 90);


    }

    private static class NameTagBorder implements Border
    {

        private Paint drawColor;
        private int insetWidth = 5;
        private int insetHeight = 5;

        public NameTagBorder(Paint drawColor)
        {
            this.drawColor = drawColor;
        }

        public void paint(Graphics2D gr, Rectangle bounds)
        {
            Paint previousPaint = gr.getPaint();
            Stroke previousStroke = gr.getStroke();
            if (drawColor != null)
            {
                gr.setPaint(drawColor);

                gr.drawLine(bounds.x + arcWidth / 2, bounds.y, bounds.x + bounds.width - arcWidth / 2, bounds.y);
                gr.drawLine(bounds.x, bounds.y + arcHeight / 2, bounds.x, bounds.y + bounds.height + arcHeight / 2);
                gr.drawLine(bounds.x + bounds.width, bounds.y + arcHeight / 2, bounds.x + bounds.width, bounds.y + bounds.height);
                gr.drawArc(bounds.x, bounds.y, arcWidth, arcHeight, 90, 90);
                gr.drawArc(bounds.x - arcWidth + bounds.width, bounds.y, arcWidth, arcHeight, 0, 90);
            }

            // reset to the previous paint and stroke
            if (previousPaint != gr.getPaint())
            {
                gr.setPaint(previousPaint);
            }

            if (previousStroke != gr.getStroke())
            {
                gr.setStroke(previousStroke);
            }
        }

        public Insets getInsets()
        {
            return new Insets(insetWidth, insetHeight, insetWidth, insetHeight);
        }

        public boolean isOpaque()
        {
            return false;
        }
    }
}

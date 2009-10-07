/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * A JGoRectangle that supports handling of a special type of pen: InsetsPen Each side of
 * the rectangle can have a variable width border. Note: Ported from
 * com.sun.editor.basicmapper.canvas.jgo package.
 * 
 * @author Josh Sandusky
 */
public class InsetsRectangle extends JGoRectangle {

    public InsetsRectangle() {
        super();
    }

    public InsetsRectangle(Rectangle rectangle) {
        super(rectangle);
    }

    public InsetsRectangle(Point point, Dimension dimension) {
        super(point, dimension);
    }

    public void paint(Graphics2D graphics2d, JGoView jgoview) {
        Rectangle rectangle = getBoundingRect();
        drawInsetsRect(graphics2d, getPen(), getBrush(), rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public static void drawInsetsRect(Graphics2D graphics2d, JGoPen jgopen, JGoBrush jgobrush, int x, int y, int w, int h) {
        if (jgobrush != null) {
            java.awt.Paint paint = null;
            if (jgobrush instanceof GradientBrush) {
                GradientBrush grad = (GradientBrush) jgobrush;
                paint = grad.getPaint(x, y, w, h);
            } else {
                paint = jgobrush.getPaint();
            }
            if (paint != null) {
                graphics2d.setPaint(paint);
                graphics2d.fillRect(x, y, w, h);
            }
        }

        // custom handling of insets pen
        if (jgopen instanceof InsetsPen) {
            InsetsPen insetsPen = (InsetsPen) jgopen;
            java.awt.Stroke stroke = jgopen.getStroke();
            if (stroke != null) {
                // we're just drawing the border rectangle, however,
                // each side of the rectangle has a custom width that
                // is defined by the InsetsPen.
                graphics2d.setStroke(stroke);
                graphics2d.setColor(insetsPen.getColor());
                int t = insetsPen.getTop();
                int l = insetsPen.getLeft();
                int b = insetsPen.getBottom();
                int r = insetsPen.getRight();
                // top
                if (t > 0)
                    graphics2d.drawRect(x, y, w, t);
                // left
                if (l > 0)
                    graphics2d.drawRect(x, y, l, h);
                // bottom
                if (b > 0)
                    graphics2d.drawRect(x, h - b, w, b);
                // right
                if (r > 0)
                    graphics2d.drawRect(w - r, y, r, h);
                return;
            }
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


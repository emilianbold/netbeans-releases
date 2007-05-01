/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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


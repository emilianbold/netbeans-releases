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
package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Temporary widget which draws a arrow. Can be dashed, and either pointing east or west.
 * May be replaced by connection widget in future.
 * 
 * @author skini
 *
 */
public class ArrowWidget extends Widget {

    private int _width = -1;
    private int _height = 10;
    private int _x = 0;
    private int _y = 0;
    private int _thickness = 2;
    private Color _color = WidgetConstants.INPUT_OUTPUT_ARROW_COLOR;
    private boolean mToEast;
    private ParameterType _type;
    private Stroke _stroke;
    
    public enum ParameterType {

        INPUT, OUTPUT, FAULT
    }
  
    public ArrowWidget(Scene scene) {
        super(scene);
    }
    
    public ArrowWidget(Scene scene, boolean toEast, ParameterType type) {
        this(scene);
        this.mToEast = toEast;
        _type = type;
        if (_type == ParameterType.INPUT) {
            _stroke = new BasicStroke(_thickness);
        } else {
            _stroke = new BasicStroke(_thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1, new float[]{5, 10, 5,10}, 0);
        }
    }
    
    @Override
    protected Rectangle calculateClientArea() {
        Rectangle r = super.calculateClientArea();
        r.height = _height;
        return r;
    }

    @Override
    protected void paintWidget() {
        Graphics2D gr = getGraphics();
        drawArrow(gr, _x, _y, getClientArea().width, _height, _color, _stroke);
    }
    
    private void drawArrow(Graphics2D g, int x, int y , int width, int height, Color color, Stroke stroke) {
        double heightOfTriangle = Math.sqrt(height * height * 5 / 4);
        int heightOfTriangleInInt = new Double(heightOfTriangle).intValue();

        int x1 = x;
        int width1 = width - heightOfTriangleInInt;
        if (!mToEast) {
            x1 = x + heightOfTriangleInInt;
        }
        Color origColor = g.getColor();
        Stroke origStroke = g.getStroke();
        
        g.setColor(color);
        g.setStroke(stroke);
        int y1 = y + height / 2;
        g.drawLine(x1, y1, x1 + width1, y1);

        Polygon p = new Polygon();
        
        int px1 = mToEast ? x + width1 : x + heightOfTriangleInInt;
        int px2 = mToEast ? x + width : x;
        int py1 = y;
        int py2 = y + height;
        int pyMidPoint = y + height / 2;
        
        p.addPoint(px1, py1);
        p.addPoint(px2, pyMidPoint); //midpoint
        p.addPoint(px1, py2);
        g.fillPolygon(p);
        g.setColor(origColor);
        g.setStroke(origStroke);
    }




    public Color getColor() {
        return _color;
    }




    public void setColor(Color color) {
        this._color = color;
    }




    public int getHeight() {
        return _height;
    }




    public void setHeight(int height) {
        this._height = height;
    }




    public int getThickness() {
        return _thickness;
    }




    public void setThickness(int thickness) {
        this._thickness = thickness;
    }




    public int getWidth() {
        return _width;
    }




    public void setWidth(int width) {
        this._width = width;
    }




    public int getX() {
        return _x;
    }




    public void setX(int x) {
        this._x = x;
    }




    public int getY() {
        return _y;
    }




    public void setY(int y) {
        this._y = y;
    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    private int _thickness = 1;
    private Color _color = new Color(0x3244A0);
    private boolean mToEast;
    private ParameterType _type;
    private Stroke _stroke;
    
    public static enum ParameterType {

        INPUT, OUTPUT, FAULT
    }
  
    public ArrowWidget(Scene scene) {
        super(scene);
    }
    
    
    
    public ArrowWidget(Scene scene, boolean toEast, ParameterType type) {
        this(scene);
        this.mToEast = toEast;
        _type = type;
        if (_type == ParameterType.FAULT) {
            _stroke = new BasicStroke(_thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, new float[]{20, 15, 20,15}, 0);
        } else if (_type == ParameterType.OUTPUT) {
            _stroke = new BasicStroke(_thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1, new float[]{5, 10, 5,10}, 0);
        } else {
            _stroke = new BasicStroke(_thickness);
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
        drawArrow(gr, _x, _y, getBounds().width, _height, _color, _stroke);
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

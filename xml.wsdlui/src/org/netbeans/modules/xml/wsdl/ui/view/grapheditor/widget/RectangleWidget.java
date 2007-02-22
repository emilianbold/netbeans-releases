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
package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;

/**
 * 
 * @author skini
 *
 */
public class RectangleWidget extends Widget {

    private int _width = 10;
    private int _height = 67;
    private int _x = 0;
    private int _y = 0;
    private int _thickness = 1;
    private Color _color = new Color(0x3244A0);
    
    public RectangleWidget(Scene scene) {
        super(scene);
        setOpaque(true);
    }
    
    public RectangleWidget(Scene scene, int width, int height) {
        this(scene);
        _width = width;
        _height = height;
        refreshBorder();
    }
    
    
    public Color getColor() {
        return _color;
    }


    private void refreshBorder() {
        setBorder(new FilledBorder(new Insets(_thickness, _thickness, _thickness, _thickness), new Insets(0,0,0,0), _color, Color.WHITE));
        setMinimumSize(new Dimension(_width, _height));
    }

    public void setColor(Color color) {
        this._color = color;
        refreshBorder();
    }




    public int getHeight() {
        return _height;
    }




    public void setHeight(int height) {
        this._height = height;
        refreshBorder();
    }




    public int getThickness() {
        return _thickness;
    }




    public void setThickness(int thickness) {
        this._thickness = thickness;
        refreshBorder();
    }




    public int getWidth() {
        return _width;
    }




    public void setWidth(int width) {
        this._width = width;
        refreshBorder();
    }




    public int getX() {
        return _x;
    }




    public void setX(int x) {
        this._x = x;
        refreshBorder();
    }




    public int getY() {
        return _y;
    }




    public void setY(int y) {
        this._y = y;
        refreshBorder();
    }

}

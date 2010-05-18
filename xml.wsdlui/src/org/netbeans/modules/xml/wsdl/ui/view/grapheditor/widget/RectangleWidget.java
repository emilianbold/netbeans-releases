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

    private int _width;
    private int _height;
    private int _x = 0;
    private int _y = 0;
    private int _thickness = 2;
    private Color _color = new Color(0x3244A0);
    
    public RectangleWidget(Scene scene) {
        this(scene, 11, 67);
    }
    
    public RectangleWidget(Scene scene, int width, int height) {
        super(scene);
        setOpaque(false);
        _width = width;
        _height = height;
        refreshBorder();
    }
    
    
    public Color getColor() {
        return _color;
    }


    private void refreshBorder() {
        //setBorder(BorderFactory.createLineBorder(_thickness, _color));
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

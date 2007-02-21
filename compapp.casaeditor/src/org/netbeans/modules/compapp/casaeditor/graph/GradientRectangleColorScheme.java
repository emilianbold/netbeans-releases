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

/*
 * GradientRectangleColorScheme.java
 *
 * Created on February 11, 2007, 2:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;

/**
 * A set of color schemes for use with GradientRectangleDrawer.
 *
 * @author Josh Sandusky
 */
public class GradientRectangleColorScheme {
    
    private Color mBorderColor;
    private Color mColor1;
    private Color mColor2;
    private Color mColor3;
    private Color mColor4;
    private Color mColor5;
    
    
    public GradientRectangleColorScheme(
            Color borderColor, 
            Color c1, 
            Color c2, 
            Color c3, 
            Color c4, 
            Color c5)
    {
        mBorderColor = borderColor;
        mColor1 = c1;
        mColor2 = c2;
        mColor3 = c3;
        mColor4 = c4;
        mColor5 = c5;
    }
    
    //For solid color
    public GradientRectangleColorScheme(
            Color borderColor,
            Color color){
        mBorderColor = borderColor;
        mColor1 = color;
        mColor2 = color;
        mColor3 = color;
        mColor4 = color;
        mColor5 = color;
    }
            
    
    public Color getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(Color color) {
        mBorderColor = color;
    }
    
    public Color getColor1() {
        return mColor1;
    }
    
    public Color getColor2() {
        return mColor2;
    }
    
    public Color getColor3() {
        return mColor3;
    }
    
    public Color getColor4() {
        return mColor4;
    }
    
    public Color getColor5() {
        return mColor5;
    }
    
}

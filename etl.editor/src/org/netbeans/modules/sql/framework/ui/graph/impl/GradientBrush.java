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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

import com.nwoods.jgo.JGoBrush;

/**
 * A JGoBrush that paints with a color gradient from input color one to two.
 * 
 * @author Josh Sandusky
 */
public class GradientBrush extends JGoBrush {

    private Color mColorOne;
    private Color mColorTwo;

    public GradientBrush(Color one, Color two) {
        super();
        mColorOne = one;
        mColorTwo = two;
    }

    public Paint getPaint(int x, int y, int w, int h) {
        int fractX = w / 8;
        int midY = h / 2;
        return new GradientPaint(x + fractX, y + midY, mColorOne, x + (w - fractX), y + midY, mColorTwo);
    }

    public Color getColorOne() {
        return mColorOne;
    }

    public Color getColorTwo() {
        return mColorTwo;
    }
}


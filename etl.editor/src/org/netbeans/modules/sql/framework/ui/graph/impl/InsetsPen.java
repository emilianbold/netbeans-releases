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

import com.nwoods.jgo.JGoPen;

/**
 * A pen that stores additional inset information. Note: Ported from
 * com.sun.editor.basicmapper.canvas.jgo package.
 * 
 * @author Josh Sandusky
 */
public class InsetsPen extends JGoPen {

    private int mTop, mLeft, mBottom, mRight;

    public InsetsPen(int style, Color color, int top, int left, int bottom, int right) {
        // the pen thickness is the largest of each side, to allow proper drawing
        super(style, Math.max(Math.max(Math.max(top, left), bottom), right), color);
        mTop = top;
        mLeft = left;
        mBottom = bottom;
        mRight = right;
    }

    public int getTop() {
        return mTop;
    }

    public int getLeft() {
        return mLeft;
    }

    public int getBottom() {
        return mBottom;
    }

    public int getRight() {
        return mRight;
    }
}


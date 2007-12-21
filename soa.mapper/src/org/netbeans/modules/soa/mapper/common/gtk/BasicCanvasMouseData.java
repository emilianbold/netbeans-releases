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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Point;


/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public class BasicCanvasMouseData implements ICanvasMouseData {

    /**
     * Description of the Field
     */
    protected ICanvas mCanvas;
    /**
     * Description of the Field
     */
    protected Point mModelLocation;
    /**
     * Description of the Field
     */
    protected Point mViewLocation;
    /**
     * Description of the Field
     */
    protected int mModifier;

    /**
     * Constructor for the BasicCanvasMouseData object
     *
     * @param canvas    Description of the Parameter
     * @param modifier  Description of the Parameter
     * @param modelLoc  Description of the Parameter
     * @param viewLoc   Description of the Parameter
     */
    public BasicCanvasMouseData(ICanvas canvas, int modifier, Point modelLoc
                                , Point viewLoc) {
        mCanvas = canvas;
        mModelLocation = modelLoc;
        mViewLocation = viewLoc;
        mModifier = modifier;
    }

    /**
     * Gets the canvas attribute of the BasicCanvasMouseData object
     *
     * @return   The canvas value
     */
    public ICanvas getCanvas() {
        return mCanvas;
    }

    /**
     * Gets the modelLocation attribute of the BasicCanvasMouseData
     * object
     *
     * @return   The modelLocation value
     */
    public Point getModelLocation() {
        return mModelLocation;
    }

    /**
     * Gets the viewLocation attribute of the BasicCanvasMouseData
     * object
     *
     * @return   The viewLocation value
     */
    public Point getViewLocation() {
        return mViewLocation;
    }

    /**
     * Gets the mouseModifier attribute of the BasicCanvasMouseData
     * object
     *
     * @return   The mouseModifier value
     */
    public int getMouseModifier() {
        return mModifier;
    }
}

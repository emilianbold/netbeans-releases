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

public interface ICanvasMouseData {



    /**
     * Retrieve the canvas
     *
     * @return ICanvas
     */
    ICanvas getCanvas();


    /**
     * Retrieves the location of the mouse on the model coordinate
     * system
     *
     * @return Point
     */
    Point getModelLocation();

    /**
     * Retrieves the location of the mouse on the view coordinate system
     *
     * @return Point
     */
    Point getViewLocation();

    /**
     * Retrieves the mouse event modifier
     *
     * @return int
     */
    int getMouseModifier();
}

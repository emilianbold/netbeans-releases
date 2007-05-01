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

package org.netbeans.modules.sql.framework.ui.graph;

import java.awt.Rectangle;

/**
 * @author radval
 */
public interface ICanvasInterface {

    /**
     * get the maximum width
     * 
     * @return maximum width
     */
    int getMaximumWidth();

    /**
     * get the maximum height
     * 
     * @return maximum height
     */
    int getMaximumHeight();

    /**
     * get the minimum width
     * 
     * @return minimum width
     */
    int getMinimumWidth();

    /**
     * get the minimum height
     * 
     * @return minimum height
     */
    int getMinimumHeight();

    /**
     * set the bounding rectangle
     * 
     * @param x x
     * @param y y
     * @param width width
     * @param height height
     */
    void setBoundingRect(int x, int y, int width, int height);

    /**
     * set the bounding rectangle
     * 
     * @param rect bounding rectangle
     */
    void setBoundingRect(Rectangle rect);

    /**
     * set the size of the area
     * 
     * @param width width
     * @param height height
     */
    void setSize(int width, int height);

    /**
     * Make the area visible
     * 
     * @param visible visible
     */
    void setVisible(boolean visible);

    /**
     * layout the children
     */
    void layoutChildren();
}


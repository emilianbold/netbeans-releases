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

/**
 * @author radval
 */
public interface IGuiInfo {

    /**
     * set the x coordinate
     * 
     * @param x x corrdinate
     */
    public void setX(int x);

    /**
     * get x coordinate
     * 
     * @return x coordinate
     */
    public int getX();

    /**
     * set y coordinate
     * 
     * @param y y coordinate
     */
    public void setY(int y);

    /**
     * get y coordinate
     * 
     * @return y coordinate
     */
    public int getY();

    /**
     * set width
     * 
     * @param width width
     */
    public void setWidth(int width);

    /**
     * get width
     * 
     * @return width
     */
    public int getWidth();

    /**
     * set height
     * 
     * @param height height
     */
    public void setHeight(int height);

    /**
     * get height
     * 
     * @return height
     */
    public int getHeight();

    //    /**
    //     * set bounding rectangle
    //     * @param x x
    //     * @param y y
    //     * @param width width
    //     * @param height height
    //     */
    //    public void setBoundingRect(int x, int y, int width, int height);
    //
    //    /**
    //     * get bounding rectangle
    //     * @return bounding rectangle
    //     */
    //    public Rectangle getBoundingRect();
    //
    //    /**
    //     * set location
    //     * @param p location
    //     */
    //    public void setLocation(Point p);
    //
    //    /**
    //     * get locatation
    //     * @return location
    //     */
    //    public Point getLocation();
    //
}


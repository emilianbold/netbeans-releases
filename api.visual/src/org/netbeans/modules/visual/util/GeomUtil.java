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
package org.netbeans.modules.visual.util;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author David Kaspar
 */
public final class GeomUtil {

    private GeomUtil () {
    }

    /**
     * Rounds Rectangle2D to Rectangle.
     * @param rectangle the rectangle2D
     * @return the rectangle
     */
    public static Rectangle roundRectangle (Rectangle2D rectangle) {
        int x1 = (int) Math.floor (rectangle.getX ());
        int y1 = (int) Math.floor (rectangle.getY ());
        int x2 = (int) Math.ceil (rectangle.getMaxX ());
        int y2 = (int) Math.ceil (rectangle.getMaxY ());
        return new Rectangle (x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Returns a center point of a rectangle.
     * @param rectangle the rectangle
     * @return the center point
     */
    public static Point center (Rectangle rectangle) {
        return new Point (rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
    }

    /**
     * Returns a x-axis center of rectangle.
     * @param rectangle the rectangle
     * @return the x-axis center
     */
    public static int centerX (Rectangle rectangle) {
        return rectangle.x + rectangle.width / 2;
    }

    /**
     * Returns a y-axis center of rectangle.
     * @param rectangle the rectangle
     * @return the y-axis center
     */
    public static int centerY (Rectangle rectangle) {
        return rectangle.y + rectangle.height / 2;
    }

    /**
     * Returns a square distance of two points.
     * @param p1 the first point
     * @param p2 the second point
     * @return the square distance
     */
    public static double distanceSq (Point p1, Point p2) {
        int w = p2.x - p1.x;
        int h = p2.y - p1.y;
        return Math.sqrt (w * w + h * h);
    }

    /**
     * Returns whether two objects are equal
     * @param o1 the first object; cound be null
     * @param o2 the second object; cound be null
     * @return true, if they are equal
     */
    public static boolean equals (Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals (o2);
    }

}

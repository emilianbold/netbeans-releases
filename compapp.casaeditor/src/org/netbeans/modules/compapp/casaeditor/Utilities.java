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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.compapp.casaeditor;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author rdara
 */
public class Utilities {

     
    /**
     * Returns a center point of a rectangle.
     * @param rectangle the rectangle
     * @return the center point
     */
    public static Point center (Rectangle rectangle) {
        return new Point (rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
    }

    /**
     * Returns a center point of a rectangle.
     * @param thisRectangle - the rectangle itslef that needs to be centered 
     * @param withinTheRectangle - The boundary rectangle within which the rectangle need to be centered
     * @return the center point
     */
    public static Point center (Rectangle thisRectangle, Rectangle withinTheRectangle) {
       return new Point(withinTheRectangle.x + (withinTheRectangle.width - thisRectangle.width) / 2,
                        withinTheRectangle.y + (withinTheRectangle.height - thisRectangle.height) / 2);
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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

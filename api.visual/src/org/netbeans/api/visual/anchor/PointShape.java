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
package org.netbeans.api.visual.anchor;

import org.netbeans.modules.visual.anchor.SquarePointShape;

import java.awt.*;

/**
 * Represents a point shape. Usually used for control points and end points of a connection widget.
 * @author David Kaspar
 */
public interface PointShape {

    /**
     * Returns a radius of the shape.
     * @return the radius
     */
    public int getRadius ();

    /**
     * Renders a shape into the graphics instance
     * @param graphics
     */
    public void paint (Graphics2D graphics);

    /**
     * The empty point shape.
     */
    public static final PointShape NONE = new PointShape () {
        public int getRadius () { return 0; }
        public void paint (Graphics2D graphics) {}
    };

    /**
     * The 8px big filled-square shape.
     */
    public static final PointShape SQUARE_FILLED_BIG = new SquarePointShape (4, true);

    /**
     * The 6px big filled-square shape.
     */
    public static final PointShape SQUARE_FILLED_SMALL = new SquarePointShape (3, true);

}

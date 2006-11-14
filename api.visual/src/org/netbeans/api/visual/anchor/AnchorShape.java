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

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * Represents an anchor shape which is rendered at the source and the target point of a connection widget where the shape is used.
 * The same instance of a shape could be shared by multiple connection widgets.
 * @author David Kaspar
 */
public interface AnchorShape {

    /**
     * Returns whether the shape is oriented by the line path of a connection.
     * @return true if it is line-oriented
     */
    public boolean isLineOriented ();

    /**
     * Returns a radius of a shape that the shape used for rendering.
     * @return the radius
     */
    public int getRadius ();

    /**
     * Renders the shape into a graphics instance
     * @param graphics the graphics
     * @param source true, if the shape is used for a source point; false if the shape is used for a target point.
     */
    public void paint (Graphics2D graphics, boolean source);

    /**
     * The empty anchor shape.
     */
    public static final AnchorShape NONE = new AnchorShape() {
        public boolean isLineOriented () { return false; }
        public int getRadius () { return 0; }
        public void paint (Graphics2D graphics, boolean source) { }
    };

    /**
     * The filled-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_FILLED = new Triangle (12, true, false);

    /**
     * The output-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_OUT = new Triangle (12, true, true);

    /**
     * Represents triangular anchor shape.
     */
    public static final class Triangle implements AnchorShape {

        private int size;
        private boolean filled;
        private boolean output;

        /**
         * Creates a triangular anchor shape.
         * @param size the size of triangle
         * @param filled if true, then the triangle is filled
         * @param output if true, then it is output triangle
         */
        public Triangle (int size, boolean filled, boolean output) {
            this.size = size;
            this.filled = filled;
            this.output = output;
        }

        public boolean isLineOriented () {
            return true;
        }

        public int getRadius () {
            return (int) Math.ceil(1.5f * size);
        }

        public void paint (Graphics2D graphics, boolean source) {
            GeneralPath generalPath = new GeneralPath ();
            float side = size * 0.3f;
            if (output) {
                generalPath.moveTo (size, 0.0f);
                generalPath.lineTo (0.0f, -side);
                generalPath.lineTo (0.0f, +side);
            } else {
                generalPath.moveTo (0.0f, 0.0f);
                generalPath.lineTo (size, -side);
                generalPath.lineTo (size, +side);
            }
            if (filled)
                graphics.fill (generalPath);
            else
                graphics.draw (generalPath);
        }

    }

}

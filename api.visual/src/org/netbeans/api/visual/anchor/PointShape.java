/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.anchor;

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
    public static final PointShape SQUARE_FILLED_BIG = new Square (4, true);

    /**
     * The 6px big filled-square shape.
     */
    public static final PointShape SQUARE_FILLED_SMALL = new Square (3, true);

    /**
     * Represents a square point shape.
     */
    public static final class Square implements PointShape {

        private int size;
        private boolean filled;

        /**
         * Creates a square shape.
         * @param size the size
         * @param filled if true, then the shape is filled
         */
        public Square (int size, boolean filled) {
            this.size = size;
            this.filled = filled;
        }

        public int getRadius () {
            return (int) Math.ceil (1.5f * size);
        }

        public void paint (Graphics2D graphics) {
            int size2 = size + size;
            Rectangle rect = new Rectangle (- size, - size, size2, size2);
            if (filled)
                graphics.fill (rect);
            else
                graphics.draw (rect);
        }
    }

}

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
 * @author David Kaspar
 */
public interface PointShape {

    public int getRadius ();

    public void paint (Graphics2D graphics);

    public static final PointShape NONE = new PointShape () {
        public int getRadius () { return 0; }
        public void paint (Graphics2D graphics) {}
    };

    public static final PointShape SQUARE_FILLED_BIG = new Square (4, true);
    public static final PointShape SQUARE_FILLED_SMALL = new Square (3, true);

    public static final class Square implements PointShape {

        private int size;
        private boolean filled;

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

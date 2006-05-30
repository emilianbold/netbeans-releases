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
import java.awt.geom.GeneralPath;

/**
 * @author David Kaspar
 */
public interface AnchorShape {

    public boolean isLineOriented ();

    public int getRadius ();

    public void paint (Graphics2D graphics, boolean source);

    public static final AnchorShape NONE = new AnchorShape() {
        public boolean isLineOriented () { return false; }
        public int getRadius () { return 0; }
        public void paint (Graphics2D graphics, boolean source) { }
    };


    public static final AnchorShape TRIANGLE_FILLED = new Triangle (8, true, false);
    public static final AnchorShape TRIANGLE_OUT = new Triangle (8, true, true);

    public static final class Triangle implements AnchorShape {

        private int size;
        private boolean filled;
        private boolean output;

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
            float side = size * 0.6f;
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

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
package org.netbeans.modules.visualweb.extension.openide.util;

import java.awt.*;

/**
 * Extends Trace class to add paint debugging fuctionality.  This is a separate
 * class so that a headless module that does not import AWT can use the Trace
 * functionality without linking in AWT.
 *
 * @author Joe Nuxoll
 */
public class TraceUI extends Trace {

    /**
     * Paints a rectangle within the specified bounds - with hashmarks. As subsequent
     * (often quickly repetitive) calls are made to this method, the hash direction
     * alternates from top-right/bottom-left to top-left/bottom-right and cycles colors
     * and hashmark spacing so that it is easy to distinguish between the different calls,
     *
     * @param g Graphics object to paint into
     * @param x Rectangle.X
     * @param y Rectangle.y
     * @param width Rectangle.width
     * @param height Rectangle.height
     */
    public static void debugRect(Graphics g, int x, int y, int width, int height) {
        if (g == null)
            return;
        Rectangle clip = g.getClipBounds();
        if (clip == null)
            return;
        g.setClip(x, y, width, height);
        Color c = g.getColor();
        if (colorWheel == null)
            colorWheel = new TraceUI.ColorWheel();
        g.setColor(colorWheel.next());
        g.drawRect(x, y, width - 1, height - 1);
        g.drawRect(x + 1, y + 1, width - 3, height - 3);
        // alternate hash directions
        if (debugRectHashLeft) {
            for (int i = 0; i < x + width + height; i += debugRectInc)
                g.drawLine(x, y + i, x + i, y);
        }
        else {
            for (int i = 0; i < x + width + height; i += debugRectInc)
                g.drawLine(x + width, y + i, x + width - i, y);
        }
        debugRectHashLeft = !debugRectHashLeft;
        if (debugRectInc > 15)
            debugRectInc = 5;
        else
            debugRectInc += 2;
        g.setColor(c);
        if (clip != null)
            g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    private static TraceUI.ColorWheel colorWheel;
    private static int debugRectInc = 10;
    private static boolean debugRectHashLeft = true;

    //------------------------------------------------------------------------------
    // ColorWheel class - for cycling colors on debugRect calls
    //------------------------------------------------------------------------------
    private static class ColorWheel {
        public static final int RED_TO_YELLOW = 1; // 0xFF0000 --> 0xFFFF00 (adding green)
        public static final int YELLOW_TO_GREEN = 2; // 0xFFFF00 --> 0x00FF00 (subtracting red)
        public static final int GREEN_TO_CYAN = 3; // 0x00FF00 --> 0x00FFFF (adding blue)
        public static final int CYAN_TO_BLUE = 4; // 0x00FFFF --> 0x0000FF (subtracting green)
        public static final int BLUE_TO_MAGENTA = 5; // 0x0000FF --> 0xFF00FF (adding red)
        public static final int MAGENTA_TO_RED = 6; // 0xFF00FF --> 0xFF0000 (subtracting blue)

        public ColorWheel() {}

        public ColorWheel(Color startColor) {
            current = startColor;
        }

        public ColorWheel(Color startColor, int startCycle) {
            current = startColor;
            cycle = startCycle;
        }

        public ColorWheel(Color startColor, int startCycle, int increment) {
            current = startColor;
            cycle = startCycle;
            this.increment = increment;
        }

        private int increment = 50;
        private int cycle = RED_TO_YELLOW;
        private Color current = Color.red;

        public int getIncrement() {
            return increment;
        }

        public void setIncrement(int newIncrement) {
            if (newIncrement >= 1 && newIncrement <= 255)
                increment = newIncrement;
            else
                throw new IllegalArgumentException("ColorWheel increment must be in range (1-255)");
        }

        public int getCycle() {
            return cycle;
        }

        public void setCycle(int newCycle) {
            if (newCycle >= RED_TO_YELLOW && newCycle <= MAGENTA_TO_RED)
                cycle = newCycle;
            else
                throw new IllegalArgumentException("Invalid ColorWheel cycle: " + newCycle);
        }

        public Color getColor() {
            return current;
        }

        public void setColor(Color newColor) {
            if (newColor != null)
                current = newColor;
        }

        public Color next() {
            return next(current);
        }

        public Color next(Color color) {
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            switch (cycle) {
                case RED_TO_YELLOW:
                    g += increment;
                    if (g > 255) {
                        g = 255;
                        cycle = YELLOW_TO_GREEN;
                    }
                    break;
                case YELLOW_TO_GREEN:
                    r -= increment;
                    if (r < 0) {
                        r = 0;
                        cycle = GREEN_TO_CYAN;
                    }
                    break;
                case GREEN_TO_CYAN:
                    b += increment;
                    if (b > 255) {
                        b = 255;
                        cycle = CYAN_TO_BLUE;
                    }
                    break;
                case CYAN_TO_BLUE:
                    g -= increment;
                    if (g < 0) {
                        g = 0;
                        cycle = BLUE_TO_MAGENTA;
                    }
                    break;
                case BLUE_TO_MAGENTA:
                    r += increment;
                    if (r > 255) {
                        r = 255;
                        cycle = MAGENTA_TO_RED;
                    }
                    break;
                case MAGENTA_TO_RED:
                    b -= increment;
                    if (b < 0) {
                        b = 0;
                        cycle = RED_TO_YELLOW;
                    }
                    break;
                default:
                    cycle = RED_TO_YELLOW;
                    break;
            }
            current = new Color(r, g, b);
            return current;
        }
    }
}

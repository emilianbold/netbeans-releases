/*
 *
 * Copyright (c) 2007, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.chooser;

import javax.microedition.lcdui.*;


/**
 * A Color chooser.  This screen can be used to display and
 * choose colors.  The current color is always available
 * via the getColor methods.  It can be  set with setColor.
 */
public class MiniColorChooser extends Canvas {
    /** Border width */
    static final int BORDER = 2;

    /** Color bar height */
    static final int BAR_H = 14;

    /** current color */
    int rgbColor;

    /** current index */
    int ndx = 0;

    /**
     * Create a new MiniColorChooser for a single color.
     */
    public MiniColorChooser() {
        setColor(0xffff00);
    }

    /**
     * Sets the current color to the specified RGB values. All subsequent
     * rendering operations will use this specified color. The RGB value
     * passed in is interpreted with the least significant eight bits
     * giving the blue component, the next eight more significant bits
     * giving the green component, and the next eight more significant
     * bits giving the red component. That is to say, the color component
     * is specified like 0x00RRGGBB.
     * @param RGB The color being set.
     */
    public void setColor(int RGB) {
        rgbColor = RGB & 0x00ffffff;
    }

    /**
     * Gets the current color.
     * @return an integer in form 0x00RRGGBB
     * @see #setColor
     */
    public int getColor() {
        return rgbColor;
    }

    /**
     * Paint the canvas with the current color and controls to change it.
     * @param g the graphics context to draw to the screen.
     */
    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        int sample_w = w - 1;
        int sample_h = h - ((BAR_H + BORDER) * 3);
        int sample_y = BORDER;

        int b_y = sample_y + sample_h + (BORDER * 2);
        int g_y = b_y + BAR_H;
        int r_y = g_y + BAR_H;

        // Fill the background
        g.setColor(0x000000);
        g.fillRect(0, 0, w, h);

        // Fill in the color sample
        g.setColor(rgbColor);
        g.fillRect(BORDER, sample_y, sample_w, sample_h);

        // Draw the color bars
        int blue = (rgbColor >> 0) & 0xff;
        g.setColor(0, 0, 255);
        g.fillRect(20, b_y, blue / 4, 10);

        int green = (rgbColor >> 8) & 0xff;
        g.setColor(0, 255, 0);
        g.fillRect(20, g_y, green / 4, 10);

        int red = (rgbColor >> 16) & 0xff;
        g.setColor(255, 0, 0);
        g.fillRect(20, r_y, red / 4, 10);

        g.setColor(255, 255, 255);
        g.drawString(Integer.toString(blue), 18, b_y - 3, Graphics.RIGHT | Graphics.TOP);
        g.drawString(Integer.toString(green), 18, g_y - 3, Graphics.RIGHT | Graphics.TOP);
        g.drawString(Integer.toString(red), 18, r_y - 3, Graphics.RIGHT | Graphics.TOP);

        if (ndx >= 0) {
            int y = b_y + (ndx * BAR_H);
            g.drawRect(20, y, 63, 10);
        }
    }

    /**
     * Handle repeat as in pressed.
     * @param key was pressed
     */
    public void keyRepeated(int key) {
        keyPressed(key);
    }

    /**
     * Left and Right are used to change which color bar to change
     * Up and Down are used to increase/decrease the value of that bar.
     * @param key was pressed
     */
    protected void keyPressed(int key) {
        int action = getGameAction(key);
        int dir = 0;

        switch (action) {
        case RIGHT:
            dir += 1;

            break;

        case LEFT:
            dir -= 1;

            break;

        case UP:
            ndx -= 1;

            break;

        case DOWN:
            ndx += 1;

            break;

        default:
            return; // nothing we recognize, exit
        }

        // Limit selection to r,g,b and palette
        if (ndx < 0) {
            ndx = 0;
        }

        if (ndx > 2) {
            ndx = 2;
        }

        if (ndx >= 0) {
            int v = (rgbColor >> (ndx * 8)) & 0xff;
            v += (dir * 0x20);

            if (v < 0) {
                v = 0;
            }

            if (v > 255) {
                v = 255;
            }

            int mask = 0xff << (ndx * 8);
            rgbColor = (rgbColor & ~mask) | (v << (ndx * 8));
        }

        repaint();
    }
}

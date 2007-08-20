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
 * A Text sample.  This screen can be used to display a Sample string.
 * using the Face, Style, Size and foreground and background color.
 */
public class TextSample extends Canvas {
    /** The width of the border. */
    static final int border = 2;
    int face;
    int style;
    int size;
    int bgcolor;
    int fgcolor;

    /** The sample text */
    String text = "Sample Text";

    /**
     * Create a new TextSample canvas.
     */
    public TextSample() {
        fgcolor = 0xffffff;
        bgcolor = 0x000000;
        size = Font.SIZE_MEDIUM;
        face = Font.FACE_SYSTEM;
        style = Font.STYLE_PLAIN;
    }

    /**
     * Set the Style of font to display.
     * @param style to which to set the font
     * @see Font#getStyle
     */
    public void setStyle(int style) {
        this.style = style;
    }

    /**
     * Get the style of font currently being displayed.
     * @return the font style
     * @see Font#getStyle
     */
    public int getStyle() {
        return style;
    }

    /**
     * Set the Face of font to display.
     * @param face to which to set the font
     * @see Font#getFace
     */
    public void setFace(int face) {
        this.face = face;
    }

    /**
     * Get the face of font currently being displayed.
     * @return the current font face
     * @see Font#getFace
     */
    public int getFace() {
        return face;
    }

    /**
     * Set the Size of font to display.
     * @param size to which the font should be set
     * @see Font#getSize
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Get the size of font currently being displayed.
     * @return The current size of font.
     * @see Font#getSize
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the Color of font to display.
     * @param color a new foreground color
     */
    public void setForegroundColor(int color) {
        fgcolor = color;
    }

    /**
     * Get the color of font currently being displayed.
     * @return the foreground color
     */
    public int getForegroundColor() {
        return fgcolor;
    }

    /**
     * Set the Color of font to display.
     * @param color a new background color
     */
    public void setBackgroundColor(int color) {
        bgcolor = color;
    }

    /**
     * Get the color of font currently being displayed.
     * @return the background color
     */
    public int getBackgroundColor() {
        return bgcolor;
    }

    /**
     * Paint the canvas with the current color and controls to change it.
     * @param g the graphics context to which to paint
     */
    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        // Fill the background
        g.setColor(bgcolor);
        g.fillRect(0, 0, w, h);

        g.setColor(fgcolor);

        Font font = Font.getFont(face, style, size);
        g.setFont(font);
        g.drawString(text, 2, border, Graphics.LEFT | Graphics.TOP);
    }
}

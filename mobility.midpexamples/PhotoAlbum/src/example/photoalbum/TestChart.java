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
package example.photoalbum;

import java.util.Vector;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


/**
 * A quick sample of graphics which generates a series of images
 * that can be used as a sample animation.  The animation
 * consists of a title, a pie, and a bar chart.  The charts
 * are sized to the requested size so they will look good on
 * devices with various screen sizes.
 */
class TestChart {
    /** Width of the canvas */
    int w;

    /** Height of the canvas */
    int h;

    /** Font used for drawing text */
    Font font;

    /** height of the font */
    int fh;

    /** Height of the title */
    int titleHeight;

    /** Padding used between items */
    int pad;

    /** Size of the Pie chart used for width and height */
    int pieSize;

    /** Size of the Bar chart used for width and height */
    int barSize;

    /** The current frame number */
    int frameno;

    /**
     * Initialize a new TestPattern to match the requested size.
     * @param width the requested width of the Images
     * @param height the requested height of the Images
     */
    public TestChart(int width, int height) {
        w = width;
        h = height;
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fh = font.getHeight();

        /*
         * Compute the sizes of the bar and pie charts
         * It should use all the space except for the title and event regions
         * Don't let the charts get too small
         */
        pad = 2;
        titleHeight = fh + (pad * 2);
        barSize = h - (titleHeight + pad);

        if (barSize < 10) { // Don't let them get too small
            barSize = 10;
        }

        if (barSize > ((w - pad) / 2)) { // Shrink to 1/2 width
            barSize = (w - pad) / 2;
        }

        pieSize = barSize;
    }

    /**
     * Generate the frames for this animated chart.
     * It consists of hour frames with the first frame
     * containing only the base, and frame 2,3,4 adding
     * the colors to the bars of the bar chart.
     * @return the Vector of Images.
     */
    public Vector generateImages() {
        Vector v = new Vector(4);

        for (frameno = 0; frameno < 4; frameno++) {
            Image image = Image.createImage(w, h);
            paint(image.getGraphics());
            v.addElement(image);
        }

        return v;
    }

    /**
     * Draw the current frame.
     * The field frameno contains the current frame number.
     * @param g the Graphics context
     */
    public void paint(Graphics g) {
        g.setFont(font);
        g.setGrayScale(255);
        g.fillRect(0, 0, w, h);

        // Draw Fill and outline for background of title Text
        int swidth = (pad * 2) + font.stringWidth("Chart Samples");
        int title_x = (w - swidth) / 2;

        g.setGrayScale(128);
        g.fillRoundRect(title_x, 0, swidth, fh, 5, 5);
        g.setGrayScale(0);
        g.drawRoundRect(title_x, 0, swidth, fh, 5, 5);

        // Sample Text
        g.setColor(0, 0, 0);
        g.drawString("Chart Samples", title_x + pad, pad, Graphics.TOP | Graphics.LEFT);

        g.translate(0, titleHeight + pad); // Translate to below title text

        // Draw pie chart on the left using the barSize for width and height
        g.setColor(255, 0, 0);
        g.fillArc(0, 0, pieSize, pieSize, 45, 270);
        g.setColor(0, 255, 0);
        g.fillArc(0, 0, pieSize, pieSize, 0, 45);
        g.setColor(0, 0, 255);
        g.fillArc(0, 0, pieSize, pieSize, 0, -45);
        g.setColor(0);
        g.drawArc(0, 0, pieSize, pieSize, 0, 360);

        // Draw Bar chart on right side of the display
        // scale the values to the pieSize maximum value
        int yorig = barSize;
        int h1 = barSize / 3;
        int h2 = barSize / 2;
        int h3 = barSize;
        int avg = (h1 + h2 + h3) / 3;

        // Move over to draw Bar chart
        g.translate((w + pad) / 2, 0);

        int bw = pieSize / 7;

        if (bw < 2) {
            bw = 2;
        }

        if (frameno > 0) {
            g.setColor(255, 0, 0);
            g.fillRect(bw * 1, yorig - h1, bw + 1, h1);
        }

        if (frameno > 1) {
            g.setColor(0, 255, 0);
            g.fillRect(bw * 3, yorig - h2, bw + 1, h2);
        }

        if (frameno > 2) {
            g.setColor(0, 0, 255);
            g.fillRect(bw * 5, yorig - h3, bw + 1, h3);
        }

        g.setColor(0);
        g.drawRect(bw * 1, yorig - h1, bw, h1);
        g.drawRect(bw * 3, yorig - h2, bw, h2);
        g.drawRect(bw * 5, yorig - h3, bw, h3);

        // Draw axis for bar chart.
        g.setGrayScale(0);
        g.drawLine(0, 0, 0, yorig);
        g.drawLine(0, yorig, barSize, yorig);
        g.setStrokeStyle(Graphics.DOTTED);
        g.drawLine(0, yorig - avg, barSize, yorig - avg);
        g.setStrokeStyle(Graphics.SOLID);

        // Restore to left and move down
        g.translate(-(w + pad) / 2, pieSize + pad);
    }
}

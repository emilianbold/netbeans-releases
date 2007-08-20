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


/**
 * This PhotoFrame provides the picture frame and drives the animation
 * of the frames and the picture. It handles the starting and stopping
 * of the Animation and contains the Thread used to do
 * the timing and requests that the Canvas be repainted
 * periodically.
 * It controls the border style and animation speed.
 */
class PhotoFrame extends Canvas implements Runnable {
    /**
     * Mapping of speed values to delays in ms.
     * Indices map to those in the speed ChoiceGroup in the options form.
     * The indices are: 0 = stop, 1 = slow, 2 = medium, 3 = fast.
     * @see setSpeed
     */
    private static final int[] speeds = { 999999999, 500, 250, 100, 0 };

    /** border style */
    private int style;

    /** animation speed set */
    private int speed;

    /** Vector of images to display */
    private Vector images;

    /** Index of next image to display */
    private int index;

    /** X offset of image in frame */
    private int imageX;

    /** X offset of image in frame */
    private int imageY;

    /** Width and height of image */
    private int imageWidth;

    /** Width and height of image */
    private int imageHeight;

    /** Thread used for triggering repaints */
    private Thread thread;

    /** buffer image of the screen */
    private Image image;

    /** Pattern image used for border */
    private Image bimage;

    /** Time of most recent paint */
    private long paintTime;

    /** Time of most recent frame rate report */
    private long statsTime;

    /** Number of frames since last frame rate report */
    int frameCount;

    /** Last reported frame rate (for re-paint) */
    int frameRate;

    /**
     * Create a new PhotoFrame.
     * Create an offscreen mutable image into which the border is drawn.
     * Border style is none (0).
     * Speed is stopped (0) until set.
     */
    PhotoFrame() {
        image = Image.createImage(getWidth(), getHeight());
        setStyle(0);
        setSpeed(0);
    }

    /**
     * Set the array of images to be displayed.
     * Update the width and height of the image and draw
     * the border to fit around it in the offscreen image.
     * @param images a vector of images to be displayed.
     */
    void setImages(Vector images) {
        this.images = images;

        if (images.size() > 0) {
            Image image = (Image)images.elementAt(0);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
        } else {
            imageWidth = 0;
            imageHeight = 0;
        }

        index = 0;
        imageX = (getWidth() - imageWidth) / 2;
        imageY = (getHeight() - imageHeight) / 2;
        genFrame(style, imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Advance to the next image and wrap around if necessary.
     */
    void next() {
        if ((images == null) || (++index >= images.size())) {
            index = 0;
        }
    }

    /**
     * Back up to the previous image.
     * Wrap around to the end if at the beginning.
     */
    void previous() {
        if ((images != null) && (--index < 0)) {
            index = images.size() - 1;
        } else {
            index = 0;
        }
    }

    /**
     * Reset the PhotoFrame so it holds minimal resources.
     * The animation thread is stopped.
     */
    void reset() {
        images = null;
        thread = null;
    }

    /**
     * Handle key events. FIRE events toggle between
     * running and stopped.  LEFT and RIGHT key events
     * when stopped show the previous or next image.
     * @param keyCode of the key pressed
     */
    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        switch (action) {
        case RIGHT:

            if (thread == null) {
                next();
                repaint();
            }

            break;

        case LEFT:

            if (thread == null) {
                previous();
                repaint();
            }

            break;

        case FIRE:

            // Use FIRE to toggle the activity of the thread
            if (thread == null) {
                thread = new Thread(this);
                thread.start();
            } else {
                synchronized (this) {
                    // Wake up the thread to change the timing
                    this.notify();
                }

                // Shouldn't be in synchronized block
                thread = null;
            }

            break;
        }
    }

    /**
     * Handle key repeat events as regular key events.
     * @param keyCode of the key repeated
     */
    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

    /**
     * Set the animation speed.
     * Speed:
     * <OL>
     * <LI>0 = stop
     * <LI>1 = slow
     * <LI>2 = medium
     * <LI>3 = fast
     * <LI>4 = unlimited
     * </OL>
     * @param speed speedo of animation 0-3;
     */
    void setSpeed(int speed) {
        this.speed = speed;
        statsTime = 0;
    }

    /**
     * Get the speed at which animation occurs.
     * @return the current speed.
     * @see setSpeed
     */
    int getSpeed() {
        return speed;
    }

    /**
     * Set the frame style.
     * Recreate the photo frame image from the current style
     * and location and size
     * <p>
     * Style:
     * <OL>
     * <LI> Style 0: No border is drawn.
     * <LI> Style 1: A simple border is drawn
     * <LI> Style 2: The border is outlined and an image
     * is created to tile within the border.
     * </OL>
     * @param style the style of the border; 0 = none, 1 = simple,
     * 2 = fancy.
     */
    void setStyle(int style) {
        this.style = style;
        genFrame(style, imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Get the style being used for borders.
     * @return the style.
     */
    int getStyle() {
        return style;
    }

    /**
     * Notified when Canvas is made visible.
     * If there is more than one image to display
     * create the thread to run the animation timing.
     */
    protected void showNotify() {
        if ((images != null) && (images.size() > 1)) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Notified when the Canvas is no longer visible.
     * Signal the running Thread that it should stop.
     */
    protected void hideNotify() {
        thread = null;
    }

    /**
     * Return true if the specified rectangle does not intersect
     * the clipping rectangle of the graphics object.  If it returns
     * true then the object must be drawn otherwise it would be clipped
     * completely.
     * The checks are done
     * @param g the Graphics context to check.
     * @param x the x value of the upper left corner of the rectangle
     * @param y the y value of the upper left corner of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @return true if the rectangle intersects the clipping region
     */
    boolean intersectsClip(Graphics g, int x, int y, int w, int h) {
        int cx = g.getClipX();

        if ((x + w) <= cx) {
            return false;
        }

        int cw = g.getClipWidth();

        if (x > (cx + cw)) {
            return false;
        }

        int cy = g.getClipY();

        if ((y + h) <= cy) {
            return false;
        }

        int ch = g.getClipHeight();

        if (y > (cy + ch)) {
            return false;
        }

        return true;
    }

    /**
     * Runs the animation and makes the repaint requests.
     * The thread will exit when it is no longer the current
     * Animation thread.
     */
    public void run() {
        Thread mythread = Thread.currentThread();
        long scheduled = System.currentTimeMillis();
        statsTime = scheduled;
        paintTime = scheduled;
        frameCount = 0;
        frameRate = 0;

        /*
         * The following code was changed to fix bug 4918599.
         * The bug is caused by a deadlock caused by
         * bad-designed use of synchronized blocks in demo.
         */
        while (thread == mythread) {
            // Update when the next frame should be drawn
            // and compute the delta till then
            scheduled += speeds[speed];

            long delta = scheduled - paintTime;

            if (delta > 0) {
                synchronized (this) {
                    try {
                        this.wait(delta);
                    } catch (InterruptedException e) {
                    }
                }
            }

            // Advance and repaint the screen
            next();
            repaint();
            serviceRepaints();
        }
    }

    /**
     * Paint is called whenever the canvas should be redrawn.
     * It clears the canvas and draws the frame and the current
     * current frame from the animation.
     * @param g the Graphics context to which to draw
     */
    protected void paint(Graphics g) {
        paintTime = System.currentTimeMillis();

        if (image != null) {
            // Draw the frame unless only the picture is being re-drawn
            // This is the inverse of the usual clip check.
            int cx = 0;

            // Draw the frame unless only the picture is being re-drawn
            // This is the inverse of the usual clip check.
            int cy = 0;

            // Draw the frame unless only the picture is being re-drawn
            // This is the inverse of the usual clip check.
            int cw = 0;

            // Draw the frame unless only the picture is being re-drawn
            // This is the inverse of the usual clip check.
            int ch = 0;

            if (((cx = g.getClipX()) < imageX) || ((cy = g.getClipY()) < imageY) ||
                    ((cx + (cw = g.getClipWidth())) > (imageX + imageWidth)) ||
                    ((cy + (ch = g.getClipHeight())) > (imageY + imageHeight))) {
                g.drawImage(image, 0, 0, Graphics.LEFT | Graphics.TOP);

                if (frameRate > 0) {
                    g.fillRect(0, getHeight(), 60, 20);
                    g.drawString("FPS = " + frameRate, 0, getHeight(),
                        Graphics.BOTTOM | Graphics.LEFT);
                }
            }

            // Draw the image if it intersects the clipping region
            if ((images != null) && (index < images.size()) &&
                    intersectsClip(g, imageX, imageY, imageWidth, imageHeight)) {
                g.drawImage((Image)images.elementAt(index), imageX, imageY,
                    Graphics.LEFT | Graphics.TOP);
            }

            frameCount++;

            // Update Frame rate
            int delta = (int)(paintTime - statsTime);

            if ((delta > 1000) && (delta < 10000)) {
                frameRate = (((frameCount * 1000) + 500) / delta);
                frameCount = 0;
                statsTime = paintTime;
                repaint(); // queue full repaint to display frame rate
            }
        }
    }

    /**
     * Paint the photo frame into the buffered screen image.
     * This will avoid drawing each of its parts on each repaint.
     * Paint will only need to put the image into the frame.
     * @param style the style of frame to draw.
     * @param x the x offset of the image.
     * @param y the y offset of the image
     * @param width the width of the animation image
     * @param height the height of the animation image
     */
    private void genFrame(int style, int x, int y, int width, int height) {
        Graphics g = image.getGraphics();

        // Clear the entire image to white
        g.setColor(0xffffff);
        g.fillRect(0, 0, image.getWidth() + 1, image.getHeight() + 1);

        // Set the origin of the image and paint the border and image.
        g.translate(x, y);
        paintBorder(g, style, width, height);
    }

    /**
     * Draw a border of the selected style.
     * @param g graphics context to which to draw.
     * @param style of the border to display
     * @param w the width reserved for the image
     * @param h the height reserved of the image
     * @see setStyle
     */
    private void paintBorder(Graphics g, int style, int w, int h) {
        if (style == 1) {
            g.setGrayScale(128);
            g.drawRect(-1, -1, w + 1, h + 1);
            g.drawRect(-2, -2, w + 3, h + 3);
        }

        if (style == 2) {
            // Draw fancy border with image between outer and inner rectangles
            if (bimage == null) {
                bimage = genBorder(); // Generate the border image
            }

            int bw = bimage.getWidth();
            int bh = bimage.getHeight();
            int i;
            // Draw the inner and outer solid border
            g.setGrayScale(128);
            g.drawRect(-1, -1, w + 1, h + 1);
            g.drawRect(-bw - 2, -bh - 2, w + (bw * 2) + 3, h + (bh * 2) + 3);

            // Draw it in each corner
            g.drawImage(bimage, -1, -1, Graphics.BOTTOM | Graphics.RIGHT);
            g.drawImage(bimage, -1, h + 1, Graphics.TOP | Graphics.RIGHT);
            g.drawImage(bimage, w + 1, -1, Graphics.BOTTOM | Graphics.LEFT);
            g.drawImage(bimage, w + 1, h + 1, Graphics.TOP | Graphics.LEFT);

            // Draw the embedded image down left and right sides
            for (i = ((h % bh) / 2); i < (h - bh); i += bh) {
                g.drawImage(bimage, -1, i, Graphics.RIGHT | Graphics.TOP);
                g.drawImage(bimage, w + 1, i, Graphics.LEFT | Graphics.TOP);
            }

            // Draw the embedded image across the top and bottom
            for (i = ((w % bw) / 2); i < (w - bw); i += bw) {
                g.drawImage(bimage, i, -1, Graphics.LEFT | Graphics.BOTTOM);
                g.drawImage(bimage, i, h + 1, Graphics.LEFT | Graphics.TOP);
            }
        }
    }

    /**
     * Create an image for the border.
     * The border consists of a simple "+" drawn in a 5x5 image.
     * Fill the image with white and draw the "+" as magenta.
     * @return the image initialized with the pattern
     */
    private Image genBorder() {
        Image image = Image.createImage(5, 5);
        Graphics g = image.getGraphics();
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, 5, 5);
        g.setColor(128, 0, 255);
        g.drawLine(2, 1, 2, 3); // vertical
        g.drawLine(1, 2, 3, 2); // horizontal

        return image;
    }
}

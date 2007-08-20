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
package example.manyballs;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;


public class ManyCanvas extends Canvas {
    static int NUM_HISTORY = 8;
    private static final int STATUSBAR_HEIGHT = 12;
    Display display;

    // a set of free roaming balls
    SmallBall[] balls;
    int numBalls;
    int width;
    int height;
    boolean paused;
    boolean menuShowing;
    long[] times = new long[NUM_HISTORY];
    int times_idx;
    private int threadCount;

    /**
     * Draws the drawing frame (which also contains the ball) and the
     * controls.
     */
    String msg = null;

    public ManyCanvas(Display d, int maxBalls) {
        display = d; // save the display

        // initialize the array of balls
        balls = new SmallBall[maxBalls];

        width = getWidth();
        height = getHeight();

        // Start with one ball
        balls[0] = new SmallBall(this, 0, 0, width, height - STATUSBAR_HEIGHT);
        numBalls = 1;
        paused = true;
    }

    protected void paint(Graphics g) {
        int x = g.getClipX();
        int y = g.getClipY();
        int w = g.getClipWidth();
        int h = g.getClipHeight();

        // Draw the frame
        g.setColor(0xffffff);
        g.fillRect(x, y, w, h);

        // Draw each ball
        for (int i = 0; i < numBalls; i++) {
            if (balls[i].inside(x, y, x + w, y + h)) {
                balls[i].paint(g);
            }
        }

        g.setColor(0);
        g.drawRect(0, 0, width - 1, height - 1);

        long now = System.currentTimeMillis();
        String str = null;

        if (times_idx >= NUM_HISTORY) {
            long oldTime = times[times_idx % NUM_HISTORY];

            if (oldTime == now) {
                // in case of divide-by-zero
                oldTime = now - 1;
            }

            long fps = ((long)1000 * (long)NUM_HISTORY) / (now - oldTime);

            if ((times_idx % 20) == 0) {
                str = numBalls + " Ball(s) " + fps + " fps";
            }
        } else {
            if ((times_idx % 20) == 0) {
                str = numBalls + " Ball(s)";
            }
        }

        if (msg != null) {
            g.setColor(0xffffff);
            g.setClip(0, height - STATUSBAR_HEIGHT, width, height);
            g.fillRect(0, height - STATUSBAR_HEIGHT, width, STATUSBAR_HEIGHT);

            g.setColor(0);
            g.drawString(msg, 5, height - STATUSBAR_HEIGHT - 2, 0);
            g.drawRect(0, 0, width - 1, height - 1);

            // draw a reflection line
            g.drawLine(0, height - STATUSBAR_HEIGHT, w, height - STATUSBAR_HEIGHT);

            msg = null;
        }

        if (str != null) {
            /*
             * Do a complete repaint, so that the message will
             * be shown even in double-buffer mode.
             */
            repaint();
            msg = str;
        }

        times[times_idx % NUM_HISTORY] = now;
        ++times_idx;
    }

    /**
     * Handle a pen down event.
     */
    public void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        switch (action) {
        case LEFT:

            // Reduce the number of threads
            if (numBalls > 0) {
                // decrement the counter
                numBalls -= 1;

                // stop the thread and remove the reference to it
                balls[numBalls].stop = true;
                balls[numBalls] = null;
            }

            break;

        case RIGHT:

            // Increase the number of threads
            if (numBalls < balls.length) {
                // create a new ball and start it moving
                balls[numBalls] = new SmallBall(this, 0, 0, width, height - STATUSBAR_HEIGHT);
                new Thread(balls[numBalls]).start();

                // increment the counter
                numBalls += 1;
            }

            break;

        case UP:
            // Make them move faster
            SmallBall.faster();

            break;

        case DOWN:
            // Make them move slower
            SmallBall.slower();

            break;
        }

        repaint();
    }

    void destroy() {
        // kill all the balls and terminate
        for (int i = 0; (i < balls.length) && (balls[i] != null); i++) {
            balls[i].stop = true;

            // enable the balls to be garbage collected
            balls[i] = null;
        }

        numBalls = 0;
    }

    /*
     * Return whether the canvas is paused or not.
     */
    boolean isPaused() {
        return paused;
    }

    protected void hideNotify() {
        menuShowing = true;
        doPause();
    }

    protected void showNotify() {
        if (!paused && menuShowing) {
            doStart();
        }

        menuShowing = false;
    }

    /**
     * Pause the balls by signaling each of them to stop.
     * The ball object still exists and holds the current position
     * of the ball.  It may be restarted later.
     * The thread will terminate.
     * TBD: is a join needed?
     */
    void pause() {
        if (!paused) {
            paused = true;
            doPause();
        }

        repaint();
    }

    private void doPause() {
        for (int i = 0; (i < balls.length) && (balls[i] != null); i++) {
            balls[i].stop = true;
        }

        waitForSpecifiedNumberOfThreads(0);
        repaint();
    }

    /*
     * Start creates a new thread for each ball and start it.
     */
    void start() {
        if (paused) {
            paused = false;
            doStart();
        }

        repaint();
    }

    private void doStart() {
        display.setCurrent(this);

        for (int i = 0; (i < balls.length) && (balls[i] != null); i++) {
            Thread t = new Thread(balls[i]);
            t.start();
        }

        waitForSpecifiedNumberOfThreads(numBalls);
        repaint();
    }

    public void notifyBallThreadStarted() {
        synchronized (this) {
            threadCount++;
            notifyAll();
        }
    }

    public void notifyBallThreadStopped() {
        synchronized (this) {
            threadCount--;
            notifyAll();
        }
    }

    public void waitForSpecifiedNumberOfThreads(final int threadNumber) {
        try {
            synchronized (this) {
                while (threadCount != threadNumber) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

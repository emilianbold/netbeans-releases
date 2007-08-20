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
package example.audiodemo;

import javax.microedition.lcdui.*;
import javax.microedition.media.control.*;


/**
 * A SmallBall is a lightweight animated ball that runs in its own thread.
 * It moves within a rectangular region, bouncing off the walls.
 */
class SmallBall implements Runnable {
    // random number generator
    static java.util.Random random = new java.util.Random();

    // Global flag to temporarily pause all balls
    public static boolean paused = false;
    private static final int DEFAULT_DELAY = 80;
    private static final int MIN_DELAY = 40;
    private static final int MAX_DELAY = 320;
    private static final int DELAY_INCREMENT = 40;
    private static final int POS_MULTIPLIER = 3;
    
    // controls the speed of all balls; delay in milliseconds
    static int delay = DEFAULT_DELAY;

    // the matrix to transform the direction based on the
    // current direction and which wall was hit
    static int[][] matrix =
        {
            { 1, -1, -1, 1, 1, 1 },
            { -1, -1, 1, 1, -1, 1 },
            null,
            { 1, 1, -1, -1, 1, -1 },
            { -1, 1, 1, -1, -1, -1 }
        };

    // the region in which the ball moves
    int top, left, width, height;

    // the position and radius of the ball
    int posX, posY;
    int radius = 5;
    int ballSize = radius * 2;

    // the direction of the ball is controlled by these two variables
    int deltaX, deltaY;

    // a handle onto the singleton Graphics object
    Graphics g;
    Canvas canvas;
    int note = ToneControl.C4;
    int clr = 0;

    // public variables to control the behaviour of the thread
    public boolean stop; // = false

    // if true, this thread will call repaint()
    public boolean doRepaint; // = false;

    /**
     * Constructor defines the region in which the ball moves as well
     * as its starting position.
     */
    SmallBall(Canvas c, int left, int top, int width, int height) {
        super();
        canvas = c;

        this.left = left + 1;
        this.top = top + 1;
        this.width = width - ((2 * radius) + 2);
        this.height = height - ((2 * radius) + 2);

        // use positive random #s
        this.posX = ((random.nextInt() >>> 1) % (this.width - 20)) + 10;
        this.posY = ((random.nextInt() >>> 1) % (this.height - 20)) + 10;

        deltaX = random.nextInt() & 1;
        deltaY = random.nextInt() & 1;

        if (deltaX == 0) {
            deltaX = -1;
        }

        if (deltaY == 0) {
            deltaY = -1;
        }

        stop = true;
    }

    static void slower() {
        delay += DELAY_INCREMENT;

        if (delay > MAX_DELAY) {
            delay = MAX_DELAY;
        }
    }

    static void faster() {
        delay -= DELAY_INCREMENT;

        if (delay < MIN_DELAY) {
            delay = MIN_DELAY;
        }
    }

    /**
     * Returns the speed in percent of
     * the DEFAULT_DELAY
     */
    static int getSpeedPercent() {
        int ret = 100; // start with 100%
        int thisDelay = delay;

        while (thisDelay > DEFAULT_DELAY) {
            thisDelay /= 2;
            ret -= 10;
        }

        while (thisDelay < DEFAULT_DELAY) {
            thisDelay *= 2;
            ret += 10;
        }

        return ret;
    }

    public void setNote(int note) {
        this.note = note;
    }

    /**
     * Starts the ball running.
     */
    public void run() {
        int right = left + width;
        int bottom = top + height;

        while (!stop) {
            ballSize = radius * 2;

            // calculate a direction of the ball as an integer in the range
            // -2 .. 2 (excluding 0)
            int direction = deltaX + deltaY;

            if (direction == 0) {
                direction = deltaX + (2 * deltaY);
            }

            // is the current position colliding with any wall
            int collision = 0;

            if ((posX <= left) || (posX >= right)) {
                collision++;
            }

            if ((posY <= top) || (posY >= bottom)) {
                collision += 2;
            }

            // change the direction appropriately if there was a collision
            if (collision != 0) {
                try {
                    javax.microedition.media.Manager.playTone(note, 100 /*ms*/, 100);
                } catch (Exception ex) {
                    System.out.println("failed to play tone");
                }

                collision = (collision - 1) * 2;

                deltaX = matrix[direction + 2][collision];
                deltaY = matrix[direction + 2][collision + 1];
            }

            // calculate the new position and queue a repaint
            posX += POS_MULTIPLIER * deltaX;
            posY += POS_MULTIPLIER * deltaY;

            if (doRepaint) {
                canvas.repaint();
            }

            // use the delay to control the speed of the ball
            // if the MIDlet is paused, keep on waiting
            do {
                try {
                    // if paused, always wait 100 millis,
                    // regardless of ball speed
                    Thread.sleep(paused ? 100 : delay);
                } catch (InterruptedException e) {
                }
            } while (paused && !stop);
        }
    }

    /**
     * Paint the ball.
     */
    void paint(Graphics g) {
        g.setColor(clr);
        g.fillArc(posX, posY, ballSize, ballSize, 0, 360);
    }

    public void setColor(int clr) {
        this.clr = clr;
    }

    public String toString() {
        return super.toString() + " x = " + posX + ", y = " + posY;
    }
}

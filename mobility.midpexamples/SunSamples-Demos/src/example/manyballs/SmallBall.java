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

import javax.microedition.lcdui.Graphics;


/**
 * A SmallBall is a lightweight animated ball that runs in it's own thread.
 * It moves within a rectangular region, bouncing off the walls.
 */
class SmallBall implements Runnable {
    // random number generator
    static java.util.Random random = new java.util.Random();

    // controls the speed of all balls; delay in centiseconds
    static int delay = 20;

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
    int top;

    // the region in which the ball moves
    int left;

    // the region in which the ball moves
    int width;

    // the region in which the ball moves
    int height;

    // the position and radius of the ball
    int posX;

    // the position and radius of the ball
    int posY;
    int radius = 5;
    int ballSize = radius * 2;

    // the direction of the ball is controlled by these two variables
    int deltaX;
    int deltaY;

    // a handle onto the singleton Graphics object
    Graphics g;
    ManyCanvas canvas;

    // public variables to control the behaviour of the thread
    public boolean stop;

    /**
     * Constructor defines the region in which the ball moves as well
     * as its starting position.
     */
    SmallBall(ManyCanvas c, int left, int top, int width, int height) {
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
    }

    static void slower() {
        delay += 10;

        if (delay > 100) {
            delay = 100;
        }
    }

    static void faster() {
        delay -= 10;

        if (delay < 0) {
            delay = 0;
        }
    }

    public void run() {
        canvas.notifyBallThreadStarted();

        stop = false;

        int right = left + width;
        int bottom = top + height;

        try {
            while (!stop) {
                moveBall(right, bottom);

                // use the delay to control the speed of the ball
                Thread.sleep(delay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        canvas.notifyBallThreadStopped();
    }

    private void moveBall(int right, int bottom) {
        ballSize = radius * 2;

        // calculate a direction of the ball
        // as an integer in the range
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

        // change the direction appropriately
        // if there was a collision
        if (collision != 0) {
            collision = (collision - 1) * 2;

            deltaX = matrix[direction + 2][collision];
            deltaY = matrix[direction + 2][collision + 1];
        }

        // calculate the new position and queue a repaint
        posX += deltaX;
        posY += deltaY;
        canvas.repaint(posX - 1, posY - 1, ballSize + 2, ballSize + 2);
    }

    /**
     * Paint the ball.
     */
    void paint(Graphics g) {
        g.setColor(0);
        g.fillArc(posX, posY, ballSize, ballSize, 0, 360);
    }

    boolean inside(int x1, int y1, int x2, int y2) {
        return (posX <= x2) && (posY <= y2) && ((posX + ballSize) >= x1) &&
        ((posY + ballSize) >= y1);
    }

    public String toString() {
        return super.toString() + " x = " + posX + ", y = " + posY;
    }
}

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

import java.io.InputStream;

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;


public class BallCanvas extends Canvas implements CommandListener {
    private BBall midlet;
    private Player player;
    private int[] notes = { 68, 72, 70, 74, 76, 80, 56, 58 };
    private int[] colors =
        { 0xff, 0xff00, 0xff0000, 0xffff, 0xff00ff, 0xff8080, 0x80ff80, 0x8080ff };

    // a set of free roaming balls
    private SmallBall[] balls;
    private int numBalls;
    private int width;
    private int height;
    private boolean paused;
    private Command backCommand = new Command("Back", Command.BACK, 1);
    private Command pauseCommand = new Command("Pause", Command.BACK, 1);
    private Command playCommand = new Command("Play", Command.BACK, 1);
    private boolean playerCreated;
    private int currBackground = -1;

    public BallCanvas(BBall parentMidlet) {
        midlet = parentMidlet;

        width = getWidth();
        height = getHeight();

        balls = null;
        numBalls = 0;
        paused = true;

        this.addCommand(backCommand);
        this.addCommand(pauseCommand);
        setCommandListener(this);
    }

    public synchronized void init(int maxBalls, int bg) {
        if (maxBalls < 1) {
            maxBalls = 1;
        } else if (maxBalls > notes.length) {
            maxBalls = notes.length;
        }

        if ((balls == null) || (player == null) || (balls.length != maxBalls) ||
                (currBackground != bg)) {
            destroy();
            // initialize the array of balls
            balls = new SmallBall[maxBalls];

            currBackground = bg;
            playerCreated = initPlayer(bg);
        }

        numBalls = 0;
        pause();

        // Start with 2 balls
        makeNumberOfBalls(2);
    }

    boolean needAlert() {
        return (!playerCreated);
    }

    private static String guessContentType(String url)
        throws Exception {
        String ctype;

        // some simple test for the content type
        if (url.endsWith("wav")) {
            ctype = "audio/x-wav";
        } else if (url.endsWith("jts")) {
            ctype = "audio/x-tone-seq";
        } else if (url.endsWith("mid")) {
            ctype = "audio/midi";
        } else {
            throw new Exception("Cannot guess content type from URL: " + url);
        }

        return ctype;
    }

    private void createPlayer(String url) throws Exception {
        if (url.startsWith("resource")) {
            int idx = url.indexOf(':');
            String loc = url.substring(idx + 1);
            InputStream is = getClass().getResourceAsStream(loc);
            String ctype = guessContentType(url);
            player = Manager.createPlayer(is, ctype);
        } else {
            player = Manager.createPlayer(url);
        }
    }

    private boolean initPlayer(int bg) {
        try {
            switch (bg) {
            case 1: // wave bg
                createPlayer(midlet.wavbgUrl);

                break;

            case 2: // tone seq bg
             {
                byte d = 8;
                byte C4 = ToneControl.C4;
                byte D4 = ToneControl.C4 + 2; // a whole step
                byte E4 = ToneControl.C4 + 4; // a major third
                byte G4 = ToneControl.C4 + 7; // a fifth
                byte rest = ToneControl.SILENCE; // eighth-note rest

                byte[] mySequence =
                    new byte[] {
                        ToneControl.VERSION, 1, ToneControl.TEMPO, 30, ToneControl.BLOCK_START, 0,
                        E4, d, D4, d, C4, d, D4, d, E4, d, E4, d, E4, d, rest, d,
                        ToneControl.BLOCK_END, 0, ToneControl.PLAY_BLOCK, 0, D4, d, D4, d, D4, d,
                        rest, d, E4, d, G4, d, G4, d, rest, d, //play "B" section
                        ToneControl.PLAY_BLOCK, 0, // content of "A" section
                        D4, d, D4, d, E4, d, D4, d, C4, d, rest, d // play "C" section
                    };
                player = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
                player.realize();

                ToneControl c = (ToneControl)player.getControl("ToneControl");
                c.setSequence(mySequence);
            }

            break;

            case 3: // MIDI bg
                createPlayer(midlet.midbgUrl);

                break;

            default:
                player = null;
            }

            if (player != null) {
                player.setLoopCount(-1);
                player.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            if (player != null) {
                player.close();
            }

            player = null;

            return false;
        }

        return true;
    }

    /**
     * Draws the drawing frame (which also contains the ball) and the
     * controls.
     */
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
            if (balls[i] != null) {
                balls[i].paint(g);
            }
        }

        g.setColor(0);
        g.drawRect(0, 0, width - 1, height - 1);
    }

    private void makeNumberOfBalls(int newNum) {
        if (balls != null) {
            if (newNum > balls.length) {
                newNum = balls.length;
            } else if (newNum < 1) {
                newNum = 1;
            }

            if (newNum != numBalls) {
                // temporarily disable painting
                numBalls = 0;

                // first create newNum balls, if necessary
                for (int i = 0; i < newNum; i++) {
                    if (balls[i] == null) {
                        balls[i] = new SmallBall(this, 0, 0, width, height);
                        balls[i].setNote(notes[i]);
                        balls[i].setColor(colors[i]);
                    }

                    if (!paused && balls[i].stop) {
                        balls[i].stop = false;
                        (new Thread(balls[i])).start();
                    }
                }

                // then destroy any other balls
                for (int i = newNum; i < balls.length; i++) {
                    if (balls[i] != null) {
                        // stop the thread and remove the reference to it
                        balls[i].stop = true;
                        balls[i] = null;
                    }
                }

                // enable painting
                numBalls = newNum;

                if (newNum > 0) {
                    balls[0].doRepaint = true;
                }
            }
        }
    }

    /**
     * Destroy
     */
    synchronized void destroy() {
        // kill all the balls and terminate
        numBalls = 0;
        pause();
        balls = null;

        if (player != null) {
            player.close();
            player = null;
        }
    }

    /*
     * Return whether the canvas is paused or not.
     */
    boolean isPaused() {
        return paused;
    }

    /**
     * Pause the balls by signaling each of them to stop.
     * The ball object still exists and holds the current position
     * of the ball.  It may be restarted later.
     * the current thread will be terminated.
     */
    void pause() {
        if (!paused) {
            synchronized (this) {
                paused = true;

                for (int i = 0; i < balls.length; i++) {
                    if (balls[i] != null) {
                        balls[i].stop = true;
                    }
                }

                try {
                    if (player != null) {
                        player.stop();
                    }
                } catch (MediaException e) {
                    // There's nothing much we can do here.
                }
            }

            repaint();
        }
    }

    /*
     * Start creates a new thread for each ball and start it.
     */
    void start() {
        if (paused) {
            synchronized (this) {
                paused = false;

                if (balls != null) {
                    for (int i = 0; i < balls.length; i++) {
                        if (balls[i] != null) {
                            balls[i].stop = false;
                            (new Thread(balls[i])).start();
                        }
                    }
                }

                if (player != null) {
                    try {
                        player.start();
                    } catch (Exception ex) {
                    }
                }
            }

            repaint();
        }
    }

    public void commandAction(Command c, Displayable s) {
        if (c == backCommand) {
            destroy();
            midlet.displayList();
        } else if (c == pauseCommand) {
            pause();
            removeCommand(pauseCommand);
            addCommand(playCommand);
        } else if (c == playCommand) {
            removeCommand(playCommand);
            addCommand(pauseCommand);
            start();
        }
    }

    /**
     * Handle a pen down event.
     */
    public void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        switch (action) {
        case LEFT:
            // Reduce the number of threads
            makeNumberOfBalls(numBalls - 1);

            break;

        case RIGHT:
            // Increase the number of threads
            makeNumberOfBalls(numBalls + 1);

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
}

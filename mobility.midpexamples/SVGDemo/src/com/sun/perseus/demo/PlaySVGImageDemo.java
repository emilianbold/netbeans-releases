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
package com.sun.perseus.demo;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.*;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;
import javax.microedition.midlet.*;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGSVGElement;


/**
 * Simple demo which plays an SVG animation, using the JSR 226
 * SVGAnimator class.
 */
public class PlaySVGImageDemo extends MIDlet implements CommandListener, SVGEventListener {
    /**
     * The SVG Namespace URI.
     */
    public static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

    /**
     * The minimal amount of time to wait when a splash screen is displayed.
     */
    public static final long SPLASH_MIN_LENGTH = 2500; // 2.5s

    /**
     * State constants. These are used to track the animator's state.
     */
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_PLAYING = 2;

    // indicates an interruption while in the playing state (for example by an 
    // incomming call)
    public static final int STATE_INTERRUPTED = 3;

    /**
     * Key codes used to control the animator's state.
     */
    public static final int KEY_STOP = Canvas.KEY_NUM0;
    public static final int KEY_PLAY = Canvas.KEY_NUM5;
    public static final int KEY_PAUSE = Canvas.KEY_NUM8;
    public static final int KEY_START_DEMO = Canvas.KEY_NUM1;
    public static final int KEY_ROTATE = Canvas.KEY_NUM7;
    public static final int KEY_HELP = Canvas.KEY_NUM4;

    /**
     * Error message thrown when the SVG resource could not be loaded.
     */
    public static final String ERROR_COULD_NOT_LOAD_SVG =
        "Error: Could not load SVG resource referenced by MIDlet : ";

    /**
     * The SVG image played by this MIDlet is stored along the MIDlet's code.
     */
    public final String svgImageName;

    /**
     * The optional splash image to show while the resources for the MIDlet are
     * loading.
     */
    public final String splashImageName;

    /**
     * This MIDlet has a single exit command to bo back to the previous menu.
     */
    private final Command exitCommand = new Command("Exit", Command.EXIT, 1);

    /**
     * The Canvas, managed by the SVGAnimator, where the SVG animation is
     * displayed.
     * @see #SVGAnimator.getTargetComponent();
     */
    protected Canvas svgCanvas;

    /**
     * The splash Canvas. May be null.
     */
    protected Canvas splashCanvas;

    /**
     * The SVGAnimator built from the SVG resource.
     */
    protected SVGAnimator svgAnimator;

    /**
     * The SVGImage associated with the SVGAnimator
     */
    protected SVGImage svgImage;

    /**
     * The Document associated with the SVGAnimator
     */
    protected Document doc;

    /**
     * The root svg element.
     */
    protected SVGSVGElement svg;

    /**
     * One of STATE_STOPPED, STATE_PAUSED, STATE_PLAYING or STATE_INTERRUPTED
     */
    protected int state = STATE_STOPPED;

    /**
     * Indicates if the animation should be started automatically.
     */
    private boolean autoStart;

    public PlaySVGImageDemo(String svgImage, String splashImage, boolean autoStart) {
        this.svgImageName = svgImage;
        this.splashImageName = splashImage;
        this.autoStart = autoStart;
    }

    public void startApp() {
        if (svgCanvas == null) {
            // If there is a splash screen defined, show it immediately.
            boolean hasSplash = ((splashImageName != null) && !"".equals(splashImageName));

            if (hasSplash) {
                InputStream splashStream = getClass().getResourceAsStream(splashImageName);

                try {
                    Image splashImage = Image.createImage(splashStream);
                    splashCanvas = new SplashCanvas(splashImage);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    hasSplash = false;
                }

                if (splashCanvas != null) {
                    Display.getDisplay(this).setCurrent(splashCanvas);
                }
            }

            long start = System.currentTimeMillis();

            // Get input stream to the SVG image stored in the MIDlet's jar.
            InputStream svgDemoStream = getClass().getResourceAsStream(svgImageName);

            // Build an SVGImage instance from the stream
            svgImage = null;

            if (svgDemoStream != null) {
                try {
                    svgImage = (SVGImage)SVGImage.createImage(svgDemoStream, null);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

            if (svgImage == null) {
                throw new Error(ERROR_COULD_NOT_LOAD_SVG + svgImageName);
            }

            // Build an SVGAnimator from the SVGImage. The SVGAnimator will handle
            // all the animation details and run the SVG animation in a 
            // Canvas instance.
            svgAnimator = SVGAnimator.createAnimator(svgImage);

            // Set to 10 fps (frames per second)
            svgAnimator.setTimeIncrement(0.01f);

            // Get the Canvas managed by the SVGAnimator and set the 
            // svgImage's size to the canvas display area.
            svgCanvas = (Canvas)svgAnimator.getTargetComponent();
            svgImage.setViewportWidth(svgCanvas.getWidth());
            svgImage.setViewportHeight(svgCanvas.getHeight());

            // Hook the exit command so that we can exit the animation demo.
            svgCanvas.addCommand(exitCommand);
            svgCanvas.setCommandListener(this);

            // The SVG root element is used to reset the time on a stop operation.
            doc = svgImage.getDocument();
            svg = (SVGSVGElement)doc.getDocumentElement();

            // Hook-in key listeners to play, pause and stop the animation.
            svgAnimator.setSVGEventListener(this);

            long end = System.currentTimeMillis();

            if (hasSplash) {
                long waitMore = SPLASH_MIN_LENGTH - (end - start);

                if (waitMore > 0) {
                    try {
                        Thread.currentThread().sleep(waitMore);
                    } catch (InterruptedException ie) {
                        // Do nothing.
                    }
                }
            }

            if (autoStart) {
                svgAnimator.play();
                state = STATE_PLAYING;
                System.err.println("PLAYING...");
            }
        }

        Display.getDisplay(this).setCurrent(svgCanvas);
        resumeAnimation();
    }

    public void keyPressed(int keyCode) {
        if ((keyCode == KEY_PLAY) && (state != STATE_PLAYING)) {
            svgAnimator.play();
            state = STATE_PLAYING;
            System.err.println("PLAYING...");
        }

        if ((keyCode == KEY_PAUSE) && (state == STATE_PLAYING)) {
            svgAnimator.pause();
            state = STATE_PAUSED;
            System.err.println("PAUSED...");
        }

        if ((keyCode == KEY_STOP) && (state != STATE_STOPPED)) {
            svgAnimator.stop();
            svg.setCurrentTime(0);
            state = STATE_STOPPED;
            System.err.println("STOPPED...");
        }
    }

    public void keyReleased(int keyCode) {
    }

    public void pointerPressed(int x, int y) {
    }

    public void pointerReleased(int x, int y) {
    }

    public void hideNotify() {
    }

    public void showNotify() {
    }

    public void sizeChanged(int width, int height) {
    }

    public void pauseApp() {
        interruptAnimation();
    }

    public void destroyApp(boolean unconditional) {
        if (state != STATE_STOPPED) {
            svgAnimator.stop();
            state = STATE_STOPPED;
        }

        svgAnimator = null;
        svgCanvas = null;
        System.gc();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }

    /**
     * Pauses the animation if it is being played.
     */
    private void interruptAnimation() {
        if (state == STATE_PLAYING) {
            svgAnimator.pause();
            state = STATE_INTERRUPTED;
        }

        // Otherwise, the animation is paused or stopped.
    }

    /**
     * Resumes the animation if it has been interrupted in its playing state.
     */
    private void resumeAnimation() {
        if (state == STATE_INTERRUPTED) {
            // resume the interrupted animation
            svgAnimator.play();
            state = STATE_PLAYING;
        }
    }
}

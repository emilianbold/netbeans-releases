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
package com.sun.perseus.demo.locationBasedService;

import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import org.w3c.dom.Document;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGRGBColor;

import com.sun.perseus.demo.PlaySVGImageDemo;


/**
 * An example of zooming and panning. A sample itinerary is used in combination
 * with a map of downtown San Francisco to show how a route can be traced from a
 * starting point to an ending point.  This also demonstrates how to
 * insert/remove the traced route into/from the map content so that the route
 * can also be scaled with the map.
 */
public final class LocationBasedService extends PlaySVGImageDemo implements Runnable {
    /**
     * The image that holds the animated SVG content.
     */
    private static final String SVG_IMAGE = "/svg/locationBasedService.svg";

    /**
     * The image that holds the splash screen image.
     */
    private static final String SPLASH_IMAGE = "/images/LocationBasedHelp.png";
    Object[][] points =
        new Object[][] {
            { new float[] { 14f, 60f }, "Washington and Battery" },
            { new float[] { 14f, 146f }, "Left on Sacramento" },
            { new float[] { 153f, 146f }, "Right on Drumm" },
            { new float[] { 153f, 204f }, "Left on Market" },
            { new float[] { 229f, 151f }, "Right on Stewart" },
            { new float[] { 278f, 219f }, "First left" },
            { new float[] { 312f, 194f }, "Left on Embarcadero" },
            { new float[] { 275f, 141f }, "You made it!" }
        };

    /**
     * Location markers
     */
    SVGElement[] markers = new SVGElement[points.length];

    /**
     * Marker connectors
     */
    SVGElement[] connectors = new SVGElement[points.length - 1];

    /**
     * Animation steps
     */
    Runnable initialDisplay = new InitialDisplay();
    Runnable locationBasedServiceAnim = new LocationBasedServiceAnim();
    Runnable finalDisplay = new FinalDisplay();

    /**
     * Message element
     */
    SVGElement message;

    /**
     * Message location
     */
    float messageX;

    /**
     * Message location
     */
    float messageY;

    /**
     * Message background
     */
    SVGElement messageBackground;

    /**
     * Color constant for markers and connectors
     */
    // EXTENSION
    // final String MARKER_COLOR = "red";
    // REPLACE WITH
    SVGRGBColor MARKER_COLOR = null;
    SVGRGBColor MARKER_BLACK = null;
    SVGRGBColor MARKER_WHITE = null;

    /**
     * Current Animation
     */
    Runnable step = initialDisplay;

    /**
     * Initial viewbox
     */
    SVGRect initialVB;

    /**
     * Initial screen bbox
     */
    SVGRect initialBBox;

    /**
     * Controls whether the animation is paused or not
     */
    boolean paused = true;

    /**
     * Default constructor
     */
    public LocationBasedService() {
        super(SVG_IMAGE, SPLASH_IMAGE, false);
    }

    /**
     * By default, start the demo in the playing state.
     */
    public void startApp() {
        super.startApp();

        init(svgImage.getDocument());
    }

    /**
     * @param doc the DocumentNode holding the expected SVG content.
     */
    public void init(final Document doc) {
        svg = (SVGSVGElement)doc.getDocumentElement();
        // EXTENSION
        MARKER_COLOR = svg.createSVGRGBColor(255,0,0);
        MARKER_BLACK = svg.createSVGRGBColor(0,0,0);
        MARKER_WHITE = svg.createSVGRGBColor(255,255,255);

        final SVGElement inserts = (SVGElement)doc.getElementById("inserts");

        //
        // Add markers for each of the locationBasedService points
        //
        final SVGElement markersGroup = (SVGElement)doc.createElementNS(SVG_NAMESPACE_URI, "g");

        markersGroup.setTrait("visibility", "visible");

        for (int i = 0; i < points.length; i++) {
            Object[] point = points[i];
            float[] coord = (float[])point[0];
            SVGElement marker = (SVGElement)doc.createElementNS(SVG_NAMESPACE_URI, "ellipse");
            marker.setFloatTrait("cx", coord[0]);
            marker.setFloatTrait("cy", coord[1]);
            marker.setFloatTrait("rx", 5);
            marker.setFloatTrait("ry", 5);
            // EXTENSION
            // marker.setTrait("fill", MARKER_COLOR);
	    // REPLACE WITH
            marker.setRGBColorTrait("fill", MARKER_COLOR);
            markersGroup.appendChild(marker);
            markers[i] = marker;
        }

        //
        // Add connector lines for each of the locationBasedService points
        // except the last.
        //
        final SVGElement connectorsGroup = (SVGElement)doc.createElementNS(SVG_NAMESPACE_URI, "g");
        connectorsGroup.setTrait("fill", "none");
        // EXTENSION
        // connectorsGroup.setTrait("stroke", MARKER_COLOR);
        // REPLACE WITH
        connectorsGroup.setRGBColorTrait("stroke", MARKER_COLOR);
        connectorsGroup.setFloatTrait("stroke-width", 2.5f);

        for (int i = 0; i < (points.length - 1); i++) {
            connectors[i] = (SVGElement)doc.createElementNS(SVG_NAMESPACE_URI, "line");
            connectors[i].setTrait("visibility", "hidden");
            connectors[i].setFloatTrait("x1", markers[i].getFloatTrait("cx"));
            connectors[i].setFloatTrait("y1", markers[i].getFloatTrait("cy"));
            connectors[i].setFloatTrait("x2", markers[i].getFloatTrait("cx"));
            connectors[i].setFloatTrait("y2", markers[i].getFloatTrait("cy"));
            connectorsGroup.appendChild(connectors[i]);
        }

        //
        // Message
        //
        message = (SVGElement)doc.createElementNS(SVG_NAMESPACE_URI, "text");
        message.setTrait("text-anchor", "middle");
        // EXTENSION
        // message.setTrait("fill", "black");
        // REPLACE WITH
        message.setRGBColorTrait("fill", MARKER_BLACK);
        // EXTENSION
        // message.setTrait("fill-opacity", "0.5");
        // message.setTrait("font-family", "SunSansDemiEmbeded");
        messageBackground = (SVGElement)doc.createElementNS(SVG_NAMESPACE_URI, "rect");
        // EXTENSION
        // messageBackground.setTrait("fill", "white");
        // REPLACE WITH
        messageBackground.setRGBColorTrait("fill", MARKER_WHITE);
        // EXTENSION
        // messageBackground.setFloatTrait("fill-opacity", 0.75f);

        //
        // Remember the initial viewbox & screenBBox
        //
        initialVB = svg.getRectTrait("viewBox");
        initialBBox = svg.getScreenBBox();

        inserts.appendChild(connectorsGroup);
        inserts.appendChild(markersGroup);
        svg.appendChild(messageBackground);
        svg.appendChild(message);

        Thread th =
            new Thread() {
                public void run() {
                    while (true) {
                        try {
                            if (state == STATE_PLAYING) {
                                if (!paused) {
                                    svgAnimator.invokeAndWait(LocationBasedService.this);
                                }
                            }

                            sleep(200);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            };

        th.start();
    }

    public void keyPressed(int keyCode) {
        if (keyCode == KEY_START_DEMO) {
            if (state != STATE_PLAYING) {
                svgAnimator.play();
                state = STATE_PLAYING;
            }

            paused = !paused;
        } else if (keyCode == KEY_ROTATE) {
            if (state == STATE_PLAYING) {
                svgAnimator.invokeLater(new Runnable() {
                        public void run() {
                            if (svg == null) {
                                System.err.println("svg is null!!!!");
                            }

                            if (svg.getCurrentRotate() == 0) {
                                // Rotate by 90 degrees counter clock wise
                                svg.setCurrentRotate(-90);

                                // Scale by the height / width factor
                                float scale = svgCanvas.getHeight() / initialBBox.getWidth();
                                svg.setCurrentScale(scale);

                                // Move the origin to bthe bottom left of the display.
                                SVGPoint pan = svg.getCurrentTranslate();
                                pan.setX(-initialBBox.getY() * scale);
                                pan.setY(svgCanvas.getHeight());
                            } else {
                                svg.setCurrentRotate(0);

                                SVGPoint pan = svg.getCurrentTranslate();
                                pan.setX(0);
                                pan.setY(0);
                                svg.setCurrentScale(1);
                            }
                        }
                    });
            }
        } else if (keyCode == KEY_HELP) {
            if (splashCanvas != null) {
                // If the demo is playing, pause it
                final boolean wasPaused = paused;

                if (state == STATE_PLAYING) {
                    paused = true;
                }

                // Show the splashCanvas for a little while.
                Display.getDisplay(this).setCurrent(splashCanvas);

                Thread th =
                    new Thread() {
                        public void run() {
                            System.err.println("Starting to sleep");

                            try {
                                Thread.currentThread().sleep(SPLASH_MIN_LENGTH);
                            } catch (InterruptedException ie) {
                            }

                            System.err.println("done sleeping");
                            Display.getDisplay(LocationBasedService.this).setCurrent(svgCanvas);

                            if (!wasPaused) {
                                paused = false;
                            }
                        }
                    };

                th.start();
            }
        }
    }

    public void run() {
        step.run();
    }

    class InitialDisplay implements Runnable {
        long start = 0;
        long pauseFor = 3000;
        long animSteps = 5;
        long curAnimStep = 0;

        public void run() {
            if (start == 0) {
                start = System.currentTimeMillis();

                // Hide markers and connectors
                for (int i = 0; i < markers.length; i++) {
                    markers[i].setTrait("visibility", "hidden");
                }

                // Position initial message
                messageX = initialVB.getX() + (initialVB.getWidth() / 2);
                messageY = initialVB.getY() + (initialVB.getHeight() / 2);
                message.setFloatTrait("x", messageX);
                message.setFloatTrait("y", messageY);
                message.setTrait("#text", "Your Itinerary");

                float fontSize = 40;
                message.setFloatTrait("font-size", fontSize);

                messageBackground.setFloatTrait("x", initialVB.getX() + 20);
                messageBackground.setFloatTrait("y",
                    (initialVB.getY() + (initialVB.getHeight() / 2)) - fontSize);
                messageBackground.setFloatTrait("width", initialVB.getWidth() - 40);
                messageBackground.setFloatTrait("height", (3 * fontSize) / 2);
                message.setTrait("visibility", "visible");
                messageBackground.setTrait("visibility", "visible");
                curAnimStep = 0;
            } else {
                // Animate the viewbox to the first locationBasedService point
                long curTime = System.currentTimeMillis();
                long aLength = curTime - start;
                float messageFontSize = 6;

                if (aLength > pauseFor) {
                    message.setTrait("visibility", "hidden");
                    messageBackground.setTrait("visibility", "hidden");

                    if (curAnimStep >= animSteps) {
                        LocationBasedService.this.step = locationBasedServiceAnim;
                        start = 0;
                        message.setFloatTrait("font-size", messageFontSize);
                    } else {
                        float p = (curAnimStep + 1) / (float)animSteps;
                        SVGRect vb = svg.getRectTrait("viewBox");
                        float[] coord = (float[])points[0][0];
                        vb.setX(coord[0] - 40);
                        vb.setY(coord[1] - 40);
                        vb.setWidth(80);
                        vb.setHeight((vb.getWidth() * initialVB.getHeight()) / initialVB.getWidth());

                        vb.setX(initialVB.getX() + (p * (vb.getX() - initialVB.getX())));
                        vb.setY(initialVB.getY() + (p * (vb.getY() - initialVB.getY())));
                        vb.setWidth(initialVB.getWidth() +
                            (p * (vb.getWidth() - initialVB.getWidth())));
                        vb.setHeight(initialVB.getHeight() +
                            (p * (vb.getHeight() - initialVB.getHeight())));

                        svg.setRectTrait("viewBox", vb);
                        curAnimStep++;
                    }
                }
            }
        }
    }

    class LocationBasedServiceAnim implements Runnable {
        int cur = 0;
        long start = 0;
        long pauseFor = 1500;
        long animateFor = 2000;
        int curAnimStep = 0;
        int animSteps = 5;
        float messageFontSize = 6;

        public void run() {
            if (cur == LocationBasedService.this.points.length) {
                cur = 0;
                LocationBasedService.this.step = finalDisplay;
            } else {
                if (start == 0) {
                    // Show the new marker
                    markers[cur].setTrait("visibility", "visible");

                    // Show the connector line to the next marker
                    // if there is a next marker, i.e except the last
                    // marker.
                    if (cur < connectors.length) {
                        connectors[cur].setTrait("visibility", "visible");
                        connectors[cur].setFloatTrait("x2", connectors[cur].getFloatTrait("x1"));
                        connectors[cur].setFloatTrait("y2", connectors[cur].getFloatTrait("y1"));
                    }

                    // Set the viewbox to be around the new
                    // point of interest
                    SVGRect vb = svg.getRectTrait("viewBox");
                    float[] coord = (float[])points[cur][0];
                    vb.setX(coord[0] - 40);
                    vb.setY(coord[1] - 40);
                    vb.setWidth(80);
                    vb.setHeight((vb.getWidth() * initialVB.getHeight()) / initialVB.getWidth());
                    svg.setRectTrait("viewBox", vb);

                    // Show message
                    messageX = vb.getX() + (vb.getWidth() / 2);
                    messageY = (vb.getY() + vb.getHeight()) - 2;
                    message.setFloatTrait("x", messageX);
                    message.setFloatTrait("y", messageY);
                    message.setTrait("#text", (String)points[cur][1]);
                    messageBackground.setFloatTrait("x", vb.getX());
                    messageBackground.setFloatTrait("y", messageY - messageFontSize);
                    messageBackground.setFloatTrait("width", 80);
                    messageBackground.setFloatTrait("height", (4 * messageFontSize) / 3);
                    messageBackground.setTrait("visibility", "visible");
                    message.setTrait("visibility", "visible");

                    // Starting now
                    start = System.currentTimeMillis();

                    curAnimStep = 0;
                } else {
                    long curTime = System.currentTimeMillis();
                    long aLength = curTime - start;

                    if (aLength > pauseFor) {
                        messageBackground.setTrait("visibility", "hidden");
                        message.setTrait("visibility", "hidden");

                        if ((cur == (LocationBasedService.this.points.length - 1)) ||
                                ((aLength > pauseFor) && (curAnimStep >= animSteps))) {
                            cur++;
                            start = 0;
                            curAnimStep = 0;
                            messageBackground.setTrait("visibility", "hidden");
                            message.setTrait("visibility", "hidden");
                        } else {
                            SVGRect vb = svg.getRectTrait("viewBox");
                            float[] startCoord = (float[])points[cur][0];
                            float[] endCoord = (float[])points[cur + 1][0];
                            float p = (curAnimStep + 1) / (float)animSteps;
                            float x = startCoord[0] + (p * (endCoord[0] - startCoord[0]));
                            float y = startCoord[1] + (p * (endCoord[1] - startCoord[1]));
                            vb.setX(x - 40);
                            vb.setY(y - 40);
                            vb.setWidth(80);
                            vb.setHeight((vb.getWidth() * initialVB.getHeight()) / initialVB.getWidth());
                            connectors[cur].setFloatTrait("x2", x);
                            connectors[cur].setFloatTrait("y2", y);
                            svg.setRectTrait("viewBox", vb);
                            curAnimStep++;
                        }
                    }
                }
            }
        }
    }

    class FinalDisplay implements Runnable {
        long start = 0;
        SVGRect startVB;
        long animateFor = 2000;
        long pauseFor = 3000;
        boolean vpRestored = false;

        public void run() {
            if (start == 0) {
                startVB = svg.getRectTrait("viewBox");
                message.setTrait("#text", ((String)points[points.length - 1][1]));
                start = System.currentTimeMillis();
                vpRestored = false;
            } else {
                long curTime = System.currentTimeMillis();

                if ((curTime - start) < animateFor) {
                    float p = (curTime - start) / (float)animateFor;
                    SVGRect vb = svg.getRectTrait("viewBox");
                    vb.setX(startVB.getX() + (p * (initialVB.getX() - startVB.getX())));
                    vb.setY(startVB.getY() + (p * (initialVB.getY() - startVB.getY())));
                    vb.setWidth(startVB.getWidth() +
                        (p * (initialVB.getWidth() - startVB.getWidth())));
                    vb.setHeight(startVB.getHeight() +
                        (p * (initialVB.getHeight() - startVB.getHeight())));
                    svg.setRectTrait("viewBox", vb);
                } else if (!vpRestored) {
                    svg.setRectTrait("viewBox", initialVB);
                    vpRestored = true;
                } else if ((curTime - start) > (animateFor + pauseFor)) {
                    start = 0;
                    step = initialDisplay;
                    paused = true;

                    for (int ci = 0; ci < connectors.length; ci++) {
                        connectors[ci].setTrait("visibility", "hidden");
                    }

                    for (int mi = 0; mi < markers.length; mi++) {
                        markers[mi].setTrait("visibility", "hidden");
                    }
                }
            }
        }
    }
}

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
package com.sun.perseus.demo.picturedecorator;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;


/**
 * The <code>ItemPicker</code> canvas creates thumbnails of the various props
 * from its associated props images and displays them in a grid.
 * The user can use the arrow keys to select a thumbnail (which gets highlighted)
 * and use the fire button to select the desired item or the '*' key to cancel
 * the picker.
 */
public class ItemPicker extends Canvas {
    /**
     * The SVG Namespace URI.
     */
    public static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

    /**
     * The XLink Namespace URI.
     */
    public static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";

    /**
     * Desired thumbnail width
     */
    public static final int THUMBNAIL_WIDTH = 60;

    /**
     * Desired thumbnail height
     */
    public static final int THUMBNAIL_HEIGHT = 60;

    /**
     * The desired padding for each thumbnail
     */
    public static final int THUMBNAIL_PADDING = 5;

    /**
     * Offscreen where the thumbnails are rendered.
     */
    private Image offscreen;

    /**
     * Selected row
     */
    private int selectedRow;

    /**
     * Selected col
     */
    private int selectedCol;

    /**
     * Number of rows
     */
    private int nr;

    /**
     * Number of columns
     */
    private int nc;

    /**
     * The highest number of rows.
     */
    private int maxR;

    /**
     * The highest number of columns
     */
    private int maxC;

    /**
     * The grid's top margin
     */
    private int topMargin;

    /**
     * The grid's left margin
     */
    private int leftMargin;

    /**
     * The display to use
     */
    private Display display;

    /**
     * The associated PhotoFrame instance where the image is decorated
     * with items selected in this picker.
     */
    private PhotoFrame frame;

    /**
     * ScalableGraphics used to render the SVG image
     */
    private ScalableGraphics sg;

    /**
     * The use element used to render items.
     */
    private SVGLocatableElement use;

    /**
     * The root svg element.
     */
    private SVGSVGElement svg;

    /**
     * True when the user is in the process of selecting an item.
     */
    private boolean selecting;

    /**
     * The list of displayed items.
     */
    private Vector items;

    /**
     * The SVGImage where the items are defined.
     */
    private SVGImage svgImage;

    /**
     * @param frame the associated PhotoFrame instance.
     * @param svgImage the svgImage where items are defined.
     * @param svgImage the SVGImage containing all the props definitions.
     */
    public ItemPicker(final PhotoFrame frame, final SVGImage svgImage, final Display display) {
        if ((svgImage == null) || (frame == null) || (display == null)) {
            throw new NullPointerException();
        }

        this.display = display;
        this.frame = frame;
        this.svgImage = svgImage;

        // Save the original viewport width / height
        int originalWidth = svgImage.getViewportWidth();
        int originalHeight = svgImage.getViewportHeight();

        // Prepare an offscreen raster
        offscreen = Image.createImage(getWidth(), getHeight());

        Graphics g = offscreen.getGraphics();
        sg = ScalableGraphics.createInstance();

        // We will render the SVG image in thumbnails
        svgImage.setViewportWidth(THUMBNAIL_WIDTH);
        svgImage.setViewportHeight(THUMBNAIL_HEIGHT);

        // Create an offscreen buffer where we will draw the different 
        // items in a grid-like layout.
        int width = getWidth();
        int height = getHeight();

        int pad = THUMBNAIL_PADDING;
        int iconWidth = THUMBNAIL_WIDTH;
        int iconHeight = THUMBNAIL_HEIGHT;

        nc = (width - pad) / (iconWidth + pad);
        nr = (height - pad) / (iconHeight + pad);

        topMargin = (height - (nr * iconHeight) - ((nr - 1) * pad)) / 2;
        leftMargin = (width - (nc * iconWidth) - ((nc - 1) * pad)) / 2;

        items = new Vector();

        Document doc = svgImage.getDocument();

        svg = (SVGSVGElement)svgImage.getDocument().getDocumentElement();
        PhotoFrame.locateProps(svg, 0, 0, items, items);

        int nItems = items.size();

        // Append a <use> element that we will use for rendering into the 
        // offscreen.
        use = (SVGLocatableElement)doc.createElementNS(SVG_NAMESPACE_URI, "use");
        svg.appendChild(use);

        SVGRect viewBox = svg.getRectTrait("viewBox");

        // Now, we render up to the maximum number of items.
        int curRow = 0;
        int curCol = 0;
        int tx = leftMargin;
        int ty = topMargin;

        for (int ci = 0; ci < nItems; ci++) {
            SVGElement item = (SVGElement)items.elementAt(ci);
            use.setTraitNS(XLINK_NAMESPACE_URI, "href", "#" + item.getId());

            SVGRect bbox = use.getBBox();

            if (bbox == null) {
                System.out.println("Bounding Box was null for " + item.getId());
            }

            bbox = pad(bbox);

            svg.setRectTrait("viewBox", bbox);

            // Render icon
            sg.bindTarget(g);
            sg.render(tx, ty, svgImage);
            sg.releaseTarget();

            curCol++;
            tx += iconWidth;
            tx += pad;

            if (curCol == nc) {
                curCol = 0;
                tx = leftMargin;
                curRow++;
                ty += iconHeight;
                ty += pad;

                if (curRow == nr) {
                    // We have rendered the maximum number of props.
                    break;
                }
            }
        }

        use.setTrait("display", "none");

        maxC = items.size() % nc;
        maxR = items.size() / nc;

        if (maxC == 0) {
            maxC = nc;
        } else {
            maxR += 1;
        }

        if (items.size() >= (nr * nc)) {
            maxC = nc;
            maxR = nr;
        }

        // Restore the original viewport width / height
        svgImage.setViewportWidth(width);
        svgImage.setViewportHeight(height);
        svg.setRectTrait("viewBox", viewBox);
    }

    public void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);

        if (gameAction == RIGHT) {
            selectedCol++;

            if (selectedRow < (maxR - 1)) {
                if (selectedCol == nc) {
                    selectedCol = 0;
                    selectedRow++;
                }
            } else {
                // We are on the last row
                if (selectedCol == maxC) {
                    selectedCol = 0;
                    selectedRow = 0;
                }
            }

            repaint();
            serviceRepaints();
        } else if (gameAction == LEFT) {
            selectedCol--;

            if (selectedCol < 0) {
                if (selectedRow == 0) {
                    selectedCol = maxC - 1;
                    selectedRow = maxR - 1;
                } else {
                    selectedCol = nc - 1;
                    selectedRow--;
                }
            }

            repaint();
            serviceRepaints();
        } else if (gameAction == UP) {
            selectedRow--;

            if (selectedRow < 0) {
                selectedRow = maxR - 1;

                if (selectedCol >= maxC) {
                    selectedCol = maxC - 1;
                }
            }

            repaint();
            serviceRepaints();
        } else if (gameAction == DOWN) {
            selectedRow++;

            if (selectedRow == maxR) {
                selectedRow = 0;
            } else if (selectedRow == (maxR - 1)) {
                if (selectedCol >= maxC) {
                    selectedCol = maxC - 1;
                }
            }

            repaint();
            serviceRepaints();
        } else if (gameAction == FIRE) {
            if (selecting) {
                // The user is confirming his/her choice
                selecting = false;

                SVGElement item = (SVGElement)items.elementAt(selectedCol + (selectedRow * nc));
                frame.addProp(item.getId());
                display.setCurrent(frame);
            } else {
                selecting = true;
                repaint();
                serviceRepaints();
            }
        } else if (keyCode == KEY_STAR) {
            if (selecting) {
                selecting = false;
                repaint();
                serviceRepaints();
            } else {
                display.setCurrent(frame);
            }
        }
    }

    /**
     * Repaints this canvas with the current item selected.
     *
     * @param g the Graphics to paint into.
     */
    public void paint(final Graphics g) {
        g.drawImage(offscreen, 0, 0, Graphics.TOP | Graphics.LEFT);

        // Draw a rectangle over the currently selected item.
        g.setColor(180, 180, 180);

        int tx = leftMargin + (selectedCol * (THUMBNAIL_PADDING + THUMBNAIL_WIDTH));
        int ty = topMargin + (selectedRow * (THUMBNAIL_PADDING + THUMBNAIL_HEIGHT));
        g.drawRect(tx, ty, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

        if (selecting) {
            SVGElement item = (SVGElement)items.elementAt(selectedCol + (selectedRow * nc));
            use.setTrait("display", "inline");
            use.setTraitNS(XLINK_NAMESPACE_URI, "href", "#" + item.getId());

            SVGRect bbox = pad(use.getBBox());
            SVGRect viewBox = svg.getRectTrait("viewBox");
            int viewportWidth = svgImage.getViewportWidth();
            int viewportHeight = svgImage.getViewportHeight();
            svg.setRectTrait("viewBox", bbox);

            int w = getWidth();

            if (w > getHeight()) {
                w = getHeight();
            }

            w = (int)(0.8f * w);
            svgImage.setViewportWidth(w);
            svgImage.setViewportHeight(w);

            g.setColor(255, 255, 255);
            g.fillRect((getWidth() - w) / 2, (getHeight() - w) / 2, w, w);
            g.setColor(180, 180, 180);
            g.drawRect((getWidth() - w) / 2, (getHeight() - w) / 2, w, w);

            sg.bindTarget(g);
            sg.render((getWidth() - w) / 2, (getHeight() - w) / 2, svgImage);
            sg.releaseTarget();

            svg.setRectTrait("viewBox", viewBox);
            svgImage.setViewportWidth(viewportWidth);
            svgImage.setViewportHeight(viewportHeight);
            use.setTrait("display", "none");
        }
    }

    /**
     * Helper method. Pads the input bounding box.
     *
     * @param bbox the box to pad.
     */
    static SVGRect pad(final SVGRect bbox) {
        float iconPadding = 0.1f; // 10% of the bounding box
        float hPad = bbox.getWidth() * iconPadding;
        float vPad = bbox.getHeight() * iconPadding;
        bbox.setX(bbox.getX() - hPad);
        bbox.setY(bbox.getY() - vPad);
        bbox.setWidth(bbox.getWidth() + (2 * hPad));
        bbox.setHeight(bbox.getHeight() + (2 * vPad));

        return bbox;
    }
}

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


// Exceptions
import java.io.IOException;

// MIDp packages
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.m2g.SVGImage;

// JSR 226 packages
import javax.microedition.m2g.ScalableGraphics;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;


/**
 * The photo frame represents a single photo with SVG overlays.
 */
public class PhotoFrame extends Canvas {
    /**
     * The namespace for xlink:href
     */
    public static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";

    /**
     * The namespace for the svg document.
     */
    public final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

    /** The movement step for the cursor. */
    private final int CURSOR_STEP = 3;

    /**
     * The anchor for the background image.
     */
    private final int ANCHOR = Graphics.VCENTER | Graphics.HCENTER;

    /**
     * The scalable graphics instance used for rendering.
     */
    protected ScalableGraphics sg = ScalableGraphics.createInstance();

    /**
     * The main application that can load images.
     */
    private final PictureDecorator picturedecorator;

    /**
     * The current photo number.
     */
    int photoNumber = 0;

    /**
     * The current background image.
     */
    Image currentImage = null;

    /**
     * The current SVG image.
     */
    SVGImage svgImage = null;

    /**
     * The SVG document.
     */
    Document doc = null;

    /**
     * The <svg> element.
     */
    SVGSVGElement svg = null;

    /**
     * The current prop.
     */
    SVGLocatableElement prop = null;

    /**
     * The list of props that have been placed and can be picked up.
     */
    Vector visibleProps = new Vector();

    /**
     * The crosshair cursor's horizontal position.
     */
    int cursorX = 0;

    /**
     * The crosshair cursor's vertical position.
     */
    int cursorY = 0;

    /**
     * The ItemPicker canvas.
     */
    ItemPicker itemPicker;

    /**
     * The ItemPicker command.
     */
    Command showItemPicker;

    /**
     * Constructs a new photo frame.
     */
    public PhotoFrame(final PictureDecorator main) {
        super();

        picturedecorator = main;

        // There is currently no background image or SVG overlays.
        currentImage = null;
        svgImage = null;
        doc = null;
        
        centerCursor();
    }

    /**
     * Centers the cursor in the canvas.
     */
    private void centerCursor() {
        cursorX = getWidth() / 2;
        cursorY = getHeight() / 2;
    }

    /**
     * Establishes the current background photo.
     * @param number The number of the photo in the JAD file.
     */
    public void setPhoto(final int number) {
        if ((number < 1) || (number > picturedecorator.getMaxPhotoNumber())) {
            throw new IllegalArgumentException("Bad photo number.");
        }

        photoNumber = number;

        final String property = "Attribute-photo" + number;

        //String title = picturedecorator.getImageName(property);
        //setTitle(title);
        currentImage = picturedecorator.getImage(property);
        repaint();
    }

    /**
     * Establishes the current prop library, which holds definitions of props in
     * its <defs> section(s). Each prop is located by its ID.
     * <p>
     * When a prop is used on the display, a <code>&lt;use&gt;</code> element is
     * inserted into the library, effectively making the prop available for
     * display.
     *
     * @param image The SVG image containing the prop definitions. The image may
     *     also contain elements that are statically positioned.
     */
    public void setPropsLibrary(final SVGImage image) {
        // Establish or cancel the props library.
        svgImage = image;
        doc = null;
        svg = null;
        prop = null;

        if (svgImage != null) {
            // Establish the new library.
            doc = svgImage.getDocument();
            svg = (SVGSVGElement)doc.getDocumentElement();

            SVGRect viewBox = svg.getRectTrait("viewBox");
            viewBox.setWidth(getWidth());
            viewBox.setHeight(getHeight());
            svg.setRectTrait("viewBox", viewBox);

            /*
             * The viewport is set to the full size of the canvas. The
             * image will then be moved, scaled and rotated within this
             * environment.
             */
            svgImage.setViewportWidth(getWidth());
            svgImage.setViewportHeight(getHeight());
        }

        // Re-build the item picker
        itemPicker = new ItemPicker(this, svgImage, picturedecorator.display);
        showItemPicker = new Command("Show Picker", Command.SCREEN, 0);
        addCommand(showItemPicker);

        repaint();
    }

    /**
     * Walk the current parent node downward to find all "hiddens" (The SVG
     * elements within a <defs> section that have IDs) and all "visibles"
     * (The SVG elements outside the <defs> sections).
     *
     * @param parent The relative parent node.
     * @param level The nesting level (0..n).
     * @param defs The <defs> nesting level.
     * @param hiddens The list of elements with IDs within a <defs> section.
     * @param visibles The list of elements outside a <defs> section.
     */
    static void locateProps(final SVGElement parent, int level, int defs, Vector hiddens,
        Vector visibles) {
        SVGElement elem = (SVGElement)parent.getFirstElementChild();

        while (elem != null) {
            String name = elem.getLocalName();
            String id = elem.getId();

            // If <defs> was found, go down one more <defs> level.
            if (name.equals("defs")) {
                defs++;
            }

            if (defs > 0) {
                /*
                 * Don't include <defs> tag. Only accept <g> tags with ID's
                 * at this time to keep the prop-locating simple.
                 */

                // If an ID was found within <defs>, record it as a usable prop.
                if (!name.equals("defs") && name.equals("g") && (id != null)) {
                    hiddens.addElement(elem);
                }
            } else {
                /*
                 * If an element is <use> or an ID'ed element outside of <defs>,
                 * record it as a visible prop that can potentially be picked up
                 * and manipulated.
                 */
                if (name.equals("use") || (id != null)) {
                    visibles.addElement(elem);
                }
            }

            // Recurse to try to keep walking the tree downward.
            locateProps(elem, level + 1, defs, hiddens, visibles);

            // Walking up again. "Walk out" of a <defs> if necessary.
            if (level < defs) {
                defs--;
            }

            // Pick up the next sibling and try walking down its subtree.
            elem = (SVGElement)elem.getNextElementSibling();
        }
    }

    /**
     * Adds the prop referenced by <code>id</code> in the library.
     *
     * @param id The identifier of the prop within a <code>&lt;defs&gt;</code>
     *     section of the library.
     */
    public void addProp(final String id) {
        prop = (SVGLocatableElement)doc.createElementNS(SVG_NAMESPACE_URI, "use");
        prop.setTraitNS(XLINK_NAMESPACE_URI, "href", "#" + id);

        addProp(prop);
    }

    /**
     * Adds the prop to the SVG document.
     *
     * @param prop The prop to be added.
     */
    public void addProp(final SVGLocatableElement newProp) {
        if (newProp != null) {
            svg.appendChild(newProp);
            visibleProps.addElement(newProp);

            // The prop is now part of the document. Pick up its bounding box.
            SVGRect r = newProp.getBBox();
            float rx = 0;
            float ry = 0;
            float rwidth = 0;
            float rheight = 0;

            if (r != null) {
                rx = r.getX();
                ry = r.getY();
                rwidth = r.getWidth() / 2;
                rheight = r.getHeight() / 2;
            }

            translateProp(newProp, cursorX - (rx + rwidth), cursorY - (ry + rheight));

            repaint();
        }
    }

    /**
     * Removes the current prop if it is displayed.
     */
    public void removeProp() {
        if (prop != null) {
            svg.removeChild(prop);
            visibleProps.removeElement(prop);
            prop = null;
            repaint();
        }
    }

    /**
     * Translate the prop by the given horizontal and vertical distances.
     *
     * @param dx The horizontal distance to move the prop.
     * @param dy The vertical distance to move the prop.
     */
    public void translateProp(final SVGLocatableElement prop, final float dx, final float dy) {
        if (prop != null) {
            SVGMatrix translateTxf = prop.getMatrixTrait("transform");

            SVGMatrix txf = prop.getScreenCTM();
            translateTxf.mMultiply(txf.inverse());
            translateTxf.mTranslate(dx, dy);
            translateTxf.mMultiply(txf);

            prop.setMatrixTrait("transform", translateTxf);
            repaint();
        }
    }

    /**
     * Scales the prop by the given scale.
     *
     * @param scale The scaling factor to be applied to the prop.
     */
    public void scaleProp(final SVGLocatableElement prop, final float scale) {
        if (prop != null) {
            SVGMatrix scaleTxf = prop.getMatrixTrait("transform");

            SVGMatrix txf = prop.getScreenCTM();
            scaleTxf.mMultiply(txf.inverse());
            scaleTxf.mTranslate(cursorX, cursorY);
            scaleTxf.mScale(scale);
            scaleTxf.mTranslate(-cursorX, -cursorY);
            scaleTxf.mMultiply(txf);

            prop.setMatrixTrait("transform", scaleTxf);
            repaint();
        }
    }

    /**
     * Rotates the prop by the given angle.
     *
     * @param angle The angle by which the prop will be rotated.
     */
    public void rotateProp(final SVGLocatableElement prop, final float angle) {
        if (prop != null) {
            SVGMatrix rotateTxf = prop.getMatrixTrait("transform");

            SVGMatrix txf = prop.getScreenCTM();
            rotateTxf.mMultiply(txf.inverse());
            rotateTxf.mTranslate(cursorX, cursorY);
            rotateTxf.mRotate(angle);
            rotateTxf.mTranslate(-cursorX, -cursorY);
            rotateTxf.mMultiply(txf);

            prop.setMatrixTrait("transform", rotateTxf);
            repaint();
        }
    }

    /**
     * Mirrors the prop about the horizontal or vertical axis.
     *
     * @param angle The angle by which the image will be rotated.
     * @param flipHorizontal <code>true</code> if mirroring will be about the
     *     vertical axis; <code>false</code> if mirroring will be about the
     *     horizontal axis.
     */
    public void mirrorProp(final SVGLocatableElement prop, final boolean flipHorizontal) {
        if (prop != null) {
            SVGMatrix mirrorTxf = prop.getMatrixTrait("transform");

            SVGMatrix mirroringTxf =
                flipHorizontal ? svg.createSVGMatrixComponents(-1, 0, 0, 1, 0, 0)
                               : svg.createSVGMatrixComponents(1, 0, 0, -1, 0, 0);

            SVGMatrix txf = prop.getScreenCTM();
            mirrorTxf.mMultiply(txf.inverse());
            mirrorTxf.mTranslate(cursorX, cursorY);
            mirrorTxf.mMultiply(mirroringTxf);
            mirrorTxf.mTranslate(-cursorX, -cursorY);
            mirrorTxf.mMultiply(txf);

            prop.setMatrixTrait("transform", mirrorTxf);
            repaint();
        }
    }

    /**
     * Handle a keypress.
     * @param keyCode The code for the key that was pressed.
     */
    public void keyPressed(int keyCode) {
        handleKey(keyCode);
    }

    /**
     * Repeat a keypress.
     * @param keyCode The code for the key that was pressed.
     */
    public void keyRepeated(int keyCode) {
        //        keyPressed(keyCode);
    }

    /**
     * Process a keypress.
     * <p>
     * Note: This routine has been made separate from keyPressed in case
     * repeated events need to be handled.
     *
     * @param keyCode The code for the key that was pressed.
     */
    private void handleKey(final int keyCode) {
        if (svgImage == null) {
            // Ignore input until the props are available.
            return;
        }

        int gameAction = getGameAction(keyCode);

        if (gameAction == FIRE) {
            int w = getWidth();
            int h = getHeight();

            if (prop != null) {
                // Detach the prop from the crosshair.
                prop = null;
            } else {
                // Pick up a prop or nothing (Leave prop null).
                prop = getPropContaining((float)cursorX, (float)cursorY);
            }

            // Center the cursor again if it went off the screen.
            if ((cursorX < 0) || (cursorX >= w) || (cursorY < 0) || (cursorY >= h)) {
                centerCursor();
            }

            // Make sure the cursor gets updated.
            repaint();
        } else if (gameAction == UP) {
            cursorY -= CURSOR_STEP;

            if (svgImage != null) {
                translateProp(prop, 0, -CURSOR_STEP);
            }

            repaint();
        } else if (gameAction == DOWN) {
            cursorY += CURSOR_STEP;
            translateProp(prop, 0, CURSOR_STEP);
            repaint();
        } else if (gameAction == LEFT) {
            cursorX -= CURSOR_STEP;
            translateProp(prop, -CURSOR_STEP, 0);
            repaint();
        } else if (gameAction == RIGHT) {
            cursorX += CURSOR_STEP;
            translateProp(prop, CURSOR_STEP, 0);
            repaint();
        } else if (keyCode == Canvas.KEY_NUM0) {
            if (prop != null) {
                removeProp();
            }
        } else if (keyCode == Canvas.KEY_NUM1) {
            scaleProp(prop, 1.00F - 0.10F);
        } else if (keyCode == Canvas.KEY_NUM2) {
            try {
                setPhoto(photoNumber + 1);
            } catch (IllegalArgumentException iae) {
                setPhoto(1);
            }
        } else if (keyCode == Canvas.KEY_NUM3) {
            scaleProp(prop, 1.00F + 0.10F);
        } else if (keyCode == Canvas.KEY_NUM4) {
            picturedecorator.splashCanvas.showAndWait(picturedecorator.display, this);
        } else if (keyCode == Canvas.KEY_NUM5) {
            mirrorProp(prop, true); // Horizontal flip
        } else if (keyCode == Canvas.KEY_NUM6) {
            mirrorProp(prop, false);
        } else if (keyCode == Canvas.KEY_NUM7) {
            rotateProp(prop, -5);
        } else if (keyCode == Canvas.KEY_NUM8) {
            try {
                setPhoto(photoNumber - 1);
            } catch (IllegalArgumentException iae) {
                setPhoto(picturedecorator.getMaxPhotoNumber());
            }
        } else if (keyCode == Canvas.KEY_NUM9) {
            rotateProp(prop, +5);
        } else if (keyCode == Canvas.KEY_POUND) {
            // Show item picker
            picturedecorator.display.setCurrent(itemPicker);
        }
    }

    /**
     * Returns the smallest prop containing the given coordinate.
     * <p>
     * Note: This isn't the most refined method for containing a point within
     * a prop. If a prop is circular, for example, and the coordinate lies
     * outside the visible part of the circle but within the bounding box,
     * the prop will be returned.
     *
     * @param x The horizontal position to be tested.
     * @param y The vertical position to be tested.
     *
     * @return The <code>SVGLocatableElement</code> that contains the
     *     coordinate; <code>null</code> if the coordinate isn't within the
     *     bounding box for any of the props on the screen.
     */
    private SVGLocatableElement getPropContaining(final float x, final float y) {
        // The last smallest rectangle.
        float lastX1 = 0;
        float lastY1 = 0;
        float lastX2 = lastX1 + getWidth();
        float lastY2 = lastY1 + getHeight();

        // The prop that was located (Initially, no prop located.).
        SVGLocatableElement foundProp = null;

        int n = visibleProps.size();

        for (int i = n - 1; i >= 0; i--) {
            SVGLocatableElement elem = (SVGLocatableElement)visibleProps.elementAt(i);

            if (elem == null) {
                // This shouldn't happen, but just in case, don't crash.
                continue;
            }

            SVGRect r = elem.getScreenBBox();

            if (r == null) {
                // This shouldn't happen, but just in case, don't crash.
                continue;
            }

            float x1 = r.getX();
            float y1 = r.getY();
            float x2 = x1 + r.getWidth();
            float y2 = y1 + r.getHeight();

            if ((x >= x1) && (x < x2) && (y >= y1) && (y < y2)) {
                /*
                 * The cursor is within this prop. If this is the first prop
                 * that was found, make sure it the user can get to it (This
                 * can happen in a case where the prop has been enlarged such
                 * that its bounding box has gone beyond the dimensions of the
                 * screen.).
                 */
                if (foundProp == null) {
                    foundProp = elem;
                }

                /*
                 * If this prop has the smallest bounding box, choose this prop
                 * over a prop that was previously selected.
                 */
                if ((x1 >= lastX1) || (x2 < lastX2) || (y1 >= lastY1) || (y2 < lastY2)) {
                    // Use the smaller bounding box now.
                    lastX1 = x1;
                    lastY1 = y1;
                    lastX2 = x2;
                    lastY2 = y2;
                    foundProp = elem;
                }
            }
        } // for

        return foundProp;
    }

    /**
     * Paint the photo in the back, then layer all SVG images on top.
     *
     * @param g The graphics context for this canvas.
     */
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        // Fill the background when a photo isn't present.
        g.setColor(0x00ffffff);
        g.fillRect(0, 0, width, height);

        // Draw the photo only when the user has taken one.
        if (currentImage != null) {
            g.drawImage(currentImage, width / 2, height / 2, ANCHOR);
        }

        // Paint the current SVG image on top of everything.
        if (svgImage != null) {
            sg.bindTarget(g);
            sg.render(0, 0, svgImage);
            sg.releaseTarget();

            /*
             * Draw the cursor. When the cursor is just moving around and has no
             * prop attached, use a green triangle. When a prop is attached to
             * the cursor, represent the cursor with a crosshair.
             *
             * When the cursor is simply moving around, give the user a hint as
             * to which prop can be picked up by highlighting the prop's
             * bounding box.
             */
            if (prop == null) {
                // First, try to locate the prop that contains the cursor.
                SVGLocatableElement elem = getPropContaining((float)cursorX, (float)cursorY);

                // If the prop could be found, show its bounding box.
                if (elem != null) {
                    SVGRect r = elem.getScreenBBox();
                    int rx = (int)r.getX();
                    int ry = (int)r.getY();
                    int rwidth = (int)r.getWidth();
                    int rheight = (int)r.getHeight();

                    // Draw the bounding box in red.
                    g.setColor(0x00FF0000);
                    g.drawRect(rx, ry, rwidth, rheight);
                }

                // No prop is being manipulated; Use a green triangle cursor.
                g.setColor(0x0000CC00);
                g.fillTriangle(cursorX, cursorY, cursorX + 4, cursorY + 12, cursorX - 4,
                    cursorY + 12);
            } else {
                // Prop is being manipulated. Use a red crosshair cursor.
                g.setColor(0x00cc0000);
                g.drawLine(cursorX - 5, cursorY, cursorX - 1, cursorY);
                g.drawLine(cursorX + 1, cursorY, cursorX + 5, cursorY);
                g.drawLine(cursorX, cursorY - 5, cursorX, cursorY - 1);
                g.drawLine(cursorX, cursorY + 1, cursorX, cursorY + 5);
            }
        } // if there's an SVG props file available.
    }
}

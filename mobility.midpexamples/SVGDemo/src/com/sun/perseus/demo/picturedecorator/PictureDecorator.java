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
import java.io.InputStream;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

// JSR 226 packages
import javax.microedition.m2g.SVGImage;
import javax.microedition.midlet.MIDlet;

// Demo packages
import com.sun.perseus.demo.SplashCanvas;


/**
 * The PictureDecorator application is a small example of how SVG images can be
 * layered on top of photo images (i.e., images taken with a camera). The image
 * and the SVG overlays could be bundled and sent to another phone, given the
 * proper messaging capabilities of a phone.
 * <p>
 * Note that this example creates a single photo frame. If the user interface
 * were enhanced, the user could be maintaining several photoframes in a small
 * library.
 */
public class PictureDecorator extends MIDlet implements CommandListener {
    /**
     * Typically thrown if the expected SVG file cannot be loaded from
     * the jar file.
     */
    private static final String ERROR_COULD_NOT_LOAD_SVG =
        "ERROR: Could not load SVG resource from: ";

   /**
    * Number of pictures in PictureDecorator.
    */       
    private static int MAX_PHOTO_NUMBER = 0;
    /**
     * The svg image that holds overlay pictures.
     */
    private static final String SVG_IMAGE = "/svg/DemoProps.svg";

    /**
     * The optional splash image to show while the resources for the MIDlet are
     * loading.
     */
    private static final String SPLASH_IMAGE = "/images/PictureDecorHelp.png";

    /**
     * Command to remove a prop from the SVG image.
     */
    private Command CMD_REMOVE = new Command("Remove Prop", Command.SCREEN, 100);

    /**
     * Command to show the help/splash screen.
     */
    private Command CMD_HELP = new Command("Help", Command.SCREEN, 100);

    /**
     * Command to exit the application.
     */
    private Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);

    /**
     * The current display.
     */
    Display display;

    /**
     * The current photo frame.
     */
    private PhotoFrame frame;

    /**
     * The splash Canvas. May be null.
     */
    protected SplashCanvas splashCanvas;

    /**
     * Constructs a new PictureDecorator application.
     */
    public PictureDecorator() {
        display = Display.getDisplay(this);

        String count = getAppProperty("Attribute-photocount");
        MAX_PHOTO_NUMBER = Integer.parseInt(count);
        
        // Create the initial photo frame and place a default photo in it.
        frame = new PhotoFrame(this);
        frame.addCommand(CMD_REMOVE);
        frame.addCommand(CMD_EXIT);
        frame.addCommand(CMD_HELP);
        frame.setCommandListener(this);

        // Show the splash screen.
        frame.setPhoto(1);
    }

    /**
     * Returns last picture index.
     */          
    int getMaxPhotoNumber() {
        return MAX_PHOTO_NUMBER;
    }

    /**
     * Start the demo!
     */
    protected void startApp() {
        if (splashCanvas == null) {
            // First, show the splash screen.
            splashCanvas = new SplashCanvas(SPLASH_IMAGE);
            splashCanvas.display(display);
            display.setCurrent(splashCanvas);
    
            SVGImage svgImage = getSVGImage(SVG_IMAGE);
            frame.setPropsLibrary(svgImage);
    
            /*
             * Switch to the first demo photo.
             */
            frame.setPhoto(1);
    
            // Switch to frame display
            splashCanvas.switchTo(display, frame);
        }
    }

    /**
     * Pause the demo. Nothing needs to be paused in this application.
     */
    protected void pauseApp() {
    }

    /**
     * Destroy the photoframe and do some clean-up.
     */
    protected void destroyApp(boolean unconditional) {
        // Destroy the photo frame.
        frame = null;

        // Release the display.
        display.setCurrent((Displayable)null);

        // Try to clean up faster by submitting a garbage collection request.
        System.gc();
    }

    /**
     * Returns the name of the image associated with <code>name</code>.
     * @return The name of the image or <code>null</code> if a match for
     *     <code>name</code> could not be found.
     */
    public String getImageName(final String property) {
        String path = getAppProperty(property);

        int start = path.lastIndexOf('/');

        if (start < 0) {
            start = 0;
        }

        if ((start + 1) < path.length()) {
            start++;
        }

        int end = path.lastIndexOf('.');

        if (end < 0) {
            end = path.length();
        }

        // Return just the name of the image. Drop the extension.
        return path.substring(start, end);
    }

    /**
     * Returns the image associated with <code>name</code>.
     * @return The image.
     */
    public Image getImage(final String property) {
        // New image! Load it and add it and its reference to the hash table.
        Image img = null;
        String path = getAppProperty(property);

        if (path != null) {
            try {
                img = Image.createImage(path);
            } catch (IOException ioe) {
                System.out.println("Unable to create image: " + ioe.getMessage());
            }
        }

        return img;
    }

    /**
     * Returns the SVG image from the given resource name.
     * @return The SVG image.
     */
    public SVGImage getSVGImage(final String path) {
        InputStream svgStream = getClass().getResourceAsStream(path);

        SVGImage img = null;

        if (svgStream != null) {
            try {
                img = (SVGImage)SVGImage.createImage(svgStream, null);
            } catch (IOException ioe) {
                System.out.println("Unable to create image: " + ioe.getMessage());
            }
        }

        if (img == null) {
            throw new Error(ERROR_COULD_NOT_LOAD_SVG + path);
        }

        return img;
    }

    /**
     * Handle the soft-button menu commands.
     *
     * @param c The command that as issued.
     * @param d The displayable associated with the command.
     */
    public void commandAction(Command c, Displayable d) {
        if (c == CMD_EXIT) {
            destroyApp(false);
            notifyDestroyed();
        } else if (c == CMD_REMOVE) {
            // Remove the current prop from the SVG image.
            frame.removeProp();
        } else if (c == CMD_HELP) {
            splashCanvas.showAndWait(display, d);
        } else if (c == frame.showItemPicker) {
            display.setCurrent(frame.itemPicker);
        } else {
            // The user picked a prop, so show the prop.
            String id = c.getLabel();
            frame.addProp(id);
        }
    }
}

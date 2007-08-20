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
package example.bluetooth.demo;

import java.io.IOException;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;


/**
 * Allows to customize the images list to be published,
 * creates the corresponding service record to describe this list
 * and send the images to clients by request.
 *
 * @version ,
 */
final class GUIImageServer implements CommandListener {
    /** Keeps the help message of this demo. */
    private final String helpText =
        "The server is started by default.\n\n" +
        "No images are published initially. Change this by corresponding" +
        " commands - the changes have an effect immediately.\n\n" +
        "If image is removed from the published list, it can't " + "be downloaded.";

    /** This command goes to demo main screen. */
    private final Command backCommand = new Command("Back", Command.BACK, 2);

    /** Adds the selected image to the published list. */
    private final Command addCommand = new Command("Publish image", Command.SCREEN, 1);

    /** Removes the selected image from the published list. */
    private final Command removeCommand = new Command("Remove image", Command.SCREEN, 1);

    /** Shows the help message. */
    private final Command helpCommand = new Command("Help", Command.HELP, 1);

    /** The list control to configure images. */
    private final List imagesList = new List("Configure Server", List.IMPLICIT);

    /** The help screen for the server. */
    private final Alert helpScreen = new Alert("Help");

    /** Keeps the parent MIDlet reference to process specific actions. */
    private DemoMIDlet parent;

    /** The list of images file names. */
    private Vector imagesNames;

    /** These images are used to indicate the picture is published. */
    private Image onImage;

    /** These images are used to indicate the picture is published. */
    private Image offImage;

    /** Keeps an information about what images are published. */
    private boolean[] published;

    /** This object handles the real transmission. */
    private BTImageServer bt_server;

    /** Constructs images server GUI. */
    GUIImageServer(DemoMIDlet parent) {
        this.parent = parent;
        bt_server = new BTImageServer(this);
        setupIdicatorImage();
        setupImageList();
        published = new boolean[imagesList.size()];

        // prepare main screen
        imagesList.addCommand(backCommand);
        imagesList.addCommand(addCommand);
        imagesList.addCommand(removeCommand);
        imagesList.addCommand(helpCommand);
        imagesList.setCommandListener(this);

        // prepare help screen
        helpScreen.addCommand(backCommand);
        helpScreen.setTimeout(Alert.FOREVER);
        helpScreen.setString(helpText);
        helpScreen.setCommandListener(this);
    }

    /**
     * Process the command event.
     *
     * @param c - the issued command.
     * @param d - the screen object the command was issued for.
     */
    public void commandAction(Command c, Displayable d) {
        if ((c == backCommand) && (d == imagesList)) {
            destroy();
            parent.show();

            return;
        }

        if ((c == backCommand) && (d == helpScreen)) {
            Display.getDisplay(parent).setCurrent(imagesList);

            return;
        }

        if (c == helpCommand) {
            Display.getDisplay(parent).setCurrent(helpScreen);

            return;
        }

        /*
         * Changing the state of base of published images
         */
        int index = imagesList.getSelectedIndex();

        // nothing to do
        if ((c == addCommand) == published[index]) {
            return;
        }

        // update information and view
        published[index] = c == addCommand;

        Image stateImg = (c == addCommand) ? onImage : offImage;
        imagesList.set(index, imagesList.getString(index), stateImg);

        // update bluetooth service information
        if (!bt_server.changeImageInfo(imagesList.getString(index), published[index])) {
            // either a bad record or SDDB is busy
            Alert al = new Alert("Error", "Can't update base", null, AlertType.ERROR);
            al.setTimeout(DemoMIDlet.ALERT_TIMEOUT);
            Display.getDisplay(parent).setCurrent(al, imagesList);

            // restore internal information
            published[index] = !published[index];
            stateImg = published[index] ? onImage : offImage;
            imagesList.set(index, imagesList.getString(index), stateImg);
        }
    }

    /**
     * We have to provide this method due to "do not do network
     * operation in command listener method" restriction, which
     * is caused by crooked midp design.
     *
     * This method is called by BTImageServer after it is done
     * with bluetooth initialization and next screen is ready
     * to appear.
     */
    void completeInitialization(boolean isBTReady) {
        // bluetooth was initialized successfully.
        if (isBTReady) {
            Ticker t = new Ticker("Choose images you want to publish...");
            imagesList.setTicker(t);
            Display.getDisplay(parent).setCurrent(imagesList);

            return;
        }

        // something wrong
        Alert al = new Alert("Error", "Can't initialize bluetooth", null, AlertType.ERROR);
        al.setTimeout(DemoMIDlet.ALERT_TIMEOUT);
        Display.getDisplay(parent).setCurrent(al, parent.getDisplayable());
    }

    /** Destroys this component. */
    void destroy() {
        // finalize the image server work
        bt_server.destroy();
    }

    /** Gets the image file name from its title (label). */
    String getImageFileName(String imgName) {
        if (imgName == null) {
            return null;
        }

        // no interface in List to get the index - should find
        int index = -1;

        for (int i = 0; i < imagesList.size(); i++) {
            if (imagesList.getString(i).equals(imgName)) {
                index = i;

                break;
            }
        }

        // not found or not published
        if ((index == -1) || !published[index]) {
            return null;
        }

        return (String)imagesNames.elementAt(index);
    }

    /**
     * Creates the image to indicate the base state.
     */
    private void setupIdicatorImage() {
        // create "on" image
        try {
            onImage = Image.createImage("/images/st-on.png");
        } catch (IOException e) {
            // provide off-screen image then
            onImage = createIndicatorImage(12, 12, 0, 255, 0);
        }

        // create "off" image
        try {
            offImage = Image.createImage("/images/st-off.png");
        } catch (IOException e) {
            // provide off-screen image then
            offImage = createIndicatorImage(12, 12, 255, 0, 0);
        }
    }

    /**
     * Gets the description of images from manifest and
     * prepares the list to control the configuration.
     * <p>
     * The attributes are named "ImageTitle-n" and "ImageImage-n".
     * The value "n" must start at "1" and be incremented by 1.
     */
    private void setupImageList() {
        imagesNames = new Vector();
        imagesList.setCommandListener(this);

        for (int n = 1; n < 100; n++) {
            String name = parent.getAppProperty("ImageName-" + n);

            // no more images available
            if ((name == null) || (name.length() == 0)) {
                break;
            }

            String label = parent.getAppProperty("ImageTitle-" + n);

            // no label available - use picture name instead
            if ((label == null) || (label.length() == 0)) {
                label = name;
            }

            imagesNames.addElement(name);
            imagesList.append(label, offImage);
        }
    }

    /**
     * Creates the off-screen image with specified size an color.
     */
    private Image createIndicatorImage(int w, int h, int r, int g, int b) {
        Image res = Image.createImage(w, h);
        Graphics gc = res.getGraphics();
        gc.setColor(r, g, b);
        gc.fillRect(0, 0, w, h);

        return res;
    }
} // end of class 'GUIImageServer' definition

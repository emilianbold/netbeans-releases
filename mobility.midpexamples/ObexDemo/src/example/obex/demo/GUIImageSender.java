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
package example.obex.demo;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;


final class GUIImageSender implements CommandListener {
    private static final int TIMEOUT = 5000;
    private static final int IMAGE_COUNT = 100;

    /** Keeps a reference to a parent MIDlet. */
    private ObexDemoMIDlet parent;

    /** Soft button for stopping download and returning to the mane screen. */
    private Command stopCommand = new Command("Stop", Command.BACK, 1);

    /** Soft button for returning to the mane screen of demo. */
    private Command backCommand = new Command("Back", Command.STOP, 1);

    /** Soft button for launching an image sending. */
    private Command sendCommand = new Command("Send", Command.ITEM, 1);

    /** Container of images file names */
    private Vector imageNames;

    /** List of images titles to select a sending image */
    private List imageList;

    /** Form to show current state of sending a picture */
    private Form senderForm = new Form("Send Image");

    /** Gauge to indicate progress of downloading process */
    private Gauge sendingItem = new Gauge("", false, 256, 0);

    /** Reference to Obex sender part */
    private ObexImageSender obexSender;

    /** Constructor initialize image list and sender form */
    GUIImageSender(ObexDemoMIDlet parent) {
        this.parent = parent;
        setupImageList();
        imageList.addCommand(sendCommand);
        imageList.addCommand(backCommand);
        imageList.setCommandListener(this);
        senderForm.append(sendingItem);
        senderForm.addCommand(stopCommand);
        senderForm.setCommandListener(this);
        showImageList();
    }

    /**
     * Responds to commands issued on "client or server" form.
     *
     * @param c command object source of action
     * @param d screen object containing the item the action was performed on
     */
    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            parent.show();
        } else if (c == stopCommand) {
            if (obexSender != null) {
                obexSender.stop();
            }

            showImageList();
        } else if ((c == sendCommand) || (c == List.SELECT_COMMAND)) {
            obexSender = new ObexImageSender(this);
            sendImage();
        } else if (c == Alert.DISMISS_COMMAND) {
            showImageList();
        }
    }

    /**
     * Shows progress of image uploading
     */
    void showProgress(String label, int maxValue) {
        sendingItem.setLabel(label);
        sendingItem.setValue(0);
        sendingItem.setMaxValue(maxValue);
        Display.getDisplay(parent).setCurrent(senderForm);
    }

    /**
     * Update progress of image uploading
     */
    void updateProgress(int value) {
        sendingItem.setValue(value);
    }

    /**
     * Shows list with image names to select one  for sending to receiver
     */
    void showImageList() {
        Display.getDisplay(parent).setCurrent(imageList);
    }

    /**
     * Shows alert with error message
     */
    void errorMessage() {
        Alert alert = new Alert("Error", "Can't read the image", null, null);
        alert.setTimeout(TIMEOUT);
        alert.setCommandListener(this);
        Display.getDisplay(parent).setCurrent(alert, imageList);
    }

    /**
     * Shows alert with "not ready" message
     */
    void notReadyMessage() {
        Alert alert =
            new Alert("Warning", "Receiver isn't ready" + " to download image", null, null);
        alert.setTimeout(TIMEOUT);
        alert.setCommandListener(this);
        Display.getDisplay(parent).setCurrent(alert, imageList);
    }

    /**
     * Shows alert with "stop" message
     */
    void stopMessage() {
        Alert alert = new Alert("Warning", "Receiver terminated" + " image loading", null, null);
        alert.setTimeout(TIMEOUT);
        alert.setCommandListener(this);
        Display.getDisplay(parent).setCurrent(alert, imageList);
    }

    /** Stops Uploading process */
    void stop() {
        if (obexSender != null) {
            obexSender.stop();
        }
    }

    /** Starts sending process */
    private void sendImage() {
        int imageIndex = imageList.getSelectedIndex();
        String imageName = (String)imageNames.elementAt(imageIndex);
        obexSender.setImageName(imageName);
        (new Thread(obexSender)).start();
    }

    /**
     * Check the attributes in the descriptor that identify
     * images and titles and initialize the lists of imageNames
     * and imageList.
     * <P>
     * The attributes are named "ImageTitle-n" and "ImageImage-n".
     * The value "n" must start at "1" and increment by 1.
     */
    private void setupImageList() {
        imageNames = new Vector();
        imageList = new List("Select Image to send", List.IMPLICIT);
        imageList.addCommand(backCommand);
        imageList.setCommandListener(this);

        for (int n = 1; n < IMAGE_COUNT; n++) {
            String nthImage = "ImageName-" + n;
            String image = parent.getAppProperty(nthImage);

            if ((image == null) || (image.length() == 0)) {
                break;
            }

            String nthTitle = "ImageTitle-" + n;
            String title = parent.getAppProperty(nthTitle);

            if ((title == null) || (title.length() == 0)) {
                title = image;
            }

            imageNames.addElement(image);
            imageList.append(title, null);
        }
    }
}

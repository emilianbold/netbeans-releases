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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;


/**
 * @version
 */
final class GUIImageReceiver implements CommandListener {
    /** Shows if debug prints should be done. */
    private static final boolean DEBUG = false;

    /** DEBUG only: keeps the class name for debug. */
    private static final String cn = "GUIImageReceiver";

    /** Keeps a reference to a parent MIDlet. */
    private ObexDemoMIDlet parent;

    /** Soft button for stopping download and returning to the mane screen. */
    private Command stopCommand = new Command("Stop", Command.BACK, 1);

    /** Soft button for returning to the main screen of demo. */
    private Command backCommand = new Command("Back", Command.STOP, 1);

    /** Soft button for confirm Image downloading. */
    private Command confirmCommand = new Command("Yes", Command.OK, 1);

    /** Soft button for reject Image downloading. */
    private Command rejectCommand = new Command("No", Command.CANCEL, 1);

    /** StringItem shows string about connection waiting state. */
    private StringItem waitingItem = new StringItem("Waiting for connection", "");

    /** ImageItem shows image on the screen */
    private ImageItem imageItem = new ImageItem("Image ...", null, Item.LAYOUT_CENTER, null);

    /** Gauge to indicate progress of downloading process. */
    private Gauge downloadingItem = new Gauge("Downloading Image ...", false, 256, 0);

    /** Form to show current state of receiver and to show a picture. */
    private Form receiverForm = new Form("Receive Image");

    /** synchronization object to prevent downloading during permission alert.*/
    private Object alertLock = new Object();

    /** Indicate that user choose to download image or not. */
    private boolean download = false;

    /** Indicate that user has made a choose. */
    private boolean choose = false;

    /** Reference to Obex receiver part */
    private ObexImageReceiver obexReceiver = null;

    /** Constructor initialize receiverForm to show states of receiving. */
    GUIImageReceiver(ObexDemoMIDlet parent) {
        this.parent = parent;
        obexReceiver = new ObexImageReceiver(this);
        waitingItem.setLayout(Item.LAYOUT_CENTER);
        receiverForm.append(waitingItem);
        receiverForm.addCommand(backCommand);
        receiverForm.setCommandListener(this);
        Display.getDisplay(parent).setCurrent(receiverForm);
        (new Thread(obexReceiver)).start();
    }

    /**
     * Responds to commands used on this displayable.
     *
     * @param c command object source of action.
     * @param d screen object containing the item the action was performed on.
     */
    public void commandAction(Command c, Displayable d) {
        if (c == stopCommand) {
            obexReceiver.stop(false);

            return;
        } else if (c == backCommand) {
            obexReceiver.stop(true);
            parent.show();

            return;
        } else if (c == confirmCommand) {
            synchronized (alertLock) {
                download = true;
                choose = true;
                alertLock.notify();
            }
        } else if (c == rejectCommand) {
            synchronized (alertLock) {
                download = false;
                choose = true;
                alertLock.notify();
            }
        }
    }

    /** Ascs user to download image. */
    boolean askPermission(String imageName, int imageLength) {
        boolean download = false;

        // ask permission to download image
        Alert alert =
            new Alert("Connected!",
                "Incoming image:\nName   = " + imageName + "\nLength = " + imageLength +
                "\nWould you like to receive it ?", null, null);
        alert.setTimeout(Alert.FOREVER);
        alert.addCommand(confirmCommand);
        alert.addCommand(rejectCommand);
        alert.setCommandListener(this);
        Display.getDisplay(parent).setCurrent(alert);

        synchronized (alertLock) {
            if (choose == false) {
                try {
                    alertLock.wait();
                } catch (InterruptedException ie) {
                    // don't wait then
                }
            }

            choose = false;
            download = this.download;
        }

        Display.getDisplay(parent).setCurrent(receiverForm);

        return download;
    }

    /**
     * Shows string with waiting for connection message.
     */
    void showWaiting() {
        receiverForm.set(0, waitingItem);
        receiverForm.removeCommand(stopCommand);
        receiverForm.addCommand(backCommand);
        downloadingItem.setValue(0);
    }

    /**
     * Shows progress bar of image downloading
     */
    void showProgress(int maxValue) {
        downloadingItem.setValue(0);
        downloadingItem.setMaxValue(maxValue);
        receiverForm.set(0, downloadingItem);
        receiverForm.removeCommand(backCommand);
        receiverForm.addCommand(stopCommand);
    }

    /**
     * Update progress of image downloading
     */
    void updateProgress(int value) {
        downloadingItem.setValue(value);
    }

    /**
     * Shows downloaded image.
     */
    void showImage(byte[] imageData) {
        Image image = Image.createImage(imageData, 0, imageData.length);
        imageItem.setImage(image);
        receiverForm.set(0, imageItem);
        receiverForm.removeCommand(stopCommand);
        receiverForm.addCommand(backCommand);
    }

    /**
     * Shows alert with "not ready" message.
     */
    void canNotConnectMessage() {
        Alert alert = new Alert("Warning", "Can not connect to any sender", null, null);
        alert.setTimeout(5000);
        Display.getDisplay(parent).setCurrent(alert, receiverForm);
    }

    /**
     * Shows alert with "stop" message.
     */
    void stopMessage() {
        Alert alert = new Alert("Warning", "Sender stopped image uploading", null, null);
        alert.setTimeout(5000);
        Display.getDisplay(parent).setCurrent(alert, receiverForm);
    }

    /**
     * Stops receiving process in OBEX part.
     */
    void stop() {
        obexReceiver.stop(true);
    }
}

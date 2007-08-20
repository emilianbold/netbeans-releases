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
package example.mms;

import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import javax.wireless.messaging.*;


/**
 * An example MIDlet to send text via an MMS MessageConnection
 */
public class MMSSend extends MIDlet implements CommandListener {
    /** user interface command for indicating Exit request. */
    private static Command CMD_EXIT = new Command("Exit", Command.EXIT, 2);

    /** user interface command for sending the message */
    private static Command CMD_SEND = new Command("Send", Command.ITEM, 1);

    /** user interface command for adding message's part */
    private static Command CMD_ADD_PART = new Command("Add Part", Command.ITEM, 1);

    /** current display. */
    private Display display;

    /** The application-ID on which we send MMS messages */
    private String appID;

    /** Area where the user enters the subject of the message */
    private TextField subjectField;

    /** Area where the user enters the phone number to send the message to */
    private TextField destinationField;

    /** Area where the user enters the phone number to send the message to */
    private StringItem partsLabel;

    /** Error message displayed when an invalid phone number is entered */
    private Alert errorMessageAlert;

    /** Alert that is displayed when a message is being sent */
    private Alert sendingMessageAlert;

    /** The last visible screen when we paused */
    private Displayable resumeScreen = null;
    private MMSMessage message;
    private PartsDialog partsDialog;

    /**
     * Initialize the MIDlet with the current display object and
     * graphical components.
     */
    public MMSSend() {
        appID = getAppProperty("MMS-ApplicationID");

        display = Display.getDisplay(this);

        Form mainForm = new Form("New MMS");

        subjectField = new TextField("Subject:", null, 256, TextField.ANY);
        mainForm.append(subjectField);

        destinationField = new TextField("Destination Address: ", "mms://", 256, TextField.ANY);
        mainForm.append(destinationField);

        partsLabel = new StringItem("Parts:", "0");
        mainForm.append(partsLabel);

        mainForm.addCommand(CMD_EXIT);
        mainForm.addCommand(CMD_SEND);
        mainForm.addCommand(CMD_ADD_PART);
        mainForm.setCommandListener(this);

        errorMessageAlert = new Alert("MMS", null, null, AlertType.ERROR);
        errorMessageAlert.setTimeout(5000);

        sendingMessageAlert = new Alert("MMS", null, null, AlertType.INFO);
        sendingMessageAlert.setTimeout(5000);
        sendingMessageAlert.setCommandListener(this);

        resumeScreen = mainForm;

        message = new MMSMessage();
    }

    /**
     * startApp should return immediately to keep the dispatcher
     * from hanging.
     */
    public void startApp() {
        display.setCurrent(resumeScreen);
    }

    /**
     * Remember what screen is showing
     */
    public void pauseApp() {
        resumeScreen = display.getCurrent();
    }

    /**
     * Destroy must cleanup everything.
     * @param unconditional true if a forced shutdown was requested
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Respond to commands, including exit
     * @param c user interface command requested
     * @param s screen object initiating the request
     */
    public void commandAction(Command c, Displayable s) {
        try {
            if ((c == CMD_EXIT) || (c == Alert.DISMISS_COMMAND)) {
                destroyApp(false);
                notifyDestroyed();
            } else if (c == CMD_ADD_PART) {
                if (partsDialog == null) {
                    partsDialog = new PartsDialog(this);
                }

                partsDialog.show();
            } else if (c == CMD_SEND) {
                promptAndSend();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void show() {
        partsLabel.setText(Integer.toString(partsDialog.counter));
        display.setCurrent(resumeScreen);
    }

    Display getDisplay() {
        return display;
    }

    MMSMessage getMessage() {
        return message;
    }

    /**
     * Prompt for and send the message
     */
    private void promptAndSend() {
        try {
            String address = destinationField.getString();
            message.setSubject(subjectField.getString());
            message.setDestination(address);

            String statusMessage = "Sending message to " + address + "...";
            sendingMessageAlert.setString(statusMessage);

            new SenderThread(message, appID).start();
        } catch (IllegalArgumentException iae) {
            errorMessageAlert.setString(iae.getMessage());
            display.setCurrent(errorMessageAlert);
        }
    }
}

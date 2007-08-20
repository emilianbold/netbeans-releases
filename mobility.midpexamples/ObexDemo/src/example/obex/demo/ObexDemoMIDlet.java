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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;


/**
 * @version
 */
public final class ObexDemoMIDlet extends MIDlet implements CommandListener {
    /** A list of menu items */
    private static final String[] elements = { "Send Image", "Receive Image" };

    /** Soft button for exiting the demo. */
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);

    /** Soft button for launching a sender or receiver of images . */
    private Command startCommand = new Command("Start", Command.ITEM, 1);

    /** A menu list instance */
    private final List menuList = new List("OBEX Demo", List.IMPLICIT, elements, null);

    /** A GUI part of OBEX client which send image to server */
    private GUIImageSender imageSender = null;

    /** A GUI part of OBEX server which receive image from client */
    private GUIImageReceiver imageReceiver = null;

    /** Shows that demo was paused */
    private boolean isPaused;

    public ObexDemoMIDlet() {
        menuList.setCommandListener(this);
        menuList.addCommand(exitCommand);
        menuList.addCommand(startCommand);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void startApp() {
        isPaused = false;
        show();
    }

    public void pauseApp() {
        isPaused = true;
    }

    public void destroyApp(boolean unconditional) {
        if (imageReceiver != null) {
            imageReceiver.stop();
        }

        if (imageSender != null) {
            imageSender.stop();
        }
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } else if ((c == startCommand) || (c == List.SELECT_COMMAND)) {
            switch (menuList.getSelectedIndex()) {
            case 0:
                imageSender = new GUIImageSender(this);

                break;

            case 1:
                imageReceiver = new GUIImageReceiver(this);

                break;

            default:
                System.err.println("Unexpected choice...");

                break;
            }
        }
    }

    /**
     * Shows main menu of MIDlet on the screen.
     */
    void show() {
        Display.getDisplay(this).setCurrent(menuList);
    }
}

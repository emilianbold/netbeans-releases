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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;


/**
 * Contains the Bluetooth API demo, that allows to download
 * the specific images from the other devices.
 *
 * @version ,
 */
public final class DemoMIDlet extends MIDlet implements CommandListener {
    /** The messages are shown in this demo this amount of time. */
    static final int ALERT_TIMEOUT = 2000;

    /** A list of menu items */
    private static final String[] elements = { "Server", "Client" };

    /** Soft button for exiting the demo. */
    private final Command EXIT_CMD = new Command("Exit", Command.EXIT, 2);

    /** Soft button for launching a client or sever. */
    private final Command OK_CMD = new Command("Ok", Command.SCREEN, 1);

    /** A menu list instance */
    private final List menu = new List("Bluetooth Demo", List.IMPLICIT, elements, null);

    /** A GUI part of server that publishes images. */
    private GUIImageServer imageServer;

    /** A GUI part of client that receives image from client */
    private GUIImageClient imageClient;

    /** value is true after creating the server/client */
    private boolean isInit = false;

    /**
     * Constructs main screen of the MIDlet.
     */
    public DemoMIDlet() {
        menu.addCommand(EXIT_CMD);
        menu.addCommand(OK_CMD);
        menu.setCommandListener(this);
    }

    /**
     * Creates the demo view and action buttons.
     */
    public void startApp() {
        if (!isInit) {
            show();
        }
    }

    /**
     * Destroys the application.
     */
    protected void destroyApp(boolean unconditional) {
        if (imageServer != null) {
            imageServer.destroy();
        }

        if (imageClient != null) {
            imageClient.destroy();
        }
    }

    /**
     * Does nothing. Redefinition is required by MIDlet class.
     */
    protected void pauseApp() {
    }

    /**
     * Responds to commands issued on "client or server" form.
     *
     * @param c command object source of action
     * @param d screen object containing the item action was performed on
     */
    public void commandAction(Command c, Displayable d) {
        if (c == EXIT_CMD) {
            destroyApp(true);
            notifyDestroyed();

            return;
        }

        switch (menu.getSelectedIndex()) {
        case 0:
            imageServer = new GUIImageServer(this);

            break;

        case 1:
            imageClient = new GUIImageClient(this);

            break;

        default:
            System.err.println("Unexpected choice...");

            break;
        }

        isInit = true;
    }

    /** Shows main menu of MIDlet on the screen. */
    void show() {
        Display.getDisplay(this).setCurrent(menu);
    }

    /**
     * Returns the displayable object of this screen -
     * it is required for Alert construction for the error
     * cases.
     */
    Displayable getDisplayable() {
        return menu;
    }
} // end of class 'DemoMIDlet' definition

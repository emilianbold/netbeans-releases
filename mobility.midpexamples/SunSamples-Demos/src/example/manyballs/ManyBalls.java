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
package example.manyballs;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class ManyBalls extends MIDlet implements CommandListener {
    Display display;
    ManyCanvas canvas; // The main screen
    private Command exitCommand = new Command("Exit", Command.EXIT, 99);
    private Command toggleCommand = new Command("Stop/Go", Command.SCREEN, 1);
    private Command helpCommand = new Command("Help", Command.HELP, 2);
    private Form helpScreen;

    // the GUI buttons
    //	Button exitButton, clearButton, moreButton, lessButton;

    /*
     * Create the canvas
     */
    public ManyBalls() {
        display = Display.getDisplay(this);

        canvas = new ManyCanvas(display, 40);
        canvas.addCommand(exitCommand);
        canvas.addCommand(toggleCommand);
        canvas.addCommand(helpCommand);
        canvas.setCommandListener(this);
    }

    public void startApp() throws MIDletStateChangeException {
        canvas.start();
    }

    public void pauseApp() {
        canvas.pause();
    }

    public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        canvas.destroy();
    }

    /*
     * Respond to a command issued on the Canvas.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == toggleCommand) {
            if (canvas.isPaused()) {
                canvas.start();
            } else {
                canvas.pause();
            }
        } else if (c == helpCommand) {
            canvas.pause();
            showHelp();
        } else if (c == exitCommand) {
            try {
                destroyApp(false);
                notifyDestroyed();
            } catch (MIDletStateChangeException ex) {
            }
        } 
    }

    /*
     * Put up the help screen. Create it if necessary.
     * Add only the Resume command.
     */
    void showHelp() {
        if (helpScreen == null) {
            helpScreen = new Form("Many Balls Help");
            helpScreen.append("^ = faster\n");
            helpScreen.append("v = slower\n");
            helpScreen.append("< = fewer\n");
            helpScreen.append("> = more\n");
        }

        helpScreen.addCommand(toggleCommand);
        helpScreen.setCommandListener(this);
        display.setCurrent(helpScreen);
    }
}

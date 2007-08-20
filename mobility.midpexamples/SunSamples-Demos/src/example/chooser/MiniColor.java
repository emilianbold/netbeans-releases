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
package example.chooser;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


/**
 * Create a single color chooser MIDlet. It uses the MiniColorChooser
 * as its main screen.
 */
public class MiniColor extends MIDlet implements CommandListener {
    /** The display for this MIDlet */
    private Display display;

    /** The MiniColorChooser */
    private MiniColorChooser chooser;

    /** The exit command */
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);


    /**
     * Create the MiniColor MIDlet.
     * Create the mini chooser and set the commands and listener.
     */
    public MiniColor() {
        display = Display.getDisplay(this);
        chooser = new MiniColorChooser();

        chooser.addCommand(exitCommand);
        chooser.setCommandListener(this);

        chooser.setColor(0xffff00);
    }

    /**
     * Create the MiniColor and make it current
     */
    public void startApp() {
        display.setCurrent(chooser);
    }

    /**
     * Pause
     */
    public void pauseApp() {
    }

    /**
     * Destroy must cleanup everything.
     * @param unconditional true if the MIDlet must destroy exit
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Respond to a commands issued on any Screen
     * @param c Command invoked
     * @param s Displayable on which the command was invoked
     */
    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } 
    }
}

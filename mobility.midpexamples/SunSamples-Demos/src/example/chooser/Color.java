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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;


/**
 * A Color chooser MIDlet.
 */
public class Color extends MIDlet implements CommandListener {
    /** This MIDlets Display object */
    private Display display; // Our display

    /** The Color chooser */
    private ColorChooser chooser;

    /** The Exit Command */
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);

    /** The Decimal Command */
    private Command decimalCommand = new Command("Decimal", Command.SCREEN, 7);

    /** The Hexadecimal Command */
    private Command hexCommand = new Command("Hexadecimal", Command.SCREEN, 7);

    /** The Coarse command */
    private Command coarseCommand = new Command("Coarse", Command.SCREEN, 8);

    /** The Fine command */
    private Command fineCommand = new Command("Fine", Command.SCREEN, 8);


    /**
     * Construct a new Color MIDlet and initialize.
     */
    public Color() {
        display = Display.getDisplay(this);
        chooser = new ColorChooser(display.isColor());

        chooser.addCommand(exitCommand);
        chooser.addCommand(hexCommand);
        chooser.addCommand(fineCommand);
        chooser.setCommandListener(this);

        chooser.setColor(0xffff00);
    }

    /**
     * Create the ColorChooser and make it current
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
     * @param unconditional true if must destroy
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Respond to a commands issued on any Screen.
     * @param c Command invoked
     * @param s Displayable on which the command was invoked
     */
    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } else if (c == decimalCommand) {
            chooser.setRadix(10);
            chooser.removeCommand(decimalCommand);
            chooser.addCommand(hexCommand);
        } else if (c == hexCommand) {
            chooser.setRadix(16);
            chooser.removeCommand(hexCommand);
            chooser.addCommand(decimalCommand);
        } else if (c == fineCommand) {
            chooser.setDelta(4);
            chooser.removeCommand(fineCommand);
            chooser.addCommand(coarseCommand);
        } else if (c == coarseCommand) {
            chooser.setDelta(32);
            chooser.removeCommand(coarseCommand);
            chooser.addCommand(fineCommand);
        } 
    }
}

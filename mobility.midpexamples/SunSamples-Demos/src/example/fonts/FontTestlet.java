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
package example.fonts;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

//import java.io.*;
//import javax.microedition.midlet.*;
//import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


/**
 * FontTestlet is simple MIDlet which attempts to display
 * text in all of the MIDP's different fonts.
 */
public class FontTestlet extends MIDlet implements CommandListener {
    private Display myDisplay;
    private FontCanvas myCanvas;
    private int currentFace = Font.FACE_SYSTEM;
    private Command monospaceCommand = new Command("monospace", Command.ITEM, 1);
    private Command proportionalCommand = new Command("proportional", Command.ITEM, 1);
    private Command systemCommand = new Command("system", Command.ITEM, 1);
    private Command exit = new Command("Exit", Command.EXIT, 3);

    /**
     * FontTestlet - default constructor
     */
    public FontTestlet() {
        super();

        // Set up the user interface
        myDisplay = Display.getDisplay(this);
        myCanvas = new FontCanvas(this); // pointer to myself
        myCanvas.setCommandListener(this);
        myCanvas.addCommand(monospaceCommand);
        myCanvas.addCommand(proportionalCommand);
        myCanvas.addCommand(exit);
    }

    /**
     * initApp()
     */
    public void init() throws MIDletStateChangeException {
    }

    /**
     * startApp()
     */
    public void startApp() throws MIDletStateChangeException {
        myDisplay.setCurrent(myCanvas);
    }

    /**
     * pauseApp()
     */
    public void pauseApp() {
        // System.out.println("pauseApp()");
    }

    /**
     * destryApp()
     *
     * This is important.  It closes the app's RecordStore
     * @param cond true if this is an unconditional destroy
     *             false if it is not
     *             currently ignored and treated as true
     */
    public void destroyApp(boolean cond) {
        myDisplay.setCurrent((Displayable)null);
        myCanvas.destroy();
        notifyDestroyed();
    }

    /**
     * draw some stuff to the graphics context
     */
    public void paint(Graphics g) {
        String title;
        int height = 0;

        g.setColor(0x00000000);
        g.fillRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());

        g.setColor(0x00ffffff);

        switch (currentFace) {
        case Font.FACE_SYSTEM:
            title = "System";

            break;

        case Font.FACE_PROPORTIONAL:
            title = "Proportional";

            break;

        case Font.FACE_MONOSPACE:
            title = "Monospaced";

            break;

        default:
            title = "unknown";

            break;
        }

        g.drawString(title, 0, 0, Graphics.TOP | Graphics.LEFT);
        height += g.getFont().getHeight();

        g.setFont(Font.getFont(currentFace, Font.STYLE_PLAIN, Font.SIZE_LARGE));
        g.drawString("Regular plain", 0, height, Graphics.TOP | Graphics.LEFT);
        height += g.getFont().getHeight();

        g.setFont(Font.getFont(currentFace, Font.STYLE_ITALIC, Font.SIZE_LARGE));
        g.drawString("Regular ital", 0, height, Graphics.TOP | Graphics.LEFT);
        height += g.getFont().getHeight();

        g.setFont(Font.getFont(currentFace, Font.STYLE_BOLD, Font.SIZE_LARGE));
        g.drawString("Bold plain", 0, height, Graphics.TOP | Graphics.LEFT);
        height += g.getFont().getHeight();

        g.setFont(Font.getFont(currentFace, Font.STYLE_BOLD | Font.STYLE_ITALIC, Font.SIZE_LARGE));
        g.drawString("Bold ital", 0, height, Graphics.TOP | Graphics.LEFT);
    }

    Command getCurrentCommand() {
        switch (currentFace) {
        case Font.FACE_MONOSPACE:
            return monospaceCommand;

        case Font.FACE_PROPORTIONAL:
            return proportionalCommand;

        case Font.FACE_SYSTEM:default:
            return systemCommand;
        }
    }

    public void commandAction(Command cmd, Displayable disp) {
        myCanvas.addCommand(getCurrentCommand());

        if (cmd == monospaceCommand) {
            myCanvas.removeCommand(monospaceCommand);
            currentFace = Font.FACE_MONOSPACE;
        } else if (cmd == proportionalCommand) {
            myCanvas.removeCommand(proportionalCommand);
            currentFace = Font.FACE_PROPORTIONAL;
        } else if (cmd == systemCommand) {
            myCanvas.removeCommand(systemCommand);
            currentFace = Font.FACE_SYSTEM;
        } else if (cmd == exit) {
            destroyApp(true);
        }

        myCanvas.repaint();
    }
}

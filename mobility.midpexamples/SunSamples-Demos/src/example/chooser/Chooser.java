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


public class Chooser extends MIDlet implements CommandListener {
    private Display display; // Our display
    private FontChooser fonts;
    private TextSample sample;
    private ColorChooser colors;
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private Command textColorCommand = new Command("Text Color", Command.SCREEN, 3);
    private Command backgroundColorCommand = new Command("Background Color", Command.SCREEN, 4);
    private Command fontsCommand = new Command("Fonts", Command.SCREEN, 11);
    private Command okCommand = new Command("Ok", Command.SCREEN, 2);
    private Command okFgCommand = new Command("Ok", Command.SCREEN, 2);
    private Command okBgCommand = new Command("Ok", Command.SCREEN, 2);



    public Chooser() {
        display = Display.getDisplay(this);
        sample = new TextSample();

        sample.addCommand(exitCommand);
        sample.addCommand(textColorCommand);
        sample.addCommand(backgroundColorCommand);
        sample.addCommand(fontsCommand);
        sample.setCommandListener(this);
    }

    /**
     * Create the FontChooser and make it current
     */
    public void startApp() {
        display.setCurrent(sample);
    }

    /**
     * Pause
     */
    public void pauseApp() {
    }

    /**
     * Destroy must cleanup everything.
     */
    public void destroyApp(boolean unconditional) {
    }

    /*
     * Respond to a commands issued on any Screen
     */
    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } else if (c == fontsCommand) {
            if (fonts == null) {
                fonts = new FontChooser();
                fonts.setFace(sample.getFace());
                fonts.setStyle(sample.getStyle());
                fonts.setSize(sample.getSize());
                fonts.addCommand(okCommand);
                fonts.setCommandListener(this);
            }

            display.setCurrent(fonts);
        } else if (c == backgroundColorCommand) {
            if (colors == null) {
                colors = new ColorChooser(display.isColor());
                colors.setCommandListener(this);
            }

            colors.addCommand(okBgCommand);
            colors.removeCommand(okFgCommand);
            colors.setColor(sample.getBackgroundColor());
            display.setCurrent(colors);
        } else if (c == textColorCommand) {
            if (colors == null) {
                colors = new ColorChooser(display.isColor());
                colors.setCommandListener(this);
            }

            colors.addCommand(okFgCommand);
            colors.removeCommand(okBgCommand);

            colors.setColor(sample.getForegroundColor());
            display.setCurrent(colors);
        } else if (c == okCommand) {
            if (s == fonts) {
                sample.setStyle(fonts.getStyle());
                sample.setFace(fonts.getFace());
                sample.setSize(fonts.getSize());
            }

            display.setCurrent(sample);
        } else if (c == okFgCommand) {
            sample.setForegroundColor(colors.getColor());
            display.setCurrent(sample);
        } else if (c == okBgCommand) {
            sample.setBackgroundColor(colors.getColor());
            display.setCurrent(sample);
        }
    }
}

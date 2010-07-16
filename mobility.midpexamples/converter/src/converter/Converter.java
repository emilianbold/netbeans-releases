/*
 * Copyright (c) 2010, Oracle.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Oracle nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Converter.java
 */
package converter;

import javax.microedition.lcdui.*;

/**
 *
 */
public class Converter extends Form implements CommandListener, ItemStateListener {
    
    private ConverterMIDlet midlet;
    
    private int[] translate;
    
    /**
     * constructor
     */
    public Converter(ConverterMIDlet midlet) {
        super("Currency Converter");
        this.midlet = midlet;
        this.translate = new int[midlet.currencies.length];
        int current = 0;
        for (int i=0; i<translate.length; i++) {
            if (midlet.selected[i]) {
                translate[current++] = i;
                append(new TextField(midlet.currencies[i], "", 12, TextField.NUMERIC));
            }
        }
        try {
            // Set up this form to listen to command events
            setCommandListener(this);
            // Set up this form to listen to changes in the internal state of its interactive items
            setItemStateListener(this);
            // Add the Curreencies command
            addCommand(new Command("Currencies", Command.OK, 1));
            // Add the Exit command
            addCommand(new Command("Exit", Command.EXIT, 1));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Called when user action should be handled
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command.getCommandType() == Command.EXIT) {
            midlet.destroyApp(true);
        } else if (command.getCommandType() == Command.OK) {
            midlet.showSettings();
        }
    }
    
    /**
     * Called when internal state of any item changed
     */
    public void itemStateChanged(Item item) {
        try {
            long value = Long.parseLong(((TextField)item).getString());
            int from = 0;
            while (get(from) != item) from++;
            from = translate[from];
            for (int i=0; i<size(); i++) {
                int to = translate[i];
                if (from != to) {
                    ((TextField)get(i)).setString(String.valueOf(midlet.convert(value, from, to)));
                }
            }
        } catch (NumberFormatException nfe) {
            for (int i=0; i<size(); i++) {
                ((TextField)get(i)).setString("");
            }
        }
    }
}

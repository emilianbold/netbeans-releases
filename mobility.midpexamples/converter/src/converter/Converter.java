/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

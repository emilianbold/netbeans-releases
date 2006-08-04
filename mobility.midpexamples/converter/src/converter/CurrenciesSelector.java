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
 * CurrenciesSelector.java
 */
package converter;

import javax.microedition.lcdui.*;

/**
 *
 */
public class CurrenciesSelector extends List implements CommandListener {
    
    private ConverterMIDlet midlet;
    
    /**
     * constructor
     */
    public CurrenciesSelector(ConverterMIDlet midlet) {
        super("Select Currencies", List.MULTIPLE, midlet.currencies, null);
        this.midlet = midlet;
        setSelectedFlags(midlet.selected);
        try {
            // Set up this list to listen to command events
            setCommandListener(this);
            // Add the Save command
            addCommand(new Command("Save", Command.OK, 1));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Called when action should be handled
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command.getCommandType() == Command.OK) {
            getSelectedFlags(midlet.selected);
            midlet.notifySettingsChanged();
        }
    }
    
}

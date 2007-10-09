/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

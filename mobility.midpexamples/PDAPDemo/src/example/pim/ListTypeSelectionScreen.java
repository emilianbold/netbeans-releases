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
package example.pim;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.pim.PIM;


/**
 * Demonstrate the use of JSR 75 PIM APIs
 */
public class ListTypeSelectionScreen extends List implements CommandListener {
    private static final String CONTACT_TYPE = "Contact Lists";
    private static final String EVENT_TYPE = "Event Lists";
    private static final String TODO_TYPE = "To-Do Lists";
    private final Command selectCommand = new Command("Select", Command.OK, 1);
    private final Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private final PIMDemo midlet;

    public ListTypeSelectionScreen(PIMDemo midlet) {
        super("Select a list type", List.IMPLICIT);
        append(CONTACT_TYPE, null);
        append(EVENT_TYPE, null);
        append(TODO_TYPE, null);
        setSelectCommand(selectCommand);
        addCommand(exitCommand);
        setCommandListener(this);
        this.midlet = midlet;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == exitCommand) {
            midlet.exit();
        } else if (command == selectCommand) {
            final int listType = getSelectedIndex() + PIM.CONTACT_LIST;
            new Thread(new Runnable() {
                    public void run() {
                        try {
                            Displayable screen =
                                new ListSelectionScreen(midlet, ListTypeSelectionScreen.this,
                                    listType);
                            Display.getDisplay(midlet).setCurrent(screen);
                        } catch (Exception e) {
                            midlet.reportException(e, ListTypeSelectionScreen.this);
                        }
                    }
                }).start();
        }
    }
}

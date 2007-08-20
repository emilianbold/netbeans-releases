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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMList;


/**
 * Demonstrate the use of JSR 75 PIM APIs
 */
public class ListSelectionScreen extends List implements CommandListener, Runnable {
    private final Command selectCommand = new Command("Select", Command.OK, 1);
    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final PIMDemo midlet;
    private final Displayable caller;
    private final int listType;

    public ListSelectionScreen(PIMDemo midlet, Displayable caller, int listType) {
        super("Select a list", List.IMPLICIT);

        String[] lists = PIM.getInstance().listPIMLists(listType);

        for (int i = 0; i < lists.length; i++) {
            append(lists[i], null);
        }

        setSelectCommand(selectCommand);
        addCommand(backCommand);
        setCommandListener(this);
        this.midlet = midlet;
        this.caller = caller;
        this.listType = listType;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            Display.getDisplay(midlet).setCurrent(caller);
        } else if (command == selectCommand) {
            Form form = new Form("Loading PIM list");
            form.append("Please wait...");
            Display.getDisplay(midlet).setCurrent(form);
            new Thread(this).start();
        }
    }

    public void run() {
        String listName = getString(getSelectedIndex());

        try {
            PIMList list = PIM.getInstance().openPIMList(listType, PIM.READ_WRITE, listName);
            Displayable screen = new ItemSelectionScreen(midlet, this, listType, list);
            Display.getDisplay(midlet).setCurrent(screen);
        } catch (Exception e) {
            midlet.reportException(e, this);
        }
    }
}

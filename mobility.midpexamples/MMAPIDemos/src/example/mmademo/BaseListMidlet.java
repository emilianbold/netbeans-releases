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
package example.mmademo;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

/**
 * A base class for MIDLets that provide a list as display.
 * A history of displayed pages is maintained, to provide
 * user flow with automatic <i>back</i> functionality.
 *
 * @version 1.4
 */
public abstract class BaseListMidlet extends MIDlet implements CommandListener {

    // often needed commands.
    // their functionality is provided in this class
    // but child class has to add it to the list in fillList
    protected Command exitCommand = new Command("Exit", Command.EXIT, 1);
    protected Command backCommand = new Command("Back", Command.BACK, 1);
    protected Command playCommand = new Command("Play", Command.ITEM, 1);
    protected Command selectCommand = new Command("Select", Command.ITEM, 1);

    private List list;
    private Stack history;

    private Displayable currDisplayable; // default: null

    /**
     * If true, then the menu will be
     * shown on startApp().
     */
    private boolean firstTime = true;

    public BaseListMidlet(String title) {
	history = new Stack();
	list = new List(title, Choice.IMPLICIT);
	list.setCommandListener(this);
	fillList(list);
	history.setSize(0);
    }

    /**
     * displays the last page that was displayed
     */
    public Displayable goBack() {
	Displayable d;
	if (Utils.DEBUG) {
	    Utils.debugOut("before goBack: ("+history.size()+" elements)");
	    for (int i=history.size()-1; i>=0; i--) {
		Utils.debugOut("   "+i+": "+history.elementAt(i).toString());
	    }
	}
	if (!history.empty()) {
	    d = (Displayable) history.pop();
	    Utils.debugOut("History: "+history.size()+" elements.");
	} else {
	    exit();
	    return null;
	}
	return replaceCurrent(d);
    }

    /**
     * displays the given page. The current one is added to the history.
     */
    public Displayable go(Displayable d) {
	Displayable curr = getCurrentDisplayable();
	if (curr != null) {
	    history.push(curr);
	}
	if (Utils.DEBUG) {
	    Utils.debugOut("after go: ("+history.size()+" elements)");
	    for (int i=history.size()-1; i>=0; i--) {
		Utils.debugOut("   "+i+": "+history.elementAt(i).toString());
	    }
	}
	return replaceCurrent(d);
    }

    /**
     * Replaces current displaying page with
     * the given one. The current page is not added
     * to the history.
     */
    public Displayable replaceCurrent(Displayable d) {
	getDisplay().setCurrent(d);
	if (! (d instanceof Alert)) {
	    // Alerts come back automatically
	    currDisplayable = d;
	}
	Utils.debugOut("SetCurrent: "+d.toString());
	return d;
    }

    public Displayable getCurrentDisplayable() {
	//return getDisplay().getCurrent();
	return currDisplayable;
    }

    protected List getList() {
	return list;
    }

    protected Display getDisplay() {
	return Display.getDisplay(this);
    }

    /**
     * Child classes must implement this to insert
     * the displayed list items and commands.
     */
    protected abstract void fillList(List list);

    /**
     * Child classes must implement this in response to
     * a selection in the list
     */
    protected abstract void selectCommand(int index);


    /**
     * Called when this MIDlet is started for the first
     * time, or when it returns from paused mode.
     * When it's started for the first time, the
     * firstTime flag is true and the list is
     * displayed. Otherwise, if the current Displayable
     * implements Utils.Interruptable, its resumeApp
     * method is called.
     *
     */
    public final void startApp() {
	if (firstTime) {
	    go(getList());
	    firstTime = false;
	} else {
	    Displayable curr = getCurrentDisplayable();
	    if (curr instanceof Utils.Interruptable) {
		((Utils.Interruptable) curr).resumeApp();
	    }
	}
    }

    /**
     * Called when this MIDlet is paused.
     * If the current Displayable
     * implements Utils.Interruptable, its pauseApp
     * method is called.
     */
    public final void pauseApp() {
	Displayable curr = getCurrentDisplayable();
	if (curr instanceof Utils.Interruptable) {
	    ((Utils.Interruptable) curr).pauseApp();
	}
    }

    /**
     * Called when this MIDlet is destroyed.
     * Subclasses should implement this for clean-up.
     */
    public void destroyApp(boolean unconditional) {}

    /**
     * Actively finish this MIDlet
     */
    public final void exit() {
	destroyApp(false);
	notifyDestroyed();
    }

    /*
     * Respond to commands, including exit
     * On the exit command, cleanup and notify that the MIDlet has
     * been destroyed.
     */
    public void commandAction(Command c, Displayable s) {
	if (c == exitCommand) {
	    exit();
	}
	else if (c == backCommand) {
	    goBack();
	}
	else if ((s == list && c == List.SELECT_COMMAND) || c == playCommand || c == selectCommand) {
	    selectCommand(getList().getSelectedIndex());
	}
    }

}

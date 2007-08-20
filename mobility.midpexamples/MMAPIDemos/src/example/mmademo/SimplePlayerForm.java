/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package example.mmademo;

import java.util.*;
import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;


/**
 * MMAPI player main window for media files, implemented as a Form
 *
 * @version 1.4
 */
public class SimplePlayerForm extends Form 
    implements SimplePlayerGUI.Parent, 
	       Utils.ContentHandler, 
	       Utils.Interruptable,
	       Runnable {

    private SimplePlayerGUI gui; // default: null
    private Utils.BreadCrumbTrail parent;

    private static Image spacerImage;
    private ImageItem iiLogo;
    private StringItem siFileTitle;
    private StringItem siTime;
    private StringItem siRate;
    private StringItem siKaraoke;
    private StringItem siFeedback;
    private StringItem siStatus;

    private boolean karaokeShowing;

    private static int maxKaraokeLines = 2;

    private static void debugOut(String s) {
	Utils.debugOut("SimplePlayerForm: "+s);
    }

    public SimplePlayerForm(String title, Utils.BreadCrumbTrail parent) {
	this(title, null, parent);
    }

    public SimplePlayerForm(String title, SimplePlayerGUI spg, Utils.BreadCrumbTrail parent) {
	super(title);
	this.parent = parent;
	this.gui = spg;
	siFileTitle = new StringItem("", "");
	siTime = new StringItem("", "");
	siRate = new StringItem("", "");
	siKaraoke = new StringItem("", "");
	siFeedback = new StringItem("", "");
	siStatus = new StringItem("", "");
	debugOut("constructor finished");
    }

    private void appendNewLine(Item item) {
	insertNewLine(size(), item);
    }

    private void insertNewLine(int pos, Item item) {
	insert(pos, (StringItem)item);

	Spacer spacer = new Spacer(3, 10);
	spacer.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
	if(pos < 8) {
	    insert(pos++, spacer);
	}
    }

    private void setUpItems() {
	// first delete all items
	for (int i = size()-1; i>=0; i--) {
	    delete(i);
	}
	karaokeShowing = false;
	getGUI();
	if (!gui.hasGUIControls()) {
	    makeImageItem();
	}
	appendNewLine(siFileTitle);
	appendNewLine(siTime);
	if (gui.hasRateControl() || gui.hasTempoControl()) {
	    appendNewLine(siRate);
	}
	if (gui.hasGUIControls()) {
	    Control[] controls = gui.getControls();
	    if (controls!=null) {
		for (int i=0; i<controls.length; i++) {
		    Control ctrl = controls[i];
		    if (ctrl instanceof GUIControl) {
			Object guiItem = ((GUIControl) ctrl).initDisplayMode(GUIControl.USE_GUI_PRIMITIVE, null);
			if (guiItem instanceof Item) {
			    append((Item) guiItem);
			    if (ctrl instanceof VideoControl) {
				try {
				    ((VideoControl) ctrl).setDisplayFullScreen(false);
				} catch (MediaException me) {
				    Utils.debugOut(me);
				}
			    }
			}
		    }
		}
	    }
	}
	appendNewLine(siFeedback);
	appendNewLine(siStatus);
    }

    private void makeImageItem() {
	if (gui!=null && !gui.isFullScreen()) {
	    if (iiLogo==null) {
		Image logo = gui.getLogo();
		if (logo!=null) {
		    iiLogo = new ImageItem("", logo,
					   ImageItem.LAYOUT_CENTER
					   | ImageItem.LAYOUT_NEWLINE_BEFORE
					   | ImageItem.LAYOUT_NEWLINE_AFTER, "MMAPI logo");
		}
	    }
	    if (iiLogo != null) {
		insert(0, iiLogo);
	    }
	}
    }

    ////////////////////////////// interface Utils.BreadCrumbTrail /////////////////

    public Displayable go(Displayable d) {
	return parent.go(d);
    }

    public Displayable goBack() {
	return parent.goBack();
    }

    public Displayable replaceCurrent(Displayable d) {
	return parent.replaceCurrent(d);
    }

    public Displayable getCurrentDisplayable() {
	return parent.getCurrentDisplayable();
    }

    /////////////////////////// interface SimplePlayerGUI.Parent //////////////////

	public Utils.BreadCrumbTrail getParent() {
	    return parent;
	}

    // called after the media is prefetched
    public void setupDisplay() {
	setUpItems();
    }

    public void setStatus(String s) {
	siStatus.setText(s);
    }

    public void setFeedback(String s) {
	siFeedback.setText(s);
    }

    public void setFileTitle(String s) {
	siFileTitle.setText(s);
    }

    public void updateKaraoke() {
	int[] karaokeParams = new int[4];
	String[] lines = gui.getKaraokeStr(karaokeParams);
	int currLine = karaokeParams[SimplePlayerGUI.KARAOKE_LINE];
	int lineCount = karaokeParams[SimplePlayerGUI.KARAOKE_LINE_COUNT];
	int syllLen = karaokeParams[SimplePlayerGUI.KARAOKE_SYLLABLE_LENGTH];
	int currLinePos = karaokeParams[SimplePlayerGUI.KARAOKE_LINE_INDEX];

	int thisLine = 0;
	if (lineCount > maxKaraokeLines) {
	    thisLine = currLine - 1;
	    if (thisLine < 0) {
		thisLine = 0;
	    }
	    if (thisLine + maxKaraokeLines > lineCount) {
		thisLine = lineCount - maxKaraokeLines;
	    } else if (lineCount - thisLine > maxKaraokeLines) {
		lineCount = thisLine + maxKaraokeLines;
	    }
	}
	String text = "";
	for (; thisLine < lineCount; thisLine++) {
	    text+=lines[thisLine]+"\n";
	}

	siKaraoke.setText(text);
	if (!karaokeShowing && !gui.isFullScreen()) {
	    // insert karaoke item before feedback line
	    for (int i = size()-1; i>=0; i--) {
		if (get(i)==siFeedback) {
		    if (i>0 && (get(i-1) instanceof ImageItem)) {
			i--;
		    }
		    insertNewLine(i, siKaraoke);
		    break;
		}
	    }
	    // do not try to visualize siKaraoke again
	    karaokeShowing = true;
	}

    }

    public void updateTime() {
	if (gui!=null) {
	    siTime.setText(gui.getMediaTimeStr());
	}
    }

    public void updateRate() {
	if (getGUI().hasTempoControl()) {
	    siRate.setText(gui.getTempoStr());
	} else {
	    siRate.setText(gui.getRateStr());
	}
    }

    public void updateDisplay() {
    }

    public void fullScreen(boolean value) {
	// nothing to do ?
    }

    //////////////////////////////// interface Utils.ContentHandler ///////////////////////////////

    public synchronized void close() {
	if (gui != null) {
	    gui.closePlayer();
	    gui = null;
	}
    }

    public boolean canHandle(String url) {
	// TODO ?
	return true;
    }

    public void handle(String name, String url) {
	Utils.debugOut("SimplePlayerForm: handle "+url);
	getGUI().setParent(this);
	gui.setSong(name, url);
	doHandle();
    }

    public void handle(String name, InputStream is, String contentType) {
	getGUI().setParent(this);
	gui.setSong(name, is, contentType);
	doHandle();
    }


    // ///////////////////////// interface Utils.ContentHandler //////////////// //

    private synchronized SimplePlayerGUI getGUI() {
	if (gui == null) {
	    debugOut("create GUI");
	    gui = new SimplePlayerGUI();
	    gui.initialize(getTitle(), this);
	    gui.setTimerInterval(500);
	    makeImageItem();
	}
	return gui;
    }


    private void doHandle() {
	debugOut("doHandle");
	setUpItems();
	// IMPL NOTE:
	// I want to display the player first, and THEN start prefetching.
	// the only way I was able to achieve this was by creating a new thread.
	new Thread(this).start();
    }

    public void run() {
	gui.startPlayer();
    }


    // /////////////////////////////////// Interface Utils.Interruptable ////////////////// //

    /**
     * Called in response to a request to pause the MIDlet.
     * This implementation will just call the same
     * method in the GUI implementation.
     */
    public synchronized void pauseApp() {
	if (gui != null) {
	    gui.pauseApp();
	}
    }

	
    /**
     * Called when a MIDlet is asked to resume operations
     * after a call to pauseApp(). This method is only
     * called after pauseApp(), so it is different from
     * MIDlet's startApp().
     *
     * This implementation will just call the same
     * method in the GUI implementation.
     */
    public synchronized void resumeApp() {
	if (gui != null) {
	    gui.resumeApp();
	}
    }


    // for debugging
    public String toString() {
	return "SimplePlayerForm";
    }

}

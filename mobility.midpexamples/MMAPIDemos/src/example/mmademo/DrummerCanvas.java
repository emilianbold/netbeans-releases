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
 * Drummer Canvas
 *
 * @version 1.1
 */
public class DrummerCanvas extends Canvas
    implements CommandListener {

    private static final String TITLE = "MMAPI Drummer";
    private static final String LOGO = "/icons/logo.png";
    private Command backCommand = new Command("Back", Command.BACK, 1);
    private Command helpCommand = new Command("Quick Help", Command.ITEM, 10);

    private static int TITLE_TOP = 2;
    private static int LOGO_GAP = 2;
    private static int CURR_DRUM_GAP = 2;
    private static int STATUS_GAP = 2;

    private Image logo = null;
    private Utils.BreadCrumbTrail parent;
    private SimpleTones tones;

    private String status="";
    private String currDrum="";

    private int displayWidth = -1;
    private int displayHeight = -1;
    private int textHeight = 10;
    private int logoTop = 0;
    private int currDrumTop = 0;
    private int statusTop = 0;

    private MIDIControl mc;

    // the MIDI numbers for the drums
    private static final int[] DRUM_NUMBERS = {
    	0,    // not used
    	0x2A, // 1: closed hihat
    	0x2E, // 2: open hihat
    	0x36, // 3: Tambourine
    	0x32, // 4: hi tom
    	0x2F, // 5: mid tom
    	0x2B, // 6: low tom
	0x33, // 7: ride cymbal
	0x38, // 8: cow bell
	0x31, // 9: crash cymbal
    	0x24, // *: bass drum
    	0x27, // 0: hand clap
    	0x28, // #: snare drum
    };

    private static final String[] DRUM_NAMES = {
    	"",              // not used
    	"Closed Hi-Hat", // 1
    	"Open Hi-Hat",   // 2
    	"Tambourine",    // 3
    	"Hi Tom",        // 4
    	"Mid Tom",       // 5
    	"Low Tom",       // 6
	"Ride Cymbal",   // 7
	"Cow Bell",      // 8
	"Crash Cymbal",  // 9
    	"Bass Drum",     // *
    	"Hand Clap",     // 0
    	"Snare Drum",    // #
    };

    private static final String[] SHORT_DRUM_NAMES = {
    	"", "CH", "OH", "TB", "HT", "MT", "LT",
	    "RC", "CB", "CC", "BD", "HC", "SD",
    };

    private static final int[] DRUM_KEYS = {
    	0, // not used
	KEY_NUM1, KEY_NUM2, KEY_NUM3, KEY_NUM4, KEY_NUM5,
	KEY_NUM6, KEY_NUM7, KEY_NUM8, KEY_NUM9,
	KEY_STAR, KEY_NUM0, KEY_POUND,
    };

    private static void debugOut(String s) {
	Utils.debugOut("DrummerCanvas: "+s);
    }

    public DrummerCanvas(SimpleTones tones, Utils.BreadCrumbTrail parent) {
	super();
	this.parent = parent;
	this.tones = tones;
    }

    // //////////////////////////// interface Utils.BreadCrumbTrail ///////////

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

    // ///////////////////////// interface CommandListener ////////////////////

    public void commandAction(Command c, Displayable s) {
	if (c == backCommand) {
	    goBack();
	} else if (c == helpCommand) {
	    showHelp();
	} else if (s != this) {
	    // e.g. when list item in MetaData display list is pressed
	    goBack();
	}
    }


    private void status(String s) {
	status = s;
	repaint(0, statusTop, displayWidth, textHeight);
	serviceRepaints();
    }

    private void setCurrDrum(int num) {
	currDrum = DRUM_NAMES[num];
	repaint(0, currDrumTop, displayWidth, textHeight);
	serviceRepaints();
    }

    public void updateDisplay() {
	repaint();
	serviceRepaints();
    }

    public void show() {
	addCommand(backCommand);
	addCommand(helpCommand);
	setCommandListener(this);
	status("Prefetching and getting MIDIControl");
	updateDisplay();
	new Thread(new Runnable() {
	    public void run() {
		try {
		    mc = tones.getMIDIControl();
		    status("Ready.");
		} catch (Exception e) {
		    status(Utils.friendlyException(e));
		    mc = null;
		}
	    }
	}).start();
    }

    // ///////////////////////// Canvas callbacks ///////////////////////////////

    protected void keyPressed(int keycode) {
	try {
	    for (int num = 1; num < DRUM_KEYS.length; num++) {
		if (keycode == DRUM_KEYS[num]) {
		    playDrum(num);
		    return;
		}
	    }
	    // action code currently not used
	    /*
	    int code = getGameAction(keycode);
	    if (code == RIGHT) {
	    } else if (code == LEFT) {
	    } else if (code == UP) {
	    } else if (code == DOWN) {
	    } else if (code == FIRE) {
	    }
	    */
	} catch (Throwable t) {
	    Utils.error(t, parent);
	}
    }

    private void playDrum(int num) {
    	if (mc == null) return;
    	setCurrDrum(num);
	mc.shortMidiEvent(0x99, DRUM_NUMBERS[num], 120);
	mc.shortMidiEvent(0x99, DRUM_NUMBERS[num], 0);
    }



    private boolean intersects(int clipY, int clipHeight, int y, int h) {
	return (clipY<=y+h && clipY+clipHeight>=y);
    }

    public void paint(Graphics g) {
	try {
	    if (displayHeight == -1) {
		displayWidth = getWidth();
		displayHeight = getHeight();
		textHeight = g.getFont().getHeight();
		logo = getLogo();

		int currTop = TITLE_TOP + textHeight;
		if (logo != null) {
		    currTop += LOGO_GAP;
		    logoTop = currTop;
		    currTop += logo.getHeight();
		}

		// curr drum: before-last line
		currDrumTop = displayHeight - 2*textHeight - STATUS_GAP;
		// status: last line.
		statusTop = displayHeight - textHeight;
	    }

		int clipX = g.getClipX();
		int clipY = g.getClipY();
		int clipWidth = g.getClipWidth();
		int clipHeight = g.getClipHeight();
		// background
		g.setColor(0);
		g.fillRect(clipX, clipY, clipWidth, clipHeight);
		// title
		if (intersects(clipY, clipHeight, TITLE_TOP, textHeight)) {
		    g.setColor(0xFF7f00);
		    g.drawString(TITLE, displayWidth>>1, TITLE_TOP, Graphics.TOP | Graphics.HCENTER);
		}
		// logo
		if (logo != null && intersects(clipY, clipHeight, logoTop, logo.getHeight())) {
		    g.drawImage(logo, displayWidth/2, logoTop, Graphics.TOP | Graphics.HCENTER);
		}

		// Curr Drum
		if (intersects(clipY, clipHeight, currDrumTop, textHeight)) {
		    g.setColor(0xE0E0FF);
		    g.drawString(currDrum, 0, currDrumTop, Graphics.TOP | Graphics.LEFT);
		}
		// Status
		if (intersects(clipY, clipHeight, displayHeight-textHeight, textHeight)) {
		    g.setColor(0xFAFAFA);
		    g.drawString(status, 0, displayHeight, Graphics.BOTTOM | Graphics.LEFT);
		}

	} catch (Throwable t) {
	    debugOut("in paint(): "+Utils.friendlyException(t));
	}
    }


    /**
     * Show a page which explains the keys.
     * For simplicity, the page is implemented as a list...
     */
    public void showHelp() {
        Form form = new Form("Drummer Help");
        StringItem stringItem = 
            new StringItem("", "Use the numeric keys to play drums. " +
            "Each Key corresponds to a different drum.");
        form.append(stringItem);
        form.addCommand(backCommand);
        form.setCommandListener(this);
        go(form);
    }

    private Image getLogo() {
	if (logo==null) {
	    try {
		logo = Image.createImage(LOGO);
	    } catch (Exception ex) {
		logo = null;
	    }
	    if (logo == null) {
		debugOut("can not load "+LOGO);
	    }
	}
	return logo;
    }


}

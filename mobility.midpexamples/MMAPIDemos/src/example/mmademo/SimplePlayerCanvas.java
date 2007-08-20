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
 * MMAPI player main window for media files, implemented as a Canvas
 *
 * @version 1.7
 */
public class SimplePlayerCanvas extends Canvas
    implements SimplePlayerGUI.Parent, 
	       Utils.Interruptable,
	       Utils.ContentHandler, 
	       Runnable {

    private static int PLAYER_TITLE_TOP = 2;
    private static int LOGO_GAP = 2;
    private static int SONG_TITLE_GAP = 2;
    private static int KARAOKE_GAP = 1;
    private static int TIME_GAP = 2;
    private static int RATE_GAP = 2;
    private static int STATUS_GAP = 2;

    private String title;
    private Image logo = null;
    private SimplePlayerGUI gui; // default: null
    private Utils.BreadCrumbTrail parent;

    private VideoControl videoControl;

    private String status="";
    private String feedback="";
    private String fileTitle="";

    int displayWidth = -1;
    int displayHeight = -1;
    int textHeight=10;
    int logoTop = 0;
    int songTitleTop = 0;
    int timeRateTop = 0;
    int timeWidth = 0;
    int karaokeTop = 0;
    int karaokeHeight = 0;
    int maxKaraokeLines = 0;
    int feedbackTop = 0;
    int statusTop = 0;

    int[] karaokeParams = new int[4];

    private static void debugOut(String s) {
	Utils.debugOut("SimplePlayerCanvas: "+s);
    }

    public SimplePlayerCanvas(String title, Utils.BreadCrumbTrail parent) {
	super();
	this.parent = parent;
	this.title = title;
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

    // ///////////////////////// interface SimplePlayerGUI.Parent /////////////

    public Utils.BreadCrumbTrail getParent() {
	return parent;
    }

    // called after the media is prefetched
    public void setupDisplay() {
	// if there is video control, change display layout
	videoControl = gui.getVideoControl();
	if (videoControl != null) {
	    Utils.debugOut("Initializing display mode.");
	    try {
		videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
	    } catch (Exception e) {
		setFeedback(Utils.friendlyException(e));
		videoControl = null;
	    }
	}
	// force recalculation of layout
	displayHeight = -1;
	updateDisplay();
    }


    public String getTitle() {
	return title;
    }

    public void setStatus(String s) {
	status=s;
	repaint(0, statusTop, displayWidth, textHeight);
	serviceRepaints();
    }

    public void setFeedback(String s) {
	feedback=s;
	repaint(0, feedbackTop, displayWidth, textHeight);
	serviceRepaints();
    }

    public void setFileTitle(String s) {
	fileTitle = s;
	repaint(0, songTitleTop, displayWidth, textHeight);
	serviceRepaints();
    }

    public void updateKaraoke() {
	repaint(0, karaokeTop, displayWidth, karaokeHeight);
	serviceRepaints();
    }

    public void updateTime() {
	repaint(0, timeRateTop, timeWidth, textHeight);
	serviceRepaints();
    }

    public void updateRate() {
	repaint(timeWidth, timeRateTop, displayWidth, textHeight);
	serviceRepaints();
    }

    public void updateDisplay() {
	repaint();
	serviceRepaints();
    }

    public void fullScreen(boolean value) {
	// may not display the other items
	// when going back to small video
	repaint();
    }

    // ////////////////////////////// interface Utils.ContentHandler //////////

    public synchronized void close() {
	if (gui != null) {
	    gui.closePlayer();
	    gui = null;
	}
    }

    public boolean canHandle(String url) {
	return true;
    }

    public void handle(String name, String url) {
	Utils.debugOut("SimplePlayerCanvas: handle "+url);
	getGUI().setParent(this);
	gui.setSong(name, url);
	doHandle();
    }

    public void handle(String name, InputStream is, String contentType) {
	getGUI().setParent(this);
	gui.setSong(name, is, contentType);
	doHandle();
    }

    public void handle(String name, Player player) {
	getGUI().setParent(this);
	gui.setSong(name, player);
	doHandle();
    }

    // ///////////////////////// interface Utils.ContentHandler //////////////// //

    private synchronized SimplePlayerGUI getGUI() {
	if (gui == null) {
	    gui = new SimplePlayerGUI();
	    gui.initialize(title, this);
	}
	return gui;
    }

    private void doHandle() {
	// IMPL NOTE:
	// I want to display the player first, and THEN start prefetching.
	// the only way I was able to achieve this was by creating a new thread.
	repaint();
	serviceRepaints();
	new Thread(this).start();
    }

    public void run() {
	gui.startPlayer();
    }


    // ///////////////////////// Canvas callbacks ///////////////////////////////

    protected void keyPressed(int keycode) {
	try {
	    SimplePlayerGUI gui = getGUI();
	    switch (keycode) {
	    case KEY_NUM1:
		// Jump backward
		gui.skip(true);
		break;
	    case KEY_NUM2:
		gui.togglePlayer();
		break;
	    case KEY_NUM3:
		// Jump forward
		gui.skip(false);
		break;
	    case KEY_NUM7:
		gui.stepFrame(-1);
		break;
	    case KEY_NUM9:
		gui.stepFrame(+1);
		break;
	    case KEY_NUM5:
		gui.pausePlayer();
		gui.setMediaTime(0);
		setFeedback("Player Stopped.");
		break;
	    case KEY_NUM4:
		gui.changeRate(true);
		break;
	    case KEY_NUM8:
		gui.toggleFullScreen();
		break;
	    case KEY_NUM6:
		gui.changeRate(false);
		break;
	    case KEY_NUM0:
		gui.toggleMute();
		break;
	    case KEY_STAR:
		gui.changeVolume(true);
		break;
	    case KEY_POUND:
		gui.changeVolume(false);
		break;
	    default:
		int code = getGameAction(keycode);
		if (code == RIGHT) {
		    // Jump forward
		    gui.skip(false);
		} else if (code == LEFT) {
		    // Jump backward
		    gui.skip(true);
		} else if (code == UP) {
		    gui.transpose(false);
		} else if (code == DOWN) {
		    gui.transpose(true);
		} else if (code == FIRE) {
		    gui.togglePlayer();
		}
	    }
	} catch (Throwable t) {
	    Utils.error(t, parent);
	}
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
		if (gui != null && videoControl == null) {
		    if (logo == null) {
			logo = gui.getLogo();
		    }
		} else {
		    logo = null;
		}
		int currTop = PLAYER_TITLE_TOP + textHeight;
		if (logo != null) {
		    currTop += LOGO_GAP;
		    logoTop = currTop;
		    currTop += logo.getHeight();
		}
		currTop += SONG_TITLE_GAP;
		songTitleTop = currTop;
		currTop += TIME_GAP + textHeight;
		timeRateTop = currTop;
		timeWidth = g.getFont().stringWidth("0:00:0  ");
		currTop +=  textHeight+KARAOKE_GAP;

		// feedback: before-last line
		feedbackTop = displayHeight - 2*textHeight - STATUS_GAP;
		// karaoke: squeeze as many lines as possible in between rate and feedback
		maxKaraokeLines = (feedbackTop - currTop) / (textHeight + KARAOKE_GAP);
		karaokeHeight = maxKaraokeLines * (textHeight + KARAOKE_GAP);
		karaokeTop = currTop + ((feedbackTop - currTop - karaokeHeight) / 2);
		// video: same space as karaoke.
		if (videoControl != null) {
		    int videoTop = timeRateTop + textHeight;
		    int dispWidth = videoControl.getSourceWidth();
		    int dispHeight = videoControl.getSourceHeight();
		    if (dispWidth > displayWidth)
			dispWidth = displayWidth;
		    if (dispHeight > feedbackTop - videoTop)
			dispHeight = feedbackTop - videoTop;
		    videoControl.setDisplayLocation((displayWidth - dispWidth) / 2,
						    videoTop);
		    videoControl.setDisplaySize(dispWidth, dispHeight);
		    videoControl.setVisible(true);
		    Utils.debugOut("Setting video to visible at (0, "
				   +videoTop+") with size ("+displayWidth+", "+(feedbackTop - videoTop)+")");
		}
		// status: last line.
		statusTop = displayHeight - textHeight;
	    }

	    int clipX=g.getClipX();
	    int clipY=g.getClipY();
	    int clipWidth=g.getClipWidth();
	    int clipHeight=g.getClipHeight();
	    // background
	    g.setColor(0);
	    g.fillRect(clipX, clipY, clipWidth, clipHeight);

	    if ((videoControl == null) || !gui.isFullScreen()) {
		// title
		if (intersects(clipY, clipHeight, PLAYER_TITLE_TOP, textHeight)) {
		    g.setColor(0xFF7f00);
		    g.drawString(title, displayWidth>>1, PLAYER_TITLE_TOP, Graphics.TOP | Graphics.HCENTER);
		}
		// logo
		if (logo != null && intersects(clipY, clipHeight, logoTop, logo.getHeight())) {
		    g.drawImage(logo, displayWidth/2, logoTop, Graphics.TOP | Graphics.HCENTER);
		}
		// song name (+ duration)
		if (intersects(clipY, clipHeight, songTitleTop, textHeight)) {
		    g.setColor(0xFF7F00);
		    g.drawString(fileTitle, displayWidth>>1, songTitleTop, Graphics.TOP | Graphics.HCENTER);
		}
		if (gui!=null) {
		    // time and rate/tempo display
		    if (intersects(clipY, clipHeight, timeRateTop, textHeight)) {
			if (intersects(clipX, clipWidth, 0, timeWidth)) {
			    g.setColor(0xF0F0F0);
			    g.drawString(gui.getMediaTimeStr(), 0, timeRateTop, Graphics.TOP | Graphics.LEFT);
			}
			if (intersects(clipX, clipWidth, timeWidth+1, displayWidth)) {
			    // tempo/rate display
			    if (gui.hasTempoControl()) {
				g.setColor(0xF0F0F0);
				g.drawString(gui.getTempoStr(), displayWidth, timeRateTop, Graphics.TOP | Graphics.RIGHT);
			    } else {
				g.setColor(0xF0F0F0);
				g.drawString(gui.getRateStr(), displayWidth, timeRateTop, Graphics.TOP | Graphics.RIGHT);
			    }
			}
		    }
		    // Karaoke text
		    if (intersects(clipY, clipHeight, karaokeTop, karaokeHeight)) {
			String[] lines = gui.getKaraokeStr(karaokeParams);
			int currTop = karaokeTop;
			int currLine = karaokeParams[SimplePlayerGUI.KARAOKE_LINE];
			int lineCount = karaokeParams[SimplePlayerGUI.KARAOKE_LINE_COUNT];
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
			int syllLen = karaokeParams[SimplePlayerGUI.KARAOKE_SYLLABLE_LENGTH];
			int currLinePos = karaokeParams[SimplePlayerGUI.KARAOKE_LINE_INDEX];
			for (; thisLine < lineCount; thisLine++) {
			    if (currLine != thisLine || syllLen == 0) {
				if (thisLine < currLine) { // && syllLen > 0
				    // already sung text in yellow
				    g.setColor(0xFFFF30);
				} else {
				    // other stuff in grey
				    g.setColor(0x909090);
				}
				g.drawString(lines[thisLine], 0, currTop, Graphics.TOP | Graphics.LEFT);
			    } else {
				// first draw any text before current position
				int xPos = 0;
				String currText;
				if (currLinePos > 0) {
				    currText = lines[thisLine].substring(0, currLinePos);
				    g.setColor(0xFFFF30); // yellow
				    g.drawString(currText, 0, currTop, Graphics.TOP | Graphics.LEFT);
				    xPos += g.getFont().stringWidth(currText);
				}
				// color the current syllable
				g.setColor(0xFFFF30);
				currText = lines[thisLine].substring(currLinePos, currLinePos + syllLen);
				g.drawString(currText, xPos, currTop, Graphics.TOP | Graphics.LEFT);
				if (currLinePos + syllLen < lines[thisLine].length()) {
				    xPos += g.getFont().stringWidth(currText);
				    currText = lines[thisLine].substring(currLinePos + syllLen);
				    g.setColor(0x909090); // grey
				    g.drawString(currText, xPos, currTop, Graphics.TOP | Graphics.LEFT);
				}
			    }
			    currTop += textHeight + KARAOKE_GAP;
			}
		    }
		}
		// Feedback
		if (intersects(clipY, clipHeight, feedbackTop, textHeight)) {
		    g.setColor(0xE0E0FF);
		    g.drawString(feedback, 0, feedbackTop, Graphics.TOP | Graphics.LEFT);
		}
		// Status
		if (intersects(clipY, clipHeight, displayHeight-textHeight, textHeight)) {
		    g.setColor(0xFAFAFA);
		    g.drawString(status, 0, displayHeight, Graphics.BOTTOM | Graphics.LEFT);
		}
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
	List list = new List("Simple Player Help", Choice.IMPLICIT);
	list.append("1: Skip back", null);
	list.append("2: Start/Stop", null);
	list.append("3: Skip forward", null);
	list.append("4: Slower", null);
	list.append("5: Stop (and rewind)", null);
	list.append("6: Faster", null);
	list.append("7: Prev. video frame", null);
	list.append("8: Fullscreen on/off", null);
	list.append("9: Next video frame", null);
	list.append("*: quieter", null);
	list.append("0: Mute on/off", null);
	list.append("#: louder", null);
	list.append("Left: skip back", null);
	list.append("Right: skip forward", null);
	list.append("Up: Pitch up", null);
	list.append("Down: Pitch down", null);
	list.append("Fire: Start/Stop", null);

	list.addCommand(gui.backCommand);
	list.setCommandListener(gui);
	go(list);
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
	return "SimplePlayerCanvas";
    }

}

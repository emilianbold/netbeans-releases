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
 * GUI functionality for SimplePlayer.
 * This class is used by SimplePlayerCanvas and SimplePlayerForm and
 * provides the actual logic.
 * SimplePlayerCanvas and SimplePlayerForm provide the interaction of
 * the main window with the user.
 *
 * @version 1.10
 */
public class SimplePlayerGUI implements CommandListener, PlayerListener,
					ItemStateListener {
    // IMPL NOTE: used for the form-based player for video
    // if you set this to false, the currently active player is used
    private final static boolean USE_FORM_PLAYER_FOR_VIDEO = false;

    // maximum rate to support, in percent
    private final static int LIMIT_RATE = 300;

    // loop
    private static final int LOOP_ONCE = 0;
    private static final int LOOP_3    = 1;
    private static final int LOOP_INF  = 2;
    private int loopmode = LOOP_ONCE;

    // timer (for time display and karaoke asynchronous event)
    private static final int DEFAULT_TIMER_INTERVAL = 50;
    private int timerInterval = DEFAULT_TIMER_INTERVAL;

    // gauge
    private static final int GAUGE_NONE   = -1;
    private static final int GAUGE_VOLUME = 0;  // gauge is used as volume slider
    private static final int GAUGE_RATE   = 1;  // gauge is used as rate slider
    private static final int GAUGE_TEMPO  = 2;  // gauge is used as tempo slider
    private static final int GAUGE_PITCH  = 3;  // gauge is used as pitch slider
    private int gaugeMode = GAUGE_NONE;
    private Gauge gauge = null;
    private StringItem gaugeLabel = null;
    private Form gaugeForm = null;

    // display timer
    private Timer guiTimer=null;
    private TimerTask timeDisplayTask=null;

    // Commands
    Command backCommand = new Command("Back", Command.BACK, 1); // is used in SimplePlayerCanvas
    private Command playCommand = new Command("Play", Command.ITEM, 1);
    private Command stopCommand = new Command("Stop", Command.ITEM, 1);
    private Command metaCommand = new Command("META data", Command.ITEM, 3);
    private Command volCommand = new Command("Volume", Command.ITEM, 2);
    private Command muteCommand = new Command("Mute", Command.ITEM, 1);
    private Command unmuteCommand = new Command("Unmute", Command.ITEM, 1);
    private Command loopCommand = new Command("Loopmode", Command.ITEM, 4);
    private Command stcCommand = new Command("Stop in 5 sec", Command.ITEM, 4);
    private Command selectCommand = new Command("Select", Command.ITEM, 1);
    private Command skipFwCommand = new Command("Skip Forward", Command.ITEM, 5);
    private Command skipBwCommand = new Command("Skip Backward", Command.ITEM, 5);
    private Command rewindCommand = new Command("Rewind", Command.ITEM, 5);
    private Command rateCommand = new Command("Rate", Command.ITEM, 5);
    private Command tempoCommand = new Command("Tempo", Command.ITEM, 5);
    private Command pitchCommand = new Command("Pitch", Command.ITEM, 5);
    private Command fullScreenCommand = new Command("Full Screen: ON", Command.ITEM, 5);
    private Command normalScreenCommand = new Command("Full Screen: OFF", Command.ITEM, 5);
    private Command startRecordCommand = new Command("Start Recording", Command.ITEM, 5);
    private Command stopRecordCommand = new Command("Stop Recording", Command.ITEM, 5);
    private Command helpCommand = new Command("Quick Help", Command.ITEM, 10);

    // display
    private Parent parent;
    private Displayable display;
    private static Image logo = null;

    // full screen
    private boolean fullScreen = false;

    // recording
    private boolean isRecording = false;
    private String recordLocator = "file:///root1/recording.wav";

    // the player instance
    private Player player=null;

    // song descriptors
    private String title;
    private InputStream songInputStream;
    private String durationStr;
    private String songContentType="";
    private String songLocator="";
    private String songName="";
    private String[] songDisplayNames = new String[0];
    private int currSongDisplay = 0;
    private int changeSongDisplayCounter = 0;
    private static final int SONG_DISPLAY_COUNTER = 2000 / DEFAULT_TIMER_INTERVAL; // 2 seconds

    // karaoke support
    private final static int LYRICS_EVENT = 0x60;       // not an official event
    private final static String LYRICS_KEY = "lyrics";  // not an official key for MetaDataControl
    // indexes in params array returned by getKaraokeStr()
    public final static int KARAOKE_LINE_COUNT = 0;
    public final static int KARAOKE_LINE = 1;
    public final static int KARAOKE_LINE_INDEX = 2;
    public final static int KARAOKE_SYLLABLE_LENGTH = 3;

    private boolean karaokeMode = false;
    private String karaokeLyrics = "";
    private int currKaraokeLine = 0;
    private int currKaraokeLinePos = 0;
    private int currKaraokeLength = 0;
    private String[] karaokeLines = null;
    private int[] karaokeLinePos = null;  // start position in karaokeLyrics for each line
    private int karaokeLineCount = 0;
    private int redisplayKaraokeCounter = 0; // asynchronous switch to next Karaoke page
    private int nextKaraokePos = 0; // for asynchronous display of next line/phrase

    // pause/resume support
    private boolean restartOnResume = false;

    private static void debugOut(String s) {
	Utils.debugOut("SimplePlayerGUI: "+s);
    }

    // /////////////////////////// INITIALIZATION /////////////////////////////

    /** Note: parent MUST be Displayable */
    public SimplePlayerGUI() {
	// initialize(String title, Parent parent) must be called after this
    }

    public void initialize(String title, Parent parent) {
	this.title = title;
	setParent(parent);
	initialize();
    }

    public void setParent(Parent parent) {
	this.parent = parent;
	if (!(parent instanceof Displayable)) {
	    throw new RuntimeException("parent must be instanceof Displayable!");
	}
	display = (Displayable) parent;
	display.addCommand(backCommand);
	display.setCommandListener(this);
	updateTime();
	updateRate(null);
	updateTempo(null);
	durationUpdated();
	parent.updateKaraoke();
	parent.updateDisplay();
    }

    private void initialize() {
	karaokeMode = false;
	karaokeLyrics = "";
	currKaraokeLine = 0;
	currKaraokeLinePos = 0;
	currKaraokeLength = 0;
	karaokeLines = null;
	karaokeLinePos = null;
	karaokeLineCount = 0;
	redisplayKaraokeCounter = 0;
	removeCommands();
	setStatus("");
	durationStr = "";
	setSong("No song loaded", "");
	guiTimer=new Timer();
    }

    public void setSong(String name, String locator) {
	songLocator = locator;
	songInputStream = null;
	songContentType = "";
	doSetSong(name);
    }

    public void setSong(String name, InputStream is, String contentType) {
	songLocator = "";
	songInputStream = is;
	songContentType = contentType;
	doSetSong(name);
    }

    public void setSong(String name, Player player) {
	songLocator = "";
	songInputStream = null;
	songContentType = "";
	doSetSong(name);
	this.player = player;
    }

    private void doSetSong(String name) {
	songName = name;
	songDisplayNames = new String[1];
	songDisplayNames[0] = name;
	currSongDisplay = 0;
	closePlayer();
	setStatus("");
	setFeedback("");
	clearKaraoke();
	updateTempo(null);
	updateTime();
	updateSongDisplay();
    }

    public void setTimerInterval(int millis) {
	timerInterval = millis;
    }

    // /////////////////////////// VISUAL FEEDBACK ////////////////////////////

    private void error(Throwable e) {
	error(Utils.friendlyException(e));
	if (Utils.DEBUG) e.printStackTrace();
    }

    private void error(String s) {
	setFeedback(s);
    }

    private void setStatus(String s) {
	parent.setStatus(s);
	if (Utils.DEBUG) System.out.println("Status: "+s);
    }

    private void setFeedback(String s) {
	parent.setFeedback(s);
	if (Utils.DEBUG) System.out.println("Feedback: "+s);
    }

    private void updateKaraoke() {
	parent.updateKaraoke();
    }

    private void updateTime() {
	parent.updateTime();
    }

    private void updateRate(RateControl c) {
	parent.updateRate();
	// if the rate gauge is activated, update it
	if (gauge != null && gaugeMode == GAUGE_RATE) {
	    if (c==null) {
		c = getRateControl();
	    }
	    if (c != null) {
		String disp = getRateStr();
		// or
		// gauge.setValue((c.getRate() - c.getMinRate() + 1)/1000);
		gauge.setValue((c.getRate() - c.getMinRate())/1000);
		gaugeLabel.setLabel(disp);
	    }
	}
    }

    private void updateTempo(TempoControl c) {
	parent.updateRate();
	// if the tempo gauge is activated, update it
	if (gauge != null && gaugeMode == GAUGE_TEMPO) {
	    if (c==null) {
		c = getTempoControl();
	    }
	    if (c != null) {
		String disp = getTempoStr();
		gauge.setValue((c.getTempo()/1000)-1);
		gaugeLabel.setLabel(disp);
	    }
	}
    }

    private void updateVolume(VolumeControl c) {
	if (c==null) {
	    c = getVolumeControl();
	}
	if (c != null) {
	    int l = c.getLevel();
	    String disp = "Volume: "+String.valueOf(l);
	    if (c.isMuted()) {
		disp += " (muted)";
	    }
	    if (gauge != null && gaugeMode == GAUGE_VOLUME) {
		gauge.setValue(l);
		gaugeLabel.setLabel(disp);
	    }
	    setFeedback(disp);
	}
    }

    private void updatePitch(PitchControl c) {
	if (c==null) {
	    c = getPitchControl();
	}
	if (c != null ) {
	    String disp = "Transpose: "+toFloatString(c.getPitch(), 3)+" semi tones";
	    // if the pitch gauge is activated, update it
	    if (gauge != null && gaugeMode == GAUGE_PITCH) {
		// WTK removed the next line
		gauge.setValue((c.getPitch() - c.getMinPitch())/1000);
		gaugeLabel.setLabel(disp);
	    }
	    setFeedback(disp);
	}
    }

    private void updateLoop() {
	if (player != null) {
	    try {
		int loop=1;
		switch (loopmode) {
		case LOOP_ONCE:
		    loop=1;
		    break;
		case LOOP_3:
		    loop=3;
		    break;
		case LOOP_INF:
		    loop=-1;
		    break;
		}
		boolean restart = false;
		if (player.getState() == player.STARTED) {
		    player.stop();
		    restart = true;
		}
		player.setLoopCount(loop);
		if (restart) {
		    player.start();
		}

	    } catch (Exception e) {
		error(e);
	    }
	}
    }

    private void updateSongDisplay() {
	String add = "";
	if (currSongDisplay == 0) {
	    add = durationStr;
	}
	parent.setFileTitle(songDisplayNames[currSongDisplay] + add);
    }

    private void durationUpdated() {
	if (player != null) {
	    // include duration in song name
	    try {
	    	long duration = player.getDuration();
		if (duration >= 0) {
		    durationStr = " ("+timeDisplay(duration)+")";
		} else {
		    durationStr = "";
		}
	    } catch (IllegalStateException ise) {
	    	// thrown in CLOSED state
		durationStr = "";
	    }
	    updateSongDisplay();
	}
    }

    private void clearKaraoke() {
	karaokeLineCount = 0;
	nextKaraokePos = -1;
	redisplayKaraokeCounter = 0;
	updateKaraoke();
    }

    // /////////////////////////// DISPLAY UTILITIES //////////////////////////

    public Image getLogo() {
	if (logo==null) {
	    try {
		logo = Image.createImage("/icons/logo.png");
	    } catch (Exception ex) {
		logo = null;
	    }
	    if (logo == null) {
		debugOut("can not load logo.png");
	    }
	}
	return logo;
    }

    private String toFloatString(int number, int digitsAfterDot) {
	StringBuffer ret=new StringBuffer(String.valueOf(number));
	while (ret.length()<(digitsAfterDot+1)) {
	    ret.insert(0, '0');
	}
	ret.insert(ret.length()-digitsAfterDot, '.');
	return ret.toString();
    }

    private String formatNumber(long num, int len, boolean leadingZeros) {
	StringBuffer ret=new StringBuffer(String.valueOf(num));
	if (leadingZeros) {
	    while (ret.length()<len) {
		ret.insert(0, '0');
	    }
	} else {
	    while (ret.length()<len) {
		ret.append('0');
	    }
	}
	return ret.toString();
    }

    private String timeDisplay(long us) {
	long ts = us/100000;
	return formatNumber(ts/600l, 2, true)+":"+formatNumber(((ts % 600) / 10), 2, true)+"."+String.valueOf(ts % 10);
    }

    public String getMediaTimeStr() {
	try {
	    if (player!=null) {
		return timeDisplay(player.getMediaTime());
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return "--:--:-";
    }

    public String getTempoStr() {
	if (player!=null) {
	    TempoControl tc = getTempoControl();
	    if (tc != null) {
		int tempo = tc.getTempo();
		return toFloatString((tempo+50)/100,1)
		    +"bpm (eff: "
		    +toFloatString((int) (((((long) tc.getRate())*((long) tempo)/100000)+50)/100l), 1)+")";
	    }
	}
	return "";
    }

    public String getRateStr() {
	if (player!=null) {
	    RateControl rc = getRateControl();
	    if (rc!=null) {
		return "Rate: "+toFloatString(rc.getRate()/100,1)+"%";
	    }
	}
	return "";
    }

    /**
     * switch to next karaoke screen
     */
    private void displayNextKaraokePhrase() {
	if (karaokeLyrics != null && karaokeLineCount > 0 && karaokeLines != null && nextKaraokePos >= 0) {
	    if (nextKaraokePos < karaokeLyrics.length() && nextKaraokePos > karaokeLinePos[currKaraokeLine] ) {
		setupKaraokeLines(nextKaraokePos, 0);
		updateKaraoke();
	    }
	}
    }

    private void addKaraokeLine(int start, int end) {
	if (karaokeLines == null || karaokeLines.length <= karaokeLineCount) {
	    String[] newKL = new String[karaokeLineCount+4];
	    if (karaokeLines != null) {
		System.arraycopy(karaokeLines, 0, newKL, 0, karaokeLineCount);
	    }
	    karaokeLines = newKL;
	    int[] newKLP = new int[karaokeLineCount+5]; // one more than karaokeLines.length
	    if (karaokeLinePos != null) {
		System.arraycopy(karaokeLinePos, 0, newKLP, 0, karaokeLineCount+1);
	    }
	    karaokeLinePos = newKLP;
	}
	karaokeLines[karaokeLineCount] = karaokeLyrics.substring(start, end);
	karaokeLinePos[karaokeLineCount++] = start;
    }


    private void setupKaraokeLines(int pos, int syllableLen) {
	int len = (karaokeLyrics == null)?0:karaokeLyrics.length();
	if (len == 0) {
	    debugOut("no karaoke lyrics");
	    return;
	}
	// cancel automatic display of next line if it is already displayed
	nextKaraokePos = -1;
	if (pos < 0 || pos >= len) {
	    karaokeLineCount = 0;
	    debugOut("setupKaraokeLines: pos out of bounds!");
	    return;
	}
	// the strings in karaokeLines never start with a control character
	while (karaokeLyrics.charAt(pos) == '\\' || karaokeLyrics.charAt(pos) == '/') {
	    pos++;
	    syllableLen--;
	}
	if (syllableLen<0) {
	    syllableLen=0;
	}
	if (karaokeLinePos == null
	    || karaokeLineCount == 0
	    || pos < karaokeLinePos[0]
	    || pos >= karaokeLinePos[karaokeLineCount]) {
	    // need to re-setup karaoke lines
	    // first find the start of this phrase
	    int startPos = pos;
	    while (karaokeLyrics.charAt(startPos) != '\\' && startPos > 0) {
		startPos--;
	    }
	    if (karaokeLyrics.charAt(startPos) == '\\') {
		startPos++;
	    }
	    // now go through the lines and add them to karaokeLines array
	    karaokeLineCount = 0;
	    int endPos = startPos;
	    while (endPos<len) {
		char c = karaokeLyrics.charAt(endPos);
		if (c == '/') {
		    // new line
		    addKaraokeLine(startPos, endPos);
		    startPos = endPos+1;
		} else if (c == '\\') {
		    // end of phrase
		    break;
		}
		endPos++;
	    }
	    addKaraokeLine(startPos, endPos);
	    if (endPos < len) {
		endPos--;
	    }
	    karaokeLinePos[karaokeLineCount] = endPos;
	}
	// search the current line and position in the current line
	currKaraokeLength = 0;
	for (int i = 0; i < karaokeLineCount; i++) {
	    if (pos >= karaokeLinePos[i] && pos < karaokeLinePos[i+1]) {
		currKaraokeLine = i;
		currKaraokeLinePos = pos - karaokeLinePos[i];
		currKaraokeLength = syllableLen;
		//debugOut("For pos="+pos+" and syllLen="+syllableLen+", found line "+i+" . Set currKaraokeLinePos="+currKaraokeLinePos);
		if ((currKaraokeLinePos + syllableLen >= karaokeLines[i].length())) {
		    nextKaraokePos = karaokeLinePos[i+1]+1;
		    redisplayKaraokeCounter = 100 / DEFAULT_TIMER_INTERVAL+1; // approx. 100ms
		}
		break;
	    }
	}
    }

    /**
     * OUT: params:
     * 0: number of valid strings in return array
     * 1: index in return array of currently sung line
     * 2: index in current line which is currently sung. if -1: nothing currently sung
     * 3: length of syllable currently sung.
     */
    public String[] getKaraokeStr(int[] params) {
	params[0] = karaokeLineCount;
	params[1] = currKaraokeLine;
	params[2] = currKaraokeLinePos;
	params[3] = currKaraokeLength;
	return karaokeLines;
    }

    // /////////////////////////// USER FLOW //////////////////////////////////

    private void goBack() {
	if (parent.getCurrentDisplayable() == display) {
	    closePlayer();
	}
	Displayable now = parent.goBack();
	if (now == display) {
	    // if main player window is showing
	    setPlayerCommands();
	}
    }

    private void setPlayerCommands() {
	removeCommands();
	display.addCommand(backCommand);
	if (player != null) {
	    if (getMetaDataControl()!=null) {
		display.addCommand(metaCommand);
	    }
	    if (getStopTimeControl()!=null) {
		display.addCommand(stcCommand);
	    }
	    VolumeControl vc = getVolumeControl();
	    if (vc!=null) {
		display.addCommand(volCommand);
		if (vc.isMuted()) {
		    display.addCommand(unmuteCommand);
		} else {
		    display.addCommand(muteCommand);
		}
	    }
	    if (getRateControl()!=null) {
		display.addCommand(rateCommand);
	    }
	    if (getTempoControl()!=null) {
		display.addCommand(tempoCommand);
	    }
	    if (getPitchControl()!=null) {
		display.addCommand(pitchCommand);
	    }
	    if (getVideoControl()!=null) {
		display.addCommand(normalScreenCommand);
		display.addCommand(fullScreenCommand);
	    }
	    if (getRecordControl()!=null) {
		if (isRecording) {
		    display.addCommand(stopRecordCommand);
		} else {
		    display.addCommand(startRecordCommand);
		}
	    }
	    display.addCommand(loopCommand);
	    if (player.getState()>=Player.STARTED) {
		display.addCommand(stopCommand);
	    } else {
		display.addCommand(playCommand);
	    }
	    display.addCommand(skipFwCommand);
	    display.addCommand(skipBwCommand);
	    display.addCommand(rewindCommand);
	    if (parent instanceof SimplePlayerCanvas) {
		// Canvas player uses the keys for control,
		// enable a simple help
		display.addCommand(helpCommand);
	    }
	}
    }

    private void removeCommands() {
	display.removeCommand(muteCommand);
	display.removeCommand(unmuteCommand);
	display.removeCommand(metaCommand);
	display.removeCommand(volCommand);
	display.removeCommand(stcCommand);
	display.removeCommand(loopCommand);
	display.removeCommand(backCommand);
	display.removeCommand(playCommand);
	display.removeCommand(stopCommand);
	display.removeCommand(skipFwCommand);
	display.removeCommand(skipBwCommand);
	display.removeCommand(rewindCommand);
	display.removeCommand(rateCommand);
	display.removeCommand(tempoCommand);
	display.removeCommand(pitchCommand);
	display.removeCommand(fullScreenCommand);
	display.removeCommand(normalScreenCommand);
	display.removeCommand(startRecordCommand);
	display.removeCommand(stopRecordCommand);
	display.removeCommand(helpCommand);
    }

    // /////////////////////////// MENU ITEM HANDLERS /////////////////////////

    private void mutePressed() {
	VolumeControl c = getVolumeControl();
	if (c != null) {
	    if (c.isMuted()) {
		//muteCommand.setLabel("Mute"); MIDP_NG only ?
		display.removeCommand(unmuteCommand);
		display.addCommand(muteCommand);
		c.setMute(false);
	    } else {
		//muteCommand.setLabel("Unmute"); MIDP_NG only ?
		display.removeCommand(muteCommand);
		display.addCommand(unmuteCommand);
		c.setMute(true);
	    }
	    updateVolume(c);
	} else {
	    setFeedback("No VolumeControl!");
	}
    }

    private void displayGauge(String title) {
	if (gaugeForm == null) {
	    gaugeForm = new Form(title);
	    gaugeForm.append(gauge);
	    gaugeForm.append(gaugeLabel);
	    gaugeForm.setItemStateListener(this);
	    gaugeForm.addCommand(backCommand);
	    gaugeForm.setCommandListener(this);
	}
	parent.go(gaugeForm);
    }


    private void volPressed() {
	VolumeControl c = getVolumeControl();
	if (c != null) {
	    if (gauge == null || gaugeMode != GAUGE_VOLUME) {
		gauge = new Gauge("Volume", true, 100, c.getLevel());
		gaugeLabel = new StringItem("Volume", "");
		gaugeMode = GAUGE_VOLUME;
		gaugeForm = null;
	    }
	    displayGauge("Set Volume");
	    updateVolume(c);
	} else {
	    setFeedback("No VolumeControl!");
	}
    }

    private void ratePressed() {
	RateControl c = getRateControl();
	if (c != null) {
	    if (gauge == null || gaugeMode != GAUGE_RATE) {
		int minRate = c.getMinRate() / 1000;
		int maxRate = c.getMaxRate() / 1000;
		// limit to LIMIT_RATE to improve usability
		if (maxRate > LIMIT_RATE) {
		    maxRate = LIMIT_RATE;
		}
		int currRate = c.getRate()/1000;
		gauge = new Gauge("Rate", true, maxRate - minRate, currRate - minRate);
		gaugeLabel = new StringItem("Rate", "");
		gaugeMode = GAUGE_RATE;
		gaugeForm = null;
	    }
	    displayGauge("Set Rate");
	    updateRate(c);
	} else {
	    setFeedback("No RateControl!");
	}
    }

    private void pitchPressed() {
	PitchControl c = getPitchControl();
	if (c != null) {
	    if (gauge == null || gaugeMode != GAUGE_PITCH) {
		gauge = new Gauge("Pitch", true, (c.getMaxPitch()-c.getMinPitch())/1000, (c.getPitch() - c.getMinPitch())/1000);
		gaugeLabel = new StringItem("Pitch", "");
		gaugeMode = GAUGE_PITCH;
		gaugeForm = null;
	    }
	    displayGauge("Set Pitch");
	    updatePitch(c);
	} else {
	    setFeedback("No PitchControl!");
	}
    }

    private void tempoPressed() {
	TempoControl c = getTempoControl();
	if (c != null) {
	    if (gauge == null || gaugeMode != GAUGE_TEMPO) {
		gauge = new Gauge("Tempo", true, 399, (c.getTempo()/1000)-1);
		gaugeLabel = new StringItem("Tempo", "");
		gaugeMode = GAUGE_TEMPO;
		gaugeForm = null;
	    }
	    displayGauge("Set Tempo");
	    updateTempo(c);
	} else {
	    setFeedback("No TempoControl!");
	}
    }

    private void metaPressed() {
	MetaDataControl c = getMetaDataControl();
	if (c != null) {
	    List list = new List("Meta Data", Choice.IMPLICIT);
	    String[] allkeys = c.getKeys();
	    Utils.sort(allkeys);
	    for (int i = 0; i < allkeys.length; i++) {
		try {
		    if (!allkeys[i].equals(LYRICS_KEY)) {
			list.append(allkeys[i]+" = "+c.getKeyValue(allkeys[i]), null);
		    }
		} catch (IllegalArgumentException t) {
		    list.append(allkeys[i]+": "+Utils.friendlyException(t), null);
		}
	    }
	    if (allkeys.length==0) {
		list.append("No meta data", null);
	    }
	    list.addCommand(backCommand);
	    list.setCommandListener(this);
	    parent.go(list);
	} else {
	    setFeedback("No MetaDataControl!");
	}
    }

    private void loopPressed() {
	loopmode = (loopmode+1)%3;
	String fb="";
	switch (loopmode) {
	case LOOP_ONCE:
	    fb = "one time";
	    break;
	case LOOP_3:
	    fb = "3 times";
	    break;
	case LOOP_INF:
	    fb = "infinite";
	    break;
	}
	setFeedback("Set loop mode to "+fb);
	updateLoop();
    }

    public boolean isFullScreen() {
	// this may not be accurate - the user can modify that with the keys
	return fullScreen;
    }

    public boolean toggleFullScreen() {
	VideoControl vc = getVideoControl();
	Utils.debugOut("toggleFullScreen");
	if (vc != null) {
	    try {
		vc.setDisplayFullScreen(!fullScreen);
		fullScreen = !fullScreen;
		parent.fullScreen(fullScreen);
		Utils.debugOut("Successfully set to "+(fullScreen?"full screen":"normal screen"));
	    } catch (Exception e) {
		fullScreen = false;
		display.removeCommand(fullScreenCommand);
		display.removeCommand(normalScreenCommand);
		error(e);
	    }
	} else {
	    setFeedback("No VideoControl!");
	}
	return fullScreen;
    }

    private void fullScreenPressed() {
	fullScreen = false;
	toggleFullScreen();
    }

    private void normalScreenPressed() {
	fullScreen = true;
	toggleFullScreen();
    }

    public void stepFrame(int frames) {
	FramePositioningControl fpc = getFramePositioningControl();
	if (fpc != null) {
	    fpc.skip(frames);
	} else {
	    setFeedback("No FramePositioningControl!");
	}
    }

    // /////////////////////////// CONTROLS ///////////////////////////////////

    public boolean hasRateControl() {
	return getRateControl()!=null;
    }

    public boolean hasTempoControl() {
	return getTempoControl()!=null;
    }

    public boolean hasGUIControls() {
	// a player which provides VideoControl should also
	// always return GUIControl. Just to be sure...
	try {
	    if (player != null) {
		return ((player.getControl("GUIControl")!=null)
			|| (player.getControl("VideoControl")!=null));
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return false;
    }

    public Control[] getControls() {
	try {
	    if (player != null) {
		return player.getControls();
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    private VolumeControl getVolumeControl() {
	try {
	    if (player != null) {
		return (VolumeControl) player.getControl("VolumeControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    private TempoControl getTempoControl() {
	try {
	    if (player != null) {
		return (TempoControl) player.getControl("TempoControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    private RateControl getRateControl() {
	try {
	    if (player != null) {
		return (RateControl) player.getControl("RateControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    private PitchControl getPitchControl() {
	try {
	    if (player != null) {
		return (PitchControl) player.getControl("PitchControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    private MetaDataControl getMetaDataControl() {
	try {
	    if (player != null) {
		return (MetaDataControl) player.getControl("MetaDataControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    private FramePositioningControl getFramePositioningControl() {
	try {
	    if (player != null) {
		return (FramePositioningControl) player.getControl("FramePositioningControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    private StopTimeControl getStopTimeControl() {
	try {
	    if (player != null) {
		return (StopTimeControl) player.getControl("StopTimeControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    public VideoControl getVideoControl() {
	try {
	    if (player != null) {
		return (VideoControl) player.getControl("VideoControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    public RecordControl getRecordControl() {
	try {
	    if (player != null) {
		return (RecordControl) player.getControl("RecordControl");
	    }
	} catch (IllegalStateException ise) {
	    // thrown when player is closed
	}
	return null;
    }

    // /////////////////////////// PLAYBACK CONTROL ///////////////////////////

    private void assertPlayer() throws Throwable {
	String state="";
	try {
	    debugOut("assertPlayer");
	    fullScreen = false;
	    // make sure we can go back, even if failed
	    display.removeCommand(backCommand);
	    display.addCommand(backCommand);
	    if (player == null) {
		if (songInputStream == null) {
		    state="Opening "+songLocator; setStatus(state+"...");
		    player = Manager.createPlayer(songLocator);
		} else {
		    state="Opening "+songName; setStatus(state+"...");
		    player = Manager.createPlayer(songInputStream, songContentType);
		}
	    }
	    player.addPlayerListener(this);
	    //System.out.println("player = " + player);
	    state="Realizing"; setStatus(state+"...");
	    player.realize();
	    state="Prefetching"; setStatus(state+"...");
	    player.prefetch();
	    state="Prefetched"; setStatus(state);
	    if (USE_FORM_PLAYER_FOR_VIDEO) {
		// MAGIC: if there are any GUI controls, switch to Form mode
		// this is not nice design...
		if (hasGUIControls() && !(parent instanceof SimplePlayerForm)) {
		    System.gc();
		    state="Setting up GUI"; setStatus(state+"...");
		    Parent newParent = new SimplePlayerForm(parent.getTitle(), this, parent.getParent());
		    debugOut("created");
		    setParent(newParent);
		    parent.replaceCurrent(display);
		    state="GUI is set up."; setStatus(state);
		    debugOut("Changed to form-based player.");
		}
	    }
	    setPlayerCommands();
	    // notify the display class
	    parent.setupDisplay();

	    // use TITLE meta data if existant for display
	    MetaDataControl mc = getMetaDataControl();
	    if (mc != null) {
		int titleCount=0;
		String title = "";
		try {
		    title = mc.getKeyValue(MetaDataControl.TITLE_KEY);
		    if (title != null && title != "") {
			titleCount++;
			while (true) {
			    String n = mc.getKeyValue(MetaDataControl.TITLE_KEY+(titleCount+1));
			    if (n == null || n == "") {
				break;
			    }
			    titleCount++;
			}
		    }
		} catch (IllegalArgumentException me) {
		    // title key doesn't exist
		}
		// now number of titles is known
		if (titleCount > 0) {
		    songDisplayNames = new String[titleCount];
		    songDisplayNames[0] = title;
		    try {
			for (int i=1; i<titleCount; i++) {
			    songDisplayNames[i] = mc.getKeyValue(MetaDataControl.TITLE_KEY+(i+1));
			}
		    } catch (IllegalArgumentException me) {}
		    currSongDisplay = 0;
		}
		try {
		    String l = mc.getKeyValue(LYRICS_KEY);
		    if (l != null && l != "") {
			karaokeMode = true;
			karaokeLyrics = l;
		    }
		} catch (IllegalArgumentException me) {
		    // lyrics key doesn't exist
		}
	    }
	    durationUpdated();
	} catch (Throwable t) {
	    player=null;
	    setStatus("");
	    throw new MediaException(Utils.friendlyException(t)+" at "+state);
	}
    }


    public void startPlayer() {
	try {
	    debugOut("startPlayer");
	    if (player == null || player.getState() < Player.PREFETCHED) {
		assertPlayer();
	    }
	    if (player == null || player.getState() >= Player.STARTED) {
		return;
	    }
	    updateLoop();
	    // auto-rewind
	    try {
		long duration = player.getDuration();
		if (duration != Player.TIME_UNKNOWN && player.getMediaTime() >= duration) {
		    player.setMediaTime(0);
		}
	    } catch (MediaException e) {
		// nothing to do
	    }
	    if (player.getMediaTime() == 0) {
		setupKaraokeLines(0, 0);
		updateKaraoke();
	    } else {
		clearKaraoke();
	    }
	    setStatus("Starting...");
	    player.start();
	    setStatus("Playing");
	    setFeedback("");
	    // tempo may have changed due to new position
	    updateTempo(null);
	} catch (Throwable t) {
	    error(t);
	}
    }

    public void closePlayer() {
	if (player != null) {
	    setStatus("Stopping...");
	    try {
		player.stop();
	    } catch (Exception e) {}
	    setStatus("Closing...");
	    player.close();
	    setStatus("Closed");
	    player = null;
	    initialize();
	}
    }


    public void pausePlayer() {
	if (player != null) {
	    setStatus("Stopping...");
	    try {
		player.stop();
	    } catch (Exception e) {}
	    setStatus("Stopped");
	    setFeedback("");
	}
    }

    public void togglePlayer() {
	if (player!=null) {
	    if (player.getState() == Player.STARTED) {
		pausePlayer();
	    } else {
		startPlayer();
	    }
	}
    }

    /** fast forward or fast backward */
    public void skip(boolean backwards) {
	if (player != null) {
	    try {
		long mTime = player.getMediaTime();
		// default 10 sec
		long jump = 10000000;
		long duration = player.getDuration();
		if (duration >= 0) {
		    // skip 5%
		    jump = duration / 20;
		    if (jump < 2000000)
			jump = 2000000; // Skip at least 2 seconds
		}
		if (backwards) {
		    // Jump backward
		    setMediaTime(mTime-jump);
		} else {
		    // Jump forward
		    setMediaTime(mTime+jump);
		}
	    } catch (IllegalStateException ise) {
		// thrown when player is closed
		error(ise);
	    }
	}
    }

    public void stopAfterTime() {
	int delay = 5000000; // 5 seconds
	StopTimeControl stc = getStopTimeControl();
	if (stc != null) {
	    try {
		stc.setStopTime(StopTimeControl.RESET);
		stc.setStopTime(player.getMediaTime()+delay);
		setFeedback("Stop in "+(delay/1000000)+" seconds.");
	    } catch (IllegalStateException ise) {
		error(ise);
	    }
	} else {
	    setFeedback("No StopTimeControl!");
	}
    }

    public void changeRate(boolean slowdown) {
	int diff = 10000; // 10%
	if (slowdown) {
	    diff = -diff;
	}
	RateControl rc = getRateControl();
	if (rc != null) {
	    int ocr = rc.getRate();
	    int ncr = ocr + diff;
	    int maxRate = rc.getMaxRate();
	    if (maxRate > LIMIT_RATE*1000) {
		maxRate = LIMIT_RATE * 1000;
	    }
	    if (ncr >= rc.getMinRate() && ncr <= maxRate) {
		int ecr = rc.setRate(ncr);
		setFeedback("New rate: "+toFloatString(ecr, 3)+"%");
		updateRate(rc);
		// rate changes effective tempo.
		updateTempo(null);
	    }
	} else {
	    setFeedback("No RateControl!");
	}
    }

    public void setMediaTime(long time) {
	if (player == null) {
	    return;
	}
	try {
	    setFeedback("Set MediaTime to "+timeDisplay(time));
	    player.setMediaTime(time);
	    updateTime();
	    clearKaraoke();
	    updateTempo(null);
	} catch (Exception e) {
	    error(e);
	}
    }

    public void changeVolume(boolean decrease) {
	int diff = 10;
	if (decrease) {
	    diff = -diff;
	}
	VolumeControl vc = getVolumeControl();
	if (vc != null) {
	    int cv = vc.getLevel();
	    cv += diff;
	    vc.setLevel(cv);
	    updateVolume(vc);
	} else {
	    setFeedback("No VolumeControl!");
	}
    }

    public void toggleMute() {
	VolumeControl vc = getVolumeControl();
	if (vc != null) {
	    vc.setMute(!vc.isMuted());
	    updateVolume(vc);
	} else {
	    setFeedback("No VolumeControl!");
	}
    }

    public void transpose(boolean down) {
	int diff = 1000; // 1 semitone
	if (down) {
	    diff = -diff;
	}
	PitchControl pc = getPitchControl();
	if (pc != null) {
	    pc.setPitch(pc.getPitch()+diff);
	    updatePitch(pc);
	} else {
	    // if no PitchControl, use FramePositioningControl
	    if (getFramePositioningControl()!=null) {
		skipFrame(down);
	    } else {
		setFeedback("No PitchControl!");
	    }
	}
    }

    public void skipFrame(boolean back) {
	int diff = 1; // 1 frame
	if (back) {
	    diff = -diff;
	}
	FramePositioningControl fpc = getFramePositioningControl();
	if (fpc != null) {
	    int res = fpc.skip(diff);
	    updateTime();
	    setFeedback("Skipped: "+res+" frames to "+fpc.mapTimeToFrame(player.getMediaTime()));
	} else {
	    setFeedback("No FramePositioningControl!");
	}
    }

    private void queryRecording() {
	try {
	    // display the screen to enter the locator
	    Utils.query("Enter a record locator", recordLocator, 200, TextField.URL, new RecordTask(), parent);
	} catch (Exception e) {
	    Utils.error(e, parent);
	}
    }

    private void startRecording(String locator) {
	try {
	    if (locator == null || locator == "") {
		Utils.error("No locator set!", parent);
	    } else {
		// user entered the locator.
		recordLocator = locator;
		//Start recording
		RecordControl rc = getRecordControl();
		if (rc != null) {
		    rc.stopRecord();
		    rc.reset();
		    rc.setRecordLocation(locator);
		    rc.startRecord();
		} else {
		    throw new MediaException("Could not get RecordControl!");
		}
	    }
	} catch (Exception e) {
	    Utils.error(e, parent);
	}
    }

    private void stopRecording() {
	try {
	    // Stop recording
	    final RecordControl rc = getRecordControl();
	    if (rc != null) {
		rc.stopRecord();
        // The commit() must be performed in a thread that is not this one:
        // we are running on the event dispatcher thread.
        // rc.commit() MIGHT display a security dialog (depending on the URL
        // that the audio is being recorded to) and the MIDP spec recommends 
        // performing actions that could bring up a security dialog
        // on a thread that is NOT the event dispatcher thread.
        new Thread(new Runnable() {
            public void run() {
                try {
                    rc.commit();
                    Utils.FYI("Recorded "+recordLocator+" successfully.", parent);
                } catch (Exception e) {
                    Utils.error(e, parent);
                }
            }
        }).start();
	    } else {
		throw new MediaException("Could not get RecordControl!");
	    }
	} catch (Exception e) {
	    Utils.error(e, parent);
	}
    }

    private void showHelp() {
	// only available for canvas player
	if (parent instanceof SimplePlayerCanvas) {
	    ((SimplePlayerCanvas) parent).showHelp();
	}
    }

    // /////////////////////////// EVENT HANDLERS /////////////////////////////

    public void commandAction(Command c, Displayable s) {
	if (c == backCommand) {
	    goBack();
	}
	else if (c == muteCommand || c == unmuteCommand) {
	    mutePressed();
	} else if (c == volCommand) {
	    volPressed();
	} else if (c == metaCommand) {
	    metaPressed();
	} else if (c == loopCommand) {
	    loopPressed();
	} else if (c == stcCommand) {
	    stopAfterTime();
	} else if (c == playCommand || c == stopCommand) {
	    togglePlayer();
	} else if (c == skipFwCommand) {
	    skip(false);
	} else if (c == skipBwCommand) {
	    skip(true);
	} else if (c == rewindCommand) {
	    setMediaTime(0);
	} else if (c == rateCommand) {
	    ratePressed();
	} else if (c == tempoCommand) {
	    tempoPressed();
	} else if (c == pitchCommand) {
	    pitchPressed();
	} else if (c == fullScreenCommand) {
	    fullScreenPressed();
	} else if (c == normalScreenCommand) {
	    normalScreenPressed();
	} else if (c == startRecordCommand) {
	    queryRecording();
	} else if (c == stopRecordCommand) {
	    stopRecording();
	} else if (c == helpCommand) {
	    showHelp();
	} else if (s != display) {
	    // e.g. when list item in MetaData display list is pressed
	    goBack();
	}
    }


    private synchronized void startDisplayTimer() {
	if (timeDisplayTask == null) {
	    timeDisplayTask = new SPTimerTask();
	    guiTimer.scheduleAtFixedRate(timeDisplayTask, 0, timerInterval);
	}
    }

    private synchronized void stopDisplayTimer() {
	if (timeDisplayTask != null) {
	    timeDisplayTask.cancel();
	    timeDisplayTask = null;
	    updateTime();
	}
    }


    public void playerUpdate(Player plyr, String evt, Object evtData) {
	try {
	    // special case: end-of-media, but loop count>1 !
	    if (evt == END_OF_MEDIA && plyr.getState() == Player.STARTED) {
		setFeedback("Looping");
		return;
	    }
	    if (evt == CLOSED
		|| evt == ERROR
		|| evt == END_OF_MEDIA
		|| evt == STOPPED_AT_TIME
		|| evt == STOPPED)  {
		stopDisplayTimer();
	    }
	    if (evt == END_OF_MEDIA  || evt == STOPPED_AT_TIME
	        || evt == STOPPED || evt == ERROR)  {
		display.removeCommand(stopCommand);
		display.addCommand(playCommand);
		changeSongDisplayCounter = 0;
		currSongDisplay = 0;
		updateSongDisplay();
	    }
	    // Sun-specific event for karaoke lyrics
	    if (evt.equals("com.sun.midi.lyrics")) {
		// META data
		byte[] data=(byte[]) evtData;
		if (data!=null && (evtData instanceof byte[]) && data.length>0) {
		    if (Utils.DEBUG) System.out.println("META event 0x"+Integer.toHexString(data[0] & 0xFF));
		    switch (data[0]) {
		    case 0x01:  // Text (commonly used for Karaoke, but not sent if Player is in Karaoke mode)
			// fall through
		    case 0x05:  // Lyrics (isn't this meant for Karaoke ??)
			if (karaokeMode) {
			    break;
			}
			// fall through if not Karaoke
		    case 0x06: // marker
			// fall through
		    case 0x07: // Cue point
			String text = new String(data, 1, data.length-1);
			setFeedback(text);
			if (Utils.DEBUG) System.out.println("META event 0x"+Integer.toHexString(data[0] & 0xFF)+": "+text);
			break;
		    case 0x51: // Tempo
			updateTempo(null);
			break;
		    case LYRICS_EVENT: // inofficial lyrics event: data 1-3 pos, 4-6 length
			int kPos = (data[1] << 16) | (data[2] << 8) | (data[3] & 0xFF);
			int kLen = (data[4] << 16) | (data[5] << 8) | (data[6] & 0xFF);
			setupKaraokeLines(kPos, kLen);
			updateKaraoke();
			break;
			//case 0x58: // Time Signature
			//case 0x59: // Key Signature
			//case 0x7F: // Proprietary
		    }
		}
	    }

	    if (evt == STARTED) {
		if (songDisplayNames.length > 1) {
		    changeSongDisplayCounter = SONG_DISPLAY_COUNTER * 2;
		}
		startDisplayTimer();
		display.addCommand(stopCommand);
		display.removeCommand(playCommand);
	    } else if (evt == DEVICE_UNAVAILABLE) {
		setFeedback("Audio device not available!");
	    } else if (evt == BUFFERING_STARTED) {
		setFeedback("Buffering started");
	    } else if (evt == BUFFERING_STOPPED) {
		setFeedback("Buffering stopped");
	    } else if (evt == CLOSED) {
		setFeedback("Closed");
	    } else if (evt == DURATION_UPDATED) {
		setFeedback("Duration updated");
		durationUpdated();
	    } else if (evt == END_OF_MEDIA) {
		setStatus("End of media.");
		setFeedback("");
	    } else if (evt == ERROR) {
		setFeedback("Error: " + ((String) evtData));
	    } else if (evt == RECORD_STARTED) {
		isRecording=true;
		display.addCommand(stopRecordCommand);
		display.removeCommand(startRecordCommand);
		setFeedback("Recording Started");
	    } else if (evt == RECORD_STOPPED) {
		isRecording=false;
		display.addCommand(startRecordCommand);
		display.removeCommand(stopRecordCommand);
		setFeedback("Recording Stopped");
	    } else if (evt == SIZE_CHANGED) {
		VideoControl vc = (VideoControl)evtData;
		setFeedback("Resize to " + vc.getDisplayWidth() + "x" + vc.getDisplayHeight());
	    } else if (evt == STOPPED_AT_TIME) {
		setStatus("Stopped at time.");
		setFeedback("");
	    } else if (evt == VOLUME_CHANGED) {
		VolumeControl vc = (VolumeControl) evtData;
		setFeedback("New volume: " + vc.getLevel());
		updateVolume(vc);
	    }
	} catch (Throwable t) {
	    if (Utils.DEBUG) System.out.println("Uncaught Exception in SimplePlayerGUI.playerUpdate()");
	    error(t);
	}
    }

    public void itemStateChanged(Item item) {
	if (item!=null) {
	    if (item == gauge) {
		switch (gaugeMode) {
		case GAUGE_VOLUME:
		    VolumeControl vc = getVolumeControl();
		    if (vc != null) {
			vc.setLevel(gauge.getValue());
			updateVolume(vc);
		    }
		    break;
		case GAUGE_RATE:
		    RateControl rc = getRateControl();
		    if (rc != null) {
			rc.setRate((gauge.getValue() * 1000) + rc.getMinRate());
			updateRate(rc);
		    }
		    break;
		case GAUGE_TEMPO:
		    TempoControl tc = getTempoControl();
		    if (tc != null) {
			tc.setTempo((gauge.getValue() + 1) * 1000);
			updateTempo(tc);
		    }
		    break;
		case GAUGE_PITCH:
		    PitchControl pc = getPitchControl();
		    if (pc != null) {
			pc.setPitch((gauge.getValue() * 1000) + pc.getMinPitch());
			updatePitch(pc);
		    }
		    break;
		} // switch
	    }
	}
    }


    // ///////////////// PAUSE / RESUME ///////////////////////// //

    /**
     * Stop the player and the display thread.
     * Some VM's may stop players and threads
     * on their own, but for consistent user
     * experience, it's a good idea to explicitly
     * stop and start resources such as player
     * and threads.
     */
    public synchronized void pauseApp() {
	if (player != null && player.getState() >= Player.STARTED) {
	    // player was playing, so stop it.
	    try {
		player.stop();
	    } catch (MediaException e) {
		// nothing we can do here
	    }
	    // make sure that the timer does not keep on
	    // displaying (we're not sure that the STOPPED
	    // event will reach us before the VM becomes
	    // suspended)
	    stopDisplayTimer();
	    // make sure to restart upon resume
	    restartOnResume = true;
	} else {
	    restartOnResume = false;
	}
    }


    /**
     * If the player was playing when the MIDlet was paused,
     * then the player will be restarted here.
     */
    public synchronized void resumeApp() {
	if (player != null && restartOnResume) {
	    try {
		player.start();
	    } catch (MediaException me) {
		error(me);
	    }
	    // player.start() will trigger the display timer
	    // to be started, so we don't need to explicitely
	    // call startDisplayTimer().
	}
	restartOnResume = false;
    }


    // ///////////////// INNER CLASSES /////////////////// //

    /**
     * Inner class for handling the query listener
     * when entering the recording URL
     */
    private class RecordTask implements Utils.QueryListener {
	private RecordTask() { }

	public void start(String location) {
	    startRecording(location);
	}

	// interface Utils.QueryListener
	// used for entering the filename of a recording
	public void queryOK(String text) {
	    start(text);
	}

	public void queryCancelled() {
	    // don't do anything if query is cancelled.
	    // the previous visible Displayable will be
	    // displayed automatically
	}

    }

    /**
     * The timer task that will be called every timerInterval
     * milliseconds
     */
    private class SPTimerTask extends TimerTask {
	public void run() {
	    updateTime();
	    if (redisplayKaraokeCounter>0) {
		redisplayKaraokeCounter--;
		if (redisplayKaraokeCounter == 0) {
		    displayNextKaraokePhrase();
		}
	    }
	    if (changeSongDisplayCounter > 0 && songDisplayNames.length>0) {
		changeSongDisplayCounter--;
		if (changeSongDisplayCounter == 0) {
		    currSongDisplay = (currSongDisplay + 1) % songDisplayNames.length;
		    updateSongDisplay();
		    changeSongDisplayCounter = SONG_DISPLAY_COUNTER;
		    if (currSongDisplay == 0) {
			changeSongDisplayCounter *= 2;
		    }
		}
	    }
	}
    }


    // ////////////////// INNER INTERFACES ///////////////////// //

    /**
     * This interface is implemented by the Displayable
     * which uses this class. It provides the main screen.
     */
    interface Parent extends Utils.BreadCrumbTrail {
	public Utils.BreadCrumbTrail getParent();
	public String getTitle();
	// callbacks for display matters of the main player screen
	public void setupDisplay(); // called after the media is prefetched
	public void setStatus(String s);
	public void setFeedback(String s);
	public void setFileTitle(String s);
	public void updateKaraoke();
	public void updateTime();
	public void updateRate(); // and tempo
	public void updateDisplay();
	public void fullScreen(boolean value);
    }
}

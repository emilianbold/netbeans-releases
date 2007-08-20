/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package example.mmademo;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.util.*;
import java.io.*;

/**
 * An example MIDlet for a generic Player for audio, MIDI media
 * The top-level URLs are configured in the jad file:<br>
 * <code>PlayerURL-n</code> defines the n'th URL<br>
 * <code>PlayerTitle-n</code> defines the n'th title
 * <p>
 * Special URL "protocols" can be used:<br>
 * <code>resource:</code> for media data from the jar<br>
 * <code>rms:</code> for media data from RMS memory<br>
 * <p>
 * Examples:<br>
 * <code>
 * PlayerURL-1:   http://server.com/ <br>
 * PlayerTitle-1: Browse server.com <br>
 * PlayerURL-2:   resource:/audio/hello.wav <br>
 * PlayerTitle-2: hello.wav from jar <br>
 * PlayerURL-3:   http://localhost/movie.mpg <br>
 * PlayerTitle-3: Funny movie <br>
 * PlayerURL-4:   rms:/ <br>
 * PlayerTitle-4: Browse Record Stores<br>
 * PlayerURL-5:   capture://audio <br>
 * PlayerTitle-5: Capture Audio from default device<br>
 * </code>
 *
 * @version 1.5
 */
public class SimplePlayer extends BaseListMidlet implements Utils.BreadCrumbTrail, Utils.QueryListener {

    private static final String manualEnterURL="manual";
    private static final String manualEnterTitle="[enter URL]";
    private String lastManualURL = "";

    private Vector titles;
    private Vector urls;
    
    /* This can be put in the Utils file if necessary */
    private static final String httpSupportedList = getSupportedList("http");
    private static final String allSupportedList = getSupportedList(null);

    private static String getSupportedList(String protocol) {
	String list[] = Manager.getSupportedContentTypes(protocol);
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < list.length; i++) {
	    buffer.append(list[i]).append(",");
	}
	if (list.length > 0)
	    return buffer.toString();
	else
	    return "";
    }

    // SimplePlayerCanvas is the default player
    private SimplePlayerCanvas simplePlayerCanvas = new SimplePlayerCanvas("MMAPI Player", this);

    private Utils.ContentHandler[] handlers = {
	new SimpleHttpBrowser("MMAPI HTTP Browser", this),
	new SimpleRmsBrowser("MMAPI RMS Browser", this),
	simplePlayerCanvas,
	//new SimplePlayerForm("MMAPI Player", this), // will be instantiated by SimplePlayer
    };

    public SimplePlayer() {
	super("MMAPI Player");
    }

    /* This can be put in the Utils file if necessary */
    protected final static boolean isSupported(String url) {
	if (!url.startsWith("capture:")) {
	    String contentType;
	    try {
		contentType = Utils.guessContentType(url);
	    } catch (Exception e) {
		contentType = "";
	    }
	    if (contentType.equals(""))
		return false;
	    if (  url.startsWith("resource:") ) {
		/**
		 * The MMAPI spec doesn't provide an api to
		 * query if a contentType is supported via
		 * InputStream. So we cannot handle the case
		 * where a contentType is supported in http
		 * protocol but not supported via InputStream.
		 * This will not be the case for Sun's MMAPI
		 * implementation.
		 * Even if this is the case in another MMAPI implementation,
		 * the player creation will fail with a message that
		 * the player couldn't be created.
		 */
		if (allSupportedList.indexOf(contentType + ",") == -1)
		    return false;
	    } else if (url.startsWith("http:")) {
		if (httpSupportedList.indexOf(contentType + ",") == -1)
		    return false;
	    }
	}
	return true;
    }

    protected void fillList(List list) {
	titles = new Vector();
	urls = new Vector();
	for (int n = 1; n < 100; n++) {
	    String nthURL = "PlayerURL-"+ n;
	    String url = getAppProperty(nthURL);
	    if (url == null || url.length() == 0) {
		break;
	    }
	    if (!isSupported(url))
		continue;
	    String nthTitle = "PlayerTitle-" + n;
	    String title = getAppProperty(nthTitle);
	    if (title == null || title.length() == 0) {
		title = url;
	    }
	    titles.addElement(title);
	    urls.addElement(url);
	    list.append(title, null);
	}
	// TODO: add MRU list here

	// manual enter of URL
	titles.addElement(manualEnterTitle);
	urls.addElement(manualEnterURL);
	list.append(manualEnterTitle, null);

	list.addCommand(exitCommand);
	list.addCommand(selectCommand);
    }

    /**
     * Main function that decides which class to display
     * dependent on the url.
     */
    public void handle(String title, String url) {
	try {
	    InputStream is = null;
	    String ct = "";
	    // first magic URL's
	    if (url.equals(manualEnterURL)) {
		enterURL();
		return;
	    }
	    else if (url.startsWith("resource:")) {
		is = getClass().getResourceAsStream(url.substring(9));
        is.mark(is.available());
	    }
	    else if (SimpleRmsBrowser.isRmsFile(url)) {
		is = SimpleRmsBrowser.getRecordStoreStream(url);
	    }
	    // ringtone text file ?
	    if (Utils.guessContentType(url).equals("audio/x-txt")) {
		RingToneConverter rtc;
		if (is != null) {
		    rtc = new RingToneConverter(is, title);
		} else {
		    rtc = new RingToneConverter(url, title);
		}
		Player tonePlayer = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
		tonePlayer.realize();
		ToneControl tc = (ToneControl) tonePlayer.getControl("ToneControl");
		tc.setSequence(rtc.getSequence());
		// do NOT prefetch this player. Otherwise SimplePlayerGUI will not initialize this player
		go(simplePlayerCanvas);
		simplePlayerCanvas.handle(rtc.getName(), tonePlayer);
		return;
	    }
	    if (is != null) {
		if (ct == "") {
		    ct = Utils.guessContentType(url);
		}
		go(simplePlayerCanvas);
		simplePlayerCanvas.handle(title, is, ct);
		return;
	    } else {
		for (int i=0; i<handlers.length; i++) {
		    if (handlers[i].canHandle(url)) {
			go((Displayable) handlers[i]);
			handlers[i].handle(title, url);
			return;
		    }
		}
	    }
	    Utils.error("No handler available!", this);
	} catch (Exception e) {
	    Utils.error(e, this);
	}
    }

    protected void selectCommand(int index) {
	if (index>=0) {
	    if (index<titles.size()) {
		handle((String) titles.elementAt(index), (String) urls.elementAt(index));
	    }
	}
    }

    /**
     * Display a prompt for a new URL
     */
    private void enterURL() {
	Utils.query("Enter a URL", lastManualURL, 300, TextField.URL, this, this);
    }

    // //////////////////// interface Utils.QueryListener /////////////////// //
    public void queryOK(String text) {
	handle(text, text);
	lastManualURL = text;
    }

    public void queryCancelled() {
	// don't do anything if query is cancelled.
	// the previous visible Displayable will be
	// displayed automatically
    }


    // //////////////////// interface MIDlet /////////////////// //
    public void destroyApp(boolean unconditional) {
	Utils.ContentHandler p = getCurrentHandler();
	if (p != null) {
	    p.close();
	}
	super.destroyApp(unconditional);
    }

    // util method
    private Utils.ContentHandler getCurrentHandler() {
	Displayable d = getDisplay().getCurrent();
	if (d instanceof Utils.ContentHandler) {
	    return (Utils.ContentHandler) d;
	}
	return null;
    }

    // for debugging
    public String toString() {
	return "SimplePlayer";
    }

}

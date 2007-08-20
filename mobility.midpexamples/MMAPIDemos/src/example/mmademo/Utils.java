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
 * Utility functions and listener interfaces
 *
 * @version 1.5
 */
public class Utils {
    public static boolean DEBUG = false;

    private Utils() {
	//prevent accidental instantiation
    }

    public static void debugOut(String s) {
	if (DEBUG) System.out.println(s);
    }

    public static void debugOut(Throwable t) {
	if (DEBUG) System.out.println(t.toString());
	if (DEBUG) t.printStackTrace();
    }

    public static void error(Throwable t, BreadCrumbTrail bct) {
	if (DEBUG) t.printStackTrace();
	error(friendlyException(t), bct);
    }

    public static void error(String s, BreadCrumbTrail bct) {
	Alert alert = new Alert("Error", s, null, AlertType.ERROR);
	alert.setTimeout(Alert.FOREVER);
	bct.replaceCurrent(alert);
    }

    public static void FYI(String s, BreadCrumbTrail bct) {
	Alert alert = new Alert("FYI", s, null, AlertType.INFO);
	alert.setTimeout(Alert.FOREVER);
	bct.replaceCurrent(alert);
    }

    /**
     * "javax.microedition.rms.RecordStoreException: Bla"
     * ->
     * "RecordStoreException: Bla"
     */
    public static String friendlyException(Throwable t) {
	if (t instanceof MediaException && t.getMessage().indexOf(" ")>5) {
	    return t.getMessage();
	}
	String s = t.toString();
	while (true) {
	    int dot = s.indexOf(".");
	    int space = s.indexOf(" "); if (space<0) space = s.length();
	    int colon = s.indexOf(":"); if (colon<0) colon = s.length();
	    if (dot >= 0 && dot < space && dot < colon) {
		s = s.substring(dot+1);
	    } else {
		break;
	    }
	}
	return s;
    }

    /**
     * Prompts the user to enter a string.<p>
     * <b>Caution</b>: this must be called in
     * a different Thread than the lcdui event dispatcher. Unfortunately !
     *
     * @param title - title of the query window
     * @param def - a default value that appears in the input field
     * @param maxSize - max number of characters
     * @return the entered text, or <code>null</code> if the user cancelled
     */
    public static void query(String title, String def, int maxSize,
			     QueryListener listener, BreadCrumbTrail bct) {
	query(title, def, maxSize, TextField.ANY, listener, bct);
    }

    /**
     * Prompts the user to enter a string. <p>
     * When the user finished entering the text,
     * the <code>listener</code>'s methods are called.
     * If the user pressed the OK button, the
     * <code>listener</code>'s <code>queryOK</code> method
     * is called with the entered text as parameter.
     *
     * The <code>listener</code> need not care about hiding the displayable.
     *
     * @param title - title of the query window
     * @param def - a default value that appears in the input field
     * @param maxSize - max number of characters
     * @param constraints - see javax.microedition.lcdui.TextBox
     * @param listener - the QueryListener which receives the events
     */
    public static void query(String title, String def, int maxSize, int constraints,
			     QueryListener listener, BreadCrumbTrail bct) {
	TextBox tb = new TextBox(title, def, maxSize, constraints);
	tb.addCommand(QueryTask.cancelCommand);
	tb.addCommand(QueryTask.OKCommand);
	QueryTask qt = new QueryTask(listener, bct);
	tb.setCommandListener(qt);
	bct.go(tb);
    }

    /**
     * splits the URL in the parts
     * E.g: http://www.12fb.com:80/Media/MIDI/fb.mid#1
     *
     * 0: protocol (e.g. http)
     * 1: host (e.g. www.12fb.com)
     * 2: port (e.g. 80)
     * 3: path (e.g. /Media/MIDI)
     * 4: file (e.g. fb.mid)
     * 5: anchor (e.g. 1)
     *
     * LIMITATION: URL must end with a slash if it is a directory
     */
    public static String[] splitURL(String url) throws Exception {
	StringBuffer u=new StringBuffer(url);
	String[] result=new String[6];
	for (int i=0; i<=5; i++) {
	    result[i]="";
	}
	// get protocol
	boolean protFound=false;
	int index=url.indexOf(":");
	if (index>0) {
	    result[0]=url.substring(0, index);
	    u.delete(0, index+1);
	    protFound=true;
	}
	else if (index==0) {
	    throw new Exception("url format error - protocol");
	}
	// check for host/port
	if (u.length()>2 && u.charAt(0)=='/' && u.charAt(1)=='/') {
	    // found domain part
	    u.delete(0, 2);
	    int slash=u.toString().indexOf('/');
	    if (slash<0) {
		slash=u.length();
	    }
	    int colon=u.toString().indexOf(':');
	    int endIndex=slash;
	    if (colon>=0) {
		if (colon>slash) {
		    throw new Exception("url format error - port");
		}
		endIndex=colon;
		result[2]=u.toString().substring(colon+1, slash);
	    }
	    result[1]=u.toString().substring(0, endIndex);
	    u.delete(0, slash);
	}
	// get filename
	if (u.length()>0) {
	    url=u.toString();
	    int slash=url.lastIndexOf('/');
	    if (slash>0) {
		result[3]=url.substring(0, slash);
	    }
	    if (slash<url.length()-1) {
		String fn = url.substring(slash+1, url.length());
		int anchorIndex = fn.indexOf("#");
		if (anchorIndex>=0) {
		    result[4] = fn.substring(0, anchorIndex);
		    result[5] = fn.substring(anchorIndex+1);
		} else {
		    result[4] = fn;
		}
	    }
	}
	return result;
    }

    public static String mergeURL(String[] url) {
	return ((url[0]=="")?"":url[0]+":/")
	    +((url[1]=="")?"":"/"+url[1])
	    +((url[2]=="")?"":":"+url[2])
	    +url[3]+"/"+url[4]
	    +((url[5]=="")?"":"#"+url[5]);
    }

    public static String guessContentType(String url) throws Exception {
	// guess content type
	String[] sURL = splitURL(url);
	String ext = "";
	String ct = "";
	int lastDot = sURL[4].lastIndexOf('.');
	if (lastDot>=0) {
	    ext = sURL[4].substring(lastDot+1).toLowerCase();
	}
	if (ext.equals("mpg") || url.equals("avi")) {
	    ct = "video/mpeg";
	} else if (ext.equals("mid") || ext.equals("kar")) {
	    ct = "audio/midi";
	} else if (ext.equals("wav")) {
	    ct = "audio/x-wav";
	} else if (ext.equals("jts")) {
	    ct = "audio/x-tone-seq";
	} else if (ext.equals("txt")) {
	    ct = "audio/x-txt";
	} else if (ext.equals("amr")) {
	    ct = "audio/amr";
	} else if (ext.equals("awb")) {
	    ct = "audio/amr-wb";
	} else if (ext.equals("gif")) {
	    ct = "image/gif";
	}
	return ct;
    }

    /**
     * From SortDemo - modified to take Strings
     *
     * This is a generic version of C.A.R Hoare's Quick Sort
     * algorithm.  This will handle arrays that are already
     * sorted, and arrays with duplicate keys.<BR>
     *
     * If you think of a one dimensional array as going from
     * the lowest index on the left to the highest index on the right
     * then the parameters to this function are lowest index or
     * left and highest index or right.  The first time you call
     * this function it will be with the parameters 0, a.length - 1.
     *
     * @param s       a String array
     * @param lo0     left boundary of array partition
     * @param hi0     right boundary of array partition
     */
    private static void quickSort(String[] s, int lo0, int hi0) {
	int lo = lo0;
	int hi = hi0;
	String mid;

	if (hi0 > lo0) {

	    /* Arbitrarily establishing partition element as the midpoint of
	     * the array.
	     */
	    mid = s[(lo0 + hi0) / 2 ].toUpperCase();

	    // loop through the array until indices cross
	    while(lo <= hi) {
		/* find the first element that is greater than or equal to
		 * the partition element starting from the left Index.
		 */
		while((lo < hi0) && (s[lo].toUpperCase().compareTo(mid) < 0)) {
		    ++lo;
		}

		/* find an element that is smaller than or equal to
		 * the partition element starting from the right Index.
		 */
		while((hi > lo0) && (s[hi].toUpperCase().compareTo(mid) > 0)) {
		    --hi;
		}

		// if the indexes have not crossed, swap
		if(lo <= hi) {
		    String temp;
		    temp = s[lo];
		    s[lo] = s[hi];
		    s[hi] = temp;
		    ++lo;
		    --hi;
		}
	    }

	    /* If the right index has not reached the left side of array
	     * must now sort the left partition.
	     */
	    if (lo0 < hi) {
		quickSort(s, lo0, hi);
	    }

	    /* If the left index has not reached the right side of array
	     * must now sort the right partition.
	     */
	    if (lo < hi0) {
		quickSort(s, lo, hi0);
	    }

	}
    }

    public static void sort(String[] elements) {
	quickSort(elements, 0, elements.length - 1);
    }

    /**
     * A class to handle the query
     */
    private static class QueryTask implements CommandListener, Runnable {
	private static Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);
	private static Command OKCommand = new Command("OK", Command.OK, 1);

	// "parameters" passed to commandAction method
	private QueryListener queryListener;
	private BreadCrumbTrail queryBCT;
	private static String queryText = "";

        private QueryTask(QueryListener listener, BreadCrumbTrail bct) {
	    this.queryListener = listener;
	    this.queryBCT = bct;
	}

    /**
     * Respond to commands
     */
    public void commandAction(Command c, Displayable s) {
	if (queryBCT != null) {
	    Utils.debugOut("Utils.commandAction: goBack()");
	    queryBCT.goBack();
	}
	if (c == cancelCommand) {
	    Utils.debugOut("Command: cancel");
	    if (queryListener!=null) {
		queryListener.queryCancelled();
	    }
	}
	else if (c == OKCommand) {
	    Utils.debugOut("Command: OK");
	    if (queryListener!=null) {
		queryText = "";
		if (s instanceof TextBox) {
		    queryText = ((TextBox) s).getString();
		}
		// for some reasons, MIDP may have a deadlock
		// if some lengthy operation (i.e. http i/o)
		// is initiated from the command listener
		// thread. Therefore, issue the event
		// from another thread...
		(new Thread(this)).start();
	    }
	}
    }

    /**
     * Runnable implementation -- sends
     * the event to the listener.
     * It is executed in a separate thread
     * to not block the VM's command dispatch
     * thread.
     */
    public void run() {
	sendListenerEvent();
    }

    /**
     * Send the text to the query listener
     */
    private void sendListenerEvent() {
	if (queryListener != null) {
	    queryListener.queryOK(queryText);
	}
    }
}

    /**
     * Interface to be implemented by classes
     * that provide <i>Back</i> functionality.
     */
    interface BreadCrumbTrail {
	public Displayable go(Displayable d);
	public Displayable goBack();
	public void handle(String name, String url);
	public Displayable replaceCurrent(Displayable d);
	public Displayable getCurrentDisplayable();
    }

    /**
     * Interface implemented by classes that
     * can handle (playback, display, etc.) url's.
     */
    interface ContentHandler {
	public void close();
	public boolean canHandle(String url);
	public void handle(String name, String url);
    }

    /**
     * Interface that is implemented by classes
     * that use the query() functions.
     */
    interface QueryListener {
	public void queryOK(String text);
	public void queryCancelled();
    }

    /**
     * Interface implemented by Displayable's that
     * want to respond to the MIDlet's startApp()
     * and pauseApp() calls.
     */
    interface Interruptable {
	/**
	 * Called in response to a request to pause the MIDlet.
	 */
	public void pauseApp();
	
	/**
	 * Called when a MIDlet is asked to resume operations
	 * after a call to pauseApp(). This method is only
	 * called after pauseApp(), so it is different from
	 * MIDlet's startApp().
	 */
	public void resumeApp();
    }
}

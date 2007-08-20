/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package example.mmademo;

import java.io.*;
import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.microedition.media.*;

/**
 * An http link browser. Used in SimplePlayer
 *
 * @version 1.2
 */
public class SimpleHttpBrowser extends List
    implements CommandListener, Utils.ContentHandler, Utils.QueryListener {

    private static final boolean MASS_TEST=false;

    private Command exitCommand = new Command("Exit Browser", Command.EXIT, 1);
    private Command backCommand = new Command("Back", Command.BACK, 1);
    private Command openCommand = new Command("Open", Command.ITEM, 1);
    private Command selectCommand = new Command("Select", Command.ITEM, 1);
    private Command saveCommand = new Command("Save in RMS", Command.ITEM, 1);
    private Command menuCommand = new Command("Menu", Command.ITEM, 1);
    private Command refreshCommand = new Command("Refresh", Command.ITEM, 1);

    private Command allCommands[] = {
	exitCommand,
	backCommand,
	openCommand,
	selectCommand,
	saveCommand,
	menuCommand,
	refreshCommand
    };

    private Vector names = new Vector();
    private Vector urls = new Vector();
    private Stack history = new Stack();
    private Stack historyIndex = new Stack();
    private String currURL = null;

    // "global" variables for user queries
    private String queryDefault;
    private String queryInputURL;

    private Utils.BreadCrumbTrail parent;

    private List menuList;

    public SimpleHttpBrowser(String title, Utils.BreadCrumbTrail parent) {
	super(title, Choice.IMPLICIT);
	this.parent = parent;
    }

    public void displayHTML(String url, int selectedIndex) {
	boolean error = false;
	currURL = url;
	clearLists();
	try {
	    readHTML(url);
	} catch (Exception e) {
	    append("["+Utils.friendlyException(e)+"]", null);
	    error = true;
	}
	addCommand(backCommand);
	setCommandListener(this);
	if (!error) {
	    for (int i=0; i<names.size(); i++) {
		append((String) names.elementAt(i), null);
	    }
	    setListIndex(selectedIndex);
	    addCommand(menuCommand);
	} else {
	    addCommand(refreshCommand);
	}
	if (MASS_TEST) {
	    massTest();
	}
    }

    private void exit() {
	currURL = null;
	clearLists();
	parent.goBack();
    }

    private static void internalError(Throwable t, String desc) {
	if (Utils.DEBUG) if (desc!="") System.out.println(desc);
	if (Utils.DEBUG) t.printStackTrace();
    }

    private void clearLists() {
	names.setSize(0);
	urls.setSize(0);
	for (int i=size()-1; i>=0; i--) {
	    delete(i);
	}
	// remove commands, just to be sure that they
	// don't appear erroneously
	removeCommand(refreshCommand);
	removeCommand(menuCommand);
	// good moment to collect garbage
	System.gc();
    }

    // interface Utils.ContentHandler
    public void close() {
	clearLists();
	history.setSize(0);
	historyIndex.setSize(0);
    }

    // interface Utils.ContentHandler
    public boolean canHandle(String url) {
	return isHTML(url);
    }

    // interface Utils.ContentHandler
    public void handle(String name, String url) {
	Utils.debugOut("SimpleHttpBrowser: handle "+url);
	displayHTML(url, 0);
    }

    /**
     * Respond to commands
     */
    public void commandAction(Command c, Displayable s) {
	try {
	    // respond to menu items
	    if ((s == menuList) && (menuList != null) && menuList.isShown()) {
		int selIndex = menuList.getSelectedIndex();
		parent.goBack();
		if (c == backCommand) {
		    return;
		}
		if (c == List.SELECT_COMMAND || c == selectCommand) {
		    c = menuItem2Command(selIndex);
		    // fall through - the commands will then be handled as if
		    // they came from the main list
		}
	    }
	    if ((c == List.SELECT_COMMAND && isShown()) || (c == openCommand)) {
		gotoURL(getSelectedIndex());
	    }
	    else if (c == saveCommand) {
		// save to RMS must be called in a different thread !
		saveToRms((String) urls.elementAt(getSelectedIndex()));
	    }
	    else if (c == backCommand) {
		goBack();
	    }
	    else if (c == refreshCommand) {
		refresh();
	    }
	    else if (c == menuCommand) {
		showMenu(getSelectedIndex());
	    }
	    else if (c == exitCommand) {
		exit();
	    }
	} catch (Throwable t) {
	    internalError(t, "in commandAction");
	}
    }

    private Command menuItem2Command(int index) {
	// go through all commands and test if their label
	// matches this menuList item's string
	if ((index < 0) || (index >= menuList.size())) {
	    return null;
	}
	String menuStr = menuList.getString(index);
	for (int i = 0; i < allCommands.length; i++) {
	    if (allCommands[i].getLabel().equals(menuStr)) {
		return allCommands[i];
	    }
	}
	return null;
    }

    private void showMenu(int index) {
	String url=(String) urls.elementAt(index);
	boolean html = isHTML(url);
	menuList = new List("Menu", Choice.IMPLICIT);
	menuList.setCommandListener(this);
	menuList.append(openCommand.getLabel(), null);
	menuList.append(refreshCommand.getLabel(), null);
	if (!html) {
	    // do not show "Save to RMS" for html/directory links
	    menuList.append(saveCommand.getLabel(), null);
	}
	menuList.append(exitCommand.getLabel(), null);
	menuList.addCommand(backCommand);
	menuList.addCommand(selectCommand);
	parent.go(menuList);
    }

    private void gotoURL(int index) {
	String url=(String) urls.elementAt(index);
	gotoURL(url, index);
    }

    private void gotoURL(String url, int index) {
	try {
	    if (index>=names.size() || isHTML(url)) {
		if (currURL != null) {
		    history.push(currURL);
		    historyIndex.push(new Integer(index));
		}
		displayHTML(url, 0);
	    } else {
		parent.handle((String) names.elementAt(index), url);
	    }
	} catch (Exception e) {
	    Utils.error(e, parent);
	}
	if (Utils.DEBUG) {
	    Utils.debugOut("SimpleHttpBrowser: after gotoURL. History contains "+history.size()+" entries.");
	    for (int i = history.size()-1; i>=0; i--) {
		Utils.debugOut("     "+i+": "+((String) history.elementAt(i)));
	    }
	}
    }

    private void goBack() {
	if (Utils.DEBUG) {
	    Utils.debugOut("SimpleHttpBrowser: before goBack. History contains "+history.size()+" entries.");
	    for (int i = history.size()-1; i>=0; i--) {
		Utils.debugOut("     "+i+": "+((String) history.elementAt(i))+"  #"+((Integer) historyIndex.elementAt(i)));
	    }
	}
	if (!history.empty()) {
	    String url=(String) history.pop();
	    int index=((Integer) historyIndex.pop()).intValue();
	    displayHTML(url, index);
	} else {
	    exit();
	}
    }

    private void refresh() {
	int selIndex = getSelectedIndex();
	Utils.debugOut("SimpleHttpBrowser.Refresh: index "+selIndex);
	displayHTML(currURL, selIndex);
    }

    // somehow this doesn't work if there was a screen switch before !
    private void setListIndex(int index) {
	if (index>=0 && index<size()) {
	    setSelectedIndex(index, true);
	}
    }

    ////////////////////// interface Utils.QueryListener /////////////////////
    public void queryOK(String text) {
	try {
	    boolean queryAgain = true;
	    // if text is null, then queryOK was called in order
	    // to display the query for the first time.
	    if (text != null) {
		if (text.indexOf("/")>=0 || text.indexOf(":")>=0) {
		    Utils.error("record store name cannot contain / or :", parent);
		}
		else if (text.length()>32) {
		    Utils.error("record store name cannot exceed 32 characters", parent);
		}
		else if (text.length()==0) {
		    Utils.error("record store name empty. please try again", parent);
		} else {
		    queryAgain = false;
		}
	    }
	    if (queryAgain) {
		Utils.query("Enter record store name:", queryDefault, 32, this, parent);
	    } else {
		int id = saveToRms(queryInputURL, "rms:/"+text);
		Utils.FYI("The file was saved successfully in RMS. The record ID is "+id+".", parent);
	    }
	} catch (Throwable t) {
	    Utils.error(t, parent);
	}
    }

    public void queryCancelled() {
	// don't do anything if query is cancelled.
	// the previous visible Displayable will be
	// displayed automatically
    }

    private void saveToRms(String inputURL) {
	try {
	    String[] splitInputURL = Utils.splitURL(inputURL);
	    queryDefault = ""; // instance variable, to be used in queryOK handler
	    for (int i=4; i>0; i--) {
		queryDefault = splitInputURL[i];
		if (queryDefault != "") {
		    break;
		}
	    }
	    queryInputURL = inputURL; // instance variable, to be used in queryOK handler
	    // call queryOK with null, so that it only displays the prompt
	    queryOK(null);
	} catch (Throwable t) {
	    Utils.error(t, parent);
	}
    }

    private static int saveToRms(String inputURL, String outputURL) throws IOException, RecordStoreException, Exception {
	InputStream is = Connector.openInputStream(inputURL);
	return SimpleRmsBrowser.saveToRecordStore(is, outputURL);
    }


    // main parsing function
    private void readHTML(String source) throws Exception {
	InputStream in=Connector.openInputStream(source);
	try {
	    String url;
	    String name;
	    String[] base=Utils.splitURL(source);
	    Utils.debugOut("readHTML: source="+Utils.mergeURL(base));
	    while ((url = readHref(in)) != null) {
		String[] splitU=joinURLs(base, url);
		url=Utils.mergeURL(splitU);
		// do not include those sort links in file listings.
		if (splitU[4].indexOf('?')!=0) {
		    name=readHrefName(in);
		    //if (TRACE) System.out.println("Read name=\""+name+"\" with url=\""+url+"\"");
		    names.addElement(name);
		    urls.addElement(url);
		}
	    }
	    if (names.size()==0) {
		throw new Exception("No links found in "+source);
	    }
	} finally {
	    in.close();
	}
    }

    // URL methods
    private static boolean isHTML(String url) {
	try {
	    String[] sURL = Utils.splitURL(url);
	    return sURL[0].equals("http")
		&& (sURL[4]=="" // no filename part
		    || sURL[4].indexOf(".html") == sURL[4].length()-5
		    || sURL[4].indexOf(".htm") == sURL[4].length()-4);
	} catch (Exception e) {
	    internalError(e, "isHTML()");
	    return false;
	}
    }

    private String[] joinURLs(String[] url, String relPath) throws Exception {
	String[] rel=Utils.splitURL(relPath);
	String[] result=new String[6];
	result[0]=(rel[0]=="")?url[0]:rel[0];
	result[1]=(rel[1]=="")?url[1]:rel[1];
	result[2]=(rel[2]=="")?url[2]:rel[2];
	if (rel[3].length()>0) {
	    if (rel[3].charAt(0)=='/') {
		// absolute path given
		result[3]=rel[3];
	    } else {
		result[3]=url[3]+'/'+rel[3];
	    }
	} else {
	    result[3]=url[3];
	}
	result[4]=(rel[4]=="")?url[4]:rel[4];
	result[5]=(rel[5]=="")?url[5]:rel[5];
	return result;
    }


    // beware: highly optimized HTML parsing code ahead !

    private boolean charEquals(char char1, char char2, boolean caseSensitive) {
	boolean equal=(char1==char2);
	if (!equal && !caseSensitive
	    && ((char1>=0x41 && char1<=0x5A) || (char1>=0x41 && char1<=0x5A))) {
	    equal=((char1^0x20)==char2);
	}
	return equal;
    }


    private boolean skip(InputStream is, String until, boolean onlyWhiteSpace, boolean caseSensitive) throws Exception {
	//if (TRACE) System.out.println("skip(is, \""+until+"\", onlyWhiteSpace="+onlyWhiteSpace+", caseSensitive="+caseSensitive+")");
	int len=until.length();
	int found=0;
	int v=is.read();
	while (v>0) {
	    if (v==0) {
		// binary data
		throw new Exception("no html file");
	    }
	    boolean equal=charEquals((char) v, until.charAt(found), caseSensitive);
	    //if (TRACE) System.out.println("Read '"+((char) v)+"' found="+found+"  equal="+equal);
	    if (!equal) {
		if (onlyWhiteSpace && v>32) {
		    throw new Exception("incorrect data format");
		}
		if (found>0) {
		    found=0;
		    continue;
		}
	    } else {
		found++;
	    }
	    if (found==len) {
		return true;
	    }
	    v=is.read();
	}
	return false;
    }

    // if a character other than white space is found, it is returned
    private int findDelimiter(InputStream is) throws Exception {
	//if (TRACE) System.out.println("findDelimiter(is)");
	while (true) {
	    int v=is.read();
	    if (v==-1) {
		return -1;
	    }
	    if (v==0) {
		// binary data
		throw new Exception("no html file");
	    }
	    if (v>32) {
		return v;
	    }
	}
    }

    private String readString(InputStream is,
			      char firstChar,
			      String delim,
			      boolean delimCaseSensitive) throws Exception {
	//if (TRACE) System.out.println(">readString(is, "
	//			      +firstChar+", delim=\""
	//			      +delim+"\", delimCaseSensitive="+delimCaseSensitive+")");
	StringBuffer sb=new StringBuffer();
	boolean lastWhiteSpace=false;
	if (firstChar!=0) {
	    sb.append(firstChar);
	    lastWhiteSpace=(firstChar<=32);
	}
	int v;
	boolean inTag=false;
	int found=0;
	int len=delim.length();
	int appendedInDelim=0;
	while (true) {
	    v=is.read();
	    if (v==-1) {
		throw new Exception("unterminated string");
	    }
	    if (v<=32) {
		// whitespace
		if (lastWhiteSpace) {
		    continue;
		}
		v=32;
		lastWhiteSpace=true;
	    } else {
		lastWhiteSpace=false;
		if (v=='<') {
		    inTag=true;
		}
	    }
	    boolean equal=charEquals((char) v, delim.charAt(found), delimCaseSensitive);
	    //if (TRACE) System.out.println("ReadString '"+((char) v)+"' found="+found+"  equal="+equal);
	    if (!equal) {
		if (found>0) {
		    found=0;
		    appendedInDelim=0;
		    equal=charEquals((char) v, delim.charAt(found), delimCaseSensitive);
		}
	    }
	    if (equal) {
		found++;
		if (found==len) {
		    if (appendedInDelim>0) {
			sb.setLength(sb.length()-appendedInDelim);
		    }
		    break;
		}
	    }
	    if (!inTag) {
		sb.append((char) v);
		// when we are inside the delimiter, we want to get rid of the delimiter later
		// so track it
		if (found>0) {
		    appendedInDelim++;
		}
	    }
	    else if (v=='>') {
		inTag=false;
	    }
	}
	//if (TRACE) System.out.println("<readString()=\""+sb.toString()+"\"");
	return sb.toString();
    }


    /**
     * Simplified parser to find xyz of a <a href="xyz">blablabla</a> statement
     */
    private String readHref(InputStream is) throws Exception {
	//if (TRACE) System.out.println(">readHref()");
	// first skip everything until "<a"
	if (!skip(is, "<a", false, false)) {
	    return null;
	}
	// read "href"
	if (!skip(is, "href", false, false)) {
	    return null;
	}
	// read until "="
	if (!skip(is, "=", true, true)) {
	    return null;
	}
	// wait for " or ' or nothing
	int delim=findDelimiter(is);
	char endDelim=(char) delim;
	char firstChar=0;
	if (delim!='"' && delim!='\'') {
	    // url not enclosed in quotes
	    endDelim='>';
	    firstChar=(char) delim;
	}
	String ret=readString(is, firstChar, ""+endDelim, true);
	if (firstChar==0) {
	    if (!skip(is, ">", true, true)) {
		return null;
	    }
	}
	//if (TRACE) System.out.println("<readHref()="+ret);
	return ret;
    }

    /**
     * Simplified parser to find blabla of a <a href="xyz">blablabla</a> statement
     */
    private String readHrefName(InputStream is) throws Exception {
	// the stream is at first char after >. We just read the string until we find "</a>"
	String ret=readString(is, (char) 0, "</a>", false);
	return ret;
    }

    // debugging: open all files in current directory
    private void massTest() {
	if (MASS_TEST) {
	    String name="";
	    for (int i=0; i<urls.size(); i++) {
		try {
		    String url=(String) urls.elementAt(i);
		    name=(String) names.elementAt(i);
		    if (!isHTML(url)) {
			System.out.print(name+"...");
			Player p=Manager.createPlayer(url);
			try {
			    System.out.print("realize...");
			    p.realize();
			    System.out.print("prefetch...");
			    p.prefetch();
			} finally {
			    System.out.print("close...");
			    p.close();
			    System.out.print("deallocate...");
			    p.deallocate();
			    System.out.println("done");
			}
		    }
		} catch (IOException ioe) {
		    System.out.println("IOException: "+name);
		    System.out.println(ioe.toString());
		} catch (MediaException me) {
		    System.out.println("MediaException: "+name);
		    System.out.println(me.toString());
		} catch (Throwable t) {
		    System.out.println("...Throwable: "+name);
		    System.out.println(t.toString());
		}
	    }
	}
    }

    // for debugging
    public String toString() {
	return "SimpleHttpBrowser";
    }
}

/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package example.mmademo;

import java.io.*;
import java.util.*;
import javax.microedition.rms.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;

/**
 * A RecordStore (RMS) browser. Used in SimplePlayer.
 * <p>
 * Pseudo URLs are used:<br>
 * <code>rms:/RecordStoreName#index</code>
 * <p>
 * Examples:<br>
 * <code>rms:/TheStorage#3</code> - to get index 3 in RecordStore named &quot;TheStorage&quot;<br>
 * <code>rms:/</code> - to list all RecordStores<br>
 * <code>rms:/TheStorage</code> - to list the indexes in RecordStore named &quot;TheStorage&quot;<br>
 *
 * @version 1.2
 */
class SimpleRmsBrowser extends List implements CommandListener, Utils.ContentHandler {

    private Command backCommand = new Command("Back", Command.BACK, 1);
    private Command selCommand = new Command("Select", Command.ITEM, 1);
    private Command delCommand = new Command("Delete", Command.ITEM, 1);

    // if this is set, then display indexes inside RecordStore
    private int lastSelectedRecord;
    private RecordStore currStore;
    private String[] names;
    private Utils.BreadCrumbTrail parent;

    public SimpleRmsBrowser(String title, Utils.BreadCrumbTrail parent) {
	super(title, Choice.IMPLICIT);
	this.parent = parent;
    }

    public void display(String url, int selectedIndex) {
	clearLists();
	try {
	    String[] sURL = splitURL(url);
	    // don't allow different protocols, or a host/port
	    if (!canHandle(sURL)) {
		throw new Exception("Invalid rms URL");
	    }
	    if (sURL[5]!="") {
		throw new Exception("is a record, not a store");
	    }
	    if (sURL[4]!="") {
		currStore = getRecordStore(sURL, false);
	    }
	    if (currStore != null) {
		RecordEnumeration re = currStore.enumerateRecords(null, null, false);
		names = new String[re.numRecords()];
		for (int i=0; i<names.length; i++) {
		    try {
			int id = re.nextRecordId();
			names[i] = String.valueOf(id);
			append(names[i]+" - "+currStore.getRecordSize(id)+" bytes", null);
		    } catch (InvalidRecordIDException irie) {
			append("[invalid record ID]", null);
		    }
		}
		if (size() == 0) {
		    throw new Exception("no records");
		}
	    } else {
		names = RecordStore.listRecordStores();
		if (names != null && names.length>0) {
		    for (int i=0; i<names.length; i++) {
			append(names[i], null);
		    }
		}
		if (size() == 0) {
		    throw new Exception("No record stores!");
		}
	    }
	} catch (Throwable t) {
	    append("["+Utils.friendlyException(t)+"]", null);
	}
	setListIndex(selectedIndex);
	addCommand(backCommand);
	addCommand(selCommand);
	addCommand(delCommand);
	setCommandListener(this);
    }

    private void setListIndex(int index) {
	if (index>=0 && index<size()) {
	    setSelectedIndex(index, true);
	}
    }

    private void goBack() {
	if (currStore != null) {
	    // display all record stores
	    display("rms:/", lastSelectedRecord);
	} else {
	    exit();
	}
    }

    private void exit() {
	clearLists();
	parent.goBack();
    }

    private void clearLists() {
	if (currStore != null) {
	    try {
		currStore.closeRecordStore();
	    } catch (Exception e) {
		Utils.debugOut(e);
	    }
	    currStore = null;
	}
	names = null;
	for (int i=size()-1; i>=0; i--) {
	    delete(i);
	}
	// good moment to garbage collect
	System.gc();
    }

    // interface Utils.ContentHandler
    public void close() {
	clearLists();
    }

    // interface Utils.ContentHandler
    public boolean canHandle(String url) {
	try {
	    Utils.debugOut("SimpleRmsBrowser.canHandle: isValidRmsURL("+url+") = "+isValidRmsURL(splitURL(url)));
	    return canHandle(splitURL(url));
	} catch (Exception e) {}
	return false;
    }

    // interface Utils.ContentHandler
    public void handle(String name, String url) {
	Utils.debugOut("SimpleRmsBrowser: handle "+url);
	display(url, 0);
    }

    private static boolean canHandle(String[] sURL) {
	try {
	    return isValidRmsURL(sURL)
		&& sURL[5] == "";		// anchor (contains index in recordstore)
	} catch (Exception e) {}
	return false;
    }

    private static boolean isValidRmsURL(String[] sURL) {
	boolean res = sURL[0].equals("rms") 			// protocol
	    && sURL[1] == ""			// host
	    && sURL[2] == ""			// port
	    && (sURL[3] == "" || sURL[3].equals("/"));	// path
	// if index is set, then record store name must be set, too
	if (res && sURL[5]!="" && sURL[4]=="") {
	    res = false;
	}
	return res;
    }

    /**
     * @return true if the URL points to a file, i.e. Record Store name + Index
     */
    public static boolean isRmsFile(String url) {
	try {
	    String[] sURL = splitURL(url);
	    return isValidRmsURL(sURL) && sURL[4]!="" && sURL[5]!="";
	} catch (Exception e) {}
	return false;
    }

    private static String[] splitURL(String url) throws Exception {
	String[] sURL = Utils.splitURL(url);
	// if filename is empty, but path exists, then
	// the path is actually the filename (name of record store)
	if (sURL[4]=="" && sURL[3]!="") {
	    sURL[4] = sURL[3];
	    sURL[3] = "";
	}
	if (sURL[4].startsWith("/")) {
	    sURL[4] = sURL[4].substring(1);
	}
	return sURL;
    }

    /**
     * Respond to commands
     */
    public void commandAction(Command c, Displayable s) {
	try {
	    if (((c == List.SELECT_COMMAND) || c == selCommand) && isShown()) {
		select(getSelectedIndex());
	    }
	    else if (c == backCommand) {
		goBack();
	    }
	    else if (c == delCommand) {
		deleteRecord(getSelectedIndex());
	    }
	} catch (Throwable t) {
	    //
	}
    }

    private void select(int index) {
	if (names==null || index<0 || index>=names.length) {
	    goBack();
	}
	if (currStore != null) {
	    try {
		String name = currStore.getName()+"#"+names[index];
		parent.handle(name, "rms:/"+name);
	    } catch (Exception e) {
		Utils.error(e, parent);
	    }
	} else {
	    lastSelectedRecord = index;
	    display("rms:/"+names[index], 0);
	}
    }

    private void deleteRecord(int index) {
	if (names==null || index<0 || index>=names.length) {
	    // error message ?
	    return;
	}
	String name="";
	String disp="";
	try {
	    if (currStore != null) {
		disp = "rms:/"+currStore.getName();
		name = "Record "+names[index];
		int i = Integer.parseInt(names[index]);
		currStore.deleteRecord(i);
	    } else {
		name = "Record Store "+names[index];
		disp = "rms:/";
		RecordStore.deleteRecordStore(names[index]);
	    }
	    Utils.FYI(name+" successfully deleted", parent);
	} catch (Exception e) {
	    Utils.error(e, parent);
	}
	display(disp, 0);
    }

    /////////////////////// RecordStore utilities ////////////////////////////////////////

	/**
	 * Callers must make sure to call closeRecordStore() !
	 */
	public static RecordStore getRecordStore(String url, boolean canCreate) throws Exception {
	    Utils.debugOut("url = "+url+" can create = "+canCreate);
	    return getRecordStore(splitURL(url), canCreate);
	}

    private static RecordStore getRecordStore(String[] sURL, boolean canCreate) throws Exception {
	if (!isValidRmsURL(sURL)) {
	    throw new Exception("Invalid rms URL");
	}
	try {
	    Utils.debugOut("Trying to open Record Store "+sURL[4]+" can create = "+canCreate);
	    return RecordStore.openRecordStore(sURL[4], canCreate);
	} catch (RecordStoreNotFoundException rsnfe) {
	    throw new Exception("Recordstore not found");
	}
    }

    private static int getRecordStoreIndex(String[] sURL) throws Exception {
	if (!isValidRmsURL(sURL)) {
	    throw new Exception("Invalid rms URL");
	}
	int result = -1;
	try {
	    result = Integer.parseInt(sURL[5]);
	    if (result<0) {
		throw new NumberFormatException();
	    }
	} catch (NumberFormatException nfe) {
	    throw new Exception("invalid record store index");
	}
	return result;
    }

    // throws Exception if url is malformed
    public static int saveToRecordStore(InputStream is, String url)
	throws IOException, RecordStoreException, Exception {
	RecordStore rs = getRecordStore(splitURL(url), true);
	int ret = 0;
	try {
	    ret = saveToRecordStore(is, rs);
	} finally {
	    rs.closeRecordStore();
	}
	return ret;
    }

    public static int saveToRecordStore(InputStream is, RecordStore rs)
	throws IOException, RecordStoreException {
	byte[] buffer = new byte[1024];
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	while (true) {
	    int read = is.read(buffer);
	    if (read < 0) {
		// finished reading
		break;
	    }
	    baos.write(buffer, 0, read);
	}
	buffer = baos.toByteArray();
	return rs.addRecord(buffer, 0, buffer.length);
    }

    public static InputStream getRecordStoreStream(String url)
	throws RecordStoreException, Exception {
	Utils.debugOut("getRecordStoreStream("+url+")");
	return getRecordStoreStream(splitURL(url));
    }

    private static InputStream getRecordStoreStream(String[] sURL)
	throws RecordStoreException, Exception {
	InputStream is = null;
	RecordStore rs = getRecordStore(sURL, false);
	try {
	    int index = getRecordStoreIndex(sURL);
	    is = getRecordStoreStream(rs, index);
	} finally {
	    rs.closeRecordStore();
	}
	return is;
    }

    public static InputStream getRecordStoreStream(RecordStore rs, int index)
	throws RecordStoreException, Exception {
	byte[] buffer = rs.getRecord(index);
	return new ByteArrayInputStream(buffer);
    }

    // for debugging
    public String toString() {
	return "SimpleRmsBrowser";
    }
}

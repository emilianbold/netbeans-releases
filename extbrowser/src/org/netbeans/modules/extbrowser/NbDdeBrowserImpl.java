/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.*;
import javax.swing.*;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.options.SystemOption;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.netbeans.modules.httpserver.*;

/**
 *
 *
 * @author  rkubacki
 * @version 
 */
public class NbDdeBrowserImpl extends ExtBrowserImpl {

    /** Netscape DDE topic names */
    public static final String BEGIN_PROGRESS     = "WWW_BeginProgress";     // NOI18N
    public static final String MAKING_PROGRESS    = "WWW_MakingProgress";    // NOI18N
    public static final String END_PROGRESS       = "WWW_EndProgress";       // NOI18N
    public static final String CANCEL_PROGRESS    = "WWW_CancelProgress";    // NOI18N
    public static final String SET_PROGRESS_RANGE = "WWW_SetProgressRange";  // NOI18N
    
    static {
        try {
            if (org.openide.util.Utilities.isWindows ())
                System.loadLibrary("extbrowser");   // NOI18N
        } catch(Exception e) {
            System.out.println(java.util.ResourceBundle.getBundle("org/netbeans/modules/extbrowser/Bundle").getString("ERR_cant_locate_dll"));
            e.printStackTrace ();
        }
    }
    
    private static final boolean debug = true;
    
    private static java.util.ResourceBundle bundle = 
        java.util.ResourceBundle.getBundle("org/netbeans/modules/extbrowser/Bundle"); // NOI18N

    /** reference to creator */
    private WinWebBrowser winBrowserFactory;
    
    /** name of DDE server receiving URLEcho notifications */
    private String  ddeUrlEchoSrvName = "NETBEANSURL";    // NOI18N
    
    private Thread  urlEchoThread = null;

    /** native thread that displays URLs */
    private Thread nativeThread = null;
    
    /** runnable class that implements the work of nativeThread */
    private NbDdeBrowserImpl.URLDisplayer nativeRunnable = null;
    
    /** name of DDE server receiving progress */
    private String  ddeProgressSrvName;
    boolean bProgressInitialized = false;
    
    /** transaction ID for progress comunication, 
     *  it is incremented for each new reported progress 
     */
    protected int   txID = 0;
    
    /** windowID of servicing window (-1 if there is no assocciated window) */
    private int     currWinID = -1;
    
    /** Creates new UnixBrowserImpl */
    public NbDdeBrowserImpl (WinWebBrowser winBrowserFactory) {
        super ();
        currWinID = -1;
        this.winBrowserFactory = winBrowserFactory;
    }
    
    // native private void createDDE (String name, String topic)
    // throws NbBrowserException;

    native private byte [] reqDdeMessage (String srv, String topic, String item, int timeout)
    throws NbBrowserException;
    
    /** finds registry entry for browser opening */
    native private String getBrowserPath (String browser)
    throws NbBrowserException;

    /** returns the command that executes default application for opening of 
     *  .html files
     */
    native private String getDefaultOpenCommand ()
    throws NbBrowserException;

    /** creates service for WWW_*Progress* topics */
    /*
    private void initProgress () {
        try {
            if (!bProgressInitialized) {
                ddeProgressSrvName = "NETBEANSPROGRESS";    // NOI18N
                String topic = BEGIN_PROGRESS+","+SET_PROGRESS_RANGE+","+MAKING_PROGRESS+","+END_PROGRESS;
                createDDE (ddeProgressSrvName, topic);
                bProgressInitialized = true;
            }
        }
        catch (NbBrowserException ex) {
            ex.printStackTrace();
        }
    }
     */
    
    
    /** This should navigate browser back. Actually does nothing.
     */
    public void backward() {
        return;
    }
    
    /** This should navigate browser forward. Actually does nothing.
     */
    public void forward() {
        return;
    }

    /** Is backward button enabled?
     * @return true if it is
     */
    public boolean isBackward() {
        return false;
    }
    
    /** Is forward button enabled?
     * @return true if it is
     */
    public boolean isForward() {
        return false;
    }
    
    /** Is history button enabled?
     * @return true if it is
     */
    public boolean isHistory() {
        return false;
    }
    
    /** Reloads current html page.
     */
    public void reloadDocument() {
        setURL (getURL ());
    }

    /** Sets current URL.
     *
     * @param url URL to show in the browser.
     */
    public synchronized void setURL(final URL url) {
        try {
            if (nativeThread == null) {
                nativeRunnable = new NbDdeBrowserImpl.URLDisplayer ();
                nativeThread = new Thread(nativeRunnable, "URLdisplayer");
            }
        
            nativeRunnable.url = url;
            nativeThread.run ();    // PENDING: should be nativeThread.start ();
            // PENDING: add this timeout to browser properties
            nativeThread.join(10000);
            if (nativeThread.isAlive()) {
                // something is wrong
                nativeThread.stop();
                TopManager.getDefault().notify(
                new NotifyDescriptor.Message(bundle.getString("MSG_win_browser_invocation_failed"),
                NotifyDescriptor.INFORMATION_MESSAGE)
                );
            }
        }
        catch (InterruptedException ex) {
            TopManager.getDefault().getErrorManager().notify( ErrorManager.INFORMATIONAL, ex);
        }
    }

    /** Invoked when the history button is pressed.
     */
    public void showHistory() {
    }

    /** Stops loading of current html page.
     */
    public void stopLoading() {
        // if we have current progress in NETSCAPE we can send CANCEL_PROGRESS topic
    }
    
    /** returns internal http server port number */
    int getInternalServerPort () throws NbBrowserException {
        HttpServerSettings setting = (HttpServerSettings)SystemOption.findObject (HttpServerSettings.class);
        if (setting == null)
            throw new NbBrowserException ("Cannot get Httpserver port number");
        
        return setting.getPort ();
    }

    private String getDDEServerName () {
        String srv = winBrowserFactory.getDDEServer ();
        if (srv != null)
            return srv;
        
        try {
            String cmd = getDefaultOpenCommand ();
            if (cmd != null) {
                if (cmd.toUpperCase ().indexOf (ExtBrowserSettings.IEXPLORE) >= 0)
                    return ExtBrowserSettings.IEXPLORE;

                if (cmd.toUpperCase ().indexOf (ExtBrowserSettings.NETSCAPE) >= 0)
                    return ExtBrowserSettings.NETSCAPE;
            }
        }
        catch (Exception ex) {
            // some problem in native code likely
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ex.printStackTrace ();
        }
        // guess IE
        return ExtBrowserSettings.IEXPLORE;
    }
    
    class URLDisplayer implements Runnable { // NOI18N
        
        /** url to be displayed */
        URL url;
        
        public void run() {
            try {
                // internal protocols cannot be displayed in external viewer
                if (isInternalProtocol(url.getProtocol())) {
                    url = WrapperServlet.createHttpURL(url);
                }
                else {
                    url = url;
                }
                byte [] data;
                boolean hasNoWindow = (currWinID == -1);

                // initProgress ();

                String winID;
                // activate browser window (doesn't work on Win9x)
                if (!win9xHack ()) {
                    // IE problem
                    if (getDDEServerName().equals(ExtBrowserSettings.IEXPLORE))
                        winID = "0xFFFFFFFF";
                    else
                        winID = "0x00000000"+Integer.toHexString(hasNoWindow? 0: currWinID).toUpperCase(); // NOI18N
                    if (winID.length() > 10) winID = "0x"+winID.substring(winID.length()-8); // NOI18N

                    try {
                        data = reqDdeMessage(getDDEServerName(),"WWW_Activate",winID,5000);
                    }
                    catch (NbBrowserException ex) {
                        startBrowser ();
                        data = reqDdeMessage(getDDEServerName(),"WWW_Activate",winID,5000);
                        hasNoWindow = false;
                    }
                    
                    if (data != null && data.length >= 4) {
                        currWinID=DdeBrowserSupport.getDWORDAtOffset(data, 0);
                        setStatusMessage(bundle.getString("MSG_use_win")+currWinID);
                    }
                    else {
                        currWinID = -1;
                        setStatusMessage(bundle.getString("ERR_cant_activate_browser"));
                        return;
                    }
                }
                else {
                    data = null;
                }


                if (getDDEServerName().equals(ExtBrowserSettings.IEXPLORE)) {
                    winID = (hasNoWindow && !win9xHack())? "0": "-1";
                }
                else
                    winID = "0x00000000"+Integer.toHexString(hasNoWindow? 0: currWinID).toUpperCase(); // NOI18N
                if (winID.length() > 10) winID = "0x"+winID.substring(winID.length()-8); // NOI18N

                // nbfs can be displayed internally and in ext. viewer too
                String args1;
                args1="\""+url.toString()+"\",,"+winID+",0x1,,,"+(ddeProgressSrvName==null?"":ddeProgressSrvName);  // NOI18N
                if (!win9xHack ()) {
                    data = reqDdeMessage(getDDEServerName(),"WWW_OpenURL",args1,3000); // NOI18N
                }
                else {
                    // we've skipped WWW_Activate step so we need to start it if it doesn't run 
                    try {
                        data = reqDdeMessage(getDDEServerName(),"WWW_OpenURL",args1,3000); // NOI18N
                    }
                    catch (NbBrowserException ex) {
                        startBrowser ();
                        data = reqDdeMessage(getDDEServerName(),"WWW_OpenURL",args1,3000); // NOI18N
                    }
                }
                    
                if (data != null && data.length >= 4) {
                    if (!getDDEServerName().equals("IEXPLORE")) {
                        currWinID=DdeBrowserSupport.getDWORDAtOffset(data, 0);
                        if (currWinID < 0) currWinID = -currWinID;
                    }
                    setStatusMessage(bundle.getString("MSG_use_win"));
                }
                URL oldUrl = NbDdeBrowserImpl.this.url;
                NbDdeBrowserImpl.this.url = url;
                pcs.firePropertyChange(PROP_URL, oldUrl, url);
            }
            catch (Exception ex) {
                final Exception ex1 = ex;
                TopManager.getDefault().getErrorManager().annotate(ex1, bundle.getString("MSG_win_browser_invocation_failed"));
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        TopManager.getDefault().notifyException(ex1);
                    }
                });
            }
        }
        
        /**
         * Checks for IExplorer & Win9x combination.
         */
        private boolean win9xHack () {
            return getDDEServerName().equals(ExtBrowserSettings.IEXPLORE)
                   && (Utilities.getOperatingSystem() == Utilities.OS_WIN98 
                      ||  Utilities.getOperatingSystem() == Utilities.OS_WIN95);
        }

        /** 
         * Utility function that tries to start new browser process.
         *
         * It is used when WWW_Activate or WWW_OpenURL fail
         */
        private void startBrowser() throws NbBrowserException, java.io.IOException, InterruptedException {
            String b;
            if (ExtBrowserSettings.OPTIONS.isStartWhenNotRunning()) {
                // get the browser
                if (winBrowserFactory.getExecutable() != null
                &&  !winBrowserFactory.getExecutable().equals("")) { // NOI18N
                    b = winBrowserFactory.getExecutable();
                }
                else {
                    b = (winBrowserFactory.getDDEServer()!=null)?
                    getBrowserPath(getDDEServerName()):
                        getDefaultOpenCommand();
                }
                
                // start it
                if (b != null) {
                    // check quoted path
                    if (b.charAt(0) == '"') {
                        int from, to;
                        from = b.indexOf('"'); to = b.indexOf('"', from+1);
                        b = b.substring(from+1, to);
                    }
                    else {
                        StringTokenizer st = new StringTokenizer(b);
                        b = st.nextToken();
                    }
                    // start IE with -nohome
                    if (ExtBrowserSettings.IEXPLORE.equals(getDDEServerName())) {
                        b += " -nohome";    // NOI18N
                    }
                    
                    setStatusMessage(bundle.getString("MSG_Running_command")+b);
                    Runtime.getRuntime().exec(b);
                    // wait for browser start
                    Thread.currentThread().sleep(7000);
                }
            }
        }
    }
}
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
import java.util.Timer;
import java.util.TimerTask; 
import java.util.Vector;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
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
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Message(java.util.ResourceBundle.getBundle("org/netbeans/modules/extbrowser/Bundle").getString("ERR_cant_locate_dll"),
                NotifyDescriptor.INFORMATION_MESSAGE)
            );
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
    private static Thread nativeThread = null;
    
    /** runnable class that implements the work of nativeThread */
    private static NbDdeBrowserImpl.URLDisplayer nativeRunnable = null;
    
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
    native static String getBrowserPath (String browser)
    throws NbBrowserException;

    /** returns the command that executes default application for opening of 
     *  .html files
     */
    native static String getDefaultOpenCommand ()
    throws NbBrowserException;

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
        if (nativeThread == null) {
            nativeRunnable = new NbDdeBrowserImpl.URLDisplayer ();
            nativeThread = new Thread(nativeRunnable, "URLdisplayer");
            nativeThread.start ();
        }

        nativeRunnable.postTask (new DisplayTask (url, this));
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

    /** Finds the name of DDE server. 
     *  If <Default system browser> is set then it resolves it into either 
     *  Netscape or IExplore
     */
    private String realDDEServer () {
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
                
                if (cmd.toUpperCase ().indexOf (ExtBrowserSettings.MOZILLA) >= 0)
                    return ExtBrowserSettings.MOZILLA;
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
    
    
    /**
     * Singleton for doing all DDE operations.
     */
    static class URLDisplayer implements Runnable { // NOI18N

        /** FIFO of urls that should be displayed */
        Vector tasks;
        
        /** flag for quiting of this thread */
        boolean doProcessing = true;
        
        /** This is set to true during displaying of URL. 
         *  Used by Timer to interrupt displaying and print error message 
         */
        boolean isDisplaying = false;

        private URLDisplayer () {
            tasks = new Vector ();
        }
        
        private void postTask (DisplayTask task) {
            synchronized (this) {
                boolean shouldNotify = tasks.isEmpty ();
                tasks.add (task);
            
                if (shouldNotify) {
                    notifyAll();
                }
            }
        }
        
        /**
         * Returns next URL from queue that was posted for displaying.
         * This method blocks other processing until there is an request
         */
        private synchronized DisplayTask getNextTask () throws InterruptedException {
            do {
                if (!tasks.isEmpty ())
                    return (DisplayTask)tasks.remove (0);
                
                wait ();
            }
            while (true);
        }
        
        public void run() {
            while (doProcessing) {
                try {
                    /** url to be displayed */
                    DisplayTask task = getNextTask ();
                   
                    isDisplaying = true;
                    Timer timer = new Timer ();
                    timer.schedule (new TimerTask () {
                        public void run() {
                            if (isDisplaying) {
                                NbDdeBrowserImpl.nativeThread.interrupt ();
                                TopManager.getDefault().notify(
                                new NotifyDescriptor.Message(bundle.getString("MSG_win_browser_invocation_failed"),
                                NotifyDescriptor.INFORMATION_MESSAGE)
                                );
                            }
                        }
                    }, 11000);  // PENDING: add this timeout to browser properties
                    dispatchURL (task);
                }
                catch (InterruptedException ex) {
                    // do nothing
                }
                finally {
                    isDisplaying = false;
                }
            }
        }

        public void dispatchURL (DisplayTask task) {
            try {
                URL url = task.url;
                
                // internal protocols cannot be displayed in external viewer
                if (isInternalProtocol(url.getProtocol())) {
                    url = WrapperServlet.createHttpURL(url);
                }
                else {
                    url = url;
                }
                byte [] data;
                boolean hasNoWindow = (task.browser.currWinID == -1);

                // initProgress ();

                String winID;
                // activate browser window (doesn't work on Win9x)
                if (!win9xHack (task) && !mozillaHack (task)) {
                    // IE problem
                    if (task.browser.realDDEServer().equals(ExtBrowserSettings.IEXPLORE))
                        winID = "0xFFFFFFFF";
                    else
                        winID = "0x00000000"+Integer.toHexString(hasNoWindow? 0: task.browser.currWinID).toUpperCase(); // NOI18N
                    if (winID.length() > 10) winID = "0x"+winID.substring(winID.length()-8); // NOI18N

                    try {
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),"WWW_Activate",winID,5000);
                    }
                    catch (NbBrowserException ex) {
                        startBrowser (task);
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),"WWW_Activate",winID,5000);
                        hasNoWindow = false;
                    }
                    
                    if (data != null && data.length >= 4) {
                        task.browser.currWinID=DdeBrowserSupport.getDWORDAtOffset(data, 0);
                        task.browser.setStatusMessage(bundle.getString("MSG_use_win")+task.browser.currWinID);
                    }
                    else {
                        task.browser.currWinID = -1;
                        task.browser.setStatusMessage(bundle.getString("ERR_cant_activate_browser"));
                        return;
                    }
                }
                else {
                    data = null;
                }


                if (task.browser.realDDEServer().equals(ExtBrowserSettings.IEXPLORE)) {
                    winID = (hasNoWindow && !win9xHack(task))? "0": "-1";
                }
                else
                    winID = "0x00000000"+Integer.toHexString(hasNoWindow? 0: task.browser.currWinID).toUpperCase(); // NOI18N
                if (winID.length() > 10) winID = "0x"+winID.substring(winID.length()-8); // NOI18N

                // nbfs can be displayed internally and in ext. viewer too
                String args1;
                args1="\""+url.toString()+"\",,"+winID+",0x1,,,"+(task.browser.ddeProgressSrvName==null?"":task.browser.ddeProgressSrvName);  // NOI18N
                if (!win9xHack (task) && !mozillaHack (task)) {
                    data = task.browser.reqDdeMessage(task.browser.realDDEServer(),"WWW_OpenURL",args1,3000); // NOI18N
                }
                else {
                    // we've skipped WWW_Activate step so we need to start it if it doesn't run 
                    try {
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),"WWW_OpenURL",args1,3000); // NOI18N
                    }
                    catch (NbBrowserException ex) {
                        startBrowser (task);
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),"WWW_OpenURL",args1,3000); // NOI18N
                    }
                }
                    
                if (data != null && data.length >= 4) {
                    if (!task.browser.realDDEServer().equals("IEXPLORE")) {
                        task.browser.currWinID=DdeBrowserSupport.getDWORDAtOffset(data, 0);
                        if (task.browser.currWinID < 0) task.browser.currWinID = -task.browser.currWinID;
                    }
                    task.browser.setStatusMessage(bundle.getString("MSG_use_win"));
                }
                URL oldUrl = task.browser.url;
                task.browser.url = url;
                task.browser.pcs.firePropertyChange(PROP_URL, oldUrl, url);
            }
            catch (InterruptedException ex) {
                // this can be timer interrupt
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
        private boolean win9xHack (DisplayTask task) {
            return task.browser.realDDEServer().equals(ExtBrowserSettings.IEXPLORE)
                   && (Utilities.getOperatingSystem() == Utilities.OS_WIN98 
                      ||  Utilities.getOperatingSystem() == Utilities.OS_WIN95);
        }

        private boolean mozillaHack (DisplayTask task) {
            return task.browser.realDDEServer().equals(ExtBrowserSettings.MOZILLA);
        }
        
        /** 
         * Utility function that tries to start new browser process.
         *
         * It is used when WWW_Activate or WWW_OpenURL fail
         */
        private void startBrowser(DisplayTask task) throws NbBrowserException, java.io.IOException, InterruptedException {
            String b;
            if (ExtBrowserSettings.OPTIONS.isStartWhenNotRunning()) {
                NbProcessDescriptor cmd = task.browser.winBrowserFactory.getBrowserExecutable();
                // setStatusMessage(bundle.getString("MSG_Running_command")+cmd);
                cmd.exec();
                // wait for browser start
                Thread.currentThread().sleep(5000);
            }
        }
    }
    /** Encapsulating class for URL and browser that asks for its displaying */
    private static class DisplayTask {
        URL url;
        NbDdeBrowserImpl browser;
        
        DisplayTask (URL url, NbDdeBrowserImpl browser) {
            this.url = url;
            this.browser = browser;
        }
    }
}
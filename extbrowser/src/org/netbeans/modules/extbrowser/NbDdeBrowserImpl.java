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
import java.util.Timer;
import java.util.TimerTask; 
import java.util.Vector;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 * Class that uses DDE to communicate with web browser through DDE.
 * Currently three browsers are supported:
 * <UL>
 * <LI>Netscape Navigator</LI>
 * <LI>Internet Explorer</LI>
 * <LI>Mozilla</LI>
 * </UL>
 *
 * <P>Limitations: Mozilla doesn't support WWW_Activate now
 * IE has different implementation on Win9x and on WinNT/Win2000. 
 * WWW_Activate creates always new window on Win9x so we don't use it.
 * Also it accepts only "0xFFFFFFFF" for WWW_Activate on WinNT/Win2K.
 *
 * <P>Documentation can be found 
 * <a href="http://developer.netscape.com/docs/manuals/communicator/DDE/ddevb.htm">
 * here</a>.
 *
 * @author  Radim Kubacki
 */
public class NbDdeBrowserImpl extends ExtBrowserImpl {

    /** Netscape DDE topic names */
    private static final String WWW_ACTIVATE      = "WWW_Activate";          // NOI18N
    private static final String WWW_OPEN_URL      = "WWW_OpenURL";           // NOI18N
    
    /** Timeout used in DDE call. */
    private static int activateTimeout = 5000;
    /** Timeout used in DDE call. */
    private static int openUrlTimeout = 3000;
    
    static {
        try {
            if (org.openide.util.Utilities.isWindows ())
                System.loadLibrary("extbrowser");   // NOI18N
            
            Integer i = Integer.getInteger("org.netbeans.modules.extbrowser.WinWebBrowser.timeout"); // NOI18N
            if (i != null) {
                activateTimeout = i.intValue();
                openUrlTimeout = i.intValue();
            }
        } catch(Exception e) {
            DialogDisplayer.getDefault ().notify (
                new NotifyDescriptor.Message(NbBundle.getMessage(NbDdeBrowserImpl.class, "ERR_cant_locate_dll"),
                NotifyDescriptor.INFORMATION_MESSAGE)
            );
        }
    }
    
    /** Returns timeout (in milliseconds) used for DDE operations.
     * Default value is 5000 for browser activation and 3000 for opening URL.
     * These values can be overriden using system property
     * <SAMP>org.netbeans.modules.extbrowser.WinWebBrowser.timeout</SAMP>.
     */
    private static int ddeOperationTimeout (String operation) {
        if (WWW_ACTIVATE.equals (operation)) {
            return activateTimeout;
        }
        else if (WWW_OPEN_URL.equals (operation)) {
            return openUrlTimeout;
        }
        throw new IllegalArgumentException ();
    }
    
    /** reference to creator */
    private WinWebBrowser winBrowserFactory;
    
    /** native thread that displays URLs */
    private static Thread nativeThread = null;
    
    /** runnable class that implements the work of nativeThread */
    private static NbDdeBrowserImpl.URLDisplayer nativeRunnable = null;
    
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
            nativeThread = new Thread(nativeRunnable, "URLdisplayer");   // NOI18N
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
        // if we had current progress in NETSCAPE we could send CANCEL_PROGRESS topic
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
                if (cmd.toUpperCase ().indexOf (WinWebBrowser.IEXPLORE) >= 0)
                    return WinWebBrowser.IEXPLORE;

                if (cmd.toUpperCase ().indexOf ("NETSCP6") >= 0)    // NOI18N
                    return WinWebBrowser.NETSCAPE6;
                
                if (cmd.toUpperCase ().indexOf (WinWebBrowser.NETSCAPE) >= 0)
                    return WinWebBrowser.NETSCAPE;
                
                if (cmd.toUpperCase ().indexOf (WinWebBrowser.MOZILLA) >= 0)
                    return WinWebBrowser.MOZILLA;
            }
        }
        catch (Exception ex) {
            // some problem in native code likely
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ex);
        }
        // guess IE
        return WinWebBrowser.IEXPLORE;
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
                                DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_win_browser_invocation_failed"),
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
                    url = URLUtil.createExternalURL(url, task.browser.realDDEServer().equals(WinWebBrowser.MOZILLA));   // XXX support Netscape too?
                }
                else {
                    url = url;
                }
                byte [] data;
                boolean hasNoWindow = (task.browser.currWinID == -1);

                String winID;
                // activate browser window (doesn't work on Win9x)
                if (!win9xHack (task.browser.realDDEServer())) {
                    StatusDisplayer.getDefault ().setStatusText (NbBundle.getMessage (NbDdeBrowserImpl.class, "MSG_activatingBrowser"));
                    winID = windowId (task.browser.realDDEServer(), hasNoWindow? 0: task.browser.currWinID)+",0x0"; // NOI18N

                    try {
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_ACTIVATE,winID,15000);
                    }
                    catch (NbBrowserException ex) {
                        startBrowser (task);
                        winID = windowId (task.browser.realDDEServer(), -1)+",0x0"; // NOI18N
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_ACTIVATE,winID,5000);
                        hasNoWindow = false;
                    }
                    
                    if (data != null && data.length >= 4) {
                        task.browser.currWinID=DdeBrowserSupport.getDWORDAtOffset(data, 0);
                        task.browser.setStatusMessage(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_use_win")+task.browser.currWinID);
                    }
                    else {
                        task.browser.currWinID = -1;
                        task.browser.setStatusMessage(NbBundle.getMessage(NbDdeBrowserImpl.class, "ERR_cant_activate_browser"));
                        return;
                    }
                }
                else {
                    data = null;
                }


                StatusDisplayer.getDefault ().setStatusText (NbBundle.getMessage (NbDdeBrowserImpl.class, "MSG_openingURLInBrowser"));
                winID = windowId (task.browser.realDDEServer(), hasNoWindow? 0: task.browser.currWinID);
                    
                String args1;
                args1="\""+url.toString()+"\",,"+winID+",0x1,,,";  // NOI18N
                if (!win9xHack (task.browser.realDDEServer())) {
                    data = task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_OPEN_URL,args1,3000);
                }
                else {
                    // we've skipped WWW_Activate step so we need to start it if it doesn't run 
                    try {
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_OPEN_URL,args1,3000);
                    }
                    catch (NbBrowserException ex) {
                        startBrowser (task);

                        winID = windowId (task.browser.realDDEServer(), -1);
                        args1="\""+url.toString()+"\",,"+winID+",0x1,,,";  // NOI18N
                        data = task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_OPEN_URL,args1,3000);
                    }
                }
                    
                if (data != null && data.length >= 4) {
                    if (!task.browser.realDDEServer().equals(WinWebBrowser.IEXPLORE)) {
                        task.browser.currWinID=DdeBrowserSupport.getDWORDAtOffset(data, 0);
                        if (task.browser.currWinID < 0) task.browser.currWinID = -task.browser.currWinID;
                    }
                    task.browser.setStatusMessage(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_use_win"));
                }
                URL oldUrl = task.browser.url;
                task.browser.url = url;
                task.browser.pcs.firePropertyChange(PROP_URL, oldUrl, url);
                
                // wait for cleaning status text and processing of URL
                Thread.currentThread ().sleep (2000);
            }
            catch (InterruptedException ex) {
                // this can be timer interrupt
            }
            catch (Exception ex) {
                final Exception ex1 = ex;
                ErrorManager.getDefault ().annotate(ex1, NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_win_browser_invocation_failed"));
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ErrorManager.getDefault ().notify (ex1);
                    }
                });
            }
            finally {
                StatusDisplayer.getDefault ().setStatusText ("");    // NOI18N
            }
        }
        
        /**
         * Checks for IExplorer & Win9x combination.
         */
        private boolean win9xHack (String browser) {
            return browser.equals(WinWebBrowser.IEXPLORE)
                   && (Utilities.getOperatingSystem() == Utilities.OS_WIN98 
                      ||  Utilities.getOperatingSystem() == Utilities.OS_WIN95);
        }

        /** 
         * Utility function that tries to start new browser process.
         *
         * It is used when WWW_Activate or WWW_OpenURL fail
         */
        private void startBrowser(DisplayTask task) throws NbBrowserException, java.io.IOException, InterruptedException {
            if (task.browser.winBrowserFactory.isStartWhenNotRunning()) {
                NbProcessDescriptor cmd = task.browser.winBrowserFactory.getBrowserExecutable();
                StatusDisplayer.getDefault ().setStatusText (NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_startingBrowser"));
                cmd.exec();
                // wait for browser start
                Thread.currentThread().sleep(5000);
            }
        }
        
        /** Creates the windowId parameter for given browser.
         * @param browser name of DDE server
         * @param currentID identifies the window (0 means that new window should be opened)
         */
        private String windowId (String browser, int currentID) {
            String winID;
            
            if (browser.equals(WinWebBrowser.NETSCAPE)) {
                winID = "0x00000000"+Integer.toHexString(currentID).toUpperCase(); // NOI18N
                if (winID.length() > 10) winID = "0x"+winID.substring(winID.length()-8); // NOI18N
            }
            else {
                if (currentID == 0) {
                    if (browser.equals(WinWebBrowser.IEXPLORE) && win9xHack (browser)) {
                        winID = "-1"; // NOI18N
                    }
                    else {
                        winID = "0"; // NOI18N
                    }
                }
                else
                winID = "-1";   // NOI18N
            }
            return winID;
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

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
    
    static {
        try {
            if (org.openide.util.Utilities.isWindows ()) {
                System.loadLibrary("extbrowser");   // NOI18N
            }
        } catch(Exception e) {
            DialogDisplayer.getDefault ().notify (
                new NotifyDescriptor.Message(NbBundle.getMessage(NbDdeBrowserImpl.class, "ERR_cant_locate_dll"),
                NotifyDescriptor.INFORMATION_MESSAGE)
            );
        }
    }
        
    /** reference to creator */
    private ExtWebBrowser extBrowserFactory;
    
    /** native thread that displays URLs */
    private static Thread nativeThread = null;
    
    /** runnable class that implements the work of nativeThread */
    private static NbDdeBrowserImpl.URLDisplayer nativeRunnable = null;
    
    /** windowID of servicing window (-1 if there is no assocciated window) */
    private int    currWinID = -1;
    
    /** Creates new UnixBrowserImpl */
    public NbDdeBrowserImpl (ExtWebBrowser extBrowserFactory) {
        super ();
        currWinID = -1;
        this.extBrowserFactory = extBrowserFactory;
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
        ExtWebBrowser.getEM().log("set URL: " + url.toString());
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
        String srv = extBrowserFactory.getDDEServer ();
        if (srv != null) {
            return srv;
        }
        
        try {
            String cmd = getDefaultOpenCommand ();
            if (cmd != null) {
                if (cmd.toUpperCase ().indexOf (ExtWebBrowser.IEXPLORE) >= 0)
                    return ExtWebBrowser.IEXPLORE;

                if (cmd.toUpperCase ().indexOf ("NETSCP") >= 0)  // NOI18N
                    return ExtWebBrowser.NETSCAPE6;
                
                if (cmd.toUpperCase ().indexOf (ExtWebBrowser.NETSCAPE) >= 0)
                    return ExtWebBrowser.NETSCAPE;
                
                if (cmd.toUpperCase ().indexOf (ExtWebBrowser.MOZILLA) >= 0)
                    return ExtWebBrowser.MOZILLA;
            }
        } catch (Exception ex) {
            // some problem in native code likely
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ex);
        }
        // guess IE
        return ExtWebBrowser.IEXPLORE;
    }
    
    /** Getter for property activateTimeout.
     * @return Value of property activateTimeout.
     *
     */
    public int getActivateTimeout() {
        return extBrowserFactory.getActivateTimeout().intValue();
    }
        
    /** Getter for property openUrlTimeout.
     * @return Value of property openUrlTimeout.
     *
     */
    public int getOpenUrlTimeout() {
        return extBrowserFactory.getOpenurlTimeout().intValue();
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
            ExtWebBrowser.getEM().log("postTask: " + task.url + ", " + task.browser);
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
        private synchronized DisplayTask getNextTask() throws InterruptedException {
            do {
                
                if (!tasks.isEmpty ()) {
                    return (DisplayTask)tasks.remove(0);
                }
                wait();
                
            } while (true);
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
                                NbDdeBrowserImpl.nativeThread.interrupt();
                                ExtWebBrowser.getEM().log("interrupted in URLDisplayer.run.TimerTask.run()");
                                DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_win_browser_invocation_failed"),
                                NotifyDescriptor.INFORMATION_MESSAGE)
                                );
                            }
                        }
                    }, 11000);  // PENDING: add this timeout to browser properties
                    dispatchURL (task);
                    timer.cancel();
                } catch (InterruptedException ex) {
                    ExtWebBrowser.getEM().log("interrupted in run(): " + ex);
                    // do nothing
                } finally {
                    isDisplaying = false;
                }
            }
        }

        public void dispatchURL (DisplayTask task) {
            try {
                URL url = task.url;                
                ExtWebBrowser.getEM().log("URLDispatcher.url: " + url);
                
                // internal protocols cannot be displayed in external viewer
                if (isInternalProtocol(url.getProtocol())) {
                    url = URLUtil.createExternalURL(url, task.browser.realDDEServer().equals(ExtWebBrowser.MOZILLA));   // XXX support Netscape too?
                }
                
                boolean triedStart = false;

                // we don't care about netscape 6 -> it opens new window for each request - so running just command-line
                // if we would try DDE communication, we would have 2 windows when the browser is not started already
                if (isNetscape6(task.browser.extBrowserFactory)) {
                    ExtWebBrowser.getEM().log("netscape 6");
                    startNetscape6(task, url.toString());
                    return;
                }

                if (!win9xHack(task.browser.realDDEServer())) {
                    ExtWebBrowser.getEM().log("not win9x hack");
                    StatusDisplayer.getDefault ().setStatusText (NbBundle.getMessage (NbDdeBrowserImpl.class, "MSG_activatingBrowser"));
                    try {
                        task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_ACTIVATE,"-1,0x0",task.browser.getActivateTimeout());
                    } catch (NbBrowserException ex) {
                        ExtWebBrowser.getEM().log("Exception, gonna start browser: " + ex);
                        triedStart = true;
                        startBrowser(task);
                        try {
                            task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_ACTIVATE,"-1,0x0",task.browser.getActivateTimeout());
                        } catch (NbBrowserException nbe) {
                            // Browser activation failed - nevermind, life goes on, it's not fully supported in browsers anyway
                            if (ExtWebBrowser.getEM().isLoggable (ErrorManager.INFORMATIONAL)) {
                                ExtWebBrowser.getEM().log(ErrorManager.INFORMATIONAL, "Browser activation failed: " + nbe); // NOI18N
                            }
                        }
                    }  
                }

                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_openingURLInBrowser"));
                String args1 = "\""+url.toString()+"\",,-1,0x1,,,";  // NOI18N

                try {
                    task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_OPEN_URL,args1,task.browser.getOpenUrlTimeout());
                } catch (NbBrowserException ex) {
                    if (!triedStart) {
                        ExtWebBrowser.getEM().log("restarting browser");
                        startBrowser (task);        
                        task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_OPEN_URL,args1,task.browser.getOpenUrlTimeout());
                    } else {
                        throw new NbBrowserException(ex.toString());
                    }
                }

                URL oldUrl = task.browser.url;
                task.browser.url = url;
                task.browser.pcs.firePropertyChange(PROP_URL, oldUrl, url);

                // wait for cleaning status text and processing of URL
                Thread.currentThread().sleep(2000);
                
            } catch (InterruptedException ex) {
                ExtWebBrowser.getEM().log("Interrupted: " + ex);
                // this can be timer interrupt
            } catch (Exception ex) {
                final Exception ex1 = ex;
                ExtWebBrowser.getEM().log("interrupted in URLDisplayer.dispatchURL.end");
                ErrorManager.getDefault ().annotate(ex1, NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_win_browser_invocation_failed"));
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ErrorManager.getDefault ().notify (ex1);
                    }
                });
            } finally {
                StatusDisplayer.getDefault().setStatusText ("");    // NOI18N
            }
        }            
        
        /**
         * Checks for IExplorer & Win9x combination.
         */
        private boolean win9xHack (String browser) {
            return browser.equals(ExtWebBrowser.IEXPLORE)
                   && (Utilities.getOperatingSystem() == Utilities.OS_WIN98 
                      ||  Utilities.getOperatingSystem() == Utilities.OS_WIN95);
        }

        /**
         * Checks for Netscape6 - because N6 does not handle requests correctly - opens new window for each one
         */
        private boolean isNetscape6(ExtWebBrowser webBrowser) {
            return webBrowser.getBrowserExecutable().getProcessName().toString().toUpperCase().indexOf("NETSCP6") > -1;
        }

        /** 
         * Utility function that tries to start new browser process.
         *
         * It is used when WWW_Activate or WWW_OpenURL fail
         */
        private void startBrowser(DisplayTask task) throws NbBrowserException, java.io.IOException, InterruptedException {
            NbProcessDescriptor cmd = task.browser.extBrowserFactory.getBrowserExecutable();
            StatusDisplayer.getDefault ().setStatusText (NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_startingBrowser"));
            cmd.exec();

            // wait for browser start
            int timeout;
            Integer i = task.browser.extBrowserFactory.getBrowserStartTimeout();

            if (i != null) {
                timeout = i.intValue();
            } else {
                timeout = ExtWebBrowser.defaultBrowserStartTimeout;
            }

            ExtWebBrowser.getEM().log("timeout: " + timeout);

            Thread.currentThread().sleep(timeout);
        }
        
        /** 
         * Utility function that starts new browser process for Netscape6 - it is better than opening two windows at startup
         */
        private void startNetscape6(DisplayTask task, String url) throws NbBrowserException, java.io.IOException, InterruptedException {            
            String process = task.browser.extBrowserFactory.getBrowserExecutable().getProcessName();
            NbProcessDescriptor cmd = new NbProcessDescriptor(process, "\"{URL}\"");
            StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_startingBrowser"));
            cmd.exec(new ExtWebBrowser.UnixBrowserFormat(url));
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

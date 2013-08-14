/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.extbrowser;

import java.net.*;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extbrowser.PrivateBrowserFamilyId;
import org.openide.util.Exceptions;


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

    /** DDE topic names */
    private static final String WWW_ACTIVATE      = "WWW_Activate";          // NOI18N
    private static final String WWW_OPEN_URL      = "WWW_OpenURL";           // NOI18N
    
    private static final String EXTBROWSER_DLL = "extbrowser"; //NOI18N
    private static final String EXTBROWSER_DLL_64BIT = "extbrowser64"; //NOI18N

    static {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "> NbDdeBrowser: static initializer: ");
        }
        try {
            if (org.openide.util.Utilities.isWindows()) {

                // should be 32 or 64 bit, but it may not be present on some jdks
                String sunDataModel = System.getProperty("sun.arch.data.model"); //NOI18N
                if (sunDataModel != null) {
                    if ("64".equals(sunDataModel)) { //NOI18N
                        System.loadLibrary(EXTBROWSER_DLL_64BIT);
                    } else {
                        System.loadLibrary(EXTBROWSER_DLL);
                    }
                } else {
                    String javaVMName = System.getProperty("java.vm.name"); //NOI18N
                    if ((javaVMName != null) && (javaVMName.indexOf("64") > -1)) { //NOI18N
                        System.loadLibrary(EXTBROWSER_DLL_64BIT);
                    } else {
                        System.loadLibrary(EXTBROWSER_DLL);
                    }
                }

            }
        } catch (Exception e) {
            DialogDisplayer.getDefault ().notify (
                new NotifyDescriptor.Message(NbBundle.getMessage(NbDdeBrowserImpl.class, "ERR_cant_locate_dll"),
                NotifyDescriptor.INFORMATION_MESSAGE)
            );
        }
    }
            
    /** native thread that displays URLs */
    private static Thread nativeThread = null;
    
    /** runnable class that implements the work of nativeThread */
    private static NbDdeBrowserImpl.URLDisplayer nativeRunnable = null;
    
    /** Creates new NbDdeBrowserImpl
     * @param extBrowserFactory factory to use
     */
    public NbDdeBrowserImpl (ExtWebBrowser extBrowserFactory) {
        super ();
        this.extBrowserFactory = extBrowserFactory;
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDdeBrowserImpl created with factory: " + extBrowserFactory); // NOI18N
        }
    }
    
    native private byte [] reqDdeMessage (String srv, String topic, String item, int timeout) throws NbBrowserException;
    
    /** finds registry entry for browser opening */
    public native static String getBrowserPath (String browser) throws NbBrowserException;
    
    /** returns the command that executes default application for opening of 
     *  .html files
     */
    public native static String getDefaultOpenCommand() throws NbBrowserException;
    
    /** Sets current URL.
     *
     * @param url URL to show in the browser.
     */
    @Override
    protected void loadURLInBrowser(URL url) {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDdeBrowserImpl.setUrl: " + url); // NOI18N
        }
        if (url == null) {
            return;
        }
        if (isInternetExplorer()) {
            if (nativeThread == null) {
                nativeRunnable = new NbDdeBrowserImpl.URLDisplayer ();
                nativeThread = new Thread(nativeRunnable, "URLdisplayer");   // NOI18N
                nativeThread.start ();
            }
            nativeRunnable.postTask (new DisplayTask (url, this));
        }
        else {
            try {
                url = URLUtil.createExternalURL(url, false);
                URI uri = url.toURI();
                
                NbProcessDescriptor np = extBrowserFactory.getBrowserExecutable();
                if (np != null) {
                    np.exec(new SimpleExtBrowser.BrowserFormat((uri == null)? "": uri.toASCIIString())); // NOI18N
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                logInfo(ex);
                org.openide.DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(NbDdeBrowserImpl.class, "EXC_Invalid_Processor"), 
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.WARNING_MESSAGE
                    )
                );
            }
        }
    }
    
    private static void logInfo(Exception ex) {
        Logger logger = Logger.getLogger(NbDdeBrowserImpl.class.getName());
        logger.log(Level.INFO, null, ex);
    }
            


    @Override
    protected PrivateBrowserFamilyId detectPrivateBrowserFamilyId(){
        PrivateBrowserFamilyId id = super.detectPrivateBrowserFamilyId();
        if (id != PrivateBrowserFamilyId.UNKNOWN){
            return id;
        }
        String ddeServer = realDDEServer();
        if ( ExtWebBrowser.FIREFOX.equals( ddeServer ) ){
            return PrivateBrowserFamilyId.FIREFOX;
        }
        else if ( ExtWebBrowser.CHROME.equals( ddeServer)){
            return PrivateBrowserFamilyId.CHROME;
        }
        return PrivateBrowserFamilyId.UNKNOWN;
    }
    
    /** Finds the name of DDE server. 
     *  If <Default system browser> is set then it resolves it into either 
     *  Netscape or IExplore
     */
    private String realDDEServer () {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDdeBrowserImpl.realDDEServer"); // NOI18N
        }
        String srv = extBrowserFactory.getDDEServer ();
        if (srv != null) {
            return srv;
        }
        
        try {
            String cmd = getDefaultOpenCommand ();
            if (cmd != null) {
                if (cmd.toUpperCase ().indexOf (ExtWebBrowser.IEXPLORE) >= 0) {
                    return ExtWebBrowser.IEXPLORE;
                }

                if (cmd.toUpperCase ().indexOf ("CHROME") >= 0) { // NOI18N
                    return ExtWebBrowser.CHROME;
                }

                // check FF before Mozilla becuase FF is Mozilla but Mozills itself is old one
                if (cmd.toUpperCase ().indexOf (ExtWebBrowser.FIREFOX) >= 0) {
                    return ExtWebBrowser.FIREFOX;
                }
                
                if (cmd.toUpperCase ().indexOf (ExtWebBrowser.MOZILLA) >= 0) {
                    return ExtWebBrowser.MOZILLA;
                }
            }
        } catch (Exception ex) {
            // some problem in native code likely
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
        // guess IE
        return ExtWebBrowser.IEXPLORE;
    }
    
    /** Getter for property activateTimeout.
     * @return Value of property activateTimeout.
     *
     */
    public int getActivateTimeout() {
        return extBrowserFactory.getActivateTimeout();
    }
        
    /** Getter for property openUrlTimeout.
     * @return Value of property openUrlTimeout.
     *
     */
    public int getOpenUrlTimeout() {
        return extBrowserFactory.getOpenurlTimeout();
    }

    private boolean isInternetExplorer() {
        return realDDEServer().equals(ExtWebBrowser.IEXPLORE);
    }
        
    /**
     * Singleton for doing all DDE operations.
     */
    static class URLDisplayer implements Runnable { // NOI18N

        private static final int ADDITIONAL_WAIT_TIMEOUT = 6000;
    
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
        private synchronized DisplayTask getNextTask() throws InterruptedException {
            do {
                
                if (!tasks.isEmpty ()) {
                    return (DisplayTask)tasks.remove(0);
                }
                wait();
                
            } while (true);
        }
        
        @Override
        public void run() {
            if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDdeBrowserImpl.run"); // NOI18N
            }
            while (doProcessing) {
                try {
                    /** url to be displayed */
                    DisplayTask task = getNextTask();
                    
                    isDisplaying = true;
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (isDisplaying) {
                                NbDdeBrowserImpl.nativeThread.interrupt();
                                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                                    ExtWebBrowser.getEM().log(Level.FINE, "interrupted in URLDisplayer.run.TimerTask.run()");   // NOI18N
                                }
                                DialogDisplayer.getDefault().notify(
                                        new NotifyDescriptor.Message(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_win_browser_invocation_failed"),
                                        NotifyDescriptor.INFORMATION_MESSAGE)
                                        );
                            }
                        }
                    }, /*task.browser.extBrowserFactory.getBrowserStartTimeout() + */ADDITIONAL_WAIT_TIMEOUT);
                    dispatchURL (task);
                    timer.cancel();
                } catch (InterruptedException ex) {
                    ExtWebBrowser.getEM().log(Level.INFO, "interrupted in run(): " + ex);     // NOI18N
                    // do nothing
                } finally {
                    isDisplaying = false;
                }
            }
        }

        public void dispatchURL (DisplayTask task) {
            if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + " NbDdeBrowserImpl.dispatchURL: " + task); // NOI18N
            }
            try {
                
                URL url = task.url;
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + " URLDispatcher.url: " + url);          // NOI18N
                }
                
                // internal protocols cannot be displayed in external viewer
                url = URLUtil.createExternalURL(url, URLUtil.browserHandlesJarURLs(task.browser.realDDEServer()));   // XXX support Netscape too?
                
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + " url: " + url);          // NOI18N
                }

                String urlStr = url.toString();
                
                boolean triedStart = false;
                final int MAX_URL_LENGTH = 199;
                
                if ((urlStr != null) && (urlStr.length() > MAX_URL_LENGTH)) {
                     urlStr = getFileUrl(urlStr);
                }

                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + " urlstr: " + urlStr);          // NOI18N
                }
                if (!win9xHack(task.browser.realDDEServer())) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage (NbDdeBrowserImpl.class, "MSG_activatingBrowser"));
                    try {
                        task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_ACTIVATE,"-1,0x0",task.browser.getActivateTimeout());
                    } catch (NbBrowserException ex) {
                        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {                
                            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "Exception, gonna start browser: " + ex);  // NOI18N
                        }
                        triedStart = true;
                        startBrowser(task.browser.extBrowserFactory.getBrowserExecutable(), urlStr);
                    }  
                }
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + " firstpart");          // NOI18N
                }

                if (!triedStart) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_openingURLInBrowser", urlStr));
                    String args1 = "\""+urlStr+"\",,-1,0x1,,,";  // NOI18N

                    try {
                        Thread.sleep(500); // trying hack for bug #42438 - Browser executes twice which is a Mozilla bug
                        task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_OPEN_URL,args1,task.browser.getOpenUrlTimeout());
                    } catch (NbBrowserException ex) {
                        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                            ExtWebBrowser.getEM().log(Level.FINE, "Restarting browser.");    // NOI18N
                        }
                        startBrowser(task.browser.extBrowserFactory.getBrowserExecutable(), urlStr);
                    } catch (InterruptedException ex) {
                        // just ignore
                    }
                }
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + " secondpart");          // NOI18N
                }

                task.browser.pcs.firePropertyChange(PROP_URL, task.browser.getURL(), url);

            } catch (Exception ex) {
                final Exception ex1 = ex;
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + " Interrupted in URLDisplayer.dispatchURL.end");   // NOI18N
                }
                Exceptions.attachLocalizedMessage(ex1, NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_win_browser_invocation_failed"));
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Exceptions.printStackTrace(ex1);
                    }
                });
            }
        }
        
        
        /** 
         *
         */
        private String getFileUrl(String url) {
            if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "Gonna get redirect file for long url: " + url);
            }
            String newurl = null;
            FileWriter fw = null;
            File f = null;
            
            int retries = 10;
            
            while ((f == null) && (retries > 0)) {
                retries--;
                try {
                    f = File.createTempFile("extbrowser", ".html");             // NOI18N
                    if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                        ExtWebBrowser.getEM().log(Level.FINE, "file: " + f);                // NOI18N
                    }
                    if (f != null) { 
                        fw = new FileWriter(f);
                        if (f.canWrite()) {
                            String s1 = org.openide.util.NbBundle.getMessage(NbDdeBrowserImpl.class, "TXT_RedirectURL1");   //NOI18N
                            String s2 = org.openide.util.NbBundle.getMessage(NbDdeBrowserImpl.class, "TXT_RedirectURL2");   //NOI18N
                            String s = s1.concat(url).concat(s2);
                            fw.write(s);
                            fw.flush();
                        }
                        newurl = "file:/" + f.getAbsolutePath();                // NOI18N
                    }
                } catch (IOException ioe) {
                     Logger.getLogger("global").log(Level.INFO, "" + System.currentTimeMillis() + ioe.toString());
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException ioe) {
                            Logger.getLogger("global").log(Level.INFO, "" + System.currentTimeMillis() + ioe.toString());
                        }
                    }
                }                
            }
            if (newurl != null) { 
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "New URL: " + newurl);                // NOI18N
                }
                return newurl;
            }
            return url;
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
         * Utility function that tries to start new browser process.
         *
         * It is used when WWW_Activate or WWW_OpenURL fail
         */
        private void startBrowser(NbProcessDescriptor cmd, String url) throws java.io.IOException {
            StatusDisplayer.getDefault ().setStatusText (NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_startingBrowser", url));
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

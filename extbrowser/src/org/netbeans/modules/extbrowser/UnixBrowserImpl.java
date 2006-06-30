/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;

import org.openide.util.RequestProcessor;

/**
 * The UnixBrowserImpl is implementation of browser that displays content in
 * external program (Netscape). It is usable on Unix platform only because it
 * uses command line option specific to this environment.
 * Additionally it uses some XWindow utilities to get information about 
 * browser windows.
 *
 * @author Radim Kubacki
 */
public class UnixBrowserImpl extends ExtBrowserImpl {
    
    /** Number of probes to get exit status of executed command. 
     * Status is checked after each second.
     */
    protected static final int CMD_TIMEOUT = 6;
    
    /** Creates modified NbProcessDescriptor that can be used to start
     * browser process when <CODE>-remote openURL()</CODE> options
     * cannot be used.
     * @return command or <CODE>null</CODE>
     * @param p Original command.
     */
    protected static NbProcessDescriptor createPatchedExecutable (NbProcessDescriptor p) {
        NbProcessDescriptor newP = null;
        
        String [] args = org.openide.util.Utilities.parseParameters(p.getArguments());
        if (args.length > 1) {
            if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {        
                ExtWebBrowser.getEM().log("Old arguments: " + p.getArguments());    // NOI18N
            }
            StringBuffer newArgs = new StringBuffer ();
            boolean found = false;
            for (int i=0; i<args.length-1; i++) {
                if (newArgs.length() > 0) {
                    newArgs.append(" ");  // NOI18N
                }
                if (args[i].indexOf("-remote") >= 0  // NOI18N
                &&  args[i+1].indexOf("openURL(") >=0) {  // NOI18N
                    found = true;
                    newArgs.append("\"{URL}\"");  // NOI18N
                }
                else {
                    newArgs.append("\""+args[i]+"\"");  // NOI18N
                }
            }
            if (found) {
                newP = new NbProcessDescriptor (p.getProcessName(), newArgs.toString(), p.getInfo());
            }
            if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {        
                ExtWebBrowser.getEM().log("ProcessName: " + p.getProcessName());    // NOI18N
                ExtWebBrowser.getEM().log("New arguments: " + newArgs.toString());    // NOI18N
            }
        }
        return newP;
    }

    /** Creates new UnixBrowserImpl */
    public UnixBrowserImpl () {
        this (null);
    }
    
    /** Creates new UnixBrowserImpl
     * @param extBrowserFactory Associated browser factory to get settings from.
     */
    public UnixBrowserImpl (ExtWebBrowser extBrowserFactory) {
        super();
        this.extBrowserFactory = extBrowserFactory;
        if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {        
            ExtWebBrowser.getEM().log("UnixBrowserImpl created from factory: " + extBrowserFactory);    // NOI18N
        }
    }
       
    /** 
     *  Sets current URL.</P>
     *
     *  <P>If browser is running and we know window ID we call 
     *  <CODE>browser_command -id _winID_ -raise -remote 'openURL(_url)'</CODE>
     *  else we start it with 
     *  <CODE>browser_command _url_</CODE></P>
     *
     * @param url URL to show in the browser.
     */
    public void setURL(URL url) {
        if (SwingUtilities.isEventDispatchThread ()) {
            final URL newUrl = url;
            RequestProcessor.getDefault ().post (
                new Runnable () {
                    public void run () {
                        UnixBrowserImpl.this.setURL (newUrl);
                    }
            });
            return;
        }
        
        NbProcessDescriptor cmd = extBrowserFactory.getBrowserExecutable ();    // NOI18N
        Process p;
        StatusDisplayer sd = StatusDisplayer.getDefault ();
        try {
            // internal protocols cannot be displayed in external viewer
            url = URLUtil.createExternalURL(url, false);
            if (ExtWebBrowser.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "External url: " + url); // NOI18N
            }
            
            cmd = extBrowserFactory.getBrowserExecutable (); // NOI18N
            if (ExtWebBrowser.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "Executable: " + cmd); // NOI18N
            }
            sd.setStatusText (NbBundle.getMessage (UnixBrowserImpl.class, "MSG_Running_command", cmd.getProcessName ()));
            p = cmd.exec (new ExtWebBrowser.UnixBrowserFormat (url.toString ()));
            
            RequestProcessor.getDefault ().post (new Status (cmd, p, url), 1000);

            URL old = this.url;
            this.url = url;
            pcs.firePropertyChange (PROP_URL, old, url);
        }
        catch (java.io.IOException ex) {
            // occurs when executable is not found or not executable
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message (
                NbBundle.getMessage (UnixBrowserImpl.class, "MSG_Cant_run_netscape", new Object [] { cmd.getProcessName () }),
                NotifyDescriptor.Message.WARNING_MESSAGE)
            );
        }
        catch (NumberFormatException ex) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ex);
        }
        catch (java.lang.Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
   
    /** Object that checks execution result
     * of browser invocation request.
     * <p>It can made another attempt to start the browser
     * when error output contains information that communication
     * through Xremote protocol failed.
     */        
    private class Status implements Runnable {
        
        /** Message printed when invocation fails even though the 
         * application runs, but there's no browser window (only mail client, e.g.).
         */
        private static final String FAILURE_MSG_BADWINDOW = "BadWindow";   // NOI18N
        
        /** Message printed when invocation fails because the application does not run. */
        private static final String FAILURE_MSG = "No running window found.";   // NOI18N

        /** Originally executed command. */
        private NbProcessDescriptor cmd;
        
        /** Handle to executed process. */
        private Process p;
        
        /** URL to be displayed. */
        private URL url;
        
        /** Retries counter. */
        private int retries = CMD_TIMEOUT;
        
        /** Creates Status object to check execution result
         * of browser invocation request.
         * @param cmd Originally executed command.
         * @param p Process that is checked.
         * @param url Displayed URL that can be used when another attempt
         * to start the browser is made or <CODE>null</CODE>.
         */        
        public Status (NbProcessDescriptor cmd, Process p, URL url) {
            this. cmd = cmd;
            this.p = p;
            this.url = url;
        }
        
        /** Checks whether process is correctly executed.
         * If it returns bad exit code or prints know error message
         * it is re-executed once again.
         * If the execution is not finished during timeout message is displayed.
         */
        public void run () {
            boolean retried = false;
            if (ExtWebBrowser.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "Retried: " + retried); // NOI18N
            }
            int exitStatus = 1;
            Reader r = new InputStreamReader (p.getErrorStream ());
            try {
                exitStatus = p.exitValue();
                if (ExtWebBrowser.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                    ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "Command executed. exitValue = " + exitStatus); // NOI18N
                }
            } catch (IllegalThreadStateException ex) {
                retries--;
                if (ExtWebBrowser.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                    ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "Retries: " + retries); // NOI18N
                    ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "Time: " + System.currentTimeMillis()); // NOI18N
                }
                if (retries > 0) {
                    RequestProcessor.getDefault().post(this, 1000);
                    return;
                } else {
                    if (ExtWebBrowser.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                        ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "Command not finished yet"); // NOI18N
                    }
                }
            }

            // hack : Netscape exits with 0 on Linux even if there is no window
            if (exitStatus == 0 && org.openide.util.Utilities.getOperatingSystem() == org.openide.util.Utilities.OS_LINUX) {
                final int LEN = 2048;
                char [] buff = new char [LEN];
                int l;
                StringBuffer sb = new StringBuffer ();
                try {
                    while ((l = r.read (buff, 0, LEN)) != -1) {
                        sb.append (buff, 0, l);
                    }
                    if (sb.toString ().indexOf (FAILURE_MSG) >= 0) {
                        if (ExtWebBrowser.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                            ExtWebBrowser.getEM ().log (ErrorManager.INFORMATIONAL, "Browser output: \""+FAILURE_MSG+"\""); // NOI18N
                        }
                        exitStatus = 2;
                    }
                } catch (java.io.IOException ioe) {
                    // suppose it was executed
                    ExtWebBrowser.getEM ().notify(ErrorManager.WARNING, ioe);
                }
            }
            
            // mozilla & netscape exits with 1 on Linux if there's mail window present, 
            // but there's no browser window - the URL is shown correctly, though
            if (exitStatus == 1 && org.openide.util.Utilities.getOperatingSystem() == org.openide.util.Utilities.OS_LINUX) {
                final int LEN = 2048;
                char [] buff = new char [LEN];
                int l;
                StringBuffer sb = new StringBuffer ();
                try {
                    while ((l = r.read (buff, 0, LEN)) != -1) {
                        sb.append (buff, 0, l);
                    }
                    if (sb.toString ().indexOf (FAILURE_MSG_BADWINDOW) >= 0) {
                        if (ExtWebBrowser.getEM().isLoggable (ErrorManager.INFORMATIONAL)) {
                            ExtWebBrowser.getEM().log (ErrorManager.INFORMATIONAL, "Browser output: \""+FAILURE_MSG_BADWINDOW+"\""); // NOI18N
                        }
                        exitStatus = 0;
                    }
                } catch (java.io.IOException ioe) {
                    // suppose it was executed
                    ExtWebBrowser.getEM ().notify(ErrorManager.WARNING, ioe);
                }
            }
            
            if (exitStatus == 2) {
                try {
                    NbProcessDescriptor startCmd = UnixBrowserImpl.createPatchedExecutable(cmd);
                    if (startCmd != null) {
                        retried = true;
                        StatusDisplayer.getDefault().
                            setStatusText (NbBundle.getMessage (UnixBrowserImpl.class, "MSG_Running_command", startCmd.getProcessName ()));
                        Process pr = startCmd.exec (new ExtWebBrowser.UnixBrowserFormat (url.toString ()));

                        // do not care about result now
                        // RequestProcessor.getDefault ().post (new Status (startCmd, pr, null), 1000);
                    }
                }
                catch (java.io.IOException ioe) {
                    // suppose it was executed
                    ExtWebBrowser.getEM ().notify(ErrorManager.WARNING, ioe);
                }
            }
            
            if (exitStatus != 0 && !retried) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message (
                    NbBundle.getMessage (UnixBrowserImpl.class, "MSG_Cant_run_netscape", new Object [] { cmd.getProcessName () }),
                    NotifyDescriptor.Message.WARNING_MESSAGE)
                );
                return;
            }

        }
    }
}

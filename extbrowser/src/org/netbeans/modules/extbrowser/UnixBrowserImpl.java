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
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.ResourceBundle;
import javax.swing.*;

import org.openide.*;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.options.SystemOption;

import org.netbeans.modules.httpserver.*;

/**
 * The UnixBrowserImpl is implementation of browser that displays content in 
 * external program (Netscape). It is usable on Unix platform only because it
 * uses command line option specific to this environment.
 * Additionally it uses some XWindow utilities to get information about 
 * browser windows.
 *
 * @author Radim Kubacki
 * @version 1.0
 */
public class UnixBrowserImpl extends ExtBrowserImpl {
    
    private static ResourceBundle bundle = NbBundle.getBundle(UnixBrowserImpl.class);
    
    private static boolean debug = false;
    
    /** windowID of servicing window (-1 if there is no assocciated window */
    private transient int     currWinID = -1;
    
    /** number of probes to get XWindow identification of used window */
    int nOfProbes = 3;
    
    /** length of delay between each probe to get XWindow identification */
    int probeDelayLength = 3000;
    
    /** reference to a factory to gett settings */
    private ExtWebBrowser extBrowserFactory;

    /** Creates new UnixBrowserImpl */
    public UnixBrowserImpl () {
        this (null);
    }
    
    /** Creates new UnixBrowserImpl */
    public UnixBrowserImpl (ExtWebBrowser extBrowserFactory) {
        super ();
        currWinID = -1;
        this.extBrowserFactory = extBrowserFactory;
    }
    
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
     * @return always false
     */
    public boolean isBackward() {
        return false;
    }
    
    /** Is forward button enabled?
     * @return always false
     */
    public boolean isForward() {
        return false;
    }
    
    /** history is disabled?
     * @return always false
     */
    public boolean isHistory() {
        return false;
    }
    
    /** Call setURL again to force reloading.
     * Browser must be set to reload document and do not cache them.
     */
    public void reloadDocument() {
        if (url != null)
            setURL (url);
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
        NbProcessDescriptor cmd = extBrowserFactory.getBrowserExecutable ();    // NOI18N
        Process p;
        TopManager tm = TopManager.getDefault ();
        try {
            // internal protocols cannot be displayed in external viewer
            if (isInternalProtocol (url.getProtocol ())) {
                url = WrapperServlet.createHttpURL (url);
            }
            if (currWinID != -1) {
                // check if given window still exists
                if (getXProperty (currWinID, "WM_NAME") == null) { // NOI18N
                    currWinID = -1;
                    // PENDING: build list of existing windows to check new winID
                    
                }
            }
            
            if (currWinID == -1) {
                // no browser window is assigned
                
                tm.setStatusText (bundle.getString("MSG_creating_new_window")+Integer.toHexString (currWinID));

                // is browser running?
                cmd = new NbProcessDescriptor ("xwininfo", "-name " + getCommand (false));   // NOI18N
                p = cmd.exec (); 
                if (p.waitFor () == 0) {
                    cmd = extBrowserFactory.getBrowserExecutable (); // NOI18N
                    tm.setStatusText (bundle.getString("MSG_Running_command")+cmd);
                    p = cmd.exec (new UnixWebBrowser.UnixBrowserFormat (new ExecInfo(""), "-raise -remote openURL("+url.toString ()+",new-window)"));
                    if (p.waitFor () != 0) {
                        tm.notify (
                            new NotifyDescriptor.Message (
                            NbBundle.getMessage (UnixBrowserImpl.class, "MSG_Cant_run_netscape", new Object [] { cmd.getProcessName () }),
                            NotifyDescriptor.Message.WARNING_MESSAGE)
                        );
                        return;
                    }
                }
                else {
                    cmd = extBrowserFactory.getBrowserExecutable (); // NOI18N
                    tm.setStatusText (bundle.getString("MSG_Running_command")+cmd);
                    p = cmd.exec (new UnixWebBrowser.UnixBrowserFormat (new ExecInfo(""), url.toString ()));
                }
                
                new Thread (new UnixBrowserImpl.WindowFinder (url.toString())).start();
            }
            else {
                // reuse old window
                
                tm.setStatusText (bundle.getString("MSG_use_win")+Integer.toHexString (currWinID));
                
                cmd = extBrowserFactory.getBrowserExecutable (); // NOI18N
                tm.setStatusText (bundle.getString("MSG_Running_command")+cmd);
                p = cmd.exec (new UnixWebBrowser.UnixBrowserFormat (
                    new ExecInfo(""), "-id 0x"+Integer.toHexString (currWinID)+" -raise -remote openURL("+url.toString ()+")")
                );
                if (p.waitFor () != 0) {
                    tm.notify (
                        new NotifyDescriptor.Message (
                        NbBundle.getMessage (UnixBrowserImpl.class, "MSG_Cant_run_netscape", new Object [] { cmd.getProcessName () }),
                        NotifyDescriptor.Message.WARNING_MESSAGE)
                    );
                    return;
                }
                // this is too early to get window title now
            }
            tm.setStatusText ("");
            
            URL old = this.url;
            this.url = url;
            pcs.firePropertyChange (PROP_URL, old, url);
        }
        catch (java.io.IOException ex) {
            // occurs when executable is not found or not executable
            tm.notify (
                new NotifyDescriptor.Message (
                NbBundle.getMessage (UnixBrowserImpl.class, "MSG_Cant_run_netscape", new Object [] { cmd.getProcessName () }),
                NotifyDescriptor.Message.WARNING_MESSAGE)
            );
        }
        catch (InterruptedException ex) {
            System.out.println(ex.getMessage ());
            ex.printStackTrace ();
        }
        catch (NumberFormatException ex) {
            System.out.println(ex.getMessage ());
            ex.printStackTrace ();
        }
        catch (java.lang.Exception ex) {
            System.out.println(ex.getMessage ());
            ex.printStackTrace ();
        }
    }
    
    /** Invoked when the history button is pressed.
     *  disabled
     */
    public void showHistory() {
        return; 
    }
    
    /** Stops loading of current html page.
     */
    public void stopLoading() {
    }
    
    private void setWindowID (int winID) {
        if (debug) System.out.println("setWindowID to "+Integer.toHexString (winID));
        currWinID = winID;
    }
    
    /** returns internal http server port number */
    int getInternalServerPort () throws java.lang.Exception {
        HttpServerSettings setting = (HttpServerSettings)SystemOption.findObject (HttpServerSettings.class);
        if (setting == null)
            throw new java.lang.Exception ("Cannot get Httpserver port number");
        
        return setting.getPort ();
    }
    
    /** 
     *  tries to find property for window, property must be of type STRING
     *
     *  @param winID    XWindow identifier of window
     *  @param propName name of property
     *
     *  @return property string if found, null if not found
     */
    private String getXProperty (int winID, String propName) {

        try {
            Process p = Runtime.getRuntime ().exec ("xprop -id 0x"+Integer.toHexString (winID)+" "+propName); // NOI18N
            if (p.waitFor () == 0) {
                // completed successfully
                BufferedReader r = new BufferedReader (new InputStreamReader (p.getInputStream ()));
                String result = r.readLine ();
                if ((result != null)
                &&  (result.startsWith (propName+"(STRING)"))) { // NOI18N
                    int b,e;
                    b = result.indexOf ('"');
                    e = result.indexOf ('"', b+1);
                    if ((b == -1) || (e == -1))
                        return null;

//System.out.println("getXProperty ("+Integer.toHexString (winID)+", "+propName+") = "+result.substring (b+1, e)); // NOI18N
                    return result.substring (b+1, e);
                }
            }
            return null;
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace ();
        }
        catch (InterruptedException ex) {
            ex.printStackTrace ();
        }
        return null;
    }
    
    /**
     * Looks into factory for executable. If nothing is found then netscape is returned
     * 
     * @param wholePath if true then all path is returned, 
     *                  if false then it is cut to last part and first char is converted
     *                  to uppercase
     * @return command to be executed
     */
    private String getCommand (boolean wholePath) {
        String exec = "netscape";  // NOI18N
        if (extBrowserFactory != null) {
            NbProcessDescriptor process = extBrowserFactory.getBrowserExecutable ();
            if (process != null) {
                exec = process.getProcessName ();
            }
        }
        if (!wholePath) {
            int idx = exec.lastIndexOf ('/');
            if ((idx == -1) && Utilities.isWindows ()) {
                idx = exec.lastIndexOf ('\\');
            }
            if ((idx != -1) && (exec.length () > idx))
                exec = exec.substring (idx+1);
            
            if (exec.length () > 0) 
                exec = Character.toUpperCase (exec.charAt (0))+exec.substring (1);
        }
        return exec;
    }  


    /**
     * This class searches for window that contains rendered content.
     * When NN4.x is used this window has _MOZILLA_URL xproperty that is equal to URL
     * In other cases we only try to find it by WM_NAME property
     */
    class WindowFinder implements Runnable {

        String url;
        
        public WindowFinder(java.lang.String url) {
            this.url = url;
        }
        
        public void run () {
            try {
                for (int i=nOfProbes; i>0; i--) {
                    // now try to get win ID
                    setStatusMessage (bundle.getString("MSG_look_for_win"));
                    Process p = Runtime.getRuntime ().exec (new String [] {
                        "sh", "-c", "xwininfo -root -tree|grep " + getCommand (false)}); // NOI18N
                    java.io.InputStream inp = p.getInputStream ();
                    int errCode = p.waitFor ();
                    if (errCode == 0) {
                        String line, s, prop;
                        int winID;
                        BufferedReader r = new BufferedReader (new InputStreamReader(inp));
                        
                        while ((line = r.readLine ()) != null) {
                            s = line.substring (line.indexOf ('x')+1);
                            s = s.substring (0, s.indexOf (' '));
                            winID = Integer.parseInt (s, 16);
                            
                            prop = getXProperty (winID, "_MOZILLA_URL"); // NOI18N
                            if (prop != null && prop.equals (url)) {
                                setWindowID (winID);
                                setTitle (getXProperty(winID, "WM_NAME")); // NOI18N
                                return;
                            }
                            
                        }
                    }
                    Thread.sleep (probeDelayLength);
                }
                // fallback - use the first one if you can't find it by URL
                setStatusMessage (bundle.getString("MSG_look_for_win"));
                Process p = Runtime.getRuntime ().exec (new String [] {
                    "sh", "-c", "xwininfo -root -tree|grep " + getCommand (false) }); // NOI18N
                java.io.InputStream inp = p.getInputStream ();
                int errCode = p.waitFor ();
                if (errCode == 0) {
                    String line, s, prop;
                    int winID;
                    BufferedReader r = new BufferedReader (new InputStreamReader(inp));

                    while ((line = r.readLine ()) != null) {
                        s = line.substring (line.indexOf ('x')+1);
                        s = s.substring (0, s.indexOf (' '));
                        winID = Integer.parseInt (s, 16);

                        prop = getXProperty (winID, "WM_NAME"); // NOI18N
                        if (prop != null) {
                            setWindowID (winID);
                            return;
                        }
                    }
                }
            }
            catch (java.io.IOException ex) {
                System.out.println(ex.getMessage ());
                ex.printStackTrace ();
            }
            catch (InterruptedException ex) {
                System.out.println(ex.getMessage ());
                ex.printStackTrace ();
            }
            // maybe not started & initialized yet
        }
        
    }
}

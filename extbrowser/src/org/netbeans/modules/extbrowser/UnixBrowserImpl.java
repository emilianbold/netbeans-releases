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
//import org.openide.awt.SwingBrowserImpl;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
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
public class UnixBrowserImpl extends org.openide.awt.HtmlBrowser.Impl {
    
    private static ResourceBundle bundle = NbBundle.getBundle(UnixBrowserImpl.class);
    
    /** standart helper variable */
    private PropertyChangeSupport pcs;
    
    /** component displayed in NB window */
    private HtmlBrowser.Impl   browserComp;
    
    /** requested URL */
    private URL url;
    private String statusMsg = "";  // NOI18N
    private String title = "";      // NOI18N
    

    /** windowID of servicing window (-1 if there is no assocciated window */
    private transient int     currWinID = -1;
    
    /** number of probes to get XWindow identification of used window */
    int nOfProbes = 3;
    
    /** length of delay between each probe to get XWindow identification */
    int probeDelayLength = 3000;

    /** Creates new UnixBrowserImpl */
    public UnixBrowserImpl (org.openide.awt.HtmlBrowser.Factory fact) {
        pcs = new PropertyChangeSupport (this);
        currWinID = -1;
        browserComp = fact.createHtmlBrowserImpl ();
    }
    
    /** Moves the browser forward. Failure is ignored.
     *  disabled
     */
    public void backward() {
        return;
    }
    
    /** Moves the browser forward. Failure is ignored.
     *  disabled
     */
    public void forward() {
        return;
    }
    
    /** Returns default component of html browser which is based on swing editor pane.
     *
     * @return visual component of html browser.
     */
    public java.awt.Component getComponent() {
        return browserComp.getComponent ();
    }
    
    /** Sets new status message for the displayed page.
     * @param msg new message
     */
    private void setStatusMessage (String msg) {
        String old = this.statusMsg;
        this.statusMsg = msg;
        pcs.firePropertyChange (PROP_STATUS_MESSAGE, old, msg);
        
        return;
    }
    
    /** Returns status message representing status of html browser.
     *
     * @return status message.
     */
    public String getStatusMessage() {
        return statusMsg;
    }
    
    /** Sets new title of the displayed page.
     * @param title new title
     */
    private void setTitle (String title) {
        String old = this.title;
        this.title = title;
        pcs.firePropertyChange (PROP_TITLE, old, title);
        
        return;
    }
    
    /** Returns title of the displayed page.
     * @return title
     */
    public String getTitle() {
        return title;
    }
    
    /** Returns current URL.
     *
     * @return current URL.
     */
    public URL getURL() {
        return url;
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
    
    /** Reloads current html page.
     */
    public void reloadDocument() {
    }
    
    /** 
     *  Sets current URL.</P>
     *
     *  <P>If netscape is running and we know window ID we call 
     *  netscape -id _winID_ -raise -remote 'openURL(_url)'
     *  else we start it with 
     *  netscape _url_</P>
     *
     *  <P> There are also some internal protocols in IDE. These are displayed in
     *  Swing editor pane.</P>
     *
     * @param url URL to show in the browser.
     */
    public void setURL(URL url) {
        try {
            // internal protocols cannot be displayed in external viewer
            if (url.getProtocol ().equals ("nbfs")               // NOI18N
            ||  url.getProtocol ().equals ("nbres")              // NOI18N
            ||  url.getProtocol ().equals ("nbrescurr")          // NOI18N
            ||  url.getProtocol ().equals ("nbresloc")           // NOI18N
            ||  url.getProtocol ().equals ("nbrescurrloc")) { // NOI18N
                browserComp.setURL (url);

                URL old = this.url;
                this.url = url;
                pcs.firePropertyChange (PROP_URL, old, url);
                if (url.getProtocol ().equals ("nbfs") // NOI18N
                &&  url.getPath () != null)               
                    url = new java.net.URL ("http", "localhost", getInternalServerPort (), 
                        "/servlet/org.netbeans.modules.extbrowser.JavaDocServlet"+
                        (url.getPath().startsWith ("/")? url.getPath(): "/"+url.getPath())+
                        ((url.getRef()!=null)?"#"+url.getRef():"")); // NOI18N
                else
                    return;
            }
            String cmd;
            if (currWinID != -1) {
                // check if given window still exists
                if (getXProperty (currWinID, "WM_NAME") == null) { // NOI18N
                    currWinID = -1;
                    // PENDING: build list of existing windows to check new winID
                    
                }
            }
            
            if (currWinID == -1) {
                setStatusMessage (bundle.getString("MSG_creating_new_window")+Integer.toHexString (currWinID));

                byte [] buff = new byte [256];
                
                // is netcape running?
                Process p = Runtime.getRuntime ().exec ("xwininfo -name Netscape"); // NOI18N
                if (p.waitFor () == 0) {
                    cmd = "netscape -raise -remote openURL("+url.toString ()+",new-window)"; // NOI18N
                    setStatusMessage (bundle.getString("MSG_Running_command")+cmd);
                    p = Runtime.getRuntime ().exec (cmd);
                    if (p.waitFor () != 0) {
                        TopManager.getDefault ().notify (
                            new NotifyDescriptor.Message (bundle.getString("MSG_Cant_run_netscape"),
                            NotifyDescriptor.Message.WARNING_MESSAGE)
                        );
                        return;
                    }
                }
                else {
                    cmd = "netscape "+url.toString (); // NOI18N
                    setStatusMessage (bundle.getString("MSG_Running_command")+cmd);
                    p = Runtime.getRuntime ().exec (cmd);
                }
                
                new Thread (new UnixBrowserImpl.WindowFinder (url.toString())).start();
            }
            else {
                setStatusMessage (bundle.getString("MSG_use_win")+Integer.toHexString (currWinID));
                
                cmd = "netscape -id 0x"+Integer.toHexString (currWinID)+
                    " -raise -remote openURL("+url.toString ()+")"; // NOI18N
                setStatusMessage (bundle.getString("MSG_Running_command")+cmd);
                Process p = Runtime.getRuntime ().exec (cmd);
                if (p.waitFor () != 0) {
                    TopManager.getDefault ().notify (
                        new NotifyDescriptor.Message (bundle.getString("MSG_Cant_run_netscape"),
                        NotifyDescriptor.Message.WARNING_MESSAGE)
                    );
                    return;
                }
                // this is too early to get window title now
                setTitle (getXProperty(currWinID, "WM_NAME")); // NOI18N
            }
            setStatusMessage (bundle.getString("MSG_done"));
            
            URL old = this.url;
            this.url = url;
            pcs.firePropertyChange (PROP_URL, old, url);
        }
        catch (java.io.IOException ex) {
            System.out.println(ex.getMessage ());
            ex.printStackTrace ();
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
        currWinID = winID;
    }
    
    /** returns internal http server port number */
    int getInternalServerPort () throws java.lang.Exception {
        HttpServerSettings setting = (HttpServerSettings)SystemOption.findObject (HttpServerSettings.class);
        if (setting == null)
            throw new java.lang.Exception ("Cannot get Httpserver port number");
        
        return setting.getPort ();
    }
    
    /** Adds PropertyChangeListener to this browser.
     *
     * @param l Listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /** Removes PropertyChangeListener from this browser.
     *
     * @param l Listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
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
                        "sh", "-c", "xwininfo -root -tree|grep Netscape"}); // NOI18N
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
    
    public static class UnixBrowserFactory implements org.openide.awt.HtmlBrowser.Factory {
        
        org.openide.awt.HtmlBrowser.Factory oldFactory;
        
        public UnixBrowserFactory (org.openide.awt.HtmlBrowser.Factory oldFact) {
            oldFactory = oldFact;
        }
        
        public HtmlBrowser.Impl createHtmlBrowserImpl () {
            return new UnixBrowserImpl (oldFactory);
        }
    }
    
}

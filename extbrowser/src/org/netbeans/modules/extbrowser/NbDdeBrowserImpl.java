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
import org.openide.awt.HtmlBrowser;
import org.openide.options.SystemOption;
import org.openide.util.SharedClassObject;
import org.netbeans.modules.httpserver.*;

/**
 *
 *
 * @author  rkubacki
 * @version 
 */
public class NbDdeBrowserImpl extends org.openide.awt.HtmlBrowser.Impl {

    /** Netscape DDE topic names */
    public static final String BEGIN_PROGRESS     = "WWW_BeginProgress";     // NOI18N
    public static final String MAKING_PROGRESS    = "WWW_MakingProgress";    // NOI18N
    public static final String END_PROGRESS       = "WWW_EndProgress";       // NOI18N
    public static final String CANCEL_PROGRESS    = "WWW_CancelProgress";    // NOI18N
    public static final String SET_PROGRESS_RANGE = "WWW_SetProgressRange";  // NOI18N
    
    static {
        try {
            System.loadLibrary("extbrowser");   // NOI18N
        } catch(Exception e) {
            System.out.println(java.util.ResourceBundle.getBundle("org/netbeans/modules/extbrowser/Bundle").getString("ERR_cant_locate_dll"));
            e.printStackTrace ();
        }
    }
    
    private static java.util.ResourceBundle bundle = 
        java.util.ResourceBundle.getBundle("org/netbeans/modules/extbrowser/Bundle"); // NOI18N

    /** standart helper variable */
    private PropertyChangeSupport   pcs;
    
    /** component displayed in NB window */
    private HtmlBrowser.Impl   browserComp;

    /** requested URL */
    private URL url;
    
    /** status message */
    private String statusMsg = "";  // NOI18N
    
    /** title of displayed page */
    private String title = "";      // NOI18N
    
    /** name of DDE server receiving URLEcho notifications */
    private String  ddeUrlEchoSrvName = "NETBEANSURL";    // NOI18N
    
    private Thread  urlEchoThread = null;


    /** name of DDE server receiving progress */
    private String  ddeProgressSrvName;
    boolean bProgressInitialized = false;
    
    /** transaction ID for progress comunication, 
     *  it is incremented for each new reported progress 
     */
    protected int   txID = 0;
    
    /** windowID of servicing window (-1 if there is no assocciated window) */
    private int     currWinID = -1;
    
    /** Creates new NbDDEBrowserImpl */
    public NbDdeBrowserImpl(HtmlBrowser.Factory fact) {
        pcs = new PropertyChangeSupport (this);
        browserComp = fact.createHtmlBrowserImpl ();
        currWinID = -1;
    }

    /*
    public void finalize () {
    }
     */

    // native private void createDDE (String name, String topic)
    // throws NbBrowserException;

    native private byte [] reqDdeMessage (String srv, String topic, String item, int timeout)
    throws NbBrowserException;
    
    /** finds registry entry for browser opening */
    native private String getBrowserPath (String browser)
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
    
    
    /** Moves the browser forward. Failure is ignored.
     */
    public void backward() {
    }

    /** Moves the browser forward. Failure is ignored.
     */
    public void forward() {
    }

    /** Returns visual component of html browser.
     *
     * @return visual component of html browser.
     */
    public java.awt.Component getComponent() {
        /*
        if (browserComp == null)
            browserComp = initUIcomponents ();
         */
        return browserComp.getComponent ();
    }

    /** Returns status message representing status of html browser.
     *
     * @return status message.
     */
    public String getStatusMessage() {
        return statusMsg;
    }

    /** Returns title of the displayed page.
 * @return title 
 */
    public String getTitle () {
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

    /** Sets status message.
     *
     * @param statusMsg new status message
     */
    private void setStatusMessage(String statusMsg) {
        String oldMsg = this.statusMsg;
        this.statusMsg = statusMsg;
        pcs.firePropertyChange (PROP_STATUS_MESSAGE, oldMsg, statusMsg);
    }
        
    /** Sets title.
     *
     * @param title new title
     */
    private void setTitle(String title) {
        String old = this.title;
        this.title = title;
        pcs.firePropertyChange (PROP_TITLE, old, title);
    }
    
    /** Sets current URL.
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
            byte [] data;
            boolean hasNoWindow = (currWinID == -1);

            // initProgress ();

            String winID;
            // IE problem
            if (ExtBrowserSettings.OPTIONS.getBrowser().equals(ExtBrowserSettings.IEXPLORE))
                winID = "0xFFFFFFFF";
            else
                winID = "0x00000000"+Integer.toHexString (hasNoWindow? 0: currWinID).toUpperCase (); // NOI18N
            if (winID.length() > 10) winID = "0x"+winID.substring(winID.length()-8); // NOI18N
                
            try {
                data = reqDdeMessage (ExtBrowserSettings.OPTIONS.getBrowser (),"WWW_Activate",winID,3000);
            }
            catch (NbBrowserException ex) {
                // try to start browser and activet it again
                data = null;
                if (ExtBrowserSettings.OPTIONS.isStartWhenNotRunning ()) {
                    String b = getBrowserPath (ExtBrowserSettings.OPTIONS.getBrowser ());
                    if (b != null) {
                        if (b.charAt(0) == '"') {
                            int from, to;
                            from = b.indexOf ('"'); to = b.indexOf ('"', from+1);
                            b = b.substring (from+1, to);
                        }
                        else {
                            StringTokenizer st = new StringTokenizer(b);
                            b = st.nextToken();
                        }
                        setStatusMessage (bundle.getString("MSG_Running_command")+b);
                        Runtime.getRuntime ().exec (b);
                        // wait for browser start
                        Thread.currentThread ().sleep (7000);
                        data = reqDdeMessage (ExtBrowserSettings.OPTIONS.getBrowser (),"WWW_Activate",winID,3000);
                        hasNoWindow = false;
                    }
                }
            }
            
            if (data != null && data.length >= 4) {
                currWinID=DdeBrowserSupport.getDWORDAtOffset (data, 0);
                setStatusMessage (bundle.getString("MSG_use_win")+currWinID);
            }
            else {
                currWinID = -1;
                // System.out.println("Corrupted data read.");
                setStatusMessage (bundle.getString("ERR_cant_activate_browser"));
                return;
            }
            
            if (ExtBrowserSettings.OPTIONS.getBrowser().equals(ExtBrowserSettings.IEXPLORE))
                winID = hasNoWindow? "0": "-1";
            else
                winID = "0x00000000"+Integer.toHexString (hasNoWindow? 0: currWinID).toUpperCase (); // NOI18N
            if (winID.length() > 10) winID = "0x"+winID.substring(winID.length()-8); // NOI18N

            // nbfs can be displayed internally and in ext. viewer too
            String args1;
            args1="\""+url.toString()+"\",,"+winID+",0x1,,,"+(ddeProgressSrvName==null?"":ddeProgressSrvName);  // NOI18N
            data = reqDdeMessage (ExtBrowserSettings.OPTIONS.getBrowser (),"WWW_OpenURL",args1,3000); // NOI18N
            if (data != null && data.length >= 4) {
                if (!ExtBrowserSettings.OPTIONS.getBrowser ().equals ("IEXPLORE")) {
                    currWinID=DdeBrowserSupport.getDWORDAtOffset (data, 0);
                    if (currWinID < 0) currWinID = -currWinID;
                }
                setStatusMessage (bundle.getString ("MSG_use_win"));
            }
            URL oldUrl = this.url;
            this.url = url;
            pcs.firePropertyChange(PROP_URL, oldUrl, url);
        }
        catch (NbBrowserException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
        catch (java.io.IOException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
        catch (InterruptedException ex) {
            TopManager.getDefault ().notifyException (ex);
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
    
    /** Removes PropertyChangeListener from this browser.
     *
     * @param l Listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
    /** Adds PropertyChangeListener to this browser.
     *
     * @param l Listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    public static class NbDdeBrowserFactory implements org.openide.awt.HtmlBrowser.Factory {
        
        org.openide.awt.HtmlBrowser.Factory oldFactory;
        
        public NbDdeBrowserFactory (org.openide.awt.HtmlBrowser.Factory oldFact) {
            oldFactory = oldFact;
        }
        
        public HtmlBrowser.Impl createHtmlBrowserImpl () {
            return new NbDdeBrowserImpl (oldFactory);
        }
    }
    
}
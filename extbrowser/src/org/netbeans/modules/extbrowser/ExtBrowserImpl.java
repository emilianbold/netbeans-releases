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
import javax.swing.*;

import org.openide.*;
import org.openide.awt.HtmlBrowser;

/**
 * The ExtBrowserImpl is generalized external browser.
 *
 * @author Radim Kubacki
 */
public abstract class ExtBrowserImpl extends org.openide.awt.HtmlBrowser.Impl {
        
    /** standart helper variable */
    protected PropertyChangeSupport pcs;
    
    /** requested URL */
    protected URL url;
    protected String title = "";      // NOI18N
    
    /** What's this?? */ // TODO
    protected HtmlBrowser.BrowserComponent delegatingBrowser = null;
    
    /** reference to a factory to get settings */
    protected ExtWebBrowser extBrowserFactory;

    /** Default constructor. 
      * <p>Builds PropertyChangeSupport. 
      */
    public ExtBrowserImpl () {
        pcs = new PropertyChangeSupport (this);
    }
    
    /** Dummy implementations */
    public boolean isBackward() { return false; }
    public boolean isForward() { return false; }
    public void backward() { }
    public void forward() { }
    public boolean isHistory() { return false; }
    public void showHistory() {}
    public void stopLoading() { }
    
    protected void setTitle (String title) {
        return;
    }
    
    public String getTitle() {
        return "";
    }

    
    /** Returns status message representing status of html browser.
     *
     * @return status message.
     */
    public String getStatusMessage() {
        return "";
    }
        
    /** Call setURL again to force reloading.
     * Browser must be set to reload document and do not cache them.
     */
    public void reloadDocument() {
        if (url != null) {
            setURL(url);
        }
    }
        
    
    /** Returns current URL.
     *
     * @return current URL.
     */
    public URL getURL() {
        return url;
    }
    
    /** 
     *  Sets current URL. Descendants of this class will implement it and they can call this
     *  to display internal resources.
     *
     * @param url URL to show in the browser.
     */
    public void setURL(URL url) {
        if (url == null)
            return;
        
        if (isInternalProtocol (url.getProtocol ())) {
            if (delegatingBrowser == null) {
                delegatingBrowser = new HtmlBrowser.BrowserComponent ();
            }
            delegatingBrowser.open ();
            delegatingBrowser.requestFocus ();
            delegatingBrowser.setURL (url);

            URL old = this.url;
            this.url = url;
            pcs.firePropertyChange (PROP_URL, old, url);
        }
    }
    
    /** Returns visual component of html browser.
     *
     * @return visual component of html browser.
     */
    public final java.awt.Component getComponent() {
        return null;
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
     * Returns whether given protocol is internal or not. 
     * (Internal protocols cannot be displayed by external viewers.
     * They must be wrapped somehow.)
     *
     * @return true if protocol is internal, false otherwise
     */
    private static boolean isInternalProtocol (String protocol) {
        // internal protocols cannot be displayed in external viewer
        if (protocol.equals ("nbfs")               // NOI18N
        ||  protocol.equals ("nbres")              // NOI18N
        ||  protocol.equals ("nbrescurr")          // NOI18N
        ||  protocol.equals ("nbresloc")           // NOI18N
        ||  protocol.equals ("nbrescurrloc"))      // NOI18N
            return true;
        
        if (protocol.startsWith ("nb"))            // NOI18N
            return true;
        
        return false;
    }
    
}

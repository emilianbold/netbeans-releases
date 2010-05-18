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

package org.netbeans.modules.webclient;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.*;
import java.util.*;
import javax.swing.SwingUtilities;

import org.openide.*;
import org.openide.awt.*;

import org.mozilla.util.*;
import org.mozilla.webclient.*;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Real implementation of Mozilla browser functionality needed by IDE.
 *
 * @author  Radim.Kubacki@sun.com
 */
class WebclientBrowserImpl extends HtmlBrowser.Impl {

    private static boolean debug = true;
    
    // variables .................................................................
    
    /** browser factory */
    private WebclientBrowser        factory;

    /** browser controller */
    private BrowserControl          browserControl;
    /** browser visual component */
    private WebclientBrowserComponent browser;
    
    /** WindowControl for embedding */
    private WindowControl           winCtrl;
    /** browser navigation interface */
    private Navigation              navigation;
    /** browser history interface */
    private History                 browserHistory;
    
    /** browser event interface */
    private EventRegistration       eventRegistration;
    
    /** current URL */
    private URL                     url;
    /** standart helper variable */
    private PropertyChangeSupport   pcs;
    
    /** Current status message. */
    private String                  statusMessage = ""; // NOI18N
    
    /** Current value of title property. */
    private String                  title = ""; // NOI18N


    // init ......................................................................

    /**
    * Creates instance of browser.
    */
    public WebclientBrowserImpl (WebclientBrowser fact) {
        pcs = new PropertyChangeSupport (this);
        this.factory = fact;
        
        // Create the browser
        try {
            // This is a workaround - webclient requires jawt but it is not loaded yet (IDE is Swing app)
            System.loadLibrary("jawt"); // NOI18N
            
            if (factory.getAppData () != null)
                BrowserControlFactory.setAppData (factory.getAppData ().getAbsolutePath ());
            else
                throw new IllegalStateException (NbBundle.getMessage(WebclientBrowserImpl.class,"ERR_appData_path_is_not_set"));
            
            browserControl = BrowserControlFactory.newBrowserControl();
            
            winCtrl = (WindowControl) 
                browserControl.queryInterface(BrowserControl.WINDOW_CONTROL_NAME);
            if (debug) System.out.println("NativeWebShell="+winCtrl.getNativeWebShell());   // NOI18N
            
            browser = new WebclientBrowserComponent (this);
            
        }
        catch(Exception e) {
            ErrorManager.getDefault().annotate(e, NbBundle.getMessage(WebclientBrowserImpl.class,"ERR_Cannot_init_impl"));
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }
    
    public void initialize () {
        browser.setVisible(true);
        if (navigation == null ) {
            try {
                // PENDING: Need to wait until it gets inited
                for (int i=0; i<20; i++) {
                    if (debug) System.out.println("Checking ("+i+") NativeWebShell="+winCtrl.getNativeWebShell()); // NOI18N
                    if (winCtrl.getNativeWebShell() != -1) 
                        break;
                    try {
                        Thread.currentThread().sleep(1000);
                    }
                    catch (InterruptedException e) { // do nothing
                    }
                }
                if (winCtrl.getNativeWebShell() == -1) 
                    throw new IllegalStateException ("Cannot get native web shell");    // NOI18N
                
                navigation = (Navigation)
                    browserControl.queryInterface(BrowserControl.NAVIGATION_NAME);
                CurrentPage currentPage = (CurrentPage)
                    browserControl.queryInterface(BrowserControl.CURRENT_PAGE_NAME);
                browserHistory = (History)
                    browserControl.queryInterface(BrowserControl.HISTORY_NAME);

                eventRegistration = (EventRegistration)
                    browserControl.queryInterface(BrowserControl.EVENT_REGISTRATION_NAME);
                eventRegistration.addDocumentLoadListener (new DocumentLoadListener () {
                    public void eventDispatched(WebclientEvent event)
                    {
                        String old = statusMessage;
                        int eventType = (int)event.getType();
                        
                        if (eventType == (int)DocumentLoadEvent.START_DOCUMENT_LOAD_EVENT_MASK) {
                            setStatusMessage (NbBundle.getMessage(WebclientBrowserImpl.class,"MSG_Loading")); 
                            Object o = event.getEventData();
                            if (debug) System.out.println("start evt data: "+o+" class "+((o!=null)?o.getClass().getName():"null"));
                            if (o != null) {
                                if (o instanceof String) {
                                    try {
                                        url = new URL ((String)o);
                                        pcs.firePropertyChange (PROP_URL, null, null);
                                    }
                                    catch (MalformedURLException ex) {
                                        // do nothing - URL won't be updated
                                    }
                                }
                                else if (o instanceof URL) {
                                    url = (URL)o;
                                    pcs.firePropertyChange (PROP_URL, null, null);
                                }
                            }
                        }
                        else if (eventType == (int)DocumentLoadEvent.END_DOCUMENT_LOAD_EVENT_MASK) {
                            setStatusMessage (NbBundle.getMessage(WebclientBrowserImpl.class,"MSG_Done")); 
                            pcs.firePropertyChange (null, null, null);
                            if (debug) System.out.println("end evt data: "+event.getEventData());
                        }
                        else if (eventType == (int)DocumentLoadEvent.START_URL_LOAD_EVENT_MASK
                        ||       eventType == (int)DocumentLoadEvent.END_URL_LOAD_EVENT_MASK
                        ||       eventType == (int)DocumentLoadEvent.FETCH_INTERRUPT_EVENT_MASK
                        ||       eventType == (int)DocumentLoadEvent.PROGRESS_URL_LOAD_EVENT_MASK
                        ||       eventType == (int)DocumentLoadEvent.STATUS_URL_LOAD_EVENT_MASK
                        ||       eventType == (int)DocumentLoadEvent.UNKNOWN_CONTENT_EVENT_MASK) {
                            if (event.getEventData () instanceof String)
                                setStatusMessage ((String)event.getEventData ());
                        }
                    }
                });
                
            }
            catch(Exception e) {
                ErrorManager.getDefault().annotate(e, NbBundle.getMessage(WebclientBrowserImpl.class,"ERR_Cannot_init_impl"));
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }

    }
    
    /** 
     * Closes BrowserControl resources
     */
    public void destroy () {
        /*
        System.out.println("destroy");
        BrowserControlFactory.deleteBrowserControl(browserControl);
        browserControl = null;
        // if it is last
        try {
            BrowserControlFactory.appTerminate();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
         */
    }


    // HtmpBrowser.Impl implementation ...........................................

    /**
    * Returns visual component of html browser.
     * Actually returns null to allow custom window handling.
    *
    * @return <CODE>null</CODE>.
    */
    public java.awt.Component getComponent () {
        return null;
    }

    /**
    * Reloads current html page.
    */
    public void reloadDocument () {
        initialize ();
        navigation.refresh (Navigation.LOAD_FORCE_RELOAD);
    }

    /**
    * Stops loading of current html page.
    */
    public void stopLoading () {
        initialize ();
        navigation.stop();
    }

    /**
    * Sets current URL.
    *
    * @param url URL to show in the browser.
    */
    public void setURL (URL url) {
        if (SwingUtilities.isEventDispatchThread ()) {
            final URL newUrl = url;
            RequestProcessor.getDefault(). post (
                new Runnable () {
                    public void run () {
                        WebclientBrowserImpl.this.setURL (newUrl);
                    }
            });
            return;
        }
        
        try {
            initialize ();
            URL old = getURL ();

            // internal protocols cannot be displayed in external viewer
            if (isInternalProtocol (url.getProtocol ())) {
                url = URLUtil.createExternalURL(url);
            }
            
            if ((old != null) && old.equals (url)) {
                navigation.refresh (Navigation.LOAD_FORCE_RELOAD);
            } else {
                WindowControl winCtrl = (WindowControl) 
                    browserControl.queryInterface(BrowserControl.WINDOW_CONTROL_NAME);
                this.url = url;
                navigation.loadURL ( url.toString ());
                pcs.firePropertyChange (PROP_URL, old, url);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * Returns current URL.
    *
    * @return current URL.
    */
    public URL getURL () {
        return url;
    }

    /**
    * Returns status message representing status of html browser.
    *
    * @return status message.
    */
    public String getStatusMessage () {
        return statusMessage;
    }


    void setStatusMessage (String msg) {
        // XXX might be better to use own status bar
        StatusDisplayer.getDefault ().setStatusText (msg);
        String old = statusMessage;
        statusMessage = msg;
        pcs.firePropertyChange (PROP_STATUS_MESSAGE, old, statusMessage);
    }
    
    /** Returns title of the displayed page.
    * @return title 
    */
    public String getTitle () {
        return title;
    }

    /** Is forward button enabled?
    * @return true if it is
    */
    public boolean isForward () {
        if (url == null) return false;
        initialize ();
        return browserHistory.canForward ();
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void forward () {
        initialize ();
        browserHistory.forward();
    }

    /** Is backward button enabled?
    * @return true if it is
    */
    public boolean isBackward () {
        if (url == null) return false;
        initialize ();
        return browserHistory.canBack ();
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void backward () {
        initialize ();
        browserHistory.back();
    }

    /** Is history button enabled?
    * @return true if it is
    */
    public boolean isHistory () {
        if (url == null) return false;
        initialize ();
        return (browserHistory.getHistoryLength() > 0);
    }

    /** Invoked when the history button is pressed.
    */
    public void showHistory () {
        // PENDING: use history manager
    }

    /**
    * Adds PropertyChangeListener to this browser.
    *
    * @param l Listener to add.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes PropertyChangeListener from this browser.
    *
    * @param l Listener to remove.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }


    // other methods ..............................................................

    
    /**
     * Returns BrowserControl interface
     */
    public BrowserControl getBrowserControl () {
        return browserControl;
    }
    
    /**
     * Returns whether given protocol is internal or not. 
     * (Internal protocols cannot be displayed by external viewers.
     * They must be wrapped somehow.)
     *
     * @return true if protocol is internal, false otherwise
     */
    protected final static boolean isInternalProtocol (String protocol) {
        if (protocol.startsWith ("nb"))            // NOI18N
            return true;
        
        return false;
    }
}

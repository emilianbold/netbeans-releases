/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.beans.*;
import org.openide.ErrorManager;

import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.awt.HtmlBrowser;

/** Factory and descriptions for external browser
 */

public class ExtWebBrowser implements HtmlBrowser.Factory, java.io.Serializable {

    private static final long serialVersionUID = -3021027901671504127L;
        
    public static final String PROP_NAME = "name"; // NOI18N
    
    /** Browser executable property name */
    public static final String PROP_BROWSER_EXECUTABLE = "browserExecutable"; // NOI18N
    
    /** DDE server property name */
    public static final String PROP_DDESERVER = "dDEServer";   // NOI18N
    
    /** Browser start timeout property name */
    public static final String PROP_BROWSER_START_TIMEOUT = "browserStartTimeout";   // NOI18N

    /** DDE activate timeout property name */
    public static final String PROP_DDE_ACTIVATE_TIMEOUT = "activateTimeout";   // NOI18N
    
    /** DDE openURL timeout property name */
    public static final String PROP_DDE_OPENURL_TIMEOUT = "openurlTimeout";     // NOI18N
    
    /** Name of DDE server corresponding to Netscape Navigator 4.x */
    public static final String NETSCAPE = "NETSCAPE";   // NOI18N
    /** Name of DDE server corresponding to Internet Explorer */
    public static final String IEXPLORE = "IEXPLORE";   // NOI18N
    /** Name of DDE server corresponding to Mozilla */
    public static final String MOZILLA  = "MOZILLA";    // NOI18N
    /** Name of DDE server corresponding to Netscape 6.x */
    public static final String NETSCAPE6 = "NETSCAPE6";   // NOI18N
    
    /** storage for DDE server property */
    protected String ddeServer;
    
    /** storage for starting browser timeout property */
    protected java.lang.Integer browserStartTimeout = new Integer(defaultBrowserStartTimeout);

    /** storage for DDE openURL timeout property */
    protected java.lang.Integer openurlTimeout = new Integer(defaultOpenUrlTimeout);

    /** storage for DDE activate timeout property */
    protected java.lang.Integer activateTimeout = new Integer(defaultActivateTimeout);

    /** Default timeout for starting the browser */
    public static final int defaultBrowserStartTimeout = 6000;
    
    /** Default for DDE activate timeout property */
    private static final int defaultActivateTimeout = 15000;

    /** Default for DDE openURL timeout property */
    private static final int defaultOpenUrlTimeout = 3000;

    /** Logger for extbrowser module. */
    private static ErrorManager err = ErrorManager.getDefault ().getInstance ("org.netbeans.modules.extbrowser");   // NOI18N
    
    protected String name;
    
    public static ErrorManager getEM () {
        return err;
    }
        
    /** Holds value of property browserExecutable. */
    private NbProcessDescriptor browserExecutable;
    
    protected transient PropertyChangeSupport pcs;
    
    /** Creates new Browser */
    public ExtWebBrowser () {
        init();
    }

    /** initialize object */
    private void init () {
        pcs = new PropertyChangeSupport (this);
    }
                
    /**
     * Gets DDE server name
     * @return server name of DDEserver.
     *         <CODE>null</CODE> when no server is selected (means default web browser).
     */
    public String getDDEServer () {
        return ddeServer;
    }
    
    /**
     * Sets DDE server name
     * @param ddeServer name of DDE server or <CODE>null</CODE>
     */
    public void setDDEServer (String ddeServer) {
        if ((ddeServer != null) && !ddeServer.equals(this.ddeServer)) {
            String old = this.ddeServer;
            this.ddeServer = ddeServer;
            pcs.firePropertyChange (PROP_DDESERVER, old, ddeServer);
            getEM().log("DDEServer changed to: " + ddeServer);
        }
    }
   
    /** Getter for property browserStartTimeout.
     * @return Value of property browserStartTimeout.
     *
     */
    public java.lang.Integer getBrowserStartTimeout() {
        return browserStartTimeout;
    }
    
    /** Setter for property browserStartTimeout.
     * @param browserStartTimeout New value of property browserStartTimeout.
     *
     */
    public void setBrowserStartTimeout(java.lang.Integer browserStartTimeout) {
        if ((browserStartTimeout != null) && !(browserStartTimeout.intValue() == this.browserStartTimeout.intValue())) {
            Integer oldVal = this.browserStartTimeout;
            this.browserStartTimeout = browserStartTimeout;
            pcs.firePropertyChange(PROP_BROWSER_START_TIMEOUT, oldVal, browserStartTimeout);
        }
    }
    
    /** Getter for property openurlTimeout.
     * @return Value of property openurlTimeout.
     *
     */
    public java.lang.Integer getOpenurlTimeout() {
        return openurlTimeout;
    }
    
    /** Setter for property openurlTimeout.
     * @param openurlTimeout New value of property openurlTimeout.
     *
     */
    public void setOpenurlTimeout(java.lang.Integer openurlTimeout) {
        if ((openurlTimeout != null) && !(openurlTimeout.intValue() == this.openurlTimeout.intValue())) {
            Integer oldVal = this.openurlTimeout;
            this.openurlTimeout = openurlTimeout;
            pcs.firePropertyChange(PROP_DDE_OPENURL_TIMEOUT, oldVal, openurlTimeout);
        }
    }
    
    /** Getter for property activeTimeout.
     * @return Value of property activeTimeout.
     *
     */
    public java.lang.Integer getActivateTimeout() {
        return activateTimeout;
    }
    
    /** Setter for property activeTimeout.
     * @param activeTimeout New value of property activeTimeout.
     *
     */
    public void setActivateTimeout(java.lang.Integer activateTimeout) {
        if ((activateTimeout != null) && !(activateTimeout.intValue() == this.activateTimeout.intValue())) {
            Integer oldVal = this.activateTimeout;
            this.activateTimeout = activateTimeout;
            pcs.firePropertyChange(PROP_DDE_ACTIVATE_TIMEOUT, oldVal, activateTimeout);
        }
    }

    /** Setter for browser name
     */
    public void setName(String name) {
        if ((name != null) && (!name.equals(this.name))) {
            String oldVal = this.name;
            this.name = name;
            pcs.firePropertyChange(PROP_NAME, oldVal, name);
        }
    }
    
    /** Getter for property browserExecutable.
     * @return Value of property browserExecutable.
     */
    public NbProcessDescriptor getBrowserExecutable () {
        if (browserExecutable == null || "".equals(browserExecutable.getProcessName())) { // NOI18N
            return defaultBrowserExecutable();
        }
        return browserExecutable;
    }

    /** Setter for property browserExecutable.
     * @param browserExecutable New value of property browserExecutable.
     */
    public void setBrowserExecutable (NbProcessDescriptor browserExecutable) {
        if ((browserExecutable != null) && (!browserExecutable.equals(this.browserExecutable))) {
            NbProcessDescriptor oldVal = this.browserExecutable;
            this.browserExecutable = browserExecutable;
            pcs.firePropertyChange(PROP_BROWSER_EXECUTABLE, oldVal, browserExecutable);
        }
        if (browserExecutable == null) {
            NbProcessDescriptor oldVal = this.browserExecutable;
            this.browserExecutable = defaultBrowserExecutable();;
            pcs.firePropertyChange(PROP_BROWSER_EXECUTABLE, oldVal, browserExecutable);
        }
    }

    /** Default command for browser execution.
     *  Can be overriden to return browser that suits to platform and settings.
     *
     * @return netscape without any argument.
     */
    protected NbProcessDescriptor defaultBrowserExecutable () {
        return new NbProcessDescriptor ("netscape", "");    // NOI18N
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        return null;
    }
    
    /**
     * @param l new PropertyChangeListener */    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * @param l PropertyChangeListener to be removed */    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
    private void readObject (java.io.ObjectInputStream ois) 
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        if (browserExecutable != null && browserExecutable.getArguments() != null) {
            // replace old {params} with {URL}
            String args = browserExecutable.getArguments();
            int idx = args.indexOf("{params}"); // NOI18N
            if (idx >= 0) {
                browserExecutable = new NbProcessDescriptor (
                    browserExecutable.getProcessName(),
                    args.substring(0, idx)+"-remote openURL({URL})"+args.substring(idx+8), // NOI18N
                    NbBundle.getMessage (ExtWebBrowser.class, "MSG_BrowserExecutorHint")
                );
            }
        }
        init ();
    }

    /** Default format that can format tags related to execution. 
     * Currently this is only the URL.
     */
    public static class UnixBrowserFormat extends org.openide.util.MapFormat {
        
        /** SVUID for serialization. */
        private static final long serialVersionUID = -699340388834127437L;
        
        /** Tag used to pass URL */
        public static final String TAG_URL = "URL"; // NOI18N
        
        /** Creates UnixBrowserFormat for URL.
         * @param url to specify URL
         */
        public UnixBrowserFormat (String url) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();        
            map.put (TAG_URL, url);
        }    
    }
    
}

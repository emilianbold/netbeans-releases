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
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;

/** Simple external browser that uses new process for each URL request.
 *  Typically it runs command like <CODE>netscape [url]</CODE>.
 *
 * @author  Radim Kubacki
 */
public class SimpleExtBrowser implements HtmlBrowser.Factory, java.io.Serializable {
    
    public static final String PROP_NAME = "name";   // NOI18N
    public static final String PROP_DESCRIPTION = "description";   // NOI18N
    public static final String PROP_BROWSER_EXECUTABLE = "browserExecutable"; // NOI18N
    
    /** process descriptor for browser */
    private NbProcessDescriptor process;
    
    /** holds description of browser */
    protected String name;
    
    private PropertyChangeSupport pcs;

    protected String browserName;
    
    private static final long serialVersionUID = -8494345762328555637L;
    
    /** Creates new SimpleExtBrowser */
    public SimpleExtBrowser() {
        if (Utilities.getOperatingSystem () == Utilities.OS_OS2) {
            process = new NbProcessDescriptor(
                "Netscape.exe", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        }
	else if (Utilities.getOperatingSystem () == Utilities.OS_MAC) {
            process = new NbProcessDescriptor(
                "/usr/bin/open", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        }
        else {
            process = new NbProcessDescriptor(
                // empty string for process
                "", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        }
        pcs = new PropertyChangeSupport (this);
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    public String getName() {
        if (name == null) {
            this.name = NbBundle.getMessage(ExtWebBrowser.class, "CTL_SimpleExtBrowser");
        }
        return name;
    }
    
    /** Setter for browser name
     */
    public void setName(String name) {
        String oldVal = this.name;
        if (name == null) return;
        if (!name.equals(oldVal)) {
            this.name = name;
            pcs.firePropertyChange(PROP_NAME, oldVal, name);
        }
    }
    
    /** Getter for property name. If name is not set then default name of bean is returned.
     *
     * @return Value of property name.
     */
    public java.lang.String getDescription () {
        if (name == null) {
            try {
                return Introspector.getBeanInfo (getClass ()).getBeanDescriptor ().getDisplayName ();
            } catch (Exception e) {
                // Catching IntrospectionException, but also maybe NullPointerException...?
                ErrorManager.getDefault ().notify (
                    ErrorManager.INFORMATIONAL,
                    e
                );
            }
        }
        return name;
    }    
    
    /** Setter for property name.
     * @param name New value of property name.
     */
    public void setDescription (java.lang.String name) {
        if (!name.equals (this.name)) {
            String old = this.name;
            this.name = name;
            pcs.firePropertyChange (PROP_DESCRIPTION, old, name);
        }
    }

    /** Builds new implementation of specified browser.
     * If embeddable property is true, then it is expected
     * that returned implementation will implement org.openide.awt.HtmlBrowser.Impl
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        return new SimpleExtBrowser.SimpleExtBrowserImpl ();
    }
    
    /** getter for process property */
    public NbProcessDescriptor getBrowserExecutable () {
        return process;
    }

    /** setter for process property */
    public void setBrowserExecutable (NbProcessDescriptor process) {
        NbProcessDescriptor old = this.process;
        this.process = process;
        pcs.firePropertyChange (PROP_BROWSER_EXECUTABLE, old, process);
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

    /** Class that implements browsing.
     *  It starts new process whenever it is asked to display URL.
     */
    public class SimpleExtBrowserImpl extends HtmlBrowser.Impl {
        
        PropertyChangeSupport pcs;
        URL url;
        
        SimpleExtBrowserImpl () {
            super ();
            pcs = new PropertyChangeSupport (this);
        }
        
        /** Given URL is displayed. 
          *  Configured process is started to satisfy this request. 
          */
        public void setURL(URL url) {
            if (url == null)
                return;
            try {
                if (url.getProtocol().equals("nbfs")) {   // NOI18N
                    url = URLUtil.createExternalURL(url, false);
                }
                process.exec(new BrowserFormat((url == null)? "": url.toString())); // NOI18N
                this.url = url;
            } catch (IOException ex) {
                org.openide.DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    NbBundle.getBundle(SimpleExtBrowser.class).getString("EXC_Invalid_Processor"),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
                    )
                );
            }
        }
        
        /** forces browser to refresh its content 
         */
        public void reloadDocument () {
            if (url != null)
                setURL (url);
            return;
        }
        
        /** Cancels loading of document (if it is possible)  
          */
        public void stopLoading () {
            return;
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
        
        /** Dummy. */
        public boolean isForward() { return false; }
        /** Dummy. */
        public void forward() {}
        /** Dummy. */
        public boolean isBackward() { return false; }
        /** Dummy. */
        public void backward() {}
        /** Dummy. */
        public boolean isHistory() { return false; }
        /** Dummy. */
        public void showHistory() {}
        /** Dummy. */
        public String getStatusMessage() { return ""; }   // NOI18N
        /** Dummy. */
        public String getTitle() { return ""; }   // NOI18N
        /** Dummy. */
        public URL getURL() { return url; }
        
        /**
         * Returns null as it is external browser.
         *
         * @return null
         */
        public java.awt.Component getComponent() {
            return null;
        }
        
    }
    
    /** Default format that can format tags related to execution. Currently this is only the URL.
     */
    private static class BrowserFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 5990981835151848381L;
        /** Tag replaced with the URL */
        public static final String TAG_URL = "URL";  // NOI18N
        
        
        /** @param info exec info about class to execute
         * @param classPath to substitute instead of CLASSPATH
         * @param bootClassPath boot class path
         * @param repository repository path
         * @param library library path
         */
        public BrowserFormat (String url) {
            super(new HashMap());
            java.util.Map map = getMap ();
            
            map.put (TAG_URL, url);
        }
        
    }
}

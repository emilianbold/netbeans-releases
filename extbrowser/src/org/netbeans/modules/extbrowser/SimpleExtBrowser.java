/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.beans.*;
import java.io.IOException;
import java.net.URL;

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Simple external browser that uses new process for each URL request.
 *  Typically it runs command like <CODE>netscape [url]</CODE>.
 *
 * @author  Radim Kubacki
 */
public class SimpleExtBrowser implements HtmlBrowser.Factory, java.io.Serializable {
    
    public static final String PROP_NAME = "BrowserName";   // NOI18N
    
    private static NbProcessDescriptor DEFAULT_EXTERNAL_BROWSER = new NbProcessDescriptor(
        // empty string for process
        "", // NOI18N
        // {URL}
        " {" + BrowserFormat.TAG_URL + "}", // NOI18N
        NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
    );

    /** process descriptor for browser */
    private NbProcessDescriptor process;
    
    protected String name;
    
    private PropertyChangeSupport pcs;

    private static final long serialVersionUID = -8494345762328555637L;
    
    /** Creates new SimpleExtBrowser */
    public SimpleExtBrowser() {
        process = DEFAULT_EXTERNAL_BROWSER;
        pcs = new PropertyChangeSupport (this);
    }
    
    /** Getter for property name. If name is not set then default name of bean is returned.
     *
     * @return Value of property name.
     */
    public java.lang.String getName () {
        if (name == null) {
            try {
                return Introspector.getBeanInfo (getClass ()).getBeanDescriptor ().getDisplayName ();
            } catch (Exception e) {
                // Catching IntrospectionException, but also maybe NullPointerException...?
                TopManager.getDefault ().getErrorManager ().notify (
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
    public void setName (java.lang.String name) {
        if (!name.equals (this.name)) {
            String old = this.name;
            this.name = name;
            pcs.firePropertyChange (PROP_NAME, old, name);
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
    public NbProcessDescriptor getProcess() {
        return process;
    }

    /** setter for process property */
    public void setProcess(NbProcessDescriptor process) {
        this.process = process;
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
        public void setURL (URL url) {
            try {
              process.exec(new BrowserFormat(new ExecInfo(""), (url == null)? "": url.toString())); // NOI18N
              this.url = url;
            } catch (IOException ex) {
              TopManager.getDefault().notify(
                new NotifyDescriptor.Exception(ex,
                  NbBundle.getBundle(SimpleExtBrowser.class).getString("EXC_Invalid_Processor")
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
        public String getStatusMessage() { return ""; }
        /** Dummy. */
        public String getTitle() { return ""; }
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
    private static class BrowserFormat extends org.openide.execution.ProcessExecutor.Format {
        /** Tag replaced with the URL */
        public static final String TAG_URL = "URL"; // NOI18N
        
        /** @param info exec info about class to execute
         * @param classPath to substitute instead of CLASSPATH
         * @param bootClassPath boot class path
         * @param repository repository path
         * @param library library path
         */
        public BrowserFormat (ExecInfo info, String url) {
            super(info);
            java.util.Map map = getMap ();
            
            map.put (TAG_URL, url);
        }
        
    }
}

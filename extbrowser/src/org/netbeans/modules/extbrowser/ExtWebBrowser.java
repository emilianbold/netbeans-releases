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

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.awt.HtmlBrowser;

/** Factory and descriptions for external browser
 */

public class ExtWebBrowser implements HtmlBrowser.Factory, java.io.Serializable {

    private static final long serialVersionUID = -3021027901671504127L;
    
    public static final String PROP_BROWSER_NAME = "name"; // NOI18N
    public static final String PROP_BROWSER_EXECUTABLE = "browserExecutable"; // NOI18N
    
    /** command that executes the browser. Used in an old version. */
    private String executable;
    
    /** Holds value of property browserExecutable. */
    private NbProcessDescriptor browserExecutable;
    
    protected transient PropertyChangeSupport pcs;
    
    /** Creates new Browser */
    public ExtWebBrowser () {
        init ();
    }

    /** initialize object */
    private void init () {
        pcs = new PropertyChangeSupport (this);
    }

    /** Getter for browser name
     *  @return browserName name of browser
     */
    public String getName () {
        return NbBundle.getMessage (ExtWebBrowser.class, "CTL_ExternalBrowser");
    }
    
    /** Getter for property browserExecutable.
     * @return Value of property browserExecutable.
     */
    public NbProcessDescriptor getBrowserExecutable () {
        if (browserExecutable == null || "".equals (browserExecutable.getProcessName ())) { // NOI18N
            return defaultBrowserExecutable ();
        }
        return browserExecutable;
    }

    /** Setter for property browserExecutable.
     * @param browserExecutable New value of property browserExecutable.
     */
    public void setBrowserExecutable (NbProcessDescriptor browserExecutable) {
        NbProcessDescriptor old = this.browserExecutable;
        this.browserExecutable = browserExecutable;
        pcs.firePropertyChange (PROP_BROWSER_EXECUTABLE, old, browserExecutable);
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
        ExtBrowserImpl impl = null;

        if (org.openide.util.Utilities.isUnix ())
            impl = new UnixBrowserImpl (this);
        else if (org.openide.util.Utilities.isWindows ())
            impl = new NbDdeBrowserImpl (new WinWebBrowser ());
        return impl;
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
        if (executable != null) {
            if (executable.charAt (0) == '"') {
                int idx = executable.indexOf ('"', 1);
                if (idx > 0)
                    executable = executable.substring (1, idx);
                else
                    executable = executable.substring (1);
            }
            browserExecutable = new NbProcessDescriptor (executable, "");   // NOI18N
            executable = null;
        }
        init ();
    }
    
}

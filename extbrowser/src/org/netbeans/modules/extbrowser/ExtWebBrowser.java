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
import org.openide.util.NbBundle;
import org.openide.awt.HtmlBrowser;

/** Factory and descriptions for external browser
 */

public class ExtWebBrowser implements HtmlBrowser.Factory, java.io.Serializable {

    private static final long serialVersionUID = -3021027901671504127L;
    
    private static final String PROP_EXECUTABLE = "Executable"; // NOI18N
    
    /** command that executes the browser */
    private String executable;
    
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
    
    public String getExecutable () {
        return executable;
    }
    
    public void setExecutable (String executable) {
        String old = this.executable;
        this.executable = executable;
        pcs.firePropertyChange (PROP_EXECUTABLE, old, executable);
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
        init ();
    }
}

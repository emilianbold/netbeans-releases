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

    private PropertyChangeSupport pcs;
    
    private static final long serialVersionUID = -3021027901671504127L;
    
    /** Creates new Browser */
    public ExtWebBrowser () {
        pcs = new PropertyChangeSupport (this);
    }

    /** Getter for browser name
     *  @return browserName name of browser
     */
    public String getName () {
        return NbBundle.getMessage (ExtWebBrowser.class, "CTL_ExternalBrowser");
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (org.openide.util.Utilities.isUnix ())
            impl = new UnixBrowserImpl ();
        else if (org.openide.util.Utilities.isWindows ())
            impl = new NbDdeBrowserImpl ();
        return impl;
    }
    
    /**
     * @param l new PropertyChangeListener */    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        if (pcs == null)
            pcs = new PropertyChangeSupport (this);
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * @param l PropertyChangeListener to be removed */    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
}

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

package org.netbeans.core.ui;

import java.beans.*;
import java.lang.reflect.Constructor;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/** Factory and descriptions for default Swing based browser
 */

public class SwingBrowser implements HtmlBrowser.Factory, java.io.Serializable {

    /** Property name */
    public static final String PROP_DESCRIPTION = "description"; // NOI18N

    protected transient PropertyChangeSupport pcs;
    
    private static final long serialVersionUID = -3735603646171376891L;
    
    /** Creates new Browser */
    public SwingBrowser () {
        init ();
    }

    /** initialize object */
    private void init () {
        pcs = new PropertyChangeSupport (this);
    }

    /** Getter for browser name
     *  @return browserName name of browser
     */
    public String getDescritpion () {
        return NbBundle.getMessage (SwingBrowser.class, "LBL_SwingBrowserDescription");
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        try {
            Class clz = Class.forName ("org.openide.awt.SwingBrowserImpl"); // NOI18N
            Constructor con = clz.getDeclaredConstructor (new Class [] {});
            con.setAccessible (true);
            return (HtmlBrowser.Impl)con.newInstance (new Object [] {});
        }
        catch (Exception ex) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Message (NbBundle.getMessage (SwingBrowser.class, "MSG_cannot_create_browser"))
            );
            return null;
        }
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

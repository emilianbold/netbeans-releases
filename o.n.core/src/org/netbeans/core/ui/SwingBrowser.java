/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.ui;

import java.beans.*;
import java.lang.reflect.Constructor;

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
            Class<?> clz = Class.forName ("org.openide.awt.SwingBrowserImpl"); // NOI18N
            Constructor con = clz.getDeclaredConstructor (new Class [] {});
            con.setAccessible (true);
            return (HtmlBrowser.Impl)con.newInstance (new Object [] {});
        }
        catch (Exception ex) {
            org.openide.DialogDisplayer.getDefault ().notify (
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

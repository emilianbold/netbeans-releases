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

package org.netbeans.modules.webclient;

import java.beans.*;

import org.openide.awt.HtmlBrowser;

/**
 * HTML browser that can be used in NetBeans IDE.
 * It uses webclient interface to Mozilla
 *
 * @author Radim.Kubacki@sun.com
 */
public class WebclientBrowser implements HtmlBrowser.Factory, java.io.Serializable {

    private static final long serialVersionUID = -3926191994353231536L;

    /** variable that can hold appDataPath value */
    private static final String PROP_APP_DATA_PATH = "MOZILLA_FIVE_HOME";   // NOI18N

    /** path to browser binaries */
    private java.io.File appData;
    
    private PropertyChangeSupport pcs;
    
    public WebclientBrowser () {
        init ();
    }
    
    private void init () {
        pcs = new PropertyChangeSupport (this);
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl () {
        try {
            return new WebclientBrowserImpl (this);
        }
        catch (Error e) {
            e.printStackTrace ();
            throw e;
        }
    }
    
    /** Getter for property appData.
     * @return Value of property appData.
     */
    public java.io.File getAppData () {
        if (appData == null) {
            if (System.getProperty (PROP_APP_DATA_PATH) != null) 
                return new java.io.File (System.getProperty (PROP_APP_DATA_PATH));
        }
        return appData;
    }
    
    /** Setter for property appData.
     * @param appData New value of property appData.
     */
    public void setAppData (java.io.File appData) {
        java.io.File old = this.appData;
        this.appData = appData;
        pcs.firePropertyChange (PROP_APP_DATA_PATH, old, appData);
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

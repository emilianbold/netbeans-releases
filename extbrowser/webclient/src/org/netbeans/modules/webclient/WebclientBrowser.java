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

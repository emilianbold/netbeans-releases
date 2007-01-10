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

package org.netbeans.modules.welcome.content;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
class HttpProxySettings {

    private static HttpProxySettings theInstance;
    private static Preferences proxySettingsNode;

    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );

    public static final String PROXY_SETTINGS = "ProxySettings"; // NOI18N
    
    /** Creates a new instance of HttpProxySettings */
    private HttpProxySettings() {
        initProxyMethodsMaybe();
    }

    public static HttpProxySettings getDefault() {
        if( null == theInstance ) {
            theInstance = new HttpProxySettings();
        }
        return theInstance;
    }

    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.removePropertyChangeListener( l );
    }

    public void showConfigurationDialog() {
        OptionsDisplayer.getDefault().open( "General" );//NOI18N
    }

    private static synchronized void initProxyMethodsMaybe() {
        proxySettingsNode = NbPreferences.root ().node ("/org/netbeans/core");
        assert proxySettingsNode != null;
        proxySettingsNode.addPreferenceChangeListener (new PreferenceChangeListener (){
            public void preferenceChange (PreferenceChangeEvent evt) {
                if (evt.getKey ().startsWith ("proxy") || evt.getKey ().startsWith ("useProxy")) {
                    getDefault ().propertySupport.firePropertyChange (PROXY_SETTINGS, null, getDefault());
                }
            }
        });
    }
}

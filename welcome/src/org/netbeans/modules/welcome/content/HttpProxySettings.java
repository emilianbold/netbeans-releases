/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;

/**
 *
 * @author S. Aubrecht
 */
class HttpProxySettings {

    private static HttpProxySettings theInstance;

    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );

    public static final String PROXY_SETTINGS = "ProxySettings"; // NOI18N

    /** Creates a new instance of HttpProxySettings */
    private HttpProxySettings() {
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
        if( ProxyDialog.showDialog() ) {
            propertySupport.firePropertyChange( PROXY_SETTINGS, null, this );
        }
    }

    // Try to avoid referring directly to IDESettings.
    // If we can in fact find IDESettings and all appropriate methods, then we
    // use them. This means proxy config etc. will be properly persisted in
    // the system option. If something goes wrong, log it quietly and revert
    // to just setting the system properties (valid just for the session duration).
    
    private static Object settingsInstance;
   
    private static Method mGetProxyType, mSetProxyType, mGetProxyHost, mSetProxyHost, mGetProxyPort, mSetProxyPort;
    
    private static boolean useReflection() {
        initProxyMethodsMaybe();
        return mSetProxyPort != null;
    }

    private static boolean reflectionAlreadyTried = false;
    
    private static synchronized void initProxyMethodsMaybe() {
        if (reflectionAlreadyTried)
            return;
        
        reflectionAlreadyTried = true;
        
        try {
            ClassLoader l = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            Class clazz = l.loadClass("org.netbeans.core.IDESettings"); // NOI18N
            settingsInstance = SharedClassObject.findObject(clazz, true);
            mGetProxyType = clazz.getMethod ("getProxyType", null); // NOI18N
            mSetProxyType = clazz.getMethod ("setProxyType", new Class[] {Integer.TYPE}); // NOI18N
            mGetProxyHost = clazz.getMethod("getUserProxyHost", null); // NOI18N
            mSetProxyHost = clazz.getMethod("setUserProxyHost", new Class[] {String.class}); // NOI18N
            mGetProxyPort = clazz.getMethod("getUserProxyPort", null); // NOI18N
            mSetProxyPort = clazz.getMethod("setUserProxyPort", new Class[] {String.class}); // NOI18N
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            // OK, use system properties rather than reflection.
        }
    }

    private static final String PROXY_HOST = "http.proxyHost"; // NOI18N
    private static final String PROXY_PORT = "http.proxyPort"; // NOI18N

    /** Gets proxy usage */
    static int getProxyType () {
        if (useReflection()) {
            try {
                return ((Integer)mGetProxyType.invoke(settingsInstance, new Object[0])).intValue();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                // return AUTO_DETECT_PROXY as default
                return 1;
            }
        } else {
            // XXX: return AUTO_DETECT_PROXY as default
            return 1;
        }
    }

    /** Gets Proxy Host */
    static String getUserProxyHost() {
        if (useReflection()) {
            try {
                return (String)mGetProxyHost.invoke(settingsInstance, new Object[0]);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return null;
            }
        } else {
            return System.getProperty(PROXY_HOST);
        }
    }

    /** Gets Proxy Port */
    static String getUserProxyPort() {
        if (useReflection()) {
            try {
                return (String)mGetProxyPort.invoke(settingsInstance, new Object[0]);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return null;
            }
        } else {
            return System.getProperty(PROXY_PORT);
        }
    }

    /** Sets the whole proxy configuration */
    static void setProxyConfiguration (int proxyType, String host, String port) {
        if (useReflection()) {
            try {
                mSetProxyType.invoke (settingsInstance, new Object[] {Integer.valueOf (proxyType)});
                mSetProxyHost.invoke (settingsInstance, new Object[] {host});
                mSetProxyPort.invoke (settingsInstance, new Object[] {port});
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        } else {
            // XXX
            if (proxyType == 0) {
                System.setProperty(PROXY_HOST, ""); // NOI18N
                System.setProperty(PROXY_PORT, ""); // NOI18N
            } else {
                System.setProperty(PROXY_HOST, host);
                System.setProperty(PROXY_PORT, port);
            }
        }
    }
}

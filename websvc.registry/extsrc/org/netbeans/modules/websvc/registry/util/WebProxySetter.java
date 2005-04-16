/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.util;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ClassLoader;
import java.lang.reflect.Method;
import javax.swing.JDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;

/**
 *
 * @author  Winston Prakash
 */
public class WebProxySetter {
    
    private final Integer MANUAL_SET_PROXY = new Integer(2);
    private final String PROXY_HOST = "http.proxyHost"; // NOI18N
    private final String PROXY_PORT = "http.proxyPort"; // NOI18N
    
    private static WebProxySetter defaultInstance = new WebProxySetter();
    
    // Try to avoid referring directly to IDESettings.
    // If we can in fact find IDESettings and all appropriate methods, then we
    // use them. This means proxy config etc. will be properly persisted in
    // the system option. If something goes wrong, log it quietly and revert
    // to just setting the system properties (valid just for the session duration).
    
    private Object settingsInstance;
    private Method mGetUseProxy, mSetUseProxy, mGetProxyHost, mSetProxyHost, mGetProxyPort, mSetProxyPort;
    
    private WebProxySetter() {
        try {
            ClassLoader l = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            Class clazz = l.loadClass("org.netbeans.core.IDESettings"); // NOI18N
            settingsInstance = SharedClassObject.findObject(clazz, true);
            mGetUseProxy = clazz.getMethod("getProxyType", null); // NOI18N
            mSetUseProxy = clazz.getMethod("setProxyType", new Class[] {Integer.TYPE}); // NOI18N
            mGetProxyHost = clazz.getMethod("getUserProxyHost", null); // NOI18N
            mSetProxyHost = clazz.getMethod("setUserProxyHost", new Class[] {String.class}); // NOI18N
            mGetProxyPort = clazz.getMethod("getUserProxyPort", null); // NOI18N
            mSetProxyPort = clazz.getMethod("setUserProxyPort", new Class[] {String.class}); // NOI18N
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            // OK, use system properties rather than reflection.
        }
    }
    
    public static WebProxySetter getInstance(){
        return defaultInstance;
    }
    
    /** Gets Proxy Host */
    public String getProxyHost() {
        
        try {
            return (String)mGetProxyHost.invoke(settingsInstance, new Object[0]);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return System.getProperty(PROXY_HOST);
        }
    }
    
    /** Gets Proxy Port */
    public String getProxyPort() {
        try {
            return (String)mGetProxyPort.invoke(settingsInstance, new Object[0]);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return System.getProperty(PROXY_PORT);
        }
    }
    
    /** Sets the whole proxy configuration */
    public void setProxyConfiguration(String host, String port ) {
        try {
            mSetUseProxy.invoke(settingsInstance,new Object[] { MANUAL_SET_PROXY });
            mSetProxyHost.invoke(settingsInstance, new Object[] {host});
            mSetProxyPort.invoke(settingsInstance, new Object[] {port});
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        System.setProperty(PROXY_HOST, host);
        System.setProperty(PROXY_PORT, port);
    }
}

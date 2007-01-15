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

package org.netbeans.core;

import java.lang.reflect.Field;
import java.net.ProxySelector;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import junit.framework.TestResult;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.util.NbPreferences;

/** Tests HTTP Proxy settings.
 *
 * @author Jiri Rechtacek
 * @see http://www.netbeans.org/issues/show_bug.cgi?id=51641
 */
public class HttpSettingsTest extends NbTestCase {
    private final Object sync = getEventQueueSync ();
    private static String SYSTEM_PROXY_HOST = "system.cache.org";
    private static String SYSTEM_PROXY_PORT = "777";
    private static String USER_PROXY_HOST = "my.webcache";
    private static String USER_PROXY_PORT = "8080";

    private Preferences proxyPreferences;
    private static String SILLY_USER_PROXY_HOST = "http://my.webcache";
    private static String SILLY_SYSTEM_PROXY_HOST = "http://system.cache.org";
    private static String MY_NON_PROXY_HOSTS = "myhost.mydomain.net";
    
    private static String NETBEANS_ORG = "*.netbeans.org";
    private static String OTHER_ORG = "*.other.org";
    
    public HttpSettingsTest (String name) {
        super (name);
    }
    
    public static void main(String[] args) {
        TestRunner.run (new NbTestSuite (HttpSettingsTest.class));
    }
    
    public void run (final TestResult result) {
        //just initialize Preferences before code NbTestCase
        Preferences.userRoot ();                        
        super.run (result);
    }
    
    protected int timeOut () {
        return 20 * 1000;
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        System.setProperty ("http.nonProxyHosts", NETBEANS_ORG + ',' + NETBEANS_ORG);
        ProxySelector.setDefault (new NbProxySelector ());
        proxyPreferences  = NbPreferences.root ().node ("/org/netbeans/core");
        proxyPreferences.addPreferenceChangeListener (new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent arg0) {
                synchronized (sync) {
                    sync.notifyAll ();
                }
            }
        });
        System.setProperty ("netbeans.system_http_proxy", SYSTEM_PROXY_HOST + ":" + SYSTEM_PROXY_PORT);
        System.setProperty ("netbeans.system_http_non_proxy_hosts", OTHER_ORG);
        synchronized (sync) {
            if (! USER_PROXY_HOST.equals (proxyPreferences.get ("proxyHttpHost", ""))) {
                proxyPreferences.put ("proxyHttpHost", USER_PROXY_HOST);
                sync.wait (10000);
            }
        }
        synchronized (sync) {
            if (! USER_PROXY_PORT.equals (proxyPreferences.get ("proxyHttpPort", ""))) {
                proxyPreferences.put ("proxyHttpPort", USER_PROXY_PORT);
                sync.wait (10000);
            }
        }
    }
    
    private void sillySetUp () throws Exception {
        synchronized (sync) {
            if (ProxySettings.DIRECT_CONNECTION != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.DIRECT_CONNECTION);
                sync.wait ();
            }
        }
        
        System.setProperty ("netbeans.system_http_proxy", SILLY_SYSTEM_PROXY_HOST + ":" + SYSTEM_PROXY_PORT);
        synchronized (sync) {
            if (! SILLY_USER_PROXY_HOST.equals (proxyPreferences.get ("proxyHttpHost", ""))) {
                proxyPreferences.put ("proxyHttpHost", SILLY_USER_PROXY_HOST);
                sync.wait (10000);
            }
        }
        proxyPreferences.put ("proxyHttpPort", USER_PROXY_PORT);
        synchronized (sync) {
            if (! USER_PROXY_PORT.equals (proxyPreferences.get ("proxyHttpPort", ""))) {
                proxyPreferences.put ("proxyHttpPort", USER_PROXY_PORT);
                sync.wait (10000);
            }
        }
    }
    
    public void testDirectConnection () throws InterruptedException {
        System.setProperty ("http.proxyHost", "");
        System.setProperty ("http.proxyPort", "");
        synchronized (sync) {
            if (ProxySettings.DIRECT_CONNECTION != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.DIRECT_CONNECTION);
                sync.wait ();
            }
        }
        assertEquals ("Proxy type DIRECT_CONNECTION.", ProxySettings.DIRECT_CONNECTION, ProxySettings.getProxyType ());
        assertEquals ("No Proxy Host set.", null, System.getProperty ("http.proxyHost"));
        assertEquals ("No Proxy Port set.", null, System.getProperty ("http.proxyPort"));
    }
    
    public void testAutoDetectProxy () throws InterruptedException {
        System.setProperty ("http.proxyHost", "");
        System.setProperty ("http.proxyPort", "");
        synchronized (sync) {
            if (ProxySettings.AUTO_DETECT_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.AUTO_DETECT_PROXY);
                sync.wait ();
            }
        }
        assertEquals("Proxy type AUTO_DETECT_PROXY.",
                     ProxySettings.AUTO_DETECT_PROXY,
                     ProxySettings.getProxyType());
        assertEquals("System Proxy Host: ", SYSTEM_PROXY_HOST,
                     System.getProperty("http.proxyHost"));
        assertEquals("System Proxy Port: ", SYSTEM_PROXY_PORT,
                     System.getProperty("http.proxyPort"));
    }
    
    public void testManualSetProxy () throws InterruptedException {
        synchronized (sync) {
            if (ProxySettings.MANUAL_SET_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.MANUAL_SET_PROXY);
                sync.wait ();
            }
        }
        assertEquals ("Proxy type MANUAL_SET_PROXY.", ProxySettings.MANUAL_SET_PROXY, ProxySettings.getProxyType ());
        assertEquals ("Manual Set Proxy Host from ProxySettings: ", USER_PROXY_HOST, ProxySettings.getHttpHost ());
        assertEquals ("Manual Set Proxy Port from ProxySettings: ", USER_PROXY_PORT, ProxySettings.getHttpPort ());
        assertEquals ("Manual Set Proxy Host from System.getProperty(): ", USER_PROXY_HOST, System.getProperty ("http.proxyHost"));
        assertEquals ("Manual Set Proxy Port from System.getProperty(): ", USER_PROXY_PORT, System.getProperty ("http.proxyPort"));
    }
    
    public void testIfTakeUpNonProxyFromProperty () {
        assertTrue (NETBEANS_ORG + " in one of Non-Proxy hosts.", ProxySettings.getNonProxyHosts ().indexOf (NETBEANS_ORG) != -1);
    }
    
    public void testNonProxy () throws InterruptedException {
        assertEquals ("The ProxySettings takes as same value as System properties in initial.", System.getProperty ("http.nonProxyHosts"), ProxySettings.getNonProxyHosts ());
        
        // change value in ProxySettings
        synchronized (sync) {
            if (ProxySettings.MANUAL_SET_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.MANUAL_SET_PROXY);
                sync.wait ();
            }
        }
        synchronized (sync) {
            if (! MY_NON_PROXY_HOSTS.equals (proxyPreferences.get ("proxyNonProxyHosts", ""))) {
                proxyPreferences.put ("proxyNonProxyHosts", MY_NON_PROXY_HOSTS);
                sync.wait ();
            }
        }
        assertEquals ("ProxySettings returns new value.", "myhost.mydomain.net", ProxySettings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", ProxySettings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
        
        // switch proxy type to DIRECT_CONNECTION
        //System.setProperty ("http.nonProxyHosts", "");
        synchronized (sync) {
            if (ProxySettings.DIRECT_CONNECTION != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.DIRECT_CONNECTION);
                sync.wait ();
            }
        }
        assertEquals ("System.getProperty() doesn't return new value if DIRECT_CONNECTION set.", null, System.getProperty ("http.nonProxyHosts"));
        
        // switch proxy type back to MANUAL_SET_PROXY
        synchronized (sync) {
            if (ProxySettings.MANUAL_SET_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.MANUAL_SET_PROXY);
                sync.wait ();
            }
        }
        assertEquals ("ProxySettings again returns new value.", "myhost.mydomain.net", ProxySettings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", ProxySettings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
        
        // switch proxy type to AUTO_DETECT_PROXY
        synchronized (sync) {
            if (ProxySettings.AUTO_DETECT_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.AUTO_DETECT_PROXY);
                sync.wait ();
            }
        }
        log ("AUTO_DETECT_PROXY: ProxySettings.SystemProxySettings.getNonProxyHosts (): " + ProxySettings.SystemProxySettings.getNonProxyHosts ());
        log ("AUTO_DETECT_PROXY: System.getProperty (\"http.nonProxyHosts\"): " + System.getProperty ("http.nonProxyHosts"));
        assertTrue ("ProxySettings contains OTHER_ORG if AUTO_DETECT_PROXY set.", System.getProperty ("http.nonProxyHosts").indexOf (OTHER_ORG) > 0);
                
        // switch proxy type back to MANUAL_SET_PROXY
        synchronized (sync) {
            if (ProxySettings.MANUAL_SET_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.MANUAL_SET_PROXY);
                sync.wait ();
            }
        }
        assertEquals ("ProxySettings again returns new value.", "myhost.mydomain.net", ProxySettings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", ProxySettings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
    }
    
    public void testAvoidDuplicateNonProxySetting () throws InterruptedException {
        synchronized (sync) {
            if (ProxySettings.AUTO_DETECT_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.AUTO_DETECT_PROXY);
                sync.wait ();
            }
        }
        assertTrue (NETBEANS_ORG + " is among Non-proxy hosts detect from OS.", ProxySettings.SystemProxySettings.getNonProxyHosts ().indexOf (NETBEANS_ORG) != -1);
        assertFalse (NETBEANS_ORG + " is in Non-Proxy hosts only once.", ProxySettings.SystemProxySettings.getNonProxyHosts ().indexOf (NETBEANS_ORG) < ProxySettings.getNonProxyHosts ().lastIndexOf (NETBEANS_ORG));
        assertEquals ("System property http.nonProxyHosts was changed as well.", ProxySettings.SystemProxySettings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
    }
    
    public void testReadNonProxySettingFromSystem () throws InterruptedException {
        synchronized (sync) {
            if (ProxySettings.AUTO_DETECT_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.AUTO_DETECT_PROXY);
                sync.wait ();
            }
        }
        assertTrue (OTHER_ORG + " is among Non-proxy hosts detect from OS.", ProxySettings.SystemProxySettings.getNonProxyHosts ().indexOf (OTHER_ORG) != -1);
        assertEquals ("System property http.nonProxyHosts was changed as well.", ProxySettings.SystemProxySettings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
    }
    
    public void testSillySetManualProxy () throws Exception {
        sillySetUp ();
        synchronized (sync) {
            if (ProxySettings.MANUAL_SET_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.MANUAL_SET_PROXY);
                sync.wait ();
            }
        }
        assertEquals ("Proxy type MANUAL_SET_PROXY.", ProxySettings.MANUAL_SET_PROXY, ProxySettings.getProxyType ());
        assertEquals ("Manual Set Proxy Host from ProxySettings: ", SILLY_USER_PROXY_HOST, ProxySettings.getHttpHost ());
        assertEquals ("Manual Set Proxy Port from ProxySettings: ", USER_PROXY_PORT, ProxySettings.getHttpPort ());
        assertEquals ("Manual Set Proxy Host from System.getProperty(): ", USER_PROXY_HOST, System.getProperty ("http.proxyHost"));
        assertEquals ("Manual Set Proxy Port from System.getProperty(): ", USER_PROXY_PORT, System.getProperty ("http.proxyPort"));
    }
    
    public void testAutoDetectSillySetProxy () throws Exception {
        sillySetUp ();
        synchronized (sync) {
            if (ProxySettings.AUTO_DETECT_PROXY != (proxyPreferences.getInt ("proxyType", -1))) {
                proxyPreferences.putInt ("proxyType", ProxySettings.AUTO_DETECT_PROXY);
                sync.wait ();
            }
        }
        assertEquals ("Proxy type AUTO_DETECT_PROXY.", ProxySettings.AUTO_DETECT_PROXY, ProxySettings.getProxyType ());
        assertEquals ("Auto Detected Proxy Host from ProxySettings: ", SILLY_SYSTEM_PROXY_HOST, ProxySettings.SystemProxySettings.getHttpHost ());
        assertEquals ("Auto Detected Proxy Port from ProxySettings: ", SYSTEM_PROXY_PORT, ProxySettings.SystemProxySettings.getHttpPort ());
        assertEquals ("System Proxy Host: ", SYSTEM_PROXY_HOST, System.getProperty ("http.proxyHost"));
        assertEquals ("System Proxy Port: ", SYSTEM_PROXY_PORT, System.getProperty ("http.proxyPort"));
    }    
    
    private Object getEventQueueSync() {
        try {
            Field f = AbstractPreferences.class.getDeclaredField("eventQueue");
            f.setAccessible(true);
            return f.get(null);
            
        } catch (Exception ex) {
            Logger.getLogger("global").log(java.util.logging.Level.SEVERE,ex.getMessage(), ex);
        }
        return null;
    }
}

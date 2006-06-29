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

package org.netbeans.core;

import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.util.io.NbMarshalledObject;

/** Tests HTTP Proxy settings. 
 *
 * @author Jiri Rechtacek
 * @see http://www.netbeans.org/issues/show_bug.cgi?id=51641
 */
public class HttpSettingsTest extends NbTestCase {
    private IDESettings settings;
    private static String SYSTEM_PROXY_HOST = "system.cache.org";
    private static String SYSTEM_PROXY_PORT = "777";
    private static String USER_PROXY_HOST = "my.webcache";
    private static String USER_PROXY_PORT = "8080";

    private static String SILLY_USER_PROXY_HOST = "http://my.webcache";
    private static String SILLY_SYSTEM_PROXY_HOST = "http://system.cache.org";
    
    public HttpSettingsTest (String name) {
        super (name);
    }
    
    public static void main(String[] args) {
        TestRunner.run (new NbTestSuite (HttpSettingsTest.class));
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        System.setProperty ("netbeans.system_http_proxy", SYSTEM_PROXY_HOST + ":" + SYSTEM_PROXY_PORT);
        System.setProperty ("netbeans.system_http_non_proxy_hosts", "*.other.org");
        System.setProperty ("http.nonProxyHosts", "*.netbeans.org,*.netbeans.org");
        settings = (IDESettings)IDESettings.findObject(IDESettings.class, true);
        settings.initialize ();
        settings.setUserProxyHost (USER_PROXY_HOST);
        settings.setUserProxyPort (USER_PROXY_PORT);
    }
    
    private void sillySetUp () throws Exception {
        System.setProperty ("netbeans.system_http_proxy", SILLY_SYSTEM_PROXY_HOST + ":" + SYSTEM_PROXY_PORT);
        settings = IDESettings.findObject(IDESettings.class, true);
        settings.setUserProxyHost (SILLY_USER_PROXY_HOST);
        settings.setUserProxyPort (USER_PROXY_PORT);
    }
    
    public void testDirectConnection () {
        settings.setProxyType (IDESettings.DIRECT_CONNECTION);
        assertEquals ("Proxy type DIRECT_CONNECTION.", IDESettings.DIRECT_CONNECTION, settings.getProxyType ());
        assertEquals ("No Proxy Host set.", "", System.getProperty (IDESettings.KEY_PROXY_HOST));
        assertEquals ("No Proxy Port set.", "", System.getProperty (IDESettings.KEY_PROXY_PORT));
    }
    
    public void testAutoDetectProxy () {
        settings.setProxyType (IDESettings.AUTO_DETECT_PROXY);
        assertEquals ("Proxy type AUTO_DETECT_PROXY.", IDESettings.AUTO_DETECT_PROXY, settings.getProxyType ());
        assertEquals ("System Proxy Host: ", SYSTEM_PROXY_HOST, System.getProperty (IDESettings.KEY_PROXY_HOST));
        assertEquals ("System Proxy Port: ", SYSTEM_PROXY_PORT, System.getProperty (IDESettings.KEY_PROXY_PORT));
    }
    
    public void testManualSetProxy () {
        settings.setProxyType (IDESettings.MANUAL_SET_PROXY);
        assertEquals ("Proxy type MANUAL_SET_PROXY.", IDESettings.MANUAL_SET_PROXY, settings.getProxyType ());
        assertEquals ("Manual Set Proxy Host from IDESettings: ", USER_PROXY_HOST, settings.getProxyHost ());
        assertEquals ("Manual Set Proxy Port from IDESettings: ", USER_PROXY_PORT, settings.getProxyPort ());
        assertEquals ("Manual Set Proxy Host from System.getProperty(): ", USER_PROXY_HOST, System.getProperty (IDESettings.KEY_PROXY_HOST));
        assertEquals ("Manual Set Proxy Port from System.getProperty(): ", USER_PROXY_PORT, System.getProperty (IDESettings.KEY_PROXY_PORT));
    }
    
    public void testHttpSettingsSerialization () throws Exception {
        assertEquals ("Original user proxy host", USER_PROXY_HOST, settings.getProxyHost ());
        assertEquals ("Original user proxy port", USER_PROXY_PORT, settings.getProxyPort ());
        IDESettings deserializedSettings = (IDESettings) new NbMarshalledObject (settings).get ();
        assertEquals ("Original user proxy host returned from deserialized IDESettings", USER_PROXY_HOST, deserializedSettings.getProxyHost ());
        assertEquals ("Original user proxy port returned from deserialized IDESettings", USER_PROXY_PORT, deserializedSettings.getProxyPort ());
        deserializedSettings.setUserProxyHost ("new.cache");
        deserializedSettings.setUserProxyPort ("80");
        deserializedSettings.setUserNonProxyHosts ("*.mydomain.org");
        IDESettings againDeserializedSettings = (IDESettings) new NbMarshalledObject (deserializedSettings).get ();
        assertEquals ("New user proxy host returned from deserialized IDESettings after change", "new.cache", againDeserializedSettings.getProxyHost ());
        assertEquals ("New user proxy port returned from deserialized IDESettings after change", "80", againDeserializedSettings.getProxyPort ());
        assertEquals ("New user non proxy hosts returned from deserialized IDESettings after change", "*.mydomain.org", againDeserializedSettings.getNonProxyHosts ());
    }
    
    public void testIfTakeUpNonProxyFromProperty () {
        assertTrue ("*.netbeans.org in one of Non-Proxy hosts.", settings.getNonProxyHosts ().indexOf ("*.netbeans.org") != -1);
    }
    
    public void testNonProxy () {
        assertEquals ("The IDESettings takes as same value as System properties in initial.", System.getProperty ("http.nonProxyHosts"), settings.getNonProxyHosts ());
        
        // change value in IDESettings
        settings.setProxyType (IDESettings.MANUAL_SET_PROXY);
        settings.setUserNonProxyHosts ("myhost.mydomain.net");
        assertEquals ("IDESettings returns new value.", "myhost.mydomain.net", settings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", settings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
        
        // switch proxy type to DIRECT_CONNECTION
        settings.setProxyType (IDESettings.DIRECT_CONNECTION);
        assertFalse ("IDESettings doesn't return new value if DIRECT_CONNECTION set.", "myhost.mydomain.net".equals (settings.getNonProxyHosts ()));
        assertEquals ("System property http.nonProxyHosts was changed as well.", settings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
        
        // switch proxy type back to MANUAL_SET_PROXY
        settings.setProxyType (IDESettings.MANUAL_SET_PROXY);
        assertEquals ("IDESettings again returns new value.", "myhost.mydomain.net", settings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", settings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
        
        // switch proxy type to AUTO_DETECT_PROXY
        settings.setProxyType (IDESettings.AUTO_DETECT_PROXY);
        assertFalse ("IDESettings doesn't return new value if AUTO_DETECT_PROXY set.", "myhost.mydomain.net".equals (settings.getNonProxyHosts ()));
        assertEquals ("System property http.nonProxyHosts was changed as well.", settings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
                
        // switch proxy type back to MANUAL_SET_PROXY
        settings.setProxyType (IDESettings.MANUAL_SET_PROXY);
        assertEquals ("IDESettings again returns new value.", "myhost.mydomain.net", settings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", settings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
    }
    
    public void testAvoidDuplicateNonProxySetting () {
        settings.setProxyType (IDESettings.MANUAL_SET_PROXY);
        assertEquals ("IDESettings returns new value.", "*.netbeans.org|localhost|127.0.0.1|rechtacek-nb|localhost.localdomain", settings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", settings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
    }
    
    public void testReadNonProxySettingFromSystem () {
        settings.setProxyType (IDESettings.AUTO_DETECT_PROXY);
        assertEquals ("IDESettings returns new value.", "*.netbeans.org|*.other.org|localhost|127.0.0.1|rechtacek-nb|localhost.localdomain", settings.getNonProxyHosts ());
        assertEquals ("System property http.nonProxyHosts was changed as well.", settings.getNonProxyHosts (), System.getProperty ("http.nonProxyHosts"));
    }
    
    public void testSillySetManualProxy () throws Exception {
        sillySetUp ();
        settings.setProxyType (IDESettings.MANUAL_SET_PROXY);
        assertEquals ("Proxy type MANUAL_SET_PROXY.", IDESettings.MANUAL_SET_PROXY, settings.getProxyType ());
        assertEquals ("Manual Set Proxy Host from IDESettings: ", SILLY_USER_PROXY_HOST, settings.getProxyHost ());
        assertEquals ("Manual Set Proxy Port from IDESettings: ", USER_PROXY_PORT, settings.getProxyPort ());
        assertEquals ("Manual Set Proxy Host from System.getProperty(): ", USER_PROXY_HOST, System.getProperty (IDESettings.KEY_PROXY_HOST));
        assertEquals ("Manual Set Proxy Port from System.getProperty(): ", USER_PROXY_PORT, System.getProperty (IDESettings.KEY_PROXY_PORT));
    }
    
    public void testAutoDetectSillySetProxy () throws Exception {
        sillySetUp ();
        settings.setProxyType (IDESettings.AUTO_DETECT_PROXY);
        assertEquals ("Proxy type AUTO_DETECT_PROXY.", IDESettings.AUTO_DETECT_PROXY, settings.getProxyType ());
        assertEquals ("System Proxy Host: ", SYSTEM_PROXY_HOST, System.getProperty (IDESettings.KEY_PROXY_HOST));
        assertEquals ("System Proxy Port: ", SYSTEM_PROXY_PORT, System.getProperty (IDESettings.KEY_PROXY_PORT));
    }    
}

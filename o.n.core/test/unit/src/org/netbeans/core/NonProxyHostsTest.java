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

import java.net.ProxySelector;
import java.net.URI;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.util.NbPreferences;

/** Tests Detect OS nonProxyHosts settings.
 *
 * @author Jiri Rechtacek
 * @see http://www.netbeans.org/issues/show_bug.cgi?id=77053
 */
public class NonProxyHostsTest extends NbTestCase {
    private static String SYSTEM_PROXY_HOST = "system.cache.org";
    private static String SYSTEM_PROXY_PORT = "777";
    private static String USER_PROXY_HOST = "my.webcache";
    private static String USER_PROXY_PORT = "8080";

    private Preferences proxyPreferences;
    private ProxySelector selector = ProxySelector.getDefault ();
    private static URI TO_LOCALHOST;
    private static URI TO_LOCAL_DOMAIN_1;
    private static URI TO_LOCAL_DOMAIN_2;
    private static URI TO_EXTERNAL;

    private boolean isWaiting = false;
    
    public NonProxyHostsTest (String name) {
        super (name);
    }
    
    public static void main(String[] args) {
        TestRunner.run (new NbTestSuite (NonProxyHostsTest.class));
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        System.setProperty ("netbeans.system_http_proxy", SYSTEM_PROXY_HOST + ":" + SYSTEM_PROXY_PORT);
        System.setProperty ("netbeans.system_http_non_proxy_hosts", "*.other.org");
        System.setProperty ("http.nonProxyHosts", "*.netbeans.org");
        ProxySelector.setDefault (new NbProxySelector ());
        proxyPreferences  = NbPreferences.root ().node ("/org/netbeans/core");;
        proxyPreferences.addPreferenceChangeListener (new PreferenceChangeListener () {
            public void preferenceChange (PreferenceChangeEvent arg0) {
                isWaiting = false;
            }
        });
        proxyPreferences.put ("proxyHttpHost", USER_PROXY_HOST);
        proxyPreferences.put ("proxyHttpPort", USER_PROXY_PORT);
        while (isWaiting);
        isWaiting = true;
        TO_LOCALHOST = new URI ("http://localhost");
        TO_LOCAL_DOMAIN_1 = new URI ("http://core.netbeans.org");
        TO_LOCAL_DOMAIN_2 = new URI ("http://core.other.org");
        TO_EXTERNAL = new URI ("http://worldwide.net");
    }
    
    public void testDirectProxySetting () {
        proxyPreferences.putInt ("proxyType", ProxySettings.DIRECT_CONNECTION);
        while (isWaiting);
        assertEquals ("Proxy type DIRECT_CONNECTION.", ProxySettings.DIRECT_CONNECTION, ProxySettings.getProxyType ());
        assertEquals ("Connect " + TO_LOCALHOST + " DIRECT.", "[DIRECT]", selector.select (TO_LOCALHOST).toString ());
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_1 + " DIRECT.", "[DIRECT]", selector.select (TO_LOCAL_DOMAIN_1).toString ());
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_2 + " DIRECT.", "[DIRECT]", selector.select (TO_LOCAL_DOMAIN_2).toString ());
        assertEquals ("Connect " + TO_EXTERNAL + " DIRECT.", "[DIRECT]", selector.select (TO_EXTERNAL).toString ());
    }
    
    public void testManualProxySettins () {
        proxyPreferences.put (ProxySettings.NOT_PROXY_HOSTS, "*.netbeans.org");
        proxyPreferences.putInt ("proxyType", ProxySettings.MANUAL_SET_PROXY);
        while (isWaiting);
        assertEquals ("Proxy type DIRECT_CONNECTION.", ProxySettings.MANUAL_SET_PROXY, ProxySettings.getProxyType ());
        assertEquals ("Connect TO_LOCALHOST DIRECT.", "[DIRECT]", selector.select (TO_LOCALHOST).toString ());
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_1 + " DIRECT.", "[DIRECT]", selector.select (TO_LOCAL_DOMAIN_1).toString ());
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_2 + " via my.webcache:8080 proxy.", "[HTTP @ my.webcache:8080]", selector.select (TO_LOCAL_DOMAIN_2).toString ());
        assertEquals ("Connect TO_EXTERNAL via my.webcache:8080 proxy.", "[HTTP @ my.webcache:8080]", selector.select (TO_EXTERNAL).toString ());
    }
    
    public void testSystemProxySettings () {
        proxyPreferences.putInt ("proxyType", ProxySettings.AUTO_DETECT_PROXY);
        while (isWaiting);
        log ("Value of System.getProperty (\"http.nonProxyHosts\"): " + System.getProperty ("http.nonProxyHosts"));
        assertTrue ("*.other.org is one of non-proxy hosts", System.getProperty ("http.nonProxyHosts").indexOf ("*.other.org") != -1);
        assertEquals ("Proxy type DIRECT_CONNECTION.", ProxySettings.AUTO_DETECT_PROXY, ProxySettings.getProxyType ());
        assertEquals ("Connect TO_LOCALHOST DIRECT.", "[DIRECT]", selector.select (TO_LOCALHOST).toString ());
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_1 + " DIRECT.", "[DIRECT]", selector.select (TO_LOCAL_DOMAIN_1).toString ());
        assertEquals ("Connect " + TO_LOCAL_DOMAIN_2 + " DIRECT ignoring settings " + System.getProperty ("http.nonProxyHosts"), "[DIRECT]", selector.select (TO_LOCAL_DOMAIN_2).toString ());
        assertEquals ("Connect TO_EXTERNAL via system.cache.org:777 proxy..", "[HTTP @ system.cache.org:777]", selector.select (TO_EXTERNAL).toString ());
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.NetworkSettings;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class ProxySettings {
    
    public static final String PROXY_HTTP_HOST = "proxyHttpHost";
    public static final String PROXY_HTTP_PORT = "proxyHttpPort";
    public static final String PROXY_HTTPS_HOST = "proxyHttpsHost";
    public static final String PROXY_HTTPS_PORT = "proxyHttpsPort";
    public static final String PROXY_SOCKS_HOST = "proxySocksHost";
    public static final String PROXY_SOCKS_PORT = "proxySocksPort";
    public static final String NOT_PROXY_HOSTS = "proxyNonProxyHosts";
    public static final String PROXY_TYPE = "proxyType";
    public static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication";
    public static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername";
    public static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword";
    public static final String USE_PROXY_ALL_PROTOCOLS = "useProxyAllProtocols";
    public static final String DIRECT = "DIRECT";
    public static final String PAC = "PAC";
    
    private static String presetNonProxyHosts;

    /** No proxy is used to connect. */
    public static final int DIRECT_CONNECTION = 0;
    
    /** Proxy setting is automatically detect in OS. */
    public static final int AUTO_DETECT_PROXY = 1; // as default
    
    /** Manually set proxy host and port. */
    public static final int MANUAL_SET_PROXY = 2;
    
    /** Proxy PAC file automatically detect in OS. */
    public static final int AUTO_DETECT_PAC = 3;
    
    /** Proxy PAC file manually set. */
    public static final int MANUAL_SET_PAC = 4;
    
    private static final Logger LOGGER = Logger.getLogger(ProxySettings.class.getName());
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule (ProxySettings.class);
    }
    
    public static String getHttpHost () {
        return normalizeProxyHost (getPreferences ().get (PROXY_HTTP_HOST, ""));
    }
    
    public static String getHttpPort () {
        return getPreferences ().get (PROXY_HTTP_PORT, "");
    }
    
    public static String getHttpsHost () {
        if (useProxyAllProtocols ()) {
            return getHttpHost ();
        } else {
            return getPreferences ().get (PROXY_HTTPS_HOST, "");
        }
    }
    
    public static String getHttpsPort () {
        if (useProxyAllProtocols ()) {
            return getHttpPort ();
        } else {
            return getPreferences ().get (PROXY_HTTPS_PORT, "");
        }
    }
    
    public static String getSocksHost () {
        if (useProxyAllProtocols ()) {
            return getHttpHost ();
        } else {
            return getPreferences ().get (PROXY_SOCKS_HOST, "");
        }
    }
    
    public static String getSocksPort () {
        if (useProxyAllProtocols ()) {
            return getHttpPort ();
        } else {
            return getPreferences ().get (PROXY_SOCKS_PORT, "");
        }
    }
    
    public static String getNonProxyHosts () {
        String hosts = getPreferences ().get (NOT_PROXY_HOSTS, getDefaultUserNonProxyHosts ());
        return compactNonProxyHosts(hosts);
    }
    
    public static int getProxyType () {
        int type = getPreferences ().getInt (PROXY_TYPE, AUTO_DETECT_PROXY);
        if (AUTO_DETECT_PROXY == type) {
            type = NbProxySelector.usePAC() ? AUTO_DETECT_PAC : AUTO_DETECT_PROXY;
        }
        return type;
    }
    
    public static boolean useAuthentication () {
        return getPreferences ().getBoolean (USE_PROXY_AUTHENTICATION, false);
    }
    
    public static boolean useProxyAllProtocols () {
        return getPreferences ().getBoolean (USE_PROXY_ALL_PROTOCOLS, false);
    }
    
    public static String getAuthenticationUsername () {
        return getPreferences ().get (PROXY_AUTHENTICATION_USERNAME, "");
    }
    
    public static char[] getAuthenticationPassword () {
        String old = getPreferences().get(PROXY_AUTHENTICATION_PASSWORD, null);
        if (old != null) {
            getPreferences().remove(PROXY_AUTHENTICATION_PASSWORD);
            setAuthenticationPassword(old.toCharArray());
        }
        char[] pwd = Keyring.read(PROXY_AUTHENTICATION_PASSWORD);
        return pwd != null ? pwd : new char[0];
    }
    
    public static void setAuthenticationPassword(char[] password) {
        Keyring.save(ProxySettings.PROXY_AUTHENTICATION_PASSWORD, password,
                // XXX consider including getHttpHost and/or getHttpsHost
                NbBundle.getMessage(ProxySettings.class, "ProxySettings.password.description"));
    }

    static void addPreferenceChangeListener (PreferenceChangeListener l) {
        getPreferences ().addPreferenceChangeListener (l);
    }
    
    static void removePreferenceChangeListener (PreferenceChangeListener l) {
        getPreferences ().removePreferenceChangeListener (l);
    }

    static class SystemProxySettings extends ProxySettings {
        
        public static String getHttpHost () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyHost ();
            } else {
                return "";
            }
        }

        public static String getHttpPort () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyPort ();
            } else {
                return "";
            }
        }

        public static String getHttpsHost () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyHost ();
            } else {
                return "";
            }
        }

        public static String getHttpsPort () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyPort ();
            } else {
                return "";
            }
        }

        public static String getSocksHost () {
            if (isSystemSocksServerDetect ()) {
                return getSystemSocksServerHost ();
            } else {
                return "";
            }
        }

        public static String getSocksPort () {
            if (isSystemSocksServerDetect ()) {
                return getSystemSocksServerPort ();
            } else {
                return "";
            }
        }

        public static String getNonProxyHosts () {
            return getDefaultUserNonProxyHosts ();
        }

        // helper methods
        private static boolean isSystemProxyDetect () {
            if (NbProxySelector.useSystemProxies ()) {
                return true;
            }
            String s = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
            return s != null && ! DIRECT.equals (s); // NOI18N
        }

        private static String getSystemProxyHost () {
            String systemProxy = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
            if (systemProxy == null) {
                return ""; // NOI18N
            }

            int i = systemProxy.lastIndexOf (":"); // NOI18N
            if (i <= 0 || i >= systemProxy.length () - 1) {
                return ""; // NOI18N
            }

            return normalizeProxyHost (systemProxy.substring (0, i));
        }

        private static String getSystemProxyPort () {
            String systemProxy = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
            if (systemProxy == null) {
                return ""; // NOI18N
             }

            int i = systemProxy.lastIndexOf (":"); // NOI18N
            if (i <= 0 || i >= systemProxy.length () - 1) {
                return ""; // NOI18N
            }
            
            String p = systemProxy.substring (i + 1);
            if (p.indexOf ('/') >= 0) {
                p = p.substring (0, p.indexOf ('/'));
            }

            return p;
        }

        private static boolean isSystemSocksServerDetect () {
            return isSystemProxyDetect () && System.getProperty ("netbeans.system_socks_proxy") != null; // NOI18N
        }
        
        private static String getSystemSocksServerHost () {
            String systemProxy = System.getProperty ("netbeans.system_socks_proxy"); // NOI18N
            if (systemProxy == null) {
                return ""; // NOI18N
            }

            int i = systemProxy.lastIndexOf (":"); // NOI18N
            if (i <= 0 || i >= systemProxy.length () - 1) {
                return ""; // NOI18N
            }

            return normalizeProxyHost (systemProxy.substring (0, i));
        }

        private static String getSystemSocksServerPort () {
            String systemProxy = System.getProperty ("netbeans.system_socks_proxy"); // NOI18N
            if (systemProxy == null) {
                return ""; // NOI18N
             }

            int i = systemProxy.lastIndexOf (":"); // NOI18N
            if (i <= 0 || i >= systemProxy.length () - 1) {
                return ""; // NOI18N
            }
            
            String p = systemProxy.substring (i + 1);
            if (p.indexOf ('/') >= 0) {
                p = p.substring (0, p.indexOf ('/'));
            }

            return p;
        }

    }

    private static String getSystemNonProxyHosts () {
        String systemProxy = System.getProperty ("netbeans.system_http_non_proxy_hosts"); // NOI18N

        return systemProxy == null ? "" : systemProxy;
    }
    
    private static String getPresetNonProxyHosts () {
        if (presetNonProxyHosts == null) {
            presetNonProxyHosts = System.getProperty ("http.nonProxyHosts", "");
        }
        return presetNonProxyHosts;
    }
    
    private static String getDefaultUserNonProxyHosts () {
        return getModifiedNonProxyHosts (getSystemNonProxyHosts ());
    }

    private static String getModifiedNonProxyHosts (String systemPreset) {
        String fromSystem = systemPreset.replaceAll (";", "|").replaceAll (",", "|"); //NOI18N
        String fromUser = getPresetNonProxyHosts () == null ? "" : getPresetNonProxyHosts ().replaceAll (";", "|").replaceAll (",", "|"); //NOI18N
        if (Utilities.isWindows ()) {
            fromSystem = addReguralToNonProxyHosts (fromSystem);
        }
        String nonProxy = fromUser + (fromUser.length () == 0 ? "" : "|") + fromSystem + (fromSystem.length () == 0 ? "" : "|") + "localhost|127.0.0.1"; // NOI18N
        String localhost = ""; // NOI18N
        try {
            localhost = InetAddress.getLocalHost().getHostName();
            if (!"localhost".equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        }
        catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        /* per Milan's agreement it's removed. See issue #89868
        try {
            String localhost2 = InetAddress.getLocalHost().getCanonicalHostName();
            if (!"localhost".equals(localhost2) && !localhost2.equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost2; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        }
        catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
         */
        return compactNonProxyHosts (nonProxy);
    }


    // avoid duplicate hosts
    private static String compactNonProxyHosts (String hosts) {
        StringTokenizer st = new StringTokenizer(hosts, ","); //NOI18N
        StringBuilder nonProxyHosts = new StringBuilder();
        while (st.hasMoreTokens()) {
            String h = st.nextToken().trim();
            if (h.length() == 0) {
                continue;
            }
            if (nonProxyHosts.length() > 0) {
                nonProxyHosts.append("|"); // NOI18N
            }
            nonProxyHosts.append(h);
        }
        st = new StringTokenizer (nonProxyHosts.toString(), "|"); //NOI18N
        Set<String> set = new HashSet<String> (); 
        StringBuilder compactedProxyHosts = new StringBuilder();
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            if (set.add (t.toLowerCase (Locale.US))) {
                if (compactedProxyHosts.length() > 0) {
                    compactedProxyHosts.append('|');
                }
                compactedProxyHosts.append(t);
            }
        }
        return compactedProxyHosts.toString();
    }
    
    private static String addReguralToNonProxyHosts (String nonProxyHost) {
        StringTokenizer st = new StringTokenizer (nonProxyHost, "|");
        StringBuilder reguralProxyHosts = new StringBuilder();
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            if (t.indexOf ('*') == -1) { //NOI18N
                t = t + '*'; //NOI18N
            }
            if (reguralProxyHosts.length() > 0) 
                reguralProxyHosts.append('|');
            reguralProxyHosts.append(t);
        }

        return reguralProxyHosts.toString();
    }

    public static String normalizeProxyHost (String proxyHost) {
        if (proxyHost.toLowerCase (Locale.US).startsWith ("http://")) { // NOI18N
            return proxyHost.substring (7, proxyHost.length ());
        } else {
            return proxyHost;
        }
    }
    
    private static InetSocketAddress analyzeProxy(URI uri) {
        Parameters.notNull("uri", uri);
        List<Proxy> proxies = ProxySelector.getDefault().select(uri);
        assert proxies != null : "ProxySelector cannot return null for " + uri;
        assert !proxies.isEmpty() : "ProxySelector cannot return empty list for " + uri;
        Proxy p = proxies.get(0);
        if (Proxy.Type.DIRECT == p.type()) {
            // return null for DIRECT proxy
            return null;
        } else {
            if (p.address() instanceof InetSocketAddress) {
                // check is
                //assert ! ((InetSocketAddress) p.address()).isUnresolved() : p.address() + " must be resolved address.";
                return (InetSocketAddress) p.address();
            } else {
                LOGGER.log(Level.INFO, p.address() + " is not instanceof InetSocketAddress but " + p.address().getClass());
                return null;
            }
        }
    }

    @ServiceProvider(service = NetworkSettings.ProxyCredentialsProvider.class, position = 1000)
    public static class NbProxyCredentialsProvider extends NetworkSettings.ProxyCredentialsProvider {

        @Override
        public String getProxyHost(URI u) {
            if (getPreferences() == null) {
                return null;
            }
            InetSocketAddress sa = analyzeProxy(u);
            return sa == null ? null : sa.getHostName();
        }

        @Override
        public String getProxyPort(URI u) {
            if (getPreferences() == null) {
                return null;
            }
            InetSocketAddress sa = analyzeProxy(u);
            return sa == null ? null : Integer.toString(sa.getPort());
        }

        @Override
        protected String getProxyUserName(URI u) {
            if (getPreferences() == null) {
                return null;
            }
            return ProxySettings.getAuthenticationUsername();
        }

        @Override
        protected char[] getProxyPassword(URI u) {
            if (getPreferences() == null) {
                return null;
            }
            return ProxySettings.getAuthenticationPassword();
        }

        @Override
        protected boolean isProxyAuthentication(URI u) {
            if (getPreferences() == null) {
                return false;
            }
            return getPreferences().getBoolean(USE_PROXY_AUTHENTICATION, false);
        }

    }
}

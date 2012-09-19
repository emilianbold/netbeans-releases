/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Jiri Rechtacek
 */
public final class NbProxySelector extends ProxySelector {
    
    private final ProxySelector original;
    private static final Logger LOG = Logger.getLogger (NbProxySelector.class.getName ());
    private static Object useSystemProxies;
        
    /** Creates a new instance of NbProxySelector */
    private NbProxySelector (ProxySelector delegate) {
        original = delegate;
        LOG.fine ("java.net.useSystemProxies has been set to " + useSystemProxies ());
        LOG.fine ("In launcher was detected netbeans.system_http_proxy: " + System.getProperty ("netbeans.system_http_proxy", "N/A"));
        LOG.fine ("In launcher was detected netbeans.system_socks_proxy: " + System.getProperty ("netbeans.system_socks_proxy", "N/A"));
        ProxySettings.addPreferenceChangeListener (new ProxySettingsListener ());
        copySettingsToSystem ();
    }
    
    static ProxySelector create(ProxySelector delegate) {
        return new NbProxySelector(delegate);
    }
    
    static void register() {
        ProxySelector prev = ProxySelector.getDefault();
        if (prev == null) {
            LOG.warning("No default system ProxySelector was found thus NetBeans ProxySelector won't delegate on it");
        } else {
            LOG.log(Level.FINE, "Override the original ProxySelector: {0}", prev);
        }
        ProxySelector.setDefault(create(prev));
    }
    
    @Override
    public List<Proxy> select(URI uri) {
        List<Proxy> res = new ArrayList<Proxy> ();
        int proxyType = ProxySettings.getProxyType ();
        switch (proxyType) {
            case ProxySettings.DIRECT_CONNECTION:
                res = Collections.singletonList (Proxy.NO_PROXY);
                break;
            case ProxySettings.AUTO_DETECT_PROXY:
                if (useSystemProxies ()) {
                    if (original != null) {
                        res = original.select (uri);                   
                    }
                } else {
                    String protocol = uri.getScheme ();
                    assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
                    if (dontUseProxy (ProxySettings.SystemProxySettings.getNonProxyHosts (), uri.getHost ())) {
                        res.add (Proxy.NO_PROXY);
                    }
                    if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                        String ports = ProxySettings.SystemProxySettings.getHttpPort ();
                        if (ports != null && ports.length () > 0 && ProxySettings.SystemProxySettings.getHttpHost ().length () > 0) {
                            int porti = Integer.parseInt(ports);
                            Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (ProxySettings.SystemProxySettings.getHttpHost (), porti));
                            res.add (p);
                        }
                    } else { // supposed SOCKS
                        String ports = ProxySettings.SystemProxySettings.getSocksPort ();
                        String hosts = ProxySettings.SystemProxySettings.getSocksHost ();
                        if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                            int porti = Integer.parseInt(ports);
                            Proxy p = new Proxy (Proxy.Type.SOCKS,  new InetSocketAddress (hosts, porti));
                            res.add (p);
                        }
                    }
                    if (original != null) {
                        res.addAll (original.select (uri));
                    }
                }
                break;
            case ProxySettings.MANUAL_SET_PROXY:
                String protocol = uri.getScheme ();
                assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";

                // handling nonProxyHosts first
                if (dontUseProxy (ProxySettings.getNonProxyHosts (), uri.getHost ())) {
                    res.add (Proxy.NO_PROXY);
                }
                if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                    String hosts = ProxySettings.getHttpHost ();
                    String ports = ProxySettings.getHttpPort ();
                    if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (hosts, porti));
                        res.add (p);
                    } else {
                        LOG.info ("Incomplete HTTP Proxy [" + hosts + "/" + ports + "] found in ProxySelector[Type: " + ProxySettings.getProxyType () + "] for uri " + uri + ". ");
                        if (original != null) {
                            LOG.finest ("Fallback to the default ProxySelector which returns " + original.select (uri));
                            res.addAll (original.select (uri));
                        }
                    }
                } else { // supposed SOCKS
                    String ports = ProxySettings.getSocksPort ();
                    String hosts = ProxySettings.getSocksHost ();
                    if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.SOCKS,  new InetSocketAddress (hosts, porti));
                        res.add (p);
                    } else {
                        LOG.info ("Incomplete SOCKS Server [" + hosts + "/" + ports + "] found in ProxySelector[Type: " + ProxySettings.getProxyType () + "] for uri " + uri + ". ");
                        if (original != null) {
                            LOG.finest ("Fallback to the default ProxySelector which returns " + original.select (uri));
                            res.addAll (original.select (uri));
                        }
                    }
                }
                res.add (Proxy.NO_PROXY);
                break;
            case ProxySettings.AUTO_DETECT_PAC:
                if (useSystemProxies ()) {
                    if (original != null) {
                        res = original.select (uri);                   
                    }
                } else {
                    // handling nonProxyHosts first
                    if (dontUseProxy (ProxySettings.getNonProxyHosts (), uri.getHost ())) {
                        res.add (Proxy.NO_PROXY);
                    }
                    ProxyAutoConfig pac = ProxyAutoConfig.get(getPacFile());
                    assert pac != null : "Instance of ProxyAutoConfig found for " + getPacFile();
                    if (pac == null) {
                        LOG.finest ("No instance of ProxyAutoConfig(" + getPacFile() + ") for URI " + uri);
                        res.add(Proxy.NO_PROXY);
                    }
                    if (pac.getPacURI().getHost() == null) {
                        LOG.finest("Malformed PAC URI " + pac.getPacURI() + " for URI " + uri);
                        res.add(Proxy.NO_PROXY);
                    } else if (pac.getPacURI().getHost().equals(uri.getHost())) {
                        // don't proxy PAC files
                        res.add(Proxy.NO_PROXY);
                    } else {
                        LOG.log(Level.FINEST, "Identifying proxy for URI {0}---{1}, PAC URI: {2}---{3}", //NOI18N
                                new Object[] { uri.toString(), uri.getHost(), pac.getPacURI().toString(), pac.getPacURI().getHost() });
                        res.addAll(pac.findProxyForURL(uri)); // NOI18N
                    }
                }
                res.add (Proxy.NO_PROXY);
                break;
            case ProxySettings.MANUAL_SET_PAC:
                // handling nonProxyHosts first
                if (dontUseProxy (ProxySettings.getNonProxyHosts (), uri.getHost ())) {
                    res.add (Proxy.NO_PROXY);
                }
                ProxyAutoConfig pac = ProxyAutoConfig.get(getPacFile());
                assert pac != null : "Instance of ProxyAutoConfig found for " + getPacFile();
                if (pac == null) {
                    LOG.finest ("No instance of ProxyAutoConfig(" + getPacFile() + ") for URI " + uri);
                    res.add(Proxy.NO_PROXY);
                }
                if (pac.getPacURI().getHost() == null) {
                    LOG.finest("Malformed PAC URI " + pac.getPacURI() + " for URI " + uri);
                    res.add(Proxy.NO_PROXY);
                } else if (pac.getPacURI().getHost().equals(uri.getHost())) {
                    // don't proxy PAC files
                    res.add(Proxy.NO_PROXY);
                } else {
                    LOG.log(Level.FINEST, "Identifying proxy for URI {0}---{1}, PAC URI: {2}---{3}", //NOI18N
                            new Object[] { uri.toString(), uri.getHost(), pac.getPacURI().toString(), pac.getPacURI().getHost() });
                    res.addAll(pac.findProxyForURL(uri)); // NOI18N
                }
                res.add (Proxy.NO_PROXY);
                break;
            default:
                assert false : "Invalid proxy type: " + proxyType;
        }
        LOG.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () +
                ", Use HTTP for all protocols: " + ProxySettings.useProxyAllProtocols ()+
                "] returns " + res + " for URI " + uri);
        return res;
    }
    
    @Override
    public void connectFailed (URI arg0, SocketAddress arg1, IOException arg2) {
        LOG.log  (Level.INFO, "connectionFailed(" + arg0 + ", " + arg1 +")", arg2);
    }

    // several modules listenes on these properties and propagates it futher
    private class ProxySettingsListener implements PreferenceChangeListener {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getKey ().startsWith ("proxy") || evt.getKey ().startsWith ("useProxy")) {
                copySettingsToSystem ();
            }
        }
    }
    
    private void copySettingsToSystem () {
        String host = null, port = null, nonProxyHosts = null;
        String sHost = null, sPort = null;
        String httpsHost = null, httpsPort = null;
        int proxyType = ProxySettings.getProxyType ();
        switch (proxyType) {
            case ProxySettings.DIRECT_CONNECTION:
                host = null;
                port = null;
                httpsHost = null;
                httpsPort = null;
                nonProxyHosts = null;
                sHost = null;
                sPort = null;
                break;
            case ProxySettings.AUTO_DETECT_PROXY:
                host = ProxySettings.SystemProxySettings.getHttpHost ();
                port = ProxySettings.SystemProxySettings.getHttpPort ();
                httpsHost = ProxySettings.SystemProxySettings.getHttpsHost ();
                httpsPort = ProxySettings.SystemProxySettings.getHttpsPort ();
                sHost = ProxySettings.SystemProxySettings.getSocksHost ();
                sPort = ProxySettings.SystemProxySettings.getSocksPort ();
                ProxySettings.SystemProxySettings.getNonProxyHosts ();
                break;
            case ProxySettings.MANUAL_SET_PROXY:
                host = ProxySettings.getHttpHost ();
                port = ProxySettings.getHttpPort ();
                httpsHost = ProxySettings.getHttpsHost ();
                httpsPort = ProxySettings.getHttpsPort ();
                nonProxyHosts = ProxySettings.getNonProxyHosts ();
                sHost = ProxySettings.getSocksHost ();
                sPort = ProxySettings.getSocksPort ();
                break;
            case ProxySettings.AUTO_DETECT_PAC:
                host = null;
                port = null;
                httpsHost = null;
                httpsPort = null;
                ProxySettings.SystemProxySettings.getNonProxyHosts ();
                sHost = null;
                sPort = null;
                break;
            case ProxySettings.MANUAL_SET_PAC:
                host = null;
                port = null;
                httpsHost = null;
                httpsPort = null;
                ProxySettings.getNonProxyHosts ();
                sHost = null;
                sPort = null;
                break;
            default:
                assert false : "Invalid proxy type: " + proxyType;
        }
        setOrClearProperty ("http.proxyHost", host, false);
        setOrClearProperty ("http.proxyPort", port, true);
        setOrClearProperty ("http.nonProxyHosts", nonProxyHosts, false);
        setOrClearProperty ("https.proxyHost", httpsHost, false);
        setOrClearProperty ("https.proxyPort", httpsPort, true);
        setOrClearProperty ("https.nonProxyHosts", nonProxyHosts, false);
        setOrClearProperty ("socksProxyHost", sHost, false);
        setOrClearProperty ("socksProxyPort", sPort, true);
        LOG.fine ("Set System's http.proxyHost/Port/NonProxyHost to " + host + "/" + port + "/" + nonProxyHosts);
        LOG.fine ("Set System's https.proxyHost/Port to " + httpsHost + "/" + httpsPort);
        LOG.fine ("Set System's socksProxyHost/Port to " + sHost + "/" + sPort);
    }
    
    private void setOrClearProperty (String key, String value, boolean isInteger) {
        assert key != null;
        if (value == null || value.length () == 0) {
            System.clearProperty (key);
        } else {
            if (isInteger) {
                try {
                    Integer.parseInt (value);
                } catch (NumberFormatException nfe) {
                    LOG.log (Level.INFO, nfe.getMessage(), nfe);
                }
            }
            System.setProperty (key, value);
        }
    }

    // package-private for unit-testing
    static boolean dontUseProxy (String nonProxyHosts, String host) {
        if (host == null) return false;
        
        // try IP adress first
        if (dontUseIp (nonProxyHosts, host)) {
            return true;
        } else {
            return dontUseHostName (nonProxyHosts, host);
        }

    }
    
    private static boolean dontUseHostName (String nonProxyHosts, String host) {
        if (host == null) return false;
        
        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, "|", false);
        while (st.hasMoreTokens () && !dontUseProxy) {
            String token = st.nextToken ().trim();
            int star = token.indexOf ("*");
            if (star == -1) {
                dontUseProxy = token.equals (host);
                if (dontUseProxy) {
                    LOG.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host " + host + " found in nonProxyHosts: " + nonProxyHosts);
                }
            } else {
                String start = token.substring (0, star - 1 < 0 ? 0 : star - 1);
                String end = token.substring (star + 1 > token.length () ? token.length () : star + 1);

                //Compare left of * if and only if * is not first character in token
                boolean compareStart = star > 0; // not first character
                //Compare right of * if and only if * is not the last character in token
                boolean compareEnd = star < (token.length() - 1); // not last character
                dontUseProxy = (compareStart && host.startsWith(start)) || (compareEnd && host.endsWith(end));

                if (dontUseProxy) {
                    LOG.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host " + host + " found in nonProxyHosts: " + nonProxyHosts);
                }
            }
        }
        return dontUseProxy;
    }
    
    private static boolean dontUseIp (String nonProxyHosts, String host) {
        if (host == null) return false;
        
        String ip = null;
        try {
            ip = InetAddress.getByName (host).getHostAddress ();
        } catch (UnknownHostException ex) {
            LOG.log (Level.FINE, ex.getLocalizedMessage (), ex);
        }
        
        if (ip == null) {
            return false;
        }

        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, "|", false);
        while (st.hasMoreTokens () && !dontUseProxy) {
            String nonProxyHost = st.nextToken ().trim();
            int star = nonProxyHost.indexOf ("*");
            if (star == -1) {
                dontUseProxy = nonProxyHost.equals (ip);
                if (dontUseProxy) {
                    LOG.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host's IP " + ip + " found in nonProxyHosts: " + nonProxyHosts);
                }
            } else {
                // match with given dotted-quad IP
                try {
                    dontUseProxy = Pattern.matches (nonProxyHost, ip);
                    if (dontUseProxy) {
                        LOG.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host's IP" + ip + " found in nonProxyHosts: " + nonProxyHosts);
                    }
                } catch (PatternSyntaxException pse) {
                    // may ignore it here
                }
            }
        }
        return dontUseProxy;
    }
    
    // NetProperties is JDK vendor specific, access only by reflection
    static boolean useSystemProxies () {
        if (useSystemProxies == null) {
            try {
                Class<?> clazz = Class.forName ("sun.net.NetProperties");
                Method getBoolean = clazz.getMethod ("getBoolean", String.class);
                useSystemProxies = getBoolean.invoke (null, "java.net.useSystemProxies");
            } catch (Exception x) {
                LOG.log (Level.FINEST, "Cannot get value of java.net.useSystemProxies bacause " + x.getMessage(), x);
            }
        }
        return useSystemProxies != null && "true".equalsIgnoreCase (useSystemProxies.toString ());
    }
    
    static boolean usePAC() {
        String s = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
        boolean usePAC = s != null && s.startsWith(ProxySettings.PAC);
        return usePAC;
    }
    
    private static String getPacFile() {
        String init = System.getProperty("netbeans.system_http_proxy"); // NOI18N
        return init.substring(4).trim();
    }
    
}

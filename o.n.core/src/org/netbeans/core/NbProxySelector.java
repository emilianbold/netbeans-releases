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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import sun.net.NetProperties;

/**
 *
 * @author Jiri Rechtacek
 */
public final class NbProxySelector extends ProxySelector {
    
    private ProxySelector original = null;
    private Logger log = Logger.getLogger (NbProxySelector.class.getName ());
    private static int num = 0;
    
    /** Creates a new instance of NbProxySelector */
    public NbProxySelector () {
        original = super.getDefault ();
        log.fine ("Override the original ProxySelector: " + original);
        log.fine ("java.net.useSystemProxies has been set to " + NetProperties.getBoolean ("java.net.useSystemProxies"));
        ProxySettings.addPreferenceChangeListener (new ProxySettingsListener ());
        copySettingsToSystem ();
    }
    
    public List<Proxy> select(URI uri) {
        List<Proxy> res = new ArrayList<Proxy> ();
        int proxyType = ProxySettings.getProxyType ();
        if (ProxySettings.DIRECT_CONNECTION == proxyType) {
            res = Collections.singletonList (Proxy.NO_PROXY);
        } else if (ProxySettings.AUTO_DETECT_PROXY == proxyType) {
            // XXX What with Solaris or KDE?
            if (NetProperties.getBoolean ("java.net.useSystemProxies")) {
                res = original.select (uri);
            } else {
                String protocol = uri.getScheme ();
                assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
                if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                    // handling nonProxyHosts first
                    if (dontUseProxy (ProxySettings.SystemProxySettings.getNonProxyHosts (), uri.getHost ())) {
                        res.add (Proxy.NO_PROXY);
                    }
                    String ports = ProxySettings.SystemProxySettings.getHttpPort ();
                    if (ports != null && ports.length () > 0 && ProxySettings.SystemProxySettings.getHttpHost ().length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (ProxySettings.SystemProxySettings.getHttpHost (), porti));
                        res.add (p);
                    }
                }
                res.addAll (original.select (uri));
            }
        } else if (ProxySettings.MANUAL_SET_PROXY == proxyType) {
            String protocol = uri.getScheme ();
            assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
            if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                // handling nonProxyHosts first
                if (dontUseProxy (ProxySettings.getNonProxyHosts (), uri.getHost ())) {
                    res.add (Proxy.NO_PROXY);
                }
                String hosts = ProxySettings.getHttpHost ();
                String ports = ProxySettings.getHttpPort ();
                if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                    int porti = Integer.parseInt(ports);
                    Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (hosts, porti));
                    res.add (p);
                } else {
                    log.info ("Incomplete HTTP Proxy [" + hosts + "/" + ports + "] found in ProxySelector[Type: " + ProxySettings.getProxyType () + "] for uri " + uri + ". ");
                    log.finest ("Fallback to the default ProxySelector which returns " + original.select (uri));
                    res.addAll (original.select (uri));
                }
            } else { // supposed SOCKS
                String ports = ProxySettings.getSocksPort ();
                String hosts = ProxySettings.getSocksHost ();
                if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                    int porti = Integer.parseInt(ports);
                    Proxy p = new Proxy (Proxy.Type.SOCKS,  new InetSocketAddress (ProxySettings.getSocksHost (), porti));
                    res.add (p);
                } else {
                    log.info ("Incomplete SOCKS Server [" + hosts + "/" + ports + "] found in ProxySelector[Type: " + ProxySettings.getProxyType () + "] for uri " + uri + ". ");
                    log.finest ("Fallback to the default ProxySelector which returns " + original.select (uri));
                    res.addAll (original.select (uri));
                }
            }
            res.add (Proxy.NO_PROXY);
        } else {
            assert false : "Invalid proxy type: " + ProxySettings.getProxyType ();
        }
        log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "] returns " + res + " for URI " + uri);
        return res;
    }
    
    public void connectFailed (URI arg0, SocketAddress arg1, IOException arg2) {
        log.log  (Level.INFO, "connectionFailed(" + arg0 + ", " + arg1 +")", arg2);
    }
    
    // XXX Copy current ProxySettings to System properties http.proxyHost, https.proxyHost, ...
    // several modules listenes on these properties and propagates it futher
    class ProxySettingsListener implements PreferenceChangeListener {
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getKey ().startsWith ("proxy")) {
                copySettingsToSystem ();
            }
        }
    }
    
    private void copySettingsToSystem () {
        String host = null, port = null, nonProxyHosts = null;
        String sHost = null, sPort = null;
        int proxyType = ProxySettings.getProxyType ();
        if (ProxySettings.DIRECT_CONNECTION == proxyType) {
            host = "";
            port = "";
            nonProxyHosts = "";
            sHost = "";
            sPort = "";
        } else if (ProxySettings.AUTO_DETECT_PROXY == proxyType) {
            host = normalizeProxyHost (ProxySettings.SystemProxySettings.getHttpHost ());
            port = ProxySettings.SystemProxySettings.getHttpPort ();
            nonProxyHosts = ProxySettings.SystemProxySettings.getNonProxyHosts ();
            sHost = ProxySettings.SystemProxySettings.getSocksHost ();
            sPort = ProxySettings.SystemProxySettings.getSocksPort ();
        } else if (ProxySettings.MANUAL_SET_PROXY == proxyType) {
            host = normalizeProxyHost (ProxySettings.getHttpHost ());
            port = ProxySettings.getHttpPort ();
            nonProxyHosts = ProxySettings.getNonProxyHosts ();
            sHost = ProxySettings.getSocksHost ();
            sPort = ProxySettings.getSocksPort ();
        } else {
            assert false : "Invalid proxy type: " + proxyType;
        }
        if (host != null && host.length() > 0) {
            System.setProperty ("http.proxyHost", host);
        }
        if (port != null && port.length() > 0) {
            try {
                Integer.parseInt (port);
                System.setProperty ("http.proxyPort", port);
            } catch (NumberFormatException nfe) {
                log.log (Level.INFO, nfe.getMessage(), nfe);
            }
        }
        if (nonProxyHosts != null && nonProxyHosts.length() > 0) {
            System.setProperty ("http.nonProxyHosts", nonProxyHosts);
        }
        if (host != null && host.length() > 0) {
            System.setProperty ("https.proxyHost", host);
        }
        if (port != null && port.length() > 0) {
            try {
                Integer.parseInt (port);
                System.setProperty ("https.proxyPort", port);
            } catch (NumberFormatException nfe) {
                log.log (Level.INFO, nfe.getMessage(), nfe);
            }
        }
        if (nonProxyHosts != null && nonProxyHosts.length() > 0) {
            System.setProperty ("https.nonProxyHosts", nonProxyHosts);
        }
        if (sHost != null && sHost.length() > 0) {
            System.setProperty ("socksProxyHost", sHost);
        }
        if (sPort != null && sPort.length() > 0) {
            try {
                Integer.parseInt (sPort);
                System.setProperty ("socksProxyPort", sPort);
            } catch (NumberFormatException nfe) {
                log.log (Level.INFO, nfe.getMessage(), nfe);
            }
        }
        log.finest ("Set System's http.proxyHost/Port/NonProxyHost to " + host + "/" + port + "/" + nonProxyHosts);
        log.finest ("Set System's socksProxyHost/Port to " + sHost + "/" + sPort);
    }

    private static String normalizeProxyHost (String proxyHost) {
        if (proxyHost.toLowerCase ().startsWith ("http://")) { // NOI18N
            return proxyHost.substring (7, proxyHost.length ());
        } else {
            return proxyHost;
        }
    }
    
    private boolean dontUseProxy (String nonProxyHosts, String host) {
        if (host == null) return false;

        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, "|", false);
        while (st.hasMoreTokens () && !dontUseProxy) {
            String token = st.nextToken ();
            int star = token.indexOf ("*");
            if (star == -1) {
                dontUseProxy = token.equals (host);
                if (dontUseProxy) {
                    log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host " + host + " found in nonProxyHosts: " + nonProxyHosts);                    
                }
            } else {
                String start = token.substring (0, star - 1 < 0 ? 0 : star - 1);
                String end = token.substring (star + 1 > token.length () ? token.length () : star + 1);
                dontUseProxy = host.startsWith(start) && host.endsWith(end);
                if (dontUseProxy) {
                    log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host " + host + " found in nonProxyHosts: " + nonProxyHosts);                    
                }
            }
        }
        return dontUseProxy;
    }
}

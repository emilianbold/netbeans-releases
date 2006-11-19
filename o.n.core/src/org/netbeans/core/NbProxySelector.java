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
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;
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
        new ProxySettings ().addPreferenceChangeListener (new ProxySettingsListener ());
        copySettingsToSystem ();
    }
    
    public List<Proxy> select(URI uri) {
        List<Proxy> res = new ArrayList<Proxy> ();
        if (ProxySettings.DIRECT_CONNECTION == ProxySettings.getProxyType ()) {
            res = Collections.singletonList (Proxy.NO_PROXY);
        } else if (ProxySettings.AUTO_DETECT_PROXY == ProxySettings.getProxyType ()) {
            // XXX What with Solaris or KDE?
            if (NetProperties.getBoolean ("java.net.useSystemProxies")) {
                res = original.select (uri);
            } else {
                String protocol = uri.getScheme ();
                assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
                if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                    String ports = ProxySettings.SystemProxySettings.getHttpPort ();
                    if (ports != null && ports.length () > 0 && ProxySettings.SystemProxySettings.getHttpHost ().length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (ProxySettings.SystemProxySettings.getHttpHost (), porti));
                        res.add (p);
                    }
                }
                res.addAll (original.select (uri));
            }
        } else if (ProxySettings.MANUAL_SET_PROXY == ProxySettings.getProxyType ()) {
            String protocol = uri.getScheme ();
            assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
            if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                // handling nonProxyHosts first
                String nphosts = ProxySettings.getNonProxyHosts ();
                String reqHost = uri.getHost ();
                boolean dontUseProxy = false;
                if (nphosts != null && nphosts.length () > 0) {
                    Pattern p = null;
                    StringTokenizer st = new StringTokenizer (nphosts, "|", false);
                    while (st.hasMoreTokens () && !dontUseProxy) {
                          p = Pattern.compile (st.nextToken ());
                          dontUseProxy = p.matcher (reqHost).matches ();
                          if (dontUseProxy) {
                            log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host " + reqHost + " found in nonProxyHosts: " + nphosts);
                          }
                    }
                }
                if (dontUseProxy) {
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
        log.info ("connectFailed (" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        // XXX: don't show Exceptions dialog, makes problem in welcome screen
        //Exceptions.printStackTrace (arg2);
        // XXX invoke Proxy Customizer??? (wait to fix issue 74855) No, don't show Options when connecting is silent (e.g. Autoupdate AutoCheck)
        //OptionsCustomizer.show ();        
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
        if (ProxySettings.DIRECT_CONNECTION == ProxySettings.getProxyType ()) {
            host = "";
            port = "";
            nonProxyHosts = "";
            sHost = "";
            sPort = "";
        } else if (ProxySettings.AUTO_DETECT_PROXY == ProxySettings.getProxyType ()) {
            host = normalizeProxyHost (ProxySettings.SystemProxySettings.getHttpHost ());
            port = ProxySettings.SystemProxySettings.getHttpPort ();
            nonProxyHosts = ProxySettings.SystemProxySettings.getNonProxyHosts ();
            sHost = ProxySettings.SystemProxySettings.getSocksHost ();
            sPort = ProxySettings.SystemProxySettings.getSocksPort ();
        } else if (ProxySettings.MANUAL_SET_PROXY == ProxySettings.getProxyType ()) {
            host = normalizeProxyHost (ProxySettings.getHttpHost ());
            port = ProxySettings.getHttpPort ();
            nonProxyHosts = ProxySettings.getNonProxyHosts ();
            sHost = ProxySettings.getSocksHost ();
            sPort = ProxySettings.getSocksPort ();
        } else {
            assert false : "Invalid proxy type: " + ProxySettings.getProxyType ();
        }
        System.setProperty ("http.proxyHost", host);
        log.finest ("Set System's http.proxyHost/Port/NonProxyHost to " + host + "/" + port + "/" + nonProxyHosts);
        System.setProperty ("http.proxyPort", port);
        System.setProperty ("http.nonProxyHosts", nonProxyHosts);
        System.setProperty ("https.proxyHost", host);
        System.setProperty ("https.proxyPort", port);
        System.setProperty ("https.nonProxyHosts", nonProxyHosts);
        System.setProperty ("socksProxyHost", sHost);
        System.setProperty ("socksProxyPort", sPort);
        log.finest ("Set System's socksHost/Port to " + sHost + "/" + sPort);
    }

    private static String normalizeProxyHost (String proxyHost) {
        if (proxyHost.toLowerCase ().startsWith ("http://")) { // NOI18N
            return proxyHost.substring (7, proxyHost.length ());
        } else {
            return proxyHost;
        }
    }
    
}

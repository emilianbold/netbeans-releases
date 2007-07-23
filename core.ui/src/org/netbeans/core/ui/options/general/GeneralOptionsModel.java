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

package org.netbeans.core.ui.options.general;

import java.util.prefs.Preferences;
import org.netbeans.core.ProxySettings;
import org.openide.util.NbPreferences;

class GeneralOptionsModel {
    
    
    private static Preferences getProxyPreferences () {
        return NbPreferences.root ().node ("org/netbeans/core");
    }
    
    int getProxyType () {
        return getProxyPreferences ().getInt (ProxySettings.PROXY_TYPE, ProxySettings.AUTO_DETECT_PROXY);
    }
    
    void setProxyType (int proxyType) {
        if (proxyType != getProxyType ()) {
            getProxyPreferences  ().putInt (ProxySettings.PROXY_TYPE, proxyType);
        }
    }
    
    String getHttpProxyHost () {
        return ProxySettings.getHttpHost ();
    }
    
    void setHttpProxyHost (String proxyHost) {
        if (!proxyHost.equals(getHttpProxyHost ())) {
            getProxyPreferences ().put (ProxySettings.PROXY_HTTP_HOST, proxyHost);
        }
    }
    
    String getHttpProxyPort () {
        return ProxySettings.getHttpPort ();
    }
    
    void setHttpProxyPort (String proxyPort) {
        if (proxyPort != getHttpProxyPort()) {
            getProxyPreferences().put(ProxySettings.PROXY_HTTP_PORT, validatePort (proxyPort) ? proxyPort : "");
        }
    }
    
    String getHttpsProxyHost () {
        return ProxySettings.getHttpsHost ();
    }
    
    void setHttpsProxyHost (String proxyHost) {
        if (!proxyHost.equals(getHttpsProxyHost ())) {
            getProxyPreferences ().put (ProxySettings.PROXY_HTTPS_HOST, proxyHost);
        }
    }
    
    String getHttpsProxyPort () {
        return ProxySettings.getHttpsPort ();
    }
    
    void setHttpsProxyPort (String proxyPort) {
        if (proxyPort != getHttpsProxyPort()) {
            getProxyPreferences().put(ProxySettings.PROXY_HTTPS_PORT, validatePort (proxyPort) ? proxyPort : "");
        }
    }
    
    String getSocksHost () {
        return ProxySettings.getSocksHost ();
    }
    
    void setSocksHost (String socksHost) {
        if (socksHost != getSocksHost()) {
            getProxyPreferences ().put (ProxySettings.PROXY_SOCKS_HOST, socksHost);
        }
    }
    
    String getSocksPort () {
        return ProxySettings.getSocksPort ();
    }
    
    void setSocksPort (String socksPort) {
        if (socksPort != getSocksPort()) {
            getProxyPreferences ().put (ProxySettings.PROXY_SOCKS_PORT, validatePort (socksPort) ? socksPort : "");
        }
    }
    
    String getNonProxyHosts () {
        return code2view (ProxySettings.getNonProxyHosts ());
    }
    
    void setNonProxyHosts (String nonProxy) {
        if (!nonProxy.equals(getNonProxyHosts())) {
            getProxyPreferences ().put (ProxySettings.NOT_PROXY_HOSTS, view2code (nonProxy));
        }
    }
    
    boolean useProxyAuthentication () {
        return ProxySettings.useAuthentication ();
    }
    
    void setUseProxyAuthentication (boolean use) {
        if (use != useProxyAuthentication()) {
            getProxyPreferences ().putBoolean (ProxySettings.USE_PROXY_AUTHENTICATION, use);
        }
    }
    
    boolean useProxyAllProtocols () {
        return ProxySettings.useProxyAllProtocols ();
    }
    
    void setUseProxyAllProtocols (boolean use) {
        if (use != useProxyAllProtocols ()) {
            getProxyPreferences ().putBoolean (ProxySettings.USE_PROXY_ALL_PROTOCOLS, use);
        }
    }
    
    String getProxyAuthenticationUsername () {
        return ProxySettings.getAuthenticationUsername ();
    }

    //TODO: not used yet - store valu just in case if modified
    void setAuthenticationUsername (String username) {
        getProxyPreferences ().put (ProxySettings.PROXY_AUTHENTICATION_USERNAME, username);
    }
    
    char [] getProxyAuthenticationPassword () {
        return ProxySettings.getAuthenticationPassword ();
    }
    
    //TODO: not used yet - store valu just in case if modified
    void setAuthenticationPassword(char [] password) {
        getProxyPreferences().put(ProxySettings.PROXY_AUTHENTICATION_PASSWORD, new String(password));
    }
    
    // private helper methods ..................................................

    private static boolean validatePort (String port) {
        if (port.trim ().length () == 0) return true;
        
        boolean ok = false;
        try {
            Integer.parseInt (port);
            ok = true;
        } catch (NumberFormatException nfe) {
            assert false : nfe;
        }
        return ok;
    }
    
    private static String code2view (String code) {
        return code == null ? code : code.replace ("|", ", ");
    }
    
    private static String view2code (String view) {
        return view == null ? view : view.replace (", ", "|");
    }
}

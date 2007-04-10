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
package org.netbeans.modules.subversion.util;

import java.util.prefs.Preferences;

/**
 *
 * @author Tomas Stupka
 */
public class ProxySettings {   

    private static final String PROXY_HTTP_HOST = "proxyHttpHost";
    private static final String PROXY_HTTP_PORT = "proxyHttpPort";
    private static final String PROXY_HTTPS_HOST = "proxyHttpsHost";
    private static final String PROXY_HTTPS_PORT = "proxyHttpsPort";
    private static final String NOT_PROXY_HOSTS = "proxyNonProxyHosts";
    private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication";
    private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername";
    private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword";
    
    private static final String PROXY_TYPE = "proxyType";
    private static final String DIRECT_CONNECTION = "0";            
    
    private String username;
    private String password;
    private String notProxyHosts;
    private boolean useAuth;
    private String httpHost;
    private String httpPort;
    private String httpsHost;
    private String httpsPort;
    private String proxyType;
      
    private String toString = null;
    
    public ProxySettings() {
        init();
    };
        
    private void init() {
        Preferences prefs = org.openide.util.NbPreferences.root ().node ("org/netbeans/core");                          // NOI18N    
        useAuth             = prefs.getBoolean ( USE_PROXY_AUTHENTICATION,       false );                               // NOI18N            
        username            = prefs.get        ( PROXY_AUTHENTICATION_USERNAME,  ""    );                               // NOI18N
        password            = prefs.get        ( PROXY_AUTHENTICATION_PASSWORD,  ""    );                               // NOI18N                
        notProxyHosts       = prefs.get        ( NOT_PROXY_HOSTS,                ""    ).replace("|", " ,");            // NOI18N                
        httpHost            = prefs.get        ( PROXY_HTTP_HOST,                ""    );                               // NOI18N                
        httpPort            = prefs.get        ( PROXY_HTTP_PORT,                ""    );                               // NOI18N                
        httpsHost           = prefs.get        ( PROXY_HTTPS_HOST,               ""    );                               // NOI18N                
        httpsPort           = prefs.get        ( PROXY_HTTPS_PORT,               ""    );                               // NOI18N                                
        proxyType           = prefs.get        ( PROXY_TYPE,                     ""    );                               // NOI18N                                        
    }
    
    public boolean isDirect() {
        return proxyType.equals(DIRECT_CONNECTION);
    }

    public boolean hasAuth() {
        return useAuth;
    }
    
    public String getHttpHost() {
        return httpHost;
    }
    
    public int getHttpPort() {
        if(httpPort.equals("")) {
            return 8080;
        }
        return Integer.parseInt(httpPort);
    }

    public String getHttpsHost() {
        return httpsHost;
    }
    
    public int getHttpsPort() {
        if(httpsPort.equals("")) {
            return 443; 
        }
        return Integer.parseInt(httpsPort);
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }    
    
    public String getNotProxyHosts() {
        return notProxyHosts;
    }
    
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(! (obj instanceof ProxySettings) ) {
            return false;
        } 
        ProxySettings ps = (ProxySettings) obj;        
        return ps.httpHost.equals(httpHost) &&
               ps.httpPort.equals(httpPort) &&
               ps.httpsHost.equals(httpsHost) &&
               ps.httpsPort.equals(httpsPort) &&
               ps.notProxyHosts.equals(notProxyHosts) &&
               ps.password.equals(password) &&
               ps.proxyType.equals(proxyType) &&
               ps.username.equals(username) &&
               ps.useAuth == useAuth;                   
    }
    
    public String toString() {
        if(toString == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append(httpHost);
            sb.append(",");        
            sb.append(httpPort);
            sb.append(",");        
            sb.append(httpsHost);
            sb.append(",");        
            sb.append(httpsPort);
            sb.append(",");        
            sb.append(notProxyHosts);
            sb.append(",");        
            sb.append(password);
            sb.append(",");        
            sb.append(proxyType);
            sb.append(",");        
            sb.append(username);
            sb.append(",");        
            sb.append(useAuth);                
            sb.append("]");
            toString = sb.toString();
        }        
        return toString;
    }

    public int hashCode() {
        return toString().hashCode();
    }
    
    
}

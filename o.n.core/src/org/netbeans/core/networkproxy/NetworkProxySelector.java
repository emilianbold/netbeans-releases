/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.networkproxy;

import java.util.prefs.Preferences;
import org.netbeans.core.ProxySettings;
import org.netbeans.core.networkproxy.fallback.FallbackNetworkProxy;
import org.netbeans.core.networkproxy.gnome.GnomeNetworkProxy;
import org.netbeans.core.networkproxy.kde.KdeNetworkProxy;
import org.netbeans.core.networkproxy.windows.WindowsNetworkProxy;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author lfischme
 */
public class NetworkProxySelector {
    
    private static NetworkProxyResolver networkProxyResolver = getNetworkProxyResolver();
    private static NetworkProxyResolver fallbackNetworkProxyResolver = getFallbackProxyResolver();
    
    private final static String COMMA = ","; //NOI18N
    private final static String GNOME = "gnome"; //NOI18N
    private final static String KDE = "kde"; //NOI18N
    private final static String RUNNING_ENV_SYS_PROPERTY = "netbeans.running.environment"; //NOI18N

    /**
     * 
     */
    public static void reloadNetworkProxy() {                
        NetworkProxySettings networkProxySettings = networkProxyResolver.getNetworkProxySettings();
        
        if (!networkProxySettings.isResolved()) {
            NetworkProxySettings fallbackNetworkProxySettings = fallbackNetworkProxyResolver.getNetworkProxySettings();
            if (fallbackNetworkProxySettings.isResolved()) {
                networkProxySettings = fallbackNetworkProxySettings;
            }
        }
                
        switch (networkProxySettings.getProxyMode()) {
            case AUTO:
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_NON_PROXY_HOSTS);
                getPreferences().put(ProxySettings.SYSTEM_PAC, networkProxySettings.getPacFileUrl());
                break;
            case MANUAL:
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTP_HOST, networkProxySettings.getHttpProxyHost());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTP_PORT, networkProxySettings.getHttpProxyPort());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTPS_HOST, networkProxySettings.getHttpsProxyHost());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_HTTPS_PORT, networkProxySettings.getHttpsProxyPort());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_SOCKS_HOST, networkProxySettings.getSocksProxyHost());
                getPreferences().put(ProxySettings.SYSTEM_PROXY_SOCKS_PORT, networkProxySettings.getSocksProxyPort());
                getPreferences().put(ProxySettings.SYSTEM_NON_PROXY_HOSTS, getStringFromArray(networkProxySettings.getNoProxyHosts()));
                getPreferences().remove(ProxySettings.SYSTEM_PAC);
                break;
            case DIRECT:
            default:
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_NON_PROXY_HOSTS);
                getPreferences().remove(ProxySettings.SYSTEM_PAC);
        }
    }
    
    private static String getStringFromArray(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length - 1; i++) {
            sb.append(stringArray[i]);
            sb.append(COMMA);
        }
        sb.append(stringArray[stringArray.length - 1]);
        
        return sb.toString();
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(ProxySettings.class);
    }
    
    private static NetworkProxyResolver getNetworkProxyResolver() {
        if (networkProxyResolver == null) {        
            if (Utilities.isWindows()) {
                return new WindowsNetworkProxy();
            } 
            
            if (Utilities.isMac()) {
                return null;
            }
            
            if (Utilities.isUnix()){
                String env = System.getProperty(RUNNING_ENV_SYS_PROPERTY);
                if (env != null) {
                    if (env.toLowerCase().equals(GNOME)) {
                        return new GnomeNetworkProxy();
                    }
                    
                    if (env.toLowerCase().equals(KDE)) {
                        return new KdeNetworkProxy();
                    }
                }
            }
            
            return new FallbackNetworkProxy();
        } else {
            return networkProxyResolver;
        }        
    }
    
    private static NetworkProxyResolver getFallbackProxyResolver() {
        if (fallbackNetworkProxyResolver == null) {
            return new FallbackNetworkProxy();
        } else {
            return fallbackNetworkProxyResolver;
        }
    }
}

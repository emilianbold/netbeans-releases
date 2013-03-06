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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.core.ProxySettings;
import org.netbeans.core.networkproxy.fallback.FallbackNetworkProxy;
import org.netbeans.core.networkproxy.gnome.GnomeNetworkProxy;
import org.netbeans.core.networkproxy.kde.KdeNetworkProxy;
import org.netbeans.core.networkproxy.mac.MacNetworkProxy;
import org.netbeans.core.networkproxy.windows.WindowsNetworkProxy;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * This class allows user to reload system network proxy settings.
 * 
 * @author lfischme
 */
public class NetworkProxySelector {
    
    private static NetworkProxyResolver networkProxyResolver = getNetworkProxyResolver();
    private static NetworkProxyResolver fallbackNetworkProxyResolver = getFallbackProxyResolver();
    
    private final static Logger LOGGER = Logger.getLogger(NetworkProxySelector.class.getName());    
    private final static String COMMA = ","; //NOI18N
    private final static String GNOME = "gnome"; //NOI18N
    private final static String KDE = "kde"; //NOI18N
    private final static String RUNNING_ENV_SYS_PROPERTY = "netbeans.running.environment"; //NOI18N

    /**
     * Reloads system proxy network settings.
     * 
     * The first it tries to retrieve proxy settings directly from system,
     * if it is unsuccessful it tries fallback (from environment property http_proxy etc.).
     */
    public static void reloadNetworkProxy() {        
        LOGGER.log(Level.FINE, "System network proxy reloading started."); //NOI18N
        NetworkProxySettings networkProxySettings = networkProxyResolver.getNetworkProxySettings();
        
        if (!networkProxySettings.isResolved()) {
            LOGGER.log(Level.WARNING, "System network proxy reloading failed! Trying fallback resolver."); //NOI18N
            NetworkProxySettings fallbackNetworkProxySettings = fallbackNetworkProxyResolver.getNetworkProxySettings();
            if (fallbackNetworkProxySettings.isResolved()) {
                LOGGER.log(Level.INFO, "System network proxy reloading succeeded. Fallback provider was successful."); //NOI18N
                networkProxySettings = fallbackNetworkProxySettings;
            } else {
                LOGGER.log(Level.WARNING, "System network proxy reloading failed! Fallback provider was unsuccessful."); //NOI18N
            }
        } else {
            LOGGER.log(Level.INFO, "System network proxy reloading succeeded."); //NOI18N
        }
                
        switch (networkProxySettings.getProxyMode()) {
            case AUTO:
                LOGGER.log(Level.FINE, "System network proxy MODE: auto"); //NOI18N
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
                LOGGER.log(Level.FINE, "System network proxy MODE: manual"); //NOI18N
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
                LOGGER.log(Level.FINE, "System network proxy MODE: direct"); //NOI18N
            default:
                LOGGER.log(Level.FINE, "System network proxy MODE: falled to default (corect if direct mode went before)"); //NOI18N
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTP_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_HTTPS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_HOST);
                getPreferences().remove(ProxySettings.SYSTEM_PROXY_SOCKS_PORT);
                getPreferences().remove(ProxySettings.SYSTEM_NON_PROXY_HOSTS);
                getPreferences().remove(ProxySettings.SYSTEM_PAC);
        }        
        LOGGER.log(Level.FINE, "System network proxy reloading fineshed."); //NOI18N
    }
    
    /**
     * Returns string from array of strings. Strings are sepparated by comma.
     * 
     * @param stringArray
     * @return String from array of strings. Strings are sepparated by comma.
     */
    private static String getStringFromArray(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            sb.append(stringArray[i]);
            if (i == stringArray.length - 1) {
                sb.append(COMMA);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Returns preferences for proxy settings.
     * 
     * @return Preferences for proxy settings.
     */
    private static Preferences getPreferences() {
        return NbPreferences.forModule(ProxySettings.class);
    }
    
    /**
     * Retuns proper network resolver for running environment.
     * 
     * If not suitable proxy resolver found, the fallback is used.
     * 
     * @return Proper network resolver for running environment.
     */
    private static NetworkProxyResolver getNetworkProxyResolver() {
        if (networkProxyResolver == null) {        
            if (Utilities.isWindows()) {
                LOGGER.log(Level.FINE, "System network proxy resolver: Windows"); //NOI18N
                return new WindowsNetworkProxy();
            } 
            
            if (Utilities.isMac()) {
                LOGGER.log(Level.FINE, "System network proxy resolver: Mac"); //NOI18N
                return new MacNetworkProxy();
            }
            
            if (Utilities.isUnix()){
                String env = System.getProperty(RUNNING_ENV_SYS_PROPERTY);
                if (env != null) {
                    if (env.toLowerCase().equals(GNOME)) {
                        LOGGER.log(Level.FINE, "System network proxy resolver: Gnome"); //NOI18N
                        return new GnomeNetworkProxy();
                    }
                    
                    if (env.toLowerCase().equals(KDE)) {
                        LOGGER.log(Level.FINE, "System network proxy resolver: KDE"); //NOI18N
                        return new KdeNetworkProxy();
                    }
                }
            }
            
            LOGGER.log(Level.WARNING, "System network proxy resolver: no suitable found, using fallback."); //NOI18N
            return new FallbackNetworkProxy();
        } else {
            return networkProxyResolver;
        }        
    }
    
    /**
     * Returns fallback proxy resolver.
     * 
     * @return Fallback proxy resolver.
     */
    private static NetworkProxyResolver getFallbackProxyResolver() {
        if (fallbackNetworkProxyResolver == null) {
            return new FallbackNetworkProxy();
        } else {
            return fallbackNetworkProxyResolver;
        }
    }
}

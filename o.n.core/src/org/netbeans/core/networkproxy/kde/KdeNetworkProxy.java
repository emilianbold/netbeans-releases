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
package org.netbeans.core.networkproxy.kde;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.core.networkproxy.NetworkProxyResolver;
import org.netbeans.core.networkproxy.NetworkProxySettings;

/**
 *
 * @author lfischme
 */
public class KdeNetworkProxy implements NetworkProxyResolver {

    private final static String EMPTY_STRING = ""; //NOI18N
    private final static String SPACE = " "; //NOI18N
    private final static String EQUALS = "="; //NOI18N
    private final static String COLON = ":"; //NOI18N
    private final static String COMMA = ","; //NOI18N
    private final static String SQ_BRACKET_LEFT = "["; //NOI18N
    private final static String HOME = "HOME"; //NOI18N
    private final static String KIOSLAVERC_PROXY_SETTINGS_GROUP = "[Proxy Settings]"; //NOI18N
    private final static String KIOSLAVERC_PROXY_TYPE = "ProxyType"; //NOI18N
    private final static String KIOSLAVERC_PROXY_CONFIG_SCRIPT = "Proxy Config Script"; //NOI18N
    private final static String KIOSLAVERC_HTTP_PROXY = "httpProxy"; //NOI18N
    private final static String KIOSLAVERC_HTTPS_PROXY = "httpsProxy"; //NOI18N
    private final static String KIOSLAVERC_SOCKS_PROXY = "socksProxy"; //NOI18N
    private final static String KIOSLAVERC_NO_PROXY_FOR = "NoProxyFor"; //NOI18N
    private final static String KIOSLAVERC_PROXY_TYPE_NONE = "0"; //NOI18N
    private final static String KIOSLAVERC_PROXY_TYPE_MANUAL = "1"; //NOI18N
    private final static String KIOSLAVERC_PROXY_TYPE_PAC = "2"; //NOI18N
    private final static String KIOSLAVERC_PROXY_TYPE_AUTO = "3"; //NOI18N
    private final static String KIOSLAVERC_PROXY_TYPE_SYSTEM = "4"; //NOI18N    
    private final static String KIOSLAVERC_PATH_IN_HOME = ".kde/share/config/kioslaverc"; //NOI18N 
    private final String KIOSLAVERC_PATH;

    public KdeNetworkProxy() {
        KIOSLAVERC_PATH = getKioslavercPath();
    }

    @Override
    public NetworkProxySettings getNetworkProxySettings() {
        Map<String, String> kioslavercMap = getKioslavercMap();

        String proxyType = kioslavercMap.get(KIOSLAVERC_PROXY_TYPE);
        if (proxyType == null) {
            return new NetworkProxySettings(false);
        }

        if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_NONE) || proxyType.equals(KIOSLAVERC_PROXY_TYPE_AUTO)) {
            return new NetworkProxySettings();
        }

        if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_PAC)) {
            String pacFileUrl = kioslavercMap.get(KIOSLAVERC_PROXY_CONFIG_SCRIPT);
            if (pacFileUrl != null) {
                return new NetworkProxySettings(pacFileUrl);
            } else {
                return new NetworkProxySettings(false);
            }
        }

        if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_MANUAL) || proxyType.equals(KIOSLAVERC_PROXY_TYPE_SYSTEM)) {                                  
            String httpProxy = kioslavercMap.get(KIOSLAVERC_HTTP_PROXY);
            String httpsProxy = kioslavercMap.get(KIOSLAVERC_HTTPS_PROXY);
            String socksProxy = kioslavercMap.get(KIOSLAVERC_SOCKS_PROXY);
            String noProxyFor = kioslavercMap.get(KIOSLAVERC_NO_PROXY_FOR);
            
            if (proxyType.equals(KIOSLAVERC_PROXY_TYPE_MANUAL)) {
                httpProxy = httpProxy == null ? EMPTY_STRING : httpProxy.trim().replaceAll(SPACE, COLON);
                httpsProxy = httpsProxy == null ? EMPTY_STRING : httpsProxy.trim().replaceAll(SPACE, COLON);
                socksProxy = socksProxy == null ? EMPTY_STRING : socksProxy.trim().replaceAll(SPACE, COLON);
            }
            
            String[] noProxyHosts = getNoProxyHosts(noProxyFor);
            
            return new NetworkProxySettings(httpProxy, httpsProxy, socksProxy, noProxyHosts);
        }

        return new NetworkProxySettings(false);
    }

    private Map<String, String> getKioslavercMap() {
        File kioslavercFile = new File(KIOSLAVERC_PATH);
        Map<String, String> map = new HashMap<String, String>();

        if (kioslavercFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(kioslavercFile);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br = new BufferedReader(new InputStreamReader(dis));
                String line;
                boolean inGroup = false;
                while ((line = br.readLine()) != null) {
                    if (inGroup) {
                        if (line.contains(EQUALS)) {
                            int indexOfEquals = line.indexOf(EQUALS);
                            String key = line.substring(0, indexOfEquals);
                            String value = line.substring(indexOfEquals + 1);
                            map.put(key, value);
                        } else if (line.startsWith(SQ_BRACKET_LEFT)) {
                            break;
                        }
                    } else if (line.startsWith(KIOSLAVERC_PROXY_SETTINGS_GROUP)) {
                        inGroup = true;
                    }
                }
                dis.close();
            } catch (FileNotFoundException ex) {
                // TODO log
            } catch (IOException ex) {
                // TODO log
            }
        }

        return map;
    }

    private String getKioslavercPath() {
        String homePath = System.getenv(HOME);

        if (homePath != null) {
            return homePath + File.separator + KIOSLAVERC_PATH_IN_HOME;
        } else {
            return EMPTY_STRING;
        }
    }
    
    private static String[] getNoProxyHosts(String noProxyHostsString) {
        if (noProxyHostsString != null && !noProxyHostsString.isEmpty()) {
            return noProxyHostsString.split(COMMA);
        }
            
        return new String[0];
    }
}

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

import org.netbeans.core.networkproxy.windows.WindowsNetworkProxy;

/**
 *
 * @author lfischme
 */
public class NetworkProxySelector {
    
    private static NetworkProxyResolver networkProxyResolver = new WindowsNetworkProxy();
        
    private final static String HTTP_PROXY_PROPERTY_KEY = "netbeans.system_http_proxy";
    private final static String HTTP_NONPROXY_PROPERTY_KEY = "netbeans.system_http_non_proxy_hosts";
    private final static String SOCKS_PROXY_PROPERTY_KEY = "netbeans.system_socks_proxy";
    
    public static void reloadNetworkProxy() {                
        NetworkProxySettings proxySettings = networkProxyResolver.getNetworkProxySettings();
                
        switch (proxySettings.getProxyMode()) {
            case AUTO:
                setSystemProperty(HTTP_PROXY_PROPERTY_KEY, "PAC " + proxySettings.getPacFileUrl());
                setSystemProperty(HTTP_NONPROXY_PROPERTY_KEY, null);
                setSystemProperty(SOCKS_PROXY_PROPERTY_KEY, null);
                break;
            case MANUAL:
                setSystemProperty(HTTP_PROXY_PROPERTY_KEY, proxySettings.getHttpProxy());
                setSystemProperty(HTTP_PROXY_PROPERTY_KEY, getStringFromArray(proxySettings.getNoProxyHosts()));
                setSystemProperty(HTTP_PROXY_PROPERTY_KEY, proxySettings.getSocksProxy());                
                break;
            case DIRECT:
            default:
                setSystemProperty(HTTP_PROXY_PROPERTY_KEY, "DIRECT");
                setSystemProperty(HTTP_NONPROXY_PROPERTY_KEY, null);
                setSystemProperty(SOCKS_PROXY_PROPERTY_KEY, null);
        }
    }
    
    private static String getStringFromArray(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length - 1; i++) {
            sb.append(stringArray[i]);
            sb.append(",");
        }
        sb.append(stringArray[stringArray.length - 1]);
        
        return sb.toString();
    }
    
    private static void setSystemProperty(String key, String value) {
        if (key != null) {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        }
    }
}

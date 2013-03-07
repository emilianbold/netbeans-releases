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
package org.netbeans.core.networkproxy.fallback;

import org.netbeans.core.networkproxy.NetworkProxyResolver;
import org.netbeans.core.networkproxy.NetworkProxySettings;

/**
 * Fallback resolver tries to retrieve proxy setting from environment variables.
 *
 * It is looking for: http_proxy, https_proxy, socks_proxy and no_proxy
 * variables. It cannot resolve if PAC is set up. Also environment variables may
 * be set but in system there are not used. Fallback cannot resolve it.
 *
 * @author lfischme
 */
public class FallbackNetworkProxy implements NetworkProxyResolver {

    private final static String AT = "@"; //NOI18N
    private final static String COMMA = ","; //NOI18N
    private final static String SLASH = "/"; //NOI18N
    private final static String PROTOCOL_PREXIF_SEPARATOR = "://"; //NOI18N
    private final static String EMPTY_STRING = ""; //NOI18N
    private final static String HTTP_PROXY_SYS_PROPERTY = "http_proxy"; //NOI18N
    private final static String HTTPS_PROXY_SYS_PROPERTY = "https_proxy"; //NOI18N
    private final static String SOCKS_PROXY_SYS_PROPERTY = "socks_proxy"; //NOI18N
    private final static String NO_PROXY_SYS_PROPERTY = "no_proxy"; //NOI18N

    @Override
    public NetworkProxySettings getNetworkProxySettings() {
        System.getenv();
        
        String httpProxyRaw = System.getenv(HTTP_PROXY_SYS_PROPERTY);
        if (httpProxyRaw != null && !httpProxyRaw.isEmpty()) {
            String httpProxy = prepareVariable(httpProxyRaw);
            String httpsProxy = prepareVariable(System.getenv(HTTPS_PROXY_SYS_PROPERTY));
            String socksProxy = prepareVariable(System.getenv(SOCKS_PROXY_SYS_PROPERTY));
            String noProxyHostsString = System.getenv(NO_PROXY_SYS_PROPERTY);
            String[] noProxyHosts = noProxyHostsString == null ? new String[0] : noProxyHostsString.split(COMMA);                   
            
            return new NetworkProxySettings(httpProxy, httpsProxy, socksProxy, noProxyHosts);
        }

        return new NetworkProxySettings();
    }

    private String prepareVariable(String variable) {
        if (variable == null) {
            return EMPTY_STRING;
        }

        // remove slash at the end if present
        if (variable.endsWith(SLASH)) {
            variable = variable.substring(0, variable.length() - 1);
        }

        // remove username and password if present
        if (variable.contains(AT)) {
            variable = variable.substring(variable.lastIndexOf(AT) + 1);
        }
        
        // remove protocol prefix if presented
        if (variable.contains(PROTOCOL_PREXIF_SEPARATOR)) {
            variable = variable.substring(variable.indexOf(PROTOCOL_PREXIF_SEPARATOR) + 3);
        }

        return variable;
    }       
}

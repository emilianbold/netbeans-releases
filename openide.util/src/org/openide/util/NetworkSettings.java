/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.util;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/** Useful static methods for getting Network Proxy required for make network
 * connection for specified resource.
 *
 * @since 8.13
 * @author Jiri Rechtacek
 */
public final class NetworkSettings {

    private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername";
    private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication";
    private static final Logger LOGGER = Logger.getLogger(NetworkSettings.class.getName());

    /** Returns the <code>hostname</code> part of network proxy address 
     * based on given URI to access the resource at.
     * Returns <code>null</code> for direct connection.
     * 
     * @param u The URI that a connection is required to
     * @return the hostname part of the Proxy address
     */
    public static String getProxyHost(URI u) {
        if (getPreferences() == null) {
            return null;
        }
        InetSocketAddress sa = analyzeProxy(u);
        return sa == null ? null : sa.getHostName();
    }

    /** Returns the <code>port</code> part of network proxy address 
     * based on given URI to access the resource at.
     * Returns <code>null</code> for direct connection.
     * 
     * @param u The URI that a connection is required to
     * @return the port part of the Proxy address
     */
    public static String getProxyPort(URI u) {
        if (getPreferences() == null) {
            return null;
        }
        InetSocketAddress sa = analyzeProxy(u);
        return sa == null ? null : Integer.toString(sa.getPort());
    }

    /** Returns the <code>username</code> for Proxy Authentication.
     * Returns <code>null</code> if no authentication required.
     * 
     * @param u The URI that a connection is required to
     * @return username for Proxy Authentication
     */
    public static String getAuthenticationUsername(URI u) {
        if (getPreferences() == null) {
            return null;
        }
        if (getPreferences().getBoolean(USE_PROXY_AUTHENTICATION, false)) {
            return getPreferences().get(PROXY_AUTHENTICATION_USERNAME, "");
        }
        return null;
    }
    
    /** Returns the <code>key</code> for reading password for Proxy Authentication.
     * Use {@link Keyring} for reading the password from the ring.
     * Returns <code>null</code> if no authentication required.
     * 
     * @param u The URI that a connection is required to
     * @return the key for reading password for Proxy Authentication from the ring
     */
    public static String getKeyForAuthenticationPassword(URI u) {
        if (getPreferences() == null) {
            return null;
        }
        if (getPreferences().getBoolean(USE_PROXY_AUTHENTICATION, false)) {
            return PROXY_AUTHENTICATION_USERNAME;
        }
        return null;
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/core"); // NOI18N
    }
    
    private static InetSocketAddress analyzeProxy(URI u) {
        if (u == null) {
            throw new IllegalArgumentException("The URI parameter cannot be null.");
        }
        List<Proxy> proxies = ProxySelector.getDefault().select(u);
        assert proxies != null : "ProxySelector cannot return null for " + u;
        assert ! proxies.isEmpty() : "ProxySelector cannot return empty list for " + u;
        Proxy p = proxies.get(0);
        if (Proxy.Type.DIRECT == p.type()) {
            // return null for DIRECT proxy
            return null;
        } else {
            if (p.address() instanceof InetSocketAddress) {
                // check is
                //assert ! ((InetSocketAddress) p.address()).isUnresolved() : p.address() + " must be resolved address.";
                return (InetSocketAddress) p.address();
            } else {
                LOGGER.log(Level.INFO, p.address() + " is not instanceof InetSocketAddress but " + p.address().getClass());
                return null;
            }
        }
    }
}

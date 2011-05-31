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
import java.net.URISyntaxException;
import java.util.Collections;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Jiri Rechtacek
 */
public class NetworkSettingsTest extends TestCase {
    private static ProxySelector defaultPS;

    public NetworkSettingsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        if (defaultPS == null) {
            defaultPS = ProxySelector.getDefault();
        }
        ProxySelector ps = new ProxySelector() {

            @Override
            public List<Proxy> select(URI uri) {
                if (uri == null) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                }
                if (uri.toString().equals("http://localhost")) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                } else if (uri.toString().startsWith("http://inner")) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                } else {
                    return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("corpcache.cache", 1234)));
                }
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        ProxySelector.setDefault(ps);
    }

    @Override
    public void tearDown() {
        ProxySelector.setDefault(defaultPS);
    }

    public void testGetProxyForLocalhost() throws URISyntaxException {
        URI u = new URI("http://localhost");
        assertNull("NetworkSettings.getProxyHost() returns null for " + u, NetworkSettings.getProxyHost(u));
        assertNull("NetworkSettings.getProxyPort() returns null for " + u, NetworkSettings.getProxyPort(u));
    }

    public void testGetProxyForRemote() throws URISyntaxException {
        URI u = new URI("http://remove.org");
        assertEquals("Check NetworkSettings.getProxyHost() for " + u, "corpcache.cache", NetworkSettings.getProxyHost(u));
        assertEquals("Check NetworkSettings.getProxyPort() for " + u, "1234", NetworkSettings.getProxyPort(u));
    }

    public void testGetProxyForIntra() throws URISyntaxException {
        URI u = new URI("http://inner.private.web");
        assertNull("NetworkSettings.getProxyHost() returns null for " + u, NetworkSettings.getProxyHost(u));
        assertNull("NetworkSettings.getProxyPort() returns null for " + u, NetworkSettings.getProxyPort(u));
    }

}
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Locale;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NetworkSettings;
import org.openide.util.lookup.ServiceProvider;

/** Check whether we can proxy to localhost.
 */
public class CanProxyToLocalhostTest extends NbTestCase {
    private static String USER_PROXY_HOST = "my.webcache";
    private static int USER_PROXY_PORT = 8080;

    private ProxySelector selector;
    private static URI TO_LOCALHOST;
    private static URI TO_NB;
    private MyPS myPS;

    public CanProxyToLocalhostTest (String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws Exception {
        myPS = new MyPS();
        selector = NbProxySelector.create(myPS);
        TO_LOCALHOST = new URI ("http://localhost");
        TO_NB = new URI ("http://netbeans.org");
    }
    
    public void testNoProxyForLocalhost() {
        Locale.setDefault(Locale.US);
        assertEquals ("Connect TO_LOCALHOST DIRECT.", "DIRECT", selector.select (TO_LOCALHOST).get(0).toString());
    }
    public void testProxyForLocalhost() {
        Locale.setDefault(new Locale("te", "ST"));
        assertEquals ("Connect TO_LOCALHOST provided by MyPS", "HTTP @ my.webcache:8080", selector.select (TO_LOCALHOST).get(0).toString());
        assertEquals("One call to my ps", 1, myPS.called);
    }
    public void testAlwaysProxyForNonLocalhost() {
        Locale.setDefault(Locale.US);
        assertEquals ("Connect TO_LOCALHOST DIRECT.", "HTTP @ my.webcache:8080", selector.select (TO_NB).get(0).toString());
        assertEquals("One call to my ps", 1, myPS.called);
    }
    
    private static class MyPS extends ProxySelector {
        int called;
        
        @Override
        public List<Proxy> select(URI uri) {
            called++;
            return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(USER_PROXY_HOST, USER_PROXY_PORT)));
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.spi.AcegiAuthorizer;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implements authentication based on Hudson's standard login form.
 * {@code main/core/src/main/resources/hudson/model/Hudson/login.jelly} shows the style.
 * Uses {@link AcegiAuthorizer} to find the username and password.
 * Assumes the Hudson instance is set up to use ACEGI-based security, not "legacy" container auth.
 */
@ServiceProvider(service=ConnectionAuthenticator.class, position=100)
public class AcegiConnectionAuthenticator implements ConnectionAuthenticator {

    private static final Logger LOGGER = Logger.getLogger(AcegiConnectionAuthenticator.class.getName());

    /**
     * Session cookies set by home.
     * {@link java.net.CookieManager} in JDK 6 would be a bit easier.
     */
    private static final Map<URL,String[]> COOKIES = new HashMap<URL,String[]>();

    public void prepareRequest(URLConnection conn, URL home) {
        if (COOKIES.containsKey(home)) {
            for (String cookie : COOKIES.get(home)) {
                String cookieBare = cookie.replaceFirst(";.*", ""); // NOI18N
                LOGGER.log(Level.FINER, "Setting cookie {0} for {1}", new Object[] {cookieBare, conn.getURL()});
                conn.setRequestProperty("Cookie", cookieBare); // NOI18N
            }
        }
    }

    public URLConnection forbidden(URLConnection conn, URL home) {
        for (AcegiAuthorizer aa : Lookup.getDefault().lookupAll(AcegiAuthorizer.class)) {
            String[] auth = aa.authorize(home);
            if (auth != null) {
                LOGGER.log(Level.FINE, "Got authorization for {0} on {1} from {2}", new Object[] {auth[0], home, aa});
                try {
                    Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
                    new ConnectionBuilder().url(new URL(home, "j_acegi_security_check")). // NOI18N
                            postData(("j_username=" + URLEncoder.encode(auth[0], "UTF-8") + "&j_password=" + // NOI18N
                            URLEncoder.encode(auth[1], "UTF-8")).getBytes("UTF-8")). // NOI18N
                            collectResponseHeaders(responseHeaders).connection();
                    List<String> cookies = responseHeaders.get("Set-Cookie"); // NOI18N
                    if (cookies == null) {
                        LOGGER.log(Level.FINE, "No cookies set from authentication to {0}", home);
                        return null;
                    }
                    LOGGER.log(Level.FINE, "Authenticated to {0}: {1}", new Object[] {home, cookies});
                    COOKIES.put(home, cookies.toArray(new String[0]));
                    return conn.getURL().openConnection();
                } catch (IOException x) {
                    LOGGER.log(Level.FINE, null, x);
                }
            }
        }
        return null;
    }

}

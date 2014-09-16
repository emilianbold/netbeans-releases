/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.weblogic.common.api;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.weblogic.common.ProxyUtils;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicRemote {

    // full weblogic code is setting this, causing CNFE on DWP
    private static final String PORTABLE_OBJECT_PROPERTY = "javax.rmi.CORBA.PortableRemoteObjectClass"; // NOI18N
    
    private final WebLogicConfiguration config;

    WebLogicRemote(WebLogicConfiguration config) {
        this.config = config;
    }

    public <T> T executeAction(@NonNull Callable<T> action, @NullAllowed Callable<String> nonProxy) throws Exception {
        ClassLoader classLoader = config.getLayout().getClassLoader();

        synchronized (this) {
            ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();

            String portable = System.getProperty(PORTABLE_OBJECT_PROPERTY);

            String originalNonProxyHosts = System.getProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS);
            String nonProxyHosts = ProxyUtils.getNonProxyHosts(nonProxy);
            if (nonProxyHosts != null) {
                System.setProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS, nonProxyHosts);
            }

            Thread.currentThread().setContextClassLoader(classLoader);
            try {
                return action.call();
            } finally {
                Thread.currentThread().setContextClassLoader(originalLoader);

                // this is not really safe considering other threads, but it is the best we can do
                if (originalNonProxyHosts == null) {
                    System.clearProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS);
                } else {
                    System.setProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS, originalNonProxyHosts);
                }

                // this is not really safe considering other threads, but it is the best we can do
                if (portable == null) {
                    System.clearProperty(PORTABLE_OBJECT_PROPERTY);
                } else {
                    System.setProperty(PORTABLE_OBJECT_PROPERTY, portable);
                }
            }
        }
    }
    
    // avoid duplicate hosts
    private static String compactNonProxyHosts (String hosts) {
        StringTokenizer st = new StringTokenizer(hosts, ","); //NOI18N
        StringBuilder nonProxyHosts = new StringBuilder();
        while (st.hasMoreTokens()) {
            String h = st.nextToken().trim();
            if (h.length() == 0) {
                continue;
            }
            if (nonProxyHosts.length() > 0) {
                nonProxyHosts.append("|"); // NOI18N
            }
            nonProxyHosts.append(h);
        }
        st = new StringTokenizer (nonProxyHosts.toString(), "|"); //NOI18N
        Set<String> set = new HashSet<String> ();
        StringBuilder compactedProxyHosts = new StringBuilder();
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            if (set.add (t.toLowerCase (Locale.US))) {
                if (compactedProxyHosts.length() > 0) {
                    compactedProxyHosts.append('|');
                }
                compactedProxyHosts.append(t);
            }
        }
        return compactedProxyHosts.toString();
    }
}

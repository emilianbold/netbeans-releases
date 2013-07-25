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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.hudson;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.util.Base64;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Connection Authenticator for ODCSS Hudson Instances.
 *
 * @author jhavlin
 */
@ServiceProvider(service = ConnectionAuthenticator.class, position = 9999)
public class ODCSPasswordAuthorizer implements ConnectionAuthenticator {

    private static final Logger LOG = Logger.getLogger(
            ODCSPasswordAuthorizer.class.getName());

    /**
     * Registry of project handles that may be asked for Hudson credentials.
     */
    public static class ProjectHandleRegistry {

        /**
         * Cache of project handles, implemented as list of weak references to
         * project handles. Assuming there is not too much open ODCSS projects.
         */
        private static final List<Reference<ProjectHandle<ODCSProject>>> cache =
                new ArrayList<Reference<ProjectHandle<ODCSProject>>>();

        public static synchronized void registerProjectHandle(
                ProjectHandle<ODCSProject> prj) {

            boolean found = false;
            Iterator<Reference<ProjectHandle<ODCSProject>>> iterator =
                    cache.iterator();
            while (iterator.hasNext()) {
                Reference<ProjectHandle<ODCSProject>> next = iterator.next();
                ProjectHandle<ODCSProject> projectHandle = next.get();
                if (projectHandle == prj) {
                    found = true; // do not break here, keep cleaning
                } else if (projectHandle == null) {
                    iterator.remove();
                }
            }
            if (!found) {
                cache.add(new WeakReference<ProjectHandle<ODCSProject>>(prj));
            }
        }

        /**
         * Find a project handle for Hudson home URL.
         */
        public static synchronized ProjectHandle<ODCSProject> findProjectHandle(
                URL hudsonHome) {
            String url = hudsonHome.toString();
            for (Reference<ProjectHandle<ODCSProject>> projectRef : cache) {
                ProjectHandle<ODCSProject> ph = projectRef.get();
                if (ph != null
                        && url.equals(ph.getTeamProject().getBuildUrl())) {
                    return ph;
                }
            }
            return null;
        }
    }

    @Override
    public void prepareRequest(URLConnection conn, URL home) {
        try {
            ProjectHandle<ODCSProject> projectHandle =
                    ProjectHandleRegistry.findProjectHandle(home);
            if (projectHandle != null) {
                ODCSClient client = ODCSHudsonUtils.getClient(projectHandle);
                if (client == null) {
                    return;
                }
                ODCSServer srv = projectHandle.getTeamProject().getServer();
                PasswordAuthentication pa = srv.getPasswordAuthentication();
                if (pa != null) {
                    String un = pa.getUserName();
                    char[] pw = pa.getPassword();
                    if (un != null && pw != null) {
                        String basicAuth = (un + ":" + new String(pw)); //NOI18N
                        byte[] bytes = basicAuth.getBytes();
                        String encoded = new String(new Base64().encode(bytes));
                        String trimmed = encoded.trim();
                        conn.setRequestProperty("Authorization", //NOI18N
                                "Basic " + trimmed);                    //NOI18N
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    @Override
    public URLConnection forbidden(URLConnection conn, URL home) {
        return null;
    }
}

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
package org.netbeans.modules.ods.hudson;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.util.Base64;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.netbeans.modules.team.c2c.api.CloudServer;
import org.netbeans.modules.team.c2c.api.ODSProject;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;

/**
 * Connection Authenticator for ODS Hudson Instances.
 *
 * @author jhavlin
 */
@ServiceProvider(service = ConnectionAuthenticator.class, position = 9999)
public class ODSPasswordAuthorizer implements ConnectionAuthenticator {

    private static final Logger LOG = Logger.getLogger(
            ODSPasswordAuthorizer.class.getName());

    /**
     * Registry of project handles that may be asked for Hudson credentials.
     */
    public static class ProjectHandleRegistry {

        private static final Set<ProjectHandle<ODSProject>> handles =
                new WeakSet<ProjectHandle<ODSProject>>();

        public static synchronized void registerProjectHandle(
                ProjectHandle<ODSProject> prj) {

            handles.add(prj);
        }

        public static synchronized void unregisterProjectHandle(
                ProjectHandle<ODSProject> prj) {

            handles.remove(prj);
        }

        /**
         * Find a project handle for Hudson home URL.
         */
        public static synchronized ProjectHandle<ODSProject> findProjectHandle(
                URL hudsonHome) {
            String url = hudsonHome.toString();
            for (ProjectHandle<ODSProject> project : handles) {
                if (url.equals(project.getTeamProject().getBuildUrl())) {
                    return project;
                }
            }
            return null;
        }
    }

    @Override
    public void prepareRequest(URLConnection conn, URL home) {
        try {
            ProjectHandle<ODSProject> projectHandle =
                    ProjectHandleRegistry.findProjectHandle(home);
            if (projectHandle != null) {
                CloudClient client = ODSHudsonUtils.getClient(projectHandle);
                if (client == null) {
                    return;
                }
                CloudServer srv = projectHandle.getTeamProject().getServer();
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

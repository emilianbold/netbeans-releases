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

package org.netbeans.modules.j2ee.weblogic9;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.netbeans.modules.weblogic.common.api.WebLogicRuntime;

/**
 *
 * @author Petr Hejl
 */
public final class CommonBridge {

    private CommonBridge() {
        super();
    }

    @NonNull
    public static WebLogicConfiguration getConfiguration(@NonNull final WLDeploymentManager dm) {
        InstanceProperties ip = dm.getInstanceProperties();
        String username = ip.getProperty(InstanceProperties.USERNAME_ATTR);
        String password = ip.getProperty(InstanceProperties.PASSWORD_ATTR);

        String serverHome = ip.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        String domainHome = ip.getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);

        final WebLogicConfiguration config;
        if (dm.isRemote()) {
            String uri = ip.getProperty(InstanceProperties.URL_ATTR);
            // it is guaranteed it is WL
            String[] parts = uri.substring(WLDeploymentFactory.URI_PREFIX.length()).split(":");

            String host = parts[0];
            String port = parts.length > 1 ? parts[1] : "";
            int realPort;
            try {
                realPort = Integer.parseInt(port);
            } catch (NumberFormatException ex) {
                realPort = 7001;
            }
            config = WebLogicConfiguration.forRemoteDomain(new File(serverHome), host, realPort, username, password);
        } else {
            config = WebLogicConfiguration.forLocalDomain(new File(serverHome), new File(domainHome), username, password);
        }
        Deployment.getDefault().addInstanceListener(new InstanceListener() {

            @Override
            public void instanceAdded(String serverInstanceID) {
            }

            @Override
            public void instanceRemoved(String serverInstanceID) {
                if (serverInstanceID.equals(dm.getUri())) {
                    WebLogicRuntime.clear(config);
                    Deployment.getDefault().removeInstanceListener(this);
                }
            }
        });
        return config;
    }

    @CheckForNull
    public static Version getVersion(@NullAllowed org.netbeans.modules.weblogic.common.api.Version version) {
        if (version == null) {
            return null;
        }
        return Version.fromJsr277OrDottedNotationWithFallback(version.toString());
    }
}

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
package org.netbeans.modules.glassfish.cloud.javaee;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.glassfish.cloud.data.GlassFishUrl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;

/**
 * Factory class producing SPI interface for Java EE platform registered with
 * GlassFish cloud.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishPlatformFactory extends J2eePlatformFactory {

    /** Cached Java EE platforms for individual URLs. */
    private static final Map<GlassFishUrl, GlassFishPlatformImpl> cache
            = new HashMap<GlassFishUrl, GlassFishPlatformImpl>();

    /**
     * Invalidate Java EE platform if currently stored in internal cache.
     * <p/>
     * @param url GlassFish cloud URL.
     */
    public static void removeJ2eePlatformImpl(GlassFishUrl url) {
         synchronized (cache) {
             cache.remove(url);
         }
    }

    /**
     * Return Java EE platform SPI interface implementation for Java EE platform
     * registered with GlassFish cloud.
     * <p/>
     * @param dm GlassFish cloud deployment manager.
     * @return Java EE platform registered with GlassFish cloud.
     */
    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        if (dm instanceof GlassFishDeploymentManager) {
            GlassFishDeploymentManager deploymentManager = (GlassFishDeploymentManager) dm;
            GlassFishUrl url = deploymentManager.url;
            GlassFishPlatformImpl platform;
            // Java EE platforms cache handling should be locked to avoid
            // concurrent modifications. 
            synchronized (cache) {
                platform = cache.get(url);
                if (platform == null) {
                    switch (url.getType()) {
                        case CLOUD:
                            platform = new GlassFishAccountPlatformImpl(url);
                            break;
                        case LOCAL:
                            platform = new GlassFishCloudPlatformImpl(url);
                            break;
                        // This is unrecheable. Being here means this class does
                        // not handle all possible values correctly.
                        default:
                            throw new IllegalArgumentException(
                                    "URL constructor set unknown URL type");
                    }
                    cache.put(url, platform);
                }
            }
            return platform;
        } else {
            throw new IllegalArgumentException(
                    "Not a deployment manager for GlassFish cloud.");
        }
    }

}

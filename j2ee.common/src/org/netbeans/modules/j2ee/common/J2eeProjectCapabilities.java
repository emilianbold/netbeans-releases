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

package org.netbeans.modules.j2ee.common;

import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

/**
 * Facade allowing queries for certain capabilities provided by Java EE runtime.
 *
 * @author Petr Hejl
 * @since 1.58
 */
public final class J2eeProjectCapabilities {

    private final J2eeModuleProvider provider;

    private J2eeProjectCapabilities(J2eeModuleProvider provider) {
        this.provider = provider;
    }

    @CheckForNull
    public static J2eeProjectCapabilities forProject(Project project) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider == null) {
            return null;
        }
        return new J2eeProjectCapabilities(provider);
    }

    public boolean isEjb30Supported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        String version = provider.getJ2eeModule().getModuleVersion();
        return J2eeModule.Type.EJB.equals(moduleType) && version.startsWith("3.0"); // NOI18N
    }

    public boolean isEjb31Supported() {
        return isEjb31FullSupported() || isEjb31LiteSupported();
    }

    public boolean isEjb31FullSupported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        String version = provider.getJ2eeModule().getModuleVersion();
        return J2eeModule.Type.EJB.equals(moduleType) && version.startsWith("3.1"); // NOI18N
    }

    public boolean isEjb31LiteSupported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        String version = provider.getJ2eeModule().getModuleVersion();
        return J2eeModule.Type.WAR.equals(moduleType) && version.startsWith("3.0"); // NOI18N
    }

    public boolean hasDefaultPersistenceProvider() {
        J2eePlatform platform  = getPlatform();
        if (platform == null) {
            // server probably not registered, can't resolve whether default provider is supported (see #79856)
            return false;
        }

        Set<Profile> profiles = platform.getSupportedProfiles(provider.getJ2eeModule().getType());
        return (profiles.contains(Profile.JAVA_EE_5) || profiles.contains(Profile.JAVA_EE_6_FULL))
                && platform.isToolSupported("defaultPersistenceProviderJavaEE5"); // NOI18N
    }

    private J2eePlatform getPlatform() {
        try {
            String instance = provider.getServerInstanceID();
            if (instance != null) {
                return Deployment.getDefault().getServerInstance(provider.getServerInstanceID()).getJ2eePlatform();
            }
        } catch (InstanceRemovedException ex) {
            // will return null
        }
        return null;
    }
}

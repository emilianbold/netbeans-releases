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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

/**
 * Facade allowing queries for certain capabilities provided by Java EE runtime.
 *
 * @author Petr Hejl
 * @since 1.58
 */
public final class Capabilities {

    private final J2eeModuleProvider provider;

    private Capabilities(J2eeModuleProvider provider) {
        this.provider = provider;
    }

    @CheckForNull
    public static Capabilities forProject(Project project) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider == null) {
            return null;
        }
        return new Capabilities(provider);
    }

    public boolean isEJB20Supported() {
        Object moduleType = provider.getJ2eeModule().getModuleType();
        // TODO consider additional capabilities not matching the Profile (?)
        return isProfileSupported(moduleType, Profile.J2EE_13);
    }

    public boolean isEJB21Supported() {
        Object moduleType = provider.getJ2eeModule().getModuleType();
        // TODO consider additional capabilities not matching the Profile (?)
        return isProfileSupported(moduleType, Profile.J2EE_14);
    }

    public boolean isEJB30Supported() {
        Object moduleType = provider.getJ2eeModule().getModuleType();
        // TODO consider additional capabilities not matching the Profile (?)
        return isProfileSupported(moduleType, Profile.JAVA_EE_5);
    }

    public boolean isEJB31Supported() {
        Object moduleType = provider.getJ2eeModule().getModuleType();
        // TODO consider additional capabilities not matching the Profile (?)
        return isProfileSupported(moduleType, Profile.JAVA_EE_6_FULL);
    }

    public boolean hasDefaultPersistenceProvider() {
        J2eePlatform platform  = getPlatform();
        if (platform == null) {
            // server probably not registered, can't resolve whether default provider is supported (see #79856)
            return false;
        }

        Set<Profile> profiles = platform.getSupportedProfiles(provider.getJ2eeModule().getModuleType());
        return (profiles.contains(Profile.JAVA_EE_5) || profiles.contains(Profile.JAVA_EE_6_FULL))
                && platform.isToolSupported("defaultPersistenceProviderJavaEE5");
    }

    private boolean isProfileSupported(Object moduleType, Profile profile) {
        J2eePlatform platform = getPlatform();
        if (platform == null) {
            return false;
        }
        // FIXME take info from project even when there is no server
        return platform.getSupportedProfiles(moduleType).contains(profile);
    }

    private J2eePlatform getPlatform() {
        try {
            return Deployment.getDefault().getServerInstance(provider.getServerInstanceID()).getJ2eePlatform();
        } catch (InstanceRemovedException ex) {
            return null;
        }
    }
}

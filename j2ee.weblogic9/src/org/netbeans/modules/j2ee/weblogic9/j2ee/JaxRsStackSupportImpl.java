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
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibraryManager;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport.WLServerLibrary;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;

/**
 *
 * @author Denis Anisimov
 */
class JaxRsStackSupportImpl implements JaxRsStackSupportImplementation {
    private static final String API = "api"; // NOI18N
    private static final String JAX_RS = "jax-rs"; // NOI18N
    private static final String JERSEY = "jersey"; //NOI18N
    private static final String JSON = "json"; //NOI18N
    private static final String JETTISON = "jettison"; //NOI18N
    private static final String ROME = "rome"; //NOI18N

    private final WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl;

    JaxRsStackSupportImpl(WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl) {
        this.platformImpl = platformImpl;
    }

    @Override
    public boolean addJsr311Api(Project project) {
        /*
         *  WL has a deployable JSR311 war. But it will appear in the project's
         *  classpath only after specific user action. This is unacceptable
         *  because generated source code requires classes independently
         *  of additional explicit user actions.
         *
         *  So the following code returns true only if there is already deployed
         *  JSR311 library on the server
         */
        WLServerLibrarySupport support = getLibrarySupport();
        Set<WLServerLibrary> libraries = support.getDeployedLibraries();
        for (WLServerLibrary library : libraries) {
            String title = library.getImplementationTitle();
            if (title != null && title.toLowerCase(Locale.ENGLISH).contains(JAX_RS) && title.toLowerCase(Locale.ENGLISH).contains(API)) {
                ServerLibrary apiLib = ServerLibraryFactory.createServerLibrary(library);
                J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
                try {
                    provider.getConfigSupport().configureLibrary(ServerLibraryDependency.minimalVersion(apiLib.getName(), apiLib.getSpecificationVersion(), apiLib.getImplementationVersion()));
                } catch (org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException ex) {
                    Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(Level.INFO, null, ex);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean extendsJerseyProjectClasspath(Project project) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        Collection<ServerLibrary> serverLibraries = getServerJerseyLibraries();
        if (provider != null && serverLibraries.size() > 0) {
            try {
                for (ServerLibrary serverLibrary : serverLibraries) {
                    provider.getConfigSupport().configureLibrary(ServerLibraryDependency.minimalVersion(serverLibrary.getName(), serverLibrary.getSpecificationVersion(), serverLibrary.getImplementationVersion()));
                }
                Preferences prefs = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
                prefs.put(BrokenServerLibrarySupport.OFFER_LIBRARY_DEPLOYMENT, Boolean.TRUE.toString());
                return true;
            } catch (org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException ex) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(Level.INFO, "Exception during extending an web project", ex); //NOI18N
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void removeJaxRsLibraries(Project project) {
        // TODO: is it possible to remove ServerLibrary from project classpath ?
    }

    private Collection<ServerLibrary> getServerJerseyLibraries() {
        WLServerLibraryManager manager = getLibraryManager();
        Collection<ServerLibrary> libraries = new LinkedList<ServerLibrary>();
        libraries.addAll(findJerseyLibraries(manager.getDeployableLibraries()));
        libraries.addAll(findJerseyLibraries(manager.getDeployedLibraries()));
        return libraries;
    }

    private Collection<ServerLibrary> findJerseyLibraries(Collection<ServerLibrary> collection) {
        Collection<ServerLibrary> result = new ArrayList<ServerLibrary>(collection.size());
        for (Iterator<ServerLibrary> iterator = collection.iterator(); iterator.hasNext();) {
            ServerLibrary library = iterator.next();
            String title = library.getImplementationTitle();
            if (title == null) {
                continue;
            }
            title = title.toLowerCase(Locale.ENGLISH);
            if (title.contains(JERSEY) || title.contains(JSON) || title.contains(ROME) || title.contains(JETTISON)) {
                result.add(library);
            }
        }
        return result;
    }

    private WLServerLibraryManager getLibraryManager() {
        return new WLServerLibraryManager(platformImpl.getDeploymentManager());
    }

    private WLServerLibrarySupport getLibrarySupport() {
        return new WLServerLibrarySupport(platformImpl.getDeploymentManager());
    }

}

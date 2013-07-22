/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.DeploymentException;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;


@ProjectServiceProvider(service = {PrerequisitesChecker.class, LateBoundPrerequisitesChecker.class}, projectType={
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_OSGI
})
public class J2EEPrerequisitesChecker implements PrerequisitesChecker, LateBoundPrerequisitesChecker {

    private List<String> SINGLE_ACTIONS = Arrays.asList(new String[] {
        ActionProvider.COMMAND_RUN_SINGLE + ".deploy",
        ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy",
        ActionProvider.COMMAND_PROFILE + ".deploy"
    });

    private List applicableActions = Arrays.asList(new String[] {
        ActionProvider.COMMAND_RUN,
        ActionProvider.COMMAND_RUN_SINGLE + ".deploy",
        ActionProvider.COMMAND_DEBUG,
        ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy",
        ActionProvider.COMMAND_PROFILE,
        ActionProvider.COMMAND_PROFILE + ".deploy"
    });
    

    @Override
    public boolean checkRunConfig(RunConfig config) {
        String actionName = config.getActionName();
        if (!applicableActions.contains(actionName)) {
            return true;
        }

        // Checking if the Servlet URI is set --> See issue #227324
        if (SINGLE_ACTIONS.contains(actionName)) {
            String urlPath = config.getProperties().get(ExecutionChecker.CLIENTURLPART);
            if (urlPath == null || "".equals(urlPath)) {
                return false;
            }
        }

        J2eeModuleProvider provider = config.getProject().getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            checkWarInplace(config, provider);
//            boolean isReady = provider.getConfigSupport().ensureConfigurationReady();
            //TODO report not-readiness.
        }
        return true;
    }

    private void checkWarInplace(RunConfig config, J2eeModuleProvider provider) {
        if (provider instanceof WebModuleProviderImpl) {
            Iterator it = config.getGoals().iterator();
            boolean inplace = false;
            while (it.hasNext()) {
                String goal = (String) it.next();
                if (goal.indexOf(":inplace") > -1) { //NOI18N
                    inplace = true;
                    break;
                }
            }
            ((WebModuleProviderImpl) provider).getModuleImpl().setWarInplace(inplace);
        }
    }

    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        String actionName = config.getActionName();
        if (!(ActionProvider.COMMAND_CLEAN.equals(actionName) || ActionProvider.COMMAND_REBUILD.equals(actionName))) {
            return true;
        }
        J2eeModuleProvider provider = config.getProject().getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            if (ExecutionChecker.DEV_NULL.equals(provider.getServerID())
                    || ExecutionChecker.DEV_NULL.equals(provider.getServerInstanceID())) {
                return true;
            }
            try {
                Deployment.getDefault ().undeploy(provider, false, new ExecutionChecker.DLogger(con.getInputOutput().getOut()));
            } catch (DeploymentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return true;
    }
    
}

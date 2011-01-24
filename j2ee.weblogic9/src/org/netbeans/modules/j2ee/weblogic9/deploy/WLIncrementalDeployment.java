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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WLIncrementalDeployment extends IncrementalDeployment implements IncrementalDeployment2 {

    private static final Logger LOGGER = Logger.getLogger(WLIncrementalDeployment.class.getName());

    private static final boolean FORBID_DIRECTORY_DEPLOYMENT = Boolean.getBoolean(WLIncrementalDeployment.class.getName() + ".forbidDirectoryDeployment");

    private final WLDeploymentManager dm;

    public WLIncrementalDeployment(WLDeploymentManager dm) {
        this.dm = dm;
    }

    @Override
    public boolean canFileDeploy(Target target, J2eeModule deployable) {
        if (FORBID_DIRECTORY_DEPLOYMENT) {
            return false;
        }
        return deployable != null && !J2eeModule.Type.CAR.equals(deployable.getType())
                && !J2eeModule.Type.RAR.equals(deployable.getType());
    }

    @Override
    public File getDirectoryForModule(TargetModuleID module) {
        return null;
    }

    @Override
    public File getDirectoryForNewApplication(Target target, J2eeModule app, ModuleConfiguration configuration) {
        return null;
    }

    @Override
    public File getDirectoryForNewModule(File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        return null;
    }

    @Override
    public ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes) {
        boolean redeploy = changes.classesChanged() || changes.descriptorChanged()
                || changes.ejbsChanged() || changes.manifestChanged() || changes.serverDescriptorChanged();
        if (changes instanceof DeploymentChangeDescriptor) {
            DeploymentChangeDescriptor deploymentChanges = (DeploymentChangeDescriptor) changes;
            redeploy = redeploy || deploymentChanges.serverResourcesChanged();
        }

        if (!redeploy) {
            WLProgressObject progress = new WLProgressObject(module);
            progress.fireProgressEvent(module, new WLDeploymentStatus(
                    ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                    NbBundle.getMessage(WLIncrementalDeployment.class, "MSG_Deployment_Completed")));
            return progress;
        }

        return dm.redeploy(new TargetModuleID[] {module});
//        CommandBasedDeployer deployer = new CommandBasedDeployer(WLDeploymentFactory.getInstance(),
//                dm.getInstanceProperties());
//        return deployer.directoryRedeploy(module);
    }

    @Override
    public ProgressObject initialDeploy(Target target, J2eeModule app, ModuleConfiguration configuration,
            File dir) {

        String name = dir.getName();
        // FIXME this needs more fine tuning (escape chars)
        try {
            FileObject content = app.getContentDirectory();
            if (content != null) {
                Project project = FileOwnerQuery.getOwner(content);
                if (project != null) {
                    name = ProjectUtils.getInformation(project).getName();
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        CommandBasedDeployer deployer = new CommandBasedDeployer(dm);
        return deployer.directoryDeploy(target, name, dir, dm.getHost(), dm.getPort(), app.getType());
    }

    @Override
    public ProgressObject incrementalDeploy(TargetModuleID module, DeploymentContext context) {
        dm.deployOptionalPackages(context.getRequiredLibraries());
        return incrementalDeploy(module, context.getChanges());
    }

    @Override
    public ProgressObject initialDeploy(Target target, DeploymentContext context) {
        dm.deployOptionalPackages(context.getRequiredLibraries());
        return initialDeploy(target, context.getModule(), null, context.getModuleFile());
    }

    @Override
    public boolean isDeployOnSaveSupported() {
        return true;
    }

    @Override
    public ProgressObject deployOnSave(TargetModuleID module, DeploymentChangeDescriptor desc) {
        return incrementalDeploy(module, desc);
    }

    @Override
    public String getModuleUrl(TargetModuleID module) {
        assert module != null;

        if (module.getWebURL() == null) {
            String url = module.getModuleID();
            return url.startsWith("/") ? url : "/" + url;
        }

        // TODO is this hack ?
        // looks like weblogic (TargetModulesIDs returned by server)
        // is using weburl as moduleID for war in ear
        // and ejb jar name for ejb in ear, we need moduleURI
        final String id = module.getModuleID();
        WLConnectionSupport support = new WLConnectionSupport(dm);
        String url = null;
        try {
            url = support.executeAction(new WLConnectionSupport.JMXRuntimeAction<String>() {

                @Override
                public String call(MBeanServerConnection con, ObjectName service) throws Exception {
                    ObjectName pattern = new ObjectName(
                            "com.bea:Type=WebAppComponentRuntime,*"); // NOI18N

                    Set<ObjectName> runtimes = con.queryNames(pattern, null);
                    for (ObjectName runtime : runtimes) {
                        String moduleId = (String) con.getAttribute(runtime, "ModuleId"); // NOI18N
                        if (id.equals(moduleId)) {
                            return (String) con.getAttribute(runtime, "ModuleURI"); // NOI18N
                        }
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            // pass through
        }
        if (url != null) {
            return url.startsWith("/") ? url : "/" + url; // NOI18N
        }
        // will fail probably
        return id.startsWith("/") ? id : "/" + id; // NOI18N
    }
}

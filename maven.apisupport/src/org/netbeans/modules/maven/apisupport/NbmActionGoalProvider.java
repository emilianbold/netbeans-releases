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
package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mkleint
 */
@ServiceProvider(service=MavenActionsProvider.class, position=55)
public class NbmActionGoalProvider implements MavenActionsProvider {
    static final String NBMRELOAD = "nbmreload";
    private static final String RELOAD_TARGET = "reload-target"; // #190469
    
    private AbstractMavenActionsProvider platformDelegate = new AbstractMavenActionsProvider() {

        protected @Override InputStream getActionDefinitionStream() {
            String path = "platformActionMappings.xml"; //NOI18N
            InputStream in = getClass().getResourceAsStream(path);
            assert in != null : "no instream for " + path; //NOI18N
            return in;
        }

        @Override
        public boolean isActionEnable(String action, Project project, Lookup lookup) {
            return isActionEnable(action, project, lookup);
        }
    };
    private AbstractMavenActionsProvider ideDelegate = new AbstractMavenActionsProvider() {

        protected @Override InputStream getActionDefinitionStream() {
            String path = "ideActionMappings.xml"; //NOI18N
            InputStream in = getClass().getResourceAsStream(path);
            assert in != null : "no instream for " + path; //NOI18N
            return in;
        }

        @Override
        public boolean isActionEnable(String action, Project project, Lookup lookup) {
            return isActionEnable(action, project, lookup);
        }
    };


    public @Override Set<String> getSupportedDefaultActions() {
        return new HashSet<String>(Arrays.asList(NBMRELOAD, RELOAD_TARGET));
    }
    
    
    @ActionID(id = "org.netbeans.modules.maven.apisupport.NBMReload", category = "Project")
    @ActionRegistration(displayName = "#ACT_NBM_Reload")
    @ActionReference(position = 1250, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_NBM_Reload=Install/Reload in Development IDE")
    public static ContextAwareAction createReloadAction() {
        ContextAwareAction a = (ContextAwareAction) ProjectSensitiveActions.projectCommandAction(NBMRELOAD, ACT_NBM_Reload(), null);
        a.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        return a;
    }

    @ActionID(id = "org.netbeans.modules.maven.apisupport.NBMReloadTarget", category = "Project")
    @ActionRegistration(displayName = "#ACT_NBM_Reload_Target")
    @ActionReference(position = 1225, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_NBM_Reload_Target=Reload in Target Platform")
    public static ContextAwareAction createReloadTargetAction() {
        ContextAwareAction a = (ContextAwareAction) ProjectSensitiveActions.projectCommandAction(RELOAD_TARGET, ACT_NBM_Reload_Target(), null);
        a.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        return a;
    }

    public @Override synchronized boolean isActionEnable(String action, Project project, Lookup lookup) {
        if (RELOAD_TARGET.equals(action) || NBMRELOAD.equals(action)) {
            return hasNbm(project);
        }
        if (ActionProvider.COMMAND_RUN.equals(action) || ActionProvider.COMMAND_DEBUG.equals(action)) {
            return hasNbm(project) || isPlatformApp(project);
        }
        if (ActionProvider.COMMAND_TEST.equals(action)) {
            return isPlatformApp(project);
        }
        return false;
    }

    @Messages("NbmActionGoalProvider.target_platform_not_running=You can only reload a module while running the application.")
    public @Override RunConfig createConfigForDefaultAction(String actionName,
            Project project,
            Lookup lookup) {
        if (RELOAD_TARGET.equals(actionName) && hasNbm(project)) {
            // Cf. ModuleActions.createReloadAction.
            Project app = MavenNbModuleImpl.findAppProject(project);
            if (app != null) {
                if (!FileUtilities.resolveFilePath(FileUtil.toFile(app.getProjectDirectory()), "target/userdir/lock").isFile()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbmActionGoalProvider_target_platform_not_running(), NotifyDescriptor.WARNING_MESSAGE));
                    return null;
                }
                RunConfig rc = createConfig(actionName, app, lookup, platformDelegate);
                assert rc != null;
                rc.setPreExecution(RunUtils.createRunConfig(FileUtil.toFile(project.getProjectDirectory()), project, rc.getTaskDisplayName(), Collections.singletonList("package")));
                MavenProject prj = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
                String outputDir = PluginPropertyUtils.getPluginProperty(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_JAR, "directory", "jar"); // NOI18N
                if (outputDir == null) {
                    outputDir = "target"; // NOI18N
                }
                // XXX why does getPluginProperty(prj, GROUP_APACHE_PLUGINS, PLUGIN_JAR, "finalName", "jar") not work?
                rc.setProperty("module", "'" + FileUtilities.resolveFilePath(FileUtil.toFile(project.getProjectDirectory()), outputDir + "/" + prj.getArtifactId() + "-" + prj.getVersion() + ".jar") + "'"); // NOI18N
                return rc;
            }
        }
        if (!ActionProvider.COMMAND_RUN.equals(actionName) &&
                !ActionProvider.COMMAND_DEBUG.equals(actionName) &&
                !ActionProvider.COMMAND_TEST.equals(actionName) &&
                !NBMRELOAD.equals(actionName)) {
            return null;
        }
        if (isPlatformApp(project)) {
            return createConfig(actionName, project, lookup, platformDelegate);
        }
        if (hasNbm(project)) {
            return createConfig(actionName, project, lookup, ideDelegate);
        }
        return null;
    }

    public @Override NetbeansActionMapping getMappingForAction(String actionName,
            Project project) {
        if (RELOAD_TARGET.equals(actionName) && hasNbm(project)) {
            Project app = MavenNbModuleImpl.findAppProject(project);
            if (app != null) {
                return createMapping(actionName, app, platformDelegate);
            }
        }
        if (!ActionProvider.COMMAND_RUN.equals(actionName) &&
                !ActionProvider.COMMAND_DEBUG.equals(actionName) &&
                !ActionProvider.COMMAND_TEST.equals(actionName) &&
                !NBMRELOAD.equals(actionName)) {
            return null;
        }
        if (isPlatformApp(project)) {
            return createMapping(actionName, project, platformDelegate);
        }
        if (hasNbm(project)) {
            return createMapping(actionName, project, ideDelegate);
        }
        return null;
    }

    private RunConfig createConfig(String actionName, Project project, Lookup lookup, AbstractMavenActionsProvider delegate) {
        RunConfig conf = delegate.createConfigForDefaultAction(actionName, project, lookup);
        if (conf != null && hasNbm(project)) { //not for platform app anymore
            NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
            if (mp.getMavenProject().getProperties().getProperty(MavenNbModuleImpl.PROP_NETBEANS_INSTALL) == null) {
                conf.setProperty(MavenNbModuleImpl.PROP_NETBEANS_INSTALL, guessNetbeansInstallation());
            }
        }
        return conf;
    }

    private NetbeansActionMapping createMapping(String actionName, Project project, AbstractMavenActionsProvider delegate) {
        NetbeansActionMapping mapp = delegate.getMappingForAction(actionName, project);
        if (mapp != null &&hasNbm(project)) { //not for platform app anymore
            NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
            if (mp.getMavenProject().getProperties().getProperty(MavenNbModuleImpl.PROP_NETBEANS_INSTALL) == null) {
                mapp.getProperties().setProperty(MavenNbModuleImpl.PROP_NETBEANS_INSTALL, guessNetbeansInstallation());
            }
        }
        return mapp;
    }

    private boolean hasNbm(Project project) {
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        String pack = watch.getPackagingType();
//        boolean isPom = NbMavenProject.TYPE_POM.equals(pack);
        boolean hasNbm = NbMavenProject.TYPE_NBM.equals(pack);
        //#139279 opening a pom project with a log ot submodules cases this to be
        // a heavy perfomance burden.
        // we handle platform app and single nbm files automatically, the multimodule ide projects have to be setup
        // manually unfortunately.
//        if (isPom) {
//            SubprojectProvider prov = project.getLookup().lookup(SubprojectProvider.class);
//            for (Project prj : prov.getSubprojects()) {
//                NbMavenProject w2 = prj.getLookup().lookup(NbMavenProject.class);
//                if (NbMavenProject.TYPE_NBM.equals(w2.getPackagingType())) {
//                    hasNbm = true;
//                    break;
//                }
//            }
//        }
        return hasNbm;
    }

    private String guessNetbeansInstallation() {
        //TODO netbeans.home is obsolete.. what to replace it with though?
        File fil = new File(System.getProperty("netbeans.home")); //NOI18N
        fil = FileUtil.normalizeFile(fil);
        return fil.getParentFile().getAbsolutePath(); //NOI18N
    }

    private boolean isPlatformApp(Project p) {
        NbMavenProject watch = p.getLookup().lookup(NbMavenProject.class);
        String pack = watch.getPackagingType();
        if (NbMavenProject.TYPE_NBM_APPLICATION.equals(pack)) {
            return true;
        }
        return false;
    }

}

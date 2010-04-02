/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.maven.spi.actions.MavenActionsProvider.class, position=55)
public class NbmActionGoalProvider implements MavenActionsProvider {
    static final String NBMRELOAD = "nbmreload";
    
    private AbstractMavenActionsProvider platformDelegate = new AbstractMavenActionsProvider() {

        protected InputStream getActionDefinitionStream() {
            String path = "/org/netbeans/modules/maven/apisupport/platformActionMappings.xml"; //NOI18N
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

        protected InputStream getActionDefinitionStream() {
            String path = "/org/netbeans/modules/maven/apisupport/ideActionMappings.xml"; //NOI18N
            InputStream in = getClass().getResourceAsStream(path);
            assert in != null : "no instream for " + path; //NOI18N
            return in;
        }

        @Override
        public boolean isActionEnable(String action, Project project, Lookup lookup) {
            return isActionEnable(action, project, lookup);
        }
    };


    /** Creates a new instance of NbmActionGoalProvider */
    public NbmActionGoalProvider() {
    }
    
    public Set<String> getSupportedDefaultActions() {
        return Collections.singleton(NBMRELOAD);
    }
    
    
    public static Action createReloadAction() {
        Action a = ProjectSensitiveActions.projectCommandAction(NBMRELOAD, NbBundle.getMessage(NbmActionGoalProvider.class, "ACT_NBM_Reload"), null);
        a.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        return a;
    }

    public synchronized boolean isActionEnable(String action, Project project, Lookup lookup) {
        if (!ActionProvider.COMMAND_RUN.equals(action) &&
                !ActionProvider.COMMAND_DEBUG.equals(action) &&
                !NBMRELOAD.equals(action)) {
            return false;
        }
        if (isPlatformApp(project)) {
            return true;
        } else if (hasNbm(project)) {
            return true;
        }

        return false;
    }

    public RunConfig createConfigForDefaultAction(String actionName,
            Project project,
            Lookup lookup) {
        if (!ActionProvider.COMMAND_RUN.equals(actionName) &&
                !ActionProvider.COMMAND_DEBUG.equals(actionName) &&
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

    public NetbeansActionMapping getMappingForAction(String actionName,
            Project project) {
        if (!ActionProvider.COMMAND_RUN.equals(actionName) &&
                !ActionProvider.COMMAND_DEBUG.equals(actionName) &&
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
                conf.getProperties().setProperty(MavenNbModuleImpl.PROP_NETBEANS_INSTALL, guessNetbeansInstallation());
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

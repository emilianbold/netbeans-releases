/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.options.MavenVersionSettings;

/**
 *
 * @author mkleint
 */
class AddOSGiParamToNbmPluginConfiguration implements ModelOperation<POMModel> {
    private static final String GROUPID_MOJO = "org.codehaus.mojo";
    private static final String NBM_PLUGIN = "nbm-maven-plugin";
    private final boolean useOsgi;
    private final MavenProject mp;


    AddOSGiParamToNbmPluginConfiguration(boolean useOsgiDeps, MavenProject mavenProject) {
        mp = mavenProject;
        useOsgi = useOsgiDeps;
    }

    @Override
    public void performOperation(POMModel model) {
        Project p = model.getProject();
        Build bld = p.getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            p.setBuild(bld);
        }
        Plugin plg = bld.findPluginById(GROUPID_MOJO, NBM_PLUGIN);
        if (plg == null) {
            //how come the plugin is not there? maybe using on wrong project?
            //check plugin management first.
            PluginManagement pm = bld.getPluginManagement();
            if (pm != null) {
                plg = pm.findPluginById(GROUPID_MOJO, NBM_PLUGIN);
            }
            if (plg == null) {
                plg = model.getFactory().createPlugin();
                bld.addPlugin(plg);
                plg.setGroupId(GROUPID_MOJO);
                plg.setArtifactId(NBM_PLUGIN);
                //just set 3.2 here, so that we don't need to update before 6.9 release.
                plg.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_NBM));
            }
        }
        Configuration cnf = plg.getConfiguration();
        if (cnf == null) {
            cnf = model.getFactory().createConfiguration();
            plg.setConfiguration(cnf);
        }
        cnf.setSimpleParameter("useOSGiDependencies", Boolean.toString(useOsgi));
    }

}

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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.buildplan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.apache.maven.lifecycle.NoSuchPhaseException;
import org.apache.maven.lifecycle.model.MojoBinding;
import org.apache.maven.lifecycle.model.Phase;
import org.apache.maven.lifecycle.plan.BuildPlan;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.buildplan.nodes.MojoNode;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Anuradha G
 */
public class BuildPlanUtil {

    public static final String PHASE_NONE_SPECIFIED = NbBundle.getMessage(MojoNode.class, "LBL_None_Specified");

    private BuildPlanUtil() {
    }

    public static BuildPlanGroup getMojoBindingsGroupByPhase(BuildPlan buildPlan) throws NoSuchPhaseException {
        BuildPlanGroup bpg = new BuildPlanGroup();
        List<MojoBinding> bindings = buildPlan.renderExecutionPlan(new Stack());


        for (MojoBinding mb : bindings) {
            Phase phase = mb.getPhase();
            if (mb.getGroupId().equals("org.apache.maven.plugins.internal") //NOi18N
                    && mb.getArtifactId().equals("maven-state-management")) {
                //ignore this
                continue;
            }

            String phaseKey = PHASE_NONE_SPECIFIED;
            if (phase != null) {
                phaseKey = phase.getName();
            }
            bpg.putMojoBinding(phaseKey, mb);
        }





        buildPlan.resetExecutionProgress();
        return bpg;

    }

    /**ref by ModulesNode
     *todo : move to some api utils class
     */
    public static Collection<MavenProject> getSubProjects(MavenProject project) {
        Collection<MavenProject> modules = new ArrayList<MavenProject>();
        File base = project.getBasedir();
        for (Iterator it = project.getModules().iterator(); it.hasNext();) {
            String elem = (String) it.next();
            File projDir = FileUtil.normalizeFile(new File(base, elem));
            FileObject fo = FileUtil.toFileObject(projDir);
            if (fo != null) {
                try {
                    Project prj = ProjectManager.getDefault().findProject(fo);
                    if (prj != null) {
                        NbMavenProject mp = prj.getLookup().lookup(NbMavenProject.class);
                        if (mp != null) {
                            modules.add(mp.getMavenProject());
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return modules;
    }
}

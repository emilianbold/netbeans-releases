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
package org.netbeans.modules.profiler.freeform;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.profiler.api.project.AntProjectSupport;
import org.netbeans.modules.profiler.nbimpl.project.AbstractAntProjectSupportProvider;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.project.AntProjectSupportProvider.class, 
                        projectTypes={@ProjectType(id="org-netbeans-modules-ant-freeform", position=1220)}) // NOI18N
public final class FreeFormAntProjectSupportProvider extends AbstractAntProjectSupportProvider {    

    @Override
    public String getProfilerTargetName(FileObject buildScript, int type, FileObject profiledClassFile) {
        Project project = getProject();
        final Element e = ProjectUtils.getAuxiliaryConfiguration(project).getConfigurationFragment("data", // NOI18N
                ProjectUtilities.PROFILER_NAME_SPACE,
                false);
        String profileTarget = e.getAttribute(FreeFormProjectsSupport.PROFILE_TARGET_ATTRIBUTE);
        String profileSingleTarget = e.getAttribute(FreeFormProjectsSupport.PROFILE_SINGLE_TARGET_ATTRIBUTE);

        switch (type) {
            case AntProjectSupport.TARGET_PROFILE:
                profileTarget = FreeFormProjectsSupport.selectProfilingTarget(project, buildScript, AntProjectSupport.TARGET_PROFILE, profileTarget);

                if (profileTarget == null) {
                    return null; // cancelled by the user
                }

                FreeFormProjectsSupport.saveProfilerConfig(project, profileTarget, profileSingleTarget);

                return profileTarget;
            case AntProjectSupport.TARGET_PROFILE_SINGLE:
                profileSingleTarget = FreeFormProjectsSupport.selectProfilingTarget(project, buildScript, AntProjectSupport.TARGET_PROFILE_SINGLE, profileSingleTarget);

                if (profileSingleTarget == null) {
                    return null; // cancelled by the user
                }

                FreeFormProjectsSupport.saveProfilerConfig(project, profileTarget, profileSingleTarget);

                return profileSingleTarget;
            default:
                return null;
        }
    }

    @Override
    public FileObject getProjectBuildScript() {
        return Util.getProjectBuildScript(getProject());
    }
    
    
    public FreeFormAntProjectSupportProvider(Project project) {
        super(project);
    }
    
}

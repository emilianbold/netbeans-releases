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
package org.netbeans.modules.cnd.gizmo;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = CppSymbolDemanglerFactory.class)
public final class CppSymbolDemanglerFactoryImpl implements CppSymbolDemanglerFactory {

    @Override
    public CppSymbolDemangler getForCurrentSession(Map<String, String> serviceInfo) {
        if (serviceInfo == null ||
                serviceInfo.get(GizmoServiceInfo.CPP_COMPILER) == null ||
                serviceInfo.get(GizmoServiceInfo.CPP_COMPILER_BIN_PATH) == null) {

            Project project = org.netbeans.api.project.ui.OpenProjects.getDefault().getMainProject();

            if (project == null) {
                Project[] projects = org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
                if (projects.length == 1) {
                    project = projects[0];
                }
            }

            NativeProject nPrj = (project == null) ? null
                    : project.getLookup().lookup(NativeProject.class);

            MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);

            if (nPrj == null || conf == null) {
                return new CppSymbolDemanglerImpl(ExecutionEnvironmentFactory.getLocal(), CPPCompiler.GNU, null);
            }

            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();

            ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
            CPPCompiler cppCompiler;
            if (compilerSet.getCompilerFlavor().isGnuCompiler()) {
                cppCompiler = CPPCompiler.GNU;
            } else {
                cppCompiler = CPPCompiler.SS;
            }
            String compilerBinDir = compilerSet.getDirectory();
            return new CppSymbolDemanglerImpl(execEnv, cppCompiler, compilerBinDir);
        } else {
            ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.fromUniqueID(serviceInfo.get(ServiceInfoDataStorage.EXECUTION_ENV_KEY));
            CPPCompiler cppCompiler = CppSymbolDemanglerFactory.CPPCompiler.valueOf(serviceInfo.get(GizmoServiceInfo.CPP_COMPILER));
            String compilerBinDir = serviceInfo.get(GizmoServiceInfo.CPP_COMPILER_BIN_PATH);
            return new CppSymbolDemanglerImpl(execEnv, cppCompiler, compilerBinDir);
        }
    }

    @Override
    public CppSymbolDemangler getDemanglerFor(CPPCompiler cppCompiler) {
        return new CppSymbolDemanglerImpl(ExecutionEnvironmentFactory.getLocal(), cppCompiler, null);
    }
}

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
package org.netbeans.modules.cnd.discovery.buildsupport;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;

/**
 *
 * @author Alexander Simon
 */
public final class BuildTraceSupport {
    public static final String CND_TOOLS = "__CND_TOOLS__"; //NOI18N
    public static final String CND_BUILD_LOG = "__CND_BUILD_LOG__"; //NOI18N
    private static final String SEPARATOR = ":"; //NOI18N
    
    private BuildTraceSupport() {
    }
    
    public static boolean useBuildTrace(MakeConfiguration conf) {
        return conf.getCodeAssistanceConfiguration().getBuildAnalyzer().getValue();
    }
    
    public static String getTools(MakeConfiguration conf, ExecutionEnvironment execEnv) {
        String res = conf.getCodeAssistanceConfiguration().getTools().getValue();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        res = prepengTool(compilerSet, execEnv, PredefinedToolKind.CCompiler, res);
        res = prepengTool(compilerSet, execEnv, PredefinedToolKind.CCCompiler, res);
        res = prepengTool(compilerSet, execEnv, PredefinedToolKind.FortranCompiler, res);
        return res;
    }

    public static boolean supportedPlatforms(ExecutionEnvironment execEnv) {
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            HostInfo.OSFamily osFamily = hostInfo.getOSFamily();
            HostInfo.CpuFamily cpuFamily = hostInfo.getCpuFamily();
            String version = hostInfo.getOS().getVersion();
            
            switch(osFamily) {
                case MACOSX:
                    return cpuFamily == HostInfo.CpuFamily.X86;
                case LINUX:
                    return cpuFamily == HostInfo.CpuFamily.X86;
                case SUNOS:
                    return cpuFamily == HostInfo.CpuFamily.X86 || cpuFamily == HostInfo.CpuFamily.SPARC;
            }
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }
        return false;
    }

    public static Set<String> getCompilerNames(Project project, PredefinedToolKind kind) {
        Set<String> res = new HashSet<String>();
        switch(kind) {
            case CCompiler:
            {
                res.add("cc"); //NOI18N
                res.add("gcc"); //NOI18N
                res.add("xgcc"); //NOI18N
                res.add("clang"); //NOI18N
                res.add("icc"); //NOI18N
                addTool(project, kind, res);
                break;
            }
            case CCCompiler:
            {
                res.add("CC"); //NOI18N
                res.add("g++"); //NOI18N
                res.add("c++"); //NOI18N
                res.add("clang++"); //NOI18N
                res.add("icpc"); //NOI18N
                res.add("cl"); //NOI18N
                addTool(project, kind, res);
                break;
            }
            case FortranCompiler:
            {
                res.add("ffortran"); //NOI18N
                res.add("f77"); //NOI18N
                res.add("f90"); //NOI18N
                res.add("f95"); //NOI18N
                res.add("gfortran"); //NOI18N
                res.add("g77"); //NOI18N
                res.add("g90"); //NOI18N
                res.add("g95"); //NOI18N
                res.add("ifort"); //NOI18N
                addTool(project, kind, res);
            }
        }
        return res;
    }
    
    private static String prepengTool(CompilerSet compilerSet, ExecutionEnvironment execEnv, PredefinedToolKind kind, String res) {
        if (compilerSet == null) {
            return res;
        }
        Tool tool = compilerSet.getTool(kind);
        if (tool == null) {
            return res;
        }
        String name = tool.getName();
        if (name == null || name.isEmpty()) {
            return res;
        }
        res = addIfNeeded(name, res);
        String path = tool.getPath();
        try {
            String canonicalPath = FileSystemProvider.getCanonicalPath(execEnv, path);
            if (canonicalPath != null) {
                name = CndPathUtilities.getBaseName(canonicalPath);
                if (name != null && !name.isEmpty()) {
                    res = addIfNeeded(name, res);
                }
            }
        } catch (IOException ex) {
        }
        return res; 
    }
    
    private static String addIfNeeded(String name, String res) {
        for(String s : res.split(SEPARATOR)) { 
            if (s.equals(name)) {
                return res;
            }
        }
        if (res.isEmpty()) {
            res = name;
        } else {
            res = name + SEPARATOR + res;
        }
        return res;
    }
    
    private static void addTool(Project project, PredefinedToolKind kind, Set<String> res) {
        if (project != null) {
            ProjectBridge projectBridge = new ProjectBridge(project);
            if (projectBridge.isValid()) {
                CompilerSet compilerSet = projectBridge.getCompilerSet();
                if (compilerSet != null) {
                    ExecutionEnvironment execEnv = null;
                    ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    if (provider != null && provider.gotDescriptor()) {
                        MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                        if (descriptor != null) {
                            MakeConfiguration activeConfiguration = descriptor.getActiveConfiguration();
                            if (activeConfiguration != null) {
                                execEnv = activeConfiguration.getDevelopmentHost().getExecutionEnvironment();
                            }
                        }
                    }
                    Tool tool = compilerSet.getTool(kind);
                    if (tool != null) {
                        String name = tool.getName();
                        if (name != null && !name.isEmpty()) {
                            if (name.endsWith(".exe")) { //NOI18N
                                name = name.substring(0,name.length()-4);
                            }
                            res.add(name);
                        }
                        if (execEnv != null) {
                            String path = tool.getPath();
                            try {
                                String canonicalPath = FileSystemProvider.getCanonicalPath(execEnv, path);
                                if (canonicalPath != null) {
                                    name = CndPathUtilities.getBaseName(canonicalPath);
                                    if (name != null && !name.isEmpty()) {
                                        if (name.endsWith(".exe")) { //NOI18N
                                            name = name.substring(0,name.length()-4);
                                        }
                                        res.add(name);
                                    }
                                }
                            } catch (IOException ex) {
                            }
                        }
                    }
                }
            }
        }
    }    
}

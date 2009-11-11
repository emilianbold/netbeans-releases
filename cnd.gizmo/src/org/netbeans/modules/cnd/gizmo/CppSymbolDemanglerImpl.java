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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory.CPPCompiler;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 * @author mt154047
 * @author Alexey Vladykin
 */
public class CppSymbolDemanglerImpl implements CppSymbolDemangler {

    private static final String ECHO = "echo"; // NOI18N
    private static final String DEM = "dem"; // NOI18N
    private static final String GCPPFILT = "gc++filt"; // NOI18N
    private static final String CPPFILT = "c++filt"; // NOI18N
    private static final String EQUALS_EQUALS = " == "; // NOI18N

    private static final int MAX_CMDLINE_LENGTH = 2000;

    private final static Map<String, String> demangledCache = new HashMap<String, String>();
    private final static List<String> searchPaths = new ArrayList<String>();
    private final ExecutionEnvironment env;
    private final CPPCompiler cppCompiler;
    private String demanglerTool;
    private boolean demanglerChecked;

    /*package*/ CppSymbolDemanglerImpl(Map<String, String> serviceInfo) {
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
                cppCompiler = CPPCompiler.GNU;
                demanglerTool = GCPPFILT;
                env = ExecutionEnvironmentFactory.getLocal();
                return;
            }

            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();

            if (compilerSet.getCompilerFlavor().isGnuCompiler()) {
                cppCompiler = CPPCompiler.GNU;
                demanglerTool = GCPPFILT;
            } else {
                cppCompiler = CPPCompiler.SS;
                demanglerTool = DEM;
            }

            String binDir = compilerSet.getDirectory();
            if (!searchPaths.contains(binDir)) {
                searchPaths.add(binDir);
            }

            env = conf.getDevelopmentHost().getExecutionEnvironment();
        } else {
            env = ExecutionEnvironmentFactory.fromUniqueID(serviceInfo.get(ServiceInfoDataStorage.EXECUTION_ENV_KEY));
            cppCompiler = CppSymbolDemanglerFactory.CPPCompiler.valueOf(serviceInfo.get(GizmoServiceInfo.CPP_COMPILER));

            String binDir = serviceInfo.get(GizmoServiceInfo.CPP_COMPILER_BIN_PATH);
            if (!searchPaths.contains(binDir)) {
                searchPaths.add(binDir);
            }

            if (cppCompiler == CPPCompiler.GNU) {
                demanglerTool = GCPPFILT;
            } else {
                demanglerTool = DEM;
            }
        }
    }

    /*package*/ CppSymbolDemanglerImpl(CPPCompiler cppCompiler) {
        this.cppCompiler = cppCompiler;
        if (cppCompiler == CPPCompiler.GNU) {
            demanglerTool = GCPPFILT;
        } else {
            demanglerTool = DEM;
        }
        env = ExecutionEnvironmentFactory.getLocal();
    }

    public String demangle(String symbolName) {
        String mangledName = stripModuleAndOffset(symbolName);

        if (!isToolAvailable() || !isMangled(mangledName)) {
            return mangledName;
        }

        String demangledName = null;

        synchronized (demangledCache) {
            demangledName = demangledCache.get(mangledName);
        }

        if (demangledName == null) {
            List<String> list = Arrays.asList(mangledName);
            demangleImpl(list);
            demangledName = list.get(0);
            synchronized (demangledCache) {
                demangledCache.put(mangledName, demangledName);
            }
        }

        return demangledName;
    }

    public List<String> demangle(List<String> symbolNames) {
        List<String> result = new ArrayList<String>(symbolNames.size());

        for (String name : symbolNames) {
            result.add(stripModuleAndOffset(name));
        }

        if (!isToolAvailable()) {
            return result;
        }

        List<String> missedNames = new ArrayList<String>();
        List<Integer> missedIdxs = new ArrayList<Integer>();

        synchronized (demangledCache) {
            for (int i = 0; i < result.size(); ++i) {
                String mangledName = result.get(i);
                if (isMangled(mangledName)) {
                    String demangledName = demangledCache.get(mangledName);
                    if (demangledName == null) {
                        missedNames.add(mangledName);
                        missedIdxs.add(i);
                    } else {
                        result.set(i, demangledName);
                    }
                }
            }
        }

        if (!missedNames.isEmpty()) {
            splitAndDemangle(missedNames);
            synchronized (demangledCache) {
                for (int i = 0; i < missedNames.size(); ++i) {
                    int idx = missedIdxs.get(i);
                    String mangledName = result.get(idx);
                    String demangledName = missedNames.get(i);
                    demangledCache.put(mangledName, demangledName);
                    result.set(idx, demangledName);
                }
            }
        }

        return result;
    }

    private boolean isMangled(String name) {
        // aggressive optimization, but invoking dozens of processes
        // on remote machine is not very fast
        return 0 < name.length() && name.charAt(0) == '_' || 0 <= name.indexOf("__"); // NOI18N
    }

    private static String stripModuleAndOffset(String functionName) {
        int plusPos = functionName.indexOf('+'); // NOI18N
        if (0 <= plusPos) {
            functionName = functionName.substring(0, plusPos);
        }
        int tickPos = functionName.indexOf('`'); // NOI18N
        if (0 <= tickPos) {
            functionName = functionName.substring(tickPos + 1);
        }
        return functionName;
    }

    /**
     * Splits mangled names list into chunks to avoid command line overflow.
     * Invokes {@link #demangleImpl(List)} for each chunk.
     *
     * @param mangledNames
     */
    private void splitAndDemangle(List<String> mangledNames) {

        if (demanglerTool == null) {
            // demangler not found
            return;
        }

        ListIterator<String> it = mangledNames.listIterator();
        while (it.hasNext()) {

            int startIdx = it.nextIndex();
            int cmdlineLength = ECHO.length() + demanglerTool.length();
            while (it.hasNext() && cmdlineLength < MAX_CMDLINE_LENGTH) {
                String name = it.next();
                cmdlineLength += name.length() + 3; // space and quotes
            }
            int endIdx = it.nextIndex();

            List<String> mangledNamesSublist = mangledNames.subList(startIdx, endIdx);
            demangleImpl(mangledNamesSublist);
        }
    }

    private void demangleImpl(List<String> mangledNames) {
        checkDemanglerIfNeeded();

        if (demanglerTool == null) {
            // demangler not found
            return;
        }

        final NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);

        if (GCPPFILT.equals(demanglerTool) || CPPFILT.equals(demanglerTool)) {
            StringBuilder cmdline = new StringBuilder();
            cmdline.append(ECHO).append(" \""); // NOI18N
            for (String name : mangledNames) {
                cmdline.append(name).append('\n'); // NOI18N
            }
            cmdline.append("\" | ").append(demanglerTool); // NOI18N
            npb.setCommandLine(cmdline.toString());
        } else {
            npb.setExecutable(demanglerTool);
            npb.setArguments(mangledNames.toArray(new String[mangledNames.size()]));
        }

        try {
            Future<Process> task = DLightExecutorService.submit(npb, "CPPSymbolDemangler call");//NOI18N
            NativeProcess np = (NativeProcess) task.get();
            BufferedReader reader = new BufferedReader(new InputStreamReader(np.getInputStream()));
            try {
                ListIterator<String> it = mangledNames.listIterator();
                while (it.hasNext()) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }

                    it.next();
                    if (cppCompiler == CPPCompiler.SS) {
                        int eqPos = line.indexOf(EQUALS_EQUALS);
                        if (0 <= eqPos) {
                            line = line.substring(eqPos + EQUALS_EQUALS.length());
                        }
                    }
                    it.set(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            // hide it
        } catch (InterruptedException e) {
        } catch (ExecutionException execException) {
        }
    }

    private synchronized void checkDemanglerIfNeeded() {
        if (!demanglerChecked) {
            String absPath = HostInfoUtils.searchFile(env, searchPaths, demanglerTool, true);
            if (absPath == null) {
                String fallbackDemangler = CPPFILT;
                absPath = HostInfoUtils.searchFile(env, searchPaths, fallbackDemangler, true);
                if (absPath == null) {
                    demanglerTool = null;
                } else {
                    demanglerTool = absPath;
                }
            } else {
                demanglerTool = absPath;
            }
            demanglerChecked = true;
        }
    }

    /**
     * Discard caches. For unit tests.
     */
    /*package*/ void clearCache() {
        synchronized (demangledCache) {
            demangledCache.clear();
        }
    }

    /**
     * Checks if native demangler tool is available.
     * We can't work without this tool!
     *
     * @return <code>true</code> if tool is available and this demangler
     *      is functional, <code>false</code> otherwise
     */
    /*package*/ boolean isToolAvailable() {
        checkDemanglerIfNeeded();
        return demanglerTool != null;
    }
}

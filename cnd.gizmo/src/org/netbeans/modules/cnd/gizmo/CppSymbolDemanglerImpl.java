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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory.CPPCompiler;
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

    private static final int MAX_CMDLINE_LENGTH = 2000;
    private final static Map<String, String> demangledCache = new HashMap<String, String>();
    private final ExecutionEnvironment env;
    private final CPPCompiler cppCompiler;
    private String demanglerTool;
    private boolean checkGnuDemangler;
    private static final String GNU_DEMANGLER_1 = "gc++filt"; //NOI18N
    private static final String GNU_DEMANGLER_2 = "c++filt"; //NOI18N
    private static final String SS_DEMANGLER = "dem"; //NOI18N
    private static final String EQUALS_EQUALS = " == "; //NOI18N

    /*package*/ CppSymbolDemanglerImpl() {
        Project project = org.netbeans.api.project.ui.OpenProjects.getDefault().getMainProject();
        if (project == null) {
             Project[] projects = org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
             if (projects.length == 1) {
                 project = projects[0];
             }
        }
        NativeProject nPrj = (project == null) ? null : project.getLookup().lookup(NativeProject.class);
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
        if (nPrj == null || conf == null) {
            cppCompiler = CPPCompiler.GNU;
            demanglerTool = GNU_DEMANGLER_1;
            checkGnuDemangler = true;
            env = ExecutionEnvironmentFactory.getLocal();
            return;
        }
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String demangle_utility = SS_DEMANGLER;
        if (compilerSet.getCompilerFlavor().isGnuCompiler()) {
            cppCompiler = CPPCompiler.GNU;
            demangle_utility = GNU_DEMANGLER_1;
            checkGnuDemangler = true;
        } else {
            cppCompiler = CPPCompiler.SS;
        }
        String binDir = compilerSet.getDirectory();
        ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        if (execEnv.isRemote()) {
            env = ExecutionEnvironmentFactory.createNew(execEnv.getUser(), execEnv.getHost());
        } else {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        demanglerTool = binDir + "/" + demangle_utility; //NOI18N BTW: isn't it better to use File.Separator?
    }

    /*package*/ CppSymbolDemanglerImpl(CPPCompiler cppCompiler) {
        this.cppCompiler = cppCompiler;
        if (cppCompiler == CPPCompiler.GNU) {
            demanglerTool = GNU_DEMANGLER_1;
            checkGnuDemangler = true;
        } else {
            demanglerTool = SS_DEMANGLER;
        }
        env = ExecutionEnvironmentFactory.getLocal();
    }

    public String demangle(String symbolName) {
        String mangledName = stripModuleAndOffset(symbolName);

        if (!isMangled(mangledName)) {
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
        ListIterator<String> it = mangledNames.listIterator();
        while (it.hasNext()) {

            int startIdx = it.nextIndex();
            int cmdlineLength = demanglerTool.length();
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

        if (cppCompiler == CPPCompiler.GNU) {
            checkGnuDemangler();
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable(demanglerTool);
        npb = npb.setArguments(mangledNames.toArray(new String[mangledNames.size()]));

        try {
            NativeProcess np = npb.call();
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
        }
    }

    private synchronized void checkGnuDemangler() {
        if (checkGnuDemangler) {
            if (HostInfoUtils.searchFile(env, Collections.<String>emptyList(), demanglerTool, true) == null) {
                String demanglerTool2 = demanglerTool.replace(GNU_DEMANGLER_1, GNU_DEMANGLER_2);
                if (HostInfoUtils.searchFile(env, Collections.<String>emptyList(), demanglerTool2, true) != null) {
                    demanglerTool = demanglerTool2;
                }
            }
            checkGnuDemangler = false;
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
        if (cppCompiler == CPPCompiler.GNU) {
            checkGnuDemangler();
        }
        return HostInfoUtils.searchFile(env, Collections.<String>emptyList(), demanglerTool, true) != null;
    }
}

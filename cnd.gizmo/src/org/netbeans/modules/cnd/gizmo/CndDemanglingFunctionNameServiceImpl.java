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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.spi.DemanglingFunctionNameService;
import org.netbeans.modules.dlight.spi.DemanglingFunctionNameServiceFactory.CPPCompiler;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 * @author mt154047
 * @author Alexey Vladykin
 */
public class CndDemanglingFunctionNameServiceImpl implements DemanglingFunctionNameService {

    private static final int MAX_CMDLINE_LENGTH = 2000;
    private final static Map<String, String> demangledCache = new HashMap<String, String>();
    private final ExecutionEnvironment env;
    private final CPPCompiler cppCompiler;
    private final String demanglerTool;
    private static final String GNU_FAMILIY = "gc++filt"; //NOI18N
    private static final String SS_FAMILIY = "dem"; //NOI18N
    private static final String EQUALS_EQUALS = " == "; //NOI18N

    CndDemanglingFunctionNameServiceImpl() {
        Project project = org.netbeans.api.project.ui.OpenProjects.getDefault().getMainProject();
        NativeProject nPrj = (project == null) ? null : project.getLookup().lookup(NativeProject.class);
        if (nPrj == null) {
            cppCompiler = CPPCompiler.GNU;
            demanglerTool = GNU_FAMILIY;
            env = ExecutionEnvironmentFactory.getLocal();
            return;
        }
        MakeConfiguration conf = (MakeConfiguration) ConfigurationSupport.getProjectDescriptor(project).getConfs().getActive();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String demangle_utility = SS_FAMILIY;
        if (compilerSet.getCompilerFlavor().isGnuCompiler()) {
            cppCompiler = CPPCompiler.GNU;
            demangle_utility = GNU_FAMILIY;
        } else {
            cppCompiler = CPPCompiler.SS;
        }
        String binDir = compilerSet.getDirectory();
        //String baseDir = new File(binDir).getParent();
        ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        if (execEnv.isRemote()) {
            env = ExecutionEnvironmentFactory.createNew(execEnv.getUser(), execEnv.getHost());
        } else {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        demanglerTool = binDir + "/" + demangle_utility; //NOI18N BTW: isn't it better to use File.Separator?
    }

    CndDemanglingFunctionNameServiceImpl(CPPCompiler cppCompiler) {
        this.cppCompiler = cppCompiler;
        if (cppCompiler == CPPCompiler.GNU) {
            demanglerTool = GNU_FAMILIY;
        } else {
            demanglerTool = SS_FAMILIY;
        }
        env = ExecutionEnvironmentFactory.getLocal();
    }

    public Future<String> demangle(String functionName) {
        return DLightExecutorService.submit(
                new Demangler(functionName),
                "Demangling function " + functionName); // NOI18N
    }

    public Future<List<String>> demangle(List<String> functionNames) {
        return DLightExecutorService.submit(
                new BatchDemangler(functionNames),
                "Batch demangling " + functionNames.size() + " functions"); // NOI18N
    }

    private static String demangleDtrace(String functionName) {
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

    private void demangleImpl(List<String> mangledNames) {
        NativeProcessBuilder npb = new NativeProcessBuilder(env, demanglerTool);
        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL).outLineBased(true);

        final List<String> demangledNames = new ArrayList<String>();

        ListIterator<String> it = mangledNames.listIterator();
        while (it.hasNext()) {

            int startIdx = it.nextIndex();
            int cmdlineLength = demanglerTool.length();
            while (it.hasNext() && cmdlineLength < MAX_CMDLINE_LENGTH) {
                String name = it.next();
                cmdlineLength += name.length() + 3; // space and quotes
                it.set(name);
            }
            int endIdx = it.nextIndex();

            List<String> mangledNamesSublist = mangledNames.subList(startIdx, endIdx);
            npb = npb.setArguments(mangledNamesSublist.toArray(new String[mangledNamesSublist.size()]));
            descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.bridge(new DemanglerLineProcessor(demangledNames));
                }
            });

            ExecutionService execService = ExecutionService.newService(
                    npb, descriptor, "Batch demangling"); // NOI18N
            Future<Integer> res = execService.run();
            try {
                res.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (mangledNamesSublist.size() == demangledNames.size()) {
                for (int i = 0; i < mangledNamesSublist.size(); ++i) {
                    mangledNamesSublist.set(i, demangledNames.get(i));
                }
            }
            demangledNames.clear();
        }
    }

    private boolean isMangled(String name) {
        // aggressive optimization, but invoking dozens of processes
        // on remote machine is not very fast
        return 0 < name.length() && name.charAt(0) == '_' || 0 <= name.indexOf("__"); // NOI18N
    }

    private class Demangler implements Callable<String> {

        private final String nameToDemangle;

        public Demangler(String nameToDemangle) {
            this.nameToDemangle = nameToDemangle;
        }

        public String call() {
            String mangledName = demangleDtrace(nameToDemangle);

            if (!isMangled(mangledName)) {
                return mangledName;
            }

            String demangledName = null;

            synchronized (demangledCache) {
                demangledName = demangledCache.get(mangledName);
            }

            if (demangledName == null) {
                List<String> list = Collections.singletonList(mangledName);
                demangleImpl(list);
                demangledName = list.get(0);
                synchronized (demangledCache) {
                    demangledCache.put(mangledName, demangledName);
                }
            }

            return demangledName;
        }
    }

    private class BatchDemangler implements Callable<List<String>> {

        private final List<String> namesToDemangle;

        public BatchDemangler(List<String> namesToDemangle) {
            this.namesToDemangle = namesToDemangle;
        }

        public List<String> call() {
            List<String> result = new ArrayList<String>(namesToDemangle.size());
            for (String name : namesToDemangle) {
                result.add(demangleDtrace(name));
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
                demangleImpl(missedNames);
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
    }

    private class DemanglerLineProcessor implements LineProcessor {

        private final List<String> output;

        public DemanglerLineProcessor(List<String> output) {
            this.output = output;
        }

        @Override
        public void processLine(String line) {
            if (0 < line.length()) {
                if (cppCompiler == CPPCompiler.SS) {
                    int eqPos = line.indexOf(EQUALS_EQUALS);
                    if (0 <= eqPos) {
                        line = line.substring(eqPos + EQUALS_EQUALS.length());
                    }
                }
                output.add(line);
            }
        }

        public void reset() {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void close() {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

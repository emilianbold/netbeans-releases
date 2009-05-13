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

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
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
 *
 * @author mt154047
 */
public class CndDemanglingFunctionNameServiceImpl implements DemanglingFunctionNameService {

    private final static Map<CharSequence, CharSequence> demangled_functions = new HashMap<CharSequence, CharSequence>();
    private final ExecutionEnvironment env;
    private final CPPCompiler cppCompiler;
    private final String dem_util_path;
    private static final String GNU_FAMILIY = "gc++filt"; //NOI18N
    private static final String SS_FAMILIY = "dem"; //NOI18N
    private static final String EQUALS_EQUALS = "=="; //NOI18N

    CndDemanglingFunctionNameServiceImpl() {
        Project project = org.netbeans.api.project.ui.OpenProjects.getDefault().getMainProject();
        NativeProject nPrj = (project == null) ? null : project.getLookup().lookup(NativeProject.class);
        if (nPrj == null) {
            cppCompiler = CPPCompiler.GNU;
            dem_util_path = GNU_FAMILIY;
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
        dem_util_path = binDir + "/" + demangle_utility; //NOI18N BTW: isn't it better to use File.Separator?
    }

    CndDemanglingFunctionNameServiceImpl(CPPCompiler cppCompiler) {
        this.cppCompiler = cppCompiler;
        if (cppCompiler == CPPCompiler.GNU) {
            dem_util_path = GNU_FAMILIY;
        } else {
            dem_util_path = SS_FAMILIY;
        }
        env = ExecutionEnvironmentFactory.getLocal();
    }

    public Future<String> demangle(String functionName) {
        //get current Project
        int plusPos = functionName.indexOf('+'); // NOI18N
        if (0 <= plusPos) {
            functionName = functionName.substring(0, plusPos);
        }
        int tickPos = functionName.indexOf('`'); // NOI18N
        if (0 <= tickPos) {
            functionName = functionName.substring(tickPos + 1);
        }
        final String nameToDemangle = functionName;
        final CharSequence nameToDemangleSeq = nameToDemangle.subSequence(0, nameToDemangle.length());

        return DLightExecutorService.submit(new Callable<String>() {

            public String call() {
                if (demangled_functions.containsKey(nameToDemangleSeq)) {
                    return demangled_functions.get(nameToDemangleSeq).toString();
                }
                NativeProcessBuilder npb = new NativeProcessBuilder(env, dem_util_path + " " + nameToDemangle); //NOI18N
                ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(
                        InputOutput.NULL).outLineBased(true);
                StringWriter result = new StringWriter();
                descriptor = descriptor.outProcessorFactory(new InputRedirectorFactory(result));
                ExecutionService execService = ExecutionService.newService(
                        npb, descriptor, "Demangling function " + nameToDemangle); // NOI18N
                Future<Integer> res = execService.run();
                try {
                    res.get();
                    String demangled_name = result.toString();
                    if (demangled_name != null && demangled_name.endsWith("\n")){//NOI18N
                         demangled_name = demangled_name.substring(0, demangled_name.length() -1);
                    }
                    if (cppCompiler == CPPCompiler.SS){
                        if (demangled_name != null && demangled_name.indexOf(EQUALS_EQUALS) != -1){
                            demangled_name = demangled_name.substring(demangled_name.indexOf(EQUALS_EQUALS) + 2);
                            demangled_name = demangled_name.trim();
                        }
                    }                    
                    demangled_functions.put(nameToDemangleSeq,
                            demangled_name.subSequence(0, demangled_name.length()));
                    return demangled_name;
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
                demangled_functions.put(nameToDemangleSeq, nameToDemangleSeq);
                return nameToDemangle;
            }
        }, "Demangle function " + nameToDemangle); // NOI18N
    }

    private static class InputRedirectorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private final Writer writer;

        public InputRedirectorFactory(Writer writer) {
            this.writer = writer;
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.copying(writer);
        }
    }
}

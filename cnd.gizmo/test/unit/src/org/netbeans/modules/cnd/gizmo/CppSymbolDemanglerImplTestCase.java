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
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory.CPPCompiler;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.test.Ifdef;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.openide.util.Utilities;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class CppSymbolDemanglerImplTestCase extends NativeExecutionBaseTestCase {

    private static final List<String> UNMANGLED_NAMES = Arrays.asList(
            "_start",
            "printf",
            "foo",
            "a");

    private static final List<String> GNU_MANGLED_NAMES = Arrays.asList(
            "_Z10Mandelbrotv", "Mandelbrot()",
            "_Z3absR7complex", "abs(complex&)",
            "_Z8wallTimev", "wallTime()",
            "_ZN7complexaSERKS_", "complex::operator=(complex const&)",
            "_ZN7complexC1Eee", "complex::complex(long double, long double)",
            "_ZN7complexmlERKS_", "complex::operator*(complex const&)",
            "_ZN7complexplERKS_", "complex::operator+(complex const&)");

    private static final List<String> SUN_MANGLED_NAMES = Arrays.asList(
            "__1cDabs6FrnHcomplex__d_", "double abs(complex&)",
            "__1cIwallTime6F_d_", "double wallTime()",
            "__1cKMandelbrot6F_v_", "void Mandelbrot()");

    private static final String LOCAL_COMPILER_SETS = "localhost.compilerSets";

    public CppSymbolDemanglerImplTestCase(String name) {
        super(name);
    }

    public CppSymbolDemanglerImplTestCase(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    @Ifdef(section = LOCAL_COMPILER_SETS, key = "GNU")
    public void testDemangleGnu() throws Exception {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(
                ExecutionEnvironmentFactory.getLocal(), CPPCompiler.GNU,
                NativeExecutionTestSupport.getRcFile().get(LOCAL_COMPILER_SETS, "GNU"));
        doTestDemangle(d, GNU_MANGLED_NAMES, true);
        doTestDemangle(d, GNU_MANGLED_NAMES, false);
    }

    @Ifdef(section = LOCAL_COMPILER_SETS, key = "GNU")
    public void testBatchDemangleGnu() throws Exception {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(
                ExecutionEnvironmentFactory.getLocal(), CPPCompiler.GNU,
                NativeExecutionTestSupport.getRcFile().get(LOCAL_COMPILER_SETS, "GNU"));
        doTestBatchDemangle(d, GNU_MANGLED_NAMES, true);
        doTestBatchDemangle(d, GNU_MANGLED_NAMES, false);
    }

    @Ifdef(section = LOCAL_COMPILER_SETS, key = "SunStudio")
    public void testDemangleSun() throws Exception {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(
                ExecutionEnvironmentFactory.getLocal(), CPPCompiler.SS,
                NativeExecutionTestSupport.getRcFile().get(LOCAL_COMPILER_SETS, "SunStudio"));
        doTestDemangle(d, SUN_MANGLED_NAMES, true);
        doTestDemangle(d, SUN_MANGLED_NAMES, false);
    }

    @Ifdef(section = LOCAL_COMPILER_SETS, key = "SunStudio")
    public void testBatchDemangleSun() throws Exception {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(
                ExecutionEnvironmentFactory.getLocal(), CPPCompiler.SS,
                NativeExecutionTestSupport.getRcFile().get(LOCAL_COMPILER_SETS, "SunStudio"));
        doTestBatchDemangle(d, SUN_MANGLED_NAMES, true);
        doTestBatchDemangle(d, SUN_MANGLED_NAMES, false);
    }

    private void addMangled(List<String> list, List<String> toAdd) {
        for (int i = 0; i < toAdd.size(); i += 2) {
            String sym = toAdd.get(i);
            if (Utilities.isMac() || Utilities.isWindows()) {
                sym = "_" + sym;
            }
            list.add(sym);
            list.add(toAdd.get(i + 1));
        }
    }

    private void addUnmangled(List<String> list) {
        for (String sym : UNMANGLED_NAMES) {
            list.add(sym);
            list.add(sym);
        }
    }

    private void doTestDemangle(CppSymbolDemanglerImpl d, List<String> data, boolean dtrace) {
        d.clearCache();
        List<String> list = new ArrayList<String>();
        addMangled(list, data);
        addUnmangled(list);
        for (int i = 0; i < list.size(); i += 2) {
            String sym = list.get(i);
            if (dtrace) {
                sym = dtrace(sym);
            }
            assertEquals(list.get(i + 1), d.demangle(sym));
        }
    }

    private void doTestBatchDemangle(CppSymbolDemanglerImpl d, List<String> data, boolean dtrace) {
        d.clearCache();
        List<String> list = new ArrayList<String>();
        addMangled(list, data);
        addUnmangled(list);
        List<String> input = prepareInput(list, dtrace);
        List<String> output = prepareOutput(list, true);
        assertEquals(output, d.demangle(input));
    }

    private String dtrace(String sym) {
        return "module`" + sym + "+0xdeadbeef";
    }

    private List<String> prepareInput(List<String> data, boolean dtrace) {
        List<String> input = new ArrayList<String>(data.size() / 2);
        for (int i = 0; i < data.size(); i += 2) {
            String sym = data.get(i);
            if (dtrace) {
                sym = dtrace(sym);
            }
            input.add(sym);
        }
        return input;
    }

    private List<String> prepareOutput(List<String> data, boolean isToolAvailable) {
        List<String> output = new ArrayList<String>(data.size() / 2);
        for (int i = isToolAvailable? 1 : 0; i < data.size(); i += 2) {
            output.add(data.get(i));
        }
        return output;
    }
}

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
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory.CPPCompiler;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class CppSymbolDemanglerImplTest extends CndBaseTestCase {

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

    public CppSymbolDemanglerImplTest(String name) {
        super(name);
    }

    public void testDemangleGnu() {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(CPPCompiler.GNU);
        doTestDemangle(d, GNU_MANGLED_NAMES, true);
        doTestDemangle(d, GNU_MANGLED_NAMES, false);
    }

    public void testBatchDemangleGnu() {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(CPPCompiler.GNU);
        doTestBatchDemangle(d, GNU_MANGLED_NAMES, true);
        doTestBatchDemangle(d, GNU_MANGLED_NAMES, false);
    }

    public void testDemangleSun() {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(CPPCompiler.SS);
        doTestDemangle(d, SUN_MANGLED_NAMES, true);
        doTestDemangle(d, SUN_MANGLED_NAMES, false);
    }

    public void testBatchDemangleSun() {
        CppSymbolDemanglerImpl d = new CppSymbolDemanglerImpl(CPPCompiler.SS);
        doTestBatchDemangle(d, SUN_MANGLED_NAMES, true);
        doTestBatchDemangle(d, SUN_MANGLED_NAMES, false);
    }

    private List<String> addUnmangled(List<String> data) {
        List<String> result = new ArrayList<String>(data);
        for (String sym : UNMANGLED_NAMES) {
            result.add(sym);
            result.add(sym);
        }
        return result;
    }

    private void doTestDemangle(CppSymbolDemanglerImpl d, List<String> data, boolean dtrace) {
        d.clearCache();
        data = addUnmangled(data);
        if (d.isToolAvailable()) {
            for (int i = 0; i < data.size(); i += 2) {
                String sym = data.get(i);
                if (dtrace) {
                    sym = dtrace(sym);
                }
                assertEquals(data.get(i + 1), d.demangle(sym));
            }
        } else if (dtrace) {
            warn("Demangler tool is not available. Running minimal check (dtrace=true).");
            for (int i = 0; i < data.size(); i += 2) {
                String sym = data.get(i);
                assertEquals(sym, d.demangle(dtrace(sym)));
            }
        } else {
            warn("Demangler tool is not available. Skipping test (dtrace=false).");
        }
    }

    private void doTestBatchDemangle(CppSymbolDemanglerImpl d, List<String> data, boolean dtrace) {
        d.clearCache();
        data = addUnmangled(data);
        if (d.isToolAvailable()) {
            List<String> input = prepareInput(data, dtrace);
            List<String> output = prepareOutput(data, true);
            assertEquals(output, d.demangle(input));
        } else if (dtrace) {
            warn("Demangler tool is not available. Running minimal check (dtrace=true).");
            List<String> input = prepareInput(data, true);
            List<String> output = prepareOutput(data, false);
            assertEquals(output, d.demangle(input));
        } else {
            warn("Demangler tool is not available. Skipping test (dtrace=false).");
        }
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

    private void warn(String msg) {
        System.err.println(getName() + ": " + msg);
    }
}

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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 * Just a continuation of the FileModelTest
 * (which became too large)
 * @author Vladimir Kvashin
 */
public class FileModel2Test extends TraceModelTestBase {

    public FileModel2Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    public void testIZ164583() throws Exception {
        // IZ#164583: Inaccuracy tests: unstable results in MySQL
        performTest("iz164583.cpp");
    }

    public void testIZ149525() throws Exception {
        // IZ#149525: can't process lazy body of macro expanded function
        performTest("iz149525.cc");
    }
    
    public void testIZ162280() throws Exception {
        // IZ#162280: Inaccuracy tests: regression in Boost and Vlc
        performTest("iz162280_friend_fwd_cls.cpp");
    }
    
    public void testIZ156061() throws Exception {
        // IZ156061: structure typedefs are highlighted as error
        performTest("iz156061.cc");
    }
    
    public void testIZ154276() throws Exception {
        // IZ154276: functions are creted instead of fields
        performTest("iz154276.cc");
    }

    public void testIZ154196() throws Exception {
        // IZ154196: Regression in LiteSQL (Error Highlighting)
        performTest("iz154196.cc");
    }

    public void testIZ136887() throws Exception {
        // IZ136887: Model do not support bit fields
        performTest("iz136887.cc");
    }

    public void testIZ149505() throws Exception {
        // IZ#149505: special handling of __VA_ARGS__ with preceding comma
        performTest("iz149505.cc");
    }

    public void testIZ145280() throws Exception {
        // IZ#145280: IDE highlights code with '__attribute__((unused))' as wrong
        performTest("iz145280.cc");
    }

    public void testIZ143977_0() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_0.cc");
    }

    public void testIZ143977_1() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_1.cc");
    }

    public void testIZ143977_2() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_2.cc");
    }

    public void testIZ143977_3() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_3.cc");
    }

    public void testIZ103462_1() throws Exception {
        // IZ#103462: Errors in template typedef processing:   'first' and 'second' are missed in Code Completion listbox
        performTest("iz103462_first_and_second_1.cc");
    }

    public void testHeaderWithCKeywords() throws Exception {
        // IZ#144403: restrict keywords are flagged as ERRORs in C header files
        performTest("testHeaderWithCKeywords.c");
    }

    public void testNamesakes() throws Exception {
        // IZ#145553 Class in the same namespace should have priority over a global one
        performTest("iz_145553_namesakes.cc");
    }

    public void testIZ146560() throws Exception {
        // IZ#146560 Internal C++ compiler does not accept 'struct' after 'new'
        performTest("iz146560.cc");
    }

    public void testIZ147284isDefined() throws Exception {
        // IZ#147284 APTMacroCallback.isDefined(CharSequence) ignores #undef
        String base = "iz147284_is_defined";
        performTest(base + ".cc");
        FileImpl fileImpl = findFile(base + ".h");
        assertNotNull(fileImpl);
        Collection<APTPreprocHandler> handlers = fileImpl.getPreprocHandlers();
        assertEquals(handlers.size(), 1);
        String macro = "MAC";
        assertFalse(macro + " should be undefined!", handlers.iterator().next().getMacroMap().isDefined(macro));
    }

    public void testIZ147574() throws Exception {
        // IZ#147574 Parser cann't recognize code in yy.tab.c file correctly
        performTest("iz147574.c");
    }

    public void testIZ148014() throws Exception {
        // IZ#148014 Unable to resolve pure virtual method that throws
        performTest("iz148014.cc");
    }

    public void testIZ149225() throws Exception {
        // IZ#149225 incorrect concatenation with token that starts with digit
        performTest("iz149225.c");
    }

    public void testIZ151621() throws Exception {
        // IZ#151621 no support for __thread keyword
        performTest("iz151621.c");
    }

    public void testInitializerInExpression() throws Exception {
        // IZ#152872: parser error in VLC on cast expression
        performTest("iz152872_initializer_in_expression.c");
    }

    public void testNamespaceAlias() throws Exception {
        // IZ#151957: 9 parser's errors in boost 1.36
        performTest("iz151957_namespace_alias.cc");
    }

    public void testIZ154349() throws Exception {
        // IZ#154349: wrongly flagged errors for destructor during template specialization
        performTest("iz154349.cc");
    }

    public void testIZ157603() throws Exception {
        // IZ#157603 : Code model does not understand __attribute, constructor, destructor keywords (GNU)
        performTest("iz157603.cc");
    }

    public void testIZ157836() throws Exception {
        // IZ#157836 : parser incorrectly handles expression in else without {}
        performTest("iz157836.cc");
    }

    public void testIZ156004() throws Exception {
        // IZ#156004 : Unexpected token = in variable declaration
        performTest("iz156004.cc");
    }

    public void testIZ159324() throws Exception {
        // IZ#159324 : Unresolved variable definition
        performTest("iz159324.cc");
    }

    public void testIZ158872() throws Exception {
        // IZ#158872 : inline keyword break code model for template definition
        performTest("iz158872.cc");
    }

    public void testIZ159238() throws Exception {
        // IZ#159238 : parser fails on attribute after friend
        performTest("iz159238.cc");
    }

    public void testIZ158124() throws Exception {
        // IZ#158124 : parser breaks on (( ))
        performTest("iz158124.cc");
    }

    public void testIZ156009() throws Exception {
        // IZ#156009 : parser fails on declaration with __attribute__
        performTest("iz156009.cc");
    }

    public void testIZ158615() throws Exception {
        // IZ#158615 : Intervals are unresolved
        performTest("iz158615.cc");
    }

    public void testIZ158684() throws Exception {
        // IZ#158684 : Invalid syntax error
        performTest("iz158684.cc");
    }

    public void testIZ134182() throws Exception {
        // IZ#134182 : missed const in function parameter
        performTest("iz134182.cc");
    }

    public void testIZ156696() throws Exception {
        // IZ#156696 : model miss extern property if declaration statement has two objects
        performTest("iz156696.cc");
    }

    public void testIZ142674() throws Exception {
        // IZ#142674 : Function-try-catch (C++) in editor shows error
        performTest("iz142674.cc");
    }

    public void testIZ165038() throws Exception {
        // IZ#165038 : parser fail on variable declaration
        performTest("iz165038.cc");
    }

    public void testIZ167547() throws Exception {
        // IZ#167547 : 100% CPU core usage with C++ project
        performTest("iz167547.cc");
    }
    
    public void testIZ166165() throws Exception {
        // IZ#166165 : Unresolved extern enum declaration
        performTest("iz166165.cc");
    }

    public void testIZ174256() throws Exception {
        // IZ#174256 : parser cant understand _Pragma operator
        performTest("iz174256.cc");
    }

    public void testIZ175324() throws Exception {
        // IZ#175324 : Bad code parsing
        performTest("iz175324.cc");
    }

    public void testIZ168253() throws Exception {
        // IZ#168253 : Unable to resolve identifier for some header files
        performTest("iz168253.cc");
    }

    public void testIZ175653() throws Exception {
        // IZ#175653 : Support for binary constants
        performTest("iz175653.cc");
    }

    public void testIZ176530() throws Exception {
        // IZ#176530 : Unresolved function parameters in function parameters
        performTest("iz176530.cc");
    }

    public void testIZ182510() throws Exception {
        // IZ#182510 : C comment block causes syntax coloring to lose sync

        File file = getDataFile("iz182510.cc");
        FileWriter writer = new FileWriter(file);
        try {
            // \r's are essential for this test, so write the test file here
            writer.write("//\\\r\n#define FOO 1\r\n#define BAR 2\r\n");
        } finally {
            writer.close();
        }

        // Test that trailing \\\r\n in line comment is interpreted as
        // single escaped newline, i.e. #define FOO is in comment.
        // Also test offsets of BAR definition.
        performTest("iz182510.cc");
    }
}

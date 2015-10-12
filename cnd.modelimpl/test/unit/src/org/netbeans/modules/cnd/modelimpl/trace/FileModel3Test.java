/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.trace;

import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;

/**
 * some tests extracted from sputnik/contrib/codemodeltests
 * @author Vladimir Voskresensky
 */
public class FileModel3Test extends TraceModelTestBase {

    public FileModel3Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
//        System.setProperty("apt.use.clank", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }
    
    public void test_const() throws Exception {
        performTest("const.cpp");
    }    

    public void test_cross_inclusionA() throws Exception {
        performTest("cross_inclusionA.h");
    }    

    public void test_cross_inclusionB() throws Exception {
        performTest("cross_inclusionB.h");
    }    
    
    public void test_delay() throws Exception {
        performTest("delay.c");
    }
    
    public void test_fun_macro_ellipsis() throws Exception {
        performTest("fun_macro_ellipsis.cc");
    }
    
    public void test_ifdef_guard() throws Exception {
        performTest("ifdef_guard.cpp");
    }
    
    public void test_init_array() throws Exception {
        performTest("init_array.cpp");
    }
    
    public void test_lexer_dollar() throws Exception {
        performTest("lexer_dollar.cc");
    }
    
    public void test_lexer_dots_at_eval() throws Exception {
        performTest("lexer_dots_at_eval.cc");
    }
    
    public void test_operators() throws Exception {
        performTest("operators.cc");
    }
    
    public void test_operators_h() throws Exception {
        performTest("operators.h");
    }
    
    public void test_ppExpression() throws Exception {
        performTest("ppExpression.cpp");
    }
    
    public void test_preproc() throws Exception {
        performTest("preproc.cpp");
    }
    
    public void test_preproc_concat() throws Exception {
        performTest("preproc_concat.cc");
    }
    
    public void test_preproc_concat_error() throws Exception {
        performTest("preproc_concat_error.cc");
    }
    
    public void test_preproc_endif() throws Exception {
        performTest("preproc_endif.cpp");
    }
    
    public void test_preproc_include_with_macro() throws Exception {
        performTest("preproc_include_with_macro.cpp");
    }
    
    public void test_preproc_incomplete_if_directives() throws Exception {
        performTest("preproc_incomplete_if_directives.cpp");
    }
    
    public void test_preproc_multi_line_macro_param() throws Exception {
        performTest("preproc_multi_line_macro_param.cpp");
    }

    public void test_recurse_define() throws Exception {
        performTest("recurse_define.h");
    }
    
    public void test_resolver_typedef_s_e_c_u() throws Exception {
        performTest("resolver_typedef_s_e_c_u.cc");
    }
    
    public void test_va_arg_using() throws Exception {
        performTest("va_arg_using.cpp");
    }
}

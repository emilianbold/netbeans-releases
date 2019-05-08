/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

/**
 * Just a continuation of the FileModelTest
 * (which became too large)
 */
public class FileModelCpp11Test extends TraceModelTestBase {

    public FileModelCpp11Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp11", "true"); 
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    @Override
    protected void postTest(String[] args, Object... params) throws Exception {
        System.setProperty("cnd.language.flavor.cpp11", "false"); 
    }
    
    public void testCpp11() throws Exception {
        performTest("cpp11.cpp");
    }

    public void testClassMemberFwdEnums() throws Exception {
        performTest("classMemberFwdEnum.cpp");
    }
    
    public void testClassMemberOperator() throws Exception {
        // #225102 - [73cat] virtual operator double() const override brokes the parser.
        performTest("bug225102.cpp");
    }
    
    public void testBug224666() throws Exception {
        // Bug 224666 - C++11: Error parsing when both "final" and "override" are present 
        performTest("bug224666.cpp");
    }
    
    public void test216084() throws Exception {
        // #216084 - Wrong editor underline of pure virtual class member - type conversion operator 
        performTest("bug216084.cpp");
    }

    public void test226562() throws Exception {
        // Bug 226562 - Template function dont recognise braces initializer
        performTest("bug226562.cpp");
    }    
    
    public void test236535() throws Exception {
        // Bug 236535 - Cannot parse c++11 deleted definitions for conversion operators
        performTest("bug236535.cpp");
    }    
    
    public void test240723() throws Exception {
        // Bug 236535 - Cannot parse c++11 deleted definitions for conversion operators
        performTest("bug240723_m.cpp");
    }        
    
    public void test241017() throws Exception {
        // Bug 241017 - False error on C++11 operator new 
        performTest("bug241017.cpp");
    }
    
    public void test242729() throws Exception {
        // Bug 242729 - Some c++11 features not recognized by code assistance engine
        performTest("bug242729.cpp");
    }    
    
    public void test243514() throws Exception {
        // Bug 243514 - inaccuracy tests (clang): unexpected token 'auto'
        // Bug 243550 - inaccuracy tests (clang): variadic template and auto 
        performTest("bug243514.cpp");
    }        
    
    public void testBug235968() throws Exception {
        // Bug 235968 - [74cat] Unexpected token 'virtual' in class destructor.
        performTest("bug235968.cpp");
    }        
    
    public void testBug243513() throws Exception {
        // Bug 243513 - inaccuracy tests (clang): trailing return types
        performTest("bug243513.cpp");
    }            
    
    public void testBug243515() throws Exception {
        // Bug 243515 - inaccuracy tests (clang): C++11 ref-qualifiers
        performTest("bug243515.cpp");
    }
    
    public void testBug243527() throws Exception {
        // Bug 243527 - inaccuracy tests (clang): complex test with C++11 ref-qualifiers
        performTest("bug243527.cpp");
    }    
    
    public void testBug243518() throws Exception {
        // Bug 243518 - inaccuracy tests (clang): function with spec returning function with spec
        performTest("bug243518.cpp");
    }    
    
    public void testBug243523() throws Exception {
        // Bug 243523 - inaccuracy tests (clang): C++11 ref-qualifiers as a arguments 
        performTest("bug243523.cpp");
    }    
    
    public void testBug243524() throws Exception {
        // Bug 243524 - inaccuracy tests (clang): lambda and throw
        performTest("bug243524.cpp");
    }        
    
    public void testBug243528() throws Exception {
        // Bug 243528 - inaccuracy tests (clang): constructor in template
        performTest("bug243528.cpp");
    }            
    
    public void testBug243522() throws Exception {
        // Bug 243522 - inaccuracy tests (clang): alignas
        performTest("bug243522.cpp");
    }                
    
    public void testBug243510() throws Exception {
        // Bug 243510 - inaccuracy tests (clang): constexpr keyword
        performTest("bug243510.cpp");
    }                    
    
    public void testBug243525() throws Exception {
        // Bug 243525 - inaccuracy tests (clang): digraphs
        performTest("bug243525.cpp");
    }    
    
    public void testBug225045() throws Exception {
        // Bug 225045 - C++11 "final" keyword improperly scoped
        performTest("bug225045.cpp");
    }    
    
    public void testBug243940() throws Exception {
        // Bug 243940 - inaccuracy tests (cpp11): forward declaration of enumerations 
        performTest("bug243940.cpp");
    }    
    
    public void testBug244199() throws Exception {
        // Bug 244199 - C++11 attributes are not supported
        performTest("bug244199.cpp");
    }
    
    public void testBug246170 () throws Exception {
        // Bug 246170 - Unexpected token in case of c++11 array initialization
        performTest("bug246170.cpp");
    }
    
    public void testBug246534() throws Exception {
        // 246534 - inaccuracy tests: regression in 5 projects
        performTest("bug246534.cpp");
    }
    
    public void testBug251214() throws Exception {
        // Bug 251214 - Implicit C++ uniform initialization among constructor arguments is marked as wrong
        performTest("bug251214.cpp");
    }
    
    public void testBug251329() throws Exception {
        // Bug 251329 - Unexpected token if final occurs after trailing type specifier
        performTest("bug251329.cpp");
    }
    
    public void testBug252513() throws Exception {
        // Bug 252513 - Broken parsing for lambdas with explicit capture list
        performTest("bug252513.cpp");
    }
    
    public void testBug255545() throws Exception {
        // Bug 255545 - decltype in initializer slightly breaks code model
        performTest("bug255545.cpp");
    }
    
    public void testBug254133() throws Exception {
        // Bug 254133 - C++11 enums with visibility makes netbeans think the syntax is incorrect
        performTest("bug254133.cpp");
    }
    
    public void testBug270231() throws Exception {
        // Bug 270231 - typeof keyword not recognized by C++ parser
        performTest("bug270231.cpp");
    }
}

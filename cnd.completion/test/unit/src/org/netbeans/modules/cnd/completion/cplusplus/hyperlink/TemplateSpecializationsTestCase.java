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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

/**
 *
 * @author Vladimir Kvashin
 */
public class TemplateSpecializationsTestCase extends HyperlinkBaseTestCase {

    public TemplateSpecializationsTestCase(String testName) {
        super(testName);
    }

    public void testFriendTemplateFun() throws Exception {
        // IZ#157359: IDE highlights protected field as wrong
        super.performTest("iz157359.cc", 15, 15, "iz157359.cc", 8, 3);
        super.performTest("iz157359.cc", 15, 25, "iz157359.cc", 8, 3);
    }

    public void testIZ143611_using_inherited_spec_field() throws Exception {
        performTest("iz143611_inherited_spec_field.cc", 21, 15, "iz143611_inherited_spec_field.cc", 7, 5); // param_t
        performTest("iz143611_inherited_spec_field.cc", 26, 15, "iz143611_inherited_spec_field.cc", 11, 5); // param_int
        performTest("iz143611_inherited_spec_field.cc", 31, 15, "iz143611_inherited_spec_field.cc", 15, 5); // param_char_int
    }
    
    public void testIZ144156_func_partial_spec_pointer() throws Exception {
        performTest("template_fun_spec.cc", 12, 33, "template_fun_spec.cc", 40, 1); // partial spec. for T*
        performTest("template_fun_spec.cc", 40, 33, "template_fun_spec.cc", 12, 1); // and back
    }
    
    public void testIZ144156_func_full_spec_char() throws Exception {
        performTest("template_fun_spec.cc", 18, 26, "template_fun_spec.cc", 50, 1); // full spec. for char
        performTest("template_fun_spec.cc", 50, 26, "template_fun_spec.cc", 18, 1); // and back
    }
    
    public void testIZ143977_Parm_in_Loki_0() throws Exception {
        performTest("iz143977_0.cc", 17, 43, "iz143977_0.cc", 7, 9);
        performTest("iz143977_0.cc", 18, 43, "iz143977_0.cc", 7, 9);
        performTest("iz143977_0.cc", 28, 42, "iz143977_0.cc", 6, 9);
    }
    
    public void testIZ143977_Parm_in_Loki_2() throws Exception {
        performTest("iz143977_2.cc", 9, 36, "iz143977_2.cc", 5, 9);
    }
    
    public void testIZ143977_Parm_in_Loki_3() throws Exception {
        performTest("iz143977_3.cc", 20, 36, "iz143977_3.cc", 8, 9);
        performTest("iz143977_3.cc", 21, 36, "iz143977_3.cc", 12, 9);
    }
    
    public void testIZ103462_1() throws Exception {
        // IZ#103462: Errors in template typedef processing:   'first' and 'second' are missed in Code Completion listbox
        performTest("iz103462_first_and_second_1.cc", 21, 16, "iz103462_first_and_second_1.cc", 3, 5);
    }

    public void testIZ160659() throws Exception {
        // IZ#160659 : Unresolved ids in case of specialization of templated class forward declaration
        performTest("iz160659.cc", 11, 45, "iz160659.cc", 7, 12);
        performTest("iz160659.cc", 25, 51, "iz160659.cc", 21, 16);
    }

    public void testIZ172227() throws Exception {
        // IZ#172227 : Unable to resolve identifier path although code compiles allright
        performTest("iz172227.cc", 14, 9, "iz172227.cc", 2, 5);
        performTest("iz172227.cc", 16, 10, "iz172227.cc", 5, 5);
    }

    public void testBug180828() throws Exception {
        // Bug 180828 : Highlighting bug
        performTest("bug180828.cpp", 7, 44, "bug180828.cpp", 4, 5);
    }

    public void testBug186388() throws Exception {
        // Bug 186388 - Unresolved ids in template specialization function definition
        performTest("bug186388.cpp", 14, 22, "bug186388.cpp", 9, 4);
    }

    public void testBug190668() throws Exception {
        // Bug 190668 - [code model] Lack of support for template specializations
        performTest("bug190668.cpp", 16, 6, "bug190668.cpp", 11, 5);
    }

    public void testBug187258() throws Exception {
        // Bug 187258 - code model does not find template specialization for unsigned type
        performTest("bug187258.cpp", 22, 50, "bug187258.cpp", 14, 5);
        performTest("bug187258.cpp", 21, 45, "bug187258.cpp", 6, 5);
    }

    public void testExplicitSpecializations() throws Exception {
        // Improving specializations
        performTest("explicit_specializations.cpp", 5, 11, "explicit_specializations.cpp", 8, 1);
//        performTest("explicit_specializations.cpp", 8, 69, "explicit_specializations.cpp", 5, 5);
        performTest("explicit_specializations.cpp", 15, 11, "explicit_specializations.cpp", 18, 1);
        performTest("explicit_specializations.cpp", 18, 64, "explicit_specializations.cpp", 15, 5);
        performTest("explicit_specializations.cpp", 22, 56, "explicit_specializations.cpp", 24, 1);
        performTest("explicit_specializations.cpp", 24, 56, "explicit_specializations.cpp", 22, 1);
        
        performTest("explicit_specializations.cpp", 25, 5, "explicit_specializations.cpp", 4, 5);

        performTest("explicit_specializations.cpp", 31, 9, "explicit_specializations.cpp", 24, 1);
        performTest("explicit_specializations.cpp", 34, 9, "explicit_specializations.cpp", 18, 1);
        performTest("explicit_specializations.cpp", 37, 9, "explicit_specializations.cpp", 8, 1);
    }

    public void testIZ144156_func_spec_main() throws Exception {
        performTest("template_fun_spec.cc", 9, 33, "template_fun_spec.cc", 35, 1); // base template
        performTest("template_fun_spec.cc", 35, 33, "template_fun_spec.cc", 9, 1); // and back
    }
    public void testIZ144156_func_partial_spec_pair() throws Exception {
        performTest("template_fun_spec.cc", 15, 33, "template_fun_spec.cc", 45, 1); // partial spec. for pair<T,T>
        performTest("template_fun_spec.cc", 45, 33, "template_fun_spec.cc", 15, 1); // and back
    }
    public void testIZ144156_func_full_spec_pair_char() throws Exception {
        performTest("template_fun_spec.cc", 21, 26, "template_fun_spec.cc", 55, 1); // full spec. for pair<char,char>
        performTest("template_fun_spec.cc", 55, 26, "template_fun_spec.cc", 21, 1); // and back
    }

    public void testBug185045() throws Exception {
        // Bug 185045 - [code model] Incorrect hyperlink with template specialization function
        performTest("bug185045.cpp", 12, 9, "bug185045.cpp", 7, 1);
    }

    public void testBug196157() throws Exception {
        // Bug 196157 - Template friend functions highlighting problems 
        performTest("bug196157.cpp", 15, 23, "bug196157.cpp", 10, 5);
    }

    public void testBug195283() throws Exception {
        // Bug 195283 - go to jumps to base template instead of specialization
        performTest("bug195283.cpp", 7, 7, "bug195283.cpp", 4, 1);
    }
    
    public void testBug209513() throws Exception {
        // Bug 209513 - a lot of renderer exceptions in log
        performTest("bug209513.cpp", 4, 37, "bug209513.cpp", 2, 9);
    }

    public void testBug210303() throws Exception {
        // Bug 210303 - Unresolved instantiation
        performTest("bug210303.cpp", 8, 7, "bug210303.cpp", 3, 5);
    }    
    
    public void testBug230585() throws Exception {
        // Bug 230585 - Wrong specialization in case of unnamed built-in type
        performTest("bug230585.cpp", 17, 16, "bug230585.cpp", 10, 9);
    }    
    
    public void testBug230589() throws Exception {
        // Bug 230589 - Wrong specialization when constant is used
        performTest("bug230589.cpp", 22, 25, "bug230589.cpp", 14, 9);
    }    
    
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return TemplateSpecializationsTestCase.class;
        }

        public void testExplicitSpecializations2() throws Exception {
            // Improving specializations
            performTest("explicit_specializations.cpp", 8, 69, "explicit_specializations.cpp", 5, 5);
        }

        public void testIZ143977_Parm_in_Loki_1() throws Exception {
            performTest("iz143977_1.cc", 45, 33, "iz143977_1.cc", 11, 9);
            performTest("iz143977_1.cc", 46, 33, "iz143977_1.cc", 12, 9);
        }

        public void testIZ143977_Parm_in_Loki_4() throws Exception {
            performTest("iz143977_3.cc", 22, 36, "iz143977_3.cc", 8, 9);
            performTest("iz143977_3.cc", 23, 36, "iz143977_3.cc", 12, 9);
        }

        public Failed(String testName) {
            super(testName, true);
        }
    }
    
}

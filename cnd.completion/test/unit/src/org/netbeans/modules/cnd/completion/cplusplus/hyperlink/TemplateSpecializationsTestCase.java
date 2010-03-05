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


    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return TemplateSpecializationsTestCase.class;
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

/**
 *
 * @author Vladimir Voskresensky
 */
public class BasicHyperlinkTestCase extends HyperlinkBaseTestCase {

    public BasicHyperlinkTestCase(String testName) {
        super(testName);
    }

    public void testIZ157907() throws Exception {
        // IZ#157907: False positive recognition of macro
        performTest("fun_macro_and_name.c", 6, 5, "fun_macro_and_name.c", 6, 3); // PREFIX as name of typedef
        performTest("fun_macro_and_name.c", 10, 10, "fun_macro_and_name.c", 6, 3); // PREFIX as name of typedef

        performTest("fun_macro_and_name.c", 1, 10, "fun_macro_and_name.c", 1, 1); // PREFIX as name of macro with params
        performTest("fun_macro_and_name.c", 8, 15, "fun_macro_and_name.c", 1, 1); // PREFIX as name of macro with params
    }

    public void testIZ151061() throws Exception {
        // IZ#151061: code model inaccuracy on VLC's is above boundary
        performTest("iz151061.c", 6, 10, "iz151061.c", 2, 5);
        performTest("iz151061.c", 7, 10, "iz151061.c", 2, 5);
        performTest("iz151061.c", 24, 20, "iz151061.c", 2, 5);

        performTest("iz151061.c", 17, 15, "iz151061.c", 13, 9);
        performTest("iz151061.c", 18, 15, "iz151061.c", 13, 9);
        performTest("iz151061.c", 22, 15, "iz151061.c", 13, 9);
    }

    public void testIZ146392() throws Exception {
        // IZ#146392: regression: some declaration statements are not rendered any more
        performTest("iz146392.cc", 4, 25, "iz146392.cc", 4, 22);
        performTest("iz146392.cc", 6, 15, "iz146392.cc", 4, 22);
    }

    public void testIZ139600() throws Exception {
        performTest("main.c", 35, 15, "main.c", 35, 5); // funPtr in int (*funPtr)();
    }

    public void testVarInFunWithInitalization() throws Exception {
        performTest("main.c", 19, 10, "main.c", 19, 5); // iiii in int iiii = fun(null, null);
    }

    public void testParamWithoutSpace() throws Exception {
        performTest("main.c", 18, 17, "main.c", 18, 10); // aaa in void foo(char* aaa, char**bbb)
        performTest("main.c", 18, 28, "main.c", 18, 21); // bbb in void foo(char* aaa, char**bbb)
    }

    public void testFileLocalVariable() throws Exception {
        performTest("main.c", 15, 12, "main.c", 15, 1); // VALUE in const int VALUE = 10;
        performTest("main.c", 16, 30, "main.c", 15, 1); // VALUE in const int VALUE_2 = 10 + VALUE;
        performTest("main.c", 16, 12, "main.c", 16, 1); // VALUE_2 in const int VALUE_2 = 10 + VALUE;
    }

    public void testFuncParamUsage() throws Exception {
        performTest("main.c", 3, 15, "main.c", 2, 9); // aa in 'int kk = aa + bb;'
        performTest("main.c", 3, 20, "main.c", 2, 17); // bb in 'int kk = aa + bb;'
    }

    public void testFuncUsage() throws Exception {
        performTest("kr.c", 6, 13, "kr.c", 9, 1); // foo in "return foo(kk) + boo(kk);"
        performTest("kr.c", 6, 23, "kr.c", 17, 1); // boo in "return foo(kk) + boo(kk);"
    }

    public void testFuncLocalVarsUsage() throws Exception {
        performTest("main.c", 5, 20, "main.c", 3, 5); // kk in "for (int ii = kk; ii > 0; ii--) {"
        performTest("main.c", 6, 10, "main.c", 4, 5); // res in "res *= ii;"
        performTest("main.c", 8, 13, "main.c", 4, 5); // res in "return res;"
        performTest("kr.c", 6, 17, "kr.c", 5, 5); // first kk in "return foo(kk) + boo(kk);"
        performTest("kr.c", 6, 27, "kr.c", 5, 5); // second kk in "return foo(kk) + boo(kk);"
    }

    public void testForLoopLocalVarsUsage() throws Exception {
        performTest("main.c", 5, 24, "main.c", 5, 10); // second ii in "for (int ii = kk; ii > 0; ii--) {"
        performTest("main.c", 5, 32, "main.c", 5, 10); // third ii in "for (int ii = kk; ii > 0; ii--) {"
        performTest("main.c", 6, 17, "main.c", 5, 10); // ii in "res *= ii;"
    }

    public void testNameWithUnderscore() throws Exception {
        performTest("main.c", 12, 6, "main.c", 11, 1); // method_name_with_underscore();
    }

    public void testSameNameDiffScope() throws Exception {
        // IZ#131560: Hyperlink does not distinguish variables with the same names within function body
        // function parameter
        performTest("main.c", 22, 30, "main.c", 22, 24); // name in void sameNameDiffScope(int name) {
        performTest("main.c", 23, 10, "main.c", 22, 24); // name in if (name++) {
        performTest("main.c", 26, 17, "main.c", 22, 24); // name in } else if (name++) {
        performTest("main.c", 26, 17, "main.c", 22, 24); // name in name--;

        // local variable
        performTest("main.c", 24, 17, "main.c", 24, 9); // name in name--;
        performTest("main.c", 25, 10, "main.c", 24, 9); // name in name--;

        // second local variable
        performTest("main.c", 27, 17, "main.c", 27, 9); // name in name--;
        performTest("main.c", 28, 17, "main.c", 27, 9); // name in name--;
    }

    public void testGlobalVar() throws Exception {
        // IZ#132295: Hyperlink does not  distinguish local variable and global
        // variable if they has same name

        // local variable
        performTest("main.c", 33, 24, "main.c", 32, 5);
        performTest("main.c", 34, 36, "main.c", 32, 5);

        // global variable
        performTest("main.c", 33, 14, "main.c", 38, 1);
        performTest("main.c", 34, 12, "main.c", 38, 1);
        performTest("main.c", 34, 28, "main.c", 38, 1);
    }

    public void testConstParameter() throws Exception {
        // IZ#76032: ClassView component doubles function in some cases
        // (partial fix: made const parameters resolve correctly)
        performTest("const.cc", 5, 44, "const.cc", 1, 1);
        performTest("const.cc", 5, 50, "const.cc", 2, 5);
    }

    ////////////////////////////////////////////////////////////////////////////
    // K&R style

    public void testKRFuncParamUsage() throws Exception {
        performTest("kr.c", 12, 15, "kr.c", 10, 1); // index in 'return index;'
    }

    public void testKRFooDeclDefUsageH() throws Exception {
        // See IZ116715
        performTest("kr.h", 2, 6, "kr.h", 9, 1); // int foo(); -> int foo(index)
        performTest("kr.h", 9, 6, "kr.h", 2, 1); // int foo(index) -> int foo();
        performTest("kr.h", 15, 6, "kr.h", 17, 1); // int boo(); -> int boo(int i)
        performTest("kr.h", 17, 6, "kr.h", 15, 1); // int boo(int i) -> int boo();
    }

    public void testKRFooDeclDefUsageC() throws Exception {
        // See IZ116715
        performTest("kr.c", 2, 6, "kr.c", 9, 1); // int foo(); -> int foo(index)
        performTest("kr.c", 9, 6, "kr.c", 2, 1); // int foo(index) -> int foo();
        performTest("kr.c", 15, 6, "kr.c", 17, 1); // int boo(); -> int boo(int i)
        performTest("kr.c", 17, 6, "kr.c", 15, 1); // int boo(int i) -> int boo();
    }

    public void testStaticVariable() throws Exception {
        // See IZ136481
        performTest("static_variable.c", 5, 16, "static_variable.h", 2, 1);
        performTest("static_variable.c", 6, 15, "static_variable.h", 1, 1);
        // See IZ151730: Unresolved static variable in header included after its definition
        performTest("static_variable.h", 2, 40, "static_variable.c", 1, 1);
    }

    public void testStaticFunctions() throws Exception {
        // IZ#151751: Unresolved usage of function name as pointer for static member initialization
        performTest("static_function.c", 26, 10, "static_function.c", 17, 1);
        performTest("static_function.c", 11, 30, "static_function.c", 2, 1);
    }

    public void testIZ131555() throws Exception {
        for (int i = 5; i <=13; i++ ) {
            performTest("IZ131555.c", i, 16, "IZ131555.c", 2, 5);
        }
    }

    public void testIZ136730() throws Exception {
        performTest("IZ136730.c", 2, 11, "IZ136730.c", 3, 1);
    }

    public void testTemplateParameter() throws Exception {
        performTest("template_parameter.cc", 2, 13, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 3, 13, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 6, 19, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 7, 14, "template_parameter.cc", 1, 17);
        performTest("template_parameter.cc", 7, 12, "template_parameter.cc", 1, 29);
        performTest("template_parameter.cc", 7, 26, "template_parameter.cc", 1, 10);
        performTest("template_parameter.cc", 8, 11, "template_parameter.cc", 1, 10);
    }

    public void testTemplateParameterBeforeFunction() throws Exception {
        // IZ#138099 : unresolved identifier for functions' template parameter
        performTest("template_parameter2.cc", 1, 18, "template_parameter2.cc", 1, 11);
        performTest("template_parameter2.cc", 4, 22, "template_parameter2.cc", 4, 15);
        performTest("template_parameter2.cc", 4, 66, "template_parameter2.cc", 4, 15);
        performTest("template_parameter2.cc", 5, 15, "template_parameter2.cc", 5, 14);
        performTest("template_parameter2.cc", 5, 41, "template_parameter2.cc", 5, 14);
        performTest("template_parameter2.cc", 8, 20, "template_parameter2.cc", 8, 10);
        performTest("template_parameter2.cc", 8, 46, "template_parameter2.cc", 8, 10);
        performTest("template_parameter2.cc", 9, 20, "template_parameter2.cc", 9, 10);
        performTest("template_parameter2.cc", 9, 46, "template_parameter2.cc", 9, 10);
        performTest("template_parameter2.cc", 11, 11, "template_parameter2.cc", 11, 10);
        performTest("template_parameter2.cc", 11, 55, "template_parameter2.cc", 11, 10);
        performTest("template_parameter2.cc", 13, 17, "template_parameter2.cc", 13, 10);
        performTest("template_parameter2.cc", 13, 29, "template_parameter2.cc", 13, 22);
        performTest("template_parameter2.cc", 13, 33, "template_parameter2.cc", 13, 22);
    }

    public void testIZ131625() throws Exception {
        performTest("IZ131625.cc",  4, 11, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc",  7, 23, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc",  7, 23, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc", 14, 35, "IZ131625.cc", 12, 3);
        performTest("IZ131625.cc", 18, 24, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc", 20,  3, "IZ131625.cc", 10, 1);
        performTest("IZ131625.cc", 21, 12, "IZ131625.cc", 13, 3);
        performTest("IZ131625.cc", 22, 11, "IZ131625.cc", 13, 3);
        performTest("IZ131625.cc", 10, 20, "IZ131625.cc",   4, 3);
    }

    public void testIZ136146() throws Exception {
        performTest("IZ136146.cc", 20, 10, "IZ136146.cc", 15, 5);
        performTest("IZ136146.cc", 21, 12, "IZ136146.cc", 15, 5);
    }

    public void testIZ132903() throws Exception {
        performTest("IZ132903.cc", 16, 10, "IZ132903.cc",  9, 5);
    }

    public void testIZ136167() throws Exception {
        performTest("IZ136167.cc", 21, 13, "IZ136167.cc",  3, 5);
    }

    public void testIZ138833() throws Exception {
        performTest("IZ138833.cc", 4, 17, "IZ138833.cc",  3, 5);
    }

    public void testIZ138905() throws Exception {
        // IZ#138905 : IDE highlights 'a1' as invalid identifier (struct {...} a1;)
        performTest("IZ138905.cc", 4, 4, "IZ138905.cc", 4, 3);
        performTest("IZ138905.cc", 9, 4, "IZ138905.cc", 9, 3);
        performTest("IZ138905.cc", 12, 18, "IZ138905.cc", 12, 17);
    }

    public void testIZ139056() throws Exception {
        // IZ#139056 : using directive affects only single namespace definition
        performTest("IZ139056.cc", 10, 8, "IZ139056.cc", 2, 5);
        performTest("IZ139056.cc", 10, 24, "IZ139056.cc", 2, 5);
        performTest("IZ139056.cc", 15, 8, "IZ139056.cc", 2, 5);
        performTest("IZ139056.cc", 15, 24, "IZ139056.cc", 2, 5);
    }

    public void testIZ139141() throws Exception {
        // IZ#139141 : unable to resolve constructor of nested structure
        performTest("IZ139141.cc", 7, 6, "IZ139141.cc", 7, 5);
        performTest("IZ139141.cc", 8, 6, "IZ139141.cc", 8, 5);
    }

    public void testIZ139618() throws Exception {
        // IZ#139618 : Syntax hightlighting failure for unnamed union
        performTest("IZ139618.cc", 2, 11, "IZ139618.cc", 2, 9);
        performTest("IZ139618.cc", 2, 15, "IZ139618.cc", 2, 14);
        performTest("IZ139618.cc", 3, 13, "IZ139618.cc", 3, 5);
        performTest("IZ139618.cc", 8, 16, "IZ139618.cc", 8, 9);
        performTest("IZ139618.cc", 9, 15, "IZ139618.cc", 9, 9);
        performTest("IZ139618.cc", 11, 7, "IZ139618.cc", 8, 9);
        performTest("IZ139618.cc", 12, 6, "IZ139618.cc", 9, 9);
        performTest("IZ139618.cc", 12, 19, "IZ139618.cc", 10, 7);
        performTest("IZ139618.cc", 12, 22, "IZ139618.cc", 9, 9);
    }

    public void testIZ139693() throws Exception {
        // IZ#139693 : function-local typedefs are not resolved
        performTest("IZ139693.cc", 2, 21, "IZ139693.cc", 2, 5);
        performTest("IZ139693.cc", 3, 9, "IZ139693.cc", 2, 5);
        performTest("IZ139693.cc", 4, 26, "IZ139693.cc", 2, 5);
    }

    public void testIZ139409() throws Exception {
        // IZ#139409 : Labels highlighted as errors
        performTest("IZ139409.cc", 1, 8, "IZ139409.cc", 1, 1);
        performTest("IZ139409.cc", 3, 17, "IZ139409.cc", 1, 1);
        performTest("IZ139409.cc", 4, 7, "IZ139409.cc", 4, 5);
        performTest("IZ139409.cc", 6, 16, "IZ139409.cc", 4, 5);
        performNullTargetTest("IZ139409.cc", 8, 11);
    }

    public void testIZ139784() throws Exception {
        // IZ#139784 : last unnamed enum overrides previous ones
        performTest("IZ139784.cc", 2, 13, "IZ139784.cc", 2, 12);
        performTest("IZ139784.cc", 2, 21, "IZ139784.cc", 2, 20);
        performTest("IZ139784.cc", 3, 13, "IZ139784.cc", 3, 12);
        performTest("IZ139784.cc", 3, 18, "IZ139784.cc", 2, 12);
        performTest("IZ139784.cc", 3, 26, "IZ139784.cc", 3, 25);
        performTest("IZ139784.cc", 3, 31, "IZ139784.cc", 2, 20);
        performTest("IZ139784.cc", 4, 16, "IZ139784.cc", 2, 12);
        performTest("IZ139784.cc", 5, 16, "IZ139784.cc", 2, 20);
        performTest("IZ139784.cc", 6, 16, "IZ139784.cc", 3, 12);
        performTest("IZ139784.cc", 7, 16, "IZ139784.cc", 3, 25);
    }

    public void testIZ139058() throws Exception {
        // IZ#139058 : unresolved identifiers in statement "this->operator std::string()"
        performTest("IZ139058.cc", 7, 65, "IZ139058.cc", 1, 1);
        performTest("IZ139058.cc", 7, 75, "IZ139058.cc", 2, 5);
    }

    public void testIZ139143() throws Exception {
        // IZ#139143 : unresolved identifiers in "(*cur.object).*cur.creator"
        performTest("IZ139143.cc", 9, 9, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 9, 14, "IZ139143.cc", 4, 5);
        performTest("IZ139143.cc", 9, 24, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 9, 29, "IZ139143.cc", 5, 5);
        performTest("IZ139143.cc", 10, 11, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 10, 18, "IZ139143.cc", 4, 5);
        performTest("IZ139143.cc", 10, 23, "IZ139143.cc", 8, 5);
        performTest("IZ139143.cc", 10, 28, "IZ139143.cc", 5, 5);
    }

    public void testIZ140111() throws Exception {
        // IZ#140111 : unresolved identifier in declaration "TCHAR c;"
        performTest("IZ140111.cc", 3, 10, "IZ140111.cc", 3, 1);
        performTest("IZ140111.cc", 4, 8, "IZ140111.cc", 4, 1);
        performTest("IZ140111.cc", 7, 14, "IZ140111.cc", 7, 5);
        performTest("IZ140111.cc", 8, 12, "IZ140111.cc", 8, 5);
        performTest("IZ140111.cc", 12, 14, "IZ140111.cc", 12, 5);
        performTest("IZ140111.cc", 13, 12, "IZ140111.cc", 13, 5);
        performTest("IZ140111.cc", 14, 8, "IZ140111.cc", 14, 5);
    }

    public void testIZ140589() throws Exception {
        // IZ#140589 : template class member is not resolved when parentheses are used
        performTest("IZ140589.cc", 8, 38, "IZ140589.cc", 3, 5);
        performTest("IZ140589.cc", 9, 38, "IZ140589.cc", 3, 5);
    }

    public void testIZ138683() throws Exception {
        // IZ#138683 : function typedef are not recognized
        performTest("IZ138683.cc", 4, 24, "IZ138683.cc", 2, 1);
    }

    public void testLabels() throws Exception {
        // IZ#141135 : Labels within code bocks are unresolved
        performTest("labels.cc", 3, 12, "labels.cc", 4, 5);
        performTest("labels.cc", 8, 12, "labels.cc", 10, 9);
        performTest("labels.cc", 15, 12, "labels.cc", 19, 9);
        performTest("labels.cc", 24, 12, "labels.cc", 26, 9);
        performTest("labels.cc", 31, 12, "labels.cc", 33, 9);
        performTest("labels.cc", 38, 12, "labels.cc", 40, 9);
        performTest("labels.cc", 45, 12, "labels.cc", 47, 9);
        performTest("labels.cc", 57, 19, "labels.cc", 54, 13);
    }

    public void testStaticConstInNamespace() throws Exception {
        // IZ141765 static const in namespace definition is unresolved
        performTest("IZ141765_static_const_in_nsp.cc", 7, 48, "IZ141765_static_const_in_nsp.h", 3, 17);
        performTest("IZ141765_static_const_in_nsp.cc", 9, 48, "IZ141765_static_const_in_nsp.h", 4, 17);
    }

    public void testStaticFunctionInHeader() throws Exception {
        // IZ141601 A static function defined in a header and used in a source file is unresolved
        performTest("IZ141601_static_fun_in_hdr.c", 4, 8, "IZ141601_static_fun_in_hdr.h", 2, 1);
    }

    public void testIZ141842() throws Exception {
        // IZ#141842 : If template parameter declared as a template class, its usage is unresolved
        performTest("IZ141842.cc", 9, 13, "IZ141842.cc", 5, 5);
        performTest("IZ141842.cc", 13, 5, "IZ141842.cc", 5, 5);
        performTest("IZ141842.cc", 14, 5, "IZ141842.cc", 5, 5);
    }

    public void testIZ137897() throws Exception {
        // IZ#137897 : parameters of function pointer are not resolved
        performTest("IZ137897.cc", 1, 24, "IZ137897.cc", 1, 15);
        performTest("IZ137897.cc", 1, 43, "IZ137897.cc", 1, 31);
        performTest("IZ137897.cc", 2, 26, "IZ137897.cc", 2, 16);
        performTest("IZ137897.cc", 2, 43, "IZ137897.cc", 2, 34);
        performTest("IZ137897.cc", 3, 30, "IZ137897.cc", 3, 24);
    }

    public void testIZ143226() throws Exception {
        // IZ#143226 : Incorrect error in the editor
        performTest("IZ143226.cc", 3, 6, "IZ143226.cc", 2, 5);
        performTest("IZ143226.cc", 3, 18, "IZ143226.cc", 2, 5);
    }

    public void testIZ144154() throws Exception {
        // IZ#144154 : nested typedef "type" is unresolved in Boost
        performTest("IZ144154.cc", 24, 49, "IZ144154.cc", 12, 9);
        performTest("IZ144154.cc", 57, 52, "IZ144154.cc", 31, 5);
    }

    public void testIZ144360() throws Exception {
        // IZ#144360 : unable to resolve typedef-ed class member in loki
        performTest("IZ144360.cc", 12, 22, "IZ144360.cc", 12, 9);
        performTest("IZ144360.cc", 13, 9, "IZ144360.cc", 12, 9);
        performTest("IZ144360.cc", 13, 15, "IZ144360.cc", 7, 9);
    }

    public void testIZ140795() throws Exception {
        // IZ#140795 : Usage of enumerators of nested enums
        // of the template specializations are unresolved
        performTest("IZ140795.cc", 8, 30, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 9, 29, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 10, 30, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 11, 34, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 12, 36, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 13, 37, "IZ140795.cc", 4, 16);
        performTest("IZ140795.cc", 14, 43, "IZ140795.cc", 4, 16);
    }

    public void testIZ140757() throws Exception {
        // IZ#140757 : Template parameter in the definition of the static
        // template class field is highlighted as an error
        performTest("IZ140757.cc", 17, 12, "IZ140757.cc", 17, 5);
        performTest("IZ140757.cc", 18, 29, "IZ140757.cc", 18, 5);
        performTest("IZ140757.cc", 19, 29, "IZ140757.cc", 19, 5);
        performTest("IZ140757.cc", 20, 36, "IZ140757.cc", 20, 5);
        performTest("IZ140757.cc", 21, 12, "IZ140757.cc", 21, 5);
        performTest("IZ140757.cc", 23, 27, "IZ140757.cc", 17, 5);
        performTest("IZ140757.cc", 23, 30, "IZ140757.cc", 18, 5);
        performTest("IZ140757.cc", 23, 33, "IZ140757.cc", 19, 5);
        performTest("IZ140757.cc", 23, 36, "IZ140757.cc", 20, 5);
        performTest("IZ140757.cc", 23, 39, "IZ140757.cc", 21, 5);
        performTest("IZ140757.cc", 24, 22, "IZ140757.cc", 17, 5);
        performTest("IZ140757.cc", 24, 25, "IZ140757.cc", 18, 5);
        performTest("IZ140757.cc", 24, 28, "IZ140757.cc", 19, 5);
        performTest("IZ140757.cc", 24, 31, "IZ140757.cc", 20, 5);
        performTest("IZ140757.cc", 24, 34, "IZ140757.cc", 21, 5);
    }

    public void testIZ144363() throws Exception {
        // IZ#144363 : typename in for-loop leads to unresolved identifier error
        performTest("IZ144363.cc", 17, 48, "IZ144363.cc", 17, 13);
        performTest("IZ144363.cc", 18, 15, "IZ144363.cc", 17, 13);
        performTest("IZ144363.cc", 20, 43, "IZ144363.cc", 9, 5);
    }

    public void testIZ145286() throws Exception {
        // IZ#145286 : const variable declared in "if" condition is not resolved
        performTest("IZ145286.cc", 3, 27, "IZ145286.cc", 3, 13);
        performTest("IZ145286.cc", 4, 14, "IZ145286.cc", 3, 13);
        performTest("IZ145286.cc", 6, 31, "IZ145286.cc", 6, 16);
        performTest("IZ145286.cc", 7, 15, "IZ145286.cc", 6, 16);
        performTest("IZ145286.cc", 9, 29, "IZ145286.cc", 9, 17);
        performTest("IZ145286.cc", 10, 22, "IZ145286.cc", 9, 17);
    }

    public void testNamesakes() throws Exception {
        // IZ#145553 Class in the same namespace should have priority over a global one
        // global
        performTest("iz_145553_namesakes.cc", 14, 26, "iz_145553_namesakes.cc", 1, 1);
        performTest("iz_145553_namesakes.cc", 15, 8, "iz_145553_namesakes.cc", 1, 1);
        performTest("iz_145553_namesakes.cc", 18, 18, "iz_145553_namesakes.cc", 11, 5);
        performTest("iz_145553_namesakes.cc", 19, 12, "iz_145553_namesakes.cc", 1, 1);
        performTest("iz_145553_namesakes.cc", 19, 19, "iz_145553_namesakes.cc", 3, 5);
        performTest("iz_145553_namesakes.cc", 20, 20, "iz_145553_namesakes.cc", 4, 9);
        performTest("iz_145553_namesakes.cc", 22, 22, "iz_145553_namesakes.cc", 14, 1);
        // namespace
        performTest("iz_145553_namesakes.cc", 36, 24, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 41, 31, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 42, 10, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 45, 20, "iz_145553_namesakes.cc", 38, 9);
        performTest("iz_145553_namesakes.cc", 46, 16, "iz_145553_namesakes.cc", 28, 5);
        performTest("iz_145553_namesakes.cc", 47, 23, "iz_145553_namesakes.cc", 31, 13);
        performTest("iz_145553_namesakes.cc", 49, 25, "iz_145553_namesakes.cc", 41, 5);
    }

    public void testIZ145071() throws Exception {
        // IZ#145071 : forward declarations marked as error
        performTest("IZ145071.cc", 2, 20, "IZ145071.cc", 2, 1);
    }

    public void testIZ136731() throws Exception {
        // IZ#136731 : No hyper link on local extern function
        performTest("IZ136731_local_extern_function.cc", 4, 18, "IZ136731_local_extern_function.cc", 3, 16);
        performTest("IZ136731_local_extern_function.cc", 3, 40, "IZ136731_local_extern_function.cc", 3, 32);
    }

    public void testIZ146464() throws Exception {
        // IZ#146464 : IDE can't find 'wchar_t' identifier in C projects
        performTest("IZ146464.c", 1, 16, "IZ146464.c", 1, 1); // NOI18N
        performTest("IZ146464.c", 2, 5, "IZ146464.c", 1, 1); // NOI18N
        performTest("IZ146464.c", 2, 23, "IZ146464.c", 1, 1); // NOI18N
    }

    public void testIZ147627() throws Exception {
        // IZ#147627 : IDE highlights code with 'i' in 'for' as wrong
        performTest("IZ147627.cc", 6, 18, "IZ147627.cc", 6, 14); // NOI18N
        performTest("IZ147627.cc", 7, 23, "IZ147627.cc", 6, 14); // NOI18N
        performTest("IZ147627.cc", 7, 28, "IZ147627.cc", 6, 14); // NOI18N
        performTest("IZ147627.cc", 8, 18, "IZ147627.cc", 8, 14); // NOI18N
        performTest("IZ147627.cc", 9, 23, "IZ147627.cc", 8, 14); // NOI18N
        performTest("IZ147627.cc", 9, 28, "IZ147627.cc", 8, 14); // NOI18N
    }

    public void testIZ147632() throws Exception {
        // IZ#147632 : IDE highlights global variable in 'if' as wrong
        performTest("IZ147632.cc", 8, 16, "IZ147632.cc", 1, 1);
        performTest("IZ147632.cc", 8, 25, "IZ147632.cc", 3, 5);
        performTest("IZ147632.cc", 10, 25, "IZ147632.cc", 3, 5);
        performTest("IZ147632.cc", 12, 25, "IZ147632.cc", 3, 5);
    }

    public void testIZ152875() throws Exception {
        // IZ#152875 : No mark occurrences in macros actual parameters
        performTest("IZ152875.cc", 12, 26, "IZ152875.cc", 9, 24);
        performTest("IZ152875.cc", 12, 43, "IZ152875.cc", 9, 39);
        performTest("IZ152875.cc", 12, 53, "IZ152875.cc", 10, 5);
    }

    public void testIZ153761() throws Exception {
        // IZ#153761 : regression in python
        performTest("IZ153761.cc", 16, 18, "IZ153761.cc", 16, 13);
        performTest("IZ153761.cc", 19, 18, "IZ153761.cc", 19, 13);
        performTest("IZ153761.cc", 22, 18, "IZ153761.cc", 22, 13);
        performTest("IZ153761.cc", 25, 18, "IZ153761.cc", 25, 13);
        performTest("IZ153761.cc", 28, 18, "IZ153761.cc", 28, 13);
        performTest("IZ153761.cc", 31, 18, "IZ153761.cc", 31, 13);
        performTest("IZ153761.cc", 35, 18, "IZ153761.cc", 35, 13);
        performTest("IZ153761.cc", 38, 18, "IZ153761.cc", 38, 13);
        performTest("IZ153761.cc", 41, 18, "IZ153761.cc", 41, 13);
        performTest("IZ153761.cc", 44, 14, "IZ153761.cc", 43, 9);
    }

    public void testKRFuncParamDecl() throws Exception {
        performTest("kr.c", 9, 10, "kr.c", 10, 1); // index in 'int foo(index)'
        performTest("kr.c", 21, 13, "kr.c", 22, 8); // index in 'int foo(index)'
        performTest("kr.c", 21, 17, "kr.c", 22, 12); // index in 'int foo(index)'
    }

    public void testKRFooDeclDefUsage() throws Exception {
        // See IZ116715
        performTest("kr.c", 2, 6, "kr.c", 9, 1); // int foo(); -> int foo(index)
        performTest("kr.c", 9, 6, "kr.c", 2, 1); // int foo(index) -> int foo();
        performTest("kr.c", 15, 6, "kr.c", 17, 1); // int boo(); -> int boo(int i)
        performTest("kr.c", 17, 6, "kr.c", 15, 1); // int boo(int i) -> int boo();
    }

    public void testIZ151705() throws Exception {
        // IZ#151705 : Unresolved ids in function call in case of empty macro
        performTest("IZ151705.cc", 9, 15, "IZ151705.cc", 6, 1);
    }

    public void testIZ151045() throws Exception {
        // IZ#151045 : Unresolved cast to macro type
        performTest("IZ151045.cc", 11, 21, "IZ151045.cc", 3, 5);
    }

    public void testIZ158816() throws Exception {
        // IZ#158816 : No hyperlink for ids after short macros
        performTest("IZ158816.cc", 8, 16, "IZ158816.cc", 2, 5);
    }

    public void testIZ150884() throws Exception {
        // IZ#150884 : Unresolved elements in local definition of type
        performTest("IZ150884.cc", 3, 11, "IZ150884.cc", 3, 9);
        performTest("IZ150884.cc", 3, 28, "IZ150884.cc", 3, 24);
        performTest("IZ150884.cc", 3, 54, "IZ150884.cc", 3, 51);
        performTest("IZ150884.cc", 9, 20, "IZ150884.cc", 2, 5);
        performTest("IZ150884.cc", 12, 17, "IZ150884.cc", 3, 9);
        performTest("IZ150884.cc", 14, 39, "IZ150884.cc", 3, 24);
        performTest("IZ150884.cc", 16, 19, "IZ150884.cc", 3, 51);
    }

    public void testIZ151588() throws Exception {
        // IZ#151588 : Unresolved element of array in case of complex index
        performTest("IZ151588.cc", 11, 26, "IZ151588.cc", 3, 5);
    }

    public void testIZ161901() throws Exception {
        // IZ#161901 : unresolved friend class forward
        performTest("IZ161901.cc", 3, 22, "IZ161901.cc", 3, 5);
    }

    public void testIZ169750() throws Exception {
        // IZ#169750 : Unresolved id in the case variable declared in while
        performTest("IZ169750.cc", 5, 37, "IZ169750.cc", 5, 12);
    }

    public void testIZ165961() throws Exception {
        // IZ#165961 : Unresolved ids in construction with macros
        performTest("IZ165961.cc", 9, 27, "IZ165961.cc", 9, 10);
    }

    public void testIZ165976() throws Exception {
        // IZ#165976 : Unresolved array element in case of complicated index
        performTest("IZ165976.cc", 15, 56, "IZ165976.cc", 4, 3);
    }

    public void testIZ173311() throws Exception {
        // IZ#173311 : Unresolved ids in function typedef
        performTest("IZ173311.cc", 2, 30, "IZ173311.cc", 2, 26);
    }

    public void testIZ145071_2() throws Exception {
        // IZ#145071 : forward declarations in function body marked as error
        performTest("IZ145071_2.cc", 2, 12, "IZ145071_2.cc", 2, 5);
        performTest("IZ145071_2.cc", 6, 20, "IZ145071_2.cc", 6, 5);
        performTest("IZ145071_2.cc", 10, 12, "IZ145071_2.cc", 10, 5);
        performTest("IZ145071_2.cc", 11, 20, "IZ145071_2.cc", 10, 5);
    }

    public void testIZ175123() throws Exception {
        // IZ#175123 : Pointer to const parsed incorrectly in some cases
        performTest("IZ175123.cc", 4, 21, "IZ175123.cc", 4, 9);
    }

    public void testStringInMacroParams() throws Exception {
        // Unresolved macro with string in params
        performTest("string_in_macro_params.cc", 7, 31, "string_in_macro_params.cc", 1, 1);
    }

    public void testIZ175877() throws Exception {
        // IZ#175877 : Error at processing #define func(args....)
        performTest("IZ175877.cc", 12, 6, "IZ175877.cc", 5, 3);
    }

    public void testIZ182152() throws Exception {
        // Bug 182152 - variable names in prototypes are unresolved in ide display
        performTest("IZ182152.cc", 3, 66, "IZ182152.cc", 3, 52);
    }

    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return BasicHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName, true);
        }
    }
}


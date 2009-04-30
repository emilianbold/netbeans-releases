/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
public class NamespacesHyperlinkTestCase extends HyperlinkBaseTestCase {

    public NamespacesHyperlinkTestCase(String testName) {
        super(testName);
        //System.setProperty("cnd.modelimpl.trace.registration", "true");
    }

    public void testScopeInTypeAfterConst() throws Exception {
        performTest("boost_in_type_after_scope.cpp", 14, 15, "boost_in_type_after_scope.cpp", 1, 1);
        performTest("boost_in_type_after_scope.cpp", 14, 25, "boost_in_type_after_scope.cpp", 2, 5);
        performTest("boost_in_type_after_scope.cpp", 14, 35, "boost_in_type_after_scope.cpp", 3, 9);
    }

    public void testTypeIdName() throws Exception {
        // IZ#162160: typeid(obj).name is not resolved
        performTest("typeid.cpp", 24, 25, "typeinfo.h", 21, 5);
        performTest("typeid.cpp", 25, 30, "typeinfo.h", 21, 5);
        performTest("typeid.cpp", 25, 40, "typeinfo.h", 21, 5);
    }

    public void testUsingInOtherNsDef() throws Exception {
        // IZ#159223: Unresolved ids from namespace with usings
        performTest("using_in_ns.cpp", 11, 24, "using_in_ns.cpp", 3, 9); // AA in struct B : public AA {
        performTest("using_in_ns.cpp", 11, 24, "using_in_ns.cpp", 3, 9); // TTT in TTT t;
        // IZ#159308: Unresolved using of using in nested namespace
        performTest("using_in_ns.cpp", 24, 20, "using_in_ns.cpp", 18, 9); // AAA in using N2::AAA;
        performTest("using_in_ns.cpp", 26, 15, "using_in_ns.cpp", 18, 9); // AAA in AAA a;
    }
    
    public void testS1FooDefFQN() throws Exception {
        performTest("file.cc", 9, 10, "file.cc", 4, 1); // S1 in S1::foo();
        performTest("file.cc", 9, 14, "file.cc", 7, 5); // foo in S1::foo();
        performTest("file.cc", 10, 10, "file.cc", 4, 1); // S1 in S1::var1();
        performTest("file.cc", 10, 14, "file.cc", 5, 5); // var1 in S1::var1;

        performTest("file.cc", 14, 10, "file.cc", 4, 1); // S1 in S1::S2::boo();
        performTest("file.cc", 14, 14, "file.cc", 20, 5); // S2 in S1::S2::boo();
        performTest("file.cc", 14, 18, "file.cc", 23, 9); // boo in S1::S2::boo();
        performTest("file.cc", 15, 10, "file.cc", 4, 1); // S1 in S1::S2::var2();
        performTest("file.cc", 15, 14, "file.cc", 20, 5); // S2 in S1::S2::var2();
        performTest("file.cc", 15, 18, "file.cc", 21, 9); // var2 in S1::S2::var2();
    }

    public void testS2BooDefFQN() throws Exception {
        performTest("file.cc", 25, 14, "file.cc", 4, 1); // S1 in S1::foo();
        performTest("file.cc", 25, 18, "file.cc", 7, 5); // foo in S1::foo();
        performTest("file.cc", 26, 14, "file.cc", 4, 1); // S1 in S1::var1();
        performTest("file.cc", 26, 18, "file.cc", 5, 5); // var1 in S1::var1;

        performTest("file.cc", 30, 14, "file.cc", 4, 1); // S1 in S1::S2::boo();
        performTest("file.cc", 30, 18, "file.cc", 20, 5); // S2 in S1::S2::boo();
        performTest("file.cc", 30, 22, "file.cc", 23, 9); // boo in S1::S2::boo();
        performTest("file.cc", 31, 14, "file.cc", 4, 1); // S1 in S1::S2::var2;
        performTest("file.cc", 31, 18, "file.cc", 20, 5); // S2 in S1::S2::var2;
        performTest("file.cc", 31, 22, "file.cc", 21, 9); // var2 in S1::S2::var2;
    }

    public void testMainDefFQN() throws Exception {
        performTest("main.cc", 6, 6, "file.cc", 4, 1); // S1 in S1::foo();
        performTest("main.cc", 6, 10, "file.cc", 7, 5); // foo in S1::foo();
        performTest("main.cc", 7, 6, "file.cc", 4, 1); // S1 in S1::var1();
        performTest("main.cc", 7, 10, "file.cc", 5, 5); // var1 in S1::var1;

        performTest("main.cc", 8, 6, "file.cc", 4, 1); // S1 in S1::S2::boo();
        performTest("main.cc", 8, 10, "file.cc", 20, 5); // S2 in S1::S2::boo();
        performTest("main.cc", 8, 14, "file.cc", 23, 9); // boo in S1::S2::boo();
        performTest("main.cc", 9, 6, "file.cc", 4, 1); // S1 in S1::S2::var2;
        performTest("main.cc", 9, 10, "file.cc", 20, 5); // S2 in S1::S2::var2;
        performTest("main.cc", 9, 14, "file.cc", 21, 9); // var2 in S1::S2::var2;
    }

    public void testS1FooDefS1Decls() throws Exception {
        performTest("file.cc", 11, 10, "file.cc", 7, 5); // foo();
        performTest("file.cc", 12, 10, "file.cc", 5, 5); // var1
    }

    public void testS2BooDefS1Decls() throws Exception {
        performTest("file.cc", 27, 14, "file.cc", 7, 5); // foo();
        performTest("file.cc", 28, 14, "file.cc", 5, 5); // var1
    }

    public void testS1FooDefS2() throws Exception {
        performTest("file.cc", 16, 10, "file.cc", 20, 5); // S2 in S2::boo();
        performTest("file.cc", 16, 14, "file.cc", 23, 9); // boo in S2::boo();
        performTest("file.cc", 17, 10, "file.cc", 20, 5); // S2 in S2::var2
        performTest("file.cc", 17, 14, "file.cc", 21, 9); // var2 in S2::var2
    }

    public void testS2BooDefS2Decls() throws Exception {
        performTest("file.cc", 32, 14, "file.cc", 20, 5); // S2 in S2::boo();
        performTest("file.cc", 32, 18, "file.cc", 23, 9); // boo in S2::boo();
        performTest("file.cc", 33, 14, "file.cc", 20, 5); // S2 in S2::var2
        performTest("file.cc", 33, 18, "file.cc", 21, 9); // var2 in S2::var2
        performTest("file.cc", 34, 14, "file.cc", 23, 9); // boo
        performTest("file.cc", 35, 14, "file.cc", 21, 9); // var2
    }

    public void testDeclsFromHeader() throws Exception {
        performTest("file.h", 6, 17, "file.cc", 5, 5); // extern int var1;
        performTest("file.h", 7, 11, "file.cc", 7, 5); // void foo();
        performTest("file.h", 9, 22, "file.cc", 21, 9); // extern int var2;
        performTest("file.h", 10, 15, "file.cc", 23, 9); // void boo();
    }

    public void testClassS1() throws Exception {
        performTest("file.cc", 39, 14, "file.h", 18, 5); // clsS1 s1;
        performTest("file.cc", 40, 20, "file.cc", 59, 5); // clsS1pubFun in s1.clsS1pubFun();
        performTest("file.cc", 52, 10, "file.h", 18, 5); // clsS1 s1;
        performTest("file.cc", 53, 15, "file.cc", 59, 5); // clsS1pubFun in s1.clsS1pubFun();
        performTest("file.cc", 59, 14, "file.h", 18, 5); // clsS1 in void clsS1::clsS1pubFun() {
        performTest("file.cc", 59, 20, "file.h", 20, 9); // clsS1pubFun in void clsS1::clsS1pubFun() {
        performTest("file.h", 20, 20, "file.cc", 59, 5); // void clsS1pubFun();
    }

    public void testClassS2() throws Exception {
        performTest("file.cc", 42, 14, "file.h", 12, 9); // clsS2 s2;
        performTest("file.cc", 43, 20, "file.cc", 46, 9); // clsS2pubFun in s2.clsS2pubFun();
        performTest("file.cc", 55, 14, "file.h", 12, 9); // clsS2 s2;
        performTest("file.cc", 46, 18, "file.h", 12, 9); // clsS2 in void clsS2::clsS2pubFun() {
        performTest("file.cc", 46, 25, "file.h", 14, 13); // clsS2pubFun in void clsS2::clsS2pubFun() {
        performTest("file.h", 14, 25, "file.cc", 46, 9); // void clsS2pubFun();
    }

    public void testUnnamed() throws Exception {
        performTest("unnamed.cc", 5, 6, "unnamed.h", 16, 5);//    funFromUnnamed();
        performTest("unnamed.cc", 6, 6, "unnamed.h", 11, 5);//    unnamedAInt = 10;
        performTest("unnamed.cc", 7, 6, "unnamed.h", 7, 5);//    ClUnnamedA in ClUnnamedA cl;
        performTest("unnamed.cc", 8, 10, "unnamed.h", 9, 9);//    funFromClassA in cl.funFromClassA();
        performTest("unnamed.cc", 9, 6, "unnamed.h", 13, 5);//    funDefFromUnnamed();

        performTest("unnamed.h", 6, 12, "unnamed.h", 16, 5);//    void funDefFromUnnamed();
    }

    public void testUsingNS1() throws Exception {
        performTest("main.cc", 15, 6, "file.cc", 5, 5); //var1 = 10;
        performTest("main.cc", 16, 6, "file.cc", 7, 5); //foo();
        performTest("main.cc", 17, 6, "file.h", 18, 5); //clsS1 in clsS1 c1;
        performTest("main.cc", 18, 10, "file.cc", 59, 5); //clsS1pubFun in c1.clsS1pubFun();
    }

    public void testUsingNS1S2() throws Exception {
        performTest("main.cc", 23, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 24, 6, "file.cc", 23, 9); //boo();
        performTest("main.cc", 25, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
        performTest("main.cc", 26, 10, "file.cc", 46, 9); //clsS2pubFun in c2.clsS2pubFun();
    }

    public void testUsingDirectivesS1() throws Exception {
        performTest("main.cc", 31, 6, "file.h", 18, 5); //clsS1 in clsS1 c1;
        performTest("main.cc", 33, 6, "file.cc", 5, 5); //var1 = 10;
        performTest("main.cc", 35, 6, "file.cc", 7, 5); //foo();
    }

    public void testUsingDirectivesS1S2() throws Exception {
        performTest("main.cc", 40, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
        performTest("main.cc", 42, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 44, 6, "file.cc", 23, 9); //boo();
    }

    public void testUsingCout() throws Exception {
        performTest("main.cc", 69, 10, "file.cc", 63, 5); //myCout in S1::myCout;
        performTest("main.cc", 70, 20, "file.cc", 63, 5); //myCout in using S1::myCout;
        performTest("main.cc", 71, 6, "file.cc", 63, 5); //myCout;
    }

    public void testUsingNS2() throws Exception {
        // IZ#106772: incorrect resolving of using directive
        performTest("main.cc", 51, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 52, 6, "file.cc", 23, 9); //boo();
        performTest("main.cc", 53, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
    }

    public void testUsingDirectivesS2() throws Exception {
        // IZ#106772: incorrect resolving of using directive
        performTest("main.cc", 61, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
        performTest("main.cc", 63, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 65, 6, "file.cc", 23, 9); //boo();
    }

    public void testNestedTypesOfTemplatedClass() throws Exception {
        // IZ#135999: string:: code completion doesn't work
        performTest("main.cc", 75, 20, "file.h", 26, 9);
        performTest("main.cc", 77, 15, "file.h", 26, 9);
    }

    public void testUsingDirectives() throws Exception {
        // IZ#144982: std class members are not resolved in litesql
        performTest("main.cc", 94, 12, "main.cc", 83, 9);
        performTest("main.cc", 102, 13, "main.cc", 83, 9);
    }

    public void testChildNamespaces() throws Exception {
        // IZ#145148: forward class declaration is not replaced by real declaration in some cases
        performTest("child_ns.cc", 9, 12, "child_ns.cc", 4, 13);
    }

    public void testChildNamespaces2() throws Exception {
        // IZ#145148: forward class declaration is not replaced by real declaration in some cases
        performTest("child_ns.cc", 18, 16, "child_ns.cc", 15, 20);
    }
    
    
    public void testChildNamespaces3() throws Exception {
        // IZ 145142 : unable to resolve declaration imported from child namespace
        performTest("child_ns.cc", 38, 17, "child_ns.cc", 30, 5);
        performTest("child_ns.cc", 39, 17, "child_ns.cc", 30, 5);
        performTest("child_ns.cc", 40, 17, "child_ns.cc", 25, 5);
        performTest("child_ns.cc", 41, 17, "child_ns.cc", 25, 5);

        performTest("child_ns.cc", 55, 23, "child_ns.cc", 50, 9);
    }
    
    public void testIZ145071() throws Exception {
        // IZ#145071 : forward declarations marked as error
        performTest("IZ145071.cc", 3, 21, "IZ145071.cc", 3, 5);
    }

    public void testIZ155148() throws Exception {
        // IZ#155148: Unresolved namespace alias
        performTest("iz155148.cc", 12, 13, "iz155148.cc", 5, 5);
    }
    
    public void testIZ145142() throws Exception {
        // IZ#145142 : unable to resolve declaration imported from child namespace
        performTest("iz145142.cc", 16, 35, "iz145142.cc", 4, 13);
    }

    public void testIZ150915() throws Exception {
        // IZ#150915 : Unresolved duplicate of a static function
        performTest("iz150915_2.cc", 2, 26, "iz150915_2.cc", 2, 5);
        performTest("iz150915_2.cc", 8, 25, "iz150915_2.cc", 2, 5);
        // File iz150915_1.cc is not referenced here, but it is important part of the test.
        // We make sure that hyperlink does not link to iz150915_1.cc.
    }

    public void testIZ159242() throws Exception {
        // IZ#159242 : Unresolved using of variable from unnamed namespace
        performTest("iz159242.cc", 33, 7, "iz159242.cc", 19, 9);
    }

    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class getTestCaseDataClass() {
            return NamespacesHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName);
        }

        public void testClassS2FunInFunS1() throws Exception {
            performTest("file.cc", 56, 20, "file.h", 14, 13); // clsS2pubFun in s2.clsS2pubFun();
        }

        public void testUsingNS2() throws Exception {
            performTest("main.cc", 54, 10, "file.cc", 46, 9); //clsS2pubFun in c2.clsS2pubFun();
        }
    }
}

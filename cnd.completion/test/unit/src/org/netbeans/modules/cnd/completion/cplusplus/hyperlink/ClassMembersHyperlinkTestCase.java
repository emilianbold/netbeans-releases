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
public class ClassMembersHyperlinkTestCase extends HyperlinkBaseTestCase {
    public ClassMembersHyperlinkTestCase(String testName) {
        super(testName);
    }

    public void testIZ151584() throws Exception {
        // IZ#151584:
        performTest("iz151584.cpp", 2, 10, "iz151584.cpp", 2, 5);
        performTest("iz151584.cpp", 4, 15, "iz151584.cpp", 4, 9);
        performTest("iz151584.cpp", 6, 20, "iz151584.cpp", 6, 13);
    }

    public void testIZ162280() throws Exception {
        // IZ#162280: Inaccuracy tests: regression in Boost and Vlc
        performTest("iz162280.cpp", 33, 20, "iz162280.cpp", 30, 9);
        performTest("iz162280.cpp", 33, 30, "iz162280.cpp", 30, 9);
        performTest("iz162280.cpp", 35, 15, "iz162280.cpp", 30, 9);
        performTest("iz162280.cpp", 35, 25, "iz162280.cpp", 27, 9);
    }

    public void testIZ149685() throws Exception {
        // IZ#149685: Multi-level class declarations not recognized by ide
        performTest("iz149685.cpp", 8, 30, "iz149685.cpp", 6, 13);
        performTest("iz149685.cpp", 19, 15, "iz149685.cpp", 6, 13);
    }

    public void testIZ155578() throws Exception {
        // IZ155578: Overloaded functions with tpedef'ed argument types break Code Assistance
        performTest("iz155578.cpp", 6, 20, "iz155578.cpp", 6, 5);
        performTest("iz155578.cpp", 7, 20, "iz155578.cpp", 7, 5);
    }

    public void testIZ159307() throws Exception {
        // IZ#159307: Wrong recognition of local constructor as global function
        performTest("iz159307.cpp", 13, 12, "iz159307.cpp", 7, 9);
        performTest("iz159307.cpp", 13, 30, "iz159307.cpp", 9, 9);
        performTest("iz159307.cpp", 14, 15, "iz159307.cpp", 1, 1);
    }

    public void testIZ159156() throws Exception {
        // IZ#159156: Wrong type resolving scope sequence in Resolver3
        performTest("iz159156.cpp", 18, 18, "iz159156.cpp", 2, 5);
    }

    public void testIZ148223() throws Exception {
        // IZ#148223: IDE can't recognize overloaded operator&&
        performTest("operators_hyperlink.cpp", 65, 21, "operators_hyperlink.cpp", 6, 9); // cc in (a1 && b1).cc()
        performTest("operators_hyperlink.cpp", 66, 21, "operators_hyperlink.cpp", 6, 9); // cc in (a1 && e1).cc()
        performTest("operators_hyperlink.cpp", 67, 21, "operators_hyperlink.cpp", 14, 9); // c1 in (e1 && d1)->c1()
    }

    public void testIZ157837() throws Exception {
        // IZ#157837: incorrect deref of function-type fields
        performTest("iz157837.cc", 23, 35, "iz157837.cc", 7, 9); // me_value
        performTest("iz157837.cc", 24, 35, "iz157837.cc", 7, 9); // me_value
        performTest("iz157837.cc", 25, 35, "iz157837.cc", 8, 9); // me_lookup
        performTest("iz157837.cc", 25, 55, "iz157837.cc", 12, 9);// mp_value
        performTest("iz157837.cc", 24, 45, "iz157837.cc", 3, 9); // value
    }

    public void testClassNameCollision() throws Exception {
        // IZ#156123: Resolve class from current namespace at first
        performTest("iz156123.cc", 21, 20, "iz156123.cc", 12, 9); // Field in "Database::Field fld;"
        performTest("iz156123.cc", 22, 15, "iz156123.cc", 14, 13); // name in "fld.name = 1;"
    }

    public void testDerefArrayByArrow() throws Exception {
        //IZ#149783: IDE does not recognize array as pointer
        performTest("iz149783.c", 9, 10, "iz149783.c", 2, 5);
        performTest("iz149783.c", 10, 15, "iz149783.c", 2, 5);
        performTest("iz149783.c", 11, 15, "iz149783.c", 2, 5);
        // IZ#151609: Unresolved struct array member
        performTest("iz149783.c", 13, 22, "iz149783.c", 2, 5);
    }

    public void testOperatorsInBaseClasses() throws Exception {
        // IZ#147312: Code completion issue with operator-> and operator*
        performTest("iz147312_operators_in_base_cls.cc", 85, 10, // o.myMethod1();
                    "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 88, 20, // sp.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 89, 15, // sp->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 90, 15, // (*sp).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 93, 20, // tp.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 94, 15, // tp->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 95, 15, // (*tp).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 98, 20, // s2p.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 99, 15, // s2p->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 100, 15, // (*s2p).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 103, 20, // t2p.get()->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 104, 15, // t2p->myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
        performTest("iz147312_operators_in_base_cls.cc", 105, 15, // (*t2p).myMethod1();
                "iz147312_operators_in_base_cls.cc", 7, 5);
    }

    public void testTemplateParamsInNestedClasses() throws Exception {
        // IZ#144881: template parameter is not resolved in nested class

        performTest("templateParameters.h", 104, 9, "templateParameters.h", 100, 10);// _Tp
        performTest("templateParameters.h", 105, 9, "templateParameters.h", 100, 10);// _Tp
        performTest("templateParameters.h", 106, 9, "templateParameters.h", 100, 10);// _Tp

        performTest("templateParameters.h", 103, 25, "templateParameters.h", 100, 24);// _Alloc
        performTest("templateParameters.h", 109, 15, "templateParameters.h", 100, 24);// _Alloc
    }

    public void testPtrOperator() throws Exception {
        // noIZ:fixed ptr operator handling
        performTest("checkPtrOperator.cc", 16, 15, "checkPtrOperator.cc", 11, 9);
    }

    public void testIZ146030_3() throws Exception {
        // IZ#146030: set of problems for declarations in Loki
        // usecase 3)
        performTest("accessMember.cc", 9, 37, "accessMember.cc", 4, 5);
        performTest("accessMember.cc", 10, 45, "accessMember.cc", 4, 5);
        performTest("accessMember.cc", 14, 32, "accessMember.cc", 5, 5);
        performTest("accessMember.cc", 15, 33, "accessMember.cc", 5, 5);
    }

    public void testIZ146030_5() throws Exception {
        // IZ#146030: set of problems for declarations in Loki
        // usecase 5)
        performTest("useenumerators.cc", 55, 20, "useenumerators.cc", 52, 9);
        performTest("useenumerators.cc", 56, 20, "useenumerators.cc", 52, 9);
    }

    public void testIZ138902() throws Exception {
        // IZ#138902: No completion and hyperl ink  to enumerator in structure init
        performTest("useenumerators.cc", 48, 35, "useenumerators.cc", 43, 19);
    }

    public void testIZ145828() throws Exception {
        // IZ#145828: & breaks completion in some expressions
        performTest("main.cc", 91, 25, "main.cc", 83, 5);
        performTest("main.cc", 92, 16, "main.cc", 84, 5);
        performTest("main.cc", 93, 24, "main.cc", 85, 5);
    }

    public void testIZ144880() throws Exception {
        // IZ#144880: enumerators in template arguments are not resolved
        performTest("useenumerators.cc", 33, 12, "useenumerators.cc", 32, 12);
        performTest("useenumerators.cc", 38, 12, "useenumerators.cc", 37, 12);
    }
    public void testIZ145617() throws Exception {
        // IZ#145617: IDE highlights code with 'sizeof' in array as wrong
        performTest("main.cc", 79, 70, "main.cc", 59, 5);
    }

    public void testIZ145230() throws Exception {
        // IZ#145230:Various C++ expressions don't resolve
        // usage of enumerators
        performTest("useenumerators.cc", 4, 20, "useenumerators.cc", 1, 8);
        performTest("useenumerators.cc", 16, 40, "useenumerators.cc", 11, 5);
        performTest("useenumerators.cc", 19, 35, "useenumerators.cc", 11, 5);
    }

    public void testIZ145822() throws Exception {
        // IZ#145230:unresolved members of typedefed class
        performTest("useenumerators.cc", 26, 20, "useenumerators.cc", 26, 5);
        performTest("useenumerators.cc", 40, 10, "useenumerators.cc", 26, 5);
    }

    public void testIZ144731() throws Exception {
        // IZ#144731: function(a->m_obj ? a->m_obj : a->m_obj);
        performTest("iz145077.cc", 132, 30, "iz145077.cc", 118, 5);
    }

    public void testClassUsageAfterDereferrencedObjects() throws Exception {
        // IZ#145230:Various C++ expressions don't resolve
        performTest("ClassNameAfterDeref.cc", 22, 18, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 23, 18, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 24, 10, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 25, 10, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 32, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 34, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 35, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 38, 16, "ClassNameAfterDeref.cc", 2, 5);
        performTest("ClassNameAfterDeref.cc", 39, 16, "ClassNameAfterDeref.cc", 2, 5);
    }

    public void testClassMembersUsageAfterDereferrencedClass() throws Exception {
        // IZ#145230:Various C++ expressions don't resolve
        performTest("ClassNameAfterDeref.cc", 22, 25, "ClassNameAfterDeref.cc", 8, 9);
        performTest("ClassNameAfterDeref.cc", 23, 25, "ClassNameAfterDeref.cc", 9, 9);
        performTest("ClassNameAfterDeref.cc", 24, 16, "ClassNameAfterDeref.cc", 5, 9);
        performTest("ClassNameAfterDeref.cc", 25, 16, "ClassNameAfterDeref.cc", 8, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 27, 20);
        performTest("ClassNameAfterDeref.cc", 32, 22, "ClassNameAfterDeref.cc", 5, 9);
        performTest("ClassNameAfterDeref.cc", 33, 15, "ClassNameAfterDeref.cc", 16, 9);
        performTest("ClassNameAfterDeref.cc", 34, 22, "ClassNameAfterDeref.cc", 6, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 35, 24);
        performTest("ClassNameAfterDeref.cc", 36, 15, "ClassNameAfterDeref.cc", 6, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 37, 17);
        performTest("ClassNameAfterDeref.cc", 38, 25, "ClassNameAfterDeref.cc", 5, 9);
        performNullTargetTest("ClassNameAfterDeref.cc", 39, 25);
    }

    public void testClassFwdTemplateParameters() throws Exception {
        // template parameters of class member forward template class declaration
        performTest("templateParameters.h", 36, 23, "templateParameters.h", 36, 13);
        performTest("templateParameters.h", 37, 40, "templateParameters.h", 37, 13);
        performTest("templateParameters.h", 38, 30, "templateParameters.h", 38, 13);
        performTest("templateParameters.h", 39, 40, "templateParameters.h", 39, 13);
        performTest("templateParameters.h", 40, 40, "templateParameters.h", 40, 13);
        performTest("templateParameters.h", 41, 40, "templateParameters.h", 41, 13);

        // template parameters of global forward template class declaration
        performTest("templateParameters.h", 48, 24, "templateParameters.h", 48, 10);
        performTest("templateParameters.h", 48, 34, "templateParameters.h", 48, 27);
        performTest("templateParameters.h", 48, 45, "templateParameters.h", 48, 37);
    }

    public void testNestedTemplateClassTemplateParameters() throws Exception {
        performTest("templateParameters.h", 21, 50, "templateParameters.h", 21, 15); // test for ThreadingModel
        performTest("templateParameters.h", 26, 45, "templateParameters.h", 21, 15); // test for ThreadingModel
        performTest("templateParameters.h", 28, 30, "templateParameters.h", 21, 15); // test for ThreadingModel

        performTest("templateParameters.h", 22, 22, "templateParameters.h", 22, 15); // test for MX
        performTest("templateParameters.h", 26, 71, "templateParameters.h", 22, 15); // test for MX
        performTest("templateParameters.h", 28, 55, "templateParameters.h", 22, 15); // test for MX

        performTest("templateParameters.h", 25, 25, "templateParameters.h", 25, 19); // test for P
        performTest("templateParameters.h", 26, 66, "templateParameters.h", 25, 19); // test for P
        performTest("templateParameters.h", 28, 50, "templateParameters.h", 25, 19); // test for P
    }

    public void testTemplateParameters() throws Exception {
        performTest("templateParameters.h", 1, 23, "templateParameters.h", 1, 10); // test for L
        performTest("templateParameters.h", 2, 25, "templateParameters.h", 1, 10); // test for L
        performTest("templateParameters.h", 5, 57, "templateParameters.h", 1, 10); // test for L
        performTest("templateParameters.h", 8, 34, "templateParameters.h", 1, 10); // test for L

        performTest("templateParameters.h", 1, 32, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 2, 28, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 2, 83, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 5, 54, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 13, 40, "templateParameters.h", 1, 26); // test for T
        performTest("templateParameters.h", 13, 63, "templateParameters.h", 1, 26); // test for T

        performTest("templateParameters.h", 1, 57, "templateParameters.h", 1, 35); // test for C
        performTest("templateParameters.h", 11, 9, "templateParameters.h", 1, 35); // test for C
        performTest("templateParameters.h", 13, 61, "templateParameters.h", 1, 35); // test for C
    }

    public void testRenamedTemplateParameters() throws Exception {
        // IZ 138903 : incorrect parsing of template function
        performTest("templateParameters.h", 89, 18, "templateParameters.h", 82, 1);
        performTest("templateParameters.h", 89, 23, "templateParameters.h", 83, 1);
        performTest("templateParameters.h", 89, 39, "templateParameters.h", 84, 1);
        performTest("templateParameters.h", 89, 43, "templateParameters.h", 85, 1);
        performTest("templateParameters.h", 89, 47, "templateParameters.h", 86, 1);
        performTest("templateParameters.h", 90, 6, "templateParameters.h", 82, 1);
    }

    public void testConstInTemplateParameters() throws Exception {
        // IZ#156679 : Constant in template is highlighted as invalid identifier
        performTest("templateParameters.h", 129, 9, "templateParameters.h", 125, 20);
    }

    public void testSameName() throws Exception {
        performTest("main.cc", 53, 10, "main.cc", 51, 1); //sameValue(  in sameValue(sameValue - 1);
        performTest("main.cc", 53, 20, "main.cc", 51, 16); //sameValue-1  in sameValue(sameValue - 1);
    }

    public void testInnerSelfDeclaration() throws Exception {
        performTest("ClassB.h", 8, 20, "ClassB.h", 8, 17); // "MEDIUM" in enum type { MEDIUM,  HIGH };
        performTest("ClassB.h", 8, 28, "ClassB.h", 8, 26); // "HIGH" in enum type { MEDIUM,  HIGH };
        performTest("ClassB.h", 8, 12, "ClassB.h", 8, 5); // "type" in enum type { MEDIUM,  HIGH };
        performTest("ClassB.h", 30, 15, "ClassB.h", 30, 5); // "myPtr" in void* myPtr;
    }

    public void testOverloads() throws Exception {
        performTest("ClassB.h", 34, 15, "ClassB.h", 34, 5); // setDescription in void setDescription(const char* description);
        performTest("ClassB.h", 36, 15, "ClassB.h", 36, 5); // setDescription in void setDescription(const char* description, const char* vendor, int type, int category, int units);
        performTest("ClassB.h", 38, 15, "ClassB.h", 38, 5); // setDescription in void setDescription(const ClassB& obj);
    }

    public void testFunParamInHeader() throws Exception {
        performTest("ClassB.h", 34, 40, "ClassB.h", 34, 25); // description in void setDescription(const char* description);
        performTest("ClassB.h", 16, 30, "ClassB.h", 16, 23); //"type1" in ClassB(int type1, int type2 = HIGH);
        performTest("ClassB.h", 16, 20, "ClassB.h", 16, 12); //"type2" in ClassB(int type1, int type2 = HIGH);
        performTest("ClassB.cc", 5, 22, "ClassB.cc", 5, 16); // type1 in ClassB::ClassB(int type1, int type2 /* = HIGH*/) :
        performTest("ClassB.cc", 5, 35, "ClassB.cc", 5, 27); // type2 in ClassB::ClassB(int type1, int type2 /* = HIGH*/) :
    }

    public void testConstructorInitializerListInHeader() throws Exception {
        performTest("ClassB.h", 13, 42, "ClassB.h", 13, 12); // second "type" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 25, "ClassB.h", 8, 17); // "MEDIUM" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 35, "ClassA.cc", 12, 1); // "ClassA" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 50, "ClassB.h", 27, 5); // "myType2" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
        performTest("ClassB.h", 13, 56, "ClassB.h", 8, 26); // "HIGH" in ClassB(int type = MEDIUM) : ClassA(type), myType2(HIGH) {
    }

    public void testConstructorInitializerListInSource() throws Exception {
        performTest("ClassB.cc", 6, 5, "ClassA.cc", 12, 1); // "ClassA" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 10, "ClassB.cc", 5, 16); // "type1" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 20, "ClassB.h", 27, 5); // "myType2" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 25, "ClassB.cc", 5, 27); // "type2" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 35, "ClassB.h", 26, 5); // "myType1" in ClassA(type1), myType2(type2), myType1(MEDIUM)
        performTest("ClassB.cc", 6, 45, "ClassB.h", 8, 17); // "MEDIUM" in ClassA(type1), myType2(type2), myType1(MEDIUM)
    }

    public void testClassNameInFuncsParams() throws Exception {
        performTest("ClassA.h", 12, 25, "ClassA.h", 2, 1); //ClassA in void publicFoo(ClassA a);
        performTest("ClassA.h", 13, 30, "ClassA.h", 2, 1); //ClassA in void publicFoo(const ClassA &a)
        performTest("ClassA.h", 23, 30, "ClassA.h", 2, 1); //ClassA in void void protectedFoo(const ClassA* const ar[]);
        performTest("ClassA.h", 31, 30, "ClassA.h", 2, 1); //ClassA in void privateFoo(const ClassA *a);
        performTest("ClassA.h", 52, 35, "ClassA.h", 2, 1); //second ClassA in  ClassA& operator= (const ClassA& obj);
    }

    public void testClassNameInFuncRetType() throws Exception {
        performTest("ClassA.h", 52, 10, "ClassA.h", 2, 1); //first ClassA in  ClassA& operator= (const ClassA& obj);
    }

    public void testStringFuncsParams() throws Exception {
        performTest("ClassB.cc", 8, 10, "ClassB.h", 20, 5); // "method" in method("string");
        performTest("ClassB.cc", 9, 10, "ClassB.h", 24, 5); // "method" in method("string", "string");
    }

    public void testCastsAndPtrs() throws Exception {
        performTest("main.cc", 45, 20, "ClassB.h", 30, 5); // myPtr in ((ClassB)*a).*myPtr;
        performTest("main.cc", 46, 21, "ClassB.h", 30, 5); // myPtr in ((ClassB*)a)->*myPtr;
        performTest("main.cc", 47, 20, "ClassB.h", 31, 5); // myVal in ((ClassB)*a).myVal;
        performTest("main.cc", 48, 20, "ClassB.h", 31, 5); // myVal in ((ClassB*)a)->myVal;
    }

    public void testFromMainToClassDecl() throws Exception {
        performTest("main.cc", 21, 6, "ClassA.h", 2, 1);
    }

    public void testPublicMethods() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 9, 11, "ClassA.cc", 24, 1); // void publicFoo();
        performTest("ClassA.h", 10, 11, "ClassA.cc", 27, 1); // void publicFoo(int a);
        performTest("ClassA.h", 11, 11, "ClassA.cc", 30, 1); // void publicFoo(int a, double b);
        //TODO: performTest("ClassA.h", 12, 11, "ClassA.cc", 33, 1); // void publicFoo(ClassA a);
        //TODO: performTest("ClassA.h", 13, 11, "ClassA.cc", 36, 1); // void publicFoo(const ClassA &a);
        performTest("ClassA.h", 15, 18, "ClassA.cc", 39, 12); // static void publicFooSt();

        // definition to declaration
        performTest("ClassA.cc", 24, 15, "ClassA.h", 9, 5); // void ClassA::publicFoo()
        performTest("ClassA.cc", 27, 15, "ClassA.h", 10, 5); // void ClassA::publicFoo(int a)
        performTest("ClassA.cc", 30, 15, "ClassA.h", 11, 5); // void ClassA::publicFoo(int a, double b)
        //TODO: performTest("ClassA.cc", 33, 15, "ClassA.h", 12, 5); // void ClassA::publicFoo(ClassA a)
        //TODO: performTest("ClassA.cc", 36, 15, "ClassA.h", 13, 5); // void ClassA::publicFoo(const ClassA &a)
        performTest("ClassA.cc", 39, 30, "ClassA.h", 15, 5); // /*static*/ void ClassA::publicFooSt()
    }

    public void testProtectedMethods() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 20, 11, "ClassA.cc", 42, 1); // void protectedFoo();
        performTest("ClassA.h", 21, 11, "ClassA.cc", 45, 1); // void protectedFoo(int a);
        performTest("ClassA.h", 22, 11, "ClassA.cc", 48, 1); // void protectedFoo(int a, double b);
        //TODO: performTest("ClassA.h", 23, 11, "ClassA.cc", 51, 1); // void protectedFoo(const ClassA* const ar[]);
        performTest("ClassA.h", 25, 18, "ClassA.cc", 54, 12); // static void protectedFooSt();

        // definition to declaration
        performTest("ClassA.cc", 42, 15, "ClassA.h", 20, 5); // void ClassA::protectedFoo()
        performTest("ClassA.cc", 45, 15, "ClassA.h", 21, 5); // void ClassA::protectedFoo(int a)
        performTest("ClassA.cc", 48, 15, "ClassA.h", 22, 5); // void ClassA::protectedFoo(int a, double b)
        //TODO: performTest("ClassA.cc", 51, 15, "ClassA.h", 23, 5); // void ClassA::protectedFoo(const ClassA* const ar[])
        performTest("ClassA.cc", 54, 30, "ClassA.h", 25, 5); // /*static*/ void ClassA::protectedFooSt()
    }

    // IZ103915 Hyperlink works wrong with private methods
    public void testPrivateMethods() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 28, 11, "ClassA.cc", 57, 1); // void privateFoo();
        performTest("ClassA.h", 29, 11, "ClassA.cc", 60, 1); // void privateFoo(int a);
        performTest("ClassA.h", 30, 11, "ClassA.cc", 63, 1); // void privateFoo(int a, double b);
        performTest("ClassA.h", 31, 11, "ClassA.cc", 66, 1); // void privateFoo(const ClassA *a);
        performTest("ClassA.h", 33, 18, "ClassA.cc", 69, 12); // static void privateFooSt();

        // definition to declaration
        performTest("ClassA.cc", 57, 15, "ClassA.h", 28, 5); // void ClassA::privateFoo()
        performTest("ClassA.cc", 60, 15, "ClassA.h", 29, 5); // void ClassA::privateFoo(int a)
        performTest("ClassA.cc", 63, 15, "ClassA.h", 30, 5); // void ClassA::privateFoo(int a, double b)
        performTest("ClassA.cc", 66, 15, "ClassA.h", 31, 5); // void ClassA::privateFoo(const ClassA *a)
        performTest("ClassA.cc", 69, 30, "ClassA.h", 33, 5); // /*static*/ void ClassA::privateFooSt()
    }

    public void testInitList() throws Exception {
        performTest("ClassA.cc", 8, 25, "ClassA.h", 46, 5); // privateMemberInt in "ClassA::ClassA() : privateMemberInt(1)"
    }

    public void testConstructors() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 7, 10, "ClassA.cc", 8, 1); // public ClassA();
        performTest("ClassA.h", 18, 10, "ClassA.cc", 12, 1); // protected ClassA(int a);
        performTest("ClassA.h", 27, 10, "ClassA.cc", 16, 1); // private ClassA(int a, double b);

        // definition to declaration
        performTest("ClassA.cc", 8, 10, "ClassA.h", 7, 5); // ClassA::ClassA()
        performTest("ClassA.cc", 12, 10, "ClassA.h", 18, 5); // ClassA::ClassA(int a)
        performTest("ClassA.cc", 16, 10, "ClassA.h", 27, 5); // ClassA::ClassA(int a, double b)
    }

    public void testDestructors() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 4, 15, "ClassA.cc", 20, 1); // ~ClassA() {

        // definition to declaration
        performTest("ClassA.cc", 20, 15, "ClassA.h", 4, 5); // ClassA::~ClassA() {
    }

    public void testIncludes() throws Exception {
        // check #include "ClassA.h" hyperlinks
        performTest("main.cc", 2, 12, "ClassA.h", -1, -1); // start of file ClassA.h
        performTest("ClassA.cc", 2, 12, "ClassA.h", -1, -1); // start of file ClassA.h
    }

    public void testOperators() throws Exception {
        // IZ#87543: Hyperlink doesn't work with overloaded operators

        // declaration do definition
        performTest("ClassA.h", 52, 15, "ClassA.cc", 74, 1); // ClassA& operator= (const ClassA& obj);
        performTest("ClassA.h", 54, 15, "ClassA.cc", 78, 1); // ClassA& operator+ (const ClassA& obj);
        performTest("ClassA.h", 56, 15, "ClassA.cc", 82, 1); // ClassA& operator- (const ClassA& obj);

        // definition to declaration
        performTest("ClassA.cc", 74, 20, "ClassA.h", 52, 5); // ClassA& ClassA::operator= (const ClassA& obj) {
        performTest("ClassA.cc", 78, 20, "ClassA.h", 54, 5); // ClassA& ClassA::operator+ (const ClassA& obj) {
        performTest("ClassA.cc", 82, 20, "ClassA.h", 56, 5); // ClassA& ClassA::operator- (const ClassA& obj) {
    }

    public void testGlobalFunctionGo() throws Exception {
        // IZ#84455 incorrect hyperlinks in case of global functions definition/declaration
        // declaration do definition
        performTest("main.cc", 4, 6, "main.cc", 8, 1); // void go();
        performTest("main.cc", 5, 6, "main.cc", 12, 1); // void go(int a);
        performTest("main.cc", 6, 6, "main.cc", 16, 1); // void go(int a, double b);

        // definition to declaration
        performTest("main.cc", 8, 6, "main.cc", 4, 1); // void go() {
        performTest("main.cc", 12, 6, "main.cc", 5, 1); // void go(int a) {
        performTest("main.cc", 16, 6, "main.cc", 6, 1); // void go(int a, double b) {

        // usage to definition
        performTest("main.cc", 24, 6, "main.cc", 8, 1); // go();
        performTest("main.cc", 25, 6, "main.cc", 12, 1); // go(1);
        performTest("main.cc", 26, 6, "main.cc", 16, 1); // go(i, 1.0);
    }

    public void testMainParamsUsing() throws Exception {
        // IZ#76195: incorrect hyperlink for "argc" in welcome.cc of Welcome project
        // usage to parameter
        performTest("main.cc", 32, 10, "main.cc", 20, 10); // f (argc > 1) {
        performTest("main.cc", 34, 30, "main.cc", 20, 10); // for (int i = 1; i < argc; i++) {
        performTest("main.cc", 35, 35, "main.cc", 20, 20); // cout << i << ": " << argv[i] << "\n";
    }

    public void testClassMethodRetClassAPtr() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 59, 15, "ClassA.cc", 86, 1); // ClassA* classMethodRetClassAPtr();
        // class name in return type to class
        performTest("ClassA.h", 59, 10, "ClassA.h", 2, 1); // ClassA* classMethodRetClassAPtr();

        // definition to declaration
        performTest("ClassA.cc", 86, 20, "ClassA.h", 59, 5); // ClassA* ClassA::classMethodRetClassAPtr() {
        // class name in return type to class
        performTest("ClassA.cc", 86, 5, "ClassA.h", 2, 1); // ClassA* ClassA::classMethodRetClassAPtr() {
        // class name in method name to class
        performTest("ClassA.cc", 86, 10, "ClassA.h", 2, 1); // ClassA* ClassA::classMethodRetClassAPtr() {
    }

    public void testClassMethodRetClassARef() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 60, 20, "ClassA.cc", 90, 1); // const ClassA& classMethodRetClassARef();
        // class name in return type to class
        performTest("ClassA.h", 60, 15, "ClassA.h", 2, 1); // const ClassA& classMethodRetClassARef();

        // definition to declaration
        performTest("ClassA.cc", 90, 25, "ClassA.h", 60, 5); // const ClassA& ClassA::classMethodRetClassARef() {
        // class name in return type to class
        performTest("ClassA.cc", 90, 10, "ClassA.h", 2, 1); // const ClassA& ClassA::classMethodRetClassARef() {
        // class name in method name to class
        performTest("ClassA.cc", 90, 20, "ClassA.h", 2, 1); // const ClassA& ClassA::classMethodRetClassARef() {
    }

    public void testClassMethodRetMyInt() throws Exception {
        // declaration do definition
        performTest("ClassA.h", 64, 20, "ClassA.cc", 94, 1); // myInt classMethodRetMyInt();
        // type name in return type to typedef
        performTest("ClassA.h", 64, 7, "ClassA.h", 1, 1); // myInt classMethodRetMyInt();

        // definition to declaration
        performTest("ClassA.cc", 94, 25, "ClassA.h", 64, 5); // myInt ClassA::classMethodRetMyInt() {
        // type name in return type to typedef
        performTest("ClassA.cc", 94, 5, "ClassA.h", 1, 1); // myInt ClassA::classMethodRetMyInt() {
        // class name in method name to class
        performTest("ClassA.cc", 94, 10, "ClassA.h", 2, 1); // myInt ClassA::classMethodRetMyInt() {
    }

    public void testFriendFuncHyperlink() throws Exception {
        // from declaration to definition
        performTest("ClassA.h", 72, 20, "ClassA.cc", 107, 1); // friend void friendFoo();
        // from definition to declaration
        performTest("ClassA.cc", 107, 10, "ClassA.h", 72, 5); // void friendFoo() {
        // from usage to definition
        performTest("main.cc", 17, 10, "ClassA.cc", 107, 1); // friendFoo();
    }

    public void testFriendOperatorHyperlink() throws Exception {
        // from declaration to definition
        performTest("ClassA.h", 69, 25, "ClassA.cc", 102, 1); // friend ostream& operator<< (ostream&, const ClassA&);
        // from definition to declaration
        performTest("ClassA.cc", 102, 15, "ClassA.h", 69, 5); // ostream& operator <<(ostream& output, const ClassA& item) {
    }

    public void testIZ136102() throws Exception {
        // from usage to definition
        performTest("IZ136102.cc", 15, 8, "IZ136102.cc", 6, 12);
    }

    public void testIZ136140() throws Exception {
        // from usage to definition
        performTest("IZ136140.cc", 16, 11, "IZ136140.cc", 11, 5);
        performTest("IZ136140.cc", 17, 12, "IZ136140.cc", 11, 5);
    }

    public void testIZ136894() throws Exception {
        performTest("main.cc", 67, 35, "main.cc", 59, 5); // itd_state in state->ehci_itd_pool_addr->itd_state;
        performTest("main.cc", 68, 35, "main.cc", 59, 5); // itd_state in state->ehci_itd_pool_addr[i].itd_state;
        performTest("main.cc", 70, 19, "main.cc", 59, 5); // itd_state in pool_addr[i].itd_state;
        performTest("main.cc", 71, 35, "main.cc", 59, 5); // itd_state in state->ehci_itd_pool_addr[0].itd_state;
        performTest("main.cc", 72, 19, "main.cc", 59, 5); // itd_state in pool_addr[0].itd_state;
    }

    public void testIZ136975() throws Exception {
        performTest("iz136975.cc", 18, 14, "iz136975.cc", 13, 5); // OP in if (OP::Release(*static_cast<SP*> (this))) {
        performTest("iz136975.cc", 19, 14, "iz136975.cc", 12, 5); // SP in SP::Destroy();
        performTest("iz136975.cc", 18, 39, "iz136975.cc", 12, 5); // SP in if (OP::Release(*static_cast<SP*> (this))) {
        performTest("iz136975.cc", 23, 10, "iz136975.cc", 15, 5); // PointerType in PointerType operator->() {
    }

    public void testIZ137483() throws Exception {
        performTest("main.cc", 75, 39, "main.cc", 75, 34);
        performTest("main.cc", 75, 24, "main.cc", 75, 15);
        performTest("main.cc", 76, 15, "main.cc", 75, 34);
        performTest("main.cc", 77, 18, "main.cc", 75, 15);
    }

    public void testIZ137798() throws Exception {
        performTest("IZ137799and137798.h", 2, 15, "IZ137799and137798.h", 2, 1);
        performTest("IZ137799and137798.h", 19, 15, "IZ137799and137798.h", 2, 1);
        performTest("IZ137799and137798.h", 3, 15, "IZ137799and137798.h", 3, 1);
        performTest("IZ137799and137798.h", 16, 25, "IZ137799and137798.h", 3, 1);
    }

    public void testIZ137799() throws Exception {
        performTest("IZ137799and137798.h", 12, 21, "IZ137799and137798.h", 12, 13);
        performTest("IZ137799and137798.h", 13, 21, "IZ137799and137798.h", 13, 13);
        performTest("IZ137799and137798.h", 14, 21, "IZ137799and137798.h", 14, 13);
        performTest("IZ137799and137798.h", 15, 21, "IZ137799and137798.h", 15, 13);
        performTest("IZ137799and137798.h", 16, 21, "IZ137799and137798.h", 16, 13);
        performTest("IZ137799and137798.h", 17, 21, "IZ137799and137798.h", 17, 13);
        performTest("IZ137799and137798.h", 18, 21, "IZ137799and137798.h", 18, 13);
    }

    public void testNestedStructAndVar() throws Exception {
        performTest("IZ137799and137798.h", 19, 12, "IZ137799and137798.h", 19, 11);
        performTest("IZ137799and137798.h", 11, 17, "IZ137799and137798.h", 11, 9);
    }

    public void testMethodPrefix() throws Exception {
        // IZ#125760: Hyperlink works wrongly if user created
        // method without declaration in class
        performNullTargetTest("IZ125760.cpp", 6, 10);
    }

    public void testStaticFields() throws Exception {
        // IZ114002: Hyperlink does not go from static field definition to its declaration

        // from definition to declaration
        performTest("ClassA.cc", 4, 30, "ClassA.h", 38, 5); // publicMemberStInt in int ClassA::publicMemberStInt = 1;
        performTest("ClassA.cc", 5, 30, "ClassA.h", 43, 5); // protectedMemberStInt in int ClassA::protectedMemberStInt = 2;
        performTest("ClassA.cc", 6, 30, "ClassA.h", 48, 5); // privateMemberStInt in int ClassA::privateMemberStInt = 3;

        // from declaration to definition
        performTest("ClassA.h", 38, 20, "ClassA.cc", 4, 12); // publicMemberStInt in ClassA
        performTest("ClassA.h", 43, 20, "ClassA.cc", 5, 12); // protectedMemberStInt in ClassA
        performTest("ClassA.h", 48, 20, "ClassA.cc", 6, 12); // privateMemberStInt in ClassA

        // from usage to definition
        performTest("ClassA.cc", 108, 25, "ClassA.cc", 4, 12); // publicMemberStInt in int i = ClassA::publicMemberStInt;
    }

    public void testGoToDeclarationForTemplateMethods() throws Exception {
        performTest("templateMethods.cc", 15, 8, "templateMethods.cc", 3, 5); //A in C2
        performTest("templateMethods.cc", 22, 8, "templateMethods.cc", 4, 5); //B in C2
        performTest("templateMethods.cc", 33, 5, "templateMethods.cc", 8, 5); //A in D2
    }

    public void testGoToDefinitionForTemplateMethods() throws Exception {
        performTest("templateMethods.cc", 3, 35, "templateMethods.cc", 12, 1); //A in C2
        performTest("templateMethods.cc", 4, 9, "templateMethods.cc", 20, 1); //B in C2
        performTest("templateMethods.cc", 8, 28, "templateMethods.cc", 31, 1); //A in D2
    }

    public void test_iz_143285_nested_classifiers() throws Exception {
        // IZ#143285 Unresolved reference to typedefed class' typedef
        performTest("IZ143285_nested_classifiers.cc", 11, 33, "IZ143285_nested_classifiers.cc", 8, 17);
        performTest("IZ143285_nested_classifiers.cc", 15, 28, "IZ143285_nested_classifiers.cc", 3, 9);
        performTest("IZ143285_nested_classifiers.cc", 16, 16, "IZ143285_nested_classifiers.cc", 7, 13);
    }

    public void testStdVector() throws Exception {
        // IZ#141105 Code model can not resolve type for vector[i]
        performTest("IZ141105_std_vector.cc", 20, 11, "IZ141105_std_vector.cc", 3, 5);
    }

    public void testTemplateParameterPriority() throws Exception {
        // IZ#144050 : inner type should have priority over global one
        performTest("templateParameters.h", 96, 5, "templateParameters.h", 95, 11);
    }

    public void testInnerTypePriority() throws Exception {
        // IZ#144050 : inner type should have priority over global one
        performTest("IZ144050.cc", 8, 43, "IZ144050.cc", 8, 5);
        performTest("IZ144050.cc", 12, 24, "IZ144050.cc", 8, 5);
    }

    public void testIZ144062() throws Exception {
        // IZ#144062 : inner class members are not resolved
        performTest("IZ144062.cc", 3, 13, "IZ144062.cc", 3, 5);
        performTest("IZ144062.cc", 4, 14, "IZ144062.cc", 4, 9);
        performTest("IZ144062.cc", 5, 20, "IZ144062.cc", 5, 9);
        performTest("IZ144062.cc", 6, 20, "IZ144062.cc", 6, 9);
        performTest("IZ144062.cc", 8, 17, "IZ144062.cc", 8, 13);
        performTest("IZ144062.cc", 9, 18, "IZ144062.cc", 9, 13);
        performTest("IZ144062.cc", 10, 11, "IZ144062.cc", 10, 11);
        performTest("IZ144062.cc", 11, 15, "IZ144062.cc", 11, 9);
        performTest("IZ144062.cc", 11, 22, "IZ144062.cc", 11, 18);
        performTest("IZ144062.cc", 12, 17, "IZ144062.cc", 12, 13);
        performTest("IZ144062.cc", 12, 21, "IZ144062.cc", 11, 18);
        performTest("IZ144062.cc", 14, 8, "IZ144062.cc", 14, 7);
        performTest("IZ144062.cc", 15, 16, "IZ144062.cc", 15, 5);
        performTest("IZ144062.cc", 15, 18, "IZ144062.cc", 15, 18);
    }

    public void testIZ144679() throws Exception {
        // IZ#144679 : IDE highlights static constants in class as wrong code
        performTest("IZ144679.cc", 11, 21, "IZ144679.cc", 10, 1);
        performTest("IZ144679.cc", 12, 22, "IZ144679.cc", 11, 1);
    }

    public void testIZ145077() throws Exception {
        // IZ#145077: Internal C++ compiler cannot resolve inner classes
        performTest("iz145077.cc", 128, 17, "iz145077.cc", 47, 9);
        performTest("iz145077.cc", 43, 50, "iz145077.cc", 33, 9);
        performTest("iz145077.cc", 44, 60, "iz145077.cc", 112, 5);
        performTest("iz145077.cc", 163, 30, "iz145077.cc", 142, 9);
        performTest("iz145077.cc", 172, 22, "iz145077.cc", 142, 9);
        performTest("iz145077.cc", 164, 30, "iz145077.cc", 143, 9);
        performTest("iz145077.cc", 173, 22, "iz145077.cc", 143, 9);
    }

    public void testIZ145071() throws Exception {
        // IZ#145071 : forward declarations marked as error
        performTest("IZ145071.cc", 4, 23, "IZ145071.cc", 4, 13);
    }

    public void testIZ147795() throws Exception {
        // IZ#147795 : Code completion issue when using '()'
        performTest("IZ147795.cc", 10, 13, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 11, 17, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 12, 15, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 13, 14, "IZ147795.cc", 5, 9);
        performTest("IZ147795.cc", 13, 27, "IZ147795.cc", 5, 9);
    }

    public void testIZ148022() throws Exception {
        // IZ#148022 : Unable to resolve outside definition of a private member class
        performTest("IZ148022.cc", 10, 14, "IZ148022.cc", 5, 9);
        performTest("IZ148022.cc", 11, 14, "IZ148022.cc", 7, 9);
    }

    public void testIZ148929() throws Exception {
        // IZ#148929 : Unable to resolve destructor of a nested class
        performTest("iz148929.cc", 13, 7, "iz148929.cc", 16, 1);
        performTest("iz148929.cc", 16, 9, "iz148929.cc", 13, 5);
    }

    public void testTypenameInTemplateParameterDeclaration() throws Exception {
        // IZ#151957: 9 parser's errors in boost 1.36
        performTest("templateParameters.h", 116, 39, "templateParameters.h", 116, 1);
        performTest("templateParameters.h", 120, 15, "templateParameters.h", 116, 1);
    }

    public void testIZ151955() throws Exception {
        // IZ#151955: java.lang.StackOverflowError in boost 1.36
        performTest("iz151955.cc", 13, 35, "iz151955.cc", 10, 5);
    }

    public void testIZ154112() throws Exception {
        // IZ#154112: Unresolved instantiations of template
        performTest("iz154112.cc", 17, 13, "iz154112.cc", 7, 5);
        performTest("iz154112.cc", 18, 13, "iz154112.cc", 7, 5);
        performTest("iz154112.cc", 19, 19, "iz154112.cc", 13, 5);
        performTest("iz154112.cc", 20, 19, "iz154112.cc", 13, 5);
    }

    public void testIZ154594() throws Exception {
        // IZ#154594: completion fails on expressions with keyword template
        performTest("iz154594.cc", 15, 32, "iz154594.cc", 12, 5);
    }

    public void testIZ154775() throws Exception {
        // IZ#154775: Unresolved inner type of instantiation
        performTest("iz154775.cc", 14, 20, "iz154775.cc", 9, 5);

        performTest("iz154775.cc", 31, 24, "iz154775.cc", 23, 5);
        performTest("iz154775.cc", 32, 24, "iz154775.cc", 27, 5);
    }

    public void testIZ154778() throws Exception {
        //IZ#154778: Completion fails on gt operator
        performTest("iz154778.cc", 9, 18, "iz154778.cc", 5, 5);
    }

    public void testIZ154789() throws Exception {
        //IZ#154789: Completion fails on macros
        performTest("iz154789.cc", 15, 22, "iz154789.cc", 5, 5);
    }

    public void testIZ154781() throws Exception {
        //IZ#154781: Completion fails on const
        performTest("iz154781.cc", 14, 20, "iz154781.cc", 5, 5);
        performTest("iz154781.cc", 15, 20, "iz154781.cc", 5, 5);
    }

    public void testIZ151592() throws Exception {
        //IZ#151592: wrong hyperlink on derived class member
        performTest("iz151592.cc", 9, 14, "iz151592.cc", 9, 5);
        performTest("iz151592.cc", 10, 14, "iz151592.cc", 10, 5);
    }

    public void testIZ148035() throws Exception {
        //IZ#148035 : Code assistance errors from instances of complex template classes
        performTest("iz148035.cc", 20, 16, "iz148035.cc", 13, 5);
    }

    public void testIZ151591() throws Exception {
        // IZ#151591 : Unresolved protected member of parent class in inner class of child class
        performTest("iz151591.cc", 13, 17, "iz151591.cc", 3, 5);
    }

    public void testIZ151763() throws Exception {
        // IZ#151763 : Unresolved usage of operator ()
        performTest("iz151763.cc", 15, 9, "iz151763.cc", 2, 5);
        performTest("iz151763.cc", 18, 11, "iz151763.cc", 2, 5);
    }

    public void testIZ154792() throws Exception {
        // IZ#154792 : Completion fails on question mark
        performTest("iz154792.cc", 10, 32, "iz154792.cc", 5, 9);
    }

    public void testIZ151765() throws Exception {
        // IZ#151765 : Unresolved shifted pointers
        performTest("iz151765.cc", 11, 12, "iz151765.cc", 3, 5);
    }

    public void testIZ160637() throws Exception {
        // IZ#160637 : space between destructor and parens makes destructor unresolved
        performTest("iz160637.cc", 9, 25, "iz160637.cc", 5, 5);
    }

    public void testIZ151043() throws Exception {
        // IZ#151043 : Unresolved dereferencing
        performTest("iz151043.cc", 8, 11, "iz151043.cc", 3, 5);
        performTest("iz151043.cc", 9, 13, "iz151043.cc", 3, 5);
        performTest("iz151043.cc", 10, 25, "iz151043.cc", 3, 5);
    }

    public void testIZ160677() throws Exception {
        // IZ#160677 : Unresolved members of global variables in global context
        performTest("iz160677.cc", 6, 14, "iz160677.cc", 2, 5);
    }

    public void testIZ104943() throws Exception {
        // IZ#104943 : Hyperlink works wrong on overloaded methods with pointer
        performTest("iz104943.cc", 3, 12, "iz104943.cc", 8, 1);
        performTest("iz104943.cc", 4, 12, "iz104943.cc", 12, 1);
        performTest("iz104943.cc", 5, 12, "iz104943.cc", 16, 1);
        performTest("iz104943.cc", 8, 20, "iz104943.cc", 3, 5);
        performTest("iz104943.cc", 12, 20, "iz104943.cc", 4, 5);
        performTest("iz104943.cc", 16, 20, "iz104943.cc", 5, 5);
    }

    public void testIZ104945() throws Exception {
        // IZ#104945 : Hyperlink works wrong on overloaded methods with const
        performTest("iz104945.cc", 3, 12, "iz104945.cc", 10, 1);
        performTest("iz104945.cc", 4, 12, "iz104945.cc", 14, 1);
        performTest("iz104945.cc", 5, 12, "iz104945.cc", 18, 1);
        performTest("iz104945.cc", 6, 12, "iz104945.cc", 22, 1);
        performTest("iz104945.cc", 7, 12, "iz104945.cc", 26, 1);
        performTest("iz104945.cc", 10, 20, "iz104945.cc", 3, 5);
        performTest("iz104945.cc", 14, 20, "iz104945.cc", 4, 5);
        performTest("iz104945.cc", 18, 20, "iz104945.cc", 5, 5);
        performTest("iz104945.cc", 22, 20, "iz104945.cc", 6, 5);
        performTest("iz104945.cc", 26, 20, "iz104945.cc", 7, 5);
    }

    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return ClassMembersHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName);
        }

        public void allFailedTests() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.h", 12, 11, "ClassA.cc", 33, 1); // void publicFoo(ClassA a);
            performTest("ClassA.h", 13, 11, "ClassA.cc", 36, 1); // void publicFoo(const ClassA &a);

            performTest("ClassA.h", 23, 11, "ClassA.cc", 51, 1); // void protectedFoo(const ClassA* const ar[]);

            performTest("ClassA.cc", 33, 15, "ClassA.h", 12, 5); // void ClassA::publicFoo(ClassA a)
            performTest("ClassA.cc", 36, 15, "ClassA.h", 13, 5); // void ClassA::publicFoo(const ClassA &a)

            performTest("ClassA.cc", 51, 15, "ClassA.h", 23, 5); // void ClassA::protectedFoo(const ClassA* const ar[])
        }

        public void test1() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.h", 12, 11, "ClassA.cc", 33, 1); // void publicFoo(ClassA a);
        }
        public void test2() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.h", 13, 11, "ClassA.cc", 36, 1); // void publicFoo(const ClassA &a);
        }
        public void test3() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.h", 23, 11, "ClassA.cc", 51, 1); // void protectedFoo(const ClassA* const ar[]);
        }
        public void test4() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.cc", 33, 15, "ClassA.h", 12, 5); // void ClassA::publicFoo(ClassA a)
        }
        public void test5() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.cc", 36, 15, "ClassA.h", 13, 5); // void ClassA::publicFoo(const ClassA &a)
        }
        public void test6() throws Exception {
            // TODO: doesn't work yet
            performTest("ClassA.cc", 51, 15, "ClassA.h", 23, 5); // void ClassA::protectedFoo(const ClassA* const ar[])
        }

        public void testMyInnerInt1() throws Exception {
            // type name in return type to typedef
            performTest("ClassA.h", 66, 10, "ClassA.h", 62, 5); // myInnerInt classMethodRetMyInnerInt();
        }

        public void testMyInnerInt2() throws Exception {
            // type name in return type to typedef
            performTest("ClassA.cc", 98, 5, "ClassA.h", 62, 5); // myInnerInt ClassA::classMethodRetMyInnerInt() {
        }

        public void testOverloadFuncs() throws Exception {
            performTest("ClassB.h", 18, 15, "ClassB.h", 18, 5); //void method(int a);
            performTest("ClassB.h", 20, 15, "ClassB.h", 20, 5); //void method(const char*);
            performTest("ClassB.h", 12, 15, "ClassB.h", 22, 5); //void method(char*, double);
            performTest("ClassB.h", 24, 15, "ClassB.h", 24, 5); //void method(char*, char*);
        }

        public void testIZ145037() throws Exception {
            // IZ#145037: "operator string" defintion incorrectly resolved
            performTest("IZ145037_conversion_operators.cc", 20, 22, "IZ145037_conversion_operators.cc", 10, 9);
            performTest("IZ145037_conversion_operators.cc", 38, 22, "IZ145037_conversion_operators.cc", 28, 9);
        }
    }

}

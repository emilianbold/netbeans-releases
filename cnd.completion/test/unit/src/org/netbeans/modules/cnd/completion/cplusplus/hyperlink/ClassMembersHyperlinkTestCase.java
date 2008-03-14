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

    public static class Failed extends HyperlinkBaseTestCase {
        
        protected Class getTestCaseDataClass() {
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
    }
        
}

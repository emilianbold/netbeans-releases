/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        performTest("main.cc", 2, 12, "ClassA.h", 1, 1); // start of file ClassA.h
        performTest("ClassA.cc", 2, 12, "ClassA.h", 1, 1); // start of file ClassA.h
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
    }
        
}

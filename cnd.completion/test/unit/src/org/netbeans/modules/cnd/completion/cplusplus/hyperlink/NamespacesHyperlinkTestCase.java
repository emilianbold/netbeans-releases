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
public class NamespacesHyperlinkTestCase extends HyperlinkBaseTestCase {
    
    public NamespacesHyperlinkTestCase(String testName) {
        super(testName);
        //System.setProperty("cnd.modelimpl.trace.registration", "true");
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
        performTest("main.cc", 68, 10, "file.cc", 63, 5); //myCout in S1::myCout;
        performTest("main.cc", 69, 20, "file.cc", 63, 5); //myCout in using S1::myCout;
        performTest("main.cc", 70, 6, "file.cc", 63, 5); //myCout;
    }   
    
    public void testUsingNS2() throws Exception {
        // IZ#106772: incorrect resolving of using directive
        performTest("main.cc", 51, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 52, 6, "file.cc", 23, 9); //boo();
        performTest("main.cc", 53, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
    } 

    public void testUsingDirectivesS2() throws Exception {
        // IZ#106772: incorrect resolving of using directive
        performTest("main.cc", 60, 6, "file.h", 12, 9); //clsS2 in clsS2 c2;
        performTest("main.cc", 62, 6, "file.cc", 21, 9); //var2 = 10;
        performTest("main.cc", 64, 6, "file.cc", 23, 9); //boo();
    }        

    
    public static class Failed extends HyperlinkBaseTestCase {
        
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

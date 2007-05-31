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
    
    public static class Failed extends HyperlinkBaseTestCase {
        
        protected Class getTestCaseDataClass() {
            return NamespacesHyperlinkTestCase.class;
        }
    
        public Failed(String testName) {
            super(testName);
        }
    
        public void testS1FooDefFoo() throws Exception {
            performTest("file.cc", 11, 10, "file.cc", 7, 5); // foo();
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS1FooDefVar1() throws Exception {
            performTest("file.cc", 12, 10, "file.cc", 5, 5); // var1
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS1FooDefS2() throws Exception {
            performTest("file.cc", 16, 10, "file.cc", 20, 5); // S2 in S2::boo();
            performTest("file.cc", 17, 10, "file.cc", 20, 5); // S2 in S2::var2
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS1FooDefS2Boo() throws Exception {
            performTest("file.cc", 16, 14, "file.cc", 23, 9); // boo in S2::boo();
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS1FooDefS2Var2() throws Exception {
            performTest("file.cc", 17, 14, "file.cc", 21, 9); // var2 in S2::var2
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS2BooDefFoo() throws Exception {
            performTest("file.cc", 27, 14, "file.cc", 7, 5); // foo();
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS2BooDefVar1() throws Exception {
            performTest("file.cc", 28, 14, "file.cc", 5, 5); // var1
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS2BooDefS2() throws Exception {
            performTest("file.cc", 32, 14, "file.cc", 20, 5); // S2 in S2::boo();
            performTest("file.cc", 33, 14, "file.cc", 20, 5); // S2 in S2::var2
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS2BooDefS2Boo() throws Exception {
            performTest("file.cc", 32, 18, "file.cc", 23, 9); // boo in S2::boo();
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS2BooDefS2Var2() throws Exception {
            performTest("file.cc", 33, 18, "file.cc", 21, 9); // var2 in S2::var2
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS2BooDefBoo() throws Exception {
            performTest("file.cc", 34, 14, "file.cc", 23, 9); // boo
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
        
        public void testS2BooDefVar2() throws Exception {
            performTest("file.cc", 34, 14, "file.cc", 21, 9); // var2
            //failByBug(84115, "IZ#84115Completion and hyperlink works incorrectly with namespace elements");
        }
    }
}

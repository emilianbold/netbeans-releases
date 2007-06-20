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
public class BasicHyperlinkTestCase extends HyperlinkBaseTestCase {
    public BasicHyperlinkTestCase(String testName) {
        super(testName);
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

    ////////////////////////////////////////////////////////////////////////////
    // K&R style
    
    public void testKRFuncParamUsage() throws Exception {
        performTest("kr.c", 12, 15, "kr.c", 10, 1); // index in 'return index;'
    }

    public void testKRFooDeclDefUsage() throws Exception {
        performTest("kr.c", 2, 6, "kr.c", 9, 1); // int foo(); -> int foo(index)
        performTest("kr.c", 9, 6, "kr.c", 2, 1); // int foo(index) -> int foo();
        performTest("kr.c", 15, 6, "kr.c", 17, 1); // int boo(); -> int boo(int i)
        performTest("kr.c", 17, 6, "kr.c", 15, 1); // int boo(int i) -> int boo();
    }

    public static class Failed extends HyperlinkBaseTestCase {
        
        protected Class getTestCaseDataClass() {
            return BasicHyperlinkTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName);
        }
        
        public void testKRFuncParamDecl() throws Exception {
            performTest("kr.c", 9, 10, "kr.c", 10, 1); // index in 'int foo(index)'
        }     
        
    }
}

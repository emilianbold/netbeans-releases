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

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 * test cases for resolving completion in friends context
 * @author Vladimir Voskresensky
 */
public class FriendTestCase extends CompletionBaseTestCase {
    
    public FriendTestCase(String testName) {
        super(testName, true);
    }
    
    public void testInFriendFuncVarA() throws Exception {
        super.performTest("file.cc", 14, 5, "a.");
    }
    
    public void testInFriendFuncVarB() throws Exception {
        super.performTest("file.cc", 14, 5, "b.");
    }
    
    public void testInFriendFuncVarD() throws Exception {
        super.performTest("file.cc", 14, 5, "d.");
    }     
        
    public void testInFriendFuncVarASt() throws Exception {
        super.performTest("file.cc", 14, 5, "ClassA::");
    }
    
    public void testInFriendFuncVarBSt() throws Exception {
        super.performTest("file.cc", 14, 5, "ClassB::");
    }

    public void testInFriendFuncVarDSt() throws Exception {
        super.performTest("file.cc", 14, 5, "ClassD::");
    }
        
    public void testInFriendCClassVarE() throws Exception {
        super.performTest("file.cc", 19, 5, "e.");
    }
            
    public void testInFriendCClassVarESt() throws Exception {
        super.performTest("file.cc", 19, 5, "ClassE::");
    }

    public void testInFriendCClassVarA() throws Exception {
        super.performTest("file.cc", 7, 5, "a.");
    }
    
    public void testInFriendCClassVarB() throws Exception {
        super.performTest("file.cc", 7, 5, "b.");
    }    
    
    public void testInFriendCClassVarASt() throws Exception {
        super.performTest("file.cc", 7, 5, "ClassA::");
    }
    
    public void testInFriendCClassVarBSt() throws Exception {
        super.performTest("file.cc", 7, 5, "ClassB::");
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        @Override
        protected Class getTestCaseDataClass() {
            return FriendTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }

        public void testOK() {
            
        }
    
    }    
}

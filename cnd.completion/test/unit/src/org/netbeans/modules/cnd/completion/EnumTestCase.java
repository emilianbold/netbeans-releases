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
 *
 * @author Vladimir Voskresensky
 */
public class EnumTestCase extends CompletionBaseTestCase {
    
    public EnumTestCase(String testName) {
        super(testName, true);
    }
    
        
    public void testClassAEnumFun() throws Exception {
        super.performTest("file.cc", 16, 5, "ClassA::");
    }  
    
    
    public void testClassAaPubFun() throws Exception {
        super.performTest("file.cc", 10, 5, "ClassA::");
    }

    public void testVarAaPubFun() throws Exception {
        super.performTest("file.cc", 10, 5, "this->");
    }
    
    public void testEnumInFun() throws Exception {
        super.performTest("file.cc", 16, 5);
    }
    
    public void testClassAEnumeratorsInFun() throws Exception {
        super.performTest("file.cc", 20, 5, "aa.");
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        @Override
        protected Class getTestCaseDataClass() {
            return EnumTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }

        public void testOK() {
            
        }
    }    
}

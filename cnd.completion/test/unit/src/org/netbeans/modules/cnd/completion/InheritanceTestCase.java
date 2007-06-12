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
public class InheritanceTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of InheritanceTestCase
     */
    public InheritanceTestCase(String testName) {
        super(testName, true);
    }
    
    /////////////////////////////////////////////////////////////////
    // void ClassA::aPubFun() {
    
    public void testClassAbPubFunVarA() throws Exception {
        performTest("file.cc", 10, 5, "a.");
    }
    
    
    public void testClassAbPubFunVarC() throws Exception {
        performTest("file.cc", 10, 5, "c.");
    }
        
    public void testClassAbPubFunVarE() throws Exception {
        performTest("file.cc", 10, 5, "e.");
    }    
    ///////////////////////////////////////////////////////////////////
    // void ClassB::bProtFun() {
    
    public void testClassBbProtFunVarB() throws Exception {
        performTest("file.cc", 19, 5, "b.");
    }
    
    public void testClassBbProtFunVarC() throws Exception {
        performTest("file.cc", 19, 5, "c.");
    }
    
    public void testClassBbProtFunVarE() throws Exception {
        performTest("file.cc", 19, 5, "e.");
    }
    
    ////////////////////////////////////////////////////////////////////
    // void ClassC::cPrivFun() {
    
    public void testClassCcPrivFunVarA() throws Exception {
        performTest("file.cc", 28, 5, "a.");
    }
    
    public void testClassCcPrivFunVarB() throws Exception {
        performTest("file.cc", 28, 5, "b.");
    }
    
    public void testClassCcPrivFunVarC() throws Exception {
        performTest("file.cc", 28, 5, "c.");
    }
    
    ////////////////////////////////////////////////////////////////////
    // void ClassD::dPubFun() {
    
    public void testClassDdPubFunVarD() throws Exception {
        performTest("file.cc", 37, 5, "d.");
    }
    
    public void testClassDdPubFunVarE() throws Exception {
        performTest("file.cc", 37, 5, "e.");
    }
    
    ////////////////////////////////////////////////////////////////////
    // void ClassE::ePubFun() {
    
    public void testClassEePubFunVarA() throws Exception {
        performTest("file.cc", 46, 5, "a.");
    }
    
    public void testClassEePubFunVarB() throws Exception {
        performTest("file.cc", 46, 5, "b.");
    }
    
    public void testClassEePubFunVarD() throws Exception {
        performTest("file.cc", 46, 5, "d.");
    }
    
    public void testClassEePubFunVarE() throws Exception {
        performTest("file.cc", 46, 5, "e.");
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        protected Class getTestCaseDataClass() {
            return InheritanceTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }
        
        /////////////////////////////////////////////////////////////////
        // void ClassA::aPubFun() {
        
        // TODO: failed test
        public void testClassAbPubFunVarB() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 10, 5, "b.");
        }
        
        // TODO: failed test
        public void testClassAbPubFunVarD() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 10, 5, "d.");
        }
        
        ///////////////////////////////////////////////////////////////////
        // void ClassB::bProtFun() {
        
        // TODO: failed test
        public void testClassBbProtFunVarA() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 19, 5, "a.");
        }
        
        // TODO: failed test
        public void testClassBbProtFunVarD() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 19, 5, "d.");
        }
        
        ////////////////////////////////////////////////////////////////////
        // void ClassC::cPrivFun() {

        
        // TODO: failed test
        public void testClassCcPrivFunVarD() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 28, 5, "d.");
        }
        
        // TODO: failed test
        public void testClassCcPrivFunVarE() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 28, 5, "e.");
        }
        
        ////////////////////////////////////////////////////////////////////
        // void ClassD::dPubFun() {
        
        // TODO: failed test
        public void testClassDdPubFunVarA() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 37, 5, "a.");
        }
        
        // TODO: failed test
        public void testClassDdPubFunVarB() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 37, 5, "b.");
        }
        
        // TODO: failed test
        public void testClassDdPubFunVarC() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 37, 5, "c.");
        }
                
        ////////////////////////////////////////////////////////////////////
        // void ClassE::ePubFun() {
        
        // TODO: failed test
        public void testClassEePubFunVarC() throws Exception {
            //failByBug(84592, "IZ#84592 'Code Completion' and class inheritance");
            performTest("file.cc", 46, 5, "c.");
        }
        
    }
        
}

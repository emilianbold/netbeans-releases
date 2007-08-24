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
    
    public void testClassAaPubFunVarA() throws Exception {
        performTest("file.cc", 10, 5, "a.");
    }
    
    public void testClassAaPubFunVarB() throws Exception {
        performTest("file.cc", 10, 5, "b.");
    }  
    
    public void testClassAaPubFunVarC() throws Exception {
        performTest("file.cc", 10, 5, "c.");
    }       
    
    public void testClassAaPubFunVarD() throws Exception {
        performTest("file.cc", 10, 5, "d.");
    }
    
    public void testClassAaPubFunVarE() throws Exception {
        performTest("file.cc", 10, 5, "e.");
    }  
    
    public void testClassAaPubFunClassA() throws Exception {
        performTest("file.cc", 10, 5, "ClassA::");
    }     
    ///////////////////////////////////////////////////////////////////
    // void ClassB::bProtFun() {
    
    public void testClassBbProtFunVarA() throws Exception {
        performTest("file.cc", 19, 5, "a.");
    }
    
    public void testClassBbProtFunVarB() throws Exception {
        performTest("file.cc", 19, 5, "b.");
    }
    
    public void testClassBbProtFunVarC() throws Exception {
        performTest("file.cc", 19, 5, "c.");
    }
    
    public void testClassBbProtFunVarD() throws Exception {
        performTest("file.cc", 19, 5, "d.");
    }    

    public void testClassBbProtFunVarE() throws Exception {
        performTest("file.cc", 19, 5, "e.");
    }
    
    public void testClassBbProtFunClassA() throws Exception {
        performTest("file.cc", 19, 5, "ClassA::");
    }
    
    public void testClassBbProtFunClassB() throws Exception {
        performTest("file.cc", 19, 5, "ClassB::");
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

    public void testClassCcPrivFunVarD() throws Exception {
        performTest("file.cc", 28, 5, "d.");
    }
        
    public void testClassCcPrivFunVarE() throws Exception {
        performTest("file.cc", 28, 5, "e.");
    }  
    
    public void testClassCcPrivFunClassC() throws Exception {
        performTest("file.cc", 28, 5, "ClassC::");
    }     
    ////////////////////////////////////////////////////////////////////
    // void ClassD::dPubFun() {
    
    public void testClassDdPubFunVarA() throws Exception {
        performTest("file.cc", 37, 5, "a.");
    }

    public void testClassDdPubFunVarB() throws Exception {
        performTest("file.cc", 37, 5, "b.");
    }

    public void testClassDdPubFunVarC() throws Exception {
        performTest("file.cc", 37, 5, "c.");
    }
        
    public void testClassDdPubFunVarD() throws Exception {
        performTest("file.cc", 37, 5, "d.");
    }
    
    public void testClassDdPubFunVarE() throws Exception {
        performTest("file.cc", 37, 5, "e.");
    }
    
    public void testClassDdPubFunClassA() throws Exception {
        performTest("file.cc", 37, 5, "::ClassA::");
    }    
    
    public void testClassDdPubFunClassB() throws Exception {
        performTest("file.cc", 37, 5, "ClassB::");
    }    

    public void testClassDdPubFunClassC() throws Exception {
        performTest("file.cc", 37, 5, "ClassC::");
    }    
    
    public void testClassDdPubFunClassD() throws Exception {
        performTest("file.cc", 37, 5, "ClassD::");
    }     
    ////////////////////////////////////////////////////////////////////
    // void ClassE::ePubFun() {
    
    public void testClassEePubFunVarA() throws Exception {
        performTest("file.cc", 46, 5, "a.");
    }
    
    public void testClassEePubFunVarB() throws Exception {
        performTest("file.cc", 46, 5, "b.");
    }
        
    public void testClassEePubFunVarC() throws Exception {
        performTest("file.cc", 46, 5, "c.");
    }
        
    public void testClassEePubFunVarD() throws Exception {
        performTest("file.cc", 46, 5, "d.");
    }
    
    public void testClassEePubFunVarE() throws Exception {
        performTest("file.cc", 46, 5, "e.");
    }
        
    public void testClassEePubFunClassC() throws Exception {
        performTest("file.cc", 46, 5, "ClassC::");
    }    
    
    public void testClassEePubFunClassE() throws Exception {
        performTest("file.cc", 46, 5, "ClassE::");
    }     
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        @Override
        protected Class getTestCaseDataClass() {
            return InheritanceTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }       

        public void testOK() {
            
        }
    }
        
}

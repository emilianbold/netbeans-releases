/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import junit.framework.TestResult;

/**
 * Extension to MultiTestSuite class.
 * Performs automatic creation and running all testcases predefined with parameters.
 * @author Alexander Pepin */
public abstract class ParametricTestSuite extends MultiTestSuite{
    
    /**
     * Creates a new instance of ParametricTestSuite
     */
    public ParametricTestSuite() {
        super();
    }
    
    /**
     * Constructs a ParametricTestSuite with the given name.
     */
    public ParametricTestSuite(String name){
        super(name);
    }
    
    /**
     * Returns an array of testcases for the given parameter.
     */
    protected abstract ParametricTestCase[] cases(Object parameter);
    /**
     * Returns an array of parameters for this suite.
     */
    protected abstract Object[] getParameters();
    
    /**
     * Factory method returns a new instance of a testcases.
     * Overrides the basic method so that it's needless any more.
     */
    final protected MultiTestCase nextTestCase(){
        return null;
    }
    
    /**
     * Creates all testcases and runs them.
     */
    protected void runAllTests(TestResult result){
        for(Object parameter: getParameters()){
            for(ParametricTestCase testCase: cases(parameter)){
                if(testCase != null){
                    testCase.parametrize(parameter);
                    runTest(testCase, result);
                }
            }
        }
    }
    
}


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
 *
 * @author Alexander Pepin
 */
public abstract class MultiTestSuite extends NbTestSuite{
    
    /**
     * Creates a new instance of MultiTestSuite
     */
    public MultiTestSuite() {
        setName(this.getClass().getSimpleName());
    }
    
    /**
     * Constructs a MultiTestSuite with the given name.
     */
    public MultiTestSuite(String name){
        super(name);
    }
    
    /**
     * Factory method returns a new instance of a testcases.
     * Should return null if there are no more testcases to be executed.
     */
    protected abstract MultiTestCase nextTestCase();
    
    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        if(isPrepared()){
            runAllTests(result);
            cleanit();
        }
        if(gotFailed())
            createFailLog(result);
    }
    
    /**
     * Creates all testcases and runs them.
     */
    protected void runAllTests(TestResult result){
        MultiTestCase testCase = null;
        while((testCase = nextTestCase()) != null){
            runTest(testCase, result);
        }
    }
    
    //stubs
    
    /**
     * The method is called before executing tests.
     * Can be overridden to perform preliminary actions.
     */
    public void prepare(){}
    /**
     * The method is called after executing tests.
     * Can be overridden to perform closing actions.
     */
    public void cleanup(){}
    
//Safe preparation and cleaning
    private Throwable err = null;
    
    private final boolean isPrepared(){
        boolean result = false;
        try{
            prepare();
            result = true;
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while preparing for test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    private final void cleanit(){
        try{
            cleanup();
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while cleaning after test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
    }
    
    private final boolean gotFailed(){
        return err != null;
    }
    
    private final void createFailLog(TestResult result){
        //Create a new test case
        final String nameFailed = getName()+"FailLog";
        MultiTestCase dummy = new MultiTestCase(){
            public void execute(){}
        };
        dummy.setName(nameFailed);
        dummy.setError(err);
        runTest(dummy, result);
    }
    
}

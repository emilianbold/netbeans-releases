/*
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.xtest.testrunner;

import junit.framework.TestListener;
import junit.framework.TestSuite;
import junit.framework.TestResult;

/**
 * This interface allows XTest to control how the results
 * should be saved. Original JUnitTask was not able to
 * save results after every testcase was started/finished, 
 * hence when JVM crashed or was killed, all results from 
 * the current test suite were lost.
 *
 * @author  martin.brehovsky@sun.com
 */
public interface JUnitTestListener extends TestListener {

     /** start test suite event
     *  
     */
    public void startTestSuite(TestSuite suite);

    /** end test suite event
     * 
     */
    public void endTestSuite(TestSuite suite, TestResult suiteResult);

    /** Sets the stream the formatter is supposed to write its results to.
     * 
     */
    /*
    public void setOutput(java.io.OutputStream out);

     */
    /** sets the output file for results formatter (in the case output stream is not used)
     * 
     */
    /*
    public void setOutputFile(java.io.File outFile);
    */
    
}

/*
 *
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

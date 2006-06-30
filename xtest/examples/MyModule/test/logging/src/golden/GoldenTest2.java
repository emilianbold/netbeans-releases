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

package golden;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;


/** Example of golden file approach.
 * For each test case method there should exist <methodName>.pass file
 * containing expected output (AKA golden file). It resides in package
 * data.goldenfiles.GoldenTest2.

 */
public class GoldenTest2 extends NbTestCase {


    public GoldenTest2(String testName) {
        super(testName);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(GoldenTest2.class));
    }
    
    /** This method use golden files approach and you need to use compareReferenceFiles()
     * method explicitly to compare generated reference file against golden file.
     */
    public void testPart1() {
        log("message to log file testPart1.log");
        ref("Output to ref file called testPart1.ref");
        // here write body of the test
        boolean somethingWrong = false;
        // You can also use assertTrue() or fail() methods in the body of the test 
        // to indicate a failure
        if(somethingWrong) fail();
        assertTrue(!somethingWrong);
        log("mySpecialLog.log", "message to log file mySpecialLog.log");
        compareReferenceFiles();
    }
    
    
    /** This method doesn't use golden file approach */
    public void testPart2() throws Exception {
        log("message to log file testPart2.log");
        // here write body of the test
    }
    
}

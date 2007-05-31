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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.PrintStream;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public class TraceModelTestBase extends ModelImplBaseTestCase {
    private TestModelHelper helper;

    public TraceModelTestBase(String testName) {
        super(testName);
    }

    protected TraceModel getTraceModel() {
        assert helper != null;
        return helper.getTraceModel();
    }
    
    protected void performTest(String source) throws Exception {
        performTest(source, source+".dat", source+".err"); // NOI18N
    }

    protected void preSetUp() {
        // init flags needed for file model tests before creating TraceModel
    }
    
    protected void postSetUp() {
        // init flags needed for file model tests
    }
    
    @Override 
    protected void setUp() throws Exception {
        preSetUp();
        super.setUp();
        super.clearWorkDir();
        helper = new TestModelHelper();
        assertNotNull("Model must be valid", getTraceModel().getModel()); // NOI18N
        postSetUp();
    }

    @Override 
    protected void tearDown() throws Exception {
        super.tearDown();
        helper.shutdown();
    }
    
    protected void performTest(String source, String goldenDataFileName, String goldenErrFileName) throws Exception {
        File workDir = getWorkDir();
        File testFile = getDataFile(source);
        
        File output = new File(workDir, goldenDataFileName);
        PrintStream streamOut = new PrintStream(output);
        PrintStream oldOut = System.out;
        File error = new File(workDir, goldenErrFileName);
        PrintStream streamErr = new PrintStream(error);
        PrintStream oldErr = System.err;
        try {
            //System.out.println("testing " + testFile);     
            // redirect output and err
            System.setOut(streamOut);
            System.setErr(streamErr);
            getTraceModel().test(testFile, streamOut, streamErr);
        } finally {
            // restore err and out
            streamOut.close();
            streamErr.close();
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        //System.out.println("finished testing " + testFile);    
        
        boolean errTheSame = true;
        File goldenErrFile = null;
        File goldenErrFileCopy = null;
        // first of all check err, because if not failed (often) => dat diff will be created
        if (goldenErrFileName != null) {
            goldenErrFile = getGoldenFile(goldenErrFileName);
            if (CndCoreTestUtils.diff(error, goldenErrFile, null)) {
                errTheSame = false;
                // copy golden
                goldenErrFileCopy = new File(workDir, goldenErrFileName + ".golden");
                CndCoreTestUtils.copyToWorkDir(goldenErrFile, goldenErrFileCopy); // NOI18N
            }
        }
        
        boolean outTheSame = true;
        File goldenDataFile = getGoldenFile(goldenDataFileName);
        File goldenDataFileCopy = null;
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            outTheSame = false;
            // copy golden
            goldenDataFileCopy = new File(workDir, goldenDataFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenDataFileCopy); // NOI18N
        }
        if (outTheSame) {
            assertTrue("ERR Difference between " + error + " and " + goldenErrFileCopy, errTheSame); // NOI18N
        } else if (errTheSame) {
            assertTrue("OUTPUT Difference between " + output + " and " + goldenDataFileCopy, outTheSame); // NOI18N
        } else {
            assertTrue("OUTPUT and ERR are different, see content of folder " + workDir, false); // NOI18N
        }
    }    
}

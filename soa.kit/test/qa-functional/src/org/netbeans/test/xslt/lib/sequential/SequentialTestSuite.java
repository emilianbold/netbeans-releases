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

package org.netbeans.test.xslt.lib.sequential;

import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 *
 * @author ca@netbeans.org
 */

public class SequentialTestSuite extends TestSuite {
    private TestSequence m_testSequence;
    
    /** Creates a new instance of SequentialTestSuite */
    public SequentialTestSuite(String name, TestSequence testSequence) {
        super(name);
        m_testSequence = testSequence;
    }
    
    public int countTestCases() {
        return 1; // doesn't matter
    }
    
    public void run(TestResult result) {
        try {
            recurseTests(m_testSequence, result);
        } finally {
            m_testSequence.finalCleanup();
        }
    }
    
    private void recurseTests(SequentialTest sequentialTest, TestResult result) {
        
        sequentialTest.setupOnce();
        
        while (true) {
            if (result.shouldStop()) {
                break;
            }
            
            sequentialTest.setup();
            
            if (sequentialTest.isCompleted()) {
                break;
            }
            
            if (sequentialTest.needsExecution()) {
                String testName = sequentialTest.getTestName().trim();
                Test test = new SequentialTestCase(testName, sequentialTest);
                runTest(test, result);
            }
            
            if (sequentialTest instanceof TestSequence) {
                TestSequence sequence = (TestSequence) sequentialTest;
                
                Iterator<SequentialTest> iterator = sequence.getIterator();
                if (iterator != null) {
                    while(iterator.hasNext()) {
                        SequentialTest st = iterator.next();
                        recurseTests(st, result);
                    }
                }
            }
            
            sequentialTest.cleanup();
        }
        
        sequentialTest.cleanupOnce();
    }
}

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

package org.netbeans.modules.mobility;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 */
public class SampleUnitTest extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public SampleUnitTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        final NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SampleUnitTest("testT1"));
        suite.addTest(new SampleUnitTest("testT2"));
        suite.addTest(new SampleUnitTest("testT3"));
        suite.addTest(new SampleUnitTest("testT4"));
        suite.addTest(new SampleUnitTest("testT5"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(final java.lang.String[] args) {
        // run only selected test case
        junit.textui.TestRunner.run(new SampleUnitTest("testT3"));
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    public void tearDown() {
    }
    
    public void testT1() {
    }
    
    public void testT2() {
    }
    
    public void testT3() {
    }
    
    public void testT4() {
        assertTrue("It is only demo failure", false);
    }
    
    public void testT5() {
        fail("It is only demo failure");
    }
    
}

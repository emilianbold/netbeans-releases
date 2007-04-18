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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.catd;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * An example of testing servlets using httpunit and JUnit.
 **/
public class SampleTest extends TestCase {

    public static void main(String args[]) {
        try {
            junit.textui.TestRunner.run( suite() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SampleTest() {
        super();
    }

    public SampleTest(String s) {
        super(s);
    }

    public static Test suite() throws Exception {
        junit.framework.TestSuite suite = new junit.framework.TestSuite();
        suite.addTest(new SampleTest("testS1"));
        suite.addTest(new SampleTest("testS2"));
        suite.addTest(new SampleTest("testS3"));
        suite.addTest(new SampleTest("testS4"));
        suite.addTest(new SampleTest("testF1"));
        return suite;
    }

    
    public void testS1() throws Exception {
        assertTrue("S1", true);
    }
    public void testS2() throws Exception {
        assertTrue("S2", true);
    }
    public void testS3() throws Exception {
        assertTrue("S3", true);
    }
    public void testS4() throws Exception {
        assertTrue("S4", true);
    }
    
    public void testF1() throws Exception {
        assertTrue("F1", false);
    }
}



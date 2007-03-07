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
package org.netbeans.jellytools.modules.web;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.NewJspFileNameStepOperator.
 * @author Martin.Schovanek@sun.com
 */
public class NewWebFreeFormSrcFoldersStepOperatorTest extends JellyTestCase {
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewWebFreeFormSrcFoldersStepOperatorTest(String testName) {
        super(testName);
    }
    
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewWebFreeFormSrcFoldersStepOperatorTest("testVerify"));
        return suite;
    }

    
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }    

    
    /** Invokes and verifies the dialog. */
    public void testVerify() throws IOException {
        NewWebFreeFormNameStepOperator
                nameStep = NewWebFreeFormNameStepOperator.invoke();
        System.out.println("project="+new File(getDataDir(), "WebFreeFormSrc").
                getCanonicalPath());
        nameStep.setProjectLocation(new File(getDataDir(), "WebFreeFormSrc").
                getCanonicalPath());
        nameStep.next();
        new NewWebFreeFormActionsStepOperator().next();
        new NewWebFreeFormWebSrcStepOperator().next();
        NewWebFreeFormSrcFoldersStepOperator
                srcFoldersStep = new NewWebFreeFormSrcFoldersStepOperator();
        srcFoldersStep.verify();
        srcFoldersStep.close();
    }

    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}

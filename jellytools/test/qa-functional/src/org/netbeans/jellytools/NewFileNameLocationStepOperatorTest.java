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
package org.netbeans.jellytools;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.NewFileNameLocationStepOperator.
 * @author tb115823
 */
public class NewFileNameLocationStepOperatorTest extends JellyTestCase {

    public static NewFileNameLocationStepOperator op;

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewFileNameLocationStepOperatorTest("testInvoke"));
        suite.addTest(new NewFileNameLocationStepOperatorTest("testComponents"));
        return suite;
    }
    
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewFileNameLocationStepOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() {
        NewFileWizardOperator wop = NewFileWizardOperator.invoke();
        wop.selectProject("SampleProject"); //NOI18N
        // Java Classes
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        // Java Class
        String javaClassLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java");
        wop.selectCategory(javaClassesLabel);
        wop.selectFileType(javaClassLabel);
        wop.next();
        op = new NewFileNameLocationStepOperator();
    }
    
    public void testComponents() {
        op.txtObjectName().setText("NewObject"); // NOI18N
        assertEquals("Project name not propagated from previous step", "SampleProject", op.txtProject().getText()); // NOI18N
        op.selectSourcePackagesLocation();
        op.selectPackage("sample1"); // NOI18N
        String filePath = op.txtCreatedFile().getText();
        assertTrue("Created file path doesn't contain SampleProject.", filePath.indexOf("SampleProject") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain sample1 package name.", filePath.indexOf("sample1") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain NewObject name.", filePath.indexOf("NewObject") > 0);  //NOI18N
        op.cancel();
    }
    
}

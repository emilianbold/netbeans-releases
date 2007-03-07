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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 * Test of org.netbeans.jellytools.NewJspFileNameStepOperator.
 * @author Martin.Schovanek@sun.com
 */
public class NewWebFreeFormActionsStepOperatorTest extends JellyTestCase {
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewWebFreeFormActionsStepOperatorTest(String testName) {
        super(testName);
    }
    
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewWebFreeFormActionsStepOperatorTest("testVerify"));
        return suite;
    }

    
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }    

    
    /** Invokes and verifies the dialog. */
    public void testVerify() throws IOException {
        NewWebFreeFormNameStepOperator
                nameStep = NewWebFreeFormNameStepOperator.invoke();
        nameStep.setProjectLocation(new File(getDataDir(), "WebFreeFormSrc").
                getCanonicalPath());
        nameStep.next();
        NewWebFreeFormActionsStepOperator
                actionsStep = new NewWebFreeFormActionsStepOperator();
        actionsStep.verify();
        actionsStep.close();
    }

    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}

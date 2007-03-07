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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 * Test of org.netbeans.jellytools.NewJspFileNameStepOperator.
 * @author Martin.Schovanek@sun.com
 */
public class NewWebProjectTest extends JellyTestCase {
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewWebProjectTest(String testName) {
        super(testName);
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewWebProjectTest("createSampleWebProject"));
        return suite;
    }

    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
        
    public void createSampleWebProject() {
        String prjName = "SampleWebApplication";
        String web = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "OpenIDE-Module-Display-Category");
        String webApplication = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        
        // if sample web project does not exist in the data dir, create it
        File prjDir = new File(getDataDir(), prjName);
        if (!prjDir.exists()) {
            ProjectSupport.closeProject(prjName);
            NewProjectWizardOperator nop = NewProjectWizardOperator.invoke();
            nop.selectCategory(web);
            nop.selectProject(webApplication);
            nop.next();
            NewWebProjectNameLocationStepOperator lop =
                    new NewWebProjectNameLocationStepOperator();
            lop.setProjectName(prjName);
            try {
                lop.setProjectLocation(getDataDir().getCanonicalPath());
            } catch (IOException ioe) {
                fail(ioe);
            }
            lop.finish();
            // Opening Projects
            String openingProjectsTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
            try {
                // wait at most 60 second until progress dialog dismiss
                JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000);
                new NbDialogOperator(openingProjectsTitle).waitClosed();
            } catch (TimeoutExpiredException e) {
                // ignore when progress dialog was closed before we started to wait for it
            }
            // wait for opening
            ProjectsTabOperator.invoke().getProjectRootNode(prjName);
            ProjectSupport.waitScanFinished();
        }
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}

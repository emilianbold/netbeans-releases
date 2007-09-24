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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.qa.form.jda;

import java.util.ArrayList;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 *
 * @author Jiri Vagner
 */
public class SimpleJDAProjectTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public SimpleJDAProjectTest(String testName) {
        super(testName);
        
        setTestProjectName("JDABasic" + this.getTimeStamp()); // NOI18N
        setTestPackageName(getTestProjectName().toLowerCase());
    }
    
    /* Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SimpleJDAProjectTest("testCreation")); // NOI18N
        suite.addTest(new SimpleJDAProjectTest("testBuild")); // NOI18N
        suite.addTest(new SimpleJDAProjectTest("testFilesAndPackages")); // NOI18N
        return suite;
    }

    /** Creating JDA Basic project */
    public void testCreation() {
        new ActionNoBlock("File|New Project",null).perform(); // NOI18N

        NewProjectWizardOperator op = new NewProjectWizardOperator();
        op.selectProject("Java Desktop Application"); // NOI18N
        op.next();
        
        NbDialogOperator newJDAOp = new NbDialogOperator("New Desktop Application"); // NOI18N
        new JTextFieldOperator(newJDAOp, 3).typeText(getTestProjectName());
        new JButtonOperator(newJDAOp, "Finish").push(); // NOI18N
    }
    
    //** Is project buildable? */
    public void testBuild() {
        new ActionNoBlock("Window|Output|Output",null).perform(); // NOI18N       
        
        new ActionNoBlock("Build|Build Main Project",null).perform(); // NOI18N
        
        OutputTabOperator outputOp = new OutputTabOperator(getTestProjectName() +" (jar)"); // NOI18N
        outputOp.waitText("BUILD SUCCESSFUL"); // NOI18N
    }

    /** Contains packages,form files, properties files for form files,... */
    public void testFilesAndPackages() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();
        
        String basePackagePath = "Source Packages|" + getTestPackageName(); // NOI18N

        ArrayList<String> nodePaths = new ArrayList<String>();

        nodePaths.add(""); // NOI18N
        nodePaths.add("|" + getTestProjectName() + "AboutBox.java"); // NOI18N
        nodePaths.add("|" + getTestProjectName() + "App.java"); // NOI18N
        nodePaths.add("|" + getTestProjectName() + "View.java"); // NOI18N

        nodePaths.add(".resources"); // NOI18N
        nodePaths.add(".resources|" + getTestProjectName() + "AboutBox.properties"); // NOI18N
        nodePaths.add(".resources|" + getTestProjectName() + "App.properties"); // NOI18N
        nodePaths.add(".resources|" + getTestProjectName() + "View.properties"); // NOI18N

        nodePaths.add(".resources.busyicons"); // NOI18N
        nodePaths.add(".resources.busyicons|busy-icon0.png"); // NOI18N
        
        for (String nodePath : nodePaths) {
            new Node(prn, basePackagePath + nodePath).select();            
        }
    }
}

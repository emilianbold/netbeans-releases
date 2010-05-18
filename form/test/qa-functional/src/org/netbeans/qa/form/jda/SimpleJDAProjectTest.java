/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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

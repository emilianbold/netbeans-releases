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

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jellytools.NbDialogOperator;

/**
 * Test of org.netbeans.jellytools.NameLocationStepOperator.
 * @author tb115823
 */
public class NewProjectNameLocationStepOperatorTest extends JellyTestCase {
    
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
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaApplicationPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaAntProjectPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaLibraryPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaWithExistingSourcesPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testWebApplication"));
        return suite;
    }
    
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewProjectNameLocationStepOperatorTest(String testName) {
        super(testName);
    }
    
    // Standard
    private static final String standardLabel = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", 
                                                                 "Templates/Project/Standard");
    
    /** Test components on Java Application panel */
    public void testJavaApplicationPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        // Standard
        op.selectCategory(standardLabel);
        // Java Application
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SE.xml"));
        op.next();
        NewProjectNameLocationStepOperator stpop = new NewProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("NewProject");   // NOI18N
        stpop.btBrowseProjectLocation().pushNoBlock();
        new NbDialogOperator("Select").cancel(); //TODO I18N
        stpop.txtProjectLocation().setText("/tmp");
        stpop.txtProjectFolder().getText();
        stpop.cbCreateMainClass().setSelected(false);
        stpop.cbSetAsMainProject().setSelected(false);

        stpop.cancel();
    }
    
    
    
    /** Test components on Java Ant Project panel */
    public void testJavaAntProjectPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // Java Project with Existing Ant Script
        op.selectProject(Bundle.getString("org.netbeans.modules.java.freeform.resources.Bundle", "Templates/Project/Standard/j2sefreeform.xml"));
        op.next();
        NewProjectNameLocationStepOperator stpop = new NewProjectNameLocationStepOperator();
        stpop.txtLocation().setText("/tmp");
        stpop.txtBuildScript().setText("/path/to/antscript");//TODO I18N
        stpop.txtProjectName().setText("ant project");//TODO I18N
        stpop.txtProjectFolder().setText("/ant/folder");//TODO I18N
        stpop.cbSetAsMainProject().setSelected(true);
        stpop.btBrowseLocation().pushNoBlock();
        new NbDialogOperator("Browse Existing Ant Project Folder").cancel();//TODO I18N
        stpop.btBrowseBuildScript().pushNoBlock();
        new NbDialogOperator("Browse Existing Ant Build Script").cancel();//TODO I18N
        stpop.btBrowseProjectFolder().pushNoBlock();
        new NbDialogOperator("Browse New Project Folder").cancel();//TODO I18N
        stpop.cancel();
    }

    /** Test component on Java Library */
    public void testJavaLibraryPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // Java Class Library
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SElibrary.xml"));
        op.next();
        NewProjectNameLocationStepOperator stpop = new NewProjectNameLocationStepOperator();
        stpop.txtProjectLocation().setText("/tmp");
        stpop.txtProjectName().setText("NewLibraryProject");
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        new NbDialogOperator("Select Project Location").cancel(); //TODO I18N
        stpop.cancel();
    }

    public void testJavaWithExistingSourcesPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // "Java Project with Existing Sources"
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/existingJ2SE.xml"));
        op.next();
        
        NewProjectNameLocationStepOperator stpop = new NewProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("MyNewProject");
        stpop.txtProjectFolder().setText("D:\\tmp");
        stpop.txtProjectFolder().getText();
        stpop.cbSetAsMainProject().setSelected(false);
        stpop.btBrowseProjectLocation().pushNoBlock();
        new NbDialogOperator("Select Project Location").cancel(); //TODO I18N
        stpop.cancel();
    }
    
    
    public void testWebApplication() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        // Web
        String webLabel = Bundle.getString("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        op.selectCategory(webLabel);
        // Web Application
        String webApplicationLabel = Bundle.getString("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
        op.selectProject(webApplicationLabel);
        op.next();
        
        NewWebProjectNameLocationStepOperator stpop = new NewWebProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("NewProject");
        stpop.btBrowseProjectLocation().pushNoBlock();
        new NbDialogOperator("Select Project Location").cancel(); //TODO I18N
        stpop.txtProjectLocation().setText("/tmp");//TODO I18N
        stpop.txtProjectFolder().getText();
        stpop.cbSetAsMainProject().setSelected(false);
        assertEquals(stpop.txtContextPath().getText(), "/NewProject");  //NOI18N
        stpop.selectJ2EEVersion("J2EE 1.4");  // NOI18N
        stpop.cancel();
    }
}

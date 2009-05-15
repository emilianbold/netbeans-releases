/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools;

import junit.framework.Test;
import junit.textui.TestRunner;

/**
 * Test of org.netbeans.jellytools.NameLocationStepOperator.
 * @author tb115823
 */
public class NewJavaProjectNameLocationStepOperatorTest extends JellyTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static final String[] tests = new String[] {
        "testJavaApplicationPanel", "testJavaAntProjectPanel",
                "testJavaLibraryPanel", "testJavaWithExistingSourcesPanel"
                //"testWebApplication"
    };
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaApplicationPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaAntProjectPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaLibraryPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testJavaWithExistingSourcesPanel"));
        suite.addTest(new NewProjectNameLocationStepOperatorTest("testWebApplication"));
        return suite;
         */
        return createModuleTest(NewJavaProjectNameLocationStepOperatorTest.class,
                tests);
    }
    
    @Override
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        standardLabel = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", 
                                                                 "Templates/Project/Standard");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewJavaProjectNameLocationStepOperatorTest(String testName) {
        super(testName);
    }
    
    // Standard
    private static String standardLabel;
    
    /** Test components on Java Application panel */
    public void testJavaApplicationPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        // Standard
        op.selectCategory(standardLabel);
        // Java Application
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SE.xml"));
        op.next();
        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("NewProject");   // NOI18N
        stpop.btBrowseProjectLocation().pushNoBlock();
        String selectProjectLocation = org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
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
        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtLocation().setText("/tmp");
        stpop.txtBuildScript().setText("/path/to/antscript");//NOI18N
        stpop.txtProjectName().setText("ant project");//NOI18N
        stpop.txtProjectFolder().setText("/ant/folder");//NOI18N
        stpop.cbSetAsMainProject().setSelected(true);
        stpop.btBrowseLocation().pushNoBlock();

        String browseExistingAntProjectFolder =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_Browse_Location");
        new NbDialogOperator(browseExistingAntProjectFolder).cancel();// I18N
        stpop.btBrowseBuildScript().pushNoBlock();

        String browseExistingAntBuildScript =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_Browse_Build_Script");

        new NbDialogOperator(browseExistingAntBuildScript).cancel();// I18N
        stpop.btBrowseProjectFolder().pushNoBlock();

        String browseNewProjectFolder =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_Browse_Project_Folder");
        new NbDialogOperator(browseNewProjectFolder).cancel();// I18N
        stpop.cancel();
    }

    /** Test component on Java Library */
    public void testJavaLibraryPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // Java Class Library
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SElibrary.xml"));
        op.next();
        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectName().setText("NewLibraryProject");
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        String selectProjectLocation =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.cancel();
    }

    public void testJavaWithExistingSourcesPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // "Java Project with Existing Sources"
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/existingJ2SE.xml"));
        op.next();
        
        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("MyNewProject");
        stpop.txtProjectFolder().setText("/tmp"); //NOI18N
        stpop.txtProjectFolder().getText();
        stpop.cbSetAsMainProject().setSelected(false);
        stpop.btBrowseProjectLocation().pushNoBlock();
        String selectProjectLocation =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.cancel();
    }

    //TODO: separate
    /*
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
        stpop.cbSetAsMainProject().setSelected(false);                
        stpop.btBrowseProjectLocation().pushNoBlock();
        String selectProjectLocation =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.txtProjectLocation().setText("/tmp");//NOI18N
        stpop.txtProjectFolder().getText();
        stpop.cbSetAsMainProject().setSelected(false);
        stpop.cancel();
    }*/
}

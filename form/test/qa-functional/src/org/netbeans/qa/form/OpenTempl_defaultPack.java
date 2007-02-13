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

package org.netbeans.qa.form;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.qa.form.VisualDevelopmentUtil;
import org.netbeans.qa.form.DeleteDir;


/**
 * A Test based on JellyTestCase. JellyTestCase redirects Jemmy output
 * to a log file provided by NbTestCase. It can be inspected in results.
 * It also sets timeouts necessary for NetBeans GUI testing.
 *
 * Any JemmyException (which is normally thrown as a result of an unsuccessful
 * operation in Jemmy) going from a test is treated by JellyTestCase as a test
 * failure; any other exception - as a test error.
 *
 * Additionally it:
 *    - closes all modal dialogs at the end of the test case (property jemmy.close.modal - default true)
 *    - generates component dump (XML file containing components information) in case of test failure (property jemmy.screen.xmldump - default false)
 *    - captures screen into a PNG file in case of test failure (property jemmy.screen.capture - default true)
 *    - waits at least 1000 ms between test cases (property jelly.wait.no.event - default true)
 *
 * @author Jana Maleckova
 * Created on 29 January 2007, 15:59
 */



public class OpenTempl_defaultPack extends JellyTestCase {
    public String DATA_PROJECT_NAME = "Sample";
    public String PACKAGE_NAME = "Source Package";
    public String PROJECT_NAME = "General";
    public String workdirpath;
    
    
    MainWindowOperator mainWindow;
    ProjectsTabOperator pto;
    ComponentInspectorOperator cio;
    
    /** Constructor required by JUnit */
    public OpenTempl_defaultPack(String name){
        super(name);
        
    }
    
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new OpenTempl_defaultPack("testApplet"));
        suite.addTest(new OpenTempl_defaultPack("testDialog"));
        suite.addTest(new OpenTempl_defaultPack("testFrame"));
        suite.addTest(new OpenTempl_defaultPack("testInternalFrame"));
        suite.addTest(new OpenTempl_defaultPack("testPanel"));
        suite.addTest(new OpenTempl_defaultPack("testBean"));
        return suite;
    }
    
    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new DesktopAppTest("test1"));
    }
    
    /** Called before every test case. */
    public void setUp() throws IOException {
        workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Called after every test case. */
    public void tearDown() {
    }
    
    // Add test methods here, they have to start with 'test' name.
    
    //method create new project in parent dir to workdir
    public void begin() throws InterruptedException{
        DeleteDir.delDir(workdirpath + System.getProperty("file.separator") + DATA_PROJECT_NAME);
        Thread.sleep(5000);
        mainWindow = MainWindowOperator.getDefault();
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(PROJECT_NAME);
        npwo.selectProject("Java Application");
        npwo.next();
        
        NewProjectNameLocationStepOperator tfo_name = new NewProjectNameLocationStepOperator();
        tfo_name.txtProjectName().setText(DATA_PROJECT_NAME);
        
        NewProjectNameLocationStepOperator tfo1_location = new NewProjectNameLocationStepOperator();
        tfo_name.txtLocation().setText(workdirpath);
        JButtonOperator bo = new JButtonOperator(npwo, "Finish");
        //bo.getSource().requestFocus();
        bo.push();
        
        log("Project "+ DATA_PROJECT_NAME + " was created");
        Thread.sleep(4000);
        
    }
    public void deleteProject() throws InterruptedException {
        //Project Deleting
        pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();
        
        DeleteAction delProject = new DeleteAction();
        delProject.perform();
        
        NbDialogOperator ndo = new NbDialogOperator("Delete Project");
        JCheckBoxOperator cbo = new JCheckBoxOperator(ndo);
        cbo.changeSelection(true);
        ndo.yes();
        
        Thread.sleep(10000);
        //check if project was really deleted from disc
        File f = new File(workdirpath + System.getProperty("file.separator") + DATA_PROJECT_NAME);
        System.out.println("adresar:"+f);
        if (f.exists()) {
            log("File " + DATA_PROJECT_NAME + " was not deleted correctly");
            System.exit(1);
        } else {
            log ("File " + DATA_PROJECT_NAME + " was deleted correctly");
        }
    }
    
    public void openTemplate(String templateName) throws InterruptedException{
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(DATA_PROJECT_NAME);
        nfwo.selectCategory("Java GUI Forms");
        nfwo.selectFileType(templateName);
        nfwo.next();
        JComboBoxOperator jcb_package = new JComboBoxOperator(nfwo, 1);
        jcb_package.clearText();
        Thread.sleep(2000);
        
        if ((templateName == "Bean Form")) {
            nfwo.next();
            JTextFieldOperator class_name = new JTextFieldOperator(nfwo);
            class_name.setText("javax.swing.JButton");
            nfwo.finish();
            log (templateName + " is created correctly");
        } else {
            nfwo.finish();
            log (templateName + " is created correctly");
            Thread.sleep(3000);
        }
    }
    
    /** Test case 1.
     *Create new JApplet template in default package
     */
    public void testApplet() throws InterruptedException, IOException {
        
        begin();
       
        openTemplate("JApplet Form");
         Thread.sleep(10000);
        testFormFile("NewJApplet");
        testJavaFile("NewJApplet");
        
    }
    
    /** Test case 2.
     * Create new JDialog template in default package
     */
    public void testDialog() throws InterruptedException, IOException {
        
        openTemplate("JDialog Form");
        
        //check if template is generated correctly
        testFormFile("NewJDialog");
        testJavaFile("NewJDialog");
        
    }
    
    /** Test case 3.
     * Create new JFrame template in default package
     */
    public void testFrame() throws InterruptedException, IOException {
        
        openTemplate("JFrame Form");
        //check if template is generated correctly
        testFormFile("NewJFrame");
        testJavaFile("NewJFrame");
    }
    
    
    /** Test case 4.
     * Create new JInternalFrame template in default package
     */
    public void testInternalFrame() throws InterruptedException, IOException {
        
        openTemplate("JInternalFrame Form");
        
        //check if template is generated correctly
        testFormFile("NewJInternalFrame");
        testJavaFile("NewJInternalFrame");
    }
    
    
    /** Test case 5.
     * Create new JPanel template in default package
     */
    public void testPanel() throws InterruptedException, IOException {
        
        openTemplate("JPanel Form");
        
        //check if template is generated correctly
        testFormFile("NewJPanel");
        testJavaFile("NewJPanel");
        
    }
    
    /** Test case 6. oa
     * Create new Bean template in default package
     */
    public void testBean() throws InterruptedException, IOException {
        
        openTemplate("Bean Form");
        
        
        testFormFile("NewBeanForm");
        //Bug in generating of new Bean Form template 95403
        //testJavaFile("NewBeanForm");
        
        deleteProject();
    }
    
    public void testFormFile(String formfile) throws IOException {
        try {
            
            getRef().print(VisualDevelopmentUtil.readFromFile(
                    getWorkDir().getParentFile().getAbsolutePath() + File.separatorChar + DATA_PROJECT_NAME +  File.separatorChar + "src" + File.separatorChar + formfile + ".form")
                    );
            // System.out.println("reffile: " + this.getName()+".ref");
            
        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }
        
        assertFile(new File(getWorkDir() + File.separator + this.getName()+".ref"), getGoldenFile(formfile + "FormFile.pass"), new File(getWorkDir(), formfile+".diff"));
        
        
        
        //compareReferenceFiles("TestScenario.ref", "testFormFile.pass", null);
    }
    
    public void testJavaFile(String javafile) throws IOException{
        try {
            String pokus = VisualDevelopmentUtil.readFromFile(
                    getWorkDir().getParentFile().getAbsolutePath() + File.separatorChar + DATA_PROJECT_NAME +  File.separatorChar + "src" + File.separatorChar + javafile + ".java");
            int start = pokus.indexOf("/*");
            int end = pokus.indexOf("*/");
            pokus = pokus.substring(0, start )+ pokus.substring(end +2);
            
            start = pokus.indexOf("/**");
            end = pokus.indexOf("*/");
            pokus = pokus.substring(0, start )+ pokus.substring(end +2);
            getRef().print(pokus);
            // System.out.println("reffile: " + this.getName()+".ref");
            
        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }
        
        assertFile(new File(getWorkDir() + File.separator + this.getName()+".ref"), getGoldenFile(javafile + "JavaFile.pass"), new File(getWorkDir(), javafile+".diff"));
        
        
    }
}

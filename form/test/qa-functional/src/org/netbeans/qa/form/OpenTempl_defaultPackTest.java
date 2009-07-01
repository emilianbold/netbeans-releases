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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.qa.form;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
//import org.netbeans.jemmy.Test;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

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
 * Test is only for java 1.6 for now
 */
public class OpenTempl_defaultPackTest extends ExtJellyTestCase {

    public String DATA_PROJECT_NAME = "SampleProject";
    public String PACKAGE_NAME = "Source Package";
    public String PROJECT_NAME = "Java";
    public String workdirpath;
    public String jdkVersion = ExtJellyTestCase.getJDKVersionCode();
    public String pokus;
    MainWindowOperator mainWindow;
    ProjectsTabOperator pto;
    ComponentInspectorOperator cio;

    /** Constructor required by JUnit */
    public OpenTempl_defaultPackTest(String name) {
        super(name);

    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(OpenTempl_defaultPackTest.class).addTest(
                "testApplet", "testDialog", "testFrame", "testInter", "testAppl", "testMidi", "testPanel", "testBean")
                .gui(true).enableModules(".*").clusters(".*"));

    }

    /** Called before every test case. */
    @Override
    public void setUp() throws IOException {
        openDataProjects(DATA_PROJECT_NAME);
        workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        System.out.println("########  " + getName() + "  #######");
    }

    /** Called after every test case. */
    public void tearDown() {
    }

    // Add test methods here, they have to start with 'test' name.
    //method create new project in parent dir to workdir
    public void begin() throws InterruptedException {
        DeleteDir.delDir(workdirpath + System.getProperty("file.separator") + DATA_PROJECT_NAME);
        Thread.sleep(10000);
        mainWindow = MainWindowOperator.getDefault();
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(PROJECT_NAME);
        npwo.selectProject("Java Application");
        npwo.next();

        NewJavaProjectNameLocationStepOperator tfo_name = new NewJavaProjectNameLocationStepOperator();
        tfo_name.txtProjectName().setText(DATA_PROJECT_NAME);

        NewJavaProjectNameLocationStepOperator tfo1_location = new NewJavaProjectNameLocationStepOperator();
        tfo_name.txtLocation().setText(workdirpath);
        JButtonOperator bo = new JButtonOperator(npwo, "Finish");
        //bo.getSource().requestFocus();
        bo.push();

        log("Project " + DATA_PROJECT_NAME + " was created");
        Thread.sleep(10000);

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
        System.out.println("adresar:" + f);
        if (f.exists()) {
            log("File " + DATA_PROJECT_NAME + " was not deleted correctly");
            System.exit(1);
        } else {
            log("File " + DATA_PROJECT_NAME + " was deleted correctly");
        }
    }

    public void openTemplate(String templateName) throws InterruptedException {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(DATA_PROJECT_NAME);
        Thread.sleep(10000);
        nfwo.selectCategory("Swing GUI Forms");
        nfwo.selectFileType(templateName);
        nfwo.next();
        JComboBoxOperator jcb_package = new JComboBoxOperator(nfwo, 1);
        jcb_package.clearText();
        Thread.sleep(5000);

        if ((templateName == "Bean Form")) {
            nfwo.next();
            JTextFieldOperator class_name = new JTextFieldOperator(nfwo);
            class_name.setText("javax.swing.JButton");
            nfwo.finish();
            log(templateName + " is created correctly");
        } else {
            nfwo.finish();
            log(templateName + " is created correctly");
            Thread.sleep(3000);
        }
    }

    /** Test case 1.
     *Create new JApplet template in default package
     */
    public void testApplet() throws InterruptedException, IOException {

        Thread.sleep(2000);
//        begin();

        openTemplate("JApplet Form");
        Thread.sleep(10000);
        System.out.println(getWorkDir());
        //testFormFile("NewJApplet");
        testJavaFile("NewJApplet");

    }

    /** Test case 2.
     * Create new JDialog template in default package
     */
    public void testDialog() throws InterruptedException, IOException {

        openTemplate("JDialog Form");

        //check if template is generated correctly
        //testFormFile("NewJDialog");
        testJavaFile("NewJDialog");

    }

    /** Test case 3.
     * Create new JFrame template in default package
     */
    public void testFrame() throws InterruptedException, IOException {

        openTemplate("JFrame Form");
        //check if template is generated correctly
        //testFormFile("NewJFrame");
        testJavaFile("NewJFrame");
    }

    /** Test case 4.
     * Create new JInternalFrame template in default package
     */
    public void testInter() throws InterruptedException, IOException {

        openTemplate("JInternalFrame Form");

        //check if template is generated correctly
        //testFormFile("NewJInternalFrame");
        testJavaFile("NewJInternalFrame");
    }

    public void testAppl() throws InterruptedException, IOException {

        openTemplate("Application Sample Form");

        //check if template is generated correctly
        //testFormFile("NewApplication");
        testJavaFile("NewApplication");

    }

    public void testMidi() throws InterruptedException, IOException {

        openTemplate("MDI Application Sample Form");

        //check if template is generated correctly
        //testFormFile("NewMDIApplication");
        testJavaFile("NewMDIApplication");

    }

    /** Test case 5.
     * Create new JPanel template in default package
     */
    public void testPanel() throws InterruptedException, IOException {

        openTemplate("JPanel Form");

        //check if template is generated correctly
        //testFormFile("NewJPanel");
        testJavaFile("NewJPanel");

    }

    /** Test case 6. oa
     * Create new Bean template in default package
     */
    public void testBean() throws InterruptedException, IOException {

        openTemplate("Bean Form");


        //testFormFile("NewBeanForm");
        //Bug in generating of new Bean Form template 95403
        //testJavaFile("NewBeanForm");
        Thread.sleep(1000);
    //Timeout needed
    }

    public void testFormFile(String formfile) throws IOException {
        try {

            getRef().print(VisualDevelopmentUtil.readFromFile(
                    getWorkDir().getParentFile().getAbsolutePath() + File.separatorChar + DATA_PROJECT_NAME + File.separatorChar + "src" + File.separatorChar + formfile + ".form"));

        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }

        assertFile(new File(getWorkDir() + File.separator + this.getName() + ".ref"), getGoldenFile(formfile + "FormFile.pass"), new File(getWorkDir(), formfile + ".diff"));
        System.out.println(formfile + " is not the same as golden file");



    //compareReferenceFiles("TestScenario.ref", "testFormFile.pass", null);
    }

    public void testJavaFile(String javafile) throws IOException {

        try {
            pokus = VisualDevelopmentUtil.readFromFile(getDataDir().getAbsolutePath() +
                    File.separatorChar + DATA_PROJECT_NAME + File.separatorChar + "src" + File.separatorChar + javafile + ".java");

            getRef().print(createRefFile(pokus));
            log("Java reference file was created");

        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }
        if (jdkVersion == "jdk15") {
            assertFile(new File(getWorkDir() + File.separator + this.getName() + ".ref"), getGoldenFile(javafile + "JavaFile15.pass"), new File(getWorkDir(), javafile + ".diff"));
        } else {
            assertFile(new File(getWorkDir() + File.separator + this.getName() + ".ref"), getGoldenFile(javafile + "JavaFile.pass"), new File(getWorkDir(), javafile + ".diff"));
        }

    }

    public String createRefFile(String test) {
        int start = test.indexOf("/*");
        int end = test.indexOf("*/");
        test = test.substring(0, start) + test.substring(end + 2);
        
        start = test.indexOf("/*");
        end = test.indexOf("*/");
        test = test.substring(0, start) + test.substring(end + 2);

        start = test.indexOf("/**");
        end = test.indexOf("*/");
        test = test.substring(0, start) + test.substring(end + 2);
        return test;
    }
}

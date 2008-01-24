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

package org.netbeans.test.j2ee;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.j2ee.addmethod.*;
import org.netbeans.test.j2ee.lib.Utils;


/**
 * EJBValidation suite for J2EE.
 * 
 * 
 * @author libor.martinek@sun.com
 */
public class EJBValidation extends JellyTestCase {
    
    public static final String EAR_PROJECT_NAME = "TestingEntApp";
    public static final String WEB_PROJECT_NAME = EAR_PROJECT_NAME + "-WebModule";
    public static final String EJB_PROJECT_NAME = EAR_PROJECT_NAME + "-EJBModule";
    
    public static File EAR_PROJECT_FILE;
    public static File EJB_PROJECT_FILE;
    public static File WEB_PROJECT_FILE;

    
    /** Need to be defined because of JUnit */
    public EJBValidation(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new EJBValidation("openProjects"));
        suite.addTest(new AddMethodTest("testAddBusinessMethod1InSB"));
        suite.addTest(new AddMethodTest("testAddBusinessMethod2InSB"));
        suite.addTest(new AddMethodTest("testAddBusinessMethod1InEB"));
        suite.addTest(new AddMethodTest("testAddBusinessMethod2InEB"));
        suite.addTest(new AddMethodTest("testAddCreateMethod1InEB"));
        suite.addTest(new AddMethodTest("testAddCreateMethod2InEB"));
        suite.addTest(new AddMethodTest("testAddHomeMethod1InEB"));
        suite.addTest(new AddMethodTest("testAddHomeMethod2InEB"));
        //suite.addTest(new GenerateDTOTest("testGenerateDTO"));
        //suite.addTest(new GenerateDTOTest("testDeleteDTO"));
        suite.addTest(new AddFinderMethodTest("testAddFinderMethod1InEB"));
        suite.addTest(new AddFinderMethodTest("testAddFinderMethod2InEB"));
        suite.addTest(new AddSelectMethodTest("testAddSelectMethod1InEB"));
        suite.addTest(new AddSelectMethodTest("testAddSelectMethod2InEB"));
        suite.addTest(new AddCMPFieldTest("testAddCMPField1InEB"));
        suite.addTest(new AddCMPFieldTest("testAddCMPField2InEB"));
        suite.addTest(new CallEJBTest("testCallEJBInServlet"));
        suite.addTest(new CallEJBTest("testCallEJB1InSB"));
        suite.addTest(new CallEJBTest("testCallEJB2InSB"));
        //suite.addTest(new EJBValidation("prepareDatabase"));
        //suite.addTest(new UseDatabaseTest("testUseDatabase1InSB"));
        //suite.addTest(new SendMessageTest("testSendMessage1InSB"));
        //suite.addTest(new EJBValidation("testStartServer"));
        //suite.addTest(new EJBValidation("testDeployment"));
        //suite.addTest(new EJBValidation("testUndeploy"));
        //suite.addTest(new CreateCMPBeansFromDB("testCreateCMPBeansFromDB"));
        //suite.addTest(new EJBValidation("testDeleteEntityBean"));

        //suite.addTest(new EJBValidation("testStopServer"));
        suite.addTest(new EJBValidation("closeProjects"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());

        /*
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new GUIValidation("openProjects"));
        suite.addTest(new GUIValidation("prepareDatabase"));
        suite.addTest(new CreateCMPBeansFromDB("testCreateCMPBeansFromDB"));
        TestRunner.run(suite);
        */
        // run only selected test case
        //junit.textui.TestRunner.run(new GUIValidation("testDeleteWSinWeb"));
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    public void tearDown() {
    }
    
    public void openProjects() {
        EAR_PROJECT_FILE = new File(getDataDir(), EAR_PROJECT_NAME);
        ProjectSupport.openProject(EAR_PROJECT_FILE);
        ProjectSupport.waitScanFinished();
        EJB_PROJECT_FILE = new File(EAR_PROJECT_FILE, EAR_PROJECT_NAME+"-ejb");
        ProjectSupport.openProject(EJB_PROJECT_FILE);
        ProjectSupport.waitScanFinished();
        WEB_PROJECT_FILE = new File(EAR_PROJECT_FILE, EAR_PROJECT_NAME + "-war");
        ProjectSupport.openProject(WEB_PROJECT_FILE);
        ProjectSupport.waitScanFinished();
        
        //TEMPORARY HACK
        try { new NbDialogOperator("Open Project").ok(); }
        catch (TimeoutExpiredException e) {}

        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        String files[] = { "TestingSession", "TestingEntity" };
        for (int i=0; i<files.length; i++) {
            Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                                 Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node")
                                 +"|"+files[i]);
            new OpenAction().performAPI(openFile);
            EditorOperator editor = new EditorWindowOperator().getEditor(files[i]+"Bean.java");
        }
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }
    
    public void closeProjects() {
        new EditorWindowOperator().getEditor().closeDiscardAll();
        ProjectSupport.closeProject(EAR_PROJECT_NAME);
        ProjectSupport.closeProject(EJB_PROJECT_NAME);
        ProjectSupport.closeProject(WEB_PROJECT_NAME);
    }
    
    public void prepareDatabase() {
        Utils.prepareDatabase();
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }
    
    public void testDeleteEntityBean() throws IOException {
        Node node = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                                 Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node")
                                 +"|TestingEntityEB");
        new DeleteAction().performAPI(node);
        NbDialogOperator dialog = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "MSG_ConfirmDeleteObjectTitle"));
        new JCheckBoxOperator(dialog).changeSelection(true);
        dialog.yes();
        
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
        Utils utils = new Utils(this);
        String ddNames[] = { "ejb-jar.xml", 
                             "sun-ejb-jar.xml"};
        utils.assertFiles(new File(EJB_PROJECT_FILE, "src/conf"), ddNames, getName()+"_");

        File dir = new File(EJB_PROJECT_FILE, "src/java/test");
        String deletedFiles[] = dir.list(new FilenameFilter () {
            public boolean accept(File dir, String name) {
                return name.endsWith(".java") && name.startsWith("TestingEntity") && !name.equals("TestingEntityDTO.java");
            }
        });
        if (deletedFiles != null && deletedFiles.length > 0) {
            fail("Bean files was not deleted from directory "+dir.getAbsolutePath()+". I found "+Arrays.asList(deletedFiles));
        }
    }
    
    public void testStartServer() throws IOException {
        Utils.startStopServer(true);
        String url = "http://localhost:8080/";
        String page = Utils.loadFromURL(url);
        log(page);
        String text = "Your server is up and running";
        assertTrue("AppServer start page doesn't contain text '"+text+"'. See log for page content.", page.indexOf(text)>=0);
    }
    
    public void testDeployment() throws IOException {
        final String CONTROL_TEXT = "ControlTextABC";
        Utils utils = new Utils(this);
        
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                                 Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node")
                                 +"|TestingSession");
        new OpenAction().performAPI(openFile);
        utils.checkAndModify("TestingSessionBean.java", 61, "public String testBusinessMethod1()", 63, "return null;", 63, true, "return \""+CONTROL_TEXT+"\";\n");
        utils.checkAndModify("TestingSessionBean.java", 103, "// javax.jms.TextMessage tm = session.createTextMessage();", 0, null, 103, true, "javax.jms.TextMessage tm = session.createTextMessage();\n");
        utils.checkAndModify("TestingSessionBean.java", 104, "// tm.setText(messageData.toString());", 0, null, 104, true, "tm.setText(messageData.toString());\n");
        utils.checkAndModify("TestingSessionBean.java", 105, "// return tm;", 0, null, 105, true, "return tm;\n");
        
        openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.WEB_PROJECT_NAME),
                                 Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_Sources")
                                 +"|test|TestingServlet");
        new OpenAction().performAPI(openFile);
        utils.checkAndModify("TestingServlet.java", 36, "out.println(\"</body>\");", 0, null, 36, false, "out.println(lookupTestingSessionBean().testBusinessMethod1());\n");
                
        String page = Utils.deploy(EAR_PROJECT_NAME, "http://localhost:8080/TestingEntApp-WebModule/TestingServlet");
        log(page);
        assertTrue("TestingServlet doesn't contain expected text '"+CONTROL_TEXT+"'. See log for page content.", page.indexOf(CONTROL_TEXT)>=0);
    }
    
    public void testUndeploy() {
        Utils.undeploy(EAR_PROJECT_NAME);
    }
    

    public void testStopServer() {
        Utils.startStopServer(false);
        String url = "http://localhost:8080/";
        try {
            String page = Utils.loadFromURL(url);
            log(page);
            fail("AppServer should not be running, but start page is available. See log for page content.");
        } catch (IOException e) {}
    }
      
}

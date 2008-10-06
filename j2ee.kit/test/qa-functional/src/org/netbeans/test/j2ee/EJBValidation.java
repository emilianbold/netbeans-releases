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
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.j2ee.addmethod.AddCMPFieldTest;
import org.netbeans.test.j2ee.addmethod.AddFinderMethodTest;
import org.netbeans.test.j2ee.addmethod.AddMethodTest;
import org.netbeans.test.j2ee.addmethod.AddSelectMethodTest;
import org.netbeans.test.j2ee.addmethod.CallEJBTest;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.netbeans.test.j2ee.lib.Utils;
import org.openide.util.Exceptions;

/**
 * EJBValidation suite for J2EE.
 * 
 * 
 * @author libor.martinek@sun.com
 */
public class EJBValidation extends J2eeTestCase {

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

    public static Test suite() {

        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();//createConfiguration(EJBValidation.class);
        conf = addServerTests(Server.GLASSFISH, conf, EJBValidation.class, "openProjects");
        conf = addServerTests(Server.GLASSFISH, conf, AddMethodTest.class,"testAddBusinessMethod1InSB",
        "testAddBusinessMethod2InSB", "testAddBusinessMethod1InEB","testAddBusinessMethod2InEB",
         "testAddCreateMethod1InEB","testAddCreateMethod2InEB", "testAddHomeMethod1InEB", "testAddHomeMethod2InEB");
        conf = addServerTests(Server.GLASSFISH, conf, AddFinderMethodTest.class,
        "testAddFinderMethod1InEB","testAddFinderMethod2InEB");
        conf = addServerTests(Server.GLASSFISH, conf, AddSelectMethodTest.class,
        "testAddSelectMethod1InEB","testAddSelectMethod2InEB");
        conf = addServerTests(Server.GLASSFISH, conf, AddCMPFieldTest.class,
        "testAddCMPField1InEB","testAddCMPField2InEB");
        conf = addServerTests(Server.GLASSFISH, conf, CallEJBTest.class,
        "testCallEJBInServlet","testCallEJB1InSB","testCallEJB2InSB");
        conf = addServerTests(Server.GLASSFISH, conf, EJBValidation.class, "closeProjects");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    /*
    NbTestSuite suite = new NbTestSuite();
    //suite.addTest(new GenerateDTOTest("testGenerateDTO"));
    //suite.addTest(new GenerateDTOTest("testDeleteDTO"));
    //suite.addTest(new EJBValidation("prepareDatabase"));
    //suite.addTest(new UseDatabaseTest("testUseDatabase1InSB"));
    //suite.addTest(new SendMessageTest("testSendMessage1InSB"));
    //suite.addTest(new EJBValidation("testStartServer"));
    //suite.addTest(new EJBValidation("testDeployment"));
    //suite.addTest(new EJBValidation("testUndeploy"));
    //suite.addTest(new CreateCMPBeansFromDB("testCreateCMPBeansFromDB"));
    //suite.addTest(new EJBValidation("testDeleteEntityBean"));
    //suite.addTest(new EJBValidation("testStopServer"));
    return suite;*/
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }

    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    public void tearDown() {
    }

    public void openProjects() {
        EAR_PROJECT_FILE = new File(getDataDir(), EAR_PROJECT_NAME);
        try {
            openProjects(EAR_PROJECT_FILE.getAbsolutePath());
            ProjectSupport.waitScanFinished();
            EJB_PROJECT_FILE = new File(EAR_PROJECT_FILE, EAR_PROJECT_NAME + "-ejb");
            openProjects(EJB_PROJECT_FILE.getAbsolutePath());
            ProjectSupport.waitScanFinished();
            WEB_PROJECT_FILE = new File(EAR_PROJECT_FILE, EAR_PROJECT_NAME + "-war");
            openProjects(WEB_PROJECT_FILE.getAbsolutePath());
            ProjectSupport.waitScanFinished();
        } catch (IOException ex) {
            System.out.println("IOException " + ex.getMessage());
        }

        //TEMPORARY HACK
        try {
            new NbDialogOperator("Open Project").ok();
        } catch (TimeoutExpiredException e) {
            System.out.println("TimeoutExpiredException " + e.getMessage());
        }

        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        String files[] = {"TestingSession", "TestingEntity"};
        for (int i = 0; i < files.length; i++) {
            Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                    Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node") + "|" + files[i]);
            new OpenAction().performAPI(openFile);
            EditorOperator editor = EditorWindowOperator.getEditor(files[i] + "Bean.java");
        }
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }

    public void closeProjects() {
        EditorOperator.closeDiscardAll();
        J2eeProjectSupport.closeProject(EAR_PROJECT_NAME);
        J2eeProjectSupport.closeProject(EJB_PROJECT_NAME);        
        J2eeProjectSupport.closeProject(WEB_PROJECT_NAME);
    }

    public void prepareDatabase() {
        Utils.prepareDatabase();
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }

    public void testDeleteEntityBean() throws IOException {
        Node node = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node") + "|TestingEntityEB");
        new DeleteAction().performAPI(node);
        NbDialogOperator dialog = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "MSG_ConfirmDeleteObjectTitle"));
        new JCheckBoxOperator(dialog).changeSelection(true);
        dialog.yes();

        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
        Utils utils = new Utils(this);
        String ddNames[] = {"ejb-jar.xml",
            "sun-ejb-jar.xml"
        };
        utils.assertFiles(new File(EJB_PROJECT_FILE, "src/conf"), ddNames, getName() + "_");

        File dir = new File(EJB_PROJECT_FILE, "src/java/test");
        String deletedFiles[] = dir.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".java") && name.startsWith("TestingEntity") && !name.equals("TestingEntityDTO.java");
            }
        });
        if (deletedFiles != null && deletedFiles.length > 0) {
            fail("Bean files was not deleted from directory " + dir.getAbsolutePath() + ". I found " + Arrays.asList(deletedFiles));
        }
    }

    public void testStartServer() throws IOException {
        Utils.startStopServer(true);
        String url = "http://localhost:8080/";
        String page = Utils.loadFromURL(url);
        log(page);
        String text = "Your server is up and running";
        assertTrue("AppServer start page doesn't contain text '" + text + "'. See log for page content.", page.indexOf(text) >= 0);
    }

    public void testDeployment() throws IOException {
        final String CONTROL_TEXT = "ControlTextABC";
        Utils utils = new Utils(this);
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node") + "|TestingSession");
        new OpenAction().performAPI(openFile);
        utils.checkAndModify("TestingSessionBean.java", 61, "public String testBusinessMethod1()", 63, "return null;", 63, true, "return \"" + CONTROL_TEXT + "\";\n");
        utils.checkAndModify("TestingSessionBean.java", 103, "// javax.jms.TextMessage tm = session.createTextMessage();", 0, null, 103, true, "javax.jms.TextMessage tm = session.createTextMessage();\n");
        utils.checkAndModify("TestingSessionBean.java", 104, "// tm.setText(messageData.toString());", 0, null, 104, true, "tm.setText(messageData.toString());\n");
        utils.checkAndModify("TestingSessionBean.java", 105, "// return tm;", 0, null, 105, true, "return tm;\n");
        openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.WEB_PROJECT_NAME),
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_Sources") + "|test|TestingServlet");
        new OpenAction().performAPI(openFile);
        utils.checkAndModify("TestingServlet.java", 36, "out.println(\"</body>\");", 0, null, 36, false, "out.println(lookupTestingSessionBean().testBusinessMethod1());\n");

        String page = Utils.deploy(EAR_PROJECT_NAME, "http://localhost:8080/TestingEntApp-WebModule/TestingServlet");
        log(page);
        assertTrue("TestingServlet doesn't contain expected text '" + CONTROL_TEXT + "'. See log for page content.", page.indexOf(CONTROL_TEXT) >= 0);
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
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
}

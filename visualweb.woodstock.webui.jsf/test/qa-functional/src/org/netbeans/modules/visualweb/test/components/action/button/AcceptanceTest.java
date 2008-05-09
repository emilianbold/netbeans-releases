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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.test.components.action.button;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.visualweb.gravy.Bundle;
import org.netbeans.modules.visualweb.gravy.RaveTestCase;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import static org.netbeans.modules.visualweb.test.components.util.ComponentUtils.typeLines;

/**
 * @author Sherry Zhou (sherry.zhou@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {

    public String _bundle = ComponentUtils.getBundle();
    public String _projectServer = Bundle.getStringTrimmed(_bundle, "projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_bundle, "logFile");
    public String _logFile = System.getProperty("xtest.workdir") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_bundle, "Exception");
    public String _close = Bundle.getStringTrimmed(_bundle, "close");
    public String _run = Bundle.getStringTrimmed(_bundle, "Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_bundle, "buildSuccess");
    public String _true = Bundle.getStringTrimmed(_bundle, "true");
    public String _projectName = "ButtonAcceptanceTest";
    public String imageDir = ComponentUtils.getDataDir() + "action" + File.separator;
    public String image1 = imageDir + "orchid1.JPG";
    public String image2 = imageDir + "orchid2.JPG";
    public static int xButton = 50;
    public static int yButton = 50;
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_bundle, "separator");
    public String _reformatCode = Bundle.getStringTrimmed(_bundle, "reformatCode");
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ServerNavigatorOperator explorer;

    public AcceptanceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new AcceptanceTest("testCreateProject"));
        suite.addTest(new AcceptanceTest("testAddButton"));
        suite.addTest(new AcceptanceTest("testAddActionEvent"));
        suite.addTest(new AcceptanceTest("testVerifyJSPEditor"));
        suite.addTest(new AcceptanceTest("testChangeTextInPropertySheet"));
        suite.addTest(new AcceptanceTest("testChangeTextInJSPEditor"));
        suite.addTest(new AcceptanceTest("testDeploy"));
        suite.addTest(new AcceptanceTest("testCloseProject"));
        suite.addTest(new AcceptanceTest("testUndeploy"));
        suite.addTest(new AcceptanceTest("testCheckIDELog"));
        return suite;
    }

    /** method called before each testcase
     */
    protected void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /** method called after each testcase
     */
    protected void tearDown() {
        System.out.println("########  " + getName() + " Finished #######");
    }

    /*
     * Start PE. Delete PointBase travel resource
     */
    private void testStartup() {
        //Start PE is it is not started yet
        ServerNavigatorOperator se = new ServerNavigatorOperator();
        // Skip next 2 steps if running on Mac as Jemmy's call popup issue
        if (!System.getProperty("os.name").equals("Mac OS X")) {
            //Start PE is it is not started yet
            try {
                se.startServer("J2EE");
            } catch (Exception e) {
            }
            // Delete pb travel resource if it exists
            se.deleteResource("jdbc/Travel");
        }
    }

    /*
     *   Create new project
     *   And add property val to SessionBean1.java
     */
    public void testCreateProject() {
        startTest();
        log("**Creating Project");
        log(System.getProperty("j2eeVersion"));
        //Create Project
        try {
            ComponentUtils.createNewProject(_projectName);
        } catch (Exception e) {
            log(">> Project Creation Failed");
            e.printStackTrace();
            log(e.toString());
            fail(e);
        }
        log("**Done");
        endTest();
    }

    /*
     *   Add 2 Buttons. Set their action event handler.
     */
    public void testAddButton() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        log("Add a button component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(2000);
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicButton"), designer, new Point(xButton, yButton));

        TestUtils.wait(2000);
        Util.saveAllAPICall();
        Util.wait(500);
        endTest();
    }

    /*
     * Verify button_action() code is generated
     */
    public void testAddActionEvent() {
        startTest();
        log("Open java code editor view by clicking designer and then double clicking on the button ");
        designer.clickMouse(20, 20, 1);
        TestUtils.wait(1000);
        designer.clickMouse(xButton + 5, yButton + 5, 2);
        TestUtils.wait(1000);
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        JEditorPaneOperator editor = new JEditorPaneOperator(
                RaveWindowOperator.getDefaultRave(), "public class " + "Page1");
        editor.setVerification(false);
        TestUtils.wait(2000);
        editor.requestFocus();
        new SaveAllAction().perform();
        log("Editor Dump:");
        log(editor.getText());
        String expectedStr = "button1_action()";
        assertTrue("There is no \"" + expectedStr + "\" string in jsp editor",
                editor.getText().contains(expectedStr));
        editor.requestFocus();
        typeLines("log(\"Action Performed\");\n", editor);
        log("Reformat code");
        TestUtils.wait(200);
        editor.clickForPopup();
        new JPopupMenuOperator().pushMenu(_reformatCode);
        TestUtils.wait(200);
        log("Editor Dump:");
        log(editor.getText());
        log("Switch back to Designer");
        designer.makeComponentVisible();
        // TestUtils.wait(10000);
        // Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }

    /*
     *  Verfiy JPS code. Check for action="#{Page1.button1_action} "
     */
    public void testVerifyJSPEditor() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.switchToJSPSource();
        TestUtils.wait(2000);
        log("Verify action tag  should present in JSP editor");
        String expectedStr = Bundle.getStringTrimmed(_bundle, "actionTag") + "=\"#{Page1.button1_action}\"";
        log(expectedStr);
        JEditorPaneOperator editor = new JEditorPaneOperator(RaveWindowOperator.getDefaultRave());
        assertFalse("There is no \"" + expectedStr + "\" string in jsp editor",
                editor.getText().indexOf(expectedStr) == -1);
        log("Switch back to Designer");
        designer.makeComponentVisible();
        TestUtils.wait(2000);
        endTest();
    }

    /*
     * Change text property via property sheet. verify JSP code
     */
    public void testChangeTextInPropertySheet() {
        startTest();
        log("Set button's text property via property sheet");
        designer.clickMouse(xButton, yButton, 1);
        TestUtils.wait(1000);
        sheet = new SheetTableOperator();

        //ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Stoplight");
        sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyText"), "Stoplight");
        TestUtils.wait(5000);

        log("Switch to JSP page. Verify that the jsp source has been updated");
        // designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.switchToJSPSource();
        TestUtils.wait(2000);
        String expectedStr = "text=\"Stoplight";
        JEditorPaneOperator editor = new JEditorPaneOperator(RaveWindowOperator.getDefaultRave());
        assertFalse("There is no \"" + expectedStr + "\" string in jsp editor",
                editor.getText().indexOf(expectedStr) == -1);

        log("Switch back to Designer");
        designer.makeComponentVisible();
        TestUtils.wait(2000);
        endTest();
    }

    /*
     * Change text property in JSP code. verify it from property sheet
     */
    public void testChangeTextInJSPEditor() {
        startTest();

        log("Set button's text in JSP Edtor");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.switchToJSPSource();
        TestUtils.wait(1000);

        JEditorPaneOperator editor = new JEditorPaneOperator(RaveWindowOperator.getDefaultRave());
        editor.selectText("Stoplight");
        editor.pushKey(KeyEvent.VK_DELETE);
        editor.typeText("Trafficlight");
        TestUtils.wait(200);

        log("Switch to designer. Verify that  propertry sheet has been updated");
        designer.makeComponentVisible();
        TestUtils.wait(2000);

        sheet = new SheetTableOperator();
        TestUtils.wait(2000);

        String expectedStr = "Trafficlight";
        log(sheet.getValue(Bundle.getStringTrimmed(_bundle, "propertyText")));
        assertFalse("There is no \"" + expectedStr + "\" string property sheet",
                sheet.getValue(Bundle.getStringTrimmed(_bundle, "propertyText")).equals(expectedStr));

        TestUtils.wait(500);
        Util.saveAllAPICall();
        TestUtils.wait(1000);
        endTest();
    }

    /*
     * Deploy application
     */
    public void testDeploy() {
        startTest();
        //need to wait responce
        Waiter deploymentWaiter = new Waiter(new Waitable() {

            public Object actionProduced(Object output) {
                String text = ((OutputOperator) output).getText();
                if (text.indexOf(_buildSuccess) != -1) {
                    return _true;
                }
                return null;
            }

            public String getDescription() {
                return ("Waiting Project Deployed");
            }
        });
        log("Deploy from menu");
        ProjectNavigatorOperator.pressPopupItemOnNode(_projectName, _run);
        TestUtils.wait(2000);
        OutputOperator outputWindow = new OutputOperator();
        deploymentWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 240000);
        log("wait until " + _buildSuccess);
        try {
            deploymentWaiter.waitAction(outputWindow);
        } catch (InterruptedException e) {
            log(outputWindow.getText());
            e.printStackTrace();
            fail("Deployment error: " + e);
        }
        log("Deployment complete");
        endTest();
    }

    public void testCloseProject() {
        startTest();
        Util.saveAllAPICall();
        new ProjectNavigatorOperator().pressPopupItemOnNode(_projectName, Bundle.getStringTrimmed(_bundle,
                "CloseProjectPopupItem"));
        //TestUtils.closeCurrentProject();
        TestUtils.wait(5000);
        endTest();
    }

    /* Need to undeploy project to finish tests correctly */
    public void testUndeploy() {
        startTest();
        log("Initialize");
        explorer = ServerNavigatorOperator.showNavigatorOperator();
        String serverPath = _serverPath + _projectServer;  //Current deployment server
        String deploymentPath = serverPath + _deploymentPath; //glassfish specific
        String applicationPath = deploymentPath + _separator + _projectName; //project name

        // Select the Server Navigator and set the JTreeOperator
        log("get explorer");
        new QueueTool().waitEmpty(100); //??
        explorer.requestFocus();
        JTreeOperator tree = explorer.getTree();
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
        } // Sleep 4 secs to make sure Server Navigator is in focus

        // Need to refresh J2EE AppServer node
        log("refresh");
        explorer.pushPopup(tree, serverPath, _refresh);
        TestUtils.wait(1000);

        log("refresh deployment path: " + deploymentPath);
        TestUtils.wait(1000);
        explorer.selectPath(deploymentPath);
        explorer.getTree().expandPath(explorer.getTree().findPath(deploymentPath));
        explorer.pushPopup(tree, deploymentPath, _refresh);
        TestUtils.wait(1000);

        log("undeploy Path: " + applicationPath);
        explorer.selectPath(applicationPath);
        TestUtils.wait(1000);

        log("Push Menu Undeploy...");
        explorer.pushPopup(explorer.getTree(), applicationPath, _undeploy);
        TestUtils.wait(5000);
        endTest();
    }

    public void testCheckIDELog() {
        startTest();
        try {
            String err = ComponentUtils.hasUnexpectedException();
            String str = "";
            if (!(err.equals(""))) {
                assertTrue("Unexpected  exceptions found in message.log: " + err, str.equals(""));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Failed to open message.log : " + ioe);
        }
        endTest();
    }
}


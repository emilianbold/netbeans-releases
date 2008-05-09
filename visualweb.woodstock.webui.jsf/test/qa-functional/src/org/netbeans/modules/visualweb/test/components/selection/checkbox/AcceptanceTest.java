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

package  org.netbeans.modules.visualweb.test.components.selection.checkbox;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * @author Sherry Zhou (sherry.zhou@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    public String _bundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _projectName = "CheckboxAcceptanceTest";
    public String _projectServer = Bundle.getStringTrimmed(_bundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_bundle,"logFile");
    public String _logFile = System.getProperty("xtest.sketchpad") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_bundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_bundle,"close");
    public String _run = Bundle.getStringTrimmed(_bundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_bundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_bundle,"true");
    
    public String basicPalette=Bundle.getStringTrimmed(_bundle, "basicPalette");
    public static int xCheckbox1=50;
    public static int yCheckbox1=50;
    public static int xCheckbox2=150;
    public static int yCheckbox2=50;
    public static int xCheckbox3=250;
    public static int yCheckbox3=50;
    public static int xMessageGroup=100;
    public static int yMessageGroup=200;
    public static int xButton=150;
    public static int yButton=150;

    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_bundle, "separator");
    
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ServerNavigatorOperator explorer;
    public String imageDir=ComponentUtils.getDataDir() + "selection" + File.separator;
    String image1= imageDir + "red.gif";
    String image2= imageDir + "white.gif";
    String image3= imageDir + "blue.gif";
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new TestSuite();
        suite.addTest(new AcceptanceTest("testCreateProject"));
        suite.addTest(new AcceptanceTest("testAddCheckboxes"));
        suite.addTest(new AcceptanceTest("testAddButtonActionEvent"));
        suite.addTest(new AcceptanceTest("testDeploy"));
        suite.addTest(new AcceptanceTest("testCloseProject"));
        suite.addTest(new AcceptanceTest("testUndeploy"));
        suite.addTest(new AcceptanceTest("testCheckIDELog"));
        
        return suite;
    }
    
    /** method called before each testcase
     */
    protected void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** method called after each testcase
     */
    protected void tearDown() {
        System.out.println("########  "+getName()+" Finished #######");
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
        //Create Project
        try {
            ComponentUtils.createNewProject(_projectName);
            Util.wait(10000);
        } catch(Exception e) {
            log(">> Project Creation Failed");
            e.printStackTrace();
            log(e.toString());
            fail();
        }
        log("**Done");
        endTest();
    }
    
    /*
     *   Add 3 checkbox components. Set id, label, and imageURL properties
     */
    
    public void testAddCheckboxes() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(basicPalette);
        Util.wait(2000);
        
        // Add first checkbox component, set its id, label and imageURl property
        Point dropPoint = new Point(xCheckbox1, yCheckbox1);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicCheckbox"), designer, dropPoint);
        //palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicCheckbox"), designer, dropPoint);
        Util.wait(2000);
        sheet = new SheetTableOperator();
        Util.wait(2000);
        //  ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyId"), "colorCheckbox1");
        sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyId"), "colorCheckbox1");
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyLabel"), "Red");
        log(image1);
        sheet.setImage("checkbox", Bundle.getStringTrimmed(_bundle, "propertyImageURL"), image1);
        Util.wait(2000);
        
        // Add second checkbox component, set its id, label and imageURl property
        // palette = new PaletteContainerOperator(basicPalette);
        palette = new PaletteContainerOperator(basicPalette);
        dropPoint = new Point(xCheckbox2, yCheckbox2);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicCheckbox"), designer, dropPoint);
        Util.wait(8000);
        sheet = new SheetTableOperator();
        Util.wait(2000);
        sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyId"), "colorCheckbox2");
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyLabel"), "White");
        //Insync bug. id does not update build 060909
        sheet.setImage("checkbox", Bundle.getStringTrimmed(_bundle, "propertyImageURL"), image2);
        Util.wait(2000);
        
        // Add third checkbox component, set its id, label and imageURl property
        palette = new PaletteContainerOperator(basicPalette);
        dropPoint = new Point(xCheckbox3, yCheckbox3);
        //palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicCheckbox"), designer, dropPoint);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicCheckbox"), designer, dropPoint);
        Util.wait(8000);
        sheet = new SheetTableOperator();
        Util.wait(2000);
        sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyId"), "colorCheckbox3");
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyLabel"), "Blue");
        //Insync bug. id does not update
        sheet.setImage("checkbox", Bundle.getStringTrimmed(_bundle, "propertyImageURL"), image3);
        Util.wait(2000);
        
        palette = new PaletteContainerOperator(basicPalette);
        dropPoint =  new Point(xButton, yButton);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicButton"), designer, dropPoint);
        Util.wait(2000);
        
        palette = new PaletteContainerOperator(basicPalette);
        dropPoint = new Point(xMessageGroup, yMessageGroup);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicMessageGroup"), designer, dropPoint);
        Util.wait(2000);
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    
    public void testAddButtonActionEvent() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.makeComponentVisible();
        // Double click at button to open Jave Editor
        designer.clickMouse(xButton+1, yButton+1, 2);
        TestUtils.wait(1000);
        EditorOperator editor = new EditorOperator(Util.getMainWindow(), "Page1.java");
        editor.setVerification(false);
        TestUtils.wait(2000);
        editor.requestFocus();
        TestUtils.wait(2000);
        editor.pushKey(KeyEvent.VK_ENTER);
        editor.insert("log(\"Action Performed.\");\n");        
        // Switch to design panel
        designer.makeComponentVisible();
        TestUtils.wait(10000);
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
                String text = ((OutputOperator)output).getText();
                if (text.indexOf(_buildSuccess)!=-1)
                    return _true;
                return null;
                
            }
            public String getDescription() {
                return("Waiting Project Deployed");
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
        } catch(InterruptedException e) {
            log(outputWindow.getText());
            e.printStackTrace();
            fail("Deployment error: "+e);
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
        try { Thread.sleep(4000); } catch(Exception e) {} // Sleep 4 secs to make sure Server Navigator is in focus

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


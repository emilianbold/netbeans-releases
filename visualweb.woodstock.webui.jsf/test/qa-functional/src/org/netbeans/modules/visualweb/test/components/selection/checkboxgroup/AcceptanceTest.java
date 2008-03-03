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

package  org.netbeans.modules.visualweb.test.components.selection.checkboxgroup;

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
import org.netbeans.jemmy.operators.*;
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
    public String _projectName = "CheckboxGroupAcceptanceTest";
    public String _projectServer = Bundle.getStringTrimmed(_bundle, "projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_bundle, "logFile");
    public String _logFile = System.getProperty("xtest.sketchpad") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_bundle, "Exception");
    public String _close = Bundle.getStringTrimmed(_bundle, "close");
    public String _run = Bundle.getStringTrimmed(_bundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_bundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_bundle,"true");
 
    public static int xCheckboxGroup=50;
    public static int yCheckboxGroup=50;
    public static int xMessageGroup=50;
    public static int yMessageGroup=100;
    public static int xButton=100;
    public static int yButton=200;
     
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_bundle, "separator");
    
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ServerNavigatorOperator explorer;
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new TestSuite();
        suite.addTest(new AcceptanceTest("testCreateProject"));
        suite.addTest(new AcceptanceTest("testAddCheckboxGroup"));
       // suite.addTest(new AcceptanceTest("testCheckboxGroupDBBinding"));
      //  suite.addTest(new AcceptanceTest("testAddSessionBeanProperties"));
       // suite.addTest(new AcceptanceTest("testBindSelectedProperty"));
     //   suite.addTest(new AcceptanceTest("testAddButtonActionEvent"));
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
     *    
     */
    public void testCreateProject() {
        startTest();
        log("**Creating Project");
        //Create Project
        try {
            ComponentUtils.createNewProject(_projectName);
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
     *   Add first CheckboxGroup components. Set properties
     */
    
    public void testAddCheckboxGroup() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        
        log("Add first CheckboxGroup component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(10000);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicCheckboxGroup"), designer, new Point(xCheckboxGroup, yCheckboxGroup));
        
        log("set its label, required, cloumns properties");
        sheet = new SheetTableOperator();
        Util.wait(2000);
        // sheet.setButtonValue() can't location lable property if labelOnTop is in property sheet
        // So use  ComponentUtils.setProperty()
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyLabel"), "Select a Person(s)");
        Util.wait(500);
        sheet.setCheckBoxValue(Bundle.getStringTrimmed(_bundle, "propertyRequired"), "true");
        Util.wait(500);
        // sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyColumns"), "5");
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyColumns"), "6");
        Util.wait(500);
         
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    public void testCheckboxGroupDBBinding() {
        startTest();
        String _dataProvider = "customerDataProvider";
        String _dbName=Bundle.getStringTrimmed(_bundle, "Databses_SampleDBNode");
        String _passwd="app";
        String _tableName=Bundle.getStringTrimmed(_bundle, "Databases_CustomerTableNode");
        log(_tableName);
        ComponentUtils.connectDB(_dbName, _passwd); 
        ComponentUtils.bindToDataProvider(xCheckboxGroup+1, yCheckboxGroup+1, _tableName, _dataProvider, "NAME", "NAME" );
        endTest();
    }
    
    public void testAddSessionBeanProperties() {
        startTest();
        log("Add SessionBean propert - choices");
        String path=_projectName+"|"+Bundle.getStringTrimmed(_bundle, "SessionBean_Path");
        // Put long sleep to wait for scanning project classpath finish, otherwisethe node won't expand
 	  Util.wait(10000);

        ComponentUtils.addObjectProperty( path,
                "choices", "String[]", 
                Bundle.getStringTrimmed(_bundle, "BeanPattern_Mode_ReadWrite"));
        Util.wait(500);

        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    public void testBindSelectedProperty() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        ComponentUtils.setPropertyBinding(designer, xCheckboxGroup, yCheckboxGroup, "selected", "#{SessionBean1.choices}");
        endTest();
    }
    
    public void testAddButtonActionEvent() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.makeComponentVisible();
        
        log("Add button and message group component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(2000);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicButton"), designer, new Point(xButton, yButton));
        Util.wait(500);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicMessageGroup"), designer, new Point(xMessageGroup, yMessageGroup));
        Util.wait(500);
        // Double click at button to open Jave Editor
        designer.clickMouse(xButton+1, yButton+1, 2);
        TestUtils.wait(1000);
        JEditorPaneOperator editor = new JEditorPaneOperator(
                RaveWindowOperator.getDefaultRave(), "public class " + "Page1");
        
        editor.setVerification(false);
        TestUtils.wait(2000);
        editor.requestFocus();
        TestUtils.wait(2000);
        editor.pushKey(KeyEvent.VK_ENTER);
        editor.setText("String showSelections=\"You selected \"; \n");
        editor.setText("String[] mySelections = getSessionBean1().getChoices();\n");
        editor.setText("for (int i = 0; i < mySelections.length; i++) \n");
        editor.setText("showSelections+=mySelections[i] +\" \"; \n");
        //  editor.setText(("} \n");
        editor.setText("if (showSelections.equals(\"\")) \n");
        editor.setText("showSelections = \"nothing selected\"; \n");
        editor.setText("else \n");
        editor.setText("showSelections = \"Values chosen:\n\" + showSelections; \n");
        editor.setText("// Display the list in the message group \n");
        editor.setText("info(showSelections);  \n");
        TestUtils.wait(200);
        
        log("Reformat code");
        editor.clickForPopup();
        new JPopupMenuOperator().pushMenu("Reformat Code");
        TestUtils.wait(200);
        
        // Switch to design panel
        designer.makeComponentVisible();
        TestUtils.wait(10000);
        Util.saveAllAPICall();
        Util.wait(2000);
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


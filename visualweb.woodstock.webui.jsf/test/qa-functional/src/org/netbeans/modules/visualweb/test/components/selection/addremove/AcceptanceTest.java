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

package   org.netbeans.modules.visualweb.test.components.selection.addremove;

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
    
    public String _bundle = ComponentUtils.getBundle();
    public String _projectName = "AddRemoveAcceptanceTest";
    public String _projectServer = Bundle.getStringTrimmed(_bundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_bundle,"logFile");
    public String _logFile = System.getProperty("xtest.workdir") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_bundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_bundle,"close");    
    public String _run = Bundle.getStringTrimmed(_bundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_bundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_bundle,"true");
    public String _projectJ2EEVersion = Bundle.getStringTrimmed(_bundle,"projectJ2EEVersion");

    public static int xAddRemove=50;
    public static int yAddRemove=150;
    public static int xMessage=150;
    public static int yMessage=50;
    public static int xButton=50;
    public static int yButton=50;
    String addRemoveID="addRemoveList1";
    String addRemoveLabel="Lab Systems to be Upgraded:";
    String addRemoveToolTip="Please select the lab systems to be upgraded.";
    String importStatement="import " + Bundle.getStringTrimmed(_bundle, "optionType")+";";
    //String[] javaCode1 =  {"import com.sun.rave.web.ui.model.Option;"};
    String[] javaCode1 =  {importStatement};
    String[] javaCode2 =  {  " Option[] ilmHosts = new Option[] { "};
    String[] javaCode3 =  {
        "new Option(\"bsqe-falcon.sfbay\"),",
        "new Option(\"rave-shark.sfbay\"),",
        "new Option(\"rave-express.sfbay\"),",
        "new Option(\"clue.sfbay\"),",
        "new Option(\"rave-sol.eng\"),",
        "new Option(\"sheezy.sfbay\"),",
        "new Option(\"caliban.sfbay\"),",
        "new Option(\"rave-eagle.sfbay\"),",
        "new Option(\"raveqe-fire.sfbay\")"
    };
    String[] javaCode4 = {"getSessionBean1().setLabMachines(ilmHosts);"};
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_bundle, "separator");
    
    //Outline variables
    public String _outlineForm1 = Bundle.getStringTrimmed(_bundle,"outlineForm1");
	
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ServerNavigatorOperator explorer;
	public static DocumentOutlineOperator outline;
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new TestSuite();
        suite.addTest(new AcceptanceTest("testCreateProject"));
        suite.addTest(new AcceptanceTest("testAddComponents"));
        suite.addTest(new AcceptanceTest("testConfigureAddRemove"));
       // suite.addTest(new AcceptanceTest("testAddSessionBeanProperty"));
       // suite.addTest(new AcceptanceTest("testEditJavaSource"));
       // suite.addTest(new AcceptanceTest("testAddRemoveDataBinding"));
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
        log(_projectJ2EEVersion);
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
     *   Add an addremove, button and message  components.
     */
    
    public void testAddComponents() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        
        log("Add a button component.");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(2000);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicButton"), designer, new Point(xButton, yButton));
//        sheet = new SheetTableOperator();
//        sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyText"), "Get Machines");
        //ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Get Machines");
        Util.wait(500);
        
        log("Add an AddRemove component");
        palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "compositePalette"));
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "compositeAddRemove"), designer, new Point(xAddRemove, yAddRemove));
        Util.wait(2500);
        
//        log("Add a message component");
//        palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
//        Util.wait(2000);
//        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicMessage"), designer, new Point(xMessage, yMessage));
//        Util.wait(500);
//        sheet = new SheetTableOperator();
//         Util.wait(1500);
//        sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyFor"), "addRemoveList1");
//        Util.wait(500);
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    public void testConfigureAddRemove() {
        startTest();
// causes ee4 to lock up
//        log("Select AddRemove Component");
//        designer.clickMouse(addRemoveID, 1);
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);

        log("**Select addRemove1 from Outline window");
        String path = _outlineForm1 + "addRemoveList1";
        doo.verify();
//        doo.clickOnPath(path);
        doo.selectPath(path);
        Util.wait(5000);        

		if (_projectJ2EEVersion.equals("J2EE 1.4")) {
			// sheet calls hang in EE4 project.
//			sheet = new SheetTableOperator();
//			sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyLabel"), addRemoveLabel);
//			Util.wait(500);
		}
		else {
//        Util.wait(500);
			log("Set following properties to True: LabelOnTop,moveButtons, selectetAll, requried, sort");
			sheet = new SheetTableOperator();
			sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyLabel"), addRemoveLabel);
			Util.wait(500);
			sheet.setCheckBoxValue(Bundle.getStringTrimmed(_bundle, "propertyLabelOnTop"), "true");
			Util.wait(500);
			sheet.setCheckBoxValue(Bundle.getStringTrimmed(_bundle, "propertyMoveButtons"), "true");
			Util.wait(500);
			sheet.setCheckBoxValue(Bundle.getStringTrimmed(_bundle, "propertySelectAll"), "true");
			Util.wait(500);
			sheet.setCheckBoxValue(Bundle.getStringTrimmed(_bundle, "propertyRequired"), "true");
			Util.wait(500);
			sheet.setCheckBoxValue(Bundle.getStringTrimmed(_bundle, "propertySorted"), "true");
			Util.wait(500);
			ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyToolTip"), addRemoveToolTip);
			Util.wait(500);
		}
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    public void testAddSessionBeanProperty() {
        startTest();
        String path=_projectName+"|"+Bundle.getStringTrimmed(_bundle, "SessionBean_Path");
        String dataType=Bundle.getStringTrimmed(_bundle, "optionType")+"[]";
        log("Datatype="+dataType);
        ComponentUtils.addObjectProperty( path,
                "labMachines", dataType,
                Bundle.getStringTrimmed(_bundle, "BeanPattern_Mode_ReadWrite"));
        endTest();
    }
    
    
    public void testEditJavaSource() {
        startTest();
        log("Double click at designer to open Jave Editor");
        designer=new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickMouse(20, 20, 2);
        TestUtils.wait(1000);
        
        log("Enter import statement");
        EditorOperator editor = new EditorOperator(Util.getMainWindow(), "Page1.java");
        editor.setCaretPosition("public class " + "Page1", true);
        editor.pushKey(KeyEvent.VK_ENTER);
        TestUtils.wait(1000);
 	log(importStatement);
        ComponentUtils.insertJavaCode(editor, javaCode1);
        editor.pushKey(KeyEvent.VK_ENTER);
        editor.pushKey(KeyEvent.VK_ENTER);
        TestUtils.wait(1000);
        
        log("Enter init sessionbean property code");
        editor.setCaretPosition("public void init() {", false);
        TestUtils.wait(1000);
        editor.pushKey(KeyEvent.VK_ENTER);
        ComponentUtils.insertJavaCode(editor, javaCode2);
        editor.pushKey(KeyEvent.VK_ENTER);
        editor.pushKey(KeyEvent.VK_DOWN);
        editor.insert(";");
        editor.pushKey(KeyEvent.VK_UP);
        ComponentUtils.insertJavaCode(editor, javaCode3);
        editor.pushKey(KeyEvent.VK_DOWN);
        editor.pushKey(KeyEvent.VK_ENTER);
        ComponentUtils.insertJavaCode(editor, javaCode4);
        TestUtils.wait(1000);
        
        log("Switch to design panel");
        designer.makeComponentVisible();
        
        TestUtils.wait(5000);
        Util.saveAllAPICall();
        TestUtils.wait(2000);
        endTest();
    }
    
    public void testAddRemoveDataBinding() {
        
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        ComponentUtils.setPropertyBinding(designer, xAddRemove+5, yAddRemove+5,
               Bundle.getStringTrimmed(_bundle, "propertyItems"), "#{SessionBean1.labMachines}" );
 
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


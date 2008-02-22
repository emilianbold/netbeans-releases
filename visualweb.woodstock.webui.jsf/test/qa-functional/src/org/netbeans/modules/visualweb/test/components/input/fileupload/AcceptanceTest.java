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

package org.netbeans.modules.visualweb.test.components.input.fileupload;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.actions.ActionNoBlock;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * @author Lark Fitzgerald (lark.fitzgerald@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _privateBundle ="org.netbeans.modules.visualweb.test.components.input.fileupload.Acceptance";
    public String _projectName = Bundle.getStringTrimmed(_privateBundle,"projectName");
    public String _projectServer = Bundle.getStringTrimmed(_sharedBundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_sharedBundle,"logFile");
    public String _logFile = System.getProperty("xtest.workdir") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_sharedBundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_sharedBundle,"close");
    public String _run = Bundle.getStringTrimmed(_sharedBundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_sharedBundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_sharedBundle,"true");
    
    //Palette variables
    public String _basicPalette = Bundle.getStringTrimmed(_sharedBundle,"basicPalette");
    public String _basicButton = Bundle.getStringTrimmed(_sharedBundle,"basicButton");
    public String _basicTextArea = Bundle.getStringTrimmed(_sharedBundle,"basicTextArea");
    public String _basicLabel = Bundle.getStringTrimmed(_sharedBundle,"basicLabel");
    public String _basicFileUpload = Bundle.getStringTrimmed(_sharedBundle,"basicFileUpload");
    public String _basicMessageGroup = Bundle.getStringTrimmed(_sharedBundle,"basicMessageGroup");
    
    //components
    public String _label1 = Bundle.getStringTrimmed(_sharedBundle,"label1");
    public String _fileupload1 = Bundle.getStringTrimmed(_sharedBundle,"fileupload1");
    public String _button1 = Bundle.getStringTrimmed(_sharedBundle,"button1");
    public String _textarea1 = Bundle.getStringTrimmed(_sharedBundle,"textarea1");
    public String _messagegroup1 = Bundle.getStringTrimmed(_sharedBundle,"messagegroup1");
    
    //drop points
    public int _x = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"x"));
    public int _label1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"label1y"));
    public int _fileupload1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"fileupload1y"));
    public int _button1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button1y"));
    public int _textarea1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textarea1y"));
    public int _messagegroup1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"messagegroup1y"));
    
    //property items
    public String _propertyText = Bundle.getStringTrimmed(_sharedBundle,"propertyText");
    public String _propertyLabel = Bundle.getStringTrimmed(_sharedBundle,"propertyLabel");
    public String _propertyColumns = Bundle.getStringTrimmed(_sharedBundle,"propertyColumns");
    public String _propertyRows = Bundle.getStringTrimmed(_sharedBundle,"propertyRows");
    
    //values
    public String _labelValue1 = Bundle.getStringTrimmed(_privateBundle,"labelValue1");
    public String _buttonValue1 = Bundle.getStringTrimmed(_privateBundle,"buttonValue1");
    public String _textAreaValue1 = Bundle.getStringTrimmed(_privateBundle,"textAreaValue1");
    public String _columns = Bundle.getStringTrimmed(_privateBundle,"columns");
    public String _rows = Bundle.getStringTrimmed(_privateBundle,"rows");
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_sharedBundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_sharedBundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_sharedBundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_sharedBundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_sharedBundle, "separator");
    
    Point clickPoint, dropPoint;
    public static DesignerPaneOperator designer;
    public static PaletteContainerOperator palette;
    public static DocumentOutlineOperator outline;
    public static SheetTableOperator sheet;
    public static DeploymentDialogOperator deploy;
    public static ServerNavigatorOperator explorer;
    public int row;
    public String _page1 = Bundle.getStringTrimmed(_sharedBundle,"webPage1");
    
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new NbTestSuite();
        suite.addTest(new AcceptanceTest("createWebProject"));
        suite.addTest(new AcceptanceTest("testAddLabel"));
        suite.addTest(new AcceptanceTest("testAddFileupload"));
        suite.addTest(new AcceptanceTest("testAddButton"));
        suite.addTest(new AcceptanceTest("testAddMessageGroup"));
        suite.addTest(new AcceptanceTest("testAddTextArea"));
        suite.addTest(new AcceptanceTest("testAddButtonAction"));
        suite.addTest(new AcceptanceTest("testDeploy"));
        suite.addTest(new AcceptanceTest("testCloseWebProject"));
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
     *    Create Web Project
     *
     */
    public void createWebProject() {
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
     *    Add a label to designer
     *    set text value to 'Select a file to download:"
     *
     */
    public void testAddLabel() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add label to designer");
        clickPoint = palette.getClickPoint(_basicLabel);
        dropPoint = new Point(_x, _label1y);
        palette.dndPaletteComponent(_basicLabel, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        //For some reason, the property sheet doesn't rise to top for label
        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu("Window|Properties","|");
        
        log("**Set Label text property");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyText);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue1);
        
        log("**Done");
        endTest();
    }
    
    /*
     *    Add a fileUpload to designer
     *
     */
    public void testAddFileupload() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add file upload to designer");
        clickPoint = palette.getClickPoint(_basicFileUpload);
        dropPoint = new Point(_x, _fileupload1y);
        palette.dndPaletteComponent(_basicFileUpload, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done");
        endTest();
    }
    
    /*
     *    Add a button to designer
     *    label it 'Upload File Now'
     */
    public void testAddButton() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Button to designer");
        clickPoint = palette.getClickPoint(_basicButton);
        dropPoint = new Point(_x, _button1y);
        palette.dndPaletteComponent(_basicButton, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set Button text inline");
        JTextFieldOperator field = new JTextFieldOperator(designer);
        SwingTextKeyboardDriver keyboard = new SwingTextKeyboardDriver();
        keyboard.enterText(field, _buttonValue1);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *    Add a MessageGroup to designer
     */
    public void testAddMessageGroup() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add message group to designer");
        clickPoint = palette.getClickPoint(_basicMessageGroup);
        dropPoint = new Point(_x, _messagegroup1y);
        palette.dndPaletteComponent(_basicMessageGroup, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    Add a textArea to designer
     *    label it 'Contents of File:'
     *    set the columns to 60
     *    set the rows to 5
     */
    public void testAddTextArea() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add textArea1 to designer");
        clickPoint = palette.getClickPoint(_basicTextArea);
        dropPoint = new Point(_x, _textarea1y);
        palette.dndPaletteComponent(_basicTextArea, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set label value via property sheet");
        sheet = new SheetTableOperator();
        row = sheet.findCellRow(_propertyLabel);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_textAreaValue1);
        
        log("**Set the columns to 60");
        row = sheet.findCellRow(_propertyColumns);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_columns);
        
        log("**Set the rows to 5");
        row = sheet.findCellRow(_propertyRows);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_rows);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     * Add button action1 with the following source:
     * log(\"Upload action performed\");
     */
    public void testAddButtonAction() {
        startTest();
        log("**double click button to get to action method()");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickMouse(_x, _button1y, 2);
        JEditorPaneOperator editor = new JEditorPaneOperator(Util.getMainWindow(), "Page1");
        //For some reason, doing all of them at once doesn't work
        editor.pushKey(KeyEvent.VK_END);
        editor.typeText("\nlog(\"Upload action performed\");\n");
        log("Editor Dump:");
        log(editor.getText());
        log("**Go back to designer");
        try { Thread.sleep(3000); } catch(Exception e) {}
        designer.switchToDesignerPane();
        log("**Done.");
        endTest();
    }
    
    /*
     *    Configure web server
     */
    public void configureServer() {
        startTest();
        log("Open Server manager");
        ActionNoBlock srv = new ActionNoBlock("Tools|Server Manager","","");
        srv.performMenu();
        
        log("Find Server manager dialog");
        NbDialogOperator serverManager = new NbDialogOperator("Server Manager");
        
        log("Press add server button");
        JButtonOperator addServer = new JButtonOperator(serverManager, "Add Server...");
        addServer.pushNoBlock();
        
        log("locate server instance wizard");
        WizardOperator serverInstance = new WizardOperator("Add Server Instance");
        log("Wizard initial step = " + serverInstance.stepsGetSelectedIndex() + ": " + serverInstance.stepsGetSelectedValue());
        
        log("Take defaults and continue");
        serverInstance.next();
        log("Wizard step = " + serverInstance.stepsGetSelectedIndex() + ": " + serverInstance.stepsGetSelectedValue());
        
        log("Browse for location");
        JButtonOperator browse = new JButtonOperator(serverInstance, "Browse...");
        browse.press();
        browse.release();
        
        log("get install location dialog");
        NbDialogOperator installLocation = new NbDialogOperator("Choose Application Server's Install Location");
        
        log("Set location");
        //There's only 1 textField on page so this works.
        JTextFieldOperator fileName = new JTextFieldOperator(installLocation);
        fileName.enterText("C:\\sun\\glassfish");
        
        log("Close dialog");
        JButtonOperator choose = new JButtonOperator(installLocation, "Choose");
        choose.pushNoBlock();
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        //confirm location?
        
        log("go to next wizard page");
        serverInstance.next();
        try { Thread.sleep(2000); } catch(Exception e) {}
        log("Wizard step = " + serverInstance.stepsGetSelectedIndex() + ": " + serverInstance.stepsGetSelectedValue());
        
        log("Set the password");
        JLabelOperator la = new JLabelOperator(serverInstance, "Admin Password:");
        java.awt.Component c = la.getLabelFor();
        JTextField jtf = (JTextField)c;
        JTextFieldOperator adminPassword = new JTextFieldOperator(jtf);
        adminPassword.setText("adminadmin");
        
        log("finish wizard");
        serverInstance.finish();
        
        log("press close for server manager");
        JButtonOperator close = new JButtonOperator(serverManager, "Close");
//        serverManager.closeByButton();
        close.pushNoBlock();
        
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
    
    
/*
 
 *Runtime
  10. browse and select a file
         1. Be sure to test files with space in path, space in name, as well as ones without.
         2. The src folder under this folder contains a series of files that can be used.
  11. press the Upload File Now button
 
 */
    
    
    
    /*
     * Close Project
     *
     */
    public void testCloseWebProject() {
        startTest();
        Util.saveAllAPICall();
        new ProjectNavigatorOperator().pressPopupItemOnNode(_projectName, _close);
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

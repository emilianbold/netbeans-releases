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

package org.netbeans.modules.visualweb.test.components.layout.pagefragment;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

import java.io.File;
import java.awt.Point;
import java.io.IOException;

/**
 * @author Lark Fitzgerald (lark.fitzgerald@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _privateBundle ="org.netbeans.modules.visualweb.test.components.layout.pagefragment.Acceptance";
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
    public String _layoutPalette = Bundle.getStringTrimmed(_sharedBundle,"layoutPalette");
    public String _layoutPageFragment = Bundle.getStringTrimmed(_sharedBundle,"layoutPageFragment");
    public String _basicTextField = Bundle.getStringTrimmed(_sharedBundle,"basicTextField");
    public String _basicLabel = Bundle.getStringTrimmed(_sharedBundle,"basicLabel");
    public String _basicMessage = Bundle.getStringTrimmed(_sharedBundle,"basicMessage");

    //Component names & values
    public String _labelValue1 = Bundle.getStringTrimmed(_privateBundle,"labelValue1");
    public String _textField1 = Bundle.getStringTrimmed(_sharedBundle,"textField1");
    public String _selectPageFragment = Bundle.getStringTrimmed(_sharedBundle,"selectPageFragment");
    public String _createNewPageFragment = Bundle.getStringTrimmed(_sharedBundle,"createNewPageFragment");
    public String _createPageFragment = Bundle.getStringTrimmed(_sharedBundle,"createPageFragment");
    public String _fragmentName1 = Bundle.getStringTrimmed(_privateBundle,"fragmentName1");

    //Property, menu & button variables
    public String _propertyText = Bundle.getStringTrimmed(_sharedBundle,"propertyText");
    public String _propertyFor = Bundle.getStringTrimmed(_sharedBundle,"propertyFor");
    public String _propertyRequired = Bundle.getStringTrimmed(_sharedBundle,"propertyRequired");
    public String _properties = Bundle.getStringTrimmed(_sharedBundle,"properties");
    public String _ok = Bundle.getStringTrimmed(_sharedBundle,"Button_OK");

    //drop points
    public int _x = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"x"));
    public int _pagefragment1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"pagefragment1y"));
    public int _button1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button1y"));
    public int _textfield1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield1y"));
    public int _message1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message1y"));
    public int _label1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"label1y"));
    
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

    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new NbTestSuite();
        suite.addTest(new AcceptanceTest("testCreateWebProject"));
        suite.addTest(new AcceptanceTest("testAddPageFragment"));
        suite.addTest(new AcceptanceTest("testOpenPageFragment"));
        suite.addTest(new AcceptanceTest("testBuildFragmentPage"));
//        suite.addTest(new AcceptanceTest("testDeploy"));
//        suite.addTest(new AcceptanceTest("testCloseWebProject"));
//        suite.addTest(new AcceptanceTest("testUndeploy"));
//        suite.addTest(new AcceptanceTest("testCheckIDELog"));        
        
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
    public void testCreateWebProject() {
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
     *    Add a page fragment to designer
     */
    public void testAddPageFragment() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_layoutPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}

        log("**Add page fragment to designer");
        clickPoint = palette.getClickPoint(_layoutPageFragment);
        dropPoint = new Point(_x, _pagefragment1y);
        palette.dndPaletteComponent(_layoutPageFragment, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("Select Page Fragment Dialog");
      	JDialogOperator pageFragmentDialog = new JDialogOperator(_selectPageFragment);
	new JButtonOperator(pageFragmentDialog, _createNewPageFragment).pushNoBlock();
        TestUtils.wait(1000);
        
        log("Create Page Fragment Dialog");
	JDialogOperator createPageFragment = new JDialogOperator(_createPageFragment);
	new JTextFieldOperator(createPageFragment, 0).setText(_fragmentName1);
	TestUtils.wait(1000);
        
        log("Close dialogs");
	new JButtonOperator(createPageFragment, _ok).pushNoBlock();
	TestUtils.wait(3000);
	new JButtonOperator(pageFragmentDialog, _close).pushNoBlock();        
	TestUtils.wait(3000);

        log("**Done.");
        endTest();
        
    }
    
    /*
     * Open Page Fragment
     */
    public void testOpenPageFragment() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());

        log("**Double click on fragment to open");
        designer.clickMouse(_x + 15, _pagefragment1y + 15, 2);
//        designer.clickMouse(_fragmentName1, 2);
        log("**Done.");
        endTest();        
    }
    
    /*
     *   add textField
     *   add a label for textfield and give it a name.
     *   mark textField required.
     *   add a message component linked to textField3
     */
    public void testBuildFragmentPage() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickMouse();
        palette = new PaletteContainerOperator(_basicPalette); //previously set to Layout
        palette.clickMouse();
//        palette = new PaletteContainerOperator(_basicPalette); //previously set to Layout
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Textfield to designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield1y); //leave room for label
//        palette.
        palette.addComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Add label to designer");
        clickPoint = palette.getClickPoint(_basicLabel);
        dropPoint = new Point(_x, _label1y);
        palette = new PaletteContainerOperator(null); //workaround
        palette.dndPaletteComponent(_basicLabel, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set Label text property");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyText);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue1);
        
        log("**Set Label for property to textField");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textField1);
        try { Thread.sleep(1000); } catch(Exception e) {}
        
        log("**Select textfield");
        designer.clickMouse(_x, _textfield1y, 1);
        try { Thread.sleep(3000); } catch(Exception e) {}
        
        log("**Set required option to true");
        row = sheet.findCellRow(_propertyRequired);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyRequired, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Drag Message onto designer");
        clickPoint = palette.getClickPoint(_basicMessage);
        dropPoint = new Point(_x, _message1y);
        palette = new PaletteContainerOperator(_basicPalette); //workaround
        palette.dndPaletteComponent(_basicMessage, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
//        log("**Set message via shift-drag and drop onto textField");

        log("**Set message for property via property sheet");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textField1);
        
        log("**Add Button to designer");
        clickPoint = palette.getClickPoint(_basicButton);
        dropPoint = new Point(_x, _button1y);
        palette = new PaletteContainerOperator(null); //workaround
        palette.dndPaletteComponent(_basicButton, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
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

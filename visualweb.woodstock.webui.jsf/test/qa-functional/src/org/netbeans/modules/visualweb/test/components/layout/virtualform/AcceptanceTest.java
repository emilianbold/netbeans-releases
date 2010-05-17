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

package org.netbeans.modules.visualweb.test.components.layout.virtualform;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
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
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 * @author Lark Fitzgerald (lark.fitzgerald@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _privateBundle ="org.netbeans.modules.visualweb.test.components.layout.virtualform.Acceptance";
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
    public String _basicTextField = Bundle.getStringTrimmed(_sharedBundle,"basicTextField");
    public String _basicMessageGroup = Bundle.getStringTrimmed(_sharedBundle,"basicMessageGroup");

    //drop points
    public int _x = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"x"));
    public int _textfield1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield1y"));
    public int _button1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button1y"));
    public int _textfield2y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield2y"));
    public int _button2y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button2y"));
    public int _messagegroup1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"messagegroup1y"));
    
    //Property, Menu & Dialog variables
    public String _propertyRequired = Bundle.getStringTrimmed(_sharedBundle,"propertyRequired");
    public String _separator = Bundle.getStringTrimmed(_sharedBundle,"separator");
    public String _menuVirtualForm = Bundle.getStringTrimmed(_sharedBundle,"menuVirtualForm");    
    public String _virtualFormDialog = Bundle.getStringTrimmed(_sharedBundle,"virtualFormDialog");
    public String _new = Bundle.getStringTrimmed(_sharedBundle,"new");
    public String _yes = Bundle.getStringTrimmed(_sharedBundle,"yes");
    public String _ok = Bundle.getStringTrimmed(_sharedBundle,"Button_OK");
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_sharedBundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_sharedBundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_sharedBundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_sharedBundle, "deploymentPathGlassfish");
    
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
        suite.addTest(new AcceptanceTest("testCreateForm"));
        suite.addTest(new AcceptanceTest("testAddVirtualForm1"));
        suite.addTest(new AcceptanceTest("testAddVirtualForm2"));
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
     *    Build Form
     */
    public void testCreateForm() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}

        log("**Add textfield1 to designer");
        clickPoint = palette.getClickPoint(_basicTextField);
        dropPoint = new Point(_x, _textfield1y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(5000); } catch(Exception e) {}

        log("**Set required option to true");
        sheet = new SheetTableOperator();
        row = sheet.findCellRow(_propertyRequired);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyRequired, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}

        log("**Add button1 to designer");
        palette = new PaletteContainerOperator(_basicPalette);
        clickPoint = palette.getClickPoint(_basicButton);
        dropPoint = new Point(_x, _button1y);
        palette.dndPaletteComponent(_basicButton, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Add textfield2 to designer");
        palette = new PaletteContainerOperator(_basicPalette);
        clickPoint = palette.getClickPoint(_basicTextField);
        dropPoint = new Point(_x, _textfield2y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}

        log("**Set required option to true");
        sheet = new SheetTableOperator();
        row = sheet.findCellRow(_propertyRequired);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyRequired, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}

        log("**Add button2 to designer");
        palette = new PaletteContainerOperator(_basicPalette);
        clickPoint = palette.getClickPoint(_basicButton);
        dropPoint = new Point(_x, _button2y);
        palette.dndPaletteComponent(_basicButton, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}

        log("**Add messagegroup1 to designer");
        palette = new PaletteContainerOperator(_basicPalette);
        clickPoint = palette.getClickPoint(_basicMessageGroup);
        dropPoint = new Point(_x, _messagegroup1y);
        palette.dndPaletteComponent(_basicMessageGroup, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    Configure virtual form 1
     */
    public void testAddVirtualForm1() {
        startTest();
//        log("**De-select component (select designer)");
//        designer.clickMouse(5, 5);
//        try { Thread.sleep(1000); } catch(Exception e) {}
        
        log("**Right click textfield1");
        designer.clickForPopup(_x+10, _textfield1y+10);
        try { Thread.sleep(1000); } catch(Exception e) {}
        
        log("**Select " + _menuVirtualForm + " menu item");
        new JPopupMenuOperator().pushMenuNoBlock(_menuVirtualForm, _separator);
        try { Thread.sleep(1000); } catch(Exception e) {}
        
        log("**locate Create Virtual Form dialog");
        NbDialogOperator vf = new NbDialogOperator(_virtualFormDialog);

        log("**Add New Virtual Form");
        JButtonOperator newButton = new JButtonOperator(vf, _new);
        newButton.clickMouse();
        try { Thread.sleep(1000); } catch(Exception e) {}
         
        log("**set participate = Yes");
        JTableOperator vfTable = new JTableOperator(vf);
        vfTable.clickOnCell(0, 2, 2);
        JComboBoxOperator yn = new JComboBoxOperator(vf);
        yn.selectItem(_yes);
        
        log("**Close dialog");
        JButtonOperator okButton = new JButtonOperator(vf, _ok);
        okButton.clickMouse();
        
        log("**Right click button1");
        designer.clickForPopup(_x+10, _button1y+10);
        try { Thread.sleep(3000); } catch(Exception e) {}
        
        log("**Select " + _menuVirtualForm + " menu item");
        new JPopupMenuOperator().pushMenuNoBlock(_menuVirtualForm, _separator);
        try { Thread.sleep(3000); } catch(Exception e) {}

        log("**locate Create Virtual Form dialog");
        vf = new NbDialogOperator(_virtualFormDialog);

        log("**set submit = Yes");
        vfTable = new JTableOperator(vf);
        vfTable.clickOnCell(0, 3, 2);
        yn = new JComboBoxOperator(vf);
        yn.selectItem(_yes);
        
        log("**Close dialog");
        okButton = new JButtonOperator(vf, _ok);
        okButton.clickMouse();
        
        log("**Done.");
        endTest();
    }  
    
    /*
     *    Configure virtual form 2
     */
    public void testAddVirtualForm2() {
        startTest();
        log("**Right click textfield1");
        designer.clickForPopup(_x+10, _textfield2y+10);
        try { Thread.sleep(1000); } catch(Exception e) {}
        
        log("**Select " + _menuVirtualForm + " menu item");
        new JPopupMenuOperator().pushMenuNoBlock(_menuVirtualForm, _separator);
        try { Thread.sleep(1000); } catch(Exception e) {}
        
        log("**locate Create Virtual Form dialog");
        NbDialogOperator vf = new NbDialogOperator(_virtualFormDialog);

        log("**Add New Virtual Form");
        JButtonOperator newButton = new JButtonOperator(vf, _new);
        newButton.clickMouse();
        try { Thread.sleep(1000); } catch(Exception e) {}
         
        log("**set participate = Yes");
        JTableOperator vfTable = new JTableOperator(vf);
        vfTable.clickOnCell(1, 2, 2);
        JComboBoxOperator yn = new JComboBoxOperator(vf);
        yn.selectItem(_yes);
        
        log("**Close dialog");
        JButtonOperator okButton = new JButtonOperator(vf, _ok);
        okButton.clickMouse();
        
        log("**Right click button1");
        designer.clickForPopup(_x+10, _button2y+10);
        try { Thread.sleep(3000); } catch(Exception e) {}
        
        log("**Select " + _menuVirtualForm + " menu item");
        new JPopupMenuOperator().pushMenuNoBlock(_menuVirtualForm, _separator);
        try { Thread.sleep(3000); } catch(Exception e) {}

        log("**locate Create Virtual Form dialog");
        vf = new NbDialogOperator(_virtualFormDialog);

        log("**set submit = Yes");
        vfTable = new JTableOperator(vf);
        vfTable.clickOnCell(1, 3, 2);
        yn = new JComboBoxOperator(vf);
        yn.selectItem(_yes);
        
        log("**Close dialog");
        okButton = new JButtonOperator(vf, _ok);
        okButton.clickMouse();
        
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
        deploymentWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 480000);
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

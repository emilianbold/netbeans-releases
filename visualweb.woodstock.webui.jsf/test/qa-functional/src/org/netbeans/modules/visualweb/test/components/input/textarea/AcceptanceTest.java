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

package org.netbeans.modules.visualweb.test.components.input.textarea;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.EditorOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

import java.io.File;
import java.io.IOException;
import java.awt.Point;

/**
 * @author Lark Fitzgerald (lark.fitzgerald@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _privateBundle ="org.netbeans.modules.visualweb.test.components.input.textarea.Acceptance";
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
    public String _basicMessage = Bundle.getStringTrimmed(_sharedBundle,"basicMessage");
    public String _basicMessageGroup = Bundle.getStringTrimmed(_sharedBundle,"basicMessageGroup");
    
    //components
    public String _label1 = Bundle.getStringTrimmed(_sharedBundle,"label1");
    public String _button1 = Bundle.getStringTrimmed(_sharedBundle,"button1");
    public String _textarea1 = Bundle.getStringTrimmed(_sharedBundle,"textarea1");
    public String _textArea1 = Bundle.getStringTrimmed(_sharedBundle,"textArea1");
    public String _textarea2 = Bundle.getStringTrimmed(_sharedBundle,"textarea2");
    public String _textarea3 = Bundle.getStringTrimmed(_sharedBundle,"textarea3");
    public String _textarea4 = Bundle.getStringTrimmed(_sharedBundle,"textarea4");
    public String _textarea5 = Bundle.getStringTrimmed(_sharedBundle,"textarea5");
    public String _textarea6 = Bundle.getStringTrimmed(_sharedBundle,"textarea6");
    public String _messagegroup1 = Bundle.getStringTrimmed(_sharedBundle,"messagegroup1");
    public String _message1 = Bundle.getStringTrimmed(_sharedBundle,"message1");
    
    //drop points
    public int _x = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"x"));
    public int _button1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button1y"));
    public int _label1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"label1y"));
    public int _textarea1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textarea1y"));
    public int _message1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message1y"));
    public int _textarea2y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textarea2y"));
    public int _textarea3y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textarea3y"));
    public int _textarea4y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textarea4y"));
    public int _textarea5y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textarea5y"));
    public int _textarea6y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textarea6y"));
    public int _messagegroup1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"messagegroup1y"));
    
    //property items
    public String _propertyText = Bundle.getStringTrimmed(_sharedBundle,"propertyText");
    public String _propertyLabel = Bundle.getStringTrimmed(_sharedBundle,"propertyLabel");
    public String _propertyColumns = Bundle.getStringTrimmed(_sharedBundle,"propertyColumns");
    public String _propertyRows = Bundle.getStringTrimmed(_sharedBundle,"propertyRows");
    public String _propertyDisabled = Bundle.getStringTrimmed(_sharedBundle,"propertyDisabled");
    public String _propertyRequired = Bundle.getStringTrimmed(_sharedBundle,"propertyRequired");
    public String _propertyVisible = Bundle.getStringTrimmed(_sharedBundle,"propertyVisible");
    public String _propertyFor = Bundle.getStringTrimmed(_sharedBundle,"propertyFor");
    
    //values
    public String _labelValue1 = Bundle.getStringTrimmed(_privateBundle,"labelValue1");
    public String _labelValue2 = Bundle.getStringTrimmed(_privateBundle,"labelValue2");
    public String _buttonValue1 = Bundle.getStringTrimmed(_privateBundle,"buttonValue1");
    public String _textValue1 = Bundle.getStringTrimmed(_privateBundle,"textValue1");
    public String _textValue2 = Bundle.getStringTrimmed(_privateBundle,"textValue2");
    public String _textValue3 = Bundle.getStringTrimmed(_privateBundle,"textValue3");
    public String _textValue4 = Bundle.getStringTrimmed(_privateBundle,"textValue4");
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
    public String _false = Bundle.getStringTrimmed(_sharedBundle,"false");
    
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    
    public static Test suite() {
        TestSuite suite= new NbTestSuite();
        
        //Create the project
        suite.addTest(new AcceptanceTest("createWebProject"));
        
        //Design the page
        suite.addTest(new AcceptanceTest("testAddButton"));
        suite.addTest(new AcceptanceTest("testTextArea1"));
        suite.addTest(new AcceptanceTest("testTextArea2"));
        suite.addTest(new AcceptanceTest("testTextArea3"));
        suite.addTest(new AcceptanceTest("testTextArea4"));
        suite.addTest(new AcceptanceTest("testTextArea5"));
        suite.addTest(new AcceptanceTest("testTextArea6"));
        suite.addTest(new AcceptanceTest("testAddMessageGroup"));
        
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
     *    Add a button
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
     *    add textArea1
     *    set the label value to 'I am a text area label'
     *    set the text area required = true.
     *    add a message list set for textArea1
     */
    public void testTextArea1() {
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
        
        //For some reason, the property sheet doesn't rise to top
        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu("Window|Properties","|");
        
        log("**Set label value via property sheet");
        sheet = new SheetTableOperator();
        row = sheet.findCellRow(_propertyLabel);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue1);
        
        log("**Set required option to true");
        row = sheet.findCellRow(_propertyRequired);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyRequired, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Drag Message onto designer");
//	palette.select();
        clickPoint = palette.getClickPoint(_basicMessage);
        dropPoint = new Point(_x, _message1y);
        palette.dndPaletteComponent(_basicMessage, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set message for property via property sheet");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textArea1);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    add textArea2
     *    set the text value to 'columns = 20'
     *    set the columns value = 20.
     */
    public void testTextArea2() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add textArea to designer");
        clickPoint = palette.getClickPoint(_basicTextArea);
        dropPoint = new Point(_x, _textarea2y);
        palette.dndPaletteComponent(_basicTextArea, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set the " + _propertyText + " to " + _textValue3);
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyText);
        new JTextComponentOperator(sheet).enterText(_textValue3);
        
        log("**Set the columns to " + _columns);
        row = sheet.findCellRow(_propertyColumns);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_columns);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    add textArea3
     *    set the text value to 'rows = 5
     *    set the rows = 5
     */
    public void testTextArea3() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add textArea to designer");
        clickPoint = palette.getClickPoint(_basicTextArea);
        dropPoint = new Point(_x, _textarea3y);
        palette.dndPaletteComponent(_basicTextArea, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set the text");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyText);
        new JTextComponentOperator(sheet).enterText(_textValue4);
        
        log("**Set the rows to " + _rows);
        row = sheet.findCellRow(_propertyRows);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_rows);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    add textArea4
     *    set the text value to 'disabled'
     *    set disabled = true
     */
    public void testTextArea4() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add textArea to designer");
        clickPoint = palette.getClickPoint(_basicTextArea);
        dropPoint = new Point(_x, _textarea4y);
        palette.dndPaletteComponent(_basicTextArea, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set text value via property sheet");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyText);
        new JTextComponentOperator(sheet).enterText(_textValue1);
        
        log("**Set disabled option to true");
        row = sheet.findCellRow(_propertyDisabled);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyDisabled, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    add a label
     *    set its value to 'visible = false'.
     *    add textArea5
     *    set visible = false
     */
    public void testTextArea5() {
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
        
        log("**Set Label text property");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyText);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue2);
        
        log("**Add textArea to designer");
        clickPoint = palette.getClickPoint(_basicTextArea);
        dropPoint = new Point(_x, _textarea5y);
        palette.dndPaletteComponent(_basicTextArea, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set visible option to true");
        row = sheet.findCellRow(_propertyVisible);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyVisible, _false);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    add textArea6,
     *    set its text value to a couple of paragraphs of text
     */
    public void testTextArea6() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add textArea to designer");
        clickPoint = palette.getClickPoint(_basicTextArea);
        dropPoint = new Point(_x, _textarea6y);
        palette.dndPaletteComponent(_basicTextArea, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set text property");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyText);
        new JTextComponentOperator(sheet).enterText(_textValue2);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *    Add a messageGroup to catch any other errors.
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

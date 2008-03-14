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

package org.netbeans.modules.visualweb.test.components.input.calendar;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.EditorOperator;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;

import java.io.File;
import java.io.IOException;

import java.awt.Point;

/**
 * @author Lark Fitzgerald (lark.fitzgerald@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _privateBundle ="org.netbeans.modules.visualweb.test.components.input.calendar.Acceptance";
    public String _projectName = Bundle.getStringTrimmed(_privateBundle,"projectName");
    public String _projectServer = Bundle.getStringTrimmed(_sharedBundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_sharedBundle,"logFile");
    public String _logFile = System.getProperty("xtest.workdir") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_sharedBundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_sharedBundle,"close");
    public String _run = Bundle.getStringTrimmed(_sharedBundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_sharedBundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_sharedBundle,"true");

    //Bundle variables
    public String _basicPalette = Bundle.getStringTrimmed(_sharedBundle,"basicPalette");
    public String _basicButton = Bundle.getStringTrimmed(_sharedBundle,"basicButton");
    public String _basicLabel = Bundle.getStringTrimmed(_sharedBundle,"basicLabel");
    public String _basicMessage = Bundle.getStringTrimmed(_sharedBundle,"basicMessage");
    public String _basicCalendar = Bundle.getStringTrimmed(_sharedBundle,"basicCalendar");

    //drop points
    public int _x = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"x"));
    public int _button1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button1y"));
    public int _message1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message1y"));
    public int _label1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"label1y"));
    public int _calendar1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"calendar1y"));

    //values
    public String _buttonValue1 = Bundle.getStringTrimmed(_privateBundle,"buttonValue1");
    public String _labelValue1 = Bundle.getStringTrimmed(_privateBundle,"labelValue1");
    public String _beanPropertyType1 = Bundle.getStringTrimmed(_privateBundle,"beanPropertyType1");
    public String _beanPropertyName1 = Bundle.getStringTrimmed(_privateBundle,"beanPropertyName1");
    public String _beanPropertyName2 = Bundle.getStringTrimmed(_privateBundle,"beanPropertyName2");
    public String _beanPropertyName3 = Bundle.getStringTrimmed(_privateBundle,"beanPropertyName3");
    public String _calendar1 = Bundle.getStringTrimmed(_privateBundle,"calendar1");
    
    //property items
    public String _propertyFor = Bundle.getStringTrimmed(_sharedBundle,"propertyFor");
    public String _propertyText = Bundle.getStringTrimmed(_sharedBundle,"propertyText");
    public String _propertyRequired = Bundle.getStringTrimmed(_sharedBundle,"propertyRequired");
    public String _propertyDateFormatPattern = Bundle.getStringTrimmed(_sharedBundle,"propertyDateFormatPattern");
    public String _propertyMaxDate = Bundle.getStringTrimmed(_sharedBundle,"propertyMaxDate");
    public String _propertyMinDate = Bundle.getStringTrimmed(_sharedBundle,"propertyMinDate");
    public String _propertySelectedDate = Bundle.getStringTrimmed(_sharedBundle,"propertySelectedDate");
    
    //Outline variables
    public String _outlineForm1 = Bundle.getStringTrimmed(_sharedBundle,"outlineForm1");
    public String _sessionBean1 = Bundle.getStringTrimmed(_sharedBundle,"sessionBean1");
    
    public String _beanPatternPath = Bundle.getStringTrimmed(_sharedBundle,"beanPatternPath");
    public String _addPropertyPath = Bundle.getStringTrimmed(_sharedBundle,"addPropertyPath");

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
//        suite.addTest(new AcceptanceTest("configureServer"));
        suite.addTest(new AcceptanceTest("createWebProject"));
//        suite.addTest(new AcceptanceTest("testAddLabel"));
        suite.addTest(new AcceptanceTest("testAddMessage"));
        suite.addTest(new AcceptanceTest("testAddButton"));
        suite.addTest(new AcceptanceTest("testAddCalendar"));
        suite.addTest(new AcceptanceTest("testAddLinks"));
//        suite.addTest(new AcceptanceTest("testSetCalendarProperties"));
//        suite.addTest(new AcceptanceTest("testAddPageSource"));
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
     * Drag a Basic > button and label it Submit
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
     * Drag a basic > message onto designer
     */
    public void testAddMessage() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Drag Message onto designer");
        clickPoint = palette.getClickPoint(_basicMessage);
        dropPoint = new Point(_x, _message1y);
        palette.dndPaletteComponent(_basicMessage, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}        
        
        log("**Done.");
        endTest();
    }
    
    /*
     * Drag a basic > label onto designer and label it: Please select your birthdate.
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
        
        //For some reason, the property sheet doesn't rise to top
        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu("Window|Properties","|");
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set label value via property sheet");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyText);
//        row = sheet.findCellRow(_propertyLabel);
//        sheet.clickForEdit(row, 1);
//        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue1);
        
        log("**Done.");
        endTest();
    }
    
    /*
     * Drag a basic > calendar onto designer.
     */
    public void testAddCalendar() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Calendar to designer");
        clickPoint = palette.getClickPoint(_basicCalendar);
        dropPoint = new Point(_x, _label1y);
//drops in wrong position
//        palette.dndPaletteComponent(_basicCalendar, designer, dropPoint);
        palette.addComponent(_basicCalendar, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();        
    }
    
    /*
     * Link the message component to the calendar
     * It should update to say: Message summary for calendar 1
     *
     * Link the label component to the calendar
     */
    public void testAddLinks() {
        startTest();
/*        log("Select the label component");
        designer.clickMouse(_x, _label1y, 1);
        
        //For some reason, the property sheet doesn't rise to top
        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu("Window|Properties","|");
        
        log("**Set Label for property to calendar1");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _calendar1);
        try { Thread.sleep(1000); } catch(Exception e) {}
*/        
        log("Select the message component");
//        designer.clickMouse(_x, _message1y, 1);
//For some reason I can't select the component on the designer.
//probably the actual drop point is different than what I selected.
//So I'm using the outline window instead.
        outline = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);
        outline.verify();
        String msg = _outlineForm1 + "message1";
//        outline.clickOnPath(msg);
        outline.selectPath(msg);
        Util.wait(5000);

        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu("Window|Properties","|");
        try { Thread.sleep(2000); } catch(Exception e) {}
                
        log("**Set message for property via property sheet");
        sheet = new SheetTableOperator();        
        row = sheet.findCellRow(_propertyFor);
//        sheet.clickCell(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _calendar1);
        
        log("**Done.");
        endTest();
    }



//        suite.addTest(new AcceptanceTest("testSetCalendarProperties"));
//        suite.addTest(new AcceptanceTest("testAddPageSource"));

    
/*
Select Calendar on the designer and go to its properties.

Go to dateFormatPattern property and press the ...

Check-on the MM.dd.yyy format and press OK.

Go to the maxDate property and press the ...
Select the #{SessionBean1.maxDate} and press OK.

Go to the minDate property and press the ...
Select the #{SessionBean1.minDate} and press OK. 

Go to the Required property and set it to true
The label should show the * indicator

Go to the selectedDate property and press the ...
Select the #{SessionBean1.selectedDate} and press OK. 

Select the Java tab of the designer and add the following import statement:
import java.util.Date; 

Scroll down to the init() method and add the following code:
getSessionBean1().setMaxDate(new Date());
getSessionBean1().setMinDate(new Date(01, 0, 1));
getSessionBean1().setSelectedDate(new Date(69, 7, 13));

Ctrl-S to save

Go back to designer
 */
    
    /*
     * Deploy application
     */
    public void testDeploy() {
        startTest();
        Util.saveAllAPICall();
        TestUtils.wait(2000);
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

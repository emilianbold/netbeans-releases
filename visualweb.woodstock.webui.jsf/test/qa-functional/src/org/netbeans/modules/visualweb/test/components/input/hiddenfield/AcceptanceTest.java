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

package org.netbeans.modules.visualweb.test.components.input.hiddenfield;

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
import org.netbeans.jemmy.operators.JRadioButtonOperator;

import java.awt.Point;
import javax.swing.JRadioButton;

import java.io.File;
import java.io.IOException;

/**
 * @author Lark Fitzgerald (lark.fitzgerald@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _privateBundle ="org.netbeans.modules.visualweb.test.components.input.hiddenfield.Acceptance";
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
    public String _basicHiddenField = Bundle.getStringTrimmed(_sharedBundle,"basicHiddenField");
    public String _basicTextField = Bundle.getStringTrimmed(_sharedBundle,"basicTextField");
    public String _basicMessage = Bundle.getStringTrimmed(_sharedBundle,"basicMessage");
    public String _basicMessageGroup = Bundle.getStringTrimmed(_sharedBundle,"basicMessageGroup");
    
    //property variables
    public String _propertyFor = Bundle.getStringTrimmed(_sharedBundle,"propertyFor");
    public String _propertyText = Bundle.getStringTrimmed(_sharedBundle,"propertyText");
    public String _propertyRequired = Bundle.getStringTrimmed(_sharedBundle,"propertyRequired");
    public String _propertyDisabled = Bundle.getStringTrimmed(_sharedBundle,"propertyDisabled");
    public String _propertyRendered = Bundle.getStringTrimmed(_sharedBundle,"propertyRendered");
    public String _propertyConverter = Bundle.getStringTrimmed(_sharedBundle,"propertyConverter");
    public String _propertyImmediate = Bundle.getStringTrimmed(_sharedBundle,"propertyImmediate");
    
    public String _newDateTimeConverter = Bundle.getStringTrimmed(_sharedBundle,"newDateTimeConverter");
    
    //Menu variables
    public String _separator = Bundle.getStringTrimmed(_sharedBundle,"separator");
    public String _propertySheet = Bundle.getStringTrimmed(_sharedBundle,"propertySheet");
    
    //Binding Variables
    public String _bindDialog = Bundle.getStringTrimmed(_privateBundle,"bindDialog");
    public String _useBinding = Bundle.getStringTrimmed(_privateBundle,"useBinding");
    public String _bindToObject = Bundle.getStringTrimmed(_privateBundle,"bindToObject");
    public String _bindTextField1Path = Bundle.getStringTrimmed(_privateBundle,"bindTextField1Path");
    public String _selectBindingTarget = Bundle.getStringTrimmed(_privateBundle,"selectBindingTarget");
    
    //Component variables
    public String _hiddenField1 = Bundle.getStringTrimmed(_privateBundle,"hiddenField1");
    
    //coordinate variables
    public int _x = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"x"));
    public int _button1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button1y"));
    public int _textfield1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield1y"));
    public int _message1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message1y"));
    public int _messagegroup1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"messageGroup1y"));
    public int _hiddenfield1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"hiddenField1y"));
    public int _hiddenfield2y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"hiddenField2y"));
    public int _hiddenfield3y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"hiddenField3y"));
    public int _hiddenfield4y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"hiddenField4y"));
    public int _hiddenfield5y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"hiddenField5y"));
    public int _hiddenfield6y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"hiddenField6y"));
    
    //component values
    public String _hiddenValue1 = Bundle.getStringTrimmed(_privateBundle,"hiddenValue1");
    
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
    public String _page1 = Bundle.getStringTrimmed(_sharedBundle,"webPage1");
    public String _false = Bundle.getStringTrimmed(_sharedBundle,"false");
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        
        try {
            suite.addTest(new AcceptanceTest("testCreateWebProject"));
                        
            suite.addTest(new AcceptanceTest("testAddTextfield1"));
            suite.addTest(new AcceptanceTest("testAddButton1"));
            suite.addTest(new AcceptanceTest("testAddMessageGroup1"));
            suite.addTest(new AcceptanceTest("testAddHiddenField1"));
//            suite.addTest(new AcceptanceTest("testAddMessage1"));
//            suite.addTest(new AcceptanceTest("testAddHiddenField2"));
//            suite.addTest(new AcceptanceTest("testAddHiddenField3"));
//            suite.addTest(new AcceptanceTest("testAddHiddenField4"));
//DateTimeConverter missing from dropdown, need to DnD instead
//        suite.addTest(new AcceptanceTest("testAddHiddenField5"));
//            suite.addTest(new AcceptanceTest("testAddHiddenField6"));
//            suite.addTest(new AcceptanceTest("testAddButton1Action"));
            
//            suite.addTest(new AcceptanceTest("testDeploy"));
//            suite.addTest(new AcceptanceTest("testCloseWebProject"));
//        suite.addTest(new AcceptanceTest("testUndeploy"));
//        suite.addTest(new AcceptanceTest("testCheckIDELog"));
            
        } catch (Exception e) {
            
        }
        return suite;
    }
    
    /** method called before each testcase
     */
    protected void setUp() {
        System.out.println("########  "+getName()+"  #######");
/*
        //Project variables
        log(_sharedBundle);
        log(_privateBundle);
        log(_projectName);
        log(_projects);
        log(_projectType);
        log(_projectPath);
        log(_projectCategory);
        log(_logFileLocation);
        log(_logFile);
        log(_exception);
        log(_close);
 
        //Palette variables
        log(_basicPalette);
        log(_basicButton);
        log(_basicHiddenField);
        log(_basicTextField);
        log(_basicMessage);
        log(_basicMessageGroup);
 
        //property variables
        log(_propertyFor);
        log(_propertyText);
        log(_propertyRequired);
        log(_propertyDisabled);
        log(_propertyRendered);
        log(_propertyConverter);
        log(_propertyImmediate);
 
        log(_newDateTimeConverter);
 
        //Menu variables
 
        //Component variables
        log(_hiddenField1);
 
        //coordinate variables
        log("" + _x);
        log("" + _button1y);
        log("" + _textfield1y);
        log("" + _message1y);
        log("" + _messagegroup1y);
        log("" + _hiddenfield1y);
        log("" + _hiddenfield2y);
        log("" + _hiddenfield3y);
        log("" + _hiddenfield4y);
        log("" + _hiddenfield5y);
        log("" + _hiddenfield6y);
 
        //component values
        log(_hiddenValue1);
 
        log(_page1);
        log(_true);
        log(_false);
 */
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
    
    /*@Test
     * drag a textField onto designer
     */
    public void testAddTextfield1() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add textField1 to designer");
        clickPoint = palette.getClickPoint(_basicTextField);
        dropPoint = new Point(_x, _textfield1y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();
        
    }
    
    
    /*
     * drag a button onto designer
     */
    public void testAddButton1() {
        
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Button to designer");
        Point clickPoint = palette.getClickPoint(_basicButton);
        Point dropPoint = new Point(_x, _button1y);
        palette.dndPaletteComponent(_basicButton, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();
    }
    
    /*
     * add Message Group to designer
     */
    public void testAddMessageGroup1() {
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
     * add hiddenfield to designer
     *   1. bind its text value to #{Page1.textField1.text}
     *   2. add a length validator to it max=8, min=2
     *   3. right click in outline and add a validation event:
     *      info("hiddenField1: validate method called");
     *   4. right click in outline and add a processValue change event
     *      info("hiddenField1: processValueChange method called");
     */
    public void testAddHiddenField1() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add hiddenfield to designer");
        clickPoint = palette.getClickPoint(_basicHiddenField);
        dropPoint = new Point(_x, _hiddenfield1y);
        palette.dndPaletteComponent(_basicHiddenField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu(_propertySheet,_separator);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Start Bind text to #{Page1.textField1.text}");
        log("open dialog");
        sheet = new SheetTableOperator();
        sheet.pushDotted(_propertyText);
        
        log("locate dialog");
        NbDialogOperator bindDialog = new NbDialogOperator(_bindDialog);
        
        log("Click the bind to Object radio button");
        JRadioButtonOperator bindOption = new JRadioButtonOperator(bindDialog,_useBinding);
        bindOption.doClick();
        
        log("Select the " + _bindToObject + " tab");
        new JTabbedPaneOperator(bindDialog).selectPage(_bindToObject);
        TestUtils.wait(1000);
        
        try {
            log("get tree");
            JTreeOperator bindPath = new JTreeOperator(bindDialog);
            log("child count: " + bindPath.getChildCount(bindPath.getRoot()));
            log("get path " + _bindTextField1Path);
            javax.swing.tree.TreePath tp = bindPath.findPath(_bindTextField1Path, _separator);
            log("Select path " + _bindTextField1Path);
            bindPath.selectPath(tp);
        } catch (Exception e) {
            log("Exception occured.");
            log(e.toString());
            e.printStackTrace();
            fail();
        }
        
        log("select #{Page1.textField1.text}");
        
        log("Close dialog");
        bindDialog.btOK();
        try { Thread.sleep(2000); } catch(Exception e) {}
        
/*
        log("**Add length validator; max=8, min=2");
//select via outline window
        log("**Set validator property to '(new lengthValidator)'");
        row = sheet.findCellRow(_propertyValidator);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyValidator, _newValidatorLength);
 
        log("**Select lengthValidator1 from Outline window");
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);
        doo.verify();
        doo.clickOnPath(_lengthValidator1);
        doo.selectPath(_lengthValidator1);
        Util.wait(5000);
 
        log("**set its max value '6'");
        row = sheet.findCellRow(_propertyMaximum);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_six);
 
        log("**set its min value '2'");
        row = sheet.findCellRow(_propertyMinimum);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_two);
 */
        
        log("add validation event");
        
        log("add processValue change event");
        //use outline window
/*
        designer.clickMouse(25, 175, 1);
        try { Thread.sleep(2000); } catch(Exception e) {}  //wait or sub-menu won't be present.
        designer.clickForPopup(25, 175);
        new JPopupMenuOperator().pushMenuNoBlock("Edit Event Handler|processValueChange", "|");
        try { Thread.sleep(3000); } catch(Exception e) {}
 
        log("**Add code to textfield4_processValueChange");
        String code = "staticText1.setValue(textField4.getValue());\n";
        EditorOperator editor = new EditorOperator(Util.getMainWindow(), _page1);
        editor.requestFocus();
        editor.txtEditorPane().setText("staticText1.setValue(textField4.getValue());\n");
        try { Thread.sleep(2000); } catch(Exception e) {}
        designer.switchToDesignerPane();
 */
        
        log("**Done.");
        endTest();
    }
    
    /*
     *  drag a message component onto designer
     *  set its for property to hiddenField1
     */
    public void testAddMessage1() {
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
        
        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu("Window|Properties","|");
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set message for property via property sheet");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyFor);
        sheet.setComboBoxValue(_propertyFor, _hiddenField1);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *  drag hiddenField2 onto designer
     *  set its disabled property = true
     */
    public void testAddHiddenField2() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add hiddenfield to designer");
        clickPoint = palette.getClickPoint(_basicHiddenField);
        dropPoint = new Point(_x, _hiddenfield2y);
        palette.dndPaletteComponent(_basicHiddenField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set disabled option to true");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyDisabled);
        sheet.setCheckBoxValue(_propertyDisabled, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Done.");
        endTest();
    }
    
    /*
     *  drag hiddenField3 onto designer
     *  set its rendered property = false
     */
    public void testAddHiddenField3() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add hiddenfield to designer");
        clickPoint = palette.getClickPoint(_basicHiddenField);
        dropPoint = new Point(_x, _hiddenfield3y);
        palette.dndPaletteComponent(_basicHiddenField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**set textfield rendered property to false");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyRendered);
        sheet.setCheckBoxValue(_propertyRendered, _false);
        
        log("**Done.");
        endTest();
    }
    
    /*
     * drag hiddenField4 onto designer
     * and set its text to: Star light, star bright...
     */
    public void testAddHiddenField4() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add hiddenfield to designer");
        clickPoint = palette.getClickPoint(_basicHiddenField);
        dropPoint = new Point(_x, _hiddenfield4y);
        palette.dndPaletteComponent(_basicHiddenField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**set its value to 'I am disabled'");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyText);
        new JTextComponentOperator(sheet).enterText(_hiddenValue1);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     * drag hiddenField5 onto designer
     * set its converter to dateTimeConverter1
     */
    public void testAddHiddenField5() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add hiddenfield to designer");
        clickPoint = palette.getClickPoint(_basicHiddenField);
        dropPoint = new Point(_x, _hiddenfield5y);
        palette.dndPaletteComponent(_basicHiddenField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set converter property to '(new DateTimeConverter)'");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyConverter);
        sheet.setComboBoxValue(_propertyConverter, _newDateTimeConverter);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *  drag hiddenField6 onto designer
     *  and set its immediate flag = true
     */
    public void testAddHiddenField6() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add hiddenfield to designer");
        clickPoint = palette.getClickPoint(_basicHiddenField);
        dropPoint = new Point(_x, _hiddenfield6y);
        palette.dndPaletteComponent(_basicHiddenField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**set hiddenfield immediate property to true");
        sheet = new SheetTableOperator();
        sheet.clickCell(_propertyImmediate);
        sheet.setCheckBoxValue(_propertyImmediate, _true);
        
        log("**Done.");
        endTest();
    }
    
    /*
     * double click button and add following source:
     *  info("hiddenField1 has validator: " + hiddenField1.getValidator().getExpressionString());
     *  info("hiddenField2 is disabled: " + hiddenField2.isDisabled());
     *  info("hiddenField3 is rendered: " + hiddenField3.isRendered());
     *  info("hiddenField4 has text: " + hiddenField4.getValue());
     *  info("hiddenField5 has converter: " + hiddenField5.getConverter().toString());
     *  info("hiddenField6 is immediate: " + hiddenField6.isImmediate());
     */
    public void testAddButton1Action() {
        startTest();
        log("**double click button to get to action method()");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickMouse(_x, _button1y, 2);
        
        log("**add source code to button1_action()");
        String code1 = "info(\"hiddenField1 has validator: \" + hiddenField1.getValidator().getExpressionString());\n";
        String code2 = "info(\"hiddenField2 is disabled: \" + hiddenField2.isDisabled());\n";
        String code3 = "info(\"hiddenField3 is rendered: \" + hiddenField3.isRendered());\n";
        String code4 = "info(\"hiddenField4 has text: \" + hiddenField4.getValue());\n";
        String code5 = "info(\"hiddenField5 has converter: \" + hiddenField5.getConverter().toString());\n";
        String code6 = "info(\"hiddenField6 is immediate: \" + hiddenField6.isImmediate());\n";
//For some reason, doing all of them at once doesn't work
//        String code = code1 + code2 + code3 + code4 + code5 + code6;
        EditorOperator editor = new EditorOperator(Util.getMainWindow(), _page1);
        editor.txtEditorPane().setText(code1);
        editor.txtEditorPane().setText(code2);
        editor.txtEditorPane().setText(code3);
        editor.txtEditorPane().setText(code4);
        editor.txtEditorPane().setText(code5);
        editor.txtEditorPane().setText(code6);
        
        log("**Go back to designer");
        try { Thread.sleep(3000); } catch(Exception e) {}
        designer.switchToDesignerPane();
        
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
 
o) Expected Results:
 
   1. enter a value in the field a press the button. (Note: validation does not get called)
         1. System Messages shows
               1. processValueChange
               2. hiddenField1 has validator: #{Page1.hiddenField1_validate}
               3. hiddenField2 is disabled: true
               4. hiddenField3 is rendered: false
               5. hiddenfield4 has text: Start light, Star bright...
               6. hiddenfield5 has converter: javax.faces.convert.DateTimeConverter@bb9819
               7. hiddenfield6 is immediate: true
 
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

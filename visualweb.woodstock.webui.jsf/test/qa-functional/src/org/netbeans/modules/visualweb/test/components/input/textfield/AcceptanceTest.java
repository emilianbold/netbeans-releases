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

/*
 * AcceptanceTest.java
 *
 * Created on May 11, 2006, 9:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.test.components.input.textfield;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.jemmy.operators.*;

import java.io.File;
import java.io.IOException;
import java.awt.Point;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.EditorOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

import java.util.Hashtable;

import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;

/**
 *
 * @author <a href="mailto:lark.fitzgerald@sun.com">lfitzger</a>
 */
public class AcceptanceTest extends RaveTestCase {
    
    //Shared Variables
    public static DesignerPaneOperator designer;
    public static PaletteContainerOperator palette;
    public static DocumentOutlineOperator outline;
    public static SheetTableOperator sheet;
    public static DeploymentDialogOperator deploy;
    public static ServerNavigatorOperator explorer;
    Point clickPoint, dropPoint;
    
    //Project variables
    public String _sharedBundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _privateBundle ="org.netbeans.modules.visualweb.test.components.input.textfield.Acceptance";
    public String _projectName = Bundle.getStringTrimmed(_privateBundle,"projectName");
    public String _projectServer = Bundle.getStringTrimmed(_sharedBundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_sharedBundle,"logFile");
    public String _logFile = System.getProperty("xtest.workdir") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_sharedBundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_sharedBundle,"close");
    public String _run = Bundle.getStringTrimmed(_sharedBundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_sharedBundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_sharedBundle,"true");
    
    //Outline variables
    public String _outlinePage1 = Bundle.getStringTrimmed(_sharedBundle,"outlinePage1");

    //Palette variables
    public String _basicPalette = Bundle.getStringTrimmed(_sharedBundle,"basicPalette");
    public String _basicButton = Bundle.getStringTrimmed(_sharedBundle,"basicButton");
    public String _basicTextField = Bundle.getStringTrimmed(_sharedBundle,"basicTextField");
    public String _basicLabel = Bundle.getStringTrimmed(_sharedBundle,"basicLabel");
    public String _basicStaticText = Bundle.getStringTrimmed(_sharedBundle,"basicStaticText");
    public String _basicMessage = Bundle.getStringTrimmed(_sharedBundle,"basicMessage");
    
    public String _validatorPalette = Bundle.getStringTrimmed(_sharedBundle,"validatorPalette");
    public String _validatorLength = Bundle.getStringTrimmed(_sharedBundle,"validatorLength");
    
    public String _converterPalette = Bundle.getStringTrimmed(_sharedBundle,"converterPalette"); // Bug with palette has this in lowercase
    public String _converterInteger = Bundle.getStringTrimmed(_sharedBundle,"converterInteger");
    
    //Editor variables
    public String _page1 = Bundle.getStringTrimmed(_sharedBundle,"webPage1");
    public String _reformatCode = Bundle.getStringTrimmed(_sharedBundle,"reformatCode");
    
    //Property variables
    public String _propertyText = Bundle.getStringTrimmed(_sharedBundle,"propertyText");
    public String _propertyLabel = Bundle.getStringTrimmed(_sharedBundle,"propertyLabel");
    public String _propertyMaxLength = Bundle.getStringTrimmed(_sharedBundle,"propertyMaxLength");
    public String _propertyFor = Bundle.getStringTrimmed(_sharedBundle,"propertyFor");
    public String _propertyRequired = Bundle.getStringTrimmed(_sharedBundle,"propertyRequired");
    public String _propertyRendered = Bundle.getStringTrimmed(_sharedBundle,"propertyRendered");
    public String _propertyValidator = Bundle.getStringTrimmed(_sharedBundle,"propertyValidator");
    public String _propertyConverter = Bundle.getStringTrimmed(_sharedBundle,"propertyConverter");
    public String _propertyMaximum = Bundle.getStringTrimmed(_sharedBundle,"propertyMaximum");
    public String _propertyMinimum = Bundle.getStringTrimmed(_sharedBundle,"propertyMinimum");
    public String _propertyDisabled = Bundle.getStringTrimmed(_sharedBundle,"propertyDisabled");
    public String _properties = Bundle.getStringTrimmed(_sharedBundle,"properties");
    public String _propertySheet = Bundle.getStringTrimmed(_sharedBundle,"propertySheet");
    
    //component variables
    public String _button1 = Bundle.getStringTrimmed(_sharedBundle,"button1");
    public String _label1 = Bundle.getStringTrimmed(_sharedBundle,"label1");
    public String _message1 = Bundle.getStringTrimmed(_sharedBundle,"message1");
    
    public String _textfield1 = Bundle.getStringTrimmed(_sharedBundle,"textfield1");
    public String _textfield2 = Bundle.getStringTrimmed(_sharedBundle,"textfield2");
    public String _textfield3 = Bundle.getStringTrimmed(_sharedBundle,"textfield3");
    public String _textfield4 = Bundle.getStringTrimmed(_sharedBundle,"textfield4");
    public String _textfield5 = Bundle.getStringTrimmed(_sharedBundle,"textfield5");
    public String _textfield6 = Bundle.getStringTrimmed(_sharedBundle,"textfield6");
    public String _textfield7 = Bundle.getStringTrimmed(_sharedBundle,"textfield7");
    public String _textfield8 = Bundle.getStringTrimmed(_sharedBundle,"textfield8");
    public String _textfield9 = Bundle.getStringTrimmed(_sharedBundle,"textfield9");
    public String _textField1 = Bundle.getStringTrimmed(_sharedBundle,"textField1");
    public String _textField3 = Bundle.getStringTrimmed(_sharedBundle,"textField3");
    public String _textField4 = Bundle.getStringTrimmed(_sharedBundle,"textField4");
    public String _textField5 = Bundle.getStringTrimmed(_sharedBundle,"textField5");
    public String _textField6 = Bundle.getStringTrimmed(_sharedBundle,"textField6");
    public String _textField7 = Bundle.getStringTrimmed(_sharedBundle,"textField7");
    public String _textField8 = Bundle.getStringTrimmed(_sharedBundle,"textField8");
    public String _textField9 = Bundle.getStringTrimmed(_sharedBundle,"textField9");
    
    public String _textValue0 = Bundle.getStringTrimmed(_privateBundle,"textValue0");
    public String _textValue1 = Bundle.getStringTrimmed(_privateBundle,"textValue1");
    public String _textValue2 = Bundle.getStringTrimmed(_privateBundle,"textValue2");
    public String _textValue3 = Bundle.getStringTrimmed(_privateBundle,"textValue3");
    public String _textValue4 = Bundle.getStringTrimmed(_privateBundle,"textValue4");
    
    public String _labelValue1 = Bundle.getStringTrimmed(_privateBundle,"labelValue1");
    public String _labelValue2 = Bundle.getStringTrimmed(_privateBundle,"labelValue2");
    public String _labelValue3 = Bundle.getStringTrimmed(_privateBundle,"labelValue3");
    public String _labelValue4 = Bundle.getStringTrimmed(_privateBundle,"labelValue4");
    public String _labelValue5 = Bundle.getStringTrimmed(_privateBundle,"labelValue5");
    public String _labelValue6 = Bundle.getStringTrimmed(_privateBundle,"labelValue6");
    
    public String _newValidatorLength = Bundle.getStringTrimmed(_sharedBundle,"newValidatorLength");
    public String _newIntegerConverter = Bundle.getStringTrimmed(_sharedBundle,"newIntegerConverter");
    public String _lengthValidator1 = Bundle.getStringTrimmed(_privateBundle,"lengthValidator1");
    public String _integerConverter1 = Bundle.getStringTrimmed(_sharedBundle,"integerConverter1");

    //General
    public String _false = Bundle.getStringTrimmed(_sharedBundle,"false");
    public String _two = Bundle.getStringTrimmed(_sharedBundle,"two");
    public String _five = Bundle.getStringTrimmed(_sharedBundle,"five");
    public String _six = Bundle.getStringTrimmed(_sharedBundle,"six");
    public String _separator = Bundle.getStringTrimmed(_sharedBundle,"separator");
    public String _menuProcessValueChange = Bundle.getStringTrimmed(_sharedBundle,"menuProcessValueChange");

    //Position variables
    public int _x = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"x"));
    public int _button1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"button1y"));
    public int _textfield1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield1y"));
    public int _textfield2y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield2y"));
    public int _textfield3y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield3y"));
    public int _textfield4y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield4y"));
    public int _textfield5y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield5y"));
    public int _textfield6y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield6y"));
    public int _textfield7y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield7y"));
    public int _textfield8y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield8y"));
    public int _textfield9y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"textfield9y"));
    public int _message1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message1y"));
    public int _message2y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message2y"));
    public int _message3y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message3y"));
    public int _message4y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"message4y"));
    public int _label1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"label1y"));
    public int _label2y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"label2y"));
    public int _statictext1y = Integer.parseInt(Bundle.getStringTrimmed(_privateBundle,"statictext1y"));
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_sharedBundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_sharedBundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_sharedBundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_sharedBundle, "deploymentPathGlassfish");
    
    /** Creates a new instance of AcceptanceTest*/
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new AcceptanceTest("createWebProject"));
        
        //Make Page
        suite.addTest(new AcceptanceTest("testAddButton"));
        suite.addTest(new AcceptanceTest("testAddTextField1"));
        suite.addTest(new AcceptanceTest("testAddTextField2"));
        suite.addTest(new AcceptanceTest("testAddTextField3"));
        suite.addTest(new AcceptanceTest("testAddTextField4"));
        suite.addTest(new AcceptanceTest("testAddTextField5"));
        suite.addTest(new AcceptanceTest("testAddTextField6"));
        suite.addTest(new AcceptanceTest("testAddTextField7"));
        suite.addTest(new AcceptanceTest("testAddTextField8"));
        suite.addTest(new AcceptanceTest("testAddTextField9"));
        
        //Deploy
        suite.addTest(new AcceptanceTest("testDeploy"));
        suite.addTest(new AcceptanceTest("testCheckIDELog"));
        
        //Verify Runtime
//        suite.addTest(new AcceptanceTest("testRuntimeScenario1"));
        
        //Clean up
        suite.addTest(new AcceptanceTest("testCloseWebProject"));
        suite.addTest(new AcceptanceTest("testUndeploy"));
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
     *   Add button to designer
     *   set its value to Execute
     *
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
        keyboard.enterText(field, _textValue0);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *   add textField1,
     *   set the text value to 'I am a textField'.
     *
     */
    public void testAddTextField1() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add textField1 to designer");
        clickPoint = palette.getClickPoint(_basicTextField);
        dropPoint = new Point(_x, _textfield1y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
//        palette.addComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("make sure property sheet is visible");
        Util.getMainMenu().pushMenu(_propertySheet, _separator);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set Text value via property sheet");
        sheet = new SheetTableOperator();
        sheet.clickForEdit(sheet.findCell(_propertyText, 2).y, 1);
        sheet.clickForEdit(sheet.findCell(_propertyText, 2).y, 1);
        new JTextComponentOperator(sheet).enterText(_textValue1);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *   add textField2,
     *   set the label to 'I am a textField label'.
     *
     */
    public void testAddTextField2() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add TextField2 to Designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield2y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set TextField2 Label property");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyLabel);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_textValue2);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *   add textField3
     *   add a label for textfield3 and give it a name.
     *   mark textField3 required.
     *   add a message component linked to textField3
     */
    public void testAddTextField3() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Textfield3 to designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield3y); //leave room for label
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
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
        new JTextComponentOperator(sheet).enterText(_labelValue1);
        
        log("**Set Label for property to textField3");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textField3);
        try { Thread.sleep(1000); } catch(Exception e) {}
        
        log("**Select textfield");
        designer.clickMouse(_x, _textfield3y, 1);
        try { Thread.sleep(3000); } catch(Exception e) {}
        
        log("**Set required option to true");
        row = sheet.findCellRow(_propertyRequired);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyRequired, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Drag Message onto designer");
        clickPoint = palette.getClickPoint(_basicMessage);
        dropPoint = new Point(_x, _message1y);
        palette.dndPaletteComponent(_basicMessage, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
//        log("**Set message via shift-drag and drop onto textField3");

        log("**Set message for property via property sheet");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textField3);
        
        log("**Done.");
        endTest();
        
    }
    
    /*
     *   add a textField4
     *   add a staticText field
     *   add a processValueChange event with code:
     *     staticText1.setValue(textField4.getValue());
     */
    public void testAddTextField4() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        JToggleButtonOperator toggleButton = new JToggleButtonOperator(Util.getMainWindow(), "Java");
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Textfield4 to designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield4y); //leave room for label
        palette.addComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Add StaticText1 to designer");
        clickPoint = palette.getClickPoint(_basicStaticText);
        dropPoint = new Point(_x, _statictext1y); //leave room for label
        palette.addComponent(_basicStaticText, designer, dropPoint);
        
        log("**select textfield4"); //(25, 175)
//        designer.clickMouse(_x+5, _textfield4y+5, 1);
//        try { Thread.sleep(2000); } catch(Exception e) {}  //wait or sub-menu won't be present.
        log("**right click menu on textfield4"); //(25, 175)
        //designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickForPopup(_x+5, _textfield4y+5); 
//wont work either        designer.clickForPopup(_textField4);
//stopped working?!?        designer.clickForPopup(_x, _textfield4y);
        new JPopupMenuOperator().pushMenuNoBlock(_menuProcessValueChange, _separator);
        try { Thread.sleep(3000); } catch(Exception e) {}
 
        log("**Add code to textfield4_processValueChange");
        EditorOperator editor = new EditorOperator(Util.getMainWindow(), _page1);
        editor.requestFocus();
        editor.pushDownArrowKey();
        editor.txtEditorPane().typeText("log(\"Action Performed\");\n");
        try { Thread.sleep(2000); } catch(Exception e) {}
        designer.switchToDesignerPane();

        log("**Done.");
        endTest();
    }
    
    
    /*
     *   add a textField5
     *   set its label text to disabled
     *   set its value to 'I am disabled'
     *   mark it disabled
     *   add a message component linked to textField5
     */
    public void testAddTextField5() {
        startTest();
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Textfield5 to designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield5y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        
        log("**set its label to 'disabled'");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyLabel);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue2);
        
        log("**set its value to 'I am disabled'");
        sheet.clickForEdit(sheet.findCell(_propertyText, 2).y, 1);
        sheet.clickForEdit(sheet.findCell(_propertyText, 2).y, 1);
        new JTextComponentOperator(sheet).enterText(_textValue3);
        
        log("**mark it disabled");
        row = sheet.findCellRow(_propertyDisabled);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyDisabled, _true);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Drag Message onto designer");
        clickPoint = palette.getClickPoint(_basicMessage);
        dropPoint = new Point(_x, _message2y);
        palette.dndPaletteComponent(_basicMessage, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set message for property via property sheet");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textfield5);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *   add textField6,
     *   set its label to max = 5.
     *   change its maxLength property to 5
     */
    public void testAddTextField6() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Textfield6 to designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield6y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**set its label to 'max = 5'");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyLabel);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue3);
        
        log("**set its value to 5");
        row = sheet.findCellRow(_propertyMaxLength);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_five);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *   add in a label with text Not rendered
     *   add textField7
     *   set rendered=false (though visible is more frequently used)
     */
    public void testAddTextField7() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add label to designer");
        Point clickPoint = palette.getClickPoint(_basicLabel);
        Point dropPoint = new Point(_x, _label2y);
        palette.dndPaletteComponent(_basicLabel, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
// Doesnt seem to work the same as button so used property sheet instead
//        log("**Set label value inline");
        
        log("**Set Label text property");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyText);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue4);
        
        log("**Add Textfield7 to designer");
        clickPoint = palette.getClickPoint(_basicTextField);
        dropPoint = new Point(_x, _textfield7y); //leave room for label
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
//        designer.clickMouse(_x, textfield7y, 1);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**set textfield rendered property to false");
        row = sheet.findCellRow(_propertyRendered);
        sheet.clickForEdit(row, 1);
        sheet.setCheckBoxValue(_propertyRendered, _false);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *   add a length validator to designer
     *   set the max=6
     *   set the min=2
     *   add textField8,
     *   set label to 'length validator,
     *   set validator = lengthValidator1
     *   Add a message component linked to textField8
     */
    public void testAddTextField8() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        PaletteContainerOperator validatorPalette = new PaletteContainerOperator(_validatorPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
        log("**Add Textfield8 to designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield8y); //leave room for label
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**set its label to 'length validator'");
        int row = sheet.findCellRow(_propertyLabel);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue5);
        
        log("**Set validator property to '(new lengthValidator)'");
        row = sheet.findCellRow(_propertyValidator);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyValidator, _newValidatorLength);
        
        log("**Select lengthValidator1 from Outline window");
        String path = _outlinePage1 + _lengthValidator1;
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);
        doo.verify();
//        doo.clickOnPath(path);
        doo.selectPath(path);
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
        
        log("**Drag Message onto designer");
        clickPoint = palette.getClickPoint(_basicMessage);
        dropPoint = new Point(_x, _message3y);
        palette.dndPaletteComponent(_basicMessage, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set message for property via property sheet");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textfield8);
        
        log("**Done.");
        endTest();
    }
    
    /*
     *   add a integer converter to the designer
     *   add textField9,
     *   set label to 'integer converter',
     *   set converter = integerConverter1
     *   Add a message component linked to textField9
     */
    public void testAddTextField9() {
        startTest();
        
        log("**Initialize");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator palette = new PaletteContainerOperator(_basicPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
        
//Drag & drop converter from palette fails, used properties new converter instead
/*
        PaletteContainerOperator converterPalette = new PaletteContainerOperator(_converterPalette);
        try { Thread.sleep(5000); } catch(Exception e) {}
 
        log("**Add Integer converter to designer");
        Point clickPoint = converterPalette.getClickPoint(_converterInteger);
        log(_converterInteger + " can be found at" + clickPoint.toString());
        Point dropPoint = new Point(10, 20); //empty space
        palette.clickMouse(clickPoint.x, clickPoint.y, 1);
        designer.clickMouse(dropPoint.x, dropPoint.y, 1);
//        palette.addComponent(_converterInteger, designer, dropPoint);
//        palette.dndPaletteComponent(_converterInteger, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
 */
        
        log("**Add Textfield9 to designer");
        Point clickPoint = palette.getClickPoint(_basicTextField);
        Point dropPoint = new Point(_x, _textfield9y);
        palette.dndPaletteComponent(_basicTextField, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**set its label to 'integer converter'");
        sheet = new SheetTableOperator();
        int row = sheet.findCellRow(_propertyLabel);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(_labelValue6);
        
        log("**Set converter property to '(new IntegerConverter)'");
        row = sheet.findCellRow(_newIntegerConverter);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyConverter, _newIntegerConverter);
        
        log("**Drag Message onto designer");
        clickPoint = palette.getClickPoint(_basicMessage);
        dropPoint = new Point(_x, _message4y);
        palette.dndPaletteComponent(_basicMessage, designer, dropPoint);
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        log("**Set message for property via property sheet");
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
//combobox is no longer present when you get more than 8 textFields so this line fails
//        sheet.setComboBoxValue(_propertyFor, _textField9);
        sheet.setTextValue(_propertyFor, _textField9);
        
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
     *    Runtime testing
     *
     */
    public void runtimeScenario1() {
        startTest();
        endTest();
/*
 
        log("**Verify insync changes jsp");
        designer.switchToJSPSource();
        //This string should present in JSP editor
        String verStr = _propertyText + "=\"" + _textValue0 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
 
        log("**Done. Return to designer");
        designer.switchToDesignerPane();
 
        log("**Verify insync changes jsp");
        designer.switchToJSPSource();
        //This string should present in JSP editor
        String verStr = _propertyText + "=\"" + _textValue1 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
 
        log("**Done. Return to designer");
        designer.switchToDesignerPane();
 
 
        log("**Verify insync made changes to jsp");
        designer.switchToJSPSource();
        //This string should present in JSP editor
        String verStr = _propertyLabel + "=\"" + _textValue2 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
 
        log("**Done. Return to designer");
        designer.switchToDesignerPane();
 
        log("**Set message for property via property sheet");
//        sheet = new SheetTableOperator();
        row = sheet.findCellRow(_propertyFor);
        sheet.clickForEdit(row, 1);
        sheet.setComboBoxValue(_propertyFor, _textfield3);
 
        log("**Verify insync changes to jsp");
        designer.switchToJSPSource();
        //This string should present in JSP editor
        String verStr = _propertyText + "=\"" + _labelValue1 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
        log("Found: " + verStr);
 
        verStr = _propertyFor + "=\"" + _textField3 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
        log("Found: " + verStr);
 
        verStr = _propertyRequired + "=\"" + _true +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
        log("Found: " + verStr);
 
        verStr = _propertyFor + "=\"" + _textField3 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
        log("Found: " + verStr);
 
        log("**Return to designer");
        designer.switchToDesignerPane();
 
         log("**Verify insync changes to jsp");
        designer.switchToJSPSource();
        //This string should present in JSP editor
        String verStr = _propertyLabel + "=\"" + _labelValue2 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
        log("Found: " + verStr);
 
        verStr = _propertyDisabled + "=\"" + _true +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
        log("Found: " + verStr);
 
        verStr = _propertyText + "=\"" + _textValue3 +"\"";
        Util.saveAllAPICall();
        assertFalse("There is no \"verStr\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(_page1).getText().indexOf(verStr)==-1);
        log("Found: " + verStr);
 
        log("**Return to designer");
        designer.switchToDesignerPane();
 
 */
    }
    
    //should echo values at runtime
    //submit should occur error free even though disabled. text should appear, even if disable, user should not be able to change value.
    //user should not be able to type in more than 5 characters.
    //should not render in designer, if view source it should not be in html.
    //values less than 2 and greater than 6 should give validation errors.
    
    /*
     * Undeploy Web Project
     *
     */
    public void undeployWebProject() {
        startTest();
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

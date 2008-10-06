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


package org.netbeans.test.uml.editcontrol;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.DefaultCharBindingMap;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyEventDriver;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.text.AWTTextKeyboardDriver;
import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;
import org.netbeans.jemmy.drivers.text.TextKeyboardDriver;
import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.Dumper;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.util.PopupConstants;



/**
 *
 * @author psb
 * @spec uml/UML-EditControl.xml
 */
public class TestToolTips extends UMLTestCase {
    
    private static String prName= "EditControlToolTips";
    private static String project = prName+"|Model";
    private static String defaultNewElementName=org.netbeans.test.uml.editcontrol.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.editcontrol.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.editcontrol.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.editcontrol.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.editcontrol.utils.Utils.defaultOperationVisibility;
    //
    private String lastTestCase=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private ProjectsTabOperator pto=null;
    private static boolean codeSync=false;
    //--
    private static String classDiagramName1 = "clD1";
    private static String  className1 ="class1";
    private static String workPkg1 = "pkg1";
    private static String attributeName1 = "attribute1";
    private static String attributeType1 = "double";
    private static String attributeDefValue1 = "2.3";
    private static String attributeVisibility1 = defaultAttributeVisibility;
    private static String tooltip1="<html>visibility type<b> name</b>[ranges] = initialValue {properties, ...}</html>"; 
    //--
    private static String classDiagramName2 = "clD2";
    private static String  className2 ="class2";
    private static String workPkg2 = "pkg2";
    private static String attributeName2 = "attribute2";
    private static String attributeType2 = "double";
    private static String attributeDefValue2 = "2.3";
    private static String attributeVisibility2 = defaultAttributeVisibility;
    private static String atStr2=attributeVisibility2+" "+attributeType2+" "+attributeName2+" = "+attributeDefValue2;
    private static String tooltip2="<html><b>visibility</b> type name[multiplicity] = initialValue {properties, ...}</html>"; 
    //--
    private static String classDiagramName3 = "clD3";
    private static String  className3 ="class3";
    private static String workPkg3 = "pkg3";
    private static String attributeName3 = "attribute3";
    private static String attributeType3 = "double";
    private static String attributeDefValue3 = "2.3";
    private static String attributeVisibility3 = defaultAttributeVisibility;
    private static String atStr3=attributeVisibility3+" "+attributeType3+" "+attributeName3+" = "+attributeDefValue3;
    private static String tooltip3="<html>visibility<b> type</b> name[multiplicity] = initialValue {properties, ...}</html>"; 
    //--
    private static String classDiagramName4 = "clD4";
    private static String  className4 ="class4";
    private static String workPkg4 = "pkg4";
    private static String attributeName4 = "attribute4";
    private static String attributeType4 = "double";
    private static String attributeDefValue4 = "";
    private static String attributeVisibility4 = defaultAttributeVisibility;
    private static String attributeMultiplicities4="*";
    private static String atStr4=attributeVisibility4+" "+attributeType4+" "+attributeName4+"["+attributeMultiplicities4+"]";
    private static String tooltip4="<html>visibility type name[<b>ranges</b>] = initialValue {properties, ...}</html>"; 
//--
    private static String classDiagramName5 = "clD5";
    private static String  className5 ="class5";
    private static String workPkg5 = "pkg5";
    private static String attributeName5 = "attribute5";
    private static String attributeType5 = "double";
    private static String attributeDefValue5 = "2.3";
    private static String attributeVisibility5 = defaultAttributeVisibility;
    private static String atStr5=attributeVisibility5+" "+attributeType5+" "+attributeName5+" = "+attributeDefValue5;
    private static String tooltip5="<html>visibility type name[multiplicity]<b> = initialValue</b> {properties, ...}</html>"; 
    //--
    private static String classDiagramName6 = "clD6";
    private static String  className6 ="class6";
    private static String workPkg6 = "pkg6";
    private static String attributeName6 = "attribute6";
    private static String attributeType6 = "double";
    private static String attributeDefValue6 = "2.3";
    private static String attributeVisibility6 = defaultAttributeVisibility;
    private static String atStr6=attributeVisibility6+" "+attributeType6+" "+attributeName6+" = "+attributeDefValue6;
    private static String tooltip6="<html>visibility type name[multiplicity] = initialValue {<b>name</b>=value}</html>"; 
    //--
    private static String classDiagramName7 = "clD7";
    private static String  className7 ="class7";
    private static String workPkg7 = "pkg7";
    private static String attributeName7 = "attribute7";
    private static String attributeType7 = "double";
    private static String attributeDefValue7 = "2.3";
    private static String attributeVisibility7 = defaultAttributeVisibility;
    private static String atStr7=attributeVisibility7+" "+attributeType7+" "+attributeName7+" = "+attributeDefValue7;
    private static String tooltip7="<html>visibility type name[multiplicity] = initialValue {name<b>=value</b>}</html>"; 
    //--
    private static String classDiagramName8 = "clD8";
    private static String  className8 ="class8";
    private static String workPkg8 = "pkg8";
    private static String attributeName8 = "attribute8";
    private static String attributeType8 = "double";
    private static String attributeDefValue8 = "2.3";
    private static String attributeVisibility8 = defaultAttributeVisibility;
    private static String atStr8=attributeVisibility8+" "+attributeType8+" "+attributeName8+" = "+attributeDefValue8;
    private static String tooltip8="<html>visibility type name[multiplicity]<b> = initialValue</b> {properties, ...}</html>"; 
    //--
    private static String classDiagramName9 = "clD9";
    private static String  className9 ="class9";
    private static String workPkg9 = "pkg9";
    private static String operationName9 = "operation9";
    private static String operationRetType9 = "int";
    private static String parameterType9 = "double";
    private static String parameterName9 = "s";
    private static String operationVisibility9 = defaultOperationVisibility;
    private static String opStr9=operationVisibility9+" "+operationRetType9+"  "+operationName9+"( "+parameterType9+" "+parameterName9+" )";
    private static String tooltip9="<html>visibility returnType <b> name</b>( parameter ) {properties, ...}</html>"; 
    //--
    private static String classDiagramName10 = "clD10";
    private static String  className10 ="class10";
    private static String workPkg10 = "pkg10";
    private static String operationName10 = "operation10";
    private static String operationRetType10 = "int";
    private static String parameterType10 = "double";
    private static String parameterName10 = "s";
    private static String operationVisibility10 = defaultOperationVisibility;
    private static String opStr10=operationVisibility10+" "+operationRetType10+"  "+operationName10+"( "+parameterType10+" "+parameterName10+" )";
    private static String tooltip10="<html><b>visibility</b> returnType  name( parameter, ... ) {properties, ...}</html>"; 
    //--
    private static String classDiagramName11 = "clD11";
    private static String  className11 ="class11";
    private static String workPkg11 = "pkg11";
    private static String operationName11 = "operation11";
    private static String operationRetType11 = "int";
    private static String parameterType11 = "double";
    private static String parameterName11 = "s";
    private static String operationVisibility11 = defaultOperationVisibility;
    private static String opStr11=operationVisibility11+" "+operationRetType11+"  "+operationName11+"( "+parameterType11+" "+parameterName11+" )";
    private static String tooltip11="<html>visibility returnType <b> name</b>( parameter, ... ) {properties, ...}</html>"; 
    //--
    private static String classDiagramName12 = "clD12";
    private static String  className12 ="class12";
    private static String workPkg12 = "pkg12";
    private static String operationName12 = "operation12";
    private static String operationRetType12 = "int";
    private static String parameterType12 = "double";
    private static String parameterName12 = "s";
    private static String operationVisibility12 = defaultOperationVisibility;
    private static String opStr12=operationVisibility12+" "+operationRetType12+"  "+operationName12+"( "+parameterType12+" "+parameterName12+" )";
    private static String tooltip12="<html>visibility returnType  name( <b>type</b> name[multiplicity] = defaultValue ) {properties, ...}</html>"; 
    //--
    private static String classDiagramName13 = "clD13";
    private static String  className13 ="class13";
    private static String workPkg13 = "pkg13";
    private static String operationName13 = "operation13";
    private static String operationRetType13 = "int";
    private static String parameterType13 = "double";
    private static String parameterName13 = "s";
    private static String operationVisibility13 = defaultOperationVisibility;
    private static String opStr13=operationVisibility13+" "+operationRetType13+"  "+operationName13+"( "+parameterType13+" "+parameterName13+" )";
    private static String tooltip13="<html>visibility returnType  name( type<b> name</b>[ranges] = defaultValue ) {properties, ...}</html>"; 
    //--
    private static String classDiagramName14 = "clD14";
    private static String  className14 ="class14";
    private static String workPkg14 = "pkg14";
    private static String operationName14 = "operation14";
    private static String operationRetType14 = "int";
    private static String parameterType14 = "double";
    private static String parameterName14 = "s";
    private static String operationVisibility14 = defaultOperationVisibility;
    private static String opStr14=operationVisibility14+" "+operationRetType14+"  "+operationName14+"( "+parameterType14+" "+parameterName14+" )";
    private static String tooltip14="<html>visibility <b>type</b>[multiplicity]  name( parameter, ... ) {properties, ...}</html>"; 
    //--
    private static String classDiagramName15 = "clD15";
    private static String  className15_1 ="class15_1";
    private static String  className15_2 ="class15_2";
    private static String workPkg15 = "pkg15";
    private static String operationName15 = "operation15";
    private static String operationRetType15 = "int";
    private static String parameterType15 = "double";
    private static String parameterName15 = "s";
    private static String operationVisibility15 = defaultOperationVisibility;
    private static String opStr15=operationVisibility15+" "+operationRetType15+"  "+operationName15+"( "+parameterType15+" "+parameterName15+" )";
    private static String tooltip15="<html>visibility returnType <b> name</b>( parameter ) {properties, ...}</html>"; 

    
    
    /** Need to be defined because of JUnit */
    public TestToolTips(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.editcontrol.TestToolTips.class);
        return suite;
    }
    
   private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        return createOrOpenDiagram(null,workPkg,"Class Diagram", diagram);
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagramType,String diagram) {
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createOrOpenDiagram(prName,workPkg,diagram,diagramType);
        pto = rt.pto;
        return rt.dOp;
    }    
    /**
     * @caseblock Tooltips
     * @usecase Tooltip appears for attribute edition
     */
    public void testAttributeTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg1, classDiagramName1);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className1,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility1,attributeType1,attributeName1,attributeDefValue1);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className1);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip1,tooltip1.equals(new JMenuItemOperator(tt).getText()));
         int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
   }
    /**
     * @caseblock Tooltips
     * @usecase Tooltip visibility highlighted when focus in edit control on visibility (for attribute).
     */
    public void testAttributeVisibilityTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg2, classDiagramName2);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className2,ElementTypes.CLASS);
       new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility2,attributeType2,attributeName2,attributeDefValue2);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className2);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.getTextFieldOperator().setCaretPosition(1);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip2,tooltip2.equals(new JMenuItemOperator(tt).getText()));
        int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
    }
    /**
     * @caseblock Tooltips
     * @usecase Tooltip type highlighted when focus in edit control on type (for attribute).
     */
    public void testAttributeTypeTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg3, classDiagramName3);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className3,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility3,attributeType3,attributeName3,attributeDefValue3);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className3);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_LEFT);
        ec.pushKey(KeyEvent.VK_LEFT);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip3,tooltip3.equals(new JMenuItemOperator(tt).getText()));
        int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
    }
    /**
     * @caseblock Tooltips
     * @usecase  Tooltip ranges highlighted when focus in edit control on multiplicity (for attribute).
     */
    public void testAttributeMultiplicityTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg4, classDiagramName4);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className4,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility4,attributeType4,attributeName4+"["+attributeMultiplicities4+"]",attributeDefValue4);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className4);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_RIGHT);
        ec.pushKey(KeyEvent.VK_RIGHT);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip4,tooltip4.equals(new JMenuItemOperator(tt).getText()));
        int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
    }
    /**
     * @caseblock Tooltips
     * @usecase Tooltip initial value highlighted when focus in edit control on initial value (for attribute).
     */
    public void testAttributeInitialValueTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg5, classDiagramName5);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className5,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility5,attributeType5,attributeName5,attributeDefValue5);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className5);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_EQUALS);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip5,tooltip5.equals(new JMenuItemOperator(tt).getText()));
        int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
    }
 
    /**
     * @caseblock Tooltips
     * @usecase Tooltip property name highlighted when focus in edit control on property name (for attribute).
     */
    public void testAttributePropertyNameTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg6, classDiagramName6);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className6,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility6,attributeType6,attributeName6,attributeDefValue6);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //add property
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(1,InputEvent.BUTTON1_MASK);
        PropertySheetOperator psh=PropertySheetOperator.invoke();
        Property tvp=new Property(psh,"Tagged Values");
        tvp.openEditor();
        JDialogOperator tvDlg=new JDialogOperator(attributeName6+" - Tagged Values");
        new JButtonOperator(tvDlg,"Add").push();
        JTableOperator tbl=new JTableOperator(tvDlg);
        tbl.selectCell(0,0);
        new EventTool().waitNoEvent(500);
        tbl.typeKey('a');new EventTool().waitNoEvent(500);
        tbl.typeKey('a');new EventTool().waitNoEvent(500);
        tbl.selectCell(0,2);new EventTool().waitNoEvent(500);
        tbl.typeKey('1');new EventTool().waitNoEvent(500);
        tbl.typeKey('1');new EventTool().waitNoEvent(500);
        try{Thread.sleep(5000);}catch(Exception ex){}
        new JButtonOperator(tvDlg,"OK").push();  
        tvDlg.waitClosed();
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className6);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_RIGHT);
        ec.pushKey(KeyEvent.VK_END);
        ec.pushKey(KeyEvent.VK_LEFT);
        ec.pushKey(KeyEvent.VK_LEFT);
        ec.pushKey(KeyEvent.VK_LEFT);
        ec.pushKey(KeyEvent.VK_LEFT);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip6,tooltip6.equals(new JMenuItemOperator(tt).getText()));
        int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
    }
    /**



     * @caseblock Tooltips
     * @usecase Tooltip property value highlighted when focus in edit control on property value (for attribute).
     */
    public void testAttributePropertyValueTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg7, classDiagramName7);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
             clE=d.putElementOnDiagram(className7,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility7,attributeType7,attributeName7,attributeDefValue7);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //add property
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(1,InputEvent.BUTTON1_MASK);
        PropertySheetOperator psh=PropertySheetOperator.invoke();
        Property tvp=new Property(psh,"Tagged Values");
        tvp.openEditor();
        JDialogOperator tvDlg=new JDialogOperator(attributeName7+" - Tagged Values");
        new JButtonOperator(tvDlg,"Add").push();
        JTableOperator tbl=new JTableOperator(tvDlg);
        tbl.selectCell(0,0);
        new EventTool().waitNoEvent(500);
        tbl.typeKey('e');new EventTool().waitNoEvent(500);
        tbl.typeKey('e');new EventTool().waitNoEvent(500);
        tbl.selectCell(0,2);new EventTool().waitNoEvent(500);
        tbl.typeKey('3');new EventTool().waitNoEvent(500);
        tbl.typeKey('3');new EventTool().waitNoEvent(500);
        try{Thread.sleep(5000);}catch(Exception ex){}
        new JButtonOperator(tvDlg,"OK").push();  
        tvDlg.waitClosed();
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className7);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_RIGHT);
        ec.pushKey(KeyEvent.VK_END);
        ec.pushKey(KeyEvent.VK_LEFT);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip7,tooltip7.equals(new JMenuItemOperator(tt).getText()));
        int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
    }
 
    /**
     * @caseblock Tooltips
     * @usecase Tooltip after initial value with existing property (for attribute).
     */
    public void testAttributeInitialValueBeforePropertyTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg8, classDiagramName8);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className8,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility8,attributeType8,attributeName8,attributeDefValue8);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //add property
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(1,InputEvent.BUTTON1_MASK);
        PropertySheetOperator psh=PropertySheetOperator.invoke();
        Property tvp=new Property(psh,"Tagged Values");
        tvp.openEditor();
        JDialogOperator tvDlg=new JDialogOperator(attributeName8+" - Tagged Values");
        new JButtonOperator(tvDlg,"Add").push();
        JTableOperator tbl=new JTableOperator(tvDlg);
        tbl.selectCell(0,0);
        new EventTool().waitNoEvent(500);
        tbl.typeKey('e');new EventTool().waitNoEvent(500);
        tbl.typeKey('e');new EventTool().waitNoEvent(500);
        tbl.selectCell(0,2);new EventTool().waitNoEvent(500);
        tbl.typeKey('3');new EventTool().waitNoEvent(500);
        tbl.typeKey('3');new EventTool().waitNoEvent(500);
        try{Thread.sleep(5000);}catch(Exception ex){}
        new JButtonOperator(tvDlg,"OK").push();  
        tvDlg.waitClosed();
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className8);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_EQUALS);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip8,tooltip8.equals(new JMenuItemOperator(tt).getText()));
        int dx=Math.abs(attrCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(attrCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>attrCmp.getRectangle().width || dy>attrCmp.getRectangle().height);
    }

    /**
     * @caseblock Tooltips
     * @usecase Tooltip appears for operation edition
     */
    public void testOperationTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg9, classDiagramName9);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className9,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility9,operationRetType9,operationName9,parameterType9,parameterName9, true);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //operation in model
        String modAt=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className9);
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className9);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        //check for constuctor
        CompartmentOperator operCmp=opComp.getCompartments().get(0);
        operCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip9,tooltip9.equals(new JMenuItemOperator(tt).getText()));
         int dx=Math.abs(operCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(operCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>operCmp.getRectangle().width || dy>operCmp.getRectangle().height);
   }
    /**
     * @caseblock Tooltips
     */
    public void testOperationVisibilityTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg10, classDiagramName10);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className10,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility10,operationRetType10,operationName10,parameterType10,parameterName10, true);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //operation in model
        String modAt=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className10);
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className10);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator operCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className10);
        operCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_HOME);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        String tooltip=new JMenuItemOperator(tt).getText();
        System.out.println(tooltip);
        assertTrue("Tooltip "+tooltip+" isn't correct, should be "+tooltip10+":::"+tooltip.length()+" vs "+tooltip10.length()+" vs "+tooltip.trim().length()+"|||"+tooltip.indexOf(tooltip10),tooltip.startsWith(tooltip10));
         int dx=Math.abs(operCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(operCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>operCmp.getRectangle().width || dy>operCmp.getRectangle().height);
   }
    /**
     * @caseblock Tooltips
     */
    public void testOperationNameTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg11, classDiagramName11);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className11,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility11,operationRetType11,operationName11,parameterType11,parameterName11, true);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //operation in model
        String modAt=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className11);
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className11);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator operCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className11);
        operCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_LEFT);
        ec.pushKey(KeyEvent.VK_RIGHT);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        String tooltip=new JMenuItemOperator(tt).getText();
        System.out.println(tooltip);
        assertTrue("Tooltip "+tooltip+" isn't correct, should be "+tooltip11+":::"+tooltip.length()+" vs "+tooltip11.length()+" vs "+tooltip.trim().length()+"|||"+tooltip.indexOf(tooltip11),tooltip.startsWith(tooltip11));
        int dx=Math.abs(operCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(operCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>operCmp.getRectangle().width || dy>operCmp.getRectangle().height);
   }
    /**
     * @caseblock Tooltips
     */
    public void testOperationParameterTypeTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg12, classDiagramName12);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className12,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility12,operationRetType12,operationName12,parameterType12,parameterName12, true);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //operation in model
        String modAt=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className12);
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className12);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator operCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className12);
        operCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        new EventTool().waitNoEvent(500);
        ec.pushKey(KeyEvent.VK_SPACE);
        new EventTool().waitNoEvent(500);
        ec.pushKey(KeyEvent.VK_LEFT);
        new EventTool().waitNoEvent(500);
        ec.pushKey(KeyEvent.VK_LEFT);
        new EventTool().waitNoEvent(500);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        String tooltip=new JMenuItemOperator(tt).getText();
        System.out.println(tooltip);
        assertTrue("Tooltip "+tooltip+" isn't correct, should be "+tooltip12+":::"+tooltip.length()+" vs "+tooltip12.length()+" vs "+tooltip.trim().length()+"|||"+tooltip.indexOf(tooltip12),tooltip.startsWith(tooltip12));
        int dx=Math.abs(operCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(operCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>operCmp.getRectangle().width || dy>operCmp.getRectangle().height);
   }
    /**
     * @caseblock Tooltips
     */
    public void testOperationParameterNameTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg13, classDiagramName13);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className13,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility13,operationRetType13,operationName13,parameterType13,parameterName13, true);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //operation in model
        String modAt=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className13);
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className13);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator operCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className13);
        operCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        new EventTool().waitNoEvent(500);
        ec.pushKey(KeyEvent.VK_SPACE);
        new EventTool().waitNoEvent(500);
        ec.pushKey(KeyEvent.VK_RIGHT);
        new EventTool().waitNoEvent(500);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        String tooltip=new JMenuItemOperator(tt).getText();
        System.out.println(tooltip);
        assertTrue("Tooltip "+tooltip+" isn't correct, should be "+tooltip13+":::"+tooltip.length()+" vs "+tooltip13.length()+" vs "+tooltip.trim().length()+"|||"+tooltip.indexOf(tooltip13),tooltip.startsWith(tooltip13));
        int dx=Math.abs(operCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(operCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>operCmp.getRectangle().width || dy>operCmp.getRectangle().height);
   }
    /**
     * @caseblock Tooltips
     */
    public void testOperationReturnTypeTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg14, classDiagramName14);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className14,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility14,operationRetType14,operationName14,parameterType14,parameterName14, true);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //operation in model
        String modAt=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className14);
        ///////refresh objects
        try {
            clE=new DiagramElementOperator(d,className14);
        } catch(Exception ex) {
            fail("Unable to find class on diagram.");
        }
        try {
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find attribute compartment");
        }
        ///////
        CompartmentOperator operCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className14);
        operCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.pushKey(KeyEvent.VK_LEFT);
        ec.pushKey(KeyEvent.VK_LEFT);
        ec.pushKey(KeyEvent.VK_LEFT);
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        String tooltip=new JMenuItemOperator(tt).getText();
        String tmp="";
        for(int i=0;i<tooltip.length();i++)
        {
            String part1="CH1("+i+")="+tooltip.charAt(i)+"/"+(int)(tooltip.charAt(i));
            String part2="";
                    if(i<tooltip14.length())part2="CH2("+i+")="+tooltip14.charAt(i)+"/"+(int)(tooltip14.charAt(i));
            System.out.println(part1 + " VS "+ part2);
        }
        assertTrue("Tooltip "+tooltip+" isn't correct, should be "+tooltip14+":::"+tooltip.length()+" vs "+tooltip14.length()+" vs "+tooltip.trim().length()+"|||"+tooltip.indexOf(tooltip14),tooltip.startsWith(tooltip14));
        int dx=Math.abs(operCmp.getCenterPoint().x-tt.getCenterX());
        int dy=Math.abs(operCmp.getCenterPoint().y-tt.getCenterY());
        assertTrue("Tooltip is located far from attribute compartment in compare to compartment width and height, (center_dx:"+dx+",center_dy:"+dy+ ")",dx>operCmp.getRectangle().width || dy>operCmp.getRectangle().height);
   }
    /**
     * @caseblock Tooltips
     */
    public void testSequenceDiagramTooltip() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg15,"Sequence Diagram", classDiagramName15);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE1=null,clE2=null;
        d.createGenericElementOnDiagram("A1:"+className15_1, ElementTypes.LIFELINE,40,50);
        new JButtonOperator(new JDialogOperator("Classifier not found"),"Yes").push();
        d.createGenericElementOnDiagram("A2:"+className15_2, ElementTypes.LIFELINE,180,50);
        new JButtonOperator(new JDialogOperator("Classifier not found"),"Yes").push();
        clE1 = new LifelineOperator(d, "A1", className15_1);
        clE2 = new LifelineOperator(d, "A2", className15_2);
        //clE2 = d.putElementOnDiagram("A2:"+className15_2, ElementTypes.LIFELINE);
        LinkOperator lnk=d.createLinkOnDiagram(LinkTypes.SYNC_MESSAGE, clE1, clE2);
        log("POINT:"+lnk.getNearCenterPointWithoutOverlayCheck());
        //JPopupMenuOperator lnkPop=lnk.getPopup();
        d.getDrawingArea().clickForPopup(lnk.getNearCenterPointWithoutOverlayCheck().x,lnk.getNearCenterPointWithoutOverlayCheck().y, InputEvent.BUTTON3_MASK);
        JPopupMenuOperator lnkPop=new JPopupMenuOperator();
        lnkPop.pushMenu("Operations|Add Operation");
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //try tooltip
        JPopupMenuOperator tt=new JPopupMenuOperator();
        //
        assertTrue("Tooltip "+new JMenuItemOperator(tt).getText()+" isn't correct, should be "+tooltip15,tooltip15.equals(new JMenuItemOperator(tt).getText()));
    }
      
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
         pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            org.netbeans.test.uml.editcontrol.utils.Utils.commonSetup(workdir, prName);
            //
            codeSync=true;
        }
   }
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        //
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        long tmp=JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        long tmp2=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000); 
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 1000);
        try{
            DiagramOperator d=new DiagramOperator("clD");
            DrawingAreaOperator drAr=d.getDrawingArea();
            java.awt.Point a=drAr.getFreePoint();
            drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
            drAr.pushKey(KeyEvent.VK_ESCAPE);
           new Thread(new Runnable() {
                public void run() {
                    new JButtonOperator(new JDialogOperator("Save"),"No").push();
                }
            }).start();
            d.closeWindow();
        }catch(Exception ex){};
         JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", tmp); 
         JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", tmp2);
       //save
        org.netbeans.test.umllib.util.Utils.tearDown();

   }
    
    
}

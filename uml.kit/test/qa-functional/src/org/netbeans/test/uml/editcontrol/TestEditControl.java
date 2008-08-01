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
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.PopupConstants;



/**
 *
 * @author psb
 * @spec uml/UML-EditControl.xml
 */
public class TestEditControl extends UMLTestCase {
    
    private static String prName= "EditControlClass";
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
    //--
    private static String classDiagramName2 = "clD2";
    private static String  className2 ="class2";
    private static String workPkg2 = "pkg2";
    private static String attributeName2 = "attribute2";
    private static String attributeType2 = "double";
    private static String attributeDefValue2 = "3.4";
    private static String attributeVisibility2 = defaultAttributeVisibility;
    private static String atStr2=attributeVisibility2+" "+attributeType2+" "+attributeName2+" = "+attributeDefValue2;
    //--
    private static String classDiagramName3 = "clD3";
    private static String  className3 ="class3";
    private static String workPkg3 = "pkg3";
    private static String operationName3  = "operation3";
    private static String operationRetType3 = "int";
    private static String parameterType3 = "double";
    private static String parameterName3 = "s";
    private static String operationVisibility3 = defaultOperationVisibility;
    private static String opStr3=operationVisibility3+" "+operationRetType3+"  "+operationName3+"( "+parameterType3+" "+parameterName3+" )";
    //--
    private static String classDiagramName4 = "clD4";
    private static String  className4_1 ="class4";
    private static String  className4_2 ="class4_modified";
    private static String workPkg4 = "pkg4";
    //--
    private static String classDiagramName5 = "clD5";
    private static String  className5 ="class5";
    private static String workPkg5 = "pkg5";
    //--
    private static String classDiagramName6 = "clD6";
    private static String  className6 ="class6";
    private static String workPkg6 = "pkg6";
    private static String attributeName6_1 = "attribute6";
    private static String attributeName6_2 = "attribute6_modified";
    private static String attributeType6 = "double";
    private static String attributeDefValue6 = "2.3";
    private static String attributeVisibility6 = defaultAttributeVisibility;
    private static String atStr6_1=attributeVisibility6+" "+attributeType6+" "+attributeName6_1+" = "+attributeDefValue6;
    private static String atStr6_2=attributeVisibility6+" "+attributeType6+" "+attributeName6_2+" = "+attributeDefValue6;
    //--
    private static String classDiagramName7 = "clD7";
    private static String  className7 ="class7";
    private static String workPkg7 = "pkg7";
    private static String attributeName7 = "attribute7";
    private static String attributeType7 = defaultAttributeType;
    private static String attributeDefValue7 = "5";
    private static String attributeVisibility7 = defaultAttributeVisibility;
    private static String atStr7=attributeVisibility7+" "+attributeType7+" "+attributeName7+" = "+attributeDefValue7;
    //--
    private static String classDiagramName8 = "clD8";
    private static String  className8 ="class8";
    private static String workPkg8 = "pkg8";
    private static String operationName8_1  = "operation8";
    private static String operationName8_2  = "operation8_modified";
    private static String operationRetType8 = "int";
    private static String parameterType8 = "double";
    private static String parameterName8 = "s";
    private static String operationVisibility8 = defaultOperationVisibility;
    private static String opStr8_1=operationVisibility8+" "+operationRetType8+"  "+operationName8_1+"( "+parameterType8+" "+parameterName8+" )";
    private static String opStr8_2=operationVisibility8+" "+operationRetType8+"  "+operationName8_2+"( "+parameterType8+" "+parameterName8+" )";
    //--
    private static String classDiagramName9 = "clD9";
    private static String  className9 ="class9";
    private static String workPkg9 = "pkg9";
    private static String operationName9  = "operation9";
    private static String operationRetType9 = "int";
    private static String parameterType9 = "double";
    private static String parameterName9 = "s";
    private static String operationVisibility9 = defaultOperationVisibility;
    private static String opStr9=operationVisibility9+" "+operationRetType9+"  "+operationName9+"( "+parameterType9+" "+parameterName9+" )";
    //--
    private static String classDiagramName10 = "clD10";
    private static String  className10 ="class10";
    private static String workPkg10 = "pkg10";
    //--
    private static String classDiagramName11 = "clD11";
    private static String workPkg11 = "pkg11";
    //--
    private static String classDiagramName12 = "clD12";
    private static String  className12 ="class12";
    private static String workPkg12 = "pkg12";
    private static String attributeName12 = "attribute12";
    private static String attributeType12 = defaultAttributeType;
    private static String attributeDefValue12 = "12";
    private static String attributeVisibility12 = defaultAttributeVisibility;
    private static String atStr12=attributeVisibility12+" "+attributeType12+" "+attributeName12+" = "+attributeDefValue12;
    private static String atStrTmp12=attributeVisibility12+" "+attributeType12+" "+" = "+attributeDefValue12;
    //--
    private static String classDiagramName13 = "clD13";
    private static String  className13 ="class13";
    private static String workPkg13 = "pkg13";
    private static String atStr13=defaultAttributeVisibility+" "+defaultAttributeType+" "+defaultNewElementName;
    private static String atStrTmp13=defaultAttributeVisibility+" "+defaultAttributeType+" ";
    //--
    private static String classDiagramName14 = "clD14";
    private static String  className14 ="class14";
    private static String workPkg14 = "pkg14";
    private static String operationName14  = "operation14";
    private static String operationRetType14 = "int";
    private static String parameterType14 = "double";
    private static String parameterName14 = "s";
    private static String operationVisibility14 = defaultOperationVisibility;
    private static String opStr14=operationVisibility14+" "+operationRetType14+"  "+operationName14+"( "+parameterType14+" "+parameterName14+" )";
    private static String opStrTmp14=operationVisibility14+" "+operationRetType14+"  "+"( "+parameterType14+" "+parameterName14+" )";
    //--
    private static String classDiagramName15 = "clD15";
    private static String  className15 ="class15";
    private static String workPkg15 = "pkg15";
    private static String operationRetType15 = defaultReturnType;
    private static String operationVisibility15 = defaultOperationVisibility;
    private static String opStr15=operationVisibility15+" "+operationRetType15+"  "+defaultNewElementName+"(  )";
    private static String opStrTmp15=operationVisibility15+" "+operationRetType15+"  "+"(  )";
    //--
    private static String classDiagramName16 = "clD16";
    private static String  className16_1 ="class16_1";
    private static String  className16_2 ="class16_2";
    private static String  linkName16 ="linkName16";
    private static String workPkg16 = "pkg16";
    //--
    private static String classDiagramName17 = "clD17";
    private static String  className17_1 ="class17_1";
    private static String  className17_2 ="class17_2";
    private static String  linkName17_1 ="linkName17";
    private static String  linkName17_2 ="linkName17_modified";
    private static String workPkg17 = "pkg17";
    //--
    private static String classDiagramName18 = "clD18";
    private static String  className18_1 ="class18_1";
    private static String  className18_2 ="class18_2";
    private static String  linkName18 ="linkName18";
    private static String workPkg18 = "pkg18";
    //--
    private static String classDiagramName19 = "clD19";
    private static String  className19 ="class19";
    private static String workPkg19 = "pkg19";
    //--
    private static String classDiagramName20 = "clD20";
    private static String  className20 ="class20";
    private static String workPkg20 = "pkg20";
    //--
    private static String classDiagramName21 = "clD21";
    private static String  className21 ="class21";
    private static String workPkg21 = "pkg21";
    private static String attributeName21[] = {defaultNewElementName,"attribute21_1",defaultNewElementName,defaultNewElementName,"attribute21_2"};
    private static String attributeType21[] = {defaultAttributeType,"double",defaultAttributeType,defaultAttributeType,defaultAttributeType};
    private static String attributeDefValue21[] = {"","2.3","","","3"};
    private static String attributeVisibility21[] = {defaultAttributeVisibility,defaultAttributeVisibility,defaultAttributeVisibility,defaultAttributeVisibility,"public"};
    private static String atStr21[]=new String[5];
    {for(int i=0;i<5;i++)atStr21[i]=attributeVisibility21[i]+" "+attributeType21[i]+" "+attributeName21[i]+(attributeDefValue21[i].equals("")?"":(" = "+attributeDefValue21[i]));}
    //--
    private static String classDiagramName22 = "clD22";
    private static String  className22 ="class22";
    private static String workPkg22 = "pkg22";
    private static String operationName22[] = {defaultNewElementName,"operation22_1",defaultNewElementName,defaultNewElementName,"operation22_2"};
    private static String operationRetType22[] = {defaultReturnType,"double",defaultReturnType,defaultReturnType,"int"};
    private static String operationParameters22[] = {"","","","","int f"};
    private static String operationVisibility22[] = {defaultOperationVisibility,defaultOperationVisibility,defaultOperationVisibility,defaultOperationVisibility,"private"};
    private static String opStr22[]=new String[5];
    {for(int i=0;i<5;i++)opStr22[i]=operationVisibility22[i]+" "+operationRetType22[i]+"  "+operationName22[i]+"( "+operationParameters22[i]+" )";}
    //--
    private static String classDiagramName23 = "clD23";
    private static String  className23 ="class23";
    private static String workPkg23 = "pkg23";
    private static String attributeName23 = "attribute23";
    private static String attributeType23 = "double";
    private static String attributeDefValue23 = "2.3";
    private static String attributeVisibility23_1 = "private";
    private static String attributeVisibility23_2 = "public";
    private static String atStr23_1=attributeVisibility23_1+" "+attributeType23+" "+attributeName23+" = "+attributeDefValue23;
    private static String atStr23_2=attributeVisibility23_2+" "+attributeType23+" "+attributeName23+" = "+attributeDefValue23;
    //--
    private static String classDiagramName24 = "clD24";
    private static String  className24 ="class24";
    private static String workPkg24 = "pkg24";
    private static String attributeName24 = "attribute24";
    private static String attributeType24_1 = "double";
    private static String attributeType24_2 = "float";
    private static String attributeDefValue24 = "2.3";
    private static String attributeVisibility24 = defaultAttributeVisibility;
    private static String atStr24_1=attributeVisibility24+" "+attributeType24_1+" "+attributeName24+" = "+attributeDefValue24;
    private static String atStr24_2=attributeVisibility24+" "+attributeType24_2+" "+attributeName24+" = "+attributeDefValue24;
    //--
    private static String classDiagramName25 = "clD25";
    private static String  className25 ="class25";
    private static String workPkg25 = "pkg25";
    private static String attributeName25 = "attribute25";
    private static String attributeType25 = "double";
    private static String attributeDefValue25_1 = "2.3";
    private static String attributeDefValue25_2 = "-4.56";
    private static String attributeVisibility25 = defaultAttributeVisibility;
    private static String atStr25_1=attributeVisibility25+" "+attributeType25+" "+attributeName25+" = "+attributeDefValue25_1;
    private static String atStr25_2=attributeVisibility25+" "+attributeType25+" "+attributeName25+" = "+attributeDefValue25_2;
    //--
    private static String classDiagramName26 = "clD26";
    private static String  className26 ="class26";
    private static String workPkg26 = "pkg26";
    private static String operationName26  = "operation26";
    private static String operationRetType26 = "double";
    private static String parameterType26 = "int";
    private static String parameterName26 = "f";
    private static String operationVisibility26_2 = "private";
    private static String operationVisibility26_1 = "public";
    private static String opStr26_1=operationVisibility26_1+" "+operationRetType26+"  "+operationName26+"( "+parameterType26+" "+parameterName26+" )";
    private static String opStr26_2=operationVisibility26_2+" "+operationRetType26+"  "+operationName26+"( "+parameterType26+" "+parameterName26+" )";
    //--
    private static String classDiagramName27 = "clD27";
    private static String  className27 ="class27";
    private static String workPkg27 = "pkg27";
    private static String operationName27  = "operation27";
    private static String operationRetType27 = "int";
    private static String operationParameters27_1="int f";
    private static String operationParameters27_2 = "int f_modified, double g";
    private static String operationVisibility27 = defaultOperationVisibility;
    private static String opStr27_1=operationVisibility27+" "+operationRetType27+"  "+operationName27+"( "+operationParameters27_1+" )";
    private static String opStr27_2=operationVisibility27+" "+operationRetType27+"  "+operationName27+"( "+operationParameters27_2+" )";
    //--
    private static String classDiagramName28 = "clD28";
    private static String  className28 ="class28";
    private static String workPkg28 = "pkg28";
private static String operationName28  = "operation28";
    private static String operationRetType28_1 = "double";
    private static String operationRetType28_2 = "int";
    private static String parameterType28 = "int";
    private static String parameterName28 = "f";
    private static String operationVisibility28 = defaultOperationVisibility;
    private static String opStr28_1=operationVisibility28+" "+operationRetType28_1+"  "+operationName28+"( "+parameterType28+" "+parameterName28+" )";
    private static String opStr28_2=operationVisibility28+" "+operationRetType28_2+"  "+operationName28+"( "+parameterType28+" "+parameterName28+" )";
    
    
    /** Need to be defined because of JUnit */
    public TestEditControl(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.editcontrol.TestEditControl.class);
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
     * @caseblock Class
     * @usecase Name after drop
     */
    public void testDropAndName() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg1, classDiagramName1);
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
            pl.selectTool("Class");
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        //
        DiagramElementOperator myCl=new DiagramElementOperator(d,"Unnamed");
        //
        drAr.typeKey(className1.charAt(0));
            new org.netbeans.test.umllib.EditControlOperator();
        for(int i=1;i<className1.length();i++) {
            drAr.typeKey(className1.charAt(i));
        }
        //
        assertTrue("Class name \""+(new org.netbeans.test.umllib.EditControlOperator().getTextFieldOperator().getText())+"\" in Class Identifier Name isn't correct,should be \""+className1+"\"",className1.equals((new org.netbeans.test.umllib.EditControlOperator().getTextFieldOperator().getText())));
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //name in model
        new EventTool().waitNoEvent(500);
        String nmMod=myCl.getSubjectVNs().get(0);
        //
        assertTrue("Class name \""+nmMod+"\" in model isn't correct,should be \""+className1+"\"",className1.equals(nmMod));
    }
    /**
     * @caseblock Class
     * @usecase Name attribute after insert
     */
    public void testAttributeInsertAndName() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg2, classDiagramName2);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
        clE=d.putElementOnDiagram(className2,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(null,attributeType2,attributeName2,attributeDefValue2);
        //check text in textfield
        assertTrue("Attribute \""+(new org.netbeans.test.umllib.EditControlOperator().getTextFieldOperator().getText())+"\" in Attribute Compartment isn't correct,should be \""+atStr2+"\"",atStr2.equals((new org.netbeans.test.umllib.EditControlOperator().getTextFieldOperator().getText())));
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        //
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr2+"\"",atStr2.equals(modAt));
    }
    /**
     * @caseblock Class
     * @usecase Name operation after insert
     */
    public void testOperationInsertAndName() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg3, classDiagramName3);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className3,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        //fail(opComp.getName()+opComp.getCenterPoint()+":::"+atComp.getName()+atComp.getCenterPoint()+":::"+nmComp.getName()+nmComp.getCenterPoint());
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(null, operationRetType3, operationName3, parameterType3,parameterName3);
        //check text in textfield
        String curEC=new org.netbeans.test.umllib.EditControlOperator().getTextFieldOperator().getText();
        assertTrue("Operation \""+curEC+"\" in Operation Compartment isn't correct,should be \""+opStr3+"\"",org.netbeans.test.uml.editcontrol.utils.Utils.compareWithoutExtraSpaceChars(opStr3,curEC));
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className3);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr3+"\"",opStr3.equals(operInModel));
    }
    /**
     * @caseblock Class
     * @usecase ReName class with double click
     */
    public void testRenameClassWithDoubleClick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg4, classDiagramName4);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className4_1,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator nmComp=null;
            nmComp=new CompartmentOperator(clE,CompartmentTypes.NAME_COMPARTMENT);
        nmComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        //
        assertTrue("Text \""+tOp.getText()+"\" in name compartment isn't correct, should be \""+className4_1+"\"",className4_1.equals(tOp.getText()));
        //start type of new name (old should be selected and replaced)
        for(int i=0;i<className4_2.length();i++) {
            drAr.typeKey(className4_2.charAt(i));
        }
        assertTrue("Class name \""+(tOp.getText())+"\" in Class Identifier Name isn't correct,should be \""+className4_2+"\"",className4_2.equals((tOp.getText())));
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON1_MASK);
        //name in model
        new EventTool().waitNoEvent(500);
        String nmMod=clE.getSubjectVNs().get(0);
        //
        assertTrue("Class name \""+nmMod+"\" in model isn't correct,should be \""+className4_2+"\"",className4_2.equals(nmMod));
    }
    /**
     * @caseblock Class
     * @usecase Name class with double click
     */
    public void testNameClassWithDoubleClick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg5, classDiagramName5);
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
            pl.selectTool("Class");
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //
        DiagramElementOperator myCl=null;
            myCl=new DiagramElementOperator(d,"Unnamed");
        CompartmentOperator nmComp=null;
            nmComp=new CompartmentOperator(myCl,CompartmentTypes.NAME_COMPARTMENT);
        nmComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        //
        assertTrue("Text \""+tOp.getText()+"\" in name compartment isn't correct, should be \""+defaultNewElementName+"\"",defaultNewElementName.equals(tOp.getText()));
        //replace selected defaultNewClassName with name
        for(int i=0;i<className5.length();i++) {
            drAr.typeKey(className5.charAt(i));
        }
        //
        assertTrue("Class name \""+tOp.getText()+"\" in Class Identifier Name isn't correct,should be \""+className5+"\"",className5.equals(tOp.getText()));
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //name in model
        new EventTool().waitNoEvent(500);
        String nmMod=myCl.getSubjectVNs().get(0);
        //
        assertTrue("Class name \""+nmMod+"\" in model isn't correct,should be \""+className5+"\"",className5.equals(nmMod));
    }
    /**
     * @caseblock Class
     * @usecase ReName attribute with double click
     */
    public void testAttributeRenameWithDoubleclick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg6, classDiagramName6);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className6,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(null,attributeType6,attributeName6_1,attributeDefValue6);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        //
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr6_1+"\"",atStr6_1.equals(modAt));
        
        ///////refresh objects
            clE=new DiagramElementOperator(d,className6);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+atStr6_1+"\"",atStr6_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(null,attributeType6,attributeName6_2,attributeDefValue6);
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+atStr6_2+"\"",atStr6_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        modAt=attrCmp.getName();
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr6_2+"\"",atStr6_2.equals(modAt));
        
    }
    /**
     * @caseblock Class
     * @usecase Name attribute with double click
     */
    public void testAttributeNameWithDoubleclick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg7, classDiagramName7);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className7,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        ///////refresh objects
            clE=new DiagramElementOperator(d,className7);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //
        String OUT_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_out.txt";
        String ERR_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_err.txt";
        TestOut defTestOut=JemmyProperties.getCurrentOutput();
        try
        {
            PrintStream myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
            PrintStream myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
            JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        }
        catch(Exception ex)
        {
            
        }
        //------------
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(null,attributeType7,attributeName7,attributeDefValue7);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+atStr7+"\"",atStr7.equals(tOp.getText()));
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //-------------
        JemmyProperties.setCurrentOutput(defTestOut);
        try
        {
            BufferedReader myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
            String line;
            do {
                line = myIn.readLine();
                if (line!=null && line.indexOf("java.lang.NullPointerException")>-1){
                    line=myIn.readLine();
                    if(line!=null && line.indexOf("at com.embarcadero.uml.core.support.umlutils.PropertyElementManager.processEnumeration(PropertyElementManager.java")>-1)
                    {
                        fail("NPE on addition attribute with initial value");
                    }
                    else
                    {
                        fail("Unknown NPE in log: "+line);
                    }
                }
                else if (line!=null && line.indexOf("Exception")>-1)
                {
                    fail("Unexpected exception in log: "+line);
                }
            } while (line != null);
        }
        catch(Exception ex)
        {
            
        }
        String modAt=attrCmp.getName();
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr7+"\"",atStr7.equals(modAt));
        
    }
    /**
     * @caseblock Class
     * @usecase ReName operation with double click
     */
    public void testOperationRenameWithDoubleclick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg8, classDiagramName8);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className8,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(null, operationRetType8, operationName8_1, parameterType8,parameterName8);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className8);
        //
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr8_1+"\"",opStr8_1.equals(operInModel));
        ///////refresh objects
            clE=new DiagramElementOperator(d,className8);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        CompartmentOperator oprCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className8);
        oprCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //opComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+opStr8_1+"\"",opStr8_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(null, operationRetType8, operationName8_2, parameterType8,parameterName8);
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+opStr8_2+"\"",opStr8_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className8);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr8_2+"\"",opStr8_2.equals(operInModel));
        
    }
    /**
     * @caseblock Class
     * @usecase Name operation with double click
     */
    public void testOperationNameWithDoubleclick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg9, classDiagramName9);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className9,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        ///////refresh objects
            clE=new DiagramElementOperator(d,className9);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        CompartmentOperator oprCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className9);
        oprCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        // opComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);   
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(null, operationRetType9, operationName9, parameterType9,parameterName9);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+opStr9+"\"",opStr9.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className9);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr9+"\"",opStr9.equals(operInModel));
        
    }
    /**
     * @caseblock Class
     * @usecase Delete class name from named class after double click
     */
    public void testDeleteClassNameWithDoubleClick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg10, classDiagramName10);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className10,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator nmComp=null;
            nmComp=new CompartmentOperator(clE,CompartmentTypes.NAME_COMPARTMENT);
        nmComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        //
        assertTrue("Text \""+tOp.getText()+"\" in name compartment isn't correct, should be \""+className10+"\"",className10.equals(tOp.getText()));
        //delete name
        new EventTool().waitNoEvent(500);
        drAr.pushKey(KeyEvent.VK_DELETE);
        //
        assertTrue("Class name \""+(tOp.getText())+"\" in Class Identifier Name isn't correct,field should be empty.","".equals((tOp.getText())));
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON1_MASK);
        //name in model
        new EventTool().waitNoEvent(500);
        String nmMod=clE.getSubjectVNs().get(0);
        //
        assertTrue("Class name \""+nmMod+"\" in model isn't correct,should be restored to \""+className10+"\"",className10.equals(nmMod));
    }
    /**
     * @caseblock Class
     * @usecase Delete class name from unnamed class after double click
     */
    public void testDeleteClassUnnameNameWithDoubleClick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg11, classDiagramName11);
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
            pl.selectTool("Class");
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //
        DiagramElementOperator myCl=null;
            myCl=new DiagramElementOperator(d,"Unnamed");
        CompartmentOperator nmComp=null;
            nmComp=new CompartmentOperator(myCl,CompartmentTypes.NAME_COMPARTMENT);
        nmComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        //
        assertTrue("Text \""+tOp.getText()+"\" in name compartment isn't correct, should be \""+defaultNewElementName+"\"",defaultNewElementName.equals(tOp.getText()));
        //delete name
        new EventTool().waitNoEvent(500);
        drAr.pushKey(KeyEvent.VK_DELETE);
        //
        assertTrue("Class name \""+tOp.getText()+"\" in Class Identifier Name isn't correct,should be empty.","".equals(tOp.getText()));
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //name in model
        new EventTool().waitNoEvent(500);
        String nmMod=myCl.getSubjectVNs().get(0);
        //
        assertTrue("Class name \""+nmMod+"\" in model isn't correct,should be \""+defaultNewElementName+"\"",defaultNewElementName.equals(nmMod));
    }
    /**
     * @caseblock Class
     * @usecase Delete name of named attribute after double click
     */
    public void testDeleteAttributeNameWithDoubleClick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg12, classDiagramName12);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className12,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
        try {
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        } catch(Exception ex) {
            fail("BLOCKING: Unable to find attribute compartment");
        }
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //
        String OUT_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_out.txt";
        String ERR_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_err.txt";
        TestOut defTestOut=JemmyProperties.getCurrentOutput();
        try
        {
            PrintStream myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
            PrintStream myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
            JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        }
        catch(Exception ex)
        {
            
        }
        //----------------
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(null,attributeType12,attributeName12,attributeDefValue12);
         //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //----------------
       JemmyProperties.setCurrentOutput(defTestOut);
        try
        {
            BufferedReader myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
            String line;
            do {
                line = myIn.readLine();
                if (line!=null && line.indexOf("java.lang.NullPointerException")>-1){
                    line=myIn.readLine();
                    if(line!=null && line.indexOf("at com.embarcadero.uml.core.support.umlutils.PropertyElementManager.processEnumeration(PropertyElementManager.java")>-1)
                    {
                        fail("NPE on addition attribute with initial value");
                    }
                    else
                    {
                        fail("Unknown NPE in log: "+line);
                    }
                }
                else if (line!=null && line.indexOf("Exception")>-1)
                {
                    fail("Unexpected exception in log: "+line);
                }
            } while (line != null);
        }
        catch(Exception ex)
        {
            
        }
        ///////refresh objects
            clE=new DiagramElementOperator(d,className12);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        //
       assertTrue("BLOCKING: Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr12+"\"",atStr12.equals(modAt));
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("BLOCKING: Attribute \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+atStr12+"\"",atStr12.equals(tOp.getText()));
        //-delete name
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(null,attributeType12,"",attributeDefValue12);
        
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+atStrTmp12+"\"",atStrTmp12.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        modAt=attrCmp.getName();
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly restored,should be \""+atStr12+"\"",atStr12.equals(modAt));
        
    }
    /**
     * @caseblock Class
     * @usecase Delete name of unnamed attribute after double click
     */
    public void testDeleteAttributeNameUnnamedWithDoubleClick() {
         lastTestCase=getCurrentTestMethodName();
       DiagramOperator d = createOrOpenDiagram(project,workPkg13, classDiagramName13);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className13,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        ///////refresh objects
            clE=new DiagramElementOperator(d,className13);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        //
        assertTrue("BLOCKING: Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr13+"\"",atStr13.equals(modAt));
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("BLOCKING: Attribute \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+atStr13+"\"",atStr13.equals(tOp.getText()));
        //-delete name
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(null,null,"");
        
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+atStrTmp13+"\"",atStrTmp13.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        modAt=attrCmp.getName();
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly restored,should be \""+atStr13+"\"",atStr13.equals(modAt));
        
    }
    /**
     * @caseblock Class
     * @usecase Delete name of named operation after double click
     */
    public void testDeleteOperationNameWithDoubleClick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg14, classDiagramName14);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className14,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(null, operationRetType14, operationName14, parameterType14,parameterName14);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className14);
        //
        assertTrue("BLOCKING: Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr14+"\"",opStr14.equals(operInModel));
        ///////refresh objects
            clE=new DiagramElementOperator(d,className14);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        CompartmentOperator oprCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className14);
        oprCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
         //opComp.clickOnCenter(2,InputEvent.BUTTON1_MASK); 
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("BLOCKING: Operation \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+opStr14+"\"",opStr14.equals(tOp.getText()));
        //-delete name
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(null, operationRetType14, "", parameterType14,parameterName14);
        //
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+opStrTmp14+"\"",opStrTmp14.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className14);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly restored,should be \""+opStr14+"\"",opStr14.equals(operInModel));
        
    }
    /**
     * @caseblock Class
     * @usecase Delete name of unnamed operation after double click
     */
    public void testDeleteOperationNameUnnamedWithDoubleClick() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg15, classDiagramName15);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className15,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className15);
        //
        assertTrue("BLOCKING: Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr15+"\"",opStr15.equals(operInModel));
        ///////refresh objects
            clE=new DiagramElementOperator(d,className15);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        CompartmentOperator oprCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className15);
        oprCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
         // opComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("BLOCKING: Operation \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+opStr15+"\"",opStr15.equals(tOp.getText()));
        //-delete name
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(null, null, "");
        //
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+opStrTmp15+"\"",opStrTmp15.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className15);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly restored,should be \""+opStr15+"\"",opStr15.equals(operInModel));
        
    }
    /**
     * @caseblock Class
     * @usecase Add and name association link
     */
    public void testAddNameAssociationLink() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg16, classDiagramName16);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE1=null,clE2=null;
        new JComboBoxOperator(d).setSelectedIndex(2);
        java.awt.Point a=drAr.getFreePoint();
            clE1=d.putElementOnDiagram(className16_1,ElementTypes.CLASS,a.x+200,a.y);
            clE2=d.putElementOnDiagram(className16_2,ElementTypes.CLASS,a.x,a.y+200);
        new EventTool().waitNoEvent(500);
        //create link
        LinkOperator assLnk=null;
            assLnk=d.createLinkOnDiagram(LinkTypes.ASSOCIATION, clE1, clE2);
        //
        /*drAr.clickForPopup(assLnk.getNearCenterPoint().x,assLnk.getNearCenterPoint().y,InputEvent.BUTTON3_MASK);
        //assLnk.getPopup().pushMenu("Labels|Link Name");
        //workaround for inner popup
        new JPopupMenuOperator().pushKey(KeyEvent.VK_LEFT);
        new EventTool().waitNoEvent(1000);*/
        assLnk.getPopup().pushMenu("Labels|Link Name");
        //end of workaround
        //new JPopupMenuOperator().pushMenu("Labels|Link Name");
        new EventTool().waitNoEvent(1000);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        //
        for(int i=0;i<linkName16.length();i++) {
            drAr.typeKey(linkName16.charAt(i));
        }
        new EventTool().waitNoEvent(1000);
        //
        assertTrue("Text \""+tOp.getText()+"\" typed in link name textfield isn't correct, should be \""+linkName16+"\"",linkName16.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(1000);
        try{Thread.sleep(1000);}catch(Exception ex){}
        String lncTxt=assLnk.getLabelsTexts()[0];
        assertTrue("There is wrong \""+lncTxt+"\" label name in model, should be \""+linkName16+"\"",linkName16.equals(lncTxt));
    }
    /**
     * @caseblock Class
     * @usecase Change a label name on a link
     */
    public void testRenameAssociationLinkUsingNameLabel() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg17, classDiagramName17);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE1=null,clE2=null;
        new JComboBoxOperator(d).setSelectedIndex(2);
        java.awt.Point a=drAr.getFreePoint();
            clE1=d.putElementOnDiagram(className17_1,ElementTypes.CLASS,a.x,a.y);
        a=drAr.getFreePoint(100);
            clE2=d.putElementOnDiagram(className17_2,ElementTypes.CLASS,a.x,a.y);
        new EventTool().waitNoEvent(500);
        //create link
        LinkOperator assLnk=null;
            assLnk=d.createLinkOnDiagram(LinkTypes.ASSOCIATION, clE1, clE2);

        //
        /*drAr.clickForPopup(assLnk.getNearCenterPoint().x,assLnk.getNearCenterPoint().y,InputEvent.BUTTON3_MASK);
        //assLnk.getPopup().pushMenu("Labels|Link Name");
        //workaround for inner popup
        new JPopupMenuOperator().pushKey(KeyEvent.VK_LEFT);
        new EventTool().waitNoEvent(1000);*/
        //end of workaround
        //new JPopupMenuOperator().pushMenu("Labels|Link Name");
        assLnk.getPopup().pushMenu("Labels|Link Name");
        new EventTool().waitNoEvent(500);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        //
        for(int i=0;i<linkName17_1.length();i++) {
            drAr.typeKey(linkName17_1.charAt(i));
        }
        new EventTool().waitNoEvent(1000);
        //
        assertTrue("BLOCKING: Text \""+tOp.getText()+"\" typed in link name textfield isn't correct, should be \""+linkName17_1+"\"",linkName17_1.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(1000);
        try{Thread.sleep(1000);}catch(Exception ex){}
        String lncTxt=assLnk.getLabelsTexts()[0];
        assertTrue("BLOCKING: There is wrong \""+lncTxt+"\" label name in model, should be \""+linkName17_1+"\"",linkName17_1.equals(lncTxt));
        //
            assLnk.getLabel(linkName17_1).select();
            try{Thread.sleep(1000);}catch(Exception ee){}
            drAr.clickMouse(assLnk.getLabel(linkName17_1).getCenterPoint().x-10,assLnk.getLabel(linkName17_1).getCenterPoint().y,2);
        //try to find edit control
        ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        tOp=ec.getTextFieldOperator();
        //
        assertTrue("Text \""+tOp.getText()+"\" in link name textfield isn't correct, should be \""+linkName17_1+"\"",linkName17_1.equals(tOp.getText()));
        //
        for(int i=0;i<linkName17_2.length();i++) {
            drAr.typeKey(linkName17_2.charAt(i));
        }
        new EventTool().waitNoEvent(500);
        //
        assertTrue("Text \""+tOp.getText()+"\" typed in link name textfield isn't correct, should be \""+linkName17_2+"\"",linkName17_2.equals(tOp.getText()));
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(1000);}catch(Exception ex){}
        lncTxt=assLnk.getLabelsTexts()[0];
        assertTrue("There is wrong \""+lncTxt+"\" label name in model, should be \""+linkName17_2+"\"",linkName17_2.equals(lncTxt));
    }
    
    /**
     * @caseblock Class
     * @usecase Delete a label name on a link
     */
    public void testAssociationLinkDeleteNameInLabel() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg18, classDiagramName18);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE1=null,clE2=null;
        new JComboBoxOperator(d).setSelectedIndex(2);
        java.awt.Point a=drAr.getFreePoint();
            clE1=d.putElementOnDiagram(className18_1,ElementTypes.CLASS,a.x,a.y);
        a=drAr.getFreePoint(100);
            clE2=d.putElementOnDiagram(className18_2,ElementTypes.CLASS,a.x,a.y);
        new EventTool().waitNoEvent(500);
        //create link
        LinkOperator assLnk=null;
            assLnk=d.createLinkOnDiagram(LinkTypes.ASSOCIATION, clE1, clE2);

        assLnk.getPopup().pushMenu("Labels|Link Name");
        new EventTool().waitNoEvent(500);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        //
        for(int i=0;i<linkName18.length();i++) {
            drAr.typeKey(linkName18.charAt(i));
        }
        new EventTool().waitNoEvent(500);
        //
        assertTrue("BLOCKING: Text \""+tOp.getText()+"\" typed in link name textfield isn't correct, should be \""+linkName18+"\"",linkName18.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(1000);}catch(Exception ex){}
        String lncTxt=assLnk.getLabelsTexts()[0];
        assertTrue("BLOCKING: There is wrong \""+lncTxt+"\" label name in model, should be \""+linkName18+"\"",linkName18.equals(lncTxt));
        //
            assLnk.getLabel(linkName18).select();
            try{Thread.sleep(1000);}catch(Exception ee){}
            drAr.clickMouse(assLnk.getLabel(linkName18).getCenterPoint().x-10,assLnk.getLabel(linkName18).getCenterPoint().y,2);
        //try to find edit control
        ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        tOp=ec.getTextFieldOperator();
        //
        assertTrue("Text \""+tOp.getText()+"\" in link name textfield isn't correct, should be \""+linkName18+"\"",linkName18.equals(tOp.getText()));
        //
        drAr.pushKey(KeyEvent.VK_DELETE);
        //
        new EventTool().waitNoEvent(500);
        //
        assertTrue("Text \""+tOp.getText()+"\" typed in link name textfield isn't correct, should be empty.","".equals(tOp.getText()));
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        lncTxt=assLnk.getLabelsTexts()[0];
        assertTrue("There is wrong \""+lncTxt+"\" label name in model, should be \""+linkName18+"\"",linkName18.equals(lncTxt));
    }
    /**
     * @caseblock Class
     * @usecase Add multiple attributes to a class symbol using Insert
     */
    public void testMultipleAttributeInsertWithInsert() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg19, classDiagramName19);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className19,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        ///////refresh objects
            clE=new DiagramElementOperator(d,className19);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        ///////
        for(int i=0;i<4;i++) {
            CompartmentOperator attrCmp=atComp.getCompartments().get(0);
            attrCmp.clickOnCenter(1,InputEvent.BUTTON1_MASK);
            //delete name and default type int
            new EventTool().waitNoEvent(500);
            drAr.pushKey(KeyEvent.VK_INSERT);
            new EventTool().waitNoEvent(500);
            a=drAr.getFreePoint();
            if(i<3)drAr.clickMouse(a.x,a.y,1);
            else drAr.pushKey(KeyEvent.VK_ENTER);
            new EventTool().waitNoEvent(500);
        }
        //
        int good_count=0;
        for(int i=0;i<atComp.getCompartments().size();i++) {
            if(atComp.getCompartments().get(i).getName().equals(defaultAttributeVisibility+" "+defaultAttributeType+" "+defaultNewElementName))good_count++;
        }
        
        assertTrue("There shoul be 5 attributes, but exists only "+good_count,good_count==5);
    }
    /**
     * @caseblock Class
     * @usecase Add multiple operations to a class symbol using Insert
     */
    public void testMultipleOperationInsertWithInsert() {
         lastTestCase=getCurrentTestMethodName();
       DiagramOperator d = createOrOpenDiagram(project,workPkg20, classDiagramName20);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className20,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        ///////refresh objects
            clE=new DiagramElementOperator(d,className20);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        for(int i=0;i<4;i++) {
            CompartmentOperator oprCmp=opComp.getCompartments().get(0);
            oprCmp.clickOnCenter(1,InputEvent.BUTTON1_MASK);
            //
            new EventTool().waitNoEvent(500);
            drAr.pushKey(KeyEvent.VK_INSERT);
            new EventTool().waitNoEvent(500);
            a=drAr.getFreePoint();
            if(i<3)drAr.clickMouse(a.x,a.y,1);
            else drAr.pushKey(KeyEvent.VK_ENTER);
            new EventTool().waitNoEvent(500);
        }
        //
        int good_count=0;
        for(int i=0;i<opComp.getCompartments().size();i++) {
            if(opComp.getCompartments().get(i).getName().equals(defaultOperationVisibility+" "+defaultReturnType+"  "+defaultNewElementName+"(  )"))good_count++;
        }
        
        assertTrue("There shoul be 5 operations, but exists only "+good_count,good_count==5);
    }
    /**
     * @caseblock Class
     * @usecase Delete multiple attributes in a class symbol using Delete
     */
    public void testDeleteMultipleAttributes() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg21, classDiagramName21);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className21,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        ///////
        //
        String OUT_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_out.txt";
        String ERR_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_err.txt";
        TestOut defTestOut=JemmyProperties.getCurrentOutput();
        try
        {
            PrintStream myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
            PrintStream myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
            JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        }
        catch(Exception ex)
        {
            
        }
        for(int iA=0;iA<5;iA++) {
            atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
            new EventTool().waitNoEvent(500);
            org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility21[iA],attributeType21[iA],attributeName21[iA],attributeDefValue21[iA],true);
            new EventTool().waitNoEvent(500);
        }
        JemmyProperties.setCurrentOutput(defTestOut);
        try
        {
            BufferedReader myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
            String line;
            do {
                line = myIn.readLine();
                if (line!=null && line.indexOf("java.lang.NullPointerException")>-1){
                    line=myIn.readLine();
                    if(line!=null && line.indexOf("at com.embarcadero.uml.core.support.umlutils.PropertyElementManager.processEnumeration(PropertyElementManager.java")>-1)
                    {
                        fail("NPE on addition attribute with initial value");
                    }
                    else
                    {
                        fail("Unknown NPE in log: "+line);
                    }
                }
                else if (line!=null && line.indexOf("Exception")>-1)
                {
                    fail("Unexpected exception in log: "+line);
                }
            } while (line != null);
        }
        catch(Exception ex)
        {
            
        }
        //
        int good_count=0;
        String f="";
        for(int i=0;i<atComp.getCompartments().size();i++) {
            if(atComp.getCompartments().get(i).getName().equals(atStr21[i]))good_count++;
            f=f+atComp.getCompartments().get(i).getName()+"\n"+atStr21[i]+"\n-------------------------------------------------------\n";
        }
        assertTrue("There should be 5 attributes, but only "+good_count+" are correct or exists:\n"+f,good_count==5);
        //select
        atComp.getCompartments().get(0).clickOnLeftCenter(1,InputEvent.BUTTON1_MASK);
        atComp.getCompartments().get(1).clickOnLeftCenter(1,InputEvent.BUTTON1_MASK,InputEvent.CTRL_MASK);
        atComp.getCompartments().get(3).clickOnLeftCenter(1,InputEvent.BUTTON1_MASK,InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(500);
        drAr.pushKey(KeyEvent.VK_DELETE);
        new EventTool().waitNoEvent(500);
        new JDialogOperator("Delete");
        new JButtonOperator(new JDialogOperator("Delete"),"Yes").pushNoBlock();
        new JDialogOperator("Delete").waitClosed();
        new EventTool().waitNoEvent(500);
        new JButtonOperator(new JDialogOperator("Delete Associated Methods"),"Yes").pushNoBlock();
        new JDialogOperator("Delete Associated Methods").waitClosed();
        new EventTool().waitNoEvent(500);
        //
        assertTrue("There should be only 2 attributes, but there are "+atComp.getCompartments().size(),atComp.getCompartments().size()==2);
        //
        good_count=0;
        if(atComp.getCompartments().get(0).getName().equals(atStr21[2]))good_count++;
        if(atComp.getCompartments().get(1).getName().equals(atStr21[4]))good_count++;
        assertTrue((2-good_count)+" of remained attributes are broken.",good_count==2);
    }
    /**
     * @caseblock Class
     * @usecase Delete multiple operations in a class symbol using Delete
     */
    public void testDeleteMultipleOperations() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg22, classDiagramName22);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className22,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        for(int iA=0;iA<5;iA++) {
            opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
            new EventTool().waitNoEvent(500);
            org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility22[iA], operationRetType22[iA], operationName22[iA], operationParameters22[iA],true);
            new EventTool().waitNoEvent(500);
        }
        //
        int good_count=0;
        String f="";
        for(int i=0,opN=0;i<opComp.getCompartments().size();i++) {
            String tmp=opComp.getCompartments().get(i).getName();
            
            if(tmp.indexOf("public "+className22+"(")==-1 && tmp.indexOf("void finalize(")==-1) {
                if(tmp.equals(opStr22[opN]))good_count++;
                f=f+tmp+"\n"+opStr22[opN]+"\n-------------------------------------------------------\n";
                opN++;
            }
            
        }
        assertTrue("There should be 5 operations, but only "+good_count+" are correct or exists:\n"+f,good_count==5);
        //select
        for(int i=0,opN=0,mask=0;i<opComp.getCompartments().size();i++) {
            String tmp=opComp.getCompartments().get(i).getName();
            
            if(tmp.indexOf("public "+className22+"(")==-1 && tmp.indexOf("void finalize(")==-1) {
                
                if(opN==0 || opN==1 || opN==3)opComp.getCompartments().get(i).clickOnLeftCenter(1,InputEvent.BUTTON1_MASK,mask);
                opN++;
                mask=InputEvent.CTRL_MASK;
                new EventTool().waitNoEvent(500);
            }
        }
        new EventTool().waitNoEvent(500);
        drAr.pushKey(KeyEvent.VK_DELETE);
        new EventTool().waitNoEvent(500);
        new JDialogOperator("Delete");
        new JButtonOperator(new JDialogOperator("Delete"),"Yes").pushNoBlock();
        new JDialogOperator("Delete").waitClosed();
        new EventTool().waitNoEvent(500);
        //
        good_count=0;
        for(int i=0,opN=0;i<opComp.getCompartments().size() && opN<4;i++) {
            String tmp=opComp.getCompartments().get(i).getName();
            
            if(tmp.indexOf("public "+className22+"(")==-1 && tmp.indexOf("void finalize(")==-1) {
                good_count++;
            }
            
        }
        assertTrue("There should be only 2 operations, but there are "+good_count,good_count==2);
        //
        good_count=0;f="";
        //
        for(int i=0,opN=2;i<opComp.getCompartments().size();i++) {
            String tmp=opComp.getCompartments().get(i).getName();
            
            if(tmp.indexOf("public "+className22+"(")==-1 && tmp.indexOf("void finalize(")==-1) {
                if(tmp.equals(opStr22[opN]))good_count++;
                f=f+tmp+"\n"+opStr22[opN]+"\n-------------------------------------------------------\n";
                opN=opN+2;
            }
            
        }
        assertTrue((2-good_count)+" of remained operaions are broken:"+f,good_count==2);
    }
    /**
     * @caseblock Class
     * @usecase Change attribute visibility from private to public
     */
    public void testChangeAttributeVisibility() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg23, classDiagramName23);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
        new EventTool().waitNoEvent(500);
            clE=d.putElementOnDiagram(className23,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility23_1,attributeType23,attributeName23,attributeDefValue23);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        //
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr23_1+"\"",atStr23_1.equals(modAt));
        
        ///////refresh objects
            clE=new DiagramElementOperator(d,className23);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+atStr23_1+"\"",atStr23_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility23_2,attributeType23,attributeName23,attributeDefValue23);
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+atStr23_2+"\"",atStr23_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        modAt=attrCmp.getName();
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr23_2+"\"",atStr23_2.equals(modAt));
     }
    /**
     * @caseblock Class
     * @usecase Change attribute type
     */
   public void testChangeAttributeType() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg24, classDiagramName24);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
        new EventTool().waitNoEvent(500);
            clE=d.putElementOnDiagram(className24,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility24,attributeType24_1,attributeName24,attributeDefValue24);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        //
       assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr24_1+"\"",atStr24_1.equals(modAt));
        
        ///////refresh objects
            clE=new DiagramElementOperator(d,className24);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+atStr24_1+"\"",atStr24_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility24,attributeType24_2,attributeName24,attributeDefValue24);
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+atStr24_2+"\"",atStr24_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        modAt=attrCmp.getName();
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr24_2+"\"",atStr24_2.equals(modAt));
    }
    /**
     * @caseblock Class
     * @usecase Change attribute initial value
     */
   public void testChangeAttributeInitialValue() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg25, classDiagramName25);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
        new EventTool().waitNoEvent(500);
            clE=d.putElementOnDiagram(className25,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator atComp=null;
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        atComp.getPopup().pushMenu(PopupConstants.INSERT_ATTRIBUTE);
        //delete name and default type int
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility25,attributeType25,attributeName25,attributeDefValue25_1);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //attribute in model
        String modAt=atComp.getCompartments().get(0).getName();
        //
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr25_1+"\"",atStr25_1.equals(modAt));
        
        ///////refresh objects
            clE=new DiagramElementOperator(d,className25);
            atComp=new CompartmentOperator(clE,CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        ///////
        CompartmentOperator attrCmp=atComp.getCompartments().get(0);
        attrCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+atStr25_1+"\"",atStr25_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.attributeNaturalWayNaming(attributeVisibility25,attributeType25,attributeName25,attributeDefValue25_2);
        assertTrue("Attribute \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+atStr25_2+"\"",atStr25_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        modAt=attrCmp.getName();
        assertTrue("Attribute \""+modAt+"\" in model isn't correctly propagated,should be \""+atStr25_2+"\"",atStr25_2.equals(modAt));
    }
    /**
     * @caseblock Class
     * @usecase Change operation visibility from public to private
     */
    public void testChangeOperationVisibility() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg26, classDiagramName26);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className26,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility26_1, operationRetType26, operationName26, parameterType26,parameterName26);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className26);
        //
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr26_1+"\"",opStr26_1.equals(operInModel));
        ///////refresh objects
            clE=new DiagramElementOperator(d,className26);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        CompartmentOperator oprCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className26);
        oprCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
//         opComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+opStr26_1+"\"",opStr26_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility26_2, operationRetType26, operationName26, parameterType26,parameterName26);
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+opStr26_2+"\"",opStr26_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className26);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr26_2+"\"",opStr26_2.equals(operInModel));
    }
    /**
     * @caseblock Class
     * @usecase Change operation parameters
     */
    public void testChangeOperationParameters() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg27, classDiagramName27);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className27,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility27, operationRetType27, operationName27, operationParameters27_1);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className27);
        //
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr27_1+"\"",opStr27_1.equals(operInModel));
        ///////refresh objects
            clE=new DiagramElementOperator(d,className27);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        CompartmentOperator oprCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className27);
        oprCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
         // opComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+opStr27_1+"\"",opStr27_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility27, operationRetType27, operationName27, operationParameters27_2);
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+opStr27_2+"\"",opStr27_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className27);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr27_2+"\"",opStr27_2.equals(operInModel));
    }
    /**
     * @caseblock Class
     * @usecase Change operation return type
     */
    public void testChangeOperationReturntype() {
        lastTestCase=getCurrentTestMethodName();
        DiagramOperator d = createOrOpenDiagram(project,workPkg28, classDiagramName28);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        DiagramElementOperator clE=null;
            clE=d.putElementOnDiagram(className28,ElementTypes.CLASS);
        new EventTool().waitNoEvent(500);
        CompartmentOperator opComp=null;
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        opComp.getPopup().pushMenu(PopupConstants.INSERT_OPERATION);
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility28, operationRetType28_1, operationName28, parameterType28,parameterName28);
        //
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        //try to find first not constructor and finalizer operation
        String operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className28);
        //
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr28_1+"\"",opStr28_1.equals(operInModel));
        ///////refresh objects
            clE=new DiagramElementOperator(d,className28);
            opComp=new CompartmentOperator(clE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        ///////
        CompartmentOperator oprCmp=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationCmp(opComp,className28);
        oprCmp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
         // opComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //--
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        //
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly displayed,should be \""+opStr28_1+"\"",opStr28_1.equals(tOp.getText()));
        //-replace name
        org.netbeans.test.uml.editcontrol.utils.Utils.operationNaturalWayNaming(operationVisibility28, operationRetType28_2, operationName28, parameterType28,parameterName28);
        assertTrue("Operation \""+tOp.getText()+"\" in edit control isn't correctly updated,should be \""+opStr28_2+"\"",opStr28_2.equals(tOp.getText()));
        //
        a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        operInModel=org.netbeans.test.uml.editcontrol.utils.Utils.getNotConstructorFinalizerOperationStr(opComp,className28);
        assertTrue("Operation \""+operInModel+"\" in model isn't correctly propagated,should be \""+opStr28_2+"\"",opStr28_2.equals(operInModel));
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

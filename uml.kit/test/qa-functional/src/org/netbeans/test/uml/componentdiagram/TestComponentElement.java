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


package org.netbeans.test.uml.componentdiagram;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
//import qa.uml.UMLClassOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LibProperties;



/**
 *
 * @author psb
 * @spec uml/UML-EditControl.xml
 */
public class TestComponentElement extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "componentDiagramProjectTC";
    private static String project = prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private static String defaultNewElementName=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private String lastTestCase=null;
    //--
    private static String componentDiagramName1 = "cpD1";
    private static String workPkg1 = "pkg1";
    private static String element1="Generalization";
    private static String treeNode1=element1;
    private static String elementName1="";
    private static LinkTypes elementType1=LinkTypes.GENERALIZATION;
    private static ElementTypes fromType1=ElementTypes.CLASS;
    private static ElementTypes toType1=ElementTypes.CLASS;
    private static boolean eIO1=false;
    //--
    private static String componentDiagramName2 = "cpD2";
    private static String workPkg2 = "pkg2";
    private static String element2="Implementation";
    private static String treeNode2=element2;
    private static String elementName2="";
    private static LinkTypes elementType2=LinkTypes.IMPLEMENTATION;
    private static ElementTypes fromType2=ElementTypes.CLASS;
    private static ElementTypes toType2=ElementTypes.INTERFACE;
    private static boolean eIO2=false;
   //--
    //SPECIAL CASE
    private static String componentDiagramName3 = "cpD3";
    private static String workPkg3 = "pkg3";
    private static String element3="Nested Link";
    private static String treeNode3=element3;
    private static String elementName3=defaultNewElementName;
    private static LinkTypes elementType3=LinkTypes.NESTED_LINK;
    private static ElementTypes fromType3=ElementTypes.CLASS;
    private static ElementTypes toType3=ElementTypes.CLASS;
    private static boolean eIO3=false;
   //--
    private static String componentDiagramName4 = "cpD4";
    private static String workPkg4 = "pkg4";
    private static String element4="Delegate";
    private static String treeNode4=element4;
    private static String elementName4="";
    private static LinkTypes elementType4=LinkTypes.DELEGATE;
    private static ElementTypes fromType4=ElementTypes.CLASS;
    private static ElementTypes toType4=ElementTypes.COMPONENT;
    private static boolean eIO4=false;
   //--
    //SPECIAL CASE
    /*private static String componentDiagramName5 = "cpD5";
    private static String workPkg5 = "pkg5";
    private static String element5="Assembly Connector";
    private static String treeNode5=element5;
    private static String elementName5="";
    private static LinkTypes elementType5=LinkTypes.USAGE;
    private static ElementTypes fromType5=ElementTypes.COMPONENT;
    private static ElementTypes toType5=ElementTypes.INTERFACE;
    private static boolean eIO5=false;*/
    //--
    private static String componentDiagramName6 = "cpD6";
    private static String workPkg6 = "pkg6";
    private static String element6="Derivation Edge";
    private static String treeNode6=element6;
    private static String elementName6="";
    private static LinkTypes elementType6=LinkTypes.DERIVATION_EDGE;
    private static ElementTypes fromType6=ElementTypes.DERIVATION_CLASSIFIER;
    private static ElementTypes toType6=ElementTypes.TEMPLATE_CLASS;
    private static boolean eIO6=false;
    //--
    private static String componentDiagramName7 = "cpD7";
    private static String workPkg7 = "pkg7";
    private static String element7="Association";
    private static String treeNode7=element7;
    private static String elementName7="";
    private static LinkTypes elementType7=LinkTypes.ASSOCIATION;
    private static ElementTypes fromType7=ElementTypes.COMPONENT;
    private static ElementTypes toType7=ElementTypes.CLASS;
    private static boolean eIO7=false;
    //--
    private static String componentDiagramName8 = "cpD8";
    private static String workPkg8 = "pkg8";
    private static String element8="Composition";
    private static String treeNode8=element8;
    private static String elementName8="";
    private static LinkTypes elementType8=LinkTypes.COMPOSITION;
    private static ElementTypes fromType8=ElementTypes.COMPONENT;
    private static ElementTypes toType8=ElementTypes.CLASS;
    private static boolean eIO8=false;
   //--
    private static String componentDiagramName9 = "cpD9";
    private static String workPkg9 = "pkg9";
    private static String element9="Navigable Composition";
    private static String treeNode9="Composition";
    private static String elementName9="";
    private static LinkTypes elementType9=LinkTypes.NAVIGABLE_COMPOSITION;
    private static ElementTypes fromType9=ElementTypes.COMPONENT;
    private static ElementTypes toType9=ElementTypes.CLASS;
    private static boolean eIO9=false;
   //--
    private static String componentDiagramName10 = "cpD10";
    private static String workPkg10 = "pkg10";
    private static String element10="Aggregation";
    private static String treeNode10=element10;
    private static String elementName10="";
    private static LinkTypes elementType10=LinkTypes.AGGREGATION;
    private static ElementTypes fromType10=ElementTypes.COMPONENT;
    private static ElementTypes toType10=ElementTypes.CLASS;
    private static boolean eIO10=false;
   //--
    private static String componentDiagramName11 = "cpD11";
    private static String workPkg11 = "pkg11";
    private static String element11="Navigable Aggregation";
    private static String treeNode11=element11;
    private static String elementName11="";
    private static LinkTypes elementType11=LinkTypes.NAVIGABLE_AGGREGATION;
    private static ElementTypes fromType11=ElementTypes.COMPONENT;
    private static ElementTypes toType11=ElementTypes.CLASS;
    private static boolean eIO11=false;
   //--
    private static String componentDiagramName12 = "cpD12";
    private static String workPkg12 = "pkg12";
    private static String element12="Navigable Association";
    private static String treeNode12=element12;
    private static String elementName12="";
    private static LinkTypes elementType12=LinkTypes.NAVIGABLE_ASSOCIATION;
    private static ElementTypes fromType12=ElementTypes.COMPONENT;
    private static ElementTypes toType12=ElementTypes.CLASS;
    private static boolean eIO12=false;
   //--
    //SPECIAL CASE
    /*private static String componentDiagramName13 = "cpD13";
    private static String workPkg13 = "pkg13";
    private static String element13="Association Class";
    private static String treeNode13=element13;
    private static String elementName13="";
    private static LinkTypes elementType13=LinkTypes.ASSOCIATION_CLASS;
    private static ElementTypes fromType13=ElementTypes.COMPONENT;
    private static ElementTypes toType13=ElementTypes.CLASS;
    private static boolean eIO13=false;*/
   //--
    //Special case
   /* private static String componentDiagramName14 = "cpD14";
    private static String workPkg14 = "pkg14";
    private static String element14="Role Binding";
    private static String treeNode14=element14;
    private static String elementName14=defaultNewElementName;
    private static LinkTypes elementType14=LinkTypes.ROLE_BINDING;
    private static ElementTypes fromType14=ElementTypes.DESIGN_PATTERN;
    private static ElementTypes toType14=ElementTypes.ACTOR_ROLE;
    private static boolean eIO14=false;*/
   //--
    //Special case
    /*private static String componentDiagramName15 = "cpD15";
    private static String workPkg15 = "pkg15";
    private static String element15="Link Comment";
    private static String treeNode15=element15;
    private static String elementName15="";
    private static LinkTypes elementType15=LinkTypes.LINK_COMMENT;
    private static ElementTypes fromType14=ElementTypes.CLASS;
    private static ElementTypes toType14=ElementTypes.COMMENT;
    private static boolean eIO14=false;*/


    
    /** Need to be defined because of JUnit */
    public TestComponentElement(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.componentdiagram.TestComponentElement.class);
        return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createOrOpenDiagram(project,workPkg,diagram,NewDiagramWizardOperator.COMPONENT_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    


  public void testCreateAssemblyConnector() {
        lastTestCase=getCurrentTestMethodName();
       //
        DiagramOperator d = createOrOpenDiagram(project,workPkg1, "cpD_1");
        JComboBoxOperator zoomCombo=new JComboBoxOperator(d);
        zoomCombo.waitComponentShowing(true);
        try {
            zoomCombo.waitComponentEnabled();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        zoomCombo.selectItem("200%");
         new EventTool().waitNoEvent(500);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        int numChild=pkgNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        DrawingAreaOperator drAr=d.getDrawingArea();
       //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dECmp=d.putElementOnDiagram("MainCompnent1",ElementTypes.COMPONENT,a.x,a.y);
        Node cmpNode=new Node(pkgNode,"MainCompnent1");
        pl.selectTool(LibProperties.getCurrentToolName(ElementTypes.ASSEMBLY_CONNECTOR));
        drAr.clickMouse(dECmp.getCenterPoint().x,dECmp.getCenterPoint().y+10,1);
        a=drAr.getFreePoint(150);
        drAr.clickMouse(a.x,a.y,1);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //try to find interface
        DiagramElementOperator dE2=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE),ElementTypes.INTERFACE);
        Node intNode=new Node(pkgNode,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE));
        //check port
        DiagramElementOperator dE3=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.PORT),ElementTypes.PORT);
        Node portNode=new Node(cmpNode,ElementTypes.PORT.toString());
        dE3.select();
        PropertySheetOperator pso=new PropertySheetOperator(ElementTypes.PORT+" - Properties");
        //link
        LinkOperator usagelink=new LinkOperator(dE3,dE2,LinkTypes.USAGE);
        //relations
        Node tmp1=new Node(intNode,"Relationships");
        Node tmp2=new Node(tmp1,LibProperties.getCurrentToolName(LinkTypes.USAGE));
        Node tmp3=new Node(tmp2,ElementTypes.PORT.toString());
        //-
        tmp1=new Node(portNode,"Required Interfaces");
        tmp2=new Node(tmp1,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE));
    }

  public void testMUltiLinkAndPopup() {
        lastTestCase=getCurrentTestMethodName();
       //
        DiagramOperator d = createOrOpenDiagram(project,workPkg2, "cpD_2");
        new JComboBoxOperator(d).selectItem("200%");
         new EventTool().waitNoEvent(500);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        int numChild=pkgNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        DrawingAreaOperator drAr=d.getDrawingArea();
       //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dECmp=d.putElementOnDiagram("MainCompnent1",ElementTypes.COMPONENT,a.x,a.y);
        Node cmpNode=new Node(pkgNode,"MainCompnent1");
        pl.selectTool(LibProperties.getCurrentToolName(ElementTypes.ASSEMBLY_CONNECTOR));
        drAr.clickMouse(dECmp.getCenterPoint().x,dECmp.getCenterPoint().y+10,1);
        a=drAr.getFreePoint(150);
        drAr.clickMouse(a.x,a.y,1);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //try to find interface
        DiagramElementOperator dE2=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE),ElementTypes.INTERFACE);
        Node intNode=new Node(pkgNode,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE));
        intNode.select();
        PropertySheetOperator pso=new PropertySheetOperator(LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE)+" - Properties");
        org.netbeans.test.uml.componentdiagram.utils.Utils.setTextProperty("Name", "INT1");
        //check ports
        DiagramElementOperator dE3=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.PORT),ElementTypes.PORT);
        Node portNode=new Node(cmpNode,ElementTypes.PORT.toString());
        dE3.select();
        pso=new PropertySheetOperator(ElementTypes.PORT+" - Properties");
        org.netbeans.test.uml.componentdiagram.utils.Utils.setTextProperty("Name", "PORT1");
        portNode=new Node(cmpNode,"PORT1");
        dE3=new DiagramElementOperator(d,"PORT1",ElementTypes.PORT);
        //2
        pl.selectTool(LibProperties.getCurrentToolName(ElementTypes.ASSEMBLY_CONNECTOR));
        drAr.clickMouse(dECmp.getCenterPoint().x+20,dECmp.getCenterPoint().y+10,1);
        a=drAr.getFreePoint(150);
        drAr.clickMouse(a.x,a.y,1);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //try to find interface
        DiagramElementOperator dE22=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE),ElementTypes.INTERFACE);
        Node intNode2=new Node(pkgNode,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE));
        intNode2.select();
        pso=new PropertySheetOperator(LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE)+" - Properties");
        org.netbeans.test.uml.componentdiagram.utils.Utils.setTextProperty("Name", "INT2");
        dE3=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.PORT),ElementTypes.PORT);
        portNode=new Node(cmpNode,ElementTypes.PORT.toString());
        dE3.select();
        pso=new PropertySheetOperator(ElementTypes.PORT+" - Properties");
        org.netbeans.test.uml.componentdiagram.utils.Utils.setTextProperty("Name", "PORT2");
        //link
        dE3=new DiagramElementOperator(d,"PORT1",ElementTypes.PORT);
        dE2=new DiagramElementOperator(d,"INT1");
        LinkOperator usagelink=new LinkOperator(dE3,dE2,LinkTypes.USAGE);
       dE3=new DiagramElementOperator(d,"PORT2",ElementTypes.PORT);
        dE2=new DiagramElementOperator(d,"INT2");
        usagelink=new LinkOperator(dE3,dE2,LinkTypes.USAGE);
        //relations
        intNode=new Node(pkgNode,"INT1");
        Node tmp1=new Node(intNode,"Relationships");
        Node tmp2=new Node(tmp1,LibProperties.getCurrentToolName(LinkTypes.USAGE));
        Node tmp3=new Node(tmp2,"PORT1");
        //-
        portNode=new Node(cmpNode,"PORT1");
        tmp1=new Node(portNode,"Required Interfaces");
        tmp2=new Node(tmp1,"INT1");
        //
        intNode=new Node(pkgNode,"INT2");
        tmp1=new Node(intNode,"Relationships");
        tmp2=new Node(tmp1,LibProperties.getCurrentToolName(LinkTypes.USAGE));
        tmp3=new Node(tmp2,"PORT2");
        //-
        portNode=new Node(cmpNode,"PORT2");
        tmp1=new Node(portNode,"Required Interfaces");
        tmp2=new Node(tmp1,"INT2");
        //popup
        JPopupMenuOperator popM=dECmp.getPopup();
        popM.waitComponentShowing(true);
        JMenuItemOperator miOp=new JMenuItemOperator(popM.pushMenu("Ports"));
        miOp.waitSelected(true);
        JPopupMenu chM=(javax.swing.JPopupMenu)(miOp.getSubElements()[0]);
        assertTrue("There is not 2 ports in Ports submenu",chM.getSubElements().length==2);
        assertTrue("There is no PORT1 in Ports submenu",((JMenuItem)(chM.getSubElements()[0])).getText().equals("PORT1"));
        assertTrue("There is no PORT2 in Ports submenu",((JMenuItem)(chM.getSubElements()[1])).getText().equals("PORT2"));
        popM.pushKey(KeyEvent.VK_ESCAPE);
        popM.waitComponentVisible(false);
        popM=dECmp.getPopup();
        popM.waitComponentShowing(true);
        miOp=new JMenuItemOperator(popM.pushMenu("Port Location"));
        chM=(javax.swing.JPopupMenu)(miOp.getSubElements()[0]);
        assertTrue("There is not 4 positions in Port Location submenu",chM.getSubElements().length==4);
        assertTrue("There is no Top in Port Location submenu",((JMenuItem)(chM.getSubElements()[0])).getText().equals("Top"));
        assertTrue("There is no Bottom in Port Location submenu",((JMenuItem)(chM.getSubElements()[1])).getText().equals("Bottom"));
        assertTrue("There is no Left in Port Location submenu",((JMenuItem)(chM.getSubElements()[2])).getText().equals("Left"));
        assertTrue("There is no Right in Port Location submenu",((JMenuItem)(chM.getSubElements()[3])).getText().equals("Right"));
        popM.pushKey(KeyEvent.VK_ESCAPE);
        popM.waitComponentVisible(false);
        dECmp.getPopup().pushMenu("Evenly Distribute Port Interfaces");
      }

  public void testRemoveAssemblyConnectorDel() {
        lastTestCase=getCurrentTestMethodName();
       //
        DiagramOperator d = createOrOpenDiagram(project,workPkg3, "cpD_3");
        new JComboBoxOperator(d).selectItem("200%");
         new EventTool().waitNoEvent(500);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        int numChild=pkgNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        DrawingAreaOperator drAr=d.getDrawingArea();
       //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dECmp=d.putElementOnDiagram("MainCompnent1",ElementTypes.COMPONENT,a.x,a.y);
        Node cmpNode=new Node(pkgNode,"MainCompnent1");
        pl.selectTool(LibProperties.getCurrentToolName(ElementTypes.ASSEMBLY_CONNECTOR));
        drAr.clickMouse(dECmp.getCenterPoint().x,dECmp.getCenterPoint().y+10,1);
        a=drAr.getFreePoint(150);
        drAr.clickMouse(a.x,a.y,1);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //try to find interface
        DiagramElementOperator dE2=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE),ElementTypes.INTERFACE);
        DiagramElementOperator dE3=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.PORT),ElementTypes.PORT);
        Node portNode=new Node(cmpNode,ElementTypes.PORT.toString());
        LinkOperator usagelink=new LinkOperator(dE3,dE2,LinkTypes.USAGE);
        usagelink.select();
        //invoke Delete key
        drAr.pushKey(KeyEvent.VK_DELETE);
        JDialogOperator delDlg=new JDialogOperator("Delete");
        try{Thread.sleep(1000);}catch(Exception ex){}
        delDlg.waitComponentShowing(true);
        JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
        chk.waitComponentShowing(true);
        if(!chk.isSelected())chk.clickMouse();
        chk.waitSelected(true);
        new JButtonOperator(delDlg,"Yes").pushNoBlock();
        dE2=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE),ElementTypes.INTERFACE);
        Node intNode=new Node(pkgNode,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE));
        //check port
        dE3=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.PORT),ElementTypes.PORT);
        portNode=new Node(cmpNode,ElementTypes.PORT.toString());
        dE3.select();
        PropertySheetOperator pso=new PropertySheetOperator(ElementTypes.PORT+" - Properties");
        //link
        try
        {
        usagelink=new LinkOperator(dE3,dE2,LinkTypes.USAGE);
        fail("Link Remains on diagram");
        }
        catch(Exception e){//good
        
        }
        //relations
        Node tmp1=null;
        if(intNode.isChildPresent("Relationships"))tmp1=new Node(intNode,"Relationships");
        Node tmp2=null;
        assertFalse("Usage link remains within interface node",tmp1!=null && tmp1.isChildPresent(LibProperties.getCurrentToolName(LinkTypes.USAGE)));
        //-
        tmp1=null;
        if(portNode.isChildPresent("Required Interfaces"))tmp1=new Node(portNode,"Required Interfaces");
        assertFalse(78854,"Required Unnamed interface remains within Port node",tmp1!=null && tmp1.isChildPresent(LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE)));
    }
 
  public void testRemoveAssemblyConnectorContext() {
        lastTestCase=getCurrentTestMethodName();
       //
        DiagramOperator d = createOrOpenDiagram(project,workPkg4, "cpD_4");
        new JComboBoxOperator(d).selectItem("200%");
         new EventTool().waitNoEvent(500);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        int numChild=pkgNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        DrawingAreaOperator drAr=d.getDrawingArea();
       //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dECmp=d.putElementOnDiagram("MainCompnent1",ElementTypes.COMPONENT,a.x,a.y);
        Node cmpNode=new Node(pkgNode,"MainCompnent1");
        pl.selectTool(LibProperties.getCurrentToolName(ElementTypes.ASSEMBLY_CONNECTOR));
        drAr.clickMouse(dECmp.getCenterPoint().x,dECmp.getCenterPoint().y+10,1);
        a=drAr.getFreePoint(150);
        drAr.clickMouse(a.x,a.y,1);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //try to find interface
        DiagramElementOperator dE2=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE),ElementTypes.INTERFACE);
        dE2=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE),ElementTypes.INTERFACE);
        Node intNode=new Node(pkgNode,LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE));
        //check port
        DiagramElementOperator dE3=new DiagramElementOperator(d,LibProperties.getCurrentDefaultName(ElementTypes.PORT),ElementTypes.PORT);
        Node portNode=new Node(cmpNode,ElementTypes.PORT.toString());
        //dE3.select();
        java.awt.Point tmpP=dE3.getCenterPoint();
        tmpP.translate(0,-3);
        dE3.clickOn(tmpP,1,InputEvent.BUTTON1_MASK,0);
        dE3.waitSelection(true);
        PropertySheetOperator pso=new PropertySheetOperator(ElementTypes.PORT+" - Properties");
        LinkOperator usagelink=new LinkOperator(dE3,dE2,LinkTypes.USAGE);
        //invoke Delete key
        JPopupMenuOperator popM=usagelink.getPopup();
//        try{Thread.sleep(5000);}catch(Exception ex){}
        popM.showMenuItem("Edit|Delete").pushNoBlock();
        JDialogOperator delDlg=new JDialogOperator("Delete");
        try{Thread.sleep(1000);}catch(Exception ex){}
        delDlg.waitComponentShowing(true);
        JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
        chk.waitComponentShowing(true);
        if(!chk.isSelected())chk.clickMouse();
        chk.waitSelected(true);
        new JButtonOperator(delDlg,"Yes").pushNoBlock();
        delDlg.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        //link
        try
        {
        usagelink=new LinkOperator(dE3,dE2,LinkTypes.USAGE);
        fail("Link Remains on diagram");
        }
        catch(Exception e){//good
        
        }
        //relations
        Node tmp1=null;
        if(intNode.isChildPresent("Relationships"))tmp1=new Node(intNode,"Relationships");
        Node tmp2=null;
        assertFalse("Usage link remains within interface node",tmp1!=null && tmp1.isChildPresent(LibProperties.getCurrentToolName(LinkTypes.USAGE)));
        //-
        tmp1=null;
        if(portNode.isChildPresent("Required Interfaces"))tmp1=new Node(portNode,"Required Interfaces");
        assertFalse(78854,"Required Unnamed interface remains within Port node",tmp1!=null && tmp1.isChildPresent(LibProperties.getCurrentDefaultName(ElementTypes.INTERFACE)));
    }
      
   public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            org.netbeans.test.uml.componentdiagram.utils.Utils.commonComponentDiagramSetup(workdir,prName);
            //
            codeSync=true;
        }
        
    }
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        if(lastDiagramNode!=null)
        {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
        }
        try{
            DiagramOperator d=new DiagramOperator("cpD");
            d.closeAllDocuments();
            d.waitClosed();
            new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }

    

    

}

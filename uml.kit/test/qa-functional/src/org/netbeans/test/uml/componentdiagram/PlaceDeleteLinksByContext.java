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

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
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
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;



/**
 *
 * @author psb
 * @spec uml/UML-EditControl.xml
 */
public class PlaceDeleteLinksByContext extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "componentDiagramProjectLDC";
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
   /* private static String componentDiagramName3 = "cpD3";
    private static String workPkg3 = "pkg3";
    private static String element3="Nested Link";
    private static String treeNode3=element3;
    private static String elementName3=defaultNewElementName;
    private static LinkTypes elementType3=LinkTypes.NESTED_LINK;
    private static ElementTypes fromType3=ElementTypes.CLASS;
    private static ElementTypes toType3=ElementTypes.CLASS;
    private static boolean eIO3=false;*/
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
    public PlaceDeleteLinksByContext(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.componentdiagram.PlaceDeleteLinksByContext.class);
        return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createOrOpenDiagram(project,workPkg,diagram,org.netbeans.test.umllib.NewDiagramWizardOperator.COMPONENT_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    


  public void testDeleteLinkByContext1() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg1,componentDiagramName1, fromType1, toType1,element1,elementName1,elementType1,treeNode1,eIO1);
    }
  public void testDeleteLinkByContext2() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg2,componentDiagramName2, fromType2, toType2,element2,elementName2,elementType2,treeNode2,eIO2);
    }
 /* public void testDeleteLinkByContext3() {
        testLinkPlaceDelete(workPkg3,componentDiagramName3, fromType3, toType3,element3,elementName3,elementType3,treeNode3,eIO3);
    }*/
  public void testDeleteLinkByContext4() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg4,componentDiagramName4, fromType4, toType4,element4,elementName4,elementType4,treeNode4,eIO4);
    }
  /*public void testDeleteLinkByContext5() {
        testLinkPlaceDelete(workPkg5,componentDiagramName5, fromType5, toType5,element5,elementName5,elementType5,treeNode5,eIO5);
    }*/
  public void testDeleteLinkByContext6() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg6,componentDiagramName6, fromType6, toType6,element6,elementName6,elementType6,treeNode6,eIO6);
    }
  public void testDeleteLinkByContext7() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg7,componentDiagramName7, fromType7, toType7,element7,elementName7,elementType7,treeNode7,eIO7);
    }
  public void testDeleteLinkByContext8() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg8,componentDiagramName8, fromType8, toType8,element8,elementName8,elementType8,treeNode8,eIO8);
    }
  public void testDeleteLinkByContext9() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg9,componentDiagramName9, fromType9, toType9,element9,elementName9,elementType9,treeNode1,eIO9);
    }
  public void testDeleteLinkByContext10() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg10,componentDiagramName10, fromType10, toType10,element10,elementName10,elementType10,treeNode1,eIO10);
    }
  public void testDeleteLinkByContext11() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg11,componentDiagramName11, fromType11, toType11,element11,elementName11,elementType11,treeNode1,eIO11);
    }
  public void testDeleteLinkByContext12() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg12,componentDiagramName12, fromType12, toType12,element12,elementName12,elementType12,treeNode12,eIO12);
    }
  /*public void testDeleteLinkByContext13() {
        testLinkPlaceDelete(workPkg13,componentDiagramName13, fromType13, toType13,element13,elementName13,elementType13,treeNode13,eIO13);
    }*/
  /*public void testDeleteLinkByContext14() {
        testLinkPlaceDelete(workPkg14,componentDiagramName14, fromType14, toType14,element14,elementName14,elementType14,treeNode14,eIO14);
    }*/
  /*public void testDeleteLinkByContext15() {
        testLinkPlaceDelete(workPkg15,componentDiagramName15, fromType15, toType15,element15,elementName15,elementType15,treeNode15,eIO15);
    }*/

 
      
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
        log("LAST_CASE:"+lastTestCase);
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
    

    
   private void testLinkPlaceDelete(String workPkg,String diagramName,ElementTypes fromType,ElementTypes toType,String element,String elementName,LinkTypes elementType,String treeNode,boolean useIO)
   {
       String localElName1="El1";
       String localElName2="El2";
       String newName="LinkName";
       //
        DiagramOperator d = createOrOpenDiagram(project,workPkg, diagramName);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        int numChild=pkgNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
       DrawingAreaOperator drAr=d.getDrawingArea();
       //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dE1=null,dE2=null;
        try
        {
            dE1=d.putElementOnDiagram(localElName1,fromType,a.x,a.y);
            a=drAr.getFreePoint(150);
            dE2=d.putElementOnDiagram(localElName2,toType,a.x,a.y);
        }
        catch(Exception ex)
        {
        
        }
        //
        try {
            pl.selectTool(element);
        } catch(NotFoundException ex) {
            fail("BLOCKING: Can't find '"+element+"' in paletter");
        }
        //
        drAr.clickMouse(dE1.getCenterPoint().x,dE1.getCenterPoint().y,1);
        drAr.clickMouse(dE2.getCenterPoint().x,dE2.getCenterPoint().y,1);
        //
        new EventTool().waitNoEvent(2000);
         drAr.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(500);
        LinkOperator testedlink=null;
        try
        {
           /* In netbeans6.0 composition and navigable composition link are shown as aggregation in 
            * project view as they are special form of aggregation. See Issue 116868
            * Similar to navigable aggregation and navigable association.The default name
            * aggrgation and association are used in project modle.
            * As workaround for test, here set type to their default type.
            */
            LinkTypes elementTypeInProjectTree=elementType;
            if ((elementType==LinkTypes.COMPOSITION) ||(elementType==LinkTypes.NAVIGABLE_COMPOSITION))
                 elementTypeInProjectTree=LinkTypes.AGGREGATION;
            if (elementType==LinkTypes.NAVIGABLE_AGGREGATION)
                elementTypeInProjectTree=LinkTypes.AGGREGATION;
            if (elementType==LinkTypes.NAVIGABLE_ASSOCIATION)
                elementTypeInProjectTree=LinkTypes.ASSOCIATION;
            testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(elementTypeInProjectTree),0);
        }
        catch(Exception ex)
        {
            fail(element+" of type "+elementType+" wasn't added to diagram:"+ex);
        }
        if(testedlink==null)
        {
            LinkOperator altLink=null;
            try
            {
                altLink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(LinkTypes.ANY),0);
            }
            catch(Exception ex)
            {
                fail("any link find failed.");
            }
            if(altLink!=null)fail("Can't find "+elementType+" link between elements, but the is "+altLink.getType()+" link.");
       }
        d.makeComponentVisible();
        testedlink.select();
      //
        PropertySheetOperator ps=null;
        try
        {
            ps=new PropertySheetOperator(elementType+" - Properties");
        }
        catch(org.netbeans.jemmy.TimeoutExpiredException ex)
        {
            ps=new PropertySheetOperator();
            throw ex;
        }
        org.netbeans.test.uml.componentdiagram.utils.Utils.setTextProperty("Name", newName);
        //
        JPopupMenuOperator popM=testedlink.getPopup();
        popM.waitComponentShowing(true);
        popM.waitComponentVisible(true);
        try{Thread.sleep(3000);}catch(Exception ex){}
        popM.pushKey(KeyEvent.VK_LEFT);
        popM=new JPopupMenuOperator();
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
        //
        new DiagramElementOperator(d,localElName1);
        new DiagramElementOperator(d,localElName2);
        //
        testedlink=null;
        try
        {
            for(int i=0;i<10;i++)
            {
                testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(elementType),0);
                if(testedlink==null)break;
                Thread.sleep(100);
            }
        }
        catch(Exception ex)
        {
            
        }
        assertFalse("Link remains on diagram after deletion",testedlink!=null);
         //
         Node n1=new Node(pkgNode,localElName1);
         Node n2=new Node(pkgNode,localElName2);
         Node rel1=null;
         if(n1.isChildPresent("Relationships"))rel1=new Node(n1,"Relationships");
         Node rel2=null;
         if(n2.isChildPresent("Relationships"))rel2=new Node(n2,"Relationships");
         new EventTool().waitNoEvent(500);
         if(useIO)
         {
             assertFalse("There is Outgoing Edges node remains after link deletion",rel1!=null && rel1.isChildPresent("Outgoing Edges"));
             assertFalse("There is Incoming Edges node remains after link deletion",rel2!=null && rel2.isChildPresent("Incoming Edges"));
         }
         else
         {
             assertFalse("There should be no "+newName+" node within Relationships node of "+localElName1,rel1!=null && rel1.isChildPresent(newName));
             assertFalse("There should be no "+newName+" node within Relationships node of "+localElName2,rel2!=null && rel2.isChildPresent(newName));
         }
   }
}

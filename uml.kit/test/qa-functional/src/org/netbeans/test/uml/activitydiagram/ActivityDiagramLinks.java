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


package org.netbeans.test.uml.activitydiagram;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author psb
 * @spec uml/UML-EditControl.xml
 */
public class ActivityDiagramLinks extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ActivityDiagramProjectL";
    private static String project = prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private static String defaultNewElementName=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private String lastTestCase=null;
   //--
    private static String activityDiagramName1 = "acD1";
    private static String workPkg1 = "pkg1";
    private static String element1="Invocation";
    private static String elementName1="";
    private static ElementTypes elementType1=ElementTypes.INVOCATION;
    //--
    private static String activityDiagramName15 = "acD15";
    private static String workPkg15 = "pkg15";
    private static String element15="Activity Edge";
    private static String treeNode15=element15;
    private static String elementName15="";
    private static LinkTypes elementType15=LinkTypes.ACTIVITY_EDGE;
    private static boolean eIO15=true;
    //--
    private static String activityDiagramName16 = "acD16";
    private static String workPkg16 = "pkg16";
    private static String element16="Dependency";
    private static String treeNode16=element16;
    private static String elementName16="";
    private static LinkTypes elementType16=LinkTypes.DEPENDENCY;
    private static boolean eIO16=false;
    //--
    private static String activityDiagramName17 = "acD17";
    private static String workPkg17 = "pkg17";
    private static String element17="Realize";
    private static String treeNode17="Realization";
    private static String elementName17="";
    private static LinkTypes elementType17=LinkTypes.REALIZE;
    private static boolean eIO17=false;
    //--
    private static String activityDiagramName18 = "acD18";
    private static String workPkg18 = "pkg18";
    private static String element18="Usage";
    private static String treeNode18=element18;
    private static String elementName18="";
    private static LinkTypes elementType18=LinkTypes.USAGE;
    private static boolean eIO18=false;
    //--
    private static String activityDiagramName19 = "acD19";
    private static String workPkg19 = "pkg19";
    private static String element19="Permission";
    private static String treeNode19=element19;
    private static String elementName19="";
    private static LinkTypes elementType19=LinkTypes.PERMISSION;
    private static boolean eIO19=false;
    //--
    private static String activityDiagramName20 = "acD20";
    private static String workPkg20 = "pkg20";
    private static String element20="Abstraction";
    private static String treeNode20=element20;
    private static String elementName20="";
    private static LinkTypes elementType20=LinkTypes.ABSTRACTION;
    private static boolean eIO20=false;

    
    /** Need to be defined because of JUnit */
    public ActivityDiagramLinks(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.activitydiagram.ActivityDiagramLinks.class);
        return suite;
    }
    
    private DiagramOperator createDiagram(String project,String workPkg, String diagram){
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagram,NewDiagramWizardOperator.ACTIVITY_DIAGRAM);
        pto = rt.pto;
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    

  /**
   * lib should be updated to handle testCreate15
   */
  public void CtestCreate15() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlace(workPkg15,activityDiagramName15,element15,elementName15,elementType15,treeNode15,eIO15);
    }
  public void testCreate16() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlace(workPkg16,activityDiagramName16,element16,elementName16,elementType16,treeNode16,eIO16);
    }
  public void testCreate17() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlace(workPkg17,activityDiagramName17,element17,elementName17,elementType17,treeNode17,eIO17);
    }
  public void testCreate18() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlace(workPkg18,activityDiagramName18,element18,elementName18,elementType18,treeNode18,eIO18);
    }
  public void testCreate19() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlace(workPkg19,activityDiagramName19,element19,elementName19,elementType19,treeNode19,eIO19);
    }
  public void testCreate20() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlace(workPkg20,activityDiagramName20,element20,elementName20,elementType20,treeNode20,eIO20);
    }


 
      
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            org.netbeans.test.uml.activitydiagram.utils.Utils.commonActivityDiagramSetup(workdir, prName);
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
            DiagramOperator d=new DiagramOperator("acD");
            d.closeAllDocuments();
            d.waitClosed();
            new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }
    
   private void testLinkPlace(String workPkg,String diagramName,String element,String elementName,LinkTypes elementType,String treeNode,boolean useIO)
   {
       String localElName1="El1";
       String localElName2="El2";
       //
        DiagramOperator d = createDiagram(project,workPkg, diagramName);
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
       DrawingAreaOperator drAr=d.getDrawingArea();
       //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dE1=null,dE2=null;
        try
        {
            dE1=d.putElementOnDiagram(localElName1,elementType1,a.x,a.y);
            a=drAr.getFreePoint(150);
            dE2=d.putElementOnDiagram(localElName2,elementType1,a.x,a.y);
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
      //
        PropertySheetOperator ps=null;
        try
        {
            ps=new PropertySheetOperator(elementType+" - Properties");
        }
        catch(JemmyException ex)
        {
            ps=new PropertySheetOperator();
            throw ex;
        }
        LinkOperator testedlink=null;
        try
        {
            testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(elementType),0);
        }
        catch(Exception ex)
        {
            fail(element+" of type "+elementType+" wasn't added to diagram.");
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
       //
        if(testedlink==null && elementType.equals(LinkTypes.ACTIVITY_EDGE))
        {
            fail("Can't find Activity Edge/MultiFlow link between elemens, may be test library limitation, please recheck manually.");
        }
        assertTrue("Can't find "+elementType+" link between elements",testedlink!=null);
         //
         Node n1=new Node(lastDiagramNode,localElName1);
         Node n2=new Node(lastDiagramNode,localElName2);
         Node rel1=new Node(n1,"Relationships");
         Node rel2=new Node(n2,"Relationships");
         new EventTool().waitNoEvent(500);
         if(useIO)
         {
             Node nI=new Node(rel1,"Outgoing Edges");
             Node nO=new Node(rel2,"Incoming Edges");
             assertTrue("There should be only one node within Outgoing Edges node",nO.getChildren().length==1);
             assertTrue("There should be only one node within Incoming Edges node",nI.getChildren().length==1);
             assertTrue("There should be "+elementType+" node within Outgoing Edges node",nO.getChildren()[0].equals(elementType));
             assertTrue("There should be "+elementType+" node within Incoming Edges node",nI.getChildren()[0].equals(elementType));
             Node nMO=new Node(nO,elementType.toString());
             Node nMI=new Node(nI,elementType.toString());
            new EventTool().waitNoEvent(500);
                assertTrue("There should be only one node within "+elementType+" node within Outgoing Edges node",nMO.getChildren().length==1);
                 assertTrue("There should be only one node within "+elementType+" node within Incoming Edges node",nMI.getChildren().length==1);
             assertTrue("There should be "+localElName2+" node within flow node within Outgoing Edges node",nMO.isChildPresent(localElName2));
             assertTrue("There should be "+localElName1+" node within flow node within Incoming Edges node",nMI.isChildPresent(localElName1));
         }
         else
         {
             assertTrue("There should be "+treeNode+" node within Relationships node of "+localElName1,rel1.getChildren()[0].equals(treeNode));
             assertTrue("There should be "+treeNode+" node within Relationships node of "+localElName2,rel2.getChildren()[0].equals(treeNode));
             Node rn1=new Node(rel1,treeNode);
             Node rn2=new Node(rel2,treeNode);
            new EventTool().waitNoEvent(500);
                assertTrue("There should be only one node within "+treeNode+" node",rn1.getChildren().length==1);
                assertTrue("There should be only one node within "+treeNode+" node",rn2.getChildren().length==1);
             Node de1=new Node(rn1,localElName2);
             Node de2=new Node(rn2,localElName1);
         }
   }
}
